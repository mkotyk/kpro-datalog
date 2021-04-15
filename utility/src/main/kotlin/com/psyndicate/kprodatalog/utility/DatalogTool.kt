package com.psyndicate.kprodatalog.utility

import com.ftdi.FTDevice
import com.psyndicate.kprodatalog.KManagerDatalogFrame
import com.psyndicate.kprodatalog.KManagerDatalogHeader
import com.psyndicate.kprodatalog.kpro2.*
import com.psyndicate.kprodatalog.serialization.decode
import com.psyndicate.kprodatalog.serialization.encode
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.system.exitProcess

class DatalogTool(private val quiet: Boolean = false) : Closeable {
    private val ftdiDevice: FTDevice
    private val KPRO_PRODUCT_ID = 0xF5F8

    init {
        FTDevice.setVIDPID(0x0403, KPRO_PRODUCT_ID)
        val devs = FTDevice.getDevices(true)
        ftdiDevice = FTDevice.getDevices(true).find {
            println("Found 0x%04x 0x%04x".format(it.devID shr 16, it.devID and 0xFFFF))
            it.devID == (0x403 shl 16) + KPRO_PRODUCT_ID
        } ?: let {
            println("No suitable USB Serial devices found.")
            exitProcess(1)
        }

        ftdiDevice.open()
        println(ftdiDevice.devDescription)
        //ftdiDevice.setResetPipeRetryCount(10)
        ftdiDevice.setTimeouts(100, 100)
        ftdiDevice.latencyTimer = 5
    }

    fun replay(datalogInputStream: InputStream) {
        datalogInputStream.use { dataInput ->
            val headerByteBuffer = ByteBuffer
                .wrap(dataInput.readNBytes(KManagerDatalogHeader.SIZE))
                .order(ByteOrder.LITTLE_ENDIAN)
            val header: KManagerDatalogHeader = headerByteBuffer.decode()
            val startTime = System.currentTimeMillis()
            for (index in 0 until header.numberOfFrames) {
                val frameByteBuffer = ByteBuffer
                    .wrap(dataInput.readNBytes(header.dataFrameSize))
                    .order(ByteOrder.LITTLE_ENDIAN)
                val frame: KManagerDatalogFrame = frameByteBuffer.decode()

                val now = System.currentTimeMillis()
                val timeDelta = startTime + frame.timeOffset - now
                if (timeDelta > 0) {
                    Thread.sleep(timeDelta)
                }

                if (!quiet) {
                    displayFrame(frame)
                }

                val request = ftdiDevice.inputStream.read()
                if (request >= 0) {
                    val message = when (request.toByte()) {
                        MessageType.Status.id -> StatusMessage(online = true)
                        MessageType.Datalog1.id -> frame.toDatalog1Message()
                        MessageType.Datalog2.id -> frame.toDatalog2Message()
                        MessageType.Datalog3.id -> frame.toDatalog3Message()
                        else -> {
                            println("Requested unknown message type [$request]")
                            null
                        }
                    }

                    message?.let { msg ->
                        val msgBuffer = ByteBuffer
                            .allocate(msg.size + 3)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .put(request.toByte())
                            .put(msg.size.toByte())
                            .encode(msg)
                        msgBuffer.put((msgBuffer.array().checksum(0, msg.size + 2)).toByte())
                        ftdiDevice.outputStream.write(msgBuffer.array())
                    }
                }
            }
        }
    }

    fun capture(datalogOutputStream: OutputStream, maxDuration: Long = 60000) {
        data class PollData(var nextPollTime: Long, val intervalMs: Long, val condition: (Boolean) -> Boolean)

        val messagePollTimes = mutableMapOf(
            MessageType.Status to PollData(0, 500) { true },
            MessageType.Datalog1 to PollData(0, 100) { it },
            MessageType.Datalog2 to PollData(0, 1000) { it },
            MessageType.Datalog3 to PollData(0, 5000) { it },
        )

        var online = false
        val recordedFrames = mutableListOf<KManagerDatalogFrame>()
        var runningFrame = KManagerDatalogFrame()
        var frameNumber = 0
        val startTime = System.currentTimeMillis()
        while (true) {
            val now = System.currentTimeMillis()
            if (now - startTime > maxDuration) {
                break
            }

            messagePollTimes.forEach { msgType, data ->
                if (now > data.nextPollTime && data.condition(online)) {
                    messagePollTimes[msgType]?.nextPollTime = now + data.intervalMs
                    try {
                        ftdiDevice.purgeBuffer(true, true)
                        ftdiDevice.outputStream.write(msgType.id.toInt())

                        val expectedReadSize = when (msgType) {
                            MessageType.Status -> StatusMessage.MSG_SIZE
                            MessageType.Datalog1 -> Datalog1Message.MSG_SIZE
                            MessageType.Datalog2 -> Datalog2Message.MSG_SIZE
                            MessageType.Datalog3 -> Datalog3Message.MSG_SIZE
                        } + 3 // (Type, length, checksum bytes)

                        val buffer = ByteArray(expectedReadSize)
                        val bytesRead = ftdiDevice.inputStream.read(buffer)
                        if (expectedReadSize - bytesRead > 2) {
                            println("Short read for $msgType.  Expected $expectedReadSize, got $bytesRead")
                        }

                        val msgType = buffer[0]
                        val msgLength = buffer[1].toInt()
                        val cksum = buffer.map { it.toUByte() }.sum().toInt() and 0xFF

                        //println("Message id: 0x%02X".format(msgType.id.toInt()))
                        //println("Message type: 0x%02x".format(msgType))
                        //println("Message length: %d".format(msgLength))
                        if (0 != cksum) {
                            println("Error: Calculated checksum and message checksum do not match: $cksum != 0 for $msgType")
                        }

                        val messageByteBuffer = ByteBuffer.wrap(buffer, 2, msgLength).order(ByteOrder.LITTLE_ENDIAN)
                        when (msgType.toByte()) {
                            MessageType.Status.id -> {
                                val statusMessage = messageByteBuffer.decode<StatusMessage>()
                                online = statusMessage.online
                            }
                            MessageType.Datalog1.id -> {
                                val newFrame = runningFrame
                                    .apply(messageByteBuffer.decode<Datalog1Message>())
                                    .copy(frameNumber = frameNumber++, timeOffset = (now - startTime).toInt())
                                recordedFrames.add(newFrame)
                                runningFrame = newFrame
                            }
                            MessageType.Datalog2.id -> {
                                val msg = messageByteBuffer.decode<Datalog2Message>()
                                val newFrame = runningFrame
                                    .apply(msg)
                                    .copy(frameNumber = frameNumber++, timeOffset = (now - startTime).toInt())
                                println("ECT: %03d -> %5.5f".format(msg.ECT.toInt(), newFrame.ECT))
                                recordedFrames.add(newFrame)
                                runningFrame = newFrame

                            }
                            MessageType.Datalog3.id -> {
                                val newFrame = runningFrame
                                    .apply(messageByteBuffer.decode<Datalog3Message>())
                                    .copy(frameNumber = frameNumber++, timeOffset = (now - startTime).toInt())
                                recordedFrames.add(newFrame)
                                runningFrame = newFrame
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        ftdiDevice.resetDevice()
                        Thread.sleep(5000)
                        return@forEach
                    }

                    if (online && !quiet) {
                        displayFrame(runningFrame)
                    }
                }
            }
        }

        datalogOutputStream.use { outputStream ->
            outputStream.write(
                ByteBuffer
                    .allocate(KManagerDatalogHeader.SIZE)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .encode(
                        KManagerDatalogHeader(
                            numberOfFrames = frameNumber,
                            durationMs = (System.currentTimeMillis() - startTime).toInt(),
                            dataFrameSize = KManagerDatalogFrame.SIZE
                        )
                    )
                    .array()
            )
            recordedFrames.forEach {
                outputStream.write(
                    ByteBuffer
                        .allocate(KManagerDatalogFrame.SIZE)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .encode(it)
                        .array()
                )
            }
        }
    }

    private fun displayFrame(frame: KManagerDatalogFrame) {
        with(frame) {
            print("#%08d  %08dms ".format(frameNumber, timeOffset))
            print(
                "RPM: %4d  VSS: %4f  GEAR: %1d MAP: %2.2f IGN: %2f INJ: %3f ".format(
                    RPM,
                    speed,
                    gear,
                    map,
                    ignition,
                    injectorDuration
                )
            )
            print("IAT: %3.2f\u00B0C ECT: %3.2f\u00B0C BAT: %2.1f".format(IAT, ECT, batteryVoltage))
            print("\r")
        }
    }

    private fun ByteArray.checksum(offset: Int, length: Int): Int = this.drop(offset).take(length).sum() and 0xFF

    override fun close() {
        ftdiDevice.close()
    }
}

enum class Mode {
    capture,
    replay
}

fun main(args: Array<String>) {
    val parser = ArgParser("KPro Datalog Tool")
    val mode by parser.option(
        ArgType.Choice<Mode>(),
        shortName = "m",
        description = "Mode of operation"
    ).required()
    val quiet by parser.option(
        ArgType.Boolean,
        shortName = "q",
        description = "Quiet.  Don't display datalog frames while processing"
    ).default(false)
    val kdlFileName by parser.option(
        ArgType.String,
        shortName = "d",
        description = "KManager format .kdl datalog file"
    ).required()

    try {
        parser.parse(args)
    } catch (ex: Exception) {
        println(ex.message)
        exitProcess(1)
    }

    val kdlFile = File(kdlFileName)
    DatalogTool(quiet = quiet).use { datalogTool ->
        when (mode) {
            Mode.capture -> datalogTool.capture(
                kdlFile.run {
                    createNewFile()
                    outputStream()
                }
            )
            Mode.replay -> datalogTool.replay(kdlFile.inputStream())
        }
    }

    exitProcess(0)
}
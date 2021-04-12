package com.psyndicate.kprodatalog.utility

import com.ftdichip.usb.FTDI
import com.ftdichip.usb.FTDIUtility
import com.psyndicate.kprodatalog.KManagerDatalogFrame
import com.psyndicate.kprodatalog.KManagerDatalogHeader
import com.psyndicate.kprodatalog.kpro2.*
import com.psyndicate.kprodatalog.serialization.decode
import com.psyndicate.kprodatalog.serialization.encode
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.system.exitProcess

class DatalogTool(private val quiet: Boolean = false) {
    private val ftdiDevice: FTDI

    init {
        val devices = FTDIUtility.findFTDIDevices()
        if (!devices.iterator().hasNext()) {
            println("No suitable USB Serial devices found.")
            exitProcess(1)
        }
        val usbDevice = devices.iterator().next()

        println("Using: ${usbDevice.deviceId}")
        ftdiDevice = FTDI.getInstance(usbDevice)
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

                val usbFrame = ftdiDevice.read()
                if (usbFrame.isNotEmpty()) {
                    val request = usbFrame[0]
                    val message = when (request) {
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
                            .put(request)
                            .put(msg.size.toByte())
                            .encode(msg)
                        msgBuffer.put((msgBuffer.array().checksum(0, msg.size + 2)).toByte())
                        ftdiDevice.write(msgBuffer.array())
                    }
                }
            }
        }
    }

    fun capture(datalogOutputStream: OutputStream, maxDuration: Long = 60000) {
        data class PollData(var nextPollTime: Long, val intervalMs: Long, val condition: (Boolean) -> Boolean)

        val messagePollTimes = mutableMapOf(
            MessageType.Status to PollData(0, 150) { true },
            MessageType.Datalog1 to PollData(0, 50) { it },
            MessageType.Datalog2 to PollData(0, 500) { it },
            MessageType.Datalog3 to PollData(0, 5000) { it },
        )

        val request = ByteArray(1)
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

                    request[0] = MessageType.Status.id
                    ftdiDevice.write(request)
                    val bytes = ftdiDevice.read()
                    val byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                    val messageType = byteBuffer.get()
                    val messageLength = byteBuffer.get().toInt()
                    val messagePayload = ByteArray(messageLength).also { byteBuffer.get(it.size) }

                    if (0 != checksum(messageType.toInt(), messageLength, messagePayload)) {
                        println("Error: Calculated checksum and message checksum do not match")
                    }

                    val messageByteBuffer = ByteBuffer.wrap(messagePayload).order(ByteOrder.LITTLE_ENDIAN)
                    when (messageType) {
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
                        MessageType.Datalog1.id -> {
                            val newFrame = runningFrame
                                .apply(messageByteBuffer.decode<Datalog2Message>())
                                .copy(frameNumber = frameNumber++, timeOffset = (now - startTime).toInt())
                            recordedFrames.add(newFrame)
                            runningFrame = newFrame

                        }
                        MessageType.Datalog1.id -> {
                            val newFrame = runningFrame
                                .apply(messageByteBuffer.decode<Datalog3Message>())
                                .copy(frameNumber = frameNumber++, timeOffset = (now - startTime).toInt())
                            recordedFrames.add(newFrame)
                            runningFrame = newFrame
                        }
                    }
                    if (!quiet) {
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
            println("#%08d  %08dms".format(frameNumber, timeOffset))
            println("RPM: %4d  VSS: %4d  GEAR: %1d".format(RPM, speed, gear))
            println("IAT: %3.2f\u00B0C ECT: %3.2f\u00B0C".format(IAT, ECT))
        }
    }

    private fun ByteArray.checksum(offset: Int, length: Int): Int = this.drop(offset).take(length).sum() and 0xFF
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
    val datalogTool = DatalogTool(quiet = quiet)

    when (mode) {
        Mode.capture -> datalogTool.capture(
            kdlFile.run {
                createNewFile()
                outputStream()
            }
        )
        Mode.replay -> datalogTool.replay(kdlFile.inputStream())
    }

    exitProcess(0)
}
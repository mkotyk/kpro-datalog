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

    /**
     * usb 1-1: new full-speed USB device number 11 using xhci_hcd
     * usb 1-1: New USB device found, idVendor=0403, idProduct=f5f8, bcdDevice= 4.00
     * usb 1-1: New USB device strings: Mfr=1, Product=2, SerialNumber=3
     * usb 1-1: Product: Hondata K-Series ECU
     * usb 1-1: Manufacturer: Hondata, Inc
     * usb 1-1: SerialNumber: HD1000
     */

    init {
        FTDevice.setVIDPID(0x0403, KPRO_PRODUCT_ID)
        ftdiDevice = FTDevice.getDevices().firstOrNull()
//        .find {
//            println("Found 0x%04x 0x%04x".format(it.devID shr 16, it.devID and 0xFFFF))
//            it.devID == (0x403 shl 16) + KPRO_PRODUCT_ID
//        }
            ?: let {
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
        ftdiDevice.setBaudRate(9600)
        datalogInputStream.use { dataInput ->
            val headerByteBuffer = ByteBuffer
                .wrap(dataInput.readNBytes(KManagerDatalogHeader.SIZE))
                .order(ByteOrder.LITTLE_ENDIAN)
            val header: KManagerDatalogHeader = headerByteBuffer.decode()
            var datalogStartTime: Long? = null
            var nextTime = 0L
            var frame = KManagerDatalogFrame()
            while (true) {
                val now = System.currentTimeMillis()
                if (datalogStartTime != null && now > nextTime) {
                    val frameByteBuffer = ByteBuffer
                        .wrap(dataInput.readNBytes(header.dataFrameSize))
                        .order(ByteOrder.LITTLE_ENDIAN)
                    frame = frameByteBuffer.decode<KManagerDatalogFrame>().also {
                        nextTime = datalogStartTime!! + it.timeOffset
                        if (it.frameNumber >= header.numberOfFrames) {
                            return@replay
                        }
                    }
                }
                val request = ftdiDevice.read()
                if (request > 0) {
                    when (request.toByte()) {
                        MessageType.Status.id -> {
                            ByteBuffer
                                .allocate(StatusMessage.MSG_SIZE + 3)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .put(request.toByte())
                                .put(StatusMessage.MSG_SIZE.toByte())
                                .encode(StatusMessage(online = true))
                        }
                        MessageType.Datalog1.id -> {
                            if (datalogStartTime == null) {
                                datalogStartTime = now
                            }
                            ByteBuffer
                                .allocate(Datalog1Message.MSG_SIZE + 3)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .put(request.toByte())
                                .put(Datalog1Message.MSG_SIZE.toByte())
                                .encode(frame.toDatalog1Message())
                        }
                        MessageType.Datalog2.id -> {
                            if (datalogStartTime == null) {
                                datalogStartTime = now
                            }
                            ByteBuffer
                                .allocate(Datalog2Message.MSG_SIZE + 3)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .put(request.toByte())
                                .put(Datalog2Message.MSG_SIZE.toByte())
                                .encode(frame.toDatalog2Message())
                        }
                        MessageType.Datalog3.id -> {
                            if (datalogStartTime == null) {
                                datalogStartTime = now
                            }
                            ByteBuffer
                                .allocate(Datalog3Message.MSG_SIZE + 3)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .put(request.toByte())
                                .put(Datalog3Message.MSG_SIZE.toByte())
                                .encode(frame.toDatalog3Message())
                        }
                        else -> {
                            println("Requested unknown message type [$request]")
                            null
                        }
                    }?.let { msg ->
                        val msgArray = msg.array()
                        msgArray[msgArray.size - 1] = (-msgArray.checksum(0, msgArray.size - 1)).toByte()
                        ftdiDevice.outputStream.write(msgArray)
                    }

                    if (!quiet && datalogStartTime != null) {
                        displayFrame(frame)
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

            messagePollTimes.forEach { pollMsgType, data ->
                if (now > data.nextPollTime && data.condition(online)) {
                    messagePollTimes[pollMsgType]?.nextPollTime = now + data.intervalMs
                    try {
                        ftdiDevice.purgeBuffer(true, true)
                        ftdiDevice.outputStream.write(pollMsgType.id.toInt())

                        val expectedReadSize = when (pollMsgType) {
                            MessageType.Status -> StatusMessage.MSG_SIZE
                            MessageType.Datalog1 -> Datalog1Message.MSG_SIZE
                            MessageType.Datalog2 -> Datalog2Message.MSG_SIZE
                            MessageType.Datalog3 -> Datalog3Message.MSG_SIZE
                        } + 3 // (Type, length, checksum bytes)

                        val buffer = ByteArray(expectedReadSize)
                        val bytesRead = ftdiDevice.inputStream.read(buffer)
                        if (expectedReadSize - bytesRead > 2) {
                            println("Short read for $pollMsgType.  Expected $expectedReadSize, got $bytesRead")
                        }

                        val msgType = buffer[0]
                        val msgLength = buffer[1].toInt()
                        val msgCksum = buffer.map { it.toUByte() }.sum().toInt() and 0xFF

                        //println("Message id: 0x%02X".format(msgType.id.toInt()))
                        //println("Message type: 0x%02x".format(msgType))
                        //println("Message length: %d".format(msgLength))
                        if (0 != msgCksum) {
                            println("Error: Calculated checksum and message checksum do not match: $msgCksum != 0 for $msgType")
                        }

                        val messageByteBuffer = ByteBuffer.wrap(buffer, 2, msgLength).order(ByteOrder.LITTLE_ENDIAN)
                        when (msgType) {
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

    private fun ByteArray.checksum(offset: Int = 0, length: Int = this.size - offset): Int =
        this.drop(offset).take(length).sum() and 0xFF

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
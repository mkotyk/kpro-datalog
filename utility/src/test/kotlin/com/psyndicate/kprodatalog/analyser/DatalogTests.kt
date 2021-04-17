package com.psyndicate.kprodatalog.analyser

import com.psyndicate.kprodatalog.KManagerDatalogFrame
import com.psyndicate.kprodatalog.KManagerDatalogHeader
import com.psyndicate.kprodatalog.kpro2.*
import com.psyndicate.kprodatalog.serialization.decode
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs

internal class DatalogTests {
    data class FrameData(
        val frameNumber: Int,
        val message: Message,
        val kdlFrame: KManagerDatalogFrame,
        val constructedKdlFrame: KManagerDatalogFrame,
    )

    private val captureInputStream =
        DatalogTests::class.java.classLoader.getResourceAsStream("bindumps/03.13.2010.raw_datalog.bin")
            ?: error("Unable to load capture stream")
    private val datalogInputStream =
        DatalogTests::class.java.classLoader.getResourceAsStream("datalogs/03.13.2010.tester2.kdl")
            ?: error("Unable to load datalog stream")

    private fun loadTestData(): List<FrameData> {
        val frameComparisonList = mutableListOf<FrameData>()
        val kdlHeader = ByteBuffer
            .wrap(datalogInputStream.readNBytes(KManagerDatalogHeader.SIZE))
            .order(ByteOrder.LITTLE_ENDIAN)
            .decode<KManagerDatalogHeader>()
        println("Frames: ${kdlHeader.numberOfFrames}")
        println("Duration (ms): ${kdlHeader.durationMs}")
        println("Frame Size: ${kdlHeader.dataFrameSize}")

        var runningKdlFrame = KManagerDatalogFrame()
        var synced = false
        for (frameNumber in 0 until kdlHeader.numberOfFrames) {
            val messageFrameNum = captureInputStream.readNBytes(4) // Frame number added by shim
            val messageType = captureInputStream.read()
            val messageLength = captureInputStream.read()
            val messagePayload = captureInputStream.readNBytes(messageLength)

            // Validate message good
            if (0 != checksum(messageType, messageLength, messagePayload)) {
                println("Error: Calculated checksum and message checksum do not match")
            }

            val payloadByteBuffer = ByteBuffer
                .wrap(messagePayload)
                .order(ByteOrder.LITTLE_ENDIAN)

            runningKdlFrame = runningKdlFrame.copy(frameNumber = frameNumber)
            val message = when (messageType.toByte()) {
                MessageType.Status.id -> {
                    payloadByteBuffer.decode<StatusMessage>().also { status ->
                        println("Online: ${status.online}")
                    }
                }
                MessageType.Datalog1.id -> {
                    payloadByteBuffer.decode<Datalog1Message>().also {
                        runningKdlFrame = runningKdlFrame.apply(it)
                    }
                }
                MessageType.Datalog2.id -> {
                    payloadByteBuffer.decode<Datalog2Message>().also {
                        runningKdlFrame = runningKdlFrame.apply(it)
                    }
                }
                MessageType.Datalog3.id -> {
                    payloadByteBuffer.decode<Datalog3Message>().also {
                        runningKdlFrame = runningKdlFrame.apply(it)
                    }
                }
                else -> error("Unexpected type")
            }
            do {
                val kdlFrame = ByteBuffer
                    .wrap(datalogInputStream.readNBytes(kdlHeader.dataFrameSize))
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .decode<KManagerDatalogFrame>()

                synced = kdlFrame.RPM == runningKdlFrame.RPM && kdlFrame.speed == runningKdlFrame.speed

                if (synced) {
                    frameComparisonList.add(
                        FrameData(
                            frameNumber = kdlFrame.frameNumber,
                            message = message,
                            kdlFrame = kdlFrame,
                            constructedKdlFrame = runningKdlFrame
                        )
                    )
                }
            } while (!synced)
        }
        return frameComparisonList
    }

    val properties = listOf(
        KManagerDatalogFrame::RPM,
        KManagerDatalogFrame::speed,
        KManagerDatalogFrame::map,
        KManagerDatalogFrame::clv,
        KManagerDatalogFrame::tps,
        KManagerDatalogFrame::camAngle,
        KManagerDatalogFrame::targetCamAngle,
        KManagerDatalogFrame::injectorDuration,
        KManagerDatalogFrame::ignition,
        KManagerDatalogFrame::IAT,
        KManagerDatalogFrame::ECT,
        KManagerDatalogFrame::unknown1,
        KManagerDatalogFrame::unknown2,
        KManagerDatalogFrame::unknown3,
        KManagerDatalogFrame::unknown4,
        KManagerDatalogFrame::unknown5,
        KManagerDatalogFrame::rvslck,
        KManagerDatalogFrame::bksw,
        KManagerDatalogFrame::acsw,
        KManagerDatalogFrame::accl,
        KManagerDatalogFrame::scs,
        KManagerDatalogFrame::eps,
        KManagerDatalogFrame::flr,
        KManagerDatalogFrame::vtp,
        KManagerDatalogFrame::vtc,
        KManagerDatalogFrame::fanc,
        KManagerDatalogFrame::mil,
        KManagerDatalogFrame::blankFlag,
        KManagerDatalogFrame::o2Voltage,
        KManagerDatalogFrame::o2mA,
        KManagerDatalogFrame::so2,
        KManagerDatalogFrame::lambda,
        KManagerDatalogFrame::targetLambda,
        KManagerDatalogFrame::shortTermTrim,
        KManagerDatalogFrame::longTermTrim,
        KManagerDatalogFrame::fuelStatus,
        KManagerDatalogFrame::padding1,
        KManagerDatalogFrame::knockRetard,
        KManagerDatalogFrame::knockLevelVolts,
        KManagerDatalogFrame::knockThresholdVolts,
        KManagerDatalogFrame::knockCount,
        KManagerDatalogFrame::padding2,
        KManagerDatalogFrame::AAP,
        KManagerDatalogFrame::RTP,
        KManagerDatalogFrame::batteryVoltage,
        KManagerDatalogFrame::eldAmps,
        KManagerDatalogFrame::n2o_arm1,
        KManagerDatalogFrame::n2o_on1,
        KManagerDatalogFrame::n2o_arm2,
        KManagerDatalogFrame::n2o_on2,
        KManagerDatalogFrame::n2o_arm3,
        KManagerDatalogFrame::n2o_on3,
        KManagerDatalogFrame::diagnostics1,
        KManagerDatalogFrame::diagnostics2,
        KManagerDatalogFrame::gear,
        KManagerDatalogFrame::boostControllerDuty,
        KManagerDatalogFrame::unknown7,
        KManagerDatalogFrame::unknown8,
        KManagerDatalogFrame::tps2,
        KManagerDatalogFrame::eldVoltage,
        KManagerDatalogFrame::datalogging
    )

    private fun <T : Number> assertSame(propertyName: String, expected: T, actual: T, maxDifference: Double) {
        val difference: Double = when (expected) {
            is Double -> abs(expected - actual as Double)
            is Int -> abs((expected - actual as Int).toDouble())
            is Byte -> abs((expected - actual as Byte).toDouble())
            else -> error("Unexpected Type")
        }

        assertTrue(
            "$propertyName difference of $difference.  Max difference allowed $maxDifference",
            difference < maxDifference
        )
    }

    @Test
    fun `test constructed datalog matches`() {
        val dataFrames = loadTestData()

        dataFrames.forEach { frame ->
            properties.forEach { property ->
                val kdlValue = property.getter.call(frame.kdlFrame) as Number
                val constructedValue = property.getter.call(frame.constructedKdlFrame) as Number

                val maxDifference = when (property) {
                    KManagerDatalogFrame::ECT -> 1.0 // This value isn't tuned perfectly
                    KManagerDatalogFrame::tps2 -> 0.1
                    else -> 0.0001
                }

                assertSame(property.name, kdlValue, constructedValue, maxDifference)
            }
        }
    }

    @Test
    fun `test show ect correlation`() {
        // ECT sensor curve
        // 12k = -20C
        // 5.0k = 0C
        // 2.0k = 20C
        // 1.2k = 40C
        // 0.7k = 60C
        // 0.4K = 80C
        // 0.2K = 100C
        // 0.1K = 120C

        val dataFrames = loadTestData()
        setOf(
            255 to -20.00,
            179 to 13.00,
            0 to 120.0
        ).union(
            dataFrames.mapNotNull {
                if (it.message is Datalog2Message) {
                    it.message.ECT.toInt() to it.kdlFrame.ECT
                } else {
                    null
                }
            })
            //.map { ((256 - it.first) / 2.2) - 22 to it.second }
            .distinctBy { it.first }
            .sortedBy { it.first }
            .forEach { println("${it.first} ${it.second} ${it.first.toDouble().ectTempConversion}") }
    }

    @Test
    fun `test hondata temp correction curve`() {
        val dataFrames = loadTestData()

        val corrections = mapOf(
            /* deg C to percent */
            -21.55 to 10.99853515625,
            31.64 to 4.998779296875,
            86.196875 to 0,
            175.4 to -5.999755859375,
            208.4 to -7.9986572265625,
        )

        fun tryConvert(input: Double): Double {
            val approxTemp = ((255.0 - input) / 1.8214 - 23)
            val correction = approxTemp.polyConvert(
                listOf(
                    8.4488203635012571e+000,
                    -1.1508603827469159e-001,
                    1.7656775114223509e-004,
                    4.3539420121624008e-007,
                    -2.1599082393997798e-009
                )
            )
            return approxTemp - (approxTemp * correction / 100)
        }
        setOf(
            255 to -20.00,
            179 to 13.00,
            0 to 120.0
        ).union(
            dataFrames.mapNotNull {
                if (it.message is Datalog2Message) {
                    it.message.ECT.toInt() to it.kdlFrame.ECT
                } else {
                    null
                }
            })
            .distinctBy { it.first }
            .sortedBy { it.first }
            .forEach { println("${it.first} ${it.second} ${tryConvert(it.first.toDouble())}") }
    }
}
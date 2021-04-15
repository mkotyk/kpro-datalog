package com.psyndicate.kprodatalog.kpro2

import com.psyndicate.kprodatalog.KManagerDatalogFrame
import com.psyndicate.kprodatalog.serialization.SerializedSize

@ExperimentalUnsignedTypes
@Suppress("ArrayInDataClass")
data class Datalog3Message(
    /*
        Offset: 2 (2h)
        Description: Diagnostic checks 1
        Units: Bit mask (unknown)
     */
    val diagnostics1: UByte = 0u,

    /*
        Offset: 3 (3h)
        Description: Diagnostic checks 2
        Units: Bit mask (unknown)
    */
    val diagnostics2: UByte = 0u,

    /*
        Offset: 4 (4h)
        Description: P codes
        Conversion: Straight copy of 20 (14h) bytes
     */
    @SerializedSize(20)
    val diagnosticCodes: ByteArray = ByteArray(20)
) : Message {
    companion object {
        const val MSG_SIZE = 22
    }

    override val size: Int get() = MSG_SIZE
}

@ExperimentalUnsignedTypes
fun KManagerDatalogFrame.apply(message: Datalog3Message): KManagerDatalogFrame = this.copy(
    diagnostics1 = message.diagnostics1.toByte(),
    diagnostics2 = message.diagnostics2.toByte(),
    diagnosticCodes = message.diagnosticCodes
)

@ExperimentalUnsignedTypes
fun KManagerDatalogFrame.toDatalog3Message(): Datalog3Message = Datalog3Message(
    diagnostics1 = diagnostics1.toUByte(),
    diagnostics2 = diagnostics2.toUByte(),
    diagnosticCodes = diagnosticCodes
)
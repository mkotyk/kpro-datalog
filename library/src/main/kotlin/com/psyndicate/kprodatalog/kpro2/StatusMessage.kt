package com.psyndicate.kprodatalog.kpro2

import com.psyndicate.kprodatalog.serialization.SerializedSize

@Suppress("ArrayInDataClass")
data class StatusMessage(
    val hardwareVersionMinor: Byte = 0x10,
    val hardwareVersionMajor: Byte = 0x2,
    val hardwareSerialNumber: UShort = 0x0u,
    val firmwareVersionMinor: Byte = 0x2,
    val firmwareVersionMajor: Byte = 0x2,
    val ecuSoftwareVersionMinor: Byte = 0x31,
    val ecuSoftwareVersionMajor: Byte = 0x12,
    val ecuType: Byte = 0x3,
    @SerializedSize(4)
    val unknowns1: ByteArray = byteArrayOf(
        0xFAu.toByte(),
        0xEDu.toByte(),
        0x30,
        0x66
    ),
    val online: Boolean,
    @SerializedSize(15)
    val unknowns2: ByteArray = byteArrayOf(
        0x01,
        0x00,
        0x00,
        0x90u.toByte(),
        0x5E,
        0x00,
        0x00,
        0xF4u.toByte(),
        0x0A,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00
    )
) : Message {
    companion object {
        const val MSG_SIZE = 29
    }

    override val size: Int get() = MSG_SIZE
}
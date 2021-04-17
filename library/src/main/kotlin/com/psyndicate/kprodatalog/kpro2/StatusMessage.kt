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
    val ecuChecksum: Int = 0,
    val online: Boolean,
    // Firmware ok?
    // ECU security enabled?
    @SerializedSize(3)
    val unknowns2: ByteArray = byteArrayOf(
        0x01,
        0x00,
        0x00
    ),
    val statsNumSectorsRead: Int = 24208,
    val statsNumSectorsWritten: Int = 2804,
    val statsNumSectorsError: Int = 0,
    override var checksum: Byte = 0
) : Message {
    companion object {
        const val MSG_SIZE = 30
    }

    override val size: Int get() = MSG_SIZE
    override val id: Byte get() = MessageType.Status.id
}
package com.psyndicate.kprodatalog

import com.psyndicate.kprodatalog.serialization.SerializedSize

@Suppress("ArrayInDataClass")
data class KManagerDatalogHeader(
    @SerializedSize(size = 6)
    val id: ByteArray = ByteArray(KFLASH.length) { idx -> KFLASH[idx].toByte() },
    val numberOfFrames: Int,
    val dataFrameSize: Int,
    val unknown1: Short = 0,
    val unknown2: Int = 0,
    val unknown3: Short = 0,
    val durationMs: Int,
    @SerializedSize(size = 128)
    val unknown4: ByteArray = ByteArray(128) { 0 }
) {
    companion object {
        const val KFLASH = "KFLASH"
        const val SIZE = 154
    }
}

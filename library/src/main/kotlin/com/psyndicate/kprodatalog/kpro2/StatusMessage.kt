package com.psyndicate.kprodatalog.kpro2

import com.psyndicate.kprodatalog.serialization.SerializedSize

@Suppress("ArrayInDataClass")
data class StatusMessage(
    @SerializedSize(13)
    val unknowns1: ByteArray = ByteArray(13),
    val online: Boolean,
    @SerializedSize(15)
    val unknowns2: ByteArray = ByteArray(15),
) : Message {
    override val size:Int get() = 29
}
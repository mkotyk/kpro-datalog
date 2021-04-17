package com.psyndicate.kprodatalog.kpro2

import com.psyndicate.kprodatalog.serialization.encode
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface Message {
    val id: Byte
    val size: Int
    var checksum: Byte
}

@ExperimentalUnsignedTypes
fun checksum(type: Int, length: Int, data: ByteArray): Int =
    (data.map { it.toUByte() }.sum().toInt() + type + length) and 0xFF

fun ByteArray.checksum(offset: Int = 0, length: Int = this.size - offset): Int = this.drop(offset).take(length).sum() and 0xFF

inline fun <reified T : Message> T.toByteArray(): ByteArray {
    val bb = ByteBuffer
        .allocate(this.size + 2)
        .order(ByteOrder.LITTLE_ENDIAN)
        .put(id)
        .put(size.toByte())
        .encode(this, T::class)

    this.checksum = (-bb.array().checksum()).toByte()

    val completedMsg =  bb
        .rewind()
        .order(ByteOrder.LITTLE_ENDIAN)
        .put(id)
        .put(size.toByte())
        .encode(this, T::class)
        .array()

    if (completedMsg.checksum() != 0) {
        throw Exception("Checksum failure")
    }
    return completedMsg
}

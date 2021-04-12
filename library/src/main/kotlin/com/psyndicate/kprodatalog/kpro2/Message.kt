package com.psyndicate.kprodatalog.kpro2

interface Message {
    val size: Int
}

@ExperimentalUnsignedTypes
fun checksum(type: Int, length: Int, data: ByteArray): Int =
    (data.map { it.toUByte() }.sum().toInt() + type + length) and 0xFF
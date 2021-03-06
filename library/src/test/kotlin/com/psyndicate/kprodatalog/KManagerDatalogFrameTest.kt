package com.psyndicate.kprodatalog

import com.psyndicate.kprodatalog.serialization.decode
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class KManagerDatalogFrameTest {
    private val sampleFrameEncoded = listOf(
        0x01, 0x00, 0x00, 0x00, 0x1e, 0x00, 0xb4, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xbe, 0x9f, 0x1a, 0x2f, 0xdd, 0x24, 0xd6, 0x3f,
        0x00, 0x00, 0x00, 0x00, 0x00, 0xc6, 0x41, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xf0, 0x3f,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x58, 0x39, 0xb4, 0xc8, 0x76, 0xbe, 0xf7, 0x3f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x40,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0xf0, 0xe4, 0x3f, 0x56, 0x64, 0x3b, 0xdf, 0x4f, 0x8d, 0x71, 0xbf,
        0x00, 0x00, 0x00, 0x00, 0x00, 0xa0, 0xe9, 0x3f, 0xef, 0x7d, 0x90, 0x1e, 0x56, 0x5c, 0xef, 0x3f,
        0xfc, 0x47, 0x82, 0xb7, 0xc6, 0x38, 0xf0, 0x3f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x50, 0x34, 0xc0,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xbe, 0x3f,
        0x00, 0x00, 0x00, 0x00, 0x00, 0xec, 0x13, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xe0, 0x3f, 0x19, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0xc0, 0x7b, 0x4e, 0xa6, 0xaf, 0x12, 0x46, 0xf9, 0x3f,
        0x00, 0x00, 0x00, 0x00, 0xd8, 0x8b, 0x09, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    ).map { it.toByte() }.toByteArray()

    private val sampleFrameEncoded2 = listOf(
        0x02, 0x00, 0x00, 0x00, 0x50, 0x00, 0x00, 0x00, 0xbb, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xbe, 0x9f, 0x1a, 0x2f, 0xdd, 0x24, 0xd6, 0x3f,
        0x00, 0x00, 0x00, 0x00, 0x00, 0xc6, 0x41, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xf0, 0x3f,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x58, 0x39, 0xb4, 0xc8, 0x76, 0xbe, 0xf7, 0x3f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1a, 0x40,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0xc8, 0xe4, 0x3f, 0x56, 0x64, 0x3b, 0xdf, 0x4f, 0x8d, 0x72, 0xbf,
        0x00, 0x00, 0x00, 0x00, 0x00, 0xa0, 0xe9, 0x3f, 0x69, 0x92, 0x0a, 0x44, 0x49, 0x78, 0xef, 0x3f,
        0xfc, 0x47, 0x82, 0xb7, 0xc6, 0x38, 0xf0, 0x3f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x50, 0x34, 0xc0,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x80, 0xc6, 0x3f,
        0x00, 0x00, 0x00, 0x00, 0x00, 0xec, 0x13, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xe0, 0x3f, 0x19, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x04, 0xc0, 0x7b, 0x4e, 0xa6, 0xaf, 0x12, 0x46, 0xf9, 0x3f,
        0x00, 0x00, 0x00, 0x00, 0xd8, 0xa9, 0x09, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    ).map { it.toByte() }.toByteArray()

    @Test
    fun `test decode sample 1`() {
        val result: KManagerDatalogFrame = ByteBuffer.wrap(sampleFrameEncoded).order(ByteOrder.LITTLE_ENDIAN).decode()

        assertEquals(1, result.frameNumber)
    }

    @Test
    fun `test decode sample 2`() {
        val result: KManagerDatalogFrame = ByteBuffer.wrap(sampleFrameEncoded2).order(ByteOrder.LITTLE_ENDIAN).decode()

        assertEquals(2, result.frameNumber)
    }
}
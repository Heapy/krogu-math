package io.heapy.krogu.math

import kotlin.test.*

class HashOracleTest {
    private class Poison(sign: Int, bytes: ByteArray) : BigInteger(sign, bytes) {
        override fun abs(): BigInteger = error("abs override called")
        override fun testBit(n: Int): Boolean = error("testBit override called")
    }

    @Test fun signedPowersAndNeighborsAcrossAllWordAlignments() {
        val expected = jdk25StructuredHashResults().iterator()
        for (power in 0 until JDK25_HASH_POWER_LIMIT) for (offset in -1..1) {
            // Independent byte encoding of 2^power + offset; no BigInteger arithmetic.
            val bytes = ByteArray(power / 8 + 1)
            if (offset == -1) {
                bytes.fill(-1)
                bytes[0] = ((1 shl (power % 8)) - 1).toByte()
            } else {
                bytes[0] = (1 shl (power % 8)).toByte()
                if (offset == 1) {
                    if (power == 0) bytes[0] = 2
                    else bytes[bytes.lastIndex] = (bytes.last().toInt() or 1).toByte()
                }
            }
            for (sign in listOf(-1, 1)) {
                assertTrue(expected.hasNext())
                assertEquals(expected.next(), Poison(sign, bytes).hashCode(), "power=$power offset=$offset sign=$sign")
            }
        }
        assertFalse(expected.hasNext())
    }

    @Test fun denseBinaryPatternsThrough65536Bytes() {
        val expected = jdk25DenseHashResults().iterator()
        for (size in jdk25HashSizes()) for (pattern in 0..4) {
            val bytes = ByteArray(size) { index -> when (pattern) {
                0 -> 0
                1 -> 255
                2 -> 170
                3 -> 85
                else -> (index * 73 + size * 11) xor (index ushr 3)
            }.toByte() }
            for (sign in listOf(-1, 1)) {
                assertTrue(expected.hasNext())
                assertEquals(expected.next(), Poison(sign, bytes).hashCode(), "bytes=$size pattern=$pattern sign=$sign")
            }
        }
        assertFalse(expected.hasNext())
    }
}

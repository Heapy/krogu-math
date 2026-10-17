package io.heapy.krogu.math

import kotlin.test.*

class MagnitudeLimitsTest {
    @Test fun representationBoundaryDoesNotWrap() {
        assertEquals(Int.MAX_VALUE, MagnitudeLimits.checkedBitLength(71582789, 127))
        assertFailsWith<ArithmeticException> { MagnitudeLimits.checkedBitLength(71582789, 128) }
        assertEquals(71582789, MagnitudeLimits.packedWords(268435456, true))
        assertFailsWith<ArithmeticException> { MagnitudeLimits.packedWords(268435457, false) }
        assertEquals(1073741824, MagnitudeLimits.initialSqrtShift(Int.MAX_VALUE))
    }

    @Test fun largePaddingAndComplementCarry() {
        val padded = ByteArray(131072) { -1 }
        padded[padded.lastIndex] = 0
        assertEquals(BigInteger.valueOf(-256), BigInteger(padded))
        for (bytes in listOf(15, 30, 45, 120, 1020)) {
            val encoded = ByteArray(bytes + 1)
            encoded[0] = -1
            val expected = BigInteger.ONE.shiftLeft(bytes * 8).negate()
            assertEquals(expected, BigInteger(encoded))
            assertContentEquals(encoded, expected.toByteArray())
        }
        val unsigned = ByteArray(4096) { -1 }
        assertEquals(BigInteger.ONE.shiftLeft(32768).subtract(BigInteger.ONE), BigInteger(1, unsigned))
        assertEquals(BigInteger.ONE.negate(), BigInteger(unsigned))
    }
}

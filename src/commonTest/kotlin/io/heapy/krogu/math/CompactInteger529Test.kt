package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompactInteger529Test {
    @Test fun overflowWordsAndMixedCancellationRetainFullEndpoints() {
        val min = BigInteger.valueOf(Long.MIN_VALUE)
        val max = BigInteger.valueOf(Long.MAX_VALUE)
        val two64 = BigInteger("18446744073709551616")
        assertEquals("18446744073709551614", max.add(max).toString())
        assertEquals("-18446744073709551616", min.add(min).toString())
        assertEquals("18446744073709551615", max.subtract(min).toString())
        assertEquals("-18446744073709551615", min.subtract(max).toString())
        assertEquals(min, two64.negate().subtract(min)) //65bit fallback must remain valid.
        val above = BigInteger("9223372036854775808")
        assertEquals(BigInteger.ZERO, above.add(min))
        assertEquals(BigInteger.ZERO, min.add(above))
        assertEquals(BigInteger.valueOf(Long.MAX_VALUE - 122), above.subtract(BigInteger.valueOf(123)))
        assertEquals(BigInteger.valueOf(-Long.MAX_VALUE + 122), BigInteger.valueOf(123).subtract(above))
        assertEquals(min, BigInteger("-9223372036854775809").add(BigInteger.ONE))
        assertEquals("9223372036854775808", above.toString())
        assertEquals("18446744073709551616", two64.toString())
    }

    @Test fun compactFloatRoundsItsNumericValue() {
        assertEquals(16777216.0, BigInteger("16777217").toFloat().toDouble())
        assertEquals(36028801313931264.0, BigInteger("36028799166447617").toFloat().toDouble())
        assertEquals(-36028801313931264.0, BigInteger("-36028799166447617").toFloat().toDouble())
        assertEquals(-16777216.0, BigInteger("-16777217").toFloat().toDouble())
        assertEquals(16777220.0, BigInteger("16777219").toFloat().toDouble())
    }

    @Test fun signedBoundaryTransitions() {
        val min = BigInteger.valueOf(Long.MIN_VALUE)
        val max = BigInteger.valueOf(Long.MAX_VALUE)
        val above = BigInteger("9223372036854775808")
        val below = BigInteger("-9223372036854775809")
        assertTrue(min.hasCompactStorage529())
        assertTrue(max.hasCompactStorage529())
        assertFalse(above.hasCompactStorage529())
        assertFalse(below.hasCompactStorage529())
        assertEquals(above, min.negate())
        assertEquals(above, min.divide(BigInteger.valueOf(-1)))
        assertEquals(BigInteger.ZERO, min.remainder(BigInteger.valueOf(-1)))
        assertEquals(min, above.negate())
        assertTrue(above.subtract(BigInteger.ONE).hasCompactStorage529())
        assertTrue(below.add(BigInteger.ONE).hasCompactStorage529())
        assertEquals(above, max.add(BigInteger.ONE))
        assertEquals(below, min.subtract(BigInteger.ONE))
        assertEquals(BigInteger("18446744073709551616"), min.multiply(BigInteger.valueOf(-2)))
    }

    @Test fun compactMetadataAndEncodingAcrossEveryWordBoundary() {
        val values = listOf(0L, 1L, -1L, (1L shl 29) - 1, 1L shl 29, -(1L shl 30),
            (1L shl 30) + 1, (1L shl 32) - 1, -(1L shl 32), (1L shl 60) - 1,
            1L shl 60, -(1L shl 60), Long.MAX_VALUE, Long.MIN_VALUE)
        for (value in values) {
            val integer = BigInteger.valueOf(value)
            assertEquals(value, integer.toLong())
            assertEquals(value.toInt(), integer.toInt())
            assertEquals(integer, BigInteger(integer.toByteArray()))
            assertEquals(integer, BigInteger(value.toString()))
            assertEquals(64 - (if (value < 0) value.inv() else value).countLeadingZeroBits(), integer.bitLength())
            assertEquals((if (value < 0) value.inv() else value).countOneBits(), integer.bitCount())
            assertEquals(if (value == 0L) -1 else value.countTrailingZeroBits(), integer.getLowestSetBit())
            for (bit in listOf(0, 29, 30, 31, 32, 59, 60, 62, 63, 64, 99))
                assertEquals(if (bit >= 64) value < 0 else value and (1L shl bit) != 0L, integer.testBit(bit))
            assertEquals(value.toString().removePrefix("-").length, integer.decimalMagnitudeDigits())
            assertTrue(integer.hasCompactStorage529())
        }
    }

    @Test fun primitiveArithmeticAndWideFallbackAgreeWithIndependentDecimalFixtures() {
        val pairs = listOf(3037000499L to 3037000499L, 3037000500L to 3037000500L,
            Long.MIN_VALUE to -1L, Long.MIN_VALUE to 1L, Long.MAX_VALUE to 2L)
        val expected = listOf("9223372030926249001", "9223372037000250000",
            "9223372036854775808", "-9223372036854775808", "18446744073709551614")
        for ((index, pair) in pairs.withIndex()) {
            val a = BigInteger.valueOf(pair.first); val b = BigInteger.valueOf(pair.second)
            val product = a.multiply(b)
            assertEquals(BigInteger(expected[index]), product)
            assertEquals(a, product.divide(b))
            assertEquals(BigInteger.ZERO, product.remainder(b))
            assertEquals(pair.first, a.toLong())
            assertEquals(pair.second, b.toLong())
        }
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class DecimalScaling533Test {
    @Test
    fun signedHalfwayStickyAndCarryHaveLiteralResults() {
        // HALF_UP, HALF_DOWN, HALF_EVEN. The last row carries to an additional digit.
        for ((input, up, down, even) in listOf(
            listOf("2.4500", "2.5", "2.4", "2.4"),
            listOf("2.4501", "2.5", "2.5", "2.5"),
            listOf("2.5499", "2.5", "2.5", "2.5"),
            listOf("2.5500", "2.6", "2.5", "2.6"),
            listOf("9.9500", "10", "9.9", "10"),
        )) for (negative in listOf(false, true)) {
            val prefix = if (negative) "-" else ""
            val value = BigDecimal(prefix + input)
            for ((mode, expected) in listOf(RoundingMode.HALF_UP to up,
                RoundingMode.HALF_DOWN to down, RoundingMode.HALF_EVEN to even)) {
                val rounded = value.round(MathContext(2, mode))
                assertEquals(BigDecimal(prefix + expected), rounded)
                assertEquals(rounded, rounded.round(MathContext(2, mode)))
            }
            assertFailsWith<ArithmeticException> { value.round(MathContext(2, RoundingMode.UNNECESSARY)) }
        }
        for (sign in listOf(-1, 1)) for (mode in RoundingMode.entries) {
            val value = BigDecimal(BigInteger.valueOf(sign * 12_000L), 4)
            assertEquals(BigDecimal(BigInteger.valueOf(sign * 12L), 1), value.round(MathContext(2, mode)))
        }
    }

    @Test
    fun compactAndChunkedStripPreserveCohortsAtScaleBoundaries() {
        val bodies = listOf("1", "-123456789", "9223372036854775807", "-9223372036854775808",
            "7".repeat(127), "-" + "8".repeat(127))
        for (body in bodies) for (zeros in listOf(0, 1, 2, 8, 9, 10, 17, 18, 19, 63, 128)) {
            val coefficient = BigInteger(body + "0".repeat(zeros))
            for (scale in listOf(zeros, Int.MIN_VALUE + zeros, Int.MAX_VALUE)) {
                val source = BigDecimal(coefficient, scale)
                val stripped = source.stripTrailingZeros()
                assertEquals(BigInteger(body), stripped.unscaledValue())
                assertEquals(scale - zeros, stripped.scale())
                assertEquals(0, stripped.compareTo(source))
                assertSame(stripped, stripped.stripTrailingZeros())
                assertEquals(coefficient, source.unscaledValue())
                assertEquals(scale, source.scale())
            }
            if (zeros > 0) assertFailsWith<ArithmeticException> {
                BigDecimal(coefficient, Int.MIN_VALUE + zeros - 1).stripTrailingZeros()
            }
        }
        assertEquals(BigDecimal.ZERO, BigDecimal(BigInteger.ZERO, Int.MIN_VALUE).stripTrailingZeros())
    }

    @Test
    fun wideBinaryFactorsDoNotImplyDecimalTail() {
        for (bits in listOf(65, 127, 512)) for (zeros in listOf(1, 2, 3, 9, 10, 19)) {
            val body = BigInteger.ONE.shiftLeft(bits)
            for (sign in listOf(1, -1)) {
                val expected = if (sign < 0) body.negate() else body
                val coefficient = expected.multiply(BigInteger.TEN.pow(zeros))
                for (scale in listOf(zeros, Int.MIN_VALUE + zeros, Int.MAX_VALUE)) {
                    val stripped = BigDecimal(coefficient, scale).stripTrailingZeros()
                    assertEquals(expected, stripped.unscaledValue())
                    assertEquals(scale - zeros, stripped.scale())
                }
            }
        }
    }

    @Test
    fun cacheBoundaryAndExtremeRescaleKeepExactAndFinitePolicies() {
        for (exponent in listOf(0, 1, 8, 9, 17, 18, 19, 20, 37)) {
            val scaled = BigDecimal("-7").setScale(exponent)
            assertEquals(BigInteger("-7" + "0".repeat(exponent)), scaled.unscaledValue())
            assertEquals(exponent, scaled.scale())
            assertEquals(BigDecimal("-7"), scaled.setScale(0))
        }
        for (mode in RoundingMode.entries) {
            val source = BigDecimal(BigInteger("1000"), Int.MIN_VALUE + 3)
            assertEquals(BigDecimal(BigInteger.ONE, Int.MIN_VALUE), source.round(MathContext(1, mode)))
            assertSame(source, source.round(MathContext.UNLIMITED))
            assertSame(source, source.setScale(Int.MIN_VALUE + 3, mode))
        }
        assertFailsWith<ArithmeticException> {
            BigDecimal(BigInteger("999"), Int.MIN_VALUE + 1).round(MathContext(1, RoundingMode.HALF_UP))
        }
        // Existing guard/sticky arithmetic must still cancel before final rounding.
        val left = BigDecimal("1000000000000000000000000000000000000001")
        val right = BigDecimal("-1000000000000000000000000000000000000000")
        for (mode in RoundingMode.entries) assertEquals(BigDecimal.ONE, left.add(right, MathContext(1, mode)))
    }

    @Test
    fun initialVirtualPrecisionAndLegacySetScaleDispatchRemain() {
        val calls = mutableListOf<String>()
        val source = object : BigDecimal("9.9500") {
            override fun precision(): Int { calls += "precision"; return super.precision() }
            @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
            override fun setScale(newScale: Int, roundingMode: Int): BigDecimal {
                calls += "legacy"; return super.setScale(newScale, roundingMode)
            }
        }
        assertEquals(BigDecimal("10"), source.round(MathContext(2, RoundingMode.HALF_UP)))
        assertEquals(listOf("precision"), calls)
        calls.clear()
        assertEquals(BigDecimal("10.0"), source.setScale(1, RoundingMode.HALF_UP))
        assertEquals(listOf("legacy"), calls)
    }
}

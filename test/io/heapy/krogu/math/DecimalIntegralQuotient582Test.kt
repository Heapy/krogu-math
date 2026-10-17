// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DecimalIntegralQuotient582Test {
    private fun power(n: Int): Long {
        require(n in 0..6)
        var result = 1L
        repeat(n) { result *= 10 }
        return result
    }

    private fun product(a: Long, b: Long): Long {
        require(a >= 0 && b > 0 && a <= Long.MAX_VALUE / b)
        return a * b
    }

    // One-shot bounded rational truncation, then independent preferred-cohort selection.
    // No production divide, decimal stream or BigInteger arithmetic supplies an oracle.
    private fun expected(a: Long, b: Long, scale: Int, sign: Int): Pair<Long, Int> {
        val numerator = if (scale < 0) product(a, power(-scale)) else a
        val denominator = if (scale > 0) product(b, power(scale)) else b
        var coefficient = numerator / denominator
        val remainder = numerator % denominator
        assertEquals(numerator, product(coefficient, denominator) + remainder)
        assertTrue(remainder in 0 until denominator)
        if (coefficient == 0L) return 0L to scale
        var resultScale = 0
        while (resultScale > scale && coefficient % 10 == 0L) {
            coefficient /= 10
            resultScale--
        }
        if (scale > resultScale) {
            coefficient = product(coefficient, power(scale - resultScale))
            resultScale = scale
        }
        return sign * coefficient to resultScale
    }

    private fun assertPair(expected: Pair<Long, Int>, actual: BigDecimal, label: String) {
        assertEquals(expected.first.toString(), actual.unscaledValue().toString(), "$label coefficient")
        assertEquals(expected.second, actual.scale(), "$label scale")
    }

    private class Truthful(coefficient: Long, scale: Int) : BigDecimal(BigInteger.valueOf(coefficient), scale) {
        override fun precision(): Int = super.precision()
        override fun scale(): Int = super.scale()
        override fun signum(): Int = super.signum()
        override fun divideToIntegralValue(divisor: BigDecimal): BigDecimal = super.divideToIntegralValue(divisor)
    }

    @Test fun ordinaryAndTruthfulIntegralCohortsAgainstBoundedRationals() {
        val pairs = listOf(1L to 2L, 1L to 3L, 1L to 99L, 1L to 999L,
            10L to 1L, 101L to 1L, 1200L to 3L, 1001001L to 1L,
            100000000001L to 1L, 1000000000001L to 3L,
            2000000000L to 1000000001L, 3999999999L to 1999999999L,
            1234567890123L to 1000000001L)
        for ((a, b) in pairs) for (scale in -4..4)
            for (leftSign in listOf(-1, 1)) for (rightSign in listOf(-1, 1)) for (mask in 0..3) {
                val left = if (mask and 1 == 0) BigDecimal.valueOf(leftSign * a, scale)
                    else Truthful(leftSign * a, scale)
                val right = if (mask and 2 == 0) BigDecimal.valueOf(rightSign * b)
                    else Truthful(rightSign * b, 0)
                val leftBefore = left.toString()
                val rightBefore = right.toString()
                val answer = expected(a, b, scale, leftSign * rightSign)
                assertPair(answer, left.divideToIntegralValue(right), "$a/$b S=$scale signs=$leftSign/$rightSign mask=$mask")
                assertEquals(leftBefore, left.toString())
                assertEquals(rightBefore, right.toString())
            }
    }

    private fun words(value: Long): IntArray {
        var remaining = value
        val result = mutableListOf<Int>()
        while (remaining > 0) {
            result += (remaining and 0x3fffffff).toInt()
            remaining = remaining ushr 30
        }
        return result.toIntArray()
    }

    @Test fun directNegativeStreamKeepsUnreadDigitsDeferredZerosAndInputs() {
        // 100000000001/1 needs a deferred whole base-1e9 cell followed by
        // a nonzero original digit. 1/99 emits a zero between nonzero digits.
        // The two-cell divisors exercise trial carry and decimal subtraction borrow.
        for ((a, b) in listOf(1L to 2L, 1L to 3L, 1L to 99L, 1L to 999L,
            1L to 1000000000000L, 1000L to 1L, 100000000001L to 1L,
            2000000000L to 1000000001L, 3999999999L to 1999999999L))
            for (scale in listOf(-1, -2, -4)) for (sign in listOf(-1, 1)) {
                val left = words(a)
                val right = words(b)
                val leftBefore = left.copyOf()
                val rightBefore = right.copyOf()
                val answer = expected(a, b, scale, sign)
                val actual = DecimalDivision581.integral(sign, left, right, scale)
                assertEquals(answer.first.toString(), actual.first.toString(), "$a/$b S=$scale sign=$sign")
                assertEquals(answer.second, actual.second)
                assertContentEquals(leftBefore, left)
                assertContentEquals(rightBefore, right)
            }
    }

    @Test fun virtualZeroSkipPreferredZerosAndUnlimitedDelegation() {
        // z<k, z=k, z>k, plus ignored original low digits for S>0.
        val controls = listOf(
            listOf(1L, 2L, -4L, 5L, -3L),
            listOf(1L, 1L, -4L, 1L, -4L),
            listOf(1000L, 1L, -4L, 1000L, -4L),
            listOf(1234567L, 3L, 2L, 411500L, 2L),
            listOf(1001L, 1L, -2L, 1001L, -2L))
        for (row in controls) for (sign in listOf(-1, 1)) for (mode in RoundingMode.entries) {
            val left = BigDecimal.valueOf(sign * row[0], row[2].toInt())
            val right = BigDecimal.valueOf(row[1])
            val answer = sign * row[3] to row[4].toInt()
            assertPair(answer, left.divideToIntegralValue(right), "literal integral $row")
            assertPair(answer, left.divideToIntegralValue(right, MathContext(0, mode)), "p0 integral $row $mode")
        }
        // These use the proved exact reduced-denominator preflight, without
        // allocating an enormous coefficient or reducing the production range.
        for (sign in listOf(-1, 1)) {
            assertPair(sign.toLong() to Int.MIN_VALUE,
                BigDecimal.valueOf(sign.toLong(), Int.MIN_VALUE).divideToIntegralValue(BigDecimal.ONE), "MIN unit")
            assertPair(5L * sign to Int.MIN_VALUE + 1,
                BigDecimal.valueOf(sign.toLong(), Int.MIN_VALUE).divideToIntegralValue(BigDecimal.valueOf(2)), "MIN half")
            assertPair(1000L * sign to Int.MIN_VALUE,
                BigDecimal.valueOf(1000L * sign, Int.MIN_VALUE).divideToIntegralValue(BigDecimal.ONE), "MIN preferred padding")
        }
        // Neighboring exact results on both sides of the preflight boundary.
        // Below it the ordinary stream has a tiny terminating prefix and skips;
        // above it the same required pair comes from rational exact reduction.
        for (zeros in listOf(646456992, 646456993, 646456994)) {
            assertPair(5L to 1 - zeros, BigDecimal.valueOf(1, -zeros)
                .divideToIntegralValue(BigDecimal.valueOf(2)), "preflight exact neighbor $zeros")
        }
        // Here the fractional residual cannot be zero and the final minimal
        // integer coefficient necessarily exceeds H digits; rejection must be cheap.
        for (scale in listOf(-646456994, Int.MIN_VALUE))
            assertFailsWith<ArithmeticException> {
                BigDecimal.valueOf(1, scale).divideToIntegralValue(BigDecimal.valueOf(3))
            }
        assertPair(0L to -5, BigDecimal.valueOf(0, -3).divideToIntegralValue(BigDecimal.valueOf(1, 2)), "zero preferred")
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divideToIntegralValue(BigDecimal.ZERO) }
        assertFailsWith<ArithmeticException> { BigDecimal.ZERO.divideToIntegralValue(BigDecimal.ZERO) }
    }

    @Test fun capturedMismatchFallbackAndExplicitCallerContracts() {
        class Mismatch(val digits: Int, val scales: Int, val negativeSign: Boolean) : BigDecimal("10.50") {
            override fun precision(): Int = super.precision() + digits
            override fun scale(): Int = super.scale() + scales
            override fun signum(): Int = if (negativeSign) -super.signum() else super.signum()
            override fun divide(divisor: BigDecimal, mc: MathContext): BigDecimal = BigDecimal("7.9")
        }
        // Diagnostic replacement output checks only the granted old fallback;
        // it is not asserted to be truthful arithmetic or a callback-count contract.
        for (source in listOf(Mismatch(1, 0, false), Mismatch(0, 1, false), Mismatch(0, 0, true)))
            assertEquals(BigDecimal("7"), source.divideToIntegralValue(BigDecimal("2.00")))

        val marker = BigDecimal("7.00")
        val failure = IllegalStateException("caller-owned582")
        val expectedDivisor = BigDecimal("2.00")
        class Caller(private val throws: Boolean) : BigDecimal("10.50") {
            override fun divideToIntegralValue(divisor: BigDecimal): BigDecimal {
                assertSame(expectedDivisor, divisor)
                if (throws) throw failure
                return marker
            }
            fun inherited(value: BigDecimal): BigDecimal = super.divideToIntegralValue(value)
        }
        val returned = Caller(false)
        val base: BigDecimal = returned
        assertSame(marker, base.divideToIntegralValue(expectedDivisor))
        assertEquals(BigDecimal("5"), returned.inherited(expectedDivisor))
        assertSame(failure, assertFailsWith<IllegalStateException> { Caller(true).divideToIntegralValue(expectedDivisor) })
    }
}

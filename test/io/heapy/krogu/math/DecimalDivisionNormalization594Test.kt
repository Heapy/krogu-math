// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalDivisionNormalization594Test {
    private data class Exact(val left: String, val right: String, val coefficient: String, val scale: Int)

    private fun signed(text: String, sign: Int): String = if (sign < 0) "-$text" else text

    private fun assertPair(coefficient: String, scale: Int, actual: BigDecimal, label: String) {
        assertEquals(BigInteger(coefficient), actual.unscaledValue(), label)
        assertEquals(scale, actual.scale(), label)
    }

    @Test fun exactNoncompactRatiosRetainLengthsPaddingScalesAndSigns() {
        // N=10^20+1. Each quotient is an independent exact cancellation,
        // including different coefficient lengths and equal padded significands.
        val cases = listOf(
            Exact("100000000000000000001", "200000000000000000002", "5", 1),
            Exact("1000000000000000000010", "200000000000000000002", "5", 0),
            Exact("1000000000000000000010", "100000000000000000001", "10", 0),
            Exact("100000000000000000001", "1", "100000000000000000001", 0),
            Exact("1", "100000000000000000000", "1", 20),
        )
        for (case in cases) for (scale in listOf(Int.MIN_VALUE, -10, 0, 10, Int.MAX_VALUE))
            for (leftSign in listOf(-1, 1)) for (rightSign in listOf(-1, 1)) {
                val leftText = signed(case.left, leftSign)
                val rightText = signed(case.right, rightSign)
                val a = BigDecimal(BigInteger(leftText), scale)
                val b = BigDecimal(BigInteger(rightText), scale)
                val label = "$leftText/$rightText commonScale=$scale"
                assertPair(signed(case.coefficient, leftSign * rightSign), case.scale, a.divide(b), label)
                assertEquals(leftText, a.unscaledValue().toString(), "$label immutable source")
                assertEquals(rightText, b.unscaledValue().toString(), "$label immutable divisor")
                assertEquals(scale, a.scale(), label)
                assertEquals(scale, b.scale(), label)
            }
    }

    // Independent primitive N/D rounding: no production decimal metadata,
    // normalized comparison, quotient exponent or BigDecimal arithmetic oracle.
    private fun rounded525(denominator: Int, sign: Int, mode: RoundingMode): Long {
        val quotient = 525 / denominator
        val remainder = 525 % denominator
        if (remainder == 0) return sign * quotient.toLong()
        val twice = 2 * remainder // denominator<=1000, hence twice<=1998.
        val increment = when (mode) {
            RoundingMode.UP -> true
            RoundingMode.DOWN -> false
            RoundingMode.CEILING -> sign > 0
            RoundingMode.FLOOR -> sign < 0
            RoundingMode.HALF_UP -> twice >= denominator
            RoundingMode.HALF_DOWN -> twice > denominator
            RoundingMode.HALF_EVEN -> twice > denominator || twice == denominator && quotient % 2 != 0
            RoundingMode.UNNECESSARY -> throw ArithmeticException("independent inexact rational")
        }
        return sign * (quotient + if (increment) 1 else 0).toLong()
    }

    @Test fun fixedScaleNoncompactProductsMatchIndependentRationalsForAllModes() {
        // 105N/20N=5.25, N=10^20+1; both stored coefficients are noncompact.
        // Negative/zero/positive target scales cover above/below/tie/exact tails.
        val leftMagnitude = "10500000000000000000105"
        val rightMagnitude = "2000000000000000000020"
        val targets = listOf(-1 to 1000, 0 to 100, 1 to 10, 2 to 1)
        for (scale in listOf(Int.MIN_VALUE, -3, 0, 7, Int.MAX_VALUE))
            for (leftSign in listOf(-1, 1)) for (rightSign in listOf(-1, 1)) {
                val leftText = signed(leftMagnitude, leftSign)
                val rightText = signed(rightMagnitude, rightSign)
                val a = BigDecimal(BigInteger(leftText), scale)
                val b = BigDecimal(BigInteger(rightText), scale)
                for ((target, denominator) in targets) for (mode in RoundingMode.entries) {
                    val label = "105N/20N signs=$leftSign/$rightSign commonScale=$scale target=$target $mode"
                    val expected = try { rounded525(denominator, leftSign * rightSign, mode) }
                        catch (_: ArithmeticException) { null }
                    if (expected == null) assertFailsWith<ArithmeticException>(label) { a.divide(b, target, mode) }
                    else assertPair(expected.toString(), target, a.divide(b, target, mode), label)
                }
                assertEquals(leftText, a.unscaledValue().toString(), "immutable source")
                assertEquals(rightText, b.unscaledValue().toString(), "immutable divisor")
                assertEquals(scale, a.scale())
                assertEquals(scale, b.scale())
            }
    }

    @Test fun noncompactBoundaryStickyZeroAndEndpointErrorsRemainMathematical() {
        val n = "100000000000000000001"
        val tenNPlusOne = "1000000000000000000011"
        val a = BigDecimal(BigInteger(n), 0)
        val b = BigDecimal(BigInteger(tenNPlusOne), 0)
        // N/(10N+1)<0.1, with a nonzero discarded suffix. At scale0,
        // directed UP/CEILING select1, all other finite modes select0.
        for (mode in RoundingMode.entries) {
            if (mode == RoundingMode.UNNECESSARY) assertFailsWith<ArithmeticException> { a.divide(b, 0, mode) }
            else assertPair(if (mode == RoundingMode.UP || mode == RoundingMode.CEILING) "1" else "0",
                0, a.divide(b, 0, mode), "sticky less-than normalized significand/$mode")
        }
        // Long.MIN_VALUE's magnitude is deliberately noncompact, even though
        // it has19 decimal digits. The other operand remains genuinely compact.
        val boundary = BigDecimal(BigInteger("9223372036854775808"), 0)
        val compact = BigDecimal(BigInteger("9223372036854775807"), 0)
        for (mode in RoundingMode.entries) {
            if (mode == RoundingMode.UNNECESSARY) {
                assertFailsWith<ArithmeticException> { boundary.divide(compact, 0, mode) }
                assertFailsWith<ArithmeticException> { compact.divide(boundary, 0, mode) }
            } else {
                assertPair(if (mode == RoundingMode.UP || mode == RoundingMode.CEILING) "2" else "1",
                    0, boundary.divide(compact, 0, mode), "just above1/$mode")
                assertPair(if (mode == RoundingMode.DOWN || mode == RoundingMode.FLOOR) "0" else "1",
                    0, compact.divide(boundary, 0, mode), "just below1/$mode")
            }
        }
        val noncompact = BigInteger(n)
        for (scale in listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE)) {
            val zero = BigDecimal(BigInteger.ZERO, scale)
            val nonzero = BigDecimal(noncompact, scale)
            assertPair("0", 0, zero.divide(nonzero), "zero exact preferred scale")
            assertPair("0", -1, zero.divide(nonzero, -1, RoundingMode.UNNECESSARY), "zero requested scale")
            assertFailsWith<ArithmeticException> { nonzero.divide(zero) }
            assertFailsWith<ArithmeticException> { nonzero.divide(zero, 0, RoundingMode.DOWN) }
            assertFailsWith<ArithmeticException> { nonzero.divide(BigDecimal.valueOf(3, scale)) }
        }
        // Ordinary exact division has a mandatory natural scale outside Int;
        // these error controls do not request giant coefficient rescaling.
        assertFailsWith<ArithmeticException> {
            BigDecimal(noncompact, Int.MAX_VALUE).divide(BigDecimal(noncompact, Int.MIN_VALUE))
        }
        assertFailsWith<ArithmeticException> {
            BigDecimal(noncompact, Int.MIN_VALUE).divide(BigDecimal(noncompact, Int.MAX_VALUE))
        }
    }
}

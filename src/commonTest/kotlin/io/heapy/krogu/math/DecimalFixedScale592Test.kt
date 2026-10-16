// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class DecimalFixedScale592Test {
    private fun limbs(value: Long): IntArray {
        var remaining = value
        val result = mutableListOf<Int>()
        while (remaining != 0L) {
            result += (remaining and 0x3fffffff).toInt()
            remaining = remaining ushr 30
        }
        return result.toIntArray()
    }

    // Independent bounded rational oracle: all rescaled Long products are
    // guarded before multiplication; no production decimal/integer operations.
    private fun expected(numerator: Long, denominator: Long, sign: Int, mode: RoundingMode): Long? {
        val quotient = numerator / denominator
        val remainder = numerator % denominator
        if (remainder == 0L) return sign * quotient
        if (mode == RoundingMode.UNNECESSARY) return null
        val half = remainder.compareTo(denominator - remainder)
        val add = when (mode) {
            RoundingMode.UP -> true
            RoundingMode.DOWN -> false
            RoundingMode.CEILING -> sign > 0
            RoundingMode.FLOOR -> sign < 0
            RoundingMode.HALF_UP -> half >= 0
            RoundingMode.HALF_DOWN -> half > 0
            RoundingMode.HALF_EVEN -> half > 0 || half == 0 && quotient % 2 != 0L
            RoundingMode.UNNECESSARY -> error("handled above")
        }
        return sign * (quotient + if (add) 1 else 0)
    }

    @Test fun directRawCoefficientsMatchIndependentSignedRationalsAndExactPadding() {
        val sources = listOf(1L, 5L, 9L, 15L, 25L, 95L, 1001L, 1500L, 10501L,
            1000000001L, 1073741823L, 1073741825L, 1152921504606846975L, 1152921504606846977L)
        val divisors = listOf(1L, 2L, 3L, 10L, 20L, 999999999L, 1073741824L, 1152921504606846975L)
        for (a in sources) for (b in divisors) for (shift in -3..3) {
            var power = 1L
            repeat(if (shift < 0) -shift else shift) { power *= 10 }
            if ((shift > 0 && a > Long.MAX_VALUE / power) ||
                (shift < 0 && b > Long.MAX_VALUE / power)) continue
            val numerator = if (shift > 0) a * power else a
            val denominator = if (shift < 0) b * power else b
            val left = limbs(a)
            val right = limbs(b)
            val leftBefore = left.copyOf()
            val rightBefore = right.copyOf()
            for (sign in listOf(-1, 1)) for (mode in RoundingMode.entries) {
                val label = "$a/$b shift=$shift sign=$sign mode=$mode"
                val wanted = expected(numerator, denominator, sign, mode)
                if (wanted == null) assertFailsWith<ArithmeticException>(label) {
                    DecimalDivision581.fixedScale592(sign, left, right, shift.toLong(), mode)
                } else assertEquals(wanted.toString(),
                    DecimalDivision581.fixedScale592(sign, left, right, shift.toLong(), mode).toString(), label)
            }
            assertEquals(leftBefore.toList(), left.toList())
            assertEquals(rightBefore.toList(), right.toList())
        }
        // Exact zero-tail skipping must retain every required coefficient zero.
        for (sign in listOf(-1, 1)) for (mode in RoundingMode.entries)
            assertEquals((if (sign < 0) "-" else "") + "1" + "0".repeat(100),
                DecimalDivision581.fixedScale592(sign, intArrayOf(1), intArrayOf(1), 100, mode).toString())
    }

    private fun check(coefficient: Long, scale: Int, actual: BigDecimal) {
        assertEquals(coefficient.toString(), actual.unscaledValue().toString())
        assertEquals(scale, actual.scale())
    }

    @Test fun publicEndpointsKeepRequestedScaleAndLegacyValidation() {
        for (sourceSign in listOf(-1, 1)) for (divisorSign in listOf(-1, 1)) {
            val sign = sourceSign * divisorSign
            val source = BigDecimal(BigInteger.valueOf(sourceSign.toLong()), Int.MAX_VALUE)
            val divisor = BigDecimal(BigInteger.valueOf(divisorSign.toLong()), Int.MIN_VALUE)
            for (target in listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE)) for (mode in RoundingMode.entries) {
                val coefficient = when (mode) {
                    RoundingMode.UP -> sign.toLong()
                    RoundingMode.CEILING -> if (sign > 0) 1L else 0L
                    RoundingMode.FLOOR -> if (sign < 0) -1L else 0L
                    else -> 0L
                }
                if (mode == RoundingMode.UNNECESSARY) assertFailsWith<ArithmeticException> {
                    source.divide(divisor, target, mode)
                } else {
                    check(coefficient, target, source.divide(divisor, target, mode))
                    check(coefficient, target, source.divide(divisor, target, mode.ordinal))
                }
            }
            // The real maximum positive shift requires an unsupported coefficient;
            // its mathematical digit lower bound must reject before a huge loop.
            val huge = BigDecimal(BigInteger.valueOf(sourceSign.toLong()), Int.MIN_VALUE)
            val tiny = BigDecimal(BigInteger.valueOf(divisorSign.toLong()), Int.MAX_VALUE)
            for (mode in RoundingMode.entries) assertFailsWith<ArithmeticException> {
                huge.divide(tiny, Int.MAX_VALUE, mode)
            }
        }
        check(110, 1, BigDecimal("99").divide(BigDecimal("9"), 1, RoundingMode.UNNECESSARY))
        check(110, 1, BigDecimal("99.0").divide(BigDecimal("9"), RoundingMode.UNNECESSARY))
        check(110, 1, BigDecimal("99.0").divide(BigDecimal("9"), BigDecimal.ROUND_UNNECESSARY))
        assertFailsWith<IllegalArgumentException> { BigDecimal.ONE.divide(BigDecimal.ONE, 0, 8) }
        assertFailsWith<IllegalArgumentException> { BigDecimal.ONE.divide(BigDecimal.ONE, 8) }
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divide(BigDecimal.ZERO, 0, RoundingMode.DOWN) }
        check(0, Int.MAX_VALUE, BigDecimal.ZERO.divide(BigDecimal.ONE, Int.MAX_VALUE, RoundingMode.UNNECESSARY))
    }

    @Test fun explicitCallerResultsExceptionsAndSuperEntryRemainSeparate() {
        val marker = BigDecimal("17.00")
        val failure = IllegalStateException("caller")
        class Caller : BigDecimal("99") {
            var fail = false
            override fun divide(divisor: BigDecimal, scale: Int, roundingMode: RoundingMode): BigDecimal =
                if (fail) throw failure else marker
            fun viaSuper(divisor: BigDecimal): BigDecimal = super.divide(divisor, 1, RoundingMode.UNNECESSARY)
        }
        val caller = Caller()
        val base: BigDecimal = caller
        assertSame(marker, base.divide(BigDecimal.ONE, 1, RoundingMode.DOWN))
        caller.fail = true
        assertSame(failure, assertFailsWith<IllegalStateException> {
            base.divide(BigDecimal.ONE, 1, RoundingMode.DOWN)
        })
        check(110, 1, caller.viaSuper(BigDecimal("9")))
    }
}

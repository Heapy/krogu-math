// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DecimalContextProduct580Test {
    // Independent oracle: bounded Long exact product, a single power-of-ten
    // quotient/remainder and comparison r versus divisor-r. It does not use
    // raw convolution, limb shifting, guard digits or sticky extraction.
    private fun expected(n: Long, sign: Int, scale: Long, p: Int, mode: RoundingMode): Pair<Long, Int>? {
        if (scale < Int.MIN_VALUE) return null
        val digits = n.toString().length
        if (scale > Int.MAX_VALUE && digits <= p) return null
        var drop = maxOf(digits - p, 0)
        var divisor = 1L
        repeat(drop) { divisor *= 10 } // drop<=18, since 0<n<4.1e18 and p>=1.
        var q = n / divisor
        val r = n % divisor
        if (r != 0L) {
            if (mode == RoundingMode.UNNECESSARY) return null
            val half = r.compareTo(divisor - r)
            val up = when (mode) {
                RoundingMode.UP -> true
                RoundingMode.DOWN -> false
                RoundingMode.CEILING -> sign > 0
                RoundingMode.FLOOR -> sign < 0
                RoundingMode.HALF_UP -> half >= 0
                RoundingMode.HALF_DOWN -> half > 0
                RoundingMode.HALF_EVEN -> half > 0 || half == 0 && q % 2L != 0L
                RoundingMode.UNNECESSARY -> false
            }
            if (up) q++
            if (q.toString().length > p) { q /= 10; drop++ }
        }
        val resultScale = scale - drop
        if (resultScale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) return null
        return sign * q to resultScale.toInt()
    }

    private fun words(value: Long): IntArray {
        var remaining = value
        val result = mutableListOf<Int>()
        while (remaining != 0L) { result += (remaining and 0x3fffffff).toInt(); remaining = remaining ushr 30 }
        return result.toIntArray()
    }

    private fun raw(a: Long, b: Long, sign: Int, scale: Long, p: Int, mode: RoundingMode) {
        val left = words(a)
        val right = words(b)
        val leftCopy = left.copyOf()
        val rightCopy = right.copyOf()
        val answer = expected(a * b, sign, scale, p, mode)
        val label = "$a * $b / sign=$sign scale=$scale p=$p $mode"
        if (answer == null) assertFailsWith<ArithmeticException>(label) {
            DecimalProduct580.rounded(sign, left, right, scale, MathContext(p, mode))
        } else {
            val actual = DecimalProduct580.rounded(sign, left, right, scale, MathContext(p, mode))
            assertEquals(answer.first.toString(), actual.first.toString(), "$label coefficient")
            assertEquals(answer.second, actual.second, "$label scale")
        }
        assertContentEquals(leftCopy, left, "$label left input isolation")
        assertContentEquals(rightCopy, right, "$label right input isolation")
    }

    @Test fun rawProductAgainstIndependentLongOracleAllSignsAndModes() {
        val pairs = listOf(1L to 1L, 125L to 1L, 135L to 1L,
            1249L to 1L, 1250L to 1L, 1251L to 1L, 999L to 1L,
            999999999L to 1L, 3999999999L to 5L,
            1000000000L to 1000000000L, 1000000001L to 1000000001L,
            1073741825L to 1073741825L, 1999999999L to 1999999999L,
            2000000001L to 2000000001L)
        // Largest product=4000000004000000001<4.1e18<Long.MAX_VALUE.
        // Large pairs have two binary and two decimal input cells and produce
        // multiword outputs. p9/p10/p19 cross decimal discard and binary boundaries.
        // 125/135 are true ties with even/odd retained quotient; 1249/1250/1251
        // bracket a midpoint with both zero/nonzero lower tails. 3999999999*5
        // at p10 increments1999999999 across the low decimal cell boundary.
        // 1999999999^2 multiplies two BASE-1 low cells, exercising row carries.
        for ((a, b) in pairs) for (sign in listOf(-1, 1))
            for (p in listOf(1, 2, 8, 9, 10, 19, Int.MAX_VALUE))
                for (mode in RoundingMode.entries) raw(a, b, sign, 0L, p, mode)
    }

    @Test fun rawScaleRescueCarryAndRequiredCohort() {
        for (scale in listOf(Int.MIN_VALUE.toLong() - 1, Int.MIN_VALUE.toLong(),
            Int.MIN_VALUE.toLong() + 3, Int.MAX_VALUE.toLong(),
            Int.MAX_VALUE.toLong() + 1, Int.MAX_VALUE.toLong() + 2)) {
            for ((a, b, p) in listOf(Triple(95L, 1L, 1), Triple(999L, 1L, 2),
                Triple(1000L, 1L, 1), Triple(1000L, 1L, 4)))
                for (sign in listOf(-1, 1)) for (mode in RoundingMode.entries)
                    raw(a, b, sign, scale, p, mode)
        }
        // No optional trailing-zero stripping: at p3, 10^18 ->100 @ -16.
        val kept = DecimalProduct580.rounded(1, words(1000000000L), words(1000000000L),
            0L, MathContext(3, RoundingMode.UNNECESSARY))
        assertEquals(BigInteger("100"), kept.first)
        assertEquals(-16, kept.second)
        // Exact HIGH product with D<=p retains566's error despite other cohorts.
        assertFailsWith<ArithmeticException> {
            DecimalProduct580.rounded(1, words(10), words(1), Int.MAX_VALUE.toLong() + 1,
                MathContext(2, RoundingMode.DOWN))
        }
    }

    @Test fun symbolicFullRangeSelectionIncludesUncertainEdge() {
        assertFalse(needsRawDecimalProduct580(0, Int.MAX_VALUE))
        assertFalse(needsRawDecimalProduct580(Int.MAX_VALUE, 0))
        assertFalse(needsRawDecimalProduct580(1073741823, 1073741824)) // sum=L
        assertTrue(needsRawDecimalProduct580(1073741824, 1073741824)) // sum=L+1
        assertTrue(needsRawDecimalProduct580(1, Int.MAX_VALUE)) // same uncertain edge
        assertTrue(needsRawDecimalProduct580(1073741825, 1073741825)) // actual audit witness bits
        assertTrue(needsRawDecimalProduct580(Int.MAX_VALUE, Int.MAX_VALUE))
    }

    @Test fun publicSmallProductsRetainScaleZeroAndUnlimitedPolicies() {
        class Truthful(value: BigInteger, scale: Int) : BigDecimal(value, scale) {
            override fun multiply(multiplicand: BigDecimal, mc: MathContext) = super.multiply(multiplicand, mc)
            fun inherited(multiplicand: BigDecimal, mc: MathContext) = super.multiply(multiplicand, mc)
        }
        // Bounded public controls retain the materialized path; direct raw calls
        // above exercise the otherwise giant fallback without a fake size cap.
        for ((a, b) in listOf(95L to 1L, 1250L to 1L, 99L to 99L,
            1000000001L to 1000000001L)) {
            for ((sa, sb) in listOf(0 to 0, 8 to -8, Int.MAX_VALUE to 1,
                Int.MIN_VALUE to -1, Int.MIN_VALUE to 0)) {
                for (ls in listOf(-1, 1)) for (rs in listOf(-1, 1)) {
                    val x = BigDecimal(BigInteger.valueOf(ls * a), sa)
                    val truthful = Truthful(BigInteger.valueOf(ls * a), sa)
                    val y = BigDecimal(BigInteger.valueOf(rs * b), sb)
                    for (mode in RoundingMode.entries) {
                        val mc = MathContext(2, mode)
                        val answer = expected(a * b, ls * rs, sa.toLong() + sb, 2, mode)
                        for (call in listOf<() -> BigDecimal>({ x.multiply(y, mc) },
                            { truthful.multiply(y, mc) }, { truthful.inherited(y, mc) })) {
                            if (answer == null) assertFailsWith<ArithmeticException> { call() }
                            else {
                                val actual = call()
                                assertEquals(answer.first.toString(), actual.unscaledValue().toString())
                                assertEquals(answer.second, actual.scale())
                            }
                        }
                    }
                }
            }
        }
        assertEquals(BigDecimal("10.00"), BigDecimal("2.0").multiply(BigDecimal("5.0"), MathContext.UNLIMITED))
        val zero = BigDecimal(BigInteger.ZERO, Int.MAX_VALUE)
        val ten = BigDecimal(BigInteger.TEN, 1)
        assertEquals(BigDecimal(BigInteger.ZERO, Int.MAX_VALUE), zero.multiply(ten, MathContext(1)))
        assertFailsWith<ArithmeticException> { ten.multiply(zero, MathContext(1)) }
    }

}

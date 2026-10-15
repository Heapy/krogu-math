// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DecimalSum585Test {
    private fun words(value: Long): IntArray {
        var n = value
        val result = mutableListOf<Int>()
        while (n != 0L) { result += (n and 0x3fffffff).toInt(); n = n ushr 30 }
        return result.toIntArray()
    }

    // Independent bounded signed rational oracle, using quotient/remainder and
    // r versus divisor-r, not chunk alignment or guard/sticky extraction.
    private fun expected(n: Long, scale: Int, p: Int, mode: RoundingMode): Pair<Long, Int>? {
        if (n == 0L || p == 0) return n to scale
        val sign = if (n < 0) -1 else 1
        val magnitude = if (n < 0) -n else n
        val drop = maxOf(0, magnitude.toString().length - p)
        var divisor = 1L
        repeat(drop) { divisor *= 10L }
        var q = magnitude / divisor
        val r = magnitude % divisor
        if (r != 0L) {
            if (mode == RoundingMode.UNNECESSARY) return null
            val half = r.compareTo(divisor - r)
            val increment = when (mode) {
                RoundingMode.UP -> true
                RoundingMode.DOWN -> false
                RoundingMode.CEILING -> sign > 0
                RoundingMode.FLOOR -> sign < 0
                RoundingMode.HALF_UP -> half >= 0
                RoundingMode.HALF_DOWN -> half > 0
                RoundingMode.HALF_EVEN -> half > 0 || half == 0 && q % 2 != 0L
                RoundingMode.UNNECESSARY -> false
            }
            if (increment) q++
        }
        var resultScale = scale.toLong() - drop
        if (q.toString().length > p) { q /= 10; resultScale-- }
        if (resultScale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) return null
        return sign * q to resultScale.toInt()
    }

    private fun checkLong(a: Long, b: Long, sa: Int, sb: Int, p: Int, mode: RoundingMode) {
        val scale = maxOf(sa, sb)
        var x = a
        var y = b
        repeat((scale.toLong() - sa).toInt()) { x *= 10 }
        repeat((scale.toLong() - sb).toInt()) { y *= 10 }
        val answer = expected(x + y, scale, p, mode)
        val left = words(if (a < 0) -a else a)
        val right = words(if (b < 0) -b else b)
        val lc = left.copyOf()
        val rc = right.copyOf()
        val label = "$a@$sa + $b@$sb p$p/$mode"
        val actual = DecimalSum585.sum(a.compareTo(0), left, sa, b.compareTo(0), right, sb, MathContext(p, mode))
        if (answer == null) assertNull(actual, label)
        else {
            assertEquals(answer.first.toString(), actual?.first?.toString(), "$label coefficient")
            assertEquals(answer.second, actual?.second, "$label scale")
        }
        assertContentEquals(lc, left, "$label left isolation")
        assertContentEquals(rc, right, "$label right isolation")
    }

    @Test fun rawSignedAlignmentAgainstBoundedLongRationalOracle() {
        val pairs = listOf(0L to 0L, 0L to 125L, 125L to 0L, 1L to 1L,
            124L to 1L, 134L to 1L, 1248L to 1L, 1249L to 1L, 1250L to 1L,
            998L to 1L, 999999999L to 1L, 1000000001L to 999999999L,
            1000000000000000000L to 1L)
        for ((a, b) in pairs) for (asign in listOf(-1, 1)) for (bsign in listOf(-1, 1)) {
            // Large pair stays unshifted; all other magnitudes <=1000000001,
            // alignment raises<=8, so sum magnitude<2.1e17. Large sum<=1e18+1.
            val scales = if (a >= 1000000000000000000L) listOf(0 to 0)
                else listOf(0 to 0, -1 to 0, 0 to -1, -8 to 0, 0 to -8)
            for ((sa, sb) in scales) for (p in listOf(0, 1, 2, 8, 9, 10, 18, 19, Int.MAX_VALUE))
                for (mode in RoundingMode.entries) checkLong(asign * a, bsign * b, sa, sb, p, mode)
        }
        // Every partial-chunk discard t=0..8, with a distant nonzero sticky.
        for (p in 1..19) for (mode in RoundingMode.entries)
            checkLong(1000000000000000000L, 1L, 0, 0, p, mode)
    }

    @Test fun rawVirtualGapBorrowStickyAndRoundingCarryAcrossPage() {
        // Genuine tiny inputs, NOT a fake public size cap. The helper is called
        // directly: 10^73729 +/-1 crosses its actual8192-cell page boundary.
        // It converts two one-word inputs and publishes only nine-digit answers.
        val input = words(1)
        val before = input.copyOf()
        for (sign in listOf(-1, 1)) for (delta in listOf(-1, 1)) for (mode in RoundingMode.entries) {
            val actual = DecimalSum585.sum(sign, input, -73729, sign * delta, input, 0, MathContext(9, mode))
            if (mode == RoundingMode.UNNECESSARY) { assertNull(actual); continue }
            val up = when (mode) {
                RoundingMode.UP -> true
                RoundingMode.DOWN -> false
                RoundingMode.CEILING -> sign > 0
                RoundingMode.FLOOR -> sign < 0
                RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN -> delta < 0
                RoundingMode.UNNECESSARY -> error("handled")
            }
            val coefficient = if (delta < 0) {
                if (up) "100000000" else "999999999"
            } else if (up) "100000001" else "100000000"
            val scale = if (delta < 0 && !up) -73720 else -73721
            assertEquals((if (sign < 0) "-" else "") + coefficient, actual?.first?.toString())
            assertEquals(scale, actual?.second)
        }
        assertContentEquals(before, input, "aliased source remains read-only")
    }

    @Test fun rawScaleEndpointsZeroAndExactPreferredCohorts() {
        for ((sa, sb) in listOf(Int.MIN_VALUE to Int.MIN_VALUE,
            Int.MIN_VALUE to Int.MIN_VALUE + 3, Int.MAX_VALUE to Int.MAX_VALUE,
            Int.MAX_VALUE - 3 to Int.MAX_VALUE)) {
            for ((a, b) in listOf(95L to 1L, 999L to 1L, 1000L to -1L, 125L to -125L))
                for (p in listOf(0, 1, 2, Int.MAX_VALUE)) for (mode in RoundingMode.entries)
                    checkLong(a, b, sa, sb, p, mode)
        }
        val exact = DecimalSum585.sum(1, words(1), -18, -1, words(1), 0, null)
        assertEquals("999999999999999999", exact?.first?.toString())
        assertEquals(0, exact?.second)
        val cohort = DecimalSum585.sum(1, words(999), 0, 1, words(1), 0, MathContext(3, RoundingMode.UNNECESSARY))
        assertEquals("100", cohort?.first?.toString())
        assertEquals(-1, cohort?.second)
        val zero = DecimalSum585.sum(1, words(1), Int.MAX_VALUE, -1, words(1), Int.MAX_VALUE, MathContext(1))
        assertEquals(BigInteger.ZERO, zero?.first)
        assertEquals(Int.MAX_VALUE, zero?.second)
    }

    @Test fun symbolicSafeAndNoRescueGatesAndBoundedPublicControls() {
        val limit = Int.MAX_VALUE
        assertTrue(materializedDecimalSum585(1, -1, limit, limit, 0, 0))
        assertTrue(materializedDecimalSum585(1, 0, limit, 0, 0, 0))
        assertFalse(materializedDecimalSum585(1, 1, limit, limit, 0, 0))
        assertTrue(materializedDecimalSum585(1, 1, limit - 1, limit - 1, 0, 0))
        assertFalse(materializedDecimalSum585(1, -1, 1, 1, Int.MIN_VALUE, Int.MAX_VALUE))
        assertTrue(certifiedDecimalSumSpan585(646456993, 1, -1, 0, 0))
        assertFalse(certifiedDecimalSumSpan585(646456993, 1, -2, 0, 0))
        assertFalse(certifiedDecimalSumSpan585(646456993, 1, -2, 0, Int.MAX_VALUE))
        assertTrue(certifiedDecimalSumSpan585(646456993, 1, -2, 0, 1))
        assertFalse(certifiedDecimalSumSpan585(1, 1, Int.MIN_VALUE, Int.MAX_VALUE, 1))
        assertFalse(certifiedDecimalSumSpan585(1, 1, Int.MIN_VALUE, Int.MAX_VALUE, Int.MAX_VALUE))
        assertTrue(certifiedDecimalSumSpan585(1, 1, 0, 0, Int.MAX_VALUE))
        val x = BigDecimal("1E+18")
        assertEquals(BigDecimal("999999999999999999"), x.add(BigDecimal.ONE.negate()))
        assertEquals(BigDecimal("999999999999999999"), x.subtract(BigDecimal.ONE))
        assertEquals(BigDecimal("1.00E+18"), x.add(BigDecimal.ONE, MathContext(3, RoundingMode.DOWN)))
        assertEquals(BigDecimal("1.00E+18"), x.subtract(BigDecimal.ONE, MathContext(3, RoundingMode.UP)))
        assertEquals(BigDecimal("2.00"), BigDecimal("1.00").add(BigDecimal("1.0"), MathContext.UNLIMITED))
    }
}

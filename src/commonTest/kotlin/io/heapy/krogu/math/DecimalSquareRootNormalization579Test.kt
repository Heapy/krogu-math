// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DecimalSquareRootNormalization579Test {
    // Independent bounded oracle: division-based Long bisection, then compare
    // four times the integer radicand with the odd midpoint's square. No
    // decimal-pair extraction or restoring recurrence is used in this oracle.
    private fun floorRoot(n: Long): Long {
        var low = 0L
        var high = 2_000_000_000L // Every test N < 1.3e18 < high^2.
        while (low + 1 < high) {
            val middle = (low + high) / 2
            if (middle <= n / middle) low = middle else high = middle
        }
        return low
    }

    @Test fun rawKernelAgainstBoundedIndependentIntegerOracleAllModes() {
        val coefficients = listOf(1L, 2L, 3L, 8L, 99L, 9999L,
            100000001L, 999999999L, 1000000000L, 1000000001L)
        for (coefficient in coefficients) for (shift in 1..6)
            checkAgainstLongOracle(coefficient, shift, 4)

        // Dedicated h=1 cases only (S=1,p=10,t=1 for the first three).
        // a=1111111111, r=1111111109/1111111119: below/above a, respectively.
        // Both root and remainder use two base-1e9 cells; root also exceeds 2^30.
        for (coefficient in listOf(123456790209876543L, 123456790209876544L,
            123456792076543210L, 99999999999999999L)) {
            checkAgainstLongOracle(coefficient, 1, 1)
        }
        // Third case: a=1111111119,r=1999999939. Its final subtraction is
        // 22000000000 - 20000000061, requiring a low-cell borrow.
        // Fourth case: N=10^18-10, floor root=999999999. Increment crosses
        // the base-1e9 cell boundary before the exact precision-carry division.
    }

    private fun checkAgainstLongOracle(coefficient: Long, shift: Int, initialScale: Int) {
        var power = 1L
        repeat(shift) { power *= 10 }
        val n = coefficient * power
        // N<1.3e18 implies a<1.2e9. Hence a*a, (a+1)^2, 4N and
        // (2a+1)^2<6e18 all fit Long; the bisection itself uses only division.
        val p = (coefficient.toString().length + shift + 1) / 2
        val a = floorRoot(n)
        assertTrue(a * a <= n && (a + 1) * (a + 1) > n)
        val exact = a * a == n
        val above = 4 * n > (2 * a + 1) * (2 * a + 1)
        for (mode in RoundingMode.entries) {
            val label = "$coefficient * 10^$shift / $mode"
            if (!exact && mode == RoundingMode.UNNECESSARY) {
                assertFailsWith<ArithmeticException>(label) {
                    DecimalSquareRoot579.normalized(BigInteger.valueOf(coefficient),
                        shift.toLong(), p, initialScale.toLong(), 0, mode)
                }
                continue
            }
            val up = !exact && when (mode) {
                RoundingMode.UP, RoundingMode.CEILING -> true
                RoundingMode.DOWN, RoundingMode.FLOOR, RoundingMode.UNNECESSARY -> false
                else -> above
            }
            var expected = a + if (up) 1 else 0
            var scale = initialScale
            if (expected.toString().length > p) { expected /= 10; scale-- }
            // Only exact roots choose a shorter preferred cohort. Inexact
            // p-digit rounding keeps its zero tail after the single carry.
            while (exact && scale > 0 && expected % 10L == 0L) { expected /= 10; scale-- }
            val actual = DecimalSquareRoot579.normalized(BigInteger.valueOf(coefficient),
                shift.toLong(), p, initialScale.toLong(), 0, mode)
            assertEquals(expected.toString(), actual.first.toString(), "$label coefficient")
            assertEquals(scale, actual.second, "$label scale")
        }
    }

    @Test fun rawCarryPreferredStopAndLongScaleRescue() {
        // sqrt(9990) is between 99.5 and 100: normalize the all-nine carry,
        // retaining two significant digits rather than stripping to one.
        for (mode in listOf(RoundingMode.UP, RoundingMode.CEILING,
            RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN)) {
            val result = DecimalSquareRoot579.normalized(BigInteger("999"), 1, 2, 5L, 3, mode)
            assertEquals(BigInteger.TEN, result.first)
            assertEquals(4, result.second)
        }
        // Exact kernel inputs exercise cohort mechanics independently of the
        // earlier public finite-root shortcut: strip only to the preferred stop.
        val stopped = DecimalSquareRoot579.normalized(BigInteger.ONE, 10, 6, 8L, 6,
            RoundingMode.UNNECESSARY)
        assertEquals(BigInteger("1000"), stopped.first)
        assertEquals(6, stopped.second)
        // A provisional Long scale must not be rejected before stripping.
        val rescued = DecimalSquareRoot579.normalized(BigInteger.ONE, 20, 11,
            Int.MAX_VALUE.toLong() + 5, Int.MAX_VALUE, RoundingMode.DOWN)
        assertEquals(BigInteger("100000"), rescued.first)
        assertEquals(Int.MAX_VALUE, rescued.second)
        assertFailsWith<ArithmeticException> {
            DecimalSquareRoot579.normalized(BigInteger("2"), 20, 11,
                Int.MAX_VALUE.toLong() + 5, Int.MAX_VALUE, RoundingMode.DOWN)
        }
    }

    @Test fun eligibilityIsAChoiceAndPublicScaleParityRegressions() {
        assertTrue(DecimalSquareRoot579.canMaterialize(1, 10, 5))
        assertFalse(DecimalSquareRoot579.canMaterialize(1, 799999998L, 399999999L))
        assertFalse(DecimalSquareRoot579.canMaterialize(1, 10, Int.MAX_VALUE.toLong() + 1))
        // sqrt(2) at seven digits is between 1.414213 and 1.414214;
        // 1.4142135^2 < 2 establishes nearest rounding up independently.
        for ((inputScale, outputScale) in listOf(0 to 6, 2 to 7, -2 to 5)) {
            for (mode in RoundingMode.entries) {
                val input = BigDecimal(BigInteger.TWO, inputScale)
                if (mode == RoundingMode.UNNECESSARY) {
                    assertFailsWith<ArithmeticException> { input.sqrt(MathContext(7, mode)) }
                } else {
                    val low = mode == RoundingMode.DOWN || mode == RoundingMode.FLOOR
                    val result = input.sqrt(MathContext(7, mode))
                    assertEquals(if (low) "1414213" else "1414214", result.unscaledValue().toString())
                    assertEquals(outputScale, result.scale())
                }
            }
        }
        for ((c, s, expected, t) in listOf(
            listOf("2", "1", "4472136", "7"),
            listOf("2", "-1", "4472136", "6"))) {
            val result = BigDecimal(BigInteger(c), s.toInt()).sqrt(MathContext(7, RoundingMode.HALF_EVEN))
            assertEquals(expected, result.unscaledValue().toString())
            assertEquals(t.toInt(), result.scale())
        }
        // The pre-existing exact path must still avoid precision-sized padding.
        assertEquals(BigDecimal("2"), BigDecimal("4").sqrt(MathContext(Int.MAX_VALUE, RoundingMode.DOWN)))
    }
}

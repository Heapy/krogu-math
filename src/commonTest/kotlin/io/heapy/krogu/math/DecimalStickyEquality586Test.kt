// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalStickyEquality586Test {
    @Test fun DecimalStickyEquality586SignedModesAndSubtractDelegation() {
        // Scale distance 10,000 bypasses the close-operand branch. The tail is
        // exactly +/-0.5, while 1,250,000 is halfway between the p2 candidates.
        val magnitude = BigInteger("5" + "0".repeat(9999))
        for (largeSign in listOf(-1, 1)) for (tailSign in listOf(-1, 1)) {
            val large = BigDecimal(1_250_000 * largeSign)
            val tail = BigDecimal(if (tailSign > 0) magnitude else magnitude.negate(), 10000)
            val before = tail.unscaledValue().toByteArray()
            for (mode in RoundingMode.entries) {
                val context = MathContext(2, mode)
                val label = "large=$largeSign tail=$tailSign mode=$mode"
                if (mode == RoundingMode.UNNECESSARY) {
                    assertFailsWith<ArithmeticException>(label) { large.add(tail, context) }
                    assertFailsWith<ArithmeticException>(label) { large.subtract(tail.negate(), context) }
                } else {
                    // All exact sums remain between the two candidates. The
                    // nonzero half-unit tail determines the side of the tie.
                    val upper = when (mode) {
                        RoundingMode.UP -> true
                        RoundingMode.DOWN -> false
                        RoundingMode.CEILING -> largeSign > 0
                        RoundingMode.FLOOR -> largeSign < 0
                        RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN ->
                            largeSign == tailSign
                        RoundingMode.UNNECESSARY -> error("handled above")
                    }
                    val expected = largeSign * if (upper) 13 else 12
                    checkPair(expected, large.add(tail, context), label)
                    checkPair(expected, large.subtract(tail.negate(), context), label)
                }
            }
            assertContentEquals(before, tail.unscaledValue().toByteArray())
            assertEquals(10000, tail.scale())
            assertEquals((1_250_000 * largeSign).toString(), large.unscaledValue().toString())
            assertEquals(0, large.scale())
        }
    }

    @Test fun DecimalStickyEquality586SelectionBoundaryAndOperandOrder() {
        // 9,999 retains the close route; 10,000 and 10,001 reach stickyAt's
        // equality case. No production coefficient limit is changed for tests.
        val context = MathContext(2, RoundingMode.HALF_EVEN)
        for (digits in listOf(9999, 10000, 10001)) {
            val magnitude = BigInteger("5" + "0".repeat(digits - 1))
            for (largeSign in listOf(-1, 1)) for (tailSign in listOf(-1, 1)) {
                val large = BigDecimal(1_250_000 * largeSign)
                val tail = BigDecimal(if (tailSign > 0) magnitude else magnitude.negate(), digits)
                val expected = largeSign * if (largeSign == tailSign) 13 else 12
                val label = "digits=$digits large=$largeSign tail=$tailSign"
                checkPair(expected, large.add(tail, context), label)
                checkPair(expected, tail.add(large, context), label)
            }
        }
    }

    private fun checkPair(expected: Int, actual: BigDecimal, label: String) {
        assertEquals(expected.toString(), actual.unscaledValue().toString(), label)
        assertEquals(-5, actual.scale(), label)
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DoubleZeroContext569Test {
    // Both signed-zero null entries move to Validation578StandaloneTest's exact JVM ledger.

    @Test fun validContextsKeepZeroCoefficientAndScaleZero() {
        var count = 0
        for (value in listOf(0.0, -0.0)) {
            val exact = BigDecimal(value)
            assertEquals(BigInteger.ZERO, exact.unscaledValue())
            assertEquals(0, exact.scale())
            for (precision in listOf(0, 1, 7, Int.MAX_VALUE)) for (mode in RoundingMode.entries) {
                val context = MathContext(precision, mode)
                val result = BigDecimal(value, context)
                val label = "bits=${value.toRawBits()} p$precision/$mode"
                assertEquals(BigInteger.ZERO, result.unscaledValue(), "$label coefficient")
                assertEquals(0, result.scale(), "$label scale")
                count++
            }
        }
        assertEquals(64, count)
    }

    @Test fun finiteNonzeroNullAndValidControls() {
        // Five finite null entries are retained at the Java constructor boundary.
        // Exact binary fractions have independently known decimal coefficients/scales.
        for ((value, fields) in listOf(1.0 to ("1" to 0), -1.0 to ("-1" to 0),
            0.5 to ("5" to 1), -1.25 to ("-125" to 2))) {
            for (precision in listOf(0, 7, Int.MAX_VALUE)) for (mode in RoundingMode.entries) {
                val result = BigDecimal(value, MathContext(precision, mode))
                assertEquals(BigInteger(fields.first), result.unscaledValue(), "$value p$precision/$mode")
                assertEquals(fields.second, result.scale(), "$value p$precision/$mode")
            }
        }
        for (mode in RoundingMode.entries) {
            val context = MathContext(2, mode)
            if (mode == RoundingMode.UNNECESSARY) {
                assertFailsWith<ArithmeticException> { BigDecimal(1.25, context) }
            } else {
                val coefficient = when (mode) {
                    RoundingMode.UP, RoundingMode.CEILING, RoundingMode.HALF_UP -> "13"
                    else -> "12"
                }
                val result = BigDecimal(1.25, context)
                assertEquals(BigInteger(coefficient), result.unscaledValue(), mode.name)
                assertEquals(1, result.scale(), mode.name)
            }
        }
    }

    @Test fun retainedNonfiniteValidationStaging() {
        // Historical staging controls only; these competing-invalid inputs do not set a global order.
        for (value in listOf(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)) {
            // The three nonfinite/null competing-invalid observations are JVM diagnostics.
            assertFailsWith<NumberFormatException> { BigDecimal(value) }
            for (mode in RoundingMode.entries)
                assertFailsWith<NumberFormatException> { BigDecimal(value, MathContext(7, mode)) }
        }
    }
}

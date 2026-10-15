// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ScaledDivisionMidpoint584Test {
    // Primitive rational rounding is independent of the production complement comparison.
    // All magnitudes here are below 2^62, and doubled remainders below 2^62.
    private fun expected(numerator: Long, denominator: Long, mode: RoundingMode): Long {
        val quotient = numerator / denominator
        val remainder = numerator % denominator
        if (remainder == 0L) return quotient
        val sign = if ((numerator < 0) == (denominator < 0)) 1L else -1L
        val twice = (if (remainder < 0) -remainder else remainder) * 2
        val magnitude = if (denominator < 0) -denominator else denominator
        val up = when (mode) {
            RoundingMode.DOWN -> false
            RoundingMode.UP -> true
            RoundingMode.CEILING -> sign > 0
            RoundingMode.FLOOR -> sign < 0
            RoundingMode.HALF_UP -> twice >= magnitude
            RoundingMode.HALF_DOWN -> twice > magnitude
            RoundingMode.HALF_EVEN -> twice > magnitude || twice == magnitude && quotient % 2 != 0L
            RoundingMode.UNNECESSARY -> error("Inexact model input")
        }
        return quotient + if (up) sign else 0L
    }

    private fun checkCase(numerator: Long, denominator: Long, sourceScale: Int,
        divisorScale: Int, target: Int, modelNumerator: Long, modelDenominator: Long) {
        val source = BigDecimal.valueOf(numerator, sourceScale)
        val divisor = BigDecimal.valueOf(denominator, divisorScale)
        for (mode in RoundingMode.entries) {
            val label = "$numerator@$sourceScale / $denominator@$divisorScale -> $target $mode"
            if (mode == RoundingMode.UNNECESSARY && modelNumerator % modelDenominator != 0L) {
                assertFailsWith<ArithmeticException>(label) { source.divide(divisor, target, mode) }
            } else {
                val coefficient = expected(modelNumerator, modelDenominator, mode)
                val result = source.divide(divisor, target, mode)
                assertEquals(coefficient.toString(), result.unscaledValue().toString(), "$label coefficient")
                assertEquals(target, result.scale(), "$label scale")
                if (target == 0) assertEquals(coefficient.toString(), result.toString(), "$label text")
            }
        }
        assertEquals(numerator.toString(), source.unscaledValue().toString())
        assertEquals(sourceScale, source.scale())
        assertEquals(denominator.toString(), divisor.unscaledValue().toString())
        assertEquals(divisorScale, divisor.scale())
    }

    @Test fun signsTiesParityAndWordBoundariesAgainstPrimitiveRationals() {
        val denominators = listOf(2L, 3L, (1L shl 30) - 1, 1L shl 30,
            (1L shl 30) + 1, (1L shl 60) - 1, 1L shl 60, (1L shl 60) + 1)
        for (denominator in denominators) {
            val remainders = listOf(0L, 1L, denominator / 2 - 1, denominator / 2,
                denominator / 2 + 1, denominator - 1).filter { it in 0L until denominator }.distinct()
            for (whole in 0L..2L) for (remainder in remainders)
                for (leftSign in listOf(-1L, 1L)) for (rightSign in listOf(-1L, 1L)) {
                    val numerator = leftSign * (whole * denominator + remainder)
                    val divisor = rightSign * denominator
                    checkCase(numerator, divisor, 0, 0, 0, numerator, divisor)
                }
        }
    }

    @Test fun compensatedEndpointScalesKeepTheRequestedCohort() {
        val scales = listOf(Triple(0, 0, 0), Triple(Int.MIN_VALUE, Int.MIN_VALUE, 0),
            Triple(Int.MAX_VALUE, Int.MAX_VALUE, 0), Triple(Int.MAX_VALUE, 0, Int.MAX_VALUE),
            Triple(Int.MIN_VALUE, 0, Int.MIN_VALUE), Triple(Int.MAX_VALUE, 1, Int.MAX_VALUE),
            Triple(Int.MIN_VALUE + 1, 0, Int.MIN_VALUE), Triple(0, Int.MIN_VALUE, Int.MAX_VALUE),
            Triple(-1, Int.MAX_VALUE, Int.MIN_VALUE))
        for ((a, b) in listOf(0L to 7L, 1L to 2L, 9L to 6L, 19L to 7L, 25L to 10L))
            for ((sourceScale, divisorScale, target) in scales)
                for (leftSign in listOf(-1L, 1L)) for (rightSign in listOf(-1L, 1L)) {
                    val shift = target.toLong() + divisorScale - sourceScale
                    check(shift in -1L..1L)
                    val numerator = leftSign * a
                    val denominator = rightSign * b
                    val modelNumerator = if (shift == 1L) numerator * 10 else numerator
                    val modelDenominator = if (shift == -1L) denominator * 10 else denominator
                    checkCase(numerator, denominator, sourceScale, divisorScale, target,
                        modelNumerator, modelDenominator)
                }
    }
}

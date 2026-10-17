// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DecimalExactLowResidual590Test {
    private data class Row(val source: Long, val divisor: Long, val coefficient: Long, val scale: Int)
    // Independent terminating rational literals: A/B = C*10^(-1-offset).
    // Multiplying by 10^(2147483649) makes each selected scale MIN+offset.
    private val rows = listOf(
        Row(1, 2, 5, Int.MIN_VALUE),
        Row(3, 2, 15, Int.MIN_VALUE),
        Row(7, 4, 175, Int.MIN_VALUE + 1),
        Row(1, 20, 5, Int.MIN_VALUE + 1),
        Row(10, 4, 25, Int.MIN_VALUE),
        Row(25, 8, 3125, Int.MIN_VALUE + 2),
        Row(1234567891, 2, 6172839455, Int.MIN_VALUE),
    )

    private fun assertDecimal(coefficient: Long, scale: Int, actual: BigDecimal, label: String) {
        assertEquals(coefficient.toString(), actual.unscaledValue().toString(), "$label coefficient")
        assertEquals(scale, actual.scale(), "$label scale")
    }

    @Test fun exactCertificateChecksSignsNumericCohortsAndWrongCandidates() {
        for (row in rows) for (sourceSign in listOf(-1, 1)) for (divisorSign in listOf(-1, 1)) {
            val sign = sourceSign * divisorSign
            val source = BigInteger.valueOf(sourceSign * row.source)
            val divisor = BigInteger.valueOf(divisorSign * row.divisor)
            val originalSource = source.toString()
            val originalDivisor = divisor.toString()
            // A padded, numerically equal candidate is a valid zero-residual
            // certificate input; this is not a preferred-cohort selection rule.
            for (padding in 0..1) {
                val magnitude = row.coefficient * if (padding == 0) 1L else 10L
                val scale = row.scale + padding
                val candidate = BigInteger.valueOf(sign * magnitude)
                val label = "$row sourceSign=$sourceSign divisorSign=$divisorSign padding=$padding"
                val actual = assertNotNull(source.certifiedExactLowResidual590(divisor,
                    Int.MIN_VALUE, 1, candidate, scale), label)
                assertEquals(BigInteger.ZERO, actual.first, label)
                assertEquals(scale + 1, actual.second, label)
                assertNull(source.certifiedExactLowResidual590(divisor, Int.MIN_VALUE, 1,
                    BigInteger.valueOf(sign * (magnitude + 1)), scale), "$label wrong coefficient")
                assertNull(source.certifiedExactLowResidual590(divisor, Int.MIN_VALUE, 1,
                    candidate, scale + 1), "$label wrong scale")
                assertNull(source.certifiedExactLowResidual590(divisor, Int.MIN_VALUE, 1,
                    candidate.negate(), scale), "$label wrong sign")
                assertEquals((sign * magnitude).toString(), candidate.toString(), label)
            }
            assertEquals(originalSource, source.toString())
            assertEquals(originalDivisor, divisor.toString())
        }
    }

    @Test fun certificateDeclinesOutsideItsExactSupportedResultClass() {
        val one = BigInteger.ONE
        val two = BigInteger.TWO
        val five = BigInteger.valueOf(5)
        assertNull(one.certifiedExactLowResidual590(two, 0, 0, five, 0), "in-range preference")
        assertNull(one.certifiedExactLowResidual590(two, Int.MAX_VALUE, Int.MIN_VALUE,
            five, Int.MIN_VALUE), "HIGH preference")
        assertNull(BigInteger.ZERO.certifiedExactLowResidual590(two, Int.MIN_VALUE, 1,
            BigInteger.ZERO, Int.MIN_VALUE), "zero source")
        assertNull(one.certifiedExactLowResidual590(BigInteger.ZERO, Int.MIN_VALUE, 1,
            five, Int.MIN_VALUE), "zero divisor")
        assertNull(one.certifiedExactLowResidual590(two, Int.MIN_VALUE, 1,
            BigInteger.ZERO, Int.MIN_VALUE), "zero candidate")
        assertNull(one.certifiedExactLowResidual590(two, Int.MIN_VALUE, 1,
            five, Int.MAX_VALUE), "residual scale HIGH")
        assertNull(one.certifiedExactLowResidual590(BigInteger.valueOf(3), Int.MIN_VALUE, 1,
            one, Int.MIN_VALUE), "nonterminating ratio")
        // A legal alternative 10@MIN is not this certificate's natural-scale
        // domain; no LOW clamp/rescue policy is added.
        assertNull(one.certifiedExactLowResidual590(one, Int.MIN_VALUE, 1,
            BigInteger.TEN, Int.MIN_VALUE), "natural exact scale below MIN")
        // Real outer-bridge declines with tiny inputs: the old safe predicate
        // is false from the scale distance, then the new LOW certificate runs.
        assertNull(one.certifiedDecimalResidual583(two, Int.MIN_VALUE, Int.MAX_VALUE,
            one, Int.MIN_VALUE), "outside LOW natural scale")
        assertNull(one.certifiedDecimalResidual583(BigInteger.valueOf(3), Int.MIN_VALUE, Int.MAX_VALUE,
            one, Int.MIN_VALUE), "outside LOW nonterminating")
    }

    @Test fun publicEndpointQuotientPairsAndRemaindersKeepExactLiteralCohorts() {
        for (row in rows.take(3)) for (sourceSign in listOf(-1, 1)) for (divisorSign in listOf(-1, 1)) {
            val source = BigDecimal(BigInteger.valueOf(sourceSign * row.source), Int.MIN_VALUE)
            val divisor = BigDecimal(BigInteger.valueOf(divisorSign * row.divisor), 1)
            val coefficient = sourceSign * divisorSign * row.coefficient
            val residualScale = row.scale + 1
            val label = "$row sourceSign=$sourceSign divisorSign=$divisorSign"
            assertDecimal(coefficient, row.scale, source.divideToIntegralValue(divisor), label)
            val unlimitedPair = source.divideAndRemainder(divisor)
            assertEquals(2, unlimitedPair.size, label)
            assertDecimal(coefficient, row.scale, unlimitedPair[0], label)
            assertDecimal(0, residualScale, unlimitedPair[1], label)
            assertDecimal(0, residualScale, source.remainder(divisor), label)
            for (mode in RoundingMode.entries) {
                val context = MathContext(Int.MAX_VALUE, mode)
                val modeLabel = "$label mode=$mode"
                assertDecimal(coefficient, row.scale, source.divideToIntegralValue(divisor, context), modeLabel)
                val pair = source.divideAndRemainder(divisor, context)
                assertEquals(2, pair.size, modeLabel)
                assertDecimal(coefficient, row.scale, pair[0], modeLabel)
                assertDecimal(0, residualScale, pair[1], modeLabel)
                assertDecimal(0, residualScale, source.remainder(divisor, context), modeLabel)
            }
        }
    }
}

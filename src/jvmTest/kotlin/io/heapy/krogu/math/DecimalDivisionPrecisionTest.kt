package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalDivisionPrecisionTest {
    @Test fun exactDivisionErrorMessagesMatchJdk() {
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -10, 0, 10, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        val mismatches = mutableListOf<String>()
        val transitions = mutableMapOf<String, Int>()
        fun outcome(block: () -> Any): String = try { block().toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        for (scale in scales) for (divisorScale in scales) for (coefficient in listOf(0L, 1L, -1L, 10L)) {
            for (divisor in listOf(0L, 1L, -2L, 3L, 5L, 6L)) {
                val expected = outcome { java.math.BigDecimal.valueOf(coefficient, scale)
                    .divide(java.math.BigDecimal.valueOf(divisor, divisorScale)) }
                val actual = outcome { BigDecimal.valueOf(coefficient, scale)
                    .divide(BigDecimal.valueOf(divisor, divisorScale)) }
                if (expected != actual) {
                    mismatches.add("$coefficient@$scale / $divisor@$divisorScale: $expected / $actual")
                    val transition = "$expected -> $actual"
                    transitions[transition] = (transitions[transition] ?: 0) + 1
                }
            }
        }
        val examples = mismatches.distinctBy { it.substringAfter(": ") }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n$transitions\n${examples.joinToString("\n")}")
    }

    @Test fun extremePrecisionAndScaleOutcomesMatchJdk() {
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -10, 0, 10, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        val precisions = listOf(1, 2, 7, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        val mismatches = mutableListOf<String>()
        val transitions = mutableMapOf<String, Int>()
        fun outcome(block: () -> Any): String = try { block().toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        for (scale in scales) for (divisorScale in scales) for (precision in precisions) for (divisor in 1L..3L) {
            for (mode in RoundingMode.entries) {
                val expected = outcome { java.math.BigDecimal(java.math.BigInteger.ONE, scale).divide(
                    java.math.BigDecimal.valueOf(divisor, divisorScale), java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))) }
                val actual = outcome { BigDecimal(BigInteger.ONE, scale).divide(
                    BigDecimal.valueOf(divisor, divisorScale), MathContext(precision, mode)) }
                if (expected != actual) {
                    mismatches.add("scale=$scale divisor=$divisor scale=$divisorScale precision=$precision mode=$mode: $expected / $actual")
                    val transition = "$expected -> $actual"
                    transitions[transition] = (transitions[transition] ?: 0) + 1
                }
            }
        }
        val examples = mismatches.distinctBy { it.substringAfter(": ") }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n$transitions\n${examples.joinToString("\n")}")
    }
}

package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class ScaledDivisionBoundaryTest {
    @Test fun nonzeroDivisorsMatchJdkResultsAndMessages() {
        val scales = listOf(Int.MIN_VALUE, -10, 0, 10, Int.MAX_VALUE)
        val transitions = mutableMapOf<String, Int>()
        val examples = mutableMapOf<String, String>()
        fun outcome(block: () -> Any): String = try { block().toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        for (coefficient in listOf(0L, 1L, -1L, 10L)) for (scale in scales) {
            for (divisor in listOf(1L, -2L, 3L)) for (divisorScale in scales) for (target in scales) {
                for (mode in RoundingMode.entries) {
                    val expected = outcome { java.math.BigDecimal.valueOf(coefficient, scale).divide(
                        java.math.BigDecimal.valueOf(divisor, divisorScale), target, java.math.RoundingMode.valueOf(mode.name)) }
                    val actual = outcome { BigDecimal.valueOf(coefficient, scale).divide(
                        BigDecimal.valueOf(divisor, divisorScale), target, mode) }
                    if (expected != actual) {
                        val transition = "$expected -> $actual"
                        transitions[transition] = (transitions[transition] ?: 0) + 1
                        examples.putIfAbsent(transition, "$coefficient@$scale / $divisor@$divisorScale target=$target mode=$mode")
                    }
                }
            }
        }
        assertTrue(transitions.isEmpty(), "$transitions\n$examples")
    }
}

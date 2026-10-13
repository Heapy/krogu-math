package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class ScaledDivisionZeroTest {
    @Test fun zeroDivisorErrorPathsMatchJdkAtCompactAndScaleBoundaries() {
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 18, -19, -18, -1, 0, 1,
            18, 19, Int.MAX_VALUE - 18, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        val coefficients = listOf("0", "1", "-1", "10", "9223372036854775807", "-9223372036854775807",
            "-9223372036854775808", "9223372036854775808", "1000000000000000000", "-1000000000000000000",
            "123456789012345678901234567890", "-123456789012345678901234567890")
        val transitions = mutableMapOf<String, Int>()
        val examples = mutableMapOf<String, String>()
        fun outcome(block: () -> Any): String = try { block().toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        for (coefficient in coefficients) for (scale in scales) {
            val expectedValue = java.math.BigDecimal(java.math.BigInteger(coefficient), scale)
            val actualValue = BigDecimal(BigInteger(coefficient), scale)
            for (zeroScale in scales) for (target in scales) for (mode in RoundingMode.entries) {
                val expected = outcome { expectedValue.divide(java.math.BigDecimal(java.math.BigInteger.ZERO, zeroScale),
                    target, java.math.RoundingMode.valueOf(mode.name)) }
                val actual = outcome { actualValue.divide(BigDecimal(BigInteger.ZERO, zeroScale), target, mode) }
                if (expected != actual) {
                    val transition = "$expected -> $actual"
                    transitions[transition] = (transitions[transition] ?: 0) + 1
                    examples.putIfAbsent(transition, "$coefficient@$scale / 0@$zeroScale target=$target mode=$mode")
                }
            }
        }
        assertTrue(transitions.isEmpty(), "$transitions\n$examples")
    }
}

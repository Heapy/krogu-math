package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalExactConversionTest {
    @Test fun exactPrimitiveConversionsMatchJdkValuesAndMessages() {
        val coefficients = listOf("0", "1", "-1", "127", "128", "-128", "-129", "32767", "32768",
            "-32768", "-32769", "2147483647", "2147483648", "-2147483648", "-2147483649",
            "9223372036854775807", "9223372036854775808", "-9223372036854775808", "-9223372036854775809",
            "1000000000000000000000000000000")
        val scales = listOf(Int.MIN_VALUE, -20, -1, 0, 1, 20, Int.MAX_VALUE)
        val transitions = mutableMapOf<String, Int>()
        val examples = mutableMapOf<String, String>()
        fun outcome(block: () -> Any): String = try { block().toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        for (coefficient in coefficients) for (scale in scales) for (operation in listOf("byte", "short", "int", "long")) {
            val expectedValue = java.math.BigDecimal(java.math.BigInteger(coefficient), scale)
            val actualValue = BigDecimal(BigInteger(coefficient), scale)
            val expected = outcome { when (operation) {
                "byte" -> expectedValue.byteValueExact()
                "short" -> expectedValue.shortValueExact()
                "int" -> expectedValue.intValueExact()
                else -> expectedValue.longValueExact()
            } }
            val actual = outcome { when (operation) {
                "byte" -> actualValue.byteValueExact()
                "short" -> actualValue.shortValueExact()
                "int" -> actualValue.intValueExact()
                else -> actualValue.longValueExact()
            } }
            if (expected != actual) {
                val transition = "$expected -> $actual"
                transitions[transition] = (transitions[transition] ?: 0) + 1
                examples.putIfAbsent(transition, "$coefficient@$scale $operation")
            }
        }
        assertTrue(transitions.isEmpty(), "$transitions\n$examples")
    }
}

package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class SetScaleBoundaryTest {
    @Test fun scaleChangesMatchJdkValuesAndMessages() {
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -19, -18, -1, 0, 1, 18, 19, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        val coefficients = listOf("0", "1", "-1", "10", "9223372036854775807", "-9223372036854775808", "123456789012345678901234567890")
        val transitions = mutableMapOf<String, Int>()
        val examples = mutableMapOf<String, String>()
        fun outcome(block: () -> Any): String = try { block().toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        for (coefficient in coefficients) for (scale in scales) for (target in scales) for (mode in RoundingMode.entries) {
            val diagnostic = outcome { java.math.BigDecimal(java.math.BigInteger(coefficient), scale)
                .setScale(target, java.math.RoundingMode.valueOf(mode.name)) }
            val source = BigDecimal(BigInteger(coefficient), scale)
            val corrected = DecimalScaleRounding566Expected.setScale(source, target, mode)
            if (corrected != null)
                println("566_JDK_OBSERVATION\tsetScale\t$coefficient@$scale\ttarget=$target\tmode=$mode\t$diagnostic")
            val expected = corrected ?: diagnostic
            val observed = outcome { source.setScale(target, mode) }
            val actual = if (corrected != null && observed.startsWith("!ArithmeticException:"))
                "!ArithmeticException" else observed
            if (expected != actual) {
                val transition = "$expected -> $actual"
                transitions[transition] = (transitions[transition] ?: 0) + 1
                examples.putIfAbsent(transition, "$coefficient@$scale target=$target mode=$mode JDK=$diagnostic port=$observed")
            }
        }
        assertTrue(transitions.isEmpty(), "$transitions\n$examples")
    }
}

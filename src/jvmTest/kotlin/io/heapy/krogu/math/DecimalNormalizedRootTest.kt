package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalNormalizedRootTest {
    @Test fun normalizedSubclassGetterDispatchMatchesJdk() {
        val mismatches = mutableListOf<String>()
        for (text in listOf("9", "0.04", "4.00", "0", "-4", "0.4", "0.9", "4.0")) {
            for (precision in listOf(0, 1, 7)) for (mode in RoundingMode.entries) for (first in listOf(null, "1", "0", "4", "2", "-4", "throw")) {
                val expectedTrace = StringBuilder()
                val actualTrace = StringBuilder()
                val expectedNormalized = object : java.math.BigDecimal(text) {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    override fun signum(): Int { expectedTrace.append("sign;"); return super.signum() }
                    override fun precision(): Int { expectedTrace.append("precision;"); return super.precision() }
                    override fun scale(): Int { expectedTrace.append("scale;"); return super.scale() }
                    private var calls = 0
                    override fun unscaledValue(): java.math.BigInteger {
                        expectedTrace.append("unscaled;")
                        if (calls++ == 0 && first != null) {
                            if (first == "throw") throw IllegalStateException("first coefficient")
                            return java.math.BigInteger(first)
                        }
                        return super.unscaledValue()
                    }
                }
                val actualNormalized = object : BigDecimal(text) {
                    override fun signum(): Int { actualTrace.append("sign;"); return super.signum() }
                    override fun precision(): Int { actualTrace.append("precision;"); return super.precision() }
                    override fun scale(): Int { actualTrace.append("scale;"); return super.scale() }
                    private var calls = 0
                    override fun unscaledValue(): BigInteger {
                        actualTrace.append("unscaled;")
                        if (calls++ == 0 && first != null) {
                            if (first == "throw") throw IllegalStateException("first coefficient")
                            return BigInteger(first)
                        }
                        return super.unscaledValue()
                    }
                }
                val expected = object : java.math.BigDecimal("4.00") {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    override fun stripTrailingZeros(): java.math.BigDecimal = expectedNormalized
                }
                val actual = object : BigDecimal("4.00") {
                    override fun stripTrailingZeros(): BigDecimal = actualNormalized
                }
                fun outcome(block: () -> Any): String = try { block().toString() }
                    catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                val a = outcome { expected.sqrt(java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))) }
                val b = outcome { actual.sqrt(MathContext(precision, mode)) }
                if (a != b || expectedTrace.toString() != actualTrace.toString())
                    mismatches.add("$text/$precision/$mode/$first: $a|$expectedTrace <> $b|$actualTrace")
            }
        }
        assertTrue(mismatches.isEmpty(), mismatches.joinToString("\n"))
    }
}

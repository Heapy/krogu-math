package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalPowerSubclassTest {
    @Test fun powerAndRootGetterDispatchMatchesJdk() {
        val mismatches = mutableListOf<String>()
        for (text in listOf("-4", "0.00", "1", "4.00", "2", "0.0004", "123.456")) {
            for (precision in listOf(0, 1, 7)) for (mode in RoundingMode.entries) for (op in listOf("sqrt", "pow", "powContext")) {
                val expectedTrace = StringBuilder()
                val actualTrace = StringBuilder()
                val expected = object : java.math.BigDecimal(text) {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    override fun signum(): Int { expectedTrace.append("sign;"); return super.signum() }
                    override fun precision(): Int { expectedTrace.append("precision;"); return super.precision() }
                    override fun scale(): Int { expectedTrace.append("scale;"); return super.scale() }
                }
                val actual = object : BigDecimal(text) {
                    override fun signum(): Int { actualTrace.append("sign;"); return super.signum() }
                    override fun precision(): Int { actualTrace.append("precision;"); return super.precision() }
                    override fun scale(): Int { actualTrace.append("scale;"); return super.scale() }
                }
                fun outcome(block: () -> Any): String = try { block().toString() }
                    catch (e: ArithmeticException) { "!${e.message}" }
                val a = outcome { when (op) {
                    "sqrt" -> expected.sqrt(java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name)))
                    "pow" -> expected.pow(3)
                    else -> expected.pow(3, java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name)))
                } }
                val b = outcome { when (op) {
                    "sqrt" -> actual.sqrt(MathContext(precision, mode))
                    "pow" -> actual.pow(3)
                    else -> actual.pow(3, MathContext(precision, mode))
                } }
                if (a != b || expectedTrace.toString() != actualTrace.toString())
                    mismatches.add("$text/$precision/$mode/$op: $a|$expectedTrace <> $b|$actualTrace")
            }
        }
        assertTrue(mismatches.isEmpty(), mismatches.joinToString("\n"))
    }
}

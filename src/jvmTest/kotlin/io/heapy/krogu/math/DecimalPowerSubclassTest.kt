package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalPowerSubclassTest {
    @Test fun squareRootReplacementSignMatchesJdk() {
        val mismatches = mutableListOf<String>()
        for (text in listOf("0E+4", "0E+3", "0E+2", "0E+1", "0", "0.0", "0.00", "0.000", "0E-4", "0E-7", "0E-20", "1", "4.00", "2", "123.456")) {
            for (replacement in -1..1) for (precision in listOf(0, 1, 7)) {
                for (mode in RoundingMode.entries) {
                    val expectedTrace = StringBuilder()
                    val actualTrace = StringBuilder()
                    val expected = object : java.math.BigDecimal(text) {
                        override fun toByte(): Byte = super.toInt().toByte()
                        override fun toShort(): Short = super.toInt().toShort()
                        override fun signum(): Int { expectedTrace.append("sign;"); return replacement }
                        override fun precision(): Int { expectedTrace.append("precision;"); return super.precision() }
                    }
                    val actual = object : BigDecimal(text) {
                        override fun signum(): Int { actualTrace.append("sign;"); return replacement }
                        override fun precision(): Int { actualTrace.append("precision;"); return super.precision() }
                    }
                    fun outcome(block: () -> Any): String = try { block().toString() }
                        catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                    val a = outcome { expected.sqrt(java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))) }
                    val b = outcome { actual.sqrt(MathContext(precision, mode)) }
                    if (a != b || expectedTrace.toString() != actualTrace.toString())
                        mismatches.add("$text/$replacement/$precision/$mode: $a|$expectedTrace <> $b|$actualTrace")
                }
            }
        }
        assertTrue(mismatches.isEmpty(), mismatches.joinToString("\n"))
    }
    @Test fun squareRootReplacementPrecisionMatchesJdk() {
        val mismatches = mutableListOf<String>()
        for (text in listOf("1", "4.00", "2", "0.0004", "123.456")) {
            for (replacement in listOf(1, 7, 30)) for (precision in listOf(1, 7)) {
                for (mode in RoundingMode.entries) {
                    val expectedTrace = StringBuilder()
                    val actualTrace = StringBuilder()
                    val expected = object : java.math.BigDecimal(text) {
                        override fun toByte(): Byte = super.toInt().toByte()
                        override fun toShort(): Short = super.toInt().toShort()
                        override fun signum(): Int { expectedTrace.append("sign;"); return super.signum() }
                        override fun precision(): Int { expectedTrace.append("precision;"); return replacement }
                    }
                    val actual = object : BigDecimal(text) {
                        override fun signum(): Int { actualTrace.append("sign;"); return super.signum() }
                        override fun precision(): Int { actualTrace.append("precision;"); return replacement }
                    }
                    fun outcome(block: () -> Any): String = try { block().toString() }
                        catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                    val a = outcome { expected.sqrt(java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))) }
                    val b = outcome { actual.sqrt(MathContext(precision, mode)) }
                    if (a != b || expectedTrace.toString() != actualTrace.toString())
                        mismatches.add("$text/$replacement/$precision/$mode: $a|$expectedTrace <> $b|$actualTrace")
                }
            }
        }
        assertTrue(mismatches.isEmpty(), mismatches.joinToString("\n"))
    }
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

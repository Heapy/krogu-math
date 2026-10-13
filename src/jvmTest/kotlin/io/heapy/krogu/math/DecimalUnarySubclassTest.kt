package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalUnarySubclassTest {
    @Test fun negateContextUsesReturnedPlusCallback() {
        val mismatches = mutableListOf<String>()
        for (behavior in listOf("super", "replace", "throw")) for (precision in listOf(-1, 0, 1, 7)) for (mode in RoundingMode.entries) {
            val expectedTrace = StringBuilder()
            val actualTrace = StringBuilder()
            val expectedResult = object : java.math.BigDecimal("12.34") {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun plus(mc: java.math.MathContext?): java.math.BigDecimal {
                    expectedTrace.append("plus;")
                    return when (behavior) { "replace" -> java.math.BigDecimal("42.00"); "throw" -> throw IllegalStateException("plus callback"); else -> super.plus(mc) }
                }
                override fun round(mc: java.math.MathContext?): java.math.BigDecimal { expectedTrace.append("round;"); return java.math.BigDecimal("99") }
            }
            val actualResult = object : BigDecimal("12.34") {
                override fun plus(mc: MathContext?): BigDecimal {
                    actualTrace.append("plus;")
                    return when (behavior) { "replace" -> BigDecimal("42.00"); "throw" -> throw IllegalStateException("plus callback"); else -> super.plus(mc) }
                }
                override fun round(mc: MathContext?): BigDecimal { actualTrace.append("round;"); return BigDecimal("99") }
            }
            val expected = object : java.math.BigDecimal("1") {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun negate(): java.math.BigDecimal = expectedResult
            }
            val actual = object : BigDecimal("1") { override fun negate(): BigDecimal = actualResult }
            fun outcome(block: () -> Any): String = try { block().toString() }
                catch (e: Exception) { "!${e::class.simpleName}" + if (e is NullPointerException) "" else ":${e.message}" }
            val a = outcome { expected.negate(if (precision < 0) null else java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))) }
            val b = outcome { actual.negate(if (precision < 0) null else MathContext(precision, mode)) }
            if (a != b || expectedTrace.toString() != actualTrace.toString())
                mismatches.add("$behavior/$precision/$mode: $a|$expectedTrace <> $b|$actualTrace")
        }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n${mismatches.take(16).joinToString("\n")}")
    }
    @Test fun unaryDispatchMatchesJdk() {
        val mismatches = mutableListOf<String>()
        for (text in listOf("0.00", "-123.456", "123.456", "1.2345678901234567890")) {
            for (sign in -1..1) for (precision in listOf(0, 1, 7)) for (mode in listOf(RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)) {
                for (op in listOf("abs", "absContext", "negate", "negateContext", "plus", "plusContext")) {
                    val expectedTrace = StringBuilder()
                    val actualTrace = StringBuilder()
                    val expected = object : java.math.BigDecimal(text) {
                        override fun toByte(): Byte = super.toInt().toByte()
                        override fun toShort(): Short = super.toInt().toShort()
                        override fun signum(): Int { expectedTrace.append("sign;"); return sign }
                        override fun abs(): java.math.BigDecimal { expectedTrace.append("abs;"); return super.abs() }
                        override fun negate(): java.math.BigDecimal { expectedTrace.append("negate;"); return super.negate() }
                        override fun negate(mc: java.math.MathContext): java.math.BigDecimal { expectedTrace.append("negateContext;"); return super.negate(mc) }
                        override fun plus(): java.math.BigDecimal { expectedTrace.append("plus;"); return super.plus() }
                        override fun plus(mc: java.math.MathContext?): java.math.BigDecimal { expectedTrace.append("plusContext;"); return super.plus(mc) }
                        override fun round(mc: java.math.MathContext?): java.math.BigDecimal { expectedTrace.append("round;"); return super.round(mc) }
                    }
                    val actual = object : BigDecimal(text) {
                        override fun signum(): Int { actualTrace.append("sign;"); return sign }
                        override fun abs(): BigDecimal { actualTrace.append("abs;"); return super.abs() }
                        override fun negate(): BigDecimal { actualTrace.append("negate;"); return super.negate() }
                        override fun negate(mc: MathContext?): BigDecimal { actualTrace.append("negateContext;"); return super.negate(mc) }
                        override fun plus(): BigDecimal { actualTrace.append("plus;"); return super.plus() }
                        override fun plus(mc: MathContext?): BigDecimal { actualTrace.append("plusContext;"); return super.plus(mc) }
                        override fun round(mc: MathContext?): BigDecimal { actualTrace.append("round;"); return super.round(mc) }
                    }
                    val a = try {
                        val mc = java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))
                        val r = when (op) { "abs" -> expected.abs(); "absContext" -> expected.abs(mc); "negate" -> expected.negate(); "negateContext" -> expected.negate(mc); "plus" -> expected.plus(); else -> expected.plus(mc) }
                        "${java.math.BigDecimal(r.unscaledValue(), r.scale())}|${r === expected}|${r.javaClass == java.math.BigDecimal::class.java}"
                    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                    val b = try {
                        val mc = MathContext(precision, mode)
                        val r = when (op) { "abs" -> actual.abs(); "absContext" -> actual.abs(mc); "negate" -> actual.negate(); "negateContext" -> actual.negate(mc); "plus" -> actual.plus(); else -> actual.plus(mc) }
                        "${BigDecimal(r.unscaledValue(), r.scale())}|${r === actual}|${r.javaClass == BigDecimal::class.java}"
                    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                    if (a != b || expectedTrace.toString() != actualTrace.toString())
                        mismatches.add("$text/$sign/$precision/$mode/$op: $a|$expectedTrace <> $b|$actualTrace")
                }
            }
        }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n${mismatches.take(24).joinToString("\n")}")
    }
}

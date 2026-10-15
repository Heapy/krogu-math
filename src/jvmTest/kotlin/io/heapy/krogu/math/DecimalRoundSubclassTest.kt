package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecimalRoundSubclassTest {
    @Test fun roundGetterReplacementMatchesJdk() {
        val mismatches = mutableListOf<String>()
        var truthfulZeroRounds = 0
        val observedFiniteKeys = mutableSetOf<List<String>>()
        for (text in listOf("0.00", "-123.456", "999.99", "1.2345678901234567890")) {
            for (replacement in listOf(-1, 0, 1, 3, 9)) for (precision in listOf(0, 1, 7)) for (mode in RoundingMode.entries) for (behavior in listOf("super", "replace", "throw", "sequence")) {
                val expectedTrace = StringBuilder()
                val actualTrace = StringBuilder()
                val expected = object : java.math.BigDecimal(text) {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    private var calls = 0
                    override fun precision(): Int { expectedTrace.append("precision;"); return if (behavior == "sequence" && calls++ > 0) 1 else replacement }
                    override fun signum(): Int { expectedTrace.append("sign;"); return super.signum() }
                    override fun plus(mc: java.math.MathContext): java.math.BigDecimal {
                        expectedTrace.append("plus;")
                        return when (behavior) { "replace" -> java.math.BigDecimal("42.00"); "throw" -> throw IllegalStateException("plus callback"); else -> super.plus(mc) }
                    }
                }
                val actual = object : BigDecimal(text) {
                    private var calls = 0
                    override fun precision(): Int { actualTrace.append("precision;"); return if (behavior == "sequence" && calls++ > 0) 1 else replacement }
                    override fun signum(): Int { actualTrace.append("sign;"); return super.signum() }
                    override fun plus(mc: MathContext): BigDecimal {
                        actualTrace.append("plus;")
                        return when (behavior) { "replace" -> BigDecimal("42.00"); "throw" -> throw IllegalStateException("plus callback"); else -> super.plus(mc) }
                    }
                }
                val a = try {
                    val result = expected.round(java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name)))
                    "${java.math.BigDecimal(result.unscaledValue(), result.scale())}|${result === expected}|${result.javaClass == java.math.BigDecimal::class.java}"
                } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                val b = try {
                    val result = actual.round(MathContext(precision, mode))
                    "${BigDecimal(result.unscaledValue(), result.scale())}|${result === actual}|${result.javaClass == BigDecimal::class.java}"
                } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                val truthfulZero = text == "0.00" && replacement == 1 && behavior in listOf("super", "sequence")
                if (truthfulZero) truthfulZeroRounds++
                if (truthfulZero)
                    println("567_DIRECT_ZERO_OBSERVATION\t$text/$replacement/$precision/$mode/$behavior\tfrozen=$a|$expectedTrace\tactual=$b|$actualTrace")
                val key = listOf("S", behavior, text, "$replacement", "$precision", mode.name)
                val finite = key in FiniteZeroPolicy567.roundKeys
                if (finite) {
                    assertTrue(observedFiniteKeys.add(key), "duplicate finite round key $key")
                    println("567_FINITE_OBSERVATION\t$key\thost=$a|$expectedTrace\tactual=$b|$actualTrace")
                }
                val required = if (truthfulZero || finite) "0|false|true" else a
                // Keep the complete exact trace comparison, including the explicit plus override.
                if (required != b || expectedTrace.toString() != actualTrace.toString())
                    mismatches.add("$text/$replacement/$precision/$mode/$behavior: required=$required JDK=$a|$expectedTrace <> $b|$actualTrace")
            }
        }
        assertEquals(48, truthfulZeroRounds)
        assertEquals(FiniteZeroPolicy567.roundKeys, observedFiniteKeys)
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n${mismatches.take(20).joinToString("\n")}")
    }
}

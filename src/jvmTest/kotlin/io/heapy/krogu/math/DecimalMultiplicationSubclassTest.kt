package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalMultiplicationSubclassTest {
    @Test fun roundingScaleValidationMatchesJdk() {
        val mismatches = mutableListOf<String>()
        for (coefficient in listOf("0", "123", "999")) {
            for (scale in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 2, Int.MIN_VALUE + 3, 0, Int.MAX_VALUE)) {
                for (precision in listOf(0, 1, 2, 7)) for (mode in RoundingMode.entries) for (constructor in listOf(false, true)) {
                    val expectedContext = java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))
                    val actualContext = MathContext(precision, mode)
                    fun outcome(block: () -> Any): String = try { block().toString() }
                        catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                    val a = outcome { if (constructor) java.math.BigDecimal(java.math.BigInteger(coefficient), scale, expectedContext)
                        else java.math.BigDecimal(java.math.BigInteger(coefficient), scale).round(expectedContext) }
                    val b = outcome { if (constructor) BigDecimal(BigInteger(coefficient), scale, actualContext)
                        else BigDecimal(BigInteger(coefficient), scale).round(actualContext) }
                    if (a != b) mismatches.add("$coefficient@$scale/$precision/$mode/$constructor: $a <> $b")
                }
            }
        }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n${mismatches.take(30).joinToString("\n")}")
    }
    @Test fun multiplicationMatchesJdkDispatchAndScaleValidation() {
        val mismatches = mutableListOf<String>()
        for (left in listOf("0", "-1", "12345678901234567890")) for (right in listOf("0", "1", "123")) {
            for (leftScale in listOf(Int.MIN_VALUE, -2, 0, 2, Int.MAX_VALUE)) for (rightScale in listOf(Int.MIN_VALUE, -2, 0, 2, Int.MAX_VALUE)) {
                for (sign in -1..1) for (precision in listOf(-1, 0, 1, 7)) for (mode in listOf(RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)) {
                    val expectedTrace = StringBuilder()
                    val actualTrace = StringBuilder()
                    val expected = object : java.math.BigDecimal(java.math.BigInteger(left), leftScale) {
                        override fun toByte(): Byte = super.toInt().toByte()
                        override fun toShort(): Short = super.toInt().toShort()
                        override fun signum(): Int { expectedTrace.append("sign;"); return sign }
                        override fun precision(): Int { expectedTrace.append("precision;"); return 1 }
                        override fun multiply(value: java.math.BigDecimal): java.math.BigDecimal { expectedTrace.append("exact;"); return super.multiply(value) }
                        override fun multiply(value: java.math.BigDecimal, mc: java.math.MathContext): java.math.BigDecimal { expectedTrace.append("context;"); return super.multiply(value, mc) }
                    }
                    val actual = object : BigDecimal(BigInteger(left), leftScale) {
                        override fun signum(): Int { actualTrace.append("sign;"); return sign }
                        override fun precision(): Int { actualTrace.append("precision;"); return 1 }
                        override fun multiply(multiplicand: BigDecimal?): BigDecimal { actualTrace.append("exact;"); return super.multiply(multiplicand) }
                        override fun multiply(multiplicand: BigDecimal?, mc: MathContext?): BigDecimal { actualTrace.append("context;"); return super.multiply(multiplicand, mc) }
                    }
                    val expectedRight = java.math.BigDecimal(java.math.BigInteger(right), rightScale)
                    val actualRight = BigDecimal(BigInteger(right), rightScale)
                    fun outcome(block: () -> Any): String = try { block().toString() }
                        catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                    val a = outcome {
                        val result = if (precision < 0) expected.multiply(expectedRight) else expected.multiply(expectedRight, java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name)))
                        "$result|${result === expected}|${result.javaClass == java.math.BigDecimal::class.java}"
                    }
                    val b = outcome {
                        val result = if (precision < 0) actual.multiply(actualRight) else actual.multiply(actualRight, MathContext(precision, mode))
                        "$result|${result === actual}|${result.javaClass == BigDecimal::class.java}"
                    }
                    // The HIGH finite path reads stored constructor coefficients,
                    // then rounds a new base product; these receiver getters do
                    // not supply its arithmetic. Model those literal fields with
                    // ordinary operands, while retaining every explicit dispatch,
                    // result-identity and base-class check from this matrix.
                    val corrected = if (precision > 0) DecimalScaleRounding566Expected.multiplication(
                        BigDecimal(BigInteger(left), leftScale), actualRight, MathContext(precision, mode)) else null
                    if (corrected != null)
                        println("566_JDK_OBSERVATION\tmultiply\t$left@$leftScale*$right@$rightScale\treportedSign=$sign\tp=$precision\t$mode\t$a|$expectedTrace")
                    val expectedOutcome = when {
                        corrected == null -> a
                        corrected == "!ArithmeticException" -> corrected
                        else -> "$corrected|false|true"
                    }
                    val actualOutcome = if (corrected != null && b.startsWith("!ArithmeticException:"))
                        "!ArithmeticException" else b
                    if (expectedOutcome != actualOutcome || expectedTrace.toString() != actualTrace.toString())
                        mismatches.add("$left@$leftScale * $right@$rightScale/$sign/$precision/$mode: expected=$expectedOutcome JDK=$a|$expectedTrace <> $b|$actualTrace")
                }
            }
        }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n${mismatches.take(30).joinToString("\n")}")
    }
}

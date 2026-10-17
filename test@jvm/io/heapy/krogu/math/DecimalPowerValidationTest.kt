package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalPowerValidationTest {
    @Test fun exactPowerValidatesScaleBeforeCoefficientGrowth() {
        val mismatches = mutableListOf<String>()
        for (coefficient in listOf("0", "1", "-1", "8", "-8", "16", "-16")) {
            for (scale in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -1, 0, 1, Int.MAX_VALUE - 1, Int.MAX_VALUE)) {
                for (power in listOf(Int.MIN_VALUE, -1, 0, 1, 2, 999999999, 1000000000)) for (context in listOf(false, true)) {
                    val expectedTrace = StringBuilder()
                    val actualTrace = StringBuilder()
                    val expected = object : java.math.BigDecimal(java.math.BigInteger(coefficient), scale) {
                        override fun toByte(): Byte = super.toInt().toByte()
                        override fun toShort(): Short = super.toInt().toShort()
                        override fun signum(): Int { expectedTrace.append("sign;"); return super.signum() }
                        override fun pow(n: Int): java.math.BigDecimal { expectedTrace.append("pow;"); return super.pow(n) }
                    }
                    val actual = object : BigDecimal(BigInteger(coefficient), scale) {
                        override fun signum(): Int { actualTrace.append("sign;"); return super.signum() }
                        override fun pow(n: Int): BigDecimal { actualTrace.append("pow;"); return super.pow(n) }
                    }
                    fun outcome(block: () -> Any): String = try { block().toString() }
                        catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                    val a = outcome { if (context) expected.pow(power, java.math.MathContext.UNLIMITED) else expected.pow(power) }
                    val b = outcome { if (context) actual.pow(power, MathContext.UNLIMITED) else actual.pow(power) }
                    if (a != b || expectedTrace.toString() != actualTrace.toString())
                        mismatches.add("$coefficient@$scale/$power/$context: $a|$expectedTrace <> $b|$actualTrace")
                }
            }
        }
        assertTrue(mismatches.isEmpty(), mismatches.joinToString("\n"))
    }
}

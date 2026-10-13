package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalPointMovementTest {
    @Test fun pointMovementMatchesJdkStateIdentityAndDispatch() {
        val mismatches = mutableListOf<String>()
        for (coefficient in listOf("0", "1", "-1", "123", "-123", "9223372036854775808")) {
            for (scale in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -2, 0, 2, Int.MAX_VALUE - 1, Int.MAX_VALUE)) {
                for (distance in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -2, -1, 0, 1, 2, Int.MAX_VALUE - 1, Int.MAX_VALUE)) {
                    for (sign in -1..1) for (op in listOf("left", "right", "scale")) {
                        val expectedTrace = StringBuilder()
                        val actualTrace = StringBuilder()
                        val expected = object : java.math.BigDecimal(java.math.BigInteger(coefficient), scale) {
                            override fun toByte(): Byte = super.toInt().toByte()
                            override fun toShort(): Short = super.toInt().toShort()
                            override fun signum(): Int { expectedTrace.append("sign;"); return sign }
                            override fun precision(): Int { expectedTrace.append("precision;"); return 1 }
                            override fun movePointLeft(n: Int): java.math.BigDecimal { expectedTrace.append("left;"); return super.movePointLeft(n) }
                            override fun movePointRight(n: Int): java.math.BigDecimal { expectedTrace.append("right;"); return super.movePointRight(n) }
                            override fun scaleByPowerOfTen(n: Int): java.math.BigDecimal { expectedTrace.append("scaleBy;"); return super.scaleByPowerOfTen(n) }
                        }
                        val actual = object : BigDecimal(BigInteger(coefficient), scale) {
                            override fun signum(): Int { actualTrace.append("sign;"); return sign }
                            override fun precision(): Int { actualTrace.append("precision;"); return 1 }
                            override fun movePointLeft(n: Int): BigDecimal { actualTrace.append("left;"); return super.movePointLeft(n) }
                            override fun movePointRight(n: Int): BigDecimal { actualTrace.append("right;"); return super.movePointRight(n) }
                            override fun scaleByPowerOfTen(n: Int): BigDecimal { actualTrace.append("scaleBy;"); return super.scaleByPowerOfTen(n) }
                        }
                        val a = try {
                            val result = when (op) { "left" -> expected.movePointLeft(distance); "right" -> expected.movePointRight(distance); else -> expected.scaleByPowerOfTen(distance) }
                            val text = java.math.BigDecimal(result.unscaledValue(), result.scale()).toString()
                            "$text|${result === expected}|${result.javaClass == java.math.BigDecimal::class.java}"
                        } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                        val b = try {
                            val result = when (op) { "left" -> actual.movePointLeft(distance); "right" -> actual.movePointRight(distance); else -> actual.scaleByPowerOfTen(distance) }
                            val text = BigDecimal(result.unscaledValue(), result.scale()).toString()
                            "$text|${result === actual}|${result.javaClass == BigDecimal::class.java}"
                        } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                        if (a != b || expectedTrace.toString() != actualTrace.toString())
                            mismatches.add("$coefficient@$scale/$distance/$sign/$op: $a|$expectedTrace <> $b|$actualTrace")
                    }
                }
            }
        }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n${mismatches.take(40).joinToString("\n")}")
    }
}

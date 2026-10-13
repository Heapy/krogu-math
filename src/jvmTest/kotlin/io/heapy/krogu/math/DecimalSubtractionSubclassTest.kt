package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalSubtractionSubclassTest {
    @Test fun exactAdditionUsesStoredSigns() {
        val mismatches = mutableListOf<String>()
        for (left in listOf("0.00", "-123.45", "1.2345678901234567890")) for (right in listOf("0.00", "-123.45", "1.2345678901234567890")) for (sign in -1..1) {
            val et = StringBuilder(); val at = StringBuilder()
            fun expected(text: String) = object : java.math.BigDecimal(text) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun signum(): Int { et.append("sign;"); return sign }
            }
            fun actual(text: String) = object : BigDecimal(text) {
                override fun signum(): Int { at.append("sign;"); return sign }
            }
            val a = expected(left).add(expected(right)).toString()
            val b = actual(left).add(actual(right)).toString()
            if (a != b || et.toString() != at.toString()) mismatches.add("$left+$right/$sign: $a|$et <> $b|$at")
        }
        assertTrue(mismatches.isEmpty(), mismatches.joinToString("\n"))
    }
    @Test fun finiteWideAdditionBypassesExactAddOverride() {
        val mismatches = mutableListOf<String>()
        for (coefficient in listOf("1", "-1")) for (leftScale in listOf(-10001, 0, 10001)) for (rightScale in listOf(-10001, 0, 10001)) {
            for (precision in listOf(1, 7)) for (mode in listOf(RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)) {
                val et = StringBuilder(); val at = StringBuilder()
                val expected = object : java.math.BigDecimal(java.math.BigInteger(coefficient), leftScale) {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    override fun add(value: java.math.BigDecimal): java.math.BigDecimal { et.append("add;"); throw IllegalStateException("exact add callback") }
                }
                val actual = object : BigDecimal(BigInteger(coefficient), leftScale) {
                    override fun add(augend: BigDecimal?): BigDecimal { at.append("add;"); throw IllegalStateException("exact add callback") }
                }
                fun outcome(block: () -> Any): String = try { block().toString() } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                val a = outcome { expected.add(java.math.BigDecimal(java.math.BigInteger.ONE, rightScale), java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))) }
                val b = outcome { actual.add(BigDecimal(BigInteger.ONE, rightScale), MathContext(precision, mode)) }
                if (a != b || et.toString() != at.toString()) mismatches.add("$coefficient@$leftScale+1@$rightScale/$precision/$mode: $a|$et <> $b|$at")
            }
        }
        assertTrue(mismatches.isEmpty(), mismatches.joinToString("\n"))
    }
    @Test fun subtractionCallbackRoutesMatchJdk() {
        val mismatches = mutableListOf<String>()
        for (left in listOf("0.00", "-123.45", "1.2345678901234567890")) for (right in listOf("0.00", "-123.45", "1.2345678901234567890")) {
            for (precision in listOf(-1, 0, 1, 7)) for (mode in listOf(RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)) {
                for (leftBehavior in listOf("super", "replace", "throw")) for (rightBehavior in listOf("super", "replace", "throw")) {
                    val et = StringBuilder(); val at = StringBuilder()
                    val expected = object : java.math.BigDecimal(left) {
                        override fun toByte(): Byte = super.toInt().toByte()
                        override fun toShort(): Short = super.toInt().toShort()
                        override fun add(value: java.math.BigDecimal): java.math.BigDecimal {
                            et.append("add;")
                            return when (leftBehavior) { "replace" -> java.math.BigDecimal("42"); "throw" -> throw IllegalStateException("add callback"); else -> super.add(value) }
                        }
                        override fun add(value: java.math.BigDecimal, mc: java.math.MathContext): java.math.BigDecimal {
                            et.append("addContext;")
                            return when (leftBehavior) { "replace" -> java.math.BigDecimal("43"); "throw" -> throw IllegalStateException("add context callback"); else -> super.add(value, mc) }
                        }
                        override fun subtract(value: java.math.BigDecimal): java.math.BigDecimal { et.append("subtract;"); return super.subtract(value) }
                    }
                    val actual = object : BigDecimal(left) {
                        override fun add(augend: BigDecimal?): BigDecimal {
                            at.append("add;")
                            return when (leftBehavior) { "replace" -> BigDecimal("42"); "throw" -> throw IllegalStateException("add callback"); else -> super.add(augend) }
                        }
                        override fun add(augend: BigDecimal?, mc: MathContext?): BigDecimal {
                            at.append("addContext;")
                            return when (leftBehavior) { "replace" -> BigDecimal("43"); "throw" -> throw IllegalStateException("add context callback"); else -> super.add(augend, mc) }
                        }
                        override fun subtract(subtrahend: BigDecimal?): BigDecimal { at.append("subtract;"); return super.subtract(subtrahend) }
                    }
                    val expectedRight = object : java.math.BigDecimal(right) {
                        override fun toByte(): Byte = super.toInt().toByte()
                        override fun toShort(): Short = super.toInt().toShort()
                        override fun negate(): java.math.BigDecimal {
                            et.append("rightNegate;")
                            return when (rightBehavior) { "replace" -> java.math.BigDecimal("99"); "throw" -> throw IllegalStateException("negate callback"); else -> super.negate() }
                        }
                    }
                    val actualRight = object : BigDecimal(right) {
                        override fun negate(): BigDecimal {
                            at.append("rightNegate;")
                            return when (rightBehavior) { "replace" -> BigDecimal("99"); "throw" -> throw IllegalStateException("negate callback"); else -> super.negate() }
                        }
                    }
                    fun outcome(block: () -> Any): String = try { block().toString() } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                    val a = outcome { if (precision < 0) expected.subtract(expectedRight) else expected.subtract(expectedRight, java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))) }
                    val b = outcome { if (precision < 0) actual.subtract(actualRight) else actual.subtract(actualRight, MathContext(precision, mode)) }
                    if (a != b || et.toString() != at.toString()) mismatches.add("$left - $right/$precision/$mode/$leftBehavior/$rightBehavior: $a|$et <> $b|$at")
                }
            }
        }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n${mismatches.take(24).joinToString("\n")}")
    }
}

package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalComparisonSubclassTest {
    private val values = listOf("0", "0.00", "1", "1.00", "-1.00", "123.456", "12345678901234567890.00", "9.0", "1.234", "9.87", "9999999999999999999.0",
        "9223372036854775807", "-9223372036854775808", "9223372036854775808", "-9223372036854775809")
    @Test fun selfComparisonMatchesJdkCallbacks() {
        val signs = (-1..1).flatMap { a -> (-1..1).map { b -> a to b } } + listOf(-2 to -2, 2 to 2)
        val mismatches = mutableListOf<String>()
        for (text in values) for ((first, second) in signs) {
            val et = StringBuilder(); val at = StringBuilder()
            val expected = object : java.math.BigDecimal(text) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                private var calls = 0
                override fun signum(): Int { et.append("sign;"); return if (calls++ == 0) first else second }
                override fun precision(): Int { et.append("precision;"); return 1 }
                override fun scale(): Int { et.append("scale;"); return 0 }
            }
            val actual = object : BigDecimal(text) {
                private var calls = 0
                override fun signum(): Int { at.append("sign;"); return if (calls++ == 0) first else second }
                override fun precision(): Int { at.append("precision;"); return 1 }
                override fun scale(): Int { at.append("scale;"); return 0 }
            }
            val a = expected.compareTo(expected); val b = actual.compareTo(actual)
            if (a != b || et.toString() != at.toString()) mismatches.add("$text/$first/$second: $a|$et <> $b|$at")
        }
        assertTrue(mismatches.isEmpty(), mismatches.joinToString("\n"))
    }
    @Test fun ordinaryComparisonResultsMatchJdk() {
        val mismatches = mutableListOf<String>()
        for (left in values) for (right in values) {
            val expected = java.math.BigDecimal(left).compareTo(java.math.BigDecimal(right))
            val actual = BigDecimal(left).compareTo(BigDecimal(right))
            if (expected != actual) mismatches.add("$left/$right: $expected <> $actual")
        }
        val scaled = listOf("0", "1", "-1", "12345678901234567890").flatMap { coefficient ->
            listOf(Int.MIN_VALUE, -2, 0, 2, Int.MAX_VALUE).map { scale -> coefficient to scale }
        }
        for ((left, ls) in scaled) for ((right, rs) in scaled) {
            val expected = java.math.BigDecimal(java.math.BigInteger(left), ls).compareTo(java.math.BigDecimal(java.math.BigInteger(right), rs))
            val actual = BigDecimal(BigInteger(left), ls).compareTo(BigDecimal(BigInteger(right), rs))
            if (expected != actual) mismatches.add("$left@$ls/$right@$rs: $expected <> $actual")
        }
        assertTrue(mismatches.isEmpty(), mismatches.joinToString("\n"))
    }
    @Test fun comparisonGetterDispatchMatchesJdk() {
        val mismatches = mutableListOf<String>()
        val signPairs = (-1..1).flatMap { a -> (-1..1).map { b -> a to b } } + listOf(-2 to -2, 2 to 2)
        for (left in values) for (right in values) for ((sign, rightSign) in signPairs) for (digits in listOf(-1, 1, 7)) {
            val et = StringBuilder(); val at = StringBuilder()
            fun expected(text: String, label: String) = object : java.math.BigDecimal(text) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun signum(): Int { et.append(label).append("sign;"); return if (label == "a") sign else rightSign }
                override fun precision(): Int { et.append(label).append("precision;"); return if (digits < 0) super.precision() else digits }
                override fun scale(): Int { et.append(label).append("scale;"); return 0 }
            }
            fun actual(text: String, label: String) = object : BigDecimal(text) {
                override fun signum(): Int { at.append(label).append("sign;"); return if (label == "a") sign else rightSign }
                override fun precision(): Int { at.append(label).append("precision;"); return if (digits < 0) super.precision() else digits }
                override fun scale(): Int { at.append(label).append("scale;"); return 0 }
            }
            val a = expected(left, "a").compareTo(expected(right, "b"))
            val b = actual(left, "a").compareTo(actual(right, "b"))
            if (a != b || et.toString() != at.toString()) mismatches.add("$left/$right/$sign/$rightSign/$digits: $a|$et <> $b|$at")
        }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n${mismatches.take(24).joinToString("\n")}")
    }
}

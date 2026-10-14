// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerGcdOracleTest {
    private class Probe(text: String, val label: String, val trace: StringBuilder, val mode: String) : BigInteger(text) {
        var other: BigInteger = BigInteger.ZERO
        val replacement = BigInteger(if (mode == "negative-fixed") "-7" else "7")
        private fun call(name: String) {
            trace.append("$label.$name;")
            if (mode == "$name:throw") throw IllegalStateException("$name callback")
        }
        override fun abs(): BigInteger { call("abs"); return when (mode) {
            "abs:self" -> this; "abs:other" -> other
            "abs:fixed", "negative-fixed" -> replacement; else -> super.abs()
        } }
        override fun negate(): BigInteger { call("negate"); return when (mode) {
            "negate:self" -> this; "negate:other" -> other
            "negate:fixed" -> replacement; else -> super.negate()
        } }
        override fun remainder(value: BigInteger): BigInteger { call("rem"); return super.remainder(value) }
        override fun divideAndRemainder(value: BigInteger): Array<BigInteger> { call("pair"); return super.divideAndRemainder(value) }
        private fun poison(name: String): Nothing { trace.append("$label.$name;"); throw IllegalStateException("$name callback") }
        override fun equals(other: Any?): Boolean = poison("equals")
        override fun signum(): Int = poison("signum")
        override fun compareTo(other: BigInteger?): Int = poison("compare")
        override fun bitLength(): Int = poison("bits")
        override fun getLowestSetBit(): Int = poison("lowest")
        override fun shiftRight(n: Int): BigInteger = poison("right")
        override fun subtract(value: BigInteger): BigInteger = poison("subtract")
    }
    @Test fun valuesCallbacksAndIdentities() = verify("dispatch")
    @Test fun wideFactorsAndConsecutiveFibonacci() = verify("wide")
    private fun verify(family: String) {
        for ((index, row) in jdk25IntegerGcdCases().withIndex()) {
            if (row[4] != family) continue
            val trace = StringBuilder()
            val left = Probe(row[0], "L", trace, row[2])
            val right = if (row[3].toBoolean()) left else Probe(row[1], "R", trace, row[2])
            left.other = right; right.other = left
            val actual = try {
                val value = left.gcd(right)
                val calls = trace.toString()
                val text = if (value === left) row[0] else if (value === right) row[1] else value.toString()
                "$text|${value === left}|${value === right}|${value === left.replacement}|${value === right.replacement}|${value === BigInteger.ZERO}|${value === BigInteger.ONE}|${value::class == BigInteger::class}|$calls"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
            assertEquals(row[5], actual, "GCD case $index ${row[2]} $family")
        }
    }
}

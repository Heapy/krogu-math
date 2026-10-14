// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerPowOracleTest {
    private class Probe(text: String, val trace: StringBuilder, val mode: String) : BigInteger(text) {
        private fun call(name: String) {
            trace.append("$name;")
            if (mode == "$name:throw") throw IllegalStateException("$name callback")
        }
        private fun replacement(name: String): BigInteger? =
            if (mode.startsWith("$name:") && mode != "$name:throw") BigInteger(mode.substringAfter(':')) else null
        override fun equals(other: Any?): Boolean { call("equals"); return when (mode) {
            "equals:true" -> true; "equals:false" -> false; else -> super.equals(other)
        } }
        override fun abs(): BigInteger { call("abs"); return if (mode == "self") this else replacement("abs") ?: super.abs() }
        override fun negate(): BigInteger { call("negate"); return replacement("negate") ?: super.negate() }
        override fun multiply(value: BigInteger): BigInteger { call("multiply"); return super.multiply(value) }
        override fun signum(): Int { call("signum"); return super.signum() }
        override fun toLong(): Long { call("long"); return super.toLong() }
        override fun getLowestSetBit(): Int { call("lowest"); return if (mode.startsWith("lowest:") && mode != "lowest:throw") mode.substringAfter(':').toInt() else super.getLowestSetBit() }
        override fun bitLength(): Int { call("bits"); return if (mode.startsWith("bits:") && mode != "bits:throw") mode.substringAfter(':').toInt() else super.bitLength() }
        override fun shiftRight(n: Int): BigInteger { call("right($n)"); if (mode == "right:throw") throw IllegalStateException("right callback"); return replacement("right") ?: super.shiftRight(n) }
        override fun shiftLeft(n: Int): BigInteger { call("left($n)"); if (mode == "left:throw") throw IllegalStateException("left callback"); return super.shiftLeft(n) }
    }
    @Test fun valuesCallbacksErrorsAndIdentities() {
        for ((index, row) in jdk25IntegerPowCases().withIndex()) {
            val trace = StringBuilder()
            val source = Probe(row[0], trace, row[2])
            val actual = try {
                val value = source.pow(row[1].toInt())
                val calls = trace.toString()
                val result = if (value === source) row[0] else value.toString()
                "$result|${value === source}|${value === BigInteger.ZERO}|${value === BigInteger.ONE}|${value === BigInteger.TWO}|${value === BigInteger.TEN}|${value::class == BigInteger::class}|$calls"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
            assertEquals(row[3], actual, "integer power case $index ${row[0]} ^ ${row[1]} ${row[2]}")
        }
    }
}

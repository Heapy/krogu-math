// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerDivisionDispatchOracleTest {
    private class Probe(text: String, val label: String, val trace: StringBuilder, val mode: String) : BigInteger(text) {
        private val replacement = BigInteger("7")
        private fun call(name: String) {
            trace.append("$label.$name;")
            if (mode == "throw") throw IllegalStateException("$name callback")
        }
        override fun abs(): BigInteger { call("abs"); return if (mode == "replace") replacement else super.abs() }
        override fun negate(): BigInteger { call("negate"); return if (mode == "replace") replacement else super.negate() }
        override fun signum(): Int { call("signum"); return if (mode == "replace") -7 else super.signum() }
        override fun bitLength(): Int { call("bitLength"); return if (mode == "replace") 1 else super.bitLength() }
        override fun compareTo(other: BigInteger): Int { call("compare"); return if (mode == "replace") 0 else super.compareTo(other) }
        override fun testBit(n: Int): Boolean { call("testBit"); return if (mode == "replace") false else super.testBit(n) }
        override fun add(value: BigInteger): BigInteger { call("add"); return if (mode == "replace") replacement else super.add(value) }
        override fun subtract(value: BigInteger): BigInteger { call("subtract"); return if (mode == "replace") replacement else super.subtract(value) }
        override fun shiftLeft(n: Int): BigInteger { call("shiftLeft"); return if (mode == "replace") replacement else super.shiftLeft(n) }
        override fun divideAndRemainder(value: BigInteger): Array<BigInteger> {
            call("pair")
            return when (mode) {
                "replace" -> arrayOf(replacement, replacement)
                "empty-pair" -> emptyArray()
                else -> super.divideAndRemainder(value)
            }
        }
        fun inheritedPair(value: BigInteger): Array<BigInteger> = super.divideAndRemainder(value)
    }
    @Test fun ordinaryAndSubclassDivision() {
        for ((index, row) in jdk25IntegerDivisionCases().withIndex()) {
            val trace = StringBuilder()
            val left = Probe(row[0], "L", trace, row[3])
            val right = if (row[4].toBoolean()) left else Probe(row[1], "R", trace, row[3])
            val actual = try {
                val values = when (row[2]) {
                    "divide" -> arrayOf(left.divide(right))
                    "remainder" -> arrayOf(left.remainder(right))
                    else -> left.inheritedPair(right)
                }
                val calls = trace.toString()
                values.joinToString("|") {
                    val text = if (it === left) row[0] else if (it === right) row[1] else it.toString()
                    "$text:${it === left}:${it === right}:${it === BigInteger.ZERO}:${it === BigInteger.ONE}:${it === BigInteger.TWO}:${it === BigInteger.TEN}:${it::class == BigInteger::class}"
                } + "|same=${values.size == 2 && values[0] === values[1]}|$calls"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
            assertEquals(row[5], actual, "division dispatch case $index ${row[2]} ${row[3]}")
        }
    }
}

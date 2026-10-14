// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegerSqrtOracleTest {
    private class Probe(text: String, val trace: StringBuilder, val mode: String) : BigInteger(text) {
        private val replacement = BigInteger("7")
        private fun call(name: String) { trace.append("$name;"); if (mode == "throw") throw IllegalStateException("$name callback") }
        override fun sqrt(): BigInteger { call("sqrt"); return if (mode == "replace") replacement else super.sqrt() }
        override fun sqrtAndRemainder(): Array<BigInteger> { call("pair"); return when (mode) {
            "replace" -> arrayOf(replacement, replacement); "empty-pair" -> emptyArray(); else -> super.sqrtAndRemainder()
        } }
        override fun compareTo(other: BigInteger?): Int { call("compare"); return if (mode == "replace") -1 else super.compareTo(other) }
        override fun bitLength(): Int { call("bits"); return if (mode == "replace") 1 else super.bitLength() }
        override fun signum(): Int { call("signum"); return if (mode == "replace") -1 else super.signum() }
        override fun divide(value: BigInteger): BigInteger { call("divide"); return if (mode == "replace") replacement else super.divide(value) }
        override fun subtract(value: BigInteger): BigInteger { call("subtract"); return if (mode == "replace") replacement else super.subtract(value) }
        override fun multiply(value: BigInteger): BigInteger { call("multiply"); return if (mode == "replace") replacement else super.multiply(value) }
        override fun add(value: BigInteger): BigInteger { call("add"); return if (mode == "replace") replacement else super.add(value) }
        override fun shiftRight(n: Int): BigInteger { call("right"); return if (mode == "replace") replacement else super.shiftRight(n) }
        override fun shiftLeft(n: Int): BigInteger { call("left"); return if (mode == "replace") replacement else super.shiftLeft(n) }
        fun inheritedRoot(): BigInteger = super.sqrt()
        fun inheritedPair(): Array<BigInteger> = super.sqrtAndRemainder()
    }
    @Test fun callbackAndIdentityCases() = verify { it == "dispatch" }
    @Test fun smallExactSquareNeighbors() = verify { it == "small-squares" }
    @Test fun rootBoundariesThrough128Bits() = verifyBits(1..128)
    @Test fun rootBoundariesThrough1024Bits() = verifyBits(129..1024)
    @Test fun rootBoundariesThrough2048Bits() = verifyBits(1025..2048)
    @Test fun rootBoundariesAt4096Bits() = verifyBits(4096..4096)
    private fun verifyBits(bits: IntRange) = verify { it.startsWith("bits-") && it.substringAfter('-').toInt() in bits }
    private fun verify(select: (String) -> Boolean) {
        for ((index, row) in jdk25IntegerSqrtCases().withIndex()) {
            if (!select(row[3])) continue
            val trace = StringBuilder()
            val source = Probe(row[0], trace, row[2])
            var returned: Array<BigInteger>? = null
            val actual = try {
                val values = if (row[1] == "sqrt") arrayOf(source.inheritedRoot()) else source.inheritedPair()
                returned = values
                assertEquals(if (row[1] == "sqrt") 1 else 2, values.size, "result length case $index")
                val calls = trace.toString()
                values.joinToString("|") { value ->
                    val text = if (value === source) row[0] else value.toString()
                    "$text:${value === source}:${value === BigInteger.ZERO}:${value === BigInteger.ONE}:${value === BigInteger.TWO}:${value::class == BigInteger::class}"
                } + "|same=${values.size == 2 && values[0] === values[1]}|$calls"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
            assertEquals(row[4], actual, "integer sqrt case $index ${row[1]} ${row[2]} ${row[3]}")
            returned?.let { values ->
                val input = BigInteger(row[0])
                val root = values[0]
                val square = root.multiply(root)
                val next = root.add(BigInteger.ONE)
                assertTrue(root.signum() >= 0 && square <= input, "lower root bound case $index")
                assertTrue(next.multiply(next) > input, "upper root bound case $index")
                if (values.size == 2) {
                    assertEquals(input, square.add(values[1]), "root/remainder reconstruction case $index")
                    assertTrue(values[1].signum() >= 0 && values[1] < root.shiftLeft(1).add(BigInteger.ONE), "remainder bound case $index")
                }
            }
        }
    }
}

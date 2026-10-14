// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame

private class BitProbe(text: String, val trace: StringBuilder, val mode: String) : BigInteger(text) {
    private fun call(name: String) { trace.append("$name;"); if (mode == "throw") throw IllegalStateException("$name callback") }
    override fun bitLength(): Int { call("bits"); return if (mode.startsWith("bits-")) mode.substringAfter('-').toInt() else super.bitLength() }
    override fun bitCount(): Int { call("count"); return super.bitCount() }
    override fun getLowestSetBit(): Int { call("lowest"); return if (mode.startsWith("lowest=")) mode.substringAfter('=').toInt() else super.getLowestSetBit() }
    override fun signum(): Int { call("sign"); return super.signum() }
    override fun toByteArray(): ByteArray { call("bytes"); return super.toByteArray() }
    override fun testBit(n: Int): Boolean { call("test"); return super.testBit(n) }
    override fun not(): BigInteger { call("not"); return super.not() }
    override fun and(value: BigInteger?): BigInteger { call("and"); return super.and(value) }
    override fun or(value: BigInteger?): BigInteger { call("or"); return super.or(value) }
    override fun xor(value: BigInteger?): BigInteger { call("xor"); return super.xor(value) }
    override fun andNot(value: BigInteger?): BigInteger { call("andNot"); return super.andNot(value) }
    override fun setBit(n: Int): BigInteger { call("set"); return super.setBit(n) }
    override fun clearBit(n: Int): BigInteger { call("clear"); return super.clearBit(n) }
    override fun flipBit(n: Int): BigInteger { call("flip"); return super.flipBit(n) }
    fun inherited(op: String, other: BigInteger, bit: Int): Any = when (op) {
        "and" -> super.and(other); "or" -> super.or(other); "xor" -> super.xor(other); "andNot" -> super.andNot(other)
        "not" -> super.not(); "set" -> super.setBit(bit); "clear" -> super.clearBit(bit); "flip" -> super.flipBit(bit)
        "test" -> super.testBit(bit); "count" -> super.bitCount(); "lowest" -> super.getLowestSetBit(); else -> super.bitLength()
    }
}
private fun bitOutcome(row: Array<String>): String {
    val leftTrace = StringBuilder(); val rightTrace = StringBuilder()
    val left = BitProbe(row[0], leftTrace, row[4])
    val right = if (row[6] == "self") left else BitProbe(row[1], rightTrace, row[5])
    return try {
        val result = left.inherited(row[2], right, row[3].toInt())
        val calls = "$leftTrace/$rightTrace"
        val value = if (result is BigInteger) "$result:${result === left}:${result === right}:${result === BigInteger.ZERO}:${result === BigInteger.ONE}:${result::class == BigInteger::class}" else result.toString()
        "$value|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$leftTrace/$rightTrace" }
}
class IntegerBitsOracleTest {
    // Observed with public JDK 25.0.4+9 APIs and a 1 GiB heap; JDK allocates
    // a sign-extension array even for these unchanged-value updates.
    @Test fun largestBitAddressValuesAndDiagnostics() {
        for (text in listOf("0", "-1")) {
            val source = BigInteger(text)
            for (op in listOf("set", "clear", "flip")) {
                fun update(): BigInteger = when (op) {
                    "set" -> source.setBit(Int.MAX_VALUE)
                    "clear" -> source.clearBit(Int.MAX_VALUE)
                    else -> source.flipBit(Int.MAX_VALUE)
                }
                if ((text == "0" && op == "clear") || (text == "-1" && op == "set")) {
                    val result = update()
                    assertEquals(source, result)
                    assertNotSame(source, result)
                    assertNotSame(BigInteger.ZERO, result)
                } else assertEquals("BigInteger would overflow supported range",
                    assertFailsWith<ArithmeticException> { update() }.message)
            }
        }
    }
    @Test fun binaryDispatch() = verify { it[7] == "dispatch" && it[2] in listOf("and", "or", "xor", "andNot") }
    @Test fun notDispatch() = verify { it[7] == "dispatch" && it[2] == "not" }
    @Test fun setDispatch() = verify { it[7] == "dispatch" && it[2] == "set" }
    @Test fun clearDispatch() = verify { it[7] == "dispatch" && it[2] == "clear" }
    @Test fun flipDispatch() = verify { it[7] == "dispatch" && it[2] == "flip" }
    @Test fun queryDispatch() = verify { it[7] == "dispatch" && it[2] in listOf("test","count","lowest","bits") }
    @Test fun wideBoundaries() = verify { it[7] == "wide" }
    private fun verify(select: (Array<String>) -> Boolean) {
        for ((index,row) in jdk25IntegerBitCases().withIndex()) if (select(row))
            assertEquals(row[8], bitOutcome(row), "integer bits case $index ${row.take(8)}")
    }
}

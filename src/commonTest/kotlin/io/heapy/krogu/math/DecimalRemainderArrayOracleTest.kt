// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals

private class ArrayProbe(text: String, val length: Int, val trace: StringBuilder) : BigDecimal(text) {
    private fun pair(): Array<BigDecimal> = Array(length) { if (it == 1) this else BigDecimal("7.0") }
    override fun divideAndRemainder(v: BigDecimal): Array<BigDecimal> {
        trace.append("pair(${v == null});"); return pair()
    }
    override fun divideAndRemainder(v: BigDecimal, mc: MathContext): Array<BigDecimal> {
        trace.append("pair(${v == null},${mc?.precision});"); return pair()
    }
    fun enter(op: String, v: BigDecimal, mc: MathContext): BigDecimal =
        if (op == "exact") super.remainder(v) else super.remainder(v, mc)
}
private fun arrayOutcome(text: String, length: Int, op: String, operand: String, precision: Int): String {
    val trace = StringBuilder()
    val source = ArrayProbe(text, length, trace)
    val divisor = when (operand) { "self" -> source; else -> BigDecimal("2.0") }
    // Exact remainder has no context slot; retain its -1 input key.
    val mc = MathContext(if (op == "exact" && precision < 0) 0 else precision)
    return try {
        val result = source.enter(op, divisor, mc)
        val calls = trace.toString()
        "${if (result === source) "source" else result.toString()}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
}
class DecimalRemainderArrayOracleTest {
    @Test fun replacementPairArraysPreserveBoundsFailuresAndSecondElementIdentity() {
        val moved = mutableSetOf<Int>()
        for ((index, row) in jdk25DecimalRemainderArrayCases().withIndex()) {
            if (DecimalBinary575Rows.array(row)) { moved.add(index); continue }
            assertEquals(row[5], arrayOutcome(row[0], row[1].toInt(), row[2], row[3], row[4].toInt()), row.take(5).toString())
        }
        assertEquals(96, moved.size, "Exact null-input rows replayed through Java explicit super")
        assertEquals(DecimalBinary575Rows.arrayOrdinals, moved)
    }
}

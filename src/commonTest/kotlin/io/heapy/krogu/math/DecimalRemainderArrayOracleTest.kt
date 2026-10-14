// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals

private class ArrayProbe(text: String, val length: Int, val trace: StringBuilder) : BigDecimal(text) {
    private fun pair(): Array<BigDecimal> = Array(length) { if (it == 1) this else BigDecimal("7.0") }
    override fun divideAndRemainder(v: BigDecimal?): Array<BigDecimal> {
        trace.append("pair(${v == null});"); return pair()
    }
    override fun divideAndRemainder(v: BigDecimal?, mc: MathContext?): Array<BigDecimal> {
        trace.append("pair(${v == null},${mc?.precision});"); return pair()
    }
    fun enter(op: String, v: BigDecimal?, mc: MathContext?): BigDecimal =
        if (op == "exact") super.remainder(v) else super.remainder(v, mc)
}
private fun arrayOutcome(text: String, length: Int, op: String, operand: String, precision: Int): String {
    val trace = StringBuilder()
    val source = ArrayProbe(text, length, trace)
    val divisor = when (operand) { "null" -> null; "self" -> source; else -> BigDecimal("2.0") }
    val mc = if (precision < 0) null else MathContext(precision)
    return try {
        val result = source.enter(op, divisor, mc)
        val calls = trace.toString()
        "${if (result === source) "source" else result.toString()}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
}
class DecimalRemainderArrayOracleTest {
    @Test fun replacementPairArraysPreserveBoundsFailuresAndSecondElementIdentity() {
        for (row in jdk25DecimalRemainderArrayCases()) {
            assertEquals(row[5], arrayOutcome(row[0], row[1].toInt(), row[2], row[3], row[4].toInt()), row.take(5).toString())
        }
    }
}

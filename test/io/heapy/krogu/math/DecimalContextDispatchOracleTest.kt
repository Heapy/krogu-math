// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals

private class ContextProbe(text: String, val label: String, val mode: String, val trace: StringBuilder) : BigDecimal(text) {
    private var signCalls = 0
    private var precisionCalls = 0
    override fun signum(): Int {
        signCalls++
        trace.append("$label.sign($signCalls);")
        if (mode == "sign-first" || mode == "sign-second" && signCalls == 2)
            throw IllegalStateException("$label sign callback")
        return when {
            mode.startsWith("zero-precision:") -> 1
            mode.startsWith("sign:") -> mode.substringAfter(':').toInt()
            mode == "sign-changing" -> if (signCalls == 1) -super.signum() else super.signum()
            else -> super.signum()
        }
    }
    override fun precision(): Int {
        precisionCalls++
        trace.append("$label.precision;")
        if (mode == "precision") throw IllegalStateException("$label precision callback")
        return when {
            mode.startsWith("zero-precision:") -> mode.substringAfter(':').toInt()
            mode.startsWith("precision:") -> mode.substringAfter(':').toInt()
            mode == "precision-changing" -> super.precision() + precisionCalls
            else -> super.precision()
        }
    }
    private fun unexpected(name: String): Nothing {
        trace.append("$label.$name;"); throw IllegalStateException("$label unexpected $name")
    }
    override fun scale(): Int = unexpected("scale")
    override fun unscaledValue(): BigInteger = unexpected("unscaled")
    override fun abs(): BigDecimal = unexpected("abs")
    override fun negate(): BigDecimal = unexpected("negate")
    override fun divide(v: BigDecimal): BigDecimal {
        trace.append("$label.exact${if (v == null) "-null" else ""};"); return BigDecimal("7.00")
    }
    fun enter(v: BigDecimal, p: Int, rounding: String): BigDecimal =
        super.divide(v, MathContext(p, RoundingMode.valueOf(rounding)))
}
private fun contextOutcome(a: String, b: String, aMode: String, bMode: String, p: Int, rounding: String): String {
    val trace = StringBuilder()
    val source = ContextProbe(a, "A", aMode, trace)
    val divisor = if (bMode == "self") source else ContextProbe(b, "B", bMode, trace)
    return try {
        val result = source.enter(divisor, p, rounding)
        val calls = trace.toString()
        "${result.toString()}:${result.scale()}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}${if (e is NullPointerException) "" else ":${e.message}"}|$trace" }
}
class DecimalContextDispatchOracleTest {
    @Test fun contextDivisionPreservesOperandCallOrderAndFailurePrecedence() {
        val failures = mutableListOf<String>()
        val moved = mutableSetOf<Int>()
        for ((index, row) in jdk25DecimalContextDispatchCases().withIndex()) {
            if (DecimalBinary575Rows.contextDispatch(row)) { moved.add(index); continue }
            val actual = contextOutcome(row[0], row[1], row[2], row[3], row[4].toInt(), row[5])
            if (row[6] != actual) failures += row.take(6).toString() + " expected=" + row[6] + " actual=" + actual
        }
        assertEquals(emptyList<String>(), failures.take(12), failures.size.toString() + " mismatches")
        assertEquals(384, moved.size, "Exact null-input rows replayed through Java explicit super")
        assertEquals(DecimalBinary575Rows.contextDispatchOrdinals, moved)
    }
}

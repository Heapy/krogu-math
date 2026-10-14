// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals

private class IntegralResult(text: String, val trace: StringBuilder) : BigDecimal(text) {
    override fun precision(): Int { trace.append("Q.precision;"); return super.precision() }
    override fun scale(): Int { trace.append("Q.scale;"); return super.scale() }
    override fun signum(): Int { trace.append("Q.sign;"); return super.signum() }
    override fun setScale(n: Int, mode: RoundingMode): BigDecimal {
        trace.append("Q.set($n,$mode);"); return super.setScale(n, mode)
    }
    override fun setScale(n: Int, mode: Int): BigDecimal {
        trace.append("Q.setInt($n,$mode);"); return super.setScale(n, mode)
    }
    override fun setScale(n: Int): BigDecimal { trace.append("Q.set($n);"); return super.setScale(n) }
    override fun stripTrailingZeros(): BigDecimal { trace.append("Q.strip;"); return super.stripTrailingZeros() }
}
private class IntegralSource(text: String, val label: String, val mode: String, val trace: StringBuilder) : BigDecimal(text) {
    override fun precision(): Int { trace.append("$label.precision;"); return super.precision() }
    override fun scale(): Int { trace.append("$label.scale;"); return super.scale() }
    override fun signum(): Int { trace.append("$label.sign;"); return super.signum() }
    override fun abs(): BigDecimal { trace.append("$label.abs;"); return super.abs() }
    override fun compareTo(v: BigDecimal?): Int { trace.append("$label.compare;"); return super.compareTo(v) }
    override fun subtract(v: BigDecimal?): BigDecimal { trace.append("$label.subtract;"); return super.subtract(v) }
    override fun divideToIntegralValue(v: BigDecimal): BigDecimal {
        trace.append("$label.integral;"); return super.divideToIntegralValue(v)
    }
    override fun divide(v: BigDecimal, mc: MathContext): BigDecimal {
        trace.append("$label.divide(${mc.precision},${mc.roundingMode});")
        if (mode == "throw") throw IllegalStateException("divide callback")
        return if (mode == "source") this else IntegralResult(mode, trace)
    }
    fun enter(v: BigDecimal, p: Int): BigDecimal = if (p < 0) super.divideToIntegralValue(v)
        else super.divideToIntegralValue(v, MathContext(p, RoundingMode.UNNECESSARY))
}
private fun integralOutcome(a: String, b: String, p: Int, mode: String): String {
    val trace = StringBuilder()
    val source = IntegralSource(a, "A", mode, trace)
    val operand = IntegralSource(b, "B", mode, trace)
    return try {
        val result = source.enter(operand, p)
        val calls = trace.toString()
        "${result.toString()}:${result.scale()}:${when { result === source -> "source"; result === operand -> "divisor"; result is IntegralResult -> "quotient"; else -> "base" }}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
}
class DecimalIntegralDispatchOracleTest {
    @Test fun inheritedIntegralDivisionUsesVirtualContextDivision() {
        val failures = mutableListOf<String>()
        for (row in jdk25DecimalIntegralDispatchCases()) {
            val actual = integralOutcome(row[0], row[1], row[2].toInt(), row[3])
            if (row[4] != actual) failures += row.take(4).toString() + " expected=" + row[4] + " actual=" + actual
        }
        assertEquals(emptyList<String>(), failures.take(12), failures.size.toString() + " mismatches")
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals

private class PairQuotient(val trace: StringBuilder, val mode: String, val original: BigDecimal) : BigDecimal("3.0") {
    override fun multiply(v: BigDecimal?): BigDecimal {
        trace.append("multiply;")
        return when (mode) {
            "multiply-throw" -> throw IllegalStateException("multiply callback")
            "multiply-source" -> original
            "multiply-divisor" -> v ?: throw NullPointerException()
            else -> super.multiply(v)
        }
    }
}
private class PairSource(text: String, val trace: StringBuilder, val mode: String) : BigDecimal(text) {
    private fun quotient(): BigDecimal {
        if (mode == "integral-throw") throw IllegalStateException("integral callback")
        return PairQuotient(trace, mode, this)
    }
    override fun divideToIntegralValue(v: BigDecimal): BigDecimal {
        trace.append("integral;")
        return quotient()
    }
    override fun divideToIntegralValue(v: BigDecimal, mc: MathContext): BigDecimal {
        trace.append("integral(${mc.precision},${mc.roundingMode});")
        return quotient()
    }
    override fun divideAndRemainder(v: BigDecimal): Array<BigDecimal> {
        trace.append("pair;")
        if (mode == "pair-throw") throw IllegalStateException("pair callback")
        if (mode == "pair-replace") return arrayOf(BigDecimal("7.0"), this)
        return super.divideAndRemainder(v)
    }
    override fun divideAndRemainder(v: BigDecimal, mc: MathContext): Array<BigDecimal> {
        trace.append("pair(${mc.precision},${mc.roundingMode});")
        if (mode == "pair-throw") throw IllegalStateException("pair callback")
        if (mode == "pair-replace") return arrayOf(BigDecimal("7.0"), this)
        return super.divideAndRemainder(v, mc)
    }
    override fun subtract(v: BigDecimal?): BigDecimal {
        trace.append("subtract(${if (v === this) "source" else v!!.toString()});")
        return when (mode) {
            "subtract-throw" -> throw IllegalStateException("subtract callback")
            "subtract-source" -> this
            "subtract-product" -> v ?: throw NullPointerException()
            else -> super.subtract(v)
        }
    }
    fun enter(op: String, v: BigDecimal, mc: MathContext): Array<BigDecimal> = when (op) {
        "pair" -> super.divideAndRemainder(v)
        "pair-context" -> super.divideAndRemainder(v, mc)
        "remainder" -> arrayOf(super.remainder(v))
        else -> arrayOf(super.remainder(v, mc))
    }
}
private fun pairOutcome(text: String, divisor: String, mode: String, op: String, precision: Int, rounding: String): String {
    val trace = StringBuilder()
    val source = PairSource(text, trace, mode)
    val operand = BigDecimal(divisor)
    return try {
        val result = source.enter(op, operand, MathContext(precision, RoundingMode.valueOf(rounding)))
        val calls = trace.toString()
        result.joinToString(",") {
            "${it.toString()}:${it.scale()}:${when { it === source -> "source"; it === operand -> "divisor"; it is PairQuotient -> "quotient"; else -> "base" }}"
        } + "|" + calls
    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
}
class DecimalPairDispatchOracleTest {
    @Test fun compositionPreservesOverloadCallbacksExceptionsAndIdentity() {
        for (row in jdk25DecimalPairDispatchCases()) {
            assertEquals(row[6], pairOutcome(row[0], row[1], row[2], row[3], row[4].toInt(), row[5]), row.take(6).toString())
        }
    }
}

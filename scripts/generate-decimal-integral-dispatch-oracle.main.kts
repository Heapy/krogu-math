#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Public-JDK integral division callbacks. Usage: [--check]. */
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.io.File
check(Runtime.version().toString() in listOf("25.0.4+9", "25.0.4+9-LTS"))
check(args.all { it == "--check" })
class IntegralResult(text: String, val trace: StringBuilder) : BigDecimal(text) {
    override fun toByte(): Byte = super.toInt().toByte()
    override fun toShort(): Short = super.toInt().toShort()
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
class IntegralSource(text: String, val label: String, val mode: String, val trace: StringBuilder) : BigDecimal(text) {
    override fun toByte(): Byte = super.toInt().toByte()
    override fun toShort(): Short = super.toInt().toShort()
    override fun precision(): Int { trace.append("$label.precision;"); return super.precision() }
    override fun scale(): Int { trace.append("$label.scale;"); return super.scale() }
    override fun signum(): Int { trace.append("$label.sign;"); return super.signum() }
    override fun abs(): BigDecimal { trace.append("$label.abs;"); return super.abs() }
    override fun compareTo(v: BigDecimal): Int { trace.append("$label.compare;"); return super.compareTo(v) }
    override fun subtract(v: BigDecimal): BigDecimal { trace.append("$label.subtract;"); return super.subtract(v) }
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
class IntegralMetadata(text: String, val label: String, val digits: Int, val reportedScale: Int, val trace: StringBuilder) : BigDecimal(text) {
    override fun toByte(): Byte = super.toInt().toByte()
    override fun toShort(): Short = super.toInt().toShort()
    override fun signum(): Int { trace.append("$label.sign;"); return super.signum() }
    override fun precision(): Int { trace.append("$label.precision;"); return digits }
    override fun scale(): Int { trace.append("$label.scale;"); return reportedScale }
    override fun divide(v: BigDecimal, mc: MathContext): BigDecimal {
        trace.append("$label.divide(${mc.precision},${mc.roundingMode});")
        throw IllegalStateException("divide callback")
    }
}
fun metadataOutcome(a: Int, b: Int, aScale: Int, bScale: Int): String {
    val trace = StringBuilder()
    val source = IntegralMetadata("10.0", "A", a, aScale, trace)
    val divisor = IntegralMetadata("2.0", "B", b, bScale, trace)
    return try { source.divideToIntegralValue(divisor).toString() + "|$trace" }
        catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
}
fun integralOutcome(a: String, b: String, p: Int, mode: String): String {
    val trace = StringBuilder()
    val source = IntegralSource(a, "A", mode, trace)
    val operand = IntegralSource(b, "B", mode, trace)
    return try {
        val result = source.enter(operand, p)
        val calls = trace.toString()
        "${result.toString()}:${result.scale()}:${when { result === source -> "source"; result === operand -> "divisor"; result is IntegralResult -> "quotient"; else -> "base" }}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
}
val rows = mutableListOf<List<String>>()
for (a in listOf("0.00", "0.1", "1.00", "-1.00", "10.50", "1000", "1E+4"))
    for (b in listOf("0.00", "2.0", "-2.0", "1E+3"))
        for (p in listOf(-1, 0, 1, 2, 7))
            for (mode in listOf("throw", "7.9", "-7.9", "0.00", "1E+4", "1E+1", "5E+2", "0", "7", "source"))
                rows += listOf(a, b, p.toString(), mode, integralOutcome(a, b, p, mode))
val metadata = mutableListOf<List<String>>()
val reportedPrecisions = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -10, -3, -2, -1, 0, 1, 2, 3, 9, 10, Int.MAX_VALUE - 1, Int.MAX_VALUE)
for (a in reportedPrecisions) for (b in reportedPrecisions)
    for ((aScale, bScale) in listOf(1 to 1, 0 to 1, -1 to 1, Int.MIN_VALUE to Int.MAX_VALUE, Int.MAX_VALUE to Int.MIN_VALUE, 0 to Int.MIN_VALUE))
        metadata += listOf(a.toString(), b.toString(), aScale.toString(), bScale.toString(), metadataOutcome(a, b, aScale, bScale))
val generated = buildString {
    appendLine("// SPDX-License-Identifier: Apache-2.0")
    appendLine("// Generated by scripts/generate-decimal-integral-dispatch-oracle.main.kts; JDK 25.0.4+9.")
    appendLine("package io.heapy.krogu.math")
    appendLine("internal fun jdk25DecimalIntegralDispatchCases(): List<Array<String>> = listOf(")
    for (chunk in rows.chunked(100)) {
        appendLine("    \"\"\"")
        for (row in chunk) appendLine(row.joinToString("\t"))
        appendLine("    \"\"\".trimIndent(),")
    }
    appendLine(").flatMap { it.lines().map { line -> line.split('\\t').toTypedArray() } }")
    appendLine("internal fun jdk25DecimalIntegralMetadataCases(): List<Array<String>> = listOf(")
    for (chunk in metadata.chunked(100)) {
        appendLine("    \"\"\"")
        for (row in chunk) appendLine(row.joinToString("\t"))
        appendLine("    \"\"\".trimIndent(),")
    }
    appendLine(").flatMap { it.lines().map { line -> line.split('\\t').toTypedArray() } }")
}
val target = File("test/io/heapy/krogu/math/Jdk25DecimalIntegralDispatchResults.kt")
if ("--check" in args) check(target.exists() && target.readText() == generated) { "Integral division dispatch oracle differs" }
else target.writeText(generated)
println("${if ("--check" in args) "Verified" else "Generated"} ${rows.size} decimal integral dispatch cases and ${metadata.size} metadata cases")

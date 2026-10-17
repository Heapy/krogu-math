#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Public-JDK context-division call order and callback failures. Usage: [--check]. */
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.io.File
check(Runtime.version().toString() in listOf("25.0.4+9", "25.0.4+9-LTS"))
check(args.all { it == "--check" })
class ContextProbe(text: String, val label: String, val mode: String, val trace: StringBuilder) : BigDecimal(text) {
    override fun toByte(): Byte = super.toInt().toByte()
    override fun toShort(): Short = super.toInt().toShort()
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
    override fun divide(v: BigDecimal?): BigDecimal {
        trace.append("$label.exact${if (v == null) "-null" else ""};"); return BigDecimal("7.00")
    }
    fun enter(v: BigDecimal?, p: Int, rounding: String): BigDecimal =
        super.divide(v, if (p < 0) null else MathContext(p, RoundingMode.valueOf(rounding)))
}
fun contextOutcome(a: String, b: String, aMode: String, bMode: String, p: Int, rounding: String): String {
    val trace = StringBuilder()
    val source = ContextProbe(a, "A", aMode, trace)
    val divisor = if (bMode == "self") source else if (bMode == "null") null else ContextProbe(b, "B", bMode, trace)
    return try {
        val result = source.enter(divisor, p, rounding)
        val calls = trace.toString()
        "${result.toString()}:${result.scale()}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}${if (e is NullPointerException) "" else ":${e.message}"}|$trace" }
}
val inputs = listOf(
    "0.00" to "0.0", "0.00" to "2.0", "10.50" to "0.0", "10.50" to "2.0", "-10.50" to "2.0",
    "10.50" to "-2.0", "-10.50" to "-2.0", "1" to "3", "9.99" to "1", "1000" to "0.001",
    "1E-20" to "3E+20", "1E+20" to "3E-20", "123456789012345678901234567890.123" to "987654321098765432109876543210.9"
)
val rows = mutableListOf<List<String>>()
for ((a, b) in inputs) for (aMode in listOf("delegate", "sign-first", "sign-second", "precision"))
    for (bMode in listOf("delegate", "sign-first", "sign-second", "precision"))
        for (p in listOf(0, 1, 2, 7)) for (rounding in RoundingMode.entries) {
            rows += listOf(a, b, aMode, bMode, p.toString(), rounding.name, contextOutcome(a, b, aMode, bMode, p, rounding.name))
        }
for (a in listOf("0.00", "1", "10.50", "-10.50", "1E+20", "123456789012345678901234567890.123"))
    for (aMode in listOf("delegate", "sign-first", "sign-second", "precision"))
        for (p in listOf(0, 1, 2, 7)) for (rounding in RoundingMode.entries)
            rows += listOf(a, a, aMode, "self", p.toString(), rounding.name, contextOutcome(a, a, aMode, "self", p, rounding.name))
for (a in listOf("0.00", "10.50", "-10.50"))
    for (aMode in listOf("delegate", "sign-first", "sign-second", "precision"))
        for (bMode in listOf("delegate", "null")) for (p in listOf(-1, 0, 2)) for (rounding in RoundingMode.entries)
            rows += listOf(a, "2.0", aMode, bMode, p.toString(), rounding.name, contextOutcome(a, "2.0", aMode, bMode, p, rounding.name))
val replacementModes = listOf("sign:-1", "sign:0", "sign:1", "sign-changing",
    "precision:-1", "precision:0", "precision:1", "precision:2", "precision:7", "precision:19", "precision-changing")
for ((a, b) in inputs) for (mode in replacementModes) for (source in listOf(true, false))
    for (p in listOf(1, 2, 7)) for (rounding in RoundingMode.entries) {
        val aMode = if (source) mode else "delegate"
        val bMode = if (source) "delegate" else mode
        rows += listOf(a, b, aMode, bMode, p.toString(), rounding.name, contextOutcome(a, b, aMode, bMode, p, rounding.name))
    }
for ((a, b) in inputs.take(7))
    for (aMode in listOf("sign:-1", "sign:0", "sign:1", "precision:0", "precision:1", "precision:7"))
        for (bMode in listOf("sign:-1", "sign:0", "sign:1", "precision:0", "precision:1", "precision:7"))
            for (p in listOf(1, 2)) for (rounding in RoundingMode.entries)
                rows += listOf(a, b, aMode, bMode, p.toString(), rounding.name, contextOutcome(a, b, aMode, bMode, p, rounding.name))
for ((a, b) in listOf("0.00" to "0.0", "0E+4" to "0E-4", "0" to "0"))
    for (aDigits in listOf(-1, 0, 1, 2, 17, 18, 19, 20))
        for (bDigits in listOf(-1, 0, 1, 2, 17, 18, 19, 20))
            for (p in listOf(1, 17, 18, 19)) for (rounding in RoundingMode.entries) {
                val aMode = "zero-precision:$aDigits"
                val bMode = "zero-precision:$bDigits"
                rows += listOf(a, b, aMode, bMode, p.toString(), rounding.name, contextOutcome(a, b, aMode, bMode, p, rounding.name))
            }
val compactInputs = listOf("9223372036854775807" to "3", "1" to "9223372036854775807",
    "-9223372036854775808" to "3", "9223372036854775808" to "3",
    "999999999999999999" to "999999999999999998", "999999999999999999" to "1",
    "1" to "999999999999999999", "9.99999999999999999" to "0.1") +
    listOf("922337203685477580", "922337203685477581", "9223372036854775807", "999999999999999999",
        "100000000000000000", "10000000000000000", "-9223372036854775807", "-999999999999999999")
        .flatMap { a -> listOf("1", "3", "10", "30", "100").map { b -> a to b } }
val compactModes = listOf("precision:-1", "precision:0", "precision:1", "precision:2", "precision:17",
    "precision:18", "precision:19", "precision:20", "precision-changing", "sign:1", "sign:-1")
for ((a, b) in compactInputs) for (mode in compactModes) for (source in listOf(true, false))
    for (p in listOf(1, 2, 7, 17, 18, 19)) for (rounding in RoundingMode.entries) {
        val aMode = if (source) mode else "delegate"
        val bMode = if (source) "delegate" else mode
        rows += listOf(a, b, aMode, bMode, p.toString(), rounding.name, contextOutcome(a, b, aMode, bMode, p, rounding.name))
    }
val generated = buildString {
    appendLine("// SPDX-License-Identifier: Apache-2.0")
    appendLine("// Generated by scripts/generate-decimal-context-dispatch-oracle.main.kts; JDK 25.0.4+9.")
    appendLine("package io.heapy.krogu.math")
    appendLine("internal fun jdk25DecimalContextDispatchCases(): List<Array<String>> = listOf(")
    for (chunk in rows.chunked(100)) {
        appendLine("    \"\"\"")
        for (row in chunk) appendLine(row.joinToString("\t"))
        appendLine("    \"\"\".trimIndent(),")
    }
    appendLine(").flatMap { it.lines().map { line -> line.split('\\t').toTypedArray() } }")
}
val target = File("test/io/heapy/krogu/math/Jdk25DecimalContextDispatchResults.kt")
if ("--check" in args) check(target.exists() && target.readText() == generated) { "Context division dispatch oracle differs" }
else target.writeText(generated)
println("${if ("--check" in args) "Verified" else "Generated"} ${rows.size} decimal context dispatch cases")

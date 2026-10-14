#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/**
 * Public-JDK comparison for replacement decimal precision near compact boundaries.
 * Build ./gradlew jvmJar first, then run:
 * kotlinr -classpath build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar scripts/audit-decimal-context-compact-overflow.main.kts [--check]
 * Default reports differences; --check fails when any difference remains.
 */
import java.io.File
import io.heapy.krogu.math.*
import java.math.BigDecimal as JDecimal
import java.math.BigInteger as JInteger
import java.math.MathContext as JContext
import java.math.RoundingMode as JMode
check(Runtime.version().toString() in listOf("25.0.4+9", "25.0.4+9-LTS"))
check(args.all { it == "--check" })

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
    override fun divide(v: BigDecimal?): BigDecimal {
        trace.append("$label.exact${if (v == null) "-null" else ""};"); return BigDecimal("7.00")
    }
    fun enter(v: BigDecimal?, p: Int, rounding: String): BigDecimal =
        super.divide(v, if (p < 0) null else MathContext(p, RoundingMode.valueOf(rounding)))
}
private fun contextOutcome(a: String, b: String, aMode: String, bMode: String, p: Int, rounding: String): String {
    val trace = StringBuilder()
    val source = ContextProbe(a, "A", aMode, trace)
    val divisor = if (bMode == "self") source else if (bMode == "null") null else ContextProbe(b, "B", bMode, trace)
    return try {
        val result = source.enter(divisor, p, rounding)
        val calls = trace.toString()
        "${result.toString()}:${result.scale()}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}${if (e is NullPointerException) "" else ":${e.message}"}|$trace" }
}

class ReferenceProbe(text: String, val label: String, val mode: String, val trace: StringBuilder) : JDecimal(text) {
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
    override fun unscaledValue(): JInteger = unexpected("unscaled")
    override fun abs(): JDecimal = unexpected("abs")
    override fun negate(): JDecimal = unexpected("negate")
    override fun divide(v: JDecimal?): JDecimal {
        trace.append("$label.exact${if (v == null) "-null" else ""};"); return JDecimal("7.00")
    }
    fun enter(v: JDecimal?, p: Int, rounding: String): JDecimal =
        super.divide(v, if (p < 0) null else JContext(p, JMode.valueOf(rounding)))
}
fun referenceOutcome(a: String, b: String, aMode: String, bMode: String, p: Int, rounding: String): String {
    val trace = StringBuilder()
    val source = ReferenceProbe(a, "A", aMode, trace)
    val divisor = if (bMode == "self") source else if (bMode == "null") null else ReferenceProbe(b, "B", bMode, trace)
    return try {
        val result = source.enter(divisor, p, rounding)
        val calls = trace.toString()
        "${result.toString()}:${result.scale()}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}${if (e is NullPointerException) "" else ":${e.message}"}|$trace" }
}

val inputs = listOf("9223372036854775807" to "3", "1" to "9223372036854775807",
    "-9223372036854775808" to "3", "9223372036854775808" to "3",
    "999999999999999999" to "999999999999999998", "999999999999999999" to "1",
    "1" to "999999999999999999", "9.99999999999999999" to "0.1") +
    listOf("922337203685477580", "922337203685477581", "9223372036854775807", "999999999999999999",
        "100000000000000000", "10000000000000000", "-9223372036854775807", "-999999999999999999")
        .flatMap { a -> listOf("1", "3", "10", "30", "100").map { b -> a to b } }
val modes = listOf("precision:-1", "precision:0", "precision:1", "precision:2", "precision:17", "precision:18", "precision:19", "precision:20", "precision-changing", "sign:1", "sign:-1")
val differences = mutableListOf<String>()
var count = 0
for ((a,b) in inputs) for (mode in modes) for (source in listOf(true,false))
    for (p in listOf(1,2,7,17,18,19)) for (rounding in RoundingMode.entries) {
        val aMode = if(source) mode else "delegate"
        val bMode = if(source) "delegate" else mode
        val expected = referenceOutcome(a,b,aMode,bMode,p,rounding.name)
        val actualResult = contextOutcome(a,b,aMode,bMode,p,rounding.name)
        count++
        if(expected != actualResult) differences += listOf(a,b,aMode,bMode,p,rounding.name).toString() + " expected=" + expected + " actual=" + actualResult
    }
val report = File("build/reports/compatibility/decimal-context-compact-overflow.txt")
report.parentFile.mkdirs()
report.writeText(differences.joinToString("\n"))
println("Compared " + count + "; differences=" + differences.size)
differences.take(12).forEach(::println)
println("Report: " + report.path)
if ("--check" in args) check(differences.isEmpty()) { "Compact context division differs from the pinned JDK" }

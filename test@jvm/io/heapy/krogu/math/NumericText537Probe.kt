// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.io.File
import java.lang.management.ManagementFactory
import java.security.MessageDigest

/** Same compiled executable against distinct baseline/candidate artifacts. No timing assertions. */
object NumericText537Probe {
    @Volatile private var sink: Any? = null
    private val memory = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
    private val ring = arrayOfNulls<Any>(32)
    private var cursor = 0
    private class Case(val id: String, val prepare: (Int) -> (() -> Any), val verify: () -> Unit)

    @JvmStatic fun main(args: Array<String>) {
        require(args.size == 7) { "baseline|candidate main.jar main-sha probe.jar probe-sha fork forward|reverse" }
        require(args[0] in listOf("baseline", "candidate"))
        require(args[6] in listOf("forward", "reverse"))
        check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS" && System.getProperty("java.vendor") == "BellSoft")
        val main = File(args[1]).canonicalFile
        val probe = File(args[3]).canonicalFile
        fun sha(file: File): String = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
            .joinToString("") { "%02x".format(it) }
        fun verifyBindings() {
            check(main != probe && sha(main) == args[2] && sha(probe) == args[4])
            check(File(BigInteger::class.java.protectionDomain.codeSource.location.toURI()).canonicalFile == main)
            check(File(BigDecimal::class.java.protectionDomain.codeSource.location.toURI()).canonicalFile == main)
            check(File(NumericText537Probe::class.java.protectionDomain.codeSource.location.toURI()).canonicalFile == probe)
        }
        verifyBindings()
        check(memory.isThreadAllocatedMemorySupported)
        memory.isThreadAllocatedMemoryEnabled = true
        println("# main_sha256=${args[2]} probe_sha256=${args[4]} fork=${args[5]} order=${args[6]}")
        val cases = cases(args[0] == "candidate").let { if (args[6] == "reverse") it.reversed() else it }
        check(cases.map { it.id }.toSet().size == cases.size)
        println("variant,fork,case,sample,operations,ns_per_op,allocated_bytes_per_op")
        for (case in cases) {
            case.verify() // Public JDK/text sanity outside all timing and allocation counters.
            var count = 1
            while (true) {
                val elapsed = measure(count, case.prepare(count)).first
                if (elapsed >= 20_000_000L || count >= 2048) break
                count *= 2
            }
            // Wall setup can dominate cold pools: accumulate TIMED warmup, not setup duration.
            var warmedNs = 0L
            var warmBatches = 0
            while (warmBatches < 5 || warmedNs < 200_000_000L) {
                check(warmBatches < 100_000) { "Warmup batch cap reached before 200ms; retain failed diagnostic" }
                warmedNs += measure(count, case.prepare(count)).first
                warmBatches++
            }
            repeat(5) { sample ->
                // For cold format, every pool is constructed here, outside BOTH counters.
                val operation = case.prepare(count)
                val (elapsed, allocated) = measure(count, operation)
                println("${args[0]},${args[5]},${case.id},$sample,$count,${elapsed.toDouble() / count},${allocated.toDouble() / count}")
            }
        }
        check(ring.any { it != null })
        verifyBindings()
        println("# artifact_recheck=PASS cases=${cases.size} result_escape=PASS")
    }

    private fun measure(count: Int, operation: () -> Any): Pair<Long, Long> {
        val thread = Thread.currentThread().threadId()
        val allocated = memory.getThreadAllocatedBytes(thread)
        val start = System.nanoTime()
        repeat(count) {
            val result = operation()
            ring[cursor++ and 31] = result
            sink = result
        }
        val elapsed = System.nanoTime() - start
        return elapsed to (memory.getThreadAllocatedBytes(thread) - allocated)
    }

    private fun cases(candidate: Boolean): List<Case> = buildList {
        for (digits in listOf(9, 77, 309, 1234)) {
            val text = "7" + buildString { repeat(digits - 1) { append(('0'.code + (it * 7 + 3) % 10).toChar()) } }
            val chars = "[$text]".toCharArray()
            val sequence = StringBuilder("[$text]")
            val expected = java.math.BigInteger(text).toString()
            fun parsed(mode: String): BigInteger = when (mode) {
                "string" -> BigInteger(text)
                "chars" -> if (candidate) BigInteger.parseChars(chars, 1, text.length, 10)
                    else BigInteger(chars.concatToString(1, chars.size - 1))
                else -> if (candidate) BigInteger.parseText(sequence, 1, text.length, 10)
                    else BigInteger(sequence.substring(1, sequence.length - 1))
            }
            for (mode in listOf("string", "chars", "sequence")) add(Case("integer/$digits/parse-$mode",
                { _ -> { parsed(mode) } }, { check(parsed(mode).toString() == expected) }))
            for (radix in listOf(3, 10, 16)) {
                val expectedRadix = java.math.BigInteger(text).toString(radix)
                add(Case("integer/$digits/cold-format-$radix", { count ->
                    val values = Array(count) { BigInteger(text) }
                    var index = 0
                    val operation: () -> Any = { values[index++].toString(radix) }
                    operation
                }, { check(BigInteger(text).toString(radix) == expectedRadix) }))
                val value = BigInteger(text)
                add(Case("integer/$digits/buffer-$radix", { _ ->
                    val buffer = CharArray(expectedRadix.length)
                    val operation: () -> Any = { if (candidate) value.writeTo(buffer, 0, buffer.size, radix)
                      else value.toString(radix).toCharArray(buffer, 0, 0, buffer.size)
                      buffer }
                    operation
                }, {
                    val buffer = CharArray(expectedRadix.length)
                    if (candidate) value.writeTo(buffer, 0, buffer.size, radix)
                    else value.toString(radix).toCharArray(buffer, 0, 0, buffer.size)
                    check(buffer.concatToString() == expectedRadix)
                }))
            }
        }
        val decimals = listOf("small" to "12345.6700", "wide" to ("7".repeat(309) + ".00100"),
            "negative-scale" to "123456789E+77", "large-scale" to "123456789E-1234")
        for ((name, text) in decimals) {
            val expected = java.math.BigDecimal(text)
            val chars = "[$text]".toCharArray()
            val sequence = StringBuilder("[$text]")
            fun parsed(mode: String): BigDecimal = when (mode) {
                "string" -> BigDecimal(text)
                "chars" -> BigDecimal(chars, 1, text.length)
                else -> if (candidate) BigDecimal.parseText(sequence, 1, text.length, MathContext.UNLIMITED)
                    else BigDecimal(sequence.substring(1, sequence.length - 1))
            }
            for (mode in listOf("string", "chars", "sequence")) add(Case("decimal/$name/parse-$mode",
                { _ -> { parsed(mode) } }, { check(parsed(mode).toString() == expected.toString()) }))
            add(Case("decimal/$name/cold-format", { count ->
                val values = Array(count) { BigDecimal(text) }
                var index = 0
                val operation: () -> Any = { values[index++].toString() }
                operation
            }, { check(BigDecimal(text).toString() == expected.toString()) }))
            add(Case("decimal/$name/construction-format", { _ -> { BigDecimal(text).toString() } },
                { check(BigDecimal(text).toString() == expected.toString()) }))
            val value = BigDecimal(text)
            value.toString() // Explicit cached control, matching the old 527 observation state.
            add(Case("decimal/$name/cached-format-control", { _ -> { value.toString() } },
                { check(value.toString() == expected.toString()) }))
            // Keep new API types behind candidate-only call sites: the baseline has no enum.
            for (format in listOf("CANONICAL", "PLAIN", "ENGINEERING")) {
                // Any keeps generated lambda descriptors loadable without the new enum on baseline.
                val candidateFormat: Any? = if (candidate) DecimalTextFormat.valueOf(format) else null
                val expectedText = when (format) {
                    "CANONICAL" -> expected.toString()
                    "PLAIN" -> expected.toPlainString()
                    else -> expected.toEngineeringString()
                }
                fun legacy(): String = when (format) {
                    "CANONICAL" -> value.toString()
                    "PLAIN" -> value.toPlainString()
                    else -> value.toEngineeringString()
                }
                add(Case("decimal/$name/buffer-${format.lowercase()}", { _ ->
                    val buffer = CharArray(expectedText.length)
                    val operation: () -> Any = { if (candidate) value.writeTo(buffer, 0, buffer.size, candidateFormat as DecimalTextFormat)
                      else legacy().toCharArray(buffer, 0, 0, buffer.size)
                      buffer }
                    operation
                }, {
                    val buffer = CharArray(expectedText.length)
                    if (candidate) value.writeTo(buffer, 0, buffer.size, candidateFormat as DecimalTextFormat)
                    else legacy().toCharArray(buffer, 0, 0, buffer.size)
                    check(buffer.concatToString() == expectedText)
                }))
            }
        }
    }
}

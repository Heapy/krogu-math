#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Signed arithmetic and subtraction dispatch from public JDK APIs. Usage: [--check]. */
import java.io.File
import java.math.BigInteger
import java.util.Random

check(Runtime.version().toString() in listOf("25.0.4+9", "25.0.4+9-LTS")) { "Requires JDK 25.0.4+9" }
check(args.all { it == "--check" })

fun hex(value: BigInteger): String = value.toByteArray().joinToString("") {
    (it.toInt() and 255).toString(16).padStart(2, '0')
}
val rows = linkedMapOf<Pair<BigInteger, BigInteger>, List<String>>()
fun record(pattern: String, left: BigInteger, right: BigInteger) {
    check(left.signum() >= 0 && right.signum() > 0)
    val size = maxOf(left.bitLength(), right.bitLength())
    for (mask in 0..3) {
        val a = if (mask and 1 == 0) left else left.negate()
        val b = if (mask and 2 == 0) right else right.negate()
        val qr = a.divideAndRemainder(b)
        rows.putIfAbsent(a to b, listOf("$size:$pattern:$mask", hex(a), hex(b), hex(a.add(b)),
            hex(a.subtract(b)), hex(a.multiply(b)), hex(qr[0]), hex(qr[1])))
    }
}
val boundaries = listOf(29, 30, 31, 59, 60, 61, 89, 90, 91, 119, 120, 121, 239, 240, 241,
    511, 512, 513, 1023, 1024, 1025, 2047, 2048, 2049, 4095, 4096, 4097, 8192, 16384)
val word = BigInteger.ONE.shiftLeft(30)
for (bits in boundaries) {
    val dense = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE)
    record("dense-square-$bits", dense, dense)
    record("single-word-$bits", dense, word.subtract(BigInteger.ONE))
    record("two-word-$bits", dense, word.add(BigInteger.ONE))
}
val random = Random(0x4b524f4755415249L)
for (bits in listOf(1024, 2048, 4096)) repeat(3) { index ->
    record("seeded-$bits-$index", BigInteger(bits, random).setBit(bits - 1),
        BigInteger(bits / 2, random).setBit(bits / 2 - 1))
}
for (bits in listOf(1024, 4096, 8192)) {
    var alternating = BigInteger.ZERO
    for (offset in 0 until bits step 60)
        alternating = alternating.or(word.subtract(BigInteger.ONE).shiftLeft(offset))
    alternating = alternating.and(BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE)).setBit(bits - 1)
    record("alternating-$bits", alternating, alternating.shiftRight(30).setBit(0))
}
record("zero-unit", BigInteger.ZERO, BigInteger.ONE)
record("zero-large", BigInteger.ZERO, BigInteger.ONE.shiftLeft(4096).subtract(BigInteger.ONE))

fun subtractionOutcome(leftText: String, rightText: String, addMode: String, negateMode: String, self: Boolean = false): String {
    val trace = StringBuilder()
    val sourceReplacement = BigInteger("7")
    val operandReplacement = BigInteger("7")
    val left = object : BigInteger(leftText) {
        override fun toByte(): Byte = super.toInt().toByte()
        override fun toShort(): Short = super.toInt().toShort()
        override fun add(value: BigInteger): BigInteger {
            trace.append("add;")
            return when (addMode) {
                "source" -> this
                "fixed" -> sourceReplacement
                "throw" -> throw IllegalStateException("add callback")
                else -> super.add(value)
            }
        }
        override fun negate(): BigInteger {
            trace.append("sourceNegate;")
            return when (negateMode) {
                "operand" -> this
                "fixed" -> operandReplacement
                "throw" -> throw IllegalStateException("source negate callback")
                else -> super.negate()
            }
        }
    }
    val right = if (self) left else object : BigInteger(rightText) {
        override fun toByte(): Byte = super.toInt().toByte()
        override fun toShort(): Short = super.toInt().toShort()
        override fun negate(): BigInteger {
            trace.append("negate;")
            return when (negateMode) {
                "operand" -> this
                "fixed" -> operandReplacement
                "throw" -> throw IllegalStateException("negate callback")
                else -> super.negate()
            }
        }
    }
    return try {
        val result: BigInteger? = left.subtract(right)
        val calls = trace.toString()
        val text = when {
            result === left -> leftText
            result === right -> rightText
            else -> result.toString()
        }
        "$text|${result === left}|${result === right}|${result === sourceReplacement}|${result === operandReplacement}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
}
val subtractionRows = mutableListOf<List<String>>()
val subtractionInputs = listOf("0", "1", "-1", "1073741824", "-1073741824", "123456789012345678901234567890", "-123456789012345678901234567890")
for (left in subtractionInputs) for (right in subtractionInputs)
    for (addMode in listOf("delegate", "source", "fixed", "throw"))
        for (negateMode in listOf("delegate", "operand", "fixed", "throw"))
            subtractionRows += listOf(left, right, addMode, negateMode, subtractionOutcome(left, right, addMode, negateMode), "distinct")
for (text in subtractionInputs) for (addMode in listOf("delegate", "source", "fixed", "throw"))
    for (negateMode in listOf("delegate", "operand", "fixed", "throw"))
        subtractionRows += listOf(text, text, addMode, negateMode, subtractionOutcome(text, text, addMode, negateMode, true), "self")

val source = buildString {
    appendLine("// SPDX-License-Identifier: Apache-2.0")
    appendLine("// Generated by scripts/generate-integer-arithmetic-oracle.main.kts; JDK 25.0.4+9.")
    appendLine("package io.heapy.krogu.math")
    appendLine("internal fun jdk25IntegerArithmeticCases(): List<Array<String>> = buildList {")
    val chunks = rows.values.chunked(20)
    for (index in chunks.indices) appendLine("    addAll(integerArithmeticBatch$index())")
    appendLine("}")
    for ((index, chunk) in chunks.withIndex()) {
        appendLine("private fun integerArithmeticBatch$index(): List<Array<String>> = listOf(")
        for (row in chunk) appendLine("    arrayOf(${row.joinToString(", ") { "\"$it\"" }}),")
        appendLine(")")
    }
    appendLine("internal fun jdk25IntegerSubtractDispatchCases(): List<Array<String>> = buildList {")
    val subtractionChunks = subtractionRows.chunked(40)
    for (index in subtractionChunks.indices) appendLine("    addAll(integerSubtractDispatchBatch$index())")
    appendLine("}")
    for ((index, chunk) in subtractionChunks.withIndex()) {
        appendLine("private fun integerSubtractDispatchBatch$index(): List<Array<String>> = listOf(")
        for (row in chunk) appendLine("    arrayOf(${row.joinToString(", ") { "\"$it\"" }}),")
        appendLine(")")
    }
}
val target = File("test/io/heapy/krogu/math/Jdk25IntegerArithmeticResults.kt")
if ("--check" in args) {
    check(target.exists() && target.readText() == source) { "Integer arithmetic oracle differs" }
    println("Verified ${rows.size} signed integer arithmetic cases and ${subtractionRows.size} subtraction dispatch cases")
} else {
    target.writeText(source)
    println("Generated ${rows.size} signed integer arithmetic cases and ${subtractionRows.size} subtraction dispatch cases")
}

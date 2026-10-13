#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Generate portable behavioral fixtures using only the public JDK 25.0.4+9 API.
 * Usage (from repository root): kotlinr scripts/generate-oracle.main.kts [--check]
 * No third-party dependencies or implementation-source access.
 */
import java.io.File
import java.math.BigInteger
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Random

check(Runtime.version().toString() == "25.0.4+9-LTS" || Runtime.version().toString() == "25.0.4+9") {
    "Oracle requires JDK 25.0.4+9, got ${Runtime.version()}"
}
check(args.all { it == "--check" }) { "Usage: generate-oracle.main.kts [--check]" }
val random = Random(0x4b524f47554d4154L)
val rows = mutableListOf<List<String>>()
fun outcome(block: () -> Any?): String = try {
    val result = block()
    if (result is Array<*>) result.joinToString("|") else result.toString()
} catch (e: Exception) { "!${e.javaClass.simpleName}" }
fun record(kind: String, op: String, a: String, b: String = "0", p: Int = 0, mode: RoundingMode = RoundingMode.HALF_EVEN, block: () -> Any?) {
    rows.add(listOf(kind, op, a, b, p.toString(), mode.name, outcome(block)))
}
val integerInputs = listOf("0", "1", "-1", "127", "128", "-128", "-129", "2147483647", "-2147483648", "9223372036854775807", "-9223372036854775808") +
    List(65) { BigInteger(ByteArray(1 + random.nextInt(64)).also(random::nextBytes)).toString() }
for (a in integerInputs) {
    val x = BigInteger(a)
    val y = BigInteger(ByteArray(1 + random.nextInt(32)).also(random::nextBytes))
    val b = y.toString()
    record("I", "add", a, b) { x.add(y) }
    record("I", "subtract", a, b) { x.subtract(y) }
    record("I", "multiply", a, b) { x.multiply(y) }
    record("I", "divide", a, b) { x.divideAndRemainder(y) }
    record("I", "gcd", a, b) { x.gcd(y) }
    record("I", "and", a, b) { x.and(y) }
    record("I", "or", a, b) { x.or(y) }
    record("I", "xor", a, b) { x.xor(y) }
    record("I", "andNot", a, b) { x.andNot(y) }
    record("I", "not", a) { x.not() }
    record("I", "bits", a) { "${x.bitLength()}|${x.bitCount()}|${x.lowestSetBit}" }
    val shift = random.nextInt(1000) - 500
    record("I", "shift", a, p = shift) { x.shiftLeft(shift) }
    record("I", "sqrt", a) { x.sqrtAndRemainder() }
    record("I", "double", a) { x.toDouble().toBits() }
    record("I", "float", a) { x.toFloat().toBits() }
    record("I", "bytes", a) { x.toByteArray().joinToString(",") }
    record("I", "longExact", a) { x.longValueExact() }
    record("I", "intExact", a) { x.intValueExact() }
    record("I", "shortExact", a) { x.shortValueExact() }
    record("I", "byteExact", a) { x.byteValueExact() }
    val radix = 2 + random.nextInt(35)
    record("I", "radix", a, p = radix) { x.toString(radix) }
    record("I", "pow", a, p = 3) { x.pow(3) }
    for (distance in listOf(0, 1, 29, 30, 31, 63, 64, 127, 128, 511, 512)) {
        record("I", "testBit", a, p = distance) { x.testBit(distance) }
        record("I", "setBit", a, p = distance) { x.setBit(distance) }
        record("I", "clearBit", a, p = distance) { x.clearBit(distance) }
        record("I", "flipBit", a, p = distance) { x.flipBit(distance) }
    }
}
// Integer neighbors of binary rounding midpoints, including the finite/infinity edge.
for (precision in listOf(24, 53)) {
    val leading = BigInteger.ONE.shiftLeft(precision - 1)
    val significands = listOf(leading, leading.add(BigInteger.ONE), leading.add(BigInteger.TWO),
        BigInteger.ONE.shiftLeft(precision).subtract(BigInteger.ONE))
    for (exponent in listOf(24, 29, 30, 31, 53, 59, 60, 61, 62, 63, 64, 65, 126, 127, 128, 254, 511, 1022, 1023, 1024)) {
        if (exponent < precision) continue
        val shift = exponent - precision + 1
        for (significand in significands) {
            val midpoint = significand.shiftLeft(shift).add(BigInteger.ONE.shiftLeft(shift - 1))
            for (offset in -1L..1L) for (negative in listOf(false, true)) {
                val magnitude = midpoint.add(BigInteger.valueOf(offset))
                val x = if (negative) magnitude.negate() else magnitude
                record("I", "float", x.toString()) { x.toFloat().toBits() }
                record("I", "double", x.toString()) { x.toDouble().toBits() }
            }
        }
    }
}
// Exact conversion range guards and observable subclass calls.
val exactInputs = listOf("0", "1", "127", "128", "32767", "32768", "2147483647", "2147483648",
    "4294967295", "4294967296", "9223372036854775807", "9223372036854775808", "-128", "-129",
    "-32768", "-32769", "-2147483648", "-2147483649", "-9223372036854775808", "-9223372036854775809",
    "-4294967295", "-4294967296", "18446744073709551615", "18446744073709551616",
    "-18446744073709551615", "-18446744073709551616")
for (a in exactInputs) for (fakeBits in listOf(Int.MIN_VALUE, -1, 0, 7, 8, 15, 16, 31, 32, 63, 64, Int.MAX_VALUE)) {
    for (op in listOf("intExact", "longExact", "shortExact", "byteExact")) {
        val trace = StringBuilder()
        val x = object : BigInteger(a) {
            override fun bitLength(): Int { trace.append("bits;"); return fakeBits }
            override fun toInt(): Int { trace.append("int;"); return 44 }
            override fun toLong(): Long { trace.append("long;"); return 45L }
            override fun toByte(): Byte { trace.append("byte;"); return 42 }
            override fun toShort(): Short { trace.append("short;"); return 43 }
        }
        record("E", op, a, fakeBits.toString()) {
            val result = try { when (op) {
                "intExact" -> x.intValueExact()
                "longExact" -> x.longValueExact()
                "shortExact" -> x.shortValueExact()
                else -> x.byteValueExact()
            }.toString() } catch (e: ArithmeticException) {
                check(e.cause == null)
                "!${e.javaClass.simpleName}:${e.message}"
            }
            "$result|$trace"
        }
    }
}
// Observable JVM Number delegation, using public APIs on independently declared subclasses.
val replacementLongs = listOf(0L, 42L, -42L, Long.MIN_VALUE, Long.MAX_VALUE,
    (1L shl 54) + (1L shl 30) + 1, (1L shl 62) + (1L shl 38) + 1, -((1L shl 62) + (1L shl 38) + 1))
for (a in listOf("0", "1", "-1", "9223372036854775807", "-9223372036854775807", "9223372036854775808", "-9223372036854775808", "18446744073709551615")) {
    for (replacement in replacementLongs) {
        val x = object : BigInteger(a) {
            override fun toLong(): Long = replacement
            // Kotlin's Java Number mapping requires these implementations even though
            // this oracle never invokes either method on this subclass.
            override fun toByte(): Byte = error("Unused byte conversion")
            override fun toShort(): Short = error("Unused short conversion")
        }
        record("P", "primitiveSubclass", a, replacement.toString()) {
            "${x.toInt()}|${x.toDouble().toBits()}|${x.toFloat().toBits()}"
        }
    }
}
// Structured signed powers and adjacent values around byte, JVM-word and our limb boundaries.
for (power in listOf(0, 1, 7, 8, 29, 30, 31, 32, 59, 60, 61, 63, 64, 89, 90, 91, 127, 128, 255, 256, 511, 512, 1023, 1024)) {
    for (offset in -1L..1L) for (negative in listOf(false, true)) {
        val magnitude = BigInteger.ONE.shiftLeft(power).add(BigInteger.valueOf(offset))
        val x = if (negative) magnitude.negate() else magnitude
        val a = x.toString()
        record("I", "bits", a) { "${x.bitLength()}|${x.bitCount()}|${x.lowestSetBit}" }
        record("I", "not", a) { x.not() }
        for (distance in listOf(Int.MIN_VALUE, -1, 0, maxOf(0, power - 1), power, power + 1, power + 30).distinct()) {
            record("I", "testBit", a, p = distance) { x.testBit(distance) }
            record("I", "setBit", a, p = distance) { x.setBit(distance) }
            record("I", "clearBit", a, p = distance) { x.clearBit(distance) }
            record("I", "flipBit", a, p = distance) { x.flipBit(distance) }
        }
        record("I", "testBit", a, p = Int.MAX_VALUE) { x.testBit(Int.MAX_VALUE) }
        for (distance in listOf(Int.MIN_VALUE, -Int.MAX_VALUE, Int.MAX_VALUE)) {
            record("I", "shift", a, p = distance) { x.shiftLeft(distance) }
            record("I", "shiftRight", a, p = distance) { x.shiftRight(distance) }
        }
        val mask = BigInteger.ONE.shiftLeft(power + 1).subtract(BigInteger.ONE)
        for (y in listOf(mask, mask.negate())) record("I", "andNot", a, y.toString()) { x.andNot(y) }
    }
}
val decimalInputs = listOf("0", "0.00", "0E+20", "0E-8", "0E-9", "0E-10", "1", "-1", "1.00", "-2.500", "0.000001", "0.0000001", "9.9999999", "1E+25", "1000.000") +
    List(85) { BigDecimal(BigInteger(100, random).let { if (random.nextBoolean()) it.negate() else it }, random.nextInt(80) - 30).toString() }
for (a in decimalInputs) {
    val x = BigDecimal(a)
    val y = BigDecimal(BigInteger(80, random).let { if (random.nextBoolean()) it.negate() else it }, random.nextInt(60) - 20)
    val b = y.toString()
    val mode = RoundingMode.entries[random.nextInt(8)]
    val p = random.nextInt(25)
    val mc = MathContext(p, mode)
    record("D", "parse", a) { x }
    record("D", "plain", a) { x.toPlainString() }
    record("D", "engineering", a) { x.toEngineeringString() }
    record("D", "add", a, b, p, mode) { x.add(y, mc) }
    record("D", "subtract", a, b, p, mode) { x.subtract(y, mc) }
    record("D", "multiply", a, b, p, mode) { x.multiply(y, mc) }
    record("D", "divide", a, b, p, mode) { x.divide(y, mc) }
    record("D", "exactDivide", a, b) { x.divide(y) }
    record("D", "integral", a, b, p, mode) { x.divideToIntegralValue(y, mc) }
    record("D", "remainder", a, b, p, mode) { x.divideAndRemainder(y, mc) }
    record("D", "round", a, p = p, mode = mode) { x.round(mc) }
    val scale = random.nextInt(60) - 20
    record("D", "scale", a, p = scale, mode = mode) { x.setScale(scale, mode) }
    record("D", "divideScale", a, b, scale, mode) { x.divide(y, scale, mode) }
    record("D", "strip", a) { x.stripTrailingZeros() }
    record("D", "sqrt", a, p = p, mode = mode) { x.sqrt(mc) }
    record("D", "pow", a, b = "7", p = p, mode = mode) { x.pow(7, mc) }
    record("D", "double", a) { x.toDouble().toBits() }
    record("D", "float", a) { x.toFloat().toBits() }
    record("D", "integerExact", a) { x.toBigIntegerExact() }
}
// Exact dyadic midpoint neighbors: gradual underflow, normal carries and overflow.
// Construct the rational numbers directly; no primitive floating-point arithmetic
// participates in the inputs or in deciding which side of a midpoint they occupy.
fun exactDyadic(numerator: BigInteger, exponent: Int): BigDecimal =
    if (exponent >= 0) BigDecimal(numerator.shiftLeft(exponent))
    else BigDecimal(numerator.multiply(BigInteger.valueOf(5).pow(-exponent)), -exponent)
for (precision in listOf(24, 53)) {
    val minimum = if (precision == 24) -126 else -1022
    val maximum = if (precision == 24) 127 else 1023
    val leading = BigInteger.ONE.shiftLeft(precision - 1)
    val significands = listOf(BigInteger.ZERO, BigInteger.ONE, leading.subtract(BigInteger.ONE),
        leading, leading.add(BigInteger.ONE), BigInteger.ONE.shiftLeft(precision).subtract(BigInteger.ONE))
    for (exponent in listOf(minimum, minimum + 1, -30, -1, 0, 1, 30, maximum - 1, maximum)) {
        for (significand in significands) {
            val midpoint = exactDyadic(significand.shiftLeft(1).add(BigInteger.ONE), exponent - precision)
            val epsilon = BigDecimal(BigInteger.ONE, midpoint.scale() + 2)
            for (offset in -1L..1L) for (negative in listOf(false, true)) {
                val positive = midpoint.add(epsilon.multiply(BigDecimal.valueOf(offset)))
                val x = if (negative) positive.negate() else positive
                record("D", "float", x.toString()) { x.toFloat().toBits() }
                record("D", "double", x.toString()) { x.toDouble().toBits() }
            }
        }
    }
}
// Observable decimal subclass calls at compact primitive-conversion boundaries.
for (coefficient in listOf("0", "1", "16777216", "16777217", "9007199254740992",
    "9007199254740993", "9223372036854775807", "-9223372036854775808", "9223372036854775808")) {
    for (scale in listOf(-350, -23, -22, -11, -10, 0, 10, 11, 22, 23, 350)) {
        for (sign in -1..1) for (single in listOf(false, true)) {
            record("X", if (single) "float" else "double", coefficient, scale.toString(), sign) {
                val trace = StringBuilder()
                val value = object : BigDecimal(BigInteger(coefficient), scale) {
                    override fun toByte(): Byte = 0
                    override fun toShort(): Short = 0
                    override fun signum(): Int { trace.append("sign;"); return sign }
                    override fun precision(): Int { trace.append("precision;"); return 1 }
                }
                val bits = if (single) value.toFloat().toBits().toLong() else value.toDouble().toBits()
                "$bits|$trace"
            }
        }
    }
}
// Midpoints, sticky tails, preferred scales and scale saturation, independent of random coverage.
for (mode in RoundingMode.entries) {
    for (a in listOf("2.5", "3.5", "-2.5", "-3.5", "0.50000000000000000001", "-0.49999999999999999999", "9.99")) {
        record("D", "scale", a, p = 0, mode = mode) { BigDecimal(a).setScale(0, mode) }
    }
    for (a in listOf("1", "-1", "1.0000", "-1.0000")) {
        for (b in listOf("1E-100000", "-1E-100000", "0E-100000")) {
            record("D", "add", a, b, 4, mode) { BigDecimal(a).add(BigDecimal(b), MathContext(4, mode)) }
        }
    }
    for (b in listOf("2", "4.00", "8", "5", "0.5", "3", "0")) {
        record("D", "divide", "1.000", b, 5, mode) { BigDecimal("1.000").divide(BigDecimal(b), MathContext(5, mode)) }
    }
}
for (text in listOf("", "-", "+", ".", "1E", "1E+", " 1", "1 ", "1_0", "1E2147483648", "0.1E-2147483647", "١٢٣.٤٥", "１２３")) {
    record("D", "parse", text) { BigDecimal(text) }
    record("I", "parse", text) { BigInteger(text) }
}
val doubles = listOf(0.0, -0.0, Double.MIN_VALUE, Double.MAX_VALUE, 0.1, 1e23, 1e7, 1e-4, Double.NaN, Double.POSITIVE_INFINITY) +
    List(40) { Double.fromBits(random.nextLong()) }
for (d in doubles) {
    record("F", "construct", d.toBits().toString()) { BigDecimal(d) }
    record("F", "valueOf", d.toBits().toString()) { BigDecimal.valueOf(d) }
}
for (text in listOf("0E+2147483647", "1E+2147483647", "0E-2147483647", "1E-2147483647", "10E+2147483647")) {
    record("D", "scale", text, p = 0) { BigDecimal(text).setScale(0, RoundingMode.HALF_EVEN) }
    record("D", "divideScale", text, b = "1", p = 0) { BigDecimal(text).divide(BigDecimal.ONE, 0, RoundingMode.HALF_EVEN) }
}
for (bytes in listOf(byteArrayOf(), byteArrayOf(0), byteArrayOf(-1), byteArrayOf(0, -128, 1))) {
    for (off in -2..bytes.size + 1) for (len in -2..bytes.size + 1) {
        val input = bytes.joinToString(",")
        rows.add(listOf("B", "twos", input, len.toString(), off.toString(), "0", outcome { BigInteger(bytes, off, len) }))
        for (sign in -2..2)
            rows.add(listOf("B", "signed", input, len.toString(), off.toString(), sign.toString(), outcome { BigInteger(sign, bytes, off, len) }))
    }
}
for (bits in -1..3) {
    record("R", "random", bits.toString()) { BigInteger(bits, null as Random?) }
    record("R", "probablePrime", bits.toString()) { BigInteger.probablePrime(bits, null) }
    for (certainty in -1..1)
        record("R", "prime", bits.toString(), certainty.toString()) { BigInteger(bits, certainty, null) }
}
fun literal(value: String): String = buildString {
    append('"')
    for (c in value) when (c) {
        '\\' -> append("\\\\")
        '"' -> append("\\\"")
        '$' -> append("\\$")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> append(c)
    }
    append('"')
}
val batches = rows.chunked(50)
val source = buildString {
    appendLine("// SPDX-License-Identifier: Apache-2.0")
    appendLine("// Generated by scripts/generate-oracle.main.kts. Oracle: JDK 25.0.4+9. Do not edit.")
    appendLine("package io.heapy.krogu.math")
    appendLine()
    appendLine("internal fun oracleVectors(): List<Array<String>> = buildList {")
    for (i in batches.indices) appendLine("    addAll(oracleBatch$i())")
    appendLine("}")
    for ((i, batch) in batches.withIndex()) {
        appendLine("\nprivate fun oracleBatch$i(): List<Array<String>> = listOf(")
        for (row in batch) appendLine("    arrayOf(${row.joinToString(", ", transform = ::literal)}),")
        appendLine(")")
    }
}
val target = File("src/commonTest/kotlin/io/heapy/krogu/math/Jdk25Vectors.kt")
if ("--check" in args) {
    check(target.exists() && target.readText() == source) { "Oracle vectors differ; regenerate with the pinned JDK" }
    println("Verified ${rows.size} oracle vectors with JDK ${Runtime.version()}")
} else {
    target.parentFile.mkdirs()
    target.writeText(source)
    println("Generated ${rows.size} oracle vectors in $target")
}

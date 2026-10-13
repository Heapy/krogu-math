#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Generate portable behavioral fixtures using only the public JDK 25.0.4+9 API.
 * Usage (from repository root): kotlinr scripts/generate-oracle.main.kts [--check]
 * No third-party dependencies or implementation-source access.
 */
@file:Suppress("DEPRECATION")

import java.io.File
import java.math.BigInteger
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Random
import java.lang.management.ManagementFactory
import com.sun.management.HotSpotDiagnosticMXBean

check(Runtime.version().toString() == "25.0.4+9-LTS" || Runtime.version().toString() == "25.0.4+9") {
    "Oracle requires JDK 25.0.4+9, got ${Runtime.version()}"
}
check(args.all { it == "--check" }) { "Usage: generate-oracle.main.kts [--check]" }
check(ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean::class.java)
    .getVMOption("OmitStackTraceInFastThrow").value == "false") {
    "Message oracle requires JAVA_TOOL_OPTIONS=-XX:-OmitStackTraceInFastThrow"
}
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
// Division error ordering and wording at wide scale/precision boundaries.
val divisionScales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -10, 0, 10, Int.MAX_VALUE - 1, Int.MAX_VALUE)
fun recordScaledZero(coefficient: String, scale: Int, zeroScale: Int, target: Int, mode: RoundingMode) {
    record("G", "scaledDivide", "$coefficient@$scale", "0@$zeroScale", target, mode) {
        try { BigDecimal(BigInteger(coefficient), scale).divide(BigDecimal(BigInteger.ZERO, zeroScale), target, mode).toString() }
        catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
    }
}
val scaledZeroCoefficients = listOf("0", "1", "-1", "9223372036854775807", "-9223372036854775807",
    "-9223372036854775808", "123456789012345678901234567890", "-123456789012345678901234567890")
for (coefficient in scaledZeroCoefficients) {
    for (scale in listOf(Int.MIN_VALUE, 0, 10, Int.MAX_VALUE)) {
        for (zeroScale in listOf(Int.MIN_VALUE, 0, 10, Int.MAX_VALUE)) {
            for (target in listOf(Int.MIN_VALUE, 0, 10, Int.MAX_VALUE)) {
                for (mode in listOf(RoundingMode.UP, RoundingMode.UNNECESSARY)) recordScaledZero(coefficient, scale, zeroScale, target, mode)
            }
        }
    }
    for (target in listOf(-19, -18, 18, 19)) for (mode in listOf(RoundingMode.UP, RoundingMode.UNNECESSARY))
        recordScaledZero(coefficient, 0, 0, target, mode)
}
for (scale in divisionScales) for (divisorScale in divisionScales) {
    for (precision in listOf(1, 2, 7, Int.MAX_VALUE - 1, Int.MAX_VALUE)) for (divisor in 1L..3L) {
        val x = BigDecimal(BigInteger.ONE, scale)
        val y = BigDecimal.valueOf(divisor, divisorScale)
        record("G", "divide", "1@$scale", "$divisor@$divisorScale", precision, RoundingMode.HALF_UP) {
            try { x.divide(y, MathContext(precision, RoundingMode.HALF_UP)).toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        }
    }
}
for (scale in divisionScales) for (divisorScale in divisionScales) {
    for (coefficient in listOf(0L, 1L, -1L, 10L)) for (divisor in listOf(0L, 1L, -2L, 3L, 5L, 6L)) {
        record("G", "exactDivide", "$coefficient@$scale", "$divisor@$divisorScale") {
            try { BigDecimal.valueOf(coefficient, scale).divide(BigDecimal.valueOf(divisor, divisorScale)).toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        }
    }
}
for (coefficient in listOf("0", "1", "-1", "123456789012345678901234567890")) {
    for (scale in listOf(0, Int.MIN_VALUE, Int.MAX_VALUE)) {
        for (op in listOf("divide", "exactDivide", "integral", "integralContext", "remainder", "remainderContext", "pair", "pairContext")) {
            record("G", op, "$coefficient@0", "0@$scale", 7, RoundingMode.HALF_UP) {
                val x = BigDecimal(coefficient)
                val zero = BigDecimal(BigInteger.ZERO, scale)
                val mc = MathContext(7)
                try {
                    when (op) {
                        "divide" -> x.divide(zero, mc)
                        "exactDivide" -> x.divide(zero)
                        "integral" -> x.divideToIntegralValue(zero)
                        "integralContext" -> x.divideToIntegralValue(zero, mc)
                        "remainder" -> x.remainder(zero)
                        "remainderContext" -> x.remainder(zero, mc)
                        "pair" -> x.divideAndRemainder(zero)
                        else -> x.divideAndRemainder(zero, mc)
                    }.toString()
                } catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
            }
        }
    }
}
// Scale-bound division keeps the pinned runtime's staged Int arithmetic, even
// where an unbounded mathematical rescaling would have required range rejection.
val nonzeroDivisionScales = listOf(Int.MIN_VALUE, -10, 0, 10, Int.MAX_VALUE)
for (coefficient in listOf(0L, 1L, -1L, 10L)) for (scale in nonzeroDivisionScales) {
    for (divisor in listOf(1L, -2L, 3L)) for (divisorScale in nonzeroDivisionScales) {
        for (target in nonzeroDivisionScales) for (mode in RoundingMode.entries) {
            record("G", "scaledDivide", "$coefficient@$scale", "$divisor@$divisorScale", target, mode) {
                try { BigDecimal.valueOf(coefficient, scale).divide(BigDecimal.valueOf(divisor, divisorScale), target, mode).toString() }
                catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
            }
        }
    }
}
val setScaleScales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -19, -18, -1, 0, 1, 18, 19, Int.MAX_VALUE - 1, Int.MAX_VALUE)
for (coefficient in listOf("0", "1", "-1", "10", "9223372036854775807", "-9223372036854775808", "123456789012345678901234567890")) {
    for (scale in setScaleScales) for (target in setScaleScales) for (mode in RoundingMode.entries) {
        record("G", "setScale", "$coefficient@$scale", "0@0", target, mode) {
            try { BigDecimal(BigInteger(coefficient), scale).setScale(target, mode).toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        }
    }
}
val exactDecimalCoefficients = listOf("0", "1", "-1", "127", "128", "-128", "-129", "32767", "32768",
    "-32768", "-32769", "2147483647", "2147483648", "-2147483648", "-2147483649",
    "9223372036854775807", "9223372036854775808", "-9223372036854775808", "-9223372036854775809",
    "1000000000000000000000000000000")
for (coefficient in exactDecimalCoefficients) for (scale in listOf(Int.MIN_VALUE, -20, -1, 0, 1, 20, Int.MAX_VALUE)) {
    for (op in listOf("byteExact", "shortExact", "intExact", "longExact")) {
        record("G", op, "$coefficient@$scale", "0@0") {
            val value = BigDecimal(BigInteger(coefficient), scale)
            try { when (op) {
                "byteExact" -> value.byteValueExact()
                "shortExact" -> value.shortValueExact()
                "intExact" -> value.intValueExact()
                else -> value.longValueExact()
            }.toString() } catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        }
    }
}
for ((coefficient, scale) in listOf("0" to Int.MIN_VALUE, "1" to 0, "1" to 1, "12300" to 4,
    "-9223372036854775808" to 0, "92233720368547758080" to 1, "1" to Int.MIN_VALUE, "1" to Int.MAX_VALUE)) {
    for (op in listOf("byte", "short", "int", "long", "integer", "scale", "enumScale", "intScale", "text")) {
        record("T", op, coefficient, scale.toString(), mode = RoundingMode.HALF_UP) {
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(coefficient), scale) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun toLong(): Long { trace.append("long;"); return super.toLong() }
                override fun longValueExact(): Long { trace.append("longExact;"); return super.longValueExact() }
                override fun toBigIntegerExact(): BigInteger { trace.append("integerExact;"); return super.toBigIntegerExact() }
                override fun precision(): Int { trace.append("precision;"); return super.precision() }
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun setScale(scale: Int, mode: RoundingMode): BigDecimal { trace.append("enumScale;"); return super.setScale(scale, mode) }
                override fun setScale(scale: Int, mode: Int): BigDecimal { trace.append("intScale;"); return super.setScale(scale, mode) }
                override fun setScale(scale: Int): BigDecimal { trace.append("scale;"); return super.setScale(scale) }
            }
            val result = try { when (op) {
                "byte" -> value.byteValueExact()
                "short" -> value.shortValueExact()
                "int" -> value.intValueExact()
                "long" -> value.longValueExact()
                "integer" -> value.toBigIntegerExact()
                "text" -> value.toString()
                "scale" -> value.setScale(0)
                "enumScale" -> value.setScale(0, RoundingMode.HALF_UP)
                else -> value.setScale(0, RoundingMode.HALF_UP.ordinal)
            }.toString() } catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
            "$result|$trace"
        }
    }
}
for (replacement in listOf(Long.MIN_VALUE, -32769L, -129L, -128L, 127L, 128L, 32768L, 2147483648L, Long.MAX_VALUE)) {
    for (op in listOf("byte", "short", "int")) {
        record("U", op, "1", replacement.toString()) {
            val trace = StringBuilder()
            val value = object : BigDecimal("1") {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun longValueExact(): Long { trace.append("longExact;"); return replacement }
            }
            val result = try { when (op) {
                "byte" -> value.byteValueExact()
                "short" -> value.shortValueExact()
                else -> value.intValueExact()
            }.toString() } catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
            "$result|$trace"
        }
    }
}
for (coefficient in listOf("0", "1", "-1", "123", "-123", "2147483646", "2147483647", "2147483648",
    "9223372036854775807", "-9223372036854775808", "123456789012345678901234567890")) {
    for (scale in listOf(-2, -1, 0, 1, 2, 3, 6, 7)) for (sign in -1..1) {
        record("W", "text", coefficient, scale.toString(), sign) {
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(coefficient), scale) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun signum(): Int { trace.append("sign;"); return sign }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun toPlainString(): String { trace.append("plain;"); return "custom" }
            }
            val result = value.toString()
            "$result|$trace"
        }
    }
}
for (coefficient in listOf("0", "1", "-1", "123", "-123", "2147483647", "123456789012345678901234567890")) {
    for (scale in listOf(-2, -1, 0, 1, 2, 3, 6, 7)) for (sign in -1..1) for (op in listOf("plain", "engineering")) {
        record("Y", op, coefficient, scale.toString(), sign) {
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(coefficient), scale) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun signum(): Int { trace.append("sign;"); return sign }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun toPlainString(): String { trace.append("plain;"); return super.toPlainString() }
            }
            val result = if (op == "plain") value.toPlainString() else value.toEngineeringString()
            "$result|$trace"
        }
    }
}
for (coefficient in listOf("0", "1", "-1", "-9223372036854775808", "123456789012345678901234567890")) {
    for (scale in listOf(-2, 0, 1, 2, 7)) for (initialSign in listOf(-1, 0)) for (op in listOf("text", "plain", "engineering")) {
        record("Z", op, coefficient, scale.toString(), initialSign) {
            var observedSign = initialSign
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(coefficient), scale) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun signum(): Int { trace.append("sign;"); return observedSign }
                override fun precision(): Int { trace.append("precision;"); return 1 }
            }
            fun format(): String = when (op) {
                "plain" -> value.toPlainString()
                "engineering" -> value.toEngineeringString()
                else -> value.toString()
            }
            val first = format()
            observedSign = 1
            "$first|${format()}|$trace"
        }
    }
}
for (text in listOf("-4", "0.00", "1", "4.00", "2", "0.0004", "123.456")) {
    for (precision in listOf(0, 1, 7)) for (mode in RoundingMode.entries) for (op in listOf("sqrt", "pow", "powContext")) {
        record("H", op, text, "3", precision, mode) {
            val trace = StringBuilder()
            val value = object : BigDecimal(text) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun precision(): Int { trace.append("precision;"); return super.precision() }
                override fun scale(): Int { trace.append("scale;"); return super.scale() }
            }
            val result = try {
                when (op) {
                    "sqrt" -> value.sqrt(MathContext(precision, mode))
                    "pow" -> value.pow(3)
                    else -> value.pow(3, MathContext(precision, mode))
                }.toString()
            } catch (e: ArithmeticException) { "!${e.message}" }
            "$result|$trace"
        }
    }
}
for (op in listOf("sqrtSign", "sqrtPrecision")) {
    val texts = if (op == "sqrtSign") listOf("0E+4", "0E+3", "0E+2", "0E+1", "0", "0.0", "0.00", "0.000", "0E-4", "0E-7", "0E-20", "1", "4.00", "2", "123.456")
        else listOf("1", "4.00", "2", "0.0004", "123.456")
    val replacements = if (op == "sqrtSign") listOf(-1, 0, 1) else listOf(1, 7, 30)
    val precisions = if (op == "sqrtSign") listOf(0, 1, 7) else listOf(1, 7)
    for (text in texts) for (replacement in replacements) for (precision in precisions) for (mode in RoundingMode.entries) {
        record("H", op, text, replacement.toString(), precision, mode) {
            val trace = StringBuilder()
            val value = object : BigDecimal(text) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun signum(): Int { trace.append("sign;"); return if (op == "sqrtSign") replacement else super.signum() }
                override fun precision(): Int { trace.append("precision;"); return if (op == "sqrtPrecision") replacement else super.precision() }
                override fun scale(): Int { trace.append("scale;"); return super.scale() }
            }
            val result = try { value.sqrt(MathContext(precision, mode)).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            "$result|$trace"
        }
    }
}
for (text in listOf("-4", "0.00", "1", "4.00", "2", "0.0004", "123.456")) {
    for (precision in listOf(0, 1, 7)) for (mode in RoundingMode.entries) for (replacement in listOf(null, "9", "0", "-4", "0.04")) {
        record("H", "sqrtNormalize", text, replacement ?: "~", precision, mode) {
            val trace = StringBuilder()
            val value = object : BigDecimal(text) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun precision(): Int { trace.append("precision;"); return super.precision() }
                override fun scale(): Int { trace.append("scale;"); return super.scale() }
                override fun stripTrailingZeros(): BigDecimal { trace.append("strip;"); return replacement?.let { BigDecimal(it) } ?: super.stripTrailingZeros() }
                override fun scaleByPowerOfTen(n: Int): BigDecimal { trace.append("scaleBy:$n;"); return super.scaleByPowerOfTen(n) }
                override fun toDouble(): Double { trace.append("double;"); return super.toDouble() }
            }
            val result = try { value.sqrt(MathContext(precision, mode)).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            "$result|$trace"
        }
    }
}
for (text in listOf("9", "0.04", "4.00", "0", "-4", "0.4", "0.9", "4.0")) {
    for (precision in listOf(0, 1, 7)) for (mode in RoundingMode.entries) for (first in listOf(null, "1", "0", "4", "2", "-4", "throw")) {
        record("N", "normalizedRoot", text, first ?: "~", precision, mode) {
            val trace = StringBuilder()
            val normalized = object : BigDecimal(text) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun precision(): Int { trace.append("precision;"); return super.precision() }
                override fun scale(): Int { trace.append("scale;"); return super.scale() }
                private var calls = 0
                override fun unscaledValue(): BigInteger {
                    trace.append("unscaled;")
                    if (calls++ == 0 && first != null) {
                        if (first == "throw") throw IllegalStateException("first coefficient")
                        return BigInteger(first)
                    }
                    return super.unscaledValue()
                }
            }
            val value = object : BigDecimal("4.00") {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun stripTrailingZeros(): BigDecimal = normalized
            }
            val result = try { value.sqrt(MathContext(precision, mode)).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            "$result|$trace"
        }
    }
}
for (coefficient in listOf("0", "1", "4", "9", "2", "-4", "10", "100", "400")) {
    for (scale in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 2, -3, -2, -1, 0, 1, 2, 3, Int.MAX_VALUE - 2, Int.MAX_VALUE - 1, Int.MAX_VALUE)) {
        for (precision in listOf(0, 1, 7)) for (mode in RoundingMode.entries) {
            record("A", "normalizedScale", coefficient, scale.toString(), precision, mode) {
                val trace = StringBuilder()
                val normalized = object : BigDecimal(BigInteger(coefficient), scale) {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    override fun unscaledValue(): BigInteger { trace.append("unscaled;"); return super.unscaledValue() }
                }
                val value = object : BigDecimal(BigInteger("4"), scale) {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    override fun stripTrailingZeros(): BigDecimal = normalized
                }
                val result = try { value.sqrt(MathContext(precision, mode)).toString() }
                    catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                "$result|$trace"
            }
        }
    }
}
for (coefficient in listOf("0", "1", "-1", "8", "-8", "16", "-16")) {
    for (scale in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -1, 0, 1, Int.MAX_VALUE - 1, Int.MAX_VALUE)) {
        for (power in listOf(Int.MIN_VALUE, -1, 0, 1, 2, 999999999, 1000000000)) for (op in listOf("exact", "unlimited")) {
            record("Q", op, coefficient, scale.toString(), power) {
                val trace = StringBuilder()
                val value = object : BigDecimal(BigInteger(coefficient), scale) {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    override fun signum(): Int { trace.append("sign;"); return super.signum() }
                    override fun pow(n: Int): BigDecimal { trace.append("pow;"); return super.pow(n) }
                }
                val result = try { (if (op == "exact") value.pow(power) else value.pow(power, MathContext.UNLIMITED)).toString() }
                    catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                "$result|$trace"
            }
        }
    }
}
for (coefficient in listOf("0", "1", "-1", "8", "-8", "16", "-16", "4294967296", "-4294967296")) {
    for (power in listOf(Int.MIN_VALUE, -1, 0, 999999999, Int.MAX_VALUE)) {
        record("Q", "integer", coefficient, p = power) {
            try { BigInteger(coefficient).pow(power).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
        }
    }
}
for (coefficient in listOf("0", "1", "-1", "123", "-123", "9223372036854775808")) {
    for (scale in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -2, 0, 2, Int.MAX_VALUE - 1, Int.MAX_VALUE)) {
        for (distance in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -2, -1, 0, 1, 2, Int.MAX_VALUE - 1, Int.MAX_VALUE)) {
            for (sign in -1..1) for (op in listOf("left", "right", "scale")) {
                record("V", "$op:$distance", coefficient, scale.toString(), sign) {
                    val trace = StringBuilder()
                    val value = object : BigDecimal(BigInteger(coefficient), scale) {
                        override fun toByte(): Byte = super.toInt().toByte()
                        override fun toShort(): Short = super.toInt().toShort()
                        override fun signum(): Int { trace.append("sign;"); return sign }
                        override fun precision(): Int { trace.append("precision;"); return 1 }
                        override fun movePointLeft(n: Int): BigDecimal { trace.append("left;"); return super.movePointLeft(n) }
                        override fun movePointRight(n: Int): BigDecimal { trace.append("right;"); return super.movePointRight(n) }
                        override fun scaleByPowerOfTen(n: Int): BigDecimal { trace.append("scaleBy;"); return super.scaleByPowerOfTen(n) }
                    }
                    val result = try {
                        val moved = when (op) { "left" -> value.movePointLeft(distance); "right" -> value.movePointRight(distance); else -> value.scaleByPowerOfTen(distance) }
                        val text = BigDecimal(moved.unscaledValue(), moved.scale()).toString()
                        "$text|${moved === value}|${moved.javaClass == BigDecimal::class.java}"
                    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                    "$result|$trace"
                }
            }
        }
    }
}
for (left in listOf("0", "-1", "12345678901234567890")) for (right in listOf("0", "1", "123")) {
    for (leftScale in listOf(Int.MIN_VALUE, -2, 0, 2, Int.MAX_VALUE)) for (rightScale in listOf(Int.MIN_VALUE, -2, 0, 2, Int.MAX_VALUE)) {
        for (sign in -1..1) for (precision in listOf(-1, 0, 1, 7)) for (mode in listOf(RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)) {
            record("L", "multiply:$sign", "$left@$leftScale", "$right@$rightScale", precision, mode) {
                val trace = StringBuilder()
                val value = object : BigDecimal(BigInteger(left), leftScale) {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    override fun signum(): Int { trace.append("sign;"); return sign }
                    override fun precision(): Int { trace.append("precision;"); return 1 }
                    override fun multiply(value: BigDecimal): BigDecimal { trace.append("exact;"); return super.multiply(value) }
                    override fun multiply(value: BigDecimal, mc: MathContext): BigDecimal { trace.append("context;"); return super.multiply(value, mc) }
                }
                val other = BigDecimal(BigInteger(right), rightScale)
                val result = try {
                    val product = if (precision < 0) value.multiply(other) else value.multiply(other, MathContext(precision, mode))
                    "$product|${product === value}|${product.javaClass == BigDecimal::class.java}"
                } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                "$result|$trace"
            }
        }
    }
}
for (coefficient in listOf("0", "123", "999")) {
    for (scale in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 2, Int.MIN_VALUE + 3, 0, Int.MAX_VALUE)) {
        for (precision in listOf(0, 1, 2, 7)) for (mode in RoundingMode.entries) for (op in listOf("round", "constructor")) {
            record("L", op, coefficient, scale.toString(), precision, mode) {
                try {
                    val context = MathContext(precision, mode)
                    (if (op == "constructor") BigDecimal(BigInteger(coefficient), scale, context)
                        else BigDecimal(BigInteger(coefficient), scale).round(context)).toString()
                } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            }
        }
    }
}
for (text in listOf("0.00", "-123.456", "999.99", "1.2345678901234567890")) {
    for (replacement in listOf(-1, 0, 1, 3, 9)) for (precision in listOf(0, 1, 7)) for (mode in RoundingMode.entries) for (behavior in listOf("super", "replace", "throw", "sequence")) {
        record("S", behavior, text, replacement.toString(), precision, mode) {
            val trace = StringBuilder()
            val value = object : BigDecimal(text) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                private var calls = 0
                override fun precision(): Int { trace.append("precision;"); return if (behavior == "sequence" && calls++ > 0) 1 else replacement }
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun plus(mc: MathContext): BigDecimal {
                    trace.append("plus;")
                    return when (behavior) { "replace" -> BigDecimal("42.00"); "throw" -> throw IllegalStateException("plus callback"); else -> super.plus(mc) }
                }
            }
            val result = try {
                val rounded = value.round(MathContext(precision, mode))
                "${BigDecimal(rounded.unscaledValue(), rounded.scale())}|${rounded === value}|${rounded.javaClass == BigDecimal::class.java}"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            "$result|$trace"
        }
    }
}
for (text in listOf("0.00", "-123.45")) for (behavior in listOf("return", "self", "throw", "super")) for (op in listOf("round", "plus")) {
    record("K", "$op:$behavior", text) {
        val trace = StringBuilder()
        val value = object : BigDecimal(text) {
            override fun toByte(): Byte = super.toInt().toByte()
            override fun toShort(): Short = super.toInt().toShort()
            override fun plus(mc: MathContext?): BigDecimal {
                trace.append("plus:").append(mc == null).append(';')
                return when (behavior) {
                    "return" -> BigDecimal("42.00")
                    "self" -> this
                    "throw" -> throw IllegalStateException("plus callback")
                    else -> super.plus(mc)
                }
            }
        }
        val result = try { (if (op == "round") value.round(null) else value.plus(null)).toString() }
            catch (e: Exception) { "!${e::class.simpleName}" + if (e is IllegalStateException) ":${e.message}" else "" }
        "$result|$trace"
    }
}
fun unaryOutcome(block: () -> String): String = try { block() }
    catch (e: Exception) { "!${e::class.simpleName}" + if (e is NullPointerException) "" else ":${e.message}" }
for (text in listOf("0.00", "-123.456", "123.456", "1.2345678901234567890")) {
    for (sign in -1..1) for (precision in listOf(0, 1, 7)) for (mode in listOf(RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)) {
        for (op in listOf("abs", "absContext", "negate", "negateContext", "plus", "plusContext")) {
            record("C", "$op:$sign", text, p = precision, mode = mode) {
                val trace = StringBuilder()
                val value = object : BigDecimal(text) {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    override fun signum(): Int { trace.append("sign;"); return sign }
                    override fun abs(): BigDecimal { trace.append("abs;"); return super.abs() }
                    override fun negate(): BigDecimal { trace.append("negate;"); return super.negate() }
                    override fun negate(mc: MathContext?): BigDecimal { trace.append("negateContext;"); return super.negate(mc) }
                    override fun plus(): BigDecimal { trace.append("plus;"); return super.plus() }
                    override fun plus(mc: MathContext?): BigDecimal { trace.append("plusContext;"); return super.plus(mc) }
                    override fun round(mc: MathContext?): BigDecimal { trace.append("round;"); return super.round(mc) }
                }
                val result = unaryOutcome {
                    val context = MathContext(precision, mode)
                    val r = when (op) { "abs" -> value.abs(); "absContext" -> value.abs(context); "negate" -> value.negate(); "negateContext" -> value.negate(context); "plus" -> value.plus(); else -> value.plus(context) }
                    "${BigDecimal(r.unscaledValue(), r.scale())}|${r === value}|${r.javaClass == BigDecimal::class.java}"
                }
                "$result|$trace"
            }
        }
    }
}
for (behavior in listOf("super", "replace", "throw")) for (precision in listOf(-1, 0, 1, 7)) for (mode in RoundingMode.entries) {
    record("C", "returnedNegate:$behavior", "1", "12.34", precision, mode) {
        val trace = StringBuilder()
        val returned = object : BigDecimal("12.34") {
            override fun toByte(): Byte = super.toInt().toByte()
            override fun toShort(): Short = super.toInt().toShort()
            override fun plus(mc: MathContext?): BigDecimal {
                trace.append("plus;")
                return when (behavior) { "replace" -> BigDecimal("42.00"); "throw" -> throw IllegalStateException("plus callback"); else -> super.plus(mc) }
            }
            override fun round(mc: MathContext?): BigDecimal { trace.append("round;"); return BigDecimal("99") }
        }
        val value = object : BigDecimal("1") {
            override fun toByte(): Byte = super.toInt().toByte()
            override fun toShort(): Short = super.toInt().toShort()
            override fun negate(): BigDecimal = returned
        }
        val result = unaryOutcome { value.negate(if (precision < 0) null else MathContext(precision, mode)).toString() }
        "$result|$trace"
    }
}
for (text in listOf("0.00", "1", "-1")) for (sign in -1..1) for (behavior in listOf("return", "self", "throw", "super")) for (op in listOf("nullAbs", "nullNegate")) {
    record("C", "$op:$behavior", text, sign.toString()) {
        val trace = StringBuilder()
        val value = object : BigDecimal(text) {
            override fun toByte(): Byte = super.toInt().toByte()
            override fun toShort(): Short = super.toInt().toShort()
            override fun signum(): Int { trace.append("sign;"); return sign }
            override fun negate(): BigDecimal { trace.append("negate;"); return super.negate() }
            override fun plus(mc: MathContext?): BigDecimal {
                trace.append("plus:").append(mc == null).append(';')
                return when (behavior) { "return" -> BigDecimal("42.00"); "self" -> this; "throw" -> throw IllegalStateException("context callback"); else -> super.plus(mc) }
            }
            override fun negate(mc: MathContext?): BigDecimal {
                trace.append("negateContext:").append(mc == null).append(';')
                return when (behavior) { "return" -> BigDecimal("42.00"); "self" -> this; "throw" -> throw IllegalStateException("context callback"); else -> super.negate(mc) }
            }
        }
        val result = unaryOutcome { (if (op == "nullAbs") value.abs(null) else value.negate(null)).toString() }
        "$result|$trace"
    }
}
// Observable constructor failure when pow's expanded working precision wraps.
// Coefficient one keeps successful high-precision cases bounded in memory.
for (precision in listOf(Int.MAX_VALUE, Int.MAX_VALUE - 1, Int.MAX_VALUE - 2, Int.MAX_VALUE - 10)) {
    for (power in listOf(0, 1, 10, 999999999)) for (mode in RoundingMode.entries) {
        record("D", "pow", "1", power.toString(), precision, mode) { BigDecimal.ONE.pow(power, MathContext(precision, mode)) }
    }
}
// Squared decimal rounding midpoints, perturbed exactly on each side. The root
// significands exercise both tie parities and carry into the next decade.
for (precision in listOf(1, 2, 7, 34, 65, 129)) {
    val leading = BigInteger.TEN.pow(precision - 1)
    val significands = listOf(leading, leading.add(BigInteger.ONE), BigInteger.TEN.pow(precision).subtract(BigInteger.ONE))
    for (exponent in listOf(-50, 0, 50)) for (significand in significands) {
        val midpoint = BigDecimal(significand.shiftLeft(1).add(BigInteger.ONE), 1)
            .scaleByPowerOfTen(exponent - precision + 1)
        val square = midpoint.multiply(midpoint)
        val epsilon = BigDecimal(BigInteger.ONE, square.scale() + 3)
        for (offset in -1L..1L) for (padding in listOf(0, 3)) {
            val value = square.add(epsilon.multiply(BigDecimal.valueOf(offset)))
            val x = value.setScale(value.scale() + padding)
            for (mode in RoundingMode.entries) {
                record("D", "sqrt", x.toString(), p = precision, mode = mode) { x.sqrt(MathContext(precision, mode)) }
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

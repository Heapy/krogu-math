package io.heapy.krogu.math

import kotlin.test.*

class PortableOracleTest {
    @Test fun jdk25BehaviorOnEveryPlatform() {
        for (v in oracleVectors()) {
            val actual = try {
                val result = evaluate(v)
                if (result is Array<*>) result.joinToString("|") else result.toString()
            } catch (e: Exception) { "!${e::class.simpleName}" }
            assertEquals(v[6], actual, "${v.take(6)}")
        }
    }
    @Suppress("DEPRECATION")
    private fun evaluate(v: Array<String>): Any? {
        val op = v[1]
        val p = v[4].toInt()
        if (v[0] == "MM") return evaluateMinMaxOracle(v)
        if (v[0] == "IM") return evaluateIntegerMinMaxOracle(v)
        if (v[0] == "ID") return evaluateIntegerDivisionOracle(v)
        if (v[0] == "CP") return evaluateComparisonOracle(v)
        if (v[0] == "ND") return evaluateNullableBinaryOracle(v)
        if (v[0] == "O") return evaluateAdditionOracle(v)
        if (v[0] == "C") return evaluateUnaryOracle(v)
        if (v[0] == "K") {
            val operation = op.substringBefore(':')
            val behavior = op.substringAfter(':')
            val trace = StringBuilder()
            val value = object : BigDecimal(v[2]) {
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
            val result = try { (if (operation == "round") value.round(null) else value.plus(null)).toString() }
                catch (e: Exception) { "!${e::class.simpleName}" + if (e is IllegalStateException) ":${e.message}" else "" }
            return "$result|$trace"
        }
        if (v[0] == "S") {
            val trace = StringBuilder()
            val value = object : BigDecimal(v[2]) {
                private var calls = 0
                override fun precision(): Int { trace.append("precision;"); return if (op == "sequence" && calls++ > 0) 1 else v[3].toInt() }
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun plus(mc: MathContext?): BigDecimal {
                    trace.append("plus;")
                    return when (op) { "replace" -> BigDecimal("42.00"); "throw" -> throw IllegalStateException("plus callback"); else -> super.plus(mc) }
                }
            }
            val result = try {
                val rounded = value.round(MathContext(p, RoundingMode.valueOf(v[5])))
                "${BigDecimal(rounded.unscaledValue(), rounded.scale())}|${rounded === value}|${rounded::class == BigDecimal::class}"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "L") {
            if (op == "round" || op == "constructor") return try {
                val context = MathContext(p, RoundingMode.valueOf(v[5]))
                (if (op == "constructor") BigDecimal(BigInteger(v[2]), v[3].toInt(), context)
                    else BigDecimal(BigInteger(v[2]), v[3].toInt()).round(context)).toString()
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            val sign = op.substringAfter(':').toInt()
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2].substringBefore('@')), v[2].substringAfter('@').toInt()) {
                override fun signum(): Int { trace.append("sign;"); return sign }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun multiply(multiplicand: BigDecimal?): BigDecimal { trace.append("exact;"); return super.multiply(multiplicand) }
                override fun multiply(multiplicand: BigDecimal?, mc: MathContext?): BigDecimal { trace.append("context;"); return super.multiply(multiplicand, mc) }
            }
            val other = BigDecimal(BigInteger(v[3].substringBefore('@')), v[3].substringAfter('@').toInt())
            val result = try {
                val product = if (p < 0) value.multiply(other) else value.multiply(other, MathContext(p, RoundingMode.valueOf(v[5])))
                "$product|${product === value}|${product::class == BigDecimal::class}"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "V") {
            val operation = op.substringBefore(':')
            val distance = op.substringAfter(':').toInt()
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return p }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun movePointLeft(n: Int): BigDecimal { trace.append("left;"); return super.movePointLeft(n) }
                override fun movePointRight(n: Int): BigDecimal { trace.append("right;"); return super.movePointRight(n) }
                override fun scaleByPowerOfTen(n: Int): BigDecimal { trace.append("scaleBy;"); return super.scaleByPowerOfTen(n) }
            }
            val result = try {
                val moved = when (operation) { "left" -> value.movePointLeft(distance); "right" -> value.movePointRight(distance); else -> value.scaleByPowerOfTen(distance) }
                val text = BigDecimal(moved.unscaledValue(), moved.scale()).toString()
                "$text|${moved === value}|${moved::class == BigDecimal::class}"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "Q") {
            if (op == "integer") return try { BigInteger(v[2]).pow(p).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun pow(n: Int): BigDecimal { trace.append("pow;"); return super.pow(n) }
            }
            val result = try { (if (op == "exact") value.pow(p) else value.pow(p, MathContext.UNLIMITED)).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "A") {
            val trace = StringBuilder()
            val normalized = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun unscaledValue(): BigInteger { trace.append("unscaled;"); return super.unscaledValue() }
            }
            val value = object : BigDecimal(BigInteger("4"), v[3].toInt()) {
                override fun stripTrailingZeros(): BigDecimal = normalized
            }
            val result = try { value.sqrt(MathContext(p, RoundingMode.valueOf(v[5]))).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "N") {
            val trace = StringBuilder()
            val normalized = object : BigDecimal(v[2]) {
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun precision(): Int { trace.append("precision;"); return super.precision() }
                override fun scale(): Int { trace.append("scale;"); return super.scale() }
                private var calls = 0
                override fun unscaledValue(): BigInteger {
                    trace.append("unscaled;")
                    if (calls++ == 0 && v[3] != "~") {
                        if (v[3] == "throw") throw IllegalStateException("first coefficient")
                        return BigInteger(v[3])
                    }
                    return super.unscaledValue()
                }
            }
            val value = object : BigDecimal("4.00") {
                override fun stripTrailingZeros(): BigDecimal = normalized
            }
            val result = try { value.sqrt(MathContext(p, RoundingMode.valueOf(v[5]))).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "H") {
            val trace = StringBuilder()
            val value = object : BigDecimal(v[2]) {
                override fun signum(): Int { trace.append("sign;"); return if (op == "sqrtSign") v[3].toInt() else super.signum() }
                override fun precision(): Int { trace.append("precision;"); return if (op == "sqrtPrecision") v[3].toInt() else super.precision() }
                override fun scale(): Int { trace.append("scale;"); return super.scale() }
                override fun stripTrailingZeros(): BigDecimal {
                    if (op == "sqrtNormalize") {
                        trace.append("strip;")
                        if (v[3] != "~") return BigDecimal(v[3])
                    }
                    return super.stripTrailingZeros()
                }
                override fun scaleByPowerOfTen(n: Int): BigDecimal {
                    if (op == "sqrtNormalize") trace.append("scaleBy:$n;")
                    return super.scaleByPowerOfTen(n)
                }
                override fun toDouble(): Double {
                    if (op == "sqrtNormalize") trace.append("double;")
                    return super.toDouble()
                }
            }
            val context = MathContext(p, RoundingMode.valueOf(v[5]))
            val result = try {
                when (op) {
                    "sqrt", "sqrtSign", "sqrtPrecision", "sqrtNormalize" -> value.sqrt(context)
                    "pow" -> value.pow(v[3].toInt())
                    else -> value.pow(v[3].toInt(), context)
                }.toString()
            } catch (e: Exception) {
                if (op == "sqrtSign" || op == "sqrtPrecision" || op == "sqrtNormalize") "!${e::class.simpleName}:${e.message}"
                else if (e is ArithmeticException) "!${e.message}" else throw e
            }
            return "$result|$trace"
        }
        if (v[0] == "Z") {
            var observedSign = p
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
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
            return "$first|${format()}|$trace"
        }
        if (v[0] == "Y") {
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return p }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun toPlainString(): String { trace.append("plain;"); return super.toPlainString() }
            }
            val result = if (op == "plain") value.toPlainString() else value.toEngineeringString()
            return "$result|$trace"
        }
        if (v[0] == "W") {
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return p }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun toPlainString(): String { trace.append("plain;"); return "custom" }
            }
            val result = value.toString()
            return "$result|$trace"
        }
        if (v[0] == "T" || v[0] == "U") {
            val trace = StringBuilder()
            val value = if (v[0] == "U") object : BigDecimal(v[2]) {
                override fun longValueExact(): Long { trace.append("longExact;"); return v[3].toLong() }
            } else object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun toLong(): Long { trace.append("long;"); return super.toLong() }
                override fun longValueExact(): Long { trace.append("longExact;"); return super.longValueExact() }
                override fun toBigIntegerExact(): BigInteger { trace.append("integerExact;"); return super.toBigIntegerExact() }
                override fun precision(): Int { trace.append("precision;"); return super.precision() }
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal { trace.append("enumScale;"); return super.setScale(newScale, roundingMode) }
                override fun setScale(newScale: Int, roundingMode: Int): BigDecimal { trace.append("intScale;"); return super.setScale(newScale, roundingMode) }
                override fun setScale(newScale: Int): BigDecimal { trace.append("scale;"); return super.setScale(newScale) }
            }
            val result = try { when (op) {
                "byte" -> value.byteValueExact()
                "short" -> value.shortValueExact()
                "int" -> value.intValueExact()
                "long" -> value.longValueExact()
                "integer" -> value.toBigIntegerExact()
                "text" -> value.toString()
                "scale" -> value.setScale(p)
                "enumScale" -> value.setScale(p, RoundingMode.valueOf(v[5]))
                "intScale" -> value.setScale(p, RoundingMode.valueOf(v[5]).ordinal)
                else -> error("Unknown exact decimal operation $op")
            }.toString() } catch (failure: Exception) { "!${failure::class.simpleName}:${failure.message}" }
            return "$result|$trace"
        }
        if (v[0] == "X") {
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return p }
                override fun precision(): Int { trace.append("precision;"); return 1 }
            }
            val bits = if (op == "float") value.toFloat().toBits().toLong() else value.toDouble().toBits()
            return "$bits|$trace"
        }
        if (v[0] == "E") {
            val trace = StringBuilder()
            val value = object : BigInteger(v[2]) {
                override fun bitLength(): Int { trace.append("bits;"); return v[3].toInt() }
                override fun toInt(): Int { trace.append("int;"); return 44 }
                override fun toLong(): Long { trace.append("long;"); return 45L }
                override fun toByte(): Byte { trace.append("byte;"); return 42 }
                override fun toShort(): Short { trace.append("short;"); return 43 }
            }
            val result = try { when (op) {
                "intExact" -> value.intValueExact()
                "longExact" -> value.longValueExact()
                "shortExact" -> value.shortValueExact()
                "byteExact" -> value.byteValueExact()
                else -> error("Unknown exact conversion $op")
            }.toString() } catch (e: ArithmeticException) {
                assertNull(e.cause)
                "!${e::class.simpleName}:${e.message}"
            }
            return "$result|$trace"
        }
        if (v[0] == "P") {
            val value = object : BigInteger(v[2]) { override fun toLong(): Long = v[3].toLong() }
            return "${value.toInt()}|${value.toDouble().toBits()}|${value.toFloat().toBits()}"
        }
        if (v[0] == "R") {
            val bits = v[2].toInt()
            val random: PlatformRandom? = null
            return when (op) {
                "random" -> BigInteger(bits, random)
                "prime" -> BigInteger(bits, v[3].toInt(), random)
                "probablePrime" -> BigInteger.probablePrime(bits, random)
                else -> error("Unknown random operation $op")
            }
        }
        if (v[0] == "B") {
            val bytes = if (v[2].isEmpty()) byteArrayOf() else v[2].split(',').map { it.toByte() }.toByteArray()
            return if (op == "twos") BigInteger(bytes, p, v[3].toInt()) else BigInteger(v[5].toInt(), bytes, p, v[3].toInt())
        }
        val mode = RoundingMode.valueOf(v[5])
        if (v[0] == "G") {
            fun operand(text: String): BigDecimal {
                val pieces = text.split('@')
                return BigDecimal(BigInteger(pieces[0]), pieces[1].toInt())
            }
            return try {
                val a = operand(v[2]); val b = operand(v[3])
                when (op) {
                    "scaledDivide" -> a.divide(b, p, mode).toString()
                    "setScale" -> a.setScale(p, mode).toString()
                    "byteExact" -> a.byteValueExact().toString()
                    "shortExact" -> a.shortValueExact().toString()
                    "intExact" -> a.intValueExact().toString()
                    "longExact" -> a.longValueExact().toString()
                    else -> {
                        val mc = MathContext(p, mode)
                        when (op) {
                            "divide" -> a.divide(b, mc)
                            "exactDivide" -> a.divide(b)
                            "integral" -> a.divideToIntegralValue(b)
                            "integralContext" -> a.divideToIntegralValue(b, mc)
                            "remainder" -> a.remainder(b)
                            "remainderContext" -> a.remainder(b, mc)
                            "pair" -> a.divideAndRemainder(b)
                            "pairContext" -> a.divideAndRemainder(b, mc)
                            else -> error("Unknown decimal message operation $op")
                        }.toString()
                    }
                }
            }
                catch (failure: Exception) { "!${failure::class.simpleName}:${failure.message}" }
        }
        if (v[0] == "F") {
            val d = Double.fromBits(v[2].toLong())
            return if (op == "construct") BigDecimal(d) else BigDecimal.valueOf(d)
        }
        if (v[0] == "I") {
            val a = BigInteger(v[2]); val b = BigInteger(v[3])
            return when (op) {
                "parse" -> a
                "add" -> a.add(b)
                "subtract" -> a.subtract(b)
                "multiply" -> a.multiply(b)
                "divide" -> a.divideAndRemainder(b)
                "gcd" -> a.gcd(b)
                "and" -> a.and(b)
                "or" -> a.or(b)
                "xor" -> a.xor(b)
                "andNot" -> a.andNot(b)
                "not" -> a.not()
                "bits" -> "${a.bitLength()}|${a.bitCount()}|${a.getLowestSetBit()}"
                "testBit" -> a.testBit(p)
                "setBit" -> a.setBit(p)
                "clearBit" -> a.clearBit(p)
                "flipBit" -> a.flipBit(p)
                "shift" -> a.shiftLeft(p)
                "shiftRight" -> a.shiftRight(p)
                "sqrt" -> a.sqrtAndRemainder()
                "double" -> a.toDouble().toBits()
                "float" -> a.toFloat().toBits()
                "bytes" -> a.toByteArray().joinToString(",")
                "longExact" -> a.longValueExact()
                "intExact" -> a.intValueExact()
                "shortExact" -> a.shortValueExact()
                "byteExact" -> a.byteValueExact()
                "radix" -> a.toString(p)
                "pow" -> a.pow(p)
                else -> error("Unknown integer operation $op")
            }
        }
        val a = BigDecimal(v[2]); val b = BigDecimal(v[3])
        val mc = MathContext(p.coerceAtLeast(0), mode)
        return when (op) {
            "parse" -> a
            "plain" -> a.toPlainString()
            "engineering" -> a.toEngineeringString()
            "add" -> a.add(b, mc)
            "subtract" -> a.subtract(b, mc)
            "multiply" -> a.multiply(b, mc)
            "divide" -> a.divide(b, mc)
            "exactDivide" -> a.divide(b)
            "integral" -> a.divideToIntegralValue(b, mc)
            "remainder" -> a.divideAndRemainder(b, mc)
            "round" -> a.round(mc)
            "scale" -> a.setScale(p, mode)
            "divideScale" -> a.divide(b, p, mode)
            "strip" -> a.stripTrailingZeros()
            "sqrt" -> a.sqrt(mc)
            "pow" -> a.pow(v[3].toInt(), mc)
            "double" -> a.toDouble().toBits()
            "float" -> a.toFloat().toBits()
            "integerExact" -> a.toBigIntegerExact()
            else -> error("Unknown decimal operation $op")
        }
    }
    @Test fun immutableValuesAndAlgebraicInvariants() {
        val bytes = byteArrayOf(0x12, 0x34, 0x56)
        val a = BigInteger(bytes)
        bytes.fill(0)
        assertEquals("1193046", a.toString())
        val encoded = a.toByteArray(); encoded.fill(0)
        assertEquals("1193046", a.toString())
        val b = BigInteger("999999999999999999999999999999999999")
        assertEquals(a, a.add(b).subtract(b))
        assertEquals(b, b.multiply(a).divide(a))
        assertEquals(BigInteger.ONE.negate(), a.or(a.not()))
        assertEquals(BigInteger.ZERO, a.and(a.not()))
        assertEquals(BigInteger.ONE.negate(), BigInteger("-3").shiftRight(100))
        assertNotEquals(BigDecimal("1.0"), BigDecimal("1.00"))
        assertEquals(0, BigDecimal("1.0").compareTo(BigDecimal("1.00")))
        assertEquals("0.50", BigDecimal("1.00").divide(BigDecimal.TWO).toString())
        assertEquals("0.3333333", BigDecimal.ONE.divide(BigDecimal(3), MathContext.DECIMAL32).toString())
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divide(BigDecimal(3)) }
        assertFailsWith<ArithmeticException> { BigInteger.ONE.divide(BigInteger.ZERO) }
    }
    @Test fun contextsAndRoundingConstants() {
        assertEquals(34, MathContext.DECIMAL128.getPrecision())
        assertEquals(RoundingMode.HALF_EVEN, MathContext.DECIMAL128.getRoundingMode())
        assertEquals(MathContext.DECIMAL128, MathContext(MathContext.DECIMAL128.toString()))
        assertEquals(RoundingMode.HALF_EVEN, RoundingMode.valueOf(6))
        assertFailsWith<IllegalArgumentException> { MathContext(-1) }
        assertFailsWith<IllegalArgumentException> { RoundingMode.valueOf(8) }
        assertFailsWith<IllegalArgumentException> { MathContext("precision=7 roundingMode=INVALID") }
    }

    @Test fun modularArithmeticAndRandomGeneration() {
        val a = BigInteger("12345678901234567890")
        val modulus = BigInteger("1000000007")
        val inverse = a.modInverse(modulus)
        assertEquals(BigInteger.ONE, a.multiply(inverse).mod(modulus))
        assertEquals(inverse.pow(3).mod(modulus), a.modPow(BigInteger("-3"), modulus))
        assertEquals(BigInteger.ZERO, a.modPow(BigInteger.ZERO, BigInteger.ONE))
        assertFailsWith<ArithmeticException> { BigInteger.TWO.modInverse(BigInteger.TEN) }
        assertFailsWith<ArithmeticException> { a.mod(BigInteger.ZERO) }
        assertFailsWith<ArithmeticException> { a.modPow(BigInteger.ONE, -modulus) }
        val random = kotlin.random.Random(42)
        for (bits in listOf(0, 1, 2, 29, 30, 31, 63, 64, 100)) {
            val n = BigInteger(bits, random)
            assertTrue(n.signum() >= 0)
            assertTrue(n.bitLength() <= bits)
        }
        for (bits in listOf(2, 3, 8, 16, 32)) {
            val prime = BigInteger.probablePrime(bits, random)
            assertEquals(bits, prime.bitLength())
            assertTrue(prime.isProbablePrime(60))
        }
        assertEquals(BigInteger("101"), BigInteger("100").nextProbablePrime())
        assertTrue(BigInteger("2147483647").isProbablePrime(60))
        for (n in listOf("0", "1", "561", "1105", "1729", "3215031751"))
            assertFalse(BigInteger(n).isProbablePrime(60))
        assertTrue(BigInteger.ZERO.isProbablePrime(0))
    }
}

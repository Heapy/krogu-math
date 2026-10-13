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
    private fun evaluate(v: Array<String>): Any {
        val op = v[1]
        val p = v[4].toInt()
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
            return try { operand(v[2]).divide(operand(v[3]), MathContext(p, mode)).toString() }
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

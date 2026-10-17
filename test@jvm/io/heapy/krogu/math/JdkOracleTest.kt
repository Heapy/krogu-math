package io.heapy.krogu.math

import java.math.BigInteger as JInteger
import java.math.BigDecimal as JDecimal
import java.math.MathContext as JContext
import java.math.RoundingMode as JRound
import kotlin.random.Random
import kotlin.test.*

/** Differential tests execute our common implementation, never JVM delegated arithmetic. */
class JdkOracleTest {
    private val random = Random(0x4b524f47)

    private fun outcome(block: () -> Any?): String = try {
        val value = block()
        if (value is Array<*>) value.joinToString("|") else value.toString()
    } catch (e: Exception) { "!${e.javaClass.simpleName}" }
    private fun same(label: String, jdk: () -> Any?, common: () -> Any?) {
        assertEquals(outcome(jdk), outcome(common), label)
    }
    private fun sameSetScale(label: String, reference: JDecimal, source: BigDecimal, target: Int) {
        val fullDiagnostic = try { reference.setScale(target, JRound.HALF_EVEN).toString() }
            catch (e: Exception) { "!${e.javaClass.simpleName}:${e.message}" }
        val corrected = DecimalScaleRounding566Expected.setScale(source, target, RoundingMode.HALF_EVEN)
        if (corrected != null)
            println("566_JDK_OBSERVATION\t$label\ttarget=$target\t$fullDiagnostic")
        val diagnostic = if (fullDiagnostic.startsWith("!")) fullDiagnostic.substringBefore(':') else fullDiagnostic
        assertEquals(corrected ?: diagnostic, outcome { source.setScale(target, RoundingMode.HALF_EVEN) },
            "$label; JDK=$fullDiagnostic")
    }
    private fun integer(bits: Int = random.nextInt(1, 512)): String {
        val bytes = random.nextBytes((bits + 7) / 8)
        return JInteger(bytes).toString()
    }
    private fun decimal(): String = JDecimal(JInteger(integer(random.nextInt(1, 150))), random.nextInt(-30, 45)).toString()

    @Test fun integerArithmeticAndBits() {
        repeat(300) {
            val sa = integer(); val sb = integer()
            val a = BigInteger(sa); val b = BigInteger(sb)
            val x = JInteger(sa); val y = JInteger(sb)
            val label = "$sa ; $sb"
            same("add $label", { x.add(y) }, { a.add(b) })
            same("subtract $label", { x.subtract(y) }, { a.subtract(b) })
            same("multiply $label", { x.multiply(y) }, { a.multiply(b) })
            same("parallelMultiply $label", { x.parallelMultiply(y) }, { a.parallelMultiply(b) })
            same("divide $label", { x.divideAndRemainder(y) }, { a.divideAndRemainder(b) })
            same("gcd $label", { x.gcd(y) }, { a.gcd(b) })
            same("and $label", { x.and(y) }, { a.and(b) })
            same("or $label", { x.or(y) }, { a.or(b) })
            same("xor $label", { x.xor(y) }, { a.xor(b) })
            same("andNot $label", { x.andNot(y) }, { a.andNot(b) })
            same("not $sa", { x.not() }, { a.not() })
            val shift = random.nextInt(-600, 600)
            same("shiftLeft $sa $shift", { x.shiftLeft(shift) }, { a.shiftLeft(shift) })
            same("shiftRight $sa $shift", { x.shiftRight(shift) }, { a.shiftRight(shift) })
            val bit = random.nextInt(0, 600)
            same("setBit", { x.setBit(bit) }, { a.setBit(bit) })
            same("clearBit", { x.clearBit(bit) }, { a.clearBit(bit) })
            same("flipBit", { x.flipBit(bit) }, { a.flipBit(bit) })
            same("testBit", { x.testBit(bit) }, { a.testBit(bit) })
            assertEquals(x.bitCount(), a.bitCount(), "bitCount $sa")
            assertEquals(x.bitLength(), a.bitLength(), "bitLength $sa")
            assertEquals(x.lowestSetBit, a.getLowestSetBit())
            assertEquals(x.hashCode(), a.hashCode(), "hash $sa")
            assertContentEquals(x.toByteArray(), a.toByteArray(), "bytes $sa")
            assertEquals(a, BigInteger(a.toByteArray()))
            val radix = random.nextInt(2, 37)
            assertEquals(x.toString(radix), a.toString(radix))
            assertEquals(a, BigInteger(x.toString(radix), radix))
            assertEquals(x.toLong(), a.toLong())
            assertEquals(x.toInt(), a.toInt())
            assertEquals(x.toDouble().toBits(), a.toDouble().toBits(), "double $sa")
            assertEquals(x.toFloat().toBits(), a.toFloat().toBits(), "float $sa")
            same("int exact", { x.intValueExact() }, { a.intValueExact() })
            same("long exact", { x.longValueExact() }, { a.longValueExact() })
            same("sqrt $sa", { x.sqrtAndRemainder() }, { a.sqrtAndRemainder() })
        }
    }
    @Test fun modularArithmeticAndPrimes() {
        repeat(70) {
            val sa = integer(80); val sm = JInteger(integer(60)).abs().add(JInteger.ONE).toString()
            val a = BigInteger(sa); val m = BigInteger(sm)
            val x = JInteger(sa); val y = JInteger(sm)
            val e = random.nextInt(-20, 40).toLong()
            same("mod $sa $sm", { x.mod(y) }, { a.mod(m) })
            same("inverse $sa $sm", { x.modInverse(y) }, { a.modInverse(m) })
            same("modPow $sa $e $sm", { x.modPow(JInteger.valueOf(e), y) }, { a.modPow(BigInteger.valueOf(e), m) })
        }
        for (i in -20..200) {
            assertEquals(JInteger.valueOf(i.toLong()).isProbablePrime(60), BigInteger.valueOf(i.toLong()).isProbablePrime(60), "prime $i")
            same("nextPrime $i", { JInteger.valueOf(i.toLong()).nextProbablePrime() }, { BigInteger.valueOf(i.toLong()).nextProbablePrime() })
        }
        for (composite in listOf("561", "1105", "1729", "3215031751", "3825123056546413051"))
            assertFalse(BigInteger(composite).isProbablePrime(100))
        for (bits in listOf(2, 3, 16, 64, 128)) {
            val prime = BigInteger.probablePrime(bits, random)
            assertEquals(bits, prime.bitLength())
            assertTrue(prime.toJavaBigInteger().isProbablePrime(100))
        }
    }
    @Test fun decimals() {
        val checks = SqrtCohort587Comparison.Checks("live-decimals")
        val roots = SqrtCohort587Comparison.Recorder("live-decimals", SqrtCohort587Comparison.Profile.LIVE, 350, "LIVE_JDK")
        fun same(label: String, jdk: () -> Any?, common: () -> Any?) = checks.check(label, jdk, common)
        repeat(350) { iteration ->
            val sa = decimal(); val sb = decimal()
            val a = BigDecimal(sa); val b = BigDecimal(sb)
            val x = JDecimal(sa); val y = JDecimal(sb)
            val mode = RoundingMode.entries[random.nextInt(8)]
            val jmode = JRound.valueOf(mode.name)
            val p = random.nextInt(0, 35)
            val mc = MathContext(p, mode); val jc = JContext(p, jmode)
            val label = "$sa ; $sb ; $mc"
            checks.strict("parse $sa", { x.toString() }, { a.toString() })
            checks.strict("plain $sa", { x.toPlainString() }, { a.toPlainString() })
            checks.strict("engineering $sa", { x.toEngineeringString() }, { a.toEngineeringString() })
            checks.strict("scale $sa", { x.scale() }, { a.scale() })
            checks.strict("precision $sa", { x.precision() }, { a.precision() })
            checks.strict("decimal hash $sa", { x.hashCode() }, { a.hashCode() })
            checks.strict("compare $label", { x.compareTo(y).compareTo(0) }, { a.compareTo(b).compareTo(0) })
            same("add $label", { x.add(y) }, { a.add(b) })
            same("add mc $label", { x.add(y, jc) }, { a.add(b, mc) })
            same("subtract mc $label", { x.subtract(y, jc) }, { a.subtract(b, mc) })
            same("multiply mc $label", { x.multiply(y, jc) }, { a.multiply(b, mc) })
            same("divide exact $label", { x.divide(y) }, { a.divide(b) })
            same("divide mc $label", { x.divide(y, jc) }, { a.divide(b, mc) })
            val scale = random.nextInt(-40, 60)
            same("divide scale=$scale $label", { x.divide(y, scale, jmode) }, { a.divide(b, scale, mode) })
            same("setScale=$scale $label", { x.setScale(scale, jmode) }, { a.setScale(scale, mode) })
            same("round $label", { x.round(jc) }, { a.round(mc) })
            same("integral $label", { x.divideToIntegralValue(y) }, { a.divideToIntegralValue(b) })
            same("remainder mc $label", { x.divideAndRemainder(y, jc) }, { a.divideAndRemainder(b, mc) })
            same("strip $sa", { x.stripTrailingZeros() }, { a.stripTrailingZeros() })
            val n = random.nextInt(-20, 20)
            same("pointLeft $sa $n", { x.movePointLeft(n) }, { a.movePointLeft(n) })
            same("pointRight $sa $n", { x.movePointRight(n) }, { a.movePointRight(n) })
            same("powerTen $sa $n", { x.scaleByPowerOfTen(n) }, { a.scaleByPowerOfTen(n) })
            same("integer $sa", { x.toBigInteger() }, { a.toBigInteger() })
            same("integer exact $sa", { x.toBigIntegerExact() }, { a.toBigIntegerExact() })
            checks.strict("decimal double $sa", { x.toDouble().toBits() }, { a.toDouble().toBits() })
            checks.strict("decimal float $sa", { x.toFloat().toBits() }, { a.toFloat().toBits() })
            checks.strict("long $sa", { x.toLong() }, { a.toLong() })
            checks.strict("int $sa", { x.toInt() }, { a.toInt() })
            checks.accountSqrt()
            val reference = SqrtCohort587Comparison.reference({ x.sqrt(jc) }) { r ->
                SqrtCohort587Comparison.Fields(r.unscaledValue().toString(), r.scale(), r.precision(), r.toString())
            }
            val actual = SqrtCohort587Comparison.own { a.sqrt(mc) }
            roots.record(iteration.toString(), SqrtCohort587Comparison.input(a, sa), p, mode, reference, actual)
            val power = random.nextInt(-8, 9)
            same("pow $power $label", { x.pow(power, jc) }, { a.pow(power, mc) })
        }
        roots.finish()
        checks.finish(10500)
    }
    @Test fun floatingPointBoundaries() {
        val values = mutableListOf(0.0, -0.0, Double.MIN_VALUE, Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NaN,
            0.1, 1e-4, 1e-3, 1e7, 1e23, 2.2250738585072014e-308)
        repeat(150) { values.add(Double.fromBits(random.nextLong())) }
        for (d in values) {
            same("double constructor $d", { JDecimal(d) }, { BigDecimal(d) })
            same("valueOf $d", { JDecimal.valueOf(d) }, { BigDecimal.valueOf(d) })
            if (d.isFinite()) {
                val actual = BigDecimal(d).toDouble()
                assertEquals(if (d == 0.0) 0L else d.toBits(), actual.toBits(), "round trip $d")
            }
        }
        for (bits in listOf(24, 53, 127, 128, 1023, 1024, 1025)) {
            val center = JInteger.ONE.shiftLeft(bits)
            for (offset in -3L..3L) {
                val x = center.add(JInteger.valueOf(offset)); val a = BigInteger(x.toString())
                assertEquals(x.toDouble().toBits(), a.toDouble().toBits(), "integer double $x")
                assertEquals(x.toFloat().toBits(), a.toFloat().toBits(), "integer float $x")
            }
        }
    }
    @Test fun serializationAndJavaAdapters() {
        for (value in listOf(BigInteger("-12345678901234567890123456789"), BigDecimal("1.234500"), MathContext.DECIMAL128)) {
            val bytes = java.io.ByteArrayOutputStream()
            java.io.ObjectOutputStream(bytes).use { it.writeObject(value) }
            val copy = java.io.ObjectInputStream(java.io.ByteArrayInputStream(bytes.toByteArray())).use { it.readObject() }
            assertEquals(value, copy)
        }
        val a = BigDecimal("123.4500")
        assertEquals(a, a.toJavaBigDecimal().toKroguBigDecimal())
        assertEquals(MathContext.DECIMAL64, MathContext.DECIMAL64.toJavaMathContext().toKroguMathContext())
        val r = java.util.Random(42)
        val j = java.util.Random(42)
        assertEquals(JInteger(100, j).toString(), BigInteger(100, r).toString())
    }
    @Test fun invalidInputsAndScaleLimits() {
        for (s in listOf("precision=7 roundingMode=HALF_EVEN", "precision=+7 roundingMode=UP", "precision=-0 roundingMode=DOWN",
            "precision=0007 roundingMode=FLOOR", "precision=-1 roundingMode=UP", "precision=7 roundingMode=UP ",
            "precision=7  roundingMode=UP", "precision=7 roundingMode=INVALID")) {
            same("context [$s]", { JContext(s) }, { MathContext(s) })
        }
        for (s in listOf("", "+", "-", " 1", "1 ", "1_0", "--1", "1.2.3", "NaN", "1E", "1E+", "1E2147483648", "1E-2147483648", "0.1E-2147483647", ".", "١٢٣.٤٥", "１２３")) {
            same("parse decimal [$s]", { JDecimal(s) }, { BigDecimal(s) })
            same("parse integer [$s]", { JInteger(s) }, { BigInteger(s) })
        }
        for (s in listOf("0E+2147483647", "1E+2147483647", "0E-2147483647", "1E-2147483647", "10E+2147483647")) {
            val x = JDecimal(s); val a = BigDecimal(s)
            same("scale overflow $s", { x.scaleByPowerOfTen(Int.MAX_VALUE) }, { a.scaleByPowerOfTen(Int.MAX_VALUE) })
            same("scale underflow $s", { x.scaleByPowerOfTen(Int.MIN_VALUE) }, { a.scaleByPowerOfTen(Int.MIN_VALUE) })
            same("extreme strip $s", { x.stripTrailingZeros() }, { a.stripTrailingZeros() })
            assertEquals(x.toDouble().toBits(), a.toDouble().toBits())
            assertEquals(x.toLong(), a.toLong())
            sameSetScale("tiny setScale $s", x, a, -10)
            sameSetScale("tiny setScale zero $s", x, a, 0)
            val diagnostic = outcome { x.divide(JDecimal.ONE, 0, JRound.HALF_EVEN) }
            val expected = if (DecimalScaleContractExpected.affectedScaledDivision(a, BigDecimal.ONE, 0))
                DecimalScaleContractExpected.scaledDivide(a, BigDecimal.ONE, 0, RoundingMode.HALF_EVEN)
            else diagnostic
            assertEquals(expected, outcome { a.divide(BigDecimal.ONE, 0, RoundingMode.HALF_EVEN) },
                "tiny divide $s JDK diagnostic=$diagnostic")
            if (s in listOf("0E+2147483647", "0E-2147483647", "1E-2147483647"))
                assertEquals("!ArithmeticException", diagnostic, "retained pinned-JDK zero/tiny defect $s")
        }
    }
}

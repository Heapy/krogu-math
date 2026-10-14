package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Suppress("DEPRECATION")
class DecimalScaleContractTest {
    @Test fun DecimalScaleContractBoundedExactWitnesses() {
        val quotient = BigDecimal(BigInteger.ONE, Int.MAX_VALUE)
        val divisor = BigDecimal(BigInteger.ONE, Int.MIN_VALUE)
        assertEquals(0, BigDecimal.TEN.compareTo(quotient.multiply(divisor)))
        assertEquals(-1, quotient.compareTo(BigDecimal("0.5")))
        assertPair("0", 0, BigDecimal.TEN.divide(divisor, 0, RoundingMode.HALF_EVEN))
        val left = BigDecimal.valueOf(10000, Int.MAX_VALUE - 1)
        val right = BigDecimal(BigInteger.ONE, Int.MAX_VALUE)
        assertPair("100001", Int.MAX_VALUE, left.add(right))
        assertPair("1", Int.MAX_VALUE - 5, left.add(right, MathContext(1, RoundingMode.HALF_EVEN)))
    }

    @Test fun DecimalScaleContractTinyQuotientsAcrossSignedScaleDistances() {
        // At the requested place every quotient's magnitude is between zero
        // and 0.1, so its rounded coefficient needs no enormous power of ten.
        for (sourceScale in listOf(-1, 0, 1, Int.MAX_VALUE - 1, Int.MAX_VALUE)) {
            for (divisorScale in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 2)) {
                for (target in listOf(Int.MIN_VALUE, -1, 0, 1)) {
                    for (leftSign in listOf(-1L, 1L)) for (rightSign in listOf(-1L, 1L)) {
                        val a = BigDecimal.valueOf(10 * leftSign, sourceScale)
                        val b = BigDecimal.valueOf(rightSign, divisorScale)
                        for (mode in RoundingMode.entries) {
                            val sign = (leftSign * rightSign).toInt()
                            val label = "$a / $b scale=$target $mode"
                            checkTiny(sign, target, mode, label) { a.divide(b, target, mode) }
                            checkTiny(sign, target, mode, label) { a.divide(b, target, mode.ordinal) }
                        }
                    }
                }
            }
        }
    }

    @Test fun DecimalScaleContractRoundingOnlyLegacyAndOperator() {
        for (sourceScale in listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE)) {
            for (divisorScale in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 2)) {
                for (leftSign in listOf(-1L, 1L)) for (rightSign in listOf(-1L, 1L)) {
                    val a = BigDecimal.valueOf(10 * leftSign, sourceScale)
                    val b = BigDecimal.valueOf(rightSign, divisorScale)
                    val sign = (leftSign * rightSign).toInt()
                    for (mode in RoundingMode.entries) {
                        checkTiny(sign, sourceScale, mode, "$a / $b $mode") { a.divide(b, mode) }
                        checkTiny(sign, sourceScale, mode, "$a / $b legacy $mode") { a.divide(b, mode.ordinal) }
                    }
                    assertPair("0", sourceScale, a / b)
                }
            }
        }
    }

    @Test fun DecimalScaleContractZeroAndZeroDivisors() {
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -1, 0, 1, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        for (source in scales) for (divisor in scales) for (target in scales) {
            val zero = BigDecimal(BigInteger.ZERO, source)
            val nonzero = BigDecimal(BigInteger.ONE, divisor)
            for (mode in RoundingMode.entries) {
                assertPair("0", target, zero.divide(nonzero, target, mode))
                assertFailsWith<ArithmeticException> { nonzero.divide(zero, target, mode) }
                assertFailsWith<ArithmeticException> { zero.divide(zero, target, mode) }
            }
            assertPair("0", source, zero / nonzero)
        }
    }

    @Test fun DecimalScaleContractBoundedRescalingAndHalfwayNeighbors() {
        for (source in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MAX_VALUE - 1, Int.MAX_VALUE)) {
            for (target in listOf(source, if (source == Int.MAX_VALUE) source - 1 else source + 1)) {
                for (n in listOf(-7L, -5L, -1L, 0L, 1L, 5L, 7L)) for (d in listOf(-2L, 2L)) {
                    val a = BigDecimal.valueOf(n, source)
                    val b = BigDecimal.valueOf(d, 0)
                    for (mode in RoundingMode.entries) {
                        val expected = DecimalScaleContractExpected.scaledDivide(a, b, target, mode)
                        assertEquals(expected, decimalOutcome { a.divide(b, target, mode) })
                    }
                }
            }
        }
        // Temporary target + divisorScale is outside Int; the net shift is only one.
        assertPair("50", Int.MAX_VALUE, BigDecimal.valueOf(5, Int.MAX_VALUE)
            .divide(BigDecimal.valueOf(1, 1), Int.MAX_VALUE, RoundingMode.UNNECESSARY))
        for (mode in RoundingMode.entries) {
            val a = BigDecimal.valueOf(5, Int.MIN_VALUE)
            val b = BigDecimal.valueOf(1, -1)
            assertEquals(DecimalScaleContractExpected.scaledDivide(a, b, Int.MIN_VALUE, mode),
                decimalOutcome { a.divide(b, Int.MIN_VALUE, mode) })
        }
        // The tiny shortcut must leave the 0.1..0.5..1 rounding interval to exact division.
        for (n in listOf(1L, 4L, 5L, 6L, 9L)) for (sign in listOf(-1L, 1L)) {
            val a = BigDecimal.valueOf(n * sign)
            for (mode in RoundingMode.entries) {
                assertEquals(DecimalScaleContractExpected.scaledDivide(a, BigDecimal.TEN, 0, mode),
                    decimalOutcome { a.divide(BigDecimal.TEN, 0, mode) })
            }
        }
    }

    @Test fun DecimalScaleContractContextAdditionAndSubtraction() {
        for (scale in listOf(Int.MIN_VALUE + 1, 0, Int.MAX_VALUE - 2, Int.MAX_VALUE - 1, Int.MAX_VALUE)) {
            for (left in listOf(-10000L, 10000L, -12345L, 12345L)) for (right in listOf(-1L, 1L)) {
                val a = BigDecimal.valueOf(left, scale - 1)
                val b = BigDecimal.valueOf(right, scale)
                val exact = left * 10 + right
                assertPair(exact.toString(), scale, a.add(b))
                for (p in listOf(1, 2, 5, 6, 7)) for (mode in RoundingMode.entries) {
                    val mc = MathContext(p, mode)
                    val expected = DecimalScaleContractExpected.roundInteger(BigInteger.valueOf(exact), scale, mc)
                    assertEquals(expected, decimalOutcome { a.add(b, mc) }, "$a + $b $mc")
                    assertEquals(expected, decimalOutcome { b.add(a, mc) }, "commuted $mc")
                    assertEquals(expected, decimalOutcome { a.subtract(b.negate(), mc) }, "subtract $mc")
                }
            }
        }
    }

    @Test fun DecimalScaleContractTruthfulSubclasses() {
        class Decimal(c: BigInteger, s: Int) : BigDecimal(c, s) {
            override fun precision(): Int = super.precision()
            override fun signum(): Int = super.signum()
            override fun scale(): Int = super.scale()
        }
        val a = Decimal(BigInteger.TEN, 0)
        val b = Decimal(BigInteger.ONE, Int.MIN_VALUE)
        assertPair("0", 0, a / b)
        val x = Decimal(BigInteger.valueOf(10000), Int.MAX_VALUE - 1)
        val y = Decimal(BigInteger.ONE, Int.MAX_VALUE)
        assertPair("1", Int.MAX_VALUE - 5, x.add(y, MathContext(1, RoundingMode.HALF_EVEN)))
    }

    private fun checkTiny(sign: Int, scale: Int, mode: RoundingMode, label: String, action: () -> BigDecimal) {
        if (mode == RoundingMode.UNNECESSARY) assertFailsWith<ArithmeticException>(label, action)
        else assertPair(DecimalScaleContractExpected.tinyCoefficient(sign, mode).toString(), scale, action())
    }

    private fun assertPair(coefficient: String, scale: Int, actual: BigDecimal) {
        assertEquals(coefficient, actual.unscaledValue().toString())
        assertEquals(scale, actual.scale())
    }
}

internal fun decimalOutcome(action: () -> BigDecimal): String = try { action().toString() }
    catch (_: ArithmeticException) { "!ArithmeticException" }

/** Independent expectations: integer alignment and decimal text rounding, never BigDecimal arithmetic. */
internal object DecimalScaleContractExpected {
    fun tinyCoefficient(sign: Int, mode: RoundingMode): Int = when (mode) {
        RoundingMode.UP -> sign
        RoundingMode.CEILING -> if (sign > 0) 1 else 0
        RoundingMode.FLOOR -> if (sign < 0) -1 else 0
        RoundingMode.DOWN, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN -> 0
        RoundingMode.UNNECESSARY -> throw ArithmeticException("Rounding necessary")
    }

    fun affectedAddition(a: BigDecimal, b: BigDecimal, mc: MathContext): Boolean {
        if (a.signum() == 0 || b.signum() == 0 || mc.precision == 0 ||
            minOf(a.scale(), b.scale()) != Int.MAX_VALUE - 1 || maxOf(a.scale(), b.scale()) != Int.MAX_VALUE) return false
        val dominant = if (a.scale() < b.scale()) a else b
        val remote = if (dominant === a) b else a
        return remote.scale().toLong() - remote.precision() + 1 >
            dominant.scale().toLong() - dominant.precision() + mc.precision + 2
    }

    fun addition(a: BigDecimal, b: BigDecimal, mc: MathContext): String {
        val scale = maxOf(a.scale(), b.scale())
        // Only the proved one-place alignment domain may replace oracle expectations.
        require(kotlin.math.abs(a.scale().toLong() - b.scale()) == 1L)
        val left = if (a.scale() < scale) a.unscaledValue().multiply(BigInteger.TEN) else a.unscaledValue()
        val right = if (b.scale() < scale) b.unscaledValue().multiply(BigInteger.TEN) else b.unscaledValue()
        return roundInteger(left.add(right), scale, mc)
    }

    fun roundInteger(coefficient: BigInteger, scale: Int, mc: MathContext): String {
        val digits = coefficient.abs().toString()
        val drop = maxOf(0, digits.length - mc.precision)
        if (drop == 0) return BigDecimal(coefficient, scale).toString()
        val retained = BigInteger(digits.dropLast(drop))
        val tail = digits.takeLast(drop)
        val nonzero = tail.any { it != '0' }
        val half = tail[0].compareTo('5').let { if (it == 0 && tail.drop(1).any { c -> c != '0' }) 1 else it }
        return rounded(coefficient.signum(), retained, nonzero, half, scale.toLong() - drop, mc.roundingMode, mc.precision)
    }

    fun scaledDivide(a: BigDecimal, b: BigDecimal, target: Int, mode: RoundingMode): String {
        require(b.signum() != 0)
        if (a.signum() == 0) return BigDecimal(BigInteger.ZERO, target).toString()
        val shift = target.toLong() + b.scale() - a.scale()
        // This strict digit bound proves |result coefficient| < 0.1 without large powers.
        if (a.precision().toLong() - b.precision() + shift < -1) {
            return if (mode == RoundingMode.UNNECESSARY) "!ArithmeticException"
            else BigDecimal(BigInteger.valueOf(tinyCoefficient(a.signum() * b.signum(), mode).toLong()), target).toString()
        }
        require(shift in -64L..64L)
        val numerator = a.unscaledValue().abs().multiply(BigInteger.TEN.pow(maxOf(shift, 0).toInt()))
        val denominator = b.unscaledValue().abs().multiply(BigInteger.TEN.pow(maxOf(-shift, 0).toInt()))
        val qr = numerator.divideAndRemainder(denominator)
        return rounded(a.signum() * b.signum(), qr[0], qr[1].signum() != 0,
            qr[1].multiply(BigInteger.valueOf(2)).compareTo(denominator), target.toLong(), mode)
    }

    fun affectedScaledDivision(a: BigDecimal, b: BigDecimal, target: Int): Boolean {
        val shift = target.toLong() + b.scale() - a.scale()
        val adjusted = target.toLong() + b.scale()
        val scales = Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()
        val failedStaging = adjusted !in scales || adjusted <= a.scale() && a.scale().toLong() - target !in scales
        if (!failedStaging && kotlin.math.abs(shift) <= 646456992L) return false
        // Only return true where a bounded mathematical expectation is available.
        return a.signum() == 0 || a.precision().toLong() - b.precision() + shift < -1 || shift in -64L..64L
    }

    fun exceedsSupportedCoefficientRange(a: BigDecimal, b: BigDecimal, target: Int): Boolean {
        if (a.signum() == 0) return false
        val shift = target.toLong() + b.scale() - a.scale()
        // |a/b * 10^shift| > 10^lowerExponent. Even truncation therefore
        // needs at least 10^lowerExponent; 10^k > 2^(3k) for k > 0.
        val lowerExponent = a.precision().toLong() - b.precision() + shift - 1
        return lowerExponent > Int.MAX_VALUE.toLong() / 3
    }

    private fun rounded(sign: Int, whole: BigInteger, inexact: Boolean, half: Int, scale: Long,
        mode: RoundingMode, precision: Int = Int.MAX_VALUE): String {
        if (inexact && mode == RoundingMode.UNNECESSARY) return "!ArithmeticException"
        val up = inexact && when (mode) {
            RoundingMode.UP -> true
            RoundingMode.DOWN, RoundingMode.UNNECESSARY -> false
            RoundingMode.CEILING -> sign > 0
            RoundingMode.FLOOR -> sign < 0
            RoundingMode.HALF_UP -> half >= 0
            RoundingMode.HALF_DOWN -> half > 0
            RoundingMode.HALF_EVEN -> half > 0 || half == 0 && whole.toString().last().digitToInt() % 2 == 1
        }
        var magnitude = if (up) whole.add(BigInteger.ONE) else whole
        var resultScale = scale
        if (magnitude.toString().length > precision) {
            magnitude = magnitude.divide(BigInteger.TEN)
            resultScale--
        }
        if (resultScale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) return "!ArithmeticException"
        return BigDecimal(if (sign < 0) magnitude.negate() else magnitude, resultScale.toInt()).toString()
    }
}

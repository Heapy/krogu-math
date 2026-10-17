// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalDivisionNormalizationContractTest {
    private class Delegating(c: BigInteger, s: Int) : BigDecimal(c, s) {
        override fun precision() = super.precision()
        override fun signum() = super.signum()
        override fun scale() = super.scale()
        override fun unscaledValue() = super.unscaledValue()
        override fun divide(divisor: BigDecimal) = super.divide(divisor)
        override fun divide(divisor: BigDecimal, mc: MathContext) = super.divide(divisor, mc)
    }

    @Test fun exactCancellationAndReducedTerminatingDenominators() {
        for ((n, d) in listOf(1L to 1L, 2L to 2L, 125L to 25L, 40L to 16L,
            6L to 15L, 3L to 24L, 700L to 28L, 1200L to 3L)) {
            for (distance in listOf(0, 1, 3, 5, 16)) for (ls in listOf(-1, 1)) for (rs in listOf(-1, 1)) {
                for (mask in 0..3) {
                    val a = input(n * ls, Int.MAX_VALUE - distance, mask and 1 != 0)
                    val b = input(d * rs, 0, mask and 2 != 0)
                    check(a, b, null)
                    for (p in listOf(0, 1, 2, 7, 17)) for (mode in RoundingMode.entries)
                        check(a, b, MathContext(p, mode))
                }
            }
        }
    }

    @Test fun roundedCarriesAndNonterminatingCarries() {
        for ((n, d, ds) in listOf(Triple(19L, 20L, 0), Triple(1L, 10000001L, 7),
            Triple(199L, 200L, 0), Triple(189L, 200L, 0), Triple(191L, 200L, 0), Triple(1999L, 2000L, 0))) {
            for (distance in listOf(0, 1, 5)) for (ls in listOf(-1, 1)) for (rs in listOf(-1, 1)) {
                for (mask in 0..3) for (p in listOf(1, 2, 7)) for (mode in RoundingMode.entries)
                    check(input(n * ls, Int.MAX_VALUE - distance, mask and 1 != 0),
                        input(d * rs, ds, mask and 2 != 0), MathContext(p, mode))
            }
        }
        assertEquals("10@${Int.MAX_VALUE}", DivisionNormalizationExpected.pair(
            input(1999, Int.MAX_VALUE - 1, false), input(2000, 0, false), MathContext(2, RoundingMode.HALF_UP)))
    }

    @Test fun hugePrecisionUsesSmallExactCohort() {
        for (mask in 0..3) for (mode in RoundingMode.entries) for (scale in listOf(0, Int.MAX_VALUE - 5, Int.MAX_VALUE)) {
            val a = input(6, scale, mask and 1 != 0)
            val b = input(15, 0, mask and 2 != 0)
            check(a, b, MathContext(Int.MAX_VALUE, mode))
            check(input(1, scale, mask and 1 != 0), input(1, 0, mask and 2 != 0), MathContext(Int.MAX_VALUE, mode))
        }
    }

    @Test fun terminatingPlacesCanExceedInputDecimalDigits() {
        // 2^800 has 241 decimal digits but needs 800 decimal places; both
        // numerator and expected coefficient remain bounded (5^800, 560 digits).
        val divisor = BigInteger.TWO.pow(800)
        val expectedCoefficient = BigInteger.valueOf(5).pow(800)
        // Check the repeated-cancellation model once, then use the independently
        // known 2^-800 = 5^800 * 10^-800 witness for every operand mask.
        assertEquals("$expectedCoefficient@800", DivisionNormalizationExpected.pair(
            BigDecimal.ONE, BigDecimal(divisor), MathContext(Int.MAX_VALUE, RoundingMode.UNNECESSARY)))
        for (scale in listOf(0, Int.MAX_VALUE - 800, Int.MAX_VALUE - 799)) for (mask in 0..3) {
            val a = input(1, scale, mask and 1 != 0)
            val b = if (mask and 2 == 0) BigDecimal(divisor) else Delegating(divisor, 0)
            val finalScale = scale.toLong() + 800
            val expected = if (finalScale > Int.MAX_VALUE) "!ArithmeticException" else "$expectedCoefficient@$finalScale"
            check(a, b, null, expected)
            check(a, b, MathContext(Int.MAX_VALUE, RoundingMode.UNNECESSARY), expected)
        }
    }

    @Test fun genuineErrorsAndZero() {
        val a = input(1, Int.MAX_VALUE, false)
        assertFailsWith<ArithmeticException> { a.divide(BigDecimal.TWO) }
        assertFailsWith<ArithmeticException> { a.divide(BigDecimal(3)) }
        assertFailsWith<ArithmeticException> { a.divide(BigDecimal.ZERO) }
        for (mode in RoundingMode.entries) {
            check(a, BigDecimal.TWO, MathContext(1, mode))
            assertFailsWith<ArithmeticException> { a.divide(BigDecimal.ZERO, MathContext(1, mode)) }
            assertEquals(Int.MAX_VALUE, BigDecimal(BigInteger.ZERO, Int.MAX_VALUE)
                .divide(BigDecimal.ONE, MathContext(7, mode)).scale())
        }
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divide(BigDecimal(3), MathContext(7, RoundingMode.UNNECESSARY)) }
        for (mask in 0..3) {
            val x = input(1, Int.MAX_VALUE, mask and 1 != 0)
            val y = input(100, 2, mask and 2 != 0)
            check(x, y, null)
            for (mode in RoundingMode.entries) check(x, y, MathContext(7, mode))
        }
    }

    @Test fun unsupportedExactCohortDoesNotPreemptRoundedCarry() {
        val denominator = BigInteger.TWO.pow(800)
        val numerator = BigInteger.valueOf(65).multiply(BigInteger.TEN.pow(240))
        val qr = numerator.divideAndRemainder(denominator)
        assertEquals(BigInteger.valueOf(9), qr[0])
        assertEquals(1, qr[1].multiply(BigInteger.TWO).compareTo(denominator))
        // The exact cohort needs scale MAX+561, but this bounded rational at
        // working scale MAX+1 can carry to 1@MAX without constructing 65*5^800.
        for (leftSign in listOf(-1, 1)) for (rightSign in listOf(-1, 1)) for (mask in 0..3) {
            val a = input(65L * leftSign, Int.MAX_VALUE, mask and 1 != 0)
            val d = if (rightSign < 0) denominator.negate() else denominator
            val b = if (mask and 2 == 0) BigDecimal(d, 239) else Delegating(d, 239)
            val sign = leftSign * rightSign
            for (mode in RoundingMode.entries) {
                val up = when (mode) {
                    RoundingMode.UP, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN -> true
                    RoundingMode.CEILING -> sign > 0
                    RoundingMode.FLOOR -> sign < 0
                    RoundingMode.DOWN, RoundingMode.UNNECESSARY -> false
                }
                val expected = if (up) "$sign@${Int.MAX_VALUE}" else "!ArithmeticException"
                check(a, b, MathContext(1, mode), expected)
            }
            assertFailsWith<ArithmeticException> { a.divide(b) }
        }
    }

    private fun input(n: Long, s: Int, subclass: Boolean): BigDecimal =
        if (subclass) Delegating(BigInteger.valueOf(n), s) else BigDecimal.valueOf(n, s)

    private fun check(a: BigDecimal, b: BigDecimal, mc: MathContext?,
        expected: String = DivisionNormalizationExpected.pair(a, b, mc)) {
        val actual = try {
            val q = if (mc == null) a.divide(b) else a.divide(b, mc)
            "${q.unscaledValue()}@${q.scale()}"
        } catch (_: ArithmeticException) { "!ArithmeticException" }
        assertEquals(expected, actual, "${a.unscaledValue()}@${a.scale()} / ${b.unscaledValue()}@${b.scale()} $mc")
    }
}

/** Bounded rational model. No BigDecimal arithmetic or reference result supplies an expectation. */
internal object DivisionNormalizationExpected {
    private val scales = Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()
    /** Only bounded ordinary input fields with a proved supported final result may replace a JDK error. */
    fun correctedText(a: BigDecimal, b: BigDecimal, mc: MathContext?): String? {
        val preferred = a.scale().toLong() - b.scale()
        if (preferred !in scales || a.signum() == 0 || b.signum() == 0 ||
            a.precision() > 256 || b.precision() > 256 ||
            (mc != null && mc.precision > 256 && mc.precision < Int.MAX_VALUE - 1)) return null
        val left = a.unscaledValue().abs().toString()
        val right = b.unscaledValue().abs().toString()
        val width = maxOf(left.length, right.length)
        val normalized = left.padEnd(width, '0').compareTo(right.padEnd(width, '0'))
        val p = if (mc == null || mc.precision == 0)
            (a.precision().toLong() + (10L * b.precision() + 2) / 3).coerceAtMost(Int.MAX_VALUE.toLong())
            else mc.precision.toLong()
        val normalization = preferred + p + b.precision() - a.precision() - if (normalized > 0) 1 else 0
        if (normalization in scales && kotlin.math.abs(normalization - preferred) <= 646456992L) return null
        val expected = pair(a, b, mc)
        if (expected.startsWith('!')) return null
        return BigDecimal(BigInteger(expected.substringBefore('@')), expected.substringAfter('@').toInt()).toString()
    }
    fun pair(a: BigDecimal, b: BigDecimal, mc: MathContext?): String {
        val preferred = a.scale().toLong() - b.scale()
        require(preferred in scales)
        var n = a.unscaledValue().abs()
        var d = b.unscaledValue().abs()
        if (d.signum() == 0) return "!ArithmeticException"
        if (n.signum() == 0) return "0@$preferred"
        val sign = a.signum() * b.signum()
        val gcd = n.gcd(d)
        var reduced = d.divide(gcd)
        var places = 0
        // Multiply the reduced rational by ten until its denominator divides it.
        var exact = n.divide(gcd)
        // Every successful cancellation at least halves the denominator; a
        // failed cancellation exits. Input bit length bounds this loop.
        while (reduced != BigInteger.ONE) {
            exact = exact.multiply(BigInteger.TEN)
            val cancellation = exact.gcd(reduced)
            exact = exact.divide(cancellation)
            reduced = reduced.divide(cancellation)
            places++
            if (cancellation == BigInteger.ONE) break
        }
        if (reduced == BigInteger.ONE && (mc == null || mc.precision == 0 || exact.toString().length <= mc.precision))
            return result(exact, preferred + places, sign)
        if (mc == null || mc.precision == 0) return "!ArithmeticException"
        if (mc.precision > 256) {
            // In this bounded input domain a nonterminating p>=MAX-1 quotient
            // needs at least p-1 decimal positions, hence more than MAX bits.
            require(mc.precision >= Int.MAX_VALUE - 1)
            return "!ArithmeticException"
        }
        // Locate floor(log10(n/d)) by integer comparisons, independent of production normalization.
        var exponent = 0
        if (n >= d) {
            while (n >= d.multiply(BigInteger.TEN)) { d = d.multiply(BigInteger.TEN); exponent++ }
        } else {
            while (n < d) { n = n.multiply(BigInteger.TEN); exponent-- }
        }
        val working = preferred + mc.precision - 1L - exponent
        n = n.multiply(BigInteger.TEN.pow(mc.precision - 1))
        val qr = n.divideAndRemainder(d)
        val inexact = qr[1].signum() != 0
        if (inexact && mc.roundingMode == RoundingMode.UNNECESSARY) return "!ArithmeticException"
        val half = qr[1].multiply(BigInteger.TWO).compareTo(d)
        val increment = inexact && when (mc.roundingMode) {
            RoundingMode.UP -> true
            RoundingMode.DOWN, RoundingMode.UNNECESSARY -> false
            RoundingMode.CEILING -> sign > 0
            RoundingMode.FLOOR -> sign < 0
            RoundingMode.HALF_UP -> half >= 0
            RoundingMode.HALF_DOWN -> half > 0
            RoundingMode.HALF_EVEN -> half > 0 || half == 0 && qr[0].testBit(0)
        }
        var whole = if (increment) qr[0].add(BigInteger.ONE) else qr[0]
        var scale = working
        if (whole.toString().length > mc.precision) { whole = whole.divide(BigInteger.TEN); scale-- }
        if (!inexact) while (scale > preferred && whole.remainder(BigInteger.TEN).signum() == 0) {
            whole = whole.divide(BigInteger.TEN); scale--
        }
        return result(whole, scale, sign)
    }

    private fun result(magnitude: BigInteger, scale: Long, sign: Int): String =
        if (scale !in scales) "!ArithmeticException" else "${if (sign < 0) magnitude.negate() else magnitude}@$scale"
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalPower593Test {
    private data class Small(val coefficient: Long, val scale: Int)

    private fun ten(exponent: Int): Long {
        require(exponent in 0..12)
        var value = 1L
        repeat(exponent) { check(value <= Long.MAX_VALUE / 10); value *= 10 }
        return value
    }

    private fun digits(value: Long): Int = kotlin.math.abs(value).toString().length

    // Independent primitive rational model: it rounds N/D directly, not by
    // reproducing the production decimal cells, guard stream or zero tails.
    private fun fraction(numerator: Long, denominator: Long, sign: Int, mode: RoundingMode): Long {
        require(numerator >= 0 && denominator > 0)
        val q = numerator / denominator
        val r = numerator % denominator
        if (r == 0L) return sign * q
        check(r <= Long.MAX_VALUE / 2)
        val twice = 2 * r
        val up = when (mode) {
            RoundingMode.UP -> true
            RoundingMode.DOWN -> false
            RoundingMode.CEILING -> sign > 0
            RoundingMode.FLOOR -> sign < 0
            RoundingMode.HALF_UP -> twice >= denominator
            RoundingMode.HALF_DOWN -> twice > denominator
            RoundingMode.HALF_EVEN -> twice > denominator || twice == denominator && q % 2 != 0L
            RoundingMode.UNNECESSARY -> throw ArithmeticException("model rounding")
        }
        return sign * (q + if (up) 1 else 0)
    }

    private fun rounded(value: Small, precision: Int, mode: RoundingMode): Small {
        var drop = maxOf(digits(value.coefficient) - precision, 0)
        if (drop == 0) return value
        val sign = if (value.coefficient < 0) -1 else 1
        var q = fraction(kotlin.math.abs(value.coefficient), ten(drop), sign, mode)
        if (digits(q) > precision) { check(q % 10 == 0L); q /= 10; drop++ }
        return Small(q, value.scale - drop)
    }

    private fun product(left: Small, right: Small, precision: Int, mode: RoundingMode): Small {
        val a = kotlin.math.abs(left.coefficient)
        val b = kotlin.math.abs(right.coefficient)
        check(b == 0L || a <= Long.MAX_VALUE / b)
        val raw = left.coefficient * right.coefficient
        check(kotlin.math.abs(raw) < 1_000_000_000_000L)
        return rounded(Small(raw, left.scale + right.scale), precision, mode)
    }

    private fun inverse(value: Small, precision: Int, mode: RoundingMode): Small {
        val denominator = kotlin.math.abs(value.coefficient)
        val d = digits(denominator)
        val exponent = if (denominator == ten(d - 1)) 1 - d else -d
        var scale = precision - 1 - exponent - value.scale
        val power = precision - 1 - exponent
        check(power <= 11 && denominator < 1_000_000)
        val numerator = ten(power)
        var coefficient = fraction(numerator, denominator, if (value.coefficient < 0) -1 else 1, mode)
        if (digits(coefficient) > precision) { check(coefficient % 10 == 0L); coefficient /= 10; scale-- }
        if (numerator % denominator == 0L) {
            val preferred = -value.scale
            while (coefficient % 10 == 0L && scale > preferred) { coefficient /= 10; scale-- }
        }
        return Small(coefficient, scale)
    }

    private fun model(base: Small, exponent: Int, precision: Int, mode: RoundingMode): Small {
        val magnitude = kotlin.math.abs(exponent)
        val work = precision + 2 // All grid exponents have one decimal digit.
        var result = Small(1, 0)
        for (bit in (32 - magnitude.countLeadingZeroBits()) - 1 downTo 0) {
            result = product(result, result, work, mode)
            if (magnitude and (1 shl bit) != 0) result = product(result, base, work, mode)
        }
        if (exponent < 0) result = inverse(result, work, mode)
        return rounded(result, precision, mode)
    }

    private fun assertState(coefficient: Long, scale: Int, value: DecimalPower593.State, label: String) {
        val actual = DecimalPower593.publish(value)
        assertEquals(BigInteger.valueOf(coefficient), actual.first, label)
        assertEquals(scale, actual.second, label)
    }

    @Test fun genericRecurrenceMatchesIndependentBoundedRationals() {
        val bases = listOf(12, 19, 25, 58, 99, 125, 187, 1581)
        var cases = 0
        for (magnitude in bases) for (scale in listOf(0, 3)) for (sign in listOf(-1, 1))
            for (exponent in listOf(-3, -2, -1, 1, 2, 3)) for (precision in 1..4)
                for (mode in RoundingMode.entries) {
                    val label = "${sign * magnitude}@$scale n$exponent p$precision $mode"
                    val expected = try { model(Small(sign * magnitude.toLong(), scale), exponent, precision, mode) }
                        catch (_: ArithmeticException) { null }
                    fun actual(): DecimalPower593.State {
                        val base = DecimalPower593.fromMagnitude(sign, intArrayOf(magnitude), scale)
                        val work = precision.toLong() + 2
                        var result = DecimalPower593.accumulate(base, kotlin.math.abs(exponent), work, mode)
                        if (exponent < 0) result = DecimalPower593.reciprocal(result, work, precision, mode)
                        return DecimalPower593.round(result, precision.toLong(), mode)
                    }
                    if (expected == null) assertFailsWith<ArithmeticException>(label) { actual() }
                    else assertState(expected.coefficient, expected.scale, actual(), label)
                    cases++
                }
        assertEquals(6144, cases)
    }

    @Test fun workAndDestinationRoundsRetainOriginalBaseAndIndependentLiterals() {
        val base = DecimalPower593.fromMagnitude(1, intArrayOf(1581), 3)
        val first = DecimalPower593.accumulate(base, 2, 3, RoundingMode.HALF_UP)
        assertState(250, 2, first, "1581 square work")
        assertState(3, 0, DecimalPower593.round(first, 1, RoundingMode.HALF_UP), "two rounds")
        val original = DecimalPower593.accumulate(base, 3, 3, RoundingMode.UP)
        assertState(400, 2, original, "original1581:253*1581=399993")
        assertState(4, 0, DecimalPower593.round(original, 1, RoundingMode.UP), "globally rounded base would give5")
        assertState(1581, 3, base, "read-only original")
        val even = DecimalPower593.accumulate(DecimalPower593.fromMagnitude(1, intArrayOf(187), 2),
            2, 3, RoundingMode.HALF_EVEN)
        assertState(350, 2, even, "187 square work")
        assertState(4, 0, DecimalPower593.round(even, 1, RoundingMode.HALF_EVEN), "direct exact square would give3")
        // Three decimal cells in the later raw base product, derived without
        // overflowing Long:998*(10^18-1)=997999999999999999002.
        val wideBase = BigInteger.valueOf(999999999999999999L).decimalPowerState593(0)!!
        val wideWork = DecimalPower593.accumulate(wideBase, 3, 3, RoundingMode.DOWN)
        assertState(997, -51, wideWork, "generic D0>W multiword original")
        assertState(9, -53, DecimalPower593.round(wideWork, 1, RoundingMode.DOWN), "multiword final")
        assertState(999999999999999999L, 0, wideBase, "multiword source unchanged")
    }

    @Test fun terminatingGuardAndLastKeptDigitHaveDistinctWorkSemantics() {
        val positiveWork = listOf(313L, 312L, 313L, 312L, 313L, 312L, 312L)
        val negativeWork = listOf(313L, 312L, 312L, 313L, 313L, 312L, 312L)
        val positive32 = listOf(4L, 3L, 4L, 3L, 3L, 3L, 3L)
        val negative32 = listOf(4L, 3L, 3L, 4L, 3L, 3L, 3L)
        val positive8 = listOf(2L, 1L, 2L, 1L, 1L, 1L, 1L)
        val negative8 = listOf(2L, 1L, 1L, 2L, 1L, 1L, 1L)
        for (sign in listOf(-1, 1)) for (mode in RoundingMode.entries) {
            val source32 = DecimalPower593.fromMagnitude(sign, intArrayOf(32), 0)
            if (mode == RoundingMode.UNNECESSARY) {
                assertFailsWith<ArithmeticException> { DecimalPower593.reciprocal(source32, 3, 1, mode) }
            } else {
                val work = DecimalPower593.reciprocal(source32, 3, 1, mode)
                val expected = (if (sign > 0) positiveWork else negativeWork)[mode.ordinal]
                assertState(sign * expected, 4, work, "32 work/$sign/$mode")
                val final = (if (sign > 0) positive32 else negative32)[mode.ordinal]
                assertState(sign * final, 2, DecimalPower593.round(work, 1, mode), "32 final/$sign/$mode")
            }
            val source8 = DecimalPower593.fromMagnitude(sign, intArrayOf(8), 0)
            val work8 = DecimalPower593.reciprocal(source8, 3, 1, mode)
            assertState(sign * 125L, 3, work8, "8 exact work/$sign/$mode")
            if (mode == RoundingMode.UNNECESSARY) {
                assertFailsWith<ArithmeticException> { DecimalPower593.round(work8, 1, mode) }
            } else {
                val final = (if (sign > 0) positive8 else negative8)[mode.ordinal]
                assertState(sign * final, 1, DecimalPower593.round(work8, 1, mode), "8 final/$sign/$mode")
            }
        }
    }

    @Test fun longWorkPrecisionAndOversizedDecimalCountsStaySparseUntilExactReciprocal() {
        val exponent = 999999999
        val workPrecision = Int.MAX_VALUE.toLong() + 10
        for (sign in listOf(-1, 1)) for (mode in RoundingMode.entries) {
            val source = DecimalPower593.fromMagnitude(sign, intArrayOf(1000), 1)
            val work = DecimalPower593.accumulate(source, exponent, workPrecision, mode)
            assertEquals(workPrecision, work.digits(), "real Long work precision")
            assertEquals(147483658, work.scale, "scale before reciprocal")
            val inverse = DecimalPower593.reciprocal(work, workPrecision, Int.MAX_VALUE, mode)
            assertState(sign.toLong(), 1999999998,
                DecimalPower593.round(inverse, Int.MAX_VALUE.toLong(), mode), "private sparse reciprocal")
            val decimal = BigDecimal(BigInteger.valueOf(sign * 1000L), 1)
            val result = decimal.pow(-exponent, MathContext(Int.MAX_VALUE, mode))
            assertEquals(BigInteger.valueOf(sign.toLong()), result.unscaledValue())
            assertEquals(1999999998, result.scale())
            // The positive result's required coefficient is unsupported. This
            // checks necessary publication failure, not a successful huge power.
            assertFailsWith<ArithmeticException> { decimal.pow(exponent, MathContext(Int.MAX_VALUE, mode)) }
        }
    }

    @Test fun precisionAbovePublicDigitRangeKeepsExactFactorsAndRejectsNontermination() {
        val exact = listOf(
            Triple(2L to 0, -3, "125" to 3),
            Triple(5L to 0, -3, "8" to 3),
            Triple(40L to 0, -2, "625" to 6),
            Triple(125L to 3, -2, "64" to 0),
            Triple(-2L to 2, -3, "-125" to -3),
            // These cross the real chunk divisors2^29 and5^12.
            Triple(1073741824L to 0, -1, "931322574615478515625" to 30),
            Triple(1220703125L to 0, -1, "8192" to 13),
        )
        for ((input, exponent, expected) in exact) for (mode in RoundingMode.entries) {
            val result = BigDecimal(BigInteger.valueOf(input.first), input.second)
                .pow(exponent, MathContext(Int.MAX_VALUE, mode))
            assertEquals(expected.first, result.unscaledValue().toString(), "$input n$exponent $mode")
            assertEquals(expected.second, result.scale(), "$input n$exponent $mode")
        }
        for (coefficient in listOf(3L, 6L, 15L)) for (mode in RoundingMode.entries) {
            assertFailsWith<ArithmeticException>("nonterminating1/$coefficient at pMAX/$mode") {
                BigDecimal(coefficient).pow(-1, MathContext(Int.MAX_VALUE, mode))
            }
        }
    }
}

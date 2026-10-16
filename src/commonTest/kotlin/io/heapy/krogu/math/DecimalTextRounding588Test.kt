// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalTextRounding588Test {
    // Independent bounded rational oracle: division/remainder by a Long
    // decimal power, not lexical prefix/guard/sticky or raw decimal chunks.
    private fun expected(value: Long, scale: Int, precision: Int, mode: RoundingMode): Pair<Long, Int>? {
        if (value == 0L || precision == 0) return value to scale
        val sign = if (value < 0) -1 else 1
        val magnitude = if (value < 0) -value else value
        val digits = magnitude.toString().length
        if (digits <= precision) return value to scale
        val drop = digits - precision
        var divisor = 1L
        repeat(drop) { divisor *= 10 }
        var quotient = magnitude / divisor
        val remainder = magnitude % divisor
        if (remainder != 0L) {
            if (mode == RoundingMode.UNNECESSARY) return null
            val half = remainder.compareTo(divisor - remainder)
            val increment = when (mode) {
                RoundingMode.UP -> true
                RoundingMode.DOWN -> false
                RoundingMode.CEILING -> sign > 0
                RoundingMode.FLOOR -> sign < 0
                RoundingMode.HALF_UP -> half >= 0
                RoundingMode.HALF_DOWN -> half > 0
                RoundingMode.HALF_EVEN -> half > 0 || half == 0 && quotient % 2 != 0L
                RoundingMode.UNNECESSARY -> false
            }
            if (increment) quotient++
        }
        var resultScale = scale - drop
        if (quotient.toString().length > precision) { quotient /= 10; resultScale-- }
        return sign * quotient to resultScale
    }

    private fun fields(coefficient: String, scale: Int, actual: BigDecimal, label: String = "") {
        assertEquals(coefficient, actual.unscaledValue().toString(), "$label coefficient")
        assertEquals(scale, actual.scale(), "$label scale")
    }

    @Test fun actualPrivatePrefixAndPublicCharsAgainstBoundedLongRationals() {
        val magnitudes = listOf(0L, 1L, 124L, 125L, 126L, 134L, 135L, 136L,
            999L, 1000L, 999999999L, 9999999996L, 100000000000000001L, 999999999999999999L)
        // |value|<1e18, divisor<=1e17 and quotient+1<=1e18:
        // every oracle intermediate is strictly inside signed Long.
        for (magnitude in magnitudes) for (sign in listOf(-1, 1)) for (scale in listOf(-4, 0, 5))
            for (precision in listOf(0, 1, 2, 8, 9, 10, 18, Int.MAX_VALUE)) for (mode in RoundingMode.entries) {
                val value = sign * magnitude
                val text = "${value}e${-scale}"
                val context = MathContext(precision, mode)
                val result = expected(value, scale, precision, mode)
                val label = "$text/$precision/$mode"
                if (result == null) {
                    assertFailsWith<ArithmeticException>(label) { DecimalText588.parse(text, context) }
                    assertFailsWith<ArithmeticException>(label) { BigDecimal(text.toCharArray(), context) }
                } else {
                    fields(result.first.toString(), result.second, DecimalText588.parse(text, context), label)
                    fields(result.first.toString(), result.second, BigDecimal(text.toCharArray(), context), label)
                }
            }
        // 5376 mathematical cases, each at the actual helper and public char
        // seam. No synthetic smaller magnitude limit selects either route.
    }

    @Test fun literalChunkCarryPartialTailUnicodeAndExactCohorts() {
        data class Row(val text: String, val p: Int, val mode: RoundingMode,
            val coefficient: String, val scale: Int, val canonical: String)
        val rows = listOf(
            Row("9999999996", 9, RoundingMode.UP, "100000000", -2, "1.00000000E+10"),
            Row("-9999999996", 9, RoundingMode.FLOOR, "-100000000", -2, "-1.00000000E+10"),
            Row("9999999999999999996", 18, RoundingMode.HALF_EVEN, "100000000000000000", -2,
                "1.00000000000000000E+19"),
            Row("123456789012345678901234567890", 0, RoundingMode.UNNECESSARY,
                "123456789012345678901234567890", 0, "123456789012345678901234567890"),
            Row("100000000000000000000000000001", 28, RoundingMode.DOWN,
                "1000000000000000000000000000", -2, "1.000000000000000000000000000E+29"),
            Row("+٠٠١٢٣.٤٥e+٢", 3, RoundingMode.DOWN, "123", -2, "1.23E+4"),
            Row("0001200.00", 3, RoundingMode.UNNECESSARY, "120", -1, "1.20E+3"),
            Row("-.0000", 1, RoundingMode.UNNECESSARY, "0", 4, "0.0000"),
            Row("-0.000e+2", Int.MAX_VALUE, RoundingMode.UP, "0", 1, "0.0"))
        for (row in rows) {
            val context = MathContext(row.p, row.mode)
            for (actual in listOf(DecimalText588.parse(row.text, context),
                BigDecimal(row.text, context), BigDecimal(row.text.toCharArray(), context))) {
                fields(row.coefficient, row.scale, actual, row.text)
                assertEquals(row.canonical, actual.toString(), row.text)
            }
        }
    }

    @Test fun fullSuffixValidationAndInitialVersusRoundingScaleErrors() {
        val invalid = listOf("", "+", ".", "-.", "1..2", "1e", "1e+", "99x", "99e1x",
            "99e99999999999", "99e-2147483648", "99e2147483649", "0e-2147483648",
            "0000099999.9999x", "0000099999.9999e+", "١٢e٣x")
        for (text in invalid) for (precision in listOf(0, 1, Int.MAX_VALUE)) {
            val context = MathContext(precision, RoundingMode.DOWN)
            assertFailsWith<NumberFormatException>(text) { DecimalText588.parse(text, context) }
            assertFailsWith<NumberFormatException>(text) { BigDecimal(text.toCharArray(), context) }
        }
        for (mode in RoundingMode.entries) {
            val context = MathContext(1, mode)
            fields("1", Int.MIN_VALUE, DecimalText588.parse("1e2147483648", context))
            fields("0", Int.MIN_VALUE, BigDecimal("0e2147483648".toCharArray(), context))
            assertFailsWith<ArithmeticException> { DecimalText588.parse("99e2147483648", context) }
            assertFailsWith<ArithmeticException> { BigDecimal("99e2147483648".toCharArray(), context) }
        }
        fields("9", Int.MIN_VALUE, BigDecimal("99e2147483647".toCharArray(), MathContext(1, RoundingMode.DOWN)))
        assertFailsWith<ArithmeticException> {
            BigDecimal("99e2147483647".toCharArray(), MathContext(1, RoundingMode.UP))
        }
        for (precision in listOf(0, Int.MAX_VALUE)) {
            fields("1000", 3, BigDecimal("000001.000".toCharArray(), MathContext(precision)))
        }
    }

    @Test fun selectedSliceIsolationAndActualPrecisionRangeClassification() {
        val selected = "-00125.0e1"
        val source = "x${selected}!".toCharArray()
        val actual = BigDecimal(source, 1, selected.length, MathContext(2, RoundingMode.HALF_EVEN))
        source.fill('x')
        fields("-12", -2, actual)
        assertEquals("-1.2E+3", actual.toString())
        for ((offset, length) in listOf(-1 to 1, 0 to -1, 2 to Int.MAX_VALUE, Int.MAX_VALUE to 0)) {
            assertFailsWith<NumberFormatException> {
                BigDecimal(charArrayOf('1'), offset, length, MathContext(1))
            }
        }
        // These exercise the SAME scalar classifier called by parse; no
        // pretend lower public cap and no giant coefficient allocation.
        assertEquals(1L, retainedDecimalText588(1, Int.MAX_VALUE))
        assertEquals(1L, retainedDecimalText588(646456994, 1))
        assertEquals(646456993L, retainedDecimalText588(646456994, 646456993))
        assertEquals(646456994L, retainedDecimalText588(646456994, Int.MAX_VALUE))
        assertEquals(646456994L, retainedDecimalText588(646456994, 0))
    }
}

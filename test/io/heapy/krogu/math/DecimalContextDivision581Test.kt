// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DecimalContextDivision581Test {
    private fun power(exponent: Int): Long {
        require(exponent in 0..18)
        var result = 1L
        repeat(exponent) { result *= 10 }
        return result
    }
    private fun product(a: Long, b: Long): Long {
        require(a >= 0 && b > 0 && a <= Long.MAX_VALUE / b)
        return a * b
    }

    // Independent one-shot rational normalization with bounded Long integers.
    // No digit stream, raw trial, paged cell or production rounding is an oracle.
    private fun expected(a: Long, b: Long, sign: Int, preferred: Long, p: Int,
        mode: RoundingMode): Pair<Long, Int>? {
        var exponent = a.toString().length - b.toString().length
        val order = if (exponent >= 0) a.compareTo(product(b, power(exponent)))
            else product(a, power(-exponent)).compareTo(b)
        if (order < 0) exponent--
        val shift = p - 1 - exponent
        val n = if (shift > 0) product(a, power(shift)) else a
        val d = if (shift < 0) product(b, power(-shift)) else b
        var q = n / d
        val r = n % d
        assertTrue(q > 0)
        var scale = preferred + shift
        if (r != 0L) {
            if (mode == RoundingMode.UNNECESSARY) return null
            val half = r.compareTo(d - r)
            val up = when (mode) {
                RoundingMode.UP -> true
                RoundingMode.DOWN -> false
                RoundingMode.CEILING -> sign > 0
                RoundingMode.FLOOR -> sign < 0
                RoundingMode.HALF_UP -> half >= 0
                RoundingMode.HALF_DOWN -> half > 0
                RoundingMode.HALF_EVEN -> half > 0 || half == 0 && q % 2 != 0L
                RoundingMode.UNNECESSARY -> false
            }
            if (up) q++
            if (q.toString().length > p) { assertEquals(0L, q % 10); q /= 10; scale-- }
        } else {
            while (scale > preferred && q % 10 == 0L) { q /= 10; scale-- }
        }
        if (scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) return null
        return sign * q to scale.toInt()
    }

    private fun words(value: Long): IntArray {
        var remaining = value
        val result = mutableListOf<Int>()
        while (remaining > 0) { result += (remaining and 0x3fffffff).toInt(); remaining = remaining ushr 30 }
        return result.toIntArray()
    }
    private fun raw(a: Long, b: Long, sign: Int, preferred: Long, p: Int, mode: RoundingMode) {
        val left = words(a)
        val right = words(b)
        val leftCopy = left.copyOf()
        val rightCopy = right.copyOf()
        val answer = expected(a, b, sign, preferred, p, mode)
        val label = "$a/$b sign=$sign preferred=$preferred p=$p $mode"
        if (answer == null) assertFailsWith<ArithmeticException>(label) {
            DecimalDivision581.rounded(sign, left, right, preferred, MathContext(p, mode))
        } else {
            val actual = DecimalDivision581.rounded(sign, left, right, preferred, MathContext(p, mode))
            assertEquals(answer.first.toString(), actual.first.toString(), "$label coefficient")
            assertEquals(answer.second, actual.second, "$label scale")
        }
        assertContentEquals(leftCopy, left, "$label left isolation")
        assertContentEquals(rightCopy, right, "$label right isolation")
    }

    @Test fun directStreamAgainstIndependentRationalModel() {
        val pairs = listOf(1L to 2L, 1L to 3L, 1L to 8L,
            125L to 100L, 135L to 100L, 1249L to 1000L, 1250L to 1000L, 1251L to 1000L,
            1250L to 1L, 1000000000001L to 1L, 1200L to 3L,
            2000000000L to 1000000001L, 3999999999L to 1999999999L,
            12345678901234567L to 999999937L, 999999999999999999L to 999999999L,
            999999999999999999L to 1L)
        // All model powers<=10^18 and scaled products<4e18<Long.MAX_VALUE.
        // 2000000000/1000000001 subtracts trial1000000001 from window2000000000:
        // its decimal low-cell 0-1 borrows. 3999999999/1999999999 has a two-cell
        // denominator and trial carry. Inputs/outputs also cross binary2^30.
        // Denominator1 with unread nonzero tails catches premature R==0 stops.
        for ((a, b) in pairs) for (sign in listOf(-1, 1))
            for (p in listOf(1, 2, 7, 9, 10)) for (mode in RoundingMode.entries)
                raw(a, b, sign, 0L, p, mode)
    }

    @Test fun directStreamScaleCarryAndRequiredExactCohorts() {
        for ((a, b) in listOf(19L to 20L, 1999L to 2000L, 1L to 2L,
            10L to 1L, 1000L to 1L, 1L to 3L))
            for (scale in listOf(Int.MIN_VALUE.toLong() - 1, Int.MIN_VALUE.toLong(),
                Int.MIN_VALUE.toLong() + 1, Int.MAX_VALUE.toLong() - 1,
                Int.MAX_VALUE.toLong(), Int.MAX_VALUE.toLong() + 1))
                for (p in listOf(1, 2, 7)) for (sign in listOf(-1, 1))
                    for (mode in RoundingMode.entries) raw(a, b, sign, scale, p, mode)
        // Exact preferred cohorts retain zeros; inexact carry has only its one drop.
        assertEquals(BigInteger("1250") to 0,
            DecimalDivision581.rounded(1, words(1250), words(1), 0L, MathContext(7)))
        assertEquals(BigInteger("10") to Int.MAX_VALUE,
            DecimalDivision581.rounded(1, words(1999), words(2000), Int.MAX_VALUE.toLong() - 1,
                MathContext(2, RoundingMode.HALF_UP)))
        assertFailsWith<ArithmeticException> {
            DecimalDivision581.rounded(1, words(1), words(1), Int.MIN_VALUE.toLong() - 1, MathContext(2))
        }
        assertFailsWith<ArithmeticException> {
            DecimalDivision581.rounded(1, words(10), words(1), Int.MAX_VALUE.toLong() + 1, MathContext(2))
        }
    }

    @Test fun sufficientFastPathBoundsAreSymbolicAndConservative() {
        val limit = Int.MAX_VALUE
        assertTrue(materializedDecimalDivision581(1, 2, 10, 0, 10, 10))
        assertFalse(materializedDecimalDivision581(limit, 4, 646456993, 0, 1, 1))
        assertFalse(materializedDecimalDivision581(limit, limit, 1, 0, 0, 0))
        assertFalse(materializedDecimalDivision581(1, 2, limit, 0, limit.toLong(), limit.toLong()))
        assertFalse(materializedDecimalDivision581(1, 2, 1, 0, 0, Long.MIN_VALUE))
        assertFalse(materializedDecimalDivision581(1, 2, 1, Int.MIN_VALUE.toLong() - 1, 0, 0))
        assertFalse(materializedDecimalDivision581(1, 2, 1, 0, Int.MAX_VALUE.toLong() + 1, 0))
        assertFalse(materializedDecimalDivision581(1, 2, 1, Int.MIN_VALUE.toLong(), Int.MIN_VALUE.toLong(), 0))
        // Bit sums exactly at L and the first nonproved neighbor, for both shifts.
        assertTrue(materializedDecimalDivision581(limit - 4, 1, 1, 0, 1, 1))
        assertFalse(materializedDecimalDivision581(limit - 3, 1, 1, 0, 1, 1))
        assertTrue(materializedDecimalDivision581(1, limit - 4, 1, 0, 0, -1))
        assertFalse(materializedDecimalDivision581(1, limit - 3, 1, 0, 0, -1))
    }

    @Test fun publicRawBridgeAndLargePrecisionExactEscape() {
        class Truthful(c: Long, s: Int) : BigDecimal(BigInteger.valueOf(c), s) {
            override fun signum(): Int = super.signum()
            override fun precision(): Int = super.precision()
            override fun divide(divisor: BigDecimal, mc: MathContext): BigDecimal = super.divide(divisor, mc)
            fun inherited(divisor: BigDecimal, mc: MathContext): BigDecimal = super.divide(divisor, mc)
        }
        for (mode in RoundingMode.entries) {
            for ((a, b, coefficient, scale) in listOf(
                listOf(1L, 2L, 5L, 1L), listOf(6L, 15L, 4L, 1L),
                listOf(1250L, 1L, 1250L, 0L), listOf(1000000000001L, 1L, 1000000000001L, 0L))) {
                val actual = BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b), MathContext(Int.MAX_VALUE, mode))
                assertEquals(coefficient.toString(), actual.unscaledValue().toString())
                assertEquals(scale.toInt(), actual.scale())
            }
            // A nonterminating p>H result cannot have a supported required coefficient.
            for (p in listOf(646456994, Int.MAX_VALUE)) assertFailsWith<ArithmeticException> {
                BigDecimal.ONE.divide(BigDecimal.valueOf(3), MathContext(p, mode))
            }
            for (sign in listOf(-1, 1)) {
                val x = Truthful(sign.toLong(), Int.MIN_VALUE)
                val y = Truthful(2, 1)
                val expected = BigDecimal.valueOf(5L * sign, Int.MIN_VALUE)
                assertEquals(expected, x.divide(y, MathContext(1, mode)))
                assertEquals(expected, x.inherited(y, MathContext(1, mode)))
            }
        }
    }

    @Test fun publicZeroP0AndExplicitCallerContractsRemain() {
        assertEquals(BigDecimal("0.5"), BigDecimal.ONE.divide(BigDecimal.valueOf(2), MathContext.UNLIMITED))
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divide(BigDecimal.valueOf(3), MathContext.UNLIMITED) }
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divide(BigDecimal.ZERO, MathContext(1)) }
        assertEquals(Int.MIN_VALUE, BigDecimal.valueOf(0, Int.MIN_VALUE)
            .divide(BigDecimal.valueOf(1, 1), MathContext(1)).scale())
        val marker = BigDecimal("7.00")
        val failure = IllegalStateException("caller-owned581")
        val returned: BigDecimal = object : BigDecimal("1") {
            override fun divide(divisor: BigDecimal, mc: MathContext): BigDecimal = marker
        }
        val thrown: BigDecimal = object : BigDecimal("1") {
            override fun divide(divisor: BigDecimal, mc: MathContext): BigDecimal = throw failure
        }
        assertSame(marker, returned.divide(BigDecimal.ONE, MathContext(Int.MAX_VALUE)))
        assertSame(failure, assertFailsWith<IllegalStateException> {
            thrown.divide(BigDecimal.ONE, MathContext(Int.MAX_VALUE))
        })
    }
}

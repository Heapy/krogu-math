// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class DecimalExactRoot563Test {
    private class Truthful(c: BigInteger, s: Int) : BigDecimal(c, s) {
        override fun signum() = super.signum()
        override fun precision() = super.precision()
        override fun scale() = super.scale()
        override fun unscaledValue() = super.unscaledValue()
        override fun stripTrailingZeros() = super.stripTrailingZeros()
        override fun sqrt(mc: MathContext?) = super.sqrt(mc)
        fun inheritedRoot(mc: MathContext?) = super.sqrt(mc)
    }

    private fun assertResult(expected: DecimalExactRoot563Expected.Result, actual: BigDecimal, label: String) {
        assertEquals(expected.coefficient, actual.unscaledValue().toString(), "$label coefficient")
        assertEquals(expected.scale, actual.scale(), "$label scale")
        assertEquals(expected.text(), actual.toString(), "$label text")
    }

    @Test fun hugePrecisionAndExtremeCohorts() {
        // Each minimal root is an independent literal, never obtained through a sqrt call.
        data class Case(val c: String, val s: Int, val r: String, val t: Int)
        val cases = listOf(
            Case("4", 0, "2", 0), Case("9", 0, "3", 0), Case("144", 0, "12", 0),
            Case("400", 2, "2", 0), Case("4000", 1, "2", -1),
            Case("250", 3, "5", 1), Case("250", 1, "5", 0), Case("250", -1, "5", -1),
            Case("100", Int.MIN_VALUE, "1", -1073741825),
            Case("400", Int.MIN_VALUE, "2", -1073741825),
            Case("1000", Int.MIN_VALUE + 1, "1", -1073741825),
            Case("40", Int.MIN_VALUE + 1, "2", -1073741824),
            Case("1", Int.MIN_VALUE, "1", -1073741824),
            Case("4", Int.MIN_VALUE + 2, "2", -1073741823),
            Case("1", Int.MAX_VALUE - 1, "1", 1073741823),
            Case("250", Int.MAX_VALUE, "5", 1073741823),
            Case("4000", Int.MAX_VALUE, "2", 1073741822),
            Case("15241578750190521", 0, "123456789", 0),
        )
        for (case in cases) {
            assertEquals(case.r to case.t, DecimalExactRoot563Expected.root(case.c, case.s), "independent digit-pair model $case")
            for (p in listOf(0, 1, 2, 7, 17, Int.MAX_VALUE - 1, Int.MAX_VALUE)) for (mode in RoundingMode.entries) {
                val mc = MathContext(p, mode)
                val ordinary = BigDecimal(BigInteger(case.c), case.s)
                val truthful = Truthful(BigInteger(case.c), case.s)
                val label = "$case / $mc"
                if (p > 0 && p < case.r.length && mode == RoundingMode.UNNECESSARY) {
                    assertFailsWith<ArithmeticException>(label) { ordinary.sqrt(mc) }
                    assertFailsWith<ArithmeticException>(label) { truthful.inheritedRoot(mc) }
                } else {
                    val expected = DecimalExactRoot563Expected.rounded(case.r, case.t, case.s / 2, mc)
                    assertResult(expected, ordinary.sqrt(mc), label)
                    assertResult(expected, truthful.sqrt(mc), "$label truthful")
                    assertResult(expected, truthful.inheritedRoot(mc), "$label caller super")
                }
            }
        }
    }

    @Test fun rootFamiliesAndTrailingZeroCancellation() {
        val roots = listOf("1", "2", "3", "5", "15", "25", "125", "995", "999", "1001",
            "123456789", "10000000000000000001")
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -3, -2, -1, 0, 1, 2, 3,
            Int.MAX_VALUE - 1, Int.MAX_VALUE)
        var checks = 0
        for (r in roots) for (zeros in 0..5) for (s in scales) {
            val normalized = s.toLong() - zeros
            if (normalized % 2L != 0L) continue
            val c = BigInteger(r).multiply(BigInteger(r)).toString() + "0".repeat(zeros)
            val t = (normalized / 2).toInt()
            assertEquals(r to t, DecimalExactRoot563Expected.root(c, s))
            for (p in listOf(0, 1, 2, r.length, r.length + 2, Int.MAX_VALUE)) for (mode in RoundingMode.entries) {
                val mc = MathContext(p, mode)
                val value = BigDecimal(BigInteger(c), s)
                val label = "$c@$s / $mc"
                if (p > 0 && p < r.length && mode == RoundingMode.UNNECESSARY)
                    assertFailsWith<ArithmeticException>(label) { value.sqrt(mc) }
                else assertResult(DecimalExactRoot563Expected.rounded(r, t, s / 2, mc), value.sqrt(mc), label)
                checks++
            }
        }
        assertEquals(19008, checks)
    }

    @Test fun independentMidpointAndCarryLiterals() {
        val modes = RoundingMode.entries.filter { it != RoundingMode.UNNECESSARY }
        // Mode order: UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN.
        val expected15 = listOf("2", "1", "2", "1", "2", "1", "2")
        val expected25 = listOf("3", "2", "3", "2", "3", "2", "2")
        for ((i, mode) in modes.withIndex()) {
            assertResult(DecimalExactRoot563Expected.Result(expected15[i], -1), BigDecimal("225").sqrt(MathContext(1, mode)), "sqrt225 $mode")
            assertResult(DecimalExactRoot563Expected.Result(expected25[i], -1), BigDecimal("625").sqrt(MathContext(1, mode)), "sqrt625 $mode")
        }
        for (mode in listOf(RoundingMode.UP, RoundingMode.CEILING, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN))
            assertResult(DecimalExactRoot563Expected.Result("10", -2), BigDecimal("998001").sqrt(MathContext(2, mode)), "999 carry $mode")
        assertResult(DecimalExactRoot563Expected.Result("99", -1), BigDecimal("998001").sqrt(MathContext(2, RoundingMode.DOWN)), "999 no carry")
    }

    @Test fun genuineErrorsAndPublicEntryRemain() {
        for (mode in RoundingMode.entries) {
            assertFailsWith<ArithmeticException> { BigDecimal("-4").sqrt(MathContext(Int.MAX_VALUE, mode)) }
            for (value in listOf(BigDecimal("2"), BigDecimal(BigInteger("4"), 1), BigDecimal(BigInteger("10"), Int.MIN_VALUE))) {
                assertEquals(null, DecimalExactRoot563Expected.root(value.unscaledValue().toString(), value.scale()))
                assertFailsWith<ArithmeticException> { value.sqrt(MathContext(0, mode)) }
                assertFailsWith<ArithmeticException> { value.sqrt(MathContext(17, RoundingMode.UNNECESSARY)) }
            }
            assertFailsWith<ArithmeticException> { BigDecimal("225").sqrt(MathContext(1, RoundingMode.UNNECESSARY)) }
        }
        assertFailsWith<NullPointerException> { BigDecimal("4").sqrt(null) }
        assertFailsWith<ArithmeticException> { BigDecimal("-4").sqrt(null) }
        assertFailsWith<NullPointerException> { BigDecimal(BigInteger.ZERO, Int.MIN_VALUE).sqrt(null) }
        assertEquals(-1073741824, BigDecimal(BigInteger.ZERO, Int.MIN_VALUE).sqrt(MathContext.UNLIMITED).scale())
        val marker = BigDecimal("7.00")
        val failure = IllegalStateException("caller owned")
        val context = MathContext(Int.MAX_VALUE, RoundingMode.HALF_EVEN)
        var entries = 0
        val replacement: BigDecimal = object : BigDecimal("4") {
            override fun sqrt(mc: MathContext?): BigDecimal {
                entries++
                assertSame(context, mc)
                return marker
            }
        }
        assertSame(marker, replacement.sqrt(context))
        assertEquals(1, entries)
        val throwing: BigDecimal = object : BigDecimal("4") {
            override fun sqrt(mc: MathContext?): BigDecimal { assertSame(context, mc); throw failure }
        }
        assertSame(failure, assertFailsWith<IllegalStateException> { throwing.sqrt(context) })
    }
}

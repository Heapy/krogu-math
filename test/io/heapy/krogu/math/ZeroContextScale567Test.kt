// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/** Task 567 source milestone: PREPARED / UNCOMPILED / UNRUN. */
class ZeroContextScale567Test {
    private val scales = listOf(Int.MIN_VALUE, -7, -1, 0, 1, 7, Int.MAX_VALUE)
    private val precisions = listOf(0, 1, 7, Int.MAX_VALUE)

    private class Truthful(c: Long, s: Int) : BigDecimal(BigInteger.valueOf(c), s) {
        override fun unscaledValue() = super.unscaledValue()
        override fun scale() = super.scale()
        override fun precision() = super.precision()
        override fun signum() = super.signum()
        override fun plus() = super.plus()
        override fun plus(mc: MathContext) = super.plus(mc)
        override fun round(mc: MathContext) = super.round(mc)
        fun plusBase(mc: MathContext) = super.plus(mc)
        fun roundBase(mc: MathContext) = super.round(mc)
    }

    private fun input(c: Long, s: Int, truthful: Boolean): BigDecimal =
        if (truthful) Truthful(c, s) else BigDecimal(BigInteger.valueOf(c), s)

    private fun assertFields(c: String, s: Int, value: BigDecimal) {
        assertEquals(c, value.unscaledValue().toString())
        assertEquals(s, value.scale())
    }

    private fun assertCanonicalZero(value: BigDecimal) {
        assertFields("0", 0, value)
        assertEquals("0", value.toString())
        // The contract specifies representation, not cached-result identity.
    }

    @Test fun directZeroHasLiteralScaleZeroAcrossEntireMetadataRange() {
        for (s in scales) for (p in precisions) for (mode in RoundingMode.entries)
            for (truthful in listOf(false, true)) {
                val x = input(0, s, truthful)
                val originalText = x.toString()
                val mc = MathContext(p, mode)
                assertCanonicalZero(x.plus(mc))
                assertCanonicalZero(x.round(mc))
                if (x is Truthful) {
                    assertCanonicalZero(x.plusBase(mc))
                    assertCanonicalZero(x.roundBase(mc))
                }
                assertFields("0", s, x)
                assertEquals(originalText, x.toString())
            }
    }

    @Test fun noContextPlusRetainsIdentity() {
        for (s in scales) for (c in listOf(0L, -7L, 7L)) for (truthful in listOf(false, true)) {
            val x = input(c, s, truthful)
            assertSame(x, x.plus())
            assertFields(c.toString(), s, x)
            // 126 null calls (including real super calls) moved to UnaryNull576StandaloneTest.
        }
    }

    @Test fun nonzeroExactRoundingKeepsRepresentableInputCohort() {
        for (s in scales) for (c in listOf(-7L, 7L)) for (p in precisions)
            for (mode in RoundingMode.entries) for (truthful in listOf(false, true)) {
                val x = input(c, s, truthful)
                val mc = MathContext(p, mode)
                assertFields(c.toString(), s, x.plus(mc))
                assertFields(c.toString(), s, x.round(mc))
                if (x is Truthful) {
                    assertFields(c.toString(), s, x.plusBase(mc))
                    assertFields(c.toString(), s, x.roundBase(mc))
                }
            }
        // Dropping exact trailing zeros changes precision and scale together.
        for (mode in RoundingMode.entries) for (truthful in listOf(false, true)) {
            val x = input(1200, 3, truthful)
            assertFields("12", 1, x.plus(MathContext(2, mode)))
            assertFields("12", 1, x.round(MathContext(2, mode)))
            assertFields("1200", 3, x.plus(MathContext(0, mode)))
        }
    }

    @Test fun nonzeroMidpointAndCarryUseLiteralModeSpecificExpectations() {
        // Fixed public rounding examples; no host oracle or production rounding helper.
        // Columns: UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN, UNNECESSARY.
        val modes = listOf(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING,
            RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN,
            RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)
        data class Row(val c: Long, val s: Int, val p: Int, val expected: List<String>)
        val rows = listOf(
            Row(125, 2, 2, listOf("13@1", "12@1", "13@1", "12@1", "13@1", "12@1", "12@1", "!")),
            Row(-125, 2, 2, listOf("-13@1", "-12@1", "-12@1", "-13@1", "-13@1", "-12@1", "-12@1", "!")),
            Row(135, 2, 2, listOf("14@1", "13@1", "14@1", "13@1", "14@1", "13@1", "14@1", "!")),
            Row(999, 2, 2, listOf("10@0", "99@1", "10@0", "99@1", "10@0", "10@0", "10@0", "!")),
            Row(-999, 2, 2, listOf("-10@0", "-99@1", "-99@1", "-10@0", "-10@0", "-10@0", "-10@0", "!")),
        )
        for (row in rows) for ((index, mode) in modes.withIndex()) for (truthful in listOf(false, true)) {
            val x = input(row.c, row.s, truthful)
            val mc = MathContext(row.p, mode)
            val calls = mutableListOf<() -> BigDecimal>({ x.plus(mc) }, { x.round(mc) })
            if (x is Truthful) {
                calls += { x.plusBase(mc) }
                calls += { x.roundBase(mc) }
            }
            for (call in calls) {
                val expected = row.expected[index]
                if (expected == "!") assertFailsWith<ArithmeticException>(block = call)
                else {
                    val fields = expected.split('@')
                    assertFields(fields[0], fields[1].toInt(), call())
                }
            }
            assertFields(row.c.toString(), row.s, x)
        }
    }

    @Test fun explicitOverridesKeepResultAndExceptionIdentity() {
        val replacement = BigDecimal("0.000")
        val failure = IllegalStateException("caller sentinel")
        class PlusCaller(private val fail: Boolean) : BigDecimal("0.00") {
            override fun plus(mc: MathContext): BigDecimal {
                if (fail) throw failure
                return replacement
            }
            fun plusBase(mc: MathContext) = super.plus(mc)
            fun roundBase(mc: MathContext) = super.round(mc)
        }
        class RoundCaller(private val fail: Boolean) : BigDecimal("0.00") {
            override fun round(mc: MathContext): BigDecimal {
                if (fail) throw failure
                return replacement
            }
            fun roundBase(mc: MathContext) = super.round(mc)
        }
        for (mc in listOf(MathContext(0), MathContext(1))) for (fail in listOf(false, true)) {
            val plus = PlusCaller(fail)
            val round = RoundCaller(fail)
            // Inherited round and caller-owned super.round still dispatch virtual plus.
            for (call in listOf<() -> BigDecimal>({ plus.plus(mc) }, { plus.round(mc) },
                { plus.roundBase(mc) }, { round.round(mc) })) {
                if (fail) assertSame(failure, assertFailsWith<IllegalStateException>(block = call))
                else assertSame(replacement, call())
            }
            // Explicit super.plus bypasses the plus override; super.round bypasses round only.
            assertCanonicalZero(plus.plusBase(mc))
            assertCanonicalZero(round.roundBase(mc))
            assertCanonicalZero(round.plus(mc))
        }
    }

    @Test fun contextualConstructorsRetainTheirOwnZeroScales() {
        for (p in precisions) for (mode in RoundingMode.entries) {
            val mc = MathContext(p, mode)
            for (s in scales) assertFields("0", s, BigDecimal(BigInteger.ZERO, s, mc))
            for ((text, s) in listOf("0E+7" to -7, "0E+1" to -1, "0" to 0,
                "0.0" to 1, "0.0000000" to 7)) {
                assertFields("0", s, BigDecimal(text, mc))
                assertFields("0", s, BigDecimal(text.toCharArray(), mc))
                val wrapped = "[$text]".toCharArray()
                assertFields("0", s, BigDecimal(wrapped, 1, text.length, mc))
            }
            assertFields("0", 0, BigDecimal(BigInteger.ZERO, mc))
            assertFields("0", 0, BigDecimal(0, mc))
            assertFields("0", 0, BigDecimal(0L, mc))
            assertFields("0", 0, BigDecimal(0.0, mc))
        }
        // These two constructor null entries are retained in Validation578StandaloneTest (JVM).
    }

    @Test fun cancellationAndZeroAdditionRetainPreferredScale() {
        for (p in precisions) for (mode in RoundingMode.entries) for (truthful in listOf(false, true)) {
            val mc = MathContext(p, mode)
            for (s in scales) {
                val x = input(1, s, truthful)
                val negative = input(-1, s, truthful)
                assertFields("0", s, x.add(negative, mc))
                assertFields("0", s, x.subtract(x, mc))
            }
            // Differing scales: max(2,3)=3; 1.00 + (-1.000) cancels exactly.
            assertFields("0", 3, input(100, 2, truthful).add(input(-1000, 3, truthful), mc))
            assertFields("0", 3, input(100, 2, truthful).subtract(input(1000, 3, truthful), mc))
            // Zero alignment never needs a power sized by the scale gap.
            for (a in scales) for (b in scales) {
                val x = input(0, a, truthful)
                val y = input(0, b, truthful)
                assertFields("0", maxOf(a, b), x.add(y, mc))
                assertFields("0", maxOf(a, b), x.subtract(y, mc))
            }
            assertFields("10000000", 7, input(0, 7, truthful).add(input(1, 0, truthful), MathContext(0, mode)))
            assertFields("1", 0, input(0, 7, truthful).add(input(1, 0, truthful), MathContext(1, mode)))
        }
    }

    @Test fun zeroProductsQuotientsAndPowersRetainOperationScales() {
        for (p in precisions) for (mode in RoundingMode.entries) for (truthful in listOf(false, true)) {
            val mc = MathContext(p, mode)
            for (s in scales) {
                val zero = input(0, s, truthful)
                val one = input(1, 0, truthful)
                assertFields("0", s, zero.multiply(one, mc))
                assertFields("0", s, one.multiply(zero, mc))
                assertFields("0", s, zero.divide(one, mc))
                for (requested in scales) assertFields("0", requested, zero.divide(one, requested, mode))
            }
            // Product uses 3+(-1)=2; quotient uses 3-(-1)=4.
            val zero = input(0, 3, truthful)
            val two = input(2, -1, truthful)
            assertFields("0", 2, zero.multiply(two, mc))
            assertFields("0", 2, two.multiply(zero, mc))
            assertFields("0", 4, zero.divide(two, mc))
            assertFields("0", 6, zero.pow(2, mc))
        }
    }

    @Test fun unaryContractsAndExplicitDelegationRemainIntact() {
        for (s in scales) for (truthful in listOf(false, true)) {
            val zero = input(0, s, truthful)
            // Contextual scale retention is the task567 compatibility choice.
            assertFields("0", s, zero.abs())
            assertFields("0", s, zero.negate())
            for (p in precisions) for (mode in RoundingMode.entries) {
                val mc = MathContext(p, mode)
                assertFields("0", s, zero.abs(mc))
                assertFields("0", s, zero.negate(mc))
            }
            // 28 zero null calls retained by UnaryNull576StandaloneTest.zeroUnary.
        }
        val replacement = BigDecimal("0.000")
        val failure = IllegalStateException("unary caller sentinel")
        for (fail in listOf(false, true)) {
            val returned = object : BigDecimal("12.34") {
                override fun plus(mc: MathContext): BigDecimal {
                    if (fail) throw failure
                    return replacement
                }
            }
            val source = object : BigDecimal("0.00") {
                override fun negate() = returned
            }
            for (mc in listOf(MathContext(0), MathContext(1)))
                for (call in listOf<() -> BigDecimal>({ returned.abs(mc) }, { source.negate(mc) })) {
                    if (fail) assertSame(failure, assertFailsWith<IllegalStateException>(block = call))
                    else assertSame(replacement, call())
                }
        }
    }

    @Test fun unaryZeroSeamUsesStoredCoefficientAndKeepsExplicitCalls() {
        val replacement = BigDecimal("0.000")
        val failure = IllegalStateException("explicit zero plus")
        for (s in scales) for (fail in listOf(false, true)) {
            val trace = StringBuilder()
            val returned = object : BigDecimal(BigInteger.ZERO, s) {
                override fun signum(): Int { trace.append("sign;"); return 1 }
                override fun plus(mc: MathContext): BigDecimal {
                    trace.append("plus;")
                    if (fail) throw failure
                    return replacement
                }
                fun plusBase(mc: MathContext) = super.plus(mc)
                fun roundBase(mc: MathContext) = super.round(mc)
            }
            val source = object : BigDecimal("-1") {
                override fun negate(): BigDecimal { trace.append("negate;"); return returned }
            }
            for (p in listOf(0, 1, 7)) for (mode in RoundingMode.entries) {
                trace.clear()
                assertFields("0", s, source.negate(MathContext(p, mode)))
                assertEquals("negate;", trace.toString())
                trace.clear()
                assertFields("0", s, source.abs(MathContext(p, mode)))
                assertEquals("negate;", trace.toString())
            }
            // Null seam and explicit Java overrides retained by UnaryNull576StandaloneTest.zeroSeam.
            // Explicit language calls still honor the override, even on stored zero.
            for (mc in listOf(MathContext(0), MathContext(1))) {
                for (call in listOf<() -> BigDecimal>({ returned.plus(mc) },
                    { returned.round(mc) }, { returned.roundBase(mc) })) {
                    if (fail) assertSame(failure, assertFailsWith<IllegalStateException>(block = call))
                    else assertSame(replacement, call())
                }
                assertCanonicalZero(returned.plusBase(mc))
            }
        }
    }

    @Test fun unaryOrderingAndExplicitAbsNegateOverridesKeepValidSemantics() {
        val trace = StringBuilder()
        val replacement = BigDecimal("0.000")
        val failure = IllegalStateException("unary dispatch")
        val context = MathContext(1)
        for (sign in listOf(-1, 0, 1)) for (fail in listOf(false, true)) {
            val value = object : BigDecimal("0.00") {
                override fun signum(): Int { trace.append("sign;"); return sign }
                override fun negate(mc: MathContext): BigDecimal {
                    trace.append("negateContext;")
                    if (fail) throw failure
                    return replacement
                }
            }
            trace.clear()
            if (sign < 0) {
                if (fail) assertSame(failure, assertFailsWith<IllegalStateException> { value.abs(context) })
                else assertSame(replacement, value.abs(context))
                assertEquals("sign;negateContext;", trace.toString())
            } else {
                assertFields("0", 2, value.abs(context))
                assertEquals("sign;", trace.toString())
            }
            val explicit = object : BigDecimal("0.00") {
                override fun abs(mc: MathContext): BigDecimal {
                    if (fail) throw failure
                    return replacement
                }
                override fun negate(mc: MathContext): BigDecimal {
                    if (fail) throw failure
                    return replacement
                }
                fun absBase(mc: MathContext) = super.abs(mc)
                fun negateBase(mc: MathContext) = super.negate(mc)
            }
            for (call in listOf<() -> BigDecimal>({ explicit.abs(context) }, { explicit.negate(context) })) {
                if (fail) assertSame(failure, assertFailsWith<IllegalStateException>(block = call))
                else assertSame(replacement, call())
            }
            // All seven null routes per sign/fail are retained in the JVM ordering test.
            assertFields("0", 2, explicit.absBase(MathContext(1)))
            assertFields("0", 2, explicit.negateBase(MathContext(1)))
            val throwing = object : BigDecimal("0.00") {
                override fun signum(): Int = throw failure
                override fun negate(): BigDecimal = throw failure
            }
            assertSame(failure, assertFailsWith<IllegalStateException> { throwing.abs(context) })
            assertSame(failure, assertFailsWith<IllegalStateException> { throwing.negate(context) })
        }
    }

    @Test fun floatingContextZeroRetainsCanonicalFactoryCohort() {
        for (p in precisions) for (mode in RoundingMode.entries) {
            val mc = MathContext(p, mode)
            for (value in listOf(0.0, -0.0)) {
                assertFields("0", 1, value.toBigDecimal(mc))
                assertFields("0", 1, value.toBigDecimal())
            }
            for (value in listOf(0.0f, -0.0f)) assertFields("0", 1, value.toBigDecimal(mc))
            assertFields("1", 1, 0.1.toBigDecimal(mc))
            if (p != 1) assertFields("125", 2, 1.25.toBigDecimal(mc))
            for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY))
                assertFailsWith<NumberFormatException> { value.toBigDecimal(mc) }
        }
    }
}

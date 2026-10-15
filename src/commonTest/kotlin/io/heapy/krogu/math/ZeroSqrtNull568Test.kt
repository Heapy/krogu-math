// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ZeroSqrtNull568Test {
    private class Truthful(c: BigInteger, s: Int) : BigDecimal(c, s) {
        override fun signum() = super.signum()
        override fun precision() = super.precision()
        override fun scale() = super.scale()
        override fun unscaledValue() = super.unscaledValue()
        override fun stripTrailingZeros() = super.stripTrailingZeros()
        override fun sqrt(mc: MathContext?) = super.sqrt(mc)
        fun inheritedRoot(mc: MathContext?) = super.sqrt(mc)
    }

    // Literal preferred scales, including negative odd scales (division toward zero).
    private val zeroScales = listOf(
        Int.MIN_VALUE to -1073741824, (Int.MIN_VALUE + 1) to -1073741823,
        -3 to -1, -2 to -1, -1 to 0, 0 to 0, 1 to 0, 2 to 1, 3 to 1,
        (Int.MAX_VALUE - 1) to 1073741823, Int.MAX_VALUE to 1073741823,
    )

    @Test fun ordinaryTruthfulAndCallerSuperZerosRequireContext() {
        for (text in listOf("0", "0.00"))
            assertFailsWith<NullPointerException>(text) { BigDecimal(text).sqrt(null) }
        for ((scale, _) in zeroScales) {
            val ordinary = BigDecimal(BigInteger.ZERO, scale)
            val truthful = Truthful(BigInteger.ZERO, scale)
            assertFailsWith<NullPointerException>("ordinary zero@$scale") { ordinary.sqrt(null) }
            assertFailsWith<NullPointerException>("truthful zero@$scale") { truthful.sqrt(null) }
            assertFailsWith<NullPointerException>("caller super zero@$scale") { truthful.inheritedRoot(null) }
        }
    }

    @Test fun validContextsKeepZeroAndPreferredScale() {
        for ((scale, preferred) in zeroScales) {
            val ordinary = BigDecimal(BigInteger.ZERO, scale)
            val truthful = Truthful(BigInteger.ZERO, scale)
            for (precision in listOf(0, 1, 7, Int.MAX_VALUE)) for (mode in RoundingMode.entries) {
                val context = MathContext(precision, mode)
                val label = "zero@$scale / $context"
                for (result in listOf(ordinary.sqrt(context), truthful.sqrt(context), truthful.inheritedRoot(context))) {
                    assertEquals(BigInteger.ZERO, result.unscaledValue(), "$label coefficient")
                    assertEquals(preferred, result.scale(), "$label preferred scale")
                }
            }
        }
    }

    @Test fun nonzeroAndNegativeEntryControlsRemain() {
        for (text in listOf("4", "2")) {
            val truthful = Truthful(BigInteger(text), 0)
            assertFailsWith<NullPointerException>(text) { BigDecimal(text).sqrt(null) }
            assertFailsWith<NullPointerException>("truthful $text") { truthful.sqrt(null) }
            assertFailsWith<NullPointerException>("caller super $text") { truthful.inheritedRoot(null) }
        }
        // Retained competing-invalid observation; this does not impose a global error order.
        val negative = Truthful(BigInteger("-4"), 0)
        assertFailsWith<ArithmeticException> { BigDecimal("-4").sqrt(null) }
        assertFailsWith<ArithmeticException> { negative.sqrt(null) }
        assertFailsWith<ArithmeticException> { negative.inheritedRoot(null) }
        for (mode in RoundingMode.entries) {
            val context = MathContext(7, mode)
            val result = BigDecimal("4.00").sqrt(context)
            assertEquals(BigInteger("20"), result.unscaledValue())
            assertEquals(1, result.scale())
            assertFailsWith<ArithmeticException> { BigDecimal("-4").sqrt(context) }
        }
    }

    @Test fun explicitOverridesKeepResultAndExceptionIdentityIncludingNullContext() {
        val marker = BigDecimal("7.00")
        val failure = IllegalStateException("caller owned")
        for (context in listOf(null, MathContext.UNLIMITED, MathContext(7, RoundingMode.HALF_EVEN))) {
            val replacement: BigDecimal = object : BigDecimal("0.00") {
                override fun sqrt(mc: MathContext?): BigDecimal {
                    assertSame(context, mc)
                    return marker
                }
            }
            val throwing: BigDecimal = object : BigDecimal("0.00") {
                override fun sqrt(mc: MathContext?): BigDecimal {
                    assertSame(context, mc)
                    throw failure
                }
            }
            assertSame(marker, replacement.sqrt(context))
            assertSame(failure, assertFailsWith<IllegalStateException> { throwing.sqrt(context) })
        }
    }
}

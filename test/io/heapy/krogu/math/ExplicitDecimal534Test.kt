// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class ExplicitDecimal534Test {
    @Test
    fun doubleFactoriesNameDifferentIntentsAndRetainScale() {
        val exact = BigDecimal("0.1000000000000000055511151231257827021181583404541015625")
        assertEquals(exact, Decimals.fromBinary(0.1))
        assertEquals(BigDecimal("0.1"), Decimals.fromCanonical(0.1))
        assertNotEquals(Decimals.fromBinary(0.1), Decimals.fromCanonical(0.1))
        for (value in listOf(-0.0, 0.0, 0.1, -0.1, Double.MIN_VALUE, Double.MAX_VALUE)) {
            assertEquals(BigDecimal(value), Decimals.fromBinary(value))
            assertEquals(BigDecimal.valueOf(value), Decimals.fromCanonical(value))
            val context = MathContext(7, RoundingMode.HALF_EVEN)
            assertEquals(BigDecimal(value, context), Decimals.fromBinary(value, context))
            val canonical = BigDecimal.valueOf(value)
            assertEquals(BigDecimal(canonical.unscaledValue(), canonical.scale(), context),
                Decimals.fromCanonical(value, context))
        }
        assertEquals(0, Decimals.fromBinary(-0.0).scale())
        assertEquals(1, Decimals.fromCanonical(-0.0).scale())
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)) {
            assertFailsWith<NumberFormatException> { Decimals.fromBinary(value) }
            assertFailsWith<NumberFormatException> { Decimals.fromCanonical(value) }
        }
    }

    @Test
    fun exactDivisionAndTwoRoundingDimensionsAreExplicit() {
        assertEquals(BigDecimal("0.5"), BigDecimal.ONE.divideExact(BigDecimal("2")))
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divideExact(BigDecimal("3")) }
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divideExact(BigDecimal.ZERO) }
        val value = BigDecimal("123.456")
        assertEquals(BigDecimal("123"), value.roundToPrecision(3, RoundingMode.HALF_EVEN))
        assertEquals(BigDecimal("123.456"), value.roundToScale(3, RoundingMode.HALF_EVEN))
        assertSame(value, value.roundToPrecision(0, RoundingMode.UNNECESSARY))
        assertFailsWith<IllegalArgumentException> { value.roundToPrecision(-1, RoundingMode.DOWN) }
        // Existing Kotlin division still uses left scale and HALF_EVEN.
        assertEquals(BigDecimal("0"), BigDecimal.ONE / BigDecimal("2"))
    }

    @Test
    fun parsingAndExactConversionFailuresHaveDifferentContracts() {
        assertNull(Decimals.parseOrNull("invalid"))
        assertEquals(BigDecimal("12.00"), Decimals.parseOrNull("12.00"))
        assertEquals(BigDecimal("1.2"), Decimals.parseOrNull("1.234", MathContext(2, RoundingMode.DOWN)))
        assertFailsWith<ArithmeticException> {
            Decimals.parseOrNull("1.234", MathContext(2, RoundingMode.UNNECESSARY))
        }
        assertEquals(ExactConversion.Value(12L), BigDecimal("12.00").toLongExactResult())
        assertEquals(ExactConversion.Value(BigInteger("12")), BigDecimal("12.00").toBigIntegerExactResult())
        assertIs<ExactConversion.Failure>(BigDecimal("12.01").toLongExactResult())
        assertIs<ExactConversion.Failure>(BigDecimal("2147483648").toIntExactResult())
        assertIs<ExactConversion.Failure>(BigInteger("9223372036854775808").toLongExactResult())
        assertEquals(ExactConversion.Value(Int.MIN_VALUE), BigInteger(Int.MIN_VALUE.toString()).toIntExactResult())
        val failure = ArithmeticException("conversion callback")
        val subclass = object : BigDecimal("1") {
            override fun longValueExact(): Long = throw failure
        }
        assertSame(failure, assertIs<ExactConversion.Failure>(subclass.toLongExactResult()).cause)
        val invalid = object : BigDecimal("1") {
            override fun intValueExact(): Int = throw IllegalArgumentException("callback configuration")
        }
        assertFailsWith<IllegalArgumentException> { invalid.toIntExactResult() }
    }

    @Test
    fun namedPairsRetainSignsScalesAndVirtualDelegation() {
        assertEquals(QuotientRemainder(BigInteger("-3"), BigInteger("-2")),
            BigInteger("-17").quotientAndRemainder(BigInteger("5")))
        val value = BigDecimal("17.00")
        val divisor = BigDecimal("5.0")
        val legacy = value.divideAndRemainder(divisor)
        assertEquals(QuotientRemainder(legacy[0], legacy[1]), value.quotientAndRemainder(divisor))
        val context = MathContext(2, RoundingMode.DOWN)
        val contextual = value.divideAndRemainder(divisor, context)
        assertEquals(QuotientRemainder(contextual[0], contextual[1]), value.quotientAndRemainder(divisor, context))
        val replacement = BigDecimal("7.00")
        val source = object : BigDecimal("1") {
            override fun divide(divisor: BigDecimal): BigDecimal = replacement
            override fun round(mc: MathContext): BigDecimal = replacement
            override fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal = replacement
        }
        assertSame(replacement, source.divideExact(divisor))
        assertSame(replacement, source.roundToPrecision(3, RoundingMode.DOWN))
        assertSame(replacement, source.roundToScale(3, RoundingMode.DOWN))
    }
}

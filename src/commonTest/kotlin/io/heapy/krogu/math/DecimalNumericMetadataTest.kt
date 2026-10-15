// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class DecimalNumericMetadataTest {
    @Test fun exactDigitsAtDecimalPowerNeighborsAndEveryScale() {
        for (exponent in listOf(1, 2, 7, 18, 19, 30, 100, 159, 1093, 2048)) {
            val models = listOf("9".repeat(exponent), "1" + "0".repeat(exponent),
                "1" + "0".repeat(exponent - 1) + "1")
            for (text in models) for (negative in listOf(false, true)) {
                val integer = BigInteger((if (negative) "-" else "") + text)
                assertEquals(text.length, integer.decimalMagnitudeDigits(), "coefficient digits: $exponent/$negative")
                for (scale in listOf(Int.MIN_VALUE, -1, 0, 1, Int.MAX_VALUE))
                    assertEquals(text.length, BigDecimal(integer, scale).precision(), "scale=$scale")
            }
        }
        for (scale in listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE))
            assertEquals(1, BigDecimal(BigInteger.ZERO, scale).precision())
    }

    @Test fun binaryPowerNeighborsAndCompactBoundariesHaveLiteralDigitExpectations() {
        for ((bits, digits) in listOf(8 to 3, 24 to 8, 53 to 16, 63 to 19, 127 to 39)) {
            for (offset in -1L..1L) {
                val integer = BigInteger.ONE.shiftLeft(bits).add(BigInteger.valueOf(offset))
                assertEquals(digits, integer.decimalMagnitudeDigits(), "2^$bits offset=$offset")
                assertEquals(digits, integer.negate().decimalMagnitudeDigits())
            }
        }
        for (text in listOf("0", "1", "9", "10", "99", "100", "110"))
            assertEquals(text.length, BigInteger(text).decimalMagnitudeDigits(), text)
    }

    @Test fun storedHelpersDoNotDispatchToReplacementIntegerMethods() {
        for (text in listOf("0", "-1", "110", "-" + "7".repeat(100))) {
            val value = object : BigInteger(text) {
                override fun signum(): Int = error("signum")
                override fun abs(): BigInteger = error("abs")
                override fun bitLength(): Int = error("bitLength")
                override fun toString(): String = error("toString")
                override fun toString(radix: Int): String = error("radix toString")
                override fun divideAndRemainder(value: BigInteger): Array<BigInteger> = error("divideAndRemainder")
                override fun pow(exponent: Int): BigInteger = error("pow")
                override fun compareTo(other: BigInteger): Int = error("compareTo")
            }
            val magnitude = text.removePrefix("-")
            assertEquals(magnitude.length, value.decimalMagnitudeDigits())
            assertEquals(0, value.compareDecimalSignificand(BigInteger(magnitude + "0")))
        }
    }

    @Test fun normalizedSignificandsMatchAnIndependentBoundedTextModel() {
        val texts = listOf("0", "1", "9", "10", "11", "100", "101", "110", "119", "120",
            "123", "1230", "1231", "999", "1000", "1001", "1" + "0".repeat(128),
            "1" + "0".repeat(127) + "1", "9".repeat(129))
        for (a in texts) for (b in texts) {
            val width = maxOf(a.length, b.length)
            val expected = a.padEnd(width, '0').compareTo(b.padEnd(width, '0')).compareTo(0)
            for (negative in listOf(false, true)) {
                val left = BigInteger((if (negative) "-" else "") + a)
                val right = BigInteger((if (negative) "-" else "") + b)
                assertEquals(expected, left.compareDecimalSignificand(right), "$a/$b negative=$negative")
            }
        }
    }

    @Test fun comparisonHandlesEqualDecadesRemaindersSignsAndExtremeScales() {
        for (scale in listOf(Int.MIN_VALUE + 129, -100, 0, 100, Int.MAX_VALUE)) {
            for (negative in listOf(false, true)) {
                val sign = if (negative) -1 else 1
                fun value(text: String, s: Int) = BigDecimal(BigInteger((if (negative) "-" else "") + text), s)
                val short = value("123", scale - 129)
                val equal = value("123" + "0".repeat(129), scale)
                val tail = value("123" + "0".repeat(128) + "1", scale)
                val below = value("122" + "9".repeat(129), scale)
                assertEquals(0, short.compareTo(equal))
                assertEquals(-sign, short.compareTo(tail))
                assertEquals(sign, short.compareTo(below))
                assertEquals(sign, tail.compareTo(short))
            }
        }
        assertEquals(1, BigDecimal(BigInteger.ONE, Int.MIN_VALUE).compareTo(BigDecimal(BigInteger.ONE, Int.MAX_VALUE)))
        assertEquals(-1, BigDecimal(BigInteger.ONE.negate(), Int.MIN_VALUE)
            .compareTo(BigDecimal(BigInteger.ONE.negate(), Int.MAX_VALUE)))
        assertEquals(0, BigDecimal(BigInteger.ZERO, Int.MIN_VALUE).compareTo(BigDecimal(BigInteger.ZERO, Int.MAX_VALUE)))
    }

    private class DecimalProbe(text: String, private val label: String, private val trace: StringBuilder,
        private val digits: Int? = null, private val failure: IllegalStateException? = null) : BigDecimal(text) {
        override fun signum(): Int { trace.append("$label.sign;"); return super.signum() }
        override fun precision(): Int {
            trace.append("$label.precision;")
            failure?.let { throw it }
            return digits ?: super.precision()
        }
        override fun scale(): Int = error("scale callback")
        override fun unscaledValue(): BigInteger = error("coefficient callback")
        override fun toString(): String = error("string callback")
        fun directCompare(other: BigDecimal): Int = super.compareTo(other)
        fun directPrecision(): Int = super.precision()
    }

    @Test fun truthfulDirectSuperCallsAndExceptionIdentityKeepTheirOrder() {
        val trace = StringBuilder()
        val left = DecimalProbe("123", "a", trace)
        val right = DecimalProbe("123.00", "b", trace)
        assertEquals(3, left.directPrecision())
        assertEquals("", trace.toString())
        assertEquals(0, left.directCompare(right))
        assertEquals("a.sign;b.sign;a.precision;b.precision;", trace.toString())
        for (throwsOnLeft in listOf(false, true)) {
            trace.clear()
            val failure = IllegalStateException("precision sentinel")
            val a = DecimalProbe("123", "a", trace, failure = if (throwsOnLeft) failure else null)
            val b = DecimalProbe("123.00", "b", trace, failure = if (throwsOnLeft) null else failure)
            assertSame(failure, assertFailsWith<IllegalStateException> { a.directCompare(b) })
            assertEquals("a.sign;b.sign;a.precision;" + if (throwsOnLeft) "" else "b.precision;", trace.toString())
        }
    }

    @Test fun misleadingPrecisionStillControlsReportedDecadeBeforeStoredSignificands() {
        val trace = StringBuilder()
        // Both reported adjusted exponents are zero: compare stored 1.23 with 1.24.
        val left = DecimalProbe("123", "a", trace, digits = 1)
        val right = DecimalProbe("12.4", "b", trace, digits = 2)
        assertEquals(-1, left.directCompare(right))
        assertEquals("a.sign;b.sign;a.precision;b.precision;", trace.toString())
        trace.clear()
        // Changing only the reported exponent must retain the earlier decision.
        assertEquals(1, DecimalProbe("123", "a", trace, digits = 2).directCompare(right))
        assertEquals("a.sign;b.sign;a.precision;b.precision;", trace.toString())
        trace.clear()
        // The compact same-scale prefix continues to bypass virtual getters.
        assertEquals(-1, DecimalProbe("1", "a", trace, failure = IllegalStateException())
            .directCompare(DecimalProbe("2", "b", trace, failure = IllegalStateException())))
        assertEquals("", trace.toString())
    }
}

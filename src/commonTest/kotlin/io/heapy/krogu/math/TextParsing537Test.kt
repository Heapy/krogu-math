// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class TextParsing537Test {
    private class View(private val source: String) : CharSequence {
        override val length: Int get() = source.length
        override fun get(index: Int): Char = source[index]
        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = error("no substring")
        override fun toString(): String = error("no materialization")
    }

    @Test fun frozenIntegerOracleAcrossAllReaders() {
        for ((index, row) in jdk25IntegerParserCases().withIndex()) {
            val radix = row[0].toInt()
            val wrapped = "[$" + row[1] + "]"
            fun outcome(parse: () -> BigInteger): String = try { parse().toString() }
                catch (e: NumberFormatException) { "!NumberFormatException:${e.message}" }
            assertEquals(row[2], outcome { BigInteger.parseText(View(row[1]), radix) }, "whole $index")
            assertEquals(row[2], outcome { BigInteger.parseText(View(wrapped), 2, row[1].length, radix) }, "slice $index")
            assertEquals(row[2], outcome { BigInteger.parseChars(wrapped.toCharArray(), 2, row[1].length, radix) }, "chars $index")
        }
    }

    @Test fun radixGroupsAgainstBoundedLongValuesAndRoundTrips() {
        val values = listOf(0L, 1L, -1L, 1073741823L, 1073741824L, 2147483647L,
            2147483648L, Long.MAX_VALUE, Long.MIN_VALUE)
        for (radix in 2..36) for (value in values) {
            val text = value.toString(radix)
            assertEquals(value.toString(), BigInteger.parseText(View(text), radix).toString(), "$radix/$value")
        }
        // 3^19 exceeds a base-2^30 cell; carry must not be truncated to one cell.
        for (radix in 2..36) for (size in listOf(1, 8, 9, 18, 19, 31, 63, 127)) {
            val text = (radix - 1).toString(radix).repeat(size)
            assertEquals(text, BigInteger.parseText(View(text), radix).toString(radix), "$radix/$size")
        }
    }

    @Test fun decimalSlicesPreserveScaleGrammarAndRounding() {
        val texts = listOf("0", "-0.000", "+123.4500", ".5", "5.", "000123e-9",
            "١٢٣.٤٥e+2", "９９９９９９９９９６", "123456789012345678901234567890.00100",
            "1E+2147483647", "1E-2147483647")
        for (text in texts) for (precision in listOf(0, 1, 7, 18)) for (mode in RoundingMode.entries) {
            val mc = MathContext(precision, mode)
            fun outcome(parse: () -> BigDecimal): String = try {
                val value = parse()
                "${value.unscaledValue()}@${value.scale()}"
            } catch (e: ArithmeticException) { "!ArithmeticException:${e.message}" }
            val expected = outcome { BigDecimal(text, mc) }
            assertEquals(expected, outcome { BigDecimal.parseText(View(text), mc) }, "$text/$mc")
            val chars = "[$text]".toCharArray()
            assertEquals(expected, outcome { BigDecimal.parseText(View("[$text]"), 1, text.length, mc) })
            assertEquals(expected, outcome { BigDecimal(chars, 1, text.length, mc) })
        }
        for (text in listOf("", "+", "1..0", "1e", "1e+", "1e2147483648", "1 0", "NaN")) {
            fun outcome(parse: () -> BigDecimal): String = try { parse().toString() }
                catch (e: NumberFormatException) { "${e::class}:${e.message}" }
            assertEquals(outcome { BigDecimal(text) }, outcome { BigDecimal.parseText(View(text)) }, text)
        }
    }

    @Test fun rangesLimitsAndOwnershipBeforeTraversal() {
        val failure = IllegalStateException("reader")
        val unreadable = object : CharSequence {
            override val length = 3
            override fun get(index: Int): Char = throw failure
            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = error("substring")
        }
        assertFailsWith<ArithmeticException> { BigInteger.parseTextBounded(unreadable, 0, 3, 2) }
        assertFailsWith<ArithmeticException> { BigDecimal.parseTextBounded(unreadable, 0, 3, 2) }
        assertSame(failure, assertFailsWith<IllegalStateException> { BigInteger.parseText(unreadable) })
        assertSame(failure, assertFailsWith<IllegalStateException> { BigDecimal.parseText(unreadable) })
        for ((offset, length) in listOf(-1 to 1, 0 to -1, Int.MAX_VALUE to 1, 1 to Int.MAX_VALUE)) {
            assertFailsWith<NumberFormatException> { BigInteger.parseText("123", offset, length) }
            assertFailsWith<NumberFormatException> { BigDecimal.parseText("123", offset, length) }
        }
        val chars = "1234500".toCharArray()
        val integer = BigInteger.parseChars(chars)
        val decimal = BigDecimal(chars)
        chars.fill('9')
        assertEquals("1234500", integer.toString())
        assertEquals("1234500", decimal.toString())
        assertEquals(integer, BigInteger.parseTextBounded("1234500", 0, 7, 7))
        assertFailsWith<IllegalArgumentException> { BigDecimal.parseCharsBounded(chars, 0, 7, -1) }
    }
}

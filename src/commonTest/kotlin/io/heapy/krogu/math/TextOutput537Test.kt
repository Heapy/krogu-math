// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TextOutput537Test {
    @Test fun integerRadicesAndBufferAtomicity() {
        for (text in listOf("0", "1", "-1", "1073741824", "-9223372036854775808",
            "12345678901234567890123456789012345678901234567890")) {
            val value = BigInteger(text)
            for (radix in listOf(0, 1, 37) + (2..36)) {
                val expected = value.toString(radix)
                val output = StringBuilder()
                assertEquals(expected.length.toLong(), value.appendTo(output, radix, expected.length.toLong()))
                assertEquals(expected, output.toString())
                val buffer = CharArray(expected.length + 4) { '#' }
                assertEquals(expected.length, value.writeTo(buffer, 2, expected.length, radix))
                assertEquals("##${expected}##", buffer.concatToString())
                val short = CharArray(expected.length + 4) { '#' }
                assertFailsWith<ArithmeticException> { value.writeTo(short, 2, expected.length - 1, radix) }
                assertTrue(short.all { it == '#' })
            }
        }
    }

    @Test fun decimalLayoutsAgreeWithExistingCanonicalAndAlternateFormats() {
        val coefficients = listOf("0", "1", "-1", "12", "-123", "1234500",
            "1234567890123456789012345678901234567890")
        for (coefficient in coefficients) for (scale in -12..15) {
            val value = BigDecimal(BigInteger(coefficient), scale)
            for (format in DecimalTextFormat.entries) {
                val expected = when (format) {
                    DecimalTextFormat.CANONICAL -> value.toString()
                    DecimalTextFormat.PLAIN -> value.toPlainString()
                    DecimalTextFormat.ENGINEERING -> value.toEngineeringString()
                }
                val output = StringBuilder()
                assertEquals(expected.length.toLong(), value.appendTo(output, format), "$coefficient@$scale/$format")
                assertEquals(expected, output.toString(), "$coefficient@$scale/$format")
                val buffer = CharArray(expected.length + 2) { '#' }
                assertEquals(expected.length, value.writeTo(buffer, 1, expected.length, format))
                assertEquals("#${expected}#", buffer.concatToString())
                val short = CharArray(expected.length + 2) { '#' }
                assertFailsWith<ArithmeticException> { value.writeTo(short, 1, expected.length - 1, format) }
                assertTrue(short.all { it == '#' })
            }
        }
    }

    @Test fun explicitLimitsRejectHugePaddingBeforeAnyDestinationWrite() {
        val output = StringBuilder("prefix")
        for (scale in listOf(Int.MIN_VALUE, Int.MAX_VALUE)) {
            val value = BigDecimal(BigInteger.ONE, scale)
            assertFailsWith<ArithmeticException> { value.appendTo(output, DecimalTextFormat.PLAIN, 100) }
            assertEquals("prefix", output.toString())
            val buffer = CharArray(10) { '#' }
            assertFailsWith<ArithmeticException> { value.writeTo(buffer, format = DecimalTextFormat.PLAIN) }
            assertTrue(buffer.all { it == '#' })
        }
        assertFailsWith<IllegalArgumentException> { BigInteger.ONE.appendTo(output, maxChars = -1) }
        assertFailsWith<IllegalArgumentException> { BigDecimal.ONE.appendTo(output, maxChars = -1) }
        for ((offset, length) in listOf(-1 to 1, 0 to -1, Int.MAX_VALUE to 1, 1 to Int.MAX_VALUE)) {
            assertFailsWith<IndexOutOfBoundsException> { BigInteger.ONE.writeTo(CharArray(3), offset, length) }
            assertFailsWith<IndexOutOfBoundsException> { BigDecimal.ONE.writeTo(CharArray(3), offset, length) }
        }
    }

    @Test fun paddingIsChunkedAndDestinationFailureKeepsIdentity() {
        var count = 0L
        var largest = 0
        val sink = object : Appendable {
            override fun append(value: Char): Appendable { count++; return this }
            override fun append(value: CharSequence?): Appendable = append(value, 0, value?.length ?: 4)
            override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
                count += endIndex - startIndex
                largest = maxOf(largest, endIndex - startIndex)
                return this
            }
        }
        assertEquals(10001L, BigDecimal(BigInteger.ONE, -10000).appendTo(sink, DecimalTextFormat.PLAIN, 10001))
        assertEquals(10001L, count)
        assertTrue(largest <= 256)
        val failure = IllegalStateException("destination")
        val throwing = object : Appendable {
            override fun append(value: Char): Appendable = throw failure
            override fun append(value: CharSequence?): Appendable = throw failure
            override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable = throw failure
        }
        assertSame(failure, assertFailsWith<IllegalStateException> { BigInteger.TEN.appendTo(throwing) })
        assertSame(failure, assertFailsWith<IllegalStateException> { BigDecimal.TEN.appendTo(throwing) })
    }

    @Test fun subtypeVirtualFormattingIsPreserved() {
        val integer = object : BigInteger("123") { override fun toString(radix: Int): String = "integer:$radix" }
        val decimal = object : BigDecimal("123") {
            override fun toString(): String = "canonical"
            override fun toPlainString(): String = "plain"
            override fun toEngineeringString(): String = "engineering"
        }
        assertEquals("integer:7", StringBuilder().also { integer.appendTo(it, 7) }.toString())
        for ((format, expected) in listOf(DecimalTextFormat.CANONICAL to "canonical",
            DecimalTextFormat.PLAIN to "plain", DecimalTextFormat.ENGINEERING to "engineering"))
            assertEquals(expected, StringBuilder().also { decimal.appendTo(it, format) }.toString())
    }
}

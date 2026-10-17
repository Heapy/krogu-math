// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class DecimalExactChars589Test {
    private fun fields(coefficient: String, scale: Int, precision: Int, actual: BigDecimal) {
        assertEquals(coefficient, actual.unscaledValue().toString())
        assertEquals(scale, actual.scale())
        assertEquals(precision, actual.precision())
    }

    @Test fun wholeAndSliceRetainExactLiteralCohortsAndScaleEndpoints() {
        data class Row(val text: String, val coefficient: String, val scale: Int, val precision: Int)
        val rows = listOf(
            Row("0001200.00", "120000", 2, 6),
            Row("-00123.4500", "-1234500", 4, 7),
            Row("+٠٠١٢٣.٤٥e+٢", "12345", 0, 5),
            Row("١٢.٣e-٢", "123", 3, 3),
            Row("-.0000", "0", 4, 1),
            Row("-000", "0", 0, 1),
            Row("1e2147483648", "1", Int.MIN_VALUE, 1),
            Row("0e2147483648", "0", Int.MIN_VALUE, 1),
            Row("1.000e-2147483644", "1000", Int.MAX_VALUE, 4),
            Row("-0e-2147483647", "0", Int.MAX_VALUE, 1),
            Row("9999999999999999996", "9999999999999999996", 0, 19),
            Row("100000000000000000000000000001", "100000000000000000000000000001", 0, 30))
        for (row in rows) {
            fields(row.coefficient, row.scale, row.precision, BigDecimal(row.text.toCharArray()))
            val selected = "?${row.text}!".toCharArray()
            fields(row.coefficient, row.scale, row.precision, BigDecimal(selected, 1, row.text.length))
        }
    }

    @Test fun ownedLeadingZeroSlicesKeepTinyExactCoefficientsAfterSourceMutation() {
        // Construct the input as chars, without a whole-input String or a fake
        // platform capacity. Leading zeros contribute no coefficient digits.
        val zeros = 16_384
        val tail = "123.4500e+2".toCharArray()
        val input = CharArray(1 + zeros + tail.size) { '0' }
        input[0] = '-'
        tail.copyInto(input, 1 + zeros)
        val whole = BigDecimal(input)
        val wrapped = CharArray(input.size + 2) { '?' }
        input.copyInto(wrapped, 1)
        val slice = BigDecimal(wrapped, 1, input.size)
        input.fill('9')
        wrapped.fill('8')
        for (actual in listOf(whole, slice)) {
            fields("-1234500", 2, 7, actual)
            assertEquals("-12345.00", actual.toString())
        }
    }

    @Test fun malformedSelectedTextAndIntBoundaryRangesStillReject() {
        val malformed = listOf("", "+", ".", "-.", "1..2", "1e", "1e+", "99x", "99e1x",
            "99e99999999999", "99e-2147483648", "99e2147483649", "0e-2147483648", "١٢e٣x")
        for (text in malformed) {
            assertFailsWith<NumberFormatException>(text) { BigDecimal(text.toCharArray()) }
            val wrapped = "?${text}!".toCharArray()
            assertFailsWith<NumberFormatException>(text) { BigDecimal(wrapped, 1, text.length) }
        }
        for ((offset, length) in listOf(-1 to 1, 0 to -1, 2 to 0, Int.MAX_VALUE to 0,
            1 to Int.MAX_VALUE, Int.MIN_VALUE to Int.MAX_VALUE, 0 to Int.MAX_VALUE)) {
            val failure = assertFailsWith<NumberFormatException> { BigDecimal(charArrayOf('x'), offset, length) }
            assertFalse(failure.cause is IndexOutOfBoundsException)
        }
    }

    private class ExactChild : BigDecimal {
        constructor(value: CharArray) : super(value)
        constructor(value: CharArray, offset: Int, length: Int) : super(value, offset, length)
        fun roundViaSuper(context: MathContext): BigDecimal = super.round(context)
    }

    @Test fun wholeAndSliceSuperConstructionRetainExactFieldsBeforeCallerRounding() {
        val input = "-00123.4500".toCharArray()
        val whole = ExactChild(input)
        val wrapped = "?-00123.4500!".toCharArray()
        val slice = ExactChild(wrapped, 1, input.size)
        input.fill('0')
        wrapped.fill('0')
        for (actual in listOf(whole, slice)) {
            fields("-1234500", 4, 7, actual)
            fields("-12345", 2, 5, actual.roundViaSuper(MathContext(5, RoundingMode.DOWN)))
        }
    }
}

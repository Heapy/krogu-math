// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Symbolic range controls and bounded grammar checks; no giant String is allocated. */
class IntegerParserRange559Test {
    @Test fun conservativeBitBoundUsesTheLastNonrejectedCountForEveryRadix() {
        // Literal count boundaries for floor(log2(radix)) = 1, 2, 3, 4, 5.
        val groups = listOf(
            2..3 to Int.MAX_VALUE,
            4..7 to 1_073_741_824,
            8..15 to 715_827_883,
            16..31 to 536_870_912,
            32..36 to 429_496_730,
        )
        for ((radices, last) in groups) for (radix in radices) {
            assertFalse(MagnitudeLimits.parserDefinitelyOverflows(1, radix), "one digit radix=$radix")
            assertFalse(MagnitudeLimits.parserDefinitelyOverflows(last, radix), "boundary radix=$radix")
            if (last != Int.MAX_VALUE) {
                assertTrue(MagnitudeLimits.parserDefinitelyOverflows(last + 1, radix), "above radix=$radix")
                assertTrue(MagnitudeLimits.parserDefinitelyOverflows(Int.MAX_VALUE, radix), "max radix=$radix")
            }
        }
    }

    @Test fun symbolicPublicHexWitnessIsRejectedWithoutAllocatingItsText() {
        // Public witness: '1', 2^30-1 zero digits, then '1': 2^30+1 digits.
        assertTrue(MagnitudeLimits.parserDefinitelyOverflows(1_073_741_825, 16))
        // These false results intentionally do not assert that every value fits:
        // the lower bound for radix three is conservative, not a complete classifier.
        assertFalse(MagnitudeLimits.parserDefinitelyOverflows(Int.MAX_VALUE, 2))
        assertFalse(MagnitudeLimits.parserDefinitelyOverflows(Int.MAX_VALUE, 3))
    }

    @Test fun ordinarySignsLeadingZerosAndRadicesKeepTheirValues() {
        val zeroPadding = "0".repeat(96) + "\u0660\uFF10"
        for (radix in 2..36) {
            val expected = radix.toLong() * radix + 1
            assertEquals(expected, BigInteger("101", radix).toLong())
            assertEquals(expected, BigInteger("+$zeroPadding" + "101", radix).toLong())
            assertEquals(-expected, BigInteger("-$zeroPadding" + "101", radix).toLong())
            for (prefix in listOf("", "+", "-"))
                assertEquals(BigInteger.ZERO, BigInteger(prefix + zeroPadding, radix))
        }
        assertEquals("9223372036854775807", BigInteger("7fffffffffffffff", 16).toString())
        assertEquals("-9223372036854775808", BigInteger("-8000000000000000", 16).toString())
        assertEquals(BigInteger.valueOf(35), BigInteger("Z", 36))
    }

    @Test fun boundedMalformedTextRetainsDigitAndSignErrors() {
        for (radix in 2..36) for (text in listOf("10!", "000!", "1 0", "1\uD8000"))
            assertFailsWith<NumberFormatException>("radix=$radix text=$text") { BigInteger(text, radix) }
        for (text in listOf("!+", "1!-", "++1", "--1"))
            assertFailsWith<NumberFormatException> { BigInteger(text, 10) }
        for (text in listOf("", "+", "-"))
            assertFailsWith<NumberFormatException> { BigInteger(text, 10) }
        for (radix in listOf(Int.MIN_VALUE, 1, 37, Int.MAX_VALUE))
            assertFailsWith<NumberFormatException> { BigInteger("0", radix) }
        assertFailsWith<NumberFormatException> { BigInteger("2", 2) }
    }
}

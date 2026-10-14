// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlainStringPreflightJvmTest {
    @Test fun directConstructorsRejectAllFourteenBeforeAllocation() {
        for (witness in plainOverflowWitnesses) {
            val value = BigDecimal(BigInteger(witness.coefficient), witness.scale)
            val failure = assertFailsWith<java.lang.OutOfMemoryError>("$witness") {
                value.toPlainString()
            }
            assertEquals("too large to fit in a String", failure.message)
        }
    }

    @Test fun lengthBoundariesNeedNoLargeCoefficientOrString() {
        // Keep the production helper private; reflection supplies symbolic digit counts.
        val method = BigDecimal::class.java.getDeclaredMethod(
            "plainStringLength", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
        ).also { it.isAccessible = true }
        fun length(digits: Int, scale: Int, sign: Int): Long =
            method.invoke(BigDecimal(BigInteger.ONE, scale), digits, sign) as Long
        for (sign in listOf(1, -1)) {
            val signLength = if (sign < 0) 1 else 0
            for (offset in -1..1) {
                val expected = Int.MAX_VALUE.toLong() + offset
                val positiveScale = (expected - 2 - signLength).toInt()
                assertEquals(expected, length(1, positiveScale, sign))
                val negativeScale = (9L + signLength - expected).toInt()
                assertEquals(expected, length(9, negativeScale, sign))
            }
        }
        assertEquals(4294967296L, length(Int.MAX_VALUE, Int.MIN_VALUE, -1))
        assertEquals(2147483647L, length(Int.MAX_VALUE, 0, 1))
        assertEquals(2147483648L, length(Int.MAX_VALUE, 0, -1))
        assertEquals(2147483649L, length(Int.MAX_VALUE, 1, -1))
        assertEquals(3L, length(1, 1, 1))
    }
}

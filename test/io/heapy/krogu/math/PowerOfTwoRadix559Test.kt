// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class PowerOfTwoRadix559Test {
    @Test fun storedLimbsFormatAcrossAllPowerOfTwoRadices() {
        val values = listOf(
            BigInteger.ONE,
            BigInteger.ONE.shiftLeft(30).add(BigInteger.ONE),
            BigInteger.ONE.shiftLeft(31).add(BigInteger.ONE),
        )
        val expected = listOf(
            listOf("1", "1", "1", "1", "1"),
            listOf(
                "1" + "0".repeat(29) + "1",
                "1" + "0".repeat(14) + "1",
                "1" + "0".repeat(9) + "1",
                "4" + "0".repeat(6) + "1",
                "1" + "0".repeat(5) + "1",
            ),
            listOf(
                "1" + "0".repeat(30) + "1",
                "2" + "0".repeat(14) + "1",
                "2" + "0".repeat(9) + "1",
                "8" + "0".repeat(6) + "1",
                "2" + "0".repeat(5) + "1",
            ),
        )
        for ((value, row) in values.zip(expected))
            for ((radix, text) in listOf(2, 4, 8, 16, 32).zip(row))
                assertEquals(text, value.toString(radix), "radix=$radix value=$value")
    }

    @Test fun signsZeroInvalidRadixAndSubclassesKeepFallbackBehavior() {
        assertEquals("-7b", BigInteger("-123").toString(16))
        assertEquals("0", BigInteger.ZERO.toString(16))
        assertEquals("123", BigInteger("123").toString(1))

        val positive = SubclassInteger("123")
        assertEquals("7b", positive.toString(16))

        val negative = SubclassInteger("-123")
        assertEquals("-7b", negative.toString(16))
    }

    private class SubclassInteger(value: String) : BigInteger(value)
}

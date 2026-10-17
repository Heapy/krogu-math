// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerParserOracleTest {
    @Test fun stringConstructorValuesAndDiagnostics() {
        for ((index, row) in jdk25IntegerParserCases().withIndex()) {
            val radix = row[0].toInt()
            fun outcome(construct: () -> BigInteger): String = try {
                construct().toString()
            } catch (e: NumberFormatException) { "!NumberFormatException:${e.message}" }
            assertEquals(row[2], outcome { BigInteger(row[1], radix) }, "parser case $index radix $radix")
            if (radix == 10) assertEquals(row[2], outcome { BigInteger(row[1]) }, "decimal parser case $index")
        }
    }
}

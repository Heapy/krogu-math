// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerByteOracleTest {
    @Test fun constructorsAndArrayIsolation() {
        val outcomes = jdk25IntegerByteOutcomes()
        for ((index, row) in jdk25IntegerByteCases().withIndex()) {
            val parts = row.split('|')
            val bytes = parts[1].hexToByteArray()
            val sign = parts[2].toInt()
            val off = parts[3].toInt()
            val len = parts[4].toInt()
            val expected = outcomes[parts[5].toInt()]
            val actual = try {
                val value = when (parts[0]) {
                    "twos" -> BigInteger(bytes, off, len)
                    "signed" -> BigInteger(sign, bytes, off, len)
                    "whole" -> BigInteger(bytes)
                    else -> BigInteger(sign, bytes)
                }
                assertEquals(expected, value.toString(), "byte constructor case $index")
                val hash = value.hashCode()
                bytes.fill(0x55)
                value.toByteArray().fill(0x33)
                assertEquals(expected, value.toString(), "array isolation case $index")
                assertEquals(hash, value.hashCode(), "hash after mutation case $index")
                value.toString()
            } catch (e: NumberFormatException) { "!NumberFormatException:${e.message}" }
              catch (e: IndexOutOfBoundsException) { "!IndexOutOfBoundsException:${e.message}" }
            assertEquals(expected, actual, "byte constructor case $index")
        }
    }
}

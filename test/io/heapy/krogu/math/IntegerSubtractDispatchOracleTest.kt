// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerSubtractDispatchOracleTest {
    @Test fun subtractionCallbacksValuesAndIdentitiesMatchJdk() {
        for (row in jdk25IntegerSubtractDispatchCases())
            assertEquals(row[4], evaluate(row), "${row.take(4)} ${row[5]}")
    }

    private fun evaluate(row: Array<String>): String {
        val trace = StringBuilder()
        val sourceReplacement = BigInteger("7")
        val operandReplacement = BigInteger("7")
        val left = object : BigInteger(row[0]) {
            override fun add(value: BigInteger): BigInteger {
                trace.append("add;")
                return when (row[2]) {
                    "source" -> this
                    "fixed" -> sourceReplacement
                    "throw" -> throw IllegalStateException("add callback")
                    else -> super.add(value)
                }
            }
            override fun negate(): BigInteger {
                trace.append("sourceNegate;")
                return when (row[3]) {
                    "operand" -> this
                    "fixed" -> operandReplacement
                    "throw" -> throw IllegalStateException("source negate callback")
                    else -> super.negate()
                }
            }
        }
        val right = if (row[5] == "self") left else object : BigInteger(row[1]) {
            override fun negate(): BigInteger {
                trace.append("negate;")
                return when (row[3]) {
                    "operand" -> this
                    "fixed" -> operandReplacement
                    "throw" -> throw IllegalStateException("negate callback")
                    else -> super.negate()
                }
            }
        }
        return try {
            val result: BigInteger? = left.subtract(right)
            val calls = trace.toString()
            val text = when {
                result === left -> row[0]
                result === right -> row[1]
                else -> result.toString()
            }
            "$text|${result === left}|${result === right}|${result === sourceReplacement}|${result === operandReplacement}|$calls"
        } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
    }
}

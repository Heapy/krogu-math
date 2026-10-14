// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

internal fun evaluateIntegerDivisionOracle(v: Array<String>): String {
    val trace = StringBuilder()
    val value = object : BigInteger(v[2]) {
        override fun compareTo(other: BigInteger?): Int {
            trace.append("compare;")
            throw IllegalStateException("comparison callback")
        }
    }
    val operand = BigInteger(v[3])
    val result = try {
        when (v[1]) {
            "divide" -> value.divide(operand).toString()
            "remainder" -> value.remainder(operand).toString()
            "pair" -> value.divideAndRemainder(operand).joinToString("|")
            "text" -> value.toString(v[4].toInt())
            else -> error("Unknown integer division oracle operation ${v[1]}")
        }
    } catch (e: Exception) { "!${e::class.simpleName}" }
    return "$result|$trace"
}

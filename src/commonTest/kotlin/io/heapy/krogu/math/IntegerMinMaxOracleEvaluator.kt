// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

internal fun evaluateIntegerMinMaxOracle(v: Array<String>): String {
    val operation = v[1].substringBefore(':')
    val throwing = v[1].substringAfter(':', "return") == "throw"
    val trace = StringBuilder()
    val value = if (operation.startsWith("base")) BigInteger(v[2]) else object : BigInteger(v[2]) {
        override fun compareTo(other: BigInteger?): Int {
            trace.append("compare:").append(other == null).append(';')
            if (throwing) throw IllegalStateException("comparison callback")
            return v[4].toInt()
        }
    }
    val operand = if (v[3] == "~") null else BigInteger(v[3])
    val result = try {
        if (operation == "baseCompare") value.compareTo(operand).toString()
        else {
            val computed = if (operation == "min" || operation == "baseMin") value.min(operand) else value.max(operand)
            "${computed?.toString() ?: "null"}|${computed === value}|${computed === operand}"
        }
    } catch (e: Exception) { "!${e::class.simpleName}" + if (e is IllegalStateException) ":${e.message}" else "" }
    return "$result|$trace"
}

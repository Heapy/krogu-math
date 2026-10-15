package io.heapy.krogu.math

internal fun evaluateMinMaxOracle(v: Array<String>): String {
    require(v[3] != "~") { "Null comparison row belongs to ComparisonNull574JvmTest" }
    val operation = v[1].substringBefore(':')
    val throwing = v[1].substringAfter(':', "return") == "throw"
    val trace = StringBuilder()
    val value = if (operation.startsWith("base")) BigDecimal(v[2]) else object : BigDecimal(v[2]) {
        override fun compareTo(other: BigDecimal): Int {
            trace.append("compare:false;")
            if (throwing) throw IllegalStateException("comparison callback")
            return v[4].toInt()
        }
    }
    val operand = BigDecimal(v[3])
    val result = try {
        val computed = if (operation == "min" || operation == "baseMin") value.min(operand) else value.max(operand)
        "${computed?.toString() ?: "null"}|${computed === value}|${computed === operand}"
    } catch (e: Exception) { "!${e::class.simpleName}" + if (e is IllegalStateException) ":${e.message}" else "" }
    return "$result|$trace"
}

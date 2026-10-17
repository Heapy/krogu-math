package io.heapy.krogu.math

internal fun evaluateComparisonOracle(v: Array<String>): String {
    val operation = v[1].split(':')
    val trace = StringBuilder()
    val result = when (operation[0]) {
        "base" -> BigDecimal(v[2]).compareTo(BigDecimal(v[3]))
        "scaled" -> {
            fun operand(text: String): BigDecimal {
                val parts = text.split('@')
                return BigDecimal(BigInteger(parts[0]), parts[1].toInt())
            }
            operand(v[2]).compareTo(operand(v[3]))
        }
        "getters" -> {
            val digits = v[4].toInt()
            fun operand(text: String, label: String, sign: Int) = object : BigDecimal(text) {
                override fun signum(): Int { trace.append(label).append("sign;"); return sign }
                override fun precision(): Int { trace.append(label).append("precision;"); return if (digits < 0) super.precision() else digits }
                override fun scale(): Int { trace.append(label).append("scale;"); return 0 }
            }
            operand(v[2], "a", operation[1].toInt()).compareTo(operand(v[3], "b", operation[2].toInt()))
        }
        "self" -> {
            val value = object : BigDecimal(v[2]) {
                private var calls = 0
                override fun signum(): Int { trace.append("sign;"); return operation[if (calls++ == 0) 1 else 2].toInt() }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun scale(): Int { trace.append("scale;"); return 0 }
            }
            value.compareTo(value)
        }
        else -> error("Unknown comparison operation ${operation[0]}")
    }
    return "$result|$trace"
}

package io.heapy.krogu.math

internal fun evaluateNullableBinaryOracle(v: Array<String>): String {
    val operation = v[1].substringBefore(':')
    val behavior = v[1].substringAfter(':')
    val precision = v[4].toInt()
    val trace = StringBuilder()
    val value = object : BigDecimal(v[2]) {
        private fun callback(operand: BigDecimal?, base: () -> BigDecimal): BigDecimal {
            trace.append(operation).append(':').append(operand == null).append(';')
            return when (behavior) {
                "return" -> BigDecimal("42.00")
                "self" -> this
                "throw" -> throw IllegalStateException("operation callback")
                "super" -> base()
                else -> error("Unknown callback behavior $behavior")
            }
        }
        override fun add(augend: BigDecimal?): BigDecimal = callback(augend) { super.add(augend) }
        override fun subtract(subtrahend: BigDecimal?): BigDecimal = callback(subtrahend) { super.subtract(subtrahend) }
        override fun multiply(multiplicand: BigDecimal?): BigDecimal = callback(multiplicand) { super.multiply(multiplicand) }
    }
    val operand = if (v[3] == "~") null else BigDecimal(v[3])
    val context = if (precision < 0) null else MathContext(precision, RoundingMode.valueOf(v[5]))
    val result = try {
        val computed = if (precision == -2) when (operation) {
            "add" -> value.add(operand)
            "subtract" -> value.subtract(operand)
            "multiply" -> value.multiply(operand)
            else -> error("Unknown operation $operation")
        } else when (operation) {
            "add" -> value.add(operand, context)
            "subtract" -> value.subtract(operand, context)
            "multiply" -> value.multiply(operand, context)
            else -> error("Unknown operation $operation")
        }
        computed.toString()
    } catch (e: Exception) { "!${e::class.simpleName}" + if (e is IllegalStateException) ":${e.message}" else "" }
    return "$result|$trace"
}

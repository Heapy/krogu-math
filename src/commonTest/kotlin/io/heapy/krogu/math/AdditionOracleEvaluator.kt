package io.heapy.krogu.math

internal fun evaluateAdditionOracle(v: Array<String>): String {
    val op = v[1].split(':')
    val trace = StringBuilder()
    val precision = v[4].toInt()
    val mode = RoundingMode.valueOf(v[5])
    fun outcome(block: () -> Any): String = try { block().toString() }
        catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
    if (op[0] == "addSign") {
        fun operand(text: String) = object : BigDecimal(text) {
            override fun signum(): Int { trace.append("sign;"); return op[1].toInt() }
        }
        val result = outcome { operand(v[2]).add(operand(v[3])) }
        return "$result|$trace"
    }
    if (op[0] == "wideAdd") {
        val left = v[2].split('@'); val right = v[3].split('@')
        val value = object : BigDecimal(BigInteger(left[0]), left[1].toInt()) {
            override fun add(augend: BigDecimal): BigDecimal { trace.append("add;"); throw IllegalStateException("exact add callback") }
        }
        val result = outcome { value.add(BigDecimal(BigInteger(right[0]), right[1].toInt()), MathContext(precision, mode)) }
        return "$result|$trace"
    }
    check(op[0] == "subtract")
    val leftBehavior = op[1]; val rightBehavior = op[2]
    val value = object : BigDecimal(v[2]) {
        override fun add(augend: BigDecimal): BigDecimal {
            trace.append("add;")
            return when (leftBehavior) { "replace" -> BigDecimal("42"); "throw" -> throw IllegalStateException("add callback"); else -> super.add(augend) }
        }
        override fun add(augend: BigDecimal, mc: MathContext): BigDecimal {
            trace.append("addContext;")
            return when (leftBehavior) { "replace" -> BigDecimal("43"); "throw" -> throw IllegalStateException("add context callback"); else -> super.add(augend, mc) }
        }
        override fun subtract(subtrahend: BigDecimal): BigDecimal { trace.append("subtract;"); return super.subtract(subtrahend) }
    }
    val other = object : BigDecimal(v[3]) {
        override fun negate(): BigDecimal {
            trace.append("rightNegate;")
            return when (rightBehavior) { "replace" -> BigDecimal("99"); "throw" -> throw IllegalStateException("negate callback"); else -> super.negate() }
        }
    }
    val result = outcome { if (precision < 0) value.subtract(other) else value.subtract(other, MathContext(precision, mode)) }
    return "$result|$trace"
}

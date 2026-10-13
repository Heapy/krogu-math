package io.heapy.krogu.math

internal fun evaluateUnaryOracle(v: Array<String>): String {
    val operation = v[1].substringBefore(':')
    val argument = v[1].substringAfter(':')
    val trace = StringBuilder()
    val precision = v[4].toInt()
    val mode = RoundingMode.valueOf(v[5])
    fun outcome(block: () -> String): String = try { block() }
        catch (e: Exception) { "!${e::class.simpleName}" + if (e is NullPointerException) "" else ":${e.message}" }
    if (operation == "returnedNegate") {
        val returned = object : BigDecimal(v[3]) {
            override fun plus(mc: MathContext?): BigDecimal {
                trace.append("plus;")
                return when (argument) { "replace" -> BigDecimal("42.00"); "throw" -> throw IllegalStateException("plus callback"); else -> super.plus(mc) }
            }
            override fun round(mc: MathContext?): BigDecimal { trace.append("round;"); return BigDecimal("99") }
        }
        val value = object : BigDecimal(v[2]) { override fun negate(): BigDecimal = returned }
        val result = outcome { value.negate(if (precision < 0) null else MathContext(precision, mode)).toString() }
        return "$result|$trace"
    }
    if (operation == "nullAbs" || operation == "nullNegate") {
        val sign = v[3].toInt()
        val value = object : BigDecimal(v[2]) {
            override fun signum(): Int { trace.append("sign;"); return sign }
            override fun negate(): BigDecimal { trace.append("negate;"); return super.negate() }
            override fun plus(mc: MathContext?): BigDecimal {
                trace.append("plus:").append(mc == null).append(';')
                return when (argument) { "return" -> BigDecimal("42.00"); "self" -> this; "throw" -> throw IllegalStateException("context callback"); else -> super.plus(mc) }
            }
            override fun negate(mc: MathContext?): BigDecimal {
                trace.append("negateContext:").append(mc == null).append(';')
                return when (argument) { "return" -> BigDecimal("42.00"); "self" -> this; "throw" -> throw IllegalStateException("context callback"); else -> super.negate(mc) }
            }
        }
        val result = outcome { (if (operation == "nullAbs") value.abs(null) else value.negate(null)).toString() }
        return "$result|$trace"
    }
    val sign = argument.toInt()
    val value = object : BigDecimal(v[2]) {
        override fun signum(): Int { trace.append("sign;"); return sign }
        override fun abs(): BigDecimal { trace.append("abs;"); return super.abs() }
        override fun negate(): BigDecimal { trace.append("negate;"); return super.negate() }
        override fun negate(mc: MathContext?): BigDecimal { trace.append("negateContext;"); return super.negate(mc) }
        override fun plus(): BigDecimal { trace.append("plus;"); return super.plus() }
        override fun plus(mc: MathContext?): BigDecimal { trace.append("plusContext;"); return super.plus(mc) }
        override fun round(mc: MathContext?): BigDecimal { trace.append("round;"); return super.round(mc) }
    }
    val result = outcome {
        val context = MathContext(precision, mode)
        val r = when (operation) {
            "abs" -> value.abs()
            "absContext" -> value.abs(context)
            "negate" -> value.negate()
            "negateContext" -> value.negate(context)
            "plus" -> value.plus()
            "plusContext" -> value.plus(context)
            else -> error("Unknown unary operation $operation")
        }
        "${BigDecimal(r.unscaledValue(), r.scale())}|${r === value}|${r::class == BigDecimal::class}"
    }
    return "$result|$trace"
}

package io.heapy.krogu.math

/** Platform-independent shortest round-trip representation with Java's decimal notation thresholds. */
internal fun decimalDoubleString(value: Double): String {
    if (!value.isFinite()) throw NumberFormatException("Infinite or NaN")
    if (value == 0.0) return "0.0"
    val exact = BigDecimal(value)
    // The shortest decimal inside the rounding interval. Search at least two significant
    // digits, as required by Double.toString for small subnormals (e.g. 4.9E-324).
    var candidate = exact
    for (digits in 2..17) {
        val rounded = exact.round(MathContext(digits, RoundingMode.HALF_EVEN))
        if (rounded.toDouble().toBits() == value.toBits()) { candidate = rounded; break }
    }
    val stripped = candidate.stripTrailingZeros()
    val exponent = stripped.precision().toLong() - stripped.scale() - 1
    if (exponent in -3..6) {
        val plain = stripped.toPlainString()
        return if ('.' in plain) plain else "$plain.0"
    }
    val digits = stripped.unscaledValue().abs().toString()
    val mantissa = digits[0] + "." + (if (digits.length == 1) "0" else digits.substring(1))
    return (if (value < 0) "-" else "") + mantissa + "E" + exponent
}

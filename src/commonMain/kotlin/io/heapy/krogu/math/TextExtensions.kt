// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

fun String.toBigInteger(): BigInteger = BigInteger(this)
fun String.toBigInteger(radix: Int): BigInteger {
    require(radix in 2..36) { "radix $radix was not in valid range 2..36" }
    return BigInteger(this, radix)
}

fun String.toBigIntegerOrNull(): BigInteger? = toBigIntegerOrNull(10)
fun String.toBigIntegerOrNull(radix: Int): BigInteger? {
    // Invalid configuration remains an error even for malformed or empty input.
    require(radix in 2..36) { "radix $radix was not in valid range 2..36" }
    return try { BigInteger(this, radix) } catch (_: NumberFormatException) { null }
}

fun String.toBigDecimal(): BigDecimal = BigDecimal(this)
fun String.toBigDecimal(mathContext: MathContext): BigDecimal = BigDecimal(this, mathContext)
fun String.toBigDecimalOrNull(): BigDecimal? =
    try { BigDecimal(this) } catch (_: NumberFormatException) { null }
fun String.toBigDecimalOrNull(mathContext: MathContext): BigDecimal? =
    // Valid input requiring forbidden rounding is an arithmetic error, not a parse failure.
    try { BigDecimal(this, mathContext) } catch (_: NumberFormatException) { null }

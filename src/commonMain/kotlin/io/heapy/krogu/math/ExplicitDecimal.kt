// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.jvm.JvmStatic

/** Explicit decimal construction policies. Existing constructors remain available. */
object Decimals {
    /** The exact decimal value of the supplied finite IEEE 754 binary64 number. */
    @JvmStatic fun fromBinary(value: Double): BigDecimal = BigDecimal(value)

    /** The exact binary value rounded once according to [context]. */
    @JvmStatic fun fromBinary(value: Double, context: MathContext): BigDecimal = BigDecimal(value, context)

    /** The canonical short decimal representation, including its documented scale. */
    @JvmStatic fun fromCanonical(value: Double): BigDecimal = BigDecimal.valueOf(value)

    /** Canonical decimal text is constructed first, then rounded according to [context]. */
    @JvmStatic fun fromCanonical(value: Double, context: MathContext): BigDecimal {
        val canonical = BigDecimal.valueOf(value)
        return BigDecimal(canonical.unscaledValue(), canonical.scale(), context)
    }

    /** Null means malformed decimal text; this does not catch arithmetic failures. */
    @JvmStatic fun parseOrNull(text: String): BigDecimal? = text.toBigDecimalOrNull()

    /** Valid text requiring forbidden rounding still throws ArithmeticException. */
    @JvmStatic fun parseOrNull(text: String, context: MathContext): BigDecimal? = text.toBigDecimalOrNull(context)
}

/** Exact division: a nonterminating decimal expansion throws ArithmeticException. */
fun BigDecimal.divideExact(divisor: BigDecimal): BigDecimal = divide(divisor)

/** Round to significant decimal digits; zero precision explicitly means unlimited. */
fun BigDecimal.roundToPrecision(precision: Int, mode: RoundingMode): BigDecimal = round(MathContext(precision, mode))

/** Round to the requested fractional-digit scale; negative scales are supported. */
fun BigDecimal.roundToScale(scale: Int, mode: RoundingMode): BigDecimal = setScale(scale, mode)

/** A named immutable view of the public quotient/remainder pair. */
data class QuotientRemainder<T>(val quotient: T, val remainder: T)

fun BigInteger.quotientAndRemainder(divisor: BigInteger): QuotientRemainder<BigInteger> =
    divideAndRemainder(divisor).let { QuotientRemainder(it[0], it[1]) }

fun BigDecimal.quotientAndRemainder(divisor: BigDecimal): QuotientRemainder<BigDecimal> =
    divideAndRemainder(divisor).let { QuotientRemainder(it[0], it[1]) }

fun BigDecimal.quotientAndRemainder(divisor: BigDecimal, context: MathContext): QuotientRemainder<BigDecimal> =
    divideAndRemainder(divisor, context).let { QuotientRemainder(it[0], it[1]) }

/**
 * The outcome of an exact conversion. Failure retains the arithmetic exception
 * without guessing its category from a runtime-specific message. Other failures
 * (including invalid arguments or resource exhaustion) are not intercepted.
 */
sealed interface ExactConversion<out T> {
    data class Value<T>(val value: T) : ExactConversion<T>
    data class Failure(val cause: ArithmeticException) : ExactConversion<Nothing>
}

private inline fun <T> exactConversion(operation: () -> T): ExactConversion<T> =
    try { ExactConversion.Value(operation()) }
    catch (failure: ArithmeticException) { ExactConversion.Failure(failure) }

fun BigDecimal.toBigIntegerExactResult(): ExactConversion<BigInteger> = exactConversion { toBigIntegerExact() }
fun BigDecimal.toLongExactResult(): ExactConversion<Long> = exactConversion { longValueExact() }
fun BigDecimal.toIntExactResult(): ExactConversion<Int> = exactConversion { intValueExact() }
fun BigInteger.toLongExactResult(): ExactConversion<Long> = exactConversion { longValueExact() }
fun BigInteger.toIntExactResult(): ExactConversion<Int> = exactConversion { intValueExact() }

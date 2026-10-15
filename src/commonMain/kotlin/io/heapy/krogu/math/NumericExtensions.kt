// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Kotlin conversion counterparts; use explicit imports to select krogu types on JVM. */
fun Int.toBigInteger(): BigInteger = BigInteger.valueOf(toLong())
fun Long.toBigInteger(): BigInteger = BigInteger.valueOf(this)
fun UInt.toBigInteger(): BigInteger = BigInteger.valueOf(toLong())
fun ULong.toBigInteger(): BigInteger = BigInteger(toString())

fun Int.toBigDecimal(): BigDecimal = BigDecimal(this)
fun Int.toBigDecimal(mathContext: MathContext): BigDecimal = BigDecimal(this, mathContext)
fun Long.toBigDecimal(): BigDecimal = BigDecimal(this)
fun Long.toBigDecimal(mathContext: MathContext): BigDecimal = BigDecimal(this, mathContext)

fun BigInteger.toBigDecimal(): BigDecimal = BigDecimal(this)
fun BigInteger.toBigDecimal(scale: Int = 0, mathContext: MathContext = MathContext.UNLIMITED): BigDecimal =
    BigDecimal(this, scale, mathContext)

/** Converts canonical decimal text, rather than the exact binary constructor value. */
fun Float.toBigDecimal(): BigDecimal = BigDecimal(decimalFloatString(this))
fun Float.toBigDecimal(mathContext: MathContext): BigDecimal = BigDecimal(decimalFloatString(this), mathContext)

/** Uses the existing canonical binary64 factory, preserving its coefficient and scale. */
fun Double.toBigDecimal(): BigDecimal = BigDecimal.valueOf(this)
fun Double.toBigDecimal(mathContext: MathContext): BigDecimal = BigDecimal.valueOf(this).round(mathContext)

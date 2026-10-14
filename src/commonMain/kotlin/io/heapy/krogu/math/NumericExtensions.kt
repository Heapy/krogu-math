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

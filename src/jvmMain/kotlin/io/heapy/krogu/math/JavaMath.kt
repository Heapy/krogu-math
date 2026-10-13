@file:JvmName("JavaMath")

package io.heapy.krogu.math

import kotlin.random.asKotlinRandom

/** Uses the supplied java.util.Random without substituting a different random generator. */
@JvmName("randomInteger")
fun BigInteger(numBits: Int, rnd: java.util.Random): BigInteger = BigInteger(numBits, rnd.asKotlinRandom())
@JvmName("randomPrime")
fun BigInteger(bitLength: Int, certainty: Int, rnd: java.util.Random): BigInteger = BigInteger(bitLength, certainty, rnd.asKotlinRandom())
fun BigInteger.Companion.probablePrime(bitLength: Int, rnd: java.util.Random): BigInteger = probablePrime(bitLength, rnd.asKotlinRandom())
fun probablePrime(bitLength: Int, rnd: java.util.Random): BigInteger = BigInteger.probablePrime(bitLength, rnd.asKotlinRandom())

fun BigInteger.toJavaBigInteger(): java.math.BigInteger = java.math.BigInteger(toByteArray())
fun java.math.BigInteger.toKroguBigInteger(): BigInteger = BigInteger(toByteArray())
fun BigDecimal.toJavaBigDecimal(): java.math.BigDecimal = java.math.BigDecimal(unscaledValue().toJavaBigInteger(), scale())
fun java.math.BigDecimal.toKroguBigDecimal(): BigDecimal = BigDecimal(unscaledValue().toKroguBigInteger(), scale())
fun MathContext.toJavaMathContext(): java.math.MathContext = java.math.MathContext(precision, java.math.RoundingMode.valueOf(roundingMode.name))
fun java.math.MathContext.toKroguMathContext(): MathContext = MathContext(precision, RoundingMode.valueOf(roundingMode.name))

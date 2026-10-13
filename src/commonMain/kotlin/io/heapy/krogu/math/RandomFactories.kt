package io.heapy.krogu.math

import kotlin.random.Random

/** Kotlin Random counterparts to the Java-compatible constructors.
 * Factory functions retain constructor call syntax without adding ambiguous Java overloads.
 */
@kotlin.jvm.JvmSynthetic
fun BigInteger(numBits: Int, rnd: Random): BigInteger = BigInteger.randomValue(numBits, rnd)

@kotlin.jvm.JvmSynthetic
fun BigInteger(bitLength: Int, certainty: Int, rnd: Random): BigInteger = BigInteger.primeValue(bitLength, certainty, rnd)

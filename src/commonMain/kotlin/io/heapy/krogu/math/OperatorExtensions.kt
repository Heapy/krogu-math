// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Kotlin operator counterparts; import these extensions when migrating from java.math. */
operator fun BigInteger.inc(): BigInteger = add(BigInteger.ONE)
operator fun BigInteger.dec(): BigInteger = subtract(BigInteger.ONE)
operator fun BigDecimal.inc(): BigDecimal = add(BigDecimal.ONE)
operator fun BigDecimal.dec(): BigDecimal = subtract(BigDecimal.ONE)

infix fun BigInteger.shl(n: Int): BigInteger = shiftLeft(n)
infix fun BigInteger.shr(n: Int): BigInteger = shiftRight(n)
fun BigInteger.inv(): BigInteger = not()

// Keep stdlib-compatible named arguments and alias imports alongside the virtual members.
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
infix fun BigInteger.and(other: BigInteger): BigInteger = and(value = other)
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
infix fun BigInteger.or(other: BigInteger): BigInteger = or(value = other)
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
infix fun BigInteger.xor(other: BigInteger): BigInteger = xor(value = other)

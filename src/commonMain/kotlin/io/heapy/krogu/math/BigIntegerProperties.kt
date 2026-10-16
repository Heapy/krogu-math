// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Kotlin property counterpart of the Java getter; import this property when migrating. */
val BigInteger.lowestSetBit: Int
    get() = getLowestSetBit()

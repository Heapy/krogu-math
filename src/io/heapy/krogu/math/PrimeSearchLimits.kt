// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Own prime-generation/search limit; not an arithmetic or primality-testing limit. */
internal object PrimeSearchLimits {
    const val MAX_BITS = 500_000_000

    fun checkBitLength(bitLength: Int) {
        if (bitLength > MAX_BITS) throw ArithmeticException("Probable prime size exceeds supported range")
    }
}

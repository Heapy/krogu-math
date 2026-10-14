// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Round budgets under independent uniform candidate and witness sampling. */
internal object PrimalityCertainty {
    fun testRounds(certainty: Int): Int = rounds(certainty.toLong())

    fun generationRounds(bitLength: Int, certainty: Int): Int {
        if (certainty <= 0) return 0
        // Among odd b-bit candidates the prime fraction exceeds 1/b.
        // Compensate for conditioning on acceptance, not just one composite test.
        // See NUMERICAL_ARGUMENTS.md, "Primality and prime generation".
        val selectionBits = Int.SIZE_BITS - (bitLength - 1).countLeadingZeroBits()
        return rounds(certainty.toLong() + selectionBits)
    }

    // For one-based candidate index i, bitLength(i)=ceil(log2(i+1)).
    // Summing 2^(-2*bitLength(i)) over every i>=1 gives 1/2.
    // Long also represents budgets when the index itself has Int.MAX_VALUE bits.
    fun successorRounds(candidateIndexBits: Int): Long = 50L + candidateIndexBits

    private fun rounds(certainty: Long): Int =
        if (certainty <= 0) 0 else ((certainty + 1) / 2).toInt()
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Symbolic limits and bounded public controls; no giant magnitude is allocated. */
class PrimeSearchRange559Test {
    @Test fun symbolicPrimeSizeBoundaryRetainsTheRequiredBitLengths() {
        for (bits in listOf(0, 1, 2, 499_999_999, 500_000_000))
            PrimeSearchLimits.checkBitLength(bits)
        for (bits in listOf(500_000_001, 671_088_639, Int.MAX_VALUE))
            assertFailsWith<ArithmeticException> { PrimeSearchLimits.checkBitLength(bits) }
    }

    @Test fun oversizedGenerationRejectsEvenNonpositiveCertaintyWithoutHugeAllocation() {
        val random = Random(559)
        for (bits in listOf(500_000_001, 671_088_639, Int.MAX_VALUE)) {
            for (certainty in listOf(Int.MIN_VALUE, 0, 1, 100))
                assertFailsWith<ArithmeticException> { BigInteger(bits, certainty, random) }
            assertFailsWith<ArithmeticException> { BigInteger.probablePrime(bits, random) }
        }
    }

    @Test fun successorRetainsSmallResultsNegativeRejectionAndTruthfulSubclass() {
        for ((source, next) in listOf(0L to 2L, 1L to 2L, 2L to 3L,
            3L to 5L, 4L to 5L, 17L to 19L, 18L to 19L, 19L to 23L)) {
            assertEquals(BigInteger.valueOf(next), BigInteger.valueOf(source).nextProbablePrime())
        }
        for (source in listOf(-1L, -17L))
            assertFailsWith<ArithmeticException> { BigInteger.valueOf(source).nextProbablePrime() }
        val stored = object : BigInteger("17") {
            override fun bitLength(): Int = super.bitLength()
        }
        assertEquals(BigInteger.valueOf(19), stored.nextProbablePrime())
    }
}

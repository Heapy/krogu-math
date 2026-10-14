// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrimalityCertaintyTest {
    @Test fun successorBudgetsBoundTheWholeSearchAndExceedIntSafely() {
        // Sum the first 16 complete groups with a common exact denominator.
        var scaledError = 0L
        for (index in 1..65535) {
            val bits = BigInteger.valueOf(index.toLong()).bitLength()
            val rounds = PrimalityCertainty.successorRounds(bits)
            val extra = 2 * rounds - 100
            assertTrue(extra in 2L..32L)
            scaledError += 1L shl (32 - extra.toInt())
        }
        assertEquals((1L shl 31) - (1L shl 15), scaledError)
        assertTrue(scaledError < (1L shl 31))
        for (bits in listOf(1, 2, 31, 32, 63, 64, 100, Int.MAX_VALUE)) {
            val rounds = PrimalityCertainty.successorRounds(bits)
            assertEquals(100L + 2L * bits, 2 * rounds)
        }
        assertEquals(2_147_483_697L, PrimalityCertainty.successorRounds(Int.MAX_VALUE))
    }

    @Test fun boundedSuccessorsNeverSkipATrialDivisionPrime() {
        fun prime(n: Int) = n >= 2 && (2..n / 2).none { n % it == 0 }
        var next = 2
        for (n in 0..4096) {
            if (next <= n) {
                next = n + 1
                while (!prime(next)) next++
            }
            assertEquals(BigInteger.valueOf(next.toLong()), BigInteger.valueOf(n.toLong()).nextProbablePrime(), "n=$n")
        }
    }

    @Test fun generationBudgetCoversConditioningOnAcceptanceWithoutIntOverflow() {
        val lengths = (2..4096).toList() + (13..30).flatMap {
            listOf((1 shl it) - 1, 1 shl it, (1 shl it) + 1)
        } + Int.MAX_VALUE
        for (bits in lengths) for (certainty in listOf(1, 2, 3, 31, 100, Int.MAX_VALUE)) {
            val rounds = PrimalityCertainty.generationRounds(bits, certainty)
            val extra = 2L * rounds - certainty
            assertTrue(extra in 0L..32L, "b=$bits c=$certainty rounds=$rounds")
            // A candidate's false-acceptance bound, multiplied by b, must
            // not exceed the requested returned-composite bound.
            assertTrue((1L shl extra.toInt()) >= bits.toLong(), "b=$bits c=$certainty")
        }
        for (certainty in listOf(Int.MIN_VALUE, -1, 0)) {
            assertEquals(0, PrimalityCertainty.testRounds(certainty))
            assertEquals(0, PrimalityCertainty.generationRounds(Int.MAX_VALUE, certainty))
        }
        assertEquals(1_073_741_824, PrimalityCertainty.testRounds(Int.MAX_VALUE))
        assertEquals(1_073_741_839, PrimalityCertainty.generationRounds(Int.MAX_VALUE, Int.MAX_VALUE))
    }

    @Test fun boundedDecisionsAgreeWithIndependentTrialDivision() {
        for (n in 0..4096) {
            val prime = n >= 2 && (2..n / 2).none { n % it == 0 }
            for (signed in listOf(n, -n)) {
                val value = BigInteger.valueOf(signed.toLong())
                assertEquals(prime, value.isProbablePrime(1), "n=$signed")
                assertTrue(value.isProbablePrime(0), "n=$signed c=0")
            }
        }
        assertFalse(BigInteger("3317044064679887385961981").isProbablePrime(100))
    }
}

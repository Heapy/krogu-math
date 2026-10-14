// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrimalityCertaintyTest {
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

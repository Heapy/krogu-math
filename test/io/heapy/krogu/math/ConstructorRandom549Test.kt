// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Common constructor-call and isolated invalid-bit contract controls. */
class ConstructorRandom549Test {
    private class SmallPrimeRandom : Random() {
        override fun nextBits(bitCount: Int): Int =
            if (bitCount == 0) 0 else -1 ushr (32 - bitCount)

        override fun nextBytes(array: ByteArray): ByteArray {
            // 0xFB is 251: a prime with exactly eight bits.
            array.fill(0xFB.toByte())
            return array
        }
    }

    private fun assertPositiveAtBits(value: BigInteger, bits: Int) {
        assertTrue(value.signum() > 0)
        assertEquals(bits, value.bitLength())
    }

    @Test fun certaintyFactoryReturnsPositiveSmallPrimes() {
        for (bits in listOf(2, 8)) for (certainty in listOf(1, 32, 100)) {
            val value = BigInteger(bits, certainty, SmallPrimeRandom())
            assertPositiveAtBits(value, bits)
            // Independent bounded trial division; no production primality oracle.
            val small = value.intValueExact()
            var divisor = 2
            while (divisor * divisor <= small) {
                assertTrue(small % divisor != 0, "$small divisible by $divisor")
                divisor++
            }
        }
    }

    @Test fun nonpositiveCertaintyStillReturnsPositiveRequestedBitLength() {
        for (bits in listOf(2, 8)) for (certainty in listOf(Int.MIN_VALUE, -1, 0)) {
            val value = BigInteger(bits, certainty, SmallPrimeRandom())
            assertPositiveAtBits(value, bits)
        }
    }

    @Test fun nonnullRandomUsesRequiredInvalidBitExceptionCategories() {
        for (bits in listOf(Int.MIN_VALUE, -1)) {
            assertFailsWith<IllegalArgumentException> { BigInteger(bits, SmallPrimeRandom()) }
        }
        for (bits in listOf(Int.MIN_VALUE, -1, 0, 1)) for (certainty in listOf(0, 1)) {
            assertFailsWith<ArithmeticException> { BigInteger(bits, certainty, SmallPrimeRandom()) }
        }
    }
}

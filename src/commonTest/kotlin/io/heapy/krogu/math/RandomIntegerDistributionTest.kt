// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RandomIntegerDistributionTest {
    @Test fun zeroBitsReturnsZeroAndRecordsRandomConsumption() {
        var draws = 0
        var emptyByteRequests = 0
        val counted = object : Random() {
            override fun nextBits(bitCount: Int): Int { draws++; return 0 }
            override fun nextBytes(array: ByteArray): ByteArray {
                if (array.isEmpty()) emptyByteRequests++ else draws++
                return array
            }
        }
        assertEquals(BigInteger.ZERO, BigInteger(0, counted))
        println("578_ZERO_RANDOM_OBSERVATION\tpositiveRequests=$draws\temptyByteRequests=$emptyByteRequests")
        val afterZero = Random(42)
        val untouched = Random(42)
        assertEquals(BigInteger.ZERO, BigInteger(0, afterZero))
        val untouchedNext = BigInteger(128, untouched)
        val afterZeroNext = BigInteger(128, afterZero)
        assertTrue(untouchedNext.signum() >= 0 && untouchedNext.bitLength() <= 128)
        assertTrue(afterZeroNext.signum() >= 0 && afterZeroNext.bitLength() <= 128)
        println("578_SEEDED_RANDOM_OBSERVATION\tuntouched=$untouchedNext\tafterZero=$afterZeroNext")
    }

    private class PayloadRandom(var payload: Int) : Random() {
        override fun nextBits(bitCount: Int): Int = error("Expected byte sampling")
        override fun nextBytes(array: ByteArray): ByteArray {
            for (i in array.indices) array[i] = (payload ushr (8 * (array.lastIndex - i))).toByte()
            return array
        }
    }

    @Test fun everySmallBytePayloadHasEqualMultiplicityInTheRequestedRange() {
        for (bits in (1..8).toList() + listOf(9, 16)) {
            val payloadCount = if (bits <= 8) 256 else 65536
            val counts = IntArray(1 shl bits)
            val random = PayloadRandom(0)
            for (payload in 0 until payloadCount) {
                random.payload = payload
                val value = BigInteger(bits, random)
                assertTrue(value.signum() >= 0 && value.bitLength() <= bits)
                counts[value.intValueExact()]++
            }
            val expectedMultiplicity = payloadCount / counts.size
            for (value in counts.indices)
                assertEquals(expectedMultiplicity, counts[value], "bits=$bits value=$value")
        }
    }
}

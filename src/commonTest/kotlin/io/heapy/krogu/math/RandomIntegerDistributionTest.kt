// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RandomIntegerDistributionTest {
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

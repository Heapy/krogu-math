// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.math.BigInteger as HostInteger
import java.util.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

/** Public host API only, with no foreign implementation source or production test hooks. */
class OddModulusContext538JvmTest {
    private fun compare(a: HostInteger, e: HostInteger, m: HostInteger) {
        val source = BigInteger(a.toByteArray())
        val power = BigInteger(e.toByteArray())
        val modulus = BigInteger(m.toByteArray())
        val snapshot = source.toByteArray()
        if (e.signum() < 0 && a.gcd(m) != HostInteger.ONE) {
            assertFailsWith<ArithmeticException> { a.modPow(e, m) }
            assertFailsWith<ArithmeticException> { source.modPow(power, modulus) }
        } else {
            val expected = a.modPow(e, m)
            val actual = source.modPow(power, modulus)
            assertEquals(expected.toString(), actual.toString(), "a=$a e=$e m=$m")
            assertTrue(expected.toByteArray().contentEquals(actual.toByteArray()))
            assertTrue(actual >= BigInteger.ZERO && actual < modulus)
            // Later calls with other moduli must not overwrite an earlier exported result.
            source.modPow(BigInteger("7"), BigInteger("1073741825"))
            assertEquals(expected.toString(), actual.toString())
            if (e > HostInteger.ONE && a.signum() != 0 && a != HostInteger.ONE &&
                a != HostInteger.ONE.negate() && m > HostInteger.ONE && m.testBit(0) &&
                a.mod(m).signum() != 0) {
                assertNotSame(BigInteger.ZERO, actual)
                assertNotSame(BigInteger.ONE, actual)
            }
        }
        assertTrue(snapshot.contentEquals(source.toByteArray()))
    }

    @Test fun boundedPublicDifferentialMatrix() {
        val random = Random(538L)
        for (bits in listOf(2, 7, 29, 30, 31, 59, 60, 61, 90, 127, 256, 512)) {
            repeat(3) {
                val odd = HostInteger(bits, random).setBit(bits - 1).setBit(0)
                val moduli = listOf(odd, odd.shiftLeft(3), HostInteger.ONE.shiftLeft(bits))
                for (m in moduli) {
                    val magnitude = HostInteger(bits + 17, random)
                    val a = if (it and 1 == 0) magnitude else magnitude.negate()
                    for (e in listOf(-5L, -1L, 0L, 1L, 2L, 3L, 17L)) {
                        compare(a, HostInteger.valueOf(e), m)
                    }
                }
            }
        }
    }

    @Test fun radixInverseAndConversionPatterns() {
        // Different odd low words exercise inverse lifting without inspecting private state.
        // Dense top/low words and small high words cover padding and normalization on export.
        val radix = HostInteger.ONE.shiftLeft(30)
        val lowWords = listOf(1L, 3L, 5L, 7L, 536870911L, 1073741821L, 1073741823L)
        for (n in listOf(1, 2, 3, 5)) {
            val high = radix.pow(n - 1)
            for (low in lowWords) {
                val m = if (n == 1) HostInteger.valueOf(low).max(HostInteger.valueOf(3))
                    else high.multiply(radix.subtract(HostInteger.ONE)).add(HostInteger.valueOf(low))
                for (a in listOf(HostInteger.ZERO, HostInteger.ONE, HostInteger.TWO,
                    m.subtract(HostInteger.ONE), m.subtract(HostInteger.TWO), m, m.add(HostInteger.ONE))) {
                    compare(a, HostInteger.valueOf(3), m)
                    compare(a, HostInteger.valueOf(10), m)
                }
            }
        }
    }

    @Test fun sparseAndDenseStoredExponentWords() {
        val a = HostInteger("123456789012345678901234567890")
        val m = HostInteger.ONE.shiftLeft(127).subtract(HostInteger.ONE)
        for (bit in listOf(29, 30, 31, 59, 60, 61, 90)) {
            val sparse = HostInteger.ONE.shiftLeft(bit)
            val dense = sparse.subtract(HostInteger.ONE)
            compare(a, sparse, m)
            compare(a, dense, m)
            compare(a, dense.negate(), m)
        }
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.math.BigDecimal as HostDecimal
import java.math.BigInteger as HostInteger
import java.math.MathContext as HostContext
import java.math.RoundingMode as HostRounding
import java.util.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Prepared for a later runtime grant; public API differential tests, no foreign source bodies. */
class Division530JvmTest {
    private fun requirePinnedJdk() {
        val runtime = System.getProperty("java.runtime.version")
        assertTrue(runtime == "25.0.4+9" || runtime.startsWith("25.0.4+9-"), "required runtime 25.0.4+9: $runtime")
        assertTrue(System.getProperty("java.vendor").contains("BellSoft"), "required Liberica vendor")
    }

    private fun compare(a: HostInteger, b: HostInteger) {
        val left = BigInteger(a.toByteArray())
        val right = BigInteger(b.toByteArray())
        val expected = a.divideAndRemainder(b)
        val actual = left.divideAndRemainder(right)
        for (i in 0..1) assertTrue(expected[i].toByteArray().contentEquals(actual[i].toByteArray()),
            "pair[$i] dividendBits=${a.abs().bitLength()} divisorBits=${b.abs().bitLength()}")
        assertTrue(expected[0].toByteArray().contentEquals(left.divide(right).toByteArray()))
        assertTrue(expected[1].toByteArray().contentEquals(left.remainder(right).toByteArray()))
        assertEquals(left, actual[0].multiply(right).add(actual[1]))
        assertTrue(actual[1].abs() < right.abs())
        assertTrue(a.toByteArray().contentEquals(left.toByteArray()))
        assertTrue(b.toByteArray().contentEquals(right.toByteArray()))
        val savedQ = actual[0].toByteArray()
        val savedR = actual[1].toByteArray()
        left.add(BigInteger.ONE).divideAndRemainder(right.add(BigInteger.TWO).abs().add(BigInteger.ONE))
        assertTrue(savedQ.contentEquals(actual[0].toByteArray()))
        assertTrue(savedR.contentEquals(actual[1].toByteArray()))
    }

    @Test fun boundedPinnedJdkDivisionMatrix() {
        requirePinnedJdk()
        val random = Random(530L)
        for (bits in listOf(1, 2, 29, 30, 31, 59, 60, 61, 90, 127, 256, 1024, 8000)) {
            for (divisorBits in listOf(1, 29, 30, 31, maxOf(1, bits / 2), bits, bits + 31).distinct()) {
                repeat(2) {
                    val a = HostInteger(bits, random).setBit(bits - 1)
                    val b = HostInteger(divisorBits, random).setBit(divisorBits - 1)
                    for (negativeA in listOf(false, true)) for (negativeB in listOf(false, true))
                        compare(if (negativeA) a.negate() else a, if (negativeB) b.negate() else b)
                    compare(HostInteger.ZERO, b)
                    compare(a, a)
                }
            }
        }
    }

    @Test fun denseSparseAndNormalizationBoundaries() {
        requirePinnedJdk()
        val one = HostInteger.ONE
        val radix = one.shiftLeft(30)
        for (shift in 0..29) {
            val b = one.shiftLeft(59 - shift).add(radix.subtract(one))
            for (q in listOf(one, radix.subtract(one), one.shiftLeft(180).subtract(one))) {
                for (r in listOf(HostInteger.ZERO, one, b.subtract(one))) {
                    val a = q.multiply(b).add(r)
                    compare(a, b)
                    compare(a.negate(), b)
                    compare(a, b.negate())
                }
            }
        }
        val b = one.shiftLeft(89).add(one)
        compare(one.shiftLeft(90).add(one), b) // Single add-back witness.
        val half = one.shiftLeft(29)
        val twoDigitDivisor = half.multiply(radix).add(radix.subtract(one))
        compare(half.subtract(one).multiply(radix.pow(2)).add(half.multiply(radix)), twoDigitDivisor)
        compare(twoDigitDivisor.multiply(radix).subtract(one), twoDigitDivisor)
    }

    @Test fun pinnedJdkConsumerComparisons() {
        requirePinnedJdk()
        val random = Random(1530L)
        for (bits in listOf(31, 61, 127, 256, 1024)) {
            repeat(3) {
                val m = HostInteger(bits, random).setBit(bits - 1).setBit(0)
                val a = HostInteger(bits + 30, random).negate()
                val left = BigInteger(a.toByteArray())
                val modulus = BigInteger(m.toByteArray())
                assertEquals(a.gcd(m).toString(), left.gcd(modulus).toString())
                if (a.gcd(m) == HostInteger.ONE) {
                    assertEquals(a.modInverse(m).toString(), left.modInverse(modulus).toString())
                    assertEquals(BigInteger.ONE, left.modInverse(modulus).multiply(left).mod(modulus))
                } else {
                    assertFailsWith<ArithmeticException> { a.modInverse(m) }
                    assertFailsWith<ArithmeticException> { left.modInverse(modulus) }
                }
            }
        }
        val numerators = listOf("123456789012345678901234567890", "-999999999999999999999999999999")
        val denominators = listOf("100000000000000000000", "1073741825", "-1152921504606846975")
        for (n in numerators) for (d in denominators) for (precision in listOf(1, 10, 34)) {
            for (mode in listOf(RoundingMode.DOWN, RoundingMode.UP, RoundingMode.HALF_EVEN, RoundingMode.FLOOR)) {
                val expected = HostDecimal(n).divide(HostDecimal(d), HostContext(precision, HostRounding.valueOf(mode.name)))
                val actual = BigDecimal(n).divide(BigDecimal(d), MathContext(precision, mode))
                assertEquals(expected.unscaledValue().toString(), actual.unscaledValue().toString())
                assertEquals(expected.scale(), actual.scale())
            }
        }
    }
}

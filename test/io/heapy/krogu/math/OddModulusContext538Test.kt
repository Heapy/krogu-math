// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Source prepared under a no-execution grant; see the task 538 implementation handoff. */
class OddModulusContext538Test {
    private val one = BigInteger.ONE
    private val two = BigInteger.TWO

    // Independent repeated full-product/remainder oracle, bounded to small operands/exponents.
    // It neither calls modPow nor reproduces the Montgomery recurrence or exponent-bit loop.
    private fun repeatedPower(a: BigInteger, exponent: Int, m: BigInteger): BigInteger {
        var value = one.mod(m)
        repeat(exponent) { value = value.multiply(a).mod(m) }
        return value
    }

    @Test fun allSmallOddModuliAndResidues() {
        // A bounded scalar oracle independent of both BigInteger reduction and MM.
        // expected*a <=62*62, so no scalar overflow or large test work is possible.
        for (modulus in 3..63 step 2) {
            val m = BigInteger.valueOf(modulus.toLong())
            for (a in 0 until modulus) {
                for (e in listOf(2, 3, 5)) {
                    var expected = 1L
                    repeat(e) { expected = expected * a % modulus }
                    assertEquals(BigInteger.valueOf(expected),
                        BigInteger.valueOf(a.toLong()).modPow(BigInteger.valueOf(e.toLong()), m),
                        "small residue a=$a e=$e m=$modulus")
                }
            }
        }
    }

    @Test fun carryHeavyAndNearRadixPowers() {
        for (limbs in listOf(1, 2, 3, 4, 8)) {
            val radixPower = one.shiftLeft(30 * limbs)
            for (m in listOf(radixPower.subtract(one), radixPower.subtract(BigInteger("3")),
                one.shiftLeft(30 * (limbs - 1)).add(BigInteger("3")))) {
                val before = m.toByteArray()
                for (a in listOf(BigInteger.ZERO, one, two, m.subtract(one), m.subtract(two))) {
                    for (e in listOf(2, 3, 5, 11)) {
                        val result = a.modPow(BigInteger.valueOf(e.toLong()), m)
                        assertEquals(repeatedPower(a, e, m), result, "limbs=$limbs a=$a e=$e m=$m")
                        assertTrue(result >= BigInteger.ZERO && result < m)
                    }
                }
                assertTrue(before.contentEquals(m.toByteArray()), "modulus snapshot")
            }
            // m=R-1 implies R mod m=1. For A=m-1=R-2 the square's raw
            // Montgomery T=R (Q=4), forcing the significant high-cell subtraction.
            val m = radixPower.subtract(one)
            assertEquals(one, m.subtract(one).modPow(two, m))
        }
        // B=2^30, K=B/2, m=K-1. Encoding x=m-1 gives A=K-3.
        // For A*A, Q=K+9 and raw T=K+1=m+2<B: subtraction with high cell zero.
        val m = one.shiftLeft(29).subtract(one)
        assertEquals(one, m.subtract(one).modPow(two, m))
    }

    @Test fun residuesSignsEvenCrtAndInverseFailures() {
        for (m in listOf(one, two, BigInteger("4"), BigInteger("7"), BigInteger("15"),
            BigInteger("17"), BigInteger("28"), BigInteger("60"))) {
            for (a in listOf(BigInteger("-31"), BigInteger("-7"), BigInteger.ZERO, one,
                two, m.subtract(one), m, m.add(one))) {
                for (e in listOf(0, 1, 2, 3, 6)) {
                    assertEquals(repeatedPower(a, e, m), a.modPow(BigInteger.valueOf(e.toLong()), m))
                }
                for (e in listOf(-1L, -2L, -5L)) {
                    val exponent = BigInteger.valueOf(e)
                    if (a.gcd(m) == one) {
                        val result = a.modPow(exponent, m)
                        assertEquals(one.mod(m), repeatedPower(a, (-e).toInt(), m).multiply(result).mod(m))
                    } else {
                        assertFailsWith<ArithmeticException> { a.modPow(exponent, m) }
                    }
                }
            }
        }
    }

    @Test fun storedExponentWordBoundaries() {
        val m = one.shiftLeft(91).subtract(one)
        val minusOne = m.subtract(one)
        for (bit in listOf(1, 28, 29, 30, 31, 59, 60, 61)) {
            val even = one.shiftLeft(bit)
            assertEquals(one, minusOne.modPow(even, m), "even exponent bit $bit")
            assertEquals(minusOne, minusOne.modPow(even.add(one), m), "odd exponent bit $bit")
            assertEquals(minusOne, minusOne.modPow(even.add(one).negate(), m))
        }
        val exponent = object : BigInteger("1073741825") {
            override fun bitLength(): Int = error("virtual exponent length")
            override fun testBit(n: Int): Boolean = error("virtual exponent bit")
        }
        assertEquals(minusOne, minusOne.modPow(exponent, m))
    }

    @Test fun squaringAliasesMixedContextsAndDetachedSnapshots() {
        val a = BigInteger("1234567890123456789012345")
        val moduli = listOf(BigInteger("1073741823"), BigInteger("1073741825"),
            one.shiftLeft(121).subtract(one), BigInteger("34"))
        val inputSnapshot = a.toByteArray()
        val results = moduli.map { m -> a.modPow(BigInteger("13"), m) }
        val snapshots = results.map { it.toString() }
        repeat(3) {
            for ((index, m) in moduli.withIndex()) {
                assertEquals(repeatedPower(a, 13, m), a.modPow(BigInteger("13"), m))
                val bytes = results[index].toByteArray()
                bytes.fill(0)
                assertEquals(snapshots[index], results[index].toString())
            }
        }
        assertTrue(inputSnapshot.contentEquals(a.toByteArray()))
        // Public self aliases still take the original entry/normalization route.
        assertEquals(BigInteger.ZERO, a.modPow(two, a))
        assertEquals(BigInteger.ZERO, a.modPow(a, a))
    }

    @Test fun sizingCutoffFallbackHasSmallArithmeticWork() {
        // n=4097 rejects setup. Only 2*2 and comparisons with a sparse large m
        // execute on the fallback: no huge quadratic exponentiation/setup is requested.
        val overCap = one.shiftLeft(4096 * 30).add(one)
        assertEquals(BigInteger("4"), two.modPow(two, overCap))
        // The largest eligible n=4096 is a SOURCE proof obligation, deliberately
        // not executed here: the simple 60n doubling setup is quadratic in n.
        val lyingParity = object : BigInteger("18") {
            var parityCalls = 0
            override fun testBit(n: Int): Boolean { assertEquals(0, n); parityCalls++; return true }
        }
        assertEquals(BigInteger("14"), two.modPow(BigInteger("5"), lyingParity))
        assertEquals(1, lyingParity.parityCalls)
        // Prevent the public reduction so m=1 reaches the private factory check.
        val source = object : BigInteger("2") {
            override fun compareTo(other: BigInteger): Int = -1
        }
        val zero = source.modPow(two, one)
        assertEquals(BigInteger.ZERO, zero)
        assertNotSame(BigInteger.ZERO, zero)
    }

    @Test fun shortcutsCallbacksNullAndFreshComputedZero() {
        val m = BigInteger("17")
        val a = BigInteger("3")
        assertSame(one, a.modPow(BigInteger.ZERO, m))
        assertSame(a, a.modPow(one, m))
        assertSame(BigInteger.ZERO, BigInteger.ZERO.modPow(two, m))
        assertSame(BigInteger.ZERO, a.modPow(BigInteger.ZERO, one))
        val zero = BigInteger("3").modPow(two, BigInteger("9"))
        assertEquals(BigInteger.ZERO, zero)
        assertNotSame(BigInteger.ZERO, zero)
        val computedOne = BigInteger("16").modPow(two, m)
        assertEquals(one, computedOne)
        assertNotSame(one, computedOne)
        // Two null exponent entries retained in Validation578StandaloneTest (JVM).

        val trace = mutableListOf<String>()
        val source = object : BigInteger("3") {
            override fun equals(other: Any?): Boolean { trace += "equals"; return super.equals(other) }
            override fun compareTo(other: BigInteger): Int { trace += "compare"; return super.compareTo(other) }
            override fun multiply(value: BigInteger): BigInteger = error("source multiply")
            override fun bitLength(): Int = error("source length")
        }
        val exponent = object : BigInteger("2") {
            override fun equals(other: Any?): Boolean { trace += "powerEquals"; return super.equals(other) }
            override fun testBit(n: Int): Boolean = error("power bit")
        }
        val modulus = object : BigInteger("17") {
            override fun testBit(n: Int): Boolean { trace += "parity"; return super.testBit(n) }
            override fun bitLength(): Int = error("modulus length")
        }
        assertEquals(BigInteger("9"), source.modPow(exponent, modulus))
        assertEquals(listOf("equals", "equals", "equals", "compare", "parity", "powerEquals"), trace)
        val equalityReplacement = object : BigInteger("2") {
            override fun equals(other: Any?): Boolean = true
        }
        assertSame(a, a.modPow(equalityReplacement, m))
        val negativeReplacement = object : BigInteger("-3") {
            override fun negate(): BigInteger = BigInteger("-5")
        }
        assertEquals(BigInteger("8"), two.modPow(negativeReplacement, m))
        val zeroReplacement = object : BigInteger("-3") {
            override fun negate(): BigInteger = BigInteger.ZERO
        }
        assertSame(one, two.modPow(zeroReplacement, m))
        val failure = IllegalStateException("reduction callback")
        val throwing = object : BigInteger("20") {
            override fun mod(m: BigInteger): BigInteger = throw failure
        }
        assertSame(failure, assertFailsWith<IllegalStateException> { throwing.modPow(two, m) })
    }
}

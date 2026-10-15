// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Source-only regression preparation; no runtime or performance claim. */
class OddModulusContext538TinyFallbackTest {
    private val zero = BigInteger.ZERO
    private val one = BigInteger.ONE
    private val two = BigInteger.TWO

    @Test fun trivialTinySignsIdentityAndDispatch() {
        val m = BigInteger("17")
        assertSame(zero, zero.modPow(two, m))
        assertSame(one, one.modPow(two, m))
        assertSame(one, two.modPow(zero, m))
        assertSame(two, two.modPow(one, m))
        assertSame(zero, two.modPow(two, one))
        assertEquals(BigInteger("4"), two.modPow(two, m))
        assertEquals(BigInteger("4"), BigInteger("-15").modPow(two, m))
        assertEquals(BigInteger("4"), BigInteger("-2").modPow(two, m))
        assertEquals(BigInteger("13"), two.modPow(BigInteger("-2"), m))
        assertEquals(BigInteger("13"), BigInteger("-15").modPow(BigInteger("-2"), m))
        assertFailsWith<ArithmeticException> {
            BigInteger("3").modPow(BigInteger("-2"), BigInteger("99"))
        }

        // Suppress only public reduction: private reduction must still run before the guard.
        val multiple = object : BigInteger("17") {
            override fun compareTo(other: BigInteger): Int = -1
        }
        val computedZero = multiple.modPow(two, m)
        assertEquals(zero, computedZero)
        assertNotSame(zero, computedZero)
        val next = object : BigInteger("18") {
            override fun compareTo(other: BigInteger): Int = -1
        }
        val computedOne = next.modPow(two, m)
        assertEquals(one, computedOne)
        assertNotSame(one, computedOne)
        val unitModulusSource = object : BigInteger("2") {
            override fun compareTo(other: BigInteger): Int = -1
        }
        val unitZero = unitModulusSource.modPow(two, one)
        assertEquals(zero, unitZero)
        assertNotSame(zero, unitZero)

        val negativeReplacement = object : BigInteger("-3") {
            override fun negate(): BigInteger = BigInteger("-2")
        }
        assertEquals(BigInteger("13"), two.modPow(negativeReplacement, m))
        val zeroReplacement = object : BigInteger("-3") {
            override fun negate(): BigInteger = zero
        }
        assertSame(one, two.modPow(zeroReplacement, m))

        val trace = mutableListOf<String>()
        val source = object : BigInteger("2") {
            override fun equals(other: Any?): Boolean { trace += "equals"; return super.equals(other) }
            override fun compareTo(other: BigInteger): Int { trace += "compare"; return super.compareTo(other) }
            override fun multiply(value: BigInteger): BigInteger = error("source multiply")
            override fun bitLength(): Int = error("source length")
        }
        val exponent = object : BigInteger("2") {
            override fun equals(other: Any?): Boolean { trace += "powerEquals"; return super.equals(other) }
            override fun bitLength(): Int = error("power length")
            override fun testBit(n: Int): Boolean = error("power bit")
        }
        val modulus = object : BigInteger("17") {
            override fun testBit(n: Int): Boolean { trace += "parity"; return super.testBit(n) }
            override fun bitLength(): Int = error("modulus length")
        }
        assertEquals(BigInteger("4"), source.modPow(exponent, modulus))
        assertEquals(listOf("equals", "equals", "equals", "compare", "parity", "powerEquals"), trace)
        val equalityReplacement = object : BigInteger("2") {
            override fun equals(other: Any?): Boolean = true
        }
        assertSame(two, two.modPow(equalityReplacement, m))
        val lyingParity = object : BigInteger("18") {
            var calls = 0
            override fun testBit(n: Int): Boolean { assertEquals(0, n); calls++; return true }
        }
        assertEquals(BigInteger("4"), two.modPow(two, lyingParity))
        assertEquals(1, lyingParity.calls)
    }

    @Test fun sufficientBoundaryLiteralControls() {
        data class Case(val a: String, val e: String, val m: String, val expected: String)
        // Fixed small powers, with odd moduli just over/at/below the sufficient bit bound.
        // a=4,e=2: b*e=6; a=8,e=2: 8; a=5,e=3: 9. No predicate replica/oracle modPow.
        val cases = listOf(
            Case("4", "2", "33", "16"), Case("4", "2", "65", "16"), Case("4", "2", "129", "16"),
            Case("8", "2", "129", "64"), Case("8", "2", "257", "64"), Case("8", "2", "513", "64"),
            Case("5", "3", "257", "125"), Case("5", "3", "513", "125"), Case("5", "3", "1025", "125"),
            Case("5", "3", "65", "60"), Case("9", "3", "17", "15"),
            // 2^8=1 mod17; these literal exponents are 7/0/1 mod8 across the radix boundary.
            Case("2", "1073741823", "17", "9"),
            Case("2", "1073741824", "17", "1"),
            Case("2", "1073741825", "17", "2"),
        )
        for ((a, e, m, expected) in cases) {
            assertEquals(BigInteger(expected), BigInteger(a).modPow(BigInteger(e), BigInteger(m)),
                "a=$a e=$e m=$m")
        }
    }

    @Test fun upperEligibleTinyExponentsSkipSetup() {
        for (limbs in listOf(4095, 4096, 4097)) {
            // n-digit sparse odd modulus: the only fallback products are 1*2 and 2*2.
            // Upper eligible sizes must skip setup; n4097 remains the over-cap control.
            val m = one.shiftLeft(30 * (limbs - 1)).add(one)
            val snapshot = m.toByteArray()
            assertEquals(BigInteger("4"), two.modPow(two, m), "limbs=$limbs")
            assertTrue(snapshot.contentEquals(m.toByteArray()), "modulus snapshot limbs=$limbs")
        }
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Uncompiled/unrun source milestone; independent scalar and algebraic division witnesses. */
class Division530Test {
    private val zero = BigInteger.ZERO
    private val one = BigInteger.ONE
    private val radix = one.shiftLeft(30)
    private val half = 1 shl 29
    private val mask = (1 shl 30) - 1

    // Input construction only: no quotient estimation, normalization or division oracle.
    private fun digits(vararg words: Int): BigInteger {
        var value = zero
        for (i in words.indices.reversed())
            value = value.shiftLeft(30).add(BigInteger.valueOf(words[i].toLong()))
        return value
    }

    private fun check(a: BigInteger, b: BigInteger, q: BigInteger, r: BigInteger) {
        val aBefore = a.toByteArray()
        val bBefore = b.toByteArray()
        val pair = a.divideAndRemainder(b)
        assertEquals(q, pair[0], "quotient a=$a b=$b")
        assertEquals(r, pair[1], "remainder a=$a b=$b")
        assertEquals(q, a.divide(b))
        assertEquals(r, a.remainder(b))
        assertEquals(a, pair[0].multiply(b).add(pair[1]), "recomposition")
        assertTrue(pair[1].abs() < b.abs(), "strict remainder magnitude")
        assertTrue(pair[1].signum() == 0 || pair[1].signum() == a.signum())
        assertTrue(aBefore.contentEquals(a.toByteArray()), "dividend unchanged")
        assertTrue(bBefore.contentEquals(b.toByteArray()), "divisor unchanged")
    }

    private fun signed(a: BigInteger, b: BigInteger, q: BigInteger, r: BigInteger) {
        for (negativeA in listOf(false, true)) for (negativeB in listOf(false, true)) {
            check(if (negativeA) a.negate() else a, if (negativeB) b.negate() else b,
                if (negativeA != negativeB) q.negate() else q, if (negativeA) r.negate() else r)
        }
    }

    @Test fun scalarOracleAcrossLimbBoundaries() {
        val values = listOf(0L, 1L, 2L, 1073741823L, 1073741824L, 1073741825L,
            2147483647L, 1152921504606846975L, 1152921504606846976L, Long.MAX_VALUE)
        val divisors = values.drop(1)
        for (a in values) for (b in divisors) {
            signed(BigInteger.valueOf(a), BigInteger.valueOf(b),
                BigInteger.valueOf(a / b), BigInteger.valueOf(a % b))
        }
        // MIN/-1 is excluded from the scalar oracle: its quotient cannot fit Long.
        for (b in listOf(1L, 3L, 1073741824L, 1073741825L, Long.MAX_VALUE)) {
            for (d in listOf(b, -b)) {
                if (d == -1L) continue
                check(BigInteger.valueOf(Long.MIN_VALUE), BigInteger.valueOf(d),
                    BigInteger.valueOf(Long.MIN_VALUE / d), BigInteger.valueOf(Long.MIN_VALUE % d))
            }
        }
        check(BigInteger.valueOf(Long.MIN_VALUE), one.negate(), BigInteger("9223372036854775808"), zero)
    }

    @Test fun literalMultiLimbSignedDivision() {
        signed(BigInteger("123456789012345678901234567890"), BigInteger("100000000000000000000"),
            BigInteger("1234567890"), BigInteger("12345678901234567890"))
        signed(BigInteger("100000000000000000001"), BigInteger("100000000000000000000"),
            one, one)
    }

    @Test fun quotientEstimateCorrectionsCapAndAddBack() {
        val divisor = digits(mask, half)
        // B=2^30, H=B/2. U=(H-1)B^2+HB: initial qhat=B-1,
        // two leading-three-digit corrections yield q=B-3, r=4B-3.
        signed(digits(0, half, half - 1), divisor,
            radix.subtract(BigInteger("3")), radix.multiply(BigInteger("4")).subtract(BigInteger("3")))
        // U=(B/4)B^2: one correction, q=H-1, r=B+H-1.
        signed(digits(0, 0, half / 2), divisor,
            BigInteger.valueOf((half - 1).toLong()), digits(half - 1, 1))
        // U=B*V-1 has a top-two estimate B+1 at j=0; cap at B-1.
        signed(digits(mask, mask - 1, half), divisor, radix.subtract(one), divisor.subtract(one))
        // V=HB^2+1, U=B^3+1=2V-1. Leading-three-digit test accepts
        // estimate=2, but low digit forces borrow and one add-back: q=1,r=V-1.
        val threeDigitDivisor = digits(1, 0, half)
        signed(digits(1, 0, 0, 1), threeDigitDivisor, one, digits(0, 0, half))
    }

    @Test fun everyNormalizationShiftAndTopCarry() {
        for (shift in 0..29) {
            val high = 1 shl (29 - shift)
            val b = digits(mask - 2, high)
            for (q in listOf(one, radix.subtract(one), radix.add(one))) {
                for (r in listOf(zero, one, b.subtract(one))) {
                    signed(b.multiply(q).add(r), b, q, r)
                }
            }
        }
        // shift=29 and source high word=2: normalized dividend needs its extra cell.
        val b = digits(1, 1)
        signed(digits(3, 2), b, BigInteger.TWO, one)
    }

    @Test fun unbalancedDenseAndSparseOperands() {
        for (bits in listOf(61, 90, 257, 1024, 4096)) {
            val power = one.shiftLeft(bits)
            for (q in listOf(power, power.subtract(one), power.add(one))) {
                for (b in listOf(radix.add(one), digits(mask, mask, half), power.add(radix))) {
                    signed(q.multiply(b).add(b.subtract(one)), b, q, b.subtract(one))
                    check(q.multiply(b), b, q, zero)
                }
            }
        }
        val a = one.shiftLeft(4096).add(one)
        check(a, a, one, zero) // Identical receiver and operand.
        check(a, a.negate(), one.negate(), zero) // Shared immutable backing magnitude.
        check(one, a, zero, one)
        check(zero, a, zero, zero)
        for (b in listOf(one, BigInteger.TWO, BigInteger.valueOf(mask.toLong())))
            check(b.multiply(a).add(one), b, if (b == one) a.add(one) else a,
                if (b == one) zero else one)
    }

    @Test fun detachedResultsAndZeroDivisor() {
        val b = digits(1, 0, half)
        val a = b.multiply(digits(mask, mask)).add(b.subtract(one))
        val pair = a.divideAndRemainder(b)
        val quotient = pair[0]
        val remainder = pair[1]
        val qBytes = quotient.toByteArray()
        val rBytes = remainder.toByteArray()
        // Exported bytes and result-pair slots belong to callers.
        val exported = quotient.toByteArray()
        exported.fill(0)
        pair[0] = zero
        pair[1] = one
        repeat(4) { i -> a.add(BigInteger.valueOf(i.toLong())).divideAndRemainder(b.add(one)) }
        assertTrue(qBytes.contentEquals(quotient.toByteArray()))
        assertTrue(rBytes.contentEquals(remainder.toByteArray()))
        for (v in listOf(zero, one, a, a.negate())) {
            for (operation in 0..2) {
                val exception = assertFailsWith<ArithmeticException> {
                    when (operation) {
                        0 -> v.divide(zero)
                        1 -> v.remainder(zero)
                        else -> v.divideAndRemainder(zero)
                    }
                }
                assertEquals("BigInteger divide by zero", exception.message)
            }
        }
    }

    private class StoredOnly(text: String) : BigInteger(text) {
        private fun callback(): Nothing = error("unexpected virtual callback")
        override fun abs(): BigInteger = callback()
        override fun negate(): BigInteger = callback()
        override fun signum(): Int = callback()
        override fun bitLength(): Int = callback()
        override fun compareTo(other: BigInteger?): Int = callback()
        override fun testBit(n: Int): Boolean = callback()
        override fun add(value: BigInteger): BigInteger = callback()
        override fun subtract(value: BigInteger): BigInteger = callback()
        override fun shiftLeft(n: Int): BigInteger = callback()
        override fun divideAndRemainder(value: BigInteger): Array<BigInteger> = callback()
        fun inheritedPair(value: BigInteger): Array<BigInteger> = super.divideAndRemainder(value)
    }

    @Test fun inheritedEntrypointsReadStoredOperands() {
        val a = StoredOnly("-123456789012345678901234567890")
        val b = StoredOnly("100000000000000000000")
        val q = BigInteger("-1234567890")
        val r = BigInteger("-12345678901234567890")
        assertEquals(q, a.divide(b))
        assertEquals(r, a.remainder(b))
        val pair = a.inheritedPair(b)
        assertEquals(q, pair[0])
        assertEquals(r, pair[1])
        assertEquals(one, a.divide(a))
        assertEquals(zero, a.remainder(a))
    }

    @Test fun gcdInverseAndDecimalConsumers() {
        val b = one.shiftLeft(127).add(one)
        val a = b.multiply(one.shiftLeft(90).subtract(one)).add(one)
        assertEquals(one, a.gcd(b))
        assertEquals(one, a.negate().gcd(b))
        assertEquals(b.add(one).shiftRight(1), BigInteger.TWO.modInverse(b))
        assertEquals(b.subtract(one), b.subtract(one).modInverse(b))
        assertFailsWith<ArithmeticException> { BigInteger.TWO.modInverse(b.shiftLeft(1)) }
        val n = BigDecimal("123456789012345678901234567890")
        val d = BigDecimal("100000000000000000000")
        assertEquals(0, n.divide(d).compareTo(BigDecimal("1234567890.12345678901234567890")))
        assertEquals(0, n.divide(d, MathContext(10, RoundingMode.DOWN)).compareTo(BigDecimal("1234567890")))
        assertEquals(0, n.divide(d, MathContext(10, RoundingMode.UP)).compareTo(BigDecimal("1234567891")))
        assertEquals(0, n.negate().divide(d, MathContext(10, RoundingMode.FLOOR))
            .compareTo(BigDecimal("-1234567891")))
    }
}

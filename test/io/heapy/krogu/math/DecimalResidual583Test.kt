// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class DecimalResidual583Test {
    private fun power(exponent: Int): Long {
        require(exponent in 0..18)
        var result = 1L
        repeat(exponent) { result *= 10L }
        return result
    }

    private fun words(value: Long): IntArray {
        require(value >= 0)
        var rest = value
        val cells = mutableListOf<Int>()
        while (rest != 0L) {
            cells += (rest and 0x3fffffff).toInt()
            rest = rest ushr 30
        }
        return cells.toIntArray()
    }

    private fun assertDecimal(coefficient: Long, scale: Int, actual: BigDecimal, label: String = "") {
        assertEquals(coefficient.toString(), actual.unscaledValue().toString(), "$label coefficient")
        assertEquals(scale, actual.scale(), "$label scale")
    }

    @Test fun residualOnlyStreamAgainstBoundedPrimitiveDivision() {
        val sources = listOf(1L, 7L, 21L, 100L, 101L, 2001L, 123456789L,
            999999999L, 1000000001L, 1234567890123L)
        val divisors = listOf(1L, 2L, 3L, 7L, 20L, 1000000007L)
        for (a in sources) for (b in divisors) for (k in 1..3) for (sign in listOf(-1, 1)) {
            require(a <= Long.MAX_VALUE / power(k))
            val numerator = a * power(k)
            val integer = numerator / b
            val residual = numerator % b
            var coefficient = integer
            var scale = 0
            if (coefficient == 0L) scale = -k
            else while (scale > -k && coefficient % 10L == 0L) {
                coefficient /= 10L
                scale--
            }
            // One-shot primitive rational model, independent of the digit stream.
            val removed = minOf(k, -scale)
            assertEquals(0L, residual % power(removed))
            val left = words(a)
            val right = words(b)
            val candidate = words(coefficient)
            val leftBefore = left.copyOf()
            val rightBefore = right.copyOf()
            val candidateBefore = candidate.copyOf()
            val actual = assertNotNull(DecimalDivision581.certifiedResidual583(sign, left, right,
                candidate, -k, 4, scale), "$a/$b k=$k sign=$sign")
            assertEquals((sign * (residual / power(removed))).toString(), actual.first.toString())
            assertEquals(4 - removed, actual.second)
            assertNull(DecimalDivision581.certifiedResidual583(sign, left, right,
                words(coefficient + 1), -k, 4, scale), "wrong quotient $a/$b k=$k")
            assertNull(DecimalDivision581.certifiedResidual583(sign, left, right,
                candidate, -k, 4, scale - 1), "wrong quotient scale $a/$b k=$k")
            assertContentEquals(leftBefore, left)
            assertContentEquals(rightBefore, right)
            assertContentEquals(candidateBefore, candidate)
        }
        // Symbolic identity, not an overflowing primitive product:
        // (3*10^k+1)*10^k = (10^k)*(3*10^k) + 10^k.
        // This exercises whole low radix cells and whole-plus-partial suffix removal.
        for (k in listOf(9, 10, 18)) for (sign in listOf(-1, 1)) {
            val b = 3L * power(k)
            val actual = assertNotNull(DecimalDivision581.certifiedResidual583(sign,
                words(b + 1), words(b), words(1), -k, 0, -k))
            assertEquals(sign.toString(), actual.first.toString())
            assertEquals(-k, actual.second)
        }
    }

    @Test fun integerCertificateHandlesPaddingFractionsAndVirtualScaleBounds() {
        for ((coefficient, scale, integer) in listOf(Triple(0L, 0, 0L), Triple(0L, Int.MIN_VALUE, 0L),
            Triple(120L, 1, 12L), Triple(1200L, 2, 12L), Triple(12L, -1, 120L),
            Triple(1000000001L, 0, 1000000001L), Triple(1234567890123L, 0, 1234567890123L)))
            assertEquals(true, DecimalDivision581.matchesInteger583(words(coefficient), scale, words(integer)))
        for ((coefficient, scale, integer) in listOf(Triple(12L, 1, 1L), Triple(120L, 3, 0L),
            Triple(120L, 1, 13L), Triple(0L, 0, 1L), Triple(1L, Int.MIN_VALUE, 1000L),
            Triple(1L, Int.MAX_VALUE, 0L)))
            assertEquals(false, DecimalDivision581.matchesInteger583(words(coefficient), scale, words(integer)))
    }

    @Test fun realEndpointCasesAdmitFiniteQuotientsAndPreserveRemainderCohorts() {
        // A, B, Sb, selected precision-one scale, nonnegative residual coefficient.
        val rows = listOf(
            listOf(100L, 1L, (Int.MIN_VALUE + 2).toLong(), -3L, 0L),
            listOf(2001L, 20L, (Int.MIN_VALUE + 2).toLong(), -3L, 1L),
            listOf(100L, 1L, Int.MIN_VALUE.toLong(), -1L, 0L),
            listOf(101L, 1L, Int.MIN_VALUE.toLong(), -1L, 1L))
        val sourceScale = Int.MIN_VALUE + 1
        for (row in rows) for (leftSign in listOf(-1, 1)) for (rightSign in listOf(-1, 1))
            for (mode in RoundingMode.entries) {
                val source = BigDecimal.valueOf(row[0] * leftSign, sourceScale)
                val divisor = BigDecimal.valueOf(row[1] * rightSign, row[2].toInt())
                val context = MathContext(1, mode)
                val sign = (leftSign * rightSign).toLong()
                val label = "${row.take(2)} signs=$leftSign/$rightSign mode=$mode"
                assertDecimal(sign, row[3].toInt(), source.divideToIntegralValue(divisor, context), label)
                val pair = source.divideAndRemainder(divisor, context)
                assertEquals(2, pair.size)
                assertDecimal(sign, row[3].toInt(), pair[0], label)
                val magnitudeText = if (row[3] == -3L) "1E+3" else "1E+1"
                assertEquals((if (sign < 0) "-" else "") + magnitudeText, pair[0].toString())
                assertDecimal(leftSign * row[4], sourceScale, pair[1], label)
                assertDecimal(leftSign * row[4], sourceScale, source.remainder(divisor, context), label)
                val unlimited = MathContext(0, mode)
                val preferred = (sourceScale.toLong() - row[2]).toInt()
                val exactPair = source.divideAndRemainder(divisor, unlimited)
                assertDecimal(100L * sign, preferred, exactPair[0], label)
                assertDecimal(leftSign * row[4], sourceScale, exactPair[1], label)
                assertDecimal(leftSign * row[4], sourceScale, source.remainder(divisor, unlimited), label)
                assertDecimal(row[0] * leftSign, sourceScale, source)
                assertDecimal(row[1] * rightSign, row[2].toInt(), divisor)
            }
        for (leftSign in listOf(-1, 1)) for (rightSign in listOf(-1, 1)) for (mode in RoundingMode.entries) {
            // Integer part 1001 has four required digits and cannot fit precision one.
            val source = BigDecimal.valueOf(2003L * leftSign, sourceScale)
            val divisor = BigDecimal.valueOf(20L * rightSign, Int.MIN_VALUE + 2)
            val context = MathContext(1, mode)
            assertFailsWith<ArithmeticException> { source.divideToIntegralValue(divisor, context) }
            assertFailsWith<ArithmeticException> { source.divideAndRemainder(divisor, context) }
            assertFailsWith<ArithmeticException> { source.remainder(divisor, context) }
        }
    }

    @Test fun selectedQuotientAliasExceptionsAndMismatchFallbackStayObservable() {
        val sourceScale = Int.MIN_VALUE + 1
        val divisor = BigDecimal.valueOf(1, Int.MIN_VALUE + 2)
        val context = MathContext(1, RoundingMode.DOWN)
        val marker = BigDecimal.valueOf(1, -3)
        val failure = IllegalStateException("caller583")
        class Caller(private val replacement: BigDecimal?, private val throws: Boolean) : BigDecimal(BigInteger.valueOf(100), sourceScale) {
            override fun divideToIntegralValue(value: BigDecimal, mc: MathContext): BigDecimal {
                assertSame(divisor, value)
                assertSame(context, mc)
                if (throws) throw failure
                return replacement!!
            }
        }
        val pair = (Caller(marker, false) as BigDecimal).divideAndRemainder(divisor, context)
        assertSame(marker, pair[0])
        assertDecimal(0, sourceScale, pair[1])
        assertSame(failure, assertFailsWith<IllegalStateException> {
            (Caller(null, true) as BigDecimal).divideAndRemainder(divisor, context)
        })
        // Deliberately nontruthful probe of the preserved fallback, not a numeric contract.
        // A wrong quotient must not be silently replaced or certified as the true 1000.
        val replacement = object : BigDecimal(BigInteger.valueOf(2), -3) {
            override fun multiply(value: BigDecimal): BigDecimal {
                assertSame(divisor, value)
                return BigDecimal.valueOf(0, sourceScale)
            }
        }
        val fallback = Caller(replacement, false).divideAndRemainder(divisor, context)
        assertSame(replacement, fallback[0])
        assertDecimal(100, sourceScale, fallback[1])
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divideAndRemainder(BigDecimal.ZERO) }
        assertFailsWith<ArithmeticException> { BigDecimal.ZERO.remainder(BigDecimal.ZERO, MathContext.UNLIMITED) }
    }
}

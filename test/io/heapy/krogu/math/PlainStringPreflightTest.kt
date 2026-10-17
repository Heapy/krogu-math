// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal data class PlainOverflowWitness(val coefficient: String, val scale: Int, val length: Long)

// Independently counted coefficient/scale layouts; no reference formatter calls.
internal val plainOverflowWitnesses = listOf(
    PlainOverflowWitness("1", 2147483647, 2147483649L),
    PlainOverflowWitness("1", 2147483646, 2147483648L),
    PlainOverflowWitness("1", -2147483647, 2147483648L),
    PlainOverflowWitness("-1", 2147483647, 2147483650L),
    PlainOverflowWitness("-1", 2147483646, 2147483649L),
    PlainOverflowWitness("-1", 2147483645, 2147483648L),
    PlainOverflowWitness("-1", -2147483647, 2147483649L),
    PlainOverflowWitness("123456789", 2147483647, 2147483649L),
    PlainOverflowWitness("123456789", 2147483646, 2147483648L),
    PlainOverflowWitness("123456789", -2147483647, 2147483656L),
    PlainOverflowWitness("-123456789", 2147483647, 2147483650L),
    PlainOverflowWitness("-123456789", 2147483646, 2147483649L),
    PlainOverflowWitness("-123456789", 2147483645, 2147483648L),
    PlainOverflowWitness("-123456789", -2147483647, 2147483657L),
)

class PlainStringPreflightTest {
    @Test fun impossibleLayoutsFailExplicitly() {
        assertEquals(14, plainOverflowWitnesses.size)
        for (witness in plainOverflowWitnesses) {
            assertTrue(witness.length > Int.MAX_VALUE.toLong())
            val value = BigDecimal(BigInteger(witness.coefficient), witness.scale)
            val failure = assertFailsWith<Error>("$witness") { value.toPlainString() }
            assertEquals("too large to fit in a String", failure.message)
        }
        for (coefficient in listOf("0", "1", "-1")) {
            val failure = assertFailsWith<Error> {
                BigDecimal(BigInteger(coefficient), Int.MAX_VALUE).toPlainString()
            }
            assertEquals("too large to fit in a String", failure.message)
        }
        for (coefficient in listOf("1", "-1")) {
            val failure = assertFailsWith<Error> {
                BigDecimal(BigInteger(coefficient), Int.MIN_VALUE).toPlainString()
            }
            assertEquals("too large to fit in a String", failure.message)
        }
    }

    @Test fun ordinaryLayoutsKeepScaleSignAndZeros() {
        val cases = listOf(
            Triple("123", -2, "12300"), Triple("-123", -2, "-12300"),
            Triple("123", 0, "123"), Triple("-123", 0, "-123"),
            Triple("123", 1, "12.3"), Triple("-123", 1, "-12.3"),
            Triple("123", 3, "0.123"), Triple("-123", 3, "-0.123"),
            Triple("123", 5, "0.00123"), Triple("-123", 5, "-0.00123"),
            Triple("1200", 3, "1.200"), Triple("1200", 5, "0.01200"),
            Triple("0", 0, "0"), Triple("0", 1, "0.0"), Triple("0", 4, "0.0000"),
            Triple("0", -1, "0"), Triple("0", Int.MIN_VALUE, "0"),
        )
        for ((coefficient, scale, expected) in cases) {
            assertEquals(expected, BigDecimal(BigInteger(coefficient), scale).toPlainString())
        }
    }

    @Test fun subclassDispatchStillOwnsSignAndEarlyReturns() {
        fun value(coefficient: String, scale: Int, sign: Int, trace: StringBuilder) =
            object : BigDecimal(BigInteger(coefficient), scale) {
                override fun signum(): Int { trace.append("sign;"); return sign }
                override fun precision(): Int = error("precision must not be called")
                override fun scale(): Int = error("scale must not be called")
                override fun unscaledValue(): BigInteger = error("unscaledValue must not be called")
            }
        val trace = StringBuilder()
        assertEquals("123", value("123", 0, -1, trace).toPlainString())
        assertEquals("", trace.toString())
        assertEquals("0", value("123", Int.MIN_VALUE, 0, trace).toPlainString())
        assertEquals("sign;", trace.toString())
        trace.clear()
        assertEquals("12300", value("123", -2, -1, trace).toPlainString())
        assertEquals("sign;", trace.toString())
        trace.clear()
        assertEquals("-0.00123", value("123", 5, -1, trace).toPlainString())
        assertEquals("sign;", trace.toString())
        trace.clear()
        val overflowing = value("123456789", Int.MAX_VALUE, -1, trace)
        val failure = assertFailsWith<Error> { overflowing.toPlainString() }
        assertEquals("too large to fit in a String", failure.message)
        assertEquals("sign;", trace.toString())
        val sentinel = IllegalStateException("caller-owned sign")
        val throwing = object : BigDecimal(BigInteger.ONE, Int.MAX_VALUE) {
            override fun signum(): Int = throw sentinel
        }
        assertSame(sentinel, assertFailsWith<IllegalStateException> { throwing.toPlainString() })
        val owned = object : BigDecimal(BigInteger.ONE, Int.MAX_VALUE) {
            override fun toPlainString(): String = "caller-owned"
        }
        assertEquals("caller-owned", owned.toPlainString())
    }
}

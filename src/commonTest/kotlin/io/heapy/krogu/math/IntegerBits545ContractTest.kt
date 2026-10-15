// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Source-only milestone: UNCOMPILED and UNRUN. Expected values are independent literals. */
class IntegerBits545ContractTest {
    @Test fun composedUpdatesCrossLimbAndPublicWordBoundaries() {
        // Each starting value is -(2^n+1). Clearing bit 0 gives -(2^n+2),
        // setting bit n gives -2, and complementing gives 1.
        val cases = listOf(
            Triple(30, "-1073741825", "-1073741826"),
            Triple(32, "-4294967297", "-4294967298"),
            Triple(64, "-18446744073709551617", "-18446744073709551618"),
        )
        for ((bit, text, cleared) in cases) {
            val source = BigInteger(text)
            val step = source.clearBit(0)
            assertEquals(cleared, step.toString())
            val updated = step.setBit(bit)
            assertEquals("-2", updated.toString())
            assertEquals("1", updated.not().toString())
            assertEquals(cleared, updated.flipBit(bit).toString())
            assertEquals(text, source.toString())
            assertEquals(false, source.testBit(bit))
            assertEquals(true, source.testBit(bit + 1))
        }
    }

    @Test fun fullTopLimbAndPartialTopLimbDecodeTheSameInfiniteTail() {
        // Exactly fifteen 8-hex-digit groups: 2^479 and 2^479-1.
        // Negative power has bitLength 479 (width 480, h=30).
        val power = "80000000" + "00000000" + "00000000" +
            "00000000" + "00000000" + "00000000" +
            "00000000" + "00000000" + "00000000" +
            "00000000" + "00000000" + "00000000" +
            "00000000" + "00000000" + "00000000"
        val below = "7fffffff" + "ffffffff" + "ffffffff" +
            "ffffffff" + "ffffffff" + "ffffffff" +
            "ffffffff" + "ffffffff" + "ffffffff" +
            "ffffffff" + "ffffffff" + "ffffffff" +
            "ffffffff" + "ffffffff" + "ffffffff"
        val negative = BigInteger("-$power", 16)
        assertEquals(479, negative.bitLength())
        assertEquals(479, negative.bitCount())
        assertEquals(479, negative.getLowestSetBit())
        assertEquals(below, negative.not().toString(16))
        assertEquals("-$power", BigInteger(below, 16).not().toString(16))
        // Address 479 expands set/flip width to 512, whose high limb has 2 bits.
        assertEquals("-$power", negative.setBit(479).toString(16))
        assertEquals("0", BigInteger(power, 16).flipBit(479).toString())
        assertEquals(below, negative.setBit(479).not().toString(16))
    }

    @Test fun negativeFloorHasExactAndInexactLimbBoundaryWitnesses() {
        val cases = listOf(
            Triple("-1073741824", 30, "-1"),
            Triple("-1073741825", 30, "-2"),
            Triple("-4294967296", 32, "-1"),
            Triple("-4294967297", 32, "-2"),
            Triple("-18446744073709551616", 64, "-1"),
            Triple("-18446744073709551617", 64, "-2"),
        )
        for ((text, distance, expected) in cases) {
            val source = BigInteger(text)
            assertEquals(expected, source.shiftRight(distance).toString())
            assertEquals(expected, source.shiftLeft(-distance).toString())
            assertEquals(text, source.shiftLeft(0).toString())
        }
        assertEquals("0", BigInteger.ZERO.shiftRight(Int.MIN_VALUE).toString())
        assertEquals("-1", BigInteger("-3").shiftLeft(Int.MIN_VALUE).toString())
        assertFailsWith<ArithmeticException> { BigInteger("-3").shiftRight(Int.MIN_VALUE) }
    }

    @Test fun sparseNegativeBorrowChainComposesMaskingAndFloor() {
        // m=2^64+2^30 has two ones and thirty trailing zeros.
        val source = BigInteger("-18446744074783293440")
        val lowMask = BigInteger("1073741823")
        assertEquals(65, source.bitLength())
        assertEquals(31, source.bitCount())
        assertEquals(30, source.getLowestSetBit())
        assertEquals("0", source.and(lowMask).toString())
        val filled = source.or(lowMask)
        assertEquals("-18446744073709551617", filled.toString())
        assertEquals("-18446744074783293440", filled.andNot(lowMask).toString())
        assertEquals("-17179869185", source.shiftRight(30).toString())
        assertEquals("-8589934593", source.shiftRight(31).toString())
    }

    private class TruthfulInteger(text: String, val events: MutableList<String>) : BigInteger(text) {
        override fun bitLength(): Int {
            events += "bits"
            return super.bitLength()
        }

        override fun getLowestSetBit(): Int {
            events += "lowest"
            return super.getLowestSetBit()
        }

        override fun xor(value: BigInteger?): BigInteger {
            events += "direct-enter"
            // Value-preserving composition, even for a self operand.
            val result = super.xor(value).not().not()
            events += "direct-exit"
            return result
        }
    }

    @Test fun truthfulCompositionAndSelfAliasWorkThroughBaseTypedCaller() {
        val events = mutableListOf<String>()
        val source: BigInteger = TruthfulInteger("-4294967296", events)
        assertEquals("-1", source.xor(BigInteger("4294967295")).toString())
        assertEquals("direct-enter", events.first())
        assertEquals("direct-exit", events.last())
        events.clear()
        assertEquals("0", source.xor(source).toString())
        assertEquals("direct-enter", events.first())
        assertEquals("direct-exit", events.last())
        // Explicit calls require dispatch. No internal callback count is asserted.
        events.clear()
        assertEquals(32, source.bitLength())
        assertEquals(32, source.bitCount())
        assertEquals(32, source.getLowestSetBit())
        assertTrue("bits" in events)
        assertTrue("lowest" in events)
        assertEquals("-4294967296", source.toString())
    }

    @Test fun directOverrideFailureIsTheCallersException() {
        val marker = IllegalStateException("explicit caller override")
        val source: BigInteger = object : BigInteger("-1") {
            override fun flipBit(n: Int): BigInteger = throw marker
        }
        // An explicit override executes before any inherited validation.
        assertSame(marker, assertFailsWith<IllegalStateException> { source.flipBit(-1) })
    }
}

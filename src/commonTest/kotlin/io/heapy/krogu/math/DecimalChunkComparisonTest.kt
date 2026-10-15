// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DecimalChunkComparisonTest {
    // Literal neighbors of 10^9, 2^30 and 2^60; no port arithmetic is an oracle.
    private val neighbors = listOf(
        Triple("1000000001", "1000000000", "1000000002"),
        Triple("1073741823", "1073741822", "1073741824"),
        Triple("1073741824", "1073741823", "1073741825"),
        Triple("1073741825", "1073741824", "1073741826"),
        Triple("1152921504606846975", "1152921504606846974", "1152921504606846976"),
        Triple("1152921504606846976", "1152921504606846975", "1152921504606846977"),
        Triple("1152921504606846977", "1152921504606846976", "1152921504606846978"),
    )
    private val drops = listOf(1, 8, 9, 10, 17, 18, 27, 36, 63, 129)

    @Test fun chunkBoundariesKeepQuotientOrderAndEveryDiscardedRemainder() {
        for ((shortText, predecessor, successor) in neighbors) for (drop in drops) {
            val cases = mutableListOf(
                shortText + "0".repeat(drop) to 0,
                shortText + "0".repeat(drop - 1) + "1" to 1,
                predecessor + "9".repeat(drop) to -1,
                predecessor + "0".repeat(drop - 1) + "1" to -1,
                successor + "0".repeat(drop) to 1,
                successor + "0".repeat(drop - 1) + "1" to 1,
            )
            if (drop >= 18) {
                // r=10^9: first remainder0, next1; any later chunks have remainder0.
                cases += shortText + "0".repeat(drop - 10) + "1" + "0".repeat(9) to 1
                cases += predecessor + "0".repeat(drop - 10) + "1" + "0".repeat(9) to -1
            }
            for ((longText, order) in cases) for (sign in listOf(1, -1)) {
                val prefix = if (sign < 0) "-" else ""
                val short = BigInteger(prefix + shortText)
                val long = BigInteger(prefix + longText)
                val context = "drop=$drop/$shortText/$longText/sign=$sign"
                assertEquals(order, long.compareDecimalSignificand(short), context)
                assertEquals(-order, short.compareDecimalSignificand(long), context)
                assertEquals(sign * order, BigDecimal(long, drop).compareTo(BigDecimal(short, 0)), context)
                assertEquals(-sign * order, BigDecimal(short, 0).compareTo(BigDecimal(long, drop)), context)
            }
        }
    }

    private class TruthfulInteger(text: String) : BigInteger(text) {
        override fun signum(): Int = super.signum()
        override fun abs(): BigInteger = super.abs()
        override fun toString(): String = super.toString()
        override fun compareTo(other: BigInteger?): Int = super.compareTo(other)
    }

    @Test fun scratchNeverMutatesOrRetainsOrdinaryOrTruthfulInputMagnitudes() {
        for (sign in listOf("", "-")) for (subtype in listOf(false, true)) {
            val shortText = sign + "1152921504606846976"
            val longText = shortText + "0".repeat(119) + "1" + "0".repeat(9)
            fun integer(text: String): BigInteger = if (subtype) TruthfulInteger(text) else BigInteger(text)
            val short = integer(shortText)
            val long = integer(longText)
            val shortBytes = short.toByteArray()
            val longBytes = long.toByteArray()
            val shortHash = short.hashCode()
            val longHash = long.hashCode()
            repeat(3) {
                assertEquals(-1, short.compareDecimalSignificand(long))
                assertEquals(1, long.compareDecimalSignificand(short))
                assertEquals(shortText, short.toString())
                assertEquals(longText, long.toString())
                assertContentEquals(shortBytes, short.toByteArray())
                assertContentEquals(longBytes, long.toByteArray())
                assertEquals(shortHash, short.hashCode())
                assertEquals(longHash, long.hashCode())
            }
            // Another comparison cannot retain/reuse a prior call's local scratch.
            assertEquals(0, long.compareDecimalSignificand(integer(longText + "0")))
            assertEquals(shortText, short.toString())
            assertEquals(longText, long.toString())
        }
    }

    private class MetadataDecimal(text: String, scale: Int, private val label: String,
        private val trace: StringBuilder, private val reportedDigits: Int? = null) : BigDecimal(BigInteger(text), scale) {
        override fun precision(): Int {
            trace.append(label).append(';')
            return reportedDigits ?: super.precision()
        }
        fun directCompare(other: BigDecimal): Int = super.compareTo(other)
    }

    @Test fun truthfulAndReportedExponentControlsKeepTheirDispatchDecision() {
        for (sign in listOf(1, -1)) {
            val prefix = if (sign < 0) "-" else ""
            val shortText = prefix + "1073741824"
            val longText = shortText + "0".repeat(26) + "1"
            for (reported in listOf(false, true)) {
                val trace = StringBuilder()
                val short = MetadataDecimal(shortText, 0, "a", trace, if (reported) 7 else null)
                val long = MetadataDecimal(longText, 27, "b", trace, if (reported) 34 else null)
                assertEquals(-sign, short.directCompare(long))
                assertEquals("a;b;", trace.toString())
                trace.clear()
                assertEquals(sign, long.directCompare(short))
                assertEquals("b;a;", trace.toString())
            }
            val trace = StringBuilder()
            val short = MetadataDecimal(shortText, 0, "a", trace, 8)
            val long = MetadataDecimal(longText, 27, "b", trace, 34)
            // Reported exponents differ: retain that earlier result even though
            // stored normalized significands would order short below long.
            assertEquals(sign, short.directCompare(long))
            assertEquals("a;b;", trace.toString())
        }
    }
}

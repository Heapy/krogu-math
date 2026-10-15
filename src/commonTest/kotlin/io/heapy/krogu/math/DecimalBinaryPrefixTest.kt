// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Numeric prefix expectations come from bounded, known decimal text, independent of its estimator. */
class DecimalBinaryPrefixTest {
    private val widths = listOf(1, 2, 7, 159, 1093)

    private fun check(value: BigInteger, magnitudeText: String, width: Int, label: String) {
        val digits = magnitudeText.length
        val lower = value.decimalDigitLowerBound()
        assertTrue(lower >= 1 && digits - lower in 0..1, "digit lower bound: $label width=$width")
        val prefix = value.decimalBinaryPrefix(width)
        val kept = minOf(width, digits)
        assertEquals(BigInteger(magnitudeText.take(kept)), prefix.magnitude, "magnitude: $label width=$width")
        assertEquals(digits, prefix.decimalDigits, "digit count: $label width=$width")
        assertEquals(digits - kept, prefix.discardedDigits, "discarded count: $label width=$width")
        assertEquals(magnitudeText.drop(kept).any { it != '0' }, prefix.sticky, "sticky: $label width=$width")
    }

    @Test fun decimalPowersAndTheirImmediateNeighborsHaveIndependentTextExpectations() {
        var count = 0
        for (exponent in listOf(0, 1, 6, 7, 158, 159, 1092, 1093)) {
            val neighbors = if (exponent == 0) listOf("0", "1", "2") else listOf(
                "9".repeat(exponent),
                "1" + "0".repeat(exponent),
                "1" + "0".repeat(exponent - 1) + "1",
            )
            for ((offset, magnitude) in neighbors.withIndex()) for (negative in listOf(false, true)) {
                val value = BigInteger((if (negative) "-" else "") + magnitude)
                for (width in widths) {
                    check(value, magnitude, width, "10^$exponent neighbor=${offset - 1} negative=$negative")
                    count++
                }
            }
        }
        assertEquals(240, count)
    }

    @Test fun binaryPowersAndTheirImmediateNeighborsCrossBinaryDigitBoundaries() {
        // Literal decimal expansions are the oracle; shiftLeft constructs only the input.
        val models = listOf(
            8 to listOf("255", "256", "257"),
            24 to listOf("16777215", "16777216", "16777217"),
            53 to listOf("9007199254740991", "9007199254740992", "9007199254740993"),
            127 to listOf(
                "170141183460469231731687303715884105727",
                "170141183460469231731687303715884105728",
                "170141183460469231731687303715884105729",
            ),
        )
        var count = 0
        for ((exponent, neighbors) in models) for ((index, magnitude) in neighbors.withIndex()) {
            val positive = BigInteger.ONE.shiftLeft(exponent).add(BigInteger.valueOf(index.toLong() - 1))
            for (negative in listOf(false, true)) {
                val value = if (negative) positive.negate() else positive
                for (width in widths) {
                    check(value, magnitude, width, "2^$exponent neighbor=${index - 1} negative=$negative")
                    count++
                }
            }
        }
        assertEquals(120, count)
    }

    @Test fun exactPrefixWidthBoundariesKeepTheCorrectDigitCount() {
        var count = 0
        for (width in listOf(159, 1093)) for (length in (width - 1)..(width + 1)) {
            val magnitude = "7".repeat(length)
            for (negative in listOf(false, true)) {
                check(BigInteger((if (negative) "-" else "") + magnitude), magnitude, width,
                    "length=$length negative=$negative")
                count++
            }
        }
        assertEquals(12, count)
    }

    @Test fun stickyTracksEveryDiscardedDigitIncludingAnExtraQuotientDigit() {
        var count = 0
        // 110 has seven bits and a two-digit lower bound. Initial trimming by
        // one digit gives quotient 11 with zero remainder; removing its extra
        // digit must still make the final one-digit prefix sticky.
        for (negative in listOf(false, true)) {
            val value = BigInteger(if (negative) "-110" else "110")
            assertEquals(2, value.decimalDigitLowerBound(), "extra quotient digit witness")
            check(value, "110", 1, "extra quotient digit negative=$negative")
            count++
        }
        for (width in listOf(159, 1093)) {
            val retained = "3141592653589793238462643383279".repeat(40).take(width)
            val tails = listOf(
                "0".repeat(32), // exact division: all omitted digits are zero
                "0".repeat(31) + "1", // nonzero remainder only at the far end
                "1" + "0".repeat(31), // first omitted digit is nonzero; all later digits are zero
                "0" + "1" + "0".repeat(30),
                "9".repeat(32),
            )
            for ((index, tail) in tails.withIndex()) for (negative in listOf(false, true)) {
                val magnitude = retained + tail
                check(BigInteger((if (negative) "-" else "") + magnitude), magnitude, width,
                    "tail model=$index negative=$negative")
                count++
            }
        }
        assertEquals(22, count)
    }

    @Test fun storedValueExtractionDoesNotConsultPublicSubclassOverrides() {
        val retained = "2718281828459045235360287471352".repeat(40)
        val inputs = listOf(
            "0", "1", "-1",
            retained.take(159) + "1" + "0".repeat(32),
            "-" + retained.take(1093) + "0".repeat(32) + "1",
        )
        var count = 0
        for ((index, text) in inputs.withIndex()) {
            val value = object : BigInteger(text) {
                override fun toString(): String = error("virtual toString")
                override fun toString(radix: Int): String = error("virtual radix toString")
                override fun bitLength(): Int = error("virtual bitLength")
                override fun signum(): Int = error("virtual signum")
                override fun abs(): BigInteger = error("virtual abs")
                override fun negate(): BigInteger = error("virtual negate")
                override fun toByteArray(): ByteArray = error("virtual toByteArray")
                override fun toInt(): Int = error("virtual toInt")
                override fun toLong(): Long = error("virtual toLong")
                override fun toFloat(): Float = error("virtual toFloat")
                override fun toDouble(): Double = error("virtual toDouble")
                override fun add(value: BigInteger): BigInteger = error("virtual add")
                override fun subtract(value: BigInteger): BigInteger = error("virtual subtract")
                override fun multiply(value: BigInteger): BigInteger = error("virtual multiply")
                override fun divide(value: BigInteger): BigInteger = error("virtual divide")
                override fun remainder(value: BigInteger): BigInteger = error("virtual remainder")
                override fun divideAndRemainder(value: BigInteger): Array<BigInteger> = error("virtual divideAndRemainder")
                override fun pow(exponent: Int): BigInteger = error("virtual pow")
                override fun shiftLeft(n: Int): BigInteger = error("virtual shiftLeft")
                override fun shiftRight(n: Int): BigInteger = error("virtual shiftRight")
                override fun compareTo(other: BigInteger): Int = error("virtual compareTo")
                override fun testBit(n: Int): Boolean = error("virtual testBit")
                override fun getLowestSetBit(): Int = error("virtual getLowestSetBit")
            }
            for (width in widths) {
                check(value, text.removePrefix("-"), width, "throwing subclass model=$index")
                count++
            }
        }
        assertEquals(25, count)
    }
}

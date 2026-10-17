// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Test data is derived from adjacent IEEE encodings, without converting a decimal oracle. */
internal data class CancellationFormat(
    val single: Boolean,
    val fractionBits: Int,
    val minExponent: Int,
    val bias: Int,
    val oneBits: Long,
    val maxFiniteBits: Long,
) {
    val signMask: Long get() = if (single) 0x80000000L else Long.MIN_VALUE
    val name: String get() = if (single) "float" else "double"
    val minNormalBits: Long get() = 1L shl fractionBits

    fun bits(value: BigDecimal): Long =
        if (single) value.toFloat().toRawBits().toLong() and 0xffffffffL else value.toDouble().toRawBits()
}

internal val cancellationFloat = CancellationFormat(true, 23, -126, 127, 0x3f800000L, 0x7f7fffffL)
internal val cancellationDouble = CancellationFormat(false, 52, -1022, 1023, 0x3ff0000000000000L, 0x7fefffffffffffffL)

internal data class CancellationCase(
    val coefficient: String,
    val scale: Int,
    val format: CancellationFormat,
    val expectedBits: Long,
    val label: String,
) {
    fun value(): BigDecimal = BigDecimal(BigInteger(coefficient), scale)
}

/**
 * The midpoint above a finite positive encoding is (2 * significand + 1) * 2^e.
 * Multiplying by 5^(-e) constructs its exact terminating decimal for negative e.
 * The expected result is the known adjacent encoding, selected by retained parity.
 * This also covers the virtual successor of max finite: infinity wins its odd tie.
 */
internal fun cancellationMidpointCases(format: CancellationFormat): List<CancellationCase> = buildList {
    val minNormal = format.minNormalBits
    val lowerEncodings = listOf(
        0L, 1L, 2L, // zero/min subnormal and both subnormal parity classes
        minNormal - 2, minNormal - 1, minNormal, minNormal + 1,
        format.oneBits - 2, format.oneBits - 1, format.oneBits, format.oneBits + 1,
        format.oneBits + minNormal - 1, // significand carry into the next binade
        format.maxFiniteBits - 1, format.maxFiniteBits,
    )
    val fractionMask = minNormal - 1
    for (lower in lowerEncodings) {
        val field = (lower ushr format.fractionBits).toInt()
        val significand = (lower and fractionMask) + if (field == 0) 0 else minNormal
        val exponent = (if (field == 0) format.minExponent else field - format.bias) - format.fractionBits - 1
        val numerator = BigInteger.valueOf(2 * significand + 1)
        val exactCoefficient: BigInteger
        val exactScale: Int
        if (exponent < 0) {
            exactCoefficient = numerator.multiply(BigInteger.valueOf(5).pow(-exponent))
            exactScale = -exponent
        } else {
            exactCoefficient = numerator.shiftLeft(exponent)
            exactScale = 0
        }
        val decimal = exactCoefficient.toString()
        // Each perturbation is at the far end of a coefficient with 4096 digits.
        // Even the shortest zero run exceeds either production prefix bound.
        val padding = 4096 - decimal.length
        val padded = BigInteger(decimal + "0".repeat(padding))
        for (tail in -1..1) {
            val coefficient = padded.add(BigInteger.valueOf(tail.toLong())).toString()
            val positiveBits = when {
                tail < 0 -> lower
                tail > 0 -> lower + 1
                lower and 1L == 0L -> lower
                else -> lower + 1
            }
            for (negative in listOf(false, true)) {
                add(CancellationCase(
                    (if (negative) "-" else "") + coefficient,
                    exactScale + padding,
                    format,
                    positiveBits or if (negative) format.signMask else 0L,
                    "${format.name} midpoint above ${lower.toString(16)} tail=$tail negative=$negative",
                ))
            }
        }
    }
}

internal data class CancellationCohort(
    val coefficient: String,
    val scale: Int,
    val floatBits: Long,
    val doubleBits: Long,
)

internal val cancellationCohorts = listOf(
    CancellationCohort("1", 1, 0x3dcccccdL, 0x3fb999999999999aL),
    CancellationCohort("1", 0, 0x3f800000L, 0x3ff0000000000000L),
    CancellationCohort("125", 2, 0x3fa00000L, 0x3ff4000000000000L),
    // Integer tie for float; exactly representable by double.
    CancellationCohort("16777217", 0, 0x4b800000L, 0x4170000010000000L),
)

internal fun cancellationCohortCases(): List<CancellationCase> = buildList {
    for (cohort in cancellationCohorts) for (padding in listOf(0, 1, 160, 1094, 4096)) {
        for (negative in listOf(false, true)) for (format in listOf(cancellationFloat, cancellationDouble)) {
            val bits = if (format.single) cohort.floatBits else cohort.doubleBits
            add(CancellationCase(
                (if (negative) "-" else "") + cohort.coefficient + "0".repeat(padding),
                cohort.scale + padding,
                format,
                bits or if (negative) format.signMask else 0L,
                "${format.name} ${cohort.coefficient}@${cohort.scale} padding=$padding negative=$negative",
            ))
        }
    }
}

class DecimalBinaryCancellationTest {
    @Test fun signedEquivalentCohortsRetainExactRawBits() {
        val cases = cancellationCohortCases()
        assertEquals(80, cases.size)
        for (case in cases) assertEquals(case.expectedBits, case.format.bits(case.value()), case.label)
    }

    @Test fun floatMidpointsRetainParityAndDistantStickyTails() = checkMidpoints(cancellationFloat)

    @Test fun doubleMidpointsRetainParityAndDistantStickyTails() = checkMidpoints(cancellationDouble)

    private fun checkMidpoints(format: CancellationFormat) {
        val cases = cancellationMidpointCases(format)
        assertEquals(84, cases.size, format.name)
        assertEquals(28, cases.count { "tail=0 " in it.label }, "exact zero suffixes")
        assertEquals(28, cases.count { "tail=1 " in it.label }, "nonzero digit at far end")
        for (case in cases) {
            assertTrue(case.coefficient.removePrefix("-").length >= 4096, case.label)
            assertEquals(case.expectedBits, format.bits(case.value()), case.label)
        }
    }

    @Test fun zeroAtExtremeAndCancellingScalesIsPositiveZero() {
        for (scale in listOf(Int.MIN_VALUE, -4096, 0, 4096, Int.MAX_VALUE)) {
            val value = BigDecimal(BigInteger.ZERO, scale)
            assertEquals(0, value.toFloat().toRawBits(), "float zero scale=$scale")
            assertEquals(0L, value.toDouble().toRawBits(), "double zero scale=$scale")
        }
    }

    @Test fun truthfulSubclassKeepsStoredValueAndGeneralPathSignDispatch() {
        var count = 0
        for (case in cancellationCohortCases().filter { "padding=4096 " in it.label }) {
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(case.coefficient), case.scale) {
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun precision(): Int { trace.append("precision;"); return super.precision() }
                override fun scale(): Int { trace.append("scale;"); return super.scale() }
                override fun unscaledValue(): BigInteger { trace.append("coefficient;"); return super.unscaledValue() }
                override fun toString(): String { trace.append("string;"); return super.toString() }
            }
            assertEquals(case.expectedBits, case.format.bits(value), case.label)
            assertEquals("sign;", trace.toString(), case.label)
            count++
        }
        assertEquals(16, count)
    }
}

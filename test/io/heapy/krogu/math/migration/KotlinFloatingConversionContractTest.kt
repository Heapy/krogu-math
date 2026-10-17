// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math.migration

import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.MathContext
import io.heapy.krogu.math.RoundingMode
import io.heapy.krogu.math.toBigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

/** Independent literals; public extension imports and declared result types are intentional. */
class KotlinFloatingConversionContractTest {
    private fun assertDecimal(coefficient: String, scale: Int, actual: BigDecimal, label: String = "") {
        assertEquals(coefficient, actual.unscaledValue().toString(), "$label coefficient")
        assertEquals(scale, actual.scale(), "$label scale")
    }

    @Test fun primitiveLiteralsPreserveCanonicalTextScaleAndSelectKroguTypes() {
        val float: BigDecimal = 0.1f.toBigDecimal()
        val double: BigDecimal = 0.1.toBigDecimal()
        val namedFloat: BigDecimal = 0.1f.toBigDecimal(mathContext = MathContext.UNLIMITED)
        val namedDouble: BigDecimal = 0.1.toBigDecimal(mathContext = MathContext.UNLIMITED)
        for (decimal in listOf(float, double, namedFloat, namedDouble)) assertDecimal("1", 1, decimal)
        for (value in listOf(0f, -0f)) assertDecimal("0", 1, value.toBigDecimal())
        for (value in listOf(0.0, -0.0)) assertDecimal("0", 1, value.toBigDecimal())
        for ((value, coefficient, scale) in listOf(
            Triple(1f, "10", 1), Triple(1000000f, "10000000", 1),
            Triple(10000000f, "10", -6), Triple(0.001f, "1", 3), Triple(0.0001f, "10", 5))) {
            assertDecimal(coefficient, scale, value.toBigDecimal(), "Float bits=${value.toBits()}")
        }
        for ((value, coefficient, scale) in listOf(
            Triple(1.0, "10", 1), Triple(1000000.0, "10000000", 1),
            Triple(10000000.0, "10", -6), Triple(0.001, "1", 3), Triple(0.0001, "10", 5))) {
            assertDecimal(coefficient, scale, value.toBigDecimal(), "Double bits=${value.toBits()}")
        }
    }

    @Test fun floatBitBoundariesRetainAsymmetricAndDecimalTieRegressions() {
        // The first three rows preserve the six signed failures recorded in task525 activity1600.
        // 2^-12 is exactly 0.000244140625: both shortest 8-digit candidates
        // are 5E-12 away and admissible; the canonical coefficient 24414062 is even.
        val cases = listOf(
            Triple(0x0f800000, "12621775", 36), Triple(0x6b000000, "15474251", -19),
            Triple(0x6c800000, "12379401", -20), Triple(0x39800000, "24414062", 11),
            Triple(0x007fffff, "11754942", 45), Triple(0x00800000, "11754944", 45),
            Triple(0x3f7fffff, "99999994", 8), Triple(0x3f800000, "10", 1),
            Triple(0x3f800001, "10000001", 7), Triple(0x4b7fffff, "16777215", 0),
            Triple(0x4b800000, "16777216", 0), Triple(0x4b800001, "16777218", 0),
            Triple(0x7f7fffff, "34028235", -31))
        for ((bits, coefficient, scale) in cases) {
            assertDecimal(coefficient, scale, Float.fromBits(bits).toBigDecimal(), "bits=$bits")
            assertDecimal("-$coefficient", scale, Float.fromBits(bits or Int.MIN_VALUE).toBigDecimal(), "negative bits=$bits")
        }
    }

    @Test fun smallSubnormalsUseTheUnionOfOneAndTwoDigitDecimals() {
        // A length-one-only pass would choose 1E-45 for the minimum Float;
        // the specification instead admits lengths one and two, selecting 1.4E-45.
        for ((bits, coefficient, scale) in listOf(
            Triple(1, "14", 46), Triple(2, "28", 46), Triple(3, "42", 46),
            Triple(4, "56", 46), Triple(5, "70", 46), Triple(7, "98", 46), Triple(8, "11", 45))) {
            assertDecimal(coefficient, scale, Float.fromBits(bits).toBigDecimal(), "subnormal bits=$bits")
            assertDecimal("-$coefficient", scale, Float.fromBits(bits or Int.MIN_VALUE).toBigDecimal())
        }
        assertDecimal("49", 325, Double.MIN_VALUE.toBigDecimal())
        assertDecimal("99", 325, Double.fromBits(2).toBigDecimal())
    }

    @Test fun floatTextDoesNotUseWidenedDoubleOrTheExactBinaryConstructor() {
        assertDecimal("1", 1, Float.fromBits(0x3dcccccd).toBigDecimal())
        val widened: BigDecimal = 0.10000000149011612.toBigDecimal()
        assertDecimal("10000000149011612", 17, widened)
        assertNotEquals(widened, 0.1f.toBigDecimal())
        val exact = BigDecimal(0.1)
        assertDecimal("1000000000000000055511151231257827021181583404541015625", 55, exact)
        assertNotEquals(exact, 0.1.toBigDecimal())
        assertDecimal("7120236347223045", 322, Double.fromBits(0x0060000000000000L).toBigDecimal())
        assertDecimal("17976931348623157", -292, Double.MAX_VALUE.toBigDecimal())
    }

    @Test fun contextsRoundTheSelectedDecimalWithAllEightModes() {
        val expectations = listOf(
            Triple(RoundingMode.UP, "13", "-13"), Triple(RoundingMode.DOWN, "12", "-12"),
            Triple(RoundingMode.CEILING, "13", "-12"), Triple(RoundingMode.FLOOR, "12", "-13"),
            Triple(RoundingMode.HALF_UP, "13", "-13"), Triple(RoundingMode.HALF_DOWN, "12", "-12"),
            Triple(RoundingMode.HALF_EVEN, "12", "-12"))
        for ((mode, positive, negative) in expectations) {
            val mc = MathContext(2, mode)
            val float: BigDecimal = 1.25f.toBigDecimal(mathContext = mc)
            val double: BigDecimal = 1.25.toBigDecimal(mathContext = mc)
            assertDecimal(positive, 1, float, mode.toString())
            assertDecimal(positive, 1, double, mode.toString())
            assertDecimal(negative, 1, (-1.25f).toBigDecimal(mc), mode.toString())
            assertDecimal(negative, 1, (-1.25).toBigDecimal(mc), mode.toString())
        }
        val exact = MathContext(2, RoundingMode.UNNECESSARY)
        assertFailsWith<ArithmeticException> { 1.25f.toBigDecimal(mathContext = exact) }
        assertFailsWith<ArithmeticException> { 1.25.toBigDecimal(mathContext = exact) }
        assertDecimal("12", 1, 1.2f.toBigDecimal(exact))
        assertDecimal("12", 1, 1.2.toBigDecimal(exact))
        for (mode in RoundingMode.entries) {
            val unlimited = MathContext(0, mode)
            assertDecimal("125", 2, 1.25f.toBigDecimal(unlimited))
            assertDecimal("125", 2, 1.25.toBigDecimal(unlimited))
            assertDecimal("0", 1, (-0f).toBigDecimal(MathContext(1, mode)))
            assertDecimal("0", 1, (-0.0).toBigDecimal(MathContext(1, mode)))
        }
        val carry = MathContext(2, RoundingMode.HALF_UP)
        assertDecimal("10", 0, 9.99f.toBigDecimal(carry))
        assertDecimal("10", 0, 9.99.toBigDecimal(carry))
    }

    @Test fun nonfiniteInputsRetainNumberFormatErrorsWithAndWithoutContext() {
        val context = MathContext(1, RoundingMode.UNNECESSARY)
        for (value in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
            Float.fromBits(0x7f800001), Float.fromBits(0x7fc00001 or Int.MIN_VALUE))) {
            assertFailsWith<NumberFormatException> { value.toBigDecimal() }
            assertFailsWith<NumberFormatException> { value.toBigDecimal(mathContext = context) }
        }
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            Double.fromBits(0x7ff0000000000001L), Double.fromBits(0x7ff8000000000001L or Long.MIN_VALUE))) {
            assertFailsWith<NumberFormatException> { value.toBigDecimal() }
            assertFailsWith<NumberFormatException> { value.toBigDecimal(mathContext = context) }
        }
    }
}

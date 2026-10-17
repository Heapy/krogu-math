package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompactDecimal529Test {
    private class Marker : RuntimeException()

    @Test fun accumulatorCompositionPreservesCohortsAndFailedAddAtomicity() {
        val cohorts = listOf(
            listOf(BigDecimal("1.20"), BigDecimal("2.30"), BigDecimal("-3.50")),
            listOf(BigDecimal.valueOf(Long.MAX_VALUE), BigDecimal.ONE, BigDecimal.ONE.negate()),
            listOf(BigDecimal.valueOf(Long.MIN_VALUE), BigDecimal.ONE, BigDecimal("2.00")),
        )
        assertTrue(BigDecimal("1.20").accumulatorCompactEligible529())
        assertFalse(BigDecimal.valueOf(Long.MIN_VALUE).accumulatorCompactEligible529())
        assertFalse(object : BigDecimal(1L) {}.accumulatorCompactEligible529())
        for (values in cohorts) for (context in listOf(MathContext.UNLIMITED, MathContext(7))) {
            for (policy in DecimalRounding.entries) {
                val accumulator = DecimalAccumulator(context, policy)
                var exact = values.first()
                var step = values.first().round(context)
                accumulator.add(values.first())
                val firstSnapshot = accumulator.snapshot()
                for (next in values.drop(1)) {
                    exact = exact.add(next)
                    step = step.add(next, context)
                    accumulator.add(next)
                }
                val expected = if (policy == DecimalRounding.FINAL_RESULT) exact.round(context) else step
                assertEquals(expected, accumulator.snapshot(), "$values/$context/$policy")
                assertEquals(values.first().round(context), firstSnapshot)
            }
        }
        val bounded = DecimalAccumulator(maxTerms = 1L)
        bounded.add(BigDecimal("1.20"))
        val retained = bounded.snapshot()
        assertFailsWith<ArithmeticException> { bounded.add(BigDecimal.ONE) }
        assertEquals(retained, bounded.snapshot())
    }

    @Test fun physicallyCompactMinRetainsComparisonCallbacks() {
        val ordinary = BigDecimal.valueOf(Long.MIN_VALUE)
        val poison = object : BigDecimal(Long.MIN_VALUE) {
            override fun signum(): Int = throw Marker()
        }
        assertFailsWith<Marker> { poison.compareTo(ordinary) }
        assertFailsWith<Marker> { ordinary.compareTo(poison) }
    }

    private fun fields(coefficient: String, scale: Int, value: BigDecimal) {
        assertEquals(coefficient, value.unscaledValue().toString())
        assertEquals(scale, value.scale())
    }

    @Test fun fullLongConstructionPromotionAndDemotion() {
        for (value in listOf(Long.MIN_VALUE, Long.MAX_VALUE, -1L, 0L, 1L)) {
            for (scale in listOf(Int.MIN_VALUE, -19, 0, 19, Int.MAX_VALUE)) {
                val decimal = BigDecimal.valueOf(value, scale)
                assertTrue(decimal.hasCompactStorage529())
                fields(value.toString(), scale, decimal)
                assertEquals(BigDecimal(BigInteger.valueOf(value), scale), decimal)
                assertEquals(BigDecimal(decimal.toString()), decimal)
                assertEquals(BigDecimal(decimal.toString().toCharArray()), decimal)
            }
        }
        val boundary = BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE)
        fields("9223372036854775808", 0, boundary)
        assertFalse(boundary.hasCompactStorage529())
        val returned = boundary.subtract(BigDecimal.ONE)
        assertTrue(returned.hasCompactStorage529())
        fields(Long.MAX_VALUE.toString(), 0, returned)
        val min = BigDecimal.valueOf(Long.MIN_VALUE)
        assertFalse(min.negate().hasCompactStorage529())
        assertEquals(min, min.negate().negate())
        fields("18446744073709551616", 0, min.multiply(BigDecimal.valueOf(-2)))
    }

    @Test fun finiteTextContextPublishesRoundedCompactEndpoints() {
        val exact = MathContext(19, RoundingMode.UNNECESSARY)
        val min = BigDecimal(Long.MIN_VALUE.toString(), exact)
        assertTrue(min.hasCompactStorage529())
        fields(Long.MIN_VALUE.toString(), 0, min)
        val max = BigDecimal(Long.MAX_VALUE.toString().toCharArray(), exact)
        assertTrue(max.hasCompactStorage529())
        fields(Long.MAX_VALUE.toString(), 0, max)
        val beyond = BigDecimal("9223372036854775808", exact)
        assertFalse(beyond.hasCompactStorage529())
        fields("9223372036854775808", 0, beyond)
        fields("10", -2, BigDecimal("999", MathContext(2, RoundingMode.HALF_UP)))
        fields("123", 2, BigDecimal("1.234", MathContext(3, RoundingMode.DOWN)))
        assertFailsWith<ArithmeticException> { BigDecimal("1.234", MathContext(3, RoundingMode.UNNECESSARY)) }
    }

    @Test fun roundingScaleAndTrailingZeroBoundaries() {
        val min = BigDecimal.valueOf(Long.MIN_VALUE)
        fields("-9223372036854775808", 0, min.stripTrailingZeros())
        fields("-9", -18, min.round(MathContext(1, RoundingMode.HALF_EVEN)))
        fields("-922337203685477581", -1, min.setScale(-1, RoundingMode.FLOOR))
        fields("-922337203685477580", -1, min.setScale(-1, RoundingMode.CEILING))
        fields("-1", -19, min.setScale(-19, RoundingMode.HALF_UP))
        fields("0", -20, min.setScale(-20, RoundingMode.HALF_UP))
        assertFailsWith<ArithmeticException> { min.setScale(-1, RoundingMode.UNNECESSARY) }
        fields("123", -2, BigDecimal.valueOf(12300).stripTrailingZeros())
        assertFailsWith<ArithmeticException> { BigDecimal.valueOf(10, Int.MIN_VALUE).stripTrailingZeros() }
        fields("1", Int.MIN_VALUE, BigDecimal.valueOf(1, Int.MIN_VALUE).stripTrailingZeros())
        fields("0", 0, BigDecimal.valueOf(0, 19).round(MathContext.UNLIMITED))
        fields("0", 19, BigDecimal.valueOf(0, 19).multiply(BigDecimal.ONE))
    }

    @Test fun compactArithmeticKeepsCohortsAndOutput() {
        fields("12345", 2, BigDecimal("120.00").add(BigDecimal("3.45")))
        fields("0", 2, BigDecimal("1.00").subtract(BigDecimal.ONE))
        fields("25000", 4, BigDecimal("1.25").multiply(BigDecimal("2.00")))
        fields("333", 3, BigDecimal.ONE.divide(BigDecimal.valueOf(3), 3, RoundingMode.DOWN))
        fields("-333", 3, BigDecimal.ONE.negate().divide(BigDecimal.valueOf(3), 3, RoundingMode.CEILING))
        fields("2", 0, BigDecimal.valueOf(5).divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_EVEN))
        fields("4", 0, BigDecimal.valueOf(7).divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_EVEN))
        val value = BigDecimal.valueOf(Long.MIN_VALUE, 3)
        for (format in DecimalTextFormat.entries) {
            val expected = when (format) {
                DecimalTextFormat.CANONICAL -> value.toString()
                DecimalTextFormat.PLAIN -> value.toPlainString()
                DecimalTextFormat.ENGINEERING -> value.toEngineeringString()
            }
            val append = StringBuilder()
            assertEquals(expected.length.toLong(), value.appendTo(append, format))
            assertEquals(expected, append.toString())
            val chars = CharArray(expected.length)
            assertEquals(chars.size, value.writeTo(chars, format = format))
            assertEquals(expected, chars.concatToString())
        }
    }
}

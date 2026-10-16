package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DecimalMetadata597Test {
    @Test fun decimalPowerNeighborsCrossCachedAndFallbackThresholds() {
        for (exponent in listOf(0, 1, 18, 19, 33, 34, 127, 128, 129, 256)) {
            val power = BigInteger("1" + "0".repeat(exponent))
            for ((coefficient, digits) in listOf(power.subtract(BigInteger.ONE) to maxOf(1, exponent),
                power to (exponent + 1), power.add(BigInteger.ONE) to (exponent + 1))) {
                for (signed in listOf(coefficient, coefficient.negate())) {
                    for (scale in listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE)) {
                        val value = BigDecimal(signed, scale)
                        repeat(3) { assertEquals(digits, value.precision()) }
                        assertEquals(scale, value.scale())
                        assertEquals(signed, value.unscaledValue())
                    }
                }
            }
            val scaled = BigDecimal.ONE.setScale(exponent)
            assertEquals(power, scaled.unscaledValue())
            assertEquals(exponent, scaled.scale())
            assertEquals(BigDecimal.ONE, scaled.setScale(0, RoundingMode.UNNECESSARY))
        }
    }

    @Test fun callerPrecisionOverrideStillDispatchesAfterBaseMetadataWarms() {
        val marker = IllegalStateException("precision callback")
        val value = object : BigDecimal("123456789012345678901234567890.123") {
            var fail = false
            override fun precision(): Int = if (fail) throw marker else super.precision()
        }
        repeat(3) { assertEquals(33, value.precision()) }
        value.fail = true
        val actual = try { value.round(MathContext(7)); null } catch (problem: IllegalStateException) { problem }
        assertSame(marker, actual)
    }
    @Test fun compactNormalizationHandlesEqualPrefixesSignsAndLongBoundary() {
        val cases = listOf(Triple("1", "1000", "0.001"), Triple("1000", "1", "1000"),
            Triple("1001", "1000", "1.001"), Triple("1230", "123", "10"),
            Triple("9223372036854775807", "1", "9223372036854775807"),
            Triple("9223372036854775807", "9223372036854775807", "1"))
        for ((a, b, expected) in cases) for (scale in listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE)) {
            for (sign in listOf(1, -1)) {
                val left = BigDecimal(BigInteger(if (sign < 0) "-" + a else a), scale)
                val right = BigDecimal(BigInteger(b), scale)
                val actual = left.divide(right, MathContext(34, RoundingMode.UNNECESSARY))
                val control = BigDecimal(if (sign < 0) "-" + expected else expected)
                assertEquals(control, actual, "$a/$b/$scale/$sign")
            }
        }
    }

}

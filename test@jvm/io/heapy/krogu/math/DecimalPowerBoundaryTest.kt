package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecimalPowerBoundaryTest {
    @Test fun workingPrecisionOverflowSeparatesReferenceObservationsFromRequiredValues() {
        fun outcome(block: () -> Any): String = try { block().toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
        for (precision in listOf(Int.MAX_VALUE, Int.MAX_VALUE - 1, Int.MAX_VALUE - 2, Int.MAX_VALUE - 10)) {
            for (power in listOf(0, 1, 10, 999999999)) for (mode in RoundingMode.entries) {
                val reference = outcome { java.math.BigDecimal.ONE.pow(power,
                    java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))) }
                // Keep all 128 reference values/errors, including exact messages,
                // as diagnostics. They do not require an internal Int overflow.
                println("POW_WORKING_PRECISION_REFERENCE\t1@0\t$precision\t$power\t$mode\t$reference")
                val label = "precision=$precision power=$power mode=$mode"
                val referenceOverflow = power != 0 &&
                    precision.toLong() + power.toString().length + 1 > Int.MAX_VALUE
                if (referenceOverflow) assertTrue(reference.startsWith("!IllegalArgumentException:"), label)
                else assertEquals("1", reference, label)
                val actual = BigDecimal.ONE.pow(power, MathContext(precision, mode))
                assertEquals(BigInteger.ONE, actual.unscaledValue(), label)
                assertEquals(0, actual.scale(), label)
            }
        }
    }

    @Test fun accumulatorRoundingAcrossExponentBitPatternsMatchesJdk() {
        val inputs = listOf("0.00", "1.0000000000000001", "0.9999999999999999",
            "-1.0000000000000001", "-0.9999999999999999", "2.5000000000000001",
            "9.9999999999999999", "1E-10", "1E+10", "0E+2147483647", "0E-2147483647")
        val powers = listOf(0, 1, 2, 3, 7, 15, 16, 17, 31, 32, 33, 127, 128, 129,
            999, 1000, 1023, 1024, 999999999).flatMap { if (it == 0) listOf(0) else listOf(it, -it) }
        fun outcome(block: () -> Any): String = try { block().toString() }
            catch (failure: Exception) { "!${failure.javaClass.simpleName}" }
        for (input in inputs) for (power in powers) for (precision in listOf(9, 16, 34, 65)) {
            for (mode in RoundingMode.entries) {
                val expected = outcome { java.math.BigDecimal(input).pow(power,
                    java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))) }
                val actual = outcome { BigDecimal(input).pow(power, MathContext(precision, mode)) }
                assertEquals(expected, actual, "$input pow=$power precision=$precision mode=$mode")
            }
        }
    }
}

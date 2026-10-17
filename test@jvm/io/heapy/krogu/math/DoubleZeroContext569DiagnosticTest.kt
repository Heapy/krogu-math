// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.math.BigDecimal as JD
import java.math.MathContext as JC
import java.math.RoundingMode as JR
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Prepared public-API replay, not evidence of an executed host probe. */
class DoubleZeroContext569DiagnosticTest {
    @BeforeTest fun pinnedOracle() {
        assertEquals("BellSoft", System.getProperty("java.vendor"))
        assertEquals("25.0.4+9-LTS", System.getProperty("java.runtime.version"))
    }

    private data class Outcome(val coefficient: String? = null, val scale: Int? = null, val exception: String? = null)
    private fun observe(action: () -> Any?): Outcome = try {
        when (val result = action()) {
            is JD -> Outcome(result.unscaledValue().toString(), result.scale())
            is BigDecimal -> Outcome(result.unscaledValue().toString(), result.scale())
            null -> Outcome(exception = "returned-null")
            else -> error("Unexpected result type")
        }
    } catch (e: Exception) { Outcome(exception = e.javaClass.simpleName) }

    @Test fun signedZeroNullObservationsStaySeparateFromRequiredNpe() {
        val seen = mutableSetOf<Long>()
        val ownContext: MathContext? = null
        val hostContext: JC? = null
        for (value in listOf(0.0, -0.0)) {
            val key = value.toRawBits()
            assertTrue(seen.add(key), "Duplicate signed-zero bits: $key")
            val rawHost = observe { JD(value, hostContext) }
            val actual = observe { Validation578Boundary.decimal(value, ownContext) }
            println("doubleBits=$key/null rawHost=$rawHost required=NullPointerException actual=$actual")
            // Existing frozen null-oracle rows record zero for these two exact inputs.
            assertEquals(Outcome("0", 0), rawHost, "$key historical host observation")
            assertEquals(Outcome(exception = "NullPointerException"), actual, "$key public null contract")
        }
        assertEquals(setOf(0L, Long.MIN_VALUE), seen)
        assertEquals(2, seen.size)
    }

    @Test fun validContextsKeepIndependentZeroFieldsAndPublicJdkComparison() {
        var count = 0
        for (value in listOf(0.0, -0.0)) for (precision in listOf(0, 1, 7, Int.MAX_VALUE))
            for (mode in RoundingMode.entries) {
                val rawHost = observe { JD(value, JC(precision, JR.valueOf(mode.name))) }
                val actual = observe { BigDecimal(value, MathContext(precision, mode)) }
                val key = "doubleBits=${value.toRawBits()}/p$precision/$mode"
                println("$key rawHost=$rawHost actual=$actual")
                assertEquals(Outcome("0", 0), actual, "$key independent coefficient/scale")
                assertEquals(rawHost, actual, "$key public JDK comparison")
                count++
            }
        assertEquals(64, count)
    }

    @Test fun retainedFiniteNullAndNonfiniteObservations() {
        // Literal historical observations from the unchanged seven-row Double/null oracle cohort.
        for (value in listOf(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.1, 1.0)) {
            val historical = if (value.isFinite()) "NullPointerException" else "NumberFormatException"
            val hostContext: JC? = null
            val rawHost = observe { JD(value, hostContext) }
            val actual = observe { Validation578Boundary.decimal(value, null) }
            println("doubleBits=${value.toRawBits()}/null rawHost=$rawHost actual=$actual")
            assertEquals(Outcome(exception = historical), rawHost, "Historical host observation for $value")
            if (value.isFinite()) assertEquals(Outcome(exception = "NullPointerException"), actual, "$value null contract")
            else assertTrue(actual.exception != null, "$value compound-invalid rejection; priority is diagnostic")
        }
        // Finite control with real rounding and a separately specified decimal result.
        val rawHost = observe { JD(1.25, JC(2, JR.HALF_EVEN)) }
        val actual = observe { BigDecimal(1.25, MathContext(2, RoundingMode.HALF_EVEN)) }
        assertEquals(Outcome("12", 1), actual)
        assertEquals(rawHost, actual)
    }
}

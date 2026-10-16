// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalScaling533JvmTest {
    @Test
    fun signedCompactDivisionRetainsEveryRoundingMode() {
        fun outcome(operation: () -> String): String = try { operation() }
            catch (failure: ArithmeticException) { "!${failure::class.simpleName}" }
        for (numerator in listOf("-9223372036854775808", "-9223372036854775807", "-251", "-250", "-149", "0", "149", "250", "251", "9223372036854775807"))
            for (denominator in listOf("-100", "-10", "-3", "-1", "0", "1", "3", "10", "100"))
                for (scale in listOf(-1, 0, 1)) for (mode in RoundingMode.entries) {
                    assertEquals(outcome {
                        java.math.BigDecimal(numerator).divide(java.math.BigDecimal(denominator), scale,
                            java.math.RoundingMode.valueOf(mode.name)).let { "${it.unscaledValue()}@${it.scale()}" }
                    }, outcome {
                        BigDecimal(numerator).divide(BigDecimal(denominator), scale, mode)
                            .let { "${it.unscaledValue()}@${it.scale()}" }
                    }, "$numerator / $denominator scale$scale $mode")
                }
    }

    @Test
    fun scalingRoundingAndStrippingMatchPublicJdk() {
        val random = Random(533)
        val coefficients = mutableListOf("0", "1", "-1", Long.MAX_VALUE.toString(), Long.MIN_VALUE.toString(),
            "9223372036854775808", "-9223372036854775809")
        repeat(100) { coefficients += random.nextLong().toString() }
        repeat(25) { coefficients += (if (it % 2 == 0) "-" else "") + "9".repeat(40 + it) + "0".repeat(it) }
        fun outcome(operation: () -> String): String = try { operation() }
            catch (failure: ArithmeticException) { "!${failure::class.simpleName}" }
        for (text in coefficients) for (scale in listOf(-19, 0, 19, Int.MIN_VALUE, Int.MIN_VALUE + 30, Int.MAX_VALUE)) {
            val own = BigDecimal(BigInteger(text), scale)
            val host = java.math.BigDecimal(java.math.BigInteger(text), scale)
            for (mode in RoundingMode.entries) {
                val hostMode = java.math.RoundingMode.valueOf(mode.name)
                for (precision in listOf(1, 7, 18, 19, 20, 34)) {
                    // Task567's existing direct-round zero contract is canonical
                    // scale zero; retain the pinned host's differing raw cohort
                    // in the initial-failure evidence, not as a kernel regression.
                    assertEquals(outcome { host.round(java.math.MathContext(precision, hostMode)).let {
                        if (it.signum() == 0) "0@0" else "${it.unscaledValue()}@${it.scale()}"
                    } },
                        outcome { own.round(MathContext(precision, mode)).let { "${it.unscaledValue()}@${it.scale()}" } },
                        "round $text@$scale p$precision $mode")
                }
                // Downscale only, avoiding huge genuine outputs at extreme source scales.
                for (drop in listOf(0, 1, 8, 9, 17, 18, 19, 20, 65)) {
                    val target = scale.toLong() - drop
                    if (target < Int.MIN_VALUE) continue
                    assertEquals(outcome { host.setScale(target.toInt(), hostMode).let { "${it.unscaledValue()}@${it.scale()}" } },
                        outcome { own.setScale(target.toInt(), mode).let { "${it.unscaledValue()}@${it.scale()}" } },
                        "scale $text@$scale drop$drop $mode")
                }
            }
            assertEquals(outcome { host.stripTrailingZeros().let { "${it.unscaledValue()}@${it.scale()}" } },
                outcome { own.stripTrailingZeros().let { "${it.unscaledValue()}@${it.scale()}" } }, "strip $text@$scale")
        }
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalSqrtMidpoint565JdkTest {
    private fun compare(c: String, s: Int, p: Int, mode: RoundingMode, literal: Pair<String, Int>) {
        val expected = java.math.BigDecimal(java.math.BigInteger(c), s)
            .sqrt(java.math.MathContext(p, java.math.RoundingMode.valueOf(mode.name)))
        val actual = BigDecimal(BigInteger(c), s).sqrt(MathContext(p, mode))
        val label = "$c@$s / p$p / $mode"
        assertEquals(literal.first, expected.unscaledValue().toString(), "$label independent JDK coefficient")
        assertEquals(literal.second, expected.scale(), "$label independent JDK scale")
        assertEquals(expected.unscaledValue().toString(), actual.unscaledValue().toString(), "$label coefficient")
        assertEquals(expected.scale(), actual.scale(), "$label scale")
        assertEquals(expected.toString(), actual.toString(), "$label text")
    }

    @Test fun boundedTinyRemaindersMatchPublicJdkAndLiterals() {
        for (k in listOf(1, 2, 17, 18, 19, 64, 256, 1024)) {
            val c = "4" + "0".repeat(k - 1) + "1"
            for (mode in RoundingMode.entries) {
                if (mode == RoundingMode.UNNECESSARY) {
                    assertFailsWith<ArithmeticException> {
                        java.math.BigDecimal(java.math.BigInteger(c), k)
                            .sqrt(java.math.MathContext(1, java.math.RoundingMode.UNNECESSARY))
                    }
                    assertFailsWith<ArithmeticException> { BigDecimal(BigInteger(c), k).sqrt(MathContext(1, mode)) }
                } else {
                    val expected = if (mode == RoundingMode.UP || mode == RoundingMode.CEILING) "3" else "2"
                    compare(c, k, 1, mode, expected to 0)
                }
            }
        }
    }

    @Test fun boundedNeighborsTiesAndCohortsMatchPublicJdkAndLiterals() {
        val modes = listOf(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING,
            RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN)
        for (case in decimalSqrtMidpoint565Cases()) for (padding in listOf(0, 80)) {
            val c = case.coefficient + "0".repeat(padding)
            val s = case.scale + padding
            for ((index, mode) in modes.withIndex()) compare(c, s, case.precision, mode, case.results[index])
            assertFailsWith<ArithmeticException> {
                java.math.BigDecimal(java.math.BigInteger(c), s)
                    .sqrt(java.math.MathContext(case.precision, java.math.RoundingMode.UNNECESSARY))
            }
            assertFailsWith<ArithmeticException> { BigDecimal(BigInteger(c), s).sqrt(MathContext(case.precision, RoundingMode.UNNECESSARY)) }
        }
    }
}

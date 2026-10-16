// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DecimalSquareRootCohort587Test {
    private class Truthful(text: String) : BigDecimal(text) {
        override fun signum(): Int = super.signum()
        override fun precision(): Int = super.precision()
        override fun scale(): Int = super.scale()
        override fun unscaledValue(): BigInteger = super.unscaledValue()
        fun callerSuperRoot(context: MathContext): BigDecimal = super.sqrt(context)
    }

    private fun assertFields(c: String, s: Int, p: Int, text: String,
        actual: BigDecimal, label: String) {
        assertEquals(c, actual.unscaledValue().toString(), "$label coefficient")
        assertEquals(s, actual.scale(), "$label scale")
        assertEquals(p, actual.precision(), "$label precision")
        assertEquals(text, actual.toString(), "$label text")
    }

    @Test fun independentLiteralGridForOrdinaryAndTruthfulCallerSuper() {
        // Exactly54 own primary calls:3 inputs*(8 p8 modes+1 p0 mode)*2 routes.
        // The midpoint1.99999995 squared is3.9999998000000025<3.999999999999.
        // For sqrt2,1.41421355 squared is1.9999999650036025<2; adjacent
        // p8 grid squares bracket2. No numerical implementation is the oracle.
        var calls = 0
        for (text in listOf("3.999999999999", "4.000000000000", "2"))
            for (precision in listOf(8, 0))
                for (mode in if (precision == 0) listOf(RoundingMode.HALF_EVEN) else RoundingMode.entries)
                    for (truthful in listOf(false, true)) {
                        val context = MathContext(precision, mode)
                        val label = "$text / p$precision / $mode / truthful=$truthful"
                        fun root(): BigDecimal {
                            calls++
                            return if (truthful) Truthful(text).callerSuperRoot(context)
                                else BigDecimal(text).sqrt(context)
                        }
                        if (text != "4.000000000000" &&
                            (precision == 0 || mode == RoundingMode.UNNECESSARY)) {
                            assertFailsWith<ArithmeticException>(label) { root() }
                        } else if (text == "4.000000000000") {
                            assertFields("2000000", 6, 7, "2.000000", root(), label)
                        } else {
                            val down = mode == RoundingMode.DOWN || mode == RoundingMode.FLOOR
                            if (text == "2") {
                                assertFields(if (down) "14142135" else "14142136", 7, 8,
                                    if (down) "1.4142135" else "1.4142136", root(), label)
                            } else {
                                assertFields(if (down) "19999999" else "20000000", 7, 8,
                                    if (down) "1.9999999" else "2.0000000", root(), label)
                            }
                        }
                    }
        assertEquals(54, calls)
    }

    @Test fun rawInexactZeroTailAndCarryKeepAllSignificantDigits() {
        // Direct raw entry is necessary for bounded fallback coverage: ordinary
        // public inputs below the cheap gate intentionally stay materialized.
        for (mode in RoundingMode.entries) {
            val label = "raw sqrt(3.999999999999) / $mode"
            if (mode == RoundingMode.UNNECESSARY) {
                assertFailsWith<ArithmeticException>(label) {
                    DecimalSquareRoot579.normalized(BigInteger("3999999999999"), 2, 8, 7, 6, mode)
                }
            } else {
                val down = mode == RoundingMode.DOWN || mode == RoundingMode.FLOOR
                val result = DecimalSquareRoot579.normalized(BigInteger("3999999999999"), 2, 8, 7, 6, mode)
                assertEquals(if (down) "19999999" else "20000000", result.first.toString(), label)
                assertEquals(7, result.second, label)
            }
        }
        // 999^2<999990<1000^2 and999.5^2<999990: a three-nine carry
        // becomes100 (three digits), not1. All products fit independent Long.
        for (mode in listOf(RoundingMode.UP, RoundingMode.CEILING,
            RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN)) {
            val result = DecimalSquareRoot579.normalized(BigInteger("99999"), 1, 3, 6, 0, mode)
            assertEquals(BigInteger("100"), result.first, "carry $mode coefficient")
            assertEquals(5, result.second, "carry $mode scale")
        }
    }

    @Test fun exactFiniteAndZeroCohortsBypassInexactDigitLimit() {
        for (mode in RoundingMode.entries) {
            for (precision in listOf(0, 8, Int.MAX_VALUE)) {
                assertFields("2000000", 6, 7, "2.000000",
                    BigDecimal("4.000000000000").sqrt(MathContext(precision, mode)),
                    "finite p$precision / $mode")
                for (scale in listOf(Int.MIN_VALUE, -5, 0, 5, Int.MAX_VALUE)) {
                    val actual = BigDecimal(BigInteger.ZERO, scale).sqrt(MathContext(precision, mode))
                    assertEquals(BigInteger.ZERO, actual.unscaledValue())
                    assertEquals(scale / 2, actual.scale())
                    assertEquals(1, actual.precision())
                }
            }
        }
        // Existing direct exact raw clients still strip before scale checking;
        // this bounded coefficient has an intentionally Long working scale.
        val rescued = DecimalSquareRoot579.normalized(BigInteger.ONE, 20, 11,
            Int.MAX_VALUE.toLong() + 5, Int.MAX_VALUE, RoundingMode.UNNECESSARY)
        assertEquals(BigInteger("100000"), rescued.first)
        assertEquals(Int.MAX_VALUE, rescued.second)
    }

    @Test fun naturalInexactDigitBoundaryAndRequiredRawEligibility() {
        // Real public size bound, not a synthetic smaller production cap.
        assertFalse(DecimalSquareRoot579.inexactPrecisionOverflows(1))
        assertFalse(DecimalSquareRoot579.inexactPrecisionOverflows(400000000))
        assertFalse(DecimalSquareRoot579.inexactPrecisionOverflows(646456993))
        assertTrue(DecimalSquareRoot579.inexactPrecisionOverflows(646456994))
        assertTrue(DecimalSquareRoot579.inexactPrecisionOverflows(Int.MAX_VALUE))
        assertFalse(DecimalSquareRoot579.canMaterialize(1, 799999998, 399999999))
        // Original public scaleOverflowTest input: sqrt10 is irrational, so
        // p=1073741825 requires an unsupported coefficient under literal p.
        assertFailsWith<ArithmeticException> {
            BigDecimal.valueOf(1, -1).sqrt(MathContext(1073741825, RoundingMode.UP))
        }
        // Tiny inputs establish the reached rejection BEFORE any huge work.
        // No input has huge coefficient/scale storage; no p=H raw run is made.
        for (precision in listOf(646456994, Int.MAX_VALUE)) for (mode in RoundingMode.entries) {
            assertFailsWith<ArithmeticException>("sqrt2 / p$precision / $mode") {
                BigDecimal.TWO.sqrt(MathContext(precision, mode))
            }
        }
    }
}

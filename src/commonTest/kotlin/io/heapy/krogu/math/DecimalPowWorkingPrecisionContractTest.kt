// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class DecimalPowWorkingPrecisionContractTest {
    private val boundaryPrecisions = (Int.MAX_VALUE - 10..Int.MAX_VALUE).toList()

    private fun assertRepresentation(coefficient: String, scale: Int, actual: BigDecimal, label: String) {
        assertEquals(coefficient, actual.unscaledValue().toString(), label)
        assertEquals(scale, actual.scale(), label)
    }

    @Test fun unitPowersSurviveEveryWorkingPrecisionBoundary() {
        val powers = listOf(0, 1, -1, 9, -9, 10, -10, 99, -99, 100, -100, 999999999, -999999999)
        for (precision in boundaryPrecisions) for (mode in RoundingMode.entries) for (power in powers) {
            val context = MathContext(precision, mode)
            assertRepresentation("1", 0, BigDecimal.ONE.pow(power, context), "$precision/$mode/$power")
            val sign = if (power and 1 == 0) "1" else "-1"
            assertRepresentation(sign, 0, BigDecimal("-1").pow(power, context), "negative/$precision/$mode/$power")
        }
    }

    @Test fun exactMonomialsKeepTheirScaleForEitherExponentSign() {
        for (scale in -2..2) for (power in listOf(-7, -2, -1, 0, 1, 2, 7))
            for (precision in boundaryPrecisions) for (mode in RoundingMode.entries) {
                val value = BigDecimal(BigInteger.ONE, scale)
                assertRepresentation("1", scale * power, value.pow(power, MathContext(precision, mode)),
                    "scale=$scale power=$power precision=$precision mode=$mode")
            }
        for (scale in listOf(Int.MIN_VALUE + 1, Int.MAX_VALUE)) for (power in listOf(-1, 1))
            for (mode in RoundingMode.entries) {
                assertRepresentation("1", scale * power,
                    BigDecimal(BigInteger.ONE, scale).pow(power, MathContext(Int.MAX_VALUE, mode)),
                    "extreme scale=$scale power=$power mode=$mode")
            }
    }

    @Test fun exactProductsAndReciprocalsHaveIndependentCoefficientScaleControls() {
        data class Case(val input: String, val power: Int, val coefficient: String, val scale: Int)
        val cases = listOf(
            Case("2", -1, "5", 1), Case("5", -1, "2", 1), Case("10", -1, "1", 1),
            Case("2", -3, "125", 3), Case("5", -3, "8", 3), Case("10", -3, "1", 3),
            Case("-2", -3, "-125", 3), Case("0.5", -3, "8", 0),
            Case("2.0", 3, "8000", 3), Case("1.00", 3, "1000000", 6), Case("1.00", -3, "1", 0),
        )
        for (case in cases) for (precision in boundaryPrecisions) for (mode in RoundingMode.entries) {
            assertRepresentation(case.coefficient, case.scale,
                BigDecimal(case.input).pow(case.power, MathContext(precision, mode)),
                "${case.input}/${case.power}/$precision/$mode")
        }
    }

    @Test fun zeroPowersRetainTheirArithmeticAndSaturatedScaleRules() {
        for (scale in listOf(Int.MIN_VALUE, -3, 0, 3, Int.MAX_VALUE)) for (mode in RoundingMode.entries) {
            val zero = BigDecimal(BigInteger.ZERO, scale)
            val context = MathContext(Int.MAX_VALUE, mode)
            assertSame(BigDecimal.ONE, zero.pow(0, context))
            for (power in listOf(1, 2, 17)) {
                val expectedScale = (scale.toLong() * power).coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
                assertRepresentation("0", expectedScale, zero.pow(power, context), "$scale/$power/$mode")
            }
            assertFailsWith<ArithmeticException> { zero.pow(-1, context) }
        }
    }

    @Test fun exponentAndUnlimitedContextValidationStillApplies() {
        for (power in listOf(Int.MIN_VALUE, -1000000000, 1000000000, Int.MAX_VALUE)) {
            assertFailsWith<ArithmeticException> { BigDecimal.ONE.pow(power, MathContext(Int.MAX_VALUE)) }
        }
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.pow(10, MathContext(1)) }
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.pow(-1, MathContext.UNLIMITED) }
        assertRepresentation("1", 0, BigDecimal.ONE.pow(1, MathContext.UNLIMITED), "unlimited")
    }

    @Test fun explicitCallerDispatchSuperAndExceptionIdentityRemainAvailable() {
        val context = MathContext(Int.MAX_VALUE)
        class Truthful : BigDecimal("1") {
            fun enter(): BigDecimal = super.pow(1, context)
        }
        assertRepresentation("1", 0, Truthful().enter(), "caller super")
        val replacement = BigDecimal("7.00")
        val caller: BigDecimal = object : BigDecimal("1") {
            override fun pow(n: Int, mc: MathContext): BigDecimal {
                assertEquals(1, n)
                assertSame(context, mc)
                return replacement
            }
        }
        assertSame(replacement, caller.pow(1, context))
        val failure = IllegalStateException("caller-owned")
        val throwing: BigDecimal = object : BigDecimal("1") {
            override fun pow(n: Int, mc: MathContext): BigDecimal = throw failure
        }
        assertSame(failure, assertFailsWith<IllegalStateException> { throwing.pow(1, context) })
    }
}

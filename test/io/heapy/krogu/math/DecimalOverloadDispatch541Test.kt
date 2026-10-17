// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/** SOURCE ONLY: bounded ordinary/truthful controls for the 39-entry inventory. */
class DecimalOverloadDispatch541Test {
    private class Truthful(text: String) : BigDecimal(text) {
        override fun unscaledValue(): BigInteger = super.unscaledValue()
        override fun scale(): Int = super.scale()
        override fun precision(): Int = super.precision()
        override fun signum(): Int = super.signum()

        fun addViaSuper(other: BigDecimal): BigDecimal = super.add(other)
        fun divideViaSuper(other: BigDecimal, mode: RoundingMode): BigDecimal = super.divide(other, mode)
        fun integralViaSuper(other: BigDecimal, mc: MathContext): BigDecimal =
            super.divideToIntegralValue(other, mc)
        fun plusViaSuper(): BigDecimal = super.plus()
        fun textViaSuper(): String = super.toString()
    }

    private fun representation(coefficient: String, scale: Int, actual: BigDecimal) {
        assertEquals(coefficient, actual.unscaledValue().toString())
        assertEquals(scale, actual.scale())
    }

    @Test
    fun binaryDivisionIntegralAndScaleRowsCoverOrdinaryAndTruthfulReceivers() {
        for (source in listOf<BigDecimal>(BigDecimal("1.20"), Truthful("1.20"))) {
            val equal = BigDecimal("1.20")
            representation("240", 2, source.add(equal))
            representation("0", 2, source.subtract(equal))
            representation("240", 2, source.multiply(BigDecimal("2")))
            representation("240", 2, source.multiply(BigDecimal("2"), MathContext(3, RoundingMode.HALF_UP)))
            representation("300", 2, source.add(BigDecimal("1.80"), MathContext(3, RoundingMode.HALF_UP)))
            representation("-60", 2, source.subtract(BigDecimal("1.80"), MathContext(3, RoundingMode.HALF_UP)))

            val divisor = BigDecimal("0.40")
            representation("3", 0, source.divide(divisor))
            representation("300", 2, source.divide(divisor, RoundingMode.UNNECESSARY))
            representation("300", 2, source.divide(divisor, 0))
            representation("3", 0, source.divide(divisor, 0, 0))
            representation("3", 0, source.divide(divisor, 0, RoundingMode.UNNECESSARY))
            representation("3", 0, source.divide(divisor, MathContext(2, RoundingMode.UNNECESSARY)))

            representation("3", 0, source.divideToIntegralValue(divisor))
            representation("3", 0, source.divideToIntegralValue(divisor, MathContext(1, RoundingMode.DOWN)))
            representation("0", 2, source.remainder(divisor))
            representation("0", 2, source.remainder(divisor, MathContext(1, RoundingMode.DOWN)))
            val exactPair = source.divideAndRemainder(divisor)
            assertEquals(2, exactPair.size)
            representation("3", 0, exactPair[0])
            representation("0", 2, exactPair[1])
            val pair = source.divideAndRemainder(divisor, MathContext(1, RoundingMode.DOWN))
            assertEquals(2, pair.size)
            representation("3", 0, pair[0])
            representation("0", 2, pair[1])

            representation("1200", 3, source.setScale(3))
            representation("1200", 3, source.setScale(3, 0))
            representation("1200", 3, source.setScale(3, RoundingMode.UNNECESSARY))
            representation("1200", 0, source.movePointRight(3))
            representation("120", 3, source.movePointLeft(1))
            representation("120", 1, source.scaleByPowerOfTen(1))
        }
        assertFailsWith<ArithmeticException> { BigDecimal("1").setScale(-1) }
        assertFailsWith<ArithmeticException> { BigDecimal("1").divide(BigDecimal.ZERO) }
    }

    @Test
    fun unaryOrderingFormattingAndAdjacentRowsUseDocumentedFiniteControls() {
        val negative = BigDecimal("-1.20")
        representation("-12", 1, negative.round(MathContext(2, RoundingMode.HALF_UP)))
        assertSame(negative, negative.plus())
        representation("-12", 1, negative.plus(MathContext(2, RoundingMode.HALF_UP)))
        representation("120", 2, negative.abs())
        representation("120", 2, negative.negate())
        representation("12", 1, negative.abs(MathContext(2, RoundingMode.HALF_UP)))
        representation("12", 1, negative.negate(MathContext(2, RoundingMode.HALF_UP)))

        for (source in listOf<BigDecimal>(BigDecimal("1.20"), Truthful("1.20"))) {
            val equalScale = BigDecimal("1.20")
            val otherCohort = BigDecimal("1.200")
            assertEquals(0, source.compareTo(otherCohort))
            assertSame(source, source.min(equalScale))
            assertSame(source, source.max(equalScale))
            assertEquals(0, source.compareTo(equalScale))
        }

        for (text in listOf<BigDecimal>(BigDecimal("1.2300E+5"), Truthful("1.2300E+5"))) {
            assertEquals("1.2300E+5", text.toString())
            assertEquals("123000", text.toPlainString())
            assertEquals("123.00E+3", text.toEngineeringString())
            representation("1", -1, text.ulp())
        }
        assertSame(BigDecimal.ZERO, BigDecimal("0.00").stripTrailingZeros())

        val truthful = Truthful("1.20")
        assertSame(truthful, truthful.plusViaSuper())
        assertEquals("1.20", truthful.textViaSuper())
        representation("200", 2, truthful.addViaSuper(BigDecimal("0.80")))
        representation("3", 0, truthful.integralViaSuper(BigDecimal("0.40"), MathContext(2, RoundingMode.DOWN)))
        representation("300", 2, truthful.divideViaSuper(BigDecimal("0.40"), RoundingMode.UNNECESSARY))
    }

    @Test
    fun callerOwnedOverridesPreserveDispatchArgumentsResultsAndExceptions() {
        val replacement = BigDecimal("7.00")
        val failure = IllegalStateException("541 dispatch sentinel")
        val augend = BigDecimal("1.00")
        val divisor = BigDecimal("2.00")
        val mode = RoundingMode.DOWN
        class Caller : BigDecimal("12") {
            var throwing = false
            var last: String? = null
            var lastAugend: BigDecimal? = null
            var lastDivisor: BigDecimal? = null
            var lastMode: RoundingMode? = null

            override fun add(augend: BigDecimal): BigDecimal {
                last = "add"
                lastAugend = augend
                if (throwing) throw failure
                return replacement
            }
            override fun divide(divisor: BigDecimal, roundingMode: RoundingMode): BigDecimal {
                last = "divide"
                lastDivisor = divisor
                lastMode = roundingMode
                if (throwing) throw failure
                return replacement
            }
            override fun ulp(): BigDecimal {
                last = "ulp"
                if (throwing) throw failure
                return replacement
            }
        }
        val caller = Caller()
        val source: BigDecimal = caller
        assertSame(replacement, source.add(augend))
        assertEquals("add", caller.last)
        assertSame(augend, caller.lastAugend)
        assertSame(replacement, source.divide(divisor, mode))
        assertEquals("divide", caller.last)
        assertSame(divisor, caller.lastDivisor)
        assertSame(mode, caller.lastMode)
        assertSame(replacement, source.ulp())
        assertEquals("ulp", caller.last)
        caller.throwing = true
        assertSame(failure, assertFailsWith<IllegalStateException> { source.add(augend) })
        assertEquals("add", caller.last)
        assertSame(augend, caller.lastAugend)
        assertSame(failure, assertFailsWith<IllegalStateException> { source.divide(divisor, mode) })
        assertEquals("divide", caller.last)
        assertSame(divisor, caller.lastDivisor)
        assertSame(mode, caller.lastMode)
        assertSame(failure, assertFailsWith<IllegalStateException> { source.ulp() })
        assertEquals("ulp", caller.last)
    }
}

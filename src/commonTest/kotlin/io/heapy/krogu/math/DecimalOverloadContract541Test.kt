// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/** Task 541 source preparation only: not compiled or executed by its author. */
class DecimalOverloadContract541Test {
    private class Truthful(text: String) : BigDecimal(text) {
        override fun signum(): Int = super.signum()
        override fun precision(): Int = super.precision()
        override fun scale(): Int = super.scale()
        override fun unscaledValue(): BigInteger = super.unscaledValue()
        override fun divideToIntegralValue(divisor: BigDecimal?): BigDecimal = super.divideToIntegralValue(divisor)
        override fun divideToIntegralValue(divisor: BigDecimal?, mc: MathContext?): BigDecimal =
            super.divideToIntegralValue(divisor, mc)
        override fun remainder(divisor: BigDecimal?): BigDecimal = super.remainder(divisor)
        override fun remainder(divisor: BigDecimal?, mc: MathContext?): BigDecimal = super.remainder(divisor, mc)
        override fun divideAndRemainder(divisor: BigDecimal?): Array<BigDecimal> = super.divideAndRemainder(divisor)
        override fun divideAndRemainder(divisor: BigDecimal?, mc: MathContext?): Array<BigDecimal> =
            super.divideAndRemainder(divisor, mc)

        fun integralViaSuper(divisor: BigDecimal?, mc: MathContext?): BigDecimal =
            super.divideToIntegralValue(divisor, mc)
    }

    private fun value(text: String, truthful: Boolean): BigDecimal = if (truthful) Truthful(text) else BigDecimal(text)

    private fun representation(coefficient: String, scale: Int, actual: BigDecimal) {
        assertEquals(coefficient, actual.unscaledValue().toString())
        assertEquals(scale, actual.scale())
    }

    @Test fun truthfulIntegralFamiliesKeepExactSignedRemaindersBeyondContextPrecision() {
        // 17.25 = 3 * 5 + 2.25. Truncation is toward zero, so the remainder
        // follows the dividend's sign even for a negative divisor. Preferred q scale is 2 - 1 = 1.
        for (mask in 0..3) for (negativeSource in listOf(false, true)) for (negativeDivisor in listOf(false, true)) {
            val source = value(if (negativeSource) "-17.25" else "17.25", mask and 1 != 0)
            val divisor = value(if (negativeDivisor) "-5.0" else "5.0", mask and 2 != 0)
            val q = if (negativeSource != negativeDivisor) "-3" else "3"
            val r = if (negativeSource) "-225" else "225"
            representation(q + "0", 1, source.divideToIntegralValue(divisor))
            representation(r, 2, source.remainder(divisor))
            val exactPair = source.divideAndRemainder(divisor)
            assertEquals(2, exactPair.size)
            representation(q + "0", 1, exactPair[0])
            representation(r, 2, exactPair[1])
            for (mode in RoundingMode.entries) {
                // One digit suffices for the integer 3, but not 3.0. Remainder
                // 2.25 is exact and is allowed three digits even under UNNECESSARY.
                val context = MathContext(1, mode)
                representation(q, 0, source.divideToIntegralValue(divisor, context))
                representation(r, 2, source.remainder(divisor, context))
                val pair = source.divideAndRemainder(divisor, context)
                assertEquals(2, pair.size)
                representation(q, 0, pair[0])
                representation(r, 2, pair[1])
            }
            // Integer 34 needs two significant digits; p=1 cannot return it
            // regardless of rounding policy. This is an error, not a rounded q=30.
            val larger = value(if (negativeSource) "-172.5" else "172.5", mask and 1 != 0)
            val context = MathContext(1, RoundingMode.DOWN)
            assertFailsWith<ArithmeticException> { larger.divideToIntegralValue(divisor, context) }
            assertFailsWith<ArithmeticException> { larger.remainder(divisor, context) }
            assertFailsWith<ArithmeticException> { larger.divideAndRemainder(divisor, context) }
        }
    }

    @Test fun explicitIntegralCallerPreservesArgumentsResultsExceptionsAndOwnedSuper() {
        val replacement = BigDecimal("7.00")
        val failure = IllegalStateException("541 caller sentinel")
        val finite = MathContext(1, RoundingMode.UNNECESSARY)
        class Caller : BigDecimal("17.25") {
            var throwing = false
            var operand: BigDecimal? = null
            var context: MathContext? = null
            override fun divideToIntegralValue(divisor: BigDecimal?, mc: MathContext?): BigDecimal {
                operand = divisor
                context = mc
                if (throwing) throw failure
                return replacement
            }
        }
        val caller = Caller()
        val source: BigDecimal = caller
        val distinct = BigDecimal("5.0")
        // Only the explicit call's override is constrained. No internal call count/order is asserted.
        for (divisor in listOf(distinct, source, null)) for (context in listOf(finite, MathContext.UNLIMITED, null)) {
            caller.throwing = false
            assertSame(replacement, source.divideToIntegralValue(divisor, context))
            assertSame(divisor, caller.operand)
            assertSame(context, caller.context)
            caller.throwing = true
            assertSame(failure, assertFailsWith<IllegalStateException> { source.divideToIntegralValue(divisor, context) })
            assertSame(divisor, caller.operand)
            assertSame(context, caller.context)
        }
        val truthful = Truthful("17.25")
        representation("3", 0, truthful.integralViaSuper(Truthful("5.0"), finite))
        representation("30", 1, truthful.integralViaSuper(Truthful("5.0"), MathContext.UNLIMITED))
        representation("1", 0, truthful.integralViaSuper(truthful, finite))
        assertFailsWith<NullPointerException> { truthful.integralViaSuper(null, finite) }
        assertFailsWith<NullPointerException> { truthful.integralViaSuper(distinct, null) }
        assertFailsWith<ArithmeticException> { truthful.integralViaSuper(BigDecimal.ZERO, finite) }
    }

    @Test fun truthfulPointMotionAndAlternateTextHaveIndependentLiteralControls() {
        for (truthful in listOf(false, true)) {
            // 1.23 * 10^4 = 12300; point motion clamps scale to zero,
            // whereas scaleByPowerOfTen retains coefficient 123 at scale -2.
            val source = value("1.23", truthful)
            representation("12300", 0, source.movePointRight(4))
            representation("12300", 0, source.movePointLeft(-4))
            representation("123", -2, source.scaleByPowerOfTen(4))
            representation("123", 6, source.movePointLeft(4))
            representation("123", 6, source.movePointRight(-4))
            // A negative input scale is normalized even by zero-distance motion.
            val negativeScale = value("1.23E+4", truthful)
            representation("12300", 0, negativeScale.movePointLeft(0))
            representation("12300", 0, negativeScale.movePointRight(0))
            val texts = listOf(
                listOf("1E-7", "100E-9", "0.0000001"),
                listOf("-1E-7", "-100E-9", "-0.0000001"),
                listOf("0E-7", "0.0E-6", "0.0000000"),
                listOf("0E+2", "0.0E+3", "0"),
                listOf("1.2300E+5", "123.00E+3", "123000"),
            )
            for ((canonical, engineering, plain) in texts) {
                val decimal = value(canonical, truthful)
                assertEquals(canonical, decimal.toString())
                assertEquals(engineering, decimal.toEngineeringString())
                assertEquals(plain, decimal.toPlainString())
            }
        }
    }
}

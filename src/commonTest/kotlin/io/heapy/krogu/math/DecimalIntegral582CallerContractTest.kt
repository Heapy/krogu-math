// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class DecimalIntegral582CallerContractTest {
    @Test fun explicitContextOverrideAndCallerSuperRemainDistinct() {
        val divisor = BigDecimal("2.0")
        val context = MathContext(3, RoundingMode.DOWN)
        val marker = BigDecimal("7.00")
        val failure = IllegalStateException("caller-context582")
        class Caller(private val throws: Boolean) : BigDecimal("10.50") {
            override fun divide(value: BigDecimal, mc: MathContext): BigDecimal {
                assertSame(divisor, value)
                assertSame(context, mc)
                if (throws) throw failure
                return marker
            }
            fun ordinaryContext(value: BigDecimal, mc: MathContext): BigDecimal = super.divide(value, mc)
            fun ordinaryIntegral(value: BigDecimal): BigDecimal = super.divideToIntegralValue(value)
        }
        val caller = Caller(false)
        val base: BigDecimal = caller
        assertSame(marker, base.divide(divisor, context))
        assertSame(failure, assertFailsWith<IllegalStateException> { (Caller(true) as BigDecimal).divide(divisor, context) })
        val contextResult = caller.ordinaryContext(divisor, context)
        assertEquals("525", contextResult.unscaledValue().toString())
        assertEquals(2, contextResult.scale())
        assertEquals("5.25", contextResult.toString())
        val integralResult = caller.ordinaryIntegral(divisor)
        assertEquals("50", integralResult.unscaledValue().toString())
        assertEquals(1, integralResult.scale())
        assertEquals("5.0", integralResult.toString())
    }
}

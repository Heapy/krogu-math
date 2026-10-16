// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class DecimalText588CallerJvmTest {
    private class Child : BigDecimal {
        constructor(text: String, context: MathContext) : super(text, context)
        constructor(chars: CharArray, context: MathContext) : super(chars, context)
        override fun precision(): Int = super.precision()
        override fun scale(): Int = super.scale()
        override fun unscaledValue(): BigInteger = super.unscaledValue()
    }

    @Test fun constructingTruthfulSubclassAndExplicitCallerOverrideRemainNumerical() {
        val context = MathContext(2, RoundingMode.HALF_EVEN)
        for (value in listOf(Child("-00125.0e1", context), Child("-00125.0e1".toCharArray(), context))) {
            assertEquals("-12", value.unscaledValue().toString())
            assertEquals(-2, value.scale())
            assertEquals(2, value.precision())
            assertEquals("-1.2E+3", value.toString())
        }
        val marker = BigDecimal("7.00")
        val failure = IllegalStateException("caller-owned")
        class Caller : BigDecimal("125".toCharArray(), context) {
            var throwing = false
            override fun round(mc: MathContext): BigDecimal {
                if (throwing) throw failure
                return marker
            }
            fun inherited(mc: MathContext): BigDecimal = super.round(mc)
        }
        val caller = Caller()
        assertSame(marker, caller.round(MathContext(1)))
        assertEquals(BigDecimal("1E+2"), caller.inherited(MathContext(1, RoundingMode.DOWN)))
        caller.throwing = true
        assertSame(failure, assertFailsWith<IllegalStateException> { caller.round(MathContext(1)) })
        // Direct caller semantics only; no internal callback order/count or
        // constructing-subclass noncall promise is inferred.
    }
}

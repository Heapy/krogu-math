// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/** Independent task 541 source controls; execution status belongs to the handoff. */
class DecimalUlp541ContractTest {
    private class Truthful(coefficient: BigInteger, scale: Int) : BigDecimal(coefficient, scale) {
        override fun unscaledValue(): BigInteger = super.unscaledValue()
        override fun scale(): Int = super.scale()
        override fun precision(): Int = super.precision()
        override fun signum(): Int = super.signum()

        fun ulpViaSuper(): BigDecimal = super.ulp()
        fun plusViaSuper(): BigDecimal = super.plus()
    }

    private val coefficients = listOf(
        "0", "1", "-1", "1200", "-1200",
        "9999999999999999999999999999999999999999",
        "-9999999999999999999999999999999999999999",
    )
    private val scales = listOf(
        Int.MIN_VALUE, Int.MIN_VALUE + 1, -7, -1, 0, 1, 7, Int.MAX_VALUE - 1, Int.MAX_VALUE,
    )

    private fun representation(coefficient: String, scale: Int, actual: BigDecimal) {
        assertEquals(coefficient, actual.unscaledValue().toString())
        assertEquals(scale, actual.scale())
    }

    private fun unitAt(scale: Int, actual: BigDecimal) {
        // Specification literals: no expected result is built by decimal arithmetic.
        representation("1", scale, actual)
        assertEquals(1, actual.signum())
        assertEquals(1, actual.precision())
    }

    @Test fun ordinaryAndTruthfulUlpKeepsUnitCoefficientAtEverySelectedStoredScale() {
        for (coefficient in coefficients) for (scale in scales) {
            val ordinary = BigDecimal(BigInteger(coefficient), scale)
            val truthful = Truthful(BigInteger(coefficient), scale)
            for (source: BigDecimal in listOf(ordinary, truthful)) {
                representation(coefficient, scale, source)
                unitAt(scale, source.ulp())
                representation(coefficient, scale, source)
            }
            // An actual caller-written super expression, separate from virtual dispatch.
            unitAt(scale, truthful.ulpViaSuper())
            representation(coefficient, scale, truthful)
        }
    }

    @Test fun noContextPlusKeepsReceiverIdentityAndItsOriginalRepresentation() {
        for (coefficient in coefficients) for (scale in scales) {
            val ordinary = BigDecimal(BigInteger(coefficient), scale)
            val truthful = Truthful(BigInteger(coefficient), scale)
            for (source: BigDecimal in listOf(ordinary, truthful)) {
                representation(coefficient, scale, source)
                assertSame(source, source.plus())
                representation(coefficient, scale, source)
            }
            assertSame(truthful, truthful.plusViaSuper())
            representation(coefficient, scale, truthful)
        }
    }

    @Test fun explicitUlpAndPlusOverridesKeepCallerOwnedResultsExceptionsAndSuperEntry() {
        val replacement = BigDecimal("7.00")
        val failure = IllegalStateException("541 explicit caller sentinel")
        class Caller : BigDecimal(BigInteger.valueOf(-1200), 7) {
            var throwing = false
            var lastEntry: String? = null
            override fun ulp(): BigDecimal {
                lastEntry = "ulp"
                if (throwing) throw failure
                return replacement
            }
            override fun plus(): BigDecimal {
                lastEntry = "plus"
                if (throwing) throw failure
                return replacement
            }
            fun ulpViaSuper(): BigDecimal = super.ulp()
            fun plusViaSuper(): BigDecimal = super.plus()
        }
        val caller = Caller()
        val source: BigDecimal = caller
        assertSame(replacement, source.ulp())
        assertEquals("ulp", caller.lastEntry)
        assertSame(replacement, source.plus())
        assertEquals("plus", caller.lastEntry)
        caller.throwing = true
        assertSame(failure, assertFailsWith<IllegalStateException> { source.ulp() })
        assertEquals("ulp", caller.lastEntry)
        assertSame(failure, assertFailsWith<IllegalStateException> { source.plus() })
        assertEquals("plus", caller.lastEntry)

        // Keep the replacements throwing: successful super calls therefore exercise
        // the requested base entries, without a fabricated null or Java adapter.
        unitAt(7, caller.ulpViaSuper())
        assertSame(caller, caller.plusViaSuper())
        representation("-1200", 7, source)
    }
}

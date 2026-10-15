// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DecimalSum585SubtypeJvmTest {
    private class TruthfulInteger(value: BigInteger) : BigInteger(value.toByteArray()) {
        override fun signum(): Int = super.signum()
        override fun negate(): BigInteger = TruthfulInteger(super.negate())
        override fun multiply(value: BigInteger): BigInteger = TruthfulInteger(super.multiply(value))
        override fun add(value: BigInteger): BigInteger = TruthfulInteger(super.add(value))
        override fun subtract(value: BigInteger): BigInteger = TruthfulInteger(super.subtract(value))
        override fun divideAndRemainder(value: BigInteger): Array<BigInteger> {
            val pair = super.divideAndRemainder(value)
            return arrayOf(TruthfulInteger(pair[0]), TruthfulInteger(pair[1]))
        }
    }

    private fun restored(coefficient: Long): BigDecimal {
        val value = BigInteger.valueOf(coefficient)
        val bytes = ByteArrayOutputStream().also { buffer ->
            ObjectOutputStream(buffer).use { it.writeObject(BigDecimal(value, 2)) }
        }.toByteArray()
        val replacement = TruthfulInteger(value)
        return object : ObjectInputStream(ByteArrayInputStream(bytes)) {
            init { enableResolveObject(true) }
            override fun resolveObject(obj: Any?): Any? = if (obj is BigInteger) replacement else obj
        }.use { it.readObject() as BigDecimal }.also {
            // Bounded current-own-format setup reachability, not a universal
            // serialized runtime-class or object-identity requirement.
            assertTrue(it.unscaledValue() is TruthfulInteger)
            assertEquals(value, it.unscaledValue())
            assertEquals(2, it.scale())
        }
    }

    @Test fun currentOwnFormatTruthfulCoefficientsAndReturnedLeavesRetainNumericCohorts() {
        for (sign in listOf(-1, 1)) for (a in listOf(124L, 134L, 999L)) {
            val x = restored(sign * a)
            val y = BigDecimal(BigInteger.valueOf(sign.toLong()), 2)
            val exact = BigDecimal(BigInteger.valueOf(sign * (a + 1)), 2)
            assertEquals(exact, x.add(y))
            assertEquals(exact, x.subtract(y.negate()))
            assertEquals(exact, x.add(y, MathContext.UNLIMITED))
            for (mode in RoundingMode.entries) {
                val context = MathContext(2, mode)
                if (a != 999L && mode == RoundingMode.UNNECESSARY) {
                    assertFailsWith<ArithmeticException> { x.add(y, context) }
                    assertFailsWith<ArithmeticException> { x.subtract(y.negate(), context) }
                    continue
                }
                val up = a != 999L && when (mode) {
                    RoundingMode.UP -> true
                    RoundingMode.DOWN -> false
                    RoundingMode.CEILING -> sign > 0
                    RoundingMode.FLOOR -> sign < 0
                    RoundingMode.HALF_UP -> true
                    RoundingMode.HALF_DOWN -> false
                    RoundingMode.HALF_EVEN -> a == 134L
                    RoundingMode.UNNECESSARY -> false
                }
                val coefficient = if (a == 999L) 10L else if (a == 124L) 12L else 13L
                val expected = BigDecimal(BigInteger.valueOf(sign * (coefficient + if (up) 1 else 0)),
                    if (a == 999L) 0 else 1)
                assertEquals(expected, x.add(y, context))
                assertEquals(expected, x.subtract(y.negate(), context))
            }
        }
        // These bounded public controls take the materialized route. Common
        // direct raw calls exercise the new helper; giant public selection is
        // source-proved, not claimed observed by this test.
    }

    @Test fun explicitCallerOverrideResultsFailuresAndSuperDelegationRemainRequired() {
        val marker = BigDecimal("7.00")
        val failure = IllegalStateException("caller-owned")
        class Caller : BigDecimal("1.25") {
            var throwing = false
            override fun add(augend: BigDecimal): BigDecimal {
                if (throwing) throw failure
                assertEquals(BigDecimal("0.10"), augend)
                return marker
            }
            fun inherited(augend: BigDecimal, mc: MathContext) = super.add(augend, mc)
        }
        val caller = Caller()
        val argument = BigDecimal("0.10")
        assertSame(marker, caller.add(argument))
        assertSame(marker, caller.inherited(argument, MathContext.UNLIMITED))
        assertEquals(BigDecimal("1.4"), caller.inherited(argument, MathContext(2, RoundingMode.HALF_EVEN)))
        caller.throwing = true
        assertSame(failure, assertFailsWith<IllegalStateException> { caller.add(argument) })
        assertSame(failure, assertFailsWith<IllegalStateException> { caller.inherited(argument, MathContext.UNLIMITED) })
    }
}

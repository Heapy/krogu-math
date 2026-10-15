// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DecimalContextDivision581SubtypeJvmTest {
    private class TruthfulInteger(value: BigInteger) : BigInteger(value.toByteArray()) {
        override fun signum(): Int = super.signum()
        override fun abs(): BigInteger = super.abs()
        override fun negate(): BigInteger = super.negate()
        override fun bitLength(): Int = super.bitLength()
    }

    private fun restored(coefficient: Long, scale: Int): BigDecimal {
        val original = BigDecimal.valueOf(coefficient, scale)
        val bytes = ByteArrayOutputStream().also { buffer ->
            ObjectOutputStream(buffer).use { it.writeObject(original) }
        }.toByteArray()
        val replacement = TruthfulInteger(BigInteger.valueOf(coefficient))
        val answer = object : ObjectInputStream(ByteArrayInputStream(bytes)) {
            init { enableResolveObject(true) }
            override fun resolveObject(obj: Any?): Any? = if (obj is BigInteger) replacement else obj
        }.use { it.readObject() as BigDecimal }
        // Bounded current-own-format setup reachability, not a general type/identity promise.
        assertTrue(answer.unscaledValue() is TruthfulInteger)
        assertEquals(BigInteger.valueOf(coefficient), answer.unscaledValue())
        assertEquals(scale, answer.scale())
        return answer
    }

    @Test fun truthfulRestoredOperandsUseTheActualPublicStreamAndExactEscape() {
        for (leftSign in listOf(-1, 1)) for (rightSign in listOf(-1, 1)) {
            val sign = leftSign * rightSign
            val lowLeft = restored(leftSign.toLong(), Int.MIN_VALUE)
            val lowRight = restored(2L * rightSign, 1)
            val highLeft = restored(19L * leftSign, Int.MAX_VALUE)
            val highRight = restored(20L * rightSign, 0)
            val ordinaryLeft = BigDecimal.valueOf(19L * leftSign, Int.MAX_VALUE)
            val ordinaryRight = BigDecimal.valueOf(20L * rightSign)
            val halfLeft = restored(leftSign.toLong(), 0)
            val halfRight = restored(2L * rightSign, 0)
            for (mode in RoundingMode.entries) {
                assertEquals(BigDecimal.valueOf(5L * sign, Int.MIN_VALUE),
                    lowLeft.divide(lowRight, MathContext(1, mode)))
                assertEquals(BigDecimal.valueOf(5L * sign, 1),
                    halfLeft.divide(halfRight, MathContext(Int.MAX_VALUE, mode)))
                // Independent literal: 19/20=.95, guard5 tie after retained9.
                // Upward magnitude carry alone changes MAX+1 to MAX.
                val carries = when (mode) {
                    RoundingMode.UP, RoundingMode.HALF_UP, RoundingMode.HALF_EVEN -> true
                    RoundingMode.CEILING -> sign > 0
                    RoundingMode.FLOOR -> sign < 0
                    RoundingMode.DOWN, RoundingMode.HALF_DOWN, RoundingMode.UNNECESSARY -> false
                }
                for (call in listOf<() -> BigDecimal>(
                    { highLeft.divide(highRight, MathContext(1, mode)) },
                    { ordinaryLeft.divide(ordinaryRight, MathContext(1, mode)) })) {
                    if (carries) assertEquals(BigDecimal.valueOf(sign.toLong(), Int.MAX_VALUE), call())
                    else assertFailsWith<ArithmeticException> { call() }
                }
            }
        }
    }
}

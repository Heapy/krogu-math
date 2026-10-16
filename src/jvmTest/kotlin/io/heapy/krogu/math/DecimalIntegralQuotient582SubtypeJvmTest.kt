// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecimalIntegralQuotient582SubtypeJvmTest {
    private class TruthfulInteger(value: BigInteger) : BigInteger(value.toByteArray()) {
        override fun signum(): Int = super.signum()
        override fun abs(): BigInteger = super.abs()
        override fun negate(): BigInteger = super.negate()
        override fun bitLength(): Int = super.bitLength()
        override fun divide(value: BigInteger): BigInteger = super.divide(value)
        override fun divideAndRemainder(value: BigInteger): Array<BigInteger> = super.divideAndRemainder(value)
    }

    private fun restored(coefficient: Long, scale: Int): BigDecimal {
        val original = BigDecimal.valueOf(coefficient, scale)
        val bytes = ByteArrayOutputStream().also { buffer ->
            ObjectOutputStream(buffer).use { it.writeObject(currentFormatCoefficientCarrier529(original)) }
        }.toByteArray()
        val replacement = TruthfulInteger(BigInteger.valueOf(coefficient))
        val result = object : ObjectInputStream(ByteArrayInputStream(bytes)) {
            init { enableResolveObject(true) }
            override fun resolveObject(obj: Any?): Any? = if (obj is BigInteger) replacement else obj
        }.use { it.readObject() as BigDecimal }
        // Current-own-format setup reachability, not a general serialization identity promise.
        assertTrue(result.unscaledValue() is TruthfulInteger)
        assertEquals(BigInteger.valueOf(coefficient), result.unscaledValue())
        assertEquals(scale, result.scale())
        return result
    }

    @Test fun truthfulCurrentFormatCoefficientsKeepExactIntegralCohorts() {
        // Independent rational literals, with every restored/ordinary operand mask.
        val rows = listOf(
            listOf(1234567L, 3L, 2L, 411500L, 2L),
            listOf(1001L, 1L, -2L, 1001L, -2L),
            listOf(1L, 99L, -4L, 101L, 0L),
            listOf(1L, 2L, -4L, 5L, -3L))
        for (row in rows) for (leftSign in listOf(-1, 1)) for (rightSign in listOf(-1, 1))
            for (mask in 0..3) {
                val a = row[0] * leftSign
                val b = row[1] * rightSign
                val scale = row[2].toInt()
                val left = if (mask and 1 == 0) BigDecimal.valueOf(a, scale) else restored(a, scale)
                val right = if (mask and 2 == 0) BigDecimal.valueOf(b) else restored(b, 0)
                val coefficient = row[3] * leftSign * rightSign
                val result = left.divideToIntegralValue(right)
                assertEquals(coefficient.toString(), result.unscaledValue().toString())
                assertEquals(row[4].toInt(), result.scale())
                assertEquals(BigDecimal.valueOf(coefficient, row[4].toInt()), result)
            }
    }
}

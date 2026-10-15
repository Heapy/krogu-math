// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecimalResidual583SubtypeJvmTest {
    private class TruthfulInteger(value: BigInteger) : BigInteger(value.toByteArray()) {
        override fun signum(): Int = super.signum()
        override fun abs(): BigInteger = super.abs()
        override fun negate(): BigInteger = super.negate()
        override fun bitLength(): Int = super.bitLength()
        override fun multiply(value: BigInteger): BigInteger = super.multiply(value)
        override fun subtract(value: BigInteger): BigInteger = super.subtract(value)
        override fun divide(value: BigInteger): BigInteger = super.divide(value)
        override fun divideAndRemainder(value: BigInteger): Array<BigInteger> = super.divideAndRemainder(value)
    }

    private fun restored(coefficient: Long, scale: Int): BigDecimal {
        val original = BigDecimal.valueOf(coefficient, scale)
        val bytes = ByteArrayOutputStream().also { buffer ->
            ObjectOutputStream(buffer).use { it.writeObject(original) }
        }.toByteArray()
        val replacement = TruthfulInteger(BigInteger.valueOf(coefficient))
        val result = object : ObjectInputStream(ByteArrayInputStream(bytes)) {
            init { enableResolveObject(true) }
            override fun resolveObject(obj: Any?): Any? = if (obj is BigInteger) replacement else obj
        }.use { it.readObject() as BigDecimal }
        // Only bounded current-own-format setup reachability, not a serialization identity promise.
        assertTrue(result.unscaledValue() is TruthfulInteger)
        assertEquals(coefficient.toString(), result.unscaledValue().toString())
        assertEquals(scale, result.scale())
        return result
    }

    @Test fun truthfulCurrentFormatLeavesComposeThroughBothResidualRoutes() {
        val sourceScale = Int.MIN_VALUE + 1
        // Independent fixed rational literals; no production quotient supplies an oracle.
        val rows = listOf(
            listOf(100L, 1L, (Int.MIN_VALUE + 2).toLong(), -3L, 0L),
            listOf(2001L, 20L, (Int.MIN_VALUE + 2).toLong(), -3L, 1L),
            listOf(100L, 1L, Int.MIN_VALUE.toLong(), -1L, 0L),
            listOf(101L, 1L, Int.MIN_VALUE.toLong(), -1L, 1L))
        for (row in rows) for (leftSign in listOf(-1, 1)) for (rightSign in listOf(-1, 1))
            for (mask in 0..3) for (mode in RoundingMode.entries) {
                val a = row[0] * leftSign
                val b = row[1] * rightSign
                val source = if (mask and 1 == 0) BigDecimal.valueOf(a, sourceScale) else restored(a, sourceScale)
                val divisor = if (mask and 2 == 0) BigDecimal.valueOf(b, row[2].toInt()) else restored(b, row[2].toInt())
                val result = source.divideAndRemainder(divisor, MathContext(1, mode))
                assertEquals(2, result.size)
                assertEquals((leftSign * rightSign).toString(), result[0].unscaledValue().toString())
                assertEquals(row[3].toInt(), result[0].scale())
                assertEquals((leftSign * row[4]).toString(), result[1].unscaledValue().toString())
                assertEquals(sourceScale, result[1].scale())
                assertEquals(a.toString(), source.unscaledValue().toString())
                assertEquals(sourceScale, source.scale())
                assertEquals(b.toString(), divisor.unscaledValue().toString())
                assertEquals(row[2].toInt(), divisor.scale())
            }
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DecimalPower593SubtypeJvmTest {
    private class TruthfulCoefficient(text: String) : BigInteger(text) {
        override fun signum(): Int = super.signum()
        override fun abs(): BigInteger = super.abs()
        override fun multiply(value: BigInteger): BigInteger = super.multiply(value)
        override fun toByteArray(): ByteArray = super.toByteArray()
    }

    @Test fun currentOwnFormatTruthfulCoefficientKeepsTheSparseReciprocalValue() {
        for (sign in listOf(-1, 1)) {
            val ordinary = BigDecimal(BigInteger.valueOf(sign * 1000L), 1)
            val bytes = ByteArrayOutputStream().also { output ->
                ObjectOutputStream(output).use { it.writeObject(currentFormatCoefficientCarrier529(ordinary)) }
            }.toByteArray()
            val replacement = TruthfulCoefficient((sign * 1000L).toString())
            val restored = object : ObjectInputStream(ByteArrayInputStream(bytes)) {
                init { enableResolveObject(true) }
                override fun resolveObject(obj: Any?): Any? = if (obj is BigInteger) replacement else obj
            }.use { it.readObject() as BigDecimal }
            // Current-format setup reachability, not a new general identity or
            // legacy/cross-format serialization promise.
            assertSame(replacement, restored.unscaledValue())
            for (mode in RoundingMode.entries) {
                val context = MathContext(Int.MAX_VALUE, mode)
                val actual = restored.pow(-999999999, context)
                val control = ordinary.pow(-999999999, context)
                assertEquals(BigInteger.valueOf(sign.toLong()), actual.unscaledValue(), "$sign/$mode")
                assertEquals(1999999998, actual.scale(), "$sign/$mode")
                assertEquals(BigInteger.valueOf(sign.toLong()), control.unscaledValue(), "ordinary/$sign/$mode")
                assertEquals(1999999998, control.scale(), "ordinary/$sign/$mode")
            }
        }
    }
}

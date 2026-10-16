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

class DecimalDivisionNormalization594SubtypeJvmTest {
    private class TruthfulCoefficient(text: String) : BigInteger(text) {
        override fun abs(): BigInteger = super.abs()
        override fun signum(): Int = super.signum()
        override fun toString(): String = super.toString()
        override fun toByteArray(): ByteArray = super.toByteArray()
    }

    private fun restore(text: String): BigDecimal {
        val ordinary = BigDecimal(BigInteger(text), 0)
        val bytes = ByteArrayOutputStream().also { output ->
            ObjectOutputStream(output).use { it.writeObject(ordinary) }
        }.toByteArray()
        val replacement = TruthfulCoefficient(text)
        val restored = object : ObjectInputStream(ByteArrayInputStream(bytes)) {
            init { enableResolveObject(true) }
            override fun resolveObject(obj: Any?): Any? = if (obj is BigInteger) replacement else obj
        }.use { it.readObject() as BigDecimal }
        // Current-format setup reachability only; no legacy-format obligation.
        assertSame(replacement, restored.unscaledValue())
        return restored
    }

    @Test fun currentOwnFormatTruthfulNoncompactCoefficientsKeepExactAndTieResults() {
        val positive = listOf(53L, 52L, 53L, 52L, 53L, 52L, 52L)
        val negative = listOf(53L, 52L, 52L, 53L, 53L, 52L, 52L)
        for (sign in listOf(-1, 1)) {
            val leftText = (if (sign < 0) "-" else "") + "10500000000000000000105"
            val rightText = "2000000000000000000020"
            val a = restore(leftText)
            val b = restore(rightText)
            val exact = a.divide(b)
            assertEquals(BigInteger.valueOf(sign * 525L), exact.unscaledValue())
            assertEquals(2, exact.scale())
            for (mode in RoundingMode.entries) {
                if (mode == RoundingMode.UNNECESSARY) assertFailsWith<ArithmeticException> { a.divide(b, 1, mode) }
                else {
                    val actual = a.divide(b, 1, mode)
                    val magnitude = (if (sign > 0) positive else negative)[mode.ordinal]
                    assertEquals(BigInteger.valueOf(sign * magnitude), actual.unscaledValue(), "$sign/$mode")
                    assertEquals(1, actual.scale(), "$sign/$mode")
                }
            }
            assertEquals(leftText, a.unscaledValue().toString())
            assertEquals(rightText, b.unscaledValue().toString())
        }
    }
}

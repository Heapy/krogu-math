// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal as JDecimal
import java.math.BigInteger as JInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DecimalNumericMetadataJdkTest {
    private class CurrentFormatTruthfulCoefficient(text: String) : BigInteger(text) {
        val trace = StringBuilder()
        var explicitStringFailure: IllegalStateException? = null
        override fun abs(): BigInteger { trace.append("abs;"); return super.abs() }
        override fun toString(): String {
            trace.append("string;")
            explicitStringFailure?.let { throw it }
            return super.toString()
        }
        fun directSuperText(): String = super.toString()
    }

    @Test fun currentOwnFormatResolvedTruthfulCoefficientsKeepNumericsAndExplicitDispatch() {
        for (text in listOf("0", "123456789012345678901", "-123456789012345678901")) {
            val ordinary = BigInteger(text)
            val control = BigDecimal(ordinary, 7)
            // Write the current own object format, then exercise the reader's
            // supported object-resolution hook. No legacy stream is constructed.
            val bytes = ByteArrayOutputStream().also { buffer ->
                ObjectOutputStream(buffer).use { it.writeObject(control) }
            }.toByteArray()
            val replacement = CurrentFormatTruthfulCoefficient(text)
            val restored = object : ObjectInputStream(ByteArrayInputStream(bytes)) {
                init { enableResolveObject(true) }
                override fun resolveObject(obj: Any?): Any? = if (obj is BigInteger) replacement else obj
            }.use { it.readObject() as BigDecimal }
            assertSame(replacement, restored.unscaledValue(), "resolved coefficient is retained")
            assertEquals(ordinary, restored.unscaledValue(), "stored value: $text")
            assertEquals(text.removePrefix("-").length, restored.precision(), "precision: $text")
            assertEquals(control.precision(), restored.precision(), "ordinary precision control: $text")
            val aligned = ordinary.multiply(BigInteger.TEN)
            val neighbors = listOf(aligned.subtract(BigInteger.ONE), aligned, aligned.add(BigInteger.ONE))
            for ((index, coefficient) in neighbors.withIndex()) {
                val other = BigDecimal(coefficient, 8)
                val order = 1 - index
                assertEquals(order, restored.compareTo(other), "different scale: $text/$index")
                assertEquals(control.compareTo(other), restored.compareTo(other), "ordinary ordering control")
                assertEquals(-order, other.compareTo(restored), "reverse different scale")
            }
            // Inherited numerical operations above have no abs/string trace
            // assertion. These separate controls concern explicit caller dispatch.
            replacement.trace.clear()
            assertEquals(text, restored.unscaledValue().toString())
            assertTrue(replacement.trace.toString().startsWith("string;"), "explicit virtual string call")
            replacement.trace.clear()
            assertEquals(ordinary.abs(), restored.unscaledValue().abs())
            assertTrue(replacement.trace.toString().startsWith("abs;"), "explicit virtual abs call")
            val failure = IllegalStateException("explicit coefficient string sentinel")
            replacement.explicitStringFailure = failure
            assertSame(failure, assertFailsWith<IllegalStateException> { restored.unscaledValue().toString() })
            // Direct super bypasses the override even after its explicit failure
            // control is enabled. No poisoned inherited outcome is asserted.
            assertEquals(text, replacement.directSuperText())
        }
    }

    @Test fun boundedLargeCoefficientsAndCompensatingScalesMatchPublicJdk() {
        val texts = listOf("0", "1", "-1", "9".repeat(128), "1" + "0".repeat(128),
            "1" + "0".repeat(127) + "1", "-" + "7".repeat(257))
        val inputs = texts.flatMap { text ->
            listOf(Int.MIN_VALUE, -257, 0, 128, Int.MAX_VALUE).map { text to it }
        }
        for ((a, aScale) in inputs) {
            val actual = BigDecimal(BigInteger(a), aScale)
            val expected = JDecimal(JInteger(a), aScale)
            assertEquals(expected.precision(), actual.precision(), "$a@$aScale")
            for ((b, bScale) in inputs)
                assertEquals(expected.compareTo(JDecimal(JInteger(b), bScale)),
                    actual.compareTo(BigDecimal(BigInteger(b), bScale)), "$a@$aScale/$b@$bScale")
        }
    }

    @Test fun privateScaleComparisonHandlesTheWholeLongRangeWithoutOverflow() {
        // This private kernel is tested directly because public scale is Int,
        // while its exact-result caller passes summed scales in Long.
        val method = BigDecimal.Companion::class.java.getDeclaredMethod("compareValues",
            BigInteger::class.java, Long::class.javaPrimitiveType, BigInteger::class.java, Long::class.javaPrimitiveType)
        method.isAccessible = true
        val scales = listOf(Long.MIN_VALUE, Long.MIN_VALUE + 1, Long.MIN_VALUE + 128,
            -4294967296L, -1L, 0L, 1L, 4294967294L, Long.MAX_VALUE - 128, Long.MAX_VALUE - 1, Long.MAX_VALUE)
        val texts = listOf("0", "1", "-1", "10", "-101", "123" + "0".repeat(128),
            "123" + "0".repeat(127) + "1")
        for (a in texts) for (b in texts) for (aScale in scales) for (bScale in scales) {
            val left = JInteger(a)
            val right = JInteger(b)
            val expected = when {
                left.signum() != right.signum() -> left.signum().compareTo(right.signum())
                left.signum() == 0 -> 0
                else -> {
                    val aText = left.abs().toString()
                    val bText = right.abs().toString()
                    val exponent = JInteger.valueOf(aText.length.toLong()).subtract(JInteger.valueOf(aScale))
                        .compareTo(JInteger.valueOf(bText.length.toLong()).subtract(JInteger.valueOf(bScale)))
                    val width = maxOf(aText.length, bText.length)
                    (if (exponent != 0) exponent else aText.padEnd(width, '0')
                        .compareTo(bText.padEnd(width, '0')).compareTo(0)) * left.signum()
                }
            }
            val actual = method.invoke(BigDecimal.Companion, BigInteger(a), aScale, BigInteger(b), bScale) as Int
            assertEquals(expected, actual, "$a@$aScale/$b@$bScale")
        }
    }
}

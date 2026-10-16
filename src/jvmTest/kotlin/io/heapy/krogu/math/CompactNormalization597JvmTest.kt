// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.reflect.InvocationTargetException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CompactNormalization597JvmTest {
    private val coefficients = listOf("0", "1", "10", "1000", "1001", "999", "123", "1230", "1234",
        "1000000000000000000", "9223372036854775807", "9223372036854775806",
        "-1", "-1000", "-1234", "-9223372036854775807", "-9223372036854775808",
        "9223372036854775808", "100000000000000000000000000000000000000")

    @Test fun numericNormalizationMatchesIndependentTextDefinitionAndPublicDivision() {
        val normalized = BigDecimal::class.java.getDeclaredMethod("normalizedComparison", BigDecimal::class.java).apply { isAccessible = true }
        val exponent = BigDecimal::class.java.getDeclaredMethod("quotientExponent", BigDecimal::class.java).apply { isAccessible = true }
        for (a in coefficients) for (b in coefficients) {
            val at = a.removePrefix("-")
            val bt = b.removePrefix("-")
            val length = maxOf(at.length, bt.length)
            val comparison = at.padEnd(length, '0').compareTo(bt.padEnd(length, '0')).compareTo(0)
            for ((sa, sb) in listOf(0 to 0, Int.MIN_VALUE to Int.MAX_VALUE, Int.MAX_VALUE to Int.MIN_VALUE)) {
                val left = BigDecimal(BigInteger(a), sa)
                val right = BigDecimal(BigInteger(b), sb)
                assertEquals(comparison, (normalized.invoke(left, right) as Int).compareTo(0), "$a/$b normalized")
                assertEquals(at.length.toLong() - bt.length + sb.toLong() - sa - if (comparison < 0) 1 else 0,
                    exponent.invoke(left, right) as Long, "$a/$b exponent $sa/$sb")
            }
            if (b == "0") continue
            val ours = BigDecimal(BigInteger(a), 2).divide(BigDecimal(BigInteger(b), -3), MathContext(34))
            val host = java.math.BigDecimal(java.math.BigInteger(a), 2)
                .divide(java.math.BigDecimal(java.math.BigInteger(b), -3), java.math.MathContext(34))
            assertEquals(host.unscaledValue().toString(), ours.unscaledValue().toString(), "$a/$b coefficient")
            assertEquals(host.scale(), ours.scale(), "$a/$b scale")
        }
    }

    private class Coefficient(text: String) : BigInteger(text) {
        var failure: IllegalStateException? = null
        override fun abs(): BigInteger { failure?.let { throw it }; return super.abs() }
    }
    private fun restored(replacement: Coefficient): BigDecimal {
        val bytes = ByteArrayOutputStream().also { stream ->
            ObjectOutputStream(stream).use { it.writeObject(BigDecimal(BigInteger("1234"), 0)) }
        }.toByteArray()
        return object : ObjectInputStream(ByteArrayInputStream(bytes)) {
            init { enableResolveObject(true) }
            override fun resolveObject(obj: Any?): Any? = if (obj is BigInteger) replacement else obj
        }.use { it.readObject() as BigDecimal }.also { assertSame(replacement, it.unscaledValue()) }
    }
    @Test fun truthfulCurrentFormatCoefficientSubtypeKeepsBothLegacyCallbackRoutes() {
        for (name in listOf("quotientExponent", "normalizedComparison")) for (asLeft in listOf(true, false)) {
            val replacement = Coefficient("1234")
            val value = restored(replacement)
            val marker = IllegalStateException("legacy abs: $name/$asLeft")
            replacement.failure = marker
            val ordinary = BigDecimal("123")
            val method = BigDecimal::class.java.getDeclaredMethod(name, BigDecimal::class.java).apply { isAccessible = true }
            val actual = try {
                method.invoke(if (asLeft) value else ordinary, if (asLeft) ordinary else value)
                null
            } catch (failure: InvocationTargetException) { failure.cause }
            assertSame(marker, actual)
        }
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class TextOracle537JvmTest {
    @Test fun coldAndCachedCanonicalWritersKeepCacheStateAndPreflight() {
        val cache = BigDecimal::class.java.getDeclaredField("stringCache").apply { isAccessible = true }
        for (text in listOf("0.000", "-9223372036854775808.00", "123.4500", "-1E+77",
            "1E-77", "1" + "0".repeat(400) + "1.00100")) {
            val value = BigDecimal(text)
            val expected = java.math.BigDecimal(text).toString()
            assertNull(cache.get(value))
            val cold = StringBuilder()
            value.appendTo(cold)
            assertEquals(expected, cold.toString())
            val buffer = CharArray(expected.length + 4) { '#' }
            value.writeTo(buffer, 2, expected.length)
            assertEquals("##${expected}##", buffer.concatToString())
            assertNull(cache.get(value), "Cold writers must not force a complete String cache")
            val cached = value.toString()
            assertSame(cached, cache.get(value))
            var largest = 0
            val warm = StringBuilder()
            val destination = object : Appendable {
                override fun append(value: Char): Appendable { warm.append(value); return this }
                override fun append(value: CharSequence?): Appendable = append(value, 0, value?.length ?: 4)
                override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable {
                    largest = maxOf(largest, endIndex - startIndex)
                    warm.append(value, startIndex, endIndex)
                    return this
                }
            }
            value.appendTo(destination, maxChars = expected.length.toLong())
            assertEquals(expected, warm.toString())
            assertTrue(largest <= 256)
            buffer.fill('#')
            value.writeTo(buffer, 2, expected.length)
            assertEquals("##${expected}##", buffer.concatToString())
            assertSame(cached, cache.get(value))
            buffer.fill('#')
            assertFailsWith<ArithmeticException> { value.writeTo(buffer, 2, expected.length - 1) }
            assertTrue(buffer.all { it == '#' })
            val prefix = StringBuilder("prefix")
            assertFailsWith<ArithmeticException> { value.appendTo(prefix, maxChars = expected.length.toLong() - 1) }
            assertEquals("prefix", prefix.toString())
        }
    }

    private class StoredCoefficient : BigInteger("123") {
        val trace = StringBuilder()
        override fun abs(): BigInteger { trace.append("abs;"); return this }
        override fun toString(): String { trace.append("text;"); return "123" }
    }

    @Test fun serializedCoefficientSubtypeRetainsFormatterCallbacks() {
        val field = BigDecimal::class.java.getDeclaredField("coefficient").apply { isAccessible = true }
        val original = BigDecimal("123")
        field.set(original, StoredCoefficient())
        val bytes = java.io.ByteArrayOutputStream().also { buffer ->
            java.io.ObjectOutputStream(buffer).use { it.writeObject(original) }
        }.toByteArray()
        val restored = java.io.ObjectInputStream(java.io.ByteArrayInputStream(bytes)).use { it.readObject() as BigDecimal }
        val coefficient = field.get(restored)
        assertTrue(coefficient is StoredCoefficient)
        val stored = coefficient as StoredCoefficient
        for (format in DecimalTextFormat.entries) {
            stored.trace.setLength(0)
            val expected = when (format) {
                DecimalTextFormat.CANONICAL -> restored.toString()
                DecimalTextFormat.PLAIN -> restored.toPlainString()
                DecimalTextFormat.ENGINEERING -> restored.toEngineeringString()
            }
            // Canonical can already be cached; compare its current observable state.
            stored.trace.setLength(0)
            when (format) {
                DecimalTextFormat.CANONICAL -> restored.toString()
                DecimalTextFormat.PLAIN -> restored.toPlainString()
                DecimalTextFormat.ENGINEERING -> restored.toEngineeringString()
            }
            val expectedTrace = stored.trace.toString()
            stored.trace.setLength(0)
            val output = StringBuilder()
            restored.appendTo(output, format)
            assertEquals(expected, output.toString())
            assertEquals(expectedTrace, stored.trace.toString(), "$format append callbacks")
            stored.trace.setLength(0)
            val buffer = CharArray(expected.length)
            restored.writeTo(buffer, 0, buffer.size, format)
            assertEquals(expected, buffer.concatToString())
            assertEquals(expectedTrace, stored.trace.toString(), "$format buffer callbacks")
        }
    }

    @Test fun publicJdkIntegerParsingAndWriting() {
        for (radix in 2..36) for (length in listOf(1, 9, 19, 31, 64, 257)) {
            val magnitude = buildString {
                append((radix - 1).toString(radix))
                repeat(length - 1) { append(((it * 17 + 5) % radix).toString(radix)) }
            }
            for (sign in listOf("", "-", "+")) {
                val text = sign + magnitude
                val oracle = java.math.BigInteger(text, radix)
                val actual = BigInteger.parseText(StringBuilder(text), radix)
                assertEquals(oracle.toString(), actual.toString(), "$radix/$length/$sign")
                for (outputRadix in listOf(2, 3, 8, 10, 16, 36)) {
                    val output = StringBuilder()
                    actual.appendTo(output, outputRadix)
                    assertEquals(oracle.toString(outputRadix), output.toString())
                }
            }
        }
    }

    @Test fun publicJdkDecimalCohortsAndAllLayouts() {
        for (coefficient in listOf("0", "1", "-1", "123", "-99999999999999999999999999999999999999"))
            for (scale in listOf(Int.MIN_VALUE, -19, -7, -3, -1, 0, 1, 6, 7, 19, Int.MAX_VALUE)) {
                val oracle = java.math.BigDecimal(java.math.BigInteger(coefficient), scale)
                val actual = BigDecimal(BigInteger(coefficient), scale)
                assertEquals(oracle.toString(), actual.toString())
                for (format in DecimalTextFormat.entries) {
                    // Huge plain output is tested through explicit rejection, never materialized.
                    if (format == DecimalTextFormat.PLAIN && (scale < -100 || scale > 100)) continue
                    val expected = when (format) {
                        DecimalTextFormat.CANONICAL -> oracle.toString()
                        DecimalTextFormat.PLAIN -> oracle.toPlainString()
                        DecimalTextFormat.ENGINEERING -> oracle.toEngineeringString()
                    }
                    val output = StringBuilder()
                    actual.appendTo(output, format)
                    assertEquals(expected, output.toString(), "$coefficient@$scale/$format")
                }
            }
        for (text in listOf("١٢٣.٤٥", "９９９９９９９９９６e-4", "-123456789012345678901234567890.00010"))
            for (precision in listOf(0, 1, 9, 18)) for (mode in RoundingMode.entries) {
                val jdkContext = java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))
                fun outcome(parse: () -> String): String = try { parse() }
                    catch (e: ArithmeticException) { "!ArithmeticException:${e.message}" }
                assertEquals(outcome { java.math.BigDecimal(text, jdkContext).toString() },
                    outcome { BigDecimal.parseText(StringBuilder(text), MathContext(precision, mode)).toString() },
                    "$text/$precision/$mode")
            }
    }
}

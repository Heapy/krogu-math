// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math.migration

import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.toKroguByte
import io.heapy.krogu.math.toKroguShort
import io.heapy.krogu.math.toKroguInt
import io.heapy.krogu.math.toKroguLong
import io.heapy.krogu.math.toKroguFloat
import io.heapy.krogu.math.toKroguDouble
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KotlinNumberConversionWorkaroundTest {
    @Test fun erasedGenericAndCollectionCallsPreserveWideLowBitsAndTruncation() {
        // Exact binary32 powers of two; large Float literals can be emitted as
        // shortened decimal JS numbers rather than their exact binary32 values.
        val twoTo53 = Float.fromBits(0x5a000000)
        val twoTo64 = Float.fromBits(0x5f800000)
        val wide = Values(1, 1, 1, 9007199254740993L, twoTo53, 9007199254740992.0)
        val negativeWide = Values(-1, -1, -1, -9007199254740993L, -twoTo53, -9007199254740992.0)
        val cases = listOf<Pair<Number, Values>>(
            BigInteger("9007199254740993") to wide,
            BigDecimal("9007199254740993.00") to wide,
            BigInteger("-9007199254740993") to negativeWide,
            BigDecimal("-9007199254740993.00") to negativeWide,
            BigDecimal("42.75") to Values(42, 42, 42, 42L, 42.75f, 42.75),
            BigDecimal("-42.75") to Values(-42, -42, -42, -42L, -42.75f, -42.75),
            BigInteger("18446744073709551617") to Values(1, 1, 1, 1L, twoTo64, 18446744073709551616.0),
            BigDecimal("18446744073709551617.9") to Values(1, 1, 1, 1L, twoTo64, 18446744073709551616.0)
        )
        for ((value, expected) in cases) {
            assertEquals(expected, throughNumber(value))
            assertEquals(expected, throughGeneric(value))
        }
        assertEquals(cases.map { it.second.long }, collectionLongs(cases.map { it.first }))
    }

    @Test fun floatAndDoubleRemainDistinctIncludingTiesOverflowAndSignedUnderflow() {
        for ((text, floatValue) in listOf("16777217" to 16777216.0f, "16777219" to 16777220.0f)) {
            for (value in listOf<Number>(BigInteger(text), BigDecimal(text))) {
                assertEquals(floatValue, genericFloat(value))
                assertEquals(if (text == "16777217") 16777217.0 else 16777219.0, genericDouble(value))
            }
        }
        val tenth: Number = BigDecimal("0.1")
        assertEquals(0x3dcccccd, genericFloat(tenth).toBits())
        assertEquals(0x3fb999999999999aL, genericDouble(tenth).toBits())
        val floatOverflow: Number = BigDecimal("1e40")
        assertEquals(Float.POSITIVE_INFINITY, genericFloat(floatOverflow))
        assertEquals(1e40, genericDouble(floatOverflow))
        val tiny: Number = BigDecimal("-1e-50")
        assertEquals(Int.MIN_VALUE, genericFloat(tiny).toBits())
        assertEquals(-1e-50, genericDouble(tiny))
        val huge: Number = BigDecimal("1e400")
        assertEquals(Float.POSITIVE_INFINITY, genericFloat(huge))
        assertEquals(Double.POSITIVE_INFINITY, genericDouble(huge))
    }

    @Test fun builtinNumbersKeepPlatformConversions() {
        val expected = Values(42, 42, 42, 42L, 42.0f, 42.0)
        for (value in listOf<Number>(42.toByte(), 42.toShort(), 42, 42L, 42.0f, 42.0)) {
            assertEquals(expected, throughNumber(value))
            assertEquals(expected, throughGeneric(value))
        }
        assertEquals(Values(-42, -42, -42, -42L, -42.75f, -42.75), throughNumber(-42.75))
        // An ordinary Long must not be coerced through Double and lose its low bits.
        assertEquals(9007199254740993L, genericLong(9007199254740993L))
        assertEquals(1, genericInt(9007199254740993L))
        assertEquals(Int.MIN_VALUE, genericFloat(-0.0f).toBits())
        assertEquals(Long.MIN_VALUE, genericDouble(-0.0).toBits())
        assertTrue(genericFloat(Float.NaN).isNaN())
        assertTrue(genericDouble(Double.NaN).isNaN())
        assertEquals(Float.NEGATIVE_INFINITY, genericFloat(Float.NEGATIVE_INFINITY))
        assertEquals(Double.POSITIVE_INFINITY, genericDouble(Double.POSITIVE_INFINITY))
    }

    @Test fun concreteDispatchPreservesAllSubclassOverridesAndThrownIdentity() {
        val expected = Values(1, 1, 16777217, 16777217L, 16777216.0f, 16777217.0)
        for (integer in listOf(true, false)) {
            val recorder = Recorder()
            val value: Number = if (integer) RecordingInteger(recorder) else RecordingDecimal(recorder)
            assertEquals(expected, throughNumber(value))
            assertEquals(listOf("Byte", "Short", "Int", "Long", "Float", "Double"), recorder.calls)
            recorder.calls.clear()
            assertEquals(expected, throughGeneric(value))
            assertEquals(listOf("Byte", "Short", "Int", "Long", "Float", "Double"), recorder.calls)
            // Reordered and repeated calls cannot be served by a stateful coercion hook.
            recorder.calls.clear()
            assertEquals(16777217.0, genericDouble(value))
            assertEquals(16777216.0f, genericFloat(value))
            assertEquals(16777216.0f, genericFloat(value))
            assertEquals(listOf("Double", "Float", "Float"), recorder.calls)
            val marker = ConversionFailure()
            recorder.failure = marker
            val operations = listOf<Pair<String, () -> Any>>(
                "Byte" to { value.toKroguByte() }, "Short" to { value.toKroguShort() },
                "Int" to { value.toKroguInt() }, "Long" to { value.toKroguLong() },
                "Float" to { value.toKroguFloat() }, "Double" to { value.toKroguDouble() }
            )
            for ((label, operation) in operations) {
                recorder.calls.clear()
                assertSame(marker, assertFailsWith<ConversionFailure> { operation() })
                assertEquals(listOf(label), recorder.calls)
            }
        }
    }

    // Non-inline callers keep the static Number/generic types at the conversion site.
    private fun throughNumber(n: Number): Values = Values(n.toKroguByte(), n.toKroguShort(),
        n.toKroguInt(), n.toKroguLong(), n.toKroguFloat(), n.toKroguDouble())
    private fun <T : Number> throughGeneric(n: T): Values = Values(n.toKroguByte(), n.toKroguShort(),
        n.toKroguInt(), n.toKroguLong(), n.toKroguFloat(), n.toKroguDouble())
    private fun collectionLongs(values: List<Number>): List<Long> = values.map { it.toKroguLong() }
    private fun <T : Number> genericInt(n: T): Int = n.toKroguInt()
    private fun <T : Number> genericLong(n: T): Long = n.toKroguLong()
    private fun <T : Number> genericFloat(n: T): Float = n.toKroguFloat()
    private fun <T : Number> genericDouble(n: T): Double = n.toKroguDouble()

    private data class Values(val byte: Byte, val short: Short, val int: Int, val long: Long,
        val float: Float, val double: Double)
    private class ConversionFailure : RuntimeException()
    private class Recorder {
        val calls = mutableListOf<String>()
        var failure: ConversionFailure? = null
        fun record(label: String) { calls.add(label); failure?.let { throw it } }
    }
    private class RecordingInteger(private val recorder: Recorder) : BigInteger("16777217") {
        override fun toByte(): Byte { recorder.record("Byte"); return 1 }
        override fun toShort(): Short { recorder.record("Short"); return 1 }
        override fun toInt(): Int { recorder.record("Int"); return 16777217 }
        override fun toLong(): Long { recorder.record("Long"); return 16777217L }
        override fun toFloat(): Float { recorder.record("Float"); return 16777216.0f }
        override fun toDouble(): Double { recorder.record("Double"); return 16777217.0 }
    }
    private class RecordingDecimal(private val recorder: Recorder) : BigDecimal("16777217") {
        override fun toByte(): Byte { recorder.record("Byte"); return 1 }
        override fun toShort(): Short { recorder.record("Short"); return 1 }
        override fun toInt(): Int { recorder.record("Int"); return 16777217 }
        override fun toLong(): Long { recorder.record("Long"); return 16777217L }
        override fun toFloat(): Float { recorder.record("Float"); return 16777216.0f }
        override fun toDouble(): Double { recorder.record("Double"); return 16777217.0 }
    }
}

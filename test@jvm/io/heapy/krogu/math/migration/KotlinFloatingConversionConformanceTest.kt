// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math.migration

import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.MathContext
import io.heapy.krogu.math.RoundingMode
import io.heapy.krogu.math.toBigDecimal
import io.heapy.krogu.math.toJavaMathContext
import java.math.BigDecimal as JDecimal
import kotlin.toBigDecimal as jdkDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

/** Live public references, prepared for the parent's later execution window. */
class KotlinFloatingConversionConformanceTest {
    private fun outcome(call: () -> Any): String = try {
        when (val result = call()) {
            is BigDecimal -> "${result.unscaledValue()}@${result.scale()}"
            is JDecimal -> "${result.unscaledValue()}@${result.scale()}"
            else -> error("Expected an explicitly typed decimal")
        }
    } catch (failure: Exception) { failure.javaClass.name }

    private fun compare(label: String, reference: () -> Any, actual: () -> Any) =
        assertEquals(outcome(reference), outcome(actual), label)

    @Test fun literalControlsAgreeWithBothPublicStdlibAndJdkText() {
        for ((value, expected) in listOf(0.1f to "1@1", 1f to "10@1", -0f to "0@1",
            Float.MIN_VALUE to "14@46", Float.MAX_VALUE to "34028235@-31",
            Float.fromBits(0x39800000) to "24414062@11")) {
            assertEquals(expected, outcome { val n: JDecimal = value.jdkDecimal(); n })
            assertEquals(expected, outcome { JDecimal(java.lang.Float.toString(value)) })
            assertEquals(expected, outcome { val n: BigDecimal = value.toBigDecimal(); n })
        }
        for ((value, expected) in listOf(0.1 to "1@1", 1.0 to "10@1", -0.0 to "0@1",
            Double.MIN_VALUE to "49@325", Double.MAX_VALUE to "17976931348623157@-292")) {
            assertEquals(expected, outcome { val n: JDecimal = value.jdkDecimal(); n })
            assertEquals(expected, outcome { JDecimal(java.lang.Double.toString(value)) })
            assertEquals(expected, outcome { val n: BigDecimal = value.toBigDecimal(); n })
        }
    }

    @Test fun floatPowerBoundariesAndSmallSubnormalsMatchPublicTextConversions() {
        val bits = buildSet {
            add(0)
            for (field in 1..254) for (delta in -1..1) add((field shl 23) + delta)
            for (power in 0..22) for (delta in -1..1) add((1 shl power) + delta)
            addAll(1..32)
            addAll(listOf(0x0f800000, 0x6b000000, 0x6c800000, 0x39800000, 0x7f7ffffe, 0x7f7fffff))
        }
        for (magnitude in bits) for (sign in listOf(0, Int.MIN_VALUE)) {
            val raw = magnitude or sign
            val value = Float.fromBits(raw)
            compare("Float bits=$raw", { val n: JDecimal = value.jdkDecimal(); n },
                { val n: BigDecimal = value.toBigDecimal(); n })
            compare("JDK Float text bits=$raw", { JDecimal(java.lang.Float.toString(value)) }, { value.toBigDecimal() })
        }
    }

    @Test fun doubleBoundaryExtensionsReuseThePublicCanonicalFactoryContract() {
        val bits = buildSet {
            add(0L)
            for (field in listOf(1, 2, 3, 6, 52, 1022, 1023, 1024, 1075, 2045, 2046))
                for (delta in -1L..1L) add((field.toLong() shl 52) + delta)
            for (power in listOf(0, 1, 25, 50, 51)) for (delta in -1L..1L) add((1L shl power) + delta)
            addAll(1L..16L)
            add(0x7fefffffffffffffL)
        }
        for (magnitude in bits) for (sign in listOf(0L, Long.MIN_VALUE)) {
            val raw = magnitude or sign
            val value = Double.fromBits(raw)
            compare("Double bits=$raw", { val n: JDecimal = value.jdkDecimal(); n },
                { val n: BigDecimal = value.toBigDecimal(); n })
            compare("JDK Double text bits=$raw", { JDecimal(java.lang.Double.toString(value)) }, { value.toBigDecimal() })
            assertEquals(BigDecimal.valueOf(value), value.toBigDecimal(), "factory bits=$raw")
        }
    }

    @Test fun contextsAndNonfiniteErrorsMatchPublicStdlibForEveryRoundingMode() {
        val floats = listOf(0f, -0f, 0.1f, -0.1f, 1.25f, -1.25f, 9.99f, -9.99f,
            Float.MIN_VALUE, Float.MAX_VALUE, Float.fromBits(0x00800000), Float.fromBits(0x0f800000),
            Float.fromBits(0x6b000000), Float.fromBits(0x6c800000), Float.fromBits(0x39800000),
            Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)
        val doubles = listOf(0.0, -0.0, 0.1, -0.1, 1.25, -1.25, 9.99, -9.99,
            Double.MIN_VALUE, Double.MAX_VALUE, Double.fromBits(0x0010000000000000L),
            Double.fromBits(0x0060000000000000L), Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)
        for (precision in listOf(0, 1, 2, 7, 9, 17)) for (mode in RoundingMode.entries) {
            val mc = MathContext(precision, mode)
            val referenceContext = mc.toJavaMathContext()
            for (value in floats) compare("Float bits=${value.toBits()} $mc",
                { val n: JDecimal = value.jdkDecimal(mathContext = referenceContext); n },
                { val n: BigDecimal = value.toBigDecimal(mathContext = mc); n })
            for (value in doubles) compare("Double bits=${value.toBits()} $mc",
                { val n: JDecimal = value.jdkDecimal(mathContext = referenceContext); n },
                { val n: BigDecimal = value.toBigDecimal(mathContext = mc); n })
        }
        for (value in floats.filter { !it.isFinite() })
            compare("nonfinite Float", { value.jdkDecimal() }, { value.toBigDecimal() })
        for (value in doubles.filter { !it.isFinite() })
            compare("nonfinite Double", { value.jdkDecimal() }, { value.toBigDecimal() })
    }
}

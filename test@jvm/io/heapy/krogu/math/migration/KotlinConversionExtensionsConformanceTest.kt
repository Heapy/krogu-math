// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math.migration

import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.MathContext
import io.heapy.krogu.math.RoundingMode
import io.heapy.krogu.math.toBigDecimal
import io.heapy.krogu.math.toBigDecimalOrNull
import io.heapy.krogu.math.toBigInteger
import io.heapy.krogu.math.toBigIntegerOrNull
import io.heapy.krogu.math.toJavaMathContext
import java.math.BigDecimal as JDecimal
import java.math.BigInteger as JInteger
import kotlin.toBigDecimal as jdkDecimal
import kotlin.toBigInteger as jdkInteger
import kotlin.text.toBigDecimal as jdkTextDecimal
import kotlin.text.toBigDecimalOrNull as jdkDecimalOrNull
import kotlin.text.toBigInteger as jdkTextInteger
import kotlin.text.toBigIntegerOrNull as jdkIntegerOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KotlinConversionExtensionsConformanceTest {
    private fun outcome(call: () -> Any?): String = try {
        when (val value = call()) {
            is JDecimal -> "${value.unscaledValue()}@${value.scale()}"
            is BigDecimal -> "${value.unscaledValue()}@${value.scale()}"
            else -> value.toString()
        }
    } catch (failure: Exception) { failure.javaClass.name }

    private fun compare(label: String, reference: () -> Any?, actual: () -> Any?) =
        assertEquals(outcome(reference), outcome(actual), label)

    @Test fun integralReceiversAndNamedDefaultsMatchPublicKotlinCalls() {
        for (value in listOf(Int.MIN_VALUE, -1, 0, 1, 12345, Int.MAX_VALUE)) {
            compare("Int integer $value", { val n: JInteger = value.jdkInteger(); n },
                { val n: BigInteger = value.toBigInteger(); n })
            compare("Int decimal $value", { val n: JDecimal = value.jdkDecimal(); n },
                { val n: BigDecimal = value.toBigDecimal(); n })
        }
        for (value in listOf(Long.MIN_VALUE, -1L, 0L, 1L, 12345L, Long.MAX_VALUE)) {
            compare("Long integer $value", { value.jdkInteger() }, { value.toBigInteger() })
            compare("Long decimal $value", { value.jdkDecimal() }, { value.toBigDecimal() })
        }
        for (value in listOf(0u, 1u, Int.MAX_VALUE.toUInt() + 1u, UInt.MAX_VALUE))
            compare("UInt $value", { value.jdkInteger() }, { value.toBigInteger() })
        for (value in listOf(0uL, 1uL, Long.MAX_VALUE.toULong() + 1uL, ULong.MAX_VALUE))
            compare("ULong $value", { value.jdkInteger() }, { value.toBigInteger() })
        for (precision in listOf(0, 2, 7)) for (mode in RoundingMode.entries) {
            val mc = MathContext(precision, mode)
            val jm = mc.toJavaMathContext()
            compare("Int context $mc", { 12345.jdkDecimal(mathContext = jm) }, { 12345.toBigDecimal(mathContext = mc) })
            compare("Long context $mc", { Long.MIN_VALUE.jdkDecimal(jm) }, { Long.MIN_VALUE.toBigDecimal(mc) })
            for (text in listOf("-12345", "0", "12345")) {
                val ji = JInteger(text)
                val ki = BigInteger(text)
                compare("integer default $text", { val n: JDecimal = ji.jdkDecimal(); n },
                    { val n: BigDecimal = ki.toBigDecimal(); n })
                compare("integer context $text $mc", { ji.jdkDecimal(mathContext = jm) }, { ki.toBigDecimal(mathContext = mc) })
                for (scale in listOf(-3, 0, 2, Int.MIN_VALUE, Int.MAX_VALUE)) {
                    compare("integer scale $text@$scale $mc", { ji.jdkDecimal(scale, jm) }, { ki.toBigDecimal(scale, mc) })
                    compare("integer scale only $text@$scale", { ji.jdkDecimal(scale = scale) }, { ki.toBigDecimal(scale = scale) })
                }
            }
        }
    }

    @Test fun nullableIntegerParsingRetainsTheLeadingPlusReferenceDiscrepancy() {
        for ((radix, expected) in listOf(10 to "123", 16 to "291", 36 to "1371")) {
            val text = "+00123"
            assertEquals(expected, text.jdkTextInteger(radix).toString())
            assertNull(text.jdkIntegerOrNull(radix), "pinned Kotlin 2.4.21 observation radix=$radix")
            assertEquals(BigInteger(expected), text.toBigIntegerOrNull(radix))
        }
        assertNull("+00123".jdkIntegerOrNull())
        assertEquals(BigInteger("123"), "+00123".toBigIntegerOrNull())
    }

    @Test fun textRadixAndContextCallsKeepParseAndArithmeticFailuresDistinct() {
        val integers = listOf("", "+", "-", "-0", "12345678901234567890", "１２", "١٢", "ff", "z",
            "-101", " 1", "1 ", "1_0", "0x10", "1.0", "NaN", "++1")
        for (text in integers) {
            compare("integer $text", { text.jdkTextInteger() }, { text.toBigInteger() })
            compare("nullable integer $text", { text.jdkIntegerOrNull() }, { text.toBigIntegerOrNull() })
            for (radix in listOf(Int.MIN_VALUE, 0, 1, 2, 10, 16, 36, 37, Int.MAX_VALUE)) {
                compare("integer $text radix=$radix", { text.jdkTextInteger(radix = radix) }, { text.toBigInteger(radix = radix) })
                compare("nullable integer $text radix=$radix", { text.jdkIntegerOrNull(radix) }, { text.toBigIntegerOrNull(radix) })
            }
        }
        val decimals = listOf("", ".", "+", "-0.00", "1.235", "1.245", "-123.4500e+2", ".5", "5.",
            "１２.３", " 1", "1 ", "1_0", "NaN", "Infinity", "1e", "1e2147483648", "1e-2147483648")
        for (text in decimals) {
            compare("decimal $text", { text.jdkTextDecimal() }, { text.toBigDecimal() })
            compare("nullable decimal $text", { text.jdkDecimalOrNull() }, { text.toBigDecimalOrNull() })
            for (precision in listOf(0, 2, 5)) for (mode in RoundingMode.entries) {
                val mc = MathContext(precision, mode)
                compare("decimal $text $mc", { text.jdkTextDecimal(mathContext = mc.toJavaMathContext()) },
                    { text.toBigDecimal(mathContext = mc) })
                compare("nullable decimal $text $mc", { text.jdkDecimalOrNull(mc.toJavaMathContext()) },
                    { text.toBigDecimalOrNull(mc) })
            }
        }
    }
}

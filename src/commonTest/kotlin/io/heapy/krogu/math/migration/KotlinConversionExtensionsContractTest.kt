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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/** Explicit imports and result types are part of these compiler-checked migration examples. */
class KotlinConversionExtensionsContractTest {
    @Test fun integerBoundariesAndNamedDecimalDefaultsSelectKroguTypes() {
        val int: BigInteger = Int.MIN_VALUE.toBigInteger()
        val long: BigInteger = Long.MIN_VALUE.toBigInteger()
        val uint: BigInteger = UInt.MAX_VALUE.toBigInteger()
        val ulong: BigInteger = ULong.MAX_VALUE.toBigInteger()
        assertEquals(listOf("-2147483648", "-9223372036854775808", "4294967295", "18446744073709551615"),
            listOf(int, long, uint, ulong).map { it.toString() })
        assertEquals(BigInteger.ZERO, 0uL.toBigInteger())
        assertEquals("9223372036854775808", (Long.MAX_VALUE.toBigInteger() + BigInteger.ONE).toString())

        val context = MathContext(3, RoundingMode.HALF_EVEN)
        val plainInt: BigDecimal = 12345.toBigDecimal()
        val roundedInt: BigDecimal = 12345.toBigDecimal(mathContext = context)
        val plainLong: BigDecimal = Long.MIN_VALUE.toBigDecimal()
        val roundedLong: BigDecimal = 12345L.toBigDecimal(context)
        val coefficient = BigInteger("12345")
        val plain: BigDecimal = coefficient.toBigDecimal()
        val scaled: BigDecimal = coefficient.toBigDecimal(scale = 2)
        val rounded: BigDecimal = coefficient.toBigDecimal(mathContext = context)
        val both: BigDecimal = coefficient.toBigDecimal(2, context)
        assertEquals(listOf("12345", "1.23E+4", "-9223372036854775808", "1.23E+4",
            "12345", "123.45", "1.23E+4", "123"),
            listOf(plainInt, roundedInt, plainLong, roundedLong, plain, scaled, rounded, both).map { it.toString() })
        assertEquals("1.2345E+7", coefficient.toBigDecimal(scale = -3).toString())
        assertEquals("0E+2147483648", BigInteger.ZERO.toBigDecimal(scale = Int.MIN_VALUE).toString())
        assertEquals("1E-2147483647", BigInteger.ONE.toBigDecimal(scale = Int.MAX_VALUE).toString())
    }

    @Test fun signedRadixAndDecimalTextPreserveValuesAndScale() {
        val integer: BigInteger = "+ff".toBigInteger(radix = 16)
        val nullable: BigInteger? = "-101".toBigIntegerOrNull(radix = 2)
        val decimal: BigDecimal = "-123.4500e+2".toBigDecimal()
        val nullableDecimal: BigDecimal? = "1.2500".toBigDecimalOrNull()
        assertEquals("255", integer.toString())
        assertEquals(BigInteger("-5"), nullable)
        assertEquals(BigDecimal("-12345.00"), decimal)
        assertEquals(BigDecimal("1.2500"), nullableDecimal)
        assertEquals(BigInteger("12"), "１２".toBigInteger())
        assertEquals(BigInteger("35"), "z".toBigInteger(36))
        for ((radix, expected) in listOf(10 to "123", 16 to "291", 36 to "1371"))
            assertEquals(BigInteger(expected), "+00123".toBigIntegerOrNull(radix))
        assertEquals(BigInteger("123"), "+00123".toBigIntegerOrNull())
        assertEquals(BigDecimal(BigInteger.ONE, Int.MIN_VALUE), "1e2147483648".toBigDecimalOrNull())
        assertEquals(BigDecimal(BigInteger.TEN, Int.MIN_VALUE), "1.0e2147483649".toBigDecimal())
        val context = MathContext(3, RoundingMode.HALF_EVEN)
        assertEquals(BigDecimal("1.24"), "1.235".toBigDecimal(mathContext = context))
        assertEquals(BigDecimal("1.24"), "1.245".toBigDecimalOrNull(mathContext = context))
    }

    @Test fun nullableParsingDoesNotSwallowConfigurationOrRoundingErrors() {
        for (text in listOf("", "+", "-", " 1", "1 ", "1_0", "1.0", "NaN", "0x10", "++1")) {
            assertNull(text.toBigIntegerOrNull(), text)
            assertFailsWith<NumberFormatException>(text) { text.toBigInteger() }
        }
        assertNull("2".toBigIntegerOrNull(2))
        for (radix in listOf(Int.MIN_VALUE, -1, 0, 1, 37, Int.MAX_VALUE))
            for (text in listOf("", "bad", "10")) {
                assertFailsWith<IllegalArgumentException> { text.toBigInteger(radix) }
                assertFailsWith<IllegalArgumentException> { text.toBigIntegerOrNull(radix) }
            }
        for (text in listOf("", ".", "+", " 1", "1 ", "1_0", "NaN", "Infinity", "1e", "1e2147483649")) {
            assertNull(text.toBigDecimalOrNull(), text)
            assertNull(text.toBigDecimalOrNull(MathContext.DECIMAL32), text)
            assertFailsWith<NumberFormatException>(text) { text.toBigDecimal() }
        }
        val exact = MathContext(2, RoundingMode.UNNECESSARY)
        assertFailsWith<ArithmeticException> { 123.toBigDecimal(exact) }
        assertFailsWith<ArithmeticException> { 123L.toBigDecimal(exact) }
        assertFailsWith<ArithmeticException> { BigInteger("123").toBigDecimal(mathContext = exact) }
        assertFailsWith<ArithmeticException> { "1.234".toBigDecimal(exact) }
        assertFailsWith<ArithmeticException> { "1.234".toBigDecimalOrNull(exact) }
        assertNull("invalid".toBigDecimalOrNull(exact))
        assertEquals(BigDecimal("1.2"), "1.20".toBigDecimalOrNull(exact))
    }
}

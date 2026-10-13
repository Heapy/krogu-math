package io.heapy.krogu.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalCompactFloatingTest {
    @Test fun coefficientSubclassNormalizationMatchesJdk() {
        for (variant in 0..3) for (bytes in listOf(byteArrayOf(42), byteArrayOf(-128), byteArrayOf(0), byteArrayOf())) {
            val expectedTrace = StringBuilder()
            val actualTrace = StringBuilder()
            val expectedSource = object : java.math.BigInteger("123456789012345678901234567890") {
                override fun toByte(): Byte = 0
                override fun toShort(): Short = 0
                override fun toByteArray(): ByteArray { expectedTrace.append("bytes;"); return bytes }
            }
            val actualSource = object : BigInteger("123456789012345678901234567890") {
                override fun toByteArray(): ByteArray { actualTrace.append("bytes;"); return bytes }
            }
            fun outcome(block: () -> Any): String = try { block().toString() }
                catch (failure: Exception) { "!${failure.javaClass.simpleName}" }
            val expected = outcome { when (variant) {
                0 -> java.math.BigDecimal(expectedSource)
                1 -> java.math.BigDecimal(expectedSource, 2)
                2 -> java.math.BigDecimal(expectedSource, java.math.MathContext.DECIMAL32)
                else -> java.math.BigDecimal(expectedSource, 2, java.math.MathContext.DECIMAL32)
            } }
            val actual = outcome { when (variant) {
                0 -> BigDecimal(actualSource)
                1 -> BigDecimal(actualSource, 2)
                2 -> BigDecimal(actualSource, MathContext.DECIMAL32)
                else -> BigDecimal(actualSource, 2, MathContext.DECIMAL32)
            } }
            assertEquals(expected, actual, "constructor $variant bytes=${bytes.contentToString()}")
            assertEquals(expectedTrace.toString(), actualTrace.toString())
        }
    }

    @Test fun overriddenSignAndPrecisionFollowJdkConversionDispatch() {
        val coefficients = listOf("0", "1", "16777216", "16777217", "9007199254740992",
            "9007199254740993", "9223372036854775807", "-9223372036854775808", "9223372036854775808")
        for (coefficient in coefficients) for (scale in listOf(-350, -23, -22, -11, -10, 0, 10, 11, 22, 23, 350)) {
            for (sign in -1..1) for (single in listOf(false, true)) {
                val expectedTrace = StringBuilder()
                val actualTrace = StringBuilder()
                val expected = object : java.math.BigDecimal(java.math.BigInteger(coefficient), scale) {
                    override fun toByte(): Byte = 0
                    override fun toShort(): Short = 0
                    override fun signum(): Int { expectedTrace.append("sign;"); return sign }
                    override fun precision(): Int { expectedTrace.append("precision;"); return 1 }
                }
                val actual = object : BigDecimal(BigInteger(coefficient), scale) {
                    override fun signum(): Int { actualTrace.append("sign;"); return sign }
                    override fun precision(): Int { actualTrace.append("precision;"); return 1 }
                }
                val expectedBits = if (single) expected.toFloat().toBits().toLong() else expected.toDouble().toBits()
                val actualBits = if (single) actual.toFloat().toBits().toLong() else actual.toDouble().toBits()
                val label = "$coefficient scale=$scale sign=$sign float=$single"
                assertEquals(expectedBits, actualBits, label)
                assertEquals(expectedTrace.toString(), actualTrace.toString(), "trace $label")
            }
        }
    }

    @Test fun longCoefficientsAcrossBinaryFastPathScalesMatchJdk() {
        val random = Random(0x4d415448)
        val coefficients = listOf(0L, 1L, -1L, Long.MIN_VALUE, Long.MAX_VALUE,
            (1L shl 24) - 1, (1L shl 24) + 1, (1L shl 53) - 1, (1L shl 53) + 1) +
            List(500) { random.nextLong() }
        for (coefficient in coefficients) for (scale in -25..25) {
            val expected = java.math.BigDecimal.valueOf(coefficient, scale)
            val actual = BigDecimal.valueOf(coefficient, scale)
            assertEquals(expected.toFloat().toBits(), actual.toFloat().toBits(), "float $coefficient scale=$scale")
            assertEquals(expected.toDouble().toBits(), actual.toDouble().toBits(), "double $coefficient scale=$scale")
        }
    }
}

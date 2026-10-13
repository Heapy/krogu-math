package io.heapy.krogu.math

import kotlin.test.*

class DecimalCoefficientSubclassTest {
    @Test fun constructorsNormalizeSubclassByteEncoding() {
        for (variant in 0..3) {
            val trace = StringBuilder()
            val source = object : BigInteger("123456789012345678901234567890") {
                override fun toByteArray(): ByteArray { trace.append("bytes;"); return byteArrayOf(42) }
                override fun signum(): Int = error("Stored decimal must not retain the subclass")
                override fun abs(): BigInteger = error("Stored decimal must not retain the subclass")
            }
            val value = when (variant) {
                0 -> BigDecimal(source)
                1 -> BigDecimal(source, 2)
                2 -> BigDecimal(source, MathContext.DECIMAL32)
                else -> BigDecimal(source, 2, MathContext.DECIMAL32)
            }
            assertEquals("bytes;", trace.toString(), "constructor $variant")
            assertEquals(BigInteger("42"), value.unscaledValue())
            assertEquals(BigInteger::class, value.unscaledValue()::class)
            assertEquals(if (variant == 1 || variant == 3) "0.42" else "42", value.toString())
            assertEquals(if (variant == 1 || variant == 3) 0.42 else 42.0, value.toDouble())
        }
    }

    @Test fun ordinaryCoefficientIdentityIsPreserved() {
        val source = BigInteger("123456789012345678901234567890")
        assertSame(source, BigDecimal(source).unscaledValue())
        assertSame(source, BigDecimal(source, 2).unscaledValue())
    }

    @Test fun invalidSubclassEncodingIsRejected() {
        val source = object : BigInteger("123") { override fun toByteArray(): ByteArray = byteArrayOf() }
        assertFailsWith<NumberFormatException> { BigDecimal(source) }
        assertFailsWith<NumberFormatException> { BigDecimal(source, 2) }
    }
}

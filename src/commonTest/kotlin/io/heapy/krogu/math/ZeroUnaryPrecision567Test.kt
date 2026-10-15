package io.heapy.krogu.math

import kotlin.test.*

class ZeroUnaryPrecision567Test {
    @Test fun storedZeroUnaryKeepsPrecisionSentinelAndNullStaging() {
        for (negate in listOf(false, true)) for (precision in listOf(0, 1, 7)) for (mode in RoundingMode.entries) {
            val sentinel = IllegalStateException("precision sentinel")
            val trace = StringBuilder()
            val zero = object : BigDecimal("0.00") {
                override fun precision(): Int { trace.append("precision;"); throw sentinel }
                override fun signum(): Int { trace.append("sign;"); return 0 }
                override fun plus(mc: MathContext?): BigDecimal = error("internal plus must not be called")
            }
            val receiver = object : BigDecimal("-1.00") {
                override fun negate(): BigDecimal { trace.append("negate;"); return zero }
            }
            fun invoke(context: MathContext?): BigDecimal = if (negate) receiver.negate(context) else zero.abs(context)
            val prefix = if (negate) "negate;" else "sign;"
            if (precision == 0) {
                assertSame(zero, invoke(MathContext(precision, mode)))
                assertEquals(prefix, trace.toString())
            } else {
                assertSame(sentinel, assertFailsWith<IllegalStateException> { invoke(MathContext(precision, mode)) })
                assertEquals("${prefix}precision;", trace.toString())
            }
            trace.clear()
            assertFailsWith<NullPointerException> { invoke(null) }
            assertEquals(prefix, trace.toString())
            assertEquals(BigInteger.ZERO, zero.unscaledValue())
            assertEquals(2, zero.scale())
            assertEquals(BigInteger("-100"), receiver.unscaledValue())
            assertEquals(2, receiver.scale())
        }
    }

    @Test fun truthfulNonzeroRoundingRemainsNumericallyCorrect() {
        // Literal quotients/remainders: -123456 / 100000 and 99999 / 10000.
        for (mode in RoundingMode.entries) for (text in listOf("-123.456", "999.99")) {
            for (subclass in listOf(false, true)) {
                val value = if (subclass) object : BigDecimal(text) {
                    override fun precision(): Int = super.precision()
                } else BigDecimal(text)
                if (mode == RoundingMode.UNNECESSARY) {
                    assertFailsWith<ArithmeticException> { value.round(MathContext(1, mode)) }
                    continue
                }
                val pair = if (text == "-123.456") {
                    if (mode in listOf(RoundingMode.UP, RoundingMode.FLOOR)) "-2" to -2 else "-1" to -2
                } else {
                    if (mode in listOf(RoundingMode.DOWN, RoundingMode.FLOOR)) "9" to -2 else "1" to -3
                }
                val result = value.round(MathContext(1, mode))
                assertEquals(BigInteger(pair.first), result.unscaledValue(), "$text/$mode/$subclass")
                assertEquals(pair.second, result.scale(), "$text/$mode/$subclass")
                assertEquals(BigDecimal(text).unscaledValue(), value.unscaledValue())
                assertEquals(if (text == "-123.456") 3 else 2, value.scale())
            }
        }
    }
}

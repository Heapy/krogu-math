package io.heapy.krogu.math

import java.math.BigDecimal as JDecimal
import java.math.BigInteger as JInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** The reference is the live Kotlin `/` expression, including its stdlib rounding contract. */
class DecimalOperatorConformanceTest {
    private data class Outcome(
        val text: String? = null,
        val scale: Int? = null,
        val exceptionType: Class<out Exception>? = null,
    )

    private fun outcome(block: () -> Pair<String, Int>): Outcome = try {
        val (text, scale) = block()
        Outcome(text, scale)
    } catch (failure: Exception) {
        Outcome(exceptionType = failure.javaClass)
    }

    private val coefficients = listOf(
        "0", "1", "-1", "2", "-2", "3", "-3", "5", "-5", "7", "-7",
        "9223372036854775807", "-9223372036854775807",
        "9223372036854775808", "-9223372036854775808",
        "123456789012345678901234567890123456789",
        "-123456789012345678901234567890123456789",
    )

    private fun compareExpression(
        dividendCoefficient: String,
        dividendScale: Int,
        divisorCoefficient: String,
        divisorScale: Int,
    ) {
        val dividend: JDecimal = JDecimal(JInteger(dividendCoefficient), dividendScale)
        val divisor: JDecimal = JDecimal(JInteger(divisorCoefficient), divisorScale)
        val kroguDividend = BigDecimal(BigInteger(dividendCoefficient), dividendScale)
        val kroguDivisor = BigDecimal(BigInteger(divisorCoefficient), divisorScale)
        val expected = outcome {
            // Keep `/` here: replacing it with divide() would test a different contract.
            val quotient: JDecimal = dividend / divisor
            quotient.toString() to quotient.scale()
        }
        val actual = outcome {
            val quotient = kroguDividend / kroguDivisor
            quotient.toString() to quotient.scale()
        }
        assertEquals(
            expected, actual,
            "$dividendCoefficient@$dividendScale / $divisorCoefficient@$divisorScale",
        )
    }

    @Test fun divisionExpressionMatchesKotlinStdlibForSignsScalesAndHalfEvenTies() {
        val scales = listOf(-4, -1, 0, 1, 2, 6)
        // Odd coefficients over +/-2 at scale 0 exercise midpoint ties with both
        // odd and even retained digits. Zero divisors and compact/large pairs are included.
        for (dividendCoefficient in coefficients) for (dividendScale in scales) {
            for (divisorCoefficient in coefficients) for (divisorScale in scales) {
                compareExpression(dividendCoefficient, dividendScale, divisorCoefficient, divisorScale)
            }
        }
    }

    @Test fun divisionExpressionMatchesKotlinStdlibAtSafeSourceScaleBoundaries() {
        val sourceScales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        // A divisor scale of zero keeps the coefficient rescaling distance zero,
        // even at these boundaries; this never constructs enormous powers of ten.
        for (dividendCoefficient in coefficients) for (dividendScale in sourceScales) {
            for (divisorCoefficient in coefficients) {
                compareExpression(dividendCoefficient, dividendScale, divisorCoefficient, 0)
            }
        }
    }

    @Test fun exactDivideRetainsItsSeparateContract() {
        val expectedHalf = JDecimal("1").divide(JDecimal("2"))
        val actualHalf = BigDecimal("1").divide(BigDecimal("2"))
        assertEquals("0.5", expectedHalf.toString())
        assertEquals(1, expectedHalf.scale())
        assertEquals(expectedHalf.toString(), actualHalf.toString())
        assertEquals(expectedHalf.scale(), actualHalf.scale())
        assertFailsWith<ArithmeticException> { JDecimal("1").divide(JDecimal("3")) }
        assertFailsWith<ArithmeticException> { BigDecimal("1").divide(BigDecimal("3")) }
    }
}

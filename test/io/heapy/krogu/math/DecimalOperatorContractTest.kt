package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalOperatorContractTest {
    @Test fun scaleZeroDivisionRoundsInsteadOfRequiringAnExactExpansion() {
        assertDecimal("0", 0, BigDecimal("1") / BigDecimal("2"))
        assertDecimal("0", 0, BigDecimal("1") / BigDecimal("3"))
        assertDecimal("0", 0, BigDecimal("-1") / BigDecimal("3"))
        assertDecimal("0", 0, BigDecimal("1") / BigDecimal("-3"))
        assertDecimal("0", 0, BigDecimal("-1") / BigDecimal("-3"))
        assertDecimal("1", 0, BigDecimal("2") / BigDecimal("3"))
        assertDecimal("-1", 0, BigDecimal("-2") / BigDecimal("3"))
        assertDecimal("-1", 0, BigDecimal("2") / BigDecimal("-3"))
        assertDecimal("1", 0, BigDecimal("-2") / BigDecimal("-3"))
    }

    @Test fun halfwayResultsChooseTheEvenEndpointForBothSigns() {
        assertDecimal("2", 0, BigDecimal("5") / BigDecimal("2"))
        assertDecimal("4", 0, BigDecimal("7") / BigDecimal("2"))
        assertDecimal("-2", 0, BigDecimal("-5") / BigDecimal("2"))
        assertDecimal("-4", 0, BigDecimal("7") / BigDecimal("-2"))
        assertDecimal("0.62", 2, BigDecimal("1.25") / BigDecimal("2"))
        assertDecimal("0.68", 2, BigDecimal("1.35") / BigDecimal("2"))
        assertDecimal("-0.62", 2, BigDecimal("-1.25") / BigDecimal("2"))
        assertDecimal("-0.68", 2, BigDecimal("1.35") / BigDecimal("-2"))
    }

    @Test fun resultRetainsTheDividendScaleRegardlessOfDivisorScale() {
        assertDecimal("0.2", 1, BigDecimal("1.0") / BigDecimal("4.000"))
        assertDecimal("0.8", 1, BigDecimal("3.0") / BigDecimal("4"))
        assertDecimal("0.33", 2, BigDecimal("1.00") / BigDecimal("3.0000"))
        assertDecimal("0.05", 2, BigDecimal("1.00") / BigDecimal("2E+1"))
        assertDecimal("61.725", 3, BigDecimal("123.450") / BigDecimal("2.0"))
        assertDecimal("61728394506172839450.5", 1,
            BigDecimal("123456789012345678901.0") / BigDecimal("2.000"))
    }

    @Test fun negativeDividendScalesAlsoRetainTheirRoundingPlace() {
        assertDecimal("0E+2", -2, BigDecimal("1E+2") / BigDecimal("4.0"))
        assertDecimal("2E+2", -2, BigDecimal("3E+2") / BigDecimal("2"))
        assertDecimal("2E+2", -2, BigDecimal("5E+2") / BigDecimal("2.00"))
        assertDecimal("-2E+2", -2, BigDecimal("-3E+2") / BigDecimal("2"))
        assertDecimal("-2E+2", -2, BigDecimal("5E+2") / BigDecimal("-2.00"))
    }

    @Test fun zeroDividendsRetainScaleAndZeroDivisorsThrowArithmeticException() {
        assertDecimal("0", 0, BigDecimal("0") / BigDecimal("3.00"))
        assertDecimal("0.00", 2, BigDecimal("0.00") / BigDecimal("-3"))
        assertDecimal("0E+3", -3, BigDecimal("0E+3") / BigDecimal("3.00"))
        for (dividend in listOf("1", "-1.00", "0", "0.00", "0E+3")) {
            for (divisor in listOf("0", "0.000", "0E+2")) {
                assertFailsWith<ArithmeticException>("$dividend / $divisor") {
                    BigDecimal(dividend) / BigDecimal(divisor)
                }
            }
        }
    }

    @Test fun namedDivisionStillRequiresAnExactExpansion() {
        assertDecimal("0.5", 1, BigDecimal("1").divide(BigDecimal("2")))
        assertFailsWith<ArithmeticException> {
            BigDecimal("1").divide(BigDecimal("3"))
        }
    }

    private fun assertDecimal(expected: String, expectedScale: Int, actual: BigDecimal) {
        assertEquals(expected, actual.toString())
        assertEquals(expectedScale, actual.scale())
        assertEquals(0, BigDecimal(expected).compareTo(actual))
    }
}

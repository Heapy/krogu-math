// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Truthful public observers; division is never called by these representation controls. */
private class TruthfulReturnedDecimal(coefficient: BigInteger, scale: Int) : BigDecimal(coefficient, scale) {
    override fun unscaledValue(): BigInteger = super.unscaledValue()
    override fun scale(): Int = super.scale()
    override fun precision(): Int = super.precision()
    override fun signum(): Int = super.signum()
    override fun toString(): String = super.toString()
}

private fun returnedRepresentationCanonical(coefficient: String, scale: Int): String {
    val digits = coefficient.removePrefix("-")
    val sign = if (coefficient.startsWith('-')) "-" else ""
    val exponent = digits.length.toLong() - 1L - scale.toLong()
    val body = if (scale >= 0 && exponent >= -6L) {
        val point = digits.length.toLong() - scale.toLong()
        when {
            scale == 0 -> digits
            point <= 0L -> "0." + "0".repeat((-point).toInt()) + digits
            else -> digits.take(point.toInt()) + "." + digits.drop(point.toInt())
        }
    } else {
        val significand = if (digits.length == 1) digits else digits.take(1) + "." + digits.drop(1)
        significand + "E" + (if (exponent >= 0L) "+" else "") + exponent
    }
    return sign + body
}

class DecimalContextReturnedRepresentationContractTest {
    @Test fun everyFrozenSuccessfulRepresentationHasDirectOrdinaryAndTruthfulControls() {
        val cases = decimalContextReturnedRepresentations()
        assertEquals(675, cases.size)
        assertEquals(675, cases.map { it.coefficient to it.scale }.toSet().size)
        assertEquals(58093, cases.sumOf { it.occurrences })
        assertEquals(341, cases.count { !it.coveredByOrdinaryControls })
        for (row in cases) {
            assertTrue(row.occurrences > 0 && row.firstOldId.startsWith("D"))
            val independentlyExpected = returnedRepresentationCanonical(row.coefficient, row.scale)
            assertEquals(independentlyExpected, row.expectedCanonical, "Expected formatter: ${row.firstOldId}")
            assertEquals(independentlyExpected, row.observedCanonical, "Frozen spelling: ${row.firstOldId}")
            checkDirectRepresentations(row.coefficient, row.scale, independentlyExpected, row.firstOldId)
        }
    }

    @Test fun independentLiteralThresholdZeroSignAndExtremeScaleWitnesses() {
        val literals = listOf(
            Triple("1", 6, "0.000001"), Triple("1", 7, "1E-7"),
            Triple("123", 8, "0.00000123"), Triple("123", 9, "1.23E-7"),
            Triple("12300", 4, "1.2300"), Triple("12300", 0, "12300"),
            Triple("12300", -1, "1.2300E+5"), Triple("-12300", 4, "-1.2300"),
            Triple("-123", 9, "-1.23E-7"), Triple("0", 0, "0"), Triple("0", 2, "0.00"),
            Triple("0", 6, "0.000000"), Triple("0", 7, "0E-7"), Triple("0", -2, "0E+2"),
            Triple("1", Int.MIN_VALUE, "1E+2147483648"),
            Triple("-12", Int.MIN_VALUE, "-1.2E+2147483649"),
            Triple("123", Int.MAX_VALUE, "1.23E-2147483645"),
            Triple("0", Int.MIN_VALUE, "0E+2147483648"),
            Triple("0", Int.MAX_VALUE, "0E-2147483647"),
        )
        for ((coefficient, scale, literal) in literals) {
            assertEquals(literal, returnedRepresentationCanonical(coefficient, scale), "Independent literal")
            checkDirectRepresentations(coefficient, scale, literal, "literal $coefficient@$scale")
        }
    }

    private fun checkDirectRepresentations(coefficient: String, scale: Int, expectedText: String, label: String) {
        val integer = BigInteger(coefficient)
        for (truthful in listOf(false, true)) {
            val value: BigDecimal = if (truthful) TruthfulReturnedDecimal(integer, scale) else BigDecimal(integer, scale)
            assertEquals(integer, value.unscaledValue(), "$label truthful=$truthful coefficient")
            assertEquals(coefficient, value.unscaledValue().toString(), "$label truthful=$truthful coefficient text")
            assertEquals(scale, value.scale(), "$label truthful=$truthful scale")
            assertEquals(expectedText, value.toString(), "$label truthful=$truthful canonical text")
        }
    }
}

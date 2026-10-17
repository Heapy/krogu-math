// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Literal and rational-square controls; no implementation sqrt supplies an oracle. */
class SqrtCohort587ComparisonTest {
    private fun input(text: String): SqrtCohort587Comparison.Input = SqrtCohort587Comparison.parseInput(text)
    private fun fields(text: String) = SqrtCohort587Comparison.parse(text)
    private fun certificate(x: String, y: String, p: Int, mode: RoundingMode) =
        SqrtCohort587Comparison.certify(input(x), fields(y), p, mode)

    @Test fun threeFrozenKeysRequireLiteralFullFields() {
        val required = mapOf(13823 to "31622776601683793320@23",
            14089 to "170025326089541060@23", 15172 to "30156158702369580@9")
        for ((ordinal, row) in SqrtCohort587Comparison.raw) {
            val original = row.toList()
            val result = SqrtCohort587Comparison.decide(input(row[2]), row[4].toInt(),
                RoundingMode.valueOf(row[5]), fields(row[6]))
            assertEquals(required.getValue(ordinal), "${result.required.coefficient}@${result.required.scale}")
            assertEquals(row[4].toInt(), result.required.precision)
            assertEquals("LIFT_INEXACT", result.disposition)
            assertEquals(original, row, "immutable frozen input")
        }
    }

    @Test fun exactAndZeroCohortsRemainAndExactOnlyValuesAreChecked() {
        for (p in listOf(0, 8)) for (mode in RoundingMode.entries) {
            val exact = SqrtCohort587Comparison.decide(input("4.000000000000"), p, mode, fields("2.000000"))
            assertEquals(fields("2.000000"), exact.required)
            assertEquals("UNCHANGED_EXACT", exact.disposition)
            val zero = SqrtCohort587Comparison.decide(input("0.000000"), p, mode, fields("0.000"))
            assertEquals(fields("0.000"), zero.required)
            assertEquals("UNCHANGED_ZERO", zero.disposition)
        }
        for ((p, mode) in listOf(0 to RoundingMode.DOWN, 7 to RoundingMode.UNNECESSARY))
            assertFailsWith<IllegalArgumentException> {
                SqrtCohort587Comparison.decide(input("3"), p, mode, fields("1.73205"))
            }
        val directed = SqrtCohort587Comparison.decide(input("3"), 7, RoundingMode.DOWN, fields("1.73205"))
        assertEquals(fields("1.732050"), directed.required)
        // 1.732050^2=2.999997202500 <3<1.732051^2=3.000000666601.
        assertEquals("DOWN_ADJACENT", directed.certificate)
        assertFailsWith<IllegalArgumentException> {
            SqrtCohort587Comparison.decide(input("-3"), 7, RoundingMode.DOWN, fields("1.73205"))
        }
        assertFailsWith<IllegalArgumentException> {
            SqrtCohort587Comparison.decide(input("0"), 7, RoundingMode.UP, fields("1"))
        }
    }

    @Test fun directedAndNearestIntervalsRejectWrongNumbers() {
        for (mode in listOf(RoundingMode.DOWN, RoundingMode.FLOOR)) {
            certificate("2", "1.4", 2, mode) // 1.96 <2<2.25.
            assertFailsWith<IllegalArgumentException> { certificate("2", "1.3", 2, mode) }
        }
        for (mode in listOf(RoundingMode.UP, RoundingMode.CEILING)) {
            certificate("2", "1.5", 2, mode)
            assertFailsWith<IllegalArgumentException> { certificate("2", "1.6", 2, mode) }
        }
        for (mode in listOf(RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN)) {
            certificate("2", "1.4", 2, mode) // 1.35^2 <2<1.45^2.
            assertFailsWith<IllegalArgumentException> { certificate("2", "1.5", 2, mode) }
        }
        assertFailsWith<IllegalArgumentException> {
            SqrtCohort587Comparison.decide(input("2"), 3, RoundingMode.DOWN, fields("1.3"))
        }
    }

    @Test fun finiteHalfwayRootsAndDecadeParityUseActualNeighbors() {
        // sqrt(2.25)=1.5: lower midpoint of candidate2, upper midpoint of1.
        for (mode in listOf(RoundingMode.HALF_UP, RoundingMode.HALF_EVEN)) certificate("2.25", "2", 1, mode)
        certificate("2.25", "1", 1, RoundingMode.HALF_DOWN)
        assertFailsWith<IllegalArgumentException> { certificate("2.25", "2", 1, RoundingMode.HALF_DOWN) }
        assertFailsWith<IllegalArgumentException> { certificate("2.25", "1", 1, RoundingMode.HALF_EVEN) }
        // sqrt(90.25)=9.5: predecessor of1E+1 is9, and aligned10 is EVEN.
        certificate("90.25", "1E+1", 1, RoundingMode.HALF_EVEN)
        certificate("90.25", "1E+1", 1, RoundingMode.HALF_UP)
        certificate("90.25", "9", 1, RoundingMode.HALF_DOWN)
        assertFailsWith<IllegalArgumentException> { certificate("90.25", "1E+1", 1, RoundingMode.HALF_DOWN) }
        assertFailsWith<IllegalArgumentException> { certificate("90.25", "9", 1, RoundingMode.HALF_EVEN) }
        // Lower neighbor of1.000000 is0.9999999, not0.999999.
        certificate("0.99999999", "1.000000", 7, RoundingMode.UP)
        certificate("0.99999999", "1.000000", 7, RoundingMode.HALF_EVEN)
        assertFailsWith<IllegalArgumentException> { certificate("0.9999998", "1.000000", 7, RoundingMode.UP) }
        certificate("99.980001", "10.0", 3, RoundingMode.UP) // sqrt=9.999, below10.
    }

    @Test fun largestProfileUsesBoundedLongDigitCertificates() {
        // x=1-10^-121. Its root is between the predecessor of the120-digit1
        // and1, and above their midpoint: (1-h/2)^2 <1-h/10 for h=10^-120.
        val source = "0." + "9".repeat(121)
        val decision = SqrtCohort587Comparison.decide(input(source), 120, RoundingMode.HALF_EVEN, fields("1"))
        assertEquals("1" + "0".repeat(119), decision.required.coefficient)
        assertEquals(119, decision.required.scale)
        assertEquals(120, decision.required.precision)
        certificate(source, decision.required.text, 120, RoundingMode.UP)
        // x=100-10^-150; y=10-10^-119 has120 digits and y²<x<100.
        // This exercises the all9 successor carry and a152-digit input.
        val near100 = PreferredScaleStringModel.text("${"9".repeat(152)}@150")
        val below10 = PreferredScaleStringModel.text("${"9".repeat(120)}@119")
        certificate(near100, below10, 120, RoundingMode.DOWN)
        assertFailsWith<IllegalArgumentException> { certificate(near100, below10, 120, RoundingMode.UP) }
    }

    @Test fun scalesUseLongAndCanonicalTextIsStrict() {
        assertEquals(SqrtCohort587Comparison.Input("1", 7, "0.0000001"), input("0.0000001"))
        assertEquals(SqrtCohort587Comparison.Input("1", 7, "1E-7"), input("1E-7"))
        for (rootScale in listOf(-1073741824, 1073741823)) {
            val sourceScale = 2 * rootScale // Both literal endpoints fit Int.
            val x = SqrtCohort587Comparison.Input("3", sourceScale, "3@$sourceScale")
            val r = fields(PreferredScaleStringModel.text("173205@${rootScale + 5}"))
            val decision = SqrtCohort587Comparison.decide(x, 7, RoundingMode.DOWN, r)
            assertEquals("1732050", decision.required.coefficient)
            assertEquals(rootScale + 6, decision.required.scale)
            assertEquals("DOWN_ADJACENT", decision.certificate)
        }
        for (text in listOf("0.000", "1E+2147483648", "1E-2147483647", "1.2300", "-1.00")) {
            val parsed = fields(text)
            assertEquals(text, PreferredScaleStringModel.text("${parsed.coefficient}@${parsed.scale}"))
        }
        for (text in listOf("01", "+1", "1e0", "-0", "1.0E+0", "1E-2147483648", "0.0000001"))
            assertFailsWith<IllegalArgumentException> { fields(text) }
        for (text in listOf("", "x", "1..2", "1E-2147483648"))
            assertFailsWith<IllegalArgumentException> { input(text) }
    }

    @Test fun everyRequiredFieldKindAndObservationStageMatters() {
        val requiredFields = fields("1.732050")
        val required = SqrtCohort587Comparison.Outcome(requiredFields.text, requiredFields)
        assertTrue(SqrtCohort587Comparison.strictMatch(required, required))
        for (wrong in listOf(requiredFields.copy(coefficient = "1732051"), requiredFields.copy(scale = 5),
            requiredFields.copy(precision = 6), requiredFields.copy(text = "1.73205")))
            assertFalse(SqrtCohort587Comparison.strictMatch(required,
                SqrtCohort587Comparison.Outcome(wrong.text, wrong)))
        assertFalse(SqrtCohort587Comparison.strictMatch(required, SqrtCohort587Comparison.frozen("!ArithmeticException")))
        assertFalse(SqrtCohort587Comparison.strictMatch(required, required.copy(fields = null)))
        assertFalse(SqrtCohort587Comparison.strictMatch(required, required.copy(unexpected = true)))
        val operationAE = SqrtCohort587Comparison.reference<Int>({ throw ArithmeticException("operation") }) {
            error("not observed")
        }
        assertEquals("ArithmeticException", operationAE.errorType)
        assertFalse(operationAE.unexpected)
        val metadataAE = SqrtCohort587Comparison.reference({ 1 }) { throw ArithmeticException("metadata") }
        assertTrue(metadataAE.unexpected)
        val other = SqrtCohort587Comparison.captureText { throw IllegalStateException("unexpected") }
        assertTrue(other.unexpected)
        val error = SqrtCohort587Comparison.frozen("!ArithmeticException")
        assertTrue(SqrtCohort587Comparison.strictMatch(error, operationAE))
        assertFalse(SqrtCohort587Comparison.strictMatch(error, metadataAE))
        assertTrue(SqrtCohort587Comparison.frozen("!IllegalStateException").unexpected)
    }
}

package io.heapy.krogu.math

import java.math.BigDecimal as JD
import java.math.MathContext as JC
import java.math.RoundingMode as JR
import kotlin.test.Test
import kotlin.test.assertEquals

/** Compact coefficients exercise the full signed scale range without huge allocations. */
class ScaleBoundaryConformanceTest {
    @Test fun wideCoefficientsAtScaleLimits() {
        val random = kotlin.random.Random(20261015)
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 2, Int.MIN_VALUE + 16,
            Int.MIN_VALUE + 32, Int.MIN_VALUE / 2, -10, 0, 10, Int.MAX_VALUE / 2,
            Int.MAX_VALUE - 32, Int.MAX_VALUE - 16, Int.MAX_VALUE - 2, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        val counts = mutableMapOf<String, Int>()
        val failures = mutableListOf<String>()
        var comparisons = 0
        val roots = SqrtCohort587Comparison.Recorder("scale-live-wide", SqrtCohort587Comparison.Profile.WIDE, 16000, "LIVE_JDK")
        fun coefficient(): java.math.BigInteger {
            val value = if (random.nextBoolean()) {
                java.math.BigInteger.TEN.pow(listOf(1, 16, 17, 18, 19, 31, 32, 63, 64, 100)[random.nextInt(10)])
                    .add(java.math.BigInteger.valueOf(random.nextLong(-1, 2)))
            } else java.math.BigInteger(random.nextBytes(listOf(1, 4, 8, 16, 32, 64)[random.nextInt(6)]))
            return if (random.nextBoolean()) value.negate() else value
        }
        fun check(label: String, reference: () -> Any?, implementation: () -> Any?) {
            comparisons++
            val expected = SqrtCohort587Comparison.captureText(reference)
            val actual = SqrtCohort587Comparison.captureText(implementation)
            if (expected.text != actual.text || expected.unexpected || actual.unexpected) {
                val category = label.substringBefore(' ')
                val previous = counts[category] ?: 0
                counts[category] = previous + 1
                println("587_SCALE_OTHER_FAILURE\twide\t$comparisons\t$label\t$expected\t$actual")
                if (previous < 6) failures.add("$label expected=$expected actual=$actual")
            }
        }
        repeat(2000) {
            val x = JD(coefficient(), scales[random.nextInt(scales.size)])
            val y = JD(coefficient(), scales[random.nextInt(scales.size)])
            val a = BigDecimal(BigInteger(x.unscaledValue().toString()), x.scale())
            val b = BigDecimal(BigInteger(y.unscaledValue().toString()), y.scale())
            check("parse $x", { JD(x.toString()) }, { BigDecimal(x.toString()) })
            check("exactDivide $x,$y", { divisionReference(a, b, null) { x.divide(y) } }, { a.divide(b) })
            for (p in listOf(1, 2, 7, 17, 18, 19, 34, 120)) {
                val mode = RoundingMode.entries[random.nextInt(8)]
                val mc = MathContext(p, mode); val jc = JC(p, JR.valueOf(mode.name))
                val label = "$x,$y,$mc"
                check("add $label", {
                    additionReference(x, y, jc, a, b, mc)
                }, { a.add(b, mc) })
                check("divide $label", { divisionReference(a, b, mc) { x.divide(y, jc) } }, { a.divide(b, mc) })
                check("multiply $label", {
                    scaleRoundingReference("multiply $label", DecimalScaleRounding566Expected.multiplication(a, b, mc)) { x.multiply(y, jc) }
                }, { a.multiply(b, mc) })
                comparisons++
                recordSqrt(roots, comparisons.toString(), a, mc) { x.sqrt(jc) }
            }
        }
        assertEquals(68000, comparisons)
        roots.finish()
        assertEquals(emptyMap(), counts, "$comparisons comparisons; discrepancy counts: $counts\n${failures.joinToString("\n")}")
        println("$comparisons wide-coefficient scale-boundary comparisons passed")
    }

    @Test fun arithmeticAtSignedScaleLimits() {
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 5, -1, 0, 1, Int.MAX_VALUE - 5, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        val inputs = listOf(-1000L, -100, -3, -1, 0, 1, 3, 100, 1000).flatMap { n -> scales.map { JD.valueOf(n, it) } }
        val failures = mutableListOf<String>()
        var comparisons = 0
        var directZeroRounds = 0
        var otherFailures = 0
        val roots = SqrtCohort587Comparison.Recorder("scale-live-compact", SqrtCohort587Comparison.Profile.COMPACT, 2592, "LIVE_JDK")
        fun check(label: String, reference: () -> Any?, implementation: () -> Any?) {
            comparisons++
            val expected = SqrtCohort587Comparison.captureText(reference)
            val actual = SqrtCohort587Comparison.captureText(implementation)
            if (expected.text != actual.text || expected.unexpected || actual.unexpected) {
                otherFailures++
                println("587_SCALE_OTHER_FAILURE\tcompact\t$comparisons\t$label\t$expected\t$actual")
                if (failures.size < 80) failures.add("$label expected=$expected actual=$actual")
            }
        }
        for (x in inputs) {
            val a = BigDecimal(x.unscaledValue().toString().let(::BigInteger), x.scale())
            check("canonical parse $x", { JD(x.toString()) }, { BigDecimal(x.toString()) })
            check("engineering $x", { x.toEngineeringString() }, { a.toEngineeringString() })
            check("strip $x", { x.stripTrailingZeros() }, { a.stripTrailingZeros() })
            for (y in inputs) {
                val b = BigDecimal(BigInteger(y.unscaledValue().toString()), y.scale())
                check("multiply $x,$y", { x.multiply(y) }, { a.multiply(b) })
                check("exact divide $x,$y", { divisionReference(a, b, null) { x.divide(y) } }, { a.divide(b) })
                for (p in listOf(1, 2, 7)) for (mode in listOf(RoundingMode.DOWN, RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)) {
                    val mc = MathContext(p, mode); val jc = JC(p, JR.valueOf(mode.name))
                    check("divide $x,$y,$mc", { divisionReference(a, b, mc) { x.divide(y, jc) } }, { a.divide(b, mc) })
                    check("add $x,$y,$mc", { additionReference(x, y, jc, a, b, mc) }, { a.add(b, mc) })
                }
            }
            for (p in listOf(0, 1, 2, 7)) for (mode in RoundingMode.entries) {
                val mc = MathContext(p, mode); val jc = JC(p, JR.valueOf(mode.name))
                comparisons++
                recordSqrt(roots, comparisons.toString(), a, mc) { x.sqrt(jc) }
                // x/a are ordinary values copied from literal stored coefficients.
                if (x.unscaledValue() == java.math.BigInteger.ZERO && x.scale() != 0) {
                    directZeroRounds++
                    val diagnostic = x.round(jc).toString()
                    assertEquals(x.toString(), diagnostic, "retained task567 JDK round: $x $mc")
                    check("round $x,$mc; JDK=$diagnostic", { "0" }, {
                        val result = a.round(mc)
                        assertEquals(BigInteger.ZERO, result.unscaledValue(), "round coefficient: $x $mc")
                        assertEquals(0, result.scale(), "round scale: $x $mc")
                        result
                    })
                    println("567_DIRECT_ZERO_OBSERVATION\tround $x,$mc\tfrozen=$diagnostic")
                } else check("round $x,$mc", { x.round(jc) }, { a.round(mc) })
                check("pow $x,2,$mc", { x.pow(2, jc) }, { a.pow(2, mc) })
            }
        }
        assertEquals(139239, comparisons)
        assertEquals(256, directZeroRounds)
        roots.finish()
        assertEquals(0, otherFailures, "$comparisons comparisons; first discrepancies:\n${failures.joinToString("\n")}")
        println("$comparisons signed-scale-boundary comparisons passed")
    }

    private fun recordSqrt(recorder: SqrtCohort587Comparison.Recorder, key: String,
        a: BigDecimal, mc: MathContext, reference: () -> JD) {
        val corrected = DecimalExactRoot563Expected.corrected(a.unscaledValue().toString(), a.scale(), mc)
        val diagnostic = SqrtCohort587Comparison.reference(reference) { r ->
            SqrtCohort587Comparison.Fields(r.unscaledValue().toString(), r.scale(), r.precision(), r.toString())
        }
        // Retain the old task563 extra own call and its coefficient/scale checks.
        val control = corrected?.let { SqrtCohort587Comparison.own { a.sqrt(mc) } }
        val actual = SqrtCohort587Comparison.own { a.sqrt(mc) }
        recorder.record(key, SqrtCohort587Comparison.input(a), mc.precision, mc.roundingMode,
            diagnostic, actual, corrected?.let { SqrtCohort587Comparison.parse(it.text()) }, control)
    }

    private fun divisionReference(a: BigDecimal, b: BigDecimal, mc: MathContext?, reference: () -> JD): Any {
        val corrected = DivisionNormalizationExpected.correctedText(a, b, mc)
            ?: PreferredScaleStringModel.correctedText(a, b, mc) ?: return reference()
        val diagnostic = try { reference().toString() }
            catch (_: ArithmeticException) { "!ArithmeticException" }
        assertEquals("!ArithmeticException", diagnostic, "retained JDK division normalization defect: $a / $b $mc")
        return corrected
    }

    private fun additionReference(x: JD, y: JD, jc: JC, a: BigDecimal, b: BigDecimal, mc: MathContext): Any {
        val corrected = DecimalScaleRounding566Expected.addition(a, b, mc)
        if (corrected != null)
            return scaleRoundingReference("add $x,$y,$mc", corrected) { x.add(y, jc) }
        if (!DecimalScaleContractExpected.affectedAddition(a, b, mc)) return x.add(y, jc)
        val diagnostic = try { x.add(y, jc).toString() }
            catch (_: ArithmeticException) { "!ArithmeticException" }
        assertEquals("!ArithmeticException", diagnostic, "retained JDK addition defect")
        return DecimalScaleContractExpected.addition(a, b, mc)
    }

    private fun scaleRoundingReference(label: String, corrected: String?, reference: () -> JD): Any {
        if (corrected == null) return reference()
        val diagnostic = try { reference().toString() }
            catch (e: ArithmeticException) { "!ArithmeticException:${e.message}" }
        println("566_JDK_OBSERVATION\t$label\t$diagnostic")
        return corrected
    }
}

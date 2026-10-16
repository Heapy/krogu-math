package io.heapy.krogu.math

import java.math.BigDecimal as JD
import java.math.MathContext as JC
import java.math.RoundingMode as JR
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/** Structured, reproducible differential campaigns; failures retain full public input data. */
class DecimalConformanceTest {
    private val failures = mutableListOf<String>()
    private var comparisons = 0L
    private fun outcome(block: () -> Any?): String = try {
        when (val value = block()) {
            is Array<*> -> value.joinToString("|")
            else -> value.toString()
        }
    } catch (e: Exception) { "!${e.javaClass.simpleName}" }
    private fun check(label: String, reference: () -> Any?, implementation: () -> Any?) {
        comparisons++
        val expected = outcome(reference)
        val actual = outcome(implementation)
        if (expected != actual && failures.size < 80) failures.add("$label: expected=$expected actual=$actual")
    }
    private fun finish() {
        assertEquals(emptyList(), failures, "$comparisons comparisons; first discrepancies:\n${failures.joinToString("\n")}")
        println("$comparisons JDK comparisons passed")
    }

    @Test fun structuredRoundingAndPreferredScales() {
        val coefficients = listOf(-1000L, -101, -100, -11, -10, -9, -5, -2, -1, 0, 1, 2, 5, 9, 10, 11, 100, 101, 1000)
        val scales = listOf(-8, -2, -1, 0, 1, 2, 8)
        val inputs = coefficients.flatMap { n -> scales.map { JD.valueOf(n, it) } }
        val random = Random(20261013)
        repeat(4000) {
            val x = inputs[random.nextInt(inputs.size)]
            val y = inputs[random.nextInt(inputs.size)]
            val a = BigDecimal(x.toString()); val b = BigDecimal(y.toString())
            for (mode in RoundingMode.entries) {
                val p = listOf(1, 2, 3, 7)[random.nextInt(4)]
                val mc = MathContext(p, mode); val jc = JC(p, JR.valueOf(mode.name))
                val label = "$x,$y,$mc"
                check("add $label", { x.add(y, jc) }, { a.add(b, mc) })
                check("subtract $label", { x.subtract(y, jc) }, { a.subtract(b, mc) })
                check("multiply $label", { x.multiply(y, jc) }, { a.multiply(b, mc) })
                check("divide $label", { x.divide(y, jc) }, { a.divide(b, mc) })
                check("integral $label", { x.divideToIntegralValue(y, jc) }, { a.divideToIntegralValue(b, mc) })
                check("remainder $label", { x.divideAndRemainder(y, jc) }, { a.divideAndRemainder(b, mc) })
            }
        }
        finish()
    }

    @Test fun rootsAndPowers() {
        val checks = SqrtCohort587Comparison.Checks("fixed-roots-powers")
        val roots = SqrtCohort587Comparison.Recorder("fixed-roots-powers", SqrtCohort587Comparison.Profile.FIXED, 960, "LIVE_JDK")
        val inputs = listOf("0", "0.00", "0E+5", "0E-5", "1.0000", "4.00", "100.00", "0.01", "2", "3", "5", "10", "81", "0.99999999", "1.00000001", "9999", "999999", "1E-30", "1E+31", "10000000000000000000000000001")
        for (s in inputs) for (mode in RoundingMode.entries) for (p in listOf(0, 1, 2, 3, 7, 16)) {
            val x = JD(s); val a = BigDecimal(s)
            val mc = MathContext(p, mode); val jc = JC(p, JR.valueOf(mode.name))
            checks.accountSqrt()
            val reference = SqrtCohort587Comparison.reference({ x.sqrt(jc) }) { r ->
                SqrtCohort587Comparison.Fields(r.unscaledValue().toString(), r.scale(), r.precision(), r.toString())
            }
            val actual = SqrtCohort587Comparison.own { a.sqrt(mc) }
            roots.record("$s/$p/$mode", SqrtCohort587Comparison.input(a, s), p, mode, reference, actual)
            for (n in listOf(-7, -3, -1, 0, 1, 2, 3, 7, 10, 31))
                checks.check("pow $s,$n,$mc", { x.pow(n, jc) }, { a.pow(n, mc) })
        }
        roots.finish()
        checks.finish(10560)
    }

    @Test fun doubleCanonicalizationCampaign() {
        val random = Random(20261014)
        val count = System.getProperty("krogu.campaign.doubles", "10000").toInt()
        repeat(count) {
            val bits = when (it % 5) {
                0 -> random.nextLong(1, 1L shl 52)
                1 -> random.nextLong(1, 1L shl 52) or Long.MIN_VALUE
                else -> random.nextLong()
            }
            val d = Double.fromBits(bits)
            check("valueOf bits=$bits d=$d", { JD.valueOf(d) }, { BigDecimal.valueOf(d) })
        }
        finish()
    }

    @Test fun byteAndCharacterConstructorRanges() {
        val bytes = listOf(byteArrayOf(), byteArrayOf(0), byteArrayOf(-1), byteArrayOf(0, -128, 1))
        for (input in bytes) for (off in -2..input.size + 1) for (len in -2..input.size + 1) {
            check("bytes ${input.toList()},$off,$len", { java.math.BigInteger(input, off, len) }, { BigInteger(input, off, len) })
            for (sign in -2..2)
                check("sign bytes $sign,${input.toList()},$off,$len", { java.math.BigInteger(sign, input, off, len) }, { BigInteger(sign, input, off, len) })
        }
        for (text in listOf("", "0", "1.00", "-123E+4", "١٢٣.٤٥")) {
            val chars = text.toCharArray()
            for (off in -1..chars.size + 1) for (len in -1..chars.size + 1)
                check("chars [$text],$off,$len", { JD(chars, off, len) }, { BigDecimal(chars, off, len) })
        }
        finish()
    }
}

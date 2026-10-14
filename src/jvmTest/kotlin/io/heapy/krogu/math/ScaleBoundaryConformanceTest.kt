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
        fun coefficient(): java.math.BigInteger {
            val value = if (random.nextBoolean()) {
                java.math.BigInteger.TEN.pow(listOf(1, 16, 17, 18, 19, 31, 32, 63, 64, 100)[random.nextInt(10)])
                    .add(java.math.BigInteger.valueOf(random.nextLong(-1, 2)))
            } else java.math.BigInteger(random.nextBytes(listOf(1, 4, 8, 16, 32, 64)[random.nextInt(6)]))
            return if (random.nextBoolean()) value.negate() else value
        }
        fun outcome(action: () -> Any?): String = try { action().toString() }
            catch (e: Exception) { "!${e.javaClass.simpleName}" }
        fun check(label: String, reference: () -> Any?, implementation: () -> Any?) {
            comparisons++
            val expected = outcome(reference); val actual = outcome(implementation)
            if (expected != actual) {
                val category = label.substringBefore(' ')
                val previous = counts[category] ?: 0
                counts[category] = previous + 1
                if (previous < 6) failures.add("$label expected=$expected actual=$actual")
            }
        }
        repeat(2000) {
            val x = JD(coefficient(), scales[random.nextInt(scales.size)])
            val y = JD(coefficient(), scales[random.nextInt(scales.size)])
            val a = BigDecimal(BigInteger(x.unscaledValue().toString()), x.scale())
            val b = BigDecimal(BigInteger(y.unscaledValue().toString()), y.scale())
            check("parse $x", { JD(x.toString()) }, { BigDecimal(x.toString()) })
            check("exactDivide $x,$y", { x.divide(y) }, { a.divide(b) })
            for (p in listOf(1, 2, 7, 17, 18, 19, 34, 120)) {
                val mode = RoundingMode.entries[random.nextInt(8)]
                val mc = MathContext(p, mode); val jc = JC(p, JR.valueOf(mode.name))
                val label = "$x,$y,$mc"
                check("add $label", {
                    additionReference(x, y, jc, a, b, mc)
                }, { a.add(b, mc) })
                check("divide $label", { x.divide(y, jc) }, { a.divide(b, mc) })
                check("multiply $label", { x.multiply(y, jc) }, { a.multiply(b, mc) })
                check("sqrt $x,$mc", { x.sqrt(jc) }, { a.sqrt(mc) })
            }
        }
        assertEquals(emptyMap(), counts, "$comparisons comparisons; discrepancy counts: $counts\n${failures.joinToString("\n")}")
        println("$comparisons wide-coefficient scale-boundary comparisons passed")
    }

    @Test fun arithmeticAtSignedScaleLimits() {
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 5, -1, 0, 1, Int.MAX_VALUE - 5, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        val inputs = listOf(-1000L, -100, -3, -1, 0, 1, 3, 100, 1000).flatMap { n -> scales.map { JD.valueOf(n, it) } }
        val failures = mutableListOf<String>()
        var comparisons = 0
        fun outcome(action: () -> Any?): String = try { action().toString() }
            catch (e: Exception) { "!${e.javaClass.simpleName}" }
        fun check(label: String, reference: () -> Any?, implementation: () -> Any?) {
            comparisons++
            val expected = outcome(reference); val actual = outcome(implementation)
            if (expected != actual && failures.size < 80) failures.add("$label expected=$expected actual=$actual")
        }
        for (x in inputs) {
            val a = BigDecimal(x.unscaledValue().toString().let(::BigInteger), x.scale())
            check("canonical parse $x", { JD(x.toString()) }, { BigDecimal(x.toString()) })
            check("engineering $x", { x.toEngineeringString() }, { a.toEngineeringString() })
            check("strip $x", { x.stripTrailingZeros() }, { a.stripTrailingZeros() })
            for (y in inputs) {
                val b = BigDecimal(BigInteger(y.unscaledValue().toString()), y.scale())
                check("multiply $x,$y", { x.multiply(y) }, { a.multiply(b) })
                check("exact divide $x,$y", { x.divide(y) }, { a.divide(b) })
                for (p in listOf(1, 2, 7)) for (mode in listOf(RoundingMode.DOWN, RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)) {
                    val mc = MathContext(p, mode); val jc = JC(p, JR.valueOf(mode.name))
                    check("divide $x,$y,$mc", { x.divide(y, jc) }, { a.divide(b, mc) })
                    check("add $x,$y,$mc", { additionReference(x, y, jc, a, b, mc) }, { a.add(b, mc) })
                }
            }
            for (p in listOf(0, 1, 2, 7)) for (mode in RoundingMode.entries) {
                val mc = MathContext(p, mode); val jc = JC(p, JR.valueOf(mode.name))
                check("sqrt $x,$mc", { x.sqrt(jc) }, { a.sqrt(mc) })
                check("round $x,$mc", { x.round(jc) }, { a.round(mc) })
                check("pow $x,2,$mc", { x.pow(2, jc) }, { a.pow(2, mc) })
            }
        }
        assertEquals(emptyList(), failures, "$comparisons comparisons; first discrepancies:\n${failures.joinToString("\n")}")
        println("$comparisons signed-scale-boundary comparisons passed")
    }

    private fun additionReference(x: JD, y: JD, jc: JC, a: BigDecimal, b: BigDecimal, mc: MathContext): Any {
        if (!DecimalScaleContractExpected.affectedAddition(a, b, mc)) return x.add(y, jc)
        val diagnostic = try { x.add(y, jc).toString() }
            catch (_: ArithmeticException) { "!ArithmeticException" }
        assertEquals("!ArithmeticException", diagnostic, "retained JDK addition defect")
        return DecimalScaleContractExpected.addition(a, b, mc)
    }
}

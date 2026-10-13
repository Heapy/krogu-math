package io.heapy.krogu.math

import java.math.BigDecimal as JD
import java.math.MathContext as JC
import java.math.RoundingMode as JR
import kotlin.test.Test
import kotlin.test.assertEquals

/** Compact coefficients exercise the full signed scale range without huge allocations. */
class ScaleBoundaryConformanceTest {
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
                    check("add $x,$y,$mc", { x.add(y, jc) }, { a.add(b, mc) })
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
}

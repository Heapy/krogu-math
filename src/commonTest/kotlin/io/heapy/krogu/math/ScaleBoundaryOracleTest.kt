package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScaleBoundaryOracleTest {
    @Test fun everyStructuredScaleBoundaryOutcomeMatchesJdk25() {
        val expected = scaleBoundaryExpected().iterator()
        var count = 0
        fun verify(label: String, action: () -> Any?) {
            assertTrue(expected.hasNext(), "Oracle ended before $label")
            val actual = try { action().toString() } catch (e: Exception) { "!${e::class.simpleName}" }
            assertEquals(expected.next(), actual, "$count: $label")
            count++
        }
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 5, -1, 0, 1, Int.MAX_VALUE - 5, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        val inputs = listOf(-1000L, -100, -3, -1, 0, 1, 3, 100, 1000).flatMap { n -> scales.map { BigDecimal.valueOf(n, it) } }
        for (a in inputs) {
            verify("canonical parse $a") { BigDecimal(a.toString()) }
            verify("engineering $a") { a.toEngineeringString() }
            verify("strip $a") { a.stripTrailingZeros() }
            for (b in inputs) {
                verify("multiply $a,$b") { a.multiply(b) }
                verify("exact divide $a,$b") { a.divide(b) }
                for (p in listOf(1, 2, 7)) for (mode in listOf(RoundingMode.DOWN, RoundingMode.HALF_EVEN, RoundingMode.UNNECESSARY)) {
                    val context = MathContext(p, mode)
                    verify("divide $a,$b,$context") { a.divide(b, context) }
                    verify("add $a,$b,$context") { a.add(b, context) }
                }
            }
            for (p in listOf(0, 1, 2, 7)) for (mode in RoundingMode.entries) {
                val context = MathContext(p, mode)
                verify("sqrt $a,$context") { a.sqrt(context) }
                verify("round $a,$context") { a.round(context) }
                verify("pow $a,2,$context") { a.pow(2, context) }
            }
        }
        assertEquals(139239, count)
        assertFalse(expected.hasNext(), "Unconsumed oracle outcomes")
    }
}

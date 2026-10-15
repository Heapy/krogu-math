// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

// Literal expectations in UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN,
// HALF_EVEN order. UNNECESSARY is an error for every listed rounded result.
// They come from squared decimal-grid boundaries, not the production identity.
internal data class DecimalSqrtMidpoint565Case(
    val coefficient: String,
    val scale: Int,
    val precision: Int,
    val results: List<Pair<String, Int>>,
)

internal fun decimalSqrtMidpoint565Cases(): List<DecimalSqrtMidpoint565Case> {
    fun row(c: String, s: Int, p: Int, low: String, high: String, t: Int,
        halfUp: String, halfDown: String, halfEven: String) = DecimalSqrtMidpoint565Case(c, s, p,
        listOf(high to t, low to t, high to t, low to t, halfUp to t, halfDown to t, halfEven to t))
    val cases = mutableListOf(
        row("5", 0, 1, "2", "3", 0, "2", "2", "2"),
        row("6", 0, 1, "2", "3", 0, "2", "2", "2"),
        row("8", 0, 1, "2", "3", 0, "3", "3", "3"),
        row("2", 0, 1, "1", "2", 0, "1", "1", "1"),
        row("2249", 3, 1, "1", "2", 0, "1", "1", "1"),
        row("2250", 3, 1, "1", "2", 0, "2", "1", "2"),
        row("2251", 3, 1, "1", "2", 0, "2", "2", "2"),
        row("2", 0, 2, "14", "15", 1, "14", "14", "14"),
        row("2", 0, 7, "1414213", "1414214", 6, "1414214", "1414214", "1414214"),
        row("156024", 4, 2, "39", "40", 1, "39", "39", "39"),
        row("156025", 4, 2, "39", "40", 1, "40", "39", "40"),
        row("156026", 4, 2, "39", "40", 1, "40", "40", "40"),
        row("4001", 3, 2, "20", "21", 1, "20", "20", "20"),
    )
    // Multiplication of the radicand by 10^6 (or 10^-6) moves the root
    // grid by 10^3 (or 10^-3), preserving the strict squared inequalities.
    for ((s, t) in listOf(-3 to -3, 3 to 0, 9 to 3)) {
        cases += row("6249", s, 1, "2", "3", t, "2", "2", "2")
        cases += row("6250", s, 1, "2", "3", t, "3", "2", "2")
        cases += row("6251", s, 1, "2", "3", t, "3", "3", "3")
    }
    cases += DecimalSqrtMidpoint565Case("9999", 2, 2,
        listOf("10" to 0, "99" to 1, "10" to 0, "99" to 1,
            "10" to 0, "10" to 0, "10" to 0))
    return cases
}

class DecimalSqrtMidpoint565Test {
    private class Truthful(c: BigInteger, s: Int) : BigDecimal(c, s) {
        override fun signum() = super.signum()
        override fun precision() = super.precision()
        override fun scale() = super.scale()
        override fun unscaledValue() = super.unscaledValue()
        override fun stripTrailingZeros() = super.stripTrailingZeros()
        override fun sqrt(mc: MathContext?) = super.sqrt(mc)
        fun inheritedRoot(mc: MathContext?) = super.sqrt(mc)
    }

    private fun assertResult(c: String, s: Int, actual: BigDecimal, label: String) {
        assertEquals(c, actual.unscaledValue().toString(), "$label coefficient")
        assertEquals(s, actual.scale(), "$label scale")
    }

    @Test fun tinyPositiveRemainderAllEightModes() {
        for (k in listOf(1, 2, 17, 18, 19, 64, 256, 1024)) {
            // Exactly 4 + 10^-k. For k>=1 it lies strictly between 4 and
            // 6.25, so 2 < sqrt(x) < 2.5 independently of sqrt's algorithm.
            val c = BigInteger("4" + "0".repeat(k - 1) + "1")
            for (mode in RoundingMode.entries) {
                val context = MathContext(1, mode)
                val value = BigDecimal(c, k)
                val truthful = Truthful(c, k)
                val label = "4+10^-$k / $mode"
                if (mode == RoundingMode.UNNECESSARY) {
                    assertFailsWith<ArithmeticException>(label) { value.sqrt(context) }
                    assertFailsWith<ArithmeticException>(label) { truthful.sqrt(context) }
                    assertFailsWith<ArithmeticException>(label) { truthful.inheritedRoot(context) }
                } else {
                    val expected = if (mode == RoundingMode.UP || mode == RoundingMode.CEILING) "3" else "2"
                    assertResult(expected, 0, value.sqrt(context), label)
                    assertResult(expected, 0, truthful.sqrt(context), "$label truthful")
                    assertResult(expected, 0, truthful.inheritedRoot(context), "$label caller super")
                }
            }
        }
    }

    @Test fun literalNeighborsTiesPrecisionScaleAndWideCohorts() {
        val modes = listOf(RoundingMode.UP, RoundingMode.DOWN, RoundingMode.CEILING,
            RoundingMode.FLOOR, RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN)
        for (case in decimalSqrtMidpoint565Cases()) for (padding in listOf(0, 80)) {
            val c = BigInteger(case.coefficient + "0".repeat(padding))
            val value = BigDecimal(c, case.scale + padding)
            val truthful = Truthful(c, case.scale + padding)
            for ((index, mode) in modes.withIndex()) {
                val context = MathContext(case.precision, mode)
                val (expected, scale) = case.results[index]
                val label = "${case.coefficient}@${case.scale} / p${case.precision} / $mode / padding=$padding"
                assertResult(expected, scale, value.sqrt(context), label)
                assertResult(expected, scale, truthful.sqrt(context), "$label truthful")
                assertResult(expected, scale, truthful.inheritedRoot(context), "$label caller super")
            }
            assertFailsWith<ArithmeticException> { value.sqrt(MathContext(case.precision, RoundingMode.UNNECESSARY)) }
        }
    }

    @Test fun exactRootsAndPreferredRepresentationRemain() {
        // Exact results at the rounding grid still take finiteExactSquareRoot;
        // these preserve API behavior, not inexact-branch coverage.
        for (mode in RoundingMode.entries) {
            assertResult("20", 1, BigDecimal("4.00").sqrt(MathContext(2, mode)), "preferred $mode")
            assertResult("200", 2, BigDecimal("4.0000").sqrt(MathContext(3, mode)), "preferred $mode")
            assertResult("2", -2, BigDecimal(BigInteger("4"), -4).sqrt(MathContext(1, mode)), "negative scale $mode")
        }
        assertResult("200025", 5, BigDecimal("4.001").sqrt(MathContext(7, RoundingMode.HALF_EVEN)), "inexact trailing zero")
    }

    @Test fun explicitOverrideSuperAndDirectExceptionIdentity() {
        val marker = BigDecimal("7.00")
        val contexts = listOf<MathContext?>(null) + listOf(0, 1, 7)
            .flatMap { p -> RoundingMode.entries.map { MathContext(p, it) } }
        for (context in contexts) {
            var entries = 0
            val replacement: BigDecimal = object : BigDecimal("4.001") {
                override fun sqrt(mc: MathContext?): BigDecimal { entries++; assertSame(context, mc); return marker }
            }
            assertSame(marker, replacement.sqrt(context))
            assertEquals(1, entries)
            val failure = IllegalStateException("caller-owned midpoint root")
            val throwing: BigDecimal = object : BigDecimal("4.001") {
                override fun sqrt(mc: MathContext?): BigDecimal { assertSame(context, mc); throw failure }
            }
            assertSame(failure, assertFailsWith<IllegalStateException> { throwing.sqrt(context) })
        }
        val failure = IllegalStateException("direct sqrt prefix")
        val throwingSign: BigDecimal = object : BigDecimal("4.001") {
            override fun signum(): Int = throw failure
        }
        assertSame(failure, assertFailsWith<IllegalStateException> { throwingSign.sqrt(MathContext(1)) })
        val throwingPrecision: BigDecimal = object : BigDecimal("4.001") {
            override fun precision(): Int = throw failure
        }
        assertSame(failure, assertFailsWith<IllegalStateException> { throwingPrecision.sqrt(MathContext(1)) })
        assertFailsWith<ArithmeticException> { Truthful(BigInteger("4001"), 3).inheritedRoot(MathContext(0)) }
        assertFailsWith<NullPointerException> { Truthful(BigInteger("4001"), 3).inheritedRoot(null) }
    }
}

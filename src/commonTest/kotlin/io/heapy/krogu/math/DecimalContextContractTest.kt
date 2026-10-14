// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Every override delegates its full public contract; no callback is poisoned or replaced. */
private class TruthfulContextDecimal(text: String) : BigDecimal(text) {
    override fun signum(): Int = super.signum()
    override fun precision(): Int = super.precision()
    override fun scale(): Int = super.scale()
    override fun unscaledValue(): BigInteger = super.unscaledValue()
    override fun abs(): BigDecimal = super.abs()
    override fun abs(mc: MathContext?): BigDecimal = super.abs(mc)
    override fun negate(): BigDecimal = super.negate()
    override fun negate(mc: MathContext?): BigDecimal = super.negate(mc)
    override fun divide(divisor: BigDecimal?): BigDecimal = super.divide(divisor)
    override fun divide(divisor: BigDecimal?, mc: MathContext?): BigDecimal = super.divide(divisor, mc)
    fun divideViaSuper(divisor: BigDecimal?, mc: MathContext?): BigDecimal = super.divide(divisor, mc)
}

private fun contextContractOutcome(action: () -> BigDecimal): String = try {
    val result = action()
    "${result.unscaledValue()}@${result.scale()}"
} catch (_: ArithmeticException) { "!ArithmeticException" }
  catch (_: NullPointerException) { "!NullPointerException" }

class DecimalContextContractTest {
    // Stable index partitions cover every row and every meaningful operand-class mask.
    @Test fun ordinaryAndTruthfulControlsPartition0() = checkControls(0)
    @Test fun ordinaryAndTruthfulControlsPartition1() = checkControls(1)
    @Test fun ordinaryAndTruthfulControlsPartition2() = checkControls(2)
    @Test fun ordinaryAndTruthfulControlsPartition3() = checkControls(3)

    private fun checkControls(partition: Int) {
        val cases = jdk25DecimalContextContractCases()
        assertTrue(cases.isNotEmpty(), "Contract projection must not be empty")
        val failures = mutableListOf<String>()
        var failureCount = 0
        var checked = 0
        for ((index, row) in cases.withIndex()) {
            if (index % 4 != partition) continue
            val masks = if (row[2] == "ordinary") 0..3 else 0..1
            for (mask in masks) {
                val source: BigDecimal = if (mask and 1 == 0) BigDecimal(row[0]) else TruthfulContextDecimal(row[0])
                val divisor: BigDecimal? = when (row[2]) {
                    "self" -> source
                    "null" -> null
                    "ordinary" -> if (mask and 2 == 0) BigDecimal(row[1]) else TruthfulContextDecimal(row[1])
                    else -> error("Unknown control topology ${row[2]}")
                }
                val p = row[3].toInt()
                val context = if (p < 0) null else MathContext(p, RoundingMode.valueOf(row[4]))
                val actual = contextContractOutcome { source.divide(divisor, context) }
                checked++
                if (actual != row[5]) {
                    failureCount++
                    if (failures.size < 12) failures += "${row.take(5)} mask=$mask expected=${row[5]} actual=$actual"
                }
            }
        }
        assertTrue(checked > 0, "Each deterministic partition must execute controls")
        assertEquals(emptyList<String>(), failures, "$failureCount mismatches in $checked controls")
    }

    @Test fun rationalMidpointsCoverBothSignsAndRetainedDigitParity() {
        // 10.50 / 2.0 = 5.25 and 11.50 / 2.0 = 5.75. At precision 2,
        // scale 1 requires rounding 1050/20 = 52 + 10/20 or 1150/20 = 57 + 10/20.
        for (coefficient in listOf(1050, 1150)) for (leftSign in listOf(-1, 1)) {
            for (rightSign in listOf(-1, 1)) for (mode in RoundingMode.entries) {
                val sign = leftSign * rightSign
                val whole = coefficient / 20
                val expected = if (mode == RoundingMode.UNNECESSARY) "!ArithmeticException" else {
                    val increment = when (mode) {
                        RoundingMode.UP, RoundingMode.HALF_UP -> true
                        RoundingMode.DOWN, RoundingMode.HALF_DOWN -> false
                        RoundingMode.CEILING -> sign > 0
                        RoundingMode.FLOOR -> sign < 0
                        RoundingMode.HALF_EVEN -> whole % 2 != 0
                        RoundingMode.UNNECESSARY -> error("Handled above")
                    }
                    "${sign * (whole + if (increment) 1 else 0)}@1"
                }
                val aText = (if (leftSign < 0) "-" else "") + if (coefficient == 1050) "10.50" else "11.50"
                val bText = if (rightSign < 0) "-2.0" else "2.0"
                for (truthful in listOf(false, true)) {
                    val a: BigDecimal = if (truthful) TruthfulContextDecimal(aText) else BigDecimal(aText)
                    val b: BigDecimal = if (truthful) TruthfulContextDecimal(bText) else BigDecimal(bText)
                    assertEquals(expected, contextContractOutcome { a.divide(b, MathContext(2, mode)) }, "$aText / $bText $mode")
                }
            }
        }
    }

    @Test fun independentPreferredScaleErrorsAndCoefficientBoundaries() {
        for (truthful in listOf(false, true)) {
            fun decimal(text: String): BigDecimal = if (truthful) TruthfulContextDecimal(text) else BigDecimal(text)
            val halfEven = MathContext(2, RoundingMode.HALF_EVEN)
            // Zero's preferred scale is 2 - 1 = 1; 5.25 needs scale 2 even though preferred scale is 1.
            assertEquals("0@1", contextContractOutcome { decimal("0.00").divide(decimal("2.0"), halfEven) })
            assertEquals("525@2", contextContractOutcome { decimal("10.50").divide(decimal("2.0"), MathContext(0)) })
            assertEquals("525@2", contextContractOutcome { decimal("10.50").divide(decimal("2.0"), MathContext(7)) })
            // A reduced denominator of 3 cannot terminate; finite UNNECESSARY forbids its discarded digits.
            assertFailsWith<ArithmeticException> { decimal("1").divide(decimal("3"), MathContext(0)) }
            assertFailsWith<ArithmeticException> { decimal("1").divide(decimal("3"), MathContext(2, RoundingMode.UNNECESSARY)) }
            assertFailsWith<ArithmeticException> { decimal("10.50").divide(decimal("0.0"), halfEven) }
            val zero = decimal("0.00")
            val nonzero = decimal("-10.50")
            assertFailsWith<ArithmeticException> { zero.divide(zero, halfEven) }
            assertEquals("1@0", contextContractOutcome { nonzero.divide(nonzero, halfEven) })
            assertFailsWith<NullPointerException> { nonzero.divide(null, halfEven) }
            assertFailsWith<NullPointerException> { nonzero.divide(decimal("2.0"), null as MathContext?) }
            assertFailsWith<NullPointerException> { nonzero.divide(null, null as MathContext?) }
            // Dropping the last of nineteen digits rounds ...5808 upward to ...581 * 10.
            assertEquals("922337203685477581@-1", contextContractOutcome {
                decimal("9223372036854775808").divide(decimal("1"), MathContext(18, RoundingMode.HALF_EVEN))
            })
            assertEquals("9223372036854775808@0", contextContractOutcome {
                decimal("9223372036854775808").divide(decimal("1"), MathContext(19, RoundingMode.HALF_EVEN))
            })
            // Eighteen nines round to 10^18 at precision 17, represented by 17 digits with scale -2.
            assertEquals("10000000000000000@-2", contextContractOutcome {
                decimal("999999999999999999").divide(decimal("1"), MathContext(17, RoundingMode.HALF_EVEN))
            })
            for (p in listOf(18, 19)) assertEquals("999999999999999999@0", contextContractOutcome {
                decimal("999999999999999999").divide(decimal("1"), MathContext(p, RoundingMode.HALF_EVEN))
            })
        }
    }

    @Test fun explicitBaseTypedCallerEntersOverridesAndPropagatesTheirException() {
        val injected = IllegalStateException("caller-owned sentinel")
        val replacement = BigDecimal("7.00")
        val divisor = BigDecimal("2.0")
        val context = MathContext(2, RoundingMode.HALF_EVEN)
        var exactArgument: BigDecimal? = null
        var contextArgument: MathContext? = null
        val replacing: BigDecimal = object : BigDecimal("10.50") {
            override fun divide(divisor: BigDecimal?): BigDecimal { exactArgument = divisor; return replacement }
            override fun divide(divisor: BigDecimal?, mc: MathContext?): BigDecimal {
                exactArgument = divisor; contextArgument = mc; return replacement
            }
        }
        assertSame(replacement, replacing.divide(divisor))
        assertSame(divisor, exactArgument)
        assertSame(replacement, replacing.divide(divisor, context))
        assertSame(divisor, exactArgument)
        assertSame(context, contextArgument)
        val throwing: BigDecimal = object : BigDecimal("10.50") {
            override fun divide(divisor: BigDecimal?): BigDecimal = throw injected
            override fun divide(divisor: BigDecimal?, mc: MathContext?): BigDecimal = throw injected
        }
        assertSame(injected, assertFailsWith<IllegalStateException> { throwing.divide(divisor) })
        assertSame(injected, assertFailsWith<IllegalStateException> { throwing.divide(divisor, context) })
    }

    @Test fun callerOwnedSuperInvocationProducesTruthfulArithmetic() {
        val source = TruthfulContextDecimal("10.50")
        assertEquals("52@1", contextContractOutcome {
            source.divideViaSuper(TruthfulContextDecimal("2.0"), MathContext(2, RoundingMode.HALF_EVEN))
        })
        assertEquals("525@2", contextContractOutcome {
            source.divideViaSuper(TruthfulContextDecimal("2.0"), MathContext(0))
        })
        // Internal dispatch remains free: all participating overrides are truthful.
        assertEquals("1@0", contextContractOutcome { source.divideViaSuper(source, MathContext(2)) })
        assertFailsWith<NullPointerException> { source.divideViaSuper(null, MathContext(2)) }
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Every override delegates its full public contract; no callback is poisoned or replaced. */
private class TruthfulContextDecimal : BigDecimal {
    constructor(text: String) : super(text)
    constructor(coefficient: BigInteger, scale: Int) : super(coefficient, scale)
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

/** Canonical text derived only from fixture digits and scale, following the public toString contract. */
private fun expectedContextCanonical(pair: String): String {
    val coefficient = pair.substringBefore('@')
    val digits = coefficient.removePrefix("-")
    val sign = if (coefficient.startsWith('-')) "-" else ""
    val scale = pair.substringAfter('@').toInt()
    // Widen before negating/subtracting: Int.MIN_VALUE's exponent exceeds Int.MAX_VALUE.
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
                var result: BigDecimal? = null
                val actual = contextContractOutcome { source.divide(divisor, context).also { result = it } }
                checked++
                if (actual != row[5]) {
                    failureCount++
                    if (failures.size < 12) failures += "${row.take(5)} mask=$mask expected=${row[5]} actual=$actual"
                }
                val value = result
                if (value != null && !row[5].startsWith('!')) {
                    val expectedText = expectedContextCanonical(row[5])
                    val actualText = value.toString()
                    if (actualText != expectedText) {
                        failureCount++
                        if (failures.size < 12) failures += "${row.take(5)} mask=$mask expected text=$expectedText actual text=$actualText"
                    }
                }
            }
        }
        assertTrue(checked > 0, "Each deterministic partition must execute controls")
        assertEquals(emptyList<String>(), failures, "$failureCount mismatches in $checked controls")
    }

    @Test fun canonicalTextLiteralThresholdScaleSignAndLongExponentWitnesses() {
        val literals = listOf(
            Triple("1", 6, "0.000001"),
            Triple("1", 7, "1E-7"),
            Triple("123", 8, "0.00000123"),
            Triple("123", 9, "1.23E-7"),
            Triple("12300", 4, "1.2300"),
            Triple("12300", 0, "12300"),
            Triple("12300", -1, "1.2300E+5"),
            Triple("-12300", 4, "-1.2300"),
            Triple("-123", 9, "-1.23E-7"),
            Triple("0", 0, "0"),
            Triple("0", 2, "0.00"),
            Triple("0", 6, "0.000000"),
            Triple("0", 7, "0E-7"),
            Triple("0", -2, "0E+2"),
            Triple("1", Int.MIN_VALUE, "1E+2147483648"),
            Triple("-12", Int.MIN_VALUE, "-1.2E+2147483649"),
            Triple("123", Int.MAX_VALUE, "1.23E-2147483645"),
            Triple("0", Int.MIN_VALUE, "0E+2147483648"),
            Triple("0", Int.MAX_VALUE, "0E-2147483647"),
        )
        for ((coefficient, scale, literal) in literals) {
            assertEquals(literal, expectedContextCanonical("$coefficient@$scale"), "Independent formatter witness")
            val integer = BigInteger(coefficient)
            assertEquals(literal, BigDecimal(integer, scale).toString(), "Ordinary formatter witness")
            assertEquals(literal, TruthfulContextDecimal(integer, scale).toString(), "Truthful formatter witness")
        }
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
        val distinct = BigDecimal("2.0")
        val finite = MathContext(2, RoundingMode.HALF_EVEN)
        val unlimited = MathContext(0)
        class CallerDecimal(val throwing: Boolean) : BigDecimal("10.50") {
            var entry: String? = null
            var entries = 0
            var divisorArgument: BigDecimal? = null
            var contextArgument: MathContext? = null
            override fun divide(divisor: BigDecimal?): BigDecimal {
                entry = "exact"; entries++; divisorArgument = divisor
                if (throwing) throw injected
                return replacement
            }
            override fun divide(divisor: BigDecimal?, mc: MathContext?): BigDecimal {
                entry = "context"; entries++; divisorArgument = divisor; contextArgument = mc
                if (throwing) throw injected
                return replacement
            }
        }
        for (throwing in listOf(false, true)) {
            val probe = CallerDecimal(throwing)
            val source: BigDecimal = probe
            // Exact division has no context parameter; context division covers the full 3 x 3 matrix.
            for (divisor in listOf(distinct, source, null)) {
                fun checkCall(contextual: Boolean, context: MathContext?) {
                    probe.entry = null
                    probe.divisorArgument = distinct
                    probe.contextArgument = finite
                    val before = probe.entries
                    val action = { if (contextual) source.divide(divisor, context) else source.divide(divisor) }
                    if (throwing) assertSame(injected, assertFailsWith<IllegalStateException>(block = action))
                    else assertSame(replacement, action())
                    assertEquals(if (contextual) "context" else "exact", probe.entry)
                    assertEquals(before + 1, probe.entries)
                    assertSame(divisor, probe.divisorArgument)
                    if (contextual) assertSame(context, probe.contextArgument)
                }
                checkCall(false, null)
                for (context in listOf(finite, unlimited, null)) checkCall(true, context)
            }
        }
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

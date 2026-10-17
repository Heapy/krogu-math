// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertFailsWith

class DecimalAccumulator535Test {
    @Test
    fun roundingCreatedZerosRescueHighWorkingScale() {
        for (negative in listOf(false, true)) for (mode in RoundingMode.entries) {
            val sign = if (negative) "-" else ""
            val a = BigDecimal(BigInteger(sign + "10001"), Int.MAX_VALUE)
            val factor = BigDecimal(BigInteger.ONE, 2)
            val accumulator = DecimalAccumulator(MathContext(4, mode)).addProduct(a, factor)
            val increments = when (mode) {
                RoundingMode.UP -> true
                RoundingMode.CEILING -> !negative
                RoundingMode.FLOOR -> negative
                else -> false
            }
            if (mode == RoundingMode.UNNECESSARY || increments)
                assertFailsWith<ArithmeticException> { accumulator.snapshot() }
            else assertEquals(0, accumulator.snapshot().compareTo(BigDecimal(BigInteger(sign + "100"), Int.MAX_VALUE)))
        }
        // Carry creates exact zeros too: 99999 -> 10000 -> an equivalent public cohort.
        for (sign in listOf("", "-")) {
            val value = BigDecimal(BigInteger(sign + "99999"), Int.MAX_VALUE)
            val result = DecimalAccumulator(MathContext(4, RoundingMode.HALF_EVEN))
                .addProduct(value, BigDecimal(BigInteger.ONE, 2)).snapshot()
            assertEquals(0, result.compareTo(BigDecimal(BigInteger(sign + "1000"), Int.MAX_VALUE)))
        }
    }

    @Test
    fun expandedFinalArithmeticCancelsBeyondPublicIntermediateDomains() {
        val low = BigDecimal(BigInteger.ONE, Int.MIN_VALUE)
        val factor = BigDecimal(BigInteger.ONE, -1)
        val compensation = BigDecimal(BigInteger("-10"), Int.MIN_VALUE)
        assertEquals(BigDecimal.ZERO, low.fma(factor, compensation))
        assertFailsWith<ArithmeticException> {
            low.fma(factor, compensation, rounding = DecimalRounding.EACH_STEP)
        }
        val high = BigDecimal(BigInteger.TEN, Int.MAX_VALUE)
        assertEquals(BigDecimal(BigInteger.ONE, Int.MAX_VALUE),
            high.fma(BigDecimal(BigInteger.ONE, 1), BigDecimal.ZERO))

        // The exact aligned coefficient spans 4,294,967,296 decimal digits,
        // far beyond the public BigInteger domain, but only two cells are live.
        val tiny = BigDecimal(BigInteger.ONE, Int.MAX_VALUE)
        val accumulator = DecimalAccumulator().add(low).add(tiny)
        assertFailsWith<ArithmeticException> { accumulator.snapshot() }
        accumulator.add(BigDecimal(BigInteger.ONE.negate(), Int.MIN_VALUE))
        assertEquals(tiny, accumulator.snapshot())
        assertEquals(3L, accumulator.size)
        for (sign in listOf(1L, -1L)) {
            val huge = BigDecimal(BigInteger.valueOf(sign), Int.MIN_VALUE)
            // A preferred aligned cohort can itself exceed the public domain
            // even though a tiny equivalent coefficient is supported.
            assertEquals(huge, DecimalAccumulator().add(huge)
                .add(BigDecimal(BigInteger.ZERO, Int.MAX_VALUE)).snapshot())
        }

        // Borrow across a huge empty span is represented as a run; rounding
        // through all nines must not allocate that span or lose its sticky bit.
        val borrowed = DecimalAccumulator(MathContext(3, RoundingMode.HALF_EVEN))
            .add(low).add(BigDecimal(BigInteger.ONE.negate(), Int.MAX_VALUE))
        assertEquals(0, borrowed.snapshot().compareTo(low))
        borrowed.add(BigDecimal(BigInteger.ONE.negate(), Int.MIN_VALUE))
        assertEquals(BigDecimal(BigInteger.ONE.negate(), Int.MAX_VALUE), borrowed.snapshot())
    }

    @Test
    fun fmaRoundingPolicyHasIndependentLiteralWitnesses() {
        // 1.234^2 - 1.52 = 0.002756 exactly. Product rounded first gives 1.52.
        for (negative in listOf(false, true)) for (mode in RoundingMode.entries) {
            val sign = if (negative) "-" else ""
            val a = BigDecimal(sign + "1.234")
            val b = BigDecimal("1.234")
            val c = BigDecimal(if (negative) "1.52" else "-1.52")
            val context = MathContext(3, mode)
            if (mode == RoundingMode.UNNECESSARY) {
                assertFailsWith<ArithmeticException> { a.fma(b, c, context) }
                assertFailsWith<ArithmeticException> { a.fma(b, c, context, DecimalRounding.EACH_STEP) }
            } else {
                val magnitude = when (mode) {
                    RoundingMode.DOWN -> "0.00275"
                    RoundingMode.CEILING -> if (negative) "0.00275" else "0.00276"
                    RoundingMode.FLOOR -> if (negative) "0.00276" else "0.00275"
                    else -> "0.00276"
                }
                assertEquals(BigDecimal(sign + magnitude), a.fma(b, c, context))
                // Directed rounding may make the first product 1.53 instead.
                assertEquals(a.multiply(b, context).add(c, context),
                    a.fma(b, c, context, DecimalRounding.EACH_STEP))
            }
        }
        val mc = MathContext(3, RoundingMode.HALF_EVEN)
        assertEquals(BigDecimal("0.00"), BigDecimal("1.234").fma(BigDecimal("1.234"), BigDecimal("-1.52"), mc, DecimalRounding.EACH_STEP))
        val zero = BigDecimal("0.00")
        val factor = BigDecimal("2.0")
        val addend = BigDecimal("1.0")
        assertEquals(zero.multiply(factor, mc).add(addend, mc), zero.fma(factor, addend, mc, DecimalRounding.EACH_STEP))
    }

    @Test
    fun cancellationScaleAndEmptyInputAreExplicit() {
        val values = listOf(BigDecimal("1E+50"), BigDecimal.ONE, BigDecimal("-1E+50"))
        val context = MathContext(3, RoundingMode.HALF_EVEN)
        assertEquals(BigDecimal.ONE, values.sumDecimals(context))
        assertEquals(0, values.sumDecimals(context, DecimalRounding.EACH_STEP).signum())
        assertEquals(BigDecimal.ZERO, emptyList<BigDecimal>().sumDecimals(context))
        assertEquals(BigDecimal.ZERO, emptyList<BigDecimal>().dotProduct(emptyList(), context))
        assertEquals(BigDecimal("1E+4"), listOf(BigDecimal("1E+4")).sumDecimals())
        assertEquals(BigDecimal("3.00"), listOf(BigDecimal("1.0"), BigDecimal("2.00")).sumDecimals())
        assertEquals(BigDecimal("23"), listOf(BigDecimal("2"), BigDecimal("3"))
            .dotProduct(listOf(BigDecimal("4"), BigDecimal("5"))))
        assertFailsWith<IllegalArgumentException> { listOf(BigDecimal.ONE).dotProduct(emptyList()) }
    }

    @Test
    fun snapshotsAreDetachedAndFailedOperationsAreAtomic() {
        val accumulator = DecimalAccumulator()
        accumulator.add(BigDecimal(Long.MAX_VALUE)).add(BigDecimal.ONE)
        val wide = accumulator.snapshot()
        assertEquals(BigDecimal("9223372036854775808"), wide)
        accumulator.add(BigDecimal("-9223372036854775808"))
        assertEquals(BigDecimal.ZERO, accumulator.snapshot())
        assertEquals(BigDecimal("9223372036854775808"), wide)
        accumulator.clear()
        assertEquals(0L, accumulator.size)
        accumulator.add(BigDecimal(Long.MIN_VALUE)).add(BigDecimal.ONE)
        val negative = accumulator.snapshot()
        assertEquals(BigDecimal("-9223372036854775807"), negative)
        accumulator.clear()
        assertEquals(BigDecimal("-9223372036854775807"), negative)

        val terms = DecimalAccumulator(maxTerms = 1).add(BigDecimal("2.00"))
        assertFailsWith<ArithmeticException> { terms.add(BigDecimal.ONE) }
        assertEquals(1L, terms.size)
        assertEquals(BigDecimal("2.00"), terms.snapshot())
        val digits = DecimalAccumulator(maxWorkingDigits = 1).add(BigDecimal("9"))
        assertFailsWith<ArithmeticException> { digits.add(BigDecimal.ONE) }
        assertEquals(BigDecimal("9"), digits.snapshot())
        assertFailsWith<ArithmeticException> { digits.addProduct(BigDecimal("99"), BigDecimal("99")) }
        assertEquals(1L, digits.size)
        val rounding = DecimalAccumulator(MathContext(2, RoundingMode.UNNECESSARY), DecimalRounding.EACH_STEP)
            .add(BigDecimal("1.0"))
        assertFailsWith<ArithmeticException> { rounding.add(BigDecimal("0.01")) }
        assertEquals(BigDecimal("1.0"), rounding.snapshot())
        val scale = DecimalAccumulator(maxWorkingDigits = 1000).add(BigDecimal(BigInteger.ONE, Int.MIN_VALUE))
        assertFailsWith<ArithmeticException> { scale.add(BigDecimal(BigInteger.ONE, Int.MAX_VALUE)) }
        assertEquals(BigDecimal(BigInteger.ONE, Int.MIN_VALUE), scale.snapshot())
        // The explicit work budget can reject huge alignment; the default
        // expanded path above does not introduce that limit.
        assertFailsWith<ArithmeticException> { rounding.addProduct(BigDecimal("1.234"), BigDecimal.ONE) }
        assertEquals(BigDecimal("1.0"), rounding.snapshot())
        for (endpoint in listOf(Int.MIN_VALUE, Int.MAX_VALUE)) for (policy in DecimalRounding.entries) {
            val extreme = DecimalAccumulator(rounding = policy)
                .add(BigDecimal(BigInteger.ONE, endpoint))
            val before = extreme.snapshot()
            extreme.add(BigDecimal(BigInteger.ONE.negate(), endpoint))
            assertEquals(0, extreme.snapshot().signum())
            assertEquals(BigDecimal(BigInteger.ONE, endpoint), before)
        }
    }

    @Test
    fun operandCallbacksCannotReenterMutationOrBypassBudgets() {
        for (policy in DecimalRounding.entries) for (action in 0..2) for (product in listOf(false, true)) {
            val acc = DecimalAccumulator(rounding = policy, maxTerms = 2).add(BigDecimal("9"))
            val input = object : BigDecimal("2") {
                override fun unscaledValue(): BigInteger {
                    when (action) {
                        0 -> acc.add(BigDecimal.ONE)
                        1 -> acc.addProduct(BigDecimal.ONE, BigDecimal.ONE)
                        else -> acc.clear()
                    }
                    return super.unscaledValue()
                }
            }
            assertFailsWith<IllegalStateException> {
                if (product) acc.addProduct(input, BigDecimal.ONE) else acc.add(input)
            }
            assertEquals(1L, acc.size)
            assertEquals(BigDecimal("9"), acc.snapshot())
            acc.add(BigDecimal.ONE) // The guard resets even when an operand getter throws.
            assertEquals(2L, acc.size)
            assertEquals(BigDecimal("10"), acc.snapshot())
            acc.clear()
            assertEquals(BigDecimal.ZERO, acc.snapshot())
        }
        val limited = DecimalAccumulator(maxTerms = 1)
        val reentrant = object : BigDecimal("2") {
            override fun unscaledValue(): BigInteger {
                limited.add(BigDecimal.ONE)
                return super.unscaledValue()
            }
        }
        assertFailsWith<IllegalStateException> { limited.add(reentrant) }
        assertEquals(0L, limited.size)
        val marker = ArithmeticException("getter failure")
        val broken = object : BigDecimal("2") {
            override fun unscaledValue(): BigInteger {
                assertEquals(BigDecimal.ZERO, limited.snapshot()) // Read-only observation is allowed.
                throw marker
            }
        }
        assertSame(marker, assertFailsWith<ArithmeticException> { limited.add(broken) })
        val observer = object : BigDecimal("2.00") {
            override fun unscaledValue(): BigInteger {
                assertEquals(BigDecimal.ZERO, limited.snapshot())
                return super.unscaledValue()
            }
        }
        limited.add(observer)
        assertEquals(1L, limited.size)
        assertEquals(BigDecimal("2.00"), limited.snapshot())
    }

    @Test
    fun subclassMetadataIsSnapshottedAtTheCallBoundary() {
        val input = object : BigDecimal("2.00") {
            var observedScale = 2
            override fun scale(): Int = observedScale
        }
        val accumulator = DecimalAccumulator().add(input)
        input.observedScale = -10
        assertEquals(BigDecimal("2.00"), accumulator.snapshot())
        assertFailsWith<IllegalArgumentException> { DecimalAccumulator(maxTerms = 0) }
        assertFailsWith<IllegalArgumentException> { DecimalAccumulator(maxWorkingDigits = 0) }
        val broken = sequence<BigDecimal> { yield(BigDecimal.ONE); throw IllegalStateException("input failure") }.asIterable()
        assertFailsWith<IllegalStateException> { broken.sumDecimals() }
        assertFailsWith<IllegalStateException> { broken.dotProduct(listOf(BigDecimal.ONE, BigDecimal.ONE)) }
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Explicitly choose where finite-precision rounding happens. */
enum class DecimalRounding { EACH_STEP, FINAL_RESULT }

/**
 * A reusable, thread-confined decimal accumulator with immutable snapshots.
 *
 * [DecimalRounding.FINAL_RESULT] retains an exact running sum and rounds only
 * when [snapshot] is requested. [DecimalRounding.EACH_STEP] rounds the first
 * term, each product, and each subsequent addition using [context]. The first
 * term establishes the initial scale; an empty accumulator snapshots as ZERO.
 *
 * Equal-scale compact exact additions reuse one Long field. Expanded private
 * digits retain final-only intermediates; per-step work delegates ordinary
 * decimal arithmetic. [maxWorkingDigits] bounds a conservative
 * aligned-coefficient estimate before arithmetic; [maxTerms] bounds accepted
 * terms. Limits are explicit new-API resource policies, not BigDecimal limits.
 * Null limits (the defaults) impose no extra budget beyond supported arithmetic.
 * A failed add leaves the previous state unchanged. Operand getters may observe
 * [snapshot], but mutating reentry into add/addProduct/clear throws IllegalStateException
 * before changing state. This class is not thread-safe.
 */
class DecimalAccumulator(
    val context: MathContext = MathContext.UNLIMITED,
    val rounding: DecimalRounding = DecimalRounding.FINAL_RESULT,
    val maxWorkingDigits: Long? = null,
    val maxTerms: Long? = null,
) {
    private var changing = false
    private var compact = 0L
    private var scale = 0
    private var wide: BigDecimal? = null
    private var finalWork: DecimalSignedWork535? = null
    var size: Long = 0L
        private set

    init { require((maxWorkingDigits == null || maxWorkingDigits > 0) && (maxTerms == null || maxTerms > 0)) }

    fun add(value: BigDecimal): DecimalAccumulator = mutate { addValue(value) }

    private fun addValue(value: BigDecimal): DecimalAccumulator {
        checkTermBudget()
        val input = observed(value)
        val inputDigits = if (maxWorkingDigits == null) 0 else checkedDigits(input)
        if (size == 0L) {
            val result = if (rounding == DecimalRounding.EACH_STEP) input.round(context) else input
            store(result)
        } else {
            val direct = input.accumulatorCompactEligible529()
            val fallback = if (direct) null else input.unscaledValue().decimalCompactValue()
            val incoming = if (direct) input.accumulatorCompactValue529() else fallback ?: 0L
            if (finalWork == null && wide == null && (direct || fallback != null) && scale == input.scale() &&
                (rounding == DecimalRounding.FINAL_RESULT || context.precision == 0) &&
                !additionOverflows(compact, incoming)) {
                val result = compact + incoming
                if (maxWorkingDigits != null) checkWork(longDigits(result).toLong())
                compact = result
            } else {
                if (rounding == DecimalRounding.FINAL_RESULT) {
                    val work = expandedCopy()
                    if (maxWorkingDigits != null)
                        checkWork(work.estimatedAlignedDigits(input.scale().toLong(), inputDigits.toLong()))
                    work.add(input)
                    finalWork = work
                    wide = null
                    size++
                    return this
                }
                val current = stored()
                if (maxWorkingDigits != null) {
                    val targetScale = maxOf(current.scale(), input.scale()).toLong()
                    val estimate = maxOf(checkedDigits(current).toLong() + targetScale - current.scale(),
                        inputDigits.toLong() + targetScale - input.scale()) + 1
                    checkWork(estimate)
                }
                val result = if (rounding == DecimalRounding.EACH_STEP) current.add(input, context)
                    else current.add(input)
                store(result)
            }
        }
        size++
        return this
    }

    /** Add a product, with the same explicit per-step or final-only policy. */
    fun addProduct(left: BigDecimal, right: BigDecimal): DecimalAccumulator =
        mutate { addProductValue(left, right) }

    private fun addProductValue(left: BigDecimal, right: BigDecimal): DecimalAccumulator {
        checkTermBudget()
        val a = observed(left)
        val b = observed(right)
        if (maxWorkingDigits != null) checkWork(checkedDigits(a).toLong() + checkedDigits(b))
        if (rounding == DecimalRounding.FINAL_RESULT) {
            val productScale = a.scale().toLong() + b.scale()
            val work = if (size == 0L) DecimalSignedWork535(productScale) else expandedCopy()
            if (maxWorkingDigits != null && size != 0L)
                checkWork(work.estimatedAlignedDigits(productScale, checkedDigits(a).toLong() + checkedDigits(b)))
            work.addProduct(a, b)
            finalWork = work
            wide = null
            size++
            return this
        }
        val product = if (rounding == DecimalRounding.EACH_STEP) a.multiply(b, context) else a.multiply(b)
        if (size == 0L) {
            // The product already used the selected policy. Rounding it again
            // would change a zero product's cohort before the first addition.
            store(product)
            size = 1L
            return this
        }
        return addValue(product)
    }

    /** The returned value owns no mutable accumulator storage. */
    fun snapshot(): BigDecimal {
        if (rounding == DecimalRounding.FINAL_RESULT) {
            if (size == 0L) return BigDecimal.ZERO
            return (finalWork ?: expandedCopy()).result(context)
        }
        val value = if (size == 0L) BigDecimal.ZERO else stored()
        return value
    }

    /** Reuse the accumulator without affecting previously returned snapshots. */
    fun clear() {
        check(!changing) { "Reentrant decimal accumulation" }
        compact = 0L
        scale = 0
        wide = null
        finalWork = null
        size = 0L
    }

    private inline fun mutate(operation: () -> DecimalAccumulator): DecimalAccumulator {
        check(!changing) { "Reentrant decimal accumulation" }
        changing = true
        try {
            return operation()
        } finally {
            changing = false
        }
    }

    private fun checkTermBudget() {
        if (size == Long.MAX_VALUE || maxTerms != null && size >= maxTerms)
            throw ArithmeticException("Decimal term limit exceeded")
    }

    private fun checkWork(digits: Long) {
        if (maxWorkingDigits != null && digits > maxWorkingDigits)
            throw ArithmeticException("Decimal work limit exceeded")
    }

    private fun checkedDigits(value: BigDecimal): Int {
        if (value.accumulatorCompactEligible529())
            return longDigits(value.accumulatorCompactValue529()).also { checkWork(it.toLong()) }
        val coefficient = value.unscaledValue()
        val small = coefficient.decimalCompactValue()
        if (small != null) return longDigits(small).also { checkWork(it.toLong()) }
        // Reject huge already-existing inputs before an exact decimal digit count.
        checkWork(coefficient.decimalDigitLowerBound().toLong())
        return value.precision().also { checkWork(it.toLong()) }
    }

    private fun store(value: BigDecimal) {
        finalWork = null
        val direct = value.accumulatorCompactEligible529()
        val fallback = if (direct) null else value.unscaledValue().decimalCompactValue()
        if (!direct && fallback == null) wide = value
        else {
            compact = if (direct) value.accumulatorCompactValue529() else fallback!!
            scale = value.scale()
            wide = null
        }
    }

    private fun stored(): BigDecimal = wide ?: BigDecimal.valueOf(compact, scale)

    private fun expandedCopy(): DecimalSignedWork535 = finalWork?.copy() ?: stored().let { value ->
        DecimalSignedWork535(value.scale().toLong()).also { it.add(value) }
    }

    private fun observed(value: BigDecimal): BigDecimal = if (value::class == BigDecimal::class) value
        else BigDecimal(value.unscaledValue(), value.scale())

    private fun additionOverflows(a: Long, b: Long): Boolean =
        b > 0 && a > Long.MAX_VALUE - b || b < 0 && a < Long.MIN_VALUE - b

    private fun longDigits(value: Long): Int {
        var negative = if (value > 0) -value else value
        var digits = 1
        while (negative <= -10) { negative /= 10; digits++ }
        return digits
    }
}

/** Streaming sum; empty input is ZERO, and the first term establishes the scale. */
fun Iterable<BigDecimal>.sumDecimals(
    context: MathContext = MathContext.UNLIMITED,
    rounding: DecimalRounding = DecimalRounding.FINAL_RESULT,
    maxWorkingDigits: Long? = null,
    maxTerms: Long? = null,
): BigDecimal {
    val accumulator = DecimalAccumulator(context, rounding, maxWorkingDigits, maxTerms)
    for (value in this) accumulator.add(value)
    return accumulator.snapshot()
}

/** Streaming dot product. Unequal iterable lengths are invalid input. */
fun Iterable<BigDecimal>.dotProduct(
    other: Iterable<BigDecimal>,
    context: MathContext = MathContext.UNLIMITED,
    rounding: DecimalRounding = DecimalRounding.FINAL_RESULT,
    maxWorkingDigits: Long? = null,
    maxTerms: Long? = null,
): BigDecimal {
    val accumulator = DecimalAccumulator(context, rounding, maxWorkingDigits, maxTerms)
    val left = iterator()
    val right = other.iterator()
    while (left.hasNext() && right.hasNext()) accumulator.addProduct(left.next(), right.next())
    require(!left.hasNext() && !right.hasNext()) { "Dot product lengths differ" }
    return accumulator.snapshot()
}

/** A product plus addend with an explicit rounding policy; final-only is the default. */
fun BigDecimal.fma(
    multiplier: BigDecimal,
    addend: BigDecimal,
    context: MathContext = MathContext.UNLIMITED,
    rounding: DecimalRounding = DecimalRounding.FINAL_RESULT,
    maxWorkingDigits: Long? = null,
): BigDecimal = DecimalAccumulator(context, rounding, maxWorkingDigits, 2L)
    .addProduct(this, multiplier).add(addend).snapshot()

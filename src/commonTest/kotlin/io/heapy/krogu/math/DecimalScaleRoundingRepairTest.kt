// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/** Task566 source milestone: PREPARED / UNCOMPILED / UNRUN. No giant operands. */
@Suppress("DEPRECATION")
class DecimalScaleRoundingRepairTest {
    private class Truthful(c: BigInteger, s: Int) : BigDecimal(c, s) {
        constructor(c: Long, s: Int) : this(BigInteger.valueOf(c), s)
        override fun precision() = super.precision()
        override fun signum() = super.signum()
        override fun scale() = super.scale()
        override fun unscaledValue() = super.unscaledValue()
        override fun add(augend: BigDecimal, mc: MathContext) = super.add(augend, mc)
        override fun multiply(multiplicand: BigDecimal, mc: MathContext) = super.multiply(multiplicand, mc)
        override fun setScale(newScale: Int, roundingMode: Int) = super.setScale(newScale, roundingMode)
        override fun setScale(newScale: Int, roundingMode: RoundingMode) = super.setScale(newScale, roundingMode)
        fun addBase(b: BigDecimal, mc: MathContext) = super.add(b, mc)
        fun multiplyBase(b: BigDecimal, mc: MathContext) = super.multiply(b, mc)
        fun scaleBase(s: Int, mode: RoundingMode) = super.setScale(s, mode)
        fun scaleBase(s: Int, mode: Int) = super.setScale(s, mode)
        fun scaleBase(s: Int) = super.setScale(s)
    }

    @Test fun downscaleAcrossEntireSignedScaleDistance() {
        val transitions = listOf(
            Int.MAX_VALUE to Int.MIN_VALUE, Int.MAX_VALUE to 0,
            0 to Int.MIN_VALUE, 646456993 to 0, 646456992 to 0,
            3 to 2, 3 to 1, 3 to 0, 3 to -1, 3 to -2,
            Int.MIN_VALUE + 3 to Int.MIN_VALUE, Int.MAX_VALUE to Int.MAX_VALUE - 3,
        )
        // k<D, k=D, k>D; below/tie/above half; even/odd retained digits; exact tails.
        for ((source, target) in transitions) for (c in listOf(0L, 1L, 4L, 5L, 6L,
            9L, 10L, 49L, 50L, 51L, 99L, 100L, 149L, 150L, 151L, 250L, 251L, 999L)) {
            for (sign in listOf(-1, 1)) for (truthful in listOf(false, true)) {
                val x = input(c * sign, source, truthful)
                for (mode in RoundingMode.entries) {
                    val expected = ScaleRounding566Expected.rescale(c * sign, source, target, mode.name)
                    assertEquals(expected, outcome { x.setScale(target, mode) })
                    assertEquals(expected, outcome { x.setScale(target, mode.ordinal) })
                    if (x is Truthful) {
                        assertEquals(expected, outcome { x.scaleBase(target, mode) })
                        assertEquals(expected, outcome { x.scaleBase(target, mode.ordinal) })
                    }
                }
                val exact = ScaleRounding566Expected.rescale(c * sign, source, target, "UNNECESSARY")
                assertEquals(exact, outcome { x.setScale(target) })
                if (x is Truthful) assertEquals(exact, outcome { x.scaleBase(target) })
            }
        }
    }

    @Test fun equalAndIncreasingScaleAndGenuineUnsupportedOutputs() {
        for (s in listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE)) for (truthful in listOf(false, true)) {
            val x = input(7, s, truthful)
            for (mode in RoundingMode.entries) assertSame(x, x.setScale(s, mode))
            for (target in listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE)) for (mode in RoundingMode.entries)
                assertEquals("0@$target", outcome { input(0, s, truthful).setScale(target, mode) })
        }
        for (sign in listOf(-1L, 1L)) for (mode in RoundingMode.entries) {
            assertEquals("${sign * 700}@2", outcome { BigDecimal.valueOf(sign * 7).setScale(2, mode) })
            // 10^MAX needs >MAX bits; the requested nonzero coefficient cannot fit.
            assertFailsWith<ArithmeticException> { BigDecimal.valueOf(sign).setScale(Int.MAX_VALUE, mode) }
            assertFailsWith<ArithmeticException> {
                BigDecimal.valueOf(sign, Int.MIN_VALUE).setScale(Int.MAX_VALUE, mode.ordinal)
            }
        }
        assertFailsWith<IllegalArgumentException> { BigDecimal.ONE.setScale(0, -1) }
        assertFailsWith<IllegalArgumentException> { BigDecimal.ZERO.setScale(0, 8) }
    }

    @Test fun boundedWideCoefficientsUseIndependentLiteralThresholds() {
        // Eighty digits exercise wide coefficients without approaching the range limit.
        // Each row is coefficient, target scale, truncated magnitude, half comparison.
        val rows = listOf(
            listOf("4" + "0".repeat(79), "0", "0", "-1"),
            listOf("5" + "0".repeat(79), "0", "0", "0"),
            listOf("5" + "0".repeat(78) + "1", "0", "0", "1"),
            listOf("55" + "0".repeat(78), "1", "5", "0"),
            listOf("65" + "0".repeat(78), "1", "6", "0"),
        )
        for ((text, targetText, wholeText, halfText) in rows) for (sign in listOf(-1, 1)) {
            val c = BigInteger((if (sign < 0) "-" else "") + text)
            val target = targetText.toInt()
            val whole = wholeText.toInt()
            val half = halfText.toInt()
            for (truthful in listOf(false, true)) for (mode in RoundingMode.entries) {
                val x = if (truthful) Truthful(c, 80) else BigDecimal(c, 80)
                val up = when (mode) {
                    RoundingMode.UP -> true
                    RoundingMode.DOWN, RoundingMode.UNNECESSARY -> false
                    RoundingMode.CEILING -> sign > 0
                    RoundingMode.FLOOR -> sign < 0
                    RoundingMode.HALF_UP -> half >= 0
                    RoundingMode.HALF_DOWN -> half > 0
                    RoundingMode.HALF_EVEN -> half > 0 || half == 0 && whole % 2 != 0
                }
                val expected = if (mode == RoundingMode.UNNECESSARY) "!ArithmeticException"
                    else "${sign * (whole + if (up) 1 else 0)}@$target"
                assertEquals(expected, outcome { x.setScale(target, mode) })
                assertEquals(expected, outcome { x.setScale(target, mode.ordinal) })
                if (x is Truthful) assertEquals(expected, outcome { x.scaleBase(target, mode) })
            }
        }
        // 10^80 at logical scale MAX+80 has 81 digits. Dropping 80 exact
        // zeros is mandatory at p1 and succeeds even under UNNECESSARY.
        val power = BigInteger("1" + "0".repeat(80))
        for (sign in listOf(-1, 1)) for (mode in RoundingMode.entries) for (truthful in listOf(false, true)) {
            val c = if (sign < 0) power.negate() else power
            val x = if (truthful) Truthful(c, Int.MAX_VALUE) else BigDecimal(c, Int.MAX_VALUE)
            val y = Truthful(1, 80)
            val mc = MathContext(1, mode)
            assertEquals("$sign@${Int.MAX_VALUE}", outcome { x.multiply(y, mc) })
            assertEquals("$sign@${Int.MAX_VALUE}", outcome { y.multiplyBase(x, mc) })
        }
    }

    @Test fun contextMultiplicationRequiredReductionAndCarry() {
        val scales = listOf(Int.MAX_VALUE to 1, Int.MAX_VALUE to 2,
            Int.MAX_VALUE to 3, Int.MAX_VALUE - 1 to 2, 0 to 0,
            Int.MIN_VALUE to 0, Int.MIN_VALUE + 2 to 0)
        for ((sa, sb) in scales) for ((a, b) in listOf(1L to 10L, 1L to 99L,
            1L to 95L, 1L to 949L, 1L to 999L, 25L to 4L, 99L to 99L)) {
            for (ls in listOf(-1, 1)) for (rs in listOf(-1, 1)) for (mask in 0..3) {
                val x = input(a * ls, sa, mask and 1 != 0)
                val y = input(b * rs, sb, mask and 2 != 0)
                for (p in listOf(1, 2, 3, 7)) for (mode in RoundingMode.entries) {
                    val mc = MathContext(p, mode)
                    val expected = ScaleRounding566Expected.product(a * ls * b * rs, sa.toLong() + sb, p, mode.name)
                    assertEquals(expected, outcome { x.multiply(y, mc) }, "$a@$sa * $b@$sb $mc")
                    assertEquals(expected, outcome { y.multiply(x, mc) })
                    if (x is Truthful) assertEquals(expected, outcome { x.multiplyBase(y, mc) })
                }
            }
        }
    }

    @Test fun exactUnlimitedAndUnprovedHighCohortControlsRemainErrors() {
        val one = BigDecimal.valueOf(1, Int.MAX_VALUE)
        val ten = BigDecimal.valueOf(10, 1)
        // Preserved controls, not a claim that all alternative cohorts are mandatory.
        assertFailsWith<ArithmeticException> { one.multiply(ten) }
        for (p in listOf(0, 2, Int.MAX_VALUE)) for (mode in RoundingMode.entries)
            assertFailsWith<ArithmeticException> { one.multiply(ten, MathContext(p, mode)) }
        for (mode in RoundingMode.entries) {
            val mc = MathContext(1, mode)
            // Existing receiver-zero saturation and nonzero-receiver rejection are retained.
            val zero = BigDecimal.valueOf(0, Int.MAX_VALUE)
            assertEquals("0@${Int.MAX_VALUE}", outcome { zero.multiply(ten, mc) })
            assertFailsWith<ArithmeticException> { ten.multiply(zero, mc) }
            assertFailsWith<ArithmeticException> {
                BigDecimal.valueOf(10, Int.MIN_VALUE).multiply(BigDecimal.valueOf(1, -1), mc)
            }
        }
    }

    @Test fun zeroContextAdditionBothOrientationsAndSubtraction() {
        val scales = listOf(Int.MIN_VALUE to Int.MAX_VALUE, Int.MIN_VALUE to 0,
            Int.MAX_VALUE to Int.MIN_VALUE, -3 to 3, 0 to 0, Int.MAX_VALUE - 1 to Int.MAX_VALUE)
        for ((s, z) in scales) for (c in listOf(-999L, -95L, -10L, -1L, 1L, 10L, 95L, 999L)) {
            for (mask in 0..3) for (p in listOf(1, 2, 3, 7)) for (mode in RoundingMode.entries) {
                val x = input(c, s, mask and 1 != 0)
                val zero = input(0, z, mask and 2 != 0)
                val mc = MathContext(p, mode)
                val expected = ScaleRounding566Expected.zeroAdd(c, s, z, p, mode.name)
                assertEquals(expected, outcome { x.add(zero, mc) })
                assertEquals(expected, outcome { zero.add(x, mc) })
                assertEquals(expected, outcome { x.subtract(zero, mc) })
                assertEquals(ScaleRounding566Expected.zeroAdd(-c, s, z, p, mode.name),
                    outcome { zero.subtract(x, mc) })
                if (x is Truthful) assertEquals(expected, outcome { x.addBase(zero, mc) })
                if (zero is Truthful) assertEquals(expected, outcome { zero.addBase(x, mc) })
            }
        }
        for (s in listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE)) for (z in listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE))
            for (mode in RoundingMode.entries) assertEquals("0@${maxOf(s, z)}", outcome {
                BigDecimal.valueOf(0, s).add(BigDecimal.valueOf(0, z), MathContext(1, mode))
            })
        // Finite precision MAX really requests unsupported padding at preferred MAX.
        for (mode in RoundingMode.entries) assertFailsWith<ArithmeticException> {
            BigDecimal.valueOf(1, Int.MIN_VALUE).add(BigDecimal.valueOf(0, Int.MAX_VALUE), MathContext(Int.MAX_VALUE, mode))
        }
    }

    @Test fun explicitCallerOverridesNullsAndCallerOwnedSuper() {
        val sentinel = IllegalStateException("caller sentinel")
        val replacement = BigDecimal("7.00")
        class Caller(private val fail: Boolean) : BigDecimal(1) {
            private fun answer(): BigDecimal { if (fail) throw sentinel; return replacement }
            override fun add(augend: BigDecimal, mc: MathContext) = answer()
            override fun multiply(multiplicand: BigDecimal, mc: MathContext) = answer()
            override fun setScale(newScale: Int, roundingMode: Int) = answer()
            override fun setScale(newScale: Int, roundingMode: RoundingMode) = answer()
        }
        for (fail in listOf(false, true)) {
            val x: BigDecimal = Caller(fail)
            for (call in listOf<() -> BigDecimal>({ x.add(BigDecimal.ONE, MathContext.UNLIMITED) }, { x.multiply(BigDecimal.ONE, MathContext.UNLIMITED) },
                { x.setScale(0, RoundingMode.DOWN) }, { x.setScale(0, 1) }, { x.setScale(0) })) {
                if (fail) assertSame(sentinel, assertFailsWith<IllegalStateException>(block = call))
                else assertSame(replacement, call())
            }
        }
        // Four direct-override and six explicit-super null executions replay on JVM.
    }

    private fun input(c: Long, s: Int, truthful: Boolean): BigDecimal =
        if (truthful) Truthful(c, s) else BigDecimal.valueOf(c, s)

    private fun outcome(action: () -> BigDecimal): String = try {
        val x = action(); "${x.unscaledValue()}@${x.scale()}"
    } catch (_: ArithmeticException) { "!ArithmeticException" }
}

/** Independent bounded Long quotient/remainder model; never calls decimal or big-integer arithmetic. */
internal object ScaleRounding566Expected {
    private const val ERROR = "!ArithmeticException"
    private fun digits(c: Long) = kotlin.math.abs(c).toString().length
    private fun power(k: Int): Long {
        require(k in 0..18)
        var n = 1L
        repeat(k) { n *= 10 }
        return n
    }
    private fun quotient(c: Long, k: Long, mode: String): Long? {
        if (c == 0L) return 0
        val sign = if (c < 0) -1 else 1
        val n = kotlin.math.abs(c)
        val divisor = if (k > 18) null else power(k.toInt())
        val q = if (divisor == null) 0 else n / divisor
        val r = if (divisor == null) n else n % divisor
        if (r == 0L) return c / divisor!!
        if (mode == "UNNECESSARY") return null
        val half = if (divisor == null) -1 else r.compareTo(divisor - r)
        val up = when (mode) {
            "UP" -> true
            "DOWN" -> false
            "CEILING" -> sign > 0
            "FLOOR" -> sign < 0
            "HALF_UP" -> half >= 0
            "HALF_DOWN" -> half > 0
            "HALF_EVEN" -> half > 0 || half == 0 && q % 2 != 0L
            else -> error(mode)
        }
        return sign * (q + if (up) 1 else 0)
    }
    fun rescale(c: Long, s: Int, target: Int, mode: String): String {
        require(target <= s)
        return quotient(c, s.toLong() - target, mode)?.let { "$it@$target" } ?: ERROR
    }
    private fun rounded(c: Long, s: Long, p: Int, mode: String): Pair<Long, Long>? {
        require(p > 0)
        val drop = maxOf(0, digits(c) - p)
        val q = quotient(c, drop.toLong(), mode) ?: return null
        return if (digits(q) > p) q / 10 to s - drop - 1 else q to s - drop
    }
    fun product(c: Long, s: Long, p: Int, mode: String): String {
        val (q, scale) = rounded(c, s, p, mode) ?: return ERROR
        // Current unproved HIGH exact-cohort behavior is intentionally retained.
        return if (scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) ERROR else "$q@$scale"
    }
    fun zeroAdd(c: Long, s: Int, z: Int, p: Int, mode: String): String {
        val (q, scale) = rounded(c, s.toLong(), p, mode) ?: return ERROR
        if (scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) return ERROR
        val target = minOf(maxOf(s, z).toLong(), scale + p - digits(q))
        require(target - scale in 0..7)
        return "${q * power((target - scale).toInt())}@$target"
    }
}

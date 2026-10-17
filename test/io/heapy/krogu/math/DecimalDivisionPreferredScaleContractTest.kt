// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DecimalDivisionPreferredScaleContractTest {
    private class Truthful(c: Long, s: Int) : BigDecimal(BigInteger.valueOf(c), s) {
        override fun precision() = super.precision()
        override fun signum() = super.signum()
        override fun scale() = super.scale()
        override fun unscaledValue() = super.unscaledValue()
        override fun divide(divisor: BigDecimal) = super.divide(divisor)
        override fun divide(divisor: BigDecimal, mc: MathContext) = super.divide(divisor, mc)
    }

    @Test fun lowExactAndUnlimitedWitness() {
        matrix(1, 2, Int.MIN_VALUE, 1, listOf(0), exact = true)
    }

    @Test fun lowFiniteWitnessAllModes() {
        matrix(1, 2, Int.MIN_VALUE, 1, listOf(1, 2, 7))
    }

    @Test fun highOneDigitWitnessOnly() {
        matrix(10, 1, Int.MAX_VALUE, -1, listOf(1))
    }

    @Test fun reducedDenominatorsAndCancellation() {
        for ((a, b) in listOf(1L to 4L, 1L to 5L, 1L to 8L, 1L to 25L, 1L to 125L,
            6L to 15L, 3L to 24L, 40L to 16L, 125L to 25L, 1200L to 3L)) {
            for (distance in listOf(0, 1, 3)) for (sb in listOf(1, 2))
                matrix(a, b, Int.MIN_VALUE + distance, sb, listOf(0, 1, 2, 7), exact = true)
        }
    }

    @Test fun finiteRoundingAndCarryNeighbors() {
        for ((a, b) in listOf(19L to 20L, 189L to 200L, 191L to 200L,
            199L to 200L, 1999L to 2000L, 1L to 3L, 29L to 3L, 299L to 30L))
            for (distance in listOf(0, 1)) for (sb in listOf(1, 2))
                matrix(a, b, Int.MIN_VALUE + distance, sb, listOf(1, 2, 7))
    }

    @Test fun hugePrecisionDoesNotPadSmallExactResult() {
        for ((a, b) in listOf(1L to 2L, 6L to 15L, 1L to 1L, 1200L to 3L))
            matrix(a, b, Int.MIN_VALUE, 1, listOf(Int.MAX_VALUE))
    }

    @Test fun genuineErrorsAndNulls() {
        matrix(1, 1, Int.MIN_VALUE, 1, listOf(1))
        matrix(1, 1, Int.MAX_VALUE, -1, listOf(0, 1, 2), exact = true)
        matrix(1, 3, Int.MIN_VALUE, 1, listOf(0), exact = true)
        for (mask in 0..3) for (mode in RoundingMode.entries) {
            val a = input(1, Int.MIN_VALUE, mask and 1 != 0)
            val zero = input(0, 1, mask and 2 != 0)
            assertFailsWith<ArithmeticException> { a.divide(zero) }
            assertFailsWith<ArithmeticException> { a.divide(zero, MathContext(1, mode)) }
            // All 4 masks x 8 modes x 3 null controls replay in DecimalBinary575StandaloneTest.
        }
        for (mode in RoundingMode.entries) {
            assertEquals(Int.MIN_VALUE, BigDecimal.valueOf(0, Int.MIN_VALUE)
                .divide(BigDecimal.valueOf(2, 1), MathContext(1, mode)).scale())
            assertEquals(Int.MAX_VALUE, BigDecimal.valueOf(0, Int.MAX_VALUE)
                .divide(BigDecimal.valueOf(1, -1), MathContext(1, mode)).scale())
        }
    }

    @Test fun independentStringModelMatchesLongRationalModel() {
        for (a in listOf(-1999L, -1200L, -29L, -1L, 1L, 19L, 125L, 2000L))
            for (b in listOf(-2000L, -125L, -30L, -3L, 1L, 2L, 8L, 24L))
                for (sa in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, 0, Int.MAX_VALUE))
                    for (p in listOf(0, 1, 2, 7)) for (mode in RoundingMode.entries)
                        assertEquals(PreferredScaleRationalModel.pair(a, sa, b, 1, p, mode.name),
                            PreferredScaleStringModel.pair(a.toString(), sa, b.toString(), 1, p, mode.name))
        val numerator = "9".repeat(100)
        for (mode in RoundingMode.entries) {
            val expected = when (mode) {
                RoundingMode.UNNECESSARY -> "!ArithmeticException"
                RoundingMode.DOWN, RoundingMode.FLOOR -> "9@${Int.MAX_VALUE - 98}"
                else -> "1@${Int.MAX_VALUE - 99}"
            }
            assertEquals(expected, PreferredScaleStringModel.pair(numerator, Int.MAX_VALUE, "1", -1, 1, mode.name))
        }
    }

    private fun input(c: Long, s: Int, truthful: Boolean): BigDecimal =
        if (truthful) Truthful(c, s) else BigDecimal.valueOf(c, s)

    private fun matrix(a: Long, b: Long, sa: Int, sb: Int, precisions: List<Int>, exact: Boolean = false) {
        for (ls in listOf(-1, 1)) for (rs in listOf(-1, 1)) for (mask in 0..3) {
            val x = input(a * ls, sa, mask and 1 != 0)
            val y = input(b * rs, sb, mask and 2 != 0)
            if (exact) verify(x, y, a * ls, sa, b * rs, sb, null, RoundingMode.UNNECESSARY)
            for (p in precisions) for (mode in RoundingMode.entries)
                verify(x, y, a * ls, sa, b * rs, sb, p, mode)
        }
    }

    private fun verify(x: BigDecimal, y: BigDecimal, a: Long, sa: Int, b: Long, sb: Int,
        p: Int?, mode: RoundingMode) {
        val expected = PreferredScaleRationalModel.pair(a, sa, b, sb, p ?: 0, mode.name)
        val actual = try {
            val q = if (p == null) x.divide(y) else x.divide(y, MathContext(p, mode))
            "${q.unscaledValue()}@${q.scale()}"
        } catch (_: ArithmeticException) { "!ArithmeticException" }
        if (expected == "!AdmittedScale") {
            val alternative = PreferredScaleRationalModel.pair(a, sa, b, sb, p ?: 0, mode.name, allowPadding = true)
            assertTrue(actual == "!ArithmeticException" || actual == alternative,
                "admitted LOW padding: $actual; supported alternative=$alternative")
        } else assertEquals(expected, actual, "$a@$sa / $b@$sb p=$p mode=$mode")
    }
}

/** Bounded Long arithmetic only; no port or JDK numerical arithmetic supplies expected values. */
internal object PreferredScaleRationalModel {
    private val scales = Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()
    private fun gcd(a: Long, b: Long): Long {
        var x = a; var y = b
        while (y != 0L) { val r = x % y; x = y; y = r }
        return x
    }
    private fun multiply(a: Long, b: Long): Long {
        require(a >= 0 && b > 0 && a <= Long.MAX_VALUE / b)
        return a * b
    }
    fun pair(a: Long, sa: Int, b: Long, sb: Int, p: Int, mode: String,
        allowPadding: Boolean = false): String {
        require(a in -2000..2000 && b in -2000..2000)
        require(p in 0..7 || p == Int.MAX_VALUE)
        val preferred = sa.toLong() - sb
        if (b == 0L) return "!ArithmeticException"
        if (a == 0L) return "0@${preferred.coerceIn(scales)}"
        val sign = if ((a < 0) != (b < 0)) -1 else 1
        var n = kotlin.math.abs(a); var d = kotlin.math.abs(b)
        val g = gcd(n, d)
        var reduced = d / g; var exact = n / g; var places = 0
        // Cancel factors after multiplying the rational by ten, rather than
        // using production's factor-two/factor-five construction.
        while (reduced != 1L) {
            exact = multiply(exact, 10)
            val cancellation = gcd(exact, reduced)
            if (cancellation == 1L) break
            exact /= cancellation; reduced /= cancellation; places++
        }
        fun result(c: Long, s: Long): String =
            if (s !in scales) "!ArithmeticException" else "${c * sign}@$s"
        var exactScale = preferred + places
        if (reduced == 1L) {
            if (exactScale < scales.first) {
                val padding = scales.first - exactScale
                if (p != 0 && exact.toString().length + padding > p) return "!ArithmeticException"
                if (!allowPadding) return "!AdmittedScale"
                require(padding <= 15)
                repeat(padding.toInt()) { exact = multiply(exact, 10); exactScale++ }
            }
            if (p == 0 || exact.toString().length <= p) return result(exact, exactScale)
        }
        if (p == 0) return "!ArithmeticException"
        require(p <= 7) // Huge precision is exercised only for a bounded exact cohort.
        var exponent = 0
        while (n < d) { n = multiply(n, 10); exponent-- }
        while (n / 10 >= d) { d = multiply(d, 10); exponent++ }
        repeat(p - 1) { n = multiply(n, 10) }
        var whole = n / d; val remainder = n % d
        if (remainder != 0L && mode == "UNNECESSARY") return "!ArithmeticException"
        val half = multiply(remainder, 2).compareTo(d)
        val increment = remainder != 0L && when (mode) {
            "UP" -> true
            "DOWN", "UNNECESSARY" -> false
            "CEILING" -> sign > 0
            "FLOOR" -> sign < 0
            "HALF_UP" -> half >= 0
            "HALF_DOWN" -> half > 0
            "HALF_EVEN" -> half > 0 || half == 0 && whole % 2 != 0L
            else -> error("Unknown mode $mode")
        }
        if (increment) whole++
        var scale = preferred + p - 1L - exponent
        if (whole.toString().length > p) { whole /= 10; scale-- }
        if (remainder == 0L) while (scale > preferred && scale > scales.first && whole % 10 == 0L) {
            whole /= 10; scale--
        }
        return result(whole, scale)
    }
}

/** Decimal long division over bounded strings, independent of both numerical libraries. */
internal object PreferredScaleStringModel {
    private val scales = Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()
    private fun canonical(s: String): String = s.trimStart('0').ifEmpty { "0" }
    private fun compare(a: String, b: String): Int =
        if (a.length != b.length) a.length.compareTo(b.length) else a.compareTo(b)
    private fun subtract(a: String, b: String): String {
        require(compare(a, b) >= 0)
        val out = CharArray(a.length); var borrow = 0
        for (i in a.indices.reversed()) {
            val j = i - (a.length - b.length)
            var digit = a[i] - '0' - borrow - if (j >= 0) b[j] - '0' else 0
            borrow = if (digit < 0) 1 else 0
            if (digit < 0) digit += 10
            out[i] = ('0'.code + digit).toChar()
        }
        return canonical(out.concatToString())
    }
    private fun times(a: String, factor: Int): String {
        require(factor in 0..10)
        val out = StringBuilder(); var carry = 0
        for (i in a.indices.reversed()) {
            val digit = (a[i] - '0') * factor + carry
            out.append(('0'.code + digit % 10).toChar()); carry = digit / 10
        }
        if (carry > 0) out.append(('0'.code + carry).toChar())
        return canonical(out.reverse().toString())
    }
    private fun increment(a: String): String {
        val out = a.toCharArray()
        for (i in out.indices.reversed()) {
            if (out[i] != '9') { out[i]++; return out.concatToString() }
            out[i] = '0'
        }
        return "1" + out.concatToString()
    }
    private fun division(a: String, b: String): Pair<String, String> {
        require(b != "0")
        val multiples = (0..9).map { times(b, it) }
        val quotient = StringBuilder(); var remainder = "0"
        for (digit in a) {
            remainder = canonical(remainder + digit)
            var q = 9
            while (compare(multiples[q], remainder) > 0) q--
            quotient.append(('0'.code + q).toChar())
            remainder = subtract(remainder, multiples[q])
        }
        return canonical(quotient.toString()) to remainder
    }
    private fun gcd(a: String, b: String): String {
        var x = a; var y = b
        while (y != "0") { val r = division(x, y).second; x = y; y = r }
        return x
    }
    fun pair(a: String, sa: Int, b: String, sb: Int, p: Int, mode: String): String {
        val preferred = sa.toLong() - sb
        var n = canonical(a.removePrefix("-")); var d = canonical(b.removePrefix("-"))
        require(n.length <= 256 && d.length <= 256)
        require(p in 0..256 || p >= Int.MAX_VALUE - 1)
        if (d == "0") return "!ArithmeticException"
        if (n == "0") return "0@${preferred.coerceIn(scales)}"
        val negative = a.startsWith('-') != b.startsWith('-')
        fun result(c: String, s: Long): String = if (s !in scales) "!ArithmeticException"
            else "${if (negative) "-" else ""}$c@$s"
        val g = gcd(n, d)
        var exact = division(n, g).first; var reduced = division(d, g).first; var places = 0
        // Ten-fold rational cancellation is bounded by the denominator bit count.
        while (reduced != "1") {
            exact = times(exact, 10)
            val cancellation = gcd(exact, reduced)
            if (cancellation == "1") break
            exact = division(exact, cancellation).first
            reduced = division(reduced, cancellation).first
            places++
        }
        var exactScale = preferred + places
        if (reduced == "1") {
            if (exactScale < scales.first) {
                val requiredDigits = exact.length.toLong() + scales.first - exactScale
                return if (p != 0 && requiredDigits > p) "!ArithmeticException" else "!AdmittedScale"
            }
            if (p == 0 || exact.length <= p) {
                // Closest preferred cohort: remove numerator's integer zeros
                // only if exact division's extra fractional places introduced them.
                while (exactScale > preferred && exactScale > scales.first && exact.endsWith('0')) {
                    exact = exact.dropLast(1); exactScale--
                }
                return result(exact, exactScale)
            }
        }
        if (p == 0 || p > 256) return "!ArithmeticException"
        var exponent = 0
        while (compare(n, d) < 0) { n = times(n, 10); exponent-- }
        while (compare(n, times(d, 10)) >= 0) { d = times(d, 10); exponent++ }
        val qr = division(n + "0".repeat(p - 1), d)
        var whole = qr.first; val remainder = qr.second
        if (remainder != "0" && mode == "UNNECESSARY") return "!ArithmeticException"
        val half = compare(times(remainder, 2), d)
        val up = remainder != "0" && when (mode) {
            "UP" -> true
            "DOWN", "UNNECESSARY" -> false
            "CEILING" -> !negative
            "FLOOR" -> negative
            "HALF_UP" -> half >= 0
            "HALF_DOWN" -> half > 0
            "HALF_EVEN" -> half > 0 || half == 0 && (whole.last() - '0') % 2 != 0
            else -> error("Unknown mode $mode")
        }
        if (up) whole = increment(whole)
        var scale = preferred + p - 1L - exponent
        if (whole.length > p) { whole = whole.dropLast(1); scale-- }
        if (remainder == "0") while (scale > preferred && scale > scales.first && whole.endsWith('0')) {
            whole = whole.dropLast(1); scale--
        }
        return result(whole, scale)
    }
    /** Substitute only independently supported bounded outside-preferred results. */
    fun correctedText(a: BigDecimal, b: BigDecimal, mc: MathContext?): String? {
        val preferred = a.scale().toLong() - b.scale()
        if (preferred in scales) return null
        val left = a.unscaledValue().toString(); val right = b.unscaledValue().toString()
        val p = mc?.precision ?: 0
        if (left.removePrefix("-").length > 256 || right.removePrefix("-").length > 256 ||
            left == "0" || right == "0" || p > 256 && p < Int.MAX_VALUE - 1) return null
        val expected = pair(left, a.scale(), right, b.scale(), p, mc?.roundingMode?.name ?: "UNNECESSARY")
        if (expected.startsWith('!')) return null
        return text(expected)
    }
    fun text(pair: String): String {
        val coefficient = pair.substringBefore('@'); val scale = pair.substringAfter('@').toInt()
        val digits = coefficient.removePrefix("-"); val sign = if (coefficient.startsWith('-')) "-" else ""
        val exponent = digits.length - 1L - scale
        if (scale >= 0 && exponent >= -6) {
            val point = digits.length - scale
            return sign + when {
                scale == 0 -> digits
                point > 0 -> digits.take(point) + "." + digits.drop(point)
                else -> "0." + "0".repeat(-point) + digits
            }
        }
        return sign + digits.take(1) + (if (digits.length > 1) "." + digits.drop(1) else "") +
            "E" + (if (exponent >= 0) "+" else "") + exponent
    }
}

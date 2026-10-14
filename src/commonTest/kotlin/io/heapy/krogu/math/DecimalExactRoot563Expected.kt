// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Independent decimal digit-pair root and string rounding model; no sqrt/round/scale APIs. */
internal object DecimalExactRoot563Expected {
    data class Result(val coefficient: String, val scale: Int) {
        fun text(): String {
            val exponent = coefficient.length.toLong() - scale - 1
            if (scale >= 0 && exponent >= -6) return when {
                scale == 0 -> coefficient
                scale < coefficient.length -> coefficient.substring(0, coefficient.length - scale) + "." +
                    coefficient.substring(coefficient.length - scale)
                else -> "0." + "0".repeat(scale - coefficient.length) + coefficient
            }
            val body = if (coefficient.length == 1) coefficient else coefficient[0] + "." + coefficient.substring(1)
            return body + "E" + (if (exponent >= 0) "+" else "") + exponent
        }
    }

    /** null means the positive radicand has no finite decimal root. */
    fun root(coefficient: String, scale: Int): Pair<String, Int>? {
        if (coefficient.startsWith('-') || coefficient == "0") return null
        val reduced = coefficient.trimEnd('0')
        val normalizedScale = scale.toLong() - (coefficient.length - reduced.length)
        if (normalizedScale % 2L != 0L) return null
        // School digit-pair extraction: after each group, prefix = r^2 + remainder.
        var r = BigInteger.ZERO
        var remainder = BigInteger.ZERO
        var offset = 0
        var width = if (reduced.length % 2 == 0) 2 else 1
        while (offset < reduced.length) {
            remainder = remainder.multiply(BigInteger("100"))
                .add(BigInteger(reduced.substring(offset, offset + width)))
            val twentyR = r.multiply(BigInteger("20"))
            var digit = 9
            var trial: BigInteger
            while (true) {
                val d = BigInteger.valueOf(digit.toLong())
                trial = twentyR.add(d).multiply(d)
                if (trial <= remainder) break
                digit--
            }
            remainder = remainder.subtract(trial)
            r = r.multiply(BigInteger.TEN).add(BigInteger.valueOf(digit.toLong()))
            offset += width
            width = 2
        }
        if (remainder != BigInteger.ZERO) return null
        val t = normalizedScale / 2
        check(t in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
        return r.toString() to t.toInt()
    }

    /** Coefficient supplied as an independently known minimal positive decimal root. */
    fun rounded(root: String, scale: Int, preferred: Int, context: MathContext): Result {
        val p = context.precision
        if (p == 0 || p >= root.length) {
            val room = if (p == 0) Long.MAX_VALUE else (p - root.length).toLong()
            val pad = minOf(preferred.toLong() - scale, room)
            check(pad >= 0 && pad <= Int.MAX_VALUE)
            return Result(root + "0".repeat(pad.toInt()), (scale.toLong() + pad).toInt())
        }
        if (context.roundingMode == RoundingMode.UNNECESSARY) throw ArithmeticException("Required rounding")
        val tail = root.substring(p)
        val half = when {
            tail[0] < '5' -> -1
            tail[0] > '5' || tail.drop(1).any { it != '0' } -> 1
            else -> 0
        }
        val increment = when (context.roundingMode) {
            RoundingMode.UP, RoundingMode.CEILING -> true
            RoundingMode.DOWN, RoundingMode.FLOOR -> false
            RoundingMode.HALF_UP -> half >= 0
            RoundingMode.HALF_DOWN -> half > 0
            RoundingMode.HALF_EVEN -> half > 0 || (half == 0 && (root[p - 1] - '0') % 2 != 0)
            RoundingMode.UNNECESSARY -> error("Already rejected")
        }
        var coefficient = root.substring(0, p)
        if (increment) {
            val digits = coefficient.toCharArray()
            var i = digits.lastIndex
            while (i >= 0 && digits[i] == '9') { digits[i] = '0'; i-- }
            if (i >= 0) { digits[i] = digits[i] + 1; coefficient = digits.concatToString() }
            else coefficient = "1" + digits.concatToString()
        }
        var targetScale = scale.toLong() - (root.length - p)
        if (coefficient.length > p) { coefficient = coefficient.dropLast(1); targetScale-- }
        check(targetScale in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
        return Result(coefficient, targetScale.toInt())
    }

    /** Precisely the proved old normalization-underflow defect; preserve every other oracle route. */
    fun corrected(coefficient: String, scale: Int, context: MathContext): Result? {
        if (context.precision != 0 && context.roundingMode != RoundingMode.UNNECESSARY) return null
        if (coefficient.startsWith('-') || coefficient == "0") return null
        val z = coefficient.length - coefficient.trimEnd('0').length
        if (scale.toLong() - z >= Int.MIN_VALUE) return null
        val root = root(coefficient, scale) ?: return null
        if (context.precision != 0 && context.precision < root.first.length) return null
        return rounded(root.first, root.second, scale / 2, context)
    }
}

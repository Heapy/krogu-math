// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Independent bounded decimal-string model for the three proved task566 triggers.
 * null leaves the original oracle assertion active. No decimal arithmetic API
 * or BigInteger arithmetic supplies an expected value; getters read ordinary inputs.
 */
internal object DecimalScaleRounding566Expected {
    private const val ERROR = "!ArithmeticException"
    private const val LIMIT = 512
    private data class Value(val coefficient: String, val scale: Long)

    private fun input(a: BigDecimal): Value? {
        if (a::class != BigDecimal::class) return null
        val c = a.unscaledValue().toString()
        if (c.removePrefix("-").length > LIMIT) return null
        return Value(c, a.scale().toLong())
    }

    fun setScale(a: BigDecimal, target: Int, mode: RoundingMode): String? {
        val x = input(a) ?: return null
        val drop = x.scale - target
        // Precisely the removed distance/power rejection; zero already had an early path.
        if (x.coefficient == "0" || drop <= 646456992L) return null
        val c = dropDigits(x.coefficient, drop, mode) ?: return ERROR
        return text(Value(c, target.toLong()))
    }

    fun multiplication(a: BigDecimal, b: BigDecimal, mc: MathContext): String? {
        val x = input(a) ?: return null
        val y = input(b) ?: return null
        val scale = x.scale + y.scale
        if (mc.precision == 0 || scale <= Int.MAX_VALUE || x.coefficient == "0" || y.coefficient == "0") return null
        val negative = x.coefficient.startsWith('-') != y.coefficient.startsWith('-')
        val magnitude = multiplyDigits(x.coefficient.removePrefix("-"), y.coefficient.removePrefix("-"))
        if (magnitude.length <= mc.precision) return null // unproved exact HIGH rescue remains unchanged
        val c = (if (negative) "-" else "") + magnitude
        val result = rounded(Value(c, scale), mc) ?: return ERROR
        return if (result.scale in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) text(result) else ERROR
    }

    fun addition(a: BigDecimal, b: BigDecimal, mc: MathContext): String? {
        val x = input(a) ?: return null
        val y = input(b) ?: return null
        if (mc.precision == 0 || (x.coefficient == "0") == (y.coefficient == "0")) return null
        val nonzero = if (x.coefficient == "0") y else x
        val result = rounded(nonzero, mc) ?: return null // existing rounding failure, not the removed guard
        if (result.scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) return null
        val preferred = maxOf(x.scale, y.scale)
        if (preferred - result.scale <= Int.MAX_VALUE) return null
        val digits = result.coefficient.removePrefix("-").length
        val target = minOf(preferred, result.scale + mc.precision - digits)
        val padding = target - result.scale
        if (padding !in 0L..LIMIT.toLong()) return null // no unsupported-output waiver
        return text(Value(result.coefficient + "0".repeat(padding.toInt()), target))
    }

    private fun rounded(x: Value, mc: MathContext): Value? {
        val digits = x.coefficient.removePrefix("-").length
        val drop = maxOf(0, digits - mc.precision)
        var c = dropDigits(x.coefficient, drop.toLong(), mc.roundingMode) ?: return null
        var scale = x.scale - drop
        if (c.removePrefix("-").length > mc.precision) { c = c.dropLast(1); scale-- }
        return Value(c, scale)
    }

    private fun dropDigits(c: String, drop: Long, mode: RoundingMode): String? {
        if (drop == 0L) return c
        require(drop > 0)
        val negative = c.startsWith('-')
        val magnitude = c.removePrefix("-")
        val split = if (drop >= magnitude.length) 0 else magnitude.length - drop.toInt()
        var whole = if (split == 0) "0" else magnitude.substring(0, split)
        val tail = magnitude.substring(split)
        if (tail.all { it == '0' }) return if (negative && whole != "0") "-$whole" else whole
        if (mode == RoundingMode.UNNECESSARY) return null
        val half = when {
            drop > magnitude.length || tail[0] < '5' -> -1
            tail[0] > '5' || tail.drop(1).any { it != '0' } -> 1
            else -> 0
        }
        val increment = when (mode) {
            RoundingMode.UP -> true
            RoundingMode.DOWN -> false
            RoundingMode.CEILING -> !negative
            RoundingMode.FLOOR -> negative
            RoundingMode.HALF_UP -> half >= 0
            RoundingMode.HALF_DOWN -> half > 0
            RoundingMode.HALF_EVEN -> half > 0 || half == 0 && (whole.last() - '0') % 2 != 0
            RoundingMode.UNNECESSARY -> error("Already rejected")
        }
        if (increment) {
            val chars = whole.toCharArray()
            var i = chars.lastIndex
            while (i >= 0 && chars[i] == '9') { chars[i] = '0'; i-- }
            if (i < 0) whole = "1" + chars.concatToString()
            else { chars[i] = chars[i] + 1; whole = chars.concatToString() }
        }
        return if (negative && whole != "0") "-$whole" else whole
    }

    private fun multiplyDigits(a: String, b: String): String {
        val digits = IntArray(a.length + b.length)
        for (i in a.indices) for (j in b.indices)
            digits[i + j + 1] += (a[i] - '0') * (b[j] - '0')
        for (i in digits.lastIndex downTo 1) { digits[i - 1] += digits[i] / 10; digits[i] %= 10 }
        return digits.joinToString("").trimStart('0').ifEmpty { "0" }
    }

    private fun text(x: Value): String {
        val negative = x.coefficient.startsWith('-')
        val c = x.coefficient.removePrefix("-")
        val exponent = c.length.toLong() - x.scale - 1
        val body = if (x.scale >= 0 && exponent >= -6) when {
            x.scale == 0L -> c
            x.scale < c.length -> c.substring(0, c.length - x.scale.toInt()) + "." + c.substring(c.length - x.scale.toInt())
            else -> "0." + "0".repeat(x.scale.toInt() - c.length) + c
        } else {
            val significand = if (c.length == 1) c else c[0] + "." + c.substring(1)
            significand + "E" + (if (exponent >= 0) "+" else "") + exponent
        }
        return (if (negative) "-" else "") + body
    }
}

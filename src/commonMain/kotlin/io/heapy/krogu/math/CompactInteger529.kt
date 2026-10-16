package io.heapy.krogu.math

/** Primitive representation operations. No sentinel excludes Long.MIN_VALUE. */
internal object CompactInteger529 {
    private const val MASK = (1L shl 30) - 1

    fun sign(value: Long): Int = if (value < 0L) -1 else if (value > 0L) 1 else 0

    fun value(high: Int, low: Int): Long = (high.toLong() shl 32) or (low.toLong() and 0xffffffffL)

    fun addFits(a: Long, b: Long, result: Long): Boolean = ((a xor result) and (b xor result)) >= 0
    fun subtractFits(a: Long, b: Long, result: Long): Boolean = ((a xor b) and (a xor result)) >= 0
    fun multiplyFits(a: Long, b: Long, result: Long): Boolean =
        a == 0L || b == 0L ||
            (a != Long.MIN_VALUE || b != -1L) && (b != Long.MIN_VALUE || a != -1L) && result / a == b

    // A signed negative accumulator represents every magnitude through 2^63.
    fun decimalDigits(value: Long): Int {
        var negative = if (value > 0) -value else value
        var digits = 1
        while (negative <= -10) { negative /= 10; digits++ }
        return digits
    }

    fun compareDecimalSignificands(a: Long, b: Long): Int {
        var left = if (a > 0) -a else a
        var right = if (b > 0) -b else b
        val difference = decimalDigits(left) - decimalDigits(right)
        var sticky = false
        if (difference > 0) repeat(difference) { sticky = sticky || left % 10 != 0L; left /= 10 }
        if (difference < 0) repeat(-difference) { sticky = sticky || right % 10 != 0L; right /= 10 }
        val order = right.compareTo(left)
        return if (order != 0) order else if (!sticky) 0 else if (difference > 0) 1 else -1
    }

    fun magnitudeBits(value: Long): Int {
        val magnitude = if (value < 0) -value else value
        return 64 - magnitude.countLeadingZeroBits()
    }

    fun fits(sign: Int, words: IntArray): Boolean = words.size < 3 || words.size == 3 &&
        (words[2] < 8 || sign < 0 && words[2] == 8 && words[1] == 0 && words[0] == 0)

    fun fromMagnitude(sign: Int, words: IntArray): Long {
        var magnitude = 0L
        for (i in words.indices.reversed()) magnitude = (magnitude shl 30) or words[i].toLong()
        return if (sign < 0) -magnitude else magnitude
    }

    fun magnitudeWord(high: Int, low: Int, index: Int): Int {
        val negative = high < 0
        val lo = if (negative) -low else low
        val hi = if (negative) high.inv() + (if (low == 0) 1 else 0) else high
        return when (index) {
            0 -> lo and MASK.toInt()
            1 -> ((lo ushr 30) or (hi shl 2)) and MASK.toInt()
            2 -> hi ushr 28
            else -> 0
        }
    }

    fun magnitude(high: Int, low: Int): IntArray {
        val bits = magnitudeBits(value(high, low))
        return IntArray((bits + 29) / 30) { magnitudeWord(high, low, it) }
    }
}

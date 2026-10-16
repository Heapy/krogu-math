// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** The existing canonical, plain and engineering decimal layouts. */
enum class DecimalTextFormat { CANONICAL, PLAIN, ENGINEERING }

internal fun checkTextLimit537(length: Long, maxChars: Long?) {
    require(maxChars == null || maxChars >= 0) { "maxChars must be nonnegative" }
    if (maxChars != null && length > maxChars) throw ArithmeticException("Output character limit exceeded")
}

internal fun checkTextBuffer537(destination: CharArray, offset: Int, length: Int) {
    if (offset < 0 || length < 0 || offset > destination.size - length)
        throw IndexOutOfBoundsException("Bad output character range")
}

internal interface NumericSink537 {
    fun character(value: Char)
    fun text(value: CharSequence, start: Int = 0, end: Int = value.length)
    fun zeros(count: Long) {
        var remaining = count
        while (remaining > 0) {
            val block = minOf(remaining, ZERO_BLOCK537.length.toLong()).toInt()
            text(ZERO_BLOCK537, 0, block)
            remaining -= block
        }
    }
}

private val ZERO_BLOCK537 = "0".repeat(256)

internal class AppendableSink537(private val destination: Appendable) : NumericSink537 {
    override fun character(value: Char) { destination.append(value) }
    override fun text(value: CharSequence, start: Int, end: Int) { destination.append(value, start, end) }
}

internal class ArraySink537(private val destination: CharArray, private var offset: Int) : NumericSink537 {
    override fun character(value: Char) { destination[offset++] = value }
    override fun text(value: CharSequence, start: Int, end: Int) {
        for (index in start until end) destination[offset++] = value[index]
    }
}

/** Little-endian radix^width cells; only bounded per-cell Strings are materialized. */
internal class RadixDigits537(
    private val chunks: IntArray,
    private val size: Int,
    private val radix: Int,
    private val width: Int,
) {
    val zero: Boolean = size == 1 && chunks[0] == 0
    private val first = chunks[size - 1].toString(radix)
    val length: Long = (size - 1).toLong() * width + first.length

    fun write(sink: NumericSink537, start: Long = 0, end: Long = length) {
        require(start in 0..end && end <= length)
        var cursor = 0L
        for (index in size - 1 downTo 0) {
            val count = if (index == size - 1) first.length else width
            val after = cursor + count
            if (after > start && cursor < end) {
                val text = if (index == size - 1) first else chunks[index].toString(radix).padStart(width, '0')
                sink.text(text, maxOf(0L, start - cursor).toInt(), minOf(count.toLong(), end - cursor).toInt())
            }
            cursor = after
            if (cursor >= end) break
        }
    }
}

/** Plans all lengths in Long before the first external write or scale-sized padding. */
internal class DecimalLayout537(
    private val digits: RadixDigits537,
    private val scale: Int,
    private val negative: Boolean,
    format: DecimalTextFormat,
) {
    private val exponent = digits.length - scale.toLong() - 1
    private val plain = format == DecimalTextFormat.PLAIN || scale >= 0 && exponent >= -6
    private val engineering = !plain && format == DecimalTextFormat.ENGINEERING
    private val mod = ((exponent % 3 + 3) % 3).toInt()
    private val before = if (engineering) mod + 1 else 1
    private val zeroDecimals = if (engineering && digits.zero) (3 - mod) % 3 else 0
    private val outputExponent = if (engineering) exponent - mod +
        (if (digits.zero && mod != 0) 3 else 0) else exponent
    private val suffix = if (plain || engineering && outputExponent == 0L) "" else
        "E" + (if (outputExponent >= 0) "+" else "") + outputExponent
    val length: Long = (if (negative) 1L else 0L) + when {
        plain && scale < 0 && digits.zero -> 1L
        plain && scale <= 0 -> digits.length - scale.toLong()
        plain && scale < digits.length -> digits.length + 1
        plain -> scale.toLong() + 2
        engineering && digits.zero -> 1L + if (zeroDecimals == 0) 0 else zeroDecimals + 1
        else -> maxOf(digits.length, before.toLong()) + if (digits.length > before) 1 else 0
    } + suffix.length

    fun write(sink: NumericSink537) {
        if (negative) sink.character('-')
        when {
            plain && scale < 0 && digits.zero -> sink.character('0')
            plain && scale <= 0 -> { digits.write(sink); sink.zeros(-scale.toLong()) }
            plain && scale < digits.length -> {
                val split = digits.length - scale
                digits.write(sink, 0, split)
                sink.character('.')
                digits.write(sink, split)
            }
            plain -> {
                sink.text("0.")
                sink.zeros(scale.toLong() - digits.length)
                digits.write(sink)
            }
            engineering && digits.zero -> {
                sink.character('0')
                if (zeroDecimals != 0) { sink.character('.'); sink.zeros(zeroDecimals.toLong()) }
            }
            digits.length <= before -> { digits.write(sink); sink.zeros(before - digits.length) }
            else -> {
                digits.write(sink, 0, before.toLong())
                sink.character('.')
                digits.write(sink, before.toLong())
            }
        }
        sink.text(suffix)
    }
}

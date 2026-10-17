// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** A synchronous view; callers must keep mutable input coherent throughout parsing. */
internal class NumericText537 private constructor(
    private val text: CharSequence?,
    private val characters: CharArray?,
    private val offset: Int,
    val length: Int,
) {
    operator fun get(index: Int): Char = text?.get(offset + index) ?: characters!![offset + index]

    fun diagnostic(start: Int, end: Int): String = buildString(end - start) {
        for (index in start until end) append(this@NumericText537[index])
    }

    fun within(maxInputChars: Int): NumericText537 {
        require(maxInputChars >= 0) { "maxInputChars must be nonnegative" }
        if (length > maxInputChars) throw ArithmeticException("Input character limit exceeded")
        return this
    }

    companion object {
        fun of(value: CharSequence, offset: Int = 0, length: Int = value.length): NumericText537 {
            checkRange(value.length, offset, length)
            return NumericText537(value, null, offset, length)
        }

        fun of(value: CharArray, offset: Int, length: Int): NumericText537 {
            checkRange(value.size, offset, length)
            return NumericText537(null, value, offset, length)
        }

        private fun checkRange(size: Int, offset: Int, length: Int) {
            if (offset < 0 || length < 0 || offset > size - length)
                throw NumberFormatException("Bad character range")
        }
    }
}

/** ASCII avoids Unicode lookup; non-ASCII retains the existing Kotlin digit grammar. */
internal fun numericDigit537(character: Char, radix: Int): Int? {
    val ascii = when (character) {
        in '0'..'9' -> character.code - '0'.code
        in 'a'..'z' -> character.code - 'a'.code + 10
        in 'A'..'Z' -> character.code - 'A'.code + 10
        else -> return character.digitToIntOrNull(radix)
    }
    return ascii.takeIf { it < radix }
}

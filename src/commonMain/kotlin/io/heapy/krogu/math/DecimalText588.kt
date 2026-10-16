// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

// The required coefficient's decimal precision, including exact carry
// normalization. A precision alone is never a reason to reject the input.
internal fun retainedDecimalText588(digits: Long, precision: Int): Long {
    require(digits >= 0 && precision >= 0)
    return if (precision == 0) digits else minOf(digits, precision.toLong())
}

/** Full lexical validation, private prefix rounding, then public normalization. */
internal object DecimalText588 {
    private const val MAX_DIGITS = 646456993L
    private const val BASE = 1_000_000_000
    private const val PAGE = 8192
    private val powers = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)

    fun parse(value: String, context: MathContext): BigDecimal = parse(NumericText537.of(value), context)

    fun parse(value: CharSequence, offset: Int, length: Int, context: MathContext): BigDecimal =
        parse(NumericText537.of(value, offset, length), context)

    fun parseBounded(value: CharSequence, offset: Int, length: Int, maxInputChars: Int,
        context: MathContext): BigDecimal = parse(NumericText537.of(value, offset, length).within(maxInputChars), context)

    fun parseBounded(value: CharArray, offset: Int, length: Int, maxInputChars: Int,
        context: MathContext): BigDecimal = parse(NumericText537.of(value, offset, length).within(maxInputChars), context)

    fun parse(value: CharArray, offset: Int, length: Int, context: MathContext): BigDecimal {
        // No whole String or slice copy. Concurrently changing input is unsupported;
        // this synchronous reader does not promise an atomic snapshot.
        return parse(NumericText537.of(value, offset, length), context)
    }

    private class Lexical(val sign: Int, val scale: Int, val first: Int,
        val digits: Long, val guard: Int, val sticky: Boolean)

    private fun scan(input: NumericText537, precision: Int): Lexical {
        if (input.length == 0) throw NumberFormatException("Empty BigDecimal")
        var i = 0
        var sign = 1
        if (input[i] == '-' || input[i] == '+') { if (input[i] == '-') sign = -1; i++ }
        var seenDigit = false
        var point = false
        var fraction = 0L
        var first = -1
        var digits = 0L
        var guard = 0
        var sticky = false
        val guardOrdinal = precision.toLong() + 1L
        while (i < input.length && input[i] != 'e' && input[i] != 'E') {
            val at = i
            val c = input[i++]
            if (c == '.' && !point) { point = true; continue }
            val digit = numericDigit537(c, 10) ?: throw NumberFormatException("Invalid decimal digit")
            seenDigit = true
            if (point) fraction++
            if (first < 0 && digit != 0) first = at
            if (first >= 0) {
                digits++
                if (precision > 0) {
                    if (digits == guardOrdinal) guard = digit
                    else if (digits > guardOrdinal && digit != 0) sticky = true
                }
            }
        }
        if (!seenDigit) throw NumberFormatException("No digits")
        var exponent = 0L
        if (i < input.length) {
            i++
            var exponentSign = 1
            if (i < input.length && (input[i] == '+' || input[i] == '-')) {
                if (input[i] == '-') exponentSign = -1
                i++
            }
            if (i == input.length) throw NumberFormatException("No exponent digits")
            while (i < input.length) {
                val digit = numericDigit537(input[i++], 10) ?: throw NumberFormatException("Invalid exponent")
                // Before a step exponent<=L+1+fraction<=2^32-1;
                // the provisional next value is <2^36, safely Long.
                exponent = exponent * 10 + digit
                if (exponent > Int.MAX_VALUE.toLong() + 1 + fraction)
                    throw NumberFormatException("Exponent overflow")
            }
            exponent *= exponentSign
        }
        val scale = fraction - exponent
        if (scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
            throw NumberFormatException("Scale out of range")
        return Lexical(sign, scale.toInt(), first, digits, guard, sticky)
    }

    private fun parse(input: NumericText537, context: MathContext): BigDecimal {
        val lexical = scan(input, context.precision)
        if (lexical.digits == 0L) return BigDecimal(BigInteger.ZERO, lexical.scale)
        val kept = retainedDecimalText588(lexical.digits, context.precision)
        if (kept > MAX_DIGITS) overflow()
        if (context.precision == 0)
            return BigDecimal(BigInteger.decimalPrefix537(input, lexical.first, kept.toInt(), lexical.sign), lexical.scale)
        val drop = lexical.digits - kept
        // Preserve ordinary scale-before-inexactness priority. No endpoint
        // padding or optional zero stripping is part of constructor rounding.
        var scale = roundingScale(lexical.scale.toLong() - drop)
        val inexact = lexical.guard != 0 || lexical.sticky
        if (drop > 0 && inexact && context.roundingMode == RoundingMode.UNNECESSARY)
            throw ArithmeticException("Rounding necessary")
        val prefix = prefix(input, lexical.first, kept.toInt())
        if (drop > 0 && inexact) {
            val half = if (lexical.guard == 5 && lexical.sticky) 1 else lexical.guard.compareTo(5)
            val increment = when (context.roundingMode) {
                RoundingMode.UP -> true
                RoundingMode.DOWN -> false
                RoundingMode.CEILING -> lexical.sign > 0
                RoundingMode.FLOOR -> lexical.sign < 0
                RoundingMode.HALF_UP -> half >= 0
                RoundingMode.HALF_DOWN -> half > 0
                RoundingMode.HALF_EVEN -> half > 0 || half == 0 && prefix[0] % 2 != 0
                RoundingMode.UNNECESSARY -> false // handled before allocation
            }
            if (increment) prefix.increment()
        }
        // Even an H-digit prefix can exceed the public bit limit. Round and
        // divide a carry EXACTLY before the first public support check.
        if (prefix.digits() > kept) {
            check(prefix.divideTen() == 0)
            scale = roundingScale(scale.toLong() - 1)
        }
        return BigDecimal(publish(prefix, lexical.sign), scale)
    }

    private fun roundingScale(scale: Long): Int {
        val narrowed = scale.toInt()
        if (narrowed.toLong() != scale)
            throw ArithmeticException(if (narrowed > 0) "Underflow" else "Overflow")
        return narrowed
    }

    private fun overflow(): Nothing = throw ArithmeticException("BigInteger overflow")

    private fun prefix(input: NumericText537, first: Int, kept: Int): Digits {
        val result = Digits(((kept.toLong() + 8) / 9 + 1).toInt())
        var i = first
        var consumed = 0
        var block = 0
        var inBlock = 0
        while (consumed < kept) {
            val c = input[i++]
            if (c == '.') continue
            // Same immutable reader, already completely validated by scan.
            block = block * 10 + checkNotNull(numericDigit537(c, 10))
            consumed++
            inBlock++
            if (inBlock == 9) {
                result[result.size++] = block
                block = 0
                inBlock = 0
            }
        }
        var low = 0
        var high = result.size - 1
        while (low < high) {
            val cell = result[low]
            result[low++] = result[high]
            result[high--] = cell
        }
        if (inBlock > 0) {
            val factor = powers[inBlock]
            var carry = block.toLong()
            for (at in 0 until result.size) {
                val part = result[at].toLong() * factor + carry
                result[at] = (part % BASE).toInt()
                carry = part / BASE
            }
            if (carry != 0L) result[result.size++] = carry.toInt()
        }
        check(result.size > 0 && result[result.size - 1] != 0)
        return result
    }

    private class Cells(private val capacity: Int) {
        private val pages = arrayOfNulls<IntArray>(((capacity.toLong() + PAGE - 1) / PAGE).toInt())
        operator fun get(index: Int): Int {
            check(index >= 0 && index < capacity)
            return pages[index / PAGE]?.get(index % PAGE) ?: 0
        }
        operator fun set(index: Int, value: Int) {
            check(index >= 0 && index < capacity)
            val slot = index / PAGE
            var page = pages[slot]
            if (page == null) {
                if (value == 0) return
                page = IntArray(minOf(PAGE, capacity - slot * PAGE))
                pages[slot] = page
            }
            page[index % PAGE] = value
        }
    }

    private class Digits(capacity: Int) {
        private val cells = Cells(capacity)
        var size = 0
        operator fun get(index: Int): Int = cells[index]
        operator fun set(index: Int, value: Int) { cells[index] = value }
        fun digits(): Long {
            var top = cells[size - 1]
            var count = 0
            do { count++; top /= 10 } while (top != 0)
            return 9L * (size - 1) + count
        }
        fun increment() {
            var i = 0
            while (i < size && cells[i] == BASE - 1) { cells[i] = 0; i++ }
            if (i == size) { cells[i] = 1; size++ }
            else cells[i] = cells[i] + 1
        }
        fun divideTen(): Int {
            var remainder = 0L
            for (i in size - 1 downTo 0) {
                val part = remainder * BASE + cells[i]
                cells[i] = (part / 10).toInt()
                remainder = part % 10
            }
            while (size > 0 && cells[size - 1] == 0) size--
            check(size > 0)
            return remainder.toInt()
        }
    }

    private fun publish(decimal: Digits, sign: Int): BigInteger {
        // Candidate<=10^H<2^(L+1). One extra binary cell is conservative;
        // all bit counts stay Long until the final <=L support check.
        val bound = minOf(Int.MAX_VALUE.toLong(), 4L * decimal.digits())
        val binary = Cells(((bound + 29) / 30 + 1).toInt())
        var size = 0
        for (i in decimal.size - 1 downTo 0) {
            var carry = decimal[i].toLong()
            var j = 0
            while (j < size) {
                val part = binary[j].toLong() * BASE + carry
                binary[j] = (part and 0x3fffffff).toInt()
                carry = part ushr 30
                j++
            }
            if (carry != 0L) binary[size++] = carry.toInt()
            val bits = 30L * (size - 1) + 32 - binary[size - 1].countLeadingZeroBits()
            if (bits > Int.MAX_VALUE) overflow()
        }
        val bits = 30L * (size - 1) + 32 - binary[size - 1].countLeadingZeroBits()
        val bytes = ByteArray(((bits + 7) / 8).toInt())
        for (i in bytes.indices) {
            val bit = 8L * i
            val word = (bit / 30).toInt()
            val offset = (bit % 30).toInt()
            var value = binary[word].toLong() ushr offset
            if (offset > 22 && word + 1 < size)
                value = value or (binary[word + 1].toLong() shl (30 - offset))
            bytes[bytes.lastIndex - i] = (value and 255).toByte()
        }
        return BigInteger(sign, bytes)
    }
}

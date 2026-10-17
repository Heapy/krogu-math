// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Expanded private working domain. Signed radix digits avoid borrow-filled zero gaps. */
internal class DecimalSignedWork535(private var scale: Long) {
    private var pages = mutableMapOf<Int, IntArray>()

    fun copy(): DecimalSignedWork535 = DecimalSignedWork535(scale).also { result ->
        for ((page, words) in pages) result.pages[page] = words.copyOf()
    }

    fun estimatedAlignedDigits(otherScale: Long, otherDigits: Long): Long {
        var top = -1L
        cells { index, _ -> top = index }
        val target = maxOf(scale, otherScale)
        return maxOf(if (top < 0) 1L else (top + 1) * 9 + target - scale,
            otherDigits + target - otherScale) + 1
    }

    fun add(value: BigDecimal) {
        val parts = parts(value.unscaledValue())
        align(value.scale().toLong())
        val shift = scale - value.scale()
        val factor = POWERS[(shift % 9).toInt()].toLong()
        val base = shift / 9
        for (i in parts.words.indices) addCell(base + i, parts.sign * parts.words[i].toLong() * factor)
        pruneZeros()
    }

    fun addProduct(left: BigDecimal, right: BigDecimal) {
        val productScale = left.scale().toLong() + right.scale()
        align(productScale)
        if (left.signum() == 0 || right.signum() == 0) return
        val a = parts(left.unscaledValue())
        val b = parts(right.unscaledValue())
        val shift = scale - productScale
        val factor = POWERS[(shift % 9).toInt()].toLong()
        val base = shift / 9
        val sign = a.sign * b.sign
        for (i in a.words.indices) for (j in b.words.indices) {
            val product = a.words[i].toLong() * b.words[j]
            // Split BEFORE decimal alignment: product*10^8 would overflow Long.
            addCell(base + i + j, sign * (product % BASE) * factor)
            addCell(base + i + j + 1, sign * (product / BASE) * factor)
        }
        pruneZeros()
    }

    private fun align(incomingScale: Long) {
        if (incomingScale <= scale) return
        val distance = incomingScale - scale
        val factor = POWERS[(distance % 9).toInt()].toLong()
        val offset = distance / 9
        val shifted = DecimalSignedWork535(incomingScale)
        cells { index, value -> shifted.addCell(index + offset, value * factor) }
        pages = shifted.pages
        scale = incomingScale
    }

    private fun addCell(start: Long, contribution: Long) {
        var index = start
        var carry = contribution
        while (carry != 0L) {
            // Supported input scales/digits and a Long term counter imply index < 1.1e9.
            check(index in 0L until Int.MAX_VALUE.toLong()) { "Expanded decimal index invariant" }
            val page = (index / PAGE).toInt()
            val offset = (index % PAGE).toInt()
            val sum = (pages[page]?.get(offset) ?: 0).toLong() + carry
            val digit = (sum % BASE).toInt()
            if (digit != 0 || pages.containsKey(page)) pages.getOrPut(page) { IntArray(PAGE) }[offset] = digit
            carry = sum / BASE
            index++
        }
    }

    private fun pruneZeros() { pages.entries.removeAll { (_, words) -> words.all { it == 0 } } }

    private inline fun cells(action: (Long, Int) -> Unit) {
        for (page in pages.keys.sorted()) {
            val words = pages.getValue(page)
            for (offset in words.indices) if (words[offset] != 0)
                action(page.toLong() * PAGE + offset, words[offset])
        }
    }

    private data class Run(val start: Long, var end: Long, val digit: Int)

    fun result(context: MathContext): BigDecimal {
        var sign = 0
        cells { _, value -> sign = if (value < 0) -1 else 1 }
        if (sign == 0) return BigDecimal.ZERO
        val runs = mutableListOf<Run>()
        fun append(start: Long, end: Long, value: Int) {
            if (value == 0 || start == end) return
            val last = runs.lastOrNull()
            if (last != null && last.end == start && last.digit == value) last.end = end
            else runs += Run(start, end, value)
        }
        var cursor = 0L
        var borrow = 0
        cells { index, signed ->
            if (borrow != 0 && index > cursor) append(cursor, index, BASE - 1)
            val value = sign * signed + borrow
            if (value < 0) { append(index, index + 1, value + BASE); borrow = -1 }
            else { append(index, index + 1, value); borrow = 0 }
            cursor = index + 1
        }
        check(borrow == 0 && runs.isNotEmpty())
        fun runAt(index: Long): Run? {
            var low = 0
            var high = runs.lastIndex
            while (low <= high) {
                val middle = (low + high) ushr 1
                val run = runs[middle]
                if (index < run.start) high = middle - 1
                else if (index >= run.end) low = middle + 1
                else return run
            }
            return null
        }
        fun digitAt(position: Long): Int = (runAt(position / 9)?.digit ?: 0) / POWERS[(position % 9).toInt()] % 10
        val last = runs.last()
        var topDigits = 1
        while (topDigits < 9 && last.digit >= POWERS[topDigits]) topDigits++
        val digits = (last.end - 1) * 9 + topDigits
        val drop = if (context.precision == 0) 0L else maxOf(0L, digits - context.precision)
        val increment = if (drop == 0L) false else {
            val position = drop - 1
            val guard = digitAt(position)
            val cell = position / 9
            val sticky = runs.first().start < cell ||
                (runAt(cell)?.digit ?: 0) % POWERS[(position % 9).toInt()] != 0
            val inexact = guard != 0 || sticky
            val half = if (guard == 5) { if (sticky) 1 else 0 } else guard.compareTo(5)
            inexact && when (context.roundingMode) {
                RoundingMode.UP -> true
                RoundingMode.DOWN -> false
                RoundingMode.CEILING -> sign > 0
                RoundingMode.FLOOR -> sign < 0
                RoundingMode.HALF_UP -> half >= 0
                RoundingMode.HALF_DOWN -> half > 0
                RoundingMode.HALF_EVEN -> half > 0 || half == 0 && digitAt(drop) % 2 != 0
                RoundingMode.UNNECESSARY -> throw ArithmeticException("Rounding necessary")
            }
        }
        fun trailingNines(): Long {
            var position = drop
            while (position < digits && digitAt(position) == 9) {
                val run = runAt(position / 9)!!
                position = if (position % 9 == 0L && run.digit == BASE - 1)
                    minOf(digits, run.end * 9) else position + 1
            }
            return position - drop
        }
        val quotientDigits = digits - drop
        val nines = if (increment) trailingNines() else 0L
        val carry = increment && nines == quotientDigits
        val roundedDigits = quotientDigits + if (carry) 1 else 0
        val trailingZeros = if (increment) nines else {
            // Truncation can introduce quotient zeros absent in the original
            // coefficient (10001 / 10 -> 1000). Inspect retained digits.
            var position = drop
            while (position < digits && digitAt(position) == 0) {
                val cell = position / 9
                if (runAt(cell) == null) {
                    var low = 0
                    var high = runs.size
                    while (low < high) {
                        val middle = (low + high) ushr 1
                        if (runs[middle].start <= cell) low = middle + 1 else high = middle
                    }
                    position = minOf(digits, runs[low].start * 9)
                } else position++
            }
            position - drop
        }
        var trim = if (context.precision > 0) maxOf(0L, roundedDigits - context.precision) else 0L
        // Only exact zeros may rescue a preferred scale or an over-wide coefficient.
        trim = maxOf(trim, scale - drop - Int.MAX_VALUE, roundedDigits - MAX_DIGITS)
        if (trim > trailingZeros) throw ArithmeticException("Decimal result exceeds supported range")
        if (roundedDigits > MAX_DIGITS || scale - drop > Int.MAX_VALUE)
            trim = maxOf(trim, minOf(trailingZeros, maxOf(0L, scale - drop - Int.MIN_VALUE)))
        var resultScale = scale - drop - trim
        val appendZeros = maxOf(0L, Int.MIN_VALUE.toLong() - resultScale)
        if (roundedDigits - trim + appendZeros > MAX_DIGITS)
            throw ArithmeticException("BigInteger overflow")
        val binary = BinaryMagnitude(roundedDigits - trim + appendZeros)
        var position = digits - 1
        while (position >= drop + trim) {
            val count = minOf(9L, position - drop - trim + 1).toInt()
            var chunk = 0
            repeat(count) { chunk = chunk * 10 + digitAt(position--) }
            binary.multiplyAdd(if (count == 9) BASE else POWERS[count], chunk)
        }
        if (increment) binary.multiplyAdd(1, 1)
        repeat((appendZeros / 9).toInt()) { binary.multiplyAdd(BASE, 0) }
        if (appendZeros % 9 != 0L) binary.multiplyAdd(POWERS[(appendZeros % 9).toInt()], 0)
        resultScale += appendZeros
        check(resultScale in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
        if (binary.bits() > Int.MAX_VALUE) {
            // H decimal digits is necessary, not sufficient, for MAX supported bits.
            // Try an equivalent exact cohort before treating the final value as out of range.
            val rescue = minOf(trailingZeros - trim + appendZeros, resultScale - Int.MIN_VALUE)
            if (rescue <= 0) throw ArithmeticException("BigInteger overflow")
            repeat((rescue / 9).toInt()) { check(binary.divideSmall(BASE) == 0) }
            if (rescue % 9 != 0L) check(binary.divideSmall(POWERS[(rescue % 9).toInt()]) == 0)
            resultScale -= rescue
        }
        return BigDecimal(binary.publish(sign), resultScale.toInt())
    }

    private data class Parts(val sign: Int, val words: IntArray)
    private fun parts(value: BigInteger): Parts {
        val sign = value.signum()
        val bytes = value.abs().toByteArray()
        // 10^9 > 2^29: this byte-derived capacity bounds the decimal cell count.
        val words = IntArray((bytes.size.toLong() * 8 / 29 + 1).toInt())
        var size = 0
        for (byte in bytes) {
            var carry = (byte.toInt() and 255).toLong()
            for (i in 0 until size) {
                val part = words[i].toLong() * 256 + carry
                words[i] = (part % BASE).toInt()
                carry = part / BASE
            }
            if (carry != 0L) words[size++] = carry.toInt()
        }
        return Parts(sign, words.copyOf(size))
    }

    /** Publication storage is bounded by final digits, never by an alignment span. */
    private class BinaryMagnitude(decimalDigits: Long) {
        // 10 < 2^4, with two extra carry cells. Even H digits fit an Int index.
        private val words = IntArray(((4 * decimalDigits + 29) / 30 + 2).toInt())
        private var size = 0
        fun multiplyAdd(multiplier: Int, addition: Int) {
            var carry = addition.toLong()
            for (i in 0 until size) {
                val part = words[i].toLong() * multiplier + carry
                words[i] = (part and MASK).toInt()
                carry = part ushr 30
            }
            if (carry != 0L) words[size++] = carry.toInt()
        }
        fun divideSmall(divisor: Int): Int {
            var remainder = 0L
            for (i in size - 1 downTo 0) {
                val part = (remainder shl 30) + words[i]
                words[i] = (part / divisor).toInt()
                remainder = part % divisor
            }
            while (size > 0 && words[size - 1] == 0) size--
            return remainder.toInt()
        }
        fun bits(): Long = if (size == 0) 0 else 30L * (size - 1) + 32 - words[size - 1].countLeadingZeroBits()
        fun publish(sign: Int): BigInteger {
            val bits = bits()
            if (bits > Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
            if (bits == 0L) return BigInteger.ZERO
            val bytes = ByteArray(((bits + 7) / 8).toInt())
            for (i in bytes.indices) {
                val bit = 8L * i
                val word = (bit / 30).toInt()
                val offset = (bit % 30).toInt()
                var value = words[word].toLong() ushr offset
                if (offset > 22 && word + 1 < size) value = value or (words[word + 1].toLong() shl (30 - offset))
                bytes[bytes.lastIndex - i] = (value and 255).toByte()
            }
            return BigInteger(sign, bytes)
        }
    }

    companion object {
        private const val BASE = 1_000_000_000
        private const val PAGE = 256
        private const val MAX_DIGITS = 646456993L
        private const val MASK = (1L shl 30) - 1
        private val POWERS = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)
    }
}

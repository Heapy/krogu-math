// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

// Keep the sufficient old-path proof independent of the raw object's buffers.
internal fun materializedDecimalDivision581(leftBits: Int, rightBits: Int, precision: Int,
    preferred: Long, target: Long, shift: Long): Boolean {
    if (leftBits <= 0 || rightBits <= 0 || precision <= 0 ||
        preferred !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() ||
        target !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() ||
        target == Int.MIN_VALUE.toLong() ||
        shift !in -646456992L..646456992L) return false
    val numeratorBits = leftBits.toLong() + if (shift > 0) 4L * shift else 0L
    val denominatorBits = rightBits.toLong() + if (shift < 0) -4L * shift else 0L
    return numeratorBits <= Int.MAX_VALUE && denominatorBits <= Int.MAX_VALUE &&
        4L * precision + rightBits <= Int.MAX_VALUE
}

/** Unscaled quotient digits; no public normalized numerator/denominator. */
internal object DecimalDivision581 {
    private const val BASE = 1_000_000_000
    private const val PAGE = 8192
    private const val MAX_DIGITS = 646456993L
    private val powers = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)

    /** The bridge supplies fresh positive base-class wrappers, never subtype receivers. */
    fun largePrecision(sign: Int, left: BigInteger, right: BigInteger, preferred: Long,
        precision: Int): Pair<BigInteger, Int> {
        require(precision.toLong() > MAX_DIGITS)
        val gcd = left.gcd(right)
        var numerator = left.divide(gcd)
        var denominator = right.divide(gcd)
        val twos = denominator.getLowestSetBit()
        denominator = denominator.shiftRight(twos)
        val five = BigInteger.valueOf(5)
        var fives = 0
        while (true) {
            val qr = denominator.divideAndRemainder(five)
            if (qr[1].signum() != 0) break
            denominator = qr[0]
            fives++
        }
        // A nonterminating result requires p digits, more than any supported
        // coefficient. Exact small results must be distinguished first.
        if (denominator != BigInteger.ONE)
            throw ArithmeticException("BigInteger would overflow supported range")
        val places = maxOf(twos, fives)
        val scale = checkedScale(preferred + places)
        if (places > twos) numerator = numerator.shiftLeft(places - twos)
        if (places > fives) numerator = numerator.multiply(five.pow(places - fives))
        return (if (sign < 0) numerator.negate() else numerator) to scale
    }

    /** Canonical nonzero supported base-2^30 inputs stay read-only throughout. */
    fun rounded(sign: Int, left: IntArray, right: IntArray, preferred: Long,
        context: MathContext): Pair<BigInteger, Int> {
        require(sign == -1 || sign == 1)
        require(left.isNotEmpty() && right.isNotEmpty() && context.precision > 0)
        require(preferred in -4294967295L..4294967295L)
        val scratch = Cells(maxOf(left.size, right.size))
        val a = decimalChunks(left, scratch)
        val b = decimalChunks(right, scratch)
        val inputDigits = a.digits()
        val lastNonzero = inputDigits - a.trailingZeros()
        val remainder = DecimalWords(b.size + 1)
        val trial = DecimalWords(b.size + 1)
        val precision = context.precision.toLong()
        val quotient = DecimalWords(((precision + 1 + 8) / 9 + 1).toInt())
        var position = 0L
        var kept = 0L
        var exponent = 0L
        while (true) {
            val input = if (position < inputDigits) a.digitFromLeft(position, inputDigits) else 0
            position++
            remainder.multiplyAdd(10, input)
            var digit = 9
            while (digit > 0) {
                trial.product(b, digit)
                if (remainder.compare(trial) >= 0) break
                digit--
            }
            if (digit != 0) remainder.subtract(trial)
            // R alone is insufficient while an original nonzero digit is unread.
            val exact = remainder.size == 0 && position >= lastNonzero
            if (kept == 0L && digit == 0) {
                check(!exact)
                continue
            }
            if (kept == 0L) exponent = inputDigits - position
            if (kept < precision) {
                quotient.multiplyAdd(10, digit)
                kept++
                if (exact) return exactResult(quotient, sign, preferred, exponent, kept, precision)
            } else {
                if (digit == 0 && exact)
                    return exactResult(quotient, sign, preferred, exponent, kept, precision)
                val half = if (digit == 5 && !exact) 1 else digit.compareTo(5)
                if (increment(context.roundingMode, sign, half, quotient.odd())) quotient.increment()
                var scale = preferred + precision - 1 - exponent
                if (quotient.digits() > precision) {
                    check(quotient.divideTen() == 0)
                    scale--
                }
                return publish(quotient, sign, checkedScale(scale))
            }
        }
    }

    /** Exact integer quotient for negative in-range S; trailing zeros stay symbolic. */
    fun integral(sign: Int, left: IntArray, right: IntArray, preferred: Int): Pair<BigInteger, Int> {
        require(sign == -1 || sign == 1)
        require(left.isNotEmpty() && right.isNotEmpty() && preferred < 0)
        val scratch = Cells(maxOf(left.size, right.size))
        val a = decimalChunks(left, scratch)
        val b = decimalChunks(right, scratch)
        val inputDigits = a.digits()
        val lastNonzero = inputDigits - a.trailingZeros()
        val limit = inputDigits - preferred.toLong()
        val remainder = DecimalWords(b.size + 1)
        val trial = DecimalWords(b.size + 1)
        val quotientDigits = maxOf(1L, minOf(MAX_DIGITS, limit - b.digits() + 1))
        val quotient = DecimalWords(((quotientDigits + 8) / 9).toInt())
        var position = 0L
        var zeros = 0L
        while (position < limit) {
            val input = if (position < inputDigits) a.digitFromLeft(position, inputDigits) else 0
            position++
            remainder.multiplyAdd(10, input)
            var digit = 9
            while (digit > 0) {
                trial.product(b, digit)
                if (remainder.compare(trial) >= 0) break
                digit--
            }
            if (digit != 0) remainder.subtract(trial)
            if (digit == 0) {
                if (quotient.size != 0) zeros++
            } else {
                val digits = if (quotient.size == 0) 1L else quotient.digits() + zeros + 1
                if (digits > MAX_DIGITS) throw ArithmeticException("BigInteger overflow")
                if (quotient.size != 0) quotient.appendZeroDigits(zeros)
                quotient.multiplyAdd(10, digit)
                zeros = 0
            }
            // An unread original nonzero digit prevents the exact zero-tail skip.
            if (remainder.size == 0 && position >= lastNonzero) {
                if (quotient.size == 0) return BigInteger.ZERO to preferred
                zeros += limit - position
                break
            }
        }
        if (quotient.size == 0) return BigInteger.ZERO to preferred
        val scale = maxOf(-zeros, preferred.toLong())
        val padding = zeros + scale
        if (quotient.digits() + padding > MAX_DIGITS) throw ArithmeticException("BigInteger overflow")
        quotient.appendZeroDigits(padding)
        return publish(quotient, sign, scale.toInt())
    }

    private fun exactResult(q: DecimalWords, sign: Int, preferred: Long,
        exponent: Long, kept: Long, precision: Long): Pair<BigInteger, Int> {
        var scale = preferred + kept - 1 - exponent
        while (q.endsInZero()) { check(q.divideTen() == 0); scale-- }
        val padding = minOf(maxOf(preferred - scale, 0L), precision - q.digits())
        val resultScale = checkedScale(scale + padding)
        var remaining = padding
        while (remaining > 0) { q.multiplyAdd(10, 0); remaining-- }
        return publish(q, sign, resultScale)
    }

    private fun increment(mode: RoundingMode, sign: Int, half: Int, odd: Boolean): Boolean = when (mode) {
        RoundingMode.UP -> true
        RoundingMode.DOWN -> false
        RoundingMode.CEILING -> sign > 0
        RoundingMode.FLOOR -> sign < 0
        RoundingMode.HALF_UP -> half >= 0
        RoundingMode.HALF_DOWN -> half > 0
        RoundingMode.HALF_EVEN -> half > 0 || half == 0 && odd
        RoundingMode.UNNECESSARY -> throw ArithmeticException("Rounding necessary")
    }

    private fun checkedScale(scale: Long): Int {
        val narrowed = scale.toInt()
        if (narrowed.toLong() != scale)
            throw ArithmeticException(if (narrowed > 0) "Underflow" else "Overflow")
        return narrowed
    }

    private fun decimalChunks(words: IntArray, scratch: Cells): DecimalWords {
        for (i in words.indices) scratch[i] = words[i]
        var size = words.size
        val digitBound = minOf(MAX_DIGITS, 10L * words.size)
        val result = DecimalWords(((digitBound + 8) / 9).toInt())
        while (size > 0) {
            var remainder = 0L
            for (i in size - 1 downTo 0) {
                val part = (remainder shl 30) + scratch[i]
                scratch[i] = (part / BASE).toInt()
                remainder = part % BASE
            }
            result.append(remainder.toInt())
            while (size > 0 && scratch[size - 1] == 0) size--
        }
        check(result.size > 0)
        return result
    }

    private fun publish(decimal: DecimalWords, sign: Int, scale: Int): Pair<BigInteger, Int> {
        val bitBound = minOf(Int.MAX_VALUE.toLong(), 4L * decimal.digits())
        val binary = Cells(((bitBound + 29) / 30 + 1).toInt())
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
            if (bits > Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
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
        return BigInteger(sign, bytes) to scale
    }

    private class Cells(limit: Int) {
        private val capacity = limit
        private val pages = arrayOfNulls<IntArray>(((limit.toLong() + PAGE - 1) / PAGE).toInt())
        operator fun get(index: Int): Int {
            check(index >= 0 && index < capacity)
            return pages[index / PAGE]?.get(index % PAGE) ?: 0
        }
        operator fun set(index: Int, value: Int) {
            check(index >= 0 && index < capacity)
            val slot = index / PAGE
            val page = pages[slot] ?: IntArray(minOf(PAGE, capacity - slot * PAGE)).also { pages[slot] = it }
            page[index % PAGE] = value
        }
    }

    private class DecimalWords(capacity: Int) {
        private val cells = Cells(capacity)
        var size = 0
            private set
        operator fun get(index: Int): Int = cells[index]
        fun append(value: Int) { cells[size] = value; size++ }
        fun digits(): Long {
            check(size > 0)
            var width = 1
            val top = cells[size - 1]
            while (width < 9 && top >= powers[width]) width++
            return 9L * (size - 1) + width
        }
        fun trailingZeros(): Long {
            var index = 0
            while (cells[index] == 0) index++
            var value = cells[index]
            var zeros = 9L * index
            while (value % 10 == 0) { value /= 10; zeros++ }
            return zeros
        }
        fun digitFromLeft(position: Long, digits: Long): Int {
            val at = digits - 1 - position
            return cells[(at / 9).toInt()] / powers[(at % 9).toInt()] % 10
        }
        fun odd(): Boolean = cells[0] % 2 != 0
        fun endsInZero(): Boolean = cells[0] % 10 == 0
        fun multiplyAdd(factor: Int, addend: Int) {
            var carry = addend.toLong()
            for (i in 0 until size) {
                val part = cells[i].toLong() * factor + carry
                cells[i] = (part % BASE).toInt()
                carry = part / BASE
            }
            if (carry != 0L) append(carry.toInt())
            trim()
        }
        fun product(source: DecimalWords, factor: Int) {
            var carry = 0L
            for (i in 0 until source.size) {
                val part = source.cells[i].toLong() * factor + carry
                cells[i] = (part % BASE).toInt()
                carry = part / BASE
            }
            size = source.size
            if (carry != 0L) append(carry.toInt())
        }
        fun compare(other: DecimalWords): Int {
            if (size != other.size) return size.compareTo(other.size)
            for (i in size - 1 downTo 0) {
                val order = cells[i].compareTo(other.cells[i])
                if (order != 0) return order
            }
            return 0
        }
        fun subtract(other: DecimalWords) {
            var borrow = 0L
            for (i in 0 until size) {
                var part = cells[i].toLong() - (if (i < other.size) other.cells[i] else 0) - borrow
                borrow = if (part < 0) 1L else 0L
                if (part < 0) part += BASE
                cells[i] = part.toInt()
            }
            check(borrow == 0L)
            trim()
        }
        fun increment() {
            var i = 0
            while (i < size && cells[i] == BASE - 1) { cells[i] = 0; i++ }
            if (i == size) append(1) else cells[i] = cells[i] + 1
        }
        fun divideTen(): Int {
            var remainder = 0L
            for (i in size - 1 downTo 0) {
                val part = remainder * BASE + cells[i]
                cells[i] = (part / 10).toInt()
                remainder = part % 10
            }
            trim()
            return remainder.toInt()
        }
        // Callers bound the selected coefficient before any shift/allocation.
        fun appendZeroDigits(count: Long) {
            check(size > 0 && count >= 0 && digits() + count <= MAX_DIGITS)
            val whole = (count / 9).toInt()
            if (whole != 0) {
                for (i in size - 1 downTo 0) cells[i + whole] = cells[i]
                for (i in 0 until whole) cells[i] = 0
                size += whole
            }
            val rest = (count % 9).toInt()
            if (rest != 0) multiplyAdd(powers[rest], 0)
        }
        private fun trim() { while (size > 0 && cells[size - 1] == 0) size-- }
    }
}

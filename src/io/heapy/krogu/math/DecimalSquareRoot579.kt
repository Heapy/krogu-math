// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Private-workspace decimal sqrt. No provisional root is a public BigInteger. */
internal object DecimalSquareRoot579 {
    private const val BASE = 1_000_000_000
    private const val PAGE = 8192
    private val powers = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)

    // 10^n < 2^(4n); a deliberately conservative eligibility check only.
    fun canMaterialize(digits: Int, shift: Long, scale: Long): Boolean =
        shift in 1..646456992L && scale in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() &&
            4L * (digits.toLong() + shift) <= Int.MAX_VALUE

    // Only after a complete ordinary finite-root check: a required p-digit
    // coefficient with p>H is at least 10^H, beyond the public bit range.
    fun inexactPrecisionOverflows(precision: Int): Boolean = precision > 646456993

    fun normalized(coefficient: BigInteger, shift: Long, precision: Int,
        scale: Long, preferred: Int, mode: RoundingMode): Pair<BigInteger, Int> {
        require(shift > 0 && precision > 0)
        val capacity = ((precision.toLong() + 11) / 9 + 1).toInt()
        // Every quotient/remainder is bounded by the original input. The first
        // call can dispatch on a restored coefficient subtype (541 boundary).
        val input = decimalChunks(coefficient)
        val top = input[input.size - 1]
        var topDigits = 1
        while (topDigits < 9 && top >= powers[topDigits]) topDigits++
        val digits = 9L * (input.size - 1) + topDigits
        val total = digits + shift
        require((total + 1) / 2 == precision.toLong())
        var position = 0L
        fun digit(): Int {
            val at = position++
            if (at >= digits) return 0
            if (at < topDigits) return top / powers[topDigits - 1 - at.toInt()] % 10
            val tail = at - topDigits
            val chunk = input.size - 2 - (tail / 9).toInt()
            return input[chunk] / powers[8 - (tail % 9).toInt()] % 10
        }
        val root = DecimalWords(capacity)
        val remainder = DecimalWords(capacity)
        val trial = DecimalWords(capacity)
        var allNines = true
        var first = true
        while (position < total) {
            val pair = if (first && total % 2L != 0L) digit() else 10 * digit() + digit()
            first = false
            remainder.multiplyAdd(100, pair)
            var d = 9
            while (true) {
                trial.assign(root)
                trial.multiplyAdd(20, d)
                trial.multiplyAdd(d, 0)
                if (trial.compare(remainder) <= 0) break
                d-- // Trial zero is zero, so the loop always stops by zero.
            }
            remainder.subtract(trial)
            root.multiplyAdd(10, d)
            if (d != 9) allNines = false
        }
        val inexact = remainder.size != 0
        if (inexact && mode == RoundingMode.UNNECESSARY) throw ArithmeticException("Rounding necessary")
        val halfAbove = remainder.compare(root) > 0 // Equality is strictly BELOW half.
        val increment = inexact && when (mode) {
            RoundingMode.UP, RoundingMode.CEILING -> true
            RoundingMode.DOWN, RoundingMode.FLOOR -> false
            RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN -> halfAbove
            RoundingMode.UNNECESSARY -> false
        }
        var finalScale = scale
        if (increment) {
            root.multiplyAdd(1, 1)
            if (allNines) {
                check(root.divideTen() == 0)
                finalScale--
            }
        }
        // Exact internal roots retain their preferred-cohort/Long-scale rescue.
        // Inexact rounding retains all p significant digits, including zeros;
        // the sole all-nine carry normalization above already preserves p.
        while (!inexact && finalScale > preferred && root[0] % 10 == 0) {
            check(root.divideTen() == 0)
            finalScale--
        }
        if (finalScale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
            throw ArithmeticException(if (finalScale < 0) "Overflow" else "Underflow")
        return binaryCoefficient(root) to finalScale.toInt()
    }

    private fun decimalChunks(coefficient: BigInteger): DecimalWords {
        val chunks = DecimalWords(((646456993L + 8) / 9).toInt())
        val divisor = BigInteger.valueOf(BASE.toLong())
        var value = coefficient
        while (value.signum() != 0) {
            val qr = value.divideAndRemainder(divisor)
            chunks.append(qr[1].toInt())
            value = qr[0]
        }
        require(chunks.size != 0)
        return chunks
    }

    /** At most one extra binary word is formed, privately, at a range crossing. */
    private fun binaryCoefficient(decimal: DecimalWords): BigInteger {
        val binary = Cells(((Int.MAX_VALUE.toLong() + 29) / 30 + 1).toInt())
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
            if (size != 0) {
                val bits = 30L * (size - 1) + 32 - binary[size - 1].countLeadingZeroBits()
                if (bits > Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
            }
        }
        check(size != 0)
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
        return BigInteger(1, bytes)
    }

    /** Capacity is a logical limit, not an eager allocation. Pages never alias. */
    private class Cells(private val limit: Int) {
        private val pages = mutableListOf<IntArray>()
        operator fun get(index: Int): Int {
            check(index >= 0 && index < limit)
            return pages[index / PAGE][index % PAGE]
        }
        operator fun set(index: Int, value: Int) {
            check(index >= 0 && index < limit)
            val page = index / PAGE
            while (pages.size <= page) pages.add(IntArray(PAGE))
            pages[page][index % PAGE] = value
        }
    }

    private class DecimalWords(capacity: Int) {
        private val cells = Cells(capacity)
        var size = 0
            private set
        operator fun get(index: Int): Int = cells[index]
        fun append(value: Int) { cells[size] = value; size++ }
        fun assign(other: DecimalWords) {
            for (i in 0 until other.size) cells[i] = other[i]
            size = other.size
        }
        fun multiplyAdd(multiplier: Int, addition: Int) {
            var carry = addition.toLong()
            for (i in 0 until size) {
                val part = cells[i].toLong() * multiplier + carry
                cells[i] = (part % BASE).toInt()
                carry = part / BASE
            }
            while (carry != 0L) { append((carry % BASE).toInt()); carry /= BASE }
            trim()
        }
        fun compare(other: DecimalWords): Int {
            if (size != other.size) return size.compareTo(other.size)
            for (i in size - 1 downTo 0) {
                val compared = cells[i].compareTo(other[i])
                if (compared != 0) return compared
            }
            return 0
        }
        fun subtract(other: DecimalWords) {
            var borrow = 0
            for (i in 0 until size) {
                var value = cells[i] - (if (i < other.size) other[i] else 0) - borrow
                borrow = if (value < 0) 1 else 0
                if (value < 0) value += BASE
                cells[i] = value
            }
            check(borrow == 0)
            trim()
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
        private fun trim() { while (size > 0 && cells[size - 1] == 0) size-- }
    }
}

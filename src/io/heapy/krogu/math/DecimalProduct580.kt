// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

// Keep selection independent of the raw helper object's scratch constants.
internal fun needsRawDecimalProduct580(leftBits: Int, rightBits: Int): Boolean =
    leftBits > 0 && rightBits > 0 && leftBits.toLong() + rightBits > Int.MAX_VALUE

/** Full private decimal product, rounded before imposing public integer limits. */
internal object DecimalProduct580 {
    private const val BASE = 1_000_000_000
    private const val PAGE = 8192
    private val powers = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)

    /** Inputs are canonical base-2^30 magnitudes, read-only for this whole call. */
    fun rounded(sign: Int, left: IntArray, right: IntArray, scale: Long,
        context: MathContext): Pair<BigInteger, Int> {
        require(sign == -1 || sign == 1)
        require(left.isNotEmpty() && right.isNotEmpty())
        val precision = context.precision
        require(precision > 0)
        // Ordinary nonzero LOW scale remains an error before product work.
        if (scale < Int.MIN_VALUE) checkedScale(scale)
        val scratch = Cells(maxOf(left.size, right.size))
        val a = decimalChunks(left, scratch)
        val b = decimalChunks(right, scratch)
        val capacity = (a.size.toLong() + b.size + 1).toInt()
        val product = DecimalWords(capacity)
        product.zeroSize(capacity - 1)
        for (i in 0 until a.size) {
            var carry = 0L
            for (j in 0 until b.size) {
                val at = i + j
                val part = a[i].toLong() * b[j] + product[at] + carry
                product[at] = (part % BASE).toInt()
                carry = part / BASE
            }
            product[i + b.size] = carry.toInt()
        }
        product.trim()
        val digits = product.digits()
        if (scale > Int.MAX_VALUE && digits <= precision) checkedScale(scale)
        var drop = maxOf(digits.toLong() - precision, 0L)
        if (drop != 0L) {
            val guardPosition = drop - 1
            val guardCell = (guardPosition / 9).toInt()
            val offset = (guardPosition % 9).toInt()
            val guard = product[guardCell] / powers[offset] % 10
            var sticky = product[guardCell] % powers[offset] != 0
            var i = 0
            while (!sticky && i < guardCell) { sticky = product[i] != 0; i++ }
            val inexact = guard != 0 || sticky
            val half = if (guard == 5) { if (sticky) 1 else 0 } else guard.compareTo(5)
            product.dropDigits(drop.toInt())
            if (inexact && increment(context.roundingMode, sign, half, product[0] % 2 != 0)) {
                product.increment()
                if (product.digits() > precision) {
                    check(product.divideSmall(10) == 0)
                    drop++
                }
            }
        }
        // No optional zero stripping: preserve multiply/roundValue's required
        // cohort and566's HIGH scale rescue only through mandatory digit drops.
        val finalScale = checkedScale(scale - drop)
        return binaryCoefficient(product, sign) to finalScale
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
        if (scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
            throw ArithmeticException(if (scale < 0) "Overflow" else "Underflow")
        return scale.toInt()
    }

    private fun decimalChunks(words: IntArray, scratch: Cells): DecimalWords {
        for (i in words.indices) scratch[i] = words[i]
        var size = words.size
        val chunks = DecimalWords(((646456993L + 8) / 9).toInt())
        while (size != 0) {
            var remainder = 0L
            for (i in size - 1 downTo 0) {
                val part = (remainder shl 30) + scratch[i]
                scratch[i] = (part / BASE).toInt()
                remainder = part % BASE
            }
            chunks.append(remainder.toInt())
            while (size > 0 && scratch[size - 1] == 0) size--
        }
        check(chunks.size > 0)
        return chunks
    }

    private fun binaryCoefficient(decimal: DecimalWords, sign: Int): BigInteger {
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
        return BigInteger(sign, bytes)
    }

    /** Unwritten cells read as zero; pages and input magnitudes never alias. */
    private class Cells(private val limit: Int) {
        private val pages = mutableListOf<IntArray>()
        operator fun get(index: Int): Int {
            check(index >= 0 && index < limit)
            val page = index / PAGE
            return if (page < pages.size) pages[page][index % PAGE] else 0
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
        operator fun set(index: Int, value: Int) { cells[index] = value }
        fun zeroSize(length: Int) { check(size == 0); size = length }
        fun append(value: Int) { cells[size] = value; size++ }
        fun trim() { while (size > 0 && cells[size - 1] == 0) size-- }
        fun digits(): Int {
            check(size > 0)
            var topDigits = 1
            val top = cells[size - 1]
            while (topDigits < 9 && top >= powers[topDigits]) topDigits++
            return (9L * (size - 1) + topDigits).toInt()
        }
        fun dropDigits(count: Int) {
            val whole = count / 9
            val length = size - whole
            // Low-to-high copies cannot overwrite an unread higher source cell.
            for (i in 0 until length) cells[i] = cells[i + whole]
            size = length
            divideSmall(powers[count % 9])
            trim()
        }
        fun divideSmall(divisor: Int): Int {
            var remainder = 0L
            for (i in size - 1 downTo 0) {
                val part = remainder * BASE + cells[i]
                cells[i] = (part / divisor).toInt()
                remainder = part % divisor
            }
            trim()
            return remainder.toInt()
        }
        fun increment() {
            var i = 0
            while (i < size && cells[i] == BASE - 1) { cells[i] = 0; i++ }
            if (i == size) append(1) else cells[i] = cells[i] + 1
        }
    }
}

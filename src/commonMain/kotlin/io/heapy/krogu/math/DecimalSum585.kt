// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

// Sufficient only. A false result selects certification, never an arithmetic error.
internal fun materializedDecimalSum585(leftSign: Int, rightSign: Int, leftBits: Int,
    rightBits: Int, leftScale: Int, rightScale: Int): Boolean {
    val scale = maxOf(leftScale, rightScale).toLong()
    val limit = if (leftSign != 0 && leftSign == rightSign) Int.MAX_VALUE - 1L else Int.MAX_VALUE.toLong()
    fun supported(sign: Int, bits: Int, sourceScale: Int): Boolean {
        if (sign == 0) return true
        val raise = scale - sourceScale
        return raise in 0L..646456992L && bits.toLong() + 4L * raise <= limit
    }
    return supported(leftSign, leftBits, leftScale) && supported(rightSign, rightBits, rightScale)
}

// No-rescue for exact/p>H; independent span certification for the selected finite
// close/post-sticky compositions. Failure always declines to the whole old route.
internal fun certifiedDecimalSumSpan585(leftDigits: Int, rightDigits: Int,
    leftScale: Int, rightScale: Int, precision: Int): Boolean {
    val scale = maxOf(leftScale, rightScale).toLong()
    val left = if (leftDigits == 0) 0L else leftDigits.toLong() + scale - leftScale
    val right = if (rightDigits == 0) 0L else rightDigits.toLong() + scale - rightScale
    val largest = maxOf(left, right)
    return if (precision == 0 || precision > 646456993) largest <= 646456994L
    else largest <= 1292913989L
}

/** Private decimal alignment, cancellation and one rounding; no public padded integer. */
internal object DecimalSum585 {
    private const val BASE = 1_000_000_000
    private const val PAGE = 8192
    private const val MAX_DIGITS = 646456993L
    private val powers = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)

    // Null retains the old composition: a span-certification decline, or a
    // proved required range/inexactness after certification. No callback,
    // allocation or invariant failure is caught or reclassified.
    fun sum(leftSign: Int, left: IntArray, leftScale: Int, rightSign: Int,
        right: IntArray, rightScale: Int, context: MathContext?): Pair<BigInteger, Int>? {
        require(leftSign in -1..1 && rightSign in -1..1)
        require((leftSign == 0) == left.isEmpty() && (rightSign == 0) == right.isEmpty())
        val scale = maxOf(leftScale, rightScale)
        val precision = context?.precision ?: 0
        val scratch = Cells(maxOf(left.size, right.size))
        val a = decimal(left, scratch)
        val b = decimal(right, scratch)
        val da = if (a.size == 0) 0 else a.digits().toInt()
        val db = if (b.size == 0) 0 else b.digits().toInt()
        if (!certifiedDecimalSumSpan585(da, db, leftScale, rightScale, precision)) return null
        val av = aligned(a, scale.toLong() - leftScale)
        val bv = aligned(b, scale.toLong() - rightScale)
        val order = compare(av, bv)
        if (leftSign != rightSign && order == 0 || leftSign == 0 && rightSign == 0)
            return BigInteger.ZERO to scale
        val subtract = leftSign != 0 && rightSign != 0 && leftSign != rightSign
        val high = if (subtract && order < 0) bv else av
        val low = if (subtract && order < 0) av else bv
        val sign = if (leftSign == 0 || subtract && order < 0) rightSign else leftSign
        val end = maxOf(high.end, low.end)
        // The selected span is <=2H+3 digits. One extra chunk holds sum carry.
        val result = Digits((end + 1L).toInt())
        var at = 0L
        var carry = 0
        while (at < end) {
            if (!high.contains(at) && !low.contains(at)) {
                val next = minOf(high.next(at, end), low.next(at, end))
                check(next > at)
                if (subtract) {
                    if (carry != 0) result.fill(at.toInt(), next.toInt(), BASE - 1)
                } else if (carry != 0) {
                    result[at.toInt()] = 1
                    carry = 0
                }
                at = next
                continue
            }
            val h = high[at]
            val l = low[at]
            if (subtract) {
                val part = h.toLong() - l - carry
                result[at.toInt()] = (if (part < 0) part + BASE else part).toInt()
                carry = if (part < 0) 1 else 0
            } else {
                val part = h.toLong() + l + carry
                result[at.toInt()] = (part % BASE).toInt()
                carry = (part / BASE).toInt()
            }
            at++
        }
        if (subtract) check(carry == 0)
        else if (carry != 0) result[end.toInt()] = carry
        result.size = end.toInt() + if (!subtract && carry != 0) 1 else 0
        result.trim()
        if (result.size == 0) return BigInteger.ZERO to scale
        val digits = result.digits()
        if (precision == 0 || digits <= precision) return publish(result, sign, scale)
        if (precision.toLong() > MAX_DIGITS) return null
        val drop = digits - precision
        val whole = (drop / 9).toInt()
        val tail = (drop % 9).toInt()
        val guardCell = if (tail == 0) whole - 1 else whole
        val guardPlace = if (tail == 0) 8 else tail - 1
        val guard = result[guardCell] / powers[guardPlace] % 10
        val sticky = result[guardCell] % powers[guardPlace] != 0 || result.anyBelow(guardCell)
        val inexact = guard != 0 || sticky
        val mode = checkNotNull(context).roundingMode
        if (inexact && mode == RoundingMode.UNNECESSARY) return null
        val quotient = result.drop(whole, powers[tail])
        val half = if (guard == 5 && sticky) 1 else guard.compareTo(5)
        val increment = inexact && when (mode) {
            RoundingMode.UP -> true
            RoundingMode.DOWN -> false
            RoundingMode.CEILING -> sign > 0
            RoundingMode.FLOOR -> sign < 0
            RoundingMode.HALF_UP -> half >= 0
            RoundingMode.HALF_DOWN -> half > 0
            RoundingMode.HALF_EVEN -> half > 0 || half == 0 && quotient[0] % 2 != 0
            RoundingMode.UNNECESSARY -> false
        }
        if (increment) quotient.increment()
        var finalScale = scale.toLong() - drop
        if (quotient.digits() > precision) {
            check(quotient.divideTen() == 0)
            finalScale--
        }
        if (finalScale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) return null
        return publish(quotient, sign, finalScale.toInt())
    }

    private fun decimal(words: IntArray, scratch: Cells): Digits {
        if (words.isEmpty()) return Digits(0)
        for (i in words.indices) scratch[i] = words[i]
        var size = words.size
        val bound = minOf(MAX_DIGITS, 10L * words.size)
        val result = Digits(((bound + 8) / 9 + 1).toInt())
        while (size > 0) {
            var remainder = 0L
            for (i in size - 1 downTo 0) {
                val part = (remainder shl 30) + scratch[i]
                scratch[i] = (part / BASE).toInt()
                remainder = part % BASE
            }
            result[result.size] = remainder.toInt()
            // Zero low chunks count too; assignment alone deliberately does
            // not infer active size from value.
            result.size++
            while (size > 0 && scratch[size - 1] == 0) size--
        }
        return result
    }

    private fun aligned(value: Digits, raise: Long): View {
        if (value.size == 0) return View(value, 0)
        val factor = powers[(raise % 9).toInt()]
        var carry = 0L
        val oldSize = value.size
        for (i in 0 until oldSize) {
            val part = value[i].toLong() * factor + carry
            value[i] = (part % BASE).toInt()
            carry = part / BASE
        }
        if (carry != 0L) { value[oldSize] = carry.toInt(); value.size++ }
        return View(value, raise / 9)
    }

    private class View(val value: Digits, val offset: Long) {
        val end: Long get() = if (value.size == 0) 0 else offset + value.size
        fun contains(at: Long): Boolean = value.size != 0 && at >= offset && at < end
        operator fun get(at: Long): Int = if (contains(at)) value[(at - offset).toInt()] else 0
        fun next(at: Long, limit: Long): Long = if (value.size != 0 && at < offset) minOf(offset, limit) else limit
        fun digits(): Long = if (value.size == 0) 0 else 9L * offset + value.digits()
    }

    private fun compare(a: View, b: View): Int {
        val order = a.digits().compareTo(b.digits())
        if (order != 0) return order
        var at = maxOf(a.end, b.end)
        while (at > 0) {
            at--
            val cell = a[at].compareTo(b[at])
            if (cell != 0) return cell
        }
        return 0
    }

    private fun publish(decimal: Digits, sign: Int, scale: Int): Pair<BigInteger, Int>? {
        if (decimal.digits() > MAX_DIGITS) return null
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
            if (bits > Int.MAX_VALUE) return null
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
                val start = slot.toLong() * PAGE
                page = IntArray(minOf(PAGE.toLong(), capacity.toLong() - start).toInt())
                pages[slot] = page
            }
            page[index % PAGE] = value
        }
        fun anyBelow(end: Int): Boolean {
            var start = 0L
            for (page in pages) {
                if (start >= end) break
                if (page != null) {
                    val count = minOf(page.size.toLong(), end.toLong() - start).toInt()
                    for (i in 0 until count) if (page[i] != 0) return true
                }
                start += PAGE
            }
            return false
        }
    }

    private class Digits(capacity: Int) {
        private val cells = Cells(capacity)
        var size = 0
        operator fun get(index: Int): Int = cells[index]
        operator fun set(index: Int, value: Int) { cells[index] = value }
        fun fill(start: Int, end: Int, value: Int) { for (i in start until end) cells[i] = value }
        fun trim() { while (size > 0 && cells[size - 1] == 0) size-- }
        fun digits(): Long {
            check(size > 0)
            var width = 1
            while (width < 9 && cells[size - 1] >= powers[width]) width++
            return 9L * (size - 1) + width
        }
        fun anyBelow(end: Int): Boolean = cells.anyBelow(end)
        fun drop(whole: Int, divisor: Int): Digits {
            val result = Digits(size - whole + 1)
            var remainder = 0L
            for (i in size - 1 downTo whole) {
                val part = remainder * BASE + cells[i]
                result[i - whole] = (part / divisor).toInt()
                remainder = part % divisor
            }
            result.size = size - whole
            result.trim()
            return result
        }
        fun increment() {
            var at = 0
            while (at < size && cells[at] == BASE - 1) { cells[at] = 0; at++ }
            cells[at] = if (at == size) 1 else cells[at] + 1
            if (at == size) size++
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
    }
}

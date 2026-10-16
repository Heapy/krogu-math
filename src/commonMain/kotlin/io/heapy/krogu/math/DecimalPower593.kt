// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Pow-only work coefficients; public integer limits apply only at publication. */
internal object DecimalPower593 {
    private const val BASE = 1_000_000_000
    private const val PAGE = 8192
    private const val MAX_DIGITS = 646456993L
    private const val MAX_WORK = 2147483657L // Mathematical p+digits(n)+1, not an Int cap.
    private const val MAX_CELLS = 477218593L
    private val powers = intArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)

    // The core is immutable after this state is committed. Moving zeros into
    // the tail changes neither the coefficient nor the scale/cohort.
    internal class State internal constructor(internal val sign: Int, internal val core: Words,
        internal val zeros: Long, val scale: Int) {
        fun digits(): Long = core.digits() + zeros
    }

    fun fromMagnitude(sign: Int, words: IntArray, scale: Int): State {
        require((sign == -1 || sign == 1) && words.isNotEmpty() && words.last() > 0)
        val scratch = words.copyOf()
        var size = scratch.size
        val bound = minOf(MAX_DIGITS, 10L * size)
        val decimal = Words(cellCount((bound + 8) / 9))
        while (size != 0) {
            var remainder = 0L
            for (i in size - 1 downTo 0) {
                val part = (remainder shl 30) + scratch[i]
                scratch[i] = (part / BASE).toInt()
                remainder = part % BASE
            }
            decimal.append(remainder.toInt())
            while (size != 0 && scratch[size - 1] == 0) size--
        }
        return commit(sign, decimal, 0, scale.toLong())
    }

    fun accumulate(base: State, exponent: Int, precision: Long, mode: RoundingMode): State {
        require(exponent in 1..999999999 && precision in 1..MAX_WORK)
        var result = commit(1, Words.one(), 0, 0)
        val bits = 32 - exponent.countLeadingZeroBits()
        for (bit in bits - 1 downTo 0) {
            result = multiply(result, result, precision, mode)
            if (exponent and (1 shl bit) != 0) result = multiply(result, base, precision, mode)
        }
        return result
    }

    private fun multiply(left: State, right: State, precision: Long, mode: RoundingMode): State {
        val scale = left.scale.toLong() + right.scale
        // Preserve the adopted nonzero LOW-scale multiplication admission.
        if (scale < Int.MIN_VALUE) checkedScale(scale)
        val a = left.core
        val b = right.core
        // Actual core sizes cover both production D0<=W and generic D0>W.
        val product = Words(cellCount(a.size.toLong() + b.size + 1))
        product.zeroSize(a.size + b.size)
        for (i in 0 until a.size) {
            var carry = 0L
            for (j in 0 until b.size) {
                val part = a[i].toLong() * b[j] + product[i + j] + carry
                product[i + j] = (part % BASE).toInt()
                carry = part / BASE
            }
            product[i + b.size] = carry.toInt()
        }
        product.trim()
        return roundOwned(left.sign * right.sign, product, left.zeros + right.zeros,
            scale, precision, mode)
    }

    fun round(value: State, precision: Long, mode: RoundingMode): State =
        roundOwned(value.sign, value.core.copy(), value.zeros, value.scale.toLong(), precision, mode)

    private fun roundOwned(sign: Int, core: Words, zeroTail: Long, scale: Long,
        precision: Long, mode: RoundingMode): State {
        require(precision in 1..MAX_WORK)
        val initialTail = core.trailingZeros()
        if (initialTail != 0L) core.dropDigits(initialTail)
        var zeros = zeroTail + initialTail
        var drop = maxOf(core.digits() + zeros - precision, 0L)
        if (drop <= zeros) {
            zeros -= drop
        } else {
            val coreDrop = drop - zeros
            val guard = core.digit(coreDrop - 1)
            val sticky = core.anyBelow(coreDrop - 1)
            core.dropDigits(coreDrop)
            zeros = 0
            val inexact = guard != 0 || sticky
            val half = if (guard == 5 && sticky) 1 else guard.compareTo(5)
            // Do not extract quotient zeros before inspecting parity/increment.
            if (inexact && increment(mode, sign, half, core.odd())) {
                core.increment()
                if (core.digits() > precision) {
                    check(core.divideSmall(10) == 0)
                    drop++
                }
            }
        }
        return commit(sign, core, zeros, scale - drop)
    }

    fun reciprocal(value: State, precision: Long, destinationPrecision: Int, mode: RoundingMode): State {
        require(destinationPrecision > 0 && precision <= MAX_WORK &&
            precision - destinationPrecision.toLong() in 2L..10L)
        if (value.core.isOne()) return commit(value.sign, Words.one(), 0, value.zeros - value.scale)
        if (destinationPrecision.toLong() > MAX_DIGITS) return exactReciprocal(value)
        val denominator = value.core
        val digits = denominator.digits()
        val remainder = Words(cellCount(denominator.size.toLong() + 1))
        remainder.powerOfTen(digits - 1)
        val trial = Words(cellCount(denominator.size.toLong() + 1))
        val quotient = Words(cellCount((precision + 1 + 8) / 9 + 1))
        fun nextDigit(): Int {
            remainder.multiplyAdd(10, 0)
            var digit = 9
            while (digit > 0) {
                trial.productSmall(denominator, digit)
                if (remainder.compare(trial) >= 0) break
                digit--
            }
            if (digit != 0) remainder.subtract(trial)
            return digit
        }
        var kept = 0L
        while (kept < precision) {
            quotient.multiplyAdd(10, nextDigit())
            kept++
            if (remainder.size == 0) {
                val scale = -value.scale.toLong() + kept - 1 + digits + value.zeros
                return commit(value.sign, quotient, 0, scale)
            }
        }
        // R0 on this guard digit is a terminating discarded tail, not an exact
        // W-digit result. In particular guard5/R0 is a genuine midpoint.
        val guard = nextDigit()
        val sticky = remainder.size != 0
        val half = if (guard == 5 && sticky) 1 else guard.compareTo(5)
        if ((guard != 0 || sticky) && increment(mode, value.sign, half, quotient.odd())) quotient.increment()
        var scale = -value.scale.toLong() + precision - 1 + digits + value.zeros
        if (quotient.digits() > precision) {
            check(quotient.divideSmall(10) == 0)
            scale--
        }
        return commit(value.sign, quotient, 0, scale)
    }

    // For p>H, nontermination or a finite unsupported exact coefficient cannot
    // become a supported p-digit result. Exact small reciprocals still survive.
    private fun exactReciprocal(value: State): State {
        val remaining = value.core.copy()
        val factor: Int
        val chunk: Int
        val chunkPower: Int
        val reciprocalBase: Int
        when {
            remaining[0] % 2 == 0 -> { factor = 2; chunk = 1 shl 29; chunkPower = 29; reciprocalBase = 5 }
            remaining[0] % 5 == 0 -> { factor = 5; chunk = 244140625; chunkPower = 12; reciprocalBase = 2 }
            else -> throw ArithmeticException("BigInteger overflow")
        }
        var count = 0L
        while (remaining.remainderSmall(chunk) == 0) {
            check(remaining.divideSmall(chunk) == 0)
            count += chunkPower
            if (count >= Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
        }
        while (remaining.remainderSmall(factor) == 0) {
            check(remaining.divideSmall(factor) == 0)
            count++
            if (count >= Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
        }
        if (!remaining.isOne()) throw ArithmeticException("BigInteger overflow")
        val scale = checkedScale(value.zeros + count - value.scale)
        val coefficient = BigInteger.valueOf(reciprocalBase.toLong()).pow(count.toInt())
        val natural = coefficient.decimalPowerState593(scale)!!
        return State(value.sign, natural.core, natural.zeros, natural.scale)
    }

    /** The only public-range publication; reading a state never changes it. */
    fun publish(value: State): Pair<BigInteger, Int> {
        val digits = value.digits()
        if (digits > MAX_DIGITS) throw ArithmeticException("BigInteger overflow")
        val whole = value.zeros / 9
        val offset = (value.zeros % 9).toInt()
        val decimal = if (offset == 0) value.core else value.core.copy().also { it.multiplyAdd(powers[offset], 0) }
        val cells = decimal.size.toLong() + whole
        val bitBound = minOf(Int.MAX_VALUE.toLong(), 4L * digits)
        val binary = Pages(cellCount((bitBound + 29) / 30 + 1))
        var size = 0
        var at = cells
        while (at != 0L) {
            at--
            var carry = if (at < whole) 0L else decimal[(at - whole).toInt()].toLong()
            for (i in 0 until size) {
                val part = binary[i].toLong() * BASE + carry
                binary[i] = (part and 0x3fffffff).toInt()
                carry = part ushr 30
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
            val shift = (bit % 30).toInt()
            var part = binary[word].toLong() ushr shift
            if (shift > 22 && word + 1 < size) part = part or (binary[word + 1].toLong() shl (30 - shift))
            bytes[bytes.lastIndex - i] = (part and 255).toByte()
        }
        return BigInteger(value.sign, bytes) to value.scale
    }

    private fun commit(sign: Int, core: Words, zeroTail: Long, scale: Long): State {
        check(core.size > 0 && (sign == -1 || sign == 1))
        val tail = core.trailingZeros()
        if (tail != 0L) core.dropDigits(tail)
        return State(sign, core, zeroTail + tail, checkedScale(scale))
    }

    private fun checkedScale(scale: Long): Int {
        if (scale !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
            throw ArithmeticException(if (scale < 0) "Overflow" else "Underflow")
        return scale.toInt()
    }

    private fun cellCount(count: Long): Int {
        check(count in 1..MAX_CELLS)
        return count.toInt()
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

    internal class Pages(private val capacity: Int) {
        private val pages = arrayOfNulls<IntArray>(((capacity.toLong() + PAGE - 1) / PAGE).toInt())
        operator fun get(index: Int): Int {
            check(index in 0 until capacity)
            return pages[index / PAGE]?.get(index % PAGE) ?: 0
        }
        operator fun set(index: Int, value: Int) {
            check(index in 0 until capacity)
            val slot = index / PAGE
            val page = pages[slot] ?: run {
                val start = slot.toLong() * PAGE
                IntArray(minOf(PAGE.toLong(), capacity.toLong() - start).toInt()).also { pages[slot] = it }
            }
            page[index % PAGE] = value
        }
    }

    internal class Words(private val capacity: Int) {
        private val cells = Pages(capacity)
        var size = 0
            private set
        operator fun get(index: Int): Int = cells[index]
        operator fun set(index: Int, value: Int) { cells[index] = value }
        fun zeroSize(length: Int) { check(size == 0 && length <= capacity); size = length }
        fun append(value: Int) { cells[size++] = value }
        fun trim() { while (size > 0 && cells[size - 1] == 0) size-- }
        fun isOne(): Boolean = size == 1 && cells[0] == 1
        fun odd(): Boolean = size != 0 && cells[0] % 2 != 0
        fun digits(): Long {
            check(size > 0)
            var width = 1
            val top = cells[size - 1]
            while (width < 9 && top >= powers[width]) width++
            return 9L * (size - 1) + width
        }
        fun copy(): Words {
            val result = Words(cellCount(size.toLong() + 1))
            for (i in 0 until size) result.append(cells[i])
            return result
        }
        fun trailingZeros(): Long {
            check(size > 0)
            var whole = 0
            while (cells[whole] == 0) whole++
            var low = cells[whole]
            var extra = 0
            while (low % 10 == 0) { low /= 10; extra++ }
            return 9L * whole + extra
        }
        fun digit(position: Long): Int {
            check(position >= 0 && position < digits())
            return cells[(position / 9).toInt()] / powers[(position % 9).toInt()] % 10
        }
        fun anyBelow(count: Long): Boolean {
            check(count >= 0)
            val whole = (count / 9).toInt()
            for (i in 0 until whole) if (cells[i] != 0) return true
            return count % 9 != 0L && cells[whole] % powers[(count % 9).toInt()] != 0
        }
        fun dropDigits(count: Long) {
            check(count >= 0 && count < digits())
            val whole = (count / 9).toInt()
            val length = size - whole
            for (i in 0 until length) cells[i] = cells[i + whole]
            size = length
            val divisor = powers[(count % 9).toInt()]
            if (divisor != 1) divideSmall(divisor) else trim()
        }
        fun divideSmall(divisor: Int): Int {
            require(divisor in 1 until BASE)
            var remainder = 0L
            for (i in size - 1 downTo 0) {
                val part = remainder * BASE + cells[i]
                cells[i] = (part / divisor).toInt()
                remainder = part % divisor
            }
            trim()
            return remainder.toInt()
        }
        fun remainderSmall(divisor: Int): Int {
            var remainder = 0L
            for (i in size - 1 downTo 0) remainder = (remainder * BASE + cells[i]) % divisor
            return remainder.toInt()
        }
        fun multiplyAdd(multiplier: Int, addition: Int) {
            var carry = addition.toLong()
            for (i in 0 until size) {
                val part = cells[i].toLong() * multiplier + carry
                cells[i] = (part % BASE).toInt()
                carry = part / BASE
            }
            if (carry != 0L) append(carry.toInt())
        }
        fun productSmall(source: Words, multiplier: Int) {
            size = 0
            var carry = 0L
            for (i in 0 until source.size) {
                val part = source[i].toLong() * multiplier + carry
                append((part % BASE).toInt())
                carry = part / BASE
            }
            if (carry != 0L) append(carry.toInt())
            trim()
        }
        fun compare(other: Words): Int {
            if (size != other.size) return size.compareTo(other.size)
            for (i in size - 1 downTo 0) if (cells[i] != other[i]) return cells[i].compareTo(other[i])
            return 0
        }
        fun subtract(other: Words) {
            var borrow = 0L
            for (i in 0 until size) {
                var part = cells[i].toLong() - (if (i < other.size) other[i] else 0) - borrow
                if (part < 0) { part += BASE; borrow = 1 } else borrow = 0
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
        fun powerOfTen(exponent: Long) {
            check(size == 0 && exponent >= 0)
            val at = (exponent / 9).toInt()
            cells[at] = powers[(exponent % 9).toInt()]
            size = at + 1
        }
        companion object {
            fun one(): Words = Words(2).also { it.append(1) }
        }
    }
}

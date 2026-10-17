package io.heapy.krogu.math

/** Independent base-2^30 multiplication. All input ranges are borrowed, read-only views. */
internal object MagnitudeMultiplication {
    // Conservative calibrated defaults; see docs/integer-multiplication.md for evidence and tradeoffs.
    const val KARATSUBA_THRESHOLD = 64
    const val SQUARE_THRESHOLD = 16
    const val SQUARE_ENTRY_THRESHOLD = 384
    private const val BITS = 30
    private const val MASK = (1L shl BITS) - 1

    fun checkBitLengths(left: Int, right: Int) {
        if (left.toLong() + right - 1 > Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
    }

    /** Public dispatch keeps medium squares whole, without changing large recursive leaves. */
    internal fun selectedKaratsuba539(a: IntArray, b: IntArray): Int =
        if (useJsMultiplicationPolicy539 && a !== b && a.size == b.size) 32 else KARATSUBA_THRESHOLD

    internal fun selectedSquareEntry539(): Int =
        if (useJsMultiplicationPolicy539) 128 else SQUARE_ENTRY_THRESHOLD

    fun multiplyAdaptive(a: IntArray, b: IntArray): IntArray {
        // Preserve the exact historical dispatch on all other platforms.
        if (!useJsMultiplicationPolicy539) return multiplyWithSquareEntry(a, b, SQUARE_ENTRY_THRESHOLD)
        if (a === b && a.size >= SQUARE_THRESHOLD && a.size < selectedSquareEntry539()) {
            val out = IntArray(a.size + b.size)
            square(a, 0, a.size, out, 0)
            return out
        }
        return multiply(a, b, selectedKaratsuba539(a, b), SQUARE_THRESHOLD)
    }

    /** Entry override is explicit so forced-kernel calibration retains its original semantics. */
    fun multiplyWithSquareEntry(a: IntArray, b: IntArray, squareEntryThreshold: Int): IntArray {
        require(squareEntryThreshold >= 0)
        if (a.size >= SQUARE_THRESHOLD && a.size < squareEntryThreshold && a === b) {
            val out = IntArray(a.size + b.size)
            square(a, 0, a.size, out, 0)
            return out
        }
        return multiply(a, b)
    }

    fun multiply(a: IntArray, b: IntArray, threshold: Int = KARATSUBA_THRESHOLD,
        squareThreshold: Int = SQUARE_THRESHOLD): IntArray {
        require(threshold >= 4 && squareThreshold >= 1)
        val out = IntArray(a.size + b.size)
        if (minOf(a.size, b.size) < threshold) {
            if (a.size >= squareThreshold && a === b) square(a, 0, a.size, out, 0)
            else school(a, 0, a.size, b, 0, b.size, out, 0)
        } else {
            val arena = Arena(scratchWords(a.size, b.size, threshold))
            product(a, 0, a.size, b, 0, b.size, out, 0, arena, threshold, squareThreshold)
        }
        return out
    }

    /** Explicit experimental policy; ordinary public dispatch is unchanged. */
    fun multiplyConfigured539(a: IntArray, b: IntArray, policy: MultiplicationPolicy539): IntArray {
        if (a.size >= policy.squareLeaf && a.size < policy.wholeSquareEntry && a === b) {
            val out = IntArray(checkedOutput539(a.size, b.size))
            square(a, 0, a.size, out, 0)
            return out
        }
        return multiply(a, b, policy.karatsuba, policy.squareLeaf)
    }

    /** One child product over immutable views, with its own output and arena. */
    private fun multiplyRange539(a: IntArray, ao: Int, al: Int, b: IntArray, bo: Int, bl: Int,
        policy: MultiplicationPolicy539): IntArray {
        val out = IntArray(checkedOutput539(al, bl))
        val arena = Arena(scratchWords(al, bl, policy.karatsuba))
        product(a, ao, al, b, bo, bl, out, 0, arena, policy.karatsuba, policy.squareLeaf)
        return out
    }

    private fun checkedOutput539(a: Int, b: Int): Int {
        val size = a.toLong() + b
        if (size > Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
        return size.toInt()
    }

    /**
     * Candidate top-level Karatsuba decomposition. Scheduling is outside this
     * common arithmetic. Each child owns its output/arena; inputs and prepared
     * sums are immutable throughout execution. A frame is single-use at join.
     */
    class ParallelFrame539 internal constructor(
        private val a: IntArray, private val b: IntArray, private val policy: MultiplicationPolicy539,
    ) {
        private val split = maxOf(a.size, b.size) / 2
        private val aLow = minOf(a.size, split)
        private val bLow = minOf(b.size, split)
        private val aHigh = a.size - aLow
        private val bHigh = b.size - bLow
        private val sumA = IntArray(maxOf(aLow, aHigh) + 1)
        private val sumALength = sum(a, 0, aLow, aLow, aHigh, sumA, 0, sumA.size)
        private val sumB = if (a === b) sumA else IntArray(maxOf(bLow, bHigh) + 1)
        private val sumBLength = if (a === b) sumALength else sum(b, 0, bLow, bLow, bHigh, sumB, 0, sumB.size)

        fun child(index: Int): IntArray = when (index) {
            0 -> multiplyRange539(a, 0, aLow, b, 0, bLow, policy)
            1 -> multiplyRange539(a, aLow, aHigh, b, bLow, bHigh, policy)
            2 -> multiplyRange539(sumA, 0, sumALength, sumB, 0, sumBLength, policy)
            else -> throw IllegalArgumentException("Child index")
        }

        /** Diagnostic transport only: owned copies may be transferred without detaching caller inputs. */
        fun copiedChildInputs539(index: Int): Pair<IntArray, IntArray> {
            fun copies(a: IntArray, ao: Int, al: Int, b: IntArray, bo: Int, bl: Int): Pair<IntArray, IntArray> {
                val left = a.copyOfRange(ao, ao + al)
                val right = if (a === b && ao == bo && al == bl) left else b.copyOfRange(bo, bo + bl)
                return left to right
            }
            return when (index) {
                0 -> copies(a, 0, aLow, b, 0, bLow)
                1 -> copies(a, aLow, aHigh, b, bLow, bHigh)
                2 -> copies(sumA, 0, sumALength, sumB, 0, sumBLength)
                else -> throw IllegalArgumentException("Child index")
            }
        }

        /** Conservative total new word payload; excludes borrowed inputs, headers and scheduler objects. */
        fun wordPayloadUpperBound539(): Long = sumA.size.toLong() +
            (if (sumB === sumA) 0 else sumB.size) + aLow + bLow + aHigh + bHigh +
            sumALength + sumBLength + a.size + b.size +
            scratchWords(aLow, bLow, policy.karatsuba) + scratchWords(aHigh, bHigh, policy.karatsuba) +
            scratchWords(sumALength, sumBLength, policy.karatsuba)

        /** All three children must have completed before any recombination. */
        fun combine(low: IntArray, high: IntArray, middle: IntArray): IntArray {
            require(low.size == aLow + bLow && high.size == aHigh + bHigh && middle.size == sumALength + sumBLength)
            require(low !== high && low !== middle && high !== middle)
            val out = IntArray(checkedOutput539(a.size, b.size))
            low.copyInto(out)
            high.copyInto(out, 2 * split)
            subtract(middle, 0, middle.size, low, 0, low.size)
            subtract(middle, 0, middle.size, high, 0, high.size)
            addAt(out, split, middle, 0, trimmed(middle, 0, middle.size))
            return out
        }
    }

    /** Candidate-only eligibility; production parallelMultiply remains virtual multiply. */
    fun prepareParallel539(a: IntArray, b: IntArray, policy: MultiplicationPolicy539): ParallelFrame539? {
        checkedOutput539(a.size, b.size)
        val small = minOf(a.size, b.size)
        if (small < maxOf(4, policy.karatsuba) || maxOf(a.size, b.size).toLong() > 2L * small) return null
        return ParallelFrame539(a, b, policy)
    }

    /** Checked arena upper bound, including a reusable short-operand-sized block product. */
    fun scratchWords(a: Int, b: Int, threshold: Int = KARATSUBA_THRESHOLD): Int {
        require(a >= 0 && b >= 0 && threshold >= 4)
        val small = minOf(a, b)
        if (small < threshold) return 0
        var size = if (maxOf(a, b).toLong() > 2L * small) small else maxOf(a, b)
        var total = if (maxOf(a, b).toLong() > 2L * small) 2L * small else 0L
        while (size >= threshold) {
            size = ((size.toLong() + 1) / 2 + 1).toInt()
            total += 4L * size
        }
        if (total > Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
        return total.toInt()
    }

    private class Arena(size: Int) {
        val words = IntArray(size)
        var used = 0
        fun allocate(size: Int): Int {
            val offset = used
            check(size <= words.size - offset) { "Multiplication scratch bound" }
            used += size
            return offset
        }
    }

    private fun product(a: IntArray, ao: Int, al: Int, b: IntArray, bo: Int, bl: Int,
        out: IntArray, oo: Int, arena: Arena, threshold: Int, squareThreshold: Int) {
        out.fill(0, oo, oo + al + bl)
        if (al == 0 || bl == 0) return
        val small = minOf(al, bl)
        val large = maxOf(al, bl)
        if (small < threshold) {
            if (a === b && ao == bo && al == bl && al >= squareThreshold) square(a, ao, al, out, oo)
            else school(a, ao, al, b, bo, bl, out, oo)
            return
        }
        val mark = arena.used
        if (large.toLong() > 2L * small) {
            // Blocking keeps both the balanced recursion and live scratch O(short).
            val longer = if (al >= bl) a else b
            val lo = if (al >= bl) ao else bo
            val shorter = if (al >= bl) b else a
            val so = if (al >= bl) bo else ao
            val temp = arena.allocate(2 * small)
            var position = 0
            while (position < large) {
                val length = minOf(small, large - position)
                product(longer, lo + position, length, shorter, so, small,
                    arena.words, temp, arena, threshold, squareThreshold)
                addAt(out, oo + position, arena.words, temp, length + small)
                position += length
            }
            arena.used = mark
            return
        }
        val split = large / 2
        val aLow = minOf(al, split)
        val bLow = minOf(bl, split)
        val aHigh = al - aLow
        val bHigh = bl - bLow
        product(a, ao, aLow, b, bo, bLow, out, oo, arena, threshold, squareThreshold)
        if (aHigh > 0 && bHigh > 0)
            product(a, ao + aLow, aHigh, b, bo + bLow, bHigh,
                out, oo + 2 * split, arena, threshold, squareThreshold)
        val ac = maxOf(aLow, aHigh) + 1
        val bc = maxOf(bLow, bHigh) + 1
        val sumA = arena.allocate(ac)
        val sumB = arena.allocate(bc)
        val middle = arena.allocate(ac + bc)
        val aLength = sum(a, ao, aLow, ao + aLow, aHigh, arena.words, sumA, ac)
        val bLength = sum(b, bo, bLow, bo + bLow, bHigh, arena.words, sumB, bc)
        product(arena.words, sumA, aLength, arena.words, sumB, bLength,
            arena.words, middle, arena, threshold, squareThreshold)
        val middleLength = aLength + bLength
        subtract(arena.words, middle, middleLength, out, oo, aLow + bLow)
        if (aHigh > 0 && bHigh > 0)
            subtract(arena.words, middle, middleLength, out, oo + 2 * split, aHigh + bHigh)
        addAt(out, oo + split, arena.words, middle, trimmed(arena.words, middle, middleLength))
        arena.used = mark
    }

    private fun school(a: IntArray, ao: Int, al: Int, b: IntArray, bo: Int, bl: Int,
        out: IntArray, oo: Int) {
        for (i in 0 until al) schoolRow(a[ao + i], b, bo, bl, out, oo + i)
    }

    // The same schoolbook row, with no temporary one-word input array.
    internal fun multiplyWord529(word: Int, borrowedWide: IntArray): IntArray {
        val out = IntArray(borrowedWide.size + 1)
        schoolRow(word, borrowedWide, 0, borrowedWide.size, out, 0)
        return out
    }

    private inline fun schoolRow(word: Int, b: IntArray, bo: Int, bl: Int, out: IntArray, oo: Int) {
        var carry = 0L
        for (j in 0 until bl) {
            val value = word.toLong() * b[bo + j] + out[oo + j] + carry
            out[oo + j] = (value and MASK).toInt()
            carry = value ushr BITS
        }
        out[oo + bl] = carry.toInt()
    }

    private fun square(a: IntArray, offset: Int, length: Int, out: IntArray, oo: Int) {
        for (i in 0 until length) {
            val word = a[offset + i].toLong()
            val diagonal = word * word + out[oo + 2 * i]
            out[oo + 2 * i] = (diagonal and MASK).toInt()
            var carry = diagonal ushr BITS
            for (j in i + 1 until length) {
                // 2*(2^30-1)^2 + two carry digits is below 2^61.
                val value = 2L * word * a[offset + j] + out[oo + i + j] + carry
                out[oo + i + j] = (value and MASK).toInt()
                carry = value ushr BITS
            }
            var index = oo + i + length
            while (carry != 0L) {
                val value = out[index].toLong() + carry
                out[index++] = (value and MASK).toInt()
                carry = value ushr BITS
            }
        }
    }

    private fun sum(a: IntArray, low: Int, lowLength: Int, high: Int, highLength: Int,
        out: IntArray, offset: Int, capacity: Int): Int {
        var carry = 0L
        for (i in 0 until capacity - 1) {
            val value = (if (i < lowLength) a[low + i].toLong() else 0L) +
                (if (i < highLength) a[high + i].toLong() else 0L) + carry
            out[offset + i] = (value and MASK).toInt()
            carry = value ushr BITS
        }
        out[offset + capacity - 1] = carry.toInt()
        return trimmed(out, offset, capacity)
    }

    private fun subtract(out: IntArray, offset: Int, length: Int, b: IntArray, bo: Int, bl: Int) {
        var borrow = 0L
        for (i in 0 until length) {
            val value = out[offset + i].toLong() - (if (i < bl) b[bo + i].toLong() else 0L) - borrow
            out[offset + i] = (value and MASK).toInt()
            borrow = if (value < 0) 1 else 0
        }
        check(borrow == 0L) { "Negative Karatsuba cross product" }
    }

    private fun addAt(out: IntArray, offset: Int, b: IntArray, bo: Int, length: Int) {
        var carry = 0L
        var i = 0
        while (i < length || carry != 0L) {
            val value = out[offset + i].toLong() + (if (i < length) b[bo + i].toLong() else 0L) + carry
            out[offset + i] = (value and MASK).toInt()
            carry = value ushr BITS
            i++
        }
    }

    private fun trimmed(a: IntArray, offset: Int, length: Int): Int {
        var size = length
        while (size > 0 && a[offset + size - 1] == 0) size--
        return size
    }
}

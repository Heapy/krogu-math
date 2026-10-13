package io.heapy.krogu.math

/** Checked representation sizing; quantities near the public range limit must not wrap Int. */
internal object MagnitudeLimits {
    const val WORD_BITS = 30
    const val MAX_BYTES = 268435456

    fun packedWords(byteCount: Int, extraCarryBit: Boolean): Int {
        require(byteCount >= 0)
        if (byteCount > MAX_BYTES) throw ArithmeticException("BigInteger overflow")
        val bits = byteCount.toLong() * 8 + if (extraCarryBit) 1 else 0
        return ((bits + WORD_BITS - 1) / WORD_BITS).toInt()
    }

    fun checkedBitLength(wordCount: Int, highWord: Int): Int {
        require(wordCount > 0 && highWord in 1 until (1 shl WORD_BITS))
        val bits = (wordCount.toLong() - 1) * WORD_BITS + 32 - highWord.countLeadingZeroBits()
        if (bits > Int.MAX_VALUE) throw ArithmeticException("BigInteger overflow")
        return bits.toInt()
    }

    fun initialSqrtShift(bitLength: Int): Int = ((bitLength.toLong() + 1) / 2).toInt()
}

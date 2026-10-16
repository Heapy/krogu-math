package io.heapy.krogu.math

internal expect fun registerPublic539(handler: (Int) -> Unit)
internal expect fun publicInput539(index: Int): IntArray
internal expect fun publicSame539(): Boolean
internal expect fun publicHash539(value: Int)

/** Parent-only root-library comparator. No worker-module classes/objects cross this boundary. */
internal object PublicWorkerComparator539 {
    private class Prepared(val a: BigInteger, val b: BigInteger, val expected: BigInteger,
        val originalA: ByteArray, val originalB: ByteArray)
    private var prepared: Prepared? = null
    private var escaped: BigInteger? = null
    fun register() = registerPublic539(::invoke)
    private fun invoke(mode: Int) {
        when (mode) {
            0 -> { // Preparation and host-to-root bridge copying are UNTIMED.
                val a = publicInput539(0); val b = publicInput539(1)
                val expected = publicInput539(2); val policy = publicInput539(3)
                require(a.size <= 65536 && b.size <= 65536 && expected.size <= 131074)
                check(policy.contentEquals(intArrayOf(MagnitudeMultiplication.KARATSUBA_THRESHOLD,
                    MagnitudeMultiplication.SQUARE_THRESHOLD, MagnitudeMultiplication.SQUARE_ENTRY_THRESHOLD))) {
                    "Public root and forced kernel policy differ; compare actual selected policy"
                }
                val x = number(a)
                val y = if (publicSame539()) { check(a.contentEquals(b)); x } else number(b)
                prepared = Prepared(x, y, number(expected), x.toByteArray(), y.toByteArray())
                escaped = null
            }
            1 -> { // Entire timed callback: actual public multiply + actual result retention.
                val values = checkNotNull(prepared)
                escaped = values.a.multiply(values.b)
            }
            2 -> { // Full latest-result and public-input checks; OUTSIDE counters.
                val values = checkNotNull(prepared)
                val result = checkNotNull(escaped)
                check(result == values.expected)
                check(values.a.toByteArray().contentEquals(values.originalA))
                check(values.b.toByteArray().contentEquals(values.originalB))
                publicHash539(result.hashCode())
            }
            3 -> { prepared = null; escaped = null }
            else -> error("Unknown public comparator mode")
        }
    }
    private fun number(words: IntArray): BigInteger {
        val bytes = ByteArray(((words.size.toLong() * 30 + 7) / 8).toInt())
        var accumulator = 0L; var bits = 0; var destination = bytes.lastIndex
        for (word in words) {
            require(word in 0..0x3fffffff)
            accumulator = accumulator or (word.toLong() shl bits)
            bits += 30 // <=37 live bits; public construction outside timing.
            while (bits >= 8) {
                bytes[destination--] = accumulator.toByte()
                accumulator = accumulator ushr 8; bits -= 8
            }
        }
        if (bits != 0) bytes[destination--] = accumulator.toByte()
        check(destination == -1)
        return BigInteger(1, bytes)
    }
}

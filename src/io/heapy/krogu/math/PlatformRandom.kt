package io.heapy.krogu.math

import kotlin.random.Random

/** Maps to java.util.Random on JVM, preserving Java constructor and factory signatures.
 * Common callers should normally supply kotlin.random.Random instead.
 */
expect open class PlatformRandom {
    open fun nextBytes(bytes: ByteArray)
}

internal fun PlatformRandom.asCommonRandom(): Random = object : Random() {
    override fun nextBits(bitCount: Int): Int {
        if (bitCount == 0) return 0
        val bytes = nextBytes(ByteArray(4))
        var bits = 0
        for (byte in bytes) bits = (bits shl 8) or (byte.toInt() and 255)
        return bits ushr (32 - bitCount)
    }

    override fun nextBytes(array: ByteArray): ByteArray {
        this@asCommonRandom.nextBytes(array)
        return array
    }
}

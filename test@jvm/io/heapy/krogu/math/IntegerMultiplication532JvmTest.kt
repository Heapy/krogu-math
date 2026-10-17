package io.heapy.krogu.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerMultiplication532JvmTest {
    @Test
    fun signedDenseSparseCarryAndThresholdNeighborsMatchPublicJdk() {
        val random = Random(2532)
        val bits = listOf(1, 30, 31, 479, 480, 481, 1890, 1919, 1920, 1921, 1950, 3840, 7680, 15360, 30720)
        for (leftBits in bits) for (rightBits in listOf(1, leftBits / 2 + 1, leftBits, leftBits * 3 + 1)) {
            for (kind in 0..2) {
                val a = integer(leftBits, kind, random)
                val b = integer(rightBits, kind, random)
                val x = BigInteger(a.toByteArray())
                val y = BigInteger(b.toByteArray())
                assertEquals(a.multiply(b).toString(), x.multiply(y).toString())
                assertEquals(a.multiply(a).toString(), x.multiply(x).toString())
                assertEquals(a.multiply(a.negate()).toString(), x.multiply(x.negate()).toString())
            }
        }
    }

    private fun integer(bits: Int, kind: Int, random: Random): java.math.BigInteger {
        val bytes = ByteArray((bits + 7) / 8).also { random.nextBytes(it) }
        bytes[0] = (bytes[0].toInt() and (255 ushr (bytes.size * 8 - bits))).toByte()
        val dense = java.math.BigInteger(1, bytes).setBit(bits - 1)
        val value = when (kind) {
            0 -> dense
            1 -> java.math.BigInteger.ONE.shiftLeft(bits).subtract(java.math.BigInteger.ONE)
            else -> java.math.BigInteger.ONE.shiftLeft(bits - 1).add(java.math.BigInteger.ONE)
        }
        return if (random.nextBoolean()) value else value.negate()
    }
}

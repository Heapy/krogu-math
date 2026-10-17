package io.heapy.krogu.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class IntegerMultiplication532Test {
    @Test
    fun forcedAlgorithmsMatchIndependentSchoolReference() {
        val random = Random(532)
        val lengths = listOf(1, 2, 3, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 127, 128, 129)
        for (length in lengths) for (kind in 0..3) {
            val left = words(length, kind, random)
            val right = words(length + (kind % 2), (kind + 1) % 4, random)
            val saved = left.copyOf()
            val expected = schoolReference(left, right)
            for (threshold in listOf(4, 8, 16, 64, Int.MAX_VALUE)) {
                assertContentEquals(expected, MagnitudeMultiplication.multiply(left, right, threshold), "$length/$kind/$threshold")
                assertContentEquals(schoolReference(left, left), MagnitudeMultiplication.multiply(left, left, threshold, 1), "square $length/$kind/$threshold")
            }
            assertContentEquals(saved, left)
        }
    }

    @Test
    fun squareEntrySelectorMatchesIndependentReferenceAtBoundaries() {
        val random = Random(2532)
        for (size in listOf(15, 16, 17, 63, 64, 65, 96, 128, 192, 255, 256, 257, 383, 384, 385, 512, 1024)) {
            for (kind in 0..3) {
                val a = words(size, kind, random)
                val saved = a.copyOf()
                val expected = schoolReference(a, a)
                for (entry in listOf(0, 256, 384, Int.MAX_VALUE)) {
                    assertContentEquals(expected, MagnitudeMultiplication.multiplyWithSquareEntry(a, a, entry), "$size/$kind/$entry")
                }
                assertContentEquals(expected, MagnitudeMultiplication.multiplyAdaptive(a, a))
                assertContentEquals(expected, MagnitudeMultiplication.multiplyAdaptive(a, a.copyOf()))
                assertContentEquals(saved, a)
            }
        }
    }

    @Test
    fun unbalancedBlocksPropagateCarriesAndRespectRatioBoundary() {
        val random = Random(1532)
        for (small in listOf(4, 8, 17, 64)) for (large in listOf(2 * small - 1, 2 * small, 2 * small + 1, 7 * small + 1)) {
            for (kind in 0..3) {
                val a = words(small, kind, random)
                val b = words(large, kind, random)
                val expected = schoolReference(a, b)
                assertContentEquals(expected, MagnitudeMultiplication.multiply(a, b, 4))
                assertContentEquals(expected, MagnitudeMultiplication.multiply(b, a, 4))
            }
        }
    }

    @Test
    fun publicSignedSquaresNeighborsAndSharedMagnitudeAliases() {
        for (digits in listOf(1, 8, 120, 479, 480, 481, 959, 960, 961, 2048)) {
            val a = BigInteger("f".repeat(digits), 16)
            val square = BigInteger("f".repeat(digits - 1) + "e" + "0".repeat(digits - 1) + "1", 16)
            assertEquals(square, a.multiply(a))
            assertEquals(square.negate(), a.multiply(a.negate()))
            assertEquals(square, a.multiply(BigInteger(a.toByteArray())))
            assertEquals(square, a.parallelMultiply(a))
            assertEquals(square.subtract(BigInteger.ONE), a.subtract(BigInteger.ONE).multiply(a.add(BigInteger.ONE)))
            assertEquals(BigInteger.ZERO, a.multiply(BigInteger.ZERO))
        }
    }

    @Test
    fun virtualParallelDispatchAndCheckedSizing() {
        val receiver = object : BigInteger("17") {
            override fun multiply(value: BigInteger): BigInteger = BigInteger("42")
        }
        assertEquals(BigInteger("42"), receiver.parallelMultiply(BigInteger.ONE))
        MagnitudeMultiplication.checkBitLengths(Int.MAX_VALUE, 1)
        MagnitudeMultiplication.checkBitLengths(Int.MAX_VALUE / 2, Int.MAX_VALUE / 2 + 2)
        assertFailsWith<ArithmeticException> { MagnitudeMultiplication.checkBitLengths(Int.MAX_VALUE, 2) }
        assertFailsWith<ArithmeticException> { MagnitudeMultiplication.scratchWords(Int.MAX_VALUE, Int.MAX_VALUE, 4) }
        assertEquals(0, MagnitudeMultiplication.scratchWords(3, 10000, 4))
        assertTrue(MagnitudeMultiplication.scratchWords(64, 100000, 4) < 1000)
    }

    private fun words(size: Int, kind: Int, random: Random): IntArray = IntArray(size) { index ->
        when (kind) {
            0 -> (1 shl 30) - 1
            1 -> random.nextInt(1 shl 30)
            2 -> if (index == 0 || index == size - 1) 1 else 0
            else -> if (index < size / 2) random.nextInt(1 shl 30) else 0
        }
    }

    // Frozen row-multiplication reference, independent of the production dispatcher/arena.
    private fun schoolReference(a: IntArray, b: IntArray): IntArray {
        val result = IntArray(a.size + b.size)
        for (i in a.indices) {
            var carry = 0L
            for (j in b.indices) {
                val n = a[i].toLong() * b[j] + result[i + j] + carry
                result[i + j] = (n and 0x3fffffff).toInt()
                carry = n ushr 30
            }
            result[i + b.size] = carry.toInt()
        }
        return result
    }
}

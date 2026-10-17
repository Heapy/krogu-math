package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class SelectedMultiplicationPolicy539Test {
    @Test fun physicalAliasEqualLengthAndRatioBoundaries() {
        for (size in listOf(16, 31, 32, 33, 63, 64, 127, 128, 129, 383, 384)) {
            val a = IntArray(size) { 0x3fffffff }
            assertEquals(64, MagnitudeMultiplication.selectedKaratsuba539(a, a))
            assertEquals(if (useJsMultiplicationPolicy539 || useNativeBalancedMultiplicationPolicy539) 32 else 64,
                MagnitudeMultiplication.selectedKaratsuba539(a, a.copyOf()))
            for (ratio in listOf(2, 4)) {
                val b = IntArray(size * ratio) { 17 }
                assertEquals(64, MagnitudeMultiplication.selectedKaratsuba539(a, b))
                assertEquals(64, MagnitudeMultiplication.selectedKaratsuba539(b, a))
            }
        }
        assertEquals(if (useJsMultiplicationPolicy539) 128 else 384,
            MagnitudeMultiplication.selectedSquareEntry539())
    }

    @Test fun selectedProductsMatchFullSchoolAndKeepBorrowedInputs() {
        for (size in listOf(31, 32, 33, 63, 64, 127, 128, 129, 383, 384)) {
            val a = IntArray(size) { if (it % 3 == 0) 0x3fffffff else (it * 104729 + 31) and 0x3fffffff }
            for (b in listOf(a, a.copyOf(), IntArray(size * 2) { 0x3fffffff }, IntArray(size * 4) { it + 1 })) {
                val beforeA = a.copyOf(); val beforeB = b.copyOf()
                val expected = MagnitudeMultiplication.multiply(a, b, Int.MAX_VALUE, Int.MAX_VALUE)
                val actual = MagnitudeMultiplication.multiplyAdaptive(a, b)
                assertContentEquals(expected, actual)
                calibration539Verify(a, b, actual)
                assertContentEquals(beforeA, a); assertContentEquals(beforeB, b)
            }
        }
    }

    @Test fun publicOppositeSignSharedMagnitudeAndDistinctCopies() {
        for (size in listOf(31, 32, 33, 127, 128, 129, 383, 384)) {
            val bit = size * 30 - 1
            val a = BigInteger.ONE.shiftLeft(bit).add(BigInteger.valueOf(31))
            // negate borrows the same immutable wide magnitude; copy construction does not.
            val negative = a.negate()
            val distinct = BigInteger(a.toByteArray())
            val expected = BigInteger.ONE.shiftLeft(bit * 2)
                .add(BigInteger.valueOf(62).shiftLeft(bit)).add(BigInteger.valueOf(961))
            val before = a.toByteArray()
            assertEquals(expected, a.multiply(a))
            assertEquals(expected, a.multiply(distinct))
            assertEquals(expected.negate(), a.multiply(negative))
            assertEquals(expected.negate(), negative.multiply(a))
            assertEquals(expected, negative.multiply(negative))
            assertContentEquals(before, a.toByteArray())
        }
    }
}

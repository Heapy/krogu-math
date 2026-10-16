package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class MultiplicationPolicy539Test {
    @Test fun configuredRangesAndSerialFrameMatchIndependentSchoolProduct() {
        val policies = listOf(MultiplicationPolicy539(4, 1, 0), MultiplicationPolicy539(16, 4, 64),
            MultiplicationPolicy539(64, 16, 384))
        for (size in listOf(4, 5, 15, 16, 17, 63, 64, 65, 127, 128, 129)) {
            val left = IntArray(size) { if (it % 3 == 0) (1 shl 30) - 1 else (it * 317 + 1) and ((1 shl 30) - 1) }
            for (right in listOf(left, left.copyOf(), IntArray(size + 1) { (1 shl 30) - 1 })) {
                val a = left.copyOf(); val b = right.copyOf()
                val expected = MagnitudeMultiplication.multiply(left, right, Int.MAX_VALUE, Int.MAX_VALUE)
                for (policy in policies) {
                    assertContentEquals(expected, MagnitudeMultiplication.multiplyConfigured539(left, right, policy))
                    val frame = MagnitudeMultiplication.prepareParallel539(left, right, policy) ?: continue
                    assertContentEquals(expected, frame.combine(frame.child(0), frame.child(1), frame.child(2)))
                }
                assertContentEquals(a, left); assertContentEquals(b, right)
            }
        }
    }

    @Test fun carryLeadingZerosAndUnbalancedEligibilityAreExplicit() {
        val policy = MultiplicationPolicy539(4, 1, 0)
        val left = intArrayOf((1 shl 30) - 1, (1 shl 30) - 1, 0, 0, (1 shl 30) - 1)
        val right = intArrayOf(1, 0, 0, 0, 0, 0, 0)
        val frame = assertNotNull(MagnitudeMultiplication.prepareParallel539(left, right, policy))
        val expected = MagnitudeMultiplication.multiply(left, right, Int.MAX_VALUE, Int.MAX_VALUE)
        assertContentEquals(expected, frame.combine(frame.child(0), frame.child(1), frame.child(2)))
        val short = IntArray(4) { (1 shl 30) - 1 }
        val twice = IntArray(8) { (1 shl 30) - 1 }
        val boundary = assertNotNull(MagnitudeMultiplication.prepareParallel539(short, twice, policy))
        assertContentEquals(MagnitudeMultiplication.multiply(short, twice, Int.MAX_VALUE, Int.MAX_VALUE),
            boundary.combine(boundary.child(0), boundary.child(1), boundary.child(2)))
        assertNull(MagnitudeMultiplication.prepareParallel539(IntArray(3), IntArray(8), policy))
        assertNull(MagnitudeMultiplication.prepareParallel539(IntArray(4), IntArray(9), policy))
    }
}

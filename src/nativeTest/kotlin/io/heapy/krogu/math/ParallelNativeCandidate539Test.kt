@file:OptIn(kotlin.concurrent.atomics.ExperimentalAtomicApi::class)
package io.heapy.krogu.math

import kotlin.concurrent.atomics.AtomicInt
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ParallelNativeCandidate539Test {
    private val policy = MultiplicationPolicy539(4, 1, 0)
    private val a = IntArray(17) { 0x3fffffff }
    private val b = IntArray(18) { it * 137 + 1 }
    private fun expected() = MagnitudeMultiplication.multiply(a, b, Int.MAX_VALUE, Int.MAX_VALUE)

    @Test fun ownedWorkersAgreeIncludingNestedAndProcessorFallback() {
        val calls = AtomicInt(0)
        lateinit var candidate: ParallelNativeCandidate539
        candidate = ParallelNativeCandidate539(2, processors = 4) { frame, index ->
            calls.fetchAndAdd(1)
            if (index == 0) assertContentEquals(expected(), candidate.multiply(a, b, policy))
            frame.child(index)
        }
        assertContentEquals(expected(), candidate.multiply(a, b, policy))
        assertEquals(3, calls.load())
        ParallelNativeCandidate539(1, processors = 1) { _, _ -> error("processor fallback scheduled") }.also {
            assertContentEquals(expected(), it.multiply(a, b, policy))
        }
    }

    @Test fun childMarkerIsObservedAndPermitsAvailableAfterTermination() {
        val marker = IllegalStateException("Native child marker")
        val candidate = ParallelNativeCandidate539(1, processors = 4) { frame, index ->
            if (index == 0) throw marker
            frame.child(index)
        }
        val observed = try { candidate.multiply(a, b, policy); null } catch (problem: Throwable) { problem }
        // This current-runtime object-identity expectation is deliberately tested,
        // rather than assuming Worker Future behaves like the JVM scheduler.
        assertSame(marker, observed)
        val calls = AtomicInt(0)
        val afterFailure = ParallelNativeCandidate539(2, processors = 4) { frame, index ->
            calls.fetchAndAdd(1); frame.child(index)
        }
        assertContentEquals(expected(), afterFailure.multiply(a, b, policy))
        assertEquals(3, calls.load())
    }
}

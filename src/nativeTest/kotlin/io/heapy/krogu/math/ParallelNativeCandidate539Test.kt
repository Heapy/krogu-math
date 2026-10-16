@file:OptIn(kotlin.concurrent.atomics.ExperimentalAtomicApi::class,
    kotlin.native.concurrent.ObsoleteWorkersApi::class, kotlin.ExperimentalStdlibApi::class)
package io.heapy.krogu.math

import kotlin.concurrent.atomics.AtomicInt
import kotlin.native.concurrent.Worker
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

    @Test fun registrationFailuresJoinLocalHandlesAndPriorAcceptedChildren() {
        Worker.current // Ensure the test caller is registered before the baseline.
        val baseline = Worker.activeWorkers.map { it.id }.toSet()
        for (futureStore in listOf(false, true)) {
            val marker = IllegalStateException("registration $futureStore")
            val candidate = ParallelNativeCandidate539(2, processors = 4,
                registrationFault539 = { future, index -> if (index == 1 && future == futureStore) throw marker })
            val observed = try { candidate.multiply(a, b, policy); null } catch (problem: Throwable) { problem }
            assertSame(marker, observed)
            assertEquals(baseline, Worker.activeWorkers.map { it.id }.toSet(), "No unregistered or prior owned Worker may survive")
            val calls = AtomicInt(0)
            val next = ParallelNativeCandidate539(2, processors = 4) { frame, index ->
                calls.fetchAndAdd(1); frame.child(index)
            }
            assertContentEquals(expected(), next.multiply(a, b, policy))
            assertEquals(3, calls.load(), "Both global permits must be available after registration failure")
            assertEquals(baseline, Worker.activeWorkers.map { it.id }.toSet())
        }
    }
}

package io.heapy.krogu.math

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.CountDownLatch
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ParallelCandidate539JvmTest {
    private val policy = MultiplicationPolicy539(4, 1, 0)
    private val a = IntArray(17) { (1 shl 30) - 1 }
    private val b = IntArray(18) { it * 137 + 1 }
    private fun expected() = MagnitudeMultiplication.multiply(a, b, Int.MAX_VALUE, Int.MAX_VALUE)
    private fun await(latch: CountDownLatch) = assertTrue(latch.await(5, TimeUnit.SECONDS), "owned task did not reach checkpoint")
    private fun join(thread: Thread) { thread.join(5_000); assertFalse(thread.isAlive, "owned caller did not terminate") }

    @Test fun nestedProcessorAndInterruptedEntryUseSynchronousFallback() {
        val calls = AtomicInteger()
        lateinit var candidate: ParallelCandidate539
        candidate = ParallelCandidate539(1, processors = 4) { frame, index ->
            calls.incrementAndGet()
            if (index == 0) assertContentEquals(expected(), candidate.multiply(a, b, policy))
            frame.child(index)
        }
        candidate.use {
            assertContentEquals(expected(), it.multiply(a, b, policy))
            assertEquals(3, calls.get(), "nested call must not schedule another frame")
            Thread.currentThread().interrupt()
            try {
                assertContentEquals(expected(), it.multiply(a, b, policy))
                assertTrue(Thread.currentThread().isInterrupted)
                assertEquals(3, calls.get())
            } finally { Thread.interrupted() }
        }
        val enclosingPool = ForkJoinPool(1)
        try {
            ParallelCandidate539(1, processors = 4) { _, _ -> error("enclosing FJP scheduled") }.use { guarded ->
                assertContentEquals(expected(), enclosingPool.submit(Callable { guarded.multiply(a, b, policy) }).get(5, TimeUnit.SECONDS))
            }
        } finally { enclosingPool.shutdown() }
        ParallelCandidate539(1, processors = 1) { _, _ -> error("processor fallback scheduled") }.use {
            assertContentEquals(expected(), it.multiply(a, b, policy))
        }
    }

    @Test fun partialSubmissionRejectionJoinsAcceptedChildBeforeFallback() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val submissions = AtomicInteger()
        val executor = object : ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, ArrayBlockingQueue(1)) {
            override fun execute(command: Runnable) {
                if (submissions.incrementAndGet() == 2) throw RejectedExecutionException("second child")
                super.execute(command)
            }
        }
        val failure = AtomicReference<Throwable?>()
        val result = AtomicReference<IntArray?>()
        val candidate = ParallelCandidate539(2, executor, processors = 4) { frame, index ->
            entered.countDown()
            check(release.await(5, TimeUnit.SECONDS))
            frame.child(index)
        }
        val caller = Thread {
            try { result.set(candidate.multiply(a, b, policy)) }
            catch (problem: Throwable) { failure.set(problem) }
        }
        try {
            caller.start(); await(entered)
            assertTrue(caller.isAlive, "rejection must not abandon the first accepted child")
        } finally {
            release.countDown(); join(caller); candidate.close()
        }
        failure.get()?.let { throw it }
        assertContentEquals(expected(), result.get())
        assertEquals(2, submissions.get())
    }

    @Test fun interruptionDuringJoinRetainsProductAndRestoresFlag() {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>()
        val result = AtomicReference<IntArray?>()
        val interrupted = AtomicReference(false)
        val candidate = ParallelCandidate539(1, processors = 4) { frame, index ->
            if (index == 0) { entered.countDown(); check(release.await(5, TimeUnit.SECONDS)) }
            frame.child(index)
        }
        val caller = Thread {
            try {
                result.set(candidate.multiply(a, b, policy))
                interrupted.set(Thread.currentThread().isInterrupted)
            } catch (problem: Throwable) { failure.set(problem) }
        }
        try {
            caller.start(); await(entered); caller.interrupt()
        } finally {
            release.countDown(); join(caller); candidate.close()
        }
        failure.get()?.let { throw it }
        assertContentEquals(expected(), result.get())
        assertTrue(interrupted.get())
    }

    @Test fun globalPermitExhaustionFallsBackAndChildFailureReleasesOwnership() {
        val entered = CountDownLatch(2)
        val release = CountDownLatch(1)
        val failure = AtomicReference<Throwable?>()
        val candidate = ParallelCandidate539(2, processors = 4) { frame, index ->
            if (index < 2) { entered.countDown(); check(release.await(5, TimeUnit.SECONDS)) }
            frame.child(index)
        }
        val caller = Thread {
            try { assertContentEquals(expected(), candidate.multiply(a, b, policy)) }
            catch (problem: Throwable) { failure.set(problem) }
        }
        try {
            caller.start(); await(entered)
            ParallelCandidate539(1, processors = 4) { _, _ -> error("exhausted gate scheduled") }.use {
                assertContentEquals(expected(), it.multiply(a, b, policy))
            }
        } finally { release.countDown(); join(caller); candidate.close() }
        failure.get()?.let { throw it }
        val marker = AssertionError("child marker")
        ParallelCandidate539(1, processors = 4) { frame, index ->
            if (index == 0) throw marker
            frame.child(index)
        }.use {
            val caught = try { it.multiply(a, b, policy); null } catch (problem: Throwable) { problem }
            assertSame(marker, caught)
        }
        val calls = AtomicInteger()
        ParallelCandidate539(2, processors = 4) { frame, index -> calls.incrementAndGet(); frame.child(index) }.use {
            assertContentEquals(expected(), it.multiply(a, b, policy))
        }
        assertEquals(3, calls.get(), "all global permits must be available after failure")
    }
}

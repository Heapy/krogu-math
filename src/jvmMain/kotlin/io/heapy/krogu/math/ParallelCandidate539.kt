package io.heapy.krogu.math

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinTask
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.Semaphore
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Bounded JVM scheduler used by diagnostic controls and the public parallel frontend.
 * Common Kotlin owns all arithmetic; final frontend acceptance requires composed evidence.
 * The shared gate bounds additional workers across candidate instances; it does
 * not claim control over unrelated application threads or external CPU load.
 * The processor count injection exists for deterministic branch tests; campaigns
 * must use the default actual runtime count. An injected executor must follow
 * Executor.execute: throwing rejects the task, returning accepts it. Pools have
 * bounded idle lifetime; the shared gate bounds active workers, not idle threads
 * across multiple experimental candidate instances.
 */
internal class ParallelCandidate539(
    private val workers: Int,
    private val executor: ExecutorService = boundedExecutor(workers),
    private val processors: Int = Runtime.getRuntime().availableProcessors(),
    // Fault-injection seam is internal; production uses the same shared context.
    private val workerContext539: ThreadLocal<Boolean> = nested,
    private val childOperation: (MagnitudeMultiplication.ParallelFrame539, Int) -> IntArray = { frame, index -> frame.child(index) },
) : AutoCloseable {
    init { require(workers in 1..2 && processors >= 1) }

    fun multiply(a: IntArray, b: IntArray, policy: MultiplicationPolicy539): IntArray {
        // No blocking permit acquisition or work submitted into an enclosing FJP.
        // A preexisting caller interruption retains the synchronous semantics.
        if (processors <= workers || nested.get() == true ||
            ForkJoinTask.inForkJoinPool() || Thread.currentThread().isInterrupted || !workerGate.tryAcquire(workers))
            return MagnitudeMultiplication.multiplyConfigured539(a, b, policy)
        val previous = nested.get()
        var result: IntArray? = null
        try {
            nested.set(true)
            val frame = MagnitudeMultiplication.prepareParallel539(a, b, policy)
            if (frame != null) result = executeFrame(frame)
        } finally {
            if (previous == null) nested.remove() else nested.set(previous)
            workerGate.release(workers)
        }
        // Rejection or ineligible shape selects this only after submitted readers
        // are terminal and invocation references/permits have been released.
        return result ?: MagnitudeMultiplication.multiplyConfigured539(a, b, policy)
    }

    private inner class Child(private var frame: MagnitudeMultiplication.ParallelFrame539?, val index: Int) : Runnable {
        val done = CountDownLatch(1)
        @Volatile var value: IntArray? = null
        @Volatile var failure: Throwable? = null
        override fun run() {
            var previous: Boolean? = null
            var contextCaptured = false
            try {
                // get() can allocate a fresh ThreadLocalMap before any arithmetic.
                // Every accepted Runnable prefix belongs to the terminal fence.
                previous = workerContext539.get()
                contextCaptured = true
                workerContext539.set(true)
                value = childOperation(checkNotNull(frame), index)
            } catch (problem: Throwable) {
                failure = problem
            } finally {
                frame = null
                try {
                    if (contextCaptured) {
                        if (previous == null) workerContext539.remove() else workerContext539.set(previous)
                    }
                } catch (cleanup: Throwable) {
                    // Preserve the first failure without allocating suppression or
                    // retrying arithmetic/context acquisition after OOM.
                    if (failure == null) failure = cleanup
                } finally {
                    done.countDown()
                }
            }
        }
        fun clear() { frame = null; value = null; failure = null }
    }

    private fun executeFrame(frame: MagnitudeMultiplication.ParallelFrame539): IntArray? {
        // Allocate owner slots before any submission. Recording an accepted task
        // is then a nonallocating store; even allocation failure cannot lose its join.
        val submitted = arrayOfNulls<Child>(workers)
        var submittedCount = 0
        val values = arrayOfNulls<IntArray>(3)
        var primary: Throwable? = null
        var rejected = false
        var interrupted = false
        try {
            try {
                for (index in 0 until workers) {
                    val task = Child(frame, index)
                    try {
                        executor.execute(task)
                        submitted[submittedCount++] = task
                    } catch (declined: RejectedExecutionException) {
                        task.clear()
                        rejected = true
                        break
                    } catch (problem: Throwable) {
                        task.clear()
                        throw problem
                    }
                }
                if (!rejected) for (index in workers..2) values[index] = childOperation(frame, index)
            } catch (problem: Throwable) {
                primary = problem
            } finally {
                // Interruption does not abandon immutable-input readers or return
                // a partial product. Restore the flag after joining every owner.
                for (index in 0 until submittedCount) {
                    val task = checkNotNull(submitted[index])
                    while (true) {
                        try { task.done.await(); break }
                        catch (_: InterruptedException) { interrupted = true }
                    }
                    values[task.index] = task.value
                    if (primary == null) primary = task.failure
                }
            }
            primary?.let { throw it } // No arithmetic/OOM retry fallback.
            if (rejected) return null
            return frame.combine(checkNotNull(values[0]), checkNotNull(values[1]), checkNotNull(values[2]))
        } finally {
            for (index in 0 until submittedCount) {
                submitted[index]?.clear()
                submitted[index] = null
            }
            values.fill(null)
            if (interrupted) Thread.currentThread().interrupt()
        }
    }

    override fun close() { executor.shutdown() }

    /** Diagnostic lifecycle fence, outside arithmetic timing; never interrupts readers. */
    fun awaitTermination539(seconds: Long): Boolean {
        require(seconds in 1..30)
        return executor.awaitTermination(seconds, TimeUnit.SECONDS)
    }

    companion object {
        private val workerGate = Semaphore(2)
        private val nested = ThreadLocal<Boolean>()
        private val threadIds = AtomicInteger()
        private fun boundedExecutor(workers: Int): ExecutorService {
            require(workers in 1..2)
            val factory = ThreadFactory { operation ->
                Thread(operation, "krogu-multiply-539-${threadIds.incrementAndGet()}").apply { isDaemon = true }
            }
            return ThreadPoolExecutor(workers, workers, 30L, TimeUnit.SECONDS,
                ArrayBlockingQueue(2), factory, ThreadPoolExecutor.AbortPolicy()).apply { allowCoreThreadTimeOut(true) }
        }
    }
}

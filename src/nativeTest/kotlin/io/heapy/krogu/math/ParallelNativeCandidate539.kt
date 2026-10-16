@file:OptIn(kotlin.native.concurrent.ObsoleteWorkersApi::class,
    kotlin.concurrent.atomics.ExperimentalAtomicApi::class, kotlin.experimental.ExperimentalNativeApi::class)
package io.heapy.krogu.math

import kotlin.concurrent.atomics.AtomicInt
import kotlin.native.Platform
import kotlin.native.concurrent.Future
import kotlin.native.concurrent.ThreadLocal
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker

@ThreadLocal private var nativeDepth539 = 0
private val nativePermits539 = AtomicInt(2)

/** Diagnostic-only: invocation-owned Workers, no production route or persistent pool. */
internal class ParallelNativeCandidate539(
    private val workers: Int,
    private val processors: Int = Platform.getAvailableProcessors(),
    private val operation: (MagnitudeMultiplication.ParallelFrame539, Int) -> IntArray = { frame, index -> frame.child(index) },
) {
    init { require(workers in 1..2 && processors >= 1) }

    private class Outcome(val value: IntArray?, val failure: Throwable?)
    private class Job(val frame: MagnitudeMultiplication.ParallelFrame539, val index: Int,
        val operation: (MagnitudeMultiplication.ParallelFrame539, Int) -> IntArray) {
        fun run(): Outcome {
            nativeDepth539++
            return try { Outcome(operation(frame, index), null) }
            catch (problem: Throwable) { Outcome(null, problem) }
            finally { nativeDepth539-- }
        }
    }

    fun multiply(a: IntArray, b: IntArray, policy: MultiplicationPolicy539): IntArray {
        if (processors <= workers || nativeDepth539 != 0 || !acquire())
            return MagnitudeMultiplication.multiplyConfigured539(a, b, policy)
        nativeDepth539++
        try {
            val frame = MagnitudeMultiplication.prepareParallel539(a, b, policy)
                ?: return MagnitudeMultiplication.multiplyConfigured539(a, b, policy)
            return execute(frame)
        } finally {
            nativeDepth539--
            nativePermits539.fetchAndAdd(workers)
        }
    }

    private fun acquire(): Boolean {
        // Bounded CAS attempts: contending calls promptly choose synchronous work.
        repeat(3) {
            val available = nativePermits539.load()
            if (available < workers) return false
            if (nativePermits539.compareAndSet(available, available - workers)) return true
        }
        return false
    }

    private fun execute(frame: MagnitudeMultiplication.ParallelFrame539): IntArray {
        val owners = arrayOfNulls<Worker>(workers)
        val futures = arrayOfNulls<Future<Outcome>>(workers)
        val values = arrayOfNulls<IntArray>(3)
        var primary: Throwable? = null
        try {
            try {
                for (index in 0 until workers) {
                    val job = Job(frame, index, operation)
                    val worker = Worker.start(errorReporting = false, name = "krogu539-child-$index")
                    owners[index] = worker // Nonallocating registration immediately after start.
                    // Only producer captures job; Worker job function captures no external state.
                    futures[index] = worker.execute(TransferMode.SAFE, { job }, { payload -> payload.run() })
                }
                for (index in workers..2) values[index] = operation(frame, index)
            } catch (problem: Throwable) { primary = problem }
            finally {
                // Consume every accepted Future even after another child/caller failed.
                for (index in futures.indices) {
                    val future = futures[index] ?: continue
                    try {
                        val outcome = future.result
                        values[index] = outcome.value
                        if (primary == null) primary = outcome.failure
                    } catch (problem: Throwable) { if (primary == null) primary = problem }
                    finally { futures[index] = null }
                }
                // A created Worker is also owned if subsequent submission failed.
                // Termination is part of invocation cost and occurs before permit release.
                for (index in owners.indices) {
                    val worker = owners[index] ?: continue
                    try { worker.requestTermination(processScheduledJobs = true).result }
                    catch (problem: Throwable) { if (primary == null) primary = problem }
                    finally { owners[index] = null }
                }
            }
            primary?.let { throw it } // No arithmetic/OOM retry fallback.
            return frame.combine(checkNotNull(values[0]), checkNotNull(values[1]), checkNotNull(values[2]))
        } finally { values.fill(null) }
    }
}

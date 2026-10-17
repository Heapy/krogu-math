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

/** Invocation-owned Native Workers shared by public dispatch and exact diagnostic controls.
 * No retained pool: every successful/failed accepted invocation joins and terminates
 * all owned Workers before releasing the bounded process-wide child permits.
 */
internal class NativeParallelEngine539(
    private val workers: Int,
    private val processors: Int = Platform.getAvailableProcessors(),
    private val registrationFault539: ((future: Boolean, index: Int) -> Unit)? = null,
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

    fun multiply(a: IntArray, b: IntArray, policy: MultiplicationPolicy539): IntArray =
        tryMultiply539(a, b, policy) ?: MagnitudeMultiplication.multiplyConfigured539(a, b, policy)

    /** Null is exclusively preacceptance refusal; any accepted failure propagates. */
    fun tryMultiply539(a: IntArray, b: IntArray, policy: MultiplicationPolicy539): IntArray? {
        if (processors <= workers || nativeDepth539 != 0 || !acquire()) return null
        nativeDepth539++
        try {
            val frame = MagnitudeMultiplication.prepareParallel539(a, b, policy)
                ?: return null
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

    private fun suppress539(primary: Throwable, cleanup: Throwable) {
        if (primary !== cleanup) {
            // Suppression itself can allocate: never replace the primary failure.
            try { primary.addSuppressed(cleanup) } catch (_: Throwable) { }
        }
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
                    // Worker/Future are value classes: nullable generic stores may
                    // BOX/allocate. Keep each local nonnullable handle until its
                    // owner registration succeeds; clean up from that local on failure.
                    try {
                        registrationFault539?.invoke(false, index)
                        owners[index] = worker
                    } catch (problem: Throwable) {
                        try { worker.requestTermination(processScheduledJobs = true).result }
                        catch (cleanup: Throwable) { suppress539(problem, cleanup) }
                        throw problem
                    }
                    // Only producer captures job; Worker job function captures no external state.
                    val future = worker.execute(TransferMode.SAFE, { job }, { payload -> payload.run() })
                    try {
                        registrationFault539?.invoke(true, index)
                        futures[index] = future
                    } catch (problem: Throwable) {
                        // Accepted work must be consumed even if boxing its Future fails.
                        try { future.result.failure?.let { suppress539(problem, it) } }
                        catch (cleanup: Throwable) { suppress539(problem, cleanup) }
                        throw problem
                    }
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

// Cheap context check before class/backend initialization. The engine repeats its
// own gate/acquire; a concurrent caller may legitimately consume permits meanwhile.
internal fun nativeParallelContext539(): Boolean = Platform.getAvailableProcessors() > 2 && nativeDepth539 == 0

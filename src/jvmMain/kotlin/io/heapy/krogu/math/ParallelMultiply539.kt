package io.heapy.krogu.math

import java.util.concurrent.ForkJoinTask

/** Conservative candidate from current JVM75; final public-route calibration pending. */
internal actual fun canParallelMultiply539(leftWords: Int, rightWords: Int): Boolean {
    val small = minOf(leftWords, rightWords)
    val large = maxOf(leftWords, rightWords)
    if (small < 4096 || large.toLong() > 2L * small) return false
    return Runtime.getRuntime().availableProcessors() > 2 &&
        !ForkJoinTask.inForkJoinPool() && !Thread.currentThread().isInterrupted
}

private object JvmPublicParallel539 {
    // One owner per defining classloader, at most two daemon workers; existing executor has
    // bounded queue2 and30s core idle timeout. No executor per public invocation.
    // Lazy initialization/worker startup belongs to the actual first-call cost.
    val candidate by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ParallelCandidate539(2) }
    val policy = MultiplicationPolicy539(MagnitudeMultiplication.KARATSUBA_THRESHOLD,
        MagnitudeMultiplication.SQUARE_THRESHOLD, MagnitudeMultiplication.SQUARE_ENTRY_THRESHOLD)
}

internal actual fun parallelMagnitude539(a: IntArray, b: IntArray): IntArray? {
    // Recheck immediately before lazy backend access: quotas may have changed
    // since initial eligibility. No arithmetic has run when this declines.
    if (Runtime.getRuntime().availableProcessors() <= 2 || ForkJoinTask.inForkJoinPool() ||
        Thread.currentThread().isInterrupted) return null
    return JvmPublicParallel539.candidate.multiply(a, b, JvmPublicParallel539.policy)
}

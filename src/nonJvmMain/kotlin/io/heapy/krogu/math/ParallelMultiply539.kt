package io.heapy.krogu.math

internal actual fun canParallelMultiply539(leftWords: Int, rightWords: Int): Boolean =
    minOf(leftWords, rightWords) >= 4096 && maxOf(leftWords, rightWords).toLong() <= 2L * minOf(leftWords, rightWords) && nodeParallelAvailable539()

internal actual fun parallelMagnitude539(a: IntArray, b: IntArray): IntArray? {
    // One explicit recursive policy per pool generation. Compare the real public
    // route against independently selected platform serial policy before adoption.
    val policy = MultiplicationPolicy539(64, 16, 384)
    var failure: Throwable? = null
    val accepted = BooleanArray(2)
    val results = arrayOfNulls<IntArray>(2)
    val lease = acquireNodeParallel539() ?: return null
    var frame: MagnitudeMultiplication.ParallelFrame539? = null
    var middle: IntArray? = null
    try {
        val prepared = checkNotNull(MagnitudeMultiplication.prepareParallel539(a, b, policy))
        frame = prepared
        for (index in 0..1) {
            val (left, right) = prepared.copiedChildInputs539(index)
            lease.submit(index, left, right, left === right, policy.karatsuba, policy.squareLeaf)
            accepted[index] = true
        }
        middle = prepared.child(2)
    } catch (problem: Throwable) { failure = problem }
    finally {
        for (index in 0..1) if (accepted[index]) {
            try { results[index] = lease.join(index) }
            catch (problem: Throwable) { if (failure == null) failure = problem }
        }
        try { lease.release(failure != null) }
        catch (problem: Throwable) { if (failure == null) failure = problem }
    }
    failure?.let { throw it }
    return checkNotNull(frame).combine(checkNotNull(results[0]), checkNotNull(results[1]), checkNotNull(middle))
}

internal interface NodeParallelLease539 {
    fun submit(index: Int, left: IntArray, right: IntArray, same: Boolean, k: Int, s: Int)
    fun join(index: Int): IntArray
    fun release(failed: Boolean)
}
internal expect fun nodeParallelAvailable539(): Boolean
internal expect fun acquireNodeParallel539(): NodeParallelLease539?

package bench

import kotlin.time.TimeSource

expect fun platformArguments(args: Array<String>): Array<String>
expect fun implementations(): List<Pair<String, List<Workload>>>
expect fun platformDescription(): String
expect fun allocatedBytes(): Long?
expect fun retainedHeapBytes(): Long?

// A bounded, globally reachable result ring escapes every operation. Hashes are read outside timing.
// It intentionally includes result escape/boxing/invocation cost equally on both JVM implementations.
val results = arrayOfNulls<Any>(256)
private fun batch(workload: Workload, count: Int): Long {
    val mark = TimeSource.Monotonic.markNow()
    repeat(count) { i -> results[i and 255] = workload.run(i) }
    return mark.elapsedNow().inWholeNanoseconds
}
private fun checksum(): Int = results.fold(1) { value, result -> value * 31 + (result?.hashCode() ?: 0) }

fun main(arguments: Array<String>) {
    val args = platformArguments(arguments)
    val warmup = args.getOrNull(0)?.toInt() ?: 5
    val samples = args.getOrNull(1)?.toInt() ?: 5
    val targetNs = (args.getOrNull(2)?.toLong() ?: 100L) * 1_000_000L
    require(warmup >= 1 && samples >= 1 && targetNs >= 1_000_000L)
    println("# platform=${platformDescription()};warmup=$warmup;samples=$samples;target_ns=$targetNs")
    println("implementation,workload,sample,operations,elapsed_ns,ns_per_op,ops_per_second,allocated_bytes_per_op,retained_heap_bytes,checksum")
    // Build BOTH JVM datasets before any timed operations. Separate process forks alternate implementation order.
    val implementations = implementations().let { if (args.getOrNull(3) == "reverse") it.reversed() else it }
    for ((implementation, workloads) in implementations) {
        for (workload in workloads) {
            var count = 256
            // Calibration and all initialization/caching/JIT work are excluded from samples.
            while (true) {
                val elapsed = batch(workload, count)
                if (elapsed >= targetNs / 2 || count >= 1_048_576) break
                count *= 2
            }
            repeat(warmup) { batch(workload, count); checksum() }
            repeat(samples) { sample ->
                val before = allocatedBytes()
                val elapsed = batch(workload, count)
                val after = allocatedBytes()
                val digest = checksum()
                val allocation = if (before != null && after != null) (after - before).toDouble() / count else null
                // Forced GC/snapshot is outside timing and allocation brackets; ring remains reachable.
                val retained = retainedHeapBytes()
                val ns = elapsed.toDouble() / count
                println("$implementation,${workload.name},$sample,$count,$elapsed,$ns,${1e9 / ns},${allocation ?: "NA"},${retained ?: "NA"},$digest")
            }
        }
    }
}

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
private fun prime(workload: Workload) {
    // Only 16 unique input results are evaluated, outside timing. This is an observation baseline,
    // not memoization: the timed path always invokes workload.run and replaces its result slot.
    val initial = Array(16) { workload.run(it) }
    results.indices.forEach { results[it] = initial[it and 15] }
}
private fun batch(workload: Workload, count: Int, offset: Int = 0): Long {
    val mark = TimeSource.Monotonic.markNow()
    repeat(count) { i ->
        val logical = i + offset
        results[logical and 255] = workload.run(logical)
    }
    return mark.elapsedNow().inWholeNanoseconds
}
private fun checksum(): Int = results.fold(1) { value, result -> value * 31 + (result?.hashCode() ?: 0) }
private fun inputMask(count: Int, offset: Int): Int = if (count >= 16) 65535 else
    (0 until count).fold(0) { mask, i -> mask or (1 shl ((offset + i) and 15)) }

fun main(arguments: Array<String>) {
    val args = platformArguments(arguments)
    val warmup = args.getOrNull(0)?.toInt() ?: 5
    val samples = args.getOrNull(1)?.toInt() ?: 5
    val targetNs = (args.getOrNull(2)?.toLong() ?: 100L) * 1_000_000L
    val inputSeed = args.getOrNull(4)?.toInt() ?: 0
    require(warmup >= 1 && samples >= 1 && targetNs >= 1_000_000L && inputSeed >= 0)
    println("# platform=${platformDescription()};warmup=$warmup;samples=$samples;target_ns=$targetNs;input_seed=$inputSeed")
    println("implementation,workload,sample,operations,elapsed_ns,ns_per_op,ops_per_second,allocated_bytes_per_op,retained_heap_bytes,checksum,input_offset,input_mask")
    // Build BOTH JVM datasets before any timed operations. Separate process forks alternate implementation order.
    val implementations = implementations().let { if (args.getOrNull(3) == "reverse") it.reversed() else it }
    for ((implementation, workloads) in implementations) {
        for (workload in workloads) {
            prime(workload)
            var count = 1
            // A slow operation may need only one invocation. Never force a large ring-sized batch.
            // Calibration and all initialization/caching/JIT work are excluded from samples.
            while (true) {
                val elapsed = batch(workload, count)
                if (elapsed >= targetNs / 2 || count >= 1_048_576) break
                count *= 2
            }
            repeat(warmup) { batch(workload, count, inputSeed + it); checksum() }
            repeat(samples) { sample ->
                val offset = inputSeed + sample
                val before = allocatedBytes()
                val elapsed = batch(workload, count, offset)
                val after = allocatedBytes()
                val digest = checksum()
                val allocation = if (before != null && after != null) (after - before).toDouble() / count else null
                // Forced GC/snapshot is outside timing and allocation brackets; ring remains reachable.
                val retained = retainedHeapBytes()
                val ns = elapsed.toDouble() / count
                println("$implementation,${workload.name},$sample,$count,$elapsed,$ns,${1e9 / ns},${allocation ?: "NA"},${retained ?: "NA"},$digest,${offset and 15},${inputMask(count, offset)}")
            }
        }
    }
}

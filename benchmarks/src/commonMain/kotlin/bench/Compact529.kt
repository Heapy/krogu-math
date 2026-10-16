package bench

import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.DecimalAccumulator
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.MathContext
import io.heapy.krogu.math.RoundingMode
import kotlin.time.TimeSource

internal data class CompactMemory529(val metric: String, val heap: Long?, val arrays: Long?, val rss: Long?)
internal expect fun compactMemory529(): CompactMemory529
internal expect fun compactSnapshot529(label: String)
private var retained529: Array<Any>? = null

/** Separate opt-in campaign; the527 workload inventory and default entry are unchanged. */
internal fun compact529(arguments: Array<String>) {
    require(arguments.size == 5 && arguments[0] == "compact529")
    val warmups = arguments[1].toInt(); val samples = arguments[2].toInt()
    val targetNs = arguments[3].toLong() * 1_000_000
    val retainedCount = arguments[4].toInt()
    require(warmups in 3..20 && samples in 5..20 && targetNs in 10_000_000L..200_000_000L && retainedCount in 1000..200000)
    println("# compact529;${platformDescription()};warmups=$warmups;samples=$samples;target_ns=$targetNs;retained_count=$retainedCount")
    println("# RETAIN reports whole-runtime before/after snapshots with an equally sized Unit-reference control array, not exact per-object bytes or isolated operation residency.")
    val wideText = "123456789".repeat(16)
    val integers = Array(16) { BigInteger.valueOf(12345000001L + it) }
    val wide = Array(16) { BigInteger(wideText + (it + 11)) }
    val decimals = Array(16) { BigDecimal.valueOf(12345000001L + it, 2) }
    val wideDecimals = Array(16) { BigDecimal(wide[it], 2) }
    val divisors = Array(16) { BigDecimal.valueOf(123L + it, 2) }
    val mc = MathContext(7, RoundingMode.HALF_EVEN)
    val original527A = Array(16) { BigDecimal("${123 + it}.45") }
    val original527B = Array(16) { BigDecimal("${11 + it}.37") }
    val mc34 = MathContext(34, RoundingMode.HALF_EVEN)
    val factor = BigInteger.valueOf(123)
    val workloads = listOf(
        Workload("integer/create-small") { BigInteger.valueOf(12345000001L + (it and 15)) },
        Workload("integer/create-min") { BigInteger.valueOf(Long.MIN_VALUE + (it and 15)) },
        Workload("integer/add-small") { integers[it and 15].add(integers[(it + 1) and 15]) },
        Workload("integer/multiply-small") { integers[it and 15].multiply(factor) },
        Workload("integer/wide-add") { wide[it and 15].add(wide[(it + 1) and 15]) },
        Workload("integer/wide-multiply") { wide[it and 15].multiply(wide[(it + 1) and 15]) },
        Workload("integer/wide-divide") { wide[it and 15].divide(integers[it and 15]) },
        Workload("decimal/create-small") { BigDecimal.valueOf(12345000001L + (it and 15), 2) },
        Workload("decimal/create-min") { BigDecimal.valueOf(Long.MIN_VALUE + (it and 15), 2) },
        Workload("decimal/add-small") { decimals[it and 15].add(divisors[it and 15]) },
        Workload("decimal/multiply-small") { decimals[it and 15].multiply(divisors[it and 15]) },
        Workload("decimal/divide-context") { decimals[it and 15].divide(divisors[it and 15], mc) },
        Workload("decimal/original527-mc34-divide") { original527A[it and 15].divide(original527B[it and 15], mc34) },
        Workload("decimal/accumulator-compact-fresh") { DecimalAccumulator().add(decimals[it and 15]).add(divisors[it and 15]).snapshot() },
        Workload("decimal/round") { decimals[it and 15].round(mc) },
        Workload("decimal/setScale") { decimals[it and 15].setScale(1, RoundingMode.HALF_EVEN) },
        Workload("decimal/metadata") { decimals[it and 15].precision() },
        Workload("decimal/wide-create") { BigDecimal(wide[it and 15], 2) },
        Workload("decimal/wide-add") { wideDecimals[it and 15].add(wideDecimals[(it + 1) and 15]) },
        Workload("decimal/wide-multiply") { wideDecimals[it and 15].multiply(wideDecimals[(it + 1) and 15], mc) },
        Workload("decimal/wide-divide") { wideDecimals[it and 15].divide(divisors[it and 15], mc) },
    )
    // Full representations are emitted untimed for paired old/new validation.
    for (workload in workloads) repeat(16) { index ->
        val value = workload.run(index)
        val representation = if (value is BigDecimal) "${value.unscaledValue()}@${value.scale()}" else value.toString()
        println("CHECK,${workload.name},$index,$representation")
    }
    for (workload in workloads) {
        fun batch(count: Int, offset: Int): Long {
            val mark = TimeSource.Monotonic.markNow()
            repeat(count) { index -> results[index and 255] = workload.run(index + offset) }
            return mark.elapsedNow().inWholeNanoseconds
        }
        val warming = TimeSource.Monotonic.markNow()
        var offset = 0
        do { results[offset and 255] = workload.run(offset); offset++ }
        while (warming.elapsedNow().inWholeNanoseconds < 200_000_000L)
        var count = 1
        while (batch(count, offset) < targetNs && count < 1_048_576) count *= 2
        repeat(warmups) { batch(count, offset + it) }
        repeat(samples) { sample ->
            val before = allocatedBytes(); val elapsed = batch(count, offset + sample); val after = allocatedBytes()
            val allocation = if (before != null && after != null) (after - before).toDouble() / count else null
            val checksum = results.fold(1) { hash, value -> 31 * hash + (value?.hashCode() ?: 0) }
            println("TIME,${workload.name},$sample,$count,$elapsed,${allocation ?: "NA"},$checksum")
        }
    }
    val factories = retentionFactories529()
    warmRetention529(factories)
    for ((name, factory) in factories) for (count in listOf(retainedCount / 4, retainedCount / 2, retainedCount)) repeat(3) { sample ->
        produceRetained529(count) { Unit }
        val before = compactMemory529()
        produceRetained529(count, factory)
        val after = compactMemory529()
        check(retained529!!.size == count)
        println("RETAIN,$name,$sample,$count,${before.metric},${before.heap ?: "NA"},${after.heap ?: "NA"},${before.arrays ?: "NA"},${after.arrays ?: "NA"},${before.rss ?: "NA"},${after.rss ?: "NA"}")
    }
    retained529 = null
    println("PASS compact529")
}

// Producer frames return before a forced-GC observation, following the Native
// memory-manager guidance. No temporary construction references remain in them.
private fun produceRetained529(count: Int, factory: (Int) -> Any) {
    retained529 = Array(count) { factory(it) }
}
private fun warmRetention529(factories: List<Pair<String, (Int) -> Any>>) {
    for ((_, factory) in factories) repeat(16) { factory(it) }
}
private fun retentionFactories529(): List<Pair<String, (Int) -> Any>> {
    val wideText = "123456789".repeat(16)
    return listOf<Pair<String, (Int) -> Any>>(
        "integer/small" to { BigInteger.valueOf(12345000001L + (it and 15)) },
        "integer/min" to { BigInteger.valueOf(Long.MIN_VALUE + (it and 15)) },
        "integer/wide" to { BigInteger(wideText + (it and 15)) },
        "decimal/small" to { BigDecimal.valueOf(12345000001L + (it and 15), 2) },
        "decimal/min" to { BigDecimal.valueOf(Long.MIN_VALUE + (it and 15), 2) },
        "decimal/wide" to { BigDecimal(BigInteger(wideText + (it and 15)), 2) },
        // Coverage controls: distinguish array headers/owned256-byte payload
        // from host-only metrics before interpreting the Wasm numeric slopes.
        "control/bytes0" to { ByteArray(0) },
        "control/bytes256" to { ByteArray(256) },
        "control/ints64" to { IntArray(64) },
    )
}

/** Bounded Node/V8 graph capture, separate from all arithmetic samples. */
internal fun compactHeap529(arguments: Array<String>) {
    require(arguments.size == 2 && arguments[0] == "compact529-heap")
    val count = arguments[1].toInt()
    require(count in 32..1024)
    val factories = retentionFactories529().filterNot { it.first.endsWith("/min") }
    warmRetention529(factories)
    produceRetained529(count) { Unit }
    compactMemory529()
    compactSnapshot529("unit")
    for ((name, factory) in factories) {
        produceRetained529(count, factory)
        compactMemory529()
        compactSnapshot529(name.replace('/', '_'))
        check(retained529!!.size == count)
    }
    retained529 = null
    println("PASS compact529-heap")
}

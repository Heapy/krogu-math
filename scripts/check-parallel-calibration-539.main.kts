#!/usr/bin/env kotlinr

/** Strict source-only checker. Runner must supply raw logs AND matching .exit files. */
import java.io.File

require(args.size == 7) { "samples mainSHA256 probeSHA256 output.csv fork1.log fork2.log fork3.log" }
val samples = args[0].toInt().also { require(it in 5..15) }
val mainSha = args[1].also { require(it.matches(Regex("[a-f0-9]{64}"))) }
val probeSha = args[2].also { require(it.matches(Regex("[a-f0-9]{64}"))) }
val output = File(args[3]).canonicalFile.also { require(!it.exists()) }
val logs = args.drop(4).map { File(it).canonicalFile }
require(logs.toSet().size == 3 && output !in logs)
data class Row(val id: String, val sample: Int, val count: Long, val wall: Long, val cpu: Long, val bytes: Long, val peak: Long)
val rows = mutableListOf<List<Row>>()
val orders = mutableListOf<String>()
var expectedActive: Set<String>? = null
for (log in logs) {
    check(File(log.path + ".exit").readText().trim() == "0") { "Nonterminal/nonzero fork: $log" }
    val lines = log.readLines()
    val runtime = lines.single { it.startsWith("# parallel539 ") }
    val processors = Regex("processors=([0-9]+)").find(runtime)!!.groupValues[1].toInt().also { check(it >= 1) }
    val order = Regex("order=(forward|reverse)$").find(runtime)!!.groupValues[1]
    orders += order
    check(lines.single { it.startsWith("# main=") }.startsWith("# main=$mainSha probe=$probeSha "))
    check(lines.count { it == "PASS_PAR539,artifact_recheck" } == 1)
    val ids = buildSet {
        for (size in listOf(256, 1024, 4096, 8192, 16384)) for (shape in listOf("distinct", "square", "ratio2")) {
            add("$size/$shape/kernel"); add("$size/$shape/frame")
            if (processors > 1) add("$size/$shape/worker1")
            if (processors > 2) add("$size/$shape/worker2")
        }
    }
    if (expectedActive == null) expectedActive = ids else check(expectedActive == ids) { "CPU eligibility changed between forks" }
    val all = buildSet {
        for (size in listOf(256, 1024, 4096, 8192, 16384)) for (shape in listOf("distinct", "square", "ratio2"))
            for (variant in listOf("kernel", "frame", "worker1", "worker2")) add("$size/$shape/$variant")
    }
    val ineligible = lines.filter { it.startsWith("INELIGIBLE539,") }.map {
        val p = it.split(','); check(p.size == 3 && p[2] == "processor_budget"); p[1]
    }
    check(ineligible.size == ineligible.toSet().size && ineligible.toSet() == all - ids)
    val checks = lines.filter { it.startsWith("CHECKPAR539,") }.map {
        val p = it.split(','); check(p.size == 3 && p[2] == "PASS"); p[1]
    }
    check(checks.size == ids.size && checks.toSet() == ids)
    val payload = lines.filter { it.startsWith("PAYLOAD539,") }.map {
        val p = it.split(','); check(p.size == 4 && p[2].toLong() > 0 && p[3] == "words_excludes_headers_scheduler_inputs"); p[1]
    }
    val shapes = all.map { it.substringBeforeLast('/') }.toSet()
    check(payload.size == shapes.size && payload.toSet() == shapes)
    val setups = lines.filter { it.startsWith("SETUP539,") }.map {
        val p = it.split(','); check(p.size == 6)
        check(p[2].toLong() in 1..60_000_000_000L && p[3].toLong() >= 0 && p[4].toLong() >= 0 && p[5].toLong() >= 0)
        p[1]
    }
    val workers = ids.filter { "/worker" in it }.toSet()
    check(setups.size == workers.size && setups.toSet() == workers)
    val timed = lines.filter { it.startsWith("PAR539,") }.map {
        val p = it.split(','); check(p.size == 9)
        val row = Row(p[1], p[2].toInt(), p[3].toLong(), p[4].toLong(), p[5].toLong(), p[6].toLong(), p[7].toLong())
        // Nine fields are emitted: marker/id/sample/count/wall/cpu/bytes/peak/hash.
        // Reject silently shifted columns before any aggregation.
        p[8].toInt()
        check(row.id in ids && row.sample in 0 until samples && row.count in 1..1_048_576)
        check(row.wall in 1..60_000_000_000L && row.cpu >= 0 && row.bytes >= 0 && row.peak >= 0)
        row
    }
    check(timed.size == ids.size * samples)
    check(timed.groupBy { it.id }.keys == ids)
    for ((id, group) in timed.groupBy { it.id }) {
        check(group.size == samples && group.map { it.sample }.toSet() == (0 until samples).toSet()) { id }
        check(group.map { it.count }.toSet().size == 1) { "Batch size drift: $id" }
    }
    rows += timed
}
check(orders.toSet() == setOf("forward", "reverse")) { "Need reversed-order fresh forks" }
fun median(values: List<Double>): Double {
    val sorted = values.sorted(); val middle = sorted.size / 2
    return if (sorted.size % 2 == 1) sorted[middle] else (sorted[middle - 1] + sorted[middle]) / 2
}
output.bufferedWriter().use { writer ->
    writer.appendLine("case,wall_median_ns,wall_min_process_median_ns,wall_max_process_median_ns,cpu_median_ns,approx_all_thread_bytes_median,heap_pool_peak_upper_bound_bytes")
    for (id in checkNotNull(expectedActive).sorted()) {
        val groups = rows.map { it.filter { row -> row.id == id } }
        fun medians(metric: (Row) -> Double) = groups.map { median(it.map(metric)) }
        val wall = medians { it.wall.toDouble() / it.count }
        val cpu = medians { it.cpu.toDouble() / it.count }
        val bytes = medians { it.bytes.toDouble() / it.count }
        writer.appendLine("$id,${median(wall)},${wall.min()},${wall.max()},${median(cpu)},${median(bytes)},${groups.flatten().maxOf { it.peak }}")
    }
}
println("PASS_PARALLEL_CHECK539,forks=3,cases=${expectedActive!!.size},samples=$samples")

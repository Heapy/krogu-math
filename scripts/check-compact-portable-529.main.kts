#!/usr/bin/env kotlinr
// Paired raw-log gate. It validates control coverage, not an exact retained graph.
import java.io.File

require(args.size == 5) { "baseline.log candidate.log samples retained-count jvm|js|wasm|native" }
val samples = args[2].toInt(); val retained = args[3].toInt(); val platform = args[4]
require(samples in 5..20 && retained in 1000..200000 && platform in listOf("jvm", "js", "wasm", "native"))
val cases = setOf("integer/create-small", "integer/create-min", "integer/add-small", "integer/multiply-small",
    "integer/wide-add", "integer/wide-multiply", "integer/wide-divide", "decimal/create-small", "decimal/create-min",
    "decimal/add-small", "decimal/multiply-small", "decimal/divide-context", "decimal/round", "decimal/setScale",
    "decimal/metadata", "decimal/wide-create", "decimal/wide-add", "decimal/wide-multiply", "decimal/wide-divide")
val cohorts = setOf("integer/small", "integer/min", "integer/wide", "decimal/small", "decimal/min", "decimal/wide",
    "control/bytes0", "control/bytes256", "control/ints64")
val counts = listOf(retained / 4, retained / 2, retained)
fun median(values: List<Double>): Double = values.sorted()[values.size / 2]
val checks = mutableListOf<List<String>>()
for ((index, path) in args.take(2).withIndex()) {
    val file = File(path).canonicalFile
    check(file.isFile)
    val lines = file.readLines()
    check(lines.lastOrNull() == "PASS compact529")
    val identity = lines.single { it.startsWith("# compact529;") }
    check(identity.contains("samples=$samples;") && identity.contains("retained_count=$retained"))
    val inputRows = lines.filter { it.startsWith("CHECK,") }
    val inputGroups = inputRows.map { it.split(',', limit = 4) }.groupBy { it[1] }
    check(inputGroups.keys == cases && inputGroups.values.all { rows -> rows.size == 16 && rows.map { it[2].toInt() }.sorted() == (0..15).toList() })
    checks += inputRows
    val timed = lines.filter { it.startsWith("TIME,") }.map { it.split(',') }
    check(timed.size == cases.size * samples && timed.all { row ->
        row.size == 7 && row[1] in cases && row[2].toIntOrNull()?.let { it in 0 until samples } == true &&
            row[3].toIntOrNull()?.let { it in 1..1_048_576 && it and (it - 1) == 0 } == true &&
            row[4].toLongOrNull()?.let { it in 1..180_000_000_000L } == true &&
            (if (platform == "jvm") row[5].toDoubleOrNull()?.let { it.isFinite() && it >= 0.0 } == true else row[5] == "NA") &&
            row[6].toIntOrNull() != null
    })
    check(timed.groupBy { it[1] }.keys == cases && timed.groupBy { it[1] }.values.all { rows -> rows.map { it[2].toInt() }.sorted() == (0 until samples).toList() })
    val memory = lines.filter { it.startsWith("RETAIN,") }.map { it.split(',') }
    val metric = when (platform) {
        "jvm" -> "jvm_total_heap_used_after_gc_request"
        "js" -> "node_heap_used_and_array_buffers_after_gc"
        "wasm" -> "wasm_node_heap_coverage_unverified_and_process_rss"
        else -> "native_last_gc_total_objects_size_bytes"
    }
    check(memory.size == cohorts.size * counts.size * 3 && memory.all { row ->
        row.size == 11 && row[1] in cohorts && row[2].toIntOrNull()?.let { it in 0..2 } == true && row[3].toIntOrNull()?.let { it in counts } == true && row[4] == metric &&
            (5..10).all { row[it] == "NA" || row[it].toLongOrNull()?.let { value -> value >= 0L } == true } &&
            row[5] != "NA" && row[6] != "NA" &&
            (platform != "js" || row[7] != "NA" && row[8] != "NA") &&
            (platform !in listOf("js", "wasm") || row[9] != "NA" && row[10] != "NA")
    })
    val groups = memory.groupBy { it[1] to it[3].toInt() }
    check(groups.keys == cohorts.flatMap { name -> counts.map { name to it } }.toSet())
    check(groups.values.all { rows -> rows.map { it[2].toInt() }.sorted() == listOf(0, 1, 2) })
    fun deltas(name: String, count: Int): List<Double> = groups.getValue(name to count).map { row ->
        val heap = row[6].toLong() - row[5].toLong()
        val arrays = if (platform == "js") row[8].toLong() - row[7].toLong() else 0L
        (heap + arrays).toDouble()
    }
    // Preregistered coverage diagnostic, not an exact byte-size acceptance band:
    // known256-byte payload must be distinguishable from header-only controls,
    // broadly proportional to N, in both larger cohorts. RSS is never substituted.
    val normalized = counts.takeLast(2).map { count ->
        val payload = deltas("control/bytes256", count); val empty = deltas("control/bytes0", count)
        val contrast = median(payload) - median(empty)
        val noise = maxOf(payload.max() - payload.min(), empty.max() - empty.min())
        println("COVERAGE,$index,$platform,$count,$contrast,$noise,${contrast / count}")
        check(contrast > noise && contrast / count in 128.0..512.0) {
            "UNVERIFIED retained-payload coverage: $platform/$count; raw observations retained, RSS is not a replacement"
        }
        contrast / count
    }
    check(normalized.max() / normalized.min() <= 1.5) { "UNVERIFIED size scaling" }
    println("VALIDATED,$index,$platform,19cases,81retentionRows,covered_payload_slope_NOT_exact_graph")
}
check(checks[0] == checks[1]) { "Paired full representation mismatch" }
println("PASS paired values/inventory/metrics/control coverage; independent graph ownership and source/artifact binding still required")

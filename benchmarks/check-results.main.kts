#!/usr/bin/env kotlinr
import java.io.File

val directory = File(args.single())
val csvFiles = directory.listFiles()!!.filter { it.name.matches(Regex("(jvm|js|wasm|native)-[0-9]+\\.csv")) }.sortedBy { it.name }
check(csvFiles.isNotEmpty()) { "No measurements" }
val environment = File(directory, "environment.txt").readText()
val config = Regex("forks=([0-9]+) warmup=([0-9]+) samples=([0-9]+) batch_ms=([0-9]+) platforms=\\[([^]]+)]").find(environment) ?: error("Missing run config")
val forks = config.groupValues[1].toInt()
val warmup = config.groupValues[2].toInt()
val samples = config.groupValues[3].toInt()
val batchMs = config.groupValues[4].toLong()
val platforms = config.groupValues[5].split(", ")
check(csvFiles.map { it.name }.toSet() == platforms.flatMap { platform -> (0 until forks).map { "$platform-$it.csv" } }.toSet()) { "Missing/extra forks" }
check(File(directory, "binding-verified.txt").exists()) { "Source/artifact binding was not verified" }
data class Row(val platform: String, val implementation: String, val name: String, val ns: Double, val allocation: String, val checksum: String, val inputMask: Int)
val all = mutableListOf<Row>()
for (file in csvFiles) {
    check(file.useLines { lines -> lines.first().contains("warmup=$warmup;samples=$samples;target_ns=${batchMs * 1_000_000L}") }) { "Arguments were not honored in $file" }
    val fork = file.nameWithoutExtension.substringAfterLast('-').toInt()
    val sampleIds = mutableMapOf<Pair<String, String>, MutableList<Int>>()
    val lines = file.readLines().filterNot { it.startsWith("#") }
    check(lines.first() == "implementation,workload,sample,operations,elapsed_ns,ns_per_op,ops_per_second,allocated_bytes_per_op,retained_heap_bytes,checksum,input_offset,input_mask")
    val rows = lines.drop(1).map { line ->
        val c = line.split(',')
        check(c.size == 12)
        sampleIds.getOrPut(c[0] to c[1]) { mutableListOf() }.add(c[2].toInt())
        check(c[3].toInt() >= 1 && c[4].toLong() > 0)
        check(c[5].toDouble().isFinite() && c[5].toDouble() > 0 && c[6].toDouble() > 0)
        check(kotlin.math.abs(c[5].toDouble() * c[6].toDouble() / 1e9 - 1) < 1e-9)
        check(c[7] == "NA" || c[7].toDouble() >= 0)
        check(c[8] == "NA" || c[8].toLong() >= 0)
        val offset = (fork * samples + c[2].toInt()) and 15
        check(c[10].toInt() == offset)
        val count = c[3].toInt()
        val expectedMask = if (count >= 16) 65535 else (0 until count).fold(0) { mask, i -> mask or (1 shl ((offset + i) and 15)) }
        check(c[11].toInt() == expectedMask) { "Input coverage mismatch in $file" }
        Row(file.name.substringBefore('-'), c[0], c[1], c[5].toDouble(), c[7], c[9], expectedMask)
    }
    val cases = rows.groupBy { it.implementation to it.name }
    check(cases.size == if (file.name.startsWith("jvm-")) 128 else 64) { "Missing cases in $file: ${cases.size}" }
    check(cases.values.all { it.size == samples })
    check(sampleIds.values.all { it.sorted() == (0 until samples).toList() }) { "Missing/duplicate sample IDs in $file" }
    all += rows
}
for ((name, rows) in all.groupBy { it.name }) check(rows.map { it.checksum }.distinct().size == 1) { "Digest mismatch: $name" }
fun median(values: List<Double>): Double = values.sorted().let { sorted ->
    if (sorted.size % 2 == 1) sorted[sorted.size / 2] else (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2
}
File(directory, "summary.csv").writeText("platform,implementation,workload,samples,median_ns_per_op,min_ns_per_op,max_ns_per_op,median_allocated_bytes_per_op,measured_input_count\n" +
    all.groupBy { Triple(it.platform, it.implementation, it.name) }.entries.sortedBy { it.key.toString() }.joinToString("\n", postfix = "\n") { (key, rows) ->
        val times = rows.map { it.ns }
        val allocations = rows.mapNotNull { it.allocation.toDoubleOrNull() }
        "${key.first},${key.second},${key.third},${rows.size},${median(times)},${times.min()},${times.max()},${if (allocations.isEmpty()) "NA" else median(allocations)},${rows.fold(0) { mask, row -> mask or row.inputMask }.countOneBits()}"
    })
println("Validated ${all.size} measurements in ${csvFiles.size} process forks; all workload digests agree across measured implementations/platforms. Wrote summary.csv")

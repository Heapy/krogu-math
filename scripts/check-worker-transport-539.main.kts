#!/usr/bin/env kotlinr

/** Strict read-only three-fork worker inventory/metric checker, not a policy selector. */
import java.io.File
import java.security.MessageDigest

require(args.size == 9) { "samples js|wasm manifestSHA256 nodeSHA256 captureScriptSHA256 new.csv forkDirectory1 forkDirectory2 forkDirectory3" }
val samples = args[0].toInt().also { require(it in 5..15) }
val kind = args[1].also { require(it in listOf("js", "wasm")) }
args.slice(2..4).forEach { require(it.matches(Regex("[a-f0-9]{64}"))) }
val output = File(args[5]).canonicalFile.also { require(!it.exists()) }
val forks = args.drop(6).map { File(it).canonicalFile }
require(forks.toSet().size == 3 && forks.all { it.isDirectory })
fun sha(file: File) = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
data class Row(val id: String, val sample: Int, val count: Long, val wall: Long, val cpu: Long, val heap: Long, val rss: Long, val maxRssKiB: Long)
val inventory = buildSet {
    for (size in listOf(256, 1024, 4096, 8192, 16384)) for (shape in listOf("distinct", "square", "ratio2"))
        for (variant in listOf("kernel", "frame", "empty", "copy", "worker1", "worker2")) add("$size/$shape/$variant")
}
val orders = mutableSetOf<String>()
val identities = mutableSetOf<String>()
val v8Versions = mutableSetOf<String>()
val allRows = mutableListOf<List<Row>>()
var expectedIds: Set<String>? = null
for (fork in forks) {
    val binding = File(fork, "binding.txt").readLines()
    fun bound(key: String) = binding.single { it.startsWith("$key=") }.substringAfter('=')
    check(bound("manifest_sha256") == args[2] && bound("node_sha256") == args[3] && bound("source_sha256") == args[4])
    check(bound("host_sha256") == sha(File(fork, "owned-host.mjs")))
    check(File(fork, "raw.log.exit").readText().trim() == "0")
    val lines = File(fork, "raw.log").readLines()
    val header = lines.single { it.startsWith("# worker539 ") }
    check("kind=$kind " in header && "node=v24.16.0 " in header && "samples=$samples " in header)
    val cpus = Regex("processors=([0-9]+)").find(header)!!.groupValues[1].toInt().also { check(it >= 1) }
    val order = Regex("order=(forward|reverse)").find(header)!!.groupValues[1]
    orders += order
    val identity = Regex("pid=([0-9]+) start_ms=([0-9]+)$").find(header)!!
    check(identity.groupValues[1].toLong() > 0 && identity.groupValues[2].toLong() > 0)
    check(identities.add(identity.value)) { "Repeated fork identity" }
    v8Versions += lines.single { it.startsWith("# v8=") }
    check(lines.count { it == "PASS_WORKER539" } == 1)
    val module = lines.single { it.startsWith("MODULE539,") }.split(',')
    check(module.size == 3 && module[1] == "parent_import_ms" && module[2].toDouble().let { it.isFinite() && it >= 0 })
    val startup = lines.single { it.startsWith("STARTUP539,") }.split(',')
    check(startup.size == 3 && startup[1].toInt() == minOf(2, cpus - 1) && startup[2].toLong() >= 0)
    val resource = lines.single { it.startsWith("POOL_RESOURCE539,") }.split(',')
    check(resource.size == 4 && resource[1].toLong() > 0 && resource[2].toLong() > 0 && resource[3].toInt() == minOf(2, cpus - 1))
    val ids = inventory.filter { id -> when (id.substringAfterLast('/')) {
        "worker2" -> cpus > 2
        "worker1", "empty", "copy" -> cpus > 1
        else -> true
    } }.toSet()
    if (expectedIds == null) expectedIds = ids else check(expectedIds == ids) { "Processor eligibility drift" }
    val skipped = lines.filter { it.startsWith("INELIGIBLE_WORKER539,") }.map {
        val p = it.split(','); check(p.size == 2); p[1]
    }
    check(skipped.size == skipped.toSet().size && skipped.toSet() == inventory - ids)
    val checks = lines.filter { it.startsWith("CHECKWORKER539,") }.map {
        val p = it.split(','); check(p.size == 3 && p[2] == "PASS"); p[1]
    }
    check(checks.size == ids.size && checks.toSet() == ids)
    val rows = lines.filter { it.startsWith("WORKER539,") }.map {
        val p = it.split(','); check(p.size == 9)
        Row(p[1], p[2].toInt(), p[3].toLong(), p[4].toLong(), p[5].toLong(), p[6].toLong(), p[7].toLong(), p[8].toLong()).also { row ->
            check(row.id in ids && row.sample in 0 until samples && row.count in 1..1_048_576)
            check(row.wall in 1..60_000_000_000L && row.cpu >= 0 && row.heap >= 0 && row.rss > 0 && row.maxRssKiB > 0)
        }
    }
    check(rows.size == ids.size * samples)
    for ((id, group) in rows.groupBy { it.id }) {
        check(group.size == samples && group.map { it.sample }.toSet() == (0 until samples).toSet()) { id }
        check(group.map { it.count }.toSet().size == 1)
    }
    check(rows.map { it.id }.toSet() == ids)
    allRows += rows
}
check(orders == setOf("forward", "reverse") && v8Versions.size == 1)
fun median(values: List<Double>): Double {
    val sorted = values.sorted(); val middle = sorted.size / 2
    return if (sorted.size % 2 == 1) sorted[middle] else (sorted[middle - 1] + sorted[middle]) / 2
}
output.bufferedWriter().use { writer ->
    writer.appendLine("case,wall_median_ns,wall_min_process_median_ns,wall_max_process_median_ns,cpu_median_ns,parent_heap_max_bytes,whole_process_rss_max_bytes,cumulative_process_highwater_max_kib")
    for (id in checkNotNull(expectedIds).sorted()) {
        val groups = allRows.map { it.filter { row -> row.id == id } }
        val wall = groups.map { median(it.map { row -> row.wall.toDouble() / row.count }) }
        val cpu = groups.map { median(it.map { row -> row.cpu.toDouble() / row.count }) }
        val rows = groups.flatten()
        writer.appendLine("$id,${median(wall)},${wall.min()},${wall.max()},${median(cpu)},${rows.maxOf { it.heap }},${rows.maxOf { it.rss }},${rows.maxOf { it.maxRssKiB }}")
    }
}
println("CHECKED_WORKER539_NOT_ADOPTED,forks=3,cases=${expectedIds!!.size},samples=$samples")

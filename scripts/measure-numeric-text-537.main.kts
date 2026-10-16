#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// baseline-root baseline.jar sha candidate-root candidate.jar sha probe.jar sha NEW-output
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.TimeUnit

require(args.size == 9)
check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS" && System.getProperty("java.vendor") == "BellSoft")
check(KotlinVersion.CURRENT.toString() == "2.4.21")
fun sha(file: File): String = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
    .joinToString("") { "%02x".format(it) }
val baselineRoot = File(args[0]).canonicalFile
val candidateRoot = File(args[3]).canonicalFile
val baseline = File(args[1]).canonicalFile
val candidate = File(args[4]).canonicalFile
val probe = File(args[6]).canonicalFile
val output = File(args[8]).canonicalFile
require(baselineRoot != candidateRoot && baseline != candidate && args[2] != args[5])
require(listOf(baselineRoot, candidateRoot).all { File(it, "src/commonMain").isDirectory })
require(listOf(baseline, candidate, probe).toSet().size == 3)
check(sha(baseline) == args[2] && sha(candidate) == args[5] && sha(probe) == args[7])
require(!output.exists()) { "Refusing overwrite $output" }
output.mkdirs()
val script = File(candidateRoot, "scripts/measure-numeric-text-537.main.kts")
fun sources(root: File): String = listOf("src/commonMain", "src/jvmMain").flatMap { directory ->
    File(root, directory).walkTopDown().filter { it.isFile }.toList()
}.sortedBy { it.relativeTo(root).path }.joinToString("\n", postfix = "\n") { "${sha(it)}  ${it.relativeTo(root).path}" }
val sourceBefore = listOf(baselineRoot, candidateRoot).map(::sources)
sourceBefore.forEachIndexed { index, text -> File(output, "${if (index == 0) "baseline" else "candidate"}.sources.sha256").writeText(text) }
val probeSource = File(candidateRoot, "src/jvmTest/kotlin/io/heapy/krogu/math/NumericText537Probe.kt")
val artifactBefore = listOf(baseline, candidate, probe, script, probeSource).associateWith(::sha)
File(output, "artifacts.sha256").writeText(artifactBefore.entries.joinToString("\n", postfix = "\n") { "${it.value}  ${it.key}" })
val expectedCases = buildSet {
    for (size in listOf(9, 77, 309, 1234)) {
        for (reader in listOf("string", "chars", "sequence")) add("integer/$size/parse-$reader")
        for (radix in listOf(3, 10, 16)) {
            add("integer/$size/cold-format-$radix")
            add("integer/$size/buffer-$radix")
        }
    }
    for (name in listOf("small", "wide", "negative-scale", "large-scale")) {
        for (reader in listOf("string", "chars", "sequence")) add("decimal/$name/parse-$reader")
        for (mode in listOf("cold-format", "construction-format", "cached-format-control",
            "buffer-canonical", "buffer-plain", "buffer-engineering")) add("decimal/$name/$mode")
    }
}
check(expectedCases.size == 72)
val java = File(System.getProperty("java.home"), "bin/java").path
val kotlinLib = File(System.getProperty("kotlin.home"), "lib")
val libraries = listOf("kotlin-stdlib.jar", "kotlin-stdlib-jdk7.jar", "kotlin-stdlib-jdk8.jar")
    .map { File(kotlinLib, it) }.filter { it.isFile }
check(libraries.any { it.name == "kotlin-stdlib.jar" })
val libraryBefore = libraries.associateWith(::sha)
File(output, "runtime-libraries.sha256").writeText(libraryBefore.entries.joinToString("\n", postfix = "\n") { "${it.value}  ${it.key}" })
val options = listOf("-Xms64m", "-Xmx512m", "-XX:+UseSerialGC", "-XX:ActiveProcessorCount=1")
File(output, "campaign.txt").writeText("start=${Instant.now()}\nforks=3 warmup=5 samples=5\noptions=$options\n" +
    "Cold pools are constructed outside counters. CPU contention and whole-process RSS are not per-operation retained size.\n")
fun stopOwned(process: Process): String {
    val handles = process.descendants().toList() + process.toHandle()
    handles.reversed().filter { it.isAlive }.forEach { it.destroy() }
    process.waitFor(3, TimeUnit.SECONDS)
    handles.filter { it.isAlive }.forEach { it.destroyForcibly() }
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
    for (handle in handles) {
        val left = deadline - System.nanoTime()
        if (handle.isAlive && left > 0) try { handle.onExit().get(left, TimeUnit.NANOSECONDS) }
        catch (_: java.util.concurrent.TimeoutException) { }
    }
    return "surviving_owned_pids=${handles.filter { it.isAlive }.map { it.pid() }} root_alive=${process.isAlive}"
}
fun capture(command: List<String>, destination: File) {
    val process = ProcessBuilder(command).redirectErrorStream(true).redirectOutput(destination).start()
    if (!process.waitFor(10, TimeUnit.SECONDS)) error("Host command timeout: ${stopOwned(process)}")
    check(process.exitValue() == 0)
}
fun host(prefix: String) {
    capture(listOf("uptime"), File(output, "$prefix.uptime.txt"))
    val raw = File(output, "$prefix.ps.tmp")
    capture(listOf("ps", "-axo", "pid,ppid,pcpu,pmem,comm"), raw)
    File(output, "$prefix.processes.txt").writeText(raw.readLines().filterIndexed { index, line ->
        index == 0 || (line.trim().split(Regex("\\s+"), limit = 5).getOrNull(2)?.toDoubleOrNull() ?: 0.0) >= 10
    }.joinToString("\n", postfix = "\n"))
    raw.delete()
    if (System.getProperty("os.name") == "Mac OS X")
        capture(listOf("sysctl", "vm.swapusage"), File(output, "$prefix.swap.txt"))
}
val started = System.nanoTime()
for (fork in 0..2) {
    val variants = listOf("baseline" to baseline, "candidate" to candidate).let { if (fork % 2 == 0) it else it.reversed() }
    for ((label, jar) in variants) {
        check(System.nanoTime() - started < TimeUnit.MINUTES.toNanos(20)) { "Campaign launch-admission budget exhausted; partials retained" }
        check(listOf(baselineRoot, candidateRoot).map(::sources) == sourceBefore)
        check((artifactBefore + libraryBefore).all { (file, hash) -> sha(file) == hash })
        val prefix = "$label-$fork"
        val classpath = (listOf(jar, probe) + libraries).joinToString(File.pathSeparator)
        val command = listOf(java) + options + listOf("-cp", classpath, "io.heapy.krogu.math.NumericText537Probe",
            label, jar.path, artifactBefore.getValue(jar), probe.path, args[7], "$fork", if (fork % 2 == 0) "forward" else "reverse")
        File(output, "$prefix.command.txt").writeText(command.joinToString("\n", postfix = "\n"))
        host("$prefix-before")
        val timing = if (System.getProperty("os.name") == "Mac OS X") listOf("/usr/bin/time", "-l") else listOf("/usr/bin/time", "-v")
        val builder = ProcessBuilder(timing + command).directory(candidateRoot)
            .redirectOutput(File(output, "$prefix.csv")).redirectError(File(output, "$prefix.process.txt"))
        listOf("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS").forEach { builder.environment().remove(it) }
        println("${Instant.now()} launch $prefix")
        val process = builder.start()
        println("$prefix pid=${process.pid()}")
        if (!process.waitFor(240, TimeUnit.SECONDS)) {
            val detail = stopOwned(process)
            File(output, "$prefix.timeout.txt").writeText(detail)
            error("Child deadline exceeded: $detail; partials retained without retry")
        }
        check(process.exitValue() == 0) { "$prefix exit=${process.exitValue()}; partials retained without retry" }
        host("$prefix-after")
        val rows = File(output, "$prefix.csv").readLines().filter { it.isNotBlank() && !it.startsWith('#') }
        check(rows.first() == "variant,fork,case,sample,operations,ns_per_op,allocated_bytes_per_op")
        val parsed = rows.drop(1).map { it.split(',') }
        check(parsed.size == expectedCases.size * 5)
        check(parsed.map { it[2] }.toSet() == expectedCases)
        check(parsed.map { it[2] to it[3] }.toSet().size == parsed.size)
        for (case in expectedCases) check(parsed.filter { it[2] == case }.map { it[3].toInt() }.toSet() == (0..4).toSet())
        check(parsed.all { it.size == 7 && it[0] == label && it[1] == "$fork" && it[4].toInt() in 1..2048 &&
            it[5].toDouble().let { n -> n.isFinite() && n > 0 } && it[6].toDouble().let { n -> n.isFinite() && n >= 0 } })
        check(File(output, "$prefix.csv").readText().contains("artifact_recheck=PASS cases=72 result_escape=PASS"))
        check((artifactBefore + libraryBefore).all { (file, hash) -> sha(file) == hash })
        check(listOf(baselineRoot, candidateRoot).map(::sources) == sourceBefore)
    }
}
File(output, "COMPLETE.txt").writeText("${Instant.now()} PASS: 2160 measurements, 6 fresh processes, 72 exact cases, source/artifact checks\n")
println("PASS: 2160 measurements; raw outputs retained in $output")

#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// Run from repository root. Reflects ONLY this project's own private context, never foreign bodies.
// --campaign baseline.jar baseline-sha candidate.jar candidate-sha NEW-output [forks=3] [warmup=5] [samples=5]
// Child processes are serial; each has a fixed 180s total deadline including script compilation.
import java.io.File
import java.lang.management.ManagementFactory
import java.net.URLClassLoader
import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.math.BigInteger as JdkInteger

fun sha(file: File): String = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
fun stopOwned(process: Process): String {
    val handles = process.descendants().toList() + process.toHandle()
    handles.reversed().forEach { if (it.isAlive) it.destroy() }
    process.waitFor(5, TimeUnit.SECONDS)
    handles.filter { it.isAlive }.forEach { it.destroyForcibly() }
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
    for (handle in handles) {
        val remaining = deadline - System.nanoTime()
        if (handle.isAlive && remaining > 0) {
            try { handle.onExit().get(remaining, TimeUnit.NANOSECONDS) } catch (_: java.util.concurrent.TimeoutException) { }
        }
    }
    return "surviving_owned_pids=${handles.filter { it.isAlive }.map { it.pid() }} root_alive=${process.isAlive}"
}
val requiredOptions = "-Xms64m -Xmx512m -XX:+UseSerialGC -XX:ActiveProcessorCount=1 -XX:-OmitStackTraceInFastThrow"
val root = File(".").canonicalFile
check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS" && System.getProperty("java.vendor") == "BellSoft")
check(KotlinVersion.CURRENT.toString() == "2.4.21")
require(args.isNotEmpty() && args[0] in listOf("--campaign", "--child"))
val expectedCases = buildSet<Pair<String, Int>> {
    for (limbs in listOf(1, 18, 142)) {
        for (shape in listOf("tiny", "dense127")) for (k in listOf(1, 4, 16, 64)) {
            add("public/$limbs/$shape/even-modPow" to k)
            for (mode in listOf("fresh-total", "reuse-total", "reuse-steady")) add("private/$limbs/$shape/$mode" to k)
            add("public/$limbs/$shape/modPow" to k)
        }
        add("private/$limbs/setup" to 1)
    }
    for (k in listOf(1, 4, 16, 64)) add("public/unit/modPow" to k)
    for (name in listOf("early-composite", "fermat", "mersenne127")) add("public/prime/$name" to 1)
}
check(expectedCases.size == 130)

if (args[0] == "--campaign") {
    require(args.size in 6..9)
    val variants = listOf("baseline" to (File(args[1]).canonicalFile to args[2]), "candidate" to (File(args[3]).canonicalFile to args[4]))
    require(variants[0].second.first != variants[1].second.first && variants[0].second.second != variants[1].second.second) {
        "Baseline and candidate must be distinct canonical files with distinct SHA-256 values"
    }
    variants.forEach { (_, pair) -> check(pair.first.isFile && sha(pair.first) == pair.second) }
    val output = File(args[5]).canonicalFile
    require(!output.exists()) { "Refusing overwrite: $output" }
    output.mkdirs()
    val forks = args.getOrNull(6)?.toInt() ?: 3
    val warmup = args.getOrNull(7)?.toInt() ?: 5
    val samples = args.getOrNull(8)?.toInt() ?: 5
    require(forks in 1..3 && warmup in 1..5 && samples in 1..5)
    val script = File(root, "scripts/measure-modulus-reuse-538.main.kts")
    val runner = File(System.getProperty("kotlin.home"), "bin/kotlinr").canonicalFile
    val started = System.nanoTime()
    val budgetNs = TimeUnit.SECONDS.toNanos(1200)
    File(output, "campaign.txt").writeText("start=${Instant.now()}\nscript_sha256=${sha(script)}\nforks=$forks warmup=$warmup samples=$samples\nJAVA_OPTS=$requiredOptions\n")
    fun capture(command: List<String>): String {
        val captureFile = File.createTempFile("host-capture-", ".txt", output)
        val remaining = (budgetNs - (System.nanoTime() - started)).coerceAtMost(TimeUnit.SECONDS.toNanos(10))
        check(remaining > 0) { "Campaign deadline exhausted before host capture" }
        val process = ProcessBuilder(command).redirectErrorStream(true).redirectOutput(captureFile).start()
        if (!process.waitFor(remaining, TimeUnit.NANOSECONDS)) {
            error("Host capture timeout: $command; ${stopOwned(process)}; output=$captureFile")
        }
        val text = captureFile.readText()
        captureFile.delete()
        check(process.exitValue() == 0) { "$command failed: $text" }
        return text.trim()
    }
    fun host(name: String) {
        File(output, "$name.host.txt").writeText(buildString {
            appendLine("timestamp=${Instant.now()}")
            appendLine(capture(listOf("uptime")))
            if (System.getProperty("os.name") == "Mac OS X") {
                appendLine(capture(listOf("sysctl", "vm.swapusage")))
                appendLine(capture(listOf("vm_stat")))
            } else appendLine(File("/proc/meminfo").readText())
            appendLine("CPU/executable snapshots >=10%, no argument lists:")
            capture(listOf("ps", "-axo", "pid,ppid,pcpu,pmem,comm")).lineSequence().drop(1).filter { line ->
                line.trim().split(Regex("\\s+"), limit = 5).getOrNull(2)?.toDoubleOrNull()?.let { it >= 10.0 } == true
            }.forEach { appendLine(it) }
        })
    }
    for (fork in 0 until forks) {
        val order = if (fork % 2 == 0) variants else variants.reversed()
        for ((label, pair) in order) {
            check(System.nanoTime() - started < budgetNs - TimeUnit.SECONDS.toNanos(190)) { "Campaign budget exhausted before next child" }
            val prefix = "$label-$fork"
            val command = listOf(runner.path, script.path, "--child", pair.first.path, pair.second,
                label, "$fork", "$warmup", "$samples", "protocol-538-v1")
            File(output, "$prefix.command.txt").writeText(command.joinToString("\n", postfix = "\n"))
            val timing = if (System.getProperty("os.name") == "Mac OS X") listOf("/usr/bin/time", "-l") else listOf("/usr/bin/time", "-v")
            val builder = ProcessBuilder(timing + command).directory(root)
                .redirectOutput(File(output, "$prefix.csv")).redirectError(File(output, "$prefix.process.txt"))
            val environment = builder.environment()
            listOf("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "KOTLIN_OPTS", "JAVACMD").forEach { environment.remove(it) }
            environment["JAVA_HOME"] = System.getProperty("java.home")
            environment["JAVA_OPTS"] = requiredOptions
            val cache = File(output, "$prefix-script-cache").apply { mkdirs() }
            environment["KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR"] = cache.path
            host("$prefix-before")
            println("${Instant.now()} launch $prefix")
            val childStart = System.nanoTime()
            val process = builder.start()
            println("$prefix pid=${process.pid()} start=${process.info().startInstant().orElse(null)}")
            val remainingNs = minOf(TimeUnit.SECONDS.toNanos(180) - (System.nanoTime() - childStart),
                budgetNs - (System.nanoTime() - started)).coerceAtLeast(0)
            if (!process.waitFor(remainingNs, TimeUnit.NANOSECONDS)) {
                // Actual ProcessHandle identities only: no names, broad signals, or foreign processes.
                val cleanup = stopOwned(process)
                File(output, "$prefix.timeout.txt").writeText("${Instant.now()} $cleanup\n")
                error("$prefix exceeded child/campaign deadline; $cleanup; partial output retained")
            }
            println("$prefix exit=${process.exitValue()}")
            check(process.exitValue() == 0) { "$prefix exit ${process.exitValue()}; partial output retained; no retry" }
            host("$prefix-after")
            check(sha(pair.first) == pair.second && sha(script) == File(output, "campaign.txt").readLines()[1].substringAfter('='))
        }
    }
    fun median(values: List<Double>): Double = values.sorted().let { sorted ->
        if (sorted.size % 2 == 1) sorted[sorted.size / 2] else (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2
    }
    val rows = output.listFiles()!!.filter { it.name.matches(Regex("(baseline|candidate)-[0-9]+\\.csv")) }.flatMap { file ->
        val lines = file.readLines()
        check(lines.last() == "# complete cases=130 samples=$samples") { "Incomplete measurement child: $file" }
        val observations = lines.filterNot { it.startsWith('#') }.drop(1).map { it.split(',') }
        check(observations.size == 130 * samples)
        val (expectedLabel, expectedFork) = file.nameWithoutExtension.split('-')
        observations.forEach { row ->
            check(row.size == 10 && row[0] == expectedLabel && row[1] == expectedFork)
            check(row[5].toInt() > 0 && row[6].toLong() > 0)
            check(row.drop(7).all { it.toDouble().isFinite() && it.toDouble() >= 0 })
        }
        val groups = observations.groupBy { it[2] to it[3] }
        check(groups.keys.map { it.first to it.second.toInt() }.toSet() == expectedCases)
        check(groups.values.all { group -> group.map { it[4].toInt() }.sorted() == (0 until samples).toList() })
        observations
    }
    check(rows.size == 2 * forks * 130 * samples)
    File(output, "summary.csv").writeText("variant,case,k,batches,median_ns_per_group,min_ns_per_group,max_ns_per_group,median_cpu_ns_per_group,median_allocated_bytes_per_group\n" +
        rows.groupBy { Triple(it[0], it[2], it[3]) }.entries.sortedBy { it.key.toString() }.joinToString("\n", postfix = "\n") { (key, values) ->
            check(values.size == forks * samples)
            val wall = values.map { it[7].toDouble() }
            "${key.first},${key.second},${key.third},${values.size},${median(wall)},${wall.min()},${wall.max()},${median(values.map { it[8].toDouble() })},${median(values.map { it[9].toDouble() })}"
        })
    File(output, "complete.txt").writeText("end=${Instant.now()}\nchildren=${2 * forks}\nall_exit=0\n")
} else {
    require(args.size == 8)
    val jar = File(args[1]).canonicalFile
    check(sha(jar) == args[2])
    val label = args[3]
    val fork = args[4].toInt()
    val warmup = args[5].toInt()
    val samples = args[6].toInt()
    require(label in listOf("baseline", "candidate") && fork in 0..2 && warmup in 1..5 && samples in 1..5)
    // Last argument is deliberately mandatory as a protocol/version guard.
    check(args[7] == "protocol-538-v1")
    val runtime = ManagementFactory.getRuntimeMXBean()
    val options = runtime.inputArguments
    requiredOptions.split(' ').forEach { option -> check(option in options) { "Missing $option: $options" } }
    check(Runtime.getRuntime().maxMemory() <= 512L * 1024 * 1024)
    check(Runtime.getRuntime().availableProcessors() == 1)
    check(ManagementFactory.getGarbageCollectorMXBeans().map { it.name }.toSet() == setOf("Copy", "MarkSweepCompact"))
    val thread = ManagementFactory.getThreadMXBean()
    check(thread.isCurrentThreadCpuTimeSupported)
    thread.isThreadCpuTimeEnabled = true
    val allocations = checkNotNull(thread as? com.sun.management.ThreadMXBean)
    check(allocations.isThreadAllocatedMemorySupported)
    allocations.isThreadAllocatedMemoryEnabled = true
    val threadId = Thread.currentThread().threadId()
    val loader = URLClassLoader(arrayOf(jar.toURI().toURL()), this::class.java.classLoader)
    val integer = Class.forName("io.heapy.krogu.math.BigInteger", true, loader)
    check(integer.classLoader === loader && File(integer.protectionDomain.codeSource.location.toURI()).canonicalFile == jar)
    val construct = integer.getConstructor(String::class.java)
    val publicPow = integer.getMethod("modPow", integer, integer)
    val prime = integer.getMethod("isProbablePrime", Int::class.javaPrimitiveType)
    val type = Class.forName("io.heapy.krogu.math.BigInteger\$OddModulusContext", true, loader)
    val companion = type.getDeclaredField("Companion").apply { isAccessible = true }.get(null)
    val create = companion.javaClass.getDeclaredMethod("create", integer).apply { isAccessible = true }
    val pow = type.getDeclaredMethod("pow", integer, integer, Int::class.javaPrimitiveType).apply { isAccessible = true }
    val sink = arrayOfNulls<Any>(64)
    data class Case(val name: String, val k: Int, val action: () -> Unit, val verify: () -> Unit)
    val cases = mutableListOf<Case>()
    for (limbs in listOf(1, 18, 142)) {
        val m = JdkInteger.ONE.shiftLeft(30 * limbs - 1).subtract(JdkInteger.ONE)
        val modulus = construct.newInstance(m.toString())
        for ((shape, a, e) in listOf(
            Triple("tiny", JdkInteger.TWO, JdkInteger.TWO),
            Triple("dense127", m.shiftRight(1).subtract(JdkInteger.TWO), JdkInteger.ONE.shiftLeft(127).subtract(JdkInteger.ONE))
        )) {
            val base = construct.newInstance(a.toString())
            val exponent = construct.newInstance(e.toString())
            val expected = a.modPow(e, m).toString()
            val context = checkNotNull(create.invoke(companion, modulus)) // initialization/preparation excluded
            val evenModulus = construct.newInstance(m.shiftLeft(1).toString())
            val evenExpected = a.modPow(e, m.shiftLeft(1)).toString()
            val bits = e.bitLength()
            for (k in listOf(1, 4, 16, 64)) {
                cases += Case("public/$limbs/$shape/even-modPow", k, {
                    repeat(k) { sink[it] = publicPow.invoke(base, exponent, evenModulus) }
                }, { repeat(k) { check(sink[it].toString() == evenExpected) } })
                val verify = { repeat(k) { check(sink[it].toString() == expected) }; Unit }
                cases += Case("private/$limbs/$shape/fresh-total", k, {
                    repeat(k) { sink[it] = pow.invoke(checkNotNull(create.invoke(companion, modulus)), base, exponent, bits) }
                }, verify)
                cases += Case("private/$limbs/$shape/reuse-total", k, {
                    val reused = checkNotNull(create.invoke(companion, modulus))
                    repeat(k) { sink[it] = pow.invoke(reused, base, exponent, bits) }
                }, verify)
                cases += Case("private/$limbs/$shape/reuse-steady", k, {
                    repeat(k) { sink[it] = pow.invoke(context, base, exponent, bits) }
                }, verify)
                cases += Case("public/$limbs/$shape/modPow", k, {
                    repeat(k) { sink[it] = publicPow.invoke(base, exponent, modulus) }
                }, verify)
            }
        }
        cases += Case("private/$limbs/setup", 1, { sink[0] = checkNotNull(create.invoke(companion, modulus)) }, { check(type.isInstance(sink[0])) })
    }
    val two = construct.newInstance("2")
    val unit = construct.newInstance("1")
    for (k in listOf(1, 4, 16, 64)) {
        cases += Case("public/unit/modPow", k, { repeat(k) { sink[it] = publicPow.invoke(two, two, unit) } },
            { repeat(k) { check(sink[it].toString() == "0") } })
    }
    for ((name, value, expected) in listOf("early-composite" to "3127", "fermat" to "65537", "mersenne127" to "170141183460469231731687303715884105727")
        .map { (name, text) -> Triple(name, construct.newInstance(text), JdkInteger(text).isProbablePrime(100)) }) {
        cases += Case("public/prime/$name", 1, { sink[0] = prime.invoke(value, 100) }, { check(sink[0] == expected) })
    }
    val ordered = if (fork % 2 == 0) cases else cases.reversed()
    check(cases.map { it.name to it.k }.toSet() == expectedCases && cases.size == expectedCases.size)
    println("# label=$label fork=$fork jar_sha256=${sha(jar)} runtime=${System.getProperty("java.runtime.version")} kotlin=${KotlinVersion.CURRENT} vm_args=$options")
    println("variant,fork,case,k,sample,groups,elapsed_ns,ns_per_group,cpu_ns_per_group,allocated_bytes_per_group")
    fun batch(case: Case, groups: Int) { repeat(groups) { case.action() } }
    for (case in ordered) {
        case.action(); case.verify()
        var groups = 1
        while (true) {
            val start = System.nanoTime(); batch(case, groups)
            if (System.nanoTime() - start >= 50_000_000L || groups >= 1_048_576) break
            groups *= 2
        }
        repeat(warmup) { batch(case, groups); case.verify() }
        repeat(samples) { sample ->
            val cpuStart = thread.currentThreadCpuTime
            val allocationStart = allocations.getThreadAllocatedBytes(threadId)
            val start = System.nanoTime()
            batch(case, groups)
            val elapsed = System.nanoTime() - start
            val allocated = allocations.getThreadAllocatedBytes(threadId) - allocationStart
            val cpu = thread.currentThreadCpuTime - cpuStart
            case.verify()
            println("$label,$fork,${case.name},${case.k},$sample,$groups,$elapsed,${elapsed.toDouble()/groups},${cpu.toDouble()/groups},${allocated.toDouble()/groups}")
        }
    }
    check(sha(jar) == args[2])
    println("# complete cases=${cases.size} samples=$samples")
    loader.close()
}

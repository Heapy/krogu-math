#!/usr/bin/env kotlin
// SPDX-License-Identifier: Apache-2.0
// Diagnostic preparation only; execution requires a later explicit parent grant.

import java.lang.management.ManagementFactory
import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import com.sun.management.ThreadMXBean as AllocationBean
import com.sun.management.OperatingSystemMXBean as MemoryBean
import kotlin.system.exitProcess

val pinnedJarSha = "daf2a13324f780b4b592e946c24dc601fc678d37f4954f0907fdbaea02674bdd"
val javaHome = Path.of("/Users/yoda/.sdkman/candidates/java/25.0.4-librca").toRealPath()
val kotlinHome = Path.of("/Users/yoda/.sdkman/candidates/kotlin/2.4.21").toRealPath()
val exponents = listOf(521, 607, 1279, 2203, 2281, 3217, 4253)
val startupNanos = TimeUnit.SECONDS.toNanos(30)
val cleanupNanos = TimeUnit.SECONDS.toNanos(10)
val wholeNanos = TimeUnit.MINUTES.toNanos(15)
val runtimeBean = ManagementFactory.getRuntimeMXBean()
// Includes this JVM's startup and script compilation, with millisecond uptime resolution.
val wholeStart = System.nanoTime() - TimeUnit.MILLISECONDS.toNanos(runtimeBean.uptime)
fun sha(path: Path): String {
    val digest = MessageDigest.getInstance("SHA-256")
    Files.newInputStream(path).use { input ->
        val bytes = ByteArray(65536)
        while (true) { val n = input.read(bytes); if (n < 0) break; digest.update(bytes, 0, n) }
    }
    return digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
}
fun hashText(value: String): String = MessageDigest.getInstance("SHA-256")
    .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it.toInt() and 255) }
fun cell(value: Any?): String = (value?.toString() ?: "NA").replace("\\", "\\\\")
    .replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n")
fun record(path: Path, vararg values: Any?) {
    Files.writeString(path, values.joinToString("\t") { cell(it) } + "\n",
        StandardOpenOption.CREATE, StandardOpenOption.APPEND)
}
fun regular(input: String): Path {
    val p = Path.of(input).toAbsolutePath().normalize()
    require(!Files.isSymbolicLink(p) && Files.isRegularFile(p)) { "Not a regular non-symlink file: $p" }
    require(p == p.toRealPath()) { "Symlinked source-directory path: $p" }
    return p
}
require(args.size % 2 == 0) { "Arguments must be --name value pairs; see docs/primality-profile-preparation.md" }
val options = args.toList().chunked(2).associate { (key, value) ->
    require(key.startsWith("--")); key.removePrefix("--") to value
}
require(options.size * 2 == args.size) { "Duplicate argument" }
val allowed = setOf("mode", "script", "runner-sha", "jar", "ledger", "ledger-sha", "grant", "evidence",
    "profile", "baseline", "exponent", "kind", "call-seconds", "attempt")
require(options.keys.all { it in allowed }) { "Unknown argument" }
fun required(name: String) = requireNotNull(options[name]) { "Missing --$name" }
fun flag(name: String) = (options[name] ?: "false").also { require(it == "true" || it == "false") }.toBoolean()
val mode = required("mode").also { require(it == "parent" || it == "child") }
val script = regular(required("script"))
val runnerSha = required("runner-sha")
require(sha(script) == runnerSha) { "Runner source hash mismatch" }
val jar = regular(required("jar"))
require(sha(jar) == pinnedJarSha) { "This preparation is pinned to the root1919 fresh own JAR" }
val ledger = regular(required("ledger"))
val ledgerSha = required("ledger-sha")
require(sha(ledger) == ledgerSha && ledgerSha.matches(Regex("[0-9a-f]{64}"))) { "Ledger hash mismatch" }
val grant = required("grant")
require(grant.matches(Regex("[A-Za-z0-9][A-Za-z0-9:._/-]{0,119}")) && !grant.contains("PLACEHOLDER", true)) {
    "An explicit later execution-grant identifier is required; this script does not grant execution"
}
require(Path.of(System.getProperty("java.home")).toRealPath() == javaHome) { "Wrong Java home" }
require(System.getProperty("java.runtime.version") == "25.0.4+9-LTS") { "Wrong Java runtime" }
require(KotlinVersion.CURRENT.toString() == "2.4.21") { "Wrong Kotlin runtime" }
require(Path.of(System.getProperty("kotlin.home")).toRealPath() == kotlinHome) { "Wrong Kotlin home" }
require(System.getProperty("java.vendor").contains("BellSoft")) { "Wrong Java vendor" }
val evidence = Path.of(required("evidence")).toAbsolutePath().normalize()
val jvmOptions = listOf("-Xms64m", "-Xmx512m", "-XX:+UseSerialGC", "-ea", "-Dfile.encoding=UTF-8")
val optionsSha = hashText(jvmOptions.joinToString("\n") + "\n${kotlinHome}/lib/*\norg.jetbrains.kotlin.runner.Main")
val cacheKey = hashText("$runnerSha\n$pinnedJarSha\n$ledgerSha\n$optionsSha")
val injectedEnvironment = listOf("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "JAVA_OPTS", "KOTLIN_OPTS", "CLASSPATH",
    "KOTLIN_RUNNER", "KOTLIN_COMPILER", "KOTLIN_COMPILER_ENVIRONMENT_KEEPALIVE")
require(injectedEnvironment.all { System.getenv(it).isNullOrEmpty() }) { "Injected runner/JVM/classpath environment is forbidden" }
val expectedCache = if (mode == "parent") "" else evidence.resolve("cache-$cacheKey-${required("attempt")}").toString()
require(System.getenv("KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR") == expectedCache)
require(System.getProperty("kotlin.main.kts.compiled.scripts.cache.dir") == expectedCache)
require(runtimeBean.inputArguments == jvmOptions + listOf("-Dkotlin.home=$kotlinHome", "-Dkotlin.main.kts.compiled.scripts.cache.dir=$expectedCache")) {
    "Actual JVM options differ from the pinned direct-launch command"
}

if (mode == "child") {
    // This branch never starts a process. The parent owns the one JVM directly.
    require(Files.isDirectory(evidence) && evidence == evidence.toRealPath())
    val attempt = required("attempt").also { require(it.matches(Regex("[a-z0-9-]+"))) }
    val out = evidence.resolve(attempt)
    require(Files.isDirectory(out) && out == out.toRealPath())
    val exponent = required("exponent").toInt().also { require(it in exponents) }
    val kind = required("kind").also { require(it in setOf("port", "profile", "baseline")) }
    val callSeconds = required("call-seconds").toLong()
    require(callSeconds == when (kind) { "profile" -> 90L; "baseline" -> 10L; else -> 60L })
    val metadata = out.resolve("child.tsv")
    record(metadata, "schema", "primality-profile-v1")
    record(metadata, "identity", ProcessHandle.current().pid(), ProcessHandle.current().info().startInstant().orElse(null))
    record(metadata, "runtime", System.getProperty("java.runtime.version"), KotlinVersion.CURRENT, runtimeBean.inputArguments)
    record(metadata, "provenance", runnerSha, pinnedJarSha, ledger, ledgerSha, optionsSha, grant)
    record(metadata, "random", "UNSEEDED", "port Random.Default has no seed parameter in this API")
    val parentLoader = kotlin.Unit::class.java.classLoader
    for (name in listOf("BigInteger", "BigDecimal", "MathContext", "RoundingMode")) {
        val fq = "io.heapy.krogu.math.$name"
        val present = try { Class.forName(fq, false, parentLoader); true } catch (_: ClassNotFoundException) { false }
        require(!present) { "Own math leaked onto parent classpath: $fq" }
    }
    val loader = URLClassLoader(arrayOf(jar.toUri().toURL()), parentLoader)
    val core = listOf("BigInteger", "BigDecimal", "MathContext", "RoundingMode").associateWith { name ->
        Class.forName("io.heapy.krogu.math.$name", true, loader).also { clazz ->
            require(clazz.classLoader === loader)
            val origin = Path.of(clazz.protectionDomain.codeSource.location.toURI()).toRealPath()
            val resource = clazz.getResource("${name}.class")?.toExternalForm()
            require(origin == jar && resource == "jar:${jar.toUri().toURL()}!/io/heapy/krogu/math/$name.class") {
                "Unexpected class origin: $name $origin $resource"
            }
            record(metadata, "class", clazz.name, origin, resource)
        }
    }
    val cls = if (kind == "baseline") java.math.BigInteger::class.java else core.getValue("BigInteger")
    val one = cls.getField("ONE").get(null)
    val fixture = cls.getMethod("subtract", cls).invoke(cls.getMethod("shiftLeft", Int::class.javaPrimitiveType).invoke(one, exponent), one)
    val prime = cls.getMethod("isProbablePrime", Int::class.javaPrimitiveType)
    record(metadata, "fixture", kind, exponent, "ONE.shiftLeft(p).subtract(ONE)", "certainty=100", "setup outside measured call")
    val bean = ManagementFactory.getThreadMXBean()
    val allocation = bean as? AllocationBean
    var allocationStatus = "NA:unsupported"
    try {
        if (allocation != null && allocation.isThreadAllocatedMemorySupported) {
            if (!allocation.isThreadAllocatedMemoryEnabled) allocation.isThreadAllocatedMemoryEnabled = true
            allocationStatus = if (allocation.isThreadAllocatedMemoryEnabled) "supported" else "NA:disabled"
        }
    } catch (t: Exception) { allocationStatus = "NA:${t.javaClass.name}" }
    var cpuStatus = "NA:unsupported"
    try {
        if (bean.isThreadCpuTimeSupported) {
            if (!bean.isThreadCpuTimeEnabled) bean.isThreadCpuTimeEnabled = true
            cpuStatus = if (bean.isThreadCpuTimeEnabled) "supported" else "NA:disabled"
        }
    } catch (t: Exception) { cpuStatus = "NA:${t.javaClass.name}" }
    fun allocated(id: Long) = if (allocationStatus == "supported") allocation!!.getThreadAllocatedBytes(id).takeIf { it >= 0 } else null
    fun cpu(id: Long) = if (cpuStatus == "supported") bean.getThreadCpuTime(id).takeIf { it >= 0 } else null
    fun gc() = ManagementFactory.getGarbageCollectorMXBeans().map { Triple(it.name, it.collectionCount, it.collectionTime) }
    val go = CountDownLatch(1)
    val finished = CountDownLatch(1)
    val release = CountDownLatch(1)
    val measuring = AtomicBoolean(false)
    val callWallStart = AtomicLong(-1)
    val callCpuStart = AtomicLong(-1)
    val callAllocationStart = AtomicLong(-1)
    val failure = AtomicReference<Throwable?>(null)
    val answer = AtomicReference<Boolean?>(null)
    var elapsed: Long? = null
    var cpuDelta: Long? = null
    var allocatedDelta: Long? = null
    val worker = Thread({
        try {
            go.await()
            val id = Thread.currentThread().threadId()
            measuring.set(true)
            val c0 = cpu(id)
            val a0 = allocated(id)
            callCpuStart.set(c0 ?: -1)
            callAllocationStart.set(a0 ?: -1)
            val t0 = System.nanoTime()
            callWallStart.set(t0)
            try { answer.set(prime.invoke(fixture, 100) as Boolean) }
            catch (t: Throwable) { failure.set(if (t is InvocationTargetException) t.targetException else t) }
            finally {
                elapsed = System.nanoTime() - t0
                val a1 = allocated(id)
                val c1 = cpu(id)
                cpuDelta = if (c0 != null && c1 != null && c1 >= c0) c1 - c0 else null
                allocatedDelta = if (a0 != null && a1 != null && a1 >= a0) a1 - a0 else null
                measuring.set(false)
                finished.countDown()
            }
            release.await() // Thread remains alive through all post-call counter/result observations.
        } catch (t: Throwable) { failure.set(t); finished.countDown() }
    }, "primality-computation")
    worker.isDaemon = true
    worker.start()
    record(metadata, "computation_thread", worker.threadId(), worker.name, cpuStatus, allocationStatus)
    Files.writeString(out.resolve("READY"), "ready\n", StandardOpenOption.CREATE_NEW)
    val waitStart = System.nanoTime()
    while (!Files.exists(out.resolve("GO"))) {
        require(System.nanoTime() - waitStart < startupNanos) { "Parent GO absent" }
        Thread.sleep(10)
    }
    val beforeGc = gc()
    val sampling = AtomicBoolean(kind == "profile")
    val ownCounts = linkedMapOf<String, Int>()
    val samples = out.resolve("stacks.tsv")
    val samplerFailure = AtomicReference<Throwable?>(null)
    val sampler = if (kind == "profile") Thread({
        try {
            Files.newBufferedWriter(samples, StandardOpenOption.CREATE_NEW).use { writer ->
                var count = 0
                var bytes = 0
                while (sampling.get() && count < 900) {
                    if (measuring.get()) {
                        val fullFrames = worker.stackTrace
                        val frames = fullFrames.take(32)
                        val now = System.nanoTime()
                        val partialCpu = cpu(worker.threadId())
                        val partialAllocation = allocated(worker.threadId())
                        val line = listOf(now, worker.state, "partial_not_final", callWallStart.get(),
                            callCpuStart.get().takeIf { it >= 0 }, partialCpu, callAllocationStart.get().takeIf { it >= 0 }, partialAllocation,
                            "frames_truncated=${fullFrames.size > 32}").joinToString("\t") { cell(it) } + "\t" +
                            frames.joinToString("|") { cell(it) } + "\n"
                        val size = line.toByteArray(Charsets.UTF_8).size
                        check(bytes + size <= 2 * 1024 * 1024) { "Stack capture byte cap exceeded" }
                        writer.write(line); bytes += size; count++
                        frames.filter { it.className.startsWith("io.heapy.krogu.math.") }
                            .map { "${it.className}.${it.methodName}:${it.lineNumber}" }.distinct().forEach {
                                ownCounts[it] = (ownCounts[it] ?: 0) + 1
                            }
                        if (count % 10 == 0) writer.flush()
                    }
                    Thread.sleep(100)
                }
                writer.flush()
            }
        } catch (t: Throwable) {
            samplerFailure.set(t)
            record(metadata, "sampler_error", t.javaClass.name, t.message?.take(4096))
        }
    }, "primality-stack-sampler").also { it.isDaemon = true; it.start() } else null
    go.countDown()
    // Parent enforces the authoritative deadline and retains partial output if this wait does not finish.
    finished.await()
    val afterGc = gc()
    sampling.set(false)
    sampler?.join(1500)
    record(metadata, "measurement", elapsed, cpuDelta, allocatedDelta, "ns,ns,bytes; NA is unsupported/unavailable")
    record(metadata, "gc_before", beforeGc)
    record(metadata, "gc_after", afterGc)
    beforeGc.forEach { before ->
        val after = afterGc.firstOrNull { it.first == before.first }
        record(metadata, "gc_delta", before.first,
            if (before.second >= 0 && after != null && after.second >= before.second) after.second - before.second else null,
            if (before.third >= 0 && after != null && after.third >= before.third) after.third - before.third else null,
            "process-wide count,milliseconds; NA when unavailable")
    }
    if (sampler?.isAlive != true) ownCounts.entries.sortedByDescending { it.value }.forEach {
        record(out.resolve("own-frame-frequency.tsv"), it.key, it.value)
    }
    val problem = failure.get()
    val sampleProblem = samplerFailure.get()
    val status = when {
        sampler?.isAlive == true -> "SAMPLER_NOT_TERMINAL"
        sampleProblem != null -> "SAMPLER_FAILURE"
        problem is OutOfMemoryError -> "CAUGHT_OOM"
        problem != null -> "EXCEPTION"
        answer.get() != true -> "FALSE"
        else -> "TRUE"
    }
    record(metadata, "result", status, answer.get(), problem?.javaClass?.name, problem?.message?.take(4096), sampleProblem?.toString()?.take(4096))
    Files.writeString(out.resolve("DONE"), "$status\t${answer.get()}\t${elapsed ?: "NA"}\n", StandardOpenOption.CREATE_NEW)
    release.countDown()
    worker.join(1000)
    loader.close()
    exitProcess(if (status == "TRUE" && !worker.isAlive) 0 else 2)
}

// Parent: disabling its compiled-script cache prevents silently reusing bootstrap bytecode.
require(System.getenv("KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR") == "") { "Disable parent script cache explicitly" }
require(System.getProperty("kotlin.main.kts.compiled.scripts.cache.dir") == "") { "Disable parent script cache property explicitly" }
require(!Files.exists(evidence)) { "Evidence path must be new (no resume/overwrite)" }
Files.createDirectories(evidence)
require(evidence == evidence.toRealPath()) { "Evidence path must not traverse symlinks" }
Files.copy(ledger, evidence.resolve("pinned-provenance-ledger"))
val summary = evidence.resolve("attempts.tsv")
fun hostMemory(path: Path, phase: String) {
    val bean = ManagementFactory.getOperatingSystemMXBean() as? MemoryBean
    fun measured(read: (MemoryBean) -> Long): Long? = try {
        bean?.let(read)?.takeIf { it >= 0 }
    } catch (_: Exception) { null } catch (_: InternalError) { null }
    record(path, "host_memory", phase, Instant.now(), System.nanoTime(),
        "total_physical_bytes", measured { it.totalMemorySize },
        "free_physical_bytes", measured { it.freeMemorySize },
        "total_swap_bytes", measured { it.totalSwapSpaceSize },
        "free_swap_bytes", measured { it.freeSwapSpaceSize },
        "parent_committed_virtual_bytes", measured { it.committedVirtualMemorySize },
        "parent_max_heap_bytes", Runtime.getRuntime().maxMemory(),
        "configured_parent_heap_mib", 512, "configured_child_heap_mib", 512,
        "note", "parent and child coexist; point sample, no reclaimable/uncontended inference; unsupported=NA")
}
record(evidence.resolve("run.tsv"), "schema", "primality-profile-v1", "diagnostic only; not public-corpus PASS")
record(evidence.resolve("run.tsv"), "provenance", script, runnerSha, jar, pinnedJarSha, ledger, ledgerSha, grant)
record(evidence.resolve("run.tsv"), "pins", javaHome, kotlinHome, System.getProperty("java.runtime.version"), optionsSha, cacheKey)
record(evidence.resolve("run.tsv"), "budgets", "startup=30s call=60s profile=90s baseline=10s cleanup=10s total=900s", "100ms polls plus observed metadata/IO latency; not hard realtime")
record(evidence.resolve("run.tsv"), "parent", ProcessHandle.current().pid(), ProcessHandle.current().info().startInstant().orElse(null), "uptime_ms", runtimeBean.uptime)
hostMemory(evidence.resolve("run.tsv"), "parent_startup")
data class Attempt(val exponent: Int, val status: String, val elapsed: Long?)
fun remainingWhole() = wholeNanos - (System.nanoTime() - wholeStart)
fun runAttempt(exponent: Int, kind: String, seconds: Long): Attempt {
    // Reserve startup, full call and cleanup; never shorten or enlarge a configured case deadline.
    if (remainingWhole() < startupNanos + TimeUnit.SECONDS.toNanos(seconds) + cleanupNanos) {
        record(summary, kind, exponent, "NOT_STARTED_WHOLE_BUDGET", "NA")
        return Attempt(exponent, "NOT_STARTED_WHOLE_BUDGET", null)
    }
    val attempt = "$kind-$exponent"
    val out = Files.createDirectory(evidence.resolve(attempt))
    val cache = Files.createDirectories(evidence.resolve("cache-$cacheKey-$attempt"))
    require(Files.list(cache).use { !it.findAny().isPresent })
    val command = mutableListOf(javaHome.resolve("bin/java").toString())
    command += jvmOptions
    command += listOf("-Dkotlin.home=$kotlinHome", "-Dkotlin.main.kts.compiled.scripts.cache.dir=$cache",
        "-cp", "$kotlinHome/lib/*", "org.jetbrains.kotlin.runner.Main", script.toString())
    val childOptions = linkedMapOf("mode" to "child", "script" to script.toString(), "runner-sha" to runnerSha,
        "jar" to jar.toString(), "ledger" to ledger.toString(), "ledger-sha" to ledgerSha, "grant" to grant,
        "evidence" to evidence.toString(), "attempt" to attempt, "exponent" to exponent.toString(),
        "kind" to kind, "call-seconds" to seconds.toString())
    childOptions.forEach { (key, value) -> command += listOf("--$key", value) }
    command.forEachIndexed { index, value -> record(out.resolve("command.tsv"), index, value) }
    val builder = ProcessBuilder(command)
    (injectedEnvironment + "KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR")
        .forEach { builder.environment().remove(it) }
    builder.environment()["JAVA_HOME"] = javaHome.toString()
    builder.environment()["KOTLIN_HOME"] = kotlinHome.toString()
    builder.environment()["KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR"] = cache.toString()
    val capturedHandle = AtomicReference<ProcessHandle?>(null)
    val capturedStart = AtomicReference<Instant?>(null)
    val stdout = AtomicReference<Thread?>(null)
    val stderr = AtomicReference<Thread?>(null)
    val truncated = AtomicBoolean(false)
    val readerProblem = AtomicReference<Throwable?>(null)
    fun reader(input: java.io.InputStream, name: String, target: AtomicReference<Thread?>) {
        val thread = Thread({
        try {
            Files.newOutputStream(out.resolve(name), StandardOpenOption.CREATE_NEW).use { destination ->
                input.use {
                    val bytes = ByteArray(8192)
                    var retained = 0
                    while (true) {
                        val n = it.read(bytes); if (n < 0) break
                        val keep = minOf(n, 2 * 1024 * 1024 - retained)
                        if (keep > 0) { destination.write(bytes, 0, keep); destination.flush(); retained += keep }
                        if (keep < n) truncated.set(true) // Continue draining until parent cleanup; preserve prefix.
                    }
                }
            }
        } catch (t: Throwable) { readerProblem.compareAndSet(null, t) }
        }, "capture-$attempt-$name")
        thread.isDaemon = true
        target.set(thread) // Retain even a thread whose start throws.
        thread.start()
    }
    var status = "STARTUP_FAILURE"
    var observedCall: Long? = null
    var goAt: Long? = null
    hostMemory(out.resolve("parent.tsv"), "before_child_launch")
    val launched = System.nanoTime()
    val process = try { builder.start() } catch (t: Exception) {
        record(out.resolve("parent.tsv"), "launch_failed", t.javaClass.name, t.message)
        record(summary, kind, exponent, "LAUNCH_FAILURE", "NA")
        return Attempt(exponent, "LAUNCH_FAILURE", null)
    }
    try {
        // All post-start metadata, evidence IO and reader creation are protected.
        val handle = process.toHandle()
        capturedHandle.set(handle)
        val start = handle.info().startInstant().orElse(null)
        capturedStart.set(start)
        record(out.resolve("parent.tsv"), "captured", handle.pid(), start, Instant.now(), "direct java child; no descendant discovery")
        reader(process.inputStream, "stdout.log", stdout)
        reader(process.errorStream, "stderr.log", stderr)
        while (process.isAlive && !Files.exists(out.resolve("READY")) && System.nanoTime() - launched < startupNanos && !truncated.get()) Thread.sleep(100)
        if (truncated.get()) status = "OUTPUT_TRUNCATED"
        else if (!Files.exists(out.resolve("READY"))) status = if (process.isAlive) "STARTUP_TIMEOUT" else "STARTUP_FAILURE"
        else if (System.nanoTime() - launched >= startupNanos) status = "STARTUP_TIMEOUT"
        else {
            val callStart = System.nanoTime()
            goAt = callStart
            Files.writeString(out.resolve("GO"), "go\n", StandardOpenOption.CREATE_NEW)
            while (process.isAlive && System.nanoTime() - callStart < TimeUnit.SECONDS.toNanos(seconds) && !truncated.get()) Thread.sleep(100)
            observedCall = System.nanoTime() - callStart
            status = when {
                truncated.get() -> "OUTPUT_TRUNCATED"
                process.isAlive -> "CALL_TIMEOUT"
                !Files.exists(out.resolve("DONE")) -> "CHILD_FAILURE"
                else -> Files.readString(out.resolve("DONE")).substringBefore('\t').trim()
            }
        }
    } finally {
        val cleanupStart = System.nanoTime()
        val cleanupProblems = mutableListOf<String>()
        fun cleanupAction(action: () -> Unit) {
            try { action() } catch (t: Throwable) { cleanupProblems += "${t.javaClass.name}:${t.message?.take(1024)}" }
        }
        fun sameIdentity(): Boolean = try {
            val handle = capturedHandle.get()
            val start = capturedStart.get()
            handle != null && start != null && handle.info().startInstant().orElse(null) == start
        } catch (t: Throwable) {
            cleanupProblems += "identity:${t.javaClass.name}:${t.message?.take(1024)}"
            false
        }
        var signalRefused = false
        if (process.isAlive) {
            if (sameIdentity()) {
                cleanupAction { process.destroy() }
                cleanupAction { process.waitFor(2, TimeUnit.SECONDS) }
                if (process.isAlive) {
                    if (sameIdentity()) cleanupAction { process.destroyForcibly() }
                    else signalRefused = true
                }
            } else signalRefused = true
        }
        cleanupAction {
            while (process.isAlive && System.nanoTime() - cleanupStart < TimeUnit.SECONDS.toNanos(7)) {
                process.waitFor(100, TimeUnit.MILLISECONDS)
            }
        }
        // Allow readers to drain a normal EOF before closing streams as a cleanup fallback.
        if (!process.isAlive) {
            cleanupAction { stdout.get()?.join(250) }
            cleanupAction { stderr.get()?.join(250) }
        }
        if (stdout.get() == null || stdout.get()?.isAlive == true) cleanupAction { process.inputStream.close() }
        if (stderr.get() == null || stderr.get()?.isAlive == true) cleanupAction { process.errorStream.close() }
        cleanupAction { process.outputStream.close() }
        val left = (cleanupNanos - (System.nanoTime() - cleanupStart)).coerceAtLeast(0)
        cleanupAction { stdout.get()?.join(minOf(TimeUnit.NANOSECONDS.toMillis(left), 1000).coerceAtLeast(1)) }
        val left2 = (cleanupNanos - (System.nanoTime() - cleanupStart)).coerceAtLeast(0)
        cleanupAction { stderr.get()?.join(minOf(TimeUnit.NANOSECONDS.toMillis(left2), 1000).coerceAtLeast(1)) }
        val terminal = !process.isAlive && stdout.get()?.isAlive != true && stderr.get()?.isAlive != true
        record(out.resolve("parent.tsv"), "terminal", terminal, "exit", if (process.isAlive) "NA" else process.exitValue(),
            "startup_observed_ns", (goAt ?: System.nanoTime()) - launched, "call_observed_ns", observedCall,
            "cleanup_observed_ns", System.nanoTime() - cleanupStart, "total_observed_ns", System.nanoTime() - launched,
            "truncated", truncated.get(), "reader_failure", readerProblem.get()?.toString(),
            "signal_refused", signalRefused, "cleanup_problems", cleanupProblems)
        check(!signalRefused) { "Captured process identity unavailable/changed; signals refused; stop and preserve owned evidence" }
        check(terminal) { "Owned invocation/capture not terminal; stop, preserve evidence, do not launch another" }
        check(cleanupProblems.isEmpty()) { "Cleanup operation failed; stop and preserve evidence" }
        check(System.nanoTime() - cleanupStart <= cleanupNanos) { "Cleanup budget exceeded; stop" }
    }
    if (truncated.get()) status = "OUTPUT_TRUNCATED"
    if (readerProblem.get() != null) status = "CAPTURE_FAILURE"
    if (status == "TRUE" && process.exitValue() != 0) status = "CHILD_FAILURE"
    val done = out.resolve("DONE")
    val elapsed = if (Files.exists(done)) Files.readString(done).trim().split('\t').getOrNull(2)?.toLongOrNull() else null
    record(summary, kind, exponent, status, elapsed, observedCall, "exit", process.exitValue())
    return Attempt(exponent, status, elapsed)
}
val attempts = exponents.map { runAttempt(it, "port", 60) }
val selected = attempts.firstOrNull { it.status == "CALL_TIMEOUT" }?.exponent
    ?: attempts.filter { it.elapsed != null }.maxByOrNull { it.elapsed!! }?.exponent
record(evidence.resolve("run.tsv"), "selection", selected, "smallest timed-out exponent else slowest completed measured call; multiple timeouts not speed-ranked")
val optional = mutableListOf<Attempt>()
if (selected != null && flag("profile")) optional += runAttempt(selected, "profile", 90)
if (selected != null && flag("baseline")) optional += runAttempt(selected, "baseline", 10)
require(sha(script) == runnerSha && sha(jar) == pinnedJarSha && sha(ledger) == ledgerSha) { "Input changed during campaign" }
check(remainingWhole() >= 0) { "Whole campaign budget exceeded" }
record(evidence.resolve("run.tsv"), "complete", true, "elapsed_from_parent_jvm_start_ns", System.nanoTime() - wholeStart,
    "scope", "diagnostic; public corpus and original deadlines unchanged")
exitProcess(if ((attempts + optional).all { it.status == "TRUE" }) 0 else 2)

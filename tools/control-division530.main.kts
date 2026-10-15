#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// SOURCE ONLY. Separate parent grants are required for preflight, check and campaign.
@file:CompilerOptions("-jvm-target", "25")

import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.TimeUnit

val operation = args.firstOrNull().also { require(it in setOf("preflight", "check", "campaign")) }
val keys = setOf("--grant", "--controller", "--controller-sha", "--driver", "--driver-sha", "--ledger", "--ledger-sha", "--out")
require(args.drop(1).size == keys.size * 2) { "Usage: preflight|check|campaign followed by exactly $keys key/value pairs" }
val pairs = args.drop(1).chunked(2).map { it[0] to it[1] }
require(pairs.map { it.first }.toSet() == keys && pairs.map { it.first }.distinct().size == pairs.size)
val options = pairs.toMap()
fun arg(key: String): String = options.getValue(key)
require(arg("--grant").startsWith("parent:") && arg("--grant").length > 7)
require(options.values.none { it.contains("PLACEHOLDER") || it.contains('<') || it.contains('\n') || it.contains('\t') })
fun exactPath(text: String): Path {
    val path = Path.of(text)
    require(path.isAbsolute && path.normalize() == path && path.toRealPath() == path) { "Canonical existing absolute path required: $text" }
    return path
}
val shaPattern = Regex("[0-9a-f]{64}")
fun fileSha(path: Path): String = Files.newInputStream(path).use { input ->
    val digest = MessageDigest.getInstance("SHA-256")
    val bytes = ByteArray(65536)
    while (true) { val count = input.read(bytes); if (count < 0) break; digest.update(bytes, 0, count) }
    digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
}
val controller = exactPath(arg("--controller"))
val controllerSha = arg("--controller-sha").also { require(shaPattern.matches(it)) }
val driver = exactPath(arg("--driver"))
val driverSha = arg("--driver-sha").also {
    require(it == "fd7c04af01aac3bb9fa2688fc4ff334831e772a178561d1d5a00a5917e46c218") { "Only the frozen accepted driver is in scope" }
}
val ledger = exactPath(arg("--ledger"))
val ledgerSha = arg("--ledger-sha").also { require(shaPattern.matches(it)) }
require(setOf(controller, driver, ledger).size == 3)
val pins = linkedMapOf(controller to controllerSha, driver to driverSha, ledger to ledgerSha)
fun checkPins() { for ((path, expected) in pins) check(fileSha(path) == expected) { "Pin mismatch: $path" } }
checkPins()
val ledgerPairs = Files.readAllLines(ledger).map { line ->
    val parts = line.split('\t'); require(parts.size == 2 && parts[1].isNotEmpty())
    require(!parts[1].contains("PLACEHOLDER") && !parts[1].contains('<'))
    parts[0] to parts[1]
}
require(ledgerPairs.map { it.first }.distinct().size == ledgerPairs.size)
val provenance = ledgerPairs.toMap()
val ledgerKeys = setOf("schema", "java.home", "kotlin.home", "build.contract", "source.delta", "source.delta.sha") +
    listOf("baseline", "candidate").flatMap { v -> listOf("$v.jar", "$v.jar.sha", "$v.source", "$v.inputs.before", "$v.inputs.after", "$v.inputs.sha", "$v.build.log", "$v.build.log.sha") }
require(provenance.keys == ledgerKeys)
check(provenance.getValue("schema") == "krogu-530-matched-v1")
val javaHome = exactPath(provenance.getValue("java.home"))
val kotlinHome = exactPath(provenance.getValue("kotlin.home"))
check(javaHome == Path.of("/Users/yoda/.sdkman/candidates/java/25.0.4-librca").toRealPath())
check(kotlinHome == Path.of("/Users/yoda/.sdkman/candidates/kotlin/2.4.21").toRealPath())
val vmFlags = listOf("-Xms32m", "-Xmx256m", "-XX:+UseSerialGC", "-XX:ActiveProcessorCount=1", "-XX:-OmitStackTraceInFastThrow")
val runnerFlags = listOf("--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow", "-Dkotlin.home=$kotlinHome")
check(ManagementFactory.getRuntimeMXBean().inputArguments == vmFlags + runnerFlags)
check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS")
check(System.getProperty("java.vendor").contains("BellSoft"))
check(Path.of(System.getProperty("java.home")).toRealPath() == javaHome)
check(System.getProperty("java.class.path") == kotlinHome.resolve("lib/kotlin-runner.jar").toString())
check(KotlinVersion.CURRENT.toString() == "2.4.21")
check(Runtime.getRuntime().maxMemory() in 200L * 1024 * 1024..256L * 1024 * 1024)
check(System.getenv("JAVA_OPTS") == vmFlags.joinToString(" "))
for (key in listOf("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "KOTLIN_OPTS", "JAVACMD"))
    check(System.getenv(key).isNullOrEmpty()) { "Injected $key" }
val out = exactPath(arg("--out"))
check(Files.list(out).use { !it.findAny().isPresent }) { "New empty controller evidence directory required" }
val ownCache = exactPath(checkNotNull(System.getenv("KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR")))
check(ownCache != out && !ownCache.startsWith(out))
check(!out.startsWith(ownCache))
fun emit(line: String) { println(line); System.out.flush() }
val current = ProcessHandle.current()
val currentStart = current.info().startInstant().orElseThrow()
emit("CONTROLLER_IDENTITY\tmode=$operation\tpid=${current.pid()}\tstart=$currentStart\tgrant=${arg("--grant")}\tcontroller=$controller\tcontrollerSha=$controllerSha\tdriver=$driver\tdriverSha=$driverSha\tledger=$ledger\tledgerSha=$ledgerSha")
emit("CONTROLLER_RUNTIME\tjava=${System.getProperty("java.runtime.version")}\tkotlin=${KotlinVersion.CURRENT}\tflags=${ManagementFactory.getRuntimeMXBean().inputArguments}\theap=${Runtime.getRuntime().maxMemory()}\tcache=$ownCache")

data class Owned(val handle: ProcessHandle, var start: Instant?, var parent: Long?, val discoveredFrom: Long, var terminal: Boolean = false)
enum class State { DEAD, SAME, CHANGED, UNKNOWN }
fun state(item: Owned): State {
    if (item.terminal || !item.handle.isAlive) return State.DEAD
    val actual = try { item.handle.info().startInstant().orElse(null) } catch (_: Exception) { null }
    return when { item.start == null || actual == null -> State.UNKNOWN; item.start == actual -> State.SAME; else -> State.CHANGED }
}
fun fields(line: String): Map<String, String> {
    val values = line.split('\t').drop(1).map { part ->
        require('=' in part); part.substringBefore('=') to part.substringAfter('=')
    }
    check(values.map { it.first }.distinct().size == values.size)
    return values.toMap()
}

fun supervise() {
    // All setup/pin checks occur BEFORE the driver clock starts. This controller's
    // own pre-entry compilation is a separate parent-supervised preparation concern.
    val driverOut = Files.createDirectory(out.resolve("driver-evidence"))
    val driverCache = Files.createDirectory(out.resolve("driver-script-cache"))
    val log = Files.createFile(out.resolve("driver.log"))
    val totalMs = if (operation == "campaign") 1200000L else 180000L
    val reserveMs = 15000L
    val clockStartPlaceholder = "LAUNCH_EPOCH_SET_IMMEDIATELY_BEFORE_START"
    val driverArguments = mutableListOf(if (operation == "campaign") "campaign" else "--check",
        "--grant", arg("--grant"), "--ledger", ledger.toString(), "--ledger-sha", ledgerSha,
        "--driver", driver.toString(), "--driver-sha", driverSha, "--out", driverOut.toString())
    if (operation == "campaign") driverArguments += listOf("--launch-epoch-ms", clockStartPlaceholder)
    val command = listOf(javaHome.resolve("bin/java").toString()) + vmFlags + runnerFlags +
        listOf("-cp", kotlinHome.resolve("lib/kotlin-runner.jar").toString(), "org.jetbrains.kotlin.runner.Main", driver.toString()) + driverArguments
    val builder = ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile())
    builder.environment().apply {
        for (key in listOf("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "KOTLIN_OPTS", "JAVACMD")) remove(key)
        put("JAVA_HOME", javaHome.toString()); put("JAVA_OPTS", vmFlags.joinToString(" "))
        put("KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR", driverCache.toString())
    }
    val epochIndex = if (operation == "campaign") builder.command().indexOf(clockStartPlaceholder).also { check(it >= 0) } else -1
    Files.writeString(out.resolve("input-pins.tsv"), pins.entries.joinToString("\n", postfix = "\n") { "${it.key}\t${it.value}" }, StandardOpenOption.CREATE_NEW)
    val owned = mutableListOf<Owned>()
    var direct: Process? = null
    var identityCaptured = false
    var launched = false
    var startNano: Long? = null
    var startEpoch: Long? = null
    var deadlineNano: Long? = null
    var deadlineEpoch: Long? = null
    var accepted = false
    var exit: Int? = null
    var failure: String? = null
    var release = false
    fun remainingMs(): Long {
        val nano = deadlineNano ?: return totalMs
        val epoch = checkNotNull(deadlineEpoch)
        return minOf(TimeUnit.NANOSECONDS.toMillis(nano - System.nanoTime()), epoch - System.currentTimeMillis())
    }
    fun seal(item: Owned, basis: String) {
        check(state(item) == State.DEAD)
        if (!item.terminal) {
            item.terminal = true
            emit("CONTROLLER_ACCOUNT\tpid=${item.handle.pid()}\tstart=${item.start}\tparent=${item.parent}\tdiscoveredFrom=${item.discoveredFrom}\tstate=DEAD\tbasis=$basis")
        }
    }
    fun remember(handle: ProcessHandle, from: Long) {
        val item = Owned(handle, null, null, from)
        owned += item // UNKNOWN is retained BEFORE any fallible identity metadata.
        item.start = handle.info().startInstant().orElse(null)
        item.parent = handle.parent().orElse(null)?.pid()
        val old = owned.lastOrNull { it !== item && !it.terminal && it.handle.pid() == handle.pid() }
        if (old != null) {
            when (state(old)) {
                State.DEAD -> seal(old, "old-identity-before-owned-PID-reuse")
                State.SAME -> {
                    check(item.start != null && item.start == old.start) { "Incoming identity does not match live captured PID: NO RELEASE" }
                    owned.remove(item) // Same validated identity was already captured.
                    return
                }
                State.CHANGED, State.UNKNOWN -> error("Prior PID identity unresolved: NO RELEASE")
            }
        }
        emit("CONTROLLER_OWNED\tpid=${handle.pid()}\tstart=${item.start}\tparent=${item.parent}\tdiscoveredFrom=$from")
        check(item.start != null || !handle.isAlive) { "Live descendant lacks start identity: NO RELEASE" }
    }
    fun census() {
        val problems = mutableListOf<String>()
        for (item in owned.toList()) {
            try {
                when (state(item)) {
                    State.DEAD -> seal(item, "positive-census")
                    State.SAME -> item.handle.descendants().use { entries -> entries.forEach { handle ->
                        try { remember(handle, item.handle.pid()) } catch (e: Exception) { problems += "register ${handle.pid()}: $e" }
                    } }
                    State.CHANGED, State.UNKNOWN -> problems += "Unresolved identity ${item.handle.pid()}"
                }
            } catch (e: Exception) { problems += "census ${item.handle.pid()}: $e" }
        }
        check(problems.isEmpty()) { "Incomplete owned census: $problems; NO RELEASE" }
    }
    fun cleanup(): List<String> {
        val problems = mutableListOf<String>()
        val allowance = minOf(12000L, maxOf(0L, remainingMs()))
        val stop = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(allowance)
        fun withinCleanupBudget(): Boolean = System.nanoTime() < stop && remainingMs() > 0
        var interrupted = false
        fun pause() {
            try { Thread.sleep(25) } catch (_: InterruptedException) {
                interrupted = true; problems += "Cleanup wait interrupted; terminal accounting continues"
            }
        }
        try { census() } catch (e: Exception) { problems += "cleanup census: $e" }
        for (item in owned.toList().asReversed()) {
            try { if (state(item) == State.SAME && withinCleanupBudget()) {
                emit("CONTROLLER_TERM\tpid=${item.handle.pid()}\tstart=${item.start}")
                item.handle.destroy()
            } } catch (e: Exception) { problems += "term ${item.handle.pid()}: $e" }
        }
        val grace = minOf(stop, System.nanoTime() + TimeUnit.SECONDS.toNanos(1))
        while (System.nanoTime() < grace && withinCleanupBudget() && owned.any { state(it) == State.SAME }) pause()
        try { census() } catch (e: Exception) { problems += "post-TERM census: $e" }
        for (item in owned.toList().asReversed()) {
            try { if (state(item) == State.SAME && withinCleanupBudget()) {
                emit("CONTROLLER_KILL\tpid=${item.handle.pid()}\tstart=${item.start}")
                item.handle.destroyForcibly()
            } } catch (e: Exception) { problems += "kill ${item.handle.pid()}: $e" }
        }
        while (withinCleanupBudget() && owned.any { state(it) == State.SAME }) pause()
        for (item in owned) {
            try {
                val final = state(item)
                if (final == State.DEAD) seal(item, "final-cleanup")
                else {
                    emit("CONTROLLER_ACCOUNT\tpid=${item.handle.pid()}\tstart=${item.start}\tparent=${item.parent}\tdiscoveredFrom=${item.discoveredFrom}\tstate=$final")
                    problems += "Unresolved ${item.handle.pid()}: $final; NO RELEASE"
                }
            } catch (e: Exception) { problems += "account ${item.handle.pid()}: $e" }
        }
        val process = direct
        if (process != null) {
            val alive = try { process.isAlive } catch (e: Exception) { problems += "direct status: $e"; null }
            val pid = try { process.pid() } catch (_: Exception) { null }
            emit("CONTROLLER_DIRECT_ACCOUNT\tpid=$pid\tref=${System.identityHashCode(process)}\talive=$alive\tidentityCaptured=$identityCaptured")
            if (alive != false || !identityCaptured) problems += "Direct Process not positively accounted: NO RELEASE"
        } else if (launched) problems += "Lost directly created Process reference: NO RELEASE"
        if (interrupted) Thread.currentThread().interrupt()
        return problems
    }
    try {
        checkPins()
        // All command/environment/output setup is complete. Sample the clock directly
        // before start, only inserting its epoch argument between sampling and start.
        startNano = System.nanoTime()
        startEpoch = System.currentTimeMillis()
        deadlineNano = checkNotNull(startNano) + TimeUnit.MILLISECONDS.toNanos(totalMs)
        deadlineEpoch = Math.addExact(checkNotNull(startEpoch), totalMs)
        if (epochIndex >= 0) builder.command()[epochIndex] = checkNotNull(startEpoch).toString()
        val process = builder.start()
        direct = process // FIRST post-start action: retain the actual Process, no metadata first.
        launched = true
        val handle = process.toHandle()
        val root = Owned(handle, null, current.pid(), current.pid())
        owned += root // Retain UNKNOWN before fetching the root start identity.
        root.start = handle.info().startInstant().orElse(null)
        identityCaptured = root.start != null
        emit("CONTROLLER_START\tmode=$operation\tpid=${handle.pid()}\tstart=${root.start}\tlaunchEpochMs=$startEpoch\tbudgetMs=$totalMs\tdeadlineEpochMs=$deadlineEpoch\tcleanupReserveMs=$reserveMs\tlog=$log\tdriverCache=$driverCache\targv=${builder.command().joinToString("|")}")
        check(identityCaptured) { "Direct root start identity missing: NO RELEASE" }
        while (process.isAlive) {
            census()
            check(remainingMs() > reserveMs) { "First driver deadline reached; cleanup uses remaining shared budget" }
            process.waitFor(50, TimeUnit.MILLISECONDS)
        }
        census()
        check(owned.all { state(it) == State.DEAD }) { "Captured descendant survived direct driver exit" }
        check(!process.isAlive)
        exit = process.exitValue()
        emit("CONTROLLER_DRIVER_EXIT\texit=$exit\tremainingMs=${remainingMs()}\tlogSha=${fileSha(log)}")
        check(exit == 0) { "Driver failed; no retry" }
        val lines = Files.readAllLines(log)
        val identity = fields(lines.single { it.startsWith("IDENTITY\t") })
        check(identity.getValue("mode") == if (operation == "campaign") "campaign" else "check")
        check(identity.getValue("grant") == arg("--grant"))
        check(owned.any { it.handle.pid().toString() == identity.getValue("pid") && it.start?.toString() == identity.getValue("start") }) { "Driver script identity not captured in owned lineage: NO RELEASE" }
        if (operation == "check") {
            check(lines.filter { it.startsWith("CHECK_COMPLETE\t") } == listOf("CHECK_COMPLETE\tinvokeExactObjectBoundary=true\tbenchmarkRuns=false\tnumericalCalls=false\tclassLoadingOfPort=false"))
            check(lines.none { it.startsWith("SAMPLE\t") || it.startsWith("CAMPAIGN_TERMINAL\t") || it.startsWith("LOADED\t") })
        } else {
            val terminal = fields(lines.single { it.startsWith("CAMPAIGN_TERMINAL\t") })
            check(terminal.keys == setOf("launched", "completed", "retainedRows", "failure", "remainingMs", "samplesSha", "pairedTotalsSha"))
            check(terminal.getValue("launched") == "12" && terminal.getValue("completed") == "12" && terminal.getValue("retainedRows") == "4320" && terminal.getValue("failure") == "null" && terminal.getValue("remainingMs").toLong() >= 0)
            check(fileSha(driverOut.resolve("samples.tsv")) == terminal.getValue("samplesSha"))
            check(fileSha(driverOut.resolve("paired-totals.tsv")) == terminal.getValue("pairedTotalsSha"))
            check(Files.lines(driverOut.resolve("samples.tsv")).use { it.count() } == 4321L)
            check(Files.lines(driverOut.resolve("paired-totals.tsv")).use { it.count() } == 541L)
        }
        checkPins()
        check(remainingMs() >= reserveMs) { "Postprocessing exhausted cleanup reserve" }
        accepted = true
    } catch (e: Throwable) {
        failure = e.toString(); emit("CONTROLLER_FIRST_FAILURE\t$failure")
    } finally {
        val problems = cleanup()
        if (problems.isNotEmpty()) failure = "${failure ?: "cleanup"}; $problems; NO RELEASE"
        try { checkPins() } catch (e: Exception) { failure = "${failure ?: "pins"}; $e" }
        val logSha = fileSha(log)
        if (remainingMs() < 0) failure = "${failure ?: "postprocessing"}; shared absolute deadline exceeded"
        release = launched && identityCaptured && accepted && exit == 0 && failure == null && direct?.isAlive == false && owned.all { it.terminal }
        emit("CONTROLLER_TERMINAL\tmode=$operation\tlaunched=$launched\taccepted=$accepted\texit=$exit\townedRecords=${owned.size}\tallCapturedTerminal=${owned.all { it.terminal }}\tdirectIdentityCaptured=$identityCaptured\tremainingMs=${remainingMs()}\tlogSha=$logSha\tfailure=$failure\trelease=$release")
    }
    check(release) { "Controller did not release; preserve all evidence and account unresolved identities; no automatic retry: $failure" }
}

if (operation == "preflight") {
    checkPins()
    emit("CONTROLLER_PREFLIGHT_COMPLETE\tlaunched=false\tnumericalCalls=false\tchecks=script-and-ledger-file-pins-ledger-schema-runtime-flags-output-and-cache-paths\tprocess-lifecycle-not-exercised=true\tdriver-not-run=true")
} else supervise()

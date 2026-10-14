#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// Controlled jtreg adaptation: parsing and planning are metadata-only.
import java.io.File
import java.util.concurrent.TimeUnit

fun mathMemorySnapshot(): Map<String, Long> {
    val bean = java.lang.management.ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean
    return mapOf("totalBytes" to bean.totalMemorySize, "freeBytes" to bean.freeMemorySize, "checkedAtEpochMillis" to System.currentTimeMillis())
}

data class MathRun(
    val runner: String, val className: String, val vm: List<String>, val arguments: List<String>,
    val timeoutSeconds: Long, val raw: String, val requires: List<String>, val modules: List<String>,
)

fun upstreamDisabledMathMethods(text: String): List<String> = Regex(
    "@Test\\(([^)]*)\\)\\s*public\\s+void\\s+(\\w+)\\s*\\(",
).findAll(text).filter { Regex("\\benabled\\s*=\\s*false\\b").containsMatchIn(it.groupValues[1]) }
    .map { it.groupValues[2] }.toList()

fun upstreamOmittedMathCalls(text: String): List<String> = Regex("(?m)^\\s*//\\s*(test\\w+)\\(\\);\\s*$")
    .findAll(text).map { it.groupValues[1] }.toList()

fun mathRuns(text: String, fallbackClass: String): List<MathRun> {
    val testBlocks = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).findAll(text)
        .map { it.value }.filter { it.contains("@test") }.toList()
    check(testBlocks.isNotEmpty()) { "No jtreg test block: $fallbackClass" }
    return testBlocks.flatMap { block ->
        val lines = block.lines().map { it.trim().removePrefix("*").trim() }
        // Retain continuation lines and reject directives outside the supported pinned corpus.
        val directives = mutableListOf<Pair<String, String>>()
        for (line in lines) {
            val match = Regex("^@([A-Za-z]+)(?:\\s+(.*))?$").matchEntire(line)
            if (match != null) directives += match.groupValues[1] to match.groupValues[2]
            else if (directives.isNotEmpty() && line.isNotEmpty() && line != "/" && line != "/*") {
                val last = directives.last()
                directives[directives.lastIndex] = last.first to (last.second + " " + line)
            }
        }
        check(directives.all { it.first in setOf("test", "bug", "summary", "author", "key", "library", "build", "run", "requires", "modules") }) {
            "Unsupported jtreg directive in $fallbackClass"
        }
        val requires = directives.filter { it.first == "requires" }.map { it.second }
        val modules = directives.filter { it.first == "modules" }.flatMap { it.second.split(Regex("\\s+")) }
        check(modules.all { it == "jdk.incubator.vector" }) { "Unreviewed module: $modules" }
        val libraries = directives.filter { it.first == "library" }.map { it.second }
        check(libraries.all { it in setOf("/test/lib", "..") }) { "Unreviewed @library: $libraries" }
        val builds = directives.filter { it.first == "build" }.map { it.second }
        check(builds.all { it in setOf("jdk.test.lib.RandomFactory", "java.base/java.math.Accessor") }) { "Unreviewed @build: $builds" }
        val runs = directives.filter { it.first == "run" }.map { it.second }.ifEmpty {
            check(Regex("public\\s+static\\s+void\\s+main\\s*\\(").containsMatchIn(text)) { "No implicit main: $fallbackClass" }
            listOf("main $fallbackClass")
        }
        runs.map { raw ->
            val tokens = raw.split(Regex("\\s+"))
            val spec = tokens.first().split('/')
            check(spec.first() in setOf("main", "junit", "testng")) { "Unsupported runner: $raw" }
            var timeout = 120L // jtreg default, retained rather than shortened.
            for (option in spec.drop(1)) {
                when {
                    option == "othervm" -> Unit // all invocations use fresh, isolated JVMs.
                    option.startsWith("timeout=") -> timeout = option.substringAfter('=').toLong()
                    else -> error("Unsupported runner option: $raw")
                }
            }
            val classIndex = tokens.indexOfFirst { !it.startsWith('-') && it != tokens.first() }
            check(classIndex > 0) { "Missing class: $raw" }
            MathRun(spec.first(), tokens[classIndex], tokens.subList(1, classIndex), tokens.drop(classIndex + 1), timeout, raw, requires, modules)
        }
    }
}

fun mathHeapMiB(flags: List<String>, resource: String): Int {
    val explicit = flags.singleOrNull { it.startsWith("-Xmx") }
    if (explicit != null) {
        val match = requireNotNull(Regex("-Xmx(\\d+)([mgMG])").matchEntire(explicit)) { "Unsupported heap: $explicit" }
        return match.groupValues[1].toInt() * if (match.groupValues[2].equals("g", true)) 1024 else 1
    }
    return when (resource) {
        "large-memory-8g", "large-memory-10g" -> 8192
        "large-memory-4g" -> 4096
        else -> 512
    }
}

fun mathResourceBarrier(run: MathRun, resource: String, profiles: Set<String>, maxHeapMiB: Int, osMemoryGiB: Double, is64Bit: Boolean): String? {
    if (resource !in profiles) return "Profile $resource not authorized by this invocation; coverage retained for an eligible run"
    val heap = mathHeapMiB(run.vm, resource)
    if (heap > maxHeapMiB) return "Required heap ${heap}MiB exceeds granted ${maxHeapMiB}MiB"
    for (requirement in run.requires) {
        val predicates = requirement.removePrefix("(").removeSuffix(")").split('&').map { it.trim() }
        for (predicate in predicates) {
            if (predicate == "sun.arch.data.model == \"64\"") {
                if (!is64Bit) return "Upstream requires a 64-bit VM"
            } else {
                val match = requireNotNull(Regex("os\\.maxMemory\\s*(>=|>)\\s*(\\d+)g").matchEntire(predicate)) { "Unreviewed @requires: $requirement" }
                val threshold = match.groupValues[2].toDouble()
                val eligible = if (match.groupValues[1] == ">") osMemoryGiB > threshold else osMemoryGiB >= threshold
                if (!eligible) return "Upstream @requires $requirement; actual declared OS memory ${osMemoryGiB}GiB"
            }
        }
    }
    return null
}

data class MathProcess(val command: List<String>, val exitCode: Int?, val timedOut: Boolean, val elapsedMillis: Long, val stdout: File, val stderr: File, val ownedDescendantPids: List<Long>)

fun mathProcess(command: List<String>, directory: File, logPrefix: File, timeoutSeconds: Long, allowOwnedTimeoutCleanup: Boolean): MathProcess {
    logPrefix.parentFile.mkdirs(); directory.mkdirs()
    val out = File(logPrefix.path + ".stdout.log")
    val err = File(logPrefix.path + ".stderr.log")
    val start = System.nanoTime()
    val builder = ProcessBuilder(command).directory(directory).redirectOutput(out).redirectError(err)
    // Ambient agents, classpaths and injected VM flags would invalidate pinned execution.
    listOf("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "CLASSPATH").forEach { builder.environment().remove(it) }
    val process = try { builder.start() } catch (failure: java.io.IOException) {
        out.writeText(""); err.writeText(failure.stackTraceToString())
        return MathProcess(command, -1, false, (System.nanoTime() - start) / 1_000_000, out, err, emptyList())
    }
    val children = linkedMapOf<Long, ProcessHandle>()
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds)
    var finished = false
    while (System.nanoTime() < deadline) {
        process.descendants().forEach { children[it.pid()] = it }
        if (process.waitFor(100, TimeUnit.MILLISECONDS)) { finished = true; break }
    }
    val orphaned = children.values.any { it.isAlive }
    if (!finished || orphaned) {
        check(allowOwnedTimeoutCleanup) { "Owned process timed out; cleanup was not authorized; PID=${process.pid()}" }
        process.descendants().forEach { children[it.pid()] = it }
        children.values.filter { it.isAlive }.forEach { it.destroy() }
        process.destroy()
        if (!process.waitFor(5, TimeUnit.SECONDS) || children.values.any { it.isAlive }) {
            children.values.filter { it.isAlive }.forEach { it.destroyForcibly() }
            process.destroyForcibly()
            process.waitFor()
        }
        for (child in children.values) if (child.isAlive) child.onExit().get(5, TimeUnit.SECONDS)
    }
    return MathProcess(command, if (finished) process.exitValue() else null, !finished || orphaned,
        (System.nanoTime() - start) / 1_000_000, out, err, children.keys.toList())
}

fun mathProcessRecord(process: MathProcess): Map<String, Any?> = linkedMapOf(
    "command" to process.command, "exitCode" to process.exitCode, "timedOut" to process.timedOut,
    "elapsedMillis" to process.elapsedMillis, "stdout" to process.stdout.path, "stderr" to process.stderr.path,
    "ownedDescendantPids" to process.ownedDescendantPids,
)

fun auditMathBytecode(javap: String, classes: File, className: String, directory: File, allowOwnedTimeoutCleanup: Boolean): Pair<Boolean, List<Map<String, Any?>>> {
    val records = mutableListOf<Map<String, Any?>>()
    var valid = true
    var coreReferences = 0
    val compiled = classes.listFiles().orEmpty().filter {
        it.extension == "class" && (it.nameWithoutExtension == className || it.nameWithoutExtension.startsWith(className + "$"))
    }.sortedBy { it.name }
    check(compiled.isNotEmpty()) { "No compiled test classes to audit" }
    for (file in compiled) {
        val dump = mathProcess(listOf(javap, "-J-Xmx256m", "-c", "-verbose", "-p", file.path), directory,
            File(directory, "bytecode-${file.name}"), 120, allowOwnedTimeoutCleanup)
        val text = dump.stdout.readText()
        val hostMembers = text.lines().filter { Regex("(?:Methodref|InterfaceMethodref|Fieldref).*// java/math/").containsMatchIn(it) }
        val coreMembers = text.lines().filter { Regex("(?:Methodref|InterfaceMethodref|Fieldref).*// io/heapy/krogu/math/").containsMatchIn(it) }
        val bridgeMembers = text.lines().filter { Regex("(?:Methodref|InterfaceMethodref|Fieldref).*// mathharness/HostBridge\\.").containsMatchIn(it) }
        val expectedBridge = when (className) {
            "ModPow65537" -> "mathharness/HostBridge.rsaExponent:"
            "DoubleFloatValueTests" -> "mathharness/HostBridge.decimal:"
            else -> null
        }
        if (bridgeMembers.any { expectedBridge == null || !it.contains(expectedBridge) }) valid = false
        coreReferences += coreMembers.size
        if (dump.exitCode != 0 || dump.timedOut || hostMembers.isNotEmpty()) valid = false
        records += mathProcessRecord(dump) + mapOf("hostMathMembers" to hostMembers, "coreMathMembers" to coreMembers, "hostIntegrationBridgeMembers" to bridgeMembers)
    }
    return (valid && coreReferences > 0) to records
}

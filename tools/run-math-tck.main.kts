#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/**
 * Preparation for public OpenJDK math tests, NOT official JCK/TCK evidence.
 * --prepare/--verify/--plan are metadata-only. --baseline/--port/--both execute
 * only with an explicit execution-window attestation; code-only grants do not qualify.
 * All downloaded/derived sources and output stay in ignored build/math-tck.
 */
@file:Import("math-tck/adapt.main.kts")
@file:Import("math-tck/runner.main.kts")

import java.io.File
import java.net.URI
import java.security.MessageDigest
import java.util.zip.ZipFile
import java.util.Properties

val revision = "7b65d74bcbd7c3a0b4c1f5c5bd85e64aa2e51b4a"
val tag = "jdk-25.0.4.1-ga"
val root = File(".").canonicalFile
val work = File(root, "build/math-tck").canonicalFile
val mode = args.firstOrNull() ?: "--prepare"
require(mode in setOf("--prepare", "--verify", "--plan", "--baseline", "--port", "--both")) { "Unknown mode: $mode" }
val options = linkedMapOf<String, String>()
var argumentIndex = 1
while (argumentIndex < args.size) {
    val name = args[argumentIndex++]
    require(name in setOf("--port-jar", "--profiles", "--max-heap-mib", "--os-memory-gib", "--execution-grant", "--ci-execution", "--tests", "--allow-owned-timeout-cleanup")) { "Unknown option: $name" }
    require(name !in options) { "Duplicate option: $name" }
    options[name] = if (name == "--allow-owned-timeout-cleanup") "true" else {
        require(argumentIndex < args.size) { "Missing value: $name" }
        args[argumentIndex++]
    }
}
val execute = mode in setOf("--baseline", "--port", "--both")
val ciExecution = options["--ci-execution"]?.let { attestation ->
    require(execute && "--execution-grant" !in options) { "CI and parent-comment execution attestations are mutually exclusive" }
    fun environment(name: String) = requireNotNull(System.getenv(name)) { "Missing CI environment: $name" }
    require(environment("GITHUB_ACTIONS") == "true") { "--ci-execution requires GitHub Actions" }
    val runId = environment("GITHUB_RUN_ID").also { require(it.matches(Regex("[1-9][0-9]*"))) }
    val sha = environment("GITHUB_SHA").also { require(it.matches(Regex("[0-9a-f]{40}"))) }
    require(attestation == "$runId:$sha") { "CI attestation must equal GITHUB_RUN_ID:GITHUB_SHA" }
    val repository = environment("GITHUB_REPOSITORY").also { require(it.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"))) }
    val attempt = environment("GITHUB_RUN_ATTEMPT").also { require(it.matches(Regex("[1-9][0-9]*"))) }
    val workflowRef = environment("GITHUB_WORKFLOW_REF").also {
        require(it.startsWith("$repository/.github/workflows/ci.yml@") && it.substringAfterLast('@').isNotBlank())
    }
    val workflowSha = environment("GITHUB_WORKFLOW_SHA").also { require(it.matches(Regex("[0-9a-f]{40}"))) }
    val workflow = environment("GITHUB_WORKFLOW").also { require(it.isNotBlank()) }
    require(File(environment("GITHUB_WORKSPACE")).canonicalFile == root) { "CI must execute from the checked-out workspace" }
    require(environment("GITHUB_JOB") == "public-math") { "CI attestation is reserved for the public-math job" }
    mapOf("provider" to "github-actions", "runId" to runId, "runAttempt" to attempt, "sourceCommit" to sha,
        "repository" to repository, "workflow" to workflow,
        "workflowRef" to workflowRef, "workflowSha" to workflowSha,
        "job" to environment("GITHUB_JOB"), "attestation" to attestation)
}
if (execute) {
    if (ciExecution == null) {
        val grant = options["--execution-grant"].orEmpty()
        require(grant.matches(Regex("[0-9]+")) && grant != "1753") { "Explicit parent execution-grant comment ID required; comment1753 is CODE-ONLY" }
    }
    require(options["--allow-owned-timeout-cleanup"] == "true") { "Execution requires authorization for timeout cleanup of this runner's own child processes" }
    require(KotlinVersion.CURRENT.toString() == "2.4.21") { "Kotlin 2.4.21 required" }
}
val profiles = (options["--profiles"] ?: "standard,serialization").split(',').toSet()
require(profiles.isNotEmpty() && profiles.all { it in setOf("standard", "serialization", "heavy", "memory-512m", "large-memory-4g", "large-memory-8g", "large-memory-10g") })
val maxHeapMiB = (options["--max-heap-mib"] ?: "512").toInt().also { require(it > 0) }
val memoryArgument = options["--os-memory-gib"] ?: "0"
val autoMemory = if (memoryArgument == "auto") mathMemorySnapshot().also { require(it.getValue("totalBytes") > 0) { "Observed physical memory unavailable" } } else null
val osMemoryGiB = (autoMemory?.getValue("totalBytes")?.let { it.toDouble() / (1024.0 * 1024 * 1024) }
    ?: memoryArgument.toDouble()).also { require(it >= 0 && it.isFinite()) }
if (execute) require(osMemoryGiB > 0) { "Positive observed or parent-declared actual OS memory capacity is required" }
if (ciExecution != null) require(mode == "--both" && "--tests" !in options && memoryArgument == "auto" && maxHeapMiB == 512 &&
    profiles == setOf("standard", "serialization", "heavy", "memory-512m")) { "CI attestation only covers the complete public 512MiB profile selection" }
check(File(root, ".gitignore").readLines().contains("build/")) { "build/ must be ignored" }
check(work.toPath().startsWith(File(root, "build").canonicalFile.toPath()))
work.mkdirs()

fun digest(algorithm: String, bytes: ByteArray) = MessageDigest.getInstance(algorithm)
    .digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }
fun json(value: Any?): String = when (value) {
    null -> "null"
    is String -> "\"" + buildString {
        value.forEach { c -> append(when (c) {
            '\\' -> "\\\\"; '"' -> "\\\""; '\n' -> "\\n"; '\r' -> "\\r"; '\t' -> "\\t"
            else -> if (c.code < 32) "\\u%04x".format(c.code) else c.toString()
        }) }
    } + "\""
    is Number, is Boolean -> value.toString()
    is Map<*, *> -> value.entries.joinToString(",", "{", "}") { json(it.key.toString()) + ":" + json(it.value) }
    is Iterable<*> -> value.joinToString(",", "[", "]") { json(it) }
    else -> error("Unsupported JSON value")
}
fun readTable(name: String): List<Map<String, String>> {
    val lines = File(root, "tools/math-tck/$name").readLines()
    val columns = lines.first().split('\t')
    return lines.drop(1).filter { it.isNotBlank() }.map { row ->
        val fields = row.split('\t')
        check(fields.size == columns.size) { "Malformed $name row: $row" }
        columns.zip(fields).toMap()
    }
}
val lock = readTable("source-lock.tsv")
val manifest = readTable("manifest.tsv")
val paths = lock.map { it.getValue("path") }
check(paths.size == paths.toSet().size && paths.toSet() == manifest.map { it.getValue("path") }.toSet())
check(manifest.size == paths.size)
val decisions = manifest.associateBy { it.getValue("path") }
check(manifest.all { it["disposition"] in setOf("selected", "adapted", "excluded", "support") })
val inventory = mutableListOf<Map<String, Any?>>()
val prepared = mutableListOf<String>()
val sources = mutableMapOf<String, String>()
val adaptations = mutableMapOf<String, MathAdaptation>()
for (entry in lock) {
    val path = entry.getValue("path")
    check(path.startsWith("test/jdk/java/math/") || path == "test/lib/jdk/test/lib/RandomFactory.java") { "Forbidden path: $path" }
    check(path.matches(Regex("[A-Za-z0-9_./-]+")) && path.split('/').none { it == ".." })
    val target = File(work, "upstream/$path").canonicalFile
    check(target.toPath().startsWith(work.toPath())) { "Escaping output: $path" }
    if (!target.exists()) {
        check(mode != "--verify") { "Missing cached file: $path" }
        val connection = URI("https://raw.githubusercontent.com/openjdk/jdk25u/$revision/$path").toURL().openConnection().apply {
            connectTimeout = 20000; readTimeout = 30000
            setRequestProperty("User-Agent", "krogu-math-public-test-harness")
        }
        val downloaded = connection.getInputStream().use { it.readBytes() }
        check(digest("SHA-256", downloaded) == entry.getValue("sha256")) { "Downloaded checksum mismatch: $path" }
        target.parentFile.mkdirs(); target.writeBytes(downloaded)
    }
    val bytes = target.readBytes()
    check(bytes.size.toString() == entry.getValue("bytes")) { "Size mismatch: $path" }
    check(digest("SHA-256", bytes) == entry.getValue("sha256")) { "SHA-256 mismatch: $path" }
    check(digest("SHA-1", "blob ${bytes.size}\u0000".toByteArray() + bytes) == entry.getValue("gitBlobSha1")) { "Git blob mismatch: $path" }
    val text = bytes.toString(Charsets.UTF_8)
    sources[path] = text
    val header = text.substringBefore("*/") + "*/"
    check(header.contains("GNU General Public License version 2 only")) { "Unreviewed license: $path" }
    check(!header.contains("Classpath", ignoreCase = true)) { "Unreviewed license exception: $path" }
    val comments = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).findAll(text).map { it.value }.toList()
    val directives = comments.filter { it.contains("@test") }.map { block ->
        block.lines().map { it.trim().removePrefix("*").trim() }.filter { it != "/" && it != "/*" }.joinToString("\n").trim()
    }
    val imports = Regex("(?m)^import\\s+([^;]+);").findAll(text).map { it.groupValues[1] }.toList()
    // Audit aids, not a claim that lexical matches exhaust API coverage.
    val callNames = Regex("\\.([A-Za-z_$][A-Za-z0-9_$]*)\\s*\\(").findAll(text).map { it.groupValues[1] }.toSortedSet().toList()
    val propertyReads = Regex("System\\.getProperty\\(\"([^\"]+)\"").findAll(text).map { it.groupValues[1] }.toSortedSet().toList()
    val decision = decisions.getValue(path)
    val relative = path.removePrefix("test/jdk/java/math/")
    val sourceOnly = decision.getValue("disposition") in setOf("selected", "adapted")
    if (sourceOnly) {
        val adaptation = adaptMathTest(path, text)
        adaptations[path] = adaptation
        val rewritten = adaptation.source
        check(!Regex("(?m)^import\\s+(?:static\\s+)?java\\.math\\.").containsMatchIn(rewritten))
        check(rewritten.startsWith(header)) { "Notice changed: $path" }
        val generated = File(work, "generated/$relative").canonicalFile
        check(generated.toPath().startsWith(work.toPath())) { "Escaping generated output: $path" }
        if (mode == "--verify") check(generated.isFile && generated.readText() == rewritten) { "Generated source mismatch: $path" }
        else { generated.parentFile.mkdirs(); generated.writeText(rewritten) }
        val baseline = File(work, "baseline/$relative").canonicalFile
        check(baseline.toPath().startsWith(work.toPath()))
        val baselineSource = if (relative == "BigInteger/ModPowPowersof2.java") adaptMathChildOrchestration(text) else text
        if (mode == "--verify") check(baseline.isFile && baseline.readText() == baselineSource) { "Baseline source mismatch: $path" }
        else { baseline.parentFile.mkdirs(); baseline.writeText(baselineSource) }
        prepared += path
    }
    inventory += linkedMapOf(
        "path" to path, "gitBlobSha1" to entry.getValue("gitBlobSha1"), "sha256" to entry.getValue("sha256"),
        "bytes" to bytes.size, "license" to "GPL-2.0-only", "classpathException" to false,
        "copyright" to header.lines().filter { it.contains("Copyright") }.map { it.trim().removePrefix("*").trim() },
        "decision" to decision, "jtregBlocks" to directives, "imports" to imports, "lexicalCallNames" to callNames,
        "systemProperties" to propertyReads, "serialization" to text.contains("ObjectInputStream"),
        "randomFactory" to text.contains("RandomFactory"),
        "randomConstructors" to Regex("new Random\\([^)]*\\)").findAll(text).map { it.value }.toList(),
        "jdkDependentCallSites" to text.lines().mapIndexedNotNull { index, line ->
            if (Regex("Float16|SunRsaSign|RSAKeyGenParameterSpec|Accessor|MutableBigInteger").containsMatchIn(line))
                mapOf("line" to index + 1, "text" to line.trim()) else null
        },
        "classNames" to Regex("\\bclass\\s+(\\w+)").findAll(text).map { it.groupValues[1] }.toList(),
        "status" to "NOT_RUN",
        "upstreamDisabledMethods" to upstreamDisabledMathMethods(text),
        "upstreamCommentedCalls" to upstreamOmittedMathCalls(text),
        "adaptation" to adaptations[path]?.let { mapOf("changes" to it.changes, "companionCoverage" to it.companionSkips, "generatedSha256" to digest("SHA-256", it.source.toByteArray())) },
    )
}
val dependencies = readTable("dependency-lock.tsv")
val lib = File(work, "lib").apply { mkdirs() }
for (dependency in dependencies) {
    val path = dependency.getValue("path")
    check(path.matches(Regex("[A-Za-z0-9_./-]+")) && path.split('/').none { it == ".." })
    val file = File(lib, path.substringAfterLast('/')).canonicalFile
    check(file.toPath().startsWith(work.toPath()))
    if (!file.isFile) {
        check(mode != "--verify") { "Missing pinned dependency: $path" }
        val connection = URI("https://repo.maven.apache.org/maven2/$path").toURL().openConnection().apply { connectTimeout = 20000; readTimeout = 30000 }
        val bytes = connection.getInputStream().use { it.readBytes() }
        check(digest("SHA-256", bytes) == dependency.getValue("sha256")) { "Downloaded dependency checksum mismatch: $path" }
        file.writeBytes(bytes)
    }
    val bytes = file.readBytes()
    check(digest("SHA-256", bytes) == dependency.getValue("sha256")) { "Dependency SHA-256 mismatch: $path" }
    check(digest(dependency.getValue("officialAlgorithm"), bytes) == dependency.getValue("officialChecksum")) { "Official dependency checksum mismatch: $path" }
    if (file.extension == "jar" && mode != "--verify") ZipFile(file).use { zip ->
        val entries = zip.entries().asSequence().filter { !it.isDirectory && !it.name.endsWith(".class") && Regex("(?i)license|notice|copyright").containsMatchIn(it.name) }.toList()
        val notices = File(work, "notices/${file.name}").apply { mkdirs() }
        File(notices, "entries.json").writeText(json(entries.mapIndexed { index, entry -> mapOf("entry" to entry.name, "file" to "$index.txt") }))
        for ((index, entry) in entries.withIndex()) File(notices, "$index.txt").writeBytes(zip.getInputStream(entry).use { it.readBytes() })
    }
}
val selectedPaths = manifest.filter { it["runner"] != "none" }.map { it.getValue("path") }
val filteredPaths = options["--tests"]?.split(',')?.map { "test/jdk/java/math/$it" }?.toSet() ?: selectedPaths.toSet()
require(filteredPaths.isNotEmpty() && filteredPaths.all { it in selectedPaths }) { "--tests must name exact selected relative paths" }
val planned = selectedPaths.flatMap { path ->
    mathRuns(sources.getValue(path), File(path).nameWithoutExtension).mapIndexed { index, run ->
        val resource = decisions.getValue(path).getValue("resource")
        val barrier = if (path !in filteredPaths) "Filtered by --tests; public coverage not executed" else
            mathResourceBarrier(run, resource, profiles, maxHeapMiB, osMemoryGiB, System.getProperty("sun.arch.data.model") == "64")
        linkedMapOf<String, Any?>("path" to path, "run" to index + 1, "directive" to run.raw, "runner" to run.runner,
            "vmFlags" to run.vm, "arguments" to run.arguments, "timeoutSeconds" to run.timeoutSeconds,
            "requires" to run.requires, "modules" to run.modules, "resource" to resource,
            "heapMiB" to mathHeapMiB(run.vm, resource), "status" to if (barrier == null) "PLANNED" else "SKIP", "reason" to barrier)
    }
}
val report = linkedMapOf<String, Any?>(
    "schema" to 1, "phase" to "metadata-preparation", "repository" to "https://github.com/openjdk/jdk25u",
    "tag" to tag, "commit" to revision, "referenceRuntime" to "BellSoft 25.0.4+9-LTS",
    "testRuntimeRevisionMismatch" to "Tests jdk-25.0.4.1-ga; BellSoft runtime 25.0.4+9-LTS. Same major/update, different revision/vendor; not asserted source-equivalent.",
    "kotlin" to "2.4.21", "gradle" to "9.8.1", "seed" to 559L,
    "metadataJvm" to mapOf("version" to System.getProperty("java.runtime.version"), "vendor" to System.getProperty("java.vendor")),
    "os" to mapOf("name" to System.getProperty("os.name"), "arch" to System.getProperty("os.arch")),
    "mode" to mode, "preparedSources" to prepared, "testsExecuted" to 0,
    "memoryInput" to memoryArgument, "autoMemorySnapshot" to autoMemory, "ciExecution" to ciExecution,
    "results" to mapOf("PASS" to 0, "FAIL" to 0, "SKIP" to 0, "NOT_RUN" to manifest.count { it["runner"] != "none" }),
    "files" to inventory,
    "dependencies" to dependencies, "plan" to planned,
)
File(work, "inventory.json").writeText(json(report) + "\n")
println("Verified ${lock.size} pinned sources and ${dependencies.size} dependency records; prepared ${prepared.size} sources.")
if (!execute) println("No javac, Gradle, or math tests invoked. Test status: NOT_RUN.")
println("Inventory: build/math-tck/inventory.json")

if (execute) {
    val jdk = File(requireNotNull(System.getenv("JAVA_HOME")) { "JAVA_HOME must name BellSoft 25.0.4+9-LTS" }).canonicalFile
    val release = File(jdk, "release").readText()
    check(release.contains("IMPLEMENTOR=\"BellSoft\"") && release.contains("JAVA_RUNTIME_VERSION=\"25.0.4+9-LTS\"")) { "Reference runtime mismatch" }
    val java = File(jdk, "bin/java").path
    val javac = File(jdk, "bin/javac").path
    val javap = File(jdk, "bin/javap").path
    check(listOf(java, javac, javap).all { File(it).canExecute() })
    val phases = when (mode) { "--both" -> listOf("baseline", "port"); "--port" -> listOf("port"); else -> listOf("baseline") }
    val portJar = if ("port" in phases) File(requireNotNull(options["--port-jar"]) { "--port-jar exact parent-selected jar required" }).canonicalFile else null
    if (portJar != null) check(portJar.isFile && portJar.extension == "jar")
    val portSha256 = portJar?.let { digest("SHA-256", it.readBytes()) }
    val timestamp = System.currentTimeMillis().toString()
    val resultsDir = File(work, "results/$timestamp")
    check(!resultsDir.exists()); resultsDir.mkdirs()
    val results = mutableListOf<Map<String, Any?>>()
    val metadata = linkedMapOf<String, Any?>(
        "schema" to 1, "phase" to "public-tests-execution", "officialJck" to false,
        "executionGrantComment" to options["--execution-grant"], "ciExecution" to ciExecution,
        "memoryInput" to memoryArgument, "autoMemorySnapshot" to autoMemory, "upstreamCommit" to revision, "tag" to tag,
        "runtimeRelease" to release, "kotlin" to "2.4.21", "gradle" to "9.8.1",
        "os" to report["os"], "declaredOsMemoryGiB" to osMemoryGiB, "grantedMaxHeapMiB" to maxHeapMiB,
        "profiles" to profiles.toList(), "seed" to 559L, "BigIntegerTestSubset" to 0, "SymmetricRangeTestsSubset" to 0,
        "hostRsaRandomness" to "Original provider key generation uses its unseeded SecureRandom; it is baseline host integration only",
        "portJar" to portJar?.path, "portJarSha256" to portSha256, "manifest" to manifest, "dependencies" to dependencies,
        "testRuntimeRevisionMismatch" to report["testRuntimeRevisionMismatch"],
        "ownedToolSources" to File(root, "tools/math-tck").listFiles().orEmpty().filter { it.extension == "java" || it.name.endsWith(".main.kts") }.sortedBy { it.name }
            .associate { it.name to digest("SHA-256", it.readBytes()) },
        "runnerSha256" to digest("SHA-256", File(root, "tools/run-math-tck.main.kts").readBytes()),
        "sourceLockSha256" to digest("SHA-256", File(root, "tools/math-tck/source-lock.tsv").readBytes()),
    )
    fun saveResults() {
        File(resultsDir, "results.json").writeText(json(metadata + mapOf(
            "results" to results,
            "counts" to listOf("PASS", "FAIL", "SKIP").associateWith { status -> results.count { it["status"] == status } },
        )) + "\n")
    }
    saveResults()
    val physical = mathMemorySnapshot()
    metadata["initialMemory"] = physical
    if (osMemoryGiB * 1024 * 1024 * 1024 > physical.getValue("totalBytes")) {
        results += mapOf("scope" to "capacity-precondition", "status" to "FAIL", "reason" to "Declared OS memory exceeds observed physical capacity")
        saveResults(); error("Invalid capacity declaration")
    }
    val runtime = mathProcess(listOf(java, "-XshowSettings:properties", "-version"), resultsDir, File(resultsDir, "runtime"), 30, true)
    metadata["runtimeCommand"] = mathProcessRecord(runtime)
    if (runtime.exitCode != 0 || !runtime.stderr.readText().contains("java.runtime.version = 25.0.4+9-LTS")) {
        results += mapOf("scope" to "runtime-precondition", "status" to "FAIL", "reason" to "Actual reference JVM version mismatch")
        saveResults(); error("Actual reference runtime mismatch; see results.json")
    }
    val jars = dependencies.filter { it.getValue("path").endsWith(".jar") }.map { File(lib, it.getValue("path").substringAfterLast('/')).path }
    val support = File(work, "upstream/test/lib/jdk/test/lib/RandomFactory.java")
    for (phase in phases) {
        for (decision in manifest) {
            val path = decision.getValue("path")
            val disposition = decision.getValue("disposition")
            if (disposition == "support") continue
            if (disposition == "excluded") {
                results += mapOf("phase" to phase, "path" to path, "status" to "SKIP", "scope" to "excluded",
                    "reason" to decision.getValue("reason"), "lostCoverage" to decision.getValue("lostCoverage"))
                saveResults(); continue
            }
            val original = sources.getValue(path)
            for (method in upstreamDisabledMathMethods(original)) {
                results += mapOf("phase" to phase, "path" to path, "method" to method, "scope" to "upstream-disabled",
                    "status" to "SKIP", "reason" to "Pinned upstream @Test(enabled=false) retained unchanged",
                    "lostCoverage" to "Original large-value boundary case $method; not executed or waived")
            }
            if (upstreamDisabledMathMethods(original).isNotEmpty()) saveResults()
            for (method in upstreamOmittedMathCalls(original)) {
                results += mapOf("phase" to phase, "path" to path, "method" to method, "scope" to "upstream-commented-call",
                    "status" to "SKIP", "reason" to "Pinned upstream commented-out call retained unchanged",
                    "lostCoverage" to "Original unscheduled probe $method; not executed or waived")
            }
            if (upstreamOmittedMathCalls(original).isNotEmpty()) saveResults()
            val relative = path.removePrefix("test/jdk/java/math/")
            val runs = mathRuns(original, File(path).nameWithoutExtension)
            for ((index, run) in runs.withIndex()) {
                val id = "$phase/${relative.removeSuffix(".java")}/run-${index + 1}"
                val result = linkedMapOf<String, Any?>("id" to id, "phase" to phase, "path" to path,
                    "directive" to run.raw, "seed" to 559L, "resource" to decision.getValue("resource"),
                    "scope" to "public-tests", "sourceSha256" to lock.single { it["path"] == path }.getValue("sha256"))
                val barrier = if (path !in filteredPaths) "Filtered by --tests; public coverage not executed" else
                    mathResourceBarrier(run, decision.getValue("resource"), profiles, maxHeapMiB, osMemoryGiB, System.getProperty("sun.arch.data.model") == "64")
                if (barrier != null) {
                    result["status"] = "SKIP"; result["reason"] = barrier
                    result["lostCoverage"] = "Entire invocation ${run.raw}; no mathematical domain waiver"
                    results += result; saveResults(); continue
                }
                val directory = File(resultsDir, id).apply { mkdirs() }
                val classes = File(directory, "classes").apply { mkdirs() }
                val source = if (phase == "baseline") File(work, "baseline/$relative") else File(work, "generated/$relative")
                result["compiledSourceSha256"] = digest("SHA-256", source.readBytes())
                val cp = (jars + listOfNotNull(portJar?.path.takeIf { phase == "port" })).joinToString(File.pathSeparator)
                val moduleOptions = run.modules.flatMap { listOf("--add-modules", it) }
                val patchOptions = mutableListOf<String>()
                if (phase == "baseline" && relative == "BigInteger/ByteArrayConstructorTest.java") {
                    val patchClasses = File(directory, "patch-java-base").apply { mkdirs() }
                    val patchRoot = File(work, "upstream/test/jdk/java/math/BigInteger/java.base")
                    val patch = mathProcess(listOf(javac, "-J-Xmx256m", "--patch-module", "java.base=${patchRoot.path}", "-d", patchClasses.path,
                        File(patchRoot, "java/math/Accessor.java").path), directory, File(directory, "compile-patch"), 120, true)
                    result["patchCompile"] = mathProcessRecord(patch)
                    if (patch.exitCode != 0 || patch.timedOut) {
                        result["status"] = "FAIL"; result["reason"] = "Baseline test-only Accessor patch compilation failed"
                        results += result; saveResults(); continue
                    }
                    patchOptions += listOf("--patch-module", "java.base=${patchClasses.path}")
                }
                val helperNames = listOf("Launcher.java", "CoreGuard.java", "ChildEntry.java", "ChildJvm.java") + if (phase == "port") listOf("HostBridge.java", "ArrayAccessor.java") else emptyList()
                val compilation = mathProcess(listOf(javac, "-J-Xmx256m", "-encoding", "UTF-8", "-cp", cp, "-d", classes.path) + moduleOptions + patchOptions +
                    listOf(source.path, support.path) + helperNames.map { File(root, "tools/math-tck/$it").path },
                    directory, File(directory, "compile"), 120, true)
                result["compile"] = mathProcessRecord(compilation)
                if (compilation.exitCode != 0 || compilation.timedOut) {
                    result["status"] = "FAIL"; result["reason"] = "Compilation failed; no source pruning or assertion removal"
                    results += result; saveResults(); continue
                }
                if (phase == "port") {
                    val audit = auditMathBytecode(javap, classes, run.className, directory, true)
                    result["bytecodeAudit"] = audit.second
                    if (!audit.first) {
                        result["status"] = "FAIL"; result["reason"] = "Test bytecode lacks core references or has host math member owners"
                        results += result; saveResults(); continue
                    }
                }
                val reports = File(directory, "framework-reports").apply { mkdirs() }
                val freshMemory = mathMemorySnapshot()
                val neededMiB = mathHeapMiB(run.vm, decision.getValue("resource")) * if (run.className == "ModPowPowersof2") 2 else 1
                val freshMemoryGate = decision.getValue("resource") in setOf("large-memory-8g", "large-memory-10g")
                result["freshMemory"] = freshMemory
                result["requiredFreeMiBWithMargin"] = neededMiB + 512
                result["freshMemoryGateRequired"] = freshMemoryGate
                if (freshMemoryGate && freshMemory.getValue("freeBytes") < (neededMiB + 512).toLong() * 1024 * 1024) {
                    result["status"] = "SKIP"; result["reason"] = "Fresh free memory below heap budget plus 512MiB orchestration margin"
                    result["lostCoverage"] = "Entire invocation ${run.raw}; retry only with fresh resource capacity"
                    results += result; saveResults(); continue
                }
                val classLog = File(directory, "class-load.log")
                val vm = mutableListOf("-ea", "-XX:-OmitStackTraceInFastThrow", "-Dseed=559", "-Dtest.src=${source.parentFile.path}",
                    "-Dmath.harness.reports=${reports.path}", "-Xlog:class+load:file=${classLog.path}")
                if (run.vm.none { it.startsWith("-Xmx") }) vm += "-Xmx${mathHeapMiB(run.vm, decision.getValue("resource"))}m"
                vm += run.vm
                if (run.className in setOf("BigIntegerTest", "SymmetricRangeTests")) vm += "-Dsubset=0"
                if (run.className == "ModPowPowersof2") {
                    vm += "-Dmath.harness.childDir=${File(directory, "child").path}"
                    vm += "-Dmath.harness.childTimeoutSeconds=${run.timeoutSeconds - 10}"
                }
                if (phase == "port") {
                    vm += "-Dmath.harness.portJar=${portJar!!.path}"
                    if (relative == "BigDecimal/DoubleFloatValueTests.java") vm += "-Dmath.harness.methods=${floatDoubleMethods.joinToString(",")}"
                }
                val invocation = mathProcess(listOf(java) + vm + moduleOptions + patchOptions +
                    listOf("-cp", classes.path + File.pathSeparator + cp, "mathharness.Launcher", phase, run.runner, run.className) + run.arguments,
                    directory, File(directory, "test"), run.timeoutSeconds, true)
                result["execution"] = mathProcessRecord(invocation)
                val out = invocation.stdout.readText()
                val err = invocation.stderr.readText()
                var childVerified = true
                if (run.className == "ModPowPowersof2") {
                    val childFile = File(directory, "child/result.properties")
                    val child = Properties()
                    if (childFile.isFile) childFile.inputStream().use { child.load(it) }
                    childVerified = child.getProperty("status") == "PASS" && child.getProperty("exitCode") == "0" &&
                        child.getProperty("exactSuccessLine") == "true" && child.getProperty("completionVerified") == "true" &&
                        (phase == "baseline" || child.getProperty("coreLoadingVerified") == "true")
                    result["childJvm"] = child.stringPropertyNames().associateWith { child.getProperty(it) }
                }
                val swallowedOom = Regex("(?i)skipped[^\\n]*OutOfMemoryError").containsMatchIn(out + "\n" + err)
                val loadedCore = phase == "baseline" || (listOf("BigInteger", "BigDecimal", "MathContext", "RoundingMode").all { simple ->
                    out.contains("CORE_CLASS io.heapy.krogu.math.$simple ") && classLog.isFile && classLog.readText().contains("io.heapy.krogu.math.$simple")
                })
                val complete = out.contains("HARNESS_COMPLETE $phase ${run.className}")
                val frameworkSkipped = out.contains("HARNESS_CASE SKIP ") ||
                    reports.listFiles().orEmpty().filter { it.extension == "xml" }.any { Regex("<skipped(?:\\s|/|>)").containsMatchIn(it.readText()) }
                result["classLoadLog"] = classLog.path
                result["coreLoadingVerified"] = if (phase == "port") loadedCore else null
                result["frameworkReports"] = reports.path
                result["caughtOomAssertionsIncomplete"] = swallowedOom
                if (phase == "port") result["baselineStatus"] = results.firstOrNull {
                    it["phase"] == "baseline" && it["path"] == path && it["directive"] == run.raw
                }?.get("status")
                result["status"] = when {
                    invocation.timedOut || invocation.exitCode != 0 || !loadedCore || !complete || !childVerified -> "FAIL"
                    swallowedOom -> "FAIL"
                    frameworkSkipped -> "SKIP"
                    else -> "PASS"
                }
                result["reason"] = when {
                    invocation.timedOut -> "Upstream invocation timeout"
                    invocation.exitCode != 0 -> "Test process failed; retain public reproducer and classify independently"
                    !loadedCore -> "Core loading proof missing"
                    !childVerified -> "Nested child termination/exit/success/core proof missing"
                    !complete -> "Launcher completion marker missing"
                    swallowedOom -> "Caught OutOfMemoryError; incomplete assertions and unresolved resource-or-implementation barrier, not a waived resource SKIP"
                    frameworkSkipped -> "Framework reported skipped cases; not a full invocation pass"
                    else -> "Completed retained invocation assertions"
                }
                results += result
                if (phase == "port") for ((caseIndex, coverage) in adaptations.getValue(path).companionSkips.withIndex()) {
                    results += mapOf("id" to "$id/adaptation-${caseIndex + 1}", "phase" to phase, "path" to path,
                        "scope" to "adaptation-companion", "status" to "SKIP", "reason" to coverage,
                        "lostCoverage" to coverage, "retainedChanges" to adaptations.getValue(path).changes)
                }
                saveResults()
                println("${result["status"]} $id: ${result["reason"]}")
            }
        }
    }
    val attempted = results.count { it["execution"] != null }
    metadata["testsExecuted"] = attempted
    metadata["complete"] = true
    saveResults()
    println("Results: ${File(resultsDir, "results.json").path}")
    if (attempted == 0 || results.any { it["status"] == "FAIL" }) kotlin.system.exitProcess(1)
}

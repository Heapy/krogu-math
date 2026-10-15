#!/usr/bin/env kotlin

// Usage: kotlin scripts/probe-js-number.main.kts [baseline|es2015|bigint|noinline|coercion|timeout-check|phase-list]
// Compile-only: kotlin scripts/probe-js-number.main.kts ir-number|ir-generic [checkpoint]
// No project build or dependencies. Each invocation creates a fresh scratch snapshot.
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.system.exitProcess

val mode = args.firstOrNull() ?: "baseline"
val irDiagnostic = mode == "ir-number" || mode == "ir-generic"
val irCheckpoints = setOf("AutoboxingTransformer", "CallsLowering", "MainFunctionCallWrapperLowering")
require(args.size <= if (irDiagnostic) 2 else 1) { "Expected one mode, optionally one IR checkpoint" }
val irPhase = if (irDiagnostic) args.getOrNull(1) ?: "CallsLowering" else null
require(irPhase == null || irPhase in irCheckpoints) { "Unknown IR checkpoint: $irPhase" }
val irFqName = if (mode == "ir-number") "numberValues" else "genericValues"
val flags = when (mode) {
    "baseline", "coercion", "timeout-check", "phase-list", "ir-number", "ir-generic" -> emptyList()
    "es2015" -> listOf("-target", "es2015")
    "bigint" -> listOf("-target", "es2015", "-Xes-long-as-bigint")
    "noinline" -> listOf("-Xno-inline")
    else -> error("Unknown mode: $mode")
}
val compilerHome = File("/Users/yoda/.sdkman/candidates/kotlin/2.4.21").canonicalFile
val javaHome = File("/Users/yoda/.sdkman/candidates/java/25.0.4-librca").canonicalFile
val compiler = File(compilerHome, "lib/kotlin-compiler.jar")
// Direct Java launch gives this runner ownership of the actual compiler process,
// rather than a shell launcher that can exit while its JVM child survives.
val compilerCommand = listOf(File(javaHome, "bin/java").path, "-Xmx512m",
    "-Dkotlin.home=${compilerHome.path}", "-cp", File(compilerHome, "lib/*").path,
    "org.jetbrains.kotlin.cli.js.K2JSCompiler")
val stdlib = File(compilerHome, "lib/kotlin-stdlib-js.klib")
val sourceName = if (mode == "coercion") "NumberCoercionProbe.kt" else "NumberProbe.kt"
val input = File("diagnostics/js-number/$sourceName").canonicalFile
require(listOf(compiler, stdlib, input).all { it.isFile })
val scratch = Files.createTempDirectory("krogu-js-number-$mode-").toFile()
val fixture = File(scratch, sourceName).also { input.copyTo(it) }
val runner = File("scripts/probe-js-number.main.kts").canonicalFile
runner.copyTo(File(scratch, runner.name))
val transcript = File(scratch, "transcript.txt")
fun record(line: String) { println(line); transcript.appendText("$line\n") }
fun hash(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { stream ->
        val buffer = ByteArray(65536)
        while (true) { val count = stream.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}
fun command(label: String, argv: List<String>, seconds: Long = 120): Int {
    record("COMMAND $label argv=${argv.joinToString(" ") { "'${it.replace("'", "'\\''")}'" }}")
    record("ENV JAVA_HOME=$javaHome JAVACMD=$javaHome/bin/java JAVA_OPTS=-Xmx512m; KOTLIN_RUNNER/KOTLIN_TOOL removed; cwd=$scratch; stdin=closed; timeout=${seconds}s")
    val process = ProcessBuilder(argv).directory(scratch).redirectErrorStream(true).apply {
        environment()["JAVA_HOME"] = javaHome.path
        environment()["JAVACMD"] = File(javaHome, "bin/java").path
        environment()["JAVA_OPTS"] = "-Xmx512m"
        environment().remove("KOTLIN_RUNNER")
        environment().remove("KOTLIN_TOOL")
    }.start()
    process.outputStream.close()
    val output = File(scratch, "$label.log")
    val readerFailure = AtomicReference<Throwable?>()
    val reader = thread(isDaemon = true) {
      try {
        process.inputStream.use { source -> output.outputStream().use { dest ->
            val buffer = ByteArray(4096)
            var retained = 0
            while (true) {
                val count = source.read(buffer)
                if (count < 0) break
                val keep = minOf(count, (524288 - retained).coerceAtLeast(0))
                dest.write(buffer, 0, keep)
                retained += keep
            }
        } }
      } catch (failure: Throwable) { readerFailure.set(failure) }
    }
    val finished = process.waitFor(seconds, TimeUnit.SECONDS)
    if (!finished) process.destroyForcibly() // Only the directly launched, owned leaf process.
    val stopped = finished || process.waitFor(5, TimeUnit.SECONDS)
    reader.join(5000)
    check(stopped && !reader.isAlive) { "$label cleanup failed; evidence at $scratch" }
    check(readerFailure.get() == null) { "$label output capture failed: ${readerFailure.get()}" }
    record("RESULT $label pid=${process.pid()} alive=${process.isAlive} exit=${process.exitValue()} timeout=${!finished} sha256=${hash(output)}")
    if (irDiagnostic) check(output.length() < 524288) {
        "$label output reached the capture limit; retain incomplete evidence at $scratch"
    }
    // Diagnostic compiler logs are retained, but may contain opaque IR output.
    if (label != "help" && label != "advanced" && !(irDiagnostic && label.startsWith("compile-")))
        record(output.readText())
    check(finished) { "$label timed out; evidence at $scratch" }
    return process.exitValue()
}
fun dumpInventory(stage: String, directory: File): Int {
    val suppliedRoot = directory.toPath().toAbsolutePath().normalize()
    val root = directory.canonicalFile.toPath()
    check(!Files.isSymbolicLink(suppliedRoot) && root.startsWith(scratch.canonicalFile.toPath())) {
        "Unsafe $stage dump root; evidence at $scratch"
    }
    var entries = 0
    var bytes = 0L
    val files = mutableListOf<Pair<Path, Long>>()
    // These are post-execution inventory bounds, not a disk quota while the compiler writes.
    Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
        fun validate(path: Path, attributes: BasicFileAttributes) {
            check(++entries <= 256 && !attributes.isSymbolicLink &&
                path.toAbsolutePath().normalize().startsWith(root) && path.toRealPath().startsWith(root)) {
                "Unsafe or excessive $stage dump tree; evidence at $scratch"
            }
            check(root.relativize(path).toString().length <= 1024) { "Excessive dump path length" }
        }
        override fun preVisitDirectory(path: Path, attributes: BasicFileAttributes): FileVisitResult {
            validate(path, attributes)
            return FileVisitResult.CONTINUE
        }
        override fun visitFile(path: Path, attributes: BasicFileAttributes): FileVisitResult {
            validate(path, attributes)
            check(attributes.isRegularFile && files.size < 128 && attributes.size() <= 8L * 1024 * 1024) {
                "Unsupported or excessive $stage dump file; evidence at $scratch"
            }
            bytes += attributes.size()
            check(bytes <= 64L * 1024 * 1024) { "Excessive $stage total dump bytes; evidence at $scratch" }
            files.add(path to attributes.size())
            return FileVisitResult.CONTINUE
        }
    })
    // Validate the complete bounded inventory before reading any file bytes for hashing.
    for ((path, size) in files.sortedBy { root.relativize(it.first).toString() }) {
        check(!Files.isSymbolicLink(path) && Files.isRegularFile(path) && Files.size(path) == size &&
            path.toRealPath().startsWith(root)) { "Dump changed after inventory; evidence at $scratch" }
        val relative = root.relativize(path).toString().replace("\\", "\\\\")
            .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t")
        record("DUMP stage=$stage path=$relative bytes=$size sha256=${hash(path.toFile())}")
    }
    record("DUMP_INVENTORY stage=$stage files=${files.size} bytes=$bytes status=${if (files.isEmpty()) "INCONCLUSIVE_EMPTY" else "CAPTURED_UNINTERPRETED"}")
    return files.size
}
record("scratch=$scratch mode=$mode timestamp=${java.time.Instant.now()}")
for (file in listOf(fixture, runner, compiler, stdlib))
    record("SHA256 ${hash(file)} ${file.path}")
record("INPUT BEGIN\n${fixture.readText()}INPUT END")
check(command("jdk", listOf(File(javaHome, "bin/java").path, "-version"), 10) == 0)
check(command("node-version", listOf("node", "--version"), 10) == 0)
if (mode == "timeout-check") {
    val failure = runCatching {
        command("timeout", listOf("node", "-e", "setInterval(() => {}, 1000)"), 1)
    }.exceptionOrNull()
    check(failure is IllegalStateException && failure.message?.startsWith("timeout timed out;") == true)
    check(transcript.readText().contains("alive=false") && transcript.readText().contains("timeout=true"))
    record("PASS owned process timed out and cleanup completed within bounded waits")
    exitProcess(0)
}
// JS CLI prints its identity but exits 1 when -version has no compilation input.
val versionExit = command("compiler-version", compilerCommand + "-version", 30)
check(versionExit in 0..1 && File(scratch, "compiler-version.log").readText().contains("kotlinc-js 2.4.21"))
check(command("help", compilerCommand + "-help", 30) == 0)
check(command("advanced", compilerCommand + "-X", 30) == 0)
if (irDiagnostic) {
    val advancedHelp = File(scratch, "advanced.log").readText()
    for (flag in listOf("-Xdump-directory", "-Xdump-fqname", "-Xphases-to-dump")) {
        check(Regex("(?m)^\\s*${Regex.escape(flag)}(?:\\s|=|$)").containsMatchIn(advancedHelp)) {
            "Pinned compiler help does not advertise $flag; no unsupported dump option will be used"
        }
    }
    val catalog = File("diagnostics/js-number/phase-list-2.4.21.txt").canonicalFile
    check(catalog.isFile && catalog.length() < 524288 &&
        hash(catalog) == "804d07a1e01283af9db5097282b888f1695efa694027c8035c5f1c3235007381") {
        "Pinned phase catalog does not match; no phase name will be guessed"
    }
    check(irCheckpoints.all { phase -> catalog.readLines().count { it.trim() == phase } == 1 }) {
        "Expected exact checkpoint names once in the retained catalog"
    }
    val retainedCatalog = File(scratch, "phase-list-2.4.21.txt").also { catalog.copyTo(it) }
    record("SHA256 ${hash(retainedCatalog)} ${retainedCatalog.path}")
    record("IR_REQUEST fqname=$irFqName checkpoint=$irPhase before=true after=true")
}
if (mode == "phase-list") {
    check(Regex("(?m)^\\s*-Xlist-phases(?:\\s|$)")
        .containsMatchIn(File(scratch, "advanced.log").readText())) {
        "Pinned compiler help does not advertise -Xlist-phases; no phase name will be guessed"
    }
}
val klibDir = File(scratch, "klib").also { it.mkdir() }
val jsDir = File(scratch, "js").also { it.mkdir() }
val common = listOf("-libraries", stdlib.path, "-ir-output-name", "number-probe", "-module-kind", "commonjs") + flags
if (irDiagnostic) {
    val klibDumps = File(scratch, "ir-klib").also { check(it.mkdir()) }
    val jsDumps = File(scratch, "ir-js").also { check(it.mkdir()) }
    fun dumpFlags(directory: File) = listOf("-Xdump-directory=${directory.path}",
        "-Xdump-fqname=$irFqName", "-Xphases-to-dump=$irPhase")
    var klibCount = 0
    var jsCount = 0
    var complete = false
    try {
        check(command("compile-klib", compilerCommand + listOf(fixture.path, "-ir-output-dir", klibDir.path) +
            common + dumpFlags(klibDumps)) == 0)
        val klib = File(klibDir, "number-probe.klib")
        record("SHA256 ${hash(klib)} ${klib.path}")
        check(command("compile-js", compilerCommand + listOf("-Xir-produce-js", "-Xinclude=${klib.path}",
            "-ir-output-dir", jsDir.path, "-main", "call") + common + dumpFlags(jsDumps)) == 0)
        val js = File(jsDir, "number-probe.js")
        record("SHA256 ${hash(js)} ${js.path}")
        complete = true
    } finally {
        // Preserve failed commands and empty stage directories without widening the FQName filter.
        try {
            klibCount = dumpInventory("klib", klibDumps)
            jsCount = dumpInventory("js", jsDumps)
        } finally {
            record("FINAL diagnostic_requested=true number_tests_executed=false node_exit=NOT_RUN compilation_complete=$complete evidence=$scratch")
        }
    }
    record("IR_CAPTURE status=${if (klibCount == 0 || jsCount == 0) "INCONCLUSIVE_EMPTY_STAGE" else "CAPTURED_UNINTERPRETED"}")
    exitProcess(0)
}
check(command("compile-klib", compilerCommand + listOf(fixture.path, "-ir-output-dir", klibDir.path) + common) == 0)
val klib = File(klibDir, "number-probe.klib")
record("SHA256 ${hash(klib)} ${klib.path}")
val linkFlags = if (mode == "phase-list") listOf("-Xlist-phases") else emptyList()
check(command("compile-js", compilerCommand + listOf("-Xir-produce-js", "-Xinclude=${klib.path}",
    "-ir-output-dir", jsDir.path, "-main", "call") + common + linkFlags) == 0)
if (mode == "phase-list") {
    check(File(scratch, "compile-js.log").length() < 524288) {
        "Phase-list output reached the capture limit; retain this incomplete evidence"
    }
    // This reports a compiler diagnostic request, not passing Number behavior.
    // Read the actual phase names before designing any later phase-local probe.
    record("FINAL phase_listing_requested=true number_tests_executed=false node_exit=NOT_RUN evidence=$scratch")
    exitProcess(0)
}
val js = File(jsDir, "number-probe.js")
record("SHA256 ${hash(js)} ${js.path}")
// Execute the unchanged compiler output. Never splice calls or inspect runtime helper bodies.
val result = command("node", listOf("node", "--max-old-space-size=128", js.path), 10)
record("FINAL node_exit=$result evidence=$scratch")
exitProcess(result)

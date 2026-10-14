#!/usr/bin/env kotlin

// Usage: kotlin scripts/probe-js-number.main.kts [baseline|es2015|bigint|noinline|coercion|timeout-check]
// No project build or dependencies. Each invocation creates a fresh scratch snapshot.
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.system.exitProcess

val mode = args.singleOrNull() ?: if (args.isEmpty()) "baseline" else error("Expected one mode")
val flags = when (mode) {
    "baseline", "coercion", "timeout-check" -> emptyList()
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
    if (label != "help" && label != "advanced") record(output.readText())
    check(finished) { "$label timed out; evidence at $scratch" }
    return process.exitValue()
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
val klibDir = File(scratch, "klib").also { it.mkdir() }
val jsDir = File(scratch, "js").also { it.mkdir() }
val common = listOf("-libraries", stdlib.path, "-ir-output-name", "number-probe", "-module-kind", "commonjs") + flags
check(command("compile-klib", compilerCommand + listOf(fixture.path, "-ir-output-dir", klibDir.path) + common) == 0)
val klib = File(klibDir, "number-probe.klib")
record("SHA256 ${hash(klib)} ${klib.path}")
check(command("compile-js", compilerCommand + listOf("-Xir-produce-js", "-Xinclude=${klib.path}",
    "-ir-output-dir", jsDir.path, "-main", "call") + common) == 0)
val js = File(jsDir, "number-probe.js")
record("SHA256 ${hash(js)} ${js.path}")
// Execute the unchanged compiler output. Never splice calls or inspect runtime helper bodies.
val result = command("node", listOf("node", "--max-old-space-size=128", js.path), 10)
record("FINAL node_exit=$result evidence=$scratch")
exitProcess(result)

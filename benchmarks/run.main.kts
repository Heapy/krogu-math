#!/usr/bin/env kotlinr
// Run from the repository root. No network dependencies; process construction uses argument lists.
import java.io.File
import java.security.MessageDigest
import java.time.Instant

val root = File(".").canonicalFile
val platform = args.getOrNull(0) ?: "all"
val output = File(args.getOrNull(1) ?: "benchmarks/results/local-${Instant.now().epochSecond}").absoluteFile
val forks = args.getOrNull(2)?.toInt() ?: 3
val warmup = args.getOrNull(3)?.toInt() ?: 5
val samples = args.getOrNull(4)?.toInt() ?: 5
val batchMs = args.getOrNull(5)?.toInt() ?: 100
require(platform in listOf("all", "jvm", "js", "wasm", "native"))
require(forks >= 1 && warmup >= 1 && samples >= 1 && batchMs >= 1)
require(!output.exists()) { "Refusing to overwrite $output" }
output.mkdirs()
val javaHome = File(System.getenv("BENCH_JAVA_HOME") ?: "${System.getProperty("user.home")}/.sdkman/candidates/java/25.0.4-librca")
val java = File(javaHome, "bin/java").absolutePath
fun capture(command: List<String>): String {
    val child = ProcessBuilder(command).directory(root).redirectErrorStream(true).start()
    val text = child.inputStream.bufferedReader().readText()
    check(child.waitFor() == 0) { "$command\n$text" }
    return text.trim()
}
val runtime = capture(listOf(java, "-XshowSettings:properties", "-version"))
check(runtime.contains("java.runtime.version = 25.0.4+9-LTS") && runtime.contains("java.vendor = BellSoft")) {
    "Expected pinned BellSoft 25.0.4+9-LTS; received:\n$runtime"
}
val os = System.getProperty("os.name")
val arch = System.getProperty("os.arch")
val nativeTarget = when {
    os == "Mac OS X" && arch == "aarch64" -> "MacosArm64"
    os == "Mac OS X" -> "MacosX64"
    os == "Linux" && arch == "aarch64" -> "LinuxArm64"
    os == "Linux" -> "LinuxX64"
    else -> error("Runner supports macOS/Linux hosts; Windows executable can be invoked manually")
}
val selected = if (platform == "all") listOf("jvm", "js", "wasm", "native") else listOf(platform)
val tasks = selected.map { when (it) {
    "jvm" -> ":benchmarks:jvmClasspath"
    "js" -> ":benchmarks:jsProductionExecutableCompileSync"
    "wasm" -> ":benchmarks:wasmJsProductionExecutableCompileSync"
    else -> ":benchmarks:linkReleaseExecutable$nativeTarget"
} }
val build = ProcessBuilder(listOf("./gradlew") + tasks + listOf("--console=plain", "--no-daemon")).directory(root)
    .redirectErrorStream(true).redirectOutput(File(output, "build.log"))
build.environment()["JAVA_HOME"] = javaHome.absolutePath
check(build.start().waitFor() == 0) { "Build failed; see ${File(output, "build.log")}" }
fun sha(file: File): String = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
fun fingerprints(files: List<File>): String = files.distinctBy { it.canonicalPath }
    .sortedBy { it.path }.joinToString("\n", postfix = "\n") { "${sha(it)}  ${it.relativeTo(root)}" }
fun manifest(name: String, files: List<File>) = File(output, name).writeText(fingerprints(files))
val sources = listOf(File(root, "src"), File(root, "benchmarks/src")).flatMap { it.walkTopDown().filter(File::isFile).toList() } +
    listOf("build.gradle.kts", "settings.gradle.kts", "gradle.properties", "gradle/wrapper/gradle-wrapper.properties", "benchmarks/build.gradle.kts", "benchmarks/run.main.kts", "benchmarks/generate-jdk.main.kts", "benchmarks/check-results.main.kts").map { File(root, it) }
manifest("sources.sha256", sources)
val nodeOs = if (os == "Mac OS X") "darwin" else "linux"
val nodeArch = if (arch == "aarch64") "arm64" else "x64"
val node = System.getenv("BENCH_NODE") ?: "${System.getProperty("user.home")}/.gradle/nodejs/node-v26.2.0-$nodeOs-$nodeArch/bin/node"
if (selected.any { it == "js" || it == "wasm" }) check(capture(listOf(node, "--version")) == "v26.2.0") { "Expected pinned Node v26.2.0" }
val environment = buildString {
    appendLine("timestamp_utc=${Instant.now()}")
    appendLine("git_head=${capture(listOf("git", "rev-parse", "HEAD"))}")
    appendLine("git_status=${capture(listOf("git", "status", "--short"))}")
    appendLine("host=${capture(listOf("uname", "-a"))}")
    if (os == "Mac OS X") appendLine("cpu=${capture(listOf("sysctl", "-n", "machdep.cpu.brand_string"))}")
    appendLine("forks=$forks warmup=$warmup samples=$samples batch_ms=$batchMs platforms=$selected")
    appendLine("java_command=$java -Xms256m -Xmx1g -XX:+UseG1GC")
    appendLine(runtime)
    if (selected.any { it == "js" || it == "wasm" }) appendLine("node=$node ${capture(listOf(node, "--version"))}")
    appendLine("gradle=9.8.1 kotlin=2.4.21 native=2.4.21; JS/Wasm production executable; Native release")
}
File(output, "environment.txt").writeText(environment)
// Host snapshots are outside measured child processes. They describe conditions, not isolation.
fun hostSnapshot(name: String) {
    val snapshot = buildString {
        appendLine("timestamp_utc=${Instant.now()}")
        appendLine(capture(listOf("uptime")))
        if (os == "Mac OS X") {
            appendLine(capture(listOf("sysctl", "vm.swapusage")))
            appendLine(capture(listOf("vm_stat")))
        } else appendLine(File("/proc/meminfo").readText())
        appendLine("Processes reporting >=10% CPU at snapshot (ps; not continuous monitoring):")
        val processes = capture(listOf("ps", "-axo", "pid,ppid,pcpu,pmem,comm"))
        appendLine(processes.lineSequence().first())
        processes.lineSequence().drop(1).filter { line ->
            line.trim().split(Regex("\\s+"), limit = 5).getOrNull(2)?.toDoubleOrNull()?.let { it >= 10.0 } == true
        }.forEach { appendLine(it) }
    }
    File(output, "$name.host.txt").writeText(snapshot)
}
val artifacts = mutableListOf<File>()
val commands = selected.associateWith { target ->
    when (target) {
        "jvm" -> {
            val classpath = File(root, "benchmarks/build/jvm-classpath.txt").readText()
            artifacts += classpath.split(File.pathSeparator).map(::File).filter(File::exists).flatMap { file -> if (file.isDirectory) file.walkTopDown().filter(File::isFile).toList() else listOf(file) }
            listOf(java, "-Xms256m", "-Xmx1g", "-XX:+UseG1GC", "-cp", classpath, "bench.MainKt")
        }
        "js", "wasm" -> {
            val directory = File(root, "benchmarks/build/compileSync/${if (target == "js") "js" else "wasmJs"}/main/productionExecutable/kotlin")
            artifacts += directory.walkTopDown().filter(File::isFile).toList()
            val entry = directory.listFiles()!!.single { it.name == "krogu-math-benchmarks.${if (target == "js") "js" else "mjs"}" }
            listOf(node, "--expose-gc", entry.absolutePath)
        }
        else -> {
            val binary = File(root, "benchmarks/build/bin/${nativeTarget.replaceFirstChar { it.lowercase() }}/releaseExecutable/benchmarks.kexe")
            artifacts += binary
            listOf(binary.absolutePath)
        }
    }
}
manifest("artifacts.sha256", artifacts)
for ((target, command) in commands) {
    for (fork in 0 until forks) {
        val invocation = command + listOf("$warmup", "$samples", "$batchMs", if (fork % 2 == 1) "reverse" else "forward", "${fork * samples}")
        File(output, "$target-$fork.command.txt").writeText(invocation.joinToString("\n", postfix = "\n"))
        val time = if (os == "Mac OS X") listOf("/usr/bin/time", "-l") else listOf("/usr/bin/time", "-v")
        hostSnapshot("$target-$fork-before")
        println("Running $target fork ${fork + 1}/$forks -> $output")
        val process = ProcessBuilder(time + invocation).directory(root)
            .redirectOutput(File(output, "$target-$fork.csv"))
            .redirectError(File(output, "$target-$fork.process.txt")).start()
        check(process.waitFor() == 0) { "Failed $target fork $fork; see process.txt" }
        hostSnapshot("$target-$fork-after")
    }
}
check(fingerprints(artifacts) == File(output, "artifacts.sha256").readText()) { "Artifacts changed during measurement" }
check(fingerprints(sources) == File(output, "sources.sha256").readText()) { "Sources changed during measurement" }
File(output, "binding-verified.txt").writeText("Source and artifact SHA-256 manifests match before and after the entire campaign.\n")
println("Completed: $output")

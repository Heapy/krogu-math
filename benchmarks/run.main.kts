#!/usr/bin/env kotlinr
// Invoke the downloaded standard benchmark adapter and preserve reports in a fresh directory.
import java.io.File
import java.util.concurrent.TimeUnit
import java.security.MessageDigest

val root = __FILE__.canonicalFile.parentFile.parentFile
require(args.size in 2..3) { "Usage: run.main.kts <bench-*|peer-*|bench-smoke-*|peer-smoke-*|fixed-smoke-jvm runner> <new-output-dir> [smoke|main]" }
val module = args[0]
require(module in listOf("bench", "peer", "bench-smoke", "peer-smoke", "fixed-smoke").flatMap { prefix -> listOf("jvm", "js", "wasm", "macos", "linux", "linux-arm64", "windows").map { "$prefix-$it" } })
val profile = args.getOrNull(2) ?: "main"
require(profile in listOf("smoke", "main"))
val output = File(args[1]).absoluteFile
require(!output.exists()) { "Refusing to overwrite $output" }
check(output.mkdirs())
val windows = System.getProperty("os.name").startsWith("Windows")
fun invocation(name: String, arguments: List<String>) = if (windows)
    listOf("cmd.exe", "/c", root.resolve("$name.bat").path) + arguments
else listOf(root.resolve(name).path) + arguments
fun execute(name: String, arguments: List<String>, explicitCommand: List<String>? = null) {
    val log = output.resolve("${name}-${arguments.first()}.log")
    val child = ProcessBuilder(explicitCommand ?: invocation(name, arguments)).directory(root).redirectErrorStream(true).redirectOutput(log).start()
    try {
        check(child.waitFor(6, TimeUnit.HOURS)) { "Timed out; raw output retained: $log" }
        check(child.exitValue() == 0) { "Command failed; raw output retained: $log" }
    } finally {
        if (child.isAlive) {
            try {
                child.descendants().use { descendants -> descendants.forEach { it.destroyForcibly() } }
            } finally {
                child.destroyForcibly()
                check(child.waitFor(30, TimeUnit.SECONDS)) { "Owned process did not terminate" }
            }
        }
    }
}
fun sha(file: File) = file.inputStream().use { input ->
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(65536)
    while (true) { val count = input.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
    digest.digest().joinToString("") { "%02x".format(it) }
}
val declaredInputs = StringBuilder("plugin_lock_sha256=${sha(root.resolve("ktc-plugins.lock.yaml"))}\n")
if (module.endsWith("-js") || module.endsWith("-wasm")) {
    val executable = (System.getenv("PATH") ?: "").split(File.pathSeparator)
        .map { File(it, if (windows) "node.exe" else "node") }.firstOrNull { it.isFile && it.canExecute() }?.canonicalFile
        ?: error("Node 24 must be on PATH")
    val versionFile = output.resolve("node-version.txt")
    val child = ProcessBuilder(executable.path, "--version").redirectErrorStream(true).redirectOutput(versionFile).start()
    try {
        check(child.waitFor(30, TimeUnit.SECONDS) && child.exitValue() == 0) { "Cannot identify Node runtime" }
        val version = versionFile.readText().trim()
        check(version.startsWith("v24.")) { "Expected Node 24, got $version" }
        declaredInputs.append("node_executable=$executable\nnode_sha256=${sha(executable)}\nnode_version=$version\n")
    } finally { if (child.isAlive) { child.destroyForcibly(); child.waitFor(30, TimeUnit.SECONDS) } }
}
output.resolve("runtime-inputs.txt").writeText(declaredInputs.toString())
execute("ktc-plugins", listOf("sync"))
execute("ktc-plugins", listOf("verify"))
val nativeConfiguration = (if (windows) listOf("cmd.exe", "/c", "kotlinr") else listOf("kotlinr")) +
    root.resolve("tools/configure-native-tests.main.kts").path +
    listOfNotNull(System.getenv("KOTLIN_TOOLCHAIN_BUILD_DIR"))
execute("native-configuration", listOf("bootstrap"), nativeConfiguration)
val command = if (profile == "smoke") "benchmarkSmoke" else "benchmark"
execute("kotlin", listOf("do", command, "-m", module))
val build = System.getenv("KOTLIN_TOOLCHAIN_BUILD_DIR")?.let(::File) ?: root.resolve("build")
val reports = build.resolve("tasks/_${module}_${command}@benchmark")
for (name in listOf("results.json", "run.json")) {
    val source = reports.resolve(name)
    check(source.isFile) { "Missing standard report: $source" }
    source.copyTo(output.resolve(name), overwrite = false)
}
output.resolve("invocation.txt").writeText("module=$module\nprofile=$profile\ncommand=./kotlin do $command -m $module\n")
println("Standard benchmark reports retained at $output")

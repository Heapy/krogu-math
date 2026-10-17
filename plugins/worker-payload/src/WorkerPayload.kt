package workerpayload

import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.Base64
import java.util.concurrent.TimeUnit
import kotlin.io.path.*

internal fun workerClosureFiles(root: Path): List<Path> {
    val files = Files.walk(root).use { paths -> paths.filter {
        Files.isRegularFile(it) && !it.fileName.toString().endsWith(".map")
    }.sorted().toList() }
    check(files.isNotEmpty() && files.all { it.parent == root &&
        it.fileName.toString().matches(Regex("[A-Za-z0-9_.-]+")) }) { "Worker runtime closure must be flat and safe" }
    check(files.sumOf(Files::size) <= 4L * 1024 * 1024) { "Worker closure unexpectedly large" }
    return files
}

/** Independently link the original worker sources, then embed its complete runtime closure. */
@TaskAction
fun generateWorkerPayload(
    @Input kernelSources: Path,
    @Input workerSources: Path,
    @Input wrapper: Path,
    @Input wrapperWindows: Path,
    platform: String,
    @Output workspace: Path,
    @Output generatedSources: Path,
) {
    require(platform == "js" || platform == "wasmJs")
    Files.createDirectories(workspace)
    // Only these source directories are generated here. Remove stale input files
    // while retaining the independent compiler's incremental build cache.
    for (name in listOf("src", "src@js", "src@wasmJs")) {
        val source = workspace.resolve(name)
        if (Files.exists(source)) Files.walk(source).use { paths ->
            paths.sorted(Comparator.reverseOrder()).forEach(Files::delete)
        }
    }
    fun copy(source: Path, relative: String) {
        val destination = workspace.resolve(relative)
        Files.createDirectories(destination.parent)
        Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    }
    for (name in listOf("MagnitudeMultiplication.kt", "MultiplicationPolicy539.kt", "MultiplicationPlatform539.kt")) {
        copy(kernelSources.resolve(name), "src/io/heapy/krogu/math/$name")
    }
    for (sourceSet in listOf("commonMain", "${platform}Main")) {
        val source = workerSources.resolve("$sourceSet/kotlin")
        Files.walk(source).use { paths -> paths.filter(Files::isRegularFile).forEach { path ->
            val prefix = if (sourceSet == "commonMain") "src" else "src@$platform"
            copy(path, "$prefix/${source.relativize(path)}")
        } }
    }
    copy(wrapper, "kotlin")
    copy(wrapperWindows, "kotlin.bat")
    workspace.resolve("kotlin").toFile().setExecutable(true)
    val kind = if (platform == "js") "js" else "wasm-js"
    workspace.resolve("module.yaml").writeText("""
        product: $kind/app
        settings:
          kotlin:
            version: 2.4.21
            freeCompilerArgs: ${if (platform == "js") "[-Xir-per-file=false, -module-kind=commonjs]" else "[]"}
    """.trimIndent() + "\n")
    val windows = System.getProperty("os.name").startsWith("Windows")
    val executable = workspace.resolve(if (windows) "kotlin.bat" else "kotlin").toAbsolutePath().toString()
    val command = (if (windows) listOf("cmd.exe", "/c", executable) else listOf(executable)) +
        listOf("build", "--project-dir", workspace.toAbsolutePath().toString(), "--variant", "release")
    val process = ProcessBuilder(command).directory(workspace.toFile()).inheritIO().start()
    try {
        check(process.waitFor(15, TimeUnit.MINUTES)) { "Independent worker build timed out" }
        check(process.exitValue() == 0) { "Independent worker build failed: ${process.exitValue()}" }
    } finally {
        if (process.isAlive) {
            val descendants = runCatching { process.descendants().toList().asReversed() }.getOrDefault(emptyList())
            descendants.forEach { it.destroy() }; process.destroy()
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                descendants.forEach { if (it.isAlive) it.destroyForcibly() }
                process.destroyForcibly(); process.waitFor(5, TimeUnit.SECONDS)
            }
        }
    }
    val entryName = "worker." + if (platform == "js") "js" else "mjs"
    val compilerOutput = workspace.resolve("build/artifacts/CompiledWebArtifact")
    val candidates = Files.walk(compilerOutput).use { paths ->
        paths.filter { Files.isRegularFile(it) && it.fileName.toString() == entryName }.toList()
    }
    check(candidates.size == 1) { "Expected one linked worker entry, found $candidates" }
    val main = candidates.single()
    val files = workerClosureFiles(main.parent)
    fun sha(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    val encoded = files.joinToString(",") { file ->
        val bytes = file.readBytes()
        "{\"name\":\"${file.fileName}\",\"sha256\":\"${sha(bytes)}\",\"base64\":\"${Base64.getEncoder().encodeToString(bytes)}\"}"
    }
    val identityDigest = MessageDigest.getInstance("SHA-256")
    files.forEach { identityDigest.update(it.readBytes()) }
    val identity = identityDigest.digest().joinToString("") { "%02x".format(it) }
    val runtimeKind = if (platform == "js") "js" else "wasm"
    val json = "{\"protocol\":\"krogu539.v1\",\"kind\":\"$runtimeKind\",\"identity\":\"$identity\",\"entry\":\"${main.fileName}\",\"files\":[$encoded]}"
    fun literal(value: String) = buildString {
        append('"')
        for (char in value) when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '$' -> append("\\\$")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (char.code < 32) append("\\u" + char.code.toString(16).padStart(4, '0')) else append(char)
        }
        append('"')
    }
    val expression = json.chunked(16_384).joinToString(" +\n    ", transform = ::literal)
    val output = generatedSources.resolve("io/heapy/krogu/math/EmbeddedNodeWorker539.kt")
    Files.createDirectories(output.parent)
    output.writeText("package io.heapy.krogu.math\ninternal fun embeddedNodeWorker539(): String = $expression\n")
}

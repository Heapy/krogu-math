#!/usr/bin/env kotlinr
// One source-built public Linux diagnostic. No numerical implementation or acceptance threshold change.
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

fun sha(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
fun sha(file: File): String = file.inputStream().use { input ->
    val digest = MessageDigest.getInstance("SHA-256"); val buffer = ByteArray(65536)
    while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
    digest.digest().joinToString("") { "%02x".format(it) }
}
fun js(text: String) = "\"" + text.flatMap { c -> when (c) {
    '\\' -> "\\\\"; '"' -> "\\\""; '\n' -> "\\n"; '\r' -> "\\r"; '\t' -> "\\t"
    else -> { require(c.code >= 32); c.toString() }
}.toList() }.joinToString("") + "\""
fun command(directory: File, vararg argv: String): String {
    val process = ProcessBuilder(argv.toList()).directory(directory).redirectError(ProcessBuilder.Redirect.INHERIT).start()
    val text = process.inputStream.bufferedReader().readText()
    check(process.waitFor(30, TimeUnit.SECONDS) && process.exitValue() == 0) { argv.toList() }
    return text.trimEnd()
}
fun replaceOne(text: String, old: String, replacement: String): String {
    check(text.indexOf(old) >= 0 && text.indexOf(old) == text.lastIndexOf(old)) { "Unexpected public controller anchor: $old" }
    return text.replace(old, replacement)
}
val baselineRef = "07910edf6d5687f1d89dbe3369c4546f648feed2"
val currentRef = "975182f9bca911be86ef1bc4efedc7039cc8fe36"
val publicTools = linkedMapOf(
    "node-calibration-host.cjs" to "41579f35267be6e69e92c0637e40d0a7dcea7200fdd60a276f999d099fe333d7",
    "check-public-paired-guard.py" to "cbde6eeb0920c65952817ca374d5b2562139daebe249b8c166c13766b20db32d",
    "remote-js-small-guard/remote-controller.cjs" to "322f17774b79da550e4007b90292fdf919f29357d28c018dbc179a3d2511e862",
    "independent-preflight-adoption-protocol.json" to "eb49f03d317e1a8c60c9ce272fd9541d333ae0635f9b7e31e8948a68d1e26fc4",
)
fun copyTree(from: File, to: File, skipped: MutableList<String>, prefix: String = "") {
    check(from.isDirectory && !Files.isSymbolicLink(from.toPath())); check(to.mkdirs())
    for (child in from.listFiles()!!.sortedBy { it.name }) {
        val name = prefix + child.name
        if (Files.isSymbolicLink(child.toPath())) {
            // Yarn workspace links and .bin commands are not imported by the retained Mocha host.
            skipped += name + " -> " + Files.readSymbolicLink(child.toPath())
        } else if (child.isDirectory) copyTree(child, File(to, child.name), skipped, "$name/")
        else { check(child.isFile); child.copyTo(File(to, child.name), overwrite = false) }
    }
}
fun manifest(root: File): String {
    val files = root.walkTopDown().filter { it.isFile }.toList().sortedBy { it.relativeTo(root).invariantSeparatorsPath }
    check(files.none { Files.isSymbolicLink(it.toPath()) })
    return files.joinToString("") { "${sha(it)}\t${it.relativeTo(root).invariantSeparatorsPath}\n" }
}
fun sourceManifest(root: File, ref: String): String {
    check(command(root, "git", "rev-parse", "HEAD") == ref)
    check(command(root, "git", "status", "--porcelain", "--untracked-files=no").isEmpty()) { "Tracked checkout changed" }
    val paths = command(root, "git", "ls-files").lines().filter {
        !it.startsWith("diagnostics/") && (it.startsWith("src/") || "/src/" in it ||
            it.endsWith(".gradle.kts") || it.endsWith(".properties") || it.endsWith("yarn.lock") || it == "gradle/wrapper/gradle-wrapper.jar")
    }.sorted()
    val text = File(root, "src/commonTest/kotlin/io/heapy/krogu/math/MultiplicationCalibration539.kt").readText()
    val start = text.indexOf("    private fun publicSmall")
    val end = text.indexOf("    private fun costs", start)
    check(start >= 0 && end > start && sha(text.substring(start, end).toByteArray()) == "584575df909b2800a06830b1728be604f0df92a2ecf0b353bc1c434400168f08")
    return paths.joinToString("") { "${sha(File(root, it))}\t$it\n" }
}

require(args.isNotEmpty())
when (args[0]) {
    "init" -> {
        require(args.size == 2)
        val file = File(args[1]); check(!file.exists()); file.parentFile.mkdirs()
        // Configuration only, outside both clean numerical checkouts. Same public plugin APIs as the pinned build.
        file.writeText("""
gradle.beforeProject { p ->
    p.plugins.withId('org.jetbrains.kotlin.multiplatform') { kgp ->
        def pairs = [
            ['org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin', 'org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec'],
            ['org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin', 'org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec']]
        pairs.each { pair ->
            p.plugins.withType(kgp.class.classLoader.loadClass(pair[0])) {
                def spec = p.extensions.getByType(kgp.class.classLoader.loadClass(pair[1]))
                spec.version.set('24.16.0')
                spec.download.set(false)
            }
        }
        p.afterEvaluate {
            pairs.each { pair ->
                def pluginType = kgp.class.classLoader.loadClass(pair[0])
                if (p.plugins.findPlugin(pluginType) != null) {
                    def spec = p.extensions.getByType(kgp.class.classLoader.loadClass(pair[1]))
                    if (spec.version.get() != '24.16.0' || spec.download.get() != false) throw new GradleException('Unpinned Node configuration')
                    println('PINNED_NODE539 project=' + p.path + ' type=' + pair[0] + ' version=' + spec.version.get() + ' download=' + spec.download.get())
                } else if (p == p.rootProject) throw new GradleException('Missing expected root Node plugin: ' + pair[0])
            }
        }
    }
}
""".trimStart())
    }
    "stage" -> {
        require(args.size == 6) { "stage baselineCheckout currentCheckout toolingCheckout newOutput node" }
        val checkouts = listOf(File(args[1]).canonicalFile, File(args[2]).canonicalFile)
        val tooling = File(args[3]).canonicalFile; val out = File(args[4]).canonicalFile; val node = File(args[5]).canonicalFile
        check(!out.exists() && out.mkdirs()); val payload = File(out, "payload"); check(payload.mkdir())
        val sourceTexts = checkouts.mapIndexed { i, root -> sourceManifest(root, if (i == 0) baselineRef else currentRef) }
        val sourceSha = sha(sourceTexts[1].toByteArray())
        sourceTexts.forEachIndexed { i, text -> File(out, if (i == 0) "baseline-source.tsv" else "current-source.tsv").writeText(text) }
        check(command(out, node.path, "--version") == "v24.16.0")
        val archive = File(checkouts[1], "diagnostics/platform-multiplication-539/provenance-tools.zip")
        val tools = ZipFile(archive).use { zip -> publicTools.mapValues { (name, hash) ->
            val member = requireNotNull(zip.getEntry(name)); val bytes = zip.getInputStream(member).use { it.readBytes() }
            check(sha(bytes) == hash) { "Public tool mismatch: $name" }; bytes
        } }
        val hashes = checkouts.mapIndexed { i, root ->
            val target = File(payload, if (i == 0) "baseline" else "current"); check(target.mkdir())
            val skipped = mutableListOf<String>()
            copyTree(File(root, "build/compileSync/js/test/testProductionExecutable/kotlin"), File(target, "probe"), skipped)
            check(skipped.isEmpty()) { "Compiler output symlinks" }
            val mochaRoot = File(target, "mocha"); check(mochaRoot.mkdir())
            copyTree(File(root, "build/js/node_modules"), File(mochaRoot, "node_modules"), skipped)
            File(target, "node-calibration-host.cjs").writeBytes(tools.getValue("node-calibration-host.cjs"))
            check(File(target, "probe/krogu-math-test.js").isFile && File(target, "mocha/node_modules/mocha/index.js").isFile)
            File(out, "${if (i == 0) "baseline" else "current"}-skipped-node-links.txt").writeText(skipped.joinToString("\n", postfix = "\n"))
            File(target, "manifest.sha256").writeText(manifest(target))
            sha(File(target, "manifest.sha256"))
        }
        File(payload, "check-public-paired-guard.py").writeBytes(tools.getValue("check-public-paired-guard.py"))
        File(payload, "preflight-adoption-protocol.json").writeBytes(tools.getValue("independent-preflight-adoption-protocol.json"))
        val original = tools.getValue("remote-js-small-guard/remote-controller.cjs").toString(Charsets.UTF_8)
        File(out, "public-controller-original.cjs").writeText(original)
        var controller = replaceOne(original,
            "const payload=fs.realpathSync(__dirname),output='/home/kokoko/.local/share/krogu-math/perf/runs/539-js-small-aa9-remote-1';",
            "const payload=fs.realpathSync(__dirname),output=" + js(File(out, "run").path) + ";")
        controller = replaceOne(controller,
            "check(process.argv.length===2 && process.platform==='linux' && process.arch==='x64' && process.version==='v24.16.0' && os.hostname()==='claws','Pinned host/runtime/arguments');",
            "check(process.argv.length===2 && process.platform==='linux' && process.arch==='x64' && process.version==='v24.16.0','Pinned Linux/runtime/arguments');")
        controller = replaceOne(controller,
            "check(payload==='/home/kokoko/.local/share/krogu-math/perf/artifacts/539-js-small-aa9-remote-1','Pinned payload root');",
            "check(payload===" + js(payload.path) + ",'Pinned newly built public payload root');")
        controller = replaceOne(controller,
            "check(process.execPath==='/home/kokoko/.local/share/krogu-math/perf/toolchains/node-v24.16.0-linux-x64/bin/node','Owned pinned runtime path');",
            "check(process.execPath===" + js(node.path) + ",'Pinned owned Node runtime');")
        controller = replaceOne(controller, "const sourceSHA='d9b87a77f258deeb173c2aee4b506ef93e9ce6faaf63c32779d755c31e3626ef';", "const sourceSHA=" + js(sourceSha) + ";")
        controller = replaceOne(controller,
            "const roots=['baseline','current'].map(label=>path.join(payload,label));const manifestSHAs=['65f31030ec367c26e68cf62dc92a4083888fc6a464cff8ec5df92a82f8f9623a','a0feefcc0c9738beae46b453238b41ba5e8280faa5cc104059ddba055df49e51'];",
            "const roots=['baseline','current'].map(label=>path.join(payload,label));const manifestSHAs=[" + hashes.joinToString(",") { js(it) } + "];")
        check("Kotlin2.4.21/Node24.16.0/Linux-x64/claws" in controller)
        controller = controller.replace("Kotlin2.4.21/Node24.16.0/Linux-x64/claws", "Kotlin2.4.21/Node24.16.0/Linux-x64/public-source-CI")
            .replace("Generated probe maps omitted; same exact numerical JS; Node flags clean/no enable-source-maps", "Fresh Linux compiler closure including maps; no macOS/claws byte identity; Node flags clean/no enable-source-maps")
            .replace("one fixed remote diagnostic", "one fixed public-source Linux diagnostic")
        File(payload, "remote-controller.cjs").writeText(controller)
        val diff = ProcessBuilder("diff", "-u", File(out, "public-controller-original.cjs").path, File(payload, "remote-controller.cjs").path)
            .redirectOutput(File(out, "public-controller-rebind.diff")).start()
        check(diff.waitFor(10, TimeUnit.SECONDS) && diff.exitValue() == 1)
        val script = File(tooling, "scripts/run-public-linux-js-guard.main.kts")
        File(out, "context.txt").writeText("epoch=PUBLIC_SOURCE_BUILT_LINUX_NOT_MAC_OR_CLAWS\nbaseline_ref=$baselineRef\ncurrent_ref=$currentRef\ntooling_ref=${command(tooling, "git", "rev-parse", "HEAD")}\nscript_sha256=${sha(script)}\nnode=${node.path}\nnode_sha256=${sha(node)}\npublic_tools_zip_sha256=${sha(archive)}\nsource_manifest_sha256=$sourceSha\nbaseline_manifest=${hashes[0]}\ncurrent_manifest=${hashes[1]}\ncwd=${File(".").canonicalPath}\n")
        File(out, "staged-payload.sha256").writeText(manifest(payload))
        println("STAGED_PUBLIC_SOURCE_LINUX_ONLY")
    }
    "execute" -> {
        require(args.size == 3) { "execute stagedOutput node" }
        val out = File(args[1]).canonicalFile; val node = File(args[2]).canonicalFile; val payload = File(out, "payload")
        check(!File(out, "run").exists()) { "Exactly one attempt, no overwrite/retry" }
        check(File(out, "staged-payload.sha256").readText() == manifest(payload))
        val context = File(out, "context.txt").readLines().filter { '=' in it }.associate { it.substringBefore('=') to it.substringAfter('=') }
        check(sha(node) == context.getValue("node_sha256"))
        fun owned(argv: List<String>, log: File, seconds: Long) {
            File(log.path + ".argv.txt").writeText(argv.joinToString("\n", postfix = "\n"))
            val builder = ProcessBuilder(argv).directory(payload).redirectErrorStream(true).redirectOutput(log)
            builder.environment().clear(); builder.environment()["LANG"] = "C"; builder.environment()["TZ"] = "UTC"
            val child = builder.start(); var done = false
            try { done = child.waitFor(seconds, TimeUnit.SECONDS); check(done && child.exitValue() == 0) { "Owned controller/checker failure: $log" } }
            finally { if (!done || child.isAlive) {
                val handles = child.toHandle().descendants().toList() + child.toHandle()
                handles.asReversed().forEach { if (it.isAlive) it.destroyForcibly() }
                val until = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
                var interrupted = Thread.interrupted()
                try { for (handle in handles) while (handle.isAlive && System.nanoTime() < until) {
                    try { handle.onExit().get(maxOf(1L, until - System.nanoTime()), TimeUnit.NANOSECONDS) }
                    catch (_: InterruptedException) { interrupted = true }
                    catch (_: java.util.concurrent.TimeoutException) { break }
                }; check(handles.none { it.isAlive }) { "Owned controller cleanup survivors" } }
                finally { if (interrupted) Thread.currentThread().interrupt() }
            } }
        }
        owned(listOf(node.path, File(payload, "remote-controller.cjs").path), File(out, "controller.log"), 35 * 60)
        check(File(out, "staged-payload.sha256").readText() == manifest(payload))
        owned(listOf("/usr/bin/python3", File(payload, "check-public-paired-guard.py").path, File(out, "run").path, "JS",
            context.getValue("source_manifest_sha256"), context.getValue("baseline_manifest"), context.getValue("current_manifest"), File(out, "paired-summary").path), File(out, "checker.log"), 120)
        File(out, "DIAGNOSTIC_COMPLETE_NOT_PERFORMANCE_CLEARANCE").writeText("All six raw children retained and strict inventory/statistics checked. Flags require disposition. Descriptive CPU timeline is not a causal proof or admission gate. No macOS/claws artifact identity, recovery, adoption or539 acceptance follows.\n")
    }
    else -> error("Expected init, stage or execute")
}

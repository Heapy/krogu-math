#!/usr/bin/env kotlinr

/** Run Kotlin Toolchain-linked JS/Wasm tests under pinned Node and Mocha. */
import java.io.File
import java.util.concurrent.TimeUnit

require(args.size <= 2) { "Usage: kotlinr tools/run-node-tests.main.kts [js|wasm] [test-name-pattern]" }
val platform = args.firstOrNull() ?: "js"
val pattern = args.getOrNull(1).orEmpty()
require(platform in setOf("js", "wasm"))
val root = File(".").canonicalFile
check(File(root, "module.yaml").isFile) { "Run from the project root" }
val windows = System.getProperty("os.name").startsWith("Windows")
fun run(command: List<String>, cwd: File = root, seconds: Long = 900) {
    val process = ProcessBuilder(command).directory(cwd).inheritIO().start()
    try {
        check(process.waitFor(seconds, TimeUnit.SECONDS)) { "Timed out: ${command.first()}" }
        check(process.exitValue() == 0) { "Command failed (${process.exitValue()}): $command" }
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
}
val version = ProcessBuilder("node", "--version").redirectErrorStream(true).start().let {
    val text = it.inputStream.bufferedReader().readText().trim()
    check(it.waitFor(10, TimeUnit.SECONDS) && it.exitValue() == 0)
    text
}
check(version == "v24.16.0") { "Node 24.16.0 required; found $version. Put the pinned runtime first in PATH." }
val task = if (platform == "js") "linkJsTest" else "linkWasmJsTestDebug"
val wrapper = File(root, if (windows) "kotlin.bat" else "kotlin").path
run((if (windows) listOf("cmd.exe", "/c", wrapper) else listOf(wrapper)) + listOf("task", ":krogu-math:$task"))
val tools = File(root, "tools/node-tests")
run(if (windows) listOf("cmd.exe", "/c", "npm.cmd", "ci", "--ignore-scripts", "--no-audit", "--no-fund")
    else listOf("npm", "ci", "--ignore-scripts", "--no-audit", "--no-fund"), tools)
val result = File(root, "build/node-tests/$platform").apply { mkdirs() }
val extension = if (platform == "js") "js" else "mjs"
val candidates = File(root, "build/artifacts/CompiledWebArtifact").walkTopDown().filter {
    it.isFile && it.extension == extension && it.nameWithoutExtension == "krogu-math_test" &&
        it.path.contains(if (platform == "js") "jsTest" else "wasmJsTest")
}.toList()
check(candidates.size == 1) { "Expected exactly one compiled $platform test entry, found $candidates" }
val entry = candidates.single().canonicalFile
val runner = File(result, "run.cjs")
runner.writeText("""
    const Mocha = require(process.argv[2]);
    const fs = require('node:fs');
    const mocha = new Mocha({ui:'bdd', timeout:120000, reporter:'spec',
      grep: process.argv[6] ? new RegExp(process.argv[6]) : undefined});
    mocha.addFile(process.argv[3]);
    (async () => {
      await mocha.loadFilesAsync();
      if (process.argv[5] === 'wasm') {
        // kotlin-test reads this optional transformer namespace before selecting its Mocha adapter.
        globalThis.kotlinTest = {};
        const testModule = await import(require('node:url').pathToFileURL(process.argv[3]).href);
        if (typeof testModule.startUnitTests !== 'function') throw Error('Missing Kotlin/Wasm test registration entry');
        testModule.startUnitTests();
      }
      const runner = mocha.run(failures => { process.exitCode = failures || runner.stats.tests === 0 ? 1 : 0; });
      const cases = [];
      runner.on('pass', test => cases.push({name:test.fullTitle(),status:'passed',durationMs:test.duration}));
      runner.on('fail', (test,error) => cases.push({name:test.fullTitle(),status:'failed',error:String(error.stack||error)}));
      runner.on('pending', test => cases.push({name:test.fullTitle(),status:'skipped'}));
      runner.on('end', () => {
        fs.writeFileSync(process.argv[4], JSON.stringify({node:process.version,platform:process.argv[5],entry:process.argv[3],filter:process.argv[6]||null,stats:runner.stats,cases},null,2)+'\n');
        if (runner.stats.tests === 0) { console.error('No tests discovered'); process.exitCode = 1; }
      });
    })().catch(error => { console.error(error); process.exitCode = 1; });
""".trimIndent() + "\n")
run(listOf("node", runner.path, File(tools, "node_modules/mocha").canonicalPath, entry.path,
    File(result, "results.json").path, platform, pattern), seconds = 1800)
println("NODE_TESTS_COMPLETE platform=$platform entry=$entry results=${File(result, "results.json")}")

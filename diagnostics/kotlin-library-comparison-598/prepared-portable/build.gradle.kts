import groovy.json.JsonSlurper
import java.nio.file.Files
import java.security.MessageDigest
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin

plugins { kotlin("multiplatform") version "2.4.21" }

// Independent correctness consumer. No root source/project dependency or publication.
val repositoryPath = providers.gradleProperty("kroguRepository").get()
val kroguVersion = providers.gradleProperty("kroguVersion").get()
val kroguCommit = providers.gradleProperty("kroguCommit").get().also { require(it.matches(Regex("[a-f0-9]{40}"))) }
val ledgerFile = file(providers.gradleProperty("publicationLedger").get())
val ledgerSha = providers.gradleProperty("publicationLedgerSha256").get()
val nodeFile = file(providers.gradleProperty("nodeExecutable").get()).canonicalFile
val nodeSha = providers.gradleProperty("nodeSha256").get()
fun sha(file: File): String = file.inputStream().use { input ->
    val digest = MessageDigest.getInstance("SHA-256"); val buffer = ByteArray(65536)
    while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
    digest.digest().joinToString("") { "%02x".format(it) }
}
fun verifyInputs598() {
    require(ledgerSha.matches(Regex("[a-f0-9]{64}")) && sha(ledgerFile) == ledgerSha)
    require(nodeSha.matches(Regex("[a-f0-9]{64}")) && sha(nodeFile) == nodeSha)
    require(System.getenv("PATH").split(File.pathSeparator).first() == nodeFile.parentFile.path) { "Put the exact pinned Node first in PATH" }
    val root = file(repositoryPath).canonicalFile
    val ledger = JsonSlurper().parse(ledgerFile) as Map<*, *>
    require(ledger["sourceCommit"] == kroguCommit && ledger["group"] == "io.heapy.krogu" &&
        ledger["module"] == "krogu-math" && ledger["version"] == kroguVersion)
    val expected = mutableSetOf<String>()
    for (entry in ledger["files"] as List<*>) {
        val record = entry as Map<*, *>; val path = record["path"] as String
        require(path.isNotEmpty() && !path.startsWith('/') && path.split('/').none { it in listOf("", ".", "..") })
        require(expected.add(path))
        val artifact = root.resolve(path).canonicalFile
        require(artifact.toPath().startsWith(root.toPath()) && artifact.isFile)
        require(artifact.length() == (record["bytes"] as Number).toLong() && sha(artifact) == record["sha256"])
    }
    val actual = root.walkTopDown().onEnter { require(!Files.isSymbolicLink(it.toPath())); true }
        .filter { it.isFile }.map { require(!Files.isSymbolicLink(it.toPath())); it.relativeTo(root).invariantSeparatorsPath }.toSet()
    require(actual == expected) { "Unindexed or missing isolated publication files" }
    require(expected.any { it.endsWith(".module") } && expected.count { it.endsWith(".klib") } >= 3)
}
verifyInputs598()
repositories {
    exclusiveContent {
        forRepository { maven { url = uri(repositoryPath) } }
        filter { includeGroup("io.heapy.krogu") }
    }
    mavenCentral()
}
plugins.withType<NodeJsPlugin> {
    extensions.getByType<NodeJsEnvSpec>().apply { version = "24.16.0"; download = false }
}
plugins.withType<WasmNodeJsPlugin> {
    extensions.getByType<WasmNodeJsEnvSpec>().apply { version = "24.16.0"; download = false }
}
kotlin {
    js {
        nodejs(); binaries.executable()
        compilerOptions { moduleKind.set(JsModuleKind.MODULE_COMMONJS) }
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs(); binaries.executable() }
    macosArm64 { binaries.executable { entryPoint = "preparation598.main" } }
    sourceSets.commonMain {
        // Reuse reviewed adapter sources; exclude JVM/JDK/KBigNum and JVM fixtures.
        kotlin.srcDir(file("../prepared-harness"))
        kotlin.srcDir(file("../prepared-timing/src/commonMain/kotlin/preparation598"))
        kotlin.include("**/PortableUntimedMain.kt", "TextWork598.kt", "Contract.kt", "KroguAdapter.kt", "IonsPinAdapter.kt")
        dependencies {
            implementation("io.heapy.krogu:krogu-math:$kroguVersion")
            implementation("com.ionspin.kotlin:bignum:0.3.10")
        }
    }
}
dependencyLocking { lockAllConfigurations() }
configurations.configureEach { resolutionStrategy.failOnVersionConflict() }
configurations.configureEach { resolutionStrategy.force(*listOf("kotlin-stdlib", "kotlin-stdlib-common", "kotlin-stdlib-jdk7", "kotlin-stdlib-jdk8", "kotlin-stdlib-js", "kotlin-stdlib-wasm-js", "kotlin-dom-api-compat").map { "org.jetbrains.kotlin:$it:2.4.21" }.toTypedArray()) }
val verify598Inputs = tasks.register("verify598Inputs") { doLast { verifyInputs598() } }
tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach { dependsOn(verify598Inputs) }
val runtasks = setOf("jsNodeProductionRun", "wasmJsNodeProductionRun", "runReleaseExecutableMacosArm64")
val reserve598Run = tasks.register("reserve598Run") {
    doLast {
        val name = providers.gradleProperty("runName").get().also { require(it.matches(Regex("[A-Za-z0-9_-]+"))) }
        val selected = gradle.startParameter.taskNames.filter { it.substringAfterLast(':') in runtasks }
        require(selected.size == 1) { "One correctness target per invocation, preserving separate attempts" }
        val output = file("runs/$name"); check(!output.exists() && output.mkdirs())
        output.resolve("inputs.txt").writeText(buildString {
            appendLine("UNTIMED_ONLY; source/API compatibility remains unverified until actual execution")
            appendLine("target_task=${selected.single()}"); appendLine("krogu_commit=$kroguCommit")
            appendLine("publication_ledger_sha256=$ledgerSha"); appendLine("node_sha256=$nodeSha")
            appendLine("gradle=${gradle.gradleVersion}"); appendLine("java_runtime=${System.getProperty("java.runtime.version")}")
            for (input in projectDir.walkTopDown().filter { it.isFile && it.extension in setOf("kt", "kts", "properties") &&
                !it.toPath().startsWith(file("build").toPath()) && !it.toPath().startsWith(file("runs").toPath()) }.sortedBy { it.path })
                appendLine("${sha(input)}\t${input.relativeTo(projectDir).invariantSeparatorsPath}")
            val textSource=file("../prepared-timing/src/commonMain/kotlin/preparation598/TextWork598.kt"); appendLine("${sha(textSource)}\t${textSource.canonicalPath}")
            for (name in listOf("Contract.kt", "KroguAdapter.kt", "IonsPinAdapter.kt")) {
                val input = file("../prepared-harness/$name"); appendLine("${sha(input)}\t${input.canonicalPath}")
            }
        })
    }
}
tasks.matching { it.name in runtasks }.configureEach {
    dependsOn(verify598Inputs, reserve598Run)
    doLast {
        verifyInputs598()
        file("runs/${providers.gradleProperty("runName").get()}/PROCESS_TASK_RETURNED").writeText(
            "Gradle run task returned successfully; retain stdout with COMPLETE598_UNTIMED_ONLY and independently audit it. Not a performance verdict.\n")
    }
}

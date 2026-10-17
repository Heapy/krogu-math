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
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of(25)); vendor.set(JvmVendorSpec.BELLSOFT) }
    jvm()
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
        kotlin.include("**/Timing598.kt", "**/TextWork598.kt", "**/TimingUntimed598.kt", "Contract.kt", "KroguAdapter.kt", "IonsPinAdapter.kt")
        dependencies {
            implementation("io.heapy.krogu:krogu-math:$kroguVersion")
            implementation("com.ionspin.kotlin:bignum:0.3.10")
        }
    }
    sourceSets.jvmMain {
        kotlin.srcDir(file("../prepared-harness"))
        kotlin.include("**/PlatformTiming598.kt", "JdkAdapter.kt", "KBigNumAdapter.kt", "UntimedFixtures.kt")
        dependencies { implementation("io.github.gatrongdev:kbignum-desktop:0.0.20") }
    }
}
dependencyLocking { lockAllConfigurations() }
configurations.configureEach { resolutionStrategy.failOnVersionConflict() }
configurations.configureEach { resolutionStrategy.force(*listOf("kotlin-stdlib", "kotlin-stdlib-common", "kotlin-stdlib-jdk7", "kotlin-stdlib-jdk8", "kotlin-stdlib-js", "kotlin-stdlib-wasm-js", "kotlin-dom-api-compat").map { "org.jetbrains.kotlin:$it:2.4.21" }.toTypedArray()) }
val verify598Inputs = tasks.register("verify598Inputs") { doLast { verifyInputs598() } }
tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach { dependsOn(verify598Inputs) }

// Prepare immutable runtime closures; the external controller never invokes Gradle.
fun freeze598(directory: File, kind: String, entry: String) {
    val sourceFiles=(projectDir.walkTopDown().filter { it.isFile && it.extension in setOf("kt","kts","properties") &&
        !it.toPath().startsWith(file("build").toPath()) }.toList() +
        listOf("Contract.kt","KroguAdapter.kt","IonsPinAdapter.kt","JdkAdapter.kt","KBigNumAdapter.kt","UntimedFixtures.kt").map { file("../prepared-harness/$it") }).distinct()
    directory.resolve("sources.sha256").writeText(sourceFiles.sortedBy { it.canonicalPath }.joinToString("\n",postfix="\n") { "${sha(it)}\t${it.canonicalPath}" })
    directory.resolve("artifact.properties").writeText("platform=$kind\nkotlin=2.4.21\nkrogu_commit=$kroguCommit\npublication_ledger_sha256=$ledgerSha\nionspin=0.3.10\nkbignum=0.0.20\nentry=$entry\n")
    val target=when(kind) { "JVM"->"jvm"; "JS"->"js"; "Wasm"->"wasmJs"; else->"macosArm64" }
    val dependencyFiles=kotlin.targets.getByName(target).compilations.getByName("main").compileDependencyFiles.files
        .flatMap { if(it.isFile) listOf(it) else it.walkTopDown().filter{f->f.isFile}.toList() }.distinct()
    directory.resolve("compile-inputs.sha256").writeText(dependencyFiles.sortedBy{it.canonicalPath}.joinToString("\n",postfix="\n") { "${sha(it)}\t${it.canonicalPath}" })
    val members=directory.walkTopDown().filter { it.isFile && it.name != "manifest.sha256" }.sortedBy { it.relativeTo(directory).invariantSeparatorsPath }.toList()
    directory.resolve("manifest.sha256").writeText(members.joinToString("\n",postfix="\n") { "${sha(it)}\t${it.relativeTo(directory).invariantSeparatorsPath}" })
}
val jvmJar598=tasks.named<Jar>("jvmJar")
tasks.register<Sync>("stageJvmTiming598") {
    dependsOn(jvmJar598,verify598Inputs)
    into(layout.buildDirectory.dir("timing-runtime/JVM"))
    from(configurations.named("jvmRuntimeClasspath")) { into("lib") }
    from(jvmJar598) { into("lib") }
    duplicatesStrategy=DuplicatesStrategy.FAIL
    doLast {
        val directory=layout.buildDirectory.dir("timing-runtime/JVM").get().asFile
        directory.resolve("classpath.txt").writeText(directory.resolve("lib").listFiles()!!.filter { it.extension=="jar" }.sortedBy { it.name }.joinToString("\n",postfix="\n") { "lib/${it.name}" })
        freeze598(directory,"JVM","preparation598.Timing598Kt")
    }
}
for((target,kind) in listOf("js" to "JS","wasmJs" to "Wasm")) tasks.register<Sync>("stage${kind}Timing598") {
    dependsOn("${target}ProductionExecutableCompileSync",verify598Inputs)
    into(layout.buildDirectory.dir("timing-runtime/$kind"))
    from(layout.buildDirectory.dir("compileSync/$target/main/productionExecutable/kotlin"))
    doLast {
        val directory=layout.buildDirectory.dir("timing-runtime/$kind").get().asFile
        val entry="peer598-timing."+if(kind=="JS") "js" else "mjs"
        check(directory.resolve(entry).isFile) { "Unexpected compiler entry; retain failure and bind actual own emitted closure before use" }
        freeze598(directory,kind,entry)
    }
}
tasks.register<Sync>("stageNativeTiming598") {
    dependsOn("linkReleaseExecutableMacosArm64",verify598Inputs)
    into(layout.buildDirectory.dir("timing-runtime/Native"))
    from(layout.buildDirectory.file("bin/macosArm64/releaseExecutable/peer598-timing.kexe"))
    doLast {
        val directory=layout.buildDirectory.dir("timing-runtime/Native").get().asFile
        check(directory.resolve("peer598-timing.kexe").isFile && directory.resolve("peer598-timing.kexe").setExecutable(true,true))
        freeze598(directory,"Native","peer598-timing.kexe")
    }
}

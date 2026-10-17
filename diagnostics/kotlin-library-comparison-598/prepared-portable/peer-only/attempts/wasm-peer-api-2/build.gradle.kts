import java.security.MessageDigest
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin

plugins { kotlin("multiplatform") version "2.4.21" }

// Independent peer API/correctness preparation; never included by a root build.
// No Krogu artifact/property, no accepted539 bypass, no numerical timing.
val harnessRoot=file("../../prepared-harness")
val gateSource=file("../src/commonMain/kotlin/preparation598/PortableUntimedMain.kt")
val expectedGateSha="40ad739770b44b1425e057752d4f7e51fe9abce604b127ce63eafd5393a7072a"
val nodeFile=file(providers.gradleProperty("nodeExecutable").get()).canonicalFile
val nodeSha=providers.gradleProperty("nodeSha256").get().also { require(it.matches(Regex("[a-f0-9]{64}"))) }
fun sha598(file:File):String=file.inputStream().use { stream ->
    val digest=MessageDigest.getInstance("SHA-256");val buffer=ByteArray(65536)
    while(true) { val n=stream.read(buffer);if(n<0)break;digest.update(buffer,0,n) }
    digest.digest().joinToString("") { "%02x".format(it) }
}
fun verifyPeerInputs598() {
    check(sha598(gateSource)==expectedGateSha) { "Changed reviewed portable fixture/gate" }
    check(sha598(nodeFile)==nodeSha) { "Changed pinned Node executable" }
    check(System.getenv("PATH").split(File.pathSeparator).first()==nodeFile.parentFile.path) { "Pinned Node must be first in PATH" }
}
verifyPeerInputs598()
val generated=layout.buildDirectory.dir("generated/peerOnly598")
val generatePeerGate598=tasks.register("generatePeerGate598") {
    inputs.file(gateSource);outputs.dir(generated)
    doLast {
        verifyPeerInputs598()
        var source=gateSource.readText()
        val replacements=listOf(
            "fun main() {" to "fun peerOnlyPortableGate598() {",
            "peer(KroguAdapter); peer(IonsPinAdapter)" to "peer(IonsPinAdapter)",
            "check(core == 128 && distinct in 0..32)" to "check(core == 64 && distinct in 0..16)",
        )
        for((before,after) in replacements) { check(source.split(before).size==2);source=source.replace(before,after) }
        val output=generated.get().file("preparation598/PortableUntimedPeerGate598.kt").asFile
        output.parentFile.mkdirs();output.writeText(source)
    }
}
plugins.withType<NodeJsPlugin> {
    extensions.getByType<NodeJsEnvSpec>().apply { version="24.16.0";download=false }
}
kotlin {
    js { nodejs();binaries.executable();compilerOptions { moduleKind.set(JsModuleKind.MODULE_COMMONJS) } }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs();binaries.executable() }
    macosArm64 { binaries.executable() }
    sourceSets {
        commonMain {
            kotlin.setSrcDirs(listOf(file("src/commonMain/kotlin"),harnessRoot,generated))
            kotlin.include("**/PeerOnlyPortableMain.kt","**/PortableUntimedPeerGate598.kt","Contract.kt","IonsPinAdapter.kt")
            dependencies { implementation("com.ionspin.kotlin:bignum:0.3.10") }
        }
        jsMain { kotlin.setSrcDirs(listOf(file("../src/jsMain/kotlin"))) }
        wasmJsMain { kotlin.setSrcDirs(listOf(file("../src/wasmJsMain/kotlin"))) }
        macosArm64Main { kotlin.setSrcDirs(listOf(file("../src/macosArm64Main/kotlin"))) }
    }
}
dependencyLocking { lockAllConfigurations() }
configurations.configureEach {
    resolutionStrategy.failOnVersionConflict()
    resolutionStrategy.force(*listOf("kotlin-stdlib","kotlin-stdlib-common","kotlin-stdlib-jdk7","kotlin-stdlib-jdk8","kotlin-stdlib-js","kotlin-stdlib-wasm-js","kotlin-dom-api-compat").map { "org.jetbrains.kotlin:$it:2.4.21" }.toTypedArray())
}
tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach { dependsOn(generatePeerGate598) }
val runTasks598=setOf("jsNodeProductionRun","wasmJsNodeProductionRun","runReleaseExecutableMacosArm64")
val reservePeerRun598=tasks.register("reservePeerRun598") {
    dependsOn(generatePeerGate598)
    doLast {
        verifyPeerInputs598()
        val selected=gradle.startParameter.taskNames.filter { it.substringAfterLast(':') in runTasks598 }
        check(selected.size==1) { "One portable target per invocation" }
        val name=providers.gradleProperty("runName").get().also { require(it.matches(Regex("[A-Za-z0-9_-]+"))) }
        val output=file("runs/$name");check(!output.exists()&&output.mkdirs()) { "Never overwrite attempt" }
        output.resolve("inputs.txt").writeText(buildString {
            appendLine("PEER_ONLY_PREPARATION_NO_KROGU_NO_TIMING_NO_FINAL598_ACCEPTANCE")
            appendLine("selected_task=${selected.single()}");appendLine("gradle=${gradle.gradleVersion}")
            appendLine("host_os=${System.getProperty("os.name")}");appendLine("host_arch=${System.getProperty("os.arch")}")
            appendLine("build_java=${System.getProperty("java.runtime.version")}");appendLine("node=${nodeFile.path}")
            appendLine("node_sha256=$nodeSha");appendLine("original_gate_sha256=$expectedGateSha")
            val sources=listOf(gateSource,harnessRoot.resolve("Contract.kt"),harnessRoot.resolve("IonsPinAdapter.kt"),file("build.gradle.kts"),file("settings.gradle.kts"),file("src/commonMain/kotlin/preparation598/PeerOnlyPortableMain.kt"))+
                listOf("jsMain","wasmJsMain","macosArm64Main").map { file("../src/$it/kotlin/preparation598/Runtime598.kt") }
            for(source in sources)appendLine("source:${sha598(source)}\t${source.canonicalPath}")
            val generatedFile=generated.get().file("preparation598/PortableUntimedPeerGate598.kt").asFile
            if(generatedFile.isFile)appendLine("generated:${sha598(generatedFile)}\t${generatedFile.canonicalPath}")
        })
    }
}
tasks.matching { it.name in runTasks598 }.configureEach {
    dependsOn(reservePeerRun598)
    doLast {
        verifyPeerInputs598()
        val output=file("runs/${providers.gradleProperty("runName").get()}")
        output.resolve("resolved-files.txt").writeText(buildString {
            for(configuration in configurations.filter { it.state==Configuration.State.RESOLVED }.sortedBy { it.name }) {
                for(artifact in configuration.files.filter { it.isFile }.sortedBy { it.absolutePath })appendLine("${configuration.name}\t${sha598(artifact)}\t${artifact.canonicalPath}")
            }
            val selected=gradle.startParameter.taskNames.single { it.substringAfterLast(':') in runTasks598 }.substringAfterLast(':')
            val artifactRoot=layout.buildDirectory.dir(when(selected) {
                "jsNodeProductionRun" -> "compileSync/js/main/productionExecutable/kotlin"
                "wasmJsNodeProductionRun" -> "compileSync/wasmJs/main/productionExecutable/kotlin"
                else -> "bin/macosArm64/releaseExecutable"
            }).get().asFile
            check(artifactRoot.isDirectory) { "Missing target artifact closure: $artifactRoot" }
            for(artifact in artifactRoot.walkTopDown().filter { it.isFile }.sortedBy { it.path })appendLine("compiled\t${sha598(artifact)}\t${artifact.canonicalPath}")
        })
        output.resolve("PROCESS_TASK_RETURNED").writeText("Gradle target returned successfully. Check retained COMPLETE598_PEER_ONLY_PREPARATION/stdout; no timing or full598 acceptance.\n")
    }
}

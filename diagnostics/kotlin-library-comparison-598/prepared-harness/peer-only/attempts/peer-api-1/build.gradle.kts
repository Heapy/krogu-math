import java.security.MessageDigest

plugins { kotlin("jvm") version "2.4.21" }

// Independent API/correctness preparation: no Krogu dependency/input, timing or publication.
val peerSources = listOf("Contract.kt", "IonsPinAdapter.kt", "KBigNumAdapter.kt", "JdkAdapter.kt", "UntimedFixtures.kt")
fun hash598(input: File): String = input.inputStream().use { stream ->
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(65536)
    while (true) { val n=stream.read(buffer); if(n<0)break; digest.update(buffer,0,n) }
    digest.digest().joinToString("") { "%02x".format(it) }
}
kotlin {
    jvmToolchain { languageVersion.set(JavaLanguageVersion.of(25)); vendor.set(JvmVendorSpec.BELLSOFT) }
    sourceSets.named("main") {
        kotlin.setSrcDirs(listOf(projectDir.parentFile, projectDir))
        kotlin.include(*(peerSources + "PeerOnlyMain.kt").toTypedArray())
    }
}
dependencies {
    implementation("com.ionspin.kotlin:bignum:0.3.10")
    implementation("io.github.gatrongdev:kbignum-desktop:0.0.20")
    implementation(kotlin("stdlib", "2.4.21"))
}
dependencyLocking { lockAllConfigurations() }
configurations.configureEach {
    resolutionStrategy.failOnVersionConflict()
    resolutionStrategy.force("org.jetbrains.kotlin:kotlin-stdlib:2.4.21")
}
val runPeerOnly by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named("classes"))
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25)); vendor.set(JvmVendorSpec.BELLSOFT)
    })
    classpath=sourceSets["main"].runtimeClasspath
    mainClass.set("preparation598.PeerOnlyMainKt")
    maxHeapSize="512m"
    jvmArgs("-XX:+UseSerialGC", "-XX:ActiveProcessorCount=1", "-XX:-OmitStackTraceInFastThrow")
    val before = mutableMapOf<File,String>()
    doFirst {
        val name=providers.gradleProperty("runName").get().also { require(it.matches(Regex("[A-Za-z0-9_-]+"))) }
        val out=layout.projectDirectory.dir("runs/$name").asFile
        check(!out.exists() && out.mkdirs()) { "Never overwrite previous preflight" }
        args(out.resolve("observations.tsv").absolutePath)
        val inputs=(peerSources.map { projectDir.parentFile.resolve(it) } + listOf("PeerOnlyMain.kt", "build.gradle.kts", "settings.gradle.kts").map { projectDir.resolve(it) }).sortedBy { it.absolutePath }
        before.clear(); inputs.forEach { before[it]=hash598(it) }
        out.resolve("resolved-inputs.txt").writeText(buildString {
            appendLine("PREPARATION_ONLY_NO_KROGU_NO_TIMING_NO_FINAL598_ACCEPTANCE")
            appendLine("gradle=${gradle.gradleVersion}")
            appendLine("launcher=${javaLauncher.get().executablePath}")
            appendLine("launcher_sha256=${hash598(javaLauncher.get().executablePath.asFile)}")
            before.forEach { (f,h) -> appendLine("source:$h\t${f.absolutePath}") }
            classpath.files.sortedBy { it.absolutePath }.forEach { f ->
                if(f.isFile) appendLine("classpath:${hash598(f)}\t${f.absolutePath}")
                else f.walkTopDown().filter { it.isFile }.sortedBy { it.absolutePath }.forEach { appendLine("class:${hash598(it)}\t${it.absolutePath}") }
            }
        })
    }
    doLast { before.forEach { (f,h) -> check(hash598(f)==h) { "Changed source during preflight: $f" } } }
}

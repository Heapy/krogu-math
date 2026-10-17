import java.security.MessageDigest
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { kotlin("jvm") version "2.4.21" }

// Standalone/unpublished consumer build. Never included by root settings.gradle.kts.
val kroguJar = file(providers.gradleProperty("kroguJar").get())
val kroguSha = providers.gradleProperty("kroguJarSha256").get().also { require(it.matches(Regex("[a-f0-9]{64}"))) }
val kroguCommit = providers.gradleProperty("kroguCommit").get().also { require(it.matches(Regex("[a-f0-9]{40}"))) }
fun sha(file: File): String = file.inputStream().use { input ->
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(65536)
    while (true) { val count = input.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
    digest.digest().joinToString("") { "%02x".format(it) }
}
val verifyKroguInput by tasks.registering {
    inputs.file(kroguJar)
    doLast { check(kroguJar.isFile && sha(kroguJar) == kroguSha) { "Missing/changed final Krogu JAR" } }
}
kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.BELLSOFT)
    }
    sourceSets.named("main") { kotlin.setSrcDirs(listOf(projectDir, file("../prepared-timing/src/commonMain/kotlin/preparation598"))); kotlin.include("Contract.kt", "KroguAdapter.kt", "IonsPinAdapter.kt", "JdkAdapter.kt", "KBigNumAdapter.kt", "UntimedFixtures.kt", "UntimedMain.kt", "TextWork598.kt") }
}
dependencies {
    implementation(files(kroguJar))
    implementation("com.ionspin.kotlin:bignum:0.3.10")
    implementation("io.github.gatrongdev:kbignum-desktop:0.0.20")
    implementation(kotlin("stdlib", "2.4.21"))
}
dependencyLocking { lockAllConfigurations() }
configurations.configureEach { resolutionStrategy.failOnVersionConflict() }
// Pin the shared stdlib instead of allowing older peer declarations to pick the runtime.
configurations.configureEach { resolutionStrategy.force(*listOf("kotlin-stdlib", "kotlin-stdlib-common", "kotlin-stdlib-jdk7", "kotlin-stdlib-jdk8").map { "org.jetbrains.kotlin:$it:2.4.21" }.toTypedArray()) }
tasks.withType<KotlinCompile>().configureEach { dependsOn(verifyKroguInput) }

val runUntimed by tasks.registering(JavaExec::class) {
    dependsOn(tasks.named("classes"), verifyKroguInput)
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(25)); vendor.set(JvmVendorSpec.BELLSOFT)
    })
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("preparation598.UntimedMainKt")
    maxHeapSize = "512m"
    jvmArgs("-XX:+UseSerialGC", "-XX:ActiveProcessorCount=1", "-XX:-OmitStackTraceInFastThrow")
    doFirst {
        val runName = providers.gradleProperty("runName").get().also { require(it.matches(Regex("[A-Za-z0-9_-]+"))) }
        val output = layout.projectDirectory.dir("runs/$runName").asFile
        check(!output.exists()) { "Never overwrite prior attempt: $output" }
        check(output.mkdirs())
        args(output.resolve("observations.tsv").absolutePath)
        output.resolve("resolved-inputs.txt").writeText(buildString {
            appendLine("krogu_commit=$kroguCommit")
            appendLine("krogu_sha256=$kroguSha")
            appendLine("gradle=${gradle.gradleVersion}")
            appendLine("launcher=${javaLauncher.get().executablePath}")
            for (input in classpath.files.sortedBy { it.absolutePath }) {
                if (input.isFile) appendLine("${sha(input)}\t${input.absolutePath}")
                else input.walkTopDown().filter { it.isFile }.sortedBy { it.absolutePath }.forEach {
                    appendLine("${sha(it)}\t${it.absolutePath}")
                }
            }
            (projectDir.listFiles()!!.filter { it.isFile && it.extension in setOf("kt", "kts", "properties") } + file("../prepared-timing/src/commonMain/kotlin/preparation598/TextWork598.kt")).sortedBy { it.name }.forEach {
                appendLine("source:${sha(it)}\t${it.name}")
            }
        })
    }
    doLast { check(sha(kroguJar) == kroguSha) { "Krogu changed during untimed checks" } }
}

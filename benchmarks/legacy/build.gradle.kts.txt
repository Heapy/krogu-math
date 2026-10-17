plugins { kotlin("multiplatform") }

kotlin {
    jvm()
    js { nodejs(); binaries.executable() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs(); binaries.executable() }
    macosArm64 { binaries.executable { entryPoint = "bench.main" } }
    macosX64 { binaries.executable { entryPoint = "bench.main" } }
    linuxX64 { binaries.executable { entryPoint = "bench.main" } }
    linuxArm64 { binaries.executable { entryPoint = "bench.main" } }
    mingwX64 { binaries.executable { entryPoint = "bench.main" } }
    jvmToolchain(25)
    sourceSets.commonMain.dependencies { implementation(project(":")) }
}

val jvm = kotlin.targets.getByName("jvm") as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
val jvmMain = jvm.compilations.getByName("main")
tasks.register<JavaExec>("jvmBenchmark") {
    dependsOn("jvmMainClasses")
    classpath(jvmMain.output.allOutputs, jvmMain.runtimeDependencyFiles)
    mainClass.set("bench.MainKt")
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(25)) })
    args(providers.gradleProperty("benchArgs").orElse("").get().split(" ").filter(String::isNotEmpty))
}
// Runner launches java directly so Gradle startup/build memory never enters process RSS.
tasks.register("jvmClasspath") {
    dependsOn("jvmMainClasses")
    doLast { layout.buildDirectory.file("jvm-classpath.txt").get().asFile.writeText(
        (jvmMain.output.allOutputs + jvmMain.runtimeDependencyFiles).asPath) }
}

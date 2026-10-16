plugins {
    kotlin("multiplatform") version "2.4.21"
    `maven-publish`
}

group = "io.heapy.krogu"
version = "0.1.0-SNAPSHOT"

// Test tooling only. Kotlin's reporter requires Mocha 11; pin its patched transitive packages.
plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    extensions.configure<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension> {
        resolution("serialize-javascript", "7.1.2")
        resolution("diff", "8.0.3")
    }
}
plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnPlugin> {
    extensions.configure<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootExtension> {
        resolution("serialize-javascript", "7.1.2")
        resolution("diff", "8.0.3")
    }
}

kotlin {
    jvm()
    js { nodejs { testTask { useMocha { timeout = "120s" } } } }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    macosArm64()
    macosX64()
    linuxX64()
    linuxArm64()
    mingwX64()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    jvmToolchain(25)
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
        val nonJvmMain = create("nonJvmMain") { dependsOn(commonMain.get()) }
        jsMain { dependsOn(nonJvmMain) }
        wasmJsMain { dependsOn(nonJvmMain) }
        nativeMain { dependsOn(nonJvmMain) }
    }
}

tasks.withType<Test>().configureEach {
    // Keep public-JDK exception messages stable after repeated implicit throws.
    jvmArgs("-XX:-OmitStackTraceInFastThrow")
    testLogging { events("failed", "skipped"); exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL }
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        name.set("krogu-math")
        description.set("Independent common Kotlin implementation of the java.math API")
        url.set("https://github.com/Heapy/krogu-math")
        licenses { license { name.set("Apache License 2.0"); url.set("https://www.apache.org/licenses/LICENSE-2.0") } }
        scm { url.set("https://github.com/Heapy/krogu-math"); connection.set("scm:git:https://github.com/Heapy/krogu-math.git") }
    }
}

// Opt-in optimized Native diagnostic test entry; ordinary test binaries are unchanged.
// Native threshold calibration must not mistake a DEBUG test executable for production cost.
if (providers.gradleProperty("multiplication539").orNull == "true") {
    kotlin.targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.test("calibration539", listOf(org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE))
    }
}

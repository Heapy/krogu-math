// Unpublished diagnostic only. No root-project dependency: duplicate FQCNs cannot
// accidentally shadow the production library in one runtime classpath.
plugins { kotlin("multiplatform") }
kotlin {
    js { nodejs(); binaries.executable() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs(); binaries.executable() }
    sourceSets.commonMain {
        // Compile the SAME original source files, not copied/reimplemented kernels.
        kotlin.srcDir(rootProject.file("src/commonMain/kotlin"))
        kotlin.include("calibration539/**", "io/heapy/krogu/math/MagnitudeMultiplication.kt",
            "io/heapy/krogu/math/MultiplicationPolicy539.kt", "io/heapy/krogu/math/MultiplicationPlatform539.kt")
    }
    sourceSets.jsMain {
        kotlin.srcDir(rootProject.file("src/jsMain/kotlin"))
        kotlin.include("calibration539/**", "io/heapy/krogu/math/MultiplicationPlatform539.kt")
    }
    sourceSets.wasmJsMain {
        kotlin.srcDir(rootProject.file("src/wasmJsMain/kotlin"))
        kotlin.include("calibration539/**", "io/heapy/krogu/math/MultiplicationPlatform539.kt")
    }
}

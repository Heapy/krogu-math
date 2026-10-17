import java.security.MessageDigest
import java.util.Base64

plugins { kotlin("multiplatform") }
kotlin {
    js { nodejs(); binaries.executable() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs(); binaries.executable() }
    sourceSets.commonMain {
        kotlin.srcDir(rootProject.file("src/commonMain/kotlin"))
        kotlin.include("nodeworker539/**", "io/heapy/krogu/math/MagnitudeMultiplication.kt",
            "io/heapy/krogu/math/MultiplicationPolicy539.kt", "io/heapy/krogu/math/MultiplicationPlatform539.kt")
    }
}

// The generated source is consumed by root KLIB compilation. This project has no
// root dependency and never consumes root-generated sources: no build cycle.
for ((target, platform) in listOf("js" to "js", "wasmJs" to "wasm")) {
    val output = layout.buildDirectory.dir("generated/payload/$target")
    tasks.register("generate${target.replaceFirstChar { it.uppercase() }}Payload539") {
        dependsOn("${target}ProductionExecutableCompileSync")
        val files = layout.buildDirectory.dir("compileSync/$target/main/productionExecutable/kotlin")
        inputs.dir(files)
        inputs.files(rootProject.file("src/commonMain/kotlin/io/heapy/krogu/math/MagnitudeMultiplication.kt"),
            rootProject.file("src/commonMain/kotlin/io/heapy/krogu/math/MultiplicationPolicy539.kt"))
        outputs.dir(output)
        doLast {
            val root = files.get().asFile
            val entries = root.walkTopDown().filter { it.isFile && !it.name.endsWith(".map") }.sortedBy { it.name }.toList()
            require(entries.isNotEmpty() && entries.all { it.parentFile == root && it.name.matches(Regex("[A-Za-z0-9_.-]+")) })
            require(entries.sumOf { it.length() } <= 4L * 1024 * 1024) { "Worker closure unexpectedly large" }
            val expectedEntry = "krogu-math-nodeWorker539." + if (platform == "js") "js" else "mjs"
            val main = entries.single { it.name == expectedEntry }
            fun sha(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            val encoded = entries.joinToString(",") { file ->
                val bytes = file.readBytes()
                "{\"name\":\"${file.name}\",\"sha256\":\"${sha(bytes)}\",\"base64\":\"${Base64.getEncoder().encodeToString(bytes)}\"}"
            }
            val identity = sha(entries.flatMap { it.readBytes().toList() }.toByteArray())
            val json = "{\"protocol\":\"krogu539.v1\",\"kind\":\"$platform\",\"identity\":\"$identity\",\"entry\":\"${main.name}\",\"files\":[$encoded]}"
            fun kotlinLiteral(value: String): String = buildString {
                append('"')
                for (char in value) when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '$' -> append("\\\$")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> if (char.code < 32) append("\\u" + char.code.toString(16).padStart(4, '0')) else append(char)
                }
                append('"')
            }
            val expression = json.chunked(16_384).joinToString(" +\n    ", transform = ::kotlinLiteral)
            val destination = output.get().asFile.resolve("io/heapy/krogu/math/EmbeddedNodeWorker539.kt")
            destination.parentFile.mkdirs()
            destination.writeText("package io.heapy.krogu.math\ninternal fun embeddedNodeWorker539(): String = " + expression + "\n")
        }
    }
}

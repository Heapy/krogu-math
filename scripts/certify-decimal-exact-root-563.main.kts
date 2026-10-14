#!/usr/bin/env kotlin
// SPDX-License-Identifier: Apache-2.0
// Independent bounded mathematical/static evidence only; no numerical-library calls.
import java.io.File
import java.security.MessageDigest

check(args.isEmpty()) { "Run from the repository root without arguments" }
val root = File(".").canonicalFile
val destination = File(root, "diagnostics/decimal-exact-root-563")
destination.mkdirs()

fun sha(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().buffered().use { input ->
        val buffer = ByteArray(8192)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
}
fun gitOutput(vararg arguments: String): String {
    val process = ProcessBuilder(listOf("git") + arguments).directory(root).start()
    val output = process.inputStream.bufferedReader().readText()
    check(process.waitFor() == 0) { "git failed" }
    return output
}
fun git(vararg arguments: String): String = gitOutput(*arguments).trimEnd()
val baseline = "85941d18908a8155d205b04d6e1a9b3e8f10b31e"
check(git("rev-parse", "HEAD") == baseline)
check(git("branch", "--show-current") == "task/563-exact-root")
val tracked = git("ls-files").lines().filter { it.isNotEmpty() }.sorted()
val manifest = tracked.joinToString("\n", postfix = "\n") { "${sha(File(root, it))}  $it" }
val manifestFile = File(destination, "baseline-sha256.txt")
val allowed = setOf(
    "src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt",
    "src/commonTest/kotlin/io/heapy/krogu/math/ScaleBoundaryOracleTest.kt",
    "src/commonTest/kotlin/io/heapy/krogu/math/PortableOracleTest.kt",
    "src/jvmTest/kotlin/io/heapy/krogu/math/DecimalNormalizedRootTest.kt",
    "src/jvmTest/kotlin/io/heapy/krogu/math/DecimalPowerSubclassTest.kt",
    "src/jvmTest/kotlin/io/heapy/krogu/math/ScaleBoundaryConformanceTest.kt",
)
if (!manifestFile.exists()) {
    check(git("diff", "HEAD", "--").isEmpty()) { "Initial freeze requires an unchanged tracked baseline" }
    manifestFile.writeText(manifest)
} else {
    val frozen = manifestFile.readLines().associate { it.substring(66) to it.substring(0, 64) }
    check(frozen.keys == tracked.toSet()) { "Tracked paths changed since baseline freeze" }
    for (path in tracked) if (path !in allowed)
        check(sha(File(root, path)) == frozen.getValue(path)) { "Outside ownership: $path" }
    val changed = git("diff", "HEAD", "--name-only", "--").lines().filter { it.isNotEmpty() }.toSet()
    check(changed.all { it in allowed }) { "Tracked changes outside exact scope: $changed" }
    val production = "src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt"
    val original = gitOutput("show", "HEAD:$production")
    val edited = File(root, production).readText()
    val start = "    open fun sqrt(mc: MathContext?): BigDecimal {"
    val end = "    override fun compareTo(other: BigDecimal?): Int {"
    check(original.substringBefore(start) == edited.substringBefore(start)) { "Production prefix changed" }
    check(original.substringAfter(end) == edited.substringAfter(end)) { "Production suffix changed" }
}
val fixtures = tracked.filter { it.substringAfterLast('/').startsWith("Jdk25") }
check(fixtures.isNotEmpty())

// For each input: 3 layout checks + 81*(multiply + exact divide + 9*2 context checks)
// + 4*8*(sqrt + round + pow) = 1719 outcomes. sqrt occupies each third slot.
val perInput = 3 + 81 * (2 + 9 * 2) + 4 * 8 * 3
check(perInput == 1719 && 81 * perInput == 139239)
val modes = listOf("UP", "DOWN", "CEILING", "FLOOR", "HALF_UP", "HALF_DOWN", "HALF_EVEN", "UNNECESSARY")
val precisions = listOf(0, 1, 2, 7)
data class Witness(val coefficient: Long, val scale: Int, val inputIndex: Int,
    val preferredCoefficient: Long, val preferredScale: Int, val minimalScale: Int)
val witnesses = listOf(
    Witness(100, Int.MIN_VALUE, 7 * 9, 10, -1073741824, -1073741825),
    Witness(1000, Int.MIN_VALUE + 1, 8 * 9 + 1, 100, -1073741823, -1073741825),
)
data class Conflict(val index: Int, val witness: Witness, val precision: Int, val mode: String,
    val expectedCoefficient: Long, val expectedScale: Int)
val conflicts = mutableListOf<Conflict>()
for (w in witnesses) {
    // Squared coefficient/exponent identity; all powers are symbolic, never allocated.
    check(w.preferredCoefficient * w.preferredCoefficient == w.coefficient * if (w.scale % 2 != 0) 10 else 1)
    check(2L * w.preferredScale == w.scale.toLong() + if (w.scale % 2 != 0) 1 else 0)
    check(w.preferredScale == w.scale / 2)
    check(w.preferredCoefficient == if (w.preferredScale - w.minimalScale == 1) 10L else 100L)
    for ((pi, p) in precisions.withIndex()) for ((mi, mode) in modes.withIndex()) {
        if (p != 0 && mode != "UNNECESSARY") continue
        val zeros = if (p == 0) w.preferredScale - w.minimalScale
            else minOf(p - 1, w.preferredScale - w.minimalScale)
        val c = when (zeros) { 0 -> 1L; 1 -> 10L; 2 -> 100L; else -> error("Unbounded certificate") }
        val s = w.minimalScale + zeros
        check(s in Int.MIN_VALUE..Int.MAX_VALUE)
        val index = w.inputIndex * perInput + 3 + 81 * 20 + (pi * 8 + mi) * 3
        conflicts.add(Conflict(index, w, p, mode, c, s))
    }
}
val selected = conflicts.associateBy { it.index }
val fixture = File(root, "src/commonTest/kotlin/io/heapy/krogu/math/Jdk25ScaleBoundaryResults.kt")
var outcomes = 0
var observed = 0
val report = StringBuilder("index\tcoefficient\tscale\tprecision\tmode\tfrozen_diagnostic\tcontract_coefficient\tcontract_scale\n")
fixture.useLines { lines ->
    lines.forEach { line ->
        val trimmed = line.trim()
        if (trimmed.startsWith("private fun scaleExpectedChunk")) {
            val chunk = trimmed.substringAfter(" = \"").removeSuffix("\"")
            for (diagnostic in chunk.split("\\n")) {
                selected[outcomes]?.let { conflict ->
                    check(diagnostic == "!ArithmeticException") { "Unexpected frozen row $outcomes: $diagnostic" }
                    report.append("$outcomes\t${conflict.witness.coefficient}\t${conflict.witness.scale}\t${conflict.precision}\t${conflict.mode}\t$diagnostic\t${conflict.expectedCoefficient}\t${conflict.expectedScale}\n")
                    observed++
                }
                outcomes++
            }
        }
    }
}
check(outcomes == 207239) { "Expected 207239 retained outcomes, read $outcomes" }
check(observed == conflicts.size && observed == 22)
File(destination, "retained-conflicts.tsv").writeText(report.toString())
val owned = (allowed + setOf(
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalExactRoot563Expected.kt",
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalExactRoot563Test.kt",
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalExactRoot563Callbacks.kt",
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalExactRoot563CallbackObservations.kt",
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalExactRoot563CallbackContractTest.kt",
    "src/jvmTest/kotlin/io/heapy/krogu/math/DecimalExactRoot563DiagnosticTest.kt",
    "scripts/certify-decimal-exact-root-563.main.kts",
    "scripts/inventory-decimal-exact-root-callbacks-563.main.kts",
    "docs/decimal-exact-root-563-scope-evidence.md",
    "docs/decimal-exact-root-563-parity-proof.md",
    "diagnostics/decimal-exact-root-563/baseline-sha256.txt",
    "diagnostics/decimal-exact-root-563/retained-conflicts.tsv",
    "diagnostics/decimal-exact-root-563/callback-observations.tsv",
)).filter { File(root, it).exists() }.sorted()
val handoff = File(destination, "handoff-sha256.txt")
handoff.writeText(owned.joinToString("\n", postfix = "\n") { "${sha(File(root, it))}  $it" })
println("PASS bounded symbolic identities and static retained-row audit: $observed conflicts / $outcomes outcomes")
println("Frozen baseline: $baseline; ${tracked.size} tracked hashes; ${fixtures.size} retained Jdk25 fixtures")
println("baseline-sha256.txt SHA-256: ${sha(manifestFile)}")
println("retained-conflicts.tsv SHA-256: ${sha(File(destination, "retained-conflicts.tsv"))}")
println("handoff-sha256.txt SHA-256: ${sha(handoff)}; ${owned.size} owned/evidence artifacts")
println("No production/JDK sqrt, Gradle, javac, or platform tests executed.")

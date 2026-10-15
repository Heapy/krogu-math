// SPDX-License-Identifier: Apache-2.0
// V2 read-only source audit. SOURCE ONLY under1918: UNCOMPILED/UNRUN.
// A later grant is required even to execute this metadata script.
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

check(Runtime.getRuntime().maxMemory() <= 256L * 1024 * 1024) { "Use a heap cap of at most 256 MiB" }
println("heap cap=${Runtime.getRuntime().maxMemory()}; script cache=" +
    System.getProperty("kotlin.main.kts.compiled.scripts.cache.dir"))

val base = "03ebb60a482873acb3fe892f0c86510472f2e0b3"
require(args.size <= 2) { "Usage: [candidate-root] [immutable-v1-root]" }
val root = Path.of(args.firstOrNull() ?: ".").toAbsolutePath().normalize()
val historical = Path.of(args.getOrNull(1) ?: "/private/tmp/task528-v1-frozen-1920").toRealPath()
fun read(path: String): String = Files.readString(root.resolve(path))
fun hash(path: String): String = MessageDigest.getInstance("SHA-256")
    .digest(Files.readAllBytes(root.resolve(path))).joinToString("") { "%02x".format(it.toInt() and 255) }
fun historicalHash(path: String): String = MessageDigest.getInstance("SHA-256")
    .digest(Files.readAllBytes(historical.resolve(path))).joinToString("") { "%02x".format(it.toInt() and 255) }
fun original(path: String): String {
    val process = ProcessBuilder("git", "show", "$base:$path").directory(root.toFile()).start()
    val text = process.inputStream.bufferedReader().readText()
    check(process.waitFor() == 0) { process.errorStream.bufferedReader().readText() }
    return text
}
fun bodyRange(text: String, signature: String): IntRange {
    val start = text.indexOf(signature)
    check(start >= 0 && text.indexOf(signature, start + 1) < 0)
    val opening = text.indexOf('{', start)
    check(opening >= 0)
    var depth = 1
    var cursor = opening + 1
    while (depth > 0) {
        when (text[cursor++]) { '{' -> depth++; '}' -> depth-- }
    }
    return opening until cursor
}
fun body(text: String, signature: String) = text.substring(bodyRange(text, signature))
fun maskedDecimal(text: String): String {
    var result = text
    val precisionLine = Regex("    private fun storedPrecision\\(\\): Int = [^\\n]+")
    check(precisionLine.findAll(result).count() == 1)
    result = result.replace(precisionLine, "    private fun storedPrecision(): Int = OWNED_BODY")
    for (signature in listOf("private fun compareMagnitude(other: BigDecimal): Int",
        "private fun compareValues(a: BigInteger, aScale: Long, b: BigInteger, bScale: Long): Int"))
        result = result.replaceRange(bodyRange(result, signature), "{ OWNED_BODY }")
    return result
}
val decimalPath = "src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt"
val integerPath = "src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt"
val decimal = read(decimalPath)
val integer = read(integerPath)
check(maskedDecimal(decimal) == maskedDecimal(original(decimalPath)))
// The new helper region is the sole BigInteger change, including comments.
val insertionStart = integer.indexOf("    // Exact stored decimal digit count.")
val insertionEnd = integer.indexOf("    internal class DecimalBinaryPrefix(", insertionStart)
check(insertionStart >= 0 && insertionEnd > insertionStart)
check(integer.removeRange(insertionStart, insertionEnd) == original(integerPath))
val previousInteger = Files.readString(historical.resolve(integerPath))
val comparisonStart = integer.indexOf("    // Compare positive decimal significands,")
val previousComparisonStart = previousInteger.indexOf("    // Compare positive decimal significands,")
val previousComparisonEnd = previousInteger.indexOf("    internal class DecimalBinaryPrefix(", previousComparisonStart)
check(comparisonStart > insertionStart && previousComparisonStart >= 0 && previousComparisonEnd > previousComparisonStart)
check(integer.removeRange(comparisonStart, insertionEnd) ==
    previousInteger.removeRange(previousComparisonStart, previousComparisonEnd))
check(body(integer, "internal fun decimalMagnitudeDigits(): Int") ==
    body(previousInteger, "internal fun decimalMagnitudeDigits(): Int"))
for (signature in listOf("internal fun decimalMagnitudeDigits(): Int",
    "internal fun compareDecimalSignificand(other: BigInteger): Int",
    "private fun divideDecimalChunk(magnitude: IntArray, length: Int, divisor: Int): Int",
    "private fun compareDecimalScratch(magnitude: IntArray, length: Int, other: IntArray): Int")) {
    val source = body(integer, signature).replace(Regex("//[^\\n]*"), "")
    for (forbidden in listOf("toString(", "padEnd(", "StringBuilder", "toByteArray(", "abs("))
        check(forbidden !in source) { "$signature contains $forbidden" }
}
val comparison = body(integer, "internal fun compareDecimalSignificand(other: BigInteger): Int")
check(Regex("\\.copyOf\\(\\)").findAll(comparison).count() == 1)
check("val quotient = longer.words.copyOf()" in comparison)
check("TEN.pow(" !in comparison && "divideStored(" !in comparison && "IntArray(" !in comparison)
check("if (drop > 0)" in comparison && "repeat(drop) { divisor *= 10 }" in comparison)
check(Regex("val remainder = divideDecimalChunk").findAll(comparison).count() == 2)
check(Regex("sticky = sticky \\|\\| remainder != 0").findAll(comparison).count() == 2)
check(!Regex("sticky\\s*\\|\\|\\s*divideDecimalChunk").containsMatchIn(comparison))
check("compareDecimalScratch(quotient, length, shorter.words)" in comparison)
for (signature in listOf(
    "private fun divideDecimalChunk(magnitude: IntArray, length: Int, divisor: Int): Int",
    "private fun compareDecimalScratch(magnitude: IntArray, length: Int, other: IntArray): Int")) {
    val source = body(integer, signature)
    check("copyOf(" !in source && "IntArray(" !in source && "normalized(" !in source)
    check("for (i in length - 1 downTo 0)" in source)
}
for (signature in listOf("private fun compareMagnitude(other: BigDecimal): Int",
    "private fun compareValues(a: BigInteger, aScale: Long, b: BigInteger, bScale: Long): Int")) {
    val source = body(decimal, signature)
    check("toString(" !in source && "padEnd(" !in source)
}
val retained = mapOf(
    "DECIMAL_BINARY_CONVERSION.md" to "f77248b6219a9ca99566caa3ef95456e30e3c0bb3b3cce8b742f4c87fcb48a47",
    "scripts/certify-decimal-binary-range.main.kts" to "7c2c4b98e0f1081dcd8030b2138e06b89b4b355375ba95f2b816ffd81d4b36ff",
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalBinaryPrefixTest.kt" to
        "8632faf6eb22fc32af9de5a9c999694898e747afcf8c14ef8bda9764dcb8eaa3",
)
for ((path, expected) in retained) check(hash(path) == expected) { "accepted artifact changed: $path" }
val unchangedV1 = mapOf(
    decimalPath to "92de3354da9545f5b4867ee2ed32e72c91fac4ed32aa5c666f5762bd9f723370",
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalNumericMetadataTest.kt" to
        "c839d17adf6312e5faaa4a6336f75ee663f17eee901f6f545b5aa744ea055ddf",
    "src/jvmTest/kotlin/io/heapy/krogu/math/DecimalNumericMetadataJdkTest.kt" to
        "28815bdaa115bfffac83e9810b17c80a15751cfb804ce0b3810be899c726b849",
    "scripts/certify-decimal-metadata.main.kts" to
        "bf457b19276a9deb49d2804cf026eccb1f04e1f5575fd84d9985784161a50acc",
    "DECIMAL_METADATA_HANDOFF.md" to "64b2a8eaf263192a3bce86abb31e8d6096ff25fc9210020f9847b8c001cc772f",
    "scripts/measure-decimal-metadata.main.kts" to
        "0e4f59ecc33350eb097d4bfe262a54a53853c2e00078f04275035bca7b2c09c5",
    "DECIMAL_METADATA_MEASUREMENT_PLAN.md" to
        "edb96d8216d31e80cb8aa3f633dd147d4d59c499a0f55effdd87651bd3a05ae0",
)
for ((path, expected) in unchangedV1) check(hash(path) == expected) { "parent-frozen source changed: $path" }
val archivedV1 = unchangedV1 + mapOf(
    integerPath to "bdbd1a0f6d0ec9571c641b7a34ff73ac969da051c648fa6b606ff775c8739f57",
    "DECIMAL_METADATA_COMPARISON.md" to "15b04b91dfccd5a646267fbee81674d674eb5e75ddb9e23cda4c83c02fda8e8e",
    "scripts/audit-decimal-metadata.main.kts" to
        "04f67b200bb0d0c689b04e48e5b76c719e1e29d9f145f517dd5cb7f293193815",
)
check(archivedV1.size == 10)
check(historicalHash("FROZEN_SOURCE.sha256") == "4d7d7bbd9fab26b4bda0fd5c288461f2130734711aa641e2dff727694b371371")
for ((path, expected) in archivedV1) check(historicalHash(path) == expected) { "historical V1 changed: $path" }
val v2 = mapOf(
    integerPath to "24cfd50f0dd9edd25590877b23b4c2e72c89ce9fab71f4b75bc0fe11def56a5f",
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalChunkComparisonTest.kt" to
        "b0aadb817b8f90e89d88032451cc26db1030eb722ae98370e2fb1a77c842bfa4",
    "DECIMAL_METADATA_COMPARISON.md" to "67b22ddb9af0d32483f7624b4be12770ca7b7f6456e388361d490090acb792b0",
)
for ((path, expected) in v2) check(hash(path) == expected) { "reviewed V2 source changed: $path" }
check(Regex("@Test\\b").findAll(read("src/commonTest/kotlin/io/heapy/krogu/math/DecimalChunkComparisonTest.kt")).count() == 3)
println("PASS: only owned significand/helper region differs from archived V1; exact digit helper frozen")
println("PASS: compareTo/adjustedExponent, fields, arithmetic kernels, binary conversion and accepted artifacts unchanged")
println("PASS: owned digit/comparison helpers contain no coefficient formatting or decimal padding")
println("PASS: one local quotient clone; eager division before sticky; positive-only tail; active-length comparison")
println("PASS: ten historical V1 files and seven retained live files hash-identical; three new common methods prepared")
for (path in listOf(decimalPath, integerPath,
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalNumericMetadataTest.kt",
    "src/jvmTest/kotlin/io/heapy/krogu/math/DecimalNumericMetadataJdkTest.kt",
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalChunkComparisonTest.kt",
    "scripts/certify-decimal-metadata.main.kts", "scripts/audit-decimal-metadata.main.kts",
    "DECIMAL_METADATA_COMPARISON.md")) println("${hash(path)}  $path")

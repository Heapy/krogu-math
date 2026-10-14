#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Input-only metadata inventory, not a numerical conformance verdict. Usage: [--check]. */
import java.io.File
import java.security.MessageDigest

require(args.all { it == "--check" })
val hashes = linkedMapOf(
    "src/commonTest/kotlin/io/heapy/krogu/math/Jdk25DecimalContextDispatchResults.kt" to "88fc3c5bc8277a048a83a9ad9b6546bd8a23ae1ad9a10126409d857367391026",
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalContextDispatchOracleTest.kt" to "2ed00a5dd3c7ec038e61f5be078697b84609606549c26b1bda1249fce860c781",
    "scripts/generate-decimal-context-dispatch-oracle.main.kts" to "0d9e9b11af4c27892758e78d6b80639b763fcec66ffbe27721d373864a501188",
)
val fixture = File(hashes.keys.first()).readBytes()
for ((path, expected) in hashes) {
    val bytes = if (path == hashes.keys.first()) fixture else File(path).readBytes()
    val actual = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    check(actual == expected) { "Reviewed evidence changed: $path ($actual)" }
}
val rows = fixture.toString(Charsets.UTF_8).lineSequence().filter { '\t' in it }.map { it.split('\t') }.toList()
require(rows.all { it.size == 7 }) { "Unexpected row shape" }

data class BaseMetadata(val sign: Int, val precision: Int)
val decimalText = Regex("([+-]?)([0-9]+)(?:\\.([0-9]*))?(?:[eE][+-]?[0-9]+)?")
fun base(text: String): BaseMetadata {
    val match = requireNotNull(decimalText.matchEntire(text)) { "Unsupported decimal text: $text" }
    // Scale/exponent changes the value's magnitude, not coefficient digit count or sign.
    val coefficient = (match.groupValues[2] + match.groupValues[3]).trimStart('0')
    return BaseMetadata(if (coefficient.isEmpty()) 0 else if (match.groupValues[1] == "-") -1 else 1,
        maxOf(1, coefficient.length))
}
check(base("-0.000E+3") == BaseMetadata(0, 1))
check(base("-0012.00e+9") == BaseMetadata(-1, 4))
check(base("1E-20") == BaseMetadata(1, 1))

data class Assessment(val invalidSign: Boolean = false, val invalidPrecision: Boolean = false,
    val throwingSign: Boolean = false, val throwingPrecision: Boolean = false) {
    val invalid get() = invalidSign || invalidPrecision
    val throwing get() = throwingSign || throwingPrecision
}
fun assess(text: String, mode: String): Assessment {
    val metadata = base(text)
    return when {
        mode == "delegate" -> Assessment()
        mode == "sign-first" || mode == "sign-second" -> Assessment(throwingSign = true)
        mode == "precision" -> Assessment(throwingPrecision = true)
        // -0 and 0 are the same Int sign: this mode is consistent for zero inputs.
        mode == "sign-changing" -> Assessment(invalidSign = metadata.sign != 0)
        mode == "precision-changing" -> Assessment(invalidPrecision = true)
        mode.startsWith("sign:") -> Assessment(invalidSign = mode.substringAfter(':').toInt() != metadata.sign)
        mode.startsWith("precision:") -> Assessment(invalidPrecision = mode.substringAfter(':').toInt() != metadata.precision)
        mode.startsWith("zero-precision:") -> Assessment(invalidSign = metadata.sign != 1,
            invalidPrecision = mode.substringAfter(':').toInt() != metadata.precision)
        else -> error("Unreviewed mode: $mode")
    }
}
val counts = sortedMapOf<String, Int>()
fun count(key: String, yes: Boolean = true) { if (yes) counts[key] = (counts[key] ?: 0) + 1 }
for (row in rows) {
    val a = assess(row[0], row[2])
    val b = when (row[3]) { "self" -> a; "null" -> null; else -> assess(row[1], row[3]) }
    val invalid = a.invalid || b?.invalid == true
    val throwing = a.throwing || b?.throwing == true
    count("rows")
    count("invalidReturnedMetadata", invalid)
    count("throwingMetadataOnly", !invalid && throwing)
    count("consistentReturnedMetadata", !invalid && !throwing)
    count("invalidSourceSign", a.invalidSign)
    count("invalidSourcePrecision", a.invalidPrecision)
    count("invalidDivisorSign", b?.invalidSign == true)
    count("invalidDivisorPrecision", b?.invalidPrecision == true)
    count("selfAlias", row[3] == "self")
    count("nullDivisor", row[3] == "null")
    count(when { row[4].toInt() < 0 -> "nullContext"; row[4].toInt() == 0 -> "unlimitedContext"; else -> "finiteContext" })
    count("referenceException", row[6].startsWith('!'))
    count("referenceValue", !row[6].startsWith('!'))
}
check(counts.getValue("rows") == counts.getValue("invalidReturnedMetadata") +
    counts.getValue("throwingMetadataOnly") + counts.getValue("consistentReturnedMetadata"))
println("fixtureSha256=${hashes.values.first()}")
counts.forEach { (key, value) -> println("$key=$value") }
println("All probe objects also override scale/unscaledValue/abs/negate with throwing callbacks and exact divide with 7.00.")
println("Consistent returned metadata does not imply a contract-preserving subclass or a required numerical outcome.")
println("No fixture, test assertion or numerical implementation was changed or evaluated.")
if ("--check" in args) {
    val expected = mapOf(
        "rows" to 75728, "invalidReturnedMetadata" to 57576,
        "throwingMetadataOnly" to 7248, "consistentReturnedMetadata" to 10904,
        "invalidSourceSign" to 10680, "invalidSourcePrecision" to 27696,
        "invalidDivisorSign" to 10680, "invalidDivisorPrecision" to 27912,
        "selfAlias" to 768, "nullDivisor" to 288, "nullContext" to 192,
        "unlimitedContext" to 2048, "finiteContext" to 73488,
        "referenceException" to 17635, "referenceValue" to 58093,
    )
    check(counts == expected) { "Metadata inventory differs: $counts" }
    println("Verified the frozen input schema and metadata inventory; full field-level classification remains open.")
}

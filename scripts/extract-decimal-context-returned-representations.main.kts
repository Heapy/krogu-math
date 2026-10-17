#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Extract public representation controls, without executing numerical implementations. Usage: [--check]. */
import java.io.File
import java.security.MessageDigest

require(args.all { it == "--check" }) { "Usage: [--check]" }
val frozenPath = "test/io/heapy/krogu/math/Jdk25DecimalContextDispatchResults.kt"
val controlPath = "test/io/heapy/krogu/math/Jdk25DecimalContextContractResults.kt"
val frozenHash = "88fc3c5bc8277a048a83a9ad9b6546bd8a23ae1ad9a10126409d857367391026"
val controlHash = "1245d75ed6837139c939496e602a085cba37b35dd4d3c98f197bdaece2ce7cc9"
fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes)
    .joinToString("") { "%02x".format(it) }
fun boundText(path: String, expected: String): String {
    val bytes = File(path).readBytes()
    check(sha256(bytes) == expected) { "Bound evidence changed: $path" }
    return bytes.toString(Charsets.UTF_8)
}

data class Representation(val coefficient: String, val scale: Int)
// Parse decimal syntax directly. No BigDecimal constructor or toString oracle supplies expectations.
val decimalPattern = Regex("(-?)([0-9]+)(?:\\.([0-9]+))?(?:E([+-]?[0-9]+))?")
fun parseDecimal(text: String): Representation {
    val match = requireNotNull(decimalPattern.matchEntire(text)) { "Unparsed successful decimal: $text" }
    val digits = (match.groupValues[2] + match.groupValues[3]).trimStart('0').ifEmpty { "0" }
    val coefficient = (if (digits != "0") match.groupValues[1] else "") + digits
    val exponent = match.groupValues[4].ifEmpty { "0" }.toLong()
    val scale = match.groupValues[3].length.toLong() - exponent
    check(scale in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) { "Scale outside Int: $text" }
    return Representation(coefficient, scale.toInt())
}
fun canonical(value: Representation): String {
    val digits = value.coefficient.removePrefix("-")
    val sign = if (value.coefficient.startsWith('-')) "-" else ""
    val adjustedExponent = digits.length.toLong() - 1L - value.scale.toLong()
    val body = if (value.scale >= 0 && adjustedExponent >= -6L) {
        val position = digits.length.toLong() - value.scale.toLong()
        when {
            value.scale == 0 -> digits
            position <= 0L -> "0." + "0".repeat((-position).toInt()) + digits
            else -> digits.take(position.toInt()) + "." + digits.drop(position.toInt())
        }
    } else {
        val significand = if (digits.length == 1) digits else digits.take(1) + "." + digits.drop(1)
        significand + "E" + (if (adjustedExponent >= 0L) "+" else "") + adjustedExponent
    }
    return sign + body
}
// Independent literals challenge the parser/formatter before deriving any fixture expectation.
val literals = listOf(
    Triple("1", 6, "0.000001"), Triple("1", 7, "1E-7"),
    Triple("123", 8, "0.00000123"), Triple("123", 9, "1.23E-7"),
    Triple("12300", 4, "1.2300"), Triple("12300", 0, "12300"),
    Triple("12300", -1, "1.2300E+5"), Triple("-12300", 4, "-1.2300"),
    Triple("-123", 9, "-1.23E-7"), Triple("0", 0, "0"), Triple("0", 2, "0.00"),
    Triple("0", 6, "0.000000"), Triple("0", 7, "0E-7"), Triple("0", -2, "0E+2"),
    Triple("1", Int.MIN_VALUE, "1E+2147483648"),
    Triple("-12", Int.MIN_VALUE, "-1.2E+2147483649"),
    Triple("123", Int.MAX_VALUE, "1.23E-2147483645"),
    Triple("0", Int.MIN_VALUE, "0E+2147483648"),
    Triple("0", Int.MAX_VALUE, "0E-2147483647"),
)
for ((coefficient, scale, text) in literals) {
    val value = Representation(coefficient, scale)
    check(canonical(value) == text) { "Independent literal formatter disagreement: $value" }
    check(parseDecimal(text) == value) { "Independent literal parser disagreement: $text" }
}

val ordinaryPairs = linkedSetOf<Representation>()
var controlRows = 0
for (line in boundText(controlPath, controlHash).lineSequence().filter { '\t' in it }) {
    val fields = line.split('\t')
    check(fields.size == 6 && fields.none(String::isEmpty))
    controlRows++
    if (!fields[5].startsWith('!')) {
        val parts = fields[5].split('@')
        check(parts.size == 2 && Regex("-?(?:0|[1-9][0-9]*)").matches(parts[0]) && parts[0] != "-0")
        ordinaryPairs += Representation(parts[0], parts[1].toInt())
    } else check(fields[5] in setOf("!ArithmeticException", "!NullPointerException"))
}
check(controlRows == 3000 && ordinaryPairs.size == 334)

data class Observation(val firstOldId: String, val observedText: String, var occurrences: Int = 0)
val observations = linkedMapOf<Representation, Observation>()
var oldRows = 0
var successes = 0
var errors = 0
for (line in boundText(frozenPath, frozenHash).lineSequence().filter { '\t' in it }) {
    val fields = line.split('\t')
    check(fields.size == 7 && fields.none(String::isEmpty)) { "Unexpected frozen row schema" }
    oldRows++
    val oldId = "D${oldRows.toString().padStart(5, '0')}"
    val outcome = fields[6]
    check(outcome.count { it == '|' } == 1) { "Unexpected outcome separator at $oldId" }
    val prefix = outcome.substringBefore('|')
    if (prefix.startsWith('!')) {
        check(Regex("![A-Za-z][A-Za-z0-9]*(?::.*)?").matches(prefix)) { "Unparsed error at $oldId" }
        errors++
        continue
    }
    val separator = prefix.lastIndexOf(':')
    check(separator > 0) { "Unparsed success at $oldId: $prefix" }
    val observedText = prefix.substring(0, separator)
    val recordedScale = prefix.substring(separator + 1).toInt()
    val value = parseDecimal(observedText)
    check(value.scale == recordedScale) { "Recorded/parsed scale disagreement at $oldId: $prefix" }
    val expected = canonical(value)
    check(expected == observedText) {
        "Public canonical spelling disagrees with frozen observation at $oldId: expected=$expected observed=$observedText"
    }
    val observation = observations.getOrPut(value) { Observation(oldId, observedText) }
    check(observation.observedText == observedText) { "Multiple observed spellings for one representation at $oldId" }
    observation.occurrences++
    successes++
}
check(oldRows == 75728 && successes == 58093 && errors == 17635)
check(observations.size == 675 && observations.values.sumOf { it.occurrences } == successes)
val additional = observations.keys.count { it !in ordinaryPairs }
check(additional == 341 && ordinaryPairs.all { it in observations })

val generated = buildString {
    appendLine("// SPDX-License-Identifier: Apache-2.0")
    appendLine("// Generated by scripts/extract-decimal-context-returned-representations.main.kts.")
    appendLine("// Frozen input SHA-256: $frozenHash")
    appendLine("// Ordinary/truthful control SHA-256: $controlHash")
    appendLine("// 58093 successful frozen rows -> 675 representations; 341 additional to 334 ordinary-control pairs.")
    appendLine("// Expected spelling is independently derived from coefficient/scale; observed spelling is separate evidence.")
    appendLine("package io.heapy.krogu.math")
    appendLine()
    appendLine("internal data class DecimalContextReturnedRepresentation(")
    appendLine("    val coefficient: String,")
    appendLine("    val scale: Int,")
    appendLine("    val expectedCanonical: String,")
    appendLine("    val observedCanonical: String,")
    appendLine("    val firstOldId: String,")
    appendLine("    val occurrences: Int,")
    appendLine("    val coveredByOrdinaryControls: Boolean,")
    appendLine(")")
    appendLine()
    appendLine("internal fun decimalContextReturnedRepresentations(): List<DecimalContextReturnedRepresentation> = listOf(")
    for (chunk in observations.entries.chunked(75)) {
        appendLine("    \"\"\"")
        for ((value, observation) in chunk) {
            appendLine(listOf(value.coefficient, value.scale, canonical(value), observation.observedText,
                observation.firstOldId, observation.occurrences, value in ordinaryPairs).joinToString("\t"))
        }
        appendLine("    \"\"\".trimIndent(),")
    }
    appendLine(").flatMap { chunk ->")
    appendLine("    chunk.lines().map { line ->")
    appendLine("        val fields = line.split('\\t')")
    appendLine("        DecimalContextReturnedRepresentation(fields[0], fields[1].toInt(), fields[2], fields[3],")
    appendLine("            fields[4], fields[5].toInt(), fields[6].toBooleanStrict())")
    appendLine("    }")
    appendLine("}")
}
val target = File("test/io/heapy/krogu/math/DecimalContextReturnedRepresentationData.kt")
if ("--check" in args) check(target.isFile && target.readText() == generated) { "Returned-representation data differs" }
else target.writeText(generated)
println("${if ("--check" in args) "Verified" else "Generated"} 675 representations from all 58093 successes; 341 additional pairs")
println("75728 rows parsed: 58093 successes, 17635 errors; zero unparsed successes or canonical/scale disagreements")
println("Both bound SHA-256 hashes, 3000 control rows/334 pairs, first-occurrence IDs and occurrence sum verified")
println("19 independent literal parser/formatter witnesses passed; no JDK/port numerical implementation executed")
println("Data SHA-256: ${sha256(generated.toByteArray(Charsets.UTF_8))}")

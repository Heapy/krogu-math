#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Frozen field/control correspondence, not a numerical verdict or archival approval. Usage: [--check]. */
import java.io.File
import java.security.MessageDigest

require(args.all { it == "--check" }) { "Usage: [--check]" }
val evidence = linkedMapOf(
    "src/commonTest/kotlin/io/heapy/krogu/math/Jdk25DecimalContextDispatchResults.kt" to "88fc3c5bc8277a048a83a9ad9b6546bd8a23ae1ad9a10126409d857367391026",
    "src/commonTest/kotlin/io/heapy/krogu/math/DecimalContextDispatchOracleTest.kt" to "2ed00a5dd3c7ec038e61f5be078697b84609606549c26b1bda1249fce860c781",
    "scripts/generate-decimal-context-dispatch-oracle.main.kts" to "0d9e9b11af4c27892758e78d6b80639b763fcec66ffbe27721d373864a501188",
    "src/commonTest/kotlin/io/heapy/krogu/math/Jdk25DecimalContextContractResults.kt" to "1245d75ed6837139c939496e602a085cba37b35dd4d3c98f197bdaece2ce7cc9",
)
val files = evidence.mapValues { (path, expected) ->
    val bytes = File(path).readBytes()
    val actual = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    check(actual == expected) { "Reviewed evidence changed: $path ($actual)" }
    bytes.toString(Charsets.UTF_8)
}
fun rows(path: String, columns: Int): List<List<String>> = files.getValue(path).lineSequence()
    .filter { '\t' in it }.map { it.split('\t') }.toList().also { parsed ->
        check(parsed.all { it.size == columns && it.none(String::isEmpty) }) { "Unexpected fixture schema: $path" }
    }
val frozen = rows(evidence.keys.first(), 7)
val controls = rows(evidence.keys.last(), 6)
check(frozen.size == 75728 && controls.size == 3000)
val roundings = setOf("UP", "DOWN", "CEILING", "FLOOR", "HALF_UP", "HALF_DOWN", "HALF_EVEN", "UNNECESSARY")
val precisions = setOf("-1", "0", "1", "2", "7", "17", "18", "19")
fun key(row: List<String>): List<String> = listOf(row[0], row[1],
    when (row[3]) { "self", "null" -> row[3]; else -> "ordinary" }, row[4], row[5])
val keys = frozen.map(::key).toCollection(linkedSetOf()).toList()
check(keys == controls.map { it.take(5) }) { "Controls must cover the complete first-occurrence projection in order" }
check(keys.size == keys.toSet().size)
val ids = keys.withIndex().associate { (index, input) -> input to "C${(index + 1).toString().padStart(4, '0')}" }
val expectedByKey = controls.associate { it.take(5) to it[5] }
val valuePattern = Regex("-?(?:0|[1-9][0-9]*)@-?(?:0|[1-9][0-9]*)")
for (control in controls) {
    check(control[2] in setOf("ordinary", "self", "null"))
    check(control[3] in precisions && control[4] in roundings)
    check(control[5] in setOf("!ArithmeticException", "!NullPointerException") || valuePattern.matches(control[5]))
    if (!control[5].startsWith('!')) check(!control[5].startsWith("-0@"))
}
check(controls.groupingBy { it[2] }.eachCount() == mapOf("ordinary" to 2736, "self" to 192, "null" to 72))
check(controls.groupingBy { it[3] }.eachCount() == mapOf("-1" to 48, "0" to 176, "1" to 536,
    "2" to 544, "7" to 520, "17" to 392, "18" to 392, "19" to 392))
check(controls.groupingBy { if (it[5].startsWith('!')) it[5] else "value" }.eachCount() ==
    mapOf("value" to 2475, "!ArithmeticException" to 429, "!NullPointerException" to 96))
check(controls.map { it[4] }.toSet() == roundings)
val maskChecks = controls.sumOf { if (it[2] == "ordinary") 4 else 2 }
check(maskChecks == 11472)

// Configured metadata is assessed independently of whether a reference trace invoked a getter.
// Decimal exponent and scale do not change coefficient digit count or sign.
data class Metadata(val sign: Int, val precision: Int)
data class Assessment(val invalid: Boolean = false, val throwing: Boolean = false)
val decimalText = Regex("([+-]?)([0-9]+)(?:\\.([0-9]*))?(?:[eE][+-]?[0-9]+)?")
fun metadata(text: String): Metadata {
    val match = requireNotNull(decimalText.matchEntire(text)) { "Unsupported decimal text: $text" }
    val digits = (match.groupValues[2] + match.groupValues[3]).trimStart('0')
    return Metadata(if (digits.isEmpty()) 0 else if (match.groupValues[1] == "-") -1 else 1, maxOf(1, digits.length))
}
fun assess(text: String, mode: String): Assessment {
    val actual = metadata(text)
    return when {
        mode == "delegate" -> Assessment()
        mode in setOf("sign-first", "sign-second", "precision") -> Assessment(throwing = true)
        mode == "sign-changing" -> Assessment(invalid = actual.sign != 0)
        mode == "precision-changing" -> Assessment(invalid = true)
        mode.startsWith("sign:") -> Assessment(invalid = mode.substringAfter(':').toInt() != actual.sign)
        mode.startsWith("precision:") -> Assessment(invalid = mode.substringAfter(':').toInt() != actual.precision)
        mode.startsWith("zero-precision:") -> Assessment(invalid = actual.sign != 1 || mode.substringAfter(':').toInt() != actual.precision)
        else -> error("Unreviewed probe mode: $mode")
    }
}
check(metadata("-0.000E+3") == Metadata(0, 1))
check(metadata("-0012.00e+9") == Metadata(-1, 4))
check(assess("0.00", "sign-changing") == Assessment())

val columns = listOf("old_row_id", "control_id", "source", "divisor", "topology", "context_precision", "rounding",
    "source_mode", "divisor_mode", "required_valid_outcome", "required_canonical_text_control", "old_outcome_kind",
    "old_value_or_error_status", "old_trace_status", "old_message_status", "runtime_identity_status",
    "unresolved_interactions", "explicit_caller_language_control", "archival_status")
val counts = sortedMapOf<String, Int>()
fun count(name: String) { counts[name] = (counts[name] ?: 0) + 1 }
val uses = ids.values.associateWith { 0 }.toMutableMap()
// Exactly eight reviewed inputs; the reference outcome is checked, never used to select a row.
data class NullArgumentBinding(val controlId: String, val rounding: String)
val requiredNullRows = linkedMapOf(
    "D07425" to NullArgumentBinding("C0609", "UP"),
    "D07426" to NullArgumentBinding("C0610", "DOWN"),
    "D07427" to NullArgumentBinding("C0611", "CEILING"),
    "D07428" to NullArgumentBinding("C0612", "FLOOR"),
    "D07429" to NullArgumentBinding("C0613", "HALF_UP"),
    "D07430" to NullArgumentBinding("C0614", "HALF_DOWN"),
    "D07431" to NullArgumentBinding("C0615", "HALF_EVEN"),
    "D07432" to NullArgumentBinding("C0616", "UNNECESSARY"),
)
val requiredNullStatus = "REQUIRED_NULL_ARGUMENT_CONTRACT"
val unresolvedStatus = "UNRESOLVED_POISONED_INTERACTION"
check(requiredNullRows.size == 8 && requiredNullRows.values.map { it.rounding }.toSet() == roundings)
check(requiredNullRows.values.map { it.controlId }.toSet().size == 8)
fun outcomeStatus(oldId: String, controlId: String, input: List<String>, sourceMode: String, divisorMode: String, kind: String): String {
    val binding = requiredNullRows[oldId] ?: return unresolvedStatus
    check(controlId == binding.controlId && input == listOf("0.00", "2.0", "ordinary", "-1", binding.rounding)) {
        "Reviewed null-argument input/control changed: $oldId"
    }
    check(sourceMode == "delegate" && divisorMode == "delegate" && kind == "ERROR")
    check(expectedByKey.getValue(input) == "!NullPointerException")
    return requiredNullStatus
}
val mapped = frozen.mapIndexed { index, row ->
    val input = key(row)
    val id = ids.getValue(input)
    val expected = expectedByKey.getValue(input)
    uses[id] = uses.getValue(id) + 1
    val separator = row[6].indexOf('|')
    check(separator >= 0 && separator == row[6].lastIndexOf('|')) { "Unexpected frozen outcome shape" }
    val prefix = row[6].substring(0, separator)
    val trace = row[6].substring(separator + 1)
    val exception = prefix.startsWith('!')
    val a = assess(row[0], row[2])
    val b = when (row[3]) { "self" -> a; "null" -> null; else -> assess(row[1], row[3]) }
    val invalid = a.invalid || b?.invalid == true
    val throwing = a.throwing || b?.throwing == true
    count(when { invalid -> "inconsistent_metadata"; throwing -> "throwing_metadata_only"; else -> "consistent_metadata" })
    count(if (exception) "old_error" else "old_value")
    count("mapped_rows")
    val unresolved = buildList {
        add("POISONED_GETTERS_AND_EXACT_OVERRIDE")
        if (invalid) add("INCONSISTENT_METADATA")
        if (throwing) add("THROWING_METADATA")
        if (input[3] == "-1") add("NULL_CONTEXT")
        if (input[2] == "null") add("NULL_DIVISOR")
        if (input[2] == "self") add("SELF_ALIAS_COUNTERS")
        if (".exact" in trace) add("INVOKED_EXACT_REPLACEMENT")
        if (exception && prefix.startsWith("!IllegalStateException:")) add("INJECTED_CALLBACK_FAILURE")
        if (exception && prefix.startsWith("!ArithmeticException:")) add("ARITHMETIC_CALLBACK_INTERACTION")
    }.joinToString(";")
    val message = if (exception && ':' in prefix) "REFERENCE_OBSERVATION" else "NOT_RECORDED"
    val oldId = "D${(index + 1).toString().padStart(5, '0')}"
    val kind = if (exception) "ERROR" else "VALUE"
    val status = outcomeStatus(oldId, id, input, row[2], row[3], kind)
    if (oldId in requiredNullRows) check(row[6] == "!NullPointerException|") { "Reviewed frozen observation changed: $oldId" }
    listOf(oldId, id) + input + listOf(row[2], row[3], expected,
        if (expected.startsWith('!')) "NOT_APPLICABLE" else "CANONICAL_TEXT:$id",
        kind, status, "REFERENCE_OBSERVATION", message,
        "NOT_OBSERVED", unresolved, "EXPLICIT_DIVIDE_CALLER_MATRIX", "BLOCKED_UNRESOLVED_FIELDS")
}
check(mapped.all { it.size == columns.size && it.none { value -> '\t' in value || '\n' in value || value.isEmpty() } })
check(mapped.filter { it[12] == requiredNullStatus }.map { it[0] }.toSet() == requiredNullRows.keys)
check(mapped.all { it[12] == if (it[0] in requiredNullRows) requiredNullStatus else unresolvedStatus })
check(uses.size == 3000 && uses.values.all { it > 0 } && uses.values.sum() == 75728)
check(counts == mapOf("mapped_rows" to 75728, "inconsistent_metadata" to 57576,
    "throwing_metadata_only" to 7248, "consistent_metadata" to 10904, "old_error" to 17635, "old_value" to 58093))
// The complete logical record above stays independently reproducible; encode only row-varying facts.
val modes = frozen.flatMap { listOf(it[2], it[3]) }.distinct()
check(modes.size < 100)
val modeIds = modes.withIndex().associate { (index, mode) -> mode to "M${(index + 1).toString().padStart(2, '0')}" }
val modesById = modeIds.entries.associate { (mode, id) -> id to mode }
check(modeIds.getValue("delegate") == "M01")
val flags = linkedMapOf(
    1 to "POISONED_GETTERS_AND_EXACT_OVERRIDE", 2 to "INCONSISTENT_METADATA", 4 to "THROWING_METADATA",
    8 to "NULL_CONTEXT", 16 to "NULL_DIVISOR", 32 to "SELF_ALIAS_COUNTERS", 64 to "INVOKED_EXACT_REPLACEMENT",
    128 to "INJECTED_CALLBACK_FAILURE", 256 to "ARITHMETIC_CALLBACK_INTERACTION", 512 to "MESSAGE_RECORDED",
)
val compactColumns = listOf("old_row_id", "control_id", "source_mode_id", "divisor_mode_id", "old_kind", "interaction_bits_hex")
val compact = mapped.map { logical ->
    val tags = logical[16].split(';').toSet()
    var bits = flags.filterValues { it in tags }.keys.fold(0) { combined, bit -> combined or bit }
    if (logical[14] == "REFERENCE_OBSERVATION") bits = bits or 512
    listOf(logical[0], logical[1], modeIds.getValue(logical[7]), modeIds.getValue(logical[8]),
        if (logical[11] == "ERROR") "E" else "V", bits.toString(16).padStart(3, '0'))
}
fun decode(compactRow: List<String>): List<String> {
    check(compactRow.size == compactColumns.size)
    check(Regex("D[0-9]{5}").matches(compactRow[0]) && Regex("C[0-9]{4}").matches(compactRow[1]))
    check(compactRow[4] in setOf("V", "E") && Regex("[0-9a-f]{3}").matches(compactRow[5]))
    val controlIndex = compactRow[1].drop(1).toInt() - 1
    check(controlIndex in controls.indices)
    val control = controls[controlIndex]
    val expected = control[5]
    val bits = compactRow[5].toInt(16)
    check(bits and 1 == 1 && bits and 1023.inv() == 0) { "Unknown or missing interaction bits" }
    val tags = flags.filter { (bit, _) -> bit != 512 && bits and bit != 0 }.values.joinToString(";")
    val sourceMode = modesById.getValue(compactRow[2])
    val divisorMode = modesById.getValue(compactRow[3])
    val kind = if (compactRow[4] == "E") "ERROR" else "VALUE"
    val status = outcomeStatus(compactRow[0], compactRow[1], control.take(5), sourceMode, divisorMode, kind)
    if (compactRow[0] in requiredNullRows)
        check(compactRow.drop(2) == listOf("M01", "M01", "E", "009")) { "Reviewed null-argument modes/kind/mask changed" }
    return compactRow.take(2) + control.take(5) + listOf(sourceMode, divisorMode,
        expected, if (expected.startsWith('!')) "NOT_APPLICABLE" else "CANONICAL_TEXT:${compactRow[1]}",
        kind, status, "REFERENCE_OBSERVATION",
        if (bits and 512 != 0) "REFERENCE_OBSERVATION" else "NOT_RECORDED", "NOT_OBSERVED", tags,
        "EXPLICIT_DIVIDE_CALLER_MATRIX", "BLOCKED_UNRESOLVED_FIELDS")
}
fun validateCompact(compactRows: List<List<String>>) {
    check(compactRows.size == frozen.size)
    for ((index, compactRow) in compactRows.withIndex()) {
        val logical = decode(compactRow)
        check(logical == mapped[index]) { "Lossless logical-field recovery differs at frozen row ${index + 1}" }
        check(logical[0] == "D${(index + 1).toString().padStart(5, '0')}")
        check(logical.slice(2..6) == key(frozen[index]))
        check(logical[7] == frozen[index][2] && logical[8] == frozen[index][3])
    }
    check(compactRows.map { it[1] }.toSet() == ids.values.toSet())
}
validateCompact(compact)
val statusHeaderRecords = requiredNullRows.map { (oldId, binding) ->
    listOf(oldId, binding.controlId, "M01", "M01", "E", "009", requiredNullStatus)
}
fun validateStatusHeaders(text: String) {
    val records = text.lineSequence().filter { it.startsWith("# STATUS ") }
        .map { it.removePrefix("# STATUS ").split(' ') }.toList()
    check(records == statusHeaderRecords) { "STATUS dictionary differs from the exact eight reviewed bindings" }
}
val generated = buildString {
    appendLine("# SPDX-License-Identifier: Apache-2.0")
    appendLine("# Schema: context-contract-coverage-v3; six compact columns plus bounded STATUS dictionary, lossless recovery of 19 logical fields")
    appendLine("# Predecessor mapper SHA-256 9557d63ad7856466623c87a6cd6750ea1e5dec2aaf1d08cae000abb145eb4e58 (reviewed historical source, not the current mapper digest)")
    for ((path, hash) in evidence) appendLine("# SHA-256 $hash $path")
    appendLine("# Stable IDs: D = 1-based frozen row; C = 1-based first-occurrence five-field projection")
    appendLine("# Control lookup: C row of the bound contract fixture supplies source/divisor/topology/context_precision/rounding/required_valid_outcome")
    appendLine("# Kind: V = VALUE; E = ERROR; flag mask is three-digit lowercase hexadecimal")
    for ((mode, id) in modeIds) appendLine("# MODE $id $mode")
    for ((bit, name) in flags) appendLine("# FLAG ${bit.toString(16).padStart(3, '0')} $name")
    for (record in statusHeaderRecords) appendLine("# STATUS ${record.joinToString(" ")}")
    appendLine("# Logical fields: ${columns.joinToString(",")}")
    appendLine("# Policy old_value_or_error_status=STATUS override for its exact D/C/mode/kind/mask binding, else UNRESOLVED_POISONED_INTERACTION; old_trace_status=REFERENCE_OBSERVATION")
    appendLine("# Status meaning: REQUIRED_NULL_ARGUMENT_CONTRACT requires exception TYPE only; trace/message/noncalls/identity are not promoted to requirements")
    appendLine("# Policy old_message_status=REFERENCE_OBSERVATION if MESSAGE_RECORDED else NOT_RECORDED; runtime_identity_status=NOT_OBSERVED")
    appendLine("# Policy required_canonical_text_control=CANONICAL_TEXT:C-id for a successful control, else NOT_APPLICABLE")
    appendLine("# Policy unresolved_interactions=ordered FLAG names present except MESSAGE_RECORDED")
    appendLine("# Policy explicit_caller_language_control=EXPLICIT_DIVIDE_CALLER_MATRIX; archival_status=BLOCKED_UNRESOLVED_FIELDS")
    appendLine("# Required valid controls: DecimalContextContractTest.ordinaryAndTruthfulControlsPartition0..3")
    appendLine("# Canonical text: public BigDecimal.toString rules independently applied to expected coefficient@scale")
    appendLine("# Caller matrix: explicitBaseTypedCallerEntersOverridesAndPropagatesTheirException; null/self/finite/unlimited/null matrix")
    appendLine("# Test extensions/verification are separate; this manifest does not certify them")
    appendLine("# Internal order/count/noncalls/forwarding and diagnostic wording are observations; runtime class/cache/result identity are not measured")
    appendLine("# Unresolved poisoned value/error interactions block archival; bad unused getters never blanket-waive inherited contracts")
    appendLine("# Null8 boundary: only the eight STATUS exception types are required; every row stays blocked for archival")
    appendLine(compactColumns.joinToString("\t"))
    compact.forEach { appendLine(it.joinToString("\t")) }
}
val target = File("diagnostics/decimal-context-dispatch/contract-coverage.tsv")
validateStatusHeaders(generated)
check(target.exists()) { "The bounded classification requires the preserved existing compact payload" }
val existingText = target.readText()
val existingPayload = existingText.split('\n').filterNot { it.startsWith('#') }.joinToString("\n")
val expectedPayload = buildString {
    appendLine(compactColumns.joinToString("\t"))
    compact.forEach { appendLine(it.joinToString("\t")) }
}
check(existingPayload == expectedPayload) { "Compact header/data bytes changed; refuse classification expansion or row rewrite" }
if ("--check" in args) {
    check(existingText == generated) { "Contract coverage manifest differs" }
    validateStatusHeaders(existingText)
    val saved = target.readLines().filterNot { it.startsWith('#') }
    check(saved.first().split('\t') == compactColumns)
    validateCompact(saved.drop(1).map { it.split('\t') })
} else target.writeText(generated)
println("${if ("--check" in args) "Verified" else "Generated"} ${mapped.size} frozen row mappings to ${ids.size} controls; $maskChecks valid class-mask checks")
println("Required correspondence: 2475 value/scale/canonical-text controls; 429 arithmetic-error controls; 96 null-error controls")
println("Old observations: ${counts.getValue("old_value")} values; ${counts.getValue("old_error")} errors; ${requiredNullRows.size} exact null-argument error types required; ${mapped.size - requiredNullRows.size} poisoned value/error statuses unresolved; all ${mapped.size} rows blocked for archival")
println("All bound hashes, schemas, ordered projection, counts, control coverage and 19-field lossless decoding verified; no production math or JDK arithmetic evaluated")

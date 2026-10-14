#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Audit stored diagnostic inputs against the public precision/signum invariants. Usage: [--check]. */
import java.io.File
import java.security.MessageDigest

require(args.all { it == "--check" })
val data = File("diagnostics/decimal-context-extreme-metadata/Jdk25DecimalContextExtremeMetadataResults.kt")
val bytes = data.readBytes()
val sha256 = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
val rows = bytes.toString(Charsets.UTF_8).lineSequence()
    .filter { '\t' in it }.map { it.split('\t') }.toList()
require(rows.all { it.size == 10 }) { "Unexpected diagnostic row shape" }

fun coefficientDigits(text: String): String {
    require(text.matches(Regex("[+-]?[0-9]+"))) { "Unsupported coefficient encoding: $text" }
    return text.removePrefix("+").removePrefix("-").trimStart('0')
}
fun precision(text: String): Int = maxOf(1, coefficientDigits(text).length)
fun signum(text: String): Int = when {
    coefficientDigits(text).isEmpty() -> 0
    text.startsWith('-') -> -1
    else -> 1
}

var invalidSourcePrecision = 0
var invalidDivisorPrecision = 0
var invalidForcedSourceSign = 0
var invalidForcedDivisorSign = 0
var contractPreservingMetadata = 0
for (row in rows) {
    val sourcePrecisionInvalid = row[4].toInt() != precision(row[0])
    val divisorPrecisionInvalid = row[5].toInt() != precision(row[1])
    require(row[6] == "true" || row[6] == "false")
    val forced = row[6] == "true"
    val sourceSignInvalid = forced && signum(row[0]) != 1
    val divisorSignInvalid = forced && signum(row[1]) != 1
    if (sourcePrecisionInvalid) invalidSourcePrecision++
    if (divisorPrecisionInvalid) invalidDivisorPrecision++
    if (sourceSignInvalid) invalidForcedSourceSign++
    if (divisorSignInvalid) invalidForcedDivisorSign++
    if (!sourcePrecisionInvalid && !divisorPrecisionInvalid && !sourceSignInvalid && !divisorSignInvalid)
        contractPreservingMetadata++
}
println("dataSha256=$sha256")
println("rows=${rows.size}; invalidSourcePrecision=$invalidSourcePrecision; invalidDivisorPrecision=$invalidDivisorPrecision")
println("invalidForcedSourceSign=$invalidForcedSourceSign; invalidForcedDivisorSign=$invalidForcedDivisorSign")
println("contractPreservingMetadata=$contractPreservingMetadata")
if ("--check" in args) {
    check(sha256 == "ad85b1bc3ffa88446cc093cee162bfe33e2239611e5dc75c25ec3642c6e1688e") { "Diagnostic evidence changed" }
    check(rows.size == 8192)
    check(invalidSourcePrecision == rows.size && invalidDivisorPrecision == rows.size)
    check(invalidForcedSourceSign == 1536 && invalidForcedDivisorSign == 512)
    check(contractPreservingMetadata == 0)
    println("Verified the frozen input classification; no numerical implementation was evaluated.")
}

#!/usr/bin/env kotlin
// SPDX-License-Identifier: Apache-2.0
// Metadata only: preserve all 128 reference outcomes; never execute a math API.
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

check(Runtime.getRuntime().maxMemory() <= 256L * 1024 * 1024)
require(args.size == 3) { "Usage: JVM-XML fresh-own-JAR new-reference-TSV" }
val xml = Path.of(args[0]).toRealPath()
val jar = Path.of(args[1]).toRealPath()
val output = Path.of(args[2]).toAbsolutePath().normalize()
check(!Files.exists(output)) { "Preserve earlier evidence: choose a new output path" }
fun hash(path: Path): String {
    val digest = MessageDigest.getInstance("SHA-256")
    Files.newInputStream(path).use { stream ->
        val buffer = ByteArray(65536)
        while (true) { val count = stream.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
    }
    return digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
}
val original = Path.of("diagnostics/decimal-pow-working-precision/DecimalPowerBoundaryTest.before.kt.txt")
val source = Path.of("src/jvmTest/kotlin/io/heapy/krogu/math/DecimalPowerBoundaryTest.kt")
check(hash(original) == "017338c7d11eba085c936692d67cadf49ef202746531dc49bdd70289003dc938")
check(hash(source) == "f4ed1092214943950ad2ed7de82b89254130ab598a987b0cc988c4b50c4150e2")
val factory = DocumentBuilderFactory.newInstance()
factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
val suite = factory.newDocumentBuilder().parse(xml.toFile()).documentElement
check(suite.tagName == "testsuite")
for (attribute in listOf("failures", "errors", "skipped")) check(suite.getAttribute(attribute) == "0")
for (tag in listOf("failure", "error", "skipped")) check(suite.getElementsByTagName(tag).length == 0)
val expectedMethods = setOf("workingPrecisionOverflowSeparatesReferenceObservationsFromRequiredValues",
    "accumulatorRoundingAcrossExponentBitPatternsMatchesJdk")
val cases = suite.getElementsByTagName("testcase")
check(suite.getAttribute("tests") == "2" && cases.length == 2)
val methods = (0 until cases.length).map { index ->
    val case = cases.item(index) as Element
    check(case.getAttribute("classname") == "io.heapy.krogu.math.DecimalPowerBoundaryTest")
    case.getAttribute("name").substringBefore('[')
}
check(methods.toSet() == expectedMethods && methods.size == expectedMethods.size)
val modes = listOf("UP", "DOWN", "CEILING", "FLOOR", "HALF_UP", "HALF_DOWN", "HALF_EVEN", "UNNECESSARY")
val expectedKeys = buildSet {
    for (precision in listOf(Int.MAX_VALUE, Int.MAX_VALUE - 1, Int.MAX_VALUE - 2, Int.MAX_VALUE - 10))
        for (power in listOf(0, 1, 10, 999999999)) for (mode in modes)
            add(listOf("1@0", precision.toString(), power.toString(), mode))
}
val marker = "POW_WORKING_PRECISION_REFERENCE\t"
val streams = suite.getElementsByTagName("system-out")
val rows = (0 until streams.length).flatMap { index ->
    streams.item(index).textContent.lineSequence().filter { it.startsWith(marker) }.toList()
}.map { line -> line.removePrefix(marker).split('\t').also { check(it.size == 5) } }
check(rows.size == 128 && expectedKeys.size == 128)
val seen = linkedSetOf<List<String>>()
var errors = 0
for (row in rows) {
    val key = row.take(4)
    check(key in expectedKeys && seen.add(key)) { "Unexpected/duplicate reference key: $key" }
    val precision = row[1].toLong()
    val power = row[2].toInt()
    val overflow = power != 0 && precision + row[2].length + 1 > Int.MAX_VALUE
    if (overflow) { check(row[4].startsWith("!IllegalArgumentException:")); errors++ }
    else check(row[4] == "1")
}
check(seen == expectedKeys && errors == 64)
val evidence = buildString {
    appendLine("# schema=pow-working-precision-reference-v1; observed reference data, not a compatibility pass")
    appendLine("# XML ${hash(xml)} $xml")
    appendLine("# supplied_JAR_metadata_only ${hash(jar)} $jar")
    appendLine("# original_test ${hash(original)} $original")
    appendLine("# revised_test ${hash(source)} $source")
    appendLine("# auditor_runtime ${Runtime.version()}; test runtime/build origins require the separate execution ledger")
    appendLine("source\tprecision\texponent\tmode\toriginal_outcome")
    for (row in rows) appendLine(row.joinToString("\t"))
}
Files.writeString(output, evidence, java.nio.file.StandardOpenOption.CREATE_NEW)
println("PASS reference capture: 128 unique original keys, 64 values, 64 diagnostic errors; both JVM methods passed")
println("SHA256 ${hash(output)} $output")

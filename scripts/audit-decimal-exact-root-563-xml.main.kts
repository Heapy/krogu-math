#!/usr/bin/env kotlin
// Source-method inventory check. Usage: <target> <raw-xml-directory> <archive-directory>
// Does not execute port or reference arithmetic.
import java.io.File
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory

require(args.size == 3)
val target = args[0]
val input = File(args[1])
val archive = File(args[2]).also { it.mkdirs() }
fun sha(file: File) = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
val common = listOf("DecimalExactRoot563Test", "DecimalExactRoot563CallbackContractTest", "PortableOracleTest",
    "ScaleBoundaryOracleTest", "NullPrecedenceOracleTest", "IntegerSqrtOracleTest", "DecimalScaleContractTest")
val jvm = listOf("DecimalExactRoot563DiagnosticTest", "ScaleBoundaryConformanceTest", "DecimalNormalizedRootTest",
    "DecimalPowerSubclassTest", "DecimalPowerBoundaryTest", "DecimalPowerValidationTest", "DecimalConformanceTest",
    "SetScaleBoundaryTest", "KotlinSubclassContractTest")
val suites = common + if (target == "jvmTest") jvm else emptyList()
val expected = suites.associateWith { suite ->
    val sourceSet = if (suite in common) "commonTest" else "jvmTest"
    val source = File("src/$sourceSet/kotlin/io/heapy/krogu/math/$suite.kt")
    check(source.isFile) { "Missing source $source" }
    val methods = Regex("@Test\\s+fun\\s+(\\w+)\\s*\\(").findAll(source.readText()).map { it.groupValues[1] }.toSet()
    check(methods.isNotEmpty()) { "Missing annotated methods in $source" }
    Triple(source, sha(source), methods)
}
val xml = input.walkTopDown().filter { it.isFile && it.extension == "xml" }.sortedBy { it.path }.toList()
check(xml.isNotEmpty()) { "No raw XML: $input" }
check(xml.map { it.name }.toSet().size == xml.size) { "Duplicate raw XML basenames would collide in archive" }
val factory = DocumentBuilderFactory.newInstance().apply {
    setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
}
val actual = mutableMapOf<String, MutableList<String>>()
val xmlHashes = mutableListOf<String>()
for (file in xml) {
    val document = factory.newDocumentBuilder().parse(file)
    val cases = document.getElementsByTagName("testcase")
    for (i in 0 until cases.length) {
        val test = cases.item(i) as org.w3c.dom.Element
        val suite = test.getAttribute("classname").substringAfterLast('.')
        check(suite in suites) { "Unexpected suite $suite in $file" }
        val name = test.getAttribute("name").substringBefore('[').removeSuffix("()")
        for (status in listOf("failure", "error", "skipped"))
            check(test.getElementsByTagName(status).length == 0) { "$target $suite/$name has $status" }
        actual.getOrPut(suite) { mutableListOf() }.add(name)
    }
    file.copyTo(File(archive, file.name), overwrite = true)
    xmlHashes += "${sha(file)}  ${file.name}"
}
check(actual.keys == expected.keys) { "Suite inventory mismatch: expected=${expected.keys} actual=${actual.keys}" }
val inventory = buildString {
    appendLine("target\tsuite\tmethod\tsource\tsource-sha256")
    for ((suite, entry) in expected) {
        val names = actual.getValue(suite)
        check(names.size == names.toSet().size) { "Duplicate methods $suite: $names" }
        check(names.toSet() == entry.third) { "$suite expected=${entry.third} actual=$names" }
        for (method in entry.third.sorted()) appendLine("$target\t$suite\t$method\t${entry.first.path}\t${entry.second}")
    }
}
File(archive, "source-method-inventory.tsv").writeText(inventory)
File(archive, "raw-xml-sha256.txt").writeText(xmlHashes.joinToString("\n", postfix = "\n"))
println("PASS $target: ${actual.size} required suites, ${actual.values.sumOf { it.size }} source methods, zero failures/errors/skips; raw XML archived at $archive")

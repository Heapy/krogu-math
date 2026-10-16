#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// SOURCE PREPARATION ONLY (587 policy +588 text +589 exact chars): uncompiled/unrun when prepared. No dependencies.
// Usage: kotlinr audit-composed247-xml.main.kts ROOT MANIFEST NEW_OUTPUT_DIR [target=XML_DIR ...]
// Targets: jvmTest jsNodeTest wasmJsNodeTest macosArm64Test. Omitted targets are UNRUN.
// Exit0 requires all four exact inventories with no issue; partial never passes.
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.security.MessageDigest
import javax.xml.XMLConstants
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import kotlin.system.exitProcess

val manifestPin = "529cc142d9a42110084155987b3022b36c2309168a159b745bf5191a0359c5a4"
val buildPin = "b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6"
require(args.size >= 3) { "ROOT MANIFEST NEW_OUTPUT_DIR [target=XML_DIR ...]" }
val root = Path.of(args[0]).toRealPath()
val manifest = Path.of(args[1]).toRealPath()
val output = Path.of(args[2]).toAbsolutePath().normalize()
require(!Files.exists(output, NOFOLLOW_LINKS)) { "Output must be new: $output" }
val sets = linkedMapOf(
    "jvmTest" to setOf("commonTest", "jvmTest"),
    "jsNodeTest" to setOf("commonTest", "nonJvmTest", "jsTest"),
    "wasmJsNodeTest" to setOf("commonTest", "nonJvmTest", "wasmJsTest"),
    "macosArm64Test" to setOf("commonTest", "nonJvmTest", "nativeTest", "appleTest", "macosTest", "macosArm64Test")
)
val suffix = mapOf("jvmTest" to "[jvm]", "jsNodeTest" to "[js, node]",
    "wasmJsNodeTest" to "[wasmJs, node]", "macosArm64Test" to "[macosArm64]")
val selected = linkedMapOf<String, Path>()
for (arg in args.drop(3)) {
    val pair = arg.split('=', limit = 2)
    require(pair.size == 2 && pair[0] in sets && pair[0] !in selected) { "Invalid/duplicate target: $arg" }
    selected[pair[0]] = Path.of(pair[1]).toAbsolutePath().normalize()
}
fun hash(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
    .joinToString("") { "%02x".format(it.toInt() and 255) }
fun hashFile(path: Path): String {
    val md = MessageDigest.getInstance("SHA-256")
    Files.newInputStream(path).use { input ->
        val buffer = ByteArray(65536)
        while (true) { val n = input.read(buffer); if (n < 0) break; md.update(buffer, 0, n) }
    }
    return md.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
}
check(hashFile(manifest) == manifestPin) { "Manifest pin mismatch" }
val pins = linkedMapOf<String, String>()
for (line in Files.readAllLines(manifest)) {
    val match = Regex("([0-9a-f]{64})  (src/[^\\r\\n]+)").matchEntire(line) ?: error("Bad manifest row")
    val rel = match.groupValues[2]
    require((rel.endsWith(".kt") || rel.endsWith(".java")) && !rel.contains('\\')) { "Bad source path $rel" }
    require(pins.put(rel, match.groupValues[1]) == null) { "Duplicate source path $rel" }
}
check(pins.size == 247) { "Expected the frozen 247-source manifest" }
fun verifyPins() {
    check(hashFile(manifest) == manifestPin && hashFile(root.resolve("build.gradle.kts")) == buildPin) { "Input pin changed" }
    val files = Files.walk(root.resolve("src")).use { stream -> stream.filter {
        Files.isRegularFile(it, NOFOLLOW_LINKS) && (it.toString().endsWith(".kt") || it.toString().endsWith(".java"))
    }.map { root.relativize(it).toString().replace('\\', '/') }.toList().toSet() }
    check(files == pins.keys) { "Source path set changed: extra=${files - pins.keys}, missing=${pins.keys - files}" }
    for ((rel, pin) in pins) {
        val path = root.resolve(rel).normalize()
        check(path.startsWith(root) && path.toRealPath().startsWith(root) && !Files.isSymbolicLink(path)) { "Unsafe source $rel" }
        check(hashFile(path) == pin) { "Changed source $rel" }
    }
}
verifyPins()

// A deliberately restricted lexer, not a Kotlin/Java compiler. Strings/comments
// cannot manufacture annotations. Unsupported @Test ownership/header fails closed.
data class Token(val text: String, val offset: Int, val identifier: Boolean = false)
fun commentEnd(text: String, start: Int): Int {
    var i = start + 2; var depth = 1
    while (depth > 0) {
        check(i < text.length) { "Unterminated comment" }
        when { text.startsWith("/*", i) -> { depth++; i += 2 }
            text.startsWith("*/", i) -> { depth--; i += 2 }; else -> i++ }
    }
    return i
}
fun literalEnd(text: String, start: Int, kotlin: Boolean): Int {
    val quote = text[start]; val raw = text.startsWith("\"\"\"", start)
    val delimiter = if (raw) "\"\"\"" else quote.toString()
    var i = start + delimiter.length
    while (i < text.length) {
        if (!raw && text[i] == '\\') { i += 2; continue }
        if (text.startsWith(delimiter, i)) return i + delimiter.length
        // Nested quoted expressions inside Kotlin interpolation must not end
        // the outer literal or manufacture source braces/annotations.
        if (kotlin && quote == '"' && text.startsWith("${'$'}{", i)) {
            i += 2; var depth = 1
            while (depth > 0) {
                check(i < text.length) { "Unterminated interpolation" }
                when {
                    text.startsWith("//", i) -> i = text.indexOf('\n', i).let { if (it < 0) text.length else it }
                    text.startsWith("/*", i) -> i = commentEnd(text, i)
                    text[i] == '"' || text[i] == '\'' -> i = literalEnd(text, i, kotlin)
                    text[i] == '`' -> { val end = text.indexOf('`', i + 1); check(end >= 0); i = end + 1 }
                    text[i] == '{' -> { depth++; i++ }
                    text[i] == '}' -> { depth--; i++ }
                    else -> i++
                }
            }
        } else i++
    }
    error("Unterminated quoted literal")
}
fun tokens(text: String, kotlin: Boolean): List<Token> {
    val result = mutableListOf<Token>(); var i = 0
    while (i < text.length) {
        val start = i; val c = text[i]
        when {
            c.isWhitespace() -> i++
            text.startsWith("//", i) -> { i = text.indexOf('\n', i).let { if (it < 0) text.length else it } }
            text.startsWith("/*", i) -> i = commentEnd(text, i)
            c == '"' || c == '\'' -> i = literalEnd(text, i, kotlin)
            c == '`' -> {
                val end = text.indexOf('`', i + 1); check(end >= 0) { "Unterminated identifier" }
                result += Token(text.substring(i + 1, end), start, true); i = end + 1
            }
            c.isLetterOrDigit() || c == '_' || c == '$' -> {
                while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_' || text[i] == '$')) i++
                result += Token(text.substring(start, i), start, true)
            }
            else -> { result += Token(c.toString(), start); i++ }
        }
    }
    return result
}
data class SourceTest(val sourceSet: String, val owner: String, val method: String, val file: String, val line: Int)
val inventory = mutableListOf<SourceTest>()
for ((rel, pin) in pins) {
    val sourceSet = rel.split('/')[1]
    if (!sourceSet.endsWith("Test")) continue
    val bytes = Files.readAllBytes(root.resolve(rel)); check(hash(bytes) == pin) { "Source changed while inventoried: $rel" }
    val text = bytes.toString(Charsets.UTF_8); val ts = tokens(text, rel.endsWith(".kt"))
    val annotations = mutableListOf<Pair<Int, Int>>()
    for (i in ts.indices) if (ts[i].text == "@") {
        var j = i + 1
        if (j >= ts.size || !ts[j].identifier) continue
        var name = ts[j++].text
        while (j + 1 < ts.size && ts[j].text == "." && ts[j + 1].identifier) { name = ts[j + 1].text; j += 2 }
        if (name in setOf("Ignore", "Disabled", "RunWith", "ParameterizedTest")) error("Unsupported test lifecycle $rel:${ts[i].offset}")
        if (name == "Test") annotations += i to j
    }
    if (annotations.isEmpty()) continue
    check(sourceSet in sets.values.flatten().toSet()) { "Unmapped test source set $sourceSet" }
    val pi = ts.indexOfFirst { it.text == "package" }; check(pi >= 0) { "Missing package $rel" }
    var j = pi + 1; val packageParts = mutableListOf(ts[j++].text)
    while (j + 1 < ts.size && ts[j].text == ".") { packageParts += ts[j + 1].text; j += 2 }
    val pkg = packageParts.joinToString(".")
    check(pkg == "io.heapy.krogu.math" || pkg.startsWith("io.heapy.krogu.math.")) { "Outside selected math package: $rel" }
    val depths = IntArray(ts.size); var depth = 0
    for (i in ts.indices) { depths[i] = depth; if (ts[i].text == "{") depth++; if (ts[i].text == "}") depth--; check(depth >= 0) }
    check(depth == 0) { "Unbalanced source braces $rel" }
    val owner = Path.of(rel).fileName.toString().substringBeforeLast('.')
    val declarations = ts.indices.filter { ts[it].text == "class" && depths[it] == 0 && ts.getOrNull(it + 1)?.text == owner }
    check(declarations.size == 1) { "Expected one filename-matching top-level test class: $rel" }
    var body = declarations.single() + 2; var parens = 0; var brackets = 0
    while (body < ts.size) {
        val t = ts[body].text
        if (t == "{" && depths[body] == 0 && parens == 0 && brackets == 0) break
        when (t) { "(" -> parens++; ")" -> parens--; "[" -> brackets++; "]" -> brackets-- }
        body++
    }
    check(body < ts.size) { "Missing test class body $rel" }
    val end = (body + 1 until ts.size).firstOrNull { ts[it].text == "}" && depths[it] == 1 } ?: error("Missing class end $rel")
    for ((at, afterName) in annotations) {
        check(at in body + 1 until end && depths[at] == 1) { "Unsupported nested/top-level @Test in $rel" }
        j = afterName
        if (ts.getOrNull(j)?.text == "(") {
            var level = 1; j++
            while (level > 0 && j < ts.size) { if (ts[j].text == "(") level++; if (ts[j].text == ")") level--; j++ }
            check(level == 0)
        }
        var steps = 0
        while (j < ts.size && ts[j].text !in setOf("fun", "void", "{", ";", "class") && steps++ < 64) j++
        check(ts.getOrNull(j)?.text in setOf("fun", "void") && ts.getOrNull(j + 1)?.identifier == true &&
            ts.getOrNull(j + 2)?.text == "(" && ts.getOrNull(j + 3)?.text == ")") { "Unsupported @Test header in $rel at ${ts[at].offset}" }
        val method = ts[j + 1].text
        inventory += SourceTest(sourceSet, "$pkg.$owner", method, rel, 1 + text.take(ts[at].offset).count { it == '\n' })
    }
}
check(inventory.isNotEmpty()) { "Empty source inventory" }
fun escape(value: Any?) = value.toString().replace("\\", "\\\\").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n")
fun table(path: Path, header: List<String>, rows: List<List<Any?>>) {
    Files.newBufferedWriter(path).use { out ->
        out.appendLine(header.joinToString("\t"))
        for (row in rows) out.appendLine(row.joinToString("\t") { escape(it) })
    }
}
val expected = sets.mapValues { (_, allowed) -> inventory.filter { it.sourceSet in allowed }.sortedWith(compareBy({ it.owner }, { it.method })) }
for ((target, rows) in expected) {
    check(rows.map { it.owner to it.method }.toSet().size == rows.size) { "Duplicate source tests for $target" }
}
// Finish and persist every source expectation BEFORE opening any XML file.
Files.createDirectories(output)
for ((target, rows) in expected) {
    table(output.resolve("$target.expected.tsv"), listOf("class", "method", "sourceSet", "file", "line"),
        rows.map { listOf(it.owner, it.method, it.sourceSet, it.file, it.line) })
    println("SOURCE $target classes=${rows.map { it.owner }.toSet().size} methods=${rows.size}")
}
verifyPins()

data class CaseRow(val file: String, val suite: String, val rawClass: String, val rawMethod: String,
    val owner: String, val method: String, var skipped: Int = 0, var failures: Int = 0, var errors: Int = 0)
data class SuiteRow(val file: String, val name: String, val tests: Int, val skipped: Int, val failures: Int, val errors: Int,
    val cases: MutableList<CaseRow> = mutableListOf())
val issues = mutableListOf<List<Any?>>()
val summaries = mutableListOf<List<Any?>>()
var failed = false
fun clean(raw: String, target: String): String = raw.removePrefix("$target.").removeSuffix(suffix.getValue(target))
for ((target, want) in expected) {
    val cases = mutableListOf<CaseRow>(); val suites = mutableListOf<SuiteRow>()
    val xmlFiles = mutableListOf<Path>(); val priorIssues = issues.size
    fun issue(message: String) { issues += listOf(target, message) }
    val dir = selected[target]
    if (dir == null) {
        issue("UNRUN: no XML directory selected")
    } else try {
        check(Files.isDirectory(dir, NOFOLLOW_LINKS) && !Files.isSymbolicLink(dir)) { "Missing/unsafe XML directory $dir" }
        Files.walk(dir).use { stream -> stream.filter { Files.isRegularFile(it, NOFOLLOW_LINKS) && it.toString().endsWith(".xml") }
            .sorted().forEach { xmlFiles.add(it) } }
        check(xmlFiles.isNotEmpty()) { "No XML files in $dir" }
        for (file in xmlFiles) {
            val factory = SAXParserFactory.newInstance()
            factory.isNamespaceAware = false; factory.isValidating = false
            for ((feature, enabled) in listOf(XMLConstants.FEATURE_SECURE_PROCESSING to true,
                "http://apache.org/xml/features/disallow-doctype-decl" to true,
                "http://xml.org/sax/features/external-general-entities" to false,
                "http://xml.org/sax/features/external-parameter-entities" to false,
                "http://apache.org/xml/features/nonvalidating/load-external-dtd" to false)) factory.setFeature(feature, enabled)
            val reader = factory.newSAXParser().xmlReader
            reader.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            reader.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
            reader.entityResolver = org.xml.sax.EntityResolver { _, _ -> throw SAXException("External entity forbidden") }
            reader.contentHandler = object : DefaultHandler() {
                var currentSuite: SuiteRow? = null; var currentCase: CaseRow? = null
                override fun startElement(uri: String?, localName: String?, qName: String, a: Attributes) {
                    fun required(name: String) = a.getValue(name) ?: error("Missing XML $name in $file/$qName")
                    fun count(name: String) = required(name).toInt().also { check(it >= 0) }
                    when (qName) {
                        "testsuite" -> {
                            check(currentSuite == null) { "Nested suite in $file" }
                            currentSuite = SuiteRow(file.toString(), required("name"), count("tests"), count("skipped"), count("failures"), count("errors"))
                            suites += currentSuite!!
                            a.getValue("disabled")?.let { if (it != "0") issue("Disabled suite: $file") }
                        }
                        "testcase" -> {
                            val suite = currentSuite ?: error("Testcase outside suite $file")
                            check(currentCase == null)
                            val rawClass = required("classname"); val rawMethod = required("name")
                            currentCase = CaseRow(file.toString(), suite.name, rawClass, rawMethod,
                                clean(rawClass, target), rawMethod.removeSuffix(suffix.getValue(target)))
                            cases += currentCase!!; suite.cases += currentCase!!
                            a.getValue("status")?.let { if (it !in setOf("run", "passed")) issue("Non-running testcase status $it in $file") }
                        }
                        "skipped" -> (currentCase ?: error("Skipped outside testcase $file")).skipped++
                        "failure" -> (currentCase ?: error("Failure outside testcase $file")).failures++
                        "error" -> (currentCase ?: error("Error outside testcase $file")).errors++
                    }
                }
                override fun endElement(uri: String?, localName: String?, qName: String) {
                    if (qName == "testcase") currentCase = null
                    if (qName == "testsuite") { check(currentCase == null); currentSuite = null }
                }
                // Default characters() discards all text, including giant system-out.
            }
            reader.errorHandler = object : DefaultHandler() {
                override fun error(e: org.xml.sax.SAXParseException) { throw e }
                override fun fatalError(e: org.xml.sax.SAXParseException) { throw e }
            }
            Files.newInputStream(file).use { reader.parse(InputSource(it)) }
        }
        val wanted = want.map { it.owner to it.method }.toSet()
        val actual = cases.groupBy { it.owner to it.method }
        for (key in wanted - actual.keys) issue("Missing ${key.first}#${key.second}")
        for (key in actual.keys - wanted) issue("Extra ${key.first}#${key.second}")
        for ((key, rows) in actual) if (rows.size != 1) issue("Duplicate ${key.first}#${key.second}: ${rows.size}")
        for (case in cases) if (case.skipped + case.errors + case.failures != 0) issue("Nonpass ${case.owner}#${case.method}: skip=${case.skipped} fail=${case.failures} error=${case.errors}")
        val owners = want.map { it.owner }.toSet(); val seenSuites = mutableListOf<String>()
        for (suite in suites) {
            val classes = suite.cases.map { it.owner }.toSet()
            val aliases = owners.filter { it == clean(suite.name, target) || it.substringAfterLast('.') == clean(suite.name, target) }
            val owner = if (classes.size == 1) classes.single() else aliases.singleOrNull()
            if (owner == null || owner !in owners || clean(suite.name, target) !in setOf(owner, owner.substringAfterLast('.'))) issue("Unmatched suite ${suite.file}/${suite.name}")
            else { seenSuites += owner; if (classes.any { it != owner }) issue("Mixed suite classes ${suite.name}") }
            if (suite.tests != suite.cases.size || suite.skipped != suite.cases.sumOf { it.skipped } ||
                suite.failures != suite.cases.sumOf { it.failures } || suite.errors != suite.cases.sumOf { it.errors }) issue("Suite counters disagree ${suite.file}")
            if (suite.skipped + suite.failures + suite.errors != 0) issue("Nonpass suite ${suite.file}")
        }
        for (owner in owners) if (seenSuites.count { it == owner } != 1) issue("Suite multiplicity $owner: ${seenSuites.count { it == owner }}")
    } catch (failure: Exception) { issue("AUDIT_ERROR ${failure.javaClass.name}: ${failure.message}") }
    table(output.resolve("$target.actual.tsv"), listOf("class", "method", "rawClass", "rawMethod", "skipped", "failures", "errors", "suite", "xml"),
        cases.map { listOf(it.owner, it.method, it.rawClass, it.rawMethod, it.skipped, it.failures, it.errors, it.suite, it.file) })
    table(output.resolve("$target.suites.tsv"), listOf("suite", "tests", "skipped", "failures", "errors", "parsedCases", "xml"),
        suites.map { listOf(it.name, it.tests, it.skipped, it.failures, it.errors, it.cases.size, it.file) })
    val state = if (dir == null) "UNRUN" else if (issues.size == priorIssues) "PASS" else "FAIL"
    if (state == "FAIL") failed = true
    summaries += listOf(target, state, want.map { it.owner }.toSet().size, want.size, xmlFiles.size, suites.size, cases.size, issues.size - priorIssues)
}
var sourcePost = "PASS"
try { verifyPins() } catch (failure: Exception) { sourcePost = "FAIL"; failed = true; issues += listOf("SOURCE_POST", failure.message) }
table(output.resolve("issues.tsv"), listOf("target", "issue"), issues)
table(output.resolve("summary.tsv"), listOf("target", "status", "expectedClasses", "expectedMethods", "xmlFiles", "suites", "actualMethods", "issues"), summaries)
Files.writeString(output.resolve("provenance.tsv"), "manifest\t$manifestPin\nbuild\t$buildPin\nsourcePre\tPASS\nsourcePost\t$sourcePost\nroot\t${escape(root)}\n")
val complete = !failed && selected.size == sets.size && issues.isEmpty()
println("AUDIT ${if (complete) "PASS" else if (failed) "FAIL" else "PARTIAL_UNRUN"} output=$output")
exitProcess(if (complete) 0 else if (failed) 1 else 2)

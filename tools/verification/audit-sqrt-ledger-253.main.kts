#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// SOURCE ONLY (593 composition), UNCOMPILED/UNRUN when prepared. No dependencies, process or network calls.
// Usage: kotlinr audit-sqrt-ledger-253.main.kts ROOT MANIFEST NEW_OUTPUT_DIR SCRIPT_FILE REVIEWED_SCRIPT_SHA [target=XML_DIR ...]
import java.math.BigInteger as BI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.security.MessageDigest
import java.lang.management.ManagementFactory
import javax.xml.XMLConstants
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import kotlin.system.exitProcess

val manifestPin = "aa90e836005efc363e9575026cd12049b0bb6ac42eb3a716f20d04c596bac1ad"
val buildPin = "b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6"
val helperPin = "97d0aeca46ad8137d9f82408484690552228b7bfb48e7dc62b7accf9d5db994d"
val modes = listOf("UP", "DOWN", "CEILING", "FLOOR", "HALF_UP", "HALF_DOWN", "HALF_EVEN", "UNNECESSARY")
val compactScales = listOf(Int.MIN_VALUE, Int.MIN_VALUE+1, Int.MIN_VALUE+5, -1, 0, 1,
    Int.MAX_VALUE-5, Int.MAX_VALUE-1, Int.MAX_VALUE)
val compactCoefficients = listOf("-1000", "-100", "-3", "-1", "0", "1", "3", "100", "1000")
val compactP = listOf(0, 1, 2, 7)
val wideP = listOf(1, 2, 7, 17, 18, 19, 34, 120)
val wideScales = setOf(Int.MIN_VALUE, Int.MIN_VALUE+1, Int.MIN_VALUE+2, Int.MIN_VALUE+16, Int.MIN_VALUE+32,
    Int.MIN_VALUE/2, -10, 0, 10, Int.MAX_VALUE/2, Int.MAX_VALUE-32, Int.MAX_VALUE-16,
    Int.MAX_VALUE-2, Int.MAX_VALUE-1, Int.MAX_VALUE)
val fixedInputs = listOf("0", "0.00", "0E+5", "0E-5", "1.0000", "4.00", "100.00", "0.01", "2", "3", "5", "10",
    "81", "0.99999999", "1.00000001", "9999", "999999", "1E-30", "1E+31", "10000000000000000000000000001")
val fixedP = listOf(0, 1, 2, 3, 7, 16)
val raw = mapOf("13823" to listOf("0.0000001", "20", "CEILING", "0.0003162277660168379332", "31622776601683793320", "23"),
    "14089" to listOf("2.89086115118547718274465872620E-12", "18", "HALF_EVEN", "0.0000017002532608954106", "170025326089541060", "23"),
    "15172" to listOf("909393907682500.536454623858577", "17", "UP", "30156158.70236958", "30156158702369580", "9"))
val columns = listOf("name", "profile", "key", "inputText", "inputCoefficient", "inputScale", "p", "mode",
    "referenceSource", "referenceKind", "referenceText", "referenceCoefficient", "referenceScale", "referencePrecision",
    "referenceErrorType", "referenceErrorMessage", "referenceUnexpected", "disposition", "certificate",
    "requiredKind", "requiredText", "requiredCoefficient", "requiredScale", "requiredPrecision", "requiredErrorType",
    "actualKind", "actualText", "actualCoefficient", "actualScale", "actualPrecision", "actualErrorType",
    "actualErrorMessage", "actualUnexpected", "control563Kind", "control563Text", "control563Coefficient",
    "control563Scale", "control563Precision", "control563ErrorType", "control563ErrorMessage", "control563Unexpected",
    "guardFailure", "rawTextEquality", "strictPass")
check(columns.size == 44)
val targets = listOf("jvmTest", "jsNodeTest", "wasmJsNodeTest", "macosArm64Test")
val suffixes = mapOf("jvmTest" to "[jvm]", "jsNodeTest" to "[js, node]", "wasmJsNodeTest" to "[wasmJs, node]", "macosArm64Test" to "[macosArm64]")
val owners = listOf("PortableOracleTest", "ScaleBoundaryOracleTest", "DecimalConformanceTest", "JdkOracleTest", "ScaleBoundaryConformanceTest")
val methodSets = mapOf(
    "PortableOracleTest" to setOf("jdk25BehaviorOnEveryPlatform", "immutableValuesAndAlgebraicInvariants", "contextsAndRoundingConstants", "modularArithmeticAndRandomGeneration"),
    "ScaleBoundaryOracleTest" to (setOf("compactCoefficientsAndScaleEndpoints") + (0..9).map { "wideCoefficientsBatch%02d".format(it) }),
    "DecimalConformanceTest" to setOf("structuredRoundingAndPreferredScales", "rootsAndPowers", "doubleCanonicalizationCampaign", "byteAndCharacterConstructorRanges"),
    "JdkOracleTest" to setOf("integerArithmeticAndBits", "modularArithmeticAndPrimes", "decimals", "floatingPointBoundaries", "serializationAndJavaAdapters", "invalidInputsAndScaleLimits"),
    "ScaleBoundaryConformanceTest" to setOf("wideCoefficientsAtScaleLimits", "arithmeticAtSignedScaleLimits"))

fun hashFile(path: Path): String {
    val md = MessageDigest.getInstance("SHA-256")
    Files.newInputStream(path).use { input -> val buffer = ByteArray(65536)
        while (true) { val n = input.read(buffer); if (n < 0) break; md.update(buffer, 0, n) } }
    return md.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
}
require(args.size >= 5) { "ROOT MANIFEST NEW_OUTPUT_DIR SCRIPT_FILE REVIEWED_SCRIPT_SHA [target=XML_DIR ...]" }
val root = Path.of(args[0]).toRealPath()
val manifest = Path.of(args[1]).toRealPath()
val output = Path.of(args[2]).toAbsolutePath().normalize()
val script = Path.of(args[3]).toRealPath()
val scriptPin = args[4].also { require(Regex("[0-9a-f]{64}").matches(it)) }
require(!Files.exists(output, NOFOLLOW_LINKS) && !output.startsWith(root)) { "Output must be new and outside ROOT" }
val selected = linkedMapOf<String, Path>()
for (arg in args.drop(5)) {
    val pair = arg.split('=', limit=2)
    require(pair.size == 2 && pair[0] in targets && pair[0] !in selected)
    val dir = Path.of(pair[1]).toRealPath()
    require(dir == root.resolve("build/test-results/${pair[0]}").toRealPath()) { "Wrong XML ownership: $arg" }
    selected[pair[0]] = dir
}
check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS")
check(KotlinVersion.CURRENT.toString() == "2.4.21")
check(BI::class.java.classLoader == null && BI::class.java.module.name == "java.base" && BI::class.java.protectionDomain.codeSource == null)
val flags = ManagementFactory.getRuntimeMXBean().inputArguments
check(flags.lastOrNull { it.startsWith("-Xmx") } == "-Xmx256m" && "-XX:+UseSerialGC" in flags)
val cache = requireNotNull(System.getenv("KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR"))
require(cache.isNotEmpty() && Path.of(cache).isAbsolute)
check(hashFile(script) == scriptPin && hashFile(manifest) == manifestPin)
val pins = linkedMapOf<String, String>()
for (line in Files.readAllLines(manifest)) {
    val m = requireNotNull(Regex("([0-9a-f]{64})  (src/[^\\r\\n]+\\.(?:kt|java))").matchEntire(line))
    check(pins.put(m.groupValues[2], m.groupValues[1]) == null)
}
check(pins.size == 253 && pins["src/commonTest/kotlin/io/heapy/krogu/math/SqrtCohort587Comparison.kt"] == helperPin)
fun verifySource() {
    check(hashFile(manifest) == manifestPin && hashFile(script) == scriptPin && hashFile(root.resolve("build.gradle.kts")) == buildPin)
    val actual = Files.walk(root.resolve("src")).use { stream -> stream.peek { check(!Files.isSymbolicLink(it)) { "Source symlink $it" } }
        .filter { Files.isRegularFile(it, NOFOLLOW_LINKS) }.map { root.relativize(it).toString().replace('\\','/') }.toList().toSet() }
    check(actual == pins.keys) { "Source path set changed" }
    for ((rel, pin) in pins) { val file = root.resolve(rel).normalize()
        check(file.startsWith(root) && file.toRealPath().startsWith(root) && !Files.isSymbolicLink(file))
        check(hashFile(file) == pin) { "Changed $rel" } }
}
verifySource()

// Diagnostic comparison integers promise sign/zero, not a nonzero magnitude.
// matchEntire anchors the full grammar. Canonical tokens plus toIntOrNull
// reject overflow, +1, -0, leading zeros, whitespace and extra components.
fun explicitSign(n: Int): Int = if (n < 0) -1 else if (n > 0) 1 else 0
fun nearestSigns(text: String): Triple<Int,Int,Boolean>? {
    val match = Regex("NEAREST_ADJACENT/(0|-?[1-9][0-9]*)/(0|-?[1-9][0-9]*)/decade=(true|false)")
        .matchEntire(text) ?: return null
    val lo = match.groupValues[1].toIntOrNull() ?: return null
    val hi = match.groupValues[2].toIntOrNull() ?: return null
    return Triple(explicitSign(lo),explicitSign(hi),match.groupValues[3] == "true")
}
fun matchesCertificate(expected: String,recorded: String): Boolean {
    if (expected == "UP_ADJACENT" || expected == "DOWN_ADJACENT") return recorded == expected
    val checked = nearestSigns(expected) ?: return false
    val observation = nearestSigns(recorded) ?: return false
    return checked == observation
}
// Pure-string controls only, AFTER all initial tool/source guards. These do
// not execute a numerical certificate, production/helper library or process.
val nearestControl = "NEAREST_ADJACENT/1/-1/decade=true"
check(matchesCertificate(nearestControl,"NEAREST_ADJACENT/1/-4/decade=true"))
check(matchesCertificate(nearestControl,"NEAREST_ADJACENT/2147483647/-2147483648/decade=true"))
check(matchesCertificate("NEAREST_ADJACENT/0/-1/decade=false","NEAREST_ADJACENT/0/-4/decade=false"))
check(matchesCertificate("NEAREST_ADJACENT/1/0/decade=true","NEAREST_ADJACENT/9/0/decade=true"))
for (wrong in listOf("NEAREST_ADJACENT/-1/-4/decade=true", "NEAREST_ADJACENT/1/4/decade=true",
    "NEAREST_ADJACENT/0/-4/decade=true", "NEAREST_ADJACENT/1/0/decade=true", "NEAREST_ADJACENT/1/-4/decade=false"))
    check(!matchesCertificate(nearestControl,wrong))
for (bad in listOf("NEAREST_ADJACENT/+1/-4/decade=true", "NEAREST_ADJACENT/-0/-4/decade=true",
    "NEAREST_ADJACENT/01/-4/decade=true", "NEAREST_ADJACENT/1/-04/decade=true",
    "NEAREST_ADJACENT/2147483648/-4/decade=true", "NEAREST_ADJACENT/1/-2147483649/decade=true",
    "NEAREST_ADJACENT//-4/decade=true", "NEAREST_ADJACENT/1/-4/decade=TRUE",
    "NEAREST_ADJACENT/1/-4/decade=true/extra", "NEAREST_ADJACENT/1/-4/decade=true\n",
    " UNKNOWN", "UNKNOWN", "UNKNOWN/1/-4/decade=true")) {
    check(!matchesCertificate(nearestControl,bad))
    check(!matchesCertificate(bad,bad))
}
check(matchesCertificate("UP_ADJACENT","UP_ADJACENT"))
check(matchesCertificate("DOWN_ADJACENT","DOWN_ADJACENT"))
check(!matchesCertificate("UP_ADJACENT","DOWN_ADJACENT"))
check(!matchesCertificate("UP_ADJACENT","UP_ADJACENT/1"))
check(!matchesCertificate("DOWN_ADJACENT",nearestControl))
check(!matchesCertificate(nearestControl,"UP_ADJACENT"))

// Null and empty are distinct; only the producer's canonical escapes are legal.
fun esc(value: String?): String = value?.replace("\\", "\\\\")?.replace("\t", "\\t")?.replace("\r", "\\r")?.replace("\n", "\\n") ?: "\\N"
fun decode(raw: String): String? {
    if (raw == "\\N") return null
    val out = StringBuilder(); var i = 0
    while (i < raw.length) {
        val c = raw[i++]
        if (c != '\\') out.append(c) else {
            require(i < raw.length)
            out.append(when (raw[i++]) { '\\' -> '\\'; 't' -> '\t'; 'r' -> '\r'; 'n' -> '\n'; else -> error("Unknown escape") })
        }
    }
    return out.toString().also { check(esc(it) == raw) }
}
data class Fields(val c: String, val s: Int, val p: Int, val text: String)
data class Decimal(val c: String, val s: Long)
fun canonical(c: String, s: Int): String {
    require(Regex("-?(?:0|[1-9][0-9]*)").matches(c) && c != "-0")
    val sign = if (c.startsWith('-')) "-" else ""; val digits = c.removePrefix("-")
    require(digits.length <= 155)
    val exponent = digits.length - 1L - s
    if (s >= 0 && exponent >= -6) {
        val point = digits.length.toLong() - s
        require(point >= -5)
        return sign + when { s == 0 -> digits; point > 0 -> digits.take(point.toInt())+"."+digits.drop(point.toInt())
            else -> "0."+"0".repeat((-point).toInt())+digits }
    }
    return sign + digits.take(1) + (if (digits.length > 1) "."+digits.drop(1) else "") + "E" +
        (if (exponent >= 0) "+" else "") + exponent
}
fun parseText(text: String, strict: Boolean): Fields {
    require(text.length <= 200)
    val m = requireNotNull(Regex("(-?)([0-9]+)(?:\\.([0-9]+))?(?:E([+-]?[0-9]+))?").matchEntire(text))
    val fraction = m.groupValues[3]
    val e = m.groupValues[4].ifEmpty { "0" }.toLong().also { require(it in -4_300_000_000L..4_300_000_000L) }
    val digits = (m.groupValues[2]+fraction).trimStart('0').ifEmpty { "0" }
    require(digits.length <= 155)
    val c = (if (digits != "0") m.groupValues[1] else "") + digits
    val s = fraction.length.toLong()-e
    require(s in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
    return Fields(c, s.toInt(), digits.length, text).also { if (strict) check(canonical(c, it.s) == text) }
}
fun cmp(a: Decimal, b: Decimal): Int {
    require(a.c.all { it in '0'..'9' } && b.c.all { it in '0'..'9' } && a.c.isNotEmpty() && b.c.isNotEmpty())
    if (a.c == "0" || b.c == "0") return when { a.c == b.c -> 0; a.c == "0" -> -1; else -> 1 }
    val exp = (a.c.length.toLong()-a.s).compareTo(b.c.length.toLong()-b.s)
    if (exp != 0) return exp
    for (i in 0 until maxOf(a.c.length,b.c.length)) {
        val digit = (a.c.getOrNull(i) ?: '0').compareTo(b.c.getOrNull(i) ?: '0'); if (digit != 0) return digit
    }
    return 0
}
fun squared(x: Decimal): Decimal { require(x.c.length <= 122); val c = BI(x.c); return Decimal(c.multiply(c).toString(), 2L*x.s) }
fun certificate(x: Decimal, y: Fields, p: Int, mode: String): String {
    require(p in 1..120 && y.p == p && !y.c.startsWith('-') && y.c != "0")
    val r = BI(y.c); val scale = y.s.toLong()
    val decade = y.c == "1"+"0".repeat(p-1)
    val previous = if (decade) Decimal(BI.TEN.pow(p).subtract(BI.ONE).toString(),scale+1)
        else Decimal(r.subtract(BI.ONE).toString(),scale)
    val center = Decimal(y.c,scale); val next = Decimal(r.add(BI.ONE).toString(),scale)
    val middle = cmp(x,squared(center)); require(middle != 0)
    if (mode == "UP" || mode == "CEILING") { require(cmp(x,squared(previous)) > 0 && middle < 0); return "UP_ADJACENT" }
    if (mode == "DOWN" || mode == "FLOOR") { require(middle > 0 && cmp(x,squared(next)) < 0); return "DOWN_ADJACENT" }
    require(mode in listOf("HALF_UP","HALF_DOWN","HALF_EVEN"))
    fun midpoint(a: Decimal,b: Decimal): Int {
        val t = maxOf(a.s,b.s); val da = t-a.s; val db = t-b.s
        require(da in 0L..1L && db in 0L..1L)
        val sum = BI(a.c).multiply(BI.TEN.pow(da.toInt())).add(BI(b.c).multiply(BI.TEN.pow(db.toInt())))
        require(sum.toString().length <= 122)
        return cmp(Decimal(BI(x.c).multiply(BI.valueOf(4)).toString(),x.s), Decimal(sum.multiply(sum).toString(),2L*t))
    }
    val lo = midpoint(previous,center); val hi = midpoint(center,next); val even = !r.testBit(0)
    val lowerTie = when(mode) { "HALF_UP" -> true; "HALF_DOWN" -> false; else -> decade || even }
    val upperTie = when(mode) { "HALF_UP" -> false; "HALF_DOWN" -> true; else -> even }
    require((lo>0 || lo==0 && lowerTie) && (hi<0 || hi==0 && upperTie))
    return "NEAREST_ADJACENT/$lo/$hi/decade=$decade"
}

data class Spec(val owner: String,val profile: String,val source: String,val keys: Set<String>)
val specs = linkedMapOf<String,Spec>()
val compactKeys = (0 until 81).flatMap { a -> (0 until 32).map { k -> (a*1719+1623+3*k).toString() } }.toSet()
specs["portable"] = Spec("PortableOracleTest","PORTABLE","FROZEN",raw.keys)
specs["scale-oracle-0"] = Spec("ScaleBoundaryOracleTest","COMPACT","FROZEN",compactKeys)
for (batch in 0..9) {
    val offset = 139239+batch*6800
    specs["scale-oracle-$offset"] = Spec("ScaleBoundaryOracleTest","WIDE","FROZEN",
        (batch*200 until (batch+1)*200).flatMap { i -> (0 until 8).map { j -> (139239+i*34+5+4*j).toString() } }.toSet())
}
specs["fixed-roots-powers"] = Spec("DecimalConformanceTest","FIXED","LIVE_JDK",
    fixedInputs.flatMap { x -> fixedP.flatMap { p -> modes.map { mode -> "$x/$p/$mode" } } }.toSet())
specs["live-decimals"] = Spec("JdkOracleTest","LIVE","LIVE_JDK",(0 until 350).map { it.toString() }.toSet())
specs["scale-live-compact"] = Spec("ScaleBoundaryConformanceTest","COMPACT","LIVE_JDK",compactKeys.map { (it.toInt()+1).toString() }.toSet())
specs["scale-live-wide"] = Spec("ScaleBoundaryConformanceTest","WIDE","LIVE_JDK",
    (0 until 2000).flatMap { i -> (0 until 8).map { j -> (i*34+6+4*j).toString() } }.toSet())
check(specs.values.sumOf { it.keys.size } == 38497)
check(specs.values.filter { it.source == "FROZEN" }.sumOf { it.keys.size } == 18595)

data class Row(val fields: List<String?>)
fun cell(r: Row,name: String): String? = r.fields[columns.indexOf(name).also { check(it >= 0) }]
fun value(r: Row,name: String) = requireNotNull(cell(r,name)) { "Missing $name" }
fun fields(r: Row,prefix: String): Fields? {
    val kind = value(r,"${prefix}Kind"); require(kind in setOf("VALUE","ERROR","UNAVAILABLE"))
    val c = cell(r,"${prefix}Coefficient"); val s = cell(r,"${prefix}Scale"); val p = cell(r,"${prefix}Precision")
    val text = cell(r,"${prefix}Text"); val error = cell(r,"${prefix}ErrorType")
    if (kind != "VALUE") {
        require(c == null && s == null && p == null)
        if (kind == "ERROR") require(error == "ArithmeticException" && text == "!ArithmeticException")
        else require(text == null && error == null)
        return null
    }
    require(error == null)
    val f = Fields(requireNotNull(c),requireNotNull(s).toInt(),requireNotNull(p).toInt(),requireNotNull(text))
    require(f.p == f.c.removePrefix("-").length && canonical(f.c,f.s) == f.text)
    return f
}
fun exactCohort(x: Decimal,f: Fields,p: Int,inputScale: Int) {
    require(f.c != "0" && !f.c.startsWith('-') && cmp(squared(Decimal(f.c,f.s.toLong())),x)==0)
    if (p>0) require(f.p<=p)
    val preferred = inputScale/2
    if (f.s<preferred) require(p>0 && f.p==p) // Otherwise one more zero is closer.
    if (f.s>preferred) require(!f.c.endsWith('0')) // Otherwise stripping is closer.
}

// Check input/key/source binding independently of any production/helper call.
fun inputAndKey(r: Row,spec: Spec): String? {
    val name=value(r,"name"); val key=value(r,"key"); require(key in spec.keys)
    require(value(r,"profile")==spec.profile && value(r,"referenceSource")==spec.source)
    val c=value(r,"inputCoefficient"); val s=value(r,"inputScale").toInt(); val p=value(r,"p").toInt(); val mode=value(r,"mode")
    require(mode in modes && Regex("-?(?:0|[1-9][0-9]*)").matches(c) && c!="-0")
    val source=parseText(value(r,"inputText"),false); require(source.c==c && source.s==s)
    when(spec.profile) {
        "PORTABLE" -> { val literal=raw.getValue(key)
            require(value(r,"inputText")==literal[0] && p==literal[1].toInt() && mode==literal[2] && value(r,"referenceText")==literal[3]) }
        "COMPACT" -> {
            val common=key.toInt()-(if(name=="scale-live-compact")1 else 0)
            val a=common/1719; val rest=common%1719-1623; require(rest>=0 && rest%3==0)
            val k=rest/3; require(a in 0..80 && k in 0..31)
            require(c==compactCoefficients[a/9] && s==compactScales[a%9] && p==compactP[k/8] && mode==modes[k%8])
            return "COMPACT/$common"
        }
        "WIDE" -> {
            val common=if(name=="scale-live-wide")key.toInt()+139238 else key.toInt()
            val offset=common-139239; val i=offset/34; val rest=offset%34-5
            require(i in 0..1999 && rest>=0 && rest%4==0 && rest/4 in 0..7)
            require(p==wideP[rest/4] && s in wideScales && c.removePrefix("-").length<=155)
            return "WIDE/$common"
        }
        "FIXED" -> require(value(r,"inputText") in fixedInputs && p in fixedP && key=="${value(r,"inputText")}/$p/$mode")
        "LIVE" -> require(key.toInt() in 0..349 && s in -30..44 && c.removePrefix("-").length<=46 && p in 0..34)
        else -> error("Unknown profile")
    }
    return null
}

fun auditMath(r: Row) {
    require(cell(r,"guardFailure")==null && value(r,"strictPass")=="true")
    require(value(r,"referenceUnexpected")=="false" && value(r,"actualUnexpected")=="false")
    val ref=fields(r,"reference"); val required=fields(r,"required"); val actual=fields(r,"actual"); val control=fields(r,"control563")
    require(required==actual && value(r,"requiredKind")==value(r,"actualKind") && cell(r,"requiredText")==cell(r,"actualText") &&
        cell(r,"requiredErrorType")==cell(r,"actualErrorType"))
    require(value(r,"rawTextEquality")==(cell(r,"referenceText")==cell(r,"actualText")).toString())
    for(prefix in listOf("reference","actual")) if(cell(r,"${prefix}ErrorType")==null) require(cell(r,"${prefix}ErrorMessage")==null)
    val c=value(r,"inputCoefficient"); val s=value(r,"inputScale").toInt(); val p=value(r,"p").toInt(); val mode=value(r,"mode")
    val x=Decimal(c,s.toLong()); val disposition=value(r,"disposition"); val proof=value(r,"certificate")
    if(disposition=="EXISTING_563") {
        require(ref==null && value(r,"referenceKind")=="ERROR" && proof=="EXISTING_LITERAL_POLICY")
        require(p==0 || mode=="UNNECESSARY")
        val zeros=c.length-c.trimEnd('0').length
        require(!c.startsWith('-') && c!="0" && s.toLong()-zeros<Int.MIN_VALUE.toLong())
        exactCohort(x,requireNotNull(required),p,s)
        if(value(r,"name").startsWith("scale-live-")) {
            require(control==required && value(r,"control563Unexpected")=="false" && cell(r,"control563ErrorMessage")==null)
        } else require(value(r,"control563Kind")=="UNAVAILABLE" && cell(r,"control563Unexpected")==null && cell(r,"control563ErrorMessage")==null)
        return
    }
    require(control==null && value(r,"control563Kind")=="UNAVAILABLE" && cell(r,"control563Unexpected")==null && cell(r,"control563ErrorMessage")==null)
    when(disposition) {
        "UNCHANGED_ERROR" -> require(ref==null && required==null && value(r,"referenceKind")=="ERROR" &&
            value(r,"requiredKind")=="ERROR" && cell(r,"referenceErrorType")==cell(r,"requiredErrorType") &&
            cell(r,"referenceText")==cell(r,"requiredText") && proof=="EXISTING_ERROR_COMPARISON")
        "UNCHANGED_ZERO" -> {
            require(c=="0" && ref==required && required!=null && required.c=="0" && required.s==s/2 && required.p==1 && proof=="ZERO") }
        "UNCHANGED_EXACT" -> {
            require(ref==required && required!=null && proof=="REFERENCE_SQUARE_EQUALS_INPUT")
            require(p==0 || mode=="UNNECESSARY" || required.p<p)
            if(p==0) require(required.p<=78)
            exactCohort(x,required,p,s) }
        "UNCHANGED_P_DIGITS" -> require(ref==required && required!=null && !c.startsWith('-') && c!="0" &&
            required.c!="0" && !required.c.startsWith('-') && p>0 && required.p==p && mode!="UNNECESSARY" && proof=="EXISTING_NUMERIC_REFERENCE")
        "LIFT_INEXACT" -> {
            require(ref!=null && required!=null && !c.startsWith('-') && c!="0" && ref.c!="0" && !ref.c.startsWith('-'))
            require(p in 1..120 && mode!="UNNECESSARY" && ref.p<p)
            val pad=p-ref.p; val scale=ref.s.toLong()+pad
            require(scale in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
            require(required.c==ref.c+"0".repeat(pad) && required.s==scale.toInt() && required.p==p)
            require(cmp(squared(Decimal(ref.c,ref.s.toLong())),x)!=0)
            val independentlyChecked = certificate(x,required,p,mode)
            require(matchesCertificate(independentlyChecked,proof)) {
                "Certificate sign/kind/decade mismatch: expected=$independentlyChecked recorded=$proof"
            }
            if(value(r,"profile")=="PORTABLE") { val literal=raw.getValue(value(r,"key")); require(required.c==literal[4] && required.s==literal[5].toInt()) }
        }
        else -> error("Unrecognized/failing disposition $disposition")
    }
}

// Output is created only after source/tool/runtime checks; this script never
// writes the validation root, supplied XML, old capsules or existing outputs.
require(Files.isDirectory(output.parent) && output.parent.toRealPath()==output.parent)
Files.createDirectory(output)
fun writer(name: String) = Files.newBufferedWriter(output.resolve(name),Charsets.UTF_8)
val rawOut=writer("observations.tsv")
val recordsOut=writer("records.tsv")
val changesOut=writer("changes-errors.tsv")
val issuesOut=writer("issues.tsv")
val provenanceOut=writer("xml-provenance.tsv")
val summariesOut=writer("recorder-summaries.tsv")
val targetOut=writer("targets.tsv")
fun write(out: java.io.BufferedWriter,items: List<String?>) { out.write(items.joinToString("\t",transform=::esc)); out.newLine() }
write(rawOut,listOf("target","xml","xmlSystemLine","channel","originalDecodedXmlLine"))
for(out in listOf(recordsOut,changesOut)) write(out,listOf("target","xml","xmlSystemLine")+columns)
write(issuesOut,listOf("target","xml","line","issue"))
write(provenanceOut,listOf("target","xml","size","beforeSHA256","parsedSHA256","afterSHA256"))
write(summariesOut,listOf("target","name","profile","expected","seen","schemas","summaries","computedDispositions","producerDispositions"))
write(targetOut,listOf("target","state","expectedRecords","seenRecords","expectedRecorders","completeRecorders","issues"))
Files.writeString(output.resolve("runtime.txt"),"java=${System.getProperty("java.runtime.version")}\nkotlin=${KotlinVersion.CURRENT}\nflags=$flags\ncache=$cache\nscript=$script\nscriptSHA256=$scriptPin\nmanifestSHA256=$manifestPin\nbuildSHA256=$buildPin\n")
var issues=0
fun issue(target: String,file: String,line: Long,message: String) { issues++; write(issuesOut,listOf(target,file,line.toString(),message)) }
data class State(val seen: MutableSet<String> = linkedSetOf(),val histogram: MutableMap<String,Int> = linkedMapOf(),
    var schemas: Int=0,var summaries: Int=0,var producerHistogram: String?=null)
val crossInputs=linkedMapOf<String,String>() // Only JVM frozen-profile inputs, no rounded numeric oracle.
val crossSeen=linkedMapOf<String,MutableSet<String>>()
val targetStatuses=linkedMapOf<String,String>()
fun parseHistogram(text: String): Map<String,Int> {
    require(text.startsWith('{') && text.endsWith('}'))
    val result=linkedMapOf<String,Int>()
    if(text!="{}") for(part in text.substring(1,text.length-1).split(", ")) {
        val m=requireNotNull(Regex("([A-Z_0-9]+)=([1-9][0-9]*)").matchEntire(part))
        require(result.put(m.groupValues[1],m.groupValues[2].toInt())==null)
    }
    return result
}
try {
    for(target in targets) {
        val startIssues=issues
        val wanted=specs.filterValues { target=="jvmTest" || it.source=="FROZEN" }
        val states=wanted.mapValues { State() }
        val expectedTotal=if(target=="jvmTest")38497 else 18595
        val dir=selected[target]
        if(dir==null) {
            issue(target,"",0,"UNRUN: target not supplied")
            targetStatuses[target]="UNRUN"
            write(targetOut,listOf(target,"UNRUN",expectedTotal.toString(),"0",wanted.size.toString(),"0",(issues-startIssues).toString()))
            continue
        }
        require(dir==root.resolve("build/test-results/$target").normalize() && dir.startsWith(root)) { "XML directory symlink/ownership changed" }
        val xmls=Files.list(dir).use { stream -> stream.filter { it.fileName.toString().endsWith(".xml") }.toList() }
            .sortedWith(compareBy<Path> { file ->
                val short=file.fileName.toString().removeSuffix(".xml").substringAfterLast('.')
                owners.indexOf(short).let { if(it<0)owners.size else it }
            }.thenBy { it.fileName.toString() })
        val expectedOwners=if(target=="jvmTest")owners.toSet() else owners.take(2).toSet()
        val foundOwners=mutableSetOf<String>()
        val crossTarget=linkedSetOf<String>(); crossSeen[target]=crossTarget
        for(file in xmls) {
            val rel=root.relativize(file).toString()
            try {
                require(Files.isRegularFile(file,NOFOLLOW_LINKS) && !Files.isSymbolicLink(file) && file.toRealPath()==file)
                val rawName=file.fileName.toString().removePrefix("TEST-").removeSuffix(".xml")
                require(file.fileName.toString().startsWith("TEST-"))
                val ownerName=rawName.removePrefix("$target.")
                val owner=ownerName.substringAfterLast('.')
                val owned=owner in expectedOwners && ownerName=="io.heapy.krogu.math.$owner"
                if(owned) require(foundOwners.add(owner)) { "Duplicate owned XML $owner" }
                val before=hashFile(file); val size=Files.size(file)
                var suiteCount=0; var cases=0; var suiteTests=-1
                var suite: String?=null; var testcase: String?=null
                val actualMethods=mutableSetOf<String>()
                var outCount=0; var channel: String?=null; var xmlLine=0L
                var pendingSchema=false; var active: String?=null
                fun consume(line: String,where: String,terminated: Boolean) {
                    if(!line.startsWith("587_")) return
                    write(rawOut,listOf(target,rel,xmlLine.toString(),where,line))
                    try {
                        require(terminated) { "Relevant unterminated/truncated line" }
                        require(owned && where=="system-out") { "Ledger/failure tag in wrong XML owner/channel" }
                        val split=line.split('\t'); val tag=split.first(); val cells=split.drop(1).map(::decode)
                        when(tag) {
                            "587_SQRT_SCHEMA" -> {
                                require(cells==columns && !pendingSchema && active==null) { "Schema bytes/multiplicity/order mismatch" }
                                pendingSchema=true
                            }
                            "587_SQRT_RECORD" -> {
                                // Preserve the complete actual encoded row before classifying it.
                                recordsOut.write(listOf(target,rel,xmlLine.toString()).joinToString("\t",transform=::esc)+"\t"+line.substringAfter('\t')+"\n")
                                require(cells.size==44)
                                val row=Row(cells); val name=value(row,"name")
                                val spec=requireNotNull(wanted[name]) { "Unknown/unselected recorder $name" }
                                require(spec.owner==owner && pendingSchema)
                                val state=states.getValue(name)
                                if(active==null) { active=name; state.schemas++ } else require(active==name) { "Interleaved recorder without schema" }
                                val key=value(row,"key"); require(state.seen.add(key)) { "Duplicate $name/$key" }
                                val disposition=value(row,"disposition")
                                state.histogram[disposition]=(state.histogram[disposition] ?:0)+1
                                if(listOf("Kind","Text","Coefficient","Scale","Precision","ErrorType").any {
                                        cell(row,"reference$it")!=cell(row,"actual$it") } || disposition=="LIFT_INEXACT" ||
                                    value(row,"referenceKind")=="ERROR" || value(row,"actualKind")=="ERROR" ||
                                    cell(row,"guardFailure")!=null || cell(row,"strictPass")!="true") {
                                    changesOut.write(listOf(target,rel,xmlLine.toString()).joinToString("\t",transform=::esc)+"\t"+line.substringAfter('\t')+"\n")
                                }
                                val logical=inputAndKey(row,spec)
                                if(logical!=null) {
                                    val signature=listOf(value(row,"inputCoefficient"),value(row,"inputScale"),value(row,"p"),value(row,"mode")).joinToString("/")
                                    if(target=="jvmTest" && name.startsWith("scale-oracle-")) {
                                        require(crossInputs.put(logical,signature)==null) { "Duplicate frozen JVM cross key" }
                                    } else {
                                        require(crossInputs[logical]==signature) { "Cross-profile/target input-context mismatch $logical" }
                                        require(crossTarget.add(logical)) { "Repeated cross-check $logical" }
                                    }
                                }
                                auditMath(row)
                            }
                            "587_SQRT_SUMMARY" -> {
                                require(cells.size==6)
                                val name=requireNotNull(cells[0]); val spec=requireNotNull(wanted[name]); val state=states.getValue(name)
                                require(spec.owner==owner && pendingSchema && active==name && cells[1]==spec.profile)
                                state.summaries++; state.producerHistogram=cells[5]
                                require(state.summaries==1 && cells[2]==spec.keys.size.toString() && cells[3]==spec.keys.size.toString() && cells[4]=="0")
                                require(state.seen==spec.keys && parseHistogram(requireNotNull(cells[5]))==state.histogram)
                                active=null; pendingSchema=false
                            }
                            "587_COMPARISON_FAILURE", "587_PORTABLE_OTHER_FAILURE", "587_SCALE_OTHER_FAILURE" -> error("Non-sqrt deferred assertion failure")
                            else -> error("Unknown/truncated587 tag: $tag")
                        }
                    } catch(e: Exception) { issue(target,rel,xmlLine,"${e::class.java.simpleName}: ${e.message}") }
                }
                // Retain only one relevant line. Unrelated arbitrarily long text
                // is discarded after its first4 chars. No entire system-out DOM.
                val line=StringBuilder(); var ignored=false; var tooLong=false
                fun append(chars: CharArray,start: Int,length: Int) {
                    for(i in start until start+length) {
                        val c=chars[i]
                        if(c=='\n') {
                            xmlLine++
                            if(!ignored) {
                                if(tooLong) issue(target,rel,xmlLine,"Relevant line exceeds65536 chars; original XML retained, incomplete ledger")
                                else consume(line.toString(),requireNotNull(channel),true)
                            }
                            line.setLength(0); ignored=false; tooLong=false
                        } else if(!ignored && !tooLong) {
                            line.append(c)
                            if(line.length==4 && line.toString()!="587_") { ignored=true; line.setLength(0) }
                            if(line.length>65536) { tooLong=true; line.setLength(0) }
                        }
                    }
                }
                val factory=SAXParserFactory.newInstance()
                factory.isNamespaceAware=false; factory.isValidating=false
                for((feature,on) in listOf(XMLConstants.FEATURE_SECURE_PROCESSING to true,
                    "http://apache.org/xml/features/disallow-doctype-decl" to true,
                    "http://xml.org/sax/features/external-general-entities" to false,
                    "http://xml.org/sax/features/external-parameter-entities" to false,
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd" to false)) factory.setFeature(feature,on)
                val reader=factory.newSAXParser().xmlReader
                reader.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD,""); reader.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"")
                reader.entityResolver=org.xml.sax.EntityResolver { _,_->throw SAXException("External entity forbidden") }
                reader.contentHandler=object: DefaultHandler() {
                    override fun startElement(uri: String?,localName: String?,qName: String,a: Attributes) {
                        fun required(name: String)=requireNotNull(a.getValue(name)) { "Missing XML $name" }
                        when(qName) {
                            "testsuite" -> {
                                require(suite==null && ++suiteCount==1)
                                val suiteName=required("name")
                                if(owned) require(suiteName==if(target=="jvmTest")"$owner[jvm]" else rawName)
                                suite=suiteName; suiteTests=required("tests").toInt()
                                for(count in listOf("skipped","failures","errors")) if(required(count)!="0") issue(target,rel,0,"Nonzero suite $count")
                                a.getValue("disabled")?.let { require(it=="0") }
                            }
                            "testcase" -> {
                                require(suite!=null && testcase==null && required("classname")==rawName)
                                testcase=required("name"); cases++
                                a.getValue("status")?.let { require(it in setOf("run","passed")) }
                                if(owned) {
                                    val rawMethod=requireNotNull(testcase); val suffix=suffixes.getValue(target)
                                    require(rawMethod.endsWith(suffix))
                                    require(actualMethods.add(rawMethod.removeSuffix(suffix))) { "Duplicate method" }
                                }
                            }
                            "skipped","failure","error" -> issue(target,rel,0,"Nonpassing testcase child $qName/$testcase")
                            "system-out","system-err" -> {
                                require(suite!=null && testcase==null && channel==null)
                                channel=qName; if(qName=="system-out") outCount++
                                line.setLength(0); ignored=false; tooLong=false
                            }
                            else -> if(channel!=null) error("Element inside text output")
                        }
                    }
                    override fun characters(ch: CharArray,start: Int,length: Int) { if(channel!=null) append(ch,start,length) }
                    override fun endElement(uri: String?,localName: String?,qName: String) {
                        when(qName) {
                            "system-out","system-err" -> {
                                require(channel==qName)
                                if(!ignored && (line.isNotEmpty() || tooLong)) {
                                    xmlLine++
                                    if(tooLong) issue(target,rel,xmlLine,"Unterminated oversized relevant line")
                                    else consume(line.toString(),qName,false)
                                }
                                channel=null; line.setLength(0)
                            }
                            "testcase" -> { require(testcase!=null); testcase=null }
                            "testsuite" -> { require(testcase==null && channel==null); suite=null }
                        }
                    }
                }
                reader.errorHandler=object:DefaultHandler() {
                    override fun error(e: org.xml.sax.SAXParseException) { throw e }
                    override fun fatalError(e: org.xml.sax.SAXParseException) { throw e }
                }
                val digest=MessageDigest.getInstance("SHA-256")
                java.security.DigestInputStream(Files.newInputStream(file),digest).use { reader.parse(InputSource(it)) }
                val parsed=digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
                val after=hashFile(file)
                write(provenanceOut,listOf(target,rel,size.toString(),before,parsed,after))
                require(before==parsed && before==after && Files.size(file)==size) { "XML changed during audit" }
                require(suiteCount==1 && suite==null && cases==suiteTests && active==null && !pendingSchema)
                if(owned) require(actualMethods==methodSets.getValue(owner) && outCount==1) { "Owned method/output inventory mismatch" }
            } catch(e: Exception) { issue(target,rel,0,"INCOMPLETE XML: ${e::class.java.simpleName}: ${e.message}") }
        }
        if(foundOwners!=expectedOwners) issue(target,"",0,"Missing owned XML: ${expectedOwners-foundOwners}")
        for((name,spec) in wanted) {
            val state=states.getValue(name)
            write(summariesOut,listOf(target,name,spec.profile,spec.keys.size.toString(),state.seen.size.toString(),
                state.schemas.toString(),state.summaries.toString(),state.histogram.toString(),state.producerHistogram))
            if(state.seen!=spec.keys || state.schemas!=1 || state.summaries!=1)
                issue(target,"",0,"Incomplete $name: missing=${spec.keys-state.seen}, extra=${state.seen-spec.keys}, schema=${state.schemas}, summary=${state.summaries}")
        }
        if(crossTarget.size!=18592) issue(target,"",0,"Cross-profile input count ${crossTarget.size}, expected18592")
        val seen=states.values.sumOf { it.seen.size }
        if(seen!=expectedTotal) issue(target,"",0,"Total record count $seen !=$expectedTotal")
        val status=if(issues==startIssues)"COMPLETE" else"FAILED_OR_PARTIAL"
        targetStatuses[target]=status
        write(targetOut,listOf(target,status,expectedTotal.toString(),seen.toString(),wanted.size.toString(),
            states.values.count { it.schemas==1 && it.summaries==1 }.toString(),(issues-startIssues).toString()))
    }
    try { verifySource() } catch(e: Exception) { issue("ALL","",0,"POST provenance failure: ${e.message}") }
    if(crossInputs.size!=18592) issue("ALL","",0,"Frozen JVM cross-input inventory ${crossInputs.size} !=18592")
    val passed=targets.all { targetStatuses[it]=="COMPLETE" } && issues==0 && crossInputs.size==18592
    Files.writeString(output.resolve("RESULT.txt"),"status=${if(passed)"PASS" else"FAILED_OR_UNRUN"}\nissues=$issues\ntargets=$targetStatuses\nsource=$manifestPin\n"+
        "Scope=ledger completeness/strict fields/bounded independent changed-candidate certificates; no full-domain or JCK claim.\n")
    println("587_LEDGER_AUDIT\t${if(passed)"PASS" else"FAILED_OR_UNRUN"}\tissues=$issues\ttargets=$targetStatuses")
} finally {
    for(out in listOf(rawOut,recordsOut,changesOut,issuesOut,provenanceOut,summariesOut,targetOut)) out.close()
}
if(issues!=0 || targets.any { targetStatuses[it]!="COMPLETE" }) exitProcess(1)

#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// SOURCE ONLY. No execution grant is implied by this prepared driver.
// campaign and child modes share exactly this source/cache key; no dependencies.
@file:CompilerOptions("-jvm-target", "25")

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.management.ManagementFactory
import java.math.BigInteger as HostInteger
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Instant
import java.util.Random
import java.util.concurrent.TimeUnit

val enteredNano = System.nanoTime()
val enteredEpoch = System.currentTimeMillis()
val mode = (if (args.firstOrNull() == "--check") "check" else args.firstOrNull()).also { require(it in setOf("campaign", "child", "check")) }
val commonKeys = setOf("--grant", "--ledger", "--ledger-sha", "--driver", "--driver-sha", "--out")
val requiredKeys = commonKeys + when (mode) {
    "campaign" -> setOf("--launch-epoch-ms")
    "child" -> setOf("--variant", "--fork", "--cache", "--campaign-deadline-epoch-ms")
    else -> emptySet()
}
require(args.drop(1).size == requiredKeys.size * 2) { "Pass exactly $requiredKeys as key/value pairs" }
val optionPairs = args.drop(1).chunked(2).map { it[0] to it[1] }
require(optionPairs.map { it.first }.toSet() == requiredKeys && optionPairs.map { it.first }.distinct().size == optionPairs.size)
val options = optionPairs.toMap()
fun arg(name: String): String = options.getValue(name)
require(arg("--grant").startsWith("parent:") && arg("--grant").length > 7)
require(options.values.none { it.contains("PLACEHOLDER") || it.contains('<') || it.contains('\n') || it.contains('\t') })
fun sha(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes)
    .joinToString("") { "%02x".format(it.toInt() and 255) }
fun fileSha(path: Path): String = Files.newInputStream(path).use { input ->
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(65536)
    while (true) { val count = input.read(buffer); if (count < 0) break; digest.update(buffer, 0, count) }
    digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
}
val hashPattern = Regex("[0-9a-f]{64}")
fun exactPath(text: String): Path {
    val path = Path.of(text)
    require(path.isAbsolute && path.normalize() == path && path.toRealPath() == path) { "Canonical existing absolute path required: $text" }
    return path
}
val driver = exactPath(arg("--driver"))
val driverSha = arg("--driver-sha").also { require(hashPattern.matches(it)) }
val ledger = exactPath(arg("--ledger"))
val ledgerSha = arg("--ledger-sha").also { require(hashPattern.matches(it)) }
check(fileSha(driver) == driverSha && fileSha(ledger) == ledgerSha)
val ledgerLines = Files.readAllLines(ledger)
require(ledgerLines.isNotEmpty() && ledgerLines.all { it.split('\t').size == 2 })
val ledgerPairs = ledgerLines.map { val p = it.split('\t'); p[0] to p[1] }
require(ledgerPairs.map { it.first }.distinct().size == ledgerPairs.size)
val provenance = ledgerPairs.toMap()
val ledgerKeys = setOf("schema", "java.home", "kotlin.home", "build.contract", "source.delta", "source.delta.sha") +
    listOf("baseline", "candidate").flatMap { v -> listOf("$v.jar", "$v.jar.sha", "$v.source", "$v.inputs.before", "$v.inputs.after", "$v.inputs.sha", "$v.build.log", "$v.build.log.sha") }
require(provenance.keys == ledgerKeys && provenance.values.none { it.contains("PLACEHOLDER") || it.contains('<') })
check(provenance.getValue("schema") == "krogu-530-matched-v1")
check(provenance.getValue("build.contract") == "fresh-jvmJar-rerun-tasks-pinned-toolchain")
val javaHome = exactPath(provenance.getValue("java.home"))
val kotlinHome = exactPath(provenance.getValue("kotlin.home"))
check(javaHome == Path.of("/Users/yoda/.sdkman/candidates/java/25.0.4-librca").toRealPath())
check(kotlinHome == Path.of("/Users/yoda/.sdkman/candidates/kotlin/2.4.21").toRealPath())
val vmFlags = listOf("-Xms32m", "-Xmx256m", "-XX:+UseSerialGC", "-XX:ActiveProcessorCount=1", "-XX:-OmitStackTraceInFastThrow")
val runnerFlags = listOf("--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow", "-Dkotlin.home=$kotlinHome")
check(ManagementFactory.getRuntimeMXBean().inputArguments == vmFlags + runnerFlags) { "Unexpected JVM arguments" }
check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS")
check(System.getProperty("java.vendor").contains("BellSoft"))
check(Path.of(System.getProperty("java.home")).toRealPath() == javaHome)
check(System.getProperty("java.class.path") == kotlinHome.resolve("lib/kotlin-runner.jar").toString())
check(KotlinVersion.CURRENT.toString() == "2.4.21")
check(Runtime.getRuntime().maxMemory() in 200L * 1024 * 1024..256L * 1024 * 1024)
check(System.getenv("JAVA_OPTS") == vmFlags.joinToString(" "))
for (key in listOf("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "KOTLIN_OPTS", "JAVACMD"))
    check(System.getenv(key).isNullOrEmpty()) { "Injected $key" }
val baselineBigInteger = "ff80e3cd25c162daed68e5723bd1bbfb277188e57594010b589b1ec6e038cae6"
val candidateBigInteger = "d0e757ebd5955604d5fb66d8dda13b1c15b2ecb300a37a5fbcb1fcfaeb65c16f"
val bigIntegerPath = "src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt"
val jars = listOf("baseline", "candidate").associateWith { exactPath(provenance.getValue("$it.jar")) }
val jarHashes = listOf("baseline", "candidate").associateWith { provenance.getValue("$it.jar.sha").also { value -> require(hashPattern.matches(value)) } }
require(jars.getValue("baseline") != jars.getValue("candidate") && jarHashes.getValue("baseline") != jarHashes.getValue("candidate"))
val filePins = linkedMapOf(driver to driverSha, ledger to ledgerSha)
for (v in listOf("baseline", "candidate")) {
    filePins[jars.getValue(v)] = jarHashes.getValue(v)
    val before = exactPath(provenance.getValue("$v.inputs.before"))
    val after = exactPath(provenance.getValue("$v.inputs.after"))
    require(before != after)
    val hash = provenance.getValue("$v.inputs.sha").also { require(hashPattern.matches(it)) }
    filePins[before] = hash; filePins[after] = hash
    filePins[exactPath(provenance.getValue("$v.build.log"))] = provenance.getValue("$v.build.log.sha").also { require(hashPattern.matches(it)) }
}
val delta = exactPath(provenance.getValue("source.delta"))
filePins[delta] = provenance.getValue("source.delta.sha").also { require(hashPattern.matches(it)) }
require(filePins.size == 11) { "Distinct driver, ledger, JARs, manifests, build logs and delta paths required" }
fun checkFiles() { for ((path, expected) in filePins) check(fileSha(path) == expected) { "Pin changed: $path" } }
checkFiles()
check(Files.readAllLines(delta) == listOf("schema\tkrogu-530-source-delta-v1", "changed\t$bigIntegerPath\t$baselineBigInteger\t$candidateBigInteger"))

// Strict manifests bind all src/, gradle/, .github/ and lock-store files plus root build inputs.
// They prove a declared file/JAR association, not that a build log is independently truthful.
val rootBuildInputs = listOf("build.gradle.kts", "settings.gradle.kts", "gradle.properties", "gradlew", "gradlew.bat")
fun sourceManifest(v: String): Map<String, String> {
    val root = exactPath(provenance.getValue("$v.source"))
    check(!Files.exists(root.resolve("buildSrc"), LinkOption.NOFOLLOW_LINKS))
    val paths = rootBuildInputs.toMutableList()
    Files.list(root).use { entries ->
        check(entries.filter { it.fileName.toString().endsWith(".gradle.kts") }.map { it.fileName.toString() }.sorted().toList() == listOf("build.gradle.kts", "settings.gradle.kts"))
    }
    for (part in listOf("src", "gradle", ".github", "kotlin-js-store")) {
        Files.walk(root.resolve(part)).use { entries -> entries.forEach { path ->
            check(!Files.isSymbolicLink(path)) { "Source symlink: $path" }
            if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) paths += root.relativize(path).toString()
            else check(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) { "Non-regular input: $path" }
        } }
    }
    val expectedPaths = paths.sorted()
    val rows = Files.readAllLines(exactPath(provenance.getValue("$v.inputs.before"))).map { line ->
        require(line.length >= 67 && hashPattern.matches(line.take(64)) && line.substring(64, 66) == "  ")
        val relative = line.substring(66)
        require(relative.isNotBlank() && '\t' !in relative && Path.of(relative).normalize().toString() == relative && !Path.of(relative).isAbsolute)
        relative to line.take(64)
    }
    check(rows.map { it.first } == expectedPaths && expectedPaths.distinct().size == expectedPaths.size)
    for ((relative, hash) in rows) {
        val path = root.resolve(relative)
        check(path.toRealPath() == path && !Files.isSymbolicLink(path) && fileSha(path) == hash) { "Source pin changed: $path" }
    }
    return rows.toMap()
}
fun checkSourcePair() {
    val b = sourceManifest("baseline"); val c = sourceManifest("candidate")
    check(b.keys == c.keys && b.getValue(bigIntegerPath) == baselineBigInteger && c.getValue(bigIntegerPath) == candidateBigInteger)
    check(b.keys.filter { b.getValue(it) != c.getValue(it) } == listOf(bigIntegerPath))
}
// Validate full sources before any numerical class is loaded in a child.
checkSourcePair()
fun emit(line: String) { println(line); System.out.flush() }
val current = ProcessHandle.current()
emit("IDENTITY\tmode=$mode\tpid=${current.pid()}\tstart=${current.info().startInstant().orElseThrow()}\tgrant=${arg("--grant")}")
emit("PROVENANCE\tledger=$ledger\tledgerSha=$ledgerSha\tdriver=$driver\tdriverSha=$driverSha\tbaselineBigInteger=$baselineBigInteger\tcandidateBigInteger=$candidateBigInteger\tsourceBinding=reviewed-ledger-not-bytecode-derivation")
emit("RUNTIME\tjava=${System.getProperty("java.runtime.version")}\tkotlin=${KotlinVersion.CURRENT}\tflags=${ManagementFactory.getRuntimeMXBean().inputArguments}\theap=${Runtime.getRuntime().maxMemory()}\tprocessors=${Runtime.getRuntime().availableProcessors()}\tloadAverage=${ManagementFactory.getOperatingSystemMXBean().systemLoadAverage}")

data class Cohort(val id: String, val a: HostInteger, val b: HostInteger)
data class Work(val id: String, val operation: String, val a: HostInteger, val b: HostInteger)
fun positive(bits: Int, seed: Long): HostInteger = HostInteger(bits, Random(seed)).setBit(bits - 1)
fun workItems(): List<Work> {
    val one = HostInteger.ONE
    val cohorts = mutableListOf(Cohort("one_limb_256_30", positive(256, 53001), HostInteger.valueOf(999999937)))
    for (bits in listOf(256, 1024, 8000)) {
        cohorts += Cohort("balanced_${bits}_${bits - 1}", positive(bits, 53000L + bits), one.shiftLeft(bits - 1).subtract(HostInteger.valueOf(33)))
        val divisorBits = when (bits) { 256 -> 61; 1024 -> 127; else -> 257 }
        cohorts += Cohort("unbalanced_${bits}_$divisorBits", positive(bits, 153000L + bits), positive(divisorBits, 253000L + bits).setBit(0))
    }
    val work = mutableListOf<Work>()
    for (cohort in cohorts) for (op in listOf("divide", "remainder", "divideAndRemainder")) {
        for (negativeA in listOf(false, true)) for (negativeB in listOf(false, true)) {
            val signs = "${if (negativeA) 'n' else 'p'}${if (negativeB) 'n' else 'p'}"
            work += Work("${cohort.id}:$op:$signs", op,
                if (negativeA) cohort.a.negate() else cohort.a, if (negativeB) cohort.b.negate() else cohort.b)
        }
    }
    val commonFactor = HostInteger.valueOf(35)
    val gcdA = positive(256, 53071).multiply(commonFactor)
    val gcdB = positive(127, 53072).multiply(commonFactor)
    for (negativeA in listOf(false, true)) for (negativeB in listOf(false, true)) {
        val signs = "${if (negativeA) 'n' else 'p'}${if (negativeB) 'n' else 'p'}"
        work += Work("gcd_seed256_seed127:gcd:$signs", "gcd", if (negativeA) gcdA.negate() else gcdA, if (negativeB) gcdB.negate() else gcdB)
    }
    val modulus = one.shiftLeft(256).subtract(HostInteger.valueOf(189))
    val inverseBase = (0 until 8).map { positive(255, 53080L + it) }.firstOrNull { it.gcd(modulus) == one }
        ?: error("Fixed inverse cohort did not supply an invertible operand")
    work += Work("inverse_255_256:modInverse:pp", "modInverse", inverseBase, modulus)
    work += Work("inverse_255_256:modInverse:np", "modInverse", inverseBase.negate(), modulus)
    check(work.size == 90 && work.map { it.id }.distinct().size == 90)
    return work
}
fun expected(work: Work): List<ByteArray> = when (work.operation) {
    "divide" -> listOf(work.a.divide(work.b).toByteArray())
    "remainder" -> listOf(work.a.remainder(work.b).toByteArray())
    "divideAndRemainder" -> work.a.divideAndRemainder(work.b).map { it.toByteArray() }
    "gcd" -> listOf(work.a.gcd(work.b).toByteArray())
    "modInverse" -> listOf(work.a.modInverse(work.b).toByteArray())
    else -> error("Unknown operation")
}
data class Gc(val name: String, val count: Long, val millis: Long)
val gcBeans = ManagementFactory.getGarbageCollectorMXBeans().sortedBy { it.name }
fun gc(): List<Gc> = gcBeans.map { Gc(it.name, it.collectionCount, it.collectionTime) }.also { values ->
    check(values.all { it.count >= 0 && it.millis >= 0 && '\t' !in it.name && '\n' !in it.name && ',' !in it.name && ':' !in it.name })
}
fun gcText(values: List<Gc>): String = values.joinToString(",") { "${it.name}:${it.count}:${it.millis}" }
fun host(label: String) {
    val os = ManagementFactory.getOperatingSystemMXBean() as? com.sun.management.OperatingSystemMXBean
    emit("HOST\tlabel=$label\tloadAverage=${ManagementFactory.getOperatingSystemMXBean().systemLoadAverage}\tphysicalBytes=${os?.totalMemorySize}\tfreePhysicalBytes=${os?.freeMemorySize}\ttotalSwapBytes=${os?.totalSwapSpaceSize}\tfreeSwapBytes=${os?.freeSwapSpaceSize}\tprocessCpuNs=${os?.processCpuTime}\tnoUncontendedHostClaim=true")
}

fun runChild() {
    val variant = arg("--variant").also { require(it in setOf("baseline", "candidate")) }
    val fork = arg("--fork").toInt().also { require(it in 1..12) }
    val deadline = arg("--campaign-deadline-epoch-ms").toLong()
    check(System.currentTimeMillis() < deadline)
    val processGcBefore = gc()
    val cache = exactPath(arg("--cache"))
    check(System.getenv("KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR") == cache.toString())
    val work = workItems() // Deterministic public JDK preparation, not measured.
    val oracle = work.associate { it.id to expected(it) }
    val thread = ManagementFactory.getThreadMXBean()
    check(thread.isCurrentThreadCpuTimeSupported)
    if (!thread.isThreadCpuTimeEnabled) thread.isThreadCpuTimeEnabled = true
    val allocations = checkNotNull(thread as? com.sun.management.ThreadMXBean)
    check(allocations.isThreadAllocatedMemorySupported)
    if (!allocations.isThreadAllocatedMemoryEnabled) allocations.isThreadAllocatedMemoryEnabled = true
    val tid = Thread.currentThread().threadId()
    val jar = jars.getValue(variant)
    emit("CHILD\tvariant=$variant\tfork=$fork\tjar=$jar\tsha=${jarHashes.getValue(variant)}\tcases=90\twarmupCalls=2\tsamples=3\tcallsPerSample=2\tcache=$cache")
    var rows = 0
    URLClassLoader(arrayOf(jar.toUri().toURL()), this.javaClass.classLoader).use { loader ->
        val cls = Class.forName("io.heapy.krogu.math.BigInteger", true, loader)
        check(cls.classLoader === loader && Path.of(cls.protectionDomain.codeSource.location.toURI()).toRealPath() == jar)
        val construct = cls.getConstructor(ByteArray::class.java)
        val export = cls.getMethod("toByteArray")
        val methods = work.map { it.operation }.distinct().associateWith { cls.getMethod(it, cls) }
        emit("LOADED\tvariant=$variant\tclass=${cls.name}\torigin=$jar\tsha=${jarHashes.getValue(variant)}")
        for (item in work) {
            check(System.currentTimeMillis() < deadline)
            val a = construct.newInstance(item.a.toByteArray())
            val b = construct.newInstance(item.b.toByteArray())
            // No Method.invoke in the measured loop. Lookup, binding and adaptation are setup.
            val invocation: MethodHandle = MethodHandles.publicLookup().unreflect(methods.getValue(item.operation))
                .bindTo(a).bindTo(b).asType(MethodType.methodType(Any::class.java))
            val expected = oracle.getValue(item.id)
            val operandHash = sha(("${item.operation}\n${item.a}\n${item.b}\n").toByteArray(Charsets.UTF_8))
            val expectedHash = sha(expected.joinToString("|") { bytes -> bytes.joinToString("") { "%02x".format(it.toInt() and 255) } }.toByteArray(Charsets.UTF_8))
            emit("CASE\tvariant=$variant\tfork=$fork\tid=${item.id}\toperandSha=$operandHash\toracleSha=$expectedHash\taBits=${item.a.abs().bitLength()}\tbBits=${item.b.abs().bitLength()}")
            val results = arrayOfNulls<Any>(2) // Output retention allocated outside every interval.
            for (sample in -1..2) {
                results.fill(null)
                val beforeGc = gc()
                val beforeBytes = allocations.getThreadAllocatedBytes(tid)
                val beforeCpu = thread.currentThreadCpuTime
                val beforeWall = System.nanoTime()
                for (call in 0..1) results[call] = invocation.invokeExact() as Any
                val wall = System.nanoTime() - beforeWall
                val cpu = thread.currentThreadCpuTime - beforeCpu
                val bytes = allocations.getThreadAllocatedBytes(tid) - beforeBytes
                val afterGc = gc()
                check(beforeBytes >= 0 && wall >= 0 && cpu >= 0 && bytes >= 0)
                check(beforeGc.map { it.name } == afterGc.map { it.name })
                val gcCount = afterGc.indices.sumOf { afterGc[it].count - beforeGc[it].count }
                val gcMillis = afterGc.indices.sumOf { afterGc[it].millis - beforeGc[it].millis }
                check(gcCount >= 0 && gcMillis >= 0)
                // Public exports/reflection/JDK equality checks and log formatting are outside counters.
                for (result in results) {
                    val values = if (item.operation == "divideAndRemainder") (result as Array<*>).toList() else listOf(result)
                    check(values.size == expected.size)
                    for (i in values.indices) {
                        val value = checkNotNull(values[i]); check(cls.isInstance(value))
                        val actual = export.invoke(value) as ByteArray
                        check(actual.contentEquals(expected[i])) { "Wrong result: $variant ${item.id} sample=$sample slot=$i" }
                    }
                }
                emit("SAMPLE\tvariant=$variant\tfork=$fork\tid=${item.id}\tsample=$sample\tcalls=2\twarmup=${sample < 0}\twallNs=$wall\tthreadCpuNs=$cpu\tthreadAllocatedBytes=$bytes\tgcCount=$gcCount\tgcMillis=$gcMillis\tgcBefore=${gcText(beforeGc)}\tgcAfter=${gcText(afterGc)}")
                rows++
            }
        }
    }
    check(rows == 360)
    checkFiles(); checkSourcePair()
    emit("PROCESS_GC\tvariant=$variant\tfork=$fork\tentry=${gcText(processGcBefore)}\texit=${gcText(gc())}\twindow=child-mode-entry-to-after-final-source-check-not-entire-JVM-lifetime")
    emit("COMPLETE\tvariant=$variant\tfork=$fork\tcases=90\trows=$rows\tmeasuredCalls=540\twarmupCalls=180\tjarSha=${jarHashes.getValue(variant)}")
}

data class Owned(val handle: ProcessHandle, var start: Instant?, var capturedParent: Long?, val discoveredFrom: Long, var terminal: Boolean = false)
enum class State { DEAD, SAME, CHANGED, UNKNOWN }
fun state(item: Owned): State {
    if (item.terminal) return State.DEAD // A positively accounted dead identity is never revived by PID reuse.
    if (!item.handle.isAlive) return State.DEAD
    val start = try { item.handle.info().startInstant().orElse(null) } catch (_: Exception) { null }
    return when { start == null || item.start == null -> State.UNKNOWN; start == item.start -> State.SAME; else -> State.CHANGED }
}
fun runCampaign() {
    val launchEpoch = arg("--launch-epoch-ms").toLong()
    val startupMs = enteredEpoch - launchEpoch
    require(startupMs in 0..100000) { "Initial startup exceeded bounded allowance; parent must account launch process" }
    val deadlineEpoch = Math.addExact(launchEpoch, 1200000L)
    val deadlineNano = enteredNano + TimeUnit.MILLISECONDS.toNanos(1200000L - startupMs)
    fun remainingMs(): Long = minOf(TimeUnit.NANOSECONDS.toMillis(deadlineNano - System.nanoTime()), deadlineEpoch - System.currentTimeMillis())
    val out = exactPath(arg("--out"))
    check(Files.list(out).use { !it.findAny().isPresent }) { "Use a new empty evidence directory" }
    val campaignCache = checkNotNull(System.getenv("KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR"))
    check(exactPath(campaignCache) != out && !exactPath(campaignCache).startsWith(out))
    val childCache = Files.createDirectory(out.resolve("child-script-cache"))
    val raw = Files.createFile(out.resolve("samples.tsv"))
    Files.writeString(raw, "pair\tposition\tvariant\tfork\tid\tsample\tcalls\twarmup\twallNs\tthreadCpuNs\tthreadAllocatedBytes\tgcCount\tgcMillis\tgcBefore\tgcAfter\n")
    val comparisons = Files.createFile(out.resolve("paired-totals.tsv"))
    Files.writeString(comparisons, "pair\tid\tbaselineWallNs\tbaselineCpuNs\tbaselineAllocatedBytes\tbaselineGcCount\tbaselineGcMillis\tcandidateWallNs\tcandidateCpuNs\tcandidateAllocatedBytes\tcandidateGcCount\tcandidateGcMillis\twallRatio\tcpuRatio\tallocationRatio\n")
    val owned = mutableListOf<Owned>() // Preserve every identity, including later reuse of a numeric PID.
    var directProcess: Process? = null
    var directIdentityCaptured = false
    fun remember(handle: ProcessHandle, from: Long) {
        val item = Owned(handle, null, null, from)
        owned += item // Retain UNKNOWN before fallible metadata.
        val start = handle.info().startInstant().orElse(null)
        val parent = handle.parent().orElse(null)?.pid()
        item.start = start; item.capturedParent = parent
        val old = owned.lastOrNull { it !== item && it.handle.pid() == handle.pid() && !it.terminal }
        if (old != null) {
            when (state(old)) {
                State.DEAD -> {
                    old.terminal = true
                    emit("ACCOUNT\tpid=${old.handle.pid()}\tstart=${old.start}\tparent=${old.capturedParent}\tdiscoveredFrom=${old.discoveredFrom}\tstate=DEAD\tbasis=prior-identity-before-owned-PID-reuse")
                }
                State.SAME -> {
                    check(start != null && old.start == start) { "Inconsistent incoming PID identity: NO RELEASE" }
                    owned.remove(item) // Same positively validated identity is already retained.
                    return
                }
                State.CHANGED, State.UNKNOWN -> error("Prior nonterminal PID identity unresolved: NO RELEASE")
            }
        }
        emit("OWNED\tpid=${handle.pid()}\tstart=$start\tparent=$parent\tdiscoveredFrom=$from")
        check(start != null || !handle.isAlive) { "Live descendant lacks identity: NO RELEASE" }
    }
    fun census() {
        val problems = mutableListOf<String>()
        for (item in owned.toList()) if (state(item) == State.SAME) {
            try { item.handle.descendants().use { entries -> entries.forEach { handle ->
                try { remember(handle, item.handle.pid()) } catch (e: Exception) { problems += "register ${handle.pid()}: $e" }
            } } } catch (e: Exception) { problems += "descendants ${item.handle.pid()}: $e" }
        }
        check(problems.isEmpty()) { "Incomplete census: $problems; NO RELEASE" }
    }
    fun cleanup(): List<String> {
        val problems = mutableListOf<String>()
        try { census() } catch (e: Exception) { problems += "census: $e" }
        val stop = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(minOf(12000L, maxOf(0L, remainingMs())))
        for (item in owned.toList().asReversed()) {
            try { if (state(item) == State.SAME) { emit("TERM\tpid=${item.handle.pid()}\tstart=${item.start}"); item.handle.destroy() } }
            catch (e: Exception) { problems += "term ${item.handle.pid()}: $e" }
        }
        val grace = minOf(stop, System.nanoTime() + TimeUnit.SECONDS.toNanos(1))
        while (System.nanoTime() < grace && owned.any { state(it) == State.SAME }) Thread.sleep(25)
        try { census() } catch (e: Exception) { problems += "cleanup census: $e" }
        for (item in owned.toList().asReversed()) {
            try { if (state(item) == State.SAME) { emit("KILL\tpid=${item.handle.pid()}\tstart=${item.start}"); item.handle.destroyForcibly() } }
            catch (e: Exception) { problems += "kill ${item.handle.pid()}: $e" }
        }
        while (System.nanoTime() < stop && owned.any { state(it) == State.SAME }) Thread.sleep(25)
        for (item in owned) {
            val final = state(item)
            emit("ACCOUNT\tpid=${item.handle.pid()}\tstart=${item.start}\tparent=${item.capturedParent}\tdiscoveredFrom=${item.discoveredFrom}\tstate=$final")
            if (final != State.DEAD) problems += "Unresolved ${item.handle.pid()}: $final"
        }
        directProcess?.let { process ->
            val alive = try { process.isAlive } catch (e: Exception) { problems += "direct status: $e"; null }
            emit("DIRECT_ACCOUNT\tpid=${process.pid()}\talive=$alive\tidentityCaptured=$directIdentityCaptured")
            if (alive != false || !directIdentityCaptured) problems += "Direct Process not fully accounted: NO RELEASE"
        }
        return problems
    }
    fun fields(line: String): Map<String, String> {
        val pairs = line.split('\t').drop(1).map { it.substringBefore('=') to it.substringAfter('=') }
        check(pairs.map { it.first }.distinct().size == pairs.size)
        return pairs.toMap()
    }
    val expectedIds = workItems().map { it.id }.toSet()
    val allOperandPins = mutableMapOf<String, Pair<String, String>>()
    var launched = 0; var completed = 0; var retained = 0
    var failure: String? = null
    emit("CAMPAIGN\tmaxMs=1200000\tstartupMs=$startupMs\tchildMs=90000\tlaunchReserveMs=115000\tcleanupMs=12000\tpairs=6\tchildren=12\torders=BC,CB,BC,CB,BC,CB\tsharedChildScriptCache=$childCache\tnoUncontendedHostClaim=true")
    host("campaign-start")
    try {
        for (pair in 1..6) {
            val order = if (pair % 2 == 1) listOf("baseline", "candidate") else listOf("candidate", "baseline")
            val paired = mutableMapOf<String, Map<String, List<Long>>>()
            for ((position, variant) in order.withIndex()) {
                check(remainingMs() >= 115000) { "Insufficient safe launch/cleanup budget" }
                check(owned.all { state(it) == State.DEAD }) { "Previous process unresolved" }
                checkFiles(); checkSourcePair()
                val fork = (pair - 1) * 2 + position + 1
                val log = out.resolve("pair$pair-pos${position + 1}-$variant.log")
                Files.createFile(log)
                val cacheInventory = Files.walk(childCache).use { entries -> entries.sorted().map { path ->
                    check(!Files.isSymbolicLink(path) && path.toRealPath() == path) { "Cache symlink: $path" }
                    if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) childCache.relativize(path).toString() + "\t" + fileSha(path)
                    else { check(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)); "DIRECTORY\t${childCache.relativize(path)}" }
                }.toList() }
                Files.write(out.resolve("fork$fork-cache-before.tsv"), cacheInventory, StandardOpenOption.CREATE_NEW)
                val childOptions = listOf("--grant", arg("--grant"), "--ledger", ledger.toString(), "--ledger-sha", ledgerSha,
                    "--driver", driver.toString(), "--driver-sha", driverSha, "--out", out.toString(), "--variant", variant,
                    "--fork", fork.toString(), "--cache", childCache.toString(), "--campaign-deadline-epoch-ms", deadlineEpoch.toString())
                val command = listOf(javaHome.resolve("bin/java").toString()) + vmFlags + runnerFlags + listOf("-cp", kotlinHome.resolve("lib/kotlin-runner.jar").toString(), "org.jetbrains.kotlin.runner.Main", driver.toString(), "child") + childOptions
                val builder = ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile())
                builder.environment().apply {
                    for (key in listOf("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "KOTLIN_OPTS", "JAVACMD")) remove(key)
                    put("JAVA_HOME", javaHome.toString()); put("JAVA_OPTS", vmFlags.joinToString(" "))
                    put("KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR", childCache.toString())
                }
                emit("COMMAND\tpair=$pair\tposition=${position + 1}\tvariant=$variant\tfork=$fork\tremainingMs=${remainingMs()}\targv=${command.joinToString("|")}")
                directProcess = null; directIdentityCaptured = false
                val began = System.nanoTime()
                val process = builder.start()
                directProcess = process // FIRST post-start action retains the actual owned Process.
                launched++
                val handle = process.toHandle()
                val directOwned = Owned(handle, null, current.pid(), current.pid())
                owned += directOwned
                val start = handle.info().startInstant().orElse(null)
                directOwned.start = start
                directIdentityCaptured = start != null
                emit("LAUNCH\tpid=${handle.pid()}\tstart=$start\tfork=$fork")
                check(start != null) { "Root identity missing: NO RELEASE" }
                while (process.isAlive) {
                    census()
                    check(System.nanoTime() - began < TimeUnit.SECONDS.toNanos(90) && remainingMs() > 15000) { "First deadline failure: fork=$fork" }
                    process.waitFor(50, TimeUnit.MILLISECONDS)
                }
                census()
                check(owned.all { state(it) == State.DEAD }) { "Owned descendant survived child exit" }
                for (item in owned.filter { !it.terminal }) {
                    emit("ACCOUNT\tfork=$fork\tpid=${item.handle.pid()}\tstart=${item.start}\tparent=${item.capturedParent}\tdiscoveredFrom=${item.discoveredFrom}\tstate=DEAD")
                    item.terminal = true
                }
                emit("CHILD_EXIT\tfork=$fork\texit=${process.exitValue()}\twholeChildMs=${TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - began)}\tlogSha=${fileSha(log)}")
                check(process.exitValue() == 0) { "First failed child; no retry: fork=$fork" }
                val lines = Files.readAllLines(log)
                val complete = fields(lines.single { it.startsWith("COMPLETE\t") })
                check(complete == mapOf("variant" to variant, "fork" to fork.toString(), "cases" to "90", "rows" to "360", "measuredCalls" to "540", "warmupCalls" to "180", "jarSha" to jarHashes.getValue(variant)))
                val cases = lines.filter { it.startsWith("CASE\t") }.map(::fields)
                check(cases.size == 90 && cases.map { it.getValue("id") }.toSet() == expectedIds)
                for (row in cases) {
                    check(row.getValue("variant") == variant && row.getValue("fork") == fork.toString())
                    val pins = row.getValue("operandSha") to row.getValue("oracleSha")
                    check(hashPattern.matches(pins.first) && hashPattern.matches(pins.second))
                    val old = allOperandPins.putIfAbsent(row.getValue("id"), pins)
                    check(old == null || old == pins) { "Operand/oracle mismatch across matched forks" }
                }
                val samples = lines.filter { it.startsWith("SAMPLE\t") }.map(::fields)
                check(samples.size == 360)
                check(samples.map { it.getValue("id") to it.getValue("sample").toInt() }.toSet() == expectedIds.flatMap { id -> (-1..2).map { id to it } }.toSet())
                for (row in samples) {
                    check(row.getValue("variant") == variant && row.getValue("fork") == fork.toString() && row.getValue("calls") == "2")
                    check(row.getValue("warmup") == (row.getValue("sample").toInt() < 0).toString())
                    for (key in listOf("wallNs", "threadCpuNs", "threadAllocatedBytes", "gcCount", "gcMillis")) check(row.getValue(key).toLong() >= 0)
                    fun parseGc(text: String): List<Gc> = text.split(',').map { value ->
                        val parts = value.split(':'); check(parts.size == 3)
                        Gc(parts[0], parts[1].toLong(), parts[2].toLong()).also { check(it.count >= 0 && it.millis >= 0) }
                    }
                    val beforeGc = parseGc(row.getValue("gcBefore")); val afterGc = parseGc(row.getValue("gcAfter"))
                    check(beforeGc.map { it.name } == gcBeans.map { it.name } && afterGc.map { it.name } == gcBeans.map { it.name })
                    check(afterGc.indices.all { afterGc[it].count >= beforeGc[it].count && afterGc[it].millis >= beforeGc[it].millis })
                    check(afterGc.indices.sumOf { afterGc[it].count - beforeGc[it].count } == row.getValue("gcCount").toLong())
                    check(afterGc.indices.sumOf { afterGc[it].millis - beforeGc[it].millis } == row.getValue("gcMillis").toLong())
                    val columns = listOf(pair.toString(), (position + 1).toString(), variant, fork.toString()) + listOf("id", "sample", "calls", "warmup", "wallNs", "threadCpuNs", "threadAllocatedBytes", "gcCount", "gcMillis", "gcBefore", "gcAfter").map { row.getValue(it) }
                    Files.writeString(raw, columns.joinToString("\t") + "\n", StandardOpenOption.APPEND)
                    retained++
                }
                paired[variant] = samples.filter { it.getValue("sample").toInt() >= 0 }.groupBy { it.getValue("id") }.mapValues { (_, rows) ->
                    check(rows.size == 3)
                    listOf("wallNs", "threadCpuNs", "threadAllocatedBytes", "gcCount", "gcMillis").map { key ->
                        rows.fold(0L) { sum, row -> Math.addExact(sum, row.getValue(key).toLong()) }
                    }
                }
                completed++
                checkFiles(); checkSourcePair()
                host("after-fork-$fork")
                check(remainingMs() >= 15000) { "Campaign processing exhausted cleanup reserve" }
            }
            for (id in expectedIds.sorted()) {
                val b = paired.getValue("baseline").getValue(id); val c = paired.getValue("candidate").getValue(id)
                val ratios = (0..2).map { metric -> if (b[metric] == 0L) "NA_zero_baseline" else (c[metric].toDouble() / b[metric]).toString() }
                Files.writeString(comparisons, (listOf(pair.toString(), id) + b.map { it.toString() } + c.map { it.toString() } + ratios).joinToString("\t") + "\n", StandardOpenOption.APPEND)
                emit("PAIR_TOTAL\tpair=$pair\tid=$id\tbaseline=$b\tcandidate=$c\tratios=$ratios\tbasis=three-measured-samples-six-calls-no-global-acceptance")
            }
        }
        check(launched == 12 && completed == 12 && retained == 4320 && allOperandPins.size == 90)
        check(remainingMs() >= 15000) { "Final campaign processing exceeded budget/reserve" }
    } catch (e: Throwable) {
        failure = e.toString(); emit("FIRST_FAILURE\t$failure")
    } finally {
        val problems = cleanup()
        if (problems.isNotEmpty()) failure = "${failure ?: "cleanup"}; $problems; NO RELEASE"
        host("campaign-final")
        val samplesSha = fileSha(raw); val comparisonsSha = fileSha(comparisons)
        if (remainingMs() < 0) failure = "${failure ?: "postprocessing"}; campaign absolute deadline exceeded"
        emit("CAMPAIGN_TERMINAL\tlaunched=$launched\tcompleted=$completed\tretainedRows=$retained\tfailure=$failure\tremainingMs=${remainingMs()}\tsamplesSha=$samplesSha\tpairedTotalsSha=$comparisonsSha")
    }
    check(failure == null) { "Pilot failed; preserve all evidence, no automatic retry: $failure" }
}

when (mode) {
    "child" -> runChild()
    "campaign" -> runCampaign()
    else -> {
        val invocation = MethodHandles.constant(Any::class.java, "adapter-ready")
        check(invocation.invokeExact() as Any == "adapter-ready")
        emit("CHECK_COMPLETE\tinvokeExactObjectBoundary=true\tbenchmarkRuns=false\tnumericalCalls=false\tclassLoadingOfPort=false")
    }
}

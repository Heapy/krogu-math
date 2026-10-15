// SPDX-License-Identifier: Apache-2.0
// PREPARED ONLY under parent grant1929 for V2 provenance. Do not execute without a later grant.
// No dependencies: select exactly one freshly built own JVM JAR on -classpath.
import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.BigInteger
import java.lang.management.ManagementFactory
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.IdentityHashMap
import com.sun.management.ThreadMXBean as AllocationBean

fun sha(path: Path): String {
    val digest = MessageDigest.getInstance("SHA-256")
    Files.newInputStream(path).use { input ->
        val buffer = ByteArray(8192)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
}
fun escaped(value: Any?): String = (value?.toString() ?: "NA").replace("\\", "\\\\")
    .replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r")
fun emit(vararg columns: Any?) = println(columns.joinToString("\t", transform = ::escaped))
fun canonical(value: String): Path = Path.of(value).toRealPath()
fun digestArgument(value: String): String {
    require(value.matches(Regex("[0-9a-f]{64}"))) { "Expected lowercase SHA256: $value" }
    return value
}

require(args.size == 12) {
    "Usage: baseline|candidate fork-id snapshot-root source-manifest manifest-sha " +
        "jar-path jar-sha driver-path driver-sha vm-flags-file vm-flags-sha forward|reverse"
}
val variant = args[0].also { require(it == "baseline" || it == "candidate") }
val fork = args[1].also { require(it.matches(Regex("[A-Za-z0-9_-]{1,40}"))) }
val snapshot = canonical(args[2])
val manifest = canonical(args[3])
val manifestSha = digestArgument(args[4])
val jar = canonical(args[5])
val jarSha = digestArgument(args[6])
val driver = canonical(args[7])
val driverSha = digestArgument(args[8])
val flagsFile = canonical(args[9])
val flagsSha = digestArgument(args[10])
val caseOrder = args[11].also { require(it == "forward" || it == "reverse") }
require(Files.size(manifest) <= 1024 * 1024 && Files.size(flagsFile) <= 64 * 1024)
require(Files.size(jar) <= 64L * 1024 * 1024 && Files.size(driver) <= 64 * 1024)
check(sha(manifest) == manifestSha && sha(jar) == jarSha && sha(driver) == driverSha && sha(flagsFile) == flagsSha)

// Verify every own JVM production input and build pin in the supplied manifest.
// A build ledger is still required: these hashes do not prove JAR provenance alone.
val requiredSources = mutableListOf<String>()
for (directory in listOf("src", "src/commonMain", "src/jvmMain"))
    check(!Files.isSymbolicLink(snapshot.resolve(directory))) { "Linked source directory: $directory" }
for (directory in listOf("src/commonMain", "src/jvmMain")) {
    Files.walk(snapshot.resolve(directory)).use { stream ->
        stream.forEach {
            check(!Files.isSymbolicLink(it)) { "Linked source entry: $it" }
            if (Files.isRegularFile(it))
                requiredSources.add(snapshot.relativize(it).toString().replace('\\', '/'))
        }
    }
}
requiredSources.addAll(listOf("build.gradle.kts", "settings.gradle.kts", "gradle.properties",
    "gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.jar"))
val entries = Files.readAllLines(manifest).map { line ->
    val match = Regex("([0-9a-f]{64})  (.+)").matchEntire(line) ?: error("Invalid source manifest row")
    match.groupValues[2] to match.groupValues[1]
}
check(entries.map { it.first } == requiredSources.sorted()) { "Manifest must cover the complete sorted input set" }
for ((relative, expected) in entries) {
    val path = snapshot.resolve(relative).normalize()
    check(!Path.of(relative).isAbsolute && path.startsWith(snapshot) && path.toRealPath().startsWith(snapshot))
    var componentPath = snapshot
    for (component in Path.of(relative)) {
        componentPath = componentPath.resolve(component)
        check(!Files.isSymbolicLink(componentPath)) { "Linked manifest input: $relative" }
    }
    check(sha(path) == expected) { "Source mismatch: $relative" }
}
val expectedProduction = if (variant == "baseline") listOf(
    "1587d2351f85399bb3d3dbd505fffd812cb7e8b8aedab4f371d4c27520334f2f",
    "73c52b66a263c51ee57461acc9fc38c99950a43e27ad6a490713785d5e4d435b",
) else listOf(
    "92de3354da9545f5b4867ee2ed32e72c91fac4ed32aa5c666f5762bd9f723370",
    "24cfd50f0dd9edd25590877b23b4c2e72c89ce9fab71f4b75bc0fe11def56a5f",
)
for ((index, name) in listOf("BigDecimal", "BigInteger").withIndex()) {
    val relative = "src/commonMain/kotlin/io/heapy/krogu/math/$name.kt"
    check(entries.single { it.first == relative }.second == expectedProduction[index])
}
for (type in listOf(BigDecimal::class.java, BigInteger::class.java)) {
    val origin = type.protectionDomain.codeSource?.location ?: error("No class origin: ${type.name}")
    check(Path.of(origin.toURI()).toRealPath() == jar) { "Wrong class origin: ${type.name}: $origin" }
    emit("ORIGIN", type.name, origin, jarSha)
    for (field in type.declaredFields.sortedBy { it.name })
        emit("FIELD", type.name, field.name, field.type.name,
            Modifier.toString(field.modifiers), field.isSynthetic)
}

val runtime = ManagementFactory.getRuntimeMXBean()
val cacheProperty = "kotlin.main.kts.compiled.scripts.cache.dir"
val actualFlags = runtime.inputArguments
val flagsWithoutCache = actualFlags.filterNot { it.startsWith("-D$cacheProperty=") }
emit("META", "actual_vm_flags", actualFlags.joinToString(" | "))
check(flagsWithoutCache == Files.readAllLines(flagsFile)) { "VM flags differ from the exact flags manifest" }
for (required in listOf("-Xms256m", "-Xmx256m", "-XX:+UseSerialGC", "-XX:-OmitStackTraceInFastThrow"))
    check(required in flagsWithoutCache) { "Required VM flag missing: $required" }
check(Runtime.getRuntime().maxMemory() <= 256L * 1024 * 1024)
check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS")
check(System.getProperty("java.vendor") == "BellSoft")
check(KotlinVersion.CURRENT.toString() == "2.4.21")
val cache = Path.of(System.getProperty(cacheProperty) ?: error("Explicit script cache required")).toRealPath()
val cacheName = "task528-measure-${driverSha.take(12)}-${jarSha.take(12)}-" +
    "${manifestSha.take(12)}-${flagsSha.take(12)}-$fork"
check(cache.fileName.toString() == cacheName) { "Cache must be keyed by driver/JAR/source/flags/fork: $cacheName" }
for ((key, value) in listOf(
    "variant" to variant, "fork" to fork, "case_order" to caseOrder,
    "snapshot" to snapshot, "source_manifest_sha256" to manifestSha,
    "jar" to jar, "jar_sha256" to jarSha, "driver" to driver,
    "driver_sha256" to driverSha, "vm_flags_sha256" to flagsSha, "script_cache" to cache,
    "java_runtime" to System.getProperty("java.runtime.version"),
    "java_vm" to System.getProperty("java.vm.name"), "java_vendor" to System.getProperty("java.vendor"),
    "java_home" to System.getProperty("java.home"), "kotlin_version" to KotlinVersion.CURRENT,
    "kotlin_home" to System.getProperty("kotlin.home"), "os_name" to System.getProperty("os.name"),
    "os_version" to System.getProperty("os.version"), "os_arch" to System.getProperty("os.arch"),
    "available_processors" to Runtime.getRuntime().availableProcessors(),
    "max_heap_bytes" to Runtime.getRuntime().maxMemory(), "start_epoch_ms" to runtime.startTime,
)) emit("META", key, value)

val allocationBean = ManagementFactory.getThreadMXBean() as? AllocationBean
var allocationStatus = "supported"
if (allocationBean == null) allocationStatus = "ThreadMXBean extension unavailable"
else if (!allocationBean.isThreadAllocatedMemorySupported) allocationStatus = "allocation counters unsupported"
else if (!allocationBean.isThreadAllocatedMemoryEnabled) {
    try { allocationBean.isThreadAllocatedMemoryEnabled = true }
    catch (failure: SecurityException) { allocationStatus = "allocation enabling denied: ${failure.message}" }
    catch (failure: UnsupportedOperationException) { allocationStatus = "allocation enabling unsupported: ${failure.message}" }
}
if (allocationStatus == "supported" && allocationBean?.isThreadAllocatedMemoryEnabled != true)
    allocationStatus = "allocation counter remained disabled"
val threadId = Thread.currentThread().threadId()
var allocationEnabled = allocationStatus == "supported"
if (allocationEnabled && allocationBean!!.getThreadAllocatedBytes(threadId) < 0) {
    allocationEnabled = false
    allocationStatus = "current-thread counter returned a negative value"
}
fun allocated(): Long = if (allocationEnabled) allocationBean!!.getThreadAllocatedBytes(threadId) else -1L
emit("META", "allocation_counter", allocationStatus)
emit("META", "counter_thread_id", threadId)

data class Gc(val name: String, val count: Long, val timeMs: Long)
val gcBeans = ManagementFactory.getGarbageCollectorMXBeans()
fun gcSnapshot(): List<Gc> = gcBeans.map { Gc(it.name, it.collectionCount, it.collectionTime) }
fun counterDelta(before: Long, after: Long): String = when {
    before < 0 || after < 0 -> "unsupported"
    after < before -> "reset-or-invalid"
    else -> (after - before).toString()
}
fun gcReport(before: List<Gc>, after: List<Gc>): String {
    check(before.map { it.name } == after.map { it.name })
    return before.zip(after).joinToString("; ") { (a, b) ->
        "${a.name}:count=${a.count}->${b.count},delta=${counterDelta(a.count, b.count)}," +
            "ms=${a.timeMs}->${b.timeMs},delta=${counterDelta(a.timeMs, b.timeMs)}"
    }
}

enum class Operation { PRECISION, COMPARISON }
enum class Mode { FRESH_ONCE, REPEATED }
data class Budget(val digits: Int, val fresh: Int, val repeated: Int, val warmup: Int)
data class Spec(val id: String, val operation: Operation, val budget: Budget,
    val leftText: String, val leftScale: Int, val rightText: String?, val rightScale: Int,
    val expected: Int)
data class Input(val left: BigDecimal, val right: BigDecimal)
object Consumed { @Volatile var checksum: Long = 1L }

val repetitions = 5
val warmupRounds = 2
val budgets = listOf(Budget(9, 128, 512, 64), Budget(128, 16, 64, 8),
    Budget(1024, 4, 8, 2), Budget(4096, 2, 2, 1))
val specifications = mutableListOf<Spec>()
for (budget in budgets) for (sign in listOf(1, -1)) {
    fun signed(text: String) = if (sign > 0) text else "-$text"
    val tag = "digits=${budget.digits}/sign=$sign"
    val full = "3" + "1".repeat(budget.digits - 1)
    specifications += Spec("precision/$tag", Operation.PRECISION, budget, signed(full), 0, null, 0, budget.digits)
    val shortDigits = budget.digits / 2
    val drop = budget.digits - shortDigits
    val short = "3" + "1".repeat(shortDigits - 1)
    // All comparison cohorts have maximum coefficient length budget.digits.
    // Alignment is expressed as known text before measuring, never in a loop.
    val models = listOf(
        Triple("equal_trailing_zeros", short + "0".repeat(drop), drop) to 0,
        Triple("early_differing_decade", short + "0".repeat(drop), drop + 1) to sign,
        Triple("quotient_difference", "2" + "9".repeat(shortDigits - 1) + "0".repeat(drop), drop) to sign,
        Triple("last_digit_remainder", short + "0".repeat(drop - 1) + "1", drop) to -sign,
    )
    for ((model, expected) in models) {
        val (name, longText, longScale) = model
        specifications += Spec("$name/short_left/$tag", Operation.COMPARISON, budget,
            signed(short), 0, signed(longText), longScale, expected)
        specifications += Spec("$name/long_left/$tag", Operation.COMPARISON, budget,
            signed(longText), longScale, signed(short), 0, -expected)
    }
}
check(specifications.size == 72)
val measuredCalls = specifications.sumOf { repetitions.toLong() * (it.budget.fresh + it.budget.repeated) }
val warmupCalls = specifications.sumOf { 2L * warmupRounds * it.budget.warmup }
check(measuredCalls == 66240L && warmupCalls == 5400L)
emit("META", "measured_target_calls", measuredCalls)
emit("META", "warmup_target_calls", warmupCalls)
emit("META", "total_target_calls", measuredCalls + warmupCalls)
emit("META", "repetitions", repetitions)
emit("META", "warmup_rounds_per_case_and_mode", warmupRounds)
emit("META", "fresh_once_definition", "first target use of each new value; code warmed on separate objects")
emit("META", "byte_sized_instance_claim", "none; FIELD rows describe declarations only")
for (budget in budgets) emit("BUDGET", budget.digits, budget.fresh, budget.repeated, budget.warmup)

fun fixtures(spec: Spec, mode: Mode, iterations: Int): Array<Input> {
    val values = Array(if (mode == Mode.FRESH_ONCE) iterations else 1) {
        val left = BigDecimal(BigInteger(spec.leftText), spec.leftScale)
        val right = if (spec.rightText == null) left else BigDecimal(BigInteger(spec.rightText), spec.rightScale)
        Input(left, right)
    }
    val identities = IdentityHashMap<BigDecimal, Boolean>()
    for (input in values) check(identities.put(input.left, true) == null) { "Fresh fixture was reused" }
    check(values.all { it.left::class == BigDecimal::class && it.right::class == BigDecimal::class })
    return values
}
// This is the only measured target-call loop: statically resolved own methods,
// primitive index/result/checksum work and stores into a preallocated IntArray.
// No reflection, fixture building, expectation building or output occurs here.
fun invoke(spec: Spec, mode: Mode, values: Array<Input>, results: IntArray, iterations: Int): Long {
    var checksum = 0x123456789abcdefL
    when (spec.operation) {
        Operation.PRECISION -> for (i in 0 until iterations) {
            val result = values[if (mode == Mode.FRESH_ONCE) i else 0].left.precision()
            results[i] = result
            checksum = (checksum xor result.toLong()) * 0x100000001b3L + i
        }
        Operation.COMPARISON -> for (i in 0 until iterations) {
            val input = values[if (mode == Mode.FRESH_ONCE) i else 0]
            val result = input.left.compareTo(input.right)
            results[i] = result
            checksum = (checksum xor result.toLong()) * 0x100000001b3L + i
        }
    }
    return checksum
}
fun expectedChecksum(result: Int, iterations: Int): Long {
    var checksum = 0x123456789abcdefL
    for (i in 0 until iterations) checksum = (checksum xor result.toLong()) * 0x100000001b3L + i
    return checksum
}
fun verify(spec: Spec, results: IntArray, checksum: Long, expected: Long) {
    check(results.all { it == spec.expected }) { "Wrong result: ${spec.id}" }
    check(checksum == expected) { "Wrong checksum: ${spec.id}" }
    Consumed.checksum = Consumed.checksum xor checksum
}

emit("ROW_HEADER", "variant", "fork", "case", "mode", "repetition", "iterations",
    "fresh_input_count", "elapsed_ns", "allocated_bytes", "allocation_status", "checksum", "gc_counters")
val orderedSpecs = if (caseOrder == "forward") specifications.toList() else specifications.asReversed()
for (spec in orderedSpecs) for (mode in Mode.entries) {
    // Each warmup batch owns new objects, never borrowed by measured samples.
    repeat(warmupRounds) {
        val values = fixtures(spec, mode, spec.budget.warmup)
        val results = IntArray(spec.budget.warmup)
        val expected = expectedChecksum(spec.expected, spec.budget.warmup)
        verify(spec, results, invoke(spec, mode, values, results, spec.budget.warmup), expected)
    }
    val iterations = if (mode == Mode.FRESH_ONCE) spec.budget.fresh else spec.budget.repeated
    repeat(repetitions) { repetition ->
        val values = fixtures(spec, mode, iterations)
        val results = IntArray(iterations)
        val expected = expectedChecksum(spec.expected, iterations)
        val gcBefore = gcSnapshot()
        val allocatedBefore = allocated()
        val started = System.nanoTime()
        val checksum = invoke(spec, mode, values, results, iterations)
        val elapsed = System.nanoTime() - started
        val allocatedAfter = allocated()
        val gcAfter = gcSnapshot()
        check(elapsed >= 0)
        verify(spec, results, checksum, expected)
        val sampleAllocationStatus = when {
            !allocationEnabled -> allocationStatus
            allocatedBefore < 0 || allocatedAfter < 0 -> "current-thread counter returned a negative value"
            allocatedAfter < allocatedBefore -> "allocation counter reset-or-invalid"
            else -> "supported"
        }
        val bytes = if (sampleAllocationStatus == "supported") (allocatedAfter - allocatedBefore).toString() else "NA"
        emit("ROW", variant, fork, spec.id, mode, repetition + 1, iterations, values.size,
            elapsed, bytes, sampleAllocationStatus, checksum, gcReport(gcBefore, gcAfter))
    }
}
emit("DONE", variant, fork, "all 720 samples verified", Consumed.checksum)

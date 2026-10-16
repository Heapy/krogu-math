#!/usr/bin/env kotlinr
// Matched JVM diagnostic. Invoke once per fresh process, alternating order across forks.
import java.io.File
import java.lang.management.ManagementFactory
import java.lang.management.MemoryType
import java.net.URLClassLoader
import java.security.MessageDigest

require(args.size in 4..5) { "baseline.jar candidate.jar NEW-output.csv forward|reverse [strip]" }
require(args.size == 4 || args[4] == "strip")
require(args[3] in listOf("forward", "reverse"))
check(System.getProperty("java.vendor") == "BellSoft" && System.getProperty("java.runtime.version") == "25.0.4+9-LTS")
val output = File(args[2])
check(!output.exists()) { "Refusing to overwrite $output" }
val bean = (ManagementFactory.getThreadMXBean() as? com.sun.management.ThreadMXBean)?.also {
    if (it.isThreadAllocatedMemorySupported) it.isThreadAllocatedMemoryEnabled = true
}
fun allocated(): Long? = bean?.takeIf { it.isThreadAllocatedMemoryEnabled }
    ?.getThreadAllocatedBytes(Thread.currentThread().threadId())?.takeIf { it >= 0 }
val pools = ManagementFactory.getMemoryPoolMXBeans().filter { it.type == MemoryType.HEAP }
val sink = arrayOfNulls<Any>(256)
data class Case(val name: String, val text: String, val operation: String, val argument: Int = 0)
val cases = listOf(
    Case("round-small", "12345.6789", "round", 7),
    Case("round-carry", "999999999999999999.95", "round", 7),
    Case("rescale-up-9", "12345.67", "setScale", 11),
    Case("rescale-up-18", "12345.67", "setScale", 20),
    Case("rescale-up-19-control", "12345.67", "setScale", 21),
    Case("rescale-down", "12345.6789", "setScale", 2),
    Case("strip-small", "12345.6700000", "stripTrailingZeros"),
    Case("strip-wide-long-tail", "12345678901234567890123456789" + "0".repeat(300), "stripTrailingZeros"),
    Case("strip-wide-no-tail", "7".repeat(512) + "9", "stripTrailingZeros"),
    Case("strip-wide-one-tail", "7".repeat(511) + "10", "stripTrailingZeros"),
    Case("strip-wide-one-tail-high-v2", java.math.BigInteger.ONE.shiftLeft(1700).multiply(java.math.BigInteger.TEN).toString(), "stripTrailingZeros"),
    Case("strip-wide-two-tail-high-v2", java.math.BigInteger.ONE.shiftLeft(1700).multiply(java.math.BigInteger.valueOf(100)).toString(), "stripTrailingZeros"),
).filter { args.size == 4 || it.operation == "stripTrailingZeros" }
class Adapter(val name: String, val file: File) : AutoCloseable {
    val loader = URLClassLoader(arrayOf(file.toURI().toURL()), this::class.java.classLoader)
    val decimal = loader.loadClass("io.heapy.krogu.math.BigDecimal")
    val rounding = loader.loadClass("io.heapy.krogu.math.RoundingMode")
    val context = loader.loadClass("io.heapy.krogu.math.MathContext")
    val mode = rounding.enumConstants.single { it.toString() == "HALF_EVEN" }
    init {
        for (type in listOf(decimal, rounding, context, loader.loadClass("io.heapy.krogu.math.BigInteger")))
            check(File(type.protectionDomain.codeSource.location.toURI()).canonicalFile == file)
    }
    fun operation(case: Case): () -> Any {
        val inputs = Array(16) { decimal.getConstructor(String::class.java).newInstance(case.text) }
        val method = when (case.operation) {
            "round" -> decimal.getMethod("round", context)
            "setScale" -> decimal.getMethod("setScale", Int::class.javaPrimitiveType, rounding)
            else -> decimal.getMethod(case.operation)
        }
        val parameters = when (case.operation) {
            "round" -> arrayOf(context.getConstructor(Int::class.javaPrimitiveType, rounding).newInstance(case.argument, mode))
            "setScale" -> arrayOf(case.argument, mode)
            else -> emptyArray()
        }
        var index = 0
        return { method.invoke(inputs[index++ and 15], *parameters) }
    }
    override fun close() { loader.close() }
}
fun sha(file: File) = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
val artifacts = listOf(File(args[0]).canonicalFile, File(args[1]).canonicalFile)
val hashes = artifacts.map(::sha)
check(artifacts[0] != artifacts[1] && hashes[0] != hashes[1]) { "Need distinct before/after artifacts" }
val adapters = listOf(Adapter("baseline", artifacts[0]), Adapter("candidate", artifacts[1]))
val operations = adapters.associateWith { adapter -> cases.associateWith { adapter.operation(it) } }
// Compare full representation outside timing, including scale encoded in canonical toString.
for (case in cases) check(operations.getValue(adapters[0]).getValue(case)().toString() ==
    operations.getValue(adapters[1]).getValue(case)().toString()) { "Outcome mismatch: ${case.name}" }
output.parentFile?.mkdirs()
output.bufferedWriter().use { csv ->
    csv.appendLine("# JDK=${System.getProperty("java.runtime.version")};vm=${System.getProperty("java.vm.name")};arch=${System.getProperty("os.arch")};order=${args[3]}")
    csv.appendLine("# baseline_sha256=${hashes[0]};candidate_sha256=${hashes[1]}")
    csv.appendLine("# Reflection/ring/loop overhead included; heap peak is sum of pool peaks, an upper bound, not isolated operation residency.")
    csv.appendLine("# Calibration targets 10ms or caps at 131072 operations; actual counts and durations are recorded.")
    csv.appendLine("implementation,case,sample,operations,elapsed_ns,ns_per_op,allocated_bytes_per_op,heap_pool_peak_upper_bound_bytes,checksum")
    for (case in cases) for (adapter in if (args[3] == "reverse") adapters.reversed() else adapters) {
        val operation = operations.getValue(adapter).getValue(case)
        fun batch(count: Int): Long {
            val start = System.nanoTime()
            repeat(count) { sink[it and 255] = operation() }
            return System.nanoTime() - start
        }
        repeat(sink.size) { sink[it] = operation() }
        var count = 1
        while (batch(count) < 10_000_000L && count < 131072) count *= 2
        repeat(5) { batch(count) }
        System.gc()
        pools.forEach { it.resetPeakUsage() }
        repeat(5) { sample ->
            val before = allocated()
            val elapsed = batch(count)
            val after = allocated()
            val allocation = if (before != null && after != null) (after - before).toDouble() / count else null
            val digest = sink.fold(1) { a, b -> 31 * a + (b?.hashCode() ?: 0) }
            csv.appendLine("${adapter.name},${case.name},$sample,$count,$elapsed,${elapsed.toDouble() / count},${allocation ?: "NA"},${pools.sumOf { it.peakUsage.used }},$digest")
        }
        csv.flush()
    }
}
check(artifacts.map(::sha) == hashes) { "Artifacts changed during measurements" }
adapters.forEach { it.close() }
println("Matched outcomes and unchanged artifacts; wrote $output")

#!/usr/bin/env kotlinr
// Same-artifact additive-API diagnostic; all Java adapter compilation/setup is untimed.
import java.io.File
import java.lang.management.ManagementFactory
import java.lang.management.MemoryType
import java.net.URLClassLoader
import java.security.MessageDigest
import java.util.concurrent.Callable
import javax.tools.ToolProvider

require(args.size == 4) { "candidate.jar NEW-output.csv forward|reverse expected-candidate-sha256 (run from repository root)" }
require(args[3].matches(Regex("[0-9a-f]{64}")))
require(args[2] in listOf("forward", "reverse"))
check(System.getProperty("java.vendor") == "BellSoft" && System.getProperty("java.runtime.version") == "25.0.4+9-LTS")
val artifact = File(args[0]).canonicalFile
val output = File(args[1]).canonicalFile
check(artifact.isFile && !output.exists())
output.parentFile.mkdirs()
fun sha(file: File) = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
val artifactHash = sha(artifact)
check(artifactHash == args[3]) { "Candidate differs from the frozen invocation ledger" }
val scriptSource = File("scripts/measure-decimal-accumulator-535.main.kts").canonicalFile
check(scriptSource.isFile) { "Run from the repository root" }
val scriptHash = sha(scriptSource)
val adapterDirectory = File(output.parentFile, output.nameWithoutExtension + "-adapter")
check(adapterDirectory.mkdir()) { "Refusing to reuse adapter directory" }
val adapterSource = File(adapterDirectory, "Accumulator535Probe.java")
adapterSource.writeText("""
import io.heapy.krogu.math.*;
import java.util.concurrent.Callable;
public final class Accumulator535Probe {
    public static Callable<Object> operation(String name, boolean accumulating) {
        final boolean dot = name.startsWith("dot-");
        final boolean fma = name.startsWith("fma-");
        final boolean wide = name.contains("wide");
        final boolean mixed = name.contains("mixed");
        final boolean step = name.contains("step");
        final boolean reuse = name.contains("reuse");
        final DecimalRounding policy = step ? DecimalRounding.EACH_STEP : DecimalRounding.FINAL_RESULT;
        final MathContext context = (dot || fma) ? new MathContext(12, RoundingMode.HALF_EVEN) : MathContext.UNLIMITED;
        final int length = fma ? 2 : (wide ? 16 : 256);
        final BigDecimal[] left = new BigDecimal[length];
        final BigDecimal[] right = new BigDecimal[length];
        for (int i = 0; i < length; i++) {
            final String digits = wide ? "123456789".repeat(28) + (i + 11) : Integer.toString(i % 7 + 1);
            final int scale = mixed ? i % 13 : 2;
            left[i] = new BigDecimal(new BigInteger((i % 3 == 0 ? "-" : "") + digits), scale);
            right[i] = new BigDecimal(new BigInteger(wide ? "987654321".repeat(20) + (i + 13) : "123"), 2);
        }
        final DecimalAccumulator reusable = new DecimalAccumulator(context, policy, null, null);
        if (accumulating) return () -> {
            if (fma) return DecimalAccumulatorKt.fma(left[0], right[0], left[1], context, policy, null);
            final DecimalAccumulator acc = reuse ? reusable : new DecimalAccumulator(context, policy, null, null);
            if (reuse) acc.clear();
            if (dot) for (int i = 0; i < length; i++) acc.addProduct(left[i], right[i]);
            else for (BigDecimal term : left) acc.add(term);
            return acc.snapshot();
        };
        return () -> {
            if (fma) return step ? left[0].multiply(right[0], context).add(left[1], context)
                : left[0].multiply(right[0]).add(left[1]).round(context);
            BigDecimal result;
            if (dot) {
                result = step ? left[0].multiply(right[0], context) : left[0].multiply(right[0]);
                for (int i = 1; i < length; i++) {
                    final BigDecimal product = step ? left[i].multiply(right[i], context) : left[i].multiply(right[i]);
                    result = step ? result.add(product, context) : result.add(product);
                }
            } else {
                result = step ? left[0].round(context) : left[0];
                for (int i = 1; i < length; i++) result = step ? result.add(left[i], context) : result.add(left[i]);
            }
            return step ? result : result.round(context);
        };
    }
}
""".trimIndent())
val stdlib = File(kotlin.Unit::class.java.protectionDomain.codeSource.location.toURI())
val compileExit = ToolProvider.getSystemJavaCompiler().run(null, System.out, System.err,
    "--release", "25", "-classpath", artifact.path + File.pathSeparator + stdlib.path,
    "-d", adapterDirectory.path, adapterSource.path)
check(compileExit == 0) { "Adapter compile failed: $compileExit" }
val adapterClass = File(adapterDirectory, "Accumulator535Probe.class")
val javaHash = sha(adapterSource)
val classHash = sha(adapterClass)
val loader = URLClassLoader(arrayOf(adapterDirectory.toURI().toURL(), artifact.toURI().toURL()), this::class.java.classLoader)
for (name in listOf("BigDecimal", "BigInteger", "MathContext", "RoundingMode", "DecimalRounding", "DecimalAccumulator", "DecimalAccumulatorKt")) {
    val type = loader.loadClass("io.heapy.krogu.math.$name")
    check(File(type.protectionDomain.codeSource.location.toURI()).canonicalFile == artifact)
}
val adapterType = loader.loadClass("Accumulator535Probe")
check(adapterType.classLoader === loader &&
    File(adapterType.protectionDomain.codeSource.location.toURI()).canonicalFile == adapterDirectory.canonicalFile)
val factory = adapterType.getMethod("operation", String::class.java, Boolean::class.javaPrimitiveType)
val cases = listOf("sum-compact", "sum-compact-reuse", "sum-mixed", "sum-wide", "dot-compact", "dot-step-compact", "dot-wide", "fma-compact", "fma-wide")
val operations = cases.associateWith { name -> listOf(false, true).associateWith { accumulate ->
    @Suppress("UNCHECKED_CAST")
    (factory.invoke(null, name, accumulate) as Callable<Any>)
} }
// Full canonical representation comparison, outside every timed batch.
for ((name, pair) in operations) check(pair.getValue(false).call().toString() == pair.getValue(true).call().toString()) { "Outcome mismatch: $name" }
val bean = (ManagementFactory.getThreadMXBean() as? com.sun.management.ThreadMXBean)?.also {
    if (it.isThreadAllocatedMemorySupported) it.isThreadAllocatedMemoryEnabled = true
}
fun allocation(): Long? = bean?.takeIf { it.isThreadAllocatedMemoryEnabled }
    ?.getThreadAllocatedBytes(Thread.currentThread().threadId())?.takeIf { it >= 0 }
val pools = ManagementFactory.getMemoryPoolMXBeans().filter { it.type == MemoryType.HEAP }
val sink = arrayOfNulls<Any>(256)
output.bufferedWriter().use { csv ->
    csv.appendLine("# JDK=${System.getProperty("java.runtime.version")};arch=${System.getProperty("os.arch")};order=${args[2]};candidate_sha256=$artifactHash;script_sha256=$scriptHash")
    csv.appendLine("# adapter_java_sha256=$javaHash;adapter_class_sha256=$classHash")
    csv.appendLine("# Same artifact; prepared inputs and Java adapter compilation excluded. Callable/ring/loop overhead included.")
    csv.appendLine("# Calibration targets 20ms or caps at 8192 batches. Heap pool peak sum is an upper bound including setup/script, not operation residency.")
    csv.appendLine("implementation,case,sample,operations,elapsed_ns,ns_per_op,allocated_bytes_per_op,heap_pool_peak_upper_bound_bytes,checksum")
    for (case in cases) for (accumulate in if (args[2] == "reverse") listOf(true, false) else listOf(false, true)) {
        val operation = operations.getValue(case).getValue(accumulate)
        fun batch(count: Int): Long {
            val start = System.nanoTime()
            repeat(count) { sink[it and 255] = operation.call() }
            return System.nanoTime() - start
        }
        repeat(sink.size) { sink[it] = operation.call() }
        var count = 1
        while (batch(count) < 20_000_000L && count < 8192) count *= 2
        repeat(5) { batch(count) }
        System.gc()
        pools.forEach { it.resetPeakUsage() }
        repeat(5) { sample ->
            val before = allocation()
            val elapsed = batch(count)
            val after = allocation()
            val bytes = if (before != null && after != null) (after - before).toDouble() / count else null
            val digest = sink.fold(1) { a, b -> 31 * a + (b?.hashCode() ?: 0) }
            csv.appendLine("${if (accumulate) "accumulator" else "chain"},$case,$sample,$count,$elapsed,${elapsed.toDouble() / count},${bytes ?: "NA"},${pools.sumOf { it.peakUsage.used }},$digest")
        }
        csv.flush()
    }
}
check(sha(artifact) == artifactHash && sha(adapterSource) == javaHash && sha(adapterClass) == classHash && sha(scriptSource) == scriptHash)
loader.close()
println("Matched outcomes and unchanged artifacts; wrote $output")

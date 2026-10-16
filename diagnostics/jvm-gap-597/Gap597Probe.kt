// SPDX-License-Identifier: Apache-2.0
package bench

import io.heapy.krogu.math.BigDecimal as KroguDecimal
import io.heapy.krogu.math.BigInteger as KroguInteger
import java.io.File
import java.lang.management.ManagementFactory
import java.security.MessageDigest
import java.time.Duration
import jdk.jfr.Recording
import jdk.jfr.consumer.RecordingFile
import kotlin.time.TimeSource

/** Compile WITH unchanged527 Workloads.kt +JdkWorkloads.kt into one separate probe JAR. */
object Gap597Probe {
    private val results = arrayOfNulls<Any>(256)
    private val memory = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
    private data class Case(val name: String, val fresh: Boolean = false,
        val prepare: (Int, Int) -> Workload)

    @JvmStatic fun main(args: Array<String>) {
        require(args.size == 10) { "timing|profile|profile-control krogu|jdk main.jar sha probe.jar sha fork forward|reverse case|all NEW-output" }
        val mode = args[0]; val own = args[1] == "krogu"
        require(mode in listOf("timing", "profile", "profile-control") && args[1] in listOf("krogu", "jdk"))
        require(mode == "timing" || own)
        require(args[7] in listOf("forward", "reverse"))
        val fork = args[6].toInt().also { require(it in 0..2) }
        check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS" && System.getProperty("java.vendor") == "BellSoft")
        val main = File(args[2]).canonicalFile; val probe = File(args[4]).canonicalFile
        fun sha(file: File) = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
        fun origin(type: Class<*>) = File(type.protectionDomain.codeSource.location.toURI()).canonicalFile
        fun binding() {
            check(main != probe && sha(main) == args[3] && sha(probe) == args[5])
            for (name in listOf("BigInteger", "BigDecimal", "MathContext", "RoundingMode"))
                check(origin(Class.forName("io.heapy.krogu.math.$name")) == main)
            check(origin(Gap597Probe::class.java) == probe && origin(Workload::class.java) == probe)
        }
        binding()
        check(memory.isThreadAllocatedMemorySupported && memory.isCurrentThreadCpuTimeSupported)
        memory.isThreadAllocatedMemoryEnabled = true; memory.isThreadCpuTimeEnabled = true
        val output = File(args[9]).canonicalFile.also { require(!it.exists()); check(it.mkdirs()) }
        val allOwn = kroguWorkloads(); val allJdk = jdkWorkloads() //both public preparations outside counters
        val ids = labels.keys.flatMap { label -> listOf("decimal/$label/divide", "decimal/$label/precision") }.toSet()
        val ownById = allOwn.associateBy { it.name }; val jdkById = allJdk.associateBy { it.name }
        for (id in ids) for (slot in 0..15)
            check(ownById.getValue(id).run(slot).toString() == jdkById.getValue(id).run(slot).toString())
        val ownControls = precisionControls(true).associateBy { it.name }
        val jdkControls = precisionControls(false).associateBy { it.name }
        for (id in ownControls.keys) {
            val left = ownControls.getValue(id).prepare(16, 0)
            val right = jdkControls.getValue(id).prepare(16, 0)
            for (slot in 0..15) check(left.run(slot).toString() == right.run(slot).toString())
        }
        val cases = (if (own) allOwn else allJdk).filter { it.name in ids }.map { job ->
            Case(job.name) { _, _ -> job }
        } + (if (own) ownControls else jdkControls).values
        val selected = cases.filter { args[8] == "all" || it.name == args[8] }
            .let { if (args[7] == "reverse") it.reversed() else it }
        check(selected.isNotEmpty() && selected.map { it.name }.toSet().size == selected.size)
        if (mode != "timing") require(selected.size == 1 && selected.single().name in ids)
        File(output, "binding.txt").writeText("mode=$mode\nimplementation=${args[1]}\nmain_sha256=${args[3]}\nprobe_sha256=${args[5]}\nfork=$fork\norder=${args[7]}\n")
        File(output, "samples.csv").bufferedWriter().use { csv ->
            csv.appendLine("implementation,workload,sample,operations,elapsed_ns,cpu_ns,allocated_bytes,checksum,input_offset,input_mask")
            for (case in selected) {
                val seed = fork * 5
                // Prime only the observation workload. Fresh controls use separate correctness pools.
                val validation = case.prepare(16, 0)
                val initial = Array(16) { validation.run(it) }
                results.indices.forEach { results[it] = initial[it and 15] }
                var count = 1
                val cap = if (case.fresh) 65_536 else 1_048_576
                while (true) {
                    val elapsed = batch(case.prepare(count, seed), count, seed)
                    //527 advertises100ms but calibrates against target/2.
                    if (elapsed >= 100_000_000L / 2 || count >= cap) break
                    count *= 2
                }
                repeat(5) { batch(case.prepare(count, seed + it), count, seed + it); checksum() }
                // Hot jobs get timed200ms warmup; fresh pools use wall200ms to bound setup growth.
                val wall = System.nanoTime(); var timed = 0L
                while (if (case.fresh) System.nanoTime() - wall < 200_000_000L else timed < 200_000_000L)
                    timed += batch(case.prepare(count, seed), count, seed)
                if (mode == "timing") repeat(5) { sample ->
                    val offset = seed + sample
                    val job = case.prepare(count, offset)
                    val tid = Thread.currentThread().threadId()
                    val alloc = memory.getThreadAllocatedBytes(tid); val cpu = memory.currentThreadCpuTime
                    val elapsed = batch(job, count, offset)
                    val cpuNs = memory.currentThreadCpuTime - cpu
                    val bytes = memory.getThreadAllocatedBytes(tid) - alloc
                    val mask = if (count >= 16) 65535 else (0 until count).fold(0) { m, i -> m or (1 shl ((i + offset) and 15)) }
                    csv.appendLine("${args[1]},${case.name},$sample,$count,$elapsed,$cpuNs,$bytes,${checksum()},${offset and 15},$mask")
                } else {
                    val job = case.prepare(count, seed)
                    val recording = if (mode == "profile") Recording().apply {
                        enable("jdk.ExecutionSample").withPeriod(Duration.ofMillis(10))
                        enable("jdk.ObjectAllocationSample").with("throttle", "300/s").withStackTrace()
                        start()
                    } else null
                    val tid = Thread.currentThread().threadId()
                    val allocation = memory.getThreadAllocatedBytes(tid); val cpu = memory.currentThreadCpuTime
                    val started = System.nanoTime(); var operations = 0L; var mask = 0
                    while (System.nanoTime() - started < 10_000_000_000L) {
                        val offset = (seed.toLong() + operations).toInt()
                        batch(job, count, offset); operations += count
                        mask = mask or if (count >= 16) 65535 else
                            (0 until count).fold(0) { m, i -> m or (1 shl ((i + offset) and 15)) }
                    }
                    val elapsed = System.nanoTime() - started
                    val cpuNs = memory.currentThreadCpuTime - cpu
                    val bytes = memory.getThreadAllocatedBytes(tid) - allocation
                    recording?.stop() //before checksum, CSV formatting or output allocations
                    csv.appendLine("krogu,${case.name},0,$operations,$elapsed,$cpuNs,$bytes,${checksum()},${seed and 15},$mask")
                    if (recording != null) {
                        val file = File(output, "own-profile.jfr")
                        recording.dump(file.toPath()); recording.close()
                        attribute(file, tid, File(output, "attribution.csv"))
                    }
                }
                csv.flush()
            }
        }
        binding()
        File(output, "COMPLETE.txt").writeText("PASS bindings/exact prepared16 public outputs; mode=$mode cases=${selected.size}; observations require independent analysis\n")
    }

    private fun batch(job: Workload, count: Int, offset: Int): Long {
        val mark = TimeSource.Monotonic.markNow()
        repeat(count) { i -> val logical = i + offset; results[logical and 255] = job.run(logical) }
        return mark.elapsedNow().inWholeNanoseconds
    }
    private fun checksum(): Int = results.fold(1) { value, result -> value * 31 + (result?.hashCode() ?: 0) }
    private val labels = linkedMapOf("small" to "123.45", "wide" to "123456789012345678901234567890.12345678901234567890",
        "negative-scale" to "12345678901234567890E+80", "large-scale" to "12345678901234567890E-80")

    private fun precisionControls(own: Boolean): List<Case> = buildList {
        for ((label, base) in labels) {
            val text = List(16) { i -> base.replaceFirst("123", (123 + i).toString()) }
            val publicValues = text.map { java.math.BigDecimal(it) }
            val coefficients = publicValues.map { it.unscaledValue().toString() }
            val scales = publicValues.map { it.scale() }
            for (construction in listOf("string", "coefficient")) add(Case("precision-first/$label/$construction", true) { count, offset ->
                if (own) {
                    val values = Array(count) { i -> val slot = (i + offset) and 15
                        if (construction == "string") KroguDecimal(text[slot]) else KroguDecimal(KroguInteger(coefficients[slot]), scales[slot]) }
                    Workload("first") { logical -> values[(logical - offset) % count].precision() }
                } else {
                    val values = Array(count) { i -> val slot = (i + offset) and 15
                        if (construction == "string") java.math.BigDecimal(text[slot]) else java.math.BigDecimal(java.math.BigInteger(coefficients[slot]), scales[slot]) }
                    Workload("first") { logical -> values[(logical - offset) % count].precision() }
                }
            })
            add(Case("precision-construction/$label/string") { _, _ ->
                if (own) Workload("construction") { KroguDecimal(text[it and 15]).precision() }
                else Workload("construction") { java.math.BigDecimal(text[it and 15]).precision() }
            })
        }
    }

    private fun attribute(file: File, threadId: Long, output: File) {
        val counts = linkedMapOf<Pair<String, String>, Long>()
        RecordingFile(file.toPath()).use { recording ->
            while (recording.hasMoreEvents()) {
                val event = recording.readEvent()
                if (event.eventType.name !in listOf("jdk.ExecutionSample", "jdk.ObjectAllocationSample")) continue
                val thread = if (event.hasField("sampledThread")) event.getThread("sampledThread") else event.thread
                if (thread?.javaThreadId != threadId) continue
                val own = event.stackTrace?.frames.orEmpty().map { it.method }.filter { it.type.name.startsWith("io.heapy.krogu.math.") }
                val leaf = own.firstOrNull()?.let { "${it.type.name}.${it.name}" } ?: "<no-own-frame>"
                if (event.eventType.name == "jdk.ExecutionSample") {
                    val key = "cpu-leaf-samples" to leaf; counts[key] = (counts[key] ?: 0) + 1
                    for (method in own.map { "${it.type.name}.${it.name}" }.distinct()) {
                        val key2 = "cpu-inclusive-samples" to method; counts[key2] = (counts[key2] ?: 0) + 1
                    }
                } else {
                    val type = event.getClass("objectClass").name
                    val key = "allocation-estimated-bytes" to "$type@$leaf"
                    counts[key] = (counts[key] ?: 0) + event.getLong("weight")
                }
            }
        }
        output.bufferedWriter().use { writer ->
            writer.appendLine("metric,own_path_or_type,weight")
            for ((key, weight) in counts) writer.appendLine("${key.first},${key.second},$weight")
        }
    }
}

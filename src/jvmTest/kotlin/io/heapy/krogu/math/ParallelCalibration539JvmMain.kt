package io.heapy.krogu.math

import java.io.File
import java.lang.management.ManagementFactory
import java.lang.management.MemoryType
import java.security.MessageDigest

/** Source-only JVM candidate experiment; no production dispatch or threshold changes. */
object ParallelCalibration539JvmMain {
    @Volatile private var escape: IntArray? = null
    private val allocations = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
    private val cpu = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean
    private val pools = ManagementFactory.getMemoryPoolMXBeans().filter { it.type == MemoryType.HEAP }
    private data class Observation(val wall: Long, val cpu: Long, val allocated: Long, val heapPeakUpperBound: Long)

    @JvmStatic fun main(args: Array<String>) {
        require(args.size == 6) { "forward|reverse samples main.jar mainSHA256 probe.jar probeSHA256" }
        require(args[0] in listOf("forward", "reverse"))
        val samples = args[1].toInt().also { require(it in 5..15) }
        val main = File(args[2]).canonicalFile
        val probe = File(args[4]).canonicalFile
        require(main != probe && args[3].matches(Regex("[a-f0-9]{64}")) && args[5].matches(Regex("[a-f0-9]{64}")))
        fun hash(file: File) = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
        fun origin(type: Class<*>, expected: File) = check(File(type.protectionDomain.codeSource.location.toURI()).canonicalFile == expected)
        fun verifyArtifacts() {
            check(hash(main) == args[3] && hash(probe) == args[5])
            listOf(MagnitudeMultiplication::class.java, MultiplicationPolicy539::class.java,
                ParallelCandidate539::class.java).forEach { origin(it, main) }
            origin(ParallelCalibration539JvmMain::class.java, probe)
        }
        verifyArtifacts()
        require(Runtime.version().feature() == 25)
        check(allocations.isThreadAllocatedMemorySupported)
        allocations.isThreadAllocatedMemoryEnabled = true
        check(allocations.totalThreadAllocatedBytes >= 0) { "All-thread allocation observation unavailable" }
        println("# parallel539 java=${System.getProperty("java.vendor")} ${System.getProperty("java.runtime.version")} arch=${System.getProperty("os.arch")} processors=${Runtime.getRuntime().availableProcessors()} order=${args[0]}")
        println("# main=${args[3]} probe=${args[5]} CPU=whole_process allocation=approx_all_threads heap_peak=sum_pool_peaks_upper_bound RSS=external_including_reference_preparation")
        println("# setup_rows=one_fresh_executor_plus_first_product_and_shutdown_not_await_termination; steady_rows=reused_warm_executor")
        val policy = MultiplicationPolicy539(64, 16, 384)
        val sizes = listOf(256, 1024, 4096, 8192, 16384).let { if (args[0] == "reverse") it.reversed() else it }
        for (size in sizes) {
            val a = IntArray(size) { ((it.toLong() * 104729 + 31) and 0x3fffffff).toInt() }
            val shapes = listOf("distinct" to a.copyOf(), "square" to a,
                "ratio2" to IntArray(2 * size) { 0x3fffffff }).let { if (args[0] == "reverse") it.reversed() else it }
            for ((shape, b) in shapes) {
                val expected = MagnitudeMultiplication.multiply(a, b, Int.MAX_VALUE, Int.MAX_VALUE)
                calibration539Verify(a, b, expected)
                fun serialFrame(): IntArray {
                    val frame = checkNotNull(MagnitudeMultiplication.prepareParallel539(a, b, policy))
                    return frame.combine(frame.child(0), frame.child(1), frame.child(2))
                }
                println("PAYLOAD539,$size/$shape,${checkNotNull(MagnitudeMultiplication.prepareParallel539(a, b, policy)).wordPayloadUpperBound539()},words_excludes_headers_scheduler_inputs")
                val variants = listOf("kernel", "frame", "worker1", "worker2").let { if (args[0] == "reverse") it.reversed() else it }
                for (variant in variants) {
                    val id = "$size/$shape/$variant"
                    val workers = when (variant) { "worker1" -> 1; "worker2" -> 2; else -> 0 }
                    if (workers > 0 && Runtime.getRuntime().availableProcessors() <= workers) {
                        println("INELIGIBLE539,$id,processor_budget")
                        continue
                    }
                    // Each setup observation uses a genuinely fresh owned pool.
                    if (workers > 0) {
                        var fresh: ParallelCandidate539? = null
                        val setup = observe(1) {
                            ParallelCandidate539(workers).also { fresh = it }.use { it.multiply(a, b, policy) }
                        }
                        check(checkNotNull(fresh).awaitTermination539(5)) { "Fresh pool did not terminate" }
                        check(expected.contentEquals(escape))
                        println("SETUP539,$id,${setup.wall},${setup.cpu},${setup.allocated},${setup.heapPeakUpperBound}")
                    }
                    val candidate = if (workers > 0) ParallelCandidate539(workers) else null
                    try {
                        val operation: () -> IntArray = when (variant) {
                            "kernel" -> { { MagnitudeMultiplication.multiplyConfigured539(a, b, policy) } }
                            "frame" -> ::serialFrame
                            else -> { { checkNotNull(candidate).multiply(a, b, policy) } }
                        }
                        check(expected.contentEquals(operation()))
                        println("CHECKPAR539,$id,PASS")
                        val warmStart = System.nanoTime()
                        while (System.nanoTime() - warmStart < 200_000_000L) escape = operation()
                        var count = 1
                        while (true) {
                            val elapsed = batch(count, operation)
                            if (elapsed >= 20_000_000L || count >= 1_048_576) break
                            count *= 2
                        }
                        repeat(3) { batch(count, operation) }
                        repeat(samples) { sample ->
                            val observed = observe(count, operation)
                            // Full result comparison, rather than a hash-only check, outside timing.
                            check(expected.contentEquals(escape))
                            println("PAR539,$id,$sample,$count,${observed.wall},${observed.cpu},${observed.allocated},${observed.heapPeakUpperBound},${escape!!.contentHashCode()}")
                            System.out.flush()
                        }
                    } finally {
                        candidate?.close()
                        if (candidate != null) check(candidate.awaitTermination539(5)) { "Steady pool did not terminate" }
                        escape = null
                    }
                }
            }
        }
        verifyArtifacts()
        println("PASS_PAR539,artifact_recheck")
    }

    private fun batch(count: Int, operation: () -> IntArray): Long {
        val begin = System.nanoTime()
        repeat(count) { escape = operation() }
        return System.nanoTime() - begin
    }
    private fun observe(count: Int, operation: () -> IntArray): Observation {
        // Bean sampling is outside the arithmetic wall interval; all-thread allocation
        // includes accounting/runtime/background costs, equally for every variant.
        pools.forEach { it.resetPeakUsage() }
        val allocated = allocations.totalThreadAllocatedBytes
        val cpuBefore = cpu.processCpuTime
        val elapsed = batch(count, operation)
        val cpuAfter = cpu.processCpuTime
        val after = allocations.totalThreadAllocatedBytes
        check(after >= allocated && cpuAfter >= cpuBefore && cpuBefore >= 0)
        return Observation(elapsed, cpuAfter - cpuBefore, after - allocated, pools.sumOf { it.peakUsage.used })
    }
}

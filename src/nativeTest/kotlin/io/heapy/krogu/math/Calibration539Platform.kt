@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.experimental.ExperimentalNativeApi::class, kotlin.native.runtime.NativeRuntimeApi::class)
package io.heapy.krogu.math
import kotlinx.cinterop.toKString
import kotlin.native.Platform
import kotlin.native.runtime.GC
import platform.posix.getenv
import platform.posix.clock
import platform.posix.CLOCKS_PER_SEC
internal actual fun calibration539Arguments(): String? = getenv("KROGU539_ARGS")?.toKString()
internal actual fun calibration539Platform() = "Native ${Platform.osFamily} ${Platform.cpuArchitecture} processors=${Platform.getAvailableProcessors()}"
internal actual fun calibration539CpuNs(): Long? {
    val ticks = clock().toLong()
    return if (ticks < 0) null else ticks * (1_000_000_000L / CLOCKS_PER_SEC.toLong())
}
internal actual fun calibration539CallerAllocatedBytes(): Long? = null
// Existing last-GC total is merely a coarse sample, not this invocation's peak.
internal actual fun calibration539HeapBytes(): Long? = GC.lastGCInfo?.memoryUsageAfter?.get("heap")?.totalObjectsSizeBytes
internal actual fun calibration539Verify(a: IntArray, b: IntArray, result: IntArray) { /* common school check */ }

internal actual fun calibration539NativeCases(): List<Calibration539Case> = buildList {
    val policy = MultiplicationPolicy539(64, 16, 384)
    val processors = Platform.getAvailableProcessors()
    for (size in listOf(256, 1024, 4096, 8192, 16384)) {
        val a = IntArray(size) { ((it.toLong() * 104729 + 31) and 0x3fffffff).toInt() }
        for ((shape, b) in listOf("distinct" to a.copyOf(), "square" to a,
            "ratio2" to IntArray(2 * size) { 0x3fffffff })) {
            val expected = MagnitudeMultiplication.multiply(a, b, Int.MAX_VALUE, Int.MAX_VALUE)
            fun serialFrame(): IntArray {
                val frame = checkNotNull(MagnitudeMultiplication.prepareParallel539(a, b, policy))
                return frame.combine(frame.child(0), frame.child(1), frame.child(2))
            }
            add(Calibration539Case("native-parallel/$size/$shape/kernel",
                { MagnitudeMultiplication.multiplyConfigured539(a, b, policy) }, {
                    check(expected.contentEquals(MagnitudeMultiplication.multiplyConfigured539(a, b, policy)))
                }))
            add(Calibration539Case("native-parallel/$size/$shape/frame", ::serialFrame,
                { check(expected.contentEquals(serialFrame())) }))
            for (workers in 1..2) {
                if (processors <= workers) {
                    println("INELIGIBLE_NATIVE539,$size/$shape/worker$workers,processor_budget")
                    continue
                }
                val candidate = ParallelNativeCandidate539(workers)
                add(Calibration539Case("native-parallel/$size/$shape/worker$workers",
                    { candidate.multiply(a, b, policy) }, { check(expected.contentEquals(candidate.multiply(a, b, policy))) }))
            }
        }
    }
}

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.experimental.ExperimentalNativeApi::class, kotlin.native.runtime.NativeRuntimeApi::class)
package io.heapy.krogu.math
import kotlinx.cinterop.toKString
import kotlin.native.Platform
import kotlin.native.runtime.GC
import platform.posix.getenv
import platform.posix.clock
import platform.posix.CLOCKS_PER_SEC
internal actual fun calibration539Arguments(): String? = getenv("KROGU539_ARGS")?.toKString()
internal actual fun calibration539Platform() = "Native ${Platform.osFamily} ${Platform.cpuArchitecture}"
internal actual fun calibration539CpuNs(): Long? {
    val ticks = clock().toLong()
    return if (ticks < 0) null else ticks * (1_000_000_000L / CLOCKS_PER_SEC.toLong())
}
internal actual fun calibration539CallerAllocatedBytes(): Long? = null
// Existing last-GC total is merely a coarse sample, not this invocation's peak.
internal actual fun calibration539HeapBytes(): Long? = GC.lastGCInfo?.memoryUsageAfter?.get("heap")?.totalObjectsSizeBytes
internal actual fun calibration539Verify(a: IntArray, b: IntArray, result: IntArray) { /* common school check */ }

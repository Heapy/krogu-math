@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class, kotlin.native.runtime.NativeRuntimeApi::class, kotlin.ExperimentalStdlibApi::class)
package bench
import kotlin.native.Platform
import kotlin.native.runtime.GC
actual fun platformArguments(args: Array<String>) = args
actual fun implementations() = listOf("krogu" to kroguWorkloads())
actual fun platformDescription() = "Native ${Platform.osFamily} ${Platform.cpuArchitecture}"
actual fun allocatedBytes(): Long? = null
actual fun retainedHeapBytes(): Long? {
    GC.collect()
    return GC.lastGCInfo?.memoryUsageAfter?.get("heap")?.totalObjectsSizeBytes
}

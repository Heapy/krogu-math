@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class, kotlin.native.runtime.NativeRuntimeApi::class, kotlin.ExperimentalStdlibApi::class)
package bench

import kotlin.native.runtime.GC

internal actual fun compactMemory529(): CompactMemory529 {
    GC.collect()
    return CompactMemory529("native_last_gc_total_objects_size_bytes", GC.lastGCInfo?.memoryUsageAfter?.get("heap")?.totalObjectsSizeBytes, null, null)
}

internal expect fun compactNativeSnapshot529(label: String)

internal actual fun compactSnapshot529(label: String): Unit = compactNativeSnapshot529(label)

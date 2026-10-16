@file:kotlin.jvm.JvmName("Compact529JvmPlatformKt")

package bench

import java.lang.management.ManagementFactory

internal actual fun compactMemory529(): CompactMemory529 {
    System.gc()
    return CompactMemory529("jvm_total_heap_used_after_gc_request", ManagementFactory.getMemoryMXBean().heapMemoryUsage.used, null, null)
}

internal actual fun compactSnapshot529(label: String): Unit = error("Node/V8 capture is not available on this target: $label")

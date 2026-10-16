@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class, kotlin.native.runtime.NativeRuntimeApi::class, kotlin.ExperimentalStdlibApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)
package bench

import kotlin.native.runtime.GC
import kotlin.native.runtime.Debugging
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.posix.O_CREAT
import platform.posix.O_EXCL
import platform.posix.O_WRONLY
import platform.posix.RLIMIT_FSIZE
import platform.posix.close
import platform.posix.fstat
import platform.posix.getenv
import platform.posix.getrlimit
import platform.posix.open
import platform.posix.rlimit
import platform.posix.setrlimit
import platform.posix.stat

// A unique typed global root makes the retained cohort identifiable in the dump.
private class NativeGraphRoot529(val phaseId: Int, val values: Array<Any>)
private var nativeGraphRoot529: NativeGraphRoot529? = null

internal actual fun compactNativeSnapshot529(label: String) {
    val phases = listOf("unit", "integer_small", "integer_wide", "decimal_small", "decimal_wide", "control_bytes0", "control_bytes256", "control_ints64")
    val phase = phases.indexOf(label)
    require(phase >= 0) { "Unexpected Native graph phase" }
    val directory = checkNotNull(getenv("KROGU_NATIVE_GRAPH_OUT")) { "Explicit Native graph output required" }.toKString()
    require(directory.startsWith('/') && directory.isNotEmpty())
    val values = compactRetainedRoots529()
    require(values.size == 256) { "Native graph pilot requires 256 roots" }
    // Replace the previous marker before collecting, so it cannot retain an old cohort.
    nativeGraphRoot529 = NativeGraphRoot529(phase, values)
    GC.collect()
    val path = directory.trimEnd('/') + "/$phase-$label.kdump"
    memScoped {
        val previous = alloc<rlimit>()
        check(getrlimit(RLIMIT_FSIZE, previous.ptr) == 0) { "Cannot read owned file-size limit" }
        val limited = alloc<rlimit>()
        limited.rlim_cur = minOf(previous.rlim_cur, 134_217_728uL)
        limited.rlim_max = previous.rlim_max
        var descriptor = -1
        var changedLimit = false
        var primary: Throwable? = null
        try {
            check(setrlimit(RLIMIT_FSIZE, limited.ptr) == 0) { "Cannot bound Native dump file" }
            changedLimit = true
            descriptor = open(path, O_WRONLY or O_CREAT or O_EXCL, 0x180)
            check(descriptor >= 0) { "Cannot create unique Native dump file" }
            check(Debugging.dumpMemory(descriptor.toLong())) { "Native dumpMemory returned false" }
            val info = alloc<stat>()
            check(fstat(descriptor, info.ptr) == 0 && info.st_size in 1..134_217_728L) { "Invalid Native dump size" }
        } catch (failure: Throwable) {
            primary = failure
            throw failure
        } finally {
            // Perform both nonthrowing POSIX cleanup calls before reporting either error.
            val closed = if (descriptor >= 0) close(descriptor) else 0
            val restored = if (changedLimit) setrlimit(RLIMIT_FSIZE, previous.ptr) else 0
            if (closed != 0 || restored != 0) {
                val cleanup = IllegalStateException("Native dump descriptor/limit cleanup failed")
                if (primary != null) primary.addSuppressed(cleanup) else throw cleanup
            }
        }
    }
    println("NATIVE_DUMP,$phase,$label")
}

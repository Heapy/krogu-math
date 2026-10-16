package bench

import java.lang.management.ManagementFactory

actual fun platformArguments(args: Array<String>) = args
actual fun implementations() = listOf("krogu" to kroguWorkloads(), "jdk" to jdkWorkloads())
actual fun platformDescription() = "JVM ${System.getProperty("java.vendor")} ${System.getProperty("java.runtime.version")} ${System.getProperty("os.arch")}"
private val allocationBean = (ManagementFactory.getThreadMXBean() as? com.sun.management.ThreadMXBean)?.also {
    if (it.isThreadAllocatedMemorySupported) it.isThreadAllocatedMemoryEnabled = true
}
actual fun allocatedBytes(): Long? = allocationBean?.takeIf { it.isThreadAllocatedMemoryEnabled }
    ?.getThreadAllocatedBytes(Thread.currentThread().threadId())?.takeIf { it >= 0 }
actual fun retainedHeapBytes(): Long? {
    System.gc()
    return ManagementFactory.getMemoryMXBean().heapMemoryUsage.used
}

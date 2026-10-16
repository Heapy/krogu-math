package io.heapy.krogu.math

import java.lang.management.ManagementFactory

internal actual fun calibration539Arguments(): String? = System.getProperty("krogu539.args")
internal actual fun calibration539Platform() = "JVM ${System.getProperty("java.vendor")} ${System.getProperty("java.runtime.version")} ${System.getProperty("os.arch")}"
private val cpu539 = ManagementFactory.getOperatingSystemMXBean() as? com.sun.management.OperatingSystemMXBean
private val allocation539 = (ManagementFactory.getThreadMXBean() as? com.sun.management.ThreadMXBean)?.also {
    if (it.isThreadAllocatedMemorySupported) it.isThreadAllocatedMemoryEnabled = true
}
internal actual fun calibration539CpuNs(): Long? = cpu539?.processCpuTime?.takeIf { it >= 0 }
internal actual fun calibration539CallerAllocatedBytes(): Long? = allocation539?.takeIf { it.isThreadAllocatedMemoryEnabled }
    ?.getThreadAllocatedBytes(Thread.currentThread().threadId())?.takeIf { it >= 0 }
internal actual fun calibration539HeapBytes(): Long? = ManagementFactory.getMemoryMXBean().heapMemoryUsage.used
internal actual fun calibration539Verify(a: IntArray, b: IntArray, result: IntArray) {
    fun number(words: IntArray): java.math.BigInteger {
        var value = java.math.BigInteger.ZERO
        for (word in words.reversed()) value = value.shiftLeft(30).add(java.math.BigInteger.valueOf(word.toLong()))
        return value
    }
    check(number(a).multiply(number(b)) == number(result)) { "JDK public product oracle" }
}

/** Direct fork entry, so Gradle/compiler process CPU and RSS stay outside the measurement. */
object MultiplicationCalibration539JvmMain {
    @JvmStatic fun main(args: Array<String>) {
        require(args.size == 1)
        MultiplicationCampaign539.run(args.single())
    }
}

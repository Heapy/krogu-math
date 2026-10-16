package io.heapy.krogu.math
internal actual fun calibration539Arguments(): String? = js("process.env.KROGU539_ARGS || null") as String?
internal actual fun calibration539Platform(): String = "JS Node " + (js("process.version") as String)
private fun cpu539(): Double = js("(function () { const c = process.cpuUsage(); return (c.user + c.system) * 1000; })()") as Double
internal actual fun calibration539CpuNs(): Long? = cpu539().toLong()
internal actual fun calibration539CallerAllocatedBytes(): Long? = null
internal actual fun calibration539HeapBytes(): Long? = (js("process.memoryUsage().heapUsed") as Double).toLong()
internal actual fun calibration539Verify(a: IntArray, b: IntArray, result: IntArray) { /* common school check */ }

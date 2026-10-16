package io.heapy.krogu.math
import kotlin.js.js
internal actual fun calibration539Arguments(): String? = js("process.env.KROGU539_ARGS || null")
internal actual fun calibration539Platform(): String = "Wasm Node " + node539Version()
private fun node539Version(): String = js("process.version")
private fun node539Cpu(): Double = js("(function () { const c = process.cpuUsage(); return (c.user + c.system) * 1000; })()")
internal actual fun calibration539CpuNs(): Long? = node539Cpu().toLong()
internal actual fun calibration539CallerAllocatedBytes(): Long? = null
// Host heapUsed does not establish WasmGC owned-object coverage.
internal actual fun calibration539HeapBytes(): Long? = null
internal actual fun calibration539Verify(a: IntArray, b: IntArray, result: IntArray) { /* common school check */ }

internal actual fun calibration539NativeCases(): List<Calibration539Case> = emptyList()

internal actual fun registerPublic539(handler: (Int) -> Unit): Unit = js("globalThis.__krogu539PublicInvoke = handler")
internal actual fun publicInput539(index: Int): IntArray =
    IntArray(publicLength539(index).also { require(it in 0..131074) }) { publicWord539(index, it) }
private fun publicLength539(side: Int): Int = js("globalThis.__krogu539PublicRequest.inputs[side].length")
private fun publicWord539(side: Int, index: Int): Int = js("globalThis.__krogu539PublicRequest.inputs[side][index]")
internal actual fun publicSame539(): Boolean = js("globalThis.__krogu539PublicRequest.same")
internal actual fun publicHash539(value: Int): Unit = js("globalThis.__krogu539PublicHash = value")

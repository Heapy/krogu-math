package bench

import kotlin.js.js

private fun gcAvailable529(): Boolean = js("typeof global.gc === 'function'")
private fun collect529(): Unit = js("global.gc()")
private fun hostHeap529(): Double = js("process.memoryUsage().heapUsed")
private fun rss529(): Double = js("process.memoryUsage().rss")
internal actual fun compactMemory529(): CompactMemory529 {
    check(gcAvailable529()) { "Run Node with --expose-gc" }
    collect529()
    // Kotlin/Wasm uses WasmGC. Node-reported heap coverage must be calibrated
    // with retained array controls; neither this value nor RSS is an exact root graph.
    return CompactMemory529("wasm_node_heap_coverage_unverified_and_process_rss", hostHeap529().toLong(), null, rss529().toLong())
}

private fun capture529(label: String): Unit = js("globalThis.__compact529Snapshot(label)")
private fun captureAvailable529(): Boolean = js("typeof globalThis.__compact529Snapshot === 'function'")
internal actual fun compactSnapshot529(label: String) {
    check(captureAvailable529()) { "Use the owned heap-capture runner" }
    capture529(label)
}

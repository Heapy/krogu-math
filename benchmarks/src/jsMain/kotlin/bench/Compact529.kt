package bench

internal actual fun compactMemory529(): CompactMemory529 {
    check(js("typeof global.gc === 'function'") as Boolean) { "Run Node with --expose-gc" }
    js("global.gc()")
    return CompactMemory529("node_heap_used_and_array_buffers_after_gc", (js("process.memoryUsage().heapUsed") as Double).toLong(),
        (js("process.memoryUsage().arrayBuffers") as Double).toLong(), (js("process.memoryUsage().rss") as Double).toLong())
}

internal actual fun compactSnapshot529(label: String) {
    check(js("typeof globalThis.__compact529Snapshot === 'function'") as Boolean) { "Use the owned heap-capture runner" }
    js("globalThis.__compact529Snapshot(label)")
}

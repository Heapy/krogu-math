package nodeworker539
private val hostData539: dynamic = js("process.getBuiltinModule('worker_threads').workerData")
internal actual fun register539(handler: () -> Unit) { registerHost539(hostData539, handler) }
private fun registerHost539(data: dynamic, handler: () -> Unit): Unit = js("data.invoke = handler")
internal actual fun length539(side: Int): Int = lengthHost539(hostData539, side)
private fun lengthHost539(data: dynamic, side: Int): Int = js("(side === 0 ? data.request.a : data.request.b).length") as Int
internal actual fun word539(side: Int, index: Int): Int = wordHost539(hostData539, side, index)
private fun wordHost539(data: dynamic, side: Int, index: Int): Int = js("(side === 0 ? data.request.a : data.request.b)[index]") as Int
internal actual fun parameter539(index: Int): Int = parameterHost539(hostData539, index)
private fun parameterHost539(data: dynamic, index: Int): Int = js("index === 0 ? data.request.k : data.request.s") as Int
internal actual fun same539(): Boolean = sameHost539(hostData539)
private fun sameHost539(data: dynamic): Boolean = js("data.request.same") as Boolean
internal actual fun output539(words: IntArray) {
    allocate539(hostData539, words.size)
    words.forEachIndexed { index, word -> write539(hostData539, index, word) }
}
private fun allocate539(data: dynamic, size: Int): Unit = js("data.result = new Int32Array(size)")
private fun write539(data: dynamic, index: Int, word: Int): Unit = js("data.result[index] = word")

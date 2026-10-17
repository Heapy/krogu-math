package nodeworker539
import kotlin.js.JsAny
import kotlin.js.js
private fun hostDataHost539(): JsAny = js("process.getBuiltinModule('worker_threads').workerData")
private val hostData539: JsAny = hostDataHost539()
internal actual fun register539(handler: () -> Unit) { registerHost539(hostData539, handler) }
private fun registerHost539(data: JsAny, handler: () -> Unit): Unit = js("data.invoke = handler")
internal actual fun length539(side: Int): Int = lengthHost539(hostData539, side)
private fun lengthHost539(data: JsAny, side: Int): Int = js("(side === 0 ? data.request.a : data.request.b).length")
internal actual fun word539(side: Int, index: Int): Int = wordHost539(hostData539, side, index)
private fun wordHost539(data: JsAny, side: Int, index: Int): Int = js("(side === 0 ? data.request.a : data.request.b)[index]")
internal actual fun parameter539(index: Int): Int = parameterHost539(hostData539, index)
private fun parameterHost539(data: JsAny, index: Int): Int = js("index === 0 ? data.request.k : data.request.s")
internal actual fun same539(): Boolean = sameHost539(hostData539)
private fun sameHost539(data: JsAny): Boolean = js("data.request.same")
internal actual fun output539(words: IntArray) {
    allocate539(hostData539, words.size)
    words.forEachIndexed { index, word -> write539(hostData539, index, word) }
}
private fun allocate539(data: JsAny, size: Int): Unit = js("data.result = new Int32Array(size)")
private fun write539(data: JsAny, index: Int, word: Int): Unit = js("data.result[index] = word")

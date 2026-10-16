package calibration539

import kotlin.js.js

internal actual fun input539(index: Int): IntArray =
    IntArray(length539(index).also { require(it in 0..131074) }) { word539(index, it) }
internal actual fun same539(): Boolean = js("globalThis.__krogu539Request.same")
internal actual fun parameter539(index: Int): Int = js("globalThis.__krogu539Request.policy[index]")
internal actual fun childIndex539(): Int = js("globalThis.__krogu539Request.child")
internal actual fun clearOutputs539(): Unit = js("globalThis.__krogu539Result = []")
internal actual fun output539(index: Int, words: IntArray) {
    allocate539(index, words.size)
    words.forEachIndexed { offset, value -> write539(index, offset, value) }
}
internal actual fun register539(handler: (Int) -> Unit): Unit = js("globalThis.__krogu539Invoke = handler")
// Explicit copies: no cross-instance WasmGC sharing. Per-word interop cost belongs
// to this candidate and cannot establish a universal transport lower bound.
private fun length539(side: Int): Int = js("globalThis.__krogu539Request.inputs[side].length")
private fun word539(side: Int, index: Int): Int = js("globalThis.__krogu539Request.inputs[side][index]")
private fun allocate539(index: Int, size: Int): Unit = js("globalThis.__krogu539Result[index] = new Int32Array(size)")
private fun write539(index: Int, offset: Int, value: Int): Unit = js("globalThis.__krogu539Result[index][offset] = value")

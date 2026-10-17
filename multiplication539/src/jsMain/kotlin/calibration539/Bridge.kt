package calibration539

import kotlin.js.unsafeCast

// Host-owned immutable request buffers are borrowed within each synchronous call.
// Only copiedChildInputs539 buffers are ever transferred/detached by the host.
internal actual fun input539(index: Int): IntArray = (js("globalThis.__krogu539Request.inputs[index]") as Any).unsafeCast<IntArray>()
internal actual fun same539(): Boolean = js("globalThis.__krogu539Request.same") as Boolean
internal actual fun parameter539(index: Int): Int = js("globalThis.__krogu539Request.policy[index]") as Int
internal actual fun childIndex539(): Int = js("globalThis.__krogu539Request.child") as Int
internal actual fun clearOutputs539() { js("globalThis.__krogu539Result = []") }
internal actual fun output539(index: Int, words: IntArray) { js("globalThis.__krogu539Result[index] = words") }
internal actual fun register539(handler: (Int) -> Unit) { js("globalThis.__krogu539Invoke = handler") }

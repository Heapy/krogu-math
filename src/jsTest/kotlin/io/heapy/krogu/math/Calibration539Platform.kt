package io.heapy.krogu.math
import kotlin.js.unsafeCast
internal actual fun calibration539Arguments(): String? = js("process.env.KROGU539_ARGS || null") as String?
internal actual fun calibration539Platform(): String = "JS Node " + (js("process.version") as String)
private fun cpu539(): Double = js("(function () { const c = process.cpuUsage(); return (c.user + c.system) * 1000; })()") as Double
internal actual fun calibration539CpuNs(): Long? = cpu539().toLong()
internal actual fun calibration539CallerAllocatedBytes(): Long? = null
internal actual fun calibration539HeapBytes(): Long? = (js("process.memoryUsage().heapUsed") as Double).toLong()
internal actual fun calibration539Verify(a: IntArray, b: IntArray, result: IntArray) {
    // Public Node BigInt black-box oracle, never used by a timed operation.
    val matches = js("""(function (left, right, product) {
        function integer(words) {
            var value = BigInt(0);
            for (var i = words.length - 1; i >= 0; i--)
                value = (value << BigInt(30)) + BigInt(words[i]);
            return value;
        }
        return integer(left) * integer(right) === integer(product);
    })(a, b, result)""") as Boolean
    check(matches) { "Public Node BigInt product oracle mismatch" }
}

internal actual fun calibration539NativeCases(): List<Calibration539Case> = emptyList()

internal actual fun registerPublic539(handler: (Int) -> Unit) { js("globalThis.__krogu539PublicInvoke = handler") }
internal actual fun publicInput539(index: Int): IntArray =
    (js("globalThis.__krogu539PublicRequest.inputs[index]") as Any).unsafeCast<IntArray>()
internal actual fun publicSame539(): Boolean = js("globalThis.__krogu539PublicRequest.same") as Boolean
internal actual fun publicHash539(value: Int) { js("globalThis.__krogu539PublicHash = value") }

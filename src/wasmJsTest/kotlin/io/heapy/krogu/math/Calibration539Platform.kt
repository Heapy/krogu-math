package io.heapy.krogu.math
import kotlin.js.js
import kotlin.js.JsAny
internal actual fun calibration539Arguments(): String? = js("process.env.KROGU539_ARGS || null")
internal actual fun calibration539Platform(): String = "Wasm Node " + node539Version()
private fun node539Version(): String = js("process.version")
private fun node539Cpu(): Double = js("(function () { const c = process.cpuUsage(); return (c.user + c.system) * 1000; })()")
internal actual fun calibration539CpuNs(): Long? = node539Cpu().toLong()
internal actual fun calibration539CallerAllocatedBytes(): Long? = null
// Host heapUsed does not establish WasmGC owned-object coverage.
internal actual fun calibration539HeapBytes(): Long? = null
internal actual fun calibration539Verify(a: IntArray, b: IntArray, result: IntArray) {
    fun host(words: IntArray): JsAny {
        val output = oracleArray539(words.size)
        words.forEachIndexed { index, word -> oracleWrite539(output, index, word) }
        return output
    }
    check(oracleProduct539(host(a), host(b), host(result))) { "Public Node BigInt product oracle mismatch" }
}
private fun oracleArray539(size: Int): JsAny = js("new Int32Array(size)")
private fun oracleWrite539(output: JsAny, index: Int, word: Int): Unit = js("output[index] = word")
private fun oracleProduct539(a: JsAny, b: JsAny, result: JsAny): Boolean = js("""(function(left,right,product) {
    function integer(words) {
        var value=BigInt(0);
        for(var index=words.length-1;index>=0;index--) value=(value<<BigInt(30))+BigInt(words[index]);
        return value;
    }
    return integer(left)*integer(right)===integer(product);
})(a,b,result)""")

internal actual fun calibration539NativeCases(): List<Calibration539Case> = emptyList()

internal actual fun registerPublic539(handler: (Int) -> Unit): Unit = js("globalThis.__krogu539PublicInvoke = handler")
internal actual fun publicInput539(index: Int): IntArray =
    IntArray(publicLength539(index).also { require(it in 0..131074) }) { publicWord539(index, it) }
private fun publicLength539(side: Int): Int = js("globalThis.__krogu539PublicRequest.inputs[side].length")
private fun publicWord539(side: Int, index: Int): Int = js("globalThis.__krogu539PublicRequest.inputs[side][index]")
internal actual fun publicSame539(): Boolean = js("globalThis.__krogu539PublicRequest.same")
internal actual fun publicHash539(value: Int): Unit = js("globalThis.__krogu539PublicHash = value")

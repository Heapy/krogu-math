package bench
import kotlin.js.js
private fun nodeArgument(index: Int): String = js("process.argv[index + 2] || ''")
actual fun platformArguments(args: Array<String>): Array<String> = Array(4) { nodeArgument(it) }.takeWhile(String::isNotEmpty).toTypedArray()
actual fun implementations() = listOf("krogu" to kroguWorkloads())
actual fun platformDescription() = "Wasm (Node version recorded in environment.txt)"
actual fun allocatedBytes(): Long? = null
// Node heapUsed excludes the Kotlin/Wasm linear heap, so do not mislabel it as retained memory.
actual fun retainedHeapBytes(): Long? = null

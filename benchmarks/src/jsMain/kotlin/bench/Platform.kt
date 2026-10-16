package bench
actual fun platformArguments(args: Array<String>): Array<String> = js("process.argv.slice(2)") as Array<String>
actual fun implementations() = listOf("krogu" to kroguWorkloads())
actual fun platformDescription(): String = "JS Node " + js("process.version") as String
actual fun allocatedBytes(): Long? = null
actual fun retainedHeapBytes(): Long? {
    js("if (global.gc) global.gc()")
    return (js("process.memoryUsage().heapUsed") as Double).toLong()
}

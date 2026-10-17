package preparation598
private var escaped598: Array<Any?>? = null
private fun nodeVersion598(): String = js("process.versions.node")
private fun cpu598(): Double = js("(function(){const u=process.cpuUsage();return (u.user+u.system)*1000})()")
private fun rss598(): Double = js("process.resourceUsage().maxRSS * 1024")
actual fun portableRuntime598(): String { check(nodeVersion598()=="24.16.0"); return "Wasm/Node24.16.0" }
internal actual fun publishRing598(ring:Array<Any?>) { escaped598=ring }
internal actual fun counters598(destination:Counters598,before:Boolean) {
    destination.cpuNs=cpu598().toLong(); destination.allocatedBytes=-1
    destination.heapUsedBytes=-1; destination.rssPeakBytes=rss598().toLong()
}
internal actual fun metricScope598()="Node process CPU(user+system); process maxRSS through snapshot; Wasm heap/allocation/owned-retained NA; JS heap is not Wasm owned graph; adapter/ring escape overhead included"
internal actual fun selectedCase598(peer:String,id:String):TimedCase598 = when(peer) {
    "krogu-final539-PENDING" -> coreCase598(KroguAdapter,id)
    "ionspin-bignum-0.3.10" -> coreCase598(IonsPinAdapter,id)
    else -> error("Peer not published for Wasm:$peer")
}
internal actual fun verifyAll598() { verifyPortableUntimed598() }

private fun nodeArgCount598(): Int = js("process.argv.length - 2")
private fun nodeArg598(index: Int): String = js("process.argv[index + 2]")
internal actual fun timingArguments598(supplied: Array<String>): Array<String> {
    val count=nodeArgCount598();require(count==4) { "peer workload fork order" }
    return Array(count) { nodeArg598(it) }
}

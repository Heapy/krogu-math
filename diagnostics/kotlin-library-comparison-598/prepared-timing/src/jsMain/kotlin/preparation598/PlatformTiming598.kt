package preparation598
private var escaped598: Array<Any?>? = null
private fun nodeVersion598(): String = js("process.versions.node") as String
private fun cpu598(): Double = js("(function(){const u=process.cpuUsage();return (u.user+u.system)*1000})()") as Double
private fun heap598(): Double = js("process.memoryUsage().heapUsed") as Double
private fun rss598(): Double = js("process.resourceUsage().maxRSS * 1024") as Double
actual fun portableRuntime598(): String { check(nodeVersion598()=="24.16.0"); return "JS/Node24.16.0" }
internal actual fun publishRing598(ring:Array<Any?>) { escaped598=ring }
internal actual fun counters598(destination:Counters598,before:Boolean) {
    destination.cpuNs=cpu598().toLong(); destination.allocatedBytes=-1
    destination.heapUsedBytes=heap598().toLong(); destination.rssPeakBytes=rss598().toLong()
}
internal actual fun metricScope598()="Node process CPU(user+system); JS whole-process used-heap snapshots; process maxRSS through snapshot; allocation/owned-retained NA; adapter/ring escape overhead included"
internal actual fun selectedCase598(peer:String,id:String):TimedCase598 = when(peer) {
    "krogu-final539-PENDING" -> coreCase598(KroguAdapter,id)
    "ionspin-bignum-0.3.10" -> coreCase598(IonsPinAdapter,id)
    else -> error("Peer not published for JS:$peer")
}
internal actual fun verifyAll598() { verifyPortableUntimed598() }

// Kotlin/JS executable mainWrapper passes []; Node owns the actual command arguments.
internal actual fun timingArguments598(supplied: Array<String>): Array<String> = js("process.argv.slice(2)") as Array<String>

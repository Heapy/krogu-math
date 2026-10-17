package preparation598

import java.lang.management.ManagementFactory

private val thread598 = ManagementFactory.getThreadMXBean()
private val allocation598 = thread598 as? com.sun.management.ThreadMXBean
@Volatile private var escaped598: Array<Any?>? = null
actual fun portableRuntime598(): String {
    check(System.getProperty("java.vendor").contains("BellSoft",true))
    check(System.getProperty("java.runtime.version") == "25.0.4+9-LTS")
    return "JVM/BellSoft25.0.4+9-LTS"
}
internal actual fun publishRing598(ring: Array<Any?>) { escaped598=ring }
internal actual fun counters598(destination: Counters598, before: Boolean) {
    fun alloc() = if(allocation598?.isThreadAllocatedMemorySupported == true && allocation598.isThreadAllocatedMemoryEnabled)
        allocation598.getThreadAllocatedBytes(Thread.currentThread().threadId()) else -1L
    if(!before) destination.allocatedBytes=alloc()
    destination.cpuNs=if(thread598.isCurrentThreadCpuTimeSupported && thread598.isThreadCpuTimeEnabled) thread598.currentThreadCpuTime else -1L
    val runtime=Runtime.getRuntime(); destination.heapUsedBytes=runtime.totalMemory()-runtime.freeMemory()
    destination.rssPeakBytes=-1L
    if(before) destination.allocatedBytes=alloc()
}
internal actual fun metricScope598() = "JVM current-thread CPU/allocation if supported+enabled; whole-process used-heap snapshots; no per-value retained claim; RSS peak only external time-l; adapter/ring escape overhead included"
private fun <I:Any,D:Any> selected598(adapter:PeerAdapter<I,D>,id:String):TimedCase598 {
    if(!id.startsWith("fixed-scale/")) return coreCase598(adapter,id)
    val fixture=FixtureCorpus.fixedScale.single { id == "fixed-scale/${it.id}" }
    return fixedCase598(adapter,id,fixture.left,fixture.right,fixture.scale,fixture.expected)
}
internal actual fun selectedCase598(peer:String,id:String):TimedCase598 = when(peer) {
    "jdk-bellsoft-25.0.4+9" -> selected598(JdkAdapter,id)
    "krogu-final539-PENDING" -> selected598(KroguAdapter,id)
    "ionspin-bignum-0.3.10" -> { require(!id.startsWith("fixed-scale/")); selected598(IonsPinAdapter,id) }
    "kbignum-0.0.20-desktop" -> selected598(KBigNumAdapter,id)
    else -> error("Unknown peer:$peer")
}
internal actual fun verifyAll598() {
    // Independently qualify the portable literal constants via public JDK, untimed.
    for(f in integerFixtures598) {
        val a=JdkAdapter.integer(f.a); val b=JdkAdapter.integer(f.b)
        check(a.add(b).toString()==f.sum && a.subtract(b).toString()==f.difference && a.multiply(b).toString()==f.product && a.multiply(a).toString()==f.square)
    }
    for(f in decimalFixtures598) {
        val a=JdkAdapter.decimal(f.a); val b=JdkAdapter.decimal(f.b)
        check(a.add(b).compareTo(JdkAdapter.decimal(f.sum))==0 && a.subtract(b).compareTo(JdkAdapter.decimal(f.difference))==0 &&
            a.multiply(b).compareTo(JdkAdapter.decimal(f.product))==0 && a.multiply(a).compareTo(JdkAdapter.decimal(f.square))==0)
    }
    fun <I:Any,D:Any> gate(adapter:PeerAdapter<I,D>) {
        val report=verifyUntimed(adapter);check(report.observations.size >= 128)
        verifyTextWorkloads598(adapter)
        println("GATE_COUNT598\t${adapter.id}\t${report.observations.size}\t${report.unavailable.size}")
    }
    gate(JdkAdapter);gate(KroguAdapter);gate(IonsPinAdapter);gate(KBigNumAdapter)
}

internal actual fun timingArguments598(supplied: Array<String>): Array<String> = supplied

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package preparation598
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.RUSAGE_SELF
import platform.posix.getrusage
import platform.posix.rusage
private var escaped598: Array<Any?>? = null
actual fun portableRuntime598(): String="macOSArm64/KotlinNative2.4.21"
internal actual fun publishRing598(ring:Array<Any?>) { escaped598=ring }
internal actual fun counters598(destination:Counters598,before:Boolean) {
    memScoped {
        val usage=alloc<rusage>(); check(getrusage(RUSAGE_SELF,usage.ptr)==0)
        destination.cpuNs=(usage.ru_utime.tv_sec+usage.ru_stime.tv_sec)*1_000_000_000L + (usage.ru_utime.tv_usec+usage.ru_stime.tv_usec)*1000L
        destination.rssPeakBytes=usage.ru_maxrss // macOS bytes, not Linux KiB.
    }
    destination.allocatedBytes=-1; destination.heapUsedBytes=-1
}
internal actual fun metricScope598()="macOS process CPU(user+system); rusage peakRSS bytes through snapshot; allocation/heap/owned-retained NA; per-child external time-l adds completed-lifetime RSS; adapter/ring escape overhead included"
internal actual fun selectedCase598(peer:String,id:String):TimedCase598 = when(peer) {
    "krogu-final539-PENDING" -> coreCase598(KroguAdapter,id)
    "ionspin-bignum-0.3.10" -> coreCase598(IonsPinAdapter,id)
    else -> error("Peer not published for macOSArm64:$peer")
}
internal actual fun verifyAll598() { verifyPortableUntimed598() }

internal actual fun timingArguments598(supplied: Array<String>): Array<String> = supplied

package preparation598

import kotlin.time.TimeSource

/** One selected peer/workload per fresh child. No peer preparation before selection. */
internal data class TimedCase598(
    val peer: String, val id: String, val inputLeft: String, val inputRight: String,
    val relation: String, val expected: String, val operation: () -> Any,
    val checkResult: (Any) -> String, val checkInputs: () -> Unit,
)
internal class Counters598 {
    var cpuNs = -1L; var allocatedBytes = -1L; var heapUsedBytes = -1L; var rssPeakBytes = -1L
}
internal expect fun timingArguments598(supplied: Array<String>): Array<String>
internal expect fun selectedCase598(peer: String, id: String): TimedCase598
internal expect fun verifyAll598()
internal expect fun counters598(destination: Counters598, before: Boolean)
internal expect fun publishRing598(ring: Array<Any?>)
internal expect fun metricScope598(): String
private val clock598 = TimeSource.Monotonic.markNow()
private fun checksumString598(text:String)=text.fold(0){h,c->31*h+c.code}
private fun now598() = clock598.elapsedNow().inWholeNanoseconds

internal fun <I : Any, D : Any> coreCase598(adapter: PeerAdapter<I, D>, id: String): TimedCase598 {
    if(id.endsWith("/PARSE") || id.endsWith("/FORMAT")) {
        val t=textCase598(adapter,id)
        return TimedCase598(adapter.id,id,t.input,t.input,"unary-text",t.input,t.operation,t.checkResult,t.checkInput)
    }
    val parts = id.split('/'); require(parts.size == 3)
    val family = parts[0]; val fixtureId = parts[1]; val opName = parts[2]
    val f = (if (family == "int") integerFixtures598 else { require(family == "decimal"); decimalFixtures598 }).single { it.id == fixtureId }
    val op = if (opName == "self") ExactOperation.MULTIPLY else ExactOperation.valueOf(opName)
    val expected = when (opName) { "ADD" -> f.sum; "SUBTRACT" -> f.difference; "MULTIPLY" -> f.product; "self" -> f.square; else -> error("Unknown operation") }
    fun <V : Any> typed(parse: (String) -> V, text: (V) -> String, operation: (ExactOperation, V, V) -> V): TimedCase598 {
        val a = parse(f.a); val b = if (opName == "self") a else parse(f.b)
        val rightText = if (opName == "self") f.a else f.b
        // No observations/formatting on these operands before FIRST.
        fun canonical(value: String) = if (family == "int") value else canonicalDecimal598(value).let { "${it.first}@${it.second}" }
        return TimedCase598(adapter.id, id, f.a, rightText,
            if (opName == "self") "self-alias" else if (a === b) "binary-native-alias" else "binary-distinct", expected,
            operation = { operation(op, a, b) },
            checkResult = { value -> @Suppress("UNCHECKED_CAST") val actual = text(value as V)
                check(canonical(actual) == canonical(expected)) { "${adapter.id}:$id result mismatch:$actual vs$expected" }; canonical(actual) },
            checkInputs = { check(canonical(text(a)) == canonical(f.a) && canonical(text(b)) == canonical(rightText)) { "Input changed" } })
    }
    return if (family == "int") typed(adapter::integer, adapter::integerText, adapter::integerOperation)
        else typed(adapter::decimal, adapter::decimalText, adapter::decimalOperation)
}

internal fun <I : Any, D : Any> fixedCase598(adapter: PeerAdapter<I, D>, id: String, left: String, right: String, scale: Int, expected: String): TimedCase598 {
    check(adapter.fixedScaleHalfUp.eligibleAfterFixtureGate) { "Explicit fixed-scale capability unavailable" }
    val a = adapter.decimal(left); val b = adapter.decimal(right)
    fun canonical(text: String) = canonicalDecimal598(text).let { "${it.first}@${it.second}" }
    return TimedCase598(adapter.id,id,left,right,if(a === b) "binary-native-alias" else "binary-distinct",expected,
        operation = { adapter.divideFixedScaleHalfUp(a,b,scale) },
        checkResult = { value -> @Suppress("UNCHECKED_CAST") val text = adapter.decimalText(value as D)
            check(canonical(text) == canonical(expected)) { "Fixed-scale numeric mismatch" }; canonical(text) },
        checkInputs = { check(canonical(adapter.decimalText(a)) == canonical(left) && canonical(adapter.decimalText(b)) == canonical(right)) { "Input changed" } })
}

fun main(args: Array<String>) {
    val actualArgs = timingArguments598(args)
    require(actualArgs.size == 4) { "peer workload fork order" }
    val (peer, id, forkText, order) = actualArgs
    val fork = forkText.toInt().also { require(it in 0..2) }
    require(order == if (fork == 1) "reverse" else "forward")
    val runtime = portableRuntime598()
    println("IDENTITY598\t$runtime\t$peer\t$id\t$fork\t$order")
    val case = selectedCase598(peer,id) // Only this peer is accessed; parse/prepare is outside FIRST.
    check(case.peer == peer && case.id == id)
    val before = Counters598(); val after = Counters598(); val ring = arrayOfNulls<Any>(64)
    println("INPUT598\t${case.inputLeft}\t${case.inputRight}\t${case.relation}\t${case.expected}")
    println("SCOPE598\t${metricScope598()}")
    println("COLD598_SCOPE\tFRESH_CHILD_FIRST_EXPLICIT_SELECTED_OP_AFTER_DECLARED_PREPARATION\tPRISTINE_VM_OR_INTERNAL_CACHE_NA")
    fun available(value: Long) = if(value < 0) "NA" else value.toString()
    fun delta(a: Long, b: Long): String { if (a < 0 || b < 0) return "NA"; check(b >= a) { "Counter decreased" }; return (b-a).toString() }
    // Fresh child, first explicit selected public arithmetic call AFTER input parsing.
    // Parsing/class init may do internal work; no uninitialized-VM or parser-cold claim.
    counters598(before,true)
    val firstStart = now598()
    val first = case.operation()
    ring[0] = first; publishRing598(ring)
    val firstNs = now598()-firstStart
    counters598(after,false)
    check(firstNs > 0)
    val firstText = case.checkResult(first); case.checkInputs()
    println("FIRST598\t$peer\t$id\t$fork\t1\t$firstNs\t${delta(before.cpuNs,after.cpuNs)}\t${delta(before.allocatedBytes,after.allocatedBytes)}\t${available(before.heapUsedBytes)}\t${available(after.heapUsedBytes)}\t${available(after.rssPeakBytes)}\t${checksumString598(firstText)}")
    verifyAll598() // Full untimed gates AFTER FIRST, BEFORE any warm measurement.
    case.checkInputs()
    println("GATE598_PASS\t$peer\t$id\t$fork")
    val retainedFirstText=case.checkResult(first)
    fun batch(count: Int, sample: Int, record: Boolean): Long {
        counters598(before,true)
        val start=now598()
        repeat(count) { ring[it and 63]=case.operation() }
        publishRing598(ring)
        val elapsed=now598()-start
        counters598(after,false)
        check(elapsed > 0)
        var digest=0
        repeat(minOf(count,64)) { digest=31*digest+checksumString598(case.checkResult(checkNotNull(ring[it]))) }
        case.checkInputs()
        check(case.checkResult(first) == retainedFirstText) { "Retained FIRST result changed" }
        if(record) println("TIME598\t$peer\t$id\t$fork\t$sample\t$count\t$elapsed\t${delta(before.cpuNs,after.cpuNs)}\t${delta(before.allocatedBytes,after.allocatedBytes)}\t${available(before.heapUsedBytes)}\t${available(after.heapUsedBytes)}\t${available(after.rssPeakBytes)}\t$digest")
        return elapsed
    }
    // Minimum elapsed training is independent of cumulative work; batches remain bounded.
    var count=1; var warmOps=0L; val warmStart=now598()
    while(now598()-warmStart < 200_000_000L) {
        val n=count
        check(n in 1..1_048_576 && warmOps <= Long.MAX_VALUE-n.toLong()) { "Training operation overflow" }
        val elapsed=batch(n,-1,false); warmOps+=n
        if(elapsed < 20_000_000L && count < 1_048_576) count=minOf(count*2,1_048_576)
    }
    println("WARM598\t$peer\t$id\t$fork\t$warmOps\t${now598()-warmStart}\t$count\tMINIMUM200ms_BATCHCAP1048576_TARGET20msbatch")
    repeat(7) { sample -> batch(count,sample,true) }
    println("COMPLETE598_TIMING_CHILD\t$peer\t$id\t$fork")
}

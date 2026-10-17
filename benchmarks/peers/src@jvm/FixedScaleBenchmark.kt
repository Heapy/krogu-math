package preparation598

import kotlinx.benchmark.*

@State(Scope.Benchmark)
open class FixedScaleBenchmark {
    @Param("krogu", "jdk", "kbignum")
    var peer: String = "krogu"
    @Param(
        "fixed-scale/positive-tie",
        "fixed-scale/negative-tie",
        "fixed-scale/negative-divisor",
        "fixed-scale/both-negative",
        "fixed-scale/nonterminating",
        "fixed-scale/carry",
        "fixed-scale/zero",
        "fixed-scale/exact")
    var workload: String = "fixed-scale/positive-tie"
    private lateinit var selected: TimedCase598
    private var retained: Any? = null

    @Setup
    fun prepare() {
        val fixture = FixtureCorpus.fixedScale.single { workload == "fixed-scale/${it.id}" }
        fun <I: Any,D: Any> prepare(adapter: PeerAdapter<I,D>) = fixedCase598(adapter, workload, fixture.left, fixture.right, fixture.scale, fixture.expected)
        selected = when(peer) {
            "krogu" -> prepare(KroguAdapter)
            "jdk" -> prepare(JdkAdapter)
            "kbignum" -> prepare(KBigNumAdapter)
            else -> error("Fixed-scale HALF_UP is unavailable for this peer")
        }
        selected.checkInputs()
        val first = selected.operation()
        val expected = selected.checkResult(first)
        repeat(2) {
            val result = selected.operation()
            check(selected.checkResult(result) == expected)
            selected.checkInputs()
            check(selected.checkResult(first) == expected) { "Retained output changed" }
        }
        retained = first
        println("PEER_SETUP\t$peer\t$workload\t${selected.relation}\tOUTPUT_INPUT_RETAINED_CHECKED")
    }

    @Benchmark
    fun operation(): Any = selected.operation()

    @TearDown
    fun verifyAfterMeasurement() {
        selected.checkInputs()
        selected.checkResult(checkNotNull(retained))
    }
}

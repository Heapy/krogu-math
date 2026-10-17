package preparation598

import kotlinx.benchmark.*

@State(Scope.Benchmark)
open class PeerBenchmark {
    @Param("krogu", "ions")
    var peer: String = "krogu"
    @Param(
        "int/zero/ADD",
        "int/zero/SUBTRACT",
        "int/zero/MULTIPLY",
        "int/zero/self",
        "int/zero/PARSE",
        "int/zero/FORMAT",
        "int/signed/ADD",
        "int/signed/SUBTRACT",
        "int/signed/MULTIPLY",
        "int/signed/self",
        "int/signed/PARSE",
        "int/signed/FORMAT",
        "int/carry30/ADD",
        "int/carry30/SUBTRACT",
        "int/carry30/MULTIPLY",
        "int/carry30/self",
        "int/carry30/PARSE",
        "int/carry30/FORMAT",
        "int/wide10/ADD",
        "int/wide10/SUBTRACT",
        "int/wide10/MULTIPLY",
        "int/wide10/self",
        "int/wide10/PARSE",
        "int/wide10/FORMAT",
        "decimal/zero/ADD",
        "decimal/zero/SUBTRACT",
        "decimal/zero/MULTIPLY",
        "decimal/zero/self",
        "decimal/zero/PARSE",
        "decimal/zero/FORMAT",
        "decimal/scale/ADD",
        "decimal/scale/SUBTRACT",
        "decimal/scale/MULTIPLY",
        "decimal/scale/self",
        "decimal/scale/PARSE",
        "decimal/scale/FORMAT",
        "decimal/signed/ADD",
        "decimal/signed/SUBTRACT",
        "decimal/signed/MULTIPLY",
        "decimal/signed/self",
        "decimal/signed/PARSE",
        "decimal/signed/FORMAT",
        "decimal/wide10/ADD",
        "decimal/wide10/SUBTRACT",
        "decimal/wide10/MULTIPLY",
        "decimal/wide10/self",
        "decimal/wide10/PARSE",
        "decimal/wide10/FORMAT")
    var workload: String = "int/carry30/MULTIPLY"
    private lateinit var selected: TimedCase598
    private var retained: Any? = null

    @Setup
    fun prepare() {
        selected = when(peer) {
            "krogu" -> coreCase598(KroguAdapter, workload)
            "ions" -> coreCase598(IonsPinAdapter, workload)
            else -> error("Unknown portable peer: $peer")
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

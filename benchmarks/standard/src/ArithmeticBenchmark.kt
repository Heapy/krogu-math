package bench

import kotlinx.benchmark.*

@State(Scope.Benchmark)
open class ArithmeticBenchmark {
    @Param(
        "integer/9/parse",
        "integer/9/format",
        "integer/9/compare",
        "integer/9/multiply",
        "integer/9/square",
        "integer/9/divide",
        "integer/77/parse",
        "integer/77/format",
        "integer/77/compare",
        "integer/77/multiply",
        "integer/77/square",
        "integer/77/divide",
        "integer/309/parse",
        "integer/309/format",
        "integer/309/compare",
        "integer/309/multiply",
        "integer/309/square",
        "integer/309/divide",
        "integer/1234/parse",
        "integer/1234/format",
        "integer/1234/compare",
        "integer/1234/multiply",
        "integer/1234/square",
        "integer/1234/divide",
        "decimal/small/parse",
        "decimal/small/format",
        "decimal/small/precision",
        "decimal/small/compare",
        "decimal/small/multiply",
        "decimal/small/square",
        "decimal/small/divide",
        "decimal/small/round",
        "decimal/small/setScale",
        "decimal/small/chain",
        "decimal/wide/parse",
        "decimal/wide/format",
        "decimal/wide/precision",
        "decimal/wide/compare",
        "decimal/wide/multiply",
        "decimal/wide/square",
        "decimal/wide/divide",
        "decimal/wide/round",
        "decimal/wide/setScale",
        "decimal/wide/chain",
        "decimal/negative-scale/parse",
        "decimal/negative-scale/format",
        "decimal/negative-scale/precision",
        "decimal/negative-scale/compare",
        "decimal/negative-scale/multiply",
        "decimal/negative-scale/square",
        "decimal/negative-scale/divide",
        "decimal/negative-scale/round",
        "decimal/negative-scale/setScale",
        "decimal/negative-scale/chain",
        "decimal/large-scale/parse",
        "decimal/large-scale/format",
        "decimal/large-scale/precision",
        "decimal/large-scale/compare",
        "decimal/large-scale/multiply",
        "decimal/large-scale/square",
        "decimal/large-scale/divide",
        "decimal/large-scale/round",
        "decimal/large-scale/setScale",
        "decimal/large-scale/chain")
    var workload: String = "integer/9/multiply"
    private lateinit var selected: Workload
    private var cursor = 0

    @Setup
    fun prepare() {
        val datasets = benchmarkDatasets()
        selected = datasets.first { it.first == "krogu" }.second.single { it.name == workload }
        // Full 16-input differential verification precedes warmup and counters.
        val expected = Array(16) { selected.run(it).toString() }
        datasets.forEach { (_, values) ->
            val peer = values.single { it.name == workload }
            repeat(16) { check(peer.run(it).toString() == expected[it]) }
        }
        cursor = 0
    }

    @Benchmark
    fun operation(): Any = selected.run(cursor++ and 15)
}

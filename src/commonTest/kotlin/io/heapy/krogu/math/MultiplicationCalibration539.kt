package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.time.TimeSource

internal data class Calibration539Case(val id: String, val operation: () -> Any, val verify: () -> Unit,
    val verifyLastResult: ((Any?) -> Unit)? = null)
internal expect fun calibration539NativeCases(): List<Calibration539Case>
internal expect fun calibration539Arguments(): String?
internal expect fun calibration539Platform(): String
internal expect fun calibration539CpuNs(): Long?
internal expect fun calibration539CallerAllocatedBytes(): Long?
internal expect fun calibration539HeapBytes(): Long?
internal expect fun calibration539Verify(a: IntArray, b: IntArray, result: IntArray)

/** Opt-in diagnostic only; ordinary test runs do no calibration. */
class MultiplicationCalibration539 {
    @Test fun optionalCampaign() {
        val arguments = calibration539Arguments() ?: return
        if (arguments == "worker-public-register") {
            PublicWorkerComparator539.register()
            return
        }
        MultiplicationCampaign539.run(arguments)
    }
}

internal object MultiplicationCampaign539 {
    // Globally retained bounded escape; only checksums, outside timing, inspect results.
    private val ring = arrayOfNulls<Any>(64)

    fun run(arguments: String) {
        val args = arguments.split(',')
        require(args.size == 4) { "mode,forward|reverse,samples,target_ms" }
        require(args[0] in listOf("kernel-small", "kernel-medium", "kernel-large", "cost", "public-small", "native-parallel", "square-control"))
        require(args[1] in listOf("forward", "reverse"))
        val samples = args[2].toInt().also { require(it in 5..15) }
        val target = args[3].toLong().also { require(it in 10..100) } * 1_000_000
        val cases = when (args[0]) {
            "native-parallel" -> calibration539NativeCases().also { require(it.isNotEmpty()) { "Native-only mode or no eligible cases" } }
            "cost" -> costs()
            "public-small" -> publicSmall()
            "square-control" -> squareControls()
            else -> kernels(args[0])
        }.let { if (args[1] == "reverse") it.reversed() else it }
        check(cases.map { it.id }.toSet().size == cases.size)
        println("# calibration539 platform=${calibration539Platform()} mode=${args[0]} order=${args[1]} cases=${cases.size}")
        println("# cpu=process_cpu_if_supported allocation=caller_thread_only heap=coarse_live_sample_not_peak external_rss_required=true")
        for (case in cases) {
            case.verify()
            println("CHECK539,${case.id},PASS")
            // Identical time-based prewarming prevents short fixed batches from
            // accidentally leaving only one policy in a lower compilation tier.
            val warm = TimeSource.Monotonic.markNow()
            var offset = 0
            while (warm.elapsedNow().inWholeNanoseconds < 200_000_000L) {
                ring[offset++ and 63] = case.operation()
            }
            var count = 1
            while (true) {
                val elapsed = batch(case, count)
                if (elapsed >= target || count >= 8_388_608) break
                count *= 2
            }
            repeat(3) { batch(case, count); digest() }
            repeat(samples) { sample ->
                val allocationBefore = calibration539CallerAllocatedBytes()
                val cpuBefore = calibration539CpuNs()
                val elapsed = batch(case, count)
                val cpuAfter = calibration539CpuNs()
                val allocationAfter = calibration539CallerAllocatedBytes()
                // Optional public comparator check: only the last actual escaped
                // result, AFTER wall/CPU/allocation counters. No all-output claim.
                case.verifyLastResult?.invoke(ring[(count - 1) and 63])
                val hash = digest()
                val heap = calibration539HeapBytes() // Outside timing/allocation brackets.
                fun difference(before: Long?, after: Long?): String =
                    if (before != null && after != null && after >= before) (after - before).toString() else "NA"
                println("TIME539,${case.id},$sample,$count,$elapsed,${difference(cpuBefore, cpuAfter)},${difference(allocationBefore, allocationAfter)},${heap ?: "NA"},$hash")
            }
        }
        println("PASS539,${cases.size},$samples")
    }

    private fun batch(case: Calibration539Case, count: Int): Long {
        val mark = TimeSource.Monotonic.markNow()
        repeat(count) { ring[it and 63] = case.operation() }
        return mark.elapsedNow().inWholeNanoseconds
    }
    private fun digest(): Int = ring.fold(1) { hash, value ->
        hash * 31 + if (value is IntArray) value.contentHashCode() else (value?.hashCode() ?: 0)
    }

    private fun kernels(mode: String): List<Calibration539Case> {
        val sizes = when (mode) {
            "kernel-small" -> listOf(8, 16, 31, 32, 63, 64)
            "kernel-medium" -> listOf(96, 128, 192, 256, 383, 384, 512)
            else -> listOf(1024, 4096, 8192)
        }
        // One-dimensional sweeps around the accepted532 policy. No measured
        // platform optimum is encoded, and radix remains base2^30 throughout.
        val policies = (listOf(32, 64, 128, 256).map { MultiplicationPolicy539(it, 16, 384) } +
            listOf(4, 8, 16, 32).map { MultiplicationPolicy539(64, it, 384) } +
            listOf(0, 128, 256, 384, 512, 1024).map { MultiplicationPolicy539(64, 16, it) }).distinct()
        return buildList {
            for (size in sizes) {
                val a = IntArray(size) { ((it.toLong() * 104729 + size * 31L + 1) and 0x3fffffff).toInt() }
                for ((shape, b) in listOf("distinct" to a.copyOf(), "square" to a,
                    "ratio2" to IntArray(size * 2) { 0x3fffffff }, "ratio4" to IntArray(size * 4) { 0x3fffffff })) {
                    val expected = MagnitudeMultiplication.multiply(a, b, Int.MAX_VALUE, Int.MAX_VALUE)
                    calibration539Verify(a, b, expected)
                    // Leaf/whole-square policy is unreachable for nonalias inputs;
                    // keep K sweeps there, rather than time duplicate arithmetic trees.
                    val activePolicies = if (shape == "square") policies else policies.filter { it.squareLeaf == 16 && it.wholeSquareEntry == 384 }
                    for (policy in activePolicies) {
                        add(Calibration539Case("kernel/$size/$shape/k${policy.karatsuba}-s${policy.squareLeaf}-e${policy.wholeSquareEntry}",
                            { MagnitudeMultiplication.multiplyConfigured539(a, b, policy) }, {
                                check(expected.contentEquals(MagnitudeMultiplication.multiplyConfigured539(a, b, policy)))
                            }))
                    }
                    // Scheduling overhead must be compared against this SAME
                    // decomposition, not attributed to a different arithmetic tree.
                    val policy = MultiplicationPolicy539(64, 16, 384)
                    if (MagnitudeMultiplication.prepareParallel539(a, b, policy) != null) {
                        fun frame(): IntArray {
                            val work = checkNotNull(MagnitudeMultiplication.prepareParallel539(a, b, policy))
                            return work.combine(work.child(0), work.child(1), work.child(2))
                        }
                        add(Calibration539Case("frame-serial/$size/$shape", ::frame, { check(expected.contentEquals(frame())) }))
                    }
                }
            }
        }
    }

    private fun squareControls(): List<Calibration539Case> = buildList {
        // Fixed pure-square comparator is essential: a unified recursive policy
        // can otherwise hide avoidable medium-square cost (the532 lesson).
        for (size in listOf(128,192,256,384,512,1024)) {
            val a = IntArray(size) { ((it.toLong() * 104729 + 31) and 0x3fffffff).toInt() }
            val expected = MagnitudeMultiplication.multiply(a, a, Int.MAX_VALUE, Int.MAX_VALUE)
            calibration539Verify(a, a, expected)
            for (entry in listOf(0,384,1024,Int.MAX_VALUE)) {
                val policy = MultiplicationPolicy539(64,16,entry)
                add(Calibration539Case("square-control/$size/e$entry",
                    { MagnitudeMultiplication.multiplyConfigured539(a,a,policy) }, {
                        check(expected.contentEquals(MagnitudeMultiplication.multiplyConfigured539(a,a,policy)))
                    }))
            }
        }
    }

    private fun publicSmall(): List<Calibration539Case> = buildList {
        for (bits in listOf(1, 30, 63, 64, 127, 511, 1023)) {
            val a = BigInteger.ONE.shiftLeft(bits - 1).add(if (bits == 1) BigInteger.ZERO else BigInteger.ONE)
            val b = a.add(BigInteger.TWO)
            val power = bits - 1
            val expected = if (bits == 1) BigInteger.valueOf(3) else
                BigInteger.ONE.shiftLeft(2 * power).add(BigInteger.ONE.shiftLeft(power + 2)).add(BigInteger.valueOf(3))
            add(Calibration539Case("public/$bits/distinct", { a.multiply(b) }, { check(a.multiply(b) == expected); println("REP539,public/$bits/distinct,$expected") }))
            add(Calibration539Case("parallel-public/$bits/distinct", { a.parallelMultiply(b) }, { check(a.parallelMultiply(b) == expected); println("REP539,parallel-public/$bits/distinct,$expected") }))
            val square = if (bits == 1) BigInteger.ONE else
                BigInteger.ONE.shiftLeft(2 * power).add(BigInteger.ONE.shiftLeft(power + 1)).add(BigInteger.ONE)
            add(Calibration539Case("public/$bits/square", { a.multiply(a) }, { check(a.multiply(a) == square); println("REP539,public/$bits/square,$square") }))
            add(Calibration539Case("parallel-public/$bits/square", { a.parallelMultiply(a) }, { check(a.parallelMultiply(a) == square); println("REP539,parallel-public/$bits/square,$square") }))
        }
    }

    private fun costs(): List<Calibration539Case> {
        val words = IntArray(256) { ((it.toLong() * 104729 + 1) and 0x3fffffff).toInt() }
        fun signed(): Long {
            var carry = 0L
            for (word in words) carry = (word.toLong() * 0x3fffffff + carry) ushr 30
            return carry
        }
        fun unsigned(): Long {
            var carry = 0uL
            for (word in words) carry = (word.toULong() * 0x3fffffffuL + carry) shr 30
            return carry.toLong()
        }
        fun integerCarry(): Int {
            var carry = 0
            for (word in words) carry = (word + 0x3fffffff + carry) ushr 30
            return carry
        }
        return listOf(
            Calibration539Case("cost/signed-long-256", ::signed, { check(signed() == unsigned()) }),
            Calibration539Case("cost/unsigned-long-256", ::unsigned, { check(unsigned() == signed()) }),
            Calibration539Case("cost/int-carry-256", ::integerCarry, { check(integerCarry() in 0..1) }),
            Calibration539Case("cost/allocate-256-words", { IntArray(256) }, {}),
            Calibration539Case("cost/allocate-4096-words", { IntArray(4096) }, {}),
        )
    }
}

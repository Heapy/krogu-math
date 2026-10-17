@file:OptIn(kotlin.native.concurrent.ObsoleteWorkersApi::class, kotlin.ExperimentalStdlibApi::class)
package io.heapy.krogu.math

import kotlin.native.concurrent.Worker
import kotlin.test.Test
import kotlin.time.TimeSource

/** Current Native public serial-retention route. The candidate engine and its
 * complete per-call Worker lifecycle measurements remain historical, not these rows. */
class NativePublicFrontendCalibration539 {
    @Test fun optionalPublicRoute() {
        val arguments = calibration539Arguments() ?: return
        if (!arguments.startsWith("native-public-serial-retention,")) return
        check(calibration539Platform().startsWith("Native "))
        val args = arguments.split(',')
        require(args.size == 4) { "native-public-serial-retention,size,distinct|square|ratio2,forward|reverse" }
        val size = args[1].toInt().also { require(it in listOf(256, 1024, 4096, 8192, 16384)) }
        val shape = args[2].also { require(it in listOf("distinct", "square", "ratio2")) }
        require(args[3] in listOf("forward", "reverse"))
        val aWords = IntArray(size) { ((it.toLong() * 104729 + 31) and 0x3fffffff).toInt() }
        val bWords = when (shape) {
            "square" -> aWords
            "ratio2" -> IntArray(2 * size) { 0x3fffffff }
            else -> aWords.copyOf()
        }
        val expectedWords = MagnitudeMultiplication.multiply(aWords, bWords, Int.MAX_VALUE, Int.MAX_VALUE)
        calibration539Verify(aWords, bWords, expectedWords)
        fun number(words: IntArray): BigInteger {
            val bytes = ByteArray(((words.size.toLong() * 30 + 7) / 8).toInt())
            var accumulator = 0L; var bits = 0; var position = bytes.lastIndex
            for (word in words) {
                accumulator = accumulator or (word.toLong() shl bits); bits += 30
                while (bits >= 8) { bytes[position--] = accumulator.toByte(); accumulator = accumulator ushr 8; bits -= 8 }
            }
            if (bits != 0) bytes[position--] = accumulator.toByte()
            check(position == -1)
            return BigInteger(1, bytes)
        }
        val a = number(aWords); val b = if (shape == "square") a else number(bWords)
        val expected = number(expectedWords)
        val selected = MultiplicationPolicy539(MagnitudeMultiplication.selectedKaratsuba539(aWords, bWords),
            MagnitudeMultiplication.SQUARE_THRESHOLD, MagnitudeMultiplication.selectedSquareEntry539())
        check(selected.karatsuba == (if (shape == "distinct") 32 else 64) &&
            selected.squareLeaf == 16 && selected.wholeSquareEntry == 384)
        val originalWordsA = aWords.copyOf(); val originalWordsB = bWords.copyOf()
        val originalA = a.toByteArray(); val originalB = b.toByteArray()
        fun verify(value: Any?) {
            when (value) {
                is BigInteger -> check(value == expected)
                is IntArray -> check(value.contentEquals(expectedWords))
                else -> error("Unexpected actual escaped result")
            }
            check(aWords.contentEquals(originalWordsA) && bWords.contentEquals(originalWordsB))
            check(a.toByteArray().contentEquals(originalA) && b.toByteArray().contentEquals(originalB))
        }
        check(retainSerialParallelMultiply539) { "Current Native public route must decline before backend helpers" }
        Worker.current
        val baselineWorkers = Worker.activeWorkers.map { it.id }.sorted()
        fun verifySerialAbsence(phase: String, sample: Int) {
            val currentWorkers = Worker.activeWorkers.map { it.id }.sorted()
            check(currentWorkers == baselineWorkers) { "Current serial route changed Native Worker ownership" }
            println("NATIVESERIALDECLINE539,$size/$shape,$phase,$sample,${baselineWorkers.joinToString(";")},${currentWorkers.joinToString(";")},true")
        }
        println("INPUTNATIVE539,$size/$shape,${aWords.size},${bWords.size},${aWords === bWords}")
        val cpuBefore = calibration539CpuNs()
        val firstStart = TimeSource.Monotonic.markNow()
        escaped = a.parallelMultiply(b)
        val firstNs = firstStart.elapsedNow().inWholeNanoseconds
        val cpuAfter = calibration539CpuNs()
        verify(escaped)
        println("FIRSTNATIVE539,$size/$shape,$firstNs,${if (cpuBefore != null && cpuAfter != null) cpuAfter - cpuBefore else "NA"}")
        verifySerialAbsence("first", -1)
        println("# first PUBLIC call after untimed school/oracle/BI preparation; current route directly declines to virtual serial multiply; no backend startup or fresh-speedup claim")
        // Current public parallel route is virtual serial; retained candidate children remain64/16 outside this fixture.
        // Each native output type escapes unchanged: no conversion/formatting in operations.
        val routes = mutableListOf<Pair<String, () -> Any>>(
            "kernel-k${selected.karatsuba}-s${selected.squareLeaf}-e${selected.wholeSquareEntry}" to {
                MagnitudeMultiplication.multiplyConfigured539(aWords, bWords, selected)
            },
            "frame-k${selected.karatsuba}-s${selected.squareLeaf}-e${selected.wholeSquareEntry}" to {
                val frame = checkNotNull(MagnitudeMultiplication.prepareParallel539(aWords, bWords, selected))
                frame.combine(frame.child(0), frame.child(1), frame.child(2))
            },
            "serial" to { a.multiply(b) },
            "parallel" to { a.parallelMultiply(b) },
        )
        val operations = if (args[3] == "reverse") routes.reversed() else routes
        println("INVENTORYNATIVE539,$size/$shape,${operations.joinToString(";") { it.first }}")
        for ((variant, calculation) in operations) {
            fun operation() { escaped = calculation() }
            val warm = TimeSource.Monotonic.markNow()
            while (warm.elapsedNow().inWholeNanoseconds < 200_000_000L) operation()
            var count = 1
            fun batch(): Long {
                val start = TimeSource.Monotonic.markNow()
                repeat(count) { operation() }
                return start.elapsedNow().inWholeNanoseconds
            }
            while (batch() < 20_000_000L && count < 8_388_608) count *= 2
            repeat(3) { batch(); verify(escaped) }
            repeat(7) { sample ->
                val before = calibration539CpuNs()
                val elapsed = batch()
                val after = calibration539CpuNs()
                verify(escaped)
                val heap = calibration539HeapBytes()
                println("PUBLICNATIVE539,$size/$shape/$variant,$sample,$count,$elapsed,${if (before != null && after != null) after - before else "NA"},NA,${heap ?: "NA"},${if (escaped is IntArray) (escaped as IntArray).contentHashCode() else escaped!!.hashCode()}")
                if (variant == "parallel") verifySerialAbsence("parallel", sample)
            }
        }
        check(Worker.activeWorkers.map { it.id }.sorted() == baselineWorkers)
        println("NATIVESERIALRESOURCE539,$size/$shape,NONE_STARTED,${baselineWorkers.joinToString(";")},true")
        println("PASS_PUBLICNATIVE539,$size/$shape")
    }
    companion object { private var escaped: Any? = null }
}

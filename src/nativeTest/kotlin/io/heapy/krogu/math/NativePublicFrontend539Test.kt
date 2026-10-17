@file:OptIn(kotlin.native.concurrent.ObsoleteWorkersApi::class, kotlin.concurrent.atomics.ExperimentalAtomicApi::class,
    kotlin.experimental.ExperimentalNativeApi::class)
package io.heapy.krogu.math
import kotlin.concurrent.atomics.AtomicInt
import kotlin.native.Platform
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.test.*
import kotlin.time.TimeSource

class NativePublicFrontend539Test {
    private fun value(words: Int): BigInteger = BigInteger.ONE.shiftLeft(words * 30 - 1).add(BigInteger.valueOf(31))
    private fun expected(left: Int, right: Int): BigInteger = BigInteger.ONE.shiftLeft(left * 30 + right * 30 - 2)
        .add(BigInteger.valueOf(31).shiftLeft(left * 30 - 1))
        .add(BigInteger.valueOf(31).shiftLeft(right * 30 - 1)).add(BigInteger.valueOf(961))
    @Test fun publicBoundariesSignsAliasAndWorkersTerminateBeforeReturn() {
        Worker.current
        val baseline = Worker.activeWorkers.map { it.id }.toSet()
        for (words in listOf(4095, 4096, 4097)) {
            val a = value(words)
            for (otherWords in listOf(words, 2 * words - 1, 2 * words, 2 * words + 1)) {
                val b = value(otherWords); val truth = expected(words, otherWords)
                for (leftSign in listOf(1, -1)) for (rightSign in listOf(1, -1)) {
                    val left = if (leftSign < 0) a.negate() else a
                    val right = if (rightSign < 0) b.negate() else b
                    val oldLeft = left.toByteArray(); val oldRight = right.toByteArray()
                    val actual = left.parallelMultiply(right)
                    assertEquals(if (leftSign == rightSign) truth else truth.negate(), actual)
                    assertNotSame(left, actual); assertNotSame(right, actual)
                    assertContentEquals(oldLeft, left.toByteArray()); assertContentEquals(oldRight, right.toByteArray())
                    assertEquals(baseline, Worker.activeWorkers.map { it.id }.toSet())
                }
            }
            assertEquals(expected(words, words), a.parallelMultiply(a))
        }
        assertSame(BigInteger.ZERO, value(4096).parallelMultiply(BigInteger.ZERO))
        assertNotSame(BigInteger.ONE, BigInteger.ONE.parallelMultiply(BigInteger.ONE))
        assertEquals(baseline, Worker.activeWorkers.map { it.id }.toSet())
        assertFalse(canParallelMultiply539(4095, 4096)); assertFalse(canParallelMultiply539(4096, 8193))
        if (Platform.getAvailableProcessors() > 2) assertTrue(canParallelMultiply539(4096, 8192))
    }
    @Test fun publicSubclassFallbackRetainsVirtualAndStoredMetadataContract() {
        val marker = BigInteger.valueOf(123)
        class Receiver(text: String) : BigInteger(text) {
            var calls = 0
            override fun multiply(value: BigInteger): BigInteger { calls++; return marker }
            override fun signum(): Int = error("No receiver signum probe")
            override fun bitLength(): Int = error("No receiver bitLength probe")
            override fun toByteArray(): ByteArray = error("No receiver byte probe")
        }
        val a = value(4096)
        for (text in listOf("0", "1", a.toString())) {
            val child = Receiver(text)
            assertSame(marker, child.parallelMultiply(a)); assertEquals(1, child.calls)
        }
        class Operand(text: String) : BigInteger(text) {
            override fun signum(): Int = error("No operand signum probe")
            override fun bitLength(): Int = error("No operand bitLength probe")
            override fun toByteArray(): ByteArray = error("No operand byte probe")
        }
        assertEquals(expected(4096, 4096), a.parallelMultiply(Operand(a.toString())))
    }
    @Test fun sharedPermitContentionAndNestedPublicFallbackDoNotCreateWorkers() {
        Worker.current
        val baseline = Worker.activeWorkers.map { it.id }.toSet()
        val entered = AtomicInt(0); val release = AtomicInt(0)
        val a = IntArray(17) { 0x3fffffff }; val b = IntArray(18) { it + 1 }
        val policy = MultiplicationPolicy539(4, 1, 0)
        val outer = Worker.start(errorReporting = false)
        try {
        val first = outer.execute(TransferMode.SAFE, { Triple(entered, release, Pair(a, b)) }, { data ->
            val candidate = NativeParallelEngine539(2, processors = 4) { frame, index ->
                if (index < 2) { data.first.fetchAndAdd(1); while (data.second.load() == 0) { } }
                frame.child(index)
            }
            candidate.multiply(data.third.first, data.third.second, MultiplicationPolicy539(4, 1, 0))
        })
        try {
            val wait = TimeSource.Monotonic.markNow()
            while (entered.load() < 2) check(wait.elapsedNow().inWholeMilliseconds < 5000) { "Accepted children not reached" }
            val occupied = Worker.activeWorkers.map { it.id }.toSet()
            assertEquals(baseline.size + 3, occupied.size)
            assertNull(NativeParallelEngine539(2, processors = 4).tryMultiply539(a, b, policy))
            val public = value(4096)
            assertEquals(expected(4096, 4096), public.parallelMultiply(public))
            assertEquals(occupied, Worker.activeWorkers.map { it.id }.toSet())
        } finally {
            release.store(1)
            first.result
        }
        } finally { outer.requestTermination(processScheduledJobs = true).result }
        assertEquals(baseline, Worker.activeWorkers.map { it.id }.toSet())
        val truth = MagnitudeMultiplication.multiply(a, b, Int.MAX_VALUE, Int.MAX_VALUE)
        val calls = AtomicInt(0)
        val nested = NativeParallelEngine539(2, processors = 4) { frame, index ->
            calls.fetchAndAdd(1)
            if (index == 0) {
                assertFalse(platformParallelAvailable539())
                val public = value(4096)
                assertEquals(expected(4096, 4096), public.parallelMultiply(public))
            }
            frame.child(index)
        }
        assertContentEquals(truth, nested.multiply(a, b, policy)); assertEquals(3, calls.load())
        assertEquals(baseline, Worker.activeWorkers.map { it.id }.toSet())
    }

    @Test fun syntheticAllocationAndRegistrationFailuresNeverRetryAndReleasePermits() {
        Worker.current
        val baseline = Worker.activeWorkers.map { it.id }.toSet()
        val a = IntArray(17) { 0x3fffffff }; val b = IntArray(18) { it + 1 }
        val policy = MultiplicationPolicy539(4, 1, 0)
        val truth = MagnitudeMultiplication.multiply(a, b, Int.MAX_VALUE, Int.MAX_VALUE)
        for (prefix in listOf(false, true)) {
            val marker = OutOfMemoryError("synthetic Native allocation failure $prefix")
            val calls = AtomicInt(0)
            val candidate = NativeParallelEngine539(2, processors = 4,
                registrationFault539 = { future, index -> if (prefix && !future && index == 1) throw marker }) { frame, index ->
                calls.fetchAndAdd(1)
                if (!prefix && index == 0) throw marker
                frame.child(index)
            }
            val observed = try { candidate.multiply(a, b, policy); null } catch (problem: Throwable) { problem }
            assertSame(marker, observed)
            if (!prefix) assertEquals(3, calls.load(), "Accepted operation must not be recomputed")
            assertEquals(baseline, Worker.activeWorkers.map { it.id }.toSet())
            val recovered = AtomicInt(0)
            assertContentEquals(truth, NativeParallelEngine539(2, processors = 4) { frame, index ->
                recovered.fetchAndAdd(1); frame.child(index)
            }.multiply(a, b, policy))
            assertEquals(3, recovered.load()); assertEquals(baseline, Worker.activeWorkers.map { it.id }.toSet())
        }
    }
}

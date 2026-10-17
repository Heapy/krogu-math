package io.heapy.krogu.math

import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicIntegerArray
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Candidate contracts, UNCOMPILED/UNRUN. No timing assertions. */
class PublicParallelFrontend539JvmTest {
    private fun value(words: Int): BigInteger = BigInteger.ONE.shiftLeft(words * 30 - 1).add(BigInteger.valueOf(31))
    private fun expected(a: BigInteger, b: BigInteger): String =
        java.math.BigInteger(a.toByteArray()).multiply(java.math.BigInteger(b.toByteArray())).toString()
    private fun checkProduct(a: BigInteger, b: BigInteger) {
        val beforeA = a.toByteArray(); val beforeB = b.toByteArray()
        val result = a.parallelMultiply(b)
        assertEquals(expected(a, b), result.toString())
        assertNotSame(a, result); assertNotSame(b, result)
        assertContentEquals(beforeA, a.toByteArray()); assertContentEquals(beforeB, b.toByteArray())
    }

    @Test fun thresholdRatioSignAliasAndFreshPublication() {
        for (words in listOf(4095, 4096, 4097)) {
            val a = value(words)
            for (b in listOf(BigInteger(a.toByteArray()), a, value(2 * words - 1), value(2 * words), value(2 * words + 1))) {
                checkProduct(a, b); checkProduct(a.negate(), b)
                checkProduct(a, b.negate()); checkProduct(a.negate(), b.negate())
            }
        }
        assertSame(BigInteger.ZERO, value(4096).parallelMultiply(BigInteger.ZERO))
        assertNotSame(BigInteger.ONE, BigInteger.ONE.parallelMultiply(BigInteger.ONE))
    }

    @Test fun subclassReceiverRetainsVirtualMultiplyAndOperandFallback() {
        val marker = BigInteger.valueOf(123)
        class Receiver(text: String) : BigInteger(text) {
            var calls = 0
            override fun multiply(value: BigInteger): BigInteger { calls++; return marker }
            override fun signum(): Int = error("No virtual signum probe")
            override fun bitLength(): Int = error("No virtual bitLength probe")
            override fun toByteArray(): ByteArray = error("No virtual byte probe")
        }
        val a = value(4096)
        for (text in listOf("0", "1", a.toString())) {
            val child = Receiver(text)
            assertSame(marker, child.parallelMultiply(a)); assertEquals(1, child.calls)
            assertSame(marker, child.multiply(a)); assertEquals(2, child.calls)
        }
        class Operand(text: String) : BigInteger(text) {
            override fun signum(): Int = error("Operand virtual signum changed")
            override fun bitLength(): Int = error("Operand virtual bitLength changed")
            override fun toByteArray(): ByteArray = error("Operand virtual bytes changed")
        }
        val b = Operand(value(4096).toString())
        assertEquals(a.multiply(b), a.parallelMultiply(b))
        assertEquals(a.multiply(a).toString(), a.parallelMultiply(a).toString())
    }

    @Test fun preInterruptedForkJoinAndConcurrentPublicCalls() {
        val a = value(4096); val b = value(8192); val truth = expected(a, b)
        Thread.currentThread().interrupt()
        try {
            assertFalse(canParallelMultiply539(4096, 8192))
            assertEquals(truth, a.parallelMultiply(b).toString())
            assertTrue(Thread.currentThread().isInterrupted)
        } finally { Thread.interrupted() }
        val fjp = ForkJoinPool(1)
        try {
            assertEquals(truth, fjp.submit(Callable {
                assertFalse(canParallelMultiply539(4096, 8192))
                a.parallelMultiply(b).toString()
            }).get(30, TimeUnit.SECONDS))
        } finally { fjp.shutdown(); assertTrue(fjp.awaitTermination(30, TimeUnit.SECONDS)) }
        val callers = Executors.newFixedThreadPool(2)
        try {
            val jobs = List(4) { callers.submit(Callable { a.parallelMultiply(b).toString() }) }
            jobs.forEach { assertEquals(truth, it.get(30, TimeUnit.SECONDS)) }
        } finally { callers.shutdown(); assertTrue(callers.awaitTermination(30, TimeUnit.SECONDS)) }
    }

    @Test fun nullBoundaryMatchesMultiplyAndSizeGateUsesContinuousFamily() {
        fun failure(name: String): Throwable = try {
            BigInteger::class.java.getMethod(name, BigInteger::class.java).invoke(value(4096), null)
            error("Expected rejection")
        } catch (failure: InvocationTargetException) { checkNotNull(failure.cause) }
        assertEquals(failure("multiply")::class, failure("parallelMultiply")::class)
        assertFalse(canParallelMultiply539(4095, 4095))
        assertFalse(canParallelMultiply539(4096, 8193))
        assertFalse(canParallelMultiply539(8193, 4096))
        // No exact-size whitelist: intermediate balanced sizes are eligible on
        // suitable normal-thread CPUs. Runtime/concurrency guards may still decline.
        if (Runtime.getRuntime().availableProcessors() > 2 && !Thread.currentThread().isInterrupted && !java.util.concurrent.ForkJoinTask.inForkJoinPool()) {
            assertTrue(canParallelMultiply539(4097, 6145))
        }
    }

    @Test fun acceptedChildPrefixAndRestoreFailuresAlwaysTerminateWithoutRetry() {
        for (phase in listOf("get", "restore", "operation-and-restore")) {
            val marker = OutOfMemoryError("synthetic $phase failure; no actual exhaustion")
            val cleanup = if (phase == "restore") marker else IllegalStateException("secondary restore fault")
            val counts = AtomicIntegerArray(3)
            val context = if (phase == "get") object : ThreadLocal<Boolean>() {
                override fun initialValue(): Boolean = throw marker
            } else object : ThreadLocal<Boolean>() {
                override fun remove() { throw cleanup }
            }
            val a = IntArray(17) { 0x3fffffff }; val b = IntArray(18) { it + 31 }
            val candidate = ParallelCandidate539(2, processors = 4, workerContext539 = context) { frame, index ->
                counts.incrementAndGet(index)
                if (phase == "operation-and-restore" && index == 0) throw marker
                frame.child(index)
            }
            // A broken completion fence must fail this bounded witness rather
            // than leave a non-daemon caller holding the whole test JVM alive.
            val outcome = java.util.concurrent.FutureTask<Throwable?> {
                try { candidate.multiply(a, b, MultiplicationPolicy539(4, 1, 0)); null }
                catch (problem: Throwable) { problem } // Deliberate exact synthetic marker capture.
            }
            val caller = Thread(outcome, "krogu539-terminal-fence-witness").apply { isDaemon = true; start() }
            try {
                assertSame(marker, outcome.get(5, TimeUnit.SECONDS), phase)
                caller.join(5_000)
                assertFalse(caller.isAlive, "Owned caller completed after all accepted readers")
                assertEquals(if (phase == "get") 0 else 1, counts.get(0), "Failed child cannot run/retry operation")
                assertEquals(if (phase == "get") 0 else 1, counts.get(1), "All accepted children reach terminal state")
                assertEquals(1, counts.get(2), "Caller child runs at most once")
            } finally { candidate.close(); assertTrue(candidate.awaitTermination539(5)) }
            val subsequentCalls = java.util.concurrent.atomic.AtomicInteger()
            val after = ParallelCandidate539(2, processors = 4) { frame, index ->
                subsequentCalls.incrementAndGet(); frame.child(index)
            }
            try {
                assertContentEquals(MagnitudeMultiplication.multiply(a, b, Int.MAX_VALUE, Int.MAX_VALUE),
                    after.multiply(a, b, MultiplicationPolicy539(4, 1, 0)))
                assertEquals(3, subsequentCalls.get(), "Both permits reusable; not silent serial fallback")
            } finally { after.close(); assertTrue(after.awaitTermination539(5)) }
        }
    }
}

package io.heapy.krogu.math

import java.util.concurrent.atomic.AtomicReference
import kotlin.test.*

class PrimalityConformanceTest {
    @Test fun maximumCertaintyCompletesForBoundedPrimes() {
        val values = listOf("2147483647", "2305843009213693951", "18446744073709551557") +
            listOf(65, 77, 78, 80, 81).map { java.math.BigInteger.probablePrime(it, java.util.Random(it.toLong())).toString() }
        val result = AtomicReference<Throwable?>()
        val worker = Thread {
            try {
                for (value in values) {
                    assertTrue(BigInteger(value).isProbablePrime(Int.MAX_VALUE), value)
                    assertTrue(BigInteger("-$value").isProbablePrime(Int.MAX_VALUE), "-$value")
                }
            } catch (e: Throwable) { result.set(e) }
        }.apply { isDaemon = true }
        worker.start()
        worker.join(5000)
        assertFalse(worker.isAlive, "Integer.MAX_VALUE certainty must not require a billion witness rounds for bounded primes")
        result.get()?.let { throw it }
    }
}

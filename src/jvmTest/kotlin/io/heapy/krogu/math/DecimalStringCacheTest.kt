package io.heapy.krogu.math

import java.io.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.test.*

class DecimalStringCacheTest {
    private class Child(var observedSign: Int) : BigDecimal("0.1") {
        var calls = 0
        override fun signum(): Int { calls++; return observedSign }
    }
    private class JChild(var observedSign: Int) : java.math.BigDecimal("0.1") {
        var calls = 0
        override fun signum(): Int { calls++; return observedSign }
        override fun toByte(): Byte = super.toInt().toByte()
        override fun toShort(): Short = super.toInt().toShort()
    }
    private fun roundTrip(value: Any): Any {
        val bytes = ByteArrayOutputStream()
        ObjectOutputStream(bytes).use { it.writeObject(value) }
        return ObjectInputStream(ByteArrayInputStream(bytes.toByteArray())).use { it.readObject() }
    }

    @Test fun repeatedCanonicalCallsMatchJdkCachedValueAndCallbacks() {
        val expected = JChild(-1)
        val actual = Child(-1)
        assertEquals(expected.toString(), actual.toString())
        expected.observedSign = 1; actual.observedSign = 1
        assertEquals(expected.toString(), actual.toString())
        assertEquals(expected.calls, actual.calls)
    }

    @Test fun currentFormatRoundTripDoesNotRetainTransientFormattingState() {
        val expected = JChild(-1)
        val actual = Child(-1)
        assertEquals(expected.toString(), actual.toString())
        expected.observedSign = 1; actual.observedSign = 1
        val expectedCopy = roundTrip(expected) as JChild
        val actualCopy = roundTrip(actual) as Child
        assertEquals(expectedCopy.toString(), actualCopy.toString())
        assertEquals(expectedCopy.calls, actualCopy.calls)
    }

    @Test fun concurrentReadersSeeCompleteImmutableText() {
        val expected = java.math.BigDecimal("-12345678901234567890.0012300").toString()
        val actual = BigDecimal(expected)
        val pool = Executors.newFixedThreadPool(8)
        try {
            pool.invokeAll(List(8) { Callable {
                repeat(1000) {
                    assertEquals(expected, actual.toString())
                    assertEquals(expected, actual.toPlainString())
                    assertEquals(expected, actual.toEngineeringString())
                }
            } }).forEach { it.get() }
        } finally { pool.shutdownNow() }
    }
}

package io.heapy.krogu.math

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamClass
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecimalMetadata597JvmTest {
    @Test fun concurrentPublicationAndCurrentSerializationKeepExactMetadata() {
        val value = BigDecimal(BigInteger("1" + "0".repeat(129)), Int.MAX_VALUE)
        val pool = Executors.newFixedThreadPool(4)
        try {
            val jobs = List(4) { pool.submit<Int> { repeat(1000) { check(value.precision() == 130) }; value.precision() } }
            for (job in jobs) assertEquals(130, job.get(5, TimeUnit.SECONDS))
        } finally {
            pool.shutdown()
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS))
        }
        assertTrue(ObjectStreamClass.lookup(BigDecimal::class.java).fields.none { it.name == "cachedStoredPrecision597" })
        val bytes = ByteArrayOutputStream().also { stream -> ObjectOutputStream(stream).use { it.writeObject(value) } }.toByteArray()
        val restored = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as BigDecimal }
        assertEquals(value, restored)
        assertEquals(Int.MAX_VALUE, restored.scale())
        repeat(3) { assertEquals(130, restored.precision()) }
    }
}

package io.heapy.krogu.math

import java.io.*
import kotlin.test.*

class SerializationConformanceTest {
    private class IntegerChild(value: String, val label: String) : BigInteger(value) {
        var alias: IntArray? = null
    }
    private class DecimalChild(value: String, val label: String) : BigDecimal(value)
    private class JIntegerChild(value: String, val label: String) : java.math.BigInteger(value) {
        override fun toByte(): Byte = toInt().toByte()
        override fun toShort(): Short = toInt().toShort()
    }
    private class JDecimalChild(value: String, val label: String) : java.math.BigDecimal(value) {
        override fun toByte(): Byte = toInt().toByte()
        override fun toShort(): Short = toInt().toShort()
    }

    private fun encode(value: Any): ByteArray = ByteArrayOutputStream().also { buffer ->
        ObjectOutputStream(buffer).use { it.writeObject(value) }
    }.toByteArray()
    private fun decode(bytes: ByteArray): Any = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() }
    private fun roundTrip(value: Any): Any = decode(encode(value))
    private fun field(owner: Class<*>, name: String) = owner.getDeclaredField(name).apply { isAccessible = true }

    @Test fun serialVersionIdentifiersAreExplicit() {
        for (port in listOf(BigInteger::class.java, BigDecimal::class.java, MathContext::class.java)) {
            val uid = port.getDeclaredField("serialVersionUID")
            assertTrue(java.lang.reflect.Modifier.isPrivate(uid.modifiers))
            assertTrue(java.lang.reflect.Modifier.isStatic(uid.modifiers))
            assertTrue(java.lang.reflect.Modifier.isFinal(uid.modifiers))
            uid.isAccessible = true
            assertEquals(uid.getLong(null), ObjectStreamClass.lookup(port).serialVersionUID)
        }
    }

    @Test fun subclassesKeepTheirTypeStateAndValue() {
        val integer = roundTrip(IntegerChild("-12345678901234567890123456789", "integer")) as IntegerChild
        val jInteger = roundTrip(JIntegerChild("-12345678901234567890123456789", "integer")) as JIntegerChild
        assertEquals(jInteger.toString(), integer.toString())
        assertEquals(jInteger.label, integer.label)
        val decimal = roundTrip(DecimalChild("12345678901234567890.1200", "decimal")) as DecimalChild
        val jDecimal = roundTrip(JDecimalChild("12345678901234567890.1200", "decimal")) as JDecimalChild
        assertEquals(jDecimal.toString(), decimal.toString())
        assertEquals(jDecimal.scale(), decimal.scale())
        assertEquals(jDecimal.label, decimal.label)
    }

    @Test fun subclassCannotKeepAnAliasToRestoredLimbs() {
        assertTrue(java.lang.reflect.Modifier.isFinal(field(BigInteger::class.java, "words").modifiers))
        val value = IntegerChild("12345678901234567890123456789", "alias")
        value.alias = field(BigInteger::class.java, "words").get(value) as IntArray
        val restored = roundTrip(value) as IntegerChild
        restored.alias!!.fill(0)
        assertEquals("12345678901234567890123456789", restored.toString())
    }

    @Test fun malformedSubclassFieldsAreRejectedDuringReading() {
        for (invalid in listOf(null, intArrayOf(), intArrayOf(-1), intArrayOf(1 shl 30), intArrayOf(1, 0))) {
            val value = IntegerChild("1", "invalid")
            field(BigInteger::class.java, "words").set(value, invalid)
            assertFailsWith<InvalidObjectException> { roundTrip(value) }
        }
        for (sign in listOf(-2, 0, 2)) {
            val value = IntegerChild("1", "invalid sign")
            field(BigInteger::class.java, "sign").setInt(value, sign)
            assertFailsWith<InvalidObjectException> { roundTrip(value) }
        }
        val decimal = DecimalChild("1.20", "missing coefficient")
        field(BigDecimal::class.java, "coefficient").set(decimal, null)
        assertFailsWith<InvalidObjectException> { roundTrip(decimal) }
    }

    @Test fun repeatedReferencesAndCyclesRemainIntact() {
        val value = IntegerChild("234567890123456789", "shared")
        val graph = arrayListOf<Any>(value, value)
        graph.add(graph)
        val copy = roundTrip(graph) as List<*>
        assertSame(copy[0], copy[1])
        assertSame(copy, copy[2])
        assertEquals(value, copy[0])
    }

    @Test fun malformedContextFieldsAreRejectedDuringReading() {
        val negative = MathContext(7, RoundingMode.HALF_UP)
        field(MathContext::class.java, "precision").setInt(negative, -1)
        assertFailsWith<InvalidObjectException> { roundTrip(negative) }
        val missingMode = MathContext(7, RoundingMode.HALF_UP)
        field(MathContext::class.java, "roundingMode").set(missingMode, null)
        assertFailsWith<InvalidObjectException> { roundTrip(missingMode) }
    }

}

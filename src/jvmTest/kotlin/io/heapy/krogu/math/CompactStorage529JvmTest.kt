package io.heapy.krogu.math

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InvalidObjectException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CompactStorage529JvmTest {
    private class Marker : RuntimeException()
    private class CoefficientChild : BigInteger("123") {
        val marker = Marker()
        override fun toByteArray(): ByteArray = throw marker
    }
    private class IntegerChild(value: String) : BigInteger(value) { var state: Any? = null }
    private class DecimalChild(value: String) : BigDecimal(value) { var state: Any? = null }
    private fun field(owner: Class<*>, name: String) = owner.getDeclaredField(name).apply { isAccessible = true }
    private fun copy(value: Any): Any {
        val bytes = ByteArrayOutputStream().also { buffer -> ObjectOutputStream(buffer).use { it.writeObject(value) } }.toByteArray()
        return ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() }
    }

    @Test fun compactInstancesRetainOnlyPrimitivePayloads() {
        for (type in listOf(BigInteger::class.java, BigDecimal::class.java)) {
            val fields = type.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }
            assertTrue(fields.none { it.type == java.lang.Long::class.java || it.type == Long::class.javaPrimitiveType })
            for (name in listOf("compactHigh", "compactLow", "representation"))
                assertTrue(Modifier.isFinal(field(type, name).modifiers))
        }
        for (value in listOf(Long.MIN_VALUE, Long.MAX_VALUE, -17L, 0L, 17L)) {
            val integer = BigInteger.valueOf(value)
            val decimal = BigDecimal.valueOf(value, 7)
            assertNull(field(BigInteger::class.java, "wideWords").get(integer))
            assertNull(field(BigDecimal::class.java, "wideCoefficient").get(decimal))
            // Explicit unscaledValue publication does not install a retained object.
            assertEquals(integer, decimal.unscaledValue())
            assertNull(field(BigDecimal::class.java, "wideCoefficient").get(decimal))
            assertEquals(integer, copy(integer))
            assertEquals(decimal, copy(decimal))
        }
    }

    @Test fun currentFormatKeepsCompactSubclassStateAndGraphIdentity() {
        val integer = IntegerChild(Long.MIN_VALUE.toString())
        val decimal = DecimalChild("92233720368547758.07")
        val graph = arrayListOf<Any>(integer, decimal, integer, decimal)
        graph.add(graph)
        integer.state = graph
        decimal.state = integer
        val restored = copy(graph) as List<*>
        assertSame(restored[0], restored[2]); assertSame(restored[1], restored[3]); assertSame(restored, restored[4])
        assertSame(restored, (restored[0] as IntegerChild).state)
        assertSame(restored[0], (restored[1] as DecimalChild).state)
        assertEquals(integer, restored[0]); assertEquals(decimal, restored[1])
    }

    @Test fun currentFormatRejectsImpossibleCompactAndWideBranches() {
        for (kind in listOf(0, -1, 3)) {
            val integer = BigInteger.valueOf(17)
            field(BigInteger::class.java, "representation").setInt(integer, kind)
            assertFailsWith<InvalidObjectException> { copy(integer) }
            val decimal = BigDecimal.valueOf(17)
            field(BigDecimal::class.java, "representation").setInt(decimal, kind)
            assertFailsWith<InvalidObjectException> { copy(decimal) }
        }
        val integer = BigInteger.valueOf(17)
        field(BigInteger::class.java, "wideWords").set(integer, intArrayOf(17))
        assertFailsWith<InvalidObjectException> { copy(integer) }
        val decimal = BigDecimal.valueOf(17)
        field(BigDecimal::class.java, "wideCoefficient").set(decimal, BigInteger.valueOf(17))
        assertFailsWith<InvalidObjectException> { copy(decimal) }
    }

    @Test fun decimalConstructorNormalizesVirtualBytesBeforeCompactSelection() {
        var calls = 0
        val source = object : BigInteger("17") {
            override fun toByteArray(): ByteArray { calls++; return byteArrayOf(42) }
            override fun toLong(): Long = error("constructor must use byte encoding")
        }
        val decimal = BigDecimal(source, 2)
        assertEquals(1, calls)
        assertEquals("0.42", decimal.toString())
        assertNull(field(BigDecimal::class.java, "wideCoefficient").get(decimal))
    }

    @Test fun restoredCoefficientSubtypeRetainsScaleConstructorNormalization() {
        val original = BigDecimal.valueOf(123, 2)
        field(BigDecimal::class.java, "wideCoefficient").set(original, CoefficientChild())
        field(BigDecimal::class.java, "representation").setInt(original, 2)
        field(BigDecimal::class.java, "compactHigh").setInt(original, 0)
        field(BigDecimal::class.java, "compactLow").setInt(original, 0)
        val restored = copy(original) as BigDecimal
        val coefficient = field(BigDecimal::class.java, "wideCoefficient").get(restored) as CoefficientChild
        val failure = assertFailsWith<Marker> { restored.scaleByPowerOfTen(1) }
        assertSame(coefficient.marker, failure)
        assertEquals(2, restored.scale())
        assertSame(coefficient, restored.unscaledValue())
    }

    @Test fun transitionNeighborsAndCompactDivisorWordsMatchPublicJdk() {
        val hostOne = java.math.BigInteger.ONE
        val hosts = buildList {
            add(java.math.BigInteger.ZERO)
            for (bits in listOf(30, 60, 63, 64, 90)) {
                val power = hostOne.shiftLeft(bits)
                for (offset in listOf(-1L, 0L, 1L, 127L)) {
                    val positive = power.add(java.math.BigInteger.valueOf(offset))
                    add(positive); add(positive.negate())
                }
            }
        }.distinct()
        for (a in hosts) for (b in hosts) {
            val left = BigInteger(a.toString()); val right = BigInteger(b.toString())
            assertEquals(a.add(b).toString(), left.add(right).toString(), "$a + $b")
            assertEquals(a.subtract(b).toString(), left.subtract(right).toString(), "$a - $b")
            if (b.signum() != 0) {
                val expected = a.divideAndRemainder(b)
                assertEquals(expected[0].toString(), left.divide(right).toString(), "$a / $b")
                assertEquals(expected[1].toString(), left.remainder(right).toString(), "$a % $b")
                val both = left.divideAndRemainder(right)
                assertEquals(expected[0].toString(), both[0].toString(), "$a divrem $b quotient")
                assertEquals(expected[1].toString(), both[1].toString(), "$a divrem $b remainder")
            }
            assertEquals(a.toString(), left.toString())
            assertEquals(b.toString(), right.toString())
        }
        //Exact original measured two-limb divisor, plus signed MIN/three-limb controls.
        val wide = hostOne.shiftLeft(511).add(java.math.BigInteger.valueOf(123456789))
        for (divisor in listOf(1L, -1L, 12345000001L, -12345000001L, Long.MIN_VALUE, Long.MAX_VALUE)) {
            for (source in listOf(wide, wide.negate())) {
                val expected = source.divideAndRemainder(java.math.BigInteger.valueOf(divisor))
                val ours = BigInteger(source.toString()).divideAndRemainder(BigInteger.valueOf(divisor))
                assertEquals(expected[0].toString(), ours[0].toString(), "$source/$divisor")
                assertEquals(expected[1].toString(), ours[1].toString(), "$source%$divisor")
            }
        }
    }

    @Test fun publicIntegerOracleCoversFullLongOverflowAndFloatMidpoints() {
        val values = listOf(Long.MIN_VALUE, Long.MIN_VALUE + 1, -3037000500L, -1L, 0L, 1L,
            3037000499L, 3037000500L, 36028799166447617L, Long.MAX_VALUE)
        for (a in values) for (b in values) {
            val ownA = BigInteger.valueOf(a); val ownB = BigInteger.valueOf(b)
            val jA = java.math.BigInteger.valueOf(a); val jB = java.math.BigInteger.valueOf(b)
            assertEquals(jA.add(jB).toString(), ownA.add(ownB).toString())
            assertEquals(jA.subtract(jB).toString(), ownA.subtract(ownB).toString())
            assertEquals(jA.multiply(jB).toString(), ownA.multiply(ownB).toString())
            if (b != 0L) {
                assertEquals(jA.divide(jB).toString(), ownA.divide(ownB).toString())
                assertEquals(jA.remainder(jB).toString(), ownA.remainder(ownB).toString())
            }
            assertEquals(jA.and(jB).toString(), ownA.and(ownB).toString())
            assertEquals(jA.or(jB).toString(), ownA.or(ownB).toString())
            assertEquals(jA.xor(jB).toString(), ownA.xor(ownB).toString())
        }
        for (value in values) {
            val own = BigInteger.valueOf(value); val jdk = java.math.BigInteger.valueOf(value)
            assertEquals(jdk.toFloat().toDouble(), own.toFloat().toDouble())
            assertEquals(jdk.toDouble(), own.toDouble())
            for (shift in listOf(-100, -64, -63, -32, -1, 0, 1, 30, 31, 32, 63, 64, 100))
                assertEquals(jdk.shiftLeft(shift).toString(), own.shiftLeft(shift).toString())
        }
    }
}

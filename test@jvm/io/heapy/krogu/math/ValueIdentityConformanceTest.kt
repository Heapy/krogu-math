// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.math.BigDecimal as JDecimal
import java.math.BigInteger as JInteger
import java.math.MathContext as JContext
import java.math.RoundingMode as JMode
import kotlin.test.*

/** Bounded black-box checks. Only public JDK calls, never numerical implementation sources. */
class ValueIdentityConformanceTest {
    @BeforeTest fun pinnedOracle() {
        assertEquals("25.0.4+9-LTS", System.getProperty("java.runtime.version"))
        assertEquals("BellSoft", System.getProperty("java.vendor"))
    }

    @Test fun signedLimbBoundariesMatchEqualityOrderingHashAndCollectionKeys() {
        val texts = linkedSetOf("0")
        for (bit in listOf(7, 8, 29, 30, 31, 32, 59, 60, 63, 64, 95, 96)) {
            val power = JInteger.TWO.pow(bit)
            for (delta in -1L..1L) {
                val positive = power.add(JInteger.valueOf(delta))
                texts += positive.toString()
                texts += positive.negate().toString()
            }
        }
        assertEquals(73, texts.size)
        val jdk = texts.map(::JInteger)
        val own = texts.map(::BigInteger)
        for (i in own.indices) {
            assertEquals(jdk[i].hashCode(), own[i].hashCode(), "hash ${texts.elementAt(i)}")
            assertContentEquals(jdk[i].toByteArray(), own[i].toByteArray())
            for (j in own.indices) {
                assertEquals(jdk[i].equals(jdk[j]), own[i].equals(own[j]), "equals $i/$j")
                assertEquals(jdk[i].compareTo(jdk[j]).compareTo(0), own[i].compareTo(own[j]).compareTo(0), "order $i/$j")
            }
        }
        assertEquals(jdk.reversed().sorted().map { it.toString() }, own.reversed().sorted().map { it.toString() })
        val map = own.withIndex().associate { it.value to it.index }
        for (i in own.indices) assertEquals(i, map[BigInteger(jdk[i].toByteArray())])
        assertEquals(jdk.toSet().size, own.toSet().size)
    }

    private data class DecimalCase(val coefficient: String, val scale: Int)

    @Test fun decimalCohortsAndExtremeScalesMatchEqualityOrderingAndHash() {
        val cases = listOf("0", "1", "-1", "10", "-10", "9223372036854775807", "-9223372036854775808",
            "9223372036854775808", "-9223372036854775809").flatMap { coefficient ->
            listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -7, 0, 7, Int.MAX_VALUE - 1, Int.MAX_VALUE)
                .map { DecimalCase(coefficient, it) }
        } + listOf(DecimalCase("2", 0), DecimalCase("20", 1), DecimalCase("200", 2),
            DecimalCase("-2", 0), DecimalCase("-20", 1), DecimalCase("-200", 2))
        assertEquals(69, cases.size)
        val jdk = cases.map { JDecimal(JInteger(it.coefficient), it.scale) }
        val own = cases.map { BigDecimal(BigInteger(it.coefficient), it.scale) }
        for (i in cases.indices) {
            assertEquals(jdk[i].hashCode(), own[i].hashCode(), "hash ${cases[i]}")
            assertEquals(jdk[i].toString(), own[i].toString(), "text ${cases[i]}")
            for (j in cases.indices) {
                assertEquals(jdk[i].equals(jdk[j]), own[i].equals(own[j]), "equals ${cases[i]}/${cases[j]}")
                assertEquals(jdk[i].compareTo(jdk[j]).compareTo(0), own[i].compareTo(own[j]).compareTo(0),
                    "order ${cases[i]}/${cases[j]}")
            }
        }
        // No arithmetic or plain-string expansion at extreme scales.
        assertEquals(jdk.sorted().map { it.toString() }, own.sorted().map { it.toString() })
        assertEquals(jdk.toSet().size, own.toSet().size)
        val jdkTree = java.util.TreeSet(jdk)
        val ownTree = java.util.TreeSet(own)
        assertEquals(jdkTree.map { it.toString() }, ownTree.map { it.toString() })
        val expectedMap = java.util.TreeMap<JDecimal, Int>()
        val actualMap = java.util.TreeMap<BigDecimal, Int>()
        for (i in cases.indices) {
            expectedMap[jdk[i]] = i
            actualMap[own[i]] = i
        }
        assertEquals(expectedMap.values.toList(), actualMap.values.toList())
        assertEquals(expectedMap.keys.map { it.toString() }, actualMap.keys.map { it.toString() })
    }

    @Test fun contextFieldsIdentityTextAndConstantsMatchThePublicOracle() {
        val pairs = listOf(0, 1, 7, 34, Int.MAX_VALUE).flatMap { precision ->
            JMode.entries.map { mode -> JContext(precision, mode) to MathContext(precision, RoundingMode.valueOf(mode.name)) }
        }
        for ((expected, actual) in pairs) {
            assertEquals(expected.precision, actual.precision)
            assertEquals(expected.roundingMode.name, actual.roundingMode.name)
            assertEquals(expected.toString(), actual.toString())
            assertEquals(actual, MathContext(expected.toString()))
            assertEquals(actual.hashCode(), MathContext(expected.toString()).hashCode())
            for ((otherExpected, otherActual) in pairs) assertEquals(expected.equals(otherExpected), actual.equals(otherActual))
        }
        // Numeric enum/context hash equality across two enum classes is unspecified.
        for (name in listOf("UNLIMITED", "DECIMAL32", "DECIMAL64", "DECIMAL128")) {
            val expected = JContext::class.java.getField(name).get(null) as JContext
            val actual = MathContext::class.java.getField(name).get(null) as MathContext
            assertEquals(expected.toString(), actual.toString(), name)
            assertEquals(MathContext(expected.precision, RoundingMode.valueOf(expected.roundingMode.name)), actual)
        }
    }

    @Test fun roundingModesAndLegacyFieldsMatchIncludingInvalidInputs() {
        assertEquals(JMode.entries.map { it.name }, RoundingMode.entries.map { it.name })
        for (expected in JMode.entries) {
            val actual = RoundingMode.valueOf(expected.name)
            assertEquals(expected.ordinal, actual.ordinal)
            val field = "ROUND_${expected.name}"
            val jdkCode = JDecimal::class.java.getField(field).getInt(null)
            val ownCode = BigDecimal::class.java.getField(field).getInt(null)
            assertEquals(jdkCode, ownCode, field)
            assertEquals(JMode.valueOf(jdkCode).name, RoundingMode.valueOf(ownCode).name)
        }
        fun failure(action: () -> Any): String {
            try { action() } catch (e: IllegalArgumentException) { return e.javaClass.name }
            error("Expected IllegalArgumentException")
        }
        for (code in listOf(Int.MIN_VALUE, -1, 8, Int.MAX_VALUE))
            assertEquals(failure { JMode.valueOf(code) }, failure { RoundingMode.valueOf(code) })
        for (name in listOf("", "up", " UP", "UP ", "HALF-EVEN", "8"))
            assertEquals(failure { JMode.valueOf(name) }, failure { RoundingMode.valueOf(name) })
    }

    /**
     * Retained pinned diagnostic, not a portable public-contract requirement.
     * Throwing getters violate their own method contracts; callback omission/order
     * is a reference observation and cannot define the normative value contract.
     */
    @Test fun pinnedDiagnosticForPoisonedMetadataEqualityHashAndIntegerComparison() {
        val jInteger = object : JInteger("4294967297") {
            override fun toByte(): Byte = super.toInt().toByte()
            override fun toShort(): Short = super.toInt().toShort()
            override fun signum(): Int = error("sign callback")
            override fun bitLength(): Int = error("bits callback")
            override fun toByteArray(): ByteArray = error("bytes callback")
            override fun toString(): String = error("text callback")
        }
        val integer = object : BigInteger("4294967297") {
            override fun signum(): Int = error("sign callback")
            override fun bitLength(): Int = error("bits callback")
            override fun toByteArray(): ByteArray = error("bytes callback")
            override fun toString(): String = error("text callback")
        }
        assertTrue(jInteger.equals(JInteger("4294967297")))
        assertTrue(JInteger("4294967297").equals(jInteger))
        assertTrue(integer.equals(BigInteger("4294967297")))
        assertTrue(BigInteger("4294967297").equals(integer))
        assertEquals(jInteger.hashCode(), integer.hashCode())
        assertEquals(jInteger.compareTo(JInteger("4294967297")), integer.compareTo(BigInteger("4294967297")))
        assertEquals(JInteger("4294967297").compareTo(jInteger), BigInteger("4294967297").compareTo(integer))
        val jDecimal = object : JDecimal("4294967297.00") {
            override fun toByte(): Byte = super.toInt().toByte()
            override fun toShort(): Short = super.toInt().toShort()
            override fun signum(): Int = error("sign callback")
            override fun precision(): Int = error("precision callback")
            override fun scale(): Int = error("scale callback")
            override fun unscaledValue(): JInteger = error("coefficient callback")
            override fun toString(): String = error("text callback")
        }
        val decimal = object : BigDecimal("4294967297.00") {
            override fun signum(): Int = error("sign callback")
            override fun precision(): Int = error("precision callback")
            override fun scale(): Int = error("scale callback")
            override fun unscaledValue(): BigInteger = error("coefficient callback")
            override fun toString(): String = error("text callback")
        }
        assertTrue(jDecimal.equals(JDecimal("4294967297.00")))
        assertTrue(JDecimal("4294967297.00").equals(jDecimal))
        assertTrue(decimal.equals(BigDecimal("4294967297.00")))
        assertTrue(BigDecimal("4294967297.00").equals(decimal))
        assertEquals(jDecimal.hashCode(), decimal.hashCode())
        assertFailsWith<NullPointerException> { JInteger.ONE.compareTo(null) }
        assertFailsWith<NullPointerException> { ComparisonNull574.compareIntegerNull(BigInteger.ONE) }
        assertFailsWith<NullPointerException> { JDecimal.ONE.compareTo(null) }
        assertFailsWith<NullPointerException> { ComparisonNull574.compareDecimalNull(BigDecimal.ONE) }
    }
}

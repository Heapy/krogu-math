// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math.migration

import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.lowestSetBit
import io.heapy.krogu.math.lowestSetBit as lowBit
import java.math.BigInteger as JInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class KotlinLowestSetBitPropertyConformanceTest {
    // Paired ordinary source fixtures: the own form additionally imports the property.
    private fun jdkProperty(value: JInteger): Int = value.lowestSetBit
    private fun ownProperty(value: BigInteger): Int = value.lowestSetBit
    private fun ownAlias(value: BigInteger): Int = value.lowBit

    @Test fun pairedImportedPropertyFormsMatchIndependentLiteralPositions() {
        val cases = listOf(
            "0" to -1, "8" to 3, "-12" to 2,
            "1073741824" to 30, "-1073741824" to 30,
            "1152921504606846976" to 60, "-1152921504606846976" to 60,
            "9223372036854775808" to 63, "-9223372036854775808" to 63,
            "123456789012345678901234567890" to 1,
        )
        for ((text, expected) in cases) {
            val reference = JInteger(text)
            val own = BigInteger(text)
            assertEquals(expected, jdkProperty(reference), text)
            assertEquals(expected, ownProperty(own), text)
            assertEquals(expected, ownAlias(own), text)
            assertEquals(expected, reference.getLowestSetBit(), text)
            assertEquals(expected, own.getLowestSetBit(), text)
        }
    }

    private class TruthfulJdk : JInteger("-1152921504606846976") {
        override fun toByte(): Byte = toInt().toByte()
        override fun toShort(): Short = toInt().toShort()
        override fun getLowestSetBit(): Int = super.getLowestSetBit()
    }

    private class TruthfulOwn : BigInteger("-1152921504606846976") {
        override fun getLowestSetBit(): Int = super.getLowestSetBit()
    }

    private class Marker : RuntimeException()
    private class ThrowingJdk(private val marker: Marker) : JInteger("8") {
        override fun toByte(): Byte = toInt().toByte()
        override fun toShort(): Short = toInt().toShort()
        override fun getLowestSetBit(): Int = throw marker
    }

    private class ThrowingOwn(private val marker: Marker) : BigInteger("8") {
        override fun getLowestSetBit(): Int = throw marker
    }

    @Test fun pairedBaseTypedSourceFixturesPreserveTruthfulResultsAndDirectFailures() {
        assertEquals(60, jdkProperty(TruthfulJdk()))
        assertEquals(60, ownProperty(TruthfulOwn()))
        assertEquals(60, ownAlias(TruthfulOwn()))
        val referenceMarker = Marker()
        val ownMarker = Marker()
        val reference: JInteger = ThrowingJdk(referenceMarker)
        val own: BigInteger = ThrowingOwn(ownMarker)
        assertSame(referenceMarker, assertFailsWith<Marker> { jdkProperty(reference) })
        assertSame(referenceMarker, assertFailsWith<Marker> { reference.getLowestSetBit() })
        assertSame(ownMarker, assertFailsWith<Marker> { ownProperty(own) })
        assertSame(ownMarker, assertFailsWith<Marker> { ownAlias(own) })
        assertSame(ownMarker, assertFailsWith<Marker> { own.getLowestSetBit() })
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math.migration

import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.lowestSetBit
import io.heapy.krogu.math.lowestSetBit as lowBit
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class KotlinLowestSetBitPropertyContractTest {
    @Test fun literalsPreserveZeroSignsAndLimbBoundariesWithoutChangingTheValue() {
        val cases = listOf(
            "0" to -1, "1" to 0, "2" to 1, "3" to 0, "4" to 2, "8" to 3,
            "1073741824" to 30, "1073741825" to 0,
            "1152921504606846976" to 60, "9223372036854775808" to 63,
            "123456789012345678901234567890" to 1,
        )
        for ((positive, expected) in cases) {
            for (text in if (positive == "0") listOf(positive) else listOf(positive, "-$positive")) {
                val value = BigInteger(text)
                val before = value.toByteArray()
                val property: Int = value.lowestSetBit
                val aliased: Int = value.lowBit
                val method: Int = value.getLowestSetBit()
                assertEquals(expected, property, text)
                assertEquals(expected, aliased, text)
                assertEquals(expected, method, text)
                assertEquals(text, value.toString())
                assertContentEquals(before, value.toByteArray())
            }
        }
    }

    private class Truthful : BigInteger("-9223372036854775808") {
        override fun getLowestSetBit(): Int = super.getLowestSetBit()
    }

    @Test fun importedAndAliasedAccessComposeWithTruthfulVirtualGetterAndSuper() {
        val value: BigInteger = Truthful()
        val property: Int = value.lowestSetBit
        val aliased: Int = value.lowBit
        assertEquals(63, property)
        assertEquals(63, aliased)
        assertEquals(63, value.getLowestSetBit())
    }

    private class Marker : RuntimeException()
    private class Throwing(private val marker: Marker) : BigInteger("8") {
        override fun getLowestSetBit(): Int = throw marker
    }

    @Test fun directGetterExceptionIdentitySurvivesBothPropertyImports() {
        val marker = Marker()
        val value: BigInteger = Throwing(marker)
        assertSame(marker, assertFailsWith<Marker> { value.lowestSetBit })
        assertSame(marker, assertFailsWith<Marker> { value.lowBit })
        assertSame(marker, assertFailsWith<Marker> { value.getLowestSetBit() })
    }
}

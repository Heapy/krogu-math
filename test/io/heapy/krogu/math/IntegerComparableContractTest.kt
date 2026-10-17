// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class IntegerComparableContractTest {
    @Test fun ordinarySortingAndGenericExtremaAreUsable() {
        val values = listOf(BigInteger("2"), BigInteger("-1"), BigInteger("0"), BigInteger("1"))
        assertEquals(listOf("-1", "0", "1", "2"), values.sorted().map { it.toString() })
        val minimum: BigInteger = minOf(BigInteger("2"), BigInteger("1"))
        val maximum: BigInteger = maxOf(BigInteger("2"), BigInteger("1"))
        assertEquals("1", minimum.toString())
        assertEquals("2", maximum.toString())
    }
}

package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalComparableContractTest {
    @Test fun ordinarySortingAndGenericExtremaAreUsable() {
        val values = listOf(BigDecimal("2"), BigDecimal("-1"), BigDecimal("0.0"), BigDecimal("1"))
        assertEquals(listOf("-1", "0.0", "1", "2"), values.sorted().map { it.toString() })
        val minimum: BigDecimal = minOf(BigDecimal("2"), BigDecimal("1"))
        val maximum: BigDecimal = maxOf(BigDecimal("2"), BigDecimal("1"))
        assertEquals("1", minimum.toString())
        assertEquals("2", maximum.toString())
    }
}

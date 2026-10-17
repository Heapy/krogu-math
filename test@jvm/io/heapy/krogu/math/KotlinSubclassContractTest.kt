package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class KotlinSubclassContractTest {
    private open class IntegerChild : BigInteger("123") { override fun toInt(): Int = 42 }
    private class IntegerGrandchild : IntegerChild() { override fun toInt(): Int = 43 }
    private open class DecimalChild : BigDecimal("1.23") { override fun toInt(): Int = 52 }
    private class DecimalGrandchild : DecimalChild() { override fun toInt(): Int = 53 }

    @Test fun inheritedNumberJvmNamesDispatchInKotlin() {
        val integers: List<BigInteger> = listOf(IntegerChild(), IntegerGrandchild(), JavaApiContractTest.IntegerSubclass())
        assertEquals(listOf(42, 43, 42), integers.map { it.toInt() })
        assertEquals(listOf(42, 43, 42), integers.map { (it as Number).toInt() })
        val decimals: List<BigDecimal> = listOf(DecimalChild(), DecimalGrandchild(), JavaApiContractTest.DecimalSubclass())
        assertEquals(listOf(52, 53, 52), decimals.map { it.toInt() })
        assertEquals(listOf(52, 53, 52), decimals.map { (it as Number).toInt() })
    }
}

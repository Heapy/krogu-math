package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NodeParallelFrontend539Test {
    @Test fun publicProductsPreserveSignAliasAndInputs() {
        val a = BigInteger.ONE.shiftLeft(30 * 4095).add(BigInteger.valueOf(31))
        val b = BigInteger.ONE.shiftLeft(30 * 4095).add(BigInteger.valueOf(47))
        val oldA = a.toString(16); val oldB = b.toString(16)
        repeat(2) {
            assertEquals(a.multiply(b), a.parallelMultiply(b))
            assertEquals(a.multiply(a), a.parallelMultiply(a))
            assertEquals(a.negate().multiply(b), a.negate().parallelMultiply(b))
            assertEquals(oldA, a.toString(16)); assertEquals(oldB, b.toString(16))
        }
    }
    @Test fun smallAndSubclassRoutesRetainVirtualMultiply() {
        val marker = BigInteger("123456789")
        val custom = object : BigInteger("1" + "0".repeat(37000)) {
            override fun multiply(value: BigInteger): BigInteger = marker
        }
        assertSame(marker, custom.parallelMultiply(custom))
        for (value in -16L..16L) {
            val a = BigInteger.valueOf(value)
            assertEquals(a.multiply(BigInteger.TEN), a.parallelMultiply(BigInteger.TEN))
        }
        assertTrue(BigInteger.ZERO.parallelMultiply(custom).signum() == 0)
    }
}

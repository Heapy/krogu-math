// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** SOURCE ONLY: prepared for the parent's later execution grant. */
class Division530SelectionTest {
    @Test fun quotientOnlyNewtonDivisionAtSquareBoundaries() {
        val one = BigInteger.ONE
        for (bits in listOf(30, 31, 61, 127, 256)) {
            val power = one.shiftLeft(bits)
            for (root in listOf(power.subtract(one), power, power.add(one))) {
                val square = root.multiply(root)
                val twiceRoot = root.shiftLeft(1)
                val values = listOf(square.subtract(one), square, square.add(twiceRoot))
                val roots = listOf(root.subtract(one), root, root)
                val remainders = listOf(twiceRoot.subtract(BigInteger.TWO), BigInteger.ZERO, twiceRoot)
                for (i in values.indices) {
                    assertEquals(roots[i], values[i].sqrt())
                    val pair = values[i].sqrtAndRemainder()
                    assertEquals(roots[i], pair[0])
                    assertEquals(remainders[i], pair[1])
                }
            }
        }
        assertEquals(BigInteger.ZERO, BigInteger.ZERO.sqrt())
        assertEquals(one, one.sqrt())
        assertFailsWith<ArithmeticException> { one.negate().sqrt() }
    }

    private class StoredSquare(text: String) : BigInteger(text) {
        private fun callback(): Nothing = error("unexpected virtual callback")
        override fun signum(): Int = callback()
        override fun bitLength(): Int = callback()
        override fun divide(value: BigInteger): BigInteger = callback()
        override fun divideAndRemainder(value: BigInteger): Array<BigInteger> = callback()
    }

    @Test fun quotientOnlyNewtonConsumerUsesStoredReceiver() {
        // Literal decimal square, with literal expected root independent of division.
        val square = StoredSquare("10000000000000000000000000000000000000000")
        assertEquals(BigInteger("100000000000000000000"), square.sqrt())
        assertEquals(BigInteger.ZERO, square.sqrtAndRemainder()[1])
    }
}

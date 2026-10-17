package io.heapy.krogu.math

import kotlin.test.*

class ShiftSubclassTest {
    private class RightPoison(text: String) : BigInteger(text) {
        override fun shiftRight(n: Int): BigInteger = error("shiftRight override called")
        override fun getLowestSetBit(): Int = error("getLowestSetBit override called")
    }
    private class LeftPoison(text: String) : BigInteger(text) {
        override fun shiftLeft(n: Int): BigInteger = error("shiftLeft override called")
        override fun getLowestSetBit(): Int = error("getLowestSetBit override called")
    }

    @Test fun shiftsUseStoredValueAndMatchResultIdentity() {
        val texts = listOf("0", "1", "-1", "42", "-42", "-1073741824") +
            listOf(BigInteger.ONE.shiftLeft(120).negate(), BigInteger.ONE.shiftLeft(120).add(BigInteger.ONE).negate()).map { it.toString() }
        for (text in texts) {
            val plain = BigInteger(text)
            val left = RightPoison(text)
            val right = LeftPoison(text)
            for (distance in listOf(-121, -120, -119, -31, -30, -1, 0, 1, 30, 31, 119, 120, 121)) {
                val l = left.shiftLeft(distance)
                val r = right.shiftRight(distance)
                assertEquals(plain.shiftLeft(distance), l)
                assertEquals(plain.shiftRight(distance), r)
                if (text == "0") {
                    assertSame(BigInteger.ZERO, l)
                    assertSame(BigInteger.ZERO, r)
                } else if (distance == 0) {
                    assertSame(left, l)
                    assertSame(right, r)
                } else {
                    assertEquals(BigInteger::class, l::class)
                    assertEquals(BigInteger::class, r::class)
                }
            }
            assertEquals(plain.shiftLeft(Int.MIN_VALUE), left.shiftLeft(Int.MIN_VALUE))
            assertEquals(plain.shiftRight(Int.MAX_VALUE), right.shiftRight(Int.MAX_VALUE))
            if (text == "0") assertSame(BigInteger.ZERO, right.shiftRight(Int.MIN_VALUE))
            else assertFailsWith<ArithmeticException> { right.shiftRight(Int.MIN_VALUE) }
        }
    }
}

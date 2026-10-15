package io.heapy.krogu.math

import kotlin.test.*

class BitwiseSubclassTest {
    private open class ArithmeticChild(text: String) : BigInteger(text) {
        override fun abs(): BigInteger = error("abs override called")
        override fun negate(): BigInteger = error("negate override called")
        override fun add(value: BigInteger): BigInteger = error("add override called")
        override fun subtract(value: BigInteger): BigInteger = error("subtract override called")
    }
    private class LogicalChild(text: String) : ArithmeticChild(text) {
        override fun not(): BigInteger = error("not override called")
        override fun and(value: BigInteger): BigInteger = error("and override called")
        override fun testBit(n: Int): Boolean = error("testBit override called")
        override fun setBit(n: Int): BigInteger = error("setBit override called")
        override fun clearBit(n: Int): BigInteger = error("clearBit override called")
    }

    @Test fun inheritedBitOperationsUseStoredValue() {
        for (text in listOf("0", "1", "42", "-1", "-42", "1073741824", "-1073741824")) {
            val plain = BigInteger(text)
            val arithmetic = ArithmeticChild(text)
            val logical = LogicalChild(text)
            assertEquals(plain.bitLength(), arithmetic.bitLength())
            assertEquals(plain.bitCount(), arithmetic.bitCount())
            assertEquals(plain.not(), arithmetic.not())
            assertEquals(plain.andNot(BigInteger.TEN), logical.andNot(LogicalChild("10")))
            for (bit in listOf(0, 1, 29, 30, 31, 63, 90)) {
                assertEquals(plain.testBit(bit), arithmetic.testBit(bit))
                val set = arithmetic.setBit(bit)
                val clear = arithmetic.clearBit(bit)
                assertEquals(plain.setBit(bit), set)
                assertEquals(plain.clearBit(bit), clear)
                assertEquals(BigInteger::class, set::class)
                assertEquals(BigInteger::class, clear::class)
                assertEquals(plain.flipBit(bit), logical.flipBit(bit))
            }
        }
    }
}

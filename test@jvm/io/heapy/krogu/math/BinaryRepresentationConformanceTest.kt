package io.heapy.krogu.math

import kotlin.test.*
import java.math.BigInteger as JInteger

class BinaryRepresentationConformanceTest {
    @Test fun everyTwoByteEncoding() {
        for (bits in 0..65535) {
            val bytes = byteArrayOf((bits ushr 8).toByte(), bits.toByte())
            val expected = JInteger(bytes)
            val actual = BigInteger(bytes)
            assertEquals(expected.toLong(), actual.toLong())
            assertEquals(expected.bitLength(), actual.bitLength())
            assertContentEquals(expected.toByteArray(), actual.toByteArray())
        }
    }

    @Test fun largeBinaryInputs() {
        val random = java.util.Random(831772L)
        for (size in listOf(1, 2, 15, 16, 30, 31, 1024, 4096, 65536)) {
            val bytes = ByteArray(size).also(random::nextBytes)
            for (sign in listOf(-1, 1)) {
                val expected = JInteger(sign, bytes)
                val actual = BigInteger(sign, bytes)
                assertContentEquals(expected.toByteArray(), actual.toByteArray(), "size=$size sign=$sign")
                assertEquals(expected.bitLength(), actual.bitLength())
                assertEquals(expected.toLong(), actual.toLong())
                assertEquals(expected.hashCode(), actual.hashCode(), "hash size=$size sign=$sign")
            }
            assertContentEquals(JInteger(bytes).toByteArray(), BigInteger(bytes).toByteArray())
        }
    }

    @Test fun boundedModularProductsMatchFullPrecisionOracle() {
        val random = java.util.Random(713449L)
        for (bits in listOf(1, 2, 30, 31, 60, 61, 128, 512)) repeat(32) {
            val modulus = JInteger(bits, random).add(JInteger.ONE)
            val a = JInteger(bits, random).mod(modulus)
            val b = JInteger(bits, random).mod(modulus)
            val expected = a.multiply(b).mod(modulus)
            assertEquals(expected.toString(), BigInteger(a.toString()).multiplyModuloBounded(
                BigInteger(b.toString()), BigInteger(modulus.toString())).toString())
        }
    }
}

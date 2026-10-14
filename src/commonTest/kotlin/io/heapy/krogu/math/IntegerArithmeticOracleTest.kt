// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegerArithmeticOracleTest {
    @Test fun limbBoundariesThrough512Bits() = verify(1..512)
    @Test fun arithmeticThrough1024Bits() = verify(513..1024)
    @Test fun arithmeticThrough4096Bits() = verify(1025..4096)
    @Test fun arithmeticThrough8192Bits() = verify(4097..8192)
    @Test fun arithmeticThrough16384Bits() = verify(8193..16384)

    private fun verify(sizes: IntRange) {
        val cases = jdk25IntegerArithmeticCases().filter { it[0].substringBefore(':').toInt() in sizes }
        assertTrue(cases.isNotEmpty())
        for (row in cases) {
            val a = BigInteger(row[1].hexToByteArray())
            val b = BigInteger(row[2].hexToByteArray())
            fun same(index: Int, actual: BigInteger, operation: String) =
                assertContentEquals(row[index].hexToByteArray(), actual.toByteArray(), "${row[0]} $operation")
            same(3, a.add(b), "add")
            same(4, a.subtract(b), "subtract")
            same(5, a.multiply(b), "multiply")
            same(5, a.parallelMultiply(b), "parallelMultiply")
            val qr = a.divideAndRemainder(b)
            assertEquals(2, qr.size)
            same(6, qr[0], "quotient")
            same(7, qr[1], "remainder")
            assertEquals(a, qr[0].multiply(b).add(qr[1]), "${row[0]} q*b+r")
            assertTrue(qr[1].abs() < b.abs(), "${row[0]} remainder bound")
            assertTrue(qr[1].signum() == 0 || qr[1].signum() == a.signum(), "${row[0]} remainder sign")
        }
    }
}

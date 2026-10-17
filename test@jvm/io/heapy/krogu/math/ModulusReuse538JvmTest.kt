// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import java.math.BigInteger as JdkInteger

/** Reflection observes only our private engine; no production test hooks or public API. */
class ModulusReuse538JvmTest {
    private val contextType = Class.forName("io.heapy.krogu.math.BigInteger\$OddModulusContext")
    private val companion = contextType.getDeclaredField("Companion").apply { isAccessible = true }.get(null)
    private val create = companion.javaClass.getDeclaredMethod("create", BigInteger::class.java).apply { isAccessible = true }
    private val prepare = contextType.getDeclaredMethod("witnesses", BigInteger::class.java, Int::class.javaPrimitiveType, BigInteger::class.java)
        .apply { isAccessible = true }

    private fun hostWitness(a: JdkInteger, n: JdkInteger): Boolean {
        val minusOne = n.subtract(JdkInteger.ONE)
        val s = minusOne.lowestSetBit
        var x = a.modPow(minusOne.shiftRight(s), n)
        if (x == JdkInteger.ONE || x == minusOne) return true
        for (r in 1 until s) {
            x = x.multiply(x).mod(n)
            if (x == minusOne) return true
        }
        return false
    }

    @Test fun everySmallResidueMatchesHostAcrossReusedSessions() {
        for (n in listOf(53, 97, 193, 257, 3127)) {
            val modulus = BigInteger(n.toString())
            val nm1 = modulus.subtract(BigInteger.ONE)
            val s = nm1.getLowestSetBit()
            val context = requireNotNull(create.invoke(companion, modulus))
            val session = prepare.invoke(context, nm1.shiftRight(s), s, nm1)
            val passes = session.javaClass.getDeclaredMethod("passes", BigInteger::class.java).apply { isAccessible = true }
            val residues = if (n < 300) (0 until n).toList() else listOf(0, 1, 2, 53, 59, 123, n - 2, n - 1)
            for (a in residues + residues.reversed()) {
                assertEquals(hostWitness(JdkInteger.valueOf(a.toLong()), JdkInteger.valueOf(n.toLong())),
                    passes.invoke(session, BigInteger(a.toString())), "a=$a n=$n")
            }
        }
    }

    @Test fun varyingOperandWidthsAndMixedContextsDoNotRetainScratchDigits() {
        val moduli = listOf("2147483647", "2305843009213693951", "170141183460469231731687303715884105727")
        for (text in moduli) {
            val m = BigInteger(text)
            val nm1 = m.subtract(BigInteger.ONE)
            val s = nm1.getLowestSetBit()
            val context = requireNotNull(create.invoke(companion, m))
            val arrays = listOf("modulus", "oneMont", "r2").map { name ->
                val array = contextType.getDeclaredField(name).apply { isAccessible = true }.get(context) as IntArray
                array to array.copyOf()
            }
            val session = prepare.invoke(context, nm1.shiftRight(s), s, nm1)
            val passes = session.javaClass.getDeclaredMethod("passes", BigInteger::class.java).apply { isAccessible = true }
            val inputs = listOf(nm1, BigInteger.TWO, m.subtract(BigInteger.TWO), BigInteger.ZERO,
                BigInteger.ONE, BigInteger.ONE.shiftLeft(30), BigInteger("3"))
            repeat(3) {
                for (a in inputs) {
                    assertEquals(hostWitness(JdkInteger(a.toString()), JdkInteger(text)), passes.invoke(session, a), "m=$text a=$a")
                    for ((array, before) in arrays) assertTrue(array.contentEquals(before), "context constants stay immutable")
                }
            }
        }
        for (text in listOf("0", "1", "-17", "18")) assertNull(create.invoke(companion, BigInteger(text)))
        val overCap = BigInteger.ONE.shiftLeft(4096 * 30).add(BigInteger.ONE)
        assertNull(create.invoke(companion, overCap))
    }
}

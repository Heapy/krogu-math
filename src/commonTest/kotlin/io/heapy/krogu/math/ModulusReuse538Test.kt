// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ModulusReuse538Test {
    @Test fun repeatedCallsKeepWitnessSquaresAndInputsIndependent() {
        // Fermat primes exercise d=1 and long repeated-squaring tails; Mersenne
        // primes exercise s=1. Composite factors all exceed the trial prefix.
        val primes = listOf("53", "97", "193", "257", "65537", "2147483647", "2305843009213693951",
            "170141183460469231731687303715884105727")
        val composites = listOf("3127", "5141", "341550071728321", "3825123056546413051")
        for ((text, expected) in primes.map { it to true } + composites.map { it to false }) {
            for (sign in listOf("", "-")) {
                val value = BigInteger(sign + text)
                val before = value.toByteArray()
                for (certainty in listOf(1, 100, 1)) assertEquals(expected, value.isProbablePrime(certainty), "$sign$text c=$certainty")
                assertTrue(before.contentEquals(value.toByteArray()), "candidate snapshot $sign$text")
            }
        }
    }

    @Test fun modulusSubclassesKeepPublicWitnessCallbacksAndFailures() {
        val value = object : BigInteger("53") {
            var parityCalls = 0
            var lengthCalls = 0
            override fun testBit(n: Int): Boolean { parityCalls++; return super.testBit(n) }
            override fun bitLength(): Int { lengthCalls++; return super.bitLength() }
        }
        assertTrue(value.isProbablePrime(1))
        // Prefix parity + the public modPow parity callback for all 13 bases.
        assertEquals(14, value.parityCalls)
        assertEquals(1, value.lengthCalls)
        val failure = IllegalStateException("first witness parity callback")
        val throwing = object : BigInteger("53") {
            var calls = 0
            override fun testBit(n: Int): Boolean {
                if (++calls == 2) throw failure
                return super.testBit(n)
            }
        }
        assertSame(failure, assertFailsWith<IllegalStateException> { throwing.isProbablePrime(1) })
    }

    @Test fun subtypeAbsMayReturnOrdinaryWorkingValueWithoutLosingItsPrefix() {
        val trace = mutableListOf<String>()
        val value = object : BigInteger("-65537") {
            override fun abs(): BigInteger { trace += "abs"; return super.abs() }
            override fun negate(): BigInteger { trace += "negate"; return super.negate() }
            override fun testBit(n: Int): Boolean = error("original negative receiver must not be used after abs")
        }
        assertTrue(value.isProbablePrime(100))
        assertEquals(listOf("abs", "negate"), trace)
    }
}

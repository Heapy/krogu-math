// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** SOURCE ONLY: UNCOMPILED/UNRUN. Literal/algebraic contracts, no runtime trace oracle. */
class IntegerArithmetic544ContractTest {
    private val zero = BigInteger.ZERO
    private val one = BigInteger.ONE

    private fun hex(text: String) = BigInteger(text, 16)

    @Test fun denseCarryBorrowAndProductsHaveIndependentHexExpectations() {
        // P=16^digits. Expected strings encode P-1, P, (P-1)^2 and P^2-1.
        for (digits in listOf(15, 30, 60)) {
            val p = hex("1" + "0".repeat(digits))
            val low = hex("f".repeat(digits))
            val high = hex("1" + "0".repeat(digits - 1) + "1")
            val square = hex("f".repeat(digits - 1) + "e" + "0".repeat(digits - 1) + "1")
            val product = hex("f".repeat(2 * digits))
            assertEquals(p, low.add(one))
            assertEquals(low, p.subtract(one))
            assertEquals(p.negate(), low.negate().subtract(one))
            assertEquals(low.negate(), p.negate().add(one))
            assertEquals(zero, low.add(low.negate()))
            assertEquals(square, low.multiply(low))
            assertEquals(square, low.negate().multiply(low.negate()))
            assertEquals(product, low.multiply(high))
            assertEquals(product.negate(), low.parallelMultiply(high.negate()))
        }
    }

    @Test fun highestDifferentDigitDominatesAllOpposingLowDigits() {
        // P-1 < P < P+1, even though every lower digit of P-1 is maximal.
        val ordered = listOf("-1152921504606846977", "-1152921504606846976",
            "-1152921504606846975", "-1", "0", "1", "1152921504606846975",
            "1152921504606846976", "1152921504606846977").map(::BigInteger)
        for (i in ordered.indices) for (j in ordered.indices) {
            assertEquals(i.compareTo(j), ordered[i].compareTo(ordered[j]).coerceIn(-1, 1))
        }
    }

    @Test fun denseProductFeedsSignedDivisionEuclidAndFloorRoot() {
        val p = BigInteger("1152921504606846976") // 2^60
        val low = BigInteger("1152921504606846975")
        val high = BigInteger("1152921504606846977")
        val product = hex("f".repeat(30)) // P^2-1, independently encoded.
        assertEquals(product, low.multiply(high))
        for (a in listOf(product, product.negate())) for (d in listOf(p, p.negate())) {
            val q = if (a.signum() == d.signum()) low else low.negate()
            val r = if (a.signum() > 0) low else low.negate()
            val pair = a.divideAndRemainder(d)
            assertEquals(q, pair[0])
            assertEquals(r, pair[1])
            assertEquals(q, a.divide(d))
            assertEquals(r, a.remainder(d))
            assertEquals(a, q.multiply(d).add(r))
            assertEquals(one, a.gcd(d)) // P^2-1 == -1 (mod P).
            assertEquals(low, a.gcd(low.negate()))
        }
        val rootPair = product.sqrtAndRemainder()
        assertEquals(low, product.sqrt())
        assertEquals(low, rootPair[0])
        assertEquals(BigInteger("2305843009213693950"), rootPair[1]) // 2P-2.
        assertEquals(product, rootPair[0].multiply(rootPair[0]).add(rootPair[1]))
        assertTrue(rootPair[0].add(one).multiply(rootPair[0].add(one)) > product)
    }

    @Test fun powersCrossCompactUnsignedAndShiftPackingBoundaries() {
        for ((e, expected) in listOf(31 to "617673396283947", 32 to "1853020188851841",
            33 to "5559060566555523")) {
            assertEquals(BigInteger(expected), BigInteger("3").pow(e))
            val signed = if (e % 2 == 0) BigInteger(expected) else BigInteger(expected).negate()
            assertEquals(signed, BigInteger("-3").pow(e))
        }
        // Unsigned compact square exceeds Long.MAX_VALUE but is below 2^64.
        assertEquals(BigInteger("18446744065119617025"), BigInteger("4294967295").pow(2))
        // 6^e=3^e*2^e: final shifts lie on opposite sides of the compact 64-bit gate.
        assertEquals(BigInteger("3486784401").shiftLeft(20), BigInteger("6").pow(20))
        assertEquals(BigInteger("31381059609").shiftLeft(22), BigInteger("6").pow(22))
    }

    private class Truthful(text: String, val events: MutableList<String>) : BigInteger(text) {
        private val ordinary = BigInteger(text)
        private fun traced(name: String, body: () -> BigInteger): BigInteger {
            events += "$name:enter"
            val result = body()
            events += "$name:exit"
            return result
        }
        // Every wrapper's stored value equals the value returned by its inherited operation.
        override fun abs(): BigInteger = Truthful(ordinary.abs().toString(), events)
        override fun bitLength(): Int { events += "bits"; return super.bitLength() }
        override fun getLowestSetBit(): Int { events += "lowest"; return super.getLowestSetBit() }
        override fun shiftRight(n: Int): BigInteger = Truthful(ordinary.shiftRight(n).toString(), events)
        // A super identity path may return this/operand. Snapshot before composing virtual calls.
        private fun plain(value: BigInteger) = BigInteger(value.toByteArray())
        override fun add(value: BigInteger): BigInteger = traced("add") { plain(super.add(value)).subtract(ZERO) }
        override fun subtract(value: BigInteger): BigInteger = traced("subtract") { plain(super.subtract(value)).add(ZERO) }
        override fun multiply(value: BigInteger): BigInteger = traced("multiply") { plain(super.multiply(value)).add(ZERO) }
        override fun divide(value: BigInteger): BigInteger = traced("divide") { plain(super.divide(value)).add(ZERO) }
        override fun remainder(value: BigInteger): BigInteger = traced("remainder") { plain(super.remainder(value)).add(ZERO) }
        override fun gcd(value: BigInteger): BigInteger = traced("gcd") { plain(super.gcd(value)).add(ZERO) }
        override fun pow(exponent: Int): BigInteger = traced("pow") { plain(super.pow(exponent)).add(ZERO) }
        override fun sqrt(): BigInteger = traced("sqrt") { plain(super.sqrt()).add(ZERO) }
        override fun compareTo(other: BigInteger): Int {
            events += "compare:enter"
            val result = super.compareTo(other)
            events += "compare:exit"
            return result
        }
        fun inheritedAdd(other: BigInteger): BigInteger = super.add(other)
        fun inheritedMultiply(other: BigInteger): BigInteger = super.multiply(other)
        fun inheritedPower(exponent: Int): BigInteger = super.pow(exponent)
        fun inheritedCompare(other: BigInteger): Int = super.compareTo(other)
    }

    @Test fun baseTypedKotlinCallsComposeTruthfulOverridesAndSuper() {
        val events = mutableListOf<String>()
        val source: BigInteger = Truthful("-12", events)
        val operand: BigInteger = Truthful("5", events)
        val calls: List<Triple<String, String, () -> BigInteger>> = listOf(
            Triple("add", "-7") { source.add(operand) },
            Triple("subtract", "-17") { source.subtract(operand) },
            Triple("multiply", "-60") { source.multiply(operand) },
            Triple("divide", "-2") { source.divide(operand) },
            Triple("remainder", "-2") { source.remainder(operand) },
            Triple("gcd", "1") { source.gcd(operand) },
            Triple("pow", "-1728") { source.pow(3) },
        )
        for ((name, expected, call) in calls) {
            events.clear()
            assertEquals(BigInteger(expected), call())
            assertEquals("$name:enter", events.first())
            assertEquals("$name:exit", events.last())
        }
        events.clear()
        assertEquals(-1, source.compareTo(operand))
        assertEquals("compare:enter", events.first())
        assertEquals("compare:exit", events.last())
        val square: BigInteger = Truthful("144", events)
        events.clear()
        assertEquals(BigInteger("12"), square.sqrt())
        assertEquals("sqrt:enter", events.first())
        assertEquals("sqrt:exit", events.last())
        // Pair APIs and parallelMultiply have value contracts, no prescribed internal callbacks.
        assertEquals(BigInteger("-60"), source.parallelMultiply(operand))
        assertEquals(listOf(BigInteger("-2"), BigInteger("-2")), source.divideAndRemainder(operand).toList())
        assertEquals(listOf(BigInteger("12"), zero), square.sqrtAndRemainder().toList())
    }

    @Test fun explicitSuperAndSelfAliasRetainValuesAcrossReturnedWrappers() {
        val events = mutableListOf<String>()
        val source = Truthful("4294967295", events)
        assertEquals(BigInteger("8589934590"), source.add(source))
        assertEquals(BigInteger("8589934590"), source.inheritedAdd(source))
        val square = BigInteger("18446744065119617025")
        assertEquals(square, source.multiply(source))
        assertEquals(square, source.inheritedMultiply(source))
        assertEquals(square, source.inheritedPower(2))
        assertEquals(0, source.inheritedCompare(source))
        assertEquals(source, source.add(zero))
        assertEquals(source, source.subtract(zero))
        assertEquals(source, source.pow(1))
        assertEquals(source, Truthful("0", events).add(source))
        // Fresh truthful abs and shiftRight wrappers carry the same stored number into pow.
        assertEquals(BigInteger("5559060566555523"), Truthful("3", events).inheritedPower(33))
        assertEquals(BigInteger("31381059609").shiftLeft(22), Truthful("-6", events).inheritedPower(22))
        assertEquals(BigInteger("4294967295"), source)
    }

    @Test fun zeroIdentityAndMathematicalErrorsDoNotRequireHugeAllocation() {
        for (text in listOf("0", "1", "-1", "12", "-12")) {
            val value = BigInteger(text)
            assertEquals(one, value.pow(0))
            assertEquals(value, value.pow(1))
            assertEquals(value, value.add(zero))
            assertEquals(zero, value.multiply(zero))
            assertEquals(value.abs(), value.gcd(zero))
            assertEquals(value.abs(), zero.gcd(value))
            assertFailsWith<ArithmeticException> { value.pow(-1) }
            assertFailsWith<ArithmeticException> { value.divide(zero) }
            assertFailsWith<ArithmeticException> { value.remainder(zero) }
            assertFailsWith<ArithmeticException> { value.divideAndRemainder(zero) }
        }
        assertEquals(zero, zero.gcd(zero))
        assertEquals(one, one.pow(Int.MAX_VALUE))
        assertEquals(BigInteger("-1"), BigInteger("-1").pow(Int.MAX_VALUE))
        assertEquals(zero, zero.sqrt())
        assertFailsWith<ArithmeticException> { BigInteger("-1").sqrt() }
        assertFailsWith<ArithmeticException> { BigInteger("-1").sqrtAndRemainder() }
    }

    @Test fun explicitOverrideFailurePreservesTheCallerMarkerBeforeSuperValidation() {
        val marker = IllegalStateException("explicit caller marker")
        val source: BigInteger = object : BigInteger("-12") {
            override fun pow(exponent: Int): BigInteger = throw marker
            override fun divide(value: BigInteger): BigInteger = throw marker
            override fun sqrt(): BigInteger = throw marker
        }
        assertSame(marker, assertFailsWith<IllegalStateException> { source.pow(-1) })
        assertSame(marker, assertFailsWith<IllegalStateException> { source.divide(zero) })
        assertSame(marker, assertFailsWith<IllegalStateException> { source.sqrt() })
    }
}

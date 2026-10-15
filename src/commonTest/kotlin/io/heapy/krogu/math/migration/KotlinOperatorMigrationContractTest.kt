// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math.migration

import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.and
import io.heapy.krogu.math.and as kroguAnd
import io.heapy.krogu.math.dec
import io.heapy.krogu.math.inc
import io.heapy.krogu.math.inv
import io.heapy.krogu.math.or
import io.heapy.krogu.math.or as kroguOr
import io.heapy.krogu.math.shl
import io.heapy.krogu.math.shr
import io.heapy.krogu.math.xor
import io.heapy.krogu.math.xor as kroguXor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/** Prepared migration examples: explicit imports and declared result types check overload selection. */
class KotlinOperatorMigrationContractTest {
    @Test fun integerPrefixAndPostfixKeepAliasesAndCrossSignedBoundaries() {
        val cases = listOf(
            Triple("-1", "0", "-2"), Triple("0", "1", "-1"), Triple("1", "2", "0"),
            Triple("9223372036854775807", "9223372036854775808", "9223372036854775806"),
            Triple("-9223372036854775808", "-9223372036854775807", "-9223372036854775809"),
            Triple("18446744073709551615", "18446744073709551616", "18446744073709551614"),
        )
        for ((text, incremented, decremented) in cases) {
            val original = BigInteger(text)
            var value: BigInteger = original
            val old: BigInteger = value++
            assertSame(original, old)
            assertEquals(text, original.toString())
            assertEquals(incremented, value.toString())
            value = original
            val next: BigInteger = ++value
            assertSame(value, next)
            assertEquals(incremented, next.toString())
            value = original
            val prior: BigInteger = value--
            assertSame(original, prior)
            assertEquals(decremented, value.toString())
            value = original
            val previous: BigInteger = --value
            assertSame(value, previous)
            assertEquals(decremented, previous.toString())
            assertEquals(text, original.toString())
        }
    }

    @Test fun decimalIncrementAndDecrementUseExactPreferredScale() {
        val cases = listOf(
            Triple("1.25", "2.25", "0.25"), Triple("0.00", "1.00", "-1.00"),
            Triple("-1.00", "0.00", "-2.00"), Triple("1E+3", "1001", "999"),
            Triple("99999999999999999999.999", "100000000000000000000.999", "99999999999999999998.999"),
        )
        for ((text, incremented, decremented) in cases) {
            val original = BigDecimal(text)
            var value: BigDecimal = original
            val old: BigDecimal = value++
            assertSame(original, old)
            assertEquals(BigDecimal(incremented), value)
            value = original
            val next: BigDecimal = ++value
            assertSame(value, next)
            assertEquals(BigDecimal(incremented), next)
            value = original
            val prior: BigDecimal = value--
            assertSame(original, prior)
            assertEquals(BigDecimal(decremented), value)
            value = original
            val previous: BigDecimal = --value
            assertSame(value, previous)
            assertEquals(BigDecimal(decremented), previous)
            assertEquals(BigDecimal(text), original)
        }
    }

    @Test fun infixBitsAndSignedShiftsKeepUnboundedTwosComplementValues() {
        val negative = BigInteger("-9")
        val five = BigInteger("5")
        val conjunction: BigInteger = negative and five
        val disjunction: BigInteger = negative or five
        val exclusive: BigInteger = negative xor five
        val inverse: BigInteger = negative.inv()
        assertEquals(listOf("5", "-9", "-14", "8"),
            listOf(conjunction, disjunction, exclusive, inverse).map { it.toString() })
        val left: BigInteger = negative shl 2
        val right: BigInteger = negative shr 2
        val reversedLeft: BigInteger = negative.shl(n = -2)
        val reversedRight: BigInteger = negative.shr(n = -2)
        assertEquals(listOf("-36", "-3", "-3", "-36"),
            listOf(left, right, reversedLeft, reversedRight).map { it.toString() })
        assertEquals(negative, negative shl 0)
        assertEquals(negative, negative shr 0)
        assertEquals(BigInteger.ZERO, BigInteger.ZERO shl 65)
        assertEquals(BigInteger.ZERO, BigInteger.ZERO shr -65)
        assertEquals(BigInteger("-1"), BigInteger.ZERO.inv())
        assertEquals(BigInteger.ZERO, BigInteger("-1").inv())
        val wide: BigInteger = BigInteger.ONE shl 64
        assertEquals("18446744073709551616", wide.toString())
        assertEquals("-18446744073709551617", (wide xor BigInteger("-1")).toString())
        assertEquals("18446744073709551617", (wide xor BigInteger.ONE).toString())
    }

    private class Marker : RuntimeException()

    @Test fun namedOperandsAndAliasImportsKeepBothPublicCallShapes() {
        val negative = BigInteger("-9")
        val five = BigInteger("5")
        val conjunction: BigInteger = negative.and(other = five)
        val disjunction: BigInteger = negative.or(other = five)
        val exclusive: BigInteger = negative.xor(other = five)
        val named: List<BigInteger> = listOf(conjunction, disjunction, exclusive)
        val member: List<BigInteger> = listOf(
            negative.and(value = five), negative.or(value = five), negative.xor(value = five),
        )
        val aliased: List<BigInteger> = listOf(
            negative.kroguAnd(other = five), negative.kroguOr(other = five), negative.kroguXor(other = five),
        )
        val infix: List<BigInteger> = listOf(negative and five, negative or five, negative xor five)
        val aliasedInfix: List<BigInteger> = listOf(negative kroguAnd five, negative kroguOr five, negative kroguXor five)
        for (values in listOf(named, member, aliased, infix, aliasedInfix))
            assertEquals(listOf("5", "-9", "-14"), values.map { it.toString() })
    }

    @Test fun integerExtensionsAndInfixMembersPreserveExplicitDispatchAndFailureIdentity() {
        val replacement = BigInteger("701")
        val marker = Marker()
        val calls = mutableListOf<String>()
        var fail = false
        fun result(call: String): BigInteger {
            calls += call
            if (fail) throw marker
            return replacement
        }
        val receiver: BigInteger = object : BigInteger("9") {
            override fun add(value: BigInteger): BigInteger {
                assertEquals(BigInteger.ONE, value)
                return result("add")
            }
            override fun subtract(value: BigInteger): BigInteger {
                assertEquals(BigInteger.ONE, value)
                return result("subtract")
            }
            override fun shiftLeft(n: Int): BigInteger = result("left:$n")
            override fun shiftRight(n: Int): BigInteger = result("right:$n")
            override fun not(): BigInteger = result("not")
            override fun and(value: BigInteger): BigInteger {
                assertSame(BigInteger.TEN, value)
                return result("and")
            }
            override fun or(value: BigInteger): BigInteger {
                assertSame(BigInteger.TEN, value)
                return result("or")
            }
            override fun xor(value: BigInteger): BigInteger {
                assertSame(BigInteger.TEN, value)
                return result("xor")
            }
        }
        val operations: List<Pair<String, () -> BigInteger>> = listOf(
            "add" to { receiver.inc() }, "subtract" to { receiver.dec() },
            "left:${Int.MIN_VALUE}" to { receiver shl Int.MIN_VALUE },
            "right:${Int.MAX_VALUE}" to { receiver shr Int.MAX_VALUE }, "not" to { receiver.inv() },
            "and" to { receiver and BigInteger.TEN }, "or" to { receiver or BigInteger.TEN },
            "xor" to { receiver xor BigInteger.TEN },
            "and" to { receiver.and(other = BigInteger.TEN) },
            "or" to { receiver.or(other = BigInteger.TEN) },
            "xor" to { receiver.xor(other = BigInteger.TEN) },
            "and" to { receiver.kroguAnd(other = BigInteger.TEN) },
            "or" to { receiver.kroguOr(other = BigInteger.TEN) },
            "xor" to { receiver.kroguXor(other = BigInteger.TEN) },
        )
        for ((label, call) in operations) {
            calls.clear()
            assertSame(replacement, call(), label)
            assertEquals(listOf(label), calls)
            fail = true
            calls.clear()
            assertSame(marker, assertFailsWith<Marker>(label) { call() })
            assertEquals(listOf(label), calls)
            fail = false
        }
    }

    @Test fun decimalExtensionsPreserveExplicitExactOverloadDispatchAndFailureIdentity() {
        val replacement = BigDecimal("7.0100")
        val marker = Marker()
        val calls = mutableListOf<String>()
        var fail = false
        val receiver: BigDecimal = object : BigDecimal("1.25") {
            override fun add(augend: BigDecimal): BigDecimal {
                assertEquals(BigDecimal.ONE, augend)
                calls += "add"
                if (fail) throw marker
                return replacement
            }
            override fun subtract(subtrahend: BigDecimal): BigDecimal {
                assertEquals(BigDecimal.ONE, subtrahend)
                calls += "subtract"
                if (fail) throw marker
                return replacement
            }
        }
        for ((label, call) in listOf<Pair<String, () -> BigDecimal>>(
            "add" to { receiver.inc() }, "subtract" to { receiver.dec() },
        )) {
            calls.clear()
            assertSame(replacement, call())
            assertEquals(listOf(label), calls)
            fail = true
            calls.clear()
            assertSame(marker, assertFailsWith<Marker> { call() })
            assertEquals(listOf(label), calls)
            fail = false
        }
    }

    private class CallerInteger(text: String) : BigInteger(text) {
        operator fun inc(): CallerInteger = CallerInteger("42")
        operator fun dec(): CallerInteger = CallerInteger("40")
    }

    private class CallerDecimal(text: String) : BigDecimal(text) {
        operator fun inc(): CallerDecimal = CallerDecimal("42.00")
        operator fun dec(): CallerDecimal = CallerDecimal("40.00")
    }

    @Test fun callerOwnedIncrementDeclarationsRemainLegalWithoutOverride() {
        var integer = CallerInteger("7")
        val oldInteger: CallerInteger = integer++
        assertEquals("7", oldInteger.toString())
        assertEquals("42", integer.toString())
        val previousInteger: CallerInteger = --integer
        assertEquals("40", previousInteger.toString())
        val baseInteger: BigInteger = CallerInteger("7")
        assertEquals("8", baseInteger.inc().toString())
        var decimal = CallerDecimal("7.00")
        val oldDecimal: CallerDecimal = decimal++
        assertEquals("7.00", oldDecimal.toString())
        assertEquals("42.00", decimal.toString())
        val previousDecimal: CallerDecimal = --decimal
        assertEquals("40.00", previousDecimal.toString())
        val baseDecimal: BigDecimal = CallerDecimal("7.00")
        assertEquals("8.00", baseDecimal.inc().toString())
    }
}

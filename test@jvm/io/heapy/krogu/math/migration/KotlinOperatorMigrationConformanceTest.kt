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
import java.math.BigDecimal as JDecimal
import java.math.BigInteger as JInteger
import kotlin.and
import kotlin.and as jdkAnd
import kotlin.dec as jdkDec
import kotlin.inc as jdkInc
import kotlin.inv as jdkInv
import kotlin.or
import kotlin.or as jdkOr
import kotlin.shl as jdkShl
import kotlin.shr as jdkShr
import kotlin.xor
import kotlin.xor as jdkXor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class KotlinOperatorMigrationConformanceTest {
    @Test fun integerUpdatesMatchPublicKotlinOperatorsWithoutChangingAliases() {
        for (text in listOf("-9223372036854775809", "-1", "0", "1", "9223372036854775807",
            "18446744073709551615", "123456789012345678901234567890")) {
            val jo = JInteger(text)
            val ko = BigInteger(text)
            var j: JInteger = jo
            var k: BigInteger = ko
            val jOld: JInteger = j.jdkInc().let { replacement -> val old = j; j = replacement; old }
            val kOld: BigInteger = k++
            assertSame(jo, jOld)
            assertSame(ko, kOld)
            assertEquals(j.toString(), k.toString(), "postfix increment $text")
            assertEquals(jOld.toString(), kOld.toString())
            j = jo
            k = ko
            val jNext: JInteger = j.jdkInc().also { j = it }
            val kNext: BigInteger = ++k
            assertSame(j, jNext)
            assertSame(k, kNext)
            assertEquals(jNext.toString(), kNext.toString(), "prefix increment $text")
            j = jo
            k = ko
            val jPrior: JInteger = j.jdkDec().let { replacement -> val old = j; j = replacement; old }
            val kPrior: BigInteger = k--
            assertSame(jo, jPrior)
            assertSame(ko, kPrior)
            assertEquals(j.toString(), k.toString(), "postfix decrement $text")
            j = jo
            k = ko
            val jPrevious: JInteger = j.jdkDec().also { j = it }
            val kPrevious: BigInteger = --k
            assertSame(j, jPrevious)
            assertSame(k, kPrevious)
            assertEquals(jPrevious.toString(), kPrevious.toString(), "prefix decrement $text")
            assertEquals(text, jo.toString())
            assertEquals(text, ko.toString())
        }
        assertEquals("9223372036854775808", BigInteger("9223372036854775807").inc().toString())
    }

    private fun representation(value: JDecimal): String = "${value.unscaledValue()}@${value.scale()}"
    private fun representation(value: BigDecimal): String = "${value.unscaledValue()}@${value.scale()}"

    @Test fun exactDecimalUpdatesMatchPublicKotlinCallsAndRetainScale() {
        for (text in listOf("-1000", "-1.00", "-0.00", "0", "1.25", "1E+3", "1E-30",
            "99999999999999999999.999")) {
            val jo = JDecimal(text)
            val ko = BigDecimal(text)
            var j: JDecimal = jo
            var k: BigDecimal = ko
            val jOld = j
            j = j.jdkInc()
            val kOld: BigDecimal = k++
            assertSame(jo, jOld)
            assertSame(ko, kOld)
            assertEquals(representation(j), representation(k), "postfix increment $text")
            j = jo
            k = ko
            val jNext: JDecimal = j.jdkInc().also { j = it }
            val kNext: BigDecimal = ++k
            assertSame(j, jNext)
            assertSame(k, kNext)
            assertEquals(representation(jNext), representation(kNext), "prefix increment $text")
            j = jo
            k = ko
            val jPrior = j
            j = j.jdkDec()
            val kPrior: BigDecimal = k--
            assertSame(jo, jPrior)
            assertSame(ko, kPrior)
            assertEquals(representation(j), representation(k), "postfix decrement $text")
            j = jo
            k = ko
            val jPrevious: JDecimal = j.jdkDec().also { j = it }
            val kPrevious: BigDecimal = --k
            assertSame(j, jPrevious)
            assertSame(k, kPrevious)
            assertEquals(representation(jPrevious), representation(kPrevious), "prefix decrement $text")
            assertEquals(representation(jo), representation(ko), "original $text")
        }
        assertEquals("-100@2", representation(BigDecimal("0.00").dec()))
        assertEquals("1001@0", representation(BigDecimal("1E+3").inc()))
    }

    @Test fun infixBitOperationsAndNamedSignedShiftsMatchPublicKotlinCalls() {
        val texts = listOf("-18446744073709551617", "-9", "-1", "0", "5", "18446744073709551616")
        for (left in texts) {
            val j = JInteger(left)
            val k = BigInteger(left)
            val complement: BigInteger = k.inv()
            assertEquals(j.jdkInv().toString(), complement.toString(), "inv $left")
            for (right in texts) {
                val jr = JInteger(right)
                val kr = BigInteger(right)
                val and: BigInteger = k and kr
                val or: BigInteger = k or kr
                val xor: BigInteger = k xor kr
                assertEquals((j jdkAnd jr).toString(), and.toString(), "$left and $right")
                assertEquals((j jdkOr jr).toString(), or.toString(), "$left or $right")
                assertEquals((j jdkXor jr).toString(), xor.toString(), "$left xor $right")
            }
            for (n in listOf(-65, -2, -1, 0, 1, 2, 65)) {
                val leftShift: BigInteger = k shl n
                val rightShift: BigInteger = k shr n
                assertEquals((j jdkShl n).toString(), leftShift.toString(), "$left shl $n")
                assertEquals((j jdkShr n).toString(), rightShift.toString(), "$left shr $n")
                assertEquals(j.jdkShl(n = n).toString(), k.shl(n = n).toString())
                assertEquals(j.jdkShr(n = n).toString(), k.shr(n = n).toString())
            }
        }
        assertEquals("-14", (BigInteger("-9") xor BigInteger("5")).toString())
    }

    private class Marker : RuntimeException()

    @Test fun namedAndAliasedBitCallsMatchPublicStdlibSourceShapes() {
        val j = JInteger("-9")
        val jr = JInteger("5")
        val k = BigInteger("-9")
        val kr = BigInteger("5")
        val jNamed: List<JInteger> = listOf(j.and(other = jr), j.or(other = jr), j.xor(other = jr))
        val kNamed: List<BigInteger> = listOf(k.and(other = kr), k.or(other = kr), k.xor(other = kr))
        val jAliased: List<JInteger> = listOf(
            j.jdkAnd(other = jr), j.jdkOr(other = jr), j.jdkXor(other = jr),
        )
        val kAliased: List<BigInteger> = listOf(
            k.kroguAnd(other = kr), k.kroguOr(other = kr), k.kroguXor(other = kr),
        )
        val jInfix: List<JInteger> = listOf(j and jr, j or jr, j xor jr)
        val kInfix: List<BigInteger> = listOf(k and kr, k or kr, k xor kr)
        val jAliasedInfix: List<JInteger> = listOf(j jdkAnd jr, j jdkOr jr, j jdkXor jr)
        val kAliasedInfix: List<BigInteger> = listOf(k kroguAnd kr, k kroguOr kr, k kroguXor kr)
        val kMembers: List<BigInteger> = listOf(k.and(value = kr), k.or(value = kr), k.xor(value = kr))
        val expected = listOf("5", "-9", "-14")
        for (values in listOf(jNamed, jAliased, jInfix, jAliasedInfix))
            assertEquals(expected, values.map { it.toString() })
        for (values in listOf(kNamed, kAliased, kInfix, kAliasedInfix, kMembers))
            assertEquals(expected, values.map { it.toString() })
    }

    private class JCallerInteger : JInteger("7") {
        override fun toByte(): Byte = toInt().toByte()
        override fun toShort(): Short = toInt().toShort()
        operator fun inc(): JCallerInteger = JCallerInteger()
        operator fun dec(): JCallerInteger = JCallerInteger()
    }

    private class KCallerInteger : BigInteger("7") {
        operator fun inc(): KCallerInteger = KCallerInteger()
        operator fun dec(): KCallerInteger = KCallerInteger()
    }

    private class JCallerDecimal : JDecimal("7.00") {
        override fun toByte(): Byte = toInt().toByte()
        override fun toShort(): Short = toInt().toShort()
        operator fun inc(): JCallerDecimal = JCallerDecimal()
        operator fun dec(): JCallerDecimal = JCallerDecimal()
    }

    private class KCallerDecimal : BigDecimal("7.00") {
        operator fun inc(): KCallerDecimal = KCallerDecimal()
        operator fun dec(): KCallerDecimal = KCallerDecimal()
    }

    @Test fun callerOwnedOperatorDeclarationsRemainSourceCompatibleOnBothTypes() {
        var jInteger = JCallerInteger()
        var kInteger = KCallerInteger()
        val jOldInteger: JCallerInteger = jInteger++
        val kOldInteger: KCallerInteger = kInteger++
        val jPreviousInteger: JCallerInteger = --jInteger
        val kPreviousInteger: KCallerInteger = --kInteger
        assertEquals(jOldInteger.toString(), kOldInteger.toString())
        assertEquals(jPreviousInteger.toString(), kPreviousInteger.toString())
        var jDecimal = JCallerDecimal()
        var kDecimal = KCallerDecimal()
        val jOldDecimal: JCallerDecimal = jDecimal++
        val kOldDecimal: KCallerDecimal = kDecimal++
        val jPreviousDecimal: JCallerDecimal = --jDecimal
        val kPreviousDecimal: KCallerDecimal = --kDecimal
        assertEquals(representation(jOldDecimal), representation(kOldDecimal))
        assertEquals(representation(jPreviousDecimal), representation(kPreviousDecimal))
    }

    @Test fun explicitJdkAndKroguExtensionCallsPreserveReplacementAndThrownObject() {
        val jReplacement = JInteger("701")
        val kReplacement = BigInteger("701")
        val marker = Marker()
        var fail = false
        val jCalls = mutableListOf<String>()
        val kCalls = mutableListOf<String>()
        fun jResult(label: String): JInteger {
            jCalls += label
            if (fail) throw marker
            return jReplacement
        }
        fun kResult(label: String): BigInteger {
            kCalls += label
            if (fail) throw marker
            return kReplacement
        }
        val j: JInteger = object : JInteger("9") {
            override fun toByte(): Byte = toInt().toByte()
            override fun toShort(): Short = toInt().toShort()
            override fun add(value: JInteger): JInteger {
                assertEquals(JInteger.ONE, value)
                return jResult("add")
            }
            override fun subtract(value: JInteger): JInteger {
                assertEquals(JInteger.ONE, value)
                return jResult("subtract")
            }
            override fun shiftLeft(n: Int): JInteger = jResult("left:$n")
            override fun shiftRight(n: Int): JInteger = jResult("right:$n")
            override fun not(): JInteger = jResult("not")
        }
        val k: BigInteger = object : BigInteger("9") {
            override fun add(value: BigInteger): BigInteger {
                assertEquals(BigInteger.ONE, value)
                return kResult("add")
            }
            override fun subtract(value: BigInteger): BigInteger {
                assertEquals(BigInteger.ONE, value)
                return kResult("subtract")
            }
            override fun shiftLeft(n: Int): BigInteger = kResult("left:$n")
            override fun shiftRight(n: Int): BigInteger = kResult("right:$n")
            override fun not(): BigInteger = kResult("not")
        }
        val reference: List<() -> JInteger> = listOf(
            { j.jdkInc() }, { j.jdkDec() }, { j jdkShl Int.MIN_VALUE },
            { j jdkShr Int.MAX_VALUE }, { j.jdkInv() },
        )
        val actual: List<() -> BigInteger> = listOf(
            { k.inc() }, { k.dec() }, { k shl Int.MIN_VALUE }, { k shr Int.MAX_VALUE }, { k.inv() },
        )
        for (index in reference.indices) {
            jCalls.clear()
            kCalls.clear()
            assertSame(jReplacement, reference[index]())
            assertSame(kReplacement, actual[index]())
            assertEquals(jCalls, kCalls)
            fail = true
            assertSame(marker, assertFailsWith<Marker> { reference[index]() })
            assertSame(marker, assertFailsWith<Marker> { actual[index]() })
            fail = false
        }
    }
}

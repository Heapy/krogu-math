// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math.migration

import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.div as kroguDiv
import io.heapy.krogu.math.minus as kroguMinus
import io.heapy.krogu.math.plus as kroguPlus
import io.heapy.krogu.math.rem as kroguRem
import io.heapy.krogu.math.times as kroguTimes
import io.heapy.krogu.math.unaryMinus as kroguUnaryMinus
import io.heapy.krogu.math.unaryPlus as kroguUnaryPlus
import java.math.BigDecimal as JDecimal
import java.math.BigInteger as JInteger
import java.math.RoundingMode as JMode
import kotlin.div as jdkDiv
import kotlin.minus as jdkMinus
import kotlin.plus as jdkPlus
import kotlin.rem as jdkRem
import kotlin.times as jdkTimes
import kotlin.unaryMinus as jdkUnaryMinus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class KotlinArithmeticOperator595JvmTest {
    @Test fun publicStdlibAndKroguImportedFormsMatchIndependentLiterals() {
        val j = JInteger("7")
        val jr = JInteger("2")
        val k = BigInteger("7")
        val kr = BigInteger("2")
        val expected = listOf("9", "5", "14", "3", "1", "-7")
        assertEquals(expected, jdkIntegers(j, jr).map { it.toString() })
        assertEquals(expected, kroguIntegers(k, kr).map { it.toString() })
        assertSame(k, k.kroguUnaryPlus()) // Own convenience; no JDK/std-lib unaryPlus claim.

        val jd = JDecimal("7.50")
        val jdr = JDecimal("2.0")
        val kd = BigDecimal("7.50")
        val kdr = BigDecimal("2.0")
        val expectedFields = listOf("950@2", "550@2", "15000@3", "375@2", "150@2", "-750@2")
        assertEquals(expectedFields, jdkDecimals(jd, jdr).map { fields(it) })
        assertEquals(expectedFields, kroguDecimals(kd, kdr).map { fields(it) })
        assertSame(kd, kd.kroguUnaryPlus())
    }

    @Test fun pairedJdkAndKroguCallerDeclarationsKeepMemberAndBaseSelection() {
        val j = JCallerInteger595()
        val k = CallerInteger595()
        val jr = JInteger("2")
        val kr = BigInteger("2")
        val labels = listOf("plus", "minus", "times", "div", "rem", "unaryMinus", "unaryPlus")
        val expected = listOf("9", "5", "14", "3", "1", "-7", "7")
        val jMembers = listOf<() -> JInteger>({ j + jr }, { j - jr }, { j * jr },
            { j / jr }, { j % jr }, { -j }, { +j })
        val kMembers = listOf<() -> BigInteger>({ k + kr }, { k - kr }, { k * kr },
            { k / kr }, { k % kr }, { -k }, { +k })
        for (i in labels.indices) {
            j.lastOperator = null; k.lastOperator = null
            assertEquals(expected[i], jMembers[i]().toString())
            assertEquals(expected[i], kMembers[i]().toString())
            assertEquals(labels[i], j.lastOperator)
            assertEquals(labels[i], k.lastOperator)
        }
        j.lastOperator = null; k.lastOperator = null
        val jb: JInteger = j
        val kb: BigInteger = k
        assertEquals(expected.take(6), jdkIntegers(jb, jr).map { it.toString() })
        assertEquals(expected.take(6), kroguIntegers(kb, kr).map { it.toString() })
        assertNull(j.lastOperator); assertNull(k.lastOperator)
        assertSame(k, kb.kroguUnaryPlus())

        val jd = JCallerDecimal595()
        val kd = CallerDecimal595()
        val jdr = JDecimal("2.0")
        val kdr = BigDecimal("2.0")
        val decimalExpected = listOf("950@2", "550@2", "15000@3", "375@2", "150@2", "-750@2", "750@2")
        val jdMembers = listOf<() -> JDecimal>({ jd + jdr }, { jd - jdr }, { jd * jdr },
            { jd / jdr }, { jd % jdr }, { -jd }, { +jd })
        val kdMembers = listOf<() -> BigDecimal>({ kd + kdr }, { kd - kdr }, { kd * kdr },
            { kd / kdr }, { kd % kdr }, { -kd }, { +kd })
        for (i in labels.indices) {
            jd.lastOperator = null; kd.lastOperator = null
            assertEquals(decimalExpected[i], fields(jdMembers[i]()))
            assertEquals(decimalExpected[i], fields(kdMembers[i]()))
            assertEquals(labels[i], jd.lastOperator)
            assertEquals(labels[i], kd.lastOperator)
        }
        jd.lastOperator = null; kd.lastOperator = null
        val jdb: JDecimal = jd
        val kdb: BigDecimal = kd
        assertEquals(decimalExpected.take(6), jdkDecimals(jdb, jdr).map { fields(it) })
        assertEquals(decimalExpected.take(6), kroguDecimals(kdb, kdr).map { fields(it) })
        assertNull(jd.lastOperator); assertNull(kd.lastOperator)
        assertSame(kd, kdb.kroguUnaryPlus())
    }

    private fun jdkIntegers(left: JInteger, right: JInteger): List<JInteger> = listOf(
        left.jdkPlus(other = right), left.jdkMinus(other = right), left.jdkTimes(other = right),
        left.jdkDiv(other = right), left.jdkRem(other = right), left.jdkUnaryMinus())
    private fun kroguIntegers(left: BigInteger, right: BigInteger): List<BigInteger> = listOf(
        left.kroguPlus(other = right), left.kroguMinus(other = right), left.kroguTimes(other = right),
        left.kroguDiv(other = right), left.kroguRem(other = right), left.kroguUnaryMinus())
    private fun jdkDecimals(left: JDecimal, right: JDecimal): List<JDecimal> = listOf(
        left.jdkPlus(other = right), left.jdkMinus(other = right), left.jdkTimes(other = right),
        left.jdkDiv(other = right), left.jdkRem(other = right), left.jdkUnaryMinus())
    private fun kroguDecimals(left: BigDecimal, right: BigDecimal): List<BigDecimal> = listOf(
        left.kroguPlus(other = right), left.kroguMinus(other = right), left.kroguTimes(other = right),
        left.kroguDiv(other = right), left.kroguRem(other = right), left.kroguUnaryMinus())
    private fun fields(value: JDecimal): String = "${value.unscaledValue()}@${value.scale()}"
    private fun fields(value: BigDecimal): String = "${value.unscaledValue()}@${value.scale()}"
}

// Positive JDK baseline source witnesses. No operator has override; only Number narrowing does.
private class JCallerInteger595 : JInteger("7") {
    override fun toByte(): Byte = toInt().toByte()
    override fun toShort(): Short = toInt().toShort()
    var lastOperator: String? = null
    private fun mark(name: String, result: JInteger): JInteger { lastOperator = name; return result }
    operator fun plus(other: JInteger): JInteger = mark("plus", super.add(other))
    operator fun minus(other: JInteger): JInteger = mark("minus", super.subtract(other))
    operator fun times(other: JInteger): JInteger = mark("times", super.multiply(other))
    operator fun div(other: JInteger): JInteger = mark("div", super.divide(other))
    operator fun rem(other: JInteger): JInteger = mark("rem", super.remainder(other))
    operator fun unaryMinus(): JInteger = mark("unaryMinus", super.negate())
    operator fun unaryPlus(): JInteger = mark("unaryPlus", this)
}

private class JCallerDecimal595 : JDecimal("7.50") {
    override fun toByte(): Byte = toInt().toByte()
    override fun toShort(): Short = toInt().toShort()
    var lastOperator: String? = null
    private fun mark(name: String, result: JDecimal): JDecimal { lastOperator = name; return result }
    operator fun plus(other: JDecimal): JDecimal = mark("plus", super.add(other))
    operator fun minus(other: JDecimal): JDecimal = mark("minus", super.subtract(other))
    operator fun times(other: JDecimal): JDecimal = mark("times", super.multiply(other))
    operator fun div(other: JDecimal): JDecimal = mark("div", super.divide(other, JMode.HALF_EVEN))
    operator fun rem(other: JDecimal): JDecimal = mark("rem", super.remainder(other))
    operator fun unaryMinus(): JDecimal = mark("unaryMinus", super.negate())
    operator fun unaryPlus(): JDecimal = mark("unaryPlus", this)
}

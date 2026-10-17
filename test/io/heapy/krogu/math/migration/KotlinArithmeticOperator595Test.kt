// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math.migration

import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.MathContext
import io.heapy.krogu.math.RoundingMode
import io.heapy.krogu.math.div
import io.heapy.krogu.math.minus
import io.heapy.krogu.math.plus
import io.heapy.krogu.math.rem
import io.heapy.krogu.math.times
import io.heapy.krogu.math.unaryMinus
import io.heapy.krogu.math.unaryPlus
import io.heapy.krogu.math.div as arithmeticDiv
import io.heapy.krogu.math.minus as arithmeticMinus
import io.heapy.krogu.math.plus as arithmeticPlus
import io.heapy.krogu.math.rem as arithmeticRem
import io.heapy.krogu.math.times as arithmeticTimes
import io.heapy.krogu.math.unaryMinus as arithmeticUnaryMinus
import io.heapy.krogu.math.unaryPlus as arithmeticUnaryPlus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class KotlinArithmeticOperator595Test {
    @Test fun importedAndAliasedArithmeticRetainsIndependentValuesAndDecimalCohorts() {
        val integer = BigInteger("7")
        val integerAlias = integer
        val ir = BigInteger("2")
        val integerExpected = listOf("9", "5", "14", "3", "1", "-7", "7")
        assertEquals(integerExpected, integerOperators(integer, ir).map { it().toString() })
        assertEquals(integerExpected, listOf(integer.plus(other = ir), integer.minus(other = ir),
            integer.times(other = ir), integer.div(other = ir), integer.rem(other = ir),
            integer.unaryMinus(), integer.unaryPlus()).map { it.toString() })
        assertEquals(integerExpected, listOf(integer.arithmeticPlus(other = ir),
            integer.arithmeticMinus(other = ir), integer.arithmeticTimes(other = ir),
            integer.arithmeticDiv(other = ir), integer.arithmeticRem(other = ir),
            integer.arithmeticUnaryMinus(), integer.arithmeticUnaryPlus()).map { it.toString() })
        assertSame(integer, +integer)
        assertSame(integer, integer.arithmeticUnaryPlus())
        assertEquals("7", integerAlias.toString())

        val decimal = BigDecimal("7.50")
        val decimalAlias = decimal
        val dr = BigDecimal("2.0")
        val decimalExpected = listOf("950@2", "550@2", "15000@3", "375@2", "150@2", "-750@2", "750@2")
        assertEquals(decimalExpected, decimalOperators(decimal, dr).map { fields(it()) })
        assertEquals(decimalExpected, listOf(decimal.plus(other = dr), decimal.minus(other = dr),
            decimal.times(other = dr), decimal.div(other = dr), decimal.rem(other = dr),
            decimal.unaryMinus(), decimal.unaryPlus()).map { fields(it) })
        assertEquals(decimalExpected, listOf(decimal.arithmeticPlus(other = dr),
            decimal.arithmeticMinus(other = dr), decimal.arithmeticTimes(other = dr),
            decimal.arithmeticDiv(other = dr), decimal.arithmeticRem(other = dr),
            decimal.arithmeticUnaryMinus(), decimal.arithmeticUnaryPlus()).map { fields(it) })
        assertSame(decimal, +decimal)
        assertSame(decimal, decimal.arithmeticUnaryPlus())
        assertEquals("750@2", fields(decimalAlias))
        assertEquals("750@2", fields(decimal.plus())) // Genuine JDK-shaped member remains available.
        assertEquals("75@1", fields(decimal.plus(MathContext(2, RoundingMode.HALF_EVEN))))
        for ((text, expected) in listOf("1.25" to "62@2", "1.35" to "68@2", "-1.25" to "-62@2"))
            assertEquals(expected, fields(BigDecimal(text) / BigDecimal("2.000")))
        assertEquals("0@0", fields(BigDecimal.ONE / BigDecimal("3")))
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divide(BigDecimal("3")) }
        assertFailsWith<ArithmeticException> { integer / BigInteger.ZERO }
        assertFailsWith<ArithmeticException> { integer % BigInteger.ZERO }
        assertFailsWith<ArithmeticException> { decimal / BigDecimal.ZERO }
        assertFailsWith<ArithmeticException> { decimal % BigDecimal.ZERO }
    }

    @Test fun callerOwnedSevenMembersAndStaticBaseExtensionsRemainDistinct() {
        val integer = CallerInteger595()
        val ir = BigInteger("2")
        val labels = listOf("plus", "minus", "times", "div", "rem", "unaryMinus", "unaryPlus")
        val integerExpected = listOf("9", "5", "14", "3", "1", "-7", "7")
        val integerMembers = listOf<() -> BigInteger>({ integer + ir }, { integer - ir },
            { integer * ir }, { integer / ir }, { integer % ir }, { -integer }, { +integer })
        for (i in labels.indices) {
            integer.lastOperator = null
            assertEquals(integerExpected[i], integerMembers[i]().toString())
            assertEquals(labels[i], integer.lastOperator)
        }
        val baseInteger: BigInteger = integer
        for ((i, operation) in integerOperators(baseInteger, ir).withIndex()) {
            integer.lastOperator = null
            assertEquals(integerExpected[i], operation().toString())
            assertNull(integer.lastOperator)
        }
        integer.lastOperator = null
        assertEquals("9", integer.arithmeticPlus(other = ir).toString())
        assertNull(integer.lastOperator) // Differently named imported extension bypasses caller plus.
        assertSame(integer, +baseInteger)

        val decimal = CallerDecimal595()
        val dr = BigDecimal("2.0")
        val decimalExpected = listOf("950@2", "550@2", "15000@3", "375@2", "150@2", "-750@2", "750@2")
        val decimalMembers = listOf<() -> BigDecimal>({ decimal + dr }, { decimal - dr },
            { decimal * dr }, { decimal / dr }, { decimal % dr }, { -decimal }, { +decimal })
        for (i in labels.indices) {
            decimal.lastOperator = null
            assertEquals(decimalExpected[i], fields(decimalMembers[i]()))
            assertEquals(labels[i], decimal.lastOperator)
        }
        val baseDecimal: BigDecimal = decimal
        for ((i, operation) in decimalOperators(baseDecimal, dr).withIndex()) {
            decimal.lastOperator = null
            assertEquals(decimalExpected[i], fields(operation()))
            assertNull(decimal.lastOperator)
        }
        decimal.lastOperator = null
        assertEquals("950@2", fields(decimal.arithmeticPlus(other = dr)))
        assertNull(decimal.lastOperator)
        assertSame(decimal, +baseDecimal)
    }

    @Test fun extensionsPreserveReachedVirtualResultOperandAndExceptionIdentity() {
        val ir = BigInteger("2")
        val integerReplies = listOf("9", "5", "14", "3", "1", "-7").map { BigInteger(it) }
        val integer = ForwardingInteger595(integerReplies)
        val integerCalls = integerOperators(integer, ir).take(6)
        for (i in integerCalls.indices) {
            assertSame(integerReplies[i], integerCalls[i]())
            assertEquals(i, integer.leaf)
            assertSame(if (i == 5) null else ir, integer.operand)
            val marker = Marker595()
            integer.failure = marker
            assertSame(marker, assertFailsWith<Marker595> { integerCalls[i]() })
            integer.failure = null
        }
        assertSame(integer, +integer)

        val dr = BigDecimal("2.0")
        val decimalReplies = listOf("9.50", "5.50", "15.000", "3.75", "1.50", "-7.50").map { BigDecimal(it) }
        val decimal = ForwardingDecimal595(decimalReplies)
        val decimalCalls = decimalOperators(decimal, dr).take(6)
        for (i in decimalCalls.indices) {
            assertSame(decimalReplies[i], decimalCalls[i]())
            assertEquals(i, decimal.leaf)
            assertSame(if (i == 5) null else dr, decimal.operand)
            if (i == 3) assertEquals(RoundingMode.HALF_EVEN, decimal.mode)
            val marker = Marker595()
            decimal.failure = marker
            assertSame(marker, assertFailsWith<Marker595> { decimalCalls[i]() })
            decimal.failure = null
        }
        assertSame(decimal, +decimal)
    }

    private fun integerOperators(left: BigInteger, right: BigInteger): List<() -> BigInteger> =
        listOf({ left + right }, { left - right }, { left * right }, { left / right },
            { left % right }, { -left }, { +left })
    private fun decimalOperators(left: BigDecimal, right: BigDecimal): List<() -> BigDecimal> =
        listOf({ left + right }, { left - right }, { left * right }, { left / right },
            { left % right }, { -left }, { +left })
    private fun fields(value: BigDecimal): String = "${value.unscaledValue()}@${value.scale()}"
}

// These are positive source-migration witnesses: all seven declarations intentionally lack override.
internal class CallerInteger595 : BigInteger("7") {
    var lastOperator: String? = null
    private fun mark(name: String, result: BigInteger): BigInteger { lastOperator = name; return result }
    operator fun plus(other: BigInteger): BigInteger = mark("plus", super.add(other))
    operator fun minus(other: BigInteger): BigInteger = mark("minus", super.subtract(other))
    operator fun times(other: BigInteger): BigInteger = mark("times", super.multiply(other))
    operator fun div(other: BigInteger): BigInteger = mark("div", super.divide(other))
    operator fun rem(other: BigInteger): BigInteger = mark("rem", super.remainder(other))
    operator fun unaryMinus(): BigInteger = mark("unaryMinus", super.negate())
    operator fun unaryPlus(): BigInteger = mark("unaryPlus", this)
}

internal class CallerDecimal595 : BigDecimal("7.50") {
    var lastOperator: String? = null
    private fun mark(name: String, result: BigDecimal): BigDecimal { lastOperator = name; return result }
    operator fun plus(other: BigDecimal): BigDecimal = mark("plus", super.add(other))
    operator fun minus(other: BigDecimal): BigDecimal = mark("minus", super.subtract(other))
    operator fun times(other: BigDecimal): BigDecimal = mark("times", super.multiply(other))
    operator fun div(other: BigDecimal): BigDecimal = mark("div", super.divide(other, RoundingMode.HALF_EVEN))
    operator fun rem(other: BigDecimal): BigDecimal = mark("rem", super.remainder(other))
    operator fun unaryMinus(): BigDecimal = mark("unaryMinus", super.negate())
    operator fun unaryPlus(): BigDecimal = mark("unaryPlus", this)
}

private class Marker595 : RuntimeException()

private class ForwardingInteger595(private val replies: List<BigInteger>) : BigInteger("7") {
    private val right = BigInteger("2")
    var failure: Marker595? = null
    var leaf: Int = -1
    var operand: BigInteger? = null
    private fun reached(index: Int, value: BigInteger?): BigInteger {
        leaf = index; operand = value
        failure?.let { throw it }
        return replies[index]
    }
    override fun add(value: BigInteger): BigInteger = if (value == right) reached(0, value) else super.add(value)
    override fun subtract(value: BigInteger): BigInteger = if (value == right) reached(1, value) else super.subtract(value)
    override fun multiply(value: BigInteger): BigInteger = if (value == right) reached(2, value) else super.multiply(value)
    override fun divide(value: BigInteger): BigInteger = if (value == right) reached(3, value) else super.divide(value)
    override fun remainder(value: BigInteger): BigInteger = if (value == right) reached(4, value) else super.remainder(value)
    override fun negate(): BigInteger = reached(5, null)
}

private class ForwardingDecimal595(private val replies: List<BigDecimal>) : BigDecimal("7.50") {
    private val right = BigDecimal("2.0")
    var failure: Marker595? = null
    var leaf: Int = -1
    var operand: BigDecimal? = null
    var mode: RoundingMode? = null
    private fun reached(index: Int, value: BigDecimal?): BigDecimal {
        leaf = index; operand = value
        failure?.let { throw it }
        return replies[index]
    }
    override fun add(augend: BigDecimal): BigDecimal = if (augend == right) reached(0, augend) else super.add(augend)
    override fun subtract(subtrahend: BigDecimal): BigDecimal =
        if (subtrahend == right) reached(1, subtrahend) else super.subtract(subtrahend)
    override fun multiply(multiplicand: BigDecimal): BigDecimal =
        if (multiplicand == right) reached(2, multiplicand) else super.multiply(multiplicand)
    override fun divide(divisor: BigDecimal, roundingMode: RoundingMode): BigDecimal {
        if (divisor != right || roundingMode != RoundingMode.HALF_EVEN) return super.divide(divisor, roundingMode)
        mode = roundingMode
        return reached(3, divisor)
    }
    override fun remainder(divisor: BigDecimal): BigDecimal =
        if (divisor == right) reached(4, divisor) else super.remainder(divisor)
    override fun negate(): BigDecimal = reached(5, null)
}

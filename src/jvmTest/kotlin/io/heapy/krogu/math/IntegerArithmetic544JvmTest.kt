// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.math.BigInteger as JI
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/** UNCOMPILED/UNRUN. Run only under the parent's numerical mutex and pinned public JDK. */
class IntegerArithmetic544JvmTest {
    private class Own(text: String, val events: MutableList<String>) : BigInteger(text) {
        private val ordinary = BigInteger(text)
        override fun abs(): BigInteger { events += "abs"; return Own(ordinary.abs().toString(), events) }
        override fun shiftRight(n: Int): BigInteger { events += "right:$n"; return Own(ordinary.shiftRight(n).toString(), events) }
        override fun bitLength(): Int { events += "bits"; return super.bitLength() }
        override fun getLowestSetBit(): Int { events += "lowest"; return super.getLowestSetBit() }
        override fun add(value: BigInteger): BigInteger {
            events += "add:enter"
            val result = BigInteger(super.add(value).toByteArray()).subtract(BigInteger.ZERO)
            events += "add:exit"
            return result
        }
        override fun multiply(value: BigInteger): BigInteger {
            events += "multiply:enter"
            val result = BigInteger(super.multiply(value).toByteArray()).add(BigInteger.ZERO)
            events += "multiply:exit"
            return result
        }
        fun inheritedAdd(value: BigInteger): BigInteger = super.add(value)
        fun inheritedMultiply(value: BigInteger): BigInteger = super.multiply(value)
        fun inheritedPow(exponent: Int): BigInteger = super.pow(exponent)
    }

    private class Host(text: String, val events: MutableList<String>) : JI(text) {
        private val ordinary = JI(text)
        override fun toByte(): Byte = super.toInt().toByte()
        override fun toShort(): Short = super.toInt().toShort()
        override fun abs(): JI { events += "abs"; return Host(ordinary.abs().toString(), events) }
        override fun shiftRight(n: Int): JI { events += "right:$n"; return Host(ordinary.shiftRight(n).toString(), events) }
        override fun bitLength(): Int { events += "bits"; return super.bitLength() }
        override fun getLowestSetBit(): Int { events += "lowest"; return super.getLowestSetBit() }
        override fun add(value: JI): JI {
            events += "add:enter"
            val result = JI(super.add(value).toByteArray()).subtract(JI.ZERO)
            events += "add:exit"
            return result
        }
        override fun multiply(value: JI): JI {
            events += "multiply:enter"
            val result = JI(super.multiply(value).toByteArray()).add(JI.ZERO)
            events += "multiply:exit"
            return result
        }
        fun inheritedAdd(value: JI): JI = super.add(value)
        fun inheritedMultiply(value: JI): JI = super.multiply(value)
        fun inheritedPow(exponent: Int): JI = super.pow(exponent)
    }

    @Test fun composedDirectAndSuperCallsUseLiteralValuesAndReportInternalTraces() {
        val ownEvents = mutableListOf<String>()
        val hostEvents = mutableListOf<String>()
        val own = Own("4294967295", ownEvents)
        val host = Host("4294967295", hostEvents)
        val ownBase: BigInteger = own
        val hostBase: JI = host
        for (multiply in listOf(false, true)) for (viaSuper in listOf(false, true)) {
            ownEvents.clear()
            hostEvents.clear()
            val actual = if (multiply) {
                if (viaSuper) own.inheritedMultiply(own) else ownBase.multiply(ownBase)
            } else if (viaSuper) own.inheritedAdd(own) else ownBase.add(ownBase)
            val reference = if (multiply) {
                if (viaSuper) host.inheritedMultiply(host) else hostBase.multiply(hostBase)
            } else if (viaSuper) host.inheritedAdd(host) else hostBase.add(hostBase)
            val expected = if (multiply) "18446744065119617025" else "8589934590"
            assertEquals(expected, actual.toString())
            assertEquals(expected, reference.toString())
            if (!viaSuper) {
                val name = if (multiply) "multiply" else "add"
                for (events in listOf(ownEvents, hostEvents)) {
                    assertEquals("$name:enter", events.first())
                    assertEquals("$name:exit", events.last())
                }
            }
            println("arithmetic544 multiply=$multiply super=$viaSuper " +
                "ownTrace=$ownEvents hostTrace=$hostEvents " +
                "ownReceiver=${actual === own} hostReceiver=${reference === host}")
        }
        assertEquals(0, ownBase.compareTo(own))
        assertEquals(0, hostBase.compareTo(host))
    }

    @Test fun truthfulReturnedWrapperPowersCompareValuesAndOnlyReportCallbackObservations() {
        val cases = listOf(Triple("3", 31, "617673396283947"),
            Triple("3", 32, "1853020188851841"), Triple("3", 33, "5559060566555523"),
            Triple("-12", 3, "-1728"), Triple("4294967295", 2, "18446744065119617025"),
            Triple("0", 0, "1"), Triple("-1", Int.MAX_VALUE, "-1"))
        for ((text, exponent, expected) in cases) {
            val ownEvents = mutableListOf<String>()
            val hostEvents = mutableListOf<String>()
            val own = Own(text, ownEvents)
            val host = Host(text, hostEvents)
            val actual = own.inheritedPow(exponent)
            val reference = host.inheritedPow(exponent)
            assertEquals(expected, actual.toString())
            assertEquals(expected, reference.toString())
            println("arithmetic544 pow=$text^$exponent ownTrace=$ownEvents hostTrace=$hostEvents " +
                "ownCalls=${ownEvents.size} hostCalls=${hostEvents.size} " +
                "ownReceiver=${actual === own} hostReceiver=${reference === host} " +
                "ownOne=${actual === BigInteger.ONE} hostOne=${reference === JI.ONE}")
        }
    }

    @Test fun boundedSignedArithmeticPipelineMatchesPublicJdkBytes() {
        val values = listOf("-1152921504606846977", "-1152921504606846975",
            "1152921504606846975", "1152921504606846977")
        for (left in values) for (right in values) {
            val a = Own(left, mutableListOf())
            val b = Own(right, mutableListOf())
            val ja = Host(left, mutableListOf())
            val jb = Host(right, mutableListOf())
            val actual = a.multiply(b).add(a).subtract(b)
            val reference = ja.multiply(jb).add(ja).subtract(jb)
            assertContentEquals(reference.toByteArray(), actual.toByteArray())
            val pair = actual.divideAndRemainder(b)
            val jPair = reference.divideAndRemainder(jb)
            assertContentEquals(jPair[0].toByteArray(), pair[0].toByteArray())
            assertContentEquals(jPair[1].toByteArray(), pair[1].toByteArray())
            assertContentEquals(reference.gcd(jb).toByteArray(), actual.gcd(b).toByteArray())
            assertEquals(ja.compareTo(jb).coerceIn(-1, 1), a.compareTo(b).coerceIn(-1, 1))
            val rootPair = actual.abs().sqrtAndRemainder()
            val jRootPair = reference.abs().sqrtAndRemainder()
            assertContentEquals(jRootPair[0].toByteArray(), rootPair[0].toByteArray())
            assertContentEquals(jRootPair[1].toByteArray(), rootPair[1].toByteArray())
        }
    }

    private fun outcome(marker: Throwable, action: () -> Any): String = try {
        "value:${action()}"
    } catch (failure: Exception) {
        "${failure.javaClass.name}:${failure.message}:marker=${failure === marker}"
    }

    @Test fun directMarkerIdentityIsNormativeWhileInheritedErrorStagesAreDiagnostic() {
        val marker = IllegalStateException("explicit arithmetic marker")
        val ownEvents = mutableListOf<String>()
        val hostEvents = mutableListOf<String>()
        val own: BigInteger = object : BigInteger("-12") {
            override fun abs(): BigInteger { ownEvents += "abs:throw"; throw marker }
            override fun multiply(value: BigInteger): BigInteger { ownEvents += "multiply:throw"; throw marker }
        }
        val host: JI = object : JI("-12") {
            override fun toByte(): Byte = super.toInt().toByte()
            override fun toShort(): Short = super.toInt().toShort()
            override fun abs(): JI { hostEvents += "abs:throw"; throw marker }
            override fun multiply(value: JI): JI { hostEvents += "multiply:throw"; throw marker }
        }
        assertSame(marker, assertFailsWith<IllegalStateException> { own.abs() })
        assertSame(marker, assertFailsWith<IllegalStateException> { host.abs() })
        assertSame(marker, assertFailsWith<IllegalStateException> { own.multiply(BigInteger.ZERO) })
        assertSame(marker, assertFailsWith<IllegalStateException> { host.multiply(JI.ZERO) })
        val ownCalls: List<() -> Any> = listOf({ own.pow(-1) }, { own.pow(0) },
            { own.pow(1) }, { own.pow(2) }, { own.gcd(BigInteger.ZERO) },
            { own.sqrt() }, { own.divide(BigInteger.ZERO) }, { own.parallelMultiply(BigInteger.ONE) })
        val hostCalls: List<() -> Any> = listOf({ host.pow(-1) }, { host.pow(0) },
            { host.pow(1) }, { host.pow(2) }, { host.gcd(JI.ZERO) },
            { host.sqrt() }, { host.divide(JI.ZERO) }, { host.parallelMultiply(JI.ONE) })
        for (index in ownCalls.indices) {
            ownEvents.clear()
            hostEvents.clear()
            val actual = outcome(marker, ownCalls[index])
            val reference = outcome(marker, hostCalls[index])
            println("arithmetic544 inherited=$index own=$actual host=$reference " +
                "ownTrace=$ownEvents hostTrace=$hostEvents")
            // Throwing overrides are not truthful numerical operations; do not assert host parity.
        }
    }
}

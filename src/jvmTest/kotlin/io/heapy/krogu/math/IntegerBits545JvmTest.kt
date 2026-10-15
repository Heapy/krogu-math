// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.math.BigInteger as JI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/** UNCOMPILED/UNRUN. Internal host traces and identities are observations, not assertions. */
class IntegerBits545JvmTest {
    @Test fun safeZeroShiftAndNegativeIndexEndpointsMatchRequiredValuesAndHost() {
        assertEquals("0", BigInteger.ZERO.shiftRight(Int.MIN_VALUE).toString())
        assertEquals("0", JI.ZERO.shiftRight(Int.MIN_VALUE).toString())
        assertEquals("0", BigInteger.ZERO.shiftLeft(Int.MAX_VALUE).toString())
        assertEquals("0", JI.ZERO.shiftLeft(Int.MAX_VALUE).toString())
        assertFailsWith<ArithmeticException> { BigInteger.ZERO.setBit(Int.MIN_VALUE) }
        assertFailsWith<ArithmeticException> { JI.ZERO.setBit(Int.MIN_VALUE) }
        assertFailsWith<ArithmeticException> { BigInteger.ZERO.clearBit(Int.MIN_VALUE) }
        assertFailsWith<ArithmeticException> { JI.ZERO.clearBit(Int.MIN_VALUE) }
        assertFailsWith<ArithmeticException> { BigInteger.ZERO.flipBit(Int.MIN_VALUE) }
        assertFailsWith<ArithmeticException> { JI.ZERO.flipBit(Int.MIN_VALUE) }
    }

    private class Own(text: String, val events: MutableList<String>) : BigInteger(text) {
        override fun bitLength(): Int { events += "bits"; return super.bitLength() }
        override fun getLowestSetBit(): Int { events += "lowest"; return super.getLowestSetBit() }
        override fun xor(value: BigInteger?): BigInteger {
            events += "direct-enter"
            val result = super.xor(value).not().not()
            events += "direct-exit"
            return result
        }
    }

    private class Host(text: String, val events: MutableList<String>) : JI(text) {
        override fun toByte(): Byte = super.toInt().toByte()
        override fun toShort(): Short = super.toInt().toShort()
        override fun bitLength(): Int { events += "bits"; return super.bitLength() }
        override fun getLowestSetBit(): Int { events += "lowest"; return super.getLowestSetBit() }
        override fun xor(value: JI?): JI {
            events += "direct-enter"
            val result = super.xor(value).not().not()
            events += "direct-exit"
            return result
        }
    }

    @Test fun composedTruthfulDirectCallsAssertValuesAndReportInternalObservations() {
        val cases = listOf(
            "-1073741824" to "1073741823",
            "-4294967296" to "4294967295",
            "-18446744073709551616" to "18446744073709551615",
        )
        for ((left, right) in cases) for (self in listOf(false, true)) {
            val ownEvents = mutableListOf<String>()
            val hostEvents = mutableListOf<String>()
            val own: BigInteger = Own(left, ownEvents)
            val host: JI = Host(left, hostEvents)
            val ownRight: BigInteger = if (self) own else Own(right, ownEvents)
            val hostRight: JI = if (self) host else Host(right, hostEvents)
            val actual = own.xor(ownRight)
            val reference = host.xor(hostRight)
            val expected = if (self) "0" else "-1"
            assertEquals(expected, actual.toString())
            assertEquals(expected, reference.toString())
            // These markers are in the explicitly invoked override itself.
            for (events in listOf(ownEvents, hostEvents)) {
                assertEquals("direct-enter", events.first())
                assertEquals("direct-exit", events.last())
            }
            println("bits545 left=$left self=$self ownTrace=$ownEvents hostTrace=$hostEvents " +
                "ownCalls=${ownEvents.size} hostCalls=${hostEvents.size} " +
                "ownReceiver=${actual === own} hostReceiver=${reference === host} " +
                "ownOperand=${actual === ownRight} hostOperand=${reference === hostRight} " +
                "ownZero=${actual === BigInteger.ZERO} hostZero=${reference === JI.ZERO}")
        }
    }

    @Test fun truthfulNegativeMetadataAssertsValuesButOnlyReportsCallbackCounts() {
        for ((text, bits) in listOf("-1073741824" to 30, "-4294967296" to 32,
            "-18446744073709551616" to 64)) {
            val ownEvents = mutableListOf<String>()
            val hostEvents = mutableListOf<String>()
            val own = Own(text, ownEvents)
            val host = Host(text, hostEvents)
            assertEquals(bits, own.bitCount())
            assertEquals(bits, host.bitCount())
            println("bits545 count=$text ownTrace=$ownEvents hostTrace=$hostEvents")
            assertEquals(bits, own.bitLength())
            assertEquals(bits, host.bitLength())
            assertEquals(bits, own.getLowestSetBit())
            assertEquals(bits, host.getLowestSetBit())
        }
    }

    private fun failure(action: () -> Any): String = try {
        "returned:${action()}"
    } catch (failure: Exception) {
        "${failure.javaClass.name}:${failure.message}"
    }

    @Test fun composedMetadataFailurePrecedenceIsDiagnosticOnly() {
        val ownEvents = mutableListOf<String>()
        val hostEvents = mutableListOf<String>()
        val marker = IllegalStateException("metadata composition")
        val own: BigInteger = object : BigInteger("-4294967296") {
            override fun bitLength(): Int {
                ownEvents += "bits-enter"
                // Deliberately compose with a second virtual metadata method.
                getLowestSetBit()
                return super.bitLength()
            }
            override fun getLowestSetBit(): Int {
                ownEvents += "lowest-throw"
                throw marker
            }
        }
        val host: JI = object : JI("-4294967296") {
            override fun toByte(): Byte = super.toInt().toByte()
            override fun toShort(): Short = super.toInt().toShort()
            override fun bitLength(): Int {
                hostEvents += "bits-enter"
                getLowestSetBit()
                return super.bitLength()
            }
            override fun getLowestSetBit(): Int {
                hostEvents += "lowest-throw"
                throw marker
            }
        }
        // Direct caller dispatch and exception identity are explicit-language obligations.
        assertSame(marker, assertFailsWith<IllegalStateException> { own.bitLength() })
        assertSame(marker, assertFailsWith<IllegalStateException> { host.bitLength() })
        val ownActions: List<() -> Any> = listOf(
            { own.clearBit(-1) }, { own.flipBit(Int.MIN_VALUE) },
            { own.xor(null) }, { own.xor(own) }, { own.not() },
        )
        val hostActions: List<() -> Any> = listOf(
            { host.clearBit(-1) }, { host.flipBit(Int.MIN_VALUE) },
            { host.xor(null) }, { host.xor(host) }, { host.not() },
        )
        for (index in ownActions.indices) {
            ownEvents.clear()
            hostEvents.clear()
            val actual = failure(ownActions[index])
            val observed = failure(hostActions[index])
            println("bits545 competing=$index own=$actual host=$observed " +
                "ownTrace=$ownEvents hostTrace=$hostEvents")
            // Neither a throwing override nor competing failures establish a numerical oracle.
        }
    }
}

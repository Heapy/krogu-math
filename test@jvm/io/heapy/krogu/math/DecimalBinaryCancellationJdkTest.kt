// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalBinaryCancellationJdkTest {
    @Test fun practicalCancellingCoefficientsAndMidpointTailsMatchLiveJdkRawBits() {
        val cases = cancellationCohortCases() + cancellationMidpointCases(cancellationFloat) +
            cancellationMidpointCases(cancellationDouble)
        assertEquals(248, cases.size)
        for (case in cases) {
            val expected = java.math.BigDecimal(java.math.BigInteger(case.coefficient), case.scale)
            val bits = if (case.format.single) expected.toFloat().toRawBits().toLong() and 0xffffffffL
                else expected.toDouble().toRawBits()
            assertEquals(case.expectedBits, bits, "independent encoding expectation: ${case.label}")
            assertEquals(bits, case.format.bits(case.value()), case.label)
        }
    }

    @Test fun truthfulInflatedSubclassValuesAndCallbackObservations() {
        var count = 0
        for (case in cancellationCohortCases().filter { "padding=4096 " in it.label }) {
            val expectedTrace = StringBuilder()
            val actualTrace = StringBuilder()
            val expected = object : java.math.BigDecimal(java.math.BigInteger(case.coefficient), case.scale) {
                override fun toByte(): Byte = toInt().toByte()
                override fun toShort(): Short = toInt().toShort()
                override fun signum(): Int { expectedTrace.append("sign;"); return super.signum() }
                override fun precision(): Int { expectedTrace.append("precision;"); return super.precision() }
                override fun scale(): Int { expectedTrace.append("scale;"); return super.scale() }
                override fun unscaledValue(): java.math.BigInteger {
                    expectedTrace.append("coefficient;"); return super.unscaledValue()
                }
                override fun toString(): String { expectedTrace.append("string;"); return super.toString() }
            }
            val actual = object : BigDecimal(BigInteger(case.coefficient), case.scale) {
                override fun signum(): Int { actualTrace.append("sign;"); return super.signum() }
                override fun precision(): Int { actualTrace.append("precision;"); return super.precision() }
                override fun scale(): Int { actualTrace.append("scale;"); return super.scale() }
                override fun unscaledValue(): BigInteger { actualTrace.append("coefficient;"); return super.unscaledValue() }
                override fun toString(): String { actualTrace.append("string;"); return super.toString() }
            }
            val expectedBits = if (case.format.single) expected.toFloat().toRawBits().toLong() and 0xffffffffL
                else expected.toDouble().toRawBits()
            assertEquals(case.expectedBits, expectedBits, "independent encoding: ${case.label}")
            assertEquals(expectedBits, case.format.bits(actual), case.label)
            // Retain both diagnostics. Internal reference callbacks are observations;
            // truthful subclass values and the own stored-value dispatch are the contract.
            assertEquals("coefficient;sign;", expectedTrace.toString(), "JDK observation: ${case.label}")
            assertEquals("sign;", actualTrace.toString(), case.label)
            count++
        }
        assertEquals(16, count)
    }
}

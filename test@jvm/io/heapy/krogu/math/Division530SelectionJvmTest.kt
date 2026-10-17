// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.math.BigInteger as HostInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** SOURCE ONLY: finite public-API comparisons; no foreign implementation source. */
class Division530SelectionJvmTest {
    @Test fun pinnedJdkSquareRootConsumersAcrossLimbBoundaries() {
        val runtime = System.getProperty("java.runtime.version")
        assertTrue(runtime == "25.0.4+9" || runtime.startsWith("25.0.4+9-"), "required runtime 25.0.4+9: $runtime")
        assertTrue(System.getProperty("java.vendor").contains("BellSoft"), "required Liberica vendor")
        for (bits in listOf(1, 29, 30, 31, 59, 60, 61, 127, 256, 1024)) {
            val power = HostInteger.ONE.shiftLeft(bits)
            for (root in listOf(power.subtract(HostInteger.ONE), power, power.add(HostInteger.ONE))) {
                val square = root.multiply(root)
                for (value in listOf(square.subtract(HostInteger.ONE), square,
                    square.add(root.shiftLeft(1)))) {
                    val actualValue = BigInteger(value.toByteArray())
                    val expected = value.sqrtAndRemainder()
                    val actual = actualValue.sqrtAndRemainder()
                    assertEquals(expected[0].toString(), actualValue.sqrt().toString())
                    assertEquals(expected[0].toString(), actual[0].toString())
                    assertEquals(expected[1].toString(), actual[1].toString())
                    assertTrue(value.toByteArray().contentEquals(actualValue.toByteArray()))
                }
            }
        }
    }
}

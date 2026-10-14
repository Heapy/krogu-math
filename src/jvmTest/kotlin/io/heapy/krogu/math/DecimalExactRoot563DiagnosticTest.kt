// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DecimalExactRoot563DiagnosticTest {
    @Test fun boundedJdkDefectIsSeparateFromMathematicalExpectation() {
        for ((c, s) in listOf("100" to Int.MIN_VALUE, "1000" to (Int.MIN_VALUE + 1))) {
            val root = DecimalExactRoot563Expected.root(c, s)!!
            for (p in listOf(0, 1, 2, 7)) for (mode in RoundingMode.entries) {
                if (p != 0 && mode != RoundingMode.UNNECESSARY) continue
                // Actual public-JDK observation, kept separate from the specified result.
                assertFailsWith<ArithmeticException> {
                    java.math.BigDecimal(java.math.BigInteger(c), s)
                        .sqrt(java.math.MathContext(p, java.math.RoundingMode.valueOf(mode.name)))
                }
                val mc = MathContext(p, mode)
                val expected = DecimalExactRoot563Expected.rounded(root.first, root.second, s / 2, mc)
                val actual = BigDecimal(BigInteger(c), s).sqrt(mc)
                assertEquals(expected.coefficient, actual.unscaledValue().toString())
                assertEquals(expected.scale, actual.scale())
            }
        }
    }
}

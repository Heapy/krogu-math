// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

class ExplicitDecimal534JvmTest {
    @Test
    fun overloadReferencesAndExplicitImportsCoexistWithMigrationCalls() {
        val exact: (Double) -> BigDecimal = Decimals::fromBinary
        val roundedExact: (Double, MathContext) -> BigDecimal = Decimals::fromBinary
        val canonical: (Double) -> BigDecimal = Decimals::fromCanonical
        val roundedCanonical: (Double, MathContext) -> BigDecimal = Decimals::fromCanonical
        val parse: (String) -> BigDecimal? = Decimals::parseOrNull
        val parseContext: (String, MathContext) -> BigDecimal? = Decimals::parseOrNull
        val construct: (Double) -> BigDecimal = ::BigDecimal
        val context = MathContext(5, RoundingMode.HALF_EVEN)
        assertEquals(construct(0.1), exact(0.1))
        assertEquals(BigDecimal(0.1, context), roundedExact(0.1, context))
        assertEquals(0.1.toBigDecimal(), canonical(0.1))
        assertEquals(0.1.toBigDecimal(context), roundedCanonical(0.1, context))
        assertEquals("1.25".toBigDecimalOrNull(), parse("1.25"))
        assertEquals("1.25".toBigDecimalOrNull(context), parseContext("1.25", context))
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DecimalSquareRoot579SubtypeJvmTest {
    // Returned quotient and remainder values stay truthful subtypes as well.
    // A positive quotient strictly decreases under division by 10^9; its
    // returned remainder is in [0,10^9), where truthful toInt is exact.
    private class TruthfulInteger(value: BigInteger) : BigInteger(value.toByteArray()) {
        override fun signum(): Int = super.signum()
        override fun toInt(): Int = super.toInt()
        override fun divideAndRemainder(value: BigInteger): Array<BigInteger> {
            val pair = super.divideAndRemainder(value)
            return arrayOf(TruthfulInteger(pair[0]), TruthfulInteger(pair[1]))
        }
    }

    private data class Case(val coefficient: String, val floor: String, val ceiling: String, val aboveHalf: Boolean)

    @Test fun currentOwnFormatTruthfulCoefficientLeavesComposeWithNormalizedRoot() {
        // Independent literals: N=10*C=a*a+r. First two: a=1111111111,
        // r=1111111109/1111111119, below/above a. Third: a=1111111119,
        // r=1999999939, above a and requiring the raw low-cell borrow.
        val cases = listOf(
            Case("123456790209876543", "1111111111", "1111111112", false),
            Case("123456790209876544", "1111111111", "1111111112", true),
            Case("123456792076543210", "1111111119", "1111111120", true),
        )
        for (case in cases) {
            val ordinaryCoefficient = BigInteger(case.coefficient)
            val ordinary = BigDecimal(ordinaryCoefficient, -1)
            val bytes = ByteArrayOutputStream().also { buffer ->
                ObjectOutputStream(buffer).use { it.writeObject(currentFormatCoefficientCarrier529(ordinary)) }
            }.toByteArray()
            val replacement = TruthfulInteger(ordinaryCoefficient)
            val restored = object : ObjectInputStream(ByteArrayInputStream(bytes)) {
                init { enableResolveObject(true) }
                override fun resolveObject(obj: Any?): Any? = if (obj is BigInteger) replacement else obj
            }.use { it.readObject() as BigDecimal }
            val coefficient = restored.unscaledValue()
            // This asserts the bounded current-format test setup's reachability,
            // not a general serialization result-identity or runtime-type API.
            assertTrue(coefficient is TruthfulInteger, "current own-format resolved coefficient")
            assertEquals(ordinaryCoefficient, coefficient, "restored stored value")
            assertEquals(-1, restored.scale(), "restored scale")

            for (mode in RoundingMode.entries) {
                val context = MathContext(10, mode)
                if (mode == RoundingMode.UNNECESSARY) {
                    assertFailsWith<ArithmeticException> {
                        DecimalSquareRoot579.normalized(coefficient, 1L, 10, 0L, 0, mode)
                    }
                    assertFailsWith<ArithmeticException> { ordinary.sqrt(context) }
                    assertFailsWith<ArithmeticException> { restored.sqrt(context) }
                    continue
                }
                val expectedText = when (mode) {
                    RoundingMode.UP, RoundingMode.CEILING -> case.ceiling
                    RoundingMode.DOWN, RoundingMode.FLOOR -> case.floor
                    RoundingMode.HALF_UP, RoundingMode.HALF_DOWN, RoundingMode.HALF_EVEN ->
                        if (case.aboveHalf) case.ceiling else case.floor
                    RoundingMode.UNNECESSARY -> error("Handled above")
                }
                val label = "${case.coefficient} * 10 / $mode"
                // Direct kernel call exercises the restored coefficient and all
                // transitively returned truthful quotient/remainder subtypes.
                val raw = DecimalSquareRoot579.normalized(coefficient, 1L, 10, 0L, 0, mode)
                assertEquals(expectedText, raw.first.toString(), "$label raw coefficient")
                assertEquals(0, raw.second, "$label raw scale")

                // These bounded public inputs take the existing materialized
                // branch (or the earlier UNNECESSARY branch), not the huge raw
                // fallback. Literal value/scale expectations are independent.
                val expected = BigDecimal(BigInteger(expectedText), 0)
                assertEquals(expected, ordinary.sqrt(context), "$label ordinary public control")
                assertEquals(expected, restored.sqrt(context), "$label restored public control")
            }
        }
    }
}

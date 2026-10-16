// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalAccumulator535JvmTest {
    @Test
    fun streamingSumsAndProductsMatchExactIndependentPublicJdkCompositions() {
        val random = Random(535)
        fun fields(value: BigDecimal) = "${value.unscaledValue()}@${value.scale()}"
        fun fields(value: java.math.BigDecimal) = "${value.unscaledValue()}@${value.scale()}"
        fun outcome(operation: () -> String) = try { operation() }
            catch (failure: ArithmeticException) { "!${failure::class.simpleName}" }
        repeat(30) {
            val text = List(12) { java.math.BigDecimal.valueOf(random.nextLong(-100000, 100000), random.nextInt(-4, 5)).toString() }
            val other = List(12) { java.math.BigDecimal.valueOf(random.nextLong(-100000, 100000), random.nextInt(-4, 5)).toString() }
            val own = text.map(::BigDecimal)
            val ownOther = other.map(::BigDecimal)
            val host = text.map { java.math.BigDecimal(it) }
            val hostOther = other.map { java.math.BigDecimal(it) }
            for (mode in RoundingMode.entries) for (policy in DecimalRounding.entries) {
                val mc = MathContext(7, mode)
                val hostMc = java.math.MathContext(7, java.math.RoundingMode.valueOf(mode.name))
                // Existing task567 direct-round zero contract is canonical ZERO.
                fun finalRound(value: java.math.BigDecimal): java.math.BigDecimal =
                    if (value.signum() == 0) java.math.BigDecimal.ZERO else value.round(hostMc)
                val expectedSum = outcome {
                    val result = if (policy == DecimalRounding.FINAL_RESULT) finalRound(host.reduce { a, b -> a.add(b) })
                        else host.drop(1).fold(finalRound(host.first())) { a, b -> a.add(b, hostMc) }
                    fields(result)
                }
                assertEquals(expectedSum, outcome { fields(own.sumDecimals(mc, policy)) })
                val expectedDot = outcome {
                    val products = host.zip(hostOther).map { (a, b) ->
                        if (policy == DecimalRounding.FINAL_RESULT) a.multiply(b) else a.multiply(b, hostMc)
                    }
                    fields(if (policy == DecimalRounding.FINAL_RESULT) finalRound(products.reduce { a, b -> a.add(b) })
                        else products.drop(1).fold(products.first()) { a, b -> a.add(b, hostMc) })
                }
                assertEquals(expectedDot, outcome { fields(own.dotProduct(ownOther, mc, policy)) })
            }
        }
    }
}

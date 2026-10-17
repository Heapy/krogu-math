// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.*

class DecimalExactRoot563CallbackContractTest {
    @Test fun completeSelectedBijectionAndTruthfulControls() {
        val seen = mutableMapOf<String, Int>()
        val snapshotIndices = DecimalExactRoot563Callbacks.observations.map { it.index }.toSet()
        var total = 0
        for ((index, v) in oracleVectors().withIndex()) {
            val category = DecimalExactRoot563Callbacks.category(v.toList())
            assertEquals(index in snapshotIndices, category != null, "predicate/snapshot bijection at $index")
            if (category != null) {
                DecimalExactRoot563Callbacks.retain(v, index)
                DecimalExactRoot563Callbacks.verifyControls(v.toList())
                DecimalExactRoot563Callbacks.verifyDirectOverrides(v.toList())
                seen[category] = (seen[category] ?: 0) + 1
            }
            total++
        }
        assertEquals(75638, total)
        assertEquals(DecimalExactRoot563Callbacks.counts, seen)
    }

    @Test fun explicitRootEntryAndExceptionIdentityAcrossContexts() {
        val contexts = listOf(0, 1, 7, Int.MAX_VALUE)
            .flatMap { p -> RoundingMode.entries.map { MathContext(p, it) } }
        val marker = BigDecimal("7.00")
        for (text in listOf("1", "4.00", "0.0004")) for (context in contexts) {
            var entries = 0
            val replacement: BigDecimal = object : BigDecimal(text) {
                override fun sqrt(mc: MathContext): BigDecimal { entries++; assertSame(context, mc); return marker }
            }
            assertSame(marker, replacement.sqrt(context))
            assertEquals(1, entries)
            val failure = IllegalStateException("caller-owned root")
            val throwing: BigDecimal = object : BigDecimal(text) {
                override fun sqrt(mc: MathContext): BigDecimal { assertSame(context, mc); throw failure }
            }
            assertSame(failure, assertFailsWith<IllegalStateException> { throwing.sqrt(context) })
        }
    }
}

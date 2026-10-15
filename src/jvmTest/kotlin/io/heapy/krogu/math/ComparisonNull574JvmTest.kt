// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.*

class ComparisonNull574JvmTest {
    @Test fun everyDisplacedFrozenRowExecutesAtItsRealJavaBoundary() {
        val executed = mutableMapOf<Int, List<String>>()
        val partitions = mutableMapOf<String, Int>()
        for ((ordinal, row) in oracleVectors().withIndex()) {
            if (!ComparisonRows574.isMoved(row)) continue
            val key = row.take(6)
            assertEquals(ComparisonRows574.moved[ordinal], key, "exact frozen source ordinal")
            assertNull(executed.put(ordinal, key), "duplicate JVM execution $ordinal")
            val partition = "${row[0]}/${if (row[1].startsWith("base")) "base" else "inherited"}"
            partitions[partition] = (partitions[partition] ?: 0) + 1
            val actual = ComparisonNull574.vector(row)
            // Global null rejection is required. Historical callback order and VM wording are diagnostics.
            println("574_NULL_COMPARISON\tindex=$ordinal\t${key.joinToString("\t")}\tpartition=$partition" +
                "\tfrozen=${row[6]}\tactual=${actual.diagnostic()}")
            assertIs<NullPointerException>(actual.failure, "$ordinal $key public null contract")
        }
        assertEquals(ComparisonRows574.moved, executed, "all moved keys and ordinals executed exactly once")
        assertEquals(186, executed.size)
        assertEquals(mapOf("IM/base" to 30, "IM/inherited" to 84, "MM/base" to 12, "MM/inherited" to 60), partitions)
    }

    @Test fun commonIdentityNullChecks() {
        // Exact two displaced standalone obligations from ValueIdentityContractTest.
        assertFailsWith<NullPointerException> { ComparisonNull574.compareIntegerNull(BigInteger.ONE) }
        assertFailsWith<NullPointerException> { ComparisonNull574.compareDecimalNull(BigDecimal.ONE) }
    }
}

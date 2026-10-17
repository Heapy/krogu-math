// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.assertEquals

/** Input-only common/JVM partition, independent of observed exceptions. */
internal object Validation578RngRows {
    val keys: Set<List<String>> = buildSet {
        for (bits in -1..3) {
            add(listOf("R", "random", "$bits", "0", "0", "HALF_EVEN"))
            add(listOf("R", "probablePrime", "$bits", "0", "0", "HALF_EVEN"))
            for (certainty in -1..1) add(listOf("R", "prime", "$bits", "$certainty", "0", "HALF_EVEN"))
        }
    }
    fun assertPartition(seen: Set<List<String>>, ordinals: List<Int>) {
        assertEquals(25, keys.size)
        assertEquals(keys, seen)
        assertEquals((75613..75637).toList(), ordinals)
    }
}

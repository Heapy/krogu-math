// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.*

class Validation578RngTest {
    @Test fun exactPortableRngRowsReachJava() {
        val keys = mutableSetOf<List<String>>()
        val ordinals = mutableListOf<Int>()
        var ordinary = 0
        for ((index, row) in oracleVectors().withIndex()) if (row[0] == "R") {
            assertTrue(keys.add(row.take(6)), "duplicate RNG row $index")
            ordinals += index
            val bits = row[2].toInt()
            val validOtherInputs = if (row[1] == "random") bits >= 0 else bits >= 2
            val actual = try {
                val result: Any? = Validation578Boundary.random(row[1], bits, row[3].toInt())
                result.toString()
            } catch (e: Exception) { "!${e::class.simpleName}" }
            println("R578 ordinal=$index key=${row.take(6)} class=${if (validOtherInputs) "ordinary-null" else "compound-invalid-priority"} frozen=${row[6]} actual=$actual")
            if (validOtherInputs) {
                ordinary++
                assertEquals("!NullPointerException", actual, "$index ${row.take(6)}")
            } else assertTrue(actual.startsWith("!"), "invalid input must be rejected: $index")
            if (row[1] == "random" && bits == 0) assertEquals("0", row[6], "retain original host difference")
        }
        Validation578RngRows.assertPartition(keys, ordinals)
        assertEquals(12, ordinary)
        assertEquals(13, keys.size - ordinary)
    }
}

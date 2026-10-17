// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnaryNull576VectorsTest {
    @Test fun everyDisplacedRowExecutesAtJavaBoundary() {
        val observed = linkedMapOf<Int, List<String>>()
        val finite = mutableSetOf<List<String>>()
        var direct = 0
        for ((ordinal, v) in oracleVectors().withIndex()) {
            if (!UnaryNull576Rows.selected(v)) continue
            assertTrue(observed.put(ordinal, v.take(6)) == null)
            val result = UnaryNull576Boundary.vector(v)
            val operation = v[1].substringBefore(':')
            val behavior = v[1].substringAfter(':')
            val directOverride = (v[0] == "K" && operation == "plus") || operation == "nullNegate"
            val required = if (directOverride) {
                direct++
                when (behavior) {
                    "return" -> "42.00"
                    "self" -> v[2]
                    "throw" -> "!IllegalStateException:" + if (v[0] == "K") "plus callback" else "context callback"
                    "super" -> "!NullPointerException"
                    else -> error("Unknown direct behavior $behavior")
                }
            } else "!NullPointerException"
            assertEquals(required, result.outcome, "ordinal=$ordinal ${v.take(6)}")
            if (directOverride) assertTrue(result.trace.startsWith(
                if (v[0] == "K") "plus:true;" else "negateContext:true;"), "explicit Java override entered")
            if (v.take(6) in FiniteZeroPolicy567.nullAbsKeys) assertTrue(finite.add(v.take(6)))
            println("576_UNARY_NULL\tordinal=$ordinal\t${v.take(6).joinToString("\t")}\t" +
                "route=${if (directOverride) "direct-java-override" else "inherited-base"}\t" +
                "frozen=${v[6]}\tactual=${result.outcome}|${result.trace}\traw=${result.diagnostic}")
        }
        assertEquals(112, observed.size)
        assertEquals(44, direct)
        assertEquals(UnaryNull576Rows.expected, observed, "exact original ordinals and six-field keys")
        assertEquals(FiniteZeroPolicy567.nullAbsKeys, finite)
    }
}

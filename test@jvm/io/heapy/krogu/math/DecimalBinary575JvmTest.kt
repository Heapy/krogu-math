// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DecimalBinary575JvmTest {
    private fun retain(family: String, index: Int, row: Array<String>, actual: String, route: String) {
        println("575_NULL_ROW\t$family\tindex=$index\tkey=${row.dropLast(1).joinToString("\t")}\tfrozen=${row.last()}\tactual=$actual\troute=$route")
    }

    @Test fun everyDisplacedNdRowUsesItsOriginalJavaOverrideAndInputs() {
        val indices = mutableSetOf<Int>()
        val keys = mutableSetOf<List<String>>()
        var direct = 0
        for ((index, row) in oracleVectors().withIndex()) {
            if (!DecimalBinary575Rows.nd(row)) continue
            assertTrue(indices.add(index))
            assertTrue(keys.add(row.take(6)))
            val actual = DecimalBinary575Adapter.nd(row)
            val isDirect = row[4] == "-2"
            retain("ND", index, row, actual, if (isDirect) "DIRECT_JAVA_OVERRIDE" else "INHERITED_BASE_NULL")
            if (isDirect) {
                direct++
                // Caller-owned Java overrides execute even for null. "super" reaches the library.
                assertEquals(row[6], actual, "direct override ${row.take(6)}")
            } else assertEquals("!NullPointerException|", actual, "base entry ${row.take(6)}")
        }
        assertEquals(180, indices.size)
        assertEquals(DecimalBinary575Rows.ndOrdinals, indices)
        assertEquals(180, keys.size)
        assertEquals(36, direct)
    }

    @Test fun everyDisplacedContextDispatchRowUsesExplicitJavaSuper() {
        val indices = mutableSetOf<Int>()
        for ((index, row) in jdk25DecimalContextDispatchCases().withIndex()) {
            if (!DecimalBinary575Rows.contextDispatch(row)) continue
            assertTrue(indices.add(index))
            val actual = DecimalBinary575Adapter.contextDispatch(row)
            retain("CONTEXT_DISPATCH", index, row, actual, "EXPLICIT_SUPER_NULL_INTERNAL_TRACE_DIAGNOSTIC")
            assertEquals("!NullPointerException|", actual, row.take(6).toString())
        }
        assertEquals(384, indices.size)
        assertEquals(DecimalBinary575Rows.contextDispatchOrdinals, indices)
    }

    @Test fun everyDisplacedContractMaskUsesOrdinaryOrTruthfulJavaReceiver() {
        val executions = mutableSetOf<Pair<Int, Int>>()
        val indices = mutableSetOf<Int>()
        for ((index, row) in jdk25DecimalContextContractCases().withIndex()) {
            if (!DecimalBinary575Rows.contextContract(row)) continue
            assertTrue(indices.add(index))
            for (mask in if (row[2] == "ordinary") 0..3 else 0..1) {
                assertTrue(executions.add(index to mask))
                val actual = DecimalBinary575Adapter.contextContract(row, mask)
                retain("CONTEXT_CONTRACT/mask=$mask", index, row, actual, "ORDINARY_OR_TRUTHFUL_NULL")
                assertEquals("!NullPointerException", actual, row.take(5).toString())
            }
        }
        assertEquals(96, indices.size)
        assertEquals(DecimalBinary575Rows.contextContractOrdinals, indices)
        assertEquals(240, executions.size)
    }

    @Test fun everyDisplacedArrayRowUsesExplicitSuperAndRetainsMalformedArrayDiagnostic() {
        val indices = mutableSetOf<Int>()
        for ((index, row) in jdk25DecimalRemainderArrayCases().withIndex()) {
            if (!DecimalBinary575Rows.array(row)) continue
            assertTrue(indices.add(index))
            val actual = DecimalBinary575Adapter.array(row)
            retain("ARRAY", index, row, actual, "EXPLICIT_SUPER_NULL_COMPETING_ARRAY_FAILURE_DIAGNOSTIC")
            // VM NPE text is retained, never required; actual target must reject null before a pair callback.
            assertTrue(actual.startsWith("!NullPointerException:"), row.take(5).toString())
            assertEquals("", actual.substringAfter('|'))
        }
        assertEquals(96, indices.size)
        assertEquals(DecimalBinary575Rows.arrayOrdinals, indices)
    }
}

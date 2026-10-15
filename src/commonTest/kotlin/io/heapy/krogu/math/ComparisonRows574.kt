// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Input-only ledger against the frozen Jdk25Vectors source, with zero-based global ordinals.
 * No expected outcome is computed here. The original seventh field remains in the fixture.
 */
internal object ComparisonRows574 {
    val all: Map<Int, List<String>> = buildMap {
        fun callbacks(start: Int, family: String, texts: List<String>, comparisons: List<Int>) {
            var ordinal = start
            for (text in texts) for (comparison in comparisons) for (behavior in listOf("return", "throw"))
                for (operand in listOf("2", "~")) for (operation in listOf("min", "max")) {
                    put(ordinal++, listOf(family, "$operation:$behavior", text, operand, comparison.toString(), "HALF_EVEN"))
                }
        }
        fun bases(start: Int, family: String, texts: List<String>, operations: List<String>) {
            var ordinal = start
            for (text in texts) for (operand in texts + "~") for (operation in operations) {
                put(ordinal++, listOf(family, operation, text, operand, "0", "HALF_EVEN"))
            }
        }
        callbacks(70839, "MM", listOf("0.00", "1", "-1"), listOf(-2, -1, 0, 1, 2))
        callbacks(70959, "IM", listOf("0", "1", "-1"), listOf(Int.MIN_VALUE, -2, -1, 0, 1, 2, Int.MAX_VALUE))
        bases(71127, "IM", listOf("-9223372036854775809", "-9223372036854775808", "-1073741824", "-1", "0",
            "1", "1073741824", "9223372036854775807", "9223372036854775808", "18446744073709551616"),
            listOf("baseMin", "baseMax", "baseCompare"))
        bases(71775, "MM", listOf("0", "0.00", "1", "1.00", "-1", "2"), listOf("baseMin", "baseMax"))
    }
    val moved: Map<Int, List<String>> = all.filterValues { it[3] == "~" }
    val common: Map<Int, List<String>> = all.filterValues { it[3] != "~" }

    fun isComparison(v: Array<String>): Boolean = v[0] == "IM" || v[0] == "MM"
    fun isMoved(v: Array<String>): Boolean = isComparison(v) && v[3] == "~"
}

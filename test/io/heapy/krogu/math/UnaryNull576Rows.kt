// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Input-only partition. Ordinals refer to the unchanged oracleVectors() sequence. */
internal object UnaryNull576Rows {
    fun selected(v: Array<String>): Boolean = v[0] == "K" ||
        (v[0] == "C" && (v[1].startsWith("nullAbs:") || v[1].startsWith("nullNegate:") ||
            (v[1].startsWith("returnedNegate:") && v[4].toInt() < 0)))

    val expected: Map<Int, List<String>> = buildMap {
        var ordinal = 60091
        for (text in listOf("0.00", "-123.45"))
            for (behavior in listOf("return", "self", "throw", "super"))
                for (operation in listOf("round", "plus"))
                    put(ordinal++, listOf("K", "$operation:$behavior", text, "0", "0", "HALF_EVEN"))
        for ((start, behavior) in listOf(60539 to "super", 60571 to "replace", 60603 to "throw"))
            for ((offset, mode) in RoundingMode.entries.withIndex())
                put(start + offset, listOf("C", "returnedNegate:$behavior", "1", "12.34", "-1", mode.name))
        ordinal = 60635
        for (text in listOf("0.00", "1", "-1")) for (sign in -1..1)
            for (behavior in listOf("return", "self", "throw", "super"))
                for (operation in listOf("nullAbs", "nullNegate"))
                    put(ordinal++, listOf("C", "$operation:$behavior", text, "$sign", "0", "HALF_EVEN"))
    }
}

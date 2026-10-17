// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

/** Input-only partitions. Frozen observations never determine where a row executes. */
internal object DecimalBinary575Rows {
    // Published60 fixture ordinals, independently transcribed from the preserved source ledger.
    val ndOrdinals: Set<Int> = buildSet {
        repeat(12) { block ->
            val base = 62336 + 24 * block
            addAll((base + 3)..(base + 11))
            addAll((base + 15)..(base + 17))
            addAll((base + 21)..(base + 23))
        }
    }
    val contextDispatchOrdinals: Set<Int> = buildSet {
        addAll(7424..7431)
        repeat(11) { block -> addAll((7448 + 48 * block)..(7479 + 48 * block)) }
        addAll(7976..7999)
    }
    val contextContractOrdinals: Set<Int> = (608..703).toSet()
    val arrayOrdinals: Set<Int> = buildSet {
        repeat(12) { block ->
            val base = 18 * block
            addAll(base..(base + 2))
            addAll((base + 9)..(base + 12))
            add(base + 15)
        }
    }
    fun nd(row: Array<String>): Boolean =
        row[0] == "ND" && (row[3] == "~" || row[4] == "-1")
    fun contextDispatch(row: Array<String>): Boolean = row[3] == "null" || row[4].toInt() < 0
    fun contextContract(row: Array<String>): Boolean = row[2] == "null" || row[3].toInt() < 0
    fun array(row: Array<String>): Boolean =
        row[3] == "null" || (row[2] == "context" && row[4].toInt() < 0)
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Finite input-selected policy; no production arithmetic supplies expected values. */
internal object DecimalIntegral582DiagnosticPolicy {
    private val sources = listOf("0.00", "0.1", "1.00", "-1.00", "10.50", "1000", "1E+4")
    private val divisors = listOf("0.00", "2.0", "-2.0", "1E+3")
    private val precisions = listOf("-1", "0", "1", "2", "7")
    private val modes = listOf("throw", "7.9", "-7.9", "0.00", "1E+4", "1E+1", "5E+2", "0", "7", "source")
    private val reported = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -10, -3, -2, -1, 0, 1, 2, 3, 9, 10, Int.MAX_VALUE - 1, Int.MAX_VALUE)
    private val scales = listOf(1 to 1, 0 to 1, -1 to 1, Int.MIN_VALUE to Int.MAX_VALUE, Int.MAX_VALUE to Int.MIN_VALUE, 0 to Int.MIN_VALUE)
    private val selectedRecords: Map<List<String>, List<String>> by lazy {
        val rows = frozenDispatch.lines().map { it.split('\t') }
        check(rows.size == 420 && rows.all { it.size == 5 })
        val indexed = rows.associateBy { it.take(4) }
        check(indexed.size == 420)
        indexed
    }

    fun dispatchSelected(row: Array<String>): Boolean = row.size == 5 && row[1] in divisors.drop(1) && row[2] in precisions.take(2)

    fun bindDispatch(rows: List<Array<String>>) {
        assertEquals(1400, rows.size)
        val keys = mutableSetOf<List<String>>()
        var selected = 0
        for ((ordinal, row) in rows.withIndex()) {
            assertEquals(5, row.size)
            val key = listOf(sources[ordinal / 200], divisors[(ordinal / 50) % 4], precisions[(ordinal / 10) % 5], modes[ordinal % 10])
            assertEquals(key, row.take(4), "dispatch ordinal=$ordinal")
            assertTrue(keys.add(key), "duplicate dispatch key=$key")
            if (dispatchSelected(row)) {
                assertEquals(selectedRecords.getValue(key), row.toList(), "frozen dispatch ordinal=$ordinal")
                selected++
            }
        }
        assertEquals(420, selected)
    }

    fun metadataSelected(row: Array<String>): Boolean = row.take(4) == listOf("3", "2", "1", "1")

    fun bindMetadata(rows: List<Array<String>>) {
        assertEquals(1176, rows.size)
        val keys = mutableSetOf<List<String>>()
        var selected = 0
        for ((ordinal, row) in rows.withIndex()) {
            assertEquals(5, row.size)
            val (aScale, bScale) = scales[ordinal % 6]
            val key = listOf(reported[ordinal / 84].toString(), reported[(ordinal / 6) % 14].toString(), aScale.toString(), bScale.toString())
            assertEquals(key, row.take(4), "metadata ordinal=$ordinal")
            assertTrue(keys.add(key), "duplicate metadata key=$key")
            if (metadataSelected(row)) {
                assertEquals(listOf("3", "2", "1", "1", "!IllegalStateException:divide callback|A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(12,DOWN);"), row.toList())
                assertEquals(804, ordinal)
                selected++
            }
        }
        assertEquals(1, selected)
    }

    data class Numeric(val coefficient: String, val scale: Int, val text: String)

    private fun power(n: Int): Long {
        check(n in 0..5)
        var value = 1L
        repeat(n) { value *= 10L }
        return value
    }

    private fun input(text: String): Pair<Long, Int> = when (text) {
        "0.00" -> 0L to 2
        "0.1" -> 1L to 1
        "1.00" -> 100L to 2
        "-1.00" -> -100L to 2
        "10.50" -> 1050L to 2
        "1000" -> 1000L to 0
        "1E+4" -> 1L to -4
        "2.0" -> 20L to 1
        "-2.0" -> -20L to 1
        "1E+3" -> 1L to -3
        "10.0" -> 100L to 1
        else -> error("Unselected integral input: $text")
    }

    fun expected(a: String, b: String): Numeric {
        val (aCoefficient, aScale) = input(a)
        val (bCoefficient, bScale) = input(b)
        check(bCoefficient != 0L)
        val preferred = aScale - bScale
        check(preferred in -5..5)
        val numerator = aCoefficient * power(maxOf(0, -preferred))
        val denominator = bCoefficient * power(maxOf(0, preferred))
        var coefficient = numerator / denominator // Long division truncates toward zero.
        var scale = 0
        if (coefficient == 0L) scale = preferred
        else if (preferred >= 0) {
            coefficient *= power(preferred)
            scale = preferred
        } else {
            while (scale > preferred && coefficient % 10L == 0L) {
                coefficient /= 10L
                scale--
            }
        }
        return Numeric(coefficient.toString(), scale, canonical(coefficient, scale))
    }

    private fun canonical(coefficient: Long, scale: Int): String {
        val sign = if (coefficient < 0) "-" else ""
        val digits = (if (coefficient < 0) -coefficient else coefficient).toString()
        val adjusted = digits.length - 1 - scale
        if (scale >= 0 && adjusted >= -6) {
            if (scale == 0) return sign + digits
            val point = digits.length - scale
            return sign + if (point > 0) digits.substring(0, point) + "." + digits.substring(point)
                else "0." + "0".repeat(-point) + digits
        }
        val significand = digits.take(1) + if (digits.length > 1) "." + digits.drop(1) else ""
        return sign + significand + "E" + if (adjusted >= 0) "+$adjusted" else adjusted.toString()
    }

    fun numericFailure(kind: String, ordinal: Int, row: Array<String>, actual: String, observed: Numeric?, expected: Numeric): String? {
        val diagnostic = "INTEGRAL_582\t$kind\t$ordinal\t${row.joinToString("\t")}\tREFERENCE_INHERITED_DIVIDE_DISPATCH\tREQUIRED_STORED_QUOTIENT\t$actual\t$observed\t$expected"
        println(diagnostic) // Every selected row, including matches; historical result/trace/identity retained.
        return if (expected == observed) null else diagnostic
    }

    // Literal full original records copied from the immutable JDK25 fixture,
    // SHA256 31e6d042d91c4c3b5a0cdea8845e284c5e9a9fef94f1ff2c412514ae11720f6f.
    private val frozenDispatch = """
0.00	2.0	-1	throw	0.0:1:base|
0.00	2.0	-1	7.9	0.0:1:base|
0.00	2.0	-1	-7.9	0.0:1:base|
0.00	2.0	-1	0.00	0.0:1:base|
0.00	2.0	-1	1E+4	0.0:1:base|
0.00	2.0	-1	1E+1	0.0:1:base|
0.00	2.0	-1	5E+2	0.0:1:base|
0.00	2.0	-1	0	0.0:1:base|
0.00	2.0	-1	7	0.0:1:base|
0.00	2.0	-1	source	0.0:1:base|
0.00	2.0	0	throw	0.0:1:base|A.integral;
0.00	2.0	0	7.9	0.0:1:base|A.integral;
0.00	2.0	0	-7.9	0.0:1:base|A.integral;
0.00	2.0	0	0.00	0.0:1:base|A.integral;
0.00	2.0	0	1E+4	0.0:1:base|A.integral;
0.00	2.0	0	1E+1	0.0:1:base|A.integral;
0.00	2.0	0	5E+2	0.0:1:base|A.integral;
0.00	2.0	0	0	0.0:1:base|A.integral;
0.00	2.0	0	7	0.0:1:base|A.integral;
0.00	2.0	0	source	0.0:1:base|A.integral;
0.00	-2.0	-1	throw	0.0:1:base|
0.00	-2.0	-1	7.9	0.0:1:base|
0.00	-2.0	-1	-7.9	0.0:1:base|
0.00	-2.0	-1	0.00	0.0:1:base|
0.00	-2.0	-1	1E+4	0.0:1:base|
0.00	-2.0	-1	1E+1	0.0:1:base|
0.00	-2.0	-1	5E+2	0.0:1:base|
0.00	-2.0	-1	0	0.0:1:base|
0.00	-2.0	-1	7	0.0:1:base|
0.00	-2.0	-1	source	0.0:1:base|
0.00	-2.0	0	throw	0.0:1:base|A.integral;
0.00	-2.0	0	7.9	0.0:1:base|A.integral;
0.00	-2.0	0	-7.9	0.0:1:base|A.integral;
0.00	-2.0	0	0.00	0.0:1:base|A.integral;
0.00	-2.0	0	1E+4	0.0:1:base|A.integral;
0.00	-2.0	0	1E+1	0.0:1:base|A.integral;
0.00	-2.0	0	5E+2	0.0:1:base|A.integral;
0.00	-2.0	0	0	0.0:1:base|A.integral;
0.00	-2.0	0	7	0.0:1:base|A.integral;
0.00	-2.0	0	source	0.0:1:base|A.integral;
0.00	1E+3	-1	throw	0.00000:5:base|
0.00	1E+3	-1	7.9	0.00000:5:base|
0.00	1E+3	-1	-7.9	0.00000:5:base|
0.00	1E+3	-1	0.00	0.00000:5:base|
0.00	1E+3	-1	1E+4	0.00000:5:base|
0.00	1E+3	-1	1E+1	0.00000:5:base|
0.00	1E+3	-1	5E+2	0.00000:5:base|
0.00	1E+3	-1	0	0.00000:5:base|
0.00	1E+3	-1	7	0.00000:5:base|
0.00	1E+3	-1	source	0.00000:5:base|
0.00	1E+3	0	throw	0.00000:5:base|A.integral;
0.00	1E+3	0	7.9	0.00000:5:base|A.integral;
0.00	1E+3	0	-7.9	0.00000:5:base|A.integral;
0.00	1E+3	0	0.00	0.00000:5:base|A.integral;
0.00	1E+3	0	1E+4	0.00000:5:base|A.integral;
0.00	1E+3	0	1E+1	0.00000:5:base|A.integral;
0.00	1E+3	0	5E+2	0.00000:5:base|A.integral;
0.00	1E+3	0	0	0.00000:5:base|A.integral;
0.00	1E+3	0	7	0.00000:5:base|A.integral;
0.00	1E+3	0	source	0.00000:5:base|A.integral;
0.1	2.0	-1	throw	0:0:base|
0.1	2.0	-1	7.9	0:0:base|
0.1	2.0	-1	-7.9	0:0:base|
0.1	2.0	-1	0.00	0:0:base|
0.1	2.0	-1	1E+4	0:0:base|
0.1	2.0	-1	1E+1	0:0:base|
0.1	2.0	-1	5E+2	0:0:base|
0.1	2.0	-1	0	0:0:base|
0.1	2.0	-1	7	0:0:base|
0.1	2.0	-1	source	0:0:base|
0.1	2.0	0	throw	0:0:base|A.integral;
0.1	2.0	0	7.9	0:0:base|A.integral;
0.1	2.0	0	-7.9	0:0:base|A.integral;
0.1	2.0	0	0.00	0:0:base|A.integral;
0.1	2.0	0	1E+4	0:0:base|A.integral;
0.1	2.0	0	1E+1	0:0:base|A.integral;
0.1	2.0	0	5E+2	0:0:base|A.integral;
0.1	2.0	0	0	0:0:base|A.integral;
0.1	2.0	0	7	0:0:base|A.integral;
0.1	2.0	0	source	0:0:base|A.integral;
0.1	-2.0	-1	throw	0:0:base|
0.1	-2.0	-1	7.9	0:0:base|
0.1	-2.0	-1	-7.9	0:0:base|
0.1	-2.0	-1	0.00	0:0:base|
0.1	-2.0	-1	1E+4	0:0:base|
0.1	-2.0	-1	1E+1	0:0:base|
0.1	-2.0	-1	5E+2	0:0:base|
0.1	-2.0	-1	0	0:0:base|
0.1	-2.0	-1	7	0:0:base|
0.1	-2.0	-1	source	0:0:base|
0.1	-2.0	0	throw	0:0:base|A.integral;
0.1	-2.0	0	7.9	0:0:base|A.integral;
0.1	-2.0	0	-7.9	0:0:base|A.integral;
0.1	-2.0	0	0.00	0:0:base|A.integral;
0.1	-2.0	0	1E+4	0:0:base|A.integral;
0.1	-2.0	0	1E+1	0:0:base|A.integral;
0.1	-2.0	0	5E+2	0:0:base|A.integral;
0.1	-2.0	0	0	0:0:base|A.integral;
0.1	-2.0	0	7	0:0:base|A.integral;
0.1	-2.0	0	source	0:0:base|A.integral;
0.1	1E+3	-1	throw	0.0000:4:base|A.precision;B.precision;
0.1	1E+3	-1	7.9	0.0000:4:base|A.precision;B.precision;
0.1	1E+3	-1	-7.9	0.0000:4:base|A.precision;B.precision;
0.1	1E+3	-1	0.00	0.0000:4:base|A.precision;B.precision;
0.1	1E+3	-1	1E+4	0.0000:4:base|A.precision;B.precision;
0.1	1E+3	-1	1E+1	0.0000:4:base|A.precision;B.precision;
0.1	1E+3	-1	5E+2	0.0000:4:base|A.precision;B.precision;
0.1	1E+3	-1	0	0.0000:4:base|A.precision;B.precision;
0.1	1E+3	-1	7	0.0000:4:base|A.precision;B.precision;
0.1	1E+3	-1	source	0.0000:4:base|A.precision;B.precision;
0.1	1E+3	0	throw	0.0000:4:base|A.integral;A.precision;B.precision;
0.1	1E+3	0	7.9	0.0000:4:base|A.integral;A.precision;B.precision;
0.1	1E+3	0	-7.9	0.0000:4:base|A.integral;A.precision;B.precision;
0.1	1E+3	0	0.00	0.0000:4:base|A.integral;A.precision;B.precision;
0.1	1E+3	0	1E+4	0.0000:4:base|A.integral;A.precision;B.precision;
0.1	1E+3	0	1E+1	0.0000:4:base|A.integral;A.precision;B.precision;
0.1	1E+3	0	5E+2	0.0000:4:base|A.integral;A.precision;B.precision;
0.1	1E+3	0	0	0.0000:4:base|A.integral;A.precision;B.precision;
0.1	1E+3	0	7	0.0000:4:base|A.integral;A.precision;B.precision;
0.1	1E+3	0	source	0.0000:4:base|A.integral;A.precision;B.precision;
1.00	2.0	-1	throw	0.0:1:base|A.precision;B.precision;
1.00	2.0	-1	7.9	0.0:1:base|A.precision;B.precision;
1.00	2.0	-1	-7.9	0.0:1:base|A.precision;B.precision;
1.00	2.0	-1	0.00	0.0:1:base|A.precision;B.precision;
1.00	2.0	-1	1E+4	0.0:1:base|A.precision;B.precision;
1.00	2.0	-1	1E+1	0.0:1:base|A.precision;B.precision;
1.00	2.0	-1	5E+2	0.0:1:base|A.precision;B.precision;
1.00	2.0	-1	0	0.0:1:base|A.precision;B.precision;
1.00	2.0	-1	7	0.0:1:base|A.precision;B.precision;
1.00	2.0	-1	source	0.0:1:base|A.precision;B.precision;
1.00	2.0	0	throw	0.0:1:base|A.integral;A.precision;B.precision;
1.00	2.0	0	7.9	0.0:1:base|A.integral;A.precision;B.precision;
1.00	2.0	0	-7.9	0.0:1:base|A.integral;A.precision;B.precision;
1.00	2.0	0	0.00	0.0:1:base|A.integral;A.precision;B.precision;
1.00	2.0	0	1E+4	0.0:1:base|A.integral;A.precision;B.precision;
1.00	2.0	0	1E+1	0.0:1:base|A.integral;A.precision;B.precision;
1.00	2.0	0	5E+2	0.0:1:base|A.integral;A.precision;B.precision;
1.00	2.0	0	0	0.0:1:base|A.integral;A.precision;B.precision;
1.00	2.0	0	7	0.0:1:base|A.integral;A.precision;B.precision;
1.00	2.0	0	source	0.0:1:base|A.integral;A.precision;B.precision;
1.00	-2.0	-1	throw	0.0:1:base|A.precision;B.precision;
1.00	-2.0	-1	7.9	0.0:1:base|A.precision;B.precision;
1.00	-2.0	-1	-7.9	0.0:1:base|A.precision;B.precision;
1.00	-2.0	-1	0.00	0.0:1:base|A.precision;B.precision;
1.00	-2.0	-1	1E+4	0.0:1:base|A.precision;B.precision;
1.00	-2.0	-1	1E+1	0.0:1:base|A.precision;B.precision;
1.00	-2.0	-1	5E+2	0.0:1:base|A.precision;B.precision;
1.00	-2.0	-1	0	0.0:1:base|A.precision;B.precision;
1.00	-2.0	-1	7	0.0:1:base|A.precision;B.precision;
1.00	-2.0	-1	source	0.0:1:base|A.precision;B.precision;
1.00	-2.0	0	throw	0.0:1:base|A.integral;A.precision;B.precision;
1.00	-2.0	0	7.9	0.0:1:base|A.integral;A.precision;B.precision;
1.00	-2.0	0	-7.9	0.0:1:base|A.integral;A.precision;B.precision;
1.00	-2.0	0	0.00	0.0:1:base|A.integral;A.precision;B.precision;
1.00	-2.0	0	1E+4	0.0:1:base|A.integral;A.precision;B.precision;
1.00	-2.0	0	1E+1	0.0:1:base|A.integral;A.precision;B.precision;
1.00	-2.0	0	5E+2	0.0:1:base|A.integral;A.precision;B.precision;
1.00	-2.0	0	0	0.0:1:base|A.integral;A.precision;B.precision;
1.00	-2.0	0	7	0.0:1:base|A.integral;A.precision;B.precision;
1.00	-2.0	0	source	0.0:1:base|A.integral;A.precision;B.precision;
1.00	1E+3	-1	throw	0.00000:5:base|A.precision;B.precision;
1.00	1E+3	-1	7.9	0.00000:5:base|A.precision;B.precision;
1.00	1E+3	-1	-7.9	0.00000:5:base|A.precision;B.precision;
1.00	1E+3	-1	0.00	0.00000:5:base|A.precision;B.precision;
1.00	1E+3	-1	1E+4	0.00000:5:base|A.precision;B.precision;
1.00	1E+3	-1	1E+1	0.00000:5:base|A.precision;B.precision;
1.00	1E+3	-1	5E+2	0.00000:5:base|A.precision;B.precision;
1.00	1E+3	-1	0	0.00000:5:base|A.precision;B.precision;
1.00	1E+3	-1	7	0.00000:5:base|A.precision;B.precision;
1.00	1E+3	-1	source	0.00000:5:base|A.precision;B.precision;
1.00	1E+3	0	throw	0.00000:5:base|A.integral;A.precision;B.precision;
1.00	1E+3	0	7.9	0.00000:5:base|A.integral;A.precision;B.precision;
1.00	1E+3	0	-7.9	0.00000:5:base|A.integral;A.precision;B.precision;
1.00	1E+3	0	0.00	0.00000:5:base|A.integral;A.precision;B.precision;
1.00	1E+3	0	1E+4	0.00000:5:base|A.integral;A.precision;B.precision;
1.00	1E+3	0	1E+1	0.00000:5:base|A.integral;A.precision;B.precision;
1.00	1E+3	0	5E+2	0.00000:5:base|A.integral;A.precision;B.precision;
1.00	1E+3	0	0	0.00000:5:base|A.integral;A.precision;B.precision;
1.00	1E+3	0	7	0.00000:5:base|A.integral;A.precision;B.precision;
1.00	1E+3	0	source	0.00000:5:base|A.integral;A.precision;B.precision;
-1.00	2.0	-1	throw	0.0:1:base|A.precision;B.precision;
-1.00	2.0	-1	7.9	0.0:1:base|A.precision;B.precision;
-1.00	2.0	-1	-7.9	0.0:1:base|A.precision;B.precision;
-1.00	2.0	-1	0.00	0.0:1:base|A.precision;B.precision;
-1.00	2.0	-1	1E+4	0.0:1:base|A.precision;B.precision;
-1.00	2.0	-1	1E+1	0.0:1:base|A.precision;B.precision;
-1.00	2.0	-1	5E+2	0.0:1:base|A.precision;B.precision;
-1.00	2.0	-1	0	0.0:1:base|A.precision;B.precision;
-1.00	2.0	-1	7	0.0:1:base|A.precision;B.precision;
-1.00	2.0	-1	source	0.0:1:base|A.precision;B.precision;
-1.00	2.0	0	throw	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	2.0	0	7.9	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	2.0	0	-7.9	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	2.0	0	0.00	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	2.0	0	1E+4	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	2.0	0	1E+1	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	2.0	0	5E+2	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	2.0	0	0	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	2.0	0	7	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	2.0	0	source	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	-2.0	-1	throw	0.0:1:base|A.precision;B.precision;
-1.00	-2.0	-1	7.9	0.0:1:base|A.precision;B.precision;
-1.00	-2.0	-1	-7.9	0.0:1:base|A.precision;B.precision;
-1.00	-2.0	-1	0.00	0.0:1:base|A.precision;B.precision;
-1.00	-2.0	-1	1E+4	0.0:1:base|A.precision;B.precision;
-1.00	-2.0	-1	1E+1	0.0:1:base|A.precision;B.precision;
-1.00	-2.0	-1	5E+2	0.0:1:base|A.precision;B.precision;
-1.00	-2.0	-1	0	0.0:1:base|A.precision;B.precision;
-1.00	-2.0	-1	7	0.0:1:base|A.precision;B.precision;
-1.00	-2.0	-1	source	0.0:1:base|A.precision;B.precision;
-1.00	-2.0	0	throw	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	-2.0	0	7.9	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	-2.0	0	-7.9	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	-2.0	0	0.00	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	-2.0	0	1E+4	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	-2.0	0	1E+1	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	-2.0	0	5E+2	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	-2.0	0	0	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	-2.0	0	7	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	-2.0	0	source	0.0:1:base|A.integral;A.precision;B.precision;
-1.00	1E+3	-1	throw	0.00000:5:base|A.precision;B.precision;
-1.00	1E+3	-1	7.9	0.00000:5:base|A.precision;B.precision;
-1.00	1E+3	-1	-7.9	0.00000:5:base|A.precision;B.precision;
-1.00	1E+3	-1	0.00	0.00000:5:base|A.precision;B.precision;
-1.00	1E+3	-1	1E+4	0.00000:5:base|A.precision;B.precision;
-1.00	1E+3	-1	1E+1	0.00000:5:base|A.precision;B.precision;
-1.00	1E+3	-1	5E+2	0.00000:5:base|A.precision;B.precision;
-1.00	1E+3	-1	0	0.00000:5:base|A.precision;B.precision;
-1.00	1E+3	-1	7	0.00000:5:base|A.precision;B.precision;
-1.00	1E+3	-1	source	0.00000:5:base|A.precision;B.precision;
-1.00	1E+3	0	throw	0.00000:5:base|A.integral;A.precision;B.precision;
-1.00	1E+3	0	7.9	0.00000:5:base|A.integral;A.precision;B.precision;
-1.00	1E+3	0	-7.9	0.00000:5:base|A.integral;A.precision;B.precision;
-1.00	1E+3	0	0.00	0.00000:5:base|A.integral;A.precision;B.precision;
-1.00	1E+3	0	1E+4	0.00000:5:base|A.integral;A.precision;B.precision;
-1.00	1E+3	0	1E+1	0.00000:5:base|A.integral;A.precision;B.precision;
-1.00	1E+3	0	5E+2	0.00000:5:base|A.integral;A.precision;B.precision;
-1.00	1E+3	0	0	0.00000:5:base|A.integral;A.precision;B.precision;
-1.00	1E+3	0	7	0.00000:5:base|A.integral;A.precision;B.precision;
-1.00	1E+3	0	source	0.00000:5:base|A.integral;A.precision;B.precision;
10.50	2.0	-1	throw	!IllegalStateException:divide callback|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
10.50	2.0	-1	7.9	7.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	2.0	-1	-7.9	-7.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	2.0	-1	0.00	0.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	2.0	-1	1E+4	10000.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	2.0	-1	1E+1	10.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	2.0	-1	5E+2	500.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	2.0	-1	0	0.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	2.0	-1	7	7.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	2.0	-1	source	10.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);A.sign;
10.50	2.0	0	throw	!IllegalStateException:divide callback|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
10.50	2.0	0	7.9	7.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	2.0	0	-7.9	-7.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	2.0	0	0.00	0.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	2.0	0	1E+4	10000.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	2.0	0	1E+1	10.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	2.0	0	5E+2	500.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	2.0	0	0	0.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	2.0	0	7	7.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	2.0	0	source	10.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);A.sign;
10.50	-2.0	-1	throw	!IllegalStateException:divide callback|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
10.50	-2.0	-1	7.9	7.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	-2.0	-1	-7.9	-7.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	-2.0	-1	0.00	0.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	-2.0	-1	1E+4	10000.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	-2.0	-1	1E+1	10.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	-2.0	-1	5E+2	500.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	-2.0	-1	0	0.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	-2.0	-1	7	7.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	-2.0	-1	source	10.0:1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);A.sign;
10.50	-2.0	0	throw	!IllegalStateException:divide callback|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
10.50	-2.0	0	7.9	7.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	-2.0	0	-7.9	-7.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	-2.0	0	0.00	0.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
10.50	-2.0	0	1E+4	10000.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	-2.0	0	1E+1	10.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	-2.0	0	5E+2	500.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	-2.0	0	0	0.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	-2.0	0	7	7.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(1,7);Q.sign;
10.50	-2.0	0	source	10.0:1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);A.sign;
10.50	1E+3	-1	throw	0.00000:5:base|A.precision;B.precision;
10.50	1E+3	-1	7.9	0.00000:5:base|A.precision;B.precision;
10.50	1E+3	-1	-7.9	0.00000:5:base|A.precision;B.precision;
10.50	1E+3	-1	0.00	0.00000:5:base|A.precision;B.precision;
10.50	1E+3	-1	1E+4	0.00000:5:base|A.precision;B.precision;
10.50	1E+3	-1	1E+1	0.00000:5:base|A.precision;B.precision;
10.50	1E+3	-1	5E+2	0.00000:5:base|A.precision;B.precision;
10.50	1E+3	-1	0	0.00000:5:base|A.precision;B.precision;
10.50	1E+3	-1	7	0.00000:5:base|A.precision;B.precision;
10.50	1E+3	-1	source	0.00000:5:base|A.precision;B.precision;
10.50	1E+3	0	throw	0.00000:5:base|A.integral;A.precision;B.precision;
10.50	1E+3	0	7.9	0.00000:5:base|A.integral;A.precision;B.precision;
10.50	1E+3	0	-7.9	0.00000:5:base|A.integral;A.precision;B.precision;
10.50	1E+3	0	0.00	0.00000:5:base|A.integral;A.precision;B.precision;
10.50	1E+3	0	1E+4	0.00000:5:base|A.integral;A.precision;B.precision;
10.50	1E+3	0	1E+1	0.00000:5:base|A.integral;A.precision;B.precision;
10.50	1E+3	0	5E+2	0.00000:5:base|A.integral;A.precision;B.precision;
10.50	1E+3	0	0	0.00000:5:base|A.integral;A.precision;B.precision;
10.50	1E+3	0	7	0.00000:5:base|A.integral;A.precision;B.precision;
10.50	1E+3	0	source	0.00000:5:base|A.integral;A.precision;B.precision;
1000	2.0	-1	throw	!IllegalStateException:divide callback|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	2.0	-1	7.9	7:0:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	2.0	-1	-7.9	-7:0:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	2.0	-1	0.00	0E+1:-1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	2.0	-1	1E+4	1.000E+4:-1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(-1,7);Q.sign;
1000	2.0	-1	1E+1	1E+1:-1:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	2.0	-1	5E+2	5.0E+2:-1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(-1,7);Q.sign;
1000	2.0	-1	0	0:0:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	2.0	-1	7	7:0:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	2.0	-1	source	1000:0:source|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	2.0	0	throw	!IllegalStateException:divide callback|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	2.0	0	7.9	7:0:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	2.0	0	-7.9	-7:0:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	2.0	0	0.00	0E+1:-1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	2.0	0	1E+4	1.000E+4:-1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(-1,7);Q.sign;
1000	2.0	0	1E+1	1E+1:-1:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	2.0	0	5E+2	5.0E+2:-1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(-1,7);Q.sign;
1000	2.0	0	0	0:0:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	2.0	0	7	7:0:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	2.0	0	source	1000:0:source|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	-2.0	-1	throw	!IllegalStateException:divide callback|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	-2.0	-1	7.9	7:0:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	-2.0	-1	-7.9	-7:0:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	-2.0	-1	0.00	0E+1:-1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	-2.0	-1	1E+4	1.000E+4:-1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(-1,7);Q.sign;
1000	-2.0	-1	1E+1	1E+1:-1:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	-2.0	-1	5E+2	5.0E+2:-1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(-1,7);Q.sign;
1000	-2.0	-1	0	0:0:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	-2.0	-1	7	7:0:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	-2.0	-1	source	1000:0:source|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	-2.0	0	throw	!IllegalStateException:divide callback|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	-2.0	0	7.9	7:0:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	-2.0	0	-7.9	-7:0:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	-2.0	0	0.00	0E+1:-1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	-2.0	0	1E+4	1.000E+4:-1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(-1,7);Q.sign;
1000	-2.0	0	1E+1	1E+1:-1:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	-2.0	0	5E+2	5.0E+2:-1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);Q.setInt(-1,7);Q.sign;
1000	-2.0	0	0	0:0:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	-2.0	0	7	7:0:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	-2.0	0	source	1000:0:source|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(14,DOWN);
1000	1E+3	-1	throw	!IllegalStateException:divide callback|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);
1000	1E+3	-1	7.9	7.000:3:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	1E+3	-1	-7.9	-7.000:3:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	1E+3	-1	0.00	0.000:3:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	1E+3	-1	1E+4	10000.000:3:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.setInt(3,7);Q.sign;
1000	1E+3	-1	1E+1	10.000:3:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.setInt(3,7);Q.sign;
1000	1E+3	-1	5E+2	500.000:3:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.setInt(3,7);Q.sign;
1000	1E+3	-1	0	0.000:3:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.setInt(3,7);Q.sign;
1000	1E+3	-1	7	7.000:3:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.setInt(3,7);Q.sign;
1000	1E+3	-1	source	1000.000:3:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);A.sign;
1000	1E+3	0	throw	!IllegalStateException:divide callback|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);
1000	1E+3	0	7.9	7.000:3:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	1E+3	0	-7.9	-7.000:3:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	1E+3	0	0.00	0.000:3:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1000	1E+3	0	1E+4	10000.000:3:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.setInt(3,7);Q.sign;
1000	1E+3	0	1E+1	10.000:3:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.setInt(3,7);Q.sign;
1000	1E+3	0	5E+2	500.000:3:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.setInt(3,7);Q.sign;
1000	1E+3	0	0	0.000:3:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.setInt(3,7);Q.sign;
1000	1E+3	0	7	7.000:3:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);Q.setInt(3,7);Q.sign;
1000	1E+3	0	source	1000.000:3:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(13,DOWN);A.sign;
1E+4	2.0	-1	throw	!IllegalStateException:divide callback|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	-1	7.9	7:0:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	2.0	-1	-7.9	-7:0:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	2.0	-1	0.00	0E+5:-5:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	2.0	-1	1E+4	1E+4:-4:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	-1	1E+1	1E+1:-1:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	-1	5E+2	5E+2:-2:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	-1	0	0:0:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	-1	7	7:0:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	-1	source	1E+4:-4:source|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	0	throw	!IllegalStateException:divide callback|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	0	7.9	7:0:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	2.0	0	-7.9	-7:0:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	2.0	0	0.00	0E+5:-5:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	2.0	0	1E+4	1E+4:-4:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	0	1E+1	1E+1:-1:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	0	5E+2	5E+2:-2:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	0	0	0:0:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	0	7	7:0:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	2.0	0	source	1E+4:-4:source|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	-1	throw	!IllegalStateException:divide callback|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	-1	7.9	7:0:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	-2.0	-1	-7.9	-7:0:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	-2.0	-1	0.00	0E+5:-5:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	-2.0	-1	1E+4	1E+4:-4:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	-1	1E+1	1E+1:-1:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	-1	5E+2	5E+2:-2:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	-1	0	0:0:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	-1	7	7:0:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	-1	source	1E+4:-4:source|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	0	throw	!IllegalStateException:divide callback|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	0	7.9	7:0:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	-2.0	0	-7.9	-7:0:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	-2.0	0	0.00	0E+5:-5:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	-2.0	0	1E+4	1E+4:-4:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	0	1E+1	1E+1:-1:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	0	5E+2	5E+2:-2:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	0	0	0:0:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	0	7	7:0:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	-2.0	0	source	1E+4:-4:source|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(15,DOWN);
1E+4	1E+3	-1	throw	!IllegalStateException:divide callback|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);
1E+4	1E+3	-1	7.9	7:0:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	1E+3	-1	-7.9	-7:0:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	1E+3	-1	0.00	0E+1:-1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	1E+3	-1	1E+4	1.000E+4:-1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);Q.setInt(-1,7);Q.sign;
1E+4	1E+3	-1	1E+1	1E+1:-1:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);
1E+4	1E+3	-1	5E+2	5.0E+2:-1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);Q.setInt(-1,7);Q.sign;
1E+4	1E+3	-1	0	0:0:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);
1E+4	1E+3	-1	7	7:0:quotient|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);
1E+4	1E+3	-1	source	1.000E+4:-1:base|A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);A.sign;
1E+4	1E+3	0	throw	!IllegalStateException:divide callback|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);
1E+4	1E+3	0	7.9	7:0:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	1E+3	0	-7.9	-7:0:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	1E+3	0	0.00	0E+1:-1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);Q.set(0,DOWN);Q.setInt(0,1);Q.sign;
1E+4	1E+3	0	1E+4	1.000E+4:-1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);Q.setInt(-1,7);Q.sign;
1E+4	1E+3	0	1E+1	1E+1:-1:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);
1E+4	1E+3	0	5E+2	5.0E+2:-1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);Q.setInt(-1,7);Q.sign;
1E+4	1E+3	0	0	0:0:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);
1E+4	1E+3	0	7	7:0:quotient|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);
1E+4	1E+3	0	source	1.000E+4:-1:base|A.integral;A.precision;B.precision;A.sign;A.precision;B.precision;A.scale;B.scale;A.divide(8,DOWN);A.sign;
    """.trimIndent()
}

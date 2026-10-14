// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals

private class ExtremeProbe(coefficient: String, scale: Int, val digits: Int, val forceSign: Boolean,
    val label: String, val trace: StringBuilder) : BigDecimal(BigInteger(coefficient), scale) {
    override fun signum(): Int {
        trace.append("$label.sign;")
        return if (forceSign) 1 else super.signum()
    }
    override fun precision(): Int { trace.append("$label.precision;"); return digits }
    fun enter(v: BigDecimal, p: Int, rounding: String): BigDecimal =
        super.divide(v, MathContext(p, RoundingMode.valueOf(rounding)))
}
private fun extremeOutcome(a: String, b: String, aScale: Int, bScale: Int, x: Int, y: Int,
    forced: Boolean, p: Int, rounding: String): String {
    val trace = StringBuilder()
    val source = ExtremeProbe(a, aScale, x, forced, "A", trace)
    val divisor = ExtremeProbe(b, bScale, y, forced, "B", trace)
    return try {
        val result = source.enter(divisor, p, rounding)
        val calls = trace.toString()
        "${result.toString()}:${result.scale()}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
}

class DecimalContextExtremeMetadataOracleTest {
    @Test fun extremeReportedPrecisionsPreserveResultsAndValidationOrder() {
        val failures = mutableListOf<String>()
        for (row in jdk25DecimalContextExtremeMetadataCases()) {
            val actual = extremeOutcome(row[0], row[1], row[2].toInt(), row[3].toInt(),
                row[4].toInt(), row[5].toInt(), row[6].toBooleanStrict(), row[7].toInt(), row[8])
            if (row[9] != actual) failures += row.take(9).toString() + " expected=" + row[9] + " actual=" + actual
        }
        assertEquals(emptyList<String>(), failures.take(12), failures.size.toString() + " mismatches")
    }
}

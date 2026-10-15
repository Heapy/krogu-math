// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.*

/** Selected internal-call observations, distinct from truthful arithmetic and explicit caller contracts. */
internal object DecimalExactRoot563Callbacks {
    data class Observation(val index: Int, val category: String, val vector: List<String>)
    val counts = mapOf("A.normalized-getter" to 540, "N.normalized-replacement-getter" to 560,
        "H.sqrt.precision" to 42, "H.sqrtSign.precision" to 28, "H.sqrtPrecision.metadata" to 126,
        "H.sqrtNormalize.strip" to 150, "H.sqrtNormalize.precision" to 210)
    val observations: List<Observation> by lazy {
        exactRoot563CallbackObservations().map { Observation(it[0].toInt(), it[1], it.drop(2)) }.toList().also {
            assertEquals(1656, it.size)
            assertEquals(1656, it.map { row -> row.index }.toSet().size)
            assertEquals(1656, it.map { row -> row.vector.take(6) }.toSet().size)
            assertEquals(counts, it.groupingBy { row -> row.category }.eachCount())
        }
    }
    private val byIndex by lazy { observations.associateBy { it.index } }
    private val byInput by lazy { observations.associateBy { it.vector.take(6) } }

    fun category(v: List<String>): String? {
        if (v.size < 6 || v[0] !in listOf("A", "N", "H")) return null
        val exact = v[4] == "0" || v[5] == "UNNECESSARY"
        return when (v[0]) {
            "A" -> if (exact && v[3].toInt() % 2 == 0) "A.normalized-getter" else null
            "N" -> if (exact) "N.normalized-replacement-getter" else null
            else -> if (v[2] !in listOf("1", "4.00", "0.0004")) null else when (v[1]) {
                "sqrt" -> if (!exact) "H.sqrt.precision" else null
                "sqrtSign" -> if (!exact && v[3] == "1") "H.sqrtSign.precision" else null
                "sqrtPrecision" -> if (!exact) "H.sqrtPrecision.metadata" else null
                "sqrtNormalize" -> if (exact) "H.sqrtNormalize.strip" else "H.sqrtNormalize.precision"
                else -> null
            }
        }
    }

    /** Checks all old fields, including the complete frozen observation, at its unchanged index. */
    fun retain(v: Array<String>, index: Int) {
        val observation = byIndex.getValue(index)
        assertEquals(observation.vector, v.toList(), "retained callback row $index")
        assertEquals(observation.category, category(v.toList()))
    }

    /** Direct-JVM probes compare their actual JDK observation with the same original input key. */
    fun retainJdk(v: List<String>, actualJdkObservation: String) {
        val observation = byInput.getValue(v.take(6))
        assertEquals(observation.category, category(v))
        assertEquals(observation.vector[6], actualJdkObservation, "retained JDK callback ${v.take(6)}")
    }

    private data class Source(val coefficient: String, val scale: Int, val root: String, val rootScale: Int)
    private fun source(v: List<String>): Source = when (v[0]) {
        "A" -> Source("4", v[3].toInt(), "2", v[3].toInt() / 2)
        "N" -> Source("400", 2, "2", 0)
        else -> when (v[2]) {
            "1" -> Source("1", 0, "1", 0)
            "4.00" -> Source("400", 2, "2", 0)
            "0.0004" -> Source("4", 4, "2", 2)
            else -> error("Unclassified source")
        }
    }
    private class Truthful(c: BigInteger, s: Int) : BigDecimal(c, s) {
        override fun signum() = super.signum()
        override fun precision() = super.precision()
        override fun scale() = super.scale()
        override fun unscaledValue() = super.unscaledValue()
        override fun stripTrailingZeros() = super.stripTrailingZeros()
        override fun sqrt(mc: MathContext) = super.sqrt(mc)
        fun inheritedRoot(mc: MathContext) = super.sqrt(mc)
    }

    fun expected(v: List<String>): DecimalExactRoot563Expected.Result {
        val source = source(v)
        return DecimalExactRoot563Expected.rounded(source.root, source.rootScale, source.scale / 2,
            MathContext(v[4].toInt(), RoundingMode.valueOf(v[5])))
    }

    /** Every selected configuration is exercised on ordinary, fully truthful and caller-super sources. */
    fun verifyControls(v: List<String>) {
        assertNotNull(category(v))
        val source = source(v)
        val context = MathContext(v[4].toInt(), RoundingMode.valueOf(v[5]))
        val expected = expected(v)
        val ordinary = BigDecimal(BigInteger(source.coefficient), source.scale)
        val truthful = Truthful(BigInteger(source.coefficient), source.scale)
        for (result in listOf(ordinary.sqrt(context), truthful.sqrt(context), truthful.inheritedRoot(context))) {
            assertEquals(expected.coefficient, result.unscaledValue().toString(), "$v coefficient")
            assertEquals(expected.scale, result.scale(), "$v scale")
            assertEquals(expected.coefficient.length, result.precision(), "$v result precision")
            assertEquals(expected.text(), result.toString(), "$v canonical text")
        }
    }

    /** Retain prefix diagnostics and truthful numeric fields; poisoned normalization arithmetic stays observational. */
    fun verifyProbeFields(v: List<String>, observation: String) {
        val source = source(v)
        if (v[0] == "H")
            assertTrue(observation.substringAfter('|').startsWith("sign;"), "retained sign callback prefix (task563 diagnostic) $v")
        val truthful = when (v[0]) {
            "A" -> v[2] == "4"
            "N" -> false // replacement normalization/getter objects are not fully truthful outer sources.
            else -> when (v[1]) {
                "sqrt", "sqrtSign" -> true
                "sqrtPrecision" -> v[3].toInt() == source.coefficient.length
                "sqrtNormalize" -> v[3] == "~"
                else -> false
            }
        }
        if (truthful) assertEquals(expected(v).text(), observation.substringBefore('|'), "truthful original numeric field $v")
    }

    /** Caller-owned getter/normalization replacements remain mandatory on explicit calls. */
    fun verifyDirectOverrides(v: List<String>) {
        when (v[0]) {
            "A", "N" -> {
                val failure = IllegalStateException("caller-owned coefficient")
                val marker = if (v[0] == "N" && v[3] !in listOf("~", "throw")) BigInteger(v[3]) else null
                val normalized = if (v[0] == "A") object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                    override fun unscaledValue(): BigInteger = super.unscaledValue()
                } else
                    object : BigDecimal(v[2]) {
                        private var calls = 0
                        override fun unscaledValue(): BigInteger {
                            if (calls++ == 0 && v[3] != "~") {
                                if (v[3] == "throw") throw failure
                                return marker!!
                            }
                            return super.unscaledValue()
                        }
                    }
                val source = source(v)
                val owner: BigDecimal = object : BigDecimal(BigInteger(source.coefficient), source.scale) {
                    override fun stripTrailingZeros(): BigDecimal = normalized
                }
                assertSame(normalized, owner.stripTrailingZeros())
                if (v[0] == "N" && v[3] == "throw")
                    assertSame(failure, assertFailsWith<IllegalStateException> { normalized.unscaledValue() })
                else if (marker != null) assertSame(marker, normalized.unscaledValue())
                else assertEquals(if (v[0] == "A") v[2] else BigDecimal(v[2]).unscaledValue().toString(), normalized.unscaledValue().toString())
                if (v[0] == "N")
                    assertEquals(BigDecimal(v[2]).unscaledValue(), normalized.unscaledValue(), "second direct getter delegates")
            }
            "H" -> when (v[1]) {
                "sqrtPrecision" -> {
                    val value: BigDecimal = object : BigDecimal(v[2]) { override fun precision(): Int = v[3].toInt() }
                    assertEquals(v[3].toInt(), value.precision())
                    val source = source(v)
                    assertEquals(source.coefficient, value.unscaledValue().toString())
                    assertEquals(source.scale, value.scale())
                    assertEquals(v[2], value.toString())
                }
                "sqrtNormalize" -> {
                    val marker = if (v[3] == "~") BigDecimal(v[2]).stripTrailingZeros() else BigDecimal(v[3])
                    val value: BigDecimal = object : BigDecimal(v[2]) { override fun stripTrailingZeros(): BigDecimal = marker }
                    assertSame(marker, value.stripTrailingZeros())
                }
                "sqrtSign" -> {
                    val value: BigDecimal = object : BigDecimal(v[2]) { override fun signum(): Int = v[3].toInt() }
                    assertEquals(v[3].toInt(), value.signum())
                }
            }
        }
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Only the original finite ordinary division grid; never selected by an outcome. */
internal object DecimalDivision581DiagnosticPolicy {
    private const val FIRST_PORTABLE = 18478
    private val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -10, 0, 10, Int.MAX_VALUE - 1, Int.MAX_VALUE)
    private val precisions = listOf(1, 2, 7, Int.MAX_VALUE - 1, Int.MAX_VALUE)
    private val modes = listOf("UP", "DOWN", "CEILING", "FLOOR", "HALF_UP", "HALF_DOWN", "HALF_EVEN", "UNNECESSARY")

    data class Input(val sourceScale: Int, val divisorScale: Int, val precision: Int,
        val divisor: Int, val mode: String) {
        val key: List<String> get() = listOf("G", "divide", "1@$sourceScale", "$divisor@$divisorScale",
            precision.toString(), mode)
    }

    data class Outcome(val text: String, val coefficient: String?, val scale: Int?, val errorClass: String?)

    fun capture(block: () -> BigDecimal): Outcome = try {
        val value = block()
        Outcome(value.toString(), value.unscaledValue().toString(), value.scale(), null)
    } catch (failure: Exception) {
        Outcome("!${failure::class.simpleName}:${failure.message}", null, null, failure::class.simpleName)
    }

    private val inputs = buildList {
        for (sa in scales) for (sb in scales) for (p in precisions) for (divisor in 1..3)
            for (mode in modes) add(Input(sa, sb, p, divisor, mode))
    }
    private val inputsByKey = inputs.associateBy { it.key }
    private val gridOrdinals = inputs.withIndex().associate { it.value.key to it.index }
    private val portableInputs = inputs.filter { it.mode == "HALF_UP" }
    private val portableByKey = portableInputs.associateBy { it.key }
    private val portableRecords: Map<Int, List<String>> = buildMap {
        val tokens = frozenTokens()
        assertEquals(735, tokens.size, "complete literal seventh-field ledger")
        portableInputs.forEachIndexed { offset, input ->
            val frozen = when (val token = tokens[offset]) {
                "R" -> "!ArithmeticException:BigInteger would overflow supported range"
                "U" -> "!ArithmeticException:Underflow"
                "O" -> "!ArithmeticException:Overflow"
                else -> token
            }
            put(FIRST_PORTABLE + offset, input.key + frozen)
        }
    }

    /** A key displaced outside its original ordinal, or an altered full row, fails. */
    fun portableInput(ordinal: Int, row: Array<String>): Input? {
        val original = portableRecords[ordinal]
        val input = portableByKey[row.take(6)]
        if (original == null && input == null) return null
        assertTrue(original != null, "581 portable key moved outside its original ordinals: $ordinal ${row.toList()}")
        assertEquals(original, row.toList(), "581 entire original seven-field portable record $ordinal")
        assertTrue(input != null, "581 original ordinal lost its exact input key: $ordinal")
        return input
    }

    private enum class Kind { VALUE, REQUIRED_AE, ADMITTED_LOW_AE }
    private data class Decision(val kind: Kind, val pair: String, val text: String?)

    private fun decision(input: Input): Decision {
        val pair = PreferredScaleStringModel.pair("1", input.sourceScale, input.divisor.toString(),
            input.divisorScale, input.precision, input.mode)
        return when (pair) {
            "!ArithmeticException" -> Decision(Kind.REQUIRED_AE, pair, null)
            "!AdmittedScale" -> Decision(Kind.ADMITTED_LOW_AE, pair, null)
            else -> {
                assertTrue(!pair.startsWith('!'), "unrecognized independent 581 model outcome: $pair")
                Decision(Kind.VALUE, pair, PreferredScaleStringModel.text(pair))
            }
        }
    }

    /** Records every selected outcome before reporting aggregate numerical/type failures. */
    class Recorder(private val portable: Boolean) {
        private val observed = linkedSetOf<List<String>>()
        private val observedOrdinals = linkedSetOf<Int>()
        private val failures = mutableListOf<String>()

        init {
            assertEquals(modes, RoundingMode.entries.map { it.name }, "exact eight-mode 581 grid")
            assertEquals(5880, inputs.size)
            assertEquals(5880, inputsByKey.size, "no duplicate complete JVM keys")
            assertEquals(5880, gridOrdinals.size)
            assertEquals(735, portableInputs.size)
            assertEquals(735, portableByKey.size, "no duplicate complete portable keys")
            assertEquals((18478..19212).toSet(), portableRecords.keys, "original portable ordinal interval")
        }

        fun jvm(input: Input, oldExpected: String, liveJdk: String, actual: Outcome) {
            assertTrue(!portable)
            assertEquals(input, inputsByKey[input.key], "exact finite JVM input selector")
            record(input, oldExpected, liveJdk, actual, null, null)
        }

        fun portable(ordinal: Int, row: Array<String>, oldExpected: String, actual: Outcome) {
            assertTrue(portable)
            val input = portableInput(ordinal, row)
            assertTrue(input != null, "only selected portable records enter this consumer")
            assertTrue(observedOrdinals.add(ordinal), "duplicate original portable ordinal $ordinal")
            record(input, oldExpected, null, actual, ordinal, row[6])
        }

        private fun record(input: Input, oldExpected: String, liveJdk: String?, actual: Outcome,
            ordinal: Int?, originalFrozen: String?) {
            assertTrue(observed.add(input.key), "duplicate selected 581 key ${input.key}")
            val required = decision(input)
            val frozenOrdinal = if (input.mode == "HALF_UP") {
                val offset = portableInputs.indexOf(input)
                assertTrue(offset >= 0)
                FIRST_PORTABLE + offset
            } else null
            val frozen = originalFrozen ?: frozenOrdinal?.let { portableRecords.getValue(it)[6] }
                ?: "NOT_IN_ORIGINAL_HALF_UP_FIXTURE"
            println(listOf("581_DIVISION_POLICY", "provider=${if (portable) "PORTABLE" else "JVM"}",
                "gridOrdinal=${gridOrdinals.getValue(input.key)}",
                "ordinal=${ordinal ?: frozenOrdinal ?: "NOT_IN_ORIGINAL_HALF_UP_FIXTURE"}",
                "key=${input.key.joinToString("|")}",
                "classification=${required.kind}", "model=${required.pair}",
                "frozen=$frozen", "oldExpected=$oldExpected", "liveJdk=${liveJdk ?: "NOT_EVALUATED"}",
                "actual=${actual.text}", "actualCoefficient=${actual.coefficient}",
                "actualScale=${actual.scale}", "actualErrorClass=${actual.errorClass}")
                .joinToString("\t") { escaped(it) })

            fun reject(reason: String) { failures += "${input.key}: $reason; old=$oldExpected; actual=${actual.text}" }
            if (oldExpected.startsWith('!') && !isArithmetic(oldExpected))
                reject("unexpected old required exception category")
            // Never convert an old required VALUE to an error/admission requirement.
            if (!oldExpected.startsWith('!') && (required.kind != Kind.VALUE || required.text != oldExpected))
                reject("independent model would discard/change an old required VALUE")
            when (required.kind) {
                Kind.VALUE -> {
                    val coefficient = required.pair.substringBefore('@')
                    val scale = required.pair.substringAfter('@').toInt()
                    if (actual.errorClass != null || actual.text != required.text ||
                        actual.coefficient != coefficient || actual.scale != scale)
                        reject("required exact coefficient/scale/canonical text ${required.pair} / ${required.text}")
                }
                Kind.REQUIRED_AE, Kind.ADMITTED_LOW_AE -> {
                    // LOW is a retained own560 admission, not a theorem that every LOW must throw.
                    if (!isArithmetic(oldExpected)) reject("old expected outcome was not ArithmeticException")
                    if (actual.errorClass != "ArithmeticException" || actual.coefficient != null || actual.scale != null)
                        reject("required ${required.kind} ArithmeticException category")
                }
            }
        }

        fun finish() {
            val expected = if (portable) portableByKey.keys else inputsByKey.keys
            assertEquals(expected, observed, "entire finite 581 input grid executes once, including old matches")
            assertEquals(if (portable) 735 else 5880, observed.size)
            if (portable) assertEquals(portableRecords.keys, observedOrdinals, "every original portable ordinal executes once")
            else assertTrue(observedOrdinals.isEmpty())
            assertEquals(0, failures.size, "581 numerical/type/model failures (all actuals retained above):\n${failures.joinToString("\n")}")
        }
    }

    private fun isArithmetic(text: String): Boolean =
        text == "!ArithmeticException" || text.startsWith("!ArithmeticException:")

    private fun escaped(text: String): String = text.replace("\\", "\\\\")
        .replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n")

    // Literal source-metadata transcription, not a reconstruction of a JDK algorithm.
    // Jdk25Vectors.kt cb4d2327... lines21107–21886, original ordinals18478–19212.
    // Each line fixes one (sourceScale,divisorScale) pair; p then divisor vary fastest.
    // R/U/O expand ONLY to the three complete exception strings above. Every value is literal.
    private fun frozenTokens(): List<String> = """
        1 0.5 0.3 1 0.5 0.33 1 0.5 0.3333333 R R R R R R
        1E+1 5 3 1E+1 5 3.3 1E+1 5 3.333333 R R R R R R
        1E+2147483638 5E+2147483637 3E+2147483637 1E+2147483638 5E+2147483637 3.3E+2147483637 1E+2147483638 5E+2147483637 3.333333E+2147483637 R R R R R R
        1E+2147483648 5E+2147483647 3E+2147483647 1E+2147483648 5E+2147483647 3.3E+2147483647 1E+2147483648 5E+2147483647 3.333333E+2147483647 R R R R R R
        U U U U U U U U U R R R R R R
        U U U U U U U U U R R R R R R
        U U U U U U U U U U U U R R R
        0.1 0.05 0.03 0.1 0.05 0.033 0.1 0.05 0.03333333 R R R O O O
        1 0.5 0.3 1 0.5 0.33 1 0.5 0.3333333 R R R R R R
        1E+2147483637 5E+2147483636 3E+2147483636 1E+2147483637 5E+2147483636 3.3E+2147483636 1E+2147483637 5E+2147483636 3.333333E+2147483636 R R R R R R
        1E+2147483647 5E+2147483646 3E+2147483646 1E+2147483647 5E+2147483646 3.3E+2147483646 1E+2147483647 5E+2147483646 3.333333E+2147483646 R R R R R R
        U U U U U U U U U R R R R R R
        U U U U U U U U U R R R R R R
        U U U U U U U U U R R R R R R
        1E-2147483638 5E-2147483639 3E-2147483639 1E-2147483638 5E-2147483639 3.3E-2147483639 1E-2147483638 5E-2147483639 3.333333E-2147483639 O O O O O O
        1E-2147483637 5E-2147483638 3E-2147483638 1E-2147483637 5E-2147483638 3.3E-2147483638 1E-2147483637 5E-2147483638 3.333333E-2147483638 O O O O O O
        1 0.5 0.3 1 0.5 0.33 1 0.5 0.3333333 R R R R R R
        1E+10 5E+9 3E+9 1E+10 5E+9 3.3E+9 1E+10 5E+9 3.333333E+9 R R R R R R
        1E+20 5E+19 3E+19 1E+20 5E+19 3.3E+19 1E+20 5E+19 3.333333E+19 R R R R R R
        U U U U U U U U U R R R R R R
        U U U U U U U U U R R R R R R
        O O O O O O O O O O O O O O O
        O O O O O O O O O O O O O O O
        1E-10 5E-11 3E-11 1E-10 5E-11 3.3E-11 1E-10 5E-11 3.333333E-11 O O O O O O
        1 0.5 0.3 1 0.5 0.33 1 0.5 0.3333333 R R R R R R
        1E+10 5E+9 3E+9 1E+10 5E+9 3.3E+9 1E+10 5E+9 3.333333E+9 R R R R R R
        1E+2147483646 5E+2147483645 3E+2147483645 1E+2147483646 5E+2147483645 3.3E+2147483645 1E+2147483646 5E+2147483645 3.333333E+2147483645 R R R R R R
        1E+2147483647 5E+2147483646 3E+2147483646 1E+2147483647 5E+2147483646 3.3E+2147483646 1E+2147483647 5E+2147483646 3.333333E+2147483646 R R R R R R
        O O O O O O O O O U U U U U U
        O O O O O O O O O U U U U U U
        1E-20 5E-21 3E-21 1E-20 5E-21 3.3E-21 1E-20 5E-21 3.333333E-21 O O O O O O
        1E-10 5E-11 3E-11 1E-10 5E-11 3.3E-11 1E-10 5E-11 3.333333E-11 O O O O O O
        1 0.5 0.3 1 0.5 0.33 1 0.5 0.3333333 R R R R R R
        1E+2147483636 5E+2147483635 3E+2147483635 1E+2147483636 5E+2147483635 3.3E+2147483635 1E+2147483636 5E+2147483635 3.333333E+2147483635 R R R R R R
        1E+2147483637 5E+2147483636 3E+2147483636 1E+2147483637 5E+2147483636 3.3E+2147483636 1E+2147483637 5E+2147483636 3.333333E+2147483636 R R R R R R
        O O O O O O U U U U U U U U U
        O O O O O O U U U U U U U U U
        O O O O O O O O O U U U U U U
        1E-2147483646 5E-2147483647 3E-2147483647 O O O O O O O O O O O O
        1E-2147483636 5E-2147483637 3E-2147483637 1E-2147483636 5E-2147483637 3.3E-2147483637 1E-2147483636 5E-2147483637 3.333333E-2147483637 O O O O O O
        1 0.5 0.3 1 0.5 0.33 1 0.5 0.3333333 R R R R R R
        1E+1 5 3 1E+1 5 3.3 1E+1 5 3.333333 R R R R R R
        O O O U U U U U U U U U U U U
        O O O O O O U U U U U U U U U
        O O O O O O O O O U U U U U U
        O O O O O O O O O O O O O O O
        1E-2147483637 5E-2147483638 3E-2147483638 1E-2147483637 5E-2147483638 3.3E-2147483638 1E-2147483637 5E-2147483638 3.333333E-2147483638 O O O O O O
        0.1 0.05 0.03 0.1 0.05 0.033 0.1 0.05 0.03333333 R R R O O O
        1 0.5 0.3 1 0.5 0.33 1 0.5 0.3333333 R R R R R R
    """.trimIndent().lines().also { lines ->
        assertEquals(49, lines.size, "seven by seven literal frozen scale-pair rows")
        lines.forEach { assertEquals(15, it.split(' ').size, "five precisions by three divisors per frozen row") }
    }.flatMap { it.split(' ') }
}

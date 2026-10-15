package io.heapy.krogu.math

import kotlin.test.*

class PortableOracleTest {
    private val directZeroDecimalKeys = setOf(
        listOf("D", "round", "0.00", "0", "5", "UP"),
        listOf("D", "round", "0E+20", "0", "9", "HALF_EVEN"),
        listOf("D", "round", "0E-8", "0", "10", "UP"),
        listOf("D", "round", "0E-9", "0", "11", "HALF_DOWN"),
        listOf("D", "round", "0E-10", "0", "17", "HALF_DOWN"),
    )
    @Test fun jdk25BehaviorOnEveryPlatform() {
        val powKeys = buildSet {
            for (precision in listOf(Int.MAX_VALUE, Int.MAX_VALUE - 1, Int.MAX_VALUE - 2, Int.MAX_VALUE - 10))
                for (power in listOf(0, 1, 10, 999999999)) for (mode in RoundingMode.entries)
                    add(listOf("D", "pow", "1", power.toString(), precision.toString(), mode.name))
        }
        val observedPowKeys = mutableSetOf<List<String>>()
        val truthfulScalePrefixes = mapOf(
            listOf("T", "integer", "1", "2147483647", "0", "HALF_UP") to "integerExact;intScale;",
            listOf("T", "scale", "1", "2147483647", "0", "HALF_UP") to "scale;intScale;",
            listOf("T", "enumScale", "1", "2147483647", "0", "HALF_UP") to "enumScale;intScale;",
            listOf("T", "intScale", "1", "2147483647", "0", "HALF_UP") to "intScale;",
        )
        val observedTruthfulScaleKeys = mutableSetOf<List<String>>()
        val observedDirectZeroKeys = mutableSetOf<List<String>>()
        val directZeroCounts = mutableMapOf<String, Int>()
        val observedFiniteKeys = mutableSetOf<List<String>>()
        val unaryNullRows = linkedMapOf<Int, List<String>>()
        var ordinaryMismatchCount = 0
        val jvmBinaryRows = mutableSetOf<Int>()
        val ordinaryMismatchExamples = mutableListOf<String>()
        val movedComparisons = mutableMapOf<Int, List<String>>()
        val commonComparisons = mutableMapOf<Int, List<String>>()
        val rngRows = mutableSetOf<List<String>>()
        val rngOrdinals = mutableListOf<Int>()
        for ((index, v) in oracleVectors().withIndex()) {
            val selectedGroups = (if (ComparisonRows574.isMoved(v)) 1 else 0) +
                (if (DecimalBinary575Rows.nd(v)) 1 else 0) + (if (UnaryNull576Rows.selected(v)) 1 else 0) +
                (if (v[0] == "R") 1 else 0)
            assertTrue(selectedGroups <= 1, "one JVM counterpart group per original oracle execution $index")
            if (ComparisonRows574.isMoved(v)) {
                assertNull(movedComparisons.put(index, v.take(6)), "duplicate comparison ordinal $index")
                assertEquals(ComparisonRows574.moved[index], v.take(6), "JVM comparison counterpart $index")
                // Account for the exact input before evaluation, without synthesizing an outcome.
                // ComparisonNull574JvmTest executes this row through a real Java boundary.
                println("574_MOVED_TO_JVM\tindex=$index\t${v.take(6).joinToString("\t")}\tfrozen=${v[6]}")
                continue
            }
            if (ComparisonRows574.isComparison(v)) commonComparisons[index] = v.take(6)
            if (DecimalBinary575Rows.nd(v)) {
                assertTrue(jvmBinaryRows.add(index))
                continue // Exact ordinal/key/frozen observation replayed by DecimalBinary575JvmTest.
            }
            if (UnaryNull576Rows.selected(v)) {
                assertNull(unaryNullRows.put(index, v.take(6)), "duplicate unary ordinal $index")
                continue // Exact counterpart: UnaryNull576VectorsTest, using real Java calls.
            }
            if (v[0] == "R") {
                assertTrue(rngRows.add(v.take(6)), "duplicate RNG key at $index")
                rngOrdinals += index
                continue // Exact 25-row JVM counterpart: Validation578RngTest.
            }
            val actual = try {
                val result = evaluate(v)
                if (result is Array<*>) result.joinToString("|") else result.toString()
            } catch (e: Exception) { "!${e::class.simpleName}" }
            val truthfulPrefix = truthfulScalePrefixes[v.take(6)]
            if (truthfulPrefix != null) {
                assertTrue(observedTruthfulScaleKeys.add(v.take(6)), "duplicate task566 truthful key ${v.take(6)}")
                verifyTruthfulScaleObservation(v, actual, truthfulPrefix)
                continue
            }
            if (verifyHighMultiplyObservation(v, actual)) continue
            if (DecimalExactRoot563Callbacks.category(v.toList()) != null) {
                // Only the granted selected internal callbacks move to observations.
                // Keep every old field/index, genuine probe fields and truthful arithmetic controls.
                DecimalExactRoot563Callbacks.retain(v, index)
                DecimalExactRoot563Callbacks.verifyProbeFields(v.toList(), actual)
                DecimalExactRoot563Callbacks.verifyControls(v.toList())
                continue
            }
            val normalization = normalizationContractExpected(v)
            val pow = if (v[0] == "D" && v[1] == "pow" && v[2] == "1" && v.take(6) in powKeys) {
                assertTrue(observedPowKeys.add(v.take(6)), "duplicate retained pow key ${v.take(6)}")
                // Preserve the pinned JDK observation separately from exact 1^n = 1.
                val overflow = v[3] != "0" && v[4].toLong() + v[3].length + 1 > Int.MAX_VALUE
                assertEquals(if (overflow) "!IllegalArgumentException" else "1", v[6],
                    "retained pow diagnostic ${v.take(6)}")
                "1"
            } else null
            val scaleRounding = scaleRoundingContractExpected(v)
            if (scaleRounding != null) {
                assertTrue(v[6] == "!ArithmeticException" || v[6].startsWith("!ArithmeticException:") || v[6] == scaleRounding,
                    "retained task566 diagnostic ${v.take(6)}: ${v[6]}")
            }
            val directZero = directZeroContractExpected(v)
            if (directZero != null) {
                assertTrue(observedDirectZeroKeys.add(v.take(6)), "duplicate task567 key ${v.take(6)}")
                directZeroCounts[v[0]] = (directZeroCounts[v[0]] ?: 0) + 1
                println("567_DIRECT_ZERO_OBSERVATION\tindex=$index\t${v.take(6).joinToString("\t")}\tfrozen=${v[6]}\tactual=$actual")
            }
            val finite = FiniteZeroPolicy567.required(v.take(6), v[6])
            if (finite != null) {
                assertTrue(observedFiniteKeys.add(v.take(6)), "duplicate finite task567 key ${v.take(6)}")
                println("567_FINITE_OBSERVATION\tindex=$index\t${v.take(6).joinToString("\t")}\tfrozen=${v[6]}\tactual=$actual")
            }
            val corrected = finite ?: directZero ?: pow ?: scaleRounding ?: normalization ?: scaledDivisionContractExpected(v)
            if (normalization != null)
                assertTrue(v[6].startsWith("!ArithmeticException"), "retained normalization diagnostic ${v.take(6)}")
            val normalized = if (corrected == "!ArithmeticException" && actual.startsWith("!ArithmeticException:"))
                "!ArithmeticException" else actual
            val required = corrected ?: v[6]
            if (required != normalized) {
                ordinaryMismatchCount++
                if (ordinaryMismatchExamples.size < 80)
                    ordinaryMismatchExamples += "index=$index ${v.take(6)} required=$required actual=$actual JDK diagnostic=${v[6]}"
            }
        }
        assertEquals(128, powKeys.size)
        assertEquals(ComparisonRows574.moved, movedComparisons, "every displaced comparison has a JVM counterpart")
        assertEquals(ComparisonRows574.common, commonComparisons, "every valid comparison still executes in common")
        assertEquals(mapOf("IM" to 114, "MM" to 72), movedComparisons.values.groupingBy { it[0] }.eachCount())
        assertEquals(mapOf("IM" to 384, "MM" to 132), commonComparisons.values.groupingBy { it[0] }.eachCount())
        assertEquals(180, jvmBinaryRows.size, "ND rows assigned to real Java boundary replay")
        assertEquals(DecimalBinary575Rows.ndOrdinals, jvmBinaryRows)
        assertEquals(478, movedComparisons.size + jvmBinaryRows.size + unaryNullRows.size,
            "186 comparison + 180 binary + 112 unary original oracle executions move exactly once")
        Validation578RngRows.assertPartition(rngRows, rngOrdinals)
        assertEquals(503, movedComparisons.size + jvmBinaryRows.size + unaryNullRows.size + rngRows.size,
            "all 478 G1–G4 and 25 RNG original oracle executions move exactly once")
        assertEquals(powKeys, observedPowKeys, "all original pow boundary observations remain represented")
        assertEquals(4, truthfulScalePrefixes.size)
        assertEquals(truthfulScalePrefixes.keys, observedTruthfulScaleKeys, "all four task566 truthful observations remain represented")
        assertEquals(mapOf("D" to 5, "L" to 160, "S" to 48, "C" to 6), directZeroCounts,
            "only the finite task567 direct-zero domain is adapted")
        assertEquals(180, FiniteZeroPolicy567.roundKeys.size)
        assertEquals(12, FiniteZeroPolicy567.directUnaryKeys.size)
        assertEquals(12, FiniteZeroPolicy567.absKeys.size)
        assertEquals(8, FiniteZeroPolicy567.nullAbsKeys.size)
        assertEquals(UnaryNull576Rows.expected, unaryNullRows, "exact JVM unary partition, including original ordinals")
        assertEquals(FiniteZeroPolicy567.keys - FiniteZeroPolicy567.nullAbsKeys, observedFiniteKeys,
            "valid finite inputs remain common; all eight nullAbs keys execute in UnaryNull576VectorsTest")
        assertEquals(0, ordinaryMismatchCount, "ordinary oracle mismatches (first80):\n${ordinaryMismatchExamples.joinToString("\n")}")
    }

    private fun directZeroContractExpected(v: Array<String>): String? {
        // These literal inputs prove stored zero independently of virtual signum.
        // Preserve full host observations, including old self/base flags and traces.
        if (v[0] == "D" && v.take(6) in directZeroDecimalKeys) {
            assertEquals(v[2], v[6], "retained complete D zero diagnostic ${v.take(6)}")
            return "0"
        }
        if (v[0] == "L" && v[1] == "round" && v[2] == "0" &&
            v[3].toInt() in listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 2, Int.MIN_VALUE + 3, Int.MAX_VALUE) &&
            v[4].toInt() in listOf(0, 1, 2, 7) && v[5] in RoundingMode.entries.map { it.name }) {
            assertEquals("0E${if (v[3].toInt() < 0) "+" else "-"}${kotlin.math.abs(v[3].toLong())}", v[6],
                "retained complete L zero diagnostic ${v.take(6)}")
            return "0" // Deliberately excludes L/constructor.
        }
        if (v[0] == "S" && v[1] in listOf("super", "sequence") && v[2] == "0.00" && v[3] == "1" &&
            v[4].toInt() in listOf(0, 1, 7) && v[5] in RoundingMode.entries.map { it.name }) {
            val trace = if (v[4] == "0") "plus;" else "plus;precision;"
            assertEquals("0.00|true|false|$trace", v[6], "retained complete truthful S diagnostic ${v.take(6)}")
            // Scale2 cannot remain self after becoming scale0; only the base ZERO
            // selected by inherited plus supplies this representation (no class override).
            return "0|false|true|$trace"
        }
        if (v[0] == "C" && v[1] == "plusContext:0" && v[2] == "0.00" && v[3] == "0" &&
            v[4].toInt() in listOf(0, 1, 7) && v[5] in listOf("HALF_EVEN", "UNNECESSARY")) {
            assertEquals("0.00|true|false|plusContext;", v[6], "retained complete truthful C diagnostic ${v.take(6)}")
            return "0|false|true|plusContext;"
        }
        return null
    }

    private fun verifyHighMultiplyObservation(v: Array<String>, actual: String): Boolean {
        if (v[0] != "L" || v[1] !in listOf("multiply:-1", "multiply:0", "multiply:1") || v[4].toInt() <= 0) return false
        fun stored(text: String) = BigDecimal(BigInteger(text.substringBefore('@')), text.substringAfter('@').toInt())
        val required = DecimalScaleRounding566Expected.multiplication(stored(v[2]), stored(v[3]),
            MathContext(v[4].toInt(), RoundingMode.valueOf(v[5]))) ?: return false
        assertEquals("!ArithmeticException:Underflow|context;", v[6], "retained full task566 HIGH L diagnostic ${v.take(6)}")
        println("566_HIGH_L_OBSERVATION\t${v.take(6).joinToString("\t")}\tfrozen=${v[6]}\tactual=$actual")
        val fields = actual.split('|')
        if (required == "!ArithmeticException") {
            assertEquals(2, fields.size, "HIGH L error outcome/trace ${v.take(6)}: $actual")
            val category = if (fields[0].startsWith("!ArithmeticException:")) "!ArithmeticException" else fields[0]
            assertEquals(required, category, "independent HIGH L category ${v.take(6)}: $actual")
            assertEquals("context;", fields[1], "exact HIGH L dispatch ${v.take(6)}: $actual")
        } else {
            assertEquals(listOf(required, "false", "true", "context;"), fields,
                "independent HIGH L value, distinct/base flags and exact dispatch ${v.take(6)}: $actual")
        }
        return true
    }

    private fun verifyTruthfulScaleObservation(v: Array<String>, actual: String, wrapperPrefix: String) {
        val frozen = "!ArithmeticException:BigInteger would overflow supported range|${wrapperPrefix}sign;"
        assertEquals(frozen, v[6], "retained complete task566 truthful diagnostic ${v.take(6)}")
        println("566_TRUTHFUL_SCALE_OBSERVATION\t${v.take(6).joinToString("\t")}\tfrozen=${v[6]}\tactual=$actual")
        // These four T receivers only trace and forward their getters to super.
        // Model the stored 1@MAX independently; do not use a callback answer as an oracle.
        val mode = if (v[1] == "integer" || v[1] == "scale") RoundingMode.UNNECESSARY else RoundingMode.HALF_UP
        val expected = assertNotNull(DecimalScaleRounding566Expected.setScale(
            BigDecimal(BigInteger.ONE, Int.MAX_VALUE), 0, mode))
        val fields = actual.split('|')
        assertEquals(2, fields.size, "complete outcome and trace ${v.take(6)}: $actual")
        val outcome = if (fields[0].startsWith("!ArithmeticException:")) "!ArithmeticException" else fields[0]
        assertEquals(expected, outcome, "independent task566 truthful result ${v.take(6)}; actual=$actual")
        assertTrue(fields[1].startsWith(wrapperPrefix), "explicit wrapper dispatch ${v.take(6)}: $actual")
        val privateTrace = fields[1].removePrefix(wrapperPrefix)
        assertTrue(Regex("(?:sign;)*").matches(privateTrace),
            "only inherited sign-read multiplicity is observational ${v.take(6)}: $actual")
    }

    private fun scaleRoundingContractExpected(v: Array<String>): String? {
        val mode = if (v[0] in listOf("G", "D") && v[1] in listOf("setScale", "scale", "add", "multiply"))
            RoundingMode.valueOf(v[5]) else return null
        if (v[0] == "G" && v[1] == "setScale") {
            val a = BigDecimal(BigInteger(v[2].substringBefore('@')), v[2].substringAfter('@').toInt())
            return DecimalScaleRounding566Expected.setScale(a, v[4].toInt(), mode)
        }
        if (v[0] != "D") return null
        val a = try { BigDecimal(v[2]) } catch (_: NumberFormatException) { return null }
        if (v[1] == "scale") return DecimalScaleRounding566Expected.setScale(a, v[4].toInt(), mode)
        val b = try { BigDecimal(v[3]) } catch (_: NumberFormatException) { return null }
        val mc = MathContext(v[4].toInt().coerceAtLeast(0), mode)
        return when (v[1]) {
            "add" -> DecimalScaleRounding566Expected.addition(a, b, mc)
            "multiply" -> DecimalScaleRounding566Expected.multiplication(a, b, mc)
            else -> null
        }
    }

    private fun normalizationContractExpected(v: Array<String>): String? {
        if (v[0] !in listOf("G", "D") || v[1] !in listOf("divide", "exactDivide")) return null
        fun operand(text: String): BigDecimal = if (v[0] == "D") BigDecimal(text)
            else BigDecimal(BigInteger(text.substringBefore('@')), text.substringAfter('@').toInt())
        val mc = if (v[1] == "exactDivide") null else MathContext(v[4].toInt(), RoundingMode.valueOf(v[5]))
        val a = operand(v[2]); val b = operand(v[3])
        return DivisionNormalizationExpected.correctedText(a, b, mc) ?: PreferredScaleStringModel.correctedText(a, b, mc)
    }

    private fun scaledDivisionContractExpected(v: Array<String>): String? {
        // Only ordinary, stored-value G and parsed D operands enter this proved domain.
        // Frozen JDK observations remain intact, including their error wording.
        val stored = v[0] == "G" && v[1] == "scaledDivide"
        val parsed = v[0] == "D" && v[1] == "divideScale"
        if (!stored && !parsed) return null
        fun operand(text: String): BigDecimal {
            if (parsed) return BigDecimal(text)
            val parts = text.split('@')
            return BigDecimal(BigInteger(parts[0]), parts[1].toInt())
        }
        val a = operand(v[2])
        val b = operand(v[3])
        if (b.signum() == 0) return null
        val target = v[4].toInt()
        return when {
            DecimalScaleContractExpected.affectedScaledDivision(a, b, target) ->
                DecimalScaleContractExpected.scaledDivide(a, b, target, RoundingMode.valueOf(v[5]))
            DecimalScaleContractExpected.exceedsSupportedCoefficientRange(a, b, target) -> "!ArithmeticException"
            else -> null
        }
    }
    @Suppress("DEPRECATION")
    private fun evaluate(v: Array<String>): Any? {
        val op = v[1]
        val p = v[4].toInt()
        if (v[0] == "MM") return evaluateMinMaxOracle(v)
        if (v[0] == "IM") return evaluateIntegerMinMaxOracle(v)
        if (v[0] == "ID") return evaluateIntegerDivisionOracle(v)
        if (v[0] == "CP") return evaluateComparisonOracle(v)
        if (v[0] == "ND") return evaluateNullableBinaryOracle(v)
        if (v[0] == "O") return evaluateAdditionOracle(v)
        if (v[0] == "C") return evaluateUnaryOracle(v)
        check(v[0] != "K") { "K rows execute in UnaryNull576VectorsTest" }
        if (v[0] == "S") {
            val trace = StringBuilder()
            val value = object : BigDecimal(v[2]) {
                private var calls = 0
                override fun precision(): Int { trace.append("precision;"); return if (op == "sequence" && calls++ > 0) 1 else v[3].toInt() }
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun plus(mc: MathContext): BigDecimal {
                    trace.append("plus;")
                    return when (op) { "replace" -> BigDecimal("42.00"); "throw" -> throw IllegalStateException("plus callback"); else -> super.plus(mc) }
                }
            }
            val result = try {
                val rounded = value.round(MathContext(p, RoundingMode.valueOf(v[5])))
                "${BigDecimal(rounded.unscaledValue(), rounded.scale())}|${rounded === value}|${rounded::class == BigDecimal::class}"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "L") {
            if (op == "round" || op == "constructor") return try {
                val context = MathContext(p, RoundingMode.valueOf(v[5]))
                (if (op == "constructor") BigDecimal(BigInteger(v[2]), v[3].toInt(), context)
                    else BigDecimal(BigInteger(v[2]), v[3].toInt()).round(context)).toString()
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            val sign = op.substringAfter(':').toInt()
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2].substringBefore('@')), v[2].substringAfter('@').toInt()) {
                override fun signum(): Int { trace.append("sign;"); return sign }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun multiply(multiplicand: BigDecimal): BigDecimal { trace.append("exact;"); return super.multiply(multiplicand) }
                override fun multiply(multiplicand: BigDecimal, mc: MathContext): BigDecimal { trace.append("context;"); return super.multiply(multiplicand, mc) }
            }
            val other = BigDecimal(BigInteger(v[3].substringBefore('@')), v[3].substringAfter('@').toInt())
            val result = try {
                val product = if (p < 0) value.multiply(other) else value.multiply(other, MathContext(p, RoundingMode.valueOf(v[5])))
                "$product|${product === value}|${product::class == BigDecimal::class}"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "V") {
            val operation = op.substringBefore(':')
            val distance = op.substringAfter(':').toInt()
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return p }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun movePointLeft(n: Int): BigDecimal { trace.append("left;"); return super.movePointLeft(n) }
                override fun movePointRight(n: Int): BigDecimal { trace.append("right;"); return super.movePointRight(n) }
                override fun scaleByPowerOfTen(n: Int): BigDecimal { trace.append("scaleBy;"); return super.scaleByPowerOfTen(n) }
            }
            val result = try {
                val moved = when (operation) { "left" -> value.movePointLeft(distance); "right" -> value.movePointRight(distance); else -> value.scaleByPowerOfTen(distance) }
                val text = BigDecimal(moved.unscaledValue(), moved.scale()).toString()
                "$text|${moved === value}|${moved::class == BigDecimal::class}"
            } catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "Q") {
            if (op == "integer") return try { BigInteger(v[2]).pow(p).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun pow(n: Int): BigDecimal { trace.append("pow;"); return super.pow(n) }
            }
            val result = try { (if (op == "exact") value.pow(p) else value.pow(p, MathContext.UNLIMITED)).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "A") {
            val trace = StringBuilder()
            val normalized = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun unscaledValue(): BigInteger { trace.append("unscaled;"); return super.unscaledValue() }
            }
            val value = object : BigDecimal(BigInteger("4"), v[3].toInt()) {
                override fun stripTrailingZeros(): BigDecimal = normalized
            }
            val result = try { value.sqrt(MathContext(p, RoundingMode.valueOf(v[5]))).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "N") {
            val trace = StringBuilder()
            val normalized = object : BigDecimal(v[2]) {
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun precision(): Int { trace.append("precision;"); return super.precision() }
                override fun scale(): Int { trace.append("scale;"); return super.scale() }
                private var calls = 0
                override fun unscaledValue(): BigInteger {
                    trace.append("unscaled;")
                    if (calls++ == 0 && v[3] != "~") {
                        if (v[3] == "throw") throw IllegalStateException("first coefficient")
                        return BigInteger(v[3])
                    }
                    return super.unscaledValue()
                }
            }
            val value = object : BigDecimal("4.00") {
                override fun stripTrailingZeros(): BigDecimal = normalized
            }
            val result = try { value.sqrt(MathContext(p, RoundingMode.valueOf(v[5]))).toString() }
                catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
            return "$result|$trace"
        }
        if (v[0] == "H") {
            val trace = StringBuilder()
            val value = object : BigDecimal(v[2]) {
                override fun signum(): Int { trace.append("sign;"); return if (op == "sqrtSign") v[3].toInt() else super.signum() }
                override fun precision(): Int { trace.append("precision;"); return if (op == "sqrtPrecision") v[3].toInt() else super.precision() }
                override fun scale(): Int { trace.append("scale;"); return super.scale() }
                override fun stripTrailingZeros(): BigDecimal {
                    if (op == "sqrtNormalize") {
                        trace.append("strip;")
                        if (v[3] != "~") return BigDecimal(v[3])
                    }
                    return super.stripTrailingZeros()
                }
                override fun scaleByPowerOfTen(n: Int): BigDecimal {
                    if (op == "sqrtNormalize") trace.append("scaleBy:$n;")
                    return super.scaleByPowerOfTen(n)
                }
                override fun toDouble(): Double {
                    if (op == "sqrtNormalize") trace.append("double;")
                    return super.toDouble()
                }
            }
            val context = MathContext(p, RoundingMode.valueOf(v[5]))
            val result = try {
                when (op) {
                    "sqrt", "sqrtSign", "sqrtPrecision", "sqrtNormalize" -> value.sqrt(context)
                    "pow" -> value.pow(v[3].toInt())
                    else -> value.pow(v[3].toInt(), context)
                }.toString()
            } catch (e: Exception) {
                if (op == "sqrtSign" || op == "sqrtPrecision" || op == "sqrtNormalize") "!${e::class.simpleName}:${e.message}"
                else if (e is ArithmeticException) "!${e.message}" else throw e
            }
            return "$result|$trace"
        }
        if (v[0] == "Z") {
            var observedSign = p
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return observedSign }
                override fun precision(): Int { trace.append("precision;"); return 1 }
            }
            fun format(): String = when (op) {
                "plain" -> value.toPlainString()
                "engineering" -> value.toEngineeringString()
                else -> value.toString()
            }
            val first = format()
            observedSign = 1
            return "$first|${format()}|$trace"
        }
        if (v[0] == "Y") {
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return p }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun toPlainString(): String { trace.append("plain;"); return super.toPlainString() }
            }
            val result = if (op == "plain") value.toPlainString() else value.toEngineeringString()
            return "$result|$trace"
        }
        if (v[0] == "W") {
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return p }
                override fun precision(): Int { trace.append("precision;"); return 1 }
                override fun toPlainString(): String { trace.append("plain;"); return "custom" }
            }
            val result = value.toString()
            return "$result|$trace"
        }
        if (v[0] == "T" || v[0] == "U") {
            val trace = StringBuilder()
            val value = if (v[0] == "U") object : BigDecimal(v[2]) {
                override fun longValueExact(): Long { trace.append("longExact;"); return v[3].toLong() }
            } else object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun toLong(): Long { trace.append("long;"); return super.toLong() }
                override fun longValueExact(): Long { trace.append("longExact;"); return super.longValueExact() }
                override fun toBigIntegerExact(): BigInteger { trace.append("integerExact;"); return super.toBigIntegerExact() }
                override fun precision(): Int { trace.append("precision;"); return super.precision() }
                override fun signum(): Int { trace.append("sign;"); return super.signum() }
                override fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal { trace.append("enumScale;"); return super.setScale(newScale, roundingMode) }
                override fun setScale(newScale: Int, roundingMode: Int): BigDecimal { trace.append("intScale;"); return super.setScale(newScale, roundingMode) }
                override fun setScale(newScale: Int): BigDecimal { trace.append("scale;"); return super.setScale(newScale) }
            }
            val result = try { when (op) {
                "byte" -> value.byteValueExact()
                "short" -> value.shortValueExact()
                "int" -> value.intValueExact()
                "long" -> value.longValueExact()
                "integer" -> value.toBigIntegerExact()
                "text" -> value.toString()
                "scale" -> value.setScale(p)
                "enumScale" -> value.setScale(p, RoundingMode.valueOf(v[5]))
                "intScale" -> value.setScale(p, RoundingMode.valueOf(v[5]).ordinal)
                else -> error("Unknown exact decimal operation $op")
            }.toString() } catch (failure: Exception) { "!${failure::class.simpleName}:${failure.message}" }
            return "$result|$trace"
        }
        if (v[0] == "X") {
            val trace = StringBuilder()
            val value = object : BigDecimal(BigInteger(v[2]), v[3].toInt()) {
                override fun signum(): Int { trace.append("sign;"); return p }
                override fun precision(): Int { trace.append("precision;"); return 1 }
            }
            val bits = if (op == "float") value.toFloat().toBits().toLong() else value.toDouble().toBits()
            return "$bits|$trace"
        }
        if (v[0] == "E") {
            val trace = StringBuilder()
            val value = object : BigInteger(v[2]) {
                override fun bitLength(): Int { trace.append("bits;"); return v[3].toInt() }
                override fun toInt(): Int { trace.append("int;"); return 44 }
                override fun toLong(): Long { trace.append("long;"); return 45L }
                override fun toByte(): Byte { trace.append("byte;"); return 42 }
                override fun toShort(): Short { trace.append("short;"); return 43 }
            }
            val result = try { when (op) {
                "intExact" -> value.intValueExact()
                "longExact" -> value.longValueExact()
                "shortExact" -> value.shortValueExact()
                "byteExact" -> value.byteValueExact()
                else -> error("Unknown exact conversion $op")
            }.toString() } catch (e: ArithmeticException) {
                assertNull(e.cause)
                "!${e::class.simpleName}:${e.message}"
            }
            return "$result|$trace"
        }
        if (v[0] == "P") {
            val value = object : BigInteger(v[2]) { override fun toLong(): Long = v[3].toLong() }
            return "${value.toInt()}|${value.toDouble().toBits()}|${value.toFloat().toBits()}"
        }
        if (v[0] == "B") {
            val bytes = if (v[2].isEmpty()) byteArrayOf() else v[2].split(',').map { it.toByte() }.toByteArray()
            return if (op == "twos") BigInteger(bytes, p, v[3].toInt()) else BigInteger(v[5].toInt(), bytes, p, v[3].toInt())
        }
        val mode = RoundingMode.valueOf(v[5])
        if (v[0] == "G") {
            fun operand(text: String): BigDecimal {
                val pieces = text.split('@')
                return BigDecimal(BigInteger(pieces[0]), pieces[1].toInt())
            }
            return try {
                val a = operand(v[2]); val b = operand(v[3])
                when (op) {
                    "scaledDivide" -> a.divide(b, p, mode).toString()
                    "setScale" -> a.setScale(p, mode).toString()
                    "byteExact" -> a.byteValueExact().toString()
                    "shortExact" -> a.shortValueExact().toString()
                    "intExact" -> a.intValueExact().toString()
                    "longExact" -> a.longValueExact().toString()
                    else -> {
                        val mc = MathContext(p, mode)
                        when (op) {
                            "divide" -> a.divide(b, mc)
                            "exactDivide" -> a.divide(b)
                            "integral" -> a.divideToIntegralValue(b)
                            "integralContext" -> a.divideToIntegralValue(b, mc)
                            "remainder" -> a.remainder(b)
                            "remainderContext" -> a.remainder(b, mc)
                            "pair" -> a.divideAndRemainder(b)
                            "pairContext" -> a.divideAndRemainder(b, mc)
                            else -> error("Unknown decimal message operation $op")
                        }.toString()
                    }
                }
            }
                catch (failure: Exception) { "!${failure::class.simpleName}:${failure.message}" }
        }
        if (v[0] == "F") {
            val d = Double.fromBits(v[2].toLong())
            return if (op == "construct") BigDecimal(d) else BigDecimal.valueOf(d)
        }
        if (v[0] == "I") {
            val a = BigInteger(v[2]); val b = BigInteger(v[3])
            return when (op) {
                "parse" -> a
                "add" -> a.add(b)
                "subtract" -> a.subtract(b)
                "multiply" -> a.multiply(b)
                "divide" -> a.divideAndRemainder(b)
                "gcd" -> a.gcd(b)
                "and" -> a.and(b)
                "or" -> a.or(b)
                "xor" -> a.xor(b)
                "andNot" -> a.andNot(b)
                "not" -> a.not()
                "bits" -> "${a.bitLength()}|${a.bitCount()}|${a.getLowestSetBit()}"
                "testBit" -> a.testBit(p)
                "setBit" -> a.setBit(p)
                "clearBit" -> a.clearBit(p)
                "flipBit" -> a.flipBit(p)
                "shift" -> a.shiftLeft(p)
                "shiftRight" -> a.shiftRight(p)
                "sqrt" -> a.sqrtAndRemainder()
                "double" -> a.toDouble().toBits()
                "float" -> a.toFloat().toBits()
                "bytes" -> a.toByteArray().joinToString(",")
                "longExact" -> a.longValueExact()
                "intExact" -> a.intValueExact()
                "shortExact" -> a.shortValueExact()
                "byteExact" -> a.byteValueExact()
                "radix" -> a.toString(p)
                "pow" -> a.pow(p)
                else -> error("Unknown integer operation $op")
            }
        }
        val a = BigDecimal(v[2]); val b = BigDecimal(v[3])
        val mc = MathContext(p.coerceAtLeast(0), mode)
        return when (op) {
            "parse" -> a
            "plain" -> a.toPlainString()
            "engineering" -> a.toEngineeringString()
            "add" -> a.add(b, mc)
            "subtract" -> a.subtract(b, mc)
            "multiply" -> a.multiply(b, mc)
            "divide" -> a.divide(b, mc)
            "exactDivide" -> a.divide(b)
            "integral" -> a.divideToIntegralValue(b, mc)
            "remainder" -> a.divideAndRemainder(b, mc)
            "round" -> a.round(mc)
            "scale" -> a.setScale(p, mode)
            "divideScale" -> a.divide(b, p, mode)
            "strip" -> a.stripTrailingZeros()
            "sqrt" -> a.sqrt(mc)
            "pow" -> a.pow(v[3].toInt(), mc)
            "double" -> a.toDouble().toBits()
            "float" -> a.toFloat().toBits()
            "integerExact" -> a.toBigIntegerExact()
            else -> error("Unknown decimal operation $op")
        }
    }
    @Test fun immutableValuesAndAlgebraicInvariants() {
        val bytes = byteArrayOf(0x12, 0x34, 0x56)
        val a = BigInteger(bytes)
        bytes.fill(0)
        assertEquals("1193046", a.toString())
        val encoded = a.toByteArray(); encoded.fill(0)
        assertEquals("1193046", a.toString())
        val b = BigInteger("999999999999999999999999999999999999")
        assertEquals(a, a.add(b).subtract(b))
        assertEquals(b, b.multiply(a).divide(a))
        assertEquals(BigInteger.ONE.negate(), a.or(a.not()))
        assertEquals(BigInteger.ZERO, a.and(a.not()))
        assertEquals(BigInteger.ONE.negate(), BigInteger("-3").shiftRight(100))
        assertNotEquals(BigDecimal("1.0"), BigDecimal("1.00"))
        assertEquals(0, BigDecimal("1.0").compareTo(BigDecimal("1.00")))
        assertEquals("0.50", BigDecimal("1.00").divide(BigDecimal.TWO).toString())
        assertEquals("0.3333333", BigDecimal.ONE.divide(BigDecimal(3), MathContext.DECIMAL32).toString())
        assertFailsWith<ArithmeticException> { BigDecimal.ONE.divide(BigDecimal(3)) }
        assertFailsWith<ArithmeticException> { BigInteger.ONE.divide(BigInteger.ZERO) }
    }
    @Test fun contextsAndRoundingConstants() {
        assertEquals(34, MathContext.DECIMAL128.getPrecision())
        assertEquals(RoundingMode.HALF_EVEN, MathContext.DECIMAL128.getRoundingMode())
        assertEquals(MathContext.DECIMAL128, MathContext(MathContext.DECIMAL128.toString()))
        assertEquals(RoundingMode.HALF_EVEN, RoundingMode.valueOf(6))
        assertFailsWith<IllegalArgumentException> { MathContext(-1) }
        assertFailsWith<IllegalArgumentException> { RoundingMode.valueOf(8) }
        assertFailsWith<IllegalArgumentException> { MathContext("precision=7 roundingMode=INVALID") }
    }

    @Test fun modularArithmeticAndRandomGeneration() {
        val a = BigInteger("12345678901234567890")
        val modulus = BigInteger("1000000007")
        val inverse = a.modInverse(modulus)
        assertEquals(BigInteger.ONE, a.multiply(inverse).mod(modulus))
        assertEquals(inverse.pow(3).mod(modulus), a.modPow(BigInteger("-3"), modulus))
        assertEquals(BigInteger.ZERO, a.modPow(BigInteger.ZERO, BigInteger.ONE))
        assertFailsWith<ArithmeticException> { BigInteger.TWO.modInverse(BigInteger.TEN) }
        assertFailsWith<ArithmeticException> { a.mod(BigInteger.ZERO) }
        assertFailsWith<ArithmeticException> { a.modPow(BigInteger.ONE, -modulus) }
        val random = kotlin.random.Random(42)
        for (bits in listOf(0, 1, 2, 29, 30, 31, 63, 64, 100)) {
            val n = BigInteger(bits, random)
            assertTrue(n.signum() >= 0)
            assertTrue(n.bitLength() <= bits)
        }
        for (bits in listOf(2, 3, 8, 16, 32)) {
            val prime = BigInteger.probablePrime(bits, random)
            assertEquals(bits, prime.bitLength())
            assertTrue(prime.isProbablePrime(60))
        }
        assertEquals(BigInteger("101"), BigInteger("100").nextProbablePrime())
        assertTrue(BigInteger("2147483647").isProbablePrime(60))
        for (n in listOf("0", "1", "561", "1105", "1729", "3215031751"))
            assertFalse(BigInteger(n).isProbablePrime(60))
        assertTrue(BigInteger.ZERO.isProbablePrime(0))
    }
}

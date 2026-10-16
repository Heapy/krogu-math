// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Test-only proof checker. No decimal/integer numerical API supplies its certificate. */
internal object SqrtCohort587Comparison {
    data class Fields(val coefficient: String, val scale: Int, val precision: Int, val text: String)
    data class Input(val coefficient: String, val scale: Int, val source: String)
    data class Outcome(val text: String, val fields: Fields? = null,
        val errorType: String? = null, val errorMessage: String? = null, val unexpected: Boolean = false)
    enum class Profile { PORTABLE, COMPACT, WIDE, FIXED, LIVE }
    private data class Decimal(val digits: String, val scale: Long)
    private val scalesCompact = setOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 5,
        -1, 0, 1, Int.MAX_VALUE - 5, Int.MAX_VALUE - 1, Int.MAX_VALUE)
    private val scalesWide = setOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, Int.MIN_VALUE + 2,
        Int.MIN_VALUE + 16, Int.MIN_VALUE + 32, Int.MIN_VALUE / 2, -10, 0, 10,
        Int.MAX_VALUE / 2, Int.MAX_VALUE - 32, Int.MAX_VALUE - 16,
        Int.MAX_VALUE - 2, Int.MAX_VALUE - 1, Int.MAX_VALUE)
    private val fixed = setOf("0", "0.00", "0E+5", "0E-5", "1.0000", "4.00", "100.00", "0.01",
        "2", "3", "5", "10", "81", "0.99999999", "1.00000001", "9999", "999999", "1E-30",
        "1E+31", "10000000000000000000000000001")
    val raw = mapOf(
        13823 to listOf("D", "sqrt", "0.0000001", "0", "20", "CEILING", "0.0003162277660168379332"),
        14089 to listOf("D", "sqrt", "2.89086115118547718274465872620E-12", "0", "18", "HALF_EVEN", "0.0000017002532608954106"),
        15172 to listOf("D", "sqrt", "909393907682500.536454623858577", "0", "17", "UP", "30156158.70236958"))

    fun input(value: BigDecimal, source: String = value.toString()): Input {
        require(value::class == BigDecimal::class)
        return Input(value.unscaledValue().toString(), value.scale(), source)
    }
    fun own(action: () -> BigDecimal): Outcome = reference(action) { value ->
        Fields(value.unscaledValue().toString(), value.scale(), value.precision(), value.toString())
    }
    fun <T> reference(action: () -> T, observe: (T) -> Fields): Outcome {
        val value = try { action() } catch (e: Exception) { return failure(e) }
            catch (e: AssertionError) { return failure(e) }
        // An exception while observing a returned VALUE is not an operation AE.
        return try { val fields = observe(value); Outcome(fields.text, fields) }
            catch (e: Exception) { failure(e).copy(unexpected = true) }
            catch (e: AssertionError) { failure(e).copy(unexpected = true) }
    }
    fun captureText(action: () -> Any?): Outcome = try {
        val value = action()
        Outcome(if (value is Array<*>) value.joinToString("|") else value.toString())
    } catch (e: Exception) { failure(e) }
      catch (e: AssertionError) { failure(e) }
    private fun failure(e: Throwable): Outcome {
        val type = e::class.simpleName ?: "UnknownException"
        return Outcome("!$type", errorType = type, errorMessage = e.message,
            unexpected = e !is ArithmeticException)
    }
    fun frozen(text: String): Outcome = if (text.startsWith('!')) Outcome(text,
        errorType = text.drop(1).substringBefore(':'),
        errorMessage = text.substringAfter(':', "").ifEmpty { null },
        unexpected = text.drop(1).substringBefore(':') != "ArithmeticException") else Outcome(text, parse(text))

    /** Only canonical bounded oracle text, not the public constructor grammar. */
    fun parse(text: String): Fields = readFields(text).also(::validate)

    /** Bounded retained input spelling may differ from canonical result text. */
    fun parseInput(text: String): Input {
        val fields = readFields(text)
        require(fields.precision <= 155)
        return Input(fields.coefficient, fields.scale, text)
    }

    private fun readFields(text: String): Fields {
        require(text.length <= 200)
        val match = requireNotNull(Regex("(-?)([0-9]+)(?:\\.([0-9]+))?(?:E([+-]?[0-9]+))?").matchEntire(text)) {
            "Not canonical decimal reference text: $text"
        }
        val fraction = match.groupValues[3]
        val exponent = match.groupValues[4].ifEmpty { "0" }.toLong()
        require(exponent in -4_300_000_000L..4_300_000_000L)
        val digits = (match.groupValues[2] + fraction).trimStart('0').ifEmpty { "0" }
        val coefficient = (if (digits != "0") match.groupValues[1] else "") + digits
        val scale = fraction.length.toLong() - exponent
        require(scale in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
        return Fields(coefficient, scale.toInt(), digits.length, text)
    }
    private fun validate(value: Fields) {
        require(Regex("-?(?:0|[1-9][0-9]*)").matches(value.coefficient) && value.coefficient != "-0")
        val digits = value.coefficient.removePrefix("-")
        require(digits.length <= 155 && value.precision == digits.length)
        require(value.text == PreferredScaleStringModel.text("${value.coefficient}@${value.scale}"))
    }
    private fun positive(c: String, s: Long): Decimal {
        require(c.all { it in '0'..'9' } && c.isNotEmpty())
        return Decimal(c.trimStart('0').ifEmpty { "0" }, s)
    }
    private fun compare(a: Decimal, b: Decimal): Int {
        if (a.digits == "0" || b.digits == "0") return when {
            a.digits == b.digits -> 0; a.digits == "0" -> -1; else -> 1
        }
        val exponent = (a.digits.length.toLong() - a.scale).compareTo(b.digits.length.toLong() - b.scale)
        if (exponent != 0) return exponent
        for (i in 0 until maxOf(a.digits.length, b.digits.length)) {
            val d = (a.digits.getOrNull(i) ?: '0').compareTo(b.digits.getOrNull(i) ?: '0')
            if (d != 0) return d
        }
        return 0
    }
    private fun multiply(a: String, b: String): String {
        require(a.length <= 122 && b.length <= 122)
        val cells = IntArray(a.length + b.length)
        for (i in a.indices) for (j in b.indices)
            cells[i + j + 1] += (a[i] - '0') * (b[j] - '0')
        for (i in cells.lastIndex downTo 1) { cells[i - 1] += cells[i] / 10; cells[i] %= 10 }
        return cells.joinToString("").trimStart('0').ifEmpty { "0" }
    }
    private fun square(value: Decimal): Decimal = Decimal(multiply(value.digits, value.digits), 2L * value.scale)
    private fun plus(a: String, b: String): String {
        val out = StringBuilder(); var carry = 0
        for (i in 0 until maxOf(a.length, b.length)) {
            val part = (a.getOrNull(a.lastIndex - i)?.minus('0') ?: 0) +
                (b.getOrNull(b.lastIndex - i)?.minus('0') ?: 0) + carry
            out.append(('0'.code + part % 10).toChar()); carry = part / 10
        }
        if (carry != 0) out.append('1')
        return out.reverse().toString()
    }
    private fun minusOne(a: String): String {
        val out = a.toCharArray(); var i = out.lastIndex
        while (out[i] == '0') { out[i] = '9'; i-- }
        out[i]--
        return out.concatToString().trimStart('0').ifEmpty { "0" }
    }
    private fun midpointSign(x: Decimal, a: Decimal, b: Decimal): Int {
        val scale = maxOf(a.scale, b.scale)
        require(scale - a.scale in 0L..1L && scale - b.scale in 0L..1L)
        val sum = plus(a.digits + "0".repeat((scale - a.scale).toInt()),
            b.digits + "0".repeat((scale - b.scale).toInt()))
        val four = plus(plus(x.digits, x.digits), plus(x.digits, x.digits))
        return compare(Decimal(four, x.scale), square(Decimal(sum, scale)))
    }
    /** Exact adjacent p-digit lattice, including the asymmetric decade seam. */
    private fun certificate(x: Decimal, y: Decimal, p: Int, mode: RoundingMode): String {
        require(y.digits.length == p && y.digits != "0")
        val decade = y.digits == "1" + "0".repeat(p - 1)
        val previous = if (decade) Decimal("9".repeat(p), y.scale + 1)
            else Decimal(minusOne(y.digits), y.scale)
        val next = Decimal(plus(y.digits, "1"), y.scale)
        val center = compare(x, square(y))
        require(center != 0)
        return when (mode) {
            RoundingMode.UP, RoundingMode.CEILING -> {
                require(compare(x, square(previous)) > 0 && center < 0); "UP_ADJACENT"
            }
            RoundingMode.DOWN, RoundingMode.FLOOR -> {
                require(center > 0 && compare(x, square(next)) < 0); "DOWN_ADJACENT"
            }
            RoundingMode.UNNECESSARY -> error("Nonexact UNNECESSARY reference VALUE")
            else -> {
                val lower = midpointSign(x, previous, y)
                val upper = midpointSign(x, y, next)
                val even = (y.digits.last() - '0') % 2 == 0
                val lowerTie = when (mode) {
                    RoundingMode.HALF_UP -> true
                    RoundingMode.HALF_DOWN -> false
                    else -> decade || even // At the lower seam, 10*R is even.
                }
                val upperTie = when (mode) {
                    RoundingMode.HALF_UP -> false
                    RoundingMode.HALF_DOWN -> true
                    else -> even
                }
                require((lower > 0 || lower == 0 && lowerTie) && (upper < 0 || upper == 0 && upperTie))
                "NEAREST_ADJACENT/$lower/$upper/decade=$decade"
            }
        }
    }
    data class Decision(val required: Fields, val disposition: String, val certificate: String)
    /** Direct bounded certificate controls exercise ties even when no lifting is needed. */
    fun certify(input: Input, result: Fields, p: Int, mode: RoundingMode): String {
        validate(result)
        require(p in 1..120 && input.coefficient.length <= 155)
        require(Regex("[1-9][0-9]*").matches(input.coefficient))
        require(Regex("[1-9][0-9]*").matches(result.coefficient))
        return certificate(positive(input.coefficient, input.scale.toLong()),
            positive(result.coefficient, result.scale.toLong()), p, mode)
    }
    fun decide(input: Input, p: Int, mode: RoundingMode, reference: Fields): Decision {
        validate(reference)
        require(p in 0..120 && input.coefficient.removePrefix("-").length <= 155)
        require(Regex("-?(?:0|[1-9][0-9]*)").matches(input.coefficient) && input.coefficient != "-0")
        require(!input.coefficient.startsWith('-')) { "Negative input cannot have a sqrt VALUE" }
        if (input.coefficient == "0") {
            require(reference.coefficient == "0")
            return Decision(reference, "UNCHANGED_ZERO", "ZERO")
        }
        require(!reference.coefficient.startsWith('-') && reference.coefficient != "0")
        if (p > 0) require(reference.precision <= p)
        if (p == 0) require(reference.precision <= 78)
        if (p > 0 && mode != RoundingMode.UNNECESSARY && reference.precision == p)
            return Decision(reference, "UNCHANGED_P_DIGITS", "EXISTING_NUMERIC_REFERENCE")
        val x = positive(input.coefficient, input.scale.toLong())
        val y = positive(reference.coefficient, reference.scale.toLong())
        if (compare(square(y), x) == 0) return Decision(reference, "UNCHANGED_EXACT", "REFERENCE_SQUARE_EQUALS_INPUT")
        require(p > 0 && mode != RoundingMode.UNNECESSARY) { "Nonexact exact-only reference VALUE" }
        val padding = p - reference.precision
        require(padding > 0)
        val scale = reference.scale.toLong() + padding
        require(scale in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
        val coefficient = reference.coefficient + "0".repeat(padding)
        val proof = certificate(x, Decimal(coefficient, scale), p, mode)
        val text = PreferredScaleStringModel.text("$coefficient@$scale")
        return Decision(Fields(coefficient, scale.toInt(), p, text), "LIFT_INEXACT", proof)
    }
    private fun escape(x: Any?): String = x?.toString()?.replace("\\", "\\\\")?.replace("\t", "\\t")
        ?.replace("\r", "\\r")?.replace("\n", "\\n") ?: "\\N"
    private fun emit(tag: String, values: List<Any?>) = println(tag + "\t" + values.joinToString("\t", transform = ::escape))

    fun strictMatch(required: Outcome, actual: Outcome): Boolean = !required.unexpected && !actual.unexpected &&
        (required.fields != null || required.errorType != null) && required.text == actual.text &&
        required.fields == actual.fields && required.errorType == actual.errorType

    /** Local deferred assertions: full lines, constant retained failure state. */
    class Checks(private val name: String) {
        var comparisons = 0; private set
        private var failures = 0
        fun equal(expected: Any?, actual: Any?, label: String) {
            if (expected != actual) bad(label, "expected=$expected", "actual=$actual")
        }
        fun bad(label: String, expected: Any?, actual: Any?) {
            failures++
            emit("587_COMPARISON_FAILURE", listOf(name, label, expected, actual))
        }
        fun check(label: String, reference: () -> Any?, actual: () -> Any?) {
            comparisons++
            val r = captureText(reference); val a = captureText(actual)
            equal(r.text, a.text, label)
            if (r.unexpected || a.unexpected) bad(label, r, a)
        }
        /** Preserve assertEquals' typed comparison at former direct assertion sites. */
        fun strict(label: String, reference: () -> Any?, actual: () -> Any?) {
            comparisons++
            var r: Any? = null; var a: Any? = null
            val ro = captureText { reference().also { r = it } }
            val ao = captureText { actual().also { a = it } }
            equal(r, a, label)
            // These sites formerly allowed no operation exceptions at all.
            if (ro.errorType != null || ao.errorType != null || ro.unexpected || ao.unexpected)
                bad(label, ro, ao)
        }
        fun accountSqrt() { comparisons++ }
        fun finish(expected: Int? = null) {
            if (expected != null) assertEquals(expected, comparisons, "$name complete comparison count")
            assertEquals(0, failures, "$name failures; complete details in587_COMPARISON_FAILURE output")
        }
    }
    class Recorder(private val name: String, private val profile: Profile, private val expected: Int,
        private val referenceSource: String) {
        private val seen = mutableSetOf<String>()
        private var failures = 0
        private val dispositions = mutableMapOf<String, Int>()
        init {
            require(referenceSource in setOf("FROZEN", "LIVE_JDK"))
            emit("587_SQRT_SCHEMA", listOf("name", "profile", "key", "inputText", "inputCoefficient", "inputScale", "p", "mode",
                "referenceSource", "referenceKind", "referenceText", "referenceCoefficient", "referenceScale", "referencePrecision",
                "referenceErrorType", "referenceErrorMessage", "referenceUnexpected", "disposition", "certificate",
                "requiredKind", "requiredText", "requiredCoefficient", "requiredScale", "requiredPrecision", "requiredErrorType",
                "actualKind", "actualText", "actualCoefficient", "actualScale", "actualPrecision",
                "actualErrorType", "actualErrorMessage", "actualUnexpected", "control563Kind", "control563Text",
                "control563Coefficient", "control563Scale", "control563Precision", "control563ErrorType",
                "control563ErrorMessage", "control563Unexpected", "guardFailure", "rawTextEquality", "strictPass"))
        }
        private fun kind(outcome: Outcome?): String = when {
            outcome == null -> "UNAVAILABLE"
            outcome.errorType != null -> "ERROR"
            outcome.fields != null -> "VALUE"
            else -> "UNAVAILABLE"
        }
        private fun domain(x: Input, p: Int) {
            require(Regex("-?(?:0|[1-9][0-9]*)").matches(x.coefficient) && x.coefficient != "-0")
            val digits = x.coefficient.removePrefix("-").length
            when (profile) {
                Profile.PORTABLE -> require(digits <= 30 && p in setOf(17, 18, 20))
                Profile.COMPACT -> require(x.coefficient in setOf("-1000", "-100", "-3", "-1", "0", "1", "3", "100", "1000") &&
                    x.scale in scalesCompact && p in setOf(0, 1, 2, 7))
                Profile.WIDE -> require(digits <= 155 && x.scale in scalesWide && p in setOf(1, 2, 7, 17, 18, 19, 34, 120))
                Profile.FIXED -> require(x.source in fixed && p in setOf(0, 1, 2, 3, 7, 16))
                Profile.LIVE -> require(digits <= 46 && x.scale in -30..44 && p in 0..34)
            }
        }
        fun record(key: String, input: Input, p: Int, mode: RoundingMode, reference: Outcome,
            actual: Outcome, existing563: Fields? = null, control563: Outcome? = null) {
            assertTrue(seen.add(key), "$name duplicate source-site key $key")
            var disposition = "GUARD_FAILURE"
            var proof = ""
            var required: Outcome? = null
            var guard: String? = null
            try {
                domain(input, p)
                if (existing563 != null) {
                    require(reference.text == "!ArithmeticException")
                    validate(existing563)
                    required = Outcome(existing563.text, existing563)
                    disposition = "EXISTING_563"; proof = "EXISTING_LITERAL_POLICY"
                } else if (reference.errorType != null) {
                    required = reference; disposition = "UNCHANGED_ERROR"; proof = "EXISTING_ERROR_COMPARISON"
                } else {
                    val decision = decide(input, p, mode, requireNotNull(reference.fields))
                    required = Outcome(decision.required.text, decision.required)
                    disposition = decision.disposition; proof = decision.certificate
                }
            } catch (e: Exception) { guard = "${e::class.simpleName}:${e.message}" }
            val matches = required != null && !reference.unexpected && strictMatch(required, actual) &&
                (control563 == null || strictMatch(required, control563))
            if (!matches || guard != null) failures++
            dispositions[disposition] = (dispositions[disposition] ?: 0) + 1
            emit("587_SQRT_RECORD", listOf(name, profile, key, input.source, input.coefficient, input.scale, p, mode,
                referenceSource, kind(reference), reference.text, reference.fields?.coefficient, reference.fields?.scale, reference.fields?.precision,
                reference.errorType, reference.errorMessage, reference.unexpected, disposition, proof,
                kind(required), required?.text, required?.fields?.coefficient, required?.fields?.scale, required?.fields?.precision, required?.errorType,
                kind(actual), actual.text, actual.fields?.coefficient, actual.fields?.scale, actual.fields?.precision,
                actual.errorType, actual.errorMessage, actual.unexpected, kind(control563), control563?.text,
                control563?.fields?.coefficient, control563?.fields?.scale, control563?.fields?.precision,
                control563?.errorType, control563?.errorMessage, control563?.unexpected,
                guard, reference.text == actual.text, matches))
        }
        fun finish() {
            emit("587_SQRT_SUMMARY", listOf(name, profile, seen.size, expected, failures, dispositions))
            assertEquals(expected, seen.size, "$name complete source-site record count")
            assertEquals(0, failures, "$name sqrt policy failures; see every587_SQRT_RECORD")
        }
    }
}

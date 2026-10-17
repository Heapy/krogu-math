package preparation598

import java.math.BigDecimal as OracleDecimal
import java.math.BigInteger as OracleInteger
import java.math.RoundingMode as OracleRounding

data class PairFixture(val id: String, val left: String, val right: String)
data class FixedScaleFixture(val id: String, val left: String, val right: String, val scale: Int, val expected: String)
data class Observation(val peer: String, val id: String, val valueParity: Boolean, val outputText: String,
    val jdkText: String, val textParity: Boolean, val operandRelation: String = "binary-inputs")
data class VerificationReport(val observations: List<Observation>, val unavailable: Map<String, String>)

/** Strings are the input contract; no binary Float/Double conversions. */
object FixtureCorpus {
    val integers = listOf(
        PairFixture("zero", "0", "0"), PairFixture("unit-sign", "1", "-1"),
        PairFixture("negative", "-7", "3"), PairFixture("negative-both", "-7", "-3"),
        PairFixture("word-low", "1073741823", "1"), PairFixture("word-high", "1073741824", "1073741823"),
        PairFixture("max", "9223372036854775807", "1"), PairFixture("min", "-9223372036854775808", "-1"),
        PairFixture("positive-wide", "9223372036854775808", "9223372036854775807"),
        PairFixture("negative-wide", "-9223372036854775809", "9223372036854775808"),
        PairFixture("carry", "999999999999999999999999999999", "1"),
        PairFixture("cancel", "123456789012345678901234567890", "-123456789012345678901234567890"),
        PairFixture("wide-mixed", "123456789012345678901234567890", "-98765432109876543210987654321"),
        PairFixture("power10", "1000000000000000000000000000000", "999999999999999999999999999999"),
        PairFixture("small-mixed", "12345", "-1137"), PairFixture("unequal", "314159265358979323846", "271828182845904523536")
    )
    val decimals = listOf(
        PairFixture("zero", "0.00", "0"), PairFixture("cohort", "1.20", "3.400"),
        PairFixture("cohort-product", "1.20", "3.0"), PairFixture("cancel", "1.00", "-1.0"),
        PairFixture("negative", "-12.345", "0.0067"), PairFixture("ties", "1.25", "-1.35"),
        PairFixture("carry", "9.99", "0.01"), PairFixture("min", "-9223372036854775808", "-1"),
        PairFixture("max", "9223372036854775807", "1"), PairFixture("positive-wide", "9223372036854775808", "1.1"),
        PairFixture("tiny", "1E-40", "3E-41"), PairFixture("negative-scale", "1.23E+30", "2E+10"),
        PairFixture("wide", "123456789012345678901234567890.123456789", "11.37"),
        PairFixture("wide-negative", "-123456789012345678901234567890.123456789", "26.37"),
        PairFixture("large-scale", "123456789E-80", "2.000"), PairFixture("trailing", "1000.000", "0.0010")
    )
    val fixedScale = listOf(
        FixedScaleFixture("positive-tie", "1", "8", 2, "0.13"),
        FixedScaleFixture("negative-tie", "-1", "8", 2, "-0.13"),
        FixedScaleFixture("negative-divisor", "1", "-8", 2, "-0.13"),
        FixedScaleFixture("both-negative", "-1", "-8", 2, "0.13"),
        FixedScaleFixture("nonterminating", "1", "3", 20, "0.33333333333333333333"),
        FixedScaleFixture("carry", "9.995", "1", 2, "10.00"),
        FixedScaleFixture("zero", "0.000", "7", 4, "0.0000"),
        FixedScaleFixture("exact", "12.00", "3", 2, "4.00")
    )
}

/** Conservative observation-level gate; does not claim unexposed internal scale parity. */
private class UntimedPreservation<T : Any>(private val observe: (T) -> String) {
    private val retained = mutableListOf<Pair<T, String>>()
    fun callTwice(inputs: List<T>, operation: () -> T): T {
        val original = inputs.map { it to observe(it) }
        var previousText: String? = null
        var finalResult: T? = null
        repeat(2) {
            val before = inputs.map { observe(it) }
            check(before == original.map { it.second }) { "Input changed before repeated operation" }
            val result = operation()
            for ((input, text) in original) check(observe(input) == text) { "Input changed by operation" }
            for ((oldResult, text) in retained) check(observe(oldResult) == text) { "Retained earlier result changed" }
            val text = observe(result)
            if (previousText != null) check(text == previousText) { "Repeated result changed" }
            retained += result to text
            previousText = text
            finalResult = result
        }
        return checkNotNull(finalResult)
    }
}

/** No main, clocks, counters, loops for measuring, dependency resolution, or automatic execution. */
fun <I : Any, D : Any> verifyUntimed(adapter: PeerAdapter<I, D>): VerificationReport {
    val unavailable = linkedMapOf<String, String>()
    val observations = buildList<Observation> {
        fun decimalCheck(id: String, value: D, expected: OracleDecimal, relation: String = "binary-inputs") {
            val text = adapter.decimalText(value)
            val equal = OracleDecimal(text).compareTo(expected) == 0
            check(equal) { "${adapter.id}:$id numeric mismatch: $text vs $expected" }
            // Text equality is recorded separately. Never relabel it as internal scale equality.
            add(Observation(adapter.id, id, equal, text, expected.toString(), text == expected.toString(), relation))
        }
        for (f in FixtureCorpus.integers) {
            val a = adapter.integer(f.left); val b = adapter.integer(f.right)
            val x = OracleInteger(f.left); val y = OracleInteger(f.right)
            val preservation = UntimedPreservation<I>(adapter::integerText)
            for (op in ExactOperation.entries) {
                val value = preservation.callTwice(listOf(a, b)) { adapter.integerOperation(op, a, b) }
                val expected = JdkAdapter.integerOperation(op, x, y)
                val text = adapter.integerText(value)
                check(OracleInteger(text) == expected) { "${adapter.id}:integer/${f.id}/$op mismatch" }
                add(Observation(adapter.id, "integer/${f.id}/$op", true, text, expected.toString(), text == expected.toString()))
            }
            val square = adapter.integerText(preservation.callTwice(listOf(a, a)) { adapter.integerOperation(ExactOperation.MULTIPLY, a, a) })
            check(OracleInteger(square) == x.multiply(x)) { "${adapter.id}:integer/${f.id}/square-self mismatch" }
            add(Observation(adapter.id, "integer/${f.id}/square-self", true, square, x.multiply(x).toString(), square == x.multiply(x).toString(), "self-alias"))
            val equalInteger = adapter.integer(f.left) // Preparation, never part of future numeric timing.
            if (a === equalInteger) unavailable["integer/${f.id}/square-distinct-equal"] = "Parser interned equal inputs; cannot label this distinct-object work"
            else {
                val distinctSquare = adapter.integerText(preservation.callTwice(listOf(a, equalInteger)) { adapter.integerOperation(ExactOperation.MULTIPLY, a, equalInteger) })
                check(OracleInteger(distinctSquare) == x.multiply(x)) { "Distinct-equal square mismatch" }
                add(Observation(adapter.id, "integer/${f.id}/square-distinct-equal", true, distinctSquare,
                    x.multiply(x).toString(), distinctSquare == x.multiply(x).toString(), "distinct-equal"))
            }
            check(adapter.integerText(a) == adapter.integerText(adapter.integer(f.left))) { "Input mutated" }
            check(adapter.integerText(b) == adapter.integerText(adapter.integer(f.right))) { "Input mutated" }
        }
        for (f in FixtureCorpus.decimals) {
            val a = adapter.decimal(f.left); val b = adapter.decimal(f.right)
            val x = OracleDecimal(f.left); val y = OracleDecimal(f.right)
            val preservation = UntimedPreservation<D>(adapter::decimalText)
            for (op in ExactOperation.entries) decimalCheck("decimal/${f.id}/$op",
                preservation.callTwice(listOf(a, b)) { adapter.decimalOperation(op, a, b) }, JdkAdapter.decimalOperation(op, x, y))
            decimalCheck("decimal/${f.id}/square-self", preservation.callTwice(listOf(a, a)) { adapter.decimalOperation(ExactOperation.MULTIPLY, a, a) }, x.multiply(x), "self-alias")
            val equalDecimal = adapter.decimal(f.left)
            if (a === equalDecimal) unavailable["decimal/${f.id}/square-distinct-equal"] = "Parser interned equal inputs; distinct-object case unavailable"
            else decimalCheck("decimal/${f.id}/square-distinct-equal",
                preservation.callTwice(listOf(a, equalDecimal)) { adapter.decimalOperation(ExactOperation.MULTIPLY, a, equalDecimal) }, x.multiply(x), "distinct-equal")
            check(OracleDecimal(adapter.decimalText(a)).compareTo(x) == 0) { "Input mutated" }
            check(OracleDecimal(adapter.decimalText(b)).compareTo(y) == 0) { "Input mutated" }
        }
        val fixedScalePreservation = UntimedPreservation<D>(adapter::decimalText)
        if (adapter.fixedScaleHalfUp.eligibleAfterFixtureGate) for (f in FixtureCorpus.fixedScale) {
            val expected = OracleDecimal(f.left).divide(OracleDecimal(f.right), f.scale, OracleRounding.HALF_UP)
            check(expected == OracleDecimal(f.expected)) { "Fixture/JDK cohort disagreement:${f.id}" }
            val a = adapter.decimal(f.left); val b = adapter.decimal(f.right)
            decimalCheck("fixed-scale-half-up/${f.id}",
                fixedScalePreservation.callTwice(listOf(a, b)) { adapter.divideFixedScaleHalfUp(a, b, f.scale) }, expected)
        }
    }
    if (!adapter.fixedScaleHalfUp.eligibleAfterFixtureGate)
        unavailable["fixed-scale-half-up/*"] = adapter.fixedScaleHalfUp.reason
    unavailable["significant-half-even/MC34"] = if (adapter.significantHalfEven.eligibleAfterFixtureGate)
        "Native capability known, but this source slice has no MC34 check/operation; not verified here"
    else adapter.significantHalfEven.reason
    return VerificationReport(observations, unavailable)
}

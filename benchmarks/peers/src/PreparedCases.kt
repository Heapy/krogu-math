package preparation598

internal data class TimedCase598(
    val peer: String, val id: String, val inputLeft: String, val inputRight: String,
    val relation: String, val expected: String, val operation: () -> Any,
    val checkResult: (Any) -> String, val checkInputs: () -> Unit,
)
internal fun <I : Any, D : Any> coreCase598(adapter: PeerAdapter<I, D>, id: String): TimedCase598 {
    if(id.endsWith("/PARSE") || id.endsWith("/FORMAT")) {
        val t=textCase598(adapter,id)
        return TimedCase598(adapter.id,id,t.input,t.input,"unary-text",t.input,t.operation,t.checkResult,t.checkInput)
    }
    val parts = id.split('/'); require(parts.size == 3)
    val family = parts[0]; val fixtureId = parts[1]; val opName = parts[2]
    val f = (if (family == "int") integerFixtures598 else { require(family == "decimal"); decimalFixtures598 }).single { it.id == fixtureId }
    val op = if (opName == "self") ExactOperation.MULTIPLY else ExactOperation.valueOf(opName)
    val expected = when (opName) { "ADD" -> f.sum; "SUBTRACT" -> f.difference; "MULTIPLY" -> f.product; "self" -> f.square; else -> error("Unknown operation") }
    fun <V : Any> typed(parse: (String) -> V, text: (V) -> String, operation: (ExactOperation, V, V) -> V): TimedCase598 {
        val a = parse(f.a); val b = if (opName == "self") a else parse(f.b)
        val rightText = if (opName == "self") f.a else f.b
        // Parsing is preparation. Standard benchmark setup observes inputs/results before warmup.
        fun canonical(value: String) = if (family == "int") value else canonicalDecimal598(value).let { "${it.first}@${it.second}" }
        return TimedCase598(adapter.id, id, f.a, rightText,
            if (opName == "self") "self-alias" else if (a === b) "binary-native-alias" else "binary-distinct", expected,
            operation = { operation(op, a, b) },
            checkResult = { value -> @Suppress("UNCHECKED_CAST") val actual = text(value as V)
                check(canonical(actual) == canonical(expected)) { "${adapter.id}:$id result mismatch:$actual vs$expected" }; canonical(actual) },
            checkInputs = { check(canonical(text(a)) == canonical(f.a) && canonical(text(b)) == canonical(rightText)) { "Input changed" } })
    }
    return if (family == "int") typed(adapter::integer, adapter::integerText, adapter::integerOperation)
        else typed(adapter::decimal, adapter::decimalText, adapter::decimalOperation)
}

internal fun <I : Any, D : Any> fixedCase598(adapter: PeerAdapter<I, D>, id: String, left: String, right: String, scale: Int, expected: String): TimedCase598 {
    check(adapter.fixedScaleHalfUp.eligibleAfterFixtureGate) { "Explicit fixed-scale capability unavailable" }
    val a = adapter.decimal(left); val b = adapter.decimal(right)
    fun canonical(text: String) = canonicalDecimal598(text).let { "${it.first}@${it.second}" }
    return TimedCase598(adapter.id,id,left,right,if(a === b) "binary-native-alias" else "binary-distinct",expected,
        operation = { adapter.divideFixedScaleHalfUp(a,b,scale) },
        checkResult = { value -> @Suppress("UNCHECKED_CAST") val text = adapter.decimalText(value as D)
            check(canonical(text) == canonical(expected)) { "Fixed-scale numeric mismatch" }; canonical(text) },
        checkInputs = { check(canonical(adapter.decimalText(a)) == canonical(left) && canonical(adapter.decimalText(b)) == canonical(right)) { "Input changed" } })
}

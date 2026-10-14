// Standalone compiler input: kotlin.Number and the standard library only.
// Expected results are literal override bodies, not another numerical library.
private val calls = mutableListOf<String>()
private val order = listOf("Byte", "Short", "Int", "Long", "Float", "Double")
private var failures = 0

private class ConstantNumber(private val wide: Boolean = false) : Number() {
    override fun toByte(): Byte { calls.add("Byte"); return if (wide) 1 else 42 }
    override fun toShort(): Short { calls.add("Short"); return if (wide) 1 else 42 }
    override fun toInt(): Int { calls.add("Int"); return if (wide) 1 else 42 }
    override fun toLong(): Long { calls.add("Long"); return if (wide) 9007199254740993L else 42L }
    override fun toFloat(): Float { calls.add("Float"); return if (wide) 9007199254740992.0f else 42.75f }
    override fun toDouble(): Double { calls.add("Double"); return if (wide) 9007199254740992.0 else 42.75 }
}

// Explicitly chosen rounding for the integer 16777217. Both returned constants
// are exactly representable. This does not assume all JS Float values round to 32 bits.
private class RoundedNumber : Number() {
    override fun toByte(): Byte { calls.add("Byte"); return 1 }
    override fun toShort(): Short { calls.add("Short"); return 1 }
    override fun toInt(): Int { calls.add("Int"); return 16777217 }
    override fun toLong(): Long { calls.add("Long"); return 16777217L }
    override fun toFloat(): Float { calls.add("Float"); return 16777216.0f }
    override fun toDouble(): Double { calls.add("Double"); return 16777217.0 }
}

private class ThrowingNumber : Number() {
    override fun toByte(): Byte = error("Byte override")
    override fun toShort(): Short = error("Short override")
    override fun toInt(): Int = error("Int override")
    override fun toLong(): Long = error("Long override")
    override fun toFloat(): Float = error("Float override")
    override fun toDouble(): Double = error("Double override")
}

// Keep these non-inline callers separate to prevent a concrete smart cast from
// replacing the Number or generic receiver under investigation.
private fun concreteValues(n: ConstantNumber): List<Any> =
    listOf(n.toByte(), n.toShort(), n.toInt(), n.toLong(), n.toFloat(), n.toDouble())
private fun numberValues(n: Number): List<Any> =
    listOf(n.toByte(), n.toShort(), n.toInt(), n.toLong(), n.toFloat(), n.toDouble())
private fun <T : Number> genericValues(n: T): List<Any> =
    listOf(n.toByte(), n.toShort(), n.toInt(), n.toLong(), n.toFloat(), n.toDouble())
private fun roundedValues(n: RoundedNumber): List<Any> =
    listOf(n.toByte(), n.toShort(), n.toInt(), n.toLong(), n.toFloat(), n.toDouble())

private fun report(label: String, expected: List<Any>, body: () -> List<Any>) {
    calls.clear()
    val actual = body()
    val valuesOk = actual == expected
    val callbacksOk = calls == order
    println("$label expected=$expected actual=$actual callbacks=${calls.toList()} values=$valuesOk dispatch=$callbacksOk")
    if (!valuesOk || !callbacksOk) failures++
}

private fun caught(body: () -> Any): String = try { "returned:${body()}" }
    catch (e: IllegalStateException) { e.message ?: "missing message" }

private fun numberThrows(n: Number): List<String> = listOf(
    caught { n.toByte() }, caught { n.toShort() }, caught { n.toInt() },
    caught { n.toLong() }, caught { n.toFloat() }, caught { n.toDouble() })

fun main() {
    for (wide in listOf(false, true)) {
        val n = ConstantNumber(wide)
        val expected: List<Any> = if (wide)
            listOf(1.toByte(), 1.toShort(), 1, 9007199254740993L, 9007199254740992.0f, 9007199254740992.0)
        else listOf(42.toByte(), 42.toShort(), 42, 42L, 42.75f, 42.75)
        report("constant wide=$wide concrete", expected) { concreteValues(n) }
        report("constant wide=$wide Number", expected) { numberValues(n) }
        report("constant wide=$wide generic", expected) { genericValues(n) }
    }
    val rounded = RoundedNumber()
    val expected: List<Any> = listOf(1.toByte(), 1.toShort(), 16777217, 16777217L, 16777216.0f, 16777217.0)
    report("rounded concrete", expected) { roundedValues(rounded) }
    report("rounded Number", expected) { numberValues(rounded) }
    report("rounded generic", expected) { genericValues(rounded) }
    val throwing = ThrowingNumber()
    val direct = listOf(caught { throwing.toByte() }, caught { throwing.toShort() },
        caught { throwing.toInt() }, caught { throwing.toLong() },
        caught { throwing.toFloat() }, caught { throwing.toDouble() })
    val wanted = order.map { "$it override" }
    val erased = numberThrows(throwing)
    println("throwing concrete expected=$wanted actual=$direct")
    println("throwing Number expected=$wanted actual=$erased")
    if (direct != wanted) failures++
    if (erased != wanted) failures++
    println("SUMMARY failing_rows=$failures total_rows=11")
    check(failures == 0) { "Number contract probe: $failures failing rows" }
}

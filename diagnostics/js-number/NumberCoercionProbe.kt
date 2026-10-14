// Library-facing JS coercion candidates, compiled normally with the stdlib.
// These deliberately keep the caller's Number type and all six expected results.
private val callbacks = mutableListOf<String>()
private class CoercibleNumber(private val wide: Boolean) : Number() {
    override fun toByte(): Byte { callbacks.add("Byte"); return 1 }
    override fun toShort(): Short { callbacks.add("Short"); return 1 }
    override fun toInt(): Int { callbacks.add("Int"); return if (wide) 1 else 16777217 }
    override fun toLong(): Long { callbacks.add("Long"); return if (wide) 9007199254740993L else 16777217L }
    override fun toFloat(): Float { callbacks.add("Float"); return if (wide) 9007199254740992.0f else 16777216.0f }
    override fun toDouble(): Double { callbacks.add("Double"); return if (wide) 9007199254740992.0 else 16777217.0 }
}
private fun concreteValues(n: CoercibleNumber): List<Any> =
    listOf(n.toByte(), n.toShort(), n.toInt(), n.toLong(), n.toFloat(), n.toDouble())
private fun numberValues(n: Number): List<Any> =
    listOf(n.toByte(), n.toShort(), n.toInt(), n.toLong(), n.toFloat(), n.toDouble())
private fun <T : Number> genericValues(n: T): List<Any> =
    listOf(n.toByte(), n.toShort(), n.toInt(), n.toLong(), n.toFloat(), n.toDouble())

fun main() {
    var failures = 0
    for (wide in listOf(false, true)) for (hook in listOf("valueOf", "Symbol.toPrimitive")) {
        val n = CoercibleNumber(wide)
        val primitive = if (wide) 9007199254740992.0 else 16777217.0
        val expected: List<Any> = if (wide)
            listOf(1.toByte(), 1.toShort(), 1, 9007199254740993L, 9007199254740992.0f, 9007199254740992.0)
        else listOf(1.toByte(), 1.toShort(), 16777217, 16777217L, 16777216.0f, 16777217.0)
        if (hook == "valueOf") n.asDynamic().valueOf = {
            callbacks.add("valueOf"); primitive
        } else n.asDynamic()[js("Symbol.toPrimitive")] = { hint: String ->
            callbacks.add("Symbol.toPrimitive:$hint"); primitive
        }
        callbacks.clear()
        val direct = concreteValues(n)
        check(direct == expected && callbacks == listOf("Byte", "Short", "Int", "Long", "Float", "Double"))
        println("wide=$wide hook=$hook concrete=$direct callbacks=${callbacks.toList()}")
        for (generic in listOf(false, true)) {
            callbacks.clear()
            val actual = if (generic) genericValues(n) else numberValues(n)
            println("wide=$wide hook=$hook generic=$generic expected=$expected actual=$actual callbacks=${callbacks.toList()} values=${actual == expected}")
            if (actual != expected) failures++
        }
    }
    println("SUMMARY coercion_failing_rows=$failures total_erased_rows=8")
    check(failures == 0) { "Coercion does not preserve all six conversion results: $failures rows" }
}

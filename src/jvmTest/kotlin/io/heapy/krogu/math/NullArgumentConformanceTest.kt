package io.heapy.krogu.math

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.math.BigInteger as JI
import java.math.BigDecimal as JD
import java.math.MathContext as JC
import java.math.RoundingMode as JR
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Invoke actual Java signatures, so null reaches the library rather than a Kotlin caller cast. */
class NullArgumentConformanceTest {
    private val mappings: Map<Class<*>, Class<*>> = mapOf(JI::class.java to BigInteger::class.java,
        JD::class.java to BigDecimal::class.java, JC::class.java to MathContext::class.java,
        JR::class.java to RoundingMode::class.java)
    private fun adapt(value: Any?): Any? = when (value) {
        is JI -> BigInteger(value.toString())
        is JD -> BigDecimal(BigInteger(value.unscaledValue().toString()), value.scale())
        is JC -> MathContext(value.precision, RoundingMode.valueOf(value.roundingMode.name))
        is JR -> RoundingMode.valueOf(value.name)
        else -> value
    }
    private fun choices(type: Class<*>): List<Any?> = when (type) {
        Int::class.javaPrimitiveType -> listOf(-3, -1, 0, 1, 2, 7, 8, 9, 31, 32, 36, 37)
        Long::class.javaPrimitiveType -> listOf(-1L, 0L, 1L, Long.MIN_VALUE, Long.MAX_VALUE)
        Double::class.javaPrimitiveType -> listOf(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -0.0, 0.0, 0.1, 1.0)
        String::class.java -> listOf(null, "", "-", "1.0", "1E+", "NaN", "0", "1", "-1", "UP", "HALF_EVEN", "precision=7 roundingMode=UP")
        ByteArray::class.java -> listOf(null, byteArrayOf(), byteArrayOf(0), byteArrayOf(-1), byteArrayOf(1))
        CharArray::class.java -> listOf(null, charArrayOf(), "0".toCharArray(), ".".toCharArray(), "1.00".toCharArray())
        JI::class.java -> listOf(null, JI.TEN.negate(), JI.ONE.negate(), JI.ZERO, JI.ONE, JI.TEN)
        JD::class.java -> listOf(null, JD("-10.00"), JD("-1.01"), JD.ZERO, JD("0.00"), JD.ONE, JD("1.00"), JD("10.10"))
        JC::class.java -> listOf(null, JC.UNLIMITED, JC(1, JR.UNNECESSARY), JC(2, JR.HALF_EVEN), JC(7, JR.UP), JC(34, JR.FLOOR))
        JR::class.java -> listOf(null) + JR.entries
        java.util.Random::class.java -> listOf(null)
        Any::class.java -> listOf(null, "x", JI.ONE, JD.ONE, JC.DECIMAL32)
        else -> error("Unsupported parameter type $type")
    }
    private fun cases(types: Array<Class<*>>): Sequence<List<Any?>> {
        var current = sequenceOf(emptyList<Any?>())
        for (type in types) {
            val prior = current
            current = prior.flatMap { prefix -> choices(type).asSequence().map { prefix + it } }
        }
        return current.filter { values -> values.any { it == null } }
    }
    private fun canonical(value: Any?): String = when (value) {
        is ByteArray -> value.joinToString(",", "[", "]")
        is CharArray -> value.concatToString()
        is Array<*> -> value.joinToString("|") { canonical(it) }
        else -> value.toString()
    }
    private fun outcome(action: () -> Any?): String = try { canonical(action()) }
        catch (e: InvocationTargetException) { "!${e.targetException.javaClass.simpleName}" }

    @Test fun nullAndInvalidArgumentPrecedence() {
        val counts = linkedMapOf<String, Int>()
        val examples = mutableListOf<String>()
        val zeroSqrtRows = mutableSetOf<String>()
        val zeroDoubleRows = mutableSetOf<String>()
        var comparisons = 0
        fun compare(key: String, label: String, reference: () -> Any?, implementation: () -> Any?,
            zeroSqrtText: String? = null, zeroDoubleKey: String? = null) {
            comparisons++
            val expected = outcome(reference); val actual = outcome(implementation)
            if (zeroSqrtText != null) {
                assertTrue(zeroSqrtRows.add(zeroSqrtText), "Duplicate zero/null receiver: $zeroSqrtText")
                println("$key $label liveHost=$expected required=!NullPointerException actual=$actual")
                assertEquals("!NullPointerException", actual, "$key $label public null contract; liveHost=$expected")
                return
            }
            if (zeroDoubleKey != null) {
                assertTrue(zeroDoubleRows.add(zeroDoubleKey), "Duplicate signed-zero/null constructor: $zeroDoubleKey")
                println("$key $label liveHost=$expected required=!NullPointerException actual=$actual")
                assertEquals("!NullPointerException", actual, "$key $label public null contract; liveHost=$expected")
                return
            }
            if (expected != actual) {
                val count = counts[key] ?: 0; counts[key] = count + 1
                if (count < 3) examples.add("$key $label expected=$expected actual=$actual")
            }
        }
        for ((jdk, port) in mappings) {
            for (ctor in jdk.constructors) {
                val target = port.getConstructor(*ctor.parameterTypes.map { mappings[it] ?: it }.toTypedArray())
                for (args in cases(ctor.parameterTypes)) {
                    // Input-only selection: exact Double/context constructor and signed-zero bits.
                    val zeroDoubleKey = if (jdk == JD::class.java &&
                        ctor.parameterTypes.contentEquals(arrayOf(Double::class.javaPrimitiveType, JC::class.java)) &&
                        args[1] == null) {
                        when ((args[0] as Double).toRawBits()) {
                            Long.MIN_VALUE -> "-0.0"
                            0L -> "0.0"
                            else -> null
                        }
                    } else null
                    compare("${jdk.simpleName}.new(${ctor.parameterTypes.joinToString { it.simpleName }})",
                        args.joinToString { canonical(it) },
                        { ctor.newInstance(*args.toTypedArray()) }, { target.newInstance(*args.map(::adapt).toTypedArray()) },
                        zeroDoubleKey = zeroDoubleKey)
                }
            }
            val receivers: List<Any> = when (jdk) {
                JI::class.java -> listOf(JI.TEN.negate(), JI.ONE.negate(), JI.ZERO, JI.ONE, JI.TEN)
                JD::class.java -> listOf(JD("-10.00"), JD("-1.01"), JD.ZERO, JD("0.00"), JD.ONE, JD("1.00"), JD("10.10"))
                JC::class.java -> listOf(JC.UNLIMITED, JC(1, JR.UNNECESSARY), JC.DECIMAL32)
                else -> listOf(JR.HALF_EVEN)
            }
            for (method in jdk.declaredMethods.filter { Modifier.isPublic(it.modifiers) && !it.isBridge }) {
                val target = port.getMethod(method.name, *method.parameterTypes.map { mappings[it] ?: it }.toTypedArray())
                val instances = if (Modifier.isStatic(method.modifiers)) listOf(null) else receivers
                for (receiver in instances) for (args in cases(method.parameterTypes)) {
                    // Select by the exact public signature, ordinary receiver cohort and null input.
                    val zeroSqrtText = if (jdk == JD::class.java && method.name == "sqrt" &&
                        method.parameterTypes.contentEquals(arrayOf(JC::class.java)) && args == listOf(null) &&
                        receiver is JD && receiver.javaClass == JD::class.java &&
                        (receiver == JD.ZERO || receiver == JD("0.00"))) receiver.toString() else null
                    compare("${jdk.simpleName}.${method.name}(${method.parameterTypes.joinToString { it.simpleName }})",
                        "receiver=$receiver args=${args.joinToString { canonical(it) }}",
                        { method.invoke(receiver, *args.toTypedArray()) },
                        { target.invoke(adapt(receiver), *args.map(::adapt).toTypedArray()) }, zeroSqrtText)
                }
            }
        }
        assertEquals(setOf("0", "0.00"), zeroSqrtRows)
        assertEquals(2, zeroSqrtRows.size)
        assertEquals(setOf("-0.0", "0.0"), zeroDoubleRows)
        assertEquals(2, zeroDoubleRows.size)
        assertEquals(emptyMap(), counts, "$comparisons comparisons; discrepancies=$counts\n${examples.joinToString("\n")}")
        println("$comparisons null/invalid-argument comparisons: ${comparisons - zeroSqrtRows.size - zeroDoubleRows.size} retained host comparisons, ${zeroSqrtRows.size + zeroDoubleRows.size} public null requirements")
    }
}

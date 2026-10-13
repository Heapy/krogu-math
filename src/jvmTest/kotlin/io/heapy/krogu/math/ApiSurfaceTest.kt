package io.heapy.krogu.math

import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertTrue

/** Detect newly missing JDK 25 methods/constructors without reading implementation sources. */
class ApiSurfaceTest {
    private fun signature(name: String, types: Array<Class<*>>): String =
        "$name(${types.joinToString { it.simpleName }})"

    @Test fun jdkPublicSurfaceIsPresent() {
        for ((jdk, port) in listOf(
            java.math.BigInteger::class.java to BigInteger::class.java,
            java.math.BigDecimal::class.java to BigDecimal::class.java,
            java.math.MathContext::class.java to MathContext::class.java,
            java.math.RoundingMode::class.java to RoundingMode::class.java,
        )) {
            val actual = port.methods.map { signature(it.name, it.parameterTypes) }.toSet()
            val expected = jdk.declaredMethods.filter { Modifier.isPublic(it.modifiers) && !it.isBridge }
                .map { signature(it.name, it.parameterTypes) }.toSet()
            assertTrue(actual.containsAll(expected), "${port.simpleName} missing methods: ${expected - actual}")
            // Random is intentionally kotlin.random.Random in common, with java.util.Random factories on JVM.
            val constructors = port.constructors.map { signature("new", it.parameterTypes) }.toSet()
            val expectedConstructors = jdk.constructors.map { signature("new", it.parameterTypes) }.toSet()
            assertTrue(constructors.containsAll(expectedConstructors), "${port.simpleName} missing constructors: ${expectedConstructors - constructors}")
            val fields = port.fields.map { it.name }.toSet()
            val expectedFields = jdk.fields.map { it.name }.toSet()
            assertTrue(fields.containsAll(expectedFields), "${port.simpleName} missing constants: ${expectedFields - fields}")
        }
    }
}

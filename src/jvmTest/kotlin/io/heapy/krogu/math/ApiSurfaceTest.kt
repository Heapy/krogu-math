package io.heapy.krogu.math

import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertTrue

/** Detect newly missing JDK 25 methods/constructors without reading implementation sources. */
class ApiSurfaceTest {
    private fun typeName(type: Class<*>): String = when {
        type.isArray -> typeName(type.componentType) + "[]"
        type.name.startsWith("io.heapy.krogu.math.") -> "java.math." + type.simpleName
        else -> type.name
    }
    private fun signature(name: String, types: Array<Class<*>>): String =
        "$name(${types.joinToString(transform = ::typeName)})"
    private fun methodSignature(method: java.lang.reflect.Method): String =
        signature(method.name, method.parameterTypes) + ":" + typeName(method.returnType) +
            if (Modifier.isStatic(method.modifiers)) ":static" else ":instance"

    @Test fun jdkPublicSurfaceIsPresent() {
        for ((jdk, port) in listOf(
            java.math.BigInteger::class.java to BigInteger::class.java,
            java.math.BigDecimal::class.java to BigDecimal::class.java,
            java.math.MathContext::class.java to MathContext::class.java,
            java.math.RoundingMode::class.java to RoundingMode::class.java,
        )) {
            assertTrue(Modifier.isFinal(jdk.modifiers) == Modifier.isFinal(port.modifiers),
                "${port.simpleName} class finality differs from JDK")
            val actual = port.methods.map(::methodSignature).toSet()
            val expected = jdk.declaredMethods.filter { Modifier.isPublic(it.modifiers) && !it.isBridge }
                .map(::methodSignature).toSet()
            assertTrue(actual.containsAll(expected), "${port.simpleName} missing methods: ${expected - actual}")
            for (method in jdk.declaredMethods.filter { Modifier.isPublic(it.modifiers) && !it.isBridge }) {
                val counterpart = port.methods.first { methodSignature(it) == methodSignature(method) }
                assertTrue(Modifier.isFinal(method.modifiers) == Modifier.isFinal(counterpart.modifiers),
                    "${port.simpleName}.${method.name} method finality differs from JDK")
            }
            val constructors = port.constructors.map { signature("new", it.parameterTypes) }.toSet()
            val expectedConstructors = jdk.constructors.map { signature("new", it.parameterTypes) }.toSet()
            assertTrue(constructors.containsAll(expectedConstructors), "${port.simpleName} missing constructors: ${expectedConstructors - constructors}")
            fun fieldSignature(field: java.lang.reflect.Field): String =
                "${field.name}:${typeName(field.type)}:${field.modifiers}"
            val fields = port.fields.map(::fieldSignature).toSet()
            val expectedFields = jdk.fields.map(::fieldSignature).toSet()
            assertTrue(fields.containsAll(expectedFields), "${port.simpleName} missing constants: ${expectedFields - fields}")
        }
    }
}

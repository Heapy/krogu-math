package io.heapy.krogu.math

import java.io.*
import java.net.URLClassLoader
import javax.tools.ToolProvider
import kotlin.test.*

class SerializationConformanceTest {
    private class IntegerChild(value: String, val label: String) : BigInteger(value) {
        var alias: IntArray? = null
    }
    private class DecimalChild(value: String, val label: String) : BigDecimal(value)
    private class JIntegerChild(value: String, val label: String) : java.math.BigInteger(value) {
        override fun toByte(): Byte = toInt().toByte()
        override fun toShort(): Short = toInt().toShort()
    }
    private class JDecimalChild(value: String, val label: String) : java.math.BigDecimal(value) {
        override fun toByte(): Byte = toInt().toByte()
        override fun toShort(): Short = toInt().toShort()
    }

    private fun encode(value: Any): ByteArray = ByteArrayOutputStream().also { buffer ->
        ObjectOutputStream(buffer).use { it.writeObject(value) }
    }.toByteArray()
    private fun decode(bytes: ByteArray): Any = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() }
    private fun roundTrip(value: Any): Any = decode(encode(value))
    private fun field(owner: Class<*>, name: String) = owner.getDeclaredField(name).apply { isAccessible = true }

    @Test fun serialVersionIdentifiersAreExplicit() {
        for ((jdk, port) in listOf(java.math.BigInteger::class.java to BigInteger::class.java,
            java.math.BigDecimal::class.java to BigDecimal::class.java,
            java.math.MathContext::class.java to MathContext::class.java)) {
            assertEquals(ObjectStreamClass.lookup(jdk).serialVersionUID, ObjectStreamClass.lookup(port).serialVersionUID)
            val uid = port.getDeclaredField("serialVersionUID")
            assertTrue(java.lang.reflect.Modifier.isPrivate(uid.modifiers))
            assertTrue(java.lang.reflect.Modifier.isStatic(uid.modifiers))
            assertTrue(java.lang.reflect.Modifier.isFinal(uid.modifiers))
        }
    }

    @Test fun subclassesKeepTheirTypeStateAndValue() {
        val integer = roundTrip(IntegerChild("-12345678901234567890123456789", "integer")) as IntegerChild
        val jInteger = roundTrip(JIntegerChild("-12345678901234567890123456789", "integer")) as JIntegerChild
        assertEquals(jInteger.toString(), integer.toString())
        assertEquals(jInteger.label, integer.label)
        val decimal = roundTrip(DecimalChild("12345678901234567890.1200", "decimal")) as DecimalChild
        val jDecimal = roundTrip(JDecimalChild("12345678901234567890.1200", "decimal")) as JDecimalChild
        assertEquals(jDecimal.toString(), decimal.toString())
        assertEquals(jDecimal.scale(), decimal.scale())
        assertEquals(jDecimal.label, decimal.label)
    }

    @Test fun subclassCannotKeepAnAliasToRestoredLimbs() {
        assertTrue(java.lang.reflect.Modifier.isFinal(field(BigInteger::class.java, "words").modifiers))
        val value = IntegerChild("12345678901234567890123456789", "alias")
        value.alias = field(BigInteger::class.java, "words").get(value) as IntArray
        val restored = roundTrip(value) as IntegerChild
        restored.alias!!.fill(0)
        assertEquals("12345678901234567890123456789", restored.toString())
    }

    @Test fun malformedSubclassFieldsAreRejectedDuringReading() {
        for (invalid in listOf(null, intArrayOf(), intArrayOf(-1), intArrayOf(1 shl 30), intArrayOf(1, 0))) {
            val value = IntegerChild("1", "invalid")
            field(BigInteger::class.java, "words").set(value, invalid)
            assertFailsWith<InvalidObjectException> { roundTrip(value) }
        }
        for (sign in listOf(-2, 0, 2)) {
            val value = IntegerChild("1", "invalid sign")
            field(BigInteger::class.java, "sign").setInt(value, sign)
            assertFailsWith<InvalidObjectException> { roundTrip(value) }
        }
        val decimal = DecimalChild("1.20", "missing coefficient")
        field(BigDecimal::class.java, "coefficient").set(decimal, null)
        assertFailsWith<InvalidObjectException> { roundTrip(decimal) }
    }

    @Test fun malformedProxiesUseSerializationExceptions() {
        fun proxy(value: Any): Any = value.javaClass.getDeclaredMethod("writeReplace").apply {
            isAccessible = true
        }.invoke(value)
        for (original in listOf(BigInteger.ONE, BigDecimal.ONE)) {
            for (invalid in listOf(null, byteArrayOf())) {
                val value = proxy(original)
                field(value.javaClass, "bytes").set(value, invalid)
                val failure = assertFailsWith<InvalidObjectException> { roundTrip(value) }
                assertIs<RuntimeException>(failure.cause)
            }
        }
        for ((name, invalid) in listOf("precision" to -1, "rounding" to "INVALID", "rounding" to null)) {
            val value = proxy(MathContext.DECIMAL128)
            field(value.javaClass, name).set(value, invalid)
            assertFailsWith<InvalidObjectException> { roundTrip(value) }
        }
    }

    @Test fun repeatedReferencesAndCyclesRemainIntact() {
        val value = IntegerChild("234567890123456789", "shared")
        val graph = arrayListOf<Any>(value, value)
        graph.add(graph)
        val copy = roundTrip(graph) as List<*>
        assertSame(copy[0], copy[1])
        assertSame(copy, copy[2])
        assertEquals(value, copy[0])
    }

    @Test fun insertingSerializableMathSuperclassWithoutDataIsRejected() {
        // Genuine hierarchy evolution: the writer's child had no math superclass.
        // The reader loads a same-name/same-UID child with a newly inserted one.
        val directory = kotlin.io.path.createTempDirectory("krogu-stream-hierarchy").toFile()
        try {
            val compiler = ToolProvider.getSystemJavaCompiler()
            val classpath = listOf(BigInteger::class.java, kotlin.Unit::class.java).joinToString(File.pathSeparator) {
                File(it.protectionDomain.codeSource.location.toURI()).path
            }
            fun compile(name: String, source: String): File {
                val output = File(directory, name).apply { mkdirs() }
                val file = File(output, "SerializedChild.java").apply { writeText(source) }
                val diagnostics = ByteArrayOutputStream()
                assertEquals(0, compiler.run(null, diagnostics, diagnostics, "-classpath", classpath,
                    "-d", output.path, file.path), diagnostics.toString())
                return output
            }
            val old = compile("old", """
                public class SerializedChild implements java.io.Serializable {
                    private static final long serialVersionUID = 1L;
                }
            """.trimIndent())
            val bytes = URLClassLoader(arrayOf(old.toURI().toURL()), javaClass.classLoader).use { loader ->
                encode(loader.loadClass("SerializedChild").getConstructor().newInstance())
            }
            for (base in listOf("BigInteger", "BigDecimal")) {
                for (namespace in listOf("java.math", "io.heapy.krogu.math")) {
                    val output = compile("$namespace-$base", """
                        public class SerializedChild extends $namespace.$base {
                            private static final long serialVersionUID = 1L;
                            public SerializedChild() { super("1"); }
                        }
                    """.trimIndent())
                    URLClassLoader(arrayOf(output.toURI().toURL()), javaClass.classLoader).use { loader ->
                        val input = object : ObjectInputStream(ByteArrayInputStream(bytes)) {
                            override fun resolveClass(descriptor: ObjectStreamClass): Class<*> =
                                if (descriptor.name == "SerializedChild") loader.loadClass(descriptor.name)
                                else super.resolveClass(descriptor)
                        }
                        input.use { assertFailsWith<InvalidObjectException>("$namespace.$base") { it.readObject() } }
                    }
                }
            }
        } finally {
            directory.walkBottomUp().forEach { it.delete() }
        }
    }
}

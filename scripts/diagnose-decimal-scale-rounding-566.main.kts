// SPDX-License-Identifier: Apache-2.0
// PREPARED / UNCOMPILED / UNRUN. Execute only after a separate parent grant.
// Later: kotlinr scripts/diagnose-decimal-scale-rounding-566.main.kts <fresh-own-jvm.jar>
// Stdout is a complete bounded TSV diagnostic; retain it, including exceptions.
// No third-party implementation source or numerical expectation provider is used.
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader
import java.security.MessageDigest

require(args.size == 1) { "Expected one fresh own JVM JAR path" }
val jar = File(args[0]).canonicalFile
require(jar.isFile) { "Missing own JVM JAR: $jar" }
val hash = MessageDigest.getInstance("SHA-256").digest(jar.readBytes()).joinToString("") { "%02x".format(it) }
println("jar\t$jar\tsha256=$hash")
println("jdk\t${System.getProperty("java.vendor")}\t${System.getProperty("java.runtime.version")}")

fun observation(action: () -> String): String = try { action() } catch (failure: Throwable) {
    val e = if (failure is InvocationTargetException) failure.targetException else failure
    "!${e.javaClass.simpleName}:${e.message}".replace('\t', ' ').replace('\n', ' ')
}

URLClassLoader(arrayOf(jar.toURI().toURL()), kotlin.Unit::class.java.classLoader).use { loader ->
    val decimal = loader.loadClass("io.heapy.krogu.math.BigDecimal")
    val integer = loader.loadClass("io.heapy.krogu.math.BigInteger")
    val context = loader.loadClass("io.heapy.krogu.math.MathContext")
    val rounding = loader.loadClass("io.heapy.krogu.math.RoundingMode")
    for (type in listOf(decimal, integer, context, rounding)) {
        val origin = type.protectionDomain.codeSource?.location
        println("loaded-class\t${type.name}\t$origin")
        require(origin != null && File(origin.toURI()).canonicalFile == jar) {
            "Ambient classpath shadowed supplied JAR for ${type.name}: $origin"
        }
    }
    fun own(c: Long, s: Int): Any = decimal.getConstructor(integer, Int::class.javaPrimitiveType)
        .newInstance(integer.getConstructor(String::class.java).newInstance(c.toString()), s)
    fun pair(value: Any): String = "${decimal.getMethod("unscaledValue").invoke(value)}@${decimal.getMethod("scale").invoke(value)}"
    fun ref(c: Long, s: Int) = java.math.BigDecimal.valueOf(c, s)
    fun refPair(x: java.math.BigDecimal) = "${x.unscaledValue()}@${x.scale()}"
    fun row(label: String, expected: String, port: () -> String, jdk: () -> String) {
        val actual = observation(port)
        val reference = observation(jdk)
        println("$label\t$expected\t$actual\t$reference")
    }
    println("case\tindependent-expectation\tport-outcome\tpublic-jdk-observation")
    for (sign in listOf(-1L, 1L)) for (mode in java.math.RoundingMode.entries) {
        val ownMode = rounding.getMethod("valueOf", String::class.java).invoke(null, mode.name)
        val mc = context.getConstructor(Int::class.javaPrimitiveType, rounding).newInstance(1, ownMode)
        val jmc = java.math.MathContext(1, mode)
        val tiny = when (mode) {
            java.math.RoundingMode.UNNECESSARY -> "!ArithmeticException"
            java.math.RoundingMode.UP -> "$sign@0"
            java.math.RoundingMode.CEILING -> "${if (sign > 0) 1 else 0}@0"
            java.math.RoundingMode.FLOOR -> "${if (sign < 0) -1 else 0}@0"
            else -> "0@0"
        }
        for (legacy in listOf(false, true)) row("setScale sign=$sign mode=$mode legacy=$legacy", tiny, {
            val type = if (legacy) Int::class.javaPrimitiveType!! else rounding
            pair(decimal.getMethod("setScale", Int::class.javaPrimitiveType, type)
                .invoke(own(sign, Int.MAX_VALUE), 0, if (legacy) mode.ordinal else ownMode))
        }, { refPair(if (legacy) ref(sign, Int.MAX_VALUE).setScale(0, mode.ordinal)
            else ref(sign, Int.MAX_VALUE).setScale(0, mode)) })
        row("multiply sign=$sign mode=$mode", "$sign@${Int.MAX_VALUE}", {
            pair(decimal.getMethod("multiply", decimal, context).invoke(own(sign, Int.MAX_VALUE), own(10, 1), mc))
        }, { refPair(ref(sign, Int.MAX_VALUE).multiply(ref(10, 1), jmc)) })
        val carry = when (mode) {
            java.math.RoundingMode.UNNECESSARY, java.math.RoundingMode.DOWN -> false
            java.math.RoundingMode.CEILING -> sign > 0
            java.math.RoundingMode.FLOOR -> sign < 0
            else -> true
        }
        row("carry-only multiply sign=$sign mode=$mode", if (carry) "$sign@${Int.MAX_VALUE}" else "!ArithmeticException", {
            pair(decimal.getMethod("multiply", decimal, context).invoke(own(sign * 99, Int.MAX_VALUE), own(1, 2), mc))
        }, { refPair(ref(sign * 99, Int.MAX_VALUE).multiply(ref(1, 2), jmc)) })
        for (reverse in listOf(false, true)) row("add sign=$sign mode=$mode reverse=$reverse", "$sign@${Int.MIN_VALUE}", {
            val a = own(sign, Int.MIN_VALUE); val b = own(0, Int.MAX_VALUE)
            pair(decimal.getMethod("add", decimal, context).invoke(if (reverse) b else a, if (reverse) a else b, mc))
        }, {
            val a = ref(sign, Int.MIN_VALUE); val b = ref(0, Int.MAX_VALUE)
            refPair(if (reverse) b.add(a, jmc) else a.add(b, jmc))
        })
    }
}
// No equality assertion or defect classification: all outcomes remain available for later review.

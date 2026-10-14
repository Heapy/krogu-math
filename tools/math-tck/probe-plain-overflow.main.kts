#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
// Independent public coefficient/scale witnesses; no numerical implementation.
// Run with pinned port jar on kotlinr -cp, BellSoft25.0.4+9-LTS and -J-Xmx256m.
import io.heapy.krogu.math.BigDecimal as PortDecimal
import io.heapy.krogu.math.BigInteger as PortInteger
import java.security.MessageDigest
import java.io.File

val expectedJarHash = "54ec9af64675eba2a89d7cba68a1c404220cfaf3169bd1de8a31193514e31db0"
val jar = File(PortDecimal::class.java.protectionDomain.codeSource.location.toURI())
check(MessageDigest.getInstance("SHA-256").digest(jar.readBytes()).joinToString("") { "%02x".format(it.toInt() and 255) } == expectedJarHash)
println("runtime=${System.getProperty("java.runtime.version")} vendor=${System.getProperty("java.vendor")} jar=${jar.path} sha256=$expectedJarHash")
println("coefficient\tscale\tmathematicalPlainLength\thost\tport")
fun outcome(call: () -> String): String = try {
    val value = call(); "RETURN:length=${value.length}"
} catch (failure: Throwable) { "${failure.javaClass.name}:${failure.message}" }
for (coefficient in listOf("1", "-1", "123456789", "-123456789")) {
    val negative = coefficient.startsWith('-')
    val digits = coefficient.removePrefix("-").length
    val extra = if (negative) 1 else 0
    val positiveScales = if (negative) listOf(Int.MAX_VALUE, Int.MAX_VALUE - 1, Int.MAX_VALUE - 2) else listOf(Int.MAX_VALUE, Int.MAX_VALUE - 1)
    for (scale in positiveScales + listOf(-Int.MAX_VALUE)) {
        val length = if (scale > 0) scale.toLong() + 2 + extra else digits.toLong() - scale.toLong() + extra
        check(length > Int.MAX_VALUE.toLong()) { "This witness must exceed JVM String's Int length" }
        val host = java.math.BigDecimal(java.math.BigInteger(coefficient), scale)
        val port = PortDecimal(PortInteger(coefficient), scale)
        check(host.scale() == scale && port.scale() == scale)
        println("$coefficient\t$scale\t$length\t${outcome { host.toPlainString() }}\t${outcome { port.toPlainString() }}")
    }
}

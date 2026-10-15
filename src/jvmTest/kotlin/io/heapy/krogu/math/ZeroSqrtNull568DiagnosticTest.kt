// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import java.math.BigDecimal as JD
import java.math.BigInteger as JI
import java.math.MathContext as JC
import java.math.RoundingMode as JR
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Prepared public-API replay; JDK observations never supply the required null outcome. */
class ZeroSqrtNull568DiagnosticTest {
    @BeforeTest fun pinnedOracle() {
        assertEquals("BellSoft", System.getProperty("java.vendor"))
        assertEquals("25.0.4+9-LTS", System.getProperty("java.runtime.version"))
    }

    private class TruthfulJdk(c: JI, s: Int) : JD(c, s) {
        override fun toByte(): Byte = super.toInt().toByte()
        override fun toShort(): Short = super.toInt().toShort()
        override fun signum() = super.signum()
        override fun precision() = super.precision()
        override fun scale() = super.scale()
        override fun unscaledValue() = super.unscaledValue()
        override fun stripTrailingZeros() = super.stripTrailingZeros()
        override fun sqrt(mc: JC?) = super.sqrt(mc)
        fun inheritedRoot(mc: JC?) = super.sqrt(mc)
    }

    private class Truthful(c: BigInteger, s: Int) : BigDecimal(c, s) {
        override fun signum() = super.signum()
        override fun precision() = super.precision()
        override fun scale() = super.scale()
        override fun unscaledValue() = super.unscaledValue()
        override fun stripTrailingZeros() = super.stripTrailingZeros()
        override fun sqrt(mc: MathContext?) = super.sqrt(mc)
        fun inheritedRoot(mc: MathContext?) = super.sqrt(mc)
    }

    private data class Outcome(val coefficient: String? = null, val scale: Int? = null, val exception: String? = null)
    private fun observe(action: () -> Any): Outcome = try {
        when (val result = action()) {
            is JD -> Outcome(result.unscaledValue().toString(), result.scale())
            is BigDecimal -> Outcome(result.unscaledValue().toString(), result.scale())
            else -> error("Unexpected result type")
        }
    } catch (e: Exception) { Outcome(exception = e.javaClass.simpleName) }

    @Test fun zeroNullRawHostObservationsStaySeparateFromRequiredNpe() {
        val expectedKeys = setOf("ordinary:0@0/null", "truthful:0@0/null", "callerSuper:0@0/null",
            "ordinary:0@2/null", "truthful:0@2/null", "callerSuper:0@2/null")
        val seen = mutableSetOf<String>()
        for (scale in listOf(0, 2)) {
            val text = if (scale == 0) "0" else "0.00"
            val jdk = TruthfulJdk(JI.ZERO, scale)
            val own = Truthful(BigInteger.ZERO, scale)
            for (origin in listOf("ordinary", "truthful", "callerSuper")) {
                val key = "$origin:0@$scale/null"
                assertTrue(seen.add(key), "Duplicate key: $key")
                val rawHost = observe {
                    when (origin) {
                        "ordinary" -> JD(text).sqrt(null)
                        "truthful" -> jdk.sqrt(null)
                        else -> jdk.inheritedRoot(null)
                    }
                }
                val actual = observe {
                    when (origin) {
                        "ordinary" -> BigDecimal(text).sqrt(null)
                        "truthful" -> own.sqrt(null)
                        else -> own.inheritedRoot(null)
                    }
                }
                println("$key rawHost=$rawHost required=NullPointerException actual=$actual")
                assertEquals(Outcome(exception = "NullPointerException"), actual, key)
                // Only ordinary inputs have frozen historical observations in the null oracle.
                if (origin == "ordinary") assertEquals(Outcome("0", scale / 2), rawHost, "$key frozen host observation")
            }
        }
        assertEquals(expectedKeys, seen)
        assertEquals(6, seen.size)
    }

    @Test fun validZeroContextsKeepPublicJdkAndIndependentPreferredScale() {
        for ((scale, preferred) in listOf(Int.MIN_VALUE to -1073741824, -3 to -1, 0 to 0, 2 to 1,
            Int.MAX_VALUE to 1073741823)) {
            val jdk = TruthfulJdk(JI.ZERO, scale)
            val own = Truthful(BigInteger.ZERO, scale)
            for (precision in listOf(0, 7)) for (mode in RoundingMode.entries) {
                val jc = JC(precision, JR.valueOf(mode.name))
                val mc = MathContext(precision, mode)
                val pairs = listOf(
                    "ordinary" to (observe { JD(JI.ZERO, scale).sqrt(jc) } to observe { BigDecimal(BigInteger.ZERO, scale).sqrt(mc) }),
                    "truthful" to (observe { jdk.sqrt(jc) } to observe { own.sqrt(mc) }),
                    "callerSuper" to (observe { jdk.inheritedRoot(jc) } to observe { own.inheritedRoot(mc) }),
                )
                for ((origin, pair) in pairs) {
                    val (rawHost, actual) = pair
                    val key = "$origin:0@$scale/p$precision/$mode"
                    println("$key rawHost=$rawHost actual=$actual")
                    assertEquals(Outcome("0", preferred), actual, "$key specified zero and scale")
                    assertEquals(rawHost, actual, "$key public JDK comparison")
                }
            }
        }
    }
}

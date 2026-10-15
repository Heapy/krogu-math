// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals

/** PREPARED / UNCOMPILED / UNRUN. Public black-box observations are not oracle replacements. */
class DecimalScaleRoundingRepairJvmTest {
    @Test fun mandatoryPairsAndRetainedPublicJdkObservations() {
        val mismatches = mutableListOf<String>()
        fun observe(label: String, expected: String, port: () -> BigDecimal, jdk: () -> java.math.BigDecimal) {
            val actual = try { port().let { "${it.unscaledValue()}@${it.scale()}" } }
                catch (_: ArithmeticException) { "!ArithmeticException" }
            val reference = try { jdk().let { "${it.unscaledValue()}@${it.scale()}" } }
                catch (e: ArithmeticException) { "!ArithmeticException:${e.message}" }
            println("task566\t$label\texpected=$expected\tport=$actual\tjdk-observation=$reference")
            if (actual != expected) mismatches += "$label expected=$expected actual=$actual"
        }
        for (sign in listOf(-1L, 1L)) for (mode in RoundingMode.entries) {
            val rm = java.math.RoundingMode.valueOf(mode.name)
            val mc = MathContext(1, mode)
            val jmc = java.math.MathContext(1, rm)
            fun own(c: Long, s: Int) = BigDecimal.valueOf(c, s)
            fun ref(c: Long, s: Int) = java.math.BigDecimal.valueOf(c, s)
            val tiny = when (mode) {
                RoundingMode.UNNECESSARY -> "!ArithmeticException"
                RoundingMode.UP -> "$sign@0"
                RoundingMode.CEILING -> "${if (sign > 0) 1 else 0}@0"
                RoundingMode.FLOOR -> "${if (sign < 0) -1 else 0}@0"
                else -> "0@0"
            }
            observe("tiny $sign $mode", tiny,
                { own(sign, Int.MAX_VALUE).setScale(0, mode) },
                { ref(sign, Int.MAX_VALUE).setScale(0, rm) })
            observe("multiply $sign $mode", "$sign@${Int.MAX_VALUE}",
                { own(sign, Int.MAX_VALUE).multiply(own(10, 1), mc) },
                { ref(sign, Int.MAX_VALUE).multiply(ref(10, 1), jmc) })
            // 99 requires two drops only when rounding carries: 99 -> 10 -> 1.
            val carry = when (mode) {
                RoundingMode.UNNECESSARY, RoundingMode.DOWN -> false
                RoundingMode.CEILING -> sign > 0
                RoundingMode.FLOOR -> sign < 0
                else -> true
            }
            observe("carry-only rescue $sign $mode", if (carry) "$sign@${Int.MAX_VALUE}" else "!ArithmeticException",
                { own(sign * 99, Int.MAX_VALUE).multiply(own(1, 2), mc) },
                { ref(sign * 99, Int.MAX_VALUE).multiply(ref(1, 2), jmc) })
            observe("add $sign $mode", "$sign@${Int.MIN_VALUE}",
                { own(sign, Int.MIN_VALUE).add(own(0, Int.MAX_VALUE), mc) },
                { ref(sign, Int.MIN_VALUE).add(ref(0, Int.MAX_VALUE), jmc) })
            observe("reversed add $sign $mode", "$sign@${Int.MIN_VALUE}",
                { own(0, Int.MAX_VALUE).add(own(sign, Int.MIN_VALUE), mc) },
                { ref(0, Int.MAX_VALUE).add(ref(sign, Int.MIN_VALUE), jmc) })
        }
        // Check only independent port expectations, after retaining every bounded observation.
        assertEquals(emptyList(), mismatches)
    }

    @Test fun truthfulExtremeScaleKeepsExplicitDispatchAndIndependentResults() {
        val mismatches = mutableListOf<String>()
        val prefixes = linkedMapOf("integer" to "integerExact;intScale;", "scale" to "scale;intScale;",
            "enumScale" to "enumScale;intScale;", "intScale" to "intScale;")
        for ((operation, prefix) in prefixes) {
            val portTrace = StringBuilder()
            val hostTrace = StringBuilder()
            val port = object : BigDecimal(BigInteger.ONE, Int.MAX_VALUE) {
                override fun toLong(): Long { portTrace.append("long;"); return super.toLong() }
                override fun longValueExact(): Long { portTrace.append("longExact;"); return super.longValueExact() }
                override fun precision(): Int { portTrace.append("precision;"); return super.precision() }
                override fun toBigIntegerExact(): BigInteger { portTrace.append("integerExact;"); return super.toBigIntegerExact() }
                override fun setScale(newScale: Int): BigDecimal { portTrace.append("scale;"); return super.setScale(newScale) }
                override fun setScale(newScale: Int, roundingMode: RoundingMode): BigDecimal {
                    portTrace.append("enumScale;"); return super.setScale(newScale, roundingMode)
                }
                override fun setScale(newScale: Int, roundingMode: Int): BigDecimal {
                    portTrace.append("intScale;"); return super.setScale(newScale, roundingMode)
                }
                override fun signum(): Int { portTrace.append("sign;"); return super.signum() }
            }
            val host = object : java.math.BigDecimal(java.math.BigInteger.ONE, Int.MAX_VALUE) {
                override fun toByte(): Byte = super.toInt().toByte()
                override fun toShort(): Short = super.toInt().toShort()
                override fun toLong(): Long { hostTrace.append("long;"); return super.toLong() }
                override fun longValueExact(): Long { hostTrace.append("longExact;"); return super.longValueExact() }
                override fun precision(): Int { hostTrace.append("precision;"); return super.precision() }
                override fun toBigIntegerExact(): java.math.BigInteger { hostTrace.append("integerExact;"); return super.toBigIntegerExact() }
                override fun setScale(newScale: Int): java.math.BigDecimal { hostTrace.append("scale;"); return super.setScale(newScale) }
                override fun setScale(newScale: Int, roundingMode: java.math.RoundingMode): java.math.BigDecimal {
                    hostTrace.append("enumScale;"); return super.setScale(newScale, roundingMode)
                }
                override fun setScale(newScale: Int, roundingMode: Int): java.math.BigDecimal {
                    hostTrace.append("intScale;"); return super.setScale(newScale, roundingMode)
                }
                override fun signum(): Int { hostTrace.append("sign;"); return super.signum() }
            }
            fun observe(action: () -> Any): String = try { action().toString() }
                catch (failure: Exception) { "!${failure.javaClass.simpleName}:${failure.message}" }
            val portObservation = observe {
                when (operation) {
                    "integer" -> port.toBigIntegerExact()
                    "scale" -> port.setScale(0)
                    "enumScale" -> port.setScale(0, RoundingMode.HALF_UP)
                    else -> port.setScale(0, RoundingMode.HALF_UP.ordinal)
                }
            }
            val hostObservation = observe {
                when (operation) {
                    "integer" -> host.toBigIntegerExact()
                    "scale" -> host.setScale(0)
                    "enumScale" -> host.setScale(0, java.math.RoundingMode.HALF_UP)
                    else -> host.setScale(0, java.math.RoundingMode.HALF_UP.ordinal)
                }
            }
            println("task566-truthful\t$operation\t1@${Int.MAX_VALUE}\ttarget=0\tHALF_UP\tport=$portObservation|$portTrace\tjdk-observation=$hostObservation|$hostTrace")
            val expected = if (operation == "integer" || operation == "scale") "!ArithmeticException" else "0"
            if (portObservation.substringBefore(':') != expected)
                mismatches += "independent result $operation: expected=$expected port=$portObservation"
            for ((label, trace) in listOf("port" to portTrace.toString(), "host" to hostTrace.toString())) {
                if (!trace.startsWith(prefix)) mismatches += "explicit dispatch $label $operation: $trace"
                else if (!Regex("(?:sign;)*").matches(trace.removePrefix(prefix)))
                    mismatches += "unexpected callback after explicit dispatch $label $operation: $trace"
            }
        }
        assertEquals(emptyList(), mismatches, "all four complete port/JDK observations are retained above")
    }
}

package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CompactDecimal529JvmTest {
    private fun compare(label: String, expected: () -> java.math.BigDecimal, actual: () -> BigDecimal) {
        val jdk = try { expected() } catch (failure: ArithmeticException) {
            val own = try { actual(); error("Expected ArithmeticException: $label") } catch (own: ArithmeticException) { own }
            assertEquals(failure::class.java, own::class.java, label)
            assertEquals(failure.message, own.message, "$label exception message")
            return
        }
        val own = actual()
        assertEquals(jdk.unscaledValue().toString(), own.unscaledValue().toString(), "$label coefficient")
        assertEquals(jdk.scale(), own.scale(), "$label scale")
        assertEquals(jdk.toString(), own.toString(), "$label text")
    }

    @Test fun compactDivisionEndpointsSignsModesAndCohortsMatchPublicOracle() {
        val texts = listOf(Long.MIN_VALUE.toString(), Long.MAX_VALUE.toString(), "9223372036854775808",
            "-1", "1", "-2", "2", "-5", "5", "-10", "10")
        var checked = 0
        for (a in texts) for (b in texts) for (mode in RoundingMode.entries) {
            val ownA = BigDecimal(BigInteger(a), 0); val ownB = BigDecimal(BigInteger(b), 0)
            val jA = java.math.BigDecimal(java.math.BigInteger(a)); val jB = java.math.BigDecimal(java.math.BigInteger(b))
            val jMode = java.math.RoundingMode.valueOf(mode.name)
            for (scale in listOf(-2, 0, 2)) {
                compare("$a/$b scale=$scale $mode", { jA.divide(jB, scale, jMode) }, { ownA.divide(ownB, scale, mode) })
                checked++
            }
            for (precision in listOf(1, 7, 19, 34)) {
                compare("$a/$b p=$precision $mode", { jA.divide(jB, java.math.MathContext(precision, jMode)) },
                    { ownA.divide(ownB, MathContext(precision, mode)) })
                checked++
            }
        }
        assertEquals(6776, checked)
    }

    @Test fun scaledCompactDivisionAndExactScaleMatchPublicOracle() {
        val values = listOf("123.4500", "-1.25", "0.125", "1E+7", "-92233720368547758.08")
        for (a in values) for (b in listOf("-2.00", "0.5", "1E+3", "-0.00001")) {
            val ownA = BigDecimal(a); val ownB = BigDecimal(b)
            val jA = java.math.BigDecimal(a); val jB = java.math.BigDecimal(b)
            compare("exact $a/$b", { jA.divide(jB) }, { ownA.divide(ownB) })
            for (precision in listOf(1, 7, 19, 34)) for (mode in RoundingMode.entries) {
                compare("scaled $a/$b p=$precision $mode", { jA.divide(jB, java.math.MathContext(precision, java.math.RoundingMode.valueOf(mode.name))) },
                    { ownA.divide(ownB, MathContext(precision, mode)) })
            }
        }
        assertTrue(BigDecimal("1.00").divide(BigDecimal("2.0")).hasCompactStorage529())
    }
}

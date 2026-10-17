package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalFormattingSubclassTest {
    @Test fun alternateFormatsMatchJdkGetterDispatch() {
        val mismatches = mutableListOf<String>()
        for (coefficient in listOf("0", "1", "-1", "123", "-123", "2147483647", "123456789012345678901234567890")) {
            for (scale in listOf(-2, -1, 0, 1, 2, 3, 6, 7)) for (sign in -1..1) for (plain in listOf(false, true)) {
                val expectedTrace = StringBuilder()
                val actualTrace = StringBuilder()
                val expected = object : java.math.BigDecimal(java.math.BigInteger(coefficient), scale) {
                    override fun toByte(): Byte = super.toInt().toByte()
                    override fun toShort(): Short = super.toInt().toShort()
                    override fun signum(): Int { expectedTrace.append("sign;"); return sign }
                    override fun precision(): Int { expectedTrace.append("precision;"); return 1 }
                }
                val actual = object : BigDecimal(BigInteger(coefficient), scale) {
                    override fun signum(): Int { actualTrace.append("sign;"); return sign }
                    override fun precision(): Int { actualTrace.append("precision;"); return 1 }
                }
                val a = if (plain) expected.toPlainString() else expected.toEngineeringString()
                val b = if (plain) actual.toPlainString() else actual.toEngineeringString()
                if (a != b || expectedTrace.toString() != actualTrace.toString())
                    mismatches.add("$coefficient@$scale sign=$sign plain=$plain: $a|$expectedTrace / $b|$actualTrace")
            }
        }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n${mismatches.take(20).joinToString("\n")}")
    }
}

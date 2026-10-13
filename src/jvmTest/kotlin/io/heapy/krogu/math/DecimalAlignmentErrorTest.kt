package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertTrue

class DecimalAlignmentErrorTest {
    @Test fun exactAlignmentErrorsMatchJdk() {
        val mismatches = mutableListOf<String>()
        val scales = listOf(Int.MIN_VALUE, Int.MIN_VALUE + 1, -2, 0, 2, Int.MAX_VALUE - 1, Int.MAX_VALUE)
        for (left in listOf("0", "1", "-1")) for (right in listOf("0", "1", "-1")) {
            for (ls in scales) for (rs in scales) for (subtract in listOf(false, true)) {
                val expected = java.math.BigDecimal(java.math.BigInteger(left), ls)
                val expectedRight = java.math.BigDecimal(java.math.BigInteger(right), rs)
                val actual = BigDecimal(BigInteger(left), ls)
                val actualRight = BigDecimal(BigInteger(right), rs)
                fun outcome(block: () -> Any): String = try { block().toString() }
                    catch (e: Exception) { "!${e::class.simpleName}:${e.message}" }
                val a = outcome { if (subtract) expected.subtract(expectedRight) else expected.add(expectedRight) }
                val b = outcome { if (subtract) actual.subtract(actualRight) else actual.add(actualRight) }
                if (a != b) mismatches.add("$left@$ls/$right@$rs/$subtract: $a <> $b")
            }
        }
        assertTrue(mismatches.isEmpty(), "${mismatches.size} mismatches\n${mismatches.take(24).joinToString("\n")}")
    }
}

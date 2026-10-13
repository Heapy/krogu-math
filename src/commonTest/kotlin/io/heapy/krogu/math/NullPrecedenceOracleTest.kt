package io.heapy.krogu.math

import kotlin.test.*

@Suppress("DEPRECATION")
class NullPrecedenceOracleTest {
    @Test fun nullAndInvalidArgumentPrecedenceAcrossPlatforms() {
        val expected = nullPrecedenceExpected().iterator()
        var count = 0
        fun verify(label: String, action: () -> Any?) {
            assertTrue(expected.hasNext(), "Oracle ended before $label")
            val actual = try { action().toString() } catch (e: Exception) { "!${e::class.simpleName}" }
            assertEquals(expected.next(), actual, "$count: $label")
            count++
        }
        val integers = listOf(-3, -1, 0, 1, 2, 7, 8, 9, 31, 32, 36, 37)
        for (sign in integers) for (off in integers) for (len in integers)
            verify("signed null bytes $sign,$off,$len") { BigInteger(sign, null, off, len) }
        for (a in listOf(-10L, -1, 0, 1, 10)) for (m in listOf(-10L, -1, 0, 1, 10))
            verify("null exponent $a,$m") { BigInteger.valueOf(a).modPow(null, BigInteger.valueOf(m)) }
        for (text in listOf("", "-", "1.0", "1E+", "NaN", "0", "1", "-1", "UP", "HALF_EVEN", "precision=7 roundingMode=UP"))
            verify("string/null context [$text]") { BigDecimal(text, null) }
        for (d in listOf(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -0.0, 0.0, 0.1, 1.0))
            verify("double/null context $d") { BigDecimal(d, null) }
        for (text in listOf("", "0", ".", "1.00"))
            verify("chars/null context [$text]") { BigDecimal(text.toCharArray(), null) }
        for (text in listOf("", "0", ".", "1.00")) for (off in integers) for (len in integers)
            verify("char slice/null context [$text],$off,$len") { BigDecimal(text.toCharArray(), off, len, null) }
        val decimals = listOf("-10.00", "-1.01", "0", "0.00", "1", "1.00", "10.10")
        for (text in decimals) verify("sqrt/null context $text") { BigDecimal(text).sqrt(null) }
        for (text in decimals) for (mode in integers)
            verify("legacy null divide $text,$mode") { BigDecimal(text).divide(null, mode) }
        for (text in decimals) for (scale in integers) for (mode in integers)
            verify("legacy null divide $text,$scale,$mode") { BigDecimal(text).divide(null, scale, mode) }
        for (precision in integers) verify("context/null rounding $precision") { MathContext(precision, null) }
        assertEquals(3462, count)
        assertFalse(expected.hasNext(), "Unconsumed oracle outcomes")
    }
}

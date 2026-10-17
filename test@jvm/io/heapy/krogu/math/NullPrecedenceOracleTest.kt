package io.heapy.krogu.math

import kotlin.test.*

@Suppress("DEPRECATION")
class NullPrecedenceOracleTest {
    @Test fun exactFrozenRowsThroughJavaBoundaries() {
        val expected = nullPrecedenceExpected().iterator()
        // Retain these exact frozen observations; the public null contract requires NPE.
        val zeroSqrtObservations = mapOf("sqrt/null context 0" to "0", "sqrt/null context 0.00" to "0.0")
        val zeroDoubleObservations = mapOf("double/null context -0.0" to "0", "double/null context 0.0" to "0")
        val selected = mutableSetOf<String>()
        val selectedDoubles = mutableSetOf<String>()
        var count = 0
        val keys = mutableSetOf<String>()
        val partitions = linkedMapOf<String, Int>()
        var ordinary = 0
        fun verify(label: String, validOtherInputs: Boolean, action: () -> Any?) {
            assertTrue(expected.hasNext(), "Oracle ended before $label")
            assertTrue(keys.add(label), "Duplicate row $count: $label")
            val family = label.substringBefore(' ')
            partitions[family] = (partitions[family] ?: 0) + 1
            var message: String? = null
            val actual = try { action().toString() } catch (e: Exception) { message = e.message; "!${e::class.simpleName}" }
            val observed = expected.next()
            println("ordinal=$count key=$label class=${if (validOtherInputs) "ordinary-null" else "compound-invalid-priority"} frozen=$observed actual=$actual message=$message")
            if (validOtherInputs) {
                ordinary++
                assertEquals("!NullPointerException", actual, "$count: $label public null contract")
            } else assertTrue(actual.startsWith("!"), "$count: $label must reject invalid inputs")
            if (label in zeroSqrtObservations) {
                assertTrue(selected.add(label), "Duplicate zero/null key: $label")
                assertEquals(zeroSqrtObservations.getValue(label), observed, "Frozen observation: $label")
                println("$label frozen=$observed required=!NullPointerException actual=$actual")
                assertEquals("!NullPointerException", actual, "$count: $label public null contract")
            } else if (label in zeroDoubleObservations) {
                assertTrue(selectedDoubles.add(label), "Duplicate signed-zero/null key: $label")
                assertEquals(zeroDoubleObservations.getValue(label), observed, "Frozen observation: $label")
                println("$label frozen=$observed required=!NullPointerException actual=$actual")
                assertEquals("!NullPointerException", actual, "$count: $label public null contract")
            }
            count++
        }
        val integers = listOf(-3, -1, 0, 1, 2, 7, 8, 9, 31, 32, 36, 37)
        for (sign in integers) for (off in integers) for (len in integers)
            verify("signed null bytes $sign,$off,$len", sign in -1..1 && off >= 0 && len >= 0) { Validation578Boundary.signedBytes(sign, null, off, len) }
        assertEquals(1728, count)
        for (a in listOf(-10L, -1, 0, 1, 10)) for (m in listOf(-10L, -1, 0, 1, 10))
            verify("null exponent $a,$m", m > 0) { Validation578Boundary.modPow(BigInteger.valueOf(a), null, BigInteger.valueOf(m)) }
        assertEquals(1753, count)
        for (text in listOf("", "-", "1.0", "1E+", "NaN", "0", "1", "-1", "UP", "HALF_EVEN", "precision=7 roundingMode=UP"))
            verify("string/null context [$text]", text in listOf("1.0", "0", "1", "-1")) { Validation578Boundary.decimal(text, null) }
        assertEquals(1764, count)
        for (d in listOf(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -0.0, 0.0, 0.1, 1.0)) {
            // Keep the signed-zero input keys stable across platform Double formatting.
            val label = when (d.toRawBits()) {
                Long.MIN_VALUE -> "double/null context -0.0"
                0L -> "double/null context 0.0"
                else -> "double/null context $d"
            }
            verify(label, d.isFinite()) { Validation578Boundary.decimal(d, null) }
        }
        assertEquals(1771, count)
        for (text in listOf("", "0", ".", "1.00"))
            verify("chars/null context [$text]", text == "0" || text == "1.00") { Validation578Boundary.decimal(text.toCharArray(), null) }
        assertEquals(1775, count)
        for (text in listOf("", "0", ".", "1.00")) for (off in integers) for (len in integers)
            verify("char slice/null context [$text],$off,$len", off >= 0 && len > 0 && off <= text.length - len &&
                text.substring(off, off + len) !in listOf("", ".")) { Validation578Boundary.decimal(text.toCharArray(), off, len, null) }
        assertEquals(2351, count)
        val decimals = listOf("-10.00", "-1.01", "0", "0.00", "1", "1.00", "10.10")
        for (text in decimals) verify("sqrt/null context $text", !text.startsWith('-')) { Validation578Boundary.sqrt(BigDecimal(text), null) }
        assertEquals(2358, count)
        for (text in decimals) for (mode in integers)
            verify("legacy null divide $text,$mode", mode in 0..7) { Validation578Boundary.divide(BigDecimal(text), null, mode) }
        assertEquals(2442, count)
        for (text in decimals) for (scale in integers) for (mode in integers)
            verify("legacy null divide $text,$scale,$mode", mode in 0..7) { Validation578Boundary.divide(BigDecimal(text), null, scale, mode) }
        assertEquals(3450, count)
        for (precision in integers) verify("context/null rounding $precision", precision >= 0) { Validation578Boundary.context(precision, null) }
        assertEquals(3462, count)
        assertEquals(3462, keys.size)
        assertEquals(mapOf("signed" to 1728, "null" to 25, "string/null" to 11,
            "double/null" to 7, "chars/null" to 4, "char" to 576, "sqrt/null" to 7,
            "legacy" to 1092, "context/null" to 12), partitions)
        // 300 signed + 10 exponent + 4 strings + 4 doubles + 2 arrays + 6 slices
        // + 5 roots + (28 + 336) legacy divides + 10 contexts.
        assertEquals(705, ordinary)
        assertEquals(zeroSqrtObservations.keys, selected)
        assertEquals(2, selected.size)
        assertEquals(zeroDoubleObservations.keys, selectedDoubles)
        assertEquals(2, selectedDoubles.size)
        assertEquals(3458, count - selected.size - selectedDoubles.size)
        assertFalse(expected.hasNext(), "Unconsumed oracle outcomes")
    }
}

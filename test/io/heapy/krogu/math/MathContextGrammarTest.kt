package io.heapy.krogu.math

import kotlin.test.*

class MathContextGrammarTest {
    private fun outcome(text: String): String = try {
        val value = MathContext(text)
        "OK:${value.precision}:${value.roundingMode.name}"
    } catch (e: IllegalArgumentException) {
        assertNull(e.cause)
        "ERR:${e.message}"
    }

    @Test fun compoundGrammarMatchesOracleIncludingErrors() {
        var count = 0
        for (row in jdk25ContextResults()) {
            val (encoded, expected) = row.split('|')
            val text = encoded.chunked(4).map { it.toInt(16).toChar() }.joinToString("")
            assertEquals(expected, outcome(text), "UTF-16 $encoded")
            count++
        }
        assertTrue(count > 1000)
    }

    private fun characters(batch: Int) {
        val digits = jdk25BmpDigits()
        for (code in batch * 8192 until (batch + 1) * 8192) {
            val char = code.toChar()
            val digit = digits[code].toInt().takeIf { it in 0..9 }
            assertEquals(if (digit == null) "ERR:bad string format" else "OK:$digit:UP",
                outcome("precision=$char roundingMode=UP"), "single U+${code.toString(16)}")
            assertEquals(if (digit == null) "ERR:bad string format" else "OK:${102 + digit * 10}:UP",
                outcome("precision=1${char}2 roundingMode=UP"), "compound U+${code.toString(16)}")
            assertEquals(if (char == ' ') "OK:1:UP" else "ERR:bad string format",
                outcome("precision=1${char}roundingMode=UP"), "separator U+${code.toString(16)}")
        }
    }
    @Test fun characters0() = characters(0)
    @Test fun characters1() = characters(1)
    @Test fun characters2() = characters(2)
    @Test fun characters3() = characters(3)
    @Test fun characters4() = characters(4)
    @Test fun characters5() = characters(5)
    @Test fun characters6() = characters(6)
    @Test fun characters7() = characters(7)

    @Test fun longPrecisionTokens() {
        for (size in listOf(10000, 1000000)) {
            assertEquals("OK:7:UP", outcome("precision=${"0".repeat(size)}7 roundingMode=UP"))
            assertEquals("ERR:bad string format", outcome("precision=${"9".repeat(size)} roundingMode=UP"))
        }
    }
}

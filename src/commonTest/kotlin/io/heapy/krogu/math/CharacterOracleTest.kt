package io.heapy.krogu.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CharacterOracleTest {
    @Test fun everyBmpCharacterAndValidRadix() {
        val digits = jdk25BmpDigits()
        for (code in digits.indices) {
            val digit = digits[code].toInt()
            for (radix in 2..36) {
                val expected = digit.takeIf { it in 0 until radix }
                val actual = code.toChar().digitToIntOrNull(radix)
                if (actual != expected) fail("U+${code.toString(16)}, radix=$radix: expected=$expected actual=$actual")
            }
        }
    }
    private fun number(action: () -> Any): Int? {
        val value = try { action() } catch (e: NumberFormatException) { return null }
        return value.toString().toInt()
    }
    private fun bmpConstructors(start: Int, end: Int) {
        val digits = jdk25BmpDigits()
        for (code in start until end) {
            val text = code.toChar().toString()
            val digit = digits[code].toInt()
            val integer = number { BigInteger(text, 36) }
            val expectedInteger = digit.takeIf { it >= 0 }
            if (integer != expectedInteger) fail("BigInteger U+${code.toString(16)}: expected=$expectedInteger actual=$integer")
            val expectedDecimal = digit.takeIf { it in 0..9 }
            val decimal = number { BigDecimal(text) }
            if (decimal != expectedDecimal) fail("BigDecimal U+${code.toString(16)}: expected=$expectedDecimal actual=$decimal")
        }
    }
    @Test fun bmpConstructors0000to3fff() = bmpConstructors(0, 0x4000)
    @Test fun bmpConstructors4000to7fff() = bmpConstructors(0x4000, 0x8000)
    @Test fun bmpConstructors8000tobfff() = bmpConstructors(0x8000, 0xc000)
    @Test fun bmpConstructorsc000toffff() = bmpConstructors(0xc000, 0x10000)

    @Test fun validDigitCharactersInEveryIntegerRadix() {
        val digits = jdk25BmpDigits()
        for (code in digits.indices) {
            val digit = digits[code].toInt()
            if (digit < 0) continue
            for (radix in 2..36) {
                val expected = digit.takeIf { it < radix }
                assertEquals(expected, number { BigInteger(code.toChar().toString(), radix) }, "U+${code.toString(16)}, radix=$radix")
            }
        }
    }
    @Test fun supplementaryCodePointsKeepUtf16ConstructorSemantics() {
        val data = jdk25SupplementaryDigits()
        for (i in data.indices step 3) {
            val cp = data[i] - 0x10000
            val text = charArrayOf((0xd800 + (cp ushr 10)).toChar(), (0xdc00 + (cp and 1023)).toChar()).concatToString()
            assertEquals(data[i + 1].takeIf { it >= 0 }, number { BigInteger(text, 36) }, "Supplementary integer U+${data[i].toString(16)}")
            assertEquals(data[i + 2].takeIf { it >= 0 }, number { BigDecimal(text) }, "Supplementary decimal U+${data[i].toString(16)}")
        }
    }
}

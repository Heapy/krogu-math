// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class TextApi537JavaTest {
    @Test public void fullSignaturesAreCallableWithoutConstructorAmbiguity() {
        BigInteger integer = BigInteger.parseText(new StringBuilder("[ff]"), 1, 2, 16);
        assertEquals("255", integer.toString());
        assertEquals(integer, BigInteger.parseChars(new char[] {'f', 'f'}, 0, 2, 16));
        assertEquals(integer, BigInteger.parseCharsBounded(new char[] {'f', 'f'}, 0, 2, 2, 16));
        BigDecimal decimal = BigDecimal.parseText(new StringBuilder("[12.3400]"), 1, 7, MathContext.UNLIMITED);
        assertEquals("12.3400", decimal.toString());
        assertEquals(decimal, BigDecimal.parseCharsBounded("12.3400".toCharArray(), 0, 7, 7, MathContext.UNLIMITED));
        StringBuilder text = new StringBuilder();
        assertEquals(7L, decimal.appendTo(text, DecimalTextFormat.PLAIN, 7L));
        assertEquals("12.3400", text.toString());
        char[] buffer = "#########".toCharArray();
        assertEquals(7, decimal.writeTo(buffer, 1, 7, DecimalTextFormat.CANONICAL));
        assertEquals("#12.3400#", new String(buffer));
        assertThrows(ArithmeticException.class, () -> integer.appendTo(new StringBuilder(), 10, 2L));
        assertThrows(ArithmeticException.class, () -> BigInteger.parseTextBounded("255", 0, 3, 2, 10));
    }

    @Test public void nullInputsAndDestinationsFailAtPublicBoundary() {
        assertThrows(NullPointerException.class, () -> BigInteger.parseText((CharSequence) null, 10));
        assertThrows(NullPointerException.class, () -> BigDecimal.parseText((CharSequence) null, MathContext.UNLIMITED));
        assertThrows(NullPointerException.class, () -> BigDecimal.parseText("1", (MathContext) null));
        assertThrows(NullPointerException.class, () -> BigInteger.ONE.appendTo(null, 10, null));
        assertThrows(NullPointerException.class, () -> BigDecimal.ONE.writeTo(null, 0, 0, DecimalTextFormat.PLAIN));
    }
}

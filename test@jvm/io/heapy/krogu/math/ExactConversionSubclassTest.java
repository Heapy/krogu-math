package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExactConversionSubclassTest {
    @Test public void narrowExactConversionsDoNotCallIntValueExact() {
        for (String text : new String[] {"0", "1", "-1", "127", "-128", "32767", "-32768"}) {
            java.math.BigInteger expected = new java.math.BigInteger(text) {
                @Override public int intValueExact() { throw new AssertionError("intValueExact called"); }
                @Override public byte byteValue() { return 42; }
                @Override public short shortValue() { return 43; }
            };
            BigInteger actual = new BigInteger(text) {
                @Override public int intValueExact() { throw new AssertionError("intValueExact called"); }
                @Override public byte byteValue() { return 42; }
                @Override public short shortValue() { return 43; }
            };
            assertEquals(expected.shortValueExact(), actual.shortValueExact());
            if (new java.math.BigInteger(text).bitLength() <= 7)
                assertEquals(expected.byteValueExact(), actual.byteValueExact());
        }
    }
    @Test public void oversizedMagnitudesRejectBeforeBitLengthOverride() {
        for (String text : new String[] {"4294967296", "-4294967296", "18446744073709551616", "-18446744073709551616"}) {
            java.math.BigInteger expected = new java.math.BigInteger(text) {
                @Override public int bitLength() { throw new AssertionError("bitLength called"); }
            };
            BigInteger actual = new BigInteger(text) {
                @Override public int bitLength() { throw new AssertionError("bitLength called"); }
            };
            assertThrows(ArithmeticException.class, expected::intValueExact);
            assertThrows(ArithmeticException.class, actual::intValueExact);
            assertThrows(ArithmeticException.class, expected::shortValueExact);
            assertThrows(ArithmeticException.class, actual::shortValueExact);
            assertThrows(ArithmeticException.class, expected::byteValueExact);
            assertThrows(ArithmeticException.class, actual::byteValueExact);
            if (new java.math.BigInteger(text).bitLength() > 64) {
                assertThrows(ArithmeticException.class, expected::longValueExact);
                assertThrows(ArithmeticException.class, actual::longValueExact);
            }
        }
    }
}

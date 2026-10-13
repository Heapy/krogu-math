package io.heapy.krogu.math;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/** Compile and execute actual Java consumers: reflection alone cannot prove source compatibility. */
public class JavaApiContractTest {
    static class IntegerSubclass extends BigInteger {
        IntegerSubclass() { super("123"); }
        @Override public int intValue() { return 42; }
        @Override public long longValue() { return 43L; }
        @Override public float floatValue() { return 44F; }
        @Override public double doubleValue() { return 45D; }
        @Override public byte byteValue() { return 46; }
        @Override public short shortValue() { return 47; }
        @Override public BigInteger add(BigInteger other) { return BigInteger.TEN; }
        public static BigInteger valueOf(long value) { return BigInteger.TWO; }
    }
    static class DecimalSubclass extends BigDecimal {
        DecimalSubclass() { super("1.23"); }
        @Override public int intValue() { return 52; }
        @Override public long longValue() { return 53L; }
        @Override public float floatValue() { return 54F; }
        @Override public double doubleValue() { return 55D; }
        @Override public byte byteValue() { return 56; }
        @Override public short shortValue() { return 57; }
        @Override public BigDecimal add(BigDecimal other) { return BigDecimal.TEN; }
        public static BigDecimal valueOf(long value) { return BigDecimal.TWO; }
    }
    @Test public void randomConstructorsAndStaticFactories() {
        for (int bits : new int[] {0, 1, 2, 31, 32, 63, 64, 100, 256}) {
            assertEquals(new java.math.BigInteger(bits, new Random(42)).toString(),
                new BigInteger(bits, new Random(42)).toString());
        }
        BigInteger prime = new BigInteger(64, 100, new Random(42));
        assertEquals(64, prime.bitLength());
        assertTrue(new java.math.BigInteger(prime.toString()).isProbablePrime(100));
        assertTrue(BigInteger.probablePrime(64, new Random(42)).isProbablePrime(100));
    }
    @Test public void javaSubclassDispatch() {
        BigInteger integer = new IntegerSubclass();
        Number integerNumber = integer;
        assertEquals(42, integerNumber.intValue());
        assertEquals(43L, integerNumber.longValue());
        assertEquals(44F, integerNumber.floatValue(), 0F);
        assertEquals(45D, integerNumber.doubleValue(), 0D);
        assertEquals(46, integerNumber.byteValue());
        assertEquals(47, integerNumber.shortValue());
        assertEquals(BigInteger.TEN, integer.add(BigInteger.ONE));
        BigDecimal decimal = new DecimalSubclass();
        Number decimalNumber = decimal;
        assertEquals(52, decimalNumber.intValue());
        assertEquals(53L, decimalNumber.longValue());
        assertEquals(54F, decimalNumber.floatValue(), 0F);
        assertEquals(55D, decimalNumber.doubleValue(), 0D);
        assertEquals(56, decimalNumber.byteValue());
        assertEquals(57, decimalNumber.shortValue());
        assertEquals(BigDecimal.TEN, decimal.add(BigDecimal.ONE));
        assertEquals(BigInteger.TWO, IntegerSubclass.valueOf(123L));
        assertEquals(BigDecimal.TWO, DecimalSubclass.valueOf(123L));
    }
    @Test public void nullableRandomCallsRemainUnambiguous() {
        // Like JDK, (int, null) is ambiguous with the sign/magnitude constructor.
        assertThrows(NullPointerException.class, () -> new BigInteger(8, (Random) null));
        assertThrows(NullPointerException.class, () -> new BigInteger(8, 100, null));
        assertThrows(NullPointerException.class, () -> BigInteger.probablePrime(8, null));
        assertEquals(BigInteger.ZERO, new BigInteger(0, (Random) null));
        assertThrows(IllegalArgumentException.class, () -> new BigInteger(-1, (Random) null));
        assertThrows(ArithmeticException.class, () -> new BigInteger(1, 100, null));
        assertThrows(ArithmeticException.class, () -> BigInteger.probablePrime(1, null));
    }
}

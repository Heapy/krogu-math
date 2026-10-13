package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class BitwiseSubclassContractTest {
    static class PortInteger extends BigInteger {
        PortInteger(String value) { super(value); }
        @Override public BigInteger not() { throw new AssertionError("not override called"); }
        @Override public BigInteger and(BigInteger value) { throw new AssertionError("and override called"); }
        @Override public BigInteger abs() { throw new AssertionError("abs override called"); }
        @Override public BigInteger negate() { throw new AssertionError("negate override called"); }
        @Override public BigInteger add(BigInteger value) { throw new AssertionError("add override called"); }
        @Override public BigInteger subtract(BigInteger value) { throw new AssertionError("subtract override called"); }
    }
    static class JdkInteger extends java.math.BigInteger {
        JdkInteger(String value) { super(value); }
        @Override public java.math.BigInteger not() { throw new AssertionError("not override called"); }
        @Override public java.math.BigInteger and(java.math.BigInteger value) { throw new AssertionError("and override called"); }
        @Override public java.math.BigInteger abs() { throw new AssertionError("abs override called"); }
        @Override public java.math.BigInteger negate() { throw new AssertionError("negate override called"); }
        @Override public java.math.BigInteger add(java.math.BigInteger value) { throw new AssertionError("add override called"); }
        @Override public java.math.BigInteger subtract(java.math.BigInteger value) { throw new AssertionError("subtract override called"); }
    }
    private void sameUpdate(java.math.BigInteger expected, BigInteger actual) {
        assertEquals(new BigInteger(expected.toString()), actual);
        assertEquals(expected.getClass() == JdkInteger.class, actual.getClass() == PortInteger.class);
    }
    @Test public void andNotDoesNotDispatchThroughOtherPublicOperations() {
        for (String left : new String[] {"0", "1", "42", "-1", "-42", "1073741824"}) {
            for (String right : new String[] {"0", "1", "10", "-1", "-10", "1073741823"}) {
                String expected = new JdkInteger(left).andNot(new JdkInteger(right)).toString();
                assertEquals(expected, new PortInteger(left).andNot(new PortInteger(right)).toString());
            }
        }
    }
    @Test public void bitQueriesAndUpdatesDoNotDispatchThroughArithmeticOverrides() {
        for (String text : new String[] {"0", "1", "42", "-1", "-42", "-1073741824"}) {
            JdkInteger expected = new JdkInteger(text);
            PortInteger actual = new PortInteger(text);
            assertEquals(expected.bitLength(), actual.bitLength());
            assertEquals(expected.bitCount(), actual.bitCount());
            for (int bit : new int[] {0, 1, 5, 29, 30, 31, 63, 90}) {
                assertEquals(expected.testBit(bit), actual.testBit(bit));
                sameUpdate(expected.setBit(bit), actual.setBit(bit));
                sameUpdate(expected.clearBit(bit), actual.clearBit(bit));
                sameUpdate(expected.flipBit(bit), actual.flipBit(bit));
            }
        }
    }
    @Test public void notDoesNotDispatchThroughNegate() {
        java.math.BigInteger expected = new java.math.BigInteger("42") {
            @Override public java.math.BigInteger negate() { throw new AssertionError("negate called"); }
        };
        BigInteger actual = new BigInteger("42") {
            @Override public BigInteger negate() { throw new AssertionError("negate called"); }
        };
        assertEquals(new BigInteger(expected.not().toString()), actual.not());
    }
    @Test public void flipBitDoesNotDispatchThroughOtherBitUpdates() {
        java.math.BigInteger expected = new java.math.BigInteger("-42") {
            @Override public boolean testBit(int n) { throw new AssertionError("testBit called"); }
            @Override public java.math.BigInteger setBit(int n) { throw new AssertionError("setBit called"); }
            @Override public java.math.BigInteger clearBit(int n) { throw new AssertionError("clearBit called"); }
        };
        BigInteger actual = new BigInteger("-42") {
            @Override public boolean testBit(int n) { throw new AssertionError("testBit called"); }
            @Override public BigInteger setBit(int n) { throw new AssertionError("setBit called"); }
            @Override public BigInteger clearBit(int n) { throw new AssertionError("clearBit called"); }
        };
        for (int bit : new int[] {0, 1, 29, 30, 31, 63, 90})
            assertEquals(new BigInteger(expected.flipBit(bit).toString()), actual.flipBit(bit));
    }
}

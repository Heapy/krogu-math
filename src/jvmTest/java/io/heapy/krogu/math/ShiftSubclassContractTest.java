package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShiftSubclassContractTest {
    private void sameShift(java.math.BigInteger expectedSource, BigInteger actualSource,
                           java.math.BigInteger expected, BigInteger actual) {
        assertEquals(expected.toString(), actual.toString());
        assertEquals(expected == expectedSource, actual == actualSource);
        assertEquals(expected.getClass() == java.math.BigInteger.class, actual.getClass() == BigInteger.class);
        if (expectedSource.signum() == 0) {
            assertSame(java.math.BigInteger.ZERO, expected);
            assertSame(BigInteger.ZERO, actual);
        }
    }
    @Test public void leftShiftDoesNotCallOverriddenRightShift() {
        for (String text : new String[] {"0", "1", "-1", "42", "-42", "-1073741824"}) {
            java.math.BigInteger expected = new java.math.BigInteger(text) {
                @Override public java.math.BigInteger shiftRight(int distance) { throw new AssertionError("shiftRight called"); }
            };
            BigInteger actual = new BigInteger(text) {
                @Override public BigInteger shiftRight(int distance) { throw new AssertionError("shiftRight called"); }
            };
            for (int distance : new int[] {Integer.MIN_VALUE, -100, -31, -30, -1, 0, 1, 30, 31, 100})
                sameShift(expected, actual, expected.shiftLeft(distance), actual.shiftLeft(distance));
        }
    }
    @Test public void rightShiftDoesNotCallOverriddenLeftShiftOrLowestBit() {
        for (String text : new String[] {"0", "1", "-1", "42", "-42", "-1073741824"}) {
            java.math.BigInteger expected = new java.math.BigInteger(text) {
                @Override public java.math.BigInteger shiftLeft(int distance) { throw new AssertionError("shiftLeft called"); }
                @Override public int getLowestSetBit() { throw new AssertionError("getLowestSetBit called"); }
            };
            BigInteger actual = new BigInteger(text) {
                @Override public BigInteger shiftLeft(int distance) { throw new AssertionError("shiftLeft called"); }
                @Override public int getLowestSetBit() { throw new AssertionError("getLowestSetBit called"); }
            };
            for (int distance : new int[] {-100, -31, -30, -1, 0, 1, 30, 31, 100, Integer.MAX_VALUE})
                sameShift(expected, actual, expected.shiftRight(distance), actual.shiftRight(distance));
        }
    }
}

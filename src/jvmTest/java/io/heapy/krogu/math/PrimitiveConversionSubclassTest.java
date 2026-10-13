package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrimitiveConversionSubclassTest {
    @Test public void byteAndShortRetainNumberIntValueDelegation() {
        java.math.BigInteger expected = new java.math.BigInteger("123456789") {
            @Override public int intValue() { return 250; }
        };
        BigInteger actual = new BigInteger("123456789") {
            @Override public int intValue() { return 250; }
        };
        assertEquals(expected.byteValue(), actual.byteValue());
        assertEquals(expected.shortValue(), actual.shortValue());
    }
    static class JTrace extends java.math.BigInteger {
        String trace = "";
        JTrace(String value) { super(value); }
        @Override public java.math.BigInteger abs() { trace += "abs;"; return super.abs(); }
        @Override public java.math.BigInteger shiftRight(int n) { trace += "shiftRight;"; return super.shiftRight(n); }
        @Override public boolean testBit(int n) { trace += "testBit;"; return super.testBit(n); }
        @Override public long longValue() { trace += "longValue;"; return super.longValue(); }
        @Override public int bitLength() { trace += "bitLength;"; return super.bitLength(); }
        @Override public int getLowestSetBit() { trace += "lowest;"; return super.getLowestSetBit(); }
    }
    static class PTrace extends BigInteger {
        String trace = "";
        PTrace(String value) { super(value); }
        @Override public BigInteger abs() { trace += "abs;"; return super.abs(); }
        @Override public BigInteger shiftRight(int n) { trace += "shiftRight;"; return super.shiftRight(n); }
        @Override public boolean testBit(int n) { trace += "testBit;"; return super.testBit(n); }
        @Override public long longValue() { trace += "longValue;"; return super.longValue(); }
        @Override public int bitLength() { trace += "bitLength;"; return super.bitLength(); }
        @Override public int getLowestSetBit() { trace += "lowest;"; return super.getLowestSetBit(); }
    }
    @Test public void floatingConversionCallTracesMatch() {
        java.util.Set<String> differences = new java.util.LinkedHashSet<>();
        for (int bits : new int[] {0, 1, 23, 24, 25, 52, 53, 54, 62, 63, 64, 65, 127, 128, 129, 1023, 1024, 1025}) {
            for (long offset : new long[] {-1, 0, 1}) for (boolean negative : new boolean[] {false, true}) {
                java.math.BigInteger n = java.math.BigInteger.ONE.shiftLeft(bits).add(java.math.BigInteger.valueOf(offset));
                if (negative) n = n.negate();
                JTrace j = new JTrace(n.toString()); PTrace p = new PTrace(n.toString());
                assertEquals(Double.doubleToLongBits(j.doubleValue()), Double.doubleToLongBits(p.doubleValue()));
                if (!j.trace.equals(p.trace)) differences.add("double bits=" + bits + " expected=" + j.trace + " actual=" + p.trace);
                j.trace = ""; p.trace = "";
                assertEquals(Float.floatToIntBits(j.floatValue()), Float.floatToIntBits(p.floatValue()));
                if (!j.trace.equals(p.trace)) differences.add("float bits=" + bits + " expected=" + j.trace + " actual=" + p.trace);
            }
        }
        assertEquals(java.util.Collections.emptySet(), differences);
    }
    @Test public void intValueDoesNotUseOverriddenLongValue() {
        for (String text : new String[] {"0", "1", "-1", "4294967295", "-4294967297"}) {
            java.math.BigInteger expected = new java.math.BigInteger(text) {
                @Override public long longValue() { return 42L; }
            };
            BigInteger actual = new BigInteger(text) {
                @Override public long longValue() { return 42L; }
            };
            assertEquals(expected.intValue(), actual.intValue());
            assertEquals(expected.byteValue(), actual.byteValue());
            assertEquals(expected.shortValue(), actual.shortValue());
        }
    }
    @Test public void floatingDelegationHonorsReplacementLongWithoutDoubleRounding() {
        long[] replacements = {0L, 42L, -42L, Long.MIN_VALUE, Long.MAX_VALUE,
            (1L << 54) + (1L << 30) + 1, (1L << 62) + (1L << 38) + 1};
        for (String text : new String[] {"0", "1", "-1", "9223372036854775807", "9223372036854775808", "-9223372036854775808"}) {
            for (long replacement : replacements) {
                java.math.BigInteger expected = new java.math.BigInteger(text) {
                    @Override public long longValue() { return replacement; }
                };
                BigInteger actual = new BigInteger(text) {
                    @Override public long longValue() { return replacement; }
                };
                assertEquals(Double.doubleToLongBits(expected.doubleValue()), Double.doubleToLongBits(actual.doubleValue()));
                assertEquals(Float.floatToIntBits(expected.floatValue()), Float.floatToIntBits(actual.floatValue()));
            }
        }
    }
}

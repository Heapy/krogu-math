// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class ComparisonDispatch574Test {
    @FunctionalInterface private interface Action { Object run() throws Throwable; }
    private static void rejectsNull(String key, Action action) throws Throwable {
        try {
            Object result = action.run();
            fail(key + " returned " + result);
        } catch (NullPointerException expected) {
            System.out.println("574_COMPARISON_BOUNDARY\t" + key + "\t" + expected);
        }
    }

    private static class DirectInteger extends BigInteger {
        int entries;
        final BigInteger marker = new BigInteger("42");
        DirectInteger() { super("1"); }
        @Override public int compareTo(BigInteger value) { entries++; assertNull(value); return 17; }
        @Override public BigInteger min(BigInteger value) { entries++; assertNull(value); return marker; }
        @Override public BigInteger max(BigInteger value) { entries++; assertNull(value); return marker; }
        int superCompare(BigInteger value) { return super.compareTo(value); }
        BigInteger superMin(BigInteger value) { return super.min(value); }
        BigInteger superMax(BigInteger value) { return super.max(value); }
    }
    private static class DirectDecimal extends BigDecimal {
        int entries;
        final BigDecimal marker = new BigDecimal("42.00");
        DirectDecimal() { super("1.00"); }
        @Override public int compareTo(BigDecimal value) { entries++; assertNull(value); return 17; }
        @Override public BigDecimal min(BigDecimal value) { entries++; assertNull(value); return marker; }
        @Override public BigDecimal max(BigDecimal value) { entries++; assertNull(value); return marker; }
        int superCompare(BigDecimal value) { return super.compareTo(value); }
        BigDecimal superMin(BigDecimal value) { return super.min(value); }
        BigDecimal superMax(BigDecimal value) { return super.max(value); }
    }

    @Test public void directJavaOverridesAndReflectionRemainVirtual() throws Throwable {
        DirectInteger integer = new DirectInteger();
        DirectDecimal decimal = new DirectDecimal();
        assertEquals(17, integer.compareTo(null));
        assertSame(integer.marker, integer.min(null));
        assertSame(integer.marker, integer.max(null));
        assertEquals(17, decimal.compareTo(null));
        assertSame(decimal.marker, decimal.min(null));
        assertSame(decimal.marker, decimal.max(null));
        assertEquals(17, ComparisonNull574.virtualNull(integer, BigInteger.class, "compareTo"));
        assertSame(integer.marker, ComparisonNull574.virtualNull(integer, BigInteger.class, "min"));
        assertSame(integer.marker, ComparisonNull574.virtualNull(integer, BigInteger.class, "max"));
        assertEquals(17, ComparisonNull574.virtualNull(decimal, BigDecimal.class, "compareTo"));
        assertSame(decimal.marker, ComparisonNull574.virtualNull(decimal, BigDecimal.class, "min"));
        assertSame(decimal.marker, ComparisonNull574.virtualNull(decimal, BigDecimal.class, "max"));
        assertEquals(6, integer.entries);
        assertEquals(6, decimal.entries);
    }

    @Test public void explicitJavaSuperCallsReachAllSixLibraryEntries() throws Throwable {
        DirectInteger integer = new DirectInteger();
        DirectDecimal decimal = new DirectDecimal();
        rejectsNull("BI.super.compareTo", () -> integer.superCompare(null));
        rejectsNull("BI.super.min", () -> integer.superMin(null));
        rejectsNull("BI.super.max", () -> integer.superMax(null));
        rejectsNull("BD.super.compareTo", () -> decimal.superCompare(null));
        rejectsNull("BD.super.min", () -> decimal.superMin(null));
        rejectsNull("BD.super.max", () -> decimal.superMax(null));
        // Internal callback counts are diagnostics, distinct from direct override obligations.
        System.out.println("574_SUPER_CALLBACKS\tinteger=" + integer.entries + "\tdecimal=" + decimal.entries);
    }

    @Test public void ordinaryAndInheritedJavaAndReflectiveBoundariesRejectNull() throws Throwable {
        BigInteger[] integers = { new BigInteger("1"), new BigInteger("1") {} };
        BigDecimal[] decimals = { new BigDecimal("1.00"), new BigDecimal("1.00") {} };
        for (int route = 0; route < 2; route++) {
            BigInteger integer = integers[route];
            BigDecimal decimal = decimals[route];
            rejectsNull("BI/" + route + "/compareTo", () -> integer.compareTo(null));
            rejectsNull("BI/" + route + "/min", () -> integer.min(null));
            rejectsNull("BI/" + route + "/max", () -> integer.max(null));
            rejectsNull("BD/" + route + "/compareTo", () -> decimal.compareTo(null));
            rejectsNull("BD/" + route + "/min", () -> decimal.min(null));
            rejectsNull("BD/" + route + "/max", () -> decimal.max(null));
            for (String method : new String[] {"compareTo", "min", "max"}) {
                rejectsNull("BI/" + route + "/reflection/" + method,
                    () -> ComparisonNull574.virtualNull(integer, BigInteger.class, method));
                rejectsNull("BD/" + route + "/reflection/" + method,
                    () -> ComparisonNull574.virtualNull(decimal, BigDecimal.class, method));
            }
        }
    }
}

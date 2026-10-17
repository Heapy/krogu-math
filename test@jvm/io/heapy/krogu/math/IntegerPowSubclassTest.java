// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IntegerPowSubclassTest {
    private static class Reference extends java.math.BigInteger {
        private final StringBuilder trace;
        private final String hook;
        private final boolean returnNull;
        Reference(String text, StringBuilder trace, String hook, boolean returnNull) {
            super(text); this.trace = trace; this.hook = hook; this.returnNull = returnNull;
        }
        private boolean call(String name) {
            trace.append(name).append(';');
            if (!name.equals(hook)) return false;
            if (!returnNull) throw new IllegalStateException(name + " callback");
            return true;
        }
        @Override public boolean equals(Object other) { call("equals"); return super.equals(other); }
        @Override public java.math.BigInteger abs() { return call("abs") ? null : super.abs(); }
        @Override public java.math.BigInteger negate() { return call("negate") ? null : super.negate(); }
        @Override public java.math.BigInteger shiftRight(int n) { return call("right") ? null : super.shiftRight(n); }
        @Override public int getLowestSetBit() { call("lowest"); return super.getLowestSetBit(); }
        @Override public int bitLength() { call("bits"); return super.bitLength(); }
    }
    private static class Actual extends BigInteger {
        private final StringBuilder trace;
        private final String hook;
        private final boolean returnNull;
        Actual(String text, StringBuilder trace, String hook, boolean returnNull) {
            super(text); this.trace = trace; this.hook = hook; this.returnNull = returnNull;
        }
        private boolean call(String name) {
            trace.append(name).append(';');
            if (!name.equals(hook)) return false;
            if (!returnNull) throw new IllegalStateException(name + " callback");
            return true;
        }
        @Override public boolean equals(Object other) { call("equals"); return super.equals(other); }
        @Override public BigInteger abs() { return call("abs") ? null : super.abs(); }
        @Override public BigInteger negate() { return call("negate") ? null : super.negate(); }
        @Override public BigInteger shiftRight(int n) { return call("right") ? null : super.shiftRight(n); }
        @Override public int getLowestSetBit() { call("lowest"); return super.getLowestSetBit(); }
        @Override public int bitLength() { call("bits"); return super.bitLength(); }
    }
    private static String outcome(Callable<?> action, Object source, String text, StringBuilder trace) {
        try {
            Object result = action.call();
            String calls = trace.toString();
            return (result == source ? text : String.valueOf(result)) + "|" + (result == source) + "|" + calls;
        } catch (Exception e) { return "!" + e.getClass().getSimpleName() + "|" + trace; }
    }
    @Test public void nullableAndThrowingPowerCallbacks() {
        for (String text : new String[] {"0", "1", "-1", "2", "-2", "3", "-3", "18446744073709551617"})
            for (int exponent : new int[] {-1, 0, 1, 2, 3, 17, Integer.MAX_VALUE})
                for (String hook : new String[] {"abs", "negate", "right"})
                    for (boolean returnNull : new boolean[] {false, true}) {
                        StringBuilder et = new StringBuilder(), at = new StringBuilder();
                        Reference expected = new Reference(text, et, hook, returnNull);
                        Actual actual = new Actual(text, at, hook, returnNull);
                        assertEquals(text + "/" + exponent + "/" + hook + "/" + returnNull,
                            outcome(() -> expected.pow(exponent), expected, text, et),
                            outcome(() -> actual.pow(exponent), actual, text, at));
                    }
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IntegerGcdSubclassTest {
    private static class Reference extends java.math.BigInteger {
        private final String label, hook;
        private final StringBuilder trace;
        private final boolean returnNull;
        Reference(String text, String label, StringBuilder trace, String hook, boolean returnNull) {
            super(text); this.label = label; this.trace = trace; this.hook = hook; this.returnNull = returnNull;
        }
        private boolean call(String name) {
            trace.append(label).append('.').append(name).append(';');
            if (!name.equals(hook)) return false;
            if (!returnNull) throw new IllegalStateException(name + " callback");
            return true;
        }
        @Override public java.math.BigInteger abs() { return call("abs") ? null : super.abs(); }
        @Override public java.math.BigInteger negate() { return call("negate") ? null : super.negate(); }
        @Override public java.math.BigInteger remainder(java.math.BigInteger other) {
            call("rem"); throw new IllegalStateException("remainder callback");
        }
    }
    private static class Actual extends BigInteger {
        private final String label, hook;
        private final StringBuilder trace;
        private final boolean returnNull;
        Actual(String text, String label, StringBuilder trace, String hook, boolean returnNull) {
            super(text); this.label = label; this.trace = trace; this.hook = hook; this.returnNull = returnNull;
        }
        private boolean call(String name) {
            trace.append(label).append('.').append(name).append(';');
            if (!name.equals(hook)) return false;
            if (!returnNull) throw new IllegalStateException(name + " callback");
            return true;
        }
        @Override public BigInteger abs() { return call("abs") ? null : super.abs(); }
        @Override public BigInteger negate() { return call("negate") ? null : super.negate(); }
        @Override public BigInteger remainder(BigInteger other) {
            call("rem"); throw new IllegalStateException("remainder callback");
        }
    }
    private static String outcome(Callable<?> action, Object source, Object operand, String left,
                                  String right, StringBuilder trace, Class<?> base) {
        try {
            Object result = action.call();
            String calls = trace.toString();
            String text = result == source ? left : result == operand ? right : String.valueOf(result);
            return text + "|" + (result == source) + "|" + (result == operand) + "|"
                + (result != null && result.getClass() == base) + "|" + calls;
        } catch (Exception e) { return "!" + e.getClass().getSimpleName() + "|" + trace; }
    }
    @Test public void nullableCallbacksAndOperandsPreserveZeroBranchPrecedence() {
        for (String left : new String[] {"0", "1", "-1", "6", "-6", "123456789012345678901234567890", "-123456789012345678901234567890"})
            for (String right : new String[] {"0", "1", "-9", "12345678901234567890", "self", "null"})
                for (String hook : new String[] {"abs", "negate"}) for (boolean returnNull : new boolean[] {false, true}) {
                    StringBuilder et = new StringBuilder(), at = new StringBuilder();
                    Reference expected = new Reference(left, "L", et, hook, returnNull);
                    Actual actual = new Actual(left, "L", at, hook, returnNull);
                    java.math.BigInteger operand = right.equals("self") ? expected : right.equals("null") ? null : new Reference(right, "R", et, hook, returnNull);
                    BigInteger other = right.equals("self") ? actual : right.equals("null") ? null : new Actual(right, "R", at, hook, returnNull);
                    String rightText = right.equals("self") ? left : right;
                    assertEquals(left + "/" + right + "/" + hook + "/" + returnNull,
                        outcome(() -> expected.gcd(operand), expected, operand, left, rightText, et, java.math.BigInteger.class),
                        outcome(() -> actual.gcd(other), actual, other, left, rightText, at, BigInteger.class));
                }
    }
}

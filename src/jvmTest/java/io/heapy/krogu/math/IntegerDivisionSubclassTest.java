// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IntegerDivisionSubclassTest {
    private static class Reference extends java.math.BigInteger {
        private final StringBuilder trace;
        private final boolean returnNull;
        Reference(String text, StringBuilder trace, boolean returnNull) {
            super(text); this.trace = trace; this.returnNull = returnNull;
        }
        private void call(String method) {
            trace.append(method).append(';');
            if (!returnNull) throw new IllegalStateException(method + " callback");
        }
        @Override public java.math.BigInteger abs() { call("abs"); return null; }
        @Override public java.math.BigInteger negate() { call("negate"); return null; }
        @Override public java.math.BigInteger[] divideAndRemainder(java.math.BigInteger other) { call("pair"); return null; }
        java.math.BigInteger[] inheritedPair(java.math.BigInteger other) { return super.divideAndRemainder(other); }
    }
    private static class Actual extends BigInteger {
        private final StringBuilder trace;
        private final boolean returnNull;
        Actual(String text, StringBuilder trace, boolean returnNull) {
            super(text); this.trace = trace; this.returnNull = returnNull;
        }
        private void call(String method) {
            trace.append(method).append(';');
            if (!returnNull) throw new IllegalStateException(method + " callback");
        }
        @Override public BigInteger abs() { call("abs"); return null; }
        @Override public BigInteger negate() { call("negate"); return null; }
        @Override public BigInteger[] divideAndRemainder(BigInteger other) { call("pair"); return null; }
        BigInteger[] inheritedPair(BigInteger other) { return super.divideAndRemainder(other); }
    }
    private static String outcome(Callable<?> action, StringBuilder trace) {
        try {
            Object result = action.call();
            String calls = trace.toString();
            String text = result instanceof Object[]
                ? Arrays.stream((Object[]) result).map(String::valueOf).collect(Collectors.joining("|"))
                : String.valueOf(result);
            return text + "|" + calls;
        } catch (Exception e) { return "!" + e.getClass().getSimpleName() + "|" + trace; }
    }
    @Test public void divisionBypassesNullableCallbacksAndRejectsNullOperands() {
        String[] inputs = {"0", "1", "-1", "100", "-100", "123456789012345678901234567890", "-123456789012345678901234567890"};
        for (String left : inputs) for (String right : new String[] {"0", "1", "-3", "1073741824", "12345678901234567890", "self", "null"})
            for (boolean returnNull : new boolean[] {false, true}) for (int op = 0; op < 3; op++) {
                StringBuilder et = new StringBuilder(), at = new StringBuilder();
                Reference source = new Reference(left, et, returnNull);
                Actual actual = new Actual(left, at, returnNull);
                java.math.BigInteger operand = right.equals("self") ? source : right.equals("null") ? null : new Reference(right, et, returnNull);
                BigInteger other = right.equals("self") ? actual : right.equals("null") ? null : new Actual(right, at, returnNull);
                final int operation = op;
                assertEquals(left + "/" + right + "/" + op + "/" + returnNull,
                    outcome(() -> operation == 0 ? source.divide(operand) : operation == 1 ? source.remainder(operand) : source.inheritedPair(operand), et),
                    outcome(() -> operation == 0 ? actual.divide(other) : operation == 1 ? actual.remainder(other) : actual.inheritedPair(other), at));
            }
    }
}

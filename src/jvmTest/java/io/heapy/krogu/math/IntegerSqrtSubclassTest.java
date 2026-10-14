// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IntegerSqrtSubclassTest {
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
        @Override public java.math.BigInteger sqrt() { return call("sqrt") ? null : super.sqrt(); }
        @Override public java.math.BigInteger[] sqrtAndRemainder() { return call("pair") ? null : super.sqrtAndRemainder(); }
        @Override public java.math.BigInteger divide(java.math.BigInteger value) { return call("divide") ? null : super.divide(value); }
        @Override public java.math.BigInteger multiply(java.math.BigInteger value) { return call("multiply") ? null : super.multiply(value); }
        @Override public java.math.BigInteger subtract(java.math.BigInteger value) { return call("subtract") ? null : super.subtract(value); }
        java.math.BigInteger inheritedRoot() { return super.sqrt(); }
        java.math.BigInteger[] inheritedPair() { return super.sqrtAndRemainder(); }
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
        @Override public BigInteger sqrt() { return call("sqrt") ? null : super.sqrt(); }
        @Override public BigInteger[] sqrtAndRemainder() { return call("pair") ? null : super.sqrtAndRemainder(); }
        @Override public BigInteger divide(BigInteger value) { return call("divide") ? null : super.divide(value); }
        @Override public BigInteger multiply(BigInteger value) { return call("multiply") ? null : super.multiply(value); }
        @Override public BigInteger subtract(BigInteger value) { return call("subtract") ? null : super.subtract(value); }
        BigInteger inheritedRoot() { return super.sqrt(); }
        BigInteger[] inheritedPair() { return super.sqrtAndRemainder(); }
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
    @Test public void inheritedRootApisBypassNullableOrThrowingCallbacks() {
        for (String text : new String[] {"-4", "-1", "0", "1", "2", "3", "4", "9", "18446744073709551617"})
            for (String hook : new String[] {"sqrt", "pair", "divide", "multiply", "subtract"})
                for (boolean returnNull : new boolean[] {false, true}) for (boolean pair : new boolean[] {false, true}) {
                    StringBuilder et = new StringBuilder(), at = new StringBuilder();
                    Reference expected = new Reference(text, et, hook, returnNull);
                    Actual actual = new Actual(text, at, hook, returnNull);
                    assertEquals(text + "/" + hook + "/" + returnNull + "/" + pair,
                        outcome(() -> pair ? expected.inheritedPair() : expected.inheritedRoot(), et),
                        outcome(() -> pair ? actual.inheritedPair() : actual.inheritedRoot(), at));
                }
    }
}

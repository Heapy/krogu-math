// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DecimalContextNullDelegationTest {
    private static class Reference extends java.math.BigDecimal {
        final String behavior;
        final StringBuilder trace = new StringBuilder();
        Reference(String text, String behavior) { super(text); this.behavior = behavior; }
        @Override public java.math.BigDecimal divide(java.math.BigDecimal value) {
            trace.append("exact(").append(value == null).append(");");
            return switch (behavior) {
                case "return" -> new java.math.BigDecimal("7.00");
                case "self" -> this;
                case "null" -> null;
                case "throw" -> throw new IllegalStateException("exact callback");
                default -> super.divide(value);
            };
        }
        java.math.BigDecimal enter(java.math.BigDecimal value, java.math.MathContext context) {
            return super.divide(value, context);
        }
    }
    private static class Actual extends BigDecimal {
        final String behavior;
        final StringBuilder trace = new StringBuilder();
        Actual(String text, String behavior) { super(text); this.behavior = behavior; }
        @Override public BigDecimal divide(BigDecimal value) {
            trace.append("exact(").append(value == null).append(");");
            return switch (behavior) {
                case "return" -> new BigDecimal("7.00");
                case "self" -> this;
                case "null" -> null;
                case "throw" -> throw new IllegalStateException("exact callback");
                default -> super.divide(value);
            };
        }
        BigDecimal enter(BigDecimal value, MathContext context) { return super.divide(value, context); }
    }
    private static String outcome(Callable<?> action, Object source, StringBuilder trace) {
        try {
            Object value = action.call();
            return (value == source ? "source" : String.valueOf(value)) + "|" + trace;
        } catch (Exception e) {
            return "!" + e.getClass().getSimpleName() +
                (e instanceof IllegalStateException ? ":" + e.getMessage() : "") + "|" + trace;
        }
    }
    @Test public void zeroPrecisionDelegatesBeforeNullableOperandValidation() {
        for (String text : new String[] {"0.00", "1", "-10.50"})
            for (String behavior : new String[] {"return", "self", "null", "throw", "super"})
                for (int precision : new int[] {-1, 0, 2})
                    for (boolean nullOperand : new boolean[] {false, true}) {
                        Reference expected = new Reference(text, behavior);
                        Actual actual = new Actual(text, behavior);
                        java.math.BigDecimal ev = nullOperand ? null : new java.math.BigDecimal("2.0");
                        BigDecimal av = nullOperand ? null : new BigDecimal("2.0");
                        java.math.MathContext em = precision < 0 ? null : new java.math.MathContext(precision);
                        MathContext am = precision < 0 ? null : new MathContext(precision);
                        assertEquals(text + "/" + behavior + "/" + precision + "/" + nullOperand,
                            outcome(() -> expected.enter(ev, em), expected, expected.trace),
                            outcome(() -> actual.enter(av, am), actual, actual.trace));
                    }
    }
}

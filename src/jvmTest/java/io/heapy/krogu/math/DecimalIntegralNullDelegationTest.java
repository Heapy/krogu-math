// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;
import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class DecimalIntegralNullDelegationTest {
private static class ReferenceQuotient extends java.math.BigDecimal {
    final StringBuilder trace;
    ReferenceQuotient(StringBuilder trace) { super("7.00"); this.trace = trace; }
    @Override public java.math.BigDecimal multiply(java.math.BigDecimal value) {
        trace.append("multiply:").append(value == null).append(';');
        return new java.math.BigDecimal("12.0");
    }
}
private static class Reference extends java.math.BigDecimal {
    final String hook, behavior;
    final StringBuilder trace = new StringBuilder();
    Reference(String text, String hook, String behavior) { super(text); this.hook = hook; this.behavior = behavior; }
    private void call(java.math.BigDecimal value, java.math.MathContext context) {
        trace.append(hook).append(':').append(value == null).append(':').append(context == null).append(';');
        if (behavior.equals("throw")) throw new IllegalStateException("division callback");
    }
    private java.math.BigDecimal replacement() {
        if (behavior.equals("null")) return null;
        return behavior.equals("self") ? this : new ReferenceQuotient(trace);
    }
    private java.math.BigDecimal[] pairReplacement() {
        if (behavior.equals("null")) return null;
        if (behavior.equals("empty")) return new java.math.BigDecimal[0];
        if (behavior.equals("short")) return new java.math.BigDecimal[] {this};
        if (behavior.equals("null-element")) return new java.math.BigDecimal[] {this, null};
        return new java.math.BigDecimal[] {this, behavior.equals("self") ? this : new java.math.BigDecimal("9.00")};
    }
    @Override public java.math.BigDecimal divideToIntegralValue(java.math.BigDecimal value) {
        if (!hook.equals("integral")) return super.divideToIntegralValue(value);
        call(value, null);
        return behavior.equals("super") ? super.divideToIntegralValue(value) : replacement();
    }
    @Override public java.math.BigDecimal divideToIntegralValue(java.math.BigDecimal value, java.math.MathContext context) {
        if (!hook.equals("integral-context")) return super.divideToIntegralValue(value, context);
        call(value, context);
        return behavior.equals("super") ? super.divideToIntegralValue(value, context) : replacement();
    }
    @Override public java.math.BigDecimal[] divideAndRemainder(java.math.BigDecimal value) {
        if (!hook.equals("pair")) return super.divideAndRemainder(value);
        call(value, null);
        return behavior.equals("super") ? super.divideAndRemainder(value) : pairReplacement();
    }
    @Override public java.math.BigDecimal[] divideAndRemainder(java.math.BigDecimal value, java.math.MathContext context) {
        if (!hook.equals("pair-context")) return super.divideAndRemainder(value, context);
        call(value, context);
        return behavior.equals("super") ? super.divideAndRemainder(value, context) : pairReplacement();
    }
    @Override public java.math.BigDecimal subtract(java.math.BigDecimal value) {
        trace.append("subtract;"); return super.subtract(value);
    }
    Object enter(String operation, java.math.BigDecimal value, java.math.MathContext context) {
        return switch (operation) {
            case "integral" -> super.divideToIntegralValue(value);
            case "integral-context" -> super.divideToIntegralValue(value, context);
            case "pair" -> super.divideAndRemainder(value);
            case "pair-context" -> super.divideAndRemainder(value, context);
            case "remainder" -> super.remainder(value);
            default -> super.remainder(value, context);
        };
    }
}
private static class ActualQuotient extends BigDecimal {
    final StringBuilder trace;
    ActualQuotient(StringBuilder trace) { super("7.00"); this.trace = trace; }
    @Override public BigDecimal multiply(BigDecimal value) {
        trace.append("multiply:").append(value == null).append(';');
        return new BigDecimal("12.0");
    }
}
private static class Actual extends BigDecimal {
    final String hook, behavior;
    final StringBuilder trace = new StringBuilder();
    Actual(String text, String hook, String behavior) { super(text); this.hook = hook; this.behavior = behavior; }
    private void call(BigDecimal value, MathContext context) {
        trace.append(hook).append(':').append(value == null).append(':').append(context == null).append(';');
        if (behavior.equals("throw")) throw new IllegalStateException("division callback");
    }
    private BigDecimal replacement() {
        if (behavior.equals("null")) return null;
        return behavior.equals("self") ? this : new ActualQuotient(trace);
    }
    private BigDecimal[] pairReplacement() {
        if (behavior.equals("null")) return null;
        if (behavior.equals("empty")) return new BigDecimal[0];
        if (behavior.equals("short")) return new BigDecimal[] {this};
        if (behavior.equals("null-element")) return new BigDecimal[] {this, null};
        return new BigDecimal[] {this, behavior.equals("self") ? this : new BigDecimal("9.00")};
    }
    @Override public BigDecimal divideToIntegralValue(BigDecimal value) {
        if (!hook.equals("integral")) return super.divideToIntegralValue(value);
        call(value, null);
        return behavior.equals("super") ? super.divideToIntegralValue(value) : replacement();
    }
    @Override public BigDecimal divideToIntegralValue(BigDecimal value, MathContext context) {
        if (!hook.equals("integral-context")) return super.divideToIntegralValue(value, context);
        call(value, context);
        return behavior.equals("super") ? super.divideToIntegralValue(value, context) : replacement();
    }
    @Override public BigDecimal[] divideAndRemainder(BigDecimal value) {
        if (!hook.equals("pair")) return super.divideAndRemainder(value);
        call(value, null);
        return behavior.equals("super") ? super.divideAndRemainder(value) : pairReplacement();
    }
    @Override public BigDecimal[] divideAndRemainder(BigDecimal value, MathContext context) {
        if (!hook.equals("pair-context")) return super.divideAndRemainder(value, context);
        call(value, context);
        return behavior.equals("super") ? super.divideAndRemainder(value, context) : pairReplacement();
    }
    @Override public BigDecimal subtract(BigDecimal value) {
        trace.append("subtract;"); return super.subtract(value);
    }
    Object enter(String operation, BigDecimal value, MathContext context) {
        return switch (operation) {
            case "integral" -> super.divideToIntegralValue(value);
            case "integral-context" -> super.divideToIntegralValue(value, context);
            case "pair" -> super.divideAndRemainder(value);
            case "pair-context" -> super.divideAndRemainder(value, context);
            case "remainder" -> super.remainder(value);
            default -> super.remainder(value, context);
        };
    }
}
    private static String describe(Object value, Object source) {
        if (value == source) return "source";
        if (value instanceof Object[] values) {
            StringBuilder text = new StringBuilder("[");
            for (Object item : values) text.append(describe(item, source)).append(',');
            return text.append(']').toString();
        }
        return String.valueOf(value);
    }
    private static String outcome(Callable<?> action, Object source, StringBuilder trace) {
        try { return describe(action.call(), source) + "|" + trace; }
        catch (Exception e) {
            return "!" + e.getClass().getSimpleName() +
                (e instanceof IllegalStateException ? ":" + e.getMessage() : "") + "|" + trace;
        }
    }
    @Test public void nullableArgumentsAndCallbackResultsPreserveInheritedComposition() {
        int comparisons = 0, differences = 0, baseNull = 0;
        StringBuilder failures = new StringBuilder();
        for (String text : new String[] {"0.00", "1", "-10.50"})
            for (String behavior : new String[] {"return", "self", "null", "throw", "super", "empty", "short", "null-element"})
                for (String hook : new String[] {"integral", "integral-context", "pair", "pair-context"})
                    for (String op : new String[] {"integral", "integral-context", "pair", "pair-context", "remainder", "remainder-context"})
                        for (int precision : new int[] {-1, 0, 2})
                            for (boolean nullOperand : new boolean[] {false, true}) {
                                Reference expected = new Reference(text, hook, behavior);
                                Actual actual = new Actual(text, hook, behavior);
                                java.math.BigDecimal ev = nullOperand ? null : new java.math.BigDecimal("2.0");
                                BigDecimal av = nullOperand ? null : new BigDecimal("2.0");
                                java.math.MathContext em = precision < 0 ? null : new java.math.MathContext(precision);
                                MathContext am = precision < 0 ? null : new MathContext(precision);
                                String e = outcome(() -> expected.enter(op, ev, em), expected, expected.trace);
                                String a = outcome(() -> actual.enter(op, av, am), actual, actual.trace);
                                comparisons++;
                                if (nullOperand || op.endsWith("-context") && precision == -1) {
                                    baseNull++;
                                    String key = text + "/" + behavior + "/" + hook + "/" + op + "/" + precision + "/" + nullOperand;
                                    System.out.println("575_EXISTING_INTEGRAL\t" + key + "\tEXPLICIT_SUPER_NULL_CALLBACK_OR_ARRAY_DIAGNOSTIC\thost=" + e + "\town=" + a);
                                    assertEquals(key, "!NullPointerException|", a);
                                    continue;
                                }
                                if (!e.equals(a)) {
                                    differences++;
                                    if (differences <= 12) failures.append(text).append('/').append(behavior).append('/').append(hook)
                                        .append('/').append(op).append('/').append(precision).append('/').append(nullOperand)
                                        .append(" expected=").append(e).append(" actual=").append(a).append('\n');
                                }
                            }
        assertEquals("comparison count", 3456, comparisons);
        assertEquals("exact null-input partition", 2016, baseNull);
        assertEquals(differences + " mismatches:\n" + failures, 0, differences);
    }
}

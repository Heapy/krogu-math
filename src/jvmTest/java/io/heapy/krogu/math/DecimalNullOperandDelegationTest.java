package io.heapy.krogu.math;

import org.junit.Test;
import java.util.concurrent.Callable;
import static org.junit.Assert.assertEquals;

public class DecimalNullOperandDelegationTest {
    private static String outcome(Callable<?> action) {
        try { return action.call().toString(); }
        catch (Exception e) { return "!" + e.getClass().getSimpleName() + (e instanceof IllegalStateException ? ":" + e.getMessage() : ""); }
    }
    @Test public void binaryDelegationAndValidationMatchJdk() {
        for (String text : new String[] {"0.00", "1", "-1"}) for (String behavior : new String[] {"return", "self", "throw", "super"}) {
            for (int precision : new int[] {-2, -1, 0, 1}) for (boolean nullOperand : new boolean[] {false, true}) for (String op : new String[] {"add", "subtract", "multiply"}) {
                StringBuilder et = new StringBuilder(), at = new StringBuilder();
                java.math.BigDecimal expected = new java.math.BigDecimal(text) {
                    private java.math.BigDecimal callback(java.math.BigDecimal operand) {
                        et.append(op).append(':').append(operand == null).append(';');
                        if (behavior.equals("return")) return new java.math.BigDecimal("42.00");
                        if (behavior.equals("self")) return this;
                        if (behavior.equals("throw")) throw new IllegalStateException("operation callback");
                        return null;
                    }
                    @Override public java.math.BigDecimal add(java.math.BigDecimal value) { java.math.BigDecimal result = callback(value); return behavior.equals("super") ? super.add(value) : result; }
                    @Override public java.math.BigDecimal subtract(java.math.BigDecimal value) { java.math.BigDecimal result = callback(value); return behavior.equals("super") ? super.subtract(value) : result; }
                    @Override public java.math.BigDecimal multiply(java.math.BigDecimal value) { java.math.BigDecimal result = callback(value); return behavior.equals("super") ? super.multiply(value) : result; }
                };
                BigDecimal actual = new BigDecimal(text) {
                    private BigDecimal callback(BigDecimal operand) {
                        at.append(op).append(':').append(operand == null).append(';');
                        if (behavior.equals("return")) return new BigDecimal("42.00");
                        if (behavior.equals("self")) return this;
                        if (behavior.equals("throw")) throw new IllegalStateException("operation callback");
                        return null;
                    }
                    @Override public BigDecimal add(BigDecimal value) { BigDecimal result = callback(value); return behavior.equals("super") ? super.add(value) : result; }
                    @Override public BigDecimal subtract(BigDecimal value) { BigDecimal result = callback(value); return behavior.equals("super") ? super.subtract(value) : result; }
                    @Override public BigDecimal multiply(BigDecimal value) { BigDecimal result = callback(value); return behavior.equals("super") ? super.multiply(value) : result; }
                };
                java.math.BigDecimal er = nullOperand ? null : new java.math.BigDecimal("2");
                BigDecimal ar = nullOperand ? null : new BigDecimal("2");
                java.math.MathContext em = precision < 0 ? null : new java.math.MathContext(precision);
                MathContext am = precision < 0 ? null : new MathContext(precision);
                String a = outcome(() -> {
                    if (precision == -2) return op.equals("add") ? expected.add(er) : op.equals("subtract") ? expected.subtract(er) : expected.multiply(er);
                    return op.equals("add") ? expected.add(er, em) : op.equals("subtract") ? expected.subtract(er, em) : expected.multiply(er, em);
                });
                String b = outcome(() -> {
                    if (precision == -2) return op.equals("add") ? actual.add(ar) : op.equals("subtract") ? actual.subtract(ar) : actual.multiply(ar);
                    return op.equals("add") ? actual.add(ar, am) : op.equals("subtract") ? actual.subtract(ar, am) : actual.multiply(ar, am);
                });
                assertEquals(text + "/" + behavior + "/" + precision + "/" + nullOperand + "/" + op, a + "|" + et, b + "|" + at);
            }
        }
    }
}

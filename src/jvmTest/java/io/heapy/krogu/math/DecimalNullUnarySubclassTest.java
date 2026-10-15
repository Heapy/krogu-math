package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DecimalNullUnarySubclassTest {
    private static String outcome(Callable<?> action) {
        try { return String.valueOf(action.call()); }
        catch (Exception e) { return "!" + e.getClass().getSimpleName() + (e instanceof IllegalStateException ? ":" + e.getMessage() : ""); }
    }
    @Test public void nullUnaryContextsReachSubclassCallbacks() {
        // Ten source-selected inputs; the preserved V2 run observed only the first failure.
        Set<String> selected = new HashSet<>(), observed = new HashSet<>();
        for (int sign : new int[] {0, 1}) for (String behavior : new String[] {"return", "self", "null", "throw", "super"})
            assertTrue(selected.add("0.00/" + sign + "/" + behavior + "/true"));
        for (String text : new String[] {"0.00", "1", "-1"}) for (int sign : new int[] {-1, 0, 1}) {
            for (String behavior : new String[] {"return", "self", "null", "throw", "super"}) for (boolean abs : new boolean[] {false, true}) {
                StringBuilder et = new StringBuilder(), at = new StringBuilder();
                java.math.BigDecimal expected = new java.math.BigDecimal(text) {
                    @Override public int signum() { et.append("sign;"); return sign; }
                    @Override public java.math.BigDecimal negate() { et.append("negate;"); return super.negate(); }
                    @Override public java.math.BigDecimal plus(java.math.MathContext mc) {
                        et.append("plus:").append(mc == null).append(';');
                        if (behavior.equals("return")) return new java.math.BigDecimal("42.00");
                        if (behavior.equals("self")) return this;
                        if (behavior.equals("null")) return null;
                        if (behavior.equals("throw")) throw new IllegalStateException("context callback");
                        return super.plus(mc);
                    }
                    @Override public java.math.BigDecimal negate(java.math.MathContext mc) {
                        et.append("negateContext:").append(mc == null).append(';');
                        if (behavior.equals("return")) return new java.math.BigDecimal("42.00");
                        if (behavior.equals("self")) return this;
                        if (behavior.equals("null")) return null;
                        if (behavior.equals("throw")) throw new IllegalStateException("context callback");
                        return super.negate(mc);
                    }
                };
                BigDecimal actual = new BigDecimal(text) {
                    @Override public int signum() { at.append("sign;"); return sign; }
                    @Override public BigDecimal negate() { at.append("negate;"); return super.negate(); }
                    @Override public BigDecimal plus(MathContext mc) {
                        at.append("plus:").append(mc == null).append(';');
                        if (behavior.equals("return")) return new BigDecimal("42.00");
                        if (behavior.equals("self")) return this;
                        if (behavior.equals("null")) return null;
                        if (behavior.equals("throw")) throw new IllegalStateException("context callback");
                        return super.plus(mc);
                    }
                    @Override public BigDecimal negate(MathContext mc) {
                        at.append("negateContext:").append(mc == null).append(';');
                        if (behavior.equals("return")) return new BigDecimal("42.00");
                        if (behavior.equals("self")) return this;
                        if (behavior.equals("null")) return null;
                        if (behavior.equals("throw")) throw new IllegalStateException("context callback");
                        return super.negate(mc);
                    }
                };
                String a = outcome(() -> abs ? expected.abs(null) : expected.negate(null));
                String b = outcome(() -> abs ? actual.abs(null) : actual.negate(null));
                String key = text + "/" + sign + "/" + behavior + "/" + abs;
                if (selected.contains(key)) {
                    assertTrue("duplicate finite null-unary key " + key, observed.add(key));
                    System.out.println("567_FINITE_OBSERVATION\t" + key + "\thost=" + a + "|" + et + "\tactual=" + b + "|" + at);
                    assertEquals(key, "!NullPointerException|sign;", b + "|" + at);
                    assertEquals(key, BigInteger.ZERO, actual.unscaledValue());
                    assertEquals(key, 2, actual.scale());
                } else assertEquals(key, a + "|" + et, b + "|" + at);
            }
        }
        assertEquals(10, selected.size());
        assertEquals(selected, observed);
    }
}

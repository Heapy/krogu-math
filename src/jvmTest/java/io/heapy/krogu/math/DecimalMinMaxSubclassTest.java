package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DecimalMinMaxSubclassTest {
    private static String outcome(Callable<?> action, Object source, Object operand) {
        try { Object result = action.call(); return String.valueOf(result) + "|" + (result == source) + "|" + (result == operand); }
        catch (Exception e) { return "!" + e.getClass().getSimpleName() + (e instanceof IllegalStateException ? ":" + e.getMessage() : ""); }
    }
    @Test public void minMaxForwardArgumentsToVirtualComparison() {
        for (String text : new String[] {"0.00", "1", "-1"}) for (int comparison : new int[] {-2, -1, 0, 1, 2}) {
            for (boolean throwing : new boolean[] {false, true}) for (boolean nullOperand : new boolean[] {false, true}) for (boolean min : new boolean[] {false, true}) {
                StringBuilder et = new StringBuilder(), at = new StringBuilder();
                java.math.BigDecimal expected = new java.math.BigDecimal(text) {
                    @Override public int compareTo(java.math.BigDecimal value) {
                        et.append("compare:").append(value == null).append(';');
                        if (throwing) throw new IllegalStateException("comparison callback");
                        return comparison;
                    }
                };
                BigDecimal actual = new BigDecimal(text) {
                    @Override public int compareTo(BigDecimal value) {
                        at.append("compare:").append(value == null).append(';');
                        if (throwing) throw new IllegalStateException("comparison callback");
                        return comparison;
                    }
                };
                java.math.BigDecimal er = nullOperand ? null : new java.math.BigDecimal("2");
                BigDecimal ar = nullOperand ? null : new BigDecimal("2");
                String a = outcome(() -> min ? expected.min(er) : expected.max(er), expected, er);
                String b = outcome(() -> min ? actual.min(ar) : actual.max(ar), actual, ar);
                assertEquals(text + "/" + comparison + "/" + throwing + "/" + nullOperand + "/" + min, a + "|" + et, b + "|" + at);
            }
        }
    }
    @Test public void javaComparableAndCollectionSortingRemainUsable() {
        Comparable<BigDecimal> value = new BigDecimal("1");
        assertEquals(-1, value.compareTo(new BigDecimal("2")));
        java.util.List<BigDecimal> values = new java.util.ArrayList<>(java.util.List.of(new BigDecimal("2"), new BigDecimal("-1"), new BigDecimal("0.0"), new BigDecimal("1")));
        java.util.Collections.sort(values);
        assertEquals("[-1, 0.0, 1, 2]", values.toString());
    }
}

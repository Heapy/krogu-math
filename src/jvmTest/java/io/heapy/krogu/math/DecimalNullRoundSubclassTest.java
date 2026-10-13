package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DecimalNullRoundSubclassTest {
    private static String outcome(Callable<?> action) {
        try { return action.call().toString(); }
        catch (Exception error) {
            return "!" + error.getClass().getSimpleName()
                + (error instanceof IllegalStateException ? ":" + error.getMessage() : "");
        }
    }

    @Test public void nullContextReachesOverriddenPlusBeforeValidation() {
        for (String text : new String[] {"0.00", "-123.45"}) {
            for (String behavior : new String[] {"return", "self", "throw", "super"}) for (boolean round : new boolean[] {false, true}) {
                StringBuilder expectedTrace = new StringBuilder();
                StringBuilder actualTrace = new StringBuilder();
                java.math.BigDecimal expected = new java.math.BigDecimal(text) {
                    @Override public java.math.BigDecimal plus(java.math.MathContext context) {
                        expectedTrace.append("plus:").append(context == null).append(';');
                        if (behavior.equals("return")) return new java.math.BigDecimal("42.00");
                        if (behavior.equals("self")) return this;
                        if (behavior.equals("throw")) throw new IllegalStateException("plus callback");
                        return super.plus(context);
                    }
                };
                BigDecimal actual = new BigDecimal(text) {
                    @Override public BigDecimal plus(MathContext context) {
                        actualTrace.append("plus:").append(context == null).append(';');
                        if (behavior.equals("return")) return new BigDecimal("42.00");
                        if (behavior.equals("self")) return this;
                        if (behavior.equals("throw")) throw new IllegalStateException("plus callback");
                        return super.plus(context);
                    }
                };
                String a = outcome(() -> round ? expected.round(null) : expected.plus(null));
                String b = outcome(() -> round ? actual.round(null) : actual.plus(null));
                assertEquals(text + "/" + behavior + "/" + round, a + "|" + expectedTrace, b + "|" + actualTrace);
            }
        }
    }
}

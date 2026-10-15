package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ZeroNullForwarding567Test {
    // Null violates the override's value contract. This tests explicit Java dispatch only.
    private static final class NullPlus extends BigDecimal {
        final StringBuilder trace = new StringBuilder();
        NullPlus() { super("0.00"); }
        @Override public BigDecimal plus(MathContext context) {
            trace.append("plus:").append(context == null).append(';');
            return null;
        }
        BigDecimal callerSuperRound(MathContext context) { return super.round(context); }
    }

    @Test public void explicitZeroPlusRoundAndCallerSuperRoundRetainNull() {
        for (MathContext context : new MathContext[] {null, new MathContext(0), new MathContext(1), new MathContext(7)}) {
            for (int operation = 0; operation < 3; operation++) {
                NullPlus value = new NullPlus();
                BigDecimal result = operation == 0 ? value.plus(context)
                    : operation == 1 ? value.round(context) : value.callerSuperRound(context);
                assertNull(result);
                assertEquals("plus:" + (context == null) + ";", value.trace.toString());
                assertEquals(BigInteger.ZERO, value.unscaledValue());
                assertEquals(2, value.scale());
            }
        }
    }
}

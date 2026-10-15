package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @Test public void explicitZeroPlusKeepsNullReturnAndBaseRoundRejectsNullInput() {
        Set<String> expected = new LinkedHashSet<>(), observed = new LinkedHashSet<>();
        for (String context : new String[] {"null", "0", "1", "7"}) for (int op = 0; op < 3; op++) expected.add(context + "/" + op);
        for (MathContext context : new MathContext[] {null, new MathContext(0), new MathContext(1), new MathContext(7)}) {
            for (int operation = 0; operation < 3; operation++) {
                NullPlus value = new NullPlus();
                String key = (context == null ? "null" : String.valueOf(context.getPrecision())) + "/" + operation;
                assertTrue(observed.add(key));
                if (context == null && operation != 0) {
                    final int route = operation;
                    assertThrows(NullPointerException.class, () -> {
                        if (route == 1) value.round(null); else value.callerSuperRound(null);
                    });
                    System.out.println("576_ZERO_NULL_FORWARD\t" + key + "\thistorical=null|plus:true;\tactual=NPE|" + value.trace);
                } else {
                    BigDecimal result = operation == 0 ? value.plus(context)
                        : operation == 1 ? value.round(context) : value.callerSuperRound(context);
                    assertNull(result);
                    assertEquals("plus:" + (context == null) + ";", value.trace.toString());
                }
                assertEquals(BigInteger.ZERO, value.unscaledValue());
                assertEquals(2, value.scale());
            }
        }
        assertEquals(12, observed.size()); assertEquals(expected, observed);
    }
}

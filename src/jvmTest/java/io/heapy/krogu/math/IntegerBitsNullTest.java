// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IntegerBitsNullTest {
    private static class Reference extends java.math.BigInteger {
        final StringBuilder trace = new StringBuilder();
        final boolean fail;
        Reference(String text, boolean fail) { super(text); this.fail = fail; }
        @Override public int bitLength() {
            trace.append("bits;");
            if (fail) throw new IllegalStateException("bits callback");
            return super.bitLength();
        }
    }
    private static class Actual extends BigInteger {
        final StringBuilder trace = new StringBuilder();
        final boolean fail;
        Actual(String text, boolean fail) { super(text); this.fail = fail; }
        @Override public int bitLength() {
            trace.append("bits;");
            if (fail) throw new IllegalStateException("bits callback");
            return super.bitLength();
        }
    }
    private static String outcome(Callable<?> action, StringBuilder trace) {
        try { Object value = action.call(); return value + "|" + trace; }
        catch (Exception e) { return "!" + e.getClass().getSimpleName() + "|" + trace; }
    }
    @Test public void sourceMetadataRunsBeforeNullOperandFailure() {
        for (String text : new String[] {"0", "-1", "18446744073709551617"})
            for (boolean fail : new boolean[] {false, true}) for (int op = 0; op < 4; op++) {
                Reference expected = new Reference(text, fail);
                Actual actual = new Actual(text, fail);
                final int operation = op;
                assertEquals(text + "/" + fail + "/" + op,
                    outcome(() -> switch (operation) {
                        case 0 -> expected.and(null); case 1 -> expected.or(null);
                        case 2 -> expected.xor(null); default -> expected.andNot(null);
                    }, expected.trace),
                    outcome(() -> switch (operation) {
                        case 0 -> actual.and(null); case 1 -> actual.or(null);
                        case 2 -> actual.xor(null); default -> actual.andNot(null);
                    }, actual.trace));
            }
    }
}

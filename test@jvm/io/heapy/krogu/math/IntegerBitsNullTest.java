// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

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
        BigInteger explicitSuperNull(int operation) {
            return switch (operation) {
                case 0 -> super.and(null); case 1 -> super.or(null);
                case 2 -> super.xor(null); case 3 -> super.andNot(null);
                default -> throw new AssertionError("Unknown operation " + operation);
            };
        }
    }
    // Frozen loop order from published60. These keys are checked against actual executions.
    private static final List<String> NULL_KEYS = Arrays.asList(
        "0/false/0", "0/false/1", "0/false/2", "0/false/3",
        "0/true/0", "0/true/1", "0/true/2", "0/true/3",
        "-1/false/0", "-1/false/1", "-1/false/2", "-1/false/3",
        "-1/true/0", "-1/true/1", "-1/true/2", "-1/true/3",
        "18446744073709551617/false/0", "18446744073709551617/false/1",
        "18446744073709551617/false/2", "18446744073709551617/false/3",
        "18446744073709551617/true/0", "18446744073709551617/true/1",
        "18446744073709551617/true/2", "18446744073709551617/true/3");

    private static BigInteger nullOperand(BigInteger receiver, int operation) {
        return switch (operation) {
            case 0 -> receiver.and(null); case 1 -> receiver.or(null);
            case 2 -> receiver.xor(null); case 3 -> receiver.andNot(null);
            default -> throw new AssertionError("Unknown operation " + operation);
        };
    }
    private static String outcome(Callable<?> action, StringBuilder trace) {
        try { Object value = action.call(); return value + "|" + trace; }
        catch (Exception e) { return "!" + e.getClass().getSimpleName() + "|" + trace; }
    }
    @Test public void baseRejectsNullAndRetainsCallbackFirstDiagnostics() {
        List<String> executed = new ArrayList<>();
        for (String text : new String[] {"0", "-1", "18446744073709551617"})
            for (boolean fail : new boolean[] {false, true}) for (int op = 0; op < 4; op++) {
                Reference expected = new Reference(text, fail);
                Actual actual = new Actual(text, fail);
                final int operation = op;
                String key = text + "/" + fail + "/" + op;
                assertEquals(NULL_KEYS.get(executed.size()), key);
                executed.add(key);
                String observedHost = outcome(() -> switch (operation) {
                        case 0 -> expected.and(null); case 1 -> expected.or(null);
                        case 2 -> expected.xor(null); default -> expected.andNot(null);
                    }, expected.trace);
                NullPointerException rejected = assertThrows(key, NullPointerException.class,
                    () -> nullOperand(actual, operation));
                // Historical callback-first outcomes remain diagnostic; no host-equality waiver.
                String historical = fail ? "!IllegalStateException|bits;" : "!NullPointerException|bits;";
                System.out.println("bits577 ordinal=" + (executed.size() - 1) + " key=" + key
                    + " frozenCallbackFirst=" + historical + " liveHost=" + observedHost
                    + " required=!NullPointerException actual=!" + rejected.getClass().getSimpleName()
                    + "|" + actual.trace + " message=" + rejected.getMessage());
            }
        assertEquals(24, executed.size());
        assertEquals(NULL_KEYS, executed);
    }

    private static class Direct extends Actual {
        final BigInteger replacement = new BigInteger("701");
        final IllegalStateException marker = new IllegalStateException("direct bitwise override");
        boolean throwMarker;
        Direct() { super("-1", true); }
        private BigInteger body(String method, BigInteger value) {
            trace.append(method).append(';');
            assertNull(value);
            if (throwMarker) throw marker;
            return replacement;
        }
        @Override public BigInteger and(BigInteger value) { return body("and", value); }
        @Override public BigInteger or(BigInteger value) { return body("or", value); }
        @Override public BigInteger xor(BigInteger value) { return body("xor", value); }
        @Override public BigInteger andNot(BigInteger value) { return body("andNot", value); }
    }

    @Test public void ordinaryEntryDirectJavaOverrideAndRealSuperAreDistinct() {
        String[] methods = {"and", "or", "xor", "andNot"};
        for (int op = 0; op < methods.length; op++) {
            final int operation = op;
            assertThrows(methods[op], NullPointerException.class,
                () -> nullOperand(new BigInteger("-1"), operation));
            Direct direct = new Direct();
            BigInteger baseTyped = direct;
            assertSame(direct.replacement, nullOperand(baseTyped, operation));
            assertEquals(methods[op] + ";", direct.trace.toString());
            direct.trace.setLength(0);
            direct.throwMarker = true;
            assertSame(direct.marker, assertThrows(IllegalStateException.class,
                () -> nullOperand(baseTyped, operation)));
            assertEquals(methods[op] + ";", direct.trace.toString());
            direct.trace.setLength(0);
            // Actual's explicit super invokes BigInteger, bypassing Direct's throwing override.
            assertThrows(NullPointerException.class, () -> direct.explicitSuperNull(operation));
            System.out.println("bits577 super=" + methods[op] + " trace=" + direct.trace);
        }
    }
}

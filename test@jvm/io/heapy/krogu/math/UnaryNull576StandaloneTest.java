// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exact null-call counterparts of ZeroContextScale567Test and ZeroUnaryPrecision567Test.
 * Valid-context mathematical tests remain common. Every super route here is Java invokespecial.
 */
public class UnaryNull576StandaloneTest {
    private static final int[] SCALES = {Integer.MIN_VALUE, -7, -1, 0, 1, 7, Integer.MAX_VALUE};
    private static final boolean[] FLAGS = {false, true};

    private static final class Rows {
        final String family;
        final Set<String> expected = new LinkedHashSet<>(), seen = new LinkedHashSet<>();
        Rows(String family) { this.family = family; }
        void expect(String key) { assertTrue("duplicate expected " + key, expected.add(key)); }
        void verify(String key, Callable<BigDecimal> call, BigDecimal replacement,
                    IllegalStateException sentinel, boolean reject, StringBuilder trace, String historical) {
            assertTrue("unexpected " + family + "/" + key, expected.contains(key));
            assertTrue("duplicate " + family + "/" + key, seen.add(key));
            BigDecimal result = null;
            Exception failure = null;
            try { result = call.call(); } catch (Exception error) { failure = error; }
            if (reject) assertTrue(key + ": expected library NPE, got " + failure, failure instanceof NullPointerException);
            else if (sentinel != null) assertSame(key, sentinel, failure);
            else { assertNull(key, failure); assertSame(key, replacement, result); }
            System.out.println("576_STANDALONE\t" + family + "\tordinal=" + (seen.size() - 1) + "\t" + key
                + "\thistorical-assertion=" + historical + "\tactual="
                + (failure == null ? String.valueOf(result) : failure.getClass().getName() + ":" + failure.getMessage())
                + "\ttrace=" + trace);
        }
        void finish(int count) {
            assertEquals(count, expected.size());
            assertEquals(new ArrayList<>(expected), new ArrayList<>(seen));
        }
    }

    private static class Truthful extends BigDecimal {
        Truthful(long c, int s) { super(BigInteger.valueOf(c), s); }
        @Override public BigInteger unscaledValue() { return super.unscaledValue(); }
        @Override public int scale() { return super.scale(); }
        @Override public int precision() { return super.precision(); }
        @Override public int signum() { return super.signum(); }
        @Override public BigDecimal plus() { return super.plus(); }
        @Override public BigDecimal plus(MathContext mc) { return super.plus(mc); }
        @Override public BigDecimal round(MathContext mc) { return super.round(mc); }
        BigDecimal plusBase(MathContext mc) { return super.plus(mc); }
        BigDecimal roundBase(MathContext mc) { return super.round(mc); }
    }

    @Test public void noContextNulls() {
        Rows rows = new Rows("ZeroContextScale567.noContextPlus");
        for (int scale : SCALES) for (long c : new long[] {0, -7, 7}) for (boolean truthful : FLAGS)
            for (String route : truthful ? new String[] {"plus", "round", "plusBase", "roundBase"} : new String[] {"plus", "round"})
                rows.expect(scale + "/" + c + "/" + truthful + "/" + route);
        for (int scale : SCALES) for (long c : new long[] {0, -7, 7}) for (boolean truthful : FLAGS) {
            BigDecimal value = truthful ? new Truthful(c, scale) : new BigDecimal(BigInteger.valueOf(c), scale);
            assertSame(value, value.plus());
            for (String route : truthful ? new String[] {"plus", "round", "plusBase", "roundBase"} : new String[] {"plus", "round"}) {
                rows.verify(scale + "/" + c + "/" + truthful + "/" + route,
                    () -> route.equals("plusBase") ? ((Truthful) value).plusBase(null)
                        : route.equals("roundBase") ? ((Truthful) value).roundBase(null)
                        : UnaryNull576Boundary.call(value, route, null), null, null, true, new StringBuilder(), "NPE");
            }
            assertEquals(BigInteger.valueOf(c), value.unscaledValue());
            assertEquals(scale, value.scale());
        }
        rows.finish(126);
    }

    @Test public void explicitOverrides() {
        Rows rows = new Rows("ZeroContextScale567.explicitOverrides");
        String[] routes = {"plus.direct", "plus.inheritedRound", "plus.superRound", "round.direct", "plus.superPlus", "round.superRound"};
        for (boolean fail : FLAGS) for (String route : routes) rows.expect(fail + "/" + route);
        BigDecimal replacement = new BigDecimal("0.000");
        IllegalStateException failure = new IllegalStateException("caller sentinel");
        class PlusCaller extends BigDecimal {
            final boolean fail;
            PlusCaller(boolean fail) { super("0.00"); this.fail = fail; }
            @Override public BigDecimal plus(MathContext mc) { if (fail) throw failure; return replacement; }
            BigDecimal plusBase(MathContext mc) { return super.plus(mc); }
            BigDecimal roundBase(MathContext mc) { return super.round(mc); }
        }
        class RoundCaller extends BigDecimal {
            final boolean fail;
            RoundCaller(boolean fail) { super("0.00"); this.fail = fail; }
            @Override public BigDecimal round(MathContext mc) { if (fail) throw failure; return replacement; }
            BigDecimal roundBase(MathContext mc) { return super.round(mc); }
        }
        for (boolean fail : FLAGS) {
            PlusCaller plus = new PlusCaller(fail); RoundCaller round = new RoundCaller(fail);
            for (String route : routes) {
                boolean direct = route.equals("plus.direct") || route.equals("round.direct");
                rows.verify(fail + "/" + route, () -> {
                    switch (route) {
                        case "plus.direct": return plus.plus(null);
                        case "plus.inheritedRound": return plus.round(null);
                        case "plus.superRound": return plus.roundBase(null);
                        case "round.direct": return round.round(null);
                        case "plus.superPlus": return plus.plusBase(null);
                        case "round.superRound": return round.roundBase(null);
                        default: throw new AssertionError(route);
                    }
                }, replacement, fail ? failure : null, !direct, new StringBuilder(),
                    route.endsWith("superPlus") || route.equals("round.superRound") ? "NPE" : fail ? "caller-sentinel" : "replacement-identity");
            }
        }
        rows.finish(12);
    }

    @Test public void zeroUnary() {
        Rows rows = new Rows("ZeroContextScale567.zeroUnary");
        for (int scale : SCALES) for (boolean truthful : FLAGS) for (String op : new String[] {"abs", "negate"})
            rows.expect(scale + "/" + truthful + "/" + op);
        for (int scale : SCALES) for (boolean truthful : FLAGS) {
            BigDecimal zero = truthful ? new Truthful(0, scale) : new BigDecimal(BigInteger.ZERO, scale);
            for (String op : new String[] {"abs", "negate"}) rows.verify(scale + "/" + truthful + "/" + op,
                () -> UnaryNull576Boundary.call(zero, op, null), null, null, true, new StringBuilder(), "NPE");
            assertEquals(BigInteger.ZERO, zero.unscaledValue()); assertEquals(scale, zero.scale());
        }
        rows.finish(28);
    }

    @Test public void returnedUnary() {
        Rows rows = new Rows("ZeroContextScale567.returnedUnary");
        for (boolean fail : FLAGS) for (String op : new String[] {"returned.abs", "source.negate"}) rows.expect(fail + "/" + op);
        BigDecimal replacement = new BigDecimal("0.000");
        IllegalStateException failure = new IllegalStateException("unary caller sentinel");
        for (boolean fail : FLAGS) {
            BigDecimal returned = new BigDecimal("12.34") {
                @Override public BigDecimal plus(MathContext mc) { if (fail) throw failure; return replacement; }
            };
            BigDecimal source = new BigDecimal("0.00") { @Override public BigDecimal negate() { return returned; } };
            rows.verify(fail + "/returned.abs", () -> returned.abs(null), null, null, true, new StringBuilder(), fail ? "sentinel" : "replacement");
            rows.verify(fail + "/source.negate", () -> source.negate(null), null, null, true, new StringBuilder(), fail ? "sentinel" : "replacement");
        }
        rows.finish(4);
    }

    @Test public void zeroSeam() {
        Rows rows = new Rows("ZeroContextScale567.zeroSeam");
        String[] routes = {"source.negate", "returned.plus", "returned.round", "returned.superRound", "returned.superPlus"};
        for (int scale : SCALES) for (boolean fail : FLAGS) for (String route : routes) rows.expect(scale + "/" + fail + "/" + route);
        BigDecimal replacement = new BigDecimal("0.000");
        IllegalStateException failure = new IllegalStateException("explicit zero plus");
        for (int scale : SCALES) for (boolean fail : FLAGS) {
            StringBuilder trace = new StringBuilder();
            class Returned extends BigDecimal {
                Returned() { super(BigInteger.ZERO, scale); }
                @Override public int signum() { trace.append("sign;"); return 1; }
                @Override public BigDecimal plus(MathContext mc) { trace.append("plus;"); if (fail) throw failure; return replacement; }
                BigDecimal plusBase(MathContext mc) { return super.plus(mc); }
                BigDecimal roundBase(MathContext mc) { return super.round(mc); }
            }
            Returned returned = new Returned();
            BigDecimal source = new BigDecimal("-1") { @Override public BigDecimal negate() { trace.append("negate;"); return returned; } };
            for (String route : routes) {
                trace.setLength(0);
                rows.verify(scale + "/" + fail + "/" + route, () -> {
                    switch (route) {
                        case "source.negate": return source.negate(null);
                        case "returned.plus": return returned.plus(null);
                        case "returned.round": return returned.round(null);
                        case "returned.superRound": return returned.roundBase(null);
                        case "returned.superPlus": return returned.plusBase(null);
                        default: throw new AssertionError(route);
                    }
                }, replacement, fail ? failure : null, !route.equals("returned.plus"), trace,
                    route.equals("source.negate") ? "NPE|negate;" : route.endsWith("superPlus") ? "NPE" : fail ? "sentinel" : "replacement");
            }
            assertEquals(BigInteger.ZERO, returned.unscaledValue()); assertEquals(scale, returned.scale());
        }
        rows.finish(70);
    }

    @Test public void ordering() {
        Rows rows = new Rows("ZeroContextScale567.ordering");
        String[] routes = {"value.abs", "explicit.abs", "explicit.negate", "explicit.superAbs", "explicit.superNegate", "throwing.abs", "throwing.negate"};
        for (int sign : new int[] {-1, 0, 1}) for (boolean fail : FLAGS) for (String route : routes) rows.expect(sign + "/" + fail + "/" + route);
        BigDecimal replacement = new BigDecimal("0.000");
        IllegalStateException failure = new IllegalStateException("unary dispatch");
        for (int sign : new int[] {-1, 0, 1}) for (boolean fail : FLAGS) {
            StringBuilder trace = new StringBuilder();
            BigDecimal value = new BigDecimal("0.00") {
                @Override public int signum() { trace.append("sign;"); return sign; }
                @Override public BigDecimal negate(MathContext mc) { trace.append("negateContext;"); if (fail) throw failure; return replacement; }
            };
            class Explicit extends BigDecimal {
                Explicit() { super("0.00"); }
                @Override public BigDecimal abs(MathContext mc) { if (fail) throw failure; return replacement; }
                @Override public BigDecimal negate(MathContext mc) { if (fail) throw failure; return replacement; }
                BigDecimal absBase(MathContext mc) { return super.abs(mc); }
                BigDecimal negateBase(MathContext mc) { return super.negate(mc); }
            }
            Explicit explicit = new Explicit();
            BigDecimal throwing = new BigDecimal("0.00") {
                @Override public int signum() { throw failure; }
                @Override public BigDecimal negate() { throw failure; }
            };
            for (String route : routes) {
                trace.setLength(0);
                boolean direct = route.equals("explicit.abs") || route.equals("explicit.negate");
                rows.verify(sign + "/" + fail + "/" + route, () -> {
                    switch (route) {
                        case "value.abs": return value.abs(null);
                        case "explicit.abs": return explicit.abs(null);
                        case "explicit.negate": return explicit.negate(null);
                        case "explicit.superAbs": return explicit.absBase(null);
                        case "explicit.superNegate": return explicit.negateBase(null);
                        case "throwing.abs": return throwing.abs(null);
                        case "throwing.negate": return throwing.negate(null);
                        default: throw new AssertionError(route);
                    }
                }, replacement, fail ? failure : null, !direct, trace,
                    route.startsWith("throwing") ? "sentinel" : direct ? (fail ? "sentinel" : "replacement")
                        : route.equals("value.abs") ? (sign < 0 ? "sign;negateContext; with sentinel/replacement" : "NPE|sign;") : "NPE");
            }
            assertEquals(BigInteger.ZERO, explicit.unscaledValue()); assertEquals(2, explicit.scale());
        }
        rows.finish(42);
    }

    @Test public void precisionSentinel() {
        Rows rows = new Rows("ZeroUnaryPrecision567.precisionSentinel");
        for (boolean negate : FLAGS) for (int precision : new int[] {0, 1, 7}) for (RoundingMode mode : RoundingMode.values())
            rows.expect(negate + "/" + precision + "/" + mode);
        for (boolean negate : FLAGS) for (int precision : new int[] {0, 1, 7}) for (RoundingMode mode : RoundingMode.values()) {
            IllegalStateException sentinel = new IllegalStateException("precision sentinel");
            StringBuilder trace = new StringBuilder();
            BigDecimal zero = new BigDecimal("0.00") {
                @Override public int precision() { trace.append("precision;"); throw sentinel; }
                @Override public int signum() { trace.append("sign;"); return 0; }
                @Override public BigDecimal plus(MathContext mc) { throw new AssertionError("internal plus must not be called"); }
            };
            BigDecimal receiver = new BigDecimal("-1.00") { @Override public BigDecimal negate() { trace.append("negate;"); return zero; } };
            // Preserve the valid-call precursor and sentinel identity from the original common row.
            MathContext mc = new MathContext(precision, mode);
            try {
                BigDecimal result = negate ? receiver.negate(mc) : zero.abs(mc);
                assertEquals(0, precision); assertSame(zero, result);
            } catch (IllegalStateException error) { assertTrue(precision > 0); assertSame(sentinel, error); }
            String prefix = negate ? "negate;" : "sign;";
            assertEquals(prefix + (precision == 0 ? "" : "precision;"), trace.toString());
            trace.setLength(0);
            rows.verify(negate + "/" + precision + "/" + mode, () -> negate ? receiver.negate(null) : zero.abs(null),
                null, null, true, trace, "NPE|" + prefix);
            assertEquals(BigInteger.ZERO, zero.unscaledValue()); assertEquals(2, zero.scale());
            assertEquals(new BigInteger("-100"), receiver.unscaledValue()); assertEquals(2, receiver.scale());
        }
        rows.finish(48);
    }
}

// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exact counterparts of standalone common executions made ill-typed by the non-null API. */
public class DecimalBinary575StandaloneTest {
    private final Set<String> executed = new LinkedHashSet<>();
    private void npe(String key, Callable<?> action) throws Exception {
        assertTrue("duplicate execution " + key, executed.add(key));
        try { action.call(); fail("library accepted null: " + key); }
        catch (NullPointerException expected) {
            System.out.println("575_STANDALONE\t" + key + "\tTARGET_NPE\t" + expected);
        }
    }
    private static class Caller extends BigDecimal {
        final BigDecimal replacement = new BigDecimal("7.00");
        final IllegalStateException failure;
        boolean throwing;
        BigDecimal operand;
        MathContext context;
        String entry;
        int entries;
        Caller(String text, String message) { super(text); failure = new IllegalStateException(message); }
        BigDecimal answer(String entry, BigDecimal operand, MathContext context) {
            this.entry = entry; this.operand = operand; this.context = context; entries++;
            if (throwing) throw failure;
            return replacement;
        }
        @Override public BigDecimal divide(BigDecimal v) { return answer("exact", v, null); }
        @Override public BigDecimal divide(BigDecimal v, MathContext mc) { return answer("context", v, mc); }
        @Override public BigDecimal divideToIntegralValue(BigDecimal v, MathContext mc) { return answer("integral", v, mc); }
        @Override public BigDecimal add(BigDecimal v, MathContext mc) { return answer("add", v, mc); }
        @Override public BigDecimal multiply(BigDecimal v, MathContext mc) { return answer("multiply", v, mc); }
    }
    private void direct(String key, Caller source, BigDecimal operand, MathContext context,
                        String entry, Callable<BigDecimal> action) throws Exception {
        assertTrue(executed.add(key));
        int before = source.entries;
        if (source.throwing) {
            try { action.call(); fail(key); }
            catch (IllegalStateException failure) { assertSame(source.failure, failure); }
        } else assertSame(source.replacement, action.call());
        assertEquals(before + 1, source.entries);
        assertEquals(entry, source.entry);
        assertSame(operand, source.operand);
        assertSame(context, source.context);
    }

    @Test public void contextStandaloneRows() throws Exception {
        MathContext missing = null;
        for (boolean truthful : new boolean[] {false, true}) {
            BigDecimal source = truthful ? new DecimalBinary575Adapter.Truthful("-10.50") : new BigDecimal("-10.50");
            BigDecimal divisor = truthful ? new DecimalBinary575Adapter.Truthful("2.0") : new BigDecimal("2.0");
            MathContext finite = new MathContext(2, RoundingMode.HALF_EVEN);
            npe("context-errors/" + truthful + "/operand", () -> source.divide(null, finite));
            npe("context-errors/" + truthful + "/context", () -> source.divide(divisor, missing));
            npe("context-errors/" + truthful + "/both", () -> source.divide(null, missing));
        }
        assertEquals(6, executed.size());
        for (boolean throwing : new boolean[] {false, true}) {
            Caller caller = new Caller("10.50", "caller-owned sentinel");
            caller.throwing = throwing;
            BigDecimal source = caller;
            BigDecimal[] operands = {new BigDecimal("2.0"), source, null};
            MathContext[] contexts = {new MathContext(2, RoundingMode.HALF_EVEN), MathContext.UNLIMITED, null};
            for (int operandIndex = 0; operandIndex < operands.length; operandIndex++) {
                BigDecimal operand = operands[operandIndex];
                if (operand == null)
                    direct("context-direct/" + throwing + "/" + operandIndex + "/exact", caller, operand, null,
                        "exact", () -> source.divide(operand));
                for (int contextIndex = 0; contextIndex < contexts.length; contextIndex++) {
                    MathContext context = contexts[contextIndex];
                    if (operand == null || context == null)
                        direct("context-direct/" + throwing + "/" + operandIndex + "/" + contextIndex,
                            caller, operand, context, "context", () -> source.divide(operand, context));
                }
            }
        }
        assertEquals(18, executed.size()); // Six required-NPE + twelve caller-owned override executions.
        DecimalBinary575Adapter.Truthful source = new DecimalBinary575Adapter.Truthful("10.50");
        npe("context-super/null", () -> source.divideBase(null, new MathContext(2)));
        assertEquals(19, executed.size());
    }

    private static class IntegralTruthful extends DecimalBinary575Adapter.Truthful {
        IntegralTruthful(String text) { super(text); }
        @Override public BigDecimal divideToIntegralValue(BigDecimal v) { return super.divideToIntegralValue(v); }
        @Override public BigDecimal divideToIntegralValue(BigDecimal v, MathContext mc) { return super.divideToIntegralValue(v, mc); }
        @Override public BigDecimal remainder(BigDecimal v) { return super.remainder(v); }
        @Override public BigDecimal remainder(BigDecimal v, MathContext mc) { return super.remainder(v, mc); }
        @Override public BigDecimal[] divideAndRemainder(BigDecimal v) { return super.divideAndRemainder(v); }
        @Override public BigDecimal[] divideAndRemainder(BigDecimal v, MathContext mc) { return super.divideAndRemainder(v, mc); }
        @Override public BigDecimal integralBase(BigDecimal v, MathContext mc) { return super.divideToIntegralValue(v, mc); }
    }
    @Test public void integralStandaloneRows() throws Exception {
        Caller caller = new Caller("17.25", "541 caller sentinel");
        BigDecimal source = caller;
        BigDecimal distinct = new BigDecimal("5.0");
        MathContext finite = new MathContext(1, RoundingMode.UNNECESSARY);
        BigDecimal[] operands = {distinct, source, null};
        MathContext[] contexts = {finite, MathContext.UNLIMITED, null};
        for (int i = 0; i < operands.length; i++) for (int j = 0; j < contexts.length; j++) {
            BigDecimal operand = operands[i]; MathContext context = contexts[j];
            if (operand != null && context != null) continue;
            for (boolean throwing : new boolean[] {false, true}) {
                caller.throwing = throwing;
                direct("integral-direct/" + i + "/" + j + "/" + throwing, caller, operand, context,
                    "integral", () -> source.divideToIntegralValue(operand, context));
            }
        }
        assertEquals(10, executed.size());
        IntegralTruthful truthful = new IntegralTruthful("17.25");
        npe("integral-super/operand", () -> truthful.integralBase(null, finite));
        npe("integral-super/context", () -> truthful.integralBase(distinct, null));
        assertEquals(12, executed.size());
    }

    @Test public void preferredScaleStandaloneRows() throws Exception {
        MathContext missing = null;
        for (int mask = 0; mask < 4; mask++) for (RoundingMode mode : RoundingMode.values()) {
            BigDecimal source = (mask & 1) == 0 ? BigDecimal.valueOf(1, Integer.MIN_VALUE) :
                new DecimalBinary575Adapter.Truthful(1, Integer.MIN_VALUE);
            String key = "preferred/" + mask + "/" + mode;
            npe(key + "/exact", () -> source.divide(null));
            npe(key + "/operand", () -> source.divide(null, new MathContext(1, mode)));
            npe(key + "/context", () -> source.divide(BigDecimal.ONE, missing));
        }
        assertEquals(96, executed.size()); // Keep duplicate receiver masks as distinct original executions.
    }

    @Test public void scaleRoundingStandaloneRows() throws Exception {
        for (boolean throwing : new boolean[] {false, true}) {
            Caller caller = new Caller("1", "caller sentinel"); caller.throwing = throwing;
            BigDecimal source = caller;
            direct("scale-direct/" + throwing + "/add", caller, null, null, "add", () -> source.add(null, null));
            direct("scale-direct/" + throwing + "/multiply", caller, null, null, "multiply", () -> source.multiply(null, null));
        }
        assertEquals(4, executed.size());
        DecimalBinary575Adapter.Truthful source = new DecimalBinary575Adapter.Truthful(1, Integer.MAX_VALUE);
        for (int precision : new int[] {0, 1}) {
            MathContext context = new MathContext(precision);
            npe("scale-super/add/" + precision, () -> source.addBase(null, context));
            npe("scale-super/multiply/" + precision, () -> source.multiplyBase(null, context));
        }
        npe("scale-super/add/context", () -> source.addBase(BigDecimal.ZERO, null));
        npe("scale-super/multiply/context", () -> source.multiplyBase(BigDecimal.ONE, null));
        assertEquals(10, executed.size());
    }
}

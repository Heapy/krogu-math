// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exact counterparts of the 73 displaced standalone common null executions. */
public class Validation578StandaloneTest {
    private final Set<String> keys = new LinkedHashSet<>();
    private void key(String key) {
        assertTrue("duplicate " + key, keys.add(key));
        System.out.println("standalone578 ordinal=" + (keys.size() - 1) + " key=" + key);
    }
    private void npe(String key, Callable<?> action) {
        key(key);
        assertThrows(key, NullPointerException.class, action::call);
    }
    private void diagnostic(String key, String historical, Callable<?> action) {
        key(key);
        Exception failure = assertThrows(key, Exception.class, action::call);
        System.out.println(key + " class=compound-invalid-priority frozen=" + historical
            + " actual=" + failure.getClass().getSimpleName() + " message=" + failure.getMessage());
    }
    private void direct(String prefix, String text, String message) {
        BigDecimal marker = new BigDecimal("7.00");
        IllegalStateException failure = new IllegalStateException(message);
        int[] entries = {0};
        BigDecimal replacement = new BigDecimal(text) {
            @Override public BigDecimal sqrt(MathContext context) {
                assertNull(context); entries[0]++; return marker;
            }
        };
        BigDecimal throwing = new BigDecimal(text) {
            @Override public BigDecimal sqrt(MathContext context) { assertNull(context); throw failure; }
        };
        key(prefix + "/return");
        assertSame(marker, replacement.sqrt(null));
        assertEquals(1, entries[0]);
        key(prefix + "/throw");
        assertSame(failure, assertThrows(IllegalStateException.class, () -> throwing.sqrt(null)));
    }

    @Test public void everyDisplacedStandaloneExecution() {
        for (String text : new String[]{"0", "0.00"})
            npe("568/text/" + text, () -> new BigDecimal(text).sqrt(null));
        int[] scales = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -3, -2, -1, 0, 1, 2, 3,
            Integer.MAX_VALUE - 1, Integer.MAX_VALUE};
        for (int scale : scales) {
            BigDecimal ordinary = new BigDecimal(BigInteger.ZERO, scale);
            Validation578Boundary.TruthfulRoot truthful = new Validation578Boundary.TruthfulRoot(BigInteger.ZERO, scale);
            npe("568/zero/ordinary/" + scale, () -> ordinary.sqrt(null));
            npe("568/zero/truthful/" + scale, () -> truthful.sqrt(null));
            npe("568/zero/super/" + scale, () -> truthful.inheritedRoot(null));
        }
        assertEquals(35, keys.size());
        for (String text : new String[]{"4", "2"}) {
            Validation578Boundary.TruthfulRoot truthful = new Validation578Boundary.TruthfulRoot(new BigInteger(text), 0);
            npe("568/nonzero/ordinary/" + text, () -> new BigDecimal(text).sqrt(null));
            npe("568/nonzero/truthful/" + text, () -> truthful.sqrt(null));
            npe("568/nonzero/super/" + text, () -> truthful.inheritedRoot(null));
        }
        Validation578Boundary.TruthfulRoot negative = new Validation578Boundary.TruthfulRoot(new BigInteger("-4"), 0);
        diagnostic("568/negative/ordinary", "ArithmeticException", () -> new BigDecimal("-4").sqrt(null));
        diagnostic("568/negative/truthful", "ArithmeticException", () -> negative.sqrt(null));
        diagnostic("568/negative/super", "ArithmeticException", () -> negative.inheritedRoot(null));
        direct("568/direct/0.00", "0.00", "caller owned");
        assertEquals(46, keys.size());
        Set<Long> bits = new LinkedHashSet<>();
        for (double value : new double[]{0.0, -0.0}) {
            assertTrue(bits.add(Double.doubleToRawLongBits(value)));
            npe("569/zero/" + Double.doubleToRawLongBits(value), () -> new BigDecimal(value, null));
        }
        assertEquals(Set.of(0L, Long.MIN_VALUE), bits);
        for (double value : new double[]{0.1, 1.0, -1.25, Double.MIN_VALUE, Double.MAX_VALUE})
            npe("569/finite/" + Double.doubleToRawLongBits(value), () -> new BigDecimal(value, null));
        for (double value : new double[]{Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
            diagnostic("569/nonfinite/" + Double.doubleToRawLongBits(value), "NumberFormatException", () -> new BigDecimal(value, null));
        assertEquals(56, keys.size());
        npe("563/root/4", () -> new BigDecimal("4").sqrt(null));
        diagnostic("563/root/-4", "ArithmeticException", () -> new BigDecimal("-4").sqrt(null));
        npe("563/root/zero-min-scale", () -> new BigDecimal(BigInteger.ZERO, Integer.MIN_VALUE).sqrt(null));
        for (String text : new String[]{"1", "4.00", "0.0004"})
            direct("563/callback/" + text, text, "caller-owned root");
        direct("565/direct/4.001", "4.001", "caller-owned midpoint root");
        npe("565/super/4.001", () -> new Validation578Boundary.TruthfulRoot(new BigInteger("4001"), 3).inheritedRoot(null));
        diagnostic("538/modPow/3/null/0", "ArithmeticException", () -> new BigInteger("3").modPow(null, BigInteger.ZERO));
        npe("538/modPow/3/null/17", () -> new BigInteger("3").modPow(null, new BigInteger("17")));
        npe("567/string/0.00", () -> new BigDecimal("0.00", null));
        npe("567/chars/0.00", () -> new BigDecimal("0.00".toCharArray(), null));
        npe("identity/context/1", () -> new MathContext(1, null));
        assertEquals(73, keys.size());
    }

    @Test public void callerModPowOverrideAndActualSuperAreDistinct() {
        BigInteger marker = new BigInteger("7");
        IllegalStateException failure = new IllegalStateException("caller modPow");
        class Caller extends BigInteger {
            Caller() { super("3"); }
            @Override public BigInteger modPow(BigInteger e, BigInteger m) { assertNull(e); return marker; }
            BigInteger parent(BigInteger e, BigInteger m) { return super.modPow(e, m); }
        }
        Caller caller = new Caller();
        assertSame(marker, caller.modPow(null, BigInteger.TEN));
        assertThrows(NullPointerException.class, () -> caller.parent(null, BigInteger.TEN));
        BigInteger throwing = new BigInteger("3") {
            @Override public BigInteger modPow(BigInteger e, BigInteger m) { assertNull(e); throw failure; }
        };
        assertSame(failure, assertThrows(IllegalStateException.class, () -> throwing.modPow(null, BigInteger.TEN)));
    }
}

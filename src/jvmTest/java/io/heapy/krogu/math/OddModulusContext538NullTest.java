// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/** Java-only returned-null controls at the unchanged public/private entry boundary. */
public class OddModulusContext538NullTest {
    @Test public void nullExponentAndInvalidModulusAreSeparateContracts() {
        BigInteger a = new BigInteger("3");
        Exception competing = assertThrows(Exception.class, () -> a.modPow(null, BigInteger.ZERO));
        System.out.println("538 3/null/0 compound-invalid-priority frozen=ArithmeticException actual="
            + competing.getClass().getSimpleName() + " message=" + competing.getMessage());
        assertThrows(NullPointerException.class, () -> a.modPow(null, new BigInteger("17")));
        assertThrows(NullPointerException.class, () -> a.modPow(BigInteger.TWO, null));
    }

    @Test public void nullNegationStillAllowsRequiredReductionBeforePrivateNullCheck() {
        StringBuilder trace = new StringBuilder();
        BigInteger a = new BigInteger("20") {
            @Override public BigInteger mod(BigInteger m) {
                trace.append("mod;");
                return super.mod(m);
            }
        };
        BigInteger e = new BigInteger("-2") {
            @Override public BigInteger negate() { trace.append("negate;"); return null; }
        };
        assertThrows(NullPointerException.class, () -> a.modPow(e, new BigInteger("17")));
        assertEquals("negate;mod;", trace.toString());
    }

    @Test public void nullReductionAndEvenSplitFailBeforeAnyContextUse() {
        StringBuilder trace = new StringBuilder();
        BigInteger a = new BigInteger("20") {
            @Override public BigInteger mod(BigInteger m) { trace.append("mod;"); return null; }
        };
        assertThrows(NullPointerException.class, () -> a.modPow(BigInteger.TWO, new BigInteger("17")));
        assertEquals("mod;", trace.toString());
        trace.setLength(0);
        BigInteger m = new BigInteger("34") {
            @Override public BigInteger shiftRight(int bits) { trace.append("split;"); return null; }
        };
        assertThrows(NullPointerException.class, () -> new BigInteger("3").modPow(BigInteger.TWO, m));
        assertEquals("split;", trace.toString());
    }
}

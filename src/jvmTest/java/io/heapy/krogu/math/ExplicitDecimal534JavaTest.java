// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.*;

/** Compiles real constructors, constructor references and explicit factory calls together. */
public class ExplicitDecimal534JavaTest {
    private static final class DecimalSubclass extends BigDecimal {
        DecimalSubclass(String value) { super(value); }
    }

    @Test public void additiveApiKeepsJavaConstructionAndNamedResults() {
        java.util.function.Function<String, BigDecimal> construct = BigDecimal::new;
        assertEquals(construct.apply("1.25"), new DecimalSubclass("1.25"));
        assertEquals(new BigDecimal("0.1"), Decimals.fromCanonical(0.1));
        assertEquals(new BigDecimal(0.1), Decimals.fromBinary(0.1));
        assertNull(Decimals.parseOrNull("bad"));
        assertEquals(new BigDecimal("0.5"), ExplicitDecimalKt.divideExact(BigDecimal.ONE, BigDecimal.TWO));
        QuotientRemainder<BigInteger> pair = ExplicitDecimalKt.quotientAndRemainder(new BigInteger("17"), new BigInteger("5"));
        assertEquals(new BigInteger("3"), pair.getQuotient());
        assertEquals(new BigInteger("2"), pair.getRemainder());
        Object exact = ExplicitDecimalKt.toLongExactResult(new BigDecimal("17.00"));
        assertTrue(exact instanceof ExactConversion.Value<?>);
        assertEquals(Long.valueOf(17), ((ExactConversion.Value<?>) exact).getValue());
        Object failure = ExplicitDecimalKt.toIntExactResult(new BigDecimal("0.5"));
        assertTrue(failure instanceof ExactConversion.Failure);
    }
}

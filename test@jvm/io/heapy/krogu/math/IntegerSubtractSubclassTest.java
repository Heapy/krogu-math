// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IntegerSubtractSubclassTest {
    private static String outcome(Callable<?> action, Object source, Object operand,
                                  String left, String right, StringBuilder trace) {
        try {
            Object result = action.call();
            String calls = trace.toString();
            String value = result == source ? left : result == operand ? right : String.valueOf(result);
            return value + "|" + (result == source) + "|" + (result == operand) + "|" + calls;
        } catch (Exception e) {
            return "!" + e.getClass().getSimpleName() + (e instanceof IllegalStateException ? ":" + e.getMessage() : "") + "|" + trace;
        }
    }

    @Test public void subtractionPreservesOnlyTheZeroSourceNegationCallback() {
        String[] inputs = {"0", "1", "-1", "1073741824", "-1073741824", "123456789012345678901234567890", "-123456789012345678901234567890"};
        for (String left : inputs) for (String right : inputs)
            for (boolean nullAdd : new boolean[] {false, true})
                for (boolean nullNegation : new boolean[] {false, true}) {
                    StringBuilder et = new StringBuilder(), at = new StringBuilder();
                    java.math.BigInteger expected = new java.math.BigInteger(left) {
                        @Override public java.math.BigInteger add(java.math.BigInteger value) {
                            et.append("add;");
                            if (nullAdd) return null;
                            throw new IllegalStateException("add callback");
                        }
                    };
                    BigInteger actual = new BigInteger(left) {
                        @Override public BigInteger add(BigInteger value) {
                            at.append("add;");
                            if (nullAdd) return null;
                            throw new IllegalStateException("add callback");
                        }
                    };
                    java.math.BigInteger er = new java.math.BigInteger(right) {
                        @Override public java.math.BigInteger negate() {
                            et.append("negate;");
                            if (nullNegation) return null;
                            throw new IllegalStateException("negate callback");
                        }
                    };
                    BigInteger ar = new BigInteger(right) {
                        @Override public BigInteger negate() {
                            at.append("negate;");
                            if (nullNegation) return null;
                            throw new IllegalStateException("negate callback");
                        }
                    };
                    assertEquals(left + "/" + right + "/" + nullAdd + "/" + nullNegation,
                        outcome(() -> expected.subtract(er), expected, er, left, right, et),
                        outcome(() -> actual.subtract(ar), actual, ar, left, right, at));
                }
    }

    @Test public void nullOperandIsRejectedBeforeAnyArithmeticCallback() {
        for (String left : new String[] {"0", "1", "-1"}) {
            StringBuilder et = new StringBuilder(), at = new StringBuilder();
            java.math.BigInteger expected = new java.math.BigInteger(left) {
                @Override public java.math.BigInteger add(java.math.BigInteger value) { et.append("add;"); return this; }
            };
            BigInteger actual = new BigInteger(left) {
                @Override public BigInteger add(BigInteger value) { at.append("add;"); return this; }
            };
            assertEquals(outcome(() -> expected.subtract(null), expected, null, left, "null", et),
                outcome(() -> actual.subtract(null), actual, null, left, "null", at));
        }
    }
}

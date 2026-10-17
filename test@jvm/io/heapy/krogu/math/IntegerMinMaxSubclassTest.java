package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IntegerMinMaxSubclassTest {
    private static String outcome(Callable<?> action, Object source, Object operand) {
        try { Object result = action.call(); return String.valueOf(result) + "|" + (result == source) + "|" + (result == operand); }
        catch (Exception e) { return "!" + e.getClass().getSimpleName() + (e instanceof IllegalStateException ? ":" + e.getMessage() : ""); }
    }

    @Test public void extremaRejectNullAndPreserveValidVirtualComparisonAndIdentity() {
        int nullRows = 0, validRows = 0;
        for (String text : new String[] {"0", "1", "-1"})
            for (int comparison : new int[] {Integer.MIN_VALUE, -2, -1, 0, 1, 2, Integer.MAX_VALUE})
                for (boolean throwing : new boolean[] {false, true})
                    for (boolean nullOperand : new boolean[] {false, true})
                        for (boolean min : new boolean[] {false, true}) {
                            StringBuilder et = new StringBuilder(), at = new StringBuilder();
                            java.math.BigInteger expected = new java.math.BigInteger(text) {
                                @Override public int compareTo(java.math.BigInteger value) {
                                    et.append("compare:").append(value == null).append(';');
                                    if (throwing) throw new IllegalStateException("comparison callback");
                                    return comparison;
                                }
                            };
                            BigInteger actual = new BigInteger(text) {
                                @Override public int compareTo(BigInteger value) {
                                    at.append("compare:").append(value == null).append(';');
                                    if (throwing) throw new IllegalStateException("comparison callback");
                                    return comparison;
                                }
                            };
                            java.math.BigInteger er = nullOperand ? null : new java.math.BigInteger("2");
                            BigInteger ar = nullOperand ? null : new BigInteger("2");
                            String a = outcome(() -> min ? expected.min(er) : expected.max(er), expected, er);
                            String b = outcome(() -> min ? actual.min(ar) : actual.max(ar), actual, ar);
                            String key = text + "/" + comparison + "/" + throwing + "/" + nullOperand + "/" + min;
                            if (nullOperand) {
                                nullRows++;
                                System.out.println("574_INTEGER_SUBCLASS_NULL\t" + key + "\thost=" + a + "|" + et + "\town=" + b + "|" + at);
                                assertEquals(key + " public null contract", "!NullPointerException", b);
                            } else {
                                validRows++;
                                assertEquals(key, a + "|" + et, b + "|" + at);
                            }
                        }
        assertEquals(84, nullRows);
        assertEquals(84, validRows);
    }

    @Test public void javaComparableAndCollectionSortingRemainUsable() {
        Comparable<BigInteger> value = new BigInteger("1");
        assertEquals(-1, value.compareTo(new BigInteger("2")));
        java.util.List<BigInteger> values = new java.util.ArrayList<>(java.util.List.of(new BigInteger("2"), new BigInteger("-1"), new BigInteger("0"), new BigInteger("1")));
        java.util.Collections.sort(values);
        assertEquals("[-1, 0, 1, 2]", values.toString());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test public void erasedComparableBridgePreservesDispatchAndTypeChecks() {
        java.math.BigInteger expected = new java.math.BigInteger("1") {
            @Override public int compareTo(java.math.BigInteger value) { return value == null ? 17 : -17; }
        };
        BigInteger actual = new BigInteger("1") {
            @Override public int compareTo(BigInteger value) { return value == null ? 17 : -17; }
        };
        for (Object operand : new Object[] {null, "wrong type"}) {
            assertEquals(outcome(() -> ((Comparable) expected).compareTo(operand), expected, operand),
                outcome(() -> ((Comparable) actual).compareTo(operand), actual, operand));
        }
    }
}

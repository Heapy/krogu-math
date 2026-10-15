// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

/** Test-only Java boundary. Nulls reach library methods; the adapter never manufactures NPE. */
public final class ComparisonNull574 {
    private ComparisonNull574() {}

    public static int compareIntegerNull(BigInteger value) { return value.compareTo(null); }
    public static int compareDecimalNull(BigDecimal value) { return value.compareTo(null); }

    public static final class Observation {
        public final Exception failure;
        public final String outcome;
        public final String trace;
        Observation(Exception failure, String outcome, String trace) {
            this.failure = failure;
            this.outcome = outcome;
            this.trace = trace;
        }
        public String diagnostic() {
            return outcome + "|" + trace + "|message=" + (failure == null ? "" : failure.getMessage());
        }
    }

    private static Observation observe(Callable<?> action, Object receiver, Object operand,
                                       StringBuilder trace, boolean comparison) {
        try {
            Object result = action.call();
            String outcome = comparison ? String.valueOf(result)
                : String.valueOf(result) + "|" + (result == receiver) + "|" + (result == operand);
            return new Observation(null, outcome, trace.toString());
        } catch (Exception failure) {
            return new Observation(failure, "!" + failure.getClass().getSimpleName()
                + (failure instanceof IllegalStateException ? ":" + failure.getMessage() : ""), trace.toString());
        }
    }

    /** Same receiver, callback behavior, operation and operand as the exact IM/MM source row. */
    public static Observation vector(String[] row) {
        if (row.length != 7 || !row[3].equals("~")) throw new AssertionError("Not a displaced comparison row");
        String operation = row[1].split(":")[0];
        boolean base = operation.startsWith("base");
        boolean throwing = row[1].endsWith(":throw");
        int comparison = Integer.parseInt(row[4]);
        StringBuilder trace = new StringBuilder();
        if (row[0].equals("IM")) {
            BigInteger value = base ? new BigInteger(row[2]) : new BigInteger(row[2]) {
                @Override public int compareTo(BigInteger other) {
                    trace.append("compare:").append(other == null).append(';');
                    if (throwing) throw new IllegalStateException("comparison callback");
                    return comparison;
                }
            };
            switch (operation) {
                case "baseCompare": return observe(() -> value.compareTo(null), value, null, trace, true);
                case "min": case "baseMin": return observe(() -> value.min(null), value, null, trace, false);
                case "max": case "baseMax": return observe(() -> value.max(null), value, null, trace, false);
                default: throw new AssertionError("Unknown IM operation: " + operation);
            }
        }
        if (row[0].equals("MM")) {
            BigDecimal value = base ? new BigDecimal(row[2]) : new BigDecimal(row[2]) {
                @Override public int compareTo(BigDecimal other) {
                    trace.append("compare:").append(other == null).append(';');
                    if (throwing) throw new IllegalStateException("comparison callback");
                    return comparison;
                }
            };
            switch (operation) {
                case "min": case "baseMin": return observe(() -> value.min(null), value, null, trace, false);
                case "max": case "baseMax": return observe(() -> value.max(null), value, null, trace, false);
                default: throw new AssertionError("Unknown MM operation: " + operation);
            }
        }
        throw new AssertionError("Unknown comparison family: " + row[0]);
    }

    /** Reflection is virtual. Only target failures are observations; adapter faults fail the test. */
    public static Object virtualNull(Object receiver, Class<?> owner, String method) throws Throwable {
        try {
            return owner.getMethod(method, owner).invoke(receiver, new Object[] {null});
        } catch (InvocationTargetException failure) {
            throw failure.getCause();
        } catch (ReflectiveOperationException | IllegalArgumentException failure) {
            throw new AssertionError("Comparison reflection adapter failed", failure);
        }
    }
}

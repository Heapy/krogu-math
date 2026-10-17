// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

/** Java boundary: no adapter null checks, casts, or synthesized target exceptions. */
public final class UnaryNull576Boundary {
    public static BigDecimal call(BigDecimal receiver, String operation, MathContext context) {
        switch (operation) {
            case "abs": return receiver.abs(context);
            case "negate": return receiver.negate(context);
            case "plus": return receiver.plus(context);
            case "round": return receiver.round(context);
            default: throw new AssertionError("Unknown operation " + operation);
        }
    }

    public static final class Observation {
        public final String outcome;
        public final String trace;
        public final String diagnostic;
        Observation(String outcome, String trace, String diagnostic) {
            this.outcome = outcome; this.trace = trace; this.diagnostic = diagnostic;
        }
    }

    public static Observation vector(String[] v) {
        String operation = v[1].substring(0, v[1].indexOf(':'));
        String behavior = v[1].substring(v[1].indexOf(':') + 1);
        StringBuilder trace = new StringBuilder();
        BigDecimal value;
        if (v[0].equals("K")) {
            value = new BigDecimal(v[2]) {
                @Override public BigDecimal plus(MathContext mc) {
                    trace.append("plus:").append(mc == null).append(';');
                    switch (behavior) {
                        case "return": return new BigDecimal("42.00");
                        case "self": return this;
                        case "throw": throw new IllegalStateException("plus callback");
                        case "super": return super.plus(mc);
                        default: throw new AssertionError(behavior);
                    }
                }
            };
        } else if (operation.equals("returnedNegate")) {
            BigDecimal returned = new BigDecimal(v[3]) {
                @Override public BigDecimal plus(MathContext mc) {
                    trace.append("plus;");
                    switch (behavior) {
                        case "replace": return new BigDecimal("42.00");
                        case "throw": throw new IllegalStateException("plus callback");
                        case "super": return super.plus(mc);
                        default: throw new AssertionError(behavior);
                    }
                }
                @Override public BigDecimal round(MathContext mc) { trace.append("round;"); return new BigDecimal("99"); }
            };
            value = new BigDecimal(v[2]) { @Override public BigDecimal negate() { return returned; } };
        } else {
            int sign = Integer.parseInt(v[3]);
            value = new BigDecimal(v[2]) {
                @Override public int signum() { trace.append("sign;"); return sign; }
                @Override public BigDecimal negate() { trace.append("negate;"); return super.negate(); }
                @Override public BigDecimal plus(MathContext mc) {
                    trace.append("plus:").append(mc == null).append(';');
                    switch (behavior) {
                        case "return": return new BigDecimal("42.00");
                        case "self": return this;
                        case "throw": throw new IllegalStateException("context callback");
                        case "super": return super.plus(mc);
                        default: throw new AssertionError(behavior);
                    }
                }
                @Override public BigDecimal negate(MathContext mc) {
                    trace.append("negateContext:").append(mc == null).append(';');
                    switch (behavior) {
                        case "return": return new BigDecimal("42.00");
                        case "self": return this;
                        case "throw": throw new IllegalStateException("context callback");
                        case "super": return super.negate(mc);
                        default: throw new AssertionError(behavior);
                    }
                }
            };
        }
        String method = operation.equals("nullAbs") ? "abs"
            : operation.equals("nullNegate") || operation.equals("returnedNegate") ? "negate" : operation;
        String outcome, diagnostic;
        try { outcome = String.valueOf(call(value, method, null)); diagnostic = outcome; }
        catch (Exception error) {
            outcome = "!" + error.getClass().getSimpleName()
                + (error instanceof IllegalStateException ? ":" + error.getMessage() : "");
            diagnostic = error.getClass().getName() + ":" + error.getMessage();
        }
        // Retain the eight finite-zero immutable receiver controls, now at the real boundary.
        if (operation.equals("nullAbs") && v[2].equals("0.00") && Integer.parseInt(v[3]) >= 0) {
            org.junit.Assert.assertEquals(BigInteger.ZERO, value.unscaledValue());
            org.junit.Assert.assertEquals(2, value.scale());
        }
        return new Observation(outcome, trace.toString(), diagnostic);
    }
}

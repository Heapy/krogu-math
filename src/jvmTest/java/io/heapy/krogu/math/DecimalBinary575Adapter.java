// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

/** Real Java entry points. No caller null assertions/casts and no synthetic NPEs. */
public final class DecimalBinary575Adapter {
    private DecimalBinary575Adapter() {}

    private static class Binary extends BigDecimal {
        final String operation, behavior;
        final StringBuilder trace = new StringBuilder();
        Binary(String text, String operation, String behavior) {
            super(text); this.operation = operation; this.behavior = behavior;
        }
        private BigDecimal callback(BigDecimal operand) {
            trace.append(operation).append(':').append(operand == null).append(';');
            return switch (behavior) {
                case "return" -> new BigDecimal("42.00");
                case "self" -> this;
                case "throw" -> throw new IllegalStateException("operation callback");
                case "super" -> null; // Caller below invokes the actual Java super method.
                default -> throw new AssertionError(behavior);
            };
        }
        @Override public BigDecimal add(BigDecimal v) {
            BigDecimal result = callback(v); return behavior.equals("super") ? super.add(v) : result;
        }
        @Override public BigDecimal subtract(BigDecimal v) {
            BigDecimal result = callback(v); return behavior.equals("super") ? super.subtract(v) : result;
        }
        @Override public BigDecimal multiply(BigDecimal v) {
            BigDecimal result = callback(v); return behavior.equals("super") ? super.multiply(v) : result;
        }
    }

    public static String nd(String[] row) {
        String[] operation = row[1].split(":");
        int precision = Integer.parseInt(row[4]);
        Binary source = new Binary(row[2], operation[0], operation[1]);
        BigDecimal operand = row[3].equals("~") ? null : new BigDecimal(row[3]);
        MathContext context = precision < 0 ? null : new MathContext(precision, RoundingMode.valueOf(row[5]));
        String result;
        try {
            BigDecimal value = switch (operation[0]) {
                case "add" -> precision == -2 ? source.add(operand) : source.add(operand, context);
                case "subtract" -> precision == -2 ? source.subtract(operand) : source.subtract(operand, context);
                case "multiply" -> precision == -2 ? source.multiply(operand) : source.multiply(operand, context);
                default -> throw new AssertionError(operation[0]);
            };
            result = String.valueOf(value);
        } catch (Exception e) {
            result = "!" + e.getClass().getSimpleName() +
                (e instanceof IllegalStateException ? ":" + e.getMessage() : "");
        }
        return result + "|" + source.trace;
    }

    private static class ContextProbe extends BigDecimal {
        final String label, mode;
        final StringBuilder trace;
        int signCalls, precisionCalls;
        ContextProbe(String text, String label, String mode, StringBuilder trace) {
            super(text); this.label = label; this.mode = mode; this.trace = trace;
        }
        @Override public int signum() {
            signCalls++;
            trace.append(label).append(".sign(").append(signCalls).append(");");
            if (mode.equals("sign-first") || mode.equals("sign-second") && signCalls == 2)
                throw new IllegalStateException(label + " sign callback");
            if (mode.startsWith("zero-precision:")) return 1;
            if (mode.startsWith("sign:")) return Integer.parseInt(mode.substring(5));
            if (mode.equals("sign-changing")) return signCalls == 1 ? -super.signum() : super.signum();
            return super.signum();
        }
        @Override public int precision() {
            precisionCalls++;
            trace.append(label).append(".precision;");
            if (mode.equals("precision")) throw new IllegalStateException(label + " precision callback");
            if (mode.startsWith("zero-precision:") || mode.startsWith("precision:"))
                return Integer.parseInt(mode.substring(mode.indexOf(':') + 1));
            return super.precision() + (mode.equals("precision-changing") ? precisionCalls : 0);
        }
        private RuntimeException unexpected(String name) {
            trace.append(label).append('.').append(name).append(';');
            return new IllegalStateException(label + " unexpected " + name);
        }
        @Override public int scale() { throw unexpected("scale"); }
        @Override public BigInteger unscaledValue() { throw unexpected("unscaled"); }
        @Override public BigDecimal abs() { throw unexpected("abs"); }
        @Override public BigDecimal negate() { throw unexpected("negate"); }
        @Override public BigDecimal divide(BigDecimal v) {
            trace.append(label).append(".exact").append(v == null ? "-null" : "").append(';');
            return new BigDecimal("7.00");
        }
        BigDecimal enter(BigDecimal v, MathContext mc) { return super.divide(v, mc); }
    }

    public static String contextDispatch(String[] row) {
        StringBuilder trace = new StringBuilder();
        ContextProbe source = new ContextProbe(row[0], "A", row[2], trace);
        BigDecimal operand = row[3].equals("self") ? source : row[3].equals("null") ? null :
            new ContextProbe(row[1], "B", row[3], trace);
        int precision = Integer.parseInt(row[4]);
        MathContext context = precision < 0 ? null : new MathContext(precision, RoundingMode.valueOf(row[5]));
        BigDecimal result;
        try {
            result = source.enter(operand, context);
        } catch (Exception e) {
            return "!" + e.getClass().getSimpleName() + (e instanceof NullPointerException ? "" : ":" + e.getMessage()) + "|" + trace;
        }
        String calls = trace.toString();
        return result == null ? "null|" + calls : result.toString() + ":" + result.scale() + "|" + calls;
    }

    private static class ArrayProbe extends BigDecimal {
        final int length;
        final StringBuilder trace = new StringBuilder();
        ArrayProbe(String text, int length) { super(text); this.length = length; }
        private BigDecimal[] pair() {
            BigDecimal[] result = new BigDecimal[length];
            for (int i = 0; i < length; i++) result[i] = i == 1 ? this : new BigDecimal("7.0");
            return result;
        }
        @Override public BigDecimal[] divideAndRemainder(BigDecimal v) {
            trace.append("pair(").append(v == null).append(");"); return pair();
        }
        @Override public BigDecimal[] divideAndRemainder(BigDecimal v, MathContext mc) {
            trace.append("pair(").append(v == null).append(',').append(mc == null ? null : mc.getPrecision()).append(");");
            return pair();
        }
        BigDecimal enter(String op, BigDecimal v, MathContext mc) {
            return op.equals("exact") ? super.remainder(v) : super.remainder(v, mc);
        }
    }
    public static String array(String[] row) {
        ArrayProbe source = new ArrayProbe(row[0], Integer.parseInt(row[1]));
        BigDecimal operand = row[3].equals("null") ? null : row[3].equals("self") ? source : new BigDecimal("2.0");
        int precision = Integer.parseInt(row[4]);
        MathContext mc = precision < 0 ? null : new MathContext(precision);
        BigDecimal result;
        try {
            result = source.enter(row[2], operand, mc);
        } catch (Exception e) {
            return "!" + e.getClass().getSimpleName() + ":" + e.getMessage() + "|" + source.trace;
        }
        String calls = source.trace.toString();
        return (result == source ? "source" : String.valueOf(result)) + "|" + calls;
    }

    public static class Truthful extends BigDecimal {
        public Truthful(String text) { super(text); }
        public Truthful(long coefficient, int scale) { super(BigInteger.valueOf(coefficient), scale); }
        @Override public int signum() { return super.signum(); }
        @Override public int precision() { return super.precision(); }
        @Override public int scale() { return super.scale(); }
        @Override public BigInteger unscaledValue() { return super.unscaledValue(); }
        @Override public BigDecimal abs() { return super.abs(); }
        @Override public BigDecimal abs(MathContext mc) { return super.abs(mc); }
        @Override public BigDecimal negate() { return super.negate(); }
        @Override public BigDecimal negate(MathContext mc) { return super.negate(mc); }
        @Override public BigDecimal divide(BigDecimal v) { return super.divide(v); }
        @Override public BigDecimal divide(BigDecimal v, MathContext mc) { return super.divide(v, mc); }
        public BigDecimal divideBase(BigDecimal v, MathContext mc) { return super.divide(v, mc); }
        public BigDecimal integralBase(BigDecimal v, MathContext mc) { return super.divideToIntegralValue(v, mc); }
        public BigDecimal addBase(BigDecimal v, MathContext mc) { return super.add(v, mc); }
        public BigDecimal multiplyBase(BigDecimal v, MathContext mc) { return super.multiply(v, mc); }
    }
    public static String contextContract(String[] row, int mask) {
        BigDecimal source = (mask & 1) == 0 ? new BigDecimal(row[0]) : new Truthful(row[0]);
        BigDecimal operand = switch (row[2]) {
            case "null" -> null;
            case "self" -> source;
            case "ordinary" -> (mask & 2) == 0 ? new BigDecimal(row[1]) : new Truthful(row[1]);
            default -> throw new AssertionError(row[2]);
        };
        int precision = Integer.parseInt(row[3]);
        MathContext mc = precision < 0 ? null : new MathContext(precision, RoundingMode.valueOf(row[4]));
        BigDecimal result;
        try {
            result = source.divide(operand, mc);
        } catch (ArithmeticException e) { return "!ArithmeticException"; }
          catch (NullPointerException e) { return "!NullPointerException"; }
        return result == null ? "null" : result.unscaledValue().toString() + "@" + result.scale();
    }
}

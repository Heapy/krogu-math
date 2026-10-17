// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

/** Actual Java entry points. No casts, null assertions, or manufactured exceptions. */
public final class Validation578Boundary {
    private Validation578Boundary() {}
    public static BigInteger signedBytes(int sign, byte[] bytes, int off, int len) {
        return new BigInteger(sign, bytes, off, len);
    }
    public static BigInteger modPow(BigInteger receiver, BigInteger exponent, BigInteger modulus) {
        return receiver.modPow(exponent, modulus);
    }
    public static BigDecimal decimal(String value, MathContext context) { return new BigDecimal(value, context); }
    public static BigDecimal decimal(double value, MathContext context) { return new BigDecimal(value, context); }
    public static BigDecimal decimal(char[] value, MathContext context) { return new BigDecimal(value, context); }
    public static BigDecimal decimal(char[] value, int off, int len, MathContext context) {
        return new BigDecimal(value, off, len, context);
    }
    public static BigDecimal sqrt(BigDecimal receiver, MathContext context) { return receiver.sqrt(context); }
    public static BigDecimal divide(BigDecimal receiver, BigDecimal divisor, int mode) { return receiver.divide(divisor, mode); }
    public static BigDecimal divide(BigDecimal receiver, BigDecimal divisor, int scale, int mode) {
        return receiver.divide(divisor, scale, mode);
    }
    public static MathContext context(int precision, RoundingMode mode) { return new MathContext(precision, mode); }
    public static BigInteger random(String operation, int bits, int certainty) {
        java.util.Random random = null;
        return switch (operation) {
            case "random" -> new BigInteger(bits, random);
            case "prime" -> new BigInteger(bits, certainty, random);
            case "probablePrime" -> BigInteger.probablePrime(bits, random);
            default -> throw new AssertionError("Unknown RNG operation " + operation);
        };
    }

    public static class TruthfulRoot extends BigDecimal {
        public TruthfulRoot(BigInteger coefficient, int scale) { super(coefficient, scale); }
        @Override public int signum() { return super.signum(); }
        @Override public int precision() { return super.precision(); }
        @Override public int scale() { return super.scale(); }
        @Override public BigInteger unscaledValue() { return super.unscaledValue(); }
        @Override public BigDecimal stripTrailingZeros() { return super.stripTrailingZeros(); }
        @Override public BigDecimal sqrt(MathContext context) { return super.sqrt(context); }
        // This must remain an invokespecial Java super call, never reflection.
        public BigDecimal inheritedRoot(MathContext context) { return super.sqrt(context); }
    }
}

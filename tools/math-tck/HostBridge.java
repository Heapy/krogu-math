// SPDX-License-Identifier: Apache-2.0
// Explicit host-integration bridge; never evidence of port Float16/RSA arithmetic.
package mathharness;

public final class HostBridge {
    private HostBridge() {}
    public static java.math.BigInteger rsaExponent(long value) {
        return java.math.BigInteger.valueOf(value);
    }
    public static java.math.BigDecimal decimal(io.heapy.krogu.math.BigDecimal value) {
        return new java.math.BigDecimal(value.toString());
    }
}

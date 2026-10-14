// SPDX-License-Identifier: Apache-2.0
// Test-only classloading precondition; no numerical reference implementation.
package mathharness;

import java.net.URI;
import java.nio.file.Path;

public final class CoreGuard {
    private CoreGuard() {}

    public static void verify() throws Exception {
        verify(System.out);
    }
    public static void verify(java.io.PrintStream evidence) throws Exception {
        Path expected = Path.of(System.getProperty("math.harness.portJar")).toRealPath();
        for (String simple : new String[] {"BigInteger", "BigDecimal", "MathContext", "RoundingMode"}) {
            Class<?> type = Class.forName("io.heapy.krogu.math." + simple);
            URI location = type.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path actual = Path.of(location).toRealPath();
            if (!actual.equals(expected)) {
                throw new AssertionError("Unexpected core location: " + type.getName() + " " + actual);
            }
            evidence.println("CORE_CLASS " + type.getName() + " " + location);
        }
    }
}

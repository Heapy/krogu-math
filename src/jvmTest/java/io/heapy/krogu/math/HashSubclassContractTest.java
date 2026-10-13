package io.heapy.krogu.math;

import org.junit.Test;
import static org.junit.Assert.*;

public class HashSubclassContractTest {
    @Test public void hashDoesNotInvokePublicMagnitudeOperations() {
        for (String text : new String[] {"0", "1", "-1", "4294967295", "-4294967295", "18446744073709551616"}) {
            java.math.BigInteger expected = new java.math.BigInteger(text) {
                @Override public java.math.BigInteger abs() { throw new AssertionError("abs called"); }
                @Override public boolean testBit(int n) { throw new AssertionError("testBit called"); }
            };
            BigInteger actual = new BigInteger(text) {
                @Override public BigInteger abs() { throw new AssertionError("abs called"); }
                @Override public boolean testBit(int n) { throw new AssertionError("testBit called"); }
            };
            assertEquals(expected.hashCode(), actual.hashCode());
        }
    }
}

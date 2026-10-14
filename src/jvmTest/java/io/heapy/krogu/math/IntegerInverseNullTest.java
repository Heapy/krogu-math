// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;

import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IntegerInverseNullTest {
    private static class Reference extends java.math.BigInteger {
        final String label, hook;
        final StringBuilder trace;
        final boolean nullable;
        Reference(String text, String label, StringBuilder trace, String hook, boolean nullable) {
            super(text); this.label=label; this.trace=trace; this.hook=hook; this.nullable=nullable;
        }
        boolean call(String name) {
            trace.append(label).append('.').append(name).append(';');
            if (!hook.equals(name)) return false;
            if (!nullable) throw new IllegalStateException(name+" callback");
            return true;
        }
        @Override public boolean equals(Object value) { call("equals"); return super.equals(value); }
        @Override public java.math.BigInteger mod(java.math.BigInteger m) { return call("mod") ? null : super.mod(m); }
        @Override public java.math.BigInteger remainder(java.math.BigInteger m) { return call("rem") ? null : super.remainder(m); }
        @Override public java.math.BigInteger abs() { return call("abs") ? null : super.abs(); }
        @Override public java.math.BigInteger negate() { return call("neg") ? null : super.negate(); }
        @Override public java.math.BigInteger add(java.math.BigInteger m) { return call("add") ? null : super.add(m); }
        @Override public java.math.BigInteger[] divideAndRemainder(java.math.BigInteger m) { return call("pair") ? null : super.divideAndRemainder(m); }
        java.math.BigInteger inherited(boolean inverse, java.math.BigInteger m) { return inverse ? super.modInverse(m) : super.mod(m); }
    }
    private static class Actual extends BigInteger {
        final String label, hook;
        final StringBuilder trace;
        final boolean nullable;
        Actual(String text, String label, StringBuilder trace, String hook, boolean nullable) {
            super(text); this.label=label; this.trace=trace; this.hook=hook; this.nullable=nullable;
        }
        boolean call(String name) {
            trace.append(label).append('.').append(name).append(';');
            if (!hook.equals(name)) return false;
            if (!nullable) throw new IllegalStateException(name+" callback");
            return true;
        }
        @Override public boolean equals(Object value) { call("equals"); return super.equals(value); }
        @Override public BigInteger mod(BigInteger m) { return call("mod") ? null : super.mod(m); }
        @Override public BigInteger remainder(BigInteger m) { return call("rem") ? null : super.remainder(m); }
        @Override public BigInteger abs() { return call("abs") ? null : super.abs(); }
        @Override public BigInteger negate() { return call("neg") ? null : super.negate(); }
        @Override public BigInteger add(BigInteger m) { return call("add") ? null : super.add(m); }
        @Override public BigInteger[] divideAndRemainder(BigInteger m) { return call("pair") ? null : super.divideAndRemainder(m); }
        BigInteger inherited(boolean inverse, BigInteger m) { return inverse ? super.modInverse(m) : super.mod(m); }
    }
    private static String outcome(Callable<?> action, StringBuilder trace) {
        try { Object value=action.call(); String calls=trace.toString(); return value+"|"+calls; }
        catch (Exception e) { return "!"+e.getClass().getSimpleName()+"|"+trace; }
    }
    @Test public void nullableAndThrowingCallbacksFollowJdkRoutes() {
        for (String text : new String[] {"-3","0","1","3","20"})
            for (String modulus : new String[] {"-1","0","1","2","10","17"})
                for (String hook : new String[] {"mod","rem","abs","neg","add","pair"})
                    for (boolean nullable : new boolean[] {false,true}) for (boolean inverse : new boolean[] {false,true}) {
                        StringBuilder et=new StringBuilder(), at=new StringBuilder();
                        Reference e=new Reference(text,"A",et,hook,nullable), em=new Reference(modulus,"M",et,"",false);
                        Actual a=new Actual(text,"A",at,hook,nullable), am=new Actual(modulus,"M",at,"",false);
                        assertEquals(text+"/"+modulus+"/"+hook+"/"+nullable+"/"+inverse,
                            outcome(() -> e.inherited(inverse,em),et), outcome(() -> a.inherited(inverse,am),at));
                    }
    }
    @Test public void nullModulusFailsBeforeCallbacks() {
        for (String text : new String[] {"-3","0","1","3","20"}) for (boolean inverse : new boolean[] {false,true}) {
            StringBuilder et=new StringBuilder(), at=new StringBuilder();
            Reference e=new Reference(text,"A",et,"mod",false);
            Actual a=new Actual(text,"A",at,"mod",false);
            assertEquals(text+"/null/"+inverse, outcome(() -> e.inherited(inverse,null),et), outcome(() -> a.inherited(inverse,null),at));
        }
    }
}

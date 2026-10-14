// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math;
import java.util.concurrent.Callable;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
public class IntegerModPowNullTest {
private static class Reference extends java.math.BigInteger {
    final String text,label,hook; final StringBuilder trace; final boolean nullable;
    Reference(String text,String label,StringBuilder trace,String hook,boolean nullable) { super(text);this.text=text;this.label=label;this.trace=trace;this.hook=hook;this.nullable=nullable; }
    String txt(java.math.BigInteger v){return v==null?"null":v instanceof Reference?((Reference)v).text:v.toString();}
    boolean call(String name,String detail){trace.append(label).append('.').append(name).append(detail).append(';');if(!hook.equals(name))return false;if(!nullable)throw new IllegalStateException(name+" callback");return true;}
    @Override public boolean equals(Object v){call("equals","("+(v instanceof java.math.BigInteger?txt((java.math.BigInteger)v):v)+")");return super.equals(v);}
    @Override public int compareTo(java.math.BigInteger v){call("compare","("+txt(v)+")");return super.compareTo(v);}
    @Override public int bitLength(){call("bits","");return super.bitLength();}
    @Override public boolean testBit(int n){call("test","("+n+")");return super.testBit(n);}
    @Override public int getLowestSetBit(){call("lowest","");return super.getLowestSetBit();}
    @Override public java.math.BigInteger mod(java.math.BigInteger v){return call("mod","("+txt(v)+")")?null:super.mod(v);}
    @Override public java.math.BigInteger remainder(java.math.BigInteger v){return call("rem","("+txt(v)+")")?null:super.remainder(v);}
    @Override public java.math.BigInteger modInverse(java.math.BigInteger v){return call("inverse","("+txt(v)+")")?null:super.modInverse(v);}
    @Override public java.math.BigInteger negate(){return call("neg","")?null:super.negate();}
    @Override public java.math.BigInteger abs(){return call("abs","")?null:super.abs();}
    @Override public java.math.BigInteger shiftRight(int n){return call("right","("+n+")")?null:super.shiftRight(n);}
    @Override public java.math.BigInteger multiply(java.math.BigInteger v){return call("mul","("+txt(v)+")")?null:super.multiply(v);}
    java.math.BigInteger inherited(java.math.BigInteger e,java.math.BigInteger m){return super.modPow(e,m);}
}
private static class Actual extends BigInteger {
    final String text,label,hook; final StringBuilder trace; final boolean nullable;
    Actual(String text,String label,StringBuilder trace,String hook,boolean nullable) { super(text);this.text=text;this.label=label;this.trace=trace;this.hook=hook;this.nullable=nullable; }
    String txt(BigInteger v){return v==null?"null":v instanceof Actual?((Actual)v).text:v.toString();}
    boolean call(String name,String detail){trace.append(label).append('.').append(name).append(detail).append(';');if(!hook.equals(name))return false;if(!nullable)throw new IllegalStateException(name+" callback");return true;}
    @Override public boolean equals(Object v){call("equals","("+(v instanceof BigInteger?txt((BigInteger)v):v)+")");return super.equals(v);}
    @Override public int compareTo(BigInteger v){call("compare","("+txt(v)+")");return super.compareTo(v);}
    @Override public int bitLength(){call("bits","");return super.bitLength();}
    @Override public boolean testBit(int n){call("test","("+n+")");return super.testBit(n);}
    @Override public int getLowestSetBit(){call("lowest","");return super.getLowestSetBit();}
    @Override public BigInteger mod(BigInteger v){return call("mod","("+txt(v)+")")?null:super.mod(v);}
    @Override public BigInteger remainder(BigInteger v){return call("rem","("+txt(v)+")")?null:super.remainder(v);}
    @Override public BigInteger modInverse(BigInteger v){return call("inverse","("+txt(v)+")")?null:super.modInverse(v);}
    @Override public BigInteger negate(){return call("neg","")?null:super.negate();}
    @Override public BigInteger abs(){return call("abs","")?null:super.abs();}
    @Override public BigInteger shiftRight(int n){return call("right","("+n+")")?null:super.shiftRight(n);}
    @Override public BigInteger multiply(BigInteger v){return call("mul","("+txt(v)+")")?null:super.multiply(v);}
    BigInteger inherited(BigInteger e,BigInteger m){return super.modPow(e,m);}
}
    private static String outcome(Callable<?> action,StringBuilder trace){
        try{Object r=action.call();String calls=trace.toString();String text=r instanceof Reference?((Reference)r).text:r instanceof Actual?((Actual)r).text:String.valueOf(r);return text+"|"+calls;}catch(Exception e){return "!"+e.getClass().getSimpleName()+"|"+trace;}
    }
    @Test public void nullableAndThrowingCallbacks(){
        for(String a:new String[]{"-3","0","1","3","20"})for(String e:new String[]{"-3","-1","0","1","2","3"})for(String m:new String[]{"1","4","10","17"})
            for(String hook:new String[]{"mod","rem","inverse","neg","right","abs","mul"})for(int position=0;position<3;position++)for(boolean nullable:new boolean[]{false,true}){
                StringBuilder et=new StringBuilder(),at=new StringBuilder();
                Reference ra=new Reference(a,"A",et,position==0?hook:"",nullable),re=new Reference(e,"E",et,position==1?hook:"",nullable),rm=new Reference(m,"M",et,position==2?hook:"",nullable);
                Actual aa=new Actual(a,"A",at,position==0?hook:"",nullable),ae=new Actual(e,"E",at,position==1?hook:"",nullable),am=new Actual(m,"M",at,position==2?hook:"",nullable);
                assertEquals(a+"/"+e+"/"+m+"/"+hook+"/"+position+"/"+nullable,outcome(()->ra.inherited(re,rm),et),outcome(()->aa.inherited(ae,am),at));
            }
    }
    @Test public void nullArgumentPrecedence(){
        for(String a:new String[]{"0","3"})for(String e:new String[]{null,"0","-1"})for(String m:new String[]{null,"-1","0","1","17"}){
            StringBuilder et=new StringBuilder(),at=new StringBuilder();
            Reference ra=new Reference(a,"A",et,"mod",false),re=e==null?null:new Reference(e,"E",et,"neg",false),rm=m==null?null:new Reference(m,"M",et,"",false);
            Actual aa=new Actual(a,"A",at,"mod",false),ae=e==null?null:new Actual(e,"E",at,"neg",false),am=m==null?null:new Actual(m,"M",at,"",false);
            assertEquals(a+"/"+e+"/"+m,outcome(()->ra.inherited(re,rm),et),outcome(()->aa.inherited(ae,am),at));
        }
    }
    @Test public void primalityParityCallbackPrecedesWitnesses(){
        for(String text:new String[]{"0","1","2","97","65537","2147483647","-97"}) {
            StringBuilder et=new StringBuilder(),at=new StringBuilder();
            java.math.BigInteger reference=new java.math.BigInteger(text) {
                @Override public boolean testBit(int n){et.append("test(").append(n).append(");");throw new IllegalStateException("parity callback");}
            };
            BigInteger actual=new BigInteger(text) {
                @Override public boolean testBit(int n){at.append("test(").append(n).append(");");throw new IllegalStateException("parity callback");}
            };
            assertEquals(text,outcome(()->reference.isProbablePrime(20),et),outcome(()->actual.isProbablePrime(20),at));
        }
    }
}

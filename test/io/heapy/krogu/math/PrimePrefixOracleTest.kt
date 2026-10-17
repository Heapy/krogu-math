// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals
private class PrimePrefixProbe(text:String,val trace:StringBuilder):BigInteger(text){
    override fun abs():BigInteger{trace.append("abs;");return super.abs()}
    override fun equals(v:Any?):Boolean{trace.append("eq($v);");return super.equals(v)}
    override fun testBit(n:Int):Boolean{trace.append("test($n);");return super.testBit(n)}
    override fun bitLength():Int{trace.append("bits;");throw IllegalStateException("bits callback")}
}
private fun prefixOutcome(text:String,certainty:Int):String{
    val trace=StringBuilder();val source=PrimePrefixProbe(text,trace)
    return try{val result=source.isProbablePrime(certainty);"$result|$trace"}catch(e:Exception){"!${e::class.simpleName}:${e.message}|$trace"}
}
class PrimePrefixOracleTest{
    @Test fun virtualLengthFailuresAndEarlyReturns(){
        for(row in jdk25PrimePrefixCases())assertEquals(row[2],prefixOutcome(row[0],row[1].toInt()),row.take(2).toString())
    }
}

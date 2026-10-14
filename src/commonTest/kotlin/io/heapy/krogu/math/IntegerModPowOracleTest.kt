// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertSame
import kotlin.test.assertNotSame

private class ModPowProbe(val text: String, val label: String, val trace: StringBuilder, val mode: String) : BigInteger(text) {
    val fixed = BigInteger(when(mode) { "mod:zero" -> "0"; "mod:one" -> "1"; "mod:negative","neg:negative" -> "-7"; else -> "7" })
    private fun txt(value: BigInteger): String = if(value is ModPowProbe) value.text else value.toString()
    private fun call(name: String, detail: String="") { trace.append("$label.$name$detail;"); if(mode=="$name:throw") throw IllegalStateException("$name callback") }
    override fun equals(value: Any?): Boolean { call("equals", "(${if(value is BigInteger)txt(value!!)else value})"); return when(mode) { "equals:true" -> true; "equals:false" -> false; else -> super.equals(value) } }
    override fun compareTo(value: BigInteger?): Int { call("compare","(${txt(value!!)})"); return when(mode) { "compare:positive" -> 1; "compare:negative" -> -1; else -> super.compareTo(value) } }
    override fun mod(value: BigInteger): BigInteger { call("mod","(${txt(value!!)})"); return when(mode) {
        "mod:self" -> this; "mod:fixed","mod:zero","mod:one","mod:negative" -> fixed; else -> super.mod(value)
    } }
    override fun remainder(value: BigInteger): BigInteger { call("rem","(${txt(value!!)})"); return super.remainder(value) }
    override fun modInverse(value: BigInteger): BigInteger { call("inverse","(${txt(value!!)})"); return super.modInverse(value) }
    override fun abs(): BigInteger { call("abs"); return super.abs() }
    override fun negate(): BigInteger { call("neg"); return if(mode in listOf("neg:fixed","neg:negative"))fixed else super.negate() }
    override fun bitLength(): Int { call("bits"); return when(mode) { "bits:zero" -> 0; "bits:one" -> 1; else -> super.bitLength() } }
    override fun testBit(n: Int): Boolean { call("test","($n)"); return when(mode) { "test:true" -> true; "test:false" -> false; else -> super.testBit(n) } }
    override fun getLowestSetBit(): Int { call("lowest"); return super.getLowestSetBit() }
    override fun shiftLeft(n: Int): BigInteger { call("left","($n)"); return super.shiftLeft(n) }
    override fun shiftRight(n: Int): BigInteger { call("right","($n)"); return super.shiftRight(n) }
    override fun add(value: BigInteger): BigInteger { call("add","(${txt(value!!)})"); return super.add(value) }
    override fun subtract(value: BigInteger): BigInteger { call("sub","(${txt(value!!)})"); return super.subtract(value) }
    override fun multiply(value: BigInteger): BigInteger { call("mul","(${txt(value!!)})"); return super.multiply(value) }
    fun inherited(e: BigInteger,m: BigInteger): BigInteger = super.modPow(e,m)
}
private fun modPowOutcome(row: Array<String>): String {
    val trace=StringBuilder()
    val a=ModPowProbe(row[0],"A",trace,row[3]); val e=ModPowProbe(row[1],"E",trace,row[4]); val m=ModPowProbe(row[2],"M",trace,row[5])
    return try {
        val r=a.inherited(e,m); val calls=trace.toString()
        val text=if(r is ModPowProbe)r.text else r.toString()
        "$text|${r===a}|${r===e}|${r===m}|${r===a.fixed}|${r===e.fixed}|${r===m.fixed}|${r===BigInteger.ZERO}|${r===BigInteger.ONE}|${r::class==BigInteger::class}|$calls"
    } catch(v:Exception) { "!${v::class.simpleName}:${v.message}|$trace" }
}
class IntegerModPowOracleTest {
    @Test fun negativeOneEqualityArgumentAndFactoryIdentity() {
        val refs=mutableListOf<BigInteger>()
        val source=object:BigInteger("3") {
            override fun equals(other:Any?):Boolean {
                if(other is BigInteger && other.toString()=="-1")refs+=other
                return super.equals(other)
            }
        }
        repeat(2){source.modPow(BigInteger.TWO,BigInteger("17"))}
        assertEquals(2,refs.size)
        assertSame(refs[0],refs[1])
        assertSame(BigInteger.valueOf(-1),refs[0])
        for(n in -20L..20L) {
            val a=BigInteger.valueOf(n);val b=BigInteger.valueOf(n)
            assertEquals(n in -16L..16L,a===b,"factory identity $n")
            assertEquals(n.toString(),a.toString())
        }
        assertSame(BigInteger.ZERO,BigInteger.valueOf(0))
        assertSame(BigInteger.ONE,BigInteger.valueOf(1))
        assertSame(BigInteger.TWO,BigInteger.valueOf(2))
        assertSame(BigInteger.TEN,BigInteger.valueOf(10))
        val random=kotlin.random.Random(5)
        repeat(6) {
            val prime=BigInteger.probablePrime(2,random)
            assertTrue(prime==BigInteger.TWO || prime==BigInteger.valueOf(3))
            assertNotSame(BigInteger.valueOf(prime.toLong()),prime)
        }
    }
    @Test fun ordinaryValuesAndIdentities()=verify { it[6]=="ordinary" }
    @Test fun sourceCallbacks()=verify { it[6]=="dispatch"&&it[3]!="delegate" }
    @Test fun exponentCallbacks()=verify { it[6]=="dispatch"&&it[4]!="delegate" }
    @Test fun modulusCallbacks()=verify { it[6]=="dispatch"&&it[5]!="delegate" }
    @Test fun wideBoundaries()=verify { it[6]=="wide" }
    private fun verify(select:(Array<String>)->Boolean) {
        for((index,row)in jdk25IntegerModPowCases().withIndex())if(select(row)) {
            val actual = modPowOutcome(row)
            assertEquals(row[7],actual,"modPow case $index ${row.take(7)}")
            if(row[6] != "dispatch") {
                val a=BigInteger(row[0]);val m=BigInteger(row[2]);val e=BigInteger(row[1])
                if(m.signum()>0) {
                    if(actual.startsWith('!')) assertTrue(e.signum()<0 && a.gcd(m)!=BigInteger.ONE,"failure coprimality case $index")
                    else {
                        val r=BigInteger(actual.substringBefore('|'))
                        assertTrue(r>=BigInteger.ZERO && r<m,"residue range case $index")
                        row[1].toIntOrNull()?.let { n ->
                            if(n in 0..64)assertEquals(a.pow(n).mod(m),r,"full power residue case $index")
                            if(n in -64..-1)assertEquals(BigInteger.ONE.mod(m),a.pow(-n).multiply(r).mod(m),"inverse power congruence case $index")
                        }
                    }
                }
            }
        }
    }
}

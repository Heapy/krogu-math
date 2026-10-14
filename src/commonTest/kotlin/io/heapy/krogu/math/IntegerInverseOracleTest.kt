// SPDX-License-Identifier: Apache-2.0
package io.heapy.krogu.math
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class InverseProbe(text: String, val label: String, val trace: StringBuilder, val mode: String) : BigInteger(text) {
    var other: BigInteger = BigInteger.ZERO
    val fixed = BigInteger(when (mode.substringAfter(':')) { "zero" -> "0"; "one" -> "1"; "negative" -> "-7"; else -> "7" })
    private fun call(name: String) { trace.append("$label.$name;"); if (mode == "$name:throw") throw IllegalStateException("$name callback") }
    private fun replace(name: String, fallback: () -> BigInteger): BigInteger = when (mode) {
        "$name:self" -> this; "$name:other" -> other
        "$name:fixed", "$name:zero", "$name:one", "$name:negative" -> fixed
        else -> fallback()
    }
    override fun mod(m: BigInteger): BigInteger { call("mod"); return replace("mod") { super.mod(m) } }
    override fun remainder(m: BigInteger): BigInteger { call("rem"); return replace("rem") { super.remainder(m) } }
    override fun equals(value: Any?): Boolean { call("equals"); return when (mode) { "equals:true" -> true; "equals:false" -> false; else -> super.equals(value) } }
    override fun compareTo(value: BigInteger?): Int { call("compare"); return super.compareTo(value) }
    override fun signum(): Int { call("sign"); return super.signum() }
    override fun bitLength(): Int { call("bits"); return super.bitLength() }
    override fun getLowestSetBit(): Int { call("lowest"); return super.getLowestSetBit() }
    override fun testBit(n: Int): Boolean { call("test"); return super.testBit(n) }
    override fun abs(): BigInteger { call("abs"); return super.abs() }
    override fun negate(): BigInteger { call("neg"); return super.negate() }
    override fun add(m: BigInteger): BigInteger { call("add"); return super.add(m) }
    override fun subtract(m: BigInteger): BigInteger { call("sub"); return super.subtract(m) }
    override fun multiply(m: BigInteger): BigInteger { call("mul"); return super.multiply(m) }
    override fun divideAndRemainder(m: BigInteger): Array<BigInteger> { call("pair"); return super.divideAndRemainder(m) }
    fun inherited(op: String, m: BigInteger): BigInteger = if (op == "mod") super.mod(m) else super.modInverse(m)
}
private fun inverseOutcome(row: Array<String>): String {
    val trace = StringBuilder()
    val a = InverseProbe(row[0], "A", trace, row[3])
    val m = if (row[5] == "self") a else InverseProbe(row[1], "M", trace, row[4])
    a.other = m; m.other = a
    return try {
        val r = a.inherited(row[2], m)
        val calls = trace.toString()
        val text = if (r === a) row[0] else if (r === m) row[1] else r.toString()
        "$text|${r === a}|${r === m}|${r === a.fixed}|${r === m.fixed}|${r === BigInteger.ZERO}|${r === BigInteger.ONE}|${r::class == BigInteger::class}|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$trace" }
}
class IntegerInverseOracleTest {
    @Test fun reductionCallbacks() = verify { it[6] == "dispatch" && it[2] == "mod" }
    @Test fun inverseSourceCallbacks() = verify { it[6] == "dispatch" && it[2] == "inverse" && it[4] == "delegate" }
    @Test fun inverseModulusCallbacks() = verify { it[6] == "dispatch" && it[2] == "inverse" && it[4] != "delegate" }
    @Test fun wideReductions() = verify { it[6] == "wide" && it[2] == "mod" }
    @Test fun wideInverses() = verify { it[6] == "wide" && it[2] == "inverse" }
    private fun verify(select: (Array<String>) -> Boolean) {
        for((index,row) in jdk25IntegerInverseCases().withIndex()) if(select(row)) {
            val actual = inverseOutcome(row)
            assertEquals(row[7],actual,"integer inverse case $index ${row.take(7)}")
            if (row[6] == "wide" && row[2] == "inverse") {
                val a = BigInteger(row[0]); val m = BigInteger(row[1])
                if (actual.startsWith('!')) assertTrue(a.gcd(m) != BigInteger.ONE, "non-invertible gcd case $index")
                else {
                    val inverse = BigInteger(actual.substringBefore('|'))
                    assertTrue(inverse >= BigInteger.ZERO && inverse < m, "residue range case $index")
                    assertEquals(BigInteger.ONE, a.multiply(inverse).mod(m), "inverse identity case $index")
                }
            }
        }
    }
}

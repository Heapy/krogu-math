#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Public-JDK modular exponentiation values, identities, errors and callback arguments. Usage: [--check]. */
import java.io.File
import java.math.BigInteger
check(Runtime.version().toString() in listOf("25.0.4+9", "25.0.4+9-LTS"))
check(args.all { it=="--check" })
for(n in -20L..20L) check((BigInteger.valueOf(n)===BigInteger.valueOf(n))==(n in -16L..16L))
check(BigInteger.valueOf(0)===BigInteger.ZERO && BigInteger.valueOf(1)===BigInteger.ONE && BigInteger.valueOf(2)===BigInteger.TWO && BigInteger.valueOf(10)===BigInteger.TEN)
val negativeRefs=mutableListOf<BigInteger>()
val argumentProbe=object:BigInteger("3") {
    override fun toByte():Byte=super.toInt().toByte()
    override fun toShort():Short=super.toInt().toShort()
    override fun equals(other:Any?):Boolean { if(other is BigInteger && other.toString()=="-1")negativeRefs+=other;return super.equals(other) }
}
repeat(2){argumentProbe.modPow(BigInteger.TWO,BigInteger.valueOf(17))}
check(negativeRefs.size==2 && negativeRefs[0]===negativeRefs[1] && negativeRefs[0]===BigInteger.valueOf(-1))
val primeRandom=java.util.Random(5)
repeat(6){val prime=BigInteger.probablePrime(2,primeRandom);check(prime!==BigInteger.valueOf(prime.toLong()))}
class ModPowProbe(val text: String, val label: String, val trace: StringBuilder, val mode: String) : BigInteger(text) {
    val fixed = BigInteger(when(mode) { "mod:zero" -> "0"; "mod:one" -> "1"; "mod:negative","neg:negative" -> "-7"; else -> "7" })
    override fun toByte():Byte=super.toInt().toByte()
    override fun toShort():Short=super.toInt().toShort()
    private fun txt(value: BigInteger): String = if(value is ModPowProbe) value.text else value.toString()
    private fun call(name: String, detail: String="") { trace.append("$label.$name$detail;"); if(mode=="$name:throw") throw IllegalStateException("$name callback") }
    override fun equals(value: Any?): Boolean { call("equals", "(${if(value is BigInteger)txt(value)else value})"); return when(mode) { "equals:true" -> true; "equals:false" -> false; else -> super.equals(value) } }
    override fun compareTo(value: BigInteger): Int { call("compare","(${txt(value)})"); return when(mode) { "compare:positive" -> 1; "compare:negative" -> -1; else -> super.compareTo(value) } }
    override fun mod(value: BigInteger): BigInteger { call("mod","(${txt(value)})"); return when(mode) {
        "mod:self" -> this; "mod:fixed","mod:zero","mod:one","mod:negative" -> fixed; else -> super.mod(value)
    } }
    override fun remainder(value: BigInteger): BigInteger { call("rem","(${txt(value)})"); return super.remainder(value) }
    override fun modInverse(value: BigInteger): BigInteger { call("inverse","(${txt(value)})"); return super.modInverse(value) }
    override fun abs(): BigInteger { call("abs"); return super.abs() }
    override fun negate(): BigInteger { call("neg"); return if(mode in listOf("neg:fixed","neg:negative"))fixed else super.negate() }
    override fun bitLength(): Int { call("bits"); return when(mode) { "bits:zero" -> 0; "bits:one" -> 1; else -> super.bitLength() } }
    override fun testBit(n: Int): Boolean { call("test","($n)"); return when(mode) { "test:true" -> true; "test:false" -> false; else -> super.testBit(n) } }
    override fun getLowestSetBit(): Int { call("lowest"); return super.getLowestSetBit() }
    override fun shiftLeft(n: Int): BigInteger { call("left","($n)"); return super.shiftLeft(n) }
    override fun shiftRight(n: Int): BigInteger { call("right","($n)"); return super.shiftRight(n) }
    override fun add(value: BigInteger): BigInteger { call("add","(${txt(value)})"); return super.add(value) }
    override fun subtract(value: BigInteger): BigInteger { call("sub","(${txt(value)})"); return super.subtract(value) }
    override fun multiply(value: BigInteger): BigInteger { call("mul","(${txt(value)})"); return super.multiply(value) }
    fun inherited(e: BigInteger,m: BigInteger): BigInteger = super.modPow(e,m)
}
fun modPowOutcome(row: Array<String>): String {
    val trace=StringBuilder()
    val a=ModPowProbe(row[0],"A",trace,row[3]); val e=ModPowProbe(row[1],"E",trace,row[4]); val m=ModPowProbe(row[2],"M",trace,row[5])
    return try {
        val r=a.inherited(e,m); val calls=trace.toString()
        val text=if(r is ModPowProbe)r.text else r.toString()
        "$text|${r===a}|${r===e}|${r===m}|${r===a.fixed}|${r===e.fixed}|${r===m.fixed}|${r===BigInteger.ZERO}|${r===BigInteger.ONE}|${r::class==BigInteger::class}|$calls"
    } catch(v:Exception) { "!${v::class.simpleName}:${v.message}|$trace" }
}
val rows=mutableListOf<Array<String>>()
fun record(a:String,e:String,m:String,am:String="delegate",em:String="delegate",mm:String="delegate",family:String="dispatch") {
    val row=arrayOf(a,e,m,am,em,mm,family)
    rows += row+modPowOutcome(row)
}
for(a in listOf("-20","-3","-1","0","1","2","3","20"))
    for(e in listOf("-17","-3","-2","-1","0","1","2","3","17","64","2147483648"))
        for(m in listOf("-1","0","1","2","4","8","10","17","32","45","65537")) record(a,e,m,family="ordinary")
val throwing=listOf("equals","compare","mod","rem","inverse","neg","abs","bits","test","lowest","right","left","mul","add","sub").map { "$it:throw" }
for(a in listOf("-3","0","1","3","20")) for(e in listOf("-3","-1","0","1","2","3")) for(m in listOf("1","4","10","17")) {
    for(mode in throwing+listOf("equals:true","equals:false","compare:positive","compare:negative","bits:zero","bits:one","mod:self","mod:fixed","mod:zero","mod:one","mod:negative")) record(a,e,m,am=mode)
    for(mode in throwing+listOf("equals:true","equals:false","bits:zero","bits:one","test:true","test:false","neg:fixed","neg:negative")) record(a,e,m,em=mode)
    for(mode in throwing+listOf("equals:true","equals:false")) record(a,e,m,mm=mode)
}
for(bits in listOf(31,32,63,64,127,128,255,256)) {
    val p=BigInteger.ONE.shiftLeft(bits)
    for(m in listOf(p.subtract(BigInteger.ONE),p,p.add(BigInteger.ONE)))
        for(a in listOf(BigInteger.valueOf(-3),BigInteger.valueOf(3),m.add(BigInteger.TWO)))
            for(e in listOf("-3","1","17","18446744073709551617")) record(a.toString(),e,m.toString(),family="wide")
}
fun quote(s:String):String="\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("$","\\$").replace("\n","\\n").replace("\t","\\t")+"\""
val source=buildString {
    appendLine("// SPDX-License-Identifier: Apache-2.0")
    appendLine("// Generated by scripts/generate-integer-modpow-oracle.main.kts; JDK 25.0.4+9.")
    appendLine("package io.heapy.krogu.math")
    val chunks=rows.chunked(30)
    appendLine("internal fun jdk25IntegerModPowCases():List<Array<String>> = listOf(")
    for(i in chunks.indices)appendLine("    integerModPowBatch$i,")
    appendLine(").flatMap { batch -> batch.split('\\n').map { it.split('\\t').toTypedArray() } }")
    for((i,chunk)in chunks.withIndex())appendLine("private const val integerModPowBatch$i = ${quote(chunk.joinToString("\n") { it.joinToString("\t") })}")
}
val target=File("src/commonTest/kotlin/io/heapy/krogu/math/Jdk25IntegerModPowResults.kt")
if("--check" in args)check(target.exists()&&target.readText()==source){"Integer modPow oracle differs"}else target.writeText(source)
println("${if("--check" in args)"Verified" else "Generated"} ${rows.size} modular exponentiation cases")

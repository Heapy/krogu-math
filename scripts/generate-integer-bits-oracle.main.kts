#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/** Public-JDK bitwise value, identity and virtual metadata oracle.
 * Usage: [--check] [--check-extreme]. Extreme checks require JAVA_OPTS=-Xmx1g.
 */
import java.io.File
import java.math.BigInteger
check(Runtime.version().toString() in listOf("25.0.4+9", "25.0.4+9-LTS"))
check(args.all { it in listOf("--check", "--check-extreme") })
if ("--check-extreme" in args) {
    check(Runtime.getRuntime().maxMemory() >= 768L * 1024 * 1024) { "Extreme checks require JAVA_OPTS=-Xmx1g" }
    for (text in listOf("0", "-1")) for (op in listOf("set", "clear", "flip")) {
        System.gc()
        val value = BigInteger(text)
        val unchanged = (text == "0" && op == "clear") || (text == "-1" && op == "set")
        val result = try {
            val updated = when (op) {
                "set" -> value.setBit(Int.MAX_VALUE)
                "clear" -> value.clearBit(Int.MAX_VALUE)
                else -> value.flipBit(Int.MAX_VALUE)
            }
            check(unchanged && updated == value && updated !== value && updated !== BigInteger.ZERO)
            "unchanged"
        } catch (e: ArithmeticException) { "${e.javaClass.simpleName}:${e.message}" }
        check(result == if (unchanged) "unchanged" else "ArithmeticException:BigInteger would overflow supported range") {
            "$text/$op/Int.MAX_VALUE: $result"
        }
    }
    println("Verified 6 maximum-bit-address outcomes")
}
class BitProbe(text: String, val trace: StringBuilder, val mode: String) : BigInteger(text) {
    override fun toByte(): Byte = super.toInt().toByte()
    override fun toShort(): Short = super.toInt().toShort()
    private fun call(name: String) { trace.append("$name;"); if (mode == "throw") throw IllegalStateException("$name callback") }
    override fun bitLength(): Int { call("bits"); return if (mode.startsWith("bits-")) mode.substringAfter('-').toInt() else super.bitLength() }
    override fun bitCount(): Int { call("count"); return super.bitCount() }
    override fun getLowestSetBit(): Int { call("lowest"); return if (mode.startsWith("lowest=")) mode.substringAfter('=').toInt() else super.getLowestSetBit() }
    override fun signum(): Int { call("sign"); return super.signum() }
    override fun toByteArray(): ByteArray { call("bytes"); return super.toByteArray() }
    override fun testBit(n: Int): Boolean { call("test"); return super.testBit(n) }
    override fun not(): BigInteger { call("not"); return super.not() }
    override fun and(value: BigInteger): BigInteger { call("and"); return super.and(value) }
    override fun or(value: BigInteger): BigInteger { call("or"); return super.or(value) }
    override fun xor(value: BigInteger): BigInteger { call("xor"); return super.xor(value) }
    override fun andNot(value: BigInteger): BigInteger { call("andNot"); return super.andNot(value) }
    override fun setBit(n: Int): BigInteger { call("set"); return super.setBit(n) }
    override fun clearBit(n: Int): BigInteger { call("clear"); return super.clearBit(n) }
    override fun flipBit(n: Int): BigInteger { call("flip"); return super.flipBit(n) }
    fun inherited(op: String, other: BigInteger, bit: Int): Any = when (op) {
        "and" -> super.and(other); "or" -> super.or(other); "xor" -> super.xor(other); "andNot" -> super.andNot(other)
        "not" -> super.not(); "set" -> super.setBit(bit); "clear" -> super.clearBit(bit); "flip" -> super.flipBit(bit)
        "test" -> super.testBit(bit); "count" -> super.bitCount(); "lowest" -> super.getLowestSetBit(); else -> super.bitLength()
    }
}
fun bitOutcome(row: Array<String>): String {
    val leftTrace = StringBuilder(); val rightTrace = StringBuilder()
    val left = BitProbe(row[0], leftTrace, row[4])
    val right = if (row[6] == "self") left else BitProbe(row[1], rightTrace, row[5])
    return try {
        val result = left.inherited(row[2], right, row[3].toInt())
        val calls = "$leftTrace/$rightTrace"
        val value = if (result is BigInteger) "$result:${result === left}:${result === right}:${result === BigInteger.ZERO}:${result === BigInteger.ONE}:${result::class == BigInteger::class}" else result.toString()
        "$value|$calls"
    } catch (e: Exception) { "!${e::class.simpleName}:${e.message}|$leftTrace/$rightTrace" }
}
val rows = mutableListOf<Array<String>>()
fun record(a: String, b: String, op: String, bit: Int, am: String, bm: String = "delegate", alias: String = "distinct", family: String = "dispatch") {
    val row = arrayOf(a, b, op, bit.toString(), am, bm, alias, family)
    rows += row + bitOutcome(row)
}
val texts = listOf("0", "1", "-1", "2", "-2", "42", "-42", "2147483648", "-2147483648", "4294967295", "-4294967296", "18446744073709551617", "-18446744073709551617")
val modes = listOf("delegate", "throw", "bits-0", "bits-31", "bits-32", "bits-63", "bits-64")
for (a in texts) for (b in texts) for (op in listOf("and", "or", "xor", "andNot")) {
    for (mode in modes) record(a,b,op,0,mode)
    for (mode in modes.drop(1)) record(a,b,op,0,"delegate",mode)
}
for (a in texts) for (op in listOf("and", "or", "xor", "andNot")) for (mode in modes) record(a,a,op,0,mode,alias="self")
for (a in texts) for (mode in modes) {
    record(a,"0","not",0,mode)
    for (op in listOf("set","clear","flip")) for (bit in listOf(-1,0,1,29,30,31,32,59,60,63,64,65,95,96,127,128)) record(a,"0",op,bit,mode)
    for (bit in listOf(Int.MIN_VALUE,-1,0,1,29,30,31,32,63,64,127,128,Int.MAX_VALUE)) record(a,"0","test",bit,mode)
    for (op in listOf("count","lowest","bits")) record(a,"0",op,0,mode)
}
for (bits in listOf(29,30,31,32,59,60,63,64,95,96,127,128,255,256,511,512,1023,1024,2048,4096)) {
    val p=BigInteger.ONE.shiftLeft(bits)
    for (delta in -1L..1L) for (sign in listOf(1,-1)) {
        val a=p.add(BigInteger.valueOf(delta)).multiply(BigInteger.valueOf(sign.toLong())).toString()
        for (op in listOf("set","clear","flip","test")) for (bit in listOf(bits-1,bits,bits+1)) record(a,"0",op,bit,"delegate",family="wide")
        for (op in listOf("and","or","xor","andNot")) record(a,p.negate().toString(),op,0,"delegate",family="wide")
        record(a,"0","not",0,"delegate",family="wide")
    }
}
for (a in texts) for (lowest in listOf(Int.MIN_VALUE,-1,0,1,31,32,Int.MAX_VALUE)) record(a,"0","count",0,"lowest=$lowest")
val source=buildString {
    appendLine("// SPDX-License-Identifier: Apache-2.0")
    appendLine("// Generated by scripts/generate-integer-bits-oracle.main.kts; JDK 25.0.4+9.")
    appendLine("package io.heapy.krogu.math")
    val chunks=rows.chunked(30)
    appendLine("internal fun jdk25IntegerBitCases(): List<Array<String>> = buildList {")
    for (i in chunks.indices) appendLine("    addAll(integerBitsBatch$i())")
    appendLine("}")
    for ((i,chunk) in chunks.withIndex()) {
        appendLine("private fun integerBitsBatch$i(): List<Array<String>> = listOf(")
        for(row in chunk) appendLine("    arrayOf(${row.joinToString(", ") { "\"$it\"" }}),")
        appendLine(")")
    }
}
val target=File("test/io/heapy/krogu/math/Jdk25IntegerBitResults.kt")
if(args.isNotEmpty()) check(target.exists() && target.readText()==source) { "Integer bit oracle differs" } else target.writeText(source)
println("${if(args.isNotEmpty()) "Verified" else "Generated"} ${rows.size} integer bit cases")

#!/usr/bin/env kotlin
// Bounded audit of a newly built OWN jar. Usage: -classpath <jar> <script> <jar> <accepted-source-sha> <accepted-proof-sha>
// No reference sqrt, third-party numerical implementation, or huge object probe.
import java.io.File
import java.security.MessageDigest
import io.heapy.krogu.math.BigDecimal as D
import io.heapy.krogu.math.BigInteger as I
import io.heapy.krogu.math.MathContext as C
import io.heapy.krogu.math.RoundingMode as R

require(args.size == 3)
val jar = File(args[0]).canonicalFile
fun sha(file: File) = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
check(sha(File("src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt")) == args[1])
check(sha(File("docs/decimal-exact-root-563-parity-proof.md")) == args[2])
println("java=${System.getProperty("java.vendor")} ${System.getProperty("java.runtime.version")}; maxHeap=${Runtime.getRuntime().maxMemory()}")
check(Runtime.getRuntime().maxMemory() <= 256L * 1024 * 1024)
println("source-sha256=${args[1]}; accepted-code-proof-sha256=${args[2]}; jar=$jar; jar-sha256=${sha(jar)}")
for (type in listOf(D::class.java, I::class.java, C::class.java, R::class.java)) {
    val location = File(type.protectionDomain.codeSource.location.toURI()).canonicalFile
    check(location == jar) { "Wrong class origin for ${type.name}: $location" }
    println("class=${type.name}; codeSource=$location; resource=${type.getResource(type.simpleName + ".class")}")
}
data class Case(val c: String, val s: Int, val root: String, val rootScale: Int)
// Minimal roots are independent literals, not output of a sqrt call.
val cases = listOf(Case("4", 0, "2", 0), Case("9", 0, "3", 0), Case("144", 0, "12", 0),
    Case("40000", 0, "2", -2), Case("400", 2, "2", 0), Case("250", -1, "5", -1),
    Case("250", -3, "5", -2), Case("4000", 1, "2", -1),
    Case("100", Int.MIN_VALUE, "1", -1073741825), Case("1000", Int.MIN_VALUE + 1, "1", -1073741825),
    Case("40", Int.MIN_VALUE + 1, "2", -1073741824), Case("1", Int.MIN_VALUE, "1", -1073741824),
    Case("250", Int.MAX_VALUE, "5", 1073741823), Case("4000", Int.MAX_VALUE, "2", 1073741822),
    Case("225", 0, "15", 0), Case("625", 0, "25", 0), Case("998001", 0, "999", 0),
    Case("15241578750190521", 0, "123456789", 0))
fun expected(case: Case, p: Int, mode: R): Pair<String, Int>? {
    val root = case.root
    if (p == 0 || p >= root.length) {
        val pad = minOf((case.s / 2).toLong() - case.rootScale, if (p == 0) Long.MAX_VALUE else (p - root.length).toLong())
        check(pad in 0..16)
        return (root + "0".repeat(pad.toInt())) to (case.rootScale.toLong() + pad).toInt()
    }
    if (mode == R.UNNECESSARY) return null
    val tail = root.substring(p)
    val half = if (tail[0] < '5') -1 else if (tail[0] > '5' || tail.drop(1).any { it != '0' }) 1 else 0
    val increment = when (mode) {
        R.UP, R.CEILING -> true
        R.DOWN, R.FLOOR -> false
        R.HALF_UP -> half >= 0
        R.HALF_DOWN -> half > 0
        R.HALF_EVEN -> half > 0 || half == 0 && (root[p - 1] - '0') % 2 == 1
        R.UNNECESSARY -> error("already classified")
    }
    var coefficient = root.take(p)
    if (increment) {
        val digits = coefficient.toCharArray()
        var index = digits.lastIndex
        while (index >= 0 && digits[index] == '9') { digits[index] = '0'; index-- }
        if (index >= 0) { digits[index]++; coefficient = digits.concatToString() }
        else coefficient = "1" + digits.concatToString()
    }
    var scale = case.rootScale.toLong() - (root.length - p)
    if (coefficient.length > p) { coefficient = coefficient.dropLast(1); scale-- }
    check(scale in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
    return coefficient to scale.toInt()
}
class Truthful(c: I, s: Int) : D(c, s) {
    override fun signum() = super.signum()
    override fun precision() = super.precision()
    override fun scale() = super.scale()
    override fun unscaledValue() = super.unscaledValue()
    override fun stripTrailingZeros() = super.stripTrailingZeros()
    override fun sqrt(mc: C?) = super.sqrt(mc)
    fun callerSuper(mc: C?) = super.sqrt(mc)
}
var checks = 0
for (case in cases) for (p in listOf(0, 1, 2, 3, 7, 17, Int.MAX_VALUE - 1, Int.MAX_VALUE)) for (mode in R.entries) {
    val e = expected(case, p, mode)
    val context = C(p, mode)
    val ordinary = D(I(case.c), case.s)
    val truthful = Truthful(I(case.c), case.s)
    for (action in listOf<() -> D>({ ordinary.sqrt(context) }, { truthful.sqrt(context) }, { truthful.callerSuper(context) })) {
        if (e == null) {
            var rejected = false
            try { action() } catch (_: ArithmeticException) { rejected = true }
            check(rejected) { "Expected UNNECESSARY rejection: $case $context" }
        } else {
            val result = action()
            check(result.unscaledValue().toString() == e.first && result.scale() == e.second) {
                "$case $context expected=$e actual=${result.unscaledValue()}@${result.scale()}"
            }
            check(result.precision() == e.first.length)
        }
        checks++
    }
}
for (value in listOf("2", "-4")) {
    var rejected = false
    try { D(value).sqrt(C(0)) } catch (_: ArithmeticException) { rejected = true }
    check(rejected)
    checks++
}
println("PASS $checks packaged-JAR checks: all eight modes, max precision, negative odd/extreme scales, midpoint parity/carry, UNNECESSARY zero-tail behavior, truthful/caller-super and genuine errors")

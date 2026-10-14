#!/usr/bin/env kotlinr
// SPDX-License-Identifier: Apache-2.0
/**
 * Bounded Long rational model versus public pinned JDK and port APIs. No numerical source is used.
 * kotlin -classpath build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar scripts/audit-decimal-division-normalization.main.kts [--check-port]
 * Use a fresh KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR for each JAR snapshot.
 */
import java.math.BigDecimal as JD
import java.math.MathContext as JM
import java.math.RoundingMode as JR
import java.math.BigInteger as JI
import io.heapy.krogu.math.BigDecimal as KD
import io.heapy.krogu.math.BigInteger as KI
import io.heapy.krogu.math.MathContext as KM
import io.heapy.krogu.math.RoundingMode as KR

check(Runtime.version().toString() in listOf("25.0.4+9", "25.0.4+9-LTS"))
check(args.all { it == "--check-port" })
class Delegating(c: Long, s: Int) : KD(KI.valueOf(c), s) {
    override fun signum() = super.signum()
    override fun precision() = super.precision()
    override fun scale() = super.scale()
    override fun unscaledValue() = super.unscaledValue()
    override fun divide(d: KD?) = super.divide(d)
    override fun divide(d: KD?, mc: KM?) = super.divide(d, mc)
}
fun gcd(a: Long, b: Long): Long { var x = a; var y = b; while (y != 0L) { val r = x % y; x = y; y = r }; return x }
fun power(n: Int): Long { require(n in 0..15); var p = 1L; repeat(n) { p = Math.multiplyExact(p, 10) }; return p }
fun model(a: Long, sa: Int, b: Long, sb: Int, p: Int, mode: JR): String {
    val preferred = sa.toLong() - sb
    require(preferred in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong())
    if (b == 0L) return "!ArithmeticException"
    if (a == 0L) return "0@$preferred"
    val sign = if ((a < 0) != (b < 0)) -1 else 1
    var n = kotlin.math.abs(a); var d = kotlin.math.abs(b)
    val g = gcd(n, d)
    var exact = n / g; var reduced = d / g; var places = 0
    while (reduced != 1L) {
        exact = Math.multiplyExact(exact, 10)
        val factor = gcd(exact, reduced)
        if (factor == 1L) break
        exact /= factor; reduced /= factor; places++
    }
    fun pair(c: Long, s: Long): String = if (s !in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) "!ArithmeticException" else "${c * sign}@$s"
    if (reduced == 1L && (p == 0 || exact.toString().length <= p)) return pair(exact, preferred + places)
    if (p == 0) return "!ArithmeticException"
    require(p in 1..7)
    var exponent = 0
    while (n < d) { n = Math.multiplyExact(n, 10); exponent-- }
    while (n / 10 >= d) { d = Math.multiplyExact(d, 10); exponent++ }
    n = Math.multiplyExact(n, power(p - 1))
    var whole = n / d; val remainder = n % d
    if (remainder != 0L && mode == JR.UNNECESSARY) return "!ArithmeticException"
    val half = Math.multiplyExact(remainder, 2).compareTo(d)
    val up = remainder != 0L && when (mode) {
        JR.UP -> true
        JR.DOWN, JR.UNNECESSARY -> false
        JR.CEILING -> sign > 0
        JR.FLOOR -> sign < 0
        JR.HALF_UP -> half >= 0
        JR.HALF_DOWN -> half > 0
        JR.HALF_EVEN -> half > 0 || half == 0 && whole % 2L != 0L
    }
    if (up) whole++
    var scale = preferred + p - 1L - exponent
    if (whole.toString().length > p) { whole /= 10; scale-- }
    if (remainder == 0L) while (scale > preferred && whole % 10 == 0L) { whole /= 10; scale-- }
    return pair(whole, scale)
}
data class Observation(val pair: String, val diagnostic: String)
fun jdk(a: Long, sa: Int, b: Long, sb: Int, p: Int?, mode: JR): Observation = try {
    val x = JD.valueOf(a, sa); val y = JD.valueOf(b, sb)
    val q = if (p == null) x.divide(y) else x.divide(y, JM(p, mode))
    Observation("${q.unscaledValue()}@${q.scale()}", q.toString())
} catch (e: ArithmeticException) { Observation("!ArithmeticException", "${e::class.simpleName}:${e.message}") }
var checks = 0; var referenceDifferences = 0; var portDifferences = 0; var controls = 0
fun verify(a: Long, sa: Int, b: Long, sb: Int, p: Int?, mode: JR, mask: Int) {
    val expected = model(a, sa, b, sb, p ?: 0, mode)
    val reference = jdk(a, sa, b, sb, p, mode)
    val x = if (mask and 1 == 0) KD.valueOf(a, sa) else Delegating(a, sa)
    val y = if (mask and 2 == 0) KD.valueOf(b, sb) else Delegating(b, sb)
    val port = try {
        val q = if (p == null) x.divide(y) else x.divide(y, KM(p, KR.valueOf(mode.name)))
        Observation("${q.unscaledValue()}@${q.scale()}", q.toString())
    } catch (e: ArithmeticException) { Observation("!ArithmeticException", "${e::class.simpleName}:${e.message}") }
    checks++
    if (expected.startsWith('!')) controls++
    val carryWitness = sa == Int.MAX_VALUE && p == 1 && mask == 0 && b > 0 &&
        (kotlin.math.abs(a) == 19L && b == 20L || kotlin.math.abs(a) == 1L && b == 10000001L)
    if (carryWitness) println("CARRY $a@$sa / $b@$sb $mode expected=$expected JDK=$reference port=$port")
    if (reference.pair != expected) {
        referenceDifferences++
        if (referenceDifferences <= 48) println("$a@$sa / $b@$sb p=$p $mode mask=$mask expected=$expected JDK=$reference port=$port")
    }
    if (port.pair != expected) { portDifferences++; if (portDifferences <= 12) println("PORT MISMATCH expected=$expected port=$port input=$a@$sa/$b@$sb p=$p $mode mask=$mask") }
}
for ((a, b, sb) in listOf(Triple(1L, 1L, 0), Triple(2L, 2L, 0), Triple(40L, 16L, 0),
    Triple(6L, 15L, 0), Triple(3L, 24L, 0), Triple(1L, 100L, 2), Triple(19L, 20L, 0),
    Triple(1L, 10000001L, 7), Triple(1999L, 2000L, 0), Triple(1L, 2L, 0), Triple(1L, 3L, 0))) {
    for (distance in listOf(0, 1, 5)) for (ls in listOf(-1, 1)) for (rs in listOf(-1, 1)) for (mask in 0..3) {
        verify(a * ls, Int.MAX_VALUE - distance, b * rs, sb, null, JR.UNNECESSARY, mask)
        for (p in listOf(0, 1, 2, 7)) for (mode in JR.entries)
            verify(a * ls, Int.MAX_VALUE - distance, b * rs, sb, p, mode, mask)
    }
}
for (scale in listOf(0, Int.MAX_VALUE - 5, Int.MAX_VALUE)) for (mask in 0..3) for (mode in JR.entries) {
    verify(1, scale, 1, 0, Int.MAX_VALUE, mode, mask)
    verify(6, scale, 15, 0, Int.MAX_VALUE, mode, mask)
    verify(1, scale, 0, 0, 7, mode, mask)
}
// A bounded counterpart of the static supported-range witness: the exact
// coefficient would be 65*5^800 at MAX+561, but integer rounding carries to MAX.
val jd = JI.ONE.shiftLeft(800)
val jn = JI.valueOf(65).multiply(JI.TEN.pow(240))
val rational = jn.divideAndRemainder(jd)
check(rational[0] == JI.valueOf(9) && rational[1].shiftLeft(1) > jd)
val kd = KI.TWO.pow(800)
for (ls in listOf(-1, 1)) for (rs in listOf(-1, 1)) for (mode in JR.entries) {
    val sign = ls * rs
    val up = when (mode) {
        JR.UP, JR.HALF_UP, JR.HALF_DOWN, JR.HALF_EVEN -> true
        JR.CEILING -> sign > 0
        JR.FLOOR -> sign < 0
        JR.DOWN, JR.UNNECESSARY -> false
    }
    val expected = if (up) "$sign@${Int.MAX_VALUE}" else "!ArithmeticException"
    val reference = try {
        val q = JD(JI.valueOf(65L * ls), Int.MAX_VALUE).divide(JD(if (rs < 0) jd.negate() else jd, 239), JM(1, mode))
        Observation("${q.unscaledValue()}@${q.scale()}", q.toString())
    } catch (e: ArithmeticException) { Observation("!ArithmeticException", "${e::class.simpleName}:${e.message}") }
    val port = try {
        val q = KD.valueOf(65L * ls, Int.MAX_VALUE).divide(KD(if (rs < 0) kd.negate() else kd, 239), KM(1, KR.valueOf(mode.name)))
        Observation("${q.unscaledValue()}@${q.scale()}", q.toString())
    } catch (e: ArithmeticException) { Observation("!ArithmeticException", "${e::class.simpleName}:${e.message}") }
    checks++
    if (!up) controls++
    check(reference.pair == "!ArithmeticException") { "Pinned bounded reference changed: $reference" }
    if (reference.pair != expected) referenceDifferences++
    if (port.pair != expected) portDifferences++
    println("PREFLIGHT 65*$ls@MAX / ($rs*2^800@239) $mode expected=$expected JDK=$reference port=$port")
}
println("runtime=${Runtime.version()} checks=$checks rejectionControls=$controls retainedReferenceDifferences=$referenceDifferences portDifferences=$portDifferences")
check(referenceDifferences == 6436) { "Pinned reference diagnostics changed: $referenceDifferences" }
if (args.contains("--check-port")) check(portDifferences == 0)

// SPDX-License-Identifier: Apache-2.0
import java.math.BigInteger

// Bounded certificate for log2(10), using only exact JDK BigInteger arithmetic.
// No 10^646456992 (or other giant input) is constructed.
data class Q(val n: BigInteger, val d: BigInteger) {
    init { require(d.signum() > 0) }
    operator fun plus(o: Q): Q {
        val g = d.gcd(o.d)
        val a = d.divide(g)
        val b = o.d.divide(g)
        val nn = n.multiply(b).add(o.n.multiply(a))
        val dd = a.multiply(o.d)
        val h = nn.gcd(dd)
        return Q(nn.divide(h), dd.divide(h))
    }
    operator fun times(o: Q): Q = Q(n.multiply(o.n), d.multiply(o.d)).reduce()
    operator fun div(o: Q): Q = Q(n.multiply(o.d), d.multiply(o.n)).reduce()
    fun reduce(): Q { val g = n.gcd(d); return Q(n.divide(g), d.divide(g)) }
}

fun lnBounds(x: Int, terms: Int): Pair<Q, Q> {
    // ln(x) = 2 * sum z^(2j+1)/(2j+1), z=(x-1)/(x+1).
    val z = Q(BigInteger.valueOf((x - 1).toLong()), BigInteger.valueOf((x + 1).toLong()))
    var power = z
    var partial = Q(BigInteger.ZERO, BigInteger.ONE)
    for (j in 0 until terms) {
        val term = Q(power.n, power.d.multiply(BigInteger.valueOf((2L * j + 1L))))
        partial += Q(term.n.multiply(BigInteger.TWO), term.d)
        power *= z
        power *= z
    }
    // Tail <= 2*z^(2n+1)/(2n+1)/(1-z^2).
    val denomTerm = BigInteger.valueOf((2L * terms + 1L))
    val tail = Q(power.n.multiply(BigInteger.TWO), power.d.multiply(denomTerm)) /
        (Q(BigInteger.ONE, BigInteger.ONE) - (z * z))
    return partial to (partial + tail)
}

operator fun Q.minus(o: Q): Q = Q(n.multiply(o.d).subtract(o.n.multiply(d)), d.multiply(o.d)).reduce()

fun floorQ(q: Q): BigInteger = q.n.divide(q.d)

val terms = 256
val (ln2Lo, ln2Hi) = lnBounds(2, terms)
val (ln10Lo, ln10Hi) = lnBounds(10, terms)
val ratioLo = ln10Lo / ln2Hi
val ratioHi = ln10Hi / ln2Lo

val k = 646456992L
val k1 = k + 1L
val kLo = ratioLo.n.multiply(BigInteger.valueOf(k)).divide(ratioLo.d)
val kHi = ratioHi.n.multiply(BigInteger.valueOf(k)).divide(ratioHi.d)
val k1Lo = ratioLo.n.multiply(BigInteger.valueOf(k1)).divide(ratioLo.d)
val k1Hi = ratioHi.n.multiply(BigInteger.valueOf(k1)).divide(ratioHi.d)

check(kLo == kHi) { "K floor disagreement: $kLo .. $kHi" }
check(k1Lo == k1Hi) { "K+1 floor disagreement: $k1Lo .. $k1Hi" }
check(kLo == BigInteger("2147483643"))
check(k1Lo == BigInteger("2147483647"))

// bitLength(10^e) = floor(e*log2(10)) + 1.
check(kLo.add(BigInteger.ONE) == BigInteger("2147483644"))
check(k1Lo.add(BigInteger.ONE) == BigInteger("2147483648"))
check(kLo.add(BigInteger.ONE) <= BigInteger.valueOf(Int.MAX_VALUE.toLong()))
check(k1Lo.add(BigInteger.ONE) > BigInteger.valueOf(Int.MAX_VALUE.toLong()))
check(k1 <= Int.MAX_VALUE.toLong())

// Exact decimal identity, represented symbolically: 10^K / 10^(K+1) = 1/10.
check(k1 == k + 1L)
println("terms=$terms")
println("log2(10) floor bounds for K=$k: $kLo .. $kHi")
println("log2(10) floor bounds for K+1=$k1: $k1Lo .. $k1Hi")
println("bitLength(10^K)=${kLo.add(BigInteger.ONE)}")
println("bitLength(10^(K+1))=${k1Lo.add(BigInteger.ONE)}")
println("scale=$k1 within Int; (10^K)*10^(-K-1)=1/10 exactly")

// Independently bound the whole midpoint domain, without invoking krogu or a
// decimal-to-primitive parser. The largest significand bounds every numerator
// in its exponent band. All exact integers below have fewer than 5000 bits;
// rational logarithm bounds above are also independent of the giant witness.
data class Format(val name: String, val precision: Int, val minExponent: Int, val maxExponent: Int)
for (format in listOf(Format("Float", 24, -126, 127), Format("Double", 53, -1022, 1023))) {
    val p = format.precision
    val bound = maxOf(p - format.minExponent + (p + 3) / 3, (format.maxExponent + 3) / 3)
    val largestOddNumerator = BigInteger.ONE.shiftLeft(p + 1).subtract(BigInteger.ONE)
    check(BigInteger.ONE.shiftLeft(p + 1) < BigInteger.TEN.pow((p + 3) / 3))
    check(BigInteger.ONE.shiftLeft(format.maxExponent + 1) <
        BigInteger.TEN.pow((format.maxExponent + 3) / 3))
    var bands = 0
    var largestDecimalCoefficient = 0
    for (q in (format.minExponent - p)..(format.maxExponent - p)) {
        val midpointCoefficient = if (q < 0)
            largestOddNumerator.multiply(BigInteger.valueOf(5).pow(-q))
        else largestOddNumerator.shiftLeft(q)
        val count = midpointCoefficient.toString().length
        check(count <= bound) { "${format.name} q=$q digits=$count bound=$bound" }
        largestDecimalCoefficient = maxOf(largestDecimalCoefficient, count)
        bands++
    }
    check(bound == if (p == 24) 159 else 1093)
    // a=digits-scale-1 in [-400,400]. Keeping B digits and appending a
    // sticky unit yields scale=B-a, never the original giant scale.
    val largestPower = bound + 400
    val boundedPower = BigInteger.TEN.pow(largestPower)
    check(boundedPower.bitLength() < 5000)
    // Worst-case numerator after bounded scaling/alignment fits 8192 bits:
    // at most B+1 decimal digits, multiplied by at most 10^400 and 2^1074.
    val conservativeNumeratorBits = (bound + 401) * 4 + (p - 1 - format.minExponent)
    check(conservativeNumeratorBits < 8192)
    println("${format.name}: $bands midpoint exponent bands; decimal bound=$bound; " +
        "actual largest coefficient bound=$largestDecimalCoefficient; " +
        "largest rounding 10-power=$largestPower (${boundedPower.bitLength()} bits); " +
        "conservative aligned numerator bits=$conservativeNumeratorBits")
}
// Exact certificate for a lower fixed-point log10(2) estimate. This chooses
// a supported divisor, never an approximate final decimal digit count.
val decimalLogLo = ln2Lo / ln10Hi
val decimalLogHi = ln2Hi / ln10Lo
val fixedNumerator = BigInteger("1292913986")
val fixedDenominator = BigInteger.ONE.shiftLeft(32)
check(fixedNumerator.add(BigInteger.ONE).multiply(BigInteger.TWO) < fixedDenominator)
check(fixedNumerator.multiply(decimalLogLo.d) < decimalLogLo.n.multiply(fixedDenominator))
check(decimalLogHi.n.multiply(fixedDenominator) < fixedNumerator.add(BigInteger.ONE).multiply(decimalLogHi.d))
check(decimalLogHi.n.multiply(BigInteger.TWO) < decimalLogHi.d)
val largestBitsMinusOne = BigInteger.valueOf(Int.MAX_VALUE.toLong() - 1)
check(largestBitsMinusOne.multiply(BigInteger.TWO) < fixedDenominator)
val largestProduct = largestBitsMinusOne.multiply(fixedNumerator)
check(largestProduct < BigInteger.valueOf(Long.MAX_VALUE))
val largestLowerDigits = largestProduct.divide(fixedDenominator).add(BigInteger.ONE)
check(largestLowerDigits == BigInteger("646456993"))
val largestTrim = largestLowerDigits.subtract(BigInteger.ONE)
check(largestTrim == BigInteger("646456992"))
println("fixed-point: 1292913986/2^32 < log10(2) < 1292913987/2^32 < 1/2")
println("max (b-1)*1292913986=$largestProduct; max E=$largestLowerDigits; max trim for B>=1=$largestTrim")
println("D-E is 0 or 1: log10(n)-floor-input < (b-1)/2^32 + log10(2) < 1")
println("quotient has at most B+1 digits; discarded tail is zero iff both division and extra-digit remainders are zero")
val witnessBits = 2147483644L
val witnessE = ((witnessBits - 1) * 1292913986L ushr 32) + 1
check(k + 1 - witnessE in 0L..1L)
for (width in listOf(159L, 1093L)) {
    val trim = witnessE - width
    check(trim in 1L..k)
    val quotientDigits = k - trim + 1
    check(quotientDigits in width..(width + 1))
    val extra = quotientDigits - width
    val finalScale = k + 1 - trim - extra
    check(finalScale == width)
    // quotient is the small symbolic power 10^(quotientDigits-1), with zero
    // remainder; trimming its extra digit also discards only zero.
    check(width - finalScale - 1 == -1L)
    println("symbolic witness width=$width: E=$witnessE; divisor exponent=$trim <=K; " +
        "quotient digits=$quotientDigits; sticky=false; reduced scale=$finalScale; adjusted exponent=-1")
}
println("PASS: supported symbolic witness, bounded IEEE midpoints and numeric prefix estimate; no giant input allocated")

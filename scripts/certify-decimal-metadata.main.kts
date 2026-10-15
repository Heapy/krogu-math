// SPDX-License-Identifier: Apache-2.0
// Exact, bounded integer/rational certificate. Does not load or execute krogu.
import java.math.BigInteger as Z

check(Runtime.getRuntime().maxMemory() <= 256L * 1024 * 1024) { "Use a heap cap of at most 256 MiB" }
println("heap cap=${Runtime.getRuntime().maxMemory()}; script cache=" +
    System.getProperty("kotlin.main.kts.compiled.scripts.cache.dir"))

data class Rational(val n: Z, val d: Z) {
    init { require(d.signum() > 0) }
    fun reduced(): Rational {
        val g = n.gcd(d)
        return Rational(n.divide(g), d.divide(g))
    }
    operator fun plus(other: Rational) = Rational(n * other.d + other.n * d, d * other.d).reduced()
    operator fun times(other: Rational) = Rational(n * other.n, d * other.d).reduced()
    operator fun div(other: Rational) = Rational(n * other.d, d * other.n).reduced()
}

fun logarithmInterval(x: Long): Pair<Rational, Rational> {
    val z = Rational(Z.valueOf(x - 1), Z.valueOf(x + 1))
    val z2 = z * z
    var power = z
    var sum = Rational(Z.ZERO, Z.ONE)
    for (j in 0 until 256) {
        sum += Rational(power.n * Z.TWO, power.d * Z.valueOf(2L * j + 1))
        power *= z2
    }
    val oneMinusZ2 = Rational(z2.d - z2.n, z2.d)
    val tail = Rational(power.n * Z.TWO, power.d * Z.valueOf(513)) / oneMinusZ2
    return sum to (sum + tail)
}

val (twoLo, twoHi) = logarithmInterval(2)
val (tenLo, tenHi) = logarithmInterval(10)
val log10TwoLo = twoLo / tenHi
val log10TwoHi = twoHi / tenLo
val log2TenLo = tenLo / twoHi
val log2TenHi = tenHi / twoLo
val c = Z.valueOf(1292913986)
val denominator = Z.ONE.shiftLeft(32)
check(c * log10TwoLo.d < denominator * log10TwoLo.n)
check(denominator * log10TwoHi.n < (c + Z.ONE) * log10TwoHi.d)
check(Z.TWO * log10TwoHi.n < log10TwoHi.d)
val maxBits = Z.valueOf(Int.MAX_VALUE.toLong())
check((maxBits - Z.ONE) * Z.TWO < denominator)
val maxProduct = (maxBits - Z.ONE) * c
check(maxProduct < Z.ONE.shiftLeft(62))
val maxE = maxProduct.divide(denominator) + Z.ONE
check(maxE == Z.valueOf(646456993))

fun certifiedPowerBits(exponent: Z): Z {
    val lowerFloor = (exponent * log2TenLo.n).divide(log2TenLo.d)
    val upperFloor = (exponent * log2TenHi.n).divide(log2TenHi.d)
    check(lowerFloor == upperFloor)
    return lowerFloor + Z.ONE
}
check(certifiedPowerBits(maxE - Z.ONE) == Z.valueOf(2147483644))
check(certifiedPowerBits(maxE) == maxBits + Z.ONE)
// Monotonicity covers every threshold E <= maxE-1. At E=maxE,
// n < 2^Int.MAX_VALUE <= 10^E for every supported magnitude n.
println("exact rational log bounds certify E<=D<=E+1 for b<=Int.MAX_VALUE")
println("max E=$maxE; max fixed-point product=$maxProduct <2^62")
println("10^(max E-1): 2147483644 bits; 10^(max E): 2147483648 bits")

// Symbolic witnesses use exponents/counts only; never form either giant power.
val giantDigits = maxE.toLong()
check(giantDigits > 536870888L)
val equalDigitDistance = giantDigits - 1
check(equalDigitDistance == 646456992L)
check(certifiedPowerBits(Z.valueOf(equalDigitDistance)) <= maxBits)
// Long scale algebra: Da-sa > Db-sb iff sb+(Da-Db)>sa.
// The only intermediate sum is guarded for mathematical overflow. Verify
// endpoints against exact unbounded integer expressions, not JVM wrapping.
val digits = listOf(1L, 2L, 128L, giantDigits)
val scales = listOf(Long.MIN_VALUE, Long.MIN_VALUE + giantDigits,
    -4294967296L, 0L, 4294967294L, Long.MAX_VALUE - giantDigits, Long.MAX_VALUE)
var comparisons = 0
for (da in digits) for (db in digits) for (sa in scales) for (sb in scales) {
    val delta = da - db
    val result = when {
        delta > 0 && sb > Long.MAX_VALUE - delta -> 1
        delta < 0 && sb < Long.MIN_VALUE - delta -> -1
        else -> (sb + delta).compareTo(sa)
    }
    val exact = (Z.valueOf(da) - Z.valueOf(sa)).compareTo(Z.valueOf(db) - Z.valueOf(sb))
    check(result == exact)
    comparisons++
}
check(comparisons == 784)
println("$comparisons exact symbolic digit/Long-scale endpoint certificates")
println("equal-decade divisor exponent <=Dlong-1<=646456992, divisor<=longer coefficient")
println("PASS: no port execution, formatter, decimal parser, or giant allocation")

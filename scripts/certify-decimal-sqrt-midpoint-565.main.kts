#!/usr/bin/env kotlin
// SPDX-License-Identifier: Apache-2.0
// PREPARED / UNCOMPILED / UNRUN. Future execution requires a parent grant.
// Independent primitive-integer and symbolic range certificate; no JDK/port
// numerical APIs, giant inputs, child processes, or writes. Run from repo root.
import java.io.File

check(args.isEmpty())
val source = File("src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt").readText()
val branch = source.substringAfter("        val qr = numerator.divideAndRemainder(denominator)\n")
    .substringBefore("        return BigDecimal(root, scale).round(context).stripTo(preferred)")
check(branch == """
        var root = qr[0].sqrt()
        val delta = qr[0].subtract(root.multiply(root))
        val exact = delta.signum() == 0 && qr[1].signum() == 0
        if (!exact) {
            // n = q*d+r, a = floor(sqrt(q)), delta = q-a*a.
            // The midpoint sign is sign((4*(delta-a)-1)*d+4*r).
            // Unequal delta/a decides it; equality needs only r versus d/4.
            val rootComparison = delta.compareTo(root)
            val half = if (rootComparison != 0) rootComparison else {
                val quarter = denominator.divideAndRemainder(BigInteger.valueOf(4))
                val remainderComparison = qr[1].compareTo(quarter[0])
                if (remainderComparison != 0) remainderComparison else -quarter[1].signum()
            }
            if (increment(context.roundingMode, 1, half, root.testBit(0))) root = root.add(BigInteger.ONE)
        }
""".trimStart('\n')) { "Production midpoint block differs from the reviewed certificate" }

fun sign(value: Long): Int = value.compareTo(0L)
fun candidate(q: Long, r: Long, d: Long, a: Long): Int {
    val comparison = (q - a * a).compareTo(a)
    if (comparison != 0) return comparison
    val quarterComparison = r.compareTo(d / 4)
    return if (quarterComparison != 0) quarterComparison else -sign(d % 4)
}

var checks = 0L
var ties = 0L
var zeroRoots = 0L
var smallDivisors = 0L
var exactRoots = 0L
for (q in 0L..128L) {
    // Independent bounded floor-root construction for the reference domain.
    var a = 0L
    while ((a + 1) * (a + 1) <= q) a++
    for (d in 1L..64L) for (r in 0L until d) {
        val n = q * d + r
        val square = a * a
        val delta = q - square
        val exact = delta == 0L && r == 0L
        // Reference forms the entire midpoint square only in this small domain.
        val reference = sign(4 * n - (2 * a + 1) * (2 * a + 1) * d)
        check(candidate(q, r, d, a) == reference) { "q=$q r=$r d=$d a=$a" }
        check(exact == (square * d == n))
        check(square <= q && delta in 0L..q && a * a <= q && q < (a + 1) * (a + 1))
        check(d / 4 <= d && d % 4 in 0L..3L && r < d)
        check(a + 1 <= maxOf(q, 2L))
        if (reference == 0) { check(delta == a && d % 4 == 0L && r == d / 4); ties++ }
        if (a == 0L) zeroRoots++
        if (d < 4L) smallDivisors++
        if (exact) exactRoots++
        checks++
    }
}
check(checks == 129L * (64L * 65L / 2L))
check(ties > 0 && zeroRoots > 0 && smallDivisors > 0 && exactRoots > 0)

// Universal inequalities in the proof do not depend on a finite enumeration.
// Verify endpoint metadata without constructing any number with this many bits.
val limit = Int.MAX_VALUE.toLong()
val maxRootBits = (limit + 1) / 2
check(2 * maxRootBits - 1 == limit) // multiplication's lower-bound guard fits
check(maxRootBits + 1 <= limit) // addition can carry one root bit, still fits
check((limit + 29) / 30 + 1 < limit) // scratch limb count remains an Int

// Witness uses the own pre-existing log2(10) certificate for this premise:
// bitLength(10^K)=2147483644. We deliberately do not construct 10^K.
val k = 646456992L
val nBits = 2147483644L
check(k % 2 == 0L && k <= limit)
check(nBits + 2 == 2147483646L && nBits + 2 <= limit)
check(nBits + 4 == limit + 1) // old (4*N+1)<<2 exceeds supported bits
// Symbolically c=4*M^2+1, M=10^(K/2), M>=1:
// c-(2*M)^2=1>0; (2*M+1)^2-c=4*M>0. No finite root.
check(k / 2 > 0)
// n=4*N+1, d=N, q=4,r=1,a=2,delta=0. The comparison is
// negative by delta<a for every N>1; exactness is false despite q=a*a.
check(candidate(4, 1, 10, 2) == -1)
check(4L - 2L * 2L == 0L && 1 != 0)

println("PASS: $checks bounded independent midpoint/exactness cases; ties=$ties, a=0=$zeroRoots, d<4=$smallDivisors, exact=$exactRoots")
println("PASS: symbolic Int.MAX_VALUE bit-domain and K=$k witness; no giant input, numerical API, process, or write")

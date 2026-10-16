# 591 primitive decimal low bits and 549 comparison result

SPDX-License-Identifier: Apache-2.0

SOURCE ONLY: the new code and three common test methods are UNCOMPILED / UNRUN.
This is an isolated successor to frozen source248 manifest
`d9074c29e8b4680663eb6ace92b83a38fafb3bcf15604be2f81a71e9058af17a`.
All original248 sources were archived **before** editing in
`../evidence/original-source248.tar`, SHA256
`6166715224e4d931c333550b54c1fe5add0aee6dc0e8be3864db0e7b231fa6cb`.
The original manifest and original BigDecimal/BigInteger copies are read-only.
Main/source247, the root248 runtime tree, original590 and original549 ledgers
are untouched. This paper uses own source, the public Java25 API and independent
arithmetic only, with no foreign numerical/stdlib/compiler implementation read.

## Requirement and exact source gap

The public Java25 [intValue](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#intValue())
and [longValue](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#longValue())
rules discard the fractional part and retain the low32/64 bits, respectively.
They do not require publishing an exact intermediate BigInteger. Write a valid
decimal as x=c*10^-s, s any Int, and let L=Int.MAX_VALUE. For supported
|c|=2^L-1 and s=-1, the required primitive values are -10; for the opposite
coefficient sign they are +10. Since L>=64, 2^L vanishes modulo both2^32 and
2^64. The old coefficient.multiply(TEN) correctly rejects the full product
at its bit precheck L+4-1>L, preventing primitive narrowing. The witness is
**symbolic and unexecuted**; no giant input/JDK behavior was observed here.
The same full product remains a legitimate unsupported final result for
toBigInteger, whose body is not changed.

## Complete Int-scale partition

`BigDecimal.integerLowBits(bits)` is called only for bits32 or64.

1. s>=0 keeps the entire original `toBigInteger().toLong()` route. It truncates
   fractional digits toward zero before narrowing; its virtual calls, exceptions
   and existing proof premises are unchanged. This patch supplies no new claim
   about all resource/subtype behavior in that retained route.
2. s<0 is an integer value. Define k=-s.toLong(), so 1<=k<=2147483648 without
   Int.MIN negation overflow. If k>=bits, 10^k=2^k*5^k is divisible by2^bits.
   The unchanged shortcut returns zero for every c and either sign, including
   zero, s=MIN, and k equal to32/64. No huge loop/power is attempted.
3. Otherwise 1<=k<bits<=64. Narrowing k to Int is safe. The new nonvirtual
   stored predicate `decimalLowBitsProductFits591(k)` reads canonical magnitude
   bit length b and accepts iff b+4k<=L, computed in Long. For k>0,
   |c|<2^b and10^k<2^(4k), hence |c|10^k<2^(b+4k)<=2^L. Zero is also safe.
   The power itself has at most252 bits and is supported. Thus every ordinary
   mathematical intermediate of the old product path is supported; the
   existing multiplication lower-bound precheck cannot reject this supported
   product. The **entire old expression**, including virtual multiply and the
   resulting object's virtual toLong, is retained in this proved-safe class.
   Environmental allocation and truthful reached-leaf premises remain explicit.
4. If this sufficient predicate declines, the bounded stored kernel computes
   the primitive bits directly. Declining does not assert the full product is
   necessarily unsupported: uncertain boundary cases are also computed exactly
   in the ring. There is no new magnitude/domain cap and no rejection solely
   from this sufficient estimate.

For the original giant witness b=L,k=1, the old-route predicate declines and
the actual new outer branch is selected by source reasoning. Bounded public
tests do not pretend to execute this giant branch.

## Stored ring kernel

`BigInteger.decimalProductLowBits591(raise,bits)` has internal preconditions
bits in{32,64}, 0<=raise<bits, checked by require. Public eligibility above
satisfies them; no new public invalid-input category is introduced.

Canonical magnitude words are little-endian base B=2^30. Words with index>=3
contribute multiples of2^90 and therefore vanish modulo2^64. Folding the first
at most three words with shift30/OR forms exactly |c| modulo2^64; each limb
occupies disjoint positions before truncation. Kotlin Long shifts/operations
retain low64 bits. Canonical negative sign negates this residue modulo2^64;
Long.MIN negation is deliberately a wrapping ring operation here, not an
unbounded absolute-value computation. Empty zero words produce zero.

Each of at most63 multiplications by10 is a Long wrapping multiplication.
Inductively its state is c*10^j modulo2^64 after j steps. For width64 the final
Long is the signed two's-complement interpretation. For width32, final
toInt().toLong() selects and sign-extends the low32 bits. Reduction modulo2^32
commutes with reduction modulo2^64, so computing the64-bit ring first is exact
for both widths. BigDecimal.toInt performs its original final toInt; inherited
toByte/toShort still call virtual toInt then narrow to8/16 bits. No new
toLong/Number/subtype callback is used by the stored kernel.

This argument assumes the ordinary Kotlin primitive wrapping/bit semantics
already used throughout this implementation. It does not discharge task557's
erased/generic JavaScript Number-dispatch defect or prove a compiler correct.
The kernel does not use floating arithmetic, unsigned-library BigInteger,
host mathematics, a new field/cache, or a magnitude-sized temporary.

## Lifetime, callback and unchanged-function boundaries

New selection reads private immutable stored metadata only. The old safe
expression's evaluation and virtual-call order are unchanged; the correcting
unsafe path reads private words/sign only and never invokes coefficient
toLong, multiply, byte conversion or metadata overrides. There is no runtime
class gate: current-own-format truthful restored coefficient subtypes follow
the same stored-value proof. Internal trace/count/which equal instance remains
diagnostic under parent526 policy; hostile replacements are not blanket
waived, and existing fixtures/assertions remain active.

Explicit caller overrides of decimal conversion methods still receive ordinary
language dispatch, including replacement results/exception identity. A caller's
super selects this base implementation. No catch is added. No mutable array or
reference to input storage is returned or written; the kernel reads at most
three limbs and has one Long accumulator. The sufficient predicate has no
allocation dependent on scale distance/magnitude. Physical primitive boxing or
ordinary allocation guarantees are not inferred for every platform.

Exact decimal conversions, toBigInteger/toBigIntegerExact, public BI toLong,
Float/Double bodies, kernels, 590 exact residual certificate, BI549 comparison
normalization, all fields/caches/serialization and every existing source/test
body outside the two specified BD hunks and new BI helpers remain unchanged.

## 549: canonical BigDecimal comparison image

The public [compareTo](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#compareTo(java.math.BigDecimal))
result is exactly -1/0/1. The previous public return forwarded primitive
comparison magnitudes, and could negate a raw magnitude for a negative receiver.
Public primitive contracts guarantee an ordering sign; they do not establish
an exact unit image or exclude the abstract Int.MIN negation boundary. No
actual target failure was observed for this premise.

The compact same-scale branch still reads left/right stored compact values in
the same order and performs one primitive comparison, then maps its sign to
-1/0/1. The noncompact route retains left signum, right signum, unequal-sign
branch, zero branch and the same single compareMagnitude call. Unequal signs
now return an explicit unit from their order. Equal nonzero signs branch on
the magnitude's sign **before any reversal**; a positive reported left sign
uses that order and every other nonzero sign uses the opposite order. Thus
the original `leftSign>0` versus `else` convention is retained even for false
reported signs±2, rather than multiplying by the reported sign or changing
their branch classification. Getter order and internal compareMagnitude bodies
are unchanged. A raw Int.MIN value is never negated in this public result.

This removes the public unit-image premise. It does not newly prove arbitrary
false metadata comparison mathematics or normalize internal private helper
return values. Those retain their existing canonical/truthful leaf premises
and diagnostic observations. Existing min/max calls still dispatch through
public compareTo and preserve their receiver-on-tie choices.

## Three prepared common methods and independent expectations

All are UNCOMPILED / UNRUN. No fake magnitude cap or forced eligibility flag is
introduced to make a small input reach the giant outer branch.

* The stored kernel method exercises19 literal hex magnitudes, both signs,
  widths32/64 and10/15 raise selections:950 direct calls. Values cover zero,
  30-bit limb carry, 32-bit truncation, 60/63/64/90-bit boundaries, high ignored
  words and mixed digits; every input is below128 bits. Expected values come
  from an independent width-bounded bit-vector model: select bits from hex,
  multiply by10 via (v<<1)+(v<<3) with one-bit carry, apply two's-complement
  sign, then assemble primitive bits. It uses no production integer operations,
  stored30-bit layout or Long multiplication. Its sum is at most3, its bit
  indices are at most63 and its carry is at most1; input text has fewer than32
  hex digits. The expected-model loops are bounded independently of source
  magnitude/Int endpoint distances. Before/after source text checks cover
  immutability.
* The public scale method uses the same19×2 signed coefficients at17 literal
  scales, including0, -31/-32/-33, -63/-64 andMIN:2584 top-level conversions
  across Int/Long/Byte/Short. Six separate fractional literals add12 direct
  Int/Long truncation controls; positive-scale MAX and zero-at-MIN add4 more,
  for2600 prepared calls. These are all bounded **safe-old-route or existing
  zero-tail** public controls, not positive execution evidence of the unsafe
  outer selection. Direct kernel calls above exercise the real correcting
  arithmetic without a synthetic production maximum.
* The comparison method has23 literal values in12 independently ordered
  numerical groups, including different-scale equal cohorts, zero cohorts,
  compact values and adjacent2^128 values of both signs. All529 pairs compare
  the **raw exact** unit result with an index-branch literal; two Int-scale
  endpoint controls make531 prepared comparisons. No production comparator,
  rounding or primitive compareTo image is used to produce the expectations.

The three method definitions add12 prospective executions across four local
targets to source248's prospective1699, giving1711; these are not actual test
results. Existing numeric/error/subtype/Java/super fixtures and all assertions
are untouched. Later independent source review, compilation, fresh complete
four-target inventories/JAR provenance, exact-head hosted supported jobs and
public profiles remain root-owned gates. Nothing here closes all591/549/548,
541/526,557 or official JCK.

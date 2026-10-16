# 590: exact-zero integral residual outside LOW preference

SPDX-License-Identifier: Apache-2.0

SOURCE ONLY; all new code and three common test methods are UNCOMPILED / UNRUN.
This private candidate is based on clean47447f2111d44735de374ec98d0a6a099a08888f,
source247 manifest529cc142d9a42110084155987b3022b36c2309168a159b745bf5191a0359c5a4.
The complete pre-edit tree is preserved in read-only archive
`../evidence/original-47447.tar`, SHA256
aa50ddf75ac39ddb645d8a13f57f1d67b3c28f4405df973a1fcc2bfd318a798c.
The accepted548 audit fa14a814a11948891d193c0a18ac74e32570e49699bad0bfed2d844366e83c7e
is preserved separately. No public checkout, old proof/evidence, fixture,
compiler/script/test configuration, numerical kernel or tracker state is edited.

## Narrow source change and premises

Only BigInteger changes. The original certifiedDecimalResidual583 safe-old
predicate remains first. The one added selection is S=Sa-Sb<MIN, evaluated in
Long; the old in-range and HIGH bodies are otherwise byte-preserved. A new
internal/JvmSynthetic method certifiedExactLowResidual590 is independently
callable by common tests. It adds no public Kotlin API, field, cache, serializer
behavior or synthetic production range maximum. BigDecimal and all old581/582/
583 raw/helper bodies remain byte-identical to source247.

The proof assumes supported immutable stored magnitudes. Captured BigDecimal
prefix callbacks and the existing callers are unchanged. The certificate reads
only private signs/words and final stored decimal helpers. Current-own-format
truthful BigInteger subclasses are not excluded by a runtime-class test; their
stored magnitudes follow the same proof. No claim is made that arbitrary
replacement/poisoned inherited callback observations match JDK. Existing direct
caller override/super contracts and diagnostic assertions are not waived.

## Exact certificate

The helper declines unless S<MIN, source/divisor/candidate are nonzero, and the
candidate sign equals the product of stored source/divisor signs. It computes
U=max(Sa,t+Sb) in Long, with t the candidate scale, and requires U in Int before
constructing an exact value.

It invokes unchanged581 largePrecision at p=MAX with fresh positive base-class
wrappers of the source/divisor words and the stored sign product. Those wrappers
share immutable input arrays; the bound existing operations use separate
scratch. They cannot invoke caller-owned overrides. For a reduced denominator
d=2^a*5^b, the leaf returns C@E with

`m=max(a,b), C=n*2^(m-a)*5^(m-b), E=S+m`.

It first checks E, then builds only the exact supported coefficient C. The LOW
domain has -S>=2147483649>L and a,b<=L-1, so E<=-3; any returned exact value is
an integer. No expanded integer with -E decimal zeros is published. A remaining
denominator factor, E outside Int, or unsupported C is out of this certificate's
scope and preserves the old fallback.

Only ArithmeticException from this own-base largePrecision invocation is caught
to decline. Under the accepted581/gcd/division/power leaves, its arithmetic
rejections here are exactly the nonterminating/range cases just described;
divisors are nonzero and exponents nonnegative. No caller invocation is inside
that catch. Comparisons and the final return are outside it. OOME, other Errors,
assertions, caller failures and unrelated exceptions are not swallowed. Decline
does not manufacture a value or mark the old path successful.

The returned C@E is compared with the supplied candidate Q@t by two independent
stored conditions:

1. digits(C)-E == digits(Q)-t, evaluated in Long;
2. compareDecimalSignificand(C,Q)==0, using the existing528 exact stored helper.

Together with the checked sign these conditions are equivalent to numeric
equality, including padded candidate cohorts. A wrong sign, coefficient or scale
cannot certify. This test does not choose or alter the candidate's cohort,
precision or identity; those are obligations of the existing integral prefix.
If equality holds, A*10^(-Sa) equals divisor*Q, hence the exact residual is zero.
The helper returns ZERO@U, precisely the old mathematical multiply/subtract
cohort max(Sa,t+Sb), without constructing the public product. Zero at this scale
is supported. The caller retains its already chosen quotient object.

## Completeness of the selected exact domain, not a LOW rescue

The accepted548 argument proves that, with S<MIN, any nonzero fractional
Euclidean residual forces the integer quotient's minimal coefficient to have
at least854569665>H646456993 digits. A supported ordinary integral answer in
this domain must therefore be an exact ratio. The new certificate can validate
that exact answer when its natural581 result and U are already supported.

It deliberately does not pad a natural E<MIN to MIN, strip/select a different
exact cohort, change the zero prefix or accept U outside Int. For example
1@MIN divided by1@1 still declines even though 10@MIN is a legal alternative;
the separate qualified LOW rescue policy is untouched. Insufficient finite
precision can give a numerically different candidate; equality then fails and
the original admission/error chain remains. The new path never rounds a
residual or uses context precision to discard residual digits.

## Symbolic supported-result witness and call composition

Take m=750000000, x=1@MIN, divisor=2^m@1 and p=MAX in any mode. The exact
integer quotient is Q=5^m@E, E=MIN-1+m=-1397483649. Input/result coefficients
are supported: bits(2^m)=m+1<L, and 5^3<2^7 implies5^m<2^1750000000<2^L.
The coefficient has no trailing ten, so E is already the unique closest scale
to S=MIN-1. The required precision is at most H<MAX. Even a literal expanded-
integer digit interpretation does not invalidate this case: 2^m>100 implies
K=10^(MAX+2)/2^m<10^MAX, hence expanded integer digits<=MAX.

Existing581 largePrecision returns Q before any new residual work. The old
materialized residual predicate first declines because sourceRaise=m exceeds
646456992; independently, 5^3>2^6 gives quotientBits>=2m+1,
and quotientBits+divisorBits-1>=3m+1>L. The added LOW selection is therefore
actually reached. Recomputing the same exact quotient and comparing it certifies
ZERO@U with U=E+1=-1397483648, both an Int scale and the required residual cohort.
No intermediate10^m is attempted. This is a source/math witness, not a practical
allocation, timing, successful run or host-JDK observation.

The independent paper challenge
`/private/tmp/krogu-590-source-challenge-XJzyLffB/REPORT.md`, SHA256
4583cefa63c3a3c478fc1fd0c49ece4ec2d7b65c8fe9f278747ae18ec6d770bb,
accepts this original witness and exact reachability, including the base5 pow
precheck2m<L and selected partial exponents<=m. It is an input to this proof,
not independent acceptance of the new candidate implementation or tests.

Finite divideToIntegralValue at BigDecimal:451–458 receives the non-null zero
certificate and omits only the old exact product/subtract admission. It retains
its candidate and original final integralStrip. No-context integral itself was
already supported by its581 fallback and is unchanged. No-context pair at481–486
now receives the zero certificate after obtaining Q. Context pair at488–495
passes finite admission, then uses the same certificate for the pair residual.
Both remainder overloads retain their pair delegation. p=0 delegation and all
old safe/in-range583 routes remain unchanged.

## Bounds, aliasing and lifetimes

New selection arithmetic is Long on two Int scales. U lies initially in
[-4294967296,4294967294] before max with Sa and before the explicit Int check.
Digits are in[1,H]; each digits-scale expression lies in[-2147483646,2793940641],
well inside Long. Stored sign products are in{-1,1}; no user arithmetic is used.
There are no new loops, mutable arrays, counters, index conversions or strings.

Workspace is the unchanged581 largePrecision workspace plus the already-live
candidate and one fresh exact result. Base wrappers add no copied magnitude.
All gcd/division operands remain supported; positive partial power products are
bounded by the final C where construction completes. The existing528 digit
helper uses only supported threshold powers (or its H boundary shortcut).
Significand comparison clones at most one supported coefficient array and drops
bounded decimal chunks in place, never mutating either input. Its original Int
indices/active lengths and Long primitive bounds are unchanged. Temporary exact
result and comparison scratch become unreachable after certificate return.

This repeats exact quotient work and can allocate another supported coefficient;
no allocation improvement, performance benefit, heap sufficiency or giant
execution feasibility is claimed. No p-sized inexact buffer is allocated at
p=MAX: the exact-only581 leaf is reused. The possible cost is a material review
limitation, not a smaller numerical domain or a fabricated resource guarantee.

## Prepared tests and remaining gates

DecimalExactLowResidual590Test has three methods, all UNCOMPILED / UNRUN:

* Direct real-certificate controls use seven independent terminating rational
  literals, all four operand sign combinations and two numeric candidate
  cohorts. Every accepted result checks zero and U; wrong coefficient, scale
  and sign each decline. The largest literal candidate61728394550 fits Long
  and crosses the30-bit limb boundary. Original source/divisor/candidate values
  are checked afterward. This is56 successes plus168 wrong-candidate controls.
* Ten explicit declines cover in-range/HIGH preference, zero source/divisor/
  candidate, U HIGH, nontermination, unchanged natural-LOW rescue, and two real
  outer-bridge outside-LOW declines with tiny coefficients. There is no fake
  production cap or substituted small range limit.
* Three independent endpoint families, four sign combinations, unlimited calls
  and all eight modes at p=MAX check strict quotient and zero remainder cohorts
  through integral/pair/remainder (324 top-level calls). These public bounded
  controls retain the safe materialized route; they are not positive execution
  evidence of the giant new outer dispatch. The direct helper controls exercise
  the actual new arithmetic certificate without giant values.

Independent source/proof review, compilation, meaningful test execution across
JVM/JS/Wasm/macOSARM, fresh exact-source/JAR provenance, exact-head supported CI
and original public normal/large profiles remain later parent-owned gates.
Old fixtures/assertions and every other source file are unchanged. Task590,
548/541/526, broader range/resource obligations and official JCK are not closed.

## Exact implementation pins

BigInteger.kt:4d325b3272c3b9bd120f919bf7a60f7b46523b1266cd304f4cf1020c0a3651cb.
DecimalExactLowResidual590Test.kt:c76002dd62ea2feb88697370ec814eda277922c7c3ed4994779db0ef9bda8388.
Unchanged BigDecimal.kt:5731c8df6ba366a51d907aec97cdef0874df21607638bdcf37b865e85426f741.
Unchanged DecimalDivision581.kt:e974af13310f8aec453223760e4d6d9554ed11d251cfe5358ec5cbee1d4131dc.

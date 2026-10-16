# 592 fixed-scale quotient without public normalization magnitudes

SPDX-License-Identifier: Apache-2.0

SOURCE ONLY; code and three new common methods are UNCOMPILED / UNRUN.
Base is the frozen591+549 source249 manifest
`dcc46f2f58b0f1aa0dc7d26f88c9200d643344241d6a8a1c59c01d985447217d`.
Before edits, all249 original sources were archived read-only in
`../evidence/original-source249.tar`, SHA256
`cca8859897f20be27b13eddea34a45ba5f16c287933b4a934108ccd619d3c95e`.
Original BD/BI/DecimalDivision581 copies and manifest are preserved separately.
No591 original, root248 runtime tree, main, fixture, consumer or task was edited.

## Requirement and accepted independent challenge

[Java25 explicit-scale divide](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#divide(java.math.BigDecimal,int,java.math.RoundingMode))
rounds x/y at the **requested Int scale**, including mandatory coefficient
zeros. It rejects divisor zero or inexact UNNECESSARY; legacy mode integers
add their invalid-token IAE. A supported required coefficient cannot be
rejected solely because a disposable scaled numerator/denominator is outside
the public BigInteger range. This does not guarantee every physical allocation
or close all548/541/526 obligations.

Independent source/spec challenge
`/private/tmp/krogu-592-source-challenge-r4H0fG4w/REPORT.md`, SHA256
`0d90be1caee506abafad9e61735cfb97987e77baed55649e038711cfebf9e68c`,
accepts the concrete obstruction and conditionally accepts the stream math.
It is not review or execution of this implementation. No foreign numerical,
stdlib/compiler implementation source or bytecode was read.

Let L=2147483647, K=646456992, H=K+1. For supported A=2^L-1, A/A at target1
requires10@1, but old numerator10A is unsupported; target-1 needs0 or1 while
old denominator10A is unsupported. Distinct denominator A-1 gives a ratio
strictly between10 and11 at target1. Also x=1@(MAX-K), y=10^K@1,
targetMAX/UNNECESSARY requires10@MAX while old outer staging checksMAX+1.
These giant witnesses are **symbolic UNRUN**. Supported integer inputs do not
imply their whole decimal strings fit every platform String type.

## Narrow source selection and callback boundary

Only the explicit-scale enum divide body adds a raw attempt. The existing
public divisor.signum zero guard and failScaledZeroDivision call remain first;
then the original Long shift `u+Sb-Sa` is captured. Stored BI certification
runs **before** the disposable outer range checks and quotientExponent's whole
coefficient Strings. Rounding-only and legacy overloads retain their original
virtual delegation and mode-token validation.

The BI bridge declines for stored zero source/divisor. For nonzero inputs it
retains the complete old body when `abs(shift)<=K` and both

    bits(A)+4*max(shift,0)<=L
    bits(B)+4*max(-shift,0)<=L.

All expressions use Long. These sufficient conditions prove required public
normalization powers/products supported; they do not prove every old String,
allocation or hostile callback behavior. Failed sufficient bounds select raw,
not an arithmetic rejection. There is no runtime-class gate or smaller cap.

On the retained old path the new reads are final stored-only helpers, so no
caller callbacks precede or replace the old sequence. On corrected unsafe
paths, later coefficient sign/abs/toString, source signum, normalization and
private-product callbacks can be skipped. Those internal observations remain
retained and separately classifiable: no assertion/fixture is removed or
blanket-waived. Explicit caller override/super/result/exception identities and
the already-reached public divisor-zero guard remain unchanged. Correct
transitive numerical leaves and current-own-format truthful coefficient
subtypes owe the same stored mathematical answer; arbitrary false replacements
are not made a numerical oracle by this proof.

**divideScaledCoefficients and every prior DecimalDivision581 method remain
byte-identical.** In particular context581's `checkSourceSign=false` fallback
never selects the new592 route. Its metadata-decline/guard/classification
boundary is not silently replaced. Context/exact/integral/residual and591
bodies, fields, caches, serialization, APIs and existing assertions are intact.

## Stream invariant and fixed-scale rounding

For nonzero magnitudes A,B and sign±1, write D=digits(A), shift=s and n=D+s.
Required magnitude is X=A*10^s/B. Raw conversion creates own decimal cells
directly from immutable binary words, without String or caller methods.
At position j the input prefix Pj comprises A's first j digits, or A followed
by j-D virtual zeros. Long division maintains

    Pj=B*Qj+Rj, 0<=Rj<B.

Reading one next input digit d forms10Rj+d<10B; choosing the largest trial
0..9 times B not exceeding it and subtracting establishes the next invariant.
The one new local digit-step uses the existing private product/compare/subtract
and page primitives; it never materializes B*10^-s or A*10^s publicly.

If n<0, X<10^n/B<=0.1 strictly: directed modes give signed0/1, nearest gives0,
and UNNECESSARY rejects. This branch scans no virtual positions. If n=0,
the integer part is zero and the first digit-step supplies a real guard.
For n>0, stream through position n. When n<D, the unread original suffix
contributes less than1 before division by B, so cannot change floor(X).
When n>=D the virtual-zero recurrence gives the same floor. Leading zero
quotient digits do not count as coefficient precision.

Once nonzero quotient has begun, zero digits are deferred as a Long count.
Before a later nonzero digit, their required digit bound is checked, then they
are appended using the unchanged in-place shift operation. If R=0 and every
original nonzero digit has been read, all remaining logical digits are zero:
the stream skips them in O(1) counter work, checks their **mandatory** final
digit bound and appends them without changing target scale. An unread nonzero
original suffix forbids this skip.

At position n, remainder0 and no unread nonzero digit mean exact coefficient.
Otherwise one further digit-step yields guard g and sticky from the new
remainder or unread original nonzero digits. The fractional part is (g+f)/10,
with0<=f<1 and f=0 exactly when sticky is false. Hence g<5/g>5 decides the
half relation; g=5 is tie iff no sticky. Existing increment maps all8 modes,
sign and retained Q parity; it is called only when the original fraction is
nonzero. Negative operands round magnitude with the same parity rule then
receive their stored sign. Zero is returned explicitly before the existing
nonzero publisher. Carry9…9+1 is retained as10…0 at the **same requested
scale**, never divided by ten or stripped toward a preferred cohort.

## Necessary output bounds, indices and lifetimes

From three Int scales, -6442450943<=s<=6442450942; D<=H gives
-6442450942<=n<=7088907935. Counts/positions remain Long. If n-digits(B)>H,
floor(X) already needs more than H digits, so no mode can rescue a supported
fixed-scale coefficient. The code rejects before virtual scanning. Otherwise
positive n<=H+digits(B)<=2H: huge work requires huge input or required output,
not billions of irrelevant zeros for tiny ordinary operands. This is not a
speedup or a practical maximum-input guarantee.

An H+1-digit coefficient is at least10^H>=2^L and unsupported. Every deferred
append and nonzero digit checks this necessary bound first. H digits may still
exceed L bits, so only the unchanged final publisher's actual binary-bit check
decides support there. No optional cohort normalization can rescue fixed scale.
Quotient floor has at most max(1,n-digits(B)+1) digits; capacity is the lesser
of that bound and H, rounded to base1e9 cells **plus one carry cell**. At most
71828556 cells suffice. Carry is checked before publication, including a
possible H+1 digit; no zero enters publish's top-word indexing.

Remainder and trial have b.size+1 cells, enough for10R+d<10B and9B. Primitive
products are below10^10, safely within Long; reused binary/decimal conversion
products retain their accepted <2^60 bounds. Real source digit lookup occurs
only for j<D<=H, so decimal indices fit Int. Virtual positions are never cast
to array indices. Deferred whole-cell shifts are bounded by the final H-digit
check and retain the existing high-to-low copy ordering.

Inputs remain read-only and unretained. Simultaneous workspace includes shared
binary conversion scratch, both converted inputs, quotient, remainder/trial,
publisher binary scratch, output bytes and constructor-owned binary result,
plus page headers. This is bounded by supported input/output sizes under the
existing page capacities; it is not a proof of heap availability or low memory.
Conversion and division can be expensive/quadratic. No cache, new production
cap, host/JDK mathematics, Error catch, OOM translation or serialization path
is introduced.

## Prepared independent controls and remaining gates

Three common methods are prepared, all UNCOMPILED / UNRUN:

* Direct real raw-helper calls use14 positive Long magnitudes,8 divisors,
 7 signed shifts,2 result signs and8 modes. Long numerator/denominator products
 are guarded before multiplication. The independent oracle uses primitive
 division/remainder and r versus d-r, not production decimal operations or
 the digit stream. All quotient increments fit Long: a nonzero remainder
 implies denominator>=2, while denominator1 has no increment. Cases cover
 .1/.5/1, tie parity/carry, guard0 with unread sticky, base1e9 and30/60-bit
 boundaries, and input nonmutation. At most12544 grid calls precede overflow
 exclusions;16 exact padding calls require1 followed by100 zeros, all modes
 and both signs. No fake production cap or same-output oracle is used.
* Public true endpoint routes use tiny coefficients and actual Int scales:
 large negative shifts produce required directed0/1 at the requested scale;
 the maximum positive shift rejects from necessary output size before a huge
 loop. Enum/legacy errors, rounding-only delegation, exact110@1, zero divisor
 and zero dividend retain independent literals. These bounded **new raw
 endpoint** controls differ from the unexecuted giant A/A, A/(A-1) and10^K
 obstruction witnesses. No giant positive small-result dispatch is claimed
 executed.
* Explicit base-typed caller override results/exception identity and a real
 super entry are kept separate from arithmetic/internal trace claims.

All old sources/tests/fixtures/consumers remain unchanged except the explicit
BD insertion, one new BI bridge and one separate581 method. Three methods add
12 prospective four-target executions, not actual PASS results. Independent
source/proof/test review, fresh composed JVM/JS/Wasm/macARM inventories/JAR,
retained ledger, exact-head supported/public gates and parent adoption remain
open. This does not close592 or all548/549/541/526/557/JCK. No compiler, script,
numerical/helper/model/certificate/probe/test/build/process/Git/tracker/mutex
execution occurred; all owned preparation calls are terminal.

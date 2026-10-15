# Integral quotient 582: isolated implementation argument

SPDX-License-Identifier: Apache-2.0

SOURCE ONLY, UNCOMPILED / UNRUN. The parent accepted the in-range design
`4b6bcad97a5ad0865615a444ae3e1a97db371128d989027cee57a1835b4bc33c`
and granted this private implementation at base
`f473ae7741973da1dd75633721994559ec3c085d` plus twelve frozen581 files.
This is not runtime acceptance, an integration result or whole541/548 closure.
No compiler, script, helper, numerical operation, certificate, probe, test,
build, measurement, Git or tracker execution occurred. No foreign numerical
implementation source/body/bytecode was consulted. The public checkout and581
validation trees were not edited.

## Exact source boundary

BigDecimal's no-context `divideToIntegralValue` captures the existing magnitude
comparison, source sign, source precision, divisor precision, source scale and
divisor scale. Splitting the original short-circuit does not change which public
call executes or its order: source sign is called only after a nonnegative
magnitude comparison, and divisor sign only when the source sign answer is zero.
The same old guard arithmetic and MathContext construction precede the bridge.
A decline executes the same virtual contextual divide and old postprocessing.
The entire finite-context overload, integralStrip, pair/remainder bodies and all
old581 rounded/largePrecision/publish and reached primitive bodies are unchanged.

New `BigInteger.integralDecimalQuotient582` reads only stored signs, words and
nonvirtual stored metadata. It declines zero stored coefficients, S outside Int,
captured scale/digit/source-sign mismatches, or a captured magnitude ordering
different from the stored ordering. A compare result is compared by sign, not
by a particular positive/negative magnitude, respecting the comparator contract.
The stored ordering is adjusted exponent then stored decimal significand.
An accepted call has |source/divisor|≥1. No runtime-class gate, extra public
operand callback, new instance field/cache, serialization or public declaration
change is introduced. Current-own-format truthful coefficient subtypes therefore
remain eligible without a foreign serialization compatibility mechanism.

This guard does not identify every contract-breaking subtype. A replacement
contextual-divide override with truthful metadata may be bypassed by the new
inherited arithmetic. Direct caller overrides/super calls retain their language
contracts; no new inherited internal callback-count/order/noncall/identity promise
is made. Existing fixtures remain active and unedited. In particular
`DecimalIntegralDispatchOracleTest.inheritedIntegralDivisionUsesVirtualContextDivision`
contains truthful-metadata replacement/throwing divide probes and is predicted
to expose changed internal-call fields on eligible inputs. No passing campaign,
blanket poisoned-subtype waiver or automatic consumer classification is claimed.
The parent's later exact per-input/per-field policy decision is a separate gate.

## Direct supported route for S≥0

Let positive coefficients be A,B, stored scale difference S, and
K=floor(A/(B10^S)). Acceptance of the prefix gives B10^S≤A. Thus 10^S≤A and
S≤Da−1≤H−1, H=646456993. Fresh base-class wrappers around the immutable stored
magnitudes avoid caller-owned arithmetic overrides. The bridge constructs
TEN.pow(S), the supported denominator B10^S, and calls existing quotientStored
on A. At S=0 it uses the base divisor wrapper directly; no multiplication by
one or decimal conversion is performed for the denominator.

The selected scale is S and coefficient K10^S≤A/B≤A. Each materialized positive
factor/product is bounded by a supported value; quotientStored's established530
scratch proof applies to supported operands. Its result is nonnegative, and the
bridge restores the product of stored signs with an immutable wrapper. There
is no fractional contextual result or K×B exactness product. The symbolic
D150000000, A=B+1 witness is now one supported integer quotient at S=0, returning
1@0. This is a source-range/cost argument, not an executed giant input, benchmark
or blanket performance improvement.

## Proved extreme-negative preflight

Before any decimal conversion, the bridge checks k=−S≥Db and
Da+k>H+2Db−1. For a nonzero final residual R, the divisibility argument below
gives z<Db, while the final quotient has I≥Da+k−Db digits. Thus
I−z≥Da+k−2Db+1>H: its minimal coefficient is necessarily unsupported.
The preflight must still preserve exact supported results; it cannot simply
reject this input class or use a smaller limit.

It calls the UNCHANGED581 `largePrecision` helper with fresh positive base
wrappers, stored sign/preference and precision MAX. That helper reduces by gcd,
strips denominator factors2/5, rejects a remaining denominator, or constructs
the exact natural numerator N at E=S+m, m=max(v2,v5). For a reduced denominator
d>1, coprimality shows N has no trailing ten: when v2>v5 the numerator is odd;
when v5>v2 it is not divisible by5; and when both positive valuations agree the
reduced numerator is coprime to10. If d=1, m=0 and N=A/g≤A, so it is supported
even when it has trailing zeros. An unsupported constructed N for d>1 cannot
be rescued by deleting decimal zeros.

The scale E fits Int: E≥S≥MIN, m≤L−1 and k≥Db≥1 give E≤MAX−1. A supported
returned N@E must have E≤0. Indeed the preflight inequality makes the exact
ratio exceed10^H (its integer-part digit count is at least H+Db); a positive E
would require N to have more than H digits. For E≤0 and d>1, no trailing ten
means the minimal integer cohort is exactly N@E, and E≥S selects that same
preferred integral cohort. For d=1, E=S already is the preferred cohort.
Therefore a successful helper result is exactly the required integral pair.
If the rational is not an integer at the endpoint, the earlier nonzero-residual
bound proves the integral answer unsupported. Failures from growing positive
numerator factors/powers likewise imply an unsupported minimal final coefficient;
the construction does not contain a larger dispensable positive product.

For tiny1/3 at S=MIN, this takes a tiny gcd/2/5 rejection instead of streaming
approximately H digits. Exact1/2 and denominator-one cases remain small. This is
an all-input bound and an exact-reduction reuse, not a special1/3 whitelist,
observed timing or resource exception converted into arithmetic failure. Large
input factor reduction/construction still has its existing allocation/time
costs; no maximum-domain resource claim follows.

## Separate negative-S stream

`DecimalDivision581.integral` is a new separate method; its callers supply
canonical nonempty supported base2^30 magnitudes and S<0 within Int. Put
k=−S in Long, L=2147483647, 1≤Da,Db≤H and N=Da+k≤2793940641. It converts A,B
to immutable private radix10^9 cells using the unchanged581 conversion, one
reusable copied binary scratch and active lengths. It then consumes Da original
digits followed by k virtual zeros. Original inputs are never written.

The remainder/trial step is restoring division: 0≤R<B gives U=10R+a<10B.
Try d=9 down to1, or0 if none fits; subtract dB only for a nonzero digit.
This leaves 0≤R<B and preserves processedDividend=B×emittedQuotient+R.
At most ten small trials occur at a position. After N positions the quotient
is exactly floor(A10^k/B), independent of sign and rounding mode.

The quotient is represented by nonzero-ending C and deferred-zero count z,
Kprefix=C10^z. Leading zero digits are ignored. A zero digit increments z without
writing cells. A nonzero digit checks the exact new core length
digits(C)+z+1 before appending zeros and the digit; the first digit has length1.
Minimal core length never decreases, so exceeding H proves an unsupported
coefficient in every possible continuation. H is the public accepted magnitude
digit ceiling, not a new smaller production limit. H-digit results still pass
the final exact bit-length check.

The exact skip requires both R=0 and position≥lastNonzero(A), not R alone.
Then every remaining original/virtual digit is zero. Adding N−position to z
and stopping is exact. It handles arbitrary unread original zero tails and
the MIN-scale unit/half examples in bounded work. A zero quotient returns ZERO
with scale S explicitly and never enters the nonzero publisher.

## Progress, capacities and publication

After A and t virtual zeros, A10^t=BC10^z+R. If R=0, exact skip finishes.
For R≠0 and t≥Db, z>t would make the nonzero R divisible by10^t>B, impossible.
Thus z≤t, and divisibility by10^z gives z<Db. With nonzero quotient digit count I,
I≥Da+t−Db. While digits(C)=I−z≤H, Da+t≤H+2Db−1. By position H+2Db≤3H,
the next step must terminate exactly or reject a proved unsupported core.
Before t reaches Db or the quotient starts, the same coarse bound covers all
positions. This bound is independent of a virtual suffix of billions for tiny
inputs; it is still potentially hundreds of millions of steps and is not a
practical-time guarantee or source of a smaller rejection cap.

The quotient capacity uses the proved output upper bound
max(1,min(H,Da+k−Db+1)) decimal digits: A<10^Da, B≥10^(Db−1) give that bound
for a nonzero final quotient. Every retained core/padded selected coefficient
is no larger than the full quotient, so this also bounds all quotient writes.
It avoids a maximum-sized page table on normal tiny inputs. R/trial each use
b.size+1 cells because 10B has at most one extra radix cell. Input decimal cells
are bounded by ceil(H/9); binary scratch is bounded by ceil(L/30). All capacities
are computed in Long before proven-safe Int conversion. Positions/z/N use Long;
there is no negation of Int.MIN_VALUE or Int scale subtraction.

New `appendZeroDigits` first verifies nonzero input, nonnegative count and
digits+count≤H. It moves whole radix cells from high to low, clears exactly the
new low cells, updates active size, then multiplies by10^(count mod9). This
ordering preserves unread cells during overlap. Whole/count narrowing is bounded
by H, and size+whole is bounded by the quotient capacity. The residual power is
at most10^8: per-cell product/carry is below10^17 and fits Long. Existing
multiplyAdd writes any final carry, and trim keeps canonical active length.
This primitive is called only from the new integral method; old581 primitive
bodies and their callers are unchanged.

Remainder small-digit operations have per-cell parts below10^10; input conversion
and binary publication have parts below10^9×2^30<2^60. Comparisons read active
lengths only. Cells provides zero for unwritten valid storage, checked capacity
access and private pages. No mutable array is exported or aliased with input.

At completion K=C10^z, choose T=max(−z,S) in Long, then append z+T zeros to C
before publication. T is in Int because S≥MIN and T≤0. If z≤k, the coefficient
is C. If z>k, it is K10^(−k)≤A/B≤A, so its required padding is supported. A
minimal H-digit C exceeding L bits cannot be rescued by another integral cohort;
all integral coefficient representations have magnitude at least C. The
unchanged publisher's increasing positive Horner prefixes can therefore reject
only an unsupported final coefficient, not an oversized dispensable numerator.
Apply the supplied stored sign at final construction. This selects the closest
integer cohort to S, with no outside-Int clamping or optional LOW rescue.

Live storage is bounded by seven workspaces: binary conversion scratch; immutable
decimal A/B; mutable remainder/trial; quotient/final decimal cells; and binary
publication scratch. The public byte array/result are output ownership, separate
from these workspaces. Scratch objects may remain live until method return; no
unproved GC release or peak-memory reduction is assumed. Actual max-range memory
and timing remain548, and allocation failure is not caught or relabelled AE.

## Prepared tests and independent oracle

All five methods below are UNCOMPILED/UNRUN. There is no fake smaller production
cap, maximum-domain allocation, harness run or generated fixture change.

* Common `ordinaryAndTruthfulIntegralCohortsAgainstBoundedRationals` crosses13
  coefficient pairs, nine S values, four signs and four ordinary/truthful masks.
  Its oracle uses one-shot bounded Long numerator/denominator division and
  remainder identity, then decimal-zero stripping/padding to the preferred
  scale. It never calls production arithmetic for the expected quotient.
  The largest scaled numerator is below1.3e16<Long.MAX_VALUE. Selected final
  coefficients are bounded by the same input magnitude inequalities. Inputs'
  textual values remain checked before/after each public call.
* Common `directNegativeStreamKeepsUnreadDigitsDeferredZerosAndInputs` has9
  distinct pairs, three negative S and two signs; independent Long results
  cover unread nonzero tails, whole-cell deferred-zero appending, zero quotient,
  two-cell trial/carry/borrow and both supplied-array isolation checks.
* Common `virtualZeroSkipPreferredZerosAndUnlimitedDelegation` uses five
  separately derived rational literals with z<k, z=k, z>k and S>0 discarded
  tails, both signs/all modes and p0 delegation. Three tiny-coefficient MIN-scale
  controls owe ±1@MIN, ±5@(MIN+1), and ±1000@MIN. They use the proved rational
  preflight, not giant allocated coefficients. Three terminating1/2 neighbors
  around its exact threshold must give5@(1−k) through either stream-skip or
  preflight. Two tiny1/3 cases require AE for proved unsupported final minimal
  coefficients. Zero/preferred-scale and zero-divisor categories stay
  separate. The literal1234567/(3×100) truncates to4115, represented411500@2.
* Common `capturedMismatchFallbackAndExplicitCallerContracts` distinguishes
  wrong captured precision, scale or source sign from truthful arithmetic. The
  replacement7.9 result gives7 under the granted old fallback; it is labelled
  diagnostic behavior, not a general poisoned-subtype numerical theorem.
  Direct caller override receives the exact divisor, returns a caller-owned
  object, propagates a caller-owned exception, and a caller-owned super entry
  computes5@0. No internal trace/count/noncall/identity assertion is added.
* JVM `truthfulCurrentFormatCoefficientsKeepExactIntegralCohorts` follows the
  existing own serialization resolveObject setup on four independent rational
  literal rows, four signs and all restored/ordinary masks. Setup asserts the
  reachable truthful coefficient type/value/scale, then checks public numerical
  coefficient/scale results. It is not a general serialization identity promise,
  legacy fixture or cross-format compatibility test. The two original581 tests
  and all existing probes/assertions are unchanged.

## Limits and chronology

S outside Int declines to the existing fallback; its admitted LOW/cohort policy
remains unresolved for integral division specifically. The finite-context p>0
residual, precision/cohort behavior and pair/remainder exact product/subtraction
are unchanged and not proved complete by582. Their p0/no-context delegations
benefit from the corrected quotient, subject to remaining composition limits.
Inherited replacement callback fields require later exact classification, not
automatic archival. The accepted giant obstruction remains symbolic and UNRUN.

The twelve inherited581 files were copied read-only BEFORE edits to
`/private/tmp/krogu-math-task-582-u84RlRs4/evidence/originals-36aIDirz`.
Its original manifest SHA-256 is
`17115d0dfff86fe7e5856b4d8f135f600ac3041e76a6a4bbdd36933c37e72b6b`.
Original581 source/proof/tests/consumer history is preserved there. Only three
production files are changed, by the granted no-context seam, one bridge, one
separate helper method and one private primitive. Two tests and these582 documents
are new. There are no public API/fields/cache/integer-kernel/serialization changes.

The first581 runtime remains historically524 JVM methods/two failures. A separate
fresh581 campaign belongs to root; no outcome or integration is inferred here.
This implementation awaits independent source/test/proof review, compilation,
fresh artifact and exact-source behavioral audits on each supported target,
composition/consumer policy and hosted gates. The worker has no runtime grant.
Final exact source/document hashes are in the separate582 source handoff.

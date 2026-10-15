# Task528 focused allocation follow-up proposal

SPDX-License-Identifier: Apache-2.0

**PROPOSAL ONLY, UNIMPLEMENTED/UNRUN.** Accepted production/source/driver hashes
remain frozen. Grant1887 authorizes reporting a focused proved fix when measured
regressions block improvement acceptance; it does not authorize changing frozen
production without parent review. No new build/test window is requested from the
user. Task528 remains in_progress.

## Observed regression and reachable source

The accepted six-fork protocol shows comparison allocation increases for
128/1024/4096 digits: aggregate candidate/baseline bytes ratios1.275/1.904/2.251.
Precision allocation falls strongly. The negative4096-digit long-left last-digit
case specifically rises from about11.717MB to30.437MB per call (ratio2.598).
These are supported current-thread counters, excluding fixture construction;
all raw samples, GC activity and fork variation remain in the runtime report.

Owned `BigInteger.compareDecimalSignificand` forms `10^k` and calls existing
private `divideStored`. When the divisor occupies multiple limbs, that kernel
visits every input bit and repeatedly creates shifted/added/subtracted remainder
values. This is a concrete reachable allocation path, not a measured attribution
of every byte or an exclusive cause. Existing arithmetic kernels remain frozen.

## Narrow replacement to review

Limit a later change to the new owned significand helper and narrowly necessary
private stored-magnitude helpers in BigInteger, plus new focused tests/proof.
Keep digit-count E/E+1 bounds, maximal unsupported threshold handling, BigDecimal
virtual precision/exponent prefix, misleading reported metadata decisions,
equal-scale public comparison, signs/zero and current truthful-subtype numerical
contracts unchanged. No serializer, legacy compatibility, inherited callback-
count guarantee, public API, existing kernel, existing test/assertion, accepted562
binary helper, field or mutable cache change.

For positive stored magnitude L with more digits than S, k=D(L)-D(S)>0:

1. Clone L's little-endian30-bit words into one local scratch array. Maintain
   its active length; neither input array is mutated or retained.
2. Drop exactly k decimal places by repeatedly dividing the active scratch
   magnitude in place by10^9, followed by one10^r division for r=k mod9.
   Each pass is ordinary high-to-low one-word long division using Long
   intermediates. Retain a Boolean sticky flag if any pass has nonzero remainder.
   Small divisors use primitive exact integer powers; no large10^k is formed.
3. Compare the final active quotient words directly with S's stored words,
   respecting active length (do not compare the untrimmed scratch array as a
   canonical magnitude). If unequal, return that order; if equal, sticky selects
   greater versus equal. Reverse once when L was the right operand.

This is an independently written owned helper proposal. It reuses the own
one-word division identity, not external numeric implementation source. It must
not dispatch through caller-overridable methods or alter existing divideStored.
An implementation should compare the active prefix without creating a second
trimmed array. Scratch storage is one input-length IntArray; there is no retained
per-instance storage or shared mutable state.

## Proof obligations already resolved at algorithm level

For divisor m<=10^9<2^30, every loop remainder satisfies0<=r<m and each stored
limb w satisfies0<=w<2^30. Thus t=r*2^30+w <10^9*2^30<2^60<Long.MAX_VALUE.
The quotient limb floor(t/m) is<2^30; next remainder t mod m is in[0,m).
High-to-low replacement reads each original lower limb before overwriting it,
so induction on the processed prefix proves exact unsigned division in place.
Removing leading zero limbs preserves magnitude; active length cannot grow.

Let Q0=L and pass j have Q(j-1)=Qj*mj+rj,0<=rj<mj. The product of all mj is
exactly10^k. Substituting these identities proves Qfinal=floor(L/10^k) and the
original remainder is the sum of rj times positive products of earlier mj.
Every summand is nonnegative, hence the original remainder is nonzero iff at
least one rj is nonzero. Sticky is therefore exact, without storing a huge
remainder. This also covers many full9-digit chunks and a final partial chunk.

Since1<=k<=D(L)-1, every partial decimal drop leaves a nonzero quotient and is
within the accepted domain. All scratch magnitudes/limbs are bounded by the
already supported input; no aligned product or unsupported10^k witness is
allocated. If Qfinal<S, then Qfinal+remainder/10^k<S; if Qfinal>S, it remains
greater; if equal, sticky distinguishes equal from greater. This is the same
normalized-significand order as the frozen quotient/remainder proof, including
misleading virtual precision answers when the reported exponent branch ties.
Long-scale guard algebra and public dispatch decisions are untouched.

Complexity remains bounded by O(n*ceil(k/9)) primitive limb divisions with
shrinking active length and O(n) local storage, n=input limb length. The current
multi-word divideStored path creates intermediate objects per binary input bit.
Avoiding those particular objects is a source-level allocation argument, not a
new measured performance claim or an assertion about JVM retained bytes.

## Later evidence required

After explicit parent source grant, add bounded new controls for k=1,8,9,10,
17,18 and multiple9-digit chunks, exact multiples, remainder1, nonzero discarded
middle chunks with final remainder0, both orientations/signs, and nonzero
leading quotient words. Retain all current metadata/retained tests and the
truthful own-format subtype/explicit-dispatch controls. Independent source/bound
review must verify word invariants, active-length comparison, input immutability
and maximum-domain arithmetic before runtime.

Then a later sole-window grant must rerun complete four-target gates and the
unchanged accepted six-fork protocol against a fresh matched candidate JAR and
the preserved exact baseline. Preserve this original regression evidence.
The proposal is not implemented, source-accepted or runtime-proven; no acceptance
or speed/allocation improvement is claimed for it.

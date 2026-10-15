<!-- SPDX-License-Identifier: Apache-2.0 -->
# Integer arithmetic 544: source argument and contract boundaries

SOURCE ONLY, 2026-10-15. All new tests are **UNCOMPILED/UNRUN**. This is
independent paper reasoning over the own implementation, not a machine-checked
certificate, a runtime result, or automatic acceptance of local:544.

Baseline: HEAD `99c81a42a5d104e045bcfe6eca2246231d37bf80`, with the preexisting
accepted 570/571 and source-accepted 545 overlays. BigInteger SHA-256 is
`9096272de5969ff7372391a1d64c7810bbea62f350e2ab9a96c04e3191093cae`.
No production source, existing fixture, assertion, or proof was edited.

## Authorities and scope

The [public BigInteger API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html)
specifies signed arithmetic, truncating division, gcd of absolute values,
nonnegative integer powers and floor square roots. Division by zero, negative
exponents and roots of negative inputs fail arithmetically. The range contract
is not reduced here. [Kotlin inheritance](https://kotlinlang.org/docs/inheritance.html)
specifies overriding and explicit superclass calls; test-written override
bodies establish their own entry/exit and exception-marker behavior.
Only public documentation was consulted; no foreign numerical implementation
source, disassembly bodies or numerical probes were inspected/executed.

Reuse [NUMERICAL_ARGUMENTS.md](../NUMERICAL_ARGUMENTS.md), particularly signed
addition/multiplication, gcd, roots and powers. Its binary multi-limb division
description is historical: the current source uses the normalized radix loop
proved in [division530-proof.md](division530-proof.md), with selected outputs
proved in [division530-selection-proof.md](division530-selection-proof.md).
Those newer arguments govern the current division kernel. No historical fix
or task 530 correction/normalization matrix is recreated here.

Relevant source: `BigInteger.kt` add/subtract/multiply at 43–80; division and
normalization at 81–191; pow and shiftPower at 193–250; gcd at 251–259;
sqrt/pair at 550–568; compareTo at 777–780; normalization/magnitude helpers at
1140–1171. These are locations in the hashed overlay, not stable API anchors.

## Domain and normalized representation

Let B=2^30, M=B-1, and A=sum(a[i]*B^i). Each stored digit is an Int in [0,M].
The array is little endian, empty iff the value is zero, and otherwise has a
nonzero highest digit. Stored sign is -1, 0 or 1, zero exactly for the empty
array. Nonzero public magnitudes have bit length at most Int.MAX_VALUE in
this implementation. `normalized` removes leading zeros, canonicalizes zero,
and calls `MagnitudeLimits.checkedBitLength` before constructing a nonzero
result. The proofs assume valid immutable stored inputs and successful
allocation; constructor/byte-format correctness is a dependency, not reproved.

A truthful getter describes that unchanged stored value. A truthful arithmetic
override returns the specified mathematical result, even if its concrete class
or object identity differs. A replacement bit length inconsistent with the
stored value does not satisfy this premise and is not a numerical counterexample.
The new wrappers copy inherited results before chaining add/subtract so that
an inherited identity return cannot accidentally recurse through the wrapper.

Magnitude order first compares length: k limbs imply B^(k-1)<=A<B^k.
At equal lengths the highest differing digit at i outweighs every lower digit,
whose total difference has absolute value at most B^i-1. Signed comparison
orders different signs, reverses magnitude order for two negatives, and makes
zero equal only to zero. `compareTo` uses stored fields, not virtual metadata.

## Addition, subtraction and multiplication

For addition, after digit i-1 the carry is 0 or 1 and the emitted prefix plus
carry*B^i equals the processed input sum. The next temporary lies in
[0,2B-1]; masking and shifting restore the prefix equation and carry bound.
An extra final digit captures the carry. For subtraction with A>=C, the next
temporary a[i]-c[i]-borrow is in [-B,B-1]. Its residue modulo B is the digit,
and its negativity supplies a borrow in {0,1}. The prefix equation is
low(A)-low(C)=outputPrefix-borrow*B^i. The final borrow is zero because A>=C.
Equal signed operands add magnitudes; different signs subtract smaller from
larger, using the larger magnitude's sign. Subtraction reverses the operand
sign algebraically. Zero-source subtraction calls operand.negate; the value
proof assumes that delegate is truthful. Cancellation normalizes to zero.

For multiplication, each step is a[i]*c[j]+out[i+j]+carry. With all three
digits/carry in [0,M], it is at most M^2+2M=B^2-1<2^60, safely within Long.
The low digit and outgoing carry stay in [0,M]. A completed row adds
a[i]*B^i*C. Its terminal carry index i+len(C) is untouched by earlier rows,
whose highest terminal index was i+len(C)-1. Assignment cannot lose data.
Induction over rows proves the exact magnitude product, followed by sign
multiplication and normalization. Shared operands are read-only; output is fresh.

If input bit lengths are u,v, the nonzero product has at least u+v-1 bits.
The Long-valued preflight rejects only a provably unsupported product; the
actual normalized high digit resolves the ambiguous upper boundary. This
does not prove allocation feasibility. `parallelMultiply` delegates publicly
to multiply; correctness assumes a truthful delegate, while its callback
strategy is recorded separately from the mathematical product contract.

## Quotient selection and Euclid

All three public division APIs enter `selectStoredDivision`. A zero stored
divisor fails first; if A<D, Q=0 and R=A. For a scalar divisor d<B the
high-to-low invariant is 0<=r<d. The next temporary r*B+a[i]<d*B<=B^2;
quotient digit is <B and the next remainder restores the invariant.

For n>=2, task 530 normalizes D to V=D*2^s with 0<=s<=29 and high digit
h in [B/2,B). Scratch N=A*2^s retains the extra top carry. At each quotient
position the n+1-digit window U satisfies 0<=U<B*V. For q=floor(U/V),
the capped estimate E=min(floor(topTwo/h),B-1) lies in [q,q+2]. The next-digit
comparison can decrement at most twice, never below q, and leaves E in
{q,q+1}. With n=2 it already determines q exactly. A negative subtraction
after that test needs exactly one decrement/add-back. The resulting window
is in [0,V), restoring the invariant after the next low digit is appended.

The detailed estimate inequalities and borrow induction are retained in
task 530's proof. In particular, the subtraction carry/borrow stays <=E;
E*v+borrow<=E*B<=B^2-B; add-back sums are <=2B-1. The estimate test guards
residual<B before shifting it by 30; the compared values are <B^2. Normalizing
a digit with its disjoint carry uses <B*2^s<=2^59. No Long overflow is hidden
in the quotient corrections. Export denormalizes R exactly, since both N
and V are multiples of 2^s. The selected-output flags omit allocations,
not correction steps or remainder state.

Thus A=Q*D+R, 0<=R<D. For input signs sa,sb, q=sa*sb*Q and r=sa*R give
a=q*b+r and a nonzero remainder of the dividend's sign. Each single-output
wrapper requests the corresponding result; the pair requests both. Scratch
is local and disjoint from the read-only operand arrays, including self aliases.

For nonzero gcd inputs, exact positive base-class magnitude views start
Euclid's (A,D) loop. Replacing (A,D) with (D,R) preserves common divisors,
because R=A-Q*D. Every nonzero second component strictly decreases in the
nonnegative integers. The loop terminates at (G,0) with G>0; positivity and
preservation establish the greatest common divisor. Zero branches instead
return the appropriate public abs result, which must be truthful for this
argument. gcd(0,0)=0. Intermediate magnitudes never exceed original inputs.

## Powers and error stages

The current source checks a negative exponent first, then exponent zero,
equals(ONE), then zero source or exponent one. These give mathematical errors
or identity values for truthful inputs; no singleton/receiver identity is
required by these new tests. For other inputs, truthful abs/lowest-set-bit/
shiftRight/bitLength calls give |a|=u*2^t with u odd and t>=0. The result is
sign(a)^e*u^e*2^(t*e). Computing t*e and bit estimates in Long is safe for
nonnegative Int metadata and exponent: their product is below 2^62.

u=1 uses a signed unit followed by the final shift. For odd u>1 of b bits,
u>2^(b-1), so (b-1)*e>=Int.MAX_VALUE proves an out-of-domain result. It is
a lower-bound rejection, not an arbitrary smaller supported range.

On the compact path b*e<=64, earlier identities ensure e>=2, hence b<=32.
The leading unsigned 32-bit word is the whole truthful u. The special
factor==2 branch cannot occur for truthful odd u. Repeated squaring maintains
answer*factor^remaining=u^e. At each used multiplication the mathematical
power exponent is at most e. All unsigned results are therefore <2^64.
Long wrapping retains those exact low 64 bits; conversion to ULong and
30-bit packing recovers the nonnegative value even above Long.MAX_VALUE.
When b*e+t*e<=64, the same bound licenses the compact shift. Otherwise packing
occurs before the checked arbitrary-precision shift. The 2^32-1 square literal
specifically exercises the unsigned half of the compact range.

On the general path, `result*base^remaining=u^e` is preserved by multiplying
for an odd exponent bit, halving the exponent, and squaring for the next bit.
The nonnegative Int exponent strictly shrinks. Working values are exact base
objects, so source multiply overrides cannot affect those internal products.
Their exponents never exceed e, bounding magnitude by u^e. The final sign and
power-of-two shift reconstruct a^e; `shiftPower` checks actual stored bits.
Near-limit allocation/error feasibility remains task 548, without narrowing
the mathematical domain. The new Int.MAX_VALUE exponents use only ±1.

## Floor square roots

For N>0 with b magnitude bits, x0=2^ceil(b/2) exceeds r=floor(sqrt(N)).
For x>=r, x^2+r^2>=2xr and N>=r^2 give floor(N/x)>=2r-x. Thus
y=floor((x+floor(N/x))/2)>=r. If x>r, N<(r+1)^2<=x^2, giving
floor(N/x)<=x-1 and y<x. At x=r, floor(N/x)>=r, so y>=x. Strict descent
therefore terminates precisely at r, including possible Newton oscillation
at a square neighbor. The implementation stops before taking the upward step.

The private routine uses stored magnitude metadata and base working values.
It rejects negative input, handles zero and one before division, and requests
only the quotient in Newton's update. The pair API computes R=N-r^2, hence
0<=R<2r+1 and N=r^2+R. `NUMERICAL_ARGUMENTS.md` bounds the Newton sum by
2*x0+1 and the intermediate bit lengths; task 530 supplies the current
division argument. Neither root API needs to delegate to its public sibling.

## New evidence and what it does not claim

The prior corpus remains intact: 396 signed pairs through 16,384-bit inputs
and 32,768-bit products; 3,216 division, 1,602 gcd, 6,677 pow and 978 sqrt
outcomes, plus existing subclass/null fixtures and task 530's stronger tests.
These are inherited inventories, not newly reproduced execution evidence.

Eight new common methods target arithmetic literals and composition. For
P=16^k, expected strings encode P-1, P+1, (P-1)^2=P^2-2P+1 and P^2-1.
Only k=15,30,60 are used. The P^2-1 pipeline has quotient/remainder P-1
when divided by P; gcd with P is one; its floor root is P-1 with residual
2P-2. These expected arithmetic values are independently specified, but the
test still depends on own parsing, representation, equality and formatting.
They are not independent parser/serialization certificates. Power literals
follow 3^20=3486784401, 3^22=31381059609, 3^30=205891132094649 and successive
tripling; no generator or numerical helper was run to produce them.

Four new JVM methods prepare public-JDK comparisons, including 16 bounded
signed composed pipelines. Their execution must be pinned by the parent to
BellSoft Liberica 25.0.4+9; no host runtime was queried here. They assert
mathematical values and explicit caller marker identity. Internal trace,
call-count, receiver/singleton identity, and competing inherited exception
stages are printed as observations only. There is no asserted cross-runtime
trace equality. Throwing or inconsistent metadata overrides are not truthful
numerical witnesses; existing fixtures covering them remain unchanged.

Direct base-typed Kotlin calls must enter the written override; explicit
super calls select the superclass body, whose own virtual calls can still
dispatch. Exact first/last markers are asserted only when the test itself
writes them around that explicit call. The common compare override writes
its two markers around stored comparison. Direct thrown marker identity is
an explicit-language property, unlike an inherited algorithm's choice of
callbacks. Java source compatibility/null injection belongs to existing TCK
work; these Kotlin tests do not simulate Java null by failing at a Kotlin cast.
Legacy/cross-format serialization, modular/bit algorithms and giant allocation
tests are outside this milestone.

No concrete production arithmetic defect was established. The only changes
are the four owned new files. Source inspection, independent review and
hash-preservation checks do not substitute for compilation, platform tests,
runtime diagnostics, broader acceptance, or task 548's remaining range work.

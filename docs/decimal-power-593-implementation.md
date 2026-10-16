# 593 contextual pow: private working-coefficient implementation

SPDX-License-Identifier: Apache-2.0

SOURCE ONLY. All new production/test source is UNCOMPILED / UNRUN. This document
binds the implementation, not a successful campaign. No source250 execution,
foreign numerical implementation, compiler/stdlib body or bytecode was used as
a substitute for the mathematical argument. No runtime lease was acquired.

## Provenance and narrow ownership

The isolated root is `/private/tmp/krogu-math-task-593-97oXaE9D/root`, copied
without Git/build/.gradle/.kotlin from the current7443 publication. Before edits,
all250 original sources matched manifest
`5f99c5c5ee792180ae64b6448c82e034e4d13e21e7fedc1b6875e6c0e8bd92ef`.
The complete no-Git original archive is read-only in `../evidence/`, SHA
`1357cce802b46bb6bae0987a0a4f72a45b6099118d4f5cc732719f036775b1cb`.
Original BD/BI copies are separately preserved read-only.

Accepted paper V1 remains unchanged at
`/private/tmp/krogu-593-working-proposal-A8yotQFR/PROPOSAL.md` (c72ca222…).
Independent full review600a17cd… required R1/R2. The separately frozen V2
`/private/tmp/krogu-593-working-proposal-v2-mxXMGkfV/PROPOSAL.md`, SHA
`d01a4da490765cc28ae7a2ff3122c57dd4230001dcc185f6fb13cead04fdcc19`,
makes those conditions explicit. No paper or old evidence was overwritten.

Only BD's contextual pow adds12 lines after its existing input guards. BI adds
one final stored-state bridge (five lines). The new helper, five common methods,
one JVM method and these new documents are separate files. Removing the12/5-line
insertions exactly recovers the originals. Every old source outside BD/BI,
including580/581, all fixtures, consumers and original assertions, is preserved.
No public Kotlin API, value-instance field, cache, serializer, dependency,
toolchain, standalone script or existing numerical kernel body is changed.

## Selection, recurrence and R1

Let L=2147483647,K=646456992,H=646456993. Reuse the pinned own566/528 logarithmic
certificate; no certificate was run here. Supported magnitudes have at most H
decimal digits;10^K<2^(L−3), while10^H>=2^L. H digits still require an actual
binary-support check.

BD retains p0 virtual exact-pow delegation, exponent validation, n0 identity and
exponent-digit validation before selection. It computes mathematical
W=p.toLong()+digits(abs(n))+1, at most2147483657. W<=K executes the entire old
body, byte-preserved; every required work coefficient then has at most K digits
and is supported under the existing580/581 leaves. Stored zero returns null
from the final BI bridge and also retains the old body/orientation. A failed
sufficient selector is never itself an arithmetic error.

The selected helper uses mathematical Long W, never a public MathContext with
an overflowing precision or a lower private cap. Its accumulator starts at ONE;
`accumulate` performs the exact original bits-high-to-low square and optional
multiply-by-original-base sequence, at most60 products. Original base conversion
is immutable; it is not pre-rounded. Generic direct calls allow its D0>W:
raw digits are bounded by max(2W,W+D0), and every multiply allocates from actual
core sizes in Long. Production W>K gives D0<=H<=W and the simpler2W bound.
This is R1, including later set bits, not just initial ONE*x.

Each committed nonzero state is sign*U*10^Z@S, U positive/not divisible by10,
Z Long>=0, S Int, and digits(U)+Z<=W. `State.digits()` reads the actual core
plus tail; it is used by final publication and internal controls, not a stored
copy of W. Core normalization transfers zeros into Z without changing S or
the conceptual coefficient/cohort. Every subsequent operation treats committed
cores as read-only. The new State/Words/Pages are local scratch representations,
not fields added to BigDecimal or BigInteger.

## Exact convolution, work rounding and scale

`multiply` first retains the existing nonzero LOW logical-scale error. It
convolves core inputs into a new zero-readable paged buffer and adds their
Long zero tails. Each row accumulates A[i]*B[j]+oldCell+carry; the target carry
cell i+m was untouched by earlier rows. With B=1e9 the primitive bound is
(B−1)^2+2(B−1)=B²−1<1e18<Long.MAX; each stored digit/carry is below B.
Aliased read-only square inputs are safe; no output aliases either input.

`roundOwned` normalizes newly trailing zeros, obtains D and mandatory drop
max(D−W,0). If drop<=Z it removes only symbolic zeros, so no inexact callback
or huge tail loop occurs. Otherwise guard/sticky are read from the original
core before quotient movement; drop−Z is positive and less than core digits.
The quotient remains nonzero. Guard5/R0 is tie; guard5 with lower nonzero digits
is above. The existing all-eight-mode sign/parity map is applied only to a
nonzero discarded fraction. Quotient zeros are not extracted before parity
and increment. A carry producing W+1 digits is divided exactly by10 with one
additional mandatory scale drop. `commit` then restores the core/tail invariant.

LOW is not newly rescued. HIGH may be rescued only by mandatory precision/carry
drops; an exact D<=W product retains its scale error. Final `round` copies its
input core and separately rounds at p; its drop is at most W−p<=10. It does
not replace any work round or strip optional zeros to squeeze output into range.
All logical scale sums/drops remain Long until checked Int commitment. The
existing qualified working-scale admission remains, including the previously
discussed negative-unit endpoint. Error category is AE; competing internal
round/scale error message/order on a corrected wide path is not asserted as a
new contract. Every old diagnostic assertion remains active for later review.

## Reciprocal: complete coefficient partition and R2

The pow-specific reciprocal precondition is positive destination p and
2<=W−p<=10, W<=MAX+10. This makes its public relation explicit and supplies the
p<=H =>W<=H+10 bound. It consumes the actual W-rounded denominator, never the
unrounded mathematical x^N. A unit core immediately returns sign*1@(Z−S),
without expanding Z or generating precision-sized quotient zeros.

For p<=H and U>1, U has no zero tail and is not a power of ten. For d=digits(U),
10^(d−1)<U<10^d. The sparse initial remainder10^(d−1) is below U; the first
multiply-by10 gives a significant quotient digit1..9. Each later step keeps
0<=R<U, forms10R<10U, chooses the largest q0..9 with qU<=10R and subtracts.
Trial/remainder have divisor core cells+1. There is no d-step leading-zero scan,
whole decimal String or public normalized power/product.

The first W significant digits enter the quotient. R0 returns the natural exact
state only within those kept digits, with E=−S+t−1+d+Z. On the(W+1)th guard,
R0 instead means sticky=false; a nonzero guard still requires work rounding.
The separate guard branch implements R2 and preserves UNNECESSARY. Inexact
scale is−S+W−1+d+Z, minus a carry drop. Terminating reciprocal coefficients
have no trailing zero and their natural E>=preferred−S, so this natural pair
is closest to preferred without padding. Exact/guard/scale checks remain distinct.

For p>H, any nonterminating reciprocal or finite result needing p-digit rounding
would have more than H required digits. A finite unsupported exact coefficient
also cannot be rescued by rounding to p>H. `exactReciprocal` therefore factors
a copy of U, using actual2/5 divisibility;10 does not divide U, so at most one
factor is present. Chunk2^29 and5^12 plus residual single-factor passes have
remainders<divisor<B and `remainder*B+cell<1e18`. A residual other than1 proves
nontermination and necessarily unsupported destination, not an accepted value.
U3 is decided with tiny work, not W billion quotient iterations.

For U=2^a*5^b,m=max(a,b), the natural exact result is
C=2^(m−a)*5^(m−b), E=Z+m−S. C has no decimal zeros; E>=−S. Factor counts are
Long (<4*digits(U)); when a count reaches L, C is necessarily unsupported even
if a non2/5 residual would later be found. Otherwise narrowing the exponent is
safe and the unchanged own BI base2/base5 pow leaf enforces actual support.
Supported C has at most H<p<W digits: both work and final rounding keep it
exactly. This is one deliberately early public BI construction: the unchanged
base2/base5 `pow` constructs the **required final coefficient magnitude** C
(the final sign still comes from the state), not an arbitrary work coefficient.
Its range failure is therefore a necessary final-output failure. On success,
no later rounding changes C, E or sign. The final stored BI bridge converts
that own base result to local decimal state without virtual byte/string
callbacks. No denominator magnitude or full decimal expansion is published.
Scale is checked at the retained natural
boundary; no optional clamp or natural-scale rescue is added.

## Bounds, indexing and live storage

Full raw production digits can reach2W=4294967314, beyond Int.MAX. Generic
max(2W,W+D0) has the same global envelope because D0<=H<MAX+10. Decimal cells
fit2*ceil((MAX+10)/9)+1=477218593; `cellCount` checks that proved bound before
Int narrowing. Actual capacities depend on core sizes. Core/tail compression
never reserves conceptual zero-tail storage merely to satisfy that envelope.
The convolution's later Int a.size+b.size is safe only after this Long capacity
check proves the sum below477218593.

All digit, zero, scale, position and drop arithmetic promotes to Long before
multiplication. Digit access divides Long by9 before narrowing. Whole-cell drop
is inside active size; the remaining divisor is a literal10^0..10^8. Quotient
movement is low-to-high, small division high-to-low. Core trim reads only active
cells; trial construction overwrites each new active cell and resets size, so
old higher pages are never observed. Unwritten valid pages read zero. Page start
uses Long, and the last page allocates only its remaining capacity.

For the small-destination reciprocal, quotient cells cover W digits+guard+
carry; R and trial cover U cells+1. Its at-most W+1 significant steps are bounded
by the requested output precision, not irrelevant leading decimal zeros. Sparse
monomials take neither those loops nor dense factor passes. Dense factoring,
convolution and conversions can still be expensive/quadratic and enormous.

The general `publish` codec is read-only and runs after destination rounding;
the certified p>H exact magnitude construction above is the explicit earlier
exception, already identical to the required final magnitude on success.
Thus the codec's “only publication” comment must not be read as denying that
certified base2/base5 call. No oversized discretionary working value is forced
through that early call. D>H rejects before any zero expansion. Otherwise the
general codec multiplies only the core by the0..8
residual zero count, then virtually reads whole base1e9 zero cells. A bounded
binary scratch receives each base1e9 cell; each primitive is below2^60. Actual
bit length is checked after every nonzero-prefix stage; H-digit uncertainty
is never mistaken for guaranteed support. Final bytes use Long bits/8 and
existing supported-byte bounds. No optional cohort stripping changes the
required final coefficient. Huge positive monomials are required-range errors,
not successful representable controls.

Original base/current/product can coexist during accumulation; reciprocal can
retain the old denominator, mutable factor or remainder/trial/quotient scratch;
finalization can retain the work state, copied rounded core, binary scratch,
bytes and final BI. Conversion from an exact own BI may temporarily retain both
binary and decimal forms. No claim is made that all these buffers fit a fixed
heap, that GC releases lexical locals early, or that a bound for one array is
the peak live allocation. Inputs are never mutated or retained beyond local
states, and no cache or background work exists. No Error/OOM/timeout is caught.

## Caller/subtype reachability

Current pow starts at the base-class ONE. Its input is an argument to base
accumulator multiply; the input's BD.multiply override is not the receiver.
BI65–79 reads stored operand fields and returns an own normalized product;
580 likewise returns an own coefficient. Later accumulator receivers are own
base objects. The wide bridge makes this stored-value substitution explicit
without an exact-class gate, new metadata probes or speculative replay.
Current-own-format restored truthful coefficient subtypes remain reachable and
covered conditionally by their stored mathematical values; public constructor
normalization does not justify excluding them after restoration.

Outer pow overrides, explicit super, p0 virtual delegation, caller-returned
identity and caller-thrown exception identity are untouched. Existing
`DecimalPowWorkingPrecisionContractTest.explicitCallerDispatchSuperAndExceptionIdentityRemainAvailable`
is unchanged. A new bounded JVM method restores only the current own format,
asserts replacement identity solely to establish setup reachability, then
checks the mathematical sparse reciprocal alongside an ordinary control.
There are no inherited callback-count/order/noncall assertions, foreign/legacy
streams or serializer changes. False/replacing/throwing diagnostic classes are
not blanket waived; old fixtures/assertions remain and any conflict requires
individual later541/526 classification.

## Independent prepared tests and exact limits

Five common methods and one JVM method are UNCOMPILED/UNRUN:

- The6144-case generic grid uses eight fixed magnitudes, scales0/3, both signs,
  exponents±1/±2/±3, p1..4 and eight modes. Its independent Long model rounds
  integer N/D using quotient/remainder and2r; it does not reproduce production
  cells, zero tails or quotient streaming. W3..6, raw products<10^12,
  normalized reciprocal numerator<=10^11, denominator<10^6 and2r<2*10^6.
  Explicit overflow guards precede multiplication. Model AE is caught only as
  expected numerical AE; assertion/error/fatal exceptions are not swallowed.
-1581/187 literals distinguish work rounding from direct destination rounding;
  n3/UP1581 distinguishes retaining the original base. A separate actual
  three-cell original-base product uses998*(10^18−1)=997999999999999999002,
  yielding work997@−51 and destination9@−53 without a Long product oracle.
  Original states are checked unchanged after accumulation.
- The±U32/W3 control observes312/313@4 for all modes and work UNNECESSARY AE;
  ±U8 observes exact125@3 including UNNECESSARY, then the separate destination
  round. Real internal phase methods and the actual publisher observe only
  these bounded supported work states, never a test-only/public debug API.
- The tiny1000@1,N999999999,pMAX family observes real work digits2147483657
  and scale147483658 before reciprocal, then requires±1@1999999998. This
  distinguishes mathematical W from a MAX cap; final public unit pairs alone
  do not. All cores stay1 and work is bounded by60 small products. Actual raw
  digit counts can exceed MAX while storage remains tiny. Its positive-N
  public calls must reject unsupported required coefficients. The current
  old source's earlier square-prefix249999998 obstruction is source-derived,
  not an observed old execution. Dense n2/H witnesses remain symbolic UNRUN.
- Additional pMAX literals exercise both chunk factor paths, exact reciprocals
  and nontermination3/6/15. In particular1/2^30=5^30@30 with independent
  decimal literal931322574615478515625, and1/5^13=8192@13. JVM current-format
  truthful-coefficient controls use the same small sparse public family.

These are prepared proofs/tests, not method executions or an all-input test
claim. Independent code/proof/model review, later compilation, fresh JVM/JS/
Wasm/macOS ARM suites, full retained ledgers and matching published-head gates
remain open. General physical capacity548, qualified scale endpoints,541/526
diagnostics,557 compiler barrier and broader542 accuracy remain distinct.
The source preserves the prescribed recurrence under the accepted leaves;
it neither invents a universal two-ulp theorem nor creates a new accuracy gate.

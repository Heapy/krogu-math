# Supported midpoint comparison for decimal rounding (584)

SPDX-License-Identifier: Apache-2.0

SOURCE ONLY, UNCOMPILED / UNRUN. Parent grant2363 authorizes this private
candidate at archive da6086342165711ac800e0c5c515eb2e6df7aae5 plus the frozen
233-source582 successor. No compiler, script, helper, numerical/model operation,
probe, test, build, process inspection, Git or tracker operation was executed.
This is a source argument and prepared finite coverage, not runtime acceptance.

## Narrow implementation boundary

Only the midpoint expression in BigDecimal.roundedDivide changes. The previous
remainder-zero return, numerator/divisor sign calls, mode selection, quotient
parity read, signed increment and return remain byte-identical. No BigInteger,
581/582 body, public declaration, field/cache or serialization mechanism changes.
Before editing, original BigDecimal SHA-256
`00e392444ce4b13122ec6dcb7ccbee357b437da1e6554cc78876e909346c1599`
was copied into unique `../originals-Gjr7NwEx/`, with a hash manifest; both files
are read-only and their directory is not writable. The inherited source-check
log is parent preparation evidence, not a newly executed build or numerical run.

## All valid residual magnitudes

Assume completing correct integer division yields signed q and r with
n=q*d+r, |r|<|d|, and r zero or of n's sign. The existing early return handles
r=0 without midpoint work. Otherwise set R=|r| and D=|d|, so 0<R<D. Then

    2R < D  iff R < D-R,
    2R = D  iff R = D-R,
    2R > D  iff R > D-R.

The implementation obtains the same absolute magnitudes as before, and compares
R with D.subtract(R). Both R and D-R are positive and strictly below supported
D. Thus their stored magnitude bit counts are at most L=Int.MAX_VALUE, even
when 2R requires L+1 bits. BigInteger's existing positive subtraction chooses
subtractMagnitude(D.words,R.words), allocating D's word count and subtracting
with a bounded borrow. It does not allocate a public D+R or D*2. The maximum
input word count ceil(L/30)=71582789 fits Int; this same-length scratch, loop
indices and normalization do not introduce an indexing overflow or a smaller
domain cap. Inputs are not written by this subtraction. Allocation completion
remains an ordinary resource premise, not a guaranteed bounded-heap outcome.

The comparison sign, including an exact tie, equals the old mathematical
comparison. All eight increment rules therefore retain their previous meaning:
DOWN truncates; UP increments away from zero; CEILING/FLOOR use the quotient
sign; HALF_UP/HALF_DOWN distinguish ties; HALF_EVEN also uses q's parity;
UNNECESSARY throws for the already-known nonzero remainder. The r=0 path stays
exact for every mode. A required rounded coefficient outside the public range
may still correctly fail in the unchanged signed add. The selected result scale
and cohort belong to the unchanged caller; no extra stripping or scale rescue
is introduced here.

## Former supported-output obstruction

Let L=2147483647, A=2^L-2 and B=2^L-1, represented as supported positive
coefficients at scale0. Both have L bits; ordinary byte construction is possible
using length268435456, leading0x7f, remaining0xff (A ends0xfe), conditionally on
resources. No unsupported ONE.shiftLeft(L) construction is assumed.
The explicit-scale divide request target0 has shift0. Since A=B-1 and odd B>1
cannot be a decimal power of ten, A and B have equal decimal digit counts.
Its quotient exponent is -1, so the tiny branch requiring less than -1 is not
taken. Integer division returns q=0,r=A; 2A-B=2^L-3>0. The seven rounding modes
other than UNNECESSARY require only0@0 or1@0. The former unconditional doubled
remainder overflowed. The new complement is B-A=1, which is supported and
correctly classifies the result as above halfway. UNNECESSARY legitimately
requires an exception; it is not counted as a false-exception witness.
This giant case is symbolic and UNRUN, not a JDK defect observation or benchmark.

The public [JDK25 explicit-scale divide contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#divide(java.math.BigDecimal,int,java.math.RoundingMode))
requires the chosen rounding at the requested scale. This correction addresses
the dispensable midpoint representation only. Earlier decimal formatting,
normalization, rescaling products, practical allocation/string limits, and other
548 range questions keep their separate premises and obligations.

## Truthful subtypes and caller contracts

The numerical refinement covers ordinary stored coefficients and compositional
truthful leaves: integer divide/remainder, sign, abs, subtract and compare must
return their correct mathematical values. Current-own-format restoration can
make coefficient subtypes reachable; it is not excluded. The complement adds
an internal virtual subtract on the absolute divisor where the prior expression
used a virtual shift on the absolute remainder. Correct leaf substitution gives
the proved result, but this is not a proof that arbitrary throwing/replacement
overrides are equivalent. Such poisoned probes need their exact policy treatment
if existing consumers expose a difference; no fixture is removed or waived.
Explicit caller override/super behavior and directly thrown exceptions retain
their language contracts. Internal abs/shift/subtract callback order/counts or
noncalls do not become new public guarantees. Existing current-format suites
remain active; legacy serialization is not introduced.

## Prepared independent finite coverage

The separate common ScaledDivisionMidpoint584Test contains two methods. Its
expected coefficient comes from signed primitive Long division/remainder and
bounded *doubling*, not the production complement identity or any production/JDK
decimal arithmetic. It checks the full coefficient and requested scale; target0
also checks canonical text. Nonzero-remainder UNNECESSARY must throw. Input
coefficient/scale immutability is checked after each mode set.

The first method uses denominators2,3 and 2^30/2^60 with both immediate neighbors;
quotient magnitudes0,1,2; distinct residuals0,1,near/below/at/above halfway,and D-1;
both operand signs; and all eight modes. Even denominators supply actual ties
with both quotient parities; odd ones supply either side without artificial ties.
There are41 distinct denominator/residual pairs, hence41*3*4*8=3936 prepared
public calls. D<=2^60+1, |n|<=3D-1<2^62, and doubled residual<2^62, so primitive
multiplication, negation, division and remainder cannot overflow or encounter
Long.MIN_VALUE/-1.

The second method uses five small rational pairs and nine explicit scale triples,
four sign pairs and all eight modes:1440 prepared calls. Some request scale0
with both operand scales at MIN or MAX; others request MIN/MAX with source and
divisor scales compensating. Exact Long shift=t+sb-sa is always -1,0 or1.
Its model multiplies only the tiny numerator or denominator by10, then uses the
same independent primitive rounding oracle. No huge power, output text, allocated
coefficient, synthetic smaller range cap or callback-noncall assertion is used.
These5376 calls are source-derived inventory, not executed results or an
all-input proof. Existing full-target/current-format suites and independent
source review remain required under separate grants; no whole548 closure follows.

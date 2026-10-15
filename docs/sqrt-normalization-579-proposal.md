# Inexact decimal sqrt normalization: constructive proposal (579)

**SOURCE-ONLY DESIGN / NOT IMPLEMENTED / UNCOMPILED / UNRUN.** This milestone
adds only this proposal. It reads own source/proofs and uses the public contract
already identified in the [542 audit](decimal-power-root-542-current-audit.md).
No foreign numerical implementation source/body/bytecode was read. No script,
compiler, numerical helper, certificate, probe, test, build, benchmark, Git or
tracker command ran. No production, test, fixture or existing document changed.
This is an independently derived arithmetic construction, not code adoption,
execution evidence or a performance claim.

## Domain, preserved entries and repair boundary

This derivation binds the original BigDecimal source
`08b4329ea5e902f1043107d42ddb02592be537497f6b0990927e8c67c4ed3f29`
and historical BigInteger source
`5a4aa675afc9cdfdff6c8761dba45c9636de5533e70fabbd6ea808dc6fa08781`.
The parallel format559 successor BigInteger
`e4dc75b253c689b0f3c634f02f9e491c9a1dcfae396a11135be4427c69ce7b68`
is a separate source-only radix-formatting change. This proposal's helper
derivation remains bound to historical5a4, not a claim that5a4 is the current
publication source. BigDecimal's full pin above is unchanged. This binding
correction changes neither the proposed arithmetic nor its execution status.
At `BigDecimal.kt:552–582`, retain the existing negative/zero handling,
finite-exact-root563 path, exact/UNNECESSARY fallback and stored-zero subtype
path. This proposal addresses ordinary positive irrational values with fixed
valid precision p>0 and one of the seven rounding modes. Finite roots keep the
existing cheap path even when p is Int.MAX_VALUE.

Write x=C*10^-S, C>0 canonical and S an Int. Let D be C's exact decimal digit
count, E=D-S-1, R=floor(E/2), epsilon=E-2R in{0,1}, and preferred=S/2 with
truncation toward zero. The desired grid scale is **Long** t=p-1-R. The logical
scaled radicand is C*10^h with

`h=2t-S=2p-D-1+epsilon`.

This identity controls every sign/parity case, including negative odd S.
All these expressions use Long. With p<=Int.MAX_VALUE, D<=646456993 and Int S,
they are far below Long overflow. Do not apply checkedScale(t) before the new
fallback: a final equivalent cohort can remove zero digits and rescue a large t.
Do not cap p or reject from a provisional root's public BigInteger bit length.

There are three ordinary normalization classes:

1. **h<=0:** preserve the current supported numerator/denominator path, justified
   below, and565's quotient/remainder midpoint logic.
2. **h>0 and the old materialized numerator is proved supported:** preserve the
   existing fast path. For example, b=stored bit length(C) and b+4h<=L,
   L=Int.MAX_VALUE, is a sufficient conservative test because10<16; require also
   the existing ten-power ceiling and an Int t. This is eligibility for the old
   path, never a rejection predicate. Failure selects the raw fallback, even
   when the sufficient bound merely missed a supported product.
3. **Other h>0:** stream the logical decimal radicand into a private restoring
   root/remainder computation. Neither the radicand nor its wide root is wrapped
   as a public BigInteger. Round and strip in raw storage before final checks.

For ordinary h<=0, k=-h=D+1-epsilon-2p<=D-1. Thus10^k<=10^(D-1)<=C and
k<=646456992; its divisor is supported. Numerator C is the supported input,
and the current both-output division gives q<=C and remainder<10^k. For h=0,
the divisor is one. Since2p<=D+1 when h<=0, working p and t are also small enough
for the existing Int-scale path: p<=323228497,
-1396970320<=t<=1396970320. Therefore the current input-sized construction
does not conceal the new positive-shift obstruction. Its kernel/allocation
contracts remain conditional as before.

## Raw representation and decimal input without a giant String

Use private little-endian **base B=10^9 Int limbs**, with an explicit active
length and no high zero active limb. This is scratch, not BigInteger's binary
representation. Logical sizes and bit lengths are Long; never feed these raw
arrays to a public magnitude wrapper or the existing Int bit-count helper.
All scratch belongs to one call and never mutates/aliases/escapes into C.

An implementation can place this kernel inside BigInteger's own private source
boundary, where stored sign/words are available without virtual metadata calls.
Build a decimal stream from C numerically:

* Clone its canonical base2^30 words once. Repeated high-to-low division by B
  emits least-significant decimal chunks; trim the working active length after
  each pass. The input remains read-only.
* For each binary word w, with0<=remainder<B, the intermediate
  remainder*2^30+w is below B*2^30<2^60. Quotient and remainder fit their
  respective limbs. This is the same small-divisor bound used by528, not a new
  dependency on public division or formatting callbacks.
* Store the chunks, then read highest first. The highest chunk uses its actual
  one-to-nine digits; every lower chunk supplies exactly nine digits, including
  leading zeros. Primitive division/remainder and the nine literal decimal
  powers suffice; no coefficient-sized String is built. Exact D is recovered
  as9*(chunkCount-1)+topChunkDigits in Long, checked into Int.
* After those D digits, a logical reader emits exactly h zero digits without
  constructing a suffix array, String or10^h. A pair may straddle the last
  coefficient digit and the first logical zero; the reader treats them uniformly.

Input chunk count is ceil(D/9), at most the supported input's digit count and
well within Int. The binary clone and decimal input chunks are input-sized
additional storage. They are separate from the root-sized remainder/trial
bounds below. Repeated decimal extraction may be expensive; no timing claim
follows from this design.

## Restoring root: exact invariant and candidate selection

For the processed decimal prefix A, maintain raw a,r such that

`A=a^2+r, a=floor(sqrt(A)), 0<=r<2a+1`.

Initially A=a=r=0. If total digit count T=D+h is odd, consume one first digit;
otherwise consume a first pair. Thereafter consume pairs g in0..99. Treat a
single first digit as a pair with a leading zero. For every step form raw

`U=100r+g` and `T_d=(20a+d)*d`, for decimal candidates d in0..9.

Choose the largest d with T_d<=U. A bounded descending scan or binary search
over those ten digits is sufficient: T_d is increasing. Then set

`a_new=10a+d`, `r_new=U-T_d`.

Indeed A_new=100A+g=(10a+d)^2+r_new. Also
100a²<=A_new<100(a+1)², so its floor root lies in10a..10a+9. The selected
largest d is exactly that floor. For d<9, failure of d+1 gives
r_new<2a_new+1; for d=9 the original strict upper square gives the same bound.
Thus induction proves the invariant through the entire logical radicand.
No radicand prefix A or square a² is actually stored.

At the end N=C*10^h=a²+r exactly. Since
T=D+h=2p-1+epsilon, the number of root digits/pair steps is exactly p, and a has
exactly p decimal digits. This also follows from R's adjusted-exponent definition.
All nonzero ordinary radicands have a nonzero first root digit.

## Scratch and primitive bounds over all Int precisions

For integer invariant r<=2a,

`U<=200a+99`, `T_d<=180a+81`, `a_new<=10a+9`.

Hence root has at most p digits, or p+1 after final rounding carry; remainder
and every trial/intermediate fit **p+3 decimal digits**. A safe per-buffer
capacity bound in base10^9 cells is

`capacity=ceil((p+3)/9)+1 = ((p.toLong()+11)/9)+1`.

At Int.MAX_VALUE precision it is at most238609296 cells, below Int.MAX_VALUE.
The scratch arithmetic needs a constant number of additional limbs beyond the
**provisional** p-digit root. It is deliberately not restricted to a constant
number beyond the PUBLIC BigInteger range: the provisional root can exceed
that range while its final stripped coefficient fits. This distinction closes
the compressed-cohort gap without a new precision cap.

Do not allocate a single maximum-size buffer eagerly. A private fixed-page
buffer is one constructive storage option: for example8192-limb IntArray pages
created as needed, with active-length/index arithmetic checked in Long before
an Int page/cell access. Its page padding is at most8191 cells per buffer, a
constant independent of p; the page table is also Int-indexable. This avoids
requiring an individual gigabyte array, but does not promise sufficient total
memory. A bounded geometric implementation is another option only if its
additional capacity/copy lifetime is included honestly in the allocation proof.

Raw multiply-by100 with an incoming pair g uses
v=100*limb+carry, initially carry=g<100 and subsequently carry<100. Thus
v<100B<10^11<Long.MAX_VALUE. The operations10a+d,20a+d and subsequent
multiply-by-d have similarly bounded primitive carries (multipliers at most100).
Raw addition/increment propagates a carry at most one. Nonnegative subtraction
uses borrow0/1 and per-cell differences in[-B,B-1], within Int; compare by active
length then high-to-low limbs. Do not compare spare capacity as active value.
Leading-zero trimming preserves the number. Each trial is read-only relative to
a and r; use distinct local trial/remainder destinations or a proved lifetime
schedule before overwriting either input.

T<=2*Int.MAX_VALUE is Long, as are digit-reader positions, suffix length h and
2*processedPairs. p pair steps fit an Int count, but the loop must not increment
past its terminal count. No Int expression2*p, p+3,9*chunkCount or t adjustment
is allowed to overflow before conversion. Final proof must bind each actual
index, capacity and page allocation to these bounds; this document does not
certify implementation indices that have not yet been written.

## Exact rounding without an oversized midpoint

For integer N=a²+r the scaled real root is exact iff r=0. Otherwise its position
relative to a+1/2 is the sign of r-a-1/4. Because r and a are integers:

* r<=a is strictly below the midpoint, including equality r=a;
* r>a is strictly above it;
* a genuine tie is impossible for an integer radicand.

Thus the raw comparison r versus a supplies exact half classification without
forming4N, a midpoint square or an out-of-range public wrapper. For the positive
root, UP/CEILING increment, DOWN/FLOOR retain, and the half modes increment
exactly when r>a. If r=0, every mode retains. The kernel can return exactness and
the strict half sign to the existing rounding policy, rather than duplicate the
mode table. UNNECESSARY is not an ordinary entry to this fallback; should the
kernel be reused, it must reject r!=0 under that mode. Public genuine sqrt ties
already have finite roots and remain on563's earlier path.

Apply any increment in raw storage. Track whether all streamed root digits
were nine; increment then causes a p+1-digit value exactly10^p only in that case.
Divide it by ten exactly and decrement t in Long once, reproducing the original
destination precision normalization. The raw capacity bound includes this carry.
No second inexact rounding is performed.

## Final cohort normalization precedes public range checks

Let b and u be the rounded raw coefficient and its Long scale after the possible
exact carry normalization. b>0 and its precision is at most p. While u>preferred
and b is divisible by ten, divide raw b by ten exactly and decrement u. This is
the current stripTo(preferred) rule. Divisibility may be tested by a read-only
small-divisor pass, followed by in-place division only for zero remainder;
chunks of nine zeros are an optional, separately proved optimization.

This step is crucial: do not throw from p, a's provisional bit length, initial t
or a temporary raw carry. It can compress a long zero tail and bring u back into
Int. All scale arithmetic remains Long until this normalization is complete.

There is no missed supported equivalent cohort when final validation rejects:

1. If final u<=preferred, the coefficient is automatically small enough. Its
   numerical approximation differs from sqrt(x) by less than10^-t (nearest
   modes have the stronger bound). Since u never increases, its coefficient
   error is at most one. Also2*preferred-S is -1,0 or1. Thus
   b<=sqrt(10C)+1, including a result that started below preferred. With the
   supported D bound, b has at most323228498 decimal digits, comfortably inside
   the public bit range using10<16. A final coefficient-range rejection in this
   class would indicate an implementation/proof mismatch.
2. If final u>preferred, stripping stopped only because b is not divisible by
   ten. Every exactly equivalent lower-scale integer-coefficient cohort would
   require that divisibility. None exists. Higher-scale cohorts multiply b by
   powers of ten and increase u; they cannot rescue an excessive coefficient
   or a scale above Int.MAX_VALUE. The working scale is bounded below by
   -1396970320 before the one carry decrement; preferred is at least
   -1073741824. Therefore normalization cannot produce a scale below Int.MIN_VALUE.

Validate final u against Int and final coefficient against the supported binary
magnitude range only now. Resource exhaustion remains a resource failure; do not
catch it and invent a numerical range rejection or successful test skip.

Convert finalized raw decimal b to canonical binary base2^30 words without a
String or public intermediate: traverse decimal chunks high-to-low, repeatedly
multiply the private binary prefix by10^9 and add the next chunk. For each binary
cell, limb*10^9+carry<10^9*2^30<2^60. Every prefix is nonnegative and no larger
than the final coefficient. If a prefix first exceeds the public L-bit limit,
the final coefficient is definitively unsupported; this check happens after
all required cohort stripping. At that crossing, temporary binary storage needs
at most L+30 bits, a constant extra limb budget. Otherwise normalize the final
binary words and only then construct the public BigInteger and BigDecimal.
No public BigInteger with an unsupported magnitude is ever created.

## Dispatch boundary, implementation gates and disposition

The public decimal prefix must retain its existing signum and precision call
sites/order, exact-root detection and error propagation. Capture the original
reported exponent once; do not call virtual precision/scale/unscaledValue again
to feed the raw kernel. Recover actual stored D from numeric chunks. Ordinary
truthful D matches that exponent's precision. A future implementation must keep
misleading/throwing metadata and replacement arithmetic cases on their separately
classified541/526 routes; this proof does not silently promise stored-value
arithmetic for all poisoned objects or use a dynamic-class exclusion as a
mathematical proof. Constructors normally normalize coefficients, but current
own-format restored coefficient subtypes remain reachable.

The raw arithmetic proof applies to truthful stored-value-consistent subtypes
as well as base integers. It does not prove old internal coefficient callback
counts or exception precedence. Preserving the public prefix is a source
requirement, while any changed inherited numerical/callback observation needs
its own exact input/field classification before adoption. Explicit caller
override and super calls retain their language meaning; no new poisoned-getter
noncall assertion is proposed.

The earlier provisional-root/public-cap gap is closed **mathematically** by the
larger private workspace plus rounding/stripping before checks. There is no
remaining mathematical requirement here to reject p>646456993 or to allocate
the logical2p-digit radicand. The400000000-digit sqrt(2) witness is included in
the constructive positive-shift class. The design also covers all Int precisions,
odd scales, carries and compressed final cohorts without assuming those outcomes
cannot occur.

Concrete gates still open: actual raw/page/index/lifetime implementation review;
the exact541 callback/fixture seam; compilation and independently derived bounded
tests for pair boundaries, nonzero tails, all modes, carry and cohort rescue;
current negative-shift/finite-root regressions; fresh matching-artifact platform
and public-test execution under a later root scope. Maximum-domain memory/time
remains548, and no speedup, all-platform feasibility, task579 completion or future
PASS count is claimed. Root owns the next implementation scope and independent
review. No runtime window is held or awaited by this source-only designer.

# Range-safe contextual multiplication: constructive proposal (580)

**SOURCE/PROOF DESIGN ONLY / NOT IMPLEMENTED / UNCOMPILED / UNRUN.** This finite
proposal adds only this file under `/private/tmp`. No root or worker source,
test, fixture, document, frozen579 artifact, Git state or tracker changed. No
compiler, script, numerical helper, certificate, probe, test, build, benchmark
or runtime operation ran. Only own source/proof text and metadata were read;
no foreign numerical implementation source/body/bytecode was consulted.

The proposal repairs the ordinary supported-result obstruction established by
`docs/decimal-context-product-580-audit.md` (original verbatim audit SHA
`9b03988a29d491df520c54d9c07a794836137a0a2f511e054a0ee9460e77df81`).
Own publication source binding is BigDecimal
`08b4329ea5e902f1043107d42ddb02592be537497f6b0990927e8c67c4ed3f29`,
BigInteger `e4dc75b253c689b0f3c634f02f9e491c9a1dcfae396a11135be4427c69ce7b68`,
and MagnitudeLimits
`5bf687bb968d22908a36b8898f19649bdbdf7e5a2596575f1808053fca0e4769`.
The isolated579 raw-helper source read for representation ideas remains frozen
`ce779a45a568a49f54c631359c6e13314ee8d05272c6d915dbc12bf7bc7be408`.
This is independent multiplication design, not adoption or modification of579.

## Required output representation and retained policy

For ordinary immutable operands write x=A*10^-s, y=B*10^-t, S=s.toLong()+t,
p=context.precision, sigma=sign(A)*sign(B), M=abs(A)*abs(B). All input scales are
Int; S lies in[-4294967296,4294967294]. This proposal concerns nonzero operands,
p>0, and the existing multiply/roundValue cohort policy. It does not change
public signatures or add a precision limit.

Preserve these branches exactly before selecting any raw arithmetic:

* p=0 keeps the current virtual exact multiply delegation. Its coefficient,
  scale, exception and explicit override behavior is outside this fallback.
* Preserve the existing short-circuit condition at BigDecimal:140, including
  which coefficient signum calls are reached. Its zero/source orientations
  continue through the old branches. Do not replace them by a symmetric new
  zero shortcut. In particular receiver-zero saturation and the retained
  nonzero-receiver/zero-right rejection outside Int scale stay as recorded566.
* For the ordinary low/normal-scale branch, retain the existing multiplyValue
  scaleForValue evaluation and its left coefficient signum call before the
  exact-product-or-raw selection. With nonzero operands S<Int.MIN_VALUE rejects
  before product work, as it does now. Zero cases stay on the old route.
* For S>Int.MAX_VALUE with ordinary nonzero operands, preserve566: only mandatory
  precision reduction/carry can rescue scale. If D=digits(M)<=p, reject the
  HIGH logical scale even when another numerically equal cohort exists. If
  D>p, do the required rounding, then validate S minus its required drop/carry.

The desired coefficient is obtained by dropping k=max(D-p,0) decimal positions,
rounding once, then dropping one additional exact position only if rounding
created p+1 digits. The desired scale is S minus those drops. **Do not strip
other trailing zeros.** For example a rounded coefficient100 at precision3 is
not automatically replaced by1 at a lower scale. That would change the closest
preferred representation and the existing566 policy. Parent explicitly confirmed
this boundary for the580 design.

Thus “supported result” here means the required coefficient/scale representation,
not the existence of any numerically equivalent cohort. Rejection of an excessive
required coefficient is not a claim that no equivalent lower-scale coefficient
exists. Optional zero stripping, wider exact integer support and changed HIGH
scale policy are separate policy changes, not hidden parts of this repair.

## Selection guard, including the uncertain bit-sum edge

For two nonzero canonical stored magnitudes let a=bits(abs(A)), b=bits(abs(B)),
L=Int.MAX_VALUE and u=a.toLong()+b. Then

`2^(u-2) <= M < 2^u`, so `bits(M)` is in {u-1,u}.

* u<=L proves the exact product supported. Preserve the existing coefficient
  multiplication and rounding path here, including the existing566 scale path.
* u>=L+2 proves the exact product unsupported; select raw arithmetic.
* **u=L+1 is uncertain:** the actual product can have L or L+1 bits. Select raw
  arithmetic for this whole edge too, rather than allowing a public product
  constructor to throw before contextual rounding. A supported exact product
  at this edge is handled numerically/cohort-equivalently by the raw route.

Therefore the sufficient complete selection is **u>L**, after the retained
scale/zero prefix. No public product, exception catching, subclass exception
inspection or speculative large allocation is needed to choose it. A future
internal helper inside BigInteger's own private boundary can read both stored
signs, words and exact bit counts. It must not call public bitLength, abs,
precision, scale or unscaledValue to make this decision. Base decimal precision
is irrelevant to multiplying its stored values. There is no p threshold/cap:
even p=Int.MAX_VALUE is admitted, with final required representation checked.

On u<=L, a supported product has at most646456993 decimal digits. Its possible
drop D-p is at most646456992, within the existing decimal power range. Thus the
old supported-coefficient rounding leaf has no new unsupported product or drop
power caused by this selection. Existing leaf allocation/scale/subtype conditions
remain explicit; this selection does not purport to settle unrelated leaves.

## Private input conversion and exact full product

A full private product is an intentionally simple complete fallback; it makes
no performance or heap claim. Use little-endian decimal limbs base B0=10^9,
with canonical active length. Keep these private throughout multiplication and
rounding; they do not obey the public L-bit magnitude limit and must never be
wrapped prematurely as BigInteger or passed to an Int bit-length helper.

Inside BigInteger's private boundary, read its canonical magnitude words in
base R=2^30 without exposing an alias. Use one owned binary scratch buffer,
filled with a copy of A's magnitude, then reused with a copy of B's magnitude.
Repeated high-to-low division by B0 emits decimal remainder limbs. Each primitive
part is remainder*R+word < B0*R < 2^60. Quotient limbs fit R, decimal remainders
fit B0, and trimming the binary active length is exact. Decimal input buffers
are independent. Never mutate either original magnitude or cache converted
operands in instances. No coefficient-sized String, public huge divisor or
public wide product is needed.

Each input has at most Dmax=646456993 decimal digits under the accepted528 bound,
so at most ceil(Dmax/9)=71828555 decimal limbs. Binary scratch has at most
ceil(L/30)=71582789 active limbs. Both signs are stored separately; negative
inputs require no public negation or sign-extension allocation.

Let decimal limb lengths be n and m. Allocate a zero-initialized paged product
with logical capacity n+m (computed in Long before narrowing), at most143657110
cells. Use schoolbook rows: for i=0..n-1, carry=0; for j=0..m-1, form

`z=A[i]*B[j]+product[i+j]+carry`,
`product[i+j]=z % B0`, `carry=z / B0`.

Then store carry at i+m. That destination was not written by previous rows,
whose greatest index is i+m-1; its initial zero is correct. Earlier partial
contributions in the inner-loop cells are included by the z term. Induction
over rows is the usual positional distributive identity, proving the final
private array equals M exactly. All terms are nonnegative. With limb and carry
at most B0-1,

`z <= (B0-1)^2+2(B0-1)=B0^2-1 < 10^18 < Long.MAX_VALUE`.

Thus carry<B0 remains invariant. No sum over all rows accumulates in a primitive;
each cell is reduced immediately. Trim high zeros after all rows. Since operands
are nonzero, the result is nonzero. Its exact D from the high chunk's1..9 digits
and 9*(activeLength-1) is at most2*Dmax=1292913986, still Int. No result-scale or
public magnitude validation occurs on this provisional full product.

## Exact guard, sticky, quotient and all rounding modes

If D<=p, k=0 and no digit is discarded. No rounding is necessary in any mode,
including UNNECESSARY; retain the entire raw coefficient with required scale S,
subject to the retained HIGH policy and final range checks. This handles all
caller precisions above the full product's digit count without a special cap.

Otherwise k=D-p lies in[1,D-1]. Before mutating the product, capture the discarded
information directly from its decimal limbs:

* guard is digit k-1 from the right: cell=(k-1)/9, offset=(k-1)%9, digit=
  `(cellValue/10^offset)%10`, using nine bounded literal decimal powers;
* sticky is whether any digit strictly below that guard is nonzero: inspect
  complete lower limbs and the current limb modulo10^offset;
* discardedNonzero is guard!=0 or sticky;
* half comparison is negative for guard<5, positive for guard>5, and for guard=5
  is positive if sticky, otherwise zero (a true decimal tie).

These classify r in M=q*10^k+r relative to0 and10^k/2 exactly, without building
10^k or r as an integer. All accesses lie within the product's active cells;
offset is0..8, with modulo1 giving an empty partial tail when offset=0.

Build q in the same product storage: shift away w=k/9 whole low cells using
**low-to-high** copies from old index i+w to i; then shorten active length.
Low-to-high is essential to avoid overwriting unread sources. Divide the
remaining array high-to-low in place by10^v, v=k%9. For v=0 this is an exact
division by1 (or no work). For v>0, remainder<10^v<=10^8 and the primitive part
remainder*B0+cell<10^17; quotient cells<B0. Trim high zeros. This is exactly
floor(M/10^k), is positive, and has p decimal digits. The original remainder
itself need not survive because all rounding decisions were captured first.

If discardedNonzero is false, keep q in every mode. Otherwise apply the same
mathematical policy as current increment(mode,sigma,half,qOdd):

| Mode | Increment magnitude q |
|---|---|
| UP | yes |
| DOWN | no |
| CEILING | iff sigma>0 |
| FLOOR | iff sigma<0 |
| HALF_UP | iff half>=0 |
| HALF_DOWN | iff half>0 |
| HALF_EVEN | iff half>0 or (half=0 and q odd) |
| UNNECESSARY | ArithmeticException |

Magnitude parity is its low decimal cell modulo2, valid also for negative
results. Increment has carry at most1. If q was all nines, the increment gives
10^p with p+1 digits; divide by ten exactly once and increase k once. Other
increments cannot increase digit count. This reproduces roundValue's second,
exact carry drop, without double rounding. There is no extra trailing-zero
normalization. The selected magnitude remains positive; apply sigma only when
constructing the final supported integer.

## Final scale/coefficient validation and cohort limits

Only after the required quotient, rounding and carry are complete form
u=S-k in Long. k is at most D (including carry), at most1292913986; S and every
intermediate scale are far inside Long. Validate u into Int using the existing
scale-error convention. Preserve the earlier nonzero LOW and HIGH/no-reduction
policies as described above; do not add alternative-cohort searches or infer
error-message/multiple-invalid priority from the arithmetic theorem.

For the final coefficient, Horner-convert the normalized decimal magnitude to
private base2^30 cells, multiplying each prefix by10^9 and adding the next chunk.
Each primitive part is below 2^30*10^9<2^60. Carry<10^9<2^30 means at most one
extra binary word per Horner step. Every nonnegative prefix is <=the final
required coefficient. After each step compute bit length in Long; a first
crossing of L proves the final required magnitude unsupported. The crossing
uses at most L+30 bits privately. Do not construct a public wrapper to discover
that condition. If no crossing occurs, encode at most ceil(L/8)=268435456 bytes
and use the signed-magnitude constructor (or a reviewed equivalent private
publication leaf). No unsupported public integer is ever created.

This is an exact decision on the required final coefficient: rejecting an
oversized provisional M before rounding was the580 defect; rejecting the final
coefficient exceeding L is a genuine unsupported required representation.
Trailing zeros could sometimes yield another numerical cohort, but current
multiply/roundValue requires the closest permitted cohort described above.
The proposal makes no stronger impossibility claim about all equal numbers.
When D<=p and M itself is unsupported, exactness alone does not authorize an
optional scale change. All ordinary inputs for which the retained policy's
required final coefficient and scale are supported reach a publication decision
without any unsupported public intermediate, subject to actual resources.

## Concrete capacities, indices, liveness and allocation qualification

Private pages may contain8192 Int cells. Bounds are logical active limits;
allocate pages on demand with checked indices. Product cells not written yet
must read as zero even if their page has not been allocated. Do not copy579's
allocated-page read assumption blindly into a zero-initialized convolution.
Each page is independently owned. Page tables and cell indices are Int-sized:
the largest logical decimal capacity is143657111 cells (product plus one
conservative carry cell), fewer than18000 pages. Binary input scratch has
71582789 cells; final binary conversion needs71582790 including its crossing
word. Page padding is at most8191 cells per buffer. All capacity sums, digit
counts, byte counts and scales are first computed in Long and checked before
narrowing. p is never used as an allocation size when it exceeds D.

Loop indices i<n and j<m obey i+j<=n+m-2 and i+m<=n+m-1, all below143657110;
terminal increments cannot approach Int overflow. Primitive product bound is
10^18-1, so addition and multiplication must be explicitly Long. Guard/sticky
indices are at most floor((D-2)/9), within active length; quotient shifting
requires no negative index. Rounded increment may use the extra carry cell;
division by10 uses part<10B0. Final binary bit arithmetic is Long, and final
byte indexing is bounded only after the supported bit-length check.

A constructive lifetime schedule uses five owned paged buffers: A-decimal,
B-decimal, reused input-binary scratch, product/rounded quotient, and final
binary publication scratch. It needs no second full product, decimal quotient
copy or remainder array. For conservative peak resource accounting retain all
five, their page tables/padding, both original input magnitudes, final byte array
and constructor binary magnitude (plus any constructor trim copy). Phase scopes
can make scratch unreachable but do not guarantee prompt GC or reclamation.
Total allocation and time can be very large: input repeated division, schoolbook
product and final conversion are not claimed fast or feasible at the domain
maximum. Resource exhaustion remains resource exhaustion, not a numerical
success, skip, invented precision cap or fabricated range rejection.

## Truthful subtypes, callers and unresolved adoption gates

The raw path is selected with canonical stored signs/words/bit lengths, not
new overridable metadata. That permits an all-ordinary theorem and extends the
arithmetic to truthful stored-value-consistent coefficient subtypes without
relying on their dynamic class. Current-format restoration can retain such
subtypes; constructor normalization alone does not prove them unreachable.
The original decimal entry/scale/zero signum prefix must still be mapped and
preserved exactly in a later actual diff. At the selected raw seam, internal
coefficient multiply/abs/byte/string callbacks are replaced by private storage
arithmetic. A truthful exact multiply at an unsupported integer result could
itself throw; its incidental inherited invocation is not a new required decimal
failure when the required rounded decimal result fits.

Explicit caller calls to public overrides, caller-written super entries and
direct caller-owned results/exceptions remain their own language contracts.
No public method becomes final and no caller adapter/serialization policy is
changed. Dishonest sign/metadata answers, changing leaves, callback replacement
values, failure staging and existing mixed oracle observations require exact
541/526 classification. This proposal does not waive their unaffected contracts
or claim full inherited trace equivalence. On the unselected supported-product
path, keep the existing virtual coefficient multiplication and result handling.

Remaining gates are actual indexing/ownership/zero-prefix implementation review,
independent bounded raw-product and rounding checks (including u=L+1 selection,
guard/sticky ties, both signs, carry, all modes, HIGH/LOW policy and required
cohort preservation), truthful subtype and explicit caller controls, all target
compilation/execution, exact-source artifact/CI binding and548 resource work.
No synthetic small production cap or giant mandatory test is proposed. The
original symbolic witness and historical evidence remain intact. No code,
future test count, public-JDK failure, speedup, universal pow error bound,
task transition or580/548/541/542 closure is claimed by this finite proposal.

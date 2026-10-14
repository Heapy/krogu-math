# Bounded decimal-to-binary conversion (task 562)

SPDX-License-Identifier: Apache-2.0

This argument extends the ordinary exact-ratio rounding argument in
`NUMERICAL_ARGUMENTS.md`, without changing its rounding kernel or its existing
dispatch observations. It addresses supported coefficients with very large
compensating scales. It assumes exact own BigInteger arithmetic and the usual
IEEE primitive operations, and does not promise that an arbitrarily large input
fits the available machine memory or finishes within a particular time.

## Certified failing baseline

Let K=646456992 and N=10^K, represented symbolically throughout the certificate.
The exact rational logarithm bounds in
`scripts/certify-decimal-binary-range.main.kts` prove:

| Quantity | Exact value |
| --- | ---: |
| floor(K log2(10)) | 2147483643 |
| bitLength(10^K) | 2147483644 |
| floor((K+1) log2(10)) | 2147483647 |
| bitLength(10^(K+1)) | 2147483648 |
| decimal scale K+1 | 646456993 |

N has supported bit length, and K+1 is a supported Int scale. The old conversion
computes decimal adjusted exponent -1 and then calls `tenPow(K+1)`, exceeding
that helper's ceiling. The exact value is N/10^(K+1)=1/10, with ordinary finite
Float and Double results. This is a static proof of our old intermediate-range
defect. No giant value was allocated, no actual giant-input JDK failure was
observed, and no JDK defect is claimed.

The logarithm certificate preserves the parent's independently executed
256-term atanh-series construction from
`/private/tmp/decimal-ceiling-certificate.main.kts`. For x=2,10 and
z=(x-1)/(x+1), it sums `2 sum(z^(2j+1)/(2j+1))` for j=0..255 using exact rational
arithmetic. The positive remainder is at most
`2 z^513 / (513 (1-z^2))`. Lower/upper ratios of the ln10 and ln2 bounds enclose
log2(10); integer floors agree at both K and K+1.

## A bound on every rounding threshold

Use precision p, minimum normal exponent L and maximum normal exponent H:

| Format | p | L | H | B below |
| --- | ---: | ---: | ---: | ---: |
| Float | 24 | -126 | 127 | 159 |
| Double | 53 | -1022 | 1023 | 1093 |

Every positive rounding midpoint, including half the least subnormal and the
finite/infinity midpoint, has the form `m * 2^q`, with positive odd integer
`m < 2^(p+1)` and `L-p <= q <= H-p`. For adjacent values in a normal binade,
write the lower value as `a * 2^(e-(p-1))`; the midpoint is
`(2a+1) * 2^(e-p)`. Subnormals use fixed spacing `2^(L-(p-1))`, so their
midpoints have q=L-p. The same expression covers the largest finite significand
and its hypothetical successor `2^(H+1)`, giving the overflow midpoint. This
includes binade carries and the subnormal/normal transition. Reducing a common
power of two, when needed, does not increase the number of decimal significant
digits and is not required for the bound.

If q=-s<0, the exact terminating decimal is `(m*5^s)*10^-s` with
`s <= p-L`. Since `2^3 < 10`, m has at most `ceil((p+1)/3)` decimal digits;
since `5^s < 10^s` for s>0, its decimal coefficient has at most
`p-L + ceil((p+1)/3)` digits. If q>=0, the midpoint is an integer smaller than
`2^(H+1)`, so it has at most `ceil((H+1)/3)` decimal digits. Removing trailing
zeros can only reduce significant digits. Therefore the derived uniform bound
is

`B = max(p-L + ceil((p+1)/3), ceil((H+1)/3))`.

The implementation uses integer forms `(p+3)/3` and `(H+3)/3`. The certificate
also checks the exact largest possible coefficient for every exponent band,
using the upper numerator `2^(p+1)-1`. This is a bounded check of the bound,
not an enumeration of every significand or a second conversion implementation.

## Prefix cells, exact zeros and sticky tails

Let D be the stored positive coefficient's decimal digit count, and
let a=D-scale-1 be the adjusted exponent, computed in Long. Values with a>400
overflow both formats; values with a<-400 round to zero in both formats. These
existing early decisions and their sign handling are retained.

For a in [-400,400] and D>B, let P be the first B digits and t=B-1-a. The input
magnitude x lies in the half-open decimal interval

`[P*10^-t, (P+1)*10^-t)`.

Every midpoint strictly inside this interval would have the same adjusted
exponent a as the interval's lower endpoint, hence its at-most-B significant
digits would put it on this interval's decimal grid. There is no grid point
strictly inside one grid cell, a contradiction. When P is all nines, the upper
endpoint is the next decade, which is still an endpoint. A midpoint can occur
at either endpoint; it cannot occur strictly inside.

Determine exactly whether the discarded suffix is zero. If it is, the lower endpoint
is the exact original magnitude, so use P with scale t. If it contains a
nonzero digit, the original is strictly inside the cell. Use the strictly
interior surrogate `(10P+1)*10^-(t+1)`. Both values are then on the same side
of every rounding midpoint. In particular, a tiny nonzero tail above a lower
endpoint tie must not be treated as a tie; a value just below an upper endpoint
tie stays below it even when its retained prefix consists of nines. No tail
length or tail magnitude assumption is needed. The surrogate need not preserve
the exact binary exponent; the unchanged kernel computes that exponent from
the surrogate's exact ratio.

Round the exact surrogate ratio using the existing HALF_EVEN kernel. An exact
endpoint keeps the original tie, including retained significand parity. An
interior point never acquires a tie, so both parities round correctly. The
threshold set includes subnormal zero, normal transitions, carries and
overflow. Applying the original sign after rounding preserves negative values
and underflow to negative zero. Stored decimal zero still returns positive
zero before this work, at every scale.

## Numeric prefix extraction without a full decimal string

The first prepared version formed the whole coefficient string before taking
a prefix. Parent review rejected that approach: the symbolic witness needs
646456993 decimal characters, exceeding the verified Node string capacity
536870888. That version's static review did not establish the required JS
domain. The corrected version formats only a quotient proved to have at most
B+1 digits. The scope expansion in task562 comment1780 permits new
internal BigInteger metadata helpers; it does not permit changing arithmetic
kernels, formatting or other callers.

For a nonzero stored magnitude n, let b be its stored positive bit length,
alpha=log10(2), c=1292913986 and Q=2^32. The same exact rational logarithm
certificate proves

`c/Q < alpha < (c+1)/Q` and `alpha < 1/2`.

Set `T=(b-1)*c/Q` and `E=floor(T)+1`. Since
`2^(b-1) <= n < 2^b`, the exact decimal count D obeys D>=E and

`log10(n)-T < (b-1)/Q + alpha < 1/2 + 1/2 = 1`.

The last strict inequality holds for every supported `b<=Int.MAX_VALUE`.
Therefore D is E or E+1. The product `(b-1)*c` is nonnegative and less than
2^62, so Long multiplication and unsigned shift by 32 compute its floor
exactly. The largest possible E from this formula is 646456993, also checked
by the certificate. Zero is handled separately with D=E=1.

The converter can classify safe early cases before constructing any large
divisor: let lowerA=E-scale-1 in Long. If lowerA>400, actual a is also>400;
if lowerA+1<-400, actual a is also<-400. Otherwise request the numeric prefix.

For requested B digits, set `trim=max(0,E-B)`. Divide the stored positive
magnitude once by `10^trim`, writing `n=q*10^trim+r`, with `0<=r<10^trim`.
When trim=0, simply retain n as q and r=0. The quotient has at most B+1
digits: if trim>0 its count is D-E+B; otherwise D<=E+1<=B+1. Only q is
formatted. Its decimal string therefore has at most 160 characters for Float,
1094 for Double, independent of n's full digit count or any engine string cap.
The exact full count is `D=trim+digits(q)`; it is not inferred by rounding a
logarithm.

If q has B+1 digits, remove its one extra final digit. In general, with
j=max(0,digits(q)-B), write `q=P*10^j+s`. Then

`n=P*10^(trim+j) + (s*10^trim+r)`.

The discarded suffix is zero exactly when both s and r are zero. The helper
returns P, D, discarded count trim+j and the exact sticky bit `(s!=0 || r!=0)`.
Thus both the original division remainder and the extra quotient digit
participate, including a nonzero digit at the far end of an arbitrarily long
zero run. Appending the sticky unit and adjusting the scale implements the
same prefix-cell proof above without scanning or formatting the original
decimal suffix.

## Range, work and caller behavior

For D<=B the original scale already lies in [-400,B+399]. For D>B the
replacement scale t or t+1 lies in [B-401,B+400]. Thus no power of ten in the
final rounding ratio exceeds B+400 (1493 for Double), independent of the
original scale or D.
There are at most B+1 retained coefficient digits. Even conservative bounds
using fewer than four bits per decimal digit and at most 1074 binary alignment
bits keep the final rational kernel's intermediates below 8192 bits.

The separate prefix-extraction divisor may be large but is always supported:
for trim>0 and B>=1, trim<=D-B<=D-1, so `10^trim<=10^(D-1)<=n`. Its bit length
cannot exceed n's. The formula also gives trim<=646456992, within the existing
ten-power ceiling. No `10^D` is formed to decide the digit boundary. In the
unchanged TEN.pow implementation, the odd powers of 5 formed by repeated
squaring/multiplication never exceed 5^trim; restoring the factor 2^trim gives
exactly the supported divisor 10^trim. Its shift guard accepts trim; the
early power bound tests 2*trim, which is below Int.MAX_VALUE, and the general
multiplication lower-bit guard cannot reject a supported actual product.
In the unchanged binary long division,
each shifted partial remainder is no larger than the processed prefix of n;
there are at most b processed bits, so no shift exceeds the supported range.
The allocated limb arrays are bounded by the input's limb count plus existing
carry space. No arithmetic kernel is modified.

Prefix extraction performs one power and one division by that power, followed
by formatting at most B+1 digits and removing at most one extra digit. It does
not repeatedly cancel single zeros or create a coefficient-sized decimal
string/character array. The unchanged arithmetic kernels can still take very
large time and O(b) space on huge inputs; availability of that memory/time is
not guaranteed. Removing the engine's hard string-capacity requirement does
not claim a measured giant-input run or new asymptotic performance for division.
No platform decimal parser participates in production conversion.

Compact eligibility is evaluated from the original coefficient and scale,
before any reduction. Original compact paths retain their no-callback behavior;
the general path still calls virtual signum exactly once and does not call
virtual precision, scale or unscaledValue. The new internal BigInteger helpers
read stored magnitude bits and wrap the stored limbs in an ordinary positive
BigInteger, so they also avoid original subclass formatting, abs and bitLength
callbacks. Magnitude comes from stored fields.
Truthful subclasses therefore retain their numerical and caller contract, and
the existing replacement-callback diagnostics remain enabled. No public API,
other arithmetic path or serialization field is changed.

## Evidence and remaining gate

The existing 1728 signed integer midpoint-neighbor outcomes and 1296 exact
decimal midpoint outcomes are retained. New common tests use adjacent IEEE
encodings to derive exact midpoint inputs and independent expected bits, with
signed decimal tails beyond B, both parities, transitions and overflow. Large
practical equivalent cohorts check cancellation. A new JVM suite compares the
bounded inputs' raw bits with public JDK conversions. These are finite
regressions, not a giant-allocation test or exhaustive compatibility claim.
Another 419 common helper cases independently compare numeric prefix metadata
with bounded known decimal text. They include decimal/binary power neighbors,
both prefix widths, zero and negative values, throwing public subclass
overrides, distant nonzero tails and an extra-quotient-digit-only sticky witness.

The code/proof phase does not authorize compilation or platform tests. At this
handoff compilation and JVM/JS/Wasm/macARM checks have not run. Task local:562
must remain in_progress until the parent grants the execution window, required
checks run and their logs/counts/XML and frozen source hashes are retained.
Root alone integrates, publishes and waits for fresh supported CI. No changes
are committed by this worker.

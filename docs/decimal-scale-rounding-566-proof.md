# Decimal scale and rounding repair: source proof

Task `local:566`, pane `01660b06`, parent root526 / `af5f5ab4`.
Base `96ddbf7b9241f14265620c72a8217c32c4cdb4ff`, branch
`task/566-decimal-scale-rounding`. **PREPARED / UNCOMPILED / UNRUN**.
This is original Apache-2.0 reasoning and source review, not runtime evidence.

## Contract and premises

The [public Java SE 25 BigDecimal specification](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
defines values by coefficient and scale, arithmetic by logical exact result
followed by context rounding, and preferred scales by the operands. In
particular, multiplication rounding reduces scale by discarded positions,
including an extra position for a leading carry. `setScale` fixes the result
scale and rounds the coefficient when necessary. Exact/unlimited preferred
cohorts outside the scale domain are not newly required by this work.

Let `L=Int.MAX_VALUE`, `MIN=Int.MIN_VALUE`, `MAX=Int.MAX_VALUE`,
`K=646456992`, and let nonzero stored coefficient `c` have magnitude `n`
and `D` decimal digits. Assume truthful stored values, metadata and own
integer operations. Decimal subclasses forwarding to `super` are covered;
arbitrary poisoned callbacks and internal callback sequence parity are not
proved. Existing storedPrecision, compare, numerical kernels, retained
serialization/subtype behavior, and resource availability remain prerequisites.
No foreign numerical production implementation was inspected.

## Downscale: complete partition without an unsupported power

The desired coefficient is `c / 10^k`, rounded to an integer, where
`k=sourceScale-targetScale>0`. Subtract the Int scales in Long:
`1 <= k <= 4294967295`, so neither negation nor subtraction overflows Long.
The target scale is already Int. Zero retains the existing early result.

1. `k>D`: `0<n/10^k<0.1`. Its integer quotient is zero, the discarded
   part is nonzero, and it is strictly below half. The existing `roundTiny`
   gives signed one for UP; zero for DOWN and every half mode; signed one
   only for positive CEILING or negative FLOOR; UNNECESSARY throws.
   No power, scale-distance narrowing, or large intermediate is needed.
2. `k=D`: write `n=h*10^(D-1)+r`, `1<=h<=9`,
   `0<=r<10^(D-1)`. The quotient at `10^D` is zero. If `h<5` the
   fraction is below half; if `h>5` it is above; if `h=5`, `r=0` is a
   tie and `r>0` is above. Compare the small leading quotient with five,
   then check remainder zero. The retained integer zero is even, including
   for negative operands. Use the stored coefficient sign, as the previous
   ordinary quotient path did. All coefficients here are nonzero, so
   UNNECESSARY must throw even at the tie. No `10^D`, `5*10^(D-1)`,
   doubled remainder, or multiplied threshold is constructed.
3. `k<D`: `10^k<=10^(D-1)<=n`, so the divisor is a supported value.
   Existing quotient/remainder rounding applies, including exact divisibility,
   both signs, every half rule, retained parity, and UNNECESSARY.

In case 2, `10^(D-1)<=n`, the quotient is at most nine and the remainder
is below that divisor; every newly created mathematical integer is supported.
In case 3 the quotient is strictly smaller than n; a possible unit increment
is at most n. The existing doubled remainder is also supported: `k<=K`,
and `r<10^k<=10^K<2^(L-3)`, hence `2*r<2^(L-2)<2^L`.
This is a value-domain proof, not a new allocation or time guarantee.

### Maximum D and the existing power ceiling

The existing source certificate
`scripts/certify-decimal-binary-range.main.kts` uses exact rational logarithm
bounds to establish

`2^(L-4) <= 10^K < 2^(L-3)` and `2^L <= 10^(K+1) < 2^(L+1)`.

These are mathematical inequalities, not measurements in this work. They
can be checked independently using
`ln(x)=2*sum[j>=0](z^(2j+1)/(2j+1))`, `z=(x-1)/(x+1)`;
after 256 terms a positive tail is bounded above by
`2*z^513/(513*(1-z²))`. Divide the bounds for ln(10) by the opposite
bounds for ln(2). The resulting floor bounds for `K*log2(10)` both equal
`2147483643`; for `(K+1)*log2(10)` both equal `2147483647`.
The source of that certificate was read, **not executed**; its later
execution is an explicit remaining validation gate.

Supported magnitudes are below `2^L`, so `D<=K+1=646456993`.
Thus both `D-1` in case 2 and `k` in case 3 respect the existing `tenPow`
ceiling K. There is no arbitrary smaller coefficient domain and no new cache.
For a no-giant symbolic endpoint witness choose `c=10^K` and `k=D=K+1`.
Then the fraction is exactly `1/10`; the new divisor is `10^K`, which is
supported, while the old required `10^(K+1)` is unsupported. A second
endpoint family `c=h*10^K+r<2^L`, with `h=5` and `r=0` or `1`, gives
tie/above-half: `5*10^K+1<2^(L-3)*5+1<2^L`. The below-half `h=4,r=0`
is also supported. These are symbolic values only; no giant is allocated.

Increasing scale keeps its original positive-distance and power guards.
For nonzero c, `c*10^shift` is the required output coefficient; this patch
does not rescue unsupported outputs. Zero, equal-scale identity, mode
validation, enum-to-legacy dispatch, and exact convenience dispatch are
retained. In particular `ONE@MAX.setScale(0,DOWN)=0@0`, and the maximum
distance `ONE@MAX.setScale(MIN,DOWN)=0@MIN` needs no huge integer.

## Multiplication: translation-invariant rounding, restricted HIGH rescue

The logical scale is `S=s1+s2`, computed in Long in
`[-4294967296,4294967294]`. Unlimited multiplication still calls the
existing explicit exact overload. For finite contexts the old path is
retained if `S<=MAX`, or either stored coefficient is zero. This retains
low-scale behavior and receiver-zero saturation versus nonzero-receiver
rejection; in particular no new zero-source symmetry is promised.

Only `S>MAX` with nonzero coefficients enters the new path. Compute the
full product c with the unchanged own integer multiply. Its supported full
coefficient domain is a premise: unsupported products, coarse integer guard
rejections, allocation failures and possible algorithms that would avoid the
full product remain explicitly open outside this scale repair.

If `D<=p`, throw the existing scale failure. This preserves unproved exact
HIGH-cohort rescue behavior, including products with removable trailing
zeros, rather than silently stripping to a different exact cohort. For
`D>p>=1`, rounding c at neutral scale zero is mathematically equivalent
to rounding c at S followed by adding S to the rounded scale. Rounding
uses only the coefficient, dropped decimal positions and mode, so scaling
the decimal grid by `10^-S` preserves each quotient, remainder, sign, tie,
parity, exactness and carry decision.

Specifically let `d=D-p>=1`. Own rounding first divides by `10^d`.
Its signed rounded quotient has at most `p+1` digits. If it has p+1,
the magnitude is exactly `10^p`, so the second rounding divides by ten
exactly, producing `10^(p-1)`. There is no second inexact rounding and
therefore no double-rounding error. The neutral scale correction is
`t=-d` or `-d-1`. This includes a precision-required drop containing
only zero digits: UNNECESSARY then succeeds. It excludes inexact
UNNECESSARY by the existing rounding exception.

`d<=D-1<=K`; both divisors are supported. A quotient increment is at most
`10^p<=10^(D-1)<=n`; remainder doubling obeys the bound above. The neutral
scale correction is in `[-D,0]`, strictly within Int, so neutral own rounding
cannot suffer scale overflow. Finally `S+t` is computed in Long and validated
once the final coefficient is known; its absolute value is below 5 billion.
No intermediate preferred scale is narrowed. If the final scale remains
above MAX it still fails. If supported, the exact required pair is returned.
Since S>MAX and D<MAX, this HIGH rescue cannot create a scale below MIN.

Ordinary witnesses for every sign and rounding mode:

* `ONE@MAX * TEN@1`, p1: product `10@(MAX+1)` drops one zero;
  required pair `1@MAX`, also for UNNECESSARY.
* `99@MAX * ONE@2`, p1: no-carry modes yield `9@(MAX+1)` and fail;
  carry modes produce `1@MAX`. Negative directed modes reverse direction.
  UNNECESSARY fails for discarded nonzero digits. This challenges a repair
  that validates after the first reduction but before the extra carry drop.
* Exact overload, p0, p>=2 on the first witness retain failure controls.
  They are preserved behavior, not claims of mandatory rejection under every
  conceivable alternative-cohort policy.

## Addition with zero: choose the finite-precision cohort first

The selected nonzero source is rounded through the existing public round
entry, retaining all its explicit dispatch and failure behavior. Let the
rounded pair be `q@s` with `d` digits, and `P=max(input scales)`. For
truthful finite context, `d<=p`; rounding never increases scale, so `P>=s`.
An exact cohort at scale `s+j` appends j zeros and has `d+j` digits.
Thus allowable padding is `0<=j<=p-d`, and the closest allowable scale
to P is exactly `min(P,s+p-d)`. Compute this in Long before validation.
The removed check tested `P-s>MAX` even when p forbade any such padding.
It did not prove the required result unsupported.

For `ONE@MIN + ZERO@MAX`, p1, the new scale is MIN for both operand
orientations, both nonzero signs, and every mode, with no rounding necessary.
p2 allows exactly one appended zero, giving `10@(MIN+1)` for positive one.
The retained existing `setScale` validates and constructs only the selected
padding. With p=MAX the selected padding for this witness is MAX-1, whose
required nonzero coefficient exceeds the supported integer range; it still
fails. Rounding a multi-digit source at MIN may itself require a scale below
MIN, which remains an error. Both-zero addition still returns zero at P.
Subtraction retains its existing negate-plus-add composition.

All additions in the selected scale calculation fit Long: s is Int,
`1<=p<=MAX`, and truthful digit counts are at least one. Explicit caller
overrides and direct `super` entries are still public virtual operations;
null dispatch and p0 behavior are unchanged. No arbitrary poisoned internal
callback compatibility claim follows from this ordinary/truthful proof.

## Independent regression expectations and later observations

`DecimalScaleRoundingRepairTest.kt` uses a separate bounded signed Long
quotient/remainder model. That model's coefficients and products have at most five
digits; finite precisions are at most seven. It never calls either decimal
implementation or big-integer arithmetic for expected values. The model
uses `r` versus `divisor-r` for half comparison, and handles large k as
strictly below half, not by constructing powers above 10^18. Output padding
is bounded to seven positions. Other literal controls explicitly distinguish
preserved exact HIGH behavior from mandatory precision reductions.
Separate 80-digit literal threshold rows cover wide below/tie/above-half
and even/odd retained digits; an 81-digit power-of-ten product drops 80
exact zero positions. Their expectations use the written leading digits and
literal output pairs, independently of the Long model or decimal arithmetic.

The common source covers k>D, k=D, k<D, exact/tie neighbors, parity, both
signs, zero, all eight modes, both rounding overloads and exact convenience,
full signed scale distances, zero-add orientations and subtraction, finite
precision/cohort choices, multiplication carry and failure controls, truthful
operand combinations, direct-super calls, explicit replacement/exception
identity and nulls. The JVM source adds literal public black-box observations,
without treating JDK values as expectations for the port.

`scripts/diagnose-decimal-scale-rounding-566.main.kts` loads a supplied fresh
own JVM JAR reflectively and uses only public JDK APIs for comparison. It
prints JAR SHA-256, JDK identity, independent literal expectations and all
bounded value/error/message outcomes without early assertion or waiver.
Before numerical rows it validates the loaded decimal, integer, context and
rounding classes' code-source locations against the supplied JAR, rejecting
ambient parent-classpath shadowing instead of misattributing observations.
Run separately against baseline and candidate fresh JARs only after a later
explicit parent execution grant; preserve both complete outputs. It is
**UNCOMPILED / UNRUN**, as are all new tests and production changes here.
No JDK defect, passing regression, full compatibility or CI completion is
claimed. Existing frozen fixtures, assertions and diagnostics remain intact;
any later oracle disagreement needs narrow proof plus actual observations.

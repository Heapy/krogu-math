# Bounded decimal square-root midpoint comparison

Task `local:565`, pane identity `074237e5`, branch `task/565-sqrt-midpoint`,
base `37f55f2de12a9e30b8d2bda114fd986e21b0e83b`.
Status: **PREPARED / UNCOMPILED / UNRUN**. This is independent Apache-2.0
source and mathematical reasoning, not runtime evidence.

## Ownership and prerequisites

The only production edit is the inexact `BigDecimal.sqrt` block after the
existing construction of `numerator` and `denominator`: division,
exactness, and midpoint comparison. No helper, public entry, finite exact
root path, exponent/precision metadata, rounding rule, stripping rule,
BigInteger kernel, fixture, old assertion, or build file changes.
Task528 separately owns metadata/range support; task542 retains other pow,
inexact-root and working-scale problems. Parent alone integrates changes.

Assume positive supported integers `n=numerator`, `d=denominator`, and truthful
stored values and integer operations, including consistent retained coefficient
subtypes as well as ordinary integers. This proof also includes `n=0` for
the comparison formula. It is conditional on successful upstream construction
of `n,d` and computation of the integer root. It proves all *new arithmetic
values* fit the supported magnitude range; it does not promise that maximum
size operands fit available memory or complete in practical time. No giant
witness is allocated. No third-party numerical implementation was consulted.

## Full-domain identity and endpoint cases

Euclidean division gives `n=q*d+r`, `0<=r<d`, `q>=0`.
Let `a=floor(sqrt(q))` and `delta=q-a²`. Then `a²<=q<(a+1)²`.
Also `a²<=n/d<(a+1)²`: the right inequality follows from integer
`q<=(a+1)²-1` and `r/d<1`. Thus this is exactly the floor of the desired
scaled root, even when the division has a positive remainder.

The true root is exact at the target scale iff `n=a²*d`, equivalently
`delta*d+r=0`, equivalently **delta=0 AND r=0**. Both summands are
nonnegative. In particular `q=a²` alone does not justify suppressing UP or
CEILING: `4+10^-k` has `q=4,a=2,delta=0,r=1`.

The root's position relative to `a+1/2` has the sign of

`H = 4*n-(2*a+1)²*d = (4*(delta-a)-1)*d+4*r`.

This expression is a proof identity only; production never constructs it.
If `delta<a`, integrality gives `4*(delta-a)-1<=-5`, so
`H<=-5*d+4*r<-d<0`. If `delta>a`, the multiplier is at least 3,
so `H>=3*d>0`. If `delta=a`, `H=4*r-d`.

Write `d=4*u+v`, `u=floor(d/4)`, `0<=v<=3`. If `r<u`,
`4*r-d<=-4-v<0`. If `r>u`, `4*r-d>=4-v>0`. If `r=u`,
`4*r-d=-v`: tie only when `v=0`, otherwise negative. Hence comparing
`delta` with `a`, then `r` with `u`, then returning `-sign(v)`
is the full-domain sign. No shifted numerator/remainder, midpoint square,
or product with `d` is needed.

For `a=0`, `q=0,delta=0`, so this reduces correctly to the quarter-divisor
rule, including the exact zero when `r=0`. For `d=1,2,3`, `u=0,v>0`:
`r=0` is below midpoint; every positive `r` is above when `delta=a`.
For `d` divisible by 4, equality `r=u` is exactly the midpoint.
At `r=0` exactness is still conditional on `delta=0`; at `r=d-1`
the strict bound `r<d` used above remains valid.

The public decimal grid's genuine midpoint has a finite decimal root, so
ordinary tie examples correctly take the earlier finite-exact path563.
The public tests retain these ties; they do not assert inexact-branch tie
coverage. The identity/proof and bounded certificate explicitly cover the
equality case, arbitrary positive `d`, `a=0`, and `d<4`. No poisoned
subclass or changed predicate forces an artificial path.

## Magnitude and construction bounds

For supported positive operands with bit length at most `L=Int.MAX_VALUE`,
`0<=q<=n`, `0<=r<d`, `0<=a²<=q`, `0<=delta<=q`.
The root `a` is nonnegative and satisfies `a<=q` for integer `q>=1`;
when `q=0` it is zero. Its square is a supported value rather than a product
whose value depends on the potentially large denominator.

The own BigInteger multiply guard checks `bitLength(a)+bitLength(a)-1`.
Since a nonzero square's bit length is at least `2*bitLength(a)-1` and
`a²<=q`, that guard cannot reject this square for supported `q`.
The square's limb scratch allocation is at most one limb larger than its
normalized result; it does not construct a larger mathematical BigInteger.
Both the array length and this extra scratch limb are distinct from a
supported-value/midpoint overflow. Allocation and kernel costs remain outside
this local arithmetic proof.

`u<=d`, `0<=v<=3`; the constant four has three bits. No sum/product
involving `d` is created. Sign comparisons return small machine integers;
negating `sign(v)` cannot overflow. For rounded increment, `a+1<=q`
when `q>=2`; for `q=0,1`, `a+1<=2`. Alternatively
`bitLength(a)<=ceil(L/2)` and a one-bit carry remains below `L`.
Thus the unchanged possible increment is supported even for maximum `q`.
Exactness involves zero tests only.

Division now requests both quotient and remainder. On this base the old
`divide` already computed both using `divideStored`; this retains rather
than recomputes its remainder. During its binary long division, each partial
remainder before subtraction is at most the processed prefix of `n`, hence
at most `n` even if a coarse `2*d` bound exceeds the domain. Division by four
uses the small-divisor branch. The existing sqrt kernel is unchanged.

## Symbolic overflow witness

The own pre-existing `scripts/certify-decimal-binary-range.main.kts` establishes
`bitLength(10^646456992)=2147483644` through bounded rational logarithm
inequalities. This run only read that certificate; it did not execute it.
Put `K=646456992`, `N=10^K`, `c=4*N+1`, and `x=c*10^-K`.
Because N is an integer, shifting it twice and adding one cannot carry
beyond its bit length plus two: `bitLength(c)=2147483646<=L`.
The old `c<<2` needs `2147483648=L+1` bits and is rejected.

K is even. With `M=10^(K/2)`, `(2*M)²<c<(2*M+1)²`, since the
gaps are `1` and `4*M`. There is no finite exact root. In the precision-one
branch, exponent=0, scale=0, n=c,d=N,q=4,r=1,a=2,delta=0.
The new comparison is negative immediately (`delta<a`). All new integers
other than existing n,d are tiny. `4<x<6.25`, so `2<sqrt(x)<2.5`.
DOWN/FLOOR/all half modes must return coefficient `2`, scale `0`;
UP/CEILING must return `3@0`; UNNECESSARY must fail.
JS input-string precision is separately task528; this change does not claim
to remove that preceding limitation or all task542 issues.

## Independent literal test derivation

The common expectations use square boundaries on the requested decimal
rounding grid, independently of quotient/delta comparison. The ordered
vectors explicitly specify UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN,
HALF_EVEN. All rows require UNNECESSARY failure.

* `5,6<2.5²=6.25<8`, with roots between 2 and 3.
* `2,2.249<1.5²=2.25<2.251`, with roots between 1 and 2;
  `2.250` is the odd-lower-digit tie.
* `6.249<2.5²=6.25<6.251`; `6.250` is the even-lower-digit tie.
  Input scale translations by ±6 multiply the root grid by 10^∓3,
  giving result scales -3,0,3 from input scales -3,3,9.
* `1.4²<2<1.45²`, so p2 half rounding gives `14@1`.
  At p7, `1.414213²<1.4142135²<2<1.414214²`, so the
  half modes give `1414214@6`, while DOWN/FLOOR give `1414213@6`.
* `15.6024<3.95²=15.6025<15.6026`, so the p2 grid is
  `39@1` / `40@1`, and the tie's lower integer digit is odd.
* `2²<4.001<2.05²`, so p2 has `20@1` / `21@1`;
  preferred scale 1 retains the zero in the lower coefficient.
* `9.95²=99.0025<99.99<10²`; p2 UP/CEILING/half modes carry
  to `10@0`, while DOWN/FLOOR give `99@1`.
* `2.000249²<2.0002495²<4.001<2.000250²`; p7 HALF_EVEN
  yields `2000250@6`, then the unchanged preferred-strip rule yields
  `200025@5`.

Appending 80 coefficient zeros and adding 80 to the input scale preserves
the exact radicand while forcing wide coefficients. Requested precision and
preferred-scale bounds preserve all listed representations, including the
p2 `40@1` tie and p2 carry `10@0`. Separate literal exact-root tests preserve
`4.00 -> 20@1`, `4.0000 -> 200@2`, and `4@-4 -> 2@-2` across all modes.

## Dispatch, retained observations, and pending verification

Existing decimal constructors normalize coefficient subtypes through their
virtual byte encoding to base BigInteger. That constructor property does not
cover every stored coefficient: current own-format `readObject`/`resolveObject`
can retain a truthful BigInteger coefficient subtype. The stated truthful
stored-value/operation premise covers consistent retained subtypes as well as
ordinary stored/computed integers. The arithmetic proof depends on the values
and operations satisfying that premise, not on every receiver having base
BigInteger dynamic type. It does not establish arbitrary override results,
exception behavior, or callback-trace parity for retained subtypes. Serializer
and legacy scope are unchanged; no additional subtype test is claimed here.
The public sqrt prefix, sign/zero/null behavior, finite-exact path,
normalization, adjustedExponent's public precision callback, return rounding,
preferred stripping, and explicit override/super dispatch remain textually
unchanged. New tests cover truthful subclasses, caller-owned super,
replacement identity, explicit exception identity, and direct sign/precision
exception identity on an inexact radicand.

The internal integer call graph changes: `divide` becomes
`divideAndRemainder`, the denominator product for exactness disappears,
and midpoint shifts/products become delta comparisons and (conditionally)
division of d by four. No new public decimal metadata callback is introduced.
This is a source analysis, not a runtime callback-parity claim. Removed or
changed internal integer callbacks, including those of a retained coefficient
subtype, are observations, not an automatic guarantee or waiver. Every existing
whole-string
oracle/callback/returned-representation assertion remains active and unedited.
Any future mismatch needs independent contract analysis and parent review;
no expected old result or trace is replaced here.

The bounded certificate enumerates `q=0..128`, `d=1..64`, every `r=0..d-1`,
compares against the direct squared midpoint, and checks exactness/ranges.
Its enumerated midpoint expressions are below 2^31; the symbolic range metadata
uses Long arithmetic. It retains only counters and one source String,
allocates no big numerical object, starts no
process, and writes nothing. It is designed for a future `-Xmx256m` run.
It verifies the exact reviewed source block and symbolic maximum-bit/witness
metadata; the bitLength(N) premise remains the referenced prior certificate.
It is supplementary bounded evidence, not a substitute for the universal proof.

All new tests and the certificate are **PREPARED / UNRUN**. No Gradle,
Kotlin/compiler, Native, javac, numerical probe, test, benchmark, harness,
or certificate execution is authorized or performed in this preparation.
Future parent-authorized gates include common tests on JVM/JS/Wasm/macARM,
bounded live public-JDK comparisons, unchanged existing roots/callback/oracle
tests, and fresh integrated all-six CI. No JDK defect or intended divergence
is asserted. Task565 stays linked/in_progress awaiting parent review and the
sole runtime-window transfer. Changes are not committed.

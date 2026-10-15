<!-- SPDX-License-Identifier: Apache-2.0 -->
# Task 530: normalized stored-limb division proof

Status: SOURCE-ONLY proposal under `/Users/yoda/.kotgent/prompts/5c644211.md`.
This is independent paper reasoning against the new source, not a machine-checked
proof or correctness acceptance. No compiler, numerical helper, symbolic tool,
script, fixture generator, test, benchmark, or harness was run. No foreign
numerical production implementation bodies were read or used.

## Scope and representation assumptions

Only the multi-limb branch of private `BigInteger.divideStored` and a new private
`normalizeStoredDivision` helper change. Public signatures and entry expressions,
the divisor-zero check, smaller-dividend branch, and one-limb path are unchanged.
The source retains the accepted composition's 528 metadata and 538 context/guards.
Production multiplication, modPow, modInverse, decimal, RNG, primality, fields,
serialization, formats and build pins are untouched.

Let B=2^30 and M=B-1. Stored magnitudes are canonical little-endian arrays:
each digit is in [0,M], a nonempty array has a nonzero top digit, and its
magnitude bit length is at most Int.MAX_VALUE. Sign is -1, 0 or 1, with sign
zero exactly for the empty magnitude. These are the existing constructor /
`normalized` / deserialization invariants, rather than new public checks.

After the unchanged checks, the new branch has A=|a| >= D=|b| > 0, dividend
length m >= divisor length n >= 2. Define s=30-bitLength(D[n-1]). Then
0<=s<=29. Let V=D*2^s and N=A*2^s.

## Normalization and initial window

`normalizeStoredDivision` copies each digit with a carried high part. At each
step `(source[i] << s)` has its low s bits zero, and carry<2^s occupies only
those bits; OR therefore equals addition. The combined value is <B*2^s<=2^59.
Masking extracts the low base-B digit and shifting yields the next carry. Thus
the helper returns the exact extra top digit and never changes its source.

For the divisor, the highest set bit moves to bit 29. No bit reaches bit 30,
so the returned divisor carry is zero and V has exactly n digits. Its high
digit h satisfies B/2<=h<B. For the dividend, an m+1st digit is reserved and
the returned carry is stored even when normalization increases the digit count.
The scratch value N may have more than Int.MAX_VALUE bits; it is a raw array,
not a public/intermediate `BigInteger` subject to that magnitude restriction.

There are m-n+1 quotient positions, from j=m-n down to zero. The first n+1-digit
window U=floor(N/B^j) satisfies U<B*V: A<B^m and D>=B^(n-1) imply
N/B^(m-n)<2^s*B^n<=B*V. The loop invariant is 0<=U<B*V. Once a window is reduced
to R<V, appending the next low digit d produces B*R+d<B*V, proving the invariant
for the following iteration. Previously processed high cells are zero.

## Estimate bound and leading-three-digit correction

Write q=floor(U/V), so 0<=q<B. Let T=floor(U/B^(n-1)), the leading two window
digits. The initial estimate is E=min(floor(T/h),B-1). Because U>=q*V,
T>=q*h, hence E>=q. Since V<(h+1)*B^(n-1) and U<(q+1)*V,

    T < (q+1)*(h+1), hence T <= (q+1)*h+q.

Here q<=B-1<2h. Dividing by h gives floor(T/h)<=q+2, so E is at most two too
large, including when the uncapped estimate is B or B+1. Capping cannot put it
below q. The source computes residual=T-E*h, which is nonnegative.

Let t=V[n-2], P=B^(n-2), C=floor(U/P)=T*B+U[n-2], and L=h*B+t. Comparing
`E*t` with `residual*B+U[n-2]` is exactly comparing E*L with C. If residual>=B,
E*t< B^2 <= residual*B+U[n-2], so the test already passes; the source guards
before shifting that residual. If the test rejects E, E>q, since q*V<=U implies
q*L<=C. Each rejection decrements E once and adds h to residual; the test can
therefore decrement at most twice without crossing q.

On termination E*L<=C. Also V<(L+1)*P and U<(q+1)*V yield
C< (q+1)*(L+1), i.e. C <= (q+1)*L+q. Because L>=B^2/2>q, E>=q+2 would give
E*L >= (q+2)*L > C. Thus the corrected E is either q or q+1. For n=2 the
leading-three-digit test sees the entire divisor/window and leaves exactly q.

All leading-digit computations fit signed Long. The stored top-two value is
at most B^2-1<2^60; E*h and E*t are at most (B-1)^2. Before capping, a division
residual is <h. If capping applies, the window bound gives its high digit<=h,
so T<=h*B+B-1 and the capped residual<=B+h-1<=2B-2. Each correction starts
with residual<B and raises it by h, preserving residual<=2B-2; a residual>=B
ends the loop. The shifted comparison is evaluated only for residual<B, so
its right-hand side is <=B^2-1. No potentially large residual is shifted.

## Subtraction, borrow and single add-back

The low n cells subtract E*V with a product carry combined with subtraction
borrow. Inductively borrow is between 0 and E. For a divisor digit v,
`product=E*v+borrow <= E*B <= B^2-B`. Its low digit is masked off; subtracting
it from a window digit yields a difference in [-M,M]. The next borrow is the
product's high digit plus 1 exactly when that difference is negative. If the
high digit equals E then the low product digit is zero, so no extra borrow is
possible; the bound borrow<=E is preserved.

The final high-cell difference has sign equal to U-E*V. For E=q it is
nonnegative and the mathematical remainder is in [0,V). For E=q+1 it is
negative, with -V<=U-E*V<0. Digits currently encode that value modulo B^(n+1).
Decrementing E and adding V once restores U-q*V to [0,V); there is no need for
a second add-back. The n low-digit additions have sums<=2B-1, carry<=1.
The top cell's wrapped addition discards the overflow from the earlier negative
representation. Since the corrected remainder is <V<B^n, that top cell is zero.

Each position subtracts exactly its final quotient digit times V*B^j from
the complete working dividend; the low j cells are unchanged. Induction over
positions therefore gives N=Q*V+R, with 0<=R<V, and every quotient digit in
[0,M]. No public virtual arithmetic, comparison, metadata, sign or shift method
is called by the new arithmetic loop or normalization helper.

## Denormalization, exact identity and signs

Since N and V are multiples of 2^s, the final R=N-Q*V is also a multiple of
2^s. The low n scratch digits (and the zero cell n) suffice. For s>0 the export
combines `u[i] >>> s` and the masked `(u[i+1] << (30-s))`, whose bit fields are
disjoint. For s=0 it copies directly, avoiding an accidental masked shift by 30.
This yields the exact integer r=R/2^s. Thus A=Q*D+r and 0<=r<D.

The unchanged `normalized` output calls apply sign(a)*sign(b) to Q and sign(a)
to r. With signed inputs a=sa*A and b=sb*D, the signed outputs satisfy
q*b+r=a, |r|<|b|, truncation toward zero, and the dividend's sign for nonzero
remainder in all four sign combinations. Normalization returns canonical ZERO
for zero quotient/remainder. Quotient and remainder cannot exceed the original
supported magnitude domain, so no valid result acquires a new range exception.

The unchanged divisor-zero check runs before magnitude comparison and preserves
`ArithmeticException("BigInteger divide by zero")`, including zero/zero.
The unchanged smaller-dividend shortcut covers zero dividend and returns ZERO
plus a base-class normalized signed dividend. The one-limb loop is preserved
byte for byte: remainder<d<B gives `(remainder<<30)+digit < B^2`, so its existing
Long arithmetic is safe. Equal and highly unbalanced multi-limb inputs use the
same new proof; no balanced-size assumption is made.

## Array lengths, maximum domain and memory

For magnitude bit length <=Int.MAX_VALUE the limb count is at most
ceil(2147483647/30)=71,582,789. At that count only seven top bits can be used.
No maximum-size object or test is allocated/executed in this milestone.

For 2<=n<=m<=71,582,789:

- Divisor normalization uses n cells; dividend normalization uses m+1.
- Quotient uses m-n+1; remainder export uses n.
- `m+1<=71,582,790`, `j+n<=m`, and `j+i<=m-1` for subtraction/add-back.
- Denormalization accesses i+1<=n<=m, within the m+1 dividend array.
- All length/index expressions fit Int. Loop endpoints can decrement to -1
  without wrapping; no bit-index multiplication is used by the new kernel.

The four digit arrays total 2m+n+2 Int cells, linear in the input sizes.
No array or BigInteger is allocated inside the quotient-position loop.
`normalized` may additionally allocate trimmed copies of the quotient and
remainder; their total length is at most m+1. A conservative total including
these export copies is 3m+n+3 cells, at most 4m+3=286,331,159 cells, excluding
existing input arrays, headers and other live application data. This is a
linear bound, not a promise that maximum-domain allocations succeed. Normal
platform allocation failures/OOM remain possible and are not intercepted.
Maximum-domain feasibility and target heap budgets remain runtime/capacity gates.

Each invocation exclusively owns all four new arrays. The helper writes only
the distinct destination given by its two call sites. Operand magnitudes are
read only, even if receiver and divisor are the same object or share their
immutable backing via negate/abs. Scratch dividend/divisor arrays never escape.
Quotient and remainder arrays are disjoint, are last mutated before output
construction, and are either retained or copied by `normalized`. No workspace
is stored in a field, global, context, pool or cache; concurrent calls therefore
share no new mutable state. The original smaller-dividend branch may retain an
input magnitude, just as before; no new writer can reach that shared array.
Caller-owned pair arrays and byte exports cannot mutate stored magnitude digits.

## Consumer contract and variants

`gcd` uses nonnegative stored views and takes the remainder component from
`divideStored`; exact remainder reduction preserves its Euclidean invariant.
`modInverse` consumes both components for the extended Euclidean update; the
new quotient/remainder identity preserves that update and its original error
and final mod paths. Public `divide`, `remainder`, `divideAndRemainder`, sqrt,
modular reduction and decimal coefficients all reach the same unchanged entry
signatures. Decimal exact reduction, rounded division, scale normalization and
rounding-mode choices still consume the same signed integer results. No decimal
or consumer implementation changes are made. The newly prepared consumer tests
and all existing dispatch/null/identity/decimal fixtures remain UNRUN gates.

Quotient-only and remainder-only variants are deliberately NOT implemented in
this first source milestone: all existing call sites still receive the pair.
The arithmetic subtraction still needs a scalar quotient estimate even for
remainder-only. A future private selection could skip storing/exporting the
quotient for remainder-only, or skip remainder export for quotient-only, while
retaining the same scratch reduction. Realizing that saving at the public
entries and at gcd/sqrt call sites changes code beyond the current authorized
kernel boundary and requires parent review. Merely adding unused variants here
would neither meet the full task acceptance nor demonstrate an allocation win.
No public delegation to virtual divideAndRemainder should be introduced: existing
subclass/null/identity prefixes must be preserved and retested first.

## Cost and unrun acceptance gates

The multi-limb kernel performs m+n normalization/export work and at most
m-n+1 passes of n multiply/subtract cells, plus at most two constant-size
corrections and one n-cell add-back per quotient position. Source complexity is
O(m+n+(m-n+1)*n), with O(m+n) bounded local storage. The prior source performed
bitwise division with repeated immutable shifts/adds/subtracts. This structural
change is a performance hypothesis, not a measured time or allocation reduction.

Before any acceptance: parent source review and a separate explicit runtime
grant; compilation on the pinned toolchain; the new common and pinned-JDK tests;
all retained public dispatch/null/identity/error and arithmetic fixtures;
gcd/modInverse/decimal regressions; JVM/JS/Wasm/macARM gates as applicable;
setup-inclusive allocation/time comparisons including one-limb, equal-size,
highly unbalanced and inverse-heavy workloads; a decision on result-selection
variants; and capacity analysis without speculative maximum-size execution.

The parent-reported historical JVM416/JS277/Wasm277/macARM277 passes predate
this source edit and are not validation of it. The parent-reported current
120218ms BigIntegerTest timeout, following a completed 2760-bit modular-inverse
message, only motivates investigating the divideStored hypothesis. It is not
sampled-stack evidence, a causal attribution, or a benchmark of this kernel.

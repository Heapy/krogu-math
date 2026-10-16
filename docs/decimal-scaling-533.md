# Decimal scaling, rounding and stripping (task 533)

The optimization preserves public overloads and the existing finite-precision
sum/product/division guard/sticky algorithms. It changes four private mechanisms:
a fixed small power table, compact integer rounding, publication of a single
decimal after precision reduction, and exact trailing-zero removal.

## Bounded powers

`tenPow` retains its negative and supported-range checks before lookup. The table
contains exactly 10^0 through 10^18, built with checked-by-construction positive
Long arithmetic: the largest multiplication produces 10^18 < Long.MAX_VALUE.
Exponents 19 and above use the original `TEN.pow` path. This is a bounded cache
coverage choice, not a measured algorithm crossover or a new accepted range.

The array is private, initialized completely before publication, and never
mutated. Entries are immutable BigIntegers; no input value or dynamically sized
power is retained. There are 19 references. ONE/TEN reuse existing constants;
the 17 other entries have 26 base-2^30 limbs altogether (104 payload bytes),
plus their objects/array headers and the table references. These are structural
counts, not a portable retained-heap byte figure. Publication follows each
target's normal object/companion initialization; no mutable lazy map or concurrent
cache update exists.

## Exact compact rounding

The compact branch in `roundedDivide` requires both runtime classes to be the
ordinary BigInteger class. Subclasses and a zero denominator keep the entire old
division path. `decimalCompactValue` certifies signed values whose magnitude is
at most Long.MAX_VALUE; Long.MIN_VALUE deliberately falls back. Thus `/`, `%`,
negation of a remainder and absolute divisor cannot overflow, including the
otherwise problematic MIN/-1 case.

Write a = qb + r, with q truncated toward zero and |r| < |b|. If r is zero,
the exact quotient is returned. Otherwise the existing `increment` policy uses
the result sign, quotient parity, and comparison of |r| with |b|-|r|. This is
equivalent to comparing 2|r| with |b| without doubling overflow. A nonzero
remainder implies |b| >= 2, hence |q| <= Long.MAX_VALUE/2. Adjusting q by its
result sign cannot overflow. The same eight rounding modes, including
UNNECESSARY's exception, remain authoritative. No floating conversion, decimal
string, approximate truncation or resource-sized intermediate is introduced.

## Single decimal publication

`roundValue` still returns immediately for unlimited precision and performs the
same initial virtual `precision()` observation for finite precision. If no
digits are dropped it returns the receiver. Otherwise the previous loop's
coefficient and scale remain local until its final decimal is constructed. Every
scale check still precedes the corresponding division. Subsequent coefficients
are ordinary immutable BigIntegers, so direct stored digit counts are identical
to the previous intermediate decimal's `precision()` calls. The constructor's
coefficient snapshot rules remain applicable at public boundaries.

The arithmetic rounding sequence is unchanged. For a truthful p-digit input and
target precision P>0, dropping p-P digits leaves at most P digits, except a carry
to 10^P. The only additional iteration then drops a trailing zero exactly. That
iteration cannot make a second inexact rounding decision. Existing scale checks,
including a carry crossing Int.MIN_VALUE, remain in their original order. The
implementation also retains the old loop for unusual initial subclass metadata;
it does not replace public callback observations with a new shortcut.

## Exact stripping

A compact coefficient removes trailing zeros with signed Long remainder/division
by 10; each successful step is exact and decrements the scale once. No negation
is needed. The boundary check for a remaining factor ten at Int.MIN_VALUE is
identical to the prior overflow policy.

A wide coefficient first makes the original one-digit divisibility probe. A
nonmultiple of ten still takes one division; an odd quotient ends immediately.
For an even quotient choose k = min(9, remaining scale distance, binary trailing
zeros). Compute only its remainder r modulo 10^k, avoiding allocation of a
speculative full quotient. If r=0, division by 10^k is proven exact. Otherwise,
the small signed remainder has |r|<10^9, and its decimal trailing-zero count z<k
is exactly the remaining coefficient's decimal trailing-zero count. A zero z
ends stripping; otherwise a quotient-only division by 10^z publishes exact work
and ends stripping. Thus short tails never attempt multiple failed full-width
quotients. Long tails take one remainder-only and one quotient-only pass per
successful chunk, a deliberate CPU/allocation tradeoff measured separately.

10^9 < 2^30 fits one integer radix word; the remainder is certified compact.
Divisibility by 10^k requires at least k binary factors, and exact division
reduces their count by k. Every chunk uses a Long-computed scale distance and
preserves value by dividing the coefficient and subtracting the same exponent.
No floor can be crossed. Remaining overflow at Int.MIN_VALUE uses the same
final remainder check. No truncation is used by stripping.

The first candidate regressed on a wide one-zero tail (2,320 allocated bytes/op
versus 704 baseline, roughly three times observed latency). Its three forks,
source and script remain under `diagnostics/decimal-scaling-533/first-measurement`.
An intermediate two-probe guard fixed that case but regressed on a high-binary-
factor two-zero tail (2,296 versus 1,024 bytes/op); that source and affected fork
remain under `two-probe-measurement`. These observations motivated the general
remainder-based selection. No performance improvement is inferred solely from
its proof; the final implementation requires fresh affected-case evidence.

## Acceptance evidence

`DecimalScaling533Test` prepares literal halfway/sticky/carry results, signed
cohorts, compact boundaries, wide zero tails, cache boundary exponents,
Int-scale boundaries, unlimited/same-scale identity, cancellation and public
precision/legacy setScale dispatch. Its expectations do not call the private
rounding implementation. `DecimalScaling533JvmTest` compares public JDK
round/setScale/strip results and arithmetic failures for deterministic signed
compact/wide values and both extreme scales. Existing scale, context, dispatch,
null and preferred-scale suites remain required.

The final unfiltered local four-target run passed 1,823 tests: JVM 605, JS 406,
Wasm 406 and macOS ARM64 406. The independently reviewed matched diagnostic
records actual time, JVM allocation and sampled heap-pool upper bounds, including
preserved adverse intermediate results and the final selector correction.
See [validation and measurement report](../diagnostics/decimal-scaling-533/README.md)
for source/artifact identities, raw results, exact commands, composition limits
and platform/measurement caveats. Task 527's general baseline harness stays
separate; no timing threshold is asserted in correctness tests.

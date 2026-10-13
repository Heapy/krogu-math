# Numerical arguments

These arguments describe the independent common implementation in
`BigInteger.kt` and `BigDecimal.kt`, together with portable JDK fixtures. They
cover integer packing, decimal conversions, roots and powers.

## Binary floating-point rounding

The relevant format parameters are precision `p=24`, upper bit-length `E=128`
for Float, and `p=53`, `E=1024` for Double. These follow from
[JLS 4.2.3](https://docs.oracle.com/javase/specs/jls/se25/html/jls-4.html#jls-4.2.3).
Java numeric conversion uses nearest rounding with even ties
([JLS 5.1.2–5.1.3](https://docs.oracle.com/javase/specs/jls/se25/html/jls-5.html#jls-5.1.2),
[JLS 15.4](https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.4)).
The argument assumes the target's primitive arithmetic and conversion implement
that policy; portable bit-pattern comparisons check the exercised cases on each
supported target.

For a positive magnitude `n` with bit length `b`, zero is handled separately.
If `b>E`, then `n>=2^E`, so signed infinity is the correct result. Otherwise set
`s=max(0,b-p)`. Reading the highest bits directly from the normalized limbs gives
`q=floor(n/2^s)`; at most 53 bits are read. The discarded part is
`r=n mod 2^s`, which need not be materialized.

When `s=0`, the integer is exact. When `s>0`, the guard bit says whether
`r>=2^(s-1)`. Sticky bits say whether any lower discarded bit is set. The private
lowest-set-bit index is less than `s-1` exactly when such a bit exists. Therefore
the increment rule `guard && (sticky || q is odd)` has three outcomes:

- Below the midpoint, retain `q`.
- Above the midpoint, increment `q`.
- At the midpoint, choose the even significand.

After rounding, `q<=2^p`, so it fits Long and converts exactly to Double.
Also `s<=E-p`: at most 104 for Float and 971 for Double. The power `2^s` is exact
in Double. `FloatingPowerTest` checks all powers from -1074 through 1023 against
its independently encoded IEEE exponent bits on every target. Multiplication
produces an exact dyadic value with at most `p`
significand bits, except when the Double result itself overflows. A carried
`q=2^p` is a power of two and also exact. Consequently a finite Float result is
already representable when the final Float cast occurs; that cast introduces
no second rounding.

The greatest finite magnitude is `(2^p-1)*2^(E-p)`. Its overflow midpoint is
`2^E-2^(E-p-1)`. At that point the retained significand is odd and the guard is
set, so the carry selects infinity. Below it the result remains finite. This
covers both the largest finite value and the carry into the next exponent.
Restoring the integer sign preserves the symmetric rounding result. Nonzero
integer magnitudes cannot underflow or produce subnormals; integer zero returns
positive zero.

For a nonzero original magnitude below `2^63`, the implementation preserves
JDK's observed virtual longValue call. Double converts that returned Long once.
Float reconstructs its exact integer magnitude and applies the rule above with
virtual dispatch disabled. Thus a replacement Long just above a Float midpoint
is not first rounded to a Double midpoint. Zero and original magnitudes at or
above `2^63` bypass that virtual call, as the Java trace fixtures demonstrate.

## Exact integral conversions

For an ordinary stored value, fitting signed width `w` is equivalent to
`bitLength()<=w-1`: positive bit length describes its magnitude; negative bit
length describes `abs(n)-1`. This includes the asymmetric negative endpoint.

The preliminary magnitude guards cannot reject a representable value: int's
negative endpoint has 32 magnitude bits and long's has 64. Short and byte use
the broader 32-bit preliminary guard and their own final signed-width test.
After the checks, the corresponding primitive conversion is exact for an
ordinary object. Subclasses can deliberately change these public methods;
the oracle constrains the observed guard order and delegation instead of
assuming overridden methods remain truthful.

## Hash packing

The observed JDK hash folds unsigned 32-bit magnitude chunks, most significant
first, with multiplier 31, then applies the sign. Our magnitude uses 30-bit
limbs. For chunk index `i`, its starting bit is `32*i`, its source limb index
is `floor(32*i/30)` and its offset is `32*i mod 30`. This offset is always even
and at most 28. Two consecutive limbs therefore contain at least
`30-offset+30>=32` needed bits. Shift the first limb right by the offset, shift
the next limb left by `30-offset`, join them and mask to 32 bits. A missing next
limb supplies zero.

The intermediate is at most 60 bits, so it fits signed Long. The chunk index
calculation uses Long even at the supported magnitude limit. Each fold's Long
arithmetic fits before conversion to Int; conversion and final sign application
implement arithmetic modulo 2^32. This is independent of our limb layout and
requires constant auxiliary storage without abs/testBit calls or intermediate
BigInteger objects. Both signs, overflow of the hash accumulator and every alignment are
compared with public JDK outputs. This is an argument for packing equivalence;
the JDK's particular hash recurrence is constrained by the oracle, rather than
specified by the Object.hashCode contract.

## Exact decimal addition and subtraction

For stored operands x=c*10^-s and y=d*10^-t, choose u=max(s,t).
The integers C=c*10^(u-s) and D=d*10^(u-t) then give
x+y=(C+D)*10^-u and x-y=(C-D)*10^-u. Both scale distances are
nonnegative, and Long subtraction represents their full Int-scale domain.
A zero coefficient remains zero for every distance, so its scaling can be
skipped without constructing the corresponding power of ten. Stored coefficient
signs determine this optimization; an overridden public signum is not numerical
input to exact alignment.

This argument assumes supported intermediate sizes and correct integer
arithmetic. It does not establish all allocation/range behavior, finite-context
sticky-tail rounding or virtual callback semantics. Separate public-JDK fixtures
constrain which overrides exact and context operations invoke.

## Decimal rational conversion

For an ordinary BigDecimal, separate its sign and write its absolute value as
`n/d`, where `n` and `d` are positive integers. A negative decimal scale multiplies
the coefficient by a power of ten; a positive scale supplies that power in the
denominator. Zero returns positive zero, consistent with the absence of a signed
decimal zero. Decimal adjusted exponents outside [-400, 400] are respectively
above both formats' overflow thresholds or below both half-minimum-subnormal
thresholds. The early infinity/zero results therefore preserve rounding and sign.

The bit-length difference `b=bitLength(n)-bitLength(d)` places the binary
exponent in `{b-1,b}`. One exact shifted comparison selects `e=floor(log2(n/d))`.
If `e` exceeds the format's greatest finite exponent, the result is infinity.
Otherwise the rounding quantum is `2^k`, with
`k=max(e, minimumNormalExponent)-(p-1)`. Scaling the numerator or denominator
by a power of two gives an exact integer division. HALF_EVEN division rounds
`(n/d)/2^k` to an integer `q`, including ties, without decimal approximation.

For normal values, `q` has at most p significant bits, apart from a carry to
the exact power `2^p`. For subnormal values the quantum stays fixed at `2^-149`
for Float or `2^-1074` for Double. Consequently `q*2^k` is representable in the
target format, including a carry into the first normal value. At underflow,
q=0 preserves the original sign through multiplication; at overflow the rounded
carry selects infinity. q fits Long and converts exactly to Double. The scaling
power itself is exactly representable in Double. For Float, the product is
already an exact Float value before the final cast, so no double rounding occurs.

Compact coefficients in the open interval (-2^63, 2^63) bypass virtual signum
at scale zero, or at scales within +/-10 for Float and +/-22 for Double when
casting the primitive coefficient back to Long reproduces the stored Long.
The comparison deliberately includes primitive-to-Long saturation at Long.MAX_VALUE,
as constrained by the public-JDK subclass oracle. Stored zero always returns
positive zero. Other paths call virtual signum exactly once and multiply the
rational magnitude by its return value; no virtual precision call participates.
These dispatch rules have 594 portable result-and-trace outcomes.

Constructor coefficient subclasses are normalized through their virtual byte
encoding into an ordinary BigInteger; ordinary coefficient identity is retained.
This argument assumes exact integer arithmetic and does not settle the practical
allocation limits for very large cancelling coefficients and
scales. The portable tests independently check every binary scaling power from
2^-1074 through 2^1023 by IEEE bit encoding, and compare signed exact midpoint
neighbors with the pinned JDK. They exercise both retained parities, significand
carry, underflow to signed zero, the subnormal/normal transition and overflow.

## Decimal square-root rounding

For positive ordinary input `x=c*10^-s`, let `E=floor(log10(x))` and
`R=floor(E/2)`. Its square root has adjusted exponent R. At positive precision p,
the result quantum is `10^-t`, where `t=p-1-R`. Scaling the radicand by
`10^(2*t)` represents it as an exact nonnegative rational `n/d`. Computing
`r=floor(sqrt(floor(n/d)))` gives `floor(sqrt(n/d))`: the integer square r^2
cannot lie strictly between floor(n/d) and n/d. Thus r brackets the scaled root
between r and r+1 without approximating the irrational value.

The equality `r*r*d==n` detects an exact result. Otherwise compare `4*n` with
`(2*r+1)^2*d`. Squaring is monotone for these nonnegative values, so this is
exactly the comparison with the midpoint r+1/2. The rounding policy chooses
between r and r+1; HALF_EVEN uses the parity of r at equality. A carry into
10^p is an exact decimal power and is adjusted to p digits by the final round.
Removing trailing zeros toward the preferred scale changes representation,
not value. Exact/unnecessary modes first remove decimal trailing zeros and
require an even normalized scale and an integer-square coefficient, followed by
the requested precision check.

For a positive normalized coefficient c, at least one of the prime-factor
exponents v2(c) and v5(c) is zero, since c is not divisible by ten. A rational
square c*10^-s requires both v2(c)-s and v5(c)-s to be even. An odd s would
therefore force both v2(c) and v5(c) to be odd and positive, contradicting
normalization. For even s, the decimal factor is already a rational square;
the remaining condition is that c itself is an integer square. Zero is handled
separately. This establishes the numerical guard for ordinary normalized inputs;
returned subclasses and their virtual getters are constrained by separate JDK
oracle cases rather than by this argument.

The public [Java SE 25 sqrt contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#sqrt(java.math.MathContext))
specifies preferred scale s/2, error bounds and exact-mode failures. The pinned
oracle additionally constrains concrete representations and errors. The 2,592
new cases square decimal midpoints exactly, perturb the radicand on both sides,
and vary parity, carry, precision through 129 digits, decimal exponent and input
trailing zeros across all eight rounding modes. This argument assumes valid
scale/intermediate sizes and ordinary methods; it does not establish the whole
scale-overflow or subclass-dispatch behavior of sqrt.

## Decimal powers and working precision

The public [pow contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#pow(int,java.math.MathContext))
describes a single accumulator, repeated squaring, expanded precision based on
the decimal digit count of the exponent, an optional reciprocal and final
rounding. Scanning exponent bits from most to least significant, the accumulator
represents the power for the scanned prefix. Squaring doubles that prefix;
multiplication by the base adds the next set bit. Every step uses the expanded
context, rather than computing an exact power and rounding only once.

A seeded JVM boundary matrix compares 13,024 outcomes for positive and negative
exponents, adjacent powers of two, all modes and precision through 65 digits.
The pinned runtime also exposes Int overflow of the expanded precision as
MathContext's IllegalArgumentException (Digits < 0), not a decimal-scale error.
There are 128 portable cases at this boundary; JVM tests additionally compare
the error message. Base one avoids enormous allocations in the successful
near-Int.MAX_VALUE precision cases. These observations do not prove every
intermediate-size limit, subclass call sequence or every exponent/input pair.

## Decimal exact primitive conversion

For an ordinary nonzero decimal with coefficient precision d and scale s, let
`k=d-s`, computed in Long. If k<=0, its absolute value is strictly between zero
and one, so exact conversion requires a fractional-part error. If k>19, its
absolute value is at least 10^19, exceeding signed Long range. Otherwise exact
rescaling to zero either rejects a nonzero fractional part or supplies an integer.
An integer of fewer than 19 decimal digits fits Long. At 19 digits the signed
bitLength rule above, bitLength<=63, distinguishes both signed endpoints from
overflow. Zero and compact scale-zero values have separate observed fast paths.

Narrow conversions first call virtual longValueExact once, then compare the
returned Long with their signed width endpoints. This preserves both fractional
versus overflow precedence and replacement-method behavior. The 560 ordinary
cases and 27 replacement-Long cases compare values and exact messages. Another
72 traces constrain public signum/precision calls, legacy-Int setScale dispatch
and canonical formatting. The long guards deliberately make separate virtual
precision calls, as observed in the pinned JDK.

Canonical text derives digit count from stored coefficient text. Its private
plain layout avoids virtual toPlainString. The reference bypasses virtual signum
at scale zero and for nonnegative compact coefficients below Int.MAX_VALUE at
scale two; other tested layouts call it once. There are 264 replacement-getter
text-and-trace cases around these boundaries. These observations concern fresh
single calls and do not prove repeated-call caching or every formatter/subclass.

## Evidence and remaining scope

The main portable oracle includes 1,728 signed midpoint-neighbor Float/Double
outcomes. It varies retained parity, carry, exponents across the 30-bit limb,
32/64-bit conversion boundaries, and the finite/infinity edges. Another 64
cases replace virtual longValue, including Long values where an intermediate
Double would change Float rounding. The exact-conversion matrix has 1,248
cases covering signed endpoints, both sides of the magnitude guards, replaced
bitLength, exact error messages and primitive-call traces. Separate power tests
check every scaling factor and positive/negative integer powers through 2^1100
against IEEE encodings, including the finite/infinity transition.
Hash fixtures add 5,760 signed power/neighbor results through two full 480-bit
alignment periods and 350 dense-byte results through 65,536 bytes. Structured
common inputs are encoded independently as byte arrays; all common hash inputs
use subclasses whose abs/testBit throw if invoked.

The decimal midpoint campaign adds 1,296 Float/Double results constructed from
exact dyadic rational midpoints and decimal epsilon offsets.

These arguments complement the fixtures; they do not prove every compiler or
runtime implementation, complete the decimal subclass audit, exercise multi-gigabyte
objects, or settle the full java.math compatibility audit. Those requirements
remain tracked in `COMPATIBILITY.md`.

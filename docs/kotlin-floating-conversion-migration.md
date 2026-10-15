# Kotlin floating conversion migration

Explicitly import the extensions as well as the result and context types:

```kotlin
import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.MathContext
import io.heapy.krogu.math.RoundingMode
import io.heapy.krogu.math.toBigDecimal

val single: BigDecimal = 0.1f.toBigDecimal()
val double: BigDecimal = 0.1.toBigDecimal()
val rounded: BigDecimal = 1.25f.toBigDecimal(
    mathContext = MathContext(2, RoundingMode.HALF_EVEN),
)
```

The four new overloads have Float/Double receivers, explicit BigDecimal results,
and either no parameter or the required parameter `mathContext: MathContext`.
Importing BigDecimal alone does not select krogu extensions on JVM. The prepared
tests live in a separate migration package, explicitly import the extensions,
and declare own result types, including named-context calls.

## Public contracts and implementation provenance

Kotlin 2.4 specifies conversion through decimal text for
[Float and Double toBigDecimal](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/to-big-decimal.html),
with the context overload applying the specified precision and mode.
[Float.toString](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Float.html#toString(float))
defines the shortest admissible canonical decimal, except that minimal length
one admits both lengths one and two. Selection minimizes distance to the binary
value, with even canonical significand breaking a tie. Its notation switches at
adjusted decimal exponents -3 and 7, retaining `.0` where specified.
[Double.toString](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Double.html#toString(double))
defines the corresponding binary64 contract.
[BigDecimal.valueOf(Double)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#valueOf(double))
uses that canonical text. The exact binary BigDecimal(Double) constructor is a
different operation. For example, 0.1f converts to `1@1` (coefficient@scale),
while the widened binary32 value's Double text gives `10000000149011612@17`.
Nonfinite inputs raise NumberFormatException; required rounding with UNNECESSARY
raises ArithmeticException.

Only public documentation/signatures and this project's own source were read.
No OpenJDK, Kotlin stdlib or third-party numerical implementation source was
read or copied. The archived own task525 NumericExtensions algorithm informed
the asymmetric-boundary regression selection. It used a nearest candidate plus
neighbors and conversion-back membership tests. The current own
DoubleFormatting algorithm supplied a reviewed grid/interval design to adapt,
with its [existing argument](canonical-double-contract.md). Neither file was
transplanted as a private Double formatter. The new Float code decodes raw bits
and uses a fixed exact integer lattice for midpoint membership and distance.
Double overloads delegate the existing public own factory and its rounding;
DoubleFormatting.kt is unchanged.

## Whole binary32-domain selection argument

This is an independent mathematical argument for the search over every finite
binary32 input, conditional on the existing BigInteger operations, decimal
construction/formatting/rounding and primitive raw-bit API satisfying their
contracts. It is not an exhaustive executed comparison or a proof of those
underlying operations.

### Exact raw-bit decoding and intervals

For a positive finite raw magnitude b, let f be its 8-bit exponent field and t
its 23-bit fraction. In units of 2^-149 its exact integer is

```
I(b) = t                              when f = 0
I(b) = (2^23 + t) * 2^(f - 1)        otherwise.
```

For normals this follows by multiplying `(2^23+t)*2^(f-150)` by 2^149.
Subnormals are exactly `t*2^-149`. Thus adjacent magnitudes are ordered by b,
including the subnormal/normal join. I(0)=0. At maximum finite b, b+1 is used
only as a virtual successor: f=255,t=0 gives 2^128, exactly one upper ulp beyond
the maximum. The public overflow threshold is the midpoint to this value.

Let F=5^150. At common decimal scale 150, the exact magnitude and boundaries
are the following **integers**:

```
M = 2*I(b)*F
L = (I(b) + I(b-1))*F
U = (I(b) + I(b+1))*F.
```

Multiplying each by 10^-150 gives m, (m+previous)/2 and (m+successor)/2,
respectively. The round-to-nearest binary interval R contains both endpoints
iff b's low bit is zero; otherwise both are excluded. This is binary
significand parity, including subnormals and the normal join. The maximum
finite significand is odd, excluding its overflow midpoint. The minimum
subnormal is odd, excluding its zero midpoint. At other normal powers of two,
the predecessor gap is half the successor gap; the two independently built
bounds retain that asymmetry.

No floating arithmetic, decimal parser or platform formatter selects the
decimal. The only primitive operation needed for selection is Float.toBits.
In particular the exact center is decoded from bits rather than a widened
Float carrier. Negative inputs reflect the positive interval, preserving
lengths, distances and canonical parity. Zero is handled separately as `0.0`;
discarding its sign preserves the resulting decimal's coefficient and scale.
All magnitudes with exponent field 255 are rejected before the interval search.

### Grid coverage and nearest admissible candidates

Let E be the adjusted decimal exponent of m. With M's decimal digits D,
`E = D.length - 151` exactly, since m=M*10^-150. The search uses budgets
n=2,...,9. In decimal band e=E+offset, the budget-n grid consists of

```
c * 10^(e-n+1),   10^(n-1) <= c <= 10^n - 1.
```

Coefficients may have trailing zeros; stripping them gives the canonical
decimal. Every canonical decimal of length at most n in a band has such an
encoding. The unit at scale 150 is `10^(150+e-n+1)`.

The floor coefficient of m on this grid equals the first n-offset digits of
D, padded with zeros if needed: since D.length=E+151, dividing M by the unit
shifts exactly D.length-(n-offset) decimal places. The implementation tests
this coefficient and its successor, each clipped to the band's coefficient
range. These are the closest points in the band on either side of m, or the
nearest endpoint when m is outside the band.

If any grid point on one side of m belongs to R, the nearer grid point on
that side belongs too: it is between that point and m, and R is connected
and contains m. This remains true when R has open endpoints. Thus these two
candidates suffice both to detect membership and to minimize distance among
admissible grid points. Testing only a rounded nearest point would not suffice
at asymmetric binary boundaries. Clipping also retains the last candidate
below a decimal carry even if both unbounded bracketing points exceed a band.
Integer comparison with L and U, respecting their inclusion flags, is exact
membership. Absolute integer difference from M is exact distance times the
same positive factor 10^150. Equal distances use parity after stripping zeros,
matching canonical rather than padded coefficient parity.

For any positive finite binary32, its R lies within [m/2,2m), with the widest
relative interval at the minimum subnormal. Hence its possible decimal bands
are only E-1,E,E+1. Budget two searches all three. Its padded grids represent
both canonical lengths one and two, implementing their joint selection rule.
If this budget fails, no power of ten belongs to R. As a connected interval
containing m, R cannot then contain a decimal in a different exponent band:
the intervening power of ten would also belong. Consequently later budgets
need only band E. At their first successful budget, a candidate's canonical
length cannot be shorter, since an earlier pass would have detected it.
This covers decimal carries, including intervals crossing a decade among the
smallest subnormals.

### Nine-digit sufficiency and fixed bounds

Within band E, the nine-digit grid spacing is h=10^(E-8). A nearest grid
point has error at most h/2, hence relative error at most 5*10^-9.
For binary32 the smaller distance from m to either rounding boundary is at
least m*2^-25. At a normal power of two above the minimum normal, the smaller
half-gap has precisely this relative size. The minimum normal has equal gaps
and relative half-gap 2^-24; inside a binade the equal half-gaps have at least
the stated lower bound. Subnormals and the maximum finite overflow boundary also
satisfy this bound. Since 5*10^-9 < 2^-25, the nearest nine-digit point is
strictly inside R. If nearest rounding carries to the next decimal band, it
is a power of ten already covered at budget two. Therefore the loop must
return by budget nine for every positive finite raw input.

The magnitude exponent E ranges from -45 to 38. Grid scales are at most 53
(budget nine at E=-45); scale 150 therefore aligns every searched decimal
without division or rounding. The finest unit exponent `150+E-8` is in
[97,180]. Nine stored units, each ten times its predecessor, supply index
`9-n+offset`: the budget-two offsets use indices 6,7,8 and the later budgets
use indices 0 through 6. All array indices and powers are nonnegative and
bounded. Prefix lengths are positive (one through nine). Each pass examines
two candidates per band: six at budget two and two at each of the remaining
seven budgets, at most twenty candidate visits. I(b+1) needs at most 278 bits;
5^150 needs 349 bits. Centers, bounds, grid coefficients times units and
distance intermediates stay below 10^190 (632 bits). The additional tenth
unit multiplication performed while building the nine-unit list is bounded
by the same limit. There are no input-sized loops or giant numerical inputs.
These are mathematical size/work bounds, not measured performance claims.

### Rendering and context

The chosen positive decimal is stripped. Its adjusted exponent selects plain
notation in -3..6; otherwise the renderer places a decimal point after its
first digit and writes the exponent. Integral plain text and a one-digit
scientific mantissa receive `.0`. Prefixing the sign completes canonical text.
Parsing with the own BigDecimal String constructor preserves its coefficient
and scale; the context constructor rounds that selected decimal. For Double,
the existing valueOf factory already parses its canonical text, and applying
round(mathContext) gives the same decimal rounding step. Neither path uses
the exact binary constructor as the extension result.

## Prepared verification and retained historical evidence

The new common suite has six methods: literal/source-selection and scale;
signed raw boundaries including the three historical asymmetric failures;
small subnormals and one/two-digit selection; widened/exact binary contrasts
and the existing Double asymmetric regression; all eight rounding modes with
independent signed expectations, carry and unlimited precision; and nonfinite
errors. A decimal tie at binary32 2^-12 has two admissible eight-digit values
at distance 5E-12; its literal expected coefficient is the even 24414062.
All independent assertions check coefficient and scale separately.

The four prepared JVM methods retain independent literal controls and pair
actual public Kotlin stdlib calls (aliased imports) with krogu calls. They also
compare public JDK text construction. Float families include all 254 normal
binary powers and adjacent raw bits, all 23 subnormal powers and neighbors,
small subnormals, signs and maximum finite neighbors. Double families cover
selected exponent boundaries, subnormal powers, signs and the evolved factory.
The context matrix covers precisions 0,1,2,7,9,17 and all eight modes, finite
boundaries and nonfinite errors. These are prepared source, not observed
outcomes; no new numerical expectations were generated by a reference campaign.

Source/proof phase only: **compilation and all runtime tests are UNRUN**.
No Gradle, compiler, Native linker, javac, harness, benchmark, JDK/port probe,
script or reference campaign was executed. A symbolic certificate is optional
and was not introduced or run. The old task525 worktree, failed terminal
evidence and both enabled JS Number-dispatch tests remain untouched. No claim
of full task525 acceptance, universal executed equality, or erased Number
dispatch compatibility follows from this slice. Task557 remains separate.
The parent owns later compiler/runtime gates, serial integration and closure.

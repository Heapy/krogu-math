# Floating conversions: current exhaustive-class proof binding (543)

This is a finite source-only composition of existing own arguments. No
production source, test, fixture, generator, timeout or toolchain was changed.
No script, compiler, numerical helper, certificate, probe, test, build or
benchmark was executed for this milestone. Historical execution records below
remain observations of their named artifacts, not fresh results of this memo.

The task543 acceptance route permits a checkable argument over exhaustive input
classes. The arguments below quantify over all ordinary supported stored values
or all primitive bit patterns; they do not require enumeration of all `2^32`
Float patterns. They are conditional on the stated leaf contracts and do not
prove every integer operation, runtime or platform implementation.

## Exact current source and assumptions

| Own source | SHA-256 |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` | `b2fb23547d97f3f7650b6d7dcf4ad53939efb3abac97d9d152376e67dd6685f5` |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt` | `1c1559c44de0a51bfcd7df0c325dfb9de22e7b6a4588b6dcb612370dc850fa55` |
| `src/commonMain/kotlin/io/heapy/krogu/math/DoubleFormatting.kt` | `9b2a1b8cb1d40a2fdc7db5110b6a8c4e398da0562d5c3d6b02cf928f3c74433f` |
| `src/commonMain/kotlin/io/heapy/krogu/math/FloatFormatting.kt` | `ba55514c181e52f20ed63fcb2634621f66cea3cc4db084e77cf2df71d1dcf77e` |
| `src/commonMain/kotlin/io/heapy/krogu/math/NumericExtensions.kt` | `20fdf7c5c76ed4207c03859b4cb60247a16e2ec4337ee674c3a85ce6101e5123` |

The initial accepted composition bound BigInteger `df55cb8c…`. The subsequent
[parser-range repair](integer-parser-range-559-proof.md) adds only a conservative
guard inside its private string parser; all conversion/division methods and
the remainder of that source are unchanged. Its independently reviewed bound
cannot reject a supported value. In particular the fixed-size canonical text
and prefix coefficients used here take the unchanged accumulation path. The
table records that later source hash without changing the conversion argument
or relabelling earlier full-suite CI as execution of the parser repair.

The subsequent [prime-search range policy](prime-search-range-559-proof.md)
changes only the two generation overloads and successor search. Its unchanged
region hashes bind every conversion and division method to the parser baseline.
Conversions do not enter prime generation or successor search. The table now
binds that source revision; neither this proof nor historical conversion test
results are relabelled as a full-suite execution of the prime-search change.

The later [power-of-two radix formatter](power-of-two-radix-559-proof.md) changes
only toString(radix) and its private helper for bases2/4/8/16/32. Conversion and
division methods remain unchanged; canonical decimal and prefix text follows
the existing base10 path. The table binds that successor without changing the
numerical argument or relabelling historical conversion CI.

The subsequent [inexact sqrt normalization](sqrt-normalization-579-verification.md)
changes only sqrt's normalization branch and adds a separate internal helper.
Every conversion and its digit/prefix/division leaves remain byte-for-byte
unchanged. The table binds that successor; the conversion proof is unchanged,
and the new local campaign is recorded separately from historical conversion CI.

The later [contextual product repair](decimal-context-product-580-verification.md)
changes only contextual multiplication, adds a nonvirtual integer bridge and a
separate raw helper. Every conversion method and its digit/prefix/division
leaves remain byte-for-byte unchanged. The table binds that successor without
changing this proof or relabelling historical conversion CI; its finite local
execution evidence is recorded separately.

Assume canonical immutable stored magnitudes, exact integer arithmetic and
decimal digit/prefix/formatting primitives, valid fixed contexts, and successful
required allocations. Primitive raw-bit APIs, nearest-even primitive conversion
and arithmetic, signed zero, infinities, and the powers of two used below must
satisfy their IEEE contracts on the target. Existing portable tests exercise
these assumptions; mathematical composition is not a proof of every compiler's
code generation or every platform primitive.

The current division dependencies are the
[normalized task530 kernel](division530-proof.md) and
[output-selection argument](division530-selection-proof.md). The current
decimal metadata dependency is [task528](../DECIMAL_METADATA_COMPARISON.md).
The current BigInteger source contains modular-context infrastructure, but
conversion does not enter that modular path. This memo neither depends on nor
adopts the separate [task538 exact-power candidate](modpow-power-bound-538-pilot.md),
whose retained pilot reports it unadopted.

## Exhaustive class inventory

| Family | Exhaustive classes and existing argument |
| --- | --- |
| Integer to Float/Double | Stored zero; nonzero original magnitude below `2^63`; larger finite-range magnitudes; above-range magnitudes. Guard/sticky classes distinguish below/at/above midpoint, retained parity, carry, overflow and both signs. [Integer rounding](../NUMERICAL_ARGUMENTS.md#binary-floating-point-rounding). |
| Decimal to Float/Double | Stored zero; safe early overflow/underflow; bounded exact coefficient; longer coefficient with zero/nonzero discarded suffix. Every positive IEEE threshold, including subnormal zero, normal join and infinity, is covered by the [prefix-cell argument](../DECIMAL_BINARY_CONVERSION.md), followed by [exact ratio rounding](../NUMERICAL_ARGUMENTS.md#decimal-rational-conversion). |
| Exact Double construction | Nonfinite; both zeros; finite nonzero subnormal/normal fields; both signs; positive/negative binary exponent and removable powers of two. Direct decomposition below binds `fromDouble`. |
| Canonical Double factory | Nonfinite; both zeros; all positive finite binary64 intervals and their sign reflections. [Candidate completeness](canonical-double-contract.md#selection-argument), including asymmetric binary boundaries, one/two-digit union, decimal carries and seventeen-digit sufficiency. |
| Canonical Float extensions | Exponent-field 255; both zeros; every positive finite binary32 lattice index and its sign reflection. [Whole-domain selection](kotlin-floating-conversion-migration.md#whole-binary32-domain-selection-argument), with nine-digit sufficiency and fixed bounds. |
| Valid-context construction/extensions | Precision zero; already-fitting values; exact/inexact discarded suffix; all eight signed rounding modes; ties and a possible carry. [Constructor composition](contextual-constructor-rounding-549-proof.md), with each entry's zero cohort retained as specified below. |

No row infers the whole domain from its finite regression count. The proofs
partition the mathematical domain; the tests provide separate implementation
and platform evidence for those partitions.

## Integer extraction and primitive assembly

`BigInteger.floatingValue()` matches the existing guard/sticky argument. For a
nonzero original magnitude with at most 63 bits, it calls virtual `toLong()`.
Double converts the returned Long once. Float reconstructs an exact base
integer from that Long and re-enters with dispatch disabled; even a replacement
`Long.MIN_VALUE` therefore cannot recurse or undergo Long-to-Double-to-Float
double rounding. Zero bypasses this callback; original magnitudes with at least
64 bits use stored limbs directly.

For the stored path, let `b` be the magnitude bit length, `p` be 24 or 53 and
`E` be 128 or 1024. If `b>E`, infinity is already correct. Otherwise
`s=max(0,b-p)` and the loop extracts exactly `floor(n/2^s)` in at most `p`
iterations. Its bit indices are nonnegative and below `b`, hence inside the
canonical limb array. At most 53 bits are accumulated in Long. The guard and
private lowest-set-bit observation classify the discarded suffix exactly.
After the possible increment the significand is at most `2^p`, still exactly
representable in Double. The scaling exponent is at most 104 or 971, so it
cannot introduce an unsupported integer magnitude or an unbounded power.

Below the finite/infinity midpoint the assembled result is finite; at the
midpoint the odd retained significand carries to infinity. Integer inputs have
no nonzero subnormal/underflow class. The final Float cast is exact for a finite
already-rounded binary32 value; the infinity case is also preserved. Signs
reflect the positive result, and integer zero produces positive zero. This is
an all-magnitude rounding proof, not an outstanding need for arbitrary large
random integer conversion campaigns.

## Current decimal prefix and division composition

`decimalDigitLowerBound()` and `decimalMagnitudeDigits()` separate the stored lower digit
estimate and the exact digit-count helper added for task528. Prefix extraction
uses the **lower estimate**, not `decimalMagnitudeDigits()`. Task562 proves
`E<=D<=E+1` for that estimate over supported stored bit lengths; task528 reuses
and checks its maximal bound while avoiding an unsupported exact-count
threshold. Their shared bound is `D<=646456993`.

For prefix budget `B` (159 for Float, 1093 for Double), the actual code chooses
`t=max(0,E-B)`. If positive, `t<=D-B<=D-1`, so `10^t<=n` and
`t<=646456992`. The power is supported whenever the input is, subject to the
existing power kernel and allocation contracts. No `10^D` is requested.

The positive working view is an exact base integer. At the `decimalBinaryPrefix()`
both-output call, `divideStored` enters `selectStoredDivision` with
`needQuotient=true, needRemainder=true` (`:93-94`). The current task530 proofs
give `n=q*10^t+r`, `0<=r<10^t`, with primitive/index bounds and input-sized
normalized scratch storage. The [selection inventory](division530-selection-proof.md#call-site-inventory-and-choices)
explicitly lists this both-output caller. Neither a quotient-only nor a
remainder-only placeholder is consumed here. If `t=0`, the unchanged branch
supplies the exact working magnitude and zero remainder directly.

The quotient has at most `B+1` decimal digits. Only this quotient is formatted.
Let `j=max(0,digits(q)-B)`, which is zero or one, and write `q=P*10^j+u`.
Then `n=P*10^(t+j)+(u*10^t+r)`. The suffix is nonzero exactly when `u!=0`
or `r!=0`, precisely the two observations used by `sticky`. Thus the returned
prefix, full digit count and discarded count are exact; `t+digits(q)=D` and
`t+j<=D` also fit Int. An arbitrarily remote nonzero tail remains visible.

Task562's endpoint/interior-cell proof then applies unchanged. Every IEEE
rounding midpoint has at most `B` significant decimal digits. A cell of that
grid has no midpoint in its interior. A zero suffix keeps the exact endpoint;
a nonzero suffix chooses `(10P+1)` on the next decimal grid, strictly inside
the same cell. The original and surrogate therefore have identical rounding
results for both parities, including all transitions and signed underflow.

The historical task562 binary-division paragraph is not a proof of the current
radix kernel. The current task530 contracts above are the replacement
dependency. The large prefix divisor and scratch allocation remain task548
obligations; removing the full-string ceiling did not prove practical maximum
allocations or bounded running time.

## Decimal ratio and bounded final rounding

`BigDecimal.kt:788-827` uses the stored lower estimate for safe early exits,
then the exact returned digit count for the remaining adjusted exponent. All
original coefficient/scale differences use Long; an Int coefficient digit count
and Int scale cannot overflow it. Subsequent surrogate scale arithmetic also
uses Long. For a retained adjusted exponent in `[-400,400]`, task562 bounds
the surrogate scale by `[-400,B+399]` when `D<=B`, or `[B-401,B+400]` otherwise.
No final decimal power exceeds 1493 for binary64, regardless of the original
coefficient length or scale.

The exact positive ratio `n/d` is rounded on a binary grid. The bit-length
difference places its exponent in two adjacent possibilities; one shifted
comparison selects the correct one. The grid quantum is fixed at `2^-149` or
`2^-1074` for subnormals and follows the binade for normal values.
`roundedDivide` (`BigDecimal.kt:957-963`) uses exact quotient/remainder and
HALF_EVEN. Its public pair call is on newly created base integers and reaches
the same current task530 both-output kernel. A zero remainder returns exactly;
otherwise comparison of twice the full remainder with the positive denominator
and quotient parity handles every tie class.

The task562 bound keeps final rational intermediates below 8192 bits. One
conservative check is that a coefficient of at most 1094 digits and a power
of at most 1493 decimal places give pre-alignment magnitudes below 5976 bits
using `10<2^4`; binary alignment adds at most 1074 bits, and the half comparison
one further bit. Initial exponent comparisons align to the larger operand's
bit length rather than adding two arbitrary operand lengths. These are small
mathematical magnitudes, not a claim about all internal allocation behavior.

The rounded significand fits Long and converts exactly to Double. Multiplication
by the exact binary quantum preserves every finite target value, including
subnormals and carry into the first normal. A carry beyond the maximum selects
infinity. Multiplication by the original ordinary sign preserves negative zero.
The Float cast introduces no second rounding. The proof assumes the target's
primitive IEEE behavior; it does not waive a concrete compiler/runtime defect.

## Exact Double constructor and context edges

`BigDecimal.kt:912-928` rejects every nonfinite input before decomposition and
returns coefficient zero/scale zero for both signed zeros. For a finite nonzero
magnitude let `f` be its exponent field and `t` its fraction. The decoded value
is `M*2^e`, with `M=t,e=-1074` for subnormals and
`M=2^52+t,e=f-1023-52` for normals. Thus `0<M<2^53`, `-1074<=e<=971`.
These are exhaustive field classes, not sample cases.

When `e<0`, removing `min(v2(M),-e)` trailing binary zeros preserves that value.
For remaining nonnegative exponent the constructor shifts `M` and uses scale
zero. Otherwise it forms `M*5^(-e)` at scale `-e`, since
`2^e=5^(-e)*10^e`. The remaining `M` is odd, so this coefficient is not divisible
by ten: no smaller nonnegative decimal scale could represent it exactly.
The maximum scale is 1074. Even the loose bound
`M*5^1074<2^(53+3*1074)=2^3275` keeps all constructor values small; positive
binary shifts produce fewer than 1025 bits. The final sign application is exact.

`roundedDouble` (`:930-933`) first obtains that exact **base** decimal. For a
valid context, zero retains scale zero; nonzero calls `exact.round(context)`.
The receiver is the fresh base value, so a constructing decimal subclass's
override is not reached. The public base round/plus path composes with the
[contextual rounding proof](contextual-constructor-rounding-549-proof.md):
precision zero is exact, every signed mode/tie is selected correctly, and a
carry needs at most one further exact division. Nonzero input cannot round to
zero at positive significant precision. All starting scales here are small;
this composition requires no extreme-scale rescue or new range cap.

The valid-context argument does not silently classify competing null/nonfinite
inputs. Existing non-null API policy and historical signed-zero/null evidence
remain separate, as recorded in the [task569 report](zero-double-context-569-verification.md).

## Canonical selection, finite bounds and source-specific zero cohorts

`DoubleFormatting.kt` matches the [existing binary64 interval proof](canonical-double-contract.md#selection-argument):
exact predecessor/successor midpoints, low-bit endpoint inclusion, clipped
bracketing grid coefficients, exact distance and stripped parity. Budget two
searches adjacent decimal bands and covers canonical lengths one and two.
Failure excludes an intervening decimal power, so later budgets need only the
center band. The seventeen-digit error bound is strictly smaller than the
minimum half-gap, proving termination for every positive finite input. Negative
inputs reflect this search. Both zeros and all nonfinite values are explicit
entry branches.

All of this factory's arithmetic has a fixed finite bound. For a deliberately
loose bound, `2^3<10` and the binary64 endpoints place adjusted exponent in
`[-358,341]`. Exact constructor/predecessor/successor scales are in `[0,1074]`.
The common scale is therefore at most 1075 and exceeds every searched grid
scale. Seventeen units use indices `17-digits+offset`: 14/15/16 at budget two,
and 14 down to zero thereafter. The finest unit exponent is nonnegative by
the definition of common scale. Including the final unused multiplication when
building units, its exponent is at most `1075+341+1=1417`. Grid coefficients
have at most 17 digits. Aligned candidates, centers, bounds and distances are
therefore below `10^1434` with at most one extra addition bit, comfortably below
6000 bits using `10<2^4`. No original-input-sized or unbounded search appears.

`FloatFormatting.kt` uses its own raw binary32 integer lattice and does not
select from widened Double text. The [whole binary32 argument](kotlin-floating-conversion-migration.md#whole-binary32-domain-selection-argument)
already establishes exact open/closed intervals, virtual maximum successor,
all signs/zeros/nonfinite classes, adjacent bands, canonical tie parity and
nine-digit sufficiency. Its source-specific indices and powers are bounded,
with at most twenty candidate visits and intermediates below `10^190`
(632 bits). This covers all binary32 input patterns conditionally, without
claiming an executed exhaustive comparison.

Canonical rendering chooses plain notation for adjusted exponents -3 through
6, scientific notation otherwise, and retains the required `.0`. Parsing that
text determines both coefficient and scale. In particular:

| Entry | Signed-zero result with a valid context, where applicable |
| --- | --- |
| `BigDecimal(Double)` / `BigDecimal(Double, mc)` | `0@0` |
| `BigDecimal.valueOf(Double)` / `Double.toBigDecimal()` | `0@1` |
| `Float.toBigDecimal()` / `Float.toBigDecimal(mc)` | `0@1` |
| `Double.toBigDecimal(mc)` | `0@1` |

The last current extension first obtains `valueOf`, then calls
`BigDecimal(canonical.unscaledValue(), canonical.scale(), mathContext)`.
It does **not** call public `canonical.round(mc)`, whose direct-zero policy
would select scale zero. Float's context extension similarly parses the
selected text with the context constructor. Historical task525 descriptions
of a public-round delegate describe that older handoff, not this current
source binding. The existing common floating-context test already checks the
canonical zero cohort across all eight modes. Nonfinite factory/extension
inputs are rejected before a decimal selection result is created.

## Dispatch, evidence and the finite closure recommendation

For BigInteger, zero and the stored large path bypass caller metadata/bit
overrides; the small path's single virtual Long result is explicitly composed
above. For BigDecimal, stored compact zero returns immediately. Compact
eligibility is calculated from the original stored coefficient/scale; selected
compact paths bypass virtual signum, and the general path calls it once and
uses stored magnitude/prefix data. No virtual precision, scale or unscaledValue
callback selects that prefix. Construction first normalizes BigInteger
subclasses through their virtual byte encoding. These source boundaries and
their finite replacement/throwing trace observations remain distinct from an
ordinary-value numerical theorem. Arbitrary poisoned/changing callbacks and
broader dispatch classification remain task541 obligations.

Existing evidence is reused, not enlarged:

* `FloatingPowerTest` independently checks all binary scaling powers used by
  assembly; integer midpoint-neighbor, replacement-Long and exact-conversion
  fixtures exercise the corresponding classes.
* The [task562 report](decimal-binary-conversion-verification.md) records 419
  prefix cases, signed midpoint/cohort checks and platform/artifact observations
  at its historical hashes. Its old artifact is not relabelled as this source.
* `CanonicalDoubleTest` retains its 16,578 coefficient/scale/text rows and
  asymmetric/subnormal/zero/nonfinite controls. `KotlinFloatingConversionContractTest`
  has independent signed literal, tie/carry/context-zero/nonfinite expectations;
  its JVM companion adds public API comparisons. No new duplicate matrix is needed.
* The accepted [exact eaa hosted report](public-math-ci-559.md#hosted-verification)
  records seven successful jobs, the actual ARM log audit and the separate
  106/0/46 public campaign. Those are finite executed scopes. They do not turn
  this source proof into an exhaustive executed Float comparison or official JCK.

The smallest remaining task543 action is independent review of this current
composition and its acceptance scope. Under the stated ordinary stored-value,
exact-leaf and IEEE assumptions, the exhaustive conversion classes have a
general argument; additional unbounded fuzzing is not a missing mathematical
step. No new production defect or absent independent regression was identified
by the source audit that led to this memo. Root owns task543 disposition.

Task548 still owns full required-range/temporary-allocation and practical
maximum-input obligations, including the large prefix divisor; task541 owns
remaining caller/subtype behavior. Task557's concrete Kotlin/JS erased
`Number`/generic-dispatch corruption remains a separate toolchain/caller defect,
as retained in [the integration report](math-port-integration-verification.md).
Assuming primitive IEEE correctness in a mathematical argument does not prove
all toolchains correct or waive that observed defect. The memo does not close
525/557, all of548/549/541, whole-project compatibility, performance or JCK.

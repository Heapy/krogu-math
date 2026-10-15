# Decimal overload contract inventory — task 541

Source baseline: `bbdd2f4a7c3d3729f2597205b794f2853cc28e67`, isolated branch
`task/541-decimal-contract-inventory`. This is source preparation and paper
reasoning, **not a passed-test certificate**. No compiler, numerical helper,
script, generator, test, benchmark or harness was executed for this work.
All source line references below refer to that unchanged baseline.

Task 541 activity comment 1688 governs the original task body. Public numerical,
scale, error and documented identity requirements remain in scope. Explicit
Kotlin calls and caller-written `super` must work. A pinned-JDK internal trace,
callback count, noncall, replacement-getter outcome, cache or allocation choice
does not by itself establish a requirement. Existing tests/fixtures remain
unchanged, including assertions that still combine these categories.

## Scope, count and notation

The requested families contain **37 public instance overloads**, counted from
the declarations, not from fixture rows: binary arithmetic 6; divide 6;
integral/remainder/pair 6; unary/round 7; setScale 3; movement 3; ordering/extrema
3; formatting 3. Adjacent `stripTrailingZeros` and `ulp` add two explicitly
labelled rows (38–39). Thus 39 is meaningful only with those two included.

`D` means `BigDecimal`, `C` means `MathContext`, `R` means `RoundingMode`.
Signatures show actual Kotlin nullability and result types; omitted results
are `D`. `x=a*10^-s`, `y=b*10^-t`; `p` and `m` are context precision/mode.
`Q` means truncation of `x/y` toward zero, not floor for negative quotients.
`r=x-Q*y`. Preferred scales are mathematical integers and must be calculated
without Int wrap. A representable final answer must not be rejected merely
because an invented intermediate scale overflows; the already proved bounded
repairs and their limits are in the division/scale documents linked below.

The [Java SE 25 public API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
and [Kotlin inheritance rules](https://kotlinlang.org/docs/inheritance.html)
are the specification sources checked on 2026-10-15. BellSoft Liberica
25.0.4+9 observations are the separately recorded reference evidence. No
implementation source of the JDK, Kotlin stdlib or another numerical library
was consulted.

For every row, ordinary/truthful inputs owe the stated numerical behavior;
null object arguments owe NPE, inexact `UNNECESSARY` owes arithmetic error,
and unsupported range remains subject to the public range contract. Legacy
Int rounding tokens outside 0..7 owe `IllegalArgumentException`; no new error
message or multiple-invalid-argument precedence is inferred here.
For ordinary context arithmetic and unary rounding, the independent mathematical
rule is exact value followed by significant-digit rounding, including carry and
the representation closest to preferred scale when the value is exact.
Integral division instead truncates independently of rounding mode; contextual
remainder is exact and may exceed context precision, as rows 13–18 specify.
Precision zero requests exactness. These shared obligations apply even where
omitted from a short table cell, subject to each method's specific contract.

Every overridable declaration below owes **L**: an explicit base-typed Kotlin
call selects the caller's override with the original arguments; a directly
returned caller-owned object or thrown exception retains identity. Explicit
caller-written `super.f(...)` selects the superclass implementation at that
call site. Its subsequent calls remain free subject to their own public
contracts. L does not make all inherited implementation calls normative.
Truthful numerical controls delegate every overridden method; replacement
controls test only this explicit language behavior, not their fake arithmetic.

## Exact existing method witnesses

Aliases resolve to exact methods, not assertions that an entire class proves
every overload or every field. C = common source, J = JVM source; all paths are
under `src/{commonTest,jvmTest}/kotlin/io/heapy/krogu/math/`.

| Alias | Exact existing method witness and its evidential limit |
|---|---|
| S | C `DecimalScaleContractTest.DecimalScaleContractContextAdditionAndSubtraction`; `.DecimalScaleContractBoundedExactWitnesses`; independent signed/aligned and endpoint controls |
| SC | C `DecimalScaleContractTest.DecimalScaleContractTinyQuotientsAcrossSignedScaleDistances`; `.DecimalScaleContractRoundingOnlyLegacyAndOperator`; `.DecimalScaleContractZeroAndZeroDivisors`; `.DecimalScaleContractBoundedRescalingAndHalfwayNeighbors`; explicit scale/legacy paths, bounded independent arithmetic |
| F | J `DecimalConformanceTest.structuredRoundingAndPreferredScales`; ordinary finite arithmetic and integral/pair differential evidence, not subclass or full-domain proof |
| CT | C `DecimalContextContractTest.ordinaryAndTruthfulControlsPartition0`, `...Partition1`, `...Partition2`, `...Partition3`; all meaningful class masks of the projected context-division inputs |
| CL | C `DecimalContextContractTest.explicitBaseTypedCallerEntersOverridesAndPropagatesTheirException`; `.callerOwnedSuperInvocationProducesTruthfulArithmetic`; exact/context divide L and selected super arithmetic |
| DN | C `DecimalDivisionNormalizationContractTest.exactCancellationAndReducedTerminatingDenominators`; `.roundedCarriesAndNonterminatingCarries`; `.hugePrecisionUsesSmallExactCohort`; `.unsupportedExactCohortDoesNotPreemptRoundedCarry` |
| DP | C `DecimalDivisionPreferredScaleContractTest.lowExactAndUnlimitedWitness`; `.lowFiniteWitnessAllModes`; `.highOneDigitWitnessOnly`; `.reducedDenominatorsAndCancellation`; `.finiteRoundingAndCarryNeighbors`; bounded endpoint repair, not every cohort |
| SR | C `DecimalScaleRoundingRepairTest.downscaleAcrossEntireSignedScaleDistance`; `.boundedWideCoefficientsUseIndependentLiteralThresholds`; `.contextMultiplicationRequiredReductionAndCarry`; `.zeroContextAdditionBothOrientationsAndSubtraction`; `.explicitCallerOverridesNullsAndCallerOwnedSuper` |
| ID | C `DecimalIntegralDispatchOracleTest.inheritedIntegralDivisionUsesVirtualContextDivision`; `.signedMetadataAndScaleEndpointsDetermineContextPrecision`; replaced quotient/metadata and trace observations, not truthful full-domain controls |
| PA | C `DecimalPairDispatchOracleTest.compositionPreservesOverloadCallbacksExceptionsAndIdentity`; replaced quotient/pair observations |
| RA | C `DecimalRemainderArrayOracleTest.replacementPairArraysPreserveBoundsFailuresAndSecondElementIdentity`; malformed/replaced pair observations, not a general public array-size or internal-forwarding guarantee |
| VI | C `ValueIdentityContractTest.decimalMinAndMaxTiesReturnTheReceiverIncludingTruthfulSubclasses`; `.decimalResultPairSlotsAreIndependentAcrossCalls`; `.integerAndDecimalOrderComposeWithCommonCollections` |
| AD | J `DecimalSubtractionSubclassTest.exactAdditionUsesStoredSigns`; `.finiteWideAdditionBypassesExactAddOverride`; `.subtractionCallbackRoutesMatchJdk`; observations include deliberately inconsistent/replaced methods |
| MU | J `DecimalMultiplicationSubclassTest.multiplicationMatchesJdkDispatchAndScaleValidation`; `.roundingScaleValidationMatchesJdk`; mixed diagnostic and corrected numeric fields |
| UN | J `DecimalUnarySubclassTest.unaryDispatchMatchesJdk`; `.negateContextUsesReturnedPlusCallback`; mixed replacement/identity/trace observations |
| RO | J `DecimalRoundSubclassTest.roundGetterReplacementMatchesJdk`; mixed replacement precision/plus/trace observations |
| SE | J `SetScaleBoundaryTest.scaleChangesMatchJdkValuesAndMessages`; enum scale, with task566 corrections; C SR supplies independent math |
| SD | J `ScaledDivisionBoundaryTest.nonzeroDivisorsMatchJdkResultsAndMessages`; `ScaledDivisionZeroTest.zeroDivisorErrorPathsMatchJdkAtCompactAndScaleBoundaries`; mixed numeric/diagnostic evidence |
| MO | J `DecimalPointMovementTest.pointMovementMatchesJdkStateIdentityAndDispatch`; replacement precision/sign and allocation observations, not entirely truthful controls |
| CO | J `DecimalComparisonSubclassTest.ordinaryComparisonResultsMatchJdk`; `.comparisonGetterDispatchMatchesJdk`; `.selfComparisonMatchesJdkCallbacks`; ordinary numeric rows separate from replaced getters |
| NC | C `DecimalNumericMetadataTest.comparisonHandlesEqualDecadesRemaindersSignsAndExtremeScales`; independent cohort/sign/remote-tail ordering; `.truthfulDirectSuperCallsAndExceptionIdentityKeepTheirOrder` also contains poisoned inspectors and internal trace assertions |
| TX | C `DecimalContextContractTest.canonicalTextLiteralThresholdScaleSignAndLongExponentWitnesses`; C `DecimalContextReturnedRepresentationContractTest.everyFrozenSuccessfulRepresentationHasDirectOrdinaryAndTruthfulControls`; `.independentLiteralThresholdZeroSignAndExtremeScaleWitnesses`; direct canonical representation controls |
| TF | J `DecimalFormattingSubclassTest.alternateFormatsMatchJdkGetterDispatch`; mixed sign/precision observations for alternate text |
| CA | J `DecimalStringCacheTest.repeatedCanonicalCallsMatchJdkCachedValueAndCallbacks`; `.concurrentReadersSeeCompleteImmutableText`; changing-getter cache details observational, complete immutable text is useful evidence |
| PO | C `PortableOracleTest.jdk25BehaviorOnEveryPlatform`; `.immutableValuesAndAlgebraicInvariants`; broad mixed fixture and selected independent arithmetic; exact relevant evaluator branches must be inspected before classifying a row |
| NE | J `NullArgumentConformanceTest.nullAndInvalidArgumentPrecedence`; C `NullPrecedenceOracleTest.nullAndInvalidArgumentPrecedenceAcrossPlatforms`; finite error-type evidence, not universal precedence rules |

## Overload inventory

Source links all target [BigDecimal.kt](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt).
The **route** column is a read-only implementation observation at the baseline,
not an invented internal-forwarding obligation. Each row also has L as defined
above. Only explicit documented identities are added to its obligation cell.
`G` denotes a missing finite control or classification task described below;
`P` denotes an unresolved full-domain argument. New N methods are unrun source.

| # | Kotlin signature / source line | Mathematical, scale, error or identity obligation | Present source route (observational) | Existing witnesses | Finite gap; proof gap |
|---|---|---|---|---|---|
| 01 | [`add(D?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L53) | x+y at max(s,t) | private aligned coefficients | S, AD, PO | G1; P1 |
| 02 | [`add(D?, C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L58) | round(x+y), preferred max(s,t); zero/nonzero operand must not spuriously overflow | p=0 calls exact add; finite zero, aligned and sticky-tail paths | S, F, SR | G1; P1 |
| 03 | [`subtract(D?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L109) | x-y at max(s,t) | private aligned coefficients | S, AD, PO | G1; P1 |
| 04 | [`subtract(D?, C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L114) | round(x-y), preferred max(s,t) | p=0 exact subtract; finite operand negate then receiver context add | S, F, SR, AD | G1; P1 |
| 05 | [`multiply(D?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L119) | x*y at s+t, subject to scale/range | private product, stored left-zero scale handling | MU, PO | G1; P2 |
| 06 | [`multiply(D?, C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L124) | round(x*y), preferred s+t; mandatory reduction/carry can rescue high logical scale | p=0 exact multiply; finite ordinary or neutral-scale high product | F, MU, SR | G1; P2 |
| 07 | [`divide(D?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L141) | exact x/y; preferred s-t, extend scale for terminating denominator; zero divisor/nontermination arithmetic error | zero check; reduced denominator factors 2/5; strip toward preferred | CL, DN, DP, PO | G1; P3 |
| 08 | [`divide(D, R)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L195) | round x/y to scale s | calls explicit-scale enum overload with stored s | SC | G1; P3 |
| 09 | [`divide(D?, Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L197) | same numerical/place rule as 08; invalid token error | validate token; enum rounding-only call | SC, NE | G1; P3 |
| 10 | [`divide(D?, Int, Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L202) | round x/y to requested scale k; invalid token error | validate token; explicit enum call | SC, NE | G1; P3 |
| 11 | [`divide(D, Int, R)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L206) | round x/y to scale k, zero coefficient keeps k; zero divisor and forbidden rounding error | Long net shift, tiny quotient shortcut, scaled coefficient kernel; separate zero diagnostics | SC, SD | G1; P3 |
| 12 | [`divide(D?, C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L254) | round x/y; preferred s-t; exact p=0; no false intermediate-scale failure in proved representable cases | p=0 exact divide; finite metadata normalization, bounded wide repair, rounded kernel | CT, CL, DN, DP, F | G1/G6; P3 |
| 13 | [`divideToIntegralValue(D?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L380) | Q, preferred s-t; arithmetic error for zero divisor | magnitude shortcut or guarded DOWN context divide, rescale/strip | ID, PO | G2 (N1); P4 |
| 14 | [`divideToIntegralValue(D?, C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L398) | Q independent of m; precision must suffice for integer part; preferred s-t within precision | p=0 exact integral; finite DOWN divide, residual check for negative scale, fit preferred scale | ID, F | G2 (N1,N2); P4 |
| 15 | [`remainder(D?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L439) | r=x-Q*y, exact, may be negative; scale from exact subtract/product composition | selects slot 1 of virtual pair | PA, RA, PO | G2 (N1); P4 |
| 16 | [`remainder(D?, C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L440) | exact r even when its digits exceed p; quotient precision errors still apply | selects slot 1 of context pair | PA, RA | G2 (N1); P4 |
| 17 | [`divideAndRemainder(D?): Array<D>`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L429) | two entries [Q,r], with each result's specified scale; caller array mutation must not change later arithmetic | integral call; q.multiply; receiver subtract; new array | PA, VI | G2 (N1); P4 |
| 18 | [`divideAndRemainder(D?, C?): Array<D>`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L433) | [Q,r] under 14/16; exact remainder, quotient precision limitation | p=0 exact pair; finite integral/product/subtract | PA, VI, F | G2 (N1); P4 |
| 19 | [`round(C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L446) | context rounding; public effect equals plus(C); p=0 no rounding; see Z1 zero-scale discrepancy | virtual plus(C) | RO, MU, PO | G3/Z1; P5 |
| 20 | [`plus()`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L50) | x at s; **receiver identity is documented** | returns this | UN, PO | G3; P5 |
| 21 | [`plus(C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L51) | context rounding of x; **zero-result scale-0 clause requires resolution, Z1**; no general receiver-identity promise | private roundValue; p=0/no drop returns this | RO, UN, PO | G3/Z1; P5 |
| 22 | [`abs()`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L46) | abs(x) at s; nonnegative receiver reuse is not a separately documented identity | virtual sign; negative negate, otherwise this | UN, PO | G3; P5 |
| 23 | [`abs(C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L47) | rounded abs(x), input scale before reduction | virtual sign then negate(C) or plus(C) | UN, PO | G3/Z1 propagation unresolved; P5 |
| 24 | [`negate()`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L48) | -x at s | stored coefficient negation | UN, PO | G3; P5 |
| 25 | [`negate(C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L49) | rounded -x | negate result's plus(C) | UN, PO | G3/Z1 propagation unresolved; P5 |
| 26 | [`setScale(Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L460) | x at requested scale k exactly; failure if rounding required | legacy Int overload with UNNECESSARY | SR, PO, NE | G1; P5 |
| 27 | [`setScale(Int, Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L462) | rescale with m, requested k; invalid token error | validate token then private rescale | SR, PO, NE | G1; P5 |
| 28 | [`setScale(Int, R)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L463) | rescale with m, requested k; reuse/allocation both permitted | legacy Int overload via ordinal | SR, SE | G1; P5 |
| 29 | [`movePointLeft(Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L504) | x*10^-n, scale max(s+n,0); overflow/range errors | private Long shift, negative scale rescaled to zero | MO | G4 (N3); P5 |
| 30 | [`movePointRight(Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L505) | x*10^n, scale max(s-n,0); do not negate MIN in Int | private Long shift, negative scale rescaled to zero | MO | G4 (N3); P5 |
| 31 | [`scaleByPowerOfTen(Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L511) | x*10^n, scale s-n; endpoint/zero saturation must be separately justified by contract | private stored coefficient, zero saturates scale | MO | G4 (N3 partial); P5 |
| 32 | [`compareTo(D?): Int`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L625) | numerical -1/0/1; cohorts compare equal; order laws and null rejection | same-scale compact shortcut, signs then magnitude/exponents/significands | CO, NC, VI | G5; P6 |
| 33 | [`min(D?): D?`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L651) | lesser operand; **receiver on numerical tie**, including unequal scales | virtual compare <=0 selects receiver | VI, PO | G5; P6 |
| 34 | [`max(D?): D?`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L652) | greater operand; **receiver on numerical tie** | virtual compare >=0 selects receiver | VI, PO | G5; P6 |
| 35 | [`toString(): String`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L666) | canonical coefficient/scale text; exponent decision from digits-1-s; locale independence, exact representation round trip | cached private canonical layout, selected virtual sign reads | TX, CA, PO | G5/G6; P6 |
| 36 | [`toPlainString(): String`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L704) | exponent-free value text; s>0 retains decimal places; s<=0 uses scale-zero spelling, negative-scale zero is "0" | private coefficient/plain layout with sign reads and length guard | TF, PO | G4 (N3); P6 |
| 37 | [`toEngineeringString(): String`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L714) | engineering exponent divisible by 3, value retained; zero spelling preserves scale; parsing need not recover nonzero original scale | private layout; exponent modulo 3; separate zero branch | TF, PO | G4 (N3); P6 |
| 38 adjacent | [`stripTrailingZeros()`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L491) | same value, remove coefficient factors 10, error on unrepresentable stripped scale; **numerical zero returns ZERO** | private stored zero/strip | PO | G3; P5 |
| 39 adjacent | [`ulp()`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L512) | coefficient 1 at input s, positive even for negative x | stored scale constructor | no dedicated witness identified in inspected suites; PO evaluator has no ulp branch | G3; P5 |

## Missing finite controls versus full-domain work

These are **coverage findings**, not claims that untested code is faulty.
No row is marked universally proved or complete by a finite corpus.

| Gap | Narrow finite control/classification still needed | What this source preparation supplies |
|---|---|---|
| G1 | Full ordinary/fully delegating operand-class controls and explicit L/super examples per overload, especially exact multiply/right subclass and enum/legacy scaled divide. S/SC/SR/CT already cover substantial numeric domains; do not duplicate their arithmetic matrices. | Inventory only; CL covers exact/context divide and SR selected scale/multiply/add caller controls. A whole-family L certificate remains absent. |
| G2 | Independent signed integral/remainder/pair coefficient+scale expectations on all four ordinary/truthful masks; remainder digits exceeding p; actual quotient-precision failure; selected explicit integral caller/super language behavior. ID/PA/RA chiefly use replacement callbacks. | N1 and N2 below, **UNRUN**. No old oracle is removed or reclassified wholesale. No general direct remainder/pair override matrix is added. |
| G3 | Separate truthful unary coefficient/scale, documented plus()/zero-strip identities, ulp and explicit unary caller/super controls from UN/RO mixed observations. Resolve Z1 first for contextual zero; do not enshrine observed source reuse or returned runtime class as requirements. | Inventory and Z1 paper witness only; not claimed filled. |
| G4 | Truthful movement and alternate-format literals; MO/TF mix fake getters with math. Endpoint zero/range interpretation and wider movement distances need their own controls. | N3 supplies small truthful/base movement and engineering/plain literals, **UNRUN**; endpoint/resource controls remain open. |
| G5 | Selected common order/cohort laws already exist (VI, NC). Remaining explicit compare/min/max/formatter caller-super controls must avoid treating NC/CO/CA private trace/cache behavior as language requirements. | Inventory only. Tie identity is already witnessed by VI and is not redundantly retested. |
| G6 | Field-level classification of poisoned context/format results and correspondence to valid controls, beyond the existing context input projection. | Existing 75,728-row correspondence retained, no new universal classification claim. |

| Proof gap | Unresolved argument beyond finite controls |
|---|---|
| P1 | All coefficient magnitudes, scale gaps and rounding modes through alignment, cancellation and sticky-tail addition/subtraction; representable extrema versus actual unsupported output. |
| P2 | Exact/finite product coefficient bounds, scale sum, required precision reduction/carry and zero orientations throughout the supported range; no blanket proof from task566's bounded HIGH repair. |
| P3 | Reduced denominators, preferred/cohort scales outside Int, normalization and all requested rounding places for every supported coefficient; allocation/range behavior. DN/DP and scale-contract arguments establish only their stated domains. Extreme reported precision and invalid metadata are separate from truthful metadata arithmetic. |
| P4 | Sufficiency of integral guard precision for every supported input, negative-scale residual detection, cohort selection under limited precision, exact remainder composition with feasible intermediates; full-range resource behavior. |
| P5 | Rounding carries at scale limits; supported coefficient limits; zero/scale specification interactions; movement/rescale overflow; full-domain identities only where documented. |
| P6 | Comparison/significand correctness for all supported magnitudes and scales, text layout/length bounds and locale-independent canonical injectivity across the entire representation range. |

The [context-control report](decimal-context-oracle-classification.md) and
[retained inventory](../diagnostics/decimal-context-dispatch/README.md) already
map 75,728 diagnostic inputs to 3,000 valid numerical/context/topology controls
and 11,472 meaningful class combinations. That is context-division finite
coverage, not evidence that all 37 overloads or poisoned interactions are done.
Likewise 1,440 pair and 1,400+1,176 integral observations are not P4 proofs.
The [scale](decimal-scale-contract.md), [normalization](decimal-division-normalization-contract.md),
[preferred-scale](decimal-division-preferred-scale-contract.md) and
[task566 proof](decimal-scale-rounding-566-proof.md) documents retain the
independent arithmetic arguments and explicit limits of existing repairs.

## New test source and independent expectations

Only [DecimalOverloadContract541Test.kt](../src/commonTest/kotlin/io/heapy/krogu/math/DecimalOverloadContract541Test.kt)
is added beside this document. Its three methods are **source-only, UNRUN**:

| ID / actual method name | Reason and independent expected result |
|---|---|
| N1 `truthfulIntegralFamiliesKeepExactSignedRemaindersBeyondContextPrecision` | 17.25=3*5+2.25. Exact quotient has preferred scale 2-1=1, hence 30@1; p=1 must use 3@0, and exact remainder is 225@2 despite having more than one digit. Sign changes follow truncation toward zero. All eight modes, including UNNECESSARY, must retain that integer part. 172.5/5 has integer part 34, which cannot fit p=1 and must error for integral/remainder/pair. Four operand-class masks exercise real fully delegating metadata and all six inherited public overloads. |
| N2 `explicitIntegralCallerPreservesArgumentsResultsExceptionsAndOwnedSuper` | Explicit context-integral override receives distinct/self/null divisor and finite/unlimited/null context unchanged. Caller-owned result/exception identity is checked without claiming fake quotient math. Separate truthful caller-owned super calls owe 3@0 finite, 30@1 unlimited, 1@0 self, NPE for either null argument, and arithmetic error for zero divisor. |
| N3 `truthfulPointMotionAndAlternateTextHaveIndependentLiteralControls` | 1.23*10^4=12300: right(4)/left(-4) use 12300@0, scaleBy(4) uses 123@-2; left(4)/right(-4) use 123@6. Negative-scale zero-distance point motion normalizes to scale zero. Five direct text triples cover engineering exponent groups, signs, positive-scale zero, negative-scale zero and trailing zeros. Expected strings are literals, not produced by port arithmetic or a replacement getter. |

All input coefficients have at most five digits (12300 in the text controls),
requested coefficient rescaling is at most four places, and text outputs are
short literals. Integral quotients have at most two digits. A paper pass through
the baseline integral budget gives only small guard precisions; no extreme
scale, huge context or allocation is needed for these witnesses. Expected
coefficients are not computed using production decimal operations. No new
JVM test, numerical script, production hook, cache or API is introduced.

## Z1: credible public-text/implementation scale discrepancy, UNRUN

The public [plus(MathContext) return clause](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#plus(java.math.MathContext))
states: “A zero result will have a scale of 0.” An ordinary input
`BigDecimal("0.00").plus(MathContext(1, RoundingMode.HALF_EVEN))` has an exactly
representable zero result. Reading that clause literally requires coefficient
0, scale 0; both 0@0 and the input 0@2 are trivial supported representations,
so there is no rounding/range obstacle to the stated scale.

Paper route: line 51 calls `roundValue`; line 448 does not take the p=0 exit;
line 450 computes precision(0)-1=0; the loop is skipped and line 457 returns
the input 0@2. This is a **source-derived prediction, not an executed result**.
The same private route is reached by ordinary `round(C)` and contextual unary
composition, but the scope of any required zero normalization needs analysis.

There is a specification interaction to resolve: round(C) says its effect is
identical to plus(C) and p=0 performs no rounding; existing mixed UN/RO/PO
evidence preserves nonzero zero scales. That evidence does not override the
quoted clause, and does not prove the JDK's output for this witness here.
Record this as an unresolved public-text/implementation mismatch, **not a
confirmed arithmetic bug, proven JDK defect, or authorized production fix**.
No expected-failing test is silently added to the normal suite. The minimal
proposed regression, if the literal clause is accepted after independent
classification, is `assertEquals(0, BigDecimal("0.00").plus(MathContext(1)).scale())`.
No known ordinary numerical defect beyond this scale discrepancy was established
by the present source audit. This is not a declaration that other inputs pass.

## Explicit boundaries and handoff

Pow/sqrt and floating conversions are separate task542/task543 work; the
existing task563/565 root and binary conversion proofs are not coverage of
this inventory. Constructor-presence/Kotlin migration shapes belong to task549.
Metadata getters (source lines 41–45), equals/hashCode (653–654), numeric
conversions, factories, operators and serialization are adjacent surfaces, not
secretly counted among the 37. Metadata truthfulness is a prerequisite for the
controls here. VI covers selected equality/hash/immutability laws separately.
Legacy Java source ABI is required only through TCK adapters; no Java subclass
ABI work, legacy serialization, or production change is included.

The task prompt records parent root baseline gates as JVM 382 and 250 common
methods per JS/WasmJS/macOS target (1,132 total), already passing. Those are
**inherited reports**, not checks rerun or independently certified here, and do
not include this new test source. Integration compilation and authorized
four-target execution of N1–N3 remain required before treating them as evidence.

Actual checks here are limited to source/metadata inspection, public API reading,
paper derivations, independent read-only review, Git read-only status/diff and
whitespace/hash checks. Review findings and final file hashes are recorded in
the ref-less task541 handoff comment. Keep task linked/in_progress.
**Changes are not committed.** No compiler window is claimed; no execution is
authorized by a later release of another worker's window without a new grant.

Independent reviewer `/root/review_541_source` checked both new files against
the declarations, named existing methods and public documentation without
execution. The review confirmed the count, signatures/lines, N1–N3 paper
expectations and appropriately qualified Z1; two documentation findings were
corrected: the shared context-rounding rule now excludes integral/remainder
semantics, and the representability paragraph counts the five-digit text input.
Kotlin source plausibility is a read-only assessment, not compilation evidence.

# Decimal overload contract inventory — task 541

Original source baseline: `bbdd2f4a7c3d3729f2597205b794f2853cc28e67`, isolated
branch `task/541-decimal-contract-inventory`. That milestone was source
preparation and paper reasoning, **not a passed-test certificate**. No compiler,
numerical helper, script, generator, test, benchmark or harness was executed by
its author. Original source-line references retain that baseline; later finite
validation and explicitly marked current routes are distinguished below.

Task 541 activity comment 1688 governs the original task body. Public numerical,
scale, error and documented identity requirements remain in scope. Explicit
Kotlin calls and caller-written `super` must work. A pinned-JDK internal trace,
callback count, noncall, replacement-getter outcome, cache or allocation choice
does not by itself establish a requirement. The original milestone left existing
tests/fixtures unchanged, including assertions that still combine these
categories. Later task567 classifications have their separately linked scope.

## Superseding finite validation status — 2026-10-15

The source-preparation and UNRUN statements below describe the original `bbdd`
milestone; they are not the current status of N1–N3 or Z1. The later
[538/567/541 integration report](context-modular-integration-verification.md#behavioral-gates-and-the-preserved-first-failure)
records complete source-derived audits: JVM 416 methods and JS/Wasm JS/macOS
ARM64 277 each, with zero failures/errors/skips, missing/extra methods,
duplicates or XML anomalies. All three task541 methods ran on each target
(12 method executions). The first integrated failure, its narrow key-encoding
correction and subsequent complete replays remain recorded in that report.

The published commit `bc1d51117a2125acc991607213b6c0bca4c2f8f6`, which includes
these controls and the task567 zero-scale repair, subsequently completed all six
jobs of [CI 38685893617](https://github.com/Heapy/krogu-math/actions/runs/38685893617)
successfully. Its actual Linux ARM64 log records 277 tests from 58 test cases,
all passed. Retained metadata is `/private/tmp/krogu-c3-ci-result.json`, SHA-256
`63c082617fd2eb3d2ef355948980434335012b7e47e9794c49a4e1b260ccffdb`;
the ARM64 log is `/private/tmp/krogu-c3-linux-arm64-job.log`, SHA-256
`4c704f5ff4165be4a4492178f4ed9c29dc0a67c1566fb6e4394b984424a1277f`.
These are retained results read for this documentation update, not new runs.

N1/N2 now supply the stated finite G2 controls; N3 supplies the stated finite
G4 controls. Z1's direct ordinary/truthful `plus(C)`/`round(C)` zero-result
scale rule is resolved by task567, including precision zero. Its
[V2 seam argument](zero-context-scale-567-v2-proof.md),
[finite classification](zero-context-scale-567-finite-proof.md) and
[key correction](zero-context-scale-567-key-correction.md) retain the separate
unary compatibility choice, original JDK outcomes and exact selected domains.
Historical SOURCE ONLY labels in those milestones remain historical.

This supersedes only those finite validation/status gaps. G1–G6 residual work,
all P1–P6 full-domain arguments, and the broader 39-entry audit remain **OPEN**;
in particular G3/P5 are not closed by the direct-zero repair. Pow/sqrt and their
separate task568 work are outside these rows and receive no validation claim
from this note. No execution or broader oracle archival is authorized here.

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
`P` denotes an unresolved full-domain argument. N1–N3 have the later finite
validation recorded above. Routes retain the original baseline observations
except the explicitly marked current task567 routes in rows 19, 21, 23 and 25;
the signature/source-line column remains the original baseline inventory.

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
| 13 | [`divideToIntegralValue(D?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L380) | Q, preferred s-t; arithmetic error for zero divisor | magnitude shortcut or guarded DOWN context divide, rescale/strip | ID, PO, N1 | G2 residual (N1 validated); P4 |
| 14 | [`divideToIntegralValue(D?, C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L398) | Q independent of m; precision must suffice for integer part; preferred s-t within precision | p=0 exact integral; finite DOWN divide, residual check for negative scale, fit preferred scale | ID, F, N1, N2 | G2 residual (N1,N2 validated); P4 |
| 15 | [`remainder(D?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L439) | r=x-Q*y, exact, may be negative; scale from exact subtract/product composition | selects slot 1 of virtual pair | PA, RA, PO, N1 | G2 residual (N1 validated); P4 |
| 16 | [`remainder(D?, C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L440) | exact r even when its digits exceed p; quotient precision errors still apply | selects slot 1 of context pair | PA, RA, N1 | G2 residual (N1 validated); P4 |
| 17 | [`divideAndRemainder(D?): Array<D>`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L429) | two entries [Q,r], with each result's specified scale; caller array mutation must not change later arithmetic | integral call; q.multiply; receiver subtract; new array | PA, VI, N1 | G2 residual (N1 validated); P4 |
| 18 | [`divideAndRemainder(D?, C?): Array<D>`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L433) | [Q,r] under 14/16; exact remainder, quotient precision limitation | p=0 exact pair; finite integral/product/subtract | PA, VI, F, N1 | G2 residual (N1 validated); P4 |
| 19 | [`round(C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L446) | context rounding; public effect equals plus(C); p=0 no digit rounding, zero-result scale 0 (Z1 resolved by 567) | current task567: virtual plus(C), retaining explicit override behavior | RO, MU, PO, task567 direct-zero controls | G3 residual; P5 |
| 20 | [`plus()`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L50) | x at s; **receiver identity is documented** | returns this | UN, PO | G3; P5 |
| 21 | [`plus(C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L51) | context rounding of x; **zero-result scale 0, including p=0; Z1 resolved by 567**; no general receiver-identity promise | current task567: validate context, private roundValue, select stored zero as 0@0; nonzero result unchanged; cached ZERO identity is not an obligation | RO, UN, PO, task567 direct-zero controls | G3 residual; P5 |
| 22 | [`abs()`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L46) | abs(x) at s; nonnegative receiver reuse is not a separately documented identity | virtual sign; negative negate, otherwise this | UN, PO | G3; P5 |
| 23 | [`abs(C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L47) | rounded abs(x); direct plus(C)'s zero-scale rule is not an inherited-forwarding requirement | current task567: virtual sign; negative branch virtual negate(C), otherwise private stored-coefficient seam; stored zero retains its cohort via roundValue, nonzero uses virtual plus(C) | UN, PO, task567 scoped unary controls | G3 unary/caller residual; P5 |
| 24 | [`negate()`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L48) | -x at s | stored coefficient negation | UN, PO | G3; P5 |
| 25 | [`negate(C?)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L49) | rounded -x; contextual zero cohort choice is separate from direct plus(C) | current task567: virtual negate(), then private seam on returned receiver; stored zero uses roundValue, nonzero uses virtual plus(C); direct overrides retain their separate L obligations | UN, PO, task567 scoped unary controls | G3 unary/caller residual; P5 |
| 26 | [`setScale(Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L460) | x at requested scale k exactly; failure if rounding required | legacy Int overload with UNNECESSARY | SR, PO, NE | G1; P5 |
| 27 | [`setScale(Int, Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L462) | rescale with m, requested k; invalid token error | validate token then private rescale | SR, PO, NE | G1; P5 |
| 28 | [`setScale(Int, R)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L463) | rescale with m, requested k; reuse/allocation both permitted | legacy Int overload via ordinal | SR, SE | G1; P5 |
| 29 | [`movePointLeft(Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L504) | x*10^-n, scale max(s+n,0); overflow/range errors | private Long shift, negative scale rescaled to zero | MO, N3 | G4 residual (N3 validated); P5 |
| 30 | [`movePointRight(Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L505) | x*10^n, scale max(s-n,0); do not negate MIN in Int | private Long shift, negative scale rescaled to zero | MO, N3 | G4 residual (N3 validated); P5 |
| 31 | [`scaleByPowerOfTen(Int)`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L511) | x*10^n, scale s-n; endpoint/zero saturation must be separately justified by contract | private stored coefficient, zero saturates scale | MO, N3 | G4 residual (N3 partial validated); P5 |
| 32 | [`compareTo(D?): Int`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L625) | numerical -1/0/1; cohorts compare equal; order laws and null rejection | same-scale compact shortcut, signs then magnitude/exponents/significands | CO, NC, VI | G5; P6 |
| 33 | [`min(D?): D?`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L651) | lesser operand; **receiver on numerical tie**, including unequal scales | virtual compare <=0 selects receiver | VI, PO | G5; P6 |
| 34 | [`max(D?): D?`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L652) | greater operand; **receiver on numerical tie** | virtual compare >=0 selects receiver | VI, PO | G5; P6 |
| 35 | [`toString(): String`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L666) | canonical coefficient/scale text; exponent decision from digits-1-s; locale independence, exact representation round trip | cached private canonical layout, selected virtual sign reads | TX, CA, PO | G5/G6; P6 |
| 36 | [`toPlainString(): String`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L704) | exponent-free value text; s>0 retains decimal places; s<=0 uses scale-zero spelling, negative-scale zero is "0" | private coefficient/plain layout with sign reads and length guard | TF, PO, N3 | G4 residual (N3 validated); P6 |
| 37 | [`toEngineeringString(): String`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L714) | engineering exponent divisible by 3, value retained; zero spelling preserves scale; parsing need not recover nonzero original scale | private layout; exponent modulo 3; separate zero branch | TF, PO, N3 | G4 residual (N3 validated); P6 |
| 38 adjacent | [`stripTrailingZeros()`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L491) | same value, remove coefficient factors 10, error on unrepresentable stripped scale; **numerical zero returns ZERO** | private stored zero/strip | PO | G3; P5 |
| 39 adjacent | [`ulp()`](../src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt#L512) | coefficient 1 at input s, positive even for negative x | stored scale constructor | no dedicated witness identified in inspected suites; PO evaluator has no ulp branch | G3; P5 |

## Missing finite controls versus full-domain work

These are **coverage findings**, not claims that untested code is faulty.
No row is marked universally proved or complete by a finite corpus.

| Gap | Narrow finite control/classification still needed | What this source preparation supplies |
|---|---|---|
| G1 | Full ordinary/fully delegating operand-class controls and explicit L/super examples per overload, especially exact multiply/right subclass and enum/legacy scaled divide. S/SC/SR/CT already cover substantial numeric domains; do not duplicate their arithmetic matrices. | Inventory only; CL covers exact/context divide and SR selected scale/multiply/add caller controls. A whole-family L certificate remains absent. |
| G2 | N1/N2 now validate the bounded signed integral/remainder/pair, four-mask, excess-remainder-digit, quotient-precision-error and selected integral caller/super controls. Wider composition/override classification remains; ID/PA/RA chiefly use replacement callbacks. | **N1 and N2 finite controls validated** on the recorded targets. No old oracle is removed or reclassified wholesale. A general direct remainder/pair override matrix and P4 remain open. |
| G3 | Wider truthful unary coefficient/scale, documented plus()/zero-strip identities, ulp and explicit unary caller/super controls still need separation from UN/RO observations. Do not enshrine source reuse or runtime class as requirements. | **Z1 direct-zero rule resolved and validated by 567**, with scoped unary compatibility controls/classifications. This does not fill all G3 or P5. |
| G4 | N3 now validates its bounded truthful/base movement and alternate-format literals; MO/TF still mix fake getters with math. Endpoint zero/range interpretation, wider distances and resource domains need their own controls. | **N3 finite controls validated** on the recorded targets; endpoint/resource controls and P5/P6 remain open. |
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

## N1–N3: original expectations and later finite validation

Only [DecimalOverloadContract541Test.kt](../src/commonTest/kotlin/io/heapy/krogu/math/DecimalOverloadContract541Test.kt)
was added beside this document. Its author's original source milestone was
UNRUN; **all three methods subsequently passed** the recorded local gates and
published `bc1d` CI. Their original independent expectations remain:

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

## Z1: original source witness; direct-zero rule resolved by task567

The following witness and paper route describe the original `bbdd` assessment.
The current disposition below supersedes its then-unresolved status.

The public [plus(MathContext) return clause](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#plus(java.math.MathContext))
states: “A zero result will have a scale of 0.” An ordinary input
`BigDecimal("0.00").plus(MathContext(1, RoundingMode.HALF_EVEN))` has an exactly
representable zero result. Reading that clause literally requires coefficient
0, scale 0; both 0@0 and the input 0@2 are trivial supported representations,
so there is no rounding/range obstacle to the stated scale.

Historical paper route: line 51 calls `roundValue`; line 448 does not take the
p=0 exit;
line 450 computes precision(0)-1=0; the loop is skipped and line 457 returns
the input 0@2. This is a **source-derived prediction, not an executed result**.
The same private route was reached by ordinary `round(C)` and contextual unary
composition; the original assessment left zero-normalization scope for analysis.

The original specification interaction was: round(C) says its effect is
identical to plus(C) and p=0 performs no rounding; existing mixed UN/RO/PO
evidence preserves nonzero zero scales. That evidence does not override the
quoted clause, and does not prove the JDK's output for this witness here.
At that milestone this was recorded as an unresolved public-text/implementation
mismatch, not a confirmed repair or fresh JDK defect verification. Its minimal
proposed regression was
`assertEquals(0, BigDecimal("0.00").plus(MathContext(1)).scale())`.
The original source audit established no broader ordinary numerical defect;
it did not declare that other inputs passed.

**Current disposition:** task567 accepted the direct plus/round scale-zero
clause, implemented it and completed the later validation recorded above.
Current `plus(C)` validates context, applies the private precision kernel and
selects coefficient-zero/scale-zero/canonical-text-zero for a stored-zero result,
including p=0. Current `round(C)` still calls virtual plus(C), so an explicit
caller replacement or exception retains its own identity. No-context plus()
still returns its receiver at its original scale; cached ZERO identity is not
added to the contextual contract. Constructors and arithmetic keep their own
preferred-scale seams. The separate inherited unary seam retains stored-zero
cohorts and nonzero virtual plus callbacks as documented in the V2/finite proofs;
that chosen compatibility behavior is not a universal forwarding requirement.

Historical JDK outcomes remain in
`/private/tmp/krogu-541-Z1-538-1991-6d0b1536-v1/observations.tsv`, SHA-256
`ec2f3c0e865edf2e644a0e253e33474373b6443fe5e7bb025654dcc809609cce`.
They were neither regenerated nor replaced by this status update. The task567
proofs and integration report retain the exact finite observation/adaptation
boundaries. Remaining G3/P5 questions are not closed by this disposition.

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

The original task prompt recorded parent root baseline gates as JVM 382 and
250 common methods per JS/WasmJS/macOS target (1,132 total), already passing.
Those were **inherited reports**, not checks rerun by the original author, and
did not include the then-new test source. Those original pending N1–N3 gates
were subsequently completed as recorded in the superseding note above; broader
finite gaps and full-domain obligations still remain.

The original source milestone's checks were limited to source/metadata inspection,
public API reading, paper derivations, independent read-only review, Git read-only
status/diff and whitespace/hash checks. Review findings and final file hashes are recorded in
the ref-less task541 handoff comment. Its uncommitted/source-only handoff status
is historical. This reconciliation edits only the inventory and reads retained
source/proof/validation evidence; it runs no compiler, helper, test or numerical
program and claims no new gate. The broader task541 audit remains open.

Independent reviewer `/root/review_541_source` checked both new files against
the declarations, named existing methods and public documentation without
execution. The review confirmed the count, signatures/lines, N1–N3 paper
expectations and appropriately qualified Z1; two documentation findings were
corrected: the shared context-rounding rule now excludes integral/remainder
semantics, and the representability paragraph counts the five-digit text input.
Kotlin source plausibility is a read-only assessment, not compilation evidence.

## Normative finite-control tranche (four local gates passed; CI pending)

The exact revision3 tests passed all four complete local target gates: JVM145
suites/498 methods, and JS/Wasm JS/macOS ARM64 each65 suites/314 methods, totaling
1440 method executions across targets. Independent actual review accepted all
four gates. The [current validation report](decimal-overload-541-verification.md)
records the evidence. Future exact published-head CI for this tranche remains
pending; these finite local passes do not establish full task541 completion.

The37-row signature table above records original `bbdd` nullability, with two
adjacent rows making39 entries. Task573 subsequently migrated all48 reviewed
inputs to non-null, and task525 migrated all four min/max results to non-null.
Current caller and actual-super controls use those non-null signatures. Three
equals inputs and four OrNull results remain nullable. Older Java-null outcomes
retain their separately classified diagnostic scope; the historical table is
not a current nullable Kotlin calling contract.

The companion `DecimalOverloadDispatch541Test.kt` names finite direct controls
for all 39 inventory entries in three behavior families: binary/division/
integral/scale, unary/order/formatting/adjacent entries, and caller-owned
override identity. Entries01–18,26–37,39 have ordinary and truthful direct
receivers; unary entries19–25 and zero stripping38 use ordinary receivers in
that class. Selected actual `super` helpers and the
caller class cover explicit base entry and virtual replacement. It uses literal
or direct algebraic representations, signed and unequal-scale values,
exact addition, zero cancellation and valid domain errors. These finite cases
do not establish a separate truthful override or universal `super` proof for
every row.
`DecimalUlp541ContractTest.kt` supplies the separate row-39 source argument and
the documented no-context `plus()` identity controls.

New remainder controls have zero results only. Existing
`DecimalOverloadContract541Test` lines36–67 retain the distinct signed nonzero
remainder, context/rounding, quotient-cohort and failure controls; the new class
does not replace or enlarge that retained finite domain.

The original source-preparation status of this tranche is historical; the four
local passes are recorded above and future CI remains pending. This does
not close P1–P6 full-domain
proofs, resource-sized text or coefficient behavior, or unspecified forged
metadata/internal callback behavior. Original scope classifications, G1–G6
residual work and full-domain/resource limits remain unchanged.

# Zero contextual scale: task 567 source proof

Status: **SOURCE ONLY / UNCOMPILED / UNRUN**. This is a candidate, not a
runtime-verified repair. Task `local:567` remains linked and `in_progress`.
Changes are not committed. Base HEAD: `bbdd2f4a7c3d3729f2597205b794f2853cc28e67`,
branch `task/567-zero-context-scale`, worktree `/private/tmp/krogu-math-task-567`.

## Independent basis

Only public Java SE 25 API documentation, this repository's own source, and the
supplied black-box evidence were used. No JDK, Kotlin stdlib, or third-party
numerical implementation bodies or unsolicited implementation snippets were
consulted. No compiler, numerical program, helper, script, generator, probe,
test, benchmark, or harness was executed by this worker or its reviewer.

The normative [plus(MathContext) return contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#plus(java.math.MathContext))
states: “A zero result will have a scale of 0.” The documentation makes plus and
[round(MathContext)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#round(java.math.MathContext))
equivalent in both directions. Unlimited precision suppresses numerical digit
rounding; it does not require preserving the input's cohort or identity. A
zero coefficient with scale zero is representable at every supported precision.
Consequently direct ordinary/truthful plus and round must return `0@0`, including
precision zero. There is no requirement that the returned reference be `ZERO`.

This deliberately diverges from the previously observed host and port. The
authorized historical evidence is unchanged:

- `/private/tmp/krogu-541-Z1-538-1991-6d0b1536-v1/observations.tsv`
- SHA-256 `ec2f3c0e865edf2e644a0e253e33474373b6443fe5e7bb025654dcc809609cce`
- Supplied evidence: 357 keys, 336 contextual cases, 7 no-context controls and
  14 null controls; 288 literal differences, 55 pair matches, 14 null matches.

Its hash was re-read, but no evidence was regenerated, rewritten, or interpreted
as a run of this candidate. The host's retained zero scale is not the expectation.

## Source invariants

`BigDecimal.plus(mc)` first performs the existing null-context check and runs
the unchanged private `roundValue`. Only a returned stored-zero coefficient is
then represented as `ZERO` (coefficient 0, scale 0, canonical text `0`). This
adds no public metadata callback. Ordinary/truthful zero has precision 1, so
no digit drop or power of ten is needed at any precision in the tested domain.
The scale is never arithmetically negated or subtracted to perform normalization.
For a nonzero result the exact prior rounding result is returned. The input's
coefficient and scale are immutable and unchanged.

`round(mc)` still calls virtual `plus(mc)`. An explicit caller override can
return its exact replacement or throw its exact exception, even for null
contexts. Explicit `super.plus` bypasses that override; `super.round` still
dispatches virtual plus. No-context `plus()` still returns `this` with its
original scale. No public declaration, serialization field, metadata helper,
BigInteger code, rounding kernel body, or supported input domain was changed.

## Complete internal rounding-call inventory

Line numbers below refer to the candidate `BigDecimal.kt`.

| Caller | Ownership and decision |
| --- | --- |
| Constructors 26, 27, 31, 33 | Fresh base decimal from integer, scaled integer, long or parsed text. Use private `roundValue` to retain constructor scale, including zero. Int delegates to long; char-array/slice delegates to String. No caller-owned decimal callback is bypassed. |
| `abs(mc)` 47 | Keep existing sign branch and virtual negate/plus dispatch. Contextual zero representation is discussed separately below. |
| `negate(mc)` 49 | Keep virtual `negate()` followed by virtual plus on the returned value, including caller replacement/exception identity. |
| Direct `plus(mc)` 51 | Existing precision rounding followed by the explicit direct-result zero representation rule. |
| Zero-operand `addContextValue` 71 | Keep virtual round on the caller operand. Existing line 72 restores a zero result to `max(input scales)`; nonzero rounding is unchanged. |
| Aligned `addContextValue` 79 | `addValue` produces a fresh base decimal; private rounding preserves exact cancellation scale. Subtract delegates to add at finite precision and retains this protection. |
| Sticky addition 85 | `addValue` again produces a fresh base result; private rounding followed by existing preferred-scale restoration. |
| Ordinary contextual multiply 137 | Fresh base `multiplyValue` result uses private rounding, retaining scale sum for zero. Unlimited precision still calls existing public exact multiply. |
| High logical-scale multiply 142 | Unchanged public round on a fresh base nonzero product: both stored factors are nonzero on this branch, and positive significant-digit precision cannot round their product to zero. |
| Contextual divide 309–310 | `divideScaledCoefficients` returns a fresh base decimal; private rounding retains its scale before existing exact-quotient scale selection. Ordinary zero dividend already returns at 265; exact and explicitly scaled division retain their separate zero paths. |
| Public `round(mc)` 451 | Preserve virtual plus dispatch. |
| `pow(mc)` final 545 | Private rounding on the owned accumulator. It begins at base `ONE`, and each inherited base multiplication/division returns a base decimal; a caller multiplicand cannot replace the accumulator. Preserve zero product scale through the final precision reduction. Unlimited precision still calls public exact pow. |
| `sqrt(mc)` 568, 601, 624 | Unchanged public round on owned roots. Ordinary/truthful zero returns at 552 with its own preferred scale. Finite roots here are positive; positive significant-digit rounding cannot make a positive root zero. Unsupported/lying metadata paths are not newly promised. |
| `roundedDouble` 928 | Zero conversion already returns directly at scale 0. The rounded branch is a fresh nonzero base decimal; unchanged. Existing null staging is untouched. |
| `NumericExtensions.kt` 25 | `Double.toBigDecimal(context)` intentionally calls public round on the valueOf result. No additional scale promise or source change. |

The private kernel still returns zero with its existing scale. There is no
blanket internal normalization. Exact add/subtract prefer the larger scale;
multiply prefers the scale sum; zero division uses its preferred difference
or explicit requested scale. No new claim is made about pre-existing saturation,
unsupported scale diagnostics, or behavior of untruthful metadata subclasses.

## Contextual unary representation decision

The specific [abs(mc)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#abs(java.math.MathContext))
and [negate(mc)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#negate(java.math.MathContext))
entries promise the numerical absolute/negated value with context rounding;
they do not state a zero-scale/cohort guarantee or bilateral equivalence with
direct plus. Selecting `0@0` for inherited contextual zero unary results is
therefore independently permissible. This candidate intentionally selects that
representation while keeping the existing virtual dispatch. It does **not**
claim those methods require zero normalization merely because they call plus.
Their no-context contracts explicitly preserve input scale, which remains intact.
Preserving a caller override's result/exception identity takes precedence over
normalizing or rescaling its replacement. The new tests assert contextual unary
numerical zero and caller dispatch, without imposing an API zero-scale requirement.

## Prepared common tests and sufficiency

New file: `src/commonTest/kotlin/io/heapy/krogu/math/ZeroContextScale567Test.kt`.
All nine methods are prepared source only:

| Method | Independent expected result / boundary |
| --- | --- |
| `directZeroHasLiteralScaleZeroAcrossEntireMetadataRange` | Scales MIN, -7, -1, 0, 1, 7, MAX × precision 0, 1, 7, MAX × eight modes × base/truthful = 448 inputs; direct plus/round and truthful caller-owned super wrappers. Literal coefficient `0`, scale `0`, canonical text `0`; unchanged input; no output identity assertion. |
| `noContextPlusRetainsIdentityAndNullContextStillFails` | Zero and signed one-digit nonzero values at every scale; identity only for no-context plus; null exceptions for direct and truthful super entry points. |
| `nonzeroExactRoundingKeepsRepresentableInputCohort` | Signed one-digit values across full metadata matrix; exact trailing-zero reduction `1200@3 -> 12@1` at p2 under all modes; unlimited precision keeps `1200@3`. |
| `nonzeroMidpointAndCarryUseLiteralModeSpecificExpectations` | Fixed literal tables for positive/negative 1.25, positive 1.35 parity, and positive/negative 9.99 carry at p2. Explicit eight-mode columns, including UNNECESSARY failures. No implementation-shaped expected calculator. |
| `explicitOverridesKeepResultAndExceptionIdentityEvenWithNullContext` | Separate plus/round overrides, a noncanonical zero replacement, exact sentinel exceptions, null contexts, direct and caller-owned super dispatch. |
| `contextualConstructorsRetainTheirOwnZeroScales` | Scaled integer constructor over full scale/precision/mode matrix; bounded exponent/fraction strings, arrays/slices; integer, long, unscaled integer and double zero constructors; nullable text/array context failures. |
| `cancellationAndZeroAdditionRetainPreferredScale` | Equal-scale signed cancellation at extremes; unequal-scale exact cancellation at scale 3; all pairs of zero scales at all contexts; zero/nonzero p0 versus p1 precision controls. |
| `zeroProductsQuotientsAndPowersRetainOperationScales` | Both zero-product orientations, zero division at every source and requested scale; literal nonzero operand at negative scale gives zero product scale 2 and quotient scale 4; zero square retains scale 6. |
| `unaryContractsAndExplicitDelegationRemainIntact` | No-context abs/negate original scale; contextual numerical zero; null errors; virtual plus on abs receiver and on a negate replacement preserves exact caller results/exceptions. |

These are bounded representation controls, not parser fuzz, large witnesses,
or a new numerical oracle. Extreme metadata never creates a huge nonzero power:
zero alignment short-circuits; nonzero cancellation uses equal or nearby scales;
requested-scale zero division returns zero directly. Precision MAX does not
request a huge coefficient in the selected cases.

## Source hashes and review status

SHA-256:

- Original `BigDecimal.kt`: `a1d6c40c04e6534b2ae934f6d77e7cd8b5a1c530bb8e594b2af08f9fc225bb8a`.
- Candidate `BigDecimal.kt`: `7214a0fda8a4ae8a6836829c2bf74fd9dfeb671e01b41f8e7ada33732b9b68d1`.
- New common test: `736472d71a2b216b1e367fbc220ec648dd11f2069bc63a19f86102cb4483bc59`.

An independent clean read-only contract/caller audit supported the boundary,
owned-result seams and the separate unary decision. Final independent diff,
test and proof review is recorded in the handoff. Whitespace inspection is not
compilation or behavioral validation.

## Remaining gates and unresolved questions

The parent owns runtime scheduling. Later explicit authorization is required
for compilation, a fresh artifact and bounded test execution, then the full
retained JVM/JS/Wasm/macOS ARM64 suites and supported exact-head CI targets.
No invocation was attempted or queued by this worker. Availability of an idle
runtime window is not authorization.

Actual retained-oracle failures are unknown until that first authorized run.
Direct host comparisons and some unary/metadata/identity rows may differ; these
are candidate impact areas, not observed failures. No existing test, fixture,
generator, diagnostic artifact, or oracle expectation was edited. The parent
must classify each actual failure and permit only independently justified,
narrow field-level expectations; raw host evidence must remain intact.
Contextual unary representation is permitted rather than uniquely prescribed;
the choice here is explicit and still requires retained-suite classification.
No claim of a new candidate black-box result or completed acceptance is made.

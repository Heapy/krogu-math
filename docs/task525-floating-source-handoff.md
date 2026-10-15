# Task525 floating source/proof handoff

## Identity, scope and status

Worker kotgent session: `8b7417dd` (floating conversions), delegated by parent
`af5f5ab4`. Worktree: `/private/tmp/krogu-math-task-525-floating`.
Branch: `task/525-floating-conversions`.
Exact unchanged base/HEAD: `955799277281f740c43d262c23892167c2c36026`.
Ref-less `kotgent task show` confirmed `local:525`, `in_progress`, before work
and again before handoff. Project krogu-math is not archived. The explicit
ownership split in parent activity1881 permits this slice despite the archived
task525 worker. The parent owns operator changes, integration and execution.

**Source/proof phase ready; compilation and every runtime gate UNRUN.**
Changes are not committed. No Git mutation or task-state/link/configuration
mutation was performed. Keep task525 linked and in_progress. No claim that the
full task or a parent goal is complete. Wait for a precise later execution
grant; root1875/1880 and then task528 own the reserved runtime windows.

## Narrow change

Exactly six files belong to this handoff:

| File | Change |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/NumericExtensions.kt` | Eight appended lines: four floating overloads and two comments; prior 17 lines frozen. |
| `src/commonMain/kotlin/io/heapy/krogu/math/FloatFormatting.kt` | New 90-line own common binary32 selector/renderer. |
| `src/commonTest/kotlin/io/heapy/krogu/math/migration/KotlinFloatingConversionContractTest.kt` | New 128-line suite, six prepared methods. |
| `src/jvmTest/kotlin/io/heapy/krogu/math/migration/KotlinFloatingConversionConformanceTest.kt` | New 102-line suite, four prepared methods. |
| `docs/kotlin-floating-conversion-migration.md` | New migration guidance, independent whole-domain proof, bounds, API links and explicit UNRUN status. |
| `docs/task525-floating-source-handoff.md` | This handoff and evidence record. |

Float selection decodes raw binary32 bits into integer units of 2^-149.
Midpoint intervals align exactly at decimal scale150 via 5^150. Clipped floor
and successor candidates preserve asymmetric intervals; budget two covers
canonical lengths one and two across adjacent decimal bands. Later budgets
remain in the center band and terminate by nine digits. Exact integer distance
and stripped coefficient parity select the result. No host formatter/parser
implements candidate selection, and Float does not use widened Double text.
Context applies to selected canonical text. Double delegates existing public
own BigDecimal.valueOf(Double), then round(mathContext) where required.

The proof establishes the candidate-search argument for every finite binary32
pattern, conditional on existing exact integer/decimal operations and raw-bit
contracts. It explains open/closed endpoints, the virtual maximum successor,
small subnormals, the minimum-normal join, signs/zero, decimal carries,
one/two-digit selection, candidate sufficiency and canonical tie parity.
Bounds include eight budgets, at most twenty candidate visits, nonnegative
bounded unit powers and indices, and intermediates below 10^190. These are
mathematical bounds, not benchmark or universal execution claims.

## Prepared method inventory

Common `KotlinFloatingConversionContractTest`:

- `primitiveLiteralsPreserveCanonicalTextScaleAndSelectKroguTypes`
- `floatBitBoundariesRetainAsymmetricAndDecimalTieRegressions`
- `smallSubnormalsUseTheUnionOfOneAndTwoDigitDecimals`
- `floatTextDoesNotUseWidenedDoubleOrTheExactBinaryConstructor`
- `contextsRoundTheSelectedDecimalWithAllEightModes`
- `nonfiniteInputsRetainNumberFormatErrorsWithAndWithoutContext`

JVM `KotlinFloatingConversionConformanceTest`:

- `literalControlsAgreeWithBothPublicStdlibAndJdkText`
- `floatPowerBoundariesAndSmallSubnormalsMatchPublicTextConversions`
- `doubleBoundaryExtensionsReuseThePublicCanonicalFactoryContract`
- `contextsAndNonfiniteErrorsMatchPublicStdlibForEveryRoundingMode`

Independent literal coefficient/scale expectations remain alongside public
stdlib/JDK comparisons. The Float family prepares all normal/subnormal binary
powers and neighbors; context controls prepare all eight modes. Historical
six signed failures at positive raw bits 0x0f800000,0x6b000000,0x6c800000 are
explicit common controls, as is the independently reasoned decimal tie at
2^-12. The archived old task525 draft, its failed campaigns and TWO enabled JS
Number failures were not edited, removed, disabled, moved or waived.
Task557 erased Number dispatch is outside this slice.

## Independent review and static evidence

Independent read-only reviewer `/root/floating_source_review` reviewed all
five code/test/proof files at their hashes below, plus existing own
DoubleFormatting.kt. Verdict: no implementation correctness findings. Reviewer
confirmed the tie expectation 24414062@11 and whole-domain search argument.
One minor wording correction qualified the half-gap equality at normal powers
of two to exclude the minimum normal; its half-gap is m*2^-24, satisfying the
unchanged lower bound m*2^-25. The reviewer confirmed the corrected proof hash
and no further actionable findings. No reviewer numerical execution occurred.

`git diff --check` passes for the tracked append. Separate
`git diff --no-index --check /dev/null <new-file>` checks of formatter, both
tests and migration proof emitted no whitespace diagnostics; these return
exit1 because the new file differs from /dev/null. The initial chained form
stopped at that ordinary first exit1, so each remaining file was subsequently
checked independently. No failed numerical or symbolic certificate exists in
this phase: none was run. This handoff file receives a separate final static
whitespace check before the tracker comment.

`git diff --exit-code` confirms unchanged BigDecimal.kt, BigInteger.kt and
DoubleFormatting.kt. Final status is restricted to the six owned paths.
No compiler, Gradle, javac, Native link, test/harness, numerical probe, black-box
campaign, generator, benchmark or standalone script was invoked. No process
was cancelled and no dependencies/caches were added. No outside writes were
needed. Compiler resolution and actual target behavior still need later gates.

## SHA-256 source freeze and provenance

| File | SHA-256 |
| --- | --- |
| NumericExtensions.kt, final | `39d7fa3e539305b5a0ffbdf5dfa9d409f9262b195dbe0f1360b44260cec8277d` |
| FloatFormatting.kt, final | `ba55514c181e52f20ed63fcb2634621f66cea3cc4db084e77cf2df71d1dcf77e` |
| KotlinFloatingConversionContractTest.kt, final | `e95b4029da6c781746aeb206649b7777f97bef2030edd9e1859af0df796f5556` |
| KotlinFloatingConversionConformanceTest.kt, final | `178aca984dea5292bb487ce851144f81b179316d7e1447af65fb995b28923d28` |
| kotlin-floating-conversion-migration.md, final corrected/reviewed proof | `05f3b6564224f9782fb0c1ffa814a957114f9e4b37fedd776808ce32970549fa` |
| NumericExtensions.kt, frozen base | `0f8e1e115a7f4e9f9ad4df29979a38ecc23701ff0f208a882f255dfc6941a94f` |
| DoubleFormatting.kt, existing own source, unchanged | `9b2a1b8cb1d40a2fdc7db5110b6a8c4e398da0562d5c3d6b02cf928f3c74433f` |
| Archived own NumericExtensions.kt, read-only historical input | `3881ffa6456d5e32553d7303ef22466ca4965ce2d57d1441dbbf169c8f16a426` |

The historical file is
`/private/tmp/krogu-math-task-525/src/commonMain/kotlin/io/heapy/krogu/math/NumericExtensions.kt`.
Its nearest-plus-neighbors/back-conversion algorithm supplied historical
context, not imported private Double code. The evolved current own interval
design was adapted with a new fixed binary32 lattice. All new production code
and tests carry Apache-2.0 SPDX headers. No foreign numerical implementation
source was accessed. Public API links used for contracts are collected in
[the migration/proof document](kotlin-floating-conversion-migration.md):

- [Kotlin2.4 Float/Double toBigDecimal](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/to-big-decimal.html)
- [JDK25 Float.toString](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Float.html#toString(float))
- [JDK25 Double.toString](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Double.html#toString(double))
- [JDK25 BigDecimal.valueOf(Double)](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#valueOf(double))

The handoff document's own hash belongs in the ref-less task comment rather
than in this self-referential table.

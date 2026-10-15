# Inexact sqrt normalization: local verification (579)

The rounded square root no longer requires an unsupported doubled working
coefficient when its required final coefficient and scale fit. The new route
streams decimal digit pairs, including a virtual zero suffix, into private
paged root/remainder/trial buffers; it publishes a BigInteger only after
rounding, precision carry, preferred-scale stripping and final range checks.

This is the actual local integration milestone. The
[proposal](sqrt-normalization-579-proposal.md),
[implementation argument](sqrt-normalization-579-implementation.md) and
[truthful-leaf seam](sqrt-normalization-579-subtype-seam.md) retain their
source-preparation labels and historical hashes. Their uncompiled/unrun labels
describe those earlier preparation milestones; the separately bound execution
below supplies subsequent evidence. No earlier failure or observation is
rewritten.

## Selection and mathematical scope

The old finite-root563 prefix and midpoint565 path remain. Ordinary
nonpositive normalization shifts retain the supported old path. For positive
shift, the new path is considered only above precision268435455 and only when
the captured reported exponent agrees with the stored coefficient digit count.
This is an eligibility optimization, not a precision cap: for smaller truthful
precisions, D+h<=2p implies 4(D+h)<=8p<Int.MAX_VALUE, the working power is within
its existing ceiling and the working scale fits Int. Larger precisions continue
to the complete raw argument when conservative materialization cannot be proved.

The restoring invariant is Nprefix=a²+r, a=floor(sqrt(Nprefix)),
0<=r<2a+1. Each decimal pair uses the greatest d in0..9 with
(20a+d)d<=100r+pair. At completion integer N is above the midpoint exactly
when r>a; equality is strictly below, and an integer radicand cannot tie.
All eight rounding modes, the one exact precision-carry drop and subsequent
preferred-scale stripping precede final public coefficient/scale checks.
Long positions/scales and checked paged cells cover all allowed precisions;
the implementation proof records maximum active lengths, primitive bounds and
all five simultaneously live workspaces.

The symbolic sqrt(2), precision400000000/DOWN witness is repaired by this
construction without forming tenPow(799999998). **That giant public input was
not executed.** The proof does not promise feasible time, heap or allocation
at that size. The restoring algorithm, repeated input division, zero stripping
and final conversion can be expensive.

## Actual four-target campaign

One fresh no-Git archive of exact35086 plus the four frozen579 source overlays
was built at /private/tmp/krogu-579-validation-3YTLxS/root. Only the provisioned
Kotlin tooling home was reused; build/results/classes were fresh. The retained
command uses pinned Gradle9.8.1/Kotlin2.4.21/Liberica25.0.4+9, in-process
compilation, one worker and the original4GiB Gradle heap:

- jvmJar and full math-package jvmTest;
- full math-package jsNodeTest;
- full math-package wasmJsNodeTest;
- full math-package macosArm64Test.

Root execution28130 exited0: BUILD SUCCESSFUL in16m41s, all49 actionable tasks
executed. Source-derived inventories exactly match the completed XML methods:

| Target | Suites | Methods | Failures/errors/skips |
|---|---:|---:|---:|
| JVM | 151 | 514 | 0 |
| JS | 70 | 329 | 0 |
| Wasm JS | 70 | 329 | 0 |
| macOS ARM64 | 70 | 329 | 0 |
| Total executions | 361 | 1501 | 0 |

Every expected method appears exactly once; there are no missing or extra
methods or duplicate suites. All223 source-manifest entries match both the
completed isolated tree and the integrated publication working tree.

Three new common methods ran on every target; one new JVM method ran once,
giving13 new method executions. Direct raw-kernel tests use64 bounded independent
Long bisection/midpoint cases across all8 modes, including output above2^30,
two-cell nonzero remainders, a low-cell borrow and decimal carry. Separate
controls cover preferred stopping, Long-scale rescue/rejection, scale parity,
all modes and the retained exact-root shortcut at maximum requested precision.
The full retained563/565, oracle, scale and dispatch suites also ran.

The JVM method uses three current-own-format roundtrips with truthful
transitive quotient/remainder subclasses. It checks24 direct raw calls and48
ordinary/restored public controls, including expected exceptions, against
independent literals. The bounded public controls retain the materialized path;
only the direct kernel calls exercise raw arithmetic at these practical sizes.
No fake smaller production threshold was introduced.

## Binding and evidence

| Artifact | SHA-256 |
|---|---|
| BigDecimal.kt | 4ab8f15b10f2aa2093e4e04e2a7e4aa6226a7230be24dead3bbe75bcde9e5cc5 |
| DecimalSquareRoot579.kt | ce779a45a568a49f54c631359c6e13314ee8d05272c6d915dbc12bf7bc7be408 |
| Common test | 0e406179a587a29c80995c8c0ac7c94586b8d43b0d04cd334d57f15d622c4580 |
| JVM test | 9928e854fd19461e5601a589dcff1f47862054d238a34d4e12c527946b2a3174 |
| Actual unchanged BigInteger | e4dc75b253c689b0f3c634f02f9e491c9a1dcfae396a11135be4427c69ce7b68 |
| 223-source manifest | 25bc9cce8426a00861c480810ea99aad47c5ac504d91c98582fdf8e4294ccfb4 |
| Source test inventory | 5806b33befdca64cbae401e5ff1370f62c8157e48cbfb6343532fae7e7525ac4 |
| Full build/test log | e88f1bdd1e48fe8652afcb99c0e324bc6c767a32bbcb03593c810fd4cdc9623b |
| JVM exact XML audit | af6584fdefda431c96f7a0478749e0fa355a1e88b45021d22fda7470de46c073 |
| JS exact XML audit | a0403a762213f456c05948a0e9c1a143c2a7a2d309b8f7bebc5b2d8d1bc505fe |
| Wasm exact XML audit | 71cff51745697723afcfcba36ada5e4e372f9fd838e07c240637187dff211d3b |
| Native exact XML audit | caf0455208e6ed5d539e9f94da863d33bea243a9da3c037d3ea571c4493c0b44 |
| Post-run source check | c571b2a0f1ed8e4656c41d32a99143b95fcd428697724acb8544e00de271da3a |
| Fresh own JVM JAR | e7047f824deb0e4f9170ad223037a0d42f49cdfa3025eed16b776ece9be9c5ea |

Raw XML, command, log, source checks and fresh own JAR are retained in the
isolated validation evidence directory. Original source/proof/test revisions
remain preserved separately. Independent reviews accepted the mathematical
construction, actual indexing/bounds, corrected multiword test cases and
truthful-leaf source seam before execution. Independent final review accepted
all four actual target inventories, source/JAR binding and the candidate for
integration, with no remaining finding in that scope.

All owned compiler/test invocations are terminal. The explicit local numerical
mutex was released (empty revision780); no other worker receives an implicit
execution grant. Task580 is a separate source-only repair for contextual
multiplication's exact-product obstruction.

## Limits and remaining gates

Later actual hosted evidence: exact1b9 normal run38783134507 completed with six
jobs SUCCESS and the public job FAILURE. Its complete ledger is105 PASS,1 FAIL,
46 SKIP; port SquareRootTests timed out at120113ms under its original120s/512MiB
regime, while baseline passed in231ms. See the [retained failure audit](public-math-1b9-regular-failure-559.md).
The producer/consumer JAR is this e7047 artifact; no failing assertion or exact
stalled input was recorded. The separately accepted same-source large12/0/128
run is documented in the [large evidence report](public-math-large-ci-559.md#actual-hosted-success-1b9-and-the-accepted579-artifact).
Matching full-workflow acceptance therefore remains open. The pending text
below describes the earlier local milestone, not a later all-seven pass.

Matching-head supported CI and hosted public campaigns for this new source/JAR
remain pending. The previous35086 large12/0/128 result is historical evidence
for its own856fc JAR, not this e7047 JAR. Official JCK has not run.

The ordinary theorem and the stated truthful coefficient-leaf composition do
not waive dishonest/changing sign, precision, division results or conversions.
Affected inherited callback staging remains541/526 classification; explicit
caller overrides and super entries retain their separate contracts. Legacy or
cross-format serialization is not required. Task548 resource/range work,
the broader542 power composition, Number/JS migration and the full project
goal remain open. Local finite passes are not an all-input or maximum-resource
execution claim.


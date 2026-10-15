# Task566 source milestone handoff

**PREPARED / UNCOMPILED / UNRUN.** This is a source milestone, not task
completion or runtime compatibility evidence. Task remains linked and
`in_progress`. Changes are not committed. No Git index, history, branch,
session link or task state mutation was performed.

## Identity and scope

* Task: `local:566`, pane `01660b06`; parent root526 / `af5f5ab4`.
* Worktree: `/private/tmp/krogu-math-task-566`.
* Branch: `task/566-decimal-scale-rounding`.
* HEAD/base: `96ddbf7b9241f14265620c72a8217c32c4cdb4ff`.
* Base BigDecimal SHA-256:
  `5de18160de8d6787613a45a0b9d99e984921676b3dbd5aa5368833ebc1b7a6d7`.
* Initial worktree was clean. Only the production file and new sources/documents
  listed below belong to this milestone; no existing draft was removed.

The production diff is restricted to the zero branch of `addContextValue`,
finite-context `multiply`, `setScaleValue`, and its new narrowly private
`roundSubunitCoefficient` helper. It has 33 added and 11 removed lines.
Root557 runner/README, primality profile, worker528 metadata, pow/sqrt/Float,
all integer kernels, frozen fixtures/assertions/diagnostic data, serialization,
build and CI sources are unchanged. Parent integrates serially.

## Result and independently derived witnesses

* Downscaling now partitions `k>D`, `k=D`, `k<D` before narrowing distance
  or constructing a divisor. `ONE@MAX.setScale(0,DOWN)` requires `0@0`;
  the maximum distance MAX to MIN works likewise. The k=D helper uses
  supported `10^(D-1)`, leading-digit versus five, and remainder zero.
  It never needs the unsupported endpoint `10^D` or a multiplied threshold.
* Finite nonzero HIGH multiplication with precision-required coefficient
  reduction rounds at neutral scale and adds the correction, including carry,
  to the logical Long scale before final validation.
  `ONE@MAX * TEN@1`, p1, requires `1@MAX` under all modes, including
  UNNECESSARY. `99@MAX * ONE@2`, p1, rescues only when rounding carries.
  Exact/unlimited, LOW and zero/source behavior retain their original paths;
  D<=p HIGH errors are intentionally retained. Unsupported full product
  coefficients and unproved exact HIGH-cohort rescue remain open.
* Zero addition now chooses its finite-precision cohort before range checking
  padding. `ONE@MIN + ZERO@MAX`, either orientation, p1, requires `1@MIN`.
  Genuine selected-padding and rounding errors, zero-zero preferred scale,
  subtraction composition and explicit public caller semantics are retained.

The proof documents full-domain Long, decimal-digit, power, remainder-doubling,
increment/carry and cohort bounds, with symbolic endpoint witnesses that
allocate no giant input. New common source has independent bounded Long
expectations and separate wide literal rows, both signs/all modes, enum and
legacy/exact overloads, truthful combinations/direct-super, replacement and
exception identity/null controls. The JVM source retains public observations
without using them as replacement oracle assertions.

## Frozen source SHA-256

Paths are relative to this worktree. These hashes were independently reviewed.

| File | SHA-256 |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt` | `737b2325e161c1f73f59daa857830578a44bd34ce14d35a8e323ff84e92f53b4` |
| `src/commonTest/kotlin/io/heapy/krogu/math/DecimalScaleRoundingRepairTest.kt` | `0279a0c3a13fe5b841c5126ffebcbde40e37d900ef3d2ac9eb3e02208942de22` |
| `src/jvmTest/kotlin/io/heapy/krogu/math/DecimalScaleRoundingRepairJvmTest.kt` | `7a1b87849257b84a89997ab16f7e00314e1beb03413632bedcbc556292c6e7a5` |
| `scripts/diagnose-decimal-scale-rounding-566.main.kts` | `be72c0b05ce712eaf2e195c5558fd2434b6963e592b5aaf4c45f60d544b44cb0` |
| `docs/decimal-scale-rounding-566-proof.md` | `d34846ef39477756f141ac168dfdd4acee3f0bf065dc04f0e3109a9dedb0afd1` |

This handoff's own hash is recorded externally in the ref-less task comment
to avoid a self-referential hash. New files remain untracked for parent review;
they are not included in ordinary `git diff` output until parent integration.

## Independent review and allowed checks

Read-only reviewer `/root/source_review_566` inspected implementation, common
expectations, JVM literals, proof and diagnostic, then rereviewed the final
hashes above. Verdict: no concrete arithmetic, expectation or proof defect;
compilability cannot be certified by source inspection. The reviewer found a
parent-classpath attribution risk in the draft diagnostic. It is corrected:
all four reflected port class origins are logged and checked against the
supplied JAR before numerical rows. The suggested carry-only standalone row
was also added and rereviewed. Reviewer executed no numerical work or mutations.

Milestone comment1950 remains the historical snapshot, whose production hash
was `49930c68dfddd6fa2e3861ec471a0c040045c45d509687588ab70292c170b444`.
Parent comment1953 records a second independent acceptance with no correctness
finding and authorizes only one follow-up: reuse the private stored digit count
locally during downscale. The final source now calls `storedPrecision()` once
inside the negative-shift branch and reuses its Long result for k>D and k=D.
It does not evaluate that helper on the increasing-scale, zero or equal-scale
paths. All numerical branches, tests, proof and diagnostic protocol are unchanged;
there is no cache field or new public callback. Only production source and this
handoff changed after1950. Focused read-only review by
`/root/source_review_566` found no issue with this correction or hash bindings.
The current hashes above supersede only the affected1950 binding. Root526
retains responsibility for later integrated runtime verification.

Executed checks: source reads, Git status/base/diff reads, SHA-256 reads,
`git diff --check` (exit 0, no whitespace errors), and per-new-file
`git diff --no-index --check /dev/null <file>` (exit 1 for new content, no
whitespace diagnostics). No compiler, script, symbolic certificate, helper,
fixture, JVM/JDK probe, test, build, benchmark or public harness was attempted.
All source-only invocations and the reviewer are terminal before reporting.
The project was confirmed non-archived; session remained solely linked here.

## Remaining execution and integration gates

Native task528 / `f5b3bd62` grant1935 owns the sole numerical execution
window. A separate later explicit parent grant is required before any of the
following; this document grants no permission to execute them now.

1. Reconfirm source hashes, task link and serial integration scope. Build fresh
   baseline and candidate own JVM JARs under the pinned toolchain and record
   exact input heads/source hashes, JAR hashes and JDK identity.
2. Compile and execute the bounded standalone diagnostic separately for each
   JAR, preserving the entire stdout and stderr even on failure. Intended later
   invocation: `kotlinr scripts/diagnose-decimal-scale-rounding-566.main.kts <fresh-own-jvm.jar>`.
   Use the pinned Kotlin runner and Liberica25.0.4+9. The diagnostic validates
   loaded origins, records all value/error/message outcomes and has no early
   equality assertion, waiver or JDK-defect label.
3. Compile and run the new common/JVM regressions, the existing rational range
   certificate as appropriate, and complete JVM, JS, Wasm and macOS ARM tests.
   Record actual baseline and public-JDK behavior before any oracle
   classification. Retain every existing frozen oracle control and diagnostic.
   A conflicting historical assertion needs narrow independently proved and
   actually observed classification in a later authorized phase; none was
   altered here.
4. Parent serially integrates, builds a fresh own JAR from the actual integrated
   head, and obtains exact-integrated-head ALL6 CI evidence. No full-CI result,
   merge readiness, runtime result or public JDK defect is claimed now.

Task566 stays `in_progress`; broad541/533/548 and the full parent goal remain
open. Unsupported full-product algorithms, arbitrary poisoned internal traces,
resource limits and other workers' numerical work are outside this source repair.

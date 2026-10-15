# Task578 terminal source milestone

**Implemented focused G6+G7 source migration. ALL UNCOMPILED/UNRUN.**

This is actual production, caller and JVM adapter source in the task578 worktree,
not the inactive task573 preparation. Root must integrate, compile and execute before
acceptance. No runtime result, public compatibility completion or JCK claim is made.

Identity: ref-less `kotgent task show` resolves local:578, in_progress, session
f3db6b0f. Worktree `/private/tmp/krogu-math-task-578`, branch
`task/578-validation-nonnull`, unchanged HEAD
`60aea413e0c7af007844aace6ec28ecdfa019b67`. Initial status was clean. The shared
project descriptor later reported task577's path; it was not reset. Work stayed in
this worktree. Other workers' files, root checkout and runtime trees were not edited.
Changes are not committed. No stage/commit/checkout/rebase/push or task transition.

## Implementation

The 13 exact public input slots in `slots.tsv` are now non-null: BI signed byte-slice
magnitude, PlatformRandom random-bits and prime constructors, prime factory and
modPow exponent; BD four context constructor slots, two legacy-int divide divisors
and sqrt context; MC rounding mode. Four dependent private helper signatures were
updated (BI signedBytes/randomBits/generatePrime and BD roundedDouble). Mathematical
kernels and valid dispatch paths are unchanged. Existing non-null constructors,
three nullable equals inputs, four min/max result declarations, four OrNull results
and other workers' input groups are unchanged.

The public non-null random-bits constructor now requires null rejection even for
zero bits. Java boundary source tests demand own NPE, while common and JVM zero-bit
controls require no random draw and no seeded-state advancement. Existing seeded
positive comparisons remain. Root's original observation remains byte-for-byte in
`root-rng-baseline-records.tsv`: own and JDK both returned zero for zero/null.
SHA-256 `0bcf73bf0ee265723a15fcb43561dde4e6594ce9c4bca37b6df80bafcc0f9437`.
No probe was run here. Root's reusable probe may still exit3 solely for the host
violation; ownViolations0 does not make that a blanket probe PASS.

## Exact execution accounting

`row-accounting.tsv` lists ordinal ranges and counts. Input predicates classify rows
before execution; no observed-failure filter or host-wide waiver is used.

* **3,462** frozen null-precedence rows move from commonTest to jvmTest, all through
  real Java constructors/virtual calls. Original label construction, loop ordering,
  frozen outcome consumption and the four zero obligations remain. Family ordinal
  boundaries, unique keys, ten family counts, 705 ordinary NPEs and 2,757 competing
  invalid diagnostics are asserted. Frozen `Jdk25NullPrecedenceResults.kt` is unchanged.
* **25** portable R rows move to JVM, keys and global ordinals **75613..75637** checked
  in both common partition accounting and JVM replay. All six input key fields and
  frozen seventh fields stay in the unchanged fixture. Twelve ordinary null cases
  require NPE; thirteen compound cases retain diagnostics. Zero/random ordinal75618
  explicitly retains frozen `0` while requiring own NPE. The other75,613 portable
  rows retain their common execution, including grammar, byte-isolation and arithmetic.
* **73** standalone common null executions have distinct Java counterparts with
  unique keys and ordinals0..72: 568=46, 569=10, 563=9, 565=3, 538=2, 567=2,
  value-identity=1. Of these,55 require NPE,8 retain compound-invalid diagnostics,
  and10 test caller-owned return/exception identity. The 568 cohort includes all35
  ordinary/truthful/explicit-super zero entries; both569 signed-zero bits survive.
* Thus **3,560 common null executions** have actual JVM counterparts. Common valid
  numerical, truthful, scale, rounding, override identity and exception controls
  remain; there is no claim that JS/Native can execute Java null boundaries.
* Existing reflection coverage now identifies exactly13 owned signatures and3,781
  owned input keys:819 ordinary NPE requirements and2,962 compound-invalid diagnostics.
  Other signatures retain their previous comparisons. Exact per-signature counts and
  unique owned keys are asserted; reflection lookup/invocation setup failures still
  escape the target-exception formatter and fail the test.
* Existing Java modPow explicit-super matrix retains all30 keys:10 null-exponent
  rows (4 ordinary,6 compound) and20 existing comparisons. Nullable callback matrix
  and internal returned-null trace assertions remain unchanged. Additional direct
  Java override controls distinguish caller-owned marker/throw results from actual
  `super.modPow` null rejection.

`Validation578Boundary.TruthfulRoot` contains actual Java `super.sqrt` calls; no
reflection is presented as explicit super. Direct caller-owned Java overrides can
receive null. Reflection is virtual. No adapter throws an expected NPE, uses caller
`!!` or null casts. JVM observations accept nullable results so a returned null
cannot be converted into an adapter-side NPE. Competing-invalid priority, exception
messages and callback traces remain diagnostics with source-input classifications.

`AcceptedInputs578.kt.txt` and `RejectedInputs578.kt.txt` supply13 positive and13
independent negative compiler obligations plus non-null override examples. They
are outside source sets and have not been compiled. Root must compile each negative
body independently and require its intended nullability diagnostic.

## Preservation and hashes

Original tree archived before editing at `/private/tmp/task578-original/tree.tar`,
SHA-256 `0e22b13c9c238d5563eb099b9e18cbbd93519669b8a6972a479dc8f3068aef77`.
`baseline-tracked.sha256` covers all404 original tracked files. Its final check is
saved in `preservation-check.txt`:383 unchanged files,20 intentionally modified
source files and one moved/deleted common source path. The check's expected failures
are those21 paths only. This is preservation accounting, not a test pass.

All fixtures, generators, frozen diagnostics, build/toolchain files, current own
serialization code/tests, and accepted544V3/545V2 files retain their original bytes.
No external numerical implementation body was read. Five new active source paths
include the moved null-precedence test; current `source.sha256` hashes all200 source
files. `source.diff` is a complete recursive diff against extracted original `src`,
including all new files and the removed common test.

| Artifact | SHA-256 |
|---|---|
| source.diff | e45d364097fbe3c58aa3de77a7b05ddd9f83ae3d17aa5823ca49214f5fc751a5 |
| source.sha256 | 25138b1a8cfb92b996282974a35e2de8fe49f5f1017a5c6980c9591eb61d3528 |
| row-accounting.tsv | 230bcc76710150c2dacb401ee7fc160ff3ce9729f90328a0d49e11b71b766a79 |
| BigInteger.kt | df529eaed79cd68a0e62bdc0c7feb1acfeba49389078d2827d285a26fa2fafe1 |
| BigDecimal.kt | e879e4c769d7a04dd5727778401abf0614b3f4095a63f0375d0b67669f40e1ca |
| MathContext.kt | d3690387b1345f90cc927044a1a450983dd0d2ce6e3e47054384c31979fd5261 |

## Independent review and parent continuation

Independent read-only reviewer `/root/source_review` reviewed production signatures,
callers, Java adapters, explicit-super distinctions, RNG cohort ordinals, reflection
selectors and displaced-row accounting. It caught and confirmed correction of a
reflection ordinary-count typo (919 to819), suggested typed Java null locals and
nullable RNG result observation, and confirmed both fixes. Final disposition:
**no remaining actionable source findings; ALL UNCOMPILED/UNRUN**. It did not claim
runtime acceptance or inspect foreign implementations.

Performed only task/source reads, semantic-search attempt (no index), source edits,
archive/copy/hash/diff and `git diff --check` (clean). No compiler, Kotlin script,
helper, certificate, Gradle, toolchain, test, numerical Java probe, build/JAR,
harness, benchmark, timing or allocation execution occurred.

Root owns integration of overlapping PortableOracleTest and reflection hunks with
other groups, pinned Gradle9.8.1/Kotlin2.4.21/Liberica25.0.4+9-LTS compilation,
exact13/aggregate48 boundary and negative-compiler checks, retained full targets,
current own serialization roundtrip, fresh artifacts/public tests, and exact-head
all6CI. Full573/549/525/526/JCK stay open. This terminal source worker ends now for
parent archive and does not wait for a runtime grant.

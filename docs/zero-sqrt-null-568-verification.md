# Zero square root with null context: task 568 validation

`BigDecimal("0").sqrt(null)` and `BigDecimal("0.00").sqrt(null)` now throw
`NullPointerException`. The Java SE 25 [class-wide null rule](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
applies to these ordinary, otherwise valid zero inputs. The
[square-root contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#sqrt(java.math.MathContext))
accepts every MathContext instance for zero without exempting null. The
preferred-scale rule continues to apply to valid contexts.

The production change moves the existing null check before zero's early
return. Negative-radicand staging remains as before; no universal precedence
for competing invalid inputs is asserted. Explicit caller overrides retain
ordinary dispatch and result/exception identity. Truthful subclasses and
caller-owned `super.sqrt` enter the repaired base method.

The [source proof](zero-sqrt-null-568-proof.md) and
[handoff](zero-sqrt-null-568-handoff.md) remain historical, byte-identical source
milestones. The actual executions below supersede their UNRUN status only for
the gates explicitly recorded here.

## Candidate and retained observations

Validation starts with published division commit
`4cc815d04156db10d665c1dae7b6ff3c748a9b0c` and overlays only the eight accepted
task 568 files. BigInteger remains
`d0e757ebd5955604d5fb66d8dda13b1c15b2ecb300a37a5fbcb1fcfaeb65c16f`;
BigDecimal becomes
`9f14989a75a21662f4dfd1b61bf71c4ad0c04bcc3d9e8be4b4038103b443ab21`.
The root source and isolated validation source match byte for byte.

The new JVM diagnostic actually ran on BellSoft 25.0.4+9-LTS. For each of
ordinary, truthful and caller-super zero origins, it retained these host results
separately from independently required own NPE assertions:

| Input coefficient/scale | Observed host coefficient/scale | Required own result |
| --- | --- | --- |
| 0 / 0, null context | 0 / 0 | NullPointerException |
| 0 / 2, null context | 0 / 1 | NullPointerException |

All six exact input keys and duplicate checks passed. Only the two ordinary
historical observations are pinned; no earlier subclass result was invented.
The same diagnostic checked 240 finite valid-context cases. Four new common
methods include 1,056 valid-context zero/scale controls across eleven literal
scale cohorts, every rounding mode and precisions 0/1/7/Int.MAX_VALUE, as well
as null, positive/negative and explicit-override controls. These finite witnesses
do not prove whole-domain square-root behavior.

The frozen null fixture and generator remain unchanged. Common oracle
adaptation selects exactly the two complete zero-input labels, preserves their
raw observations, requires NPE independently and retains the other 3,460
comparisons. The reflection consumer selects only the exact sqrt signature,
null argument and two ordinary receiver representations, retaining live host
outcomes. No output-driven exception or broad comparator waiver was added.

## Behavioral gates

| Target | Suites | Passed method executions |
| --- | ---: | ---: |
| JVM | 128 | 436 |
| JS | 61 | 291 |
| Wasm JS | 61 | 291 |
| macOS ARM64 | 61 | 291 |
| Total | — | **1,309** |

The JVM invocation completed in 1m 4s, exit 0. A source-derived audit validates
the complete suite/method set and all six exact new method names, with no
failures, errors, skips, omissions, extras, duplicate cases/suites or XML
anomalies. Both FSEvents warnings around BUILD SUCCESSFUL remain in the log;
there was no test retry. Independent read-only review accepted the actual JVM
XML, source hashes and retained host observations. JS then completed in 7m 45s,
exit 0; its source-derived audit confirms all 291 methods and four exact common
additions, with the same zero-anomaly checks. Wasm completed in 1m 57s, exit 0;
its audit confirms the same 291 common methods and four new names. macOS ARM64
completed in 3m 31s, exit 0, with the same 291-method/four-addition audit. All
four gates have zero inventory/XML anomalies; the total counts executions
across targets, including 18 new executions, not distinct test definitions.

The 199-file input manifest before, after JVM and after JS is identical, SHA-256
`fb108c4b8db7fc56b9176f42853ffd18ad47602cde50e2a123f7e6584dbe8130`.
It covers production/test sources, build/wrapper files, Gradle metadata,
workflows and pre-existing Kotlin JS store files. The separately prepared
auditor retains prior source parsing and XML rejection rules; it adds exact
named inventories and checks the retained d0 baseline before deriving totals.
Auditor SHA: `d90dd1bf07c5d52dfe0fb460ccec45b9cc2d304422229b24a7ba6951f9390621`.

Wasm generated one previously absent output, `kotlin-js-store/wasm/yarn.lock`,
SHA `970a85c50fa072f8543e444af2d28a16de073c3e7ead47b86527d83a557b4dcc`.
The raw after-Wasm inventory has 200 files and only that addition; every
pre-existing input matches. The new output and exact inventory diff were
preserved separately. After archiving it outside the runtime root, the complete
199-file inventory again matches `fb108c4b…`; the after-macOS inventory also
matches. It was not unchanged throughout Wasm execution. This first Wasm
invocation succeeded; there was no retry.

Evidence is retained under `/private/tmp/krogu-568-integrated-validation-v1/`:
frozen initial input archive, sorted manifests, runtime root, JVM XML and logs.
JVM log SHA: `3e7f98586c844ae5cf5df41004dba18c3b072031fe6ddaff2f7210e59661ce49`;
audit log SHA: `a8e92c18d4c3c635f937f0f56c32ebcc0116f3f6002759dc7ca314786f67f751`.
JS log SHA: `63b812861adfc45c44258fb741c51a880d0dd4b5360c712f0b69471bf10e4682`;
audit log SHA: `12f4dae07dca84b1854c48185a62ceff4754d35963eb46a87d91dd606a51e7f0`.
Wasm log SHA: `fe8017510cd9176a246c82af97bb745169c78b52b254a4d3a9cfd2b3927cea71`;
audit log SHA: `9fec9c4dd74e8d901b083d880c3f230aaff7a1a958c53993eefb34ba93999cbd`.
macOS log SHA: `91832f9e879ed16127ffe28a2b27ba261bbea1e19ee9b29b72eb027af294f8cd`;
audit log SHA: `3733d94fea56e7e030f86c080f684d54f789ea4f5aed87b7fc3539cd3179095b`.

## Fresh artifact and selected public tests

A forced `jvmJar --rerun-tasks` build of this exact snapshot completed in seven
seconds, exit 0, using Gradle 9.8.1, Kotlin 2.4.21 and Liberica 25.0.4+9-LTS,
one worker, in-process compilation and a 2 GiB Gradle JVM. The before-build
(after-macOS) and after-JAR 199-file input manifests match `fb108c4b…`.
The freshly copied JAR is
`/private/tmp/krogu-568-integrated-validation-v1/artifacts/integrated-fresh.jar`,
SHA `86ac6b849d98870fcca6e29103b8e9b47fb8132b72fa1a5a1ab98f9d0ec8f673`.
Build log SHA: `51a5fd62830607c194032d50c918aa6ebb1611e53a9aa4f52d5d7f78ddc9481b`.

The unchanged public OpenJDK selection of AddTests, CompareToTests,
MultiplyTests, PrecisionTests, RoundingTests, ZeroScalingTests and
SquareRootTests then completed both baseline/port phases: **14 PASS / 0 FAIL /
126 declared SKIP**, complete=true, officialJck=false. All fourteen compilations,
seven port core-loading checks and nine bytecode audits passed; no host math
member references, OOM or timeout were observed. The exact fresh 86ac JAR,
original assertions, operands, directive limits, 512 MiB heap and seed 559 were
retained. Test-tag/runtime revision mismatch and declared lost coverage remain
recorded; this is not a full public-suite pass. No unchanged BigInteger public
campaign was repeated or relabelled as new task 568 evidence.

Results are retained at `build/math-tck/results/1792046486318/`, JSON SHA
`2c7891703b2f59d2e68b6d3ac7ac0e36e70e86bd60357ddc47a3e80ec2761ddc`.
The complete result archive `/private/tmp/krogu-568-public-v1-results-first.tar`
has SHA `061c383ead9cc55e85a7f1d0f3dae01f5253ea901a79684720e3bd651f9da6db`;
parent log SHA: `ae14d7509dfde013d4fd06a2256191e9e50ec27ed1b3801e53f5e7de568b2904`.

The combined retained evidence, including input/source archives, all four XML
sets and logs, fresh artifact, frozen auditor source and public results, is
`/private/tmp/krogu-568-final-evidence-v1.tar`, SHA
`9c2c78edfe0d8b2a4d8e2d573406b3a539fe1016bcd495a5e45e18ccfe428a77`.

Publication acceptance additionally requires fresh six-job CI for the published
commit; its exact head and run belong in the task's final tracker entry. Prior
task 530 results validate their earlier BigDecimal source separately. No
official JCK or complete compatibility claim follows from this repair.

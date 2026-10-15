# Double signed-zero context: task 569 validation

Kotlin construction `BigDecimal(0.0, null)` and `BigDecimal(-0.0, null)` now requires `NullPointerException` independently of host behavior. Both signed zeros are finite; the context is their only invalid argument. The Java SE 25 [class-wide null contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html) applies, and the Double/context constructor provides no zero exemption. Valid contexts retain coefficient zero and scale zero.

The production repair moves the existing context guard after `fromDouble` and before zero's early return. Nonfinite validation remains first. Its retained staging for NaN/infinities with null is not a universal precedence rule for competing invalid inputs. No constructor signature, floating conversion extension, or other numerical API changes. This evidence concerns Kotlin construction, not Java constructor ABI.

The [source proof](zero-double-context-569-proof.md) and [handoff](zero-double-context-569-handoff.md) remain unchanged historical UNCOMPILED/UNRUN milestones. Executions below supersede that status only for explicitly completed gates.

## Snapshot and bounded consumers

The isolated candidate overlays the seven accepted task 569 files onto published task 568 commit `5fb00ee449570effbd27556ad4c248a26faf787c`. BigDecimal is `57a5af7b…`; BigInteger remains the accepted `d0e757eb…` division source. The null fixture and generator remain unchanged.

The common frozen consumer retains 3,462 rows: 3,458 original host comparisons plus four independently required NPE assertions, comprising the two task 568 sqrt inputs and two task 569 signed-zero inputs. Exact input keys, counts and duplicate rejection bound selection. Raw host observations remain asserted separately. The reflection consumer selects only the exact Double/MathContext constructor, null context and signed-zero raw bits; existing sqrt selections and all other comparisons remain. Neither policy depends on an observed output.

## Fresh artifact and paired replay

A forced fresh JAR build completed in eight seconds. Artifact `/private/tmp/krogu-569-integrated-validation-v1/artifacts/integrated-fresh.jar` has SHA-256 `96af86730b4f492061df0a8ba2b185160644e2b18306fc10332703084957cfb1`.

The frozen baseline replay (`a0424c45…`) exited 3 with exactly two zero/null violations: both host and old own code returned `0@0`. Candidate replay (`80dafd33…`) exited 0 with 70 rows, 140 constructor calls and zero violations. Host zero results remain visible; own zero/null results are NPE. All 64 valid-zero controls passed across both signs, all eight modes and precisions 0/1/7/Int.MAX_VALUE. Finite/null and nonfinite/null observations remain separate. Artifact identity and before/after hash guards passed on BellSoft 25.0.4+9-LTS with Kotlin 2.4.21.

Raw evidence is in `/private/tmp/krogu-569-baseline-probe-v1/` and `/private/tmp/krogu-569-candidate-probe-v1/`, with their adjacent `*-evidence.tar` archives. Candidate log SHA-256: `76970bf821b8593a8b932fb1e3cc4ba02b24fb6147b2b6b7446d4f7aa02dcb34`.

## Behavioral gates and input integrity

| Target | Suites | Passed methods | Duration/status |
| --- | ---: | ---: | --- |
| JVM | 130 | 443 | 1m 19s, PASS |
| JS | 62 | 295 | 6m 8s, PASS |
| Wasm JS | 62 | 295 | 1m 32s, PASS |
| macOS ARM64 | 62 | 295 | 3m 9s, PASS |

Source-derived auditors accepted the complete inventories and exact seven new method names: four common and three JVM. All four targets have no failures, errors, skips, missing/extra/duplicate cases or XML anomalies: 1,328 method executions, including 19 new executions. Independent read-only review accepted the actual XML, logs and input bindings. Finite/null, exact/nonexact and valid-zero controls are bounded witnesses, not whole-domain proof.

Logs, XML and manifests are under `/private/tmp/krogu-569-integrated-validation-v1/`. The frozen 201-file manifest SHA-256 is `9a9eeba89cb7c775ee5a853ff3f91e934fc7874044452ec5681efc67b3af2cdf`; after-macOS and after-JAR manifests match. Wasm's raw inventory had 202 files: only a newly generated `kotlin-js-store/wasm/yarn.lock` (`970a85c5…`) was added. Archiving that output outside the runtime root, with its exact diff, restored the identical 201-file inventory. The complete inventory changed during Wasm execution; every pre-existing input remained unchanged. The publication root matches 200 raw file hashes; `gradlew.bat` differs only by exact LF-to-CRLF conversion, verified by a bounded metadata check and identical Git blob identity. All production/test source bytes match.

## Selected public tests and publication

The unchanged selected public OpenJDK decimal campaign completed both phases: **14 PASS / 0 FAIL / 126 declared SKIP**, complete=true, officialJck=false. AddTests, CompareToTests, MultiplyTests, PrecisionTests, RoundingTests, ZeroScalingTests and SquareRootTests retain their original assertions, operands and directive limits, seed 559, standard/heavy profiles and 512 MiB heap. The exact fresh JAR above and unchanged runner `b73026d2…` were used; this is a selected campaign, not a full public-suite pass.

The first attempt exited 3 before any numerical test because the sandbox denied process enumeration at the runtime-version precondition. Its incomplete results retain zero executions. That evidence is archived separately; a scoped process-access retry then completed the unchanged campaign. Results are at `build/math-tck/results/1792050088403/`, JSON SHA-256 `bce2ab37139fe0818bdcb5978399a6ba453c0016103264356c6675678e50cbba`.

The combined local, baseline/candidate replay, public-test, failed-infrastructure and publication-provenance evidence is `/private/tmp/krogu-569-final-evidence-v2.tar`, SHA-256 `ecf83dc1df46628e5b050c1876dcf700b54501f6ecd8eb69c6b72df245e07198`.

## Exact published-head CI

[CI run 38701013714](https://github.com/Heapy/krogu-math/actions/runs/38701013714), attempt 2, completed successfully at exact published commit `99c81a42a5d104e045bcfe6eca2246231d37bf80`, with all six jobs successful. Actual Linux ARM64 job `116335819194` executed 295 tests in 62 cases: all passed, with no skipped or failed methods, in 324,548 ms. Final CI JSON SHA is `49e39548688f861ee642d237794f491c25ecbd9b558a58dc97edf9eb6d1f2124`; raw ARM log SHA is `5dc459597158a0ba4a4efdcd29d39527eecae90edf50d0bb3d35f270fc287c1e`.

Attempt 1's Intel Gradle build succeeded, then artifact upload exhausted HTTP-timeout retries; its dependent ARM job was skipped. The failed-only rerun supplied a successful Intel job and the actual ARM execution while retaining the earlier successful jobs. This is not six fresh executions. The original failure evidence remains preserved.

Final V3 evidence combines V2 with both CI-attempt records and the raw ARM log: `/private/tmp/krogu-569-final-evidence-v3.tar`, SHA-256 `67a672a97fed64dbe350963663094f28b0e773da5a26d00534cd7c6856f8e7fe`. Independent final review accepted the exact-head CI and source binding; task 569 is done. This does not establish later constructor candidates, official JCK, full compatibility or performance.

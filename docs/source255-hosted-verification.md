# Published source255: supported platforms and oracle checks

SPDX-License-Identifier: Apache-2.0

Published commit `12d0fe287cdec99567bd7cbf981b65aa86ef834b` has accepted hosted evidence for **5,855 passing method executions across 13 target instances**, plus **25 separately verified oracle commands**. These are the supported-platform and oracle jobs of [Compatibility run38858645066](https://github.com/Heapy/krogu-math/actions/runs/38858645066), attempt 1, on 2026-10-16. Complete source-derived class/method sets, actual counters and producer/consumer provenance were independently reviewed before this document was prepared.

All seven job APIs report completedSUCCESS. **Normal-public ledger acceptance is excluded here**: its job status alone supplies no106-public-result or118-retained-ID claim. Public TEST coverage, resource profiles, licensing and adaptation losses require their separate actual report. This is not official JCK/TCK, whole mathematical-domain/resource acceptance or evidence for later source595/596 work. Source253 remains a separate historical revision.

## Actual complete target inventories

| Job / executed target | XML suites | Passing methods |
| --- | ---: | ---: |
| Ubuntu JVM |175|584|
| Ubuntu JS Node |87|391|
| Ubuntu Wasm JS Node |87|391|
| Ubuntu Linux x64 Native |87|391|
| Windows JVM |175|584|
| Windows MinGW x64 |87|391|
| macOS ARM JVM |175|584|
| macOS ARM Native |87|391|
| iOS ARM simulator |87|391|
| macOS Intel JVM |175|584|
| macOS x64 Native |87|391|
| iOS x64 simulator |87|391|
| **Twelve XML instances** |**1,396**|**5,464**|
| Bare Linux ARM64 |not XML:87 groups|391|
| **All13 executed instances** |**1,396 XML suites plus87 ARM groups**|**5,855**|

Every XML suite counter agrees with its actual testcase nodes, with zero failures/errors/skips. Complete `(jobOS,target,class,method)` multisets equal all 5,855 predeclared qualified keys; per-instance class sets, ownership, freshness and duplicate/missing/extra lists also pass. The 12 XML instance identities and bare ARM identity are disjoint. These are instance-qualified executions, not 5,855 different source methods. The new594 classes contribute 43 method instances: three common methods on each target, plus one JVM-only method on four JVM targets. Internal loop cases are not counted as methods.

Original names and raw XML remain retained. Normalization removes only exact platform decorations, never broadly rewrites a method/class name. iOS device `compileKotlinIosArm64` completed as compilation only and is not a 14th execution instance. Linux ARM linking was producer-only until its separate actual consumer run.

The bare ARM transcript contains391 distinct RUN names and391 matching OK names in identical order,87 matching group starts/ends with correct method counts, and one terminal391/87 summary plus PASSED391 footer. Its exact method/class set equals both the source-derived common set and accepted same-run Wasm set, including all three new common594 methods. No unfinished, failed, skipped, disabled, duplicate, missing or extra state remains. Its10,304,495-byte raw log SHA is `d0b0534990d3991682a0a7604e067972512714467147131380b11af7e3e50e9c`; its351011ms summary is diagnostic completion timing, not a benchmark.

## Exact source and producer-consumer provenance

The [source255 manifest](source255.sha256) hashes **`8427be10a953974f1b74348dec87ea0cbbbb6f93c6e806875b049e4ff2fb339d`**. The accepted fresh exact-head archive hashes **`d11d6b25f53032e7ee852c2e92e980a7f6ccff3017e7eb13cae4ea20c6d8b2c1`**. All255 source hashes and the complete source pathset were checked, together with configuration,13 harness pins and the25 oracle-source pins. Checkout logs and all accepted job/artifact APIs identify exact12d/run38858645066/attempt1. Mutable later source and earlier253 outcomes are not substituted.

The same-run Linux own-JAR package contains exactly `krogu-math.jar`, `SOURCE_COMMIT` and `SHA256SUMS`. The successful binding step checks checkout HEAD against GITHUB_SHA; the extracted full head plus newline and both embedded checksums verify. Own JAR SHA is **`bb15a352df94be410ce79c310af6f541ee4206c59148dc30f81e9dc79270104b`**. It byte-compares equal to the independently accepted local594/source255 JAR and manual producer JAR. This is exact byte/provenance binding, not a new JAR execution or public TEST outcome acceptance.

The Linux ARM artifact contains only `test.kexe`,79,176,760 bytes, SHA **`b739f817ddf8b5f942594c8c1a0f11d03cf6ea9e11b70eade7cf27295eb46904`**. The consumer has no checkout: its original log names the exact repository/run and producer artifact12028169936, with measured download digest equal to the accepted producer ZIP. Its original steps are `chmod +x test.kexe` and unfiltered `./test.kexe`. The file-only ARM package has no invented SOURCE_COMMIT/checksum file; its source/build/link/upload/API/ZIP/download chain is retained independently of the JVM marker. No substituted/rebuilt consumer binary is inferred.

| Same-run artifact | ID | ZIP bytes | ZIP SHA-256 |
| --- | ---: | ---: | --- |
| Linux test results |12028693624|5484533|`5a4b717471d56f71de2da038b520d136a9d0a1db1aa06e8e089b454e75e926a8`|
| Windows test results |12029245258|3487928|`8f8503f835537b1f041449d91f1a9fa2c8c00c0104bd0e04386af91e4260c0d3`|
| Mac ARM test results |12028519233|4494174|`8d98fc40f39cd7ec6469c0b84350a06cc0d6f23a1f3ac0290104afa84541f83e`|
| Mac Intel test results |12028819173|4506612|`66963917170bfb4fb3aa90675147fb79ea9b987e02eb034c986e0cab3c46f850`|
| Own JVM producer |12028119866|174634|`73d599551e72867faa851ea86a451bcfb33f7a150e3315ca1d0400c144da49ec`|
| Linux ARM executable producer |12028169936|9517619|`04f35219b44f87474eacfae6e2705193d4bda8ff6ff3b0ab66ee6f2c7c3543eb`|

Each ZIP size/digest matches its specific API and successful upload/download log, exact run/head and upload interval. Result artifacts retain445/267/356/356 files respectively, including opaque Gradle result/state metadata; only XML contributes the1,396 suites. Their complete ZIP/extracted pathsets agree, with no unsafe/nonregular entries. No numerical implementation body/bytecode was read during these metadata reviews, and no JAR/executable was run by the reviewers.

## Original commands, resources and completed jobs

All platform commands retain the suffix `--max-workers=1 "-Dorg.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8" --stacktrace`:

| Completed job | Task arguments after `./gradlew` | UTC interval2026-10-16 |
| --- | --- | --- |
|[Linux116835691343](https://github.com/Heapy/krogu-math/actions/runs/38858645066/job/116835691343)|`jvmJar jvmTest jsNodeTest wasmJsNodeTest linuxX64Test linkDebugTestLinuxArm64`|05:59:06–06:27:12|
|[Windows116835691470](https://github.com/Heapy/krogu-math/actions/runs/38858645066/job/116835691470)|`jvmTest mingwX64Test`|05:59:06–06:13:41|
|[Mac ARM116835691500](https://github.com/Heapy/krogu-math/actions/runs/38858645066/job/116835691500)|`jvmTest macosArm64Test iosSimulatorArm64Test compileKotlinIosArm64`|05:59:14–06:17:22|
|[Mac Intel116835691374](https://github.com/Heapy/krogu-math/actions/runs/38858645066/job/116835691374)|`jvmTest macosX64Test iosX64Test`|05:59:07–06:23:11|

Their primary builds report successful46/11/24/23 executed tasks respectively. Later setup-gradle cleanup/noops remain recorded and add no coverage. [ARM consumer116843895918](https://github.com/Heapy/krogu-math/actions/runs/38858645066/job/116843895918) completes06:27:19–06:33:14; its unfiltered run step is06:27:22–06:33:13. The original ARM workflow declares no additional custom job/step timeout; separate public-job timeout settings are not ARM bounds. No input, assertion, method, filter, resource or timeout is reduced or increased to obtain this report.

Kotlin2.4.21, Gradle9.8.1 and BellSoft Liberica25.0.4+9-LTS remain bound to the original configuration/runtime metadata. Kotlin compilers run in-process;4GiB platform heap,one worker,UTF-8,JVM fast-throw suppression and JS Mocha120s remain unchanged. Native bundle/setup records retain2.4.21; Node/Wasm setup is recorded without inventing an independently measured Node-version pin. Restored caches, action-runtime warnings and ordinary compiler warnings remain in raw evidence. No cold-cache, hermetic-build, peak-memory or performance claim follows.

Configuration hashes are build `b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6`, Gradle properties `0e719146a69861ce1d5e0d33da043abf6fb3c1f81a67b7af617f9f44c249d2cf`, Compatibility workflow `af3813bbf060def341086f4dc4edcacc0c5f9059d39c40462c3888110af5be9b` and separate manual-large workflow `25d97734ca350b2cd4fd70750964be4549ba70b22246ec641954bfb9c0badb61`. Public TEST profiles/limits/license/adaptation obligations remain separate from this supported-method inventory.

## Separate25-command oracle record

[Oracle116835691495](https://github.com/Heapy/krogu-math/actions/runs/38858645066/job/116835691495) completesSUCCESS05:59:06–06:04:42. All25 original ordered `kotlinr` commands match the before-actual expected list, run once, and retain successful API steps and actual result lines. Every command retains `JAVA_TOOL_OPTIONS=-XX:-OmitStackTraceInFastThrow`; the V7 mapper alone adds `-J-Xmx512m` and `JAVA_OPTS=-Xmx512m`. Mapper SHA is `3e9552377df5702b483e5e325bb67de42b46280d7aa588d8533fee907f3e0dd7`; compact TSV SHA is `d116367be22ace568e46e1721c8aeeaf99442e3f1ab3aa1883c3e2624bd9519b`.

The mapper retains 75,728 mappings, 3,000 controls and 11,472 class masks; 8 required historical null-error statuses, 2,048 reference replacement-value prefixes, 280 historical-null error prefixes and 73,392 unresolved prefixes. All 75,728 archival blocks remain. Actual checked scopes and result/hash lines for all 25 commands remain in the separate oracle report; they are not summed as additional supported methods or a whole526/poisoned-subtype classification. Mathematical/cohort/error/identity/caller controls and all original diagnostics/assertions remain distinct and active.

## Frozen reviews and limits

Before actual results, source255 expectations were frozen at `/private/tmp/krogu-source255-hosted-expectations-EnKFx9Gm/REPORT.md`, SHA `5f42341e8777c6dc44591e17a89c9306f75cdab92c51427e3de2855a6c665029`,186-entry index `3f8f9f37c430cbbad2fa0626199948d98a839813367b867d24f86da807c08e6f`. Original publication UNBOUND labels remain historical; later exact12d bindings are separate. These declarations supply full method/instance/source/command expectations, not older outcomes.

The accepted final supported composition is `/private/tmp/krogu-12d-supported-final-PRijCY1O/REPORT.md`, SHA **`a89c2a3c069cffac6df5e73c34bd77a59fabef2e81fee46e908c66eee74e2593`**. Its256-input/50-external/65-output indices hash `1143d4b8437b07d6353e615c8f1842f8bdf35de967b46bf13f29beea38422fbb`, `249697cf4754a4e9d0aed3431b44b38df2126dd35c13963e3aa31aad6481ff8c`, `04c392ef2c1dfa406eb9607d5fa45267d91f9502005de343ea23b2bbb3fcd016`; all checks were independently accepted with zero bad hashes. It binds the full5855-key join, current ARM transcript and these already accepted platform reports:

| Frozen report root | REPORT SHA-256 |
| --- | --- |
|`/private/tmp/krogu-12d-windows-review-eYfPLfsT`|`67f1785973fe9f2a2ab15afa53cc1b9b676afe6d59ef41b47af037e828221c68`|
|`/private/tmp/krogu-12d-macarm-review-dirEjS5Y`|`423ebfa4428a4e1eed88e39a6391435b772aa10ea368b1ee796dd4b0e97b19fd`|
|`/private/tmp/krogu-12d-macintel-review-1KLSaO4e`|`2313aab3565bc4c9f8fe023fdc0075a9b3d99f61a3b6a30b5fa38a8f736b85a1`|
|`/private/tmp/krogu-12d-linux-review-lNiarK5K`|`de502b1adcafc101a78f8c11b8e41408af8031a41b9553191ff0d7a45a4731fc`|

The separate oracle report `/private/tmp/krogu-12d-oracle-review-z1dLVwWu/REPORT.md` hashes **`267d4c3b1d199687e9ff17b75d5f832d53ef83f391a2ad07f8777502efcb2efd`**. Its48-input/56-output indices hash `9dcf347c0e2780768cc94751ffde41c4b88bce11b1c915db554d5a5c5bee9d69` and `a0772e1c47e96beea751d4961315837bd0c9e2ddea423e75f4b2a250a915fece`; parent accepted all checks. Full raw APIs/logs/archives/ZIPs/XML/ARM names and per-command results remain in these workspaces. This document curates those accepted records without acquisition, re-execution or relabelling historical253 outcomes.

Reader diagnostics stay preserved: Windows' initial default XML-parser CDATA limit and corrected standard --huge read, minor target-qualified filename lookups, ARM's fractional-versus-whole-second timestamp comparison, and oracle's initial gh escape-sequence refusal followed by a corrected read of unchanged server evidence. None was a numerical/workflow retry or a reason to waive a failed assertion. Corrected metadata predicates preserve original observations, keysets and raw bytes.

Official JCK/TCK was not run. Finite methods and oracle commands do not prove every supported magnitude can finish within a fixed heap/deadline, unconditional subtype behavior, actual-randomness distribution or every mathematical API input. Source proofs retain their leaf/range/scale/resource/randomness premises; required caller/identity/error behavior is not blanket-waived. Number migration is not waived and the separate557/toolchain obligation remains unsatisfied. Later task595/596 source is outside exact12d/source255 and receives no validation from these old results. Historical failures, unresolved526 questions and other whole-goal obligations remain. Normal-public ledger acceptance and any retained-public-ID union require their separately reviewed record.

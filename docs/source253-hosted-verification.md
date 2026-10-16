# Published source253: supported platforms and oracle checks

SPDX-License-Identifier: Apache-2.0

Exact commit `62bb43ccd384d5678dd22d25a95c97cc18f51362` has accepted supported-platform evidence for **5,812 passing methods across13 target instances**, plus25 successful ordered oracle commands. These are the six supported/oracle jobs of [Compatibility run38852537605](https://github.com/Heapy/krogu-math/actions/runs/38852537605), attempt1: four platform jobs, oracle and real bareLinuxARM execution. This document combines three already independently reviewed and parent-accepted records without running or acquiring anything anew.

All seven jobs are reported API-success, but **normal-public evidence acceptance is excluded here**. This is not a106-public-result or118-retained-ID union report, official JCK/TCK, whole mathematical-domain/resource acceptance or evidence for later source255.

## Actual complete target inventories

| Job / target | XML suites | Passing methods |
| --- | ---: | ---: |
| Ubuntu JVM |173|580|
| Ubuntu JS Node |86|388|
| Ubuntu Wasm JS Node |86|388|
| Ubuntu Linuxx64 Native |86|388|
| Windows JVM |173|580|
| Windows MinGWx64 |86|388|
| macOS ARM JVM |173|580|
| macOS ARM Native |86|388|
| iOS ARM simulator |86|388|
| macOS Intel JVM |173|580|
| macOSx64 Native |86|388|
| iOSx64 simulator |86|388|
| **Twelve XML instances** |**1,380**|**5,424**|
| BareLinuxARM64 |not XML:86 groups|388|
| **All13 executed instances** |**1,380 XML suites plus86 ARM groups**|**5,812**|

All XML counters and actual testcase elements agree; failures/errors/skips, duplicate/missing/extra keys and ownership/freshness issues are zero. The accepted reviews compare full class/method multisets with source-derived expectations, not only totals. XML normalization removes only exact platform prefixes/suffixes; original names, CDATA and raw XML hashes remain retained. iOS device `compileKotlinIosArm64` completed, but device execution is not an additional instance.

The ARM consumer supplies independent actual runtime coverage: all388 distinct RUN names equal all388 OK names in their original order, all86 group starts/ends match source-derived classes/counts, and no unfinished/failed/skipped/duplicate/missing/extra state remains. Its full set equals both the predeclared common set and the accepted same-run Wasm set. The10,303,243-byte transcript hashes `3daf50eec7e4ac200ede62eb72e6cc1102c1f0696a66482af837c432cfade01e`; these86 groups are not XML suites or cross-compilation inference.

## Exact source and producer-consumer provenance

The [source253 manifest](source253.sha256) hashes `aa90e836005efc363e9575026cd12049b0bb6ac42eb3a716f20d04c596bac1ad`. The accepted exact-head archive hashes `8b8fbaa172d4d91c4524563cda7c5f41e0f9abeae889112b7db73a220053dc09`. All253 source hashes and the complete pathset were checked against that archive, together with25 oracle-source pins,13 harness pins and configuration. No mutable later source tree supplies these outcomes.

The same-run Linux producer JVM artifact contains exactly `krogu-math.jar`, `SOURCE_COMMIT` and `SHA256SUMS`. The full commit plus newline and both embedded checksums verify. Own JAR SHA256 is:

`5e07541044a098601d0cdb05ae1c73eaae063fb8181836b5da3ff6a1a97d72c1`.

It is byte-identical to the accepted local593/source253 JAR recorded in [decimal-power-593-verification.md](decimal-power-593-verification.md). Source attestation checks checkout HEAD against GITHUB_SHA before copying the JAR. Matching bytes establish this exact artifact relationship, not public TEST outcome acceptance or a new execution of the JAR.

The LinuxARM artifact contains only `test.kexe`,79,119,352 bytes, SHA256 `a6b3a26a5b5f2c3e6489d3e9948e5c36a71955ee64a09c76e6fc1bd36538a963`. The consumer has no checkout: its log identifies the exact repository/run and artifact12025679180, and the recorded download digest equals the accepted producer ZIP. Its original command is unfiltered `./test.kexe`, after `chmod +x test.kexe`; no rebuilding or substituted consumer binary is inferred.

| Same-run artifact | ID | ZIP bytes | ZIP SHA256 |
| --- | ---: | ---: | --- |
| Linux test results |12026620186|5460284|`ef1b02bfeb0e72710a139771ba6601d115f5ead38af93ef1a9633129f7d05791`|
| Windows test results |12026830081|3488116|`f569a442169f7ea220bb1a8b5a696313ca55f133677471e1c13d8c64ad9de25b`|
| Mac ARM test results |12026695379|4489113|`cf889f8dc0b66bae094f96ee5f6bd02d6a9983680824b98e3617909c1309338f`|
| Mac Intel test results |12027400566|4515788|`844b58516b92ceb40d0884f951e5674c602f9ae9e8fc095f927b024bf07cd2a4`|
| Own JVM producer |12026525340|174543|`33589078bfdc5f2f4fe08013bee0acc6a47d5d8078d78d5be97fb1c9b4ecdd5c`|
| LinuxARM executable producer |12025679180|9499216|`292587b34641c095f76fa30f4b74f90c784daf9d5c85ae7d765fa097ad4b6f60`|

Each accepted ZIP size/digest matches its individual API and successful upload/download log, exactrun/head and upload interval. Platform result file counts440/264/352/352 include retained non-XML state; only XML contributes the1,380 suites. No old artifact substitutes for a current instance.

## Original commands, tools and completed jobs

Every platform command uses the unchanged suffix `--max-workers=1 "-Dorg.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8" --stacktrace`:

| Completed job | Task arguments after `./gradlew` | UTC interval2026-10-16 |
| --- | --- | --- |
|[Linux116816734740](https://github.com/Heapy/krogu-math/actions/runs/38852537605/job/116816734740)|`jvmJar jvmTest jsNodeTest wasmJsNodeTest linuxX64Test linkDebugTestLinuxArm64`|04:48:52–05:05:23|
|[Windows116816734754](https://github.com/Heapy/krogu-math/actions/runs/38852537605/job/116816734754)|`jvmTest mingwX64Test`|04:48:52–05:04:18|
|[Mac ARM116816734761](https://github.com/Heapy/krogu-math/actions/runs/38852537605/job/116816734761)|`jvmTest macosArm64Test iosSimulatorArm64Test compileKotlinIosArm64`|04:48:57–05:07:47|
|[Mac Intel116816734776](https://github.com/Heapy/krogu-math/actions/runs/38852537605/job/116816734776)|`jvmTest macosX64Test iosX64Test`|04:48:55–05:25:02|

Their primary builds report successful46/11/24/23 executed tasks, respectively, with durations15m45s/14m32s/18m1s/33m57s. Later setup-Gradle noops are retained but do not add test coverage. [Oracle116816734462](https://github.com/Heapy/krogu-math/actions/runs/38852537605/job/116816734462) completes04:48:53–04:56:26. [ARM consumer116826422402](https://github.com/Heapy/krogu-math/actions/runs/38852537605/job/116826422402), `ubuntu-24.04-arm`, completes05:25:07–05:31:07, with the actual unfiltered binary step05:25:10–05:31:05. All individual accepted job APIs are completed/success and all steps are successful or intentional skipped.

Kotlin2.4.21, Gradle9.8.1 and BellSoft Liberica25.0.4+9 (retained runtime metadata25.0.4+9-LTS) remain pinned. Kotlin compilers run in-process; platform build heap4GiB, one worker, UTF-8, JVM fast-throw suppression and JS Mocha120s remain unchanged. Restored Gradle/compiler caches are explicit; no cold-cache, hermetic-build, peak-memory or performance claim is made. Compilation/execution of public TEST profiles, their separate heaps/deadlines and licensing/adaptation losses require their own actual report and are not broadened by these supported jobs.

Configuration hashes are build `b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6`, Gradle properties `0e719146a69861ce1d5e0d33da043abf6fb3c1f81a67b7af617f9f44c249d2cf`, Compatibility workflow `af3813bbf060def341086f4dc4edcacc0c5f9059d39c40462c3888110af5be9b` and separate manual-large workflow `25d97734ca350b2cd4fd70750964be4549ba70b22246ec641954bfb9c0badb61`. Workflow profiles/resources/flags/target commands match the frozen predeclaration; no timeout or resource setting was increased to obtain this report.

## Oracle and diagnostic retention

All25 ordered `kotlinr` commands exactly match the predeclared command file and have successful corresponding steps. The environment retains `JAVA_TOOL_OPTIONS=-XX:-OmitStackTraceInFastThrow`; only the V7 mapper adds both `-J-Xmx512m` and `JAVA_OPTS=-Xmx512m`. Mapper SHA is `3e9552377df5702b483e5e325bb67de42b46280d7aa588d8533fee907f3e0dd7`; compact TSV SHA `d116367be22ace568e46e1721c8aeeaf99442e3f1ab3aa1883c3e2624bd9519b`.

Its actual correspondence check retains75,728 mappings,3,000 controls and11,472 class masks:8 required error statuses,2,048 reference replacement-value prefixes,280 historical-null error prefixes and73,392 unresolved questions. All75,728 remain archival-blocked. Successful verification is not a universal classification of poisoned subclasses, permission to erase observations or proof that a reference-specific trace/message is mandatory. Required mathematical/cohort/error/identity/caller controls stay distinct; no active assertion is retired by this document.

## Frozen reports, predeclaration and limits

Before hosted results, source253 expectations were frozen at `/private/tmp/krogu-source253-hosted-expectations-21EXn2hQ/REPORT.md`, SHA `98461d901f1b3e67e92bce75efab23b5a4fb297b8a8a9c5b3f73af3dc0ebc925`,98-entry index `8c462e3af43db372a8018c0dc7182d76fdebada0d07692e55e8a6369253320c2`. They supply full source-derived method/instance/command sets; older250 outcomes do not supply current passes.

The three accepted actual records are:

| Report workspace / SHA256 | Input index SHA256 / entries | Output index SHA256 / entries |
| --- | --- | --- |
|`/private/tmp/krogu-62bb-supported-stage1-OIgOzUwE/REPORT.md` / `5dac040620f6faf703dfbbe1a3134e743cce1c0b40cb15fe2f23fdf0c94a4c89`|`5186ab7dbe524d3907fc69a3e525dc4cfab3bb2497dda5376a64dc32e59d3607` /1861|`36a311b236c57d2c698ab3ff749c7951888465202572f6cdaed6187de320f628` /159|
|`/private/tmp/krogu-62bb-macintel-addendum-Z43FpoDx/REPORT.md` / `4e675b334454519dc2bdc17518ba9a3df3045de7f6002054c2f421787d2e6954`|`01f8452c118be39375e8ced2ed48058f9f02cbd2e67584e62e6c0c971225d7cd` /721|`251c128c2d69f8a20c36377c135e5c1c21b0d4e73f5c5965c4f16280b2343e4e` /55|
|`/private/tmp/krogu-62bb-arm-consumer-OeogxyUH/REPORT.md` / `fea1b26ca15981bebcafa97dd44625d1c79c248c1e2bcc0b9e9ee5ecf34f657e`|`afcbfb90bc274f26bcf4aa6e455980564d5f943087f01d57620e136a68ea7b70` /15|`04e82654b402c0d654e303da0473d17cad8f82953a5be91756636fbd0fd8a634` /29|

Parent independently checked all these original indices with zero bad hashes. Their original report scopes remain unchanged; this document combines disjoint accepted target instances, not a rerun of the original validators. Full raw logs, source archives, ZIPs, exact XML/ARM names and checks remain in those workspaces.

Earlier metadata mistakes remain recorded: stage1's wrong checksum cwd and native suffix projections, plus ARM's early hash lookup before producer-copy completion. Corrected metadata checks used unchanged acquired bytes; no numerical/workflow retry followed. MacIntel acquisition stderr was empty. These are not relabelled test failures or hidden numerical reruns.

This result does not waive541/548/549 conditional subtype/range/scale/resource obligations,547 actual-randomness premises or525/557 Number/compiler requirements. Source255 local1757/94282 is a later separate composition and is not borrowed into62bb hosted evidence. Symbolic giant witnesses remain unexecuted; finite passes do not prove every supported value can finish within a fixed heap or deadline. Normal-public and manual-large acceptance retain their own separate artifact/protocol scope; no118public union, all-input theorem, performance claim or officialJCK follows here.

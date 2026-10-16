# Source240 normal CI: six supported/oracle jobs

Qualified ACCEPT of the six supported/oracle jobs in
[run38821252611, attempt1](https://github.com/Heapy/krogu-math/actions/runs/38821252611),
exact head `2e767ad6f4ac34a09a0056ef19533067ab02e747`. All six are completed
SUCCESS in the preserved API snapshot. Independently inspected actual XML and
ARM console inventories establish **5,375 passing method executions across13
target instances**, with no observed omission, extra, duplicate or nonpass.

The overall run and public job116730047114 are still in_progress/null in this
snapshot. Public evidence and any later outcome are excluded. This report
does not combine the normal and large public campaigns or establish official
JCK/TCK, full supported-range correctness, practical capacity or performance.

## Actual executed sets

|Terminal job|Executed targets / evidence|Actual suites / methods|
|---|---|---|
|116718087122 oracle|25 original kotlinr generator/check/classification commands|Raw command/output and successful API steps; no platform XML|
|116718086783 Ubuntu24.04 x64|jvmTest;jsNodeTest;wasmJsNodeTest;linuxX64Test|163/545;78/355;78/355;78/355|
|116718087109 Windows2025|jvmTest;mingwX64Test|163/545;78/355|
|116718087201 macOS15 Intel|jvmTest;macosX64Test;iosX64Test|163/545;78/355;78/355|
|116718087209 macOS15 ARM|jvmTest;macosArm64Test;iosSimulatorArm64Test|163/545;78/355;78/355|
|116730047161 Ubuntu24.04 ARM|Actual same-run downloaded `./test.kexe`|78/355, native RUN/OK inventory|

The four artifacts retain exactly1,276 original XML files. Their12 target
instances contain5,020 testcase executions: four JVM sets of545 and eight
common-only sets of355. Every testsuite's declared tests/failures/errors/skips
agrees with its actual testcase/child counters; all failure/error/skipped
counts are zero. Every complete sorted method multiset equals the independently
extracted exact-source list. Zero duplicate methods, extra methods or omissions
remain in any set. All testcases within each suite have one classname, and
normalized suite names match their owning class. JVM simple-name `[jvm]`
suite labels and platform display/prefix suffixes are normalized explicitly,
without dropping any method or duplicate.

The archived sources independently declare78 common test classes with355
methods. JVM adds85 classes/190 methods:129 Kotlin and61 Java. The resulting
JVM source set has163 classes/545 methods. All545 Test annotation markers,
matched method declarations and163 corresponding class declarations are
retained. This accounts for declaration inventory, not execution of an oracle
or a proof of all-domain mathematics.

ARM separately contains355 unique RUN names and355 matching OK names. Both
full lists are byte-identical to the355-method source inventory and the
same-run Wasm XML list. Its raw footer records355 tests from78 cases,
326,929ms total, all355 passed. The original artifact download identifies
run38821252611, artifact12012958888 and the exact checked ZIP digest before
`./test.kexe`. The independently downloaded file is an ELF aarch64 executable;
no binary or numerical program was executed by this audit. There is no ARM XML
artifact, so its gate is explicitly console-based.

All XML timestamps lie inside the respective four platform job intervals.
`xml-freshness.tsv` retains complete per-target earliest/latest values and
`job-intervals.tsv` retains the API intervals. Original raw Gradle lines show
actual target tasks, with no FROM-CACHE or UP-TO-DATE test task. Only setup-gradle
post-action noop cleanup tasks are UP-TO-DATE. The actual ARM interval is
2026-10-15T23:40:00Z–23:45:32Z. iOS ARM device `compileKotlinIosArm64` completed,
but this is device compilation only, not an iOS-device runtime gate. The
Ubuntu `linkDebugTestLinuxArm64` task produces the binary; the separate ARM job
supplies its actual runtime evidence.

## Commands, oracle and toolchain

The25 original oracle commands compare byte-identically to the source workflow
command list. All six jobs' API steps are terminal with success or documented
conditional skip conclusions; no failed/cancelled/incomplete step exists.
Original numerical fixture checks remain distinguished from metadata/static
checks. For example, the526 mapper reports75,728 retained rows, all archival
blocks,8 required ERROR bindings,96 reference VALUE bindings,280 historical
reference ERROR bindings and75,344 unresolved statuses, explicitly without
evaluating production or JDK arithmetic. It is not counted as another platform
test or as whole526 acceptance. All original output remains in the oracle log.

The exact workflow SHA256 is
`af3813bbf060def341086f4dc4edcacc0c5f9059d39c40462c3888110af5be9b`;
build SHA256 is `b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6`.
Wrapper properties SHA256
`db7a6b948fb1e4fd362a4bd6ee3ef9cdb90967be4c403e45d75891f052d00965`
selects Gradle9.8.1 with its pinned distribution hash. Source/build selects
Kotlin2.4.21 and JVM toolchain25. Setup logs identify Liberica25.0.4+9/BellSoft,
and Native bundle paths identify2.4.21. Oracle setup verifies its Kotlin
compiler2.4.21. These unchanged protocol files match the previously audited
f58 protocol; its outcomes/counts are not substituted for this run.

Original four matrix commands retain their exact target lists,
`--max-workers=1`, `-Dorg.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8` and
`--stacktrace`. Source JS Mocha timeout remains120s and JVM tests retain
`-XX:-OmitStackTraceInFastThrow`. This audit changed no heap, timeout, precision,
input, expected assertion or fixture. No explicit test-Node executable version
is inferred from action-runtime warnings. Original setup/deprecation,
Kotlin override-name and Native bundle-overwrite warnings remain in the raw
logs; they are not silently reclassified test failures.

## Exact source and binary provenance

The source archive `source.tar.gz` was copied byte-for-byte from the accepted
same-head large evidence `/private/tmp/krogu-2e767-large-ci-GilTXdK1`. Its SHA256
is `8b5664df4efbeefe453c87384d1a7b356dca6843e80c2f22456dba04bb13e747`.
The240-source manifest is
`e3539c0bda309350d72ebb89656a9f54e3c7f720fe943616153050e2be493aa9`.
All240 source hashes pass against this archive and the full Kotlin/Java source
pathset compares identically. Five raw checkout logs identify the exact head;
ARM instead binds the same-run producer binary through its artifact download.
No current checkout or earlier source236 inventory is substituted.

Ubuntu producer artifact12012859049 contains SOURCE_COMMIT exactly equal to
2e767ad6f4ac34a09a0056ef19533067ab02e747, and both SHA256SUMS entries pass.
Its own JAR is
`1cacc5b41ede2e154e9c7baf948bd1748f4d424932f3fb7e4a592085b1ed00e1`,
byte-identical to the accepted same-head large producer and locally validated
585/586 JAR. The archive has57 entries, all confined to META-INF/io paths.
This is metadata/hash checking, not decompilation or a new class-origin probe.
Other OS jobs do not upload independent own JARs or runtime origin dumps;
their binding rests on exact checkout, pinned commands and fresh complete XML.
No unavailable per-OS JAR hash/origin evidence is invented.

The same-run ARM native artifact contains only test.kexe, SHA256
`70588bb2d48a1d8b64b194f84eefe2502040828226df474a51b83fa2f12e2a87`.
The ARM raw log's artifact ID and verified ZIP digest match both the API and
the independently downloaded original ZIP here.

All six original ZIPs match their API digests:

|Artifact|ID|ZIP SHA256|
|---|---|---|
|test-results-macos-15-intel|12014262697|`d8a89b982bee0ec7e7be66028bebbccbf129e276dd0191e3a679b7efb511cc1d`|
|test-results-windows-2025|12013882496|`5ae6b31273aa4edde9be5270b056e3c9cbdbec07df01420d4ca19ad05b30242a`|
|test-results-macos-15|12013518959|`23d27a7b98ecf030dda324d2291093dd67fa3480db4fdb069cee70224f8c4cdc`|
|linux-arm64-tests|12012958888|`0a0178bce158dc20c59ff309f60e46a26e547fbb1b894b8e2321396127117452`|
|public-math-jvm-2e767…|12012859049|`84773c4a72093dfcb10925043ac9fc319676cb4b5bb214514d47ae10a9728b52`|
|test-results-ubuntu-24.04|12012764465|`17bc1cb956a41a9c61177d4d1dfd68ee9af208fc98d25eb0532c53de64150944`|

## Retained evidence and scope

Evidence root: `/private/tmp/krogu-2e767-supported-actual-a297q1KT`.

|Evidence|SHA256|
|---|---|
|run.json|`dd8b1c3d1a69900560899d98491cf5917861f887e093b52395a3e936ad09ca03`|
|jobs.json|`1563e034c17cb15138d7efbffa999a261cb5e74ef4032a1decb211b4683ed93b`|
|artifacts.json|`67a4267c9ae6c0948e4ab0a3f7ab3336fb959cc50287f35df9a9b101c8696ac9`|
|XML summary|`156bc9f17cb96ca357690655acde90d0dc619a1f61797aba21a9ad6683dbc8e3`|
|Final method-set checks|`5a0d1e57d09b194c000e7aeadadc29b77e6a6bb99b9a7f02c5b39f051bf46404`|
|Common source/RUN/OK methods|`ea73ff2ca45b2192b65ed5d1cb99d9e986fa3590a62a3c4899c393fe2baedbb0`|
|JVM source methods|`7b90032ff3ce1c6e06f41f8c1f7823b3364ee7547694bb1f7d80a834d81afd92`|
|ARM raw log|`079c81ed35997d87c463b9bddea591176c851089a440bac3c98a7a2c25001f12`|

`xml-audit/` retains merged original XML, xmllint attributes/counters and the
first malformed normalization outputs; `xml-final/` retains the corrected full
method/suite/owner lists and final comparisons. No malformed derived output is
used for acceptance. `ACQUISITION.md` records all acquisition and metadata
corrections, including initial escape-sequence refusals, a checksum attempted
before the sole ARM download finished, wrong relative extraction paths and
incorrect first paste pairing. Original raw artifacts, initial records and
final records remain distinct. No correction changed source, test outcomes,
fixtures, expected assertions or any hosted execution.

The earlier incomplete snapshot at
`/private/tmp/krogu-2e767-supported-ci-ZFiZGwj3` remains unchanged. Its report
and index are copied here only as historical provenance; its then-running ARM
state is not rewritten. No public job was awaited or downloaded. Parent's
later independent review/publication is separate from this finite audit.

No script/compiler/numerical/helper/model/certificate/probe/test/build/process
inspection, workflow dispatch/retry, Git/tracker or repository mutation was
performed. Direct CLI JSON/XML/archive/text/hash tools checked metadata only;
no foreign numerical implementation source or bytecode was read. All owned
downloads and metadata calls are terminal, and no runtime mutex was acquired
or reserved. The final index covers every retained file except itself and its
path-list/check outputs.

These are finite supported-platform/oracle integration gates for the exact
581/582/583/584/585/586 source240 composition under its recorded configurations.
The subsequent587 cohort policy/repair and588 contextual lexical domain are
not represented as repaired by these results. Public conformance, giant
symbolic witnesses, practical maximum allocation, RNG independence, broad
541/526 subtype policy, full542/548 mathematics and whole-project closure
remain separate.

## Parent adoption

Root independently verified all2,234 indexed files, index SHA256
`2aa712e442dde9f9f1eafbf93f899c4799cd509d72da7ae85d5b342696acb406`.
The original REPORT.md prefix has SHA256
`8b392661c98dd2b416f72a590768822b1ab3c4576ea6944836d635dd700810c3`.
All12 hosted method multisets also compare identically to the independently
lexed source240 inventories of the accepted local585/586 XML auditor. Source,
ARM RUN/OK and same-run Wasm lists match, as do all25 oracle commands. These
additional metadata comparisons do not rerun numerical code or substitute
local execution for hosted results. Initial acquisition/metadata mistakes
remain preserved in ACQUISITION.md and the indexed original/final outputs.

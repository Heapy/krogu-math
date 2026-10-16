# Source247: published hosted verification

Published commit `47447f2111d44735de374ec98d0a6a099a08888f` passed the complete
[Compatibility run38837430311](https://github.com/Heapy/krogu-math/actions/runs/38837430311)
and the separate manual [large-profile run38837441144](https://github.com/Heapy/krogu-math/actions/runs/38837441144).
Both are attempt 1: all seven Compatibility jobs and both manual-workflow jobs
are terminal SUCCESS. Actual evidence records **5,626 supported-platform method
executions across 13 target instances**, 25 successful oracle workflow commands,
and separately **106 normal + 12 large public PASS IDs**. The two public PASS
sets are disjoint and their union equals all 118 retained primary IDs.

This is a source-bound verification snapshot, not official JCK, a full-domain
proof or a performance result. Both public ledgers explicitly say
`officialJck=false`. Private source248 and its subsequent work are excluded.

## Source, output and protocol binding

The published [247-source manifest](source247.sha256) has SHA256
`529cc142d9a42110084155987b3022b36c2309168a159b745bf5191a0359c5a4`.
All 247 paths/hashes verify in the API source archive, SHA256
`3fbf81ef92342b25affc56e1c4071d868f1c6b607b81685e6318ef85b5144e82`.
Both public producer/consumer pairs bind the full commit through SOURCE_COMMIT
and verified SHA256SUMS. The measured own JAR is byte-identical across both
hosted pairs and the accepted local source247 output:
`6e6bc44856543cd79ad3d182d858a31c674e5154873991076ac1732c3c39a421`.
Gradle tests use their normal main/test class outputs; this JAR equality does
not assert that those tests loaded the packaged JAR.

The original Gradle 9.8.1/Kotlin 2.4.21/Liberica 25.0.4+9-LTS configuration,
workflow action pins, filters, resource limits and test deadlines are retained.
Build SHA is `b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6`.
Compatibility workflow SHA is
`af3813bbf060def341086f4dc4edcacc0c5f9059d39c40462c3888110af5be9b`;
manual workflow SHA is
`25d97734ca350b2cd4fd70750964be4549ba70b22246ec641954bfb9c0badb61`.
Cache restoration is recorded; no empty-cache or hermetic-build claim follows.

The 13 owned public-harness tools match the published source and artifacts.
Entrypoint SHA is `8383d062feac7a3b2e8eda8f9e2844f7d2dbe6ac8d4e5c575f3fc92f58ad1265`;
runner SHA is `ed32beb00f4c6896cfd783f2286f95336fee3cc2616420b9a83687c4c5f32ea0`.
The restored adapter and actual `manifest.tsv` pins are respectively
`4ac1b789cd3df236bac8503383ddcbb4764399154b250f73e68227ddb12a1da7` and
`c3fd7136baa405654c4f1f0b5bdee482117c1d57810697174ea5648318e3a2c4`.
Public TEST tag `jdk-25.0.4.1-ga`, upstream commit
`7b65d74bcbd7c3a0b4c1f5c5bd85e64aa2e51b4a`, is locked separately from the
runtime. That declared revision/vendor mismatch is not erased by passing tests.
Source/dependency lock SHAs are respectively
`03493e7b75110b9a709e126e5547bbe606b6a27f0fc2f0e9ac8747188af52e64`
and `85bae4a80af6f3e3b866c903a4d2ea2914f90d5f2b10486ea271e117430003fb`.

## Supported platforms and oracle checks

Source-derived full method inventories were retained before hosted XML was
read. All normalized method keysets match exactly, with no missing, extra,
duplicate, failed, skipped or unfinished methods. The 1,328 raw XML files
contain 5,252 method executions. The separate Linux ARM64 transcript contributes
374 more, not another XML result set.

| Completed job | Executed target instances | XML suites | Methods |
|---|---|---:|---:|
| Linux 116769576287 | JVM, JS, Wasm, Linux x64 |414|1,687|
| Windows 116769576316 | JVM, MinGW x64 |250|939|
| macOS ARM 116769576002 | JVM, macOS ARM64, iOS simulator ARM64 |332|1,313|
| macOS Intel 116769576297 | JVM, macOS x64, iOS simulator x64 |332|1,313|
| Linux ARM 116777498146 | Linux ARM64, bare execution |0|374|
| **Total** | **13** | **1,328** | **5,626** |

Each JVM instance has 168 suites/565 methods; each non-JVM instance has 82
suite/class groups/374 methods. ARM logs preserve 374 RUN/OK names, source and
same-run Wasm keyset equality, all group counters and the unfiltered
`./test.kexe` command. Its same-run producer artifact binds the downloaded ARM
binary SHA `d74164870d190f40624383e5223dc075d67819dd864687ffb21739bb4fb83480`.
iOS-device coverage is successful compilation only, not device execution.

Oracle job 116769576341 completed all 25 exact ordered workflow commands;
command/env/source pins and raw output were checked. The existing fast-throw
flag and the mapper's 512MiB settings were retained. These are successful
regeneration/check commands, not 25 extra methods or an unconditional proof
that every historical JDK observation is normative.

## Separate public normal and large results

Normal public job 116777498138 reports **106 PASS / 0 FAIL / 46 SKIP**, complete=true,
in 152 unique semantic records: 53 retained plans pass in each phase. The large
consumer 116769911377 uses producer 116769611532's bound JAR and reports
**12 PASS / 0 FAIL / 128 SKIP**, complete=true, in 140 unique semantic records:
six other plans pass per phase.
Both ledgers contain the same 118 primary baseline/port IDs for 59 retained
plans; exact PASS-set comparison proves no overlap and no omitted primary ID
in their union. These are separate executions, not a combined single ledger.

All 118 selected compilations/executions exit 0 without timeout and have exact
completion markers. All 59 port core-loading checks bind the own JAR; caller
audits, core origins/class-load entries and the two ModPowPowersof2 nested
child completion protocols pass. Normal evidence preserves 666 raw paths,
106 markers, 63 caller audits and 212 primary core origins. Large evidence
preserves 74 raw paths, 12 markers and six caller audits. The two declared host
integration bridges remain restricted to their documented adapters; no host
numerical fallback or blanket original-baseline claim is inferred. In
particular, the nested host-baseline orchestration is adapted.

Normal profiles retain 512MiB heaps and original 120s deadlines, except the two
BigIntegerTest run-2 invocations retain 400s. Large profiles retain their
original 4/8/10GiB physical-memory predicates, 4096/8192MiB heaps and 120s/180s
deadlines, seed 559 and both subsets 0. Every selected large invocation passed
its fresh heap+512MiB free-memory gate: two 4608MiB and ten 8704MiB gates. Actual
large-host total was 16,766,414,848 bytes and per-invocation free memory was
11,694,419,968–11,852,976,128 bytes. There are no selected capacity omissions;
this is an observed gate result, not a general resource guarantee.

The normal 46 SKIPs are 12 unselected large primary records, 12 adaptation
companions, 16 disabled methods, two commented calls and four private-internal
exclusions. The large 128 SKIPs are 106 unselected normal primary records plus
the same 16+2+4 original exclusions; companions of unselected parents produce
no separate large SKIP rows. The remaining **12 adaptation losses** are four
joint-invalid char/null precedence cases, five Float16 integration methods,
one storage-invariant replacement, one RSA-provider phase and one prime-cutoff
expectation. Retained PASS IDs do not recover these omitted assertions.

The original SquareRootTests `main` call to `scaleOverflowTest()` is restored
and actually compiled/called/completed in both normal phases. Reversing only
the port imports recovers the original source byte-for-byte; the caller audit
contains that invocation. The unchanged input 10, precision 1,073,741,825, UP and
expected ArithmeticException assertion run under the original 120s/512MiB
limits. Both phases exit 0 and emit their completion markers; no sqrt companion
remains. This is the specific restored public assertion, not execution of the
separate giant symbolic witnesses or proof of the whole sqrt domain.

Upstream/prepared/compiled source pins, dependency lock/POM pins, notices and
ZIP pathsets verify. GPL public TEST source/text/notices remain quarantined
and packaged with provenance; foreign compiled TEST/framework binaries are
excluded, and the own Apache-licensed input JAR is separate. The normal/large
API artifact ZIP digests below match measured bytes and upload/download logs.

| Artifact | ID | ZIP SHA256 |
|---|---:|---|
| Windows results |12020331981|b66df41fb3242338ea03f34dac2e30c541a5ed8d7c034537be06ec6569b28b43|
| macOS ARM results |12020192818|5af1282bca976cc74e55167a682609ce4139605cb3db47d36065c71b5c461215|
| Linux results |12021081482|74c1b05750f77c9fc34ecc03c913130d773251e9f282e6ff3f9027cf47747a24|
| macOS Intel results |12020193997|3561e77925312e10eb6eb573e234802e04c52c9318ca9edf9d3a9465ee04b097|
| Normal own JVM input |12020169764|c6df196363ee218c7852fc7b21c21b35c88d1a6271cfaa66df44ee79e885a36d|
| Linux ARM input |12020961533|0949bbc9e64c4ad490086c076bee53c6ffbd9d1a120adb5910af96b05705f729|
| Normal public evidence |12021266735|993203e55258bbdc2eee059a1374b1ece81d161d18a0c747809417d64009802b|
| Large own JVM input |12019624296|cdad2bdc5789f32a36bb4c4943013ee3931dbf2de5cffed2c3b77ebd335460c4|
| Large public evidence |12019448492|7a830372aacbdfdfe452bcc47a2c011b957a90cad023ff8cc651da500bacda0b|

## Reviewed evidence and remaining scope

The independent reports and complete checked indices are retained at:

| Scope | Private review directory | REPORT SHA256 | Index SHA256 / entries |
|---|---|---|---|
| Supported/oracle |`/private/tmp/krogu-47447-supported-review-USqzgezo`|2b96b7f2fada082b0b95f8754a5f93396add29d01f49c59579c88a169e499e11|280a0d20e4fff916d41c93cbf1c4594bf418c842c0cd3cf61e2d60a4d3006ade /1,540|
| Normal public |`/private/tmp/krogu-47447-normal-public-review-4DEHAdXo`|7008aa41083398f627087186c1028dbb1a723e030d6c376374a8d85c33c3f1a2|8f7a15cb7c43b0e13502a5b0b9bd81782703ed267dc64b7e79e10ea0bfb6d3f9 /1,596|
| Large public |`/private/tmp/krogu-47447-large-producer-review-MWllLHi2`|a1dd45937491ec6cb4b7c1607aec1c6458099617d814c6ebdf0a0a08c560d7bd|01fb927522e9edc458739f02427278e4fb32ece336893785ef3711221109a144 /346|

Normal results 1792117281312 SHA is
`b9133cf6eba030f252c1246a2047aa5e1cfd13baf94e8b7ff3d928685c48cf30`;
large results 1792115654273 SHA is
`b369806ec69685b5bdac119e61ac2a339ca268c34d1f6f9aad765905bcfc86b1`.
The reviews preserve initial acquisition/query errors as metadata history;
they did not rerun compilers, tests, arithmetic, helpers or workflows. Earlier
failed campaigns and their raw observations remain failed historical evidence.

This hosted snapshot supplies the matching-head gates requested by 587/588/589
and the 551 platform snapshot. The [local integration and ledger report](decimal-text-sqrt-587-589-verification.md)
retains the separate 1,687-method local campaign and 94,282-record sqrt audit,
including its narrower independent-certificate scope and earlier failures.
Task review/state changes remain parent-owned; this document does not close
559, 549, 548, 541, 526 or the overall mathematical API goal. Giant lexical/type/
range witnesses remain symbolic; no giant allocation success is claimed.
The 525/557 Kotlin/JS Number/generic caller defect remains open and unwaived.
No legacy/cross-format serialization, official JCK access, all-input equivalence
or performance certification is supplied by these retained finite tests.

# Published source255: retained public math tests

SPDX-License-Identifier: Apache-2.0

Exact commit `12d0fe287cdec99567bd7cbf981b65aa86ef834b` has accepted evidence
for all **118 predeclared retained primary invocation IDs**, comprising59 original
plans executed in baseline and port phases. This is the disjoint union of two
separate completed campaigns:

| Campaign | Run / attempt | Primary PASS | FAIL | SKIP | Complete semantic records |
| --- | --- | ---: | ---: | ---: | ---: |
| Normal | [38858645066](https://github.com/Heapy/krogu-math/actions/runs/38858645066) /1 |106|0|46|152|
| Manual large | [38858676270](https://github.com/Heapy/krogu-math/actions/runs/38858676270) /1 |12|0|128|140|

The exact selected ID sets have an empty intersection; their sorted union equals
the frozen118-ID set. These are invocation IDs, not118 Java files or methods.
The two SKIP totals must not be added as unique coverage. These are retained,
explicitly adapted public tests, **not official JCK/TCK certification**.
Supported-target and oracle execution has its separate scope in
[source255-hosted-verification.md](source255-hosted-verification.md).

## Source and completed jobs

Both campaigns bind the exact [source255 manifest](source255.sha256), SHA256
`8427be10a953974f1b74348dec87ea0cbbbb6f93c6e806875b049e4ff2fb339d`.
All255 source hashes and the full pathset were verified against the exact12d
archive, SHA `d11d6b25f53032e7ee852c2e92e980a7f6ccff3017e7eb13cae4ea20c6d8b2c1`.
The normal producer/consumer, manual-large producer/consumer and accepted local
source255 JAR independently have identical bytes and SHA256:

`bb15a352df94be410ce79c310af6f541ee4206c59148dc30f81e9dc79270104b`.

| Role | Completed job | UTC interval on2026-10-16 | Outcome |
| --- | --- | --- | --- |
| Normal Linux producer |116835691343|05:59:06–06:27:12|SUCCESS|
| Normal consumer |[116843895860](https://github.com/Heapy/krogu-math/actions/runs/38858645066/job/116843895860)|06:27:14–06:33:36|SUCCESS|
| Manual-large producer |[116835789798](https://github.com/Heapy/krogu-math/actions/runs/38858676270/job/116835789798)|05:59:29–06:00:26|SUCCESS|
| Manual-large consumer |[116836067307](https://github.com/Heapy/krogu-math/actions/runs/38858676270/job/116836067307)|06:00:29–06:03:15|SUCCESS|

Normal's actual campaign step is06:27:29–06:33:30UTC; large's is06:00:46–06:03:10.
Each consumer downloads its own same-run/attempt producer artifact; full source
markers, checksum records, API identities and original upload/download log
digests agree. No current result is borrowed from source253/250. Other jobs'
API SUCCESS statuses are not substituted for their separate actual audits here.

## Artifact and protocol identities

The downloaded ZIP hashes equal GitHub API and original upload/download digests.

| Artifact | ID / bytes | ZIP SHA256 |
| --- | --- | --- |
| `public-math-jvm-12d0fe287cdec99567bd7cbf981b65aa86ef834b` |12028119866 /174634|`73d599551e72867faa851ea86a451bcfb33f7a150e3315ca1d0400c144da49ec`|
| `public-math-evidence-12d0fe287cdec99567bd7cbf981b65aa86ef834b-1` |12030030068 /3729907|`fcb9328365a19568a00be80a918809b2f58883171ec3b97e178c84d31939daf5`|
| `public-math-large-jvm-12d0fe287cdec99567bd7cbf981b65aa86ef834b-1` |12028800374 /174634|`f4c24e2506da806ac2c68eddaa45a8544d0e610de28b990bb3fce9d3c04d20b6`|
| `public-math-large-evidence-12d0fe287cdec99567bd7cbf981b65aa86ef834b-1` |12028855474 /1196492|`2bf5bc2e9d851e72af8bcf0478ea7545d6237dacdea73f39ef988ac6fe82848c`|

Normal results `build/math-tck/results/1792132066643/results.json` hash
`feb313206ca3708f19c5abc82d4b20ef9d35c5abfa661f9fd882040cf2929681`;
its inventory hashes `86aec0593ca85dd623b8bb0506915f6fe3b9353f2516a2faa9ebe4a42415a2be`.
Large results `build/math-tck/results/1792130467409/results.json` hash
`4f12fe731d0e901e00059ba8c640baa54915ea2f37231d8cf8688bc7eec8ec02`;
its inventory hashes `b9c97a532b13aff45047dd7193437bcc6d62a4ef7f7d9bfb542ec697effef33a`.
Normal/large consumer full raw logs hash respectively
`560cdcda752509468cb787d212f42e90cf97a8431d3cbb42278827faefca8117` and
`0f64565a8839516096e35e294eefdaa7e724c6150fea8b5fc4e29a939b8e2f99`.

The before-actual declaration is REPORT
`5f42341e8777c6dc44591e17a89c9306f75cdab92c51427e3de2855a6c665029`,
index `3f8f9f37c430cbbad2fa0626199948d98a839813367b867d24f86da807c08e6f`.
Its original UNBOUND publication labels are preserved; later actual bindings are
separate evidence. Every actual59-plan field matches those frozen directives,
arguments, VM flags, heap/resource, requires/modules and timeout. All semantic
`(scope,phase,path,id,method)` keys are complete and unique in each ledger.

The unchanged build hash is `b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6`;
Compatibility/manual-large workflow hashes are
`af3813bbf060def341086f4dc4edcacc0c5f9059d39c40462c3888110af5be9b` /
`25d97734ca350b2cd4fd70750964be4549ba70b22246ec641954bfb9c0badb61`.
Both campaigns retain Kotlin2.4.21, Gradle9.8.1, BellSoft Liberica25.0.4+9-LTS,
Linuxamd64 and seed559. TEST remains pinned to jdk-25.0.4.1-ga,
`7b65d74bcbd7c3a0b4c1f5c5bd85e64aa2e51b4a`; vendor/revision mismatch is explicit.
The original host RSA provider uses unseeded SecureRandom; deterministic RSA
integration and a clean-cache/hermetic/performance premise are not asserted.

## Original resources and complete execution

Normal selects45 standard,two serialization,five heavy and one512MiB plan in
both phases. All106 compiles/executions exit0 without timeout or caught-OOM
incompleteness. Compile retains256MiB/120s; execution retains assertions,
fast-throw suppression,512MiB and seed559. All four BigIntegerTest commands include
subset0. Original execution deadlines remain120s except BigIntegerTest run2's400s;
normal job/step limits remain360/330 minutes.

Manual-large selects all six original large plans in both phases, without a
selected-capacity SKIP:

| Original plan | Heap MiB | Deadline seconds | Baseline / port execution ms |
| --- | ---: | ---: | ---: |
| BitLengthOverflow |8192|120|116 /198|
| DoubleValueOverflow |8192|120|90 /114|
| LargeValueExceptions |4096|120|28492 /790|
| largeMemory/DivisionOverflow |8192|120|508 /5406|
| largeMemory/StringConstructorOverflow |8192|120|3717 /3871|
| largeMemory/SymmetricRangeTests |8192|180|26220 /38046|

All12 fresh gates meet heap+512MiB margins: two4608MiB and ten8704MiB requirements.
Original64-bit/physical-memory predicates and non-heap flags remain. The10g profile
retains8192MiB heap; it is not a10GiB-heap substitution. SymmetricRangeTests retains
subset0. Producer job/build45/30min and large consumer job/step180/150min remain.
All selected compiles/executions exit0/no timeout/incomplete-OOM. Captured timings
establish finite completion, not a benchmark or universal allocation/capacity bound.

All118 selected raw stdout files contain their exact completion marker once.
Each of the59 port invocations has the four distinct core classes loaded from
the verified own JAR in stdout and separate class-load logs:212 normal plus24
large records in each channel. Baseline classpaths exclude the own JAR. All63
normal and six large TEST caller audits exit0/no timeout, with zero direct host-math
member owners. Normal retains two explicit integration bridge references,
`HostBridge.decimal` and `HostBridge.rsaExponent`; large retains none. Only captured
TEST callers were inspected, not foreign numerical implementation bodies.

Normal's ModPowPowersof2 parents record observed child IDs4353/8115. Both complete
raw property maps equal their recorded result maps and prove PASS/exit0/no timeout,
exact `success` stdout and completion (47/99ms). These are captured child identities,
not assertions about live processes. Original assertions/seed/512MiB remain and
the port child independently loads all four own core classes. The parent scaffold
is explicitly adapted in both phases; its nested numerical TEST body is retained.

Normal's two Constructor TestNG runs each retain five passes/zero failures/skips;
the retained JUnit suite headers show zero failures/errors/skips. Both large
LargeValueExceptions TestNG runs enumerate the same five enabled methods, with
ten PASS markers total. Framework cases and internal main assertions are not
confused with primary invocation counts.

## Restored SquareRoot call and twelve losses

SquareRootTests baseline/port PASS at182/334ms under original120s/512MiB. Original
source SHA is `1d4abb1ea98e14a3cd28defd54d16f2ee49563e3862e86cad11399f5591274aa`;
port compiled source is `afb93448ad8b9224e7b6674226ce894a8a7ad746d3c64a55917047723c7387b3`.
The complete transformation is imports only. The original unconditional
`failures += scaleOverflowTest();`, value10/precision1073741825/UP and expected-AE
assertion remain; the captured compiled TEST main retains its invokestatic and
reaches its final completion marker. This establishes the restored selected
call's actual execution, without separate giant-case timing or a universal public
sqrt cutoff claim. Large-only does not select this standard-profile TEST.

The twelve unchanged adaptation losses are explicit:

| Count | Lost original coverage |
| ---: | --- |
|4|Invalid char-array range combined with null MathContext: only null becomes UNLIMITED; range operands/error assertions remain, but joint-invalid precedence is lost.|
|5|Incubator Float16 host-BigDecimal integrations; ordinary Float/Double cases remain.|
|1|JDK private `mag` storage invariant replaced by the own normalized-word invariant; public racy-constructor execution remains.|
|1|SunRsaSign generation/signing phase; all three modular-power known answers remain.|
|1|Mandatory prime-cutoff expectation beyond the public minimum guaranteed domain; PASS alone does not identify its adapted Boolean/AEx branch.|

All twelve exact IDs/reasons equal predeclaration. Normal records12 companion
SKIPs; large's parent plans are unselected, so companions are not extra emitted
large records. Baseline originals and the explicit nested-child orchestration
adaptation remain. There is no SquareRoot omission companion. Historical
null/precedence/PrimeTest failures and original sqrt failures/marked timeout stay
historical failures at their original source/run.

Normal's46 SKIPs are12 unselected large primaries,12 companions,16 disabled methods,
two commented calls and four private exclusions. Large's128 are106 unselected
normal primaries and the same22 disabled/commented/private records. Full reason
and lostCoverage records remain; no selected failed capacity/assertion is a PASS.

## Harness and licensing boundary

All57 upstream/support hashes,53 generated hashes and selected compiled-source
hashes were checked. Baseline ModPow's known parent orchestration transformation
is identified rather than called byte-original. All13 owned tool hashes and the
complete captured tools tree/entrypoint/contract match exact12d. Entrypoint is
`8383d062feac7a3b2e8eda8f9e2844f7d2dbe6ac8d4e5c575f3fc92f58ad1265`,
adapter `4ac1b789cd3df236bac8503383ddcbb4764399154b250f73e68227ddb12a1da7`,
manifest `c3fd7136baa405654c4f1f0b5bdee482117c1d57810697174ea5648318e3a2c4`,
source lock `03493e7b75110b9a709e126e5547bbe606b6a27f0fc2f0e9ac8747188af52e64`,
dependency lock `85bae4a80af6f3e3b866c903a4d2ea2914f90d5f2b10486ea271e117430003fb`.

Original57 and generated53 headers preserve GPL-2.0-only; originals have no
Classpath Exception. GPL text hash is
`edaef632cbb643e4e7a221717a6c441a4c1a7c918e6e4d56debc3d8739b233f6`.
The source-specific adapter/TEST derivatives remain separate from independently
authored Apache2 production/orchestration. All14 dependency lock rows and seven
retained POM hashes match; notice indices/texts remain. Absent dependency JARs'
recorded hashes are not described as fresh checks of absent bytes. The separate
own input JAR has no TEST/framework leakage; evidence packages contain source,
notices, provenance and raw caller/log records, not compiled TEST/dependency
binaries. This preserves the [licensing boundary](../tools/math-tck/LICENSING.md).

## Accepted evidence and limits

Normal audit: `/private/tmp/krogu-source255-normal-actual-dHf3UcxP/REPORT.md`, SHA
`c825fdd153d21cdb295e850582cad60dc17a9adb19e89ee0c925559fdbc68c91`;
1604-input index `c26952ddb4e3dcce04a91da696e248c979e2e0d1b7b16db37c75fc2976b77b29`,
152-output index `0998c5d45831717b7c257a2223eb939685774a1d4555b6a2ddd7fe8eb81164e8`,
1740-entry final index `af993eae145d9b0246387080b3425410ba799ed39ce901a42f2e74c44a4ca52d`.

Large audit: `/private/tmp/krogu-12d-large-actual-UTm8a3Tc/REPORT.md`, SHA
`81cbe76324382f2e2867d4192a0752d1e6599b3c6bd80222827b5ff52e363a2a`;
input/output indices `24998e073cc7648092e3257308f2e022731c60bc323f87f09242df0016bd6e65` /
`fb95a20f02f2f24bfa7e930f11b780c3f2f80b64ab6cab09da2c372ea4cd1cd4`.
Separate exact-head producer audit is `9fd3506066605c30eda0d49af00ea734fd6a105541ee22c599bdeda4eb9a4b1d`.
Root accepted these frozen reports/indices; acquisition/reader corrections and
warnings remain with them. This document reuses their observations without any
new acquisition, numerical execution or expanded acceptance.

These finite adapted campaigns do not establish official JCK/TCK compliance,
every supported input/subtype/range or performance/termination, full526/541/542/
548/549/553, old/cross-format serialization compatibility or the giant594 witnesses.
The Kotlin/JS erased Number525/557 requirement remains UNSAT. Source253 is
historical; later private source260 and new595/596 candidates are outside source255
and are not accepted by these old outcomes. All remaining obligations retain
their separate proofs, actual gates and explicit boundaries.

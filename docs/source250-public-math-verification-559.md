# Published source250: retained public math tests

SPDX-License-Identifier: Apache-2.0

Exact commit `7443de65bb2a03aff75346a1c1234e9d9da4c3d0` has accepted evidence for **all 118 predeclared retained primary invocation IDs**, comprising 59 original plans in baseline and port phases. This is the disjoint union of **two separate completed campaigns**:

| Campaign | Run / attempt | Selected primary PASS | FAIL | SKIP | Complete semantic records |
| --- | --- | ---: | ---: | ---: | ---: |
| Normal | [38847087981](https://github.com/Heapy/krogu-math/actions/runs/38847087981) / 1 | 106 | 0 | 46 | 152 |
| Manual large | [38847100047](https://github.com/Heapy/krogu-math/actions/runs/38847100047) / 1 | 12 | 0 | 128 | 140 |

The selected ID sets have an empty intersection, and their exact sorted union equals the independently frozen118-ID set. These are invocation IDs, not118 Java source files or118 methods. The SKIP totals belong to their individual ledgers; adding them would double-count unselected coverage and exclusions. Neither result is official JCK/TCK certification or a claim that every original OpenJDK assertion is unadapted.

Supported-platform execution and oracle evidence are recorded separately in [source250-hosted-verification.md](source250-hosted-verification.md):5,743 passing method executions across13 supported target instances, plus25 successful oracle workflow checks. Those counts are not derived from these public-test campaigns.

## Source, JAR and completed jobs

Both campaigns bind the same exact [source250 manifest](source250.sha256), SHA-256 `5f99c5c5ee792180ae64b6448c82e034e4d13e21e7fedc1b6875e6c0e8bd92ef`. All250 hashes and the full source pathset were independently verified against the exact7443 source archive, SHA `56df40a554087526ef4e6ccfe9d6ee63561516cc040320c78cc1fd2341090a3c`.

The independently measured normal producer, normal consumer, manual-large producer, manual-large consumer and accepted local250 JAR are byte-identical:

`24a9e68403b77b7e6e1980472291b38e2cbb1bc30ee2f0d73e4d87e53239067f`.

Each producer/consumer `SOURCE_COMMIT` names the full7443 head; both checksum entries verify. Original API snapshots, producer uploads and consumer download logs bind the artifacts to their respective runs. The own JAR contains the four core classes and no compiled public TEST/framework/harness classes; no implementation body was decompiled to establish that boundary.

| Campaign role | Completed job | UTC interval on2026-10-16 | Outcome |
| --- | --- | --- | --- |
| Normal producer |116799960747|03:45:21–04:13:03|SUCCESS|
| Normal public consumer |[116807364435](https://github.com/Heapy/krogu-math/actions/runs/38847087981/job/116807364435)|04:13:29–04:21:34|SUCCESS|
| Manual-large producer |116799999947|03:45:30–04:01:08|SUCCESS|
| Manual-large consumer |[116804030795](https://github.com/Heapy/krogu-math/actions/runs/38847100047/job/116804030795)|04:01:10–04:03:58|SUCCESS|

The normal campaign step completed04:13:43–04:21:30. Manual-large's setup action encountered and retained a Java download abort, then its own download recovery; this is not a test/workflow retry. Original acquisition corrections and historical logs remain in the accepted evidence. This document combines accepted observations without rerunning a test, compiler, numerical helper, script/auditor or JAR.

## Artifact and raw-result identities

API digests agree with downloaded ZIP hashes and the corresponding upload/download logs.

| Artifact | ID / size bytes | ZIP SHA-256 |
| --- | --- | --- |
| `public-math-jvm-7443de65bb2a03aff75346a1c1234e9d9da4c3d0` |12024468026 /159463|`f0b9e821e5963afa7cb2971944641f4a38fd8aacef311a1cce979373f41677ab`|
| `public-math-evidence-7443de65bb2a03aff75346a1c1234e9d9da4c3d0-1` |12024743246 /3723533|`bc8512ab7fb9c29eeece02aa30b2ff9bd7e58f3541ae08831cbc571b22748799`|
| `public-math-large-jvm-7443de65bb2a03aff75346a1c1234e9d9da4c3d0-1` |12024167761 /159463|`61a9c2f85bd0d6f246d7f4cc447f54fc7b83efdca73aaffe39f9690827e8daad`|
| `public-math-large-evidence-7443de65bb2a03aff75346a1c1234e9d9da4c3d0-1` |12023648395 /1180664|`ef2011a308d9255f60a15bc675994c9e4a542bf20e83752daf773d39f35d88c8`|

The normal `build/math-tck/results/1792124038978/results.json` hashes `1440cf32599f117da3ce2b19240495679cddb90f50ec529026e33ad75698e2d5`; its `inventory.json` hashes `457fbeba285f2c245e2503789ab1588072efc60ac3b3958007784308b39e8c4e`. The manual-large `build/math-tck/results/1792123302295/results.json` hashes `182bdddc20c40e74a88c90b30df38f53ed0bb3a761cdb0321a273c731d40cfe2`; its inventory hashes `80826d6a2da1bef877c264b3ef6c0b3fbdce222b9cb7c3409244da8cbb7878f7`.

Before either result was read, frozen expectations declared the250-source pathset,59 complete plans, normal106 IDs, large12 IDs, retained118 IDs and12 loss records. Expectation report SHA is `3a66faf955872f9fc685286ff996f4d102bc9826d4594ab2045f1abe87784ff2`, with37-entry index `45891abd3579b26ef98bf7c1724cfd1b67857d12d05818d9ebd4fa772ccc591e`. Every actual plan field other than its outcome/reason matches that protocol, including original directive, arguments, flags, heap, timeout, requires and module fields. Both ledgers have unique complete `(scope,phase,path,id,method)` keys; counts alone were not used for acceptance.

## Original resources and completion

The normal campaign selects45 standard, two serialization, five heavy and one512MiB plan, in both phases. Its106 selected compiles/executions all exit0 without timeout or caught-OOM incompleteness. Compilation retains256MiB/120s; executions retain assertions, fast-throw-disabled flag,512MiB, seed559 and original deadlines:120s except `BigIntegerTest` run2's original400s. All four executed `BigIntegerTest` commands include subset0. Normal job/step limits remain360/330 minutes.

Manual-large selects the six original large plans in both phases, without a capacity SKIP:

| Original plan | Heap MiB | Deadline seconds | Baseline / port execution ms |
| --- | ---: | ---: | ---: |
| `BitLengthOverflow` |8192|120|142 /240|
| `DoubleValueOverflow` |8192|120|103 /97|
| `LargeValueExceptions` |4096|120|35046 /829|
| `largeMemory/DivisionOverflow` |8192|120|761 /5488|
| `largeMemory/StringConstructorOverflow` |8192|120|3702 /3809|
| `largeMemory/SymmetricRangeTests` |8192|180|31792 /36129|

All12 fresh pre-invocation memory checks meet original heap+512MiB margins: two4608MiB and ten8704MiB requirements. Original64-bit/physical-memory requires conditions and VM flags remain. The10g profile describes physical memory with8192MiB heap; it is not a10GiB heap substitution. Both `SymmetricRangeTests` commands retain subset0. All selected compilation/execution statuses are0 with no timeout or caught-OOM incompleteness. Producer job/step limits45/30 minutes and consumer180/150 minutes remain. Captured times establish completion, not a benchmark, uncontended-host premise or allocation bound.

Both campaigns use BellSoft Liberica25.0.4+9-LTS/Linuxamd64, Kotlin2.4.21, Gradle9.8.1, seed559, auto host memory and the unchanged runner. Public TEST sources remain pinned to `jdk-25.0.4.1-ga`, commit `7b65d74bcbd7c3a0b4c1f5c5bd85e64aa2e51b4a`; the runtime/test vendor/revision mismatch is explicit. Host-baseline RSA key generation uses its original unseeded `SecureRandom`; full deterministic RSA integration is not claimed.

All118 selected raw stdout files contain exactly their expected `HARNESS_COMPLETE` line. Each of the59 port invocations records the four distinct own core classes from the verified consumer JAR in both stdout and class-load logs:212 normal and24 large origin records in each channel. Baseline classpaths exclude the own JAR. All63 normal plus six large TEST caller audits exit0/no timeout with no host-math member references. Normal's only declared integration bridge references are `HostBridge.decimal` and `HostBridge.rsaExponent`; none is referenced by the six large callers. The audit reads captured public TEST callers, never foreign numerical implementation bodies.

Both normal `ModPowPowersof2$ModTester` children have complete property records, exact `success` stdout, exit0, no timeout and matching parent result records; the port child independently loads the four own core classes from the own JAR. Its original assertions/seed/512MiB are retained. Normal's TestNG Constructor runs each report five passes/zero failures/skips; retained JUnit suite headers show zero failures/errors/skips. Both large `LargeValueExceptions` runs retain the same five active TestNG methods, ten PASS markers total. Framework method counts are not confused with primary invocation counts.

## Restored SquareRoot call and twelve explicit losses

The normal `SquareRootTests` baseline and port both PASS. Original source SHA is `1d4abb1ea98e14a3cd28defd54d16f2ee49563e3862e86cad11399f5591274aa`; port compiled source SHA is `afb93448ad8b9224e7b6674226ce894a8a7ad746d3c64a55917047723c7387b3`. The complete transformation is imports only. The original unconditional `failures += scaleOverflowTest();`, its input10/precision1073741825/UP and expected exception assertion remain. The captured TEST main's compiled caller retains that call and reaches its final completion marker:230ms baseline,365ms port under original120s/512MiB. There is no separate giant-case timing or claim that this one cutoff expectation defines the entire public sqrt range. The large-only campaign does not select SquareRootTests.

The twelve unchanged port adaptation losses remain explicit:

| Count | Lost original coverage |
| ---: | --- |
|4|Invalid char-array range together with null MathContext: only null is replaced by UNLIMITED, retaining range operands and exception assertions; original compound-invalid precedence is lost.|
|5|Incubator Float16 host-BigDecimal integration methods; ordinary Float/Double cases remain.|
|1|JDK private `mag` storage invariant replaced by the own normalized-word invariant; racy public constructor execution remains.|
|1|SunRsaSign provider generation/signing phase; all three modular-power known answers remain.|
|1|Mandatory prime-cutoff expectation beyond the public minimum prime-domain guarantee; adapted acceptance does not identify the actual Boolean/exception branch without a raw branch observation.|

All twelve exact IDs/reasons match predeclaration. Normal emits their12 companion SKIPs; large's normal parents are unselected, so those companions are recorded in inventory but not emitted as extra large ledger rows. All baseline originals remain. There is no SquareRoot omission companion now. Original null/precedence and PrimeTest failures, earlier original SquareRoot failures and marked ordinal50 timeout remain historical evidence at their original source/run; they are not rewritten into passes.

Normal's46 SKIPs comprise12 unselected large primaries, these12 companions,16 upstream-disabled methods, two upstream-commented calls and four private/internal exclusions. Large's128 SKIPs comprise106 unselected normal primaries and the same22 disabled/commented/private records. No selected capacity failure was relabelled as PASS.

## Harness and licensing boundary

All57 upstream/support hashes,53 generated hashes and actually compiled baseline/port source hashes were checked. Original notices, source/dependency locks, generated sources, complete raw logs and captured caller audits remain in quarantined harness evidence. The13 owned tool hashes match exact7443. Entrypoint SHA is `8383d062feac7a3b2e8eda8f9e2844f7d2dbe6ac8d4e5c575f3fc92f58ad1265`, adapter `4ac1b789cd3df236bac8503383ddcbb4764399154b250f73e68227ddb12a1da7`, manifest `c3fd7136baa405654c4f1f0b5bdee482117c1d57810697174ea5648318e3a2c4`, source lock `03493e7b75110b9a709e126e5547bbe606b6a27f0fc2f0e9ac8747188af52e64`.

The57 original headers specify GPL-2.0-only without Classpath Exception. Original/derivative TEST material and the source-specific GPL adapter are separate from independently authored Apache-2.0 production/orchestration. GPL text, notices and seven dependency POMs remain; the14 dependency lock rows match, and retained POM hashes check. Dependency JAR/combined TEST binaries are deliberately excluded from evidence ZIPs; their recorded execution hashes are not falsely called a fresh check of absent binary bytes. The own input JAR is separate and has no TEST/harness/framework class leakage. This preserves the [licensing boundary](../tools/math-tck/LICENSING.md), not a new legal distribution determination.

## Accepted evidence and limits

The complete normal audit is frozen at `/private/tmp/krogu-7443-normal-actual-DyTMy5Kc/REPORT.md`, SHA `e3dec84a105bdad4f1e78d3118327744600cc18d8b974f4a30f750713ded8cfe`. Its1599-input index is `07d339db34e61a288314466f3be6aa2226f02eaa9a5a60ae4a96aca670fc38bd`,75-output index `6dae7dbb502bd2419121c9ddd3a91884e0af5d4b82e633f9c2ae08d37c339397`, and1636-entry final index `d4499ae928a988614f9df3e4ced9432404ddf76b71516a49e67360993936819d`; all were independently accepted without a bad hash.

The accepted large report is `/private/tmp/krogu-source250-large-actual-P1iBbzsP/REPORT.md`, SHA `e8cb063c29bc9e0cae50219cc51d7ce56e77c3762ac17d1f1be942957ef27368`;1030-input index `7b6142583dbb9d067940bd278f6e114b618dd360195c8da385d372ba0ca9d096`,108-output index `8d073877c7baf87f7510dba3575878517552d4fee3c3c9b4b1386798db994f28`. Original API/log/archive/results and all acquisition corrections remain in those frozen roots. The exact normal/large ID-set intersection/union tables are retained in the normal audit.

These are successful retained adapted public-test gates at source250. They do not adopt private source253/task593, execute symbolic giant590–592 witnesses, prove all supported ranges/subtypes or termination/performance, close526/548/557, impose legacy Java serialization compatibility, or establish official JCK/TCK compliance. The separate supported/oracle report and these two finite public campaigns keep their own scopes and evidence.

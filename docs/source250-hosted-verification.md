# Published source250: supported platforms and oracle checks

SPDX-License-Identifier: Apache-2.0

Published commit `7443de65bb2a03aff75346a1c1234e9d9da4c3d0` has accepted
evidence for **5,743 passing method executions across 13 supported target
instances**, plus 25 successful oracle workflow checks. These are six completed
jobs of [Compatibility run38847087981](https://github.com/Heapy/krogu-math/actions/runs/38847087981),
attempt1: four platform matrices, oracle and bare Linux ARM64. The normal-public
job is separately reviewed; its actual evidence is not accepted by this report.
No all-seven-jobs, public retained-ID union, full TCK/JCK or full-range claim
follows from this supported/oracle snapshot.

## Source and build binding

The exact [source250 manifest](source250.sha256) is
`5f99c5c5ee792180ae64b6448c82e034e4d13e21e7fedc1b6875e6c0e8bd92ef`.
All250 source hashes and the complete pathset match the freshly acquired
exact7443 archive, SHA
`56df40a554087526ef4e6ccfe9d6ee63561516cc040320c78cc1fd2341090a3c`.
This is the unchanged numerical composition of commit
`157e241dcfab31be0ce4709dd8ace9915424f46a`;7443 adds the adopted V7
classification metadata, not different numerical/test sources.

The original build and Compatibility workflow are unchanged:

| File | SHA-256 |
| --- | --- |
| `build.gradle.kts` | `b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6` |
| `.github/workflows/ci.yml` | `af3813bbf060def341086f4dc4edcacc0c5f9059d39c40462c3888110af5be9b` |

Actual logs retain Kotlin2.4.21/Gradle9.8.1/Liberica25.0.4+9-LTS, JVM25,
one worker,4GiB Gradle heap and UTF-8. Source configuration retains in-process
compilers, JVM `-XX:-OmitStackTraceInFastThrow` and JS Mocha120s. The requested
test tasks execute in their original jobs; later cache-cleanup `noop` commands
are not substituted for them. Restored caches remain part of the recorded
build; no empty-cache, hermetic-build or performance premise is asserted.

The Linux producer's own JAR is
`24a9e68403b77b7e6e1980472291b38e2cbb1bc30ee2f0d73e4d87e53239067f`,
byte-identical to the accepted [local250 artifact and verification](decimal-range-590-592-verification.md),
report SHA
`71933e0186c63657970a0f7cd368f231c5f554a5dd8f2c1472214d916118668a`.
That local report retains1,723 methods/426 suites on four targets. Its results
are separate from the hosted counts below. The input artifact's SOURCE_COMMIT
is the full7443 head plus newline, and SHA256SUMS verifies it and the JAR.
This binds the packaged build output; Gradle tests are not claimed to load that
packaged JAR instead of their compiled classes.

## Complete supported execution inventory

The full source-derived class/method keysets were frozen and independently
accepted before hosted results were read. Each actual XML multiset equals its
entire expected table, not merely its count. Exact target prefixes/suffixes are
the only name normalization. Every original XML/raw name is retained; all
duplicate/missing/extra/nonpass/owner/time-window issue tables are empty.
Each suite's declared tests equal its actual testcase children, with zero
failure/error/skip counters and child nodes. All1,364 XML files are individually
hashed in the stage1 evidence index.

| Completed job | Executed target methods | XML suites | Methods |
| --- | --- | ---: | ---: |
| Linux (116799960747) | JVM:574; JS:383; Wasm:383; LinuxX64:383 |426|1,723|
| Windows (116799960698) | JVM:574; MinGWX64:383 |256|957|
| macOS ARM (116799961040) | JVM:574; macOS ARM:383; iOS simulator ARM:383 |341|1,340|
| macOS Intel (116799960721) | JVM:574; macOSX64:383; iOSX64:383 |341|1,340|
| Bare Linux ARM64 (116807364288) | Linux ARM64:383 |0|383|
| **Total** | **13 target instances** | **1,364** | **5,743** |

Every JVM has 171 suites; each other XML target has 85. The bare ARM transcript
has 85 groups, not 85 additional XML files. MacARM also successfully compiles
`iosArm64`; iOS-device coverage is compilation only, not a 14th executed target.

ARM consumer116807364288 is completed/success at the same head/run/attempt,
on declared `ubuntu-24.04-arm`,04:13:31Z–04:19:25Z on2026-10-16. It downloads
the exact same-run Linux producer artifact and executes the original unfiltered
`./test.kexe`. All383 RUN names equal all383 OK names in order; unique keys equal
both the full source-derived non-JVM set and same-run verified Wasm set. Every
one of85 starts/ends has the correct class/count, with no unfinished method or
group, failed/skipped marker or stream issue. Header/footer and passed counter
all report383. These383 methods were executed, not inferred from cross-compilation.

## Artifact and provenance checks

All six artifact API sizes/digests match measured ZIPs and completed producer
upload logs; API metadata binds the exact same run/head. Full ZIPs, raw logs,
source archive, extracted XML and checked identities remain retained.

| Artifact | ID | ZIP SHA-256 |
| --- | ---: | --- |
| test-results-ubuntu-24.04 |12024831608|`a7987819b2d677d6880c3bc12a6bed425d61b3d0f015a05a65f15949194f5a41`|
| test-results-windows-2025 |12023487654|`961cfe40a0297606fd63c0efe727a01b905e257642d40c03a93c8bbd46b055e1`|
| test-results-macos-15 |12024297284|`604671f76a9da650c9fa68a6835a5f9dae6c6eeafbf94e0f59d171651e3748af`|
| test-results-macos-15-intel |12023859005|`b4fce9378ce165a2eb8c5944000074685690284e7ffa6abde24b9b63c869d8ae`|
| public-math-jvm-7443… |12024468026|`f0b9e821e5963afa7cb2971944641f4a38fd8aacef311a1cce979373f41677ab`|
| linux-arm64-tests |12024408258|`811aeabf423c32f16a65b5d2bd79094373b0eba445e3771053563a109ca620f4`|

The ARM ZIP contains only `test.kexe`,78,961,104 bytes, SHA
`189a374e24f82e8ae078942160414b8c437fa17df75ef7538de3e06558c201a5`.
The consumer log records downloading artifact12024408258 and the same measured
ZIP digest. It makes no source checkout; its source provenance is the verified
Linux build/upload and same-run binary. The complete ARM log SHA is
`32006e0a9b04e05853950730d4913e3b060cdc3a891c1f23825dc1096bf389e2`.
This is API/digest/source provenance, not a newly asserted signed-attestation
or reproducible-build guarantee. No compiled implementation was inspected.

## All25 oracle workflow checks

Oracle116799960598 is completed/success; its25 steps4–28 are all successful.
The ordered raw command strings match the predeclared25 original commands
byte-for-byte, and every corresponding script hash verifies in the exact-head
archive. All25 environment blocks retain the original fast-throw-disabled
JAVA_TOOL_OPTIONS. The context mapper retains both `-J-Xmx512m` and
`JAVA_OPTS=-Xmx512m`, with current V7 source SHA
`3e9552377df5702b483e5e325bb67de42b46280d7aa588d8533fee907f3e0dd7`.

Its actual completion verifies75728 row mappings/3000 controls/11472 class-mask
checks and the8 required/2048 reference-value/280 historical-error/73392 unresolved
partition. All75728 rows remain archival-blocked. Every other original check's
raw command, environment and output is retained. These25 workflow checks are
not25 additional test methods or a new independent arithmetic recertification.

## Independent evidence and limits

The accepted private reports and their full indices are:

| Evidence | Report SHA-256 | Index SHA-256 |
| --- | --- | --- |
| Predeclared source-derived expectations,37 files | `3a66faf955872f9fc685286ff996f4d102bc9826d4594ab2045f1abe87784ff2` | `45891abd3579b26ef98bf7c1724cfd1b67857d12d05818d9ebd4fa772ccc591e` |
| Matrix/oracle stage1,2,256 files | `2e0f4593ed8ac1226d7305f3e9c1bd94ba88030f67c357c1875f392adb37d9a8` | `977d3a4025eeda812a986c181fa1a8e543adb02b246dd8acbbaae9a749c1ae6c` |
| ARM stage2,35 files | `b69eeb63f9e122b5ef73bebef140f97d0aa8db31fcffce2d745d91e39465b071` | `a1f947fc2ec43d6f22ad01b107a67ba702c11dfd2550eeef2d0ebb2e4840636d` |

Their retained roots are respectively
`/private/tmp/krogu-source250-hosted-expectations-rMrByrHi`,
`/private/tmp/krogu-7443-supported-stage1-hJYW1nfe` and
`/private/tmp/krogu-7443-arm-stage2-sUW8l7HC`. Independent root checks accepted
all2,256+35 actual index entries. Metadata reader/index corrections remain
documented with the original evidence; no numerical retry or changed test
expectation was involved. Preparing this durable text runs nothing and changes
no source, fixture, assertion or previous evidence.

The earlier [47447/source247 hosted report](source247-hosted-verification.md)
remains historical evidence for its earlier source/JAR. Normal-public and
manual-large campaigns are separately reported by root; this report does not
accept their outcomes or assert an118-ID union. Normal-public actual acceptance
is still pending at this document's preparation milestone.

These finite supported results do not rerun the local94,282-row sqrt ledger
auditors, prove every mathematical/API input or actual-PRNG premise, execute
symbolic giant witnesses, establish maximum548 allocation capacity/performance,
close526/541/542/548/549/557 or the unimplemented593 pow obligation, validate
legacy streams, or certify official JCK/full public-TCK conformance. The525/557
Number caller issue remains open. Parent owns all later publication and task
dispositions; no future campaign/window is awaited by this preparation.

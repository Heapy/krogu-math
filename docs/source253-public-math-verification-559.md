# Source253: hosted public-math verification

Published commit `62bb43ccd384d5678dd22d25a95c97cc18f51362` completed two separate public-test campaigns on 2026-10-16. The normal campaign passed 106 retained primary invocation IDs; the manual-large campaign passed the other 12. Their actual PASS sets are disjoint and their union is exactly **118 phase-qualified IDs: 59 retained plans in baseline and port phases**, covering 53 public TEST source paths. Both ledgers report `complete=true` and `officialJck=false`.

| Campaign, attempt 1 | Producer / consumer jobs | PASS / FAIL / SKIP | Unique semantic records |
| --- | --- | --- | ---: |
| [Normal run38852537605](https://github.com/Heapy/krogu-math/actions/runs/38852537605) | 116816734740 / 116826422506 | 106 / 0 / 46 | 152 |
| [Manual-large run38852553721](https://github.com/Heapy/krogu-math/actions/runs/38852553721) | 116816787912 / 116817167865 | 12 / 0 / 128 | 140 |

These are two campaigns, not one 118-PASS normal run. SKIP totals are not additive coverage. The separate [supported-platform and oracle record](source253-hosted-verification.md) covers 5,812 methods across 13 execution instances plus 25 oracle commands; those are not additional public TEST invocations. The later source255 local composition (1,757 methods and 94,282 sqrt-ledger records) has a different source identity and supplies none of these hosted outcomes.

## Source, runtime and artifact binding

Both campaigns use the exact published head above. The archive and complete 253-source hash/pathset checks bind the source; same-run producer and consumer `SOURCE_COMMIT` files contain that full head plus newline, and their embedded `SHA256SUMS` pass. The normal producer, large producer, both consumers and separately accepted local source253 build have byte-identical own JARs. Mutable main or a later build is not substituted.

| Bound input | SHA-256 |
| --- | --- |
| Exact-head source archive | `8b8fbaa172d4d91c4524563cda7c5f41e0f9abeae889112b7db73a220053dc09` |
| Source253 manifest | `aa90e836005efc363e9575026cd12049b0bb6ac42eb3a716f20d04c596bac1ad` |
| Own producer / consumer / local JAR | `5e07541044a098601d0cdb05ae1c73eaae063fb8181836b5da3ff6a1a97d72c1` |
| Build configuration | `b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6` |
| Normal CI workflow | `af3813bbf060def341086f4dc4edcacc0c5f9059d39c40462c3888110af5be9b` |
| Manual-large workflow | `25d97734ca350b2cd4fd70750964be4549ba70b22246ec641954bfb9c0badb61` |
| Public entrypoint | `8383d062feac7a3b2e8eda8f9e2844f7d2dbe6ac8d4e5c575f3fc92f58ad1265` |
| Source adapter | `4ac1b789cd3df236bac8503383ddcbb4764399154b250f73e68227ddb12a1da7` |
| TEST manifest | `c3fd7136baa405654c4f1f0b5bdee482117c1d57810697174ea5648318e3a2c4` |

All 13 harness/tool pins, the full retained-plan projection, source/dependency locks and original configuration match the before-actual source253 expectations. That predeclaration is frozen at `/private/tmp/krogu-source253-hosted-expectations-21EXn2hQ`, report SHA `98461d901f1b3e67e92bce75efab23b5a4fb297b8a8a9c5b3f73af3dc0ebc925`, index SHA `8c462e3af43db372a8018c0dc7182d76fdebada0d07692e55e8a6369253320c2`. Expectations selected the required IDs; historical outcomes did not.

Runtime is BellSoft Liberica **25.0.4+9-LTS**, Linux amd64, Kotlin 2.4.21 and Gradle 9.8.1. Public TESTs remain pinned to `jdk-25.0.4.1-ga`, commit `7b65d74bcbd7c3a0b4c1f5c5bd85e64aa2e51b4a`: runtime and TEST revision are explicitly different. Restored caches are recorded, without a clean-cache or hermetic-build claim.

| Actual artifact | ID | ZIP bytes | Measured SHA-256, matching API and upload |
| --- | ---: | ---: | --- |
| Normal own-JAR producer | 12026525340 | 174543 | `33589078bfdc5f2f4fe08013bee0acc6a47d5d8078d78d5be97fb1c9b4ecdd5c` |
| Normal evidence | 12026618204 | 3728728 | `e25b5279bebe3dc4176b49bcbfae96d7db480192d5764a05b6d2a038d4707f73` |
| Large own-JAR producer | 12025777169 | 174543 | `7ab3f06805366d0fc14f4d8eff645e3a0f2e0bc08a9f9b490d624faa8aab4b2e` |
| Large evidence | 12026200722 | 1196794 | `f21681aaa7411f9df09276ff56b68ff0332ac82d08f5865cc5da812a18604235` |

Both consumer download logs identify their exact same-run producer artifact and digest. Normal results `1792128338064/results.json` hash to `56e5481e2ef58099d237497da75af0db66833df070f4756e72c431c5c01b1fde`; large results `1792126260724/results.json` hash to `31a796f230926072a170cac73a1b8afdf02a46d68cc41b497722968555eb3cd1`.

## Original selection, resources and completion

Normal executes 53 plans per phase: 45 standard, two serialization, five heavy and one memory-512m. Its six unselected large plans yield 12 primary SKIPs for **profile not authorized**, not external capacity unavailability. The remaining 34 SKIPs are 12 adaptation companions, 16 upstream-disabled methods, two commented calls and four private exclusions. Large executes all six selected plans in both phases; its 128 SKIPs are 106 unselected normal primaries, 16 disabled methods, two commented calls and four private exclusions. Adaptation companions are not emitted there because their parents are unselected; their static losses remain.

Normal retains 512 MiB heaps. Compilation has the original 120-second limit and `-J-Xmx256m`; execution retains 120 seconds except BigIntegerTest run2's original 400 seconds. Nested ModPow children retain 110 seconds. Normal outer job/step bounds are 360/330 minutes. Large retains the original three 4g/8g/10g profiles, maximum heap 8192 MiB, 120-second execution deadlines except SymmetricRangeTests at 180 seconds, and producer 45/30-minute and consumer 180/150-minute job/step bounds.

| Selected large plan, both phases | Heap | Execution deadline |
| --- | ---: | ---: |
| BitLengthOverflow | 8192 MiB | 120 s |
| DoubleValueOverflow | 8192 MiB | 120 s |
| LargeValueExceptions | 4096 MiB | 120 s |
| largeMemory/DivisionOverflow | 8192 MiB | 120 s |
| largeMemory/StringConstructorOverflow | 8192 MiB | 120 s |
| largeMemory/SymmetricRangeTests | 8192 MiB | 180 s |

All original assertions, arguments, applicable VM flags, seed559 and both subset values zero remain. Actual selected BigIntegerTest commands contain `-Dsubset=0`; no test input, certainty, precision, subset or deadline was reduced to obtain a pass. RSA provider key generation still uses unseeded SecureRandom; seed559 does not make that phase deterministic.

Both hosts report 16,766,414,848 total physical bytes. Normal's 106 fresh free-memory samples range from 11,613,536,256 to 11,871,723,520 bytes; its fresh-memory gate is not required. The recorded normal margins are 1024 MiB for 104 rows and 1536 MiB for the two nested-child rows. Heavy 512 MiB is not proof of the large-capacity gates. Large's 12 fresh samples range from 11,723,247,616 to 11,898,994,688 bytes: two 4608 MiB and ten 8704 MiB heap-plus-512 MiB gates pass, along with original physical/64-bit predicates. There is no selected large capacity omission.

Every selected compile and execution exits zero without timeout. Neither ledger reports caught-OOM assertion incompleteness. All 106 normal and 12 large raw completion markers occur exactly once for their expected invocation; all 666 normal raw-log references and 74 large raw-log files are retained, pathset-checked and hashed. Timings are diagnostic observations, not performance comparisons.

## Class loading, callers, children and frameworks

All 53 normal port invocations and six large port invocations independently identify the four own core classes from the bound own JAR in stdout and class-loading logs. Baselines use host classes and omit the own core JAR. Normal's 63 public TEST caller audits and large's six audits exit zero without timeout: no host `java.math` member references remain in port callers. Their recorded own-core reference counts are 603 and 71 respectively.

The only two allowlisted integration bridge references are `HostBridge.decimal` in DoubleFloatValueTests and `HostBridge.rsaExponent` in ModPow65537. These are explicit host-integration boundaries, not proof that every calculation is port-only. Baseline ModPowPowersof2 nested orchestration is adapted and is the sole changed baseline prepared primary source; “baseline” does not mean every prepared file is byte-original.

Both nested ModPowPowersof2 children pass with exit zero, no timeout, exact `success` stdout and their expected completion marker; the port child also has all four own-JAR origins. Captured child properties agree with the result metadata. Assertions, seed, heap and child deadline are retained.

Normal's two Constructor TestNG invocations each pass their five original enabled methods. Its four JUnit invocations retain all 12 engine XML files; each Jupiter suite passes the original two methods and companion engines contain zero tests. LargeValueExceptions' TestNG invocations each pass five enabled methods. All framework counters and method sets agree, without failures, errors or skips; framework methods are not counted as extra primary IDs.

**SquareRootTests is restored, imports-only, and actually completes in both normal phases.** The original main call `failures += scaleOverflowTest();`, its input, assertions and body remain, and the retained compiled public caller invokes it. Original source SHA is `1d4abb1ea98e14a3cd28defd54d16f2ee49563e3862e86cad11399f5591274aa`; port source SHA is `afb93448ad8b9224e7b6674226ce894a8a7ad746d3c64a55917047723c7387b3`. Complete main passes under the original 120-second/512 MiB limits. This establishes restored selected-main coverage, not separately measured scaleOverflowTest timing or every giant sqrt input. Earlier timeout/failure evidence remains historical and unchanged.

## Retained adaptation losses and package boundary

Twelve explicit original-coverage losses remain. The frozen report and full ID/reason ledger retain each exact selector:

- Four char-array invalid-range plus null-context calls replace only the null context with UNLIMITED, retaining the range operands and NumberFormatException assertion; joint-invalid precedence is lost.
- Five Float16 integration calls are omitted: near minimum, minimum-normal, maximum, random and extremes.
- One JDK-private storage invariant is replaced by the own normalized-word invariant; the racy public constructor execution remains.
- One SunRsaSign key-generation/signing provider phase is omitted; its three modPow known-answer assertions remain.
- One PrimeTest mandatory-cutoff expectation above the public minimum prime-domain guarantee is adapted. Its old failure remains a failure; current PASS does not reveal which permitted normal-false/ArithmeticException alternative occurred.

There is no thirteenth SquareRoot omission companion in this source revision. Restoring that call does not recover these twelve losses, disabled/commented calls, private exclusions, source/ABI differences or provider integrations.

All 57 locked upstream/support files, 53 generated derivatives and actually compiled primary source hashes verify. The 14 dependency-lock records, seven retained POM hashes, 17 notice/index files and ten nonempty indexed notice texts are checked. Dependency binary bytes are intentionally absent, so their locked identities are not represented as newly rehashed absent binaries.

Original TESTs and derivatives retain GPL-2.0-only notices without a Classpath-exception reclassification; the source adapter is explicitly GPL-2.0-only. Independent orchestration and production remain Apache-2.0. Sources, generated sources, GPL text, notices, locks, provenance, audits and raw logs are packaged. The normal evidence has 896 regular files and large has 290; neither has unsafe paths or packaged TEST/framework/dependency JARs or class binaries. The only packaged JAR is the separate own input JAR. No numerical implementation body or bytecode was inspected by these metadata reviews.

## Accepted evidence and limits

The complete normal review is `/private/tmp/krogu-62bb-normal-public-JGlhRSaD/REPORT.md`, SHA `d20406d608f93334e9649edb472899b486bc8ad60d986c9280d2adc022ccaa61`. Its input/output indices are `e8192d0a82b2b5b2d69617e6a2eacafbf1046373ed36b80361fac37175f0f255` and `7797965c64e2dc5779efb77db311ec32f511395e2a5615f482626ca19a471963`: 1,681 input and 203 output checks were independently accepted. Preserved acquisition/reader corrections do not alter raw observations.

The complete large review is `/private/tmp/krogu-62bb-large-actual-OMDZVwDT/REPORT.md`, SHA `2f4978c69ededd953be4bc43ff21a339a467190c0895c97493ca649d375d6b9e`. Its input/output indices are `7db1d895f5e9a653573e0511844701871436c7ed0a7f1e7a59ec08a8a061d17a` and `42678aee0590aa06a0112b3327887eb608962bda3b989891a567f833c28b4283`: 1,086 input and 119 output checks were independently accepted. Complete original IDs, observations and raw/API artifact bindings remain in these frozen reports and indices.

This is finite, source253-scoped retained-public-test evidence. It is not official JCK/TCK certification, complete API/source/ABI compatibility, a whole supported-magnitude or physical-capacity proof, a performance result, a PRNG-distribution proof, a Number-migration waiver, or closure of all tasks526/541/542/548/549/557/593. Conditional source proofs and remaining contract obligations keep their own premises. Later source255 evidence, historical source247/250 outcomes and unexecuted symbolic giant witnesses remain separate.

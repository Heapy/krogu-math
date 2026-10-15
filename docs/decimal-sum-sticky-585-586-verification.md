# Decimal sum and remote-tail validation (585/586)

The aligned decimal sum now rounds or cancels coefficients before requiring a
public BigInteger representation. This repairs supported-result cases where
the old add/subtract route first constructed an unsupported aligned operand or
sum. A separate remote-tail equality repair avoids constructing10^D when a
nonzero coefficient has exactlyD truthful digits and therefore quotient zero.

The [sum proof](decimal-sum-585-proof.md) binds the private raw representation,
alignment/cancellation, rounding, publication and workspace bounds. The
[sticky equality proof](decimal-sticky-equality-586-proof.md) binds its narrower
branch, including the stored-zero and truthful-precision guards. Existing
fallbacks, validation prefixes, explicit caller override/super contracts and all
old tests/fixtures remain. Internal callback traces do not become universal
requirements. The numeric value classes retain their fields and serialization
format. The route adds no precision/magnitude cap, host-math fallback or
persistent cache.

## Exact validated source

Fresh root `/private/tmp/krogu-585-586-validation-g0etGe15/root` was created from
archive `aad5297c47df6d6a76fe58a8e0497ae9169c9a1b`, then received the independently
reviewed585/586 overlay. Archive SHA256 is
`35f53ba2975ed2600b283110b32427e74571921cb3ece5adf2ceb03a6d33cf88`.
The240-source manifest SHA256 is
`e3539c0bda309350d72ebb89656a9f54e3c7f720fe943616153050e2be493aa9`.

|Source|SHA256|
|---|---|
|BigDecimal.kt|`5dd07b9f985c53f09e5c551d30b0ae0c4faeea705343285a91eac1d9282f1e1c`|
|BigInteger.kt|`12fd0a14164bfd81b004c66673ae37ea40719bf3552f171ba20b434a296d8602`|
|DecimalSum585.kt|`f89ca4fb38988f58b0ce764c54ef50f95338b69400a8ef202d5078e89c8179c5`|
|DecimalSum585Test.kt|`e773fe7c8226fcf0f2a254e405030b2ff6bd4b2c8d1d6f26740e8569b7454f52`|
|DecimalSum585SubtypeJvmTest.kt|`2549c2bae5c935eb65e11b6b6457fb185c57ecb48a1c8c4798c641c4049a33ad`|
|DecimalStickyEquality586Test.kt|`edb718581affff76260e3930e9a798b7e170918a03f24298c7f09fc11c8a667f`|

Only BigDecimal and BigInteger changed among the236 baseline sources; four
source/test files are new. All other234 files retain their bytes. BigInteger's
only change is an18-line nonvirtual decimal bridge after parallelMultiply;
every preexisting integer body remains unchanged. The published src tree was
checked against all240 pins after adoption. Documentation updates do not change
that source binding.

## Actual local execution

Root grant was local:526 comment2389. One full Gradle invocation, session13641,
exited0: **BUILD SUCCESSFUL in14m22s,49 tasks executed**. It built jvmJar and ran
all `io.heapy.krogu.math.*` tests on all four targets. Kotlin2.4.21,
Liberica25.0.4+9-LTS, Gradle9.8.1, the4GiB Gradle heap, one-worker/in-process
configuration and existing test settings remained unchanged. No campaign retry
or source/assertion/input/deadline/resource change occurred.

The independently reviewed XML auditor ran once afterward, session67336 exit0,
with a fresh512MiB SerialGC script cache. Script SHA256 is
`f7baa12b531a2f84857569569c0d9980f59c7978d718dca3d7b8cdda59b5eb04`.
It derives the complete expected class/method inventory from the pinned sources
before opening XML and verifies all source/build pins again afterward.

|Target|Actual suites|Actual methods|Failure/error/skip/inventory issues|
|---|---:|---:|---:|
|JVM|163|545|0|
|JS Node|78|355|0|
|Wasm JS Node|78|355|0|
|macOS ARM64|78|355|0|

All **1,610 method executions** passed, with no missing, extra or duplicate
test. Six new common methods on each target and two new JVM methods contribute
26 executions beyond the historical1,584 baseline. An independent actual/adoption
review verified all397 raw XML files against source-derived and recorded exact
multisets, all240 source pins, the full capsule and fresh own JAR.

The585 common source prescribes18,267 direct raw-helper calls using independent
bounded integer/rational controls, all rounding modes, signed alignment and
cancellation, real sparse-page gaps, carry/borrow/sticky and alias checks. The
586 methods prescribe64 mode/sign/add-subtract cases plus24 boundary/order
controls, including bounded10,000-digit truthful coefficients. Two JVM methods
exercise current-own-format truthful coefficient subtypes and explicit caller
contracts. These counts describe loops within passing methods; they are not an
independent per-call ledger or additional XML test methods.

## Evidence and limits

Evidence root: `/private/tmp/krogu-585-586-validation-g0etGe15`.
Frozen actual report SHA256 is
`d1a477399b3d925290c72cb2be05fa2acd424b96159e86402e2f6b2e9af3bade`.
Capsule index `065214023c63d45ef5f1018a0ff9b52d5d7c6c3a2c1ef99a14b6b14e457e3dba`
covers683 files, including all240 sources, all397 raw XML files, source/actual
inventories, commands, original logs, proofs, baseline archive and built artifacts.
Both parent and independent reviewer verified every index entry. Fresh own JVM
JAR SHA256 is `1cacc5b41ede2e154e9c7baf948bd1748f4d424932f3fb7e4a592085b1ed00e1`.

Both owned numerical/compiler invocations terminated before the explicit mutex
release; the tracker confirmed an empty local window at revision812. A later
window owner receives no implicit runtime grant from that release. One reviewer
metadata checksum attempt used the wrong directory; corrected source/path checks
passed without numerical execution or changes to evidence.

The giant range witnesses remain symbolic and unexecuted. This report supplies
no giant JDK bug observation, benchmark, practical maximum allocation promise,
full541/548 proof, all-input experiment or official JCK result. Matching hosted
platform and public-test gates for this source240 revision remain separate.
Historical f58/e36 CI and public evidence retain their original source hashes.

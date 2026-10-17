# Kotlin call forms595/596: local source260 verification

SPDX-License-Identifier: Apache-2.0

This records the narrow Kotlin migration changes in the composed **local
source260** revision. Task595 replaces seven arithmetic operator members on each
number type with equivalent imported extensions. Task596 adds the imported
`BigInteger.lowestSetBit` extension property. Root has captured compiler and full
four-target execution results for this composition; these are distinct from the
previously published12d/source255 results.

**Independent actual review accepted this local composition.** Its report,
`/private/tmp/krogu-260-actual-independent-ztHKNEJl/REPORT.md`, has SHA256
`62704502dae3784fcae3acd06c178893e5667f93359560e18124035f4217e019`.
Root read the complete report and verified its seven copied inputs, 795 external
inputs and 92 output entries. The review checks every source/path, all 446 raw
XML files and 1,785 method keys, all 94,282 ledger records and the compiler/artifact
bindings. It uses metadata readers; it does not rerun the numerical verifiers or
extend their accepted scope. Matching published-head source260 gates remain open.

## Concrete source forms

The earlier [finite call-form inventory](kotlin-migration-525-callforms.md)
describes source255 and keeps its original paper witnesses uncompiled in that
inventory. Its two concrete findings have new, separately recorded dispositions
here; no historical source ledger is silently rewritten as an execution report.

**595, caller-owned arithmetic.** A Kotlin subclass of a JDK number can declare
its own `operator fun plus(other: BigInteger)` without `override`. The old krogu
final member collides with that declaration; adding `override` cannot override a
final method. A separate old-source compiler witness records JDK exit0 versus
old-krogu exit1 at the two BI/BD caller-plus declarations. Its observed diagnostic
is hiding/needing override; finality is the independently reviewed language/source
argument, not a second observed override-attempt diagnostic.

The new common extensions keep the exact old bodies for `plus`, `minus`, `times`,
`div`, `rem`, `unaryMinus`, `unaryPlus`, for both types. Applicable caller members
win on a subtype receiver. A base-typed receiver selects the imported extension,
which calls the existing virtual arithmetic member. A differently named alias
can explicitly select the extension even on the subtype. This preserves the
member/static-extension distinction that merely making the old operators open
would change. The named operand remains `other`.

BigDecimal `/` still calls `divide(other, RoundingMode.HALF_EVEN)` and retains the
dividend scale; exact one-argument `divide` is not substituted. Genuine open
BigDecimal `plus()` and `plus(mc)` remain. Unary plus keeps krogu's own previous
receiver-identity convenience; no stdlib `java.math` unary-plus extension is
invented. Public add/subtract/multiply/divide/remainder/negate result and directly
thrown exception objects pass through unchanged. Explicit caller `super` calls
remain real member calls; no incidental numerical-kernel callback trace is promised.
See [the import/operator guide](kotlin-operator-migration.md).

**596, getter property.** Java's `getLowestSetBit()` has a Kotlin synthetic
property form. An own Kotlin method declaration alone did not provide that
property. The new read-only common extension is `val BigInteger.lowestSetBit:
Int get() = getLowestSetBit()`. It retains virtual getter dispatch and its exact
Int result/direct exception object, without recomputation, backing field or setter.
An explicit property import is required; a class import alone remains insufficient.
Method spelling and alias imports remain available. This is ordinary property
access, not a promise about experimental Java synthetic-property references.
See [the property guide](kotlin-lowest-set-bit-property.md).

```kotlin
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.plus
import io.heapy.krogu.math.lowestSetBit

val sum: BigInteger = BigInteger("7") + BigInteger.TWO
val position: Int = BigInteger("8").lowestSetBit
```

The accepted migration boundary includes explicit extension imports and the
previously adopted non-null/override-nullability edits. This change does not
alter the delegated numerical methods, nullability signatures, constructors,
serialization or Number conversion bodies, add a kernel/cap/cache or require
production Java source/ABI compatibility. Current
own-format round trips and every old assertion/fixture remain active. Legacy and
cross-format streams remain outside the requirement. Number525/557 is unchanged.

## Exact composition and source reviews

The fresh no-Git/no-build validation tree composes frozen595 payload9 and596 six
additions over published `12d0fe287cdec99567bd7cbf981b65aa86ef834b`. Source260
manifest is **`fb77e13e2db823c45a9fc384818b9f9c43032f94997a1270d459768be3e4ffa7`**:
four original sources change, five are added, none are removed; all251 nonowned
original source hashes remain unchanged. Source pre/post checks pass in root's
captured runtime. New own JAR is
**`58dd02a0da05c3f4740bc772029f44023c367f1b00f2022ae0b8620567da4322`**.

| Accepted source/preparation argument | SHA256 |
| --- | --- |
| Independent595 source/code/test review |`eb66fcd45d3062547abd2900005af80f5f4fd57e0094fbfb2a4750672aa4dca6`|
| Independent596 source review |`7aaf8150dfc901446cb93cea5edb3acfe752cdba23b2a2ad748e629f34a06699`|
| Independent composition/auditor/runtime-plan preparation review |`cbbe44260af38577ecf50e93a97b901a62b914c5883dc5d1b4169c42a3fec93c`|
| [595 source proof](arithmetic-operator-595-proof.md) |`71d71db6c21c2754e6a5c02d5b63f96ffff07759a8a2c2484d261d7a15242ba0`|
| [596 source proof](lowest-set-bit-property-596-proof.md) |`59d7b9b395c54ea01352416b30845fb3bca17e47f09b04f5f4c7e522acbbd7f6`|

Both original proof/handoff/guide payloads remain frozen at their source-only
milestones. Their UNCOMPILED/UNRUN-at-preparation statements are historical;
the subsequent composition/compiler/runtime observations here are separate.
The new tests use separate migration packages, explicit result types/imports,
bounded independent literals and paired JDK/own source shapes. They cover all
seven truthful caller-owned operators, named/aliased/base selection, decimal
scale/HALF_EVEN, unchanged aliases and explicit reached result/exception identity;
property controls add zero/sign/limb boundaries, getter/super and direct exception
identity. Each task adds three common and two additional JVM methods.

## Root-captured local execution

Root granted and recorded six strictly sequential calls. Each was terminal before
the next; no source repair/retry or numerical output-class execution was added.

| Order / tool session | Captured outcome |
| --- | --- |
|1 /71623|OldPropertyMissing.kt versus old source255 JAR: expected compiler exit1, unresolved lowestSetBit at5:37; no emitted classes.|
|2 /5685|One jvmJar plus complete JVM/JS/Wasm/macARM Gradle selection: exit0, BUILD SUCCESSFUL10m42s,49 tasks executed.|
|3 /10586|Full source260 XML auditor exit0:446 suites/1785 method executions, zero issues.|
|4 /66221|Complete retained sqrt-ledger auditor exit0:94282 records/52 recorders, zero issues.|
|5 /93094|Unchanged truthful caller-owned operator witness versus fresh source260 JAR: compiler exit0, empty stdout/stderr.|
|6 /95596|NewPropertyImported.kt versus the same JAR: compiler exit0, empty stdout/stderr.|

The operator witness bytes remain
`9e5da4ffee7739f17e4815d44c7da75a6bad630671cb98ce151333e6023b0a03`.
Its old-negative header remains historical; it is compiled successfully only in
the separately recorded new context. Compiler output classes are retained but
not executed. The positive property witness adds the explicit property import.
Old paired JDK/old-krogu compiler report remains
`7377e71f7a698c37c0f2ca08e6f47d32145af0a88453a9c00868a2835947a775`.

| Local target | Classes/suites | Methods | Failure/error/skip/omission/duplicate issues |
| --- | ---: | ---: | ---: |
| JVM |179|594|0|
| JS Node |89|397|0|
| Wasm JS Node |89|397|0|
| macOS ARM64 |89|397|0|
| **Total** |**446**|**1785**|**0**|

The source-derived expected/actual class/method multisets and suite counters
match, including all28 new595/596 method instances. These are test executions,
not counts of every assertion or arbitrary caller class. Gradle NO-SOURCE/SKIPPED
configuration tasks are not skipped tests. XML summary SHA is
`ed06ec3c4d54cf013a7eef45a525d73cf448aecd51380b4c4a5081e479ba45c3`.

The complete strict44-column numerical ledger retains38497 JVM records/16
recorders,18595/12 per other target:94282/52, all four COMPLETE. Full record bytes
are identical to accepted255, SHA
`997e40c39be2a0b4ffb8152690d45d7f1cf5e1f713b11e6c2a01205d2203457a`.
No numerical policy/assertion changed. The numerical outcomes are retained
regression evidence, not new all-domain arithmetic/subtype certification.

## Reproducible evidence and warnings

Root's frozen runtime report is
`/private/tmp/krogu-595-596-composition-PpYrGAS5/evidence/RUNTIME.md`, SHA
`253e02e6391639d5eaedbba4e1e6b3d381b41b377d1bd4558416aa86ec8aa879`.
Its787-entry runtime index is
`6f0dbe02a11a32a5a17a885d2a84a7ae4f68a5aa97cb01de8cb7ddfa3888d7e4`.

| Exact protocol/output | SHA256 |
| --- | --- |
| Runtime plan |`d4f034f25aa6c378b1da15c7d99c3ab66e0be8a5168967ed79f460acaedf28d6`|
| Captured six-command preparation |`c7ebfeccee44bb381a16d98737dc51acc53916749033c039f8c8a5e01e3028af`|
| XML auditor |`4b02f4f69b5f3bf44c0b976beba60db1738d6530a89c282683ace9c2c05aafc8`|
| Ledger auditor |`ef358f50ba2326cf2a4a9dbaacec37ebfdbea10a9f4657d3f2ed4c27ef6879b8`|
| Full Gradle log |`15fa2d9e6204136f249f1e695be6bc8f5a3313ea9fc3336ba80db69182e0ee4a`|
| Recorder summary |`b5af10056ef1316f2b8e1425bc8f5893e7991694e55b20369f49da4e80bd5fb4`|

Each auditor differs from its accepted255 predecessor only by four provenance
lines; mathematical/lexer/ownership/status protocols are unchanged. Actual XML
arguments name the four owned root/build directories. Auditors use distinct
fresh absolute script caches/output paths,256MiB/SerialGC and sanitized inherited
options/classpath. Standalone compilers use512MiB/JVM17. Toolchain remains
Gradle9.8.1/Kotlin2.4.21/Liberica25.0.4+9-LTS; the Gradle call retains4GiB,one worker,
in-process compilation and existing shared tool caches. This is not a clean-cache,
performance or uncontended-host claim.

Root's launch marker is06:46:56UTC and terminal result was observed06:58:24UTC;
10m42s is Gradle's own duration, not a fabricated exact wall-stop time. All six
owned calls were terminal before explicit runtime-window release07:01:53UTC,
followed by an empty mutex list. This does not assert a system-wide process scan.
The log retains the nonfatal FSEvents diagnostic, existing compiler warnings,
custom JS reporter notice and Gradle configuration-cache suggestion. A manual
post-runtime source reread first used the parent cwd and reported missing paths;
the failed reader output remains, and corrected cwd gives all260 checks PASS.
Both auditors already passed source pre/post guards. No source edit, auditor
rerun or warning suppression followed that reader correction.

## Separate publication and remaining scope

Previously published12d/source255 evidence remains
[5855 methods across13 supported instances plus25 oracle checks](source255-hosted-verification.md)
and [normal106 plus separately large12 public IDs](source255-public-math-verification-559.md).
Those results bind old source255/JARbb15, **not source260**. Matching published-head
supported/oracle/public gates for260, adoption and task transitions remain separate.
The finite source255 call-form inventory remains a historical accounting, with
these two new local dispositions recorded here rather than retroactively compiled
paper witnesses.

This does not close full525/553, change the required Kotlin/JS erased/generic
Number525/557 UNSAT result, validate arbitrary numerical/resource/subtype domains,
run giant witnesses, establish performance or pass official JCK/TCK. It is the
narrow call-form repair and its specifically bound local observations.


## Later caller-workaround disposition

The preceding remaining-scope statements belong to this source260 report. The
later user-selected525/557 migration explicitly edits affected Number/generic
callers to the imported `toKrogu*` conversions (525 comments2544/2545), as described
in the [current caller guidance](../README.md#kotlinjs-converting-through-number).
This accepted workaround preserves the original UNSAT diagnostic and compiler
limitation; it does not claim transparent repair of `Number.toX()` or third-party
call sites. The595/596 implementation repairs are accepted historical evidence,
not tasks reopened by the report's old pending wording. Final553 still requires
matching final-source caller/supported/public evidence and its full qualified
requirement audit. Neither539 nor598 nor553 is declared complete here.

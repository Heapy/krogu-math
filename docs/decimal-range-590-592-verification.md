# Local verification of decimal range repairs590–592

The exact250-source composition completed426 suites /1,723 methods on JVM,
JS Node, Wasm Node and macOS ARM64 with zero failures, errors, skips, duplicates,
omissions or extra methods. Its complete sqrt comparison ledger contains94,282
strict records and52 completed recorders with zero issues; every exported record
is byte-identical to the accepted247 and248 tables. These are bounded local
results, not full-domain or official JCK certification.

The composition contains the [590 exact LOW residual certificate](decimal-exact-low-residual-590-implementation.md),
[591 primitive low-bit conversion and comparison refinement](decimal-primitive-low-bits-591-proof.md),
and [592 fixed-scale quotient route](decimal-fixed-scale-592-proof.md).
Their original source-only handoffs remain historical and unchanged:
[590](decimal-exact-low-residual-590-handoff.md),
[591](decimal-primitive-low-bits-591-handoff.md), and
[592](decimal-fixed-scale-592-handoff.md). The UNCOMPILED/UNRUN statements there
describe preparation milestones; this report supplies the later exact-composition
execution evidence without rewriting those milestones.

## Source and artifact binding

The retained validation workspace is
`/private/tmp/krogu-590-592-composed-validation-9LiexHQG`, repository subdirectory
`root/`. The exact [source250 manifest](source250.sha256)
lists all250 Kotlin/Java source paths and hashes. The build file and full pathset
were checked before/after the campaign and both auditors. An independent later
metadata read rechecked every source and all426 actual XMLs.

| Item | SHA-256 |
| --- | --- |
|250-source manifest |`5f99c5c5ee792180ae64b6448c82e034e4d13e21e7fedc1b6875e6c0e8bd92ef`|
|`build.gradle.kts` |`b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6`|
|Fresh own JVM JAR |`24a9e68403b77b7e6e1980472291b38e2cbb1bc30ee2f0d73e4d87e53239067f`|
|`BigDecimal.kt` |`b1570ccd8cfb0e640e4566ce727d7d1f7ed18cc4346f90d2378b72834ac2323e`|
|`BigInteger.kt` |`652262fa7f92f9766dea92fd2143224449d509968228d8b21f6d85ee86cf2441`|
|`DecimalDivision581.kt` |`553de7bece22f88526e029d0a931cf0fccc99a73d90534e588911b19d46bc193`|
|Initial Gradle log |`7f88e3b6ec1a721fe09d8eb20b38fa5710ddbb90755e4280fc84ed7d7388f658`|

The JAR is `root/build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar` and original
command/log are `evidence/command.txt` and `evidence/initial-four-target.log`.
The [source247 hosted report](source247-hosted-verification.md) records the
previous published47447f2/source247/JAR6e6 artifact. Its hosted results are not
relabeled as250 results. Matching250 hosted gates remain pending here.
Source593 pow is unimplemented and absent from this composition.

## Actual campaign and complete method inventories

ONE original Gradle session17140 completed exit0 in10m50s,51 actionable tasks,
all51 executed. Its recorded bracket is2026-10-16T03:17:26Z–03:28:17Z. The
command builds `jvmJar` and selects the full `io.heapy.krogu.math.*` package for
all four targets, with one worker, no daemon,4GiB Gradle JVM, pinned
Liberica25 Java home and unchanged Kotlin home. Resources, test deadlines,
seeds, fixture inputs and assertions were not reduced. Original warnings and
logs remain retained; no retry is part of this result.

ONE [XML auditor](../tools/verification/audit-composed250-xml.main.kts) completed
exit0 at03:29:11Z–03:29:16Z. This source is a byte-identical copy of the accepted
executed script, SHA-256
`2c546d28f55d17529dc72811e56ed1d2b8353bf509d3963dfccacf9f103bd488`.
Its preparation-time comment remains historical. It securely streams XML,
rejects external entities/DTD, inventories selected source methods and enforces
full method multisets, source hashes/pathset, ownership, duplicate/omission and
nonpass checks. It is not a mathematical oracle.

| Target | XML/suites | Expected/actual methods | Issues |
| --- | ---: | ---: | ---: |
|JVM |171|574/574|0|
|JS Node |85|383/383|0|
|Wasm Node |85|383/383|0|
|macOS ARM64 |85|383/383|0|
|Total |426|1723/1723|0|

The independent metadata review separately extracted all raw testcase
class/name pairs and XML counters, compared them with the auditor's raw and
normalized tables, and found exact agreement. Actual target filenames are
retained in `xml-audit-output/*.suites.tsv`; no assumed cross-platform filename
convention supplies coverage. `summary.tsv` SHA-256 is
`85c957a690f48b3e318be8085090315013fa34d218bf4af59b3975ef6f4bf592`.

Three590, three591 and three592 methods pass on every target. The six new591/592
methods add24 executions relative to248. The590 controls exercise certificate
sign/cohort/wrong-candidate rejection and public endpoint quotient/pair/remainder
cohorts. The591 controls use an independent bit-vector model and raw-unit
comparison/public primitive endpoints;592 uses bounded independent signed
rationals, requested-scale/endpoints, exact padding and explicit caller/super
result/exception controls. Source-derived loop counts are not separate emitted
per-case ledgers. The existing544 raw comparison assertion also remains active
for nine ordered inputs × nine on each target, without normalizing actual
compareTo output.

## Complete strict sqrt ledger

ONE [ledger auditor](../tools/verification/audit-sqrt-ledger-250.main.kts)
completed exit0 at03:31:40Z–03:31:45Z. Its source is copied byte-identically,
SHA-256 `cdf1f46ffe82840b1942930ae76ec7efd03d34a37732d6668dd21c57ddb5e249`.
Its recorded runtime is Java25.0.4+9-LTS, Kotlin2.4.21,256MiB SerialGC, with a
fresh private compiled-script cache. Its source-change diff from248 changes
only the manifest pin and250-source count; mathematical certificate logic,
strict fields, keysets, schemas and security/provenance guards remain unchanged.
The recorded flags also retain `--enable-native-access=ALL-UNNAMED`,
`--sun-misc-unsafe-memory-access=allow` and the pinned Kotlin2.4.21 home.
Complete runtime/cache/script provenance is `ledger-audit-output/runtime.txt`.

| Target | Expected/seen records | Complete recorders | Issues |
| --- | ---: | ---: | ---: |
|JVM |38497/38497|16/16|0|
|JS Node |18595/18595|12/12|0|
|Wasm Node |18595/18595|12/12|0|
|macOS ARM64 |18595/18595|12/12|0|

Raw XML independently contains94,282 RECORD,52 SCHEMA and52 SUMMARY markers.
All426 XML before/parsed/after hashes match. Every record retains44 schema
fields plus target/XML/line, with strictPass=true, no unexpected exception or
guard failure, and matching required/actual coefficient, scale, precision,
canonical text or error type. Full prescribed keysets, unique keys, expected
recorder multiplicities and computed/producer histograms pass. For changed
inexact candidates the retained checker independently certifies rounding
intervals/decade/tie conditions before comparing sign-equivalent raw proof
tokens. Unchanged references and errors retain their narrower recorded
acceptance scopes; this is not a new full-domain sqrt proof.

| Actual disposition | All-target records |
| --- | ---: |
|UNCHANGED_ERROR |51624|
|UNCHANGED_P_DIGITS |34150|
|UNCHANGED_EXACT |4193|
|LIFT_INEXACT |2473|
|UNCHANGED_ZERO |1632|
|EXISTING_563 |210|

`ledger-audit-output/records.tsv` SHA-256 is
`997e40c39be2a0b4ffb8152690d45d7f1cf5e1f713b11e6c2a01205d2203457a`.
The complete table compares byte-identically with both accepted247 and248
tables, including all original/required/actual fields, raw certificate
magnitudes and path/line metadata. This establishes observed preservation for
all94,282 records, rather than extrapolating from formerly unprinted cases.
The52-recorder summaries hash is
`b5af10056ef1316f2b8e1425bc8f5893e7991694e55b20369f49da4e80bd5fb4`;
target completeness hash is
`22d37080bd853b0e1cc2a9696f096dfa1558956128eff3fa8f34169650e5e5da`.

## Independent reviews, retained evidence and limits

Independent source reviews precede execution and have distinct scopes:

* 590 plus549 normalization: `/private/tmp/krogu-590-549-source-review-GA1gipqY/REVIEW.md`,
SHA-256 `db8e9cf1904007ca001c877cfba931fafa5bffaf59e857e6cff5aa2ba8491f31`.
* 591 source/test/proof: `/private/tmp/krogu-591-source-review-yss6bK8q/REPORT.md`,
SHA-256 `ddec43426897f265ff870d3a7b8babbc4a7cdc39f80544b7101d805fd8dd4ab8`.
* 592 source/test/proof: `/private/tmp/krogu-592-code-review-14F7C2jl/REPORT.md`,
SHA-256 `abe2402ffc6054fc443d5ba1f6edf90834dd9502dceb117c9724610a5cc62db2`.

The later actual-evidence/protocol review is
`/private/tmp/krogu-250-actual-review-pqUH906b/REPORT.md`, SHA-256
`87e33087a638ca83d4967ae545472305d85fb3b3894212c8e4856f00582d6914`.
Its author also authored590 and does not claim an independent590 mathematical
review. Root separately read and accepted that qualified report. Its733-entry
input index is `f934b37bac9f3eccb344df49d942a827b1e1cc897390d767715bc12042686cba`;
its61-entry output index is
`32a80308dd49c35ce4944820c0505bef05d0ea395b31cb9ac7e224229ab226b2`.
All entries verify. Original command/logs, complete XML/output tables and
first metadata mistakes are retained at those immutable workspace paths.

The review first guessed a nonexistent summary.json, corrected a wrong marker
prefix, and used zsh's special path variable during a first metadata index
attempt. The missing/empty derived outcomes are disclosed in the full report;
separate corrected metadata reads verify the original files. No numerical,
auditor, compiler or test rerun occurred during that review. Explicit terminal
calls and sole-key releases were recorded by root; no process state was inferred
by this reviewer.

Giant590 LOW-scale,591 oversized low-bit and592 intermediate-range witnesses
remain symbolic and UNEXECUTED. Bounded helper/public controls do not establish
their runtime/resource feasibility. Incidental internal dispatch observations
and hostile metadata remain separately classified; ordinary truthful values,
explicit caller/super obligations and all old assertions are preserved.
Matching250 hosted/platform/public gates, source593 pow, broader548/549/526 and
official JCK conformance remain outside this local result. Historical247 hosted
evidence remains tied to its own source/JAR.

# Decimal text and square-root integration: local source247 verification

This records the local587/588/589 composition before hosted/public acceptance.
The complete247-source manifest is `source247.sha256`, SHA256
`529cc142d9a42110084155987b3022b36c2309168a159b745bf5191a0359c5a4`.
BigDecimal is5731c8df6ba366a51d907aec97cdef0874df21607638bdcf37b865e85426f741;
unchanged BigInteger is12fd0a14164bfd81b004c66673ae37ea40719bf3552f171ba20b434a296d8602.
Fresh JVM JAR is6e6bc44856543cd79ad3d182d858a31c674e5154873991076ac1732c3c39a421.

## Behavior

Inexact sqrt results retain their required p-digit cohort, including trailing
zeros and the all-nine carry; exact roots retain the closest preferred cohort.
The necessary upper coefficient-range check follows the complete finite-root
prefix. It allows exact small roots at huge requested precision and rejects
necessarily unsupported inexact precision before oversized work. The raw
below-bound fallback remains available. See the587 proof and observation.

Contextual text constructors fully validate syntax/exponent/initial scale,
retain only the required decimal prefix, round in private scratch and perform
the first public coefficient support check after carry normalization. The
proved safe ordinary String route stays unchanged. Contextual and exact
CharArray slices use an owned same-type snapshot, without a whole-input String.
Exact construction uses the same lexer at precision0. No lower production
precision/magnitude cap, OOM translation, field/cache or legacy stream format
is added. The588/589 proofs retain their allocation/toolchain premises.

## Actual local execution

Pinned Kotlin2.4.21, Gradle9.8.1 and Liberica25.0.4+9-LTS were used. One
initial corrected247 Gradle invocation ran jvmJar and the full math-package
JVM/JS/Wasm/macOS ARM selections: exit0,12m27s,49 tasks executed. The original
4GiB/in-process/one-worker configuration and JS Mocha120s budget were retained.
The separate source-derived XML auditor persisted expectations before reading
XML, then passed all exact method/suite multisets and counters:

|Target|Suites/XML|Executed methods|Failure/error/skip/missing/extra/duplicate|
|---|---:|---:|---:|
|JVM|168|565|0|
|JS|82|374|0|
|Wasm|82|374|0|
|macOS ARM|82|374|0|
|Total|414|1687|0|

All247 source hashes/pathset and the fresh JAR binding were verified. The
independent actual method-inventory report is64e3936c4a0a6afdbf15aa418547ffb8e5408cf4d7547610808f643311d9cbc7,
index1e7382405c9d3048c4e75e38cfa424484e75b8ca2d76390a1eb0b8d4ad6aa619.
Twenty new JVM methods/nineteen per other target account77 executions relative
to source240. Inner loops and giant symbolic witnesses are not extra methods.

## Complete retained-input sqrt ledger

Five source consumers retain their original inputs, RNG draws/order, numeric
actions, frozen data and counters. Only the three exact Portable ordinal/full
keys13823/14089/15172 are selected there; subclass/callback partitions remain
separate. Existing563 literal/control policy has priority. Short positive
references are lifted only after an independent exact-square classification
and adjacent-grid rounding certificate; kind/coefficient/scale/precision/
canonical text remain strict. Numerical equality alone cannot pass.

The separately reviewed V4 main.kts auditor passed all94282 actual records:
38497 on JVM and18595 on each other target,52 recorders, zeroissues. It checks
exact unique keys,44-field schema/summary/histograms, raw observations, strict
fields and18592 cross-input/context bindings per target. Public bootstrap
JDK BigInteger primitives independently check the bounded changed candidates.

|Disposition|All four targets|Acceptance scope|
|---|---:|---|
|LIFT_INEXACT|2473|Independent square/adjacent/midpoint/tie certificate and strict p-digit fields|
|UNCHANGED_EXACT|4193|Independent square equality and closest preferred cohort|
|EXISTING_563|210|Original selector/error plus independent exact/cohort checks and original controls|
|UNCHANGED_ZERO|1632|Original zero coefficient, precision and preferred scale|
|UNCHANGED_P_DIGITS|34150|Original strict numerical reference; no newly claimed rounding certificate|
|UNCHANGED_ERROR|51624|Original error category; no all-domain theorem of every error decision|

The current complete JVM frozen/live wide profiles each contain474 certified
LIFT rows. The first six current counterparts match source244's six printed
mismatches byte for byte. The other468 current source-site counterparts are
now fully observed; their old unprinted literal records and aborted tails are
not retroactively invented. The current350 live-decimal keys now complete.

Initial source244 executed554 methods with16 failing methods. An unexecuted policy
revision had a canonical-input parser defect caught before compilation. The
first ledger-auditor source had preflight/lexical defects caught before execution;
V2's actual histogram-parser80142-issue failure and V3's227-issue JS diagnostic
failure are retained. V4 does not rewrite these statuses or raw records.

All227 JS proof differences are nonzero comparison magnitudes only; the other43
fields, signs and decade flags match JVM. [Char.compareTo's public contract](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-char/compare-to.html)
guarantees ordering signs. V4 strictly parses canonical signed Int tokens,
compares sign/zero and exact decade, keeps directed tags exact, and rejects
malformed/wrong-sign/wrong-zero/wrong-decade proofs. Independent numeric checks
still execute first. The entire raw record file is byte-identical to V3,
SHA997e40c39be2a0b4ffb8152690d45d7f1cf5e1f713b11e6c2a01205d2203457a.

The independent mathematical/field actual report is337381009c6c2547e4b09b8fd02cf936c88d66d91f6134b372ab9cb3d6a20340.
The separate full raw-XML/transcript, keyset and historical-binding report is
d6d2672eb0c8954e2269171689f948e5fbb428a79eee424ec5ebb68350c4c7b1,
with76-entry indexe7042a211db73a40fc3001560f653ae140a8dc8e683d987dbbdcd548401ca5b6.
The numerical execution capsule has692 entries, SHA37e34ce6f81094bdf65b458c92027794116aa9fde8e412fa94548a24541c8c8c;
the complete V4 actual evidence index isf313d0940c40c83c5cdadaf5870e3762152ebdb4a9e53b1861341bbbda3f9cc0.
All owned invocations are terminal, with explicit sole-window releases.

The nine unchanged source-phase proof/handoff/input-correction documents
describe their historical preparation revisions. Their SOURCE ONLY and
UNCOMPILED/UNRUN statements record those milestones. This report supersedes
execution-status statements only for the exact source247 composition; the
original source pins, failures, proof premises and limits remain historical.

## Reproduction and limits

The pinned source/XML and complete-ledger auditors are in tools/verification.
Run the full selected tests first, then use a fresh main.kts cache and a NEW
output directory for each auditor. Both accept ROOT, this source manifest and
target=ROOT/build/test-results/TARGET arguments. The ledger auditor additionally
requires its own file path and exact reviewed SHA argument; its frozen SHA is
bd5336de589432386f9f6dbbccf60274c689e860abd16ee57643b377c3f49e1e.
It requires Kotlin2.4.21, Java25.0.4+9-LTS, -Xmx256m and SerialGC. The XML auditor
was executed separately at512MiB. All four targets are required for complete
acceptance; partial output is not a pass. Guarded source changes require a new
reviewed manifest/pin, rather than silently reusing old evidence.

This local record does not assert hosted all-six CI, public-harness execution,
all-input/range feasibility, performance, giant allocation or official JCK/TCK
success. The harness restores the original SquareRootTests.scaleOverflowTest
call, but its actual restored coverage is deliberately not claimed here.
Published source240's public118 retained IDs and13 losses remain historical
until a fresh source247 hosted/public campaign is independently accepted.

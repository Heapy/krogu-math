# Task575 terminal source milestone

**Implemented focused G2+G3 source. ALL UNCOMPILED/UNRUN. No runtime acceptance.**

Baseline/HEAD remains `60aea413e0c7af007844aace6ec28ecdfa019b67`, branch
`task/575-decimal-binary-nonnull`, worktree `/private/tmp/krogu-math-task-575`.
Ref-less task identity is local:575, in_progress, session cb783375; project is active.
Only this worktree was edited. Changes are not committed.

## Implementation and integration

BigDecimal's exact and MathContext overloads of add, subtract, multiply, divide,
divideToIntegralValue, divideAndRemainder and remainder now have all21 input slots
non-null (14 declarations). Existing arithmetic bodies, preferred scales, rounding,
valid-context virtual delegation, quotient/pair/remainder construction and identity
remain intact. Kotlin overrides and dependent helpers/callers have migrated.
The other27 input slots intentionally retain baseline declarations for sibling workers.
No constructor, equals, min/max return, OrNull result, serialization or toolchain changes.

[source.patch](source.patch) is the complete applicable source diff, including five new
active common/JVM test source files. [production.patch](production.patch) isolates the
production change. Apply the source patch during root integration, resolving shared-file
overlap with sibling migrations without dropping either group's assertions.

- Complete source patch SHA-256:
  `441859337d06734b6ea4e9341b26956cde33c4b1ccc782d004950a2bfa88ea39`.
- Production patch SHA-256:
  `6721ca541c59454c6fed88a37e27b6e4eb7a3714529114d3ea34ef860e2893e0`.
- BigDecimal.kt SHA-256:
  `2c2e73056f75e23d32330864c5e8dc0760f7b1ff6fc1f87f9be75dae60c59be8`.
- [source-files.sha256](source-files.sha256) records every one of the24 changed/new
  production/test files. Manifest SHA-256:
  `163bf4c320566916c318ad7a998a56d0ce04d22061f2200618ba09b72a7bdc22`.

## Exact displaced execution accounting

Partitions depend exclusively on input fields. The common tests and JVM counterparts
also assert independently pinned source ordinals in DecimalBinary575Rows. Each JVM
fixture replay prints the original ordinal, complete key, frozen observation, actual
outcome and route/classification. Frozen fixtures and raw observations are not rewritten.

| Family | Original rows/executions | JVM moved | Common retained | Exact ledger |
|---|---:|---:|---:|---|
| ND | 288 | 180 | 108 | [nd-rows.tsv](nd-rows.tsv) |
| Context dispatch | 75728 | 384 | 75344 | [context-dispatch-rows.tsv](context-dispatch-rows.tsv) |
| Context contract rows | 3000 | 96 | 2904 | [context-contract-executions.tsv](context-contract-executions.tsv) |
| Context contract mask executions | 11472 | 240 | 11232 | same ledger, one row per mask |
| Remainder array | 216 | 96 | 120 | [array-rows.tsv](array-rows.tsv) |
| Pair composition | 1440 | 0 | 1440 | original fixture/assertions retained |
| Standalone null executions | 137 | 137 | valid controls retained | [standalone-executions.tsv](standalone-executions.tsv) |

Thus756 fixture rows /900 fixture executions and137 standalone executions have
actual JVM counterparts: **1037 displaced common executions**, none discarded.
ND's36 direct Java override null rows preserve the frozen result/exception/trace;
its144 inherited contextual null rows require a target NPE. The -2 exact selector
has no context argument. Array exact rows likewise retain their unused -1 placeholder
on common targets when their operand is non-null.

Standalone execution keys map exactly to the original common methods:
context19 (six base NPE, twelve direct override, one super), integral12 (ten direct
override, two super), preferred-scale96 (four masks x eight modes x three null slots),
and scale-rounding10 (four direct override, six super). Direct Java overrides preserve
argument, result and exception identity, entry kind and count. The common Kotlin
counterparts retain valid inputs and mathematical assertions; scale-rounding also
gains valid add/multiply direct-override controls.

All pre-existing Java loops remain:

| Existing Java test | Total entries | Base-null contract partition | Other retained entries |
|---|---:|---:|---:|
| DecimalNullOperandDelegationTest | 360 | 180 | 45 direct-null +135 other |
| DecimalContextNullDelegationTest | 90 | 60 | 30 |
| DecimalIntegralNullDelegationTest | 3456 | 2016 | 1440 |

Their exact input keys select the changed partition before comparison. Both host and
own outcomes/traces are printed for selected keys; every remaining comparison is
unchanged. Internal callback timing, malformed replacement arrays competing with
invalid null input, and VM messages are retained diagnostics, not a new host-parity
promise. Valid-input invalid-return/array controls remain active. The six integral-family
entry routes still use real Java explicit super. No reflection is mislabeled as super.

DecimalBinary575BoundaryTest adds ordinary Java rejection coverage for all21 exact
signature/slot combinations, with unlimited and finite context controls. Adapters
pass null directly into Java library calls. They neither cast null into Kotlin types
nor throw their own NPE. Formatting is outside target-exception catches where a
null return could otherwise be mistaken for a library rejection.

## Preservation and independent review

Initial404 tracked files were archived before editing in [initial-tree.tar](initial-tree.tar),
SHA-256 `45825bb9815834d989cd0880bb2a6ef90c91961aec8a75c07ebfb7990376dc47`.
[baseline-tracked.sha256](baseline-tracked.sha256) records all404 original hashes.
Nineteen tracked source files changed; [preserved-tracked.sha256](preserved-tracked.sha256)
and [preservation-check.txt](preservation-check.txt) verify the other385 byte-identical,
including all frozen fixtures, generators, raw diagnostics, accepted544V3/545V2 files,
serialization, build descriptors and existing documentation.

Independent read-only reviewer /root/source_review reviewed production, callers,
adapters, standalone replay, exact ordinals and existing-Java partitions in two passes.
No actionable source issue was found. Review confirmed21 slots,137 standalone
executions, the three Java partitions, true direct/inherited/super routes and ordinal
formulas against preserved fixture source. This is not compiler or runtime approval.

Static checks only: clean baseline identity; source reads; source-ledger extraction;
hash/archive preservation; `git diff --check`; complete patch
`git apply --reverse --check`; independent read-only source review.
No compiler, Kotlin script/helper/certificate, Gradle/toolchain, test, numerical Java
probe, build/JAR, harness, benchmark, allocation or timing execution occurred.
No Git mutation, task transition, unlink, descriptor reset or foreign-worktree edit.
Only own implementation/test source was inspected; no foreign numerical bodies.

Root retains sole numerical/compiler window and must run pinned
Gradle9.8.1/Kotlin2.4.21/Liberica25.0.4+9-LTS verification after aggregate integration:
compiler fixtures, boundary/row replay, retained local targets, current own roundtrip,
fresh own JAR/public tests and exact published-head all6 CI. Legacy serialization
remains excluded. No full compatibility/JCK or aggregate task closure is claimed.

This worker ends immediately after the terminal SOURCE comment for native archival;
it does not wait for a runtime grant. **ALL UNCOMPILED/UNRUN.**

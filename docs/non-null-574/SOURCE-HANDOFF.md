# Task574 comparison source milestone

**G1 source implementation complete. ALL UNCOMPILED/UNRUN. No runtime acceptance.**

The active production and test sources implement this focused group; this is not
another inactive production patch or pending migration inventory. Root owns later
pinned compilation, serial numerical verification, integration, publication and CI.
Changes are not committed. Task574 remains in_progress and linked; no review/done/unlink.

## Scope and implementation

- Worktree: `/private/tmp/krogu-math-task-574`, branch `task/574-comparison-nonnull`.
  Initial/final HEAD: `60aea413e0c7af007844aace6ec28ecdfa019b67`; initial status clean.
- Six public inputs are now non-null: BigInteger and BigDecimal compareTo/min/max.
  Both Comparable type arguments are non-null. Four extrema result annotations,
  three equals inputs, four OrNull results, constructors and other groups are unchanged.
- Production arithmetic/ordering kernels and extrema inequalities are unchanged.
  Integer ties select the argument; decimal ties select the receiver. Common tests
  retain decimal scale-cohort identity and add distinct-equal integer identity across
  all four ordinary/truthful receiver/argument combinations.
- Every own Kotlin compareTo override is migrated. The accepted544V3 arithmetic
  contract changes only its override parameter; its assertions/wrappers/traces remain.
  All545V2 files and frozen544/545 data remain byte-identical.
- Null-bearing common evaluator operands now belong to an explicit JVM partition.
  Evaluators reject accidental invocation with the null sentinel as an invalid test
  route; they do not synthesize a passing numerical result or expected NPE.
- README and the comparison section of COMPATIBILITY describe this source state and
  pending runtime verification. Other groups retain their own baseline documentation.

## Exact coverage accounting

`ComparisonRows574` independently describes all702 six-field keys and zero-based
global fixture ordinals. Common execution asserts exact maps for both partitions.
The selection is input-only (IM/MM with operand `~`), before evaluating any outcome.

| Family | Frozen total | Valid common | Moved JVM base | Moved JVM inherited | Moved total |
|---|---:|---:|---:|---:|---:|
| IM | 498 | 384 | 30 | 84 | 114 |
| MM | 204 | 132 | 12 | 60 | 72 |
| Total | 702 | 516 | 42 | 144 | 186 |

Exact source rows, ordinals, all six inputs, original seventh-field observations,
classification and executable counterpart are in [moved-vector-rows.tsv](moved-vector-rows.tsv).
SHA-256: `b1c5e8dede643c2ac5ec2f00cd4c06e135be70af7c7361ff1ebf7cea70b39a57`.
This ledger was extracted by ordinary CLI text processing, not a Kotlin/helper run.

The four exact ordinal blocks are MM callbacks70839–70958, IM callbacks70959–71126,
IM bases71127–71456 and MM bases71775–71858. JVM execution requires the same exact
moved map, unique ordinals and 30/84/12/60 partition counts. Each real Java call uses
the original receiver text, callback comparison/throw behavior and operation.
The required library outcome is NPE. Frozen selected-null/throw/trace observations,
actual outcome/trace and actual VM message are emitted separately. Internal callback
order and VM wording are diagnostic, not unconditional host-equality requirements.
There is no competing-invalid numeric argument in these six single-input slots.

Two additional standalone common obligations move from ValueIdentityContractTest to
ComparisonNull574JvmTest.commonIdentityNullChecks (BI.ONE and BD.ONE compareTo(null)).
Thus188 common null executions are reassigned in source:186 vector rows plus2 standalone.
The two existing ValueIdentityConformanceTest own null calls remain JVM executions
through direct Java helpers; their two JDK calls and assertions remain.

Existing Java subclass matrices retain all168 integer and120 decimal executions:
84+60 valid rows keep their full parity/identity/trace assertions;84+60 null rows
require NPE and retain host/own observations under their exact loop input keys.
These144 rows already were JVM tests and are not additional displaced common rows.

New ComparisonDispatch574Test separates six direct Java override calls, six virtual
reflection calls, six actual explicit-Java-super calls, and24 ordinary/inherited
direct/reflection boundary calls. Target exceptions alone are unwrapped by reflection;
lookup/access/argument failures become AssertionError. Direct Java override marker
identity and entry counts remain obligations. Explicit super is real Java source,
not reflection. Existing integer erased-Comparable checks and Java sorting survive.

## Source review and preservation

Independent read-only reviewer `comparison_review` reported no actionable defect:
six slots/Comparable, Kotlin callers, four nullable results, tie inequalities,
all ordinal ranges/counts and actual adapters were checked. No reviewer executed
compilers, tests, probes or helpers. `git diff --check` passed. Neither review nor
file hashing proves runtime behavior or generated guard placement.

Before editing, all404 initial tracked files were hashed and archived:

- Archive: `/private/tmp/task574-source-baseline/initial-tree.tar`
  SHA-256 `66978831d1494e7c6047bb8ea5ec7a6e65f8f86f8a4bab84dca5b9b4652215a6`.
- [baseline-tracked.sha256](baseline-tracked.sha256):
  `fba8d73408889ec6593888e3c6803908ef763ce9569005872afff9fbdc4530cb`.
- 26 tracked files intentionally changed (24 source files plus README/COMPATIBILITY).
  All378 other originals pass the [preserved-tracked.sha256](preserved-tracked.sha256)
  check, including every frozen fixture/generator/raw diagnostic, build descriptor,
  current serialization source/test and unowned group. Manifest SHA-256
  `0fd5477b947b953ed136514e9464d6e13dab4cb21a90fd41ec17098baf978530`.
- [changed-source.sha256](changed-source.sha256) records all28 changed/new active
  production/test sources, SHA-256
  `53e193ba9ddd7a41b9537902b4bdac4b1e18b04c15123f4b68b69fb9aa4701da`.
  BigInteger: `0089ac95a27cd8523225641950f75823cee7df0c56533e225c326ce665b34006`.
  BigDecimal: `59de1baca5d17fe16782c7a6c68a292586c345988f6803098e31444ee04fb3e2`.

Initial parent573 terminal preparation2174 and its slot/patch/index artifacts were
read; only G1 was implemented. No files in foreign worktrees/root/runtime/frozen
history were edited. No Git mutation or project descriptor reset was performed.

## Root continuation

The complete tracked/new-file diff is `/private/tmp/task574-source-baseline/source.patch`;
the owned final files are `/private/tmp/task574-source-baseline/source-artifacts.tar`.
Their final hashes are in the terminal ref-less task comment and accompanying
`final-artifacts.sha256` outside the source tree.

Integrate G1 hunks with sibling changes to shared PortableOracleTest, production and
override files; do not replace sibling files wholesale. The other42 inputs remain
intentionally unchanged here. Compile the six positive/negative fixtures separately
under the pinned Kotlin2.4.21 toolchain; each negative requires its argument mismatch,
not an unrelated failure. Verify generated boundary/bridge behavior, exact row replay,
all retained targets, current own roundtrip and selected unchanged public math tests
with Gradle9.8.1/Liberica25.0.4+9-LTS. Exact published-head all6CI and aggregate573/task
closure remain root-owned. No officialJCK/full-compatibility claim is made.

This worker ran only task/source reads, static text inspection, archive/hash/diff
operations and source edits. No compiler, Kotlin script/helper/certificate, Gradle,
toolchain, test, JAR/build, numerical probe, harness, benchmark or allocation/timing
measurement ran. The terminal source milestone ends this worker; no runtime grant
is awaited. **ALL UNCOMPILED/UNRUN.**

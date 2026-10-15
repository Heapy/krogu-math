# Task528 runtime handoff — execution grant1887

SPDX-License-Identifier: Apache-2.0

**Correctness gates and six-fork protocol passed; improvement acceptance is
OPEN because comparison allocation regressed.** Task stays linked/in_progress.
No Git/task transitions, production changes, fixture/assertion changes, source
migration, serializer/legacy changes or inherited callback-count guarantee.
Accepted production/proof/tests/audit/certificate/driver/plan remain frozen.
Earlier PREPARED/UNRUN documents are preserved historical milestones; this
document records their subsequent authorized execution, not final task acceptance.
Root still owns later serial integration and fresh exact-head all-six CI.

Evidence directory: `/private/tmp/task528-execution-v1`. Review/release status is
recorded in the final append-only section after independent follow-up.

## Actual four-target gates

| Target | Command exit | Duration | Actual suites/methods | Failure/error/skip/missing/extra |
|---|---:|---|---|---|
| JVM | 0 | 2m48s | 97/317 | 0/0/0/0/0 |
| JS/Node | 0 | 11m41s | 43/205 | 0/0/0/0/0 |
| Wasm/Node | 0 | 2m23s | 43/205 | 0/0/0/0/0 |
| macOS ARM64 | 0 | 10m24s | 43/205 | 0/0/0/0/0 |

932 actual method executions. All seven new common methods executed on each
target and all three new JVM methods executed, including CURRENT own-format
truthful coefficient resolution, explicit caller dispatch and private full-Long
comparisons. Retained tests/assertions remained active. Each serial target command
has its own trailing `--tests 'io.heapy.krogu.math.*'`. Exact commands are in
`commands.txt`; raw XML is archived in `xml-jvm`, `xml-js`, `xml-wasm`, `xml-mac`.
Each `*-xml-audit*.log` contains every source-derived suite/method and XML SHA.

The first JS metadata auditor exited3 because it expected bare package classnames
where Gradle supplied `jsNodeTest.io.heapy...`. Initial script and failure log are
preserved as `audit-test-results-v1.main.kts` and `js-xml-audit.log`. Parent1900
authorized only exact-target prefix normalization; independent review accepted
v2 SHAa5b9e9dc64c4d2040290d1ffff31e4f0c3895a969eaa3022bbd588ba84543b9a.
All source-inventory/duplicate/out-of-selection/failure/error/skip/omission checks
remain. V2 audited the SAME raw XML; no numerical rerun or XML/test rewrite.
Compile warnings and Gradle macOS filesystem-watch warnings remain in logs.

## Frozen source, fresh builds and runtime origins

Candidate worktree remains at intentional03ebb60a482873acb3fe892f0c86510472f2e0b3
plus accepted528 changes. Root's later sqrt/operator changes were not copied.
Parent baseline snapshot `/private/tmp/krogu-math-task-528-baseline03` matches
archive SHAfe9379f9f32b0206f5944440c97879ef523245f7111abeb48f78ba57d7de31b0.

`frozen-before-measurement.sha256` gives the full ten accepted artifact hashes.
Production BigDecimal SHA92de3354da9545f5b4867ee2ed32e72c91fac4ed32aa5c666f5762bd9f723370;
BigInteger SHAbdbd1a0f6d0ec9571c641b7a34ff73ac969da051c648fa6b606ff775c8739f57.
Driver SHA0e4f59ecc33350eb097d4bfe262a54a53853c2e00078f04275035bca7b2c09c5;
plan SHAedb96d8216d31e80cb8aa3f633dd147d4d59c499a0f55effdd87651bd3a05ae0.
No source changes were needed for compilation/provenance or tests.

Both fresh OWN jvmJar builds ran serially after all four tests/audits terminated,
with identical pins/normalized flags, one worker, --no-daemon, --rerun-tasks,
distinct Kotlin homes and in-process compilation. Both compileKotlinJvm and
jvmJar actually executed. Before/after22-input source/build manifests match
within each variant and differ across variants only in the two owned files.
Full commands/log hashes/artifact binding are in `BUILD_LEDGER.md`.

| Variant | Manifest SHA256 | Fresh saved JAR SHA256 | Build outcome |
|---|---|---|---|
| baseline03 | f241ca37922dcdf7689ee9d2168f6cf579f8be7d4c1ef154255c80e1f75a04ee | 046b24f4888f94802d530114cc1837dbfb868c00c64c50720f4b6e4a07398033 | exit0,7s |
| frozen candidate528 | 460fede0a4a551a922a1c3644b62b1c3b717dc1a9e7879155272e7d3fddcc51e | 25b707ccc19c95dfed1b955afaafe459a79a39857999ea82fc3e8e6739ee718a | exit0,13s |

Saved JARs are `artifacts/baseline03-fresh.jar` and
`artifacts/candidate528-frozen-fresh.jar`. All six logs have two ORIGIN rows,
binding both own runtime classes to exactly that fork's selected JAR and SHA.
No root955/worker562/older JAR was substituted. Each driver checked complete
manifest contents/symlinks, hashes, exact flags, runtime pins, heap and origins
before target-call windows. VM flags SHA845866a2215750569000d742f7c0c8d56c5b41ef0e9abf51e14a3084695bee0f.

## Six actual independent forks

Order B1,C1,C2,B2,B3,C3; B1/C1/B3/C3 forward, C2/B2 reverse. Every invocation
used the identical accepted source, four sizes, signs/orientations/cohorts,
fresh-once/repeated modes, separate warmup fixtures, budgets and repetitions.
No extra warmup, cohort reduction, fastest-fork selection or measurement retry.

| Fork | Variant | Exit | Samples | Measured + warmup calls | Allocation NA | Rows with positive GC delta |
|---|---|---:|---:|---|---:|---:|
| B1 | baseline | 0 | 720 | 66240+5400 | 0 | 67 |
| C1 | candidate | 0 | 720 | 66240+5400 | 0 | 127 |
| C2 | candidate | 0 | 720 | 66240+5400 | 0 | 131 |
| B2 | baseline | 0 | 720 | 66240+5400 | 0 | 61 |
| B3 | baseline | 0 | 720 | 66240+5400 | 0 | 65 |
| C3 | candidate | 0 | 720 | 66240+5400 | 0 | 126 |

4320 raw samples,397440 measured plus32400 warmup calls,429840 total.
All independent expected results, stored per-call results and checksums passed.
The result auditor independently reconstructed complete72-case/two-mode/five-
repetition inventories, per-call budgets, row checksums and DONE checksums.
Supported current-thread allocation counters produced no NA rows; unsupported
handling remains in the driver. All raw GC counts/times/deltas are retained.

Every fork had the reviewed supervisor's configured1200-second timeout including
runner startup/driver compilation. Observation uses100ms polling plus metadata-
call latency, followed by up to10 seconds of cleanup reconciliation; this is not
a hard-real-time enforcement claim or an extended measurement deadline. All
forks completed in approximately5–15 seconds, far below1200 seconds. Each
`<fork>.supervisor.tsv` records command, start/deadline, owned root and captured
descendants with PID/start/lineage, and terminal states. All supervisor exits0,
all captured identities TERMINAL, no unresolved/unidentified PIDs or observer/
logging problems; no timeout or signal/cleanup action occurred. Polling cannot
prove discovery of short-lived escaped descendants; that limitation is recorded,
with the normal launcher/JVM chains captured throughout. No unrelated process
was canceled or matched by name. Own sleep and descendant metadata preflights
passed before measurement; initial rejected supervisor source was retained.

## Balanced results, including regressions

The following values are candidate/baseline ratios of three-fork means of all
per-invocation samples, grouped across both signs and all comparison cohorts/
orientations. Values above1 indicate a regression. They are descriptive results
for this protocol and host, not statistical significance or JMH-grade uncertainty.

| Digits | Operation | Fresh time | Repeated time | Fresh allocated bytes | Repeated allocated bytes |
|---:|---|---:|---:|---:|---:|
| 9 | precision | 0.774 | **1.648** | 0.103 | 0.104 |
| 9 | comparison | 0.823 | 0.391 | 0.225 | 0.223 |
| 128 | precision | 0.028 | 0.018 | 0.043 | 0.043 |
| 128 | comparison | **1.554** | **1.104** | **1.275** | **1.275** |
| 1024 | precision | 0.006 | 0.006 | 0.008 | 0.008 |
| 1024 | comparison | 0.315 | 0.311 | **1.904** | **1.904** |
| 4096 | precision | 0.005 | 0.005 | 0.002 | 0.002 |
| 4096 | comparison | 0.241 | 0.244 | **2.251** | **2.251** |

Across all144 case/mode groups,24 mean time ratios exceed1 and72 allocated-byte
ratios exceed1. These counts describe point estimates, not significant outcomes.
Precision temporary allocation falls strongly, while larger unequal-scale
comparison allocation rises. For example negative4096-digit long-left last-digit
remainder fresh mode uses about11.717MB/call baseline versus30.437MB/call
candidate (2.598x), despite its observed time ratio0.280. Its three paired time
ratios C1/B1,C2/B2,C3/B3 are0.193,0.384,0.333. No allocation win is presumed.

Compact repeated precision mean timing is unstable: baseline fork means
221.395/103.231/216.886ns/call versus candidate653.092/58.203/180.941; the
aggregate1.648 regression remains reported despite two favorable pairs.
128-digit comparison fresh means13314.937/6764.486/6953.778ns/call baseline
versus21574.938/7406.119/13024.316 candidate, so its slowdown is retained too.

Complete144-group details, ALL per-fork means/medians/minima/maxima, allocation
means/NA counts and balanced paired ratios are in `per-fork-case-mode.tsv` and
`balanced-case-mode.tsv`; the full16-row grouped table with actual fork means is
`grouped-by-size-operation.tsv`. Raw ROW logs retain every repetition, allocation
status/reason and GC row.
Report outputs have SHA256 in `measurement-analysis.log`.

Source inspection identifies a reachable multi-word binary divideStored
temporary-remainder allocation path from the new comparison helper. It is not
an allocation profiler proving every byte's cause. The focused OWN-helper
in-place decimal-chunk division proposal in
`DECIMAL_METADATA_ALLOCATION_PROPOSAL.md` gives a bounded quotient/sticky proof
and later required tests. It is UNIMPLEMENTED/UNRUN, awaiting explicit parent
source review; accepted production remains frozen. The current measurement gate
does not establish required comparison allocation improvement.

## Field, host and protocol limits

All runtime FIELD rows are identical across all six logs: BigDecimal instance
fields coefficient/decimalScale/stringCache and BigInteger sign/words remain.
Static/synthetic declarations are also retained in `runtime-declared-fields.tsv`.
No new precision field/cache is a verified structural claim. Declared fields
do not measure offsets, headers, retained bytes or byte-sized object deltas;
no such claim is made. Existing formatting stringCache was not changed.

Pinned runtime BellSoft25.0.4+9-LTS/Kotlin2.4.21, heap<=256MiB, SerialGC and
the complete ordered flags are emitted by every fork. Host macOS26.6.2 arm64,
14 logical processors/36GiB RAM; compressed/swap activity and unrelated Java CPU
use are recorded. All owned tests/builds/audits ended before timing; parent1897
held other source workers code-only without symbolic compilation. Initial ps
sandbox denial and one sysctl field denial are preserved; scoped read-only
escalations succeeded. No available-memory manipulation or uncontended-host
claim. VM counters are historical cumulative values, not measured swap during
each fork.

Fixtures/arrays/checks/output/expectations are outside target-call windows; direct
calls plus common primitive loop/stores/checksum overhead are inside and are not
subtracted. Allocation intervals include nanoTime calls/counter perturbation;
GC intervals are slightly broader context. Fresh-once means new values with
separately warmed code. Five samples and especially2-call4096-digit windows are
small and noisy. Order, startup/JIT/GC and host effects limit timing conclusions.
No System.gc, deleted GC row, significance claim or optimized dead-work claim.

Metadata generator e0cb809b, result auditor25ad6bbb and final supervisor3e112761
received independent static reviews before use. All are own bounded .main.kts,
<=256MiB, with source/flag-keyed caches; no external numeric implementation source
or dependency. Analyzer exited0 after all six forks terminated, preserving all
raw rows and identifying regressions. Existing independent source/proof acceptance
and accepted preparation review1864/1879 remain conditional on remaining gates.

## Final independent follow-up and explicit local-window release

Reviewer /root/numeric_bounds_review independently matched four-target totals,
new/retained methods, fresh build evidence/source manifests/JAR hashes/origins,
six complete raw logs/clean captured terminal traces, all grouped regressions
and the detailed allocation example. Two initial report wording findings were
corrected: configured timeout/polling/reconciliation limits and precise summary-
versus-raw allocation-status locations. Final source-only follow-up found no
remaining actionable finding, verified all ten accepted frozen hashes, and
confirmed the proposal's mathematical quotient/sticky/word bounds. Reviewer
ran no script/library/build/test/probe and edited nothing. It verified the
pre-append runtime report SHA9df9e0786e017506f5d7db30be6542c6781d1095d34dcb7ab12f8f38acce1160
and unchanged proposal SHAc5968c5f2ade38866013481542472b3230f3e2d116585d8993c788b38781bb64.
The final runtime-report hash is recorded separately in the task handoff comment.

Immutable raw evidence manifest `EVIDENCE.sha256` SHA
0c076b6f37f2aec5b83f7686a965736236b9bae6bb1d0027c2f628cc32722d4e
binds helper sources, commands/flags/manifests/build ledger, all logs/traces/raw
samples/summary tables, both saved JARs and all archived XML. shasum -c exited0;
`EVIDENCE-verification.log` preserves the full verification. Accepted ten-file
before/after hash manifests match; whitespace check passed. The test run also
created untracked `kotlin-js-store/wasm/yarn.lock`, left as generated test output;
it is outside the owned production change/integration scope.

**EXPLICIT LOCAL EXECUTION-WINDOW RELEASE: 2026-10-15 00:16:36 UTC.** All owned
tests/builds/audits/metadata scripts/measurement forks/supervisors are terminal,
all captured measurement/preflight identities terminal, and independent review
is finished. No timeout, process signal, unresolved tracking identity or live
owned runtime invocation remains. Root may transfer the sole local numerical
window. Task528 remains linked/in_progress, accepted sources/driver/plan frozen,
with NO Git/task-state transitions or additional numerical work scheduled.

Comparison-allocation acceptance remains OPEN. Parent source authorization,
implementation review and a later sole runtime transfer are required for the
unimplemented proposal; none is implied by this release. Root retains serial
integration and fresh exact-integrated-head all-six CI. Broader corpus/Number/
range/source goals remain open.

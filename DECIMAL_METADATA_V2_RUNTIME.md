# Task528 V2 runtime handoff — grant1935

SPDX-License-Identifier: Apache-2.0

**Four-target correctness gates and the accepted six-fork protocol passed.
V2 reduces measured allocation in all144 case/mode groups; one timing point
regresses and remains recorded.** This is evidence for parent acceptance,
not overall task closure or a statistical speed guarantee. Task stays linked
in_progress. Later root serial integration and fresh exact-integrated-head
all-six CI remain required. Changes are not committed.

All accepted production/tests/proof/auditor/driver/plan/handoffs remain frozen.
No Git/task-state/root mutation, serializer/legacy change, inherited callback-count
guarantee, cache/instance field or arithmetic-kernel expansion. Earlier UNRUN
source/preparation documents are historical milestones; this new report records
their later authorized execution. V1 raw evidence, failures and allocation
regressions are preserved, not replaced by these results.

Evidence directory: `/private/tmp/task528-execution-v2`.
Independent review and explicit window release are appended after completion.

## Actual tests and certificates

| Target | Exit | Duration | Archived suites/methods | Failure/error/skip/missing/extra |
|---|---:|---|---|---|
| JVM | 0 | 1m05s | 98/320 | 0/0/0/0/0 |
| JS/Node | 0 | 8m31s | 44/208 | 0/0/0/0/0 |
| Wasm/Node | 0 | 1m22s | 44/208 | 0/0/0/0/0 |
| macOS ARM64 | 0 | 5m49s | 44/208 | 0/0/0/0/0 |

944 actual method executions. Original7common metadata methods and all3new
DecimalChunkComparisonTest methods ran on each target; original3JVM methods
ran on JVM, including CURRENT own-format truthful-subtype resolution and explicit
caller dispatch. Source-derived complete package inventories retain duplicate,
failure/error/skip/omission/extra checks and narrow exact-target XML prefix
normalization. Metadata auditor e9a56253 independently reviewed before use;
only its explicit3new-common count guard differs from accepted V1 auditor.
No V2 numerical/source/fixture/assertion/parser correction or test rerun was needed.
Compilation and filesystem-watch warnings remain in raw logs.

Each serial target command has its own trailing
`--tests 'io.heapy.krogu.math.*'`; exact commands and exits in commands.txt.
Raw XML in xml-jvm/js/wasm/mac; audit logs list every method and XML hash.
Accepted source auditor4c4281fb and unchanged certificatebf457b19 ran with
fresh source-keyed caches and <=256MiB heap, both terminal0; certificate checked
784 symbolic digit/Long-scale endpoints without executing giant port witnesses.

## Frozen source and fresh matched artifacts

Intentional base03ebb60a482873acb3fe892f0c86510472f2e0b3 plus accepted528
changes; root96ddbf7/operator/sqrt/pow sources were not imported.
Baseline03 parent archiveSHA
fe9379f9f32b0206f5944440c97879ef523245f7111abeb48f78ba57d7de31b0.

Current production:
- BigDecimalSHA92de3354da9545f5b4867ee2ed32e72c91fac4ed32aa5c666f5762bd9f723370.
- BigIntegerSHA24cfd50f0dd9edd25590877b23b4c2e72c89ce9fab71f4b75bc0fe11def56a5f.
- SAME newdriver for BOTH variants:
  544584e8b48a55d75d3799f7a300ff547bdcc5fa96ce6d265522f61daeb905da.
- FlagsSHA845866a2215750569000d742f7c0c8d56c5b41ef0e9abf51e14a3084695bee0f.

All14 frozen artifacts verified before tests, before timing and after execution.
Original0e4 driver, planedb, V1 snapshot/archive and all historical evidence remain.
No source changes during grant1935.

Both jvmJar builds occurred serially after all tests/audits terminal; --rerun-tasks
actually executed compileKotlinJvm and jvmJar (3executed tasks each), same
Liberica25.0.4+9-LTS/Kotlin2.4.21/Gradle9.8.1, no daemon, one worker,
Gradle2GiB/UTF8/in-process compilation and distinct Kotlin homes.

| Variant |22-input before=after manifest SHA256|Fresh saved JAR SHA256|Outcome|
|---|---|---|---|
| baseline03 | f241ca37922dcdf7689ee9d2168f6cf579f8be7d4c1ef154255c80e1f75a04ee | 046b24f4888f94802d530114cc1837dbfb868c00c64c50720f4b6e4a07398033 | exit0,8s |
| candidateV2 | a9307cd26f6348d0b7aae5871cf99c4b19496c44f26e66cd466135cb61200201 | f0253f3f138709870e1c8b32da1767a411856ef4dfc285135de522d0f2600613 | exit0,7s |

Fresh unique saved JARs in artifacts/. Baseline hash reproduces V1, but fresh
actual compilation is retained; no borrowed JAR. Cross-variant manifests differ
only at the two owned production files. BUILD_LEDGER.md records build logs/flags.
All12 ORIGIN rows bind both own classes to exact selected saved JAR/SHA; driver
guards checked source contents/manifests, symlinks, JAR/driver/flags/cache identities,
runtime pins and heap before target windows. Six unique fresh caches include
source-manifest/JAR/driver/flags/fork identities.

## All six measurements

Balanced serial order B1,C1,C2,B2,B3,C3; C2/B2 reverse, other four forward.
Unchanged72cases, digits9/128/1024/4096, signs/orientations/cohorts, fresh-once and
repeated modes, two warmup batches on separate objects, five repetitions and
per-size budgets. Fixtures, expected results and output outside target-call
windows; direct own calls and checksum/result stores inside. No extra warmup,
fastest-fork selection, discarded GC rows, protocol change or numerical retry.

| Fork |Exit|Samples|Measured+warmup calls|Allocation NA|Rows with positive GC delta|
|---|---:|---:|---|---:|---:|
| B1 |0|720|66240+5400|0|69|
| C1 |0|720|66240+5400|0|1|
| C2 |0|720|66240+5400|0|0|
| B2 |0|720|66240+5400|0|59|
| B3 |0|720|66240+5400|0|77|
| C3 |0|720|66240+5400|0|1|

4320 raw samples,397440 measured+32400 warmup=429840 total target calls.
All expected outputs/per-call results/checksums passed. Unchanged independently
reviewed metadata analyzer25ad6bbb exited0 and independently reconstructed every
case/mode/repetition, iteration/fresh-object budget, checksum and DONE checksum.
Supported current-thread allocation counters; unsupported NA/reason handling
remains in driver. Raw allocation statuses and GC counts/times/deltas preserved.

Reviewed supervisor3e112761 configured1200-second per-fork deadline includes
runner startup/driver compilation. Observation100ms plus metadata-call latency,
then up to10seconds cleanup reconciliation; no hard-real-time guarantee.
Actual created child chains finished approximately4.3–10.4seconds after START,
well within deadline. All six supervisor/measurement processes exited0; captured
PID/start/lineage identities all TERMINAL, unresolved/unidentified/problems empty,
no timeout/signals/cleanup. Polling cannot prove discovery of escaped short-lived
descendants; ordinary launcher/JVM chains were captured. No process-name matching
or unrelated process/CI cancellation.

B1 initial orchestration check expected CAPTURED_TERMINAL_CLEAN in the trace,
but that marker is in B1.launcher.log (stderr), absent from B1.supervisor.tsv.
It rejected observation despite clean END. Original note/raw/trace retained;
the note's broader 'absent from supervisor output' wording is corrected here
and in B1-observation-review-addendum.txt. Actual END,
all3captured terminal states and720ROW/DONE checked without fork restart.
Initial V1 preservation hashcheck used wrong cwd and exited1 for298missing
relative paths; its log/note retained. Correct-cwd metadata hashcheck exited0.
Neither was a numerical failure or source/helper change.

## Results, including the remaining timing regression

Ratios below are candidate/baseline means of all three forks, weighted by target
calls within each size/operation/mode. Both signs and all comparison cohorts/
orientations included. Descriptive protocol/host point estimates, no statistical
significance or JMH uncertainty claim.

|Digits|Operation|Fresh time|Repeated time|Fresh bytes|Repeated bytes|
|---:|---|---:|---:|---:|---:|
|9|precision|0.374304|0.600912|0.106544|0.107248|
|9|comparison|0.271280|0.447950|0.164926|0.164244|
|128|precision|0.079086|0.038865|0.043221|0.043219|
|128|comparison|0.052019|0.051428|0.058488|0.058431|
|1024|precision|0.006042|0.005927|0.007702|0.007702|
|1024|comparison|0.024666|0.024181|0.009996|0.009996|
|4096|precision|0.005118|0.005539|0.002211|0.002211|
|4096|comparison|0.024549|0.023776|0.002876|0.002876|

0/144 allocated-byte ratios exceed1; 1/144 timing ratios exceeds1.
Remaining point regression: negative9digit equal-trailing-zero comparison,
long-left REPEATED, C/B3.429695. Baseline fork means
445.687500/373.877344/375.976563ns/call; candidate
149.218750/353.516016/3597.607422. Paired ratios
0.334806/0.945540/9.568701; allocated-byte ratio0.164141.
The high C3 point is retained, not classified away or rerun; these small budgets
and host/JIT/GC variability do not establish statistical significance.

All144 complete comparisons/paired ratios in balanced-case-mode.tsv; full
fork mean/median/min/max, byte means and NA counts in per-fork-case-mode.tsv.
Raw status/reasons and GC remain in fork logs; all16 grouped rows in
grouped-by-size-operation.tsv. No overhead subtraction.
V1 comparison allocation regressions ~1.275x/1.904x/2.251x at128/1024/4096
remain historical. V2 observed corresponding comparison ratios ~0.0585/0.0100/
0.00288 versus freshly measured03 baseline under the same accepted protocol.
This addresses the observed V1 allocation problem for bounded cohorts;
it is not a claim of every-input runtime complexity or universal speedup.

Runtime FIELD declarations agree across all six runs and no new production
instance/cache fields exist. Declared-field evidence is not retained-object bytes,
object layout, cache memory measurement or total peak heap. Allocation values
measure current-thread temporary allocations during call windows; new scratch
is unretained and input storage is not mutated. No mutable-cache race introduced.

## Host and acceptance limits

macOS26.6.2(25G83), aarch64,14logical CPUs,36GiB RAM, BellSoft25.0.4+9-LTS,
Kotlin2.4.21, normalized256MiB/SerialGC child flags. Host snapshots before/after
timing and during JS/native builds preserved. Parent held all local numerical/
compiler/helper work; that coordination does not prove an uncontended host.
Before-timing snapshot shows WindowServer4.6% and loginwindow19.4% CPU, other
apps/services active; unrelated Java47622 was0% at that snapshot, unlike heavy
activity seen in V1. No unrelated process was stopped.

VM snapshots show substantial compressed memory and historical paging/swap.
Across the broader before/after snapshot interval swapins rose74329674->74330313,
swapouts stayed91541216; not attributed specifically to a measured fork.
Snapshots/cumulative counters cannot certify CPU-frequency, scheduling or
absence of interference inside target windows. Small per-case samples and JIT/
allocation/GC variation limit timing inference. All GC-active rows retained.

Static full-domain math/proof coverage complements bounded executed tests;
no646456993-digit coefficient was allocated. No public harness/JDK-port campaign
or extra numerical scope. Parent acceptance of improvement and subsequent
composition/all-six exact-head CI remain open; task is not done/archived.

## Final independent review and explicit release

Final append status supersedes the pending acceptance/review/release wording
above. Parent comment1957 independently conditionally accepts the measured
improvement for this protocol; no universal speed/significance claim. Root will
compose only the accepted owned hunks/helpers, preserving its orthogonal sources.
Composition, full retained integration tests and fresh exact-head all-six CI
remain required. Task528 remains linked/in_progress, not archived/done.

Independent read-only follow-up found no remaining actionable issue after the
two narrative corrections. Reviewer verified actual944methods,4320samples,
429840calls, matched fresh provenance,14artifactfreeze, all144allocationratios
below1, retained3.429695x timing regression (including its raw GC-affected sample),
captured terminal identities and deadline/host/memory/statistical/bounded limits.
Reviewer performed no edits/compiler/scripts/numerical execution.
Reviewed pre-status reportSHA17d56a16478bbb901f703df234ce38af7f25ce4915365d362e78ad7797a7f487;
ledgerSHA77a4b723ee279392cc6d8834ba88424ab0cce280ddc107ccfb02341362d23518;
analysisSHA7f932303cbce3269046b1896d3062abb54948e4ac0f8f622a61cc5a500eecce4;
B1addendumSHA0e99a6a58b8c7f1f89cf42f2950ca610f286c3b3dede079b03dea7e6f8096112.

**EXPLICIT ALL-TERMINAL WINDOW RELEASE1935: 2026-10-15 01:47:35 UTC.**
Every owned audit/certificate/manifest/test/build/fork/analyzer invocation is
terminal; all six captured process chains are terminal with no unresolved
identities, observer failures, timeout or cleanup. Independent reviewer terminal.
No own numerical/compiler/script/background invocation remains. Exact terminal
handles in INVOCATION_TERMINAL_LEDGER.txt. Root may take the sole numerical window;
task528 will run no further compiler/scripts/tests/probes/benchmarks without a
later explicit grant. Remaining final hash/tracker/notification operations are
read-only metadata or authorized handoff writes, not a numerical campaign.
No Git/task-state/root mutations. Changes are not committed.


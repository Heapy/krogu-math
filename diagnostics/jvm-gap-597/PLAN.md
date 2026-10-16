# JVM gap attribution 597

Preparation only: no compiler, numerical probe or profile has run. Tracker owner
731e7f2f; source base781d961b973274771999d0805d2238b7b8831511. Runtime waits
for538, then535; refresh to accepted main before freezing artifacts.

## Identical public workloads first

Compile one probe with the unchanged own Workloads.kt and generated JdkWorkloads.kt.
Load exactly that probe bytecode against each Krogu artifact; JDK expressions stay
public black-box calls. Retain original16 deterministic inputs and MathContext34 /
HALF_EVEN. Original priority IDs: decimal/{small,wide,negative-scale,large-scale}/
{divide,precision}. The original precision IDs observe prepared repeatedly visited
objects. They are never replaced by a cold scenario. A256-slot escaped ring and
post-batch checksum follow527; initialization/checks remain outside measurements.
Three independent processes, alternate order, five warmups and five samples,
100ms nominal target /1048576 cap. Like the actual527 Main.kt, calibration
stops at target/2 (50ms), so do not describe every sample as100ms. An extra untimed200ms warmup protects short hot cases.
Keep each process/case and raw count/elapsed/allocation, not only one aggregate.

Record three Krogu artifact eras with the same probe: original527089a414d...,
post533c04f08c4... (30 arithmetic source hashes match accepted parent), and fresh
current main. This separates533 effects from later multiplication/API changes.
The original and post533 JARs are retained in .git/local-evidence/533-measured-jars;
no deleted /tmp path is a dependency. Fresh artifact must be built and source-bound
in the serialized slot. Preserve original527 raw ratios as historical observations.

## Precision cache-equivalence controls

Separately named IDs use first public precision observation on fresh String-parsed
objects, first observation on fresh coefficient+scale-constructed objects, and
construction-plus-precision. Pool construction for first observations is outside
both time and allocation counters, with one precision call per object. Both
implementations receive identical public construction paths/coefficients/scales.
Do not clear private JDK caches. String construction itself may establish metadata;
that is a measured public construction policy, not proof of arithmetic regression.
Warmup code runs before fresh measurement pools; cap pool size explicitly and report
short batches/variance. Public values/cohorts and16 input slots are checked untimed.

## Own-code profiles and overhead

Use installed JDK25 JFR APIs and configuration, never foreign numerical source.
Own Krogu workload only; do not profile JDK implementation bodies. Separate fresh
profile runs from uninstrumented paired timings. Each selected own job runs for a
fixed10s window after warmup, with an equally timed no-JFR control. Record elapsed,
CPU, operations and current-thread allocated bytes. JFR ExecutionSample period10ms
and ObjectAllocationSample throttle300/s are available in installed profile.jfc.
Report CPU sample counts/own leaf and inclusive paths; allocation sample weights
by allocated type and first own callsite are estimates, not exact counts. Aggregate
weights only once per event. Retain unknown/non-own samples and sampling coverage.
ThreadMXBean allocation totals independently bound sampled attribution; profiler
throughput/allocation overhead is reported separately, not subtracted silently.
No-JFR/JFR RSS and sampled heap are process-level/limited observations.

## Decisions after evidence

Map each measured case to an own path and allocation family with confidence and
unattributed fraction. Route compact representation to529, multiplication to532,
text conversion to537 and platform policy to539. New or same-ticket fixes only for
uncovered causes: e.g. wide metadata cache or context-division temporary churn is
a hypothesis until profiles support it. Do not duplicate another owner's source.
Any confirmed arithmetic change needs independent review, relevant all-mode/scale/
zero/range/subclass oracles and four-target regression. Re-run identical expressions
and controls after the fix, with adverse results retained. Report measured/fixed,
routed/pending and hypotheses separately; never promise a ratio to JDK.

## Binding and admission

Exact source/script/probe/JAR/runtime SHA, loaded class origins, pinned BellSoft
25.0.4+9-LTS/Kotlin2.4.21, JVM flags and actual commands are mandatory. Capture
host CPU/load/swap using executable identity only. Child processes are serial,
time-bounded, and only their owned descendants may be stopped. New output folders
must not exist; failed/partial evidence is preserved. No heavy run before direct
slot handoff. Source-only preparation must not be described as measured evidence.

Profile recording stops immediately after timing/CPU/allocation counter reads,
before checksum/CSV formatting or writes. There is a small boundary around the
start/stop counter reads themselves; post-measurement own hashCode calls are not
included in JFR attribution. Recorded input offsets/masks are checked exactly
against operations, including contiguous logical indices across profile blocks.

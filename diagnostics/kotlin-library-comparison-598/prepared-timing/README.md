# 598 exact-core first/warm timing consumer — SOURCE ONLY

UNRESOLVED / UNCOMPILED / UNRUN. No compiler, dependency download, benchmark or numerical peer code was run. Execution requires the actual final accepted539 Krogu publication, actual peer/toolchain resolution, fixture parity and independent source/artifact/method review. This external consumer does not modify root539, its source freeze, tracker, previous598 adapters or correctness builds. No new speedup/adoption threshold is introduced.

The previously prepared JVM and portable builds are correctness-only. This adds the missing runnable timing path for their already specified exact-core intersection and JVM fixed-scale subset, using the accepted539/527 pattern: prepared operands, bounded warmup and sample counts, 64-result escape, untimed full-result/input checks, fresh alternating processes, exact closure SHA, strict inventories, raw per-fork statistics and explicit metric scopes. It does not add a benchmark service, executor framework, approximate decimal adapter or undocumented peer API.

## Work and finite inventories

The common four integer and four decimal fixtures, independent expected literals and portable gate are reused from prepared-portable. TimingUntimed598.kt has precisely the same gate body, with internal fixture visibility and main renamed to verifyPortableUntimed598. The original prepared-portable bytes remain unchanged. Shared existing adapter sources are included directly; foreign numerical source/bytecode is not inspected/copied.

Each fixture has exact add/subtract/multiply and self-multiply. The native parser and toString/expanded observation stay outside arithmetic counters. Native results are kept as actual references; no cross-library String conversion or JDK arithmetic appears inside a peer's numeric expression. The generic adapter call/enum dispatch, loop and ring-write/escape overhead ARE included symmetrically; these are public-adapter workloads, not intrinsic-kernel timings. Ordinary multiply is used, not a renamed parallel route.

| Target | Peers | Workloads per fresh cohort | Children / FIRST, three cohorts | Warm rows, seven each child |
| --- | --- | --- | --- | --- |
| JVM | public BellSoftJDK25.0.4+9, final Krogu, IonsPin0.3.10, KBigNum desktop0.0.20 | (32 exact core+16 text)×4 +8 explicit fixed-scale HalfUp×3 (IonsPin NA)=216 | 648 | 4536 |
| JS Node24.16.0 | final Krogu, IonsPin0.3.10 | (32+16)×2=96 | 288 | 2016 |
| Wasm-JS Node24.16.0 | final Krogu, IonsPin0.3.10 | (32+16)×2=96 | 288 | 2016 |
| Native macOS Arm64 | final Krogu, IonsPin0.3.10 | (32+16)×2=96 | 288 | 2016 |

Every selected peer/workload gets its OWN fresh child in each of three F/R/F cohorts. Children are adjacent by matched workload and peer order; the reverse cohort reverses both work and peer order. These are matched fresh processes on one host/runtime, not the same process or two different-host observations. The runtime flags are identical within a target: JVM512MiB SerialGC/ActiveProcessorCount1; Node512MiB/expose-gc; Native fixed compiled binary with clean LANG/TZ. Hardware/host runtime and actual effective flags still require execution evidence. No claim of identical cross-runtime GC behavior is made.

Fixed-scale IDs use the existing explicit native HalfUp divide(scale) API and the same eight independently specified fixtures. They are numeric-value IDs, not significant-digit contexts or internal-cohort parity. IonsPin fixed-scale is explicit NA; ambiguous significant HALF_EVEN/MC34 remains unverified/NA. Other parsing/formatting/bitwise/division/root/cache workloads and larger input distributions remain in the operation matrix, not silently represented by this core. This preparation is not task598 completion.

Binary input cases record actual native alias identity (binary-native-alias or binary-distinct); self always uses the exact same object. Native parser interning can make binary aliasing differ across peers. Paired output flags such mismatches and they must NOT be described as equivalent physical-alias work. There is no claim that repeated parsing creates a fresh identity. All input strings, both lengths (derivable from retained strings), operation and expected value are retained per child.

## FIRST and warm states

Harness dispatch explicitly accesses only the selected peer through selectedCase598; compiler/module loading may have run dependency initializers earlier, especially in linked JS/Wasm closures. No claim that other dependency modules remained uninitialized is made. The child first initializes the harness/runtime and independent literal metadata, then parses ONLY that peer's operands outside counters. FIRST measures one explicit public arithmetic operation plus the same result-ring escape. The resulting native value and inputs are checked IMMEDIATELY after counters, using independent expected values. No full correctness corpus has executed before this FIRST.

Public parsing/class initialization may itself execute internal arithmetic and initialize immutable caches; black-box preparation cannot prove otherwise. FIRST therefore means **fresh child, first explicit selected public arithmetic call after described input parsing**, NOT pristine VM, first-ever internal multiplication, cold parser, cold operating-system page cache, cold process startup or first formatting. COLD598_SCOPE makes that limitation explicit. Genuine uninitialized internal-cache arithmetic is NA with that reason. Process elapsed from spawn through exit and external time-l include startup, parsing, gates, checks and warm work and cannot be substituted for FIRST latency.

After FIRST has passed its own result/input check, the full untimed gate runs, then inputs/retained FIRST are checked again. JVM verifies the portable literals independently through public JDK and executes all four already reviewed full JVM correctness suites. Portable targets execute the reviewed128 mandatory value/preservation checks plus identity-qualified distinct cases and explicit NA. Any mismatch/exception fails the child; no warm samples run and its FIRST cannot become accepted comparison evidence. The gate makes separate parse calls/cohort preparations, but native interning can overlap actual identities with the retained FIRST operands. FIRST occurs before the gate, and warm state explicitly follows the gate and all observations; no physical-freshness or cache-pristine guarantee is inferred.

Warm work reuses the selected child's FIRST operands AFTER this explicitly observed/gated state. Training v2 must reach at least200ms elapsed; cumulative operations do not terminate it. Each batch remains bounded at1,048,576 operations, with checked cumulative Long accounting; batch size doubles toward20ms up to that per-batch cap. Existing600s owned-child deadline bounds the full process. These are bounded operational settings reused in spirit from the accepted duration/cap method, not evidence of stable JIT by themselves. WARM598 reports actual operation count, duration and selected batch size; actual cap/duration effects must be reported. Seven warm samples use the same count. No automatic repetition changes the corpus until results look favorable.

The 64-slot ring receives actual returned references during each batch and is escaped through the platform hook. After all counters, every touched retained slot (the latest64 for longer batches), the original inputs and retained FIRST result are checked and folded into a value-canonical checksum. This is last-ring consumption, not retention of all transient outputs or a heap ownership proof. Text checks, decimal canonicalization, oracle work, formatting and explicitly defined Int31 character hash folding are outside wall/CPU/allocation brackets. The metadata-only checker has independent exact input/expected tuples and the same specified polynomial checksum; it does not assume platform String.hashCode equality. Reports must preserve the loop/escape overhead rather than subtract an invented fixed cost.

## Metrics and limitations

Wall latency is raw batch elapsed/count. Throughput is derived from the SAME wall bracket, count×1e9/elapsed, not independently measured saturated concurrent throughput. First and warm remain separate. CPU/counter getters occur outside the wall bracket; their observation order means tiny FIRST CPU/allocation values include adjacent metrology overhead. Unsupported counters emit NA. Counter decreases or invalid elapsed readings fail metrology, not mathematical correctness; no timing values are correctness assertions.

- JVM: current-thread CPU and allocated bytes only if the actual ThreadMXBean reports support AND enabled state. Other-thread allocation/CPU is not measured by those deltas. Runtime used-heap snapshots are process-wide/coarse and include prepared inputs and harness, not per-value retained size. The external time-l footer records whole-child peak RSS; JVM internal RSS is NA.
- JS Node: process.cpuUsage user+system microseconds are converted to ns; process.memoryUsage heapUsed is a whole-process V8 heap sample. Allocation is NA. process.resourceUsage maxRSS is KiB converted to bytes and is the high-water mark through that counter snapshot, not through later exit. [Pinned Node24.16.0 process documentation](https://nodejs.org/download/release/v24.16.0/docs/api/process.html#processcpuusagepreviousvalue).
- Wasm Node: process CPU and process maxRSS have the same scope. Wasm heap/allocation is NA; a V8 heap sample is not passed off as owned Wasm graph bytes.
- macOS Native: getrusage SELF user+system is process CPU; ru_maxrss is macOS bytes through the snapshot. Allocation/heap are NA in this runner. No Linux unit assumption is used. The existing task539 macOS process/rusage approach is reused, not numerical implementation code.
- Every platform: `/usr/bin/time -l` is retained verbatim for completed whole-child peak RSS including all correctness/checking/startup/exit. It is labelled for that complete process sequence and cannot rank one object's retained footprint or replace transient-allocation counters. Exact owned graph/retained memory requires separate already-required metric-specific capability/control evidence; currently NA here, not declared impossible forever. There is no object-size inference from RSS or heap deltas.

Host start/end load average, available processors and precise supervisor wall/monotonic windows are retained as contextual snapshots. The .identity PID/start belongs to /usr/bin/time, NOT the JVM/Node/Native runtime descendant. Actual runtime PID/start is unobserved by this controller; executable SHA, exact command/flags and child version/target assertions provide the stated runtime binding. No descendant PID or independently observed effective-flag witness is inferred from IDENTITY598 labels. They do not prove quiet-host isolation or explain a timing difference. Raw per-fork ranges/adverse values must accompany the report; no universal ranking follows from this corpus.

## Future exact preparation and commands

The independent build reuses the reviewed portable isolated Maven repository ledger/GAV validation and three-target source boundaries, adds JVM with benchmark-only KBigNum, and pins Kotlin2.4.21/Node24.16.0/BellSoft25.0.4+9. Compile and source/peer signatures are STILL UNVERIFIED. Existing approved598 dependency/accepted539 workflow applies; no homemade approval boolean.

Supply the SAME final publication/node properties as prepared-portable (kroguRepository, kroguVersion, kroguCommit, publicationLedger, publicationLedgerSha256, nodeExecutable, nodeSha256). Use the root wrapper and fresh owner attempt logs, no auto JDK/Node downloads, and one build target at a time. Example future build from repository root, after539:

```sh
env -i HOME="$HOME" \
  PATH="/Users/yoda/.gradle/nodejs/node-v24.16.0-darwin-arm64/bin:/Users/yoda/.sdkman/candidates/java/25.0.4-librca/bin:/usr/bin:/bin" \
  JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca \
  ./gradlew -p .codex-work/598/prepared-timing --no-daemon --console=plain \
  -PkroguRepository=/ABSOLUTE/FINAL_ISOLATED_REPOSITORY -PkroguVersion=FINAL_VERSION \
  -PkroguCommit=FINAL_40_HEX_COMMIT -PpublicationLedger=/ABSOLUTE/FINAL_LEDGER.json \
  -PpublicationLedgerSha256=FINAL_LEDGER_SHA \
  -PnodeExecutable=/Users/yoda/.gradle/nodejs/node-v24.16.0-darwin-arm64/bin/node \
  -PnodeSha256=1ee75375e33b94fc34b3b19aede049e11dae90efb63b374dc96d6bdace70c4b8 \
  --write-locks stageJvmTiming598
```

Other explicit stages: stageJSTiming598, stageWasmTiming598, stageNativeTiming598. Each stages its compiler-produced closure under build/timing-runtime/{JVM,JS,Wasm,Native} with artifact.properties, complete member SHA manifest, own source SHA and resolved compile-input binary SHA. JVM additionally records exact runtime classpath JARs; Node stages the whole compiler closure, never just its main file; Native preserves executable permission. Main filenames and Gradle API/task names are asserted hypotheses until the first actual compile; preserve any failure and resolve only through public build/API documentation.

Copy/freeze each produced closure into a new task598 owned artifact directory before execution. The external controller must never overlap a build/mutation of those exact files. Binding verification hashes binaries only; it does not inspect foreign implementation or bytecode. Actual dependency locks/resolution/license and source-to-output build logs must also be retained. The fixed source label krogu-final539-PENDING is the unchanged adapter token; the actual accepted40-hex commit and publication ledger SHA in artifact.properties/binding.txt are authoritative and required, not inferred from that token.

The controller has six exact future arguments:

```sh
/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlinr \
  .codex-work/598/prepared-timing/run-timing.main.kts \
  /ABSOLUTE/FROZEN_TIMING_RUNTIME_TREE ACTUAL_MANIFEST_SHA \
  /ABSOLUTE/PINNED_RUNTIME_EXECUTABLE ACTUAL_RUNTIME_EXE_SHA \
  FINAL_40_HEX_KROGU_COMMIT /ABSOLUTE/NEW_TASK598_RUN_OUTPUT
```

For JVM use the pinned BellSoft bin/java; for JS/Wasm use the pinned Node executable; for Native the runtime executable must be the exact frozen manifest member .kexe. The macOS-only controller runs three cohorts and never runs Gradle/downloads. Per child600s, entire target campaign6h; output-size8MiB is a polling stop threshold (not a strict persisted-byte cap). Owned cleanup has a ten-second termination fence and preserves partial logs/primary failures; no foreign process is signalled. All old outputs must remain; a previously existing run directory is rejected.

Once EXECUTED_NOT_YET_ANALYZED exists, a separate future invocation produces strict summaries without overwriting raw:

```sh
/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlinr \
  .codex-work/598/prepared-timing/check-timing.main.kts \
  /ABSOLUTE/COMPLETED_RUN /ABSOLUTE/NEW_SUMMARY_DIRECTORY
```

It checks supervisor PID/start identities, exit/log SHA, exact input/expected tuples and allowed relations, FIRST/result-ring polynomial checksums, exactly one FIRST before a successful gate, strict WARM peer/id/fork/budget/count and seven ordered samples using that one selected count per child; publishes raw first/warm, per-fork median/min/max, derived throughput, available CPU/allocation, whole-child memory and every paired ratio/delta versus Krogu. Mixed observed relations remain explicit, and all adverse rows are retained. No arbitrary threshold, winning-only filter, hidden rerun or statistical claim of independence is added. The checker is metrology/inventory preparation, not a final independent method/report acceptance.

All build outputs, actual counters/variants/interop/Node/JVM/Native runtime behavior, stability/cap effects, artifact/source bindings, memory controls and reports remain pending. This adds the missing execution path for the prepared core; it does not close the broader literal598 operation/target/reproducibility/report requirements.

## Added literal task598 text workloads

The task explicitly includes parsing/formatting. The original arithmetic/fixed-scale inventory is retained unchanged;16 additional text workloads per peer cover both families, four literals and PARSE/FORMAT separately. Integer inputs are0,−7,1073741823 and100000000000000000000. Decimal text inputs are0,1.2,−1.25 and100000000000000000001.25: unique expanded spellings without insignificant trailing fractional zeros. These text inputs are an additive corpus; the original arithmetic decimal scale fixtures remain unchanged.

PARSE measures the existing public native decimal-text constructor/parser and returns its native value; only the immutable source String is prepared. Result formatting and exact expected-string checks occur outside counters. FORMAT parses its native input outside counters and returns the actual native String from integerText/decimalText. These are native APIs on a selected plain-spelling intersection: Krogu/JDK/KBigNum use their existing toString, IonsPin uses its existing toStringExpanded; no claim of equal general scientific/expanded formatting contracts is made outside these literals. It does not format that input before FIRST. Every output must equal the frozen exact expanded spelling; numeric normalization cannot hide a shorter or scientific output. Unexpected mismatches fail the case, never silently qualify a cheaper non-equivalent formatter. All peers' exact API spellings are the already compiled adapter methods, not a new wrapper implementation. These cases do not promise arbitrary JDK decimal cohort/scale equivalence or default scientific-text equivalence.

FIRST is the first explicit selected public operation after declared preparation: PARSE after String-only preparation, FORMAT after native parsing without prior formatting, arithmetic after native parsing. Input parsing may initialize internals, so FORMAT FIRST is not pristine-cache/process cold. Warm FORMAT follows result observations and is explicitly cached/observed repeated format on one immutable value. A fresh-value-pool format workload is not claimed by these IDs. Warm PARSE repeats parsing the same immutable String and consumes native result references. Allocation, loop/adapter/ring and any native format caching stay in scope. No conversion or result checking is moved into the brackets.

Inventory now216 workloads/648 fresh children/4536 warm rows on JVM, and96/288/2016 on each JS/Wasm/Native target. Sixteen newly added cases per peer are checked twice by the small peer-only text preflight in ../prepared-harness/text-only, independently of accepted539/Krogu/performance. Full timing still requires accepted539 and current artifact provenance. Existing arithmetic/fixed-scale adapter/fixture bytes and previous source reviews remain in ../history/timing-before-text-workloads. Text preflight is not full598 correctness or performance acceptance.

Operation selection remains exact add/subtract/multiply/self, explicit fixed-scale HalfUp where equivalent, and now parse/native format on exact plain-spelling intersection. Signed quotient/remainder/bitwise/root/primality are not individually mandated by literal598; their potential extensions remain outside this selected comparison, not falsely labelled unavailable APIs. The semantic matrix preserves actual NA (missing published targets, non-equivalent context/cohort/rounding or undocumented equivalent primitive) separately from operations deliberately not selected. Significant HALF_EVEN ambiguity is unverified, not a measured equivalent.

Peer-only text preflight exact command (after explicit quiet handoff; no Krogu, timing or accepted539 inference):

```sh
env -i HOME="$HOME" PATH="/Users/yoda/.sdkman/candidates/java/25.0.4-librca/bin:/usr/bin:/bin" JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca ./gradlew -p .codex-work/598/prepared-harness/text-only --no-daemon --console=plain -PrunName=text-1 --write-locks runTextOnly
```

Expected96 CHECK_TEXT598 rows (3peers×16IDs×2repetitions), exact input/output, retained-result rechecks and COMPLETE_TEXT598_PEER_ONLY. Failure output is preserved and cannot certify full598 or unsupported peers. The full future runner still checks Krogu on the same selected literals after accepted539.

## Training v2 and actual argv correction

The first completed JVM648-child campaign remains immutable in timing-jvm-1 with its old source snapshot and independent raw/statistics audit. It is not robust steady-warm ranking:565 children reached the old cumulative operation cap before200ms; CPU/allocation-coherent late-series transitions were observed. The minimum200ms schedule is a concrete metrology correction, not a result-based threshold or winning-only rerun. Exactly one full216-workload×3 JVMv2 campaign and the full96×3 portable campaigns are authorized after narrow review; all7 observations, inputs/operations/FIRST brackets/order and adverse results remain unchanged. Actual trends still determine the report's limits;200ms alone does not prove every cache/JIT is steady. Per-batch duration may remain below20ms at its fixed operation cap and is reported honestly.

The preserved initial JS attempt timing-js-1 failed before INPUT/FIRST/TIME: own emitted Kotlin/JS mainWrapper calls main([]), regardless of Node command arguments. A platform-only argument hook now reads actual Node process.argv[2..] for JS/Wasm before the unchanged four-argument/order guards and peer preparation. JVM/Native return their supplied args unchanged. This is setup wiring, not another measured JS campaign or arithmetic change. Original source/manifests and failure outputs are retained under history/before-node-timing-argv-fix. Raw and actual source/artifact manifests always identify their separate harness eras; accepted Krogu library source remains829ce211.

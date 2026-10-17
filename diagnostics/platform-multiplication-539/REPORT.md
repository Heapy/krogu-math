# Platform multiplication 539: measured decisions and residual acceptance

Task539 remains **INCOMPLETE**: JS small-input performance clearance is unresolved. The single monitored local admission produced NO_NUMERIC_RUN; permission for the separately prepared remote diagnostic is pending. No private archive has been exported. Successful correctness, numerical checks or explicit backend refusal do not close that requirement.

## Stable production and measurement eras

Settled production source is 3f0e1fb8f763b6518395535e222c18407112c9aa, based on accepted parent c9415d55fe8d9a743266e75709cf95738011d311. Source manifest has 405 paths, SHA48c4b61794256a738e36af34fe8d1ccc32a897590fa86c7cf7813c24170e5e0a. Compared with aa9, 400 files have exact bytes; three Native-only files change and two common/JVM files have reviewed KDoc/comments only. Four additional documentation files have no executable effect. Final full3267 independently passed2112 tests: JVM702, JS468, Wasm468, Native474, zero failures/errors/skips. These are current actual counts, not historical sums.

Measured eras are never spliced. Initial common calibration and prototypes,07910 public comparators, selected JS/Native policies,2d74 JVM public timings,a207 JVM small guard,aa9 JS/Wasm serial comparisons and3f0 Native retained comparison keep their original source/artifact identities. Historical data applicability is a bounded semantic/engineering judgment, not new measurements or a guaranteed margin on a later artifact.

## Product decisions

| Target | Ordinary multiplication | Explicit parallelMultiply | Decision and scope |
|---|---|---|---|
| JVM | K64 / square leaf16 / whole-square entry384 | Adopt bounded two-worker reused pool for physical minoperand4096 and max<=2*min, exact ordinary classes,CPU>2; nested/busy/ineligible falls back virtually | Current small guard accepted; historical large-route measurements have explicit compiled-semantic carry-forward. |
| JS | Historical K64/16/384 | Direct virtual multiply before physical helpers/backend | Backend declined due investigated small-path cost. Six aa9 small flags remain unresolved even after withdrawal. No universal Node impossibility claim. |
| Wasm | Historical K64/16/384 | Direct virtual multiply before physical helpers/backend | Backend declined. Current small guard has zero frozen triggers; historical511 adverse remains, without causal recovery claim. |
| Native | Physical distinct equal-length magnitudes K32; physical alias/unbalanced K64; leaf16/entry384 | Direct virtual multiply before physical helpers/backend | Nine large candidate dimensions qualified, but investigated small-path risk led to refusal. Candidate engine and all evidence remain. |

Physical array alias is decisive: two BigInteger objects can share magnitude storage (including opposite signs). Selected Native K32 uses both equal word lengths and physically different arrays. Alias/unbalanced routes retain64. JS32/entry128 was a measured but withdrawn candidate. Radix remains base2^30, arithmetic remains original common Kotlin; production never delegates multiplication to java.math or a foreign numeric backend.

## Serial selection evidence

Each initial target has417 kernel configurations,24 medium-square controls,5 cost controls and28 both-API small baseline cases:474 configurations/9954 timed rows per platform in three fresh F/R/F processes with seven samples. Native additionally has75 full-lifecycle parallel configurations (549/11529 total); JVM75 and Node105 include actual same-fork public serial comparators. Old60-case and partial worker runs remain historical and excluded from newer inventories.

Native balanced K32 is supported by active417-grid evidence plus one bounded33-case K16/32/current64 extension (693 rows). K16 is all-three adverse versusK32 at9/11 sizes, mixed512/1024, with no all-three win. K32/K64 active gains and mixed large points are all retained.16/31 are same-school arithmetic controls, not threshold causality. Native square entry stays384 and leaf16: active entry128 evidence was inconsistent across128..383; tiny leaf gains did not establish a blanket change. No global optimum or automatic K8 claim.

JS bounded neighbor likewise completed33/693. K16 had an adverse4096 result, so blanketK16 was rejected. The selected32/128 candidate later failed credible small-path acceptance despite unchanged tiny arithmetic. It was explicitly withdrawn, not silently described as final.

JVM keeps64: active K32 showed adverse384/512/1024/4096, while128 was mixed/median-adverse, not all-three adverse. Allocation and lower-size controls are retained. Wasm keeps64; no unsupported cross-platform transfer is made. Cost probes include Any/ring/boxing overhead and cannot establish isolated instruction latency or a better radix.

## JVM public adoption and resources

Actual2d74 route2(95846) contains45FIRST/1260warm in45fresh processes. All8 actual eligible points meet the frozen latency rule versus the fastest same-fork matched kernel/frame/actual multiply and adjacent-family rule: distinct/square4096/8192/16384, plus ratio2 left8192/right4096 and left16384/right8192. JVM ratio2 left4096/right2048 correctly falls back because physical minimum is2048. Labels are not transferred to Node/Native, whose ratio2 generator uses a different operand orientation.

The frozen rule is median candidate/fastest-serial<=.80 and everypair<=.90, with adjacent measured sizes. It does not invent a serial tuning20/10 gate. JVM consciously accepts processCPU ratios0.989..1.974, approximate all-live-thread allocation delta170..393196B/op and eligible FIRST11.458292..37.651875ms for the explicit reused model. Whole-childRSS187924480..339148800B is not backend-owned/per-op peak. Thread allocation includes observer bookkeeping and may miss terminated threads. FIRST follows untimed operand/JDK preparation, has no matched serial FIRST, and is neither pristine arithmetic coldness nor a first-call speedup/amortization claim.

Pool ownership is per defining classloader: lazy2workers,queue2/shared admission,30s idle expiry, bounded nesting/contention. Accepted failures propagate without arithmetic retry. Ordinary multiply is unchanged. The thin entry fix reduced137B to26B; later compact direct guard gives33B and aa9 adds one inertNOP (34B). These are static facts, not observed JIT decisions. Accepted current a207 small guard has zero15%-and5ns triggers; older four flagged cases and all adverse pairs remain. aa9 instruction graph/cold/pool semantics were independently checked; final comments/Native-only delta preserves executable scope. No new45/1260 or JIT causality is inferred.

## Node public refusal, delivery and final serial execution

Historical worker105 results show genuine warm gains under a reused Node pool, separate startup/rootload and teardown outside rows. Node synchronous scheduling was technically feasible; standalone KLIB publication/linking and four active SHORT/IDLE cases proved self-contained original-Kotlin worker delivery without consumer-root replay or manual sidecars. The independent worker compiler closure is embedded reproducibly; identities remain JS c483d011… and Wasm3043a0ed… . Missing implementation was not used as a refusal reason.

Actual early public candidates and multiple bounded investigations found credible small-path costs. JS fused basecase dispatch and compact direct entry were genuine investigated code changes, not proof of V8 recovery. Final aa9 JS91477 has six flags; ordinary compact cases also participate, so public backend withdrawal alone is not evidence of ordinary parity. Monitored99668 admitted no numerical pairs because external churn made the one bounded20s admission indeterminate:10/15 intervals,1.484–2.528 external cores,16.757s coverage. No instrumentation fault or measured overlap causality was proved; there is no automatic retry.

Wasm's reachable nullable-size lowering was concretely corrected: fresh WAT removed kotlin.Int construction/unboxing in the private wide-size getter. Compact public entry later bypassed helpers, then final public route became direct virtual serial. Current25695 guard has zero triggers but ordinary511distinct remains all-three slower1.112/1.128/1.042 (+38.438/+42.371/+15.111ns), parallel5111.098/1.131/1.061 (+33.682/+43.099/+21.289ns), below frozen15%. Historical stronger adverse rows remain. No isolated V8 allocation or ordinary511 recovery claim.

Final retained public comparisons on aa9 each cover15points,45freshFIRST and1260warm across matched current kernel/frame/actual serial/explicit parallel. JS24308 and Wasm19329 independently pass exact numerical/input/hash/stat/absence checks. Literal parallel calls virtual serial; these are current final public comparisons, not prototype rows or backend gains. JS FIRST5.559–4728.638ms, after-run processRSS312.26–399.97MB; WasmFIRST0.358–172.604ms,RSS207.81–398.48MB,heap/allocationNA. Same serial JS routes vary materially between pairs (16384distinct ratios1.638/.814/1.592;ratio2 .661/.616/.980), with similar CPU spread. This limits performance interpretation; it is not controlled recovery evidence.

Two separately published aa9 declined KLIB consumers actually pass numerical/sign/alias/input/app-init and natural exit checks with no registry/temp resources. This is resource absence, not cleanup of active Workers or small-guard clearance. Historical active033 consumer proofs remain033. Final Native-only changes plus comments justify scoped applicability without relabelling the aa9 linked artifacts.

## Native candidate refusal and final retained public proof

Actual aa9 candidate50930 qualifies all9 eligible large dimensions with per-operation Worker construction/start/join/termination/combine inside the timed route. CPU1.003–2.093, whole-childRSS19.84–153.04MB and eligible FIRST0.983–53.551ms are retained candidate tradeoffs. Latency refusal would be false; resource caps were not invented. The refusal follows investigated small-path risk: one preregistered full28 produced two mixed flags, parallel63distinct1.160/.762/2.391 (+11.256/−26.260/+100.238ns) and ordinary30distinct1.570/1.276/.934 (+7.772/+6.171/−.998ns), with similar CPU behavior. The three preregistered compact30square/alias1023square/activeK32distinct1023 controls were unflagged; old18 flags remain historical.

Own lowering comparison found retained compact arithmetic prefixes and a real extra public save/restore prefix; +100ns causality was not proved. No unsupported microfix or repeat-until-green followed. Production Native flag alone now declines before helpers. Fresh3f0 Releasef014e741… has19 fast public instructions,48B frame,3save/restore pairs and one virtual call, noTLS/getter/field/backend. It is simpler than the historical841 baseline but not literal body identity or measured ordinary parity.

Current11contracts (4public decline/3private engine/4selected) independently pass. Current retained public2087/strict61359 and independent audit a957621d pass45FIRST/1260warm,45inputs,360outside-counter baselineWorker-ID equalities and45NONE_STARTED. FIRST0.023584–61.410292ms; completed-childRSS19.87–21.46MB/footprint13.07–14.66MB; allocationNA/global lastGCheapcoarse. These prove serial execution/numeric/contracts/resource absence, not active Worker cleanup, backend gain or old small recovery. No new small28 was run after the accepted product refusal.

## Protocol, correctness and limits

Small guards use three fresh alternating pairs and seven samples per process; per-case trigger is median ratio>1.15 AND median delta>5ns. This requires investigation, not mathematical zero regression or automatic waiver of mixed flags. Both APIs/cases/pairs remain separate; no averaging away adverse data. Checksum rings have their recorded partial sampling limits; full correctness/result comparisons are separate.

Historical common JS/Wasm/Native hooks were school-only, not external BigInt oracles. JVM kernel comparisons use JDK; worker host uses actual Node BigInt. New JS K16 and later Wasm public oracle checks are additive in those eras only. Cost/public-small controls have their own stated polynomial/REP/full-result witnesses.

Shared M4Max/macOS host is unisolated. Snapshots establish sampled activity, not continuous quiet, attribution or controlled old/new causal speedup. Native K16 heavyJava snapshot was92.96s after the last fork, not evidence of overlap. Runtime resources are caller/process scopes with explicit NA; no RSS-as-owned-perop peak. No exact retained allocation or universal threshold is inferred.

Final full3267 completed in8m59s. Independent9b61ec84 verifies all530 XML files,2112 tests and405 source hashes. JVM supplement51596f4a verifies fresh final JAR e8ba87ac equals producer:106/114 byte-equal entries, exact method instruction listings in eight changed classes,34B public entry. Debug/metadata changes are not executable changes; whole-JAR identity is not claimed. Prior2108 suites remain historical. Lossless raw/provenance archives preserve failed/partial attempts, guard matrices and NO_NUMERIC_RUN. Task539 remains incomplete while JS small clearance is unresolved.

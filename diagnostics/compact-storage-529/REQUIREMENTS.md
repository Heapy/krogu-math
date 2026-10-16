# Task529 current acceptance ledger (nonfinal)

Numeric production:09e6dc36b3dfaae3878dbe6304b9c9dcaa96735d. Candidate HEAD:d6d594f9bb5f8da5ea962e87ee92796135e9f3d9 adds only the independently reviewed matching five-file benchmark capture hook. Accepted baseline:f8565cae98081ff0121ce7a40919d8a4cfd92013. Main JARs baselinef54132036bcd7b0fa4cda473a1576cb715a7356e4fbdb188f0d650d32735397c /candidate64625bcdbba069763d9da0318dfc143ea9c0a5bcd5e99334744495379d371e37.

| Literal requirement / mapped gate | Actual evidence | Current status |
|---|---|---|
| Physical primitive storage in BOTH public types; no mandatory coefficient/magnitude materialization | Reviewed Integer/Decimal source, primitive publication and parser/writer seams; explicit twoInt+tag+nullablewide; current JVM root graphs | PASS source/runtime; no universal allocation-free claim |
| Full Long range, signs, overflow, promotion/demotion, scale/cohort/rounding/division | Endpoint/common fixtures and6776-case JVM division oracle matrix; final unfiltered suite | PASS |
| Current-format serialization, immutable arrays, cycles/alias, required subclass/callback behavior | Current-format discriminator and truthful subtype fixtures; existing original oracle/serialization suites | PASS; no historical/cross-format interoperability promise |
| Existing observable result publication identities | Initial45317 failed12/683 JVMmethods; reviewed09e fix; affected84984 passed120methods; full46319 | PASS with assertions unchanged; public valueOf cache/shortcuts preserved |
| Final unfiltered four targets | logs/full-regression-2-summary.json +rawXML/archive/log; independent-full-regression-2-audit.json |2055 PASS=684JVM+457JS+457Wasm+457macOS;0fail/error/skip;320 original source hashes unchanged |
| Diagnostic-only hook source separation | native-graph-hook-applied.json; five equal baseline/candidate files; full-tested production bytes unchanged | PASS; current manifests323candidate/312baseline, original320 freeze retained |
| Final JVM33 current-source arithmetic/transient allocation/fresh owned graphs |87962, jvm-paired-5-summary.json, independent-jvm-fifth-raw-graph-audit.json | PASS1386TIME/528CHECK×8,33cases;17all-pair faster/1all-pair slower;13pooled adverse retained |
| Final JS21 current-source arithmetic |71641, portable-js-3-summary.json, independent-portable-js-3-raw-audit.json | PASS TIME/CHECK/bindings;9all-pair faster/8slower; all adverse retained;allocationNA |
| JS runtime heap-delta method | All3 final strict checkers exit3 plus earlierfailedmethods/raw | FAILED_UNVERIFIED; not per-value size proof; separate actual owned graph method |
| Final Wasm21 current-source arithmetic/control slopes |40973 terminal0, portable-wasm-2-summary.json; all3checkers0 | Independent PASS; slope coverage not exact graph |
| Final Native21 current-source arithmetic |77132, portable-native-2-summary.json, independent-portable-native-2-raw-audit.json | PASS TIME/CHECK/bindings;12all-pair wins/5adverse retained; GC controls remain FAILED_UNVERIFIED |
| Actual JS/Wasm owned graph small/wide +shared cache |16fresh9641 candidate phases against unchanged baseline, strongclosure/256roots/payloadcontrols; scopedsource invariance after09e | Actual graph PASS with source-era labels; final structural applicability independently accepted via generated-function/type/import/field/mapping equality, initializer difference disclosed; no invented fresh PropertyArray measurement |
| Native retained graph small/wide +shared cache | Matched Debug16phases, exact image Unit proof, typed roots/ref fields/arraypayload; pinnedABI model | PASS debuggraph/serializedpayload +explicit modeled runtime instance extent; excludes allocator/pages/process; Release transfer only if independently bound |
| Native wholeheap GC method |18398 all3strict controls failed; pinnedGC.collect waits completion | FAILED_UNVERIFIED retainedsize; no arbitrary extraGC/no RSS replacement |
| Fresh source/build/artifact/runtime matching | final-release-{baseline,candidate}-source-before/build-summary.json +27files/tree; independent finalJVM54artifact review | PASS current bindings and all-campaign postchecks; canonical index/Git verification pending |
| Canonical report/raw/adverse/reproduction/index/integration | CANONICAL-PACKAGE-CHECKLIST.md | PENDING packaging/independent acceptance/serial integration; no closure claim |

## Source-era separation

Older chronological notes are retained verbatim in acceptance-history-before-final-package.md. They are history, not current pending claims. Original task/feed is task-full-current.json; upstream accepted537 and597 are present in baseline, so no parser-speedup credit is assigned to529.

| Era | Meaning |
|---|---|
| f84c007/b06947f | Initial compact draft/facade fix; focused144; setup/compile failures retained |
| fade6ce | First allocation regression correction; earlier JVM campaign2 retained |
| ef3db6c | Scalar-wide shared-row correction; JVMcampaign3 and early portable/graph evidence remain historical |
|9641a95 | Decimal power/digit reuse correction; focused159 but initialfull45317 failed12identity methods; its JVM/JS/Wasm/Native campaigns and graphs retain original artifacts |
|09e6dc3 | Identity publication correction,120affected focusedPASS and2055fullPASS; final current timing campaigns above |
|d6d594f | Only matched unpublished diagnostic hook, no numerical changes; Native Debug graph evidence and final Release build manifests separately bound |

Shared cache scopes:19 cells /152B logical raw payload are not total heap. JVM array168B/helper16B; observedJS1072B/helper96B andWasm232B/helper64B have original snapshot identities; NativeDebug modeledarray168B/helper24B with actual152payload. Associated helper is not incremental cache-only owner growth. Wide extra fields and every adverse arithmetic case stay explicit in final tables. JVM/portable protocols differ; compare baseline/candidate within each platform, not absolute cross-platform ratios.

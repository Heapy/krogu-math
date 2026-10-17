# Krogu versus Kotlin/KMP big-number libraries — #598

**Final measured report for four targets.** All four v2 campaigns passed independent review for a bounded comparison that explicitly identifies unstable cases. These are finite observations, not evidence of a steady-state or universal ranking. Integration and delivery are recorded separately. Earlier SOURCE ONLY headings in preparation files describe historical snapshots; the execution receipts below establish the current results.

## Pinned libraries and source

Krogu uses accepted `main` commit **829ce21154c7e75f5206eeb173351c391ed21597**, following completion of tasks #529, #537 and #539. The isolated local publication `io.heapy.krogu:krogu-math:0.1.0-598-829ce211` contains 135 files across five publications: root metadata, JVM, JS, Wasm-JS and macOS Arm64. Its [publication ledger](final-publication-829ce211/publication-ledger.json) has SHA256 `4a424029142eb791357a8bb222fca52f836f67956dee6c36289d448a580e8d23`. The newly published JVM JAR is byte-identical to the accepted JAR, `e8ba87ac09532760f64bd226c7b00aea40b76d8c0b7ae21ddade521570b4517a`. This publication serves local consumers; it is not a Maven Central release.

| Measured target | Krogu | Kotlin alternatives | Reference and limitations |
|---|---|---|---|
| JVM | final829 | IonsPin bignum 0.3.10 and KBigNum desktop 0.0.20 | Public BellSoft JDK 25.0.4+9; all four APIs passed executed correctness checks |
| JS / Node 24.16.0 | final829 | IonsPin 0.3.10 | JDK unavailable; the pinned KBigNum artifact has no JS variant |
| Wasm-JS / Node 24.16.0 | final829 | IonsPin 0.3.10 | JDK unavailable; KBigNum has no Wasm variant |
| macOS Arm64 Native | final829 | IonsPin 0.3.10 | KBigNum's iOS variant is not a macOS runtime; other Native targets were not measured |

Both Kotlin alternatives declare Apache-2.0 in their pinned public POMs. Exact coordinates, licenses, public README/API/variant links and Kotlin metadata are collected in [peer-selection-preparation.md](peer-selection-preparation.md). Kt-math has a JS variant but was not selected: its callable public API and semantics were not qualified by an adapter. This does not establish that other peers are unavailable or that a second JS peer is technically impossible. No third-party numerical implementation source or bytecode was inspected; execution uses public APIs, binary hashes and Gradle resolution metadata. The toolchain is Kotlin 2.4.21, Gradle 9.8.1 and BellSoft 25.0.4+9-LTS. Hardware and OS were observed between JS completion and Wasm startup: Apple M4 Max, 14 physical/logical cores, 38,654,705,664 bytes of physical RAM, macOS 26.6.2 / Darwin 25.6.0 / arm64 ([host-environment-598.json](host-environment-598.json)). RAM capacity does not establish free memory during benchmark windows. The Kotlin stdlib family was pinned to 2.4.21 after the preserved dependency-conflict preflight.

## Operations and executed correctness checks

Selected workloads cover exact integer and decimal addition, subtraction, multiplication and self-alias squaring, plus native parsing and formatting on four exact plain-text literals per family. JVM adds eight explicit fixed-scale HalfUp fixtures for Krogu, JDK and KBigNum; IonsPin is not replaced with divide-then-round. The arithmetic scale fixtures remain intact, with text fixtures added separately. The current [56-workload inventory](actual-workload-inputs.md) and [1,512 per-child inputs](actual-workload-inputs.tsv) retain exact strings, both lengths and signs, integer bit lengths or decimal coefficient digits and scale, expected values and observed relations. These are the executed v2 inputs. The historical [semantic matrix](operation-semantic-matrix.md) distinguishes exact values, cohort/scale, significant precision, rounding and unavailable or unqualified APIs. Signed quotient, bitwise and root operations were not selected; they are not claimed to be absent from the APIs. Fixed-scale arithmetic does not substitute for MC34/HALF_EVEN; no common peer-equivalent MC34 workload was qualified.

| Executed correctness | Arithmetic / fixed-scale | Additional exact-text checks | Explicit NA | Scope |
|---|---:|---:|---:|---|
| JVM | 662: JDK 168, Krogu 168, Ions 159, KBig 167 | 128: 32 per peer | 7 | Larger 16+16-fixture corpus, identity-qualified cases and eight fixed-scale fixtures |
| JS | 128 mandatory + 30 distinct | 64 | 7 | Existing smaller portable subset, not the full JVM corpus |
| Wasm-JS | 128+30 | 64 | 7 | Same subset; the initial sandbox cache-lock failure is retained separately with zero numerical rows |
| macOS Arm64 | 128+30 | 64 | 7 | Executed Release binary; no substitution of an iOS runtime |

All numerical parity checks passed, and text output matched exactly on the selected plain-text subset. Checks cover signs, self-aliasing, actual distinct identity, input preservation and retention of earlier results. Decimal value parity does not establish internal scale/cohort parity; differing text from arithmetic observations is retained. Parser interning is recorded as NA for distinct-object cases, not hidden by cloning inside timing. See the [independent execution audit db549b5d](independent-full-four-target-correctness-and-publication-audit.json) and [owner aggregate receipt](full-correctness-1-actual.json).

## Method and measured states

Each peer/workload has its own fresh child process in three alternating F/R/F cohorts on the same host/runtime. Peers run adjacently for a matched workload; the reverse cohort reverses both workload and peer order. JVM uses 512 MiB, SerialGC and ActiveProcessorCount1. Node uses 512 MiB of V8 old space and `--expose-gc`. Native uses one Release binary with a clean LANG/TZ environment. Flags match between peers on a target, not between different runtimes.

FIRST measures the first explicit selected public operation after the stated preparation: arithmetic and FORMAT follow native parsing, with no prior formatting of that FORMAT operand; PARSE follows preparation of the source String only. Parsing or module initialization may warm internal caches. FIRST is not a pristine VM/cache measurement or process startup. Results and inputs are checked immediately after FIRST, followed by the full untimed gate and seven warm samples at one selected count. Warm FORMAT follows observations and may be cached; PARSE repeatedly invokes the native parser on one immutable String. No separate fresh-value-pool formatting workload is claimed.

V2 training reaches at least 200 ms of elapsed time, including untimed checks; this is not 200 ms of accumulated numerical bracket work. A batch is capped at 1,048,576 operations, so the 20 ms target is not guaranteed at that cap. Actual cumulative operations and durations, checked Long accounting, the 600 s child deadline and the 6 h campaign deadline are retained. Native values and Strings are returned into a 64-slot ring; every touched retained slot and the inputs are checked after counters. Adapter, dispatch, loop and ring costs are included. Throughput is derived from the same wall-time bracket. Timing is not a correctness assertion, and no significance threshold or invented overhead subtraction is applied.

### JVM v2: observed advantages and disadvantages

The complete run contains 216 workloads × 3 cohorts = 648 FIRST and 4,536 warm rows. All 648 training phases reached 200 ms (200.016–283.191 ms). The [independent raw audit c2575ef6](independent-jvm-timing-v2-actual-audit.json) and [case disposition 6d721078](independent-jvm-timing-v2-case-disposition.json) support a **finite comparison after minimum training, with unstable cases explicitly identified**, not a steady-state or universal ranking.

The table gives ranges across three per-fork medians. Wall-time ratios are listed in F/R/F order as **peer ns / Krogu ns** (>1 means the peer was slower in that measured pair). CPU measurements corroborate the direction of these selected examples. Both advantages and disadvantages are shown; no best fork is selected.

| Peer | Workload | Krogu ns/op | Peer ns/op | Krogu derived Mops/s | Wall ratio F/R/F | Alloc B/op Krogu / peer |
|---|---|---:|---:|---:|---|---|
| JDK25 | `int/signed/PARSE` | 18.92–20.54 | 17.16–18.15 | 48.69–52.85 | 0.896 / 0.959 / 0.836 | 96.00–96.00 / 88.00–112.00 |
| JDK25 | `int/signed/self` | 7.09–9.08 | 10.95–14.04 | 110.15–141.08 | 1.547 / 1.701 / 1.478 | 32.00–32.00 / 88.00–88.00 |
| IonsPin0.3.10 | `int/carry30/SUBTRACT` | 5.85–9.25 | 16.93–24.76 | 108.16–170.91 | 2.678 / 2.832 / 2.894 | 32.00–32.00 / 56.00–56.00 |
| IonsPin0.3.10 | `int/carry30/PARSE` | 41.66–69.15 | 238.56–448.49 | 14.46–24.00 | 6.430 / 6.486 / 5.727 | 128.00–128.00 / 1216.00–1216.00 |
| IonsPin0.3.10 | `int/wide10/FORMAT` | 85.93–94.98 | 1062.06–1276.60 | 10.53–11.64 | 12.360 / 13.746 / 12.793 | 296.00–296.00 / 5784.00–5880.00 |
| KBigNum0.0.20 | `int/carry30/PARSE` | 41.66–69.15 | 33.50–58.20 | 14.46–24.00 | 0.842 / 0.842 / 0.804 | 128.00–128.00 / 272.00–272.00 |
| KBigNum0.0.20 | `decimal/signed/ADD` | 6.74–8.81 | 11.52–17.94 | 113.55–148.28 | 1.709 / 1.957 / 2.037 | 40.00–40.00 / 136.00–136.00 |

All 160 peer/workload cases are retained in the disposition JSON, with 480 per-fork pairs in [paired.tsv](timing-jvm-v2-summary/paired.tsv). Absolute medians, minima, maxima, CPU and allocations are in [per-fork.tsv](timing-jvm-v2-summary/per-fork.tsv). Six cases with mismatched observed operand relations are identified separately. Of the remaining 154 cases, 123 have the same direction in all three observations and 31 have mixed directions. **123 is not a count of reliable wins:** some magnitudes remain unstable even when ratio signs agree. For example, IonsPin `int/carry30/MULTIPLY` gives 6.923/7.532/1.127 and KBigNum `decimal/scale/SUBTRACT` gives 1.964/3.626/10.864; such variation cannot be reduced to a stable speedup. JDK `decimal/zero/ADD` changes direction at 1.189/0.077/0.068. All 648 seven-sample traces are retained in [sample-trends.json](independent-jvm-timing-v2-sample-trends.json). CPU-coherent regime changes are observed, but a specific JIT/GC cause is not established. Unstable cases are results with interpretation limits, not hidden failures or peer NA.

### Portable timing

Each portable campaign contains 96 workloads × 3 cohorts = 288 FIRST and 2,016 warm rows. JS v2 completed execution and strict checking; see [execution audit 124b0570](independent-js-timing-v2-actual-audit.json) and [finite disposition 14ad4596](independent-js-timing-v2-case-disposition.json). All 288 children reached 200 ms of training (200.087–246.568 ms). Across 48 Ions/Krogu cases, Ions was slower in all three observations for 38, faster for nine, and mixed for one (`int/zero/self`). These are descriptive directions, not 38 universal wins: three zero binary cases (`ADD/SUBTRACT/MULTIPLY`) have mismatched actual alias relations and do not perform equivalent object-shape work. All 48 cases are retained in [comparisons.json](independent-js-timing-v2-comparisons.json), and all 288 traces in [sample-trends.json](independent-js-timing-v2-sample-trends.json).

| JS workload | Krogu ns/op, range3 medians | Ions ns/op | Ions/Krogu F/R/F |
|---|---:|---:|---|
| `decimal/scale/MULTIPLY` | 404.45–405.14 | 1796.88–1825.43 | 4.443 / 4.450 / 4.506 |
| `int/carry30/SUBTRACT` | 108.10–109.00 | 264.48–268.17 | 2.440 / 2.460 / 2.455 |
| `int/signed/PARSE` | 1750.77–1798.54 | 402.35–412.94 | 0.227 / 0.236 / 0.224 |

Complete absolute latency, ranges and throughput in ops/s (`derived_ops_per_second = 1e9/ns_per_op_median`) for both peers and all three forks are in [JS per-fork.tsv](timing-js-v2-summary/per-fork.tsv); the [FIRST summary](timing-js-v2-summary/first-summary.tsv) is separate. These examples include both advantages and disadvantages at identical input values; process CPU corroborates the selected finite comparisons. Very large cached-FORMAT ratios concern observed repeated formatting of the same immutable value, not general fresh formatting or new distributions. Process heap/RSS is not treated as allocation. Native v2 results follow below.

### Wasm-JS v2

All 288 FIRST and 2,016 warm rows completed. The [execution audit 3375d735](independent-wasm-timing-v2-actual-audit.json) and [disposition eed365df](independent-wasm-timing-v2-case-disposition.json) support finite comparisons, not a universal ranking. All 288 training phases reached 200 ms (200.068–249.524 ms). Across 48 Ions/Krogu cases, Ions was slower in all three forks for 45 and faster for three, with no mixed directions. The three zero binary relation mismatches remain explicit; these counts do not establish 45 reliable general wins. Ratio magnitudes vary between forks and within sample series.

| Wasm workload | Krogu ns/op, range3 medians | Ions ns/op | Ions/Krogu F/R/F |
|---|---:|---:|---|
| `decimal/scale/MULTIPLY` | 16.89–30.37 | 389.88–606.38 | 23.082 / 30.811 / 18.086 |
| `decimal/zero/FORMAT` | 9.54–16.23 | 9.30–16.19 | 0.974 / 0.925 / 0.998 |
| `int/signed/PARSE` | 65.16–67.50 | 107.15–127.71 | 1.892 / 1.616 / 1.645 |

The near-parity `decimal/zero/FORMAT` ratios 0.974/0.925/0.998 do not establish a material win. Complete latency, minima, maxima and derived throughput in ops/s for both peers and each fork are in [Wasm per-fork.tsv](timing-wasm-v2-summary/per-fork.tsv), with a separate [FIRST summary](timing-wasm-v2-summary/first-summary.tsv). All 48 cases and 288 traces are retained in [comparisons](independent-wasm-timing-v2-comparisons.json) and [sample trends](independent-wasm-timing-v2-sample-trends.json). Process CPU was measured; allocation and Wasm heap remain strictly NA, and RSS describes the process. These results neither establish the cause of drift nor transfer a JS/JVM ranking to Wasm.

### macOS Arm64 Native v2

All 288 FIRST and 2,016 warm rows passed the [execution audit aa5430fb](independent-native-timing-v2-actual-audit.json) and [finite disposition bec14d0a](independent-native-timing-v2-case-disposition.json). Training took 200.011–261.192 ms. Across 48 Ions/Krogu cases, Ions was slower in all three forks for 47 and faster for one (`int/zero/PARSE`: 0.526/0.527/0.537), with no mixed directions. Three zero binary physical-relation differences remain explicit. These direction counts describe those 48 finite cases, not universal library performance.

| Native workload | Krogu ns/op, range3 medians | Ions ns/op | Ions/Krogu F/R/F |
|---|---:|---:|---|
| `int/carry30/SUBTRACT` | 13.18–15.20 | 58.73–64.17 | 4.382 / 4.223 / 4.463 |
| `decimal/scale/MULTIPLY` | 14.20–16.37 | 245.61–321.51 | 19.646 / 17.603 / 17.268 |
| `int/zero/PARSE` | 23.42–43.50 | 12.31–23.36 | 0.526 / 0.527 / 0.537 |

Complete ns/op, derived ops/s, minima, maxima and CPU for all peers/forks are in [Native per-fork.tsv](timing-native-v2-summary/per-fork.tsv). All 48 cases and 288 sample traces are retained in [comparisons](independent-native-timing-v2-comparisons.json) and [trends](independent-native-timing-v2-sample-trends.json). For example, cached `decimal/wide10/FORMAT` ratios 2312/1266/1264 vary substantially between forks; neither the cache state nor the magnitude of such a ratio establishes a general fresh-formatting gain. getrusage measures process CPU; allocation and heap are NA. Other Native architectures and operating systems were not measured.

### FIRST and warm results are separate

The table gives the minimum and maximum of three FIRST observations in ns for the same `int/signed/PARSE` workload. The source String is prepared before the bracket, and the native result is checked immediately afterward. This is not process startup or proof of pristine internal caches. The full FIRST tables include every workload and peer, not only fast cases.

| Target | Krogu FIRST ns | Ions FIRST ns | JDK FIRST ns | KBigNum FIRST ns |
|---|---:|---:|---:|---:|
| JVM | 5416209–6359166 | 6285167–8078292 | 3041083–3211917 | 4166875–6222875 |
| JS | 1140250–1202458 | 1167208–1201334 | NA | NA |
| Wasm | 463708–517500 | 1388875–1470958 | NA | NA |
| macOS Arm64 | 14084–21917 | 26625–34541 | NA | NA |

Full FIRST summaries: [JVM](timing-jvm-v2-summary/first-summary.tsv), [JS](timing-js-v2-summary/first-summary.tsv), [Wasm](timing-wasm-v2-summary/first-summary.tsv), [Native](timing-native-v2-summary/first-summary.tsv). Raw FIRST CPU/allocation includes adjacent counter-getter overhead; a tiny FIRST measurement should not be interpreted as pure intrinsic cost.

## Allocation and memory

| Metric | JVM | JS Node | Wasm Node | macOS Native |
|---|---|---|---|---|
| CPU | Current-thread CPU, if supported and enabled | Process user+system | Process user+system | getrusage process user+system |
| Allocation B/op | ThreadMXBean current-thread delta |NA |NA |NA |
| Heap | Coarse process used-heap snapshots | Coarse V8 process heapUsed | NA: JS heap is not the Wasm object graph | NA |
| Peak RSS through snapshot | NA internally | Node maxRSS, KiB→bytes | Node maxRSS, KiB→bytes | macOS ru_maxrss bytes |
| Completed whole-child peak | `/usr/bin/time -l` | Same | Same | Same |
| Owned graph / retained bytes per value | NA: no qualified common method in this runner | NA | NA | NA |

Allocation and RSS are not interchangeable. Whole-child peak includes startup, FIRST, full gates, warm work, checks and exit; it is not the footprint of one operation or value. Heap snapshots are not retained object graphs, and snapshot maxRSS is not the completed lifetime peak. For tiny FIRST measurements, some CPU/allocation getter overhead enters the delta because of the documented counter order. Each `.host` record retains load average and processor count in its child window. Free host RAM was not measured, and host isolation is not established. PID/start belongs to the `/usr/bin/time` supervisor; no independently observed runtime-descendant PID is claimed.

### Observed whole-child memory

The table gives the range and median peak RSS across all fresh children for a peer/target, in MiB (bytes/1048576). It includes startup, FIRST, full gates, warm work, checks and exit. Differences are not per-operation allocation, retained object-graph size or isolated library footprint; the gates also use other peers.

| Target / peer | Children | Peak RSS min / median / max MiB |
|---|---:|---:|
| JVM / ionspin-bignum-0.3.10 | 144 | 163.30 / 214.24 / 231.09 |
| JVM / jdk-bellsoft-25.0.4+9 | 168 | 161.38 / 212.46 / 227.42 |
| JVM / kbignum-0.0.20-desktop | 168 | 163.58 / 215.69 / 225.52 |
| JVM / krogu-final539-PENDING | 168 | 161.70 / 212.38 / 224.48 |
| JS / ionspin-bignum-0.3.10 | 144 | 66.72 / 81.34 / 98.31 |
| JS / krogu-final539-PENDING | 144 | 66.58 / 77.91 / 105.50 |
| Wasm / ionspin-bignum-0.3.10 | 144 | 69.20 / 74.57 / 83.78 |
| Wasm / krogu-final539-PENDING | 144 | 68.31 / 74.55 / 84.20 |
| Native-macOSArm64 / ionspin-bignum-0.3.10 | 144 | 18.06 / 18.22 / 19.20 |
| Native-macOSArm64 / krogu-final539-PENDING | 144 | 18.03 / 18.23 / 18.56 |

The machine-readable [resource summary](actual-resource-summary.json) accompanies the original time-l footer and `.host` record for each raw child. Available host memory is **NA**: 36 GiB of physical capacity does not establish free RAM during measurement. Current-thread JVM allocation is reported separately in `per-fork.tsv`; portable allocation and graph/retained size remain NA.

## Exact actual bindings

| Target | Frozen runtime manifest SHA256 | Own source-map SHA256 | Actual receipt SHA256 |
|---|---|---|---|
| jvm | `647f13bfaa0181cf99c6d9f28f87c9e25384ea5f56db312423729bb64adccdcd` | `cefcafd651ca0bb88d068aa1f12706f9691ad22baf1fe9a6be7178701b18a473` | `8333999cd132ec2d194e0f2de9852da385f4291bd0b8af51296b2b2023c0fae1` |
| js | `d5871785f1056288507273d5402343b6cf31fd5e03b3bd71caa74a0afa9feb66` | `d74497eb93bf97278ddf3c54fb350a590241ced8f84ff5da72858b6bbb1607b7` | `e267411c92caa9c0285b60f0f13b15d2cc464e6766558668cc949445620489b6` |
| wasm | `b9f472acd3a3aa9c83701700f3d96227ddbe2337ffb5c1ff2202add4d05e2270` | `99a6a9db215839c21053f31b1388ea5562e2c22dd6fb6c66c551ea10a579ea49` | `3698104d226bafc40829e9037a47679b2075d3a73425e59cd7c207169bcddfbd` |
| native | `702286f6302253c45ff905f99c8b9d5233d148a498f2356c35c93239a3a04390` | `71bd2266e0da0a7eb3750386187afb52c3df652d05a07439d386576338fd5f17` | `7c9fbf55ca9bf7ebe9e9819d372face50a8916e3b905bdd23a7d2faa02eb9677` |

## History, raw data and reproduction

- The complete initial JVM v1 run is preserved: 648 FIRST/4,536 TIME rows, its [raw receipt](timing-jvm-1-actual.json), raw/statistics audit and [training disposition](independent-jvm-timing-1-training-disposition.json). The cumulative operation cap ended 565 training phases before 200 ms; regime transitions prevent a robust warm ranking. V1 has not been relabelled v2.
- Initial JS attempt `timing-js-1` retains a first-child setup failure: the generated mainWrapper called main([]), producing zero INPUT/FIRST/TIME/gate rows. The [argument fix](node-timing-argv-fix.json) preserves source/failure mappings. The Node argv bridge and minimum-200-ms correction passed [composed review 94884adc](independent-timing-v2-composed-source-review.json). No run was repeated merely to obtain a favourable outcome.
- Original adapters and fixtures, earlier manifests and descriptors, and dependency/stdlib and Wasm cache/runtime/link failures remain in history and the earlier execution receipts. Their counts do not substitute for the complete final consumer.

Exact runnable commands and metric boundaries are in [prepared-timing/README.md](prepared-timing/README.md), immutable runtime trees/source snapshots, and each child's raw `.argv.txt`, `.binding` and `.host` files. The final accepted SHA/GAV/ledger above supersede historical placeholders. See [REPRODUCE.md](REPRODUCE.md) for concrete commands and recovery boundaries. The delivery manifest and independent final review/completion verdicts accompany the tracked package; task #598 is not considered complete until delivery is verified.

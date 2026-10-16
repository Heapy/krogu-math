# Arithmetic baseline: 16 October 2026

This is a current-host baseline from source commit `a809e79be2085b4b8886ccbc846337af0cf918d2`. It contains actual observations, not a speedup or stable regression claim. Later numerical changes need fresh paired measurements; these observations do not certify their performance.

[Raw measurements, commands, environment and manifests](results/2026-10-16-macos-arm64/) and [all-case summary](results/2026-10-16-macos-arm64/summary.csv) are committed. The campaign ran from 12:07:06 through 12:18:03 UTC on Apple M4 Max / macOS ARM64. Environment: BellSoft Liberica **25.0.4+9-LTS**, Kotlin/Native **2.4.21**, Gradle **9.8.1**, Node **26.2.0**; exact flags and paths are in `environment.txt` and per-fork command files. JS/Wasm are production builds and Native is a macOS ARM64 release executable.

The complete command was:

```sh
kotlinr benchmarks/run.main.kts all benchmarks/results/2026-10-16-macos-arm64 3 5 5 100
kotlinr benchmarks/check-results.main.kts benchmarks/results/2026-10-16-macos-arm64
```

Use a new output directory to reproduce; the runner refuses overwrites. [README](README.md) provides pinned installation paths, flags, workload definitions and metric limitations. The shell used the pinned JAVA_HOME and a writable Kotlin script cache in `/private/tmp/krogu-527-kts-cache`; this script/compiler process is outside measured child processes.

## Completion and coverage

All 12 process forks exited successfully: three each for JVM, JS, Wasm and Native. JVM measures both Krogu and public JDK expressions. There are **4,800 measured rows**, 64 workloads per implementation, five measured batches after five warmup batches, and 15 batch means per platform/implementation/workload. Those 15 means come from three independent processes, not 15 independent process replicates. The 320 summary rows preserve median/min/max ns/op and median thread-allocated bytes/op where available.

The validator passed exact fork/case/sample accounting, unique sample IDs, positive observations, reciprocal throughput, allocation/heap validity, per-sample input offset/mask, and stable result digests across measured platforms and JDK/Krogu. All 320 groups covered all **16 logical input slots** over the campaign. Slots are fixed deterministic operands; integer suffixes cycle through nine values, so slot coverage does not imply 16 distinct numeric values. Priming and result hashes are observability/basic differential checks, not full arithmetic proofs.

Source and artifact SHA-256 manifests match before and after the entire campaign (`binding-verified.txt`). Builds and operand preparation, calibration, priming, warmup, hashing and forced heap snapshots are excluded from arithmetic timing. Throughput is the reciprocal of mean batch latency for one named expression, including the entire chain where applicable. No concurrent throughput or individual-operation latency quantiles are inferred.

Operation counts ranged from **2 to 1,048,576**. Actual batch durations are retained: **1,615 / 4,800** batches were shorter than 50 ms, because calibration precedes further warming and fast cases can reach the iteration cap. This limits compiler/thermal stability and confidence for very short cases; no outlier was removed and no confidence or universal ranking claim is made.

## Selected observations

The following JVM cells are medians of 15 batch means. Allocation includes result escape, boxing, ring writes and counter/timer harness costs; it is a current-thread allocation count, not retained bytes per result. Formatting/precision use prepared immutable operands and include warmed caches where the implementation has them.

| JVM expression | Krogu ns/op | JDK ns/op | Krogu allocated B/op | JDK allocated B/op |
| --- | ---: | ---: | ---: | ---: |
| `decimal/small/multiply` | 30.39 | 5.66 | 216.00 | 56.00 |
| `decimal/wide/divide` | 1,291.27 | 101.93 | 8,253.97 | 352.00 |
| `integer/1234/multiply` | 5,704.97 | 1,277.06 | 1,719.88 | 1,639.97 |
| `integer/1234/format` | 305,464.52 | 25,480.65 | 523,304.53 | 39,551.07 |

Separate runtime observations below use the same named workloads. Runtime/compiler and memory definitions differ, and this table does not establish a cross-platform ranking.

| Krogu runtime | small decimal multiply ns/op | wide decimal divide ns/op | 1,234-digit integer format ns/op |
| --- | ---: | ---: | ---: |
| js | 1,956.08 | 80,679.28 | 36,874,375.00 |
| wasm | 93.79 | 2,945.41 | 391,358.07 |
| native | 124.14 | 3,441.10 | 390,022.78 |

## Per-fork conditions and process memory

Peak RSS is measured by macOS `/usr/bin/time -l` in bytes and converted here to MiB. It includes runtime startup, calibration/warmup, all prepared data and GC; it excludes Gradle/compiler processes. The JVM process holds both implementations, so its peak cannot be attributed separately to JDK or Krogu. The one-minute load snapshots do not prove an idle interval. Other applications/processes were active; substantial existing swap remained throughout the campaign. No unrelated process was stopped.

| Fork | process wall s | peak RSS MiB | one-minute load before / after | swap used MiB before / after |
| --- | ---: | ---: | ---: | ---: |
| js-0 | 51.54 | 150.45 | 3.87 / 5.01 | 8456.38 / 8456.38 |
| js-1 | 52.34 | 148.83 | 5.01 / 4.54 | 8456.38 / 8456.38 |
| js-2 | 50.79 | 150.73 | 4.54 / 3.73 | 8456.38 / 8456.38 |
| jvm-0 | 72.39 | 617.77 | 5.78 / 4.59 | 8456.38 / 8456.38 |
| jvm-1 | 71.84 | 592.75 | 4.59 / 3.84 | 8456.38 / 8456.38 |
| jvm-2 | 72.78 | 535.58 | 3.84 / 3.87 | 8456.38 / 8456.38 |
| native-0 | 48.24 | 22.92 | 6.44 / 5.51 | 8448.38 / 8448.38 |
| native-1 | 48.33 | 23.28 | 5.51 / 5.17 | 8448.38 / 8448.38 |
| native-2 | 47.08 | 22.94 | 5.17 / 4.09 | 8448.38 / 8448.38 |
| wasm-0 | 47.03 | 138.81 | 3.73 / 5.43 | 8456.38 / 8456.38 |
| wasm-1 | 45.65 | 137.09 | 5.43 / 6.75 | 8456.38 / 8448.38 |
| wasm-2 | 46.88 | 138.98 | 6.75 / 6.44 | 8448.38 / 8448.38 |

Per-operation allocation is available on JVM only. Total post-GC retained-heap observations span 1,867,808–2,224,840 bytes on JVM, 5,206,168–6,333,360 on JS, and 6,029,312–16,121,856 on Native. These include prepared inputs, runtime state and the live result ring; ring object counts may vary with batch size and implementation. They are not per-operation retained sizes, and their heap definitions differ. Wasm retained heap is `NA` because Node heapUsed excludes the Kotlin/Wasm linear heap; JS/Native/Wasm per-operation allocation counters are also unavailable. Process RSS remains available for all targets.

Twenty-four host snapshots retain load/swap/VM counters and CPU/executable identity only, without foreign process argument lists. Host contention, JIT/GC activity, batching/iteration caps, warmed caches and fixed workload order remain limitations. The raw min/max observations are preserved. There is no absolute CI timing threshold or optimization-adoption conclusion.

## Superseded calibration diagnostics

[The earlier incomplete campaign](results/superseded-256-minimum-2026-10-16-macos-arm64/) remains separate. Source `521e90d` incorrectly forced at least 256 operations, causing roughly 12-second JS formatting batches for a 100 ms request. Its own Node child was stopped with SIGTERM; the runner exited nonzero. All prior raw observations remain, with source/artifact hashes separately checked at stop. It is not passed through the corrected complete-campaign validator or mixed into this baseline.

The independently reviewed correction starts calibration at one operation and primes the observation ring using only 16 input results outside timing. Timed operations always execute; per-sample offset/mask exposes actual timed coverage. The replacement retained every workload, three forks, five warmups and five samples. The corrected four-target smoke passed 320 measurements before this complete campaign.

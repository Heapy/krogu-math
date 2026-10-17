# Standard multiplatform benchmarks

The active suite uses [kotlinx-benchmark 0.5.0](https://github.com/Kotlin/kotlinx-benchmark/tree/v0.5.0), JMH 1.37 on JVM, and the standard synchronous portable runtime on Native, JavaScript and Wasm. Kotlin Toolchain 0.13.0 builds it without Gradle. The independently maintained [ktc-benchmark adapter](https://github.com/Heapy/ktc-benchmark/tree/4731c58cddc20d04aeb1e1a64075c8130f4aeb75) is downloaded at that exact commit, not vendored.

## Bootstrap and run

```sh
./ktc-plugins sync
./ktc-plugins verify
kotlinr tools/configure-native-tests.main.kts
kotlinr benchmarks/generate-jdk.main.kts --check
./kotlin do benchmarkSmoke -m bench-smoke-jvm
./kotlin do benchmarkSmoke -m bench-smoke-js
./kotlin do benchmarkSmoke -m bench-smoke-wasm
./kotlin do benchmarkSmoke -m bench-smoke-macos # Apple Silicon host
```

Before the first Toolchain command on a fresh checkout, run `kotlinr tools/configure-native-tests.main.kts` from the repository root. It derives ignored host-specific Native test friend paths required by Toolchain 0.13.0; it is first-party build configuration, not a vendored plugin. Regenerate it if the checkout or build directory moves. Node 24 must be on PATH. Other matching-host runners are `bench-linux`, `bench-linux-arm64` and `bench-windows`. Native executables run on their target host. No browser or mobile-device runner is configured. The former Gradle benchmark configuration also declared macOS Intel (macosX64); the pinned adapter does not accept that target, so no Intel macOS benchmark runner is claimed. Supporting it requires an upstream adapter update and a newly reviewed lock, not relabelling the ARM64 runner. `benchmarkSmoke` checks execution with one 50 ms warmup and one 50 ms measurement iteration; smoke numbers are not performance evidence. For measurements replace `benchmarkSmoke` with `benchmark`.

For a report-preserving invocation with a fresh output directory:

```sh
kotlinr benchmarks/run.main.kts bench-jvm benchmarks/results/my-standard-run smoke
kotlinr benchmarks/check-results.main.kts benchmarks/results/my-standard-run
```

The normal profile uses five warmups and seven measurement iterations of one second, average time in ns/op, one thread, and three JVM forks. Commands have a six-hour supervisory deadline, including their owned build/run process. These settings do not guarantee stationarity. Native runs one separate process per benchmark/parameter combination; JS and Wasm use the upstream built-in runtime on Node. Engine statistics and isolation differ, so do not compare their scores as equivalent hardware experiments. Raw samples remain in results.json. Throughput derived from ns/op is not a separately measured concurrent workload.

## Inputs and correctness

The standard suite retains the historical 64 workloads: four integer sizes with six operations, and four decimal shapes with ten operations. Each has 16 deterministic rotating inputs. JVM contains matching Krogu and public java.math benchmark classes; all other targets contain Krogu only. Both classes invoke the same Workload abstraction and return the native result to the generated standard harness for consumption. Lambda invocation, cursor rotation and boxing are included equally. No formatting or checksum is added inside the timed method unless formatting is the named workload.

Setup prepares operands and compares all 16 outputs across available implementations before measurement. Portable setup checks repeatability; it is not an independent mathematical oracle. The library correctness suite remains required. The ordinary arithmetic shapes preserve their existing operation semantics, MathContext(34, HALF_EVEN), rounding context and scale choices. Format and precision reuse observed immutable values, so these cases include warmed caches. They are not FIRST-call, cold-format or fresh-allocation measurements. Benchmark state setup intentionally observes results before standard warmup. No result is filtered by its timing.

The JVM and portable parameter names and input expressions are identical. Different implementation object identities and native cache behavior are not asserted equivalent. Production APIs only are invoked; no foreign numerical implementation is copied. The JDK workload mirror is retained from the project's existing public-API expressions.

## Plugins and metric scope

Commit the installer launchers, ktc-plugins.yaml, ktc-plugins.lock.yaml and plugins/.gitignore. Downloaded plugin sources remain ignored. `sync` restores the locked source commit without advancing it; `verify` rejects local drift. Update only through an explicit reviewed installer update. Installer 0.3.0 release launchers verify the downloaded native executable checksum. Its source archives and binary cache live outside the repository.

The adapter currently does not expose configurable JMH profilers. This suite therefore reports standard latency/raw samples, not allocation B/op, retained graph size, CPU/op or process RSS. Their absence must not be reported as zero. Adding a supported profiler is a separate integration change. Historical custom resource observations remain in their original evidence epochs.

The library module is benchmarks/standard; executable runner modules live in benchmarks/runners. The downloaded plugin template supplies KSP harness generation, runtime dependencies and JVM all-open/JMH wiring. Common Kotlin/JDK pins come from common.module-template.yaml. Inspect `./kotlin show settings -m standard` and `./kotlin show commands` for the effective model.

## Historical evidence

Existing results, BASELINE.md and diagnostics are unchanged historical measurements. The former custom runner and documentation are retained under legacy, with the original source corpus under src. They require their original Gradle revision and are not the active build or standard harness. Standard scores are a new measurement protocol and must never be labelled as a rerun or replacement of those raw observations. The detailed #598 comparative study remains under diagnostics/kotlin-library-comparison-598; the baseline suite alone does not reproduce its additional peers or corpus. The peer companion below migrates the warm matched workload cases, while the historical FIRST-call/resource protocol remains separate.

## Active peer comparisons

The additional `benchmarks/peers` library provides an active standard-harness counterpart to the matched #598 value/text corpus. Use runners `peer-jvm`, `peer-js`, `peer-wasm`, `peer-macos`, `peer-linux`, `peer-linux-arm64` or `peer-windows` with the same `benchmarkSmoke` / `benchmark` commands. The report-preserving runner accepts these module names too.

JVM has 216 parameter combinations: Krogu, JDK, Ions bignum 0.3.10 and KBigNum desktop 0.0.20 each cover 32 exact ADD/SUBTRACT/MULTIPLY/self cases and 16 native PARSE/FORMAT cases; Krogu/JDK/KBigNum additionally cover eight explicit fixed-scale HALF_UP cases each. Portable runners have 96 combinations, the same 48 cases for Krogu and Ions. JDK is JVM-specific. The pinned KBigNum dependency selected here is its desktop/JVM artifact; this integration does not claim a portable peer artifact or classify all other KBigNum versions.

Setup uses independent literal expected values and the reviewed own public-API adapters. It checks complete output observations, repeated calls, inputs and retained output before measurement; teardown rechecks inputs and the retained output. Standard harness consumption observes every measured native return value. Format has native String output; parse returns the native number. No output conversion is inserted inside arithmetic or PARSE timing. Decimal arithmetic compares mathematical value independently of textual cohort, and records no claim of matching hidden scale/representation. Text workloads deliberately use the common exact plain-spelling intersection; they do not establish general formatting API equivalence. FORMAT inputs are parsed and observed during setup, so measured formatting includes cached/observed state.

Self cases pass the same object twice. Binary cases use separate parse calls, which may still produce the same interned object for some peers; no physical-distinctness claim is made for those cases. Ions has no explicit fixed-scale HALF_UP mapping in the reviewed adapter, so it is absent from that subset, rather than replaced by significant-digit division. Significant-digit MathContext34 and fixed-scale rounding are not interchangeable. The baseline suite's separate MC34 workloads are not relabelled as matched Ions/KBigNum comparisons.

This is an active warm standard-harness suite, not an assertion that historical #598 FIRST-call/resource results have been migrated unchanged. The historical 216/96 workload IDs and expressions are reused, but process lifecycle, warmup and compiler controls now belong to JMH/kotlinx-benchmark. No new speed ranking is inferred from integration smoke checks. Historical raw results, failures, criteria and immutable publication bindings remain untouched.

### Representative integration checks

Dedicated `bench-smoke-{jvm,js,wasm,macos}` and `peer-smoke-{jvm,js,wasm,macos}` runners select representative arithmetic, PARSE and FORMAT parameters without removing the full library inventory. `fixed-smoke-jvm` additionally checks the explicit fixed-scale HALF_UP route for Krogu, JDK and KBigNum. These configurations use one JVM fork. Use `benchmarkSmoke` with them for short 50 ms integration checks. The ordinary bench-* / peer-* runners retain the full inventories and three JVM forks. The checker verifies each runner's declared selection; full discovery is separately visible in the generated ktc-benchmark-manifest.json.

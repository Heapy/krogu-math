# Task 533 validation and matched diagnostics

No OpenJDK or third-party implementation source was used. Public JDK operations
are the JVM differential oracle. Measurements use two independently loaded Krogu
artifacts, not JDK-versus-Krogu timings.

## Source and artifact identities

Baseline: task 527 artifact, SHA256
`089a414d64ddc7822e0174227689c309603ff28772a189967303b17473dad3aa`.
Its numerical sources are the integrated baseline at `33f5550` (527 adds only the
benchmark module; 536's separate key API does not change BigDecimal).
The candidate production changes only BigDecimal.kt; new tests/docs/script are
separate. `final-measurement/sources.sha256` binds all source/test/build/script
files; `artifacts.sha256` records the exact two JARs. Each CSV also records both
JAR hashes, VM identity, order, actual count/duration and per-sample values. The
script checks origins of all four loaded numerical classes, full result string
representations before timing, and artifact hashes again after timing.

## Reproduction

Use Liberica JDK `25.0.4+9-LTS` (BellSoft), Kotlin script runner 2.4.21, Gradle
9.8.1 and the pinned project dependencies. Build each revision separately with
`./gradlew --no-daemon jvmJar`, preserving the distinct JARs. Then invoke:

```sh
JAVA_HOME=/path/to/liberica-25.0.4 \
KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/krogu-533-kts-cache \
kotlinr scripts/measure-decimal-scaling-533.main.kts \
  /path/to/baseline.jar /path/to/candidate.jar NEW-output.csv forward strip
```

The recorded local invocations used the same installation's compatibility
`/Users/yoda/.sdkman/candidates/kotlin/current/bin/kotlin` entry point; the
Kotlin 2.4.21 reproduction spelling above is `kotlinr`.

Use three fresh processes, in forward/reverse/forward order. The final optional
`strip` argument selects only the six affected strip cases. Omit it to run all
12 cases. Inputs, reflection methods, contexts and sixteen-value corpus are
prepared outside timing. Each batch publishes every result to an escaping ring.
Five warmups and five samples follow adaptive calibration targeting 10 ms or
capping at 131,072 operations; the CSV, rather than the target, states the actual
duration. Allocation is the JVM current-thread allocated-byte counter. Heap peak
is the sum of independently reset heap-pool peaks, an upper bound including the
script, reflection, classloaders and both implementations' live setup, not an
isolated operation residency or whole-process RSS. GC before each sample group
is outside timing. Context/power table initialization is warm, so these data do
not measure cold startup or isolated retained cache size.

## Measurements and limitations

`first-measurement` contains the three complete original ten-case forks. Compact
round/rescale cases reduced allocated bytes: round-small 576→336, round-carry
960→888, rescale-down 472→256, up9 200→96, up18 288→104. The exponent19 fallback
control remains 296→296. Per-fork latency varies (e.g. up9 candidate/baseline
median ratios 0.58/0.85/0.96), so no universal latency multiplier is asserted.
The subsequent production fixes affect only stripping; these older-artifact
figures are explicitly kept as original-candidate evidence, not relabelled as
measurements of the final JAR.

The first wide one-tail strip regressed (2,320 vs 704 B/op); an intermediate
`two-probe-measurement` fixed that input but regressed on the high-v2 two-tail
control (2,296 vs 1,024 B/op). Both adverse campaigns and source/script snapshots
are preserved. They led to the final general remainder-first selector.

`final-measurement` contains three fresh affected-case forks (180 samples),
forward/reverse/forward. Ranges below are the three per-fork medians, in ns/op;
B/op are rounded medians. These are local warmed JVM observations, without a
statistical confidence interval or cross-platform speedup claim.

| Case | Baseline ns/op | Candidate ns/op | Baseline B/op | Candidate B/op |
|---|---:|---:|---:|---:|
| compact strip | 76–85 | 18–20 | 664 | 264 |
| wide 300-zero tail | 16,767–19,061 | 3,233–3,493 | 54,752 | 8,544 |
| wide no tail | 189–198 | 188–202 | 360 | 360 |
| wide odd one-tail | 364–374 | 187–191 | 704 | 360 |
| wide high-v2 one-tail | 354–363 | 324–334 | 704 | 432 |
| wide high-v2 two-tail | 570–581 | 477–500 | 1,024 | 704 |

The six-case final campaign's sampled heap-pool upper bounds range from
82,464,176 to 215,680,104 bytes for baseline and 73,102,768 to 189,464,176 bytes
for candidate. These are measured JVM peaks with the overhead above; the ranges
mix workloads and are not proof of an operation-specific retained-memory saving.
`measurement-summary.csv` preserves per-case/per-fork medians; raw CSV files
remain authoritative. No timing thresholds appear in correctness tests.

## Correctness evidence

`initial-zero-oracle` retains the initial failed test and its documented oracle
correction against the established task 567 zero-round contract.
`focused-before-tail-guard` records the corrected original four-target focused
run (18 passes). `final-focused-jvm` records the final selector JVM run (7
passes), including signed high-v2 tails across scale endpoints. Public-JDK
round/setScale/strip differential, literal carry/half/sticky, unlimited identity,
preferred scale and dispatch controls are separate from measurement outcomes.
The unfiltered final run completed with exit 0 in 10m41s (owned session 21990):
JVM 605, JS 406, Wasm 406 and macOS ARM64 406 tests, 1,823 passes total with no
failures, errors or skips. `full-regression` retains the complete Gradle log,
summary and losslessly compressed original XML files. Command:

```sh
JAVA_HOME=/path/to/liberica-25.0.4 ./gradlew --no-daemon \
  jvmTest jsNodeTest wasmJsNodeTest macosArm64Test --console=plain
```

This candidate branches from `89ad40a`; the later separate task 536 key API and
tests are absent from its corpus. BigDecimal/BigInteger baseline kernels are
identical to current main before integration. Task 532's required rebased full
suite covers the subsequent combined 527+533+536+532 composition. The source
manifest was verified again after the final run. Independent source review and
measurement acceptance by worker `decimal_keys_536` found no open findings.
This is the declared local regression evidence, not a TCK or hosted-gate claim.

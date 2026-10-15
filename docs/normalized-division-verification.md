# Normalized integer division: task 530

BigInteger now uses an independently derived radix-2^30 normalized division
kernel for multi-limb divisors. Private quotient-only, remainder-only and both
wrappers omit unused output storage. Scratch arrays belong to one invocation;
public values remain immutable. The selected consumers include gcd, inverse,
integer square root and decimal arithmetic. Public virtual call boundaries are
preserved where required by the existing API.

The kernel [proof](division530-proof.md) and output-selection
[proof](division530-selection-proof.md) cover estimates and corrections,
signed results, scalar and smaller-dividend paths, aliases, callback boundaries,
and supported-domain index/Long bounds. Conservative allocated-cell bounds do
not guarantee successful allocation or a resident-heap requirement at maximum
size. No speculative maximum-size run was used.

This report supersedes the historical SOURCE ONLY status in the two handoffs.
It does not establish complete mathematical compatibility, official JCK
certification, universal speed improvement or constant-time arithmetic.

## Exact candidate and behavioral gates

The integrated BigInteger SHA-256 is
`d0e757ebd5955604d5fb66d8dda13b1c15b2ecb300a37a5fbcb1fcfaeb65c16f`.
The entire integrated `src/` tree matches the frozen tested candidate byte for
byte. BigDecimal remains `9cbaef67…`; the separate task 568 sqrt-null change
is not included or validated by these gates.

| Target | Suites | Passed method executions |
| --- | ---: | ---: |
| JVM, including Java and migration tests | 126 | 430 |
| JS | 60 | 287 |
| Wasm JS, diagnosed tooling retry | 60 | 287 |
| macOS ARM64 | 60 | 287 |
| Total | — | **1,291** |

Source-derived XML audits found no failures, errors, skips, missing/extra
methods, duplicates or XML anomalies. All 48 explicitly inventoried JVM methods
and 37 common methods per other target passed. These are executions across
targets, not 1,291 distinct definitions.

The first Wasm invocation failed before compilation/tests because its tooling-cache
lock was outside sandbox write permissions. The separately authorized retry used
identical source, tasks and flags. It passed and created a previously absent
`kotlin-js-store/wasm/yarn.lock`, SHA `970a85c5…`. That generated output was
preserved separately; no pre-existing input changed. Therefore the full
inventory was not unchanged throughout the Wasm run. After archiving that
output, the input inventory again matched the frozen manifest `ba08da19…`.
The first failure and retry logs remain separate.

Complete evidence is retained at
`/private/tmp/krogu-530-selection-validation-v2/`, including archived XML,
auditors, logs, frozen source and the generated lock. The full input manifest
SHA is `ba08da194111c8d0ac425e1df2af51b0da7b8d6826632d7e570fad2de4b4c3e1`.

## Fresh matched artifacts

Both baseline and candidate were actually rebuilt with Gradle 9.8.1,
Kotlin 2.4.21 and BellSoft 25.0.4+9-LTS: forced `jvmJar --rerun-tasks`, one worker,
in-process compilation and a 2 GiB Gradle JVM. Builds completed in 28 and 20
seconds. Before/after full input inventories match for each build, with only
BigInteger ff80→d0 differing between variants. All other production, test and
build inputs match. Equality with a historical JAR does not provide freshness;
these actual build logs and inventories do.

| Variant | JAR SHA-256 |
| --- | --- |
| Baseline ff80 | `80ea49801d8de21aafc4ac76488a389363c706e3b1e2d801b8e346660e43fb68` |
| Candidate d0 | `ae120ec9b879185dea719b49d9433448970943eaba2ee57c91500722206e4b2a` |

Artifacts and the 22-field build ledger are retained under
`/private/tmp/krogu-530-matched-pair-v2/`. Ledger SHA-256:
`f9a5737785206dd463f0eac14699a7d45f0b8722e5f55b4cea024c981ff2e90b`.

## Bounded measurement pilot

One campaign completed twelve fresh serial child JVMs in six matched pairs,
alternating BC/CB order. Each child ran 90 signed case-operations with two warmup
calls followed by three samples of two calls. Every returned result matched
public JDK oracle bytes checked outside the measured interval. All 4,320 rows,
6,480 measured calls, 2,160 warmup calls and 540 paired totals were retained.

The measured boundary contains prebound zero-argument Object MethodHandle
calls, their loop and result stores. Setup, oracle work, byte export and result
checks are outside. Wall time, current-thread CPU, cumulative calling-thread
allocated bytes and surrounding GC snapshots are retained. Dispatch/counter
overhead is included; no calibration is subtracted. Allocated bytes are not
live memory, resident heap or whole-process allocation.

The table gives candidate/baseline ratios of summed measured values across
all six pairs, operations and signs within each shape. It is not a mean of
individual ratios. Seed widths in the gcd label precede multiplication by 35.

| Shape | Wall | Thread CPU | Allocated bytes |
| --- | ---: | ---: | ---: |
| One-limb 256/30 | 0.9495 | 0.9951 | 0.6267 |
| Balanced 256/255 | 0.06753 | 0.09860 | 0.005131 |
| Balanced 1024/1023 | 0.01557 | 0.02261 | 0.001534 |
| Balanced 8000/7999 | 0.001543 | 0.002095 | 0.0002194 |
| Unbalanced 256/61 | 0.08364 | 0.1100 | 0.005397 |
| Unbalanced 1024/127 | 0.01958 | 0.02764 | 0.002561 |
| Unbalanced 8000/257 | 0.02811 | 0.03165 | 0.0009977 |
| gcd, seed 256/127 | 0.03826 | 0.04222 | 0.01318 |
| inverse 255/256 | 0.02065 | 0.02444 | 0.02142 |

Allocations decreased in **86 aggregate cases**, tied in the four one-limb
`divideAndRemainder` cases, and increased in none. No allocation pair regressed.
Timing is mixed: **30/540 wall** and **32/540 CPU** pair ratios exceed one;
each timing metric has **6/90 adverse aggregate cases**, all one-limb divide or
both-output calls. In particular, negative/negative one-limb both-output calls
have aggregate wall **1.7214×**, CPU **1.5254×** and unchanged allocation.
Individual adverse remainder pairs also remain present. All cases, ties and
adverse pairs are published in the [summary](../diagnostics/division530/pilot-summary.tsv)
and [paired totals](../diagnostics/division530/pilot-paired-totals.tsv).

This cold finite pilot supports the observed reduction for the intended
multi-limb and consumer cohort. It compares the combined ff80→d0 kernel and
selection changes, not selection alone relative to V1. Two warmups do not
establish steady state. Fixed within-fork order, JIT, first-child compilation,
GC and host load around 10–12 affect interpretation. The 36 GiB host reported
roughly 15 GiB occupied swap. No uncontended-host, statistical-significance,
universal-latency or whole-domain performance claim follows.

The unchanged executed [driver](../tools/measure-division530.main.kts), SHA
`fd7c04af01aac3bb9fa2688fc4ff334831e772a178561d1d5a00a5917e46c218`,
and external [controller](../tools/control-division530.main.kts), SHA
`332b91a1061c867f423b1f38d544c867c40a0015d10da284f9ff93a186f6a5c2`,
are included. Their generic CLI paths must point to separately prepared pinned
artifacts and a filled ledger; they do not build artifacts themselves. Both
require an explicit execution grant. Controller preflight and driver check
were separate successful invocations before the arithmetic campaign.

The campaign budget was 1,200 seconds including driver compilation and shared
cleanup, with 90 seconds per child. All twelve children and the driver were
positively accounted DEAD; controller and parent accounting confirmed terminal
exit, without timeout, cleanup signals or unresolved captured identity.
This proves the observed normal lifecycle, not unexercised timeout cleanup or
absence of unseen short-lived descendants. Controller pre-entry compilation
remains the parent's supervision responsibility.

Raw evidence is retained at `/private/tmp/krogu-530-controller-campaign-v1/`;
the full archive SHA-256 is
`d878acd62218a225995f874614238324d1a50e02444b8f20b4699faea0af79ad`.
Sample SHA: `76760518cc549d8aee9b26874eae408cdb664b2dcd51af26958557402bedfcba`;
paired totals: `f8217ac47c612db5af5878c6390c103816a75fcba9932ac39108c967677c95b1`;
driver log: `b20480a1172eda09bfb8c1f1dc6c6499456cd92a2d330ab39f1d23bf6901de35`.
The checked descriptive aggregator is retained at
`/private/tmp/krogu-530-summarize-v1.main.kts`, SHA `79592fb3…`.

## Public workload and publication

The unchanged public OpenJDK campaign for the exact d0 JAR completed under root
grant 2072: **19 PASS / 1 FAIL / 120 declared SKIP**, complete=true,
officialJck=false. Nine source files supply ten directives per baseline/port
phase. All twenty compilations passed; all ten port core-loading checks and
twelve bytecode audits passed with no host math member references. There was
no OOM or timeout. Both port BigIntegerTest directives completed their original
assertions in **41,632 ms** and **42,003 ms**, within unchanged 120/400-second
limits. All seven selected decimal files passed in both phases.

The sole failure remains PrimeTest's huge-size exception expectation
(`checkHugeFails` for 2^500000001+1), after 26,908 ms, exit 1. The port completed
the public call instead of throwing the expected ArithmeticException. The
test discards its Boolean result: this log does not observe a false positive.
The permitted supported-range difference remains classified separately; it is
not converted to PASS and no artificial cutoff was introduced. The
[earlier report](context-modular-integration-verification.md) retains its own
timeouts and size failure. Prior V1 results validate 590d9391 separately.

This campaign keeps seed 559, subset 0, the 512 MiB heap, original operands,
assertions, timeout directives and import-only adaptation. Seed 559 does not
seed the port's internal Random.Default. The pinned public test tag and runtime
revision mismatch remains disclosed. Declared skips retain lost coverage;
these selected files do not establish full public-suite or official JCK
acceptance.

Results are retained at `build/math-tck/results/1792044478663/`, JSON SHA
`ef25dca2ec1647d0490fec3b8e77b60f5d2a3f228f9ac5aff0d00b7a30437216`.
The full result archive is `/private/tmp/krogu-530-public-v2-results-first.tar`,
SHA `d0c6a268adcab53650a169336033e980621a9b2ec0f0952b48d6e9319ee021be`;
parent log SHA `1fb55f0cd51f6a2112da9fe5828040aef514470ef678dedbd3e9c068ff07b688`.
Publication acceptance additionally requires fresh six-job CI for the published
commit; the task's final tracker entry records its exact head and run. The local
gates above do not substitute for that check.

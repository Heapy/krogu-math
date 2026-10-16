# 538 candidate-local modular witness reuse — executed evidence

Source implementation and diagnostics were independently reviewed before execution; composed production was reviewed again after accepted 532/533 and rebased onto accepted additive 534. The frozen implementation completed the executed gates below. Independent acceptance and integration remain separate from this evidence report.

## Scope and binding

Measured parent: `781d961b973274771999d0805d2238b7b8831511` (accepted 532/533/534).
Candidate source: `be76fb7fea5de133cc22b685b89faa025d0adb2f`.
Only production difference is the reviewed private BigInteger witness reuse delta.
Public modPow dispatch, even CRT, inverse and the accepted context factory/kernel
are unchanged. A lazy candidate-local session retains context/workspace across
eligible primality witnesses after the original prefix; subtype fallback and RNG
draw/budget policy remain intact. No V6 exact-power guard was adopted.

Fresh parent JAR SHA-256:
`91f0d6a48b8f677f468cbd8b6cee0a617a6d456b96b8132cbd1488c9e92adbf8`.
Candidate JAR SHA-256:
`7f084311aea30b74f68db340e28e2ea60e7f4ce2decd7d219d603456f51be79b`.
Source/build binding is in `source-bindings/binding.json`; production, script and
artifact before/after checks passed. Commands/logs and raw outputs are retained.

## Measurement completion and interpretation

Smoke: both artifacts completed all 130 cases. Full campaign: 3900 observations,
six fresh serial processes, three alternating forks per artifact, five warmups and
five samples for each of 130 cases. Every exact result, origin, SHA, case/sample
inventory and completion guard passed. Independent audit verified 3900 rows,
260 variant/case groups, elapsed arithmetic, CPU/allocation statistics and 108
binding checks (`independent-campaign-audit.json`).

This was a shared host. Baseline fork 2 took about 179 seconds, close to the
unchanged 180-second child limit; other forks took about 138–151 seconds. All
processes completed normally. Original measurements are retained without retries.
Executable-only host snapshots and swap/load counters are in each campaign folder.
An unrelated active Java process was independently identified by cwd as another
project during subsequent full regressions; it was not stopped. No isolated-host
or universal timing improvement is claimed.

Public call medians from 15 batch means per variant (three processes, not 15
independent forks):

| Case | Parent ns/call | Candidate ns/call | Parent allocated B/call | Candidate allocated B/call |
| --- | ---: | ---: | ---: | ---: |
| Early composite 3127 | 625.33 | 646.02 | 2016 | 2040 |
| Fermat prime 65537 | 3547.16 | 3181.99 | 20488 | 3464 |
| Mersenne prime 2^127−1 | 729149.09 | 683495.12 | 35054.50 | 10589.13 |

The early-composite regression (+24 allocated bytes and about +3.3% aggregate
wall time) is retained. Timing is not uniformly better: Fermat fork 1 was about
2645 ns parent versus 3044 ns candidate. Mersenne parent fork medians were
729149/653722/1494294 ns, candidate 684180/626291/690636 ns. Allocation reductions
for multi-witness calls are clearer than a broad timing claim. Random.Default
witness draws are not fully seeded by the fixed operands.

Private context setup candidate aggregate medians: 1/18/142 radix-2^30 words took
about 185 ns / 10.04 µs / 814.25 µs, with 104/384/2368 current-thread allocated
bytes per setup. These include reflected diagnostic execution. Factory source is
unchanged; differences against parent setup timings cannot be attributed to new
session code. Whole-process peak RSS includes compiler/JVM/runtime/setup and is
not per-context retained size.

Reuse comparisons include setup in both fresh-total and reuse-total. Tiny-exponent
K=4 groups were lower in all three forks at all three word sizes. For dense 127-bit
exponents, the first tested K>1 that was lower in all three forks was K=16, K=64,
K=16 for 1, 18, 142 words respectively. These are observed points, not a stable
crossover: 1-word K=64 fork 2 had reuse/fresh ratio about 1.095; 18-word K=4 fork 2
about 2.005. Do not derive a production cutoff from this noisy series. K=1 performs
the same setup work by design; mixed timing differences do not establish a setup
benefit. Exact per-fork points are in `independent-reuse-points.json` and all modes,
K values, CPU counters and allocations remain in `campaign-1/summary.csv`/raw CSV.

The source payload bound remains `9n+2` Int cells for context plus witness session,
excluding object headers/caller operands, or 147464 bytes at cap n=4096. It is a
reachability proof, not an Instrumentation or per-platform retained-heap result.

## Executed acceptance

Focused tests passed: JVM/Java 58, JS 43, Wasm 43, macOS ARM64 43 methods, all zero
failures/errors/skips. Unfiltered regression passed JVM/Java 629 methods/191 suites and JS, Wasm, macOS ARM64 423 methods/95 suites each: 1898 methods total, zero failures/errors/skips. Full build exited 0 in 18m54s; XML and full log are retained.

Selected public harness executions: all six retained invocations PASS, all exit0,
no deadline and no caught-OOM incomplete assertions. Host BigIntegerTest runs took
5.871/5.843 s and PrimeTest 1.125 s; port 37.174/37.474 s and 21.504 s. Original
120/400-second directives, 512 MiB and seed559 were retained. Port class loading
was verified against the frozen candidate JAR. This is selected public coverage,
not the entire public math corpus or official JCK certification. The existing
PrimeTest huge-range expectation adapter remains one separately reported
adaptation-companion SKIP; it is not silently credited as the original assertion.

Strict supplemental public `isProbablePrime(100)` calls each returned TRUE, on the
same candidate JAR and fixed input hashes, in serial 512 MiB children with an
unchanged 120-second total child deadline including compilation:

| Mersenne exponent | Wall s | CPU s | Current-thread allocated bytes |
| --- | ---: | ---: | ---: |
| 521 | 0.03385 | 0.03385 | 357784 |
| 607 | 0.03767 | 0.03767 | 360368 |
| 1279 | 0.22341 | 0.22340 | 369680 |
| 2203 | 1.08021 | 1.08010 | 383272 |
| 2281 | 1.22948 | 1.22941 | 384792 |
| 3217 | 3.61629 | 3.60684 | 398552 |
| 4253 | 12.13141 | 10.28336 | 414216 |

Those are single actual correctness calls including first-call public setup/RNG
initialization, not paired performance estimates. A timeout or false would have
remained failure; neither occurred. Logs, input hashes and rows are in
`known-primes-1/`. Historical failed/adverse V6 and older public observations remain
unchanged and are not the new artifact's baseline.

## Bounded JS residual investigation

The first full JS run showed broad shared-host inflation: 416 matched methods totaled 299.803 s in accepted 532 evidence versus 835.930 s here. Prime/primality methods rose 5.237→28.919 s (5.52×), while other unchanged methods rose 294.566→805.965 s (2.74×). Added methods explain only 1.046 s. These are run observations, not a controlled causal attribution (`independent-js-duration-comparison.json`).

One prereviewed bounded check then used freshly compiled exact parent 781d961 and the frozen candidate, identical compiled-test callback selection and byte-identical original fixture sources. It invoked four original PrimeOracle methods and two unchanged controls, with all original assertions intact; inherited hooks or async callbacks would fail closed. Each fresh Node process registered tests outside timing, warmed each case for at least two calls and 200 ms, calibrated to 20 ms or count 64, then recorded three samples. Three alternating process pairs completed under unchanged 180-second child deadlines. Node 24.16.0, Mocha 11.7.6, 512 MiB Node old-space limit, complete compiled-module/source manifests and loaded dependency origins/hashes were bound before/after. Internal Random.Default draws remain unseeded; fixed input and expected-output source hashes are retained. No allocation metric is claimed for JS.

All six processes and 108 rows passed. Per-process sample medians give these candidate/parent ratios; samples within one process are not independent forks:

| Original fixture method | Wall ratios, forks 0 / 1 / 2 | Process CPU ratios, forks 0 / 1 / 2 |
| --- | --- | --- |
| successorsAt257Bits | 0.927 / 0.910 / **1.309** | 0.923 / 0.906 / **1.316** |
| successorsInLower256BitHalf | 0.876 / 0.865 / 0.778 | 0.875 / 0.864 / 0.777 |
| successorsInUpper256BitHalf | 0.892 / 0.894 / 0.995 | 0.891 / 0.894 / 0.995 |
| successorsThrough255Bits | 0.854 / 0.830 / 0.871 | 0.855 / 0.827 / 0.876 |
| carryHeavyAndNearRadixPowers, unchanged control | 1.002 / 0.999 / 0.936 | 1.019 / 0.991 / 0.944 |
| literalMultiLimbSignedDivision, unchanged control | 0.995 / 1.004 / 0.932 | 1.001 / 0.985 / 0.899 |

The broad 5.52× residual was not consistently reproduced by this paired fixture diagnostic. The adverse At257 fork remains: candidate CPU 1260.674 ms versus parent 958.208 ms, and wall 1248.390 versus 953.667 ms. The other two forks were lower; this is neither a universal speedup nor a no-regression theorem. No rerun was made to improve appearance, and no JIT/RNG/host cause is asserted. Independent audit rechecked all rows, arithmetic, selected titles, completion and loaded-module hashes (`paired-js-1/independent-audit.json`). This measures full assertion-bearing fixture callbacks, not isolated numeric operation throughput.

## Reproduction and retained artifacts

Run commands are in COMMANDS.md and paired-js-1/source-lineage.json. `sha256sum -c evidence.sha256` (or macOS `shasum -a 256 -c evidence.sha256`) verifies this directory. Paths in original raw manifests identify the actual measurement locations; they are not requirements to keep worktrees forever. Source commits, source/script manifests, measured JVM JARs, exact compiled JS modules, raw campaign rows, strict/public results, full/focused XML, logs and independent audits are retained here. The public sources and pinned dependencies are reproducible through the repository's existing public harness and its source/dependency inventory; raw class-loading evidence is in artifacts/public-selected-raw.tar.gz. Generated script caches are excluded. Numerical production sources and measured artifacts passed final unchanged-source checks after all gates; report text was updated afterward.

Independent final index review and integration are still required; this report does not alter historical V6 evidence or claim cryptographic, constant-time, full-domain empirical or uncontended-host guarantees.

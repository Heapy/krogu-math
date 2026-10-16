# Integer multiplication 532 evidence

Final source is based on integrated parent
`e95836f97e91433b49b005f9589ca6d359bfa2ac` (includes 527, 533 and 536).
The final selected constants are Karatsuba 64, square leaf 16 and square entry 384.
New raw logs, immutable JARs/probe sources and source manifests are kept under
`/Users/yoda/dev/pet/krogu-math/.codex-work/532/{logs,artifacts}` in accordance
with the workspace housekeeping policy. The active worktree stays in /tmp until
integration; future worktrees use `.codex-work/<task>/worktree`.

## Source and artifact binding

- Final 384 candidate: `artifacts/selected-candidate-384.jar`, SHA-256
  `e9331e7e6285eec56718ed5e10d2967364d694466ca30c6e90699ab1c9741f9e`.
- Same-harness baseline: `artifacts/integrated-parent-baseline.jar`, SHA-256
  `c04f08c4f2cfefb034db3ff208eec25fd8e13db311b2e31ae3a901d391dd02f0`.
- `artifacts/parent-source-binding.json` certifies 30 common/JVM main sources
  byte-identical to parent. Only the additive DecimalKey file is absent in this
  measured 533 artifact; BigInteger and every multiplication dependency match.
  Original actual 533 source/artifact ledgers are retained as `533-final-*.sha256`.
- Final 34-source manifest: `artifacts/final-source/sources.json`; includes
  integrated DecimalKey 536 and decimal-scaling 533. Source snapshots are alongside.
- Frozen final probe: `artifacts/entry-warmed-probe.jar` and
  `entry-warmed-probe-source.kt.txt`. The compiled final test-support class was
  byte-compared with this exact probe before the final campaign.
- `logs/final-paired-memory-manifest.json` records every actual command,
  artifact SHA and source-manifest SHA. Every process checks loaded main/probe
  origins and hashes before and after work. Source/artifact/aggregate mapping
  received independent coordinator review.

## Performance evidence

Final `final-public-summary.csv` and `final-public-wide-summary.csv` retain all
14 small and 26 medium/large cases separately, using medians of three process
medians. All 40 pass the predeclared investigation rule: slowdown above BOTH 15%
and 5 ns/op. One-limb self is explicitly retained at 8.31→9.85ns (+18.5%,+1.54ns),
48 B/op unchanged. All other small cases pass individually; wide cases do too.
The two source variants use identical frozen probe bytecode, prepared inputs,
≥200 ms warmup and three warm/seven measured adaptive batches per case.
No large-case average hides a small regression.

Raw final evidence is `logs/final-public-{baseline,candidate}-{0,1,2}.log`,
`logs/final-public-wide-{baseline,candidate}-{0,1,2}.log`, and
`logs/final-memory-{school,adaptive,square}.log`. The 8192-limb isolated kernel
run measured 37.789/4.857/4.773ms and 65,575/196,415/196,415B/op respectively.
Output payload 65,536 B and extra adaptive arena 130,800 B document a linear
allocation tradeoff, not a memory saving. Whole-process RSS 381–419 MB includes
JVM/JDK reference preparation; sampled heap is not operation residency.

Calibration retained 3,960 original samples across 264 configurations. Initial
entry sweeps retained 780 intermediate samples with observed variance; a bounded
repeat added 780 robust samples with identical ≥200 ms per-policy warmup. At 384,
recursive medians 33.499–34.347µs versus pure 38.527–39.187µs meet the predeclared
10% conservative margin in all three processes. At 256 typical gains were 4–5%
with a substantial arena cost; the outlier 37.149µs observation stays retained.
`logs/square-entry-warmed-summary.csv` and the raw three sweeps bind this choice.
No precise universal crossover or cross-platform timing improvement is claimed.

Adverse evidence remains available: the initial fixed 20,000-iteration warmup
produced a one-limb self signal 13.9104→27.5625ns. Original logs/probe and two
`original-jit-*.log` diagnostics show C2 traps/recompilation overlapping that
short window. The strengthened same-artifact guard passed; this supports a
tiering-sensitive measurement window without proving sole causality or cold
startup equivalence. The first memory school numerical result/hash checks also
passed, but its sandboxed `/usr/bin/time -l` wrapper exited 1 on denied
`sysctl kern.clockrate`. The failed wrapper log is retained; approved host-RSS
reruns completed successfully. No numerical failure was hidden.

## Correctness and reproduction

Final focused JVM 6 methods plus JS 5/Wasm 5/macOS 5 pass: 21 tests, no failures,
errors or skips. Full composed unfiltered regression passes JVM 618, JS 416, Wasm 416 and macOS
416: 1866 tests, no failures, errors or skips; exit 0, BUILD SUCCESSFUL in 8m 12s.
`artifacts/full-regression/` retains four XML tar archives, summary.json and
the Gradle log. All 34 final source hashes remained unchanged after execution.
The independent coordinator verified source, measurements and all full XML counts.

See [commands](commands.md) and [algorithm and limits](../../docs/integer-multiplication.md).
The diagnostic is JVM test-support source, not a timing assertion or standalone
script. `.main.kts` remains the project convention for standalone automation.

## Historical initial evidence

`initial-jvm.log` and `initial-jvm/` retain the initial 5-method focused run
(before entry selector), exact kernel source/delta and XML. `rebased-jvm.log`
and `rebased-jvm/` retain the first integrated-parent focused run.

The old 536 reference JAR is preserved in durable `artifacts/historical-536-baseline.jar`:
SHA-256 `f805f1ce3e003ec50941b404d0a8acdbff4ce2bd98c94efee861b6af76683eb6`.
Its BigInteger source is Git blob `c3ddc51d1d9d71ff9975fdfd502a560fc9ad70da`,
SHA-256 `5d820640e0e2df14197105aa5d20cb7c0b88a314a04350b338b4bc4bb69a473a`.
That historical JAR was not substituted for the final paired baseline.
Original completed 536/533 worktrees were removed only after exact copies and
source/artifact metadata were verified; future checks depend on durable paths.

The durable task evidence index is `evidence-index.json` at the task root
`.codex-work/532/`; it lists every retained log/artifact/source snapshot by
relative path, size and SHA-256. This committed ledger intentionally indexes
external durable evidence rather than embedding measured JAR binaries.

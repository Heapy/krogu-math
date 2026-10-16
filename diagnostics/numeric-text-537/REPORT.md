# Direct numeric text readers and bounded writers (537)

Status: independently accepted source, correctness and measurements; final integration is tracked separately. Fixed source passed 1,988 unfiltered methods and the final local campaign completed all 2,160 rows.

The implementation reads coherent String/CharSequence/CharArray slices directly, groups validated radix digits into an owned base 2^30 magnitude builder, and publishes immutable values. ASCII recognition has a Unicode fallback. Opt-in bounded factories validate input limits before traversal/magnitude allocation. Existing error/range behavior, signs, exponents, scale/cohort and canonical text remain covered by public-oracle and common regression tests. Existing finite-context 588 parsing is credited through composed tests, not assumed replaced.

New Appendable and provided-buffer APIs plan output lengths in Long, reject explicit limits/short or invalid buffers before writing, and stream bounded chunks. Plain/engineering padding does not allocate a scale-sized intermediate String. Subclasses and truthful restored coefficient subtypes retain their virtual formatter route; allocations performed by arbitrary user callbacks cannot be bounded by these APIs. Existing binary helpers suffice; no new binary API was needed for the text task.

## Source composition and correctness

Exact baseline parent is accepted597 `e4bb2abddd77266c0a6690074bf85104f8b3c4df`. Both compared artifacts include accepted 532/533/534/535/538/597. Only 537 text changes differ; no compact 529 representation changes enter the comparison. The original parser/writer patch identity remains `4e9085b29cf60f8111d13ba57f71faf13c53820f`, independently reviewed after rebase. The optional external-time wrapper is a separate reviewed tooling change.

The first candidate `2b461b6ed729f70d3d185ff00b5c7103a55f9689` passed 1,983 unfiltered regression methods (657 JVM, 442 each JS/Wasm/macOS), all without failures/errors/skips. Its raw XML/logs, source patch, immutable source/artifact bundle and 2,160 measured rows remain retained separately. They are diagnostic evidence for that source, not final-source correctness.

The first campaign exposed avoidable overhead in the new writers: canonical buffers ignored an existing String cache, and numeric chunks used per-chunk String conversion/padding. Four-file source fix `519a757a2f7c2d595506f0f193c20e93ee9a7f38` reuses only an already populated canonical cache after exact ordinary-value guards, preserving subtype fallback and preflight. Chunk length/digits now use integer arithmetic and one bounded CharArray per chunk-emission call (a decimal layout can emit multiple slices), with bulk destination copies. Power-of-two chunks use shifts/masks. The existing old hexadecimal String formatter is unchanged; unrelated noisy observations do not justify changing it.

The fix passed independent source review and ten focused JVM methods. New witnesses exercise all radices 2..36 chunk slices/padding/zero/binary boundaries and public-JDK cold/warm canonical output, cache state, cohort, long cached chunks and limit/buffer atomicity. Cold writers do not force creation of the whole canonical String cache. Revised unfiltered full run 5474 exited 0: 659 JVM, 443 JS, 443 Wasm and 443 macOS methods, totaling 1,988, with zero failures, errors or skips. All frozen source and measured JAR hashes remained unchanged.

## Measurement protocol and scope

The same compiled probe and controller are used for parent/candidate in three alternating fresh-process pairs, five samples per each of 72 exact case IDs (2,160 rows). JVM flags are `-Xms64m -Xmx512m -XX:+UseSerialGC -XX:ActiveProcessorCount=1`; pinned BellSoft 25.0.4+9-LTS and Kotlin 2.4.21 runtime libraries are bound by hashes. Each case receives at least 200 ms of timed warmup and five batches. Adaptive batches target 20 ms or stop at 2,048 operations. Short capped fast cases and fifteen batch means from only three processes limit precision; there is no timing assertion or arbitrary speed promise.

Cold-format cases prepare fresh equivalent objects outside both time/allocation counters and use each once. Construction-format deliberately includes parsing. Cached-format controls are separate IDs. Buffer baselines use the available legacy String-plus-copy route; canonical baselines may reuse their already warmed String cache. Costs of a new direct writer against that route are not regressions in the unchanged legacy formatter. Appendable has correctness coverage but is not separately timed; buffer results are not extrapolated to every overload. Result ring/volatile publication prevents discarded outputs, and public JDK/text verification precedes each case outside counters.

Actual measurements are LOCAL macOS 26.6.2 ARM64/Apple M4 Max, 36 GiB RAM. The initial host snapshot showed load 14.40 and 12,888 MiB swap used. Per-process host snapshots and adverse results are retained; no isolated-host claim is made. `/usr/bin/time -l` describes the whole process including object preparation, warmup, JIT and GC; it is separate from exact current-thread transient allocated bytes and is not per-operation retained size. Initial and revised campaigns are separate observations, not a controlled isolated-stage speedup ratio. No ratios against remote 597/claws or historical 527 measurements are made.

A planned claws upload was rejected by automatic approval review before SCP execution. Only an empty remote staging directory was created; no files or numerical process ran there. The authorized safe fallback is local matched measurement. Rejection metadata is infrastructure history, not a failed numerical sample; no alternative export bypass was attempted.

## Final same-host results

Final campaign 68570 exited 0. Independent review reproduced every ID, sample, counter, binding and all 72 pooled/per-process summaries. Fifty-two cases were faster in all three process pairs; none was slower in all three. Twenty cases changed direction between pairs. These observations do not establish universal improvement.

Values below are medians of fifteen batch means (three processes × five samples), in ns/op and transient allocated B/op. Full 72-case and three-fork statistics are retained in `final-local-summary.csv` and `final-local-summary.json`; all raw rows remain archived.

| Case | Parent ns/op | Candidate ns/op | Parent B/op | Candidate B/op |
| --- | ---: | ---: | ---: | ---: |
| decimal/wide/buffer-canonical | 9.562 | 13.286 | 0.012 | 0.012 |
| decimal/wide/cold-format | 19204.508 | 2361.063 | 63528.012 | 2320.012 |
| decimal/wide/construction-format | 38924.641 | 4477.213 | 190680.047 | 3216.012 |
| decimal/wide/parse-string | 19931.193 | 2488.668 | 127152.023 | 896.012 |
| integer/1234/buffer-16 | 1626.221 | 1536.173 | 2120.012 | 736.012 |
| integer/1234/cold-format-10 | 513338.547 | 40187.580 | 523272.375 | 5376.047 |
| integer/1234/parse-string | 291049.156 | 8669.128 | 1400312.188 | 2792.012 |
| integer/9/buffer-16 | 41.301 | 57.333 | 72.012 | 128.012 |

The first campaign's wide canonical buffer cost (approximately 177× latency and +2,904 B/op) exposed a real avoidable writer defect; that campaign and source are retained. The fix reuses an existing canonical cache and removes per-chunk Strings. Final wide canonical buffer measured 9.562→13.286 ns/op and 0.0117→0.0117 B/op; its three latency ratios were 1.389/1.676/0.751. The small common allocation remainder is measured rather than attributed to the formatter. Cached copying can still be cheaper than new writer guards/planning.

The nine-digit hexadecimal buffer remains an adverse pooled result: 41.301→57.333 ns/op, 72.012→128.012 B/op (+56 B bounded scratch), with fork ratios 1.433/0.638/2.051. Larger hexadecimal buffers use fewer bytes; the 1,234-digit case measured 1,626.221→1,536.173 ns/op and 2,120.012→736.012 B/op. Decimal CharArray parse has adverse pooled latency despite lower allocation and an opposite direction in fork 2. Cached controls and the unchanged old hexadecimal String path also vary. These cases are not omitted or claimed to improve.

### Every pooled adverse latency case

Ratios are candidate/parent; values above one are slower. The table includes unchanged controls and additive APIs, not only changed old routes.

| Case | Pooled ratio | Fork 0 / 1 / 2 | Parent / candidate B/op |
| --- | ---: | --- | --- |
| decimal/large-scale/buffer-canonical | 1.316 | 1.717 / 2.412 / 0.837 | 0.000 / 0.012 |
| decimal/large-scale/cached-format-control | 1.394 | 1.433 / 2.707 / 0.899 | 0.000 / 0.012 |
| decimal/large-scale/parse-chars | 1.275 | 1.275 / 1.727 / 0.898 | 448.012 / 216.012 |
| decimal/negative-scale/buffer-canonical | 1.125 | 1.658 / 1.365 / 0.868 | 0.012 / 0.012 |
| decimal/negative-scale/cached-format-control | 1.244 | 1.982 / 1.549 / 0.922 | 0.000 / 0.012 |
| decimal/negative-scale/parse-chars | 1.560 | 1.560 / 2.031 / 0.503 | 448.012 / 216.012 |
| decimal/small/buffer-canonical | 1.266 | 1.668 / 0.944 / 0.862 | 0.012 / 0.012 |
| decimal/small/cached-format-control | 1.575 | 1.600 / 2.781 / 0.816 | 0.012 / 0.012 |
| decimal/small/parse-chars | 1.419 | 1.444 / 1.653 / 0.651 | 440.012 / 216.012 |
| decimal/wide/buffer-canonical | 1.389 | 1.389 / 1.676 / 0.751 | 0.012 / 0.012 |
| decimal/wide/cached-format-control | 1.717 | 1.875 / 1.890 / 0.910 | 0.012 / 0.012 |
| decimal/wide/parse-chars | 1.149 | 1.174 / 1.186 / 0.446 | 1584.012 / 896.012 |
| integer/1234/cold-format-16 | 1.168 | 1.300 / 1.035 / 0.961 | 2120.012 / 2120.012 |
| integer/9/buffer-16 | 1.388 | 1.433 / 0.638 / 2.051 | 72.012 / 128.012 |

### Limits and reproducibility

Final host load was 7.14/8.81/8.57 with approximately 12,744 MiB swap used. Host context and process memory envelopes are preserved for every child. The final baseline/candidate reached the 2,048-operation cap in 825/1,080 and 1,015/1,080 rows; 801/1,080 and 1,028/1,080 batches were shorter than 20 ms, and 608/1,080 and 725/1,080 shorter than 1 ms. Minimum batches were 2,666 and 6,250 ns. Tiny cached-operation ratios therefore have particularly weak precision. There are three process pairs, not fifteen independent replicates.

The probe measures elapsed time and current-thread allocated bytes, not operation CPU or retained graph size. Whole-process memory includes setup, JIT, GC and all workloads. No timed JDK variant is present: JDK APIs provide correctness oracles, not a performance-parity result. No cross-host or cross-campaign stage speedup is inferred.

`evidence-index.json` maps retained files and archive entry manifests; `evidence.sha256` verifies the package. Archives contain both campaigns, initial and final full regressions, focused checks, frozen source/artifact bundles, exact commands, host/runtime ledgers, rejected-export history and independent reviews. Measured parent/initial/fixed JARs and the shared probe/controller are preserved. Reproduction uses the committed script, pinned runtime and source-bound artifacts; original command ledgers document the actual compiled-controller invocation and local fallback.

# JVM division and precision investigation (597)

Final performance and correctness evidence passed independent review. Final four-platform correctness passed 1,942 methods with zero failures, errors or skips.

## Result

On the same claws host, the unchanged small context-division workload improved from 1,249.55 to 169.80 ns/op (7.36×), with allocation reduced from 6,233 to 466 B/op. Wide division improved from 1,992.54 to 284.88 ns/op (6.99×), with 8,158 reduced to 624 B/op. Warm wide precision improved from 154.33 to 3.99 ns/op and from 712 to 0 B/op. These are medians of fifteen batch means from three fresh JVM processes per artifact, not fifteen independent processes.

The fresh matched JDK remains faster: division gaps are 3.28–4.05×, construction-plus-precision gaps 7.55–10.67×, first prepared-string precision gaps 3.45–6.35×, and warm wide precision is 1.399×. No global parity or universal speedup is claimed.

Every priority division/precision case improved in every process. An adverse construction result remains: large-scale construction in process 2 was 1.227× its baseline, despite a pooled ratio of about 0.971. Cross-process ranges and all raw data are retained. Intermediate and final campaigns are separate observations; they are not an isolated measurement of the second change.

## Final matched measurements

All times are ns/op. Allocation is exact current-thread allocated bytes divided by operation count; it includes shared harness/wrapper overhead. Each row uses the same inputs, expression, counter scope and probe bytecode for before, after and JDK.

| Workload | Before ns | Final ns | JDK ns | Before B/op | Final B/op | JDK B/op |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| decimal/large-scale/divide | 1026.924 | 200.214 | 49.454 | 4218.000 | 548.000 | 300.000 |
| decimal/large-scale/precision | 38.234 | 3.162 | 3.002 | 160.000 | 0.000 | 0.000 |
| decimal/negative-scale/divide | 1024.288 | 198.587 | 49.085 | 4218.000 | 548.000 | 300.000 |
| decimal/negative-scale/precision | 36.902 | 3.147 | 3.220 | 160.000 | 0.000 | 0.000 |
| decimal/small/divide | 1249.550 | 169.802 | 43.538 | 6233.000 | 466.000 | 232.000 |
| decimal/small/precision | 25.049 | 2.451 | 2.478 | 104.000 | 0.000 | 0.000 |
| decimal/wide/divide | 1992.539 | 284.883 | 86.919 | 8158.000 | 624.000 | 336.000 |
| decimal/wide/precision | 154.325 | 3.992 | 2.852 | 712.000 | 0.000 | 0.000 |
| precision-construction/large-scale/string | 648.371 | 629.849 | 83.410 | 3083.485 | 2931.472 | 279.996 |
| precision-construction/negative-scale/string | 638.932 | 607.460 | 79.968 | 3083.485 | 2931.485 | 279.998 |
| precision-construction/small/string | 168.756 | 148.336 | 15.926 | 819.493 | 723.496 | 87.998 |
| precision-construction/wide/string | 1899.985 | 1790.938 | 167.851 | 9240.438 | 8536.438 | 375.996 |
| precision-first/large-scale/coefficient | 44.438 | 17.931 | 9.449 | 175.969 | 15.969 | 15.969 |
| precision-first/large-scale/string | 47.504 | 18.125 | 4.936 | 175.969 | 15.969 | 15.969 |
| precision-first/negative-scale/coefficient | 42.661 | 16.039 | 10.460 | 175.969 | 15.969 | 15.969 |
| precision-first/negative-scale/string | 48.695 | 18.385 | 5.329 | 175.969 | 15.969 | 15.969 |
| precision-first/small/coefficient | 36.498 | 28.583 | 5.944 | 119.969 | 15.969 | 15.969 |
| precision-first/small/string | 44.725 | 23.264 | 3.664 | 119.969 | 15.969 | 15.969 |
| precision-first/wide/coefficient | 167.257 | 22.106 | 13.152 | 727.969 | 15.969 | 15.969 |
| precision-first/wide/string | 173.653 | 23.639 | 6.296 | 727.969 | 15.969 | 15.969 |

Per-process medians and ratios are in `final-paired-summary-observed.json` and the independent final audit. Warmed operands, first observation on a prepared coefficient/string object, and construction-plus-precision are distinct cases. Fresh pools are constructed outside first-observation counters. Construction cases deliberately include parsing; they must not be interpreted as pure arithmetic.

## Attribution and implementation

Eight ten-second JFR profiles and eight separate controls identified repeated `TEN.pow` in decimal magnitude metadata. Wide precision had 963/978 CPU samples with own frames and 919 with `pow`; wide division had 975/977 own, 849 metadata and 806 `pow`. These inclusive counts overlap and must not be added. Small precision own-frame coverage was 815/974, so a 99% coverage claim for all cases would be false. Recorded allocation weights were 1.040–1.085 of exact thread allocation; they estimate sites/types rather than exact bytes per site. Profile/control wall ratios of 0.916–1.047 are single-pair variation, not negative instrumentation cost.

The metadata change adds an immutable table for powers 10^0 through 10^128, preserving the full previous fallback beyond that threshold, special supported-range checks, and existing BigDecimal 0..18 identities. The table has at most 1,935 Int payload cells, 129 BigInteger entries and 129 table references. This excludes all object/array headers and padding. First-ever initialization occurs outside hot and per-object first-observation timing; its cost is not claimed free. There is no dynamically growing cache or numerical range cap.

A private volatile, transient precision field caches only exact stored immutable BigInteger metadata. Public overridable precision still dispatches; restored coefficient subtypes retain their callback routes. Zero is a safe uninitialized sentinel because precision is positive. Each decimal gains an Int field and platform-dependent padding; no total object-heap claim is inferred from source payload bounds. Concurrent publication and current-format serialization are tested.

The first change passed a separate 900-row paired campaign and 1,936 full regression methods. Its artifact and results remain distinct from final source. A residual small-division JFR/control pair then identified repeated coefficient-to-string normalization: 955/963 own CPU samples, 591 quotientExponent, 555 toString and 306 normalizedComparison, again overlapping inclusive counts. Exact allocation was 2,724 B/op; sampled/exact allocation ratio was 1.064. The single profile/control wall ratio 0.959 is variation.

The final normalization change uses numeric digit counts and prefix quotient/remainder comparison only when both stored coefficients are exact ordinary compact BigIntegers. Long.MIN_VALUE and all subtype/wide fallback sequences remain unchanged. The decimal divisor is at most 10^18; no overflowing 19-digit rescale multiplication or floating approximation is used. Tests cover signs, zero, equal prefixes with and without a nonzero suffix, reversed operands, Long endpoints, wide fallback, extreme scales, public JDK MC34 results, and serialized subtype exception identity.

## Protocol and provenance

Host: claws, Linux x86_64 KVM, Ryzen 7700X, 8 physical / 16 logical processors. Runtime: existing BellSoft Liberica 25.0.4+9-LTS. Every child uses an empty environment with explicit Java/path/locale/timezone and `-Xms256m -Xmx1g -XX:+UseG1GC`. Runtime binary hashes, actual argv, source snapshots, class origins, artifacts, inputs, masks, checksums and exit statuses are checked before/after. No foreign numerical implementation source was read.

The initial campaign measured original 527, post-533, accepted-535 current-before and fresh JDK in twelve JVMs: 1,200 rows. This separates earlier integrated changes using fresh Linux observations, rather than cross-host ratios against the old macOS baseline. Original priority IDs and sixteen prepared slots, MathContext(34, HALF_EVEN), five warmups, five samples and three processes are preserved. Twelve additional first-observation/construction controls use separately documented state. Adaptive calibration, extra prewarm, escaped result ring, counter boundaries and maximum iteration caps are explicit in the frozen probe.

The final campaign has nine fresh JVMs and 900 rows, with before/final/JDK ordering reversed in the middle process pair. All workloads use the original identical probe:

- Before accepted535 JAR: `1f730223b9b5fb809c1101fd5a7f581574550fde74192369251234582e470cb6`.
- Final JAR: `de9435a2f08df94c09e78f1300b017844a083bd527a8d3e6f884f372044b61df`.
- Probe: `d30394216cd721dcb02b2fa73ce2b3d2474dd19d78c3371a8bdd9096696f8559`.

The final independent audit verified 900 rows, nine child exits, 20 IDs, 91 transfer hashes, 82 before/after source snapshots, exact Java arguments and bindings, offsets/masks/counters, cross-artifact checksums and independently recomputed medians. Maximum observed CPU steal was 0.0497%. This is not proof of an isolated host.

Available process memory is separate: final VmHWM was 426,372–524,224 KiB over the whole process lifetime, including startup, setup, JIT, GC and any profiling/attribution work. It is not per-operation retained storage or a numeric-operation peak. External `/usr/bin/time` is absent on claws and explicitly NA. No missing metric is replaced by RSS or an allocation counter with a different meaning.

Failed prepare-1 compiler setup evidence remains preserved. Removing inherited KOTLIN_RUNNER only from the owned compiler child restored the intended pinned compiler; exact compiler/JDK/classpath/exit provenance is recorded. Failed setup is not numerical data.

## Correctness and remaining work

Final focused JVM verification passed six methods with all 41 frozen source hashes unchanged. Final unfiltered four-platform run 24166 completed successfully: 643 JVM, 433 JS, 433 Wasm and 433 macOS ARM64 methods, all without failures, errors or skips. All 41 frozen source hashes and the measured candidate JAR hash remained unchanged. The separate earlier first-change full run passed 640 JVM and 432 each JS/Wasm/macOS methods. Earlier counts are not relabeled as final-source coverage.

Existing final regression includes all eight rounding modes and error/cohort policies in DecimalConformanceTest and DecimalDivisionPrecisionTest, zero and cancellation fixtures, supported-range/extreme-scale cases and existing subclass dispatch probes. New focused tests cover the actual cache and normalization boundaries. There are no timing assertions in correctness tests.

Parser/construction/cold-format improvement remains task537; physical compact retention and selected primitive arithmetic remain529; multiplication532 is already accepted; policy calibration539 is pending composed sources. The second normalization repair overlaps a future ordinary compact path in529 and must be preserved during composition. Its MC34 wide-result quotient fallback is not covered by a smaller MC7 primitive benchmark. These mappings do not claim pending tasks have already eliminated measured residual costs.

Source review and initial, intermediate, residual and final raw audits have passed independently. The final source passed all required four-platform gates. The immutable evidence package, report, source bindings and final correctness counts passed independent review. Serial integration and tracker closure are recorded in the task feed; no further numerical campaign is needed.

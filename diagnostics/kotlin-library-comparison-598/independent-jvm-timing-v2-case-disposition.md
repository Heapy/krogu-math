# JVM598 v2 finite comparison disposition

QUALIFIED_USABLE_FOR_LITERAL598_FINITE_LATENCY_THROUGHPUT_REPORT_WITH_UNSTABLE_CELLS_NOT_STEADY_RANKING

Yes: full corpus supports an honest comparative finite post-minimum-training latency/derived-throughput report with all raw/variance/unstable cells and resource scopes. It does not support one stable universal ordering or pretending each median is steady state. Unstable cells are results, not omitted failures or peer NA.

Training and measured batch execute identical operation/ring/counter bracket. record=false/true only controls TIME println after counters/input/result checking. First record=true may change JVM profile, but no compiler/deoptimization trace observes that mechanism.

Remaining sequential CPU-coherent transitions are real observed execution-regime changes. Specific JIT/GC or record-branch attribution is unproved; no demonstrated arithmetic mismatch, counter error or systematic measurement asymmetry remains requiring another protocol fix.

Ratios below are peer/Krogu, one median per child from all seven observations. Values above1 mean peer slower only on this measured workload. No statistical significance or global ranking is inferred.

| Peer | Workload | Wall ratios F/R/F | CPU ratios F/R/F | Disposition |
|---|---|---|---|---|
| jdk-bellsoft-25.0.4+9 | int/signed/PARSE | 0.896 / 0.959 / 0.836 | 0.885 / 0.970 / 0.830 | Observed consistent direction |
| jdk-bellsoft-25.0.4+9 | int/signed/self | 1.547 / 1.701 / 1.478 | 1.697 / 1.591 / 1.508 | Observed consistent direction |
| ionspin-bignum-0.3.10 | int/carry30/SUBTRACT | 2.678 / 2.832 / 2.894 | 2.864 / 2.968 / 2.909 | Observed consistent direction |
| ionspin-bignum-0.3.10 | int/carry30/PARSE | 6.430 / 6.486 / 5.727 | 6.153 / 6.124 / 5.651 | Observed consistent direction |
| ionspin-bignum-0.3.10 | int/wide10/FORMAT | 12.360 / 13.746 / 12.793 | 12.136 / 13.140 / 12.835 | Observed consistent direction |
| kbignum-0.0.20-desktop | int/carry30/PARSE | 0.842 / 0.842 / 0.804 | 0.812 / 0.752 / 0.770 | Observed consistent direction |
| kbignum-0.0.20-desktop | decimal/signed/ADD | 1.709 / 1.957 / 2.037 | 1.763 / 2.138 / 1.866 | Observed consistent direction |
| jdk-bellsoft-25.0.4+9 | decimal/zero/self | 0.688 / 0.033 / 0.082 | 0.788 / 0.039 / 0.089 | Unstable magnitude/ranking |
| jdk-bellsoft-25.0.4+9 | decimal/zero/ADD | 1.189 / 0.077 / 0.068 | 1.365 / 0.081 / 0.087 | Unstable magnitude/ranking |
| ionspin-bignum-0.3.10 | decimal/zero/MULTIPLY | 0.908 / 5.213 / 1.707 | 1.080 / 6.031 / 1.625 | Unstable magnitude/ranking |
| kbignum-0.0.20-desktop | decimal/zero/FORMAT | 2.258 / 0.811 / 3.721 | 2.255 / 0.813 / 3.723 | Unstable magnitude/ranking |
| kbignum-0.0.20-desktop | decimal/scale/SUBTRACT | 1.964 / 3.626 / 10.864 | 1.892 / 4.244 / 10.865 | Unstable magnitude/ranking |
| ionspin-bignum-0.3.10 | int/carry30/MULTIPLY | 6.923 / 7.532 / 1.127 | 9.596 / 8.592 / 1.209 | Unstable magnitude/ranking |

The table is illustrative; the JSON preserves all160 comparisons and all648 sample traces separately. No unstable case is removed from aggregate evidence.

648 fresh supervisor children, first explicit selected public operation before full gate after declared prep. Ratios retained separately from warm; may include class/module initialization and parse-internal work. Not pristine cache/process-cold causal comparison.

PARSE repeats native parsing of same immutable String. FORMAT first parses unobserved native operand outside first counter; warm FORMAT occurs after full output observations and may cache. Exact plain-spelling selected API intersection only; not general scientific/cohort equivalence.

Current-thread bytes/op sometimes changes across7samples; retain sequence and all3 cohorts. Does not measure total pool/process allocation or native object retained size.

No further numerical run or posthoc duration/stability acceptance threshold is necessary or recommended for this finite reporting scope. Any later steady-state-specific research would be separately scoped, not silent completion prerequisite.

Historical v1 remains transient evidence. Portable trends require their own actual review.

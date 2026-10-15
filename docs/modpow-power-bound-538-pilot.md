# Exact-power modular bound: bounded feasibility pilot

The private candidate remains unadopted. Its focused JVM tests and bounded five-pair pilot passed their execution checks, but the pilot contains adverse timing/allocation observations and does not close task538.

The candidate adds a proved bound for an exact power-of-two base to the existing generic bound. Baseline and candidate both already contain the modular-context infrastructure and generic guard. No private dispatch was observed. Source-model predictions and public-input predicates are separate from measured public `modPow` calls.

## Source and artifact binding

Both variants derive from published `60aea413e0c7af007844aace6ec28ecdfa019b67`. Their only production difference is BigInteger's private bound:

| Variant | BigInteger SHA-256 | JVM JAR SHA-256 |
| --- | --- | --- |
| Baseline | `d0e757ebd5955604d5fb66d8dda13b1c15b2ecb300a37a5fbcb1fcfaeb65c16f` | `96af86730b4f492061df0a8ba2b185160644e2b18306fc10332703084957cfb1` |
| Candidate | `904853003e38ac20a095376867e4fe9498d163449a8fa5502f933b9f59350834` | `23ffbc26c2143fc3f559c3a3f26466c35375ebb496f01728345d6a308c72f9b8` |

Both focused JVM selections passed7 suites/29 methods with zero failures, errors, skips or inventory anomalies. Fresh compilation and JAR creation occurred in those first focused builds (baseline1m21s, candidate50s). Later standalone JAR invocations were UP-TO-DATE verification, not forced rebuilds. All206 input hashes remained unchanged within each variant.

The initial V5 baseline failed one test because its snapshot called a deliberately strict subclass callback before the operation. That failure is preserved. V6 changes only the two snapshots to use ordinary base values through inherited double negation; its strict operation callback assertion remains. Independent source and actual-result reviews accepted this correction and both focused runs.

## Actual pilot

Liberica25.0.4+9-LTS and Kotlin2.4.21 ran exactly five pairs/ten serial measurement children, each with one cold call, no warmup and one sample. First variants alternate baseline/candidate/baseline/candidate/baseline. Host JDK oracle preparation is outside timing. Each child checks the returned result, artifact origin/digest and VM flags. The guardian checks paired operand hashes and CALL/TOTAL accounting.

| Case | Candidate/baseline wall | CPU | Allocated bytes |
| --- | ---: | ---: | ---: |
| Power-bound edge, base2^255/exponent2/modulus2^510+3 | 0.185527 | 0.185736 | 0.659724 |
| Existing generic-bound edge, base2^254 | 0.869518 | 0.870168 | 0.998179 |
| Tiny base with142-limb modulus | 0.568899 | 0.659430 | **1.001524** |
| Ordinary18-limb case | **2.645382** | **1.399509** | 0.995345 |
| Rejected nonpower, base2^255+2^254 | 0.925853 | 0.925344 | **1.001349** |

The ordinary timing observation remains unresolved; tiny/nonpower allocation increases also remain visible. These single cold observations establish neither stable regressions nor general improvement. Before/after host readings show substantial load and swap: swap12671.94/12559.94MiB and one-minute load20.83/18.30. No uncontended-host, statistical, crossover, context-reuse or private-branch claim follows.

Every child/supervisor completed successfully within the unchanged30s/70s limits. The guardian's720s absolute budget includes startup and cleanup; it finished with573035ms remaining, ten CALLs/ten TOTALs, `failure=null`, all captured identities DEAD and its final direct Process reference not alive. No retry or extension ran. All heaps were bounded by256MiB. The parent observed outer invocation91530 exit0 and guardian34442 absent with scoped `ps` exit1/no rows; these two terminal tool records are parent-reported and are not separate members of the capsule.

Independent read-only review accepted the pilot's evidence integrity and retained every adverse observation. Other targets, broad performance acceptance, public primality completion and task538 closure remain open. The subsequent non-null API candidate retains published d0 kernels and excludes this optimization.

## Preserved evidence

The capsule `/private/tmp/krogu-538-v6-pilot-evidence-capsule-v1.tar` has SHA-256 `064965f67915e9338a7563016d30eef82073e8d4525db0c96db9cb34beea48de`. It retains source preparation, raw measurements/process accounting, host records, focused JVM/artifact evidence and the earlier failed baseline capsule; compiled caches are excluded. The original sandboxed sysctl stdout is empty; successful escalated host readings are retained separately.

```text
fd1ebdcfb7db34e685dcb9259c466f2d8b8499ebe7be559b9d0fdfff341c16bc  campaign.main.kts
621d4765133413cf5aa9be011de628c3ffd9bc28d1b3992572499374a6ee819a  measure-modpow.main.kts
dbf294f3d7b02c6cbba3d1a61ac4836d2d28f78a3d945618be3dddd02acdd6bb  supervise-measurement.main.kts
f16cd9a4710f088c723f444ce54ebee22622c3c2d66aa3955b22e5dc62775d0b  krogu-538-v6-pilot-campaign.log
f96e85769322f45d35c834c985614e174767812b3226eee6aa3944ffc438630c  samples.tsv
1fb31ff890958b7f05c3355587fec5def32411695f6b0309f34e464094ae14ca  calls.tsv
```

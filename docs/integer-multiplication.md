# Adaptive integer multiplication

Task `local:532` adds independent magnitude kernels underneath the existing
`BigInteger.multiply` API. Coefficients use unsigned base-2^30 limbs. Small
products use row multiplication. Shared magnitude arrays can use a specialized
square that computes each off-diagonal product once and doubles it. Larger
balanced operands use Karatsuba; strongly unequal operands use short-sized
blocks instead of padding both operands to the longer size. No Toom–Cook or
parallel executor is introduced.

The public result is still exact and signed. Zero behavior, stored coefficient
range checks, final normalized bit-length validation, and subclass dispatch of
`parallelMultiply` through `multiply` are preserved. No mutable input limb array
is written. Shared magnitude storage, including `x.negate()`, is safe for square
recognition because sign is applied separately.

## Arithmetic and workspace bounds

With radix B = 2^30, a school accumulation is below B². A doubled square cross
product plus its existing result limb and carry is below 2B² = 2^61, so every
accumulation fits signed Long. Diagonal and cross carries propagate within the
2n-limb square result.

Karatsuba computes z0 = lowA*lowB and z2 = highA*highB directly into disjoint
ranges of the result. It then computes (lowA+highA)*(lowB+highB) in scratch and
subtracts z0 and z2 before adding the cross product at the split offset. The
subtractions are nonnegative; carry propagation during recombination is not
limited to the temporary product length.

Input halves are borrowed array/offset/length views, with no slice copies.
One invocation-owned IntArray arena reserves both sums (including carry limbs)
and the middle product before recursing. Child allocations follow all live
parent ranges, and marks restore the arena after each child operation. No
workspace is retained globally or shared between calls. A checked recurrence
bounds scratch storage linearly in the balanced operand length; unbalanced
blocking needs linear scratch in the shorter length. The output needs a+b
limbs. Native/JVM call-stack depth is logarithmic, and stack/object headers are
additional to the recorded array payload bounds.

The early overflow test still uses the minimum possible product bit length:
leftBits + rightBits - 1. Supported borderline products receive a final exact
normalized bit-length check. No threshold or dimension is silently narrowed.

## Threshold evidence and limits

The selected thresholds are 64 limbs for Karatsuba, 16 for square leaves, and 384
for the whole-square entry selector. Shared magnitudes of 16..383 limbs use a
whole square. Larger shared magnitudes enter the 64-limb recursive dispatcher;
recursive leaves keep the 16-limb square rule. Distinct arrays retain the general
multiplication route, including numerically equal operands with separate storage.
The entry wrapper does not change forced-kernel calibration semantics.

Three original sweeps retained 3,960 samples across 264 forced configurations.
After identifying avoidable medium-square overhead, three robust entry sweeps
compared pure square, recursion, entry 256 and entry 384 with identical ≥200 ms
per-policy warmup and 20 ms target batches. At 384 limbs, recursive process medians
were 33.499–34.347µs versus 38.527–39.187µs pure square, meeting the predeclared 10%
diagnostic margin in each process. At 256 typical recursion benefit was only 4–5%
while allocation increased from 2064 to 5768 B/op; a retained 37.149µs pure-square
outlier widened that range. These measurements support a conservative policy,
not a precise minimum crossover or a universal optimum. The intermediate noisier
780-sample campaign is retained alongside the robust 780 samples.

The final public comparison uses exactly the same frozen probe bytecode against
the integrated-parent and final candidate JARs. Fixed inputs, ≥200 ms warmup,
three warm batches and seven measured batches are identical. Counts adapt toward
20ms with an 8,388,608-operation cap. Results escape through a volatile field;
public JDK product checks and decimal conversion stay outside timing. Three
fresh processes per implementation alternate launch order. Loaded main/probe
origins and SHA-256 values are checked before and after execution.

All 14 small-case guards and 26 medium/large-case guards pass the predeclared
investigation criterion (slowdown exceeding both 15% and 5 ns/op). This is a local
diagnostic threshold, not a CI timing assertion. For example, one-limb self
products changed 8.31→9.85ns (+18.5%,+1.54ns), below the absolute threshold;
allocation remained 48 B/op. Full per-case tables retain such changes rather than
hiding them in an aggregate. Selected public median-of-process-medians results:

| Operation | Limbs | Parent µs | Candidate µs | Parent B/op | Candidate B/op |
|---|---:|---:|---:|---:|---:|
| distinct | 64 | 1.933 | 1.675 | 552 | 1120 |
| distinct | 128 | 7.975 | 5.338 | 1064 | 2688 |
| distinct | 384 | 75.954 | 37.284 | 3112 | 8608 |
| distinct | 1024 | 550.908 | 173.296 | 8232 | 24288 |
| distinct | 2048 | 2177.013 | 527.624 | 16426 | 48896 |
| self | 64 | 1.929 | 1.056 | 552 | 552 |
| self | 128 | 7.965 | 4.239 | 1064 | 1064 |
| self | 384 | 78.188 | 33.709 | 3112 | 8608 |
| self | 1024 | 546.820 | 166.621 | 8232 | 24288 |
| self | 2048 | 2202.878 | 513.909 | 16426 | 48896 |

These are BellSoft 25.0.4+9-LTS, Kotlin 2.4.21, macOS arm64 observations. No timing
claim is transferred to JS, Wasm or Native. The earlier short fixed-count probe
produced a one-limb self signal; its original logs and JIT chronology are retained.
Candidate C2 traps/recompilation overlapped that short measurement window; the
stronger identical-harness guard passes. This supports a tiering-sensitive window
explanation without claiming sole causality or cold-start equivalence.

An isolated 8192-limb kernel diagnostic measured school 37.789 ms/65,575B per op,
adaptive 4.857 ms/196,415B, and square 4.773 ms/196,415B. Each verifies its exact
product first. Timing/allocation exclude a separate heap-polling batch. Output
payload is 65,536 B and adaptive arena payload 130,800 B; extra linear scratch is an
explicit allocation tradeoff for lower arithmetic cost. Array/object headers,
caller-owned inputs and logarithmic call stack are additional to these payloads.
Process maximum RSS 381–419 MB includes JVM and JDK reference preparation; sampled
heap misses transient peaks. Neither establishes isolated operation residency or
a memory saving. No workspace is retained globally.

## Reproduction and correctness

The common tests compare forced algorithms and entry boundaries with an independent
frozen row reference, including maximal carry chains, zero high halves, odd/even
threshold neighbors, 2n−1/2n/2n+1 ratio boundaries, blocked imbalance, shared and
separate arrays, signed squares and unchanged inputs. JVM tests use the public
JDK arithmetic API as an oracle; no foreign numerical implementation source is
used. Final focused JVM 6 methods plus JS 5/Wasm 5/macOS 5 pass (21 methods).
The full composed regression passes JVM 618, JS 416, Wasm 416 and macOS 416:
1866 tests, no failures, errors or skips. Source hashes remained unchanged
through all four targets.

See [the evidence ledger](../diagnostics/integer-multiplication-532/README.md) and
[commands](../diagnostics/integer-multiplication-532/commands.md). New raw logs,
frozen JARs/probe sources and manifests are preserved under the main checkout's
`.codex-work/532/{logs,artifacts}`. The final production source manifest includes
integrated DecimalKey 536 and decimal-scaling 533. Final artifact SHA-256 is
`e9331e7e6285eec56718ed5e10d2967364d694466ca30c6e90699ab1c9741f9e`.

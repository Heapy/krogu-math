<!-- SPDX-License-Identifier: Apache-2.0 -->
# Task 538: candidate-local witness reuse

The implementation extends the accepted odd-modulus context with candidate-local witness reuse; it does not adopt the historical V6 exact-power guard. It was rebased and independently reviewed on accepted 532/533, then composed with accepted additive 534 at parent `781d961b973274771999d0805d2238b7b8831511`. Historical capsules, failed runs and published V6 adverse observations remain separate. Actual commands, raw observations, source/artifact hashes and limits are recorded in [the evidence report](../diagnostics/modulus-reuse-538/REPORT.md).

## Change and public boundaries

Only private `passesPrimeTest` dispatch and a new `OddModulusContext.WitnessSession` change production behavior. Public `modPow`, its negative-exponent/inverse suffix, even-modulus CRT, the existing private one-shot `pow`, context creation, Montgomery multiplication and the ordinary multiplication kernel are unchanged. Modulus 1, even/negative moduli, zero residues and exponent identities therefore keep their existing public routes. Existing modular/inverse/null/identity tests remain acceptance gates.

The complete certainty/abs/equality/parity/ONE/bitLength/sign/trial-prime prefix and `nm1/s/d` construction execute first. Reuse requires exact runtime classes for the **working** `n`, `nm1`, `d` and witness, after those callbacks. This does not reject numerical inputs or require that the original receiver have the base class: a subtype may return an ordinary value from `abs`, whose original prefix has already executed. If working values remain subtypes, the existing public `a.modPow(d,n)` and `x.multiplyMod(x,n)` route remains unchanged, including callbacks, exception order and error identities. No public callback is replaced by a guessed result.

Context creation is lazy at the first actual eligible witness, attempted at most once per primality call, and never shared across candidates or API invocations. The existing factory rejects unit/even/nonpositive/over-4096-word cases without creating a context; the original full-domain fallback remains. A failed numerical allocation is not caught or reclassified. The eager setup still costs quadratic work for eligible large operands; there is no new timing-derived cutoff or universal acceleration claim.

Deterministic bases, bound and witness order are unchanged. Above the deterministic bound, the original `Random.Default` draws, rejection conditions, repeated `n.bitLength()` observations, round count and witness success/failure order remain unchanged. Fixed public operands do not fully seed the internal random witnesses. A zero-round or earlier-return path does not prepare a context.

## Arithmetic and array proof

Let `B=2^30`, `R=B^n` and let the accepted factory's positive odd modulus be `m>1`. Since `gcd(R,m)=1`, encoding `E(x)=xR mod m` is a bijection on canonical residues. The unchanged Montgomery kernel satisfies `MM(E(x),E(y))=E(xy mod m)`. Context `oneMont` is `E(1)`; the session computes `E(m−1)` once using the unchanged kernel and a disjoint output.

For each witness, ordinary stored-value reduction produces a canonical residue before copying it into the padded input. Reachable public witnesses are positive and below `n`: deterministic surviving candidates exceed the small-prime prefix's 47, while all thirteen fixed bases are at most 41; random witnesses satisfy the unchanged `2 <= a < n−1` rejection rule. Thus their reduction uses the smaller-than-modulus route, including a safe ordinary wrapper; no new full product is materialized. Direct private-engine tests also include canonical zero and one.

At exponentiation entry `result=E(1)`, `factor=E(a)`. The copied right-to-left exponent-bit schedule preserves its old accumulator/factor invariant and finishes at `E(a^d mod m)`. Initial equality with encoded one/minus-one is equivalent to the old ordinary comparison. Repeated squaring stays encoded and executes exactly the old `1 until s` order, accepting only at encoded minus-one and otherwise continuing to the same final false. In particular it does not add an early exit on an intermediate one.

Every session owns five workspace arrays (scratch `n+2`, padded input, factor, result, next) and a separate `minusOneMont[n]`. Context modulus/oneMont/r2 are immutable and never output targets. Factor/result/next begin as three distinct arrays. Each exponent multiply/square writes only to next then swaps two role references; this permutation preserves pairwise distinctness. Witness-tail squaring writes next and swaps with result. No input aliases output/scratch. Before the following witness, padded input is cleared, factor is fully overwritten by encoding, result is fully overwritten by oneMont, and next is written by a complete kernel operation before being read. Old scratch digits are cleared by every Montgomery call. No witness or workspace array escapes as a public BigInteger.

Reachable context-owned primitive payload is `3n` immutable parameter cells plus `5n+2` workspace cells plus `n` minus-one cells: **`9n+2` Int cells**, or `36n+8` bytes, excluding object headers/caller-owned operands. At the unchanged cap 4096 this is 147,464 bytes. Conservatively retaining the factory's temporary `gap[n]` through session preparation gives `10n+2` cells. Canonical public witness reduction shares immutable stored magnitude arrays on its smaller-than-modulus path; it adds wrappers, not a full arithmetic product. This is a source reachability bound, not a measured JVM/JS/Native retained-heap or allocation-site assertion. Existing upper-size resource checks and fallback proofs remain applicable; no claim is made that quadratic setup at the cap is cheap.

## Tests prepared

`ModulusReuse538Test` adds three common methods:

- repeated calls/signs/certainties for Fermat primes (`d=1`, nontrivial square tails), Mersenne primes (`s=1`), and composites whose factors exceed the trial prefix; immutable input snapshots;
- positive modulus subtype parity/length callback counts, with the original first-witness exception object preserved;
- a negative subtype whose observable abs/negate prefix produces an ordinary working value;
- the prime `2^127−1`, above the deterministic bound, exercises actual public random-witness reuse at certainty 1/100/1 without false-negative randomness for a genuine prime.

`ModulusReuse538JvmTest` adds two methods reflecting **only our private classes**, without production test hooks. A public JDK oracle checks all small canonical residues across reused sessions, including zero/one. Wide→small operands, reversed orders, repeated sessions and mixed moduli exercise input clearing, role rotation and immutable context snapshots. Unit/even/negative/zero and over-cap factory rejection remain explicit controls. Existing task-538 common/JVM/Java methods and all relevant primality/modPow/inverse/prefix contracts must still pass after composition.

## Measurement prepared

`scripts/measure-modulus-reuse-538.main.kts` is a separate pinned JVM diagnostic. It accepts fresh matched baseline/candidate JARs and SHA-256 values, verifies actual class origin/loader and runtime/flags, and launches only serial children in alternating variant order. Default three forks, five warmups and five samples; minimum-one adaptive outer group calibration targets at least 50 ms up to a fixed cap. Every returned value escapes through a result ring and is checked after timing against public JDK arithmetic. No foreign numeric implementation body is read.

For 1/18/142 radix words and tiny/dense-127-bit exponents, K is 1/4/16/64:

- `setup` times context factory only;
- `fresh-total` includes K factories and K reflected pow calls;
- `reuse-total` includes one factory and K reflected pow calls;
- `reuse-steady` excludes the one prepared context and includes K reflected pow calls;
- public odd/even/unit `modPow` controls retain their actual setup/prefix/CRT policies; K public calls do **not** reuse a context and cannot establish shared-context amortization;
- public small/composite/Fermat/Mersenne-127 primality cases compare the actual new call-local reuse against its matched baseline.

The fresh/reuse private modes use the same reflected pow method. They still include reflection/boxing/harness costs and deliberately observe private execution, not an exported context API. The measured *total* reuse cost, not steady-state cost alone, determines any observed setup break-even. Report every adverse tiny/one-witness result and distinguish observed K points from a stable crossover claim. Do not infer branch coverage from timings.

Wall, current-thread CPU and allocated bytes are per outer group; each group contains K public/private pow operations where stated. `summary.csv` retains median/min/max batch means. Process peak RSS includes the Kotlin script compiler/class-loading/runtime as well as arithmetic and must not be presented as isolated context residency. Numeric timing/counters begin after compilation, loading, operand/oracle preparation and class initialization. Per-child host load/swap/VM and executable-only CPU snapshots record contention without claiming isolation. Host subprocesses have a maximum 10 s wait, bounded further by the remaining campaign budget. Child 180 s and parent 1,200 s budgets stop only owned ProcessHandle identities, preserve partials, and do not silently retry. The parent budget begins after its own script compilation; timeout cleanup gets up to 10 additional seconds, waits on captured handles and reports surviving PIDs/root liveness rather than asserting successful cleanup. Baseline/candidate canonical paths and SHA values must both differ. The workload's ability to finish within these budgets is an execution gate, not a source-only guarantee.

Before runtime, independently review the diagnostic and freeze it. Run paired fresh JARs from the same integrated 532+533 source basis; their only production difference must be this private reuse change. The historical V6 capsule or JAR pins do not serve as a fresh baseline.

## Pending acceptance

No execution result is claimed by this source document. Required next gates are independent composed-source review; JVM/JS/Wasm/macOS ARM64 modular/inverse/primality contracts with exact inventories; fresh matched JAR origins/hashes; actual setup/total-reuse measurements and one-witness/tiny-prime controls; and the unchanged public primality/BigInteger acceptance inputs, assertions, certainty/round budgets, 512 MiB and original 120/400 s directives.

The frozen candidate passed 187 focused methods and 1898 unfiltered methods across JVM/Java, JS, Wasm and macOS ARM64, with no failures, errors or skips. Three alternating JVM process pairs produced all 3900 required observations over 130 cases. The selected original public harness executions passed under unchanged 120/400-second directives and 512 MiB; one existing adaptation-companion skip remains separately disclosed. Seven strict public `isProbablePrime(100)` checks returned TRUE for Mersenne exponents 521/607/1279/2203/2281/3217/4253 under serial 120-second child deadlines including script compilation. These are named coverage and actual outcomes, not certification of the entire public math corpus.

Measurements were taken on a shared host. Allocation reductions in repeated witnesses are clearer than timing: individual forks include adverse observations and the early-composite case adds 24 allocated bytes. Tested reuse points are nonmonotonic; no universal setup cutoff or speedup is inferred. The full JS run had broad inflation in unchanged methods as well as primality; the bounded, separately named paired original-fixture diagnostic is documented with its raw evidence in the report.

The public harness's documented adapters/resource skips remain separate from original coverage. Current `docs/public-math-consolidated-559.md` records historical 106 PASS/46 SKIP on its named artifact, including adapted PrimeTest; it does not certify this new implementation or restore the skipped original huge-range expectation. Strict known-prime Mersenne checks and actual Boolean outcomes remain explicit evidence. No constant-time, cryptographic, full-domain empirical, universal speedup or uncontended-host claim is made.

## Private boundary for a later compact representation (task 529)

The current proof assumes normalized immutable little-endian radix-2^30 `words`, with zero represented by an empty array and matching stored sign. An exact runtime class does **not** prove which storage tag a future implementation uses. Context factory, witness reduction/copy, exponent digit reads and nm1 preparation must continue to receive that canonical stored magnitude view if task 529 introduces compact storage. Such conversion must use private stored values, preserve callback-free reads and include any expansion/setup cost in measurements; never substitute a public overridable Number conversion. A representation-ineligible fast path must use the correct existing generic fallback rather than reject a supported value.

No context/workspace array may be published or retained as a mutable compact backing array. A newly allocated compact-to-limb view changes the payload accounting and needs a fresh lifetime proof; the current `9n+2` excludes caller-owned input arrays already present under the current representation. The varying-width/zero/session tests and subtype prefix tests must be repeated on the composed representation. Reflection names are diagnostic implementation details, not a public contract; tools must fail closed and be reviewed/repinned if those private boundaries move.

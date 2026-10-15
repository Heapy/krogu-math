<!-- SPDX-License-Identifier: Apache-2.0 -->
# Task 538: bounded odd-modulus context proposal

Status: source/proof proposal only; no production implementation, compilation,
numerical helper, test, fixture generator, benchmark or public harness was run.
Task `local:538` remains linked and `in_progress`. Changes are not committed.
This document is independently derived from our source and integer arithmetic;
no JDK, Kotlin standard-library or third-party numerical implementation was read.
There are no constant-time, cryptographic-security or measured-speedup claims.

## Scope and provenance

Inspected base: `96ddbf7b9241f14265620c72a8217c32c4cdb4ff`, isolated branch
`task/538-odd-modulus-context`. Ownership is this new document only. Parent
528/566 edits are outside this worktree and must not be imported or reverted.
Parent grant 1959 owns the sole numerical/compiler/script/helper window.
Implementation and execution require later parent scope; this is a bounded
proposal milestone, not task acceptance or authorization for another campaign.

Own-source references (line numbers at the inspected base):

| Source | Relevant behavior |
|---|---|
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt:69` | Base-2^30 schoolbook product and full-product range guard |
| `BigInteger.kt:87` | `divideStored`: stored operands, one-word division or allocating binary long division |
| `BigInteger.kt:183` | `mod` validates stored modulus sign, then calls virtual `remainder` |
| `BigInteger.kt:188` | Public `modPow` prefix, odd/even dispatch, CRT and inverse suffix |
| `BigInteger.kt:220` | Private `oddModPowStored` entry shortcuts and stored exponent-bit loop |
| `BigInteger.kt:242` | Power-of-two path with its own virtual metadata behavior |
| `BigInteger.kt:262` | Public inverse validation and private Euclidean working values |
| `BigInteger.kt:282` | `multiplyMod` chooses full product/reduction or bounded add/double |
| `BigInteger.kt:288` | Existing overflow-safe residue multiplier |
| `BigInteger.kt:471` | Primality prefix, witness exponentiation and repeated squaring |
| `BigInteger.kt:823` | `normalized` trims high zeros, validates range, returns singleton zero |
| `src/commonMain/kotlin/io/heapy/krogu/math/MagnitudeLimits.kt` | Public stored magnitude bit length is at most `Int.MAX_VALUE` |
| `src/commonMain/kotlin/io/heapy/krogu/math/PrimalityCertainty.kt` | Test/generation/successor round budgets |

`README.md`, `COMPATIBILITY.md`, existing modular/prefix tests and
`tools/math-tck/primality-timeout-classification.md` were also read. Semantic
search had no repository index; direct own-source reads supplied the evidence.

Parent-supplied diagnostics, not measurements made here: historical fresh
root-1919 own JAR `daf2a133...`, unseeded `Random.Default`, single current-thread
`isProbablePrime(100)` calls returned true for Mersenne exponents 521
(1.693206 s, 15,448,553,152 allocated bytes), 607 (2.503274625 s,
23,219,513,392 bytes), and 1279 (15.467302708 s; allocation not supplied).
Evidence directory: `/private/tmp/krogu-primality-profile-root1959-v2`.
These are call totals, not allocation-site attribution or a statistically proved
hot spot. Stack profiling was pending at delegation. The JAR is not asserted
source-identical to this base; the prompt reports relevant kernels unchanged.
The original public 559 timeouts remain failures (120/400 s); none is a pass.

Task activity 1964 subsequently reports that normal diagnostic calls for
2203/2281/3217/4253 each reached their fixed 60 s deadline with no Boolean
result claimed, owned children terminal 143 and exact-identity cleanup. Parent
reports raw, partial p2203 stack samples through our
`passesPrimeTest -> modPow -> oddModPowStored -> multiplyMod -> mod -> remainder
-> divideStored -> shift/add/subtract` path, and approximately 281 GB cumulative
partial allocation at approximately 23 s. This is parent-supplied interim
evidence, not a final allocation total or allocation-site attribution. Stack
path: the evidence directory's `profile-2203/stacks.tsv`. Root's window remains
held; no kernel implementation is authorized. The source proposal does not
require stronger hotspot or runtime conclusions from these observations.

Read-source SHA-256 metadata:

* BigInteger.kt: `0bcca3834a0839693782c5ff500d0ddc0f0dc8b6ad668695cb6d19c2ab7730da`
* MagnitudeLimits.kt: `3024002fc88ea3e90329905041eaa609fd0d2f2e08323a78e84c8640debff161`
* PrimalityCertainty.kt: `93f0c4743f9a7d8d9acdedd283760904bbfd2caf55e5e4973dd6331d2aa57479`

## Representation and lifetime

Use the existing radix `B = 2^30`, mask `B-1`, little-endian nonnegative `Int`
digits, and signed `Long` for all products/carries. For odd stored modulus
`m > 1`, let `n` be its normalized word count and `R = B^n`. Then
`B^(n-1) <= m < R`, `gcd(m,R)=1`. Never construct a public BigInteger for R,
R^2, a full product or an unnormalized scratch value.

A private context owns copied modulus digits M[n], `rho = -M[0]^-1 mod B`,
`oneMont = R mod m` and `r2 = R^2 mod m`, all canonical residues padded to n
digits. Arrays stay private, immutable after construction, with no global cache,
public API, serializable field or cross-modulus reuse. A separate invocation
workspace is mutable and never shared across threads or reentrant calls. The
context can be shared only if each caller supplies its own workspace. Prefer a
lexically local context/workspace per exponentiation or primality call.

Canonical integer inputs satisfy `0 <= a,b < m`; padded digits are in [0,B).
Montgomery representation of x is `xR mod m`, also canonical in [0,m).
Never mix encoded and ordinary residues except at the named conversion edges.
Export trims only high zero digits, checks the existing magnitude limit and
copies away from mutable slots; no BigInteger may retain workspace storage.
Modulus and operand stored arrays must never be written to, including aliases
`a===b`, `a===m`, or arrays shared by immutable base-class copies.

Proposed initial resource cutoff: `MAX_CONTEXT_WORDS = 4096` (122,880 bits).
This is an optimization cutoff, not a public numerical limit or rejection.
Factory returns no context for m<=1, even m or n>4096, and the unchanged old path
runs instead. Use stored digits/sign, not public `bitLength`, `testBit` or
`signum`, to decide suitability after required callbacks. Do not catch arbitrary
arithmetic errors or OOM to retry. A later change of cutoff needs memory and
break-even evidence, not an exception for a recognized prime.

## Low-word inverse: bounded exact construction

Let `u=M[0]`, which is odd and less than B. Start y=1, so uy=1 mod 2.
For k=1..29, y is the inverse modulo 2^k with 0<y<2^k. Read bit k of
the positive Long product uy. If it is one, replace y by y+2^k; otherwise
leave it unchanged. Since u is odd, adding u*2^k flips bit k and leaves
lower bits unchanged. Induction gives uy=1 mod 2^(k+1), y<2^(k+1).
At the end `rho=(B-y) and (B-1)`. All y additions fit positive Int; uy<B^2
=2^60 fits Long. No signed-overflow Newton expression or full modular inverse
is needed. This inverse is solely modulo the limb radix; negative public
exponents still use the existing `modInverse(m)` semantics.

## Montgomery multiplication, independent derivation

Define `MM(a,b)` for canonical n-digit arrays; result is `abR^-1 mod m`.
Use a zeroed scratch accumulator t[n+2] with every digit kept in [0,B).
At the start of outer iteration i, its mathematical value T satisfies
`0 <= T < 2m`. For i=0..n-1, in exactly this order:

1. Add `a*b[i]` into t, propagating the final carry through t[n] and t[n+1].
2. Compute `q = (t[0].toLong()*rho) and (B-1)`.
3. Add `q*M` into t using a second digit loop and high carry propagation.
4. The low digit is exactly zero. Divide by B by shifting scratch digits down
   one position and zeroing t[n+1]. Do not discard t[n] before normalization.

Each add-multiply loop uses `z = digit.toLong()*word + t[j] + carry`, then
`t[j]=z and (B-1)`, `carry=z ushr 30`. It must not accumulate several
products into an unreduced Long. Inputs to the loop give
`z <= (B-1)^2 + (B-1) + (B-1) = B^2-1 < 2^60`, and carry<=B-1.
High-digit propagation uses additions only: a digit plus carry is at most
2B-2, followed by a carry of at most one. Conservative intermediate top-digit
addition is at most two. All fit Int, but perform the sums in Long for clarity.

Divisibility: `1+rho*M[0]=0 mod B`; hence `T+a*b[i]+q*m` is divisible by B.
The loop recurrence is

    T_(i+1) = (T_i + a*b[i] + q_i*m) / B.

Its bound is strict:

    T_i + a*b[i] + q_i*m < 2m + (B-1)m + (B-1)m = 2Bm.

Thus every next T is <2m; every unshifted value is <2BR, which fits n+2
radix digits (B>2). At iteration entry t[n]<=1 and t[n+1]=0; the global bound
also proves no carry can escape t[n+1]. After shifting, t[n]<=1 again.
These bounds apply to both accumulation phases, which only add nonnegative
terms. Clearing the high cell and fully propagating carries are mandatory.

Let `b_low(i)=sum(j<i,b[j]*B^j)` and `Q_i=sum(j<i,q_j*B^j)`.
Induction on the recurrence gives `B^i*T_i = a*b_low(i) + m*Q_i`.
At n iterations `R*T_n=ab+mQ_n` with 0<=Q_n<R. Since R is invertible
mod m, T_n has the required residue. Compare T_n and M using n+1 digits
(M padded with high zero); if T_n>=m, subtract m exactly once. T_n<2m proves
the result lies in [0,m). Never compare only the low n digits: the high one
can be significant for m near R. Subtraction uses digit differences in
[-B,B-1], mask and a 0/1 borrow, propagating through the high cell. Its
postcondition is high cell zero. Copy the low n digits to a distinct output.

Inputs may alias each other for squaring; scratch and output must be disjoint
from inputs/context and each other. Rotate result/factor/temporary array roles
between calls; do not mutate an input while the multiplication still reads it.
An internal validation failure is a defect, not an excuse to emit a truncated
residue. Production code must prove its caller preconditions rather than scan
or allocate extra arrays inside each inner multiplication.

## Setup without oversized powers, and conversions

Setup computes R mod m and R^2 mod m by exactly 60*n modular doublings,
starting from ordinary residue 1. Save after 30*n as oneMont, and after
60*n as r2. This is intentionally simple O(n^2) setup, measured separately
later; no division, full 2n product or public shift to R^2 is needed.

For canonical r, compute gap=m-r with bounded n-digit subtraction. If
r>=gap, replace r by r-gap; otherwise replace r by r+r. The latter branch
proves 2r<m<R, so no high carry is lost. The former computes 2r-m in
[0,m). The temporary gap must not overwrite r before comparison; a dedicated
gap slot suffices. Each doubling is O(n) and all residues stay below m.
The loop bound `60L*n` is checked before any Int narrowing (at cutoff <=245760).

Encode ordinary x with `MM(x,r2)=xR mod m`. Decode X with
`MM(X,ordinaryOne)=X*R^-1 mod m`; ordinaryOne is the digit vector [1,0,...].
It can be synthesized by indexed digit access to avoid another permanent
array. Zero encodes/decodes to zero. Encode nm1 once for a private primality
workspace if comparisons remain in Montgomery form. No public `mod` call is
replaced before its required callback stage. For arbitrary signed/unreduced
inputs use the original public reduction routing first, then copy the already
canonical private result; the kernel is not a signed/general reducer.

## Storage and arithmetic limits

Context size depends on modulus n, not exponent bit length, witness count,
the number of operations or the size of ab. Scratch is O(n); never materialize
2n product digits. A conservative allocation budget is 16*n+8 Int cells of
simultaneously live context/workspace-owned array payload, excluding existing
caller objects (including primality n, nm1, d and the current witness), and the
unchanged legacy entry-reduction path. An implementation must
account for context M/r2/oneMont (3n), accumulator (n+2), gap (n), factor,
result and rotated next slot (3n), canonical conversion input (n), encoded nm1
(n for primality only), and both export and normalization-trim copies (2n).
That inventory is 12n+2 cells, leaving 4n+6 headroom; it is a design budget,
not measured heap residency. Reuse those slots during setup; do not keep every
successive result or witness alive. Export copies retained by callers are
caller-owned results, not workspace caches. Object/array headers, platform
runtime overhead and GC retention require measurement and are not included.

At n=4096 the budget is 262,176 payload bytes. Check n+2, slot counts and byte
counts in Long before allocating or narrowing; there are no unchecked 2*n or
30*n calculations on full-domain operands. All scalar products are below
2^60, so common Kotlin Long arithmetic remains exact also on JS/Wasm; do not
coerce multiplication intermediates to Double/JS Number. No unsigned 64-bit
or platform-specific intrinsics are necessary.

The public supported magnitude range remains the existing checked bit-length
range. For n>4096 retain `multiplyMod`'s full-product guard and
`multiplyModuloBounded` fallback. Those algorithms and even-modulus paths
are not redesigned here. This proposal proves the new kernel avoids oversized
products; it does not claim the old full-domain division/inverse routines or
resource-exhaustion behavior have received a new proof. Parent scope is needed
for any pre-existing defect found in those paths.

## Integration and observable contracts

The conservative first seam is inside `oddModPowStored`, after its existing
null check, `power==ONE` equality callback/return, empty receiver shortcut,
stored positive modulus copy, `BigInteger(1,words).mod(m)` and `ONE.mod(m)`.
Keep those entry expressions in the same order. Then select context using only
stored m digits. Keep the old loop as the fallback, unchanged. Exponent digits
and magnitude are read privately exactly as today, including replacement
negative exponents returned by a `negate` override. Do not add virtual exponent
metadata calls. A context loop preserves the existing right-to-left bit order:
result=oneMont, factor=encode(base), multiply on each set bit, square except
after the final bit, decode once. An entry with zero stored exponent bits may
return the existing initialized ordinary result directly, avoiding setup.

Public `modPow` at lines 188-219 remains textually unchanged in the first
implementation. In particular preserve:

* Stored m-sign validation before nullable exponent validation; zero exponent,
  receiver equals ONE/ZERO/-ONE shortcuts and their exact return values.
* Virtual receiver equality/comparison/reduction, exponent equality/negation,
  and modulus `testBit(0)`, `getLowestSetBit` and `shiftRight` routing. A
  subclass can report odd parity for an even stored modulus: reject the context
  internally and run the existing fallback, without an additional callback.
* Private odd power's exponent-one `return this`, empty stored receiver
  `return ZERO`, and final **fresh zero** on non-shortcut computed zero.
  The computed-zero export branch explicitly constructs `BigInteger(ZERO)`;
  `normalized` alone would return the singleton and lose that fresh identity.
  Nonzero exports use `normalized(1,detachedDigits)`, not `valueOf` or a newly
  introduced cache. Do not change the entry shortcut identities.
* m=1: leave the entire existing odd-power path in fallback; public exponent
  zero/one and +/-1 shortcuts still execute first. A mathematical zero result
  does not justify moving validation or callback stages earlier.
* Negative exponents: compute the same positive-magnitude modular power and
  leave the final `result.modInverse(m)` call in place. Preserve nonpositive
  modulus, null, callback failures and missing-inverse exceptions; do not
  change to invert-the-base-first or suppress a callback with an early gcd.
* Even modulus: retain m=2^p*mOdd decomposition, oddBase routing, binary
  exponentiation, both CRT inverse calls, full-product/reduced CRT guard and
  final inverse order. Only its existing oddModPowStored subcall may use a
  context, when mOdd>1 and suitable. Pure powers of two, binary reduction,
  CRT products and their near-range fallback remain unchanged.

No change to public `multiply`, `mod`, `divideStored`, `modInverse` or internal
`multiplyModuloBounded` is proposed. Automatically creating a new context in
every `multiplyMod` call would pay setup repeatedly and is excluded. There is
no implicit context retained across separate public modPow calls; an internal
caller can explicitly reuse a local context for a fixed stored modulus.

### Reuse in primality: separate, constrained integration step

One context can serve all witnesses and their squarings for the same candidate,
but bypassing `a.modPow(d,n)` unconditionally would skip virtual callbacks on n.
First complete the existing certainty guard and `passesPrimeTest` prefix:
`abs`, equality TWO, parity/ONE checks, `n.bitLength`, sign, small-prime
equality/remainder loop, and nm1/s/d creation, in the original order. Keep
the deterministic boundary/bases and random sampling/accept-reject loop exact.

For initial reusable integration require **exact base runtime class**
`n::class == BigInteger::class`, eligible odd stored modulus and exact-base
nm1/d working values (derived from exact-base n by the unchanged methods).
Witness a is a private exact-base value from `valueOf` or `randomBits`.
The runtime-class check does not invoke operand callbacks. An original subclass
whose required `abs` callback has already returned a plain base value may
qualify; if n remains any subclass, retain the whole original witness route,
including `a.modPow(d,n)` and `x.multiplyMod(x,n)` at their original positions.
Do not add an overridable opt-in method or infer absence of overrides by value.

For eligible n, lazily build the context when the first witness is actually
tested, reuse its fixed parameters and workspace for later witnesses, reset
slots before every witness, and compare encoded x to oneMont/encoded(nm1).
Repeated squaring stays encoded; no decode is necessary for its Boolean
result. Preserve the loop `r in 1 until s`, early success/failure and random
draw sequence. A small deterministic base can be >=n; preserve canonical
reduction at the private entry even though typical eligible candidates exceed
the bases. Exact class is a semantic safety gate, not a numerical range gate.
No witness cache, prime recognition, reduced certainty, reseeding, fewer
rounds, cross-candidate cache or unrelated prime-generation algorithm change.
Context reuse across generated candidates is invalid; each modulus gets its own.

This exact-class route avoids observing fewer subclass callbacks or exceptions.
Broader subclass reuse would need a separate callback-by-callback proof and
new tests; keeping the legacy route is the specified fallback for now.

## Later implementation, proof and verification plan (not executed)

1. Parent scopes implementation after reviewing this proposal and independent
   review. Implement a private nested context/workspace with access to stored
   limbs; add no public API and no serialized field. Keep the old loop available
   for explicit eligibility failures. First land only the odd-power seam;
   primality-local reuse is a separately reviewed follow-on of the same scope.
2. Translate each loop above literally, with explicit carry-cell operations.
   Annotate range, congruence, scratch/output alias and single-subtraction
   invariants. Independently review the actual index mapping and all lifetimes
   before execution. Enforce the 16n+8 payload budget by a static inventory.
3. In common tests add direct internal-test coverage without exposing production
   arrays publicly: inverse-word congruence for odd low words 1,3,B-3,B-1;
   exhaustive small odd moduli/residues (3..63), then dense, sparse, alternating
   and all-carry inputs at 29/30/31,59/60/61 bits and multiword boundaries.
   Check encode/decode, MM congruence/range, high-carry normalization,
   at-most-one subtraction, zero, one, m-1, square alias, unchanged input
   snapshots, scratch reuse, output detachment, and different contexts used
   interleaved. m near R is essential to exercise t[n]=1 before subtraction.
4. Force eligibility decisions at n=4095/4096/4097 with own-source test access
   to the factory; do not run a quadratic maximum-size exponentiation merely
   to inspect a guard. Compare old/new on feasible boundary-size one/few
   products in the bounded granted campaign. Test checked sizing using metadata
   alone near `Int.MAX_VALUE`, and prove the unchanged fallback is selected for
   every n above cutoff without constructing enormous fixtures. Reduced-size
   injected test cutoff can exercise the same fallback branch with real values.
   Such checks do not establish physical max-domain execution; retain that
   limitation explicitly. No modulus-size ArithmeticException is introduced.
5. Run existing `IntegerModPowOracleTest` (all value/identity/callback groups),
   `IntegerInverseOracleTest`, `PrimePrefixOracleTest`, `PrimeOracleTest`,
   `PrimalityCertaintyTest`, JVM `BinaryRepresentationConformanceTest`,
   `PrimalityConformanceTest`, `JdkOracleTest`, and Java
   `IntegerModPowNullTest`. The inverse class name was confirmed by source read.
   Add delegating/throwing moduli subclasses reaching
   multiple witnesses; make sure the exact-class fallback keeps callbacks on
   every witness, including bitLength calls during random rejection sampling.
   Include abs returning a different plain value, nullable replacement
   callbacks, false odd/even parity and exponent-one/zero-return identities.
6. Use public JDK APIs only as black-box differential oracles in the later
   granted window, never their implementation source. Cover m=1, odd/even,
   pure 2^p and mixed CRT, residues0, negative bases, exponents0/1/-1 and
   signed dense/sparse exponents, coprime inverses and non-invertible failures.
   Run exact pseudoprime/deterministic-boundary cases and unchanged public
   primality certainty endpoints; a composite may require fixed internal
   test witnesses for reproducibility, without changing production RNG.
7. With a granted execution window and fresh recorded source/JAR hashes run
   focused JVM tests, then repository checks:
   `./gradlew jvmTest jsNodeTest wasmJsNodeTest macosArm64Test` on the matching
   host; run other configured native targets through matching CI hosts.
   Confirm JS/Wasm Long arithmetic and detached outputs, plus JVM API and
   serialization compatibility. No generator/check command is authorized now.

### Measurement and public acceptance gates

An executor must hold the parent's sole window and record source/dirty-tree
identity, Kotlin/JDK/Gradle versions, exact JAR SHA-256, command/heap/deadline,
platform, operands, witness policy, returned values, wall/CPU time, allocated
bytes and owned process terminal status. Never compare a historical JAR under
the label of current source. Separate setup, encode/decode, steady MM,
one-shot modPow and complete repeated-witness primality timings; measure old
and proposed paths with identical operands and reproducible test-only witness
sequences, warmup policy and enough samples to show variation.

Measure odd modulus sizes 1,2,4,8,18,21,43,74,77,108,142 limbs and selected
larger sizes/cutoff neighbors as resources allow, plus even moduli where only
the odd subpath qualifies. Use dense/sparse/carry-heavy random and structured
moduli, not only Mersennes. Sweep repetition counts K=1,2,4,8,16,32,64,128.
Report both setup-inclusive and pre-encoded cases. If S is measured setup plus
conversion cost, Cold/ Cnew are comparable per-operation costs, the elementary
estimate is `K > S/(Cold-Cnew)` when Cold>Cnew; confirm the crossover from
measured totals, and report no crossover where the difference is nonpositive.
Track peak live payload separately from cumulative allocated bytes. Compare
fallback cost around the cutoff and small moduli before choosing any dispatch
threshold; 4096 is a resource cap, not an asserted performance optimum.

Retest all seven original Mersenne inputs 521/607/1279/2203/2281/3217/4253
with public `isProbablePrime(100)`, independent uniform Random.Default witnesses
and unchanged 50 rounds for this probabilistic domain. Public seed559 fixes
RandomFactory operands, not these internal unseeded witnesses. Test-only fixed
witness microbenchmarks are not replacements for the original calls.

Then rerun the retained public BigIntegerTest default (120 s), BigIntegerTest
timeout=400 (400 s), both subset=0, and PrimeTest default (120 s), with 512 MiB,
assertions, unchanged operands/batches and original assertions. The existing
`tools/run-math-tck.main.kts` supports exact `--tests` paths and explicit parent
`--execution-grant`/memory/owned-cleanup arguments; the executor must fill those
from its later grant rather than reusing 1959 or inventing host capacity. Preserve
the full original public methods and their later assertions; per-input diagnostic
completion is not a public pass. Do not waive timeouts or claim JCK certification.
No rerun is authorized by this document. Keep the original failure evidence.

## Proof boundaries and handoff

The recurrence, inverse construction, canonical conversion and scalar/scratch
bounds above are paper arguments. They are not an executed certificate or a
proof that future code faithfully implements the recurrence. Remaining gates:
actual carry/shift/subtract indexing; concrete workspace lifetime accounting;
all callback/identity/null comparisons on the existing suite; multi-target
arithmetic; actual setup crossover and allocation/latency data; and the original
public timeout barriers. No hotspot ranking or end-to-end improvement is proved.
Independent read-only review is required before the proposal handoff comment.
Task state must remain linked/in_progress; no commit, push or review transition.

### Independent review record

Read-only reviewer `/root/review_538_proposal` independently checked the proposal
and relevant own-source contracts under the same no-execution/no-foreign-source
constraints. Reviewed draft SHA-256:
`50e93a91ba2aa28537f04c479defca380850d73aa0c98cd41ef8740921966f8d`.
Verdict: no blocking defect found in the paper recurrence, inverse, conversion,
carry/normalization, sizing/fallback or proposed callback integration. Two minor
clarifications were applied: name the context/workspace ownership boundary of
the payload budget, and spell out the fresh-zero export constructor. Subsequent
parent activity 1964 was added as attributed interim evidence above; it changes
neither the derivation nor the scope. Actual code correctness, test outcomes,
resource residency, crossover and public completion remain unverified.

Owned terminal state for this milestone: one uncommitted proposal document;
no owned compiler/numerical/helper/harness child was launched, and the sole
independent reviewer completed. The task remains linked/in_progress awaiting
later parent scope; no background implementation or execution campaign follows.

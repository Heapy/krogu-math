<!-- SPDX-License-Identifier: Apache-2.0 -->
# Task 538: odd-modulus source implementation and handoff

Status: source-only milestone under `/Users/yoda/.kotgent/prompts/5128683b.md`.
Changes are not committed. Task `local:538` stays linked and `in_progress`.
No compiler, Kotlin/Java/Gradle, numerical helper, script, certificate, fixture,
test, benchmark or public harness was executed. No foreign numerical production
source was read. This document records paper reasoning and prepared test source,
not executable verification, task acceptance, performance or cryptographic claims.

Base HEAD: `96ddbf7b9241f14265620c72a8217c32c4cdb4ff`, branch
`task/538-odd-modulus-context`, worktree `/private/tmp/krogu-math-task-538`.
The historical [proposal](odd-modulus-context-538-proposal.md) is preserved byte
for byte, SHA-256 `ff58d1220b0740700d2190db652beb928ac47f62cc41075574b9bc81ff4913d4`.
Parent's orthogonal 528/566 work has not been imported or altered.

## Implementation boundary

The only production changes are two lines inside private `oddModPowStored`
and the new private nested `OddModulusContext`, including its private workspace
and companion helpers. Public `modPow` is textually unchanged. The existing
private odd entry expressions remain, in order:

1. Nullable exponent check.
2. Virtual exponent equality against ONE and `return this`.
3. Empty stored receiver shortcut returning ZERO.
4. Positive base-class modulus view of stored digits.
5. `BigInteger(1, words).mod(m)`.
6. `ONE.mod(m)`.
7. Stored exponent bit count.

Only then does the new factory run, and only if that count is nonzero. It
accepts stored positive odd m>1 with 1<=n<=4096. Stored parity determines
eligibility even when a public subclass parity callback selected the odd route
for an even stored modulus. Ineligible cases retain the old loop and its final
fresh-zero wrapper. No rejection exception, OOM catch, or numerical retry was
introduced. The original multiply/divide, bounded fallback, public conversions,
serialization, Number/JS diagnostics, decimal helpers, primality and RNG code
remain unchanged. The 4096 cutoff has no measured performance justification yet.

Dispatch is currently eager for every eligible nonzero-bit exponent after the
existing shortcuts. Parent activity 1970 flags a concrete unmeasured regression
risk: a small base/exponent with a large eligible modulus can make the 60n
full-digit doubling setup much more expensive than the legacy tiny-product
path. No arbitrary performance threshold has been chosen. Setup-inclusive
measurements and a later dispatch decision remain required; no universal
speedup is claimed. The legacy fallback remains available.

The context reads stored exponent magnitude/digits in precisely the original
right-to-left order, including a negative stored replacement returned by a
virtual `negate`. Its exponentiation state starts with encoded one, multiplies
on each set bit, squares except after the last bit, and decodes once. A
replacement zero exponent takes the old zero-iteration path. Public negative
exponents still take the original final `result.modInverse(m)` suffix; public
even-modulus splitting, CRT and its range fallback are unchanged.

## Arithmetic proof against actual source

Let B=2^30, M be the copied n-digit stored modulus, R=B^n. All array digits
are nonnegative Ints less than B. Since M is normalized, 1<M<R, and odd M
is coprime to R. `rho` is a Long holding -M[0]^-1 modulo B.

Inverse lifting starts at y=1. At bit k (1 through 29), lower k bits already
satisfy M[0]*y=1 mod 2^k. Adding 2^k to y flips bit k of that product because
M[0] is odd, without changing lower bits. The tested bit determines whether
that addition is needed. Afterwards y<2^(k+1); every product is below B^2.
Thus the final masked B-y is the required rho with no signed overflow.

Setup keeps `r2` canonical, initially 1. `gap` is M-r2, using n-digit
subtraction and explicit borrow. If r2>=gap, in-place r2-gap computes 2r2-M
in [0,M). Otherwise r2+r2<M<R, proving the final doubling carry is zero.
Each loop preserves the bound without public powers/division or a 2n product.
Exactly 30n doublings are copied into `oneMont`, and exactly 60n produce r2.
These become R mod M and R^2 mod M. Their local arrays are never modified
after construction returns; `gap` is not retained in the context.

`multiply` clears n+2 scratch digits before every operation. At the start of
outer iteration i its value T is in [0,2M). `addProduct` adds a*b[i] in a
complete n-digit pass, then propagates its carry into scratch[n] and
scratch[n+1]. A second call adds q*M, where q=(scratch[0]*rho) mod B.
Each inner sum is at most (B-1)^2+(B-1)+(B-1)=B^2-1<2^60;
carry<=B-1. The first high addition is <=2B-2, followed by a carry<=1.
Both phases are nonnegative additions bounded by the complete unshifted sum:

    T + a*b[i] + q*M < 2M + (B-1)M + (B-1)M = 2BM < 2BR.

Consequently no carry can escape the n+2 cells; the top cell stays below B.
The selected q makes the low cell zero. The source copies scratch[j+1] to
scratch[j] for j=0..n, then clears scratch[n+1]. This retains the significant
high cell and divides by B exactly, restoring T<2M and scratch[n]<=1.
With b_low(i)=sum(j<i,b[j]*B^j) and Q_i=sum(j<i,q_j*B^j), induction gives
`B^i*T_i=a*b_low(i)+M*Q_i`. At n steps `R*T=ab+M*Q`, so its residue is
ab/R mod M.

Final comparison includes scratch[n]; a nonzero high cell necessarily means
T>=M. Otherwise `compareLow` scans precisely n cells from high to low.
One subtraction suffices because T<2M. Each difference is in [-B,B-1],
with masking and borrow 0 or 1; the borrow propagates through scratch[n].
The resulting value is in [0,M), hence the high cell is zero before the low n
digits are copied to output. This is a caller-invariant proof, without runtime
assertions that could introduce a new failure path.

Encoding calls MM(x,r2)=xR mod M, where x is the already canonical result of
the unchanged private entry reduction. Decoding supplies ordinary one as the
nullable second-array sentinel, with digit 1 at index zero and 0 elsewhere.
Both operations satisfy canonical multiplier preconditions since M>1.
Canonical zero/one/M-1 and small-high-word exports obey the same proof.

## Aliasing, snapshots and actual payload

Each `pow` call constructs its own lexically local workspace. It copies the
entry-reduced base into a zero-filled n-cell input. The context never retains
the base, exponent, output or workspace. The three original factor/result/next
arrays stay pairwise distinct after either swap: a conditional multiply swaps
result and next; a square swaps factor and next. Both inputs therefore remain
unchanged until `multiply` has finished all reads. Squaring may pass the same
array twice; output and scratch are still disjoint. No array grows inside a
multiply or exponent-bit loop.

Decode writes into the remaining next slot. Computed zero returns
`BigInteger(ZERO)`, preserving a fresh object with canonical empty magnitude.
Nonzero output uses `normalized(1,next.copyOf())`. That first copy detaches
the result, and normalization may make a second trimming copy. It never exports
workspace or context arrays and never introduces a cached ONE result. Original
shortcut object identities remain at their original returns.

| Allocation lifetime | Int cells |
|---|---:|
| Context modulus, oneMont, r2 | 3n |
| Setup-only gap | n |
| Workspace scratch | n+2 |
| Workspace input and three rotating slots | 4n |
| Detached export and possible normalization copy | <=2n |

Setup ordinarily peaks at 4n. Exponentiation including export ordinarily peaks
at 10n+2; even conservatively retaining setup's gap through export gives
11n+2, below 16n+8. Local alias variables and workspace fields reference the
same slots, so they add no digit payload. Caller-retained results of earlier
calls are caller-owned, with no workspace cache. The unchanged entry reduction
and pre-existing caller arrays are excluded, as specified in the grant.
Array/object headers, runtime overhead, dead stack slot retention and GC heap
residency have not been measured; this is a simultaneous owned-payload bound.

Factory resource arithmetic is Long before allocation or narrowing: n+2,
60n, 16n+8 and 4*(16n+8). At n<=4096 these are at most 4098, 245760,
65544 and 262176 respectively. Workspace's later Int size+2 is safe by that
same invariant. For n=4097 and all larger supported n, the cutoff returns
null before any new context array or setup loop. Zero/negative/even/unit
stored moduli also return null before allocation. The extra range-check
failure branches are unreachable under the current cap; they are defensive
resource guards for a future changed cap, not separately covered public branches.

## Prepared tests and finite coverage boundaries

No new test-hook API or reflective access to private implementation was added.
All new tests enter through public APIs. Their names start with
`OddModulusContext538` and they remain uncompiled/unexecuted:

| Source | Independent checks prepared |
|---|---|
| common `OddModulusContext538Test` | Bounded scalar oracle for every residue of every odd modulus 3..63 at exponents 2/3/5; repeated full-product/remainder oracle; carry-heavy/near-R and small-high-word moduli; zero/one/M-1; signs, m=1, odd/even CRT and pure powers of two; exponents 0/1/negative, noninvertibility; stored exponent boundaries; squaring, public self aliases, mixed contexts, snapshots/detached exports; null precedence, callback traces, replacement exponent values, shortcut and fresh-result identities |
| JVM `OddModulusContext538JvmTest` | Public host numerical API comparison for deterministic bounded random 2..512-bit moduli, both signs, odd/even/power-of-two routes and inverse failures; low-word inverse patterns through 5 limbs; sparse/dense exponents through bit 90; output snapshots |
| JVM Java `OddModulusContext538NullTest` | Null arguments, null negate with required intervening public reduction, null mod return and null even-split callback |

Two common cases have derived final-subtraction witnesses, not a branch-coverage
claim. For M=R-1, encoding x=M-1 gives A=R-2. Squaring has Q=4, raw T=R,
and a significant high cell; subtraction returns 1. Tested source sizes are
1/2/3/4/8 limbs. For n=1, K=B/2, M=K-1, x=M-1 encodes to A=K-3.
Then A^2=K^2-6K+9, Q=K+9 gives raw T=K+1=M+2<B, exercising subtraction
with high cell zero and canonical encoded result 2. Decode gives ordinary 1.
Non-subtracting/zero cases and both setup doubling branches follow the general
proof and prepared numerical matrices; they have no executed coverage evidence.

The n=4097 public fallback case uses sparse M=B^4096+1, a=2, e=2, expected
4. Entry and fallback arithmetic involve only small residues, so it avoids
quadratic setup or large full products. At n=4096 the eligibility and resource
boundary are covered by the source proof above only: running 60n doublings on
n digits would be an intentionally expensive test and is not in this suite.
This same limitation applies to n=4095. The public supported-range extreme and
legacy near-Int.MAX_VALUE arithmetic fallback remain unexecuted obligations;
no giant allocation witness is prepared. Common negative-exponent congruence
checks use original inverse machinery; JVM public host comparison supplies an
independent expected value. Arbitrary independent kernel input pairs and direct
parameter round trips are proved here but not separately exposed by a kernel
test seam. A minimal internal JvmSynthetic seam would require parent approval
before any future addition, if direct kernel tests become necessary.

Existing `IntegerModPowOracleTest`, `IntegerModPowNullTest`, modular fixtures,
primality prefix/certainty tests and all public acceptance fixtures are preserved.
They must be rerun in the parent's later execution window alongside the new
tests and common JS/Wasm/native targets. Future execution should begin with
compilation and these bounded suites, without a generator or certificate run.
The current milestone does not authorize even a tiny numerical helper.

## Exact later primality seam, deferred

No primality code changed. A later scope may reuse a single local context only
after the unchanged certainty/abs/equality/parity/ONE/bitLength/sign/small-prime
prefix and nm1/s/d construction. Require exact base runtime classes for the
working candidate and nm1/d, and keep subclass candidates on the full existing
`a.modPow(d,n)` / `x.multiplyMod(x,n)` route. Initialize lazily at the first
actual witness, never across candidates. Preserve witness selection, random
draw/rejection sequence, deterministic boundary/bases and all round budgets.
Reduce the witness through the existing private entry, reset workspace slots,
encode nm1 once, compare x to encoded one/nm1, and keep repeated squarings
encoded with the original `1 until s` success/failure order. No public callback
may be skipped before the exact-class eligibility gate. This needs separate
scope, implementation review, callback proof, tests and execution approval.

## Evidence and remaining obligations

Parent-supplied profile evidence is terminal, not an execution grant: normal
521/607/1279 calls returned TRUE and four other fixed-60s calls timed out;
fresh p2203 profile returned TRUE in 70.642865292s, CPU 68.186054s, with
818625623872 current-thread allocated bytes. Of 319 raw samples, 318 contain
own math; those 318 contain multiplyMod/mod/remainder. These observations
support investigating repeated generic reduction, without per-site allocation
attribution, a benchmark ratio or fully seeded internal witnesses. The host
0.0816785s diagnostic is descriptive, not a controlled comparison. Evidence:
`/private/tmp/krogu-primality-profile-root1959-v2`; parent report
`/Users/yoda/dev/pet/krogu-math/docs/primality-profile-runtime.md`.

Original public task 559 timeout failures remain failures. Later acceptance must
retain original public inputs, certainty100/round budgets, batches, assertions,
512 MiB and the original 120/400s deadlines. Context setup, encode/decode,
steady multiplication, one-shot modPow, repeated-witness primality, crossover
and allocation/latency measurements remain outstanding. No speedup, public
pass, measured residency or target correctness is claimed here.

Independent reviewer `/root/review_538_source` inspected actual production
loops/indices/carry/lifetimes and observable integration read-only. Production
SHA-256 reviewed: `86046c92c12c84071a543655d13a1d7aa0352729a5ae64823e1fc6ad6f24a75a`.
Initial verdict: no blocking source defect; confirmed recurrence/shift/high-cell
handling, inverse/setup, role rotation/export, payload bound and unchanged public
routing. Compiler behavior, tests, platform arithmetic, performance, physical
residency and public timeout completion remain unverified. Test/proof source
review and final source-freeze metadata follow.

### Independent test/proof review and freeze record

The same independent reviewer subsequently inspected all three new test files
and this document, finding no blocking expected-value, callback/identity,
compile-shape or workload-bound defect by source inspection. Both subtraction
witnesses were checked algebraically. A final bounded scalar-oracle addition
was separately reviewed: 3,069 one-limb public calls cover all residues modulo
every odd integer 3..63 at exponents 2/3/5, with scalar products <=3844.
The reviewed proof/coverage draft, before this metadata footer and the parent
activity 1970 risk note, had SHA-256
`946ec67d5222859c17cee5160fd3d5416559095c6d5f205d23a6bd3ea0873285`.

Frozen reviewed code SHA-256:

| File | SHA-256 |
|---|---|
| `BigInteger.kt` | `86046c92c12c84071a543655d13a1d7aa0352729a5ae64823e1fc6ad6f24a75a` |
| `OddModulusContext538Test.kt` | `b4da048cc1570a709f60e47a4c1b51dc15da0b6a7f4689f28fe2b4d5ad1ecfbc` |
| `OddModulusContext538JvmTest.kt` | `99cc2da61d12ca470d7da343c035540e22babfe6499eae4004e44c0de04a8b60` |
| `OddModulusContext538NullTest.java` | `2ebc1786c1bf3285556d7a1a9831a944689c46aca287a30f6b82e6b73e4536c5` |

Remaining review limits are explicit: direct arbitrary kernel-pair, isolated
inverse-congruence and encode/decode execution are absent; eligibility at
4095/4096 words and public maximum-domain behavior are source-proof obligations.
There is no executed compiler, branch-coverage, target, residency or benchmark
evidence. Ordinary source/status/SHA-256 and Git whitespace checks are the only
validation performed here. `git diff --no-index --check /dev/null <new-file>`
emitted no whitespace diagnostics (exit 1 denotes the new-file difference);
tracked `git diff --check` emitted none and returned zero.

Owned terminal state: all source/metadata invocations are terminal, the sole
read-only reviewer completed, and no owned compiler/numerical/helper/script/test/
benchmark/fixture/harness process was launched. No Git mutation, commit, push
or task-state transition occurred. Code is frozen at the hashes above; the
final document hash is recorded in the ref-less task 538 handoff comment.
Task remains linked/in_progress, awaiting parent scope. No background campaign
or later primality integration follows from this source-only milestone.

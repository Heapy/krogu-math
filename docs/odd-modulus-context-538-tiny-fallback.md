<!-- SPDX-License-Identifier: Apache-2.0 -->
# Task 538: proved tiny-power fallback, source-only milestone

This follow-up implements parent activity 2022 and
`/private/tmp/task538-tiny-context-guard-source-grant.txt`. It changes only
private `oddModPowStored` context selection, adds the separate common class
`OddModulusContext538TinyFallbackTest`, and adds this proof/handoff.
Changes are not committed. Task 538 stays linked and `in_progress`.
All new code and tests are **UNCOMPILED/UNRUN**. No script, compiler,
numerical/helper/certificate/probe/test/build/measurement ran in this scope.
The next runtime window belongs to the parent's separately coordinated 567 work.

## Preserved history and the narrow change

Before edits, a new read-only snapshot was made at
`/private/tmp/krogu-538-tiny-guard-source-41bf2445-v1/originals`.
It contains the six accepted source/test/proof artifacts and complete copies of
all four prior runtime, V1 pilot, V2 preparation/evidence and V2b directories,
including their scripts, JARs, raw logs, caches and indices. Copy comparisons
were empty, all original indices passed, and the 13,360-file snapshot manifest
is SHA-256 `a4e8876ee2596cb35f2d414169d8a8be3163fcbaf0774c9e151be12db60f13b1`.
All snapshot files/directories had write bits removed before source edits.
The original historical test/proof files are not rewritten to imply a later state.

The prior `BigInteger.kt` hash is
`86046c92c12c84071a543655d13a1d7aa0352729a5ae64823e1fc6ad6f24a75a`.
Its former context decision was `bits == 0 ? null : create(m)` in Kotlin form.
The new selection additionally skips creation for either of these stored facts:

- The privately reduced base has zero sign or one digit equal to one.
- The exponent magnitude has exactly one radix digit, and
  `digit.toLong() * base.magnitudeBits().toLong() <= m.magnitudeBits().toLong() - 1`.

These decisions occur **after** the unchanged nullable exponent check,
virtual exponent equality/receiver shortcut, empty stored receiver shortcut,
stored positive modulus view, private base reduction, `ONE.mod(m)`, and
stored exponent bit count. The old multiply/reduce loop and final fresh-zero
return are byte-identical. No early ZERO/ONE/base return was added.
No public dispatch, context factory/setup/kernel, multiplication/division,
primality/RNG/certainty, range ceiling, field/cache or serialization changed.
There is no new size cutoff or measured crossover heuristic.

## Sufficient-condition proof against the actual loop

For supported reduced `a > 1` and positive stored `m > 1`, let
`b = magnitudeBits(a)`, `q = magnitudeBits(m)`, and let `e` be the exponent's
stored nonnegative magnitude. Then `a < 2^b` and `2^(q-1) <= m`.
If the one-digit exponent satisfies `b*e <= q-1`,

    a^e < 2^(b*e) <= 2^(q-1) <= m.

For a nonzero exponent, the old loop's initial factor is `a^(2^0)`.
Before iteration i the factor is `a^(2^i)`, while the accumulator is a power
with exponent equal to a subset of the already processed low exponent bits.
A conditional accumulator product therefore has exponent at most e.
A square runs only when `i+1 < bits`; its exponent is `2^(i+1)`, at most
the highest set-bit value and therefore at most e. All full intermediate
products are thus at most `a^e < m`, so each original `.mod(m)` uses the
existing smaller-than-modulus path. This justifies avoiding the quadratic
context setup without changing the loop or claiming it is always fastest.

The single exponent digit is in `[0, 2^30-1]`; supported magnitude bit lengths
are at most `Int.MAX_VALUE`. Converting **both factors before multiplication**
gives a nonnegative Long product strictly below `2^61`. The right-hand
subtraction is also Long. No Int product, narrowing, logarithm, floating-point
comparison, allocation, virtual metadata call or new rejection is introduced.
For a zero stored exponent the prior zero-iteration path remains selected;
the sufficient inequality need not supply a separate e=0 proof.

For privately reduced zero or one, all old loop factors/results stay zero/one
(or the pre-existing unit-modulus zero). Context preparation supplies no
necessary arithmetic. The loop is still run for the same number of bits;
this change does not introduce cached singleton returns for computed results.
Zero sign is the stored canonical-zero fact, not a virtual equality result.
The one test reads the reduced base's stored digit; no public `equals`,
`signum`, `bitLength`, `testBit` or arithmetic override is consulted by the guard.

## Signs, unit modulus, callback and identity boundaries

The public negative-input reduction is unchanged. Its result is copied to the
same private positive stored-base view and reduced again before the guard.
For example, `-15 mod17 = 2` qualifies, while `-2 mod17 = 15` does not satisfy
the sufficient bound for exponent two; both still return four.
The exponent guard reads the same magnitude digits as the old loop, even if
a virtual `negate` returns a still-negative replacement. It does not inspect
that replacement's sign or call its metadata overrides. The unchanged public
negative-exponent suffix computes `result.modInverse(originalModulus)`,
preserving noninvertibility, inverse callbacks and exception behavior.

An equality override that makes `power == ONE` true still returns the
receiver before any of the entry reductions or new guard. A stored-zero
replacement still takes the old zero-bit loop and final inverse suffix.
An empty stored receiver still returns the pre-existing ZERO shortcut.
Public zero/exponent-zero/base-one identities remain in their original order.

For stored `m=1`, private reductions produce zero before the guard. The new
trivial-base branch runs the same old loop that the factory's existing unit
rejection selected, retaining its final fresh-zero wrapper. A public shortcut
or public base reduction that already produces cached ZERO remains unchanged.
The prepared tests distinguish both paths without adding a private hook.

A modulus subclass may lie about parity and send stored even digits to the
odd method. The new guard uses the same privately reconstructed digits; if it
skips the context, it uses exactly the fallback that the unchanged factory
would already choose for even m. A source subclass may suppress public
comparison/reduction: the private reconstruction/reduction still happens in
the original order. Exceptions or nulls from earlier public callbacks keep
their existing order because the guard runs later. No new validation or
supported-domain extension is claimed for malformed or impossible stored
representations returned through unusual callbacks; the old entry operations
still run before this selection. Retained null/callback/range tests stay intact.

## Three prepared bounded common methods

The new class is separate from every accepted test file. Its three `@Test`
methods prepare 36 public `modPow` calls, using literal expected values and
the existing public API only:

| Method | Finite evidence prepared |
|---|---|
| `trivialTinySignsIdentityAndDispatch` | 19 calls: zero/one/exponent-zero/one and m1 identities; ordinary tiny values; negative bases and inverse exponents; noninvertible inverse; forced private reduction to zero/one and fresh computed identities; negative/zero exponent replacement; exact callback trace with forbidden virtual length/bit/arithmetic hooks; lying equality/parity controls |
| `sufficientBoundaryLiteralControls` | 14 calls: powers 4²=16, 8²=64 and 5³=125 with modulus bit bounds immediately over/at/below the sufficient product; actual reduction controls 125 mod65=60 and 729 mod17=15; radix exponent boundary controls from 2^8=1 mod17, giving 9/1/2 for exponents 2^30-1/2^30/2^30+1 |
| `upperEligibleTinyExponentsSkipSetup` | 3 calls: n=4095/4096/4097, m=2^(30(n-1))+1, a=e=2, literal result4 and unchanged modulus bytes. The only fallback products are 1*2 and 2*2; no upper-size large-exponent case or forced context setup is prepared |

The boundary cases intentionally include a conservative false-negative: a
small power may still be below m when the bit inequality fails. No maximal
dispatch claim or private branch-coverage measurement is made. The large
cases would expose a missing guard by hitting the former expensive setup,
but none is run here and no test timeout is waived. Maximum-domain and
general upper-size context arithmetic remain separate proof/runtime limits.

## Adverse evidence and later gates remain visible

The accepted V2b campaign used old source `86046...` and fresh matched JARs:
candidate `429e2658543d4d6ad57a689684917a2195126859438d76882fe9445cca0910d8`,
baseline `daf2a13324f780b4b592e946c24dc601fc678d37f4954f0907fdbaea02674bdd`.
Its complete 52-entry evidence index is
`b23fa5fd015a2ba3d4c8425b999c8b42bf0f11f900c69e465b0a463476860fae`.
All eight values matched; these were K1/warmups0/samples1 cold observations:

| Old-source case | C/B wall | C/B CPU | C/B thread allocation |
|---|---:|---:|---:|
| tiny n1 | 1.8608137989437903 | 1.8381502890173411 | 1.5173201370384468 |
| tiny n142 | 13.03176152866242 | 13.520084566596195 | 1.5703844689760182 |
| ordinary n18 | 0.05412878318967939 | 0.06461589747388695 | 0.0004982640391586476 |
| ordinary n142 | 0.06271426811277833 | 0.06535315459757651 | 0.000012428640408782611 |

The tiny regressions remain historical facts. Nothing here measures whether
the new guard improves them. The eager 60n full-digit-doubling setup remains
quadratic for eligible cases not covered by this sufficient guard, including
small powers that conservatively fail it. There is no universal speedup,
statistical acceptance, crossover/threshold, isolated setup/kernel,
shared-context amortization, peak residency or uncontended-host claim.
Historical V1 nullable-counter compilation failure and V2 pre-Java sandbox
metadata failure remain preserved, with zero samples in both.

Later execution requires a **new grant**, all retained JVM/JS/Wasm/macOSArm64
selections plus the new three common methods, exact source-derived test
inventories, and new matched candidate/baseline builds with origin checks.
The historical 373+240+240+240=1093 passes concern the old source only.
If the inventory stays otherwise unchanged, three additional common methods
would add three per target; this is a preparation count, not an executed total.
The old measurement driver's 429e JAR pin cannot serve as this new source's
artifact evidence. A later narrow, reviewed measurement/provenance repin is
needed; no driver, supervisor, JAR or raw evidence is altered now.
Original public559 inputs/certainty100/rounds/batches/assertions/512MiB and
120/400-second deadlines remain unchanged, and its original failures remain open.

## Source handoff

Only normal CLI source reads/copies/hashes/diffs/status/whitespace checks and
patches occurred. No Python or other metadata/custom script was run in this
source scope; no numerical/compiler/runtime mutex was acquired or consumed.
The earlier Python preparation acknowledged in the V2b handoff remains honest
historical evidence. No other worktree's changes were imported or reverted.
The ref-less task comment records final source/test/proof/diff hashes,
independent read-only review, freeze verification and terminal call state.
No compiler or test result is claimed by source review. All preparation calls
and the reviewer must be terminal before handoff; no background campaign follows.

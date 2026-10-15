<!-- SPDX-License-Identifier: Apache-2.0 -->
# Task 530 source-only handoff

Task `local:530`, session `5c644211`, branch `task/530-limb-division`, worktree
`/private/tmp/krogu-math-task-530`. HEAD remains
`bbdd2f4a7c3d3729f2597205b794f2853cc28e67`. Changes are not committed.
The task remains linked and `in_progress`; no review/done transition was made.

This milestone implements the independently derived normalized radix-2^30
multi-limb branch of private `divideStored`, with one new private normalization
helper. The original public entrypoints, validation/exception ordering,
smaller-dividend shortcut and one-limb fast path are unchanged. Scratch arrays
belong to one call, input arrays are read only, and output arrays stop mutating
before construction. No per-quotient-position object/array allocation is added.

This is ready for parent SOURCE review only. No Kotlin, Java, compiler, Gradle,
JAR, fixture generator, script, numerical/symbolic helper, test, benchmark or
public harness execution occurred. No mutex was acquired/released. No foreign
numerical production implementation bodies were read or used. Runtime hold
continues until a distinct explicit parent grant; no numerical correctness,
compilation, performance acceptance or task completion is claimed.

## Exact baseline and transfer artifacts

Before editing, exact original production files were copied into
`/private/tmp/division530-source-original-5c644211/` and made read-only with
`chmod a-w`. Their SHA-256 values matched the parent pins:

| Archived original | SHA-256 |
|---|---|
| BigInteger.kt | `ff80e3cd25c162daed68e5723bd1bbfb277188e57594010b589b1ec6e038cae6` |
| BigDecimal.kt | `9cbaef673f966579e52f44bb9f6bc7c3b01f3ef9adae412f9d96c86c063a1446` |
| NumericExtensions.kt | `20fdf7c5c76ed4207c03859b4cb60247a16e2ec4337ee674c3a85ce6101e5123` |

The [narrow production patch](division530-source.patch) is a unified diff
against that exact BigInteger archive, **not** published Git HEAD. It contains
one hunk: the former binary multi-limb branch and the new private helper.
The accepted 528 metadata, 538 context/guard and 567 decimal composition remains
in place. BigDecimal and NumericExtensions still have the original hashes above.
No other pre-existing file was edited, staged, reverted, committed or pushed.

| Changed/new artifact | SHA-256 |
|---|---|
| src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt | `590d93919ce590b098708a5f567730fca2374cd8bf5d027f7164974203c99607` |
| src/commonTest/kotlin/io/heapy/krogu/math/Division530Test.kt | `8bc7853264804970a7bfcada861294022d9f2b06a05b7a994e1689581f6be2a0` |
| src/jvmTest/kotlin/io/heapy/krogu/math/Division530JvmTest.kt | `4642ffbd0ebf050eadfe518050b4ebcb7ed32dd1401da52418a40a129a84ca57` |
| docs/division530-proof.md | `29726dd6473f79aac5fb267cfb599227077eddb2e5581c358c1870b5021fded3` |
| docs/division530-source.patch | `50e2dbe644d06d7e2209a65cdd9538409b9ed517f4c4274aba7c46d9ef965870` |

This handoff's own hash is reported separately so it does not self-reference.
All new Kotlin/document/patch artifacts are source text; none is a generated
numerical fixture or executable helper script.

## Evidence and independent review

Allowed checks performed: ref-less `kotgent task show` and project inspection;
Git root/status/branch/HEAD reads; named source/test/build/skill reads; exact
source archive/copy and SHA-256 checks; unified diff inspection; and
`git diff --check` (exit 0, with the pre-existing gradlew.bat CRLF notice).
No Git mutation or task-state transition was performed.

An independent read-only reviewer checked the production source using terminal
reads/diffs/SHA and paper reasoning under the same runtime/foreign-source hold.
The reviewed initial production hash was
`68c996e5d8096f7f0f0acc2494116fd0e3b5062ad8ef9d812efe9189dff7b4b3`;
the only subsequent production edit adds the explicit U<B*V invariant comment.
That pass found no implementation defects in normalization, estimate correction,
borrow/add-back, signs, maximum-domain indices or ownership/dispatch. A second
source review of proof/tests caught a primitive MIN/-1 overflow in the proposed
scalar test oracle. It was corrected by excluding that scalar pair and checking
its expected +2^63 quotient with a literal. These reviews are static evidence,
not compilation or executable validation.
The second reviewer then confirmed that correction, all four algebraic witness
outputs, and the explicit residual bounds, finding no further concrete errors
in the proof or common/JVM test sources.

## Prepared tests, all UNCOMPILED and UNRUN

[Division530Test](../src/commonTest/kotlin/io/heapy/krogu/math/Division530Test.kt)
uses bounded primitive Long / and % oracles across limb boundaries, a literal
large quotient/remainder, four sign combinations, and algebraically derived
inputs with known outputs. Specific witnesses cover one/two estimate corrections,
a raw estimate of B+1 capped to B-1, and the n=3 subtraction add-back. It also
covers all normalization shifts 0..29, a normalization top carry, exact/zero/
equal/unbalanced inputs up to modest 4096-bit patterns, preserved one-limb
cases, immutable backing alias cases, detached results, zero-divisor messages,
stored-operand subclass bypass and gcd/modInverse/decimal consumers. It does not
inspect or mirror the production digit algorithm to calculate expected outputs.

[Division530JvmTest](../src/jvmTest/kotlin/io/heapy/krogu/math/Division530JvmTest.kt)
is a later public-API differential source against `java.math` only. It requires
Liberica/BellSoft runtime `25.0.4+9` (allowing a hyphenated vendor suffix), uses
fixed seeds and bounded 1..8000-bit inputs, checks divide/remainder/pair byte
results and recomposition, and compares gcd, inverse and decimal rounded results.
It does not read or copy reference implementation bodies. The library build
pins remain Gradle 9.8.1 / Kotlin 2.4.21 / Liberica 25.0.4+9.

All existing outputs/fixtures/failures are retained. Existing common/JVM
integer division dispatch and nullable callback tests are unchanged and must
also run later. No maximum-size speculative execution is proposed.

## Limits and next decisions

The [proof](division530-proof.md) gives the radix/Long bounds, normalization
and carry argument, qhat<=q+2, corrected qhat<=q+1, single add-back, exact
q*b+r=a, signs, zero/one-limb/unbalanced handling, Int.MAX_VALUE-bit array
bounds, ownership/detachment and consumer assumptions. Its representation
premises are the existing canonical stored-limb invariant.

Four local arrays total 2m+n+2 Int cells for m dividend and n divisor limbs.
A conservative bound allowing trimmed exports is 3m+n+3 cells, without any
maximum-size allocation or memory-success claim. Maximum-domain capacity and
target heap budgets remain open; normal platform OOM behavior is retained.

Quotient-only/remainder-only variants remain OPEN: current callers still
receive both outputs. A private selection could skip quotient storage/export
for remainder-only or remainder export for quotient-only, but public/caller
wiring is deferred for parent scope/dispatch review. This milestone therefore
does not satisfy that part of the complete task's acceptance criteria.

After parent source review, a distinct explicit runtime grant is required for
compilation and tests. Then preserve/compare all public dispatch/null/identity/
exception fixtures, common and pinned-JDK arithmetic tests, gcd/inverse/decimal
consumers, and target gates; resolve the result-selection design; and measure
time/allocations with equal-size, one-limb, unbalanced and inverse-heavy inputs.
O(m+n+(m-n+1)*n) source work and removal of immutable bitwise intermediates are
structural observations, with no measured speedup or allocation delta yet.

The parent's prior four allPASS gates and current public-harness timeout are
context only. No historical gate is represented as validation of this edit,
and no timeout is attributed to division without measurement.

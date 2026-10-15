# Task 567 source-only handoff

**UNCOMPILED / UNRUN. Changes are not committed.**

Worktree `/private/tmp/krogu-math-task-567`, branch `task/567-zero-context-scale`,
HEAD `bbdd2f4a7c3d3729f2597205b794f2853cc28e67`. Keep `local:567` linked and
`in_progress`; this handoff does not complete runtime acceptance or grant a
runtime window. No Git mutation, commit, push or task transition was performed.

## Changed files and hashes

All hashes are SHA-256:

| File | Hash |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt` | `7214a0fda8a4ae8a6836829c2bf74fd9dfeb671e01b41f8e7ada33732b9b68d1` |
| `src/commonTest/kotlin/io/heapy/krogu/math/ZeroContextScale567Test.kt` | `736472d71a2b216b1e367fbc220ec648dd11f2069bc63a19f86102cb4483bc59` |
| `docs/zero-context-scale-567-proof.md` | `5054a2a9110d41c8ad4f8226c1967ee9b67da5f23eb277e40166d2351cd1c4ac` |
| `docs/zero-context-scale-567-handoff.md` | This handoff's final hash is recorded externally in the task comment to avoid self-reference. |

Source change: 16 insertions, 10 deletions. The other three files are new.
No existing tests, oracles, fixtures, generators, frozen 538 diagnostics, 541
inventory, BigInteger, metadata helper, serialization or public ABI declarations
were edited. No other worktree source was imported.

## Reviewable behavior

- Direct ordinary/truthful contextual plus/round selects coefficient 0, scale 0,
  canonical text `0`, including precision 0. Result identity is not promised.
- Existing context validation and precision reduction precede zero selection.
  `roundValue`'s numerical body is unchanged, and nonzero results pass through.
- No-context plus still returns the original input and scale. Public round still
  dispatches virtual plus. Caller-owned overrides and explicit super entry
  points keep their replacement/exception semantics.
- Constructors and owned add, multiply, divide and pow results use private
  precision rounding to preserve their independent scale rules. The complete
  inventory, including unchanged callers, is in the proof.
- Contextual abs/negate retain virtual delegation and intentionally permit
  `0@0`; their specific public contracts do not mandate a zero cohort.
  No-context abs/negate scale guarantees remain intact. This choice is documented
  separately, without importing plus's obligation through its call chain.

The proof lists all nine prepared common test methods, literal expectations,
scope and boundedness. Direct zero controls span seven scales (MIN through MAX),
four precisions (0, 1, 7, MAX), all eight modes and base/truthful receivers.
Other controls cover nonzero exact/midpoint/carry, null/no-context/override/super,
constructor scale, cancellation, zero products/division, pow and unary dispatch.
No JVM-specific new test is needed for these nullable common Kotlin entry points.

## Independent reviews and source checks

1. `/root/contract_review`: clean read-only public-contract/caller audit;
   supported direct zero normalization, owned-result seams and the independently
   permissible contextual unary choice. Terminal; no writes or execution.
2. `/root/final_source_review`: **no source-review blocker found**; reviewed
   complete diff/test/proof, confirmed source and test hashes above, checked
   literal rounding tables, bounded metadata cases and compile plausibility
   by inspection. Terminal; no writes or numerical execution. Its only noted
   proof line typo (zero dividend 266 -> 265) was already corrected in the
   final proof hash above. No production/test revision followed this review.

Ordinary status/diff/hash/whitespace reads were performed. Tracked diff
whitespace check passed. Such checks are not compilation or behavioral proof.
No compiler, script, numerical helper, certificate, probe, generator, test,
benchmark or harness was executed or left running by this worker or reviewers.

The historical raw observations file remains at
`/private/tmp/krogu-541-Z1-538-1991-6d0b1536-v1/observations.tsv`, unchanged SHA-256
`ec2f3c0e865edf2e644a0e253e33474373b6443fe5e7bb025654dcc809609cce`.
No candidate black-box outcome is claimed. The supplied host behavior remains
documented deliberate divergence from the direct public zero-scale contract.

## Parent-owned remaining gates

After separate explicit scheduling authorization: compile the candidate;
produce and identify a fresh artifact; execute the bounded common controls;
classify actual retained-oracle failures; permit only independently justified,
narrow field-level oracle adaptations; run the full retained JVM/JS/Wasm/
macOS ARM64 suites; then run all supported CI targets at the exact final HEAD.
Do not rewrite raw host evidence, normalize all zeros globally, or assume any
unexecuted test passed. An idle runtime window does not authorize execution.

No remaining source-review blocker was identified. Runtime behavior, retained
suite compatibility, the precise failing oracle rows and exact-head CI remain
unverified. The contextual unary cohort choice is explicit, permitted, and
requires classification of any retained comparison failures by the parent.

Requested notification target: parent thread
`01a13482-0bb7-7752-b638-2ea87e5bcafb`. Queue delivery status is reported in the
worker's final response; queue acceptance does not imply parent processing.

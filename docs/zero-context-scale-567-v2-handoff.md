# Task 567 V2 source handoff

**UNCOMPILED / UNRUN. Changes are not committed.**
Native worker `9146edda`, parent native `af5f5ab4` / task526,
parent Codex thread `01a13482-0bb7-7752-b638-2ea87e5bcafb`.
Worktree `/private/tmp/krogu-math-task-567`, branch `task/567-zero-context-scale`,
unchanged HEAD `bbdd2f4a7c3d3729f2597205b794f2853cc28e67`.
Task `local:567` stays linked/in_progress; no Git mutation or task transition.

## Finite source result

- Preserve direct contextual plus/round0@0, including precision0; no-context
  plus/self/scale and explicit virtual calls remain unchanged.
- Contain contextual unary cohort changes with the requested private stored-zero
  precision seam. Initial abs sign/negative negate(mc), negate() first, nonzero
  replacement plus callbacks, exact return/exception identity and null ordering
  remain. This is the parent-selected compatibility choice, not another spec bug.
- Double(mc) copies the existing canonical factory coefficient/scale through the
  existing precision-only context constructor, retaining signed-zero0@1. Float
  and original migration assertions remain unchanged.
- Three owned base sqrt result rounds use roundValue; earlier source/normalized
  virtual callbacks and raw H/sqrtPrecision assertions remain intact.
- Narrow direct-zero adaptations only: Portable D5/L160/S48/C6, compact frozen
  and live scale round fields256 each, ordinary direct round160 in the named
  multiplication suite, truthful round48, truthful direct unary plus6. Original
  raw values/flags/traces remain diagnostics; counters guard those exact scopes;
  compact cursor/comparison final139239 remains.
- Common test now has twelve methods: original nine names, revised unary control,
  and three new methods for stored-zero/internal-vs-explicit dispatch, unary
  null/ordering/super controls, and signed floating conversion cohorts. Every V2
  assertion is unrun. Existing V1 midpoint/constructor/arithmetic controls remain.

The unchanged V1 proof/handoff are historical. The detailed current proof is
`docs/zero-context-scale-567-v2-proof.md`, including literal coefficient/cohort
derivations, exact finite predicates and unresolved key sets.

## Unresolved assertions are intentionally still active

No full-suite pass is claimed or predicted. The proof enumerates exact keys:

- S/DecimalRound untruthful precision: source-selected zero keys160 plus20
  nonzero inputs yielding zero intermediates. No poisoned metadata waiver.
- C/DecimalUnary poisoned direct plus sign±1:12 keys.
- C/DecimalUnary absContext storedzero/sign0 or1:12 trace-only differences from
  bypassed internal plusContext. Cohort/self/base are preserved by the new seam.
- Portable nullAbs storedzero/sign0 or1:8 keys (return/self/throw/super), changing
  internal callback outcomes/traces. No corresponding adaptation.
- Previously passing Java DecimalNullUnarySubclassTest has10 analogous keys
  (adds null-return behavior). Its original assertions remain untouched, so a
  new V2 method failure is source-predicted. This is an additional consequence
  of the specifically selected seam, not an executed result.

Parent must classify each internal callback observation or revisit that choice;
this source milestone neither suppresses those comparisons nor broadens the
initial direct-zero predicate to force a green suite. Explicit calls to the
same overrides remain required and are covered separately.

## Preservation and hashes

Unique pre-edit read-only snapshot:
`/private/tmp/krogu-567-v2-original-9146edda/`, four exact V1 artifacts, hashes in
the V2 proof. Prior runtime worker9974b07a is stopped/archived; release and empty
mutex evidence read. Its immutable first-runtime directory/log/XML/inventory/
JAR/inputs were untouched. V1 115suites391methods=382pass9fail, all9newpass;
those results do not validate V2.

Final source SHA-256:

| File | Hash |
| --- | --- |
| BigDecimal.kt | `9cbaef673f966579e52f44bb9f6bc7c3b01f3ef9adae412f9d96c86c063a1446` |
| NumericExtensions.kt | `20fdf7c5c76ed4207c03859b4cb60247a16e2ec4337ee674c3a85ce6101e5123` |
| ZeroContextScale567Test.kt | `662cc1f3c913ddfa0d44c481b2c38548419fc1a2a5887e5abe09ec03ef647f04` |
| PortableOracleTest.kt | `f522ed277c79c0b73cb7f1a9e07893602626cf1a1a92459faa60ff5424ad2580` |
| ScaleBoundaryOracleTest.kt | `f7fe6fc8cb8575c48161cde769a884845918c1ea887f67766d1607ae4fd5c18b` |
| DecimalMultiplicationSubclassTest.kt | `e56c25895a07714c52dd25128f83081a78f85a04262a546b43020c4a6ec53559` |
| DecimalRoundSubclassTest.kt | `3ffe301bf9049aff297bc93e937c103f089e76efffef228df34936df795b8b9a` |
| DecimalUnarySubclassTest.kt | `afcf18525c48a0229cd967813a657dad6247e63e32735aebb8652d9bb8e89760` |
| ScaleBoundaryConformanceTest.kt | `c756a1f609c99b6889acafba3ad63d7a10856cdbfecf05c64b0476b26eea34e6` |

Final proof/handoff hashes are recorded externally in the final task comment
to avoid self-reference. Frozen fixture/raw hashes are in the proof and remain
unchanged. No generators, task525 migration tests, JdkOracleTest, H assertions,
other worktree sources, old reports, fixtures or numerical artifacts were edited.

## Review, checks and remaining gates

Independent `/root/v2_source_review` read the actual production/test/oracle diff:
no implementation or compilation-plausibility blocker by source inspection;
confirmed literal predicates/counts, owned sqrt receivers, deferred null staging,
latest caller-super controls, and additional unadapted internal unary/null keys.
Documentation review disposition is recorded in the final task comment.

Tracked `git diff --check` passed. Source/hash/diff/whitespace inspections are
not compilation or behavioral validation. Reviewer is read-only; no runtime,
script, helper, compiler, numerical job, probe, test, benchmark or harness was
started or queued by this worker/reviewer. Final reviewer completion is required
before the worker's final task comment and parent notification.

Remaining gates: parent decisions on exact unresolved fields/callbacks; review
any later source adaptation; separate explicit sole-owner runtime authorization;
fresh V2 compilation/JAR and retained full JVM inventory; full retained JS/Wasm/
macARM; supported exact-final-HEAD CI. New538V2 pilot owns the numerical window
when running. An idle window gives this source worker no execution permission.

The worker leaves task567 in_progress and finishes this finite source milestone
after all owned preparation calls/reviewers are terminal and the required parent
handoff is queued using the message-codex skill. Queue acceptance does not imply
parent processing. No idle wait for future runtime authorization.

# Task 541 normative source handoff

## Current root adoption

The original revision3 handoff below is preserved as historical source preparation,
from SHA-256 `e673b61095e47945aecaf59f873062f6efd1157da67f5ef6b02666e357ed1329`.
The exact two tests now copied into root passed all four complete local target
gates with independent actual review accepted: JVM145/498 and JS/Wasm JS/macOS
ARM64 each65/314, totaling1440 method executions across targets. Future exact
published-head CI remains pending. The [current validation report](decimal-overload-541-verification.md)
owns actual evidence and review status; this finite tranche does not close541.

All213 accepted V4 base source files remain byte-identical. The adopted215-source
manifest is `e4a136545f2fe00f0f3c9a9e80fd6f6a54fbad96b04dc155e0992cbb83c52962`.
DecimalOverloadDispatch541Test.kt remains
`2dfbbf27cad7cca7c59c3d0b210b04e75290c7c6be68c392d68be6d2f926d060`;
DecimalUlp541ContractTest.kt remains
`196333f2dca246fc213c1fb4e92983d85a9470260121edaa7d694d0823f0013e`.
The ulp proof is copied exactly, SHA-256
`c7314a796bc9f06c1146edd556901ed01549aa115692df6b62f4acb39f8f2b32`.
The historical `11196333...` ulp pin is a transcription error; the correct pin
is the196333f2 value above, as already explained in the preserved handoff.

This root adoption changes only the two new common tests and their four scoped
documents. It changes no production or script source. This worker ran no compiler,
script/helper, numerical program, test/build/harness, Git or tracker operation.
Earlier source milestones, failed revisions and G/P scope limits remain intact.

## Historical revision3 source handoff (preserved)

SOURCE ONLY. Own artifacts in this directory are new preparation outputs. The
repository and root task files were not edited. No compiler, test, build,
script/helper, numerical program, certificate, probe, harness, benchmark,
foreign implementation/source/bytecode, Git or tracker operation was run.

Files:

- `DecimalOverloadDispatch541Test.kt`: bounded common controls for all 39
  named entries, grouped into three family methods.
- `DecimalUlp541ContractTest.kt`: exact copy of the accepted prepared row-39
  and no-context-plus source controls.
- `decimal-overload-contract-inventory-541.md`: copied historical inventory
  with a source-milestone section naming this tranche and retaining G1–G6/P1–P6
  limits.
- `DECIMAL_OVERLOAD_541_REVIEW.md`: scope, contract boundary and acceptance
  gates.

This source preparation found no concrete ordinary-input production defect;
independent review remains open. The
remaining blockers are runtime acceptance and full-domain/resource arguments,
not unsupported forged metadata or internal callback-order observations.
Expected values are literals or direct algebra; no generated oracle or foreign
implementation is used. Runtime acceptance belongs to the parent’s separately
authorized window.

The copied ulp source hashes to
`196333f2dca246fc213c1fb4e92983d85a9470260121edaa7d694d0823f0013e`; this
recomputed hash is recorded in `ARTIFACTS.sha256`.

The historical prepared handoff's `11196333...` ulp hash was a transcription
error; the original and this byte-identical copy both hash to the value above.

This is parent-prepared revision3 after independent rejection of revisions1
and2. Both earlier directories remain unchanged. Revision3 corrects five
remaining divide-scale/unary-sign representations, derives them explicitly in
the review ledger, records exact ordinary/truthful and five actual-super scopes,
and credits retained nonzero remainder controls without claiming new coverage.
Independent revision3 review remains pending. Nothing has been compiled or run.

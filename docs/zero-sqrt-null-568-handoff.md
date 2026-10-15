# Task 568 source handoff

**UNCOMPILED / UNRUN. Changes are not committed.**

Session `990acd40` owns only this source preparation in
`/private/tmp/krogu-math-task-568`, branch `task/568-zero-sqrt-null`.
Unchanged HEAD: `bc1d51117a2125acc991607213b6c0bca4c2f8f6`.
Task `local:568` remains linked and `in_progress`; parent `af5f5ab4` retains
its root526 link and sole runtime window for task530.

The production diff moves the existing context validation in `BigDecimal.sqrt`
after the negative check and before zero's return. Truthful zero/null now reaches
the public NPE rule. Valid contexts retain preferred scale and arithmetic;
explicit caller overrides and calls to super retain their public semantics.

Only three pre-existing test consumers change: exact zero/null keys `0` and
`0.00` in the common null oracle and reflected JVM null suite, and the extreme
zero/null assertion in `DecimalExactRoot563Test`. Raw frozen/live observations
remain separately visible. All other comparisons and the frozen fixture and
generator remain. Two new bounded suites prepare four common methods and two
JVM methods with exact Liberica vendor/runtime checks. No test has run.

The focused contract argument, finite input domains, preserved historical
observations, inventory, archive identity and source/test hashes are in
`docs/zero-sqrt-null-568-proof.md`. Independent read-only review disposition and
final proof/handoff/diff hashes will be in the terminal ref-less task comment.
Reviewer `/root/source_contract_review_568` finished with no actionable source or
contract findings; it did not independently execute coverage or verify hashes.

Original archive (created before any edits):
`/private/tmp/krogu-568-original-990acd40/original.tar`, SHA256
`82be5ec59560ede1a296e6f5e384f8ea46767039f8fb3d15c0c85b0d0656dfe9`.
The exact final diff against its extracted originals is prepared alongside it as
`source.diff`; final file copies and the final hash manifest are in the same
archive directory for parent inspection.

Only source/API-documentation, hash, diff, whitespace, normal status and
coordination operations are authorized here. No script, compiler, numerical
helper, public harness, probe, test, build, JAR, fixture generation or measurement
was invoked or queued. No Git mutations, tracker transition/unlink, mutex
operation, runtime-grant waiting, or edits to another worktree occurred.

Parent owns any later bounded pinned public-API replay with full raw outcomes,
fresh artifact, retained full JVM/JS/Wasm/macARM runs and exact-final-HEAD CI.
This handoff does not establish fresh JDK defect verification, a compatibility
gate pass, or exhaustive conformance. The source worker finishes terminally
after recording the review result and final milestone; no future runtime wait.

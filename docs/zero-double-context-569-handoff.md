# Task 569 source handoff

**All changed code and tests UNCOMPILED / UNRUN. Changes are not committed.**

Source worker `9543a8cb` prepared this change only in
`/private/tmp/krogu-math-task-569`, branch `task/569-zero-double-context`.
HEAD remains `5fb00ee449570effbd27556ad4c248a26faf787c`.
Task `local:569` remains linked and `in_progress`; parent session `af5f5ab4`
retains its root526 link and sole numerical/compiler/helper window.

The production change moves the existing context guard in `roundedDouble`
after `fromDouble` validation/conversion and before zero's early return.
Both signed-zero/null inputs now reach the independently required NPE guard.
Valid contexts keep coefficient zero/scale zero and the same rounding behavior;
nonfinite/null inputs keep their historical validation staging.

Only two existing consumers change: exact signed-zero/Double/null constructor
keys in the common frozen oracle and live JVM reflection suite. Input-only
selection, exact sets/counts and duplicate rejection bound the change. Frozen
and live host outcomes remain separately visible. Existing task 568 sqrt
selections and all other host comparisons remain. The fixture and generator,
BigInteger d0, task 528/530/538/568 files and all other production code are
untouched. No additional affected consumer was identified.

Two focused Kotlin suites prepare four common and three JVM test methods.
They use public Kotlin construction calls, with no new Java constructor/ABI
requirements. Independent expected fields cover both signs and p0/p1/p7/pMAX
across all modes; finite/null, exact/nonexact finite and nonfinite controls
retain the intended boundaries. JVM tests retain historical/public-API host
observations, gated by BellSoft `25.0.4+9-LTS`.

The contract argument, finite domains, baseline identities and source/test hashes
are in `docs/zero-double-context-569-proof.md`. Final proof/handoff/diff hashes
and independent review disposition are in the terminal ref-less task comment
and the final archive manifest. Exact originals were archived read-only before
edits at `/private/tmp/krogu-569-source-9543a8cb.WwEQfM/original.tar`, SHA256
`93fcb3699ad86300e18fc1d21b072e928581ebc709c3fc2a9f3f4b91ab8087b5`.
The same directory holds extracted originals, final file copies and the complete
source diff including new tests and documentation for parent inspection.

Only ordinary source reads/edits, public API documentation, file operations,
hashes, diff/whitespace/status checks and tracker coordination were performed.
No numerical helper, compiler, script, test, probe, build, JAR, generator,
harness or benchmark was invoked or queued. No fresh own/JDK observation or
compatibility pass is claimed. No Git mutation, tracker transition/unlink,
mutex transfer, other-worktree write or wait for a runtime grant occurs.

Parent must obtain separately authorized fresh pinned baseline/candidate raw
outcomes, fresh own artifact checks, retained and new JVM/JS/Wasm/macARM runs,
unchanged decimal controls and fresh exact-published-HEAD all-six-job CI before
closing the task. Task 568 passes cannot establish any task 569 gate. The source
worker finishes terminally after recording its source milestone and review.

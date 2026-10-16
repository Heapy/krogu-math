# Explicit decimal API 534 acceptance

The change adds construction, precision/scale rounding, exact division, nullable
parsing, typed exact-conversion outcomes and named quotient/remainder results.
Existing constructors, operators, equality and arithmetic kernels are unchanged.
See [API and migration examples](../../docs/explicit-decimal-api-534.md).

Independent source review approved the overloads, nullable/failure boundaries,
public virtual delegation and Java construction/subclass compatibility. Actual
focused checks on integrated parent `943635476c052bb6377f20d79d2bcb856c2c104e`
passed: JVM 6 methods (four common intent/failure/dispatch tests, one Kotlin
reference-overload fixture, one real Java constructor/static-call fixture),
JS 4, Wasm 4 and macOS ARM64 4; 18 total, zero failures/errors/skips.
All 41 recorded production/API/test/doc hashes were unchanged after execution.
This additive delegation change replaces no numerical kernel; the parent's full
1,866-test regression passed before this focused consumer/API verification.
No performance improvement is claimed.

The [evidence index](evidence-index.json) binds raw Gradle logs, target XMLs,
summary files, exact commands and the executed source manifest under project-local
`.codex-work/534`. JDK BellSoft25.0.4+9-LTS and Kotlin2.4.21 are pinned by the
project. Ordinary pre-existing compiler/Gradle warnings remain; no new deprecation
or warnings-as-errors migration requirement is imposed on consumers.

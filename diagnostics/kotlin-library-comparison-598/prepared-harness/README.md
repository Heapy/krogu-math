# 598 JVM adapter/untimed fixture slice — UNCOMPILED / UNRUN

Owned preparation only. No dependency downloads/resolution, compiler, numerical code execution, clock/counter or benchmark was run. No main/539 source or tracker change. Runtime waits for accepted539 final snapshot and task598's existing authorization/owner/provenance workflow. This directory is NOT a new measurement framework. Its standalone JVM build and explicit UntimedMain correctness entrypoint are preparation only; nothing executes automatically from the root build.

Sources: tagged public READMEs/API descriptions linked in ../peer-selection-preparation.md and ../operation-semantic-matrix.md, plus own Krogu API and public JDK API. No foreign numerical implementation/source archives/bytecode inspected. IonsPin0.3.10 imports/classes and KBigNum0.0.20 overloads remain compilation hypotheses tied to those public descriptions; a future compile failure must be resolved via public API docs, not guessed hidden methods. KBigNum desktop coordinate is kbignum-desktop, not kbignum-jvm. Published target metadata remains authoritative; this slice is JVM only.

Contract.kt is a typed adapter boundary. Four adapters expose exact integer/decimal add/subtract/multiply and self-multiply via the same operation, plus explicit native fixed-scale HalfUp where documented. No coercion to Double, precision truncation, homemade peer arithmetic or writer wrapper. Native values are returned for future escaping; text and JDK conversions occur only in verifyUntimed, outside future counters. Benchmark adapter dispatch/enum/boxing overhead must be identical or removed through reviewed per-case typed closures later, reusing accepted timing/provenance scheme.

UntimedFixtures.kt freezes16 integer and16 decimal input pairs, exact arithmetic and alias-self squares, plus8 explicit fixed-scale fixtures. JDK expected results are public black-box oracle outcomes to be generated/verified later, not executed or claimed PASS now. Fixed-scale literals separately assert expected JDK coefficient/scale. Value parity is mandatory; text parity is recorded independently and does NOT establish a peer's internal scale/cohort. Before any cohort-strict ID, establish public scale/unscaled access and check both; current value-only IDs cannot be silently presented as full BigDecimal compatibility. Unexpected exceptions and mismatches fail closed; unavailable capabilities remain explicit NA rather than caught and called success.

IonsPin fixed-scale divide is NA because DecimalMode combines precision/scale differently. KBigNum significant-digit MC34/HALF_EVEN is NA; IonsPin mapping remains unverified due ambiguous README tie descriptions. Neither adapter implements or fabricates HALF_EVEN equivalence. Fixed-scale20 is a separate operation, never a substitute for original527 MC34. Signed division/remainder, bitwise, parsing/format timing, random/primality/roots, retained-memory/profiler support and full598 scope are intentionally not implemented in this bounded slice; their capability gates remain in the operation matrix.

Remaining compile/check sequence (not run):

1. After539, freeze final Krogu/JDK/peers and isolate benchmark-only dependencies/licenses; resolve exact JVM variants under pinned Kotlin2.4.21/BellSoft25.0.4+9 and verify public signatures. No production dependency changes.
2. Compile each peer adapter separately, then fixture contracts; preserve command/env/version/classpath/source/artifact hashes and failures. Do not infer target compatibility from Maven metadata alone.
3. Execute untimed fixtures and bind observations/counts (128 mandatory core observations per peer, up to32 distinct-equal observations (interned cases explicitly NA), plus8 fixed-scale where eligible). Fail on numeric mismatch; record text/scale limitations and NA reasons independently. Add public cohort access before claiming cohort-strict equivalence.
4. Complete already-required remaining598 operation/target/cache-state/metric capability checks, including signed quotient/bitwise and any real significant-digit peer support. No task closure from this slice.
5. Independently review actual timed expressions and source-bound inputs before integrating with existing warmup/freshfork/counter/escaping/provenance methods. Fixtures are not timing operands prepared inside counters. Never use JDK oracle arithmetic in a measured peer route.

## Internal read-through corrections (source only)

Self-square IDs now explicitly record self-alias. Separately parsed equal operands have distinct-equal IDs ONLY when reference identity differs; parser interning yields explicit unavailable entries, never a mislabeled distinct-input measurement. VerificationReport carries unsupported fixed-scale and unimplemented MC34 reasons instead of silently returning a shorter successful list. No fallback arithmetic or enum-name HALF_EVEN mapping was added. Arithmetic methods contain only native operations; parsing, observation text, JDK normalization, expectations and identity checks remain exclusively in untimed fixture preparation/checks. Fixed-scale signed ties use explicit HalfUp; '/' defaults are never used. No timing eligibility follows from this source read-through or capability declaration.

## Independent review correction: reusable input / retained result gate

UntimedPreservation now performs each exact, self-alias, distinct-equal and fixed-scale operation twice on the same inputs; it snapshots both textual observations before EACH call, compares them immediately after, and verifies retained earlier results after later calls. Both repeated results must have the same observation; the final result is independently checked against JDK, so a failed numeric/text/input-preservation check fails the gate. These are observation-level witnesses, not a proof about unexposed internal scale or every possible mutation. Decimal numeric JDK comparison and separately recorded text parity remain distinct.

No reusable-input timing is eligible before this actual gate passes. If a public peer cannot support equivalent immutable reuse, declare that case NA or independently review a separately named identical one-use preconstructed-pool workload across ALL peers; never clone/parse inside one peer's timed numeric route. The fixtures' observed objects MUST NOT become timing inputs: formatting/checks may initialize caches. Future timing uses fresh, source-bound input cohorts with the explicitly selected cold/warm state. No execution of these new checks has occurred.

## Standalone compile / correctness entrypoint (prepared, NOT EXECUTED)

settings.gradle.kts/build.gradle.kts form a separate unpublished Kotlin/JVM consumer build, NOT included by root settings. Uses Kotlin2.4.21, BellSoftJDK25, ionspin root coordinate0.3.10 (Gradle JVM variant selection), explicit kbignum-desktop0.0.20 and Kotlinstdlib2.4.21. Main root Gradle wrapper is reused at9.8.1/checksum; no second downloaded wrapper. Automatic JDK downloads are disabled. Source selection includes only root *.kt, excluding generated runs/build. runUntimed performs correctness only, with fresh output names and no timing loops. Peer jars/classes and own source hashes are recorded; runtime rejects anything other than BellSoft25.0.4+9-LTS. First resolution/compile must still verify actual selected versions/variants/license constraints and adapter signatures; current descriptor itself is UNCOMPILED.

Future command, from repository root, ONLY after actual accepted539 SHA/JAR is available and host slot authorization exists. Replace placeholders with accepted artifact evidence; never substitute a current intermediate JAR:

```sh
env -i HOME="$HOME" PATH="/Users/yoda/.sdkman/candidates/java/25.0.4-librca/bin:/usr/bin:/bin" \
  JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca \
  ./gradlew -p .codex-work/598/prepared-harness --no-daemon --console=plain \
  -PkroguCommit=FINAL_40_HEX_COMMIT \
  -PkroguJar=/ABSOLUTE/PATH/TO/FINAL_KROGU.jar -PkroguJarSha256=FINAL_64_HEX_SHA \
  -PrunName=untimed-1 --write-locks runUntimed
```

Capture the entire actual command/exit and stdout/stderr externally when authorized. Build descriptor verifies Krogu SHA before compilation and again after the successful check. The actual accepted539 task/commit provenance must be checked through the existing workflow; no manually supplied approval boolean is used. Resolution locks will be generated/retained on first authorized attempt; exact transitive versions and all artifact hashes still need independent review. runUntimed creates runs/untimed-1/{resolved-inputs.txt,observations.tsv}; previous output is never overwritten. An error preserves partial outputs without COMPLETE_UNTIMED_ONLY. No success is inferred from compilation alone.

JVM only: peers' published Native/JS/Wasm coverage remains as in the matrix; this build is not portable runtime evidence. No production publishing, root-library dependency mutation, measurement, source/bytecode inspection or existing539 interference. Fresh539 artifact compilation and these untimed observations are still pending; future measurement will reuse the accepted timing/provenance system after its own review.

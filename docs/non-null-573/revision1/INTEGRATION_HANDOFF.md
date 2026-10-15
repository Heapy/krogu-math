# Task573 G1–G4 source composition, revision 1

**ALL UNCOMPILED/UNRUN. Source preparation only; 31/48 input slots.**

The independent candidate is `/private/tmp/krogu-573-combined-source-v1/candidate`.
Its untouched published base is the sibling `base` directory. Neither directory is
a Git checkout. Root retains the sole compiler/numerical/helper window, task526
linkage, task573 management, runtime acceptance, integration and publication.
No runtime grant is awaited by this source worker.

## Exact inputs

Published base: `60aea413e0c7af007844aace6ec28ecdfa019b67`.
Archive `/private/tmp/krogu-538-v5-jvm-feasibility-v1/published60-base.tar`, SHA-256
`c799013eaadcc85e6cb48031c2b7028fa216f3709d864bf5e7c4f96fc79edf28`.
The base contains exactly404 files. All404 pass the original manifest, retained in
`docs/non-null-574/baseline-tracked.sha256`.

The exact three patches are retained in the sibling `inputs` directory:

| Group | Input patch | SHA-256 |
|---|---|---|
| G1 /574 | `inputs/574/source.patch` | `f0788992d7c891a198840ab34431f346ee51ee7e6983bfe30bf7bf7d81f10c35` |
| G2+G3 /575 | `inputs/575/non-null-575/source.patch` | `441859337d06734b6ea4e9341b26956cde33c4b1ccc782d004950a2bfa88ea39` |
| G4 /576 | `inputs/576/non-null-576/source.patch` | `296fd6c24d267d9e2fa748e3adac077d4ee3ef2bbb4b47ba3faa766a4a8a74c3` |

Focused handoffs and proof ledgers are copied without alteration into
`docs/non-null-574`, `docs/non-null-575`, and `docs/non-null-576`.
Their handoff hashes respectively are
`100afa6cc9eec32fa10c80898e403b3fe3bdd510b6ccc9481b579b9018cc4984`,
`580922367059542b2ab6384f1b81203a49e39cb55d3e6d98211ead5393829af8`, and
`a193c5a6306264823ecc9d267e5f948f5720fe682d9df086a2ffdc55099e523a`.
The task575 initial-tree archive remains available under `inputs`, outside the
candidate. Historical task-specific manifests describe their focused trees;
the composed hashes are the new manifest below.

## Exactly31 migrated input slots

| Type / methods | Migrated inputs | Slots |
|---|---|---:|
| BigInteger compareTo, min, max | one BigInteger operand each |3|
| BigDecimal compareTo, min, max | one BigDecimal operand each |3|
| BigDecimal add, subtract, multiply, divide, divideToIntegralValue, divideAndRemainder, remainder: exact overloads | one BigDecimal operand each |7|
| The same seven methods: MathContext overloads | BigDecimal operand and MathContext each |14|
| BigDecimal abs, negate, plus, round: contextual overloads | MathContext each |4|
| Total | |31|

Both Comparable type arguments and matching Kotlin overrides migrate with G1.
The contextualUnaryRound private helper migrates with G4. These are support edits,
not extra public input slots.

The remaining17 reviewed nullable public inputs retain published60 declarations:

- BigDecimal sqrt context and the two deprecated int-rounding divide operands:3.
- BigInteger modPow exponent; and/or/xor/andNot operands; probablePrime random:6.
- BigInteger signed slice magnitude and two PlatformRandom constructor inputs:3.
- BigDecimal String/context, CharArray/context, CharArray slice/context and
  Double/context constructor context inputs:4.
- MathContext primary-constructor rounding mode:1.

All three equals(Any?) inputs remain nullable. All four nullable min/max results
and four nullable OrNull results retain baseline annotations. Constructor bodies
and all remaining17 inputs are unchanged in this phase. This is not48 completion.

## Baseline-aware merge decisions

G1 applied first. G2+G3 and then G4 applied their nonoverlapping hunks. Shared-file
conflicts used `git merge-file` against the exact published base and were resolved
additively; no full focused file replaced another group's edits.

- BigDecimal retains G1 comparison/Comparable changes, all21 binary/context slots
  from575 and all four unary contexts from576. Arithmetic bodies, valid-context
  virtual delegation, stored-zero seam and tie inequalities remain as supplied.
- DecimalIntegralDispatchOracleTest combines the574 non-null comparison override
  with the575 non-null subtraction/integral/division overrides.
- DecimalContextContractTest combines575 division overrides, valid controls and
  moved-row accounting with576 non-null abs/negate overrides.
- PortableOracleTest keeps all three input-only selectors and each exact map/set
  assertion. Its comparison valid-row map, ND ordinal set, unary ordinal/key map,
  finite-zero controls and every prior ordinary/math assertion remain active.
  Integration additionally asserts selector disjointness before evaluation,
  duplicate unary-ordinal rejection, and the exact478 combined moved oracle count.
- README and COMPATIBILITY describe the31-slot source phase and pending runtime
  verification. Historical evidence sections remain historical observations.

Checking each focused source manifest against this composition differs only on
the expected shared files:574 has BigDecimal/integral/portable;575 has those three
plus context contract;576 has BigDecimal/context contract/portable. Every other
focused changed/new source is byte-identical to its focused implementation.
The three check logs are retained beside the candidate.

All new names retain their unique574/575/576 suffixes. Java direct override,
inherited/base, virtual reflection and actual explicit Java super routes remain
distinct. Java adapters pass null into actual target methods. No caller-side !!,
null cast, manufactured adapter NPE, outcome-selected partition or blanket
host-parity waiver is introduced by composition.

## Original execution provenance and accounting

The original frozen fixture keys/ordinals and observations remain untouched.
Counts below are source obligations, not observed test execution.

| Origin | Original rows/executions | Moved JVM executions | Common retained |
|---|---:|---:|---:|
|574 IM / MM |702|186|516|
|575 ND |288|180|108|
|575 context dispatch |75728|384|75344|
|575 context contract mask executions |11472|240|11232|
|575 remainder array |216|96|120|
|575 pair composition |1440|0|1440|
|576 unary vectors |112 selected null rows|112|all other valid rows|
|574 identity standalone |2|2|valid identity controls retained|
|575 standalone |137|137|valid controls retained|
|576 standalone |330|330|valid controls retained|

The portable oracle partition moves186+180+112=478 original executions. Its three
selectors have disjoint family domains (IM/MM, ND, K/C), and the new assertion
checks this before any group can continue the loop. Each group retains its own
independently pinned ordinal/key map or set and its JVM replay assertions.

Other575 fixture families add384+240+96=720 executions. Context contract contains
96 selected original rows with240 masks; the common test's four partition methods
repeat only selection/accounting of the96 rows, not their moved numerical calls.
Total moved fixture executions are1198 on1054 distinct original fixture rows.
The standalone families add2+137+330=469: total1667=188+1037+442 original common
executions reassigned exactly once in source.

The focused ledgers pin574's186 six-field rows and two identity calls;575's900
fixture executions and137 standalone calls;576's112 vectors and330 standalone
calls. The unchanged JVM loops are separate pre-existing obligations, not extra
displaced common rows:574 matrices168 integer/120 decimal;575 matrices360/90/3456;
576 returned-negate null24, null-round20, null-unary90 and forwarding12. Their
exact input keys, valid mathematical assertions and Java override controls remain.
No duplicate run or missing numerical execution is inferred as runtime evidence.

## Source preservation bindings

The manifest [source.sha256](source.sha256) hashes all61 changed/new active source
files:48 original source files plus13 new files. It is the union of the focused
28/24/15-file lists, deduplicating the four shared paths. SHA-256:
`3f13f9d6dfff30dac6aa644262fa0d14d93d87c4179d5f0cd7664a28e0b8e4ba`.
The source path-list hash is
`9b124a88eb14c33f4a49b4d359370da8b11ac960304969fef69ee7fcf693418d`.

Among404 originals, exactly50 change (48 source files plus README/COMPATIBILITY).
All354 others pass [preserved.sha256](preserved.sha256), hash
`f77a227f264c89c1721c584351673408a5624ba49e8028d220bbc17d3c0ec494`.
The354-line successful check log hash is
`0d85a03edf01643546bd54272b44585e85126736dc027da636ba0fe983f9bb4d`.
These bindings include all frozen fixtures/generators/raw observations, build and
toolchain files, serialization source/current-own roundtrip tests, all545 files
and all544 fixtures/assertions/wrappers/traces. The accepted544 contract changes
only its compareTo parameter annotation, exactly as supplied by574.

Published60 d0 integer division/modular kernels are retained. No private538
performance9048 patch is included. BigInteger's sole production change is G1.
BigInteger source SHA-256:
`0089ac95a27cd8523225641950f75823cee7df0c56533e225c326ce665b34006`.
BigDecimal composed source SHA-256:
`c3dde862f5bffe803fc43c677b2bae5ed1bc1d88790dfcad3e1054a7130053a4`.
PortableOracleTest composed source SHA-256:
`3d63d3f659807f77c9232b00902152f6ccfb9dbb45d353339bdb1d155709a3ab`.

## Static compiler/test preparation inventory

The13 new source files comprise three common row-selector objects, seven JVM Java
files and three JVM Kotlin files. Seven new test classes contain22 new @Test
methods, listed statically below. ValueIdentityContractTest adds the23rd new method,
integerMinAndMaxTiesReturnTheDistinctArgumentIncludingTruthfulSubclasses. Existing
method renames retain their test annotations and source assertions.

- ComparisonDispatch574Test: directJavaOverridesAndReflectionRemainVirtual;
  explicitJavaSuperCallsReachAllSixLibraryEntries;
  ordinaryAndInheritedJavaAndReflectiveBoundariesRejectNull.
- ComparisonNull574JvmTest: everyDisplacedFrozenRowExecutesAtItsRealJavaBoundary;
  commonIdentityNullChecks.
- DecimalBinary575BoundaryTest: allTwentyOneSlotsRejectNullAtOrdinaryJavaEntry.
- DecimalBinary575StandaloneTest: contextStandaloneRows; integralStandaloneRows;
  preferredScaleStandaloneRows; scaleRoundingStandaloneRows.
- DecimalBinary575JvmTest: everyDisplacedNdRowUsesItsOriginalJavaOverrideAndInputs;
  everyDisplacedContextDispatchRowUsesExplicitJavaSuper;
  everyDisplacedContractMaskUsesOrdinaryOrTruthfulJavaReceiver;
  everyDisplacedArrayRowUsesExplicitSuperAndRetainsMalformedArrayDiagnostic.
- UnaryNull576StandaloneTest: noContextNulls; explicitOverrides; zeroUnary;
  returnedUnary; zeroSeam; ordering; precisionSentinel.
- UnaryNull576VectorsTest: everyDisplacedRowExecutesAtJavaBoundary.

These names/counts are static preparation only. No test discovery, compiler,
Kotlin/helper/certificate script, Gradle, Java numerical probe, JAR/build, harness,
benchmark or timing/allocation measurement executed. Only source reads/edits,
archive/hash/diff/apply/merge-file and ordinary static text processing ran.

Focused groups each report an independent read-only source review with no
actionable defect; those reports are preserved in their handoffs. Aggregate formal
source review remains root-owned. This worker does not claim independent aggregate
review or compiler/runtime acceptance.

## Artifacts and continuation

The sibling directory contains `source.sha256`, `source-paths.txt`,
`preserved.sha256`, `preservation-check.txt`, `output.sha256`, `output-paths.txt`,
`combined-source.patch`, `combined-source.tar`, and `final-artifacts.sha256`.
The output manifest hashes every candidate file including these proof documents.
The final artifact manifest binds the complete source archive/diff/output manifest
without a self-referential hash. The patch applies to published60 with `git apply`.

Preserve this31-slot snapshot before integrating577/578 into a new revision. Root
may compile only an isolated copy under pinned Kotlin2.4.21/Gradle9.8.1/
Liberica25.0.4+9-LTS, including each negative fixture independently and checking
its specific argument mismatch, generated JVM boundaries/bridges, complete exact
row replay, retained local targets, current-own roundtrip, fresh own JAR/public
tests and exact published-head all6 CI. No JCK/full compatibility claim is made.
No root/native worktree/runtime/evidence, Git ref/index or tracker was modified.

**ALL UNCOMPILED/UNRUN. This source phase is terminal.**

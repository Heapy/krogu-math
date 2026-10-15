# Decimal metadata and scale repair integration

Work in progress under root526 grant1959, after task528 explicitly released
its numerical window in comment1958. This report records actual executed
observations. All four retained local platform selections and the targeted
public tests have passed; publication and new exact-head CI remain pending.

## Source and artifact

Base:96ddbf7b9241f14265620c72a8217c32c4cdb4ff. The independently reviewed
composition retains the published pow/sqrt/floating changes, adds accepted528
V2 metadata helpers, and adds the three independently proved566 scale repairs.

* BigDecimal SHA256:a1d6c40c04e6534b2ae934f6d77e7cd8b5a1c530bb8e594b2af08f9fc225bb8a.
* BigInteger SHA256:01d8aa0a2980de78614110ca64b7d4314c103030179da59e1367e7ae38991aa7.
* Fresh own JAR SHA256:a790ce630d6d4e76f83f78825009f0dbc3b86cd8daecf2ebc5439571bcb5b1ea.

The JAR was forcibly rebuilt using `jvmJar --rerun-tasks`, with all three tasks
executed in7s, pinned Gradle9.8.1/Kotlin2.4.21/BellSoft25.0.4+9-LTS,
`--no-daemon --max-workers=1`,2GiB heap and in-process Kotlin compilation.
The before/after28-input production/build manifests are identical, SHA256
4d4d5bc7f93ad0786d27413c2b4d33cf51e91cb163db2450eed1809068f7919c.
Build log SHA256:5b8d6c89404cd45750b87692e00bd3a44589524c2140ddbad5cdd347a3b1dd8a.
Saved artifact and evidence: `/private/tmp/krogu-root528566-validation-v1`.
The metadata source checker intentionally pinned to worker528's03 base is
unchanged; it is not repinned or claimed to certify the combined root source.

## Initial full JVM gate

The complete retained package selection compiled and executed381 methods:
364 passed,17 failed; command exit1 in1m1s. All new528/566 methods passed.
Failures are in the existing scale/setScale/multiplication oracle consumers.
Original stdout and complete XML are retained as `jvm-initial.log` and
`xml-jvm-initial` before any retry. This is not a passing gate.

Six oracle consumer sources are archived before their narrow classification.
The independent bounded decimal-string model and the revised consumers have
conditional source acceptance from root and a separate read-only reviewer.
Every frozen generated row and loop remains active; subclass dispatch traces,
result identity/base-class controls and unclassified assertions remain.
See [the classification record](../diagnostics/decimal-scale-rounding-566/README.md).
Subsequent complete JVM attempts preserved two further first-conflict records:
retry1 executed381 methods with one failure at a truthful `T` exact conversion;
retry2 executed382 methods with one failure at the stored-value `L` multiply
matrix. Their full logs and XML remain separately archived. The four exact `T`
keys now assert frozen messages/traces separately, independent results and the
explicit public wrapper chain; only private sign-read multiplicity is observed.
The `L` classification preserves exact `context;` and distinct/base-class flags.
An added JVM method retains actual public-JDK observations for all four `T`
operations. Ordinary mismatches now collect through replay and must total zero.

Final full JVM retry3 exited0 in1m27s. The source-derived XML audit passed under
256MiB:114 suites/382 methods, zero failure/error/skip/missing/extra. All new
methods are present, including both566 JVM methods. Auditor SHA256:
417bf6903b7f2d299cff5fe4360c9fe2cc3f747e51b4c5655553240ec3d96c24.
Final PortableOracleTest SHA256:
94eb86c093cc8ddfb283c05013803f110d307a102e54d2fc72b818786950d7f2.
Final566 JVM regression SHA256:
3b30f1d1570cb36543f8eb18df069241fbf97b551c6a86123924fc5d13285bda.
Independent source review accepted each narrow revision. Production source and
the fresh JAR stayed unchanged throughout these test-only revisions.

The complete retained JS/Node selection subsequently exited0 in9m26s.
Its source-derived XML audit passed:53 suites/250 methods, zero failure/error/
skip/missing/extra. All17 new common methods execute. Raw log/XML/audit remain
in `js-test.log`, `xml-js` and `js-audit.log` in the same evidence directory.
The complete retained Wasm/Node selection then exited0 in1m18s, with the same
53 suites/250 methods and zero failure/error/skip/missing/extra in its source-
derived XML audit. Raw evidence is `wasm-test.log`, `xml-wasm` and
`wasm-audit.log`. The complete macOS ARM64 selection exited0 in5m45s; its
source-derived XML audit also passed53 suites/250 methods, with no failure,
error, skip, missing or extra methods. Evidence: `mac-test.log`, `xml-mac`
and `mac-audit.log`. The four final commands total1132 successful method
executions, separate from all three preserved earlier failed JVM invocations.

The [retained truthful/subclass observations](../diagnostics/decimal-scale-rounding-566/truthful-and-high-multiply-observations.tsv)
contain98 actual stdout records: four fresh public-JDK/port truthful controls,
four frozen/current truthful rows and90 selected stored-value HIGH multiplication
rows, including cases that still require an arithmetic exception. SHA256:
5ec0fe871f22d3eb93e8911e6f43f36ed04c86c31ea55a751cfdc49141473fb5.
The original successful XML remains intact; these are extracted observations,
not replacement input fixtures or an expansion of the classification domain.

## Actual standalone566 observations

The unchanged diagnosticbe72c0b05ce712eaf2e195c5558fd2434b6963e592b5aaf4c45f60d544b44cb0
ran separately against the preceding forced-build root1919 JARdaf2a133... and
the new combined JARa790ce63..., both exit0. All four own core class origins
were checked against each selected artifact. Both stderr files are empty.
These are separate bounded diagnostic invocations, not official TCK results.

Each retains96 original rows: two signs, all eight modes, enum/legacy setScale,
finite-context multiplication, carry-only multiplication and both zero-add
orientations. Candidate results match all96 independent expected pairs or
required exception categories. The pinned public JDK differs in86 rows.
The baseline still exhibits the former premature errors. Actual error messages
are retained, including UNNECESSARY; no reference behavior is reconstructed.

* [Baseline observations](../diagnostics/decimal-scale-rounding-566/baseline-observations.tsv),
  SHA256:57d457d55ba3c6dd6518f6e347b7894764c9ce655fdcd4d4a169525f58ef0d8c.
* [Candidate observations](../diagnostics/decimal-scale-rounding-566/candidate-observations.tsv),
  SHA256:0c1b82e91e56f44e2a56ec4dede24eefa440b8c330a8f852b7833d211238c1a9.

The [independent566 proof](decimal-scale-rounding-566-proof.md) derives the
required final value/cohort from JavaSE25's setScale and exact-intermediate
rounding contracts. Observed differences apply to those proved cases; they do
not establish a blanket JDK-defect waiver or cover all arithmetic overloads.

## Separate528 performance evidence

[The final528 V2 runtime report](../DECIMAL_METADATA_V2_RUNTIME.md), SHA256
5ec43348ae9d903f87d339cb26caace338a582accade5a0cea51b04609d59d67,
records944 successful method executions on the deliberate isolated03 candidate,
matched forced own builds and six complete balanced measurement forks.
All4320 samples/429840 calls are retained; all144 allocation ratios are below1.
One3.429695 timing point regression remains, together with host/JIT/GC limits.
This conditionally accepts the observed allocation improvement for that protocol;
it is not a performance measurement or platform pass for the combined root JAR.
The adverse V1 report and evidence remain intact.

## Targeted public tests

Under root continuation1974, the unchanged public AddTests, CompareToTests,
MultiplyTests, PrecisionTests, RoundingTests, ZeroScalingTests and SquareRootTests
passed in both baseline and port:14 actual invocations,0 FAIL. All compile/test
commands exited0 without timeout or incomplete caught OOM. All seven port runs
verified own-core loading from the selected fresh JAR; their nine test-class
bytecode audits have empty hostMathMembers. Assertions, operands, run directives,
seed559 and512MiB child heaps were unchanged. Public test/support sources remain
segregated under ignored `build/math-tck`, under their original licenses.

Run1792031844435 is complete=true, officialJck=false. Results JSON SHA256:
03acb8aefad591eb7e91e640f99d36efa4b0022b489dc0be696083ff019fc8b3.
The126 SKIP records comprise104 outside-selection records,16 unchanged upstream
disabled-test records,4 excluded private-infrastructure records and2 unchanged
commented-out-call records. They are not executed passes. The pinned public
tests are jdk-25.0.4.1-ga while the runtime is BellSoft25.0.4+9-LTS; identical
vendor/revision sources are not asserted. Broad public primality/resource
failures and gaps remain, independently of these targeted passes.

The remaining combined-source gates are publication and fresh exact-published-
head all-six CI.
No official JCK certification or full-project compatibility is claimed.
Task525's separate enabled Number/generic migration controls still fail on
pinned Kotlin/JS; their isolated unmerged draft is not part of this retained
main package selection. The [compiler investigation](../diagnostics/js-number/README.md)
is submitted for review under task557's external-cause alternative. These
passing retained gates neither fix nor waive that remaining Number contract.

The preceding published96ddbf7 head separately completed all six jobs in
[CI38663897527](https://github.com/Heapy/krogu-math/actions/runs/38663897527)
with success, including an actual LinuxARM64 run233/233 methods in50 cases.
Its raw ARM log SHA256 is
d6d2df56f4c24a2c50cec0773145456bffa5bcd70c326fae107a0900134b6797.
That closes the focused565 midpoint acceptance gates; it does not validate
the newer uncommitted528/566 composition described here.

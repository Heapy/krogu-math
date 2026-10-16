# Contextual decimal powers: private working coefficients

The source253 composition passes the full local JVM, JS Node, Wasm Node and
macOS ARM64 selections: **1,744 methods in 431 suites**, with zero failures,
errors, skips, missing, extra or duplicate methods. Its complete sqrt ledger
contains 94,282 strict records in 52 completed recorders, byte-identical to
source250. Matching published-head hosted/public gates remain separate.

## Resulting behavior

Contextual `pow` preserves its enlarged working precision and every prescribed
work round before the separate destination round. It no longer requires an
oversized working coefficient to fit the public `BigInteger` range when the
required final result is supported. A private decimal core and Long zero tail
carry mathematical working precision through `Int.MAX_VALUE + 10`; the original
base remains unrounded for later set-bit products. Negative powers reciprocate
the actual rounded work state. Exact reciprocals above the public decimal-digit
bound are classified without generating billions of irrelevant quotient digits.

For example, `(1000@1)^-999999999` at maximum context precision has the supported
result `1@1999999998`, for every rounding mode. Its observed private work state
has 2,147,483,657 digits and scale 147,483,658 while retaining a unit core.
Both signs and current-own-format truthful coefficient subclasses are covered.
The corresponding positive exponent requires an unsupported final coefficient
and remains an arithmetic error. The dense maximum-range witness in task593
remains symbolic; no giant allocation or failing JDK execution is claimed.

The old sufficient working-precision path and stored-zero route remain intact.
Standalone multiplication/division retain their own public output limits.
Existing working-scale admission is explicitly qualified in the proof; this
change does not close every intermediate-scale endpoint, resource or accuracy
obligation in tasks542/548. Public dispatch, explicit `super`, unlimited-context
delegation and all existing diagnostic fixtures/assertions are preserved.

The [implementation proof](decimal-power-593-implementation.md) gives the
recurrence, rounding, reciprocal, aliasing, primitive/index and live-storage
bounds. The [source handoff](decimal-power-593-handoff.md) is the unchanged
pre-execution milestone; its UNCOMPILED/UNRUN statements are historical.

## Exact composition and independent source review

The [253-source manifest](source253.sha256) lists every Kotlin/Java source.
Relative to source250, only the decimal `pow` selection, one final stored-value
integer bridge and the new private helper change production. Five common
methods and one JVM method are new; all 248 unowned source files are unchanged.
No public API, value field/cache, serializer, dependency or toolchain changes.

| Item | SHA-256 |
| --- | --- |
| Source253 manifest | `aa90e836005efc363e9575026cd12049b0bb6ac42eb3a716f20d04c596bac1ad` |
| BigDecimal | `5761b7113f808d258deaa6266ff55a1a409ee41b238b2dc1f28a1724bd8f0b9a` |
| BigInteger | `8b6359be57ebbc8047102e5dea420e307d4fb128cbf686a054b04dff2600b1ff` |
| DecimalPower593 helper | `851cf8b6bd0839d418c0ff71a5fc365daebf2ffb5bb5599de7e8f44fa5050f9c` |
| Fresh own JVM JAR | `5e07541044a098601d0cdb05ae1c73eaae063fb8181836b5da3ff6a1a97d72c1` |
| Build configuration | `b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6` |

The independent frozen code/test review is
`/private/tmp/krogu-593-code-review-PwtwDLOr/REPORT.md`, SHA
`a315c670b37797704d9567ce7c581aed6c07ada54e7d23a48d543ea74d7fe24a`.
The final proof/handoff addendum is
`/private/tmp/krogu-593-proof-addendum-9xJDOpKb/REPORT.md`, SHA
`d16b746b4d162c50202782dbd727d5411a7db3d937983dccc0064355826401fc`.
Both precede execution and found no actionable defect within their stated
scope. The exact-factor route may construct the already-required final
magnitude early; the proof explicitly justifies this exception to the general
post-round publication path. Arbitrary oversized denominators are not published.

## Actual local execution and complete inventories

Retained workspace: `/private/tmp/krogu-593-validation-4lHqg6gE`, subdirectory
`root/`. ONE original Gradle invocation, session3301, completed exit0 from
2026-10-16T04:27:42Z to 04:38:35Z, 10m53s, all 50 actionable tasks executed.
It built `jvmJar` and selected the complete `io.heapy.krogu.math.*` package on
all four targets. Original Kotlin2.4.21, Gradle9.8.1, Liberica25.0.4+9-LTS,
one worker, in-process compilers, 4GiB Gradle heap, flags and test deadlines
were retained. The project copy had no prior build output; shared dependency
caches and the existing Kotlin Native toolchain home were reused. This is not
a hermetic build or performance measurement. The FSEvents warning and ordinary
compiler/deprecation warnings remain in the original log; there was no retry.

`evidence/command.txt` records the exact command. The full log SHA is
`698343c66cc185bc16f0ea50f711f90cb39a0c4ef94d8a9718787bcc05b47a74`.
Source/pathset/build checks before and after execution and audits pass. The
fresh JAR hash measured after Gradle remains unchanged after both auditors.

ONE [XML auditor](../tools/verification/audit-composed253-xml.main.kts), SHA
`41cbebef623b80e9769338a76e6a0c6c9a402bd40729a95850292f7e98d3ee07`,
completed exit0 at 04:39:21Z–04:39:26Z. It independently derives the complete
source keysets before opening XML and verifies their full actual multisets.
Count-only agreement cannot hide an omitted, extra, duplicate or skipped test.

| Target | Expected/actual suites | Expected/actual methods | Issues |
| --- | ---: | ---: | ---: |
| JVM | 173/173 | 580/580 | 0 |
| JS Node | 86/86 | 388/388 | 0 |
| Wasm Node | 86/86 | 388/388 | 0 |
| macOS ARM64 | 86/86 | 388/388 | 0 |
| Total | 431/431 | 1744/1744 | 0 |

The five new common methods run on each target; the JVM method runs on JVM.
Their source-derived 6,144-tuple rational grid is an independent bounded Long
oracle, not 6,144 separately emitted JUnit methods. Other controls distinguish
original-base recurrence, work/final double rounding, generic D0>W, three-cell
products, guard termination versus exact termination, signed directed rounding,
UNNECESSARY, sparse Long work counts and both factor-chunk paths. Current-own-
format restoration proves reachable truthful subtype setup, without a legacy
or cross-format serialization requirement or incidental callback counts.

ONE [sqrt-ledger auditor](../tools/verification/audit-sqrt-ledger-253.main.kts),
SHA `3cb892ec1a7d3f9de7e097b15d750dba949bff3747f0a7b57256bddc2ac7d700`,
completed exit0 at 04:39:37Z–04:39:43Z. Both auditors change only source-manifest
provenance/count and comments from their accepted250 predecessors; reverse
substitution recovers the complete old hashes. Mathematical certificate logic,
schemas, strict fields, keysets, XML security and runtime guards are unchanged.
Each runs with a fresh private compiled-script cache and 256MiB SerialGC.

| Target | Records | Complete recorders | Issues |
| --- | ---: | ---: | ---: |
| JVM | 38497 | 16/16 | 0 |
| JS Node | 18595 | 12/12 | 0 |
| Wasm Node | 18595 | 12/12 | 0 |
| macOS ARM64 | 18595 | 12/12 | 0 |

The entire `ledger-audit-output/records.tsv` is byte-identical to source250,
SHA `997e40c39be2a0b4ffb8152690d45d7f1cf5e1f713b11e6c2a01205d2203457a`.
The full XML summary SHA is
`7067bde137dda2e0531dc4d3dcbdc82c849f92a102d17ebfaf756f9e5a5bd6a5`.
All numerical/compiler/script calls are terminal and their sole-window tokens
released. The independent actual metadata report is
`/private/tmp/krogu-253-actual-review-i18d8Bzg/REPORT.md`, SHA
`69260d2c6eccba6575daddcf42e29b0479ca43ca085873cfe83df696c906782b`.
Root read the full report and verified its757 input entries and59 output
entries. Input-index SHA is
`6d8b71ce8b77c46eaa88a822e593c07cba102ce7528ad505a5f3df37c19ed5b5`;
output-index SHA is
`69b44cb1fd51942e2da9963521a94abb5803970f1158fb01c6131041841f4de8`.
The review independently binds every raw XML identity/counter, all21 new method
executions, every44-cell ledger payload and full recorder/key completeness to
the retained outputs, without rerunning arithmetic. Initial metadata-reader
size/format/name corrections are preserved separately and are not test retries.

These bounded checks do not prove giant-object feasibility, maximum allocation
capacity, performance, actual PRNG properties, universal power accuracy or every
scale/subtype/API input. Tasks526/541/542/548/549 and the525/557 compiler issue
retain their explicit remaining scope. The previous
[source250 supported](source250-hosted-verification.md) and
[public](source250-public-math-verification-559.md) results remain evidence for
that earlier composition; official JCK conformance is unproven.

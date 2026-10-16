# Numerical division normalization without coefficient Strings

Source255 passes the complete local JVM, JS Node, Wasm Node and macOS ARM64
selections: **1,757 methods in 436 suites**, with no failures, errors, skipped,
missing, extra or duplicate methods. The complete retained sqrt ledger passes
94,282 strict records in 52 completed recorders. Published-head hosted/public
acceptance remains a separate gate; the preceding source253 reports describe
that preceding composition.

## Behavior and proof boundary

Exact and fixed-scale division now use the existing stored numerical digit and
significand helpers when either coefficient is noncompact. Numerical
normalization no longer needs a whole noncompact coefficient String or a
right-zero-padded String. When both coefficients are compact, the complete old
helper bodies and their callbacks remain intact; truthful magnitudes then have
at most 19 digits. The selector is not a smaller supported mathematical range.

The quotient exponent remains
`dA - dB + Sb - Sa - [significand(A) < significand(B)]`.
Long arithmetic covers the full supported digit and Int-scale ranges. Signs,
zero's lexical metadata convention, equal padded significands and discarded
nonzero suffixes retain their mathematical order. No public API, numerical
kernel, field/cache, serializer, old test, fixture, dependency or toolchain
changes. Correct current-own-format restored coefficient subclasses remain
reachable; there is no runtime-class exclusion or legacy-stream requirement.

The motivating symbolic `A/A = 1`, `A = 10^646456992`, remains unexecuted.
Binary support for A alone does not establish a host's capacity for its
646,456,993-digit temporary String. This change removes that independent
representation dependency. It establishes no observed Node/V8/Wasm String
ceiling, failing JDK execution, maximum allocation feasibility or performance
improvement. Required textual outputs and necessary numerical buffers retain
their own resource premises.

The [implementation proof](decimal-division-normalization-594-proof.md)
records exact equivalence, predecessor reachability, Long bounds and reused528
kernel/resource premises. The [source handoff](decimal-division-normalization-594-handoff.md)
is preserved as the historical pre-execution milestone; its UNCOMPILED/UNRUN
labels describe that earlier stage. Existing diagnostic assertions remain
active. False/replacing/throwing metadata observations need their individual
541/526 classification; they do not waive otherwise valid mathematical or
explicit caller/super/result/exception obligations.

## Frozen composition and source review

The [255-source manifest](source255.sha256) lists every Kotlin/Java source.
Relative to source253, five lines are added to two private BigDecimal helpers
and two test classes are added. All 252 other original source files are
unchanged. Deleting exactly those five lines recovers the full original
BigDecimal hash; this is not a comparison of selected regions alone.

| Item | SHA-256 |
| --- | --- |
| Source255 manifest | `8427be10a953974f1b74348dec87ea0cbbbb6f93c6e806875b049e4ff2fb339d` |
| BigDecimal | `82e5342ab19024aed3aee3191eb340a72eba316c843d708986e5eb5827d2468f` |
| Unchanged BigInteger | `8b6359be57ebbc8047102e5dea420e307d4fb128cbf686a054b04dff2600b1ff` |
| New common test | `7872c6c50aebe56e00e0e0241c0b5736952c388d68ef17f8a91efded54e98c1e` |
| New JVM test | `1cf898eb09e38c10d445b5344479c5b21f3e6f2e04901f7de47eee54600bcb2c` |
| Fresh own JVM JAR | `bb15a352df94be410ce79c310af6f541ee4206c59148dc30f81e9dc79270104b` |
| Unchanged build configuration | `b883bf096dd0c80f1dfec459af4a439c26232c92f0cdff6687dca23cd631efe6` |

Independent source/code/test/proof review:
`/private/tmp/krogu-594-source-review-UDMqHkDe/REPORT.md`, SHA
`dfca54c9a932eb96e5a9b1e144575928cbd000a38b39d20e4f01eea06301aa35`.
It found no actionable defect in this scope. Root read the full report and
verified its 290 input and 20 output entries before compilation.

## Actual local execution

Retained workspace: `/private/tmp/krogu-594-validation-bZbgHiBl`, subdirectory
`root/`. ONE original Gradle invocation, session95857, completed exit0 from
2026-10-16T05:22:44Z to 05:33:31Z, reporting 10m46s and all 50 actionable tasks
executed. It built `jvmJar` and selected the complete `io.heapy.krogu.math.*`
package on all four targets. Kotlin2.4.21, Gradle9.8.1, Liberica25.0.4+9-LTS,
one worker, in-process compilers, 4GiB Gradle heap and original flags/deadlines
were retained. The private project copy had no prior build output. Shared
dependency caches and the existing Kotlin Native toolchain home were reused;
this is not a hermetic-build or timing claim. Warnings remain in the raw log.
No command was retried.

`evidence/command.txt` records the exact Gradle command. Raw log SHA:
`906cc1af920ec929a503eb813660e1c3719d78683c931e5cbcc12c994a131263`.
All source/pathset/build checks before and after execution and audits pass;
the fresh JAR remains unchanged after both auditors.

ONE [XML auditor](../tools/verification/audit-composed255-xml.main.kts), SHA
`d1b4651e302a825574174a95f82593cd6ee9bbade195fbb87a172fd854f75017`,
session49081, completed exit0 at 05:34:28Z–05:34:32Z. It derives complete
source keysets before opening XML, then compares full actual multisets.

| Target | Expected/actual suites | Expected/actual methods | Issues |
| --- | --- | --- | --- |
| JVM | 175/175 | 584/584 | 0 |
| JS Node | 87/87 | 391/391 | 0 |
| Wasm Node | 87/87 | 391/391 | 0 |
| macOS ARM64 | 87/87 | 391/391 | 0 |
| Total | 436/436 | 1757/1757 | 0 |

The three new common methods run on every target; the one new JVM method runs
on JVM, adding 13 method executions. Their independent oracles cover 100 exact
literal divisions, 640 primitive-rational all-mode fixed-scale cases and 41
sticky/boundary/zero/nontermination/scale controls per common target, plus 18
current-own-format truthful-subtype calls on JVM. Internal loop cases are not
separately emitted JUnit methods. Operands have at most 23 coefficient digits;
some zero/error controls deliberately exit before the changed helpers.

ONE [sqrt-ledger auditor](../tools/verification/audit-sqrt-ledger-255.main.kts),
SHA `99b7da722c4e3c4f8cf86f556827d57cf1f497d8cd66028daafdba723b09de2c`,
session67909, completed exit0 at 05:34:51Z–05:34:57Z.

| Target | Records | Complete recorders | Issues |
| --- | --- | --- | --- |
| JVM | 38497 | 16/16 | 0 |
| JS Node | 18595 | 12/12 | 0 |
| Wasm Node | 18595 | 12/12 | 0 |
| macOS ARM64 | 18595 | 12/12 | 0 |

Both successors change only source-manifest provenance/count and comments from
the accepted253 scripts. Exact reverse substitution recovers both complete
original hashes. Mathematical certificate logic, schemas, keysets, XML security,
strict fields and runtime guards remain unchanged. Each uses a separate fresh
absolute compiled-script cache and 256MiB SerialGC. Exact commands are retained
in `evidence/audit-commands.txt`. All invocations are terminal; the sole local
numerical-window token was explicitly released afterward.

Full XML summary SHA:
`a18dbb9c3de159970c0abf7e21e409ef9c486bdb788f9c3f9bde4c9271075bbc`.
The entire `ledger-audit-output/records.tsv` is byte-identical to source253/250,
SHA `997e40c39be2a0b4ffb8152690d45d7f1cf5e1f713b11e6c2a01205d2203457a`.

Independent actual metadata review:
`/private/tmp/krogu-255-actual-review-rsfdNn9x/REPORT.md`, SHA
`ab816253d1313f75cf01a34ca4eeac84a9de2c62f84566c670199ff1e4271126`.
Root read the full report and verified its 752 input entries and 499 output
entries, with no bad checksum. Input-index SHA is
`0eed22ef94121f1048ad83a2336a44fe839144ccee6d70e22993b4a00893fca0`;
output-index SHA is
`6334558691d0f43fdf0424d28411e7bc9ec7db0d9d357ce541e938823b1e48a8`.
The review independently binds every raw XML identity/counter/timestamp and
all 13 new executions, extracts all 94,282 raw 44-cell payloads from the current
XML, checks all 52 recorder closures and full key/input joins, and compares the
entire projection with the executed auditor. Source/JAR/command/runtime and
script-recovery chains verify. It reruns no numerical check. Initial metadata
reader size/format/name/schema corrections are retained; they changed no input
or numerical outcome and are not test/auditor retries.

These checks do not close physical maximum allocations, other range/scale
questions, wider subclass/API audits, actual PRNG properties, the525/557
JavaScript Number issue or official JCK conformance. Matching published-head
hosted/platform/public evidence is separate from this local result.

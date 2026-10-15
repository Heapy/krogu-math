# Combined pow, sqrt and Kotlin floating conversion verification

This records parent grant1919 on local main604ac4b plus the reviewed, uncommitted
task526 power fix, task565 midpoint fix and task525 floating conversions.
It is not official JCK/TCK evidence or a claim of complete compatibility.
Task528's measured V1 candidate is not integrated: its comparison allocation
regressed. Its V2 allocation follow-up is a separate, unexecuted source milestone.

## Changes and mathematical scope

- `pow(n, mc)` computes working precision in Long before capping the internal
  context at Int.MAX_VALUE. Supported exact products and terminating reciprocal
  cohorts fit both the capped and mathematical working precisions. The proof is
  conditional on the existing arithmetic completing; it does not repair every
  intermediate range or formatting limit. See
  [the power proof](decimal-pow-working-precision.md).
- Inexact `sqrt(mc)` compares quotient/delta/remainder with the rounding midpoint
  without shifting the numerator left by two or forming a larger squared
  midpoint. Existing finite-exact-root handling and preferred cohorts remain.
  See [the midpoint proof](decimal-sqrt-midpoint-565-proof.md).
- Common Float/Double `toBigDecimal` overloads preserve canonical text semantics,
  then apply the requested context. Float has an independent binary32 interval
  selector; it does not use widened Double text. Double uses the existing own
  canonical factory. See
  [the floating conversion proof](kotlin-floating-conversion-migration.md).

Java source adaptation remains confined to the public-test harness where needed.
Legacy/cross-format Java serialization is outside scope. No serializer, new
per-instance cache, existing BigInteger arithmetic kernel or frozen generated oracle changed.
Kotlin/JS erased Number/generic dispatch remains unresolved in task557.

## Actual serial platform gates

All commands use pinned BellSoft25.0.4+9-LTS, Gradle9.8.1 and Kotlin2.4.21,
`--no-daemon --max-workers=1`, a2GiB in-process compiler heap and isolated
`-Pkotlin.user.home=/private/tmp/krogu-root526565525-kotlin`. Each target
independently repeats `--tests 'io.heapy.krogu.math.*'`.

| Gate | Result | Suites/methods | Failure/error/skip/missing/extra |
|---|---|---|---|
| JVM full retry | exit0,1m7s | 109/360 | 0/0/0/0/0 |
| JS/Node | exit0,14m8s | 50/233 | 0/0/0/0/0 |
| Wasm/Node | exit0,2m5s | 50/233 | 0/0/0/0/0 |
| macOS ARM64 | exit0,7m | 50/233 | 0/0/0/0/0 |

The source-derived XML auditor requires every declared common/JVM test method,
rejects missing/extra/duplicate methods and actual failure/error/skipped XML
descendants as well as aggregate counters. Exact target prefixes are normalized;
no XML is rewritten. All16 new common methods execute on each completed target;
all6 new JVM methods and both retained power-boundary methods execute on JVM.
These counts include the complete retained package selection:1059 successful
method executions across the four final commands. The initial failed360-method
JVM invocation is separately retained below and is not hidden in that total.

Raw logs/XML, complete method/XML-hash audits and exact commands are retained at
`/private/tmp/krogu-root526565525-validation-v1`. The metadata auditor SHA256 is
`ecc5fa7a2d93f554467430ab0b35e0062eca58a9da2b45175f3b543b836759c5`.

## Initial failure and retained reference observations

The first full JVM command exited1 in2m55s:360 methods,359 passing and one
failure. PortableOracleTest still required the pinned JDK constructor error for
`ONE.pow(1, MathContext(Int.MAX_VALUE, UP))`, while the corrected value was1.
Every new method and both power-boundary methods already passed that invocation.
The original log and complete XML remain in `jvm-test.log` and `xml-jvm-initial`.

Independent review accepted a correction limited to exactly128 generated plain
ONE boundary keys. Every original64 error/64 value diagnostic remains asserted,
the mathematical result1 is checked independently, and a duplicate-free complete
key bijection is required. Other evaluator code, callback classifications,
indices and assertions remain unchanged. Pre-classification source and the
original JVM test are preserved in
[the diagnostic archive](../diagnostics/decimal-pow-working-precision/README.md).
The generated Jdk25Vectors.kt remains SHA256
`cb4d2327da9850e347dd1db6b96519bb228a0072ccd778b8372a647cd7f1ac4f`.

The JVM working-precision boundary method retains all128 original reference
inputs and prints the complete outcome strings, including messages. The separate TSV capture audit
passed:128 unique original keys,64 values and64 diagnostic errors. The actual
[capture](../diagnostics/decimal-pow-working-precision/reference-outcomes.tsv)
has SHA256 `0e23103fc29a148a15787dbe79f0d67befdcac64667e78ecd00f97125f0c00ec`.
No JDK outcome is silently reconstructed. Its supplied-JAR metadata does not
itself establish test-JVM/build provenance.

## Bounded midpoint certificate

The reviewed565 certificate SHA256
`dc61fce2adf1590c3e5ca352096dd1d7effa04d18659175b7abc6ff7e2b7997d`
actually compiled/executed under256MiB with a fresh source-keyed script cache.
It passed268320 independent bounded midpoint/exactness cases and the symbolic
Int.MAX_VALUE bit-domain/K646456992 witness. It executes no port numerical API
and creates no giant input. This is mathematical/source-bound evidence, not an
execution claim for maximum-sized objects. Log: `sqrt-certificate.log`.

## Fresh artifact and public tests

Production source/build manifest before execution SHA256:
`46486e0138f850331b6d48ffb0a5cfccafdb08daf2103b3b8599fa53ef8ca37d`.
It covers28 JVM production/build inputs. BigDecimal is
`5de18160de8d6787613a45a0b9d99e984921676b3dbd5aa5368833ebc1b7a6d7`;
BigInteger is
`0bcca3834a0839693782c5ff500d0ddc0f0dc8b6ad668695cb6d19c2ab7730da`.
Production compiled and the JAR task executed in the first JVM invocation;
the test-only retry correctly reused unchanged production outputs.

After all four platform commands and XML audits were terminal, a forced fresh
`--rerun-tasks jvmJar` build completed in7s with all3 tasks executed, including
compileKotlinJvm and jvmJar. Before/after28-input manifests are identical. The
saved read-only JAR is `artifacts/root-integration-fresh.jar`, SHA256
`daf2a13324f780b4b592e946c24dc601fc678d37f4954f0907fdbaea02674bdd`.
Build log SHA256 is
`e0816885bd3045ae261598ff3503fa253a75c5e419d4b4d2efcb2b2a3a8d08f0`.
No old root/worker JAR was substituted. The existing bounded packaged audit
passed3458 checks under256MiB and verified all four own math class origins at
this exact JAR. That audit rechecks retained finite-root behavior; the new
midpoint branch also has its own certificate and platform methods above.

The original public OpenJDK PowTests and SquareRootTests passed in baseline and
port:4 actual invocations,0 failures,136 outside-selection SKIP records.
Run1792025775874 is complete=true and officialJck=false; its result JSON SHA256
is `160f4324fe4aab991e7bdc654192adc684d79110397238ea7ce1da3c9b32bdd9`.
All compile/test commands exited0 without timeout or incomplete caught OOM.
Both port records verified own-core loading from the selected JAR; all four
test-class bytecode audits have empty hostMathMembers. Numerical test bodies,
directives and arguments remain unchanged. PowTests exercises exact pow(n),
so it is a retained-overload regression, not coverage of the new context path;
the common and JVM boundary controls supply that coverage.

The first harness attempt failed during JVM identity preflight because sandbox
process enumeration was denied. It attempted no mathematical tests and retained
an empty0/0/0 result ledger. The original log and partial run1792025664514 are
preserved. The same unchanged command/source/JAR then ran with ordinary scoped
access needed to track only its own children. No process was canceled, timeout
increased, source pruned or assertion weakened. Standard heaps remained512MiB;
physical36GiB and actual low free-memory samples are recorded without inventing
large-profile eligibility. Public tests and derivatives remain segregated under
ignored build/math-tck. These four passes do not rewrite the earlier broad
corpus ledger or resolve its resource/performance barriers.

## Compiler diagnostic and remaining gates

The prepared557 phase-list mode also executed successfully with pinned2.4.21:
KLIB and JS-link commands exit0; all owned processes terminal. Its actual
[catalog](../diagnostics/js-number/phase-list-2.4.21.txt) is retained. It executes
no Number fixture, disables no phase, and establishes no Number compatibility
fix. See [the diagnostic record](../diagnostics/js-number/README.md).

Fresh exact-published-head all-six CI for this combined source remains pending.
Task563's earlier finite-exact-root implementation separately reached DONE after
all six jobs at6efbd7b passed, including actual Linux ARM64:217/217.
Broader mathematical range, oracle classification, public-corpus resource/
performance, Number and JCK goals remain open.

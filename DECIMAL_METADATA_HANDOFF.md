# Task 528 code/proof handoff

SPDX-License-Identifier: Apache-2.0

The numeric digit/comparison source milestone incorporates parent review1855.
Task local:528 remains linked to pane session f5b3bd62 and in_progress.
Changes are not committed. No Git mutations were performed.
Base is `03ebb60a482873acb3fe892f0c86510472f2e0b3`, branch
`task/528-numeric-decimal-metadata`, worktree `/private/tmp/krogu-math-task-528`.

## Scope and frozen files

Tracked production diff: BigDecimal 14 added/10 removed lines, BigInteger
31 added lines. Only storedPrecision, compareMagnitude and compareValues
change in BigDecimal; BigInteger adds two internal @JvmSynthetic stored-value
helpers. No new fields, cache, serialization members, public API, arithmetic
kernels, constructors, formatting, binary conversion, fixture, existing test,
build, CI or harness change. Two independent new test files, two bounded
scripts, this handoff and the dedicated proof are added.

| File | Final SHA256 |
| --- | --- |
| src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt | 92de3354da9545f5b4867ee2ed32e72c91fac4ed32aa5c666f5762bd9f723370 |
| src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt | bdbd1a0f6d0ec9571c641b7a34ff73ac969da051c648fa6b606ff775c8739f57 |
| src/commonTest/kotlin/io/heapy/krogu/math/DecimalNumericMetadataTest.kt | c839d17adf6312e5faaa4a6336f75ee663f17eee901f6f545b5aa744ea055ddf |
| src/jvmTest/kotlin/io/heapy/krogu/math/DecimalNumericMetadataJdkTest.kt | 28815bdaa115bfffac83e9810b17c80a15751cfb804ce0b3810be899c726b849 |
| scripts/certify-decimal-metadata.main.kts | bf457b19276a9deb49d2804cf026eccb1f04e1f5575fd84d9985784161a50acc |
| scripts/audit-decimal-metadata.main.kts | 04f67b200bb0d0c689b04e48e5b76c719e1e29d9f145f517dd5cb7f293193815 |
| DECIMAL_METADATA_COMPARISON.md | 15b04b91dfccd5a646267fbee81674d674eb5e75ddb9e23cda4c83c02fda8e8e |

Baseline hashes are in the proof. The static audit confirms accepted task562
helpers, proof, certificate and prefix tests remain byte-identical to the base.
Existing fixture assertions remain unchanged. Current own-format JVM object
resolution can retain a coefficient subtype; the revised proof conditions
numerical equivalence on truthful stored-value consistency and classifies
removed inherited abs/string traces as observations. Explicit caller
dispatch/super and directly thrown exception identity remain guaranteed,
without a blanket guarantee or waiver for poisoned inherited outcomes.

## Original milestone executed checks

At the original milestone, `git diff --check` passed. Both then-final scripts
exited 0, with an explicitly
verified 268435456-byte maximum heap and distinct created cache directories
keyed by source snapshot and flags. They execute no math-library code.
The rational certificate verifies global fixed-point/threshold bounds and
784 symbolic digit/Long-scale boundary comparisons. The source audit verifies
the exact ownership diff, no formatting/padding in the changed digit/comparison
code and accepted artifact hashes. That historical log retains the original
hashes; the table above is updated for revision1855 below.

Exact original commands (run sequentially from the worktree, before1855):

```sh
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca JAVA_OPTS='-Xmx256m -Dkotlin.main.kts.compiled.scripts.cache.dir=/private/tmp/task528-cert-bf457b19-xmx256m' KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/task528-cert-bf457b19-xmx256m /Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin scripts/certify-decimal-metadata.main.kts > /private/tmp/task528-certificate-bf457b19.txt 2>&1
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca JAVA_OPTS='-Xmx256m -Dkotlin.main.kts.compiled.scripts.cache.dir=/private/tmp/task528-audit-4f01b3f0-xmx256m' KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/task528-audit-4f01b3f0-xmx256m /Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin scripts/audit-decimal-metadata.main.kts > /private/tmp/task528-audit-4f01b3f0.txt 2>&1
```

Both cache directories were created before execution. Each contains its own
compiled script jar after execution. Source changes or changed flags require
a different cache identity. No dependencies are downloaded by these scripts.
Running through the exact runner above needs no executable bit; `chmod +x`
would only be needed for direct shebang execution with a suitable shebang.

| Archived output | SHA256 |
| --- | --- |
| /private/tmp/task528-certificate-bf457b19.txt | 3150a1b909efab438edde14f332969f12a3594de4d61e9beb10b2b2158e35bfb |
| /private/tmp/task528-audit-4f01b3f0.txt | fbe019a31f976a71e281d1e296624c12e2759a05327a87fe244e840ef8f8c2c7 |

Final certificate output:

```text
heap cap=268435456; script cache=/private/tmp/task528-cert-bf457b19-xmx256m
exact rational log bounds certify E<=D<=E+1 for b<=Int.MAX_VALUE
max E=646456993; max fixed-point product=2776511640619672956 <2^62
10^(max E-1): 2147483644 bits; 10^(max E): 2147483648 bits
784 exact symbolic digit/Long-scale endpoint certificates
equal-decade divisor exponent <=Dlong-1<=646456992, divisor<=longer coefficient
PASS: no port execution, formatter, decimal parser, or giant allocation
```

Historical audit failure is preserved here: the initial audit snapshot
`f65441e4fa585d39d93c62716c46c00f23028ba1d94cc5e214fed44d876ded12`
exited 3 with:

```text
java.lang.IllegalStateException: internal fun compareDecimalSignificand(other: BigInteger): Int contains abs(
    at Audit_decimal_metadata_main.<init>(audit-decimal-metadata.main.kts:55)
```

This was a false positive in the new source audit: the matched text was
`abs(difference)` in a proof comment, not an executable abs call. The audit
now removes line comments before forbidden-call scanning. The intermediate
corrected snapshot f45cd2cb also passed. Early script invocations used the
documented cache environment variable alone; no cache directory was observed,
so those runs are not used as evidence of verified cache isolation. The final
commands above explicitly set the system property, create distinct directories,
log the property/heap, and have verified per-snapshot jars. An intermediate
certificate log is retained at /private/tmp/task528-certificate-e44d791c.txt.

## Original independent delegated review

Read-only reviewer `/root/numeric_bounds_review` inspected the actual source,
all new tests, proof, certificate, and final audit changes. It reported no
actionable correctness issue and executed no scripts, library code or tests.
Its original conclusions are retained below. Parent review1855 subsequently
identified the proof's unconditional base-coefficient assumption as false;
the revised proof and follow-up supersede that assumption and any unconditional
reading of the original inherited coefficient callback statement.

- BigInteger.kt:615 selects exact E/E+1, with equality and maximal unsupported
  threshold handled correctly. Existing pow at line116 has supported
  intermediates/guards for all constructed thresholds.
- BigInteger.kt:626 implements quotient/remainder order with correct shorter
  orientation, ignored signs, zero, trailing zeros and distant nonzero tails.
  The divisor is <=longer input; divideStored's shifted partial remainders are
  bounded by the processed dividend prefix.
- BigDecimal.kt:559 retains public compact/sign/precision behavior. Misleading
  precision still controls differing reported exponents; equal reported
  exponents preserve stored-significand order. Exception identity and direct
  super semantics remain intact for explicit caller controls; this does not
  guarantee inherited coefficient abs/string callback counts.
- BigDecimal.kt:754 has correct full-Long guard algebra. Callers at254/298
  consume only sign/equality, so normalized return magnitudes preserve behavior.
- Prepared tests have correct literals, scale ranges, callback traces and
  exception assertions. Reflection naming remains a later runtime check.
- The rational certificate uses the correct omitted exponent/denominator513
  and positive tail bound; its 784-case count is correct.
- The final audit's line-comment stripping fixes the false positive and still
  scans executable helper code. Its brace scanner is adequate for these exact
  sources, not a general Kotlin parser. Heap is enforced; cache identity is
  logged and supplied by the final invocation.

## Parent1855 proof and current-format test revision

Parent review1855 accepted the production mathematics/source subject to gates
and corrected the proof's claim that both private callers necessarily receive
base BigIntegers. The current own JVM defaultReadObject/non-null check permits
ObjectInputStream.resolveObject to retain a consistent coefficient subtype.
The previous unconditional base-coefficient assertion is withdrawn. No serializer,
constructor, legacy stream, cross-format behavior, or production code is changed.

The revised proof admits this reachability for both callers and proves numeric
precision/order for truthful stored-value-consistent coefficients. Removed
internal abs/toString callbacks are observations, not inherited BigDecimal
callback-count guarantees. Explicit caller override/super behavior and directly
thrown exception identity still apply; neither every poisoned inherited outcome
is guaranteed nor every such outcome waived.

Exactly one bounded JVM method was added to the owned test file:
`currentOwnFormatResolvedTruthfulCoefficientsKeepNumericsAndExplicitDispatch`.
It writes current own BigDecimal streams and resolves each coefficient to a
truthful subtype for zero and positive/negative 21-digit values. Prepared checks
cover retained coefficient identity/value, independent digit count and ordinary
precision control, equal-valued and one-unit neighbor coefficients at a different
scale, both comparison directions, and ordinary numerical controls. Separately,
explicit abs/string calls check override entry, a direct super string call
bypasses the explicit override, and a directly thrown exception retains identity.
No inherited abs/string trace count or poisoned inherited result is asserted.
Current-format runtime behavior is still **UNRUN**.

Production hashes and the seven-common-test hash are unchanged from 1850 and
now enforced by the bounded source audit itself. The new JVM/proof/audit hashes
appear in the current table above. Original 1850 hashes remain in comment 1850
and the archived original audit log; the original handoff hash was
400a4b9bab7e6dfbcbb2d7f44e37e888ad64016ccdf355e44b0b33ddc8b23553.

The refreshed read-only source/hash audit exited 0 with a verified 256MiB heap
and a new explicitly created source/flags cache. Exact command:

```sh
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca JAVA_OPTS='-Xmx256m -Dkotlin.main.kts.compiled.scripts.cache.dir=/private/tmp/task528-audit-1855-04f67b20-xmx256m' KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/task528-audit-1855-04f67b20-xmx256m /Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin scripts/audit-decimal-metadata.main.kts > /private/tmp/task528-audit-1855-04f67b20.txt 2>&1
```

The cache contains its compiled audit jar. Output includes
`PASS: review1855 frozen production and seven common tests unchanged` and the
current frozen hashes. Archived log `/private/tmp/task528-audit-1855-04f67b20.txt`
has SHA256
`7f24c9c5983b2ec512705f81255732232e2cfc72b422b7dd2f7a3c74e5eaae03`.
`git diff --check` also passed. The numerical certificate/source is unchanged;
its original bounded execution remains the certificate evidence. No library,
serialization runtime or numerical test was executed during this revision.

Independent reviewer `/root/numeric_bounds_review` performed the requested
read-only follow-up on actual test/proof/source and the audit's frozen assertions.
It withdrew its earlier unconditional base-coefficient statement, verified
current-format test structure, signs/zero neighbor orders and explicit-dispatch
separation, and found no apparent Kotlin signature/source compilation hazard.
It identified one remaining unqualified proof sentence about return normalization;
that sentence was narrowed to comparison-result branch decisions for truthful
consistent inputs. Final follow-up confirmed the finding resolved, hashes
matched and no remaining actionable issue. Reviewer executed no scripts,
builds, tests or port code. Static acceptance leaves runtime verification open.

## Awaiting parent execution grant

No library compilation, Gradle, Native/linking, javac, numerical probe,
JDK/port comparison campaign, benchmark, public harness, or background test
job was run. New tests are **prepared**, not passed: seven common methods and
three JVM methods after revision1855. The symbolic giant input is never allocated.
No measured speedup, allocation reduction, or instance-size result is claimed.
No new field/cache means no new cache publication race or retained cache cost
by source structure; actual measurement remains required.

After the parent grants the sole execution window, planned complete retained
suite commands are separate target invocations with trailing wildcard filters
repeated for every target:

```sh
./gradlew --no-daemon jvmTest --tests 'io.heapy.krogu.math.*'
./gradlew --no-daemon jsNodeTest --tests 'io.heapy.krogu.math.*'
./gradlew --no-daemon wasmJsNodeTest --tests 'io.heapy.krogu.math.*'
./gradlew --no-daemon macosArm64Test --tests 'io.heapy.krogu.math.*'
```

These include existing comparison/context/integral dispatch, decimal arithmetic
and binary suites as well as new tests. Actual resource flags, any authorized
subset staging, timeout and XML assertions belong in the later explicit grant.
Performance/allocation cohorts and evidence obligations are specified in the
proof. Root alone integrates serially, performs Git mutations, and requires
all-six CI at the exact integrated head before acceptance/closure. This
milestone does not complete the numerical performance task.

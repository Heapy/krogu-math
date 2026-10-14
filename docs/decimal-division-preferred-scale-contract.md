# Decimal division with preferred scale outside Int

Task `local:560`, isolated base `0150163d6931fc9b1ac524293ef64ad6a14b2a1e`.
Independent common Kotlin, Apache-2.0. Only own code, public API documentation,
independent bounded models and public black-box calls were used. No numerical
implementation source from OpenJDK, Kotlin stdlib or another library was read.

## Proved contract and witnesses

The [Java SE 25 BigDecimal contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
defines coefficient times ten to minus scale, exact/unlimited division, preferred
scale as the difference of operand scales, finite-precision rounding and cohort
selection. Exact division may need a larger scale to express fractional digits;
finite division selects digits before validating the result scale.

Write `c@s` for `c * 10^-s`, `MIN=-2147483648`, `MAX=2147483647`.
For `1@MIN / 2@1`, preferred is `MIN-1`. The reduced rational `1/2`
requires one decimal place: `5@MIN`. Cross multiplication is `1*10 = 2*5`.
All signs, exact division, precision zero in all eight modes, and every finite
precision at least one require this same pair. No rounding or huge power is needed.

For `10@MAX / 1@-1`, preferred is `MAX+1`. At precision one the exact
one-digit pair is `1@MAX`: `10 = 1*1*10`. All modes and signs require it.
Exact, precision zero and precision at least two are **admitted diagnostics**,
not mandatory rescues or universal mathematical rejection assertions. This task
makes no claim that their smaller-scale alternative cohort must be chosen.

Genuine controls are `1@MIN / 1@1` at precision one (every supported
cohort needs at least two digits), `1@MAX / 1@-1` (supported scales need a
fractional coefficient), nonterminating exact quotients, inexact UNNECESSARY,
zero divisors and null operands/contexts. Exception messages remain observations.

## Independent models and bounded scope

`DecimalDivisionPreferredScaleContractTest` crosses four operand sign combinations,
four ordinary/truthful delegating operand masks, all eight rounding modes,
exact/unlimited/finite precisions, reduced 2/5 denominators, factor cancellation,
carry neighbors, inexact quotients and small exact cohorts at precision MAX.
Its first model uses only bounded Long arithmetic. It cancels a rational after
multiplication by ten, locates the decimal exponent by integer comparisons,
rounds quotient/remainder using twice the remainder and quotient parity, and
validates the final pair. Every multiplication checks its Long bound.

The second model uses elementary decimal-string subtraction, small multiplication,
long division and Euclidean cancellation, independent of either BigInteger or
BigDecimal implementation. A portable test compares it to the Long model and
checks a 100-digit closed-form carry neighbor. It supports bounded wide consumer
fields and preserves genuine errors; a substitution requires an independently
computed supported final pair. Its text conversion also uses independent decimal
placement/exponent arithmetic. Old frozen and live JDK results remain diagnostics.

LOW cases whose reduced natural exact scale E is below MIN need additional zeros
to reach a supported cohort. These are admitted diagnostics: the kernel retains
its exception, models label them separately, and tests permit either that exception
or an independently validated alternative pair if its digits fit precision. Their
representability is not a proof of a mandatory rescue. No consumer substitutes
these fields. This matches the restraint for HIGH exact/p0 cases.

Required successes are limited to natural exact E within Int, or finite rounding
whose selected digits (including any carry) give a supported final scale. For the
wide finite witness `29804050770374717654296164957404873906@0 / 10^19@MIN`,
precision one HALF_EVEN rounds coefficient rational about 2.980405e18 upward
from two to three at scale `2147483630`. Twice the remainder exceeds the
scaled denominator; this unique one-digit rounded pair is `3@2147483630`.
The same bounded quotient/remainder argument determines directed modes and signs.

## Kernel changes

Exact/context division keeps preferred and working scales in Long through rational
coefficient selection, digit counts, carry and cohort handling. Finite wide division
retains exceptions for the unproved natural E below MIN family and avoids constructing
a huge precision-sized coefficient for a small exact result. Final scale validation
still rejects unsupported results. Existing 558 normalization/carry handling remains.
The eager exact preferred-scale check's source-sign callback disappears with that
check; callback order is an observation, not an invented arithmetic requirement.
No runtime-class exclusions, fake-getter emulation or unit whitelist were added.

## Evidence and remaining gates

Local evidence lives in `/tmp/krogu-560-evidence`:

- Baseline unchanged core: seven new tests, six failures, zero errors/skips, 55s;
  retained `baseline-jvm.log`, XML and source/test/log SHA-256 digests.
- Independent `/tmp/preferred-scale-range-rational-proof.main.kts`: PASS all four
  sign masks; the replay log is `rational-proof.log`.
- First focused 558+560 JVM run: terminal PASS, 24s (before the precision guard).
- Initial broad JVM attempt: 131 tests, 16 failures, zero errors/skips, 27s.
  Failures are retained separately as `gate-jvm-attempt1.log` and XML; the four
  ordinary/live consumers need narrowly proved classifications, not fixture rewrites.
- First pinned audit: 46,624 model checks, 8,368 genuine-error controls,
  20,192 retained JDK differences, zero port differences; `audit-attempt1.log`.

The initial optional LOW padding policy was withdrawn after normative challenge.
An intermediate narrowed JVM attempt had 132 tests, two diagnostic-message
failures, zero errors/skips, 112s; log/XML are `gate-jvm-attempt2*`. The final
failure-only diagnostic mapping resolves those differences without changing success
classification. Its JVM gate passed 132 tests, zero failures/errors/skips, 30s.

Final-core audit `audit-final-v3.log` passed 46,624 small checks (8,368 genuine
errors, 3,216 admitted LOW padding outcomes, 16,976 required-result JDK differences)
and 1,152 wide finite checks (1,008 JDK differences); zero port differences.
The separate 400 HIGH exact/p0/p>=2 observations remain admitted diagnostics.
The tested runner is `/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin`,
which reports Kotlin 2.4.21 on Liberica 25.0.4+9-LTS; the skill's generic `kotlinr`
rename does not match this installed runner. Each audit/JAR snapshot used a new
compiled-script cache directory because the cache key ignores classpath/flags.

The independent reviewer found no actionable defect in the narrowed core, models,
consumer fallbacks or document. Parent activity 1751 separately approves the same
classification and wide finite proof. The static preflight for `1@MIN /1@600000000`
rejects natural E below MIN before coefficient construction, including at huge
finite precision; this giant-allocation witness was **not executed**.

The four granted consumer changes only append the new bounded outside-preferred
expectation after the unchanged 558 helper. They retain old JDK diagnostics;
no unsupported expectation or admitted LOW padding field is substituted. All
tracked frozen/generator files and the original 558 regression are byte-identical.

All gates use Liberica 25.0.4, Gradle 9.8.1, Kotlin 2.4.21, `--no-daemon`, and
`-Pkotlin.user.home=/private/tmp/krogu-560-kotlin-home`. The repeated filters after
each test task are `*DecimalDivision*`, `*ScaledDivision*`, `*Scale*`, `*Context*`,
`*Integral*`, `*PortableOracleTest`, `*CanonicalDouble*`, `*DecimalConformanceTest`,
`*JdkOracleTest`, and `*DecimalPairDispatchOracleTest`. JVM additionally builds the
fresh `jvmJar`; common filters run on JS Node, Wasm Node and macOS ARM. No test is
disabled/skipped, no process/CI run is cancelled, and no worker Git mutation occurs.

The three-target process finished exit zero in 13m02s: JS 95/0/0/0,
Wasm 100/0/0/0, macOS ARM 100/0/0/0 (tests/failures/errors/skips).
Its initial JS scope was incomplete: JS interprets exact class filter patterns
against full method names and omitted `PortableOracleTest` (four methods) and
`DecimalPairDispatchOracleTest` (one method). Original XML is preserved; the
missing five JS methods are refreshed with `*PortableOracleTest*` and
`*DecimalPairDispatchOracleTest*`. Final JS evidence combines both disjoint runs
on the unchanged final source; this is a filter correction, not a platform skip.
For full future reproduction use trailing `*` on both class patterns on each target.
The actual original and correction commands are in `commands.txt`.

The JS correction finished exit zero in 34s, five tests with zero failures/errors/
skips. The complete disjoint JS XML manifest has 100 unique methods, including
both previously omitted suites. Each runtime executes all eight new methods and
all six unchanged 558 methods. Final totals and XML manifest hashes are below.

| Runtime | Tests/failures/errors/skips | XML manifest SHA-256 |
|---|---|---|
| JVM | 132/0/0/0 | `fec9da77cd88e03f8ee40cb149a12235a9d7bf575f03bfa65f7067af1cf4b0e7` |
| JS | 100/0/0/0 | `1c4ae41a34249e48d292bbafd38e2e825fba51ce73702fa758b6dfbe258bde3d` |
| Wasm | 100/0/0/0 | `5ce0b2f9cf967d5211390557de651f4608b1ccc72c3f1da4e65b1aa63442ba28` |
| macOS ARM | 100/0/0/0 | `b98e5f5816f3007aab3c27d80b215b579f749304eb11f155f485916814c03be4` |

All 202 pre-existing tracked file digests were checked. Only BigDecimal.kt,
COMPATIBILITY.md and the four granted ordinary/live consumers differ; the three
new owned files are the test/models, main-kts audit and contract document. The
48-file frozen/generator manifest and original 558 regression are byte-identical.
Full baseline/final source manifests, four XML manifests, review note and command
record remain under `/tmp/krogu-560-evidence`. The generated untracked Wasm yarn
lock was preserved there and removed from the worktree after terminal completion.

All local Gradle/Native processes used by this task have reached terminal results.
The build window is released with the ref-less task review handoff; no further
worker builds are planned. Changes are not committed. Parent serial integration,
its focused integration gate and fresh supported CI precede parent closure.
Broad division task 541, range/resource task 548, classification 526 and the
full mathematical/source-migration objective remain open. Java production source
compatibility and legacy serialization are outside this task's contract.

## Parent integration validation

The parent copied exactly the six authorized changed files and three new files
onto main `0150163d6931fc9b1ac524293ef64ad6a14b2a1e`, retaining the reviewed
kernel, models and audit hashes. The complete local XML evidence was independently
parsed using `.main.kts`: JVM 132, JS 100, Wasm 100 and macOS ARM 100 tests,
with zero failures, errors or skips. Every target includes all eight new tests,
six retained normalization tests, four portable oracle methods and the pair suite.
The JS 100-method result includes the documented disjoint five-method correction.

The fresh main-worktree JVM integration command completed successfully in 82s:
22 tests, zero failures/errors/skips. The newly built JVM jar passed all 47,776
independent audit cases with zero port differences. Logs are retained at
`/private/tmp/krogu-root-560-integration.log` and
`/private/tmp/krogu-root-560-integrated-audit.log`. Supported CI for this
integration is a separate gate; the successful six-job CI for the base commit
does not prove the new division patch.

Final code/JAR SHA-256 (unchanged throughout the platform gates):

| Artifact | SHA-256 |
|---|---|
| BigDecimal.kt | `b207348a0249a48adb51e9a72d93f5fe2ab07dbf811dac2c9551eac34e1c23f0` |
| Preferred-scale test and models | `0783547ae604aa3ed8b290b99309f3010c1112d9c34a07a64e199be8dd582085` |
| Audit main-kts | `a6071a855e8e11678587812f819d6475cedbfb99cf71027930675b1b67017be2` |
| Fresh JVM JAR | `54ec9af64675eba2a89d7cba68a1c404220cfaf3169bd1de8a31193514e31db0` |

# Decimal division normalization contract

Task `local:558`, base HEAD `3c278955b858eb58a386a11ab1e1a2f85388a2da`.
Independent common Kotlin arithmetic, Apache-2.0. Only public API documentation,
own code and public black-box execution were used; no numerical implementation
source was read. Local verification precedes parent integration and supported CI.

## Contract and bounded witnesses

The public [Java SE 25 BigDecimal contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
requires exact division when the quotient is representable, and finite-context
division to round the mathematical quotient. Its preferred scale is the difference
of operand scales. An exact result chooses the representation closest to that
preferred scale within precision. A carry may reduce the final scale. Scale-range
permission applies to the result, not a manufactured normalization or padding scale.

Write `c@s` for coefficient `c` times `10^-s`, and `MAX=2147483647`.
`1@MAX /1@0` and `2@MAX /2@0` have the exact quotient `1@MAX`.
No additional decimal position or power is needed. Exact division, precision zero
in every mode, and finite precision at least one must succeed.

For `19@MAX /20@0`, precision one, scaling the rational coefficient by ten
gives `190/20 = 9 + 10/20`, at logical scale `MAX+1`. UP, positive CEILING,
negative FLOOR, HALF_UP and HALF_EVEN increment magnitude nine to ten.
Removing the carried digit yields `±1@MAX`. DOWN, the opposite directed mode
and HALF_DOWN require `±9@(MAX+1)`, which is unsupported; UNNECESSARY rejects.

For `1@MAX /10000001@7`, the corresponding division is
`100000000/10000001 = 9 + 9999991/10000001`. Its exact expansion is nonterminating,
yet all three half modes, UP and the appropriate directed mode carry into range.
These two signed families have 18 supported outcomes and 14 rejection controls.

At precision two, `1999@(MAX-1) /2000@0` with HALF_UP gives `10@MAX`.
It is inexact, so stripping another zero to `1@(MAX-1)` would select the wrong
cohort. `1@MAX /100@2` instead has exact quotient `1@MAX` after denominator
reduction, demonstrating cancellation of coefficient factors and operand scales.

## General arithmetic argument

Let `S=sourceScale-divisorScale`, within Int, and reduce the signed coefficient
fraction by its positive gcd. An exact decimal exists iff the remaining positive
denominator is `2^a * 5^b`. Set `d=max(a,b)`. Multiplying the reduced numerator
by `2^(d-a) * 5^(d-b)` gives coefficient `C` at scale `S+d`.
If `d>0`, coprimality excludes a trailing decimal zero in `C`; if `d=0`, its
scale is already preferred. Thus this is the required exact cohort and validating
`S+d` suffices. The conservative precision used by the old normalization check
has no independent role in its representability.

For finite precision `p`, compute coefficient exponent
`e=floor(log10(abs(sourceCoefficient/divisorCoefficient)))`. The working scale
is `W=S+p-1-e`, retained as Long. Integer division of the coefficient rational
scaled by `10^(W-S)` yields quotient and remainder. The remainder and sign
determine every rounding-mode decision. The unrounded quotient has `p` digits;
an increment that produces `p+1` digits is exactly a signed power of ten.
Dividing by ten and decrementing W preserves value and restores precision.
Only a zero remainder permits removing additional zeros toward preferred scale.
The final coefficient/scale pair is validated after these steps.

A reduced terminating quotient that already fits precision is selected before
precision padding. This allows even `p=Int.MAX_VALUE` to return a small exact
cohort without constructing a huge power. Nonterminating huge-precision results
still exceed the supported coefficient range. `1@MAX /2` still needs `MAX+1`;
exact `/3`, division by zero and inexact UNNECESSARY remain errors.

The reduced exact scale is preflighted **before** coefficient powers. If it is
unsupported, finite division skips that exact candidate and rounds the original
coefficient rational instead. Exact/p0 division rejects before the unnecessary
power. This is safe because the reduced fractional cohort cannot end in zero.
For terminating coefficient C with L digits and exact scale E, finite working
scale is `W=E+p-L`; the prior normalization scale T is W or W+1. If `E<=MAX<T`,
then `p>=L`: any remaining exact materialization in this upper normalization
category constructs the required exact result itself.

The bounded regression `65@MAX /(2^800@239)` uses working rational
`65*10^240 /2^800 = 9 + r/2^800`, with `2r>2^800`, at scale MAX+1.
Its exact cohort would be `65*5^800 @(MAX+561)`, which is unnecessary for
the supported rounded result `±1@MAX`. Across four sign combinations and eight
modes, 20 ordinary outcomes carry into range and 12 genuinely reject; all four
ordinary/delegating operand masks remain asserted.

The same issue is reachable at the supported coefficient boundary without
executing a giant test. Let `k=1_100_000_000`, `D=2^k`, N be its decimal digit
count, and `c=floor(D/10^(N-3))`, so `100<=c<=999`. Divide `c@MAX` by
`D@(N-3)` at precision one. The scaled rational lies strictly between
`1000/101` and 10, so half/up and appropriate directed modes carry to `1@MAX`.
Its working numerator is less than `10D`, needing at most k+4 bits; the shift
`N-2<k/2` is below the existing 646456992 bound, since `2^k<10^(k/2)`.
But gcd cancellation removes at most nine twos, and the exact coefficient
requires `5^(k-v)`, whose existing own power guard `2*(k-v)>=Int.MAX_VALUE`
rejects before rounding. Its exact scale is also strictly greater than MAX.
The new scale preflight skips this unnecessary coefficient. This is a static
integer argument only; the giant operand and powers were never constructed.
Broader arbitrary coefficient/intermediate resource handling remains `local:548`.

## Scope and retained evidence

Production changes stay in exact/context division. The wide finite path activates
only for failed normalization or unsupported padding distance with in-range
preferred scale and already-observed precision equal to stored digit counts.
It checks stored nonzero coefficients and does not test runtime class. Ordinary
and fully delegating subclasses use the same mathematics. No extra public
callbacks are introduced on the finite path; exact division retains its existing
sign callback. False-precision and zero-coefficient probes retain their old path.
If the repaired path has no supported result, the original normalization error
diagnostic is retained. This does not establish unspecified callback ordering as
a new contract or classify every old subclass observation.

The new common suite uses bounded integer cancellation and repeated rational
comparisons, independent of production normalization. It covers both operand signs,
all four ordinary/delegating masks, exact and unlimited division, all eight modes,
finite contexts, terminating factor reduction, carry neighbors and huge precision.
The bounded `2^800` denominator has 241 input digits but needs 800 decimal
places; its 560-digit exact coefficient covers final scales MAX and MAX+1.
The expected model therefore bounds cancellation by decreasing denominator bit
length, rather than imposing an incorrect input-decimal-digit place limit.
For that supplemental witness, the repeated-cancellation model is checked once
against `2^-800 = 5^800 * 10^-800`. All 24 production result assertions remain;
they reuse the proved coefficient to avoid redundant expectation computation.
The standalone [audit](../scripts/audit-decimal-division-normalization.main.kts)
uses a separate checked-Long model; overflow in that model fails the audit.
On pinned BellSoft Liberica `25.0.4+9-LTS`, the original 17,712 cases retain 6,416
reference differences and include 6,656 rejection controls. The final extended
audit adds the 32 preflight witnesses: 17,744 cases, 6,436 retained reference
differences and 6,668 rejection controls. Both sets of signed carry observations
retain the reference exception messages.

Frozen fixtures and generators remain unchanged. Consumer substitutions are
restricted to bounded ordinary input fields, failed reference normalization and
a separately computed supported final pair. The reference error is still consumed
or executed and asserted as retained diagnostic evidence. Genuine error fields
continue to use their original observations.

## Verification

Use pinned Java and the task-owned temporary Kotlin npm home:

```text
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca
-Pkotlin.user.home=/private/tmp/krogu-558-kotlin-home
```

The failing baseline used unchanged production:

```text
./gradlew -Pkotlin.user.home=/private/tmp/krogu-558-kotlin-home jvmTest --tests '*DecimalDivisionNormalizationContractTest*'
```

Terminal exit 1, 2m32s; XML: 4 tests, 3 failures, 0 errors, 0 skips.
Failures prove exact cancellation, carry and huge-precision exact cohort defects.
Baseline production SHA-256: `494739170b49ad10fbb790bfbaeb49411da90a53bbc9e7b84e115019ef7896a6`.
Log `/private/tmp/krogu-558-baseline.log` SHA-256:
`aa18db61cf3ca200bdfd9266bbe633f40fb0a467cfb93c7a4c3d53e34c1ee2a9`.
Saved baseline XML SHA-256:
`a80ba4a1b4200f9065f9c1651d262ad2df5a044d45a469b46f741b6ec4833992`.

The baseline audit was terminal exit 0 (diagnostic mode), with 6,416 port
differences. After repair the same 17,712-case audit was terminal exit 0 with
`--check-port`, zero port differences and the same retained reference count.
Audit baseline log SHA-256:
`726ced5a4ddc8877da30c1ed19cdf2df88858abae6e75d48938ce33fbb056e4a`.
Preflight-free audit log `/private/tmp/krogu-558-audit-final.log` SHA-256:
`a3fa07bd6d906f7b8dd1f0f6e7f6f65a72121eefd48014224ab22ac512236556`.

Intermediate widened JVM runs exposed reference-consumer expectations that
needed narrow correction; they were not reported as passing:

| Run | Terminal | XML tests/failures/errors/skips | XML manifest SHA-256 |
|---|---|---|---|
| `/private/tmp/krogu-558-jvm-first.log` | exit 1, 1m21s | 93/12/0/0 | `5b90e4e9cca18caf2b24226f68efe9dde80e6edd1be44f3e28d76b15b8054a7c` |
| `/private/tmp/krogu-558-jvm-second.log` | exit 1, 2m41s | 96/4/0/0 | `9a33884931cebdf09c44a8c3f901f8d95aa81b9400b17ba98dadb239c95bfd65` |

The second run's four assertions are the exact and finite precision campaigns
in `DecimalDivisionPrecisionTest`, plus compact and wide live campaigns in
`ScaleBoundaryConformanceTest`. The latter wide run reports 12 exact and 46
finite-context supported-result differences in 68,000 comparisons. The owned
portable and frozen scale consumers pass after independent success-only
substitutions. Ownership expansion was requested before touching these two
additional consumers; parent task comment 1710 granted that ownership before
their success-only live diagnostic substitutions. Earlier diagnostic-only mismatches were eliminated by
retaining old diagnostics for genuine errors.

The initial three-target gate was terminal exit 0, 22m46s. It used the seven
common filters below on **each** of jsNodeTest, wasmJsNodeTest and
macosArm64Test; saved XML directories are `/private/tmp/krogu-558-initial-<target>`.

| Initial target | Tests/failures/errors/skips | XML manifest SHA-256 |
|---|---|---|
| JS | 75/0/0/0 | `4f5368a08e641885c4b5ab1da60855f6d4b07e5ea72413cd9641aa0fcfb0d8a0` |
| Wasm | 76/0/0/0 | `8a2ede6379e884fbeb26cfcd79d89b6cbeafd382dfba3e8a1b20bcc92d278c55` |
| macOS ARM | 76/0/0/0 | `d30dda57191d1094ab874d3388f167f6206ee4f9bbaa605a812a993a3f1a3f94` |

Initial log `/private/tmp/krogu-558-nonjvm.log` SHA-256:
`a2988d44cc5d2dbe3f05143152b1f3fab745e70b818f68ba27d2e3dc903e263d`.

The preflight-free invocation followed the initial source edits. JVM checks corrected live consumers;
JS repeats the full selected common gate because its earlier snapshot lacked the
fifth regression. Wasm and macOS ARM refresh the modified normalization suite;
their retained full gates above already passed on unchanged production and
expected-model arithmetic. Every task receives its own repeated test filters.

```text
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca ./gradlew -Pkotlin.user.home=/private/tmp/krogu-558-kotlin-home \
  jvmTest --tests '*DecimalDivisionNormalizationContractTest*' --tests '*Scale*Test*' --tests '*DecimalContext*Test*' --tests '*DecimalIntegral*Test*' --tests '*PortableOracleTest*' --tests '*CanonicalDouble*Test*' --tests '*Division*Test*' --tests '*JdkOracleTest*' --tests '*DecimalConformanceTest*' \
  jsNodeTest --tests '*DecimalDivisionNormalizationContractTest*' --tests '*Scale*Test*' --tests '*DecimalContext*Test*' --tests '*DecimalIntegral*Test*' --tests '*PortableOracleTest*' --tests '*CanonicalDouble*Test*' --tests '*Division*Test*' \
  wasmJsNodeTest --tests '*DecimalDivisionNormalizationContractTest*' \
  macosArm64Test --tests '*DecimalDivisionNormalizationContractTest*' > /private/tmp/krogu-558-final.log 2>&1
```

Preflight-free arithmetic/test/script/compatibility source manifest
`/private/tmp/krogu-558-source.sha256` SHA-256:
`be7ed59379ac67dfd283a25f990f2640689b50374c090fe0b642102ac6bc4af1`.

That invocation was terminal exit 0, 8m52s: JVM 97/0/0/0, JS 76/0/0/0,
refreshed Wasm 5/0/0/0 and macOS ARM 5/0/0/0.

The first non-JVM process began before the additional `2^800` regression was
written. Its first JS XML contains the earlier four-method suite. The 8m52s
refresh included all five methods; the final preflight gate below includes all
six. That earlier snapshot alone is not the final gate.

The preceding 8m52s invocation passed before the independent preflight finding.
It is retained as intermediate evidence, and does not verify the final kernel.
The final preflight-source command and terminal evidence below supersede it.

```text
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca ./gradlew -Pkotlin.user.home=/private/tmp/krogu-558-kotlin-home \
  jvmTest --tests '*DecimalDivisionNormalizationContractTest*' --tests '*Scale*Test*' --tests '*DecimalContext*Test*' --tests '*DecimalIntegral*Test*' --tests '*PortableOracleTest*' --tests '*CanonicalDouble*Test*' --tests '*Division*Test*' --tests '*JdkOracleTest*' --tests '*DecimalConformanceTest*' \
  jsNodeTest --tests '*DecimalDivisionNormalizationContractTest*' --tests '*Scale*Test*' --tests '*DecimalContext*Test*' --tests '*DecimalIntegral*Test*' --tests '*PortableOracleTest*' --tests '*CanonicalDouble*Test*' --tests '*Division*Test*' \
  wasmJsNodeTest --tests '*DecimalDivisionNormalizationContractTest*' --tests '*Scale*Test*' --tests '*DecimalContext*Test*' --tests '*DecimalIntegral*Test*' --tests '*PortableOracleTest*' --tests '*CanonicalDouble*Test*' --tests '*Division*Test*' \
  macosArm64Test --tests '*DecimalDivisionNormalizationContractTest*' --tests '*Scale*Test*' --tests '*DecimalContext*Test*' --tests '*DecimalIntegral*Test*' --tests '*PortableOracleTest*' --tests '*CanonicalDouble*Test*' --tests '*Division*Test*' > /private/tmp/krogu-558-preflight-final.log 2>&1
```

Every target repeats all seven common filters; the JVM also retains both live
JDK consumers. The final six-method suite passed on every target.

Final preflight gate: **terminal exit 0, 12m02s**. No failures, errors or skips.
The report contains 329 test methods across the four targets; each target
includes the full selected scale/context/integral/portable/canonical Double
gate, in addition to the six normalization methods.

| Final target | Tests/failures/errors/skips | XML manifest SHA-256 |
|---|---|---|
| JVM | 98/0/0/0 | `c13d88656c1f775bb9fdabf43fabb68dc2a47de380b6e47a6c7b1d52a832d95c` |
| JS | 77/0/0/0 | `43817a3641719bc26e437679a4ed324f251a6e6e5e596283bb888b8a33f89849` |
| Wasm | 77/0/0/0 | `ad64f22d2496070059e512e6b637b525e85b33ac0b0a78f0a5723057f8aa5ae6` |
| macOS ARM | 77/0/0/0 | `b9ef6196f4d14facd0d2f327e8eba2e5bcc4bc7140f921b66d461d4829e00439` |

Manifests are `/private/tmp/<target>-558-manifest.sha256`. The final Gradle log
`/private/tmp/krogu-558-preflight-final.log` SHA-256 is
`0cf8112430bf2a37da28e0541b746b2073b3e733044ee40d8a70b9de8292d993`.
Generated untracked Wasm yarn-lock output was removed after terminal completion;
the tracked JS lock, build files, platforms, wrappers and dependencies are unchanged.

Extended audit command (terminal exit 0, zero port differences):

```text
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/krogu-558-cache-preflight JAVA_OPTS='-Xmx512m -XX:-OmitStackTraceInFastThrow' /Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin -classpath build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar scripts/audit-decimal-division-normalization.main.kts --check-port
```

Log `/private/tmp/krogu-558-audit-preflight-final.log` SHA-256:
`1735eb640da24668e762b036f3710193d666b2b421873235e4d6be1e09b753dd`.
Final kernel source SHA-256:
`aedb07b098531d0cff451ea3ac5bf3076e02b65854ab5da9b8adea3c8d723179`.
JVM JAR SHA-256:
`bd4cc200d2ec5f2ec41423b723d425b7e6fff012fda6784d2ecacf605858e2ad`.

Final arithmetic/test/script/compatibility source manifest
`/private/tmp/krogu-558-preflight-source.sha256` SHA-256:
`15ac7d7987cc964025a73c9d233097e701266df045d158e41841281b67f492a3`.
The independent frozen-file check is terminal exit 0: 50 fixture/generator/coverage
files byte-identical to HEAD. `/private/tmp/krogu-558-frozen.sha256` SHA-256:
`4120f71611333f54ad4237d2992848845ab47606422e689299056f47ca638a77`.

```text
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/krogu-558-cache-frozen-v2 JAVA_OPTS='-Xmx512m' /Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin /private/tmp/krogu-558-frozen-check.main.kts
```

Parent integration verification used the same arithmetic, test and audit source
hashes. The focused JVM invocation with `--no-daemon` passed in 44s: six new
normalization tests and four live precision/scale tests, with zero failures,
errors or skips. A fresh parent-JAR standalone audit again checked 17,744 cases
with zero port differences, 6,436 retained JDK differences and 6,668 rejection
controls. The parent logs are `/private/tmp/krogu-root-558-integration-no-daemon.log`
and `/private/tmp/krogu-root-558-integrated-audit.log`.

Preferred scales outside Int are excluded from this proved domain. Practical
multi-gigabyte coefficient limits, all remaining poisoned-probe classification,
legacy serialization and plus(mc) zero-scale questions are outside this task.
Parent fresh full supported CI is a subsequent closure gate. This evidence does
not complete `local:526`, broad `local:541`, or the full compatibility goal.

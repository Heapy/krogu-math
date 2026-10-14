# Exact decimal root: scoped evidence for local:563

**Superseded draft.** Parent review comments 1812/1815 found that formatting the
full input coefficient invalidates this draft's full supported-domain claim on
Node. The factorization theorem below remains mathematically valid, but the
input-string implementation and callback-preservation assessment were withdrawn.
Use [the current parity proof](decimal-exact-root-563-parity-proof.md) and its
recorded callback scope request for the revised source. This historical document
must not be used as an integration approval.

Worktree `/private/tmp/krogu-math-task-563`, branch `task/563-exact-root`, baseline
`85941d18908a8155d205b04d6e1a9b3e8f10b31e`. Changes are not committed.
The initial evidence stopped before production changes. Parent comment **1803**
subsequently authorized the exact requested sqrt assertion routing expansion.
The implementation now changes only `BigDecimal.sqrt`/two new private helpers
and that retained test's sqrt routing, plus new focused tests/evidence. Frozen
fixtures, generators, other production methods, build and CI remain unchanged.

## Scope decision and parent grant

The worker prompt explicitly says: “If the minimal fix necessarily changes
existing diagnostics/tests, STOP at scoped evidence and request exact expansion”.
That condition holds on ordinary operands, independently of poisoned subclass
callbacks. The full requested domain cannot pass the existing direct square-root
assertions in `ScaleBoundaryOracleTest` unchanged.

Granted expansion (comment 1803): edit only the square-root assertion routing in
`src/commonTest/kotlin/io/heapy/krogu/math/ScaleBoundaryOracleTest.kt`, supported by
a new independent exact-root expectation helper/test. Keep consuming every frozen
outcome at its original position, explicitly assert the retained JDK diagnostic
for each proved difference, and assert the mathematical coefficient/scale on the
implementation. No frozen data, generators, other operation routes, or assertion
disabling is requested. Further conflicting callback expectations require their
own evidence and exact expansion before any changes. This is a scope request,
not an execution-window request.

## Ordinary retained counterexamples

Notation `c@s` denotes the value `c * 10^(-s)`.

| Input | Unlimited (all modes) | Precision 1 (all modes) | Precision 2 | Precision 7 |
| --- | --- | --- | --- | --- |
| `100@-2147483648` | `10@-1073741824` | `1@-1073741825` | `10@-1073741824` | `10@-1073741824` |
| `1000@-2147483647` | `100@-1073741823` | `1@-1073741825` | `10@-1073741824` | `100@-1073741823` |

These are identities between symbolic powers of ten. No enormous power, number,
or decimal string is needed. Their preferred scales are the input scale divided
by two with integer truncation toward zero. Precision 1 and 2 select the closest
cohort that fits the digit budget. Every listed result is exact, so its rounding
mode cannot require rejection.

For precision zero or `UNNECESSARY`, the baseline `BigDecimal.sqrt` called
`stripTrailingZeros()` (baseline lines 524–525). `stripTo` stops decrementing at
`Int.MIN_VALUE` and throws when the remaining coefficient is divisible by ten
(baseline lines 473–479). Thus the first input failed immediately; stripping the second
reaches `100@Int.MIN_VALUE` and also fails. Their roots remain representable.

The baseline compact retained oracle included both operands and contexts (lines
44–45, 59–63). Its `OracleCursor.verify` supplied no independent sqrt expectation
and required the frozen result (baseline lines 12–13, 32). The first witness has eleven
conflicting rows: eight unlimited modes at zero-based indices `109920 + 3*j`
for `j=0..7`, then `UNNECESSARY` at precisions 1/2/7 at `109965`, `109989`,
`110013`. The second witness supplies eleven more. The certificate records all
22 actual frozen strings, each `!ArithmeticException`, separately from the
independently derived coefficient/scale. These are retained observations, not a
new JDK run.

## Implemented numerical argument

The [public Java SE 25 API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#sqrt(java.math.MathContext))
and class-level exact arithmetic, precision and cohort rules supply the contract.
For positive stored coefficient `c` of `d` digits, factor only decimal trailing
zeros: `c = a * 10^z`, with `a` not divisible by ten and `0 <= z <= d-1`. Retain
`u = s-z` as `Long`; do not construct a normalized decimal at scale `u`.

At least one of the exponents of 2 and 5 in `a` is zero. If `u` is odd, a rational
square would require both of those exponents to be odd and positive, a
contradiction. If `u` is even, the power of ten is a rational square and a finite
decimal root exists exactly when `a` is an integer square. The existing owned
integer sqrt/remainder kernel can detect this with arithmetic bounded by the
input coefficient. For an exact square, write `a = r^2` and `t = u/2`; `r` has no
decimal trailing zeros. Its `k`-digit coefficient is the minimal exact cohort.
No requested-precision powers enter this detection. `finiteExactSquareRoot`
implements this factor/parity/integer-square check without changing integer
kernels or calling the receiver's virtual normalization methods.

Since `d <= Int.MAX_VALUE`, `u >= -4294967294`, so `t >= -2147483647`;
also `t <= 1073741823`. Hence the minimal exact root scale itself fits `Int`,
even when stripping the radicand would cross the lower scale boundary.
For valid even `u`, `t <= preferred = s/2`; their distance is at most `(z+1)/2`.

At precision zero, pad `r` by exactly `preferred-t` trailing zeros. At positive
precision `p >= k`, pad by `min(preferred-t, p-k)`. This chooses the closest
admissible cohort; padding is bounded by the input trailing zeros, even when
`p == Int.MAX_VALUE`. `4@0` therefore yields `2@0` without a precision-sized power.

At `0 < p < k`, `UNNECESSARY` must throw: the minimal coefficient has no trailing
zeros. For the other modes, divide `r` by `10^(k-p)`, choose the retained integer
using the exact remainder, and decrease scale by `k-p`. Positive roots use UP /
CEILING to increment, DOWN / FLOOR to retain, half modes compare twice the
remainder with the divisor and use retained parity for HALF_EVEN. A carry to
`10^p` is divided by ten and decrements scale once more. The power and operands
are bounded by the input root digits. The root adjusted exponent is
`R = floor((d-1-s)/2) <= Int.MAX_VALUE`; at positive precision the rounded scale
is `p-1-R`, or one less on carry, so even a leading carry stays at or above
`Int.MIN_VALUE`. The upper scale cannot increase through this rounding. Preserve
genuine range/kernel errors; no arbitrary precision cap is proposed.

Odd `u` or a nonsquare `a` continues to the existing inexact path at finite
rounding precision and throws for unlimited or `UNNECESSARY`. The wider inexact
sqrt proof (task542), pow workprecision overflow, division, BigInteger kernels,
binary conversion, serialization and build/CI are outside this work.

Public sign/null precedence, explicit overrides and caller-owned `super` behavior
must be preserved. An internal trace on a replacement/poisoned normalization
object cannot justify rejecting an ordinary exact root. No callback test policy
has been changed here. The implementation keeps the existing exact-context
normalization branch when the stored normalized scale fits Int: its powers and
padding were already input-bounded. An exact root with normalization below MIN
uses `finiteSquareRootResult` instead. Finite rounding contexts first retain the
existing `adjustedExponent()` precision callback. Truthful precision selects the
bounded exact result; inconsistent precision retains the previous
metadata-sensitive fallback. Thus the full numerical guarantee is for ordinary
and truthful operands, while poisoned metadata observations remain diagnostic.
Negative/zero/null public checks retain their original order.

The independently expected model `DecimalExactRoot563Expected` uses decimal
digit-pair extraction (school square-root arithmetic) and string digit retention,
midpoint comparison and carry. It calls no sqrt, round, setScale or
stripTrailingZeros API. Retained oracle correction requires all of: exact context,
normalization below MIN, a finite exact root, and sufficient precision. It
consumes and explicitly asserts the frozen `!ArithmeticException` at the same
index before checking the implementation coefficient, scale and canonical text.
All other sqrt outcomes keep their previous assertion route; other operations
and all 207,239 fixture positions are preserved.

New unrun common tests comprise four methods: 18 literal radicands across 56
contexts each (including precision MAX-1/MAX), ordinary/truthful/caller-super
controls; 396 known-square/cohort radicands across 48 contexts (**19,008** family
checks); independent 15/25 midpoint and 999 carry literals; and genuine errors,
sign/null/zero precedence, explicit override and direct exception identity.
The separate JVM method retains **22** bounded public-JDK normalization defect
observations independently from the mathematical coefficient/scale checks. These
are planned assertions, not reported execution results.

## Frozen evidence and actual verification

`diagnostics/decimal-exact-root-563/baseline-sha256.txt` freezes **205 tracked
files**, including **24 retained Jdk25 fixture files**, before any tracked edits.
Its SHA-256 is
`c5e6229dfa729376417d1edaa450e05c9a2d2e8ec9994d08b1bbf069fc8a7e15`.
`retained-conflicts.tsv` has 22 independently expected conflicts and SHA-256
`10c22062766a7639aa643eaffaa97a6044a6f3c826c89a02ae81bf080c07e4b4`.

The bounded symbolic/static `.main.kts` certificate passed:

```sh
env JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca \
JAVA_OPTS='-Xmx256m -Dkotlin.main.kts.compiled.scripts.cache.dir=/private/tmp/krogu-exact-root-563-symbolic-cache-v1' \
/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin \
scripts/certify-decimal-exact-root-563.main.kts
```

It checks small coefficient-square identities and symbolic scale equalities,
hashes files with a bounded buffer and decodes retained source strings. It reads
all 207,239 frozen outcomes and identifies 22 conflicts. It calls no numerical
root implementation. The initial certificate run failed because it assumed
one fixture outcome per line; the fixture uses escaped-newline chunks. The
parser was corrected and the complete certificate passed on rerun.

An independent read-only agent confirmed both witnesses, all 22 exact indices,
source path, unavoidable oracle conflict, normalized-factor criterion, rounding
and range proof, and absence of numerical campaign calls in the certificate.
Its manifest-hardening recommendation was applied: compare every tracked path
against `HEAD`, including staged changes, before accepting the initial baseline
freeze. After implementation the audit checks all 203 unmodified tracked files
against that immutable manifest, allows exactly the two granted tracked paths,
and verifies that the raw production prefix/suffix around sqrt/helpers is unchanged. It writes
`handoff-sha256.txt` containing exact hashes of changed/new source and evidence.
The independent implementation/model/test review found no correctness issue and
no additional frozen callback conflict. It verified the production range/cohort
argument, 19,008 family count, all literals and separate diagnostic scope.
No third-party numerical implementation was read. No Gradle, javac, production
execution, black-box numerical reference campaign, or platform tests were run.
The task remains linked and `in_progress`; changes are not committed.

Task562 now owns the sole local execution window. No Gradle, javac, Native or
numerical reference campaign has run for this change. Implementation and proof
are ready for a later explicit parent comment and exact session queue grant.
Proposed commands, **unrun**, from this isolated worktree:

```sh
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca ./gradlew jvmTest \
  --tests 'io.heapy.krogu.math.DecimalExactRoot563*' \
  --tests 'io.heapy.krogu.math.PortableOracleTest*' \
  --tests 'io.heapy.krogu.math.ScaleBoundaryOracleTest*' \
  --tests 'io.heapy.krogu.math.DecimalNormalizedRootTest*' \
  --tests 'io.heapy.krogu.math.DecimalConformanceTest*' \
  --tests 'io.heapy.krogu.math.NullPrecedenceOracleTest*' \
  --tests 'io.heapy.krogu.math.KotlinSubclassContractTest*' \
  --tests 'io.heapy.krogu.math.IntegerSqrtOracleTest*'

JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca ./gradlew \
  jsNodeTest wasmJsNodeTest macosArm64Test \
  --tests 'io.heapy.krogu.math.DecimalExactRoot563Test*' \
  --tests 'io.heapy.krogu.math.PortableOracleTest*' \
  --tests 'io.heapy.krogu.math.ScaleBoundaryOracleTest*' \
  --tests 'io.heapy.krogu.math.NullPrecedenceOracleTest*' \
  --tests 'io.heapy.krogu.math.IntegerSqrtOracleTest*'
```

PortableOracleTest retains ordinary/context/midpoint/subclass observations. The
full supported-target CI remains the parent's integration gate. Task542's wider
sqrt proof and parent526 remain open. The worker stays linked/in_progress and
does not commit, review, complete, unlink or publish this code/proof handoff.

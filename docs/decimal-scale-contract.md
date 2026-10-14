# Decimal arithmetic at the scale endpoints

Task `local:554` follows the public Java SE 25 numerical contract rather than two
confirmed defects in BellSoft Liberica **25.0.4+9-LTS**. This is independent common
Kotlin arithmetic. No OpenJDK, Kotlin stdlib or third-party numerical implementation
source was consulted. The existing JDK oracle fixture data remains diagnostic
evidence and is unchanged.

## Specification and bounded witnesses

The [BigDecimal public specification](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
defines values as coefficient times ten to the negative scale. Scaled division
returns the quotient rounded to the requested scale. Context arithmetic uses the
logical exact intermediate followed by precision rounding. A result scale outside
the signed 32-bit range can cause an exception. The
[RoundingMode specification](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/RoundingMode.html)
defines the rounding directions and rejects required rounding with `UNNECESSARY`.

Here `c@s` denotes an unscaled coefficient `c` and scale `s`.

* **Division:** `10@0 / 1@MIN`, target scale zero. The exact quotient is `1@MAX`,
  a representable decimal: multiplying it by the divisor gives `1@-1 = 10`.
  It lies strictly between zero and 0.5. The pinned JDK returns `10@0` for every
  mode, including `UNNECESSARY`, and returns `-10@0` for a negative dividend.
  The contract requires `0@0` for DOWN and all half modes; UP returns the signed
  unit; CEILING/FLOOR return the unit only in their respective sign direction;
  UNNECESSARY throws. Every operand, exact witness and rounded result has a
  supported coefficient and scale.
* **Addition:** `10000@(MAX-1) + 1@MAX` aligns to the six-digit integer
  coefficient `100001@MAX`. With precision one, the retained digit is `1` and
  the discarded tail `00001` is nonzero but below half. The result is therefore
  `1@(MAX-5)`, or `2@(MAX-5)` for UP/CEILING. UNNECESSARY throws because rounding
  is required. Negative operands reverse directed rounding. The pinned JDK
  throws `Underflow` for every context mode, while its exact-add-then-round
  calculation agrees with these bounded witnesses.

## Arithmetic argument and affected domains

For scaled division let `d = targetScale + divisorScale - dividendScale`, computed
as a `Long`. The required unscaled result is the rounded rational
`(dividendCoefficient / divisorCoefficient) * 10^d`. Its sign is the product of
the operand signs. A zero numerator has coefficient zero at any requested scale
when the divisor is nonzero; zero divisors still throw.

The decimal coefficient digit lengths and their normalized lexical comparison
give the exact floor of the quotient's base-ten exponent without constructing
`10^abs(d)`. If the unscaled quotient's exponent is less than -1, its magnitude
is strictly less than 0.1. Hence rounding only needs the sign, rounding direction,
and the fact that the remainder is nonzero and below half. This check must precede
the power range guard. At larger magnitudes, ordinary integer division and
remainder-based rounding handle ties and carries. Distances stay wide through
power selection; only a supported, checked power is narrowed. Temporary scale
sums need not fit `Int` when the actual net distance and result do.

Context addition's alignment threshold is also a scale distance and must be
computed as `dominantScale.toLong() + 2`. The previous signed wrap at `MAX-1`
could request an out-of-range guard scale before doing a one-place exact
alignment. The correction leaves the exact-add and remote-sticky arithmetic
paths intact. The affected oracle classification requires both nonzero inputs,
scales exactly `MAX-1` and `MAX`, finite precision, and the previous alignment
condition `remoteScale - remoteDigits + 1 > dominantScale - dominantDigits + p + 2`.
Only that proved domain substitutes an independent numerical expectation for
the retained JDK exception. Expected addition aligns integer coefficients by
one factor of ten and rounds decimal digit text; it does not call decimal add,
subtract or round. The selected oracle operands have at most 154 decimal digits;
one-place alignment and addition need at most 156 digits, so their exact
intermediates remain far inside the supported coefficient range. Ordinary and
contract-preserving subclass behavior remains
covered. Fake precision overrides are not introduced or reclassified here.

Scaled division oracle checks use independently calculated expectations only for
zero dividends, quotients proved tiny by a strict digit-length bound, and bounded
rescaling distances. The calculation uses signed integer quotient/remainder and
the public rounding rules. Unsupported huge coefficients still fail with range
errors; this work does not establish their practical allocation limits.

The first focused JVM run found one additional consequence of the same wrapped
division distance: `1@MIN / 1@0`, target zero, HALF_EVEN returns `1@0` on the pinned
JDK. Its required coefficient is `10^2147483648`. This is a **supported-range
rejection**, not a third bounded witness for guaranteed success. The
[BigInteger range contract and reference range note](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html)
allow rejection beyond `2^Int.MAX_VALUE`. More generally, if the nonzero
unscaled rational has lower exponent `k = dividendDigits - divisorDigits + d - 1`
and `3*k > Int.MAX_VALUE`, even its truncation needs a coefficient at least
`10^k > 2^(3*k)`. Every rounding mode therefore requires an unsupported
coefficient (or rejects inexact UNNECESSARY). The scaled-division matrix keeps
executing these JDK diagnostics and requires a range exception only in this
proved domain. It keeps comparing original diagnostics elsewhere. No huge
power is constructed. No valid bounded coefficient is excluded by this rule.

## Reproducer and verification

The first full CI of the integrated scale correction exposed two additional
oracle consumers still requiring the same false range rejection:
`PortableOracleTest` and `JdkOracleTest.invalidInputsAndScaleLimits`. The pinned
JDK rejects zero at scales `-MAX` (`MIN+1`) and `MAX` divided by `ONE`, requested scale zero,
and rejects `0@MIN / 1@MIN` at requested scale `MAX`. Zero has coefficient zero at
every requested Int scale; no large power is needed. The witness `0@MAX`
multiplied by `1@MIN` reconstructs the zero dividend's value at scale -1.
It also rejects `+/-1@MAX / ONE` at scale zero, although the represented exact
quotient has magnitude below 0.1. The directional/half-mode argument above gives
0 or a signed unit; UNNECESSARY correctly requires an exception, whose message
is not a separate numerical guarantee.

Those consumers now use the existing independent model only for proved ordinary
scaled-division inputs: zero/tiny/bounded rescaling after failed staging, or the
conservative unsupported-coefficient range bound. Zero divisors and other
operations stay outside this substitution. Frozen JDK fixture values are not
edited, and assertion labels retain them. The live JVM test also retains the
specific pinned JDK failures. A classified input need not have a bad JDK result;
ordinary already-correct members of the bounded domain remain checked too.

The initial combined parent JVM/JS/WasmJS/macOS ARM64 check exited 1 in 9m25s:
the same two JVM assertions and the portable assertion on each other target
failed. New canonical-Double and ordinary/truthful context controls passed.
The consumers' classification and explicit zero/tiny witnesses require a fresh
combined rerun; those initial failures are not passing evidence. The next combined
run exited 1 in 8m43s: the live JVM consumer passed, but the portable parsed
`D/divideScale` zero case still required the same JDK exception. The identical
ordinary-operand classification now covers both stored `G/scaledDivide` and
parsed `D/divideScale` inputs. A focused JVM rerun of both consumers then exited
zero in 1m02s. The final integrated four-target run exited zero in **8m20s**:
JVM **89** methods and JS/WasmJS/macOS ARM64 **73** methods each, with no
failures, errors or skips. It includes the eight scale-contract methods, both
corrected consumers, new canonical-Double and ordinary/truthful context controls,
and retained scale/operator/context suites. The terminal log is
`/private/tmp/krogu-root-context-double-final.log`; an independent XML summary
confirmed all four target totals. Fresh full supported-platform CI is still required.

`scripts/audit-decimal-scale-contract.main.kts` prints the runtime, independent
expected results, JDK diagnostics and port results for all eight modes and both
signs. It proves the division reconstruction and addition alignment with bounded
public operations, then checks JDK exact-add-then-round against the independent
digit argument. Pass `--check-port` to require the corrected port behavior.
Use Kotlin 2.4.21 `kotlinr`; the first invocation compiles the script. For direct
execution, `chmod +x scripts/audit-decimal-scale-contract.main.kts` is optional.

```sh
KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/krogu-scale-script-cache \
  kotlinr -classpath build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar \
  scripts/audit-decimal-scale-contract.main.kts --check-port
```

Baseline evidence, collected **before editing production arithmetic**:

```sh
KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/krogu-scale-baseline-cache \
  kotlinr -classpath /private/tmp/krogu-math-task-525/build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar \
  scripts/audit-decimal-scale-contract.main.kts
KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/krogu-scale-baseline-cache \
  kotlinr -classpath /private/tmp/krogu-math-task-525/build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar:/Users/yoda/.sdkman/candidates/kotlin/current/lib/kotlin-test.jar \
  /private/tmp/krogu-task554-baseline-regressions.main.kts
```

The diagnostic script exited zero and reproduced both JDK/port defects while
verifying the bounded mathematical witnesses. The temporary regression runner
was generated from the initial six common regression methods, removing only
the package/Test annotations and adding calls that report each failure. Against
the existing unchanged arithmetic snapshot, it exited **3**: all six methods
failed (four false scale/range exceptions, incorrect bounded context addition,
and the truthful-subclass tiny division returning 10). Logs:
`/private/tmp/krogu-task554-baseline-diagnostic.txt` and
`/private/tmp/krogu-task554-baseline-regressions.txt`. Replaying the same runner
with the corrected task-554 JVM jar exited zero, with six PASS lines. The final
common suite additionally includes a seventh explicit bounded-witness method.

Terminal JVM command:

```sh
./gradlew jvmTest \
  --tests '*DecimalScaleContract*' \
  --tests '*ScaledDivisionBoundaryTest*' \
  --tests '*ScaledDivisionZeroTest*' \
  --tests '*DecimalOperator*' \
  --tests '*ScaleBoundary*' \
  --tests '*DecimalAlignmentErrorTest*' \
  --tests '*DecimalDivisionPrecisionTest*' \
  --tests '*DecimalConformanceTest.structuredRoundingAndPreferredScales' \
  --tests '*DecimalSubtractionSubclassTest*' \
  --tests '*DecimalContextDispatchOracleTest*' --no-daemon
```

The initial invocation exited 1: 41 methods, only the scaled-division matrix
failed, exposing the separately classified unsupported-coefficient diagnostic
above. The rerun exited **0**, `BUILD SUCCESSFUL in 1m 1s`: **41 methods,
zero failures/errors/skips**. This includes all seven new regressions, the
existing operator regressions, all 207,239 frozen scale outcomes and the live
139,239 compact / 68,000 wide scale comparisons. Logs:
`/private/tmp/krogu-task554-jvm.log` and
`/private/tmp/krogu-task554-jvm-fixed.log`.

The final proof command (the script command above with
`JAVA_TOOL_OPTIONS=-XX:-OmitStackTraceInFastThrow`) exited **0** on
BellSoft 25.0.4+9-LTS / Kotlin 2.4.21, requiring all 32 bounded sign/mode
outcomes and the separate range diagnostic. It retains JDK `+/-10@0` division
results, `Underflow` addition exceptions, and the range case's `1@0` result in
`/private/tmp/krogu-task554-final-diagnostic.txt`.

The initial three-target Gradle invocation exited 1 **before test execution**:
Wasm tooling could not write `~/.kotlin/kotlin-npm-tooling/.../lock` in the
filesystem sandbox. For the retry, existing NPM tooling was copied into
`/private/tmp/krogu-task554-kotlin-home`, and the build uses
`-Pkotlin.user.home=/private/tmp/krogu-task554-kotlin-home`. No repository build,
wrapper, platform or host configuration was changed.

Terminal common-target command (filters are repeated for every task):

```sh
./gradlew -Pkotlin.user.home=/private/tmp/krogu-task554-kotlin-home \
  jsNodeTest \
    --tests '*DecimalScaleContract*' --tests '*DecimalOperatorContractTest*' \
    --tests '*ScaleBoundaryOracleTest*' --tests '*DecimalContextDispatchOracleTest*' \
  wasmJsNodeTest \
    --tests '*DecimalScaleContract*' --tests '*DecimalOperatorContractTest*' \
    --tests '*ScaleBoundaryOracleTest*' --tests '*DecimalContextDispatchOracleTest*' \
  macosArm64Test \
    --tests '*DecimalScaleContract*' --tests '*DecimalOperatorContractTest*' \
    --tests '*ScaleBoundaryOracleTest*' --tests '*DecimalContextDispatchOracleTest*' \
  --no-daemon
```

This retry exited **0**, `BUILD SUCCESSFUL in 6m 51s`. Each target's XML has
**25 methods, zero failures/errors/skips**: seven DecimalScaleContract,
six DecimalOperatorContract, eleven ScaleBoundaryOracle and one
DecimalContextDispatchOracle. Every target consumes all 207,239 frozen scale
outcomes, including the independently checked affected addition cases. Logs:
`/private/tmp/krogu-task554-common-targets.log` (setup failure) and
`/private/tmp/krogu-task554-common-targets-fixed.log` (terminal success).

`git diff --check` passes. The eight-file change consists of BigDecimal's two
scale paths, the common regression/expectation file, three affected scale/oracle
assertion files, the diagnostic script, this report and the COMPATIBILITY link.
No fixtures, existing DecimalOperator regressions, BigInteger, extensions,
inc/dec, serialization, public type design or build/platform files changed.
These local results were collected in the isolated task-554 worktree before
parent integration.

Fresh supported-platform CI remains the parent integration gate. Full normative
oracle classification (`local:526`) and the broader arithmetic audit (`local:541`)
remain separate work; this finite proof does not certify the full API or range.

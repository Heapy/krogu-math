# Canonical Double factory selection

Parent integration passed the combined JVM/JS/WasmJS/macOS ARM64 run in 8m20s,
including all 16,578 canonical coefficient/scale/text rows on each target and the
retained JVM campaign. The integrated run had 89 JVM methods and 73 per other
target, with zero failures/errors/skips. The public pinned-JDK generator's
`--check` also reproduced all 16,578 rows. Log:
`/private/tmp/krogu-root-context-double-final.log`. Full supported-platform CI
remains the subsequent integration gate.

`BigDecimal.valueOf(Double)` parses the canonical decimal text specified by
[BigDecimal.valueOf](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#valueOf(double))
and [Double.toString](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Double.html#toString(double)).
The exact binary `BigDecimal(Double)` constructor has a different contract and
is unchanged. This work uses public specifications, black-box API calls and an
independent common Kotlin algorithm; no foreign numerical implementation source.

## Selection argument

Let `m` be the exact positive magnitude of a finite nonzero binary64 value, and
`R` its round-to-nearest, ties-to-even real interval. The binary midpoints define
the interval, including or excluding endpoints according to binary significand
parity. It contains `m`; symmetry is unnecessary. At a normal power of two the
lower gap is smaller than the upper gap (except at the normal/subnormal join).
Testing only the nearest decimal grid point can therefore miss an admissible
point on the other side. For bits `0x0060000000000000`, the old factory returned
`71202363472230444@323`, while the required shorter result is
`7120236347223045@322`; both round-trip to the same binary value. Signs reflect
the interval and do not change lengths, distances or parity.

The implementation constructs `m` and its adjacent positive binary values as
exact decimals. Raw positive finite bit patterns have the same order as their
values. The lower bound is `(m + previous)/2`; the upper is `(m + next)/2`.
For the largest finite value, the virtual next value is one ulp higher, so the
upper bound is `m + (m - previous)/2`, the overflow midpoint. The binary
significand is even exactly when the raw low bit is zero, determining whether
both endpoints belong to `R`. Exact decimal comparisons test membership directly,
without repeatedly converting each candidate back to binary.
To avoid repeated wide-coefficient comparisons and decimal division, use the
common integer scale `S = 1 + max(scale(m), scale(previous), scale(next), 16-E)`
(omit `next` for the maximum finite value). All aligned binary-value coefficients
are multiples of ten, hence even. The midpoint coefficients are exactly
`(M+P)>>1` and `(M+N)>>1`; at the finite/infinity edge the latter is
`M + ((M-P)>>1)`. These shifts are exact halves, with no rounding or GCD operation.
Every searched grid scale is at most `S`, including the budget-two adjacent bands.
A grid unit becomes the integer `10^(S-gridScale)`. Build the finest unit once
as `10^(S+E-16)` and obtain coarser units by multiplying by ten. This exponent is
nonnegative by the definition of `S`.

The grid floor also needs no large division: let the exact decimal coefficient
have `L` digits and scale `q`. In band `E+offset`, dividing `m` by the grid unit
in real units gives `C * 10^(n-offset-L)`. Its positive integer floor is the
first `n-offset` digits of `C`, or those digits padded with zeros when `C` has
fewer digits. This is exactly the quotient of the aligned coefficient by the
integer grid unit. Its successor gives the other bracketing coefficient.
Multiplying a candidate coefficient by the unit yields an integer in the same
scale as the bounds. Membership and absolute-distance comparisons are therefore
the same real comparisons, multiplied by the shared positive factor `10^S`.
Only admitted candidates need a stripped decimal representation.
The zero and infinity endpoint conventions also match the public
[Double.valueOf(String) contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Double.html#valueOf(java.lang.String)).

Write `E = floor(log10(m))`, calculated from the exact decimal precision and
scale, without floating-point logarithms. At budget `n`, the grid in decimal
exponent band `e` has spacing `h = 10^(e-n+1)`, with integer coefficients in
`[10^(n-1), 10^n-1]`. Trailing zeros are permitted in this grid encoding and are
stripped before measuring canonical length or tie parity.

Compute `floor(m/h)` and its successor, clipping both coefficients to the band.
These are the closest grid values to `m` on each available side (or the closest
band endpoint when `m` is outside the band). If another grid value on a side
belongs to `R`, the closest one does too: it lies between that value and `m`,
and `R` is an interval containing `m`. This remains true with open binary
midpoints; the nearer point either equals the admitted point or lies strictly
inside. Thus these two candidates suffice to detect membership and minimize
distance within a band. Exact decimal distance comparison selects the closest
admissible candidate across bands; equal distances select an even **stripped**
significand, rather than parity of a zero-padded coefficient.

For every finite positive binary64, `R` lies between `m/2` and `2m`, with a
strict upper inequality. The broadest relative interval occurs at the minimum
subnormal; normal intervals are much narrower. Hence admissible decimals have
adjusted exponents only `E-1`, `E`, or `E+1`. At budget two, search all three
bands. This also represents every length-one decimal by padding its coefficient
with a zero, and implements the public rule: when minimal length is one, select
the closest decimal from the union of lengths one and two. Starting with a
length-one-only pass, or measuring printed digits including the mandatory `.0`,
would give the wrong rule. Small subnormal examples include `4.9E-324` and
`9.9E-324`.

If budget two fails, `R` contains no decimal power of ten, because those are
length-one decimals. Since `R` is connected and contains `m`, it cannot cross a
decimal exponent boundary. Budgets three through seventeen therefore need only
band `E`. At the first successful budget, every admitted candidate has minimal
canonical length: a shorter candidate would have been detected at an earlier
budget. A decimal carry across a power of ten is covered explicitly by the
adjacent bands at budget two; later carries cannot be admissible after that
failure. Clipping also considers the last grid point immediately below a carry,
even when ordinary rounding on that finer grid falls outside its exponent band.

Seventeen digits suffice. Nearest rounding on the band-E grid has absolute
error at most `0.5 * 10^(E-16)`, hence relative error at most `5 * 10^-17`.
If it carries above the band, the carried decimal power of ten is covered at
budget two. The smaller half-gap from any finite binary64 `m` to its binary
rounding boundaries is at least `m * 2^-54`, which exceeds that decimal error.
Subnormals have at least this relative margin too, and the maximum finite
value's overflow boundary has the same half-ulp margin. Thus an admissible
candidate is found by seventeen, assuming the existing exact binary constructor
and decimal arithmetic satisfy their own contracts.

## Formatting and limits

After selection, stripping zeros gives canonical significand and exponent.
Adjusted decimal exponents -3 through 6 use plain notation; others use
scientific notation. An integral plain value and a one-digit scientific
significand retain `.0`, so coefficient **and scale** of the factory result
depend on the canonical text. Both signed zeros yield `0@1`; nonfinite values
throw `NumberFormatException` as required by the factory.

The selection argument applies to the candidate search, conditional on existing
exact constructor and arithmetic correctness. It is not a proof of every
library operation or every Double-domain execution. The finite boundary corpus
and existing seeded campaigns supply distinct evidence. Full-domain audit
remains kotgent `local:543`; supported-platform CI remains the parent integration
gate.

## Reproducible evidence

The public-only generator uses BellSoft Liberica 25.0.4+9-LTS and freezes 16,578
signed inputs with exact coefficient, scale and canonical text. It includes all
2,046 normal binary powers, all 52 subnormal powers, adjacent raw bits, small
signed subnormals, every finite decimal power from 10^-323 through 10^308 and
its neighbors, maximum finite neighbors, and signed zeros. Common tests replay
those outcomes; a JVM test rechecks each frozen result with live public APIs.
The portable corpus is spread across 32 residue classes of the row index, with
an exact expected count per class. Every row and coefficient/scale/text assertion
is retained, while each class runs as a bounded test method. Separate tests assert
the original asymmetric counterexample, zero/nonfinite
handling, the length-one/two rule and the unchanged exact constructor.

Run from the repository root:

```shell
kotlinr scripts/generate-canonical-double-oracle.main.kts --check
./gradlew jvmTest --tests '*CanonicalDouble*' --tests '*doubleCanonicalizationCampaign*' --tests '*PortableOracleTest*' --tests '*JdkOracleTest*' --no-daemon
./gradlew jsNodeTest --tests '*CanonicalDouble*' --tests '*PortableOracleTest*' wasmJsNodeTest --tests '*CanonicalDouble*' --tests '*PortableOracleTest*' macosArm64Test --tests '*CanonicalDouble*' --tests '*PortableOracleTest*' -Pkotlin.user.home=/private/tmp/krogu-task555-kotlin-home --no-daemon
```

The local npm tooling directory above is a copy of existing cached tooling into
`/private/tmp`, so verification does not write to restricted host tooling paths.
No build settings, platform configuration, wrapper or host services were changed.

The unchanged formatter baseline command was:

```shell
./gradlew jvmTest --tests '*CanonicalDoubleTest.asymmetricPowerBoundaryHasTheShortestPermittedCoefficient*' --no-daemon
```

It terminated with exit 1, `BUILD FAILED in 1m 18s`, one method and one expected
assertion failure: coefficient `7120236347223045` versus `71202363472230444`.
The independent parent power probe on an unchanged formatter snapshot also
terminated with exit 3, 4,092 signed cases and 92 mismatches: 46 positive
magnitudes and their negative counterparts. Its reproducer is
`/private/tmp/krogu-double-factory-boundary-probe.main.kts`; baseline log is
`/private/tmp/krogu-555-baseline-probe.log`.

The initial fixed focused JVM command (before the midpoint membership optimization
and corpus partitioning) terminated with exit 0,
`BUILD SUCCESSFUL in 3m 15s`: 16 methods, zero failures/errors/skips. This includes
the four common CanonicalDouble methods, the live JVM method, all four existing
PortableOracle methods, all six JdkOracle methods, and the unchanged 10,000-input
canonicalization campaign. The public generator `--check` also terminated with
exit 0, regenerating all 16,578 frozen coefficient/scale/text rows exactly.
Logs are `/private/tmp/krogu-555-jvm-baseline.log`,
`/private/tmp/krogu-555-jvm-fixed.log`, `/private/tmp/krogu-555-jvm-summary.log`
and `/private/tmp/krogu-555-oracle-check.log`.

The first common-target command ended with exit 1, `BUILD FAILED in 28m 54s`,
when this task's confirmed Node worker was stopped after over 26 minutes of CPU
in the single corpus test method. It had not returned an assertion mismatch or
completed test result; WasmJS/macOS had not executed. Only this task's Node child
was stopped. The log is `/private/tmp/krogu-555-common-targets.log`; a one-second
native stack sample is `/private/tmp/krogu-555-node-sample.txt`. This execution
failure motivated bounded test methods and direct exact-interval membership;
no corpus rows, assertions, old tests, fixtures or timeout settings were removed.

The intermediate midpoint-only focused JVM command also completed with exit 0,
`BUILD SUCCESSFUL in 6m 57s` (`/private/tmp/krogu-555-jvm-interval-fixed.log`).

The next intermediate four-target attempt passed the JVM stage (47 methods,
zero failures/errors/skips), then failed the unchanged JS timeout in
`portableBoundaryCases06`. The confirmed owned Node worker was stopped during
case 10; this invocation ended exit 1, `BUILD FAILED in 21m 1s`. Log:
`/private/tmp/krogu-555-final-four-targets.log`. A bounded public execution of the
original two signed counterexamples on that JS build passed in one method
(4.198 seconds); temporary `.main.kts` runner and log:
`/private/tmp/krogu-555-js-focused-probe.main.kts` and
`/private/tmp/krogu-555-js-focused-probe.log`. No assertion mismatch was hidden;
the final implementation removes the expensive decimal midpoint divisions and
large grid quotients as described above. All 32 partitions, all rows and all
assertions remain enabled with the original timeout.

The final implementation passed this exact terminal command:

```shell
./gradlew jvmTest --tests '*CanonicalDouble*' --tests '*doubleCanonicalizationCampaign*' --tests '*PortableOracleTest*' --tests '*JdkOracleTest*' jsNodeTest --tests '*CanonicalDouble*' --tests '*PortableOracleTest*' wasmJsNodeTest --tests '*CanonicalDouble*' --tests '*PortableOracleTest*' macosArm64Test --tests '*CanonicalDouble*' --tests '*PortableOracleTest*' -Pkotlin.user.home=/private/tmp/krogu-task555-kotlin-home --no-daemon
```

It ended with exit 0, `BUILD SUCCESSFUL in 5m 43s`. XML inspection by
`kotlinr /private/tmp/krogu-555-test-summary.main.kts build/test-results/jvmTest build/test-results/jsNodeTest build/test-results/wasmJsNodeTest build/test-results/macosArm64Test`
also ended with exit 0:

| Target | Methods | Failures | Errors | Skips |
|---|---:|---:|---:|---:|
| JVM | 47 | 0 | 0 | 0 |
| JS Node | 39 | 0 | 0 | 0 |
| WasmJS Node | 39 | 0 | 0 | 0 |
| macOS ARM64 | 39 | 0 | 0 | 0 |

All 16,578 portable rows and their coefficient/scale/text assertions ran on
each target, along with every selected existing fixture method. JVM additionally
ran the live oracle and unchanged 10,000-input canonicalization campaign.
Terminal log and XML summary are `/private/tmp/krogu-555-terminal-four-targets.log`
and `/private/tmp/krogu-555-terminal-summary.log`.

The final formatter also passes the independent parent power probe, exit 0,
4,092 signed comparisons and zero mismatches. Exact commands:

```shell
kotlinc src/commonMain/kotlin/io/heapy/krogu/math/DoubleFormatting.kt -classpath build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar -d /private/tmp/krogu-555-final-formatter.jar
KOTLIN_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR=/private/tmp/krogu-555-script-cache kotlinr -classpath /private/tmp/krogu-555-final-formatter.jar:build/libs/krogu-math-jvm-0.1.0-SNAPSHOT.jar /private/tmp/krogu-double-factory-boundary-probe.main.kts
```

Log: `/private/tmp/krogu-555-final-power-probe.log`. A copy of the final Gradle
JVM artifact is `/private/tmp/krogu-task555-canonical-final.jar` for parent review.
The Wasm test generated an untracked `kotlin-js-store/wasm/yarn.lock`; that owned
build artifact was removed after verification, leaving the seven-file scope.
Old fixtures, test expectations, timeouts and build settings remain unchanged.
No benchmark claim or full-domain proof follows from these finite checks.
Final formatter SHA-256 is
`9b2a1b8cb1d40a2fdc7db5110b6a8c4e398da0562d5c3d6b02cf928f3c74433f`;
common test SHA-256 is
`329330a3280f4a329c0f6b22ade163b36a36503b4adaad9ce01db3585aa88d10`.
Parent preparation review used formatter SHA beginning `432e3eff`; the parent
still needs the final delta review and supported-platform CI before integration.

## Diff scope

Seven files belong to this uncommitted task:

- `src/commonMain/kotlin/io/heapy/krogu/math/DoubleFormatting.kt`: the sole production change.
- `src/commonTest/kotlin/io/heapy/krogu/math/CanonicalDoubleTest.kt`: portable regressions.
- `src/commonTest/kotlin/io/heapy/krogu/math/CanonicalDoubleOracleData.kt`: new frozen public-oracle data.
- `src/jvmTest/kotlin/io/heapy/krogu/math/CanonicalDoubleConformanceTest.kt`: live public-oracle comparisons.
- `scripts/generate-canonical-double-oracle.main.kts`: reproducible generator/checker.
- `docs/canonical-double-contract.md`: argument and evidence.
- `COMPATIBILITY.md`: a three-line link to this report.

Existing fixture/test files and other arithmetic/extension files are unchanged.
Changes are not committed.

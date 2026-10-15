# Negative power at the asymmetric scale endpoint: source/spec audit (542)

Historical source-only milestone copied into the repository after review.
“Publication root” below identifies the then-current pinned snapshot, not the
later580 integration. The original private memo remains byte-preserved at
`/private/tmp/krogu-542-negative-power-scale-audit.md`, SHA-256
`83f90ce9d81c61f5360e810745a86bb98c9799acdde906e18da401dd78d28ee8`.

Later actual diagnostic evidence is appended at the end. Source-only statements
in the preserved milestone below describe its original preparation.

The tiny ordinary input `[1,1073741824]` with exponent-2 and MathContext(1,DOWN)
has the exact mathematical result **[1,Int.MIN_VALUE]**, yet current source
throws before reciprocal formation. This is a confirmed source-derived
supported-final-value limitation. Its specification classification is qualified:
pow documents positive-first accumulation and BigDecimal permits scale-overflow
errors. It is not an observed JDK failure or an unconditional JDK/API defect
claim. No input operation, port or JDK probe was executed, despite the tiny
coefficient size.

This finite **SOURCE/spec ONLY** audit adds only this `/private/tmp` document.
No publication-root or private580 file, frozen579/580 source/test/proof, fixture,
Git state or tracker changed. No compiler, script, numerical helper, certificate,
probe, test, build, benchmark or numerical runtime command ran. Only own source,
proof/test text, metadata and official public API documentation were read.
No foreign numerical implementation source/body/bytecode was consulted. Root
holds the separately scoped runtime window; its eventual release is not an
execution grant for this witness.

## Exact snapshots

| Snapshot/file | SHA-256 |
|---|---|
| Publication root `BigDecimal.kt` | `4ab8f15b10f2aa2093e4e04e2a7e4aa6226a7230be24dead3bbe75bcde9e5cc5` |
| Publication root `BigInteger.kt` | `e4dc75b253c689b0f3c634f02f9e491c9a1dcfae396a11135be4427c69ce7b68` |
| Private580 `BigDecimal.kt` | `1c1559c44de0a51bfcd7df0c325dfb9de22e7b6a4588b6dcb612370dc850fa55` |
| Private580 `BigInteger.kt` | `b2fb23547d97f3f7650b6d7dcf4ad53939efb3abac97d9d152376e67dd6685f5` |
| Private580 `DecimalProduct580.kt` | `2ef14e2a2d52b75286f2e5830301a75fe586ed41cb3fbbacb9fd96c346d5234d` |
| Current root `decimal-power-root-542-current-audit.md` | `5eda89a207c3a2493eb807efcbfc4047bc1381a0ca97c13ea385135813cb9440` |
| `decimal-pow-working-precision.md` | `47f47850763ed4d0ba4c07af0bddaa27d94feebc454283a11028666af844b1f7` |

Publication source is `/Users/yoda/dev/pet/krogu-math`; private580 is
`/private/tmp/krogu-math-task-580-UvdPNirY/root`. Published pow is at530–549;
private580 shifts the unchanged pow body down seven lines to537–556. Its multiply
HIGH no-reduction guard moves from143 to150. Snapshot pins matter: the root542
audit now explicitly labels its earlier08b/5a4 analysis historical and links
the separate579/580 work. This document does not change those bindings.

## Mathematical result and validation

Let s=1073741824=2^30 and x=[1,s]=10^-s. Scale s is an Int, coefficient1 is
supported and no large magnitude, String or digit array is necessary to represent
x. For n=-2,

`x^(-2)=10^(2s)=10^2147483648=[1,-2147483648]`.

The final scale is precisely Int.MIN_VALUE and the coefficient has one bit and
one decimal digit. The exact unit recurrence has no discarded nonzero digit
at any working precision>=1. Thus widened arithmetic gives that same exact
result for all eight rounding modes, including UNNECESSARY; this is not an
inexactness exception or a half-boundary problem. The proposed mathematical
answer is exact, not a guessed approximation selected solely by a two-ulp bound.

p=1 is valid; |n|=2 is in1..999999999 and has one decimal digit, so exponent-digit
validation passes. The working precision is1+1+1=3, far from the repaired Int
addition boundary. This witness does not reopen that overflow defect. Nonzero
x avoids zero division. No subclass, fake precision, alternate coefficient,
null input or incidental callback observation is involved.

The **positive** exact square is different:

`x^2=10^-2147483648=[1,2147483648]`.

Its scale exceeds Int.MAX_VALUE by one. Indeed it has no nonzero exact public
decimal representation at any coefficient size: for positive integer C>=1 and
any allowed scale S<=Int.MAX_VALUE, C*10^-S>=10^-Int.MAX_VALUE, strictly larger
than x². This is an actual intermediate decimal value-domain obstruction, not
580's merely oversized chosen coefficient intermediate. A public multiply that
is required to return this exact positive square can legitimately reject it.

## Full reached own-source recurrence

At root BigDecimal:530–549:

1. p>0 avoids unlimited delegation; n=-2 passes range, nonzero and digit-count
   checks. Magnitude is2, its bit count2, and work is MathContext(3,mode).
2. Accumulator starts1@0. At bit i=1, squaring1 stays1@0. That bit is set, so
   the subsequent multiply by x produces1@s exactly; S=s fits Int and there
   is no coefficient precision reduction.
3. At i=0, squaring that accumulator has logical scale2s=2147483648, coefficient
   1*1=1. Both signs are nonzero, so the normal/zero branch is bypassed.
4. The HIGH branch constructs the neutral product1@0, sees D=1<=workPrecision3,
   then checkedScale(logicalScale) throws ArithmeticException. The current own
   text follows the HIGH scale diagnostic; no exception was observed here.
5. The bit0 multiply is irrelevant and root548's reciprocal call is never
   reached. Destination rounding at549 is also never reached.

Every mode follows the same path: the failure precedes any mode-sensitive
rounding. In private580, its stored bridge sees bit-sum1+1=2<=Int.MAX_VALUE and
returns null. The exact same retained HIGH guard then rejects. Even a forced
raw product route would retain D<=p HIGH rejection by design.580 must not be
silently altered to make a public unrepresentable positive square succeed;
this is a pow-level accumulator representation question.

## Official public algorithm and classification

The Java25 [contextual pow contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#pow(int,java.math.MathContext))
specifies expanded-precision squaring, positive-power accumulation before a
negative exponent's reciprocal, then destination rounding. Its stated validation
and inexact-UNNECESSARY cases do not exclude the input above. The
[class scale rule](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
also permits arithmetic failure when a result's scale exceeds Int range.

Consequently a strict interpretation using public decimal values after every
documented multiplication has a defensible intermediate range failure. The
documentation does not explicitly require public multiply calls or settle
whether internal working scales must narrow at every step. A wider private
scale could preserve the numerical recurrence and reach the supported exact
final value. Neither final representability alone nor the algorithm paragraph
proves an unconditional compatibility violation. Classify this as a concrete
algorithmic range limitation requiring an explicit project policy/spec review,
not a confirmed reference defect or an automatic new normative test assertion.

## Minimal endpoint family and finite neighboring controls

For canonical unit inputs [1,s], positive s and negative exponent-m, the natural
positive unit cohort has scale ms and its reciprocal cohort scale-ms. To put
the first beyond Int.MAX_VALUE while keeping the second within Int, require

`Int.MAX_VALUE < ms <= -Int.MIN_VALUE`, hence **ms=2^31**.

Because the product is a power of two, positive integer divisors give
m=2^k, s=2^(31-k). The valid contextual exponent bound admits k=1..29;
m=2^29=536870912 is valid, m=2^30 is not. All input scales are Int. Choose
p>=decimalDigits(m) (one through nine); the exact unit result remains1@MIN
for every mode. This family makes its first scale failure on its last squaring:
every earlier positive accumulator scale is at most2^30, then the last doubles
it to2^31. Replacing the unit coefficient by-1 also gives a positive final
result, since every admissible m in this family is even.

The smallest exponent magnitude in this family is **m=2**, and p=1 is the
smallest valid context precision. m=1 would require the invalid input scale2^31.
The family characterizes these natural unit cohorts, not every possible decimal
cohort at larger precision; no blanket result-range classification is inferred.

Useful unexecuted controls derived from source and exact arithmetic:

| Input/exponent/context | Exact result or classification |
|---|---|
| [1,2^30-1], -2, p1 | 1@-2147483646; positive accumulator scale2147483646 fits. Source reciprocal has compact equal coefficients and returns that preferred scale. |
| [1,2^30], -1, p1 | 1@-1073741824; no overflowing square is needed. |
| [1,2^30], +2, p1 | Exact positive unit square has no nonzero public representation; current HIGH rejection reflects that intermediate result's range. |
| [1,2^30+1], -2, p1 | Natural precision1 result needs scale-2147483650, outside Int. Do not call the number globally unrepresentable:100@MIN has the same numeric value but precision3, outside this required precision1 cohort. |
| [1,2^30], -2, p0 | Required negative-exponent/unlimited validation rejection; not the witness. |
| [1,s], 0, any valid context | ONE, including zero base as separately specified. |
| Zero base, negative exponent, p>0 | Reciprocal of zero is undefined; required arithmetic rejection, not endpoint rescue. |
| Any base, abs(n)>999999999 | Exponent-range rejection before this recurrence. |

These are proposed mathematical/source controls, not executed results. Even the
small practical witness remains unprobed under the current source-only scope.

## Existing coverage and the remaining finite decision

`DecimalPowWorkingPrecisionContractTest.exactMonomialsKeepTheirScaleForEitherExponentSign`
uses scales-2..2 and endpoint scalesMIN+1/MAX only with exponents-1/+1. It does
not cover this m=2 one-step asymmetry. Other methods cover coefficient/sign
unit powers at scale0, bounded exact reciprocal examples, zeros, validation and
explicit caller contracts. Those obligations and their historical executed
evidence remain valid; no assertion or fixture is changed by this audit.

`DecimalPowerBoundaryTest`'s128 reference keys are scale0 ONE working-precision
boundaries; `DecimalConformanceTest.rootsAndPowers` has bounded scale samples.
The working-precision proof explicitly assumes supported intermediates and
the broad542 audit explicitly leaves positive-first/reciprocal range composition
conditional. No green finite suite or one-unit coefficient argument closes that
condition. Pins read for this audit:

| Evidence | SHA-256 |
|---|---|
| `DecimalPowWorkingPrecisionContractTest.kt` | `9d6a474993b07cd4a417733c0875ee123db1bfc2b8c1f499a80ad545bac54445` |
| `DecimalPowerBoundaryTest.kt` | `f4ed1092214943950ad2ed7de82b89254130ab598a987b0cc988c4b50c4150e2` |
| `DecimalConformanceTest.kt` | `b93b566deb5cd4eb9b710f8a499b278479636f8de92b8b5b61f2e09e40fc1a57` |

If the project's mathematical policy elects to return supported final values
despite internal scale excursions, keep the positive-first working-precision
recurrence in a private coefficient/Long-scale representation until reciprocal
formation and final validation, or prove a narrowly complete unit-cohort rule.
Inverting first is not a general justified substitute: rounding and reciprocal
do not commute for arbitrary nonunit inputs, so changing that order requires
its own error/algorithm argument. The present unit family alone proves no
general all-input pow repair, resource theorem or universal two-ulp bound.

The finite conclusion is a source-confirmed, practical tiny-coefficient
supported-final-value limitation with an explicit specification qualification.
Root owns the separate policy classification, any task, implementation, exact
bounded reference/port observation and tests. No automatic JDK-bug, mandatory
repair or future pass claim follows. No runtime window is held or awaited;
all owned calls are terminal.

## Later actual bounded observation

Root grant2329 compiled and ran a separately reviewed standalone main-kts
diagnostic exactly once, session15551 exit0, with a fresh script cache and
256MiB heap. Its source SHA-256 is
`9d7dffb3d3c5f6024d246e9d12dcd6eaa1b988dc3a4fd740be14d9c565c9aa2e`.
It used Liberica25.0.4+9-LTS/Kotlin2.4.21 and fresh own580 JAR
`4861457772e98ef1716c1c4094c5e5422ee9b0d26235e50d10bb082c4f660df2`,
with225-source manifest
`b2315fea5e93c1d3b0e28c88a33f6b1e78c942cc524b2e18f90023cca05bbb5a`.
Actual flags, heap, all eight class origins and source/JAR pre/post guards passed.

Exactly80 unique calls cover five cases, eight rounding modes and both
providers. All64 required controls pass: adjacent scale1073741823 at exponent-2,
exponent-1 at scale1073741824, genuine positive-square range failure and
unlimited-precision negative-exponent rejection. The16 primary rows were
diagnostic only. **Both JDK and port threw ArithmeticException: Underflow in
all eight modes** for the primary. No result was inferred from a missing row;
all80 BEGIN/ROW keys completed, control_bad is0, HARNESS_COMPLETE is present
and stderr is empty. No expanded decimal formatting or giant input was used.

Raw observations and the source/protocol are retained under
`/private/tmp/krogu-542-negative-power-cache.QfMvZj3q/` and
`/private/tmp/krogu-542-negative-power-probe-v1/` respectively.
The observations TSV SHA-256 is
`e2ac4ad9a3a32808dcaaac6d6c9d50384efed4dcbea2a8bfb8f4c72975541016`.
Independent actual read-only review accepted the bindings, complete calls,
controls and observations. All owned invocations terminated; root explicitly
released the local numerical window (empty mutex revision786).

This confirms shared observed behavior. It does not decide the intermediate
scale-range interpretation, establish a JDK defect or supply a general pow
proof. The source/spec classification remains qualified; no production or
test-assertion change follows from agreement alone. Broader542 and project
acceptance remain separate.

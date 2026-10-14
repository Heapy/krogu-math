# Exact decimal root: parity proof and callback scope evidence

Task `local:563`, live pane `2a5251a7`, isolated worktree
`/private/tmp/krogu-math-task-563`, branch `task/563-exact-root`, unchanged HEAD
`85941d18908a8155d205b04d6e1a9b3e8f10b31e`. Changes are not committed.
Parent grants **1803**, **1822** and **1832** authorize the implemented exact
scope, including all identified callback and ordinary sqrt reference routes.
Code/proof and independent source review are complete; compilation and runtime
verification remain pending. No integration readiness or test pass is claimed.
Task564 owns the sole local execution window under parent comment1839.

## Corrected supported-domain argument

The previous implementation factored decimal zeros by formatting the whole
input. Parent review **1812/1815** found a concrete supported-domain hazard:
valid input coefficients can have 646,456,993 decimal digits, beyond the
536,870,888 Node string capacity established by task562, although their exact
roots fit that engine. The earlier full-domain approval was withdrawn. There is
no giant-input execution or probe here; these established bounds are used only
in symbolic arithmetic.

Let a positive radicand be `x = c * 10^(-s)`, where `c` is an integer and `s` is
an Int. For even `s = 2k`, a finite root exists iff `c = r^2` for an integer
`r`: `sqrt(x) = r * 10^(-k)`. A rational square root of an integer must itself
be an integer, because the square of its reduced denominator divides one.

For odd `s = 2k+1`, `sqrt(x) = sqrt(c/10) * 10^(-k)`. If `sqrt(c/10) = a/b`
in lowest terms, `10*a^2 = c*b^2` forces `b^2` to divide ten. The only square
divisor of ten is one. Thus finite exactness requires and is implied by
`c = 10*r^2`. Divide `c` by ten with remainder; a nonzero remainder rejects
finite exactness, while a zero remainder leaves the integer-square criterion
on `c/10`. There is no multiplication of the whole input by ten, whole-input
string, decimal-zero factorization, or normalized input-scale construction.

The implemented `finiteExactSquareRoot` uses only the stored coefficient's
sign, division/remainder by ten for odd scale, and the existing owned integer
sqrt/remainder kernel. That kernel uses bit length, Newton division and integer
operations, without input decimal conversion; its source and all integer kernels
remain unchanged. Root square verification is bounded by the input magnitude.
Independent follow-up review also traced `divideAndRemainder(TEN)` through
stored-limb single-limb division and magnitude normalization: no input decimal
conversion or callback occurs. With divisor ten, each remainder is below ten,
so each 30-bit-limb partial dividend is below `10 * 2^30` and fits Long; quotient
bit length cannot exceed the input. Thus the odd-scale branch does not hide a
whole-input formatting operation inside division.

The raw root scale is `q = s/2` for even scale and `(s.toLong()-1)/2` for odd.
Using Long subtraction prevents MIN/negative-odd overflow or truncation mistakes.
Always `-1073741824 <= q <= 1073741823`. Preferred scale is `s/2` with truncation
toward zero. Raw scale equals preferred except for negative odd `s`, where it
is one less. The raw coefficient can contain zeros; it need not be normalized.

At precision zero, no digits are rounded. At positive precision, round the raw
root coefficient exactly as an integer decimal coefficient, with only its own
digit drop. `UNNECESSARY` succeeds when all removed digits are zero, even if the
raw coefficient has more than `p` digits; otherwise it throws. Other modes use
the exact retained digits, remainder, midpoint comparison, parity and carry.
The existing owned `round` implementation supplies that coefficient rounding.
For precision-limited results, choose scale
`min(preferred, rounded.scale + p - rounded.precision)`; unlimited uses preferred.
This is the closest representable cohort within the digit budget. Inexact
rounding returns p digits, so it adds no padding. Exact discarded zeros consume
the digit budget and likewise cannot cause precision-sized padding. Without a
digit drop, padding is at most the one zero between raw and preferred scales.

Using the supported coefficient-digit bound from task562, the raw even root
has at most 323,228,497 digits. The odd root after dividing the radicand by ten
has at most 323,228,496 digits and can need one preferred-scale zero. Both final
bounds are below the established Node string capacity. Only root/results are
formatted on this path. Rounding can drop at most that input-dependent root
digit count and decrement scale once more on carry; even the conservative lower
bound `-1073741824 - 323228497 - 1` is greater than Int.MIN_VALUE. Upper scale
never exceeds preferred. Available memory still matters; no arbitrary precision
cap or giant allocation has been added.

All detected finite roots return before `stripTrailingZeros`, receiver precision,
`adjustedExponent`, or any precision-derived working power. Negative/sign/zero
and null precedence retain the existing sqrt entry implementation. Explicit caller
overrides remain open and caller-owned `super` still reaches the inherited
arithmetic. Nonfinite roots retain the prior inexact/exact-mode paths; broad
task542's other sqrt bounds and poisoned interactions are not closed here.

## Exact additional callback assertion scope

Removing internal normalization/precision calls is required for this supported
domain. Their old call counts, replacement-normalization results and injected
getter behavior are observations, not a requirement that inherited sqrt invoke
those methods. Direct public calls into overrides, truthful numerical arithmetic,
documented errors and caller-owned super remain separate mandatory controls.
Existing sign/zero/null behavior is preserved for this task; the inherited
signum callback trace is a retained implementation diagnostic, not a general
language or public-API invocation guarantee.

`callback-observations.tsv` preserves every selected row's original index, source
line and all seven frozen fields, including the entire old outcome plus trace.
Its SHA-256 is
`038bb6c9f85fbc03187230dd1e94f56ce9b12ef8668818b53dfeda7db96ee839`.
The static classifier selects **1,656** rows out of **75,638**:

| Exact selected interaction | Rows |
| --- | ---: |
| A normalized getter: even owned scale and exact context | 540 |
| N replacement normalization/getter: exact context | 560 |
| H sqrt: finite rounding precision callback | 42 |
| H sqrtSign: positive replacement 1, finite rounding precision callback | 28 |
| H sqrtPrecision: finite rounding metadata callback | 126 |
| H sqrtNormalize: exact-context stripping/replacement | 150 |
| H sqrtNormalize: finite rounding precision callback | 210 |

Exact context means p=0 or UNNECESSARY. Other finite rounding contexts have p>0
and another mode. H's stored finite exact sources are exactly `1`, `4.00`,
`0.0004`. A's stored source is `4@v[3]`; N's is `4.00`. Negative/zero/nonfinite
owners, other families, pow and all unaffected sqrt fields stay outside this
classification. The manifest is exact selected row evidence, not permission
to drop a family.

The granted consumers are `PortableOracleTest` A/N/H sqrt-only
routing, both `DecimalNormalizedRootTest` methods, and sqrt assertions in the
four `DecimalPowerSubclassTest` methods. Corresponding direct-JVM selected counts
are 540/560 and 360/28/126/42. Each selected JVM probe now checks its actual
public-JDK whole outcome and trace against the archived observation. All
unaffected old comparisons and every pow case remain mandatory.
Final independent review also identified ordinary sqrt reference routing in
`ScaleBoundaryConformanceTest`: both the compact and wide loops directly compare
the JDK sqrt result. The compact loop includes the same 22 normalization-defect
cases. Grant1832 authorizes only its two sqrt reference routes. The implemented
helper uses the existing independent model, explicitly checks the JDK exception
separately and checks port coefficient/scale; its other arithmetic reference
routes stay intact.
Example: frozen `Jdk25Vectors.kt:44931` requires `2.0|sign;precision;` on the
truthful `4.00`, p7, HALF_EVEN probe. The numeric result is mandatory; the
internal receiver precision invocation must not be used to force an input String
allocation. N/A replacement and throwing getter outcomes are likewise retained
whole observations, not automatically substituted by a universal poisoned-object
arithmetic contract.

The implemented focused policy retains every old row/outcome/trace unchanged.
`DecimalExactRoot563CallbackObservations.kt` is a new, statically copied archive
of the selected original rows; the inventory script verifies its exact contents
against the original source. Original frozen fixtures/generators are unedited.
The common contract test walks all 75,638 rows to enforce the case-predicate /
index-snapshot bijection, unique input keys and all seven category counts.

Every selected configuration receives independent literal-root/string-rounding
coefficient, scale, result precision and canonical-text controls on ordinary,
fully truthful, and caller-owned super variants (**4,968** root calls per complete
projection). PortableOracleTest still executes the original probe, verifies all
its original fields at the original index, and applies that numerical projection.
Truthful original A/H probes also retain their actual numeric-text assertions;
H retains its selected sign-callback prefix as this task's diagnostic policy,
separate from explicit caller-owned override entry. Precision-poisoned operands keep their
explicit getter value and valid operand coefficient/scale/text fields. A/N
normalization replacements and the N getter's first-call injected result/exception
and second-call delegation have direct caller-owned identity/value controls.
Unspecified inherited invocation counts/order/noncalls and metadata-induced
replacement outcomes remain observations. They are not substituted by universal
stored-value arithmetic on poisoned objects.

The complete projection runs in the portable oracle and the separate common
contract test, so each common target plans 9,936 callback root calls. Selected
direct-JVM probes add the same 4,968 calls while checking all 1,656 actual JDK
observations. The explicit root-entry matrix uses three source literals across
33 contexts (including null, unlimited, all modes and maximum precision), for
198 direct replacement/exception-identity calls. Existing focused tests supply
negative, inexact-unlimited and insufficient-UNNECESSARY exception controls.
All counts describe written assertions, not executed results.

## Evidence actually executed and work not executed

The bounded static/symbolic inventory passed with Kotlin 2.4.21, Liberica25.0.4+9,
256 MiB and a unique source/flags/classpath cache identity:

```sh
env JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca \
JAVA_OPTS='-Xmx256m -Dkotlin.main.kts.compiled.scripts.cache.dir=/private/tmp/krogu-exact-root-563-callback-symbolic-cache-v1' \
/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin \
scripts/inventory-decimal-exact-root-callbacks-563.main.kts
```

It decodes frozen literal metadata and checks small symbolic scale/digit bounds.
It calls no numerical library API or production arithmetic. Its additional
lexical source checks enforce no input formatting/precision/powers in exact
detection, and an exact return preceding normalization and adjustedExponent.
Independent read-only review separately proved the parity criterion, ranges,
integer-kernel absence of decimal input conversion and selected callback counts.
Final revised source/proof/inventory and actual consumer-classification review
passed read-only, with no mathematical or source correctness finding. It checked
all 1,656 archived rows against the original full fields and indices, unique
input keys, generator/probe shape correspondence, numerical-field transfer,
unaffected pow/assertion routes and all six authorized tracked files. Its
optional stateful-N direct-getter recommendation was applied and tests now cover
first injection followed by direct delegation on the second call.

The initial 205-file/24-fixture baseline freeze is immutable. The 22 compact scale
defect observations remain preserved separately from the independently expected
results. Existing source and focused common/JVM tests are not compiled or run;
no Gradle, javac, Native, port math or JDK numerical campaign has executed. The
new root-family test still specifies 19,008 checks, and new diagnostic tests 22
JDK observations; these are planned counts. No huge-object probe has run.

Proposed future wildcard filters are concrete but **unrun**. Only after the exact
execution queue grant:

```sh
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca ./gradlew jvmTest \
  --tests 'io.heapy.krogu.math.DecimalExactRoot563*' \
  --tests 'io.heapy.krogu.math.PortableOracleTest*' \
  --tests 'io.heapy.krogu.math.ScaleBoundaryOracleTest*' \
  --tests 'io.heapy.krogu.math.ScaleBoundaryConformanceTest*' \
  --tests 'io.heapy.krogu.math.DecimalNormalizedRootTest*' \
  --tests 'io.heapy.krogu.math.DecimalPowerSubclassTest*' \
  --tests 'io.heapy.krogu.math.DecimalConformanceTest*' \
  --tests 'io.heapy.krogu.math.NullPrecedenceOracleTest*' \
  --tests 'io.heapy.krogu.math.KotlinSubclassContractTest*' \
  --tests 'io.heapy.krogu.math.IntegerSqrtOracleTest*'

JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca ./gradlew \
  jsNodeTest wasmJsNodeTest macosArm64Test \
  --tests 'io.heapy.krogu.math.DecimalExactRoot563*' \
  --tests 'io.heapy.krogu.math.PortableOracleTest*' \
  --tests 'io.heapy.krogu.math.ScaleBoundaryOracleTest*' \
  --tests 'io.heapy.krogu.math.NullPrecedenceOracleTest*' \
  --tests 'io.heapy.krogu.math.IntegerSqrtOracleTest*'
```

The static freeze audit allows exactly six granted tracked paths and checks all
199 other tracked hashes against the immutable 205-file baseline, including all
24 retained fixtures and all old generators. The raw production prefix/suffix
around sqrt/helpers must remain identical. `handoff-sha256.txt` contains exact
hashes for all changed/new source, snapshot, scripts and evidence.

Task563 stays linked/in_progress. Parent alone integrates/publishes and waits
for exact-head supported CI; tasks542/526 remain open. This code/proof handoff
awaits the execution grant, compilation and runtime gates; it is not task review
or completion. Changes are not committed.

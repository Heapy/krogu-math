# Decimal context contract controls

This scoped `local:526` addition supplies independent ordinary and fully truthful
subclass controls for context division. It does not complete the classification
of the old context-dispatch family or the full task. The old fixture, evaluator,
generator and whole-string assertions remain unchanged and are still required
by the existing suite.

## Contract basis

The [JDK 25 BigDecimal API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
specifies the numerical quotient, precision rounding and result representation.
Division prefers the source scale minus the divisor scale. Zero precision means
exact arithmetic; nonterminating exact quotients, zero divisors and required
rounding under `UNNECESSARY` cause `ArithmeticException`. Null object parameters
cause `NullPointerException`. These contracts determine coefficient, scale and
exception type on the ordinary and fully delegating operands used here.

[Kotlin inheritance documentation](https://kotlinlang.org/docs/inheritance.html)
describes overriding methods and caller-owned `super` access. Explicit calls
through base-typed references must enter the corresponding override. A directly
thrown caller-owned exception propagates. This does not require an inherited
context implementation to forward internally to the exact-division override,
or constrain its other internal virtual calls.

## Complete input projection

The generator reads every one of the 75,728 frozen rows from
`src/commonTest/kotlin/io/heapy/krogu/math/Jdk25DecimalContextDispatchResults.kt`
and verifies its SHA-256:
`88fc3c5bc8277a048a83a9ad9b6546bd8a23ae1ad9a10126409d857367391026`.

It projects source text, divisor text, divisor topology (`ordinary`, `self`,
`null`), context precision and rounding mode. Deduplication preserves first
occurrence order. Metadata override modes and old outcomes are excluded only
from the new controls; no old result is reinterpreted or replaced. Negative
precision preserves a null `MathContext`; zero preserves an unlimited context.
Self division reuses the source object. Null division constructs no divisor.

The projection contains **3,000 distinct inputs**. Every ordinary pair exercises
all four combinations of base/subclass source and divisor. Self and null inputs
exercise both source classes; the divisor cannot vary independently. This gives
**11,472 checks**, with no sampling or omitted projected inputs. Common tests use
four deterministic index-modulo-four partitions.

| Divisor topology | Distinct inputs | Ordinary/truthful checks |
|---|---:|---:|
| Ordinary | 2,736 | 10,944 |
| Self alias | 192 | 384 |
| Null | 72 | 144 |
| Total | 3,000 | 11,472 |

| Context precision | Distinct inputs |
|---|---:|
| Null context (encoded -1) | 48 |
| 0 | 176 |
| 1 | 536 |
| 2 | 544 |
| 7 | 520 |
| 17 | 392 |
| 18 | 392 |
| 19 | 392 |

All eight rounding modes survive the projection. The projected coefficient and
precision boundaries include signed long boundaries, 17/18/19 digit contexts,
large coefficients, zero representations and both signs. They do not imply
coverage of every possible scale or precision.

| Normal public-JDK outcome | Distinct inputs |
|---|---:|
| Coefficient and scale | 2,475 |
| `ArithmeticException` | 429 |
| `NullPointerException` | 96 |

## Coverage map

| Field or interaction | New required control coverage | Old observation status |
|---|---|---|
| Numeric quotient and significant-digit rounding | All projected ordinary/truthful inputs, all meaningful class masks | Poisoned/replacement rows are not converted into mathematical controls |
| Result representation | Exact unscaled coefficient and scale, encoded `coefficient@scale` | Runtime class and unspecified caching remain observations |
| Canonical result text | All 9,580 successful operand-class controls, independently derived from expected coefficient/scale | Specified spelling is preserved; poisoned result expectations remain separate |
| Documented arithmetic and null errors | Exception type only on every projected input | Callback/message/precedence fields remain frozen observations |
| Self alias and null topology | Actual alias or absent object, including zero/nonzero self division and null contexts | Existing modes and traces remain untouched |
| Truthful subclass behavior | Delegating sign, precision, scale, coefficient, abs, negate, exact and context divide | Consistent sign/precision alone never certifies the old probes |
| Explicit Kotlin method entry | Base-typed exact/context calls across distinct/self/null divisors and finite/unlimited/null contexts preserve entry, arguments and caller-owned replacement | No inference about inherited internal forwarding |
| Direct injected exception | Same exception object and arguments in the same 24-call replacement/throwing matrix | No poisoned-getter precedence inference |
| Caller-owned `super` | Truthful finite, unlimited and self arithmetic, plus null error | No no-callback, noncall or trace-order assertion |

`DecimalContextContractTest.kt` additionally derives its own bounded witnesses:
10.50/2.0 and 11.50/2.0 are 5.25 and 5.75. Rounding their tenths coefficients
means rounding 52.5 (even retained digit) or 57.5 (odd retained digit), across
both operand signs and every rounding mode. Thus precision-two `HALF_EVEN`
produces 52@1 or 58@1, with the quotient sign applied. Zero 0.00/2.0 has preferred
scale 2-1=1; exact 5.25 needs scale 2. Reduced denominator 3 supplies a
nonterminating exact witness. Dropping the final digit of 9223372036854775808
rounds upward to 922337203685477581@-1 at precision 18; eighteen nines carry to
10^18 with a 17-digit coefficient and scale -2. These expectations are derived
from rational arithmetic and digit retention, separately from generated data.

Canonical expected text uses only the fixture coefficient's digits and scale.
The adjusted exponent is `digits.length - 1 - scale`, calculated as `Long`.
Nonnegative scale and exponent at least -6 use plain decimal placement; other
cases use scientific notation with the signed exponent. Coefficient trailing
zeros and scale remain intact. Nineteen independent literals cover both sides
of the -6 threshold, zero, signs, trailing zeros and Int scale endpoints; each
checks the expected formatter and ordinary/truthful results. Explicit caller
controls make six exact and eighteen context calls. Each verifies exactly one
entry into the selected override, its original arguments and the caller-owned
result or exception identity; this is not an inherited forwarding requirement.

## Limits of the old observations

The [input inventory](../diagnostics/decimal-context-dispatch/README.md) records
57,576 rows with inconsistent configured sign/precision returns, 7,248 further
rows with throwing sign/precision callbacks and 10,904 rows whose configured
sign/precision returns are consistent. Every old probe still poisons `scale`,
`unscaledValue`, `abs` and `negate`, and replaces exact division with `7.00`.
The last group is therefore not a fully truthful control group.

The numerical/error requirements survive on the ordinary and fully truthful
operands. The old invocation plus trace is not a universal normative assertion:
a contract-preserving implementation may call a getter poisoned by that probe.
This does not declare every operation on a poisoned subclass unconstrained.
Each method and interaction still requires its own contract analysis. Internal
call counts/order, selected noncalls, exact-override forwarding and diagnostic
wording are observations here. Some callback, null and arithmetic failure
precedence interactions remain unresolved. None of the old assertions are
archived or removed by this addition.

### Null arguments and an unlimited-context replacement

Frozen row `D07457` illustrates why a null input alone does not settle an old
poisoned outcome. Its seven fields are:

```text
0.00\t2.0\tdelegate\tnull\t0\tUP\t7.00:2|A.exact-null;
```

Here `0` means an unlimited, non-null context. The divisor is null. The probe's
exact `divide` override accepts that argument, records `A.exact-null`, and returns
its caller-owned replacement `7.00`. The separate truthful control `C0625` has
topology `null`, precision `0`, rounding `UP`, and requires
`NullPointerException`. Its expected error is not the old replacement outcome.

The compact row is `D07457 C0625 M01 M06 V 051`. Mask `051` contains only the
poisoned-probe, null-divisor and invoked-exact-replacement flags (`001`, `010`,
`040`); it does **not** contain the null-context flag `008`. Stable IDs count
tab-separated fixture records, not physical Kotlin source lines, because the
fixtures contain multiple string chunks.

Current source ordering can explain an invocation, but it cannot independently
prove a required internal-forwarding or callback-precedence rule. This row's old
value/error status remains `UNRESOLVED_POISONED_INTERACTION`, its trace remains
`REFERENCE_OBSERVATION`, and archival remains blocked. Explicit caller entry and
caller-owned result/exception identity are covered by the separate language
controls. This analysis changes no fixture, assertion or numerical code, and
does not claim a JDK defect or waive the inherited method's contract.

## Reproduction and verification

Run from the repository root with Kotlin 2.4.21 and the public JDK 25.0.4+9 API:

```sh
JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca \
JAVA_OPTS='-Xmx512m -XX:-OmitStackTraceInFastThrow -Dkotlin.main.kts.compiled.scripts.cache.dir=/private/tmp/krogu-contract526-cache' \
/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin \
scripts/generate-decimal-context-contract-oracle.main.kts --check
```

Without `--check`, the script regenerates only the new contract fixture. The
script obtains expectations from normal public-JDK instances and checks all
fully delegating JDK subclass combinations against them. Its output records
input, topology, precision and outcome counts. No implementation sources,
trace, runtime class, cache identity or exception message enter the fixture.

At creation, generation and `--check` passed, including all 11,472 public-JDK
checks. Whitespace/row-shape/projection checks passed. Common Gradle and Native
compilation/tests were **not run** during this subtask because the shared build
window was reserved by another worker; they remain integration checks.

Parent integration subsequently passed the combined JVM/JS/WasmJS/macOS ARM64
run in 8m20s (`/private/tmp/krogu-root-context-double-final.log`). Each target ran
all eight methods in this new suite without failures, errors or skips, alongside
canonical-Double, scale/operator and retained oracle checks. The complete run
contained 89 JVM methods and 73 per other target. This finite evidence does not
close the field-level classification; the old 75,728-row assertions remain
required and unchanged.

The follow-up canonical-text/direct-caller controls passed the focused four-target
run in **3m44s** (`/private/tmp/krogu-root-context-text-dispatch.log`). Each target
ran nine contract methods plus the retained 75,728-row context-dispatch method,
with zero failures/errors/skips. This preserves all 11,472 value/error checks
and adds 9,580 canonical-text comparisons, 57 literal assertions and 24 direct
caller cases on every target. The field correspondence manifest records the
remaining unresolved poisoned interactions; it does not authorize archival.

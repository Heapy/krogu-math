# Decimal context contract controls

The original scoped `local:526` addition supplied independent ordinary and fully
truthful subclass controls for context division without changing the old fixture,
evaluator, generator or whole-string assertions at that stage. The later 575
non-null migration separately retains selected historical null observations and
requires real JVM boundary rejection. The one-row analysis below distinguishes
those scopes; it changes no fixture, evaluator, generator or assertion and does
not complete the context-dispatch family or the full task.

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

### Historical v3 milestone: eight required null-context exception types

Rows `D07425`–`D07432` map to `C0609`–`C0616`: source `0.00`, divisor `2.0`,
ordinary topology, both modes `delegate`, null context and all eight rounding
modes. Their compact records retain `M01/M01/E/009`. The public null-argument
contract requires `NullPointerException` for these exact calls. The caller
explicitly enters `super.divide(divisor, null)`; current source rejects the
context before any latent poisoned getter or replacement callback can execute.
This path explanation does not make internal noncalls or trace order mandatory.

Only their exception-type status becomes `REQUIRED_NULL_ARGUMENT_CONTRACT`.
The frozen `!NullPointerException|` strings, reference traces, unobserved identity,
latent flags and every archival block remained. At this v3 milestone the other
75,720 poisoned outcome statuses remained unresolved. This classification did not extend
to the null-divisor/unlimited-context replacement below.

The revised mapper validates exact IDs, inputs, modes, controls and masks and
losslessly recovers all 19 fields. Isolated generation and final checking passed;
all 75,729 noncomment lines of the six-column payload stayed byte-identical.
The original artifact is retained, and the expected first check against its old
header remains a recorded failure. The [integration report](context-modular-integration-verification.md)
records hashes and verification scope. No mathematical code, frozen fixture or
existing assertion changed for this metadata update.

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

For **D07457 alone**, classify the old `7.00:2` prefix as a reference observation
of a **contract-breaking exact-division override**, not a required numerical
result of inherited contextual division: that override returns normally for a
null argument instead of rejecting it. C0625's required valid-control outcome
remains `NullPointerException`. The recorded `A.exact-null;` is an internal
forwarding observation, not a required callback, order or noncall; trace status
stays `REFERENCE_OBSERVATION`, message status `NOT_RECORDED`, and result identity
`NOT_OBSERVED`. The explicit language entry in this row is
`ContextProbe.enter -> super.divide(null, unlimitedContext)`. It does not
explicitly call the one-argument override. Separate well-typed/reachable direct
caller controls require selected override entry and caller-owned result/exception
identity; they do not require inherited exact forwarding. The historical nullable
probe is not the current non-null Kotlin API: input-only selection sends old
ordinal 7456 to the real Java explicit-super replay in `DecimalBinary575JvmTest`,
requiring NPE and retaining the old observation separately; C0625/ordinal 624 has
its ordinary/truthful JVM counterpart. Permitted override-nullability edits do
not preserve a requirement to accept null. This one-row analysis neither declares
a JDK bug, waives inherited contracts nor invalidates truthful subclasses.

At the original source-only field-analysis milestone no new manifest status
was materialized. The preserved v3 mapper has no D07457 binding: its `requiredNullRows` and
`validateStatusHeaders` permit exactly the eight reviewed error rows
D07425–D07432. Consequently the stored v3
`old_value_or_error_status` for D07457 was
`UNRESOLVED_POISONED_INTERACTION`; that analysis changed no TSV or count. Its
materialized counts were eight required error-type statuses and 75,720 unresolved statuses
(58,093 value and 17,627 error), with every row blocked for archival. Recording
the new one-row value-prefix analysis in executable metadata required the bounded
successor policy and separate validation below, not an unvalidated ninth header.
The frozen observation/control and all current assertions remain intact.
No script, compiler, helper, numerical operation, probe or test ran for this
source-only analysis; exact NPE text and inherited internal traces are not newly
promoted to requirements.

### Historical source-only v4 preparation

The source-only successor mapper adds exactly
`D07457 C0625 M01 M06 V 051` with the value-prefix status
`REFERENCE_CONTRACT_BREAKING_EXACT_OVERRIDE_RESULT`. It pins the full seven-field
old row, the full six-field C0625 NPE control and the historical generator source
containing the nullable probe. The eight required error bindings are unchanged.
No generic unsupported status or trace/identity/archival exemption is added.
The compact payload guard still refuses any change to its header or 75,728 rows;
all other logical selectors, interaction flags and archival blocks remain.

The prepared mapper's bounded policy predicted eight required error-type
statuses, one reference-only value
prefix, and **75,719 unresolved statuses**: 58,092 value and 17,627 error. These
were source-derived expected counts, not results of a run at that milestone.
The materialized v3 TSV then remained at eight required / 75,720 unresolved. The successor
separately verifies the migrated common evaluator hash `bb61d3f8…` while retaining
the old `2ed00a5d…` pin as explicitly historical provenance; no evaluator or
assertion is edited. No predecessor check on current sources is claimed.
The [source preparation record](decimal-context-value1-policy-526.md) lists full
pins, predecessor preservation and exact field scope.

### Root2300: accepted actual v4 metadata validation

Parent generation session 93225 and check session 39678 both exited 0 using the
frozen mapper `78869800…`, its four pinned source files, a 512MiB script heap and
a fresh isolated compiled-script cache. Independent read-only actual review
accepted the evidence. All 75,728 compact data records and their six-column
header stayed byte-identical; only the bounded metadata changed. The TSV
adopted at that v4 milestone had SHA-256
`f8f797e79d706fbed221e3ec73f77778f1f952a938d071e972983ffb45098927`.
It had exactly nine status bindings: eight required error types, one reference-only
value prefix and 75,719 unresolved statuses; every archival block remains.

The [actual validation record](decimal-context-value1-policy-526.md#root2300-actual-metadata-validation)
gives full source/log hashes and retained v3 evidence. This was metadata-only
validation: no mathematical test or old public-JDK oracle rerun. Adoption was
not yet committed or pushed at this documentation handoff; root owns publication
and CI. Fixtures, evaluators and assertions remain unchanged.

### Historical source-only v5 class preparation

The source-only mapper constructed one bounded class from inputs: source
texts `0.00`, `10.50`, `-10.50`; source modes `delegate`, `sign-first`,
`sign-second`, `precision`; divisor text `2.0`, null divisor, unlimited non-null
context and all eight roundings. Its 96 VALUE bindings include D07457 and add
95 reference-only classifications under the same public null-rejection argument.
Historical exact override acceptance of null and its returned `7.00` are
contract-breaking replacement behavior. The direct entry remains
`enter -> super.divide(null, unlimitedContext)`; later exact forwarding is
diagnostic, not required. Latent throwing modes do not waive inherited contracts.

Full frozen rows, 24 NPE controls and exact D/C/mode/kind/051-or-055 masks are
guarded after input-only selection. All other logical fields and compact payload
bytes stay protected. Expected v5 counts are eight required errors, 96
reference-only values and 75,624 unresolved statuses (57,997 VALUE / 17,627 ERROR),
with every archival block. The then-adopted v4 TSV remained unchanged during
that preparation: eight required / one reference-only / 75,719 unresolved.
The successor was **UNCOMPILED/UNRUN** at that milestone; no script, compiler,
helper, test, probe, numerical operation or metadata validation ran during its
source preparation. This is not family closure or a JDK bug claim.
The [class preparation record](decimal-context-value1-policy-526.md#historical-source-only-v5-class-preparation)
gives exact formulas, excluded boundary cases and the then-pending parent gates.

### Root2310: accepted actual v5 metadata validation

Generation session 36380 and check session 47559 both exited 0 with frozen
mapper `37f861df…`, its four unchanged input pins, a 512MiB heap and fresh
isolated cache. Independent actual review accepted the evidence. The TSV
adopted at that v5 milestone had SHA-256
`8d9c9bfb5657b684b19e76dcc5b21176f94fa7601e753ced84f3748760d3fd43`.
All 75,728 compact rows and their six-column header stayed byte-identical to
v4. Its 104 STATUS records bind eight unchanged required errors and 96
reference-only values (24 mask051 / 72 mask055), including D07457, hence 95
new classifications. There remain 75,624 unresolved statuses (57,997 VALUE /
17,627 ERROR) and all 75,728 archival blocks. The metadata correspondence still
has 3,000 controls and 11,472 class-mask checks.

The [actual validation record](decimal-context-value1-policy-526.md#root2310-accepted-actual-v5-metadata-validation)
preserves exact logs/diff/pins and v4 evidence. No production or public-JDK
numerical evaluation or tests ran; fixtures, evaluator and assertions stayed
unchanged. Local calls are terminal and the mutex released under Root2311.
Hosted CI for the new mapper remains pending. This finite metadata result
does not complete the family or authorize archival.

### Historical source-only v6 historical-null-error preparation

The independently reviewed finite policy in
`/private/tmp/krogu-526-next-input-class-proposal.md` (`f43773e0…`) prepares a
coherent input-selected class:192 null-context entries plus96 finite-p2/null-
divisor entries, excluding the original8 required ERROR bindings. Only those
280 old error-prefix statuses would become
`REFERENCE_HISTORICAL_NULL_INPUT_ERROR`. The original8 required errors and all96
v5 reference replacement values remain unchanged. Full records/controls and
exact modes/kinds/masks guard the generated dictionary; outcomes are checked
evidence, never selectors.

Current575 Java-to-Kotlin NN rejection remains a separate required NPE boundary
for all384 null-input entries, alongside the96-key/240-mask ordinary/truthful
controls. Historical nullable inherited failure precedence against poisoned
getters or replacement callbacks is not proved by those current tests or an
empty frozen trace. The new historical-reference status supplies no blanket
waiver of unaffected contracts and changes no assertions or archival blocks.

Expected v6 totals are8 required ERROR +96 reference VALUE +280 reference
historical ERROR +75,344 unresolved (57,997 VALUE /17,347 ERROR). All75,728 rows
remained blocked for archival. During that preparation the adopted TSV was
still v5 `8d9c9bfb…` with its accepted actual totals above. The successor was
**UNCOMPILED/UNRUN** at that source-only milestone: no generation/check,
compiler, helper, numerical operation, probe, tests or build ran then.
The [source preparation record](decimal-context-value1-policy-526.md#historical-source-only-v6-historical-null-error-preparation)
contains exact binding/preservation and the then-pending independent
review/execution gates. No whole-family closure or historical JDK defect is claimed.

### Accepted actual v6 metadata validation

Generation37902 and check48932 exited0 with frozen mapper `08adda6a…`, its four
unchanged evidence pins and512MiB heap in an isolated validation workspace.
Independent actual review accepted adoption of v6 TSV
`bc03df8ef1e68a1d602181901f02466d677ab10cace9d4a3d1a57aee400f1b8f`.
All75,728 compact rows and their six-column header stayed byte-identical to v5.
The384 STATUS bindings comprise8 unchanged required ERROR,96 unchanged
reference VALUE and280 separately classified historical ERROR prefixes.
There remain75,344 unresolved statuses (57,997 VALUE /17,347 ERROR), with
every archival block retained. Current575 NN384 and96-key/240-mask truthful
controls remain independent mandatory obligations, not new execution claims.

The [actual record](decimal-context-value1-policy-526.md#accepted-actual-v6-metadata-validation)
lists retained logs/diff/source hashes and v5 evidence. This was metadata-only
generation/check, not production/public-JDK numerical or test execution.
Fixtures, evaluators and assertions stayed unchanged. Parent owns hosted CI
and publication; full-family closure and archival permission remain unclaimed.

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

# JDK 25 conformance audit

Objective: full Kotlin source and behavioral compatibility for the migration
**Java → Kotlin using java.math → Kotlin using krogu-math**, with the public
Java SE 25 numerical contract and documented Kotlin stdlib operators/extensions
exposed in `io.heapy.krogu.math`. Arithmetic remains independent Apache-2.0
common Kotlin. Java source compatibility is required only in the TCK harness,
where test-only adapters may supply the expected Java call shapes. Public types
may use interfaces and constructor-like Kotlin factories; real Java constructors
and direct Java subclass source compatibility are not library requirements.
Kotlin nullability follows the [nullability policy](docs/kotlin-nullability-policy.md):
inputs that reject null must be non-null. Migration may require matching caller
and override edits; task573 implements the reviewed 48-input migration.
Proven JDK defects are handled as specified below. The objective remains
**active and unproven**.

The task573 source composition contains G1–G7: all 48 reviewed input
slots. It combines task574 comparison, task575 decimal binary/context, task576
unary-context, task577 bitwise and task578 validation/constructor/random changes.
Revision4 retains task525's four non-null extrema result declarations and classifies
RNG consumption as diagnostics. The exact adopted source passed all four local
platform gates (1,416 method executions across JVM, JS, Wasm JS and macOS ARM64)
and all 49 reviewed input-nullability compiler jobs. A selected public campaign passed 30 adapted
invocations with 115 declared coverage skips. Four original joint-invalid
constructor calls remain outside that success; their earlier failure is preserved.
Published-revision CI is recorded separately. The [current verification report](docs/non-null-api-573-verification.md)
records accepted evidence, including all six jobs passing on published de5,
and remaining broader obligations; this does not establish full
compatibility or JCK conformance.
The first48-input JVM run compiled and executed492 methods, with one test-counter
failure retained in the [revision3 handoff](docs/non-null-573/POST_REVIEW_HANDOFF.md).
The next V3 run reached an unsupported seeded-state equality assertion and also
failed one method. Both failures and the subsequent contract classification are
preserved in the [revision4 review](docs/non-null-573/RNG_CONTRACT_REVIEW.md).
Historical runtime and callback
summaries below remain evidence of the published baseline. Exact Java boundary contracts, preserved diagnostics and
all 5227 reassigned common executions are described in the
[integration handoff](docs/non-null-573/INTEGRATION_HANDOFF.md), with the subsequent
revision3 changes linked above. Nullable equals inputs and OrNull results retain
their declarations; extrema results become non-null under task525.

Reference runtime: BellSoft Liberica **25.0.4+9**. Implementation source from
OpenJDK or other numerical libraries must not be used. Public API specifications,
independent mathematical reasoning and black-box results supply evidence; a
runtime result does not override the public contract.

The [contextual-zero and modular integration report](docs/context-modular-integration-verification.md)
records the preceding four local target gates, bounded modPow measurements and
unchanged public-test campaign, including its three retained failures. Direct
contextual zero-scale behavior and the finite unary observation policy supersede
earlier broad trace summaries for those exact cases. The report also records
eight narrowly classified null-context exception types; broader audits remain open.

The later [normalized division report](docs/normalized-division-verification.md)
records the exact d0 candidate, four target gates, bounded time/allocation pilot
and selected public campaign, retaining its PrimeTest failure and timing regressions.
The separate [zero sqrt-null validation](docs/zero-sqrt-null-568-verification.md)
records the null-contract repair and its own current execution evidence.
The [Double zero/null validation](docs/zero-double-context-569-verification.md)
records the constructor repair, retained JDK observations and fresh target/public gates.
The later [inexact sqrt normalization](docs/sqrt-normalization-579-verification.md)
records the private-workspace range repair and1501 completed local method
executions. Its giant boundary witness is symbolic; supported matching-head CI,
general resource work and broader power/dispatch audits remain separate.
The subsequent [contextual-product repair](docs/decimal-context-product-580-verification.md)
rounds a private decimal product before publishing its required coefficient.
Its four-target campaign completed1517 method executions with no failures or
skips. Maximum-size witnesses remain symbolic; matching-head hosted gates and
the broader compatibility tasks remain separate.
The subsequent [contextual quotient repair](docs/decimal-context-division-581-verification.md)
and [integral quotient repair](docs/integral-quotient-582-verification.md) avoid
oversized normalization intermediates in their reviewed routes. Their fresh
four-target campaigns completed1538 and1559 method executions respectively,
with no failures or skips. Maximum-size resource assumptions and broader
range/dispatch audits remain open.
The [midpoint refinement](docs/scaled-division-midpoint-584-proof.md) compares
the remainder with its complement instead of constructing twice the remainder,
so that an unnecessary midpoint temporary cannot reject a supported result.
Its giant range witness is symbolic; fresh local results are recorded separately
in the [584 verification report](docs/scaled-division-midpoint-584-verification.md).
The subsequent [residual repair](docs/decimal-residual-583-verification.md)
certifies the selected integral quotient and computes its remainder without
publishing an oversized intermediate product. It also repairs the earlier
finite-context integral admission check. Its four-target campaign completed1584
method executions without failures or skips; broader scale/cohort combinations,
resource limits and matching published-head gates remain separate.
The [integer arithmetic and bit audit report](docs/integer-audits-544-545-verification.md)
records the later test-only integration, four local target gates and all six CI
jobs passing on published commit60aea413, including actual Linux ARM64 execution.
The later [decimal normative finite-control report](docs/decimal-overload-541-verification.md)
records six additional common methods, four accepted local target gates and
the remaining full-domain/dispatch proof limits. All six matching-head CI jobs
pass on published de5, including the actual314-method Linux ARM64 runtime.

The later [consolidated public OpenJDK campaign](docs/public-math-consolidated-559.md)
passed106 baseline/controlled-port invocations with0 failures and46 declared
coverage skips on the then-production ded66 JAR. It covers the unfiltered
standard, serialization, heavy and512MiB profiles through the frozen b730 harness.
The later [82b1 hosted CI verification](docs/public-math-ci-559.md#hosted-verification)
independently passes106/0/46 on published eaaea5, with all seven workflow jobs
successful and actual314/65 Linux ARM64 inventory audited. Larger profiles were
not selected by that job despite a snapshot above their capacity thresholds; their
separate attempt, explicit adaptation losses and the full Kotlin/domain contract
remain outside this finite success. This is not official JCK certification.
The later [e36 large-profile campaign](docs/public-math-large-e36-verification-559.md)
passed12 invocations with0 failures and128 declared skips on its exact234-source
checkout. The [same-source normal campaign](docs/public-math-e36-regular-559.md)
subsequently passed106 invocations with0 failures and47 explicit skips; together
their disjoint PASS sets cover all59 retained primary plans in both phases.
The13 omitted port calls/phases remain explicit losses, and this is not official
JCK certification. The [supported-platform audit](docs/e36-supported-platform-verification.md)
records5,233 successful method executions across13 target instances, with iOS
device compilation separate. Later583/585/586 sources are outside those e36 results.
The subsequent [f58 large-profile campaign](docs/public-math-large-f58-verification-559.md)
also passed12 invocations with0 failures and128 declared skips, now bound to the
exact236-source checkout and the same391b73 own JAR as local583 validation.
The harness and original large profiles were unchanged. Its separate normal
CI and the full numerical/range contract are outside that large-only result.
The [same-f58 supported-platform audit](docs/f58-supported-platform-verification.md)
records5,289 successful method executions across13 target instances, including
the actual349-method Linux ARM64 run; iOS device coverage is compilation only.
All seven workflow jobs subsequently completed successfully. The separate
[same-f58 public ledger audit](docs/public-math-f58-regular-559.md) confirms106
normal PASS/0FAIL/47SKIP. Together with its12 large PASS records, their disjoint
sets cover all118 retained primary IDs; all13 adaptation losses remain explicit.
The source240585/586 revision is outside those f58 results.

The [585/586 repair and local verification](docs/decimal-sum-sticky-585-586-verification.md)
address the later source-confirmed addition range defect: both finite
rounding of an oversized exact sum and exact cancellation of an oversized
aligned term can have supported final coefficients. The private raw sum now
rounds or cancels before publication. The separate586 stickyAt equality repair
selects a nonzero truthfulD-digit coefficient's zero quotient without building10^D.
All1,610 source-derived method executions pass on JVM/JS/Wasm/macOS ARM64 with
the frozen240-source composition and fresh own JAR. Maximum-size witnesses stay
symbolic, not executed JDK failures. Matching hosted/public gates and the broader
541/P1/548 obligations remain separate.
The [same-source large-profile report](docs/public-math-large-2e767-verification-559.md)
subsequently confirms12PASS/0FAIL/128SKIP with all selected fresh-memory gates,
original profiles/limits and producer/consumer/local JAR1cacc provenance verified.
The separate normal/platform run remains outside that large-only result.

The subsequent [source240 normal public report](docs/public-math-source240-normal-559.md)
confirms106PASS/0FAIL/47SKIP in38821252611 at2e767, with all seven jobs successful.
Its normal106 and same-source large12 records are disjoint and cover all118
retained baseline/port IDs. All13 adaptation losses remain; the later proposed
587 sqrt-call restoration was not executed by either published-source campaign.

The [source240 supported-platform report](docs/source240-supported-platform-verification.md)
records5,375 passing method executions from all six supported/oracle jobs of
38821252611 at2e767, with exact source inventories, original XML and actual
Linux ARM64 execution. The public job is excluded from that snapshot;
iOS-device coverage is compilation only. These results validate the published
585/586 composition, not the subsequent587/588 candidates.

The subsequent [source247 local integration report](docs/decimal-text-sqrt-587-589-verification.md)
records the587 inexact sqrt precision/cohort repair,588 contextual lexical
conversion before full-coefficient materialization, and589 exact CharArray
construction without a whole-input String. All1,687 method executions pass on
JVM/JS/Wasm/macOS ARM64. A separately checked complete94,282-record sqrt ledger
retains strict coefficient/scale/precision/text/error checks and independently
certifies2,473 required inexact cohort lifts. Unchanged p-digit references and
error decisions have the narrower acceptance scopes recorded in that report.
The subsequent [source247 hosted report](docs/source247-hosted-verification.md)
records 5,626 supported-platform method executions across 13 target instances
and 25 successful oracle commands at published 47447f2. Normal 106 and separate
large 12 PASS IDs form the exact disjoint 118 retained-ID set, with 12 adaptation
losses still declared. The original public sqrt scale-overflow call is restored
and executed. Giant lexical/range witnesses remain symbolic; broader 548/549
obligations and the 525/557 Number caller issue remain open. Private source248
is excluded from this published snapshot.

The later [590–592 local range verification](docs/decimal-range-590-592-verification.md)
records the exact250-source composition: the590 exact LOW residual certificate,
591 primitive low-bit conversion and comparison normalization, and592 fixed-scale
division without oversized disposable intermediates. All1,723 method executions
across426 suites pass on JVM/JS/Wasm/macOS ARM64. The complete94,282-record,
52-recorder sqrt ledger retains strict fields and is byte-identical to247/248.
At that local milestone, the giant590/591/592 witnesses remained symbolic,
source593 pow was unimplemented and matching source250 hosted gates were pending.
The later [source250 supported/oracle report](docs/source250-hosted-verification.md)
confirms 5,743 methods across 13 target instances and 25 oracle checks at published7443.
The [same-source public campaigns](docs/source250-public-math-verification-559.md)
confirm 106 normal and 12 large PASS records, forming the exact disjoint 118
retained-ID set with all 12 adaptation losses explicit. Earlier247 results retain
their original source/JAR binding; broader548/549/526 and JCK remain unproved.

The subsequent [593 local verification](docs/decimal-power-593-verification.md)
keeps the true enlarged working precision and private coefficient through work
rounding and reciprocal before final publication. All 1,744 methods/431 suites
pass on JVM/JS/Wasm/macOS ARM64; the complete94,282-record sqrt ledger remains
byte-identical to250. The tiny sparse maximum-precision family is executed,
while dense giant witnesses remain symbolic. At that local milestone, existing
working-scale admission, broader accuracy/resource/subtype obligations and
matching published-head253 hosted/public gates remained separate.

The later [source253 supported/oracle report](docs/source253-hosted-verification.md)
confirms5,812 methods across13 target instances and25 oracle checks at published62bb.
The [same-source public campaigns](docs/source253-public-math-verification-559.md)
confirm106 normal and12 large PASS records, forming the exact disjoint118 retained
IDs with all12 adaptation losses explicit. Those outcomes remain source253.

The subsequent [594 local verification](docs/decimal-division-normalization-594-verification.md)
removes whole noncompact coefficient Strings from numerical division normalization
using the existing stored digit/significand helpers. Its five-line production
delta preserves bothcompact old bodies and changes no public API or numerical
kernel. All1,757 methods/436 suites pass on JVM/JS/Wasm/macOS ARM64; the complete
94,282-record/52-recorder sqrt ledger remains byte-identical to253/250. Independent
source, actual-XML/ledger and publication reviews retain exact provenance. The
maximum String-capacity witness is conditional and unexecuted, not an observed
engine or JDK failure. Matching source255 hosted/public acceptance is separate.

The later [source255 supported/oracle record](docs/source255-hosted-verification.md)
confirms 5,855 exact method executions across 13 target instances and 25 separate
oracle commands at published12d. The [same-source public record](docs/source255-public-math-verification-559.md)
confirms 106 normal and 12 separately selected large PASS IDs, with an exact
disjoint 118-ID union covering 59 retained plans and all 12 adaptation losses explicit.
These outcomes belong to source255, with iOS-device coverage compilation only.

The subsequent [Kotlin call-form repair](docs/kotlin-callform-595-596-verification.md)
moves arithmetic wrappers to imported extensions so caller-owned operators remain
legal, and adds an imported `BigInteger.lowestSetBit` property delegating to the
public virtual getter. All 1,785 methods/446 suites pass locally across four
targets; the complete 94,282-record/52-recorder ledger remains byte-identical to255.
The [finite call-form inventory](docs/kotlin-migration-525-callforms.md) preserves
the older source255 findings and their later disposition. Matching source260
hosted/public evidence retains that historical source binding. The later accepted
caller-side Number migration is documented below; it does not repair the original
Kotlin/JS compiler behavior.

The [unified range/allocation ledger](docs/supported-range-allocation-548-ledger.md)
maps public entries to selectors, fallbacks, guards, indices and simultaneously
live buffers. The [current decimal overload](docs/decimal-overload-541-current-binding.md)
and [constructor/API](docs/api-constructor-549-current-binding.md) bindings preserve
the historical inventories and connect their repaired leaves to current source.
The [oracle boundary](docs/oracle-contract-boundary-526.md) permits precise retained
research dispositions without hiding any known required mismatch or removing
the active stronger reference assertions. Necessary physical resources and
qualified scale/compound-invalid questions do not create a smaller numerical cap.

The per-entry [BigInteger](docs/integer-public-contract-549-ledger.md) and
[BigDecimal](docs/decimal-public-contract-549-ledger.md) API/error ledgers,
together with the existing [context/mode ledger](docs/context-mode-contract-549-ledger.md)
and [constructor inventory](docs/constructor-overload-inventory-549.md), account
for the public surface and identify the remaining numerical, subtype and range
leaves. Their historical source bindings and conditional premises remain
explicit; the later private repairs are not accepted by those ledgers alone.

Two confirmed extreme-scale decimal defects, their bounded mathematical witnesses,
retained JDK diagnostics and deliberate numerical divergences are documented in
[Decimal scale contract](docs/decimal-scale-contract.md).

Representable exact/context division despite an unsupported intermediate scale,
including rounding carries, is documented in the [division normalization contract](docs/decimal-division-normalization-contract.md).

Division with a preferred scale outside Int is tracked in the [preferred-scale division contract](docs/decimal-division-preferred-scale-contract.md).

The [integral/remainder endpoint report](docs/decimal-residual-583-verification.md)
records small-coefficient inputs with supported quotient/remainder pairs and
their pinned-JDK `Overflow` observations. The independent required results,
ordinary-scale controls and precise divergence scope are retained together.

The portable equality/order/hash laws, context/enum contracts and mutation/alias
witnesses are documented in [Value identity and immutability](docs/value-identity-contract.md).

The independent canonical Double candidate argument, binary-boundary regressions
and exact coefficient/scale/text corpus are documented in
[Canonical Double contract](docs/canonical-double-contract.md).

A [stdlib-only compiler diagnostic](diagnostics/js-number/README.md) independently
reproduces Kotlin/JS 2.4.21 Number/generic conversion corruption and the known
upstream bug. Bounded runner and coercion checks preserve the failing evidence;
they do not repair the original `Number.toX()` calls. The accepted caller-side
migration workaround is to explicitly import and call `Number.toKroguByte`,
`toKroguShort`, `toKroguInt`, `toKroguLong`, `toKroguFloat` and `toKroguDouble`.
These common extensions select the concrete krogu type's virtual method and
retain subclass dispatch; other `Number` values keep their platform conversion.
See [trigger cases, examples and limitations in README](README.md#kotlinjs-converting-through-number).
This changes the affected Kotlin call sites and does not fix conversions inside
third-party code or unrelated custom `Number` implementations. Historical
557/525 failure evidence remains unchanged.

Message-oracle runs and JVM test workers use `-XX:-OmitStackTraceInFastThrow`.
A repeated scaled-zero-division campaign exposed 6,975 null messages caused by
HotSpot exception reuse; rerunning with this flag restored the canonical `/ by zero`
message in every such case. The generator verifies the effective VM option.
This diagnostic configuration follows [Oracle's documented flag](https://www.oracle.com/java/technologies/javase/release-notes-introduction.html)
for disabling preallocated exceptions; numerical values and exception classes
are compared independently of this optimization.

## Normative contract and oracle classification

The public [Java SE 25 package contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/package-summary.html)
and documentation of [BigInteger](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html),
[BigDecimal](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html),
[MathContext](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/MathContext.html)
and [RoundingMode](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/RoundingMode.html)
define the numerical and behavioral requirements. Kotlin source migration also
includes documented [operators](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/div.html),
[numeric conversions](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/to-big-decimal.html)
and [string conversions](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/to-big-decimal.html).
For example, Kotlin decimal `/` keeps the dividend scale and rounds HALF_EVEN;
it is not interchangeable with exact `divide(other)`.

| Classification | Evidence needed | Compatibility obligation |
|---|---|---|
| Specification guarantee | A public contract clause or Kotlin language/stdlib guarantee, plus an independent expected result where numerical | Preserve it, including specified values, rounding, scale, encodings, text, range, error conditions/types and identity. Kotlin caller compilation and overload resolution remain migration obligations, with explicit caller/override nullability edits permitted by the nullability policy. |
| Reference observation | Reproducible public inputs and outputs on the pinned runtime | Retain as diagnostic evidence and preserve useful additional behavior when consistent with the contract. Unspecified callback order, allocation/cache choices, exact diagnostic wording and random sequences do not become independent requirements solely from one JDK build. |
| Proven JDK defect | Minimal public reproducer, the precise violated clause, independent expected behavior and tested JDK version/configuration | Follow the contract, retain the reference counterexample and add a regression for correct behavior. Record every deliberate divergence explicitly. |
| Unresolved difference | A reproducible mismatch without sufficient normative or mathematical evidence | Investigate; do not silently ignore it or call it a JDK bug because documentation is absent. |
| Contract-violating subclass input | A specific overridden method violating a documented invariant or method contract | Keep the probe as diagnostic evidence; it does not establish a required numerical outcome. This classification does not waive Kotlin extensibility or contract-preserving overrides. |

Documented identities remain requirements: for example BigDecimal `plus()` returns
its receiver, `min`/`max` ties return the receiver, and stripping a numerical zero
returns `ZERO`. Where `setScale` permits reuse or allocation, one runtime's choice
does not add an identity guarantee. Dynamic dispatch of an explicit overridable
call follows the language contract; internal call counts/order require their own
documented guarantee before becoming normative.

The [549 NPE audit](docs/npe-message-policy-549-audit.md) applies this distinction
to the current normalization helpers: null rejection remains required, while
unrecorded VM/compiler wording remains an unverified observation. Caller-created
exceptions and their supplied messages are separate. The
[MathContext/RoundingMode ledger](docs/context-mode-contract-549-ledger.md)
records the two small types' complete declared surface and conditional source
arguments. Canonical MathContext text and round trips are required; the ledger
records the retained broader accepted grammar as a compatibility extension.
Neither memo closes the full549 error/call-site audit.

BigDecimal precision is the number of decimal digits in its unscaled value,
with zero having precision one. An override reporting negative or inconsistent
precision violates that invariant. Such a probe can reveal implementation
mechanics, but cannot alone prove either a JDK defect or a required port result.
Contract-preserving subclasses and documented exception/identity behavior remain
in scope. Current corpora contain mixed categories; full case classification is
tracked in kotgent `local:526`, not claimed complete by this policy change.

The first fully classified subset is the 8,192
[extreme replacement-metadata observations](diagnostics/decimal-context-extreme-metadata/README.md).
Both operands violate the documented precision invariant in every row. Frozen
reference data and the original evaluator are retained outside the normal test
source sets; the public-JDK generator remains reproducible. The standalone input
classifier checks all rows and the retained data hash independently of the port.
This classification neither excludes valid extreme-scale inputs nor completes
the classification of other existing oracle families.

An [input-only inventory of the tracked context-dispatch corpus](diagnostics/decimal-context-dispatch/README.md)
also covers all 75,728 rows: 57,576 configure inconsistent sign/precision returns,
7,248 have throwing metadata callbacks without inconsistent returns, and 10,904
have consistent returned metadata. Other replacement callbacks prevent treating
the last category as certified contract-preserving subclasses. The fixture and
all current assertions remain unchanged; field-level normative classification
is still open.

New [ordinary and fully delegating context-division controls](docs/decimal-context-oracle-classification.md)
project all 75,728 input rows to 3,000 distinct numerical/topology/context cases
and 11,472 meaningful operand-class combinations. They compare values, scales,
canonical result text and exception types without poisoned getters or internal
trace requirements. Separate direct caller tests preserve override entry,
arguments and result/exception identity for null/self/distinct divisors and
finite/unlimited/null contexts.
The old observations remain intact; this addition does not finish their classification.

A frozen [field/control correspondence](diagnostics/decimal-context-dispatch/README.md#contract-coverage-correspondence)
maps every old row to these valid-input controls and preserves unresolved
poisoned interactions. Its mechanical checks do not authorize archival or
declare the remaining outcome fields unconstrained.
The [V7 precision-zero policy](docs/decimal-context-p0-policy-526.md) classifies
1,952 additional historical replacement-value observations after exact input
and control correspondence checks. All 75,728 original rows and assertions
remain intact; 73,392 outcome statuses remain unresolved and every archival
block remains. This metadata validation supplies no new mathematical test result.

For every new difference:

1. Preserve a minimal input, reference runtime/configuration, port result and
   reproducible command, including any preceding callbacks.
2. Locate the public contract and determine whether the input and overrides
   satisfy it. Use independent mathematics or compiler-checked Kotlin migration
   examples to establish the expected result.
3. Assign one of the categories above with evidence. A numerical mismatch on a
   valid input remains a port defect until the reference result is shown to
   violate the governing contract; lack of a specified internal detail is not
   proof of a bug.
4. For a proven reference defect, retain diagnostic oracle data, document the
   divergence and require a correct-behavior regression before removing any
   emulation. For observations or invalid overrides, do not silently delete
   evidence or exclude entire methods/subclass families.
5. Verify required behavior on the supported common targets and Kotlin call
   surfaces. Java call-shape adaptation belongs to the TCK harness and does not
   constrain the library to JDK-shaped classes or constructors.

The migration boundaries and release-to-release policy are stated in
[README.md](README.md#source-and-behavioral-compatibility-contract). JVM assignability
to `java.math`, its binary ABI and legacy/cross-format serialization are outside
requirements. Current krogu serialization round trips remain supported. Official
TCK/JCK has not been run; existing Java fixtures and differential suites are not
an official TCK result or certification.

Official kit scope, access barriers and the requirements for an authorized run
are documented in [TCK/JCK applicability](docs/tck-applicability.md).

## Requirements and current evidence

Campaign descriptions below preserve observations from the pinned runtime.
Statements about what an implementation "must" do to match those observations
do not establish additional public guarantees. Their normative classification
is governed by the policy above and remains work in kotgent `local:526`; numerical
and source guarantees themselves are not waived by that classification.

| Requirement | Evidence | Remaining audit |
|---|---|---|
| Kotlin numerical call surface, constants, operators and conversions of all four types | `ApiSurfaceTest`, the [finite call-form inventory](docs/kotlin-migration-525-callforms.md), existing conversion/operator/floating tranches and the [595/596 source260 local repair](docs/kotlin-callform-595-596-verification.md); task573's49 compiler jobs retain their narrower input-nullability scope | Original Kotlin/JS Number.toX dispatch retains its failing diagnostic; the accepted migration uses explicitly imported toKrogu* calls (see the Number workaround above). Final caller/compiler and supported/public evidence must bind the final composed artifact, not inherit source260 status. Declaration/language arguments and compiled examples keep their exact scopes; Java class/constructor shape is not a library requirement |
| Java source compatibility inside the TCK harness | `JavaApiContractTest`, explicit test adapters and the accepted [source255 public campaigns](docs/source255-public-math-verification-559.md):106 normal plus12 large baseline/port PASS IDs, covering all59 retained plans in both phases | Each later publication needs its own artifact acceptance; the12 declared adaptation losses remain explicit. No direct Java consumer source-compatibility promise or official JCK result |
| BigInteger signed arithmetic and exact results | Portable oracle; 396 signed pairs through 16,384-bit inputs and 32,768-bit products; carry/borrow and quotient arguments; seeded JVM arithmetic and modular tests; 10,156 string-constructor outcomes; 11,798 modular reduction/inverse outcomes; 9,176 modular-power outcomes; [transitively truthful modular substitution argument](docs/modular-substitution-546-proof-binding.md) | Kernel completion and supported-range/allocation548 remain premises; false/changing callback fields retain their separate classification, with no universal inherited trace guarantee |
| Infinite two's-complement bit semantics | Portable oracle, seeded JVM operations, algebraic invariants and the [signed-width/endpoint argument](docs/integer-bits-545-proof.md); 17,198 bit value/callback/identity outcomes and the current range ledger | Physical allocation/completion and truthful reached-leaf premises remain explicit; finite tests do not claim maximum multi-gigabyte execution or arbitrary changing-metadata outcomes |
| All constructor encodings and validation | Historical 25,584 byte and 10,156 string outcomes; BMP digit/radix corpus; conditional lexical/slice, byte and rounding arguments composed by the [current constructor/API binding](docs/api-constructor-549-current-binding.md); [588/589 text-constructor verification](docs/decimal-text-sqrt-587-589-verification.md); separate16-call scale-wording diagnostic | Precise malformed/compound-invalid and scale-wording questions remain under526/549; required isolated types, grammar, encoding and caller contracts are retained. Necessary resources keep the548 premises |
| Decimal rounding, precision, preferred scales, comparisons and text | Historical192,000 structured JVM comparisons,207,239 portable scale outcomes and75,728 context observations; [current39-entry overload binding](docs/decimal-overload-541-current-binding.md), product/division/sum arguments and [594 normalization verification](docs/decimal-division-normalization-594-verification.md) bind the later repairs | Required numeric/cohort/canonical/error/identity/caller fields remain distinct from internal diagnostic traces. Conditional reached-leaf/resource premises stay explicit; source255 hosted gates are accepted separately and260 needs its own matching gates. No arbitrary-subclass or fixed-heap theorem |
| Powers and roots | Historical exponent/rounding and midpoint fixtures; the [542 operational/accuracy scope](docs/decimal-pow-542-accuracy-scope.md), [587 root normalization](docs/decimal-text-sqrt-587-589-verification.md), integer-power/root arguments, [593 private working-coefficient repair](docs/decimal-power-593-verification.md) and complete94,282-record strict sqrt ledger retained through260; source255 matching hosted/public records above | Qualified working-scale admission542, truthful reached-leaf and physical resource premises remain; no universal sharp two-ulp theorem or dense giant execution is claimed. Matching260 hosted/public gates remain separate |
| Float/Double conversions and canonical factory values | Bitwise oracle comparisons, midpoint/canonical boundary fixtures, all binary scaling powers from -1074 through 1023, and the independently reviewed [current exhaustive-class composition](docs/floating-conversion-543-proof-binding.md) for integer/decimal rounding, exact Double construction and canonical Float/Double selection | Ordinary conversion mathematics is covered conditionally by that proof; maximum-domain allocations548, broader decimal subtype classification541 and final verification of the accepted explicit toKrogu* caller migration remain separate; original erased Number.toX compiler limitations are unchanged |
| Prime generation and certainty | 416 portable primality/successor/trace cases; bounded MAX_VALUE-certainty liveness test; conclusive thirteen-base domain; Carmichael/strong-pseudoprime regressions; [whole-domain conditional certainty and sampling argument](docs/primality-547-proof-binding.md); exhaustive small byte-payload distribution, primality and successor trial-division checks | Actual history-conditioned platform/caller randomness, truthful kernel and capacity completion548, and false/changing callback classification remain separate; no practical billion-round completion claim |
| Exception conditions and types | Historical portable/null/invalid fixtures, the [current API/error binding](docs/api-constructor-549-current-binding.md), public-entry ledgers and reviewed48-input non-null policy implemented by573–578 | The [oracle boundary](docs/oracle-contract-boundary-526.md) preserves required isolated types and specified diagnostics. V7's73,392 unresolved prefix questions and all archival payloads/assertions remain; they are not a waiver of a known required mismatch. Messages and competing-invalid ordering retain their precise scopes |
| Equality, ordering, hash codes, immutability | JVM oracle/common invariants, 6,110 portable integer hash outcomes, [context/mode](docs/context-mode-contract-549-ledger.md), current API/overload bindings and591's normalized public comparison units | Conditional reached-leaf obligations and documented identities remain; observation of one runtime's hash or unspecified object reuse is not a universal extra guarantee |
| Current-format JVM serialization; optional snapshot adapter evidence | Current-format round trips, validated base state, subclass state, reference identity, cycles and array-alias isolation; existing exact coefficient/scale adapters | Java adapters do not constrain the future library shape; cross-version serialization and java.math stream migration are outside scope |
| Same common behavior on every supported platform | Accepted [source255 hosted snapshot](docs/source255-hosted-verification.md):5,855 methods across13 target instances and25 separate oracle checks, with iOS device compilation separate; source260 has1,785 local methods across four targets | Every later publication needs its own complete supported-target inventory and actual artifact acceptance;260 hosted/public gates remain separate |
| Required numerical range | Integer/decimal kernel arguments, selected extreme-scale regressions, [590–592 boundary repairs](docs/decimal-range-590-592-verification.md), [593 private pow coefficients](docs/decimal-power-593-verification.md), [594 numerical normalization](docs/decimal-division-normalization-594-verification.md) and the [unified source/allocation ledger](docs/supported-range-allocation-548-ledger.md), with dense giant witnesses explicitly symbolic | Conditional kernels/reached leaves, necessary host resources and precise scale/parser questions remain explicit. Required final range, avoidable temporary overflow and physical allocation/time premises are distinct; no fixed-heap maximum theorem or arbitrary lower cap |

The remaining acceptance work includes final source-bound caller verification of
the accepted Number workaround, supported/public evidence for the final composed
artifact, and final dispositions of the source-bound541/542/548/549 audits under
the complete requirement audit553. The595/596 repairs and caller-side525/557
workaround have accepted dispositions; the original compiler failure remains
documented, rather than an outstanding demand for a transparent compiler repair. Their precise scale, resource
and compound-invalid research qualifiers remain explicit. Benchmarks, compact storage,
algorithm tuning and additional ergonomic APIs are separate follow-up work;
their absence alone does not establish a source or numerical-contract failure.

## Verification commands

```shell
./ktc-plugins sync
./ktc-plugins verify
kotlinr tools/configure-native-tests.main.kts
./kotlin test -m krogu-math --platform jvm '--include-classes=*'
kotlinr tools/run-node-tests.main.kts js
kotlinr tools/run-node-tests.main.kts wasm
./kotlin test -m krogu-math --platform macosArm64 '--include-classes=*'
export JAVA_TOOL_OPTIONS=-XX:-OmitStackTraceInFastThrow
kotlinr scripts/generate-oracle.main.kts --check
kotlinr scripts/generate-scale-oracle.main.kts --check
kotlinr scripts/generate-null-oracle.main.kts --check
kotlinr scripts/generate-character-oracle.main.kts --check
kotlinr scripts/generate-prime-oracle.main.kts --check
kotlinr scripts/generate-prime-prefix-oracle.main.kts --check
kotlinr scripts/generate-integer-arithmetic-oracle.main.kts --check
kotlinr scripts/generate-integer-parser-oracle.main.kts --check
kotlinr scripts/generate-integer-bytes-oracle.main.kts --check
kotlinr scripts/generate-integer-division-oracle.main.kts --check
kotlinr scripts/generate-integer-pow-oracle.main.kts --check
kotlinr scripts/generate-integer-gcd-oracle.main.kts --check
kotlinr scripts/generate-integer-sqrt-oracle.main.kts --check
kotlinr scripts/generate-integer-bits-oracle.main.kts --check
kotlinr scripts/generate-integer-inverse-oracle.main.kts --check
kotlinr scripts/generate-integer-modpow-oracle.main.kts --check
kotlinr scripts/generate-context-oracle.main.kts --check
kotlinr scripts/generate-hash-oracle.main.kts --check
```

The current build uses the pinned Kotlin Toolchain 0.13.0 wrapper and Kotlin
2.4.21. Keep `'--include-classes=*'` for full discovery, including existing
diagnostic classes whose names do not end in `Test`. Use the appropriate
`--platform` for other Native OS/architectures. Run these commands from the
repository root. The Native bootstrap generates an ignored template with this
checkout's absolute friend paths; regenerate it after moving the checkout or
pass a custom build directory as its sole argument when that layout changes.
The web-test friend paths require the default `build` directory. CI
cross-compiles Linux ARM64 tests on Linux x64 and executes them on ARM64. Device
iOS builds are compiled; simulator targets execute the shared behavioral suite.
The explicit JS/Wasm runner requires Node 24.16.0 and uses the Toolchain-linked
tests with locked Mocha tooling, preserving CI's Node coverage. The optional
Toolchain `test --platform wasmJs` runner uses Chromium/Playwright; it is not a
claim of Node execution. Historical Gradle reports retain their original
environments and are not measurements of this build migration.

`DecimalConformanceTest` defaults to 213,505 comparisons. Its canonical Double
campaign can be extended with
`./kotlin test -m krogu-math --platform jvm --include-test io.heapy.krogu.math.DecimalConformanceTest.doubleCanonicalizationCampaign --jvm-args=-Dkrogu.campaign.doubles=100000`.
Failure output includes reproducible public inputs. Passing a finite corpus must
not be promoted into a claim that all numerical or API contracts are satisfied.

Kotlin decimal operator division has six portable specification-led tests and
11,560 live JVM comparisons of actual Kotlin `/` expressions against stdlib.
The matrix covers signed compact and wide coefficients, differing/negative
scales, HALF_EVEN midpoint parity, zero divisors and source scale endpoints with
bounded coefficient rescaling. Values, scales and exception types are compared.
Separate checks keep named exact division distinct:
`BigDecimal("1").divide(BigDecimal("2"))` returns `0.5`, while exact division by
`BigDecimal("3")` throws for a nonterminating expansion. These tests cover the
operator contract, not the entire Kotlin migration surface.

`ScaleBoundaryOracleTest` compares all 207,239 outcomes individually on every
target. Its separate generator stores complete expected results in JVM-safe
string chunks, not hashes. `ScaleBoundaryConformanceTest` also regenerates each
reference result against the live pinned JDK. The corpus covers compact signed
coefficients, both scale endpoints and their neighbors, zero operands, exact and
context division, addition, multiplication, roots, powers, canonical parsing,
engineering text and trailing-zero removal. Its 68,000-case wide-coefficient
extension includes powers of ten and their immediate neighbors, up to 512-bit
coefficients, precisions around 17/18/19 digits, and very remote signed tails.

`NullArgumentConformanceTest` invokes public Java signatures by reflection so
null reaches the library, not a caller-side Kotlin cast. It compares all public
constructors and reference-argument methods over a bounded invalid-argument
matrix. `NullPrecedenceOracleTest` replays 3,462 cases for the affected contracts
on every target, including zero/negative square roots, invalid text, legacy
rounding constants, sign/magnitude validation and nullable contexts. Public
nullability is relaxed only where the oracle requires a particular validation
order or intentionally ignores a null argument; global parameter assertions
remain enabled.

`CharacterOracleTest` verifies all 2,293,760 BMP-character/valid-radix pairs,
131,072 single-character BigInteger/BigDecimal constructor outcomes, every
valid digit character in every integer radix, and all 390 supplementary digit
code points accepted by JDK 25 `Character.digit(codePoint, 36)`. The generator
checks the complete public-JDK results before losslessly encoding the BMP map
as 41 arithmetic ranges. It distinguishes constructor failures from failures
in test-result conversion. Supplementary UTF-16 constructor behavior is stored
explicitly, rather than inferred from code-point digit classification.

For positive certainty and absolute values strictly below
`3317044064679887385961981`, the first thirteen prime bases give a conclusive
Miller–Rabin decision ([Sorenson–Webster, Theorem 1.1](https://arxiv.org/html/1509.00864v1)).
This removes the billion-round `Int.MAX_VALUE`-certainty path in that domain
without weakening the result. Larger inputs retain the probability-driven
random-witness path. Its round-budget arithmetic covers all positive Int
certainties under fresh independent uniform witnesses; practical completion,
actual platform/caller randomness and supported-range kernels remain separate
obligations in the [current proof binding](docs/primality-547-proof-binding.md).
Non-positive
certainty retains the JDK's unconditional `true`. The portable oracle includes
strong pseudoprimes to initial basis subsets, both strict-boundary neighbors,
signed primes through 81 bits, and ordinary-strength 89/127-bit primes.
Prime generation now adds ceil(log2(bitLength)) certainty bits before computing
its random-witness budget, accounting for conditioning on candidate acceptance.
The density bound and explicit RNG assumptions are described in
[NUMERICAL_ARGUMENTS.md](NUMERICAL_ARGUMENTS.md#primality-and-prime-generation).
Successor search assigns 50+bitLength(i) rounds to its ith odd candidate;
the union bound over all candidates stays below the returned-value error limit.
The unbounded index uses BigInteger, and round budgets use Long. Both arguments
still rely on independent uniform witnesses. Portable successor fixtures cover
81..256-bit binary boundaries, the composite gap after 64!+1, source callback
traces and negative-start diagnostics; independent trial division checks every
start from 0 through 4096.

A separate 78-case primality-prefix oracle checks certainty endpoints and
stable callback paths before randomized witnesses. For positive odd values
other than one, virtual bitLength now runs after abs/equality/parity checks
and before the private small-prime shortcuts. A throwing getter therefore
propagates its error even for stored three, five or nine, matching JDK's exact
prefix and message. Non-positive certainty bypasses all callbacks; zero, one,
two and even values retain their earlier returns. In these negative-input
fixtures, delegated abs returns an ordinary base object, so the original
subclass getter is not called.
These cases do not assert a deterministic count for random witness callbacks
or establish behavior for replacement length values and compound overrides.

Binary constructors now pack bytes directly into limbs, including stripped
negative sign extension and a final complement carry. JVM checks compare all
65,536 two-byte encodings and seeded inputs through 65,536 bytes with JDK;
portable regressions include 131,072-byte sign padding and carry boundaries.
Checked sizing rejects magnitudes beyond the supported bit-length range and
avoids Int overflow in division allocation and the initial square-root shift.
Boundary arithmetic is tested without allocating the largest possible arrays;
actual multi-gigabyte execution remains unverified. Modular exponentiation has
a bounded-residue multiplication fallback so an oversized intermediate product
does not alone reject an otherwise representable result. Its arithmetic is
compared with full-precision JDK products on seeded operands through 512 bits.

MathContext string parsing uses a single bounded integer scan rather than
arbitrary-precision arithmetic and repeated parsing. The public-JDK oracle
checks every BMP code unit in single-digit, compound-digit and separator
positions (196,608 outcomes), 1,115 compound grammar/error-precedence cases,
and four precision tokens through one million characters. Common tests compare
the exact exception message and absence of a cause as well as successful
precision and rounding mode. Overflow/invalid syntax produces `bad string
format`; negative precision is rejected with `Digits < 0` only after a valid
rounding mode has been parsed. This closes those MathContext message mismatches;
the broader exception-message audit remains open.

Bit-query and bit-update routines work on stored limbs and two's-complement
words. Bit operations invoke virtual `bitLength()` to select a signed 32-bit-word
width; negative `bitCount()` invokes virtual `getLowestSetBit()`. They bypass
overridable arithmetic operations and neighboring public bit updates.
Black-box Java subclass fixtures reproduce
the former `andNot`/`setBit` failures and compare the corrected dispatch and
returned base-class type with JDK; common subclass fixtures run on every target.
The main oracle now contains 75,638 vectors, including 8,888 added signed
power/adjacent-value, dense-value, byte/30-bit-limb/32-bit-word boundary and
extreme-shift cases. Negative bit length/count are computed without constructing
`abs(n)-1`. `andNot` complements individual words directly, so it does not
require the potentially unrepresentable separate value `not(operand)`.
The largest supported magnitudes still require the range audit noted above;
these tests do not allocate multi-gigabyte integers or complete the broader
subclass-dispatch audit for arithmetic, shifts and conversions.

A separate 17,198-case public-JDK bit oracle compares all four binary operations,
complement, bit queries and bit updates, including source and operand callbacks,
throwing and replacement metadata, self aliases, zero-result identities and
signed power neighbors through 4,097-bit inputs. Each result is a fresh base
instance, including zero and unchanged updates. Negative bit addresses fail
before metadata callbacks. The published baseline recorded null operand failure
after the source bitLength callback in24 live Java comparisons. Task577's source
migrates the four binary operands and Kotlin overrides to non-null. The same24
Java rows retain their historical/live-host observations while requiring an actual
own NPE. Four ordinary, eight caller-owned direct override and four actual Java
super controls distinguish these entry routes. These tests passed in the accepted
local JVM gate; exact published-head CI remains open. Fake negative/huge bitLength values, changing callback
results and allocation-scale bit addresses still require a broader audit.
Six additional common cases cover updates at `Int.MAX_VALUE`: unchanged zero/-1
values still return fresh instances; unsupported results report `BigInteger would
overflow supported range`. The public-JDK experiment is reproducible with
`JAVA_OPTS=-Xmx1g kotlinr scripts/generate-integer-bits-oracle.main.kts --check-extreme`.
It needs that heap because JDK allocates large sign-extension arrays; the port
avoids those arrays for these endpoint cases. Allocation timing is not asserted.

The separate modular reduction/inverse oracle has 11,798 public-JDK outcomes.
It checks modulus validation before callbacks, virtual modulus equals(ONE),
conditional source mod/remainder calls, virtual reduced-value equals(ONE),
replaced/throwing results, self aliases and canonical ZERO/ONE shortcuts. Working
Euclidean values bypass source and modulus arithmetic/metadata overrides. The
non-invertible diagnostic now includes JDK's final period. Wide cases through
1,025-bit moduli additionally check residue bounds, the inverse congruence and
failure gcds. Java tests add 720 nullable/throwing callback comparisons and ten
null-modulus comparisons. The [current substitution ledger](docs/modular-substitution-546-proof-binding.md)
supplies a conditional argument for transitively truthful compound callbacks,
including returned subtypes and aliases. False/changing replacements remain
diagnostic classes; supported-range/allocation548 is a separate premise.

The modular-power oracle adds 9,176 exact outcomes, including callback arguments
and source/exponent/modulus replacement or throwing hooks. Zero exponents,
one/zero/minus-one bases and odd-modulus exponent-one results now retain JDK's
shortcuts and identities. Odd-modulus binary powering reads stored exponent bits;
the power-of-two path uses virtual exponent bit queries and the observed odd-base
period reduction. Even moduli use independently implemented CRT recombination.
Negative powers invert the computed residue, preserving a source inverse callback
when the odd exponent-one path returned the source. Normal inputs additionally
check residue bounds, full small powers and inverse-power congruences.
The negative replacement -7 from the exponent's negate callback is covered:
the odd path reads the magnitude bits, while the binary-modulus path retains its
public bit-query behavior. For base three, replacement -7 and modulus seventeen,
the result is fourteen rather than six.

Java tests add 5,040 nullable/throwing callback comparisons, 30 null-argument
precedence comparisons and seven stable primality parity-callback comparisons.
Primality witness callback counts can depend on JDK's random witnesses and are
not asserted as a fixed count. Factory checks establish stable cached values
from -16 through 16, public constant aliases and the negative-one equality
argument. Division remainders and two-bit generated primes retain fresh-instance
behavior despite the factory cache. The oracle generator also verifies these
observations directly against the pinned JDK.

The generator includes an expensive JDK exponent-one/false-equality case; it
eventually produces a result and is retained. Timing is not asserted. Broader
callback compositions, fake modulus parity/lowest-bit results, wide exponent and
modulus domains, and allocation behavior at the full supported range remain
unproven. The large-range numeric paths use bounded residue multiplication and
bounded CRT addition; their feasibility at multi-gigabyte sizes is not established.

Shift routines use private left/right helpers and a private lowest-set-bit
query, so negative distances and negative rounding do not dispatch through
overridden public methods. Java black-box fixtures compare both directions,
signs, zero, 30-bit boundaries and extreme distances with JDK. They also compare
whether the source object is retained and whether the result has the base type.
Zero shifts return canonical ZERO even for a zero-valued subclass; a zero
distance on a nonzero value retains the source. Common subclass regressions
include 120-bit powers and neighboring negative values to exercise discarded-bit
rounding, plus Int.MIN_VALUE/Int.MAX_VALUE distances. The existing structured
oracle independently supplies normal-value results at the extreme distances.
Broader dispatch behavior in arithmetic and conversions remains unverified.

Primitive conversion subclass fixtures compare actual Java call traces with
JDK across zero, signs, precision boundaries and overflow-to-infinity boundaries.
`intValue` reads the low magnitude directly instead of calling overridable
`longValue`. Inherited byte/short conversion still delegates to virtual intValue,
as JDK Number does. Nonzero magnitudes below 2^63 use virtual longValue for
Float/Double; zero and larger magnitudes do not. Larger conversions extract
the mantissa and guard/sticky information from private limbs without abs,
shiftRight, testBit, bitLength or getLowestSetBit overrides. The oracle adds 64
portable subclass cases with replacement Long values, including values just
above Float halfway points that would be double-rounded by a Long→Double→Float
path. Floating numerical-range proof and exhaustive Float-domain verification
remain open. Exact conversion dispatch evidence is described below.

Exact BigInteger conversions now reject oversized magnitudes before consulting
overridden bitLength: the threshold is 32 magnitude bits for int/short/byte
and 64 for long. Within those thresholds they consult bitLength and delegate
to the corresponding virtual primitive conversion. Short/byte exact methods
do not go through intValueExact. Java poison overrides reproduce the previous
failures. An additional 1,248 portable JDK cases cross 26 signed boundary values,
12 replacement bitLength results and all four exact methods; they compare
results, exact error messages, absence of causes and call traces. This includes
both sides of 2^32 and 2^64, so the early-rejection branches are exercised on
every target. The broad arithmetic/conversion subclass audit is still ongoing.

`NUMERICAL_ARGUMENTS.md` derives BigInteger guard/sticky rounding, carry,
finite/infinity thresholds, absence of a second Float rounding, and ordinary
signed exact-conversion range checks. The main oracle adds 1,728 Float/Double
outcomes immediately below, at and above binary midpoints, varying retained
parity, exponent and sign. This strengthens the integer conversion algorithm
audit without closing the BigDecimal/subnormal, exhaustive Float-domain,
compiler/runtime and practical largest-magnitude requirements.

BigInteger hashCode now repacks unsigned 32-bit magnitude chunks directly from
private 30-bit limbs. It does not call overridable abs/testBit and does not
construct intermediate BigIntegers. A Java poison-subclass fixture reproduces
the former failure. A separate portable JDK oracle compares 5,760 signed powers
and neighbors through two 480-bit alignment cycles, plus 350 dense-byte cases
through 65,536 bytes. The common hash fixtures use poison subclasses throughout
and independently encode structured powers as byte arrays. JVM seeded large
binary inputs also compare hash results with JDK. The packing argument is
recorded in `NUMERICAL_ARGUMENTS.md`; broader hash/equality and subclass behavior
for the other public types remains part of the audit.

## Completion gate

Every required row above must have direct, current evidence at the required scope
and no unresolved mismatch in the required contract. Proven reference defects
must have retained reproducers, an explicit violated contract and correct-behavior
regressions. Required API contracts need specification-led tests
and algorithmic reasoning in addition to random examples. Every discovered
mismatch in required common behavior must have a portable regression and pass
the complete target matrix. Reference observations, unspecified diagnostics and
contract-violating subclass inputs must be classified transparently rather than
silently disappearing from the audit. Specified diagnostics, current-format
serialization, range guarantees and other required behavior cannot be replaced
by a narrower claim.

Existing active whole-string oracle assertions remain active as stronger
reference regression checks. They do not alone define the mandatory public-contract set.
Preserved research questions and blocked archival are allowed dispositions;
they cannot hide a known required mismatch. The [oracle contract boundary](docs/oracle-contract-boundary-526.md)
records the controls, field/input-class dispositions and action triggers.

JVM serialization supports round trips of the current implementation, including
subclass state, shared references and cycles. Deserialization validates base-class
state and detaches integer limb arrays. Legacy proxies, older binary formats and
interchange or migration with `java.math` streams are not compatibility requirements.
This exception does not narrow mathematical API compatibility.

Decimal floating conversion now distinguishes compact no-dispatch paths from
general paths that call virtual signum once and bypass virtual precision.
The public-JDK matrix contributes 594 portable result-and-call-trace outcomes,
covering zero, both primitive mantissa boundaries, compact Long limits, scales
around 10/22 and extreme exponents. A separate seeded JVM matrix compares 25,959
ordinary Long-coefficient/scale combinations. BigInteger subclass coefficients
are normalized through virtual toByteArray at construction, as confirmed by a
public-JDK probe; common tests exercise all four coefficient constructors,
replacement encodings, invalid empty encoding and ordinary coefficient identity.
Other decimal virtual calls and compound invalid-argument precedence remain open.

A 735-case portable division matrix combines signed scale endpoints, small
divisors and near-Int.MAX_VALUE precision under HALF_UP, comparing results,
exception types and messages. It initially exposed 408 message differences. The
JVM matrix extends these inputs across all eight modes (5,880 comparisons), all
matching the pinned JDK. Context division now
checks normalization and intermediate range before preferred/result scale, and
uses the observed narrowed-Int sign for normalization error wording. All 735
cases match; the existing 207,239 scale-boundary comparisons also pass. The same
matrix is included in the portable oracle. This is evidence for this domain,
not a complete exception-message audit of every division overload or input domain.

An additional 1,176 exact-division cases initially exposed 523 message differences.
Exact division now reports the pinned JDK's complete nonterminating-result message
for arithmetic failures while constructing the exact result, including scale
failures, after checking division by zero. Zero divided by zero is distinguished
as Division undefined for exact, context and integral quotient families. These
cases and 96 zero-divisor cases across quotient/remainder/pair overloads are in
the portable corpus. Scale-bound division follows different primitive or
BigInteger zero-divisor error paths. A 210,912-case JVM campaign compares compact
boundaries, signed scale endpoints, power-of-ten boundaries and all eight rounding
modes. All cases match; 1,088 representative outcomes are in the portable corpus.
The error classifier preserves the observed rescaling order and Int wrap without
allocating an intermediate numeric value for an operation that must fail. Operand
subclasses and the full nonzero-divisor message domain remain
separate audits.

A public-JDK composition oracle adds 1,440 portable BigDecimal quotient/remainder
pair outcomes. Exact pairs call the exact virtual integral-quotient overload;
context pairs with zero precision delegate to the exact virtual pair overload.
Finite contexts call the context integral-quotient overload. The returned quotient
then receives virtual multiply before source subtract, and remainder overloads
select the second element of the corresponding virtual pair. Replacement and
throwing callbacks constrain this order, returned source/divisor/quotient identities,
values and scales. The matrix includes zero divisors whose quotient callbacks
replace division, both source signs and positive/negative preferred scales. It
isolates composition using overridden integral-quotient methods; null callback
inputs remain a separate audit.

An inherited integral-division oracle adds 1,400 portable outcomes for both operand
signs, zero divisors, smaller dividends, preferred scales, exact division and context
precisions 0, 1, 2 and 7. Public-JDK probes exposed a bypassed context-divide override:
source 10.50, divisor 2.0 and an override returning 7.9 formerly produced 5.0 here,
instead of the JDK's 7.0. Both integral overloads now call virtual context divide
with DOWN. Exact division preserves its observed metadata call order and guard-digit
budget; context division checks the residual when the returned scale is negative.
The oracle constrains callbacks on returned quotient subclasses, zero scale,
source aliases and fresh base results. Exact division only strips after truncating
a fractional result; finite-context stripping materializes a base value even when
the scale stays unchanged. Divide exceptions are propagated, including replacements
that bypass the ordinary zero-divisor failure. Poisoned metadata, nullable callback
results, all callback compositions, delegated context-divide traces and full-range
resource behavior remain unproven.

The integral oracle adds 1,176 signed metadata cases, combining 14 reported source
precisions, 14 divisor precisions and six scale pairs, including both Int endpoints.
The former positive-only ceiling expression caused 143 differences. It now rounds
the guard-digit term toward positive infinity for either sign. With equal input
scales, source precision 0 and divisor precision -1 reject the negative context
precision with `IllegalArgumentException: Digits < 0`; source precision 1 and divisor
precision -1 dispatch context divide with precision 0. The oracle also preserves
the observed upper precision cap and narrowing of negative Long budgets to Int;
it does not introduce a lower saturation absent from the pinned runtime. Call
order and failures match the JDK for this matrix. Changing getters, different
stored-scale comparisons and nullable callbacks still need separate audits.

A context-division dispatch oracle adds 6,656 portable outcomes across all eight
rounding modes, precisions 0, 1, 2 and 7, both signs, zero operands, compact and
wide coefficients, and throwing getters on both operands. It exposed 2,160
differences: source precision must be queried before divisor precision, and the
source signum check must not be repeated in the coefficient kernel. Cached context
metadata now preserves that order; the scale-bound and exact paths retain their
separate dispatch. Results, scales, call counts and exception precedence match
the pinned JDK for this matrix. A zero-precision context delegates directly to
virtual exact divide. Operand scale, unscaledValue, abs and negate overrides are
not invoked in the tested finite-context paths. Broader replacement getter domains
remain separate audits.

The context matrix now contains 8,000 outcomes: the original 6,656, another 768
with the same object used for both operands, and 576 nullable operand/context
cases. Shared signum counters and repeated precision calls follow the pinned JDK.
NullPointerException diagnostics generated by the VM/compiler are not compared;
their type and preceding callbacks are checked.

The context oracle now contains 25,040 outcomes. An additional 10,896 cases replace
signum with -1, 0 or 1, precision with -1, 0, 1, 2, 7 or 19, or use changing getter
answers. This extension exposed 1,917 differences: reported precision determines
the coefficient normalization and intermediate quotient scale, rather than only
the overflow check. Nonzero replacement signs select the nonzero path without
replacing the stored coefficient sign. Private rounding must not make further
virtual signum calls. Exact stored-zero quotients reduce their scale toward the
preferred scale; a stored zero divisor reporting nonzero can reach primitive or
integer division and its corresponding diagnostic.

Another 6,144 cases cover pairs of stored zeros reporting nonzero sign, with
reported precisions -1, 0, 1, 2, 17, 18, 19 and 20, context precisions 1, 17, 18
and 19, three scale pairs and all eight rounding modes. The pinned JDK's compact
equality path returns a unit ratio when source precision does not exceed divisor
precision and both operand/context precisions are below 18. Outside this tested
path, division by the stored zero fails. The port preserves this observable
subclass behavior. All 25,040 value, scale, trace and error outcomes match the
pinned JDK on JVM; wider getter values, compact overflow boundaries and other
subclass compositions remain unproven.

Another 50,688 outcomes near Long and compact digit boundaries bring the portable
context oracle to 75,728. They exposed 3,136 further differences, initially found
by a smaller 8,448-case probe with 192 differences. For example, Long.MAX_VALUE
divided by 3 with source precision reported as 0 and context precision 1 returns
zero in the pinned JDK's compact path. The port now preserves this result: when
compact normalization selected by reported precision overflows Long, this path
returns zero at the preferred scale, including UNNECESSARY rounding mode.
The guard checks multiplication by ten without overflowing or allocating large
powers. Ordinary precisions keep normalized compact values within range.

The independent `scripts/audit-decimal-context-compact-overflow.main.kts` live
comparison now reports zero differences across the same 50,688 outcomes.
It writes differences to `build/reports/compatibility/decimal-context-compact-overflow.txt`;
its `--check` mode fails if any recur. Build `jvmJar`, then run it with that JAR
on the Kotlin runner classpath as documented in its header. The finite matrix
does not prove every replacement Int value, allocation boundary or composition.
The complete 75,728-case oracle and the existing common suites pass locally on
JVM, JS, Wasm JS and macOS ARM64 Native. The full supported-platform CI result
for each published head remains a separate requirement.

JVM tests add 90 context-divide and 3,456 integral quotient/remainder/pair
comparisons with nullable operands, nullable contexts, source aliases, callback
replacements, null callback results, exceptions and inherited delegation. The
integral family exposed 300 differences caused by Kotlin entry checks before
virtual delegation. Division-family operand/context parameters are now nullable:
zero-precision contexts forward the operand to the exact override before operand
validation, and remainder forwards both arguments to the corresponding pair
override before validation. Base computations validate arguments when needed.
The Java method descriptors stay the same; Kotlin overrides must mirror the
nullable parameter types. All 3,546 live JVM comparisons match the pinned JDK.
The expanded JVM matrix includes empty and single-element pair arrays, null
arrays and null second elements. It checks exception types and preceding callbacks;
VM/compiler-generated NullPointerException messages are not compared.

A further 216 portable remainder cases cover replacement pair arrays of lengths
0 through 3, both overloads, source identity, nullable arguments and contexts.
Lengths 0 and 1 throw ArrayIndexOutOfBoundsException with the pinned JDK message
after invoking the pair override. This requires an explicit bounds check on JS,
where an unchecked array access otherwise returns undefined. Longer arrays return
the second element unchanged. Other replacement getter and callback combinations
remain unproven.

Scale-bound division adds 12,000 nonzero-divisor outcomes across all modes,
including returned values and exact messages. The initial wide-Long rescaling
rejected some values returned by the pinned JDK and accepted some calls it
rejects. The scale-bound overload now follows the observed checked scale stages
and Int raises, including wraparound. For example, coefficient one at scale
Int.MIN_VALUE divided by ONE with target scale zero returns ONE in the reference
runtime. The fixture preserves that observed endpoint behavior. Context division
uses the shared coefficient-rounding kernel with a separately checked Long raise,
so these scale-bound stages do not change context arithmetic. Existing scale,
context, exact-division and zero-divisor campaigns also pass. Operand subclasses,
wider coefficient domains and resource-limit behavior remain open.

SetScale adds 6,776 value-and-message comparisons spanning both scale endpoints,
18/19-digit rescaling boundaries, compact/inflated coefficients, zero and every
rounding mode. The initial 3,072 differences were diagnostic messages. Both
increase and decrease now validate a positive scale distance before the power-of-ten
range guard, matching the pinned Underflow and integer-range messages. The JVM
matrix and existing scale-boundary tests pass; the same cases are portable fixtures.

Decimal exact primitive conversion adds 560 value/message cases. The initial 315
message differences are resolved by long-range/fraction guards before rescaling,
followed by narrow-width checks. Another 72 public-call traces and 27 replacement
longValueExact cases constrain the legacy-Int setScale delegation and virtual
conversion guards. Canonical toString uses stored digit length and private plain
layout, with observed signum dispatch outside scale zero and the nonnegative
small-compact scale-two fast path. Its 264 getter-replacement cases compare text
and calls. Another 336 portable cases cover plain and engineering layouts,
including overridden signum/precision and whether engineering formatting calls
the public plain formatter. Another 150 portable cases compare two successive
calls after changing the subclass signum result, for all three formatters.
Canonical text retains the first successful result;
its cache is volatile and transient on JVM. JVM regressions compare repeated
calls with a changing subclass getter, current-format serialization after a warm
cache, and concurrent readers. Wider subclass callback domains remain open.

Power/root getter dispatch adds 504 portable cases across seven signed/zero/
fractional inputs, three precisions, all eight rounding modes and sqrt/exact pow/
context pow. Exact pow uses stored coefficient zero detection rather than virtual
signum; sqrt reads virtual signum once. The cases compare result text, exact
arithmetic error messages and signum/precision/scale callback traces. Inexact
square roots now retain the JDK message's terminating period. Replacement getter
values, additional overridable methods, exponent domains and resource limits
remain part of the broader power/root audit.

Another 1,320 portable square-root cases replace signum or precision results.
The zero/sign replacement domain spans scales -4 through 20 and all rounding
modes. A stored zero reporting a positive sign follows the nonzero context
dispatch, then returns the preferred zero scale; finite UNNECESSARY precision
also caps that scale at precision minus one. This corrects odd-scale and
high-precision zero layouts without changing the ordinary zero early return.
The 240 precision replacements also retain the pinned JDK's observable behavior
when the reported digit count differs from the stored coefficient's precision.

Exact square roots (unlimited precision or UNNECESSARY) normalize through the
public stripTrailingZeros method. Another 840 portable cases exercise ordinary
normalization and replacement values 9, 0, -4 and 0.04. They compare results,
exception messages and callback traces, including the absence of public
scaleByPowerOfTen/double conversion calls. The normalized value supplies the
coefficient and scale for exact root extraction; the original scale still supplies
the preferred result scale, padded only as far as finite precision allows.
stripTrailingZeros itself checks the stored coefficient for zero and bypasses
virtual signum. The broader replacement-method domain remains open.

Returned normalization subclasses add 1,344 portable cases. Exact-root handling
rejects an odd stored normalized scale before invoking coefficient getters. The
first virtual unscaledValue result selects the coefficient-one shortcut; otherwise
a second call supplies the integer root input. Cases vary the first result across
one, zero, signed nonsquares, ordinary values and a throwing callback, while
tracking the absence of virtual scale/signum/precision calls on the returned object.
Finite exact contexts validate the extracted integer root's digit count before
removing coefficient zeros, matching the pinned rejection of an unnormalized
4.00 replacement at precision one. Extreme normalized scales, additional returned
subclass methods and large-input resource behavior remain open.

A further 2,808 normalized-root cases cross nine coefficients with thirteen
scales (both Int endpoints and neighboring/ordinary values), three precisions
and all rounding modes. They distinguish the normalized zero's root scale from
the original preferred scale and cover trailing-zero integer roots at MIN_VALUE.
The final zero scale now uses the normalized root scale when applying finite
precision, then adjusts toward the original preferred scale. An extra check that
renormalized the extracted root before doubling its scale has been removed:
the reference accepts the unnormalized 100/400 coefficient replacements at
MIN_VALUE. Ordinary input normalization still validates its own scale. The matrix
uses bounded coefficients and matching source/replacement scales to avoid large
power-of-ten allocations; it does not establish resource behavior for distant
source/replacement scales.
For the rounded branches, source coefficient four has adjusted exponent -s;
the scaling shift is 2p-2 or 2p-1. With tested positive precisions one and seven,
the largest constructed decimal power is therefore 10^13, even at Int scale
endpoints. Exact branches use only the small supplied integer coefficients.

Exact decimal powers add 686 portable value/message/callback cases across seven
coefficients, seven scales, seven exponents and direct/unlimited-context overloads.
After exponent validation and the exponent-zero shortcut, scale multiplication is
validated before coefficient exponentiation. This corrects 16 cases where a
coefficient-range error masked the pinned Overflow/Underflow scale error, and
avoids coefficient growth for an invalid result scale. The same matrix confirms
zero-scale saturation and virtual pow delegation from the unlimited overload.
Another 45 direct BigInteger power cases constrain negative exponents, zero
exponents, zero and signed unit values, and supported-range failures. The power range
guard now reports `BigInteger would overflow supported range`, matching 24
additional decimal-message differences. Large exponents in this corpus either
retain a zero/unit coefficient or violate the bit-length lower bound before
allocation; it does not test feasible multi-gigabyte powers or certify the full
range/exception-message audit.

Point movement adds 3,402 portable cases across six coefficients (including zero,
both signs and an inflated value), seven scales, nine signed distances, three
replacement signs and all three movement methods. Scale overflow and zero
saturation inspect the stored coefficient, bypassing virtual signum/precision.
Zero-distance left/right movement preserves the source object only when its
scale is nonnegative; a negative scale is normalized to zero. scaleByPowerOfTen
still returns a base instance. Result text, exact errors, source identity, base
type and public call traces are compared with JDK, including MIN_VALUE distances
and the absence of delegation to neighboring public movement methods.
Selected negative result scales are either within four places of zero or beyond
the supported power-of-ten range, so the corpus avoids large successful power
allocations. Wider distances, caller-overridden result inspectors and practical
large-input allocation behavior remain open.

Multiplication adds 5,400 portable cases with replacement source getters, signed/
zero/inflated coefficients, both scale endpoints, direct and context overloads,
and HALF_EVEN/UNNECESSARY rounding. Scale validation checks the stored left
coefficient. Finite contexts use the private multiplication path; only unlimited
contexts delegate through public multiply. Exact errors, call traces, source
identity and result base type are included. A separate 1,152-case matrix compares
round and the BigInteger/scale/context constructor across all rounding modes,
carry-producing coefficients and scale neighbors. Rounding validates the reduced
scale before coefficient division and uses the sign of its narrowed Int for the
pinned overflow diagnostic, fixing 108 multiplication outcomes where Underflow
was replaced by Overflow or Rounding necessary. Public round getter replacements,
other constructor overloads, right-operand subclasses and larger products remain
open. Products here have at most 22 digits, keeping precision-reduction powers
small even at extreme scales.

Public round/plus dispatch adds 1,920 portable cases across zero, negative,
carry-producing and inflated values, five replacement precisions, three context
precisions and all rounding modes. round delegates to virtual plus even for an
unlimited context; plus performs the private rounding calculation. Replacement
plus results and callback exceptions propagate directly. Each rounding step
reads precision once, so a changing subclass getter cannot supply a second
value for the same reduction. Cases compare stored numerical state, identity,
base type, errors and call traces. These close the selected getter-replacement
domain noted above; extreme replacement
precisions, other unary operations and returned-subclass combinations remain open.

The historical null round/plus matrix contains 16 outcomes and a compiled Java
call-site test. It records the pinned JDK forwarding a null context to overridden
plus before base validation. Task576 migrates both Kotlin inputs and their
overrides to non-null MathContext. The displaced null calls now execute at real
Java boundaries: base, inherited and actual-super entries require own entry
NPE, while their original internal forwarding and VM-message observations
remain diagnostics. Explicit calls to caller-owned Java overrides retain their
language dispatch, replacement-result and exception obligations. The Kotlin plus(BigDecimal) operator is
JvmSynthetic so Java plus(null) resolves to the JDK-compatible MathContext
overload instead of failing as ambiguous. The cases compare NPE classes, callback
exception messages and traces; detailed NPE diagnostics remain unverified.

Unary operations add 600 portable outcomes: 432 result/identity/type/trace cases,
96 returned-negation callback cases across all rounding modes and null/ordinary
contexts, and 72 null-context callback cases also exercised from Java. abs(context)
reads virtual signum, then calls virtual negate(context) or plus(context) directly.
negate(context) calls plus(context) on the object returned by virtual negate(),
preserving replacement results and exceptions instead of invoking that object's
round override. The selected matrices exposed 72 dispatch differences in each
of these two chains in the historical comparison. Task576 makes the abs/negate
context inputs and migrated Kotlin overrides non-null as well. Valid non-null
dispatch controls remain common; the historical null-context outcomes are
retained at Java boundaries under the same base/inherited/actual-super entry-NPE
policy and diagnostic classification. Explicit caller-owned override calls
retain their dispatch, result and exception obligations. Broader returned-subclass combinations, extreme unary scales and
detailed NPE messages remain open.

Addition/subtraction routing adds 747 portable cases: 648 subtraction callback
cross-products, 27 exact-add sign replacements and 72 finite additions with scale
gaps through 20,002. Exact subtraction aligns stored coefficients directly,
bypassing source add and right-operand negate overrides. Unlimited context
subtraction delegates to public subtract; finite context subtraction preserves
virtual right negate followed by left add(context). Finite addition's ordinary
and remote-tail paths use private exact addition. Exact alignment checks stored
zero coefficients, so a replacement signum cannot suppress decimal scaling.
Replacement values, throwing callbacks, exact errors and call traces are compared
with JDK; the original subtraction matrix exposed 364 differences. The selected
JVM checks also rerun the numerical decimal, scale and null campaigns. Null
callback operands, broader getter replacement domains and full range/allocation
behavior remain open.

The frozen decimal min/max corpus contains 204 cases (120 custom-comparison and
84 base cases). The integer corpus contains 498 (168 custom-comparison and 330
base min/max/compareTo cases), including signed 30/63/64-bit boundaries and
Int.MIN_VALUE/Int.MAX_VALUE comparator answers. Their historical null forwarding,
selected-null results and callback-first exceptions remain frozen observations.

Task574 migrates the six comparison inputs and both Comparable type arguments
to non-null Kotlin types. Kotlin overrides migrate with them. Task525 separately
makes the four member min/max results non-null after a paired compiler witness.
Valid extrema still use virtual
comparison and retain their tie identities: decimal selects the receiver and
integer selects the argument. Ordinary ordering and mathematical assertions stay
in common tests, including distinct-equal ordinary/truthful subclass controls.

Exactly 132 decimal and 384 integer frozen rows remain executable in common;
72 decimal and 114 integer invalid-null rows move to actual Java library calls.
The shared input-only ledger checks every six-field key and global source ordinal.
The JVM counterpart requires library NPE while retaining the full frozen outcome,
actual callback trace and VM message as diagnostics. Existing Java subclass
matrices retain all 144 valid assertions and classify their 144 null rows
separately. Direct Java overrides, virtual reflection, inherited/base boundaries
and all six explicit Java super calls have distinct controls. Reflection does not
stand in for explicit super. Java sorting and erased bridge checks are retained.

The adopted revision4 passed the four local gates and 49 compiler jobs recorded
in the [current verification report](docs/non-null-api-573-verification.md).
The [task574 handoff and exact row ledger](docs/non-null-574/SOURCE-HANDOFF.md)
retain their historical source milestone and original row provenance.
Detailed VM messages, unspecified internal callback order and broader callback
compositions do not become stronger host-parity promises.

The initial quotient magnitude comparison now reads stored limbs directly rather
than invoking public compareTo. An overridden comparison no longer changes
division or leaks a callback into integer formatting. Another 318 portable JDK
outcomes cover divide/remainder/pairs with both divisor signs and one-/multi-limb
magnitudes, plus every radix 2..36 for signed/zero inputs, while source compareTo
throws if called. These cases also constrain formatting of a selected min/max
source. Broader combinations with formatting overrides remain a separate audit.

Division adds 3,216 portable callback/value/identity cases through 8,192-bit
dividends, including both operand signs, zero divisors, one-/multi-limb divisors
and self aliases. Public divide and remainder now enter a private implementation
rather than virtual divideAndRemainder. Stored magnitude comparisons and direct
bit reads prevent operand abs, negate, signum, bitLength, compareTo, testBit and
arithmetic overrides from changing inherited division. The multi-limb reduction
uses a base-class divisor view. Nonzero remainders are normalized into base values,
including the smaller-dividend path, preserving the observed result type and
identity. Small-remainder cases cross both signs, quotient parity and 29/30/90-bit
divisors with remainders 0, 1, 2 and 10; a final add of ONE must not leak that
constant as the returned remainder. Callback modes include delegation, replacements,
exceptions and empty pair arrays. The pair cases explicitly enter the inherited base implementation
via super; a user's direct call to its own override still dispatches normally.
Another 294 live Java cases cover null/throw callback returns, null operands and
self aliases. Detailed null diagnostics, wider identity combinations and extreme
resource limits remain open.

Integer powers add 6,677 portable outcomes over exponent endpoints and compact
boundaries, both signs, unit/zero identities and individual callback replacements
or exceptions. Negative/zero exponents precede all callbacks; positive exponents
first consult virtual equals(ONE), then stored zero and exponent-one paths.
General powers extract factors of two through virtual abs, lowest-set-bit and
shiftRight calls, then consult the shifted value's bitLength. Working products
use base-class values, bypassing the source multiply override. The observed
compact path reads the leading unsigned 32-bit magnitude word, uses unsigned
64-bit arithmetic and creates a separate result object, including wrapped zero.
False metadata can expose masked shifts, a period-64 leading-word-two path, or an
empty-magnitude array exception; the JVM exception is the Java array-index subtype.
Another 336 Java cases cover null/throw abs, negate and shiftRight callbacks and
their precedence. Wider callback compositions, cached identities beyond public
constants and near-allocation-limit powers remain open.

Integer GCD adds 1,602 portable callback/value/identity outcomes, including zero
in either position, both zero, both signs and self aliases. A stored zero operand
delegates only to source abs; otherwise a stored zero source delegates only to
operand abs. These paths forward replacement values and identities without
normalization. Two nonzero stored magnitudes use base-class working views and
private division, bypassing source/operand abs, negate, remainder, pair, equality,
comparison and bit metadata overrides. The result is a separate positive base
value. Wide cases include shared 1,024-bit factors, a 4,096-bit dividend and
consecutive Fibonacci numbers after 750 recurrence steps. Another 168 live Java
cases verify null/throw abs and negate callbacks, null arguments and self aliases.
Wider callback graphs, detailed NPE messages and resource limits remain open.

Integer square roots add 978 portable value/callback/identity outcomes across
both APIs. The inherited implementations use a private common root routine;
they bypass source comparison, bit metadata, arithmetic and the other public
root overload. Zero roots and zero remainders are canonical ZERO; nonzero roots
and remainders are separate base-class values. Probes enter each inherited API
through super, including throwing/replacement overrides and empty pair arrays.
Direct calls to user overrides still dispatch normally. Numerical cases cover
small square neighbors and roots around powers of two through exponent 4,096
(inputs through 8,193 bits). Every successful case checks the floor-root
inequalities; pairs also check shape, reconstruction and remainder bounds.
Another 180 Java null/throw callback cases constrain inherited sqrt/pair, divide,
multiply and subtract independence. Larger input/allocation domains and decimal
root subclass compositions remain open.

Integer subtraction adds 896 portable dispatch cases. A stored zero operand
returns the source without callbacks; only a stored zero source and nonzero
operand delegate to operand.negate. Other paths use stored signed magnitudes,
bypassing source.add and public negation. Fixtures cover both signs, zero,
30-bit boundaries and larger values, distinct and self operands, callback
replacement objects and exceptions, result identity and call order. Observation
captures the trace before formatting and uses known values for returned source
or operand objects, so unrelated formatting overrides do not alter the measured
subtraction trace. Another 196 Java null/throw combinations and three null-operand
checks constrain direct null forwarding and argument rejection. The existing
396 wide ordinary arithmetic pairs remain the numerical regression domain;
broader callback compositions, range failures and detailed NPE messages remain open.

String constructors add 10,156 portable value and exact-message outcomes from
the pinned JDK. Invalid radix precedes empty text; embedded ASCII signs precede
digit validation. Leading zero digits, including Unicode zero digits, are removed
before diagnostic grouping. Invalid digits identify the corresponding group,
whose radix power fits a positive Int; radix ten omits the radix suffix. The
independent per-digit arithmetic does not use these groups. Fixtures cover every
radix 2..36, invalid radix endpoints, both constructor overloads at radix ten,
Unicode and malformed UTF-16, whitespace/control characters, and invalid digits
around one-, two- and three-group boundaries. Exhaustive BMP digit acceptance
remains covered separately; arbitrary-length resource limits are still open.

The [constructor coverage map and conditional lexical argument](docs/constructor-contract-549-coverage.md)
distinguish existing common/JVM cases, grammar and scale correctness, the four
adapted public char-range methods and their lost joint-invalid coverage.
The diagnostic ordering described here records the pinned observations; it
does not establish a universal exception-priority requirement. Broader task549
call-site and exception classification and task548 range obligations remain open.

Byte constructors add 25,584 portable outcomes across all four overloads, with
987 distinct pinned JDK values or exact diagnostics. The matrix crosses signum
endpoints and offset/length endpoints, including Int overflow combinations, over
empty arrays, zero/sign extension, high-bit inputs and 30-bit limb boundaries.
Empty two's-complement input precedes slice validation; invalid signum precedes
signed-magnitude slice validation. Invalid slices now include the original offset,
length and array size in the diagnostic without overflowing offset plus length.
Every successful case additionally mutates both the input array and the exported
byte array, then verifies that the number's value and hash remain unchanged.
Null precedence remains covered separately; near-allocation-limit byte arrays
are outside this finite regression domain.

Exact alignment errors add 882 portable add/subtract outcomes over signed unit
and zero coefficients and seven scales spanning both Int endpoints. The matrix
exposed 384 diagnostic differences. A nonzero coefficient's positive alignment
distance is validated as an Int before the power-of-ten range check; oversized
distances report Underflow. The positive power range guard now uses the JDK
message `BigInteger would overflow supported range`. Zero coefficients bypass
unnecessary scaling. Selected distances are small or far beyond supported range,
so this campaign does not allocate near-limit integers. The broader range and
exception-message audit remains open.

Nullable binary delegation adds 288 portable outcomes and a compiled Java matrix
for add/subtract/multiply, direct and context overloads, null/ordinary operands,
null/unlimited/finite contexts and four exact-callback behaviors. Unlimited
contexts forward the operand to the public exact override before operand
validation. Finite contexts validate before private calculation. Context-null
controls reject without invoking the exact callback. Binary operand and context
parameters now use nullable Kotlin types; subclass overrides use them too.
Base numeric behavior, callback replacements, callback exception messages and
traces are compared. NPE classes are checked separately from their still-open
detailed diagnostic audit. More callback compositions remain open.

Java returned-null callback checks add 94 cases to the three null-context/operand
matrices (470 JVM cases total). The observer uses String.valueOf so a returned
null is distinguished from an NPE thrown by the library. The added domain covers
58 forwarded null results and 36 controls where null/finite contexts prevent
exact-callback delegation. Round/plus, abs/negate and unlimited binary overloads
match the pinned JDK in these Java-only interop cases without another arithmetic
change. Other runtime interop surfaces, chained returned-null callbacks and
detailed NPE diagnostics remain open.

Comparison adds 8,215 portable outcomes: 7,425 getter/sign combinations, 225
ordinary text pairs, 400 extreme-scale pairs and 165 self comparisons with
changing sign getters. Same-scale compact operands compare stored coefficients
without getters. General paths read each virtual sign once; stored-zero and
same-scale magnitude paths bypass precision, while different-scale nonzero
magnitudes read virtual precision. Scale getters are bypassed. Magnitude string
comparison now normalizes its result to -1/0/1, correcting ordinary results such
as 1 versus 9.0 returning -8 instead of the pinned -1. Sign direction uses positive
versus negative branching, including the selected replacement signs +/-2.
Long boundaries and adjacent inflated values constrain compact-path selection.
The numerical order argument assumes truthful getter metadata; broader dynamic
getter domains, null comparison/min/max callbacks and large-input allocation
behavior remain open.

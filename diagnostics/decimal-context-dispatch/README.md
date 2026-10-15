# Context-division observation inventory

This is an input-only inventory for `local:526`, covering all **75,728** rows of
the tracked context-dispatch corpus. The existing fixture, evaluator, generator
and assertions remain unchanged. This inventory does not classify every result
field or make the whole corpus a normative mathematical test.

Rows contain source/divisor decimal text, source/divisor probe mode, context
precision, rounding mode and a reference outcome with its callback trace.
Negative context precision supplies a **null MathContext**, not exact division;
zero supplies an unlimited context. A `self` divisor aliases the source including
its override mode; a `null` divisor has no metadata to classify.

The script derives sign and unscaled coefficient digit count directly from ASCII
decimal text. It preserves coefficient trailing zeros and gives zero precision
one. It classifies configured getter returns independently of whether the JDK
happened to call that getter. Changing precision always violates the invariant;
changing sign is inconsistent only for nonzero values (`-0 == 0`). Throwing
getter callbacks are tracked separately from incorrect returned metadata.

| Disjoint row category | Rows |
|---|---:|
| At least one operand configured to return inconsistent sign/precision | 57,576 |
| No inconsistent return, but a sign/precision callback can throw | 7,248 |
| Sign/precision returns consistent, without these throwing callbacks | 10,904 |
| Total | 75,728 |

These last 10,904 rows are **not certified contract-preserving subclasses**.
Every probe also overrides `scale`, `unscaledValue`, `abs` and `negate` to throw,
and exact `divide` to return the replacement `7.00`. Numerical guarantees,
explicit language dispatch, permitted callback propagation, internal callback
order and diagnostic wording still need field-level classification. An unused
bad getter alone does not identify which outcome fields are affected. Nothing
here excludes valid ordinary or truthful-subclass mathematical behavior.

Historical SHA-256 evidence bound to the original inventory:

| File | SHA-256 |
|---|---|
| `Jdk25DecimalContextDispatchResults.kt` | `88fc3c5bc8277a048a83a9ad9b6546bd8a23ae1ad9a10126409d857367391026` |
| `DecimalContextDispatchOracleTest.kt` | `2ed00a5dd3c7ec038e61f5be078697b84609606549c26b1bda1249fce860c781` |
| `generate-decimal-context-dispatch-oracle.main.kts` | `0d9e9b11af4c27892758e78d6b80639b763fcec66ffbe27721d373864a501188` |

The original inventory command, from the repository root with Kotlin 2.4.21, was:

```sh
kotlin scripts/inventory-decimal-context-dispatch.main.kts --check
```

That inventory retains the historical evaluator pin; it is not a current-source
verification command after the 575 migration. Its check verifies the three
historical hashes, row shape, configured-mode coverage and aggregate counts.
It evaluates no JDK or krogu numerical implementation and
does not delete, move or rewrite the observations. Other oracle families and the
full normative classification remain open.

## Contract coverage correspondence

[`contract-coverage.tsv`](contract-coverage.tsv) maps every frozen observation
to the ordinary/truthful control with the same source text, divisor text,
divisor topology, context precision and rounding mode. `D00001` through
`D75728` identify the one-based frozen rows; `C0001` through `C3000` identify
the distinct five-field keys in first-occurrence order. No metadata mode or
old result becomes a new mathematical expectation. The accepted v6 mapper verifies the
same dispatch fixture and generator hashes, the current migrated evaluator
`bb61d3f8a5f61850fb208f0e8f5ba814c4b3c7c68b0cb658a0db62081ea41300`
and the control fixture SHA-256
`1245d75ed6837139c939496e602a085cba37b35dd4d3c98f197bdaece2ce7cc9`.

Every old row has exactly one control correspondence, and every control has
at least one old row. The 2,736 ordinary pairs have four meaningful base/subclass
class masks each; 192 self aliases and 72 null-divisor inputs have two each.
Together they correspond to **11,472 valid-input checks**. Control outcomes
contain 2,475 coefficient/scale values, 429 arithmetic errors and 96 null errors.
For successful controls, the map separately records the required canonical-text
control derived independently from that expected coefficient/scale. Canonical
`BigDecimal.toString()` spelling is specified; it is not diagnostic wording.

The manifest is a **coverage correspondence, not a passed-test certificate**.
Its value/error expectations come from the existing new fixture. The 9,580
successful class-mask controls now include canonical text, and the 24 expanded
explicit-caller cases are implemented and reviewed. Their separate focused
JVM/JS/WasmJS/macOS ARM64 run passed in 3m44s: ten methods per target including
the retained dispatch corpus, with zero failures/errors/skips. Details are in
[the contract report](../../docs/decimal-context-oracle-classification.md).
No test implementation, math implementation or JDK arithmetic is executed by
the mapper itself.

Current schema `context-contract-coverage-v6` stores only six columns:
`old_row_id`, `control_id`, `source_mode_id`, `divisor_mode_id`, `old_kind` and
`interaction_bits_hex`. The header's `MODE` dictionary recovers each original
probe mode. `V` and `E` recover `VALUE` and `ERROR`. The three-digit lowercase
hexadecimal mask combines the header's `FLAG` bits. The input/outcome dictionary
is the hash-bound control fixture itself, indexed by the one-based `C` ID.
384 bounded `STATUS` header records classify exactly eight required error-type
statuses,96 reference-only value prefixes and280 reference historical error
prefixes for exact D/C/mode/kind/mask bindings;
the six-column data stays byte-identical.
Together these recover **all 19 logical fields** of the original expanded map,
without repeating fixture data or constant policy strings per row:

| Logical field(s) | Lossless recovery |
|---|---|
| `old_row_id`, `control_id` | Stored directly; IDs retain the original stable `D`/`C` numbering |
| `source`, `divisor`, `topology`, `context_precision`, `rounding` | First five fields of the `C` row in the bound control fixture |
| `source_mode`, `divisor_mode` | Header `MODE` entries selected by the two stored mode IDs |
| `required_valid_outcome` | Sixth field of the `C` control row |
| `required_canonical_text_control` | `CANONICAL_TEXT:C-id` for a successful control; `NOT_APPLICABLE` for an error control |
| `old_outcome_kind` | Stored `V` → `VALUE`; `E` → `ERROR` |
| `old_value_or_error_status` | Exact eight ERROR bindings → `REQUIRED_NULL_ARGUMENT_CONTRACT`; exact96 unlimited/null VALUE bindings → `REFERENCE_CONTRACT_BREAKING_EXACT_OVERRIDE_RESULT`; exact280 historical-null ERROR bindings → `REFERENCE_HISTORICAL_NULL_INPUT_ERROR`; otherwise `UNRESOLVED_POISONED_INTERACTION` |
| `old_trace_status` | Constant `REFERENCE_OBSERVATION` |
| `old_message_status` | `REFERENCE_OBSERVATION` when bit `200` is set; otherwise `NOT_RECORDED` |
| `runtime_identity_status` | Constant `NOT_OBSERVED` |
| `unresolved_interactions` | Ordered names of set `FLAG` bits except `MESSAGE_RECORDED` |
| `explicit_caller_language_control` | Constant `EXPLICIT_DIVIDE_CALLER_MATRIX` |
| `archival_status` | Constant `BLOCKED_UNRESOLVED_FIELDS` |

| Hexadecimal flag | Decoded evidence |
|---|---|
| `001` | `POISONED_GETTERS_AND_EXACT_OVERRIDE` (present in every row) |
| `002` | `INCONSISTENT_METADATA` |
| `004` | `THROWING_METADATA` |
| `008` | `NULL_CONTEXT` |
| `010` | `NULL_DIVISOR` |
| `020` | `SELF_ALIAS_COUNTERS` |
| `040` | `INVOKED_EXACT_REPLACEMENT` |
| `080` | `INJECTED_CALLBACK_FAILURE` |
| `100` | `ARITHMETIC_CALLBACK_INTERACTION` |
| `200` | `MESSAGE_RECORDED` (message-status evidence, not an unresolved tag) |

| Manifest field | Meaning and boundary |
|---|---|
| `required_valid_outcome` | Required coefficient/scale or error-type control on ordinary/fully truthful operands |
| `required_canonical_text_control` | Required canonical result spelling for the corresponding successful control; not an expected poisoned result |
| `old_value_or_error_status` | Eight exact null-context error types are required;96 exact replacement value prefixes and280 historical-null error prefixes are reference observations;75,344 other statuses remain unresolved; current575 NN rejection remains separately required |
| `old_trace_status` | `REFERENCE_OBSERVATION`; internal order, counts, noncalls and exact-override forwarding are not established requirements |
| `old_message_status` | `REFERENCE_OBSERVATION` when a diagnostic message was recorded; otherwise `NOT_RECORDED` |
| `runtime_identity_status` | `NOT_OBSERVED`; this evaluator measures neither result identity nor runtime class/cache choices |
| `unresolved_interactions` | Row-specific metadata, null, self-alias, invoked replacement and failure-path tags, in addition to the latent poisoned callbacks |
| `explicit_caller_language_control` | Direct base-typed exact/context override calls, original null/self arguments and contexts, caller-owned replacement/exception identity; no inherited-forwarding promise |
| `archival_status` | `BLOCKED_UNRESOLVED_FIELDS` for every old row; this map authorizes no removal of old assertions |

The explicit caller matrix covers distinct/self/null divisor arguments
and finite/unlimited/null context arguments. It extends
`explicitBaseTypedCallerEntersOverridesAndPropagatesTheirException` in
`DecimalContextContractTest`. These are language controls for a caller entering
its override, including an override whose mathematical return is deliberately
replaced. They do not require inherited context division to invoke that override
internally. The ordinary/truthful partitions separately cover the documented
null and self arithmetic.

57,997 old value prefixes and17,347 old error-type statuses remain unresolved;
eight required null-context error types,96 reference-only value prefixes and280
reference historical-null error prefixes are
classified below. This does not declare other fields unconstrained:
it keeps their possible required fields open while transferring valid-input
coverage. In particular, a configured but unused bad getter is not a waiver of
an inherited method's independent contract. Null/throw/arithmetic precedence and
self-alias callback counters remain explicit investigation topics. The existing
fixture, evaluator, generator and assertions stay unchanged.

### Historical v3 milestone: eight exact null-context exception types

Only D07425–D07432, respectively C0609–C0616 in UP/DOWN/CEILING/FLOOR/HALF_UP/
HALF_DOWN/HALF_EVEN/UNNECESSARY order, receive `REQUIRED_NULL_ARGUMENT_CONTRACT`.
Their exact source/divisor are `0.00`/`2.0`, ordinary topology, both mode IDs
M01 (`delegate`), context precision−1 (null), old kind E and mask009. Selection
uses these inputs and stable IDs, never the recorded outcome. The complete
frozen `!NullPointerException|` observation remains asserted as provenance.

The required field is exception TYPE only, under the public null-object-argument
contract for the caller's direct `super.divide(divisor,null)` entry. Current
source rejects null context before metadata/exact calls; this explains the
observation without making no-callback/trace ordering a requirement. Trace stays
`REFERENCE_OBSERVATION`, identity `NOT_OBSERVED`, and latent poisoned-callback/
null-context tags and `BLOCKED_UNRESOLVED_FIELDS` remain. At that v3 milestone,
D07457/C0625 and every other row were untouched. No assertion, fixture or production change or archival
permission follows. Independent review accepted the isolated generation/check
and byte-comparison evidence: all75,729 noncomment lines stayed identical,
and only the eight logical error-type statuses changed. The original v2 data,
expected first-check failure and exact new metadata are retained in
`/private/tmp/krogu-526-null8-validation-v1/`; see the
[integration report](../../docs/context-modular-integration-verification.md).

From the repository root, with Kotlin 2.4.21:

```sh
JAVA_OPTS='-Xmx512m -Dkotlin.main.kts.compiled.scripts.cache.dir=/private/tmp/krogu-contract526-map-cache' \
/Users/yoda/.sdkman/candidates/kotlin/2.4.21/bin/kotlin \
scripts/map-decimal-context-contract-coverage.main.kts --check
```

Without `--check`, the script regenerates only the TSV manifest. With `--check`,
it reproduces the manifest byte-for-byte and checks bound hashes, both fixture
schemas, all row/control counts, first-occurrence control order, complete key
coverage and the metadata-inventory totals. It also decodes every saved compact
row, verifies all 19 logical fields against the expanded independently computed
record, and rechecks its key and original modes against the frozen row. It does
not establish further required outcome fields beyond the eight exact null-context error types,
classify other oracle families, or complete
`local:526`.

## Single-row field analysis: D07457 / C0625

For the exact binding `D07457 C0625 M01 M06 V 051`, the frozen prefix
`7.00:2` is a reference observation of the historical exact override accepting
null and returning its replacement, contrary to public null rejection. It is
not a required numerical outcome of inherited context division. The truthful
control C0625 still requires NPE. The row explicitly enters
`super.divide(null, unlimitedContext)`; language dispatch guarantees the selected
explicit entry, not a later internal exact-override call. Separate direct
caller-owned override controls govern their own entry/result/exception identity;
this row observes no result identity. Its trace remains `REFERENCE_OBSERVATION`,
message `NOT_RECORDED`, identity `NOT_OBSERVED`, and archival blocked. This
one-row conclusion does not classify neighboring rows, waive inherited contracts,
declare a JDK bug or invalidate truthful subclasses. Under the current non-null
policy, old ordinal 7456 executes through the Java explicit-super adapter and
requires NPE in `DecimalBinary575JvmTest`; C0625/ordinal 624 has the separate
ordinary/truthful JVM counterpart. The frozen observations remain, while permitted
Kotlin nullability edits do not require accepting null in current common code.
See the [one-row analysis](../../docs/decimal-context-oracle-classification.md#null-arguments-and-an-unlimited-context-replacement).

At the original source-only field-analysis milestone, the v3 dictionary
recognized only eight exact error bindings, and its validator required that
exact list. D07457 had no v3 status override to edit. Its machine status remained
`UNRESOLVED_POISONED_INTERACTION` pending a separately scoped executable-policy
update; that human field analysis did not invent a ninth dictionary
entry or change the TSV. Counts then stayed 58,093 unresolved value statuses and 17,627
unresolved error statuses (75,720 total), eight required error-type statuses,
75,728 archival blocks. Frozen D07457/C0625 data, mapper, evaluator and assertions
were unchanged by that source-only documentation update; no script, compiler,
helper, numerical operation, probe or test was executed in it. The evaluator hash above
binds its historical nullable source; the migrated common evaluator is now
`bb61d3f8a5f61850fb208f0e8f5ba814c4b3c7c68b0cb658a0db62081ea41300`.
No historical inventory/mapper check was rerun or claimed current here.

## Historical source-only v4 preparation

The separately scoped source-only mapper preparation permitted exactly the
eight existing ERROR bindings plus `D07457 C0625 M01 M06 V 051`. Only that one
old value prefix gets `REFERENCE_CONTRACT_BREAKING_EXACT_OVERRIDE_RESULT`;
C0625's required NPE, the frozen full old row/control, historical probe source,
all other logical selectors/flags and every archival block are preserved.
It validates full input/outcome/control pins and the exact ninth compact binding,
not a general unsupported-probe rule. Its existing payload guard refuses any
six-column header/data change.

Expected v4 counts, then conditional on later generation and metadata checking, were
eight required error-type statuses, one reference-only value prefix and 75,719
unresolved statuses (58,092 value / 17,627 error). The materialized TSV was then
v3, with 75,720 unresolved statuses. The prepared mapper
verifies the migrated evaluator `bb61d3f8…` separately from historical
`2ed00a5d…` provenance; fixtures, generator, evaluators and assertions are not
edited. The new mapper was UNCOMPILED/UNRUN at that source milestone, and no
manifest generation or check occurred in that preparation. See the
[source preparation record](../../docs/decimal-context-value1-policy-526.md)
for full pins and preservation. A check against preserved v3 would differ from
v4; no such pre-generation check was claimed.

## Root2300: accepted actual v4 metadata validation

Parent generation session 93225 and check session 39678 exited 0 with frozen
mapper `78869800…`, all four pinned source files, a 512MiB script heap and fresh
isolated compiled-script cache. Independent read-only actual review accepted
the evidence. All 75,728 compact records and the six-column header stayed
byte-identical. The v4 TSV adopted at that milestone had SHA-256
`f8f797e79d706fbed221e3ec73f77778f1f952a938d071e972983ffb45098927`.
Exactly nine bindings recover eight required error types and one reference-only
value prefix, leaving 75,719 unresolved statuses and all 75,728 archival blocks.

Raw logs and full pins are recorded in the
[actual validation section](../../docs/decimal-context-value1-policy-526.md#root2300-actual-metadata-validation).
The preserved v3 artifact and readonly predecessor remain. This metadata run
reran no mathematical test or old public-JDK oracle and changed no fixture,
evaluator or assertion. Adoption was not yet committed/pushed at this handoff;
root owns CI and publication.

## Historical source-only v5 class preparation

The separately scoped mapper source constructed the complete finite
null-divisor/unlimited-context replacement class: three source texts
(`0.00`, `10.50`, `-10.50`) × four source modes (`delegate`, `sign-first`,
`sign-second`, `precision`) × all eight rounding modes, with divisor text `2.0`,
divisor mode `null` and context precision `0`. This is **96 total VALUE
bindings**, including D07457: 95 additional classifications, not 96 additions.
Input construction selects the class; complete frozen outcome/control records
and exact compact kinds/masks are then checked, never used as selectors.

The 24 ordinary/truthful controls still require NPE. Only these old replacement
value prefixes would become reference-only. Trace, message, identity, caller
control and every archival block remain unchanged; latent throwing modes are
retained, not waived. Null contexts, finite contexts, non-null divisors, self
aliases and other APIs are excluded. In particular, source mode `precision` is
M04, not context precision `0`; the construction requires both the listed mode
and the separate unlimited-context field. It does not select an arbitrary
mode spelled `precision:0`.

The successor predicted eight unchanged required ERROR bindings, 96 reference-only
VALUE bindings and **75,624 unresolved statuses** (57,997 VALUE / 17,627 ERROR),
with all 75,728 archival blocks. The adopted TSV was then **v4 `f8f797e7…`** and
decoded eight required / one reference-only / 75,719 unresolved.
The v5 mapper was **UNCOMPILED/UNRUN** at that source-only milestone; no
generation, check or numerical operation occurred in that preparation. A check
against preserved v4 would differ; no such pre-generation run is claimed.
See the [class source preparation](../../docs/decimal-context-value1-policy-526.md#historical-source-only-v5-class-preparation)
for exact formulas, guards, preserved predecessor hashes and contract boundaries.

## Root2310: accepted actual v5 metadata validation

Parent generation session 36380 and check session 47559 exited 0 using mapper
`37f861df…`, its four unchanged pinned files, the pinned Kotlin/JDK tooling,
512MiB heap and a fresh isolated compiled-script cache. Independent actual
review accepted the evidence. The TSV adopted at that v5 milestone had SHA-256
`8d9c9bfb5657b684b19e76dcc5b21176f94fa7601e753ced84f3748760d3fd43`.
All 75,728 compact records and their six-column header remain byte-identical;
104 STATUS records comprise eight unchanged required ERROR bindings and 96
reference-only VALUE bindings (24 mask051 / 72 mask055). This adds 95 statuses
to v4 and leaves 75,624 unresolved (57,997 VALUE / 17,627 ERROR), with every
archival block retained. The full correspondence remains 3,000 controls and
11,472 class-mask checks; these are metadata correspondence checks, not a new
mathematical test run.

The [actual record](../../docs/decimal-context-value1-policy-526.md#root2310-accepted-actual-v5-metadata-validation)
retains exact log/diff/source hashes and original v4 evidence. No production
math or public-JDK numerical oracle ran; fixtures, evaluator and assertions
stayed unchanged in this metadata run. Root2311 records terminal local
calls and released mutex. Hosted CI for this new mapper remains pending;
full-family closure and archival permission remain unclaimed.

## Historical source-only v6 historical-null-error preparation

The mapper source prepared280 additional input-selected historical ERROR
prefixes with status `REFERENCE_HISTORICAL_NULL_INPUT_ERROR`. The then-adopted
TSV remained v5 `8d9c9bfb…`:8 required ERROR +96 reference VALUE +75,624 unresolved.
No TSV was generated/edited and no script/compiler/helper/numerical/test
execution occurred during that source preparation. The mapper was
**UNCOMPILED/UNRUN** at that milestone. A `--check` against preserved v5 would
differ; no such pre-generation run is claimed.

The class is192 null-context inputs plus96 finite-p2/null-divisor inputs,
excluding the unchanged original8 required ERROR bindings. Inputs, not frozen
outcomes, select it; full old rows, NPE controls, D/C/mode/kind/masks and exact
predicate coverage are checked. The v5 unlimited/null96 VALUE class stays
separate. The predicted v6 dictionary was384 bindings,8 required ERROR +96 reference
VALUE +280 reference historical ERROR;75,344 unresolved (57,997 VALUE /
17,347 ERROR), with every archival block preserved.

Current575 JVM NN boundary NPE remains required for all384 displaced null-input
rows and the independent96-key/240-mask ordinary/truthful controls. Historical
nullable ERROR provenance does not establish a universal poisoned-callback
failure-precedence theorem. Trace/message/identity and every other logical field
remain unchanged; no fixture or assertion is waived/archived. See the
[v6 source preparation record](../../docs/decimal-context-value1-policy-526.md#historical-source-only-v6-historical-null-error-preparation)
for formulas, evidence pins, preservation and later parent gates.

## Accepted actual v6 metadata validation

Generation session37902 and check session48932 exited0 in the isolated
`/private/tmp/krogu-526-null-errors-v6-validation-z6PNyhDR/root`, using the
unchanged mapper `08adda6a…`, its four pinned inputs and512MiB script heap.
Independent actual review accepted adoption. The current adopted TSV is v6,
SHA-256 `bc03df8ef1e68a1d602181901f02466d677ab10cace9d4a3d1a57aee400f1b8f`.
Its75,728 compact rows and six-column header remain byte-identical to v5.

The384 exact STATUS records retain8 required ERROR and96 reference VALUE,
then classify280 historical ERROR prefixes separately. There remain75,344
unresolved statuses (57,997 VALUE /17,347 ERROR), with all75,728 archival blocks.
The current575 NN384-entry NPE obligation and96-key/240-mask ordinary/truthful
controls are unchanged; this metadata run executes none of them. No production
or public-JDK numerical operation, test, fixture/evaluator/assertion change is
claimed. The [actual evidence record](../../docs/decimal-context-value1-policy-526.md#accepted-actual-v6-metadata-validation)
preserves raw hashes and v5 chronology. Hosted CI/publication and full-family
classification remain parent gates; no archival permission follows.

# One exact context-dispatch value-prefix policy: source preparation

This record preserves the finite `local:526` source preparation and subsequent
parent metadata validation. At the original source-only milestone the v4 mapper
was **UNCOMPILED/UNRUN**. No script, compiler, helper,
numerical operation, test, build or probe ran; no manifest was generated or
checked during that preparation; the materialized TSV was then v3. Independent
review and root's later metadata generation/check grant were still required.
The accepted actual validation is recorded below. This is not a new
numerical result, JDK bug classification, inherited forwarding guarantee or
completion of the 75,728-row family.

## Exact binding and unchanged contracts

This section records the accepted v4 one-row policy. The later v5 class
extension and its actual metadata validation are recorded below.

Only `D07457 C0625 M01 M06 V 051` gains the logical
`old_value_or_error_status` value
`REFERENCE_CONTRACT_BREAKING_EXACT_OVERRIDE_RESULT`. D07457 is zero-based frozen
record 7456; C0625 is control record 624, counting tab-separated records rather
than physical source lines. The source guard checks both complete records:

```text
D07457: 0.00\t2.0\tdelegate\tnull\t0\tUP\t7.00:2|A.exact-null;
C0625:  0.00\t2.0\tnull\t0\tUP\t!NullPointerException
```

The five-field input projection is `0.00 / 2.0 / null / 0 / UP`; context zero
means unlimited and non-null. `M01=delegate`, `M06=null`; mask `051` retains
`001` poisoned callbacks, `010` null divisor and `040` invoked replacement, with
no null-context bit. The generator pin below binds historical
`ContextProbe.divide(BigDecimal?)`, which appends the exact-call trace and
returns `BigDecimal("7.00")` even for null. Its `enter` explicitly calls
`super.divide(v, MathContext(...))`. The observed internal exact call is not the
caller's explicit one-argument override entry. This old value prefix is a
reference observation of that contract-breaking exact override, while C0625's
ordinary/truthful required outcome remains NPE. The public null and dispatch
basis is documented in the [field analysis](decimal-context-oracle-classification.md#null-arguments-and-an-unlimited-context-replacement).

The eight existing D07425–D07432 / C0609–C0616 error-type bindings, their
`M01 M01 E 009` tuples and all eight rounding modes are unchanged. The ninth
binding is a fixed VALUE tuple, not a general status for other null inputs,
poisoned probes or replacement outcomes. Both logical construction and compact
decoding validate it; the ordered STATUS dictionary must equal exactly those
eight ERROR records followed by this one VALUE record. Neighboring rows with
the same observed value receive no new status.

No input/control keys, old kind, source/divisor mode, flag, trace status,
message status, identity status, canonical-text control, explicit-caller
control or archival block changes. D07457 keeps trace `REFERENCE_OBSERVATION`,
message `NOT_RECORDED`, identity `NOT_OBSERVED` and
`BLOCKED_UNRESOLVED_FIELDS`. All old fixtures/evaluators/assertions and current
575 NPE-boundary adapters stay intact. The non-null migration allows Kotlin
override-nullability edits; it does not require null acceptance. Historical
nullable probes remain separately identified.

## Pins and provenance chronology

| Source or predecessor | SHA-256 |
|---|---|
| Preserved v3 mapper | `e94afbc73c76202fcc7c1453f809df5ba659237f1d5acfb97928889ccd316497` |
| Preserved predecessor v3 TSV | `431cc1e12510bb3b69db4ca89ad1d25e7bc2a4211f8ab24e7ede392e3b8df40b` |
| Frozen dispatch fixture | `88fc3c5bc8277a048a83a9ad9b6546bd8a23ae1ad9a10126409d857367391026` |
| Frozen control fixture | `1245d75ed6837139c939496e602a085cba37b35dd4d3c98f197bdaece2ce7cc9` |
| Historical generator / nullable probe source | `0d9e9b11af4c27892758e78d6b80639b763fcec66ffbe27721d373864a501188` |
| Historical nullable evaluator, provenance only | `2ed00a5dd3c7ec038e61f5be078697b84609606549c26b1bda1249fce860c781` |
| Current migrated common evaluator, verified file pin | `bb61d3f8a5f61850fb208f0e8f5ba814c4b3c7c68b0cb658a0db62081ea41300` |

The preserved v3 mapper pins the historical nullable evaluator `2ed00a5d…`.
Task574 covered comparison/Comparable migration; its
[`changed-source.sha256`](non-null-574/changed-source.sha256) does not list this
context evaluator. Task575's
[`baseline-tracked.sha256`](non-null-575/baseline-tracked.sha256) records
`2ed00a5d…` for this exact evaluator, and its
[`source-files.sha256`](non-null-575/source-files.sha256) records the resulting
`bb61d3f8…`. The preserved
[`source.patch`](non-null-575/source.patch) shows that change: the overrides/entry
become non-null and 384 historical null rows
move by input selection to the JVM explicit-super replay. In particular,
D07457/7456 is selected by `DecimalBinary575Rows.contextDispatch`; its JVM
boundary test requires NPE, and C0625/624 retains the separate truthful control.
Thus this file's old-to-current pin change is documented by task575, rather than
attributed to the sibling 574 comparison edits or an unrecorded intermediate
commit. This preparation preserves the old pin in an
explicitly historical header and separately verifies the current file pin.
The two fixture and generator pins do not change. No old mapper check is
claimed to have passed against the migrated source.

Before any edits, the eight live predecessor files (mapper, TSV, both docs,
both fixtures, generator and current evaluator) were copied into
`/private/tmp/krogu-526-value1-source-predecessor-vI8KpP/`, with `SHA256SUMS`,
then write bits removed recursively. The snapshot manifest SHA-256 is
`17534b4f65ba36a7d1c7a13e1ddf504b49fef2ffd5bde69154b43533d7c8c2a1`.
The historical evaluator bytes are not
invented or included as current source: their existing historical pin is
preserved in the old mapper/TSV and docs. This snapshot is local preservation,
not a newly executed historical certificate.

## Historical conditional counts and execution gates

The fixed 75,728-row construction remains. Eight required error-type statuses
plus one fixed reference-only VALUE binding leave **75,719 unresolved**:
58,093 old values minus one = 58,092 unresolved value statuses; 17,635 old errors
minus eight = 17,627 unresolved error statuses. The source asserts this exact
status partition. All 75,728 rows remain blocked for archival. These counts
are derived from the unchanged bounded corpus and the one-row policy, not from
new execution at that milestone; materialized v3 then had eight required / 75,720 unresolved.

The existing mapper generates the whole manifest, not a header patch. It must
first reproduce the identical six-column header and every compact data byte
from the frozen fixtures, then refuses to write if any noncomment payload byte
differs from the existing TSV. V4 changes schema/provenance/policy/STATUS headers
and only this one decoded logical status; its 19-field decoder still checks
every row against independently constructed logical records. An eventual
`--check` against the untouched v3 manifest would report a mismatch; no such
run occurred in the source preparation.

Root owned independent review and later regeneration plus `--check` using an
isolated compiled-script cache and 512MiB heap, exact before/after payload
comparison, final artifact/source hashes and integration acceptance. Source
inspection is not execution, a successful metadata check is not a mathematical
test, and this one-row policy does not resolve any of the remaining 75,719
poisoned value/error statuses or authorize archival.

## Root2300 actual metadata validation

This retained record describes the earlier v4 adoption; later v5 actual
validation is recorded in Root2310 below, followed by current v6 evidence.

Parent generation session **93225 exited 0**, followed by check session
**39678 exiting 0**, with the pinned Kotlin 2.4.21/JDK 25.0.4+9 tooling,
512MiB script heap and fresh isolated compiled-script cache. The frozen mapper
and its four verified files were:

| File | SHA-256 |
|---|---|
| `scripts/map-decimal-context-contract-coverage.main.kts` | `78869800d0823040e165e8adb62bf25ee10f2fe91cd5e87f538cc43cf5658b0e` |
| Dispatch fixture | `88fc3c5bc8277a048a83a9ad9b6546bd8a23ae1ad9a10126409d857367391026` |
| Current migrated evaluator | `bb61d3f8a5f61850fb208f0e8f5ba814c4b3c7c68b0cb658a0db62081ea41300` |
| Historical generator / nullable probe | `0d9e9b11af4c27892758e78d6b80639b763fcec66ffbe27721d373864a501188` |
| Control fixture | `1245d75ed6837139c939496e602a085cba37b35dd4d3c98f197bdaece2ce7cc9` |

Raw evidence is retained at
`/private/tmp/krogu-526-value1-validation-v1/evidence/`:

| Artifact | SHA-256 |
|---|---|
| `generate.log` | `cf75777a454f15d15f122413ae76250b2fa8c559b836ff9a662efe9cc3054fd8` |
| `check.log` | `aaaf8741a784264dfeb012a595eb8d01a6ef99d1e79be905ace052c6c188008c` |
| `v3-original.tsv` | `431cc1e12510bb3b69db4ca89ad1d25e7bc2a4211f8ab24e7ede392e3b8df40b` |
| Adopted v4 `contract-coverage.tsv` | `f8f797e79d706fbed221e3ec73f77778f1f952a938d071e972983ffb45098927` |

Independent read-only actual review accepted the generation/check evidence and
bounded change. All 75,728 compact data records and their six-column header
remain byte-identical to preserved v3. Exactly nine STATUS bindings are present:
the same eight required null-error types plus the one D07457 reference-only
value prefix. There are **75,719 unresolved statuses** (58,092 value / 17,627
error) and **75,728 archival blocks**. Root copied the same v4 TSV hash into the
publication workspace. No mathematical test or old public-JDK oracle ran, and
no fixture, evaluator or assertion changed in this metadata validation. This is
not full-family closure or archival permission.

Before updating this record with actual evidence, all three source-preparation
docs were copied read-only to
`/private/tmp/krogu-526-value1-docs-before-actual-7vmQpW/`; this document's
preserved source-milestone SHA-256 is
`69c8fe0e02245a2177d4483b58647f720221dd26fe6c2593fffa0160879307e5`.
The original v3 artifact and earlier readonly predecessor snapshot remain intact.
At this documentation handoff adoption is not yet committed or pushed; root
owns subsequent CI, commit and publication. This documentation update itself
performed only source/evidence reads, hashing and the three owned text edits.

## Historical source-only v5 class preparation

This later finite source-only scope replaced individual-row transcription with
one independently constructed input class. The prepared v5 mapper was
**UNCOMPILED/UNRUN** at that milestone: no script, compiler, helper, certificate,
probe, numerical operation, test or build ran during its preparation. No TSV
was generated or edited. The then-adopted TSV remained v4, SHA-256
`f8f797e79d706fbed221e3ec73f77778f1f952a938d071e972983ffb45098927`,
with eight required / one reference-only / 75,719 unresolved statuses. The
accepted v4 execution above did not validate the successor. Its separately
accepted actual execution is recorded below.

Before editing, the accepted v4 mapper and all three docs were copied into the
unique write-bit-removed snapshot
`/private/tmp/krogu-526-value96-source-predecessor-r0K17F/`. Its `SHA256SUMS`
hash is `77341fa457083a5bd5afd7ff67a39c4708d08f1befceebe43b8fe90eb46b8404`.
Preserved mapper SHA-256 is `78869800d0823040e165e8adb62bf25ee10f2fe91cd5e87f538cc43cf5658b0e`;
the docs are `2d04c32de6948de138d008487f5863b74f47764245fb3c1931e36f80c656a374`,
`9b416bbeeb1dc90ae64a43d90c40129484d982630d92c3efcb0f6e5fd25bb365`
and `123832e2d83ae39d023fc41348b861bb5587ba25c9e14f4d13830f35b45145a0`.
Parent identifies current local provenance as `c373…` and published provenance
as `495cb…`; neither is a claim that v5 has been published or executed.

### Input construction and exact guards

Use these independently ordered dimensions, matching the pinned generator's
null-input loop, not any recorded outcome:

| Dimension | Values in index order |
|---|---|
| `s` | `0.00`, `10.50`, `-10.50` |
| `m` | `delegate`, `sign-first`, `sign-second`, `precision` |
| `r` | `UP`, `DOWN`, `CEILING`, `FLOOR`, `HALF_UP`, `HALF_DOWN`, `HALF_EVEN`, `UNNECESSARY` |
| Fixed fields | divisor text `2.0`; divisor mode `null`; context precision `0` |

For every `s=0..2`, `m=0..3`, `r=0..7`, the one-based numeric stable IDs are:

```text
D = 7457 + 192*s + 48*m + r
C = 625 + 32*s + r
source mode ID = M01/M02/M03/M04 according to m
divisor mode ID = M06
old kind = V
mask = 051 when m=0, otherwise 055
```

The source also scans frozen input fields only and requires that this predicate
denote exactly the constructed D list in order. It then checks the full old
seven-field row, including exact `7.00:2|A.exact-null;`, and full six-field
control, including exact `!NullPointerException`. Outcomes and traces are
checked evidence, never selectors. It checks 96 unique D IDs and 24 C IDs, each
used four times. The three control ranges are C0625–C0632, C0657–C0664 and
C0689–C0696. D07457/C0625 retains its accepted tuple.

Both logical construction and compact decoding require the full input, source
mode, null divisor, VALUE kind and exact compact tuple. Mask `051` preserves
poisoned getters, null divisor and invoked replacement; `055` additionally
preserves latent throwing metadata. Neither mask has null-context, inconsistent
metadata or recorded-message bits. The STATUS dictionary is exactly the original
eight unchanged required ERROR tuples followed by these 96 VALUE tuples in
constructed order. The existing fixture, generator and migrated evaluator pins
are unchanged; historical `2ed00a5d…` remains provenance only. The unchanged
payload guard must still refuse any six-column header/data byte change, and
the decoder must still recover every one of the 19 logical fields.

### Contract argument and excluded boundaries

This generalizes the accepted D07457 argument, not a rule that every poisoned
subclass is unconstrained. The public null-object-argument contract supplies
NPE. The historical nullable exact override instead returns `7.00` for null;
that replacement therefore violates the underlying exact-divide null contract
for every input in this class. Changing the source text, a latent source
getter mode or the non-null context's rounding mode does not repair that
violation. The 24 ordinary/truthful controls retain NPE. The class's recorded
entry is explicitly `enter -> super.divide(null, unlimitedContext)`, not an
explicit one-argument override call. Its later `A.exact-null;` is an internal
forwarding observation, never a required callback or absence of other calls.
Well-typed direct caller-owned override entry/result/exception controls remain
independent. The user's non-null input and permitted Kotlin override-migration
policy does not require current common code to accept null. All class inputs
remain selected for task575's Java explicit-super NPE replay; no assertion is
changed or newly claimed to have run here.

These boundaries prevent trace-only or name-only expansion:

- Context precision `-1` means a null context and is excluded, even for the same
  receiver/null divisor. Precision `2` is a finite context and is excluded.
- Source mode `precision` is M04 with a latent throwing getter. It is distinct
  from the context precision field `0`, and from replacement mode `precision:0`;
  the latter is not a class member.
- Non-null divisor mode `delegate` and self aliases are excluded even if an old
  result is `7.00` or a trace includes `.exact;`. Non-null exact replacements
  require their own numerical/contract analysis.
- Other source/divisor texts, probe modes, contextual operations and explicit
  one-argument null calls are excluded. No other API's null behavior or
  mathematical return is classified by this construction.

### Conditional counts and later parent gates

The class has **96 total** VALUE bindings, including D07457, so adds **95**
reference-only statuses to adopted v4. Conditional v5 totals are eight required
ERROR statuses, 96 reference-only VALUE statuses and **75,624 unresolved**:
58,093 old values minus 96 = 57,997 unresolved VALUE statuses; 17,635 old errors
minus eight = 17,627 unresolved ERROR statuses. Source checks enforce these
counts. All 75,728 rows retain `BLOCKED_UNRESOLVED_FIELDS`; trace/message/identity,
canonical control, explicit caller control, modes, flags and keys remain intact.

These are source-derived expectations, not metadata or mathematical results.
Running v5 `--check` against the preserved v4 TSV would differ; no such
pre-generation run is claimed. Root owned independent source review and a
separately granted isolated generation/check, exact payload comparison, artifact
hashes, actual review and adoption. No full-family resolution or archival
permission follows from this finite preparation.

## Root2310: accepted actual v5 metadata validation

Generation session **36380 exited 0**, followed by check session **47559 exiting
0**, using pinned Kotlin 2.4.21/JDK 25.0.4+9, a 512MiB heap and a fresh isolated
compiled-script cache. Mapper SHA-256 remained
`37f861df762f4b5dc0959b7f4d060edd0f75cf8722d77e0f726e86bf0ac1b12e`;
the dispatch fixture `88fc3c5b…`, control fixture `1245d75e…`, migrated evaluator
`bb61d3f8…` and historical generator `0d9e9b11…` remained unchanged at the full
pins recorded above. No production or public-JDK numerical evaluation, tests,
fixture/evaluator/assertion changes were performed in this metadata run.

Evidence root: `/private/tmp/krogu-526-value96-validation-WGZEul/`.

| Artifact | SHA-256 |
|---|---|
| `evidence/generate.log` | `717dbd87f141b9a4b498c8ca7ee47d60e8e76099db390d13ab862ee367c6ab64` |
| `evidence/check.log` | `22d10bfc763bdf98d60dcba919009120373045294ec875861b6089e86551aa69` |
| `evidence/v4-v5.diff` | `12e6ffc5407587c9deec5be9ecfa0996184848ab2a74f7a5c89203169c52fd6b` |
| Preserved `evidence/v4-original.tsv` | `f8f797e79d706fbed221e3ec73f77778f1f952a938d071e972983ffb45098927` |
| Retained adopted v5 TSV | `8d9c9bfb5657b684b19e76dcc5b21176f94fa7601e753ced84f3748760d3fd43` |

Independent actual review accepted the evidence, bounded dictionary and exact
payload comparison (exit 0). All **75,728 compact rows** and their six-column
header stayed byte-identical to v4, retaining all keys and flags. Exactly 104
STATUS bindings comprise the original eight required ERROR tuples and 96
reference-only VALUE tuples: 24 mask051 and 72 mask055. D07457 is included,
therefore **95 statuses are new** relative to v4. There remain **75,624
unresolved statuses** (57,997 VALUE / 17,627 ERROR), with all **75,728 archival
blocks**. The lossless 19-field mapping retains 3,000 controls and 11,472
class-mask correspondence checks. These are metadata checks, not a new run of
11,472 mathematical tests.

Root adopted the exact v5 TSV hash in the publication workspace. Root2311
records that all local invocations are terminal and mutex `cfc74…` was released
(empty mutex revision 776). Hosted CI for the new mapper remains pending; this
record makes no hosted acceptance, full-family completion or archival claim.

Before this text-only actual update, the prepared mapper and all three docs
were preserved write-bit-removed at
`/private/tmp/krogu-526-value96-docs-before-actual-E8Kvbu/`, manifest SHA-256
`0f2727346b77b38ce1363838bba7e3d77b6c5c7ca53f0ddde37cc291a44eff5c`.
The three prepared docs had SHA-256 `e4f197ff567f92799ebf3d2bec0cff1e5153305fa2c6f7490c05c01d382b7d7a`,
`6221fd30ccfa740ad0a6d48aebdd958f4301f925a0316c0a2592fc8fe82d2004`
and `6b503c1a6c82dfcc56c8876e63c8e5b7f8556f9771ada092bd85259c2d6e1b23`.
This documentation update read evidence and hashes and edited only these three
docs. It ran no scripts, compiler, helper, probe, tests or numerical work and
changed no mapper, TSV or input. Parent owns final review and publication.

## Historical source-only v6 historical-null-error preparation

This later finite source-only scope implements the independently accepted
policy proposal `/private/tmp/krogu-526-next-input-class-proposal.md`, SHA-256
`f43773e09909acb128bb751aa48a8084cef6a37a3fdb5db08bc1709b66952ed7`.
The prepared mapper SHA-256 is
`08adda6ad57223c073861141d680bb2e3f11d10ba15ca893f03851a3a60da030`.
It was **UNCOMPILED/UNRUN** at that source-only milestone: no script, compiler,
helper, certificate, numerical operation, probe, test, build or metadata
generation/check ran during preparation. The then-adopted v5 TSV remained
untouched at `8d9c9bfb5657b684b19e76dcc5b21176f94fa7601e753ced84f3748760d3fd43`,
with8 required ERROR +96 reference VALUE +75,624 unresolved. Its Root2310 actual
evidence above is not relabelled as execution of v6.

Before edits the accepted mapper, adopted TSV, three docs and all four pinned
evidence files were copied into the unique write-bit-removed snapshot
`/private/tmp/krogu-526-null-errors-v6-predecessor-8JDgiz/`. The nine-entry
manifest hash is `0281fdf6d7aef07418415c8f0dd0508ec7ad876cce9678c970f3db557a200364`.
The predecessor mapper is `37f861df762f4b5dc0959b7f4d060edd0f75cf8722d77e0f726e86bf0ac1b12e`.
Predecessor doc hashes are `214ec13cedc625c06d853d3abb5cd81817db0d0ba762ba3671f0c7af91985f90`
(diagnostic README), `09360aa48cff493938d7842bb6eb0afd52a9f91256e3d823a9f9fa4437aa80b6`
(classification) and `cedc5a48f5ae803cedd87287425c517a6fcc4141637be68fabe7bbc752f32667`
(this record). The dispatch fixture `88fc3c5b…`, control fixture `1245d75e…`,
generator `0d9e9b11…` and current evaluator `bb61d3f8…` retain their full pins
above. No evaluator/fixture/generator/assertion was edited.

### Input-generated class and exact guards

Reuse ordered source texts s=`0.00,10.50,-10.50`, source modes
m=`delegate,sign-first,sign-second,precision`, and the eight rounding modes r
from v5. Add divisor modes b=`delegate,null` and encoded context precisions
c=`-1,0,2`, all indexed from zero, matching generator77–80. Divisor text is2.0.
The stable IDs are:

```text
D = 7425 + 192*s + 48*m + 24*b + 8*c + r
C = 609 + 32*s + r                    when b=0,c=0
C = 617 + 32*s + 8*c + r             when b=1
```

Select `c=0 || (b=1 && c=2)` from these inputs:192 null-context rows plus96
finite/null-divisor rows. Exclude the unchanged eight required rows exactly
s=0,m=0,b=0,c=0. The remaining280 records use72 controls: eight controls have
three remaining m variants;64 controls have four. The generated order must
equal an independent frozen-input predicate scan, excluding the required set.
The source also checks that all384 corpus null-input IDs equal the disjoint
union of old8, unchanged v5-96 and new280; the384-row task575 obligation stays
separate from the historical status meaning.

Require the full seven-field old record ending exactly
`!NullPointerException|`, the full six-field NPE control, original D/C mapping,
source mode M01/M02/M03/M04 and ERROR kind. Exact divisor modes/masks are:

| Route | Divisor mode | m=0 | m=1/2/3 |
|---|---|---|---|
| Null context, ordinary divisor | M01 |009|00d|
| Null context, null divisor | M06 |019|01d|
| Finite p2, null divisor | M06 |011|015|

Outcomes and flags are evidence guards after input construction, never selection
criteria. Construction and compact decoding both check the exact tuples.
The ordered384-record STATUS dictionary keeps the original8 required records
and96 v5 reference records, then appends the new280 historical ERROR records.
The existing six-column payload byte guard, all keys/modes/flags and lossless
19-field decoder remain protected. Only the280 old error-prefix status fields
would change. Trace remains reference-only, message NOT_RECORDED, identity
NOT_OBSERVED, NPE control/canonical NOT_APPLICABLE, explicit caller control and
every `BLOCKED_UNRESOLVED_FIELDS` archival status remain unchanged.

### Historical provenance and current mandatory rejection

The new status is `REFERENCE_HISTORICAL_NULL_INPUT_ERROR`. It records historical
nullable ERROR prefixes without claiming their callback/exception precedence
universally required on poisoned subclasses. Original generator `enter` calls
`super.divide(v,context)`; its nullable exact override is not the caller's
explicit entry. The v5 unlimited/null replacement VALUE class therefore remains
separate. Latent throwing modes are preserved, not erased as supposedly unused.

Current575 JVM Java-to-Kotlin NN NPE rejection remains **required for all384**
displaced input rows. `everyDisplacedContextDispatchRowUsesExplicitJavaSuper`
requires that boundary outcome. Independent ordinary/truthful controls remain
96 distinct keys/240 class masks under
`everyDisplacedContractMaskUsesOrdinaryOrTruthfulJavaReceiver`. These tests are
not run by the mapper. No current NN guarantee is weakened, and current replay
is not proof that the historical JDK rejected v5's96 replacement values.
The stronger historical nullable failure-precedence theorem remains unproved;
an empty old trace or a current NN test does not establish it. This conservative
field classification does not waive other inherited or direct caller contracts.

### Prepared ledger and remaining parent gates

Prospective v6 totals are8 required ERROR,96 reference VALUE,280 reference
historical ERROR and**75,344 unresolved**:57,997 VALUE and17,347 ERROR.
The unchanged old totals give17635-8-280=17347 unresolved errors and
58093-96=57997 unresolved values. All75,728 archival blocks remain. Source
checks assert this exact partition; these are expected counts, not execution
results. No old fixture/assertion is removed or archived.

V6 `--check` against preserved v5 would report a mismatch; no such
pre-generation run occurred. Parent owned independent implementation review,
separately granted fresh-cache generation/check, exact payload comparison,
final hashes, actual review and adoption, recorded below. This finite
preparation closed no whole family/task and acquired/awaited no runtime window.

## Accepted actual v6 metadata validation

Generation session**37902 exited0**, followed by check session**48932 exiting0**,
in `/private/tmp/krogu-526-null-errors-v6-validation-z6PNyhDR/root`.
Captured runner commands use512MiB through both JAVA_OPTS and `-J-Xmx512m`,
with an isolated compiled-script cache. Mapper SHA-256 remained
`08adda6ad57223c073861141d680bb2e3f11d10ba15ca893f03851a3a60da030`;
the four evidence inputs retained their full fixture/generator/current-evaluator
pins above. Independent actual review accepted adoption. Root copied the exact
v6 TSV into the publication workspace; no source, fixture, evaluator or assertion
change was required by this metadata execution. Artifact paths below are
relative to `/private/tmp/krogu-526-null-errors-v6-validation-z6PNyhDR/`.

| Retained artifact | SHA-256 |
|---|---|
| `evidence/generate.log` | `5f9d9b30ca5a8986ffa6b9ead447f8b34146c1ce39926cf5f77a32bb84216e57` |
| `evidence/check.log` | `ab5123743f5e747aa7a0de8d946ca13459fc9b5d2310491baa77c0569a34b98b` |
| `evidence/v5-v6.diff` | `52a1e4ca75f1ad3e87460d9ff1dae68557b036d44444c3ccdaa6720ba60a71a0` |
| `evidence/v5-original.tsv` | `8d9c9bfb5657b684b19e76dcc5b21176f94fa7601e753ced84f3748760d3fd43` |
| Current adopted v6 TSV | `bc03df8ef1e68a1d602181901f02466d677ab10cace9d4a3d1a57aee400f1b8f` |

The retained v5/v6 noncomment payloads compare byte-identically (exit0),
including all75,728 compact data rows and the six-column header. The exact384
STATUS bindings preserve8 required ERROR and96 reference VALUE records, and
append280 `REFERENCE_HISTORICAL_NULL_INPUT_ERROR` records. Only those historical
error-prefix statuses change; their traces, messages, identities, flags, keys,
canonical/explicit caller controls and all75,728 archival blocks stay protected.
The exact decoded ledger is**75,344 unresolved**:57,997 VALUE +17,347 ERROR.
The correspondence still covers3,000 controls and11,472 class masks; those
counts describe checked metadata, not a new mathematical test campaign.

Current575 NN NPE obligations for384 displaced entries and the96-key/240-mask
ordinary/truthful controls remain required and unchanged. This run executes
neither575 test nor the historical nullable JDK generator. It provides no
production/JDK numerical result, stronger historical poisoned-callback
failure-precedence theorem, full-family completion or archival permission.
Hosted CI and publication remain parent gates.

Before this text-only actual update, the three prepared docs were copied into
the unique write-bit-removed snapshot
`/private/tmp/krogu-526-v6-docs-before-actual-iDgILR/`, manifest SHA-256
`dc572f213a6dad6a376596c60ee5805fa74cdd9887b279102454ab96272b67ad`.
Prepared doc hashes were `d280754b0c97dea0e761ae389bf36a8f27fc817696932c0f2b2d11a9937b0bb7`
(README), `af4f562f8770c44bb35179a095fb09e77d1ac18d7f2f14f6433bbdfc4911494f`
(classification), and `288b8987cd8133761c4915492594e06f84914a820b155247078ed95ef07ea717`
(this record). This update read evidence/hashes and edited only these three
docs; it ran no scripts/compiler/helpers, numerical operations, probes, tests,
builds, Git or tracker commands and changed no mapper, TSV or input.

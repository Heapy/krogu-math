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

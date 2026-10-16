# 526 V7 non-null p0 VALUE-prefix preparation

This is a SOURCE-ONLY successor proposal. The mapper, its new branches and its checks are UNCOMPILED/UNRUN. The candidate `contract-coverage.tsv` is still the unchanged V6 file; no V7 manifest has been generated. Parent review and a separate execution grant are required before generation/check. No production, fixture, evaluator, control, assertion or generator source changed.

The accepted input is `/private/tmp/krogu-526-p0-exact-review-8MCC2E6H/REPORT.md`, SHA-256 `e8867632cbfa6a621b2c1729ce0352b12f4c859ab3e56ad5cc4da4fa04fd4b09`. Its input/output indices are respectively `aaf03ed2064f46b4a3097cbd98cf92c617059d376e251a2d8f63edc78221136b` and `dd3a09b7a683e2081b1fb94eb05fd3a180469cd699a4e4372dd5d2a1c4ec0a1a`.

## Exact admitted class

The mapper embeds the accepted 13 ordinary source/divisor families and six self families, the original four probe modes and eight rounding modes. Only the pinned corpus's non-null-divisor, precision-zero input rows are candidates; the observed replacement value never selects a row. Every constructed row must match its full frozen seven fields, full control six fields, D/C IDs, source/divisor mode IDs, VALUE kind and mask. Their concatenation in frozen-row order is 19 tab-separated fields followed by LF per row; its SHA-256 must be `7dfb1f7459e5f9241d148fd64160ea703d1fa4f5fbf926803c3e9900f84a36be`. A retained static projection is `evidence/expected1952-full-bindings.tsv`; it is metadata evidence, not an executed V7 output or an extra mapper dependency.

The zero-based construction is:

| Family | Frozen row index | Control row index | Count |
| --- | --- | --- | ---: |
| Ordinary | `512*i + 128*a + 32*b + r` | `32*i + r` | 1664 |
| Self | `6656 + 128*i + 32*a + r` | `416 + 32*i + r` | 192 |
| Repeated ordinary | `7432 + 192*s + 48*a + r` | `32*j + r`, `j=[1,3,4][s]` | 96 |

Here ordinary `i=0..12`, self `i=0..5`, `a,b=0..3`, `r=0..7`, `s=0..2`; one is added when rendering D/C IDs. The largest selected frozen index is 7967 and largest selected control index is 583; all arithmetic and indexing are bounded. This uses no numerical library or numerical oracle. Direct static metadata comparison found zero formula mismatches across 1952 rows; the Kotlin construction itself remains unexecuted.

Every full frozen observation is exactly `7.00:2|A.exact;`. Masks are `041:128`, `045:1632`, `061:48`, `065:144`. There are 152 projected controls and 512 ordinary/truthful class masks: 96 VALUE controls and 56 ArithmeticException controls. Control multiplicities are 48 used four times, 80 used sixteen times and 24 used twenty times. They cover ordinary zero division and nonterminating division as well as successful values and self division. None requires the numeric quotient seven. “Non-null” does not mean a numerically nonzero divisor.

## Contract and preservation

The accepted argument concerns the actually reached unconditional exact override returning `7.00`, not dormant poisoned getters. The public precision-zero division contract requires an exact quotient or the applicable arithmetic error; these ordinary projected outcomes remain required. The [Java 25 divide-with-context contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#divide(java.math.BigDecimal,java.math.MathContext)) does not prescribe internal forwarding to another override. Explicit caller overrides/super obligations, truthful subclass controls and canonical-text/error controls remain intact. This does not waive other invalid-subclass outcomes.

Only logical field `old_value_or_error_status` changes for these 1952 IDs, to the existing `REFERENCE_CONTRACT_BREAKING_EXACT_OVERRIDE_RESULT`. No new status is introduced. The old eight required ERROR, 280 historical ERROR and 96 reference VALUE bindings remain unchanged. The V7 dictionary preserves their exact 384-record prefix and appends precisely these 1952 records. The mapper rejects a changed V6 prefix or any partial/unreviewed successor dictionary. Expected totals are 2336 STATUS records, including 2048 replacement VALUE prefixes; 73392 statuses remain unresolved (56045 VALUE, 17347 ERROR).

All 75728 compact data rows and their noncomment header remain byte-identical, protected by the pre-write payload guard. Full 19-field decode/encode equality remains required. All trace, message, identity, interaction, caller, alias, metadata and archival fields remain unchanged; all rows remain blocked for archival. The four original fixture/evaluator/generator/control hashes are unchanged and verified before any metadata parsing. `DecimalContextContractTest`, its four partitions, the 575 selectors and explicit-caller suites are not edited. No assertions are removed or bypassed by this mapper.

The private original archive was created before edits. A complete extracted-tree comparison before adding this document found only the mapper differed. The candidate and publication TSV retain SHA-256 `bc03df8ef1e68a1d602181901f02466d677ab10cace9d4a3d1a57aee400f1b8f`; the publication mapper retains V6 SHA-256 `08adda6ad57223c073861141d680bb2e3f11d10ba15ca893f03851a3a60da030`. The private original archive has SHA-256 `f3d7ae405de76a5feda153db66010ea13b172302948969eeef18f9b114a02dd3`.

The implementation is proportional metadata work: bounded loops, lists, exact string comparisons, counts and SHA-256; no new dependencies, production imports or arithmetic model. New full bindings contain 1952 records and the retained projection is 259552 bytes. Existing full-corpus logical/compact lists and text assembly remain unchanged; this is no heap/time guarantee. No numerical/compiler/helper/script/test/build/process invocation, Git/task transition or runtime mutex acquisition occurred in this preparation.

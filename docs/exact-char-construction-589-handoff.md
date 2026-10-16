# 589 finite source handoff

UNCOMPILED/UNRUN; no production adoption, compilation, test result, commit or
task transition is claimed. Candidate root:
`/private/tmp/krogu-math-task-589-Otaig4Jy/root`.
Parent owns independent source review, composition and any later execution.
No runtime mutex was acquired or future window awaited.

## Frozen payload

|Relative candidate path|SHA256|
|---|---|
|src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt|`5731c8df6ba366a51d907aec97cdef0874df21607638bdcf37b865e85426f741`|
|src/commonTest/kotlin/io/heapy/krogu/math/DecimalExactChars589Test.kt|`e3d8a2fc085cd056c990f0ff4e3481df32b7e4ba295fa2b8ed14159493217b0e`|
|docs/exact-char-construction-589-proof.md|`63bab70c6baa5e0c07a66f31b5be579c7c97d625fb72c46bfe3aa7a1f2667da7`|

This fourth file's hash is supplied in the external ../evidence/payload.sha256.
The complete source245 manifest is ../evidence/source245.sha256,
SHA256 `d97fc05b98ba21c72e65bb126de36574650d5b74c363d0a0c346a8801b58497b`.
It differs from frozen source244 only in BigDecimal and the one new common
test file; all243 other old source hashes pass unchanged. All245 current
source hashes also pass. Full src diff and precise manifest diff are retained.

Before edits, all244 copied source hashes and the exact pathset passed. The
read-only baseline archive is ../originals/source244-baseline.tar,
SHA256 `d82ff3335f0b767dbb93c268202f943b31d2d41a6ed7729c573c517a6aa3f9ce`.
Original BigDecimal, unchanged588 helper, source244 manifest and initial
capsule index were separately preserved and verified in originals.sha256.
The archive excludes build/.gradle/.git/.kotlin; there is no .git in this root.
No original source/evidence, main0854, publication or other worker tree changed.

## Narrow change

The exact CharArray slice constructor now calls the SAME accepted588 owned-char
lexer at MathContext.UNLIMITED. Whole-array exact construction already delegates
to that slice. The old private characters->String helper had only this caller
and was removed; before/after reference inventory is retained. The exact diff
is ../evidence/BigDecimal.diff,
SHA256 `2ef04c42e11ea4e6593177e7afc9c6067421ee3b5b8c383299f47ba686261915`.

Every String/contextual-char/numerical constructor, DecimalText588, other
helper, field, public signature, cache, serializer, fixture, existing test and
consumer is byte-identical. There is no new algorithm, lower cap, optional
zero stripping, scale rescue, String fallback, host arithmetic or OOM catch.

The focused proof specializes588 to p0: full selected lexical/exponent/Int-scale
validation, D0 zero at the lexical scale, otherwise retained precision D,
drop0 and unchanged exact coefficient/scale, then actual final binary support.
It includes owned slice bounds/snapshot/lifetimes, leading-zero input/type
capacity premise, unchanged resource548 assumptions and diagnostic inventory.
No giant allocation, backend String-limit crossing or JDK behavior was executed.

Four prepared common methods add independent exact literal fields for whole/
slice Unicode/zero/19- and30-digit values and endpoint scales; a bounded actual
16,384-leading-zero char input with tiny result and later caller mutation;
malformed selected text and Int overflow-shaped ranges; and exact whole/slice
Kotlin super construction before real caller-owned super.round arithmetic.
Expected numerical fields never use a decimal constructor as their oracle.
No callback count/order/noncall, generic identity or legacy serialization
promise is introduced. No existing assertion is waived or changed.

No known bounded required diagnostic conflict was identified. Ordinary lexical
categories/messages and exact fields are preserved by source argument. Changed
giant intermediate/resource chronology, VM-generated null wording and future
concrete library diagnostic conflicts are explicitly separate parent526/548
questions, not a blanket waiver. Existing records remain active.

## Remaining gates and historical limits

All new code/tests/proof are SOURCE ONLY. Root/prime independent review must
precede later composition, initial compilation, actual new/common/retained
record tests, fresh source/JAR binding and hosted/public gates. This narrow
589 change is not a fix for source244's original16 sqrt assertion failures;
their unprinted468 aggregate discrepancies and aborted loop tails remain
unclassified/unobserved exactly as the accepted initial audit records.

No scripts/compiler/numerical/helper/model/certificate/probe/test/build/
benchmark/process, Git/tracker or mutex operation occurred. Direct metadata
CLI reads/hash/diffs/archive copies and source/doc writes only were used.
PREPARATION.md preserves the corrected initial empty-copy cwd/read-path
mistakes; those were metadata failures, not numerical retries or changed
outcomes. All preparation calls are terminal at final handoff; no background
campaign or future-window waiting follows. No whole549/548/541/526 acceptance
is claimed.

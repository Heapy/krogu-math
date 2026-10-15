# Task528 V2 driver provenance preparation — grant1929

SPDX-License-Identifier: Apache-2.0

**PREPARED SOURCE ONLY, UNCOMPILED/UNRUN.** Parent1929 permits a new driver
copy with only one candidate production pin and its prepared-scope comment
changed. Root1919 owns the current Wasm/macARM numerical window. This document
does not grant script/compiler/fixture/test/probe/build/measurement execution.
Task528 remains linked/in_progress; improvement acceptance remains OPEN.
Changes are not committed; no Git/task transitions or root edits.

## Exact source identity and minimal change

Original live and historical driver:
`scripts/measure-decimal-metadata.main.kts`, SHA256
`0e4f59ecc33350eb097d4bfe262a54a53853c2e00078f04275035bca7b2c09c5`.

New separate copy:
`scripts/measure-decimal-metadata-v2.main.kts`, SHA256
`544584e8b48a55d75d3799f7a300ff547bdcc5fa96ce6d265522f61daeb905da`.

The original was copied byte-for-byte before exactly two line replacements:

1. Prepared-scope comment names grant1929/V2 instead of1865.
2. Candidate BigInteger source pin changes from
   `bdbd1a0f6d0ec9571c641b7a34ff73ac969da051c648fa6b606ff775c8739f57`
   to accepted V2
   `24cfd50f0dd9edd25590877b23b4c2e72c89ce9fab71f4b75bc0fe11def56a5f`.

Unified diff `/private/tmp/task528-driver-v2-1929.diff` has only these two hunks,
SHA256 `51ec4f4a6cf0b9fc8964117a7852368ff39ef96f189b26a4568afd2d9e316c5d`.
Its `diff -u` exit1 is the expected source difference, not an executed numerical
failure. Reversing exactly these two replacements must recover original bytes.
The original driver remains live0e4f59ec and in the read-only V1 snapshot.

## Byte-identical protocol and guards

Every other byte is unchanged. Baseline BigDecimal/BigInteger pins and candidate
BigDecimal92de3354 remain. All72 cohorts, signs/orientations, compact9/128/1024/
4096-digit bounds,720 samples/fork,66240 measured+5400 warmup calls, two modes,
warmup/repetition/iteration budgets, direct target calls, separate fixtures,
expectations, result arrays, checksum consumption and output schema are identical.

Allocation support/NA reasons, counter enablement/validation, GC before/after
counts/time/deltas, field declarations and their limits, heap/runtime pins,
ordered flags, complete source-manifest/symlink/hash validation, exact class
origins and source/JAR/flags/fork cache identity guards are unchanged. No new
dependency, field, cache, fixture/oracle change, guard waiver or warmup is added.

The existing twelve args already supply driver path/SHA, so no filename or cache
logic changes are needed. Later both variants use the SAME new driver source
and its544584e8 SHA, the same accepted normalized flags and balanced six-fork
order B1/C1/C2/B2/B3/C3. Fresh per-fork cache names will automatically include
the new driver hash plus selected JAR/manifest/flags identities. Old compiled
caches must not be borrowed. Six forks retain4320 samples/429840 total target
calls; all raw samples/failures/regressions and host limits must remain.

## Frozen sources and historical evidence

All twelve artifacts in `/private/tmp/task528-v2-source-1918-final.sha256`
(manifest SHAe4dce7c3615c781486516d7b7cdf603eb349a874a2c7806f076080e1d1c005f7)
were verified unchanged before preparation. This includes V2 production/tests/
proof/auditor/handoff, original driver/plan and retained originals. Original
plan SHAedb96d8216d31e80cb8aa3f633dd147d4d59c499a0f55effdd87651bd3a05ae0;
V2 handoff SHAb19e6523636203faaec879a429c092161120141170d9004c130c80e78b36b5f3.
They were not rewritten. The historical handoff's request for a provenance-only
driver grant is fulfilled by1929 and this separate preparation record.

V1 ten-file read-only snapshot/archive, matched JARs, full raw measurement and
test evidence, initial failures and comparison allocation regressions remain
preserved. This preparation creates no new JAR or performance/test evidence.

## Review and pending execution gates

Focused independent read-only diff/document review found no findings and accepted
the provenance-only source preparation. Reversing exactly the two authorized
replacements reproduces original SHA0e4f59ec, proving all remaining protocol/
guard bytes unchanged. Reviewer verified new driver544584e8, minimal diff51ec4f4a,
and the pre-review-status document SHA
`6d4f5dba4ce051758ac89addf70f7948606040c1cd35fd0813501daff283a04f`.
It made no edits, compiler/script/library execution or fixture construction.
Final preparation-document SHA is recorded separately in the task handoff.
Current checks are CLI source/hash/whitespace reads only; no driver/script/
certificate/port compilation or execution and no runtime fixture construction.

Only AFTER root's explicit all-owned-invocations-terminal release and a separate
parent runtime grant may V2 execute full retained/new four-target gates, prepared
static audit, fresh matched own baseline/candidate JAR builds/source manifests/
build ledger, and the accepted balanced six forks. The later grant must retain
1200-second per-fork deadline including driver startup/compilation, captured-
owned-PID/start/lineage-only timeout cleanup, partial evidence and actual host
conditions. Timeout observation/polling/reconciliation limits remain disclosed;
no hard-real-time, JMH uncertainty, retained-byte or overall pass claim.

Source acceptance/preparation does not establish lower allocation or faster
comparison. V2's improvement gate and root fresh exact-integrated-head all-six
CI remain pending.

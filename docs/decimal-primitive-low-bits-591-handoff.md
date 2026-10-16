# 591 + BigDecimal549 finite SOURCE handoff

SPDX-License-Identifier: Apache-2.0

Candidate: `/private/tmp/krogu-math-task-591-yGlD46B8/root`.
All new code/test methods are **UNCOMPILED / UNRUN**. This is not publication,
runtime acceptance, a task transition, or an execution-window request.

## Frozen base and exact payload

The candidate was copied from frozen source248 at
`/private/tmp/krogu-590-549-validation-9oin4Tqj/root`, excluding .git/build/
.gradle/.kotlin. Its complete248-source manifest is
`d9074c29e8b4680663eb6ace92b83a38fafb3bcf15604be2f81a71e9058af17a`.
All248 original sources were archived before edits in read-only
`../evidence/original-source248.tar`, SHA256
`6166715224e4d931c333550b54c1fe5add0aee6dc0e8be3864db0e7b231fa6cb`.
Original BI/BD copies, task body and source manifest are separately read-only.
No existing frozen evidence, publication file or other worker's source changed.

| Owned file | SHA256 |
|---|---|
| src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt | d23bb784426669405c95f0661da4cc0c559ed7efc9b6d2a060d62258ea6b9c9e |
| src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt | b2bc16ada086cff71a79de53b739b792e190f17a1834e295c784e7b017af32c3 |
| src/commonTest/kotlin/io/heapy/krogu/math/DecimalPrimitiveLowBits591Test.kt | 5389c0c20d7c557afab48bb6b577cdbc0a0077f825a56e3ef281a508d4caa9fb |
| docs/decimal-primitive-low-bits-591-proof.md | 76d441ccd5312c3e29540de6a777ecd0c5ce7f185cdf435f820022d9e903b76b |

This handoff is the fifth owned file; its hash is external. Full source249
manifest `../evidence/candidate249-sources.sha256` has SHA256
`dcc46f2f58b0f1aa0dc7d26f88c9200d643344241d6a8a1c59c01d985447217d`.
All249 candidate hashes pass, actual pathset equals original248 plus the one
new common test, all246 nonowned original source hashes pass, and all248
frozen parent source hashes still pass after editing this private candidate.
Exact BD/BI diffs are retained separately in evidence/.

## Source change

For negative scale s, k=-s.toLong() is safe even atMIN. The entire s>=0 route
and existing k>=32/64 modular-zero shortcut remain unchanged. For0<k<bits,
the stored sufficient guard b+4k<=L keeps the **original materialized/virtual
expression** when its full mathematical product is supported. Otherwise a
new internal stored BI helper reads at most the first three30-bit limbs,
applies canonical sign and bounded wrapping Long products, and returns signed
low32/64 bits. No public integer product, extra caller toLong callback,
magnitude-sized allocation, class gate, field/cache or new numerical cap is
introduced. The full ring/width/sign/alias/resource argument is in the proof.

For symbolic A=2^L-1@-1 the real new outer guard chooses the ring helper and
gives-10, or+10 for negative A, instead of rejecting the oversized temporary.
That input remains **UNEXECUTED**. Bounded public controls keep the safe old
path or the existing zero-tail shortcut; direct helper calls exercise the
actual correcting arithmetic without a fake production limit.

BigDecimal.compareTo now returns explicit unit results on compact/sign branches
and branches on magnitude sign before reversal. Compact metadata bypass and
noncompact getter order remain intact. The old positive-sign versus else
classification is retained for false reported signs±2; no multiplication by a
reported sign or negation of raw Int.MIN is introduced. Explicit caller
override/super behavior, all existing fixtures and assertions remain intact.

Public BI toLong and BI549 normalized compareTo, the590 exact-residual helper,
all other BI bodies, all decimal exact conversions, toBigInteger, Float/Double,
fields, caches, serialization, arithmetic/formatting helpers and compiler/build
settings are unchanged.592 fixed-scale division is **not** part of this scope.

## Meaningful prepared tests and later gates

Three new common methods prepare:

* 950 direct stored-kernel calls across19 hex magnitudes, both signs,32/64 widths,
 and selected raise/limb/carry boundaries. An independent bit-vector shift/add
 model supplies expected bits without production integer operations or Long
 multiplication. Inputs are below128 bits; source immutability is checked.
* 2600 top-level public conversion calls, including bounded negative scales,
 width boundaries, MIN/MAX scales, zero and literal fractional truncation.
* 531 raw comparison calls from23 literals in12 independently ordered cohort
 groups plus two scale-endpoint controls. Expected units use index branches.

These are prepared call counts, not executions or all-input claims. Three
additional common methods imply prospective571 JVM and380 on each other local
target,1711 total, if the current selection remains complete. Root's separately
accepted source2481699-method/418-XML and94282-ledger results remain evidence
for the **unchanged base**, not this successor.

Independent source/code/test/proof review, actual compilation, fresh complete
JVM/JS/Wasm/macARM methods and retained ledger, exact-source/JAR provenance,
matching-head hosted supported jobs and original public profiles, and parent
adoption remain open. Task591,549/548/541/526/557 and JCK are not closed here.

No compiler, script, numerical/helper/model/certificate, probe, test, build,
benchmark, process inspection, Git/tracker mutation or mutex action occurred.
All owned metadata/read/write calls are terminal; no background campaign or
future-window wait is held. Later source composition/runtime belongs to root.

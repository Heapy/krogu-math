# 582 finite source implementation handoff

SOURCE ONLY, UNCOMPILED / UNRUN. Candidate root:
`/private/tmp/krogu-math-task-582-u84RlRs4/root`.
Base `f473ae7741973da1dd75633721994559ec3c085d` plus the parent's twelve581
overlays; task582 comment2345/source-grant SHA-256
`86cc2c2b2c5f2655c58f8d88f435d35260e16251a2dbb0de18c8dfcc4bd641fd`.
Accepted proposal:
`4b6bcad97a5ad0865615a444ae3e1a97db371128d989027cee57a1835b4bc33c`.

Only the following seven source/document files are owned. The six review-set
files were frozen for the parent's independent read-only reviewer before this
separate handoff was written; no reviewer acceptance is presumed here.

| File | SHA-256 |
|---|---|
| src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt | `00e392444ce4b13122ec6dcb7ccbee357b437da1e6554cc78876e909346c1599` |
| src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt | `66bb59deeeceb4f664681f99ddacc490c3054b5c42c539d3b8a3a44ed2ddb7c2` |
| src/commonMain/kotlin/io/heapy/krogu/math/DecimalDivision581.kt | `00557b2517c70f9294ed1be484177962c441841334585cba14ac7a94c647fa6f` |
| src/commonTest/kotlin/io/heapy/krogu/math/DecimalIntegralQuotient582Test.kt | `f3736aae2cca21147dbb878a64bef77277746b899a9b88653ec08242d0b24219` |
| src/jvmTest/kotlin/io/heapy/krogu/math/DecimalIntegralQuotient582SubtypeJvmTest.kt | `688710730bc63cdb0c6940f78e19958295c02af550366a007f2dfa48c6db45f0` |
| docs/integral-quotient-582-implementation.md | `150c94a9e895e37ff5bf2bf1158a5d04cc1b3c83a875ed4e679f46dcbf85e70f` |
| docs/integral-quotient-582-handoff.md | Supplied separately; no self-referential pin |

The source changes are the no-context captured-metadata seam, one nonvirtual
stored integer bridge, one separate integral stream and its zero-appending
primitive. S≥0 uses existing supported integer division; S<0 preserves symbolic
trailing zeros and publishes the selected cohort only. A proved extreme-scale
preflight reuses UNCHANGED581.largePrecision before decimal conversion, retaining
exact supported results and avoiding the tiny1/3@MIN huge-stream regression.
The implementation proof contains its denominator-factor, scale and range
argument, plus loop/cell/index/alias/lifetime bounds and independent oracle limits.

All twelve inherited581 originals were copied read-only before source edits:
`/private/tmp/krogu-math-task-582-u84RlRs4/evidence/originals-36aIDirz`.
Manifest `originals.sha256`:
`17115d0dfff86fe7e5856b4d8f135f600ac3041e76a6a4bbdd36933c37e72b6b`.
Its twelve entries were independently rehashed against that snapshot, all OK.
The nine inherited files outside the three owned production files match their
original pins in the candidate. The old rounded/largePrecision/publish and
reached primitive method bodies are retained; the helper diff contains only
the separate method and private primitive additions. The finite-context integral
overload is text-identical by direct extraction/diff. No existing integer kernel,
field/cache, public declaration, serialization, fixture or consumer was edited.
Jdk25Vectors.kt still has `cb4d2327da9850e347dd1db6b96519bb228a0072ccd778b8372a647cd7f1ac4f`
and compares byte-identically with the retained pre-consumer original.

Narrow three-file production diff:
`/private/tmp/krogu-math-task-582-u84RlRs4/evidence/production-582.patch`, SHA-256
`2af8aa12c296a47500de69078ee82a995cc5e0c4301e86d9643fbc26529de9da`.
The new common test has four methods; the JVM current-format subtype test has
one. All five are UNCOMPILED/UNRUN. They use independent bounded Long/rational
results/literals, signed preferred cohorts, direct/public seams and input isolation,
terminating preflight neighbors and proved unsupported tiny thirds. Existing
probes remain active; no fixture result has been relabelled or discarded.

Remaining boundaries: S outside Int follows the original path, without a new
LOW rescue/clamping rule. Finite-context p>0 residual/product/precision/cohort
and pair/remainder composition are separate P4 obligations. Truthful-metadata
replacement contextual-divide probes may expose changed inherited internal-call
fields; exact classification is a later parent decision, not a blanket subtype
waiver. Direct explicit caller override/super result/exception behavior remains
covered independently. The giant150000000-digit witness is symbolic and UNRUN.

The original581 failed campaign and the parent's fresh separate campaign remain
their own evidence; no numerical outcome is inferred here. Independent source
review, compilation, fresh artifact, all-target exact source/XML audits, consumer
policy/composition, full integration and hosted gates remain. This source grant
does not grant runtime. No script, helper, numerical operation, certificate,
compiler, probe, test, build, measurement, Git or tracker action was performed.
Publication and validation trees were untouched. All owned calls are terminal;
the finite author milestone finishes without waiting for a future window.

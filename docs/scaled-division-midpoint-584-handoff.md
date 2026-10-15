# 584 finite source handoff

Candidate `/private/tmp/krogu-math-task-584-vEIpPEW2/root`, parent source-only
grant2363. Base da6086342165711ac800e0c5c515eb2e6df7aae5 plus frozen233-source582
successor. All new source/tests are UNCOMPILED / UNRUN. Changes are not committed.

## Frozen owned files

| File | SHA-256 |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt` | `a925bf9144044fe23e4bb618b6777b7f2f90d1f591d9a0856f3651bcaaaf3e50` |
| `src/commonTest/kotlin/io/heapy/krogu/math/ScaledDivisionMidpoint584Test.kt` | `f3b76ad72fa1215646749f0716d9ec1a55efca6ff17c7061f4c7109c42f410bb` |
| `docs/scaled-division-midpoint-584-proof.md` | `f66178a2c1f7d0e438ea33af1b5f79b1a1f7a5d25ec4da0e999d4d00009b01cc` |

This separate handoff is the fourth owned file; its final hash is returned in
the parent message, avoiding a self-referential hash.

## Original preservation and narrow diff

Before any source edit, original BigDecimal was copied to unique read-only
`../originals-Gjr7NwEx/BigDecimal.kt`, SHA-256
`00e392444ce4b13122ec6dcb7ccbee357b437da1e6554cc78876e909346c1599`.
The read-only `original.sha256` manifest SHA-256 is
`f29941afe31aa5f73a061692f627cd138dbb309ef0f1b3a6c2649c1d64836869`.
The directory is mode0555 and both files mode0444. The inherited parent's
233-line all-OK source-check log SHA-256 is
`a798d662b2f97ad1821c27ab28fb1293aeea2065ceb8795c619dfafd00faf817`;
it remains historical setup evidence, not a new validation run.

The entire production diff is the following midpoint replacement in the private
roundedDivide helper:

```diff
-            val half = qr[1].abs().shiftLeft(1).compareTo(denominator.abs())
+            val remainder = qr[1].abs()
+            val divisor = denominator.abs()
+            // For 0 < r < d, compare(2r, d) = compare(r, d-r); both operands fit.
+            val half = remainder.compareTo(divisor.subtract(remainder))
```

Plain diff against the preserved original shows only this hunk. All other
BigDecimal bytes, including581/582, mode selection, zero remainder, sign,
quotient parity and signed increment, are unchanged. No production edit was
made to another file. Read-only pins also confirm BigInteger
`66bb59deeeceb4f664681f99ddacc490c3054b5c42c539d3b8a3a44ed2ddb7c2`,
DecimalDivision581 `00557b2517c70f9294ed1be484177962c441841334585cba14ac7a94c647fa6f`,
582 diagnostic policy `ce9751b8576299fe0bc4324023c79363c01a50308212acd8f5d9baa450a669ab`,
and integral dispatch consumer `ae3c8ec204fc3b077c9d3b9c59c400747e28fe9c16f2ac429dbe2b7ced643d43`.
No fixture, old test, public API, field/cache, serializer, or publication tree
was edited. Other workers' changes were preserved.

## Proof and prepared tests

For every valid nonzero residual magnitude0<R<D, comparison of R with D-R has
the same sign as comparison of2R with D. Both new operands are smaller than
supported D. Existing same-sign subtraction and normalization therefore avoid
the unnecessary L+1-bit midpoint; input words remain immutable. All sign/mode,
tie/parity, zero and requested-cohort behavior is retained. Genuine final-result
overflow is still rejected. The original L-bit giant witness stays symbolic and
UNRUN, with earlier normalization/string/resource limits explicitly separate.

Two independent common test methods prepare5376 public calls, derived on paper:
3936 signed bounded-Long rational cases spanning denominator2,3 and neighbors
of2^30/2^60, all rounding modes, zero/exact residuals and odd/even quotient ties;
1440 small-input endpoint-scale compensation cases with actual shift only-1/0/+1.
The oracle uses bounded primitive doubling, not production complement arithmetic.
It asserts coefficient, requested scale, scale0 canonical text, expected
UNNECESSARY exceptions and input coefficient/scale immutability. No giant
allocation, synthetic smaller range cap, incidental callback-count/noncall
promise, legacy serialization or test that merely asserts an override was added.

The proof explicitly notes the new internal subtract leaf on abs(divisor):
truthful current-format subtype arithmetic composes conditionally on correct
abs/subtract/compare/division/sign results. Poisoned probes retain their separate
classification obligations. Direct caller overrides/super behavior is preserved;
no existing consumer is waived or silently adapted.

## Remaining gates and terminal status

Independent source review is next. Root owns any later compiler/tests/full-target
gates, current-format composition replay, consumer-difference classification,
integration, publication and task disposition. No all548/public-TCK/JCK,
performance or bounded-heap acceptance is claimed. The separate P4 residual
product obstruction remains outside this patch.

Only standard source text reads/copies/diff/count/hash and permission operations
were used. Two initial guessed file reads (source-grant.txt and a test filename)
were absent and caused no mutations; the parent message supplies the actual grant
and exact known source/tests were then read. No script, compiler, helper,
numerical/model, probe, test, build, process inspection, Git or tracker operation
was executed. All preparation calls are terminal; no owned background work or
future-window waiting remains. Source is frozen pending parent scope.

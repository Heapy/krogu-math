# 592 finite SOURCE handoff

SPDX-License-Identifier: Apache-2.0

Candidate `/private/tmp/krogu-math-task-592-DTPKLyOb/root` is a separate clone of
frozen591+549 source249. All592 production and test bytes are frozen and
**UNCOMPILED / UNRUN**. This is not adoption, runtime acceptance or a task state
change. Main/root248/591 originals and other workers' files are untouched.

## Preservation and exact payload

Original249 manifest:
`dcc46f2f58b0f1aa0dc7d26f88c9200d643344241d6a8a1c59c01d985447217d`.
Before edits, all249 sources were archived read-only in
`../evidence/original-source249.tar`, SHA256
`cca8859897f20be27b13eddea34a45ba5f16c287933b4a934108ccd619d3c95e`.
Original BD/BI/DecimalDivision581 and task body are separately preserved.

| Owned file | SHA256 |
|---|---|
| src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt | b1570ccd8cfb0e640e4566ce727d7d1f7ed18cc4346f90d2378b72834ac2323e |
| src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt | 652262fa7f92f9766dea92fd2143224449d509968228d8b21f6d85ee86cf2441 |
| src/commonMain/kotlin/io/heapy/krogu/math/DecimalDivision581.kt | 553de7bece22f88526e029d0a931cf0fccc99a73d90534e588911b19d46bc193 |
| src/commonTest/kotlin/io/heapy/krogu/math/DecimalFixedScale592Test.kt | bb0ff8f4d57be0cca92bfa88512ef286a30ba305a31b485a203b0d4dcd0852fc |
| docs/decimal-fixed-scale-592-proof.md | 644d604f6d1a73f3ab93a8e471b09ff387e0b9f44a836ac1b7de2306ef942f73 |

This handoff is the sixth owned file; its hash is external. Complete250-source
manifest `../evidence/candidate250-sources.sha256` SHA256:
`5f99c5c5ee792180ae64b6448c82e034e4d13e21e7fedc1b6875e6c0e8bd92ef`.
All250 candidate hashes, all246 nonowned original hashes and all249 frozen591
hashes verify. Source pathset is exactly original249 plus the new common test.
Three narrow original-vs-candidate source diffs are retained in evidence/.

## Behavior and deliberate boundaries

After the existing public divisor-zero guard, explicit-scale enum divide
attempts a stored-only raw quotient **before** disposable outer scale checks
and quotientExponent's whole coefficient Strings. Its BI bridge keeps the
entire original body for zero stored operands or sufficient supported old
products: abs(shift)<=K and both bit-length+4*raise bounds fit L.

Otherwise the new fixedScale592 method streams original numerator digits and
symbolic zeros against the unscaled denominator. It preserves all mandatory
coefficient zeros, one guard+sticky rounding, all8 modes/sign/parity/carry,
explicit zero publication and requested scale. Necessary output lower bound
n-digits(B)>H rejects before billions of virtual steps; exact zero tails skip
counter work only after their required digit bound is checked. An extra carry
cell and final actual binary support check cover H-digit uncertainty. No
significant-precision cohort stripping, public scaled magnitude, smaller cap,
host/JDK fallback, cache, field or serializer behavior is added.

All previous DecimalDivision581 methods/private primitives and the entire
divideScaledCoefficients body remain byte-identical. Context581's
checkSourceSign=false fallback is not rerouted.591/590/BI549 and BD549 method
bodies are preserved; only their file line numbers move. Exact/context/integral/
residual/FloatDouble/primitive conversion bodies and all existing tests/data/
consumer assertions/build configuration are untouched.

On unsafe corrected paths, later internal sign/abs/toString/normalization/
product observations may be skipped. The public divisor-zero callback remains
first; safe fallback retains its entire old sequence. Existing observations
remain retained for parent field-level classification; no blanket poison/error
waiver or rewritten raw fixture is claimed. Explicit caller override/super
results/exception identities retain ordinary language dispatch. There is no
Error catch or new exception-to-success substitution.

## Prepared tests, proof and remaining gates

Three new common methods prepare a bounded independent rational/direct-helper
matrix with checked Long products, all signs/modes, tie/parity/guard0/unread-tail,
base1e9/30/60-bit and borrow/carry boundaries; literal exact1-with100-zeros
padding; genuine tiny-coefficient endpoint raw/public and legacy controls;
necessary-output rejection at actual maximum positive logical shift; and
explicit caller override/super identity controls. Source arrays are checked
unchanged. No fake production cap or result from another production decimal
operation supplies the numeric expectations.

The A/A, A/(A-1) and supported10^K outer-stage witnesses remain symbolic
**UNEXECUTED**. Bounded true negative-shift endpoint raw calls are prepared,
not run, and are not evidence of executing those giant positive witnesses.
The code can still require expensive input conversion/division and large
simultaneous buffers; the proof keeps548 allocation/time premises explicit.

Independent paper challenge0d90be1caee506abafad9e61735cfb97987e77baed55649e038711cfebf9e68c
accepts the original defect/conditional stream mathematics, not this source.
Root read the frozen source/test and reported no issue; independent source
review is separately in progress. No independent acceptance or runtime result
is fabricated here. Root's terminal narrow591 four-method PASS belongs only to
its unchanged source249 snapshot and is not592 evidence.

Fresh independent source/proof/test review, compilation, full composed local
four-target method/ledger inventories, JAR/source provenance, matching-head
supported CI/public profiles and parent adoption remain open. Three new common
definitions imply12 prospective extra local method executions, not PASS counts.
No whole592/548/549/541/526/557 or JCK acceptance follows.

No compiler/script/numerical/helper/model/certificate/probe/test/build/benchmark/
process inspection/Git/tracker/mutex execution occurred. All owned metadata/
source-preparation calls are terminal; no background campaign or window wait is
held. Later changes/runtime require a separate root scope.

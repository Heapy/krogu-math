# 590 finite SOURCE handoff

SPDX-License-Identifier: Apache-2.0

Candidate: `/private/tmp/krogu-math-task-590-ps6OIcvb/root`.
Base47447f2111d44735de374ec98d0a6a099a08888f/source247529cc142 was copied before
editing. Complete read-only original archive:
`../evidence/original-47447.tar`, SHA256
aa50ddf75ac39ddb645d8a13f57f1d67b3c28f4405df973a1fcc2bfd318a798c.
Original548 audit fa14a814 remains read-only and unchanged. This is a private
candidate, not publication, implementation acceptance or a task transition.

## Payload

| File | SHA256 |
|---|---|
| src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt | 4d325b3272c3b9bd120f919bf7a60f7b46523b1266cd304f4cf1020c0a3651cb |
| src/commonTest/kotlin/io/heapy/krogu/math/DecimalExactLowResidual590Test.kt | c76002dd62ea2feb88697370ec814eda277922c7c3ed4994779db0ef9bda8388 |
| docs/decimal-exact-low-residual-590-implementation.md | cf532538218629088e8d9bc80d43e6c753f4fa6d3022ffcc4d6b5ab888030de8 |

This handoff is the fourth payload file; its hash is external to avoid a
self-hash. No other existing file is changed. BigDecimal remains5731c8df6ba3,
DecimalDivision581 remains e974af13310f and all246 other existing source files
verify against the original247 manifest. Exact source pathset is original247
plus the one new common test class, with no omissions/extras.

`../evidence/candidate248-sources.sha256`, SHA256
3bb47eaeb3a19deab9f5f88a54da2b8fc58f7f68c28e83330b10d86f2fbdcc34,
has248 entries, all verified in candidate248-source-check.log. The deliberate
initial original-manifest check after editing records exactly one mismatch,
BigInteger; unchanged246-check.log separately verifies every nonowned source.
`../evidence/BigInteger-590.diff`, SHA256
173c480c7c48babe0af96248bd9996de7538e5c64ad8f7272051778e6b4c92fc,
contains only the two-line selection and new exact-zero helper.

## Result and boundary

After the unchanged safe-old predicate, outside-LOW preference selects a
stored-value certificate. It recomputes an exact quotient via unchanged581 on
fresh positive base wrappers, compares candidate sign/adjusted exponent/exact
decimal significand, and returns zero at checked max(Sa,t+Sb). Internal own-base
ArithmeticException declines; caller methods are outside that catch and all
Errors remain uncaught. Wrong/nonexact candidates never become passing values.
The quotient object/cohort is preserved by callers. No public product,
out-of-range natural value, expanded string, LOW clamp/rescue, new cap or field
is introduced. Existing in-range583/582 and kernel bodies are byte-preserved.

The accepted symbolic m750000000/pMAX witness now reaches this certificate:
the no-context quotient Q remains the same; finite admission and both pair/
remainder families can retain Q and supported zero@-1397483648 without10^m.
This is source composition, not execution. The independent4583cefa paper
acceptance supports the original obstruction/reachability; it does not review
this new code. Fresh exact recomputation may add substantial time/memory, and
no full-range heap feasibility or speedup is claimed.

## Tests prepared, not run

Three common methods provide56 direct exact successes plus168 wrong-candidate
declines, ten out-of-domain declines, and324 bounded public endpoint entrypoint
calls across all signs/modes. Expected terminating rational coefficients and
residual scales are independent literals. A candidate above30 bits exercises
multiword metadata; padded equal cohorts exercise the stored significand
comparison. No synthetic production maximum or giant input is used.

The public bounded controls use the retained safe path. Positive execution of
the new outer dispatch is not fabricated from them; direct internal calls test
the actual new certificate, while the giant dispatch remains symbolic. Existing
fixtures, callback assertions, data, policies and all build settings are intact.

Independent code/test/proof review, compilation, all four local targets,
fresh artifact/provenance, exact-head CI/public profiles and parent adoption
remain open. All new source/tests are UNCOMPILED / UNRUN. No compiler, script,
helper, numerical operation, certificate, model, probe, test, build, benchmark,
Git mutation, tracker transition or numerical-window action occurred. All owned
metadata/read/write calls are terminal; no background job or future window wait
is held. Task590 and broader548/541/526/JCK disposition remain parent-owned.

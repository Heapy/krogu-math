# Inexact square-root precision: task587

The bounded diagnostic observed a shared representation difference in own
source240 and BellSoft JDK25.0.4+9-LTS. With precision8, the irrational input
`3.999999999999` produces `2.000000` (coefficient2000000, scale6, precision7)
under UP, CEILING, HALF_UP, HALF_DOWN and HALF_EVEN. The independent p-digit
expectation is `2.0000000` (coefficient20000000, scale7, precision8). The values
are numerically equal. This is a representation issue, with no observed
numerical error or diagnosed JDK implementation cause.

## Contract disposition

The [Java25 class contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
includes infinite square-root expansions in its precision rule. The rule
allowing fewer digits at the preferred scale applies when the exact
mathematical result fits the context. An irrational root does not meet that
condition merely because rounding selects the number2. The method's
[approximation, preferred-scale and ulp clauses](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#sqrt(java.math.MathContext))
do not expressly relax result precision. The general allowance for
method-specific semantics warrants checking explicit exceptions; it does not
establish an unstated zero-stripping exception.

The project therefore adopts p-digit output after inexact rounding, including
significant trailing zeros, under its compatibility-modulo-JDK-bugs policy.
Independent source-plan and actual/normative reviews accepted that reading.
Exact finite roots and zero retain their existing preferred cohorts. Carry
normalization retains p digits by exactly dividing a p+1-digit coefficient by
ten once; it does not authorize further inexact zero stripping.

Historical579 premise R allowed compression of inexact zero tails. Its earlier
symbolic compressed-cohort witness and observations remain historical evidence,
conditional on R. They do not prove permission to choose that representation.
Under the adopted rule, an inexact p>646456993 output requires an unsupported
coefficient. Any resulting necessary bound must follow the complete finite-root
classification and truthful stored-value checks; it cannot reject a small exact
root merely because the requested precision is large. Raw579 arithmetic remains
necessary for supported outputs whose normalization radicand is oversized.

## Actual bounded evidence

One initial V2 compilation/execution, root scope2397, session21175, exited0
without retry. Each of three inputs was evaluated at p8 with all eight modes
and p0/HALF_EVEN, through both direct own and bootstrap JDK calls:54 unique
primary calls,46 VALUE rows and8 expected SQRT ArithmeticException rows. There
were zero numerical/type failures or unexpected exceptions. Exactly ten rows
had the shared cohort difference above; the other36 VALUE representations
matched independent literals. Exact `4.000000000000` remained `2.000000`.

The driver validates observed coefficient/scale/precision and canonical text
independently, and separates numerical equality from representation. Its
exit0 means complete diagnostic capture, not cohort conformance. No provider
supplies expected values for the other. The V1 missing canonical-text check was
caught on source review before any execution; V1 remains uncompiled/unrun.

Frozen run: `/private/tmp/krogu-587-sqrt-v2-run-IcrJpEaK`.

| Evidence | SHA-256 |
| --- | --- |
| Evidence manifest,12 files | `2eeb8501b461e141ab156526dadbe36d512b2d57c7d027896eea0192da1e40f7` |
| Raw54-row observations | `124b13298034f50ecccbdf00ba922a0f963c32d690c6c26e3d1df84fe39da497` |
| V2 driver | `43dc9474a961ad59527ec1052162816f3d3b0fc7ebb06ca7510203f90737da57` |
| Source240 manifest | `e3539c0bda309350d72ebb89656a9f54e3c7f720fe943616153050e2be493aa9` |
| Own JAR | `1cacc5b41ede2e154e9c7baf948bd1748f4d424932f3fb7e4a592085b1ed00e1` |

Fresh-cache inventory preceded compilation. Pre/post provenance is identical;
source hashes/pathset, JAR, runtime, stdlib, driver and four own/four bootstrap
class origins were checked. Flags include256MiB SerialGC. All indexed evidence
and240 source hashes were independently checked. After all calls were terminal,
root released the sole numerical mutex; list was empty at revision814.

This report adopts no production change or suite adaptation. Source587,
independent code review, affected-record classification, local full suites and
matching-source hosted/public gates remain open. Giant inputs remain symbolic
and unrun; this is not full542/548/541 closure or official JCK/TCK acceptance.

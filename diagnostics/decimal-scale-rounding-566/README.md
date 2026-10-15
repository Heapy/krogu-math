# Task566 narrow oracle classification

Current execution status is recorded in the
[integration report](../../docs/decimal-metadata-scale-integration-verification.md):
all four retained local selections and targeted public tests have passed; fresh
published-head CI remains pending. The UNCOMPILED/UNRUN notes below describe
the preserved source-preparation revisions, not the current replay status.

Source preparation only: the revised oracle callers and model are UNCOMPILED/UNRUN.
The parent retains the initial 381-method/17-failure JVM invocation under
`/private/tmp/krogu-root528566-validation-v1/xml-jvm-initial` and `jvm-initial.log`.
The separate baseline/candidate 96-row standalone observations in that directory
are evidence for the witnesses, not permission to waive other rows.

`PortableOracleTest.before.kt.txt` and `ScaleBoundaryOracleTest.before.kt.txt`
preserve the complete callers before this classification. Frozen generated
`Jdk25Vectors.kt` and `Jdk25ScaleBoundaryResults.kt` are unchanged. Every row is
still evaluated and consumed in its original order; all cursor end checks remain.
The JVM worker separately preserves its original consumers in `original-jvm/`.

The shared common model `DecimalScaleRounding566Expected` accepts only ordinary
BigDecimal inputs (JVM subclass tests may separately construct ordinary models
from their original stored-value literals while retaining callback assertions).
It reads coefficient text and scale, then uses independent decimal-string
multiplication, discarded-digit rounding, carry and canonical formatting. It
never calls production decimal/integer arithmetic for an expected value.

Classification is limited to:

* Nonzero setScale downscaling with distance greater than646456992, where the
  old distance/power guard rejected before deciding the subunit result.
* Finite-context nonzero multiplication with logical scale above Int.MAX_VALUE
  and exact product digit count greater than destination precision. Exact HIGH
  cohorts, unlimited multiplication, LOW scales and zero multiplication remain
  outside classification.
* Exactly-one-zero finite-context addition where the rounded source is supported
  but the preferred-minus-rounded scale distance exceeds Int.MAX_VALUE. Only the
  independently selected finite-precision padding cohort is required.

The string model bounds each input coefficient to512 digits, and selected
zero-add padding to512 places. These bound test-model work, not library support:
outside those bounds it returns null and the original oracle assertion stays
active. Product accumulation is bounded by512*81 plus carry, safely within Int.
All scale arithmetic uses Long. No giant decimal powers are allocated.

Within selected rows the frozen diagnostic must remain ArithmeticException or
already equal the independently required value. When rounding is forbidden or
the selected final scale is outside Int, the required category remains
ArithmeticException. Only that selected category is normalized; frozen messages
remain in the generated fixture and assertion labels, and other messages,
callbacks, identities, classes and exception types are not waived. The two
common consumers do not alter any callback classifier or generated fixture.

Full replay, row counts and any additional conflicting assertion require parent
review and actual runtime evidence; no broad task526/541 completion is claimed.

## Four truthful T scale observations

The first full JVM retry left one failing method at the first of four frozen
keys, exactly `(T, op, 1, 2147483647, 0, HALF_UP)` for op `integer`, `scale`,
`enumScale`, or `intScale`. Their receivers' getters only trace and call super;
there are no replacement values or thrown callback errors in these keys.
`PortableOracleTest.before-truthful-four.kt.txt` preserves the complete caller
before this second classification (SHA256
`b72c39408c8f5774b128736ce6c5b4612b9a6b3845a389e5855dedfcfce3392e`).
The original pre566 archive and generated fixture are untouched.

Each exact key is required once, with a duplicate check and complete set equality.
The original full outcome/message/trace is asserted literally, and both frozen
and actual full strings are printed as `566_TRUTHFUL_SCALE_OBSERVATION`.
The independent ordinary string model requires ArithmeticException for
`integer` and exact `scale` (UNNECESSARY on nonzero1*10^-MAX), and coefficient0
at scale0 for the two HALF_UP overload calls (strictly below half).

The explicit wrapper trace prefixes remain exact: `integerExact;intScale;`,
`scale;intScale;`, `enumScale;intScale;`, and `intScale;`, respectively.
Only a suffix composed of zero or more `sign;` reads is observational; no
precision/getter substitution, extra wrapper dispatch, callback exception,
other result type or other T key is accepted by this branch. The new internal
sign-read multiplicity and ArithmeticException wording cannot replace the
independently required value/category. Direct caller replacement/exception and
truthful566 controls remain enabled separately. Revised source is UNRUN pending
parent and independent review; no production or fixture change accompanies it.

## Stored-value HIGH L multiplication and complete ordinary replay

The second JVM retry retained a remaining first mismatch at
`(L,multiply:-1,-1@2,123@2147483647,1,HALF_EVEN)`. The full pre-revision
portable caller is archived as `PortableOracleTest.before-high-L.kt.txt`,
SHA256 `e6b9c69e6b5e26cf8165c06d86c228a8e7b7d3286f3c9732b78b6b7a9a50bec2`.

The added branch accepts only L operations `multiply:-1`, `multiply:0`, or
`multiply:1`, finite positive precision, and a non-null HIGH/mandatory-reduction
answer from the same independent bounded string model. Model operands come
from the original constructor coefficient/scale literals, not the overridden
sign/precision getters. The inherited context multiply reads those stored
coefficients; this branch does not waive an invoked getter or callback.
Other L keys, exact/unlimited calls, zero products and unproved HIGH cohorts
still use their original assertions.

For each selected row the complete original
`!ArithmeticException:Underflow|context;` field is asserted literally. Full frozen
and actual strings are printed as `566_HIGH_L_OBSERVATION`. Successful values
must match the independent text, distinct-receiver flag `false`, base-result
flag `true`, and exact `context;` trace. Required errors must remain
ArithmeticException with the exact same trace. No L callback multiplicity is
made observational.

Ordinary final row equality now accumulates the mismatch count and the first80
full diagnostics, then requires zero after the complete loop. Every row still
evaluates, every original expectation remains, and schema/key bijections,
callback assertions and correction-control assertions retain immediate failure.
This exposes subsequent ordinary mismatches without pruning any selection or
turning a failure into a pass. Revised classification/replay remains UNRUN.

# Task 541 normative finite-control review

Current root adoption retains the historical source-preparation review below.
The original revision3 review document hashes to
`2c33efad34f2ea32da0cebff228a9dbd5627edfd680b9df109ee3cb159424063`.
Both accepted test files subsequently passed all four complete local target
gates (1440 method executions across targets), with all four independent actual
reviews accepted. See the [current validation report](decimal-overload-541-verification.md)
for evidence and future published-head CI gates. This finite tranche does not
close task541's G1–G6/P1–P6 or establish full compatibility/JCK conformance.

## Historical source-preparation review (preserved)

Status: SOURCE ONLY; awaiting independent review. These artifacts are uncompiled and unrun. No production
source, existing test, fixture, runtime, Git state, tracker state, compiler,
script, helper, numerical program, foreign implementation or bytecode was
changed or executed.

## Scope

`DecimalOverloadDispatch541Test.kt` is one bounded common-test tranche for the
39-entry inventory. Its three methods name finite controls for:

1. exact/context binary arithmetic, all divide forms, integral/remainder/pair,
   setScale, point movement and scale-by-power-of-ten;
2. round/plus/abs/negate, ordering, all three text forms, strip-trailing-zeroes
   and ulp;
3. caller-owned override result and exception identity for representative
   public entries.

Entries 01–18, 26–37 and 39 have ordinary and truthful direct receivers in the
new dispatch class. Unary entries19–25 and zero stripping38 use ordinary
receivers there; the separate prepared ulp/plus suite adds its own selected
truthful and actual-super coverage. Selected actual `super` helpers and the caller class cover
explicit base entry and virtual replacement; this does not claim a separate
truthful override or universal internal `super` proof for every row. The test
uses real Kotlin `super` expressions, literal/algebraic representations, signs,
unequal scales, exact addition and zero cancellation, and valid error cases. The prepared
`DecimalUlp541ContractTest.kt` is copied byte-for-byte from the accepted ulp
source preparation and independently covers every selected stored scale,
ordinary/truthful inherited calls, actual `super.ulp()`, and documented
no-context `plus()` identity.

## Contract boundary

The tests assert public numerical, scale, error and documented identity rules.
They do not assert internal callback order/count, poisoned metadata getter
behavior, result runtime class, allocation/cache behavior, or implementation
forwarding that the public API does not require. Caller overrides are checked
only for explicit base-typed virtual selection, argument/result identity and
exception identity.

## Independent finite expectation ledger

The direct arithmetic fixture uses `x = 120@2` and `e = 120@2`, so
`x+e = 240@2`, `x-e = 0@2`, and `x*2 = 240@2`. With context precision 3,
`x+180@2 = 300@2` and `x-180@2 = -60@2`; the retained three significant
digits fit the addition result exactly; subtraction keeps its preferred scale.
For `d = 40@2`, the quotient is numerically3. Exact/context division and both
integral forms return `3@0` here. The two rounding-mode-only divide forms
retain the dividend scale2, so return `300@2`; the explicit-scale forms request0
and return `3@0`. `x - 3*d = 0@2`, which is the exact remainder and
the second pair slot under precision 1. The exact quotient preferred scale is
`2-2=0`, while the remainder/product scale is 2.

The scale controls are direct shifts of `120@2`: setScale(3) is `1200@3`,
movePointRight(3) is `1200@0`, movePointLeft(1) is `120@3`, and
scaleByPowerOfTen(1) is `120@1`. Unary controls use `-120@2`: no-context abs
and negate each return `120@2`. With p=2, round/plus return `-12@1`, while
abs/negate return positive `12@1`. No-context plus preserves the receiver.
The text input `12300@-1` has canonical `1.2300E+5`, plain `123000`,
engineering `123.00E+3`, and ulp `1@-1`. These are algebraic/literal
expectations and do not call another decimal implementation.

The new remainder/pair examples cover zero remainders only. Retained
`DecimalOverloadContract541Test` lines36–67 separately cover signed nonzero
`225@2`, all eight rounding modes and four ordinary/truthful masks, exact
`30@1` versus finite-context `3@0` quotient cohorts and error controls.
That retained coverage is not claimed as a new test in this tranche. No broad
rounding-carry or cancellation-neighborhood proof follows from these literals.

## Acceptance gates

Independent source review remains open for this milestone. Compilation, supported
target execution, source/XML method inventories, and published-head CI remain
open. The tranche does not close P1–P6 full-domain proofs, extreme resource
behavior, or the remaining G1–G6 classification boundaries. No production
change is proposed from this source review.

# Decimal sticky equality without an unsupported divisor (586)

SPDX-License-Identifier: Apache-2.0

This source-only repair concerns the private tail preparation before the
task585 remote sum. Its giant witness is symbolic and UNRUN. No compiler,
script, numerical helper, probe, test or build has run for this milestone.
The independent implementation uses no foreign numerical implementation.

## Reached obstruction

The baseline is BigDecimal
`1f65c1d99d3144989068dfbdc8ffa70a32ceab2fc0a6ea2a4eb3ac142e03bf3e`
and BigInteger
`d6d531742a7b12bd6cc09cf6b98daca11f70b5207687ecd4c04c5a11d655a9b2`.
The isolated repair binds BigDecimal
`9ddd72736ab1b8e269db5de9a869a7bcfc5159c89c7f9c217fadff86e71d94c7`
and its separate common test
`edb718581affff76260e3930e9a798b7e170918a03f24298c7f09fc11c8a667f`.
This is the586-only source; composition with585 needs a new complete source
manifest and is not falsely identified by that BigDecimal hash.
Use the existing [566 range lemma](decimal-scale-rounding-566-proof.md):
L=2147483647, K=646456992, H=K+1,
`10^K<2^(L-3)` and `2^L<10^H<2^(L+1)`.
A=5*10^K is supported. Both [A,0] and [A,H] are valid inputs; the latter
has value0.5. Their precision1/DOWN sum requires [5,-K], also supported.
No result scale overflows.

The original finite addition has scale gapH>=10000 and adjusted-exponent
gapH>4, so selects its remote branch. Its alignment check sees leading
position1 and threshold2 and succeeds. Working scale is0. On the remote
operand, drop=H and reported precision=H. The old equality branch requests
tenPow(H), rejected by the coefficient ceiling before task585's sum seam.
This is an own-source range defect, not an observed JDK runtime result.
The [public arithmetic/cohort contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
requires the supported rounded result; its logical intermediate does not
mandate this public divisor.

## Exact shortcut and eligibility

Let D be the exact stored digit count of a nonzero coefficient c. If drop=D,
`0<abs(c)<10^D`, so truncating division by10^D has quotient0 and nonzero
remainder c. The old successful branch therefore produces a signed-unit
coefficient at workingScale+1. The shortcut constructs exactly that same
tail, with no decimal divisor, quotient or remainder allocation.

The existing public precision() result is captured once. The shortcut
requires drop=reportedPrecision=storedPrecision and a nonvirtual stored
nonzero check. decimalCompactValue() is nonvirtual: it returns0 exactly
for stored zero and either a nonzero Long or null for a stored nonzero
coefficient. Thus null!=0L correctly permits large nonzero coefficients.
This guard is important because stored zero has precision1: a lying
signum must not turn the old zero-remainder result into a signed unit.
All mismatching precision inputs and that stored-zero class retain the old
branch. No new public metadata method is called.

The existing initial signum/scale short circuit remains unchanged. In the
eligible nonzero case the old remainder would be nonzero, so it would call
signum once more; the shortcut retains exactly that call and uses its
returned number. A later throwing signum propagates without a catch.
An internal coefficient division callback may be avoided; its inherited
trace is not a documented direct caller guarantee. Explicit override,
super, argument, result and exception obligations are unchanged. Truthful
current-format restored coefficient subtypes are eligible without a class
test; no legacy serialization requirement is added.

At the only caller, workingScale>=the dominant stored scale>=MIN. Equality
also gives workingScale=remoteScale-D<=MAX-1. Hence workingScale+1 is inInt;
the existing checkedScale call is retained. No endpoint clamping or new
precision/magnitude cap is introduced. The existing sticky-tail rounding
argument, task585 sum certificate and548 resource premises remain separate.

## Prepared bounded behavioral checks

DecimalStickyEquality586Test has two common methods, UNCOMPILED/UNRUN.
The coefficient consisting of5 followed by9,999 zeros at scale10,000 is
exactly0.5. Adding it to1250000 at precision2 goes through the actual remote
equality path, without lowering any production limit. The two neighboring
precision-two candidates are1200000 and1300000, with midpoint1250000.
Four sign pairs and all eight modes give independently determined literal
coefficient/scale results; subtraction tests its existing negate/add
delegation. UNNECESSARY requires an arithmetic error. Byte snapshots and
stored coefficients/scales verify operands remain unchanged.

The second method uses9,999/10,000/10,001 digits and both operand orders.
The first retains the close route; the latter two reach the equality
shortcut. All expectations follow from the half-unit value and midpoint,
not from the production rounding helper or a fake smaller coefficient cap.
There are64 prepared calls in the first method and24 in the second.
These are source counts, not execution results.

## Remaining acceptance

Independent review must bind the actual source and both test methods,
including the stored-zero and false-precision guards. Composition with585
must retain its accepted exact/close/post-sticky seams and preserve all
existing fixtures. A separate fresh four-target JVM/JS/Wasm/macOS ARM
campaign and supported CI are still required. No giant allocation, all-input
execution, performance, official JCK or complete541/526/548 claim is made.

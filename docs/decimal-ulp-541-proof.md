# Task 541: row 39 ulp source argument and bounded common controls

Status: SOURCE ONLY; the accompanying three test methods are UNCOMPILED and
UNRUN. This preparation changes no production source, existing test, fixture,
inventory, runtime tree or evidence. It targets the immutable V3 candidate at
`/private/tmp/krogu-573525-combined-source-v3/candidate`.

## Public requirement and scope

The [Java SE 25 ulp contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#ulp())
defines the result for both zero and nonzero operands by the representation
`[1, this.scale()]`, which is positive for every scale. The
[no-context plus contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#plus())
separately specifies returning the receiver itself, preserving its scale.
These public API clauses were read on 2026-10-15; no JDK or foreign numerical
implementation source or bytecode was read, and no JDK probe was run.

The receiver domain here is an ordinarily constructed valid decimal, or a
truthful subclass whose inherited metadata agrees with that representation.
The claims apply when the caller selects the base implementation, including a
real Kotlin `super` expression. A caller replacement override has its separate
language dispatch obligation; its fabricated numerical result is not evidence
against the base arithmetic law. No promise about internal calls to `scale()`,
`signum()`, `precision()` or `unscaledValue()` follows from these clauses.

## Complete source argument for the base row 39 operation

Write the valid receiver as stored coefficient `a` and stored scale `s`, with
`s` any Int. V3 BigDecimal.kt:522 is exactly:

```kotlin
open fun ulp(): BigDecimal = BigDecimal(BigInteger.ONE, decimalScale)
```

1. `decimalScale` is the receiver's private immutable Int field. For the stated
   ordinary/truthful domain it is `s`; no negation, addition, subtraction,
   narrowing conversion or scale adjustment occurs. In particular neither
   Int endpoint is converted to its out-of-range negation.
2. Own BigInteger.kt:1005 constructs `ONE` with sign 1 and the single magnitude
   limb 1. It is an ordinary BigInteger, not a caller subclass coefficient.
3. The primary decimal constructor, BigDecimal.kt:10–16, retains an ordinary
   BigInteger coefficient directly and stores the supplied Int scale directly.
   Its subclass-coefficient normalization branch is not entered for this own
   ONE. It performs no exponentiation, decimal rescaling or receiver-coefficient
   copy in this route.
4. The result's stored representation is consequently exactly `(1,s)` for
   every valid stored receiver coefficient `a` and every representable `s`.
   Its mathematical value `1 * 10^(-s)` is positive, including at both Int
   endpoints. This is a symbolic value statement: no power of ten is allocated
   or exponent computed in an Int. The source never examines `a`, so zero,
   either sign, digit count and trailing zeros cannot alter the result.
5. Only a new result is constructed; the receiver's immutable fields are not
   written. No result object identity or allocation-count promise is inferred.

The result coefficient has one limb and its scale already fits Int. Thus this
route has no scale-range or coefficient-range obstacle dependent on the input
magnitude or scale. This is a source argument, not a measurement or a promise
that allocation can never fail from environmental resource exhaustion.

This establishes the row 39 base arithmetic law across its representation
domain in the inspected source, rather than extrapolating it from selected
test points. It is not yet independent acceptance, compilation evidence or a
passed-platform certificate. A poisoned replacement metadata getter is outside
the truthful arithmetic domain; its callback presence, absence, order or
exception is not newly made a compatibility requirement.

## Narrow adjacent row 20 identity argument

V3 BigDecimal.kt:54 is `open fun plus(): BigDecimal = this`. Once this base entry
is selected, the returned reference is the same receiver for any valid stored
coefficient and scale. Its existing representation is therefore unchanged,
including zero at a nonzero scale and negative scale. No metadata inspector,
rounding kernel or contextual overload participates in this source expression.
The proof concerns `plus()` only. It does not extend identity to `plus(mc)`,
`round(mc)`, `abs()`, `negate()` or a general receiver-reuse policy.

## Prepared finite controls

`DecimalUlp541ContractTest.kt` is a new, separate common test class. The
existing three `DecimalOverloadContract541Test` methods are untouched.

| Prepared method | Independent expectations and finite boundary |
|---|---|
| `ordinaryAndTruthfulUlpKeepsUnitCoefficientAtEverySelectedStoredScale` | Seven literal coefficients: zero, both signs of one, both signs of 1200, and both signs of a 40-digit all-nine coefficient. Nine literal scales: Int.MIN_VALUE, its next value, -7, -1, 0, 1, 7, Int.MAX_VALUE-1 and Int.MAX_VALUE. Each of 63 pairs has an ordinary and a truthful metadata-delegating receiver; both inherited calls owe literal coefficient `1`, selected scale, sign `1` and precision `1`. A separate actual truthful `super.ulp()` owes the same result. Total 189 base-entry result controls. Before/after coefficient and scale checks retain the receiver representation. |
| `noContextPlusKeepsReceiverIdentityAndItsOriginalRepresentation` | The same 63 pairs and two receiver forms owe reference identity from `plus()`. A separate actual truthful `super.plus()` returns that same truthful receiver. Total 189 identity controls, with before/after representation checks. No context or cached-zero identity is used. |
| `explicitUlpAndPlusOverridesKeepCallerOwnedResultsExceptionsAndSuperEntry` | Base-typed explicit calls select caller overrides for both methods and preserve the exact replacement object or thrown sentinel identity. A replacement returning 7.00 deliberately is not an ulp numerical oracle. With those overrides still throwing, actual `super.ulp()` produces literal `1@7`, and `super.plus()` returns the caller object; its stored `-1200@7` is retained. |

Expected ulp results come directly from literal coefficient 1 and the input
scale, not from another production decimal operation, JDK output, formatting,
numeric comparison, floating conversion, setScale or a generated fixture.
The tests inspect only integer coefficient strings, never decimal text at an
extreme scale. The largest constructed input coefficient is 40 digits; all
ulp result coefficients have one digit. No power of ten, huge character output,
scale-distance-sized structure or boundary-sized magnitude is requested.

The truthful subclass delegates only four metadata inspectors and provides real
caller-written super entry methods; it does not record or constrain their
internal call count/order. The explicit replacement class records only which
of the two direct caller overrides was selected. No inherited implementation
trace, JDK callback parity, runtime result class, receiver coefficient identity,
result freshness, or constructor routing is asserted by the regressions. No
Java adapter or fabricated null is present.

## Disposition and remaining gates

Row 39's formerly absent dedicated source argument is supplied above, along
with separate ready-to-integrate finite witnesses. Row 20's documented
no-context identity receives the narrow argument and controls above. These
changes do not certify the whole task 541 inventory or silently remove its old
G3/P5 labels. Appropriate later inventory wording is: source argument prepared;
independent source acceptance and execution status separately recorded.

Remaining acceptance gates for this addition are independent source review;
integration as a new common test without modifying the existing three controls;
compilation and source-derived complete test inventories on supported targets;
and the parent-required published-head CI validation. All are OPEN here. The
new methods add three test definitions; no method has run at this milestone.

Broader G3 still includes truthful/contextual abs/negate/round/plus coverage,
stripTrailingZeros identity/range cases and their caller/super distinctions.
P5 still includes rounding carries and coefficient limits, zero/cohort rules,
movement/rescale overflow and the remaining documented identities; this row's
simple no-rescaling law proves none of those algorithms. G1/G2/G4/G5/G6 and
P1–P4/P6 retain their own residuals. The complete 39-entry audit remains OPEN.
No old poisoned-metadata assertions, null diagnostics or fixture observations
are weakened or reclassified by this preparation. The nullability migration,
Java interoperability choices and legacy serialization scope are unchanged.

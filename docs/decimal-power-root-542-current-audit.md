# Current decimal power/root domain audit (542)

This records the pre579 source snapshot below. The subsequent
[inexact-root repair and local verification](sqrt-normalization-579-verification.md)
address its concrete scaled-radicand obstruction; supported matching-head CI
remains a separate gate. The conditional contextual-product leaf now has its
own [580 source witness](decimal-context-product-580-audit.md). The original
audit and its symbolic, unexecuted witness remain historical evidence.

The first remaining concrete obstruction is **inexact sqrt's scaled radicand**,
not the repaired working-precision, finite-root or midpoint defects.
`BigDecimal("2").sqrt(MathContext(400000000, DOWN))` reaches an unsupported
799999998-place working power although its correctly rounded result has a
supported coefficient and scale. This is a symbolic source witness, not an
executed failure or a claim that the required large allocation is feasible.

This finite **SOURCE-ONLY** audit adds only this document. It reads current own
source and public Java25 API documentation, without foreign numerical
implementation source or bytecode. No compiler, script, numerical helper,
certificate, probe, test, build, benchmark, Git or tracker command ran. No
production source, test, fixture, assertion, tool or generated evidence changed.
The parent supplied current HEAD `c373789`; the source hashes below, rather than
an unexecuted Git query, bind this analysis. Root owns independent review and
task disposition. Neither542 nor548 is closed by this audit.

## Current source and existing dependencies

All source references below are relative to
`src/commonMain/kotlin/io/heapy/krogu/math/` and use the current line numbers.
Assume ordinary canonical stored values, valid fixed contexts, truthful integer
and decimal leaf operations and successful required allocations. Caller/subtype
semantics are explicitly separated below.

| Own source | SHA-256 |
| --- | --- |
| `BigDecimal.kt` | `08b4329ea5e902f1043107d42ddb02592be537497f6b0990927e8c67c4ed3f29` |
| `BigInteger.kt` | `5a4aa675afc9cdfdff6c8761dba45c9636de5533e70fabbd6ea808dc6fa08781` |
| `MathContext.kt` | `d3690387b1345f90cc927044a1a450983dd0d2ce6e3e47054384c31979fd5261` |

The current integer division dependencies are the
[task530 normalized kernel](division530-proof.md) and its
[output selection](division530-selection-proof.md). Integer sqrt is
`BigInteger.kt:550–568`; its Newton step requests the private quotient-only
selector, whereas decimal sqrt at `BigDecimal.kt:589` requests both outputs.
Integer powers are `BigInteger.kt:193–246`. Decimal digit counting is the
[528 argument](../DECIMAL_METADATA_COMPARISON.md), not a full input string.
Decimal coefficient rounding composes with the
[549 rounding proof](contextual-constructor-rounding-549-proof.md).
All these leaf contracts retain their own range, allocation and subtype limits;
listing a dependency does not independently reprove every kernel.

## Public contract and already repaired issues

The public Java25 [exact power](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#pow(int)),
[context power](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#pow(int,java.math.MathContext))
and [square root](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#sqrt(java.math.MathContext))
contracts supply the normative validation, context rounding and preferred
representation. Exact power accepts exponents0..999999999. Context power permits
signed exponents with the documented precision restrictions and specifies an
expanded-precision accumulator, optional reciprocal and destination rounding.
Sqrt uses preferred scale `s/2`; nearest modes have the half-ulp bound, other
rounding modes the one-ulp bound. Negative radicands and impossible exact requests
fail arithmetically. Zero is a special preferred-scale result. These statements
do not require an internal callback sequence or reproduce an accidental reference
overflow.

The following defects are **not** current open findings:

* The working-precision Int addition is already repaired by d660475. Current
  `BigDecimal.kt:540` adds in Long, then caps the internal context. The
  [working-precision argument](decimal-pow-working-precision.md) proves conditional
  equivalence of this cap for supported intermediates. Its128 retained plain-ONE
  reference outcomes remain diagnostics alongside independent required values.
* Task563 already detects every ordinary finite decimal root by coefficient/
  scale parity before input normalization or precision-sized powers. The
  [current parity proof](decimal-exact-root-563-parity-proof.md) supersedes the
  explicitly withdrawn full-input-string draft. Its
  [execution/integration report](decimal-exact-root-verification.md) records
  the retained independent controls and published CI.
* Task565 already avoids the oversized four-times numerator and midpoint-square
  products. Its [midpoint proof](decimal-sqrt-midpoint-565-proof.md) assumes the
  numerator and denominator have been constructed successfully. The
  [combined execution report](math-port-integration-verification.md) records the
  actual certificate and platform execution. That premise is precisely where
  the new witness below fails, before565's block.

The old power summary in NUMERICAL_ARGUMENTS retains historical pinned-runtime
working-precision failures. It is not evidence that current own ONE powers
still fail or should fail; the later focused proof and controls govern that
repaired case. No historical outcome is deleted or relabelled here.

## Complete public power branch inventory

| Entry/current source | Exhaustive branch or domain | Binding and remaining leaf |
| --- | --- | --- |
| `pow(n)`,524–525 | `n<0` or `n>999999999` | Required ArithmeticException; rejects Int.MIN_VALUE without taking its absolute value. |
| 526 | `n=0`, every ordinary source including zero | Returns ONE; no coefficient power or source scale product is needed. |
| 527–528 | `1<=n<=999999999`, zero coefficient | Scale product is computed in Long and saturated by scaleForValue; integer zero power supplies zero. |
| 527–528 | Same exponent domain, nonzero coefficient | Checked scale product, exact integer coefficient power, then construction. Conditional on the integer power and required intermediate/cohort range; zero saturation is not used. |
| `pow(n,mc)`,531 | `p=0` | Virtual delegation to pow(n), so negative exponents reject and the exact nonnegative branches above apply. |
| 532–533 | `p>0`, out-of-range n / n=0 | ArithmeticException / ONE respectively, before exponent-digit validation. |
| 534–536 | Remaining nonzero n, exponent digit count e>p | ArithmeticException. Valid n has absolute magnitude1..999999999 and e in1..9. Negation cannot overflow Int after validation. |
| 540–547 | `p>0`, e<=p, n>0 | Work precision min(p+e+1,MAX) in Long; one expanded-context accumulator, high-to-low bits. Requires contextual multiplication and final roundValue leaves. |
| 548–549 | Same validation, n<0 | Same positive-power accumulator, then ONE.divide(result,work), then roundValue. Zero divisor rejects; terminating/inexact reciprocal behavior depends on the current divide leaf, including its wide-context route. |

For exact power, `s.toLong()*n` has magnitude at most
2147483648*999999999, below Long.MAX_VALUE. For contextual power the exponent
has at most30 bits; loop indices are29..0 at most, and `1 shl i` cannot select a
negative shift or sign bit. The work-precision sum is at most Int.MAX_VALUE+10.

In **exact** arithmetic, let h be the already scanned exponent prefix. The
initial accumulator1 is x^0; squaring maps h to2h and multiplication on a set
bit maps it to2h+1. Thus the final exponent is |n|, and a reciprocal produces
x^n. With rounded multiplication, that exact-value invariant must instead be
read as an operational recurrence: round each square/product with the work
context. Current source implements the public prescribed recurrence. It does
not compute an exact full power followed by one rounding. Sign follows the
base's sign and exponent parity, including the reciprocal.

This establishes the algorithm binding, conditional on multiplication, division
and rounding leaves. It does not independently derive a universal two-ulp error
inequality merely from h's recurrence or a finite oracle matrix. Nor does a
representable final reciprocal prove every positive-power intermediate is
supported. Context multiplication is at128–147 and context division at263–345;
their checked scale, product and wide-context requirements remain real composed
leaves. The existing MAX-work-precision repair does not settle those obligations.
No second power defect is asserted without a corresponding supported-result
argument and path analysis.

## Complete square-root branch inventory

| Current source | Exhaustive ordinary class / additional subtype path | Status |
| --- | --- | --- |
| 553–554 | Negative value | ArithmeticException; existing sign call/negative prefix retained. |
| 555–557 | Stored zero with truthful sign | Zero at truncating preferred scale s/2, for every valid context. |
| 562–563,608–621 | Positive, finite decimal root | Complete parity/square detection; successful roots go directly to624–632. |
| 628–632 | Finite root, p=0 | Exact root at nearest preferred cohort; no precision-sized power. |
| Same | Finite root, p>0, UNNECESSARY | Exact rounding succeeds when only zero digits are dropped; otherwise ArithmeticException. |
| Same | Finite root, p>0, seven rounding modes | Full integer coefficient rounding, possible carry, then bounded preferred-scale padding. |
| 564–576 | No finite root, p=0 or UNNECESSARY | Ordinary exact request must fail. Normalized parity/integer-square fallback cannot produce an ordinary successful finite root missed by the complete earlier criterion. Its intermediate ArithmeticException is already a required failure category. Replacement normalization/getters are separate541/526 paths. |
| 578–581 | Stored zero but reported positive sign | Inherited poisoned-sign behavior; not an additional ordinary mathematical class. |
| 583–588 | Positive irrational root, p>0, seven rounding modes | Compute exact decimal exponent, working scale and scaled rational radicand. This is the first remaining representation-range obstruction. |
| 589–604 | Successfully constructed positive n,d | Both-output division, integer floor root, exactness and bounded565 midpoint comparison. Conditional all-input rounding proof below. |
| 605 | Rounded grid root / decimal carry | roundValue normalizes a p+1-digit carry; stripTo removes only value-preserving zeros toward preferred scale. |

### Finite roots and preferred cohorts

For x=c*10^-s>0, an even s=2k has a finite root iff c=r². For odd s=2k+1,
a finite root requires c=10r²: a reduced rational square root of c/10 has
a denominator whose square divides ten, so that denominator is one. These
criteria are both necessary and sufficient, including negative odd scales.
They are exactly608–621 and avoid multiplying c by ten or formatting it.

The raw root scale q is s/2 for even s and (s-1)/2 in Long for odd s.
It lies in[-1073741824,1073741823], and differs from the truncating preferred
scale by at most one, only for negative odd s. The root coefficient's decimal
digits are at most323228497; an odd-scale root has at most323228496 before
possible one-zero padding. Existing coefficient rounding drops only its own
digits; a carry requires at most one further exact division. All eight signed
rounding predicates are supplied by the existing roundValue/roundedDivide
composition, here with a positive coefficient.

At positive p the chosen target is
`min(preferred, roundedScale+p-roundedDigits)`: the greatest scale no higher
than preferred that fits the digit budget. An inexact rounded root has p
digits and needs no padding. If exact zero-tail digits were dropped, they have
already used the precision budget. With no drop, q differs from preferred by
at most one. Hence padding is at most one zero; it never depends on an enormous
p. The resulting scales remain inside Int, even after the largest supported
root-digit drop. These are the563 whole-class bounds, not sample-count claims.

### Inexact roots, floor invariant and rounding

For truthful coefficient digit count D and Int s, E=D-s-1 is safe in Long.
The code's negative branch (E-1)/2 is exactly floor(E/2), called R. Sqrt's
adjusted decimal exponent is R. At precision p>0 the quantum is10^-t with
t=p-1-R. All formula additions and `2L*t-s` are safe in Long; conversion of t
to Int and the actual integer powers/products are separate range preconditions.

When the scaled radicand n/d is supported, write n=qd+r,0<=r<d and
a=floor(sqrt(q)). Then a²<=n/d<(a+1)², so a is the exact scaled floor root.
Exactness is precisely delta=q-a²=0 and r=0. If inexact, midpoint sign is the
sign of `(4*(delta-a)-1)*d+4*r`. Delta<a makes it negative; delta>a makes it
positive; equality reduces to4r-d. Writing d=4u+v,0<=v<4 yields the code's
r-versus-u comparison, then -sign(v) on equality. The expression is a proof
identity; production does not allocate those large products. Task565 proves
the constructed delta/square/increment values stay within its supported n,d
premise. The current530 pair selector supplies both q and r.

The mode selects a or a+1 using exact midpoint sign and retained parity.
Directed errors are less than one quantum; nearest errors are at most half.
A carry to10^p is exact and the final round removes one zero without a second
inexact decision. Trailing-zero stripping preserves the rounded number.
Ordinary genuine midpoint ties have a finite decimal root and take563's earlier
path; the same midpoint identity also handles ties algebraically. This proves
rounding conditional on obtaining the supported scaled radicand, not that the
precondition holds for every supported result.

## Concrete remaining obstruction: supported sqrt result, oversized radicand

Choose the ordinary input c=2,s=0, p=400000000, mode DOWN. Input and context
are valid and tiny apart from the precision integer. Sqrt(2) is irrational, so
finiteExactSquareRoot returns null: the coefficient2 is not an integer square.
This is neither an exact request nor UNNECESSARY. Truthful precision gives
E=0, R=0, t=399999999 and shift=799999998.

At587, evaluation calls tenPow(799999998) before multiplication. The explicit
helper guard at936–938 rejects exponents above646456992 with ArithmeticException.
Moreover the desired full working power really is outside the integer domain:
10>2^3 gives bitLength(10^799999998)>=2399999995>Int.MAX_VALUE. Simply removing
the tenPow guard would not establish correctness.

Nevertheless let N=floor(sqrt(2)*10^399999999). Independently,
1<sqrt(2)<2 implies N has exactly400000000 decimal digits and

`N < 10^400000000 < 16^400000000 = 2^1600000000`.

Its coefficient therefore needs at most1600000000 bits, strictly below the
supported Int.MAX_VALUE bound. Scale399999999 is an Int. The result
N*10^-399999999 is precisely DOWN's p-significant-digit rounding, with error
less than its quantum. Any trailing-zero cohort stripping only reduces the
coefficient and scale. Thus there is a supported result representation even
though this implementation tries to create a mathematically unsupported
working radicand. No absent maximum-size allocation or elapsed-time measurement
is needed for that source-level contradiction.

This witness is not a revived563 case: no finite decimal root exists. It is
not565's former shifted-midpoint problem: execution cannot reach589–604.
It does not assert an observed JDK failure, a particular JDK result string,
maximum-domain execution feasibility or a new failing runtime test. It identifies
a concrete shared542/548 representation-range obligation. Repaired source must
avoid materializing the oversized radicand, rather than impose a smaller p cap,
extend timeouts or replace the witness by a smaller input and call it complete.

## Existing independent tests and execution scope

| Retained source | Relevant independent evidence / precise limit |
| --- | --- |
| `DecimalPowWorkingPrecisionContractTest`,17–96 | Six methods: unit signs, MAX-neighbor work precisions, monomials, exact products/reciprocals, zero/saturation, validation and explicit caller/super identity. Repairs the specific Int overflow; not general intermediate-range closure. |
| JVM `DecimalPowerBoundaryTest`,7–57 |128 original boundary reference keys plus the accumulator matrix. Live comparisons are distinct from independent mathematical expectations. |
| JVM `DecimalPowerValidationTest`,7 onward | Exact/unlimited scale-validation ordering; callback diagnostics are not a universal internal-order contract. |
| `DecimalExactRoot563Test`,26–116 | Four methods, independent literal roots and digit/string rounding model, extreme scales/precision, trailing-zero cancellation, ties/carry, genuine exact-mode errors and truthful/caller-super routes. |
| `DecimalSqrtMidpoint565Test`,67–146 | Four methods, independently squared decimal boundaries, tiny positive tails, all modes, carry/cohorts and explicit caller controls; no giant scaled-radicand witness execution. |
| JVM `DecimalSqrtMidpoint565JdkTest`,21–57 | Two methods combine independent literals with public JDK comparisons and wide zero cohorts. |
| `ZeroSqrtNull568Test`,28 onward | Preferred zero scales including negative odd and Int endpoints, valid contexts and explicit caller identity. Historical null entries are real JVM boundaries under578. |
| Scale/portable/normalized/subclass suites | Retain original observations, selected563 diagnostic classification and ordinary/truthful arithmetic controls with their original scope. No family-wide callback waiver follows. |

The source review binds the three principal existing common controls to hashes
`9d6a474993b07cd4a417733c0875ee123db1bfc2b8c1f499a80ad545bac54445`
(pow), `be6745c30c3d9bdf81bfb1fdca2e6e61963ef164b178ac7aec7c55a278d8ae86`
(563), and `78bea9b16239a9675f527844803394cb7a06732c43eb345174af56acd6389853`
(565). Their execution histories are recorded in the linked563 and combined
reports. Later [non-null API gates](non-null-api-573-verification.md) and
[hosted CI evidence](public-math-ci-559.md#hosted-verification) preserve these
tests on their own named snapshots. This audit reruns none of them and does
not promote a green finite suite to all-domain proof.

## Remaining work and scope boundary

The next mathematical task is the concrete inexact-root radicand construction
above. A future repair needs a bounded representation of the logically scaled
root computation and an exact remainder/midpoint argument without a2p-digit
BigInteger. Existing565 rounding logic remains a valid conditional component.
General power intermediate/cancellation and all required multiply/divide/scale
leaves must also remain explicit; matching the prescribed accumulator does not
prove every such operation completes throughout the supported-result domain.

All public pow/sqrt entries remain open. Explicit calls into caller overrides,
direct caller-owned exceptions and super entry retain their language meaning.
Inherited callbacks, poisoned sign/precision/normalization, restored coefficient
subtypes and reference diagnostic precedence remain541/526 classifications;
they cannot justify either a mathematical waiver or new noncall assertions.
No new poisoned-subclass test is proposed here.

Root must independently review this source witness and choose a separately
scoped repair/proof/test campaign. Current source hashes, conditional proofs,
historical failures and platform evidence remain separately bound. No claim of
full542 completion, JCK conformance, performance acceptance or future PASS count
follows from this finite audit.

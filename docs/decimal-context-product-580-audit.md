# Context-rounded decimal product: source range obstruction (548/542)

The current contextual multiplication eagerly constructs an exact coefficient
product outside its public integer range even when the context-rounded result
has a one-digit supported coefficient and an Int scale. The symbolic witness is
ordinary `x.multiply(x, MathContext(1, DOWN))`, where x has coefficient
`2^1073741824` and scale zero. No such large object was constructed or evaluated.

This finite **SOURCE-ONLY** audit adds only this file under `/private/tmp`.
Publication-root and isolated579 production/tests/docs remain untouched. No
compiler, script, numerical helper, certificate, probe, test, build, benchmark,
Git or tracker command ran. Metadata reads/hashes and public API browsing were
the only examination tools. No foreign numerical implementation source, body
or bytecode was read. This is a mathematical/source witness, not an executed
JDK failure, allocation feasibility result, repair or task acceptance.

## Bound own source

Paths below are relative to `/Users/yoda/dev/pet/krogu-math`.

| File | SHA-256 |
|---|---|
| `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt` | `08b4329ea5e902f1043107d42ddb02592be537497f6b0990927e8c67c4ed3f29` |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` | `e4dc75b253c689b0f3c634f02f9e491c9a1dcfae396a11135be4427c69ce7b68` |
| `src/commonMain/kotlin/io/heapy/krogu/math/MagnitudeLimits.kt` | `5bf687bb968d22908a36b8898f19649bdbdf7e5a2596575f1808053fca0e4769` |
| `docs/decimal-power-root-542-current-audit.md` | `ff4e9f93e3b0718848f518d3349e45aca0224ca3abdad754dee6b6fc6e2fea4a` |

These publication-root pins are distinct from isolated579's changed BigDecimal
and historical5a4 BigInteger. This audit does not change or rebind that candidate.

## Normative boundary and resource qualification

Java25 [BigInteger](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html)
requires the open integer interval (-2^L,2^L), L=Int.MAX_VALUE, and permits wider
implementations. Exact integer operations producing values outside their
supported range may reject. Own normalized magnitudes implement that required
interval through the checked L-bit limit. Thus the inputs below belong to both
the mandatory integer domain and own domain; their exact product does not belong
to own domain. An exact standalone BigInteger multiplication rejection is valid.

Java25 [BigDecimal](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
defines contextual arithmetic by rounding the logical exact value, with multiply's
preferred scale equal to the input-scale sum. Discarding digits reduces that
scale. Its scale is an Int, and the documentation discusses intermediate
allocations and their cost. These value/rounding rules determine the result
below; they do not guarantee any finite heap or deadline. The logical exact
intermediate need not itself be a returned BigInteger. Its unsupported integer
representation is therefore not a mathematical range defect in the specified
rounded decimal result.

This identifies a supported-result obstruction under the project's mathematical
API contract. It does **not** prove that the reference JDK computes this huge
case, diagnose a JDK implementation defect, or establish an observed Java/port
discrepancy. A future reference observation, if authorized, must be retained
separately; the current witness needs none to determine its mathematical result.
Guaranteed numeric value domain is separate from resource availability. There
is no giant-input runtime proposal, benchmark or heap claim in this audit.

## Exact symbolic witness and independently defined rounded result

Let b=1073741824=2^30, an Int-valid positive shift, and let

`A=B=2^b`, `x=[A,0]`, `y=[B,0]`, `mc=(precision=1, DOWN)`.

The input bit length is b+1=1073741825, strictly below L=2147483647. In source,
`BigInteger.ONE.shiftLeft(b)` passes `magnitudeBits()+b<=L`
(`BigInteger.kt:571–587`) and its normalized result passes
`MagnitudeLimits.checkedBitLength` (`MagnitudeLimits.kt:15–19`). This describes
an admissible representation, conditional on allocation, not an executed
construction. Ordinary BigDecimal construction with that base integer and
scale0 introduces no subtype replacement or parser/string requirement.

The logical product is the positive integer P=2^(2b)=2^2147483648. It has
2147483649 bits, so P is outside own public BigInteger range. Define e as the
unique integer satisfying

`10^e <= P < 10^(e+1)`.

An exact decimal logarithm or leading-digit computation is unnecessary. From
10<16 and 8<10,

* `10^536870912 < 16^536870912 = 2^2147483648 = P`,
* `10^715827883 > 8^715827883 = 2^2147483649 > P`.

Therefore **536870912 <= e <= 715827882**, entirely within Int. Let

`d=floor(P/10^e)` and `r=P-d*10^e`.

The defining decade inequalities give d in1..9 and 0<=r<10^e. In fact r>0:
e>0, so 10^e contains a factor5, whereas the pure power P of2 contains none.
Consequently

`d*10^e < P < (d+1)*10^e`.

Those are the exact adjacent positive one-significant-digit grid boundaries.
DOWN selects the lower one, without a tie or carry ambiguity. The uniquely
specified result representation is **[d,-e]**: preferred scale starts at0,
P has e+1 decimal digits, and discarding e positions to retain precision1
reduces scale to-e. The coefficient is positive and at most9 (at most4 bits);
the scale lies in[-715827882,-536870912]. Both are supported. d is nonzero and
not divisible by10, so no alternate zero-tail cohort is needed. Neither
UNNECESSARY, an invalid context nor an out-of-Int final scale can justify
rejection. d is specified exactly by integer inequalities; no guessed literal
leading digit or host floating approximation is used.

For comparison, the candidate family with b=1100000000 has the same defect:
input bits1100000001, product bits2200000001, and final decimal exponent
between550000000 and733333333. The b=2^30 witness above is the first symmetric
power-of-two exponent beyond the exact integer product's supported boundary:
b=1073741823 would still give product bit length2147483647. No larger family
claim is needed for this finding.

## Complete reached source path

1. `BigDecimal.multiply(multiplicand,mc):133–140` sees p=1, so it does not take
   the precision-zero exact-delegation return. The logical input-scale sum is0.
2. Its condition `logicalScale<=Int.MAX_VALUE` is true; OR short-circuiting
   requires no later zero checks there. It executes
   `multiplyValue(operand).roundValue(context)`.
3. `multiplyValue:129–131` accepts scale0 via scaleForValue and calls the exact
   coefficient multiplication. C is nonzero, so this is not zero saturation.
4. `BigInteger.multiply:65–68` accepts the nonzero operands, then tests
   `(b+1).toLong()+(b+1)-1 = 2147483649 > L` and throws ArithmeticException.
   Its product-array allocation at69 and multiplication loops are never reached.
5. Decimal `roundValue:457–467` is never entered, so its precision reduction
   cannot rescue the coefficient. The separate HIGH-logical-scale route at
   `BigDecimal.kt:142–147` is also irrelevant: the witness's logical scale is0.

This is an eager intermediate rejection conditional only on having the supported
inputs already present. It is not a claim that an attempted huge construction
would reach it before OOME or another resource failure. The exact integer leaf
does the correct thing for an exact product. The mismatch is composing that
public limited-range leaf before the decimal context can discard digits.
Raising an arbitrary application precision cap or treating a finite test pass as
full-range coverage would not supply a mathematical resolution.

## Existing coverage and remaining composition obligations

The inspected existing witnesses remain valid for their actual domains:

* `DecimalScaleRoundingRepairTest.contextMultiplicationRequiredReductionAndCarry`
  (`src/commonTest/...:122–141`) uses seven small coefficient pairs, endpoint
  input scales, all signs, truthful masks, precisions1/2/3/7 and all modes.
  Its largest exact coefficient product is small. The adjacent80-digit controls
  at76–120 exercise supported wide coefficients and mandatory HIGH scale
  reduction. Neither requires an exact coefficient product wider than L bits.
* `DecimalConformanceTest.structuredRoundingAndPreferredScales`
  (`src/jvmTest/...:30–51`) compares structured small Long coefficients against
  the JDK. It is useful rounding/cohort evidence but cannot cover this symbolic
  integer-range crossing.
* `docs/decimal-overload-contract-inventory-541.md` already records the complete
  ordinary product/range proof as residual P2 (`:206`); its MU/SR/F witnesses
  are not a full-range certificate. This audit supplies a concrete ordinary
  leaf obstruction, not a new subtype dispute or blanket541 reclassification.
* `docs/decimal-power-root-542-current-audit.md:112–120,250–259` explicitly leaves
  contextual multiplication/division and intermediate support conditional.
  `docs/decimal-pow-working-precision.md:40–71` repairs the internal precision
  addition but disclaims repair of unsupported products. Its unit/monomial
  tests remain a proof of that repair, not this obligation.
* `docs/math-port-integration-verification.md` records historical completed
  finite platform/public selections on its own pins. No selection was rerun
  or audited anew here, and no old PASS count is changed by a symbolic finding.
  Maximum-domain allocation/performance remains548, with this additional
  mathematical intermediate-range condition distinguished from heap sizing.

Current `pow(n,mc):531–549` calls contextual multiplication for every accumulator
square and set-bit multiply at545–546, so its operational composition inherits
the requirement that each required rounded multiplication can complete in its
supported-result domain. This does not automatically make the same p1 square
a pow failure: pow first rounds its accumulator at enlarged working precision,
which can reduce this particular base to a short coefficient before squaring.
A concrete pow counterexample requires its own reached recurrence, intermediate
cohort and final supported-result proof. This audit does not assert one, prove
the universal two-ulp bound, replace that prescribed recurrence by exact-power
rounding, or reopen the repaired Int working-precision overflow.

Inspected evidence pins:

| File | SHA-256 |
|---|---|
| `DecimalScaleRoundingRepairTest.kt` | `c63f02f60e4c4b3fc00f69abf35d1f6183feb18005d8b37533972ab7a36d84d1` |
| `DecimalConformanceTest.kt` | `b93b566deb5cd4eb9b710f8a499b278479636f8de92b8b5b61f2e09e40fc1a57` |
| `DecimalPowWorkingPrecisionContractTest.kt` | `9d6a474993b07cd4a417733c0875ee123db1bfc2b8c1f499a80ad545bac54445` |
| `decimal-pow-working-precision.md` | `47f47850763ed4d0ba4c07af0bddaa27d94feebc454283a11028666af844b1f7` |
| `decimal-overload-contract-inventory-541.md` | `3b536dfc1ca283847b3e402b46cff928a33da2eed63f4a6a0e871f0ccc19cec8` |
| `math-port-integration-verification.md` | `31d0abd229951a75320ed1bd83d61799a8856412187a602ded3a8ee7d6767a97` |

The finite outcome is a confirmed ordinary supported-result source obstruction,
ready for parent independent review and separate task/scoping decisions. No
repair, new test, empirical outcome, task transition or closure of548/542 is
claimed. All owned examination calls are terminal; no runtime window is held.

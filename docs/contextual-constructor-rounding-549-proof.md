# Contextual integer-coefficient constructors: conditional proof (549)

The initial source-only argument covers the own paths
`BigDecimal(BigInteger, MathContext)` and
`BigDecimal(BigInteger, Int, MathContext)`. It adds no production code or tests,
and no script, compiler, helper, numerical operation, test or build was executed
for that source milestone. Existing finite tests and proofs below are source evidence;
their historical execution evidence is not a new execution of this argument.

## Public clauses and the exact conditional claim

Java25 describes both
[the integer/context constructor](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#%3Cinit%3E(java.math.BigInteger,java.math.MathContext))
and [the integer/scale/context constructor](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#%3Cinit%3E(java.math.BigInteger,int,java.math.MathContext))
as context-rounded conversions. Its
[general precision discussion](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html)
uses significant-digit rounding, allows a carry to discard one additional digit,
makes precision zero exact, requires an arithmetic exception for inexact
`UNNECESSARY`, and permits one for an unrepresentable Int scale.

There is a wording tension: the integer/context constructor also says its scale
is zero, without the explicit before-rounding qualification used for the
int/context and long/context constructors. Finite precision can require negative
scale: rounding integer 123 to precision two with HALF_EVEN gives coefficient 12
and scale -1 under the general significant-digit rule. A scale-zero coefficient
for that same rounded value would have three digits. This proof establishes the
own significant-digit/context rule with initial scale zero; it does not prove
an unconditional final-scale-zero reading or silently waive that literal clause.
Resolving that specification-wording classification remains under root526.
The example here is algebra, not a newly observed JDK call or a JDK bug claim.

## Source binding and assumptions

Directly read own source:

| Source | SHA-256 |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt` | `08b4329ea5e902f1043107d42ddb02592be537497f6b0990927e8c67c4ed3f29` |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` | `df55cb8c8c6a291b59f6ee6daf365c9b4c38e4e1f33583e108183c5a28039fb3` |
| `src/commonMain/kotlin/io/heapy/krogu/math/MathContext.kt` | `d3690387b1345f90cc927044a1a450983dd0d2ce6e3e47054384c31979fd5261` |

Assume a valid non-null context with fixed precision `P >= 0` and a valid rounding
mode, successful constructor normalization to a canonical base integer `C`,
exact integer division/remainder, powers, sign, parity, comparison, addition and
shift primitives, and successful required allocations. Stored decimal digit
count must be exact and fit Int. For the supported-range corollary below assume
the established bound `D <= 646456993` for a canonical supported coefficient.
This argument composes those contracts; it does not independently prove the
integer kernels, their temporary storage limits, platform primitives or all
required numerical range (task548).

## Constructor reduction and reachable callbacks

`BigDecimal.kt:10-18,25-27` first creates an exact base `BigDecimal(C,S)`, where
`S=0` for the two-argument overload and `S` is the supplied Int for the
three-argument overload. It invokes private `roundValue` on that temporary, then
the private copying constructor reads its private coefficient and scale fields.

The primary constructor retains an exact-class `BigInteger` directly; for a
subtype it invokes that input's virtual `toByteArray()` and constructs a base
`BigInteger` from the returned bytes. Thus arbitrary coefficient subtype
callbacks, lies, exceptions and encodings remain a real boundary before this
proof's effective `C` exists. Unlimited precision does not bypass normalization.
No claim is made that the original subtype's reported numerical methods and its
encoded value agree, nor that all such callback behavior is solved (task541).

The temporary and every result allocated inside `roundValue` are base decimals,
not the outer subclass whose superclass constructor is being evaluated.
`roundValue` does call virtual `result.precision()`, but here that receiver is
the freshly constructed base temporary/result; it reaches
`storedPrecision()` and `coefficient.decimalMagnitudeDigits()`
(`BigDecimal.kt:43-44`). It does not call the constructing decimal subclass's
precision override. Neither public `round(mc)` nor `plus(mc)` is called by
these two constructor paths. The final copy uses fields rather than virtual
`unscaledValue()`/`scale()`. This is a reachability argument for these paths,
not a claim that all decimal operations avoid virtual calls, or that every
coefficient callback is absent. Restored objects entering other operations and
hostile/reflection-modified states are outside this constructor argument.

## First division and all rounding modes

For nonzero `C`, put `n=abs(C)` and define `D` by
`10^(D-1) <= n < 10^D`. Zero has stored precision one.
`roundValue` is at `BigDecimal.kt:457-467`.

* `P=0` returns the exact temporary before consulting its precision or mode.
* For zero and `P>=1`, `drop=1-P<=0`; the stored zero scale is retained.
* For nonzero `P>=D`, `drop=D-P<=0`; both coefficient and scale are retained.
* Otherwise `1<=P<D`, and `d=D-P` is positive. Set `b=10^d`. Then
  `b<=10^(D-1)<=n`, so the divisor is no larger than the supported input.

Write `n=q*b+r`, `0<=r<b`. The digit inequalities give
`10^(P-1)<=q<=10^P-1`. Truncating signed division returns `sign(C)*q`, with
remainder of the same sign as `C` when nonzero. `roundedDivide`
(`BigDecimal.kt:957-963`) returns that quotient unchanged when `r=0`, so exact
division also succeeds under `UNNECESSARY`. For `r>0`, let `s=sign(C)` and let
`h` be the comparison of `2*r` with `b`. It either retains the quotient or adds
`s`, as selected by `increment` (`BigDecimal.kt:966-975`):

| Mode | Add `s` for nonzero remainder exactly when |
| --- | --- |
| UP | always |
| DOWN | never |
| CEILING | `s>0` |
| FLOOR | `s<0` |
| HALF_UP | `h>=0` |
| HALF_DOWN | `h>0` |
| HALF_EVEN | `h>0`, or `h=0` and truncated quotient is odd |
| UNNECESSARY | never returns; throws `ArithmeticException` |

These predicates implement the public
[RoundingMode directions and tie policies](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/RoundingMode.html).
Signed quotient parity from `testBit(0)` agrees with magnitude parity, including
negative quotients; adding the sign moves exactly one unit away from zero.
Comparison uses the full remainder, not a truncated guard digit, so exact halves
and either side of a half are distinguished. The signed proof covers negative
inputs even where the finite constructor fixture below uses positive ones.

There is also a useful bound on helper-created mathematical magnitudes.
For `r>0`, `2*r < b+r <= q*b+r=n` because `r<b` and `q>=1`.
Thus the half-comparison shift cannot itself require a larger integer magnitude
than the input. Since `b>=10`, `q+1<=q*b+r=n`; a rounded quotient is bounded by
the input too. This bounds the mathematical values, not every allocation or
internal kernel workspace. It does not discharge task548.

## At most two loop iterations; only one inexact rounding

If the first division returns, its magnitude `Q` is either `q` or `q+1`.
Consequently `10^(P-1)<=Q<=10^P`. The result is nonzero for every returning mode;
there is no nonzero-to-zero case needing scale saturation.

When `Q<10^P`, the first result has exactly `P` digits, so the loop terminates.
When `Q=10^P`, a single carry has created `P+1` digits. The next `drop` is
exactly one. Division by ten is exact, returning magnitude `10^(P-1)` with
exactly `P` digits, and reducing the scale by one more. Its remainder is zero;
neither a second rounding decision nor a new `UNNECESSARY` rejection occurs.
The two representations have the same numerical value. Thus, subject to leaf
contracts and successful scale checks, the loop has at most two iterations and
performs at most one inexact rounding. There is no double-rounding error.

For an exact first division, `q<=10^P-1`; carry cannot occur without an increment.
Exact trailing-zero removal stops as soon as the requested precision is met,
rather than stripping further zeros and moving farther from the initial scale.

## Scale representability and exact rejection

The first candidate scale is `S1=S-d`, evaluated in Long before conversion.
The carry case additionally needs `S2=S1-1`. Validating `S1` before division
cannot reject a case later rescued by carry: carry only decreases the scale.
The scale never increases, so a representability failure is below Int.MIN_VALUE,
not above Int.MAX_VALUE.

The final nonzero rounded coefficient has exactly `P` digits at scale `T0`
(`S1` without carry, `S2` with carry). If `T0<Int.MIN_VALUE`, any valid Int scale
`T>=Int.MIN_VALUE` for the *same rounded numerical value* requires coefficient
`C0*10^(T-T0)`. That coefficient has `P+(T-T0)>P` digits. Hence no equal member
of the selected rounded cohort can both have valid Int scale and at most `P`
digits. If `S1` is already invalid this reasoning applies after hypothetical
rounding whether or not there is carry. If `S1=Int.MIN_VALUE` and carry occurs,
only the second check fails; representing that value at `S1` would need `P+1`
digits. This distinguishes a genuine scale barrier from an avoidable transient
overflow that a later operation could repair.

`checkedRoundingScale` (`BigDecimal.kt:944-948`) returns the narrowed Int iff
converting it back to Long equals the candidate; this is an exact Int-range
test. It does not saturate zero or call `checkedScale`/`scaleForValue`.
`checkedScale` (`:940-943`) rejects the same out-of-range numeric scales but
chooses its message from the Long sign; `checkedRoundingScale` chooses from the
narrowed Int sign. They must not be conflated for diagnostics. With
`d<=646456992`, the first candidate is at least `-2793940640`, safely within
Long; a valid first result needs at most one further subtraction. Invalid
negative candidates in this range narrow to positive Int, producing the own
rounding message `Underflow`, where `checkedScale` would say `Overflow`.
The public exception category and these preserved diagnostic observations are
different claims. When scale failure and inexact `UNNECESSARY` compete, the
source checks scale first; this proof makes no universal public priority claim.

## The power ceiling and primitive bounds

For supported nonzero coefficients, the existing own
[decimal metadata proof](../DECIMAL_METADATA_COMPARISON.md#exact-digit-count-over-the-supported-domain)
reduces the supported bit-length bound to `D<=646456993`; its
[binary-range argument](../DECIMAL_BINARY_CONVERSION.md#certified-failing-baseline)
records `bitLength(10^646456992)=2147483644` and
`bitLength(10^646456993)=2147483648`. Those are reused conditional proof results,
not a newly executed certificate or allocation of a maximal coefficient.

Because `P>=1` in a dropping iteration, `1<=d=D-P<=646456992`.
`tenPow(d)` (`BigDecimal.kt:935-939`) therefore does not hit its explicit
exponent ceiling, and the exponent-to-Int conversion is exact. Moreover
`10^d<=n`; the requested divisor is itself supported whenever the input is.
The optional carry divisor is ten, also bounded by the input. The existing
metadata proof discusses the unchanged power kernel separately; this argument
does not replace that proof or promise maximal-sized execution can finish.

The exact digit helper is `BigInteger.kt:854-867`; it uses stored magnitude
metadata and a supported power threshold, with its special maximal estimate
branch. Its correctness and allocation behavior remain assumptions here.
`D-P` fits Int: `1<=D<=646456993` and `0<=P<=Int.MAX_VALUE`; positive `drop`
is at most 646456992. The subsequent carry precision and drop are `P+1` and
one, with `P<D`, so neither precision subtraction can overflow. Context validity
comes from `MathContext.kt:5-8`; invalid/reflectively modified contexts and null
boundary policy are not converted into mathematical precision inputs.

## Existing finite coverage and remaining obligations

No duplicate regression methods are added. The following existing sources cover
the finite controls relevant to this composition:

| Evidence | Actual source coverage and limit |
| --- | --- |
| `ZeroContextScale567Test.contextualConstructorsRetainTheirOwnZeroScales` (`:155-172`) | Both contextual BigInteger overloads; all eight modes; precisions 0, 1, 7, Int.MAX_VALUE; scale overload includes Int.MIN_VALUE and Int.MAX_VALUE. This is a finite zero/cohort control, not a maximal coefficient exercise. |
| `PortableOracleTest.jdk25BehaviorOnEveryPlatform` and its `L/constructor` evaluator (`:13,296-301`) | Replays the original three-argument contextual constructor rows and preserves exception text observations. Generator `scripts/generate-oracle.main.kts:632-644` enumerates coefficients 0/123/999, six scales including both Int extremes and three neighbors above the minimum, four precisions 0/1/2/7 and all modes: 576 constructor input keys, alongside separate round keys. This is the generator's finite Cartesian domain, not a new executed row count. |
| `Jdk25Vectors.kt:61962,62386,62688,62944,63181` | Frozen example keys include zero at the minimum scale; 123 at the minimum with P1/UP yielding an arithmetic exception observation; 123 at scale zero with P2/HALF_EVEN yielding `1.2E+2`; the 999 carry near the minimum; and unchanged 999 at maximum scale/P7/UNNECESSARY. These rows use the explicit-scale overload, including scale zero; they do not directly settle the two-argument constructor's wording tension. |
| `ZeroContextScale567Test.nonzeroExactRoundingKeepsRepresentableInputCohort` (`:66`) and `nonzeroMidpointAndCarryUseLiteralModeSpecificExpectations` (`:87`) | Independent literal signed tie/carry/exact-zero-tail controls for the shared rounding seam through plus/round, including real super calls. They are supporting helper evidence, not direct tests of every constructor callback path. |

The existing lexical constructor argument remains in
[constructor-contract-549-coverage.md](constructor-contract-549-coverage.md#conditional-lexical-and-scale-argument).
This memo supplies only the conditional rounding composition for these two
integer-coefficient overloads. It does not close the full constructor inventory,
integer required-range/temporary-allocation obligations (548), subtype callback
classification and other operations (541), every diagnostic/null priority,
the two-argument scale wording tension (526), or whole-project acceptance.
Historical fixtures and public-test adaptations are preserved unchanged; no
assertion is waived and no new JDK result, runtime pass or certification is
claimed. Independent read-only review and any further execution are root-owned.

## Parent review and finite wording diagnostic

Independent read-only mathematical/source review accepted the original argument
and its exact source/fixture references, including the conditional bounds,
callback limits and unresolved wording classification. The accepted source-only
document has SHA-256
`a8f3dfb47e15a423a8624be9e92c845e22cd4893c1b950c37d3e7a4eb605dfd5`
and is preserved separately before this parent appendix.

Root grant2280 then authorized one independently reviewed private `.main.kts`
diagnostic. Four fixed inputs were passed to both integer/context overloads
(explicit scale0 for the three-argument form), against both reference JDK and
the exact current-production ded66 JAR:16 calls total. Kotlin2.4.21 and
Liberica25.0.4+9-LTS used a fresh isolated compiled-script cache and a256MiB
script heap; session49461 exited0,
stderr was empty, and the output contains `COMPLETE 16 0`. All four port classes
were bound to the exact JAR. No retries or source changes occurred.

Both overloads, on both engines, returned these literal fields:

| Input/context | Coefficient | Scale | Precision | Text |
|---|---:|---:|---:|---|
| 123, P2 HALF_EVEN |12|-1|2|`1.2E+2`|
| 999, P2 HALF_UP |10|-2|2|`1.0E+3`|
| 1200, P2 UNNECESSARY |12|-2|2|`1.2E+3`|
| 0, P2 HALF_EVEN |0|0|1|`0`|

Independent raw-evidence review confirmed16 unique MATCH keys, eight per engine,
the literal fields, completion and exact port origin. These are fresh observations
of the two-argument overload, rather than an inference from the explicit-scale
fixture. They agree with the significant-digit interpretation and still do not
resolve the documentation tension or establish a JDK defect, a signed runtime
matrix or full constructor-domain acceptance.

Evidence remains at `/private/tmp/constructor549-scale-wording-probe-v1`.
An initial metadata-only jq audit had an incorrect null-input option and exited5;
the corrected TSV audit exits0/true. The numerical probe was not repeated.
Both the first capsule and its final metadata correction are retained; raw
script/output and the accepted source proof are unchanged.

```text
321829e416f2dbd1c8432df290c0f5e7cb2e784c4cea029cbf0739053437c236  probe.main.kts
5f6c1be87946358bc7b8077cce544106110cb318d1c12d0bf4b75c5002e7d377  output.tsv
efd462387968caa1c21662b2cea452fb87ed736340a4a3bd737df602f3bdfa1e  constructor549-scale-wording-probe-v1-final.tar
```

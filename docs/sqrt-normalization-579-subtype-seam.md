# 579 truthful coefficient-leaf seam: bounded source preparation

**SOURCE ONLY / UNCOMPILED / UNRUN / NOT INTEGRATED.** The original finite seam
addition owned `DecimalSquareRoot579SubtypeJvmTest.kt` and this document in the
isolated `/private/tmp/krogu-math-task-579-PlUizX/root`. A later authorized
cheap-gate correction updates only BigDecimal and the implementation/seam
documents. The common/JVM tests, raw helper, proposal and BigInteger stay frozen.
No publication-root file changed. No script, compiler,
helper, certificate, numerical operation, probe, test, build, benchmark, Git
or tracker operation ran. No foreign implementation source/body/bytecode was
read. Parent owns review, any later execution and adoption.

## Frozen implementation and reachability

| Own frozen file | SHA-256 |
|---|---|
| BigDecimal.kt | `4ab8f15b10f2aa2093e4e04e2a7e4aa6226a7230be24dead3bbe75bcde9e5cc5` |
| DecimalSquareRoot579.kt | `ce779a45a568a49f54c631359c6e13314ee8d05272c6d915dbc12bf7bc7be408` |
| DecimalSquareRootNormalization579Test.kt | `0e406179a587a29c80995c8c0ac7c94586b8d43b0d04cd334d57f15d622c4580` |
| Accepted proposal | `4db4e916c2ffbf7a795a4c1bd9411efcda91e94496b5e15eb1271d440db45bfe` |
| Implementation document | `3d7dda87b59c7cd6beb08673f748fc29a1b307b0a0bb63c74784087a7e528efd` |

The earlier BigDecimal01d814, implementation882fa2 and this documentbca237
were preserved read-only before that correction in
`/private/tmp/krogu-579-cheap-gate-originals.DglxVbQc/`. Source line counts are
unchanged in BigDecimal: only the one-line predicate at591 gained the cheap gate.

Ordinary BigDecimal constructors normalize a coefficient subtype through its
virtual `toByteArray()` (`BigDecimal.kt:15–16`). That is not an unconditional
stored-class invariant. Current own-format JVM `readObject` uses
`defaultReadObject` and checks only that the coefficient is non-null (`19–22`).
A current-format object-resolution hook can supply a consistent integer
subtype, which is retained. No reflection field injection, legacy proxy,
cross-format stream or old binary fixture is needed.

The test reuses the setup pattern in
`DecimalNumericMetadataJdkTest.currentOwnFormatResolvedTruthfulCoefficientsKeepNumericsAndExplicitDispatch`:
write an ordinary decimal, resolve its coefficient to a newly constructed
truthful subtype while reading, then check stored value and scale. Its type
assertion establishes this bounded setup's reachability; it introduces no
general serialization runtime-type or result-identity promise. The existing
528/549 own-format arguments are provenance, not execution of this new test.

## Exact callback ownership

| Stage | Existing/public ownership and new boundary |
|---|---|
| Decimal entry (`552–557`) | Decimal receiver owns virtual `signum`; the base implementation calls the coefficient's `signum`. Negative/zero outcomes remain before normalization. |
| Finite-root prefix (`622–635`) | Coefficient owns `signum`; odd scale invokes its `divideAndRemainder(TEN)`, the returned remainder's `signum`, and the returned quotient's `sqrtAndRemainder`. Even scale invokes coefficient `sqrtAndRemainder`. These sites are unchanged. |
| Inexact prefix (`579–587`) | Existing coefficient `signum` is captured, then decimal `precision` is called through `adjustedExponent`. Stored digits do not dispatch to a public precision override. |
| Old positive-shift branch (`601–619`) | Coefficient owns `multiply(tenPow(h))`; its returned numerator owns division, and returned arithmetic objects own subsequent root/remainder operations. |
| New extraction (`DecimalSquareRoot579.kt:86–95`) | Current value owns `signum` and `divideAndRemainder(10^9)`; its returned remainder owns `toInt`; its returned quotient becomes the next current value. Quotient subtypes can prolong the whole chain. |
| New publication (`100–134`) | Private binary cells create a private byte array and an exact base integer, then BigDecimal is constructed from that base result. No caller coefficient byte hook is used here. |

The extraction hook is **remainder.toInt, not toLong**. The pinned BigInteger
`toInt` reads stored low limbs directly; any `toLong` call inside a user
override would be that override's implementation. Ordinary constructor byte
normalization and the unchanged prefix may have their own hooks. The raw
kernel does not newly require an input byte/string callback or guarantee its
absence across all surrounding inherited operations.

## Truthful-leaf composition and precision guard

For immutable stored C>0, require each reached signum to report the stored sign,
each division by B=10^9 to return a two-element pair with exact stored q,r,
`C=qB+r`, `0<=r<B`, and each reached remainder conversion to return its exact
Int value. These conditions quantify transitively over returned quotient and
remainder subtypes. For every positive current value q is nonnegative and
strictly smaller. Iteration terminates, chunks reconstruct C exactly, and
their positive high chunk makes the digit stream and restoring proof apply.
No runtime-class assumption is needed for this numerical composition.

The test's division override obtains exact results from `super` and wraps both
in the same truthful subtype; signum and toInt delegate truthfully. Thus the
second and later quotient iterations, and the remainder conversions, exercise
the transitive leaf seam. It asserts numerical results, not invocation counts,
internal order, noncalls or inherited result identity.

The captured exponent at `BigDecimal.kt:584` is compared with stored D-S-1 at
`593`. With safe Long arithmetic, equality means the captured reported
precision equals stored D. Positive captured coefficient sign, h>0,
p>Int.MAX_VALUE/8 and failed conservative materialization eligibility are also
required. The precision gate is checked before storedPrecision or the helper
object call. For truthful ordinary metadata, M=D+h=2p-1+epsilon<=2p; at
p<=268435455, 4M<=8p<=2147483640<Int.MAX_VALUE and C*10^h<10^M<2^(4M).
Also h<=2p-1<646456992 and t=(S+h)/2 is Int, so the original construction is
supported. This skips an unnecessary second exact digit-count/TEN.pow allocation
and helper powers-array initialization on that branch. It imposes no precision
cap: higher p continues to the full raw path. Misleading small-p metadata keeps
the earlier materialized path; larger misleading answers still fail the stored
comparison and keep that path too. Correct positive leaves
establish stored positivity; a dishonest positive sign alone does not.
Zero/negative handling, exact roots, precision-zero and UNNECESSARY entries
precede the new route. Additional stored-digit work can allocate; no identical
resource-failure precedence for arbitrary poisoned subtypes is claimed.

## One prepared bounded JVM method

Each current-format roundtrip stores BigDecimal(C,-1). Direct raw calls use
h=1, p=10, t=0, preferred=0. All C have18 digits, so D+h=19 and the raw root
has10 digits. Independent literal identities for N=10C are:

| C | a=floor(sqrt(N)) | r=N-a² | Half decision |
|---|---|---|---|
| 123456790209876543 | 1111111111 | 1111111109 | Below, r<a |
| 123456790209876544 | 1111111111 | 1111111119 | Above, r>a |
| 123456792076543210 | 1111111119 | 1999999939 | Above; low-cell borrow witness |

The first two squares follow from expanding (10^9+111111111)^2; the third
uses (1111111111+8)^2. In each case 0<r<2a+1, proving the floor root; integer
N cannot equal (a+1/2)^2, and r>a decides the midpoint. The third raw final
subtraction is 22000000000-20000000061, requiring a base10^9 low-cell borrow.
No rounding increases the10-digit precision or changes scale0/preferred0.

All eight modes are prepared: positive UP/CEILING choose a+1; DOWN/FLOOR
choose a; all half modes use the literal strict decision; inexact UNNECESSARY
throws. The method prepares24 direct raw calls and48 public controls across
three roundtrips, including the expected exceptions. Counts describe bounded
source work, not execution results or callback promises.

Ordinary and restored public sqrt results are each checked against the same
independent literal coefficient and scale, not only against one another.
These bounded public calls use the original materialized branch: p=10 is below
the cheap-gate threshold, so no extra stored-digit/helper call occurs; D=18,h=1,t=0 also
satisfy the proved materialization bounds (4*(D+h)=76). UNNECESSARY uses the earlier exactness branch.
Only the direct internal call exercises the new raw kernel at these sizes.
This is not a claim that a public giant fallback has run.

Existing explicit caller/override/super controls remain separate, including
the current-format528 method cited above and the retained563 callback contract
suite. No sentinel self-test merely exercising Kotlin dispatch is added.

## Unresolved behaviors and later gates

Wrong/changing signum or precision, incorrect quotient/remainder replacements,
non-progressing quotient returns, malformed/null array elements, dishonest
numeric conversion and directly injected failures do not satisfy this leaf
theorem. Their affected inherited numeric/error/precedence observations remain
separately classified under541/526; this is not a blanket waiver of their other
method contracts. Well-typed explicit caller override entry and caller-owned
results/exceptions retain their own controls. Inherited callback order/count,
multiply-versus-extraction staging and incidental failure opportunities are
diagnostic, not newly required equivalence.

Parent still owns independent source review, compilation and actual bounded
execution with the frozen implementation, relevant target/dispatch checks and
matching-source artifact/CI acceptance. The truthful-leaf argument plus future
test evidence can cover this finite seam; it does not close all541/579/548,
prove public huge-input execution or promise maximum-domain resources/performance.

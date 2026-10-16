# 587: inexact sqrt keeps the requested significant digits

SOURCE ONLY / UNCOMPILED / UNRUN. This candidate is confined to
`/private/tmp/krogu-math-task-587-Eir69hdD/root`. It was copied from the parent's
main48f0d00 snapshot; all240 source hashes first matched e3539c0bda309350d72ebb89656a9f54e3c7f720fe943616153050e2be493aa9.
The complete source archive and original owned files were preserved before
edits in its parent base-main48f0.tar and read-only originals/. The publication
root, historical proofs, raw fixtures and original execution evidence are not
edited. No compiler, numerical/model/helper/script/certificate/probe/test/build,
benchmark, Git/tracker mutation or numerical child process was run or acquired.
Root owns independent review, separate execution, integration and publication.

## Baseline, accepted policy and observed evidence

Original BigDecimal SHA5dd07b9f985c53f09e5c551d30b0ae0c4faeea705343285a91eac1d9282f1e1c;
original DecimalSquareRoot579 SHAce779a45a568a49f54c631359c6e13314ee8d05272c6d915dbc12bf7bc7be408;
unchanged BigInteger SHA12fd0a14164bfd81b004c66673ae37ea40719bf3552f171ba20b434a296d8602.
The accepted private plan is preserved unchanged as originals/PLAN.md,
SHA562c860e4b51fb9435e6de61abf100665ac489e678aeccc64991ed7cec14a370.
Final candidate code/test/document pins are in the adjacent handoff and parent
prepared.sha256. No historical source pin is relabelled as current candidate.

The policy adopted by the parent follows the literal combined public Java25
BigDecimal class precision/cohort text and sqrt(MathContext) contract:
https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html
The general precision paragraph explicitly includes infinite square-root
expansions. A shorter preferred cohort is described for an exact result that
fits the precision. The sqrt-specific preferred-scale and ulp clauses do not
authorize replacing an inexact p-digit result by fewer significant digits.
Thus an inexact root at positive p retains p significant digits, including
coefficient zeros after rounding; finite exact roots and zero retain their own
preferred cohorts. This is the parent's accepted modulo-JDK-bugs disposition,
not a rule inferred from numerical identity or a missing error sentence.

Preserved baseline diagnostic54-call evidence:
`/private/tmp/krogu-587-sqrt-v2-run-IcrJpEaK`, index
2eeb8501b461e141ab156526dadbe36d512b2d57c7d027896eea0192da1e40f7;
ACTUAL_RESULT.md5608772e5df9cdf33d645e793534d983994d908a92e8684563c904b6464c643c;
observations.tsv124b13298034f50ecccbdf00ba922a0f963c32d690c6c26e3d1df84fe39da497.
Those actual observations belong to unchanged source240/JAR1cacc, not this
candidate: both own and JDK returned2.000000 rather than literal-p8
2.0000000 in five selected modes. They record representation differences,
zero numeric/type failures and no unexpected exception; they do not execute
this repair, establish a foreign implementation cause or justify normalizing
every JDK comparison. The original rows remain intact.

Historical premise R permitted optional inexact zero stripping. Its giant
compressed-cohort example was conditional on R. That remains true historical
reasoning under that premise; this policy does not adopt R. In particular
q=2^29,p=2q+1,x=(10^q+2)@q has a supported shorter UP/nearest numeric cohort,
but its REQUIRED p-digit cohort under the accepted literal policy is
unsupported. This disposition does not rewrite historical579 evidence or
claim that numerical equality alone proves representation conformance.

## Exact prefix, metadata and changed seams

BigDecimal.sqrt preserves verbatim the negative/zero prefix, preferred S/2,
complete finiteExactSquareRoot/finiteSquareRootResult call, p0/UNNECESSARY
branch, coefficient sign capture, stored-zero branch with its precision call,
adjustedExponent call, root-exponent/working-scale arithmetic and cheap gate.
The finite-root helper/result bodies are unchanged (only later line numbers
shift). No public metadata call is added or deleted in that prefix.

At candidate BD637–650, the prior positive-shift/high-p/stored-digit eligibility
test still requires captured exponent = stored D-S-1. Inside that equality,
the new pure internal inexactPrecisionOverflows(p) check precedes normalization
work. Failure of the materialization estimate still selects raw579 whenever
the required final precision is not necessarily unsupported; it is not a
replacement for the raw fallback. Misleading reported exponent retains the
original route. The new size check is not put before the finite-root prefix.

At BD658 and671–672 the already-computed exactness flag is retained. If exact,
the former roundValue(context).stripTo(preferred) suffix is preserved. If
inexact, the same roundValue result is returned without optional zero deletion.
This preserves the exact behavior of any reached materialized exact result,
even though a complete ordinary finite-root prefix normally handles it first.
Midpoint565 arithmetic, mode choice and roundValue carry handling are unchanged.

At raw helper62, inexact is exactly a nonzero final integer remainder. Its
restoring invariant, digit search, raw arrays, rounding selection and sole
all-nine carry normalization are unchanged. At helper82, the preferred-scale
zero-tail loop is now conditioned on !inexact. Exact internal callers retain
their existing compressed cohort and Long-scale rescue BEFORE final public
range/scale checks. Inexact callers retain all p significant digits, even if
their integer coefficient ends in zeros. No second rounding occurs.

There are no new decimal fields, caches, serializer mechanisms, public API
signatures, numerical kernels, changes to byte normalization or BigInteger
source. The internal helper adds one pure comparison, not a public option or
synthetic maximum.

## Natural necessary range bound and retained raw route

Let L=2147483647, H=646456993. The already accepted528/566 logarithm theorem
records bitLength(10^H)=L+1. A nonzero coefficient with p>H decimal digits is
at least10^(p-1)>=10^H>2^L-1. Hence the required coefficient cannot be published
under the existing own magnitude limit. All-nine increment does not evade
this: its exact division by10 leaves p digits, not one digit unless p=1.
This is a necessary actual representation bound, not an arbitrary p cap;
p<=H does not imply that every H-digit coefficient is supported.

Why it applies only after the complete finite prefix: for ordinary C>0 at
even scale S, a finite root exists iff C is an integer square. For odd S,
it exists iff C=10r², detected by division by10 and the integer remainder/root
tests. A rational root of a finite decimal has only factors2/5 in its
denominator and is finite, so prefix null means irrational in this ordinary
truthful domain. Finite roots, including exact2 from4 at p=MAX, bypass the new
size check and need not manufacture p digits. Zero likewise returns earlier.
An inexact irrational result cannot have an exact shorter preferred cohort
under the accepted literal policy.

The unchanged gate reaches every ordinary p>H case: D<=H, so
h=2p-D-1+epsilon>=H+1>0, with epsilon in{0,1}; also p>MAX/8. The stored exponent
equality is true for the ordinary value. Thus the necessary rejection happens
before tenPow, a p-sized raw loop, working-scale narrowing or public coefficient
publication. The existing UNNECESSARY failure still precedes this route.

For 1<=p<=H, t=p-1-floor((D-S-1)/2) is in
[-1396970320,1720198816]. A single carry decrement stays Int. The raw helper's
actual final binary check still handles unsupported boundary H-digit
coefficients. Internal exact raw clients with a Long t continue stripping
zeros before checking final Int scale; this case is deliberately retained.

Raw579 remains necessary BELOW H. With C=2,S=0,p=400000000, h=799999998;
the virtual radicand has799999999 digits and cannot be a public coefficient,
but the required rounded p-digit root is below2*10^(p-1)<2^(4p)<2^L and has
scale399999999. canMaterialize still declines and inexactPrecisionOverflows
is false. This is symbolic and UNRUN, not a giant runtime or fixed-heap claim.

The original public scaleOverflowTest input, valueOf(1,-1),p1073741825,UP,
also has a tiny stored input and irrational root. Its required p-digit
coefficient exceeds H. It now reaches the above truthful check before any
giant normalization. A bounded public regression prepares exactly that input.
Restoring the original public TEST call is a ROOT-owned separate adapter scope;
no harness file changes or claim that the restored call passed are made here.

## Arithmetic, scale and workspace preservation

Materialized rounding still computes a=floor(sqrt(n/d)), exact iff both
delta=q-a² and division remainder are zero. Its midpoint comparison is
unchanged565. For inexact ordinary inputs normalization makes a p-digit grid
integer; increment preserves p digits unless it carries through p nines.
roundValue's exact carry division then normalizes to p digits and decrements
scale once. Removing a later optional strip changes representation only.

Raw integer remainder r satisfies0<=r<2a+1. r>a is above half; equality is
below half, since an integer radicand cannot equal(a+1/2)². Its rounding choices
and allNines tracking are unchanged. After carry normalization an inexact
root still has p digits. Exact integer raw inputs may delete only zero digits,
so their preferred-cohort adjustment cannot change numeric value.

The previous helper's loop/index/primitive/live-buffer bounds remain valid:
root, remainder and trial arenas at capacity ceil((p+3)/9)+1; input chunks
<=71828555 cells; binary scratch<=71582790 words; raw primitive trial products
below10^11 and binary Horner parts below2^60. No capacity, index increment,
alias, input extraction, byte publication or staging path is added/changed.
Inexact zero stripping is removed rather than replaced with another buffer;
exact stripping remains before publication. This preserves mathematical
bounds, not an OOME/time guarantee. The source241 candidate is not yet built.

## Prepared independent tests and the two narrowly changed expectations

New DecimalSquareRootCohort587Test has FOUR methods, all UNCOMPILED/UNRUN:

1. independentLiteralGridForOrdinaryAndTruthfulCallerSuper prepares exactly54
   OWN primary calls: three inputs times eight p8 modes plus p0 HALF_EVEN,
   through ordinary and truthful explicit-super routes. It uses strict literal
   coefficient/scale/precision/text or AE assertions, never a JDK oracle.
   1.99999995²=3.9999998000000025<3.999999999999 proves the first midpoint;
   1.41421355²=1.9999999650036025<2 and adjacent grid squares prove sqrt2.
   Exact4.000000000000 retains2.000000 preferred scale6.
2. rawInexactZeroTailAndCarryKeepAllSignificantDigits calls the actual internal
   raw kernel with bounded genuine C/h/p values. The first row rounds a
   non-all-nines floor to20000000@7; sqrt999990 has floor999 and is above999.5,
   so all-nine carry yields100@5 rather than a shorter cohort. UNNECESSARY is
   independently required to fail on the irrational raw input.
3. exactFiniteAndZeroCohortsBypassInexactDigitLimit covers all modes at p0,p8,
   pMAX, extreme signed zero scales, and the retained exact raw Long-scale
   rescue1*10^20 ->100000@MAX after five exact zero deletions.
4. naturalInexactDigitBoundaryAndRequiredRawEligibility tests the REAL H/H+1
   selector symbolically, the below-H necessary raw seam, original public
   sqrt10 input and tiny sqrt2 at above-H/MAX precision. These public failures
   require no giant input, fake cap or p=H raw execution.

Existing579 oracle uses independent division-based Long bisection and midpoint
squares. Its only decision change is to strip expected zeros when its exact
square test is true. Its old raw carry expectation1@3 becomes10@4, retaining
two digits; all exact stop/rescue inputs remain unchanged. Original test
SHA0e406179a587a29c80995c8c0ac7c94586b8d43b0d04cd334d57f15d622c4580
is preserved read-only. No oracle arithmetic, bisection bounds, coefficients,
mode grid, comparison kernel or other expectations are changed.

Additional parent-authorized565 correction is exactly sqrt(4.001,p7,HALF_EVEN):
old200025@5 ->2000250@6. Original565 test
SHA78bea9b16239a9675f527844803394cb7a06732c43eb345174af56acd6389853
is preserved read-only. The input is irrational (odd scale3 and4001 not
divisible10). Independent squares:
2.000249²=4.000996062001<4.001<4.0010000625=2.000250²;
2.0002495²=4.00099806225025<4.001. Thus nearest p7 selects2.000250 with its
seven-digit coefficient. The old stripped literal is historical, not retained
as a normative expectation. No other565 case or assertion is changed.

The prepared four new methods would add16 method executions to the prior
four-target selection if all were compiled/discovered/executed: prospective
JVM164 suites/549 methods, other targets79/359 each,1626 total. These are
SOURCE-DERIVED prospective counts, not actual validation or an acceptance gate.

## Frozen fixtures and future comparison policy remain explicit gates

No generated raw fixture, existing consumer, JDK comparison, formatter or TCK
adapter is edited. Three concrete shortened raw D/sqrt rows were identified by
source reading and retained in parent evidence/known-raw-sqrt-policy-rows.txt.
The zero-based vector ordinals are derived from literal arrayOf rows, not a
new runtime replay:

| Ordinal / source line | Exact input key after D/sqrt | Retained observed literal | Literal-p representation of same numeric value |
| --- | --- | --- | --- |
|13823 /16173|0.0000001,0,20,CEILING|0.0003162277660168379332|31622776601683793320@23|
|14089 /16454|2.89086115118547718274465872620E-12,0,18,HALF_EVEN|0.0000017002532608954106|170025326089541060@23|
|15172 /17603|909393907682500.536454623858577,0,17,UP|30156158.70236958|30156158702369580@9|

The first has odd scale7 and C not divisible10. The second has odd scale41,
with C/10 congruent2 mod4, hence not a square. The third has odd scale15 and
C not divisible10. All three roots are irrational; the retained literals have
one fewer significant digit than the requested p. Appending that one zero and
increasing scale preserves their numeric value while meeting the literal
representation obligation. This does not re-evaluate their rounding numerics
or claim a complete new fixture inventory. Their current baseline values came
from the frozen oracle and prior baseline verification, not this candidate.

PortableOracleTest, ScaleBoundaryOracleTest, JdkOracleTest's random decimal
comparison, DecimalConformanceTest.rootsAndPowers and other existing sqrt
comparisons remain active. For example the latter selects1.00000001 at p7:
independent inequalities put its irrational root below1.0000005, so nearest
literal output is1.000000, not an optional shortened cohort. No unobserved
JDK outcome for that case or exact count of later dynamic failures is claimed.
Any initial mismatch must be captured completely and then given a narrow
independently reviewed policy. There is no blanket JDK comparison normalization,
raw-data replacement, skip, category/message waiver or historical pass relabel.
The new necessary-size guard throws AE with text "BigInteger overflow". No
claim is made that every former oversized power/working-scale path had that
same message or competing-error chronology. Existing concrete diagnostic
assertions stay active; any required changed record needs its own disposition.

The first full candidate build/tests, full initial failure capture and any
later separately scoped consumer/adapter decisions remain OPEN. The actual
54-call baseline diagnostic is not testing this source. Root also owns review
and original public TEST restoration, fresh matched JAR/source evidence, all
supported platforms and hosted CI. Nothing here closes542/548/541/526 or claims
an uncontended host, practical all-range completion, full TCK or official JCK.

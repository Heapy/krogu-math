# Integral division guard precision: conditional proof and range obstruction (541/P4)

Parent adoption of the original private memo SHA-256
`3d6c7e573e3e7f00da7b51781575faf97b6077d9586623dd24b6db03f9f5ed65`.
Parent and an independent mathematical/source reviewer accepted the uncapped
witness and the stated conditional lemmas. Task582 records the required fix;
no implementation, giant execution or whole-P4 acceptance is added here.

Finite SOURCE/math memo, 2026-10-15. Only this private file is written. No code,
fixture, test, public document, Git or tracker changes; no script, helper,
certificate, numerical operation, compiler, probe, test, build or runtime execution.
Own source/proofs and official public API documentation were read, never foreign
numerical implementation bodies. Giant values below are symbolic, not allocated.

## Finding

The no-context guard precision is sufficient to recover the truncated integer
**if the contextual divide and postprocessing complete correctly**. Its cap does
not create a hidden fractional-closeness counterexample when the integer quotient
has a supported coefficient representation. However the guard does not ensure a
supported contextual intermediate. The following ordinary non-endpoint witness
already prevents a complete P4 composition theorem, even with a correct581 leaf.

Let L=Int.MAX_VALUE=2147483647, H=646456993 (maximum supported coefficient digits),
D=150000000, and set

`B=10^(D-1)+3`, `A=B+1`, both operand scales0.

Both positive coefficients have D digits and are supported: A<10^D<2^(4D),
4D=600000000<L. The exact quotient is1+1/B, so its required truncated integer
and preferred cohort are exactly1@0. All ordinary input metadata is truthful.

BigDecimal.kt:403–408 chooses, without hitting the Int cap,

`p=D+ceil(10D/3)+2=650000002 > H`.

Consecutive A,B have gcd1. Since B>1 ends in3, the reduced denominator has neither
a factor2 nor5; its decimal expansion is nonterminating. A correct p-digit DOWN
division therefore returns a coefficient with exactly p digits and scale p-1.
That coefficient is at least10^(p-1)>=10^H>=2^L, using the accepted528 threshold.
It is outside our public integer range. Thus a correct context-divide leaf must
reject its required intermediate rather than publish it, although the integral
method requires the tiny supported1@0. This is a structural intermediate-result
obstruction, not insufficient significant digits, optional endpoint padding,
poisoned metadata, an executed JDK result or a practical memory claim.

On the bound current source, normalized A>B, equal digit counts and scales0 give
shift=p-1; this exceeds646456992. The truthful wide path270–290 reaches its
nonterminating exact escape182–197, then rejects at338–339. A correct581 stream
would remove that particular power obstruction but still reject the required
oversized p-digit contextual result.581 alone therefore cannot make this integral
composition total over its required supported outputs.

The same range failure can occur when capped: choose D=H in the same consecutive
coefficient family, still supported because10^(H-1) has bit length L-3 and adding
3/4 leaves ample headroom. The guard exceeds MAX, selects p=MAX>H, and its
nonterminating contextual result remains unsupported while the integral answer
is1@0. The uncapped witness above is sufficient and avoids any cap ambiguity.

## Conditional truncation lemma, including the cap

Write positive magnitudes A,B with digit counts Da,Db<=H, input scale difference
S=sA-sB in Long, and X=(A/B)*10^(-S). For X>=1 let I=floor(log10 X)+1 be its
integer-part digit count. Since A<10^Da and B>=10^(Db-1),

`I <= Da-Db-S+1 <= Da+abs(S)`.

The uncapped guard G=Da+ceil(10Db/3)+abs(S)+2 is therefore greater than I.
Its source arithmetic is Long: Da,Db<=H and abs(S)<=4294967295 bound the sum
below8e9. No Int wrap is used before the positive MAX cap.
With correct p=G contextual DOWN rounding, an inexact result has decimal quantum
10^(I-p), which divides1. Rounding toward zero cannot cross the integer floor:
floor(round_DOWN_p(X))=floor(X). No lower bound on the fractional part and no
Diophantine closeness estimate is needed. Exact contextual results already have
the right value. Apply the sign afterward to obtain truncation toward zero for
all operand sign combinations. X<1/zero is covered by the existing prefix.

For p=MAX, if I<=p the same grid argument applies. If I>p and the integer part
K=floor(X) nevertheless has a supported representation, write
K=C*10^z with C not divisible by10 and m=digits(C)<=H. Thus z=I-m>=I-H.
Put delta=-S. The earlier digit bound gives delta>=I-H as well. Because
I>MAX>2H, both delta and z exceed H and hence exceed Db. Euclidean division gives

`A*10^delta = B*C*10^z + r`, `0<=r<B`.

Both terms are divisible by10^min(delta,z), which exceeds10^Db>B. Their
difference r must therefore be0. X is an exact integer. Its minimal coefficient
has at most H<p digits; any supported selected exact cohort also has at most H
digits. Correct completing contextual division consequently cannot lose a small
fraction by the cap in this supported-K case. If K itself needs an unsupported
coefficient at every Int scale, its rejection is not this lemma's counterexample.

This is a conditional numerical statement. It does not infer that the called
context division completes from the supported final K; the preceding witness
specifically disproves that inference.

## Preferred cohort and postprocessing scope

For S within Int, a nonzero integer K has minimal exact pair C0@s0, with no
coefficient trailing zero and s0<=0. The ordinary preferred integer cohort has
scale max(s0,S), subject to its required coefficient/scale being supported.
When the intermediate R has positive scale, source409–410 truncates it at0
and strips zeros only toward S; source412 pads to S if needed. These choices
give precisely that cohort. Truncation's scale-zero coefficient is no larger
than the already supported abs(unscaled(R)), so numerical support of that
particular intermediate is not inferred merely from K's later stripped form.

When X is exact and the context leaf returns a nonpositive scale, its own exact
cohort selection applies. If S>=s0, the preferred coefficient is K*10^S=A/B
and is at most A; its digits are at most Da<=G. If S<s0, the minimal coefficient
has at most I<=G digits in the uncapped case. For a capped completing supported
cohort, its digits are at most H<MAX. Hence precision does not obstruct this
preferred exact cohort in the cases just proved.

This cohort argument assumes the corresponding leaf/strip/rescale operations
complete correctly and the required chosen pair is supported. It does not
resolve S outside Int, the saturated preference at398, or admitted560 alternative
endpoint cohorts. No endpoint clamping or alternative-pair rescue is adopted.

## Context integral and pair/remainder: what is still separate

For caller precision p>0, source414–434 intentionally uses p-DOWN division.
If its result R has negative scale, it is an integer. In exact mathematical
arithmetic, the check

`abs(source - R*divisor) >= abs(divisor)`

is equivalent to abs(source/divisor-R)>=1, and detects loss of an integer unit
under toward-zero approximation. This does not prove feasibility of its exact
public multiply/subtract at422. Correct exact expressions alone do not guarantee
their public coefficients/scales or scratch fit. Limited-precision cohort choice
and all such resource/range compositions remain P4 obligations.

Pair methods445–453 select Q through these integral methods and then compute
the exact remainder by source-Q*divisor; remainder455–459 selects that slot.
The witness already makes the no-context integral call, context precision0
delegation, and their dependent pair/remainder calls fail before this composition.
The independently required pair/remainder for the witness is [1@0,1@0], since
A=B+1. No callback-trace, allocation-identity or arbitrary subtype guarantee is
inferred. This memo stops at the established obstruction instead of claiming
the wider P4 residual/cohort/resource proof or proposing mirror tests.

The public [integral quotient contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#divideToIntegralValue(java.math.BigDecimal))
requires the integer part and preferred scale difference. The
[context overload](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#divideToIntegralValue(java.math.BigDecimal,java.math.MathContext))
adds the quotient precision restriction; its rounding mode does not select a
different integer part. The witness concerns no context or unlimited delegation,
not permission to ignore a finite caller precision restriction.

## Pins and evidence limits

| Read input | SHA-256 |
|---|---|
| BigDecimal.kt | `1c1559c44de0a51bfcd7df0c325dfb9de22e7b6a4588b6dcb612370dc850fa55` |
| BigInteger.kt | `b2fb23547d97f3f7650b6d7dcf4ad53939efb3abac97d9d152376e67dd6685f5` |
| DECIMAL_METADATA_COMPARISON.md | `4a401448e260d34f358fee230095a97b3f93b2854b38c20d54f7459a06e87244` |
| docs/decimal-overload-contract-inventory-541.md | `3b536dfc1ca283847b3e402b46cff928a33da2eed63f4a6a0e871f0ccc19cec8` |
| docs/decimal-overload-541-verification.md | `8244d2da211aa92cc7def0968ef09c81fac6229fe8b13185ef7a70a0101ed5a8` |
| frozen581 proposal | `03337ca552cfd18bde80382fc56977c50457701f65d5fa895b93fd743f82a963` |

581 is not adopted into this bound root. The parent reports its initial isolated
campaign failed two JVM methods; no later success or source revision is assumed
here. The counterexample applies even if its contextual leaf is subsequently
correct for every supported required contextual output. Existing N1/N2 finite
541 controls are not all-input guard/feasibility proofs. Maximum-domain allocations
and time remain548; source/spec review and any future implementation/execution
are parent decisions. Finite memo complete; all owned calls terminal, no window wait.

Parent subsequently independently accepted the uncapped witness and created
local:582, with541/548 depending on it, at main
`b06aaf8d04a2168e4094f79b44d1e9d5b169aaa6`. This records the parent's task
accounting only; this author performed no tracker action or task transition,
and received no implementation or execution grant.

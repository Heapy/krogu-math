# Contextual pow: operational refinement and accuracy scope (542)

Historical source/math memo adopted after independent review. Its original
private bytes remain at `/private/tmp/krogu-542-pow-accuracy-scope.md`, SHA-256
`e5027a10fa9531e3d7470115cc7245468831ab1b0273c66ea3439e29e0b0c390`.
Adoption changes no code or assertion and does not close542 or581.

Finite SOURCE/spec and mathematical audit, 2026-10-15. This new private memo is
the only file written. No code/test/fixture/public-document/Git/tracker changes;
no script, compiler, helper, certificate, numerical operation, probe, test, build,
benchmark or runtime execution. Own source/proof and official public API text
were read. No foreign numerical implementation body/source/bytecode was read.
All formulas below are paper arguments, not newly executed certificates.

## Conclusion: the actual compatibility obligation

The public [JDK25 pow contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#pow(int,java.math.MathContext))
specifies exponent validation, a repeated-squaring single-accumulator computation
with enlarged working precision, a working-precision reciprocal for negative
exponents, and destination rounding. Its accuracy description is qualified by
“In general” before the two-ulp statement. The class also warns that pow may
occasionally differ from a once-rounded mathematical power by more than one ulp.

Required compatibility therefore includes the described algorithm and rounding
semantics, its documented error conditions and qualified accuracy claim. It is
not exact x^n rounded just once, and the qualification must not be rewritten as
an unconditional universal two-ulp theorem. Conversely, the qualifier is not
permission to ignore incorrect leaves, arbitrary numerical errors or actual
evidence inconsistent with the stated algorithm/accuracy.

For ordinary inputs with correct, completing rounded leaves, the source refines
the prescribed operational computation. An independent sharp cumulative-error
theorem is useful additional assurance, but its absence alone does not create
a new acceptance barrier beyond that public contract. Refinement can be argued
from the public algorithm and the own leaf proofs without independently
rederiving the entire external standard. Existing finite counts are separate
evidence, not a domain-wide accuracy argument.

Current ordinary leaf completeness is not established by that conditional claim:
the accepted581 source counterexamples remain concrete obligations. Its frozen
proposal is accepted design only, with implementation/integration/runtime gates
still separate. This memo does not close581 or whole542.

## Exact source and proof binding

| Input | SHA-256 |
|---|---|
| BigDecimal.kt | `1c1559c44de0a51bfcd7df0c325dfb9de22e7b6a4588b6dcb612370dc850fa55` |
| MathContext.kt | `d3690387b1345f90cc927044a1a450983dd0d2ce6e3e47054384c31979fd5261` |
| docs/decimal-pow-working-precision.md | `47f47850763ed4d0ba4c07af0bddaa27d94feebc454283a11028666af844b1f7` |
| docs/decimal-power-root-542-composition-after-580.md | `3c1e15daf61329ed10b8c27d6af00b3b7ad6618cf09cab28c3d44cc5a8242002` |
| private581 normalized-division proposal | `03337ca552cfd18bde80382fc56977c50457701f65d5fa895b93fd743f82a963` |

The proposal is `/private/tmp/krogu-581-normalized-division-proposal.md`.
Historical evidence/hashes within the linked proofs retain their original
snapshots. No proposed581 implementation elsewhere is read or adopted here.

## Source refinement and its premises

At BigDecimal.kt:537–556, for finite valid precision p and nonzero exponent n,
write k=abs(n), d=decimalDigits(k), ideal q=p+d+1. Validation gives
1<=k<=999999999,1<=d<=9 and p>=d. The source computes q in Long and caps only at
Int.MAX_VALUE; the cap is discussed separately below.

Starting from a=1, scan k's bits from most to least significant. For each bit b,
perform a=round_q(a*a); for b=1 also a=round_q(a*x). At the end, for n<0 perform
a=round_q(1/a), then return round_p(a). This is exactly the current source call
sequence. The exponent-prefix m satisfies m'=2m+b. Without rounding that
recurrence represents x^m by induction; with rounding it defines the operational
algorithm being refined. The source does not silently substitute reciprocal-first,
an exact full power, a different working precision, or repeated multiply-only.

The refinement premises are canonical ordinary stored values, valid fixed
contexts, correct leaf rounding/scale/cohort behavior and successful required
allocations. Each reached working result must complete under its supported
coefficient/scale policy.580 supplies the separately proved contextual-product
construction while retaining its genuine scale rules; the current reciprocal
division still has581 obstructions. Explicit caller-owned overrides and poisoned
subtype paths have separate541/526 classification; this ordinary argument neither
waives their required direct dispatch nor proves them all truthful automatically.

## A conservative independent rounding-error enclosure

This supplement concerns nonzero x, nonzero n, completing leaves, no cap
(q=p+d+1<=Int.MAX_VALUE), and ordinary significant-digit rounding. It bounds
numerical values, not preferred representations, exception messages or runtime.
Use real arithmetic symbolically; no decimal power or giant rational is allocated.

### One-leaf lemma and signs/modes

For a nonzero exact leaf value v, let E=floor(log10(abs(v))). Its q-digit decimal
quantum is h=10^(E-q+1). Correct rounding gives

`round_q(v)=v*(1+delta)`, with `abs(delta)<=u`, `u=10^(1-q)`.

Indeed a directed rounding error is less than h, and abs(v)>=10^E. Nearest
modes have absolute error at most h/2, hence abs(delta)<=u/2. A carry changes
representation, not this bound. Correct significant-digit rounding of nonzero
v returns nonzero when its required result completes; an arbitrary exponent
underflow-to-zero shortcut is not assumed.

More precise factor signs, relative to the signed exact v, are:

| Mode | Completed-leaf delta |
|---|---|
| UP | delta>=0, magnitude rounds away from zero. |
| DOWN | delta<=0, magnitude rounds toward zero. |
| CEILING | delta>=0 for positive v; delta<=0 for negative v. |
| FLOOR | delta<=0 for positive v; delta>=0 for negative v. |
| HALF_UP / HALF_DOWN / HALF_EVEN | abs(delta)<=u/2; ties use their specified direction/parity. |
| UNNECESSARY | delta=0 on completion; an inexact leaf rejects arithmetically. |

Thus signs remain those of the exact intermediate arithmetic. The uniform
absolute bound works across negative bases and all mode combinations induced by
the intermediate signs. In particular, a negative exponent reverses denominator
error: a final UP mode does not by itself prove the entire approximate reciprocal
lies above the exact power.

### Weighted repeated-squaring recurrence

Let X_m=abs(x)^m and write the current accumulator magnitude as X_m*F_m.
For the next bit b, the square contributes a factor (1+delta_s), and a set bit
contributes a further (1+delta_m):

`F_(2m+b)=F_m^2*(1+delta_s)*(1+delta_m)^b`.

Each prior rounding factor consequently acquires twice its previous exponent
on a square. The total nonnegative integer factor-weight budget T obeys
T'=2T+1+b. The initial square of1 is exact; the first leading set-bit multiply
can round x, so a safe initial budget is m=1,T=1, even if that particular multiply
happens to be exact. By induction T<=2m-1:

`2*(2m-1)+1+b <= 2*(2m+b)-1` for b in{0,1}.

At completion of the positive-power phase, with T<=2k-1,

`(1-u)^T <= F_k <= (1+u)^T`.

This counts the initially rounded base input correctly; it is not a claim that
only O(log k) unweighted rounding errors are amplified. Negative bases satisfy
the same magnitude relation, with the exact exponent-parity sign.

For n<0 the reciprocal contributes one more q-rounding factor and inverts F_k,
so its ratio to the exact reciprocal magnitude lies in

`[(1-u)/(1+u)^T, (1+u)/(1-u)^T]`.

Since 0<u<1 and (1-u)*(1+u)<=1, both signs of n are safely enclosed by

`(1-u)^(2k) <= F_work <= (1-u)^(-2k)`.

For our uncapped q, u=10^(-p-d). Because k<10^d, set

`t=2k*u < 2*10^(-p) <= 1/5`.

Bernoulli's inequality for the integer exponent2k gives
(1-u)^(2k)>=1-2k*u=1-t>0. Consequently

`1-t <= F_work <= 1/(1-t)`.

For completeness, the inequality follows by induction from
(1-u)^(r+1)>=(1-r*u)*(1-u)>=1-(r+1)*u whenever the latter bound is used with
r*u<1; for all r<=2k that positivity holds here. No floating logarithm estimate
or sampled rounding behavior is used in this enclosure.

### Final destination rounding

Let v_p=10^(1-p). The final rounded result has an additional factor between
1-v_p and1+v_p, measured against the actual working value, not the exact power.
Its signed result/exact-power ratio F_final is positive and satisfies

`(1-v_p)*(1-t) <= F_final <= (1+v_p)/(1-t)`.

Therefore a convenient conservative relative-error bound is

`abs(F_final-1) <= (v_p+t)/(1-t)`.

This remains valid but coarse at p=1, where the lower estimate may be zero.
For nearest modes, the corresponding one-leaf u and v_p may each be halved
throughout the same argument. Ties, carry and negative signs require no change
to the enclosure once the one-leaf lemma holds.

This is deliberately **not** a sharp two-ulp theorem. Translating a relative
enclosure into a sharp ulp statement must account for decimal decade crossings,
the result's selected precision/scale/cohort and final rounding boundaries.
A bound too coarse to prove two ulps is not evidence that an actual result
violates the public description. Nor is an example within this coarse bound
automatically acceptable if its leaf/operational result is wrong.

## Zero, exact mode, capped work and range failures

* n=0 returns ONE by its explicit source/public rule, including zero base.
* For n>0 and x=0, any returned numerical result is exactly zero; its scale and
  zero orientation follow the separate multiplication/power rules. Relative
  error is undefined at zero, so the nonzero enclosure is not applied.
* For n<0 and x=0, the reciprocal's division-by-zero rejection remains required.
* Precision0 delegates to exact pow; negative n is rejected there. Exact positive
  power, scale/coefficient range and resource behavior are separate contracts.
* UNNECESSARY can reject at an inexact working or final leaf. If all leaves
  complete, each is exact, hence the returned numerical power is exact. Do not
  infer that an exact *mathematical final* power forces every prescribed working
  intermediate to be representable or makes every scale error a port defect.
* If ideal q>Int.MAX_VALUE, do not substitute the uncapped u formula above into
  the actual capped computation. The existing conditional cap argument is
  `docs/decimal-pow-working-precision.md`. Here p>=MAX-9 and actual q=MAX, both
  far above the supported public coefficient digit bound H=646456993. Under
  correct significant-digit leaves, an inexact q-digit result would need more
  than H digits and cannot be a completed supported public coefficient. Thus
  completing supported work leaves do not require working rounding; the same
  numerical exactness applies at a larger ideal precision. Destination rounding
  of their supported coefficients is also exact. This is conditional value
  equivalence, not a proof that unsupported intermediates or nonterminating
  reciprocals must complete. Recomposition of actual581 source must preserve
  these premises/cohort policy; the old power-ceiling failure is not reused as
  a mathematical accuracy argument.
* A genuine prescribed working coefficient/scale failure is distinct from an
  unsupported artificial normalization temporary. The qualified negative-unit
  boundary and admitted560 endpoint policies remain as documented.581's1/3
  reciprocal and post-round exactness-product witnesses are concrete examples
  of the latter obstruction; this enclosure cannot repair them or make their
  required successful calls vacuous promises.

## What remains, and what is not a new gate

The concrete compatibility work is to validate the actual reached rounded
leaves and their supported required result domain, integrate581 without changing
the prescribed recurrence or scale/cohort rules, preserve explicit caller/null/
error contracts, and bind the changed source to appropriate platform/public
evidence. Existing proofs may supply leaf premises within their stated scope;
one need not reprove every kernel or an external standard from scratch.

The conservative recurrence above supplies independent quantitative assurance
for its stated nonzero uncapped domain. It does not replace those concrete
source/leaf obligations, prove every subclass/runtime/resource path or establish
whole542 completion. Finite power/oracle campaigns remain observations of their
bound sources, not the proof of this error enclosure.

If a stronger public project claim is later proposed, such as universal two-ulp
accuracy for every completing call, its precise missing lemma is a sharper
rounding-grid analysis converting the weighted error recurrence through
reciprocal and final rounding into that specified ulp bound, with decade/carry/
cohort cases and all signs/modes/cap domains stated. The API's qualifier does
not itself supply that universal quantifier. Proving this new stronger statement
is not imposed here as an extra acceptance criterion for faithful implementation
of the actual prescribed algorithm. The existing qualified accuracy description
must still be retained honestly; any substantive observed or proved discrepancy
requires its own normative classification and independent explanation.

Finite scope complete. All owned read/hash/document calls are terminal; no
runtime window was acquired, requested or awaited.

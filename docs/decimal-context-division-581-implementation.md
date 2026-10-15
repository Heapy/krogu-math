# Finite contextual division581: prepared implementation argument

SPDX-License-Identifier: Apache-2.0

**SOURCE ONLY / UNCOMPILED / UNRUN / NOT INTEGRATED.** This candidate is confined
to `/private/tmp/krogu-math-task-581.xC0S84dl/root`, a no-Git archive of
`80987e04f11a5abd8bc45b4646bcfe5bd0312043`, under parent source grant2335.
Only the contextual-divide seam, one internal integer bridge, new helper, two
separate tests and this proof/handoff are owned. Publication-root and other
workers' files are untouched. No script, compiler, numerical helper, certificate,
probe, test, build, benchmark, measurement, Git or tracker command ran. No foreign
numerical implementation body or bytecode was read. Parent owns independent
review, any integration and every later execution grant.

The accepted full-stream proposal is
`/private/tmp/krogu-math-task-581.xC0S84dl/accepted-proposal.md` outside this root,
SHA `03337ca552cfd18bde80382fc56977c50457701f65d5fa895b93fd743f82a963`.
Original BigDecimal/BigInteger/proposal were copied and hash-checked BEFORE edits
to read-only `/private/tmp/krogu-581-originals.dyPRTKc2/`. Original source pins
are BD `1c1559c4…`, BI `b2fb2354…`. The proposal's ten existing input/proof pins
remain historical bindings, not execution of this candidate.

## Exact prepared source and diff

| Candidate | SHA-256 |
|---|---|
| BigDecimal.kt | `24e4e63001ea72a2fed9d29361f3707ad59cbe59c0547d47a648993146cf0849` |
| BigInteger.kt | `e284f3aac0a09d293784d38265d00656dec632d18a61985c756dc42c0c4d6d31` |
| DecimalDivision581.kt | `9f882c2ee58fc6e38d41819e7d258d382088a7a9ecb0f8fd538b296f7b484446` |
| Common test | `c198132e64c3ac1f9815c6227b2322d041d972f079207edb6d0eec94fa74a6e1` |
| JVM test | `12fad4905a903927a630b227da5cc8d24ba98959d2fc724bda9d07355f7f2c41` |

BigDecimal's sole diff inserts284–286 after the unchanged zero/sign/precision/
normalized prefix and Long target/shift calculation. A non-null bridge result
is the final checked pair. The complete old body remains as the null-result
fallback, including existing p0 exact delegation, diagnostic paths and fixtures.
BigInteger's sole diff inserts the nonvirtual bridge92–106. No existing kernel,
public declaration, instance field, cache, serializer or580 multiply path changes.
The new helper's fields are private per-invocation scratch; its nine literal
decimal powers are private read-only constants, not a result cache.

Use L=MAX=2147483647, MIN=-2147483648, H=646456993 and T=10^9 below. Canonical
immutable nonzero input magnitudes have at most L bits and H decimal digits.
The theorem is ordinary finite-context arithmetic, all positive Int precisions,
all modes and signs, subject to the existing cohort policy, exact leaf contracts
and successful required allocation. Zero/p0 routes remain unchanged.

## Conservative materialized path: actual sufficient predicate

The top-level `materializedDecimalDivision581` (helper5–16) reads no raw object
or buffers. It requires positive stored bit lengths/p; preferred S in Int;
target Nscale in(MIN,MAX]; shift h in[-646456992,646456992]; and Long bounds

 bitsA+4*max(h,0)<=L,
 bitsB+4*max(-h,0)<=L,
 4*p+bitsB<=L.

The shift interval is checked before negation/multiplication, so even the
symbolic Long.MIN_VALUE test cannot overflow. Requiring target>MIN reserves
room for the one possible later scale decrement. Failure selects another
complete route, never an argument rejection or precision reduction.

For truthful captured metadata, let e=floor(log10(A/B)). The unchanged target
is W=S+p-1-e except normalized equality, when it is W+1 and the scaled ratio
is exactly10^p. Otherwise the scaled ratio lies in[10^(p-1),10^p). Consequently
its integer quotient is nonzero; after one magnitude increment it is <=10^p.
Because10^p<2^(4p), this temporary, any increment and the final coefficient
have at most4p bits. The last inequality proves those supported and proves
the exactness comparison's final C*B supported as well.

The first two inequalities bound the actual scaled numerator and denominator:
10^k<2^(4k). Every power is within the accepted528 ceiling, and each multiply
result is supported. Existing pow/multiply lower-bit guards cannot reject a
supported actual product. The530 divide kernel takes supported operands;
quotient and remainder stay within its numerator. Since q>=1, N=qD+r gives
N-2r=qD-r>=D-r>0, proving the public doubled remainder supported too. The
possible p+1-digit result is exactly10^p; roundValue drops one exact digit,
never performs two inexact roundings. Its target-1 scale is Int by the strict
lower target bound. All its powers are small (at most10), and final C*B fits
as above. Preferred stripping decreases scale only toward in-range S.

The old wide-condition is false: preferred/target are in range and |h| is below
the power ceiling. Thus this path cannot invoke exactDivisionCoefficient or
divideWideContext behind the proof's back. The old compact-overflow branch
is impossible for truthful inputs: when Da<=Db<18, A*10^(Db-Da)<10^Db<=10^17.
Its normalized-equality shortcut returns the exact unit ratio at scale
S+Db-Da, between S and target, hence in Int. All other compact paths join the
same proved materialized arithmetic. Incorrect captured metadata keeps the
same old path when this predicate succeeds; this proof does not classify it.

This retains ordinary source behavior/cost structure, with extra stored bit
reads and scalar checks. No measured timing, allocation improvement or deadline
compliance is claimed. Compiler code generation is not inferred from source.

## Eligibility, large precision and supported own leaves

After a failed sufficient predicate, BI100–101 checks both captured digit
counts and normalized order against nonvirtual528 stored helpers. Stored zero
declines at95. There is no runtime-class test or extra public metadata callback.
Mismatch leaves the original diagnostic route; a match is eligibility for the
stored-value theorem, not a declaration that every poisoned subtype is valid.

For p>H, BI102–104 supplies fresh **base-class** positive wrappers over each
immutable stored magnitude. `largePrecision` (helper26–50) never receives the
caller's dynamic coefficient object. Its reachable public integer leaf calls
therefore operate on ordinary base receivers/results, with no caller override.

Gcd uses the existing supported remainder kernel; its positive nonzero values
decrease. Dividing A and B by gcd yields coprime n,d bounded by the inputs.
Remove denominator twos by its stored lowest set bit and right shift; repeated
exact divisions by5 then find whether the remaining denominator is1. Both
outputs of each53 division are supported, positive denominator quotients decrease,
and the final nonzero remainder stops the loop. Twos/fives counts are <L and
fit Int. No oversized public normalization operand is made.

If a factor other than2/5 remains, the quotient is nonterminating. Every finite
context result is inexact and retains p digits even after carry, so p>H would
require a coefficient>=10^H>=2^L. The early AE is a necessary final-range
decision, not a smaller precision cap. In particular tiny1/3,p=MAX does not
enter a billion-digit loop. Mode UNNECESSARY also requires an AE; wording and
multiple-invalid priority are not promoted into a new contract.

Otherwise put k=max(twos,fives), exact natural scale E=S+k. The natural coefficient
is C=n*2^(k-twos)*5^(k-fives). With a nontrivial reduced denominator, coprimality
ensures C has no trailing10 factor: one of2/5 is absent. If the denominator was1,
C=n<=A and is already supported at preferred S. Thus any supported exact C has
<=H<p digits and is already the required natural cohort, without a precision pad.
Checking E before constructing C is sound: if C fits, E is that required final
scale; if C does not fit, either its minimal exact coefficient or any p-digit
rounded coefficient is unsupported regardless of E. This is not a claim that
the natural scale would be the rounded scale for an unsupported C with >p digits.

If C is unsupported, either its minimal exact coefficient is outside the range,
or a precision reduction would still require p>H digits. Neither can yield a
supported required result. It is consequently safe for the own shift/power/
multiply leaves to discover that final obstruction without a catch/retry.
Conversely, when C fits, the shifted n,5 power and all positive intermediate
products are <=C. For base5 specifically, existing pow has twos0, bitLength3;
its2*exponent>=L guard implies5^exponent>2^L. Compact3*exponent<=64 means
exponent<=21, and5^21<2^63, so the scalar path cannot overflow or take its
leading-word-two special case. In the other path all exponentiation-by-squaring
powers have exponents<=the requested exponent. Thus a supported C cannot be
falsely rejected by these own range guards. No full arbitrary-base pow theorem
is inferred. Non-AE failures/OOME propagate; there is no resource-as-range catch.

Scale E is Long-safe (S in[-4294967295,4294967295], k<L). A natural exact LOW
scale remains rejected, and HIGH exact p>H remains the same qualified diagnostic
policy: selecting a different optional endpoint cohort is not introduced.
The existing560 1/2,p=MAX case returns5@1 promptly. The accepted530 and528/580
own primitive proofs remain dependencies; no foreign implementation is used.

## Actual decimal stream and cohort proof

For p<=H, the bridge invokes `rounded` (helper54–106), whose generic arithmetic
also remains Long-safe for any positive Int precision. It converts immutable
inputs to private T-cells using one reusable copied binary scratch
(`decimalChunks`,137–154). For r<T and limb<2^30, part=r*2^30+limb<2^60, so
quotient limbs and decimal remainders are exact. The per-input digit bound is
min(H,10*wordCount), valid since2^(30*n)<10^(10*n) for positive word count n.
Only the copied active range is read after reuse. Inputs are never destinations.

`trailingZeros`215–221 finds A's last nonzero input position without a suffix
copy. Canonical A has a nonzero high cell, so its low-zero scan terminates.
At each feed position,73 forms U=10R+a_digit<10B. Trial product239–248 is
recomputed into one V for digits9 down to1; if none fits, digit0 needs no
subtraction. The chosen digit is the largest fitting one; subtraction257–267
leaves0<=R<B. Trial active size is reset on every product, ignoring old high
cells. Products with positive factor have nonzero high active cell or carry.
The window and trial may exceed the public bit limit; they are private cells.

The accepted proposal's Pj=B*Qj+Rj induction applies to the actual loop. Before
all original A digits are consumed, its fractional residual includes the unread
original tail; afterward it is R/B. Line83 tests both R==0 and position at/past
the saved last nonzero input digit. A zero R with an unread nonzero suffix is
not exact. Leading quotient zeros are skipped; the first nonzero position j0
is Db or Db+1. Line88 captures e=Da-j0. Q retains at most p digits; the next
digit is guard g. The new residual is nonzero exactly when line83 is false.
Line96 therefore classifies g<5, g>5, true g5 ties and g5 with sticky correctly.
Low-cell parity handles negative ties as magnitude parity. All eight mode
decisions119–128 match the accepted signed rounding table.

Only an all-nines increment increases Q to p+1 digits. Lines99–102 divide its
exact10^p by10 and lower scale once. There is no second inexact rounding and
no optional inexact trailing-zero stripping. `exactResult`108–117 obtains the
minimal exact C0@E0 privately, then pads z=max(0,min(S-E0,p-digits(C0))) zeros.
It checks the already selected final scale E0+z before padding. This equals
stripping the padded exact p-digit quotient only toward preferred S. It keeps
1250@0, the558 inexact10@MAX cohort, and all prescribed scale/carry behavior.
It does **not** clamp an out-of-range exact cohort to MIN/MAX. In particular
1@MIN /1@1,p2 and10@MAX /1@-1,p2 keep their admitted diagnostic AE, while the
established1@MIN /2@1 and one-digit HIGH witnesses succeed. No optional policy
extension or claim of all numerically equivalent cohorts is made.

Final publication156–185 happens only after rounding/carry/required cohort
selection. Decimal Horner conversion uses part=limb*T+carry<2^60 and one extra
binary cell. The bit length is Long; every prefix is <=final C. A first crossing
of L proves final C unsupported and needs at most L+30 temporary bits. Binary
capacity uses min(L,4*decimalDigits) plus one cell, safely sufficient whether
the coefficient fits or the first crossing occurs. On success byte count is
<=268435456. Long8*i and bounded offsets extract the magnitude; the unchanged
signed-magnitude constructor receives an already supported value for either sign.

## Index, primitive and simultaneous-storage bounds

All decimal cells lie in[0,T). multiply-by10/add-digit uses part<10T and carry<=9;
trial*d uses part<9T and carry<=8. Both promote to Long first. Subtraction's
cell difference is in[-T,T-1] with a one-bit borrow; nonnegative trial ordering
guarantees its final borrow zero. Division by10 has part<10T. Canonical active
length, not capacity, controls comparisons and digit counts. Input digit
positions, significant count,9*cell, scale and padding arithmetic use Long.
One decimal carry can transiently have p+1 digits, including Int.MAX_VALUE+1
for a direct generic helper caller; `digits()`208–214 deliberately returns Long.

Public stream calls have p<=H; its maximum source positions are2H+1, with Long
indices regardless. The generic helper's positive-Int bound is Db+1+p<=2793940641.
Every scale stays below7.1e9 in absolute value. Input arrays have at most71582789
binary words. Seven independent paged workspaces may all remain live:

| Workspace | Maximum logical cells |
|---|---:|
| Shared input binary scratch |71582789|
| Decimal A and B, each |71828555|
| Decimal remainder/window and trial, each |71828556|
| Decimal quotient, generic Int p |238609296|
| Decimal quotient, actual public p<=H |71828556|
| Final binary scratch |71582790|

All capacities are below Int.MAX_VALUE. Derived capacities narrow only after
Long arithmetic; b.size+1 is separately bounded by71828556. Nullable fixed page
tables188–198 have at most29128 entries. A checked index gives slot*8192<=index,
so that Int product cannot overflow. The allocated last page is shortened to
the exact remaining capacity. Unwritten pages read zero; active cells have
been written. There are no growing page-table copies or per-trial allocations.
Input byte conversion never retains mutable scratch in a result.

Also count both original immutable magnitudes, all seven page tables, output
ByteArray, final public magnitude and any constructor trim copy. The stream
proof assumes no earlier scratch has been collected. The large-precision
branch instead retains its two ordinary wrappers, gcd, reduced numerator,
current denominator and current two-slot factorization result, followed by
the shift/5-power/product operands and each leaf's proved private workspace.
It allocates none of the seven stream buffers. Euclidean/factor loops release
references to old values but no fixed-heap claim relies on collection; total
allocation and time remain548 obligations. No maximum-sized execution is claimed.

## Prepared independent tests and truthful seam

All five new common methods and the one JVM method are UNCOMPILED/UNRUN.
The common oracle26–59 uses a single bounded Long rational numerator/denominator,
power-of-ten normalization and r versus denominator-r. It shares no trial,
digit stream, paged buffer, sticky extraction or production rounding helper.
Its checked products stay below4e18; powers are<=10^18; q increments are at
most10^10, signed results fit Long, and scale operations are Long.

The first method has16 input pairs, two signs, five precisions and eight modes
(1280 prepared comparisons). The scale/cohort matrix has6 pairs,6 preferred
scales,3 precisions,two signs/eight modes (1728), plus four independent literals.
Both raw input arrays are checked unchanged on expected success/failure.
2000000000/1000000001 explicitly subtracts1000000001 from window2000000000,
requiring low-cell borrow;3999999999/1999999999 uses multi-cell trial carry.
Other cases cover binary2^30 boundaries, real ties/parity, sticky neighbors,
all-nines carry and denominator1 with unread nonzero original tails.

Symbolic selector boundary tests allocate no large operands. Public tests
include the actual p>H bridge's exact1/2,6/15 and integer cohorts, prompt
nonterminating p>H rejection, and truthful explicit-super LOW boundary success
that reaches the actual stream. P0/zero and caller-owned override result/
exception identity controls remain separately required. No fake size threshold,
sentinel-only internal leaf or callback noncall/count assertion is introduced.

The new JVM test restores both coefficients as truthful subclasses using real
current-own-format serialization and resolveObject. Its class assertion proves
that bounded setup reachable, not a general serialization type/identity API.
All four sign combinations check the actual public stream at LOW/HIGH boundaries
against5@MIN and the independent .95 carry literals in every mode. Restored
1/2,pMAX separately reaches the large-precision base-wrapper branch. These are
prepared tests, not observations of any run or giant production witness.

The source seam composes because stored canonical magnitudes and truthful
retained prefix answers determine the same values. Stream arithmetic is
nonvirtual. Large-precision public leaf calls use proven base wrappers/results,
not transitive caller coefficients. No serializer/constructor rewrite or class
exclusion is needed. Dishonest/changing metadata, injected failures, replacement
objects and affected inherited staging remain541/526 classifications; no blanket
poisoned-subtype waiver or new internal trace guarantee is asserted.

## Remaining parent gates

Independent review must accept this actual source binding, fast-path proof,
large-precision supported-leaf argument, stream/cohort/buffer code, tests and
truthful restored seam before any execution. Compilation and finite target
tests may expose ordinary implementation defects; none has run here. Exact-head
CI, fresh public harness evidence and548 time/allocation investigation remain
separate grants. No public deadline or precision is reduced. Historical558/560/
580 evidence and the qualified negative-power boundary remain unchanged.
No full pow theorem, official JCK, task/whole-project completion, performance
acceptance or maximum-resource feasibility is claimed. All author calls are
terminal; no runtime window is held, awaited or implicitly granted.

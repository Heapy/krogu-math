# Numerical arguments

These arguments describe the independent common implementation in
`BigInteger.kt` and `BigDecimal.kt`, together with portable JDK fixtures. They
cover integer packing, signed arithmetic, decimal conversions, roots, powers
and primality budgets.

## Primality and prime generation

Write an odd candidate as n-1=2^s*d with d odd. A witness passes when
a^d mod n is 1, or one of a^(d*2^r) mod n is -1 for 0<=r<s.
Every prime passes by the field property that the only square roots of 1
are 1 and -1. For composite n the strong-liar fraction is at most 1/4
([Rabin, 1980](https://doi.org/10.1016/0022-314X(80)90084-0)). Excluding
the two always-passing bases 1 and n-1 makes the fraction strictly less
than 1/4 on the sampled interval 2..n-2. Independent uniform witnesses
therefore give a false acceptance probability less than 2^(-2*r) after
r rounds. `isProbablePrime(c)` uses r=ceil(c/2) for c>0. Below the strict
thirteen-base bound, the cited Sorenson–Webster theorem instead gives a
conclusive decision; c<=0 returns true before examining the value.

`randomBits(b)` samples ceil(b/8) bytes and keeps the low b bits. Each
integer in 0..2^b-1 has exactly 2^(8*ceil(b/8)-b) byte-string preimages.
Uniform bytes thus give uniform integers. Rejection outside 2..n-2
preserves uniformity on that interval. For prime generation with b>=3,
forcing the highest and lowest bits gives each odd b-bit candidate four
preimages, so candidates are uniform on a set of size 2^(b-2).
`RandomIntegerDistributionTest` exhausts one-byte payloads for each b=1..8,
and two-byte payloads for b=9 and b=16, checking equal multiplicities and
range. These finite checks do not establish the underlying RNG's quality.

The returned-composite probability of rejection sampling needs a separate
bound from a single candidate's error probability. Let p be the fraction
of actual primes among candidates and q the probability that one draw
is composite and nevertheless accepted. The eventual returned-composite
probability is q/(p+q), less than q/p. Using only c bits of candidate-test
certainty does not by itself establish c bits for the returned value.

For b>=6, set x=2^b. Rosser–Schoenfeld's Corollary 1, inequalities (3.5)
and (3.6), gives pi(x)>x/ln(x) and pi(x/2)<1.25506*(x/2)/ln(x/2)
([original paper, p. 69](https://denisevellachemla.eu/Rosser-Schoenfeld-1962.pdf#page=6)).
Since ln(x)/ln(x/2)<=6/5, subtraction gives more than
0.246964*x/ln(x), and hence more than x/(5*ln(x)), primes in the interval.
Dividing by the odd candidate count x/4 gives p>4/(5*b*ln(2))>1/b.
For b=3,4,5 the respective exact fractions are 2/2, 2/4 and 5/8,
also greater than 1/b. For b=2 both possible outputs are prime.

`PrimalityCertainty.generationRounds` therefore uses
r=ceil((c+ceil(log2(b)))/2) for positive c. Then
q/p < b*2^(-2*r) <= 2^-c. The additions use Long: even c=Int.MAX_VALUE
and b=Int.MAX_VALUE yield the positive Int round count 1,073,741,839.
The portable budget test checks the integer inequality 2^(2*r-c)>=b,
including power-of-two neighbors and Int endpoints. Independent trial
division also checks both signs throughout 0..4096.

For successor search, index the tested odd candidates by i=1,2,... and put
k=bitLength(i)=ceil(log2(i+1)). `successorRounds(k)` assigns r=50+k rounds.
There are 2^(k-1) indices with that bit length, so the sum of all candidate
false-acceptance bounds, even over an infinite search, is
2^-100 * sum(k>=1, 2^(k-1)*2^(-2*k)) = 2^-101.
A union bound therefore limits the probability of returning any composite
to less than 2^-100. This bound does not need a density estimate or a fixed
maximum prime gap. Early termination only reduces the set of possible errors.
Prime candidates always pass: below 2 the result is 2, and otherwise the
search visits every subsequent odd value, so no prime is skipped.

The index uses BigInteger rather than a wrapping primitive counter.
For initial candidate n0>=3, the ith candidate is n=n0+2*(i-1), hence
i<=(n-1)/2. Thus the counter cannot exceed the supported magnitude before
the candidate does. Round counts use Long because 50+Int.MAX_VALUE no
longer fits Int. The private witness loop also uses Long. Portable tests
sum 16 complete index groups using exact integer weights, exercise budget
endpoints, and compare every successor for starts 0..4096 with trial division.
The JDK fixtures include 81..256-bit binary boundaries and the gap following
64!+1, whose subsequent offsets 2..64 are all composite. Source comparison
is bypassed, source add is retained, and negative-start diagnostics retain
the observed source formatting/abs call; portable traces constrain those paths.

These probability arguments assume independent uniform candidate bytes
and witnesses, correct modular arithmetic and termination. They do not
prove those assumptions for platform pseudorandom generators or arbitrary
caller-supplied RNG implementations. In particular, generation with a
biased or repeating caller RNG remains a separate audit.
Extreme certainty can require a very large number of rounds; the public
[JDK contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#isProbablePrime(int))
explicitly permits long execution. Practical large-input performance and
supported-range behavior remain unproven here.

## Binary floating-point rounding

The relevant format parameters are precision `p=24`, upper bit-length `E=128`
for Float, and `p=53`, `E=1024` for Double. These follow from
[JLS 4.2.3](https://docs.oracle.com/javase/specs/jls/se25/html/jls-4.html#jls-4.2.3).
Java numeric conversion uses nearest rounding with even ties
([JLS 5.1.2–5.1.3](https://docs.oracle.com/javase/specs/jls/se25/html/jls-5.html#jls-5.1.2),
[JLS 15.4](https://docs.oracle.com/javase/specs/jls/se25/html/jls-15.html#jls-15.4)).
The argument assumes the target's primitive arithmetic and conversion implement
that policy; portable bit-pattern comparisons check the exercised cases on each
supported target.

For a positive magnitude `n` with bit length `b`, zero is handled separately.
If `b>E`, then `n>=2^E`, so signed infinity is the correct result. Otherwise set
`s=max(0,b-p)`. Reading the highest bits directly from the normalized limbs gives
`q=floor(n/2^s)`; at most 53 bits are read. The discarded part is
`r=n mod 2^s`, which need not be materialized.

When `s=0`, the integer is exact. When `s>0`, the guard bit says whether
`r>=2^(s-1)`. Sticky bits say whether any lower discarded bit is set. The private
lowest-set-bit index is less than `s-1` exactly when such a bit exists. Therefore
the increment rule `guard && (sticky || q is odd)` has three outcomes:

- Below the midpoint, retain `q`.
- Above the midpoint, increment `q`.
- At the midpoint, choose the even significand.

After rounding, `q<=2^p`, so it fits Long and converts exactly to Double.
Also `s<=E-p`: at most 104 for Float and 971 for Double. The power `2^s` is exact
in Double. `FloatingPowerTest` checks all powers from -1074 through 1023 against
its independently encoded IEEE exponent bits on every target. Multiplication
produces an exact dyadic value with at most `p`
significand bits, except when the Double result itself overflows. A carried
`q=2^p` is a power of two and also exact. Consequently a finite Float result is
already representable when the final Float cast occurs; that cast introduces
no second rounding.

The greatest finite magnitude is `(2^p-1)*2^(E-p)`. Its overflow midpoint is
`2^E-2^(E-p-1)`. At that point the retained significand is odd and the guard is
set, so the carry selects infinity. Below it the result remains finite. This
covers both the largest finite value and the carry into the next exponent.
Restoring the integer sign preserves the symmetric rounding result. Nonzero
integer magnitudes cannot underflow or produce subnormals; integer zero returns
positive zero.

For a nonzero original magnitude below `2^63`, the implementation preserves
JDK's observed virtual longValue call. Double converts that returned Long once.
Float reconstructs its exact integer magnitude and applies the rule above with
virtual dispatch disabled. Thus a replacement Long just above a Float midpoint
is not first rounded to a Double midpoint. Zero and original magnitudes at or
above `2^63` bypass that virtual call, as the Java trace fixtures demonstrate.

## Exact integral conversions

For an ordinary stored value, fitting signed width `w` is equivalent to
`bitLength()<=w-1`: positive bit length describes its magnitude; negative bit
length describes `abs(n)-1`. This includes the asymmetric negative endpoint.

The preliminary magnitude guards cannot reject a representable value: int's
negative endpoint has 32 magnitude bits and long's has 64. Short and byte use
the broader 32-bit preliminary guard and their own final signed-width test.
After the checks, the corresponding primitive conversion is exact for an
ordinary object. Subclasses can deliberately change these public methods;
the oracle constrains the observed guard order and delegation instead of
assuming overridden methods remain truthful.

## Signed integer addition and multiplication

For ordinary objects, each magnitude digit lies in 0..B-1 with B=2^30.
Addition computes a digit sum plus an incoming carry in 0..1. The maximum
is 2*B-1, so the Long intermediate is exact. Its low 30 bits give the next
digit and its quotient by B gives the outgoing carry in 0..1. Subtraction
of the smaller magnitude from the larger computes a digit difference minus
a borrow in 0..1. The intermediate lies in [-B,B-1]; masking gives its
residue modulo B, and negativity gives the next borrow. Magnitude order
ensures the final borrow is zero. Equal signs add magnitudes; opposite signs
subtract the smaller from the larger and retain the larger magnitude's sign.
Equal opposing magnitudes give zero. Ordinary subtraction reverses the second
sign algebraically and applies the same reduction, without increasing either
input magnitude. Nonzero-source paths use stored signs and digits. A zero
operand returns the source; a zero source with nonzero operand retains the
public operand.negate delegate, whose override behavior is constrained separately
by the subtraction dispatch fixtures and compiled Java null-result tests.

In multiplication, one inner step computes a_i*b_j+out_(i+j)+carry.
Assuming the output digit and carry are in 0..B-1, this is at most
(B-1)^2+2*(B-1)=B^2-1=2^60-1, below signed Long's maximum. The low 30
bits and quotient by B again produce a digit and carry in 0..B-1, completing
the induction. Row i adds a_i*B^i times the second magnitude to the prior
partial product. Its terminal carry goes into position i+len(b), which no
earlier row has reached; assigning that position cannot discard prior data.
After all rows the array therefore represents the full unsigned product.
The operand-sign product restores the sign; normalization removes high zero
digits and canonicalizes zero.

For nonzero operands of bit lengths u,v, the product lies in
[2^(u+v-2),2^(u+v)). Thus u+v-1>Int.MAX_VALUE proves an out-of-range result
and justifies the early guard. The bit-length sum uses Long. At the ambiguous
upper boundary, normalization must still check the actual highest digit.
This argument establishes digit arithmetic and the early bound; it does not
prove every allocation failure or range-error diagnostic.

## Integer division

The implementation details below record the pre-530 binary kernel. The current
normalized radix-B kernel and its output selection are proved in
[division530-proof.md](docs/division530-proof.md) and
[division530-selection-proof.md](docs/division530-selection-proof.md).
The signed identities and retained corpus still supply behavioral evidence;
the historical binary-loop argument does not prove the current kernel.

For ordinary operands, division first checks whether abs(x)<abs(y) using
normalized stored magnitudes with radix B=2^30. A nonzero magnitude with k
limbs lies in [B^(k-1), B^k), so limb count decides unequal-length order.
For equal lengths, the highest unequal limb decides order: all lower limb
differences together have absolute value less than the weight B^i of that
limb. Comparing from highest to lowest therefore gives exact magnitude order.
When abs(x)<abs(y), truncation toward zero gives quotient zero and remainder x.

This comparison is an arithmetic step, independent of a subclass's public
compareTo implementation. Reading stored limbs prevents replacement ordering
or a comparison exception from altering it. Portable JDK fixtures exercise
positive/negative/zero dividends, both divisor signs, single-/multi-limb cases
and formatting in every radix while source compareTo throws if called.
An additional division dispatch corpus checks stored magnitudes with inherited
division on subclassed source and operand objects. Public divide and remainder
enter the same private implementation as inherited divideAndRemainder. It reads
stored limbs and source bits directly; the multi-limb divisor view is an exact
base-class object sharing immutable limbs. Subsequent reduction operations act
on base-class working values, preserving the ordinary magnitude argument even
when operand overrides return replacements or throw. Nonzero remainders are
normalized into separate base-class values, including the smaller-dividend path
and a final working value equal to the ONE constant; zero remains canonical ZERO.
The 3,216 portable cases additionally constrain quotient parity and small final
remainders across the one-/multi-limb boundary. This does not settle unrelated
formatting callbacks, null diagnostics or practical allocation limits.

For a one-digit positive divisor d, the high-to-low loop maintains 0<=r<d.
Appending the next radix-B digit x forms t=r*B+x<d*B<=B^2. Hence t fits
Long, floor(t/d)<B is a valid quotient digit, and t mod d restores the
remainder invariant. Appending all digits yields A=Q*d+R with 0<=R<d.

For a multi-digit divisor D, binary long division processes the dividend's
bits from highest to lowest. After a prefix P, maintain P=Q*D+R with
0<=R<D. Appending bit v gives 2*P+v=(2*Q)*D+(2*R+v). Since 2*R+v<2*D,
subtracting D at most once and choosing the next quotient bit restores the
invariant. The fixed output bit positions encode those successive quotient
bits, so at termination A=Q*D+R and 0<=R<D.

The stronger prefix bound also addresses supported-range intermediates.
R<=P implies 2*R<=2*P<=2*P+v, and every processed prefix is at most the
original dividend magnitude A. Both the shift and the optional addition of
one therefore stay <=A, even when 2*D itself would be out of range.
Subtracting D cannot increase the nonnegative magnitude. The quotient is
also <=A, and the output array has the dividend's digit capacity. Thus this
reduction does not require an out-of-range numerical intermediate for
supported ordinary inputs; actual resource exhaustion remains a separate issue.

Finally, for signs sx,sy, choose q=sx*sy*Q and r=sx*R. Then q*y+r=x,
abs(r)<abs(y), and a nonzero remainder has the dividend's sign, giving
truncation toward zero. Zero is canonicalized rather than assigned a sign.

`IntegerArithmeticOracleTest` checks 396 independent signed JDK operand pairs:
30-bit digit neighbors, dense squares, maximal one-digit and two-digit divisors,
alternating digit blocks, seeded wide operands and zero dividends. Inputs reach
16,384 bits and products reach 32,768 bits. Expected sums, differences, products,
quotients and remainders use signed byte encodings, independent of decimal
formatting. Every pair also checks q*y+r, the remainder bound and its sign.
These cases corroborate the arguments on each target; they do not exhaust the
operand domain or establish all subclass behavior, resource limits or diagnostics.

## Integer square roots

For N>0 with b stored magnitude bits, let r=floor(sqrt(N)) and start Newton's
iteration at x0=2^ceil(b/2). Since N<2^b<=x0^2, x0>r. The update is
y=floor((x+floor(N/x))/2). For x>=r, x^2+r^2>=2*x*r and N>=r^2 imply
floor(N/x)>=2*r-x, so y>=r. If x>r, then N<(r+1)^2<=x^2 and therefore
floor(N/x)<=x-1, giving y<x. Thus the positive iterate strictly decreases until
x=r. At x=r, floor(N/x)>=r gives y>=x, which is precisely the stopping condition.
This establishes termination and the floor-root inequalities without floating
approximations or operand subclass metadata.

The initial shift uses Long to compute ceil(b/2), including b=Int.MAX_VALUE.
All iterates stay in [r,x0]. Since N<=r^2+2*r, floor(N/x)<=r+2<=x0+1 and
x+floor(N/x)<=2*x0+1. This sum has at most ceil(b/2)+2 bits, which is <=b
for b>=4; smaller inputs require at most four bits and are far below the supported
limit. Division intermediates are bounded separately by the private division
argument. For pairs, r^2<=N ensures the square is supported, and the nonnegative
stored-magnitude subtraction gives R=N-r^2<2*r+1. Zero is handled before division.

Both inherited APIs use this private routine on base-class working objects.
Source compare/bit/arithmetic overrides and the other public root API cannot
alter it. The 978 pinned outcomes cover exact-square neighbors through 8,193-bit
inputs and callback/result identities, plus independent inequalities and pair
reconstruction. Java tests add 180 nullable/throwing callback cases. This does
not establish practical near-limit allocation or every decimal-root contract.

## Greatest common divisors

For two nonzero stored operands, start with positive magnitude views A=abs(x)
and B=abs(y). Euclidean division gives A=q*B+R with 0<=R<B. A number divides
both A and B exactly when it divides both B and R, so replacing (A,B) by (B,R)
preserves the common divisors. Every nonzero second component strictly decreases;
the loop therefore terminates with (G,0), where G>0 is the greatest common divisor.
The private division argument bounds each remainder and its intermediates by
the current dividend, so no new numerical magnitude exceeds the initial inputs.

Working values are exact base-class objects. Subclass callbacks cannot alter
this reduction. The final positive result is normalized into a separate base
object. For a zero operand, the observed public abs callback is forwarded
instead, including its identity and any replacement/null result; the ordinary
mathematical argument assumes truthful abs behavior.

`IntegerGcdOracleTest` compares 1,602 pinned outcomes on every target, including
sign/zero combinations, self aliases, replacement and throwing callbacks, shared
wide factors and consecutive Fibonacci inputs. Java tests add 168 nullable
callback/argument cases. These domains do not establish all returned subclass
graphs, exact null diagnostics or practical allocation limits.

## Modular exponentiation

For positive `m`, ordinary positive powering keeps a residue `r` and a base
`b`, initially `r=1 mod m` and `b=a mod m`. At bit `i`, `b` is congruent to
`a^(2^i)`. Multiplying `r` by `b` when the exponent bit is set incorporates
exactly that exponent contribution; squaring `b` advances the invariant.
There are finitely many stored exponent bits. The odd-modulus implementation
uses this independent binary algorithm; public equality selects the observed
exponent-one identity shortcut before the stored-bit loop.

For even `m`, write `m=q*B`, with odd `q` and `B=2^p`. These factors are coprime.
Compute residues `u=a^e mod q` and `v=a^e mod B`. Let `s=B^(-1) mod q` and
`t=q^(-1) mod B`. Then `u*B*s+v*q*t` is congruent to `u` modulo `q` and to `v`
modulo `B`. Reducing the sum modulo `m` gives the unique required CRT residue.
When `q=1`, the odd residue and its coefficient are zero and this reduces to
the binary residue.

Low-magnitude masking computes reduction modulo `B` for ordinary nonnegative
bases. For odd bases, reducing the exponent modulo `2^(p-1)` is sufficient:
odd `x` obeys `x^(2^(p-1))=1 mod 2^p`. For `p=1` this is immediate. Squaring
`1+c*2^p` proves the induction step from `p` to `p+1`. Thus retaining only
`p-1` low exponent bits is valid. Even bases use all exponent bits. Virtual
metadata and bit callbacks intentionally affect this path for subclasses;
the ordinary proof assumes truthful results.

With modulus bit length `b`, the direct CRT products and sum need at most
`2*b+1` bits. The direct branch conservatively requires `2*b+2<=Int.MAX_VALUE`.
Otherwise `u*B<m` and `v*q<m` are formed first, and further products are
computed as bounded residues. To add residues `L,R` without creating `2*m`,
set `gap=m-R`: return `L-gap` when `L>=gap`, otherwise `L+R`. In each case the
result lies in `[0,m)`. Bounded multiplication repeats that operation with
doubling and selected multiplier bits, so it never materializes the full product.
The binary-modulus path similarly switches to bounded multiplication if its
full product would exceed the magnitude limit; `B` itself fits because `p+1`
does not exceed the supported modulus bit length.

For negative exponents the positive power is inverted after recombination.
Coprimality of that residue with `m` is equivalent to coprimality of `a` with
`m`, so failure and successful inverse congruences follow from the inverse
argument below. Canonical constants, source identities and callback order are
separate black-box observations. False and changing metadata compositions and
full-range resource feasibility still require audit.

## Modular inverses

For truthful ordinary operands, first reduce `a` to `0<=a<m`, with `m>0`.
The modulus-one shortcut returns zero. For all other moduli, zero has no inverse;
one has inverse one. The public
[modInverse contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#modInverse(java.math.BigInteger))
requires coprimality and a nonnegative residue below the modulus.

The independent extended Euclidean loop starts with remainders `(m,a)` and
coefficients `(0,1)`. Each division `r=q*nextR+remainder` replaces the pair with
`(nextR,remainder)` and the coefficient pair with `(nextT,t-q*nextT)`.
By induction each remainder is congruent to `a` times its coefficient modulo
`m`. The remainder is nonnegative and strictly smaller than the divisor, so
the loop terminates at `g=gcd(m,a)`. If `g!=1`, no inverse exists. Otherwise
the last coefficient satisfies `a*t=1 (mod m)`; reducing it gives the required
unique residue in `[0,m)`.

For the reduced ordinary input, successive nonzero coefficients alternate in
sign and their absolute values grow by `abs(tNext)=abs(tPrev)+q*abs(tCurrent)`.
Each swap/subtract step has determinant minus one on the two Bezout coefficient
rows, so the terminal zero row remains primitive. Its equation
`m*s+a*t=0` then forces `abs(t)=m/g`. Thus the terminal zero-remainder coefficient
has magnitude `m/g`, and every
coefficient and every product `q*nextT` has magnitude at most `m`, and the signed
subtraction remains within that bound. This proves the coefficient arithmetic
does not require a magnitude larger than the supported modulus in this path.
The argument still assumes exact supported-range division and feasible arrays;
it does not prove resource behavior at multi-gigabyte inputs.

Subclass equality and reduction callbacks intentionally precede the private
working loop. Replacement negative reductions supply their magnitude to that
loop, matching the black-box JDK outcomes; fake equality can select a constant
shortcut. Those observations are checked separately from ordinary mathematical
invariants. The proof above assumes truthful callbacks and does not cover every
composition of replacement objects.

## Integer powers

For truthful operand metadata, write abs(x)=u*2^t with u odd. Then
x^e=sign(x)^e*u^e*2^(t*e). After zero/unit/identity cases, the implementation
obtains u and t through the observed public callbacks. Binary exponentiation
maintains answer*base^remaining=u^e: consuming an odd low bit multiplies answer
by base, and halving the exponent squares base. Base-class working objects
prevent unrelated source multiply overrides from changing this reduction.

If u has b bits, u<2^b implies u^e<2^(b*e). Thus b*e<=64 permits exact unsigned
64-bit arithmetic; if b*e+t*e<=64, the final shift also fits. Otherwise the exact
unsigned result is packed into the library's 30-bit limbs before shifting. The
unit factor is handled separately. For odd u>1, u>2^(b-1), so (b-1)*e>=Int.MAX_VALUE
already implies an unsupported output. The final shift checks the exact stored
bit length. Actual supported-range allocation and intermediate failures still
require a broader audit.

The 6,677 pinned JDK outcomes also cover false subclass metadata. Those paths
deliberately preserve observed compact-word selection, wrapping Long operations,
masked shift distances, result identity and exceptions; the numerical argument
above assumes truthful callbacks. The leading-word-two compact path has period
64 in the exponent, corroborated against other even leading words at exponents
31/32/33/63/64/65/127 and Int.MAX_VALUE. Individual callback modes do not exhaust
all combinations or returned subclass graphs.

## Byte-slice validation

Let N be the byte-array size. A slice is valid exactly when off>=0, len>=0
and off+len<=N over mathematical integers. The implementation rejects negative
off or len first, then compares off with N-len. Since 0<=N,len<=Int.MAX_VALUE,
N-len is representable as Int. Thus the guard is equivalent to the mathematical
condition without overflowing. For an accepted slice, off+len<=N also proves
that computing its end index is safe. Diagnostics print the original operands
separately rather than adding them.

`IntegerByteOracleTest` compares 25,584 constructor outcomes with the pinned JDK,
including both Int endpoints, empty inputs, invalid signum precedence and selected
sign-extension/limb boundaries. Successful cases mutate the input and exported
arrays and verify stable value and hash. This finite domain does not establish
the maximum practical allocation size or all byte-packing behavior by itself.

## Infinite two's-complement bit operations

For an ordinary value with Java bit length `b`, choose `w=32*(floor(b/32)+1)`.
Then `w>b`, so both positive values and negative values fit a signed `w`-bit
representation with a sign bit. Reading the stored base-2^30 limbs with complement
and carry for negatives gives exactly the residue modulo `2^w`. Binary operations
use the larger operand width. Every bit above that width is a constant sign bit;
applying the Boolean operation to those bits gives the sign bit of the finite
result. Interpreting the finite result as signed therefore agrees with infinite
sign extension. The same reasoning applies to complement.

Updates additionally select a width beyond the addressed bit with space for a
sign bit: set/flip use at least `32*(floor(n/32)+2)` bits; clear uses at least
`32*(floor((n+1)/32)+1)`. Masking the partial highest base-2^30 limb to the selected
width and extending its sign into that limb preserves this signed interpretation.
Width arithmetic uses Long, including `n+1`, before conversion to a limb count.
This bounds the arithmetic used for indexing; it does not establish that every
permitted allocation succeeds or resolve all supported-magnitude limits.

At address `n=Int.MAX_VALUE`, any supported positive magnitude has a zero bit
and any negative value has a sign-extension one bit. Clearing the former or
setting the latter leaves the value unchanged. The other updates create a
magnitude at least `2^n`, whose bit length exceeds the supported `n` bits.
Thus the endpoint can return a fresh unchanged value or reject the result before
allocating a sign-extension array, while preserving the virtual width callback.

For negative `-m`, let `t` be the number of trailing zero bits of positive `m`.
Writing `m=u*2^t` with odd `u` gives
`m-1=(u-1)*2^t+(2^t-1)`, hence
`popcount(m-1)=popcount(m)-1+t`. These are exactly the bits differing from the
negative sign extension. The implementation counts magnitude bits directly and
adds the virtual lowest-set-bit result minus one. For ordinary values this proves
the count; replacement callback values intentionally retain JDK Int arithmetic.

Virtual bitLength also intentionally controls finite width for subclass instances.
That behavior and fresh result identities are black-box compatibility observations,
checked by the separate 17,198-case oracle, rather than assumptions about sensible
metadata overrides. All ordinary width arguments above assume truthful metadata.

## Hash packing

The observed JDK hash folds unsigned 32-bit magnitude chunks, most significant
first, with multiplier 31, then applies the sign. Our magnitude uses 30-bit
limbs. For chunk index `i`, its starting bit is `32*i`, its source limb index
is `floor(32*i/30)` and its offset is `32*i mod 30`. This offset is always even
and at most 28. Two consecutive limbs therefore contain at least
`30-offset+30>=32` needed bits. Shift the first limb right by the offset, shift
the next limb left by `30-offset`, join them and mask to 32 bits. A missing next
limb supplies zero.

The intermediate is at most 60 bits, so it fits signed Long. The chunk index
calculation uses Long even at the supported magnitude limit. Each fold's Long
arithmetic fits before conversion to Int; conversion and final sign application
implement arithmetic modulo 2^32. This is independent of our limb layout and
requires constant auxiliary storage without abs/testBit calls or intermediate
BigInteger objects. Both signs, overflow of the hash accumulator and every alignment are
compared with public JDK outputs. This is an argument for packing equivalence;
the JDK's particular hash recurrence is constrained by the oracle, rather than
specified by the Object.hashCode contract.

## Exact decimal addition and subtraction

For stored operands x=c*10^-s and y=d*10^-t, choose u=max(s,t).
The integers C=c*10^(u-s) and D=d*10^(u-t) then give
x+y=(C+D)*10^-u and x-y=(C-D)*10^-u. Both scale distances are
nonnegative, and Long subtraction represents their full Int-scale domain.
A zero coefficient remains zero for every distance, so its scaling can be
skipped without constructing the corresponding power of ten. Stored coefficient
signs determine this optimization; an overridden public signum is not numerical
input to exact alignment.

For a nonzero coefficient and positive distance k, the scaled magnitude is at
least 10^k, hence greater than 2^k. If k exceeds Int.MAX_VALUE, the scaled integer
necessarily exceeds the supported Int-sized bit-length domain. Validating the
positive distance before power construction is therefore sound for nonzero
coefficients. At target scale u, the other coefficient is an unchanged stored
input with magnitude below 2^Int.MAX_VALUE. The oversized aligned term exceeds
2^(Int.MAX_VALUE+1), so cancellation with the other input cannot bring the
result back into range. A zero coefficient still requires no such power. The pinned JDK
error label and validation order are established by separate oracle cases.

This argument assumes supported intermediate sizes and correct integer
arithmetic. It does not establish all allocation/range behavior, finite-context
sticky-tail rounding or virtual callback semantics. Separate public-JDK fixtures
constrain which overrides exact and context operations invoke.

## Decimal comparison

For ordinary inputs with different nonzero signs, sign order determines numeric
order. Equal nonzero signs reduce the problem to absolute magnitudes, reversing
the result for negative numbers. At equal scales, comparing stored coefficients
(or magnitudes after sign separation) preserves order because the decimal factor
is positive.

For nonzero magnitude c*10^-s with d coefficient digits, its adjusted exponent
is d-s-1. Different adjusted exponents determine magnitude order by disjoint
decimal decades. Equal exponents reduce comparison to decimal significands.
The current implementation uses the exact digit-count and scratch-quotient
argument in [DECIMAL_METADATA_COMPARISON.md](DECIMAL_METADATA_COMPARISON.md),
rather than the historical padded-string implementation.

Let L have k more decimal digits than S. Write L=q*10^k+r, where
0<=r<10^k. Comparing L with S*10^k is equivalent to comparing q with S;
on quotient equality, a nonzero remainder makes L greater. In-place divisions
by powers of ten of at most 10^9 produce exactly q. The OR of their nonzero
remainders is exactly the original remainder's nonzero predicate, even when a
later division has zero remainder. The scratch comparator uses trimmed active
length, so retained array capacity cannot change the order. Reversing the
answer when L is the right operand restores original orientation. Equal digit
counts need only stored magnitude comparison; zero is handled separately.

The accepted logarithm bound places the exact digit count in {E,E+1}; comparison
with 10^E selects the correct one, except at the maximal E where the unsupported
threshold is known to exceed every supported input and is not constructed.
Every partial quotient is bounded by L, and one scratch array has L's limb count.
No aligned product or full coefficient string is needed. Scale arithmetic uses
Long; for public Int scales and truthful precisions it cannot overflow. These
arguments establish numerical order over the supported representation domain,
conditional on the referenced integer-kernel and allocation assumptions. They
do not guarantee maximum-size allocation or execution time (548). Virtual getter
dispatch and replacement metadata remain separately classified observations;
explicit override and super calls retain their language meaning.

## Decimal rational conversion

For an ordinary BigDecimal, zero returns positive zero, consistent with the
absence of a signed decimal zero. Decimal adjusted exponents outside [-400, 400]
are respectively above both formats' overflow thresholds or below both
half-minimum-subnormal thresholds. Otherwise the current implementation first
extracts a bounded coefficient prefix and exact sticky information. The
[task562 threshold/cell argument](DECIMAL_BINARY_CONVERSION.md) proves that its
exact endpoint or interior surrogate lies on the same side of every IEEE
rounding midpoint as the original magnitude. This includes arbitrarily long
cancelling coefficient/scale cohorts; it does not form a full coefficient string
or an unsupported power of ten from the original scale.

Write the selected positive surrogate as `n/d`. A negative surrogate scale
multiplies its coefficient by a power of ten; a positive scale supplies that
power in the denominator. The prefix division uses current task530
`divideStored` with both outputs, preserving the quotient and remainder needed
for sticky information. Its current kernel/selection proof and source binding
are composed in [the task543 memo](docs/floating-conversion-543-proof-binding.md).

The bit-length difference `b=bitLength(n)-bitLength(d)` places the binary
exponent in `{b-1,b}`. One exact shifted comparison selects `e=floor(log2(n/d))`.
If `e` exceeds the format's greatest finite exponent, the result is infinity.
Otherwise the rounding quantum is `2^k`, with
`k=max(e, minimumNormalExponent)-(p-1)`. Scaling the numerator or denominator
by a power of two gives an exact integer division. HALF_EVEN division rounds
`(n/d)/2^k` to an integer `q`, including ties, without decimal approximation.

For normal values, `q` has at most p significant bits, apart from a carry to
the exact power `2^p`. For subnormal values the quantum stays fixed at `2^-149`
for Float or `2^-1074` for Double. Consequently `q*2^k` is representable in the
target format, including a carry into the first normal value. At underflow,
q=0 preserves the original sign through multiplication; at overflow the rounded
carry selects infinity. q fits Long and converts exactly to Double. The scaling
power itself is exactly representable in Double. For Float, the product is
already an exact Float value before the final cast, so no double rounding occurs.

Compact coefficients in the open interval (-2^63, 2^63) bypass virtual signum
at scale zero, or at scales within +/-10 for Float and +/-22 for Double when
casting the primitive coefficient back to Long reproduces the stored Long.
The comparison deliberately includes primitive-to-Long saturation at Long.MAX_VALUE,
as constrained by the public-JDK subclass oracle. Stored zero always returns
positive zero. Other paths call virtual signum exactly once and multiply the
rational magnitude by its return value; no virtual precision call participates.
These dispatch rules have 594 portable result-and-trace outcomes.

Constructor coefficient subclasses are normalized through their virtual byte
encoding into an ordinary BigInteger; ordinary coefficient identity is retained.
The task562 prefix proof removes the former coefficient-string ceiling and
bounds the final rational work below 8192 bits. Prefix extraction still uses a
possibly large supported divisor and current task530 division. This argument
assumes exact integer arithmetic and successful required allocations; it does
not establish practical maximum-domain execution or discharge task548.
The portable tests independently check every binary scaling power from
2^-1074 through 2^1023 by IEEE bit encoding, and compare signed exact midpoint
neighbors with the pinned JDK. They exercise both retained parities, significand
carry, underflow to signed zero, the subnormal/normal transition and overflow.

## Decimal square-root rounding

For positive ordinary input `x=c*10^-s`, let `E=floor(log10(x))` and
`R=floor(E/2)`. Its square root has adjusted exponent R. At positive precision p,
the result quantum is `10^-t`, where `t=p-1-R`. Scaling the radicand by
`10^(2*t)` represents it as an exact nonnegative rational `n/d`. Computing
`r=floor(sqrt(floor(n/d)))` gives `floor(sqrt(n/d))`: the integer square r^2
cannot lie strictly between floor(n/d) and n/d. Thus r brackets the scaled root
between r and r+1 without approximating the irrational value.

The equality `r*r*d==n` detects an exact result. Otherwise compare `4*n` with
`(2*r+1)^2*d`. Squaring is monotone for these nonnegative values, so this is
exactly the comparison with the midpoint r+1/2. The rounding policy chooses
between r and r+1; HALF_EVEN uses the parity of r at equality. A carry into
10^p is an exact decimal power and is adjusted to p digits by the final round.
Removing trailing zeros toward the preferred scale changes representation,
not value. Exact/unnecessary modes first remove decimal trailing zeros and
require an even normalized scale and an integer-square coefficient, followed by
the requested precision check.

For a positive normalized coefficient c, at least one of the prime-factor
exponents v2(c) and v5(c) is zero, since c is not divisible by ten. A rational
square c*10^-s requires both v2(c)-s and v5(c)-s to be even. An odd s would
therefore force both v2(c) and v5(c) to be odd and positive, contradicting
normalization. For even s, the decimal factor is already a rational square;
the remaining condition is that c itself is an integer square. Zero is handled
separately. This establishes the numerical guard for ordinary normalized inputs;
returned subclasses and their virtual getters are constrained by separate JDK
oracle cases rather than by this argument.

The public [Java SE 25 sqrt contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#sqrt(java.math.MathContext))
specifies preferred scale s/2, error bounds and exact-mode failures. The pinned
oracle additionally constrains concrete representations and errors. The 2,592
new cases square decimal midpoints exactly, perturb the radicand on both sides,
and vary parity, carry, precision through 129 digits, decimal exponent and input
trailing zeros across all eight rounding modes. This argument assumes valid
scale/intermediate sizes and ordinary methods; it does not establish the whole
scale-overflow or subclass-dispatch behavior of sqrt.

The later [inexact-normalization construction](docs/sqrt-normalization-579-implementation.md)
removes the unsupported doubled working radicand for ordinary supported results.
It streams decimal pairs into private paged root/remainder/trial buffers, rounds
and normalizes before final public range checks. Its cheap precision gate proves
the old path safe where retained; it imposes no smaller precision cap. The
[separate verification](docs/sqrt-normalization-579-verification.md) records1501
actual local method executions across JVM/JS/Wasm/macOS ARM64. The giant symbolic
witness was not executed; matching-head CI, broader subtype and resource gates
remain separate.

The subsequent [contextual multiplication construction](docs/decimal-context-product-580-implementation.md)
uses private base10^9 product cells when an exact public binary coefficient
cannot conservatively be proved supported. It discards only the digits required
by the requested precision, applies signed guard/sticky rounding and one exact
precision-carry drop, then checks the required final coefficient and scale.
It does not optionally strip zeros to substitute a different cohort. The
existing HIGH-scale rejection when no precision reduction is required remains.
The [actual local report](docs/decimal-context-product-580-verification.md)
separates bounded raw-helper tests, public materialized-path controls and the
symbolic maximum-size argument. No maximum-size allocation or execution is
claimed.

## Decimal powers and working precision

The public [pow contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html#pow(int,java.math.MathContext))
describes a single accumulator, repeated squaring, expanded precision based on
the decimal digit count of the exponent, an optional reciprocal and final
rounding. Scanning exponent bits from most to least significant, the accumulator
represents the power for the scanned prefix. Squaring doubles that prefix;
multiplication by the base adds the next set bit. Every step uses the expanded
context, rather than computing an exact power and rounding only once.

A seeded JVM boundary matrix compares 13,024 outcomes for positive and negative
exponents, adjacent powers of two, all modes and precision through 65 digits.
The pinned runtime also exposes Int overflow of the expanded precision as
MathContext's IllegalArgumentException (Digits < 0), not a decimal-scale error.
There are 128 portable cases at this boundary; JVM tests additionally compare
the error message. Base one avoids enormous allocations in the successful
near-Int.MAX_VALUE precision cases. These observations do not prove every
intermediate-size limit, subclass call sequence or every exponent/input pair.

Those pinned-runtime working-precision failures are historical observations;
the own [Long working-precision repair](docs/decimal-pow-working-precision.md)
has its separate conditional cap argument. The
[current composition audit](docs/decimal-power-root-542-composition-after-580.md)
incorporates563/565/579 roots and580 products and identifies a remaining
context-division normalization defect tracked as581: at precision646456993,
rounded1/3 has a supported required coefficient and scale although its internal
normalization power does not fit. This witness is symbolic and independently
source-verified. The [accuracy-scope argument](docs/decimal-pow-542-accuracy-scope.md)
separately gives a conservative relative-error enclosure under its completing,
ordinary-leaf assumptions. It does not claim a sharp universal two-ulp theorem
or turn the API's qualified statement into a stronger acceptance gate. The581
repair and qualified negative-unit scale question remain separate; no finite
test count supplies an all-input accuracy argument.

## Decimal exact primitive conversion

For an ordinary nonzero decimal with coefficient precision d and scale s, let
`k=d-s`, computed in Long. If k<=0, its absolute value is strictly between zero
and one, so exact conversion requires a fractional-part error. If k>19, its
absolute value is at least 10^19, exceeding signed Long range. Otherwise exact
rescaling to zero either rejects a nonzero fractional part or supplies an integer.
An integer of fewer than 19 decimal digits fits Long. At 19 digits the signed
bitLength rule above, bitLength<=63, distinguishes both signed endpoints from
overflow. Zero and compact scale-zero values have separate observed fast paths.

Narrow conversions first call virtual longValueExact once, then compare the
returned Long with their signed width endpoints. This preserves both fractional
versus overflow precedence and replacement-method behavior. The 560 ordinary
cases and 27 replacement-Long cases compare values and exact messages. Another
72 traces constrain public signum/precision calls, legacy-Int setScale dispatch
and canonical formatting. The long guards deliberately make separate virtual
precision calls, as observed in the pinned JDK.

Canonical text derives digit count from stored coefficient text. Its private
plain layout avoids virtual toPlainString. The reference bypasses virtual signum
at scale zero and for nonnegative compact coefficients below Int.MAX_VALUE at
scale two; other tested layouts call it once. There are 264 replacement-getter
text-and-trace cases around these boundaries. These observations concern fresh
single calls and do not prove repeated-call caching or every formatter/subclass.

## Signed integral-division metadata

For the guard-digit term, let `n = 10 * divisor.precision()` in Long arithmetic.
Truncating division writes `n = 3q + r`, with `-2 <= r <= 2`. Therefore
`ceil(n / 3) = q + 1` when `r > 0`, and `q` otherwise. The former `(n + 2) / 3`
expression only supplies the ceiling for nonnegative `n`; a negative reported
precision can alter both the context precision and whether its constructor fails.

Both reported precisions and scales are Int values. The guard term lies between
-7,158,278,826 and 7,158,278,824, and the absolute scale difference is at most
4,294,967,295. Including source precision and two extra digits, the budget lies
between -9,305,762,472 and 13,600,729,768, so its Long arithmetic cannot overflow.
The observed upper cap and subsequent Int narrowing are separate compatibility
steps. The 1,176-case public-JDK matrix checks their observable dispatch and
failures; this arithmetic argument establishes the signed ceiling and Long bounds,
not every subclass composition or the complete integral-division contract.

## Context-division replacement metadata

Let `a` and `b` be the stored signed coefficients, `s` the stored scale difference,
`x` and `y` the reported operand precisions, and `p` the context precision.
Comparing `abs(a) * 10^-x` with `abs(b) * 10^-y` does not require allocating those
powers: compare decimal digit counts minus the reported precision, then compare
the stored digit strings padded on the right when the exponents are equal.
The intermediate quotient scale is `s + p + y - x - adjustment`, where
`adjustment` is one when that normalized magnitude comparison is positive.
This scale uses the reported precisions, even when a subclass supplies values
inconsistent with its stored coefficient. Dividing the stored coefficients at
this scale and rounding to the context preserves the observed replacement-getter
behavior. Int scale/precision inputs keep this expression within Long range;
the separate scale and power range checks remain necessary.

For ordinary coefficients, exact results strip decimal zeros only toward the
preferred scale. An exact zero can reduce its scale without changing value.
The pinned runtime also exposes a compact equality shortcut for subclasses:
when source precision is at most divisor precision and both operand precisions
and context precision are below 18, equal normalized compact coefficients are
treated as a unit ratio. The oracle includes stored-zero inputs reporting nonzero
sign to distinguish this shortcut from ordinary division. This is black-box
compatibility evidence, not a mathematical definition of zero divided by zero.

The compact path also exposes a normalization-overflow result for inconsistent
metadata. If multiplying the compact coefficient by `10^(y-x)` exceeds Long
range, the observed result is zero at the preferred scale. The guard uses the
absolute compact value; Long.MIN_VALUE has already been excluded from compact
storage. Before multiplying by ten, compare with `Long.MAX_VALUE / 10`. If the
raise exceeds 18, every nonzero magnitude must overflow, since `10^19` exceeds
Long.MAX_VALUE. Zero never overflows. Thus at most 18 checked multiplications
decide the result for every nonnegative raise, without allocating a power of ten.
For ordinary compact values with accurate precision, normalization produces
fewer than 18 digits in this path, so the guard cannot change their quotient.

The expanded 75,728-case oracle checks these observations, including changing
getter answers, all rounding modes, the 17/18/19 precision boundaries and values
on both sides of compact multiplication thresholds. A separate 50,688-case live
JVM comparison reports zero differences. This does not establish every replacement
Int value, scale-overflow ordering or subclass composition.

## Evidence and remaining scope

The main portable oracle includes 1,728 signed midpoint-neighbor Float/Double
outcomes. It varies retained parity, carry, exponents across the 30-bit limb,
32/64-bit conversion boundaries, and the finite/infinity edges. Another 64
cases replace virtual longValue, including Long values where an intermediate
Double would change Float rounding. The exact-conversion matrix has 1,248
cases covering signed endpoints, both sides of the magnitude guards, replaced
bitLength, exact error messages and primitive-call traces. Separate power tests
check every scaling factor and positive/negative integer powers through 2^1100
against IEEE encodings, including the finite/infinity transition.
Hash fixtures add 5,760 signed power/neighbor results through two full 480-bit
alignment periods and 350 dense-byte results through 65,536 bytes. Structured
common inputs are encoded independently as byte arrays; all common hash inputs
use subclasses whose abs/testBit throw if invoked.

The decimal midpoint campaign adds 1,296 Float/Double results constructed from
exact dyadic rational midpoints and decimal epsilon offsets.

These arguments complement the fixtures; they do not prove every compiler or
runtime implementation, complete the decimal subclass audit, exercise multi-gigabyte
objects, or settle the full java.math compatibility audit. Those requirements
remain tracked in `COMPATIBILITY.md`.

# Inexact decimal sqrt normalization: source candidate 579

**SOURCE ONLY / UNCOMPILED / UNRUN / NOT INTEGRATED.** The implementation is in
the isolated no-Git archive `/private/tmp/krogu-math-task-579-PlUizX/root`, derived
from exact b419. The initial implementation owns BigDecimal's inexact normalization seam, the new internal
`DecimalSquareRoot579.kt`, the new three-method common test and this document
only. The later cheap-gate correction changes BigDecimal and the two579
implementation/seam documents; the seam JVM test stays frozen. No publication-root file, BigInteger implementation, existing
test/fixture/proof, public API, decimal instance field or cache was changed.
No compiler, script/helper, numerical computation, certificate, probe, test,
build, benchmark, Git or tracker operation ran. The assertions and arithmetic
below were inspected as source, not executed. No foreign numerical implementation
source/body/bytecode was read. Root owns independent review and any execution.

The accepted [proposal](sqrt-normalization-579-proposal.md) remains byte-for-byte
`4db4e916c2ffbf7a795a4c1bd9411efcda91e94496b5e15eb1271d440db45bfe`.
Original BigDecimal was
`08b4329ea5e902f1043107d42ddb02592be537497f6b0990927e8c67c4ed3f29`;
the isolated, unchanged BigInteger is historical
`5a4aa675afc9cdfdff6c8761dba45c9636de5533e70fabbd6ea808dc6fa08781`.
The latter is not a claim about the publication root's parallel format559 source.
Before editing, original BigDecimal and proposal were copied into
`/private/tmp/krogu-579-source-originals-PlUizX/`, hashes verified, and both copies
made read-only. Final source/test/document hashes are in the parent handoff.
The later one-line cheap-gate revision binds BigDecimal
`4ab8f15b10f2aa2093e4e04e2a7e4aa6226a7230be24dead3bbe75bcde9e5cc5`.
Before that revision, BigDecimal01d814, this document882fa2 and the subtype seam
documentbca237 were preserved read-only in
`/private/tmp/krogu-579-cheap-gate-originals.DglxVbQc/`. The raw helper, tests,
proposal and BigInteger remain frozen; this is still source-only work.

## Entry and compatibility boundary

The original public sqrt prefix is retained: virtual decimal signum; negative
and zero cases; finite-exact-root563 detection/result; exact/unnecessary fallback;
and the stored-zero subtype route including its precision call. The existing
coefficient signum call immediately before adjustedExponent is captured once,
instead of repeating it. adjustedExponent still calls decimal precision once at
the same point. Negative reported stored coefficient signs and nonpositive
normalization shifts retain the old arithmetic branch. Finite563 and565 code
is unchanged.

The new route is considered only when the captured coefficient sign is positive,
shift h is positive, and p>Int.MAX_VALUE/8. Only then is D recovered by the
existing nonvirtual stored decimal-digit helper and the reported exponent
compared with D-S-1. A misleading decimal precision answer retains the original
checked-scale/materialized branch at every p; it is not fed to a p-digit raw
theorem. At p<=268435455 no second stored-digit helper or fallback helper-object
call occurs, avoiding their extra exact TEN.pow work and helper powers-array
initialization on this branch. At larger p the extra stored check can allocate;
no identical resource/exception precedence for arbitrary subclasses is claimed.

Write x=C*10^-S, C>0, p=context.precision>0, E=D-S-1, R=floor(E/2), t=p-1-R.
The captured ordinary exponent gives

`h=2t-S=2p-D-1+epsilon`, `epsilon=E-2R in {0,1}`.

All these quantities are Long. The cheap gate is justified without computing D
again: for truthful captured metadata, M=D+h=2p-1+epsilon<=2p. If
p<=floor(Int.MAX_VALUE/8)=268435455, then 4M<=8p<=2147483640<Int.MAX_VALUE and
C*10^h<10^M<2^(4M), so the old numerator is supported. Also
0<h<=2p-1<=536870909<646456992, and t=(S+h)/2 lies inside Int because S is Int
and h has that bound. Thus no ordinary input below this gate needs the fallback.
This is an eligibility optimization, not a p/domain cap: larger precisions still
reach the complete raw theorem. Misleading small-p metadata keeps the old path;
the ordinary bound is not asserted for it.

At larger p, the old materialization path is chosen only if
t fits Int, h is within the existing ten-power ceiling, and 4(D+h)<=Int.MAX_VALUE.
Since C<10^D and 10<16, this proves C*10^h has supported bit length. This bound
is sufficient, not necessary: failing it selects raw scratch, never a public
range exception or precision cap. For h<=0 the old path is unchanged; the
proposal proves -h<=D-1, denominator<=C, and t inside Int for ordinary inputs.

The raw route is a mathematical repair for the 542 witness C=2,S=0,p=400000000:
it never constructs 10^799999998, a radicand product, or a provisional public
root. That witness was not allocated or executed here. No feasibility or speed
claim follows for that size.

## Numeric input and restoring invariant

`decimalChunks` repeatedly uses the existing public BOTH-output division by
10^9. For an ordinary positive coefficient, each quotient is smaller than its
input and every remainder lies in [0,10^9). The source division's one-limb
divisor branch reads stored magnitude words without modifying them, constructs
a fresh quotient and a bounded remainder; the original coefficient is read-only.
The first receiver is C; subsequent receivers are returned quotients. No decimal
String or logical zero suffix is materialized. This implementation intentionally
uses the parent-authorized public leaf rather than modifying BigInteger.

This introduces a precise separate541 condition: a reachable own-format restored
coefficient subtype can override signum, divideAndRemainder, or the returned
remainder's numeric conversion. Truthful stored-value-consistent leaves satisfy
the arithmetic theorem. Replacement/throwing leaves, their internal callback
traces and diagnostic priority are not proved equivalent or waived here. The
public finite-root prefix itself already observes coefficient operations; no
argument based only on ordinary constructor normalization makes restored types
unreachable. BigDecimal precision overrides that report the actual D satisfy
the raw theorem; misleading answers retain the old branch. No new poison test,
normative noncall guarantee, whole541/526 acceptance or public dispatch exemption
is introduced. These exact seams need the parent's classified dispatch review
before integration acceptance.

Chunks are little-endian base B=10^9; the highest is nonzero. Top width is found
using nine literal powers. D=9*(chunkCount-1)+topWidth is computed in Long.
The digit reader accesses the top width, then all lower chunks with width nine,
then emits h zeros by position alone. A crossing coefficient/suffix pair is
handled by the same reader. T=D+h=2p-1+epsilon, so ceil(T/2)=p; the internal
precondition is checked before processing. A single initial digit is used when
T is odd, followed by pairs g in [0,99].

For each processed prefix A, root a and remainder r maintain

`A=a*a+r`, `a=floor(sqrt(A))`, `0<=r<2a+1`.

`remainder.multiplyAdd(100,g)` forms U=100r+g. A distinct trial buffer forms
T_d=(20a+d)d, scanning d from nine downward to the first T_d<=U. Zero always
succeeds. Subtracting that trial and replacing a by 10a+d gives the exact next
invariant: the next floor root lies between 10a and 10a+9, and maximal d chooses
it. The original strict upper square gives the remainder bound also for d=9.
No A, a*a, midpoint square or full radicand is stored. At completion a has
exactly p digits. `allNines` records the chosen root digits, not spare storage.

## Actual cell, index and primitive bounds

`Cells` allocates independent 8192-Int pages on demand. Its logical limit is
checked before every indexed read/write; append writes before increasing size.
All active sizes are Int, and every limit is far below Int.MAX_VALUE, so those
increments cannot wrap. Pages are never shared, shrunk or exported. Assignment
copies active cells, then updates active size; stale high cells are ignored.
Zero has size zero, including a trial multiplied by zero. Comparisons inspect
active length then high-to-low cells; subtraction reads only the other active
cells and finishes with zero borrow. All source/destination buffers are distinct.

From r<=2a, U<=200a+99 and T_d<=180a+81. The root has at most p digits, or p+1
after final increment; remainder and trials fit p+3 digits. Each of the three
root/remainder/trial buffers has the source capacity

`((p.toLong()+11)/9)+1 <= 238609296` cells.

Input chunks have capacity ceil(646456993/9)=71828555 cells. Binary conversion
has ceil(Int.MAX_VALUE/30)+1=71582790 cells. Every allocation is a fixed-size
page; at most8191 padding cells per buffer are allocated beyond its logical
used extent. Page indices are below29128 for the largest root buffer; page table
sizes are Int-indexable. These are bounds, not eager allocations.

Decimal multiplyAdd has multiplier in [0,100], initial carry at most99 (or a
final increment one), and later carry<100, giving part<100B<10^11. No primitive
product approaches Long.MAX_VALUE. Raw subtraction differences lie in [-B,B-1],
within Int. divideTen uses remainder<10, so part<10B and each quotient cell<B.
All low-digit divisibility checks are on a positive root. Decimal extraction
remainder.toInt is lossless under its leaf contract.

T<=2*Int.MAX_VALUE, digit positions, 9*chunkCount, p+11, capacities and scales
are Long before narrowing. The reader stops exactly at T; its casts cover only
actual coefficient positions, giving chunk indices in [0,chunkCount). The
descending digit scan stops at zero; no -1 trial is reached. The root capacity
formula is positive for all permitted p, including Int.MAX_VALUE. No doubled
precision or scale expression is evaluated in Int.

The raw stage owns input chunks plus root, remainder and trial: **four** paged
buffers. Final conversion adds a fifth paged binary buffer. None is assumed
collected before return. Peak retained workspace must therefore include all
five page collections and their page tables, output byte array, constructed
public binary magnitude, input C and live decimal-extraction/division leaves.
There is no per-pair allocation of a new trial buffer. Each public extraction
iteration can transiently retain its input quotient, quotient result, remainder,
two-element result array and the divisor; the original C stays reachable.
Within existing normalization, trimming may copy the returned quotient magnitude.
The constructor from output bytes also may retain a temporary magnitude alongside
its trimmed copy. All those input/output-sized terms are part of allocation
gate548; no constant-memory, bounded-heap, speedup or all-range availability claim
is made. Prior dead quotients can remain until GC; the design promises no GC
schedule or fixed total allocation bound.

## Rounding, carry, cohort and final publication

For integer N=a*a+r, inexactness is exactly r!=0 and sqrt(N) is above a+1/2
iff r>a. Equality r=a is strictly below half; an integer N never ties. Thus all
three half modes share this strict decision; positive UP/CEILING increment and
DOWN/FLOOR retain. Exact raw inputs are unchanged even for UNNECESSARY; inexact
UNNECESSARY throws. Public ordinary calls never reach this fallback with that
mode because the original earlier branch remains in place.

When incrementing an all-nine p-digit root, the result is exactly 10^p. Divide
once by ten and decrement Long scale once, retaining p digits. Other increments
cannot increase precision. Then divide exact factors of ten only while scale
exceeds preferred. All rounding and stripping precedes any public coefficient
bit-range or Int-scale check. No second inexact rounding occurs.

The accepted proposal's cohort argument applies unchanged: if final u<=preferred,
b<=sqrt(10C)+1 has at most323228498 digits and is supported. If u>preferred,
stripping stopped at a nonmultiple of ten, so no exactly equivalent lower-scale
integer coefficient exists; higher scales cannot rescue an excessive coefficient
or scale. The ordinary working scale is bounded below by -1396970320 before its
one carry decrement; stripping stops at preferred>=-1073741824. No lower Int
overflow is introduced. A too-large provisional Long scale can be rescued by
the zero tail. Only the final normalized scale is checked into Int.

Final decimal chunks are converted to private base2^30 cells by Horner steps
prefix*10^9+chunk. A primitive part is below 2^30*10^9<2^60; carry<10^9<2^30,
so at most one extra binary word is appended per step. Every nonnegative prefix
is <=the final stripped coefficient. A prefix crossing Int.MAX_VALUE bits proves
the final coefficient unsupported. The check follows each Horner step before
any public wrapper; a crossing temporary uses at most L+30 bits. Actual bit
count is Long. Byte count ceil(bits/8)<=268435456 is safely Int; each byte index
maps its low bit to floor(bit/30), and offsets above22 combine the next word
only when it exists. Bits above the final high word become zero. The byte
constructor receives only a supported positive final magnitude, never an
unsupported intermediate. Conversion is String-free.

## Prepared independent checks and remaining gates

`DecimalSquareRootNormalization579Test` has three bounded methods, all UNRUN:

1. Sixty-four (coefficient,positive-shift) cases, all eight modes, compare the direct
   internal kernel against Long division-bisection floor roots and squared odd
   midpoint boundaries. The original sixty small cases alone did **not** exercise
   a second output binary word or a nonzero two-cell remainder; that source-test
   gap was found by independent review before compilation. Four dedicated h=1
   cases now cross those boundaries, without applying shifts2..6 to large C.
   For C=123456790209876543/544, a=1111111111 and r=1111111109/1111111119,
   respectively: both raw root/remainder use two decimal cells, output root is
   above2^30, and the pair straddles the strict midpoint decision r>a. For
   C=123456792076543210, a=1111111119 and r=1999999939; its final subtraction
   22000000000-20000000061 requires a low-base10^9-cell borrow. These identities
   follow by expanding (1111111111+8)^2 and subtracting the stated N, separately
   from the restoring code. C=99999999999999999 gives N=10^18-10, floor root
   999999999 and an increment across a decimal-cell boundary before carry
   normalization. All eight modes (including inexact UNNECESSARY rejection)
   are checked by the same independent oracle. The largest N is
   1234567920765432100<1.3e18, so a<1.2e9 and all four-N/midpoint-square products
   are below6e18<Long.MAX_VALUE. Bisection has exclusive upper bound2e9 and
   uses division comparisons; it does not use decimal restoring pairs. No new
   runtime result is claimed by this test-only correction.
2. Literal all-nine carry, exact preferred stop, Long-scale rescue by zero tails,
   and unresolved upper-scale rejection. Exact internal cases deliberately test
   normalization mechanics even though public finite563 would take its shortcut.
3. Conservative eligibility controls, ordinary public sqrt(2) scaled by even
   and odd powers of ten, all modes, and the unchanged exact shortcut with
   Int.MAX_VALUE precision. No synthetic production maximum, giant public object,
   poisoned callback guarantee or RNG expectation is used.

The direct raw tests are necessary because bounded ordinary public examples
retain the materialized path; no forced production threshold was added just to
exercise this fallback. The literal sqrt(2) and sqrt(.2)/sqrt(20) controls use
squared decimal boundaries, not a host floating approximation as an oracle.

Still required: independent actual-source review, compilation, those tests plus
retained563/565 and relevant dispatch tests, full supported target selections,
exact-source artifact/CI binding, 541/526 coefficient callback classification,
and548 resource/performance investigation. The restoring method and repeated
zero-tail division can be expensive; there is no speedup claim. No empirical
result, public JDK defect, giant-case pass, platform acceptance, task transition,
or closure of579/542/541/548 is claimed by this source milestone.

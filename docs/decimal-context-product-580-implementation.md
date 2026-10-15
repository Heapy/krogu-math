# Context product580: implemented source/proof candidate

**SOURCE ONLY / UNCOMPILED / UNRUN / NOT INTEGRATED.** All changes are confined
to `/private/tmp/krogu-math-task-580-UvdPNirY/root`, a no-Git archive of35086
with the parent's frozen579 four-source overlay. The supplied223-file base
manifest check is retained outside root at `../evidence/source-base-check.txt`;
this author read its original OK ledger, not a new execution certificate.
Original owned BigDecimal4ab8/BigIntegere4dc and accepted proposalfdfe were
copied and hash-verified BEFORE edits in read-only
`/private/tmp/krogu-580-originals.utE2tddx/`. No publication-root, foreign worker,
579 helper/test/proposal, existing test or other numerical kernel was edited.
No compiler, script, numerical helper, certificate, probe, test, build, benchmark,
Git or tracker operation ran. No foreign implementation source/body/bytecode
was read. The parent owns independent review, integration and all runtime grants.

## Exact source binding and diff

| Candidate | SHA-256 |
|---|---|
| `BigDecimal.kt` | `1c1559c44de0a51bfcd7df0c325dfb9de22e7b6a4588b6dcb612370dc850fa55` |
| `BigInteger.kt` | `b2fb23547d97f3f7650b6d7dcf4ad53939efb3abac97d9d152376e67dd6685f5` |
| new `DecimalProduct580.kt` | `2ef14e2a2d52b75286f2e5830301a75fe586ed41cb3fbbacb9fd96c346d5234d` |
| new `DecimalContextProduct580Test.kt` | `e8cff7f799607b7902a5e9f37f4fb272357793f33d5ff1b336047301ed743e2b` |

BigDecimal's only diff is the contextual multiply hunk at137–148. Its exact
multiply, private multiplyValue, sqrt579 and all other methods are unchanged.
BigInteger's only diff inserts the single internal nonvirtual bridge at82–90;
the existing exact multiply and every other kernel are unchanged. New helper
and test files are standalone. No public declaration, instance field, cache or
serializer changes. The helper's small immutable decimal powers are scratch
constants, not a per-value or persistent result cache.

Accepted complete design is `/private/tmp/krogu-580-context-product-proposal.md`,
SHA `fdfe935d7bb4daea0464b6162446dabd5c6f3951dd6b764dc8de72ddc051fb3b`;
the initial mathematical obstruction is the separately retained580 audit. The
design's ordinary arithmetic/cohort theorem is instantiated below. Neither the
proposal nor historical evidence has been relabelled as executed code.

## Original public/virtual prefix and selection

`BigDecimal.kt:135` retains p=0 virtual exact-multiply delegation. Lines137/140
retain the same logical Long scale and exact short-circuit OR expression. Thus
the high-scale coefficient signum observations occur at the same source points.
In the true branch,142 evaluates the same scaleForValue(logicalScale,
left.signum()==0) that multiplyValue previously evaluated first. It still
rejects a nonzero receiver below Int.MIN_VALUE or preserves the existing zero
saturation/orientation before product selection. On a null bridge result,145
performs the same exact coefficient multiply, decimal construction and private
roundValue as multiplyValue did. The false/HIGH branch retains its original
neutral product, stored-digit check, round and adjusted scale at149–154 when
the bridge returns null. The raw selection is inserted at143/147 only after
these preserved prefixes. No old public null/argument validation is replaced.

The BI bridge reads private stored signs and exact magnitude bit lengths; it
invokes no public bitLength, abs, signum, byte encoding or arithmetic callback.
The top-level selector `DecimalProduct580.kt:5–6` has no scratch constants to
initialize on ordinary unselected calls. The raw object/powers are reached
only on selection. For nonzero bit lengths a,b and L=Int.MAX_VALUE,
bits(abs(A)*abs(B)) is either a+b-1 or a+b. Hence sum<=L guarantees the existing
exact product fits. Sum>L selects raw, covering both definitely oversized
sum>=L+2 and uncertain sum=L+1. Rejecting just sum-1>L would miss that edge.
All bit sums are Long. Stored zero signs return null first. No p cap, fake
production threshold, catch/retry or public oversized product is used.

With a supported exact product, its decimal D<=646456993 and a required
roundValue drop<=646456992; the retained materialized branch's drop power is
inside the accepted integer domain. Allocation and unrelated existing leaf
contracts still apply. This repair does not widen exact BigInteger multiply.

## Actual raw representation, input isolation and product invariant

`rounded:15–28` accepts canonical read-only base2^30 magnitudes and sign±1 from
the bridge, p>0, and the already mapped scale. No input reference is retained
outside this call. It allocates one paged binary scratch with the larger input
word count and reuses it for both conversions. `decimalChunks:85–100` copies
each input's words before division, overwriting all cells in that input's active
range. Old higher cells from the previous operand are outside active size and
never read. Original input arrays are never destinations. For each divisor1e9
pass, part=(remainder<<30)+word<10^9*2^30<2^60, quotient words fit30 bits,
remainders fit a decimal cell, and high-zero trimming preserves value.

The two decimal buffers are little-endian B=1e9, with nonzero high limbs. Each
has at most71828555 cells from the accepted Dmax646456993 bound. The product
capacity is n+m+1 in Long before a proven-safe Int narrowing, at most143657111;
its initial active size n+m is zero-initialized logically. `Cells:134–146`
returns zero for an unwritten valid cell even if its page does not exist yet.
This is necessary for convolution; it does not reuse579's allocated-page read
assumption. Writes allocate independent zero-filled8192-Int pages.

At29–38, each row adds the positional products A[i]*B[j] to the accumulated
lower rows. The existing cell and carry are included, then reduced modulo B.
The carry target i+m was untouched by earlier rows (whose greatest target is
i+m-1). Induction gives the exact full magnitude abs(A)*abs(B). Every primitive
part is bounded by (B-1)^2+2(B-1)=B²-1<10^18<Long.MAX_VALUE; carry remains<B.
The implementation explicitly promotes the first multiplicand toLong before
the primitive product. All product coefficients remain private decimal cells,
including a result with more than L binary bits. Trimming39 yields positive
canonical magnitude. Its D<=1292913986 fits Int; `digits:158–164` first computes
9*(size-1)+top width inLong. No public radicand/product, giant String or10^k
divisor is constructed.

## Rounding binding for every p, mode, sign and carry

For p>=D, drop=0 and all digits are retained, including under UNNECESSARY.
For p<D,42 computes k=D-p inLong, 1<=k<=D-1. The guard at k-1 and all lower
sticky digits are captured at44–52 BEFORE any destructive quotient movement.
The cell/offset decomposition and bounded literal powers exactly classify the
discarded remainder relative to zero and10^k/2. Guard5 with no sticky is a
genuine tie; guard5 with sticky is above. q odd is its low decimal cell mod2.

`dropDigits:165–173` copies cells low-to-high to remove k/9 entire low cells.
This direction cannot overwrite any unread higher source. Active size is then
shortened, and `divideSmall` traverses high-to-low to divide by10^(k%9).
Remainder<10^8 gives part<10^17; quotient cells fit B. The result is exactly
floor(M/10^k), positive with p digits. The mode table68–76 implements UP away
from zero, DOWN toward zero, directed sign choices, the three half rules and
inexact UNNECESSARY rejection. It is invoked only when discarded digits are
nonzero, so exact reductions keep values in every mode.

`increment:184–188` carries through B-1 cells and at most one extra cell.
If precision becomes p+1, its value is exactly10^p:57 divides byten exactly,
checks zero remainder, and increases the required drop once. This is the old
roundValue exact precision-carry drop, not a second inexact rounding. No other
increment changes precision. Product positive magnitude cannot round to zero
at positive significant precision. Sign is applied only on final publication.
All p through Int.MAX_VALUE work: p is never a scratch allocation size, and
D-p is computed inLong, so a large p simply retains the exact private product.

## Required cohort, scale and final supported coefficient

Ordinary nonzero LOW scale is rejected at22 before raw conversion (normally
the BD prefix already does it). HIGH scale with D<=p rejects at41, retaining566
even when optional trailing-zero removal could produce another numeric cohort.
Otherwise the mandatory drop/carry is complete before checkedScale(scale-drop)
at64. Input scale sum lies in[-4294967296,4294967294] and total drop<=D, so this
arithmetic is safely Long. There is no optional zero stripping: e.g10^18 atp3
must retain100@-16, not1@-18. The claim is support for the existing required
coefficient/scale representation, not all numerically equal cohorts. Existing
scale/error diagnostics and multiple-invalid precedence are not inferred anew.

Final conversion103–130 is monotone Horner in private base2^30 cells. Each
part=limb*1e9+carry<2^30*1e9<2^60 and carry<1e9<2^30, so one extra word suffices
per decimal chunk. Every positive prefix is <=the final required coefficient.
A first Long bit-count crossing of L proves that final coefficient unsupported;
no public wrapper discovers the crossing. The crossing needs at mostL+30 bits,
bounded by71582790 cells. On success byte countceil(bits/8)<=268435456 fitsInt.
Byte positions use Long8*i, map to30-bit word/offset, combine the next word only
when it exists and offsets exceed22, and mask one byte. The signed-magnitude
constructor receives an already supported magnitude for either sign. Its own
normalization remains a separate existing exact publication leaf. All raw
rounding and mandatory carry normalization precedes coefficient validation.

## Indices, live allocations and actual source tests

For maximum n,m, i+j<=n+m-2 and i+m<=n+m-1<143657110, so Int loop/index sums
do not wrap. Guard/sticky indices lie below active size because k<D. Dropping
whole limbs leaves at least one active limb. Decimal small-division primitives
are bounded as above; increment carries are at most1; no raw subtraction/borrow
algorithm exists in this design. A separate invented borrow path is not needed.
Capacities/bit/byte/scale calculations useLong before proven-safe narrowing.
The largest page table has fewer than18000 entries. Each logical index is bounds
checked before page access, append writes before increasing size, and unused
high capacity never affects numeric comparison/digit count.

Five paged buffers can remain simultaneously live: input binary scratch, both
decimal operands, product reused as quotient, and final binary scratch. Add all
page tables and up to8191 padding cells per buffer, both original inputs, output
byte array, final public magnitude and possible constructor trim copy. The code
does not rely on GC of earlier buffers before final publication. No per-product
row/trial/quotient/remainder allocation occurs. This is not a fixed-heap, fast,
maximum-domain feasible or low-total-allocation claim. Repeated conversion and
schoolbook multiplication can be expensive; resource gate548 remains separate.

The new common class has four prepared methods, all UNCOMPILED/UNRUN:

1. Fourteen positive Long input pairs, both product signs, seven precisions
   (including Int.MAX_VALUE), all eight modes. The independent model uses a
   bounded Long product and one power-of-ten quotient/remainder; half is
   r.compareTo(divisor-r), not the raw guard/sticky or convolution. Maximum
   product4000000004000000001<4.1e18<Long.MAX_VALUE. Drop<=18 keeps powers<=1e18;
   r<divisor, divisor-r positive, and even2r would be below2e18 (not computed).
   q+1 occurs only with positive drop and q<4.1e17; atdrop0 no increment occurs.
   These are explicit Long bounds, not a floating oracle. Multiword binary
   inputs/outputs, two-cell decimal operands, B-1 row carry, true even/odd ties,
   sticky neighbors, cell-crossing discard and precision carry are covered.
   Both raw input arrays are compared with pre-call copies, including failures.
2. Six scale endpoints, mandatory/exact reduction/carry cases, both signs and
   every mode, plus literal required100@-16 and retained HIGH/no-drop rejection.
3. Symbolic selector tests for zero bits, sum=L, uncertain L+1, definitely
   oversized and maximal sums. They allocate no giant number or fake small cap.
4. Independent ordinary, truthful override and caller-written-super public
   product value/scale controls, endpoints, unlimited multiplication and the
   asymmetric zero controls. These bounded public products use the materialized
   path; only direct internal raw calls exercise the new kernel at these sizes.

Existing explicit caller result/exception identity suites remain unchanged and
required. No sentinel-only dispatch self-test, internal callback order/count,
noncall or exact-multiply-delegation requirement is added. No optional JVM class
was added: the bridge introduces no virtual coefficient leaf. Its restored
coefficient composition is a source obligation below, not claimed execution of
the new bridge from579's distinct current-format test.

## Restored coefficient seam and remaining gates

Ordinary constructors normalize coefficient subtypes; current own-format
restoration can retain a consistent subtype. Both are accounted for. Canonical
stored fields satisfy the BI bridge and raw theorem regardless of dynamic class.
Only the exact original BD prefix invokes public coefficient signum; the raw
bridge reads stored signs/words and makes fresh private scratch. It never
consults a public metadata/byte/division/multiply hook. Truthful prefix sign
answers therefore compose directly with the same stored-value theorem. Current
format reachability is established by the retained source setup, not a new580
execution or a legacy serialization requirement.

Selected inherited coefficient multiply staging is replaced; a truthful exact
integer multiply may itself reject a product outside its public range. That
incidental inherited call does not justify rejecting the required supported
rounded decimal result. Explicit public coefficient calls and explicit decimal
override/super calls keep their own contracts. On unselected paths the old
virtual exact multiply stays. Dishonest prefix sign answers, callback replacement
objects/bytes, directly injected failures and mixed oracle observations remain
exact541/526 classifications; this is no blanket waiver of unaffected contracts
or proof of all inherited trace/exception-priority equivalence.

Still required: independent actual-source/proof/seam review, compilation, the
new bounded checks and retained566/579/dispatch suites on supported targets,
exact composed artifact/all-six CI, any specifically identified truthful seam
gap, and548 time/allocation investigation. No giant execution, public JDK
failure, performance acceptance, completed task or universal pow error proof
is claimed. All author-owned examination calls are terminal; no runtime window
is held or awaited. Source is frozen for parent review and separate grants.

# Integer bit semantics and source proof for task 545

This is a source-only argument for the bit kernels in `BigInteger.kt` at
`99c81a42a5d104e045bcfe6eca2246231d37bf80`, with the accepted constructor overlays
570 and 571 present. It supplements `NUMERICAL_ARGUMENTS.md:421–458`.
No numerical execution, compilation, oracle regeneration, or allocation experiment
was performed. The new tests are UNCOMPILED and UNRUN. No production defect was
established within the assumptions below; this is not full task acceptance.

## Contract and assumptions

The [public Java SE 25 BigInteger API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html)
specifies infinite two's-complement bit semantics, arithmetic shifts (including
negative distances), negative-index rejection, and metadata describing the value.
Its required magnitude range is strictly between `-2^Int.MAX_VALUE` and
`2^Int.MAX_VALUE`. Outside the supported range an arithmetic failure is permitted;
finite resources do not promise allocation success. Callback sequences and result
reference identities are not specified there.

The following derivation is independent mathematics applied to our own source,
not a description of foreign implementation internals. Assume a normalized stored
value `x=s*m`, `s` in `{-1,0,1}`, with little-endian limbs `a_i` in `[0,2^30)`,
`m=sum(a_i*2^(30*i))`. Zero has no limbs; a nonzero magnitude has a nonzero last
limb and magnitude bit length `B<=M=Int.MAX_VALUE`. `normalized` at line 1140
trims high zeros and checks the top limb and magnitude range using
`MagnitudeLimits.checkedBitLength`; sign and lower-limb validity are preserved
by its callers, not fully revalidated there. Serialization validation also
enforces these invariants. Constructor correctness
and other arithmetic kernels remain their own audit obligations.

Public metadata overrides used in this numerical proof must return the metadata
of the stored value every time. Logging and composition that preserve the value
are allowed. An immutable value cannot truthfully have changing bit length or
lowest set bit. Lying metadata, deliberate callback exceptions, and changing
answers are diagnostics, not counterexamples to the mathematical contract.

## Finite width and infinite sign extension

For `x>=0`, let `b` be the ordinary magnitude bit length (zero gives zero).
For `x=-m<0`, `b=bitLength(m-1)`. In either case, `w>b` implies
`-2^(w-1)<=x<2^(w-1)`. In particular `-2^(w-1)` is representable even though
its magnitude has `w` bits. Set `w=32*(floor(b/32)+1)`; then `w>b`, including
`b=0`. Binary operations use the larger selected width.

Define `E_w(x)=x mod 2^w`, chosen in `[0,2^w)`. Bits at positions `>=w-1`
are the constant sign of `x`. Applying AND, OR, XOR, or AND-NOT to two such
sign-extended sequences gives a constant high tail equal to the Boolean
operation on the two sign bits. Its low `w` bits therefore decode to the unique
signed integer with that infinite tail. Complement uses the same argument and
equals `-x-1`. This proves finite-width evaluation, provided any out-of-range
decoded magnitude is rejected at normalization. Example: complementing
`2^M-1` mathematically yields `-2^M`, outside this implementation's range.

`bitwiseLength32` at line 680 uses `(bitLength() ushr 5)+1`, equal to the formula
for truthful nonnegative `b`. Left metadata is evaluated before right metadata;
self operands cause two evaluations in this implementation. Neither ordering nor
count is a premise of the value proof. A false small width can discard real bits;
there is deliberately no attempt here to call that result mathematically valid.

## Packing and decoding at a partial limb

Let `L=ceil(w/30)` and `P=30*L`. `twosComplement` at line 707 produces
`E_P(x)`. For positive `x`, copying magnitude limbs and filling zeros suffices.
For negative `x`, each digit is complemented under `MASK=2^30-1` and a carry
initially equal to one is added. Inductively after `k` digits the emitted low
digits equal `-m mod 2^(30*k)`; the carry is always zero or one. Each temporary
sum is at most `2^30`, safely held in Long. The binary operation masks each limb
back to 30 bits. Thus its low `w` bits are the intended Boolean result.

The top limb contains `h=((w-1) mod 30)+1`, between 1 and 30, selected bits.
`bitwiseResult` at line 689 masks that limb with `(1 shl h)-1` and takes bit
`h-1` as the sign. `1 shl 30` is positive and representable. Filling positions
`h..29` with that sign changes the P-bit integer to the sign extension of its
w-bit interpretation. If `h=30`, the extension mask is zero. Consequently the
decoder can inspect bit 29 uniformly. For a negative result, complement plus
one recovers `2^P-E_P(x)`; for a nonnegative result the limbs are already the
magnitude. Normalization then enforces the range and canonical magnitude.
The extra copy used for zero results affects identity only.

Since `w` is a multiple of 32, the reachable `h` values are the even numbers
2 through 30. Width 480 is the first complete 30/32 alignment; width 512 has
`h=2`. The new portable aligned literal exercises complement and an update
through that transition. It uses a 480-bit magnitude, not an enormous endpoint.

## Queries and single-bit updates

For nonnegative `n`, `index=n/30` and offset `n%30` satisfy `0<=offset<=29`.
`testBitValue` at line 632 needs no array proportional to n. A missing positive
limb is zero. For negatives, locate the first nonzero magnitude limb `f`.
Complement plus one gives zero below f, `-a_f mod 2^30` at f, and the complemented
magnitude above f. Reading only a low-30-bit mask makes the unmasked signed Int
forms `-raw` and `raw.inv()` valid. Missing high limbs become ones. The scan is
safe because a negative normalized value is nonzero.

Each query/update rejects `n<0`, including Int.MIN_VALUE, before dividing or
allocating. Updates then evaluate width metadata and use the private bit query.
For set and flip the addressed width is `32*(floor(n/32)+2)`, strictly larger
than `n+1`. For clear it is `32*(floor((n+1)/32)+1)`, also strictly larger than
`n+1`. In both cases bit n lies below an untouched sign bit. Taking the larger
of operand and addressed widths and updating only bit n therefore implements
the infinite operation without changing its infinite sign tail. Zero may become
positive, and clearing the last positive one may produce zero.

At `n=M`, all supported nonnegative values have a zero bit and all supported
negative values have a one bit. Clear on the former and set on the latter are
unchanged values; this implementation returns a fresh copy after the metadata
call. The other updates produce magnitudes at least `2^M` and are rejected.
For example, clearing bit M of `-m` gives `-m-2^M`. This shortcut does not prove
anything about enormous lower addresses or require the reference JDK's
allocation strategy. Existing MAX/null tests already cover those observations.

## Shift semantics and storage bounds

Write `m=q*2^n+r`, `0<=r<2^n`, for `n>0`. The limb splice in `shiftRightValue`
at line 590 computes q. For negative x, floor division is `-q` if r is zero
and `-q-1` otherwise. With `t=lowestSetBit(m)`, `r!=0` exactly when `t<n`.
The private lowest-bit query supplies that predicate; subtraction is applied
to a normalized base-class intermediate, not to the original subclass. If
`n>=B`, the result is zero or minus one without allocating a shifted magnitude.

Left shift by positive n has magnitude bit length `B+n`. Its Long preflight
rejects `B+n>M`; otherwise whole=n/30 and rem=n%30 implement multiplication
by `2^n`. A shifted digit and carry use fewer than 60 bits. The allocated
`words.size+whole+1` is at most `ceil((B+n)/30)+1`, hence at most 71,582,790;
the extra carry cell is permitted even when it normalizes away. Right shift
with `0<n<B` has `whole<words.size` and allocates a positive difference.

Zero is handled before distances and returns zero for all Int distances. For
nonzero x, distance zero returns the receiver in our source; that identity is
an observation. Negative distances reverse direction through private methods.
Int.MIN_VALUE cannot be negated as Int: left shift at that distance is a right
shift by `2^31`, yielding zero or minus one because `B<=M`. Right shift at that
distance would multiply a nonzero magnitude by `2^(2^31)`, necessarily outside
the range, and throws. The zero shortcut remains valid for that distance.

## Metadata and integer intermediates

For negative `-m`, bit length is B except when m is a power of two, when it is
`B-1`. Checking that the top limb has one set bit and every lower limb is zero
recognizes exactly those powers, including `-1`. Short-circuiting the nonnegative
branch keeps the empty zero limb array safe.

The source expression for B is `(size-1)*30+32-leadingZeros(top)`. The product
is at most `M-1`; **the intermediate addition of 32 can overflow Int** near the
endpoint. The final mathematical B lies in `[1,M]`, and Int wraparound followed
by subtraction recovers it modulo `2^32`. This is a modular-arithmetic argument,
not a claim that every intermediate is in range. For maximal limb count
71,582,789, the product is 2,147,483,640 and the valid top limb has at most
seven bits. The checked Long normalization calculation is independent of that
temporary wrap. Cross-target lowering remains a runtime verification obligation.

Let `t=v2(m)` and `c=popcount(m)`. The lowest one bit of `-m` is still t, so
`lowestSetBitValue` scans the magnitude. Its multiplication `i*30` and addition
are bounded by `B-1`. Zero returns -1. Counting negative differing bits is
`popcount(m-1)=c+t-1`: subtracting one replaces the lowest one by zero and
the t lower zeros by ones. Since `c<=B-t`, the source addition `c+t<=B<=M`
does not overflow for truthful metadata. Each partial positive popcount sum
is bounded by B. `bitCount` calls virtual `getLowestSetBit` for negative values;
arbitrary replacement integers can overflow and are diagnostic inputs only.

## Width arithmetic and range handoff to task 548

For truthful metadata, `length32<=67,108,864`, so an ordinary Boolean width is
at most 2,147,483,648 bits. For `0<=n<M`, set/flip use at most 67,108,865
32-bit words, width 2,147,483,680. Clear uses at most 67,108,864 words: its
`n+1` is evaluated in Long before division. Conversion to the 30-bit cell count
occurs only after Long ceiling division; at the largest update width the count
is 71,582,790. `index<=71,582,788` at accepted update addresses, so indexing is
inside the selected array. These are arithmetic bounds, not memory promises.

Even false Int metadata cannot overflow the width calculation: unsigned shift
by five plus one is in `[1,134,217,728]`; Long width is at most 4,294,967,296,
and ceiling division by 30 still fits Int. Such metadata can cause very large
allocations or truncate the value and is outside the truthful proof.

Task 548 owns practical allocation/range policy and endpoint experiments under
the parent's numerical mutex. Remaining cases include valid near-M magnitudes,
supported-result operations whose temporary sign arrays exceed M bits, and
normalization of an actually out-of-range Boolean result. No smaller limit,
early rejection of a supported value, or allocation-success guarantee is proposed.
No enormous endpoint test is added here.

## Evidence gaps and prepared tests

The 17,198-row generator covers fixed metadata modes, separate-side overrides,
self operands, and power-of-two neighborhoods up to 4096. Its selected widths
do not include 480; it does not enumerate all programs that compose overrides.
`BitwiseSubclassContractTest`, `ShiftSubclassContractTest`, and
`IntegerBitsNullTest` already cover throwing bypass overrides, many shifts,
MAX queries/updates and receiver-metadata versus null diagnostics. They are
preserved rather than duplicated wholesale.

`IntegerBits545ContractTest` adds literal composed pipelines at 30/32/64 bits,
a full top-limb alignment at 480, exact/inexact negative-floor shifts, and
a sparse negative borrow chain plus truthful composed overrides invoked through
a base-typed direct caller. Literal
expected values avoid using a second bit kernel as the only numerical oracle.
`IntegerBits545JvmTest` prepares paired public-JDK/own cases with independently
asserted values. It logs internal trace/count/identity observations separately,
including self aliasing and competing failures. Direct caller dispatch and
propagation of an explicitly thrown exception are language-level assertions;
internal callback order and fresh/cached identity are not normative assertions.

No finite collection of these cases proves all composed subclasses, resource
behavior, or platform lowering. The files have only received source review;
the parent must choose subsequent execution under its sole numerical mutex.

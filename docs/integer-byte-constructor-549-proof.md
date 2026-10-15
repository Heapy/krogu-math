# Integer byte constructors: conditional representation proof (549)

This source-only argument covers all four own byte-array constructors. It adds
no implementation or tests and claims no new certificate, runtime observation
or platform pass. No script, compiler, helper, numerical operation, test, build,
Git operation or tracker operation was executed for this milestone. Required
integer-range and allocation obligations in task548 and the complete constructor
inventory in task549 remain separate.

The [Java25 public constructor contracts](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#%3Cinit%3E(byte%5B%5D,int,int))
use big-endian two's-complement slices and big-endian unsigned magnitudes with
sign -1/0/1. They permit empty magnitudes, reject an empty two's-complement input
array, and assume the array remains unchanged during construction. The
[class range clause](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html)
requires magnitudes below `2^Int.MAX_VALUE`, excluding both signed endpoints;
out-of-supported-range results may be rejected arithmetically. Those are public
interfaces, not foreign implementation sources.

## Exact source and leaf assumptions

| Own source/evidence | SHA-256 |
| --- | --- |
| `src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt` | `df55cb8c8c6a291b59f6ee6daf365c9b4c38e4e1f33583e108183c5a28039fb3` |
| `src/commonMain/kotlin/io/heapy/krogu/math/MagnitudeLimits.kt` | `3024002fc88ea3e90329905041eaa609fd0d2f2e08323a78e84c8640debff161` |
| `src/commonTest/kotlin/io/heapy/krogu/math/IntegerByteOracleTest.kt` | `6120e6bd9eb4645b89847fc1bdd2d9325215fb396e39ab2fe352c2621309890d` |
| `src/commonTest/kotlin/io/heapy/krogu/math/MagnitudeLimitsTest.kt` | `237ad39ce56a9108fd3b15df4043da035aaba48be15d3e9b60e6e49dc7001a10` |
| `scripts/generate-integer-bytes-oracle.main.kts` | `f5d30d549e883271aa5ab42470772ed9c3e9ccd64de5a0724a5b859bd13c62c9` |
| `src/commonTest/kotlin/io/heapy/krogu/math/Jdk25IntegerByteResults.kt` | `7c495db81638707251a3c0fb708c66b837f231697105d96058ff7ce5e42a5461` |

Assume stable non-null input bytes for the duration of construction, correct
Kotlin primitive integer/bit/array operations, ordinary fresh-array semantics,
and completion of required allocations. The bounds below prove primitive
operations do not overflow where exact arithmetic is required; they do not
prove those primitive/runtime contracts on every platform, available memory,
termination within a budget, or behavior under concurrent input mutation.

The actual byte helpers are companion `fromBytes` (`BigInteger.kt:1087-1122`),
`signedBytes` (`:1123-1134`), `checkBounds` (`:1135-1138`) and `normalized`
(`:1139-1145`). There is no helper named `fromSignMagnitude`; that role belongs
to `signedBytes` plus `fromBytes(...,twos=false)`. The separate instance helper
`fromTwosComplement(IntArray)` (`:698-706`) belongs to bitwise result conversion,
not byte construction. Its distinct interpretation is discussed below rather
than silently substituting it for the byte path.

## Bounds, empty input and validation order

Let `N=bytes.size`. After `checkBounds` accepts, `off>=0`, `len>=0` and
`off<=N-len`. Left-to-right OR short-circuiting stops on negative offset or
negative length before the subtraction. Its subtraction is safe: only nonnegative Int lengths reach that
comparison, and `N-len` lies between `-Int.MAX_VALUE` and `Int.MAX_VALUE`.
If `len>N`, the difference is negative, so every nonnegative offset is rejected.
Otherwise the accepted inequalities imply

`0 <= off <= end=off+len <= N <= Int.MAX_VALUE`.

Thus accepted `end` is computed without overflow. A zero-length slice may start
at `N`; no byte is read in that case. Positive length implies `off<N` and
`end>=1`, making the sign-byte read and `end-1` iteration start valid.
All stripping/packing/scanning indices stay within `[off,end)`.

Validation paths are intentionally distinguished:

* Two's-complement construction rejects `bytes.isEmpty()` before bounds checks.
  A whole empty array therefore throws `NumberFormatException`, as does the
  sliced overload on an empty array before competing invalid slice checks.
* For a nonempty two's-complement backing array, bounds are checked first; an
  accepted `len=0` returns zero before a sign-byte read. An empty backing array
  and an empty selected slice in a nonempty array are different cases.
* `signedBytes` rejects an invalid sign before inspecting slice bounds. With
  sign zero it checks bounds and scans only the selected slice, rejecting any
  nonzero byte as a sign/magnitude mismatch; otherwise it returns zero.
* With sign +1/-1, bounds and empty-slice handling occur in
  `fromBytes(...,false)`. Empty valid magnitudes give zero for either sign.

These are reachable own branches and retained finite diagnostic observations.
They do not establish a universal priority for every combination of invalid
sign, null, empty backing array and invalid slice. Public exception categories
for isolated invalid inputs and historical exact message/compound-invalid order
are separate claims. Null boundary policy is outside this accepted-slice proof.

## Meaning of stripped bytes

For a selected slice of `L>0` bytes, let its unsigned polynomial be

`U = sum(unsigned(bytes[off+j]) * 256^(L-1-j), j=0..L-1)`.

Unsigned magnitude and nonnegative two's-complement inputs use this value
directly. The latter's sign is selected from the original first byte before
stripping. Removing leading zero bytes preserves `U`; an all-zero effective
slice returns canonical zero immediately. Otherwise let `k>0` be the effective
length after stripping, with the same unsigned value.

For negative two's-complement input, its mathematical value is `U-256^L`.
The code retains the original negative flag, then removes `t` leading `FF`
bytes while leaving at least one effective byte. Put `k=L-t` and call the
effective unsigned polynomial `V`. Then

`U = (256^t-1)*256^k + V = 256^L-256^k+V`,

so the same signed value is `V-256^k`, and its magnitude is `256^k-V`.
No condition on the next retained sign bit is needed: the effective bytes are
not reinterpreted as a new signed byte array. The retained negative flag and
this algebra supply the sign. In particular `FF 00` can strip to one zero byte
with magnitude 256; treating that byte afresh as signed zero would be wrong.
An all-`FF` slice strips to one `FF` byte and denotes -1, not zero.

## Complement-plus-carry invariant

Read the effective bytes from least to most significant; let their unsigned
values be `u_j` for `j=0..k-1`. For a positive magnitude the converted byte is
simply `u_j`. For a negative input initialize `c_0=1` and define

`z_j=(255-u_j)+c_j`, `a_j=z_j mod 256`, `c_(j+1)=floor(z_j/256)`.

The source's xor, mask and unsigned shift implement these equations, since
`0<=u_j<=255`, `c_j` is 0 or 1, and `0<=z_j<=256`. If
`A_j=sum(a_i*256^i, i=0..j-1)` and
`V_j=sum(u_i*256^i, i=0..j-1)`, telescoping the carry equations gives

`A_j + c_j*256^j = 256^j - V_j`.

At `j=k` the magnitude is exactly `A_k+c_k*256^k=256^k-V`.
The final carry is one precisely when `V=0`; all produced low bytes are then
zero. It is a genuine extra magnitude bit, not signed overflow. The code's
post-loop carry insertion is therefore necessary for `FF 00`, `FF 00 00`, and
the corresponding longer stripped-zero payloads.

## Packing into 30-bit cells, including crossings

Set `B=2^30`; the own constants are `LIMB_BITS=30` and `MASK=B-1`
(`BigInteger.kt:1002-1003`). After `j` converted low bytes have been consumed,
let `w=floor(8*j/30)` and `h=8*j-30*w`. The packing invariant is:

* `word=w`, `bufferedBits=h`, and `0<=h<30`;
* each written `result[i]`, `0<=i<w`, is the corresponding base-`B` digit of
  `A_j`;
* `buffer=floor(A_j/B^w)`, with `0<=buffer<2^h`.

Appending a converted byte places its low eight bits at position `h`. Because
the old buffer has no bits there, bitwise OR equals exact addition. Before a
possible flush the buffer is below `2^(h+8)<=2^37`; in fact `h` is even and at
most 28, because `h=8*j-30*w=2*(4*j-15*w)`. Thus it is below `2^36`.
All such shifts/ORs fit positive Long.
`h+8` is at most 36, so a single `if`, rather than repeated flushing, suffices.
When it reaches 30, masking emits the next exact cell, shifting right 30 retains
the remaining high bits, and subtracting 30 restores the invariant. Each cell
is in `0..B-1` and fits positive Int. This handles a byte straddling a 30-bit
cell without dropping or duplicating bits.

After all bytes, insert the final carry at buffer bit `h`. With `h=8*k mod 30`,
`h<=28`, so the resulting buffer is below `2^(h+1)<=2^29` and fits an Int cell.
For a positive input the carry is zero. For a negative carry of one the
converted payload is all zero, so this insertion yields precisely bit `8*k`.

`packedWords(k,negative)` reserves

`R = ceil((8*k + e)/30)`, where `e=1` for negative and `e=0` otherwise.

All loop emissions occupy indices below `floor(8*k/30)<=R`. At the final write,
if `h>0`, index `w=floor(8*k/30)` is below `R`. If `h=0`, a positive input has
zero final buffer and does not write at index `R`; a negative carry may require
index `w`, and `e=1` reserves exactly that extra cell. The important aligned
case is `k` a multiple of 15: `8*k` is a multiple of 30, yet a stripped all-zero
negative payload needs one more cell. The reservation supplies it. If the
negative carry is zero, any extra reserved cell remains zero and is trimmed.

## Normalization, supplied sign and isolation

The resulting cells represent the intended magnitude by the two invariants.
`normalized` removes only high zero cells, preserving the polynomial value.
An empty trimmed array returns canonical `ZERO`; otherwise the highest cell is
nonzero, every cell is below `B`, and the supplied sign is +1 or -1.

For sign-magnitude constructors, sign zero was handled by the earlier scan.
Sign +1 uses the unsigned conversion. Sign -1 negates its base-integer result
(`BigInteger.kt:41`): zero stays zero; otherwise only the sign changes, with
the same immutable magnitude. Thus sign zero plus a nonzero selected byte is
rejected, all-zero selected magnitudes normalize to zero for every legal sign,
and bytes outside the slice do not affect value or mismatch detection.

The input `ByteArray` is never stored. Packing allocates a fresh `IntArray`;
normalization either retains this private fresh array or copies its live prefix.
The constructor's private base-value copy (`BigInteger.kt:15`) and internal
negation can share that private magnitude safely; neither exposes it to callers.
The base `toByteArray` implementation (`:819-827`) allocates a new `ByteArray`.
Consequently later mutation of either the original byte input or a returned
encoding cannot mutate the constructed object's magnitude. This does not extend
to arbitrary caller-owned overrides of `toByteArray` on other subclasses or
to concurrent mutation during the constructor's reads.

## Range guards do not reject a required-range value prematurely

The capacity guard runs after padding removal, with effective length `k`, not
the original input length. Leading zero/`FF` padding up to the input array's
representable size is therefore not itself classified as numerical overflow. All-zero
positive inputs return even before this guard.

Let `K=268435456=ceil(Int.MAX_VALUE/8)`. For a nonzero positive effective input,
its first byte is nonzero, giving magnitude at least `256^(k-1)`. For a negative
effective input with `k>1`, its first retained byte is not `FF`; otherwise the
stripping loop would continue. Hence
`V<=255*256^(k-1)-1` and `256^k-V>=256^(k-1)+1`.
If `k>K`, either magnitude has bit length at least
`8*(k-1)+1>=2147483649>Int.MAX_VALUE`. The one-byte all-`FF` exception is
irrelevant to `k>K`. Thus rejecting effective byte counts above `K` cannot
reject a value inside the required magnitude range.

For `k<=K`, capacity arithmetic in `MagnitudeLimits.kt:8-13` uses Long before
multiplication. Its largest numerator before division is
`8*K+1+29=2147483678`; the reservation is at most 71582789 cells, fitting Int.
It deliberately permits capacity for 2147483648 positive bits or 2147483649
negative-complement bits; reservation capacity is not the actual value's bit
length and is not rejected merely for crossing Int.MAX_VALUE.

After trimming, `checkedBitLength` (`MagnitudeLimits.kt:15-20`) computes exactly
`30*(liveWords-1)+bitLength(highWord)` in Long. The highest nonzero cell has
between one and 30 bits, so this is the actual magnitude bit length. It returns
iff that length is at most Int.MAX_VALUE and otherwise throws arithmetically.
For example live length 71582789 with high word 127 has exactly Int.MAX_VALUE
bits; high word 128 has one more. This is the magnitude count, not the public
negative `bitLength()` convention. It accepts all mathematically representable
results within this guard's range, provided the fresh reservation and trimming
allocations succeed. A conservative extra cell or a large backing byte array
does not prove memory availability or discharge all range obligations in548.

## Distinct `fromTwosComplement(IntArray)` route

The separate bitwise-result helper receives nonempty 30-bit cells. If the high
cell's bit 29 is zero it normalizes a positive result; otherwise it complements
each cell under `MASK` and adds one in base `B`, then normalizes a negative
result. With `m` cells and unsigned polynomial `W`, that computes `B^m-W`.
The selected high sign bit ensures `0<W<B^m`, so no extra final carry cell is
needed in this route. Its precondition comes from `bitwiseResult`'s masking and
sign extension (`BigInteger.kt:688-706`), not from byte-array sign detection.
This explains why its fixed-width carry handling differs from stripped byte
payloads; it is not a proof of all bitwise public behavior or subclass dispatch.

## Existing finite evidence and remaining scope

`IntegerByteOracleTest.constructorsAndArrayIsolation` (`:8-34`) consumes the
frozen JDK25 byte-constructor rows for all four overloads. Its generator
(`scripts/generate-integer-bytes-oracle.main.kts:9-39`) includes empty, zero,
single-byte signs, `FF 00`, all-zero/all-`FF`, and selected 4/7/8/9/15/16-byte
patterns. It combines finite offset/length neighborhoods with Int extremes and
seven sign choices, then includes whole-array calls. For successful rows the
test mutates the input and a returned encoding and checks both value and hash.
Its message-bearing error outcomes are finite historical observations, not a
proof of every invalid-input priority or every byte sequence.

`MagnitudeLimitsTest.representationBoundaryDoesNotWrap` (`:6-12`) already tests
the symbolic capacity/high-cell guards without allocating a maximal magnitude.
`largePaddingAndComplementCarry` (`:14-28`) already covers 131072 bytes of `FF`
padding ending in zero, aligned effective zero-payload lengths 15/30/45/120/1020,
and a 4096-byte all-`FF` input in signed/unsigned interpretations. These source
controls are reused; no duplicate tests or fresh execution are introduced.

The earlier [constructor coverage map](constructor-contract-549-coverage.md)
remains unchanged. This proof composes stable byte input, bounded primitive
conversion and checked representation guards; it identifies no new bug. It does
not close all of549, prove every primitive/platform/runtime, promise maximal
allocations, resolve all required numerical-range behavior in548, or settle
null/compound-invalid diagnostic classification. Independent read-only review
and any later execution remain root-owned.

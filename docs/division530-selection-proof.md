# Private division output selection: source proof

This is an independent Apache-2.0 SOURCE ONLY proposal. Nothing in this
directory has been compiled, executed, symbolically evaluated, tested or
measured. Baseline production SHA-256 is
`590d93919ce590b098708a5f567730fca2374cd8bf5d027f7164974203c99607`.
The original production and the two original task530 test sources were copied
before editing into the read-only `original/` directory; `original.sha256`
records them. No foreign numerical implementation source was consulted.

## Selection and arithmetic

There is one source-level numerical kernel, `selectStoredDivision`. Exactly
three private wrappers call it:

| Wrapper | needQuotient | needRemainder | Returned value |
| --- | --- | --- | --- |
| quotientStored | true | false | quotient |
| remainderStored | false | true | remainder |
| divideStored | true | true | fresh two-slot array containing both |

The final inline lambda is invoked once on each successful path, after all
requested outputs are ready. Division by zero throws before invoking it. No
lambda or result carrier is stored. Omitted outputs use the existing immutable
ZERO singleton internally. Each single-output wrapper ignores exactly its
omitted argument; the both-output wrapper requests both. No placeholder can
become a requested nonzero result. There is no unchecked cast, nullable result
cast, new object field, cache, mutable shared scratch, or duplicate numerical
loop in source. Code generation and the cost of inline specialization remain
unmeasured.

For the normalized multi-limb path, the baseline normalization, top-two
estimate, leading-three-digit correction, subtract/borrow, add-back, and digit
denormalization expressions are unchanged. The loop formerly ranged from
`quotient.lastIndex` to zero. Its new upper bound is `words.size - n`, exactly
the last index of the baseline quotient array of length `words.size - n + 1`.
It is independent of whether that output array exists. Only the write of a
finished estimate is conditional. No later estimate reads any quotient digit.
The remainder extraction loop runs only when its output is requested, after
the last subtract/add-back. It never feeds the division kernel. Thus omitting
either output cannot change the digits of the other.

For a one-limb divisor, each step still computes the same `part` and scalar
`part % divisor`. This scalar remainder is required even for quotient-only
operation because it feeds the next limb. The quotient digit calculation and
write occur only if a quotient output exists; neither feeds scalar remainder.
No single-limb numerical loop is duplicated.

## Values, signs, exceptions and identity

The canonical stored invariant is unchanged: nonzero magnitudes contain
30-bit limbs, no high zero limb, sign -1 or +1, and supported bit length;
zero has sign zero and an empty magnitude. The proposal does not change
construction, deserialization validation, or normalization.

* The stored zero-divisor test and its message precede every branch and
  allocation as before. Public non-null signatures remain unchanged.
* When `abs(a) < abs(b)`, quotient remains the ZERO singleton. A requested
  remainder still uses precisely `normalized(sign, words)`, including the
  old fresh nonzero wrapper sharing the immutable receiver magnitude and the
  ZERO result for a zero receiver. Quotient-only operation skips this unused
  normalization. It never returns the receiver in place of a requested
  remainder.
* For all other paths, requested quotient is still
  `normalized(sign * value.sign, quotientDigits)` and requested remainder is
  still `normalized(sign, remainderDigits)`. The baseline proof of
  `a = q*b+r`, `abs(r) < abs(b)` and truncation toward zero therefore applies
  independently to either selected output and to the pair.
* Requested zero outputs remain the ZERO singleton. Requested nonzero
  outputs retain baseline normalization, signs and fresh result objects.
  In the both-output path, evaluation order remains quotient normalization,
  remainder normalization, then fresh two-slot array construction. The
  single-output paths create no such carrier array.
* The skipped normalization of an unrequested output cannot suppress a
  magnitude-range error on valid stored operands: both quotient and
  remainder magnitudes are bounded by the valid dividend magnitude.
  Deliberately reduced allocation can change resource exhaustion behavior;
  it is not an arithmetic or argument-validation promise.
* Multi-limb divisor and dividend scratch remain fresh local arrays and are
  never mutated after returning a result. Requested output arrays are
  distinct from scratch and from one another. Normalization can retain or
  trim an output array exactly as before; no live mutable alias escapes.
  The lower-magnitude path may share only the unchanged immutable receiver
  array. `a === b` and magnitudes shared by `negate()` have no special write
  path. Caller mutation of exported bytes or pair slots cannot affect any
  stored magnitude.

All numerical accesses in the selector still use stored fields. No public
`signum`, `compareTo`, `abs`, `bitLength`, arithmetic override or subclass
callback was introduced. Public virtual call sites elsewhere are retained.

## Call-site inventory and choices

| Caller | Choice | Reason / preserved boundary |
| --- | --- | --- |
| BigInteger.divide | quotient-only | Same public signature, direct stored arithmetic; never delegates to an overridable pair method. |
| BigInteger.remainder | remainder-only | Same public signature and stored arithmetic. |
| BigInteger.divideAndRemainder | both | Public result shape and fresh pair retained. |
| gcd Euclidean loop | remainder-only | It only consumed old pair slot 1; zero fast paths and their virtual abs calls stay byte-for-byte unchanged. |
| sqrtStored Newton loop | quotient-only | It only consumed old pair slot 0; negative/zero/one-bit checks, private receiver copy and Newton operations stay unchanged. |
| sqrtAndRemainder | indirect quotient-only | It still calls sqrtStored, squares its root and constructs its own root/remainder pair. No public method is bypassed beyond the pre-existing private path. |
| modInverse Euclidean loop | both | Both quotient and new remainder are required; working operand copies, public prefix and final mod are unchanged. |
| decimalBinaryPrefix | both | Both coefficient quotient and nonzero remainder/sticky information are consumed; trim-zero branch retained. |
| toString(radix) | public divideAndRemainder, unchanged | Both values are consumed. Its virtual call is an observable subclass boundary and is not replaced with a private helper. |
| mod | public remainder, unchanged | Validation and virtual remainder call remain; inherited remainder uses the new selected path. |
| BigDecimal public integer consumers | public calls, unchanged | Existing divide calls naturally request quotient only, remainder calls request remainder only, and pair calls still request both. No BigDecimal source changed. |
| compareDecimalSignificand / divideDecimalChunk | separate unchanged private kernel | Its local in-place decimal-chunk quotient and scalar sticky data are not routed through this selector. |

These are every direct `divideStored` call and every newly added selected
private call in this file. No numerical consumer was rerouted through a
public override merely to avoid allocation.

## Array bounds and allocation bounds

Let `m = words.size` and `n = value.words.size`. In the multi-limb path,
`m >= n >= 2`. Normalized divisor V has n cells; dividend scratch U has m+1.
An optional quotient Q has m-n+1; an optional remainder R has n. All existing
index proofs therefore remain valid, including the extra U top cell. The
representation maximum is 71,582,789 limbs; m+1 and m-n+1 still fit Int.
No new size product, shifted magnitude, or potentially wider result exists.

| Requested outputs | Mandatory/local IntArray cells | Conservative cells including all possible normalization trim copies |
| --- | --- | --- |
| both | 2m+n+2 | 3m+n+3 |
| quotient only | 2m+2 | 3m-n+3 |
| remainder only | m+2n+1 | m+3n+1 |

The second column sums V, U and requested output buffers. The final column
adds up to m-n+1 copied quotient cells and/or n copied remainder cells even
if an optimizer could reclaim dead scratch earlier. It is a conservative
sum of allocated limb capacity, not a resident heap or GC claim. Both-only
also allocates its two-reference carrier; single-output wrappers do not.
Unrequested outputs allocate neither a limb buffer, a trim copy nor a result
BigInteger. Removing a multi-limb quotient buffer saves m-n+1 limb cells
before possible trimming; removing a remainder buffer saves n. The estimates,
subtractions and U storage are still necessary in remainder-only operation.

For a one-limb divisor, both outputs require an m-cell quotient buffer and a
one-cell remainder buffer; possible quotient trimming gives a conservative
2m+1 total cell bound. Quotient-only uses at most 2m cells including trimming;
remainder-only uses only its one-cell result buffer and no quotient array.
The scalar remainder remains a Long local in every mode. The smaller-input
path allocates no new magnitude array (normalization reuses canonical stored
words), and only requested nonzero remainder normalization constructs a
BigInteger wrapper. Existing ZERO construction/normalization semantics are
unchanged.

These are source allocation bounds. They do not establish timing, JIT code
size, actual allocation counts, maximum-heap behavior or whole-application
improvement. Such gates remain open.

## Prepared validation

Original Division530Test and Division530JvmTest remain byte-identical. They
already check both single-output public methods against the pair across
signs, zero/smaller inputs, scalar/multi-limb divisors, correction/add-back,
normalization shifts, aliases, public stored-only subclass entry points,
gcd/modInverse and decimal consumers. Their earlier execution, if any, does
not validate this proposal.

Two new common methods check the newly rewired sqrt consumer: independent
square-boundary identities across limb sizes and a stored subclass whose
public divide/metadata callbacks throw. One new JVM method compares sqrt
and sqrtAndRemainder against the pinned JDK at bounded limb boundaries.
All are UNCOMPILED/UNRUN. No test asserts allocator internals or duplicates
the division algorithm. Further parent integration, compilation, full target
gates, public workload execution and matched-source measurements are still
required before runtime or performance acceptance.

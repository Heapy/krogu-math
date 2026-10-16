# Separate decimal types: decision and contract study

Task: `local:540`. Evaluated against source
`b3a70fc5427f3771cfea07903c3b89155cff8b39`.

## Decision

Do not add another arithmetic type in this change. Keep `BigDecimal` as the
compatible arithmetic value, use the explicit numerical collection key from task
536 for scale-independent collections, and keep scale policy at the application
boundary. A fixed-scale type can become useful when a concrete application needs
enforced units and scale; a general decimal-value type with wider scale would
require new arithmetic, resource and conversion contracts that a wrapper cannot
provide. This is a completed evaluation and a decision against introducing these
types now, rather than a commitment to implement both later.

Neither a new equality default nor a larger exponent range changes existing
`BigDecimal` construction, equality, subclassing or arithmetic. Any future type
would be explicitly imported and final, with conversions named for their loss
policy. The sketches below describe candidate contracts, not existing APIs.

## Concrete scenarios

### Invoice line amounts with two fractional digits

An application receives a unit price `19.995`, quantity `3`, and a policy to round
the final line amount to two places using HALF_EVEN. Exact multiplication gives
`59.985`; rounding the line gives `59.98`. Rounding the unit price first gives
`20.00`, then `60.00`. A fixed-scale operand type alone cannot decide which
business policy is correct. Its rounding point must be explicit.

Existing operations express the final-rounding policy directly:

```kotlin
val price = BigDecimal("19.995")
val quantity = BigDecimal("3")
val lineAmount = price.multiply(quantity).setScale(2, RoundingMode.HALF_EVEN)
check(lineAmount == BigDecimal("59.98"))
```

A possible future `FixedDecimal` would therefore take an exact construction
path that rejects `19.995` at scale 2, plus an explicitly rounded construction
path. It would not silently quantize every input before the application chooses
the arithmetic rounding point. Currency, tax jurisdiction and units remain
separate domain data; equal decimal scale does not make unlike units addable.

### Measurement or price collections with representation-independent keys

A deduplication index receives `2.0`, `2.00`, and `2.000`. It needs one key while
retaining the original display precision in the record. A collection key satisfies
this without replacing the stored arithmetic value. That requirement belongs to
task 536; a second decimal-value arithmetic class would duplicate its equality
mechanism and add another operator surface without a demonstrated need.

### Scientific metadata with an exponent outside Int scale

A record may describe coefficient 1 at scale `2147483648`. It cannot be converted
to a nonzero `BigDecimal` merely by changing an import: the scale is beyond its
Int contract. Such metadata can be compared or transmitted symbolically, but
addition to 1 would request over two billion coefficient digits if made exact.
A wider scale field does not make that arithmetic feasible. This scenario needs
an explicitly bounded symbolic or scientific-number design, not an unqualified
promise of arbitrary exact decimal arithmetic.

## Candidate contracts and conversions

| Concern | Fixed-scale decimal candidate | Decimal-value candidate |
| --- | --- | --- |
| Representation | Immutable coefficient and an Int scale, or a separate immutable scale policy shared by values | Canonical signed coefficient with trailing decimal zeros removed, Long scale; canonical zero |
| Construction | `exact(value, scale)` uses UNNECESSARY; `rounded(value, scale, mode)` makes loss explicit | Exact coefficient/scale input within a declared supported range and resource policy |
| Equality | Same coefficient and scale; scale is part of the quantity representation | Numerical equality; canonical zero and sign/coefficient/scale determine equal hashes |
| Ordering | Numerical order followed by scale as a tie-break, so ordering agrees with equality | Numerical order consistent with equality |
| Addition/subtraction | Require the same scale policy; exact coefficient operation, checked supported range | Exact unless a named finite-precision operation is requested; scale alignment subject to a declared work limit |
| Multiplication | Named `multiplyExact` returns a BigDecimal intermediate; `multiplyRounded(other, targetScale, mode)` quantizes once | Exact coefficient product and checked Long scale sum; finite-context form states its rounding point |
| Division | Require target scale and rounding mode; an exact result request rejects nonterminating results | Exact form rejects nonterminating results; context form requires precision and rounding |
| Overflow | ArithmeticException for supported coefficient/range or scale-policy mismatch; no silent wrap or saturation | ArithmeticException for scale overflow, unsupported coefficient size or explicit work limit; no silent truncation |
| Conversion to BigDecimal | `toBigDecimalExact()` retains the exact coefficient and Int scale | `toBigDecimalExact()` succeeds only when an equivalent representation fits Int scale and the supported coefficient range |
| Lossy conversion | Quantization must name target scale and rounding mode | `toBigDecimalRounded(targetScale, mode)` states quantization; may still reject excessive work |
| Inheritance | Final wrapper; no promise of BigDecimal subtype substitutability | Final independent value; no cross-type equals |

For a fixed-scale candidate, numerical equality across different scales would be
a separately named operation or explicit numerical key conversion. Sorting must
not silently make unequal scale-sensitive values collapse in a sorted set.

For the wider-scale candidate, exact conversion can sometimes succeed outside
the nominal Int scale. For example coefficient 10 at scale `2147483648` is exactly
coefficient 1 at scale Int.MAX_VALUE. Conversely coefficient 1 at scale
`2147483648` has no such exact representation. Coefficients may also need powers
of ten to compensate a scale below Int.MIN_VALUE; exact conversion must check
range and work before allocating. Zero has an explicit canonical BigDecimal.ZERO
conversion. No conversion simply narrows a Long to Int.

The candidate rounded conversion is not a general guarantee that every wide-scale
value is convertible. Rounding a huge positive exponent into an Int-scale value
can still exceed the supported coefficient range. UNNECESSARY rejects discarded
nonzero digits; every rounding mode follows the named target scale. Interchange
serialization would need a separate versioned contract, not java.math stream
compatibility.

## Memory and performance assessment

The current BigDecimal stores a BigInteger coefficient and an Int scale, and its
constructor already snapshots a subclass coefficient through its byte encoding.
A wrapper retaining a BigDecimal necessarily retains that entire graph plus its
own object and reference when the wrapper escapes; JVM boxing and optimizer
elimination are workload-dependent. A direct coefficient/scale wrapper can avoid
retaining a nested BigDecimal, but would duplicate arithmetic plumbing or create
BigDecimal temporaries on each delegated operation. A Kotlin value class over a
BigDecimal does not make the underlying coefficient graph disappear and may box
in generic collections. These are structural costs, not measured byte counts.

Fixed-scale batches can share a scale policy and avoid repeated rescale checks,
but exact intermediate multiplication still requires wide coefficients. A
primitive-coefficient representation would overlap task 529; reusable arithmetic
workspace and final-rounding batches overlap task 535. Neither benefit requires
introducing another public type first.

A decimal-value wrapper that caches canonical digits/hash pays decimal conversion
and retained digit storage at construction, then can reuse the cached hash. Task
536 supplies precisely this tradeoff for collection keys. A Long-scale arithmetic
type additionally needs checked scale arithmetic and bounded alignment; wide
exponent metadata alone is cheap, while exact alignment can dominate memory.

No timing, retained-size figure or speedup is claimed here. Task 527 supplies the
reproducible cross-platform baseline harness. If a concrete product case later
justifies a new type, compare these fixed workloads with that harness:

| Workload | Baseline and candidate evidence needed |
| --- | --- |
| 100,000 invoice lines, exact product then final HALF_EVEN scale 2 | Existing BigDecimal chain versus explicit fixed-scale operations; identical values/scales, operation count, latency, allocations and peak memory |
| Repeated lookups of small and 4096-digit equivalent cohorts | Existing numerical comparison, task 536 key creation and cached-key hash lookup; record construction separately from reuse |
| Wide arithmetic with coefficients already beyond primitive storage | Check wrapper overhead and temporary creation rather than assuming small-value benefits transfer |
| Extreme positive/negative scales and zero | Validate checked exact/lossy conversion and early resource rejection, with no scale-sized padding allocation |

Record source/artifact bindings, input corpus, VM/runtime, warmup, repetitions and
available memory metrics separately on JVM, JS, Wasm and host Native. Until such
evidence and a concrete consumer exist, the added semantic and maintenance cost
outweighs an unmeasured convenience or speed claim.

## Completion boundary

This task delivers a concrete comparison and a justified minimal decision: no
new arithmetic type. Task 536 addresses the demonstrated collection-key use
case; existing BigDecimal plus explicit final `setScale` addresses the invoice
example. The study creates no deferred implementation obligation and changes no
public numerical contract. Future adoption requires a new scoped task with a
consumer, the contracts above resolved, and task 527 measurements.

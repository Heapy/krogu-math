# Decimal accumulation and batch arithmetic (task 535)

The new API is additive: `DecimalAccumulator`, `sumDecimals`, `dotProduct`, and
`BigDecimal.fma`. It does not change existing arithmetic or equality. Callers
choose `DecimalRounding.FINAL_RESULT` (the default) or `EACH_STEP` together with
a MathContext. These policies can give different numerical results.

```kotlin
val a = BigDecimal("1.234")
val b = BigDecimal("1.234")
val c = BigDecimal("-1.52")
val mc = MathContext(3, RoundingMode.HALF_EVEN)
check(a.fma(b, c, mc) == BigDecimal("0.00276"))
check(a.fma(b, c, mc, DecimalRounding.EACH_STEP) == BigDecimal("0.00"))
check(a.multiply(b, mc).add(c, mc) == BigDecimal("0.00"))
```

The exact product is 1.522756. Final rounding sees the exact sum 0.002756;
step rounding first changes the product to 1.52. Neither policy substitutes for
the other, and final-only does not promise the result of an ordinary rounded
immutable chain.

## Numerical and scale contracts

| Operation | FINAL_RESULT | EACH_STEP |
| --- | --- | --- |
| First plain term | Retain its exact coefficient/scale | Apply `term.round(context)` |
| First product | Exact multiplication | `left.multiply(right, context)`, with no extra rounding of the returned product |
| Later plain term or product | Exact addition, after exact multiplication for products | Add with `current.add(term, context)`; products use `multiply(context)` first |
| Snapshot | Round the exact sum once, then apply the equivalent-cohort publication policy below; retain the exact state for later additions | Return the current value, already rounded at the stated operation points |
| Empty result | BigDecimal.ZERO rounded with context | BigDecimal.ZERO |

The first nonempty term establishes the scale instead of adding to an implicit
scale-zero identity. Thereafter exact addition uses maximum operand scale and
exact multiplication sums operand scales. FINAL_RESULT holds Long scales and
private expanded coefficients, without public integer/decimal publication at
each step. EACH_STEP follows ordinary context-operation range/scale contracts.
Precision zero explicitly means unlimited. Final snapshots still invoke public
`round`, including its canonical scale-zero rule for zero, even under unlimited
precision; the step policy follows the individual operation's zero cohort. The
policies therefore specify representation as well as mathematical rounding.

`fma` accepts a multiplier and addend, plus context, policy and work limit. It
implements the same product/add sequence as this table. `sumDecimals` and
`dotProduct` stream input iterators, without retaining the input collection or a
list of products. Dot product inputs must have equal lengths; differing lengths
throw IllegalArgumentException. Input iterator exceptions propagate. Batch
functions return no partial result on failure.

## Ownership and resource policy

The accumulator is mutable and thread-confined. It is not a BigDecimal subclass.
An ordinary BigDecimal is immutable and may be borrowed; subclass operands are
snapshotted through `unscaledValue()` and `scale()` into an ordinary BigDecimal,
which also applies the existing defensive BigInteger coefficient normalization.
Operand getters may call `snapshot()` to observe the previous state. Mutating
reentry into `add`, `addProduct` or `clear` throws IllegalStateException before
changing state; it cannot bypass the term/work budget or invalidate the outer
transaction. The guard resets in `finally`, including getter failures, and
ordinary getter exceptions retain their identity. Internal per-step product/add
composition uses the private mutation core rather than reentering the public
operation. No arithmetic policy is changed by this guard.

Getters must describe a coherent value during this call; concurrent mutation of
a subclass is not an atomic-snapshot guarantee. Arithmetic uses these observed
values rather than user subclass overrides of add/multiply/round.

For exact equal-scale additions with compact coefficients, a private Long field
holds the running total. Before a+b, the implementation checks positive b against
Long.MAX_VALUE-b and negative b against Long.MIN_VALUE-b. Overflow promotes to the
expanded working path. Long.MIN_VALUE can itself be retained exactly in
this field, and digit counting uses the negative domain so it cannot overflow
by taking its absolute value. Wide values and other scales use private signed
radix-10^9 pages and a Long scale. Step rounding with finite precision uses
ordinary context arithmetic. No mutable magnitude or array escapes in a snapshot. `clear()` reuses
the accumulator and cannot change an earlier snapshot.

`maxTerms` and `maxWorkingDigits` are optional Long budgets, defaulting to null
(no additional budget). Explicit budgets must be positive. No arbitrary decimal
size or term cap is introduced by default; existing supported arithmetic remains
the domain boundary. A Long term counter rejects its own overflow. Explicit
budgets are configurable resource policies for the new API; they do not restrict
old BigDecimal calls. The term count is checked before each plain term/product.
Digit limits use checked Long estimates before arithmetic:

- An input coefficient must fit the digit budget. The stored digit lower bound
  rejects enormous inputs before computing their exact decimal precision.
- A product requires the sum of the two coefficient digit counts to fit. This
  conservatively reserves a possible carry digit.
- Different-scale or wide addition conservatively bounds the aligned span by
  whole working cells plus a carry, at maximum input scale.
- Compact same-scale addition checks its actual result digit count directly.

The estimates may reject a mathematically small final result whose exact
intermediate alignment/product exceeds the budget, including cancellation or a
zero product. They are deliberately work bounds, not approximate arithmetic.
Increasing or omitting the explicit budget permits more work. FINAL_RESULT
accepts expanded intermediates and validates its rounded result for public
representation. `(1, Int.MIN_VALUE) * (1, -1) + (-10, Int.MIN_VALUE)` therefore
returns zero; EACH_STEP retains the ordinary multiply scale overflow. Likewise,
`1E+2147483648 + 1E-2147483647 - 1E+2147483648` can retain its huge intermediate
aligned coefficient privately and return the supported tiny result. These methods neither
silently truncate nor silently switch rounding policy to get past a limit.

ArithmeticException reports a term/work limit, ordinary numerical overflow, or
forbidden rounding. A failed `add`/`addProduct` leaves the previous value and size
unchanged: validation, product and result construction finish before state is
published. Snapshot rounding failures likewise leave the exact state intact.
Invalid configuration and unequal dot lengths use IllegalArgumentException;
other exceptions and resource exhaustion are not converted to numerical values.

## Expanded-domain proof and publication policy

Every private radix cell is strictly between -10^9 and 10^9. Signed remainder and
division preserve that invariant after every update, propagating signed carries
without borrow-filled arrays across empty gaps. Alignment shifts cell indices
and multiplies by at most 10^8. Product terms are split into low/high radix cells
before applying that factor; multiplying a 10^18 product by 10^8 is never done.
All individual carry calculations fit Long. Wide changes operate on private
transactional copies; canceled empty pages are removed before publication.

For supported inputs, coefficient digits are at most H=646456993. Products need
at most 2H digits, product scales lie in [-4294967296,4294967294], the maximum
alignment distance is 8589934590, and a Long number of terms contributes fewer
than 19 decimal carry digits. Working cell indices are consequently below 1.1
billion, within Int addressing. No single array is sized by that span. Physical
work/storage can still be large; an explicit caller budget may reject it.

Snapshot reads the state without changing it. The highest nonzero signed cell
determines the sign, because the absolute sum of all lower cells is strictly
less than one unit at that position. Canonicalization represents borrow through
an empty interval as a compact run of 999999999 cells. Digit lookup, guard,
sticky and rounding carry consult those runs; huge all-nine spans need no dense
materialization merely to round them. FINAL_RESULT makes one rounding decision
on the exact final sum.

Ordinary supported preferred cohorts are retained. When scale or coefficient
range requires an equivalent cohort, publication removes available exact
trailing zeros (as far as Int.MIN_VALUE permits), or appends exact zeros to reach
Int.MIN_VALUE. Zero is canonical ZERO. Mandatory appended zeros may increase
coefficient precision beyond the requested context precision while preserving
the already-rounded numerical value. If no equivalent public representation
exists, ArithmeticException reports the final supported-domain failure.
EACH_STEP retains ordinary public-operation behavior and gains no such rescue.

Coefficient import uses public magnitude bytes and numeric radix conversion;
publication builds binary words and a sign/magnitude byte constructor. No
mandatory decimal String restricts the arithmetic domain. Publication storage
depends on retained final digits, not the alignment span. H decimal digits is
necessary, not sufficient: exact final binary length and the public constructor
still enforce the supported bit range.

Independent source review identified and corrected this counterexample before runtime:
`10001@(Int.MAX_VALUE+2)` rounded to four digits DOWN retains coefficient 1000
at scale Int.MAX_VALUE+1, which can be represented as 100 at Int.MAX_VALUE.
The first draft counted original trailing zeros and incorrectly refused it.
Trailing zeros are now counted in the retained quotient, using sparse-gap-aware
digit lookup. Incremented quotients use their trailing-nine carry count. Tests
retain this witness, both signs, directed/no-rescue modes and carry-created zeros.
The original defect was found by source reasoning, not an executed failure. Its corrected regressions subsequently passed on JVM, JS, Wasm and macOS ARM64.

## Cost and acceptance evidence

The compact equal-scale exact path reuses its Long sum, whereas a conventional
immutable chain constructs intermediate coefficient/decimal results. Conversion
to an immutable BigDecimal is deferred to snapshots or a wide/scaled operation.
This is a structural opportunity for lower allocation, not a claimed speedup.
Large coefficients use expanded arithmetic and can cost more than an ordinary
chain because of radix conversion, transactional page copying and validation. No global
cache, term list or retained history exists; snapshots may retain their immutable
values independently of later accumulator state.

`DecimalAccumulator535Test` prepares literal signed fma rounding witnesses,
cancellation, empty/scale contracts, compact/wide boundary transitions,
snapshot/clear independence, failure atomicity, limits, subclass snapshots and
iterator errors. `DecimalAccumulator535JvmTest` compares deterministic bounded
sum/dot operations against public JDK exact and per-step compositions for all
rounding modes. This is finite evidence, not exhaustive correctness.

Actual focused validation completed on accepted main `38b2642`: JVM 8, JS 7,
Wasm 7 and macOS ARM64 7 methods, 29 total, zero failures/errors/skips. The
39-file pre-runtime manifest remained unchanged through those gates. Tests
include huge sparse cancellation, scale/cohort rescue, retained rounding zeros,
snapshots and mutating getter reentry. This remains finite test evidence plus
the source arguments above.

A same-artifact diagnostic ran on the user-authorized `claws` Linux x86_64 VM,
BellSoft 25.0.4+9-LTS, with G1, 256 MiB initial and 1 GiB maximum heap. A standalone
JVM entry compiled locally from the reviewed main.kts body ran three fresh
serial processes in forward/reverse/forward order. Inputs and the Java adapter
were prepared outside timing; both paths' full result text matched before
sampling. The same candidate artifact contained both ordinary chains and the
new API. Five warmup batches and five measured batches per implementation/case
used a 20 ms target or 8192-operation cap; results escaped through a 256-entry ring.
Medians below pool 15 batch means per implementation, from three processes,
not 15 independent forks. Callable/ring overhead is included. Host CPU/load/steal
snapshots are retained. External process RSS was unavailable; sampled heap-pool
peaks are upper bounds including setup, not exact per-value retained memory.

| Case | Chain ns/op | Accumulator ns/op | Chain B/op | Accumulator B/op | Latency ratio |
| --- | ---: | ---: | ---: | ---: | ---: |
| dot-compact | 14717.0 | 39544.6 | 83624.0 | 385392.0 | 2.687 |
| dot-step-compact | 29898.3 | 35913.5 | 136568.0 | 174504.0 | 1.201 |
| dot-wide | 15649.3 | 268952.0 | 30968.0 | 35288.2 | 17.186 |
| fma-compact | 97.8 | 452.8 | 416.0 | 3272.0 | 4.633 |
| fma-wide | 2676.0 | 21930.5 | 5952.0 | 6080.0 | 8.195 |
| sum-compact | 10787.5 | 1463.3 | 58944.0 | 1800.0 | 0.136 |
| sum-compact-reuse | 10883.2 | 1467.1 | 58944.0 | 1736.0 | 0.135 |
| sum-mixed | 12946.9 | 28525.6 | 62344.0 | 366984.0 | 2.203 |
| sum-wide | 2727.3 | 69782.9 | 11912.0 | 27248.0 | 25.587 |

The measured benefit applies to equal-scale compact sums, whose Long running
state avoids intermediate immutable values. It does not establish a universal
benefit for this API. On these inputs, mixed-scale sums and all measured products
were slower; wide sums were about 25.6x slower and allocated about 2.29x more,
and wide dot products about 17.2x slower. Expanded exact arithmetic imports
binary coefficients into sparse decimal pages, copies transactional state and
converts snapshots back to binary. These are current implementation overheads and optimization opportunities,
not a mathematical lower bound imposed by final rounding. Page cloning on
each wide update, repeated binary-to-decimal input conversion, map/carry work
and schoolbook decimal products can be improved independently while preserving
expanded intermediate-domain semantics and huge-scale cancellation. Callers seeking ordinary rounded-chain behavior should assess the
EACH_STEP policy and their own input distribution; these measurements do not
show that it accelerates dot products. Reusing a compact accumulator did not
improve latency materially over constructing one in this campaign.

All APIs are explicit additions. Existing BigDecimal operations retain their
existing implementation routes; no old call is redirected to this accumulator. The task
requires demonstrated benefit and visible wide-result costs; adverse rows are
retained rather than averaged into a broad speedup claim. No global cache,
known-result shortcut or numerical rounding substitution was introduced.

The reproducible original diagnostic is
`scripts/measure-decimal-accumulator-535.main.kts`. Its standalone JVM wrapper
retains the same adapter, input, comparison, timing and counter body and adds
only portable entry/source binding. Sources, compiled runner/candidate/stdlib,
exact commands, original failed prelaunch setup (missing external time utility),
all 270 samples and independent acceptance are retained in the task evidence.
The setup failure launched no numerical child and supplied no measurements;
no timed campaign was retried. Both latency and allocation data above come from
the one completed same-host campaign, not a local/remote comparison.

The nine diagnostic cases do not time finite-precision EACH_STEP sum or fma;
only the dot case explicitly measures EACH_STEP. The remaining cases use the
policies named in the script, without extrapolation to unmeasured combinations.
Run the script from the repository root with candidate JAR, new output CSV,
forward/reverse order and the independently frozen candidate SHA256. The script
checks adapter loader/origin, the expected artifact digest and unchanged
script/adapter/artifact bytes; raw headers bind all these identities.

An additional source-only counterexample was found before runtime: a subclass
coefficient getter could recursively add a term between the outer budget check
and publication, exceeding `maxTerms=1`. The mutation reentry guard addresses the
general callback path; tests cover nested add/product/clear, both policies,
getter exception identity, guard reset and permitted snapshot observations.
These corrected regressions passed in the 29-method four-target validation above.

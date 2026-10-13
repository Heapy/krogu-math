# JDK 25 conformance audit

Objective: exhaustive compatibility with the public Java SE 25 `java.math` API,
relocated to `io.heapy.krogu.math`, with independent Apache-2.0 common Kotlin
arithmetic. The objective remains **active and unproven**.

Reference runtime: BellSoft Liberica **25.0.4+9**. Implementation source from
OpenJDK or other numerical libraries must not be used. Only public API
specifications and black-box results may supply evidence.

Message-oracle runs and JVM test workers use `-XX:-OmitStackTraceInFastThrow`.
A repeated scaled-zero-division campaign exposed 6,975 null messages caused by
HotSpot exception reuse; rerunning with this flag restored the canonical `/ by zero`
message in every such case. The generator verifies the effective VM option.
This diagnostic configuration follows [Oracle's documented flag](https://www.oracle.com/java/technologies/javase/release-notes-introduction.html)
for disabling preallocated exceptions; numerical values and exception classes
are compared independently of this optimization.

## Requirements and current evidence

| Requirement | Evidence | Remaining audit |
|---|---|---|
| Every public constructor, method and constant of all four types | `ApiSurfaceTest` compares qualified parameter and return types, static/instance modality, finality and field modifiers, after package relocation | An inventory proves shape, not method behavior |
| Java source compatibility, including subclassing and virtual dispatch | Compiled `JavaApiContractTest`, `KotlinSubclassContractTest`; real Java Random constructors | Broader Java call-site, null precedence and subclass behavior audit |
| BigInteger signed arithmetic and exact results | Portable oracle, seeded JVM arithmetic and modular tests | Larger magnitudes, limb-boundary campaigns, algorithm/range limits |
| Infinite two's-complement bit semantics | Portable oracle, seeded JVM operations and algebraic invariants | Structured positive/negative bit boundaries, extreme shift/bit distances |
| All constructor encodings and validation | Portable byte slice/sign matrix; JVM byte and character range matrix; complete BMP digit/radix corpus and supplementary digit constructors | Extreme integer offsets, compound textual grammar, malformed text fuzzing |
| Decimal rounding, precision, preferred scales, comparisons and text | 192,000 structured JVM comparisons; 207,239 exact portable scale-boundary outcomes; portable fixtures | Wider coefficient/scale/precision combinations and overflow across remaining overloads |
| Powers and roots | 10,560 initial JVM comparisons, 13,024 exponent-bit/rounding comparisons, 128 working-precision overflow cases, 2,592 squared-midpoint root fixtures through 129 digits, integer midpoint rounding argument | Sqrt/pow subclass behavior and intermediate/scale-overflow audit; larger exponent/operand combinations |
| Float/Double conversions and canonical Double factory values | Bitwise oracle comparisons, 1,296 exact decimal midpoint-neighbor outcomes, rational rounding argument, all binary scaling powers from -1074 through 1023, 10,000 seeded canonical Double cases | Exhaustive Float input domain, decimal subclass dispatch and very large cancelling coefficient/scale allocation audit |
| Prime generation and certainty | 368 portable primality/successor cases; bounded MAX_VALUE-certainty liveness test; conclusive thirteen-base domain; Carmichael/strong-pseudoprime regressions | Very large certainty beyond the proven bound, witness independence at larger magnitudes, supported-range limits, caller RNG behavior and distribution |
| Exception conditions and types | Portable fixtures; 7,486 live JVM null/invalid-argument comparisons; 3,462 portable null-precedence outcomes; constructor precedence matrix | Wider invalid-argument domains, extreme offsets and cross-products; exception messages remain unmatched |
| Equality, ordering, hash codes, immutability | JVM oracle and common invariants; 6,110 portable BigInteger hash outcomes | Full context/enum/hash and subclass audit |
| JVM serialization and Java adapters | Current-format round trips, validated base state, subclass state, reference identity, cycles and array-alias isolation; exact coefficient/scale conversion | Cross-version serialization and migration to/from java.math streams are explicitly outside scope |
| Same common behavior on every supported platform | CI matrix for JVM, JS, Wasm, macOS/Linux/Windows Native and iOS simulators; cross-built Linux ARM64 binary executed on ARM64 | Each new common regression must pass on every supported target |
| Required numerical range | Overflow guards and selected extreme-scale regressions | Supported-range proof and practical large-input tests remain incomplete |

## Verification commands

```shell
./gradlew jvmTest jsNodeTest wasmJsNodeTest macosArm64Test
export JAVA_TOOL_OPTIONS=-XX:-OmitStackTraceInFastThrow
kotlinr scripts/generate-oracle.main.kts --check
kotlinr scripts/generate-scale-oracle.main.kts --check
kotlinr scripts/generate-null-oracle.main.kts --check
kotlinr scripts/generate-character-oracle.main.kts --check
kotlinr scripts/generate-prime-oracle.main.kts --check
kotlinr scripts/generate-context-oracle.main.kts --check
kotlinr scripts/generate-hash-oracle.main.kts --check
```

Use the appropriate Native host test tasks for other OS/architectures. CI
cross-compiles Linux ARM64 tests on Linux x64 and executes them on ARM64. Device
iOS builds are compiled; simulator targets execute the shared behavioral suite.

`DecimalConformanceTest` defaults to 213,505 comparisons. Its canonical Double
campaign can be extended with
`JAVA_TOOL_OPTIONS=-Dkrogu.campaign.doubles=100000 ./gradlew jvmTest --tests '*doubleCanonicalizationCampaign*'`.
Failure output includes reproducible public inputs. Passing a finite corpus must
not be promoted into a claim that all numerical or API contracts are satisfied.

`ScaleBoundaryOracleTest` compares all 207,239 outcomes individually on every
target. Its separate generator stores complete expected results in JVM-safe
string chunks, not hashes. `ScaleBoundaryConformanceTest` also regenerates each
reference result against the live pinned JDK. The corpus covers compact signed
coefficients, both scale endpoints and their neighbors, zero operands, exact and
context division, addition, multiplication, roots, powers, canonical parsing,
engineering text and trailing-zero removal. Its 68,000-case wide-coefficient
extension includes powers of ten and their immediate neighbors, up to 512-bit
coefficients, precisions around 17/18/19 digits, and very remote signed tails.

`NullArgumentConformanceTest` invokes public Java signatures by reflection so
null reaches the library, not a caller-side Kotlin cast. It compares all public
constructors and reference-argument methods over a bounded invalid-argument
matrix. `NullPrecedenceOracleTest` replays 3,462 cases for the affected contracts
on every target, including zero/negative square roots, invalid text, legacy
rounding constants, sign/magnitude validation and nullable contexts. Public
nullability is relaxed only where the oracle requires a particular validation
order or intentionally ignores a null argument; global parameter assertions
remain enabled.

`CharacterOracleTest` verifies all 2,293,760 BMP-character/valid-radix pairs,
131,072 single-character BigInteger/BigDecimal constructor outcomes, every
valid digit character in every integer radix, and all 390 supplementary digit
code points accepted by JDK 25 `Character.digit(codePoint, 36)`. The generator
checks the complete public-JDK results before losslessly encoding the BMP map
as 41 arithmetic ranges. It distinguishes constructor failures from failures
in test-result conversion. Supplementary UTF-16 constructor behavior is stored
explicitly, rather than inferred from code-point digit classification.

For positive certainty and absolute values strictly below
`3317044064679887385961981`, the first thirteen prime bases give a conclusive
Miller–Rabin decision ([Sorenson–Webster, Theorem 1.1](https://arxiv.org/html/1509.00864v1)).
This removes the billion-round `Int.MAX_VALUE`-certainty path in that domain
without weakening the result. Larger inputs retain the probability-driven
random-witness path; extreme certainty there still needs work. Non-positive
certainty retains the JDK's unconditional `true`. The portable oracle includes
strong pseudoprimes to initial basis subsets, both strict-boundary neighbors,
signed primes through 81 bits, and ordinary-strength 89/127-bit primes.

Binary constructors now pack bytes directly into limbs, including stripped
negative sign extension and a final complement carry. JVM checks compare all
65,536 two-byte encodings and seeded inputs through 65,536 bytes with JDK;
portable regressions include 131,072-byte sign padding and carry boundaries.
Checked sizing rejects magnitudes beyond the supported bit-length range and
avoids Int overflow in division allocation and the initial square-root shift.
Boundary arithmetic is tested without allocating the largest possible arrays;
actual multi-gigabyte execution remains unverified. Modular exponentiation has
a bounded-residue multiplication fallback so an oversized intermediate product
does not alone reject an otherwise representable result. Its arithmetic is
compared with full-precision JDK products on seeded operands through 512 bits.

MathContext string parsing uses a single bounded integer scan rather than
arbitrary-precision arithmetic and repeated parsing. The public-JDK oracle
checks every BMP code unit in single-digit, compound-digit and separator
positions (196,608 outcomes), 1,115 compound grammar/error-precedence cases,
and four precision tokens through one million characters. Common tests compare
the exact exception message and absence of a cause as well as successful
precision and rounding mode. Overflow/invalid syntax produces `bad string
format`; negative precision is rejected with `Digits < 0` only after a valid
rounding mode has been parsed. This closes those MathContext message mismatches;
the broader exception-message audit remains open.

Bit-query and bit-update routines now work directly on stored limbs and
two's-complement words. They do not invoke overridable arithmetic operations
or neighboring public bit methods. Black-box Java subclass fixtures reproduce
the former `andNot`/`setBit` failures and compare the corrected dispatch and
returned base-class type with JDK; common subclass fixtures run on every target.
The main oracle now contains 43,040 vectors, including 8,888 added signed
power/adjacent-value, dense-value, byte/30-bit-limb/32-bit-word boundary and
extreme-shift cases. Negative bit length/count are computed without constructing
`abs(n)-1`. `andNot` complements individual words directly, so it does not
require the potentially unrepresentable separate value `not(operand)`.
The largest supported magnitudes still require the range audit noted above;
these tests do not allocate multi-gigabyte integers or complete the broader
subclass-dispatch audit for arithmetic, shifts and conversions.

Shift routines use private left/right helpers and a private lowest-set-bit
query, so negative distances and negative rounding do not dispatch through
overridden public methods. Java black-box fixtures compare both directions,
signs, zero, 30-bit boundaries and extreme distances with JDK. They also compare
whether the source object is retained and whether the result has the base type.
Zero shifts return canonical ZERO even for a zero-valued subclass; a zero
distance on a nonzero value retains the source. Common subclass regressions
include 120-bit powers and neighboring negative values to exercise discarded-bit
rounding, plus Int.MIN_VALUE/Int.MAX_VALUE distances. The existing structured
oracle independently supplies normal-value results at the extreme distances.
Broader dispatch behavior in arithmetic and conversions remains unverified.

Primitive conversion subclass fixtures compare actual Java call traces with
JDK across zero, signs, precision boundaries and overflow-to-infinity boundaries.
`intValue` reads the low magnitude directly instead of calling overridable
`longValue`. Inherited byte/short conversion still delegates to virtual intValue,
as JDK Number does. Nonzero magnitudes below 2^63 use virtual longValue for
Float/Double; zero and larger magnitudes do not. Larger conversions extract
the mantissa and guard/sticky information from private limbs without abs,
shiftRight, testBit, bitLength or getLowestSetBit overrides. The oracle adds 64
portable subclass cases with replacement Long values, including values just
above Float halfway points that would be double-rounded by a Long→Double→Float
path. Floating numerical-range proof and exhaustive Float-domain verification
remain open. Exact conversion dispatch evidence is described below.

Exact BigInteger conversions now reject oversized magnitudes before consulting
overridden bitLength: the threshold is 32 magnitude bits for int/short/byte
and 64 for long. Within those thresholds they consult bitLength and delegate
to the corresponding virtual primitive conversion. Short/byte exact methods
do not go through intValueExact. Java poison overrides reproduce the previous
failures. An additional 1,248 portable JDK cases cross 26 signed boundary values,
12 replacement bitLength results and all four exact methods; they compare
results, exact error messages, absence of causes and call traces. This includes
both sides of 2^32 and 2^64, so the early-rejection branches are exercised on
every target. The broad arithmetic/conversion subclass audit is still ongoing.

`NUMERICAL_ARGUMENTS.md` derives BigInteger guard/sticky rounding, carry,
finite/infinity thresholds, absence of a second Float rounding, and ordinary
signed exact-conversion range checks. The main oracle adds 1,728 Float/Double
outcomes immediately below, at and above binary midpoints, varying retained
parity, exponent and sign. This strengthens the integer conversion algorithm
audit without closing the BigDecimal/subnormal, exhaustive Float-domain,
compiler/runtime and practical largest-magnitude requirements.

BigInteger hashCode now repacks unsigned 32-bit magnitude chunks directly from
private 30-bit limbs. It does not call overridable abs/testBit and does not
construct intermediate BigIntegers. A Java poison-subclass fixture reproduces
the former failure. A separate portable JDK oracle compares 5,760 signed powers
and neighbors through two 480-bit alignment cycles, plus 350 dense-byte cases
through 65,536 bytes. The common hash fixtures use poison subclasses throughout
and independently encode structured powers as byte arrays. JVM seeded large
binary inputs also compare hash results with JDK. The packing argument is
recorded in `NUMERICAL_ARGUMENTS.md`; broader hash/equality and subclass behavior
for the other public types remains part of the audit.

## Completion gate

Every row above must have direct, current evidence at the required scope and no
known unresolved mismatch. Required API contracts need specification-led tests
and algorithmic reasoning in addition to random examples. Every discovered
common mismatch must have a portable regression and pass the complete target
matrix. Exception messages, serialization details, range guarantees and other
remaining differences cannot silently disappear from the audit or be replaced
by a narrower compatibility claim.

JVM serialization supports round trips of the current implementation, including
subclass state, shared references and cycles. Deserialization validates base-class
state and detaches integer limb arrays. Legacy proxies, older binary formats and
interchange or migration with `java.math` streams are not compatibility requirements.
This exception does not narrow mathematical API compatibility.

Decimal floating conversion now distinguishes compact no-dispatch paths from
general paths that call virtual signum once and bypass virtual precision.
The public-JDK matrix contributes 594 portable result-and-call-trace outcomes,
covering zero, both primitive mantissa boundaries, compact Long limits, scales
around 10/22 and extreme exponents. A separate seeded JVM matrix compares 25,959
ordinary Long-coefficient/scale combinations. BigInteger subclass coefficients
are normalized through virtual toByteArray at construction, as confirmed by a
public-JDK probe; common tests exercise all four coefficient constructors,
replacement encodings, invalid empty encoding and ordinary coefficient identity.
Other decimal virtual calls and compound invalid-argument precedence remain open.

A 735-case portable division matrix combines signed scale endpoints, small
divisors and near-Int.MAX_VALUE precision under HALF_UP, comparing results,
exception types and messages. It initially exposed 408 message differences. The
JVM matrix extends these inputs across all eight modes (5,880 comparisons), all
matching the pinned JDK. Context division now
checks normalization and intermediate range before preferred/result scale, and
uses the observed narrowed-Int sign for normalization error wording. All 735
cases match; the existing 207,239 scale-boundary comparisons also pass. The same
matrix is included in the portable oracle. This is evidence for this domain,
not a complete exception-message audit of every division overload or input domain.

An additional 1,176 exact-division cases initially exposed 523 message differences.
Exact division now reports the pinned JDK's complete nonterminating-result message
for arithmetic failures while constructing the exact result, including scale
failures, after checking division by zero. Zero divided by zero is distinguished
as Division undefined for exact, context and integral quotient families. These
cases and 96 zero-divisor cases across quotient/remainder/pair overloads are in
the portable corpus. Scale-bound division follows different primitive or
BigInteger zero-divisor error paths. A 210,912-case JVM campaign compares compact
boundaries, signed scale endpoints, power-of-ten boundaries and all eight rounding
modes. All cases match; 1,088 representative outcomes are in the portable corpus.
The error classifier preserves the observed rescaling order and Int wrap without
allocating an intermediate numeric value for an operation that must fail. Operand
subclasses and the full nonzero-divisor message domain remain
separate audits.

Scale-bound division adds 12,000 nonzero-divisor outcomes across all modes,
including returned values and exact messages. The initial wide-Long rescaling
rejected some values returned by the pinned JDK and accepted some calls it
rejects. The scale-bound overload now follows the observed checked scale stages
and Int raises, including wraparound. For example, coefficient one at scale
Int.MIN_VALUE divided by ONE with target scale zero returns ONE in the reference
runtime. The fixture preserves that observed endpoint behavior. Context division
uses the shared coefficient-rounding kernel with a separately checked Long raise,
so these scale-bound stages do not change context arithmetic. Existing scale,
context, exact-division and zero-divisor campaigns also pass. Operand subclasses,
wider coefficient domains and resource-limit behavior remain open.

SetScale adds 6,776 value-and-message comparisons spanning both scale endpoints,
18/19-digit rescaling boundaries, compact/inflated coefficients, zero and every
rounding mode. The initial 3,072 differences were diagnostic messages. Both
increase and decrease now validate a positive scale distance before the power-of-ten
range guard, matching the pinned Underflow and integer-range messages. The JVM
matrix and existing scale-boundary tests pass; the same cases are portable fixtures.

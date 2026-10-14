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
| BigInteger signed arithmetic and exact results | Portable oracle; 396 signed pairs through 16,384-bit inputs and 32,768-bit products; carry/borrow and quotient arguments; seeded JVM arithmetic and modular tests | Larger and broader operand patterns, supported-range/allocation limits, diagnostics and subclass behavior |
| Infinite two's-complement bit semantics | Portable oracle, seeded JVM operations and algebraic invariants | Structured positive/negative bit boundaries, extreme shift/bit distances |
| All constructor encodings and validation | Portable byte slice/sign matrix; JVM byte and character range matrix; complete BMP digit/radix corpus and supplementary digit constructors | Extreme integer offsets, compound textual grammar, malformed text fuzzing |
| Decimal rounding, precision, preferred scales, comparisons and text | 192,000 structured JVM comparisons; 207,239 exact portable scale-boundary outcomes; portable fixtures | Wider coefficient/scale/precision combinations and overflow across remaining overloads |
| Powers and roots | 10,560 initial JVM comparisons, 13,024 exponent-bit/rounding comparisons, 128 working-precision overflow cases, 2,592 squared-midpoint root fixtures through 129 digits, integer midpoint rounding argument | Sqrt/pow subclass behavior and intermediate/scale-overflow audit; larger exponent/operand combinations |
| Float/Double conversions and canonical Double factory values | Bitwise oracle comparisons, 1,296 exact decimal midpoint-neighbor outcomes, rational rounding argument, all binary scaling powers from -1074 through 1023, 10,000 seeded canonical Double cases | Exhaustive Float input domain, decimal subclass dispatch and very large cancelling coefficient/scale allocation audit |
| Prime generation and certainty | 416 portable primality/successor/trace cases; bounded MAX_VALUE-certainty liveness test; conclusive thirteen-base domain; Carmichael/strong-pseudoprime regressions; generation density argument and successor whole-search error budget; exhaustive small byte-payload distribution, primality and successor trial-division checks | Very large certainty beyond the proven bound, witness independence at larger magnitudes, supported-range limits, caller RNG behavior and wider subclass dispatch |
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
Prime generation now adds ceil(log2(bitLength)) certainty bits before computing
its random-witness budget, accounting for conditioning on candidate acceptance.
The density bound and explicit RNG assumptions are described in
[NUMERICAL_ARGUMENTS.md](NUMERICAL_ARGUMENTS.md#primality-and-prime-generation).
Successor search assigns 50+bitLength(i) rounds to its ith odd candidate;
the union bound over all candidates stays below the returned-value error limit.
The unbounded index uses BigInteger, and round budgets use Long. Both arguments
still rely on independent uniform witnesses. Portable successor fixtures cover
81..256-bit binary boundaries, the composite gap after 64!+1, source callback
traces and negative-start diagnostics; independent trial division checks every
start from 0 through 4096.

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
The main oracle now contains 75,638 vectors, including 8,888 added signed
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

Decimal exact primitive conversion adds 560 value/message cases. The initial 315
message differences are resolved by long-range/fraction guards before rescaling,
followed by narrow-width checks. Another 72 public-call traces and 27 replacement
longValueExact cases constrain the legacy-Int setScale delegation and virtual
conversion guards. Canonical toString uses stored digit length and private plain
layout, with observed signum dispatch outside scale zero and the nonnegative
small-compact scale-two fast path. Its 264 getter-replacement cases compare text
and calls. Another 336 portable cases cover plain and engineering layouts,
including overridden signum/precision and whether engineering formatting calls
the public plain formatter. Another 150 portable cases compare two successive
calls after changing the subclass signum result, for all three formatters.
Canonical text retains the first successful result;
its cache is volatile and transient on JVM. JVM regressions compare repeated
calls with a changing subclass getter, current-format serialization after a warm
cache, and concurrent readers. Wider subclass callback domains remain open.

Power/root getter dispatch adds 504 portable cases across seven signed/zero/
fractional inputs, three precisions, all eight rounding modes and sqrt/exact pow/
context pow. Exact pow uses stored coefficient zero detection rather than virtual
signum; sqrt reads virtual signum once. The cases compare result text, exact
arithmetic error messages and signum/precision/scale callback traces. Inexact
square roots now retain the JDK message's terminating period. Replacement getter
values, additional overridable methods, exponent domains and resource limits
remain part of the broader power/root audit.

Another 1,320 portable square-root cases replace signum or precision results.
The zero/sign replacement domain spans scales -4 through 20 and all rounding
modes. A stored zero reporting a positive sign follows the nonzero context
dispatch, then returns the preferred zero scale; finite UNNECESSARY precision
also caps that scale at precision minus one. This corrects odd-scale and
high-precision zero layouts without changing the ordinary zero early return.
The 240 precision replacements also retain the pinned JDK's observable behavior
when the reported digit count differs from the stored coefficient's precision.

Exact square roots (unlimited precision or UNNECESSARY) normalize through the
public stripTrailingZeros method. Another 840 portable cases exercise ordinary
normalization and replacement values 9, 0, -4 and 0.04. They compare results,
exception messages and callback traces, including the absence of public
scaleByPowerOfTen/double conversion calls. The normalized value supplies the
coefficient and scale for exact root extraction; the original scale still supplies
the preferred result scale, padded only as far as finite precision allows.
stripTrailingZeros itself checks the stored coefficient for zero and bypasses
virtual signum. The broader replacement-method domain remains open.

Returned normalization subclasses add 1,344 portable cases. Exact-root handling
rejects an odd stored normalized scale before invoking coefficient getters. The
first virtual unscaledValue result selects the coefficient-one shortcut; otherwise
a second call supplies the integer root input. Cases vary the first result across
one, zero, signed nonsquares, ordinary values and a throwing callback, while
tracking the absence of virtual scale/signum/precision calls on the returned object.
Finite exact contexts validate the extracted integer root's digit count before
removing coefficient zeros, matching the pinned rejection of an unnormalized
4.00 replacement at precision one. Extreme normalized scales, additional returned
subclass methods and large-input resource behavior remain open.

A further 2,808 normalized-root cases cross nine coefficients with thirteen
scales (both Int endpoints and neighboring/ordinary values), three precisions
and all rounding modes. They distinguish the normalized zero's root scale from
the original preferred scale and cover trailing-zero integer roots at MIN_VALUE.
The final zero scale now uses the normalized root scale when applying finite
precision, then adjusts toward the original preferred scale. An extra check that
renormalized the extracted root before doubling its scale has been removed:
the reference accepts the unnormalized 100/400 coefficient replacements at
MIN_VALUE. Ordinary input normalization still validates its own scale. The matrix
uses bounded coefficients and matching source/replacement scales to avoid large
power-of-ten allocations; it does not establish resource behavior for distant
source/replacement scales.
For the rounded branches, source coefficient four has adjusted exponent -s;
the scaling shift is 2p-2 or 2p-1. With tested positive precisions one and seven,
the largest constructed decimal power is therefore 10^13, even at Int scale
endpoints. Exact branches use only the small supplied integer coefficients.

Exact decimal powers add 686 portable value/message/callback cases across seven
coefficients, seven scales, seven exponents and direct/unlimited-context overloads.
After exponent validation and the exponent-zero shortcut, scale multiplication is
validated before coefficient exponentiation. This corrects 16 cases where a
coefficient-range error masked the pinned Overflow/Underflow scale error, and
avoids coefficient growth for an invalid result scale. The same matrix confirms
zero-scale saturation and virtual pow delegation from the unlimited overload.
Another 45 direct BigInteger power cases constrain negative exponents, zero
exponents, zero and signed unit values, and supported-range failures. The power range
guard now reports `BigInteger would overflow supported range`, matching 24
additional decimal-message differences. Large exponents in this corpus either
retain a zero/unit coefficient or violate the bit-length lower bound before
allocation; it does not test feasible multi-gigabyte powers or certify the full
range/exception-message audit.

Point movement adds 3,402 portable cases across six coefficients (including zero,
both signs and an inflated value), seven scales, nine signed distances, three
replacement signs and all three movement methods. Scale overflow and zero
saturation inspect the stored coefficient, bypassing virtual signum/precision.
Zero-distance left/right movement preserves the source object only when its
scale is nonnegative; a negative scale is normalized to zero. scaleByPowerOfTen
still returns a base instance. Result text, exact errors, source identity, base
type and public call traces are compared with JDK, including MIN_VALUE distances
and the absence of delegation to neighboring public movement methods.
Selected negative result scales are either within four places of zero or beyond
the supported power-of-ten range, so the corpus avoids large successful power
allocations. Wider distances, caller-overridden result inspectors and practical
large-input allocation behavior remain open.

Multiplication adds 5,400 portable cases with replacement source getters, signed/
zero/inflated coefficients, both scale endpoints, direct and context overloads,
and HALF_EVEN/UNNECESSARY rounding. Scale validation checks the stored left
coefficient. Finite contexts use the private multiplication path; only unlimited
contexts delegate through public multiply. Exact errors, call traces, source
identity and result base type are included. A separate 1,152-case matrix compares
round and the BigInteger/scale/context constructor across all rounding modes,
carry-producing coefficients and scale neighbors. Rounding validates the reduced
scale before coefficient division and uses the sign of its narrowed Int for the
pinned overflow diagnostic, fixing 108 multiplication outcomes where Underflow
was replaced by Overflow or Rounding necessary. Public round getter replacements,
other constructor overloads, right-operand subclasses and larger products remain
open. Products here have at most 22 digits, keeping precision-reduction powers
small even at extreme scales.

Public round/plus dispatch adds 1,920 portable cases across zero, negative,
carry-producing and inflated values, five replacement precisions, three context
precisions and all rounding modes. round delegates to virtual plus even for an
unlimited context; plus performs the private rounding calculation. Replacement
plus results and callback exceptions propagate directly. Each rounding step
reads precision once, so a changing subclass getter cannot supply a second
value for the same reduction. Cases compare stored numerical state, identity,
base type, errors and call traces. These close the selected getter-replacement
domain noted above; extreme replacement
precisions, other unary operations and returned-subclass combinations remain open.

Null round/plus callbacks add 16 portable outcomes and a compiled Java call-site
test. round forwards a null context to overridden plus, whose replacement value,
source return or callback exception is preserved; the base plus validates null
before its private calculation. Kotlin signatures now use MathContext? for both
methods, including subclass overrides. The Kotlin plus(BigDecimal) operator is
JvmSynthetic so Java plus(null) resolves to the JDK-compatible MathContext
overload instead of failing as ambiguous. The cases compare NPE classes, callback
exception messages and traces; detailed NPE diagnostics remain unverified.

Unary operations add 600 portable outcomes: 432 result/identity/type/trace cases,
96 returned-negation callback cases across all rounding modes and null/ordinary
contexts, and 72 null-context callback cases also exercised from Java. abs(context)
reads virtual signum, then calls virtual negate(context) or plus(context) directly.
negate(context) calls plus(context) on the object returned by virtual negate(),
preserving replacement results and exceptions instead of invoking that object's
round override. The selected matrices exposed 72 dispatch differences in each
of these two chains. abs/negate context signatures now use MathContext? to forward
null to callbacks before base validation. Kotlin overrides use the nullable
parameter too. Broader returned-subclass combinations, extreme unary scales and
detailed NPE messages remain open.

Addition/subtraction routing adds 747 portable cases: 648 subtraction callback
cross-products, 27 exact-add sign replacements and 72 finite additions with scale
gaps through 20,002. Exact subtraction aligns stored coefficients directly,
bypassing source add and right-operand negate overrides. Unlimited context
subtraction delegates to public subtract; finite context subtraction preserves
virtual right negate followed by left add(context). Finite addition's ordinary
and remote-tail paths use private exact addition. Exact alignment checks stored
zero coefficients, so a replacement signum cannot suppress decimal scaling.
Replacement values, throwing callbacks, exact errors and call traces are compared
with JDK; the original subtraction matrix exposed 364 differences. The selected
JVM checks also rerun the numerical decimal, scale and null campaigns. Null
callback operands, broader getter replacement domains and full range/allocation
behavior remain open.

Min/max delegation adds 204 portable cases: 120 custom-comparison outcomes and
84 ordinary/null-base outcomes. Both methods invoke virtual compareTo once,
including null operands, preserve source identity on ties and can return the
selected null operand when a subclass comparison permits it. Callback exceptions
propagate before argument rejection. compareTo accepts a nullable operand while
its base implementation still rejects null. The common class implements
Comparable<BigDecimal?>; Java Comparable<BigDecimal> assignment and collection
sorting are compiled and exercised. Kotlin member min/max now have nullable
return types. Ordinary Kotlin sorted/minOf/maxOf remain usable with non-null
result types, as checked in common tests. Source/operand identities, callback
traces, selected null results and NPE classes match JDK in this domain. Broader
callback compositions, returned subclasses and detailed NPE messages remain open.

BigInteger min/max add 498 portable cases: 168 custom-comparison outcomes and
330 ordinary min/max/compareTo outcomes, including null and signed 30/63/64-bit
boundaries. Both integer extrema pass the operand to virtual
compareTo before base null validation, select the operand on ties, propagate
callback exceptions and can select null when the override permits it. Comparator
results include Int.MIN_VALUE and Int.MAX_VALUE. The common class now implements
Comparable<BigInteger?>, with nullable compareTo arguments and member min/max
return types; Kotlin subclass overrides must accept nullable comparison arguments.
The base comparison still rejects null. Java Comparable<BigInteger>, Java
collection sorting, erased bridge dispatch/type checks and non-null Kotlin
sorted/minOf/maxOf results are compiled and tested. Public JVM method descriptors
remain unchanged. These cases constrain result identity and exception classes;
detailed null messages and broader callback compositions remain open.

The initial quotient magnitude comparison now reads stored limbs directly rather
than invoking public compareTo. An overridden comparison no longer changes
division or leaks a callback into integer formatting. Another 318 portable JDK
outcomes cover divide/remainder/pairs with both divisor signs and one-/multi-limb
magnitudes, plus every radix 2..36 for signed/zero inputs, while source compareTo
throws if called. These cases also constrain formatting of a selected min/max
source. Abs/division/formatting overrides beyond this comparison callback remain
a separate audit.

Integer subtraction adds 896 portable dispatch cases. A stored zero operand
returns the source without callbacks; only a stored zero source and nonzero
operand delegate to operand.negate. Other paths use stored signed magnitudes,
bypassing source.add and public negation. Fixtures cover both signs, zero,
30-bit boundaries and larger values, distinct and self operands, callback
replacement objects and exceptions, result identity and call order. Observation
captures the trace before formatting and uses known values for returned source
or operand objects, so unrelated formatting overrides do not alter the measured
subtraction trace. Another 196 Java null/throw combinations and three null-operand
checks constrain direct null forwarding and argument rejection. The existing
396 wide ordinary arithmetic pairs remain the numerical regression domain;
broader callback compositions, range failures and detailed NPE messages remain open.

Exact alignment errors add 882 portable add/subtract outcomes over signed unit
and zero coefficients and seven scales spanning both Int endpoints. The matrix
exposed 384 diagnostic differences. A nonzero coefficient's positive alignment
distance is validated as an Int before the power-of-ten range check; oversized
distances report Underflow. The positive power range guard now uses the JDK
message `BigInteger would overflow supported range`. Zero coefficients bypass
unnecessary scaling. Selected distances are small or far beyond supported range,
so this campaign does not allocate near-limit integers. The broader range and
exception-message audit remains open.

Nullable binary delegation adds 288 portable outcomes and a compiled Java matrix
for add/subtract/multiply, direct and context overloads, null/ordinary operands,
null/unlimited/finite contexts and four exact-callback behaviors. Unlimited
contexts forward the operand to the public exact override before operand
validation. Finite contexts validate before private calculation. Context-null
controls reject without invoking the exact callback. Binary operand and context
parameters now use nullable Kotlin types; subclass overrides use them too.
Base numeric behavior, callback replacements, callback exception messages and
traces are compared. NPE classes are checked separately from their still-open
detailed diagnostic audit. More callback compositions remain open.

Java returned-null callback checks add 94 cases to the three null-context/operand
matrices (470 JVM cases total). The observer uses String.valueOf so a returned
null is distinguished from an NPE thrown by the library. The added domain covers
58 forwarded null results and 36 controls where null/finite contexts prevent
exact-callback delegation. Round/plus, abs/negate and unlimited binary overloads
match the pinned JDK in these Java-only interop cases without another arithmetic
change. Other runtime interop surfaces, chained returned-null callbacks and
detailed NPE diagnostics remain open.

Comparison adds 8,215 portable outcomes: 7,425 getter/sign combinations, 225
ordinary text pairs, 400 extreme-scale pairs and 165 self comparisons with
changing sign getters. Same-scale compact operands compare stored coefficients
without getters. General paths read each virtual sign once; stored-zero and
same-scale magnitude paths bypass precision, while different-scale nonzero
magnitudes read virtual precision. Scale getters are bypassed. Magnitude string
comparison now normalizes its result to -1/0/1, correcting ordinary results such
as 1 versus 9.0 returning -8 instead of the pinned -1. Sign direction uses positive
versus negative branching, including the selected replacement signs +/-2.
Long boundaries and adjacent inflated values constrain compact-path selection.
The numerical order argument assumes truthful getter metadata; broader dynamic
getter domains, null comparison/min/max callbacks and large-input allocation
behavior remain open.

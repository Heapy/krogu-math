# JDK 25 conformance audit

Objective: exhaustive compatibility with the public Java SE 25 `java.math` API,
relocated to `io.heapy.krogu.math`, with independent Apache-2.0 common Kotlin
arithmetic. The objective remains **active and unproven**.

Reference runtime: BellSoft Liberica **25.0.4+9**. Implementation source from
OpenJDK or other numerical libraries must not be used. Only public API
specifications and black-box results may supply evidence.

## Requirements and current evidence

| Requirement | Evidence | Remaining audit |
|---|---|---|
| Every public constructor, method and constant of all four types | `ApiSurfaceTest` compares qualified parameter and return types, static/instance modality, finality and field modifiers, after package relocation | An inventory proves shape, not method behavior |
| Java source compatibility, including subclassing and virtual dispatch | Compiled `JavaApiContractTest`, `KotlinSubclassContractTest`; real Java Random constructors | Broader Java call-site, null precedence and subclass behavior audit |
| BigInteger signed arithmetic and exact results | Portable oracle, seeded JVM arithmetic and modular tests | Larger magnitudes, limb-boundary campaigns, algorithm/range limits |
| Infinite two's-complement bit semantics | Portable oracle, seeded JVM operations and algebraic invariants | Structured positive/negative bit boundaries, extreme shift/bit distances |
| All constructor encodings and validation | Portable byte slice/sign matrix; JVM byte and character range matrix; complete BMP digit/radix corpus and supplementary digit constructors | Extreme integer offsets, compound textual grammar, malformed text fuzzing |
| Decimal rounding, precision, preferred scales, comparisons and text | 192,000 structured JVM comparisons; 207,239 exact portable scale-boundary outcomes; portable fixtures | Wider coefficient/scale/precision combinations and overflow across remaining overloads |
| Powers and roots | 10,560 structured JVM comparisons plus portable fixtures | Adversarial midpoint and carry cases at larger precision |
| Float/Double conversions and canonical Double factory values | Bitwise oracle comparisons, binary boundaries, 10,000 seeded canonical Double cases | Exhaustive Float input domain, wider subnormal/tie/overflow campaigns and algorithm audit |
| Prime generation and certainty | 368 portable primality/successor cases; bounded MAX_VALUE-certainty liveness test; conclusive thirteen-base domain; Carmichael/strong-pseudoprime regressions | Very large certainty beyond the proven bound, witness independence at larger magnitudes, supported-range limits, caller RNG behavior and distribution |
| Exception conditions and types | Portable fixtures; 7,486 live JVM null/invalid-argument comparisons; 3,462 portable null-precedence outcomes; constructor precedence matrix | Wider invalid-argument domains, extreme offsets and cross-products; exception messages remain unmatched |
| Equality, ordering, hash codes, immutability | JVM oracle and common invariants | Full context/enum/hash and subclass audit |
| JVM serialization and Java adapters | Validated proxies; subclass round trips, invalid fields, missing-superclass-data streams and array-alias isolation; explicit JDK serialVersionUID values; exact coefficient/scale conversion | Descriptor/field-schema compatibility with JDK, stream-version migration and broader malformed-stream audit |
| Same common behavior on every supported platform | CI matrix for JVM, JS, Wasm, macOS/Linux/Windows Native and iOS simulators; cross-built Linux ARM64 binary executed on ARM64 | Each new common regression must pass on every supported target |
| Required numerical range | Overflow guards and selected extreme-scale regressions | Supported-range proof and practical large-input tests remain incomplete |

## Verification commands

```shell
./gradlew jvmTest jsNodeTest wasmJsNodeTest macosArm64Test
kotlinr scripts/generate-oracle.main.kts --check
kotlinr scripts/generate-scale-oracle.main.kts --check
kotlinr scripts/generate-null-oracle.main.kts --check
kotlinr scripts/generate-character-oracle.main.kts --check
kotlinr scripts/generate-prime-oracle.main.kts --check
kotlinr scripts/generate-context-oracle.main.kts --check
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

JVM deserialization now validates base-class state even when a subclass bypasses
the private serialization proxy. Invalid signs, null or noncanonical limbs,
out-of-range limbs and null decimal coefficients are rejected during reading.
Restored limb arrays are copied so another stream object cannot retain a mutable
alias to integer storage. Invalid proxy fields produce `InvalidObjectException`
with the constructor failure as cause. Tests retain subclass type and extra
fields, repeated references and cycles, and compare valid subclass round trips
with JDK. The three serialVersionUID constants match the published JDK values;
proxy UIDs remain 1. This does **not** establish JDK stream-format compatibility:
the library uses its own package, proxy classes and subclass field schema.
Private `readObjectNoData` hooks reject missing base data. A hierarchy evolution
test writes a child without a math superclass, then reads it as a same-name,
same-UID child that extends BigInteger or BigDecimal. Both the port and JDK
reject those actual streams with `InvalidObjectException`. Existing snapshot streams
that used computed base-class UIDs have no migration guarantee yet.

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
The main oracle now contains 13,519 vectors, including 8,888 added signed
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

## Completion gate

Every row above must have direct, current evidence at the required scope and no
known unresolved mismatch. Required API contracts need specification-led tests
and algorithmic reasoning in addition to random examples. Every discovered
common mismatch must have a portable regression and pass the complete target
matrix. Exception messages, serialization details, range guarantees and other
remaining differences cannot silently disappear from the audit or be replaced
by a narrower compatibility claim.

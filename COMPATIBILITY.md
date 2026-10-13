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
| All constructor encodings and validation | Portable byte slice/sign matrix; JVM byte and character range matrix | Extreme integer offsets, every radix, Unicode character corpus, malformed text fuzzing |
| Decimal rounding, precision, preferred scales, comparisons and text | 192,000 structured JVM comparisons; 139,239 exact portable scale-boundary outcomes; portable fixtures | Larger coefficients, wider scale/precision combinations and overflow across remaining overloads |
| Powers and roots | 10,560 structured JVM comparisons plus portable fixtures | Adversarial midpoint and carry cases at larger precision |
| Float/Double conversions and canonical Double factory values | Bitwise oracle comparisons, binary boundaries, 10,000 seeded canonical Double cases | Exhaustive Float input domain, wider subnormal/tie/overflow campaigns and algorithm audit |
| Prime generation and certainty | Seeded generation, external primality checks, Carmichael/composite regressions | Witness independence, certainty/range boundaries, caller RNG behavior and distribution |
| Exception conditions and types | Portable fixtures, dynamic JVM oracle, constructor precedence matrix | Full cross-product of invalid arguments and null precedence; exception messages remain unmatched |
| Equality, ordering, hash codes, immutability | JVM oracle and common invariants | Full context/enum/hash and subclass audit |
| JVM serialization and Java adapters | Value round trips using validated proxies; exact coefficient/scale conversion | Malformed streams, subclass streams, descriptor/version policy and Java serialization compatibility audit |
| Same common behavior on every supported platform | CI matrix for JVM, JS, Wasm, macOS/Linux/Windows Native and iOS simulators; cross-built Linux ARM64 binary executed on ARM64 | Each new common regression must pass on every supported target |
| Required numerical range | Overflow guards and selected extreme-scale regressions | Supported-range proof and practical large-input tests remain incomplete |

## Verification commands

```shell
./gradlew jvmTest jsNodeTest wasmJsNodeTest macosArm64Test
kotlinr scripts/generate-oracle.main.kts --check
kotlinr scripts/generate-scale-oracle.main.kts --check
```

Use the appropriate Native host test tasks for other OS/architectures. CI
cross-compiles Linux ARM64 tests on Linux x64 and executes them on ARM64. Device
iOS builds are compiled; simulator targets execute the shared behavioral suite.

`DecimalConformanceTest` defaults to 213,505 comparisons. Its canonical Double
campaign can be extended with
`JAVA_TOOL_OPTIONS=-Dkrogu.campaign.doubles=100000 ./gradlew jvmTest --tests '*doubleCanonicalizationCampaign*'`.
Failure output includes reproducible public inputs. Passing a finite corpus must
not be promoted into a claim that all numerical or API contracts are satisfied.

`ScaleBoundaryOracleTest` compares all 139,239 outcomes individually on every
target. Its separate generator stores complete expected results in JVM-safe
string chunks, not hashes. `ScaleBoundaryConformanceTest` also regenerates each
reference result against the live pinned JDK. The corpus covers compact signed
coefficients, both scale endpoints and their neighbors, zero operands, exact and
context division, addition, multiplication, roots, powers, canonical parsing,
engineering text and trailing-zero removal.

## Completion gate

Every row above must have direct, current evidence at the required scope and no
known unresolved mismatch. Required API contracts need specification-led tests
and algorithmic reasoning in addition to random examples. Every discovered
common mismatch must have a portable regression and pass the complete target
matrix. Exception messages, serialization details, range guarantees and other
remaining differences cannot silently disappear from the audit or be replaced
by a narrower compatibility claim.

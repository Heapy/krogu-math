# krogu-math

Independent Kotlin Multiplatform arithmetic targeting the Java SE 25 `java.math`
numerical contract and Kotlin source migration, in the package
`io.heapy.krogu.math`. Licensed under Apache 2.0.

`BigInteger`, `BigDecimal`, `MathContext` and `RoundingMode` perform all arithmetic
in **common Kotlin**, including on JVM. There are no runtime dependencies other
than the Kotlin standard library and no delegation to `java.math`.

```kotlin
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.MathContext

val integer = BigInteger("123456789012345678901234567890")
val root = integer.sqrt()
val price = BigDecimal("19.95").multiply(BigDecimal("1.20")) // 23.9400
val third = BigDecimal.ONE.divide(BigDecimal(3), MathContext.DECIMAL128)
```

For integral and String conversions, explicitly import krogu's `toBigInteger`,
`toBigDecimal` and their nullable counterparts alongside its types; see
[Kotlin conversion imports](docs/kotlin-conversion-extensions.md).
For increment/decrement, shifts, complements and named or aliased bitwise calls,
see [Kotlin operator migration](docs/kotlin-operator-migration.md).
Float/Double conversions use the same explicit `toBigDecimal` import; see
[floating conversion semantics](docs/kotlin-floating-conversion-migration.md).

This is an initial implementation (`0.1.0-SNAPSHOT`), not a published Maven Central
release. To consume locally, run `./gradlew publishToMavenLocal`, add `mavenLocal()`
to your repositories, and depend on `io.heapy.krogu:krogu-math:0.1.0-SNAPSHOT`.

## Implementation

* BigInteger: immutable sign and little-endian base-2^30 limbs; addition,
  schoolbook multiplication, normalized limb division, arithmetic shifts, infinite
  two's-complement bit operations, extended Euclidean inverse, modular powers,
  integer square roots, Miller–Rabin primality tests and prime generation.
* BigDecimal: an integer coefficient and signed 32-bit decimal scale; exact and
  rounded arithmetic, all eight rounding modes, preferred scales, integral
  division, powers, square roots and decimal formatting.
* Floating-point conversions use exact integer ratios and round to nearest,
  ties to even, with subnormal values and overflow. The Double constructor
  preserves the exact binary value; `valueOf(Double)` uses a shortest decimal
  representation compatible with Java's notation conventions.
* Java-style methods and constants coexist with Kotlin arithmetic operators.
  BigDecimal equality includes scale; comparison is numerical.

The code was written independently against the
[Java SE 25 API specification](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/package-summary.html).
OpenJDK and third-party big-number implementation sources are not incorporated.
The oracle uses only public JDK methods and their observable results.

Probable-prime generation and successor search support magnitudes of at most
500,000,000 bits. Larger requests throw `ArithmeticException`; a successor
search also stops if its next candidate crosses that boundary. This chosen
limit preserves the Java API's required prime-value range through
`2^500000000`. It does not cap other integer operations or `isProbablePrime`,
or guarantee memory or running time at the limit. See the
[range argument and validation](docs/prime-search-range-559-proof.md).

## Platforms and verification

Gradle uses a 4 GiB heap and one worker to limit memory pressure from the
in-process compilers when compiling the multiplatform oracle suites.

Build pins: **Gradle 9.8.1**, **Kotlin 2.4.21**, **BellSoft Liberica JDK 25.0.4+9**.
The Gradle distribution checksum and CI action revisions are pinned.
The [latest local normalization verification](docs/decimal-division-normalization-594-verification.md)
records 1,757 passing methods on JVM, JS, Wasm and macOS ARM64, with the complete
94,282-record sqrt ledger preserved. The preceding published source253 has
[5,812 passing supported-platform executions](docs/source253-hosted-verification.md)
across 13 target instances and 25 oracle checks, plus
[106 normal and 12 large public-test PASS records](docs/source253-public-math-verification-559.md).
Each report retains its source/artifact binding and limits; these results do not
establish official JCK certification or full compatibility. The later source255
publication needs its own matching hosted/platform/public artifact acceptance.

The current [range/allocation ledger](docs/supported-range-allocation-548-ledger.md),
[decimal overload binding](docs/decimal-overload-541-current-binding.md),
[constructor/API binding](docs/api-constructor-549-current-binding.md) and
[oracle contract boundary](docs/oracle-contract-boundary-526.md) distinguish
required behavior, conditional proof premises and retained research questions.
The Kotlin/JS Number migration issue remains an unsatisfied caller contract;
successful numerical campaigns do not waive it.

| Platform | Target | Verification |
|---|---|---|
| JVM | `jvm` | Portable fixtures, live differential tests, API inventory, serialization |
| JavaScript | `js` | Portable fixtures on Node.js |
| WebAssembly | `wasmJs` | Portable fixtures on Node.js |
| macOS | `macosArm64`, `macosX64` | Portable fixtures on matching CI hosts |
| Linux | `linuxX64`, `linuxArm64` | Portable fixtures on matching CI hosts |
| Windows | `mingwX64` | Portable fixtures on Windows CI |
| iOS | `iosSimulatorArm64`, `iosX64` | Portable fixtures in simulators |
| iOS device | `iosArm64` | Compilation in CI; the common implementation is exercised in simulators |

```shell
./gradlew jvmTest jsNodeTest wasmJsNodeTest macosArm64Test
```

Use the native test task appropriate to your host (`linuxX64Test`, `mingwX64Test`,
etc.). Native tests cannot execute on a different host OS or CPU architecture.
Kotlin/Native 2.4.21 does not provide a Linux ARM64 compiler host: CI cross-compiles
the ARM64 test executable on Linux x64 and then runs it on a Linux ARM64 runner.

The committed 75,638 portable vectors compare values, preferred scales, text,
floating-point bits and exception **types** on every test target.
The vectors include 8,888 structured/dense bit-query, bit-update and extreme
shift cases. Portable subclass tests also check that inherited bit operations
use the stored value independently of neighboring public overrides.
Exact conversions add 1,248 subclass cases checking range-guard order,
delegation, exact error messages and call traces against JDK.
Another 1,728 Float/Double midpoint-neighbor outcomes exercise rounding parity,
sticky bits and finite/infinity edges; see [numerical arguments](NUMERICAL_ARGUMENTS.md).
Separate hash fixtures add 6,110 JDK outcomes, including complete 30/32-bit
alignment cycles and signed dense inputs through 65,536 bytes.
Separate signed arithmetic fixtures cover 396 operand pairs through 16,384-bit
inputs and 32,768-bit products, including digit carry chains, dense/sparse
patterns, both signs and quotient/remainder invariants.
Subtraction dispatch adds 896 portable cases for zero paths, replacement callback
values, exceptions and aliased operands; Java tests also cover null callback results.
String-constructor fixtures add 10,156 value and exact-diagnostic cases across all
radices, invalid radix precedence, signs, Unicode digits and digit-group boundaries.
Byte constructors add 25,584 outcomes for signed magnitudes, two's-complement
slices and exact range diagnostics; successful cases also check array isolation.
Division adds 3,216 callback/value/identity cases for subclassed operands, aliased
operands and wide dividends; Java tests add nullable callback and operand cases.
Integer powers add 6,677 callback/value/identity cases, including exponent endpoints,
factor-of-two extraction, compact arithmetic and replacement metadata.
GCD adds 1,602 callback/value/identity outcomes, including zero routing, self aliases,
shared wide factors and consecutive Fibonacci inputs; Java tests cover nullable callbacks.
Integer roots add 978 value/callback/identity outcomes and independent square/remainder
bounds, including exact-square neighbors through 8,193-bit inputs.
Bit operations add 17,198 value/callback/identity outcomes, including virtual
metadata, self aliases, signed boundaries through 4,097 bits and fresh zero results.
Modular reduction and inversion add 11,798 outcomes for conditional callbacks,
replacement results, equality shortcuts, identities and moduli through 1,025 bits.
Modular exponentiation adds 9,176 value/error/identity/callback outcomes, including
callback arguments, even/odd moduli, negative powers and replacement metadata.
Primality prefixes add 78 deterministic outcomes for certainty endpoints,
early returns and propagation of virtual bitLength failures before witness tests.
Decimal quotient/remainder composition adds 1,440 outcomes for overload routing,
replacement and throwing callbacks, returned identities, values and scales.
Inherited decimal integral division adds 1,400 outcomes for context-divide callbacks,
preferred scales, precision, residual checks and returned subclass identities.
It also adds 1,176 signed metadata outcomes, including precision and scale endpoints.
Context division adds 75,728 outcomes for operand callback order, failure precedence,
all rounding modes, compact/wide coefficients, shared operands, nullable calls,
replacement precision/signum values, compact equality-path boundaries and
normalization overflow near Long limits.
Java tests add 3,546 nullable division-family comparisons, including callback results.
Another 216 portable cases check remainder delegation to replacement pair arrays,
including bounds failures and returned element identity.
JVM tests add seeded random arithmetic, negative bit operations, modular arithmetic, primes,
rounding, scale boundaries, serialization and API surface checks. Structured JVM
campaigns add over 213,000 comparisons, including 10,000 canonical Double values.
An additional 207,239 structured scale-boundary cases compare full expected
results on every target, including errors at both signed scale limits.
Null/error precedence adds 7,486 live JVM comparisons and 3,462 portable outcomes.
Character tests cover every BMP character in bases 2–36, both single-character
string constructors, and all supplementary digit characters classified by JDK 25.
MathContext grammar tests add 196,608 BMP-position outcomes, 1,115 compound
cases with exact error messages, and precision tokens up to a million characters.
Primality tests include maximum certainty in a proven deterministic domain,
strong pseudoprimes and the strict domain boundary.
Java sources are compiled and executed to check constructors, static factories,
subclassing, method hiding and virtual Number dispatch. Fixtures are
generated reproducibly with a dependency-free Kotlin script:

```shell
# Requires JDK 25.0.4+9 and Kotlin 2.4.21 (kotlinr).
export JAVA_TOOL_OPTIONS=-XX:-OmitStackTraceInFastThrow
kotlinr scripts/generate-oracle.main.kts
kotlinr scripts/generate-oracle.main.kts --check
kotlinr scripts/generate-scale-oracle.main.kts --check
kotlinr scripts/generate-null-oracle.main.kts --check
kotlinr scripts/generate-character-oracle.main.kts --check
kotlinr scripts/generate-prime-oracle.main.kts --check
kotlinr scripts/generate-prime-prefix-oracle.main.kts --check
kotlinr scripts/generate-decimal-pair-dispatch-oracle.main.kts --check
kotlinr scripts/generate-decimal-integral-dispatch-oracle.main.kts --check
kotlinr scripts/generate-decimal-context-dispatch-oracle.main.kts --check
kotlinr scripts/generate-integer-arithmetic-oracle.main.kts --check
kotlinr scripts/generate-integer-parser-oracle.main.kts --check
kotlinr scripts/generate-integer-bytes-oracle.main.kts --check
kotlinr scripts/generate-integer-division-oracle.main.kts --check
kotlinr scripts/generate-integer-pow-oracle.main.kts --check
kotlinr scripts/generate-integer-gcd-oracle.main.kts --check
kotlinr scripts/generate-integer-sqrt-oracle.main.kts --check
kotlinr scripts/generate-integer-bits-oracle.main.kts --check
kotlinr scripts/generate-integer-inverse-oracle.main.kts --check
kotlinr scripts/generate-integer-modpow-oracle.main.kts --check
kotlinr scripts/generate-context-oracle.main.kts --check
kotlinr scripts/generate-hash-oracle.main.kts --check
```

The first script invocation compiles the script; later runs use the compiler cache.
You may also `chmod +x scripts/generate-oracle.main.kts` and execute it directly.
Generated fixtures contain public inputs and observable outputs, not JDK code.
Oracle and JVM test processes disable HotSpot's preallocated implicit exceptions
so diagnostic messages remain reproducible after repeated failures.

## JVM integration

Common factory functions and prime factories accept `kotlin.random.Random`, using
constructor call syntax. JVM has actual constructors and static prime factories
accepting `java.util.Random`:

```kotlin
import io.heapy.krogu.math.*

val n = BigInteger(256, java.util.Random(42))
val javaValue: java.math.BigInteger = n.toJavaBigInteger()
val commonValue = javaValue.toKroguBigInteger()
```

Java callers can use `new BigInteger(bits, random)`,
`new BigInteger(bits, certainty, random)` and
`BigInteger.probablePrime(bits, random)` directly. The `JavaMath` factory helpers
remain available. Java can use ordinary constructors, static constants and static
`valueOf` methods. Conversion helpers for integers,
decimals and contexts preserve coefficient, scale and rounding policy.

The three value classes implement Java `Serializable` on JVM and validate restored
state. The serialized form is private to the current krogu-math implementation.
Compatibility with older krogu-math streams or `java.math` streams, and migration
between these formats, are outside the project requirements. Non-JVM platforms
do not depend on Java serialization.

Task573's input migration covers all 48 reviewed slots:
`BigInteger`/`BigDecimal` comparison inputs; exact and MathContext overloads of
decimal add, subtract, multiply, divide, integral division, pairs and remainder;
decimal abs, negate, plus and round contexts; integer bitwise operands; and the
reviewed constructor, random, modPow, legacy-divide and sqrt inputs. Kotlin overrides use matching
non-null types, and both numbers implement `Comparable<T>`. Task525 additionally
makes the four member `min`/`max` results non-null. Three equals inputs and four
OrNull results remain nullable.
Valid common mathematical coverage remains; ill-typed null calls have actual
JVM Java counterparts with separate direct override, inherited and explicit-super
contracts. The revision4 source passed all four local platform gates (1,416 method
executions across JVM, JS, Wasm JS and macOS ARM64) and all 49 input compiler jobs.
The selected public campaign passed 30 adapted invocations with 115 declared
coverage skips. Four original joint-invalid constructor calls remain excluded
from that success; their earlier failure is preserved. Published-revision CI
results are recorded separately.
See the [current verification report](docs/non-null-api-573-verification.md) for
evidence and remaining gates. The [revision4 contract review](docs/non-null-573/RNG_CONTRACT_REVIEW.md)
preserves earlier failures and the RNG observation classification. Focused handoffs
retain their historical source milestones and displaced execution accounting.

The later [decimal finite-control audit](docs/decimal-overload-541-verification.md)
adds six common methods covering the39-entry inventory and selected explicit
dispatch/identity rules. All four local targets and all six matching-head CI
jobs pass on published `de5d2a1`, including Linux ARM64 execution. Broader
numerical-domain proofs remain open.

## Source and behavioral compatibility contract

The intended migration is **Java → Kotlin using java.math → Kotlin using
krogu-math**. Source compatibility targets the second step: Kotlin call sites
retain their operations and behavior after replacing type and extension imports.
The numerical contract follows the public
[Java SE 25 java.math API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/package-summary.html).
Documented
[Kotlin operators](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/div.html)
and [numeric conversions](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/to-big-decimal.html)
are part of the migration contract. Proven JDK defects are handled through the
specification-led procedure in [COMPATIBILITY.md](COMPATIBILITY.md#normative-contract-and-oracle-classification).
The current snapshot has not yet achieved this target.

| Surface | Compatibility target and boundary |
|---|---|
| Kotlin source | Preserve numerical calls, constants, documented operators and extensions after replacing their imports. Constructor-like factories and interface-backed types are allowed when the Kotlin call contract remains compatible. |
| Java source | Required only inside the TCK harness. Test-only adapters may supply Java constructors, constructor references or subclass facades where tests need them. The public library does not promise Java source compatibility and need not expose real Java constructors. |
| Numerical behavior | Preserve documented values, rounding, precision, preferred scales, encodings, text and primitive conversions. Decimal equality remains scale-sensitive; ordering is numerical. |
| Exceptions and identity | Preserve documented error conditions/types, any specified diagnostics or identity, and language-level virtual dispatch. Unspecified allocation, caches, diagnostic wording and internal callback order are reference observations rather than independent guarantees. |
| JVM boundaries | Existing Java `Random` constructors and `toJava*`/`toKrogu*` adapters are snapshot facilities, not requirements on the future library shape. Kotlin calls into APIs whose signatures use `java.math`, including JDBC, still need explicit boundary conversion. |
| Binary compatibility | Relocation does not provide ABI compatibility with binaries compiled against `java.math`; consumers must recompile. |
| Serialization | Current-format krogu JVM round trips are supported. Older krogu streams, legacy proxies and interchange with `java.math` streams are outside requirements. |
| Runtime details | Algorithm choice, private representation, performance, resource-exhaustion timing and exact random/prime sequences are not promised. Documented numerical range and probability guarantees remain required. |

Kotlin's JVM numeric extensions in the
[default-imported](https://kotlinlang.org/docs/packages.html) `kotlin` and
`kotlin.text` packages target `java.math` types. A krogu migration needs
corresponding krogu extensions or members; importing an extension does not replace
a matching [member function](https://kotlinlang.org/docs/extensions.html).
Decimal `/` retains the dividend scale and rounds `HALF_EVEN`, matching Kotlin
stdlib. The named `divide(other)` method remains exact. For example,
`BigDecimal("1") / BigDecimal("3")` is zero at scale zero, while
`BigDecimal("1.00") / BigDecimal("3")` is `0.33` at scale two.
The library provides `inc`/`dec`, primitive/string `toBigInteger` and
`toBigDecimal` extensions, and nullable parsing helpers in
`io.heapy.krogu.math`. Migrated callers must import these extensions.
Broader migration checks remain tracked in kotgent `local:525`; the available
helpers do not establish full migration compatibility.

Kotlin/JS 2.4.21 also has a confirmed compiler bug in conversions through
`kotlin.Number` and generic `<T : Number>` receivers: calls can bypass the
conversion overrides and return incorrect values. The
[standalone Number diagnostic](diagnostics/js-number/README.md) reproduces the
problem without krogu arithmetic and records the upstream issue and failed
coercion workarounds. A production workaround remains unresolved; full Kotlin/JS
source and behavioral migration is still open.

During the current unpublished `0.x` phase, release-to-release source and binary
stability is not established. Changes to public Kotlin calls or behavior must be
documented. Stable releases must declare supported toolchains and binary
compatibility separately; they target source and behavioral stability within a
major version, with breaking public-contract changes requiring a major version
and migration notes. This policy does not add serialized-format compatibility.

The test suite is evidence for the covered cases, not proof of full compatibility.
Existing oracle suites mix specification guarantees with additional runtime
observations; their classification is tracked in kotgent `local:526`. Absence of
a documented detail does not prove a JDK bug. A defect exception requires a
public reproducer, an explicit violated contract and an independent expected
result; diagnostic oracle data is retained. Contract-preserving subclasses remain
in scope. A subclass reporting a negative precision or a digit count inconsistent
with its unscaled value violates the documented precision invariant, so that
case alone does not establish a required result or a JDK defect.

Official TCK/JCK has not been run. These differential suites are not a TCK result
or certification.

`parallelMultiply` computes the same result synchronously. Multiplication and
division currently prioritize straightforward correctness over JDK's advanced
large-input algorithms. Extremely large inputs remain subject to available
memory and execution time. Random sources are caller-selected; the default
primality witness generator is Kotlin's default random source.

The active work toward full source and behavioral compatibility, its evidence and remaining
gaps are tracked in [COMPATIBILITY.md](COMPATIBILITY.md). The current passing
campaigns do not establish exhaustive conformance.

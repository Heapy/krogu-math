# krogu-math

Independent Kotlin Multiplatform implementation of the Java SE 25 `java.math`
public API, in the package `io.heapy.krogu.math`. Licensed under Apache 2.0.

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

This is an initial implementation (`0.1.0-SNAPSHOT`), not a published Maven Central
release. To consume locally, run `./gradlew publishToMavenLocal`, add `mavenLocal()`
to your repositories, and depend on `io.heapy.krogu:krogu-math:0.1.0-SNAPSHOT`.

## Implementation

* BigInteger: immutable sign and little-endian base-2^30 limbs; addition,
  schoolbook multiplication, binary long division, arithmetic shifts, infinite
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

## Platforms and verification

Build pins: **Gradle 9.8.1**, **Kotlin 2.4.21**, **BellSoft Liberica JDK 25.0.4+9**.
The Gradle distribution checksum and CI action revisions are pinned.

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

The committed 4,631 portable vectors compare values, preferred scales, text,
floating-point bits and exception **types** on every test target. JVM tests add
seeded random arithmetic, negative bit operations, modular arithmetic, primes,
rounding, scale boundaries, serialization and API surface checks. Structured JVM
campaigns add over 213,000 comparisons, including 10,000 canonical Double values.
Java sources are compiled and executed to check constructors, static factories,
subclassing, method hiding and virtual Number dispatch. Fixtures are
generated reproducibly with a dependency-free Kotlin script:

```shell
# Requires JDK 25.0.4+9 and Kotlin 2.4.21 (kotlinr).
kotlinr scripts/generate-oracle.main.kts
kotlinr scripts/generate-oracle.main.kts --check
```

The first script invocation compiles the script; later runs use the compiler cache.
You may also `chmod +x scripts/generate-oracle.main.kts` and execute it directly.
Generated fixtures contain public inputs and observable outputs, not JDK code.

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

The three value classes implement Java `Serializable` on JVM through validated
serialization proxies. Their serialized form belongs to krogu-math and is **not**
the `java.math` object-stream format. Non-JVM platforms do not depend on Java
serialization.

## Compatibility boundaries

The test suite is evidence for the covered cases, not proof of exhaustive JDK
conformance. Exception messages, Java binary compatibility with the original `java.math` namespace, exact
prime/random output sequences and performance equivalence are not promised.
`parallelMultiply` computes the same result synchronously. Multiplication and
division currently prioritize straightforward correctness over JDK's advanced
large-input algorithms. Extremely large inputs remain subject to available
memory and execution time. Random sources are caller-selected; the default
primality witness generator is Kotlin's default random source.

The active work toward exhaustive compatibility, its evidence and remaining
gaps are tracked in [COMPATIBILITY.md](COMPATIBILITY.md). The current passing
campaigns do not establish exhaustive conformance.

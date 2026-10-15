# Kotlin conversion imports

For Kotlin code migrating from `java.math`, explicitly import krogu conversion
extensions as well as its types:

```kotlin
import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.MathContext
import io.heapy.krogu.math.RoundingMode
import io.heapy.krogu.math.toBigDecimal
import io.heapy.krogu.math.toBigDecimalOrNull
import io.heapy.krogu.math.toBigInteger
import io.heapy.krogu.math.toBigIntegerOrNull

val count: BigInteger = UInt.MAX_VALUE.toBigInteger()
val amount: BigDecimal = "12.3400".toBigDecimal()
val scaled: BigDecimal = count.toBigDecimal(scale = 2)
val rounded: BigDecimal = count.toBigDecimal(mathContext = MathContext.DECIMAL32)
```

Importing the number class alone does not change a Kotlin standard-library
extension's result type. Explicit extension imports select the krogu overloads.
The compile examples use a separate package and declared result types to check
that selection.

This addition provides Int/Long/UInt/ULong integer conversions, Int/Long decimal
conversions with optional context overloads, BigInteger decimal conversions with
named scale/context defaults, and String throwing/nullable conversions. It uses
the own constructors and arithmetic; no JDK numerical implementation is called.
The later [floating conversion integration](kotlin-floating-conversion-migration.md)
adds Float/Double overloads. Increment/decrement, shifts, inversion and named
bitwise extensions are also present in `OperatorExtensions.kt`; import those
extensions explicitly when used. Tasks525 and557 remain open, including
Kotlin/JS erased Number dispatch.

The public Kotlin [integer parsing contract](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/to-big-integer-or-null.html)
distinguishes invalid text (null for OrNull) from an invalid radix (an
IllegalArgumentException). Valid input that needs rounding with UNNECESSARY
retains its ArithmeticException even through
[decimal OrNull](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/to-big-decimal-or-null.html).
Only NumberFormatException is converted to null.

The preserved pinned Kotlin 2.4.21 observation rejects `+00123` through
toBigIntegerOrNull despite the throwing parser accepting it. The nullable parser
promises a parsed integer for valid text, and the public
[BigInteger grammar](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#%3Cinit%3E(java.lang.String,int))
permits the optional plus sign. Krogu accepts it. Independent decimal values
123, 291 and 1371 at radix 10, 16, 36 respectively and both reference outcomes are
kept in the JVM test; no implementation source supplied these expectations.

This is a selected integration from the archived task525 draft, whose historical
four-target campaign had two enabled JS Number failures. That failed campaign
is preserved and does not count as acceptance of the new current-main files.
The selected current-main files subsequently passed fresh isolated checks on
JVM, JS/Node, Wasm/Node and macOS ARM64, using Gradle 9.8.1, Kotlin 2.4.21 and
Liberica 25.0.4+9-LTS. JVM ran six methods in two suites; each other target ran
the three common methods in one suite. Parent independently parsed the archived
XML and matched all required methods, with zero failures, errors or skips.
All four borrowed source/test files retained their frozen hashes.

Those initial checks ran separately in task564 after its formatter/harness snapshot was
archived. They do not validate floating conversions, other operators or erased
Number calls. Later floating and non-null API integration reports record their
own platform gates; these historical six/three-method checks are not evidence
for those additional surfaces.

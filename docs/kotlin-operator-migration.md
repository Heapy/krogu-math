# Kotlin operator migration

Kotlin callers migrating from `java.math` can explicitly import the krogu types
and operator extensions:

```kotlin
import io.heapy.krogu.math.BigDecimal
import io.heapy.krogu.math.BigInteger
import io.heapy.krogu.math.and
import io.heapy.krogu.math.dec
import io.heapy.krogu.math.inc
import io.heapy.krogu.math.inv
import io.heapy.krogu.math.or
import io.heapy.krogu.math.shl
import io.heapy.krogu.math.shr
import io.heapy.krogu.math.xor

var count: BigInteger = BigInteger("9223372036854775807")
val oldCount: BigInteger = count++
var amount: BigDecimal = BigDecimal("1.25")
val nextAmount: BigDecimal = ++amount
val shifted: BigInteger = BigInteger("-9") shr 2
val masked: BigInteger = shifted and BigInteger("5")
val complement: BigInteger = shifted.inv()
```

The increment/decrement extensions call the receiver's public exact
`add(ONE)`/`subtract(ONE)` overloads. BigDecimal arithmetic has no MathContext
here: `1.25` increments to `2.25`, `0.00` decrements to `-1.00`, and `1E+3`
increments to `1001` at scale zero. Prefix and postfix forms follow Kotlin's
variable assignment rules; an alias to the old number keeps its value.

BigInteger `shl(n: Int)`, `shr(n: Int)` and `inv()` call the public
`shiftLeft(n)`, `shiftRight(n)` and `not()` methods. Negative shift distances
retain their public arithmetic behavior. Existing open `and`, `or` and `xor`
members gain the `infix` modifier without changing their implementations,
nullable operands or existing parameter name `value`. Ordinary infix member
calls need no extension import. Top-level `and(other: BigInteger)`, `or(other:
BigInteger)` and `xor(other: BigInteger)` extensions additionally preserve the
standard-library named operand and alias-import forms. Each delegates to its
virtual member using `value = other`, which excludes the extension itself from
that call's candidates. For example:

```kotlin
import io.heapy.krogu.math.and as bitAnd

val named: BigInteger = shifted.and(other = BigInteger("5"))
val existing: BigInteger = shifted.and(value = BigInteger("5"))
val aliased: BigInteger = shifted.bitAnd(other = BigInteger("5"))
val infixAlias: BigInteger = shifted bitAnd BigInteger("5")
```

The [overload-resolution specification](https://kotlinlang.org/spec/overload-resolution.html#call-with-named-parameters)
filters candidates by supplied parameter names before choosing member or
extension candidate sets. Thus `other` selects the imported extension while
`value` keeps the existing member form. The three declarations narrowly
suppress `EXTENSION_SHADOWED_BY_MEMBER` for these intentional named-argument
and alias-import counterparts.

These wrappers preserve explicit receiver overrides, their returned object and
directly thrown exceptions. Increment/decrement are extensions so a caller
subclass can still declare its own `operator fun inc()` or `dec()` without an
`override` modifier. Static receiver type controls extension resolution; a
caller-owned member takes precedence when applicable.

The public Kotlin 2.4 API documents
[inc](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/inc.html),
[dec](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/dec.html),
[shl with parameter n](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/shl.html),
[shr with parameter n](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/shr.html),
[inv](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/inv.html),
[and](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/and.html),
[or](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/or.html) and
[xor](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/xor.html).
The language documents the
[prefix/postfix assignment semantics](https://kotlinlang.org/docs/operator-overloading.html#increments-and-decrements).
Only these public declarations/contracts informed this implementation; no
OpenJDK or Kotlin numerical implementation source was read or copied.

Seven common test methods passed on JVM, JS/Node, Wasm/Node and macOS ARM64;
all six additional JVM methods passed too. The combined integration selection
ran 36 methods in 10 suites on JVM and 24 methods in six suites per other target,
with exact source/XML inventories and no failures, errors, skips or omissions.
The pins were Gradle 9.8.1, Kotlin 2.4.21 and Liberica 25.0.4+9-LTS.
The tests use a separate migration package, explicit imports and declared own
result types. Common cases retain independent numerical literals and source
examples for caller-owned operators; JVM cases also call the public JDK/Kotlin
counterparts. Direct override assertions concern explicit calls rather than
internal arithmetic callback traces. Named `other`, existing member `value`,
unaliased and aliased imports, and caller-owned increment/decrement declarations
were compiled and executed. The first JVM compilation failed because three new
JDK fixture subtypes lacked explicit `toByte`/`toShort` implementations. Adding
their truthful `toInt` narrowing implementations fixed those fixtures; assertions
and production code stayed unchanged. The original failure log is retained.
Supported-platform CI at the integrated head remains required.

This is a selected task525 source migration addition. Floating conversion
extensions, the existing Kotlin/JS erased Number dispatch gap in task557, and
the archived task525 campaign's enabled Number failures remain open. These
finite checks provide no full migration, performance or TCK acceptance claim.

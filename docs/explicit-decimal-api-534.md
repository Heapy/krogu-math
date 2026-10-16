# Explicit arithmetic intent (task 534)

The additive API names construction, rounding and failure policies. Existing
constructors, factories, inheritance, Kotlin operators and BigDecimal equality
keep their existing signatures and behavior. New functions live in
`ExplicitDecimal.kt`; no arithmetic kernel is replaced.

## Construction and rounding

```kotlin
import io.heapy.krogu.math.*

// The old constructor remains exact for the supplied binary floating value.
val old = BigDecimal(0.1)
val binary = Decimals.fromBinary(0.1)
val canonical = Decimals.fromCanonical(0.1)
check(old == binary)
check(canonical == BigDecimal("0.1"))
check(binary != canonical)

val exactHalf = BigDecimal.ONE.divideExact(BigDecimal.TWO) // 0.5
val significant = BigDecimal("123.456").roundToPrecision(3, RoundingMode.HALF_EVEN) // 123
val fractional = BigDecimal("123.456").roundToScale(3, RoundingMode.HALF_EVEN) // 123.456
```

`Decimals.fromBinary` uses the exact IEEE binary64 value; `fromCanonical` uses
the same canonical short decimal representation and scale as `BigDecimal.valueOf`.
Both reject NaN and infinities. Signed zero follows the corresponding original
construction contract: binary conversion has scale 0 and canonical conversion
has scale 1. Explicit MathContext overloads use the original context constructor
for the binary path and canonical coefficient/scale followed by that constructor
for the canonical path. Thus context construction keeps its original zero and
preferred-scale policies, rather than substituting a different operation.

`divideExact` delegates the public exact `divide`; nonterminating results and
zero divisors throw ArithmeticException. It does not change `/`, which retains
the left operand's scale and HALF_EVEN policy. `roundToPrecision` delegates public
`round`, with precision zero meaning unlimited and negative precision rejected.
`roundToScale` delegates public `setScale`, including negative scales. Both
require a rounding mode; UNNECESSARY explicitly requests lossless conversion.
Overriding these public operations in a subclass remains observable.

## Parsing, conversion and named pairs

```kotlin
val parsed = Decimals.parseOrNull("12.00") // null only for malformed text
val converted: ExactConversion<Long> = BigDecimal("12.00").toLongExactResult()
val amount = when (converted) {
    is ExactConversion.Value -> converted.value
    is ExactConversion.Failure -> throw converted.cause
}
val pair = BigInteger("17").quotientAndRemainder(BigInteger("5"))
check(pair.quotient == BigInteger("3"))
check(pair.remainder == BigInteger("2"))
```

`Decimals.parseOrNull` uses the existing String decimal parser with an optional
explicit context overload. It catches NumberFormatException only. A valid
`"1.234"` with precision 2/UNNECESSARY still throws ArithmeticException. Existing
`String.toBigDecimalOrNull` and `String.toBigIntegerOrNull` remain available,
including the integer parser's explicit radix validation; no competing parser
grammar or nullable-receiver overload is introduced.

`ExactConversion.Value` and `ExactConversion.Failure` distinguish success from an
arithmetic refusal. Decimal conversions support exact BigInteger, Long and Int;
integer conversions support exact Long and Int. They call the existing public
exact conversions and retain the actual ArithmeticException on refusal. Fractional
values and out-of-range results are failures; no message-based classification
or zero substitute is used. Invalid arguments, other subclass exceptions and
resource failures propagate. These wrappers do not redefine supported numerical
range or claim that conversions allocate no intermediate storage.

`QuotientRemainder<T>` gives immutable names to the two result slots. Integer and
decimal overloads delegate the existing public `divideAndRemainder`; decimals
also accept MathContext. Their quotient/remainder signs, scales, identities,
exceptions and subclass callbacks therefore follow that operation. The wrapper
does not promise mutable-array reuse savings; it is an intent and naming API.

## Migration and diagnostics policy

These are opt-in additions. No old API receives a new warning: existing consumers
with warningsAsErrors continue to compile. A constructor must remain a real
constructor for Java `new`, `BigDecimal::new`, subclass `super`, and Kotlin
constructor references. `Decimals` is an explicit factory namespace, not a
constructor emulation. Its JVM methods are callable as `Decimals.fromBinary` and
`Decimals.fromCanonical`; Kotlin extensions are ordinary `ExplicitDecimalKt`
static functions in Java. Nullable parse returns are intentional; nonnullable
operands/context/mode retain Kotlin's boundary checking.

For a consumer-owned soft migration, an IDE inspection can offer these rewrites:

| Existing intent | Suggested replacement |
| --- | --- |
| `BigDecimal(value: Double)` when exact binary value is wanted | `Decimals.fromBinary(value)` |
| `BigDecimal.valueOf(value: Double)` | `Decimals.fromCanonical(value)` |
| `x.divide(y)` | `x.divideExact(y)` |
| `x.round(MathContext(p, mode))` | `x.roundToPrecision(p, mode)` |
| `x.setScale(scale, mode)` | `x.roundToScale(scale, mode)` |
| `x.divideAndRemainder(y)[0/1]` used as one pair | `x.quotientAndRemainder(y).quotient/remainder` |

These are proposed `ReplaceWith`-style hints, not blanket deprecations or an
automatic rewrite of `/`. A consumer may add an opt-in lint rule for ambiguous
Double construction and discarded immutable results, such as an expression
statement `amount.roundToScale(2, mode)`. The rule must preserve assignments,
returns, constructor references and consumer subtype members. No global compiler
flag or warning policy is enabled by this library change. A discarded immutable
result cannot update the receiver; all examples bind or use results explicitly.

Extension names are distinct from existing class members and stdlib conversions;
there are no default arguments in the new overload families. A consumer subclass
may define a member with the same name; Kotlin members take precedence over
extensions, so such a consumer must choose an explicit receiver/import strategy
or keep calling the original member. The library cannot override that language
rule. Focused Kotlin typed callable-reference tests and a Java construction/
subclass fixture validate the added overload forms with the existing ones.

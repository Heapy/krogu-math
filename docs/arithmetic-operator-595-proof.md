# Arithmetic operator source migration: task595

This is a SOURCE-only candidate based on published
`12d0fe287cdec99567bd7cbf981b65aa86ef834b`, source255 manifest
`8427be10a953974f1b74348dec87ea0cbbbb6f93c6e806875b049e4ff2fb339d`.
The candidate and all five new test methods are **UNCOMPILED/UNRUN**.
No current candidate result is inferred from the published revision's tests.

## Obstruction and language boundary

The original BigInteger lines1198–1204 and BigDecimal lines933–939 declared
seven final arithmetic members. A Kotlin subclass of `java.math.BigInteger` or
`java.math.BigDecimal` can instead declare its own same-signature `operator`
without `override`. That is a new caller member, not an override of a JDK member.
Changing the class import made it collide with krogu's final member. Kotlin's
[final-method rule](https://kotlinlang.org/docs/inheritance.html#overriding-methods)
forbids that declaration with or without `override`.

The accepted original SOURCE analysis is `ee6e44f186659481f3d00409a7e02e393a2e74a0ceaaeef1efd386264563ff0e`.
A later, separate parent-owned compiler witness records JDK exit0 and old-krogu
exit1 at both caller `plus` declarations:
`/private/tmp/krogu-595-compiler-witness-M7ySKquH/OUTCOME.md`, SHA256
`7377e71f7a698c37c0f2ca08e6f47d32145af0a88453a9c00868a2835947a775`.
Its first diagnostic is that `plus` hides the supertype member and needs override;
it did not compile a second attempt adding override. Finality remains a source
and language-rule argument. This later observation does not compile this fix or
the seven-operator positive witnesses prepared here.

## Exact production change

Only the fourteen final operator declarations are removed from the two classes.
BigDecimal's `@JvmSynthetic` attached to the removed binary `plus` is removed
with it. `OperatorExtensions.kt` gains the following seven extensions for each
type, using the same non-null `other` parameter and the identical old expression:

| Name | BigInteger body | BigDecimal body |
| --- | --- | --- |
| `plus(other)` | `add(other)` | `add(other)` |
| `minus(other)` | `subtract(other)` | `subtract(other)` |
| `times(other)` | `multiply(other)` | `multiply(other)` |
| `div(other)` | `divide(other)` | `divide(other, RoundingMode.HALF_EVEN)` |
| `rem(other)` | `remainder(other)` | `remainder(other)` |
| `unaryMinus()` | `negate()` | `negate()` |
| `unaryPlus()` | `this` | `this` |

BigDecimal's genuine open `plus()` and `plus(MathContext)` remain byte-identical.
Existing increment, decrement, bitwise and shift extensions remain byte-identical.
No arithmetic kernel, constructor, metadata getter, Number conversion, field,
cache, range cap, serialization code or nullability signature is changed. The
separate `lowestSetBit` property question is outside this candidate.

For an imported extension on a base-typed non-null receiver, its receiver is the
same object and its arguments are the original objects. The unqualified delegate
is resolved on that receiver to the existing public member. There is no operator
recursion: `add`/`subtract`/etc have different names, and the decimal two-argument
`divide` has the preserved enum overload. A normally returned object is returned
without copying; an exception escapes without catch/translation. Unary plus
returns that receiver without calling genuine BigDecimal `plus()`. Thus the
existing mathematical/cohort/error obligations of each reached leaf remain the
same conditional premises, not newly proved kernel theorems.

The [public stdlib division contract](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/div.html)
requires receiver scale and HALF_EVEN for BigDecimal `/`, explaining why exact
one-argument `divide` is not substituted. This candidate preserves krogu's own
identity unary-plus convenience; no stdlib `java.math` unary-plus declaration is
asserted. Public plus/minus/times/div/rem/unaryMinus extension signatures are the
stdlib migration counterparts, without reading their implementation bodies.

## Caller resolution, imports and explicit calls

Under [explicit receiver overload resolution](https://kotlinlang.org/spec/overload-resolution.html#call-with-an-explicit-receiver),
an applicable caller member precedes an imported extension. The new source permits
the original caller-owned seven member declarations, intentionally without
`override`. Widening the same object to a base type selects the extension and then
the public arithmetic member; it does not select a new virtual `plus` slot. This
is why merely making the old operators open is not the equivalent repair.

An alias import changes the name, so `n.arithmeticPlus(other = rhs)` can explicitly
select the extension even where `n.plus(rhs)` would select a caller member.
`other` remains the extension parameter name. A caller's explicit `super.add`
continues to call a non-extension member. These are observable source-language
and explicit-call requirements. They do not prescribe a kernel's internal
callback count, order or identity. No poisoned-metadata class is blanket-waived.

Package extensions must be imported in external packages. Static inspection of
the existing Kotlin source finds one affected own external-package call:
`KotlinConversionExtensionsContractTest` gains only `import io.heapy.krogu.math.plus`
for `Long.MAX_VALUE.toBigInteger() + BigInteger.ONE`. Its assertions are unchanged.
Existing same-package tests need no import. Existing external migration files and
own scripts importing the big-number types use public named arithmetic or only
primitive arithmetic at their other sites; no broad wildcard/import rewrite was
made. `kotlin-operator-migration.md` gives the seven explicit import names and
the alias/base/subtype distinction. Java source compatibility outside the TCK and
old krogu binary member ABI are not preservation claims of this source migration.

## Prepared independent finite controls

`KotlinArithmeticOperator595Test` is in a separate migration package, with three
methods and explicit unaliased/aliased imports:

1. Ordinary integer `7` and decimal `7.50`, rhs `2`/`2.0`, use independent
   mathematical literals for all seven operators, named and aliased forms.
   Decimal fields are `950@2,550@2,15000@3,375@2,150@2,-750@2,750@2`.
   Additional halfway `1.25/2.000→62@2`, `1.35/2.000→68@2` and negative controls
   distinguish receiver scale and HALF_EVEN. Exact named division remains exact;
   zero-divisor error categories, source aliases and unary-plus identity are kept.
2. Truthful caller-owned seven operators on both types call the genuine `super`
   arithmetic methods. A label is assigned only inside those explicitly declared
   caller operators. The test distinguishes narrow member selection from base
   extension and differently named alias selection. It asserts no incidental
   kernel callback trace.
3. Reached public leaf overrides return independently correct preconstructed
   results for the actual operand and otherwise fall back to their truthful
   `super` calculation. The test checks exact returned object/operand identity,
   decimal enum overload/mode, and an explicitly thrown caller exception object
   for each of the six delegated operations. The injected throw mode is a direct
   exception-transport control, not a claim of ordinary mathematical success.

`KotlinArithmeticOperator595JvmTest` has two methods. One pairs six public stdlib
aliased/named calls with own calls and the same independent literals. The other
pairs Kotlin JDK subclasses and the common krogu caller subclasses for all seven
caller-owned declarations, plus base-typed imported calls. The JDK fixtures supply
truthful `toByte`/`toShort` Number narrowing methods; operator declarations lack
`override` on both providers. Caller-defined JDK unary plus is tested without
inventing a stdlib counterpart. These class declarations are the new paired
positive source witnesses, not previously executed artifacts.

All data are tiny decimal literals; there is no fake production cap, giant
allocation, random fixture drift or new oracle waiver. Existing assertions,
fixtures, harness and source255 numerical proofs are retained unchanged.

## Binding and remaining gates

The candidate source257 manifest is
`8bc495400f686cc162920d80ea832ea68dfd556fb3d7c9ed88b7afc1c23de51b`.
Exactly four original source files change (three production files and one test
import); the other251 source files match their original hashes. Two source files
are added. Original archive and255 manifest were verified and preserved read-only
before edits; a later read-only baseline extraction supports the full narrow diff.

Independent source/test review, candidate compilation (including the paired
witnesses), fresh selected/full platform tests and exact source/XML/JAR binding
remain required before adoption. Source-derived prospective additions are three
common methods and two JVM-only methods, not execution evidence. Existing decimal
operator tests remain active. Matching published-head supported/oracle/public
gates remain separate. Number525/557 remains required and unresolved; this finite
repair is not full525/553, performance, TCK/JCK or whole-project acceptance.

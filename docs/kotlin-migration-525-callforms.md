# Kotlin migration call forms: inventory and later repairs

SPDX-License-Identifier: Apache-2.0

The unchanged inventory below records published source255 at commit
`12d0fe287cdec99567bd7cbf981b65aa86ef834b`. It found two concrete source-form
obstructions,595 and596, alongside the separate Kotlin/JS Number issue. Its
original source references and C/L/H/U labels keep that historical scope;
they are not silently relabelled as a later version's results.

The subsequent source260 composition moves the fourteen arithmetic operators
to imported extensions and adds the imported `BigInteger.lowestSetBit` property.
Its local actual record has 1,785 passing method executions/446 suites over four
targets, the unchanged complete 94,282-record ledger, and successful compiler
witnesses for both repaired forms. Independent actual review accepted this local scope;
matching published-head source260 gates remain separate. See
[the composed verification](kotlin-callform-595-596-verification.md),
[operator imports](kotlin-operator-migration.md) and
[property imports](kotlin-lowest-set-bit-property.md).

The old paper examples were UNCOMPILED when this inventory was frozen. Later
paired old-JDK/old-krogu compiler evidence and the new-source positive witnesses
are separate chronological observations, recorded in the composed verification.
The original inventory SHA is
`8e1511ad0fe693412a1ff9525eca47d19b5b01cf3400740b50deeb43ce0adeb7`;
its body follows unchanged.

### Later accepted caller migration

After this historical inventory, the user selected explicit caller edits using
`Number.toKroguByte`, `toKroguShort`, `toKroguInt`, `toKroguLong`, `toKroguFloat`
and `toKroguDouble` (525 comments2544/2545). See the
[documented workaround and limitations](../README.md#kotlinjs-converting-through-number).
The595/596 repairs above and this caller migration have accepted dispositions;
the historical remaining-work statement below records source255, not new open
implementation tasks. Original `Number.toX()` compiler behavior and the retained
UNSAT diagnostic are unchanged. Third-party calls and unrelated custom Number
implementations are not transparently repaired. Final composed caller and
supported/public evidence remain obligations of553; no official JCK/TCK or full
compatibility acceptance follows from these narrow dispositions.

---

#525/553 finite Kotlin migration call-form ledger at source255

The migration row is **not complete**. This finite accounting confirms the retained conversion, named/default, import, infix, increment/decrement and selected inheritance/super tranches. It identifies two specific source-form obstructions not represented by their successful examples: a caller-owned `plus` declaration collides with the own final member, and Java's synthetic `lowestSetBit` property has no own property counterpart. The known Kotlin/JS erased/generic Number behavior remains **UNSAT**. None of these findings is inferred from a missing giant test or a numerical proof gap.

## Binding and method

The reviewed publication is `12d0fe287cdec99567bd7cbf981b65aa86ef834b`. `docs/source255.sha256` is `8427be10a953974f1b74348dec87ea0cbbbb6f93c6e806875b049e4ff2fb339d`; all255 source hashes and the exact source pathset checked successfully in this scope. The four core hashes are:

| Type | SHA256 |
| --- | --- |
| BigInteger | `8b6359be57ebbc8047102e5dea420e307d4fb128cbf686a054b04dff2600b1ff` |
| BigDecimal | `82e5342ab19024aed3aee3191eb340a72eba316c843d708986e5eb5827d2468f` |
| MathContext | `d3690387b1345f90cc927044a1a450983dd0d2ce6e3e47054384c31979fd5261` |
| RoundingMode | `e47d7aa314ddaa9c79ba13b79a00beadb8be96cbb473cd4ad2dfae3e0501a5ee` |

The fresh525 task snapshot and accepted553 boundary report (`b3d5b36eee1c766b5bf0ea7d24756f5662daff5b08d5edcdb083ac69be83fc28`) are preserved in inputs. The original task's Java syntax examples are qualified by the user's later explicit boundary: production Java source/constructor ABI is unnecessary, Java adapters belong in TCK, and Kotlin caller/override nullability edits are permitted. Kotlin call forms, ordinary results and explicit caller behavior remain required.

This is source/declaration/language accounting, reusing accepted execution reports at their original scopes. No new compiler result is claimed. `C` below means a current source positive example belongs to the accepted exact-source255 full selection; `L` means declaration/language composition, without claiming an individually compiled paired example for every possible spelling; `H` identifies a narrower historical tranche; `U` identifies an unsatisfied required form. Lines refer to the pinned workspace files copied under inputs. Complete member/constructor overload inventories are reused from549 rather than duplicated here.

## Finite promised forms

| Family | Declaration and current positive call sites | Evidence / disposition |
| --- | --- | --- |
| Four relocated types; constructors and constructor-like random functions | BI31–38, BD10/25–42, MC5/14/16, RM4–5; `RandomFactories.kt:9,12` supplies the two `kotlin.random.Random` forms.549's constructor inventory maps27 real constructors and two functions. `ValueIdentityContractTest.kt:119–168` includes byte/char slice/context constructions. | C+L. Real Kotlin constructors, superclass constructor calls, and overloaded String/primitive/coefficient/slice forms exist. Accepted549 handles their numerical/error contracts. This does not reinstate production Java `new`/constructor-reference ABI. |
| Constants, static-style factories and enum forms | BI1212–1215/1225/1234–1235; BD959–962/972–974; MC33–36; RM entries/generated `values`, name/ordinal/valueOf. `ValueIdentityContractTest.kt:215,236,265` exercises constants, context properties/getters, enum snapshots/names/legacy mapping. | C+L. Companion lookup supports `BigInteger.valueOf`, `BigDecimal.valueOf`, etc.; enum/property forms have explicit existing examples. No missing factory declaration was found. Factory callable references are supported by the declarations plus ordinary Kotlin callable-reference resolution; no separate paired factory-reference campaign is claimed. |
| Constructor references | `IntegerArithmetic544ContractTest.kt:41` uses `.map(::BigInteger)`; `DecimalPrimitiveLowBits591Test.kt:113` uses `.map(::BigDecimal)`. | C for real constructor references in current common sources. This is not a Java constructor-reference guarantee. |
| Ordinary member overloads, explicit result types and context overload selection | Accepted549 maps57 integer and75 decimal nonconstructor entries. `DecimalOverloadDispatch541Test.kt:30–108` covers the39-entry finite overload inventory. Conversion migration tests explicitly declare own result types in another package. | C+L with accepted541/549 numerical and reached-call premises. Reflection `ApiSurfaceTest` is supplementary, not a proof of Kotlin expression resolution or synthetic properties. No fresh all-overload mathematical audit is claimed. |
| Typed/named/default numeric conversions | `NumericExtensions.kt:5–16`; `KotlinConversionExtensionsContractTest.kt:19–46` selects Int/Long/UInt/ULong results, zero-argument BI→BD and named `scale`, `mathContext`, both positional arguments and negative scales. JVM counterpart `KotlinConversionExtensionsConformanceTest.kt:37`. | C; H six-JVM/three-other selected conversion integration is preserved in `kotlin-conversion-extensions.md`. Named/default extension overloads are concrete source examples, not inferred from49 nullability jobs. |
| Throwing/OrNull text conversions and errors | `TextExtensions.kt:4–21`; common migration tests47/68 and JVM71/82 exercise named radix/context, nullable result types, NFE→null, invalid radix IAE and UNNECESSARY AE propagation. | C. Existing optional-plus reference discrepancy remains independently specified/recorded. No generic parse/error-message waiver. |
| Floating conversion extensions versus exact Double construction | `NumericExtensions.kt:20–25`; six common methods in `KotlinFloatingConversionContractTest.kt:20–115`, four JVM methods at27/43/60/79. Canonical Float/Double text extensions and exact binary constructor are deliberately distinct. | C; historical floating integration report retains its own dates/pins. Ordinary conversion mathematics composes with543, not with an erased Number bypass. |
| Explicit imports, alias imports and named infix operands | Types alone do not retarget stdlib conversions; explicit own `toBigInteger`, `toBigDecimal`, OrNull and operator imports are documented. `OperatorExtensions.kt:16–20` uses `other` and delegates to open `value` members. Common operator111 and JVM151 exercise `other=`, `value=`, aliased imports and infix aliases; JVM fixtures alias JDK classes and stdlib functions. | C+L. Class/import aliases are ordinary name binding, not new numerical contracts. Current separate migration packages and declared own types prevent accidental host-return acceptance. Imports alone cannot repair the two concrete gaps below. |
| Arithmetic operators and augmented assignment | Current BI1198–1204 and BD933–939 declare seven member operators. Ordinary `+,-,*,/,%,unaryMinus` delegate to accepted own leaves; BD `/` specifically calls rounding-only HALF_EVEN, unlike exact `divide(other)`. Kotlin `+=` etc. without a matching assignment operator compose through the selected operator and assignment/type rules. | L for the finite source family, supported by underlying accepted member behavior. **U for caller-owned same-signature `plus`: see finding A.** Ordinary calls do not establish compatibility of subclass declarations. No separate exhaustive paired expression campaign is claimed here. |
| Prefix/postfix inc/dec and alias values | `OperatorExtensions.kt:5–8`; common operator tests24/54 and JVM36/79 have typed prefix/postfix results and old-alias preservation. Caller-owned inc/dec are at common223–233 and JVM176–200. | C. Existing caller-owned method name is inc/dec specifically; the broad JVM test name does not imply caller-owned plus was compiled. Extension static resolution and member precedence preserve those existing inc/dec declarations. |
| Shifts, inversion and signed bitwise/infix forms | `OperatorExtensions.kt:10–20`; BI open infix and/or/xor members822–824; common operator82/111/130 and JVM120/151/219. | C. Negative distances/two's-complement literals, named `n`, both operand names, alias imports and explicit replacement/throwing override identities are retained. |
| Properties / Java getter syntax | MC has real `precision`/`roundingMode` properties5/7 and method aliases20–25; `ValueIdentityContractTest.kt:236–245` exercises both. RM properties are enum-generated. BI769 is only `fun getLowestSetBit()`. `JdkOracleTest.kt:64` already uses host `x.lowestSetBit` versus own `a.getLowestSetBit()`. | C for MC/enum properties. **U for own `.lowestSetBit`: see finding B.** BI `bitLength()` and BD `precision()/scale()/unscaledValue()` are ordinary methods in the Java API, so this ledger does not invent synthetic properties for them. |
| Kotlin subclass construction, explicit overrides, super and transitive inheritance | `IntegerArithmetic544ContractTest.kt:127,161,203`; `DecimalOverloadDispatch541Test.kt:11–22,73,109`; `DecimalOverloadContract541Test.kt:11–26,71`; JVM `KotlinSubclassContractTest.kt:7–18`. | C+L for real constructor/override/super/base-typed examples and caller results/exceptions. Explicit nullability edits are adopted. No claim covers arbitrary invalid/changing subclass metadata or waives inherited mathematics because an unused getter is poisoned. Finding A is a new declaration-name collision outside these selected override examples. |
| Concrete/Number/generic conversion calls | JVM `KotlinSubclassContractTest.kt:12` checks concrete and Number-typed toInt dispatch. Existing543 conversion proofs bind reached bodies. `diagnostics/js-number/README.md` and accepted553 preserve the independent tiny erased/generic Kotlin/JS failure evidence. | C for the JVM selection; **U for JS Number and `<T:Number>` required dispatch** (525/557). Direct concrete calls or a successful matrix cannot substitute for the erased caller, including distinct Float/Double and callback/exception behavior. |
| Java integration/adapters | `JavaMath.kt:17–22`: `toJavaBigInteger`, `toKroguBigInteger`, `toJavaBigDecimal`, `toKroguBigDecimal`, `toJavaMathContext`, `toKroguMathContext`. `JdkOracleTest.kt:178–189` has real decimal/context round trips and java.util.Random construction; integer use at99 crosses to a public JDK primality call. | C+L. JDBC/third-party `java.math` signatures require explicit adapters. No automatic binary assignability or absent generic `toJava()` name is promised. Java source compatibility only in TCK does not waive Kotlin source compatibility. |

## Finding A: caller-owned arithmetic member collision (595)

The following is a **paper/source witness, UNCOMPILED in this scope**. Only the type import is changed:

```kotlin
import java.math.BigInteger // migrate to io.heapy.krogu.math.BigInteger
class CallerInteger : BigInteger("1") {
    override fun toByte(): Byte = toInt().toByte()
    override fun toShort(): Short = toInt().toShort()
    operator fun plus(other: BigInteger): BigInteger = BigInteger.TEN
}
fun chosen(): BigInteger = CallerInteger() + BigInteger.ONE
```

The truthful byte/short overrides follow existing compiled JDK fixtures in `KotlinOperatorMigrationConformanceTest.kt:176–190`; they prevent conflating an unrelated fixture requirement with this issue. The public JDK-facing Kotlin `plus` is an extension, so a caller can declare its own member. With the own import, BI1198 introduces an inherited same-signature **final** member. The caller declaration then hides a base declaration and cannot be repaired merely by adding `override`, because the base member is final. A corresponding BD-valued plus witness has the same obstruction; JDK BigDecimal's own `plus()`/`plus(MathContext)` are different signatures.

Root independently inspected this source and public authority and created focused local595. Public language bindings supplied by root are [overriding methods](https://kotlinlang.org/docs/inheritance.html#overriding-methods), [stdlib plus declarations](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/plus.html), and [member-before-extension resolution](https://kotlinlang.org/spec/overload-resolution.html#call-with-an-explicit-receiver). No foreign stdlib implementation was inspected. The seven final declarations are a factual family requiring the focused resolution; the plus witness alone proves the obstruction. Root found public counterparts for six arithmetic extensions. No public stdlib unaryPlus counterpart was verified: own unaryPlus is an own convenience API that can still collide with a caller-defined member, not evidence that a JDK stdlib unaryPlus promise exists.

## Finding B: Java synthetic getter property has no own counterpart

A second bounded source witness is:

```kotlin
import java.math.BigInteger // migrate to io.heapy.krogu.math.BigInteger
val bit: Int = BigInteger("8").lowestSetBit
```

The host form is already present in current compiled `JdkOracleTest.kt:64`. Own BI769 preserves the callable getter, but a Kotlin-defined `getLowestSetBit()` function does not itself declare a Kotlin property. The reviewed common/JVM sources contain no `lowestSetBit` property/extension. Thus the import replacement loses this expression even while `getLowestSetBit()` and its numerical result remain available. This is a Kotlin source form gap, not a missing JVM getter or Java ABI demand. Root independently confirmed the source distinction and created separate local596 against the public [Java getters and setters interop rule](https://kotlinlang.org/docs/java-interop.html#getters-and-setters). No fresh compiler error is claimed. The eventual narrowly imported extension-property route could retain virtual getter result/exception behavior without inventing a new backing field; no implementation is authorized by this report.

## Exact evidence limits and proportional remaining work

The exact-source255 accepted local report is `docs/decimal-division-normalization-594-verification.md`, SHA `8381e550ae87d02dc3136fae508481c505ca6ab907492d0092f2b71d33eb8d69`. It records JVM175 suites/584 methods and JS/Wasm/macARM each87/391, total436 suites/1757 methods, zero issues. Its complete source-derived inventories include the existing migration examples above. Fresh own JAR is `bb15a352df94be410ce79c310af6f541ee4206c59148dc30f81e9dc79270104b`. No test ran for this ledger. Successful current examples do not compile every possible caller declaration, as A demonstrates.

`non-null-api-573-verification.md:38–46` records one positive compilation batch (48 calls,32 migrated overrides,three equals controls) plus48 separate negative argument jobs. No compiled batch function was invoked. These49 jobs are a historical rejecting-null tranche, not49 runtime checks, a fresh source255 compiler campaign, or a complete migration matrix. Existing current tests retain NN declarations. Three nullable equals inputs/four OrNull results remain intentional; nullability override edits are explicitly permitted.

The original unmerged525 draft's enabled JS Number failures and its obsolete nullable-override diagnostics are preserved history, not current compile failures of the selected conversion/operator/floating files. Each selected integration doc retains its own pins and first failure/correction. No historical result is relabelled as the new witness compilation. Exact-source253 hosted5812/13+25oracle and its accepted106 normal+12 large retained public IDs remain earlier artifacts; they do not supply current255 hosted outcomes. Current matching hosted acceptance is outside this finite scope.

The remaining migration work is now concrete: resolve595's caller member collision with bounded real compiler regressions; resolve596's getter-property form without changing virtual numerical behavior; retain the mandatory Number525/557 blocker and external compiler boundary. The rest of this finite table supplies declaration/language or compiled-positive accounting rather than an unbounded enumeration request. No new missing factory/constructor/member overload is established merely because a separate spelling has no dedicated paired fixture. Full525/553/JCK acceptance is not claimed.

Only new private memo/input snapshots/hash indices were written. No repository/source/test/fixture/assertion/mapper/task/workflow/Git/mutex mutation, compiler/script/numerical/helper/model/certificate/probe/test/build/JAR or numerical child-process execution, new network acquisition, delegation or foreign implementation read occurred. Public authority above was supplied by root from its separate public-only review. One metadata lookup guessed nonexistent common `MathTest.kt` and `DecimalConformanceTest.kt`; rg reported missing paths, no numerical call occurred, and no claimed evidence relies on those paths. All owned calls are terminal.

#596: imported lowestSetBit property — source proof

## Frozen baseline and scope

The isolated no-Git/no-build candidate is copied from published
`12d0fe287cdec99567bd7cbf981b65aa86ef834b`, source255 manifest
`8427be10a953974f1b74348dec87ea0cbbbb6f93c6e806875b049e4ff2fb339d`.
The full255 source hashes and exact source pathset passed before copying; all255
copied source hashes passed before edits. The original archive
`../originals/base-12d0.tar`, SHA
`79427ffa3c6a6d008a4e4a286d1720ee8dd9b19bd0421f58402f849d20e2f962`,
and original source manifest were made read-only before edits.

Only a new common extension property, separate common/JVM migration tests and
new596 documents are added. BigInteger, BigDecimal, MathContext, RoundingMode,
OperatorExtensions, every old source/test/fixture, the build and harness remain
unchanged.595 has a different isolated owner. No compiler, script, numerical
helper/model/certificate, probe, test, build, JAR or numerical child was run.

## Source-form obligation

The public Kotlin [Java getter/property rule](https://kotlinlang.org/docs/java-interop.html#getters-and-setters)
allows `java.math.BigInteger.getLowestSetBit()` to be spelled `.lowestSetBit`.
Existing `JdkOracleTest.kt:64` already compiles that host form and calls the own
getter through its method spelling. A Kotlin-defined `fun getLowestSetBit()`
alone does not declare the same Kotlin property. The source255 finding was
confirmed by root's public-only language/source inspection; no new negative
compiler diagnostic is claimed.

The new `BigIntegerProperties.kt` declares exactly:

```kotlin
val BigInteger.lowestSetBit: Int
    get() = getLowestSetBit()
```

The public migration requires an explicit property import as well as the type.
An aliased import supplies the same body under a different source name. This is
the same adopted explicit-extension-import boundary used by other525 forms,
not a promise that a class import alone imports all extensions.

## Refinement of the existing virtual call

For every non-null receiver x, evaluating the extension getter evaluates the
public `x.getLowestSetBit()` call and returns its Int result unchanged. Resolution
is to that member function, not back to the property. No sign, bit length,
absolute-value, text or stored-limb accessor is added. A base-typed receiver still
dispatches through its ordinary public override; a truthful override calling
super therefore retains its mathematical position. A directly throwing override
has no catch/wrapping path, so its same exception object escapes. This is an
explicit getter-call obligation, not a blanket rule for incidental internal
callback traces or arbitrary inherited calls with misleading metadata.

On the ordinary base mathematical domain, the existing getter supplies -1 for
zero. For nonzero x it supplies the largest k such that 2^k divides x; replacing
x with -x preserves that k. The extension adds no arithmetic theorem, coefficient
support check, index/counter, buffer or allocation proportional to the value.
It reuses the existing getter's range/resource premises instead of introducing
a smaller domain. There are no fields, setters, caches, serializers or mutations.

An extension is resolved from the static receiver/import context, while its
body's member invocation remains virtual. Caller-owned applicable members retain
ordinary member precedence. No property reference syntax is introduced; in
particular experimental Java synthetic-property references are not required.
On JVM the extension accessor belongs to its generated top-level file class,
with a receiver parameter. It is not a second member getter on BigInteger and
does not collide with or alter the existing member/getter override surface.
Production Java compatibility remains outside the requirement.

## Prepared independent checks

All tests below are **UNCOMPILED/UNRUN**. Three common methods and two additional
JVM methods are prepared in a separate migration package, with explicit normal
and aliased imports and declared Int results.

| Common method | Independent obligation |
| --- | --- |
| literalsPreserveZeroSignsAndLimbBoundariesWithoutChangingTheValue | Zero=-1; odd numbers=0; 2,4,8 have positions1,2,3; literal powers2^30,2^60,2^63 cross stored-limb boundaries; adjacent2^30+1 is odd. The large even decimal ends90 and so is divisible by2 but not4. Every nonzero literal is also checked negative. Property, alias and method must each equal that literal expectation; text/byte outputs remain unchanged. |
| importedAndAliasedAccessComposeWithTruthfulVirtualGetterAndSuper | A real base-typed Kotlin subclass delegates its getter to super at -2^63; both imports and the method must yield63. |
| directGetterExceptionIdentitySurvivesBothPropertyImports | A directly throwing getter must propagate its original marker through property, alias and method calls. No callback-count/absence assertion is made. |

The JVM fixtures `jdkProperty(JInteger)` and `ownProperty(BigInteger)` contain
paired ordinary `.lowestSetBit` expressions; `ownAlias` uses the explicit alias.
One method checks both providers independently against literal positions, rather
than using host output as the sole oracle. The second uses truthful super getter
subclasses and per-provider direct marker identity. JDK subtype byte/short
overrides are the existing required truthful `toInt` narrowing pattern; they do
not change the getter or import selection. No fake metadata, synthetic maximum,
giant object, counter-only test or new kernel is introduced.

These fixtures are source preparation, not observed paired compiler success.
Independent source review, a separately authorized compilation/execution and
fresh four-target/matching published-head evidence remain future gates. Source
inventories should derive the composed counts after595/596 integration. The
known Number525/557 failure,595 collision, full525/553, mathematical range audits,
performance and official JCK remain outside this narrow repair.

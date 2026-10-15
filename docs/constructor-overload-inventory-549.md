# Public constructor overload inventory (549)

The later [focused parent validation](integer-parser-range-559-proof.md#parent-focused-validation)
compiled and passed all three new constructor methods on JVM, JS, Wasm and
macOS ARM64, with exact method inventories and no failures/errors/skips. The
source-only status below records the original preparation phase. Matching-head
supported CI and the separate public parser-range failures remain open.

This finite source inventory accounts for every public constructor declared by
the four own math types, including the public BigDecimal primary constructor.
There are **27 real public constructors**: eight BigInteger, sixteen BigDecimal,
three MathContext, and none for RoundingMode. Two additional Kotlin Random
functions preserve BigInteger constructor-call syntax; they are listed separately
and are not missing JVM constructors. Private copying/parsing constructors,
companion factories such as `valueOf`/`probablePrime`, and bridge-type constructors
are not added to that count.

Only this document and the newly authorized common `ConstructorRandom549Test.kt`
change. Its three methods are **UNCOMPILED/UNRUN**. No compiler, script, helper, numerical operation,
test, build, probe, Git or tracker operation was executed for this milestone.
The semantic index was unavailable; the delegated read-only explorer used exact
symbol/source reads without building an index. This is coverage accounting,
not a fresh runtime report, an all-input proof or closure of549/548.

## Common/actual API and policy

All four math declarations are in `src/commonMain/kotlin/io/heapy/krogu/math/`.
There are no platform-specific replacement class declarations or additional
constructor overloads for them. `PlatformRandom` is the expect input bridge:
JVM actual aliases `java.util.Random`, while non-JVM actual wraps Kotlin Random.
`RandomFactories.kt` supplies the preferred Kotlin Random call shapes.
`MathSerializable` actual declarations change only the serialization marker;
they introduce no math constructors or legacy interchange requirement.

All object constructor parameters remain non-null under the user's adopted
[Kotlin nullability policy](kotlin-nullability-policy.md). Kotlin caller/override
edits are permitted. The [573–578 acceptance report](non-null-api-573-verification.md)
records compiler rejection and real JVM null-boundary counterparts. Valid other
inputs plus a null argument require rejection at that boundary; exact NPE text
and priority among competing invalid arguments are diagnostic classification
under526, not additional mathematical constructor requirements. Java source
shape is a TCK/adapter concern, not a reason to add constructors or widen inputs.

## Evidence key

Every path below is relative to the repository. `common` means commonTest;
`JVM` means jvmTest or the separately documented public JVM harness.

| Key | Existing evidence and precise limit |
| --- | --- |
| L | [Accepted lexical/scale argument and coverage map](constructor-contract-549-coverage.md#conditional-lexical-and-scale-argument); common `IntegerParserOracleTest:8-16`, `CharacterOracleTest:8-65`, and JVM `DecimalConformanceTest:82-94`. Grammar and finite malformed/range/Unicode rows; integer primitives, allocations and arbitrary multi-character strings are not proved merely by row count. |
| B | [Byte-constructor proof](integer-byte-constructor-549-proof.md); common `IntegerByteOracleTest.constructorsAndArrayIsolation:8-34`, `MagnitudeLimitsTest:6-28`. All four byte overloads, selected invalid slices/signs and mutation controls; maximal allocation and required range remain548. |
| C | [Contextual coefficient proof](contextual-constructor-rounding-549-proof.md); common `ZeroContextScale567Test.contextualConstructorsRetainTheirOwnZeroScales:155-172`; `PortableOracleTest:296-301` consumes the `L/constructor` rows generated at `scripts/generate-oracle.main.kts:632-644`. Conditional exact-base/roundValue composition, finite zero/extreme-scale/carry/error controls; BI+context scale-zero wording tension remains526. |
| I | Common `src/commonTest/kotlin/io/heapy/krogu/math/migration/KotlinConversionExtensionsContractTest.kt:19-44,82-90`; JVM `src/jvmTest/kotlin/io/heapy/krogu/math/migration/KotlinConversionExtensionsConformanceTest.kt:38-65`. Own `NumericExtensions.kt:9-17` delegates directly to the Int/Long/coefficient constructors, so these are transitive constructor tests, not unrelated extension results. Common literal results and UNNECESSARY exceptions; JVM primitive boundaries, fields, all modes and selected precision/scale combinations. |
| D | Common `DoubleZeroContext569Test:12-65`: both signed zeros, finite exact fractions, all modes, a tie and UNNECESSARY, plus NaN/infinities with valid contexts. JVM `JdkOracleTest.floatingPointBoundaries:147-171` compares exact Double constructors for selected boundaries/subnormal/min/max and150 raw-bit values. Finite coverage, not all binary64 inputs. Double conversion extensions deliberately use canonical valueOf/text routes and must not be counted as exact Double-constructor tests. |
| A | Common `ValueIdentityContractTest.mutableInputSlicesCannotChangeCollectionKeys:119-168`: all four decimal char-array overloads, contextual rounding, input mutation and selected slices. `ZeroContextScale567Test:155-165` adds zero-scale controls. Accepted lexical proof covers copied slices; JVM `DecimalConformanceTest:82-94` adds finite invalid ranges. |
| M | Common `MathContextGrammarTest.compoundGrammarMatchesOracleIncludingErrors`, `characters0..7`, `longPrecisionTokens`; `ValueIdentityContractTest.everyContextModeAndPrecisionHasFieldBasedIdentity:236-262`; `PortableOracleTest.contextsAndRoundingConstants:635-643`. Direct fields/default mode, precision zero/extrema, negative precision, string grammar and all enum modes. Character mapping checks do not prove arbitrary whole-string grammar by themselves. |
| R | Common `RandomIntegerDistributionTest.zeroBitsReturnsZeroAndRecordsRandomConsumption:10-30`, `everySmallBytePayloadHasEqualMultiplicityInTheRequestedRange:41-57`; `PortableOracleTest.modularArithmeticAndRandomGeneration:655-664`; `PrimalityCertaintyTest.generationBudgetCoversConditioningOnAcceptanceWithoutIntOverflow:41-59`; [numerical probability argument](../NUMERICAL_ARGUMENTS.md#primality-and-prime-generation). Random-bit result/distribution controls and probablePrime/budget evidence; not every certainty-constructor call shape or caller RNG behavior. |
| J | JVM `JdkOracleTest.serializationAndJavaAdapters:171-181` compares a non-null java.util.Random random-bit constructor; public TEST `build/math-tck/upstream/test/jdk/java/math/BigInteger/BigIntegerTest.java:190,952-954,1055` uses successful certainty-bearing PlatformRandom constructors at certainties4/5/100. These public-test calls retain subset0 in the [accepted hosted campaign](public-math-ci-559.md#hosted-verification), whose source/artifact/run scope stays separate. |
| N | JVM `Validation578RngTest.exactPortableRngRowsReachJava:7-33`, `NullArgumentConformanceTest`, `Validation578StandaloneTest`, plus [573–578 report](non-null-api-573-verification.md). Ordinary null rejection and retained competing-invalid observations. A null-RNG invalid-bit row that accepts any rejection does not prove the required exception category for a non-null RNG. |

The short test names in this table resolve under
`src/commonTest/kotlin/io/heapy/krogu/math/` unless explicitly marked JVM, which
resolves under `src/jvmTest/kotlin/io/heapy/krogu/math/`. The full migration paths
are written out because their package/subdirectory differs.

## BigInteger: eight constructors and two constructor-like functions

`BigInteger.kt:31-38` declares the eight real constructors. `PlatformRandom` in
the signatures below is an own expect/actual bridge, not a copied implementation
of Java's RNG. `randomValue` and `primeValue` are internal delegation seams,
not public constructors.

| ID / public signature | Actual own route and result/validation branches | Existing coverage / open boundary |
| --- | --- | --- |
| BI1 `BigInteger(String)` | Delegates radix10 to BI2, then exact parser. | L; no separate algorithm gap from the one-argument wrapper. |
| BI2 `BigInteger(String, Int radix)` | `parse:1055-1086`: reject radix outside2..36, empty/sign-only/invalid digits, consume sign/leading zeros and positional digits; NumberFormatException for invalid text/radix. | L; integer arithmetic/range/allocation leaf contracts remain548. Extension radix configuration errors can differ before this constructor, so constructor evidence comes from the parser oracle itself. |
| BI3 `BigInteger(ByteArray)` | `fromBytes(...,true)` over the whole array; empty backing array rejects, signed big-endian conversion and normalized copy. | B; whole-array path and input/output isolation already covered. |
| BI4 `BigInteger(ByteArray, off, len)` | Same conversion after checked slice; nonempty backing array with accepted zero-length slice returns zero; bounds errors are IndexOutOfBoundsException. | B; empty backing array and empty selected slice are intentionally distinct. |
| BI5 `BigInteger(signum, ByteArray)` | Delegates whole slice to BI6. | B; no separate algorithm beyond the existing signed-byte evidence. |
| BI6 `BigInteger(signum, ByteArray, off, len)` | `signedBytes:1123-1134`: legal sign -1/0/1; sign0 plus nonzero selected magnitude rejects with NumberFormatException; unsigned magnitude conversion, then optional negation; legal zero magnitude yields zero. | B; bounds/sign compound-invalid order remains diagnostic, not a new mandatory priority. |
| BI7 `BigInteger(numBits, PlatformRandom)` | `randomBits:1034-1037`: negative numBits IllegalArgumentException; zero returns zero; otherwise bridge to common Random and byte sampling/masking at1048-1053. | J covers positive JVM results and Java zero controls; N covers null/invalid diagnostics. Shared random-bit calculation has R coverage. Exact non-null negative-bit exception witness is a small finite gap G2 below. |
| BI8 `BigInteger(bitLength, certainty, PlatformRandom)` | `generatePrime:1030-1032` rejects bitLength<2 with ArithmeticException, then bridges to the Random implementation. | J covers successful positive-certainty constructor use; N covers null/invalid observations. Non-null invalid-bit category and nonpositive-certainty constructor-result controls remain G1/G2. |
| RF1 `BigInteger(numBits, kotlin.random.Random)` function | `RandomFactories.kt:8` -> internal `randomValue:1028` -> `randomBits:1048-1053`: require nonnegative bits, allocate ceil(bits/8) bytes using Long sizing, mask excess high bits, unsigned fromBytes. | R directly covers non-null common results, zero and finite payload multiplicity. Zero callback count/subsequent RNG state are observations, not requirements. Non-null negative-bit exception category is G2. |
| RF2 `BigInteger(bitLength, certainty, kotlin.random.Random)` function | `RandomFactories.kt:11-12` -> `primeValue:1029` -> `generatePrime:1039-1047`: bitLength<2 ArithmeticException; bitLength2 chooses2/3; otherwise force top/low bits, compute certainty budget, return first candidate when rounds0 or repeat until the prime test accepts. | R exercises the same positive-certainty generator through probablePrime with fixed certainty100 and separately checks budget arithmetic, but no successful common RF2 call was found. Its direct nonpositive-certainty return path and non-null invalid-bit category are G1/G2. |

The PlatformRandom adapter itself (`PlatformRandom.kt:12-26`) delegates nextBytes
and implements nextBits through bytes; JVM actual is a typealias and non-JVM
actual delegates to its supplied/default Kotlin Random. This inventory does not
require unchanged PRNG consumption, universal termination under a hostile RNG,
or statistical quality from a deterministic/bad caller source. Those are
different from positive result/bit-length and valid-input exception requirements.

## BigDecimal: sixteen constructors

The public primary constructor is at `BigDecimal.kt:10-16`; the secondary
constructors are at25-39. Every coefficient path runs primary normalization:
exact-class BigInteger is retained, a subtype is normalized through its virtual
byte encoding into a base integer. Successful immutable storage is exact
`coefficient * 10^(-scale)`; subtype callback behavior remains541.

| ID / public signature | Actual own route | Existing coverage / qualification |
| --- | --- | --- |
| BD1 `BigDecimal(BigInteger, Int scale)` | Public primary; retain/normalize coefficient, store supplied Int scale exactly. | C/I and many stored-value oracle consumers; zero/extreme-scale controls. |
| BD2 `BigDecimal(BigInteger)` | BD1 with scale0. | I's plain coefficient extension delegates directly; C for exact temporary composition. |
| BD3 `BigDecimal(BigInteger, MathContext)` | Construct base BD2 temporary, private `roundValue:457-467`, private field copy. | Direct common zero constructor controls C; nonzero scale0 calculation already represented by explicit-scale rows and conditional proof C. A missing duplicate direct-call sample is not a new leaf-algorithm gap. Literal public scale-zero wording tension remains526. |
| BD4 `BigDecimal(BigInteger, Int scale, MathContext)` | Base BD1 temporary -> private roundValue -> private field copy. | C/I; all modes, selected carry/exact/scale barriers and UNNECESSARY. Conditional proof assumes digit/integer leaves, so not full548 coverage. |
| BD5 `BigDecimal(Int)` | Exact widening to Long -> BD6. | I common literals; JVM primitive boundaries. No separate conversion/rounding algorithm from BD6. |
| BD6 `BigDecimal(Long)` | `BigInteger.valueOf:1015-1023` handles cached small values and unsigned magnitude extraction including Long.MIN_VALUE; store scale0. | I common Long.MIN_VALUE and JVM boundary controls; no decimal parse approximation. |
| BD7 `BigDecimal(Int, MathContext)` | Widen to Long -> BD8. | I common rounded literal and UNNECESSARY; JVM all modes/selected precisions. |
| BD8 `BigDecimal(Long, MathContext)` | Exact base BD6 temporary -> private roundValue -> copy. | I; signed Long.MIN_VALUE contextual JVM cases and common successful rounding/UNNECESSARY. Adding direct constructor versions would duplicate these delegating extension routes. |
| BD9 `BigDecimal(String)` | `parse:874-907`: decimal grammar, mantissa/exponent, checked resulting scale -> coefficient -> private copy. NumberFormatException on invalid text/scale. | L; common migration text controls I and JVM malformed/Unicode/scale cases. |
| BD10 `BigDecimal(String, MathContext)` | Same parser -> private roundValue -> copy. | L/C/I plus existing decimal rounding fixtures; malformed input and inexact UNNECESSARY already represented. |
| BD11 `BigDecimal(CharArray)` | Whole-slice BD12. | A; mutation and zero/context-free scales. |
| BD12 `BigDecimal(CharArray, off, len)` | `characters:908-910` rejects invalid ranges with NumberFormatException, copies selected text, then BD9. | A/L; existing valid/invalid slices and alias isolation. |
| BD13 `BigDecimal(CharArray, MathContext)` | Whole-slice BD14. | A/C; no extra parser beyond whole-slice delegation. |
| BD14 `BigDecimal(CharArray, off, len, MathContext)` | Checked copied slice -> BD10. | A/L/C; four public TCK range+null cases are explicitly adapted/lost coverage under the NN policy, not four new missing range tests. |
| BD15 `BigDecimal(Double)` | `fromDouble:912-928`: nonfinite NumberFormatException; either signed zero -> scale0 zero; decompose binary exponent/mantissa, cancel factors2, then exact shift or coefficient*5^scale, apply sign, copy. | D common zero/nonfinite controls and JVM exact-conversion boundaries/raw-bit samples. Float/Double conversion extensions are deliberately excluded from this constructor evidence. |
| BD16 `BigDecimal(Double, MathContext)` | `roundedDouble:930-934`: exact BD15; zero returns exact zero, otherwise public round on the base temporary -> plus -> private roundValue; then copy. | D directly covers finite exact fractions, signed zeros, every mode, tie rounding and ArithmeticException for inexact UNNECESSARY; valid nonfinite inputs reject with NumberFormatException. No missing mandatory finite exception/result path identified here. All-binary64/domain proof remains separate. |

For all contextual rows, `P=0` preserves the exact constructor temporary; finite
precision uses the selected mode, and inexact UNNECESSARY rejects arithmetically.
Zero constructor scale is operation-specific: explicit-scale constructors retain
that scale, while Double starts at scale0. Public plus/round zero canonicalization
must not be substituted for the constructor's private rounding path. C and D
cover that distinction without proposing duplicate regressions.

## MathContext: three constructors; RoundingMode: none

| ID / public signature | Actual own route | Existing coverage / qualification |
| --- | --- | --- |
| MC1 `MathContext(Int precision, RoundingMode)` | Primary at `MathContext.kt:5-8`: precision>=0 or IllegalArgumentException; store given non-null enum. | M directly covers five precisions including0/Int.MAX_VALUE and every mode; N covers the real JVM null boundary. MC2's negative precision exercises the same primary guard. |
| MC2 `MathContext(Int precision)` | Delegate to MC1 with HALF_UP (`:14`). | M checks default mode, constants and negative-precision rejection. |
| MC3 `MathContext(String)` | `parse:37-60` checks prefix/separator, optional sign, digits/bounded magnitude and exact enum token; private Pair constructor delegates to MC1. Invalid grammar/range or negative precision rejects with IllegalArgumentException. | M extensive finite grammar/BMP/token-length controls and JVM selected comparisons; exact wording/compound-invalid priority diagnostic526. |
| RoundingMode | Enum constants only; no public constructor. | `ValueIdentityContractTest.roundingModesHaveStableNamesLegacyMappingAndIsolatedSnapshots:265` covers enum/legacy mapping. `valueOf(Int)` and generated name lookup are factories, not missing constructors. |

## Identified finite gaps and prepared regressions

No production defect was established by this inventory. The following finite
assertion gaps were identified before adding the three new common method sources;
they remain runtime acceptance gaps until those methods compile and execute.

**G1: common certainty-bearing factory result path.** Existing common positive
prime generation calls probablePrime, which fixes certainty100; it does not call
RF2 or directly exercise its rounds0 return with nonpositive certainty. JVM
public tests cover BI8 with positive certainties, and budget tests prove that
nonpositive certainties select zero rounds, but those are not a common RF2
constructor-result witness. Two bounded common methods use a controlled non-null
Kotlin Random, asserting positive result and exact requested bit length for all
returning cases and independently trial-dividing tiny positive-certainty results.
The prepared methods
`ConstructorRandom549Test.certaintyFactoryReturnsPositiveSmallPrimes` and
`nonpositiveCertaintyStillReturnsPositiveRequestedBitLength` implement those
controls using bit lengths 2/8 and certainties 1/32/100 or Int.MIN_VALUE/-1/0.
They do not require every nonpositive-certainty
sample to be prime, exact RNG callback counts, unchanged subsequent state or a
host's random sequence. This adds direct branch/result evidence, not a statistical
probability proof; 547/548 and the existing conditional probability argument stay
open where applicable.

**G2: exact invalid-bit exception type with a non-null RNG.** The inspected common
random tests exercise valid bit counts. Existing 578 null+invalid rows deliberately
allow competing-invalid rejection without imposing a global exception priority.
They therefore do not establish the isolated public requirements:
`numBits<0` -> IllegalArgumentException, and certainty-constructor `bitLength<2`
-> ArithmeticException. One bounded common method calls RF1 and RF2 with a real
non-null source and asserts those categories. A JVM counterpart through java.util.Random can cover BI7/BI8's separate
bridge-side validation guards; positive BI8 behavior already has public evidence.
The prepared common method
`ConstructorRandom549Test.nonnullRandomUsesRequiredInvalidBitExceptionCategories`
checks RF1 at Int.MIN_VALUE/-1 and RF2 at Int.MIN_VALUE/-1/0/1 with certainty 0/1.
It asserts those isolated exception classes and no message, no-draw behavior or
invalid-null precedence. A new JVM bridge counterpart is not included in this
finite common-only scope. No giant allocation is needed. The public requirements
are in the
[Java25 random constructors](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html#%3Cinit%3E(int,int,java.util.Random)).

The test's controlled `SmallPrimeRandom` supplies byte payload `FB`, whose
unsigned value 251 is prime: every possible prime divisor at most sqrt(251) is
among 2/3/5/7/11/13, and none divides it. In the current own RF2 route, an 8-bit
candidate from that byte already has its high/low bits set; a positive certainty
therefore has a prime candidate without an unbounded search through repeated
composites. The two-bit special case returns 2 or 3. The test asserts neither an
exact returned numeric value nor object identity, byte/nextBits call counts,
seed-state preservation or a distribution. It checks result sign/bit length and,
for positive-certainty controls only, independent trial division. With at most
eight-bit results the divisor-square loop is bounded and cannot overflow Int.
This fixture documents its current source reachability; it is not a guarantee
of termination for every hostile caller RNG or a proof of the probability bound.
The existing common zero-bit test already supplies its ordinary result control,
so no duplicate zero-bit method was added.

Independent parent review accepted the complete overload accounting and the
three common test bodies. It also checked the BI7/BI8 bridge guards directly:
their negative-bit checks occur before adapter creation and use exactly the
same required exception categories; BI7 zero returns ZERO directly. Combined
with the common-path controls and retained successful JVM bridge tests, no
duplicate JVM invalid-input test is needed for that source composition. This
does not turn the prepared common methods into executed evidence. The parent
removed only their temporary UNRUN class comment before validation; the three
method bodies and mathematical expectations are unchanged.

All three new methods call the public common Kotlin Random functions, then
`randomValue`/`primeValue` and the exact helper routes in RF1/RF2 above. They do
not bypass the factory using internal numerical helpers or host java.math.
No existing test, fixture, public TEST source or production declaration changed.
Independent review and a later root-authorized common/JVM validation remain
required; prepared method count is not executed evidence.

Do not add duplicate Int/Long or Double constructor tests merely because existing
tests enter through numeric extensions or a different test class. In particular
Int/Long extensions directly call these constructors, while Double extensions
do not: that distinction was checked in own NumericExtensions source.
Likewise, BD3's thin scale0 composition is already conditionally proved from
BD4's helper evidence; its wording ambiguity cannot be resolved by another
sample or by silently fixing its scale to zero.

This finite list does not discharge arbitrary Unicode/string grammar, subtype
callback semantics541, probability/RNG assumptions547, required magnitude and
intermediate-allocation range548, all-binary64 behavior, or the specification
classification526. Existing proofs remain conditional on their explicitly named
leaves. Sample counts, a green hosted job and compiler null-rejection records
must not be promoted to all-domain constructor closure.

## Source pins

All paths below are own sources read for this inventory. The accepted earlier
proofs and nullability reports retain their own historical evidence scopes.

```text
df55cb8c8c6a291b59f6ee6daf365c9b4c38e4e1f33583e108183c5a28039fb3  src/commonMain/kotlin/io/heapy/krogu/math/BigInteger.kt
08b4329ea5e902f1043107d42ddb02592be537497f6b0990927e8c67c4ed3f29  src/commonMain/kotlin/io/heapy/krogu/math/BigDecimal.kt
d3690387b1345f90cc927044a1a450983dd0d2ce6e3e47054384c31979fd5261  src/commonMain/kotlin/io/heapy/krogu/math/MathContext.kt
e47d7aa314ddaa9c79ba13b79a00beadb8be96cbb473cd4ad2dfae3e0501a5ee  src/commonMain/kotlin/io/heapy/krogu/math/RoundingMode.kt
d66f6002d3e2f32b1a2f03143984eee2da55a2fc7edda2bf6e8bdada41257c20  src/commonMain/kotlin/io/heapy/krogu/math/RandomFactories.kt
20fdf7c5c76ed4207c03859b4cb60247a16e2ec4337ee674c3a85ce6101e5123  src/commonMain/kotlin/io/heapy/krogu/math/NumericExtensions.kt
e408e6fad2a92acec592520ea3eaf1c414493a6db708db9ef9ae686b1303f3d0  src/commonMain/kotlin/io/heapy/krogu/math/PlatformRandom.kt
5acb4c2184f981699c5fec755bf223ab61a441af84275c5151da6cc4fde345fb  src/jvmMain/kotlin/io/heapy/krogu/math/PlatformRandomJvm.kt
8673184b6bfcd9117a12c91a3152f265afec42f2f17d1309963ac5498c472319  src/nonJvmMain/kotlin/io/heapy/krogu/math/PlatformRandom.kt
```

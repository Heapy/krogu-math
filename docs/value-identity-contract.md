# Value identity, ordering, and immutability

This audit belongs to kotgent `local:550`, based on
`3c278955b858eb58a386a11ab1e1a2f85388a2da`. The implementation remains independent
common Kotlin in `io.heapy.krogu.math`, licensed under Apache-2.0. This is a
focused contract audit, not an official TCK pass or a claim that every API is
fully compatible.

## Contract and evidence

| Type | Identity and order | Focused evidence |
| --- | --- | --- |
| `BigInteger` | Integer value determines equality; natural order agrees with equality. Equal objects have equal, stable hashes. | Independent string/byte/subclass constructions; sign, antisymmetry, transitivity; common sorting, set and map keys; JVM signed boundary differential. |
| `BigDecimal` | Equality requires both value and scale. Natural order groups numerically equal representations into cohorts, including zero; min/max comparison ties return the receiver. | Scale-sensitive set/map keys; equal-scale independent constructions and truthful subclasses; order laws and cohort substitution; both min/max receiver positions under four ordinary/truthful masks; JVM hash/order/equality differential and sorted collections. |
| `MathContext` | Precision and rounding mode both determine equality, even at precision zero. | All eight modes at precision 0, 1, 7, 34, and `Int.MAX_VALUE`; field access, pairwise equality, equal-object hashes, canonical text/parser round trips and four constants. |
| `RoundingMode` | Eight named enum instances in the public declaration order; legacy integer codes map to those instances. | All names/ordinals/codes, identity and collection keys, `values()` array isolation and copied `entries` isolation, valid/invalid string and integer lookup; JVM public legacy fields. |

The portable additions are in
`src/commonTest/kotlin/io/heapy/krogu/math/ValueIdentityContractTest.kt`.
The live reference checks are in
`src/jvmTest/kotlin/io/heapy/krogu/math/ValueIdentityConformanceTest.kt`.

The JVM integer campaign has 73 distinct values: zero and both signs of
`2^b - 1`, `2^b`, and `2^b + 1`, for twelve bit positions around byte,
30-bit limb, 32-bit chunk, and 64-bit boundaries. It compares all 5,329 ordered
pairs and checks each value's hash and byte encoding. The decimal campaign
has 69 coefficient/scale representations and compares all 4,761 ordered pairs.
Its coefficients straddle signed 64-bit compact-storage boundaries; its scales
include both integer extremes and their adjacent values. Positive and negative
`2`, `2.0`, and `2.00` add explicit cohort witnesses. Extreme-scale cases perform
comparison, hashing, canonical text, and collection operations only, without
plain-string expansion or arithmetic alignment.

Common expected results are independent small witnesses: integer division
`100 / 7 = 14` remainder `2`, negative truncation, zero quotient/remainder,
square roots `100 = 10²` and `101 = 10² + 1`, decimal
`100.00 / 7.0 = 14.0` remainder `2.00`, and the literal order/name/constant
tables. Algebraic checks require symmetry, transitivity, sign reversal and
cohort substitution. They do not calculate expected hashes using the
implementation's hash algorithm.

Exact numerical integer/decimal hash results in JVM tests are pinned-runtime
compatibility observations. Public API documentation requires consistency with
equality but does not prescribe those numerical hash formulas. The common tests
assert the portable equality/hash laws independently.

## Isolation and dispatch boundaries

The common tests mutate byte input slices, every decimal character-constructor
variant (whole array or offset slice, exact or context-rounded), output byte
arrays, and both slots of result pair arrays. Independently obtained result
arrays and subsequent calls must preserve their values. Receiver/divisor value
and hash, collection lookup, and shared `ZERO`, `ONE`, and `TEN` constants must
remain stable after compositions. Sharing immutable result elements is allowed;
no test requires fresh objects for those elements or undocumented cache identity.

Required common tests use ordinary values and truthfully delegating subclasses
for equality, hashing and comparison. A separate, retained JVM diagnostic probes
equality/hash and integer comparison with poisoned getters against the pinned
JDK. These throwing getters violate their own method contracts, and the public
API does not prescribe internal callback counts, omission, or ordering. The
diagnostic is a non-normative reference observation, not a portable callback
independence guarantee. Decimal comparison is exercised here with truthful
getters only. Subclass coefficient normalization and ordinary coefficient
identity are already exercised by `DecimalCoefficientSubclassTest` as pinned
reference observations. The public `unscaledValue()` contract guarantees the
value, not object identity; this audit adds no coefficient identity or broader
caching guarantee. Existing coefficient tests are unchanged.

Enum hashes use object identity. The numerical hash of a `RoundingMode` instance
is not portable across processes, platforms, or distinct enum classes. Thus
`MathContext` hashes are checked for consistency with equality within each
implementation, while fields, equality, and text are compared to the JDK.
No test requires a krogu enum/context hash to equal a JDK enum/context hash.
This is the public hash contract, not a waiver of a numerical mismatch.

## Existing evidence retained

These additions complement the existing suites rather than replacing their
larger campaigns:

- `HashOracleTest` and JVM `HashSubclassContractTest`: integer hash outcomes and
  independence from public magnitude operations.
- `IntegerByteOracleTest`: input/output byte-array isolation and offset/signed
  constructor cases.
- `IntegerComparableContractTest`, `DecimalComparableContractTest`, and JVM
  `DecimalComparisonSubclassTest`: comparable integration and public callback
  dispatch, including extreme scales.
- `MathContextGrammarTest`, `DecimalContextContractTest`, and
  `DecimalContextDispatchOracleTest`: grammar, context operations and callbacks.
- `DecimalRemainderArrayOracleTest`: overridden result arrays, bounds failures,
  and second-element identity.
- `DecimalCoefficientSubclassTest`: existing pinned observations of subclass
  byte normalization and ordinary-coefficient identity, beyond the public
  unscaled-value guarantee.

## Reference provenance and scope

Reference material is limited to the public Java SE 25 API documentation:
[BigInteger](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigInteger.html),
[BigDecimal](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html),
[MathContext](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/MathContext.html),
and [RoundingMode](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/RoundingMode.html).
Live differential tests require BellSoft Liberica runtime `25.0.4+9-LTS` and
vendor `BellSoft`. Only public JDK calls and public-field reflection are used.
No OpenJDK, Kotlin standard-library, or third-party numerical implementation
sources were read or copied. Java adapters are confined to JVM test code.
Production Java source compatibility, legacy serialization/proxies, and old
stream migration are outside this audit's scope.

## Validation status and build handoff

Parent build-window grant was verified in kotgent comment `1730`. All gates ran
serially on the unchanged base commit on 2026-10-14, with
`JAVA_HOME=/Users/yoda/.sdkman/candidates/java/25.0.4-librca`, Gradle 9.8.1,
Kotlin 2.4.21, `--no-daemon`, and the final project-property override
`-Pkotlin.user.home=/private/tmp/krogu-math-550-kotlin-7b01e9de`.

| Final gate | XML suites | Tests | Failures | Errors | Skipped | Terminal result / duration |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| `jvmTest` | 14 | 49 | 0 | 0 | 0 | exit 0 / 10s |
| `jsNodeTest` | 10 | 39 | 0 | 0 | 0 | exit 0 / 20s |
| `wasmJsNodeTest` | 10 | 39 | 0 | 0 | 0 | exit 0 / 1m 37s |
| `macosArm64Test` | 10 | 39 | 0 | 0 | 0 | exit 0 / 1m 57s |

Every final invocation specifies filters after its own Gradle task, with a
trailing wildcard, for all ten common suites listed above. JVM additionally
selects `ValueIdentityConformanceTest`, `HashSubclassContractTest`,
`DecimalComparisonSubclassTest`, and `NullArgumentConformanceTest`. Each target
executed all ten new common tests; JVM also executed all five new reference
tests. No final gate has a skipped or disabled test. These counts establish the
scoped evidence only; parent integration and supported CI remain separate.

Evidence is retained at `/private/tmp/krogu-math-550-evidence-7b01e9de/`:
`commands.txt` holds every exact terminal command and outcome;
`final-counts.json` holds the XML totals; `jvm-final.log`, `js-final.log`,
`wasm-final.log`, and `macarm-final.log` hold the complete output. Corresponding
`*-final-xml/` directories preserve XML and binary results.

Two initial attempts were invalid gates and are retained explicitly. The
class-only JS filters matched no test methods (86s, exit 0, HTML root group
reported one skipped item); `js.log`, `js-initial-html/`, and
`js-initial-results/` preserve that evidence. Adding the trailing wildcard
executed the required 39 tests. Initial Wasm setup failed before compilation or
test execution (21s, exit 1) on a global npm-tooling cache lock with `Operation
not permitted`; `wasm.log` preserves the failure. Replacing the ineffective
`-Dkotlin.user.home` with the `-P` project property isolated that tooling cache.
The initial JVM run had already passed 49 tests (42s); its log/XML are retained,
and it was repeated with the corrected property for the final gate. No tests,
fixtures, or repository build settings were changed to resolve these invocation
issues. The generated untracked Wasm Yarn lock was preserved in the evidence
directory and removed from the worktree.

The executed test sources retain their approved SHA-256 hashes:

```text
25f844e69b1eaefbe5ab9d92dfe3797f25c7f82dbe4e03e4bb11407a27206f6e  ValueIdentityContractTest.kt
3a2ce890427afa0cdf7dd1e6c5cfde1282b62fa69ec5cf7b280c882d448b73f3  ValueIdentityConformanceTest.kt
```

The frozen documentation hash and final independent read-only review result are
recorded in linked task activity. Only the two new test files and this document
are changed. The parent integrates serially and runs supported CI; no official
TCK pass is claimed.

Parent integration on main after division-normalization integration passed in
1m 08s: all ten new common tests and five new JVM tests, with zero failures,
errors or skips. The parent used `./gradlew --no-daemon jvmTest` with repeated
`ValueIdentityContractTest*` / `ValueIdentityConformanceTest*` filters; its log is
`/private/tmp/krogu-root-550-integration.log`. Fresh supported CI remains the
closure gate.
